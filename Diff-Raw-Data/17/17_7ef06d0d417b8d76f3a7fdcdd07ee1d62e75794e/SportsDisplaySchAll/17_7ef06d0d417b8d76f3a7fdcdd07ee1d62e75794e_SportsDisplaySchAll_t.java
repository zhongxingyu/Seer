 package org.umece.android.umaine.sports;
 
 import org.umece.android.umaine.R;
 import org.umece.android.umaine.R.id;
 import org.umece.android.umaine.R.layout;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
import android.widget.TableRow.LayoutParams;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class SportsDisplaySchAll extends Activity {
 	// public static String results = "bbbbbbbbbbbbb";
 
 	// TextView textView;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		/*
 		 * // for testing setContentView(R.layout.sprots_activity_sch_all);
 		 * TextView textView = (TextView) findViewById(R.id.text_view_all);
 		 * textView.setText("test1 all");
 		 */
 
 		this.setContentView(R.layout.sprots_activity_sch_all);
 
 		/* Find Tablelayout defined in main.xml */
 		TableLayout tl = (TableLayout) findViewById(R.id.myTableLayout);
	
 	/*	
 		 Create a new row to be added. 
 		TableRow tr = new TableRow(this);
 		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT));
 		
 		
 		 Create a TextView to be the row-content. 
 		TextView[] textView = new TextView[5];
 		for (int i =0; i < 5; i++){
 		textView[i].setText("Dynamic Button"+i);
 		textView[i].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT));
 		 Add Button to row. 
 		tr.addView(textView[i]);
 		}
 		
 		
 		 Add row to TableLayout. 
 		tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT));
 		*/
 		
 		String [] testData = {"hello1","hello2","hello3","hello4","hello5"};
 		
 		for (int current = 0; current < 5; current++)
         {
             // Create a TableRow and give it an ID
             TableRow tr = new TableRow(this);
             tr.setId(100+current);
             tr.setLayoutParams(new LayoutParams(
                     LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));   
 
             // Create a TextView to house the name of the province
             TextView labelTV = new TextView(this);
             labelTV.setId(200+current);
             labelTV.setText(testData[current]);
            labelTV.setTextColor(Color.WHITE);
             labelTV.setLayoutParams(new LayoutParams(
                     LayoutParams.FILL_PARENT,
                     LayoutParams.WRAP_CONTENT));
             tr.addView(labelTV);
 
             // Create a TextView to house the value of the after-tax income
             TextView valueTV = new TextView(this);
             valueTV.setId(current);
             valueTV.setText("$0");
            valueTV.setTextColor(Color.WHITE);
             valueTV.setLayoutParams(new LayoutParams(
                     LayoutParams.FILL_PARENT,
                     LayoutParams.WRAP_CONTENT));
             tr.addView(valueTV);
 
             // Add the TableRow to the TableLayout
             tl.addView(tr, new TableLayout.LayoutParams(
                     LayoutParams.FILL_PARENT,
                     LayoutParams.WRAP_CONTENT));
 		
 		
 /*
         // Go through each item in the array
         for (int current = 0; current < numProvinces; current++)
         {
             // Create a TableRow and give it an ID
             TableRow tr = new TableRow(this);
             tr.setId(100+current);
             tr.setLayoutParams(new LayoutParams(
                     LayoutParams.FILL_PARENT,
                     LayoutParams.WRAP_CONTENT));   
 
             // Create a TextView to house the name of the province
             TextView labelTV = new TextView(this);
             labelTV.setId(200+current);
             labelTV.setText(provinces[current]);
             labelTV.setTextColor(Color.BLACK);
             labelTV.setLayoutParams(new LayoutParams(
                     LayoutParams.FILL_PARENT,
                     LayoutParams.WRAP_CONTENT));
             tr.addView(labelTV);
 
             // Create a TextView to house the value of the after-tax income
             TextView valueTV = new TextView(this);
             valueTV.setId(current);
             valueTV.setText("$0");
             valueTV.setTextColor(Color.BLACK);
             valueTV.setLayoutParams(new LayoutParams(
                     LayoutParams.FILL_PARENT,
                     LayoutParams.WRAP_CONTENT));
             tr.addView(valueTV);
 
             // Add the TableRow to the TableLayout
             tl.addView(tr, new TableLayout.LayoutParams(
                     LayoutParams.FILL_PARENT,
                     LayoutParams.WRAP_CONTENT));
                     */
         
 
 	}
		
 	}
 }
 	
