 package se.chalmers.h_sektionen;
 
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import se.chalmers.h_sektionen.adapters.ContactCardArrayAdapter;
 import se.chalmers.h_sektionen.containers.InfoContainer;
 import se.chalmers.h_sektionen.utils.LoadData;
 import se.chalmers.h_sektionen.utils.MenuItems;
 
 /**
  * InfoActivity takes care about the info view
  */
 public class InfoActivity extends BaseActivity {
 	
 	/**
 	 * Sets up the view, via the GetInfoTask.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstance) {
 		super.onCreate(savedInstance);
		if(!connectedToInternet()) {
			setErrorView(getString(R.string.INTERNET_CONNECTION_ERROR_MSG));
		} else {
			new GetInfoTask().execute();
		}
 	}
 	
 	/**
 	 * Sets the static "currentView" variable in the super class, BaseActivity.
 	 * The method also start the AsyncTask that fetches the information data.
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setCurrentView(MenuItems.INFO);
 	}
 	
 	
 	/**
 	 * GetInfoTask loads the info data and sets up the view.
 	 */
 	private class GetInfoTask extends AsyncTask<String, String, InfoContainer> {
 
 		/**
 		 * Runs super method and starts the load animation.
 		 */
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			
 			runLoadAnimation();
 		}
 		
 		/**
		 * Uses the LoadData class to download information and then returns it like
		 * an InfoContainer object
		 * @return null if the parsing did not succeed, else an InfoContainer object
 		 */
 		@Override
 		protected InfoContainer doInBackground(String... params) {
			return LoadData.loadInfo(InfoActivity.this);
 		}
 		
 		/**
 		 * runs setUpView()
 		 * @param container An InfoContainer object with info to be showed.
 		 */
 		@Override
 		protected void onPostExecute(InfoContainer container) {
 			super.onPostExecute(container);
 			setUpView(container);
 		}
 		
 	}
 	
 	/**
 	 * Sets up the view with data from doInBackground method.
 	 * If doInBackground returns null, an error view is going to show up.
 	 * @param container An InfoContainer object with info to be showed.
 	 */
 	private void setUpView(InfoContainer container) {
 		// If there was no error (connection or JSON string error), set up the view.
 		if (container != null) {
 			getFrameLayout().removeAllViews();
 			getFrameLayout().addView(getLayoutInflater().inflate(R.layout.view_info, null));
 
 			ListView contactListView = (ListView) findViewById(R.id.contact_info_list);
 			contactListView.setCacheColorHint(Color.WHITE);
 
 			LinearLayout linksLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.info_links, null);
 			TextView linksTextView = (TextView)linksLayout.findViewById(R.id.links);
 			TextView openingHoursTextView = (TextView)linksLayout.findViewById(R.id.opening_hours);
 
 			linksTextView.setMovementMethod(LinkMovementMethod.getInstance());
 			linksTextView.setText(Html.fromHtml(container.getHtmlLinks()));
 
 			openingHoursTextView.setText(Html.fromHtml(container.getOpeningHoursString()));
 			contactListView.addHeaderView(linksLayout);
 			contactListView.setAdapter(new ContactCardArrayAdapter(this, R.layout.contact_list_item, container.getContactCards()));
 		} else {
 			setErrorView(getString(R.string.INFO_ERROR));
 		}
 	}
 	
 }
