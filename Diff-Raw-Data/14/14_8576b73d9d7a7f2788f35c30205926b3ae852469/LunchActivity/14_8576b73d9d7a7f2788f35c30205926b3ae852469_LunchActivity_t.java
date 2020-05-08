 package se.chalmers.h_sektionen;
 
 import se.chalmers.h_sektionen.utils.LoadData;
 import se.chalmers.h_sektionen.utils.MenuItems;
 
 import android.os.AsyncTask;
 import android.text.Html;
 import android.widget.TextView;
 /**
  * Activity responsible of the lunch menu view.
  * @Author Robin Tornquist
  * @Copyright (c) 2013 Anders Johansson, Olle Svensson, Robin Tornquist, Rikard Ekbom, Oskar Gustavsson, Jonas Berglund
  * @Licens Apache
  */
 public class LunchActivity extends BaseActivity {
 
 	private AsyncTask<String, String, String> getLunchTask;
 	
 	/**
 	 * On resume. 
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setCurrentView(MenuItems.LUNCH);
 		if (connectedToInternet()) {
 			getFrameLayout().removeAllViews();
 			getFrameLayout().addView(getLayoutInflater().inflate(R.layout.view_lunch, null));
 			
 			getLunchTask = new DownloadLunchRSS().execute();
 			
 		} else {
 		    setErrorView(getString(R.string.INTERNET_CONNECTION_ERROR_MSG));
 		}
 	}
 	
 	/**
 	 * On pause: cancel the AsyncTask that downloads the lunch menu.
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (getLunchTask != null && !getLunchTask.isCancelled()) {
 			getLunchTask.cancel(true);
 		}
 	}
 	
 	/**
 	 * Creates the lunch view.
 	 */
 	private void setUpLunchView(String result){
 		
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
 			stopAnimation();
 			
 			setUpLunchView(result);
 		}
 	}
 }
 
 
 
