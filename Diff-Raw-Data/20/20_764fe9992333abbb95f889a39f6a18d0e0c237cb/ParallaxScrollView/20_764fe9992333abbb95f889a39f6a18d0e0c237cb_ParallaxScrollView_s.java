 package net.nessness.android.sample.parallaxscroll;
 
 import java.util.ArrayList;
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.HorizontalScrollView;
 
 public class ParallaxScrollView extends HorizontalScrollView {
 
     /** 連動させるレイヤ。 一番上のレイヤが使用する */
     private ArrayList<ParallaxScrollView> mBackLayers;
     /** スクロール比率。 後ろのレイヤ用 */
     private float mRatio;
 
     /**
      * このレイヤーと一緒に動かす背景を設定
      * 
     * @param hsv このレイヤーと一緒に動かす背景. nullを渡すとそれまでaddしたレイヤーがクリアされる
      * @return this
      */
    public ParallaxScrollView addBackLayer(ParallaxScrollView hsv, float ratio) {
         if(mBackLayers == null) {
             mBackLayers = new ArrayList<ParallaxScrollView>();
         }
        mBackLayers.add(hsv);
        hsv.setScrollRatio(ratio);

         return this;
     }
 
     @Override
     protected void onScrollChanged(int l, int t, int oldl, int oldt) {
         super.onScrollChanged(l, t, oldl, oldt);
         // 力技
         if(mBackLayers != null) {
            for(ParallaxScrollView hsv: mBackLayers) {
                hsv.scrollTo(l, 0);
             }
         }
     }
 
     @Override
     public void scrollTo(int x, int y) {
         super.scrollTo((int)(x * mRatio), y);
     }
 
     public void setScrollRatio(float ratio) {
         mRatio = ratio;
     }
 
     private void init() {
         setScrollRatio(1f);
     }
 
     public ParallaxScrollView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
 
     public ParallaxScrollView(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public ParallaxScrollView(Context context) {
         super(context);
         init();
     }
 
     /**
      * タッチイベントを伝播する<br>
      * 
      */
     public static class OnTouchDispatcher implements View.OnTouchListener {
 
         /** イベントの伝播先 */
         private final View mTarget;
         /** trueなら自身のタッチイベントを抑止(スクロールなど) */
         private final boolean mConsume;
 
         /**
          * @param receiver イベントの伝播先 伝播させない場合はnull
          * @param consume trueタッチイベントを消費する
          */
         public OnTouchDispatcher(View receiver, boolean consume) {
             mTarget = receiver;
             mConsume = consume;
         }
 
         @Override
         public boolean onTouch(View v, MotionEvent e) {
             if(mTarget != null) {
                 mTarget.dispatchTouchEvent(e);
             }
             return mConsume;
         }
     }
 }
