 package war.webapp.dao.hibernate;
 
 import org.hibernate.HibernateException;
 import org.springframework.stereotype.Repository;
 import war.webapp.dao.DayDutyDao;
 import war.webapp.model.DayDuty;
 import war.webapp.model.User;
 import war.webapp.util.DateUtil;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * This class interacts with Spring's HibernateTemplate to save/delete and
  * retrieve DayDuty objects.
  */
 @Repository
 public class DayDutyDaoHibernate extends GenericDaoHibernate<DayDuty, Long> implements DayDutyDao {
 
     public DayDutyDaoHibernate() {
         super(DayDuty.class);
     }
 
     @SuppressWarnings("rawtypes")
     public DayDuty loadDayDutyByDateAndFloor(Calendar date, String floor) {
         clearTime(date);
         List dayDuties = getHibernateTemplate().find("from DayDuty where date=? and floor=?",
                 new Object[] { date, floor });
         if (dayDuties == null || dayDuties.isEmpty()) {
             return null;
         } else {
             return (DayDuty) dayDuties.get(0);
         }
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public List<DayDuty> loadAllDayDutyByDateAndFloor(Integer year, Integer month, String floor) {
        List dayDuties = getHibernateTemplate().find("from DayDuty where floor=? and month(date)=? and year(date)=?", new Object[]{floor, month+1, year});
         if (dayDuties == null || dayDuties.isEmpty()) {
             return new LinkedList<DayDuty>();
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
         if (log.isDebugEnabled()) {
             log.debug("user's id: " + dayDuty.getId());
         }
         clearTime(dayDuty.getDate());
         getHibernateTemplate().saveOrUpdate(dayDuty);
         // necessary to throw a DataIntegrityViolation and catch it in
         // UserManager
         getHibernateTemplate().flush();
         return dayDuty;
     }
 
     private void clearTime(Calendar date) {
         date.set(Calendar.HOUR_OF_DAY, 0);
         date.set(Calendar.MINUTE, 0);
         date.set(Calendar.SECOND, 0);
     }
 
     public void deleteDayDuty(DayDuty dayDuty) {
         remove(dayDuty.getId());
     }
 
     @SuppressWarnings("rawtypes")
     public DayDuty loadSingleDayDutyByExample(DayDuty exampleDayDuty) {
         List result = getHibernateTemplate().findByExample(exampleDayDuty);
         if (result.size() == 0 || result.size() > 1) {
             throw new HibernateException("Zero or more than one result from the query. Expected single result");
         }
         return (DayDuty)result.get(0);
     }
 
     public void deleteFirstDutyUser(DayDuty dayDuty) {
         DayDuty loadedDayDuty = loadSingleDayDutyByExample(dayDuty);
         loadedDayDuty.setFirstUser(null);
         loadedDayDuty.setOwnFirstDuty(null);
 
         getHibernateTemplate().update(loadedDayDuty);
 
     }
 
     public void deleteSecondDutyUser(DayDuty dayDuty) {
         DayDuty loadedDayDuty = loadSingleDayDutyByExample(dayDuty);
         loadedDayDuty.setSecondUser(null);
         loadedDayDuty.setOwnSecondDuty(null);
         getHibernateTemplate().update(loadedDayDuty);
     }
 
     public List<DayDuty> loadDutiesByUser(User user) {
         List duties = getHibernateTemplate().find("from DayDuty where (firstUser=? or secondUser=?) and date > ?", new Object[]{user, user, DateUtil.getFirstDayOfStudyYear()});
         if (duties == null || duties.isEmpty()) {
             return new ArrayList<DayDuty>(0);
         }
         return duties;
     }
 }
