 package com.disrupt.conservo;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.RadioButton;
 
 /**
  *  @author Skylar Castator
  */
 public class InsideWaterUseActivity extends Activity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.insidewateruse);
        
     }
     
     public void onRadioButtonClicked(View view) {
         // Is the button now checked?
         boolean checked = ((RadioButton) view).isChecked();
        int TotalTolietriesScore; 
         
         // Check which radio button was clicked
         switch(view.getId()) {
             case R.id.InsideWater_radio0:
                 if (checked)
                     //Faucet Aerators 0
                 	Score.faucetsAndFixturesScore = 0;
                 break;
             case R.id.InsideWater_radio1:
                 if (checked)
                     //Faucet Aerators 75
                 	Score.faucetsAndFixturesScore = 75;
                 break;
             case R.id.InsideWater_radio2:
                 if (checked)
                     //Faucet Aerators 85
                 	Score.faucetsAndFixturesScore = 85;
                 break;
             case R.id.InsideWater_radio3:
                 if (checked)
                     //Faucet Aerators 100
                 	Score.faucetsAndFixturesScore = 100;
                 break;
             case R.id.InsideWater2_radio0:
                 if (checked)
                     // Low Flow 60
                 	TotalTolietriesScore += 60;
                 	
                 break;
             case R.id.InsideWater2_radio1:
                 if (checked)
                 	// Low Flow 80
                 	TotalTolietriesScore += 80;
                 break;
             case R.id.InsideWater2_radio2:
                 if (checked)
                 	// Low Flow 100
                 	TotalTolietriesScore += 100;
                 break;
             case R.id.InsideWater3_radio0:
                 if (checked)
                     //Ultra Low Flow 60
                 	TotalTolietriesScore += 60;
                 break;
             case R.id.InsideWater3_radio1:
                 if (checked)
                 	//Ultra Low Flow 60
                 	TotalTolietriesScore += 80;
                 break;
             case R.id.InsideWater3_radio2:
                 if (checked)
                 	//Ultra Low Flow 60
                 	TotalTolietriesScore += 100;
                 break;
             case R.id.InsideWater4_radio0:
                 if (checked)
                     // Automatic Control 60
                 	TotalTolietriesScore += 60;
                 break;
             case R.id.InsideWater4_radio1:
                 if (checked)
                     // Automatic Control 80
                 	TotalTolietriesScore += 80;
                 break;
             case R.id.InsideWater4_radio2:
                 if (checked)
                     // Automatic Control 100
                 	TotalTolietriesScore += 100;
                 break;
                 
                 //Send Full Value to score
         }
     }
 }
