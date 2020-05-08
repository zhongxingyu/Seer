 package uk.co.mentalspace.android.bustimes.sources.londonuk;
 
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import uk.co.mentalspace.android.bustimes.Location;
 import uk.co.mentalspace.android.bustimes.LocationRefreshTask;
 import uk.me.jstott.jcoord.LatLng;
 import uk.me.jstott.jcoord.OSRef;
 import android.util.Log;
 
 public class LondonUK_AsyncBusStops extends LocationRefreshTask {
 	private static final String BUS_LOCATIONS_URL = "http://www.tfl.gov.uk/tfl/businessandpartners/syndication/feed.aspx?email=willinghamg%40hotmail.com&feedId=10";
 	private static final String LOGNAME = "LondonUK_AsyncBusStops";
 
 	protected String[] progressLabels = new String[] {"Contacting server", "Downloading data", "Processing records"};
 	
 	protected void publishProgress(int labelIndex, int value) {
 		Log.v(LOGNAME, "Updating progress values");
 		String progressLabel = progressLabels[labelIndex];
 		if (PROGRESS_POSITION_PROCESSING_DATA == labelIndex) progressLabel += " ("+value+" / "+getMaxProgress()+")";
 
 		publishProgress(progressLabel, value);
 	}
 	
 	@Override
 	public String getSourceId() {
 		//TODO remove hard coding, and read from master 'LondonUK' value instead
 		return "londonuk-tfl";
 	}
 	
 	public void performRefresh() {
 		if (null == ldba) {
 			Log.e(LOGNAME, "get Bus Stops called with no Location DB Adapter set");
 			return;
 		}
 
 		String url = BUS_LOCATIONS_URL;
 		Log.d(LOGNAME, "Data feed url: "+url);
 
 		BufferedReader br = null;
 		try {
 			publishProgress(PROGRESS_POSITION_CONTACTING_SERVER, 0);
 			HttpClient client = new DefaultHttpClient();
 			HttpGet request = new HttpGet(url);
 			Log.d(LOGNAME, "Requesting data from Server");
 			HttpResponse response = client.execute(request);
 			
 			publishProgress(PROGRESS_POSITION_DOWNLOADING_DATA, 0);
 			Log.d(LOGNAME, "Request executed - processing response");
 			InputStreamReader isr = new InputStreamReader(response.getEntity().getContent());
 			br = new BufferedReader(isr);
 			
 			String line = br.readLine();
 			Log.d(LOGNAME, "First line: "+line);
 			
 			//skip the first line, as it is headers
 			line = br.readLine();
 
 			//keep a counter for the progress bar
 			currentProgress = 0;
 			
 			//open the DB connection now, instead of inside the loop
 			publishProgress(PROGRESS_POSITION_PROCESSING_DATA, currentProgress);
 			Map<String,String> keys = ldba.getComboKeys(getSourceId());
 			while (null != line && !("".equals(line.trim())) && !this.isCancelled()) {
 
 				try {
 					processLocation(keys, line);
 				} catch (Exception e) {
 					Log.w(LOGNAME, "Unknown exception processing stop data ["+line+"]", e);
 				}
 
 				line = br.readLine();
 				currentProgress++;
 				if (currentProgress%100 == 0) {
 					publishProgress(PROGRESS_POSITION_PROCESSING_DATA, currentProgress);
 				}
 			}
 
 			Log.d(LOGNAME, "Finished processing ["+currentProgress+"] rows in the response.");
 			
 		} catch (IOException ioe) {
 			Log.e(LOGNAME, "Unexception IOException occured: "+ioe);
 			return;
 		} finally {
 			if (null != br) {
 				try { br.close(); } catch (IOException ioe2) { Log.e(LOGNAME, "Failed to close input stream. cause: "+ioe2); }
 			}
 		}
 		
 		finish();
 	}
 
 	private String[] getLocValues(String line) {
 		ArrayList<String> cols = new ArrayList<String>();
 		String[] splits = line.split(",");
 		
 		String col = "";
 		boolean hasQuotedCols = false;
 		boolean startsWithQuote = false;
 		boolean endsWithQuote = false;
 		boolean isFirstToken = true;
 		for (String split: splits) {
 			if (split.startsWith("\"")) {
 				split = split.substring(1); //remove leading quote
 				startsWithQuote = true;
 			}
 			if (split.endsWith("\"")) {
 				split = split.substring(0, split.length()-1); //remove the trailing quote
 				endsWithQuote = true;
 			}
 
 			if (!isFirstToken) col += ", ";
 			col += split;
 	
 			//debugging flag
 			if (startsWithQuote && endsWithQuote) {
 				hasQuotedCols = true;
 			}
 			
 			if (!startsWithQuote || (startsWithQuote && endsWithQuote)) {
 				startsWithQuote = false;
 				endsWithQuote = false;
 				isFirstToken = true;
 				cols.add(col);
 				col = "";
 			}
 		}
 		if (hasQuotedCols) {
 			Log.v(LOGNAME, "Quoted Line ["+line+"], token count ["+cols.size()+"]");
 		}
 		
 		String[] toReturn = cols.toArray(new String[]{});
 		return toReturn;
 	}
 	
 	private void processLocation(Map<String,String> keys, String s) {
 		String cols[] = getLocValues(s);
 		if (cols.length < 6) return;
 
 		String comboKey = ldba.getComboKey(cols[1], cols[4], cols[5]);
 		String stopCode = keys.get(comboKey);
 
 		if (null == stopCode) {
 			if (keys.entrySet().contains(cols[1])) {
 				//stop code exists but srcPosA or srcPosB don't match - has moved location - delete old one and re-create
				ldba.deleteLocationByStopCode(cols[1]);
 			}
 			createNewLocation(cols);
 		}
 		else {
 			Location loc = ldba.getLocationByStopCode(stopCode);
 			ldba.updateLocation(loc.getId(), cols[1], cols[3], loc.getDescription(), loc.getLat(), loc.getLon(), cols[4], cols[5], cols[6], loc.getNickName(), loc.getChosen(), this.getSourceId());
 		}
 	}
 	
 	private void createNewLocation(String cols[]) {		
 		LatLng latlng = null;
 		try {
 			double spa = Double.parseDouble(cols[4]);
 			double spb = Double.parseDouble(cols[5]);
 			latlng = new OSRef(spa,spb).toLatLng();
 			latlng.toWGS84();
 		} catch (NumberFormatException nfe) {
 			Log.e(LOGNAME, "Failed to parse northing,easting values ["+cols[4]+","+cols[5]+"].  Other values ["+cols[0]+","+cols[1]+","+cols[3]+","+cols[6]+"]");
 			return;
 		}
 		int lat = (int)(latlng.getLat()*10000);
 		int lng = (int)(latlng.getLng()*10000);
 		ldba.createLocation(cols[1], cols[3], "", lat, lng, cols[4], cols[5], cols[6], this.getSourceId());
 	}
 
 	@Override
 	public int getMaxProgress() {
 		return 20000;
 	}
 
 	@Override
 	public String getSourceName() {
 		return "London, UK (TFL)";
 	}
 }
