 package com.haubey.tangent;
 
 import com.android.graphbutton.plot2d;
 import de.congrace.exp4j.Calculable;
 import de.congrace.exp4j.ExpressionBuilder;
 import de.congrace.exp4j.UnknownFunctionException;
 import de.congrace.exp4j.UnparsableExpressionException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 
 public class Grapher extends Activity
 {
 	String function_string = "";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		
 		LinearLayout buttonGraph = new LinearLayout(this);
 		buttonGraph.setOrientation(LinearLayout.HORIZONTAL);
 		
 		//Get string extra:
 		Intent intent = getIntent();
		function_string = new String(intent.getStringExtra("function"));
 		
 		//Set up graph:
 		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); //not sure about this
 		
 		float[] xvalues = new float[1000]; //stores x values
 		float[] yvalues = new float[1000]; //stores y values
 		float i = 0; //counter for acquiring values
 		
 //		Toast.makeText(getApplicationContext(), "hi", Toast.LENGTH_SHORT).show();
 //		Toast.makeText(getApplicationContext(), function_string, Toast.LENGTH_SHORT).show();
 
 
 		try {
 			//Set up function as a Calculable:
 			Calculable function_calc = new ExpressionBuilder(function_string).withVariable("x", i).build();
 			
 			//Fill arrays:
 			for(int j = 0; j < yvalues.length; j++)
 			{
 				xvalues[j] = i;
 				yvalues[j] = (float) function_calc.calculate(i);
 				i+=0.01;
 			}
 
 			plot2d graph = new plot2d(this, xvalues, yvalues, 1);
 			
 			buttonGraph.addView(graph, lp);
 			
 			setContentView(buttonGraph);
 		}
 		
 		//These catch blocks should never be tripped
 		catch (UnknownFunctionException e)
 		{}
 		catch (UnparsableExpressionException e)
 		{} 
 		
 	}
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 	}
 	
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 	}
 }
