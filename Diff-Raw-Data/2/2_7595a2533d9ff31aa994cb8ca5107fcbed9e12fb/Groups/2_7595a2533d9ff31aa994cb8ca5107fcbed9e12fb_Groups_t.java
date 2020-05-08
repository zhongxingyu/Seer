 package controllers;
 
 import java.io.Console;
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.net.URLEncoder;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 
 import models.*;
 
 import com.cedarsoftware.util.io.JsonWriter;
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.reflect.TypeToken;
 import com.google.gson.stream.JsonReader;
 
 import flexjson.JSONSerializer;
 
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 import play.mvc.Before;
 import play.mvc.Controller;
 import requests.Comment_request;
 import requests.Datum_request;
 import requests.JsonRequest;
 import requests.RunId_request;
 import requests.YT_request;
 import respones.RollCall;
 import util.UnicodeString;
 
 /*******************************************************************************
  * Class Name: Group Controller - contain necessary implementations to
  * facilitate the groups activities - serves as proxy server between
  * client[flash, HTML5] and RollCall
  *******************************************************************************/
 public class Groups extends Controller {
 	
 	
 	/***********************************************************************
 	 * Heat pump Simulation Services
 	 **********************************************************************/
 	/********************* Retrieve the runid by title **********************/
 	public static void getRunIdByTitle(String title) {
 		String res = "Null";
 		// if(title == "miracle1")
 		{
 			res = "{ \"run_id\": \"2\"}";
 		}
 		renderJSON(res);
 	}
 
 	/********************* Retrieve the list of all groups *********************/
 	public static void all() {
 		List<MyGroup> groups = MyGroup.findAll();
 		// By Using FlexJSON library
 		JSONSerializer modelSerializer = new JSONSerializer().include("name",
 				"id", "run_id", "members.user.name").exclude("*");
 		renderJSON(modelSerializer.serialize(groups));
 		// by using import com.cedarsoftware.util.io.JsonWriter;
 		// String json = JsonWriter.toJson(groups).toString();
 		// renderText(json);
 		// by template
 		// renderTemplate("Groups/list.json", groups);
 	}
 
 	public static void allGroupsByRunId(Long id) {
 		List<MyGroup> groups = MyGroup.find(
 				"SELECT g from MyGroup g Where run_id =?", id.toString())
 				.fetch();
 		// By Using FlexJSON library
 		JSONSerializer modelSerializer = new JSONSerializer().include("name")
 				.exclude("*");
 		renderJSON(modelSerializer.serialize(groups));
 	}
 
 	/********************* Create new Comment **********************************/
 	public static void postComment() throws IOException {
 		String json = IOUtils.toString(request.body);
 		Comment_request req = new Gson().fromJson(json, Comment_request.class);
 		// Serialize request
 		Long project_id = req.project_id;
 		Long run_id = req.run_id;
 		Long group_id = req.group_id;
 		Long task_id = req.task_id;
 		float xpos = req.xpos;
 		float ypos = req.ypos;
 		String content = req.content;
 
 		MyGroup myGroup = MyGroup.findById(group_id);
 		Project project = Project.findById(project_id);
 		Task task = Task.findById(task_id);
 		Comment comment = myGroup.postComment(project, run_id, task, content,
 				xpos, ypos);
 		renderTemplate("Comments/comment.json", comment);
 	}
 
 	/********************* Update the Comment **********************************/
 	public static void updateComment(Long id) throws IOException {
 		String json = IOUtils.toString(request.body);
 		System.out.println("PUT comments/id:" + json);
 		Comment_request req = new Gson().fromJson(json, Comment_request.class);
 		System.out.println(json);
 		//Long comment_id = req.comment_id;
 		// Unicode conversion
 		UnicodeString us = new UnicodeString();
 		String content = us.convert(req.content);
 		// String content = req.content
 		float xpos = req.xpos;
 		float ypos = req.ypos;
 		Comment comment = Comment.findById(id);
 		comment.content = content;
 		comment.rawcontent = req.content;
 		comment.xpos = xpos;
 		comment.ypos = ypos;
 		comment.save();
 		renderTemplate("Comments/comment.json", comment);
 	}
 
 	/********************* Delete the Comment **********************************/
 	public static void deleteComment(Long id) {
 
 		Comment.delete("from Comment c where c.id=?", id);
 	}
 
 	/********************* Show all Comments **********************************/
 	public static void showAllComments() {
 		List<Comment> comments = Comment.findAll();
 		renderTemplate("Comments/list.json", comments);
 
 	}
 
 	/********************* Show Comments by Group **********************/
 	public static void showCommentbyG() {
 		Long group_id = params.get("grpid", Long.class);
 		List<Comment> comments = Comment.find(
 				"SELECT c from Comment c Where myGroup_id =?", group_id)
 				.fetch();
 		JSONSerializer modelSerializer = new JSONSerializer().include("xpos",
 				"ypos", "content", "myGroup.id", "project.id", "task.id","rawcontent",
				"run_id","wxpos","wypos").exclude("*");
 		renderJSON(modelSerializer.serialize(comments));
 	}
 
 	/********************* Show Comments by Group and Task **********************/
 	public static void showCommentbyGT() {
 		Long group_id = params.get("group_id", Long.class);
 		Long task_id = params.get("task_id", Long.class);
 		List<Comment> comments = Comment
 				.find("SELECT c  from Comment c Where c.myGroup.id =? and c.task.id=?",
 						group_id, task_id).fetch();
 
 		renderTemplate("Comments/list.json", comments);
 	}
 
 	/********************* Update Task Data **********************************/
 	public static void updateTaskData() throws IOException {
 		String json = IOUtils.toString(request.body);
 		System.out.println(json);
 		Datum_request req = new Gson().fromJson(json, Datum_request.class);
 		// Serialize request
 
 		Long data_id = Long.parseLong(req.data_id);
 		String data = req.data;
 		// System.println(json);
 		TaskData existing_var = TaskData.findById(data_id);
 		existing_var.taskdata = data;
 		existing_var.save();
 		renderTemplate("TaskDatum/taskdataU.json", existing_var);
 	}
 
 	public static void index(String name) {
 		List<MyGroup> groups = MyGroup.findAll();
 		render(groups);
 	}
 
 	/***********************************************************************
 	 * Swipe to Wall Services
 	 **********************************************************************/
 	/********************* add Youtube links by Group *********************/
 	// @parms {group_name:"Test", task_name:"spray can", url:"http://"}
 	public static void addYoutubeLink() throws IOException {
 		String json = IOUtils.toString(request.body);
 		YT_request req = new Gson().fromJson(json, YT_request.class);
 		System.out.println(json);
 		String group_name = req.group_name;
 		String task_name = req.task_name;
 		String yt_url = req.url;
 		//Remove the base you-tube link
 		yt_url= yt_url.substring(25);
 		MyGroup tgroup = MyGroup.find("byName", group_name).first();
 		YTubeVideo yt = tgroup.addYTlink(task_name, yt_url);
 		JSONSerializer modelSerializer = new JSONSerializer().include("id",
 				"url", "content").exclude("*");
 		renderJSON(modelSerializer.serialize(yt));
 	}
 
 	/***********************************************************************
 	 * SciWorks Services
 	 **********************************************************************/
 	/********************* Update the YTbox **********************************/
 	public static void updateYtBoxPos() throws IOException {
 		String json = IOUtils.toString(request.body);
 		YT_request req = new Gson().fromJson(json, YT_request.class);
 		System.out.println(json);
 
 		// String content = req.content
 		int wxpos = req.wxpos;
 		int wypos = req.wypos;
 		Long yt_id = req.yt_id;
 		YTubeVideo  ytv = YTubeVideo.findById(yt_id);
 
 		ytv.wxpos = wxpos;
 		ytv.wypos = wypos;
 		ytv.save();
 		renderTemplate("YTubeVideos/ytv.json", ytv);
 	}
 	
 	/********************* Show All contents by group **********************************/
 	
 	public static void showYTVidoByGroup() {
 		Long group_id = params.get("grpid", Long.class);
 		MyGroup group = MyGroup.findById(group_id);
 		List<YTubeVideo> ytlist = group.ytVideos;
 		
 		JSONSerializer modelSerializer = new JSONSerializer().include(
 				"id", "yt_url", "wxpos","wypos").exclude("*");
 		renderJSON(modelSerializer.serialize(ytlist));
 	}
 	
 	
 	public static void showGroupContents() {
 		Long group_id = params.get("grpid", Long.class);
 		MyGroup group = MyGroup.findById(group_id);
 		MyGroup tgroup = new MyGroup();
 		//Assign contents to tgroup
 		List<YTubeVideo> ytlist = group.ytVideos;
 		List<Comment> comments = group.comments;
 		tgroup.ytVideos = ytlist;
 		tgroup.comments = comments;
 		tgroup.id = group.id;
 		tgroup.name = group.name;
 		tgroup.run_id = group.run_id;
 
 		JSONSerializer modelSerializer = new JSONSerializer().include(
 				"ytVideos.id", "ytVideos.yt_url", "comments.xpos",
 				"comments.ypos", "comments.wxpos","comments.wypos","comments.rawcontent", "id", "run_id","comments.task.id","comments.project.id").exclude(
 				"*");
 		renderJSON(modelSerializer.serialize(tgroup));
 	}
 	
 	
 	/********************* add fb comment **********************************/
 	public static void addFbComment(Long id) throws IOException {
 		String json = IOUtils.toString(request.body);
 		System.out.println("PUT comments/id:" + json);
 		Comment_request req = new Gson().fromJson(json, Comment_request.class);
 		System.out.println(json);
 		Long comment_id = req.comment_id;
 		// Unicode conversion
 		UnicodeString us = new UnicodeString();
 		String content = us.convert(req.content);
 		// String content = req.content
 		float xpos = req.xpos;
 		float ypos = req.ypos;
 		Comment comment = Comment.findById(id);
 		comment.content = content;
 		comment.xpos = xpos;
 		comment.ypos = ypos;
 		comment.save();
 		renderTemplate("Comments/comment.json", comment);
 	}
 }
 
 
 
 // /********************* Establish Connection with RollCall ******************/
 // @Before
 // static void createRollcallSession() {
 // String contents =
 // "{ \"Session\": {\"username\":\"binden\" , \"password\":\"binden\"} }";
 // String url = "http://imediamac28.uio.no:8080";
 // WS.url(url).body(contents).post();
 // }
 // /********************* Retrieve the list of all groups *********************/
 // public static void all(){
 // //play serve as client to Ruby rollcall server at port 8080
 // //Talking to Jeremy Ruby Server for testing http://129.240.161.29:4567/groups
 // String url = "http://imediamac28.uio.no:8080/groups.json";
 // JsonElement result = WS.url(url).get().getJson();
 // String res = result.toString();
 // renderJSON(res);
 // }
 // /********************* Retrieve the group with ID `1` **********************/
 // public static void getById(String id){
 // //JsonReader.setLenient(true);
 // String url = "http://imediamac28.uio.no:8080/groups/" + id + ".json";
 // //WS.url accept only String type parameters
 // JsonElement result = WS.url(url).get().getJson();
 // String res = result.toString();
 // renderJSON(res);
 // }
 //
 // /********************* Retrieve the runid by title **********************/
 // public static void getRunIdByTitle(String title){
 // //JsonReader.setLenient(true);
 // String url = "http://imediamac28.uio.no:8080/runs/" + title + ".json";
 // //WS.url accept only String type parameters
 // JsonElement result = WS.url(url).get().getJson();
 // String res = result.toString();
 // renderJSON(res);
 // }
 // /********************* Delete group with ID `1` ****************************/
 // public static void deleteGroup(String id){
 // String url = "http://imediamac28.uio.no:8080/groups/" + id;
 // Integer status = WS.url(url).delete().getStatus();
 // if(status == 1)
 // renderText("Group deleted");
 // }
