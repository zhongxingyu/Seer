 package com.gingbear.githubtest;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.widget.TextView;
 
 public class CustomSensorEvent implements SensorEventListener {
 		private SensorManager manager;
 		private TextView textView;
 
 		public void setTetView(TextView textView){
 			this.textView = textView;
			textView.setText("start");
 		}
 	public void onAccuracyChanged(Sensor sensor, int i) {
 		// TODO 自動生成されたメソッド・スタブ
 		
 	}
 	CustomSensorEvent(Activity activity){
 		manager = (SensorManager)activity.getSystemService(Activity.SENSOR_SERVICE);
 	}
 		public void onStop(){
 			// Listenerの登録解除
 			manager.unregisterListener(this);
 		}
  public void onResume(){
 	 // Listenerの登録
 		List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
 		if(sensors.size() > 0) {
 		Sensor s = sensors.get(0);
 		manager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
 		}
  }
 	public void onSensorChanged(SensorEvent event) {
 		// TODO 自動生成されたメソッド・スタブ
 		String str = "";
 		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 			str = "X軸:" + event.values[0]
 					+ "\nY軸:" + event.values[1] 
 							+ "\nZ軸:" + event.values[2];
 			textView.setText(str);
 		}// else if (event.sensor.getType() == Sensor.TY)
 	}
 
 }
