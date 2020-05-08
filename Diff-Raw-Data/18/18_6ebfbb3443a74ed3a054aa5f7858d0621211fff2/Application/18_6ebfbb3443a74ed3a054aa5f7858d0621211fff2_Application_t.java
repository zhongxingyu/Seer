 package controllers;
 
 import models.Piece;
 import models.PieceImage;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import play.api.templates.Html;
 import play.mvc.Controller;
 import play.mvc.Result;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import scala.collection.mutable.StringBuilder;
 
 public class Application extends Controller {
 
     public static Result home() {
         return direct("werk");
     }
 
     public static Result direct(String subsite) {
         return ok( views.html.main.render(getSubsite(subsite),getMeta(subsite)) );
     }
 
     public static Result directToImage(String subsite,String imageId) {
     	System.out.println(imageId);
         return ok( views.html.main.render(getSubsite(subsite),getMeta(subsite)) );
     }
 
     public static Result receiveSubsite(String subsite) {
         return ok( getSubsite(subsite) );
     }
 
     public static Result receiveMeta(String subsite) {
         return ok( getMeta(subsite) );
     }
 
     public static Html getSubsite(String subSiteName) {    	
         if(subSiteName.equalsIgnoreCase("werk")){
         	List<Piece> allPieces = Piece.find
                     .where()
                     .eq("kind","sier")
                     .findList();
                     
         	if(allPieces.isEmpty())
         		allPieces.add(createExamplePiece("sier"));
         	
             return views.html.werk.render(allPieces);
         }else if(subSiteName.equalsIgnoreCase("acc")){
         	List<Piece> allPieces = Piece.find
                     .where()
                     .eq("kind","acc")
                     .findList();
                     
         	if(allPieces.isEmpty())
         		allPieces.add(createExamplePiece("acc"));
         	
             return views.html.werk.render(allPieces);
         }else if(subSiteName.equalsIgnoreCase("willu")){
             return views.html.willu.render();
         }else if(subSiteName.equalsIgnoreCase("cont")){
             return views.html.cont.render();
         }
         
     	StringBuilder tmp = new StringBuilder();
     	tmp.append("404 - Page not found");
         return new Html(tmp);
     }
     
     public static Html getMeta(String subSiteName){
         if(subSiteName.equalsIgnoreCase("werk")){
             return views.html.meta.werk.render();
         }else if(subSiteName.equalsIgnoreCase("acc")){
             return views.html.meta.acc.render();
         }else if(subSiteName.equalsIgnoreCase("willu")){
             return views.html.meta.willu.render();
         }else if(subSiteName.equalsIgnoreCase("cont")){
             return views.html.meta.cont.render();
         }
         
     	StringBuilder tmp = new StringBuilder();
     	tmp.append("<!--400 - Internal server error-->");
         return new Html(tmp);
     }
 
     @SuppressWarnings("unchecked")
 	public static Result getItem(Long pieceId) {
 
         Piece piece = Piece.find.byId(pieceId);
         JSONObject jsonPiece = new JSONObject();
         JSONArray images = new JSONArray();
         jsonPiece.put("name",piece.name);
         jsonPiece.put("desc",piece.description);
         jsonPiece.put("kind",piece.kind);
         Iterator<PieceImage> itr = piece.getImages().iterator();
         while(itr.hasNext()){
             PieceImage image = itr.next();
             JSONObject img = new JSONObject();
             img.put("name",image.name);
             img.put("focus",image.focus.replace("=", ""));
             img.put("url",image.url);
             images.add(img);
         }
         jsonPiece.put("images",images);
 
         return ok(jsonPiece.toJSONString());
     }
     
     
   
    protected static Piece createExamplePiece(String kind){
 		Piece piece = new Piece();
 		List<PieceImage> sampleImages = new ArrayList<>();
         piece.name = "Example";
         piece.addeddate = "xx-xx-xx";
         piece.description = "This is just an example for when the database is empty, go to the admin screen and remove it from the db";
         piece.kind = kind;
         piece.thumbnail = "http://www.colourbox.com/preview/4207474-183718-sample-stamp-shows-example-symbol-or-taste.jpg";
         PieceImage img = new PieceImage();
         img.name = "Sample image";
         img.description = "And example of an image";
         img.focus = "50% 50%";
         img.setUrl("http://akhil.solutionsforstartup.com/wp-content/uploads/2013/07/sample.jpg");
         sampleImages.add(img);
         piece.images = sampleImages;
         piece.save();
         
         return piece;
     }
 }
