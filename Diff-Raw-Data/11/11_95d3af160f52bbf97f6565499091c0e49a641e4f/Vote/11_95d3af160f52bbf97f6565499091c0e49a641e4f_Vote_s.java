 package com.easytag.core.entity.jpa;
 
 import com.easytag.core.enums.VoteType;
 import java.io.Serializable;
 import javax.persistence.*;
 
 /**
  *
  * @author Ovchinnikov Valeriy (email: kremsnx@gmail.com)
  */
 @Entity
 @Table(name="votes")
 public class Vote implements Serializable {
     @Id
     private Long userId;
     private Long albumId;
     private VoteType voteType;
 
     public Vote() {}
     
     public Vote(Long userId, Long albumId, VoteType voteType) {
         this.userId = userId;
         this.albumId = albumId;
         this.voteType = voteType;
     }
     
     public void setUserId(Long userId) {
         this.userId = userId;
     }
 
     public void setAlbumId(Long albumId) {
         this.albumId = albumId;
     }
 
     public void setVoteType(VoteType voteType) {
         this.voteType = voteType;
     }
 
     public Long getUserId() {
         return userId;
     }
 
     public Long getAlbumId() {
         return albumId;
     }
 
     public VoteType getVoteType() {
         return voteType;
     }
 }
