 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.supinblog.controlers;
 
 import com.supinblog.services.entities.Post;
 import com.supinblog.services.entities.Tag;
 import com.supinblog.services.entities.UserAccount;
 import com.supinblog.services.services.BlogServiceLocal;
 import java.io.Serializable;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.bean.ManagedBean;
 import javax.inject.Named;
 
 /**
  *
  * @author popi
  */
 @ManagedBean
 @Named(value="blogControler")
 @SessionScoped
 public class BlogControler implements Serializable {
     @EJB
     private BlogServiceLocal serv;// link to entities service.
 
     private String name;         // name used to auth
     private String password;     // password used to auth
     private UserAccount user;    // curent User if auth.
     private UserAccount newUser; // tmp User used to register.
     private Tag newTag;
     private Post newPost;
     
     public String resetUser(){
         this.user=null;
         return "index";
     }
     
     public String auth(){
         user= serv.getAuthenticatedUser(name, password);
         return user==null?"login":"index";
     }
     public String addUser(){
         serv.addUser(this.newUser);
         return "login";
     }
 
     public BlogServiceLocal getServ() {
         return serv;
     }
     
     public List<Post> getAllPosts() {
         return serv.getPosts();
     }
     
     public List<Tag> getAllTags() {
         return serv.getTags();
     }
 
     public String savePost(){
         if (newPost.getId() != null && newPost.getId() != 0) {
            serv.updatePost(newPost);
         }else{
             newPost.setAuthor(user);
             serv.addPost(newPost);
         }
         newPost=null;
         return "blog";
     }
     public String saveTag(){
         if (newTag.getId() != null && newTag.getId() != 0) {
            serv.updateTag(newTag);
         }else{
             serv.addTag(this.newTag);
         }
         this.newTag=null;
         return "blog";
     }
     
     /** Creates a new instance of BlogControler */
     public BlogControler() {
     }
     
     // <editor-fold defaultstate="collapsed" desc="Generated, getters, setters">
     public Post getNewPost() {
         if(newPost==null)
         {
             newPost = new Post();
         }
         return newPost;
     }
 
     public void setNewPost(Post newPost) {
         if(this.newPost==null)
         {
             this.newPost = new Post();
         }
         this.newPost = newPost;
     }
     
     public Tag getNewTag() {
         if(newTag==null)
             newTag = new Tag();
         return newTag;
     }
     
     public void setNewTag(Tag newTag) {
         if(this.newTag==null)
             this.newTag = new Tag();
         this.newTag = newTag;
     }
         public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
     
     public UserAccount getNewUser() {
         if(this.newUser==null)
             this.newUser = new UserAccount();
         return newUser;
     }
 
     public void setNewUser(UserAccount newUser) {
         if(this.newUser==null)
             this.newUser = new UserAccount();
         this.newUser = newUser;
     }
 
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
     
     public UserAccount getUser() {
         return user;
     }
     //</editor-fold>
 }
