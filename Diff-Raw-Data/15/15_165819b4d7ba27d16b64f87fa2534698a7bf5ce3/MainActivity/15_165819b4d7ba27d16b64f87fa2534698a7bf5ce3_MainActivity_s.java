 package echelonfour.quantumdice;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		// getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public void onButtonGoClicked(View view) {
 		new RunRandomLookup().execute();
 	}
 
	public void onRadioCustomClicked(View view) {
 		EditText customField = (EditText) findViewById(R.id.editTextCustom);
		if (customField.isEnabled()) {
			customField.setEnabled(false);
			customField.setFocusable(false);
		} else {
 			customField.setEnabled(true);
			customField.setFocusable(true);
 		}
 	}
 
 	private class RunRandomLookup extends AsyncTask<Void, Void, String> {
 		TextView textResult;
 		int checkId;
 		int customBound;
 
 		@Override
 		protected void onPreExecute() {
 			textResult = (TextView) findViewById(R.id.textViewResult);
 			checkId = ((RadioGroup) findViewById(R.id.radioGroup))
 					.getCheckedRadioButtonId();
 			customBound = Integer
 					.parseInt(((EditText) findViewById(R.id.editTextCustom))
 							.getText().toString());
 			textResult.setText(R.string.loading);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			try {
 				switch (checkId) {
 				case R.id.radioDice:
 					return Integer
 							.toString(RandomGrabber.getRandomBound(6) + 1);
 				case R.id.radioCustom:
 					return Integer.toString(RandomGrabber
 							.getRandomBound(customBound));
 				default:
 				case R.id.radioCoin:
 					if (RandomGrabber.getRandomBound(2) == 0) {
 						return getString(R.string.heads);
 					} else {
 						return getString(R.string.tails);
 					}
 				}
 			} catch (QuantumException e) {
 				return getString(R.string.default_result);
 			}
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			textResult.setText(result);
 			if (result.equals(getString(R.string.default_result))) {
 				Toast.makeText(getApplicationContext(), R.string.network_issue,
 						Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 }
