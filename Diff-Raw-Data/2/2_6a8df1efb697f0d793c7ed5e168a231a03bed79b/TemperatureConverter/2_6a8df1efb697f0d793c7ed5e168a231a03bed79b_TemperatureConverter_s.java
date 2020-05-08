 package com.webs.samirapplications.temperature_convert;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Button;
 import android.widget.Toast;
 import android.view.View;
 import java.lang.Math;
 
 
 
 public class TemperatureConverter extends Activity {
     /** Called when the activity is first created. */
 	
 	private EditText in;
 	private double temp = 0;
 	private double ans = 0;
 	private TextView txtanswer;
 	private Button toFa;
 	private Button toCe;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         toFa = (Button)findViewById(R.id.toFa);
         toCe = (Button)findViewById(R.id.toCe);
         txtanswer = (TextView)findViewById(R.id.txtanswer);
         in = (EditText)findViewById(R.id.temp);
       	toFa.setOnClickListener(new Button.OnClickListener() 
       	{ 
       		public void onClick (View v)
       		{ calculateFa();} 
       		
        	});
       	toCe.setOnClickListener(new Button.OnClickListener() 
       	{ 
       		public void onClick (View v)
       		{ calculateCe();} 
       		
        	});
     }
     private void calculateFa() 
 	{
 		temp=Double.parseDouble(in.getText().toString());
		ans = (temp + 32)*(9.00/5.00);
 		txtanswer.setText(Double.toString(ans));
 	}
     private void calculateCe() 
 	{
 		temp=Double.parseDouble(in.getText().toString());
 		ans = (temp - 32)*(5.00/9.00);
 		txtanswer.setText(Double.toString(ans));
 	}
 }
