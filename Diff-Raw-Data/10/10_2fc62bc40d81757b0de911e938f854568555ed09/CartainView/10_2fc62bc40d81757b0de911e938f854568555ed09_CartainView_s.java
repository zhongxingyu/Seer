 /*
  * Copyright 2012 akaiosorani(akaiosorani@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package jp.srgtndr.akaiosorani.android.cartain;
 
 import java.util.Date;
 
 import jp.srgtndr.akaiosorani.android.cartain.controller.AppInfo;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;    
 import android.graphics.PixelFormat;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;    
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 public class CartainView extends FrameLayout {
 
     private static final String SYNC_KEY = "CartainView";
 
     private static CartainView view = null;
 
     private TextView label;
     
     private static int drawingCount = 0;
 
     CartainView(Context context) {
         super(context);
         setLayout(context);
     }
 
     protected void setLayout(Context context)
     {
         setBackgroundColor(Color.GREEN);
 
         LayoutParams params = this.generateDefaultLayoutParams();
         params.gravity = Gravity.LEFT;
 
         label = new TextView(context);
         label.setTextColor(Color.BLACK);
         label.setText(" ");
         label.setTextSize(8);
         addView(label, params);
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
        Log.d("cartain", "ondraw" + new Integer(drawingCount++).toString());
 /*
     	super.onDraw(canvas);
         Date current = new Date(System.currentTimeMillis());
 //      label.setText(current.toLocaleString());
         label.setText(AppInfo.getMemoryInfo(getContext()));
 */
     }
     public static CartainView createView(Context context, boolean isIconOnLeft) {
         CartainView v = new CartainView(context);
         WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
 
         WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                 50, 50, 
                 WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                 WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                 WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                 PixelFormat.TRANSLUCENT);
         params.x = 0;
         params.y = 0;
         params.alpha = 0.3f;
         
         params.gravity = (isIconOnLeft ? Gravity.LEFT : Gravity.RIGHT) |Gravity.CENTER_VERTICAL;
         wm.addView(v, params);
         Log.d("cartain", "add to wm");
         view = v;
         return v;
     }
 
     public static void destroyView(Context context)
     {
         if (view==null) return;
         synchronized (SYNC_KEY) {
             if (view==null) return;
             WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
             wm.removeView(view);
             view = null;
             Log.d("cartain", "remove from WM");
         }
     }
 
     public static void show()
     {
         if(view==null){
             return;
         }
         view.setVisibility(View.VISIBLE);
         Log.d("cartain", "show icon");
     }
 
     public static void hide()
     {
         if(view==null){
             return;
         }
         view.setVisibility(View.INVISIBLE);
         Log.d("cartain", "hide icon");
     }
 
 }
