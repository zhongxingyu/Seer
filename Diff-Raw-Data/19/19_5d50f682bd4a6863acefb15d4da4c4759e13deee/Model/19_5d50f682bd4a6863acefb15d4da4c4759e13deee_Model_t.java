 package models;
 
 import play.db.jpa.JPA;
 import play.db.jpa.Transactional;
 
import java.util.List;

 /**
  * Author: Nicolas Forney (nicolas@eforney.com)
  */
 public class Model {
     @Transactional
     public void save(){
         JPA.em().persist(this);
     }

    @Transactional
    public List all(){
       return JPA.em().createQuery("from " + this.getClass().getCanonicalName()).getResultList();
    }
 }
