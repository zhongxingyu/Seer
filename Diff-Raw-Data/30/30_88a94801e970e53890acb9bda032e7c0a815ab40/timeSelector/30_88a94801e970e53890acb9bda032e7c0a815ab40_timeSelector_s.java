 package yoloswagswag.swe_app;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class timeSelector extends Activity {
 
     /* this is where the selected times are stored */
     public static final String SELECTED_TIMES_STORAGE = "selectedTimesStorage";
     public static final String PAST_ALARMS_STORAGE = "pastAlarmsStorage";
 
     /* this is where the selected timeslots are stored */
     int[][] timeSelector = new int[7][4];
 
     /* today */
     Calendar currentDate = new GregorianCalendar();
 
     /* always start with monday selected*/
     int selectedDay = Calendar.MONDAY;
 
     Button[] timeButtons = new Button[15];
     Button[] weekButtons = new Button[7];
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.time_selector);
 
         SharedPreferences selectedTimesSett = getSharedPreferences(SELECTED_TIMES_STORAGE, 0);
 
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
 
         weekButtons[0] = (Button) findViewById(R.id.buttonSun);
         weekButtons[1] = (Button) findViewById(R.id.buttonMon);
         weekButtons[2] = (Button) findViewById(R.id.buttonTue);
         weekButtons[3] = (Button) findViewById(R.id.buttonWed);
         weekButtons[4] = (Button) findViewById(R.id.buttonThu);
         weekButtons[5] = (Button) findViewById(R.id.buttonFri);
         weekButtons[6] = (Button) findViewById(R.id.buttonSat);
 
 
         Calendar textDay = (Calendar) currentDate.clone();
         TextView date = (TextView) findViewById(R.id.chosenDate);
 
         while(textDay.get(Calendar.DAY_OF_WEEK)!=selectedDay){
             textDay.roll(Calendar.DAY_OF_MONTH,true);
         }
         date.setText(textDay.get(Calendar.DAY_OF_MONTH)+"."+(textDay.get(Calendar.MONTH)+1)+".");
 
         /* check for previous userCodeSett */
         if(selectedTimesSett.contains("day6slot3")){
             for (int i=0;i<timeSelector.length;i++){
                 for (int j=0;j<timeSelector[i].length;j++){
                     timeSelector[i][j] = selectedTimesSett.getInt("day"+i+"slot"+j,0);
                 }
             }
         }else{
             /* no previous userCodeSett -> initialise timeButtons */
 
             /* initialise */
             for(int i=0;i < timeSelector.length; i++){
                 /* default selected timeslots */
                 timeSelector[i][0] = 9;
                 timeSelector[i][1] = 13;
                 timeSelector[i][2] = 16;
                 timeSelector[i][3] = 20;
             }
 
         }
 
         /* and apply the selected times to the view */
         setButtonColors();
 
         disableButtons();
 
 
     }
 
     private void updateAlarm(){
         SharedPreferences selectedTimesStorage = getSharedPreferences("selectedTimesStorage",0);
         Calendar currentDay = new GregorianCalendar();
         int nextSlot =0;
         int slotHour = selectedTimesStorage.getInt("day"+(currentDay.get(Calendar.DAY_OF_WEEK)-1)+"slot"+ nextSlot, 0);
         while(slotHour<= currentDay.get(Calendar.HOUR_OF_DAY)&& nextSlot <4)
         {
             nextSlot++;
            slotHour=selectedTimesStorage.getInt("day"+(currentDay.get(Calendar.DAY_OF_WEEK)-1)+"slot"+ nextSlot, 0);
        }
        if(nextSlot ==4){
            slotHour=selectedTimesStorage.getInt("day"+(currentDay.get(Calendar.DAY_OF_WEEK))+"slot"+0, 0);
         }
         Calendar nextAlarmTime = new GregorianCalendar();
         nextAlarmTime.set(Calendar.HOUR_OF_DAY, slotHour);
         nextAlarmTime.set(Calendar.MINUTE, 0);
         nextAlarmTime.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, pollActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 10000, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), pendingIntent);
     }
 
     public void okTime(View view){
 
         /* store chosen times in userCodeSett */
         SharedPreferences settings = getSharedPreferences(SELECTED_TIMES_STORAGE, 0);
         SharedPreferences.Editor editor = settings.edit();
         for(int i=0;i<timeSelector.length;i++){
             for (int j=0;j<timeSelector[i].length;j++){
                 editor.putInt("day"+i+"slot"+j, timeSelector[i][j]);
             }
         }
         editor.commit();
 
         updateAlarm();
 
         if (!this.isTaskRoot())
             this.finish();
         else {
             startActivity(new Intent(this, chillActivity.class));
             this.finish();
         }
 
     }
 
     public void daySelect(View view){
         Calendar textDay = (Calendar) currentDate.clone();
 
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
             default:
                 break;
         }
 
 
         TextView date = (TextView) findViewById(R.id.chosenDate);
 
         while(textDay.get(Calendar.DAY_OF_WEEK)!=selectedDay){
             textDay.roll(Calendar.DAY_OF_MONTH,true);
         }
         date.setText(textDay.get(Calendar.DAY_OF_MONTH)+"."+(textDay.get(Calendar.MONTH)+1)+".");
 
         /* update the shown buttons */
         setButtonColors();
         disableButtons();
     }
 
     public void timeSelect(View view){
 
         switch(view.getId()){
             case R.id.button09:
                 timeSelector[selectedDay-1][0] = 9;
                 break;
             case R.id.button10:
                 timeSelector[selectedDay-1][0] = 10;
                 break;
             case R.id.button11:
                 timeSelector[selectedDay-1][0] = 11;
                 break;
             case R.id.button12:
                 timeSelector[selectedDay-1][0] = 12;
                 break;
             case R.id.button13:
                 timeSelector[selectedDay-1][1] = 13;
                 break;
             case R.id.button14:
                 timeSelector[selectedDay-1][1] = 14;
                 break;
             case R.id.button15:
                 timeSelector[selectedDay-1][1] = 15;
                 break;
             case R.id.button16:
                 timeSelector[selectedDay-1][2] = 16;
                 break;
             case R.id.button17:
                 timeSelector[selectedDay-1][2] = 17;
                 break;
             case R.id.button18:
                 timeSelector[selectedDay-1][2] = 18;
                 break;
             case R.id.button19:
                 timeSelector[selectedDay-1][2] = 19;
                 break;
             case R.id.button20:
                 timeSelector[selectedDay-1][3] = 20;
                 break;
             case R.id.button21:
                 timeSelector[selectedDay-1][3] = 21;
                 break;
             case R.id.button22:
                 timeSelector[selectedDay-1][3] = 22;
                 break;
             case R.id.button23:
                 timeSelector[selectedDay-1][3] = 23;
                 break;
             default:
                 break;
         }
         setButtonColors();
     }
 
     public void setButtonColors(){
         /* color all buttons in the line red and the selected one green */
         for (Button timeButton : timeButtons){
             timeButton.setBackgroundColor(0xffff4444);
         }
         timeButtons[timeSelector[selectedDay - 1][0] - 9].setBackgroundColor(0xff99cc00);
         timeButtons[timeSelector[selectedDay - 1][1] - 9].setBackgroundColor(0xff99cc00);
         timeButtons[timeSelector[selectedDay - 1][2] - 9].setBackgroundColor(0xff99cc00);
         timeButtons[timeSelector[selectedDay - 1][3] - 9].setBackgroundColor(0xff99cc00);
 
         for (Button weekButton : weekButtons) {
             weekButton.setBackgroundColor(0xffff4444);
         }
         weekButtons[selectedDay-1].setBackgroundColor(0xff99cc00);
     }
 
     private void disableButtons() {
 
         SharedPreferences pastAlarmsSett = getSharedPreferences(PAST_ALARMS_STORAGE,0);
 
         /* disable Buttons */
         if(selectedDay == currentDate.get(Calendar.DAY_OF_WEEK)){
             /* disable times in the past */
             for(int i=0;i<timeButtons.length;i++){
                 if (i+9 <= currentDate.get(Calendar.HOUR_OF_DAY)){
                     timeButtons[i].setTextColor(Color.GRAY);
                     timeButtons[i].setClickable(false);
                 } else {
                     timeButtons[i].setTextColor(Color.WHITE);
                     timeButtons[i].setClickable(true);
                 }
             }
 
             /* disable timeslots which already have triggered an alarm */
             if (pastAlarmsSett.getInt("day",0)==currentDate.get(Calendar.DAY_OF_WEEK)){
                 int disableSlots=0;
                 switch (pastAlarmsSett.getInt("slot",4)){
                     case 0:
                         disableSlots=3;
                         break;
                     case 1:
                         disableSlots=6;
                         break;
                     case 2:
                         disableSlots=10;
                         break;
                     case 3:
                         disableSlots=14;
                         break;
                     default:
                         break;
                 }
                 for (int i=0;i<=disableSlots;i++){
                     timeButtons[i].setTextColor(Color.GRAY);
                     timeButtons[i].setClickable(false);
                 }
             }
         } else {
             for (Button timeButton : timeButtons) {
                 timeButton.setTextColor(Color.WHITE);
                 timeButton.setClickable(true);
             }
         }
     }
 
     @Override
     public void onBackPressed() {
         if (!this.isTaskRoot())
             this.finish();
         else
             super.onBackPressed();
     }
 }
