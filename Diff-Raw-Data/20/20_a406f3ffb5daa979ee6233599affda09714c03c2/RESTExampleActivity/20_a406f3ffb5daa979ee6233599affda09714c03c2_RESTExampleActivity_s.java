 package cz.android.monet.restexample;
 
 import cz.android.monet.restexample.fragments.OutputFragment;
 import cz.android.monet.restexample.interfaces.OnServerResultReturned;
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
import android.widget.Toast;
 
 public class RESTExampleActivity extends FragmentActivity implements
 		OnServerResultReturned {
 
 	private static final String TAG = "RESTExampleActivity";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// receiveData = (TextView) findViewById(R.id.textReceiveData);
 
 		// ThreadPolicy tp = ThreadPolicy.LAX;
 		// StrictMode.setThreadPolicy(tp);
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.default_menu, menu);
 		return true;
 	}
 
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
 
 	@Override
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
 
		if (getIntent().getType().equals("text/plain")) {
 			// Create intent to deliver some kind of result data
 			Intent result = new Intent("RESULT_ACTION", Uri.parse("content://"
 					+ resultMessage));
 			setResult(Activity.RESULT_OK, result);
 			finish();
 		}

 	}
 
 }
