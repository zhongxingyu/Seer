 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  * This class represents a programme.
  * 
  * @author johan
  */
 
 @Entity
 @Table(name="Programme")
 public class Programme implements Serializable{
     
     @Id
     @GeneratedValue
     private Long id;
     
     @Column(name="name")
     String name;
     
     @Column(name="detail")
     String detail;
 
    @ManyToMany(mappedBy = "programmes")
     List<Course> courses;
     
     @ManyToMany
     List<Quiz> quizzes;
     
     @ManyToMany
     List<LibraryItem> documents;
     
     @ManyToMany
     List<Article> news;
     
     @ManyToMany
     List<Video> videos;
     
     // temp greier til vi lager noe bedre
     @ManyToOne
     Article info;
     
     @ManyToOne
     Article dates;
     
     @ManyToOne
     Article help;
     
     /**
      * Empty constructor for the Programme JPA class.
      */
     public Programme(){
         
     }
     
     /**
      * This is the constructor for the Programme JPA class.
      * 
      * @param name name of the Programme.
      * @param detail details about the Programme.
      */
     public Programme(String name, String detail) {
         this.name = name;
         this.detail = detail;   
     }
     
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
     
     public String getDetail() {
         return detail;
     }
 
     public void setDetail(String detail) {
         this.detail = detail;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
     
     public void addCourse(Course c) {
         getCourses().add(c);
     }
 
     public List<Course> getCourses() {
         if(courses == null) {
             courses = new ArrayList<Course>();
         }
         
         return courses;
     }
 
     public void setCourses(List<Course> courses) {
         this.courses = courses;
     }
 
     public void removeCourse(Course c) {
         getCourses().remove(c);
     }
     
     
 
 //    @Override
 //    public String toString() {
 //        return name;
 //    }
 
     public List<Quiz> getQuizzes() {
         return quizzes;
     }
 
     public void setQuizzes(List<Quiz> quizzes) {
         this.quizzes = quizzes;
     }
     
     public void remvoveQuiz(Quiz q){
         getQuizzes().remove(q);
     }
 
     public Article getInfo() {
         return info;
     }
 
     public void setInfo(Article info) {
         this.info = info;
     }
 
     public Article getDates() {
         return dates;
     }
 
     public void setDates(Article dates) {
         this.dates = dates;
     }
 
     public Article getHelp() {
         return help;
     }
 
     public void setHelp(Article help) {
         this.help = help;
     }
     
     @XmlTransient
     public List<Video> getVideos() {
         return videos;
     }
 
     public void setVideos(List<Video> video) {
         this.videos = video;
     }
     
     public void addVideo(Video video) {
         if(videos == null) {
             videos = new ArrayList<Video>();
         }
         videos.add(video);
     }
 
     public void removeVideo(Video video){
         
         getVideos().remove(video);
     }
 
     public List<LibraryItem> getDocuments() {
         return documents;
     }
     
     public void addDocument(LibraryItem document) {
         if(document == null) {
             documents = new ArrayList<LibraryItem>();
         }
         documents.add(document);
     }
 
     public void removeDocument(LibraryItem document){
         getDocuments().remove(document);
     }
 
     public void setDocuments(List<LibraryItem> documents) {
         this.documents = documents;
     }
 
     public List<Article> getNews() {
         return news;
     }
 
     public void setNews(List<Article> news) {
         this.news = news;
     }
 
 
 }
