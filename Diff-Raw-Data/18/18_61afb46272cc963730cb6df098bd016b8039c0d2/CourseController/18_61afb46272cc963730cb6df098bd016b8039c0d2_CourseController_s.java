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
 import no.hials.muldvarpweb.domain.*;
 import no.hials.muldvarpweb.fragments.Fragment;
 import no.hials.muldvarpweb.fragments.FragmentModel;
 import no.hials.muldvarpweb.service.ArticleService;
 import no.hials.muldvarpweb.service.CourseService;
 import no.hials.muldvarpweb.service.LibraryService;
 import no.hials.muldvarpweb.service.QuizService;
 import no.hials.muldvarpweb.service.VideoService;
 import org.primefaces.model.DualListModel;
 
 /**
  *
  * @author kristoffer
  */
 @Named
 @SessionScoped
 public class CourseController implements Serializable {
     @Inject CourseService service;
     Course newCourse;
     Theme newTheme;
     Task newTask;
     ObligatoryTask newObligatoryTask;
     Exam newExam;
     Question newQuestion;
     Alternative newAlternative;
     List<Course> courses;
     Course selected;
     Course filter;
     Theme selectedTheme;
     Task selectedTask;
     ObligatoryTask selectedObligatoryTask;
     Exam selectedExam;
     List<Programme> programmes;
 
     public List<Course> getCourses() {
         //if(courses == null) {
             courses = service.findCourses();
         //}
         return courses;
     }
 
     public void setCourses(List<Course> courses) {
         this.courses = courses;
     }
 
     public Course getFilter() {
         return filter;
     }
     
 
     public void setFilter(Course filter) {
         this.filter = filter;
     }
 
     public Course getCourse() {
         if(newCourse == null) {
             newCourse = new Course();
             save();
         }
         
         return newCourse;
     }
 
     public void setCourse(Course course) {
         this.newCourse = course;
     }
 
     public Course getSelected() {
         return selected;
     }
 
     public String setSelected(Course selected) {
         if(selected == null) {
             newCourse = null;
             selected = getCourse();
         }
         this.selected = selected;
         videos = null;
         documents = null;
         quizzes = null;
         articles = null;
         fragmentBundle = null;
         fragmentModel = null;
         return "editCourse?faces-redirect=true";
     }
     
     public String setSelectedTheme(Theme selectedTheme) {
         this.selectedTheme = selectedTheme;
         return "editTheme";
     }
 
     public Theme getSelectedTheme() {
         return selectedTheme;
     }
 
     public Task getSelectedTask() {
         return selectedTask;
     }
     
     public String setSelectedTask(Task selectedTask) {
         this.selectedTask = selectedTask;
         return "editTask";
     }
 
     public Exam getSelectedExam() {
         return selectedExam;
     }
 
     public String setSelectedExam(Exam selectedExam) {
         this.selectedExam = selectedExam;
         return "editExam";
     }
     
 
     public ObligatoryTask getSelectedObligatoryTask() {
         return selectedObligatoryTask;
     }
 
     public String setSelectedObligatoryTask(ObligatoryTask selectedObligatoryTask) {
         this.selectedObligatoryTask = selectedObligatoryTask;
         return "editObligTask";
     }
 
     public CourseService getService() {
         return service;
     }
 
     public void setService(CourseService service) {
         this.service = service;
     }
     
     public String addCourse() {
         if(newCourse != null) {
             
 //            Date ts =  new Date();
 //            SimpleDateFormat sDF = new SimpleDateFormat("H:mm:ss dd.MM.yyyy");
 //        
 //            String date = sDF.format(ts);
             
             //newCourse.setLastUpdate(date);
             
             service.addCourse(newCourse);
         }
         return "listCourses";
     }
     
     public String editCourse() {
         if(selected != null) {
             service.editCourse(selected);
         }
         return "listCourses";
     }
     
     public String addNewRevCourse() {
         if(selected != null ) {
             service.addNewRevCourse(selected);
         }
         return "listCourses";
     }
     
     public String removeCourse() {
         if(selected != null) {
             service.removeCourse(selected);
         }
         return "listCourses";
     }
     
     public void addTheme() {
         if(newTheme != null && selected != null) {
             selected = service.addTheme(selected, newTheme);
             newTheme = null;
         }
     }
     
     public String editTheme() {
         if(selectedTheme != null) {
             service.editTheme(selected, selectedTheme);
         }
         return "editCourse?faces-redirect=true";
     }
     
     public String removeTheme(Theme theme) {
         if(selected != null) {
             service.removeTheme(selected, theme);
         }
         return "editCourse";
     }
     
     public void addExam() {
         if(newExam != null && selected != null) {
             selected = service.addExam(selected, newExam);
             newExam = null;
         }
     }
     
     public String removeExam(Exam exam) {
         if(selectedExam != null) {
             service.removeExam(selected, exam);
         }
         return "editCourse?faces-redirect=true";
     }
     
     public void addTask() {
         if(newTask != null && selected != null) {
             selected = service.addTask(selected, selectedTheme, newTask);
             newTask = null;
         }
     }
     
     public String editTask() {
         if(selectedTask != null) {
             service.editTask(selected, selectedTheme, selectedTask);
         }
         return "editTheme?faces-redirect=true";
     }
     
     public String removeTask(Task task) {
         if(selected != null) {
             service.removeTask(selected, selectedTheme, task);
         }
         return "editTheme?faces-redirect=true";
     }
     
     public void addObligatoryTask() {
         if(newObligatoryTask != null && selected != null) {
             selected = service.addObligatoryTask(selected, newObligatoryTask);
             newObligatoryTask = null;
         }
     }
     
     public String editObligatoryTask() {
         if(selectedObligatoryTask != null) {
             service.editObligatoryTask(selected, selectedObligatoryTask);
         }
         return "editCourse?faces-redirect=true";
     }
     
     public String removeObligatoryTask(ObligatoryTask obligtask) {
         if(selected != null) {
             service.removeObligatoryTask(selected, obligtask);
         }
         return "editCourse?faces-redirect=true";
     }
     
     public String acceptObligatoryTask() {
         if(selectedObligatoryTask != null) {
             service.acceptObligatoryTask(selected, selectedObligatoryTask);
         }
         return "editObligTask?faces-redirect=true";
     }
 
     public Exam getExam() {
         if(newExam == null) {
             newExam = new Exam();
         }
         return newExam;
     }
     
     public String editExam() {
         if(selectedExam != null) {
             service.editExam(selected, selectedExam);
         }
         return "editCourse?faces-redirect=true";
     }
 
     public void setExam(Exam newExam) {
         this.newExam = newExam;
     }
        
     public ObligatoryTask getObligatoryTask() {
         if(newObligatoryTask == null) {
             newObligatoryTask = new ObligatoryTask();
         }
         return newObligatoryTask;
     }
 
     public void setObligatoryTask(ObligatoryTask newObligatoryTask) {
         this.newObligatoryTask = newObligatoryTask;
     }
 
     public Task getTask() {
         if(newTask == null) {
             newTask = new Task();
         }
         return newTask;
     }
 
     public void setTask(Task newTask) {
         this.newTask = newTask;
     }
 
     public Theme getTheme() {
         if(newTheme == null) {
             newTheme = new Theme();
         }
         return newTheme;
     }
 
     public void setTheme(Theme newTheme) {
         this.newTheme = newTheme;
     }
     
     public void makeTestData() {
         service.makeTestData();
     }
     
 //     /**
 //     * This function retrieves a list of programs and creates a list of Select Items for use with JSF
 //     * 
 //     * 
 //     * @return List of SelectItem
 //     */
 //    public List<SelectItem> getProgrammeItems(List<Programme> programmeList) {
 //        
 //                 
 //        List<SelectItem> selectItems = new ArrayList<SelectItem>();
 //        
 //        
 //        //Loop once for each element in the supplied List of Programmes
 //        for (int i = 0; i < programmeList.size(); i++) {
 //            
 //            SelectItem currentSelectItem = new SelectItem(programmeList.get(i), programmeList.get(i).getName());
 //            
 //            //Loop once for every Programme in the selected course
 //            for(int n = 0; n < selected.getProgrammes().size() ; n++) {
 //                
 //                //Compare and check if there are matching ID's between programmes
 //                if(programmeList.get(i).getId() == selected.getProgrammes().get(n).getId()){
 //                    
 //                    //Set checkbox to true
 //                    currentSelectItem.setValue(true);
 //
 //                }
 //                
 //            }
 //            
 //            selectItems.add(currentSelectItem);
 //            
 //        }
 //        
 //        return selectItems;
 //    }  
         
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
 
     public List<Programme> getProgrammes() {
         this.programmes = service.getProgrammesInCourse(selected.getId());
         return programmes;
     }
 
     public void setProgrammes(List<Programme> programmes) {
         selected.setProgrammes(programmes);
     }
     
     public void addProgramme(Programme p) {
         selected.addProgramme(p);
     }
 
     // Quiz stuff
     private DualListModel<Quiz> quizzes;
     @Inject QuizService quizService;
     
     public DualListModel<Quiz> getQuizzes() {
         if(quizzes == null) {
             List<Quiz> source = quizService.findQuizzes();
             List<Quiz> target = new ArrayList<Quiz>();
             if(selectedFragment.getQuizzes() != null) {
                 target = selectedFragment.getQuizzes();
                 
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
     
     public void refreshLists() {
         quizzes = null;
         videos = null;
         documents = null;
         getQuizzes();
         getVideos();
         getDocuments();
     }
     
     public void addQuizzes(List<Quiz> q) {
         selectedFragment.setQuizzes(q);
     }
     
     // Video stuff
     private DualListModel<Video> videos;
     @Inject VideoService videoService;
 
     public DualListModel<Video> getVideos() {
         if(videos == null) {
             List<Video> source = videoService.findVideos();
             List<Video> target = new ArrayList<Video>();
             if(selectedFragment.getVideos() != null) {
                 target = selectedFragment.getVideos();
                 
                 for(int i = 0; i < target.size(); i++) {
                     Video v = target.get(i);
                     for(int k = 0; k < source.size(); k++) {
                         Video vv = source.get(k);
                         if(vv.getId().equals(v.getId())) {
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
     
     public void setVideos(DualListModel<Video> videos) {
         this.videos = videos;
     }
     
     public void addVideos(List<Video> v) {
         selectedFragment.setVideos(v);
         videos = null;
     }
     
     // Document stuff
     private DualListModel<LibraryItem> documents;
     @Inject LibraryService documentService;
     
     public DualListModel<LibraryItem> getDocuments() {
         System.out.println("getDocuments");
         if(documents == null) {
             System.out.println("getting docs");
             List<LibraryItem> source = documentService.getLibrary();
             for(LibraryItem d : source) {
                 System.out.println("Got doc: " + d.getTitle());
             }
             List<LibraryItem> target = new ArrayList<LibraryItem>();
             if(selectedFragment.getDocuments() != null) {
                 target = selectedFragment.getDocuments();
                 
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
         selectedFragment.setDocuments(v);
         documents = null;
     }
     
     // Article stuff
     @Inject ArticleService articleService;
     List<Article> articles;
     
     public List<Article> getArticles() {
         articles = articleService.findArticles();
         return articles;
     }
 
     // Fragment stuff
     
     String articlename;
     String newsname;
     String programmename;
     String quizname;
     String category;
     Article article;
     Fragment selectedFragment;
     FragmentModel fragmentModel;
     List<Fragment> fragmentBundle;
     String coursename;
     long parentId;
     private String videoname;
     private String documentname;
     
     public List<Fragment> getFragmentBundle() {
         if(fragmentBundle == null) {
             fragmentBundle = selected.getFragmentBundle();
         }
         return fragmentBundle;
     }
 
     public void setFragmentBundle(List<Fragment> fragmentBundle) {
         this.fragmentBundle = fragmentBundle;
     }
     
     public void addArticleFragment() {
         Fragment f = new Fragment(articlename, parentId, Fragment.Type.ARTICLE);
         f.setArticle(article);
         addFragment(f);
     }
     
     public void addNewsFragment() {
         Fragment f = new Fragment(newsname, parentId, Fragment.Type.NEWS);
         f.setCategory(category);
         addFragment(f);
     }
     
     public void addQuizFragment(List<Quiz> quizzes) {
         Fragment f = new Fragment(quizname, parentId, Fragment.Type.QUIZ);
         f.setQuizzes(quizzes);
         addFragment(f);
     }
     
     public void addVideoFragment(List<Video> videos) {
         Fragment f = new Fragment(videoname, parentId, Fragment.Type.VIDEO);
         f.setVideos(videos);
         addFragment(f);
     }
     
     public void addDocumentFragment(List<LibraryItem> documents) {
         Fragment f = new Fragment(documentname, parentId, Fragment.Type.DOCUMENT);
         f.setDocuments(documents);
         addFragment(f);
     }
     
     public void addFragment(Fragment f) {
         fragmentBundle.add(f);
         reset();
     }
     
     public void removeFragment(Fragment f) {
         fragmentBundle.remove(f);
     }
     
     public void reset() {
         articlename = "";
         newsname = "";
         quizname = "";
         videoname = "";
         documentname = "";
         category = "";
         article = null;
         quizzes = null;
         documents = null;
     }
     
     public String save() {
         if(selected != null) {
             try {
                     selected.setFragmentBundle(fragmentBundle);
             } catch(NullPointerException ex) {
                 System.out.println(ex);
             }
             selected = service.persist(selected);
         }
         return "editCourse";
     }
     
     public Fragment getSelectedFragment() {
         if(selectedFragment == null) {
             selectedFragment = new Fragment();
         }
         return selectedFragment;
     }
 
     public void setSelectedFragment(Fragment selectedFragment) { 
         this.selectedFragment = selectedFragment;
     }
 
     public FragmentModel getFragmentModel() {
         if(fragmentModel == null) {
             fragmentModel = new FragmentModel(getFragmentBundle());
         }
         return fragmentModel;
     }
 
     public void setFragmentModel(FragmentModel fragmentModel) {
         this.fragmentModel = fragmentModel;
     }
 
     public String getArticlename() {
         return articlename;
     }
 
     public void setArticlename(String articlename) {
         this.articlename = articlename;
     }
 
     public String getQuizname() {
         return quizname;
     }
 
     public void setQuizname(String quizname) {
         this.quizname = quizname;
     }
 
     public String getCategory() {
         return category;
     }
 
     public void setCategory(String category) {
         this.category = category;
     }
 
     public Article getArticle() {
         return article;
     }
 
     public void setArticle(Article article) {
         this.article = article;
     }
 
     public String getVideoname() {
         return videoname;
     }
 
     public void setVideoname(String videoname) {
         this.videoname = videoname;
     }
 
     public String getDocumentname() {
         return documentname;
     }
 
     public void setDocumentname(String documentname) {
         this.documentname = documentname;
     }
 
     public String getNewsname() {
         return newsname;
     }
 
     public void setNewsname(String newsname) {
         this.newsname = newsname;
     }
 }
