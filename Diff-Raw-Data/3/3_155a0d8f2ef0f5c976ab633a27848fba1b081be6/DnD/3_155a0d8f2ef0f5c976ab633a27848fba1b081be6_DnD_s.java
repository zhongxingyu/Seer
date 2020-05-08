 package controllers;
 
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 import play.libs.WS.WSRequest;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.utils.HTTP;
 import sun.net.www.http.HttpClient;
 
 import java.util.List;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import canvas.CanvasRequest;
 import canvas.SignedRequest;
 
 public class DnD extends Controller {
 
     public static void index() {
     	// Get our secret from the environment, we use this to access and verify the request
         String yourConsumerSecret=System.getenv("CANVAS_CONSUMER_SECRET");
         
     	// Verify the request and decode into concrete object
         CanvasRequest cReq = SignedRequest.verifyAndDecode(params.get("signed_request"), yourConsumerSecret);
     	
         // Sample of running a query using canvas context
     	String jsonQueryResult = doAQuery(cReq);
     	JsonArray records = (new JsonParser()).parse(jsonQueryResult).getAsJsonObject().getAsJsonArray("records");
 
     	// Cause the index.html page to be rendered passing in the requet json string for js use
     	// and the records that came back from the web service call.
    	render(cReq.getJsonRequest(), records);
     }
     
     private static String doAQuery(CanvasRequest cReq) {
     	// Create an instance of WSRequest constructing the queryURL
     	WSRequest req = WS.url(cReq.getInstanceUrl() + cReq.getContext().getLinkContext().getQueryUrl() + "?q=Select Id, Name From Account");
     	
     	// Set the authentication header using oauth token from request
     	req.setHeader("Authorization", "OAuth " + cReq.getOauthToken());
     	
     	// Make the request and return the json representation
     	return req.get().getJson().toString();
     }
     
 }
