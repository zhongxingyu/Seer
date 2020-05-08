 package assignment2;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import uvamult.assignment2.R;
 import android.hardware.Camera.Size;
 import assignment2.android.CameraView;
 import android.view.View;
 import android.widget.Button;
 import android.widget.SeekBar;
 
 public class DrawCamera implements SeekBar.OnSeekBarChangeListener{
 	public Size imageSize;
 	
 	private int[] rgb;			// the array of integers
 	private int[] rgbout;
 	private double angle = Math.toRadians(90);
 	private double sin = 1;
 	private double cos = 0;
 	
 	
 	private enum Interpolation{
 		BILINEAIR, NN;
 	}
 	
 	private Interpolation interpolation = Interpolation.NN;
 	private Paint p;
 	
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
 		angle = Math.toRadians(progress + 90);
 		sin = Math.sin(angle);
 		cos = Math.cos(angle);
 	}
 	
 	public void onStartTrackingTouch(SeekBar seekBar){
 	}
 	
 	public void onStopTrackingTouch(SeekBar seekBar){
 	}
 	
 	public void imageReceived(byte[] data) {
 		// Allocate the image as an array of integers if needed.
 		// Then, decode the raw image data in YUV420SP format into a red-green-blue array (rgb array)
 		int arraySize = imageSize.width*imageSize.height;
 		if(rgb == null){
 			rgb = new int[arraySize];
 			rgbout = new int[arraySize];
 		}
 		decodeYUV420SP(rgb, data);
 
 		double rx, ry, dx, dy;
 		// Center
 		double cx = (double) imageSize.width / 2.0d;
 		double cy = (double) imageSize.height / 2.0d;
 		
 		for(int y = 0; y < imageSize.height; y++){
 			dy = y - cy;
 			for(int x = 0; x < imageSize.width; x++){
 				dx = x - cx;
 				
 				rx = (dx * cos) + (dy * sin) + cx;
 				ry = (-1 * dx * sin) + (dy * cos) + cy;
 				
 				if(interpolation == Interpolation.NN){
 					rgbout[xyToIdx(x, y)] = interpolateNN(rx, ry, rgb);
 				}else{
 					rgbout[xyToIdx(x, y)] = interpolateBilineair(rx, ry, rgb);
 				}
 			}
 		}
 	}
 	
 	private int interpolateNN(double x, double y, int[] rgb){
 		x = Math.round(x);
 		y = Math.round(y);
 		int idx = xyToIdx((int) x, (int) y);
 		if(x < 0 || x > imageSize.width || y < 0 || y > imageSize.height || idx >= rgb.length || idx < 0){
 			return 0;
 		}
 		return rgb[idx];
 	}
 	/*
 	 * 
 	 * *--x-*
 	 * .    .
 	 * .    .
 	 * .    .
 	 * *--x-*
 	 * 
 	 */
 	private void interpolateLineairHorz(double x, int y, int[] rgb, double[] irgb){	
 		if(x >= imageSize.width || x < 0 || y >= imageSize.height || y < 0){
 			return;
 		}
 		
 		int low = (int) Math.floor(x);
 		
 		int rgbHigh = rgb[xyToIdx((int) Math.ceil(x), y)];
 		int rgbLow = rgb[xyToIdx(low, y)];
 		
 		double dist = x - low;
 		
 		irgb[0] = Math.abs(r(rgbHigh) - r(rgbLow));
 		irgb[1] = Math.abs(g(rgbHigh) - g(rgbLow));
 		irgb[2] = Math.abs(b(rgbHigh) - b(rgbLow));
 		
		if(r(rgbHigh) < r(rgbLow))irgb[0] *= (1d - dist);
		else irgb[0] *= dist;
		
		if(g(rgbHigh) < g(rgbLow))irgb[1] *= (1d - dist);
		else irgb[1] *= dist;
		
		if(b(rgbHigh) < b(rgbLow))irgb[2] *= (1d - dist);
		else irgb[2] *= dist;
 		
 		irgb[0] += Math.min(r(rgbHigh), r(rgbLow));
 		irgb[1] += Math.min(g(rgbHigh), g(rgbLow));
 		irgb[2] += Math.min(b(rgbHigh), b(rgbLow));
 	}
 	
 	/*
 	 * 
 	 * *--x-*
 	 * .  | .
 	 * .  x .
 	 * .  | .
 	 * *--x-*
 	 * 
 	 */
 	private int interpolateBilineair(double x, double y, int[] rgb){
 		if(x >= imageSize.width || x < 0 || y >= imageSize.height || y < 0){
 			return 0;
 		}
 		
 		double rgbHigh[] = new double[3];
 		double rgbLow[] = new double[3];
 		double irgb[] = new double[3];
 		
 		int low = (int) Math.floor(y);
 		
 		interpolateLineairHorz(x, (int) Math.ceil(y), rgb, rgbHigh);
 		interpolateLineairHorz(x, low, rgb, rgbLow);
 		
 		double dist = y - low;
 		irgb[0] = Math.abs(rgbHigh[0] - rgbLow[0]);
 		irgb[1] = Math.abs(rgbHigh[1] - rgbLow[1]);
 		irgb[2] = Math.abs(rgbHigh[2] - rgbLow[2]);
 		
		if(rgbHigh[0] < rgbLow[0])irgb[0] *= (1d - dist);
		else irgb[0] *= dist;
		
		if(rgbHigh[1] < rgbLow[1])irgb[1] *= (1d - dist);
		else irgb[1] *= dist;
		
		if(rgbHigh[2] < rgbLow[2])irgb[2] *= (1d - dist);
		else irgb[2] *= dist;
 		
 		irgb[0] += Math.min(rgbHigh[0], rgbLow[0]);
 		irgb[1] += Math.min(rgbHigh[1], rgbLow[1]);
 		irgb[2] += Math.min(rgbHigh[2], rgbLow[2]);
 		
 		return combine((int) Math.round(irgb[0]), (int) Math.round(irgb[1]), (int) Math.round(irgb[2]));
 	}
 	
 	private int xyToIdx(int x, int y){
 		return y * imageSize.width + x;
 	}
 	
 	public void draw(Canvas c) {	
 		 int centrex = c.getWidth() - imageSize.width;
 		 int centrey = c.getHeight() - imageSize.height;
 		 centrey = centrey / 2;
 		 centrex = centrex / 2;
 		 
 		 c.drawColor(Color.BLACK);
 		 c.save();
 		 c.translate(centrex, centrey);
 		 drawImage(c);
 		 
 		 c.restore();
 		 c.drawText("Interpolation:", 15f, 15f, p);
 		 if(interpolation == Interpolation.NN){
 			 c.drawText("Nearest neighbour", 15f, 30f, p);
 		 }else{
 			 c.drawText("Bilinear", 15f, 30f, p);
 		 }
 	}
 	
 	/*
 	 * Setup your environment
 	 */
 	public void setup(CameraView view) {		
 		SeekBar seekBar = (SeekBar) view.activity.findViewById(R.id.seekBar1);
 		seekBar.setMax(360);
 		seekBar.setOnSeekBarChangeListener(this);
         Button BI = (Button) view.activity.findViewById(R.id.buttonBI);
         Button NN = (Button) view.activity.findViewById(R.id.buttonNN);
         BI.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View arg) {
         		interpolation = Interpolation.BILINEAIR;
 			}
         });
         NN.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View arg) {
         		interpolation = Interpolation.NN;
 			}
         });
 	}
 	
 	/*
 	 * Below are some convenience methods,
 	 * like grabbing colors and decoding.
 	 */
     
 	// Extract the red element from the given color
     private int r(int rgb) {
     	return (rgb & 0xff0000) >> 16;
     }
 
 	// Extract the green element from the given color
     private int g(int rgb) {
     	return (rgb & 0x00ff00) >> 8;
     }
 
 	// Extract the blue element from the given color
     private int b(int rgb) {
     	return (rgb & 0x0000ff);
     }
     
     // Combine red, green and blue into a single color int
     private int combine(int r, int g, int b) {
     	 return 0xff000000 | (r << 16) | (g << 8) | b;
     }
     
     private void drawImage(Canvas c) {
 		c.drawBitmap(rgbout, 0, imageSize.width, 0f, 0f, imageSize.width, imageSize.height, true, null);
     }
     
     /*
      * Decode the incoming data (YUV format) to a red-green-blue format
      */
 	private void decodeYUV420SP(int[] rgb, byte[] yuv420sp) {
 		final int width = imageSize.width;
 		final int height = imageSize.height;
     	final int frameSize = width * height;
     	
     	for (int j = 0, yp = 0; j < height; j++) {
     		int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
     		for (int i = 0; i < width; i++, yp++) {
     			int y = (0xff & ((int) yuv420sp[yp])) - 16;
     			if (y < 0) y = 0;
     			if ((i & 1) == 0) {
     				v = (0xff & yuv420sp[uvp++]) - 128;
     				u = (0xff & yuv420sp[uvp++]) - 128;
     			}
     			
     			int y1192 = 1192 * y;
     			int r = (y1192 + 1634 * v);
     			int g = (y1192 - 833 * v - 400 * u);
     			int b = (y1192 + 2066 * u);
     			
     			if (r < 0) r = 0; else if (r > 262143) r = 262143;
     			if (g < 0) g = 0; else if (g > 262143) g = 262143;
     			if (b < 0) b = 0; else if (b > 262143) b = 262143;
     			
     			rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
     		}
     	}
     }
 
 	public DrawCamera(){
 		p = new Paint();
 		p.setColor(Color.RED);
 	}
 }
