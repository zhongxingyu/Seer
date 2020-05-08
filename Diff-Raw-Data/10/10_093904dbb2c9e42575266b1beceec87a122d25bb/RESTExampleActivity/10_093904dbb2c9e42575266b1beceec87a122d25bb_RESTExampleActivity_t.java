 /*
  * 
  */
 package cz.android.monet.restfulclient;
 
 import cz.android.monet.restfulclient.R;
 import cz.android.monet.restfulclient.fragments.OutputFragment;
 import cz.android.monet.restfulclient.interfaces.OnServerResultReturned;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class RESTExampleActivity.
  */
 public class RESTExampleActivity extends FragmentActivity implements
 		OnServerResultReturned {
 
 	/** The Constant TAG. */
 	private static final String TAG = "RESTExampleActivity";
 
 	/**
 	 * Called when the activity is first created.
 	 *
 	 * @param savedInstanceState the saved instance state
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Log.v(TAG, "onCreate");
 		
		Log.v(TAG, "CPU count: " + Runtime.getRuntime().availableProcessors());

		
 		setContentView(R.layout.main);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.default_menu, menu);
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.quit:
 			break;
 		default:
 			// Toast.makeText(this, "Menu Item undefined selected",
 			// Toast.LENGTH_SHORT).show();
 			Log.d(TAG, "Undefined item id.");
 			break;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see cz.android.monet.restfulclient.interfaces.OnServerResultReturned#onResultReturned(java.lang.String)
 	 */
 	public void onResultReturned(String resultMessage) {
 		// The user selected the headline of an article from the
 		// HeadlinesFragment
 		// Do something here to display that article
 
 		OutputFragment outputFrag = (OutputFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.output_fragment);
 
 		if (outputFrag != null) {
 			// If article frag is available, we're in two-pane layout...
 
 			// Call a method in the ArticleFragment to update its content
 			outputFrag.updateResultView(resultMessage);
 		} else {
 			// Otherwise, we're in the one-pane layout and must swap frags...
 
 			// Create fragment and give it an argument for the selected article
 			OutputFragment newFragment = new OutputFragment();
 			Bundle args = new Bundle();
 			args.putString("Message", resultMessage);
 			newFragment.setArguments(args);
 
 			FragmentTransaction transaction = getSupportFragmentManager()
 					.beginTransaction();
 
 			// Replace whatever is in the fragment_container view with this
 			// fragment,
 			// and add the transaction to the back stack so the user can
 			// navigate back
 			transaction.replace(R.id.input_fragment, newFragment);
 			transaction.addToBackStack(null);
 
 			// Commit the transaction
 			transaction.commit();
 		}
 
 		// Get the intent that started this activity
 		Intent intent = getIntent();
 		Uri data = intent.getData();
 
 		if (data != null && intent.getType().equals("text/plain")) {
 			// Create intent to deliver some kind of result data
 			Intent result = new Intent("RESULT_ACTION", Uri.parse("content://"
 					+ resultMessage));
 			setResult(Activity.RESULT_OK, result);
 			finish();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 		// The activity is about to become visible.
 		
 		Log.v(TAG, "onStart");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// The activity has become visible (it is now "resumed").
 		
 		Log.v(TAG, "onResume");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// Another activity is taking focus (this activity is about to be
 		// "paused").
 		
 		Log.v(TAG, "onPause");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		// The activity is no longer visible (it is now "stopped")
 		
 		Log.v(TAG, "onStop");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		// The activity is about to be destroyed.
 		
 		Log.v(TAG, "onDestroy");
 	}
 
 }
