 package mdettlaff.comics.service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 public class HttpService {
 
 	private final HttpClient client;
 
 	public HttpService() {
 		client = new DefaultHttpClient();
 	}
 
	public BufferedReader read(String url) throws ClientProtocolException, IOException {
 		InputStream stream = getEntity(url).getContent();
 		return new BufferedReader(new InputStreamReader(stream));
 	}
 
 	public void closeReader(BufferedReader reader) {
 		IOUtils.closeQuietly(reader);
 		client.getConnectionManager().shutdown();
 	}
 
	public byte[] download(String url) throws ClientProtocolException, IOException {
 		try {
 			return EntityUtils.toByteArray(getEntity(url));
 		} finally {
 			client.getConnectionManager().shutdown();
 		}
 	}
 
	private HttpEntity getEntity(String url) throws IOException, ClientProtocolException {
		return client.execute(new HttpGet(url)).getEntity();
 	}
 }
