 package no.koteng.paxTube;
 
 import com.google.api.client.googleapis.json.GoogleJsonResponseException;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson2.JacksonFactory;
 import com.google.api.services.youtube.YouTube;
 import com.google.api.services.youtube.model.SearchListResponse;
 import com.google.api.services.youtube.model.SearchResult;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /*
 * This class is more or less written by Google.
 * See https://developers.google.com/youtube/v3/docs/search/list for source.
 * */
 public class Search {
     private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
     private static final JsonFactory JSON_FACTORY = new JacksonFactory();
     private static final long NUMBER_OF_VIDEOS_RETURNED = 10;
 
     public static List<SearchResult> doSearch(String queryTerm) {
         List<SearchResult> searchResultList = new ArrayList<SearchResult>();
 
         try {
             YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                 public void initialize(HttpRequest request) throws IOException {
                 }
             }).setApplicationName(Settings.YOUTUBE_APPLICATION_NAME).build();
 
             YouTube.Search.List search = youtube.search().list("id,snippet");
 
             String apiKey = Settings.YOUTUBE_API_KEY;
             search.setKey(apiKey);
             search.setQ(queryTerm);
             search.setType("video");
             search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
             search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
             SearchListResponse searchResponse = search.execute();
 
             searchResultList = searchResponse.getItems();
         } catch (GoogleJsonResponseException e) {
             System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                     + e.getDetails().getMessage());
         } catch (IOException e) {
             System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
         } catch (Throwable t) {
             t.printStackTrace();
         }
 
         return searchResultList;
     }
 }
