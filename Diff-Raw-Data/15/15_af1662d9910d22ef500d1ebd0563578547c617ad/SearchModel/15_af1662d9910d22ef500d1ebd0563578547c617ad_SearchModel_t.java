 package cs169.project.thepantry;
 
 import java.net.URLEncoder;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONObject;
 
 /* SearchModel searches the Yummly API using a SearchCriteria and returns a SearchResult
  * 
  */
 public class SearchModel {
 	
 	private String API_ID = "a169c318";
 	private String API_KEY = "4778f72efbcabe0f761efefd92afe5b0";
 	int TIMEOUT_MILLISEC = 10000;
 	
 	// some default URL Strings
 	String URL_BASE    = "http://api.yummly.com/v1/api";
 	String URL_GET     = URL_BASE + "/recipe/";
     String URL_SEARCH  = URL_BASE + "/recipes";
     String URL_META    = URL_BASE + "/metadata";
     
     String type;
     String q;
     int maxResults;
     int resultsToSkip;
 	
 	public Storage search(SearchCriteria sc) {
 		
 		//the first string is the type of request, either "recipe" (get recipe), "search" (search for recipe),
 		//"home", to generate recommended recipes for the homepage
 		//the second string passed in is either the recipe ID or the search query q in String format
 		//this can be extended to multiple strings to search for individual ingredients, allergies, cuisines, etc.
 		this.type = sc.type;
 		this.q = sc.q;
 		this.maxResults = sc.maxResults;
 		this.resultsToSkip = sc.resultsToSkip;
 		
     	try {
     		// generate the correct URL for the get request
     		// create http parameters for the get request that will be appended to the URL in the request
     		String getURL;
         	HttpParams httpParams = new BasicHttpParams();
     		if (type == "recipe") {
     			//q should be correctly encoded in this case b/c recipe search is only called by us
     			getURL = URL_GET + q;
     		}
     		else if (type == "search" || type == "home") {
     			//parse query for mulitple ingredients separated by commas
     			//right now the first thing is treated as a normal search
     			getURL = URL_SEARCH;
     			String[] qs = q.replaceAll(",\\s", ",").split(",");
     			getURL += "?q=" + URLEncoder.encode(qs[0], "UTF-8");
     			for (int i=1; i<qs.length; i++) {
     				getURL += "&allowedIngredient%5B%5D=" + URLEncoder.encode(qs[i], "UTF-8");
     			}
     			getURL += "&maxResult=" + maxResults;
     			getURL += "&start=" + resultsToSkip;
     		}
     		else {
     			// TODO throw invalid request exception?
     			getURL = "";
     		}
     		//create an http connection and client
     		//set timeout for connection and socket
         	HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
         	HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
         	HttpClient client = new DefaultHttpClient(httpParams);
     		
         	// create the get request and set authorization headers
             HttpGet httpget = new HttpGet(getURL);
             httpget.setHeader("X-Yummly-App-ID", API_ID);
             httpget.setHeader("X-Yummly-App-Key", API_KEY);
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             
     		// issue the get request to the Yummly server
             // create a Storage object out of the JSON response
             String responseBody = client.execute(httpget, responseHandler);
             JSONObject jsonResponse = new JSONObject(responseBody);
             Storage result;
             if (type == "recipe") {
             	result = new Recipe(jsonResponse);
             } else if (type == "search" || type == "home") {
             	result = new SearchResult(jsonResponse);
             } else {
             	// TODO invalid request
            	result = null;//new Recipe(new JSONObject());
             }
             return result;
             
     	} catch (Exception e) {
     		// TODO return some kind of default results (not this though it's sketch)
     		e.printStackTrace();
    		//Storage defaultresult = new Recipe(new JSONObject());
    		return null;//defaultresult;
         }
 	}
 }
