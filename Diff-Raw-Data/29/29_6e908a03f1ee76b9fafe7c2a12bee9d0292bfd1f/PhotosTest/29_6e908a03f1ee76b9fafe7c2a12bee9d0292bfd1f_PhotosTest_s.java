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
 
 public class PhotosTest {
 	
 	private final String user = "cerealguy";
	private final String secret = "h67U3OKYHlf0RzMBoG2gY+uahXxi8FEcH0HVMzpekWg=";
 	
 	@Test
 	public void HttpGetPhotoTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/photos/fcf6a3a3-77e8-4a0f-8ce5-cd9aaf9b5e0d/";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpDeletePhotoTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/photos/3002ee16-43b2-49f1-97dd-5356293f8845/";
 		
 		HttpDelete delete = ApiHelper.assembleHttpDelete(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(delete);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	} 
 	
 	@Test
 	public void HttpCreatePhotoTest() throws ClientProtocolException, IOException {
 		
 		String data = "{\"title\":\"Foto de Prueba\",\"photoId\":\"de324fe2-778d-4a0f-5fe6-e679023098ea\",\"dataId\":\"de324fe2-778d-4a0f-5fe6-e679023098ea\"}";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/photos/";
 		
 		HttpPost post = ApiHelper.assembleHttpPost(this.user, this.secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(post);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpUpdatePhotoTest() throws ClientProtocolException, IOException {
 		
 		String data = "{\"title\":\"Fajitassss!\"}";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/photos/fcf6a3a3-77e8-4a0f-8ce5-cd9aaf9b5e0d/";
 		
 		HttpPut put = ApiHelper.assembleHttpPut(this.user, this.secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(put);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpGetPhotoAlbumTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/photos/fcf6a3a3-77e8-4a0f-8ce5-cd9aaf9b5e0d/album/";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 
 }
