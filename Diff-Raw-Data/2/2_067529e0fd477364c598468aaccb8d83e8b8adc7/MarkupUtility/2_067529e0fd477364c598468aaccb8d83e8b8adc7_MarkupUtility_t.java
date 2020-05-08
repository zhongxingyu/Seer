 package sce.finalprojects.sceprojectbackend.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class MarkupUtility {
 	
 	private final static String COMMENTS_AMOUNT_MARKUP_JSON_VAR = "module";
 	private final static String PAGINATION_KEY_MARKUP_JSON_VAR = "more";
 	private final static String COMMENTS_AMOUNT_CONTAINER_SELECTOR = "#collapsed-comments-show";
	private final static String PAGINATION_KEY_CONTAINER_SELECTOR = ".generic-button";
 	private final static String USER_AGENT = "Mozilla/5.0";
 	
 	public static String  getCommentBodyFromMarkup(String markup){
 		Document doc = Jsoup.parse(markup);
 		Elements matchingElements  = doc.select(".comment-content");
 		
 		if(matchingElements.size() == 0) return null;
 		
 		Element elem =  matchingElements.get(0);
 		return elem.text();
 	}
 	
 	public static int getLatestCommentAmount(String url){
        // String url = "http://news.yahoo.com/_xhr/contentcomments/get_all/?content_id=40acf925-e8e2-37f9-9ad9-eb73d42274f1&_device=full&done=http%3A%2F%2Fnews.yahoo.com%2Fshots-fired-at-washington-navy-yard--u-s--navy-confirms-130407614.html&_media.modules.content_comments.switches._enable_view_others=1&_media.modules.content_comments.switches._enable_mutecommenter=1&enable_collapsed_comment=1";
         JSONParser parser = new JSONParser();
         try {
        
                 URL obj = new URL(url);
 
                 HttpURLConnection con = (HttpURLConnection) obj.openConnection();
  
                 con.setRequestMethod("GET");
                
                 //add request header
                 con.setRequestProperty("User-Agent", USER_AGENT);
 
                 int responseCode = con.getResponseCode();
 
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(con.getInputStream()));
                 String inputLine;
                 StringBuffer response = new StringBuffer();
  
                 while ((inputLine = in.readLine()) != null) {
                         response.append(inputLine);
                 }
                 in.close();
                
                 JSONObject jsonObject = (JSONObject) parser.parse(response.toString());
                
                 String commentsMarkup = (String) jsonObject.get(COMMENTS_AMOUNT_MARKUP_JSON_VAR);
                
                 Document doc = Jsoup.parse(commentsMarkup);
                 Elements elem =  doc.select(COMMENTS_AMOUNT_CONTAINER_SELECTOR);
                 String rawCommentAmount = elem.get(0).text();
                 Pattern p = Pattern.compile("\\d+");
                 Matcher m = p.matcher(rawCommentAmount);
                 StringBuilder sb = new StringBuilder();
                 while (m.find()) {
                 	sb.append(m.group());
                 }
                
                 return Integer.valueOf(sb.toString());
                
                
        
         } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
         } catch (org.json.simple.parser.ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         return -1;
 	}
 	
 	public static String getNextPaginationKey(String jsonResponse){
 		
 		try{
 	    		JSONParser parser = new JSONParser();
 	            JSONObject jsonObject;
 	    			jsonObject = (JSONObject) parser.parse(jsonResponse);
 
 	            
 	    	        String commentsMarkup = (String) jsonObject.get(PAGINATION_KEY_MARKUP_JSON_VAR);
 	    	       
 	    	        Document doc = Jsoup.parse(commentsMarkup);
 	    	        Elements selectedElements =  doc.select(PAGINATION_KEY_CONTAINER_SELECTOR);
 	    	        Element elem = selectedElements.get(0);
 	    	        String queryParams = elem.attr("data-query");
 	    	        Pattern pattern = Pattern.compile("%3A[a-zA-Z0-9-%]+&");
 	    	        Matcher matcher = pattern.matcher(queryParams);
 	    	        
 	    	        matcher.find();
 	    	        
 	    	        return matcher.group();
 	    		
 	       
 	        } catch (org.json.simple.parser.ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				
 			}
 	        
 	        return null;
 	}
 
 }
