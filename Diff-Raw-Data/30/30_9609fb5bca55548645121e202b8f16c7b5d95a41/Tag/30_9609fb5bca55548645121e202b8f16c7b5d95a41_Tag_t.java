 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.supinblog.services.entities;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 /**
  *
  * @author popi
  */
 @Entity
 @NamedQueries({
 @NamedQuery(name="Tag.all",query="SELECT t FROM Tag AS t"),
 @NamedQuery(name="Tag.withName",query="SELECT t FROM Tag AS t WHERE t.name = :name"),
 @NamedQuery(name="Tag.withId",query="SELECT t FROM Tag AS t WHERE t.id = :id"),
 })
 @Table(name="CATEGORIES")
 public class Tag implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     
     private String name;
     private String description;
     
    @ManyToMany(mappedBy = "tags",cascade={CascadeType.ALL})
     @JoinTable(name="POSTS_CATEGORIES")
     private List<Post> posts;
     
     @Override
     public String toString() {
         return name;
     }
     
 // <editor-fold defaultstate="collapsed" desc="Generated, getters, setters">
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<Post> getPosts() {
         return posts;
     }
 
     public void setPosts(List<Post> posts) {
         this.posts = posts;
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
         if (!(object instanceof Tag)) {
             return false;
         }
         Tag other = (Tag) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 //</editor-fold>
 }
