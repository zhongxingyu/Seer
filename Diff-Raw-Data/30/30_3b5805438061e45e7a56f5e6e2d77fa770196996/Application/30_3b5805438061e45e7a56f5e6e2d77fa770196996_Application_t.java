 package controllers;
 
 import play.*;
 import play.data.validation.*;
 import play.mvc.*;
 
 import java.util.*;
 import models.*;
 import models.Woman.*;
 
 
 /**
  * The Class Application.
  */
 public class Application extends Controller {
     
     /**
      * Index page.
      */
     public static void index() {
     
        // Application.update((long) 1, Status.COMPLETE, Outcome.NONE);
         
         List<String> sectors = Woman.find("SELECT DISTINCT w.sectorId AS id FROM Woman w ORDER BY w.sectorId ASC").fetch();
         sectors.add(0, "All");
         
         List<FormEntity> events = FormEntity.find("scheduled >= ? AND done=0", new Date()).fetch();
         render(events, sectors);
     }
     
     /**
      * Filter Schedules by Sector Id.
      * 
      * @param id
      *            the sector id
      */
     public static void filter(Long id) {
     
         List<FormEntity> events;
         if (id == null) {
             events = FormEntity.find("scheduled >= ?", new Date()).fetch();
         }
         else {
             events = FormEntity
                     .find("SELECT f FROM FormEntity AS f, Woman AS w WHERE f.woman = w AND f.scheduled >= ?  AND w.sectorId = ? AND done=0",
                             new Date(), id).fetch();
         }
         
         render(events);
     }
     
     /**
      * Update.
      * 
      * @param form_id
      *            the form_id
      * @param status
      *            the status
      * @param outcome
      *            the outcome
      */
     public static void update(Long form_id, Status status, Woman.Outcome outcome) {
     
         FormEntity fe = FormEntity.findById(form_id);
         fe.update(status, outcome);
        // index();
     }
 }
