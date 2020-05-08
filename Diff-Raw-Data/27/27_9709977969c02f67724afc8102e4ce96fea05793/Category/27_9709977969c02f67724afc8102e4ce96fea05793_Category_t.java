 package controllers;
import views.html.*;
import play.mvc.*;
 import java.util.*;
 
 import static play.libs.Json.toJson;
 import java.util.Map;
 import java.util.HashMap;
 
 public class Category extends Controller {
 
     public static Result list() {
     
     	Map<String,Object> d = new HashMap<String,Object>();
     	d.put("ok","true");
     	List<String> cat_list = new LinkedList<String>();
     	cat_list.add("Current Students");
     	cat_list.add("Student Application");
     	cat_list.add("Student marks");
 		d.put("list",cat_list);
 		return ok(toJson(d));
 
     }
 
     public static Result by_id(Long cat_id) {
       	return ok(category.render(cat_id));
      
}
 
 
 }
