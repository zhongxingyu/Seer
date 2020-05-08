 package nl.miraclethings.parkeerbeter;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 
 public class StartupActivity extends Activity {
     private ProgressDialog pd;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.startup);
           
         this.pd = ProgressDialog.show(this, 
         		getString(R.string.startup_busy), 
         		getString(R.string.startup_login), 
         		true, false);
         new CheckLogonTask().execute();
     }
 
 	public void loaded(int result) {
 		this.pd.dismiss();
 		
 		Intent intent = null;
 		
 		if (result != 0) {  
 			intent = new Intent("nl.miraclethings.parkeerbeter.LOGIN");
 			Log.v(StartupActivity.class.toString(), "Result: " + result);
 			intent.putExtra("reason", result);
 		} else {
 			intent = new Intent("nl.miraclethings.parkeerbeter.MAIN");
 			if (getIntent().getExtras() != null) {
 				intent.putExtra("stop", getIntent().getExtras().getString("stop"));
 			}
 		}
		finish();
 		startActivity(intent);
 	}
    
     private class CheckLogonTask extends AsyncTask<String, Void, Integer> {
 
 		protected Integer doInBackground(String... params) {
 			// check logon status
 			ParkeerAPI api = new ParkeerAPI(getSharedPreferences("ParkeerBeter", MODE_PRIVATE));
 			try { Thread.sleep(500); } catch (Throwable e) {}
 			return new Integer(api.checkLogin());
 		}
 
         protected void onPostExecute(Integer result) {
         	StartupActivity.this.loaded(result.intValue());
         }
     }		
 }
 
