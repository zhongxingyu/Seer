 package controllers;
 
 import play.*;
 import play.data.Form;
 import play.data.DynamicForm;
 import play.mvc.*;
 
 import java.util.List;
 
 import views.html.*;
 import models.*;
 
 import play.libs.Json;
 import org.codehaus.jackson.node.ObjectNode;
 
 
 public class Application extends Controller {
   
     static Form<Task> taskForm = Form.form(Task.class);
 
     public static Result index() {
         return ok(index.render("Your new application is ready."));
     }
 
     public static Result tasks() {
         allowCors();
         DynamicForm df = Form.form().bindFromRequest();
         List<Task> l_tasks;
         
         String orderBy = df.get("orderBy") != null ? df.get("orderBy") : null;
         
         if(df.get("status") != null){
             if(df.get("status").equalsIgnoreCase("done")){
                 l_tasks = Task.allDone(orderBy);
             }  else {
                 l_tasks = Task.allOpen(orderBy);
             }
         } else {
             l_tasks = Task.all();
         }        
         return ok(Json.toJson(l_tasks));
     }
   
     public static Result newTask() {
         // TODO enhance API policy about cross domain data
         allowCors();
         Form<Task> filledForm = taskForm.bindFromRequest();
         if(filledForm.hasErrors()) {
             // TODO make better error handling
            return badRequest("{'error' : 'bad request'}");
         } else {
             Task.create(filledForm.get());
             return ok(Json.toJson(filledForm.get()));
         }
     }
   
     public static Result deleteTask(Long id) {
         allowCors();
         Task.delete(id);
         return ok();
     }
 
     public static Result updateTask(Long id) {
         allowCors();
         Form<Task> filledForm = taskForm.bindFromRequest();
         if(filledForm.hasErrors()) {
             return badRequest(filledForm.errorsAsJson());
         }
        Task.update(filledForm.get(), id);
        return ok(Json.toJson(filledForm.get()));
     }
 
     public static Result taskDone(Long id){
         allowCors();
         Task done = Task.find.byId(id);
         done.done = true;        
         done.update(id);
         return ok();
     }
 
     public static Result taskOpen(Long id){
         allowCors();
         Task done = Task.find.byId(id);
         done.done = false;        
         done.update(id);
         return ok();
     }
 
     public static Result checkPreFlight(){
         allowCors();
         return ok();
     }
        
     private static void allowCors(){
         response().setHeader("Access-Control-Allow-Origin", "*");
         response().setHeader("Access-Control-Allow-Methods", "POST,GET,DELETE,PUT");   // Only allow POST
         response().setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
         response().setHeader("Access-Control-Allow-Headers", 
                              "Origin, X-Requested-With, Content-Type, Accept");         // Ensure this header is also allowed!  
     }
   
 }
