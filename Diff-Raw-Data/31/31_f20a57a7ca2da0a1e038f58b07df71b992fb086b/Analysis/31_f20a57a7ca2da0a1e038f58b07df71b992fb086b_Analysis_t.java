 package com.bouncingdata.plfdemo.datastore.pojo.model;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.jdo.annotations.Element;
 import javax.jdo.annotations.Join;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 
 @PersistenceCapable
 public class Analysis extends BcDataScript {
   
   private String status;
   private int score;
   // 1-N bidirectional relationship, this list should not be added to default fetch group
   @Persistent(mappedBy="analysis") List<Comment> comments;  
   @Persistent(table="Analysis_tags")
   @Join (column="id_OID")
   @Element (column="id_EID")
  private   Set<Tag> tags;
   
   @Persistent
   private String thumbnail;
   
   @NotPersistent
   private int commentCount;
   
   public String getStatus() {
     return status;
   }
   public void setStatus(String status) {
     this.status = status;
   }
   public int getScore() {
     return score;
   }
   public void setScore(int score) {
     this.score = score;
   }
   public List<Comment> getComments() {
     return comments;
   }
   public void setComments(List<Comment> comments) {
     this.comments = comments;
   }
   public int getCommentCount() {
     return commentCount;
   }
   public void setCommentCount(int commentCount) {
     this.commentCount = commentCount;
   }
   @Override
   public Set<Tag> getTags() {
     return tags;
   }
   @Override
   public void setTags(Set<Tag> tags) {
     this.tags = tags;
   }
   public String getThumbnail() {
     return thumbnail;
   }
   public void setThumbnail(String thumbnail) {
     this.thumbnail = thumbnail;
   }
   
   
 }
