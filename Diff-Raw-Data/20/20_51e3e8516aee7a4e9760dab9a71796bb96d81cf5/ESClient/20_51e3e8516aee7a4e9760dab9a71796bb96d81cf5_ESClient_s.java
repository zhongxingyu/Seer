 package ca.dreamteam.newrecipebook.Helpers.ElasticSearch;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import ca.dreamteam.newrecipebook.Models.Recipe;
 
 import com.google.gson.Gson;
 
 //This code was modified from it's original form as hosted on : https://github.com/rayzhangcl/ESDemo
 public class ESClient {
 	
 	private HttpClient httpClient = new DefaultHttpClient();
 	
 	private Gson gson = new Gson();
 
 	public void insertRecipe(Recipe recipe) throws IOException, IllegalStateException{
 		HttpPost httpPost = new HttpPost("http://cmput301.softwareprocess.es:8080/testing/"+recipe.getId());
 		StringEntity stringEntity = null;
 		try {
 			stringEntity = new StringEntity(gson.toJson(recipe));
 		} catch (UnsupportedEncodingException e) {
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		
 		httpPost.setHeader("Accept", "application/json");
 		
 		httpPost.setEntity(stringEntity);
 		HttpResponse response = null;
 		try {
 			response = httpClient.execute(httpPost);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		HttpEntity entity = response.getEntity();
 		//BufferedReader bReader = new BufferedReader(new InputStreamReader(entity.getContent()));
 		try {
			EntityUtils.consume(entity);
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 	}
 }
