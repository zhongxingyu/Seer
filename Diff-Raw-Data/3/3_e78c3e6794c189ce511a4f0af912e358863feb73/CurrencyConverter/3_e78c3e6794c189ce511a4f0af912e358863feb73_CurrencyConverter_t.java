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
 
 	private static final String OEHOST = "http://openexchangerates.org/latest.json";
 	private static final HttpClient httpClient = new DefaultHttpClient();
 
 	private String fromC;
 	private String toC;
 	private double amount;
 	private final Context ctx;
 	private HashMap<String, Double> exRatesMap;
 	private SharedPreferences prefs;
 	private CurrencyDBWrapper cDB;
 	private long updateFreq;
 
 	public CurrencyConverter(Context ctx, String from, String to, double amount) {
 		this.ctx = ctx;
 		this.setFrom(from);
 		this.setTo(to);
 		this.setAmount(amount);
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
 		updateFreq = Long.valueOf(prefs.getString("default_updatefreq", "604800"));
 		cDB = new CurrencyDBWrapper(ctx);
 
 		cDB.open();
 		exRatesMap = cDB.getAllCurrencies();
 		cDB.close();
 	}
 
 	/*
 	 * Uses google to convert currencies immediately
 	 */
 	public double convert() throws IllegalStateException, IOException, JSONException {
 
 		if (isOnline()) {
 			// Checking if we have a copy of the exchange rates yet, or if they
 			// are old
 			if (exRatesMap == null || exRatesMap.isEmpty()
 					|| ((int) (System.currentTimeMillis() / 1000L) - prefs.getLong("timestamp", (System.currentTimeMillis() / 1000L))) > updateFreq) {
 				getExchangeRates();
 			}
 		}
 
		cDB.open();
 		// Lets convert that stuff!
 		if ((exRatesMap = cDB.getAllCurrencies()) != null && !exRatesMap.isEmpty()) {
 			cDB.close();
 			return convertFromSavedEx();
 		}
 		// No internet and no saved rates = no currency convertion possible :(
 		else {
 			cDB.close();
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
 	 * Uses a free API to get pretty up-to-date exchange rates and puts them
 	 * into our database
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
 
 			cDB.open();
 			while (it.hasNext()) {
 				String next = it.next().toString();
 				cDB.addCurrency(next, rates.getDouble(next));
 			}
 			cDB.close();
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
