 package org.peaceful.banana.git.util;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ekun
  * Date: 05.11.12
  * Time: 10:17
  */
 public class CommitStatistics {
     private String user;
     private String message;
     private int added;
     private int deleted;
 
     public CommitStatistics() {
         added = 0;
         deleted = 0;
     }
 
     public CommitStatistics(int added, int deleted) {
         this.added = added;
         this.deleted = deleted;
     }
 
     public CommitStatistics(int added, int deleted, String user) {
         this.added = added;
         this.deleted = deleted;
         this.user = user;
     }
 
     public CommitStatistics(int added, int deleted, String user, String message) {
         this.added = added;
         this.deleted = deleted;
         this.user = user;
         this.message = message;
     }
 
     public int getImpact() {
         return (added + deleted);
     }
 
     public void setAdded(int added) {
         this.added = added;
     }
 
     public int getAdded() {
         return added;
     }
 
     public void setDeleted(int deleted) {
         this.deleted = deleted;
     }
 
     public int getDeleted() {
         return deleted;
     }
 
     public String getUser() {
         return user;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public void setUser(String user) {
         this.user = user;
     }
 
     public void addImpact(int added, int deleted) {
         this.added += added;
         this.deleted += deleted;
     }
 }
