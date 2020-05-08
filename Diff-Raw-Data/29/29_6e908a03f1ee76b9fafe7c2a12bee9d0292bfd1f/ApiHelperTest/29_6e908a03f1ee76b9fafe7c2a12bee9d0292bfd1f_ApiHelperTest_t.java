 package org.mdissjava.api;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.junit.Test;
 import org.mdissjava.api.helpers.ApiHelper;
 
 public class ApiHelperTest {
 
 	private String user = "cerealguy";
	private String secret = "h67U3OKYHlf0RzMBoG2gY+uahXxi8FEcH0HVMzpekWg=";
 	
 	@Test
 	public void HttpGetTest() throws ClientProtocolException, IOException {
 		String user = "slok";
 		String secret = "32d9737523ec594fc9b007643b162011863c1024";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/bb305a10-8960-48dc-aba2-e176dd718c65/";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(user, secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpPostTest() throws ClientProtocolException, IOException {
 		String data = "{\"title\":\"Me\",\"userNick\":\"horl\"}";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/";
 		
 		HttpPost post = ApiHelper.assembleHttpPost(user, secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(post);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}	
 	
 	@Test
 	public void HttpPutTest() throws ClientProtocolException, IOException {
 		String data = "{\"title\":\"Catuset\",\"userNick\":\"cerealguy\"}";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/51758be8-2d48-4567-8660-740173633da8/";
 		
 		HttpPut put = ApiHelper.assembleHttpPut(user, secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(put);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpDeleteTest() throws ClientProtocolException, IOException {
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/bb305a10-8960-48dc-aba2-e176dd718c65/";
 		
 		HttpDelete delete = ApiHelper.assembleHttpDelete(user, secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(delete);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	} 
 	
 	@Test
 	public void HttpPostContentTest() throws ClientProtocolException, IOException {
 		File f = new File("/home/slok/Desktop.png");
 		byte[] data = FileUtils.readFileToByteArray(f);
 		String url = "http://localhost:8080/mdissapi/api/1.0/photo-content/";
 		
 		HttpPost post = ApiHelper.assembleHttpPostData(user, secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(post);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 
 }
