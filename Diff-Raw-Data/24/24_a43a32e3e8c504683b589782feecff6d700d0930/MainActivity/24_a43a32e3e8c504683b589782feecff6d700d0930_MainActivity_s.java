 package net.kevxu.sense;
 
 import java.util.Formatter;
 
 import net.kevxu.senselib.Sense;
 import net.kevxu.senselib.SensorNotAvailableException;
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	private TextView mTextView;
 
 	private Sense mSense;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		mTextView = (TextView) findViewById(R.id.activity_main_textview);
 
 		try {
 			mSense = new Sense(this);
 		} catch (SensorNotAvailableException e) {
 			String sensor;
 			switch (e.getSensorType()) {
 			case Sensor.TYPE_LINEAR_ACCELERATION:
 				sensor = getString(R.string.sensor_linear_acceleration);
 				break;
 			case Sensor.TYPE_GRAVITY:
 				sensor = getString(R.string.sensor_gravity);
 				break;
 			default:
 				sensor = getString(R.string.sensor_not_available);
 				break;
 			}
 			Formatter formatter = new Formatter();
 			formatter.format(getString(R.string.sensor_not_available), sensor);
 			mTextView.setText(formatter.toString());
 			formatter.close();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		mSense.close();
 	}
 
 	@Override
 	protected void onResume() {
 		mSense.reload();
 	}
 
 }
