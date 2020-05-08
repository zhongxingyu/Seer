 package controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import models.Note;
 import models.User;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import play.Play;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Http.StatusCode;
 import auth.UserAuth;
 import controllers.auth.Auth;
 
 public class Notes extends Controller {
 
     public static void displayForm() {
 	User user = UserAuth.getUser();
 	render(user);
     }
 
     @Before(unless = { "create", "last", "sendAll", "viewMail" })
     static void checkAuthenticated() {
 	if (!UserAuth.isUserLoggedIn()) {
 	    Auth.login();
 	}
     }
 
     public static void created() {
 	render();
     }
 
     public static void create(String message, String date, Integer offset,
 	    String[] receivers) {
 	if (!UserAuth.isUserLoggedIn()) {
 	    forbidden();
 	}
 
 	List<String> filteredReceivers = new ArrayList<String>();
 	if (receivers != null) {
 	    for (String receiver : receivers) {
 		if (receiver != null && receiver.length() > 0) {
 		    filteredReceivers.add(receiver);
 		}
 	    }
 	}
 
 	validation.required(message);
 	validation.required(date);
 
 	Date when = null;
 
 	if (offset != null) {
 	    when = new DateTime(date).withZoneRetainFields(
 		    DateTimeZone.forOffsetMillis(offset)).toDate();
 	} else {
 	    when = new DateTime(date).toDate();
 	}
 
 	validation.future(when);
 	if (filteredReceivers != null) {
 	    for (String receiver : filteredReceivers) {
 		try {
 		    Long.parseLong(receiver);
 		} catch (NumberFormatException e) {
 		    validation.email(receiver);
 		}
 	    }
 	}
 
 	if (!validation.hasErrors()) {
 	    User user = UserAuth.getUser();
 	    Note note = new Note();
 	    note.sendDate = when;
 	    note.message = message;
 	    note.sender = user;
 	    if (filteredReceivers.size() > 0) {
 		note.setReceivers(filteredReceivers.toArray(new String[] {}));
 	    } else {
 		note.setReceivers(new String[] { user.email });
 	    }
 	    note.save();
 	    response.status = StatusCode.CREATED;
 	    renderJSON(user);
 	} else {
 	    badRequest();
 	}
     }
 
     public static void last() {
 	if (Play.mode.isDev()) {
 	    Note lastNote = Note.last();
 	    render(lastNote);
 	} else {
 	    notFound();
 	}
     }
 
     public static void sendAll() {
 	if (Play.mode.isDev()) {
 	    List<Note> notes = Note.findAll();
 	    for (Note note : notes) {
 		note.sent = true;
 		note.save();
 		System.out.println("Sent note: " + note);
 	    }
 	} else {
 	    notFound();
 	}
     }
 
     public static void view(Long id) {
 	Note note = Note.findById(id);
 	if (note == null) {
 	    notFound();
 	}
 	User user = UserAuth.getUser();
 	if (!note.wasReadBy(user)) {
 	    note.markReadBy(user);
 	}
 	render(note);
     }
}
