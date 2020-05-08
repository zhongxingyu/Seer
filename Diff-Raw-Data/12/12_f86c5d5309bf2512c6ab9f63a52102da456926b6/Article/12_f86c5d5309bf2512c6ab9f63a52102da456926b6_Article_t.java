 package no.hials.muldvarpweb.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.*;
 
 /**
  *
  * @author mikael
  */
 @Entity
 public class Article implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     
     String title;
     String category;
     @Temporal(javax.persistence.TemporalType.DATE)
    Date date;
     String ingress;
     String author;
     String text;
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public String getCategory() {
         return category;
     }
 
     public void setCategory(String category) {
         this.category = category;
     }
 
     public String getIngress() {
         return ingress;
     }
 
     public void setIngress(String ingress) {
         this.ingress = ingress;
     }
 
    public Date getDate() {
        return date;
     }
 
    public void setDate(Date publishdate) {
        this.date = publishdate;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
     String description;
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
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
         if (!(object instanceof Article)) {
             return false;
         }
         Article other = (Article) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "no.hials.muldvarp.web.Article[ id=" + id + " ]";
     }
 
 }
