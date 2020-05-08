 package ru.spbau.cheque;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * Classname:
  * User: dimatwl
  * Date: 4/23/12
  * Time: 12:15 PM
  */
 public class SpendingsPeriodActivity extends Activity{
 
     private Long date1 = null;
     private Long date2 = null;
 
     EditText editDate1 = null;
     EditText editDate2 = null;
 
     static final int DATE_DIALOG_ID1 = 1;
     static final int DATE_DIALOG_ID2 = 2;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         //helper.fillDBWithRandomData(20);
 
         setContentView(R.layout.spendingsperiod);
         Button goBackBtn = (Button) findViewById(R.id.goBack);
         
         editDate1 = (EditText) findViewById(R.id.date1);
         editDate2 = (EditText) findViewById(R.id.date2);
 
         final long millis = 1000;
         final long seconds = 60;
         final long minutes = 60;
         final long hours = 24;
         final long days = 30;
         final long millisInMonth = millis*seconds*minutes*hours*days;
         date1 = System.currentTimeMillis() - millisInMonth;
         date2 = System.currentTimeMillis();
 
         goBackBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(SpendingsPeriodActivity.this, Main.class);
                 startActivity(intent);
             }
         });
 
         editDate1.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID1);
             }
         });
 
 
         editDate1.setOnTouchListener(new View.OnTouchListener(){
             public boolean onTouch(View v, MotionEvent e) {
                 showDialog(DATE_DIALOG_ID1);
                 return false;
             }
         });
 
         editDate2.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID2);
             }
         });
 
         editDate2.setOnTouchListener(new View.OnTouchListener(){
             public boolean onTouch(View v, MotionEvent e) {
                 showDialog(DATE_DIALOG_ID2);
                 return false;
             }
         });
 
 
 
         UpdateScreen();
 
     }
 
     private DatePickerDialog.OnDateSetListener mDateSetListener1 =
             new DatePickerDialog.OnDateSetListener() {
 
                 public void onDateSet(DatePicker view, int year,
                                       int monthOfYear, int dayOfMonth) {
                     Calendar c = new GregorianCalendar();
                     c.set(year,monthOfYear, dayOfMonth);
                     date1 = c.getTimeInMillis();
                     UpdateScreen();
                 }
             };
 
     private DatePickerDialog.OnDateSetListener mDateSetListener2 =
             new DatePickerDialog.OnDateSetListener() {
 
                 public void onDateSet(DatePicker view, int year,
                                       int monthOfYear, int dayOfMonth) {
                     Calendar c = new GregorianCalendar();
                     c.set(year,monthOfYear, dayOfMonth);
                     date2 = c.getTimeInMillis();
                     UpdateScreen();
                 }
             };
 
     private void UpdateScreen(){
         String DATE_FORMAT = "dd/MM/yyyy";
         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
         if (date1 != null){
             Calendar c = new GregorianCalendar();
             c.setTimeInMillis(date1);
             Date dt =  c.getTime();
             editDate1.setText(sdf.format(c.getTime()));
         }
         if (date2 != null){
             Calendar c = new GregorianCalendar();
             c.setTimeInMillis(date2);
             editDate2.setText(sdf.format(c.getTime()));
         }
 
         TextView textOnScreen = (TextView) findViewById(ru.spbau.cheque.R.id.sum);
         
         if (date1 != null && date2 != null){
             if (date1 <= date2){
                 //do querry
                 String rawSql = "SELECT SUM("+DBOpenHelper.BLUE_TABLE_NAME+"."+DBOpenHelper.BLUE_PRICE+") FROM "+
                         DBOpenHelper.BLUE_TABLE_NAME+
                         " INNER JOIN "+
                         DBOpenHelper.CHEQUE_TABLE_NAME+
                         " ON "+
                         DBOpenHelper.BLUE_TABLE_NAME+"."+DBOpenHelper.BLUE_CHEQUE_REF+" = "+DBOpenHelper.CHEQUE_TABLE_NAME+"._cheque_id"+
                         " WHERE "+DBOpenHelper.CHEQUE_DATE+" >= "+date1.toString()+
                         " AND "+DBOpenHelper.CHEQUE_DATE+" <= "+date2.toString();
                 SQLiteDatabase db = new DBOpenHelper(SpendingsPeriodActivity.this).getWritableDatabase();
                 Cursor cursor = db.rawQuery(rawSql, null);
                 cursor.moveToFirst();
                 textOnScreen.setText("Sum of spendings is: " + new DecimalFormat("#.##").format(cursor.getFloat(0)) + " rubles.");
                cursor.close();
 
             } else {
                 textOnScreen.setText("Sorry, but it's impossible!");
             }
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DATE_DIALOG_ID1:
                 Calendar c = new GregorianCalendar();
                 c.setTimeInMillis(date1);
                 return new DatePickerDialog(this,
                         mDateSetListener1,
                         c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
             case DATE_DIALOG_ID2:
                 Calendar c1 = new GregorianCalendar();
                 c1.setTimeInMillis(date2);
                 return new DatePickerDialog(this,
                         mDateSetListener2,
                         c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH));
         }
         return null;
     }
 }
