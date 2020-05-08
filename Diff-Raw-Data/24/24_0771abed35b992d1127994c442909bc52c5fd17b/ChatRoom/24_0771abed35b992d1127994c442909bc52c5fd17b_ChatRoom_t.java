 package co.speechat.data;
 
 import java.util.Collection;
 
 /**
  * TODO: Document me
  * Author: Iurii Lytvynenko
  */
 public interface ChatRoom {
     String register();
 
     Collection<Message> getAll(String member);
     Collection<Message> get(String member);
     
     void add(Message message);
 
     int getUsersCount();
    
 }
