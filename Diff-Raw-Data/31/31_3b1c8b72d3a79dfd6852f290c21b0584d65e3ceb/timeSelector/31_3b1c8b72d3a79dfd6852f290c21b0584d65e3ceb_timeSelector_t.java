 package yoloswagswag.swe_app;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class timeSelector extends Activity {
 
     /* this is where the selected times are stored */
     public static final String PREFS_NAME = "selectedTimesStrorage";
 
     /* this is where the selected timeslots are stored */
     Calendar[][] timeSelector = new Calendar[7][4];
 
     /* today */
     Calendar currentDate = Calendar.getInstance();
 
     /* always start with monday selected*/
     int selectedDay = Calendar.MONDAY;
 
     Button[] timeButtons = new Button[15];
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.time_selector);
 
 
         /* need these for initialisation of timeSelector */
         int year = currentDate.get(Calendar.YEAR);
         int month = currentDate.get(Calendar.MONTH);
         int day = currentDate.get(Calendar.DAY_OF_MONTH);
 
 
         File f = new File("/data/data/"+getPackageName()+"/shared_prefs/"+PREFS_NAME+".xml");
 
         /* check for previous settings */
         if(f.exists()){
             SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
             for (int i=0;i<timeSelector.length;i++){
                 for (int j=0;j<timeSelector[i].length;j++){
 
 
                     /* avoid invalid values */
                     if ((day+i) > currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)){
                         if (month > currentDate.getActualMaximum(Calendar.MONTH)-1){
                             month = 1;
                             year++;
                         } else {
                             month++;
                         }
                         day = 1-i;
                     }
 
                     timeSelector[i][j].set(year, month, day+i, settings.getInt("day"+i+"slot"+j,0),0);
                 }
             }
 
         }else{
 
             /* not previous settings -> initialise timeButtons */
             timeButtons[0] = (Button) findViewById(R.id.button09);
             timeButtons[1] = (Button) findViewById(R.id.button10);
             timeButtons[2] = (Button) findViewById(R.id.button11);
             timeButtons[3] = (Button) findViewById(R.id.button12);
             timeButtons[4] = (Button) findViewById(R.id.button13);
             timeButtons[5] = (Button) findViewById(R.id.button14);
             timeButtons[6] = (Button) findViewById(R.id.button15);
             timeButtons[7] = (Button) findViewById(R.id.button16);
             timeButtons[8] = (Button) findViewById(R.id.button17);
             timeButtons[9] = (Button) findViewById(R.id.button18);
             timeButtons[10] = (Button) findViewById(R.id.button19);
             timeButtons[11] = (Button) findViewById(R.id.button20);
             timeButtons[12] = (Button) findViewById(R.id.button21);
             timeButtons[13] = (Button) findViewById(R.id.button22);
             timeButtons[14] = (Button) findViewById(R.id.button23);
 
             /* initialise */
             for(int i=0;i < timeSelector.length; i++){
                 /* avoid invalid values */
                 if ((day+i) > currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)){
                     if (month > currentDate.getActualMaximum(Calendar.MONTH)-1){
                         month = 1;
                         year++;
                     } else {
                         month++;
                     }
                     day = 1-i;
                 }
 
                 //Log.v("test", String.valueOf(i));
                 /* default selected timeslots */
                 timeSelector[i][0] = new GregorianCalendar();
                 timeSelector[i][0].set(year, month, day+i, 9, 0);
                 timeSelector[i][1] = new GregorianCalendar();
                 timeSelector[i][1].set(year, month, day+i, 13, 0);
                 timeSelector[i][2] = new GregorianCalendar();
                 timeSelector[i][2].set(year, month, day+i, 16, 0);
                 timeSelector[i][3] = new GregorianCalendar();
                 timeSelector[i][3].set(year, month, day+i, 20, 0);
             }
 
         }
 
         /* and apply the selected times to the view */
         setButtonColors();
     }
 
     public void okTime(View view){
 
         /* store chosen times in settings */
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         SharedPreferences.Editor editor = settings.edit();
 
         for(int i=0;i<timeSelector.length;i++){
             for (int j=0;j<timeSelector[i].length;j++){
                 editor.putInt("day"+i+"slot"+j, timeSelector[i][j].get(Calendar.HOUR_OF_DAY));
             }
         }
        
        if (!this.isTaskRoot())
            this.finish();
        else
            startActivity(new Intent(this, chillActivity.class));
     }
 
     public void daySelect(View view){
         switch(view.getId()){
             case R.id.buttonMon:
                 selectedDay = Calendar.MONDAY;
                 break;
             case R.id.buttonTue:
                 selectedDay = Calendar.TUESDAY;
                 break;
             case R.id.buttonWed:
                 selectedDay = Calendar.WEDNESDAY;
                 break;
             case R.id.buttonThu:
                 selectedDay = Calendar.THURSDAY;
                 break;
             case R.id.buttonFri:
                 selectedDay = Calendar.FRIDAY;
                 break;
             case R.id.buttonSat:
                 selectedDay = Calendar.SATURDAY;
                 break;
             case R.id.buttonSun:
                 selectedDay = Calendar.SUNDAY;
                 break;
         }
         /* show selected times for selected day */
         setButtonColors();
     }
 
     public void timeSelect(View view){
 
         switch(view.getId()){
             case R.id.button09:
                 timeSelector[selectedDay-1][0].set(Calendar.HOUR_OF_DAY, 9);
                 break;
             case R.id.button10:
                 timeSelector[selectedDay-1][0].set(Calendar.HOUR_OF_DAY, 10);
                 break;
             case R.id.button11:
                 timeSelector[selectedDay-1][0].set(Calendar.HOUR_OF_DAY, 11);
                 break;
             case R.id.button12:
                 timeSelector[selectedDay-1][0].set(Calendar.HOUR_OF_DAY, 12);
                 break;
             case R.id.button13:
                 timeSelector[selectedDay-1][1].set(Calendar.HOUR_OF_DAY, 13);
                 break;
             case R.id.button14:
                 timeSelector[selectedDay-1][1].set(Calendar.HOUR_OF_DAY, 14);
                 break;
             case R.id.button15:
                 timeSelector[selectedDay-1][1].set(Calendar.HOUR_OF_DAY, 15);
                 break;
             case R.id.button16:
                 timeSelector[selectedDay-1][2].set(Calendar.HOUR_OF_DAY, 16);
                 break;
             case R.id.button17:
                 timeSelector[selectedDay-1][2].set(Calendar.HOUR_OF_DAY, 17);
                 break;
             case R.id.button18:
                 timeSelector[selectedDay-1][2].set(Calendar.HOUR_OF_DAY, 18);
                 break;
             case R.id.button19:
                 timeSelector[selectedDay-1][2].set(Calendar.HOUR_OF_DAY, 19);
                 break;
             case R.id.button20:
                 timeSelector[selectedDay-1][3].set(Calendar.HOUR_OF_DAY, 20);
                 break;
             case R.id.button21:
                 timeSelector[selectedDay-1][3].set(Calendar.HOUR_OF_DAY, 21);
                 break;
             case R.id.button22:
                 timeSelector[selectedDay-1][3].set(Calendar.HOUR_OF_DAY, 22);
                 break;
             case R.id.button23:
                 timeSelector[selectedDay-1][3].set(Calendar.HOUR_OF_DAY, 23);
                 break;
         }
         setButtonColors();
     }
 
     public void setButtonColors(){
         /* fetch the button to be colored green and pass it's index to colorButton */
         colorButton(timeSelector[selectedDay-1][0].get(Calendar.HOUR_OF_DAY)-9);
         colorButton(timeSelector[selectedDay-1][1].get(Calendar.HOUR_OF_DAY)-9);
         colorButton(timeSelector[selectedDay-1][2].get(Calendar.HOUR_OF_DAY)-9);
         colorButton(timeSelector[selectedDay-1][3].get(Calendar.HOUR_OF_DAY)-9);
     }
 
     public void colorButton(int buttonIndex){
         /* color all buttons in the line red and the selected one green */
         int i=0;
         if (buttonIndex<4){
             for (;i<=3;i++)
                 timeButtons[i].setBackgroundColor(0xffff4444);
             timeButtons[buttonIndex].setBackgroundColor(0xff99cc00);
         }else if (buttonIndex<7){
             for (;i<=2;i++)
                 timeButtons[i+4].setBackgroundColor(0xffff4444);
             timeButtons[buttonIndex].setBackgroundColor(0xff99cc00);
 
         }else if (buttonIndex<11){
             for (;i<=3;i++)
                 timeButtons[i+7].setBackgroundColor(0xffff4444);
             timeButtons[buttonIndex].setBackgroundColor(0xff99cc00);
 
         }else {
             for (;i<=3;i++)
                 timeButtons[i+11].setBackgroundColor(0xffff4444);
             timeButtons[buttonIndex].setBackgroundColor(0xff99cc00);
 
         }
 
     }

    @Override
    public void onBackPressed() {
    // Wenn es nicht der knoten ist, soll es geschlossen werden
        if (!this.isTaskRoot())
            this.finish();
        else
            super.onBackPressed();
    }
 }
