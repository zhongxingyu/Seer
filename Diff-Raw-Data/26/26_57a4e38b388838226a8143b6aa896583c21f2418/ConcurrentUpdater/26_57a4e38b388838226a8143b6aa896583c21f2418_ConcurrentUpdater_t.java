 package matdue.raidstatus;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import matdue.raidstatus.data.DkpData;
 import matdue.raidstatus.data.database.RaidDatabase;
 import matdue.raidstatus.helper.HttpClientHelper;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.util.EntityUtils;
 
 import android.os.Handler;
 import android.util.Log;
 
 public class ConcurrentUpdater implements Runnable {
 
 	public final static int OK = 1000;
 	public final static int NETWORK_ERROR = 1001;
 	public final static int NO_DATA = 1002;
 	
 	private String url;
 	private Handler handler;
 	private RaidDatabase raidDatabase;
 	
 	private final static String USER_AGENT = "DKP Raidstatus";
 
 	public ConcurrentUpdater(String url, Handler handler, RaidDatabase raidDatabase) {
 		this.url = url;
 		this.handler = handler;
 		this.raidDatabase = raidDatabase;
 	}
 	
 	public void run() {
 		HttpClient httpClient = HttpClientHelper.newInstance(USER_AGENT);
 		try {
 			HttpGet httpRequest = new HttpGet(url);
 			HttpResponse httpResponse = httpClient.execute(httpRequest);
 			int responseStatusCode = httpResponse.getStatusLine().getStatusCode();
 			if (responseStatusCode == 200) {
 				HttpEntity entity = httpResponse.getEntity();
 				if (entity != null) {
 					String contentCharset = EntityUtils.getContentCharSet(entity);
 		        	if (contentCharset == null) {
 		        		contentCharset = "ISO-8859-1";
 		        	}
 		        	
 		        	InputStream stream = entity.getContent();
 		        	BufferedReader reader = new BufferedReader(new InputStreamReader(stream, contentCharset), 8192);
 		        	parseAndSave(reader);
 					stream.close();
 				} else {
 					handler.sendEmptyMessage(NO_DATA);
 					return;
 				}
 			} else {
 				handler.sendEmptyMessage(NO_DATA);
 				return;
 			}
 		} catch (IOException e) {
 			handler.obtainMessage(NETWORK_ERROR, e.getMessage()).sendToTarget();
 			return;
 		} finally {
 			httpClient.getConnectionManager().shutdown();
 		}
 		
 		handler.sendEmptyMessage(OK);
 	}
 	
 	private void parseAndSave(BufferedReader reader) throws IOException {
 		Log.v("DKPRaidStatus", "Start parseAndSave()");
 		DkpData dkpData = new DkpData();
 		dkpData.parse(reader);
 		Log.v("DKPRaidStatus", "Parsing done");
 		
 		raidDatabase.deleteAllData();
 		Log.v("DKPRaidStatus", "Delete done");
 		raidDatabase.insertPlayers(dkpData.players.values());
 		Log.v("DKPRaidStatus", "Player storage done");
 		raidDatabase.insertRaids(dkpData.raids);
 		Log.v("DKPRaidStatus", "Raid storage done");
 		raidDatabase.close();
 	}
 
 }
