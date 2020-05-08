 package cs585_hw3.team33.browse;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import cs585_hw3.team33.MainActivity;
 import cs585_hw3.team33.R;
 import cs585_hw3.team33.browse.list.Result;
 import cs585_hw3.team33.browse.list.ResultAdapter;
 import cs585_hw3.team33.browse.map.ShowResultsMapActivity;
 import cs585_hw3.team33.lib.ProgressRunnable;
 
 public class BrowseActivity extends ListActivity {
 	ArrayList<Result> result_list = null;
 	ResultAdapter result_adapt;
 	
 	ProgressDialog viewResultsProgress = null;
 
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.browse);
         
         result_list = new ArrayList<Result>();
         result_adapt = new ResultAdapter(this, R.layout.browse_row, result_list);
         setListAdapter(result_adapt);
        
         ((Button)findViewById(R.id.QueryButton)).setOnClickListener( new QueryListener(this) );
         ((Button)findViewById(R.id.MapButton)).setOnClickListener( new MapListener(this) );
 	}
 	
 	public void executeQuery(ProgressRunnable pr) {
 		String kStr = ((EditText)findViewById(R.id.kTxt)).getText().toString();
 		
 		String keywords = ((EditText)findViewById(R.id.keywordTxt)).getText().toString();
 		int k = (kStr.equals("")? Integer.MAX_VALUE : Integer.parseInt(kStr));
 		int x = 0, y = 0;
 		
 		MainActivity m = ((MainActivity)this.getParent());
 		
 		if (m.current_loc != null) {
 			x = m.current_loc.getLatitudeE6();
 			y = m.current_loc.getLongitudeE6();
 		} else {
 			pr.reportToast("No location found; using 0,0.");
 		}
 		
 		if (m.dh.isOpen()) 
 			m.dh.query(x,y,keywords, k, result_list);
 		else
 			pr.reportAlert("Couldn't execute query; open the database first." );
 			
         try{
 			 Thread.sleep(500);
 		 } catch (Exception e) { 
          }
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		ArrayList<Result> one_item = new ArrayList<Result>();
 		one_item.add( result_list.get(position) );
 		
 		Intent mapIntent = new Intent(this, ShowResultsMapActivity.class);
 		mapIntent.putExtra("results",one_item);
 		startActivity(mapIntent);
 
 		super.onListItemClick(l, v, position, id);
 	}
 	
 	class QueryListener implements OnClickListener {
 		Activity parent;
 		QueryListener(Activity a) {
 			parent = a;
 		}
 		public void onClick(View v) {
 			ProgressRunnable getResults  
 			= new ProgressRunnable("Please wait...", "Finding blogs...") {
 				@Override
 				public void onGo() {
 					executeQuery(this);
 				}
 				public void onEnd() {
 		            result_adapt.notifyDataSetChanged();
 		            ((EditText)findViewById(R.id.keywordTxt)).setText("");
 		    		((EditText)findViewById(R.id.kTxt)).setText("");
 		    		
 				}
 			};
 			getResults.startThread(parent,"BackgroundQuery");
 		}
 	};
 	class MapListener implements OnClickListener {
 		Activity parent;
 		MapListener(Activity a) {
 			parent = a;
 		}
 		public void onClick(View v) {			
 			String keywords = ((EditText)findViewById(R.id.keywordTxt)).getText().toString();
 			String kStr = ((EditText)findViewById(R.id.kTxt)).getText().toString();
 			
 			if (result_list.size() == 0 || !keywords.equals("") || !kStr.equals("")) {					
 				ProgressRunnable getResults  
 				= new ProgressRunnable("Please wait...", "Finding blogs...") {
 					@Override
 					public void onGo() {
 						executeQuery(this);
 					}
 					public void onEnd() {
 			            result_adapt.notifyDataSetChanged();
 			            ((EditText)findViewById(R.id.keywordTxt)).setText("");
 			    		((EditText)findViewById(R.id.kTxt)).setText("");
 			    		
 			    		makeAlertToast();
 		    			if (result_list.size() == 0)
 		    				reportToast("Nothing to display; no results returned.");
 
 		    			Intent mapIntent = new Intent(parent, ShowResultsMapActivity.class);
 		    			mapIntent.putExtra("results",result_list);
 		    			startActivity(mapIntent);
 					}
 				};
 				getResults.startThread(parent,"BackgroundQuery");
 			} else {
 				Intent mapIntent = new Intent(parent, ShowResultsMapActivity.class);
 				mapIntent.putExtra("results",result_list);
 				startActivity(mapIntent);
 				
 			}
 		}
 	};
 	
 }
