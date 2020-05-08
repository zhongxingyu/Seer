 package controllers;
 
 import static ui.tags.Messages.info;
 import static ui.tags.MessagesHelper.invalidForm;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import models.Event;
 import models.EventChoice;
 
 import org.joda.time.LocalDate;
 
 import play.data.DynamicForm;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import services.EventService;
 import views.html.event.eventAddDates;
 import views.html.event.eventCreated;
 import views.html.event.eventNew;
 
 import com.google.common.base.Strings;
 
 public class CreateEvent extends Controller {
	private static final Form<Event> FORM_EVENT = form(Event.class);
 
 	public static Result newEvent() {
 		return ok(eventNew.render(FORM_EVENT));
 	}
 
 	public static Result createEvent() {
 		Form<Event> filledForm = FORM_EVENT.bindFromRequest();
 
 		if (filledForm.hasErrors()) {
 			// Error handling.
 			return invalidForm(eventNew.render(FORM_EVENT));
 		}
 
 		Event event = filledForm.get();
 		Long eventId = EventService.createEvent(event);
 		return setDates(eventId);
 	}
 
 	public static Result setDates(Long eventId) {
 		Event event = EventService.getEvent(eventId);
 		return ok(eventAddDates.render(event, FORM_EVENT));
 	}
 
 	public static Result saveDates(Long eventId) {
 		DynamicForm dynamicForm = form().bindFromRequest();
 		EventChoice date;
 		List<EventChoice> dates = new ArrayList<EventChoice>();
 		String value;
 		for (Entry<String, String> entry : dynamicForm.data().entrySet()) {
 			value = entry.getValue();
 			if (!Strings.isNullOrEmpty(value)) {
 				date = new EventChoice();
 				date.date = LocalDate.parse(entry.getValue());
 				dates.add(date);
 			}
 		}
 		EventService.saveDates(eventId, dates);
 		info("Event successfully created.");
 		return confirmEventCreation(eventId);
 	}
 
 	public static Result confirmEventCreation(Long id) {
 		Event event = EventService.getEvent(id);
 		return ok(eventCreated.render(event));
 	}
 }
