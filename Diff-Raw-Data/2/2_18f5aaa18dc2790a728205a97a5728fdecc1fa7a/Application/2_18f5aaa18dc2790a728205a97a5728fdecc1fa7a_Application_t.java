 package controllers;
 
 import models.Topic;
 import org.joda.time.DateTime;
 import play.*;
 import play.data.DynamicForm;
 import play.data.format.Formatters;
 import static play.data.format.Formatters.*;
 
 import play.mvc.*;
 import play.data.Form;
 import static play.data.Form.*;
 import play.data.validation.Constraints;
 import play.mvc.*;
 
 import views.html.*;
 
 import java.util.Locale;
 
 public class Application extends Controller {
 
     final static Form<Topic> topicForm = form(Topic.class);
 
     public static Result index() {
         Topic result = Topic.findByTitle("Crisis");
         if (result == null) {
             result = Topic.find.byId(1l);
         }
         if (result == null) {
             return ok("Under Construction");
         }
         return renderTopic(result);
     }
 
     public static Result renderTopic(Topic topic) {
         if (topic.isPublic() || isAdmin()) {
            return ok(index.render(topic.title, topic.imageUrl, topic.summary, topic.getFullJson().toString()));
         } else {
             if (topic.title.equals("Crisis")) {
                 return ok("Under Construction");
             }
             return pageNotFound();
         }
     }
 
     public static Result pageNotFound() {
         return redirect("/Crisis");
     }
 
     public static Result show(String title) {
         Topic result = Topic.findByTitle(title);
         if (result == null) {
             return pageNotFound();
         }
         return renderTopic(result);
     }
 
     public static Result getTopicJson() {
         Form<Topic> t = topicForm.bindFromRequest();
         Topic topic = Topic.findByTitle(t.get().title);
         if (topic == null) {
             return ok("{}");
         }
         return ok(topic.getFullJson());
     }
 
     public static void setAdmin() {
         session("administrator", "yes");
     }
 
     public static boolean isAdmin() {
         if (session().isDirty) return false;
         String sessionData = session().get("administrator");
         boolean isAdmin = sessionData != null && sessionData.equals("yes");
         return isAdmin;
     }
 
     public static Result adminTools() {
         if (!isAdmin()) {
             return ok(enter_password.render());
         } else {
             return ok(edit_topics.render());
         }
     }
 
     public static class Password {
         @Constraints.Required
         public String password;
     }
 
     final static Form<Password> passwordForm = form(Password.class);
 
     public static Result verifyPassword() {
        Form<Password> form = passwordForm.bindFromRequest();
 
        if(!form.hasErrors() && form.get().password.equals(Play.application().configuration().getString("password"))) {
            setAdmin();
            return redirect("/admin_tools");
        }
 
        return ok("Access Denied");
     }
 
 
     public static Result postTopic() {
         if (!isAdmin()) {
             return ok("Access Denied");
         }
 
         DynamicForm dform = form().bindFromRequest();
         long id = Long.parseLong(dform.get("id"));
         String title = dform.get("title");
         Topic result = Topic.find.byId(id);
         if (result == null) {
             result = Topic.findByTitle(title);
         }
         Form<Topic> form;
 
         form =  topicForm.bindFromRequest();
         if (form.hasErrors()) {
             return ok(form.errorsAsJson().toString());
         }
 
         if (result == null) {
             result = form.get();
             result.id = null;
         } else {
             Topic temp = form.get();
             result.updateFields(temp);
         }
 
         if(form.hasErrors()) {
             return ok(form.globalErrors().toString());
         }
 
         String supers = form.field("supers").valueOr("");
         String subs  = form.field("subs").valueOr("");
         String ftitle = Topic.handleForm(result, supers, subs);
         if (ftitle == null)
             return redirect("/admin_tools");
         else
             return redirect("/" + ftitle);
     }
 
     public static Result logout() {
         session().clear();
         return ok("Logged out");
     }
 }
