 package echelonfour.quantumdice;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private static final String prefName = "quantumDice";
 	private static final String prefManualReseed = "manualReseed";
 	private static final String prefCustomNumber = "customNumber";
 	private static final String prefSelectedOption = "selectedOption";
 	private boolean manualReseed = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		resetOptionMenu(menu);
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	private void resetOptionMenu(Menu menu) {
 		if (manualReseed) {
 			menu.findItem(R.id.option_reseed).setTitle(
 					R.string.option_reseed_unchecked);
 			menu.findItem(R.id.option_manual_seed).setEnabled(true);
 		} else {
 			menu.findItem(R.id.option_reseed).setTitle(
 					R.string.option_reseed_checked);
 			menu.findItem(R.id.option_manual_seed).setEnabled(false);
 		}
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.option_reseed:
 			manualReseed = !manualReseed;
 			return true;
 		case R.id.option_manual_seed:
 			new RunReseed().execute();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		RandomGrabber.deinitilise();
 		Editor editor = getSharedPreferences(prefName, MODE_PRIVATE).edit();
 		editor.putBoolean(prefManualReseed, manualReseed);
 		editor.putString(prefCustomNumber,
 				((EditText) findViewById(R.id.editTextCustom)).getText()
 						.toString());
 		editor.putInt(prefSelectedOption,
 				((RadioGroup) findViewById(R.id.radioGroup))
 						.getCheckedRadioButtonId());
 		editor.commit();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		RandomGrabber.initilise();
 		SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);
 		manualReseed = prefs.getBoolean(prefManualReseed, false);
 		((EditText) findViewById(R.id.editTextCustom)).setText(prefs.getString(
 				prefCustomNumber, "10"));
 		((RadioGroup) findViewById(R.id.radioGroup)).check(prefs.getInt(
 				prefSelectedOption, R.id.radioCoin));
 		if (manualReseed) {
 			new RunReseed().execute();
 		}
		onRadioClicked(null);
 	}
 
 	public void onButtonGoClicked(View view) {
 		new RunRandomLookup().execute();
 	}
 
 	public void onRadioClicked(View view) {
 		EditText customField = (EditText) findViewById(R.id.editTextCustom);
 		if (((RadioGroup) findViewById(R.id.radioGroup))
 				.getCheckedRadioButtonId() == R.id.radioCustom) {
 			customField.setEnabled(true);
 		} else {
 			customField.setEnabled(false);
 		}
 
 	}
 
 	private void showNetworkError() {
 		Toast.makeText(getApplicationContext(), R.string.network_issue,
 				Toast.LENGTH_LONG).show();
 	}
 
 	private class RunRandomLookup extends AsyncTask<Void, Void, Boolean> {
 		TextView textResult;
 
 		@Override
 		protected void onPreExecute() {
 			textResult = (TextView) findViewById(R.id.textViewResult);
 			textResult.setText(R.string.loading);
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			try {
 				if (!manualReseed) {
 					RandomGrabber.reseed();
 				}
 
 			} catch (QuantumException e) {
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			int checkId = ((RadioGroup) findViewById(R.id.radioGroup))
 					.getCheckedRadioButtonId();
			int customBound;
			try {
			customBound = Integer
 					.parseInt(((EditText) findViewById(R.id.editTextCustom))
 							.getText().toString());
			} catch (NumberFormatException e) {
				((EditText) findViewById(R.id.editTextCustom)).setText("10");
				customBound = 10;
			}
 			switch (checkId) {
 			case R.id.radioDice:
 				textResult.setText(Integer.toString(RandomGrabber
 						.getRandomBound(6) + 1));
 				break;
 			case R.id.radioCustom:
 				textResult.setText(Integer.toString(RandomGrabber
 						.getRandomBound(customBound)));
 				break;
 			default:
 			case R.id.radioCoin:
 				if (RandomGrabber.getRandomBound(2) == 0) {
 					textResult.setText(getString(R.string.heads));
 				} else {
 					textResult.setText(getString(R.string.tails));
 				}
 				break;
 			}
 
 			if (result) {
 				showNetworkError();
 			}
 		}
 	}
 
 	private class RunReseed extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			try {
 				RandomGrabber.reseed();
 			} catch (QuantumException e) {
 				showNetworkError();
 			}
 			return null;
 		}
 
 	}
 
 }
