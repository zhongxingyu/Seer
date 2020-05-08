 package models.mail;
 
 import models.EMessages;
 import play.Play;
 
 public class StudentTeacherEmailReset extends EMail{
 
     public StudentTeacherEmailReset(String recipient, String id, String message){
         super();
         this.setSubject(EMessages.get("forgot_pwd.teachersubject"));
         this.addReplyTo(Play.application().configuration().getString("email.contactmail"));
         this.addToAddress(recipient);
 	String url = message.replace("<>",id);
	System.out.println(url);
         this.setMessage(EMessages.get("forgot_pwd.teachermail", id, url));
     }
 }
