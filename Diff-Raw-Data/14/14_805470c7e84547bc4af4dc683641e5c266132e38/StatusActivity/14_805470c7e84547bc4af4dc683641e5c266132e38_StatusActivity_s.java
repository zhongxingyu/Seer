 package c3t.de;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class StatusActivity extends Activity {
 
 	boolean isOn = false;
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.status);     
     }
     
     void setStatusOn() {
    	 findViewById( R.id.LinearLayout01).setBackgroundResource(R.drawable.led_an);
     	 isOn = true;
     }
     
     void setStatusOff() {
    	findViewById( R.id.LinearLayout01).setBackgroundResource(R.drawable.led_an);
     	isOn = false;
     }
     
     boolean getStatus() {
     	return isOn;
     }
 }
