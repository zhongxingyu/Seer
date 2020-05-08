 package dummy.MahjonggCalc.activity;
 
 import android.widget.TextView;
 import dummy.MahjonggCalc.R;
 
 public class ActivityTools {
     public static void setLabel(TextView textView, Integer value) {
         textView.setText(value.toString());
         if (value < 0) {
             textView.setTextColor(textView.getResources().getColor(R.color.red));
         } else {
             textView.setTextColor(textView.getResources().getColor(R.color.green));
         }
     }
 
     public static void setLabel(TextView textView, String text, Integer value) {
        textView.setText(text.toString());
         if (value < 0) {
             textView.setTextColor(textView.getResources().getColor(R.color.red));
         } else {
             textView.setTextColor(textView.getResources().getColor(R.color.green));
         }
     }
 }
