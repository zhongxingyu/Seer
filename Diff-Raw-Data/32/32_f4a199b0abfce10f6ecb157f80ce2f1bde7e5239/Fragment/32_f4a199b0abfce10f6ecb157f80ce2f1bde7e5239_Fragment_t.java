 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.fragments;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import no.hials.muldvarpweb.domain.Article;
 import no.hials.muldvarpweb.domain.Course;
 import no.hials.muldvarpweb.domain.LibraryItem;
 import no.hials.muldvarpweb.domain.Quiz;
 import no.hials.muldvarpweb.domain.Video;
 
 /**
  *
  * @author Kristoffer
  */
 @Entity
 @Table(name = "fragment")
 public class Fragment implements Serializable {
     @Id
     @GeneratedValue
     private Long id;
     String name;
     public enum Type {
        FRONTPAGE, PROGRAMME, COURSE, NEWS, ARTICLE, QUIZ, DOCUMENT, VIDEO, BIBSYS, FRONTER
     }
     Type fragmentType;
     
     @ManyToOne(cascade = CascadeType.ALL)
     Article article;
     String category;
     String iconurl;
 
     public Fragment() {
     }
 
     public Fragment(String name, long parentID, Type fragmentType) {
         this.name = name;
         this.fragmentType = fragmentType;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Type getFragmentType() {
         return fragmentType;
     }
 
     public void setFragmentType(Type fragmentType) {
         this.fragmentType = fragmentType;
     }
 
     public Article getArticle() {
         return article;
     }
 
     public void setArticle(Article article) {
         this.article = article;
 //        article.setFragment(this);
     }
 
     public String getCategory() {
         return category;
     }
 
     public void setCategory(String category) {
         this.category = category;
     }
 
     public String getIconurl() {
         switch(fragmentType) {
             case ARTICLE:
                 iconurl = "../resources/images/stolen_contacts.png";
                 break;
             case COURSE:
                 iconurl = "../resources/images/stolen_course_programme.png";
                 break;
             case FRONTPAGE:
                 iconurl = "../resources/images/stolen_contacts.png";
                 break;
             case NEWS:
                 iconurl = "../resources/images/stolen_news.png";
                 break;
             case PROGRAMME:
                 iconurl = "../resources/images/stolen_course_programme.png";
                 break;
             case QUIZ:
                 iconurl = "../resources/images/stolen_quiz.png";
                 break;
             case VIDEO:
                 iconurl = "../resources/images/stolen_videos.png";
                 break;
             case DOCUMENT:
                 iconurl = "../resources/images/stolen_docs.png";
                 break;
            case BIBSYS:
                iconurl = "../resources/images/bibsys.png";
                break;
            case FRONTER:
                iconurl = "../resources/images/fronter.png";
                break;
         }
         return iconurl;
     }
     
     @ManyToMany(cascade = CascadeType.ALL)
     List<Quiz> quizzes;
 
     public List<Quiz> getQuizzes() {
         return quizzes;
     }
 
     public void setQuizzes(List<Quiz> quizzes) {
         this.quizzes = quizzes;
         for(Quiz q : quizzes) {
             q.getFragments().add(this);
         }
     }
     
     @ManyToMany(cascade = CascadeType.ALL)
     List<Course> courses;
 
     public List<Course> getCourses() {
         return courses;
     }
 
     public void setCourses(List<Course> courses) {
         this.courses = courses;
         for(Course c : courses) {
             c.getFragments().add(this);
         }
     }
     
     @ManyToMany(cascade = CascadeType.ALL)
     List<LibraryItem> documents;
 
     public List<LibraryItem> getDocuments() {
         return documents;
     }
 
     public void setDocuments(List<LibraryItem> documents) {
         this.documents = documents;
         for(LibraryItem d : documents) {
             d.getFragments().add(this);
         }
     }
     
     @ManyToMany(cascade = CascadeType.ALL)
     List<Video> videos;
 
     public List<Video> getVideos() {
         return videos;
     }
 
     public void setVideos(List<Video> videos) {
         this.videos = videos;
         for(Video v : videos) {
             v.getFragments().add(this);
         }
     }
 }
