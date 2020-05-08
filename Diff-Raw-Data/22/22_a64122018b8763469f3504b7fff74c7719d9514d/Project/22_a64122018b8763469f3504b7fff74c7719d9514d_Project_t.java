 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.adde.webbapp_model;
 
 import com.adde.webbapp_model.DeadlinePost.Priority;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Objects;
 import java.util.Random;
 
 /**
  *
  *
  * @author Joakim Danielsson
  */
 public class Project {
 
     private String name;
     private final long id;
     private User admin;
    private List<User> users;
    private List<DeadlinePost> deadlinePosts;
    private List<MilestonePost> milestonePosts;
    private List<Article> articles;
    private List<WallPost> wallPosts;
 
     public Project(String name, User admin) {
         id = new Long(new Random().nextInt(1000));
         this.name = name;
         this.admin = admin;
        this.users = new ArrayList<User>();
        this.deadlinePosts = new ArrayList<DeadlinePost>();
        this.milestonePosts = new ArrayList<MilestonePost>();
        this.articles = new ArrayList<Article>();
        this.wallPosts = new ArrayList<WallPost>();
     }
 
     public Project(long id, String name, User admin) {
         this.id = id;
         this.name = name;
         this.admin = admin;
        this.users = new ArrayList<User>();
        this.deadlinePosts = new ArrayList<DeadlinePost>();
        this.milestonePosts = new ArrayList<MilestonePost>();
        this.articles = new ArrayList<Article>();
        this.wallPosts = new ArrayList<WallPost>();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String newName) {
         this.name = newName;
     }
 
     public long getId() {
         return id;
     }
     
     public boolean setAdmin(User currentAdmin, User newAdmin){
         if(currentAdmin.equals(admin)){
             admin = newAdmin;
             return true;
         }
         return false;
     }
 
     public List<User> getUsers() {
         return users;
     }
 
     public List<DeadlinePost> getDeadlinePosts() {
         return deadlinePosts;
     }
 
     public List<MilestonePost> getMilestonePosts() {
         return milestonePosts;
     }
 
     public List<Article> getArticles() {
         return articles;
     }
 
     public List<WallPost> getWallPosts() {
         return wallPosts;
     }
 
     public void addUser(User user) {
         users.add(user);
     }
 
     public void createDeadlinePost(User author, User responsibleUser, String msg,
             Date deadline, Priority priority) {
         deadlinePosts.add(new DeadlinePost(author, responsibleUser, msg,
                 deadline, priority));
     }
 
     public void createMilestonePost(User author, User responsibleUser, String msg,
             Date deadline, Priority priority, List<User> assignedTo) {
         milestonePosts.add(new MilestonePost(author, responsibleUser, msg,
                 deadline, priority, assignedTo));
     }
 
     public void createArticle(User author, String content) {
         articles.add(new Article(author, content));
     }
 
     public void createWallPost(User author, String msg) {
         wallPosts.add(new WallPost(author, msg));
     }
 
     public void deleteUser(User u) {
         users.remove(u);
     }
 
     public void deleteArticle(Article a) {
         articles.remove(a);
     }
 
     public void deleteDeadlinePost(DeadlinePost d) {
         deadlinePosts.remove(d);
     }
 
     public void deleteMilestonePost(MilestonePost p) {
         milestonePosts.remove(p);
 
     }
 
     public void deleteWallPost(WallPost w) {
         wallPosts.remove(w);
     }
 
     @Override
     public String toString() {
         return "Project{Name: " + name + " Id: " + id;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 71 * hash + Objects.hashCode(this.id);
         return hash;
     }
 
     @Override
     public boolean equals(Object o) {
         if (o == null) {
             return false;
         }
         if (o instanceof Project) {
             Project p = (Project) o;
             if (p.getId() == this.id) { //equal if same id
                 return true;
             }
         }
         return false;
     }
 }
