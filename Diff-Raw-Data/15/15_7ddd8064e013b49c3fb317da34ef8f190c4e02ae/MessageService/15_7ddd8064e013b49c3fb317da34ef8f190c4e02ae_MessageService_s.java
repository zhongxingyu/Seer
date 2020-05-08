 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Message;
 import com.thoughtworks.twu.persistence.HibernateConnection;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.List;
 
 
 public class MessageService {
 
     private HibernateConnection connection;
     private Session session;
 
     public MessageService() {
         connection = HibernateConnection.getInstance();
         session = connection.getSession();
 
     }
     public List<Message> getAllMessages() {
         return session.createQuery("from com.thoughtworks.twu.domain.Message").list();
     }
 
    public Message getMessageMessageById(String alteast2CharsForSearch) {
         return (Message) session.createCriteria(Message.class).
                add(Restrictions.eq("messageId", alteast2CharsForSearch)).list().get(0);
     }
 }
