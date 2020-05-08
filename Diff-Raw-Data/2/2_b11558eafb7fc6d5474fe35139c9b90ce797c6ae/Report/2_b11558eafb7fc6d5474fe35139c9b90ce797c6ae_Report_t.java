 package controllers;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 import models.Token;
 import play.Logger;
 import play.libs.Json;
 import play.libs.WS;
 import play.libs.WS.Response;
 import play.libs.WS.WSRequestHolder;
 import play.mvc.Controller;
 import play.mvc.Result;
 import util.AuthorizationUtil;
 import constants.APIConstants;
 
 /**
  * Report controller for generating reports
  * 
  * @author tejawork
  *
  */
 public class Report extends Controller{
     
 	//constants
 	private static String PAGE_COUNT_PARAMETER_NAME = "page_count";
 	private static String PAGE_COUNT = "20";
 
 	//private static Token token = AuthorizationUtil.getNewToken();
 
     /**
      * Route: /reports/topAudience
      * <p>
      * This creates a request to Lotame API to get a list of Top Audience Report
      * data.
      * 
      * @return Returns Top Audience report as JSON object
      */
     public static Result topAudiences() {
     	
     	Logger.trace("Getting the top audience from Lotame API");
     	
     	Token token = AuthorizationUtil.getToken();
     	
     	//setup the request with headers
 		WSRequestHolder request = WS.url(APIConstants.LOTAME_TOP_AUDIENCE_URL);
 		request.setHeader(APIConstants.ACCEPT_HEADER, APIConstants.JSON_HEADER_TYPE);
 		request.setHeader(APIConstants.AUTHORIZATION_HEADER, token.tokenCode);
 		request.setQueryParameter(PAGE_COUNT_PARAMETER_NAME, PAGE_COUNT);
 	
 		Logger.debug(String.format("Request being sent to %s", APIConstants.LOTAME_TOP_AUDIENCE_URL));
 		
 		//extracting the response from the promise
 		Response response = request.get().get();
 		
 		//creating Jackson mapper
 		ObjectMapper mapper = new ObjectMapper();
 		JsonNode rootNode;
 		
 		try {
 			rootNode = mapper.readValue(response.asJson(), JsonNode.class);
 		} catch (Exception exception){
 			Logger.error("Json mapper has an error", exception);
 			
 			ObjectNode result = Json.newObject();
 			result.put(APIConstants.JSON_ERROR_KEY, APIConstants.JSON_500_MESSAGE);
 			
 			//returning a Http 500, when json mapper error occurs
			return badRequest(result);
 		}
 		
 		
 		JsonNode array = rootNode.get(APIConstants.LOTAME_JSON_TOP_AUDIENCE_STAT_VAR);	
 
         return ok(array);
     }
 }
