 package se.lth.student.eda040.a1;
 
 import se.lth.student.eda040.a1.network.Image;
 import android.content.Context;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.graphics.Canvas;
 import android.view.View.MeasureSpec;
 import android.view.Gravity;
 import android.view.View;
 
 
 public class AwesomeFrameLayout extends FrameLayout {
 	private ImageView view;
 	private TextView overlay;
 	private Bitmap disconnectedImage;
 	private boolean disconnected;
 
 	public AwesomeFrameLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		Log.d("AwesomeFrameLayout", " In constructor");
 	}
 
 	public void onFinishInflate() {
 		Log.d("AwesomeFrameLayout", " In onFinishInflate");
 		this.view = (ImageView) findViewById(R.id.view);
 		this.overlay = (TextView) findViewById(R.id.overlay);
 		this.disconnectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.disconnected);
 		//view.setImageBitmap(BitmapFactory.decodeFile("/sdcard/test2.jpg"));
 		view.setImageBitmap(disconnectedImage);
 		overlay.setVisibility(View.INVISIBLE);
 	}
 
 	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		Log.d("AwesomeFrameLayout", " In onMeasure");
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		//int measureSpecHeight = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) / 2, MeasureSpec.getMode(heightMeasureSpec));
 		int measureSpecHeight = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.getMode(heightMeasureSpec));
 
 		view.measure(widthMeasureSpec, measureSpecHeight);
 
 		overlay.measure(0, 0);
 		overlay.measure(overlay.getMeasuredWidth(), getMeasuredHeight());
 		//overlay.setText(String.valueOf(Math.random()));
 
 		//setMeasuredDimensionwidthMeasureSpec, measureSpecHeight); //dont call, brejks overlay
 	}
 	
 	
 	public void drawImage(Image image) {
 		//Log.d("VideoView", "Recieved image to draw from camera " + image.getCameraId());
 		if (!disconnected) {
 			view.setImageBitmap(image.toBitmap());
 			overlay.setVisibility(View.VISIBLE);
			overlay.setText("Δ " + image.getDelay() + " ms");
 		}
 	}
 
 	public void connect() {
 		disconnected = false;
 	}
 
 	public void disconnect() {
 		disconnected = true;
 		view.setImageBitmap(disconnectedImage);
 		overlay.setVisibility(View.INVISIBLE);
 	}
 }
