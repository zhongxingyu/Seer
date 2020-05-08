 package publicholidays;
 
 import entity.DateEntry;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.ListIterator;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspWriter;
 import javax.swing.JOptionPane;
 
 public class Database {
 
     private EntityManager entityManger;
     private List<DateEntry> holidays;
 
     public Database() {
         EntityManagerFactory factory = Persistence.createEntityManagerFactory("holidays");
         entityManger = factory.createEntityManager();
         Query query = entityManger.createNamedQuery("DateEntry.findAll", DateEntry.class);
         holidays = query.getResultList();
     }
 
     public List<DateEntry> getHolidays() {
         return holidays;
     }//end getHolidays
 
     public void add(HttpServletRequest request) {
         //The next id will be the size as the List is zero indexed
         DateEntry toAdd = new DateEntry(holidays.size(), request.getParameter("name"),
                 request.getParameter("desc"), request.getParameter("date"), Integer.parseInt(request.getParameter("same_day")));
         persist(toAdd);
     }
 
     public void update(HttpServletRequest request) {
         DateEntry update = holidays.get(Integer.parseInt(request.getParameter("id")) - 1);
         update.setHolidayName(request.getParameter("name"));
         update.setHolidayDesc(request.getParameter("desc"));
         update.setHolidayDate(request.getParameter("date"));
         //update.setAlwaysOnSameDay(Integer.parseInt(request.getParameter("same_day")));
         persist(update);
     }//end update
 
     public void print(JspWriter out) {
         try {
             for (DateEntry d : holidays) {
                 out.print(d + "<br/>");
             }
         } catch (IOException e) {
             JOptionPane.showMessageDialog(null, e.getMessage());
         }
 
     }//end print
 
     public void findLongWeekend(HttpServletRequest request, JspWriter out) {
         try {
             List<DateEntry> longweekend = findLongWeekend(request);
             for (DateEntry date : longweekend) {
                 out.print(date + "<br/>");
             }
         } catch (IOException e) {
             JOptionPane.showMessageDialog(null, e.getMessage());
         }
 
     }//end findLongWeekend
 
     public List findLongWeekend(HttpServletRequest request) {
         /*Pseudocode for findLongWeekend
          * while there still more holidays in comingHolidays
          * if the next day is a public holiday or saturday or sunday or monday after sunday holiday
          * print it
          * else if the holidays collides skip the next day as it will be a holiday
          * end if
          * end while
          */
         ArrayList<DateEntry> longweekend = new ArrayList<DateEntry>();
         DateEntry current = new DateEntry(request.getParameter("startDate"));
         do {
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
         } while (isHoliday(current) || isWeekend(current) || isMondayAfterHoliday(current));
 
         return longweekend;
     }//end findLongWeekend
 
     public List findlongWeekendBefore(HttpServletRequest request) {
         List<List<DateEntry>> longweekends = longweekend(request);
         return longweekends.get(longweekends.size() - 1);
     }//end findLongWeekendBefore
 
     public List findLongWeekendAfter(HttpServletRequest request) {
         List<List<DateEntry>> longweekends = longweekend(request);
         return longweekends.get(0);
     }
 
     //Priavte Methods
     private void persist(DateEntry d) {
         entityManger.getTransaction().begin();
         entityManger.persist(d);
         entityManger.getTransaction().commit();
         entityManger.close();
     }//end persist
 
     private List<List<DateEntry>> longweekend(HttpServletRequest request) {
         List<List<DateEntry>> allLongWeekends = new ArrayList<List<DateEntry>>();
         DateEntry current = new DateEntry(request.getParameter("startDate"));
         String endDate = request.getParameter("endDate");
         while (!current.getHolidayDate().equals(endDate)) {
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
             if (!longweekend.isEmpty()) {
                 allLongWeekends.add(longweekend);
             }
             current = current.nextDate();
         }
 
         ListIterator<List<DateEntry>> iterator = allLongWeekends.listIterator();
         while (iterator.hasNext()) {
             if (isOnlyWeekend(iterator.next())) {
                 iterator.remove();
             }
         }
         return allLongWeekends;
     }//end longweekend
 
     private boolean isHoliday(DateEntry d) {
         return holidays.contains(d);
     }//end isHoliday
 
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
     }//end isWeekend
 
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
     }//end isMondayAfterHoliday
 
     private ArrayList<Object> isHolidayColliding(DateEntry d) {
         /*Info on data return by this method
          * The first return value is a Boolean to know if the holidays collided
          * The second return value is the first date the same day
          * The third is the second date that had the same day
          * It returns true if the date passed in is colliding
          */
 
         //Need a way to start make this start searching for holidays after the one passed in
         ArrayList<Object> collision = new ArrayList<Object>();
         DateEntry current, next;
         for (int outerCounter = 0; outerCounter < holidays.size(); outerCounter++) {
             current = holidays.get(outerCounter);
             for (int innerCounter = 1; innerCounter < holidays.size(); innerCounter++) {
                 next = holidays.get(innerCounter);
 
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
     }//end isHolidayColliding
 
     private boolean isOnlyWeekend(List<DateEntry> list) {
         //Checking if the list only has a weekend to know if to remove it
         try {
             if (isWeekend(list.get(0)) && isWeekend(list.get(1))) {
                 return true;
             }
         } catch (IndexOutOfBoundsException e) {
             return false;
         }
         return false;
     }
 }
