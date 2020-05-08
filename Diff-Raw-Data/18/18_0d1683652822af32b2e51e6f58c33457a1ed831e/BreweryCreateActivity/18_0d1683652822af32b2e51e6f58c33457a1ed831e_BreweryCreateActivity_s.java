 package ch.hsr.bieridee.android.activities;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 import ch.hsr.bieridee.android.R;
 import ch.hsr.bieridee.android.Validators;
 import ch.hsr.bieridee.android.adapters.BrewerySizeSpinnerAdapter;
 import ch.hsr.bieridee.android.config.Res;
 import ch.hsr.bieridee.android.http.AuthJsonHttp;
 import ch.hsr.bieridee.android.http.HttpHelper;
 
 /**
  * Activity to create a new beer.
  * 
  */
 public class BreweryCreateActivity extends Activity {
 
 	private static final String LOG_TAG = BreweryCreateActivity.class.getName();
 	private BrewerySizeSpinnerAdapter brewerySizeAdapter;
 	private HttpHelper httpHelper;
 	private MultithreadProgressDialog progressDialog;
 	private EditText breweryName;
 	private EditText breweryDescription;
 	private Spinner brewerySizeSpinner;
 	private Button createButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.brewerycreate);
 
 		this.httpHelper = AuthJsonHttp.create();
 		this.progressDialog = new MultithreadProgressDialog(getString(R.string.pleaseWait), getString(R.string.loadingData));
 
 		this.breweryName = (EditText) findViewById(R.id_brewerycreate.breweryName);
 		this.breweryDescription = (EditText) findViewById(R.id_brewerycreate.breweryDescription);
 
 		this.brewerySizeSpinner = (Spinner) findViewById(R.id_brewerycreate.brewerySizeSpinner);
 		this.brewerySizeAdapter = new BrewerySizeSpinnerAdapter(this);
 		this.brewerySizeSpinner.setAdapter(this.brewerySizeAdapter);
 
 		this.createButton = (Button) findViewById(R.id_brewerycreate.createButton);
 		setButtonAction();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		new GetBrewerySizes().execute();
 	}
 
 	private void setButtonAction() {
 		this.createButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View view) {
 				final String breweryName = BreweryCreateActivity.this.breweryName.getText().toString().trim();
 				final String breweryDescription = BreweryCreateActivity.this.breweryDescription.getText().toString().trim();
 				final String brewerySize = BreweryCreateActivity.this.brewerySizeSpinner.getSelectedItem().toString();
 
 				if (Validators.validateNonEmpty(breweryName) && Validators.validateNonEmpty(breweryDescription)) {
 					final JSONObject newBrewery = new JSONObject();
 					try {
 						newBrewery.put("name", breweryName);
 						newBrewery.put("description", breweryDescription);
 						newBrewery.put("size", brewerySize);
 						newBrewery.put("picture", "");
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
					Log.d(LOG_TAG, newBrewery.toString());
 					new BreweryCreateActivity.AddNewBrewery().execute(newBrewery);
 				} else {
					Toast.makeText(BreweryCreateActivity.this, "Bitte alles ausfüllen", Toast.LENGTH_SHORT).show();
 				}
 
 			}
 
 		});
 	}
 
 	/**
 	 * Adds a new beer!
 	 */
 	private class AddNewBrewery extends AsyncTask<JSONObject, Void, JSONObject> {
 
 		@Override
 		protected JSONObject doInBackground(JSONObject... params) {
 			Log.d(LOG_TAG, "doInBackground()");
 
 			final HttpResponse response = BreweryCreateActivity.this.httpHelper.post(Res.getURI(Res.BREWERY_COLLECTION), params[0]);
 
 			if (response != null) {
 				final int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode == HttpStatus.SC_OK) {
 					try {
 						final String responseText = new BasicResponseHandler().handleResponse(response);
 						return new JSONObject(responseText);
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(JSONObject result) {
 			if (result != null) {
				Toast.makeText(BreweryCreateActivity.this, "Brauerei wurde erfolgreich erstellt", Toast.LENGTH_SHORT).show();
 				BreweryCreateActivity.this.breweryName.setText("");
 				// TODO proper "go back" to the previous activity
 				BreweryCreateActivity.this.finish();
 			} else {
				Toast.makeText(BreweryCreateActivity.this, "whoa, fehler!", Toast.LENGTH_SHORT).show();
 			}
 		}
 
 	}
 
 	/**
 	 * Async task to get breweriesSizes from server and update UI.
 	 */
 	private class GetBrewerySizes extends AsyncTask<Void, Void, JSONArray> {
 		@Override
 		protected void onPreExecute() {
 			Log.d(LOG_TAG, "onPreExecute()");
 			BreweryCreateActivity.this.progressDialog.display(BreweryCreateActivity.this, true);
 		}
 
 		@Override
 		protected JSONArray doInBackground(Void... voids) {
 			Log.d(LOG_TAG, "doInBackground()");
 
 			final HttpResponse response = BreweryCreateActivity.this.httpHelper.get(Res.getURI(Res.BREWERYSIZE_COLLECTION));
 
 			if (response != null) {
 				final int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode == HttpStatus.SC_OK) {
 					try {
 						final String responseText = new BasicResponseHandler().handleResponse(response);
 						return new JSONArray(responseText);
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(JSONArray result) {
 			Log.d(LOG_TAG, "onPostExecute()");
 			if (result != null) {
 			}
			BreweryCreateActivity.this.brewerySizeAdapter.updateData(result);
			BreweryCreateActivity.this.brewerySizeAdapter.notifyDataSetChanged();
			BreweryCreateActivity.this.progressDialog.hide();
 		}
 	}
 }
