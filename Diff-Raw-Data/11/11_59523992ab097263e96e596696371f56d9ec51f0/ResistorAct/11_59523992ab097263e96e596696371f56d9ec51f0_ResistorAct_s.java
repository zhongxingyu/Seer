 package kamkowarrier.collab.resistorreader;
 
 import org.taptwo.android.widget.ViewFlow;
 import org.taptwo.android.widget.ViewFlow.ViewSwitchListener;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class ResistorAct extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_resistor);
 		
 	    Integer[] colors = { R.color.black, R.color.brown, R.color.red, 
 	    		R.color.orange, R.color.yellow, R.color.green,
 	    		R.color.blue, R.color.violet, R.color.gray, R.color.white  };
 	    
 	    Integer[] tolerance = { R.color.violet, R.color.blue, 
 	    		R.color.green, R.color.brown, R.color.red, R.color.gold, R.color.silver };
 	    
	    final int[] band_vals = {4,6,7,1};
	    
 	    
 	    TextView symbols = (TextView) findViewById(R.id.plus_minus);
 	    symbols.measure(
 	            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
 	            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
 	    int width = symbols.getMeasuredWidth();
 	    symbols.setText(R.string.plusminus);
 	    symbols.setTextSize(TypedValue.COMPLEX_UNIT_PX, width + 5);
 	    
 		ViewFlow band1_vf = (ViewFlow) findViewById(R.id.band1);
 		ViewFlow band2_vf = (ViewFlow) findViewById(R.id.band2);
 		ViewFlow multiplier_vf = (ViewFlow) findViewById(R.id.multiplier);
 		ViewFlow tolerance_vf = (ViewFlow) findViewById(R.id.tolerance);
 		
 		CircularAdapter band1_ca = new CircularAdapter(this, colors);
 		CircularAdapter band2_ca = new CircularAdapter(this, colors);
 		CircularAdapter multiplier_ca = new CircularAdapter(this, colors);
 		CircularAdapter tolerance_ca = new CircularAdapter(this, tolerance);
 		
 		band1_vf.setAdapter(band1_ca, band1_ca.MIDDLE + 4);
 		band2_vf.setAdapter(band2_ca, band2_ca.MIDDLE + 6);
 		multiplier_vf.setAdapter(multiplier_ca, multiplier_ca.MIDDLE + 7);
 		tolerance_vf.setAdapter(tolerance_ca, tolerance_ca.MIDDLE + 1);
 		
 		Calculator calc = new Calculator();
 		TextView output = (TextView) findViewById(R.id.output_value);
 		String out = calc.calculate(band_vals[0], band_vals[1], band_vals[2], band_vals[3]);
 		output.setText(out);
 		
 		band1_vf.setOnViewSwitchListener(new ViewSwitchListener() {
 		    public void onSwitched(View v, int position) {
 		    	Calculator calc = new Calculator();
 		    	TextView output = (TextView) findViewById(R.id.output_value);
 		    	String out = calc.calculate(((Integer)v.getTag()).intValue(), band_vals[1], 
 		    			band_vals[2], band_vals[3]);
 		    	output.setText(out);
 		    	band_vals[0] = ((Integer)v.getTag()).intValue();
 		    }
 		});
 		
 		band2_vf.setOnViewSwitchListener(new ViewSwitchListener() {
 		    public void onSwitched(View v, int position) {
 		    	Calculator calc = new Calculator();
 		    	TextView output = (TextView) findViewById(R.id.output_value);
 		    	String out = calc.calculate(band_vals[0], ((Integer)v.getTag()).intValue(), 
 		    			band_vals[2], band_vals[3]);
 		    	output.setText(out);
 		    	band_vals[1] = ((Integer)v.getTag()).intValue();
 		    }
 		});
 		
 		multiplier_vf.setOnViewSwitchListener(new ViewSwitchListener() {
 		    public void onSwitched(View v, int position) {
 		    	Calculator calc = new Calculator();
 		    	TextView output = (TextView) findViewById(R.id.output_value);
 		    	String out = calc.calculate(band_vals[0], band_vals[1], 
 		    			((Integer)v.getTag()).intValue(), band_vals[3]);
 		    	output.setText(out);
 		    	band_vals[2] = ((Integer)v.getTag()).intValue();
 		    }
 		});
 		
 		tolerance_vf.setOnViewSwitchListener(new ViewSwitchListener() {
 		    public void onSwitched(View v, int position) {
 		    	Calculator calc = new Calculator();
 		    	EditText output = (EditText) findViewById(R.id.tolerance_output);
 		    	String out = calc.calculate(band_vals[0], band_vals[1], band_vals[2], ((Integer)v.getTag()).intValue());
 		    	out = Double.toString(calc.tol);
 		    	output.setText(out);
 		    	band_vals[3] = ((Integer)v.getTag()).intValue();
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
