 package org.walley.strokeme;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import jama.Matrix;
 import jkalman.JKalman;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class StrokeMeActivity extends Activity implements OnClickListener {
     // For logging and debugging purposes
     private static final String TAG = "StrokeMeActivity";
     
    private static final double resetPeriod = 10.0;
     private static final int maxHistory = 3;
 	
     private LinearLayout layoutView;
 	private TextView rateView;
 	private ListView historyView;
 	
 	private JKalman kalman;
 	
 	private Matrix state; // state [x, dx]        
     private Matrix measurement; // measurement [z]
 	
     private int numStrokes; // Number of strokes recorded in current sequence
 	
 	double strokeRate; // The current estimate of the stroke rate
 	
 	private List<HistoryItem> history; // Array holding history of most recent stroke rates
 	private ArrayAdapter<HistoryItem> historyAdapter;
 	
 	private long lastTime; // time of last stroke (in milliseconds)
 	private Date lastDate; // time of last stroke (used for history)
 	
 	private boolean displayHistory;
 
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         rateView = (TextView)findViewById(R.id.rate);
         
         layoutView = (LinearLayout)findViewById(R.id.screen);
 
         layoutView.setOnClickListener(this);
         
         historyView = (ListView)findViewById(R.id.history);
         history = new ArrayList<HistoryItem>();
                 
         // Define custom ArrayAdapter and override getView
         historyAdapter = new ArrayAdapter<HistoryItem>(this, R.layout.history_item, history) {
         	@Override
         	public View getView(int position, View convertView, ViewGroup parent) {
         		
         		View row;
          
         		if (null == convertView) {
         			LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         			row = vi.inflate(R.layout.history_item, null);
         		} else {
         			row = convertView;
         		}
          
         		TextView rateHistoryView = (TextView) row.findViewById(R.id.rateHistory);
         		rateHistoryView.setText(getItem(position).getRate());
         		
         		TextView timeHistoryView = (TextView) row.findViewById(R.id.timeHistory);
         		timeHistoryView.setText(getItem(position).getTime());
          
         		return row;
         	}
         };
 
         historyView.setAdapter(historyAdapter);
                 
         displayHistory = true;
         
         numStrokes = 0; // Just started. No strokes recorded yet
         
         lastTime = System.currentTimeMillis(); // Need a valid value but this is never used to estimate stroke rate
 
         // Instantiate Kalman filter implementation
         try {
 			kalman = new JKalman(2, 1);
 			
             kalman.setProcess_noise_cov(kalman.getProcess_noise_cov().identity());
             kalman.setMeasurement_noise_cov(kalman.getMeasurement_noise_cov().identity());	
 		} catch (Exception e) {
             Log.e(TAG, e.getMessage());
 		}
         
         state = new Matrix(2, 1); // state [x, dx]        
         measurement = new Matrix(1, 1); // measurement [z]
     }
     
     private class HistoryItem {
     	private String rate;
     	private String time;
     	
     	public HistoryItem(String rate, String time) {
     		this.setRate(rate);
     		this.setTime(time);
     	}
 
 		public String getTime() {
 			return time;
 		}
 
 		public void setTime(String time) {
 			this.time = time;
 		}
 
 		public String getRate() {
 			return rate;
 		}
 
 		public void setRate(String rate) {
 			this.rate = rate;
 		}
     }
     
     // Tap received
     public void onClick(View v) {   	
     	numStrokes++;
     	
     	// Calculate time since last stroke (in seconds)
     	long currentTime = System.currentTimeMillis();
     	double dt = (double)(currentTime-lastTime)/1000.0;
     	
     	// If sufficient time has passed we treat this stroke as the first in a new sequence
     	if (dt>resetPeriod) {
     		    		
     		// No point in storing in history if numStrokes equals 2
     		if (numStrokes>2) {
 	    		// Move this value to history    		
 	    		DateFormat formatter = new SimpleDateFormat("h:mm a");
 	
 	    		// Really should be a bounded Queue but this works
 	    		historyAdapter.insert(new HistoryItem(String.format("%2.0f spm", strokeRate), String.format("%s", formatter.format(lastDate))), 0); // Insert at beginning of list
 	    		if (historyAdapter.getCount()>maxHistory) {
 	    			historyAdapter.remove(historyAdapter.getItem(maxHistory-1)); // Remove oldest history item if over limit
 	    		}
     		}
     		
     		numStrokes = 1;
     	}
     	
     	switch (numStrokes) {
     	case 1:
     		state.set(0, 0, 0.0);
     		state.set(1, 0, Double.NaN);
     		Log.i(TAG, "dx:" + state.get(1, 0));
     		break;    		
     	case 2:
     		state.set(0, 0, 1.0);
     		state.set(1, 0, 1.0/dt);
     		// Seed filter with initial state estimate. Assumes first two measurements are unbiased
     		kalman.setState_post(state);
     		Log.i(TAG, "dx:" + state.get(1, 0));
     		break;
     	default:
 			// Set up filter
             kalman.setTransition_matrix(new Matrix(new double[][] { {1, dt}, {0, 1} }));
             
             kalman.Predict();
             
             measurement.set(0, 0, (double)numStrokes-1.0);
             
             state = kalman.Correct(measurement);
             
             Log.i(TAG, "dx:" + state.get(1, 0));
     	}
     	
     	strokeRate = 60.0*state.get(1, 0);
     	    	
     	if (Double.isNaN(strokeRate)) {
     		rateView.setText("..."); 
     	} else {
     		rateView.setText(String.format("%2.0f", strokeRate)); 
     	}
     	
     	lastTime = currentTime; 	
     	lastDate = Calendar.getInstance().getTime();
     	
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.toggleHistory:
         	displayHistory = !displayHistory;
         	if (displayHistory) {
             	historyView.setVisibility(View.VISIBLE);
         	} else {
             	historyView.setVisibility(View.GONE);
         	}
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }    
 }
