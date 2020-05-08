 package org.springframework.social.appdotnet.api.data.post;
 
 import org.springframework.social.appdotnet.api.data.entities.ADNEntities;
 import org.springframework.social.appdotnet.api.data.user.ADNUser;
 
 import java.util.Date;
 
 /**
  * @author Arik Galansky
  */
 public class ADNPost {
     private String id;
     private ADNUser user;
     private Date created_at;
     private String text;
     private String html;
     private ADNPostSource source;
     private String reply_to;
     private String thread_id;
     private Integer num_replies;
     // TODO Arikg: add annotations
     private ADNEntities entities;
    private Boolean is_deleted;
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public ADNUser getUser() {
         return user;
     }
 
     public void setUser(ADNUser user) {
         this.user = user;
     }
 
     public Date getCreated_at() {
         return created_at;
     }
 
     public void setCreated_at(Date created_at) {
         this.created_at = created_at;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public String getHtml() {
         return html;
     }
 
     public void setHtml(String html) {
         this.html = html;
     }
 
     public ADNPostSource getSource() {
         return source;
     }
 
     public void setSource(ADNPostSource source) {
         this.source = source;
     }
 
     public String getReply_to() {
         return reply_to;
     }
 
     public void setReply_to(String reply_to) {
         this.reply_to = reply_to;
     }
 
     public ADNEntities getEntities() {
         return entities;
     }
 
     public void setEntities(ADNEntities entities) {
         this.entities = entities;
     }
 
     public String getThread_id() {
         return thread_id;
     }
 
     public void setThread_id(String thread_id) {
         this.thread_id = thread_id;
     }
 
     public Integer getNum_replies() {
         return num_replies;
     }
 
     public void setNum_replies(Integer num_replies) {
         this.num_replies = num_replies;
     }
 
    public Boolean getIs_deleted() {
        return is_deleted;
     }
 
    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
     }
 }
