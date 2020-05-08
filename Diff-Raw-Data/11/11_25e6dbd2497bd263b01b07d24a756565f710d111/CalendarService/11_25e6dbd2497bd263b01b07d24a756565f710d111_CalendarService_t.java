 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.tripbrush.service;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import java.util.HashMap;
 import java.util.List;
 import net.fortuna.ical4j.model.component.VEvent;
 
 import net.fortuna.ical4j.model.property.CalScale;
 import net.fortuna.ical4j.model.property.ProdId;
 import net.fortuna.ical4j.model.property.Version;
 import org.hibernate.Criteria;
 import uk.tripbrush.model.travel.Attraction;
 import uk.tripbrush.model.travel.Category;
 import uk.tripbrush.util.PojoConstant;
 import org.hibernate.classic.Session;
 import org.hibernate.criterion.Restrictions;
 import uk.tripbrush.model.core.Plan;
 import uk.tripbrush.model.travel.AttractionEvent;
 import uk.tripbrush.model.travel.AttractionSeason;
 import uk.tripbrush.model.travel.AttractionTime;
 import uk.tripbrush.view.AttractionOpenView;
 
 /**
  *
  * @author Samir
  */
 public class CalendarService implements Serializable {
 
     private static SimpleDateFormat sdf;
     
     private static final String ID = "id";
 
     public static List<Category> getCategories() {
         Session session = Database.getSession();
         return session.createCriteria(PojoConstant.CATEGORY_MODEL).list();
     }
     
     public static Category getCategory(int cat) {
         Session session = Database.getSession();
         return (Category)session.createCriteria(PojoConstant.CATEGORY_MODEL).add(Restrictions.eq(ID, cat)).uniqueResult();
     }
 
     public static Category getCategory(String name) {
         Session session = Database.getSession();
         return (Category)session.createCriteria(PojoConstant.CATEGORY_MODEL).add(Restrictions.eq("name", name)).uniqueResult();
     }
 
     public static HashMap<Integer,AttractionTime> buildTimes(Attraction attraction,List<AttractionTime> times) {
         HashMap<Integer,AttractionTime> result = new HashMap<Integer,AttractionTime>();
         for (AttractionTime at: times) {
             result.put(at.getDow(),at);
         }
         Calendar today = Calendar.getInstance();
         Session session = Database.getSession();
         List list = session.createCriteria(PojoConstant.ATTRACTIONSEASON_MODEL).add(Restrictions.eq("attraction", attraction)).add(Restrictions.gt("today", today)).add(Restrictions.le("fromday", today)).list();
         for (Object obj: list) {
             AttractionSeason evt = (AttractionSeason)obj;
             AttractionTime at = result.get(evt.getDow());
             if (at==null) {
                 at = new AttractionTime();
                 at.setAttraction(attraction);
                 at.setDow(evt.getDow());
             }
             at.setStarthour(evt.getStarthour());
             at.setStartminute(evt.getStartminute());
             at.setEndhour(evt.getEndhour());
             at.setEndminute(evt.getEndminute());
             result.put(evt.getDow(),at);
         }
         return result;
     }
     
     public static List<Attraction> getAttractions(Plan plan) {
         if (sdf==null) {
             sdf = new SimpleDateFormat("dd/MM/yyyy");
         }
        
         
         List<Attraction> result = new ArrayList<Attraction>();
         Session session = Database.getSession();
         Criteria c = session.createCriteria(PojoConstant.ATTRACTION_MODEL).add(Restrictions.eq("location",plan.getLocation()));
         for (Object atr: c.list()) {
             Attraction attraction = (Attraction)atr;
             loadAttractionTimes(plan,attraction);
             result.add(attraction);
         }
         return result;
     }
     
     public static void loadAttractionTimes(Plan plan,Attraction attraction) {
         Session session = Database.getSession();
          int dow = plan.getStartdate().get(Calendar.DAY_OF_WEEK);
         List<AttractionOpenView> aovlist = new ArrayList<AttractionOpenView>();
             List<AttractionTime> times =  session.createCriteria(PojoConstant.ATTRACTIONTIME_MODEL).add(Restrictions.eq("attraction", attraction)).list();
             HashMap<Integer,AttractionTime> htimes = buildTimes(attraction,times);
             for (int counter=0;counter<plan.getLength();counter++) {
                 AttractionOpenView aov = new AttractionOpenView();
                 aov.getFrom().setTime(plan.getStartdate().getTime());
                aov.getTo().setTime(plan.getEnddate().getTime());
                 int d = (counter+dow)%8;
                 if (d==0) d = 1;
                 AttractionTime at = htimes.get(d);
                 if (at!=null) {
                     aov.getFrom().add(Calendar.DAY_OF_MONTH,(counter));
                     aov.getTo().add(Calendar.DAY_OF_MONTH,(counter));
                     aov.getFrom().set(Calendar.HOUR_OF_DAY,at.getStarthour());
                     aov.getFrom().set(Calendar.MINUTE,at.getStartminute());
                     aov.getTo().set(Calendar.HOUR_OF_DAY,at.getEndhour());
                     aov.getTo().set(Calendar.MINUTE,at.getEndminute());
                     aovlist.add(aov);
                 }
             }
             List<AttractionEvent> etimes =  session.createCriteria(PojoConstant.ATTRACTIONEVENT_MODEL).add(Restrictions.eq("attraction", attraction)).add(Restrictions.ge("hday", plan.getStartdate())).add(Restrictions.le("hday", plan.getEnddate())).list();
             for (AttractionEvent time: etimes) {
                 AttractionOpenView aov = new AttractionOpenView();
                 aov.setFrom(time.getHday());
                 aov.setTo(time.getHday());
                 aov.getFrom().set(Calendar.HOUR_OF_DAY,time.getStarthour());
                 aov.getFrom().set(Calendar.MINUTE,time.getStartminute());
                 aov.getTo().set(Calendar.HOUR_OF_DAY,time.getEndhour());
                 aov.getTo().set(Calendar.MINUTE,time.getEndminute());
                 aov.setDescription(time.getDescription());
                 aovlist.add(aov);
             }            
             attraction.setOpeningTimes(aovlist);        
     }
     
     public static void main(String[] args) {
         System.out.println("DDD");
     }
 }
