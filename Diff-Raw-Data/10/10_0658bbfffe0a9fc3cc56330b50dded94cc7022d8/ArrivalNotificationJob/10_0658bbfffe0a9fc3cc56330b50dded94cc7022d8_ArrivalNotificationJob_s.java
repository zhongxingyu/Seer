 package jobs;
 
 import java.util.List;
 
 import models.Note;
 import models.Receiver;
 import play.Logger;
 import play.Play;
 import play.jobs.Job;
 import play.jobs.On;
 import services.Facebook;
 import controllers.NotesMailer;
 
 @On("cron.notification.arrival")
 public class ArrivalNotificationJob extends Job {
 
     public void doJob() {
 	String jobenabled = Play.configuration
 		.getProperty("jobs.arrivalnotification");
 	if (jobenabled != null && jobenabled.equals("disabled")) {
 	    Logger.info("Arrival notification job is disabled!!!");
 	    return;
 	}
 	Logger.info("Starting arrival notification job.");
 	List<Note> notes = Note.pendingForNotification();
 	Logger.info("There are " + notes.size() + " notifications to send.");
 	for (Note note : notes) {
 	    for (Receiver receiver : note.receivers) {
 		if (receiver.sendByEmail()) {
 		    NotesMailer.arrivalNotification(note, receiver);
 		} else if (receiver.sendToFacebookWall()) {
 		    if (Facebook.postToWall(note, receiver) == null) {
 			Logger.warn("Couldn't send notification #" + note.id
 				+ " to receiver #" + receiver.id);
 			receiver.attempts++;
 			receiver.save();
 			continue;
 		    }
 		}
 		receiver.sent = true;
 		receiver.save();
 		Logger.info("Sent notification #" + note.id + " to receiver #"
 			+ receiver.id);
 	    }
 
 	    boolean sentToAll = true;
 	    for (Receiver receiver : note.receivers) {
 		if (!receiver.sent && receiver.attempts < 2) {
 		    sentToAll = false;
 		    break;
 		}
 	    }
 
 	    if (sentToAll) {
 		note.sent = true;
 		note.save();
 	    }
 	}
     }
 
 }
