 package mobi.monaca.framework.nativeui.container;
 
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.UIUtil;
 import mobi.monaca.framework.nativeui.component.Component;
 import mobi.monaca.framework.psedo.R;
 import mobi.monaca.framework.util.MyLog;
 
 import org.json.JSONObject;
 import android.graphics.Bitmap;
 import android.graphics.PorterDuff;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Handler;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import static mobi.monaca.framework.nativeui.UIUtil.*;
 
 public class TabbarItem implements Component {
 
     protected Drawable drawable;
     protected TabbarItemView view;
     protected JSONObject style;
     protected UIContext context;
     protected String link;
     protected Handler handler;
 
     public TabbarItem(UIContext context, String link, JSONObject style) {
     	MyLog.v(TAG, "TabbarItem constructor. link:" + link  + ", style:" + style);
         this.view = new TabbarItemView(context);
         this.style = style != null ? style : new JSONObject();
         this.link = link;
         this.context = context;
         this.handler = new Handler();
 
         style();
     }
 
     public TabbarItemView getView() {
         return view;
     }
 
     @Override
     public JSONObject getStyle() {
         return style;
     }
 
     @Override
     public void updateStyle(JSONObject update) {
         updateJSONObject(style, update);
         style();
     }
 
     /*
      * text: テキスト [string] (default: "") image: 画像への相対パス [string] (default: "")
      * badgeText: バッジのテキスト [string] (default: "")
      */
     protected void style() {
         view.setText(style.optString("text"));
         view.setBadgeText(style.optString("badgeText"));
 
         String imagePath = style.optString("image");
 		if (imagePath.length() > 0) {
         	MyLog.v(TAG, "we have image icon for Tabbar!");
             Bitmap bitmap = context.readBitmap(imagePath);
             if (bitmap != null) {
                 view.setIconBitmap(bitmap);
             }
         }
     }
 
     public class TabbarItemView extends LinearLayout {
 
         protected TextView textView;
         protected boolean isSelected = true;
         protected TextView badgeTextView;
         protected ImageView imageView;
 
         public TabbarItemView(UIContext context) {
             super(context);
 
             setOrientation(LinearLayout.VERTICAL);
 
             FrameLayout layout = new FrameLayout(context);
 
             setBackgroundResource(R.drawable.monaca_selected_tab_bg);
 
             textView = new TextView(context);
             textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
             textView.setTextColor(0xffffffff);
             textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                     context.getFontSizeFromDip(Component.TAB_TEXT_DIP));
             textView.setShadowLayer(1f, 0f, -1f, 0xcc000000);
 
             imageView = new ImageView(context);
             imageView.setPadding(UIUtil.dip2px(context, 16), 0,
                     UIUtil.dip2px(context, 16), 0);
             imageView.setColorFilter(0xffffffff, PorterDuff.Mode.SRC_IN);
 
             badgeTextView = new TextView(context);
             badgeTextView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
             badgeTextView.setVisibility(View.GONE);
             badgeTextView.setBackgroundColor(0xff990000);
             badgeTextView.setTextColor(0xffffffff);
             badgeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                     context.getFontSizeFromDip(Component.TAB_BADGE_TEXT_DIP));
             badgeTextView.setShadowLayer(1.0f, 0f, 1f, 0x99000000);
             badgeTextView.setTypeface(Typeface.DEFAULT_BOLD);
             badgeTextView.setBackgroundDrawable(context.getResources()
                     .getDrawable(R.drawable.monaca_tab_badge));
 
             layout.addView(imageView, new FrameLayout.LayoutParams(
                     FrameLayout.LayoutParams.WRAP_CONTENT,
                     FrameLayout.LayoutParams.WRAP_CONTENT));
 
             layout.addView(badgeTextView, new FrameLayout.LayoutParams(
                     FrameLayout.LayoutParams.WRAP_CONTENT,
                     FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
 
             setGravity(Gravity.CENTER);
 
             addView(layout, new LinearLayout.LayoutParams(
                     LinearLayout.LayoutParams.WRAP_CONTENT,
                     LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
 
             addView(textView, new LinearLayout.LayoutParams(
                     LinearLayout.LayoutParams.WRAP_CONTENT,
                     LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
 
             switchToUnselected();
         }
 
         public void setIconBitmap(Bitmap bitmap) {
             bitmap = UIUtil.resizeBitmap(bitmap, dip2px(context, 28));
             Drawable icon = new BitmapDrawable(context.getResources(), bitmap);
             icon.setColorFilter(isSelected() ? 0xffffffff : 0x99ffffff,
                     PorterDuff.Mode.SRC_IN);
             imageView.setImageDrawable(icon);
         }
 
         @Override
         protected void finalize() throws Throwable {
             super.finalize();
             imageView.setImageDrawable(null);
         }
 
         public void setBadgeText(String text) {
             badgeTextView.setText(text);
 
             if (text.length() == 0) {
                 badgeTextView.setVisibility(View.GONE);
             } else {
                 badgeTextView.setVisibility(View.VISIBLE);
             }
         }
 
         public void setText(String text) {
             textView.setText(text);
         }
 
         public void setTextColor(int color) {
             textView.setTextColor(color);
         }
 
         public void initializeSelected() {
             if (!isSelected) {
                 getBackground().setAlpha(0x33);
 
                 textView.setTextColor(0xffffffff);
                 isSelected = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        context.changeCurrentUri(link);
                    }
                });
             }
             imageView.setAlpha(0xff);
             invalidate();
         }
 
 
         public void switchToSelected() {
         	MyLog.v(TAG, "switchToSelected() link:" + link);
             if (!isSelected) {
                 getBackground().setAlpha(0x33);
 
                 textView.setTextColor(0xffffffff);
                 isSelected = true;
                 handler.post(new Runnable() {
                     @Override
                     public void run() {
                         context.loadRelativePathWithoutUIFile(link);
                     }
                 });
             }
             imageView.setAlpha(0xff);
             invalidate();
         }
 
         public void switchToUnselected() {
             if (isSelected) {
                 getBackground().setAlpha(0);
 
                 textView.setTextColor(0x99ffffff);
                 isSelected = false;
             }
 
             imageView.setAlpha(0x66);
             invalidate();
         }
 
         public boolean isSelected() {
             return isSelected;
         }
     }
 
 }
