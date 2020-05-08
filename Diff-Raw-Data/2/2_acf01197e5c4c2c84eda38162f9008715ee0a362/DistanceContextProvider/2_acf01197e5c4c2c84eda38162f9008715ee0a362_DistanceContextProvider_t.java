 package br.rio.puc.inf.lac.mobilis.cms.provider;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URLEncoder;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import br.rio.puc.inf.lac.mobilis.cms.ContextInformationObject;
 import br.rio.puc.inf.lac.mobilis.cms.provider.ContextProvider;
 
 /**
  * @author victor
  *
  */
 public class DistanceContextProvider extends ContextProvider {
 	/** saves the refresh interval */
 	private int refreshInterval;
 	private Handler handler;
 	private LocationManager lm;
 	private String currentlocation, destination;
 	static final String TAG = "DistanceContextProvider";
 
 	/**
 	 * Implements a class to get the distance.
 	 * 
 	 * @author victor
 	 *
 	 */
 	private class DistanceGetter implements Runnable {
 		private String from, to;
 
 		DistanceGetter (String from, String to) {
 			this.from = from;
 			this.to = to;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void run() {
 			ContextInformationObject contextInformation = new ContextInformationObject("distance");
 
 			String url = "http://maps.google.com/maps/api/directions/json?origin="+from+"&destination="+to+"&sensor=true&language=pt-BR";
 			String result = queryRESTurl(url);  
 			try{
 				JSONObject json = new JSONObject(result);  
 				if (json.getString("status").equals("OK"))
 				{
 					JSONArray routesArray = json.getJSONArray("routes");
 					JSONObject routeObject = routesArray.getJSONObject(0);
 
 					JSONArray legsArray = routeObject.getJSONArray("legs");
 					JSONObject legsObject = legsArray.getJSONObject(0);
 
 					JSONObject distanceObject = legsObject.getJSONObject("distance");
 					JSONObject durationObject = legsObject.getJSONObject("duration");
 					String distancia = distanceObject.getString("text");
 					String duracao = durationObject.getString("text");
 
 					Log.i(TAG,"Distancia: " + distancia + "\nDuracaoo: " + duracao);
 
 					contextInformation.addContextInformation("time",duracao);
 					contextInformation.addContextInformation("meters",distancia);
 					sendUpdatedInformation(contextInformation);
 				}
 			}  
 			catch (JSONException e) {  
 				Log.e("JSON", "There was an error parsing the JSON", e);  
 			}
 		}
 	}
 
 	private class Where implements LocationListener {
 		DistanceContextProvider parent;
 		public Where (DistanceContextProvider parent) {
 			Log.d(TAG, "Instantiating Where object");
 			this.parent = parent;
 		}
 		
 		@Override
 		public void onLocationChanged(Location location) {
 			/* String from = "-22.9575,-43.196797";
 			 * String to = "-22.977415,-43.233876"; */
 
 			parent.currentlocation = String.format("%g,%g", location.getLongitude(), location.getLatitude());
 			Log.i(TAG, "Current Location: " + currentlocation );
 
 			parent.handler.post(new DistanceGetter(parent.currentlocation, parent.destination));
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {}
 
 		@Override
 		public void onProviderEnabled(String provider) {}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 	}
 	
 	private class myHandler extends Handler {
 		public DistanceContextProvider p;
 		public Context c;
 		myHandler (DistanceContextProvider parent, Context context) {
 			this.p = parent;
 			this.c = context;
 		}
 	}
 
 	/**
 	 * @param context
 	 */
 	public DistanceContextProvider(Context context) {
 		super(context, "DistanceContextProvider");
 	    handler = new myHandler(this, context) {
 	        @Override
 	        public void handleMessage(Message msg) {
 	          super.handleMessage(msg);
 	          
 	          p.lm = (LocationManager) c.getSystemService(android.content.Context.LOCATION_SERVICE);
 	          p.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, refreshInterval * 1000, Float.parseFloat("0.001"), new Where(p));
 	        }
 	      };
 		
 		addInformationProvided("distance.time");
 		addInformationProvided("distance.meters");
 
 		addConfigurationSuported("destination");
 		addConfigurationSuported("refreshInterval");
 
 		Log.d("distancecontextprovider", "Provedor de Distancia Inicializado! (v2.17)");
 	}
 
 	@Override
 	public int setConfiguration(String configName, String configValue) {
 		Log.d(TAG, "Setando refresh");
 		if (configName.equals("refreshInterval")) {
 			this.refreshInterval = Integer.parseInt(configValue);
 			return 0;
 		}
 		
 		if (configName.equals("destination")) {
 			Log.d(TAG, "Setando destino");
			String url = "http://maps.google.com/maps/api/geocode/json?address="+ URLEncoder.encode(configValue) +"&sensor=true";
 			String result = queryRESTurl(url);  
 			try {
 				JSONObject json = new JSONObject(result);  
 				if (json.getString("status").equals("OK"))
 				{
 					JSONArray resultsArray = json.getJSONArray("results");
 					JSONObject addressObject = resultsArray.getJSONObject(0);
 					
 					JSONObject geometryObject = addressObject.getJSONObject("geometry");
 					JSONObject locationObject = geometryObject.getJSONObject("location");
 					
 					this.destination = String.format("%s,%s", locationObject.getString("lat"), locationObject.getString("lng"));
 					Log.i(TAG,"Destination: " + this.destination );
 					
 					handler.sendEmptyMessage(0);
 				}
 			}
 			
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 
 			return 0;
 		}
 		return -1;
 	}
 
 	@Override
 	public void updateInformation(String information) {
 		updateInformations();
 	}
 
 	@Override
 	public void updateInformations() {
 		return;
 	}
 
 	protected void sendUpdatedInformation(ContextInformationObject obj) {
 		updateInformation(obj);
 	}
 
 	private String queryRESTurl(String url) {  
 		HttpParams params = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
 		HttpConnectionParams.setSoTimeout(params, 10 * 1000);
 		HttpConnectionParams.setTcpNoDelay(params, true);
 		HttpConnectionParams.setStaleCheckingEnabled(params, true);
 		
 		HttpClient httpclient = new DefaultHttpClient(params);
 		HttpGet httpget = new HttpGet(url);  
 		HttpResponse response;
 
 		try {
 			Log.i(TAG, "Querying URL:" + url);
 			response = httpclient.execute(httpget);  
 			// Log.i(TAG, "Status:[" + response.getStatusLine().toString() + "]");  
 			HttpEntity entity = response.getEntity();  
 
 			if (entity != null) {  
 
 				InputStream instream = entity.getContent();  
 				String result = convertStreamToString(instream);  
 				// Log.i(TAG, "Result of converstion: [" + result + "]");  
 
 				instream.close();  
 				return result;  
 			}  
 		} catch (ClientProtocolException e) {  
 			Log.e("mapa", "There was a protocol based error", e);  
 		} catch (IOException e) {  
 			Log.e("mapa", "There was an IO Stream related error", e);  
 		}
 		return null;  
 	}
 
 	private String convertStreamToString(InputStream is) {
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return sb.toString();
 	}
 }
