 package com.tware.anexter;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 public class lightSensor extends Activity implements SensorEventListener{
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
 	private Button bBlue;
 	private String LOG;
 
 	final private float CONSTLx = 800; /* define standard lux for testing */
 	private SensorManager sm;
 	private Sensor sensors;
 	private TextView lightV;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.lightsensor);
         this.setTitle("aNexter - 光感测试");
 
     	lightV = (TextView)findViewById(R.id.lightView);
 
     	bPass = (Button)findViewById(R.id.btn_pass);     
         bPass.setEnabled(false);
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "PASS|");
         		i.setClass(lightSensor.this , camera.class);
         		startActivity(i);
         		lightSensor.this.finish();
         	}
         });
         
         bFail = (Button)findViewById(R.id.btn_fail);
         bFail.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "FAIL|");
         		i.setClass(lightSensor.this , getResults.class);
         		startActivity(i);
         		lightSensor.this.finish();
         	}
         });
         
         bNa = (Button)findViewById(R.id.btn_na);
         bNa.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "N/A|");
         		i.setClass(lightSensor.this , camera.class);
         		startActivity(i);
         		lightSensor.this.finish();
         	}
         });
         
         LOG = this.getIntent().getStringExtra("LOG");
         
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensors = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
 		if (sensors == null)
 		{
 			lightV.setText("No light Sensor");
 		}
         
     }
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		sm.unregisterListener(this);
 	}
     
 	@Override
 	protected void onResume() { 
 		super.onResume();
 		sm.registerListener(this, sensors, SensorManager.SENSOR_DELAY_FASTEST);
 	}
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		bBlue.setEnabled(true);
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// Do nothing
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		lightV.setText("流明值: " + event.values[0] + " lx(Pass >800 lx)");
 		if ( Float.compare(event.values[0], CONSTLx) >= 0 )
 		{
 			sm.unregisterListener(this);  /* stop Sensor changed */
     		Intent i = new Intent();
     		i.putExtra("LOG", LOG + "PASS|");
     		i.setClass(lightSensor.this , camera.class);
     		startActivity(i);
     		lightSensor.this.finish();
 		}
 	}
 	
 }
