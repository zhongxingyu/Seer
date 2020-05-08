 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.web;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import no.hials.muldvarpweb.domain.*;
 import no.hials.muldvarpweb.service.CourseService;
 
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
     List<Course> courses;
     Course selected;
     Course filter;
     Theme selectedTheme;
     Task selectedTask;
     ObligatoryTask selectedObligatoryTask;
     Exam selectedExam;
 
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
         if(newCourse == null)
             newCourse = new Course();
         
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
             selected = getCourse();
         }
         this.selected = selected;
         return "editCourse";
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
             service.addTheme(selected, newTheme);
             newTheme = null;
         }
     }
     
     public String editTheme() {
         if(selectedTheme != null) {
             service.editTheme(selected, selectedTheme);
         }
         return "editCourse";
     }
     
     public String removeTheme(Theme theme) {
         if(selected != null) {
             service.removeTheme(selected, theme);
         }
         return "editCourse";
     }
     
     public void addExam() {
         if(newExam != null && selected != null) {
             service.addExam(selected, newExam);
             newExam = null;
         }
     }
     
     public String removeExam(Exam exam) {
         if(selected != null) {
             service.removeExam(selected, exam);
         }
         return "editCourse";
     }
     
     public void addTask() {
         if(newTask != null && selected != null) {
             service.addTask(selected, selectedTheme, newTask);
             newTask = null;
         }
     }
     
     public String editTask() {
         if(selectedTask != null) {
             service.editTask(selected, selectedTheme, selectedTask);
         }
         return "editCourse";
     }
     
     public String removeTask(Task task) {
         if(selected != null) {
             service.removeTask(selected, selectedTheme, task);
         }
         return "editTheme";
     }
     
     public void addObligatoryTask() {
         if(newObligatoryTask != null && selected != null) {
             service.addObligatoryTask(selected, newObligatoryTask);
             newObligatoryTask = null;
         }
     }
     
     public String editObligatoryTask() {
         if(selectedObligatoryTask != null) {
             service.editObligatoryTask(selected, selectedObligatoryTask);
         }
         return "editCourse";
     }
     
     public String removeObligatoryTask(ObligatoryTask obligtask) {
         if(selected != null) {
             service.removeObligatoryTask(selected, obligtask);
         }
         return "editCourse";
     }
 
     public Exam getExam() {
         if(newExam == null)
             newExam = new Exam();
         return newExam;
     }
     
     public void editExam() {
         if(selectedExam != null) {
             service.editExam(selected, selectedExam);
         }
     }
 
     public void setExam(Exam newExam) {
         this.newExam = newExam;
     }
 
     public ObligatoryTask getObligatoryTask() {
         if(newObligatoryTask == null)
             newObligatoryTask = new ObligatoryTask();
         return newObligatoryTask;
     }
 
     public void setObligatoryTask(ObligatoryTask newObligatoryTask) {
         this.newObligatoryTask = newObligatoryTask;
     }
 
     public Task getTask() {
         if(newTask == null)
             newTask = new Task();
         return newTask;
     }
 
     public void setTask(Task newTask) {
         this.newTask = newTask;
     }
 
     public Theme getTheme() {
         if(newTheme == null)
             newTheme = new Theme();
         return newTheme;
     }
 
     public void setTheme(Theme newTheme) {
         this.newTheme = newTheme;
     }
     
     public void makeTestData() {
         service.makeTestData();
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
 }
