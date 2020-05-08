 package com.adde.webbapp.model.entity;
 
 import java.io.Serializable;
 import java.util.*;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 
 /**
  * @author ehannes
  **/
 @Entity
 public class Article extends AbstractEntity implements Serializable {
 
    @OneToMany(cascade={CascadeType.REMOVE, CascadeType.MERGE})
     private List<ArticleEdit> articleEditions;
     private String title;
     private String content;
 
     public Article() {}
     
     public Article(String title, String content) {
         this.content = content;
         this.title = title;
        articleEditions = new LinkedList<>();
     }
     
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public List<ArticleEdit> getArticleEditions() {
         return articleEditions;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     private String allEditors() {
         String result = "";
         for(ArticleEdit see : articleEditions){
             result += see.toString();
         }
         return result;
     }
     
     @Override
     public String toString() {
         return "Article{" + super.toString() + " Title: " + title
                 + " Content: " + content + allEditors();
     }
 }
