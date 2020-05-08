 package war.webapp.dao.hibernate;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.springframework.stereotype.Repository;
 
 import war.webapp.dao.DayDutyDao;
 import war.webapp.model.DayDuty;
 
 /**
  * This class interacts with Spring's HibernateTemplate to save/delete and retrieve DayDuty objects.
  *
  * @author <a href="mailto:tokefa@tut.by">kefa</a>
  */
 @Repository
 public class DayDutyDaoHibernate extends GenericDaoHibernate<DayDuty, Long> implements DayDutyDao {
 
     public DayDutyDaoHibernate() {
         super(DayDuty.class);
     }
 
     @SuppressWarnings("rawtypes")
     public DayDuty loadDayDutyByDateAndFloor(Calendar date, Integer floor) {
         clearDate(date);
         List dayDuties = getHibernateTemplate().find("from DayDuty where date=? and floor=?",
                new Object[]{date.getTime(), floor});
         if (dayDuties == null || dayDuties.isEmpty()) {
             return null;
         } else {
             return (DayDuty) dayDuties.get(0);
         }
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public List<DayDuty> loadAllDayDutyByDateAndFloor(Integer month, Integer floor) {
         List dayDuties = getHibernateTemplate().find("from DayDuty where floor=?", floor);
         if (dayDuties == null || dayDuties.isEmpty()) {
             return new ArrayList<DayDuty>();
         }
         List<DayDuty> allDuties = (List<DayDuty>) dayDuties;
         List<DayDuty> newDuties = new ArrayList<DayDuty>();
         for (DayDuty d : allDuties) {
             if (d.getDate().get(Calendar.MONTH) == month) {
                 newDuties.add(d);
             }
         }
         return newDuties;
     }
 
     public DayDuty saveDayDuty(DayDuty dayDuty) {
         if (log.isDebugEnabled())
             log.debug("user's id: " + dayDuty.getId());
         clearDate(dayDuty.getDate());
         getHibernateTemplate().saveOrUpdate(dayDuty);
         // necessary to throw a DataIntegrityViolation and catch it in UserManager
         getHibernateTemplate().flush();
         return dayDuty;
     }
 
     private void clearDate(Calendar date) {
         date.set(Calendar.HOUR, 0);
         date.set(Calendar.MINUTE, 0);
         date.set(Calendar.SECOND, 0);
     }
 
     public void deleteDayDuty(DayDuty dayDuty) {
         remove(dayDuty.getId());
     }
 
 }
