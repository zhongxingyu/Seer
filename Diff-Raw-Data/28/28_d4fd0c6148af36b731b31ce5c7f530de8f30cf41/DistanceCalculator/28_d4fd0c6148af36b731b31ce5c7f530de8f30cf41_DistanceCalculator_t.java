 package com.webs.samirapplications.distance_calculator;
 
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Button;
 import android.widget.Toast;
 import android.view.View;
 
 
 
 
 
 public class DistanceCalculator extends Activity {
     /** Called when the activity is first created. */
 	
 	private EditText x1;
 	private EditText x2;
 	private EditText y1;
 	private EditText y2;
 	private EditText slope;
 	private double slop;
 	private Button pointslope;
 	private double ans;
 	private TextView txtans;
 	private Button calculate;
 	private Button reset;
 	private double xOne;
 	private double yOne;
 	private double xTwo;
 	private double yTwo;
 	private Button calculateslope;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         initControls();
     }
     
     private void initControls()
     {
     	calculate = (Button)findViewById(R.id.calculate);
     	pointslope = (Button)findViewById(R.id.pointslope);
     	calculateslope = (Button)findViewById(R.id.calculateslope);
     	txtans = (TextView)findViewById(R.id.txtans);
     	reset = (Button)findViewById(R.id.reset);
     	x1 = (EditText)findViewById(R.id.x1);
     	x2 = (EditText)findViewById(R.id.x2);
     	y1 = (EditText)findViewById(R.id.y1);
     	y2 = (EditText)findViewById(R.id.y2);
     	slope = (EditText)findViewById(R.id.slope);
     	
     	calculate.setOnClickListener(new Button.OnClickListener()
     	{
     		public void onClick (View v)
       		{
    			try{
 					calculate();
    			}
					catch(Exception e){
						error();
					}
       		} 
       		{Toast.makeText(DistanceCalculator.this, R.string.short_notification_text, Toast.LENGTH_SHORT).show();}
        	});
     	
     	calculateslope.setOnClickListener(new Button.OnClickListener()
     	{
     		public void onClick (View v)
       		{
    			try{
 					calculateslope();
    			}
    			catch(Exception e){
    				error();
    			}
       		} 
       		{Toast.makeText(DistanceCalculator.this, R.string.short_notification_text, Toast.LENGTH_SHORT).show();}
        	});
     	
     	pointslope.setOnClickListener(new Button.OnClickListener() 
		{ public void onClick (View v){
			
			try{
 				pointslope(); 
 			}
		catch(Exception e){
			error();
		}
		}
 		});
     	
     	reset.setOnClickListener(new Button.OnClickListener() 
 		{ public void onClick (View v)
 			{
 				reset(); 
 			}
 		});
     	
     }
     public void calculate() 
     {
     	xOne=Double.parseDouble(x1.getText().toString());
     	xTwo=Double.parseDouble(x2.getText().toString());
     	yOne=Double.parseDouble(y1.getText().toString());
     	yTwo=Double.parseDouble(y2.getText().toString());
 		
     	double x = ( xTwo - xOne ); 
 		double y = ( yTwo - yOne );
 		ans = Math.sqrt ( x*x + y*y );
 		String answer = " The distance is " + ans;		
 		txtans.setText(answer);
     }
     
     public void calculateslope() 
     {
     	xOne=Double.parseDouble(x1.getText().toString());
     	xTwo=Double.parseDouble(x2.getText().toString());
     	yOne=Double.parseDouble(y1.getText().toString());
     	yTwo=Double.parseDouble(y2.getText().toString());
 		
     	double x = ( xTwo - xOne ); 
 		double y = ( yTwo - yOne );
 		ans = y/x;
 		slope.setText(Double.toString(ans));
     }
     
     private void pointslope()
     {
     	xOne=Double.parseDouble(x1.getText().toString());
     	yOne=Double.parseDouble(y1.getText().toString());
     	slop=Double.parseDouble(slope.getText().toString());
     	
     	String answer = "y-"+yOne+"="+slop+"(x-"+xOne+")";
     	txtans.setText(answer);
     }
     
     private void reset()
 	{
 		txtans.setText("");
 		x1.setText("");
 		x2.setText("");
 		y1.setText("");
 		y2.setText("");
 		slope.setText("");
 	}
    public void error(){
		{Toast.makeText(DistanceCalculator.this, R.string.error, Toast.LENGTH_LONG).show();}	
	}
 }
