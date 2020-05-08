 package models;
 
 import play.db.jpa.JPA;
 import play.db.jpa.Transactional;
 
 /**
  * Author: Nicolas Forney (nicolas@eforney.com)
  */
 public class Model {
     @Transactional
     public void save(){
         JPA.em().persist(this);
     }
 }
