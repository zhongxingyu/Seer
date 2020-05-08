 package controllers;
 
 import static play.libs.Json.toJson;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jackson.JsonNode;
 
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 import com.mongodb.MongoException;
 import com.mongodb.gridfs.GridFSDBFile;
 
 import models.*;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Results;
 import play.mvc.Http.MultipartFormData.FilePart;
 import scala.util.control.Exception.Finally;
 import utils.GridFsHelper;
 
 /**
  * @author Muhammad Fahied
  */
 
 public class SImages extends Controller {
 
 	
 	
 	
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
 
 	
 	
 	
 	
 	
 	//
 	// private static final Form<SImage> uploadForm = form(SImage.class);
 	//
 	// public static Result showBlank(){
 	// return ok(upload.render(uploadForm));
 	// }
 
 	public static Result addImage(String groupId, String taskId, String runId) {
 
 		FilePart filePart = ctx().request().body().asMultipartFormData()
 				.getFile("picture");
 		SImage image = null;
 
 		if (filePart.getFile() == null)
 			return ok(toJson("{status: No Image found}"));
 		try {
 			image = new SImage(filePart.getFile(), filePart.getFilename(),
 					filePart.getContentType(), taskId);
 			SGroup group = SGroup.find.byId(groupId);
 
 			if (group.simages == null) {
 				group.simages = new ArrayList<SImage>();
 			}
 
 			group.addImage(image);
 			group.save();
 		}
 
 		catch (IOException e) {
 			flash("uploadError", e.getMessage());
 		}
 
 		return ok(toJson(image));
 	}
 
 
 	
 	
 	
 	
 	
 	public static Result teacherAddImage() {
 
 		FilePart filePart = ctx().request().body().asMultipartFormData()
 				.getFile("picture");
 		List<SImage> images = new ArrayList<SImage>();
 
 		if (filePart.getFile() == null)
 			return ok(toJson("{status: No Image found}"));
 
 
 			Set<String> taskIds = new HashSet<String>();
 			taskIds.add("50ab46d2300480c12ec3695d"); // spry box
 			taskIds.add("50ab4724300480c12ec36967"); // SYKKELPUMPE
 			taskIds.add("50ab4779300480c12ec36972");
 			// fetch 3 tasks
 			// create 3 images for each task
 			for (String taskId : taskIds) {
 				SImage one;
 				try {
 					one = new SImage(filePart.getFile(),
 							filePart.getFilename(), filePart.getContentType(),
 							taskId);
 					images.add(one);
 				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
 				}
 				
 			}
 
 			List<SGroup> groups = SGroup.find.asList();
 			for (SGroup group : groups) {
 				if (group.simages == null) {
 					group.simages = new ArrayList<SImage>();
 					group.simages = images;
 					group.save();
 				}
 				
 			}
 			
 			return ok(toJson("Status Code"));
 	}
 
 
 
 
 	
 	
 	
 	
 	
 	public static Result addTeacherImageByTaskId(String taskId) {
 
 		FilePart filePart = ctx().request().body().asMultipartFormData()
 				.getFile("picture");
 		SImage image = null;
 
 		if (filePart.getFile() == null)
 			return ok(toJson("{status: No Image found}"));
 		try {
 			image = new SImage(filePart.getFile(), filePart.getFilename(),
 					filePart.getContentType(), taskId);
 
 			final int runId = 3;
 			List<SGroup> groups = SGroup.find.filter("runId", runId).asList();
 
 			for (SGroup group : groups) {
 				if (group.simages == null) {
 					group.simages = new ArrayList<SImage>();
 				}
 
 				group.addImage(image);
 				group.save();
 
 				if (group.taskCompleted == null) {
 					group.taskCompleted = new HashSet<String>();
 				}
 
 				if (!group.taskCompleted.contains(taskId)) {
 					group.taskCompleted.add(taskId);
 					group.save();
 				}
 			}
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
 	public static Result updateImage() {
 
 		JsonNode node = ctx().request().body().asJson();
 		String imageId = node.get("id").asText();
 
 		if (SGroup.find.field("simages.id").equal(imageId).get() == null) {
 			return status(401, "Not Authorized");
 		}
 
 		int xpos = node.get("xpos").asInt();
 		int ypos = node.get("ypos").asInt();
 		Boolean isPortfolio = node.get("isPortfolio").asBoolean();
 		Boolean isFinalPortfolio = node.get("isFinalPortfolio").asBoolean();
 
 		Query<SGroup> query = SGroup.datastore.createQuery(SGroup.class)
 				.field("simages.id").equal(imageId);
 		UpdateOperations<SGroup> ops = SGroup.datastore
 				.createUpdateOperations(SGroup.class).disableValidation()
 				.set("simages.$.xpos", xpos).set("simages.$.ypos", ypos)
 				.set("simages.$.isPortfolio", isPortfolio)
 				.set("simages.$.isFinalPortfolio", isFinalPortfolio);
 
 		SGroup.datastore.findAndModify(query, ops);
 
 		return status(200, "OK");
 
 	}
 
 	
 	
 	
 	
 	
 	public static Result deleteImageById(String imageId) throws MongoException,
 			IOException {
 
 		SGroup group = SGroup.find.filter("simages.id", imageId).get();
 		// Second locate the fruit and remove it:
 		for (SImage p : group.simages) {
 			if (p.id.equals(imageId)) {
 				// delete file from gridFS
 				p.deleteImage(p.fileId);
 				// Remove meta info and Group Document
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
 
 		// Second locate the fruit and remove it:
 		SComment comment = new SComment(content);
 		// update member of embedded object list
 		Query<SGroup> query = SGroup.datastore.createQuery(SGroup.class)
 				.field("simages.id").equal(imageId);
 		UpdateOperations<SGroup> ops = SGroup.datastore
 				.createUpdateOperations(SGroup.class).disableValidation()
 				.add("simages.$.scomments", comment);
 
 		SGroup group = SGroup.datastore.findAndModify(query, ops);
 
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
 
 	
 	
 	
 	
 	
 	
 	
 	public static String getFileExtension(String filePath) {
 		StringTokenizer stk = new StringTokenizer(filePath, ".");
 		String FileExt = "";
 		while (stk.hasMoreTokens()) {
 			FileExt = stk.nextToken();
 		}
 		return FileExt;
 	}
 
 }
