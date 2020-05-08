 package com.vaadin.training.bugrap.domain.entity;
 
 import javax.persistence.*;
 import java.util.Date;
 
 @Entity
 public class Comment extends AbstractEntity {
 
     @ManyToOne
     private User author;
 
     private String comment;
 
     @Enumerated(EnumType.STRING)
     private CommentType type;
 
     private String attachmentName;
 
     @Temporal(TemporalType.TIMESTAMP)
     private Date timestamp;
 
     @ManyToOne
     private Report report;
 
     public User getAuthor() {
         return author;
     }
 
     public void setAuthor(User author) {
         this.author = author;
     }
 
     public String getComment() {
         return comment;
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     public Date getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(Date timestamp) {
         this.timestamp = timestamp;
     }
 
     public CommentType getType() {
         return type;
     }
 
     public void setType(CommentType type) {
         this.type = type;
     }
 
     public String getAttachmentName() {
         return attachmentName;
     }
 
     public void setAttachmentName(String attachmentName) {
         this.attachmentName = attachmentName;
     }
 
     @PrePersist
     void updateDates() {
         if (timestamp == null) {
             timestamp = new Date();
         }
     }
 
     public Report getReport() {
         return report;
     }
 
     public void setReport(Report report) {
         this.report = report;
     }
 }
