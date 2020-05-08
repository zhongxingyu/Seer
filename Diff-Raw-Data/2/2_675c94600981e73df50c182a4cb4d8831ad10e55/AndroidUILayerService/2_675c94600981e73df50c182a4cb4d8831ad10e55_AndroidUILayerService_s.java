 package net.avh4.framework.uilayer.android;
 
 import android.content.Context;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import net.avh4.framework.uilayer.ClickReceiver;
 import net.avh4.framework.uilayer.Font;
 import net.avh4.framework.uilayer.KeyReceiver;
 import net.avh4.framework.uilayer.ResponseListener;
 import net.avh4.framework.uilayer.SceneCreator;
 import net.avh4.framework.uilayer.UILayerService;
 import org.apache.commons.lang.NotImplementedException;
 
 import java.util.List;
 
 public class AndroidUILayerService implements UILayerService {
 
     Context context;
 
     public void run(SceneCreator game, ClickReceiver receiver, KeyReceiver keyReceiver) {
         throw new RuntimeException(
                 "Android applications do not implement a main entry point.  " +
                         "Subclass AndroidSceneRendererActivity instead.");
     }
 
     public void init(Context context) {
         this.context = context;
     }
 
     public int getImageWidth(String image) {
         return 0;
     }
 
     public int getImageHeight(String image) {
         return 0;
     }
 
     @Override
     public int getPixel(String image, int x, int y) {
         throw new NotImplementedException(); // TODO
     }
 
     public int getFontHeight(Font font) {
         final Paint paint = new Paint();
         paint.setTypeface(Typeface.createFromAsset(context.getAssets(), font.getResourceName()));
         paint.setTextSize(font.getSize());
         final Paint.FontMetrics metrics = paint.getFontMetrics();
         return (int) Math.ceil(-metrics.ascent + metrics.descent + metrics.leading);
     }
 
     public int measureText(Font font, String text) {
         final Paint paint = new Paint();
         paint.setTypeface(Typeface.createFromAsset(context.getAssets(), font.getResourceName()));
         paint.setTextSize(font.getSize());
         return (int) Math.ceil(paint.measureText(text));
     }
 
     @Override
    public void showChoices(String title, List<String> choices, ResponseListener listener) {
         throw new NotImplementedException(); // TODO
     }
 }
