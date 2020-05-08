 package ru.spbau.cheque;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import ru.spbau.cheque.recognition.BlueObject;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class SpendingsSumActivity extends Activity {
 
    private DBOpenHelper helper = new DBOpenHelper(SpendingsSumActivity.this);
 
 
 
     private String getGlobalSumm(){
         SQLiteDatabase db = helper.getReadableDatabase();
         Cursor cursor = db.query(DBOpenHelper.BLUE_TABLE_NAME, new String[] { "SUM(" + DBOpenHelper.BLUE_PRICE + ")" }, null, null, null, null, null);
         cursor.moveToFirst();
        return new DecimalFormat("#.##").format(cursor.getFloat(0));
     }
 
 
     private String getSumOfLast(Integer inpLimit){
         SQLiteDatabase db = helper.getReadableDatabase();
         Cursor cursor = db.query(DBOpenHelper.BLUE_TABLE_NAME, new String[] { "SUM(" + DBOpenHelper.BLUE_PRICE + ")" }, "_id > (SELECT MAX(_id) FROM " + DBOpenHelper.BLUE_TABLE_NAME + ") - " + inpLimit.toString(), null, null, null, null, null);
         cursor.moveToFirst();

        return new DecimalFormat("#.##").format(cursor.getFloat(0));
     }
 
     private void drobDB(){
         SpendingsSumActivity.this.deleteDatabase(DBOpenHelper.DB_NAME);
     }
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
        //helper.fillDBWithRandomData(20);
         //drobDB();
 
         setContentView(ru.spbau.cheque.R.layout.spendingssum);
         Button goBackBtn = (Button) findViewById(R.id.goBack);
         TextView textOnScreen = (TextView) findViewById(ru.spbau.cheque.R.id.sum);
         textOnScreen.setText("Current sum of all spendings is: " + getGlobalSumm() + " rubles.");
 
         goBackBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(SpendingsSumActivity.this, Main.class);
                 startActivity(intent);
             }
         });
 
     }
 
 }
