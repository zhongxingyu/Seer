 package com.adde.webbapp.model.entity;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
import java.util.GregorianCalendar;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  *
  *
  * @author Joakim Danielsson
  */
 @Entity
 public class Project extends AbstractEntity implements Serializable {
 
     private String name;
     @Temporal(TemporalType.DATE)
     private Calendar dateCreated;
     @ManyToOne
     private Person admin;
     @ManyToMany(cascade = CascadeType.REFRESH)
     private List<Person> collaborators;
     @OneToMany(cascade = CascadeType.ALL)
     private List<Article> articles;
     @OneToMany(cascade = CascadeType.ALL)
     private List<WallPost> wallPosts;
     @OneToMany(cascade = CascadeType.ALL)
     private List<TodoPost> todoPosts;
 
     //private List<TodoPost> milestonePosts;
     
     public Project() {
     }
 
     public Project(String name, Person admin) {
         this.name = name;
         this.admin = admin;
         this.collaborators = new ArrayList<>();
         this.articles = new ArrayList<>();
         this.wallPosts = new ArrayList<>();
         this.todoPosts = new ArrayList<>();
        //this.milestonePosts = new ArrayList<>();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String newName) {
         this.name = newName;
     }
 
     public Person getAdmin() {
         return admin;
     }

    public void setAdmin(Person currentAdmin, Person newAdmin) throws Exception {
        if (currentAdmin.equals(admin)) {
            admin = newAdmin;
            collaborators.add(admin);
            collaborators.remove(newAdmin);
        }
        throw new Exception("Current admin in setAdmin was incorrect");
     }
 
     public Calendar getDateCreated() {
         return dateCreated;
     }
 
     public void setDateCreated(Calendar dateCreated) {
         this.dateCreated = dateCreated;
     }
 
     public List<Person> getCollaborators() {
         return collaborators;
     }
 
     public List<TodoPost> getTodoPosts() {
         return todoPosts;
     }
 
     public List<Article> getArticles() {
         return articles;
     }
 
     public List<WallPost> getWallPosts() {
         return wallPosts;
     }
     
 //    public List<TodoPost> getMilestonePosts() {
 //        return milestonePosts;
 //    }
 
 
     @Override
     public String toString() {
         return "Project{Name: " + name + "Admin: " + admin + " Id: " + super.toString()
                 + "}";
     }
 }
