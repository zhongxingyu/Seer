 package net.avh4.framework.uilayer.android;
 
 import android.app.Activity;
 import android.content.Context;
import android.graphics.Rect;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.widget.ImageView;
 import net.avh4.framework.data.android.AndroidDataStore;
 import net.avh4.framework.data.android.AndroidExternalStorage;
 import net.avh4.framework.uilayer.R;
 import net.avh4.framework.uilayer.UI;
 import net.avh4.framework.uilayer.UILayer;
 import net.avh4.framework.uilayer.scene.AndroidSceneRenderer;
 import org.picocontainer.DefaultPicoContainer;
 import org.picocontainer.MutablePicoContainer;
 
 public class AndroidSceneRendererActivity extends Activity {
 
     private UI ui;
     private AndroidSceneRenderer uiView;
     protected final MutablePicoContainer pico;
    private int statusBarHeight;
 
     public AndroidSceneRendererActivity() {
         super();
         pico = new DefaultPicoContainer();
         pico.addComponent(this);
         pico.addComponent(AndroidDataStore.class);
         pico.addComponent(AndroidExternalStorage.class);
 
         ((AndroidUILayerService) UILayer.service).init(pico.getComponent(Context.class));
     }
 
     public AndroidSceneRendererActivity(Class<? extends UI> uiClass) {
         this();
         pico.addComponent(uiClass);
     }
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setUI(pico.getComponent(UI.class));
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_UP) {
            ui.click((int) event.getX(), (int) event.getY() - getStatusBarHeight());
             uiView.invalidate();
         }
         return true;
     }
 
     public void setUI(final UI ui) {
         this.ui = ui;
         runOnUiThread(new Runnable() {
             public void run() {
                 if (AndroidSceneRendererActivity.this.ui != null) {
                    uiView = new AndroidSceneRenderer(AndroidSceneRendererActivity.this,
                            AndroidSceneRendererActivity.this.ui);
                     setContentView(uiView);
                 } else {
                     final ImageView placeholder = new ImageView(
                             AndroidSceneRendererActivity.this);
                     placeholder.setImageResource(R.drawable.icon);
                     setContentView(placeholder);
                 }
             }
         });
     }
 
    private int getStatusBarHeight() {
        final Rect rect= new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        final int statusBarHeight= rect.top;
        if (statusBarHeight == 0) {
            throw new RuntimeException("Too early to call getStatusBarHeight(). The Window has not been laid out yet.");
        }
        return statusBarHeight;
    }
 }
