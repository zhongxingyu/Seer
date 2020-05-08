 package com.markchung.HouseAssist;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ImageView.ScaleType;
 import android.widget.TextView;
 
 public class CompassActivity extends Activity implements SensorEventListener {
 	private SensorManager sm;
 	private ImageView imageView;
 	private Matrix matrix;
 	private Bitmap m_bmp;
 	private AdView adView;
 
 	private void CreateBitmap() {
 		DisplayMetrics dm = new DisplayMetrics();
 		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
 		int width = dm.widthPixels;
		int height = dm.heightPixels - 80-50;
 		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
 				R.drawable.compass);
 		Matrix matrix = new Matrix();
 		float scale = Math.min(((float) width) / bmp.getWidth(),
 				((float) height) / bmp.getHeight());
 		matrix.setScale(scale, scale);
 		m_bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
 				matrix, true);
 
 	}
 	private TextView m_info_view;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_compass);
 		CreateBitmap();
 		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
 		imageView = (ImageView) findViewById(R.id.image_compass);
 		matrix = new Matrix();
 		matrix.setScale(1, 1);
 		imageView.setScaleType(ScaleType.MATRIX);
 		imageView.setImageMatrix(matrix);
 		imageView.setImageBitmap(m_bmp);
 		m_info_view = (TextView) findViewById(R.id.info_view);
 		
 		adView = new AdView(this, AdSize.BANNER, MainActivity.myAdID);
 		  LinearLayout layout = (LinearLayout)findViewById(R.id.adview);
 		  layout.addView(adView);
 		  AdRequest adRequest = new AdRequest();
 		  adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
 		  adRequest.addTestDevice("BA76119486D364D047D0C789B4F61E46");
 		  adView.loadAd(adRequest);		
 	}
 
 	@Override
 	protected void onPause() {
 		sm.unregisterListener(this);
 		super.onPause();
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
 		sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.getType() != Sensor.TYPE_ORIENTATION)
 			return;
 		float value = 360-event.values[SensorManager.DATA_X];
 		if(value<180){
 			m_info_view.setText(Integer.toString((int)value));
 		}else{
 			m_info_view.setText(Integer.toString((int)(value-360)));
 		}
 		
 		matrix.setRotate(value,
 				m_bmp.getWidth() / 2, m_bmp.getHeight() / 2);
 		imageView.setImageMatrix(matrix);
 
 	}
 
 }
