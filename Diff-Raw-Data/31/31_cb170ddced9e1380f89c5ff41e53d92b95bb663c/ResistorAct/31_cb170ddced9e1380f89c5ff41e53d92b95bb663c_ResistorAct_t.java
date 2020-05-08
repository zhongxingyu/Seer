 package kamkowarrier.collab.resistorreader;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.view.*;
 import android.view.View.*;
 import android.widget.*;
 
 public class ResistorAct extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_resistor);
 		
 			// Setting output elements
 			final EditText valueOut = (EditText) findViewById(R.id.output_value);
 			final EditText tolOut = (EditText) findViewById(R.id.tolerance_output);
 			final TextView lower = (TextView) findViewById(R.id.lower_bound);
 			final TextView upper = (TextView) findViewById(R.id.upper_bound);
 			final TextView ohm = (TextView) findViewById(R.id.ohm);
 			final TextView percent = (TextView) findViewById(R.id.percent);
 			
 			// Initializing Calculator and setting outputs
 			final Calculator calc = new Calculator();
 			calc.setOutputViews(valueOut, tolOut, lower, upper);
 			
 			// Setting input elements
 			final ResistorView resistorView = (ResistorView) findViewById(R.id.resistor_view);
 			final ListView selectLV = (ListView) findViewById(R.id.LV_bands);
 			
 			// Initializing and assigning ColorSelectionAdapter
 			final ColorSelectionAdapter selectAdapter = new ColorSelectionAdapter(ResistorAct.this, 
 					R.layout.textview, resistorView, calc);
 			selectLV.setAdapter(selectAdapter);
 			resistorView.setSelector(selectAdapter);
 			resistorView.setCalc(calc);
 			
 			selectLV.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
 				public void onGlobalLayout() { 
 					int selectLVHeight = selectLV.getHeight();
 					selectAdapter.setParams(selectLVHeight);
 				}
 			});
 			
 			// Initial calculate
 			resistorView.calculate();
 			
 			// Listener for EditText boxes
 			//Need to add error checking/ handling
 			//TextReader doesn't work with 5 band resistors
 			//CALCULATOR IS WRONG ARRRRGGH (try salmon black orange, off by factor of ten)
 			
 			// The above is to set the X for error checking
 			final SpannableString redX = new SpannableString("X");
 			redX.setSpan(new ForegroundColorSpan(Color.RED), 0, 1, 0);
 			
 			//element at index 0 is for value, 1 for tolerance
 			final String[] boxVals = {valueOut.getText().toString(), tolOut.getText().toString()};
 			
 			valueOut.setOnKeyListener(new OnKeyListener() {
 				public boolean onKey(View view, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
 						switch(keyCode) {
 							case KeyEvent.KEYCODE_ENTER:
 		   						TextReader reader = new TextReader();
 		   						if (!reader.isValidString(valueOut.getText().toString())) {
 		   							ohm.setText(redX);
 		   							break;
 		   						}
 	    						  reader.read(valueOut.getText().toString());
         						  valueOut.setText(reader.realVal);
         						  boxVals[0] = valueOut.getText().toString();
         						  //BAD! This needs to be cleaned up
 							  	  int original = resistorView.activeBandNum;
 								  for (int i = 0; i < 3; i++) { //replace 3 with variable for length
 			        			    resistorView.activeBandNum = i;
 			        			    ColorBand c = new ColorBand(resistorView.getContext());
 			        			    ColorBand.ValBand valB = c.new ValBand(resistorView.getContext());
 			        			    int val = valB.valueToColor(reader.band[i]);
 					    			resistorView.updateWithoutCalc(val);
 								  }
 								  resistorView.activeBandNum = original;
         						  return true;
 						    default:
 						    	break;
 						} 
 				  }
 				  return false;	
 			}
 		});
 			tolOut.setOnKeyListener(new OnKeyListener() {
 				public boolean onKey(View view, int keyCode, KeyEvent event) {
 					if (event.getAction() == KeyEvent.ACTION_DOWN) { //Key down? {
 						switch(keyCode) {
 							case KeyEvent.KEYCODE_ENTER:
 								resistorView.activeBandNum = 3; //make this more general!
 								TextReader reader = new TextReader();
								if (!reader.isValidString(tolOut.getText().toString())) {
									percent.setText(redX);
		   							break;
								}
 								double val = Double.valueOf(tolOut.getText().toString());
 								System.out.println(val);
 								val = reader.findClosestVal(val,reader.validTols);
 								tolOut.setText(Double.valueOf(val).toString());
								boxVals[1] = Double.valueOf(val).toString();
 								ColorBand c = new ColorBand(resistorView.getContext());
 			        			ColorBand.TolBand tolB = c.new TolBand(resistorView.getContext());
 			        			int color = tolB.valueToColor(val);
 			        			resistorView.updateWithoutCalc(color);
 			        	        return true;
 							default:
 								break;
 						}
 					}
 					return false;
 				}
 			});
 							
 	 
 	//Touch listener for ohm textView
 	ohm.setOnTouchListener(new OnTouchListener() {
 		public boolean onTouch(View view, MotionEvent event) {
 			switch(event.getAction()) {
 			    case MotionEvent.ACTION_DOWN:
 			    	if (ohm.getText().toString().equals("X")) {
 			    		ohm.setText(getString(R.string.ohm));
 			    		valueOut.setText(boxVals[0]);
 			    	}
 					default:
 						break;
 				}
 			return false;
 		}
 	});
 	//Touch listener for percent TextView
 	percent.setOnTouchListener(new OnTouchListener() {
 		public boolean onTouch(View view, MotionEvent event) {
 			switch(event.getAction()) {
 			    case MotionEvent.ACTION_DOWN:
			    	if (percent.getText().toString().equals("X")) {
			    		percent.setText(getString(R.string.percent));
			    		tolOut.setText(boxVals[1]);
 			    	}
 					default:
 						break;
 				}
 			return false;
 		}
 	});
 	
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.resistor, menu);
 		return true;
 	}
 	
 } 
