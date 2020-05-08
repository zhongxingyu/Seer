 package com.oneandhalf;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Toast;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 
 import com.oneandhalf.helpers.FareCalculator;
 import com.oneandhalf.helpers.Fares;
 import com.oneandhalf.helpers.Meters;
 
 public class OneandhalfActivity extends Activity implements OnCheckedChangeListener, OnClickListener
 {
 	
 	RadioGroup meterRG;
     RadioGroup fareRG;
     
     RadioButton meter17RB;
     RadioButton meter20RB;
     
     RadioButton fare15RB;
     RadioButton fare2RB;
     
     Button calculateB;
     
     FareCalculator fc;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         meterRG = (RadioGroup)findViewById(R.id.meterRadioGroup);
     	fareRG = (RadioGroup)findViewById(R.id.fareRadioGroup);
     	
     	meter17RB = (RadioButton)findViewById(R.id.meter17Radio);
     	meter20RB = (RadioButton)findViewById(R.id.meter20Radio);
     	
     	fare15RB = (RadioButton)findViewById(R.id.fare15Radio);
     	fare2RB = (RadioButton)findViewById(R.id.fare2Radio);
     	
     	calculateB = (Button)findViewById(R.id.calculateButton);
     	
         fc = new FareCalculator();
         
         meterRG.setOnCheckedChangeListener(this);
         fareRG.setOnCheckedChangeListener(this);
         
         calculateB.setOnClickListener(this);
     }
 
 	@Override
 	public void onCheckedChanged(RadioGroup group, int checkedId)
 	{
 		if (group.getId() == meterRG.getId())
 		{
 			if (checkedId == meter17RB.getId())
 				fc.setMeter(Meters.seventeen);
 			else if (checkedId == meter20RB.getId())
 				fc.setMeter(Meters.twenty);
 		}
 		else if (group.getId() == fareRG.getId())
 		{
 			if (checkedId == fare15RB.getId())
 				fc.setFare(Fares.oneAndHalf);
 			else if (checkedId == fare2RB.getId())
 				fc.setFare(Fares.doubles);
 		}
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 		if (v.getId() == calculateB.getId())
 		{
 			EditText fareET = (EditText)findViewById(R.id.fareEditText);
 			TextView resultTV = (TextView)findViewById(R.id.resultTextView);
 			
 			String fare_str = fareET.getText().toString().trim();
 			
 			if (fare_str.equals(""))
 			{
				//Put toast , just returning for now
 				return;
 			}
 			
			if (Integer.parseInt(fare_str) < 17)
 			{
				// put another toast, just considering fare to be 17 for now.
				fare_str = "17";
 			}
 			
 			int fare = fc.calculate(fare_str);
 			
 			resultTV.setText("Rs. " + fare);
 		}
 			
 			
 	}
 }
