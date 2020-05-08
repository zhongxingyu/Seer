 package edu.uncc.cci.KnightVisor;
 
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.Size;
 import android.util.Log;
 import android.view.View;
 import edu.uncc.cci.KnightVisor.Toolbox.BinaryOperation;
 
 public class EdgeView extends View implements PreviewCallback
 {
     public static final String TAG = EdgeView.class.getSimpleName();
     
     private final Lock  cameraPreviewLock   = new ReentrantLock();
     private final Paint edgePaint           = new Paint();
     
     private Bitmap      bitmap              = null;
     private Rect        canvasRect          = null;
     private Rect        cameraRect          = null;
         
     private IntBuffer  intBuffer            = null;
 	private byte[]     cameraPreview        = null;
 	private int        width                = 0;
 	private int        height               = 0;
 	
 	public static final byte[][] sobelNorm = { { -1, -2, -1}, { 0, 0, 0}, { 1, 2, 1} };
 	public static final byte[][] sobelTran = { { -1,  0,  1}, {-2, 0, 2}, {-1, 0, 1} };
 	
 	static
     {
         System.loadLibrary("native");
     }
     
     public native void nativeProcessing(byte[] f, int width, int height, IntBuffer output);
     public native void setThresholdManually(int threshold);
     public native void setColorSelected(int color);
     public native void setMedianFiltering(boolean on);
     
 	public EdgeView(Context context)
 	{
 		super(context);
 		edgePaint.setColor(Color.GREEN);
 	}
 	
 	private void tron(Canvas canvas)
 	{
 		int[][] f = new int[height][width]; // input image (2D)
 		int[] g = new int[height * width]; // output image (1D)
 		int r, c, r_width;
 
 	    // convert 1D cameraPreview to 2D f
 	    for(r = 0; r < height; r++) {
             r_width = r * width;
             for(c = 0; c < width; c++)
                 f[r][c] = cameraPreview[r_width + c];
         }
 	    
 	    // output g by tossing f into a bitmap, and tossing that onto the canvas 
 	    canvas.drawBitmap(Bitmap.createBitmap(g, width, height, Bitmap.Config.ARGB_8888), 0, 0, null);
 	}
 	
     private void sobelDetection(Canvas canvas)
 	{
 	    int[][] f = new int[height][width];    //input image
 	    int[]   g = new int[width * height];   //output image
 	    int r,c,r_width;
 	    
 	    /* to convert from 1-D to 2-D matrix */
 	    for(r=0; r < height; r++){
             r_width = r * width;
             for(c=0; c < width; c++)
                 f[r][c] = cameraPreview[r_width + c];
         }
 	    
 	    /* smooth that mother fucker */
 	    int[][] smooth = Toolbox.smooth(f);
 	    
 	    /* do gx, gy, and gm */
 	    int[][] gx = Toolbox.imfilter(smooth, sobelNorm);
 	    int[][] gy = Toolbox.imfilter(smooth, sobelTran);
 	    
 	    /* this type of thing is kinda unecessary, but I got carried away */
 	    int[][] gm = Toolbox.transform(gx, gy, new BinaryOperation() {
                     public int it(int a, int b) {
                         return (int) Math.sqrt(a*a + b*b);
                     }
         });
 	    
 	    /* convert from 2-D intensity matrix to a 1-D grayscale array that we'll output */
 	    int color;
 	    for(r=0; r < height; r++)
         {
 	        r_width = r * width;
             for(c=0; c < width; c++)
             {
                 color = gm[r][c];
                 if (color > 255)
                 {
                     //if you log all these values, you'll see they're commonly
                     //between 255 and 700. not sure if I'm doing anything wrong
                     //to cause that.....but anyway. this works for meow.
                     //Log.d(TAG, String.valueOf(color));
                     color = 255;
                 }
                 color = color > 48 ? 255 : 0;
                 
                 g[r_width+c] = (color << 0) +
                         (color << 8) +
                 (color << 16) +
                 (color << 24);
             }
         }
         /*
 	    Rect src = new Rect(0,0,width, height);
 	    Rect dst = new Rect(0,0,width, height);
 	    Bitmap bitmap = Bitmap.createBitmap(g, width, height, Bitmap.Config.ARGB_8888);
 	    Paint paint = new Paint();
 	    paint.setColor(Color.WHITE);
 	    paint.setAlpha(Color.TRANSPARENT);
 	    canvas.drawBitmap(bitmap, src, dst, paint);
 	    */
 	    /* toss array into a bitmap, and toss that sucker onto the canvas */
 	    canvas.drawBitmap(Bitmap.createBitmap(g, width, height, Bitmap.Config.ARGB_8888), 0, 0, null);
 	}
 	
 	private void edgeDetection(Canvas canvas)
 	{
 	    /* a rather poor implementation of edge detection.
 	     * code is a translation of William Beene's C code. */
 	    int px, cx, nx, ly, val, y, x, y_width;
 	    int threshold = 30;
 	    
 	    Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
         
 	    for (y = 1; y < height-1; y++) {
 
 	        y_width = y*width;
 
 	        px = cameraPreview[y_width]/2; // init previous x
 	        cx = cameraPreview[y_width+1]/2; // init current x
 
 	        for (x = 1; x < width-1; x++) {
 	            nx = cameraPreview[y_width+x+1]/2; // next x
 
 	            ly = (cameraPreview[(y-1)*width+x]/2) - (cameraPreview[(y+1)*width+x]/2);
 	            val = Math.abs(px - nx) + Math.abs(ly);
 
 	            if(val > threshold) {
 	                bitmap.setPixel(x, y, Color.GREEN);
 	                //canvas.drawPoint((float)x, (float)y, edgePaint);
 	            }
 	            //else bitmap.setPixel(x, y, Color.TRANSPARENT);
 
 	            // previous x becomes current x and current x becomes next x
 	            px = cx;
 	            cx = nx;
 	        }
 	    }
 	    canvas.drawBitmap(bitmap, 0, 0, null);
 	    
 	}
 
 	
 	private void drawEverything(Canvas canvas)
 	{
 	    //cameraPreview = convertToGrayscale(cameraPreview);
 	    //Bitmap bitmap = Bitmap.createBitmap(cameraPreview, width, height, Bitmap.Config.ARGB_8888);
 	    //canvas.drawBitmap(bitmap, 0, 0, null);
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 
 		if (cameraPreview != null && cameraPreviewLock.tryLock())
 			try {
 			    if (canvasRect == null)
			        canvasRect = new Rect(0,0,canvas.getWidth(), canvas.getHeight());
			    
 			    
 	            Log.d(TAG, "Before native processing");
 	            
 	            /* do some processing in seaworld */
                 nativeProcessing(cameraPreview, width, height, intBuffer);
                 
                 Log.d(TAG, "After native processing");
 			    
                 
 			    /* copy the pixels from intBuffer to bitmap */
 			    bitmap.copyPixelsFromBuffer(intBuffer);
 			    
 			    
 			    /* draw the canvas onto the screen */
 			    canvas.drawBitmap(bitmap, cameraRect, canvasRect, null);
 			    
 			} finally {
 				cameraPreviewLock.unlock();
 			}
 	}
 	
 	public void onPreviewFrame(byte[] yuv, Camera camera) 
 	{
 		if (cameraPreviewLock.tryLock() && yuv != null)
 			try {
 
 				Size size = camera.getParameters().getPreviewSize();
 				width  = size.width;
 				height = size.height;
 				final int length = width * height;
 				
 				if (cameraPreview == null || cameraPreview.length != length)
 				{
 	                cameraRect = new Rect(0,0,width, height);
 				    cameraPreview = new byte[length];
 				    intBuffer = ByteBuffer.allocateDirect(width * height * 4).asIntBuffer();
 				    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
 				}
 				
 				System.arraycopy(yuv, 0, cameraPreview, 0, length);
 	
 				
 			} finally {
 				cameraPreviewLock.unlock();
 				postInvalidate();
 			}
 	}
 
 }
