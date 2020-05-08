 package publicholidays;
 
 import entity.DateEntry;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspWriter;
 import javax.swing.JOptionPane;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 public class Database {
 
     public static final int LONG_WEEKEND_BEFORE = 0;
     public static final int LONG_WEEKEND_AFTER = 1;
     
     private EntityManager entityManger;
     private List<DateEntry> holidays;
 
     public Database() {
         EntityManagerFactory factory = Persistence.createEntityManagerFactory("holidays");
         entityManger = factory.createEntityManager();
         Query query = entityManger.createNamedQuery("DateEntry.findAll", DateEntry.class);
         holidays = query.getResultList();
         
         //Modifing the list to add the hoidays that are on the same day each year
         List<DateEntry> nextYearHolidays = new ArrayList<DateEntry>(), 
                 yearAfterNextHolidays = new ArrayList<DateEntry>();
         for(DateEntry d: holidays){
             if(d.getAlwaysOnSameDay() == DateEntry.ALWAYS_ON_SAME_DAY){
                 DateEntry nextYearVersion = d.nextYear();
                 nextYearHolidays.add(nextYearVersion);
                 yearAfterNextHolidays.add(nextYearVersion.nextYear());
             }
         }
         holidays.addAll(nextYearHolidays);
         holidays.addAll(yearAfterNextHolidays);
         Collections.sort(holidays);
     }
 
     public List<DateEntry> getHolidays() {
         return holidays;
     }
 
     public void add(HttpServletRequest request) {
         //This needs to modified to account for the fact that more holidays are being added for the next years
         //Need a way to get the next non -1 id holiday to add properly
         //The next id will be the size as the List is zero indexed
         DateEntry toAdd = new DateEntry(holidays.size(), request.getParameter("name"),
                 request.getParameter("desc"), request.getParameter("date"), Integer.parseInt(request.getParameter("same_day")));
         persist(toAdd);
     }
 
     public void update(HttpServletRequest request) {
         DateEntry update = new DateEntry(Integer.parseInt(request.getParameter("id")),
                  request.getParameter("name"),request.getParameter("desc"), request.getParameter("date"),
                  Integer.parseInt(request.getParameter("same_day")));
         persist(update);
     }
 
     public void print(JspWriter out) {
         try {
             for (DateEntry d : holidays) {
                 if(d.getId() != -1)
                     out.print(d + "<br/>");
             }
         } catch (IOException e) {
             JOptionPane.showMessageDialog(null, e.getMessage());
         }
 
     }
 
     public List findLongWeekend(HttpServletRequest request) {
         List<List<DateEntry>> allLongWeekends = new ArrayList<List<DateEntry>>();
         String startDate = request.getParameter("startDate");
         String endDate = request.getParameter("endDate");
         getUserDates(request);
         
         //Set up a list of possible long weekend candidates
         List<DateEntry> toTraverse = new ArrayList<DateEntry>();
 	for(DateEntry d: holidays){
             if(startDate.compareTo(d.getHolidayDate()) <= 0 
                             && endDate.compareTo(d.getHolidayDate()) >= 0 ){
                 //Get the first possible day in a long weekend
                 toTraverse.add(getStartDate(d));
             }
 	}        
         
         for(DateEntry current: toTraverse){
             List<DateEntry> longweekend = new ArrayList<DateEntry>();
             while (isHoliday(current) || isWeekend(current) || isMondayAfterHoliday(current)) {
                 ArrayList<Object> collision = isHolidayColliding(current);
                 if ((Boolean) collision.get(0)) {
                     longweekend.add((DateEntry) collision.get(1));//Add the first colliding date
                     longweekend.add((DateEntry) collision.get(2));//Add the second colliding date
                     DateEntry next = (DateEntry) collision.get(2);
                     current = next.nextDate();//Get the next date to modify
                     current.setHolidayName("Honorary Date");
                     current.setHolidayDesc("Honorary Date");
                     longweekend.add(current);
                     current = current.nextDate();
                     continue;
                 }
                 for (DateEntry d : holidays) {
                     if (d.getHolidayDate().equals(current.getHolidayDate())) {
                         current = d;
                         break;
                     }
                 }
                 longweekend.add(current);
                 current = current.nextDate();
             }
             allLongWeekends.add(longweekend);
         }
         removeIntersecting(allLongWeekends);
         
        String s = request.getParameter("selector");
        int selector =  s != null ? Integer.parseInt(s) : LONG_WEEKEND_AFTER;
         switch(selector){
             case LONG_WEEKEND_BEFORE:
                 return allLongWeekends.get(allLongWeekends.size()-1);
             case LONG_WEEKEND_AFTER:
                 return allLongWeekends.get(0);
             default:
                 return allLongWeekends.get(0);
         }
     }
 
     //Priavte Methods
     private void persist(DateEntry d) {
         entityManger.getTransaction().begin();
         entityManger.persist(d);
         entityManger.getTransaction().commit();
         entityManger.close();
     }
 
     private boolean isHoliday(DateEntry d) {
         return holidays.contains(d);
     }
 
     private boolean isWeekend(DateEntry d) {
         GregorianCalendar date = d.toGregorianCalendar();
         if (date.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY) {
             d.setHolidayName("Saturday");
             d.setHolidayDesc("Saturday");
             return true;
         } else if (date.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY) {
             d.setHolidayName("Sunday");
             d.setHolidayDesc("Sunday");
             return true;
         }
         return false;
     }
 
     private boolean isMondayAfterHoliday(DateEntry d) {
         DateEntry previous = d.previousDate();
         if (!isHoliday(previous)) {
             return false;
         }
 
         GregorianCalendar previousDay = previous.toGregorianCalendar();
         GregorianCalendar day = d.toGregorianCalendar();
         if (previousDay.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY
                 && day.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.MONDAY) {
             d.setHolidayName("Honorary Date");
             d.setHolidayDesc("Honorary Date");
             return true;
         }
         return false;
     }
 
     private ArrayList<Object> isHolidayColliding(DateEntry d) {
         /*Info on data returned by this method
          * The first return value is a Boolean to know if the holidays collided
          * The second return value is the first date the same day
          * The third is the second date that had the same day
          * It returns true if the date passed in is colliding
          */
         
         //Reducing the amount of holidays to check for collision
         ArrayList<DateEntry> holidaySet = new ArrayList<DateEntry>();
         for(DateEntry c: holidays){
             if(d.compareTo(d) >= 0){
                 holidaySet.add(c);
             }
         }
         ArrayList<Object> collision = new ArrayList<Object>();
         DateEntry current, next;
         for (int outerCounter = 0; outerCounter < holidaySet.size(); outerCounter++) {
             current = holidaySet.get(outerCounter);
             for (int innerCounter = 1; innerCounter < holidaySet.size(); innerCounter++) {
                 next = holidaySet.get(innerCounter);
 
                 //If the date is the samebut different names you get the next day
                 if (current.getHolidayDate().equals(next.getHolidayDate())
                         && !current.getHolidayName().equals(next.getHolidayName())) {
                     if (d.getHolidayDate().equals(current.getHolidayDate())) {
                         collision.add(true);
                         collision.add(current);
                         collision.add(next);
                         return collision;//The date passed is a collision
                     }
                 }
             }
         }
         collision.add(false);
         return collision;
     }
     
     private DateEntry getStartDate(DateEntry d){
         GregorianCalendar date = d.toGregorianCalendar();
         if(date.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)
             return d.previousDate();//Set the date to the saturday
         else if(date.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.MONDAY)
             return d.previousDate().previousDate();//Set the date to the saturday
         return d;//Tuesday-Saturday
     }
     
     public void removeIntersecting(List<List<DateEntry>> list) {
         //Assumes that the first contains all the elements 
         for(int i = 1; i < list.size(); i++){
             List<DateEntry> list1 = list.get(i-1), list2 = list.get(i);
             if(listIntersect(list1, list2)){
                 list.remove(i);
                 i = 1;
             }
         }
     }
     
     public boolean listIntersect(List<DateEntry> list1, List<DateEntry> list2) {
         for(DateEntry d: list2){
             if(list1.contains(d)){
                 return true;
             }
         }
         return false;
     }
 
     private void getUserDates(HttpServletRequest request){
         String userDatesJSON = request.getParameter("userDates");
         if(userDatesJSON == null || userDatesJSON.isEmpty())
             return;
         
         JSONArray userDates = new JSONArray(userDatesJSON);
         for(int i = 0;i < userDates.length(); i++){
             JSONObject jsonObject = userDates.getJSONObject(i);
             DateEntry add = new DateEntry(jsonObject.getString("date"));
             add.setHolidayName(jsonObject.getString("name"));
             add.setHolidayDesc(jsonObject.getString("desc"));
             holidays.add(add);
         }
         
         Collections.sort(holidays);
     }
 }
