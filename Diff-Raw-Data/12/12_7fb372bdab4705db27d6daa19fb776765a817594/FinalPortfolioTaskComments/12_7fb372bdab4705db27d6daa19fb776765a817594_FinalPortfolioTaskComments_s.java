 package controllers;
 
 import static play.libs.Json.toJson;
 
 import java.util.ArrayList;
 
 import models.FinalPortFolioTaskComment;
 import models.SGroup;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import com.google.code.morphia.query.Query;
 import com.google.code.morphia.query.UpdateOperations;
 
 /**
  * @author Jeremy Toussaint
  **/
 
 public class FinalPortfolioTaskComments extends Controller {
 
 	public static Result addTaskComment() {
 		JsonNode node = ctx().request().body().asJson();
 		String text = node.get("text").asText();
 		String taskId = node.get("taskId").asText();
 		String groupId = node.get("groupId").asText();
 
 		FinalPortFolioTaskComment finalPortFolioTaskComment = new FinalPortFolioTaskComment(text, taskId);
 		SGroup group = SGroup.find.byId(groupId); // GUL
 		
 		if (group.finalPortfolioTaskComments == null) {
 			group.finalPortfolioTaskComments = new ArrayList<FinalPortFolioTaskComment>();
		} 
 		
		if (SGroup.find.field("finalPortfolioTaskComments.taskId").equal(taskId).get() == null) {
 			group.addPortFolioTaskComment(finalPortFolioTaskComment);
 			group.save();
 			
 			return ok(toJson(finalPortFolioTaskComment));
 		}
 		else {
 			return status(401, "Not Authorized");
 		}
 	}
 
 	public static Result updateTaskComment() {
 
 		JsonNode node = ctx().request().body().asJson();
 		String finalPortFolioTaskCommentId = node.get("id").asText();
 
 		if (SGroup.find.field("finalPortfolioTaskComments.id").equal(finalPortFolioTaskCommentId).get() == null) {
 			return status(401, "Not Authorized");
 		}
 
 		String text = node.get("text").asText();
 
 		Query<SGroup> query = SGroup.datastore.createQuery(SGroup.class).field("finalPortfolioTaskComments.id").equal(finalPortFolioTaskCommentId);
 		UpdateOperations<SGroup> ops = SGroup.datastore.createUpdateOperations(SGroup.class).disableValidation().set("finalPortfolioTaskComments.$.text", text);
 		SGroup.datastore.findAndModify(query, ops);
 
 		SGroup group = SGroup.find.filter("finalPortfolioTaskComments.id", finalPortFolioTaskCommentId).get();
 		FinalPortFolioTaskComment res = null;
 		for (FinalPortFolioTaskComment p : group.finalPortfolioTaskComments) {
 			if (p.id.equals(finalPortFolioTaskCommentId)) {
 				res = p;
 				break;
 			}
 		}
 
 		if (res == null) {
 			return ok("{}");
 		}
 		return ok(toJson(res));
 	}
 }
