 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import static play.data.Form.*;
 
 import org.codehaus.jackson.JsonNode;
 
 import views.html.command.*;
 import views.html.*;
 
 import models.*;
 
 import java.util.List;
 
 public class CommandController extends Controller {
   /**
   * Defines a form wrapping the CommandModel class.
   */ 
   private static Form<CommandModel> form = form(CommandModel.class);
 
   public static Result all() {
     List<CommandModel> commands = CommandModel.find.all();
     return ok(list.render(commands));
   }
 
 }
