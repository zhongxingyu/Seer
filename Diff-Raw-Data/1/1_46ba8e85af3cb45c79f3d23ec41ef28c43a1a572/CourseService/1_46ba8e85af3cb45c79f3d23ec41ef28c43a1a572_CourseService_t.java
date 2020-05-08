 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.service;
 
 import java.util.ArrayList;
 import java.util.List;
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
         return retVal;
     }
     
     public List<Course> getCourse(String name) {       
         TypedQuery<Course> q =  em.createQuery("Select c from Course c where c.name LIKE :name", Course.class);
         q.setParameter("name", "%" + name + "%");
         return q.getResultList();
     }
 }
