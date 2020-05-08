 package controllers;
 
 import play.api.templates.Html;
 import play.core.Router;
 import play.gtengine.gte;
 import play.mvc.*;
 import views.html.index;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Application extends Controller {
 
     public static Result index() {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("name", "Morten");
 
         //int q = 1/ 0;
 
         List<Integer> myList = Arrays.asList(1,2,3,4,5);
         params.put("myList", myList);
         
         return ok(gte.template("index.html").render(params));
     }
     
     public static Result someOtherPage(String input) {
         return ok(new Html("other page: " + input));
     }
     
     public static Result scalaTemplate() {
         return ok( index.render("hi"));
     }
 
 }
