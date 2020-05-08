 package uk.me.tom_fitzhenry.motionremote.mjpeg;
 
 import java.util.concurrent.TimeUnit;
 
import com.google.common.base.Stopwatch;

 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 
 public class FpsOverlay {
 	
 	private final MjpegView mjpegView;
 	private final Stopwatch stopwatch = new Stopwatch();
 	
 	private int frameCounter = 0;
 	private Bitmap fpsOverlay;
 	
 	public FpsOverlay(MjpegView mjpegView) {
 		this.mjpegView = mjpegView;
 	}
 	
 	public void start() {
 		stopwatch.start();
 	}
 	
 	public void renderFps(PorterDuffXfermode mode, Rect destRect, Canvas c, Paint p) {
 		p.setXfermode(mode);
 		if(fpsOverlay != null) {
 		    int height = ((mjpegView.ovlPos & 1) == 1) ? destRect.top : destRect.bottom-fpsOverlay.getHeight();
 		    int width  = ((mjpegView.ovlPos & 8) == 8) ? destRect.left : destRect.right -fpsOverlay.getWidth();
 		    c.drawBitmap(fpsOverlay, width, height, null);
 		}
 		p.setXfermode(null);
 		
 		frameCounter++;
 		
 		if (stopwatch.elapsed(TimeUnit.SECONDS) > 1) {
 		    frameCounter = 0; 
		    stopwatch.reset();
		    makeFpsOverlay(mjpegView.overlayPaint, String.valueOf(frameCounter) + " fps");
 		}
 	}
 	
     private Bitmap makeFpsOverlay(Paint p, String text) {
         Rect b = new Rect();
         p.getTextBounds(text, 0, text.length(), b);
         int bwidth  = b.width()+2;
         int bheight = b.height()+2;
         Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
         Canvas c = new Canvas(bm);
         p.setColor(mjpegView.overlayBackgroundColor);
         c.drawRect(0, 0, bwidth, bheight, p);
         p.setColor(mjpegView.overlayTextColor);
         c.drawText(text, -b.left+1, (bheight/2)-((p.ascent()+p.descent())/2)+1, p);
         return bm;           
     }
 }
