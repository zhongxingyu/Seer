 package net.doorstop.metrocalc;
 
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class Calculator extends Activity {
 
     private static int TABLE[] = {
        1565, 1365, 1165, 965, 2135,
        1935, 1735, 1535, 1335, 1135,
        935, 930, 2100, 1900, 1700,
        1500, 1300, 1100, 900, 2265,
        2065, 1865, 1665, 1465, 1265,
        1065, 865, 2035, 1835, 1635,
        1435, 1235, 1035, 835, 830,
        2000, 1800, 1600, 1400, 1200,
        1000, 800, 2165, 1965, 1765
    };
 
     private static final int BONUS_MULTIPLIER = 15;
     private int currentCents = 200;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         refresh();
     }
 
     public void addCents(View v) {
         addCents(Integer.valueOf((String)v.getTag()));
     }
 
     public void addCents(int n) {
         currentCents += n;
         currentCents = Math.max(0, currentCents);
         refresh();
     }
 
     private void refresh() {
         setTextViewText(R.id.currentValue, formatCents(currentCents));
         int suggestion = computeSuggestionWithBonus(currentCents);
         setTextViewText(R.id.suggestion, formatCents(suggestion));
         int bonus = suggestion * BONUS_MULTIPLIER / 100;
         setTextViewText(R.id.bonus, formatCents(bonus));
         int newTotal = currentCents + suggestion + bonus;
         setTextViewText(R.id.newTotal, formatCents(newTotal));
     }
 
     public static int computeSuggestionWithBonus(int cents) {
         if (cents % 5 != 0) {
             throw new IllegalArgumentException("input must be nonnegative multiple of 5.");
         }
         int i = cents % 225 / 5;
         return TABLE[i];
     }
 
     private void setTextViewText(int textViewId, String value) {
         TextView view = (TextView) findViewById(textViewId);
         view.setText(value);
     }
 
     private String formatCents(int value) {
         String dollars = new DecimalFormat("$0.").format(value / 100);
         String cents = new DecimalFormat("00").format(value % 100);
         return dollars + cents;
     }
 }
