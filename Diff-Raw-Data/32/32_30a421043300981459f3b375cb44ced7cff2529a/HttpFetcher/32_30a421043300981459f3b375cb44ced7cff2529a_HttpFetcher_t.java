 package com.saturdaycoder.easydoubanfm.player;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 import com.saturdaycoder.easydoubanfm.Debugger;
 import com.saturdaycoder.easydoubanfm.Global;
 import com.saturdaycoder.easydoubanfm.Utility;
 
 public class HttpFetcher {
 	private ArrayList<IHttpFetcherObserver> observerList = new ArrayList<IHttpFetcherObserver>();
	private Map<String, byte[]> contentMap = new LinkedHashMap<String, byte[]>();
 	private Map<String, FetcherTask> taskMap = new HashMap<String, FetcherTask>();
 	private static HttpFetcher inst = new HttpFetcher();
 	public static HttpFetcher getInstance() {
 		return inst;
 	}
 	
 	public Set<String> runingKeySet() {
 		return taskMap.keySet();
 	}
 	
 	public Set<String> finishedKeySet() {
 		return contentMap.keySet();
 	}
 	
 	
 	public void fetch(String url) {
 		if (contentMap.containsKey(url) || taskMap.containsKey(url))
 			return;
 		FetcherTask ft = new FetcherTask(url);
 		synchronized(taskMap) {
 			taskMap.put(url, ft);
 		}
 		Debugger.verbose("HttpFetcher start fetch " + url);
 		ft.execute();
 	}
 	
 	public byte[] getContent(String url) {
 		return contentMap.get(url);
 	}
 	
 	public void registerObserver(IHttpFetcherObserver o) {
 		if (!observerList.contains(o))
 			observerList.add(o);
 	}
 	
 	public void unregisterObserver(IHttpFetcherObserver o) {
 		if (observerList.contains(o))
 			observerList.remove(o);
 	}
 	
     private class FetcherTask extends AsyncTask<String, Integer, byte[]> {
     	private String url;
     	private int reason;
     	public FetcherTask(String url){
     		this.url = url;
     	}
     	public String getUrl() {
     		return url;
     	}
     	
     	@Override
     	protected void onCancelled () {
     		Debugger.info("FetcherTask is cancelled");
     		synchronized(taskMap) {
     			taskMap.remove(url);
     		}
     	}
     	@Override
     	protected byte[] doInBackground(String... params) {
         	//if (params.length < 1)
         	//	return null;
     		//taskMap.put(url, this);
         	
     		HttpGet httpGet = new HttpGet(url);
     		httpGet.setHeader("User-Agent", 
     				String.valueOf(Utility.getSdkVersionName()));
     		httpGet.setHeader("Connection", "Keep-Alive");
     		HttpResponse httpResponse = null;
     		try {
     			httpResponse = new DefaultHttpClient().execute(httpGet);
 				if (isCancelled()) {
 					reason = Global.REASON_CANCELLED;
 					return null;
 				}
     			publishProgress(70);
     		} catch (Exception e) {
     			Debugger.error("Error getting response of downloading picture: " + e.toString());
     			reason = Global.REASON_NETWORK_IO_ERROR;
     			return null;
     		}
     		
     		int statuscode = httpResponse.getStatusLine().getStatusCode();
 			if (statuscode != 200) {
 				Debugger.error("Error getting response of downloading music: status " + statuscode);
 				reason = statuscode;
     			return null;
 			}
 				
 			byte b[] = null;
 			try {
 				InputStream is = httpResponse.getEntity().getContent();
 				long len = httpResponse.getEntity().getContentLength();
 				
 				publishProgress(80);
 				
 				int length = (int)(len);
 				b = new byte[length];
 				int l = 0;
 				while (l < length) {
 					if (isCancelled()) {
 						reason = Global.REASON_CANCELLED;
 						return null;
 					}
 					int tmpl = is.read(b, l, length);
 					if (tmpl == -1)
 						break;
 					l += tmpl;
 				}
 				
 				
 				if (isCancelled()) {
 					reason = Global.REASON_CANCELLED;
 					return null;
 				}
 					
 				reason = Global.NO_REASON;
 				return b;
 			} catch (Exception e) {
 				Debugger.error("Error getting picture: " + e.toString());
 				reason = Global.NO_REASON;
 				return null;
 			}
         	
     	}
     	
     	@Override
         protected void onPostExecute(byte[] bytearray) {
     		synchronized(taskMap) {
     			taskMap.remove(url);
     		}
     		if (bytearray == null) {
     			for (IHttpFetcherObserver o: observerList)
     				o.onHttpFetchFailure(url, reason);
         		return;
     		}
      		
     		if (isCancelled()) {
     			for (IHttpFetcherObserver o: observerList)
     				o.onHttpFetchFailure(url, reason);
     			return;
     		}
     		
     		contentMap.put(url, bytearray);
    		
    		while (contentMap.size() > 10)
    		{
    			Iterator<String> iter = contentMap.keySet().iterator(); 
    			if (iter.hasNext()) {
    				contentMap.remove(iter.next());
    			}
    		}
    		
     		for (IHttpFetcherObserver o: observerList)
 				o.onHttpFetchSuccess(url);
         }
     }
 
 }
