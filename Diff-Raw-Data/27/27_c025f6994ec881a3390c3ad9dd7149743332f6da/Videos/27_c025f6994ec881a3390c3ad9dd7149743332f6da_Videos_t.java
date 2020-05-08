 package controllers;
 
 import java.io.File;
 import java.util.List;
 
 import models.Annotation;
 import models.Video;
 import play.data.DynamicForm;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 import views.html.streaming;
 
 import com.google.code.morphia.Key;
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 import com.google.code.morphia.query.UpdateResults;
 
 public class Videos  extends Controller{
 
 
 	public static Result streaming(String nameVideo) throws Exception {
 
 		//ex:http://localhost:9000/api/video/Oasis
 		//on affiche par nom et non par id pour le selectVideo & URL
 		List<Video> myVideos = MorphiaObject.datastore.find(Video.class).asList();
 		Video myVideo = null;
 		List<Annotation> myAnnos = null;
 		for(Video v : myVideos) {
 			if(v.getNom().equals(nameVideo)){
 				myVideo = v;
 				myAnnos = v.getAnnotations();
 
 			}
 		}
 
 		String myPath = myVideo.getPath();
 		return ok(streaming.render("/" + myPath, myAnnos, myVideos));
 	}
 
 
 
 
 
 
 	/*
 	 * GET
 	 */
 
 	public static Result list() {
 		List<Video> res = MorphiaObject.datastore.find(Video.class).asList();
 		return ok(Json.toJson(res));
 	}
 
 	public static Result getAnnotationsOnVideo(String id) {
 		List<Video> res = MorphiaObject.datastore.find(Video.class).asList();
 		for(Video v : res) {
 			if(v.getId().equals(id))
 				return ok(Json.toJson(v.getAnnotations()));
 		}
 		return ok("Error : Unknown Id");
 	}
 
 	public static Result getQuadrant(String id) {
 		List<Video> res = MorphiaObject.datastore.find(Video.class).asList();
 		for(Video v : res) {
 			if(v.getId().equals(id)) {
 				v.genererQuadrant();
 			}
 		}
 		
 		return ok();
 	}
 
 	/*
 	public static Result getVideo(String id) throws Exception
 	{	
 		List<Video> res = MorphiaObject.datastore.find(Video.class).asList();
 		Video vid = res.get(0);
 		//response().setHeader("Content-Disposition", "attachment; filename=oasis.mp4");
 		response().setContentType("video/mp4");
 		File f = new File("oasis.mp4");
 		return ok(index.render("oasis.mp4"));
 	}
 	 */
 
 	/*
 	 * POST
 	 */
 
 	public static Result upload() {
 
 		Form<Video> form = Form.form(Video.class).bindFromRequest();
		DynamicForm df = Form.form().bindFromRequest();
		
 		String result = "";
 		if(form.hasErrors() || form.get().getNom() == null) {
 			return ok("Error : Must specify name.");
 		}
 		else {
 			Key<Video> key = MorphiaObject.datastore.save(form.get());
 			result = key.getId().toString();
 		}
 
 		return ok(result);
 	}
 
 	public static Result saveBinary(String id) {
 
 		MultipartFormData body = request().body().asMultipartFormData();
		
 		FilePart video = body.getFile("stream");
 		//ObjectId oid = new ObjectId(id);
 		String oid = id;
 
 		UpdateOperations<Video> ops;
 		Query<Video> updateQuery = MorphiaObject.datastore.createQuery(Video.class).field("_id").equal(oid);
 
 
 		if(video != null) {
 			File file = video.getFile();
 			String path = "public"+File.separator+"video"+File.separator+video.getFilename();
 			String url = "video/"+video.getFilename();
 			file.renameTo(new File(path));
 			file.delete();
 			ops = MorphiaObject.datastore.createUpdateOperations(Video.class).set("path", url);
 			UpdateResults<Video> ur = MorphiaObject.datastore.update(updateQuery, ops);
 			return ok(ur.getWriteResult().toString());
 		}
 
 
 		return ok("Error : File not uploaded.");
 
 
 
 
 	}
 
 	/*
 	 * if (video != null) {
 				String fileName;
 				if(df.hasErrors()) {
 					fileName = video.getFilename();
 				}
 				else {
 					fileName = df.get("nom");
 				}
 
 				File file = video.getFile();
 				String path = "public"+File.separator+"video"+File.separator+fileName;
 				file.renameTo(new File(path));
 				file.delete();
 				MorphiaObject.datastore.save(new Video(fileName, path));
 				return ok("File uploaded");
 	 */
 
 
 	/*
 	 * DELETE
 	 */
 
 	public static Result delete(String id) throws Exception
 	{	
 
 		return ok("File DELETE : OK");
 	}
 
 
 
 
 }
