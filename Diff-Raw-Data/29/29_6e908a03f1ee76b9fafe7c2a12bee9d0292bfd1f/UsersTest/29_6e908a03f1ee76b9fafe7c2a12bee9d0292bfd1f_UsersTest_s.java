 package org.mdissjava.api;
 
 import java.io.IOException;
 
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
 
 public class UsersTest {
 	
 	private final String user = "cerealguy";
	private final String secret = "h67U3OKYHlf0RzMBoG2gY+uahXxi8FEcH0HVMzpekWg=";
 
 	@Test
 	public void HttpGetUserFollowingsTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/users/cerealguy/following";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpGetUserFollowersTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/users/cerealguy/followers";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	
 	@Test
 	public void HttpGetUserTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/users/cerealguy/";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpUpdateUserTest() throws ClientProtocolException, IOException {
 		
 		String data = "{\"name\":\"Cruz\"}";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/users/cerealguy/";
 		
 		HttpPut put = ApiHelper.assembleHttpPut(this.user, this.secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(put);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	/*
 	@Test
 	public void HttpCreateUserTest() throws ClientProtocolException, IOException {
 		
 		String data = "{\"title\":\"Me\",\"userNick\":\"horl\"}";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/users/";
 		
 		HttpPost post = ApiHelper.assembleHttpPost(this.user, this.secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(post);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}	
 	*/
 	
 	@Test
 	public void HttpGetUserAlbumsTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/users/cerealguy/albums";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 
 }
