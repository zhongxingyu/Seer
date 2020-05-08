 package com.aciertoteam.mail.dto;
 
 
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.aciertoteam.mail.entity.MailTemplate;
 import com.aciertoteam.mail.enums.NotificationStatus;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class NotificationDTO implements Serializable {
 
     private static final long serialVersionUID = 3807457856789914986L;
 
     private Long id;
 
     private String from;
 
     private List<String> to = new LinkedList<String>();
 
     private String templateName;
 
     private String subjectName;
 
     private String comment;
 
     private Date timestamp;
 
     private NotificationStatus status;
 
     private Map<String, Object> properties = new HashMap<String, Object>();
 
     private Map<String, Object> attachments = new HashMap<String, Object>();
 
    public MailTemplate getMailTemplateName() {
        return new MailTemplate(templateName, subjectName);
    }

     public NotificationDTO() {
         timestamp = new Date();
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getFrom() {
         return from;
     }
 
     public void setFrom(String from) {
         this.from = from;
     }
 
     public List<String> getTo() {
         return to;
     }
 
     public void addTo(List<String> to) {
         for (String t : to) {
             this.to.add(t);
         }
     }
 
     public void setTo(String... to) {
         this.to.addAll(Arrays.asList(to));
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
 
     public NotificationStatus getStatus() {
         return status;
     }
 
     public void setStatus(NotificationStatus status) {
         this.status = status;
     }
 
     public Map<String, Object> getProperties() {
         return properties;
     }
 
     public void addProperty(String name, Serializable value) {
         this.properties.put(name, value);
     }
 
     public void addAttachment(String name, Serializable value) {
         this.attachments.put(name, value);
     }
 
     public Map<String, Object> getAttachments() {
         return attachments;
     }
 
     public String getSubjectName() {
         return subjectName;
     }
 
     public void setSubjectName(String subjectName) {
         this.subjectName = subjectName;
     }
 
     public String getTemplateName() {
         return templateName;
     }
 
     public void setTemplateName(String templateName) {
         this.templateName = templateName;
     }
 }
