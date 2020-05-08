 package controllers;
 
 import java.util.Map;
 
 import models.EventAction;
 import models.EventWatch;
 import models.EventWatchAction;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import com.avaje.ebean.Ebean;
 
 public class Events extends Controller {
 	static Form<EventWatch> eventForm = form(EventWatch.class);
 	
 	public static Result list() {
 		return ok(views.html.events.render(
 			EventWatch.find.join("todos.action").findList()
 		));
 	}
 	
 	public static Result create() {
 		return ok(views.html.event.render(eventForm, EventAction.toMap()));
 	}
 	
 	public static Result save() {
 		Form<EventWatch> f = eventForm.bindFromRequest();
		
		final Map<String, String[]> urlFormEncoded = 
			Controller.request().body().asFormUrlEncoded();
		for (String s : urlFormEncoded.get("todos.action.id")) {
			Long actionId = Long.parseLong(s);
			EventAction action = EventAction.find.ref(actionId);
			
			EventWatchAction watchAction = new EventWatchAction();
			watchAction.action = action;
			watchAction.event = f.get();
			
			f.get().todos.add(watchAction);
		}
		
 		if (f.hasErrors()) {
 			return badRequest(
 				views.html.event.render(f, EventAction.toMap())
 			);
 		} else {
 			f.get().save();
 			for (EventWatchAction action : f.get().todos)
 				Ebean.save(action);
 		}
 
 		return redirect(routes.Events.list());
 	}
 	
 	public static Result delete(Long id) {
 		EventWatch.delete(id);
 		return redirect(routes.Events.list());
 	}
 }
