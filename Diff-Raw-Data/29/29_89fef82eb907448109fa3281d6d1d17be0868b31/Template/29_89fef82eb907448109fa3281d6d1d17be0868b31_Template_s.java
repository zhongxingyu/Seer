 package controllers;
 
 import static play.data.Form.form;
 
 import java.io.File;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import models.Base;
 import models.Base.LayoutJSON;
 import play.Logger;
 import play.libs.Json;
 import play.data.DynamicForm;
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 import play.mvc.Security;
 
 //@Security.Authenticated(Secured.class)
 public class Template extends Controller {
     
 	public static Result save() {
 	    final DynamicForm form = form().bindFromRequest();
             Base base = new Base();
             base.siteId = form.get("siteId");
             base.templateName = form.get("templateName");
 			base.bgColor = form.get("bgColor");
 			base.txtColor = form.get("txtColor");
             Base.save(base);
         return ok();
     }
 	
 	public static Result saveLayout() {
 		JsonNode json = request().body().asJson();
 //        Base base = new Base();
         LayoutJSON returnedContainer = Base.parseContainer(json); 
         Base.saveLayout(returnedContainer);
         return ok();
     }
 
 	public static Result siteSearch(String siteId) {
 		ObjectNode result = Json.newObject();
 //		Base base = new Base();
 		Base siteInfo = Base.siteSearch(siteId);
 		if(siteInfo == null) {
 			return null;	
 		} else {
 			result.put("siteId", siteId);
 			result.put("templateName", siteInfo.templateName);
 			result.put("bgColor", siteInfo.bgColor);
 			result.put("txtColor", siteInfo.txtColor);
 			return ok(result);
 		}
         
     }
 	
 	public static Result loadLayout(String siteId) {
//		Base base = new Base();
 		Base returnedLayout = Base.loadLayout(siteId);
		if(returnedLayout == null) {
 			Logger.debug("returnedLayout is null");
 			return badRequest();	
 		} else {
 			Logger.debug("returnedLayout ok");
 			Logger.debug(returnedLayout.siteId);
 			return ok(Json.toJson(returnedLayout));
 		}
         
     }
 	
 	public static Result fileUpload() {
 		  MultipartFormData body = request().body().asMultipartFormData();
 		  FilePart picture = body.getFile("picture");
 		  if (picture != null) {
 		    String fileName = picture.getFilename();
 		    String contentType = picture.getContentType(); 
 		    File file = picture.getFile();
 		    Logger.debug("file upload info:");
 		    Logger.debug(contentType);
 		    Logger.debug(fileName);
 		    return ok("File uploaded");
 		  } else {
 		    flash("error", "Missing file");
 		    return redirect(routes.Application.index());    
 		  }
     }
 }
