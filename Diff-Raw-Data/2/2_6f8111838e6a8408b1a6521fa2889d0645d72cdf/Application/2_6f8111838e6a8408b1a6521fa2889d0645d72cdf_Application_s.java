 package controllers;
 
 import java.io.File;
 import java.lang.reflect.Type;
 import java.util.List;
 
 import models.CodeReview;
 import models.CodeReviewRepository;
 import models.ReviewComment;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.mvc.Controller;
 import util.Lists;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 import com.google.gson.reflect.TypeToken;
 
 public class Application extends Controller {
     
     public static void uploadCodeReview(String name, File codeReview) {
    	new CodeReviewRepository().save(name, codeReview);
     	RandomCodeReviewPresenter.display();
     }
     
     public static void add() {
     	render();
     }
     
     public static void someMethod(String name, String json) {
     	JsonElement jsonObject = new JsonParser().parse(json);
     	Type type = new TypeToken<List<String>>(){}.getType();
     	List<String> lines = new Gson().fromJson(jsonObject, type);
     	
     	CodeReview codeReview = new CodeReview(name, StringUtils.join(lines, "\n"), Lists.<ReviewComment>create(), Lists.<ReviewComment>create());
     	new CodeReviewRepository().save(name, codeReview);
 
     	renderText("{ \"redirectTo\": \"/\" }");
     }
     
     public static void uploadSomeCode(String name, String code) {
     	CodeReview codeReview = new CodeReview(name, code, Lists.<ReviewComment>create(), Lists.<ReviewComment>create());
     	new CodeReviewRepository().save(name, codeReview);
     	RandomCodeReviewPresenter.display();
     }
 }
