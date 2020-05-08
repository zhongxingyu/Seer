 package ch.hsr.bieridee.android.activities;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import ch.hsr.bieridee.android.R;
 import ch.hsr.bieridee.android.config.Res;
 import ch.hsr.bieridee.android.http.AuthJsonHttp;
 import ch.hsr.bieridee.android.http.HttpHelper;
 import ch.hsr.bieridee.android.utils.ErrorHelper;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 
 /**
  * Activity that initiates a barcode scan.
  */
 public class BarcodeScanActivity extends Activity {
 
 	private static final String LOG_TAG = BarcodeScanActivity.class.getName();
 	private ProgressDialog progressDialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.barcodescan);
 
 		findViewById(R.id_barcodescan.button).setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				BarcodeScanActivity.this.readBarcode();
 			}
 		});
 
		this.readBarcode();
 	}
 
 	/**
 	 * Start barcode scanner.
 	 */
 	private void readBarcode() {
		final IntentIntegrator integrator = new IntentIntegrator(BarcodeScanActivity.this);
 		integrator.initiateScan();
 	}
 
 	/**
 	 * Process the return value from the barcode scanner.
 	 *
	 * @param requestCode Request code
	 * @param resultCode Result code
	 * @param intent Intent
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 
 		// Error condition, should not happen
 		if (scanResult == null) {
			ErrorHelper.onError(getString(R.string.barcodescan_error), this);
 
 			// Process returned barcode
 		} else {
 			final String barcode = scanResult.getContents();
 			if (barcode == null) {
				ErrorHelper.onError(getString(R.string.barcodescan_failed), this);
 			} else {
 				final String format = scanResult.getFormatName();
 				Log.d(LOG_TAG, String.format("Received barcode %s with format %s.", barcode, format));
 				new FindBeer().execute(barcode);
 			}
 		}
 	}
 
 	/**
 	 * Async task to get beer detail from server and update UI.
 	 */
 	private class FindBeer extends AsyncTask<String, Void, JSONObject> {
 
 		@Override
 		protected void onPreExecute() {
 			BarcodeScanActivity.this.progressDialog = ProgressDialog.show(BarcodeScanActivity.this, getString(R.string.pleaseWait), getString(R.string.loadingData), true);
 		}
 
 		@Override
 		protected JSONObject doInBackground(String... barcodes) {
 			final String barcode = barcodes[0];
 
			final String uri = Res.getURIwithGETParams(Res.BEER_COLLECTION, Res.BEER_FILTER_PARAMETER_BARCODE, barcode);
 
 			HttpResponse response = null;
 			final HttpHelper httpHelper = AuthJsonHttp.create();
 			try {
 				response = httpHelper.get(uri);
 			} catch (IOException e) {
 				Log.d(LOG_TAG, e.getMessage(), e);
 				ErrorHelper.onError(getString(R.string.connectionError), BarcodeScanActivity.this);
 			}
 
 			if (response != null) {
 				final int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode == HttpStatus.SC_OK) {
 					JSONArray beerList = null;
 
 					try {
 						final String responseText = new BasicResponseHandler().handleResponse(response);
 						beerList = new JSONArray(responseText);
 					} catch (IOException e) {
 						Log.d(LOG_TAG, e.getMessage(), e);
 						ErrorHelper.onError(getString(R.string.connectionError), BarcodeScanActivity.this);
 					} catch (JSONException e) {
 						Log.d(LOG_TAG, e.getMessage(), e);
 						ErrorHelper.onError(getString(R.string.malformedData), BarcodeScanActivity.this);
 					}
 
 					if (beerList.length() > 0) {
 						try {
 							return (JSONObject) beerList.get(0);
 						} catch (JSONException e) {
 							Log.d(LOG_TAG, e.getMessage(), e);
 							ErrorHelper.onError(getString(R.string.malformedData), BarcodeScanActivity.this);
 						}
 					} else {
 						Log.d(LOG_TAG, "Beer with barcode " + barcode + " not found.");
 						ErrorHelper.onError(String.format(getString(R.string.barcodescan_not_found), barcode), BarcodeScanActivity.this);
 					}
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(JSONObject jsonObject) {
 			if (jsonObject != null) {
 				Long beerId = null;
 				try {
 					beerId = jsonObject.getLong("id");
 				} catch (JSONException e) {
 					Log.d(LOG_TAG, e.getMessage(), e);
 					ErrorHelper.onError(getString(R.string.malformedData), BarcodeScanActivity.this);
 				}
 				if (beerId != null) {
 					Log.d(LOG_TAG, "Found beer with ID " + beerId);
 					final Intent intent = new Intent(getBaseContext(), BeerDetailActivity.class);
 					intent.putExtra(BeerDetailActivity.EXTRA_BEER_ID, beerId);
 					startActivity(intent);
 				} else {
 					Log.d(LOG_TAG, "Could not find beer ID.");
 					ErrorHelper.onError(getString(R.string.barcodescan_error), BarcodeScanActivity.this);
 				}
 			}
 			BarcodeScanActivity.this.progressDialog.dismiss();
 		}
 	}
 }
