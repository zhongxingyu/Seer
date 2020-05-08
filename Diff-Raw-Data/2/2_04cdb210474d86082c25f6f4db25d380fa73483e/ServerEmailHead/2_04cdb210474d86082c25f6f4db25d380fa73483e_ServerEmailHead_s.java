 package server;
 import java.util.*;
 
 public class ServerEmailHead  {
     private String subject;
     private Calendar date;
     private String sender;
     private String reciver;
     
     
 	public String getSubject() {
 		return subject;
 	}
 	public void setReciver(String reciver){
 		this.reciver = reciver;
 	}
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 	public Calendar getDate() {
 		return date;
 	}
 	public void setDate(Calendar date) {
 		this.date = date;
 	}
 	public String getSender() {
 		return sender;
 	}
 	public void setSender(String sender) {
 		this.sender = sender;
 	}
 	public String getReciver() {
 		return reciver;
 	}
 	public ServerEmailHead(String reciver,String subject,Calendar date, String sender) {
 		this.setSubject(subject);
 		this.setDate(date);
 		this.setReciver(reciver);
         this.setSender(sender);
 	}
    	
 }
