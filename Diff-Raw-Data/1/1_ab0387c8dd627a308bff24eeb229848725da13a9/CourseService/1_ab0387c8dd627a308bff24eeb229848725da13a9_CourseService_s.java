 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.service;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import no.hials.muldvarpweb.domain.*;
 
 /**
  *
  * @author kristoffer
  */
 @Stateless
 @Path("course")
 public class CourseService {
     @PersistenceContext
     EntityManager em;
     
     @GET
     @Produces({MediaType.APPLICATION_JSON})
     public List<Course> findCourses() {       
         return em.createQuery("SELECT c from Course c", Course.class).getResultList();
     }
     
     @GET
     @Path("{id}")
     @Produces({MediaType.APPLICATION_JSON})
     public Course getCourse(@PathParam("id") Integer id) {
         TypedQuery<Course> q = em.createQuery("Select c from Course c where c.id = :id", Course.class);
         q.setParameter("id", id);
         return q.getSingleResult();
     }
     
     public List<Course> getCourse(String name) {       
         TypedQuery<Course> q =  em.createQuery("Select c from Course c where c.name LIKE :name", Course.class);
         q.setParameter("name", "%" + name + "%");
         return q.getResultList();
     }
     
     @GET
     @Path("edit/{cid}/{themeid}/{taskid}/{val}")
     public String setTask(@PathParam("cid") Integer cid,
     @PathParam("themeid") Integer themeid,
     @PathParam("taskid") Integer taskid,
     @PathParam("val") Integer val) {
         // Sette task som done
         // ide: bruke lagre knapp og lagre alt i ett
         String retval = "ERROR\n";
         Course c = getCourse(cid);
         List<Theme> themes = c.getThemes();
         Theme theme = null;
         for(int i = 0; i < themes.size(); i++) {
             if(themes.get(i).getId() == themeid) {
                 theme = themes.get(i);
                 break;
             }
         }
         
         if(theme != null) {
             List<Task> tasks = theme.getTasks();
             for(int i = 0; i < tasks.size(); i++) {
                 if(tasks.get(i).getId() == taskid) {
                     Task task = tasks.get(i);
                     if(val == 1) {
                         task.setDone(true);
                     } else if (val == 0) {
                         task.setDone(false);
                     }
                     editTask(c, theme, task);
                     retval = "Task completed";
                 }
             }
         } else {
             retval += "Theme == null";
         }
         
         return retval;
     }
     
     public void addCourse(Course course) {
         persist(course);
     }
     
     public void persist(Course c) {
         c = em.merge(c);
         em.persist(c);
     }
     
     public void addNewRevCourse(Course course) {
         course = new Course(course.getName(), course.getDetail(), course.getImageurl(), course.getRevision(), course.getThemes(), course.getObligatoryTasks(), course.getExams(), course.getTeachers(), course.getProgrammes());
         course.setRevision(course.getRevision()+1);
         persist(course);
     }
     
     public void editCourse(Course course) {        
         persist(course);
     }
     
     public void removeCourse(Course course) {
         course = em.merge(course);
         em.remove(course);
     }
     
     public void addTheme(Course course, Theme theme) {
         course.addTheme(theme);
         persist(course);
     }
     
     public void editTheme(Course course, Theme theme) {
         course.editTheme(theme);
         persist(course);
     }
     
     public void removeTheme(Course course, Theme theme) {
         course.removeTheme(theme);
         persist(course);
     }
     
     public void addTask(Course course, Theme theme, Task task) {
         course.addTask(theme, task);
         persist(course);
     }
     
     public void editTask(Course course, Theme theme, Task task) {
         course.editTask(theme, task);
         persist(course);
     }
     
     public void removeTask(Course course, Theme theme, Task task) {
         course.removeTask(theme, task);
         persist(course);
     }
     
     public void addObligatoryTask(Course course, ObligatoryTask obligtask) {
         course.addObligatoryTask(obligtask);
         persist(course);
     }
     
     public void editObligatoryTask(Course course, ObligatoryTask obligtask) {
         course.editObligatoryTask(obligtask);
         persist(course);
     }
     
     public void removeObligatoryTask(Course course, ObligatoryTask obligtask) {
         course.removeObligatoryTask(obligtask);
         persist(course);
     }
     
     public void acceptObligatoryTask(Course course, ObligatoryTask obligtask) {
         obligtask.acceptTask();
         editObligatoryTask(course, obligtask);
     }
     
     public void addExam(Course course, Exam exam) {
         course.addExam(exam);
         persist(course);
     }
     
     public void editExam(Course course, Exam exam) {
         course.editExam(exam);
         persist(course);
     }
     
     public void removeExam(Course course, Exam exam) {
         course.removeExam(exam);
         persist(course);
     }
     
     public void addProgramme(Course c, Programme p) {
         c.addProgramme(p);
         editCourse(c);
     }
     
     public void addQuestion(Course selected, Theme selectedTheme, Task selectedTask, Question newQuestion) {
         selectedTask.addQuestion(newQuestion);
         editTask(selected, selectedTheme, selectedTask);
     }
     
     public void editQuestion(Course selected, Theme selectedTheme, Task selectedTask, Question q) {
         selectedTask.editQuestion(q);
         editTask(selected, selectedTheme, selectedTask);
     }
     
     public void addAlternative(Course selected, Theme selectedTheme, Task selectedTask, Question selectedQuestion, Alternative newAlternative) {
         selectedQuestion.addAlternative(newAlternative);
         editQuestion(selected, selectedTheme, selectedTask, selectedQuestion);
     }
     
     public void setAnswer(Course selected, Theme selectedTheme, Task selectedTask, Question q, Alternative a) {
         q.setAnswer(a);
         editQuestion(selected, selectedTheme, selectedTask, q);
     }
     
     public void removeQuestion(Course selected, Theme selectedTheme, Task selectedTask, Question q) {
         selectedTask.removeQuestion(q);
         editTask(selected, selectedTheme, selectedTask);
     }
     
     public void removeAlternative(Course selected, Theme selectedTheme, Task selectedTask, Question selectedQuestion, Alternative a) {
         selectedQuestion.removeAlternative(a);
         editQuestion(selected, selectedTheme, selectedTask, selectedQuestion);
     }
     
     public void makeTestData() {     
         Course retVal = new Course("Fagnavn");
         retVal.setDetail("Details");
         DateFormat df = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss");
         Date date = new Date();
         
         ArrayList<ObligatoryTask> obligTasks = new ArrayList<ObligatoryTask>();
         try {
             date = df.parse("2013-11-28T12:34:56");
         } catch (ParseException ex) {
             Logger.getLogger(CourseService.class.getName()).log(Level.SEVERE, null, ex);
         }
         ObligatoryTask oblig1 = new ObligatoryTask("Obligatorisk 1");
         oblig1.setDueDate(date);
         obligTasks.add(oblig1);
         oblig1 = new ObligatoryTask("Obligatorisk 2");
         Calendar c = Calendar.getInstance();
         int year = 2012;
         int month = 11;
         int day = 28;
         int hour = 12;
         int minute = 34;
         c.clear();
         c.set(year, month, day, hour, minute);
         oblig1.setDueDate(c.getTime());
         oblig1.setDone(true);
         obligTasks.add(oblig1);
         retVal.setObligatoryTasks(obligTasks); 
         
         try {
             date = df.parse("2011-12-31T12:34:56");
         } catch (ParseException ex) {
             Logger.getLogger(CourseService.class.getName()).log(Level.SEVERE, null, ex);
         }
         ArrayList<Exam> exams = new ArrayList<Exam>();
         Exam exam = new Exam("Eksamen 1");
         exam.setExamDate(date);
         exams.add(exam);
         exam = new Exam("Eksamen 2");
         exam.setExamDate(date);
         exams.add(exam);
         retVal.setExams(exams);
         
         ArrayList<Theme> themes = new ArrayList<Theme>();
         
         Theme theme1 = new Theme("Kult tema");
         ArrayList<Task> tasks = new ArrayList<Task>();
         Task task = new Task("Oppgave 1.1");
         tasks.add(task);
         task = new Task("Oppgave 1.2");
         tasks.add(task);
         theme1.setTasks(tasks);
         themes.add(theme1);
         
         Theme theme2 = new Theme("Dummy tema");
         ArrayList<Task> tasks2 = new ArrayList<Task>();
         task = new Task("Les dette");
         task.setDone(true);
         task.setContentType("PDF");
         tasks2.add(task);
         
         task = new Task("Quiz");
         task.setContentType("Quiz");
         ArrayList<Question> questions = new ArrayList<Question>();
         Question q = new Question();
         q.setName("Spørsmål");
         ArrayList<Alternative> alts = new ArrayList<Alternative>();
         Alternative a = new Alternative();
         a.setName("Alternativ 1");
         alts.add(a);
         a = new Alternative();
         a.setName("Alternativ 2");
         alts.add(a);
         q.setAlternatives(alts);
         questions.add(q);
         task.setQuestions(questions);
         tasks2.add(task);
         
         task = new Task("Læringsvideo 1");
         task.setContent_url("XsFR8DbSRQE");
         task.setContentType("Video");
         tasks2.add(task);
         theme2.setTasks(tasks2);
         themes.add(theme2);
         
       
         retVal.setThemes(themes);
         em.persist(retVal);
         
        ArrayList<Programme> programmes = new ArrayList<Programme>();
         Programme prog = new Programme("Test program", "blablabla");
         prog.addCourse(retVal);
         retVal.addProgramme(prog);
         
         retVal = em.merge(retVal);       
     }
 }
