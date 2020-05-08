 package com.mycompany.entity;
 
 import javax.persistence.*;
 import javax.validation.constraints.Size;
 import java.util.Collection;
 
 import static javax.persistence.CascadeType.ALL;
 
 /**
 
  * Created with IntelliJ IDEA.
  * User: fermin
  * Date: 20/10/12
  * Time: 16:53
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 public class Feed {
 
     /* Fields */
 
     @Id
     @GeneratedValue
     private int id;
 
     @Size(max = 100)
     private String name;
 
     @OneToMany(cascade = ALL, mappedBy = "feed")
     private Collection<Post> posts;
 
    //@ManyToMany(mappedBy = "subscribedFeeds")
    @ManyToMany
     private Collection<User> subscribedUsers;
 
     @ManyToOne
     private Company company;
 
     /* Setter and Getter methods */
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Collection<Post> getPosts() {
         return posts;
     }
 
     public void setPosts(Collection<Post> posts) {
         this.posts = posts;
     }
 
     public Collection<User> getSubscribedUsers() {
         return subscribedUsers;
     }
 
     public void setSubscribedUsers(Collection<User> subscribedUsers) {
         this.subscribedUsers = subscribedUsers;
     }
 
     public Company getCompany() {
         return company;
     }
 
     public void setCompany(Company company) {
         this.company = company;
     }
 }
