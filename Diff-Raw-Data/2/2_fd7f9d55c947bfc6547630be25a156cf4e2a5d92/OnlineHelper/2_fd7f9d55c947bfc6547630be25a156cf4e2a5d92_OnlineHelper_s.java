 
 package ualberta.g12.adventurecreator.online;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import ualberta.g12.adventurecreator.data.Story;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 
 public class OnlineHelper {
 
     // Http Connector
     private HttpClient httpclient = new DefaultHttpClient();
 
     // JSON Utilities
     private Gson gson = new Gson();
 
     private static String testServer = "http://cmput301.softwareprocess.es:8080/testing/";
     private static String ourServer = "http://cmput301.softwareprocess.es:8080/cmput301f13t12/";
 
     /**
      * Consumes the POST/Insert operation of the service
      * 
      * @throws IOException
      * @throws IllegalStateException
      */
     public void insertStory(Story story) throws IllegalStateException, IOException {
         HttpPost httpPost = new HttpPost(ourServer + "stories/" + story.getId());
         StringEntity stringentity = null;
         try {
             stringentity = new StringEntity(gson.toJson(story));
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         httpPost.setHeader("Accept", "application/json");
 
         httpPost.setEntity(stringentity);
         HttpResponse response = null;
         try {
             response = httpclient.execute(httpPost);
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         String status = response.getStatusLine().toString();
         System.out.println(status);
         HttpEntity entity = response.getEntity();
         BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
         String output;
         System.err.println("Output from Server -> ");
         while ((output = br.readLine()) != null) {
             System.err.println(output);
         }
 
         try {
             entity.consumeContent();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         // httpPost.releaseConnection();
     }
 
     /**
      * Gets the story by the id from our server
      * 
      * @param storyID is the id in which we are searching for ( should be unique
      *            also)
      */
     public Story getStory(int storyId) {
         Story story = null;
         try {
             // HttpGet getRequest = new
             // HttpGet("http://cmput301.softwareprocess.es:8080/testing/lab02/999?pretty=1");//S4bRPFsuSwKUDSJImbCE2g?pretty=1
            HttpGet getRequest = new HttpGet(ourServer + storyId + "?pretty=1");// S4bRPFsuSwKUDSJImbCE2g?pretty=1
             getRequest.addHeader("Accept", "application/json");
 
             HttpResponse response = httpclient.execute(getRequest);
 
             String status = response.getStatusLine().toString();
             System.out.println(status);
 
             String json = getEntityContent(response);
 
             // We have to tell GSON what type we expect
             Type elasticSearchResponseType = new TypeToken<ElasticSearchResponse<Story>>() {
             }.getType();
             // Now we expect to get a Recipe response
             ElasticSearchResponse<Story> esResponse = gson
                     .fromJson(json, elasticSearchResponseType);
             // We get the recipe from it!
             story = esResponse.getSource();
             System.out.println(story.toString());
             // getRequest.releaseConnection();
 
         } catch (ClientProtocolException e) {
 
             e.printStackTrace();
 
         } catch (IOException e) {
 
             e.printStackTrace();
         }
 
         return story;
     }
 
     /**
      * Obtains all story objects stored in the server
      * 
      * @return
      */
     public ArrayList<Story> getAllStories() {
         ArrayList<Story> allStories = new ArrayList<Story>();
         try {
             // HttpGet getRequest = new
             // HttpGet("http://cmput301.softwareprocess.es:8080/testing/lab02/999?pretty=1");//S4bRPFsuSwKUDSJImbCE2g?pretty=1
             HttpGet getRequest = new HttpGet(ourServer + "_search?pretty=1");// S4bRPFsuSwKUDSJImbCE2g?pretty=1
             getRequest.addHeader("Accept", "application/json");
 
             HttpResponse response = httpclient.execute(getRequest);
 
             String status = response.getStatusLine().toString();
             System.out.println(status);
 
             String json = getEntityContent(response);
 
             // We have to tell GSON what type we expect
             Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<Story>>() {
             }.getType();
             // Now we expect to get a Recipe response
             ElasticSearchSearchResponse<Story> esResponse = gson.fromJson(json,
                     elasticSearchSearchResponseType);
             // We get the recipe from it!
             for (ElasticSearchResponse<Story> s : esResponse.getHits()) {
                 Story story = s.getSource();
                 allStories.add(story);
                 System.out.println(story.toString());
             }
 
             // getRequest.releaseConnection();
 
         } catch (ClientProtocolException e) {
 
             e.printStackTrace();
 
         } catch (IOException e) {
 
             e.printStackTrace();
         }
 
         return allStories;
     }
 
     /**
      * Obtains all stories from the server but is only a partial representation
      * of the object Will search only the storyTitle, author and Id of a story
      * This method is used to quickly find all the different stories in our
      * server without having to also downloading all the potentially large media
      * associated with it
      * 
      * @return
      * @throws ClientProtocolException
      * @throws IOException
      */
     public ArrayList<Story> getAllStoryTitlesIdAuthor() throws ClientProtocolException, IOException {
 
         ArrayList<Story> resultList = new ArrayList<Story>();
 
         // lets prepare our query to only get our required fields
         HttpPost searchRequest = new HttpPost(ourServer + "_search?pretty=1");
         String query = "{\"fields\" : [\"storyTitle\",\"author\", \"id\"], \"query\" :{ \"match_all\" : {}    }}";
         StringEntity stringentity = new StringEntity(query);
 
         // set our header let it know json!
         searchRequest.setHeader("Accept", "application/json");
         searchRequest.setEntity(stringentity);
 
         // get our json string back ..
         HttpResponse response = httpclient.execute(searchRequest);
         String json = getEntityContent(response);
 
         Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<Story>>() {
         }.getType();
         ElasticSearchSearchResponse<Story> esResponse = gson.fromJson(json,
                 elasticSearchSearchResponseType);
 
         // now we want to change all our response objects back into stories by
         // obtaining the fields we got back
         for (ElasticSearchResponse<Story> s : esResponse.getHits()) {
             Story story = s.getFields();
             resultList.add(story);
         }
         return resultList;
     }
 
     /**
      * checks if there is already a story with the same id in our database This
      * function can be used to check for a storyId already on the system to
      * prevent the case of accidental overwriting
      * 
      * @param id int value which is our story id ( should be unique)
      * @return true if a story with id exists, else false
      * @throws ClientProtocolException
      * @throws IOException
      */
     public boolean checkId(int id) throws ClientProtocolException, IOException {
 
         // lets prepare our query to only get our required fields
         HttpPost searchRequest = new HttpPost(ourServer + "_search?pretty=1");
         String query = "{\"fields\" : [\"id\"], \"query\" :{ \"term\" : { \"id\" : \"" + id
                 + "\"}    }  }";
         StringEntity stringentity = new StringEntity(query);
 
         // set our header let it know json!
         searchRequest.setHeader("Accept", "application/json");
         searchRequest.setEntity(stringentity);
 
         // get our json string back ..
         HttpResponse response = httpclient.execute(searchRequest);
         System.out.println(response.getStatusLine());
         String json = getEntityContent(response);
         Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<Story>>() {
         }.getType();
         ElasticSearchSearchResponse<Story> esResponse = gson.fromJson(json,
                 elasticSearchSearchResponseType);
         if (esResponse.getHits().size() > 0)
             return true;
         else
             return false;
     }
 
     /**
      * search by keywords
      */
     public ArrayList<Story> searchStories(String str) throws ClientProtocolException, IOException {
         // HttpGet searchRequest = new
         // HttpGet("http://cmput301.softwareprocess.es:8080/testing/lab02/_search?pretty=1&q="
         // +
         // java.net.URLEncoder.encode(str,"UTF-8"));
         ArrayList<Story> resultList = new ArrayList<Story>();
         HttpGet searchRequest = new HttpGet(ourServer + "_search?pretty=1");
         searchRequest.setHeader("Accept", "application/json");
         HttpResponse response = httpclient.execute(searchRequest);
         String status = response.getStatusLine().toString();
         System.out.println(status);
 
         String json = getEntityContent(response);
 
         Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<Story>>() {
         }.getType();
         ElasticSearchSearchResponse<Story> esResponse = gson.fromJson(json,
                 elasticSearchSearchResponseType);
         System.err.println(esResponse);
         for (ElasticSearchResponse<Story> s : esResponse.getHits()) {
             Story story = s.getSource();
             resultList.add(story);
             System.err.println(story);
         }
         return resultList;
         // searchRequest.releaseConnection();
     }
 
     /**
      * advanced search (logical operators)
      */
     public ArrayList<Story> searchsearchStories(String str) throws ClientProtocolException,
             IOException {
         ArrayList<Story> resultList = new ArrayList<Story>();
         HttpPost searchRequest = new HttpPost(ourServer + "_search?pretty=1");
         String query = "{\"query\" : {\"query_string\" : {\"default_field\" : \"ingredients\",\"query\" : \""
                 + str + "\"}}}";
         StringEntity stringentity = new StringEntity(query);
 
         searchRequest.setHeader("Accept", "application/json");
         searchRequest.setEntity(stringentity);
 
         HttpResponse response = httpclient.execute(searchRequest);
         String status = response.getStatusLine().toString();
         System.out.println(status);
 
         String json = getEntityContent(response);
 
         Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<Story>>() {
         }.getType();
         ElasticSearchSearchResponse<Story> esResponse = gson.fromJson(json,
                 elasticSearchSearchResponseType);
         System.err.println(esResponse);
         for (ElasticSearchResponse<Story> s : esResponse.getHits()) {
             Story story = s.getSource();
             resultList.add(story);
             System.err.println(story);
         }
         return resultList;
         // searchRequest.releaseConnection();
     }
 
     /**
      * delete an entry specified by the id
      */
     public void deleteStories() throws IOException {
         // HttpDelete httpDelete = new
         // HttpDelete("http://cmput301.softwareprocess.es:8080/testing/lab02/1");
         HttpDelete httpDelete = new HttpDelete(ourServer + "/1");
         httpDelete.addHeader("Accept", "application/json");
 
         HttpResponse response = httpclient.execute(httpDelete);
 
         String status = response.getStatusLine().toString();
         System.out.println(status);
 
         HttpEntity entity = response.getEntity();
         BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
         String output;
         System.err.println("Output from Server -> ");
         while ((output = br.readLine()) != null) {
             System.err.println(output);
         }
         entity.consumeContent();
 
         // httpDelete.releaseConnection();
     }
 
     /**
      * get the http response and return json string
      */
     String getEntityContent(HttpResponse response) throws IOException {
         BufferedReader br = new BufferedReader(
                 new InputStreamReader((response.getEntity().getContent())));
         String output;
         System.err.println("Output from Server -> ");
         String json = "";
         while ((output = br.readLine()) != null) {
             System.err.println(output);
             json += output;
         }
         // System.err.println("JSON:"+json);
         return json;
     }
 
 }
