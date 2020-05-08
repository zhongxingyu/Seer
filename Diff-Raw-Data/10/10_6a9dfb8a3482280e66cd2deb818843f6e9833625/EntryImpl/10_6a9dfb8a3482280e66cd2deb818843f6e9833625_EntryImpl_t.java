 package com.jayway.khelg.model;
 
 import java.util.Date;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.jayway.khelg.domain.Entry;
 
 @PersistenceCapable
 public class EntryImpl implements Entry {
 
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Long id;
     @Persistent
     private long topicId;
     @Persistent
     private Date date;
     @Persistent
     private String header;
     @Persistent
     private String message;
 
     public EntryImpl(long topicId, String header, String message) {
         this.topicId = topicId;
         this.date = new Date();
         this.header = header;
         this.message = message;
     }
 
     @Override
     public long getId() {
         return id;
     }
 
     @Override
     public Date getDate() {
         return date;
     }
 
     @Override
     public String getHeader() {
         return header;
     }
 
     @Override
     public String getMessage() {
         return message;
     }
 
    public long getTopicId()
    {
        return topicId;
    }
 }
