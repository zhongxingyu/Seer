 package il.ac.shenkar.qrmap;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.CountDownTimer;
 import android.util.DisplayMetrics;
 import android.view.View;
 
 public class MyView extends View {
 	private Bitmap pinpoint;										//point on map
 	private Bitmap pinpoint_red;										//point on map
 	private ArrayList<Pinpoint> points = new ArrayList<Pinpoint>();	//list of points
 	private Drawable mCustomImage;									//image
 	public float maxW;
 	public float maxH;
 	public Integer changeColor = -1;
 	public Integer orientation;
 	
 	
 	//Draw pinpoint and map
 	public MyView(Context context) {
 		super(context);
 		pinpoint = BitmapFactory.decodeResource(getResources(),
 				R.drawable.pinpoint);
 		pinpoint_red = BitmapFactory.decodeResource(getResources(),
 				R.drawable.pinpointred);
 	
 		mCustomImage = context.getResources().getDrawable(R.drawable.shenkar);
 		
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		// TODO Auto-generated method stub
 		super.onSizeChanged(w, h, oldw, oldh);
 		
 		this.maxW=w;		
 		this.maxH=h;
 				
 	}
 
 	@SuppressLint("DrawAllocation")
 	@Override
 	protected void onDraw(Canvas canvas) {
 		// TODO Auto-generated method stub
 		//super.drawableStateChanged();
 		super.onDraw(canvas);
 		
 		mCustomImage.setBounds(canvas.getClipBounds());
 		mCustomImage.draw(canvas);		
 		Iterator<Pinpoint> it = points.iterator();
 		int i=0;
 		while (it.hasNext()) {
 			Pinpoint p = it.next();
 			//View.onSizeChanged()
 			orientation =getResources().getConfiguration().orientation;
 			p=viewTrans(p);			
 			if(i == changeColor)		
 				  canvas.drawBitmap(pinpoint_red, p.getxPos() - pinpoint_red.getWidth()/ 2, p.getyPos() - pinpoint_red.getHeight() / 2, null);
 			else	
 			  canvas.drawBitmap(pinpoint, p.getxPos() - pinpoint.getWidth()/ 2, p.getyPos() - pinpoint.getHeight() / 2, null);
 			i++;
 			
 		}
 	//	invalidate();
 		
 	}
 	public void inValid(){
		invalidate();				
 	}
 	//Add new point
 	public void addPinpoint(Pinpoint pinpoint) {
 
 		points.add(new Pinpoint(pinpoint.getxPos(), pinpoint.getyPos(),
 				pinpoint.getText(),pinpoint.getMaxW(),pinpoint.getMaxH(),pinpoint.getOrientation(),pinpoint.getIdQR(),pinpoint.getParseId()));
 	}
 	//Delete point from canvas
 
 	public void delPinpoint(int index) {
 		System.out.println(points.size());
 		points.remove(index);
 	}
 	public void delLastPinpoint() {
 		points.remove(points.size()-1);
 	}
 	public void changeColor(int index){	
 		System.out.println(points.size());
 		changeColor=index;
 		MyCount counter = new MyCount(5000, 1000); // set your seconds
 		   counter.start();
 	}
 	/**
 	 * This method convents dp unit to equivalent device specific value in
 	 * pixels.
 	 * 
 	 * @param dp
 	 *            A value in dp(Device independent pixels) unit. Which we need
 	 *            to convert into pixels
 	 * @param context
 	 *            Context to get resources and device specific display metrics
 	 * @return A float value to represent Pixels equivalent to dp according to
 	 *         device
 	 */
 	public static float convertDpToPixel(float dp, Context context) {
 		Resources resources = context.getResources();
 		DisplayMetrics metrics = resources.getDisplayMetrics();
 		float px = dp * (metrics.densityDpi / 160f);
 		return px;
 	}
 
 	/**
 	 * This method converts device specific pixels to device independent pixels.
 	 * 
 	 * @param px
 	 *            A value in px (pixels) unit. Which we need to convert into db
 	 * @param context
 	 *            Context to get resources and device specific display metrics
 	 * @return A float value to represent db equivalent to px value
 	 */
 	public static float convertPixelsToDp(float px, Context context) {
 		Resources resources = context.getResources();
 		DisplayMetrics metrics = resources.getDisplayMetrics();
 		float dp = px / (metrics.densityDpi / 160f);
 		return dp;
 
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasWindowFocus) {
 		// TODO Auto-generated method stub
 		super.onWindowFocusChanged(hasWindowFocus);
 //		 int intrinsicHeight = mCustomImage.getIntrinsicHeight();
 //		    int intrinsicWidth = mCustomImage.getIntrinsicWidth();
 		   
 	}
 	
 	public Pinpoint viewTrans(Pinpoint pin){
 		Pinpoint p = new Pinpoint(this.maxW*pin.getxPos()/pin.getMaxW(),this.maxH*pin.getyPos()/pin.getMaxH(),pin.getText(),pin.getMaxW(),pin.getMaxH(),pin.getOrientation(),pin.getIdQR(),pin.getParseId());
 		return p;
 	}
 	class MyCount extends CountDownTimer {  
 	    public MyCount(long millisInFuture, long countDownInterval) {  
 	        super(millisInFuture, countDownInterval);  
 	    }
 
 		@Override
 		public void onFinish() {
 			// TODO Auto-generated method stub
 			changeColor=-1;			
			invalidate();
 		}
 
 		@Override
 		public void onTick(long arg0) {
 			// TODO Auto-generated method stub
 			
 		}//MyCount  
 
 	}
 }	
 	
