 package ch.hsr.bieridee.android.activities;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RatingBar;
 import android.widget.RatingBar.OnRatingBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.hsr.bieridee.android.R;
 import ch.hsr.bieridee.android.config.Auth;
 import ch.hsr.bieridee.android.config.Res;
 import ch.hsr.bieridee.android.exceptions.BierIdeeException;
 import ch.hsr.bieridee.android.http.AuthJsonHttp;
 import ch.hsr.bieridee.android.http.HttpHelper;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 
 /**
  * Activity that shows a beer detail.
  */
 public class BeerDetailActivity extends Activity {
 
 	private Button consumptionButton;
 	private TextView name;
 	private TextView brand;
 	private TextView averageRating;
 	private RatingBar ratingBar;
 	private long beerId;
 	private String username;
 
 	private MultithreadProgressDialog progressDialog;
 
 	private static final String LOG_TAG = BeerDetailActivity.class.getName();
 	public static final String EXTRA_BEER_ID = "beerId";
 	private static final int DATA_LOADING_THREAD_COUNT = 2;
 	private HttpHelper httpHelper;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.beerdetail);
 		this.httpHelper = AuthJsonHttp.create();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		// Get brewery ID from intent
 		this.beerId = this.getIntent().getExtras().getLong(EXTRA_BEER_ID);
 		Log.d(LOG_TAG, "onStart() with breweryId " + this.beerId);
 
 		this.username = Auth.getUsername();
 
 		this.consumptionButton = (Button) this.findViewById(R.id_beerdetail.consumptionButton);
 
 		this.name = (TextView) this.findViewById(R.id_beerdetail.beerName);
 		this.brand = (TextView) this.findViewById(R.id_beerdetail.beerBrand);
 		this.averageRating = (TextView) this.findViewById(R.id_beerdetail.beerAverageRating);
 
 		this.ratingBar = (RatingBar) this.findViewById(R.id_beerdetail.ratingBar);
 		this.ratingBar.setStepSize(1); // no half-star-ratings
 
 		this.progressDialog = new MultithreadProgressDialog(getString(R.string.pleaseWait), getString(R.string.loadingData));
 
 		this.setConsumtionButtonAction();
 		this.setRatingBarAction();
 
 		new GetBeerDetail().execute();
 		new GetBeerRating().execute();
 	}
 
 	private void setConsumtionButtonAction() {
 		this.consumptionButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				new TrackConsumption().execute();
 			}
 		});
 	}
 
 	private void setRatingBarAction() {
 		this.ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
 			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
 				if (!fromUser) {
 					return;
 				}
 				new SaveRating().execute(rating);
 			}
 		});
 	}
 
 	/**
 	 * Async task to get beer detail from server and update UI.
 	 */
 	private class GetBeerDetail extends AsyncTask<Void, Void, JSONObject> {
 
 		private boolean showDialog = false;
 
 		public GetBeerDetail() {
 			super();
 		}
 
 		public GetBeerDetail(boolean showDialog) {
 			super();
 			this.showDialog = showDialog;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			Log.d(LOG_TAG, "onPreExecute()");
 			if (this.showDialog) {
 				BeerDetailActivity.this.progressDialog.display(BeerDetailActivity.this, true);
 			}
 		}
 
 		@Override
 		protected JSONObject doInBackground(Void... voids) {
 			final String uri = Res.getURI(Res.BEER_DOCUMENT, Long.toString(BeerDetailActivity.this.beerId));
 			final HttpResponse response = BeerDetailActivity.this.httpHelper.get(uri);
 
 			if (response != null) {
 				final int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode == HttpStatus.SC_OK) {
 					try {
 						final String responseText = new BasicResponseHandler().handleResponse(response);
 						return new JSONObject(responseText);
 					} catch (IOException e) {
 						Log.e(LOG_TAG, "IOException in GetBeerDetail::doInBackground");
 						e.printStackTrace();
 					} catch (JSONException e) {
 						Log.e(LOG_TAG, "JSONException in GetBeerDetail::doInBackground");
 						e.printStackTrace();
 					}
 				} else {
 					Log.e(LOG_TAG, "HTTP Response " + statusCode + " in GetBeerDetail::doInBackground");
 				}
 			}
 			Log.e(LOG_TAG, "HTTP Response was null in GetBeerDetail::doInBackground");
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(JSONObject result) {
 			if (result != null) {
 				try {
 					final String name = result.getString("name");
 					final String brand = result.getString("brand");
 					final String averageRating = result.getString("rating");
 					BeerDetailActivity.this.name.setText(name);
 					BeerDetailActivity.this.brand.setText(brand);
 					BeerDetailActivity.this.averageRating.setText(averageRating);
 				} catch (JSONException e) {
 					Log.e(LOG_TAG, "JSONException in GetBeerDetail::onPostExecute");
 					e.printStackTrace();
 				}
 			} else {
 				Log.w(LOG_TAG, "Result was null in GetBeerDetail::onPostExecute");
 			}
 			if (this.showDialog) {
 				BeerDetailActivity.this.progressDialog.hide();
 			}
 		}
 	}
 
 	/**
 	 * Async task to get beer rating from server and update UI.
 	 */
 	private class GetBeerRating extends AsyncTask<Void, Void, JSONObject> {
 		@Override
 		protected void onPreExecute() {
 			Log.d(LOG_TAG, "onPreExecute()");
 			BeerDetailActivity.this.progressDialog.display(BeerDetailActivity.this, true);
 		}
 
 		@Override
 		protected JSONObject doInBackground(Void... voids) {
 			Log.d(LOG_TAG, "doInBackground()");
 
 			final String uri = Res.getURI(Res.RATING_DOCUMENT, Long.toString(BeerDetailActivity.this.beerId), BeerDetailActivity.this.username);
 			Log.d(LOG_TAG, "GET " + uri);
 			final HttpResponse response = BeerDetailActivity.this.httpHelper.get(uri);
 
 			if (response != null) {
 				final int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode == HttpStatus.SC_OK) {
 					try {
 						final String responseText = new BasicResponseHandler().handleResponse(response);
 						return new JSONObject(responseText);
 					} catch (IOException e) {
 						Log.e(LOG_TAG, "IOException in GetBeerRating::doInBackground");
 						e.printStackTrace();
 					} catch (JSONException e) {
 						Toast.makeText(BeerDetailActivity.this, getString(R.string.beerdetail_fail_loadRating), Toast.LENGTH_LONG).show();
 						e.printStackTrace();
 					}
 				} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
 					Log.i(LOG_TAG, "No current rating found.");
 					return null;
 				} else {
 					Log.e(LOG_TAG, "HTTP Response " + statusCode + " in GetBeerRating::doInBackground");
 				}
 			}
 			Log.e(LOG_TAG, "HTTP Response was null in GetBeerDetail::doInBackground");
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(JSONObject result) {
 			Log.d(LOG_TAG, "onPostExecute()");
 			if (result != null) {
 				try {
 					final int currentRating = result.getInt("rating");
 					BeerDetailActivity.this.ratingBar.setRating(currentRating);
 				} catch (JSONException e) {
 					Log.e(LOG_TAG, "JSONException in GetBeerRating::onPostExecute");
 					e.printStackTrace();
 				}
 			} else {
 				Log.w(LOG_TAG, "Result was null in GetBeerRating::onPostExecute");
 			}
 			BeerDetailActivity.this.progressDialog.hide();
 		}
 	}
 
 	/**
 	 * Async task to track consumption.
 	 */
 	private class TrackConsumption extends AsyncTask<Void, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Void... voids) {
 			Log.d(LOG_TAG, "TrackConsumption doInBackground()");
 
 			final String uri = Res.getURI(Res.CONSUMPTION_DOCUMENT, Long.toString(BeerDetailActivity.this.beerId), BeerDetailActivity.this.username);
 			final HttpResponse response = BeerDetailActivity.this.httpHelper.post(uri);
 
 			if (response == null) {
 				return false;
 			}
 			final int statusCode = response.getStatusLine().getStatusCode();
 			return statusCode == HttpStatus.SC_OK;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			Log.d(LOG_TAG, "TrackConsumption onPostExecute()");
 			final int msgResId = result ? R.string.beerdetail_success_saveConsumption : R.string.beerdetail_fail_saveConsumption;
 			Toast.makeText(BeerDetailActivity.this, getString(msgResId), Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	/**
 	 * Async task to store a rating.
 	 */
 	private class SaveRating extends AsyncTask<Float, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Float... rating) {
 			Log.d(LOG_TAG, "SaveRating doInBackground()");
 
 			final String uri = Res.getURI(Res.RATING_DOCUMENT, Long.toString(BeerDetailActivity.this.beerId), BeerDetailActivity.this.username);
 
 			final JSONObject newRating = new JSONObject();
 			try {
 				newRating.put("rating", rating[0]);
 			} catch (JSONException e) {
 				throw new BierIdeeException("Could not create rating JSONObject", e);
 			}
 
			final HttpResponse response = BeerDetailActivity.this.httpHelper.post(uri, newRating);

			if (response == null) {
 				return false;
 			}
			final int statusCode = response.getStatusLine().getStatusCode();
			return statusCode == HttpStatus.SC_CREATED;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			Log.d(LOG_TAG, "SaveRating onPostExecute()");
 			final int msgResId = result ? R.string.beerdetail_success_saveRating : R.string.beerdetail_fail_saveRating;
 			Toast.makeText(BeerDetailActivity.this, getString(msgResId), Toast.LENGTH_SHORT).show();
 		}
 	}
 }
