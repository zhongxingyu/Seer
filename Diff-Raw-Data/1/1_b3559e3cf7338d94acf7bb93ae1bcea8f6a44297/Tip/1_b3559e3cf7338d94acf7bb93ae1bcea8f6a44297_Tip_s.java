 package com.willkwon.tip;
 
 import java.text.DecimalFormat;
 import com.actionbarsherlock.app.SherlockActivity;
import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class Tip extends SherlockActivity {
 	public static TextView total;
 	public static Boolean solved;
 	public static Double subtotal;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.tip);
         total = (TextView) findViewById(R.id.equation);
         solved = false;
         subtotal = 0.00;
     }
 
     /*
      * Inputs the selected number into the TextView. If already solved, it clears the screen first.
      */
     public void input(View view) {
     	String tag = view.getTag().toString();
     	if (solved) {
     		total.setText("");
     		solved = false;
     		subtotal = 0.00;
     	}
     	
     	String text = total.getText().toString();
     	if (text.contains(".") && text.length() > 3 && text.charAt(text.length() - 3) == '.') {
     		return;
     	}
     	
     	if (tag.equals(".")) {
     		if (text.equals("")) {
     			total.append("0");
     		} else if (text.contains(".")) {
     			return;
     		}
     	}
     	
     	total.append(tag);
     }
     
     /*
      * When one of the percentages are pushed, this gives back the subtotal/tip/total.
      */
     public void tip(View view) {
     	if (subtotal == 0.00) { // No sub-total yet
     		String eq_s = total.getText().toString();
         	if (eq_s.length() > 0 && !solved) {
         		subtotal = Double.parseDouble(eq_s);
         	}
     	}
     	
     	String tag_s = view.getTag().toString();
     	Double tip = Double.parseDouble(tag_s);
     	Double tip_total = subtotal * tip;
     	Double total_cost = subtotal + tip_total;
     	DecimalFormat df = new DecimalFormat("#.00");
     	total.setText("Subtotal: " +  df.format(subtotal) + "\nTip: " + df.format(tip_total) + "\nTotal: " + df.format(total_cost));
     	solved = true;
     }
     
     /*
      * Clears the TextView
      */
     public void backspace(View view) {
     	total.setText("");
     }
     
 }
