 package com.force.sample.model;
 
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.persistence.*;
 
 /**
  * 
  * This entity describes a single chatter post as stored in the Chatter To Do database.
  * Most fields come from the Chatter API, but some are specific to the To Do application.
  * One entry will be created per chatter post per user. So a single post that is liked by more than
  * one user will appear in the database multiple times so that it's completeness can be tracked on a
  * per user basis.
  *
  * @author John Simone
  */
 @Entity
 public class ChatterPost implements Comparable<ChatterPost>{
 
     public enum TO_DO_REASON {LIKE,MENTION};
     
     @Id
     @GeneratedValue(strategy=GenerationType.AUTO) 
     private int localId;
     private String id;
     private String feedOwnerUserId;
     private String title;
    @Column(length=1000)
     private String body;
     private String authorName;
     private String authorId;
     private TO_DO_REASON reason;
     private boolean done;
     private URL postLink;
     private URL authorLink;
     private Date postDate;
     
     public int getLocalId() {
         return localId;
     }
     public void setLocalId(int localId) {
         this.localId = localId;
     }
     public String getId() {
         return id;
     }
     public void setId(String id) {
         this.id = id;
     }
     public String getTitle() {
         return title;
     }
     public void setTitle(String title) {
         this.title = title;
     }
     public String getBody() {
         return body;
     }
     public void setBody(String body) {
         this.body = body;
     }
     public String getAuthorName() {
         return authorName;
     }
     public void setAuthorName(String authorName) {
         this.authorName = authorName;
     }
     public String getAuthorId() {
         return authorId;
     }
     public void setAuthorId(String authorId) {
         this.authorId = authorId;
     }
     public String getFeedOwnerUserId() {
         return feedOwnerUserId;
     }
     public void setFeedOwnerUserId(String userId) {
         this.feedOwnerUserId = userId;
     }
     public TO_DO_REASON getReason() {
         return reason;
     }
     public void setReason(TO_DO_REASON reason) {
         this.reason = reason;
     }
     public boolean isDone() {
         return done;
     }
     public void setDone(boolean done) {
         this.done = done;
     }
     public URL getPostLink() {
         return postLink;
     }
     public void setPostLink(URL postLink) {
         this.postLink = postLink;
     }
     public URL getAuthorLink() {
         return authorLink;
     }
     public void setAuthorLink(URL authorLink) {
         this.authorLink = authorLink;
     }
     public Date getPostDate() {
         return postDate;
     }
     public void setPostDate(Date postDate) {
         this.postDate = postDate;
     }
     public String getPostDateStr() {
         if(postDate != null) {
             SimpleDateFormat df = new SimpleDateFormat("MM/dd HH:mm");
             return df.format(postDate);            
         } else {
             return "";
         }
     }
     @Override
     public String toString() {
         return "{id=" + id + ", title=" + title + ", author=" + authorName + ", reason=" + reason + ", completed=" + done + "}";
     }
     
     /**
      * It's important that we only consider values that come from the chatter api 
      * in the comparison methods since these are mainly used to determine whether or not to store
      * the post in the database.
      * 
      * For this reason we'll need to exclude reason and done from this algorithm
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (obj == null) return false;
         if (getClass() != obj.getClass()) return false;
         ChatterPost other = (ChatterPost)obj;
         if (authorName == null) {
             if (other.authorName != null) return false;
         } else if (!authorName.equals(other.authorName)) return false;
         if (authorId == null) {
             if (other.authorId != null) return false;
         } else if (!authorId.equals(other.authorId)) return false;
         if (body == null) {
             if (other.body != null) return false;
         } else if (!body.equals(other.body)) return false;
         if (feedOwnerUserId == null) {
             if (other.feedOwnerUserId != null) return false;
         } else if (!feedOwnerUserId.equals(other.feedOwnerUserId)) return false;
         if (id == null) {
             if (other.id != null) return false;
         } else if (!id.equals(other.id)) return false;
         if (title == null) {
             if (other.title != null) return false;
         } else if (!title.equals(other.title)) return false;
         return true;
     }
  
     /**
      * It's important that we only consider values that come from the chatter api 
      * in the comparison methods since these are mainly used to determine whether or not to store
      * the post in the database.
      * 
      * For this reason we'll need to exclude reason and done from this algorithm
      */
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((authorName == null) ? 0 : authorName.hashCode());
         result = prime * result + ((authorId == null) ? 0 : authorId.hashCode());
         result = prime * result + ((body == null) ? 0 : body.hashCode());
         result = prime * result + ((feedOwnerUserId == null) ? 0 : feedOwnerUserId.hashCode());
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         result = prime * result + ((title == null) ? 0 : title.hashCode());
         return result;
     }
     
     /**
      * posts will be sorted in descending date order.
      */
     @Override
     public int compareTo(ChatterPost o) {
         if(o.postDate == null ) {
             if(this.postDate != null) {
                 return -1;
             } else {
                 return 0;
             }
         }
         return o.postDate.compareTo(this.postDate);
     }
     
 }
