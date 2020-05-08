 package ru.spbau.cheque;
 
 import android.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 /**
  * Classname:
  * User: dimatwl
  * Date: 4/20/12
  * Time: 9:25 PM
  */
public class SpendingsSumActivity extends Activity {
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(ru.spbau.cheque.R.layout.spendingssum);
         TextView textOnScreen = (TextView) findViewById(ru.spbau.cheque.R.id.sum);
         textOnScreen.setText("Current sum of your spendings is: 100500$");
 
     }
 }
