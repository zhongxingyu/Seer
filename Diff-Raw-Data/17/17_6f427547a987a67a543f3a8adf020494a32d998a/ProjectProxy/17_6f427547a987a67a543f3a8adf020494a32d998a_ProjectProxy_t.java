 package com.adde.webbapp.frontend.rest;
 
 import com.adde.webbapp.model.entity.Article;
 import com.adde.webbapp.model.entity.Person;
 import com.adde.webbapp.model.entity.Project;
 import com.adde.webbapp.model.entity.TodoPost;
 import com.adde.webbapp.model.entity.WallPost;
 import java.util.Calendar;
 import java.util.List;
 import javax.xml.bind.annotation.*;
 
 /**
  * Need this because translation from XML to JSON
  *
  * @author hajo
  */
 @XmlRootElement(name = "project")
 @XmlType(name = "ProjectType")
 public class ProjectProxy {
 
     private Project project;
 
     protected ProjectProxy() { 
     }
 
     public ProjectProxy(Project project) {
         this.project = project;
     }
 
     @XmlElement(required = true)
     public String getName() {
         return project.getName();
     }
 
     @XmlElement(required = true)
     public Long getId() {
         return project.getId();
     }
 
     @XmlElement(required = true)
    public Person getAdmin() {
         return project.getAdmin();
     }
 
     @XmlElement(required = true)
     public List<Person> getCollaborators() {
         return project.getCollaborators();
     }
 
     @XmlElement(required = true)
     public List<Article> getArticles() {
         return project.getArticles();
     }
 
     @XmlElement(required = true)
     public List<TodoPost> getTodoPosts() {
         return project.getTodoPosts();
     }
 
     @XmlElement(required = true)
     public List<WallPost> getWallPosts() {
         return project.getWallPosts();
     }
 
     @XmlElement(required = true)
     public Calendar getDateCreated() {
         return project.getDateCreated();
     }
 }
