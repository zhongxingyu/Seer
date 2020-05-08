 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.web;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import no.hials.muldvarpweb.domain.Article;
 import no.hials.muldvarpweb.domain.Course;
 import no.hials.muldvarpweb.domain.LibraryItem;
 import no.hials.muldvarpweb.domain.Programme;
 import no.hials.muldvarpweb.domain.Quiz;
 import no.hials.muldvarpweb.domain.Video;
 import no.hials.muldvarpweb.service.ArticleService;
 import no.hials.muldvarpweb.service.CourseService;
 import no.hials.muldvarpweb.service.LibraryService;
 import no.hials.muldvarpweb.service.ProgrammeService;
 import no.hials.muldvarpweb.service.QuizService;
 import no.hials.muldvarpweb.service.VideoService;
 import org.primefaces.model.DualListModel;
 
 /**
  * This is the controller for the ProgrammeService.
  * 
  * @author johan
  */
 @Named
 @SessionScoped
 public class ProgrammeController implements Serializable{
     
     @Inject ProgrammeService service;
     
     List<Programme> programmeList;
     Programme newProgramme;
     Programme selected;
     
     
     /**
      * This function returns the selectedProgramme variable.
      * 
      * @return The selected Programme
      */
     public Programme getSelected(){
         
         //Check if the selectedProgramme variable is null, and set new Programme if it is
         if (selected == null) {
             
             selected = new Programme();
         }
                 
         return selected;
     }
     
     public String setSelected(Programme selected) {
         if(selected == null) {
             newProgramme = null;
             selected = getProgramme();
         }
         this.selected = selected;
         courses = null;
         videos = null;
         documents = null;
         quizzes = null;
         return "editProgramme?faces-redirect=true";
     }
 
     public Programme getProgramme() {
         if(newProgramme == null) {
             newProgramme = new Programme();
         }
         return newProgramme;
     }
 
     public void setProgramme(Programme newProgramme) {
         this.newProgramme = newProgramme;
     }
     
     public String editProgramme() {
         if(selected != null) {
             service.editProgramme(selected);
         }
         return "listProgramme";
     }
     
     public String removeProgramme() {
         if(selected != null ) {
             service.removeProgramme(selected);
         }
         return "listProgramme?faces-redirect=true";
     }
     
     /**
      * This function returns a List of Programmes from the injected ProgrammeService.
      * 
      * @return List of Programmes
      */
     public List<Programme> getProgrammes() {
         programmeList = service.findProgrammes();
         return programmeList;
     }
 
     public void setProgramme(List<Programme> programmeList) {
         this.programmeList = programmeList;
     }
     
     /**
      * This function makes a call to the ProgrammeService instantiation and adds the supplied Programme.
      * 
      * @param newProgramme The Programme to be added.
      * @return 
      */
     public Programme addProgramme() {
         
 //        //Check if there is a Programme to add
 //        if(selectedProgramme != null){
 //        
 //            
 //        }
         
         service.addProgramme(selected);
         return newProgramme;
     }
     
     public void addInfo(int i) {  
         switch(i) {
             case 1:
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "INFO: ", "Changes saved"));
                 break;
             case 2:
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "INFO: ", "Course deleted"));
                 break;
             case 3:
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "INFO: ", "New revision created"));
                 break;
          }
     }
     
     public void removeCourseFromProgramme(Course c) {
         service.removeCourseFromProgramme(selected, c);
     }
     
     // experiment zone
     private DualListModel<Course> courses;
     @Inject CourseService courseService;
 
     public DualListModel<Course> getCourses() {
         if(courses == null) {
             List<Course> source = courseService.findCourses();
            List<Course> target = selected.getCourses();
             if(selected.getCourses() != null) {
                 target = selected.getCourses();
                 
                 for(int i = 0; i < target.size(); i++) {
                     Course v = target.get(i);
                     for(int k = 0; k < source.size(); k++) {
                         Course vv = source.get(k);
                         if(vv.getId().equals(v.getId())) {
                             source.remove(vv);
                             break;
                         }
                     }
                 }
             }
             
             courses = new DualListModel<Course>(source, target);
         }
         
         return courses;
     }
 
     public void setCourses(DualListModel<Course> course) {
         this.courses = course;
     }
     
     public void addCourses(List<Course> c) {
         selected = service.setCourses(selected, courses.getTarget(), selected.getCourses());
         //selected = service.addCourses(selected, courses.getTarget());
     }
     
     public void setCourses(List<Course> c) {
         selected.setCourses(c);
     }
     
     // Video stuff
     private DualListModel<Video> videos;
     @Inject VideoService videoService;
 
     public DualListModel<Video> getVideos() {
         if(videos == null) {
             List<Video> source = videoService.findVideos();
             List<Video> target = new ArrayList<Video>();
             if(selected.getVideos() != null) {
                 target = selected.getVideos();
                 
                 for(int i = 0; i < target.size(); i++) {
                     Video v = target.get(i);
                     System.out.println("Checking Video " + v.getVideoName());
                     for(int k = 0; k < source.size(); k++) {
                         Video vv = source.get(k);
                         System.out.println("Comparing " + v.getVideoName() + " to " + vv.getVideoName());
                         if(vv.getId().equals(v.getId())) {
                             System.out.println("It's the same!");
                             source.remove(vv);
                             break;
                         }
                     }
                 }
             }
             
             videos = new DualListModel<Video>(source, target);
         }
         
         return videos;
     }
     
 
 //    public DualListModel<Video> getVideos2() {
 //        DualListModel<Video> result = new DualListModel<Video>(videoService.findVideos(),new ArrayList<Video>());
 //        System.out.println("Result is size " + result.getSource().size());
 //        return result;
 //    }
     
     public void setVideos(DualListModel<Video> videos) {
         this.videos = videos;
     }
     
     public void addVideos(List<Video> v) {
         selected = service.setVideos(selected, videos.getTarget());
         /*for(Video vv : v) {
             service.addVideo(selected, vv);
             //selected.addVideo(vv);
             //vv.addCourse(selected);
         }*/
     }
     
     public void setVideos(List<Video> v) {
         selected.setVideos(v);
     }
     
     // Document stuff
     private DualListModel<LibraryItem> documents;
     @Inject LibraryService documentService;
     
     public DualListModel<LibraryItem> getDocuments() {
         if(documents == null) {
             List<LibraryItem> source = documentService.getLibrary();
             List<LibraryItem> target = new ArrayList<LibraryItem>();
             if(selected.getVideos() != null) {
                 target = selected.getDocuments();
                 
                 for(int i = 0; i < target.size(); i++) {
                     LibraryItem v = target.get(i);
                     for(int k = 0; k < source.size(); k++) {
                         LibraryItem vv = source.get(k);
                         if(vv.getId().equals(v.getId())) {
                             source.remove(vv);
                             break;
                         }
                     }
                 }
             }
             
             documents = new DualListModel<LibraryItem>(source, target);
         }
         
         return documents;
     }
     
     public void setDocuments(DualListModel<LibraryItem> document) {
         this.documents = document;
     }
     
     public void addDocuments(List<LibraryItem> v) {
         selected = service.setDocuments(selected, documents.getTarget());
     }
     
     // Quiz stuff
     private DualListModel<Quiz> quizzes;
     @Inject QuizService quizService;
     
     public DualListModel<Quiz> getQuizzes() {
         if(quizzes == null) {
             List<Quiz> source = quizService.findQuizzes();
             List<Quiz> target = new ArrayList<Quiz>();
             if(selected.getQuizzes() != null) {
                 target = selected.getQuizzes();
                 
                 for(int i = 0; i < target.size(); i++) {
                     Quiz v = target.get(i);
                     for(int k = 0; k < source.size(); k++) {
                         Quiz vv = source.get(k);
                         if(vv.getId() == v.getId()) {
                             source.remove(vv);
                             break;
                         }
                     }
                 }
             }
             
             quizzes = new DualListModel<Quiz>(source, target);
         }
         
         return quizzes;
     }
     
     public void setQuizzes(DualListModel<Quiz> quizzes) {
         this.quizzes = quizzes;
     }
     
     public void addQuizzes(List<Quiz> q) {
         selected = service.setQuizzes(selected, quizzes.getTarget());
     }
     
     // Article stuff
     @Inject ArticleService articleService;
     List<Article> articles;
     
     public List<Article> getArticles() {
         articles = articleService.findArticles();
         return articles;
     }
 
     public Article getInformation() {
         return selected.getInfo();
     }
 
     public void setInformation(Article information) {
         selected.setInfo(information);
     }
 
     public Article getDates() {
         return selected.getDates();
     }
 
     public void setDates(Article dates) {
         selected.setDates(dates);
     }
 
     public Article getHelp() {
         return selected.getHelp();
     }
 
     public void setHelp(Article help) {
         selected.setHelp(help);
     }
 }
