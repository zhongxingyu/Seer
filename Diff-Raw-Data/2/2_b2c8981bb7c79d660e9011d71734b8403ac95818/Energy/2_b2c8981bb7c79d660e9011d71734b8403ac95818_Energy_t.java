 package edu.hawaii.systemh.android.energy;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.TextView;
 import edu.hawaii.systemh.android.R;
 import edu.hawaii.systemh.android.menu.Menu;
 import edu.hawaii.systemh.android.systemdata.SystemData;
 
 /**
  * The activity that starts the help page.
  * 
  * @author Group H
  * 
  */
 public class Energy extends Activity {
   
   /**
    * Called when the activity is first created.
    * 
    * @param savedInstanceState - A mapping from String values to various Parcelable types.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // requesting to turn the title OFF
     requestWindowFeature(Window.FEATURE_NO_TITLE);
 
     // making it full screen
     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
     setContentView(R.layout.energy);
     
     // Get power consumption from electricity and display it
     SystemData electric = new SystemData("electric");
     TextView electricText = (TextView) findViewById(R.id.PowerValue);
     int currentElectric = (int) electric.getPower();
     electricText.setText(currentElectric + "");
     electricText.setTextColor(Color.YELLOW);
     
     // Get power generation from PV and display it
     SystemData pv = new SystemData("photovoltaics");   
     TextView pvText = (TextView) findViewById(R.id.PVValue);
     int currentPV = (int) pv.getEnergy();
     pvText.setText(currentPV + "");
     if (currentPV > currentElectric) {
       pvText.setTextColor(Color.GREEN);
     }
     else {
       pvText.setTextColor(Color.RED);
     }
     
     // Display the difference between power generation and consumption
     TextView netPowerText = (TextView) findViewById(R.id.NetPowerValue);
     int difference = currentPV - currentElectric;
     if (difference > 0) {
       netPowerText.setText("+" + difference);
       netPowerText.setTextColor(Color.GREEN);
     }
    else if (difference == 0) {
       netPowerText.setText(difference);
       netPowerText.setTextColor(Color.CYAN);
     }
     else {
       netPowerText.setText(difference);
       netPowerText.setTextColor(Color.RED);
     }
   }
 
   /**
    * Take the user to the menu.
    * 
    * @param view The view.
    */
   public void showMenu(View view) {
     Intent intent = new Intent(Intent.ACTION_VIEW);
     intent.setClassName(this, Menu.class.getName());
     startActivity(intent);
   }
 
   /**
    * Destroys this activity when onStop is called.
    */
   @Override
   protected void onStop() {
     finish();
     super.onDestroy();
   }
 
 }
