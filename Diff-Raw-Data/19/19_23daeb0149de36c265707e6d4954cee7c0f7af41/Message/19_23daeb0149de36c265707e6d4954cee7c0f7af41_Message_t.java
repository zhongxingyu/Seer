 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package catchatmodel;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  *
  * @author Joakim
  */
 public class Message {
     
     	private String author;
 	private String content;
 	private DateFormat time = new SimpleDateFormat("HH:mm:ss");
 	private Date date = Calendar.getInstance().getTime();
 
 	public  Message(String author, String content) {
 		this.author = author;
 		this.content = content;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
            public String getTime() {
 		return time.format(date);
 	}
     
 }
