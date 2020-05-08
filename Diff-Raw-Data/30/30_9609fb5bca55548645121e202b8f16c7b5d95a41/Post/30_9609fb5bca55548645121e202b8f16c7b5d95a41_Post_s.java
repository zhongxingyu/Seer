 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.supinblog.services.entities;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 
 /**
  *
  * @author popi
  */
 @Entity
 @NamedQueries({
 @NamedQuery(name="Post.all",query="SELECT p FROM Post AS p ORDER BY p.creationDate DESC"),
 })
 @Table(name="POSTS")
 public class Post implements Serializable {
 
     public Post() {
     }
     
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     
     private String title;
     private String content;
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     private Date creationDate;
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     private Date modificationDate;
     
     @ManyToOne
     private UserAccount author;
     @OneToMany(mappedBy = "post")
     private List<Comment> comments;
    @ManyToMany(mappedBy = "posts")
     private List<Tag> tags;
     
     @Override
     public String toString() {
         return "com.supinblog.services.entities.Post[ id=" + id + " ]";
     }
 
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
 
     public UserAccount getAuthor() {
         return author;
     }
 
     public void setAuthor(UserAccount author) {
         this.author = author;
     }
 
     public List<Tag> getTags() {
         return tags;
     }
 
     public void setTags(List<Tag> tags) {
         this.tags = tags;
     }
 
     public List<Comment> getComments() {
         return comments;
     }
 
     public void setComments(List<Comment> comments) {
         this.comments = comments;
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
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Post)) {
             return false;
         }
         Post other = (Post) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 //</editor-fold>
 }
