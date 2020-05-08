 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package catchatbb;
 
 import javax.inject.Named;
 import static catchatbb.AddUserBB.PU;
 import catchatmodel.Chat;
 import catchatmodel.ChatFactory;
 import catchatmodel.Message;
 import catchatmodel.UserAccount;
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.context.SessionScoped;
 
 /**
  *
  * @author Elin
  */
 @Named("message")
 @SessionScoped
 public class MessageManager implements Serializable{
     private Chat chat;
     private String content;
     private String author;
     private Message m2;
     private static List<Message> list;
     private DateFormat time;
     
     public void addMessage(){
         if(list == null){
             list = new ArrayList<Message>();
         }
         m2 = new Message(author, content);
         list.add(m2);
         content="";
     }
     
     public List<Message> getAll() {
         if(author==null){
            author=StatusManager.username;
         }
         if(list == null){
             list = new ArrayList<Message>();
         }
         
         return list;
     }
     
      public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
         
     public String getTime() {
         return m2.getTime();
 	}
  }
 
