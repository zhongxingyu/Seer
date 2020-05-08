 package controllers;
 
 import external.InstagramParser;
 import helpers.TwitterHelper;
 import models.Feature;
 import play.api.templates.Html;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.index;
 
 public class Contents extends Controller{
 	
 	
 	
 	
 	
 	
 	
 	
 	public static Result contentOfFeature(String id)
 	{
 		Feature feature = Feature.find().byId(id);
 		
 		if (feature == null) {
 			return ok("This POI does not exist anymore.");
 		}
 		else {
 			String decString = feature.properties.get("description").toString();
 			
 
 			decString = decString.replaceAll("^\"|\"$", "");
 			String description = TwitterHelper.parse(decString, "Overlay");
 			String image = "";
 			
 			if (feature.properties.get("standard_resolution") != null) {
 				image  = "<div id=\"image-holder\"> " +
	                    "<img src="+feature.properties.get("standard_resolution").toString()+" alt=\"Smiley face\"  width=\"612\" height=\"612\" > " +
 	                    "</div> " ;
 			}
 			
 			Html content = new Html(image+description);
 			
 			
 			return ok(index.render(feature,content));
 			
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
 			if (feature.properties.get("standard_resolution") != null) {
 				image  = "<div id=\"image-holder\"> " +
 	                    "<img src="+feature.properties.get("standard_resolution").toString()+" alt=\"Smiley face\"  width=\"612\" height=\"612\" > " +
 	                    "</div> " ;
 			}
 			
 			Html content = new Html(image+description);
 			
 			
 			return ok(index.render(feature,content));
 		} catch (Exception e) {
 			return ok("This POI does not exist anymore.");
 		}
 		
 	}
 	
 
 
 }
