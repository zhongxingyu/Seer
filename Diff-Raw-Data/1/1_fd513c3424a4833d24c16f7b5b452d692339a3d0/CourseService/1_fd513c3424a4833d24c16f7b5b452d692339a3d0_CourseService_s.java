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
 import no.hials.muldvarpweb.domain.Course;
 import no.hials.muldvarpweb.domain.Exam;
 import no.hials.muldvarpweb.domain.ObligatoryTask;
 import no.hials.muldvarpweb.domain.Task;
 import no.hials.muldvarpweb.domain.Theme;
 
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
         //return em.createQuery("SELECT c from Course c", Course.class).getResultList();
         
         //testdata
         Course c = new Course("Test");
         List<Course> retVal = new ArrayList<Course>();
         retVal.add(c);
         c = new Course("Hei fra muldvarpweb");
         c.setImageurl("http://developer.android.com/assets/images/bg_logo.png");
         retVal.add(c);
         for(int i = 0; i <= 20; i++) {
            c = new Course("Fagnavn numero " + i);
            c.setDetail("Details");
            retVal.add(c); 
         }
         return retVal;
     }
     
     @GET
     @Path("{id}")
     @Produces({MediaType.APPLICATION_JSON})
     public Course getCourse(@PathParam("id") Short id) {
 //        TypedQuery<Course> q = em.createQuery("Select c from Course c where c.id = :id", Course.class);
 //        q.setParameter("id", id);
 //        return q.getSingleResult();
         
         
         // testdata
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
         task = new Task("Oppgave 2.1");
         task.setDone(true);
         tasks2.add(task);
         task = new Task("Oppgave 2.2");
         task.setDone(true);
         tasks2.add(task);
         theme2.setTasks(tasks2);
         themes.add(theme2);
         
         
         retVal.setThemes(themes);
         
         return retVal;
     }
     
     public List<Course> getCourse(String name) {       
         TypedQuery<Course> q =  em.createQuery("Select c from Course c where c.name LIKE :name", Course.class);
         q.setParameter("name", "%" + name + "%");
         return q.getResultList();
     }
 }
