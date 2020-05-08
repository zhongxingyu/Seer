 package controllers;
 
 
 import static play.libs.Json.toJson;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jackson.JsonNode;
 
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 import com.mongodb.MongoException;
 import com.mongodb.gridfs.GridFSDBFile;
 
 import models.*;
 
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Results;
 import play.mvc.Http.MultipartFormData.FilePart;
 import utils.GridFsHelper;
 
 
 /**
  * @author Muhammad Fahied
  */
 
 public class SImages extends Controller {
 	
 //	public static Result showBlank(){
 //		return ok(form.render(productForm));
 //		}
 	
 	
 	
 	
 	
 
 	public static Result fetchImagesById(String imageId) {
 
 		SGroup group = SGroup.find.filter("simages.id", imageId).get();
 		SImage res = null;
 		for (SImage p : group.simages) {
 			if (p.id.equals(imageId)) {
 				res = p;
 				break;
 			}
 		}
 
 		if (res == null) {
 			return ok("{}");
 		}
 		return ok(toJson(res));
 
 	}
 
 	
 	
 	
 	
 	
 	
 	public static Result fetchImagesByGroupId(String groupId) {
 		SGroup group = SGroup.find.byId(groupId);
 		List<SImage> images = group.simages;
 		if (images == null)
 			return ok("[]");
 		else
 			return ok(toJson(images));
 	}
 
 
 	
 	
 	
 	
 	
 	
 	public static Result addImage(String groupId, String taskId) {
 
 		FilePart filePart = ctx().request().body().asMultipartFormData().getFile("picture");
 		SImage image = null;
 
 		if (filePart.getFile() == null)
 			return ok(toJson("{status: No Image found}"));
 		try {
 			image = new SImage(filePart.getFile(),filePart.getFilename(),filePart.getContentType(), taskId);
 			SGroup group = SGroup.find.byId(groupId);
 
 			if (group.simages == null) {
 				group.simages = new ArrayList<SImage>();
 			}
 
 			group.addImage(image);
 			group.save();
 		} catch (IOException e) {
 			flash("uploadError", e.getMessage());
 		}
 		return ok(toJson(image));
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 	public static Result showImage(String imageId) throws IOException {
 		
 		GridFSDBFile file = GridFsHelper.getFile(imageId);
 		
 		byte[] bytes = IOUtils.toByteArray(file.getInputStream());
 		
 		return Results.ok(bytes).as(file.getContentType());
 		
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	// {"imageId":"3423j342kjl23h1", "wxpos":120, "wypos":32}
 	public static Result updateImageOnWeb(String imageId ) {
 		
 		JsonNode node = ctx().request().body().asJson();
 		
 		int wxpos = node.get("wxpos").asInt();
		int wypos = node.get("wypos").asInt();
 
 		SGroup group = SGroup.find.filter("simages.id",imageId ).get();
 		
 		Query<SGroup> query = group.datastore.createQuery(SGroup.class)
 				.field("simages.id").equal(imageId);
 		UpdateOperations<SGroup> ops = group.datastore.createUpdateOperations(SGroup.class).disableValidation()
 				.set("simages.$.wxpos", wxpos)
 				.set("simages.$.wypos", wypos);
 		group.datastore.update(query, ops);
 
 		SImage image = null;
 		for (SImage p : group.simages) {
 			if (p.id.equals(imageId)) {
 				image = p;
 				break;
 			}
 		}
 		return ok(toJson(image));
 	}
 
 	
 	
 	
 	public static Result deleteImageById(String imageId) throws MongoException, IOException {
 
 		SGroup group = SGroup.find.filter("simages.id", imageId).get();
 		// Second locate the fruit and remove it:
 		for (SImage p : group.simages) 
 		{
 			if (p.id.equals(imageId)) 
 			{
 				//delete file from gridFS
 				p.deleteImage(p.fileId);
 				//Remove meta info and Group Document
 				group.simages.remove(p);
 				group.save();
 				break;
 			}
 		}
 		
 		return ok("deleted successfully");
 	}
 		
 	
 	
 	
 	
 	
 	public static Result postCommentOnImage() {
 
 		JsonNode node = ctx().request().body().asJson();
 		String imageId = node.get("imageId").asText();
 		String content = node.get("content").asText();
 
 		SGroup group = SGroup.find.filter("simages.id", imageId).get();
 		// Second locate the fruit and remove it:
 		SComment comment = new SComment(content);
 		// update member of embedded object list
 		Query<SGroup> query = group.datastore.createQuery(SGroup.class)
 				.field("simages.id").equal(imageId);
 		UpdateOperations<SGroup> ops = group.datastore.createUpdateOperations(SGroup.class).disableValidation()
 				.add("simages.$.scomments", comment);
 		group.datastore.update(query, ops);
 
 		SImage image = null;
 		for (SImage p : group.simages) {
 			if (p.id.equals(imageId)) {
 				image = p;
 				break;
 			}
 		}
 		return ok(toJson(image));
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	public static Result fetchCommentsByImage(String imageId) {
 
 		SGroup group = SGroup.find.filter("simages.id", imageId).get();
 
 		List<SComment> comments = null;
 		for (SImage p : group.simages) {
 			if (p.id.equals(imageId)) {
 				comments = p.scomments;
 				break;
 			}
 
 		}
 
 		if (comments == null) {
 			return ok("[]");
 		}
 		return ok(toJson(comments));
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	public static String getFileExtension(String filePath){  
 		  StringTokenizer stk=new StringTokenizer(filePath,".");  
 		  String FileExt="";  
 		  while(stk.hasMoreTokens()){  
 		   FileExt=stk.nextToken();  
 		  }  
 		  return FileExt;  
 		 } 
 	
 
 }
