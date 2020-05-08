 package controllers;
 
 import org.codehaus.jackson.JsonNode;
 
 import external.InstagramParser;
 import helpers.TwitterHelper;
 import models.Feature;
 import models.User;
 import play.api.templates.Html;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.index;
 import static play.libs.Json.toJson;
 
 public class Contents extends Controller{
 	
 	
 	
 	
 	
 	
 	
 	public static Result contentOfFeature(String id) {
 		Feature feature = Feature.find().byId(id);
 
 		if (feature == null) {
 			return ok("This POI does not exist anymore.");
 		} else {
 
 			String decString = feature.properties.get("description").toString();
 
 			decString = decString.replaceAll("^\"|\"$", "");
 			String description = TwitterHelper.parse(decString, "Overlay");
 			String image = "";
 			User user = new User();
 
 			if (feature.properties.get("standard_resolution") != null) {
 				image = "<div id=\"image-holder\"> "
 						+ "<img src="
 						+ feature.properties.get("standard_resolution")
 								.toString()
 						+ " alt=\"Smiley face\"  width=\"612\" > " + "</div> ";
 			}
 			
 			if (feature.properties.get("user") != null) {
 				
 				JsonNode userNode = toJson(feature.properties.get("user"));
 				user.id =userNode.get("id").asText();
 				user.full_name = userNode.get("full_name").asText();;
 				
 			}
 
 			Html content = new Html(image + description);
 
 			return ok(index.render(user, content));
 
 		}
 
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static Result contentOfInstaPOI(String id)
 	{
 		Feature feature;
 		try {
 			feature = InstagramParser.getInstaByMediaId(id);
 			
 			
 			String decString = feature.properties.get("description").toString();
 			decString = decString.replaceAll("^\"|\"$", "");
 			String description = TwitterHelper.parse(decString, "Instagram");
 			
 			String image = "";
			User user = new User();
			
 			if (feature.properties.get("standard_resolution") != null) {
 				image  = "<div id=\"image-holder\"> " +
 	                    "<img src="+feature.properties.get("standard_resolution").toString()+" alt=\"Smiley face\"  width=\"612\" height=\"612\" > " +
 	                    "</div> " ;
 			}
 			
			if (feature.properties.get("user") != null) {
				
				JsonNode userNode = toJson(feature.properties.get("user"));
				user.id =userNode.get("id").asText();
				user.full_name = userNode.get("full_name").asText();;
				
			}

			Html content = new Html(image + description);

			return ok(index.render(user, content));
 		} catch (Exception e) {
 			return ok("This POI does not exist anymore.");
 		}
 		
 	}
 	
 
 
 }
