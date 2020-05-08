 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.supinblog.services.entities;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 
 /**
  *
  * @author popi
  */
 @Entity
 @Table(name="COMMENTS")
 public class Comment implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     
     private String content;
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     private Date creationDate;
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     private Date modificationDate;
     
     @ManyToOne
     private UserAccount author;
     @ManyToOne
     private Post post;
     
 // <editor-fold defaultstate="collapsed" desc="Generated, getters, setters"> 
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Comment)) {
             return false;
         }
         Comment other = (Comment) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
     
         public UserAccount getAuthor() {
         return author;
     }
 
     public void setAuthor(UserAccount author) {
         this.author = author;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Date getModificationDate() {
         return modificationDate;
     }
 
     public void setModificationDate(Date modificationDate) {
         this.modificationDate = modificationDate;
     }
 
     public Post getPost() {
         return post;
     }
 
     public void setPost(Post post) {
         this.post = post;
     }

    @Override
    public String toString() {
        return "com.supinblog.services.entities.Comment[ id=" + id + " ]";
    }

 // </editor-fold>
 
 }
