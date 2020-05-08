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
 
 public class AlbumsTest {
 	
 	private final String user = "cerealguy";
	private final String secret = "eau2Z1WczR7mwJsZWq/dYBBgD+FKN/UgfuymSFFdw2g=";
 	
 	@Test
 	public void HttpGetAlbumsTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpGetAlbumTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/bb305a10-8960-48dc-aba2-e176dd718c65/";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpUpdateAlbumTest() throws ClientProtocolException, IOException {
 		
 		String data = "{\"title\":\"Catuset\",\"userNick\":\"cerealguy\"}";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/51758be8-2d48-4567-8660-740173633da8/";
 		
 		HttpPut put = ApiHelper.assembleHttpPut(this.user, this.secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(put);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpCreateAlbumTest() throws ClientProtocolException, IOException {
 		
 		String data = "{\"title\":\"Master\",\"userNick\":\"cerealguy\"}";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/";
 		
 		HttpPost post = ApiHelper.assembleHttpPost(this.user, this.secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(post);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}	
 	
 	@Test
 	public void HttpDeleteAlbumTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/51758be8-2d48-4567-8660-740173633da8/";
 		
 		HttpDelete delete = ApiHelper.assembleHttpDelete(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(delete);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	} 
 
 	@Test
 	public void HttpGetAlbumsPhotosTest() throws ClientProtocolException, IOException {
 		
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/bb305a10-8960-48dc-aba2-e176dd718c65/photos/";
 		
 		HttpGet get = ApiHelper.assembleHttpGet(this.user, this.secret, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(get);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}
 	
 	@Test
 	public void HttpMovePhotoToAlbumTest() throws ClientProtocolException, IOException {
 		
 		String data = "";
 		String url = "http://127.0.0.1:8080/mdissapi/api/1.0/albums/24e161f9-eade-441d-976a-6807c0d69194/addPhoto/15ccb7bf-7260-4cc7-b797-13633a135e2e/";
 		
 		HttpPut post = ApiHelper.assembleHttpPut(this.user, this.secret, data, url);
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response = client.execute(post);
 		
 		String message = ApiHelper.inputStreamToOutputStream(response.getEntity().getContent()).toString();
 		
 		System.out.println(message);
 	}	
 }
