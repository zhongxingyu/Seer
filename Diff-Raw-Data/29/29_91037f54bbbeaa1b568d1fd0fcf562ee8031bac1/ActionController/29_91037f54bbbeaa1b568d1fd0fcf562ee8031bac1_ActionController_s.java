 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import static play.data.Form.*;
 
 import org.codehaus.jackson.JsonNode;
 
 import views.html.action.*;
 import views.html.*;
 
 import models.*;
 
 import java.util.List;
 
 public class ActionController extends Controller {
   
   public static Result all() {
     List<ActionModel> actions = ActionModel.find.all();
     return ok(list.render(actions));
   } 
 }
