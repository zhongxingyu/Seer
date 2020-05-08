 package com.forum.domain;
 
 import com.forum.service.validation.ContentLength;
 import org.hibernate.validator.constraints.NotEmpty;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class Advice implements Serializable {
     private int id;
     private int questionId;
     private Date createdAt;
     private User user;
     @NotEmpty
     @ContentLength
     private String description;
 
     public Advice() {
     }
 
     public Advice(int questionId, User user, String description) {
         this.questionId = questionId;
         this.user = user;
         this.description = description;
 
     }
 
     public int getQuestionId() {
         return questionId;
     }
 
     @Override
     public String toString() {
         return "Advice{" +
                 "id=" + id +
                 ", questionId=" + questionId +
                 ", createdAt=" + createdAt +
                 ", user=" + user +
                 ", description='" + description + '\'' +
                 '}';
     }
 
     public void setCreatedAt(Date createdAt) {
         this.createdAt = createdAt;
     }
 
     public void setQuestionId(int questionId) {
         this.questionId = questionId;
     }
 
     public Date getCreatedAt() {
 
         return createdAt;
     }
 
     public User getUser() {
 
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Advice advice = (Advice) o;
 
         if (questionId != advice.questionId) return false;
         if (createdAt != null ? !createdAt.equals(advice.createdAt) : advice.createdAt != null) return false;
         if (description != null ? !description.equals(advice.description) : advice.description != null) return false;
         if (user != null ? !user.equals(advice.user) : advice.user != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = questionId;
         result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
         result = 31 * result + (user != null ? user.hashCode() : 0);
         result = 31 * result + (description != null ? description.hashCode() : 0);
         return result;
     }
 }
