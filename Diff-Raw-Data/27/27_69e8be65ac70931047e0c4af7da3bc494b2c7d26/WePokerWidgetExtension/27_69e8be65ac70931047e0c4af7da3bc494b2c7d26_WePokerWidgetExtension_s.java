 package edu.vub.at.nfcpoker.smartwatch;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Handler;
 import android.text.TextPaint;
 import android.util.DisplayMetrics;
 import android.util.Log;
 
 import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
 import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;
 
 public class WePokerWidgetExtension extends WidgetExtension {
 
 	public static final String UPDATE_ACTION = "edu.vub.at.nfcpoker.smartwatch.UPDATE";
 
 	private static final int WIDTH = 128;
 	private static final int HEIGHT = 110;
 
 	private Runnable mInfoTimeout;
 	private Handler mHandler = new Handler();
 	private BroadcastReceiver mReceiver;
 	private IntentFilter mFilter;
 
 	private static double mProbability = -1;
 
 	private final BitmapFactory.Options mBitmapOptions;
 
 	public WePokerWidgetExtension(Context context, String hostAppPackageName) {
 		super(context, hostAppPackageName);
 		
         mBitmapOptions = new BitmapFactory.Options();
         mBitmapOptions.inDensity = DisplayMetrics.DENSITY_DEFAULT;
         mBitmapOptions.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
         
 		mFilter = new IntentFilter();
 		mFilter.addAction(UPDATE_ACTION);
 		
 		mReceiver = new BroadcastReceiver() {
 			
 			@Override
 			public void onReceive(Context context, Intent intent) {
				Log.d("wePoker service", "received intent" + intent);
 				mProbability = intent.getDoubleExtra("probability", -1);
				Log.d("wePoker service", "new probability: " + mProbability);
 				mHandler.post(new Runnable() {
 					
 					@Override
 					public void run() {
 						updateWidget(true);
 					}
 				});
 			}
 		};
 	}
 
 	@Override
 	public void onStartRefresh() {
         mInfoTimeout = new Runnable() {
             public void run() {
                 updateWidget(true);
                 mInfoTimeout = null;
             }
         };
         mHandler.postDelayed(mInfoTimeout, 2000);
         mContext.registerReceiver(mReceiver, mFilter);
 	}
 
 	@Override
 	public void onStopRefresh() {
 		mContext.unregisterReceiver(mReceiver);
         if (mInfoTimeout != null) {
             mHandler.removeCallbacks(mInfoTimeout);
             mInfoTimeout = null;
         }
 	}	 
 
 	protected void updateWidget(boolean b) {
         Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
 
         // Set the density to default to avoid scaling.
         bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
         Canvas canvas = new Canvas(bitmap);
         
         canvas.drawColor(Color.WHITE);
 
         Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.background, mBitmapOptions);
         canvas.drawBitmap(icon, (WIDTH - 90) / 2 + 2, (HEIGHT - 100) / 2, new Paint());
         
 
         if (mProbability >= 0) {
         	final int textSize = 30;
 
         	// Create default text paint
         	TextPaint textPaint = new TextPaint();
         	textPaint.setAntiAlias(true);
         	textPaint.setTextAlign(Paint.Align.CENTER);
         	textPaint.setTextSize(textSize);
         	textPaint.setColor(Color.BLACK);
         	
         	Paint whitePaint = new Paint();
         	whitePaint.setColor(Color.WHITE);
         	whitePaint.setAlpha(192);
 			canvas.drawRect(0, (HEIGHT - textSize) / 2, WIDTH, (HEIGHT + textSize) / 2, whitePaint); 
 
 	        ExtensionUtils.drawText(canvas, String.format("%02d%%", Math.round(100 * mProbability)), WIDTH / 2, (HEIGHT + textSize) / 2 - 4, textPaint, WIDTH);
         }
         
         showBitmap(bitmap);
 	}
 }
