 package ch.bli.mez.model.dao;
 
 import java.util.Calendar;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import ch.bli.mez.model.Employee;
 import ch.bli.mez.model.SessionManager;
 import ch.bli.mez.model.TimeEntry;
 
 /**
  * 
  * @author dave
  * @version 1.0
  */
 // TODO Naming der Report-DAO's einheitlich gestalten
 @SuppressWarnings("unchecked")
 public class EmployeeReportDAO {
 
   public EmployeeReportDAO() {
   }
 
   /**
    * 
    * @param employee
    *          concerned employee
    * @return newest Week in TimeEntries
    */
   public Calendar getNewestDateOfWork(Employee employee) {
     if (employee == null) {
       return null;
     }
     Session session = SessionManager.getSessionManager().getSession();
     List<TimeEntry> timeEntries = session.createCriteria(TimeEntry.class).add(Restrictions.eq("employee", employee))
         .addOrder(Order.desc("date")).list();
     if (timeEntries.size() > 0) {
       return timeEntries.get(0).getDate();
     }
     return null;
   }
 
   public Integer getWeekSummaryAmount(Employee employee, Calendar weekBegin) {
     if (weekBegin == null || employee == null) {
       return 0;
     }
     Calendar weekEnd = (Calendar) weekBegin.clone();
     weekEnd.set(Calendar.WEEK_OF_YEAR, weekBegin.get(Calendar.WEEK_OF_YEAR) + 1);
     Session session = SessionManager.getSessionManager().getSession();
     Long weekSum = (Long) session.createCriteria(TimeEntry.class).setProjection(Projections.sum("worktime"))
         .add(Restrictions.eq("employee", employee)).add(Restrictions.ge("date", weekBegin))
         .add(Restrictions.lt("date", weekEnd)).uniqueResult();
     if (weekSum != null) {
       return weekSum.intValue();
     }
     return 0;
   }
 }
