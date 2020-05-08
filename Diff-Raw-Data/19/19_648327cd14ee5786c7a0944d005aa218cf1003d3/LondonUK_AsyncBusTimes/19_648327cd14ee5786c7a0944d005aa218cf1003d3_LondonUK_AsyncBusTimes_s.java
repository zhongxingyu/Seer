 package uk.co.mentalspace.android.bustimes.sources.londonuk;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import uk.co.mentalspace.android.bustimes.BusTime;
 import uk.co.mentalspace.android.bustimes.DataRefreshTask;
 import android.util.Log;
 
 public class LondonUK_AsyncBusTimes extends DataRefreshTask {
 	private static final String BUS_TIMES_URL = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1";
 	private static final String LOGNAME = "LondonUK_AsyncBusTimes";
 
 	public void executeSync() {
 		List<BusTime> busTimes = getBusTimes();
 		onPostExecute(busTimes);
 	}
 	
 	protected List<BusTime> doInBackground(Void... strings) {
 		return getBusTimes();
 	}
 
 	private List<BusTime> getBusTimes() {
 		if (null == display) {
 			failure = new IllegalArgumentException("Renderer not initialised");
 			return null;
 		}
 		if (null == location) {
 			failure = new IllegalArgumentException("Location not initialised");
 			return null;
 		}
 		
 		String url = BUS_TIMES_URL + "?StopCode1="+location.getStopCode()+"&DirectionID=1&VisitNumber=1&ReturnList=LineName,DestinationText,EstimatedTime";
 		Log.d(LOGNAME, "Data feed url: "+url);
 
 		ArrayList<BusTime> busTimes = new ArrayList<BusTime>();
 		BufferedReader br = null;
 		try {
 			HttpClient client = new DefaultHttpClient();
 			HttpGet request = new HttpGet(url);
 			Log.d(LOGNAME, "Requesting data from Server");
 			HttpResponse response = client.execute(request);
 			
 			Log.d(LOGNAME, "Request executed - processing response");
 			InputStreamReader isr = new InputStreamReader(response.getEntity().getContent());
 			br = new BufferedReader(isr);
 			
 			String line = br.readLine();
			Log.d(LOGNAME, "First line: "+line);
 			long refTime = getRefTime(line);
 
 			line = br.readLine();  //ignore first line - headers
 			while (null != line && !("".equals(line))) {
				Log.d(LOGNAME, "Line: " + line);
 				BusTime bt = getBusTime(line, refTime);
 				busTimes.add(bt);
 				line = br.readLine();
 			}
 
 			Log.d(LOGNAME, "Finished processing response.");
 			
 		} catch (IOException ioe) {
 			Log.e(LOGNAME, "Unexception IOException occured: "+ioe);
 			return null;
 		} finally {
 			if (null != br) {
 				try { br.close(); } catch (IOException ioe2) { Log.e(LOGNAME, "Failed to close input stream. cause: "+ioe2); }
 			}
 		}
 		
 		return busTimes;
 	}
 	
 	private long getRefTime(String line) {
 		try {
 			JSONArray j = new JSONArray(line);
 			if (j.length() != 3) return new Date().getTime();
 			return j.getLong(2);
 		} catch (JSONException e) {
 			Log.e(LOGNAME, "JSON Error parsing header.\nData: "+line+"\nError: ",e);
 		}
 		return new Date().getTime();
 	}
 	
 	private BusTime getBusTime(String line, long refTime) {
 		if (null == line || "".equals(line.trim())) return new BusTime("", "", "");
 		
 		try {
 			JSONArray j = new JSONArray(line);
 			if (j.length() < 4) return new BusTime("", "", "");
 			
 			long l = j.getLong(3) - refTime;
 			if (l < 0) l = 0; //prevent negative time estimates
 			l = l/1000; //convert to seconds
 			l = l/60; //convert to minutes
 			String est = (l > 0) ? String.valueOf(l) : "Due";
 			return new BusTime(j.getString(1), j.getString(2), est);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			Log.e(LOGNAME, "JSON Error parsing bus data.\nData: "+line+"\nError: ", e);
 		}
 		return new BusTime("", "", "");
 	}
 }
