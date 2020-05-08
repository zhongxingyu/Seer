 package pl.nk.warehouse.openddr.client;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import pl.nk.warehouse.openddr.common.Configuration;
 
 /**
  * HTTP Client that queries configured server for attributes of given useragent
  * 
  * @author w
  * 
  */
 public class OpenDDRHTTPClient {
 	
 	public String getAttributest(String useragent) throws ClientProtocolException, IOException {
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(getConnectionURL(useragent));
 		HttpResponse response = httpclient.execute(httpget);
 		HttpEntity entity = response.getEntity();
 		
 		String returnedJson = "{}";
 		
 		if (entity != null) {
 			returnedJson = EntityUtils.toString(entity);
 		}
 		return returnedJson;
 	}
 	
	public String getConnectionURL(String useragent){
		return "http://" + Configuration.SERVER_HOST + ":" + Configuration.SERVER_PORT + "/?" + Configuration.UA_PARAMETER + "=" + useragent;
 	}
 	
 	public void setServerhost(String hostname){
 		Configuration.setServerHost(hostname);
 	}
 
 }
