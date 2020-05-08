 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.tripbrush.service;
 
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import net.fortuna.ical4j.data.CalendarOutputter;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.property.CalScale;
 import net.fortuna.ical4j.model.property.Location;
 import net.fortuna.ical4j.model.property.ProdId;
 import net.fortuna.ical4j.model.property.Version;
 import net.fortuna.ical4j.util.UidGenerator;
 import uk.tripbrush.model.core.Plan;
 import uk.tripbrush.util.PojoConstant;
 import uk.tripbrush.view.MResult;
 import org.hibernate.classic.Session;
 import org.hibernate.criterion.Restrictions;
 import uk.tripbrush.model.core.User;
 import uk.tripbrush.model.travel.Event;
 import uk.tripbrush.util.DateUtil;
 
 /**
  *
  * @author Samir
  */
 public class PlanService {
 
     public static void loadPlans(User user,Plan plan) {
         Session session = Database.getSession();
         List<Plan> plans = session.createCriteria("uk.tripbrush.model.core.Plan").add(Restrictions.eq("user", user)).list();
         List<Plan> allplans = new ArrayList<Plan>();
         for (Plan dplan: plans) {
             if (plan!=null && dplan.getId()==plan.getId()) {
                 allplans.add(dplan);
             }
             else {
                 List<Event> events = session.createCriteria(PojoConstant.EVENT_MODEL).add(Restrictions.eq("plan", dplan)).list();
                 if (events.size()>0) {
                     allplans.add(dplan);
                 }
             }
         }
         if (plans != null) {
             user.setPlans(allplans);
         }
     }
 
     public static void createPlan(User user, Plan plan) {
         Session session = Database.getSession();
         plan.setReference(CommonService.genererateReferenceNumber(PojoConstant.PLAN_MODEL));
         plan.setUser(user);
         session.save(plan);
         user.getPlans().add(plan);
     }
 
     public static MResult deletePlan(User user, int id) {
         MResult result = new MResult();
         Session session = Database.getSession();
         Plan plan = (Plan) session.createCriteria(PojoConstant.PLAN_MODEL).add(Restrictions.eq("user", user)).add(Restrictions.eq("id", id)).uniqueResult();
         if (plan != null) {
             session.delete(plan);
             result.setCode(MResult.RESULT_OK);
         } else {
             result.setCode(MResult.RESULT_NOTOK);
             result.setMessage("delete.error");
         }
         return result;
     }
 
     public static MResult getPlan(User user, int id) {
         MResult result = new MResult();
         Session session = Database.getSession();
         Plan plan = (Plan) session.createCriteria(PojoConstant.PLAN_MODEL).add(Restrictions.eq("user", user)).add(Restrictions.eq("id", id)).uniqueResult();
         if (plan != null) {
             result.setObject(plan);
             plan.setEvents(session.createCriteria(PojoConstant.EVENT_MODEL).add(Restrictions.eq("plan", plan)).list());
             result.setCode(MResult.RESULT_OK);
         } else {
             result.setCode(MResult.RESULT_NOTOK);
             result.setMessage("plan.error");
         }
         plan.setEvents(EventService.getEvents(plan));
         return result;
     }
 
     public static MResult getPlan(String keypass) {
         MResult result = new MResult();
         Session session = Database.getSession();
         Plan plan = (Plan) session.createCriteria(PojoConstant.PLAN_MODEL).add(Restrictions.eq("reference", keypass)).uniqueResult();
         if (plan != null) {
             result.setObject(plan);
             plan.setEvents(session.createCriteria(PojoConstant.EVENT_MODEL).add(Restrictions.eq("plan", plan)).list());
             result.setCode(MResult.RESULT_OK);
         } else {
             result.setCode(MResult.RESULT_NOTOK);
             result.setMessage("plan.error");
         }
         plan.setEvents(EventService.getEvents(plan));
         return result;
     }
     
     
     public static MResult updatePlan(Plan plan) {
         MResult result = new MResult();
         result.setCode(MResult.RESULT_OK);
         Session session = Database.getSession();
         session.saveOrUpdate(plan);
         return result;
     }
 
     public static void savePlan(Plan plan) {
         Session session = Database.getSession();
         session.saveOrUpdate(plan);
     }
 
     public static Plan createNewPlan(String dest, String from, int length) {
         Plan plan = new Plan();
         plan.setLocation(CommonService.getLocation(dest));
         plan.setEditable(true);
         plan.setLength(length);
         Calendar cal = Calendar.getInstance();
         cal.setTime(CommonService.getDate(from));
         Calendar ecal = cal.getInstance();
         ecal.setTime(cal.getTime());
         ecal.add(Calendar.DAY_OF_MONTH, length);
         String dateto = CommonService.getSDate(ecal.getTime());
        plan.setTitle("Trip to " + dest + "(" + from + "-" + dateto + ")");
         plan.setStartdate(cal);
         plan.setEnddate(ecal);
         return plan;
     }
 
     public static HashMap<String, List<Event>> formatTime(Plan plan) {
         HashMap<String, List<Event>> result = new HashMap<String, List<Event>>();
         for (Event event : plan.getEvents()) {
             String day = DateUtil.getDay(event.getStartdate());
             if (!result.containsKey(day)) {
                 result.put(day, new ArrayList<Event>());
             }
             result.get(day).add(event);
         }
         return result;
     }
 
     public static void sendPlan(Plan plan) {
         try {
             net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
             calendar.getProperties().add(new ProdId("-//" + plan.getUser().getEmail() + "//TripBrush//EN"));
             calendar.getProperties().add(Version.VERSION_2_0);
             calendar.getProperties().add(CalScale.GREGORIAN);
             StringBuffer bodyb = new StringBuffer();
 
             String filename = ConfigService.getRoot() + "calendar" + plan.getId() + ".ics";
             UidGenerator ug = new UidGenerator("1");
             bodyb.append("<table>");
             bodyb.append("<tr><th>Attraction</th><th>Time</th><th>Location</th></tr>");
 
             HashMap<String, List<Event>> events = formatTime(plan);
 
             List<String> dateevents = new ArrayList<String>();
             for (String key : events.keySet()) {
                 dateevents.add(key);
             }
             Collections.sort(dateevents);
             for (String key : dateevents) {
                 List<Event> dateventslist = events.get(key);
                 bodyb.append("<tr><td colspan=3>" + key + "</td></tr>");
                 for (Event event : dateventslist) {
                     VEvent meeting = new VEvent(new net.fortuna.ical4j.model.DateTime(event.getStartdate().getTime()), new net.fortuna.ical4j.model.DateTime(event.getEnddate().getTime()), event.getAttraction().getName());
                     meeting.getProperties().add(new Location(event.getAttraction().getPostcode()));
 
                     meeting.getProperties().add(ug.generateUid());
 
                     calendar.getComponents().add(meeting);
 
                     bodyb.append("<tr>");
                     bodyb.append("<td>");
                     bodyb.append(event.getAttraction().getName() + "</td><td>" + event.getTime() + "</td><td>" + event.getAttraction().getPostcode());
                     bodyb.append("</td>");
                     bodyb.append("</tr>");
                 }
             }
             FileOutputStream fout = new FileOutputStream(filename);
             CalendarOutputter outputter = new CalendarOutputter();
             outputter.output(calendar, fout);
 
             List<String> files = new ArrayList<String>();
             files.add(filename);
             bodyb.append("</table>");
             
             System.out.println(bodyb.toString());
             
             EmailService.sendEmail(plan.getUser().getEmail(), "Your Trip Calendar to " + plan.getLocation().getName(), "calendar.htm", bodyb.toString(), files);
 
             
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
