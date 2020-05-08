 package controllers;
 
 import models.Note;
 import play.Play;
 import play.i18n.Lang;
 import play.mvc.Mailer;
 
 public class NotesMailer extends Mailer {
 
     public static void arrivalNotification(Note note) {
 	Lang.change(note.sender.language);
 	setFrom(Play.configuration.getProperty("mail.from"));
 	setSubject("mail.arrival.subject");
 	addRecipient(note.receiver);
	send(note);
     }
 }
