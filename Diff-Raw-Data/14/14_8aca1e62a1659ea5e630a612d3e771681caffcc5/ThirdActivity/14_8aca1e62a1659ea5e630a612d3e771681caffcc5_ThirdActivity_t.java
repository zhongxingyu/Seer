 package sim7.speedscanadvanced;
 
 import java.text.DecimalFormat;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class ThirdActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_third);
 		TextView t =new TextView(this); 
 		   DecimalFormat dFormatObject = new DecimalFormat("#.##");
 	    ;
 		Bundle extras = getIntent().getExtras();
 		long[] arrayB = extras.getLongArray("numbers");
 		long finalMilTime =arrayB[1] - arrayB[0];
 		double finalTime=  finalMilTime;
 		double speedTime= (3799551.0*8.0)/finalTime; 
 				
 		t=(TextView)findViewById(R.id.textViewResults);
 	
 		TextView t2 =new TextView(this); 
 		t2=(TextView)findViewById(R.id.textViewMore);
 		
 		t.setText("Speed Test Results= \n"+
 		"Current Download speed \n"+ dFormatObject.format(speedTime) +" kbps \n"
 		+dFormatObject.format(speedTime/1000.0) +" mbps");
 		
 		t2.setText("This will download a 250 Megabyte file in " +dFormatObject.format(250.0/(speedTime/8192.0))	
 				+	" seconds\n or " +dFormatObject.format((250.0/(speedTime/8192.0))/60.0) + " minutes.\n"+
				"\nThis will download a 1 Gigabyte file in " + dFormatObject.format(1.0/(speedTime/8388608.0)) + " seconds\n " +
 						"or" + dFormatObject.format((1.0/(speedTime/8388608.0))/60.0) + "minutes");
 	
 	
 	
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.third, menu);
 		return true;
 	}
 
 }
