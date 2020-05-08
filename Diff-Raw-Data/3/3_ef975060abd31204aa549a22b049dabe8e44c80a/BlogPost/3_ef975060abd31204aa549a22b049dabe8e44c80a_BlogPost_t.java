 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entities;
 
 import java.io.Serializable;
 import java.util.Date;
import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Temporal;
 
 /**
  *
  * @author Patrik Larsson
  */
 @Entity
 public class BlogPost implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     private String title;
    @Column(length = 1024)
     private String message;
   
     @Temporal(javax.persistence.TemporalType.DATE)
     private Date postDate;
   
     private Blog blog;
     
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public Date getPostDate() {
         return postDate;
     }
 
     public void setPostDate(Date postDate) {
         this.postDate = postDate;
     }
 
     public Blog getBlog() {
         return blog;
     }
 
     public void setBlog(Blog blog) {
         this.blog = blog;
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
         if (!(object instanceof BlogPost)) {
             return false;
         }
         BlogPost other = (BlogPost) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "entities.BlogPost[ id=" + id + " ]";
     }
     
 }
