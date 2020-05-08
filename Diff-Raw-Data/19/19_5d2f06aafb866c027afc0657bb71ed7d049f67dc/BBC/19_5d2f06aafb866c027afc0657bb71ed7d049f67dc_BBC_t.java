 package com.hackeurope.feedspeak;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
  *
  * @author Calum
  */
 public class BBC {
    private static final String BBC_YQL_URL = "http://query.yahooapis.com/v1/public/yql?q=SELECT%20title%20From%20rss%20Where%20url%3D'http%3A%2F%2Ffeeds.bbci.co.uk%2Fnews%2Frss.xml'%20LIMIT%2020&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
     
     public static List<String> getNewsHeadlines()
     {
         try {
 			
             //Make connection
             HttpClient client = new DefaultHttpClient();
             HttpGet request = new HttpGet(BBC_YQL_URL);
             HttpParams params = request.getParams();
             HttpConnectionParams.setSoTimeout(params, 30000);
             request.setParams(params);
 
 
             HttpResponse response = client.execute(request);
             InputStreamReader inputStream = new InputStreamReader(response.getEntity().getContent());
 
             StringWriter sw = new StringWriter();
             IOUtils.copy(inputStream, sw);
 
             return parseJson(sw.toString());    //Returns list of headlines
 
         } 
         catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ParseException pe) {
             pe.printStackTrace();
             System.err.println("Error parsing BBC Json");
         }
         return null;
     }
     
     private static List<String> parseJson(String json) throws ParseException {
 
         ArrayList<String> headlines = new ArrayList<String>();
         JSONObject jsonObject = new JSONObject(json);
                 
        JSONObject query = jsonObject.getJSONObject("query");
        JSONObject results = query.getJSONObject("results");
        JSONArray jsonArray = results.getJSONArray("item");
        
         for(int i=0; i<jsonArray.length(); i++){
             JSONObject currentHeadline = jsonArray.getJSONObject(i);
             headlines.add(currentHeadline.getString("title"));
         }
 
         return headlines;
     }
 }
