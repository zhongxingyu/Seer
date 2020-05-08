 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lp.reminiscens.crawler;
 
 import java.util.Iterator;
 import java.util.List;
 import lp.reminiscens.crawler.entities.Event;
 import lp.reminiscens.crawler.entities.Location;
 import lp.reminiscens.crawler.entities.Media;
 import lp.reminiscens.crawler.entities.Media_Metadata;
 import lp.reminiscens.crawler.entities.Person;
 import lp.reminiscens.crawler.entities.RatingCouple;
 import lp.reminiscens.crawler.entities.RecommendationSet;
 import lp.reminiscens.crawler.entities.Time_Interval;
 
 /**
  *
  * @author Nicola
  */
 public class RecommendationEngine {
 
     Database db;
     Iterator iter;
 
     public RecommendationEngine() {
         db = new Database();
     }
 
     public RecommendationSet getRecommendations(Event myEvent) {
         RecommendationSet result = new RecommendationSet();
 
         List eventList = db.getEvents();
         iter = eventList.listIterator();
         Event event;
         while (iter.hasNext()) {
             event = (Event) iter.next();
             int res = isRelated(myEvent.getTimeInterval(), event.getTimeInterval(), myEvent.getLocation(), event.getLocation(), "EVENT");
             if (res > 0) {
                 result.events.add(new RatingCouple<Event, Integer>(event, res));
             }
             eventList.remove(event);
         }
 
         List MDList = db.getMDs();
         iter = MDList.listIterator();
         Media_Metadata mediaMD;
         while (iter.hasNext()) {
             mediaMD = (Media_Metadata) iter.next();
             int res = isRelated(myEvent.getTimeInterval(), mediaMD.getReleaseDate(), null, null, "MEDIA_METADATA");
             if (res > 0) {
                 result.mediaMDs.add(new RatingCouple<Media_Metadata, Integer>(mediaMD, res));
             }
             MDList.remove(mediaMD);
         }
 
         List peopleList = db.getPeople();
         iter = peopleList.listIterator();
         Person person;
 
        /*while (iter.hasNext()) {
             int res;
             person = (Person) iter.next();
             if (person.getPerson_Profile.getDeathDate() != null && person.getPerson_Profile.getDeathPlace() != null) {
                 res = isRelated(myEvent.getTimeInterval(), person.getPerson_Profile().getBirthDate(), myEvent.getLocation(), person.getBirthPlace, "PERSON")
                         + isRelated(myEvent.getTimeInterval(), person.getPerson_Profile().getDeathDate(), myEvent.getLocation(), person.getDeathPlace, "PERSON");
             } else {
                 res = isRelated(myEvent.getTimeInterval(), person.getPerson_Profile().getBirthDate(), myEvent.getLocation(), person.getBirthPlace, "PERSON");
             }
             if (res > 0) { // DEVO CAMBIARE QUI
                 result.people.add(new RatingCouple<Person, Integer>(person, res));
             }
             peopleList.remove(person);
        }*/
 
         List photosList = db.getMedia();
         iter = photosList.listIterator();
         Media photo;
         int res;
         while (iter.hasNext()) {
             photo = (Media) iter.next();
             res = isRelated(myEvent.getTimeInterval(), photo.getTimeInterval(), myEvent.getLocation(), photo.getLocation(), "PHOTO");
             if (res > 0) {
                 result.photos.add(new RatingCouple<Media, Integer>(photo, res));
             }
             photosList.remove(photo);
 
         }
 
         return result;
     }
 
     public int isRelated(Time_Interval time1, Time_Interval time2, Location space1, Location space2, String type) {
         if (type.equalsIgnoreCase("PERSON")) {
 
             if (space1 != null && space2 != null) {
                 if (space1.getCountry().equals(space2.getCountry())) {
                     if (time1.getStartdate() != null && time2.getStartdate() != null) {
                         int year1 = Integer.parseInt(time1.getStartdate().getYear());
                         int year2 = Integer.parseInt(time2.getStartdate().getYear());
                         if (Math.abs(year1 - year2) <= 5) {
                             return 3;
                         } else {
                             return 2;
                         }
                     } else {
                         return 2;
                     }
                 } else {
                     return 0;
                 }
             } else {
                 if (time1.getStartdate() != null && time2.getStartdate() != null) {
                     int year1 = Integer.parseInt(time1.getStartdate().getYear());
                     int year2 = Integer.parseInt(time2.getStartdate().getYear());
                     if (Math.abs(year1 - year2) <= 5) {
                         return 1;
                     } else {
                         return 0;
                     }
                 } else {
                     return 0;
                 }
             }
         } else {
 
             int year1 = Integer.parseInt(time1.getStartdate().getYear());
             int year2 = Integer.parseInt(time2.getStartdate().getYear());
             if (Math.abs(year1 - year2) <= 5) {
                 if (space1 != null && space2 != null) {
                     if (space1.getCountry().equals(space2.getCountry())) {
                         return 3;
                     } else {
                         return 2;
                     }
                 } else {
                     if (type.equalsIgnoreCase("MEDIA_METADATA")) {
                         return 2;
                     }
                     return 1;
                 }
             } else {
                 return 0;
             }
         }
     }
 }
