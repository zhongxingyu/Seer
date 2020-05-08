 
 package uk.co.jarofgreen.phonenumbersindnsandroid;
 
 import java.util.List;
 
 import uk.co.jarofgreen.phonenumbersindnsandroid.lib.Query;
 import uk.co.jarofgreen.phonenumbersindnsandroid.lib.Result;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.app.Activity;
 import android.content.Intent;
 
 
 public class ResultsActivity extends Activity {
 
 	String url;
 	List<Result> results;
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_results);
         
         Bundle extras = getIntent().getExtras();
         url = extras.getString("url");
         
         setTitle("Results for "+url);
         
         new ResultsActivityWorker().execute(url);
         
     }
     
     
     
     private class ResultsActivityWorker extends AsyncTask<String, Integer, Boolean> {
     	
    	
     	
 		@Override
 		protected Boolean doInBackground(String... params) {
 
 			try {
 				Query query = new Query(params[0]);
 				results = query.execute();
 				
 				return true;
 			} catch (Exception e) {
				Log.d("AGH",e.toString());
 				return false;
 			}
 		}
 		
 		
         protected void onPostExecute(Boolean taskResult) {   	
         	if (taskResult) {
         		
         		LinearLayout parent = (LinearLayout)findViewById(R.id.results_container);
 
         		for (int i=0;i<results.size();i++) {
         			Result result = results.get(i);
         		        			
         			View child =  getLayoutInflater().inflate(R.layout.result,null);
         			
         			TextView tv1 = (TextView)child.findViewById(R.id.description);
         			tv1.setText(result.getDescription());
         			
         			TextView tv2 = (TextView)child.findViewById(R.id.number);
         			tv2.setText("+"+result.getCountryCode()+" "+result.getNumber());
        			
         			Log.d("PARSEDDATA",result.getCountryCode());
         			Log.d("PARSEDDATA",result.getNumber());
         			Log.d("PARSEDDATA",result.getDescription());
 
         			// Passing index in this way to be consumed in click handler. Surprised no side effects ...
                     child.setId(i);
                     
                     child.setClickable(true);
                     child.setOnClickListener(new View.OnClickListener() {
                             public void onClick(View v) {
                             	Result result = results.get(v.getId());
                             	String url = "tel:+"+result.getCountryCode()+result.getNumber();
                                 Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                                 startActivity(intent);
                             }
                     });     			
         			
         			parent.addView(child);
         			
         		}		
         	}
         }
 		
 		
     }
 
 
 
 
     
 }
