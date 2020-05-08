 package com.example.bmicalc;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 
 
 public class BmicalcActivity extends Activity implements OnClickListener {
     
 	protected Button CalcLabel;
 	protected TextView ErgebnisLabel;
 	protected EditText HeightLabel;
 	protected EditText WeightLabel;
 	
 	
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         
         CalcLabel = (Button) findViewById(R.id.calc_bmi);
         ErgebnisLabel = (TextView) findViewById(R.id.et_result);	
         HeightLabel = (EditText) findViewById(R.id.et_height);
     	WeightLabel = (EditText) findViewById(R.id.et_weight);
         
         
         CalcLabel.setOnClickListener( this);
         
         HeightLabel.setText("180");
         WeightLabel.setText("80");
     }
         
     public void onClick(View v) {
             	if(v == CalcLabel) {
                  String s1 = HeightLabel.getText().toString();
                  Double h = Double.parseDouble(s1);
 
                  String s2 = WeightLabel.getText().toString();
                  Double w = Double.parseDouble(s2);
 
                  h = h/100;
                  
                  Double bmi = w / ( h * h );
 
                  NumberFormat formatter = new DecimalFormat(".00");
                  String s3 = formatter.format(bmi); 
                  
            	ErgebnisLabel.setText( " Der BMI beträgt "  + s3 +"kg/m²." );
                  
             	}
      }
           
 }
