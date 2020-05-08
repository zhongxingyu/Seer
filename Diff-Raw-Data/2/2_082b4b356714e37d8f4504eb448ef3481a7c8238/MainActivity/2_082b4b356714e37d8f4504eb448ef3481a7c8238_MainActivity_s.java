 package solarAndroid;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.my_layout);
         
     }
     
     // check what view matters
     public void submitDetails(View view) {    	
     	Integer panelSize = Integer.parseInt(((EditText)findViewById(R.id.PanelSize)).getText().toString());
 		Integer panelEfficiency = Integer.parseInt(((EditText)findViewById(R.id.PanelEfficiency)).getText().toString());
 		Integer inverterEfficiency = Integer.parseInt(((EditText)findViewById(R.id.InverterEfficiency)).getText().toString());
 		String address = ((EditText)findViewById(R.id.Address)).getText().toString();
 		String orientation = ((EditText)findViewById(R.id.PanelOrientation)).getText().toString();
 		Integer angle = Integer.parseInt(((EditText)findViewById(R.id.PanelAngle)).getText().toString());
 		Integer sunlight = Integer.parseInt(((EditText)findViewById(R.id.SunlightHours)).getText().toString());
 		Integer consumption = Integer.parseInt(((EditText)findViewById(R.id.PowerConsumption)).getText().toString());
     }
 
 
 	//Might want a menu later
     /*@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }*/
 
     
 }
