 package com.tiptax;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 
 public class CurrencyConverter {
 
 	private static final String GOOGLEHOST = "http://www.google.com/ig/calculator?";
 	private static final String OEHOST = "http://openexchangerates.org/latest.json";
 	private static final HttpClient httpClient = new DefaultHttpClient();
 	private static final long weekInSeconds = 604800;
 
 	private String fromC;
 	private String toC;
 	private double amount;
 	private final Context ctx;
 	private HashMap<String, Double> exRatesMap;
 	private SharedPreferences prefs;
 	private CurrencyDBWrapper cDB;
 
 	public CurrencyConverter(Context ctx, String from, String to, double amount) {
 		this.ctx = ctx;
 		this.setFrom(from);
 		this.setTo(to);
 		this.setAmount(amount);
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
 		cDB = new CurrencyDBWrapper(ctx);
 		cDB.open();
 		exRatesMap = cDB.getAllCurrencies();
 	}
 
 	/*
 	 * Uses google to convert currencies immediately
 	 */
 	public double convert() throws IllegalStateException, IOException, JSONException {
 
 		if (isOnline()) {
 
 			// Checking if we have a copy of the exchange rates yet, or if they
 			// are over a week old
 			if (exRatesMap == null || exRatesMap.isEmpty()
 					|| ((int) (System.currentTimeMillis() / 1000L) - prefs.getLong("timestamp", (System.currentTimeMillis() / 1000L))) > weekInSeconds) {
 				try {
 					getExchangeRates();
 				} catch (Exception e) {
 					// we don't care about messups here, we'll just continue
 					// with the normal convert
 				}
 			}
 
 			HttpResponse response = httpClient.execute(makeQuery());
 			HttpEntity entity = response.getEntity();
 			String stripedRes = null;
 
 			if (entity != null) {
 
 				InputStream instream = entity.getContent();
 				String result = isToString(instream);
 				instream.close();
 
 				JSONObject json = new JSONObject(result);
 
 				// Google returns the currency name, so we need to get rid of
 				// that
 				stripedRes = json.getString("rhs").replaceAll("[^\\d.]", "");
 
 			}
 			return Double.parseDouble(stripedRes);
 		}
 
 		// No internet but we have saved exchange rates we can use!
 		else if ((exRatesMap = cDB.getAllCurrencies()) != null && !exRatesMap.isEmpty()) {
 			return convertFromSavedEx();
 		}
 
 		// No internet and no saved rates = no currency convertion possible :(F
 		else {
 			throw new IOException("No internet or saved exchange rates!");
 		}
 	}
 
 	/*
 	 * Converts an inputStream into a String.
 	 */
 	private String isToString(InputStream is) throws IOException {
 		String line = "";
 		StringBuilder total = new StringBuilder();
 		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 
 		while ((line = rd.readLine()) != null) {
 			total.append(line);
 		}
 
 		return total.toString();
 	}
 
 	/*
 	 * Uses a free API to get pretty up-to-date exchange rates
 	 */
 	private void getExchangeRates() throws ClientProtocolException, IOException, JSONException {
 
 		HttpResponse response = httpClient.execute(new HttpPost(OEHOST));
 		HttpEntity entity = response.getEntity();
 
 		if (entity != null) {
 
 			InputStream instream = entity.getContent();
 			String result = isToString(instream);
 			instream.close();
 
 			JSONObject json = new JSONObject(result);
 
 			// Saving timestamp
 			SharedPreferences.Editor ed = prefs.edit();
 			ed.putLong("timestamp", json.getLong("timestamp"));
 			ed.commit();
 
 			JSONObject rates = json.getJSONObject("rates");
 			Iterator<?> it = rates.keys();
 
 			while (it.hasNext()) {
 				String next = it.next().toString();
 				cDB.addCurrency(next, rates.getDouble(next));
 			}
 		}
 	}
 
 	/*
 	 * Converts from one currency to another using the saved exchange rates
 	 */
 	private double convertFromSavedEx() {
		return amount * (exRatesMap.get(toC) / exRatesMap.get(fromC));
 	}
 
 	/*
 	 * Returns true if the phone is connected to the internet
 	 */
 	private boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	 * Creates a Google HttpPost Query
 	 */
 	private HttpPost makeQuery() {
 		String query = GOOGLEHOST + "hl=en&q=" + amount + fromC + "%3D%3F" + toC;
 		return new HttpPost(query);
 	}
 
 	public void setAmount(double amount) {
 		this.amount = amount;
 	}
 
 	public void setFrom(String from) {
 		this.fromC = from;
 	}
 
 	public void setTo(String to) {
 		this.toC = to;
 	}
 
 }
