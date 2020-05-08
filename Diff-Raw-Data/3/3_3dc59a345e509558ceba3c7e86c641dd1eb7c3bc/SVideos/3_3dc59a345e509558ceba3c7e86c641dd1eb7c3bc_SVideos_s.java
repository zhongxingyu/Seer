 package controllers;
 
 import static play.libs.Json.toJson;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.SComment;
 import models.SGroup;
 import models.SPostit;
 import models.SVideo;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 import com.google.inject.Inject;
 
 public class SVideos extends Controller {
 	
 	@Inject
     public static Datastore datastore; // requestStaticInjection(..)
 
 	
 	
 	
 	
 	
 	
 	public static Result fetchVideoById(String videoId) {
 
 		// need indexing for postit
 		// SPostit postit =
 		// SGroup groups = SGroup.find.;
 
 		return ok(toJson("postit"));
 
 	}
 	
 	
 	
 	
 	
 	
 
 	public static Result fetchVideosByGroupId(String groupId) {
 
 		
 		//String groupId = ctx().request().queryString().get("groupId").toString();
 		SGroup group = SGroup.find.byId(groupId);
 		List<SVideo> videos = group.svideos;
 		if (group.svideos == null) {
 			return ok("[]");
 		}
 		return ok(toJson(videos));
 	}
 
 
 	
 	/* POST : JSON Request
 	{
 		"groupId":"4fe42505da063acbfc99d735" , 
 		"title": "My 2 video", 
 		"uri":"http://www.youtube.com/XYZ"
 	}
 	*/
 	
 	//TODO : Validation Required
 	public static Result addVideo() {
 
 		// parse JSON from request body
 		JsonNode node = ctx().request().body().asJson();
 		String title = node.get("title").asText();
 		String uri = node.get("uri").asText();
 		String taskName = node.get("taskName").asText();
 		
 		String groupId = node.get("groupId").asText();
 		
 		String groupName = node.get("groupName").asText();
 
 		SVideo video = new SVideo(title,uri, taskName);
 		SGroup group = SGroup.find.byId("4fe9b7be30045a331901ea11"); //GUL
 		if (group.svideos == null) {
 			group.svideos = new ArrayList<SVideo>();
 		}
 		group.addVideo(video);
 		group.save();
 
 		return ok(toJson(video));
 	}
 	
 	
 	
 	/* POST : JSON request
 	
 	{
 	"videoId":"4fe43023da063acbfc99d764" , 
 	"wxpos": 400, 
 	"wypos":400
 	}	
 	*/
 	
 	//TODO : Validation required
 	public static Result updateVideoOnWeb() {
 
 		JsonNode node = ctx().request().body().asJson();
 		String videoId = node.get("videoId").asText();
 		int wxpos = node.get("wxpos").asInt();
 		int wypos = node.get("wxpos").asInt();
 
 		SGroup group = SGroup.find.filter("svideos.id",videoId ).get();
 		// Second locate the fruit and remove it:
 		SVideo res = new SVideo();
 		for (SVideo p : group.svideos) {
 			if (p.id.equals(videoId)) {
 				res.id = p.id;
 				res.wxpos = wxpos;
 				res.wypos = wypos;
 				res.title = p.title;
 				res.uri = p.uri;
 				group.svideos.remove(p);
 				group.svideos.add(res);
 				group.save();
 				break;
 			}
 		}
 		return ok(toJson(res));
 
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	public static Result deleteVideoById(String videoId) {
 
 		// First find out the group from which you want to remove one postit:
 		// SGroup group = SGroup.find.byId(groupId);
 		SGroup group = SGroup.find.filter("svideos.id", videoId).get();
 		// Second locate the fruit and remove it:
 		for (SVideo p : group.svideos) {
 			if (p.id.equals(videoId)) {
 				group.svideos.remove(p);
 				group.save();
 				break;
 			}
 		}
 		return ok("deleted successfully");
 	}
 		
 		
 	
 	
 	
 	
 	
 	
 	public static Result postCommentOnVideo() {
 
 		JsonNode node = ctx().request().body().asJson();
 		String videoId = node.get("videoId").asText();
 		String content = node.get("content").asText();
 
 		SGroup group = SGroup.find.filter("svideos.id", videoId).get();
 		// Second locate the fruit and remove it:
 		SVideo res = new SVideo();
 		for (SVideo p : group.svideos) {
 			if (p.id.equals(videoId)) {
 				group.svideos.remove(p);
 
 				if (p.scomments == null) {
 					p.scomments = new ArrayList<SComment>();
 				}
 				p.scomments.add(new SComment(content));
 				group.svideos.add(p);
 				group.save();
 				res = p;
 				break;
 			}
 
 		}
 
 		return ok(toJson(res));
 	}
 	
 	
 	public static Result fetchCommentByVideo(String videoId) {
 
 		SGroup group = SGroup.find.filter("svideos.id", videoId).get();
 		
 		List<SComment> comments = null;
 		for (SVideo p : group.svideos) {
 			if (p.id.equals(videoId)) {
 				comments = p.scomments;
 				break;
 			}
 
 		}
 
 		return ok(toJson(comments));
 	}
 	
 	
 	
 
 }
