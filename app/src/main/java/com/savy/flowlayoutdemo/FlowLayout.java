package com.savy.flowlayoutdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    //所有的子控件的容器
    List<List<View>> lists = new ArrayList<>();
    //把每一行的行高存起来
    List<Integer> listlineHeight = new ArrayList<>();

    private boolean isMeasured = false;

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet p) {
        return new MarginLayoutParams(getContext(), p);
    }

    //在被调用者方法之前，它的父容器已经把它的测量模式改成了当前控件的测量规则
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //获取父容器的参考值
        int widthSize  = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        Log.e("DEBUG", "width reference is " + widthSize);
        Log.e("DEBUG", "height reference is " + heightSize);

        //获取自身的模式数据
        int widthMode  = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //保存当前控件中子控件的最大宽度和高度
        int childCountWidth = 0;
        int childCountHeight = 0;

        if (!isMeasured) {
            isMeasured = true;
            Log.e("DEBUG", "test isMeasured in if " + isMeasured);
        } else {

            Log.e("DEBUG", "test isMeasured in else " + isMeasured);

            //当前控件中一行使用的宽度值
            int lineCountWidth = 0;
            //当前一行中最高的子控件的高度
            int lineMaxHeight = 0;

            //存储每个子控件的宽高
            int iChildWidth = 0;
            int iChildHeight = 0;

            //创建一行的容器
            List<View> viewList = new ArrayList<>();

            //遍历所有的子控件
            int childCount = getChildCount();
            for (int x = 0; x < childCount; x++) {
                //先获取子控件的view
                View child = getChildAt(x);
                //测量子控件
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();

                //计算当前子控件的实际宽度
                //计算子控件的实际所占空间宽度和高度
                iChildWidth = child.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                iChildHeight = child.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;

                //当这个子控件的宽度累加之后，是否大于它当前父容器允许的最大值
                if (iChildWidth + lineCountWidth > widthSize) {
                    //需要换行，保存上一行的信息
                    //每次换行的时候，都要比较当前行和前面行，谁的宽度最大
                    childCountWidth = Math.max(lineCountWidth, childCountWidth);
                    //如果需要换行，要累加行高
                    childCountHeight += lineMaxHeight;
                    //把行高记录到集合中
                    listlineHeight.add(lineMaxHeight);
                    //把这一行的数据放进总的容器
                    lists.add(viewList);

                    //重新创建一个新的viewList
                    viewList = new ArrayList<>();

                    //讲每一行的总长度初始化，新起了一行，所以现在的
                    lineCountWidth = iChildWidth;
                    //高度也重新初始化
                    lineMaxHeight = iChildHeight;

                    //当前item
                    viewList.add(child);

                } else {
                    //不需要换行
                    lineCountWidth += iChildWidth;

                    //对比子控件谁的高度最高
                    lineMaxHeight = Math.max(lineMaxHeight, iChildHeight);

                    //如果当前不需要换行，就需要保存当前控件保存在一行中
                    viewList.add(child);
                }

                if ( x == childCount - 1) {
                    //最后一行单独对待
                    childCountWidth = Math.max(lineCountWidth, childCountWidth);
                    childCountHeight += lineMaxHeight;

                    listlineHeight.add(lineMaxHeight);
                    lists.add(viewList);
                }
            }
        }

        //设置控件最终的大小
        int measuredWidth = widthMode == MeasureSpec.EXACTLY ? widthSize : childCountWidth;
        int measuredHeight = heightMode == MeasureSpec.EXACTLY ? heightSize : childCountHeight;
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //摆放子空间的位置
        int left, top, bottom, right = 0;

        //保存上一行控件的边距
        int countLeft = getPaddingLeft();

        //保存上一行高度的边距
        int countTop = getPaddingTop();

        //遍历所有行
        for (List<View> views : lists) {
            //遍历每一行的控件
            for (View view : views) {
                //获取每一个控件的参数对象
                MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
                left = countLeft + layoutParams.leftMargin;
                top = countTop + layoutParams.topMargin;
                right = left + view.getMeasuredWidth();
                bottom = top + view.getMeasuredHeight();
                view.layout(left, top, right, bottom);

                countLeft += view.getMeasuredWidth() + layoutParams.rightMargin + layoutParams.leftMargin;
            }

            //获取到当前这一行的position
            int index = lists.indexOf(views);
            countLeft = getPaddingLeft();
            countTop += listlineHeight.get(index);
        }
    }

}
