 package se.chalmers.h_sektionen;
 
 import se.chalmers.h_sektionen.utils.LoadData;
 import se.chalmers.h_sektionen.utils.MenuItems;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.widget.TextView;
 /**
  * Activity responsible of the lunch menu view.
  */
 public class LunchActivity extends BaseActivity {
 
 	/**
	 * Sets an error view if no Internet connection, else downloads the lunch RSS and shows it.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstance) {
 		super.onCreate(savedInstance);
 		
 		if (connectedToInternet())
 			new DownloadLunchRSS().execute();
 		else 
 		    setErrorView(getString(R.string.INTERNET_CONNECTION_ERROR_MSG));
 	}
 	
 	/**
 	 * On resume.
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setCurrentView(MenuItems.LUNCH);
 	}
 	/**
 	 * Creates the lunch view.
 	 */
 	private void setUpLunchView(String result){
 		getFrameLayout().removeAllViews();
 		getFrameLayout().addView(getLayoutInflater().inflate(R.layout.view_lunch, null));
 		
 		TextView textView = (TextView)findViewById(R.id.LunchTextView);
 		textView.setText(Html.fromHtml(result));
 
 
     	
     }
 	
 	/**
 	 * Thread that starts the download and parsing function
 	 * of the lunch menus.
 	 */
 	public class DownloadLunchRSS extends AsyncTask<String, String, String>{
 		/**
 		 * Runs a loading animation
 		 */
 		@Override
 		protected void onPreExecute(){
 			runTransparentLoadAnimation();
 		}
 		
 		/**
 		 * Starts the download of a web page and returns
 		 * the string value of the data which was parsed. 
 		 */
 		@Override
 		protected String doInBackground(String... string) {
 			
 			return LoadData.loadLunch();
 		}
 		
 		/**
 		 * Receives the result string and sets it in
 		 * the text view in the activity view.
 		 */
 		@Override
 		protected void onPostExecute(String result){
 			setUpLunchView(result);
 		}
 	}
 }
 
 
 
