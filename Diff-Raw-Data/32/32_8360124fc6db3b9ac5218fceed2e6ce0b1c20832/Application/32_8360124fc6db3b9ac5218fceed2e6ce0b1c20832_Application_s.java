 package controllers;
 
 import models.Poll;
 import play.*;
 import play.data.Form;
 import play.db.ebean.Model;
 import play.mvc.*;
 
 import views.html.*;
 
 import java.util.List;
 
 import static play.data.Form.form;
 import static play.libs.Json.toJson;
 
 public class Application extends Controller {
 
     public static Result index() {
        return ok(index.render());
     }
 
     public static Result newPoll() {
         return ok(createPoll.render(form(Poll.class)));
     }
 
     public static Result showPoll(Long id) {
         Form<Poll> pollForm = form(Poll.class).fill(
                 Poll.find.byId(id)
         );
         return ok(
                 showPoll.render(id, pollForm)
         );
     }
 
     public static Result submitPoll() {
         Form<Poll> form = form(Poll.class).bindFromRequest();
         if (form.hasErrors()) {
            return badRequest(index.render());
        }
        else {
             Poll poll = form.get();
             poll.save();
             return redirect(routes.Application.index());
         }
     }
 
    public static Result getPoll() {
        List<Poll> polls = new Model.Finder(String.class, Poll.class).all();
        return ok(toJson(polls));
    }

 }
