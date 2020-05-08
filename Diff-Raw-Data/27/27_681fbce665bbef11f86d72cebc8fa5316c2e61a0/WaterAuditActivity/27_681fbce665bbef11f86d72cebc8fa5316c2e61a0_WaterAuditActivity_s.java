 package com.disrupt.conservo;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.RadioButton;
 
 /**
  *  @author Skylar Castator
  */
 public class WaterAuditActivity extends Activity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState); 
         setContentView(R.layout.wateraudit);
     }
     
     public void onRadioButtonClicked(View view) {
         // Is the button now checked?
         boolean checked = ((RadioButton) view).isChecked();
         
         // Check which radio button was clicked
         switch(view.getId()) {
            case R.id.LastAudit_0:
                 if (checked)
                     //Water Audit 0
                 	Score.waterAuditScore = 0;
                 break;
            case R.id.LastAudit_1:
                 if (checked)
                     //Water Audit 80
                 	Score.waterAuditScore = 80;
                 break;
            case R.id.LastAudit_2:
                 if (checked)
                     //Water Audit 100
                 	Score.waterAuditScore = 100;
                 break;
             case R.id.AuditProject_radio0:
                 if (checked)
                     //Water Project 0
                 	Score.waterProjectsScore = 0;
                 break;
             case R.id.AuditProject_radio1:
                 if (checked)
                     //Water Project 75
                 	Score.waterProjectsScore = 75;
                 break;
             case R.id.AuditProject_radio2:
                 if (checked)
                     //Water Project 85
                 	Score.waterProjectsScore = 85;
                 break;
             case R.id.AuditProject_radio3:
                 if (checked)
                     //Water Project 100
                 	Score.waterProjectsScore = 100;
                 break;
         }
     }
 }
