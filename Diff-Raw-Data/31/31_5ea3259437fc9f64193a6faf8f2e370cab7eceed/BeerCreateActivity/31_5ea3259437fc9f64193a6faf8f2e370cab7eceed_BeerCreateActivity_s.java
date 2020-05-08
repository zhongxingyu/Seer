 package ch.hsr.bieridee.android.activities;
 
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.Toast;
 import ch.hsr.bieridee.android.R;
 import ch.hsr.bieridee.android.Validators;
 import ch.hsr.bieridee.android.adapters.BreweryListAdapter;
 import ch.hsr.bieridee.android.adapters.CreateBeerSpinnerAdapter;
 import ch.hsr.bieridee.android.config.Res;
 import ch.hsr.bieridee.android.http.AuthJsonHttp;
 import ch.hsr.bieridee.android.http.HttpHelper;
 
 /**
  * Activity to create a new beer.
  * 
  */
 public class BeerCreateActivity extends Activity {
 
 	private static final String LOG_TAG = BreweryListActivity.class.getName();
 	private CreateBeerSpinnerAdapter breweryAdapter;
 	private CreateBeerSpinnerAdapter beertypeAdapter;
 	private ArrayAdapter<String> autoCompleteAdapter;
 	private HttpHelper httpHelper;
 	private MultithreadProgressDialog progressDialog;
 	private EditText beername;
 	private AutoCompleteTextView brand;
 	private Spinner brewerySpinner;
 	private Spinner beertypeSpinner;
 	private ImageButton beerNameInfoButton;
 	private ImageButton brandInfoButton;
 	private ImageButton beertypeInfoButton;
 	private ImageButton breweryInfoButton;
 	private Button createButton;
 	private ImageButton createBreweryButton;
 	private long beerId;
 
 	private GetBrandData getBrandDataTask;
 	private GetBreweryData getBreweryDataTask;
 	private GetBeertypeData getBeertypeDataTask;
 	private final CountDownLatch allLoadingTasksFinished = new CountDownLatch(3);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.beercreate);
 
 		this.httpHelper = AuthJsonHttp.create();
 		this.progressDialog = new MultithreadProgressDialog(getString(R.string.pleaseWait), getString(R.string.loadingData));
 
 		this.beername = (EditText) findViewById(R.id_beercreate.beername);
 		this.brand = (AutoCompleteTextView) findViewById(R.id_beercreate.brandAutocomplete);
 
 		this.beertypeSpinner = (Spinner) findViewById(R.id_beercreate.beertypeSpinner);
 		this.beertypeAdapter = new CreateBeerSpinnerAdapter(this);
 		this.beertypeSpinner.setAdapter(this.beertypeAdapter);
 
 		this.brewerySpinner = (Spinner) findViewById(R.id_beercreate.brewerySpinner);
 		this.breweryAdapter = new CreateBeerSpinnerAdapter(this);
 		this.brewerySpinner.setAdapter(this.breweryAdapter);
 
 		this.createBreweryButton = (ImageButton) findViewById(R.id_beercreate.createBreweryButton);
 		this.createButton = (Button) findViewById(R.id_beercreate.createButton);
 
 		setButtonAction();
 		setBreweryCreateAction();
 
 		this.beerNameInfoButton = (ImageButton) findViewById(R.id_beercreate.nameInfoButton);
 		this.setBeerNameInfoButtonAction();
 		this.brandInfoButton = (ImageButton) findViewById(R.id_beercreate.brandInfoButton);
 		this.setBrandNameInfoButtonAction();
 		this.beertypeInfoButton = (ImageButton) findViewById(R.id_beercreate.beertypeInfoButton);
 		setBeertypeInfoButtonAction();
 		this.breweryInfoButton = (ImageButton) findViewById(R.id_beercreate.breweryInfoButton);
 		setBreweryInfoButtonAction();
 		final Bundle extras = getIntent().getExtras();
 		if (extras != null && extras.getLong("beerToUpdate") > 0) {
 			BeerCreateActivity.this.beerId = extras.getLong("beerToUpdate");
 			new GetBeerDetail().execute();
 		}
 
 	}
 
 	private void setBeerNameInfoButtonAction() {
 		this.beerNameInfoButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				final AlertDialog.Builder builder = new AlertDialog.Builder(BeerCreateActivity.this);
 				builder.setMessage(BeerCreateActivity.this.getString(R.string.beercreate_nameInfo)).setCancelable(true).setPositiveButton(BeerCreateActivity.this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 				final AlertDialog info = builder.create();
 				info.show();
 			}
 		});
 	}
 
 	private void setBrandNameInfoButtonAction() {
 		this.brandInfoButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				final AlertDialog.Builder builder = new AlertDialog.Builder(BeerCreateActivity.this);
 				builder.setMessage(BeerCreateActivity.this.getString(R.string.beercreate_brandInfo)).setCancelable(true).setPositiveButton(BeerCreateActivity.this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 				final AlertDialog info = builder.create();
 				info.show();
 			}
 		});
 	}
 
 	private void setBeertypeInfoButtonAction() {
 		this.beertypeInfoButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				final AlertDialog.Builder builder = new AlertDialog.Builder(BeerCreateActivity.this);
 				builder.setMessage(BeerCreateActivity.this.getString(R.string.beercreate_beertypeInfo)).setCancelable(true).setPositiveButton(BeerCreateActivity.this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				}).setNeutralButton(BeerCreateActivity.this.getString(R.string.more), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						Toast.makeText(BeerCreateActivity.this, "TOFU", Toast.LENGTH_SHORT).show();
 						dialog.cancel();
 					}
 				});
 				final AlertDialog info = builder.create();
 				info.show();
 			}
 		});
 	}
 
 	private void setBreweryInfoButtonAction() {
 		this.breweryInfoButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				final AlertDialog.Builder builder = new AlertDialog.Builder(BeerCreateActivity.this);
 				builder.setMessage(BeerCreateActivity.this.getString(R.string.beercreate_breweryInfo)).setCancelable(true).setPositiveButton(BeerCreateActivity.this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				}).setNeutralButton(BeerCreateActivity.this.getString(R.string.add), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						Toast.makeText(BeerCreateActivity.this, "TOFU", Toast.LENGTH_SHORT).show();
 						dialog.cancel();
 					}
 				});
 				final AlertDialog info = builder.create();
 				info.show();
 			}
 		});
 	}
 
 	private void setBreweryCreateAction() {
 		this.createBreweryButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				final Intent intent = new Intent(v.getContext(), BreweryCreateActivity.class);
 				startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		this.getBrandDataTask = new GetBrandData();
 		this.getBeertypeDataTask = new GetBeertypeData();
 		this.getBreweryDataTask = new GetBreweryData();
 
 		this.getBrandDataTask.execute();
 		this.getBeertypeDataTask.execute();
 		this.getBreweryDataTask.execute();
 	}
 
 	private void setButtonAction() {
 		this.createButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View view) {
 				final String beername = BeerCreateActivity.this.beername.getText().toString().trim();
 				final String brand = BeerCreateActivity.this.brand.getText().toString().trim();
 				final long beertypeId = BeerCreateActivity.this.beertypeSpinner.getSelectedItemId();
 				final long breweryId = BeerCreateActivity.this.brewerySpinner.getSelectedItemId();
 
 				if (Validators.validateNonEmpty(beername) && Validators.validateNonEmpty(brand)) {
 					final JSONObject newBeer = new JSONObject();
 					try {
 						newBeer.put("name", beername);
 						newBeer.put("brand", brand);
 						newBeer.put("beertype", beertypeId);
 						newBeer.put("brewery", breweryId);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 					Log.d(LOG_TAG, newBeer.toString());
 					new BeerCreateActivity.AddNewBeer().execute(newBeer);
 				} else {
 					Toast.makeText(BeerCreateActivity.this, "Bitte alles ausfüllen", Toast.LENGTH_SHORT).show();
 				}
 
 			}
 
 		});
 	}
 
 	/**
 	 * Adds a new beer!
 	 */
 	private class AddNewBeer extends AsyncTask<JSONObject, Void, JSONObject> {
 
 		@Override
 		protected JSONObject doInBackground(JSONObject... params) {
 			Log.d(LOG_TAG, "doInBackground()");
 
 			final HttpResponse response = BeerCreateActivity.this.httpHelper.post(Res.getURI(Res.BEER_COLLECTION), params[0]);
 
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
 				Toast.makeText(BeerCreateActivity.this, "Bier wurde erfolgreich erstellt", Toast.LENGTH_SHORT).show();
 				BeerCreateActivity.this.beername.setText("");
 				BeerCreateActivity.this.brand.setText("");
 			} else {
 				Toast.makeText(BeerCreateActivity.this, "whoa, fehler!", Toast.LENGTH_SHORT).show();
 			}
 		}
 
 	}
 
 	/**
 	 * Async task to get beertypes from server and update UI.
 	 */
 	private class GetBeertypeData extends AsyncTask<Void, Void, JSONArray> {
 		@Override
 		protected void onPreExecute() {
 			Log.d(LOG_TAG, "onPreExecute()");
 			BeerCreateActivity.this.progressDialog.display(BeerCreateActivity.this, true);
 		}
 
 		@Override
 		protected JSONArray doInBackground(Void... voids) {
 			Log.d(LOG_TAG, "doInBackground()");
 
 			final HttpResponse response = BeerCreateActivity.this.httpHelper.get(Res.getURI(Res.BEERTYPE_COLLECTION));
 
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
 				BeerCreateActivity.this.beertypeAdapter.updateData(result);
 				BeerCreateActivity.this.beertypeAdapter.notifyDataSetChanged();
 			}
 			BeerCreateActivity.this.progressDialog.hide();
 			BeerCreateActivity.this.allLoadingTasksFinished.countDown();
 			Log.d("info", "Beertype data finished");
 		}
 	}
 
 	/**
 	 * Async task to get breweries from server and update UI.
 	 */
 	private class GetBreweryData extends AsyncTask<Void, Void, JSONArray> {
 		@Override
 		protected void onPreExecute() {
 			Log.d(LOG_TAG, "onPreExecute()");
 			BeerCreateActivity.this.progressDialog.display(BeerCreateActivity.this, true);
 		}
 
 		@Override
 		protected JSONArray doInBackground(Void... voids) {
 			Log.d(LOG_TAG, "doInBackground()");
 
 			final HttpResponse response = BeerCreateActivity.this.httpHelper.get(Res.getURI(Res.BREWERY_COLLECTION));
 
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
 				BeerCreateActivity.this.breweryAdapter.updateData(result);
 				BeerCreateActivity.this.breweryAdapter.notifyDataSetChanged();
 			}
 			BeerCreateActivity.this.progressDialog.hide();
 			BeerCreateActivity.this.allLoadingTasksFinished.countDown();
 			Log.d("info", "Brewery data finished");
 		}
 	}
 
 	/**
 	 * Async task to get brands from server and add to the autocomplete list.
 	 */
 	private class GetBrandData extends AsyncTask<Void, Void, JSONArray> {
 		@Override
 		protected void onPreExecute() {
 			Log.d(LOG_TAG, "onPreExecute()");
 			BeerCreateActivity.this.progressDialog.display(BeerCreateActivity.this, true);
 		}
 
 		@Override
 		protected JSONArray doInBackground(Void... voids) {
 			Log.d(LOG_TAG, "doInBackground()");
 
 			final HttpResponse response = BeerCreateActivity.this.httpHelper.get(Res.getURI(Res.BRAND_COLLECTION));
 
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
 				final String[] brands = new String[result.length()];
 				for (int i = 0; i < result.length(); i++) {
 					try {
 						brands[i] = result.getString(i);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 				BeerCreateActivity.this.autoCompleteAdapter = new ArrayAdapter<String>(BeerCreateActivity.this, android.R.layout.simple_dropdown_item_1line, brands);
 				BeerCreateActivity.this.brand.setAdapter(BeerCreateActivity.this.autoCompleteAdapter);
 			}
 			BeerCreateActivity.this.progressDialog.hide();
 			BeerCreateActivity.this.allLoadingTasksFinished.countDown();
 			Log.d("info", "Brand data finished");
 
 		}
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
 				BeerCreateActivity.this.progressDialog.display(BeerCreateActivity.this, true);
 			}
 		}
 
 		@Override
 		protected JSONObject doInBackground(Void... voids) {
 			final String uri = Res.getURI(Res.BEER_DOCUMENT, Long.toString(BeerCreateActivity.this.beerId));
 			final HttpResponse response = BeerCreateActivity.this.httpHelper.get(uri);
 
 			if (response != null) {
 				final int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode == HttpStatus.SC_OK) {
 					try {
 						final String responseText = new BasicResponseHandler().handleResponse(response);
						
						// wait for all other tasks to be finished.
 						BeerCreateActivity.this.allLoadingTasksFinished.await();
 						return new JSONObject(responseText);
 					} catch (IOException e) {
 						Log.e(LOG_TAG, "IOException in GetBeerDetail::doInBackground");
 						e.printStackTrace();
 					} catch (JSONException e) {
 						Log.e(LOG_TAG, "JSONException in GetBeerDetail::doInBackground");
 						e.printStackTrace();
 					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
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
 
					// Query String and brand
 					BeerCreateActivity.this.beername.setText(result.getString("name"));
 					BeerCreateActivity.this.brand.setText(result.getString("brand"));
 
					// Query Brewery
 					final JSONObject resultBrewery = result.getJSONObject("brewery");
 					final long breweryId = resultBrewery.optLong("id");
 					final BreweryListAdapter breweryListAdapter = (BreweryListAdapter) BeerCreateActivity.this.brewerySpinner.getAdapter();
					Log.d("info", "Data in breweryList: " + breweryListAdapter.getCount());
 					final int breweryPosition = breweryListAdapter.getPositionOf(breweryId);
 					BeerCreateActivity.this.brewerySpinner.setSelection(breweryPosition);
					Log.d("info", "brewery: " + breweryId + " position: " + breweryPosition);
 
					// Query Beertype
 					final JSONObject resultBeertype = result.getJSONObject("beertype");
					Log.d("info", resultBrewery.toString());
					Log.d("info", resultBeertype.toString());
 					final BreweryListAdapter beertypeListAdapter = (BreweryListAdapter) BeerCreateActivity.this.beertypeSpinner.getAdapter();
 					final long beertypeId = resultBeertype.optLong("id");
 					final int beerTypePosition = beertypeListAdapter.getPositionOf(beertypeId);
 					BeerCreateActivity.this.beertypeSpinner.setSelection(beerTypePosition);
					Log.d("info", "beertype: " + beertypeId + " position: " + beerTypePosition);
 
 				} catch (JSONException e) {
 					Log.e(LOG_TAG, "JSONException in GetBeerDetail::onPostExecute");
 					e.printStackTrace();
 				}
 			} else {
 				Log.w(LOG_TAG, "Result was null in GetBeerDetail::onPostExecute");
 			}
 			if (this.showDialog) {
 				BeerCreateActivity.this.progressDialog.hide();
 			}
 		}
 	}
 
 }
