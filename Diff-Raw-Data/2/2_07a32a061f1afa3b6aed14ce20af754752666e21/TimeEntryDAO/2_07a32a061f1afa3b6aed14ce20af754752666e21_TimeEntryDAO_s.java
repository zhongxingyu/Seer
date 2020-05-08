 package ch.bli.mez.model.dao;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.CriteriaSpecification;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Restrictions;
 
 import ch.bli.mez.model.Employee;
 import ch.bli.mez.model.Mission;
 import ch.bli.mez.model.Position;
 import ch.bli.mez.model.SessionManager;
 import ch.bli.mez.model.TimeEntry;
 import ch.bli.mez.util.Keyword;
 import ch.bli.mez.util.Parser;
 
 @SuppressWarnings("unchecked")
 public class TimeEntryDAO implements Searchable {
 
   public TimeEntryDAO() {
   }
 
   public List<TimeEntry> findAll(Employee employee) {
     Session session = SessionManager.getSessionManager().getSession();
     Transaction tx = session.beginTransaction();
     List<TimeEntry> timeEntries = session.createQuery(
         "FROM TimeEntry WHERE employee_id = " + employee.getId() + " order by date").list();
     tx.commit();
     return timeEntries;
   }
 
   public void addTimeEntry(TimeEntry timeEntry) {
     Session session = SessionManager.getSessionManager().getSession();
     Transaction tx = session.beginTransaction();
     try {
       session.save(timeEntry);
       tx.commit();
     } catch (IllegalArgumentException ex) {
       tx.rollback();
       throw ex;
     }
   }
 
   public TimeEntry getTimeEntry(Integer id) {
     Session session = SessionManager.getSessionManager().getSession();
     Transaction tx = session.beginTransaction();
     TimeEntry timeEntry = (TimeEntry) session.load(TimeEntry.class, id);
     tx.commit();
     return timeEntry;
   }
 
   public void updateTimeEntry(TimeEntry timeEntry) {
     Session session = SessionManager.getSessionManager().getSession();
     Transaction tx = session.beginTransaction();
     try {
       session.update(timeEntry);
       tx.commit();
     } catch (Exception ex) {
       tx.rollback();
     }
   }
 
   public void deleteTimeEntry(Integer id) {
     Session session = SessionManager.getSessionManager().getSession();
     Transaction tx = session.beginTransaction();
     TimeEntry timeEntry = (TimeEntry) session.load(TimeEntry.class, id);
     if (null != timeEntry) {
       session.delete(timeEntry);
     }
     tx.commit();
   }
 
   public List<TimeEntry> findByKeywords(String url) {
     Criteria criteria = createCriteria(Keyword.getKeywords(url));
     criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
     return criteria.list();
   }
 
   private Criteria createCriteria(Map<String, String> keywords) {
     Session session = SessionManager.getSessionManager().getSession();
     Criteria criteria = session.createCriteria(TimeEntry.class);
     Calendar cal = null;
     try {
       cal = Parser.parseDateStringToCalendar(keywords.get("date"));
     } catch (Exception e) {
     }
     if (cal != null) {
       Criterion date = Restrictions.eq("date", cal);
       criteria.add(date);
     }
 
     EmployeeDAO employeeDAO = new EmployeeDAO();
     Employee employee = employeeDAO.getEmployee(Integer.parseInt(keywords.get("employeeID")));
     if (employee != null) {
       criteria.add(Restrictions.eq("employee", employee));
     }
 
     MissionDAO missionDAO = new MissionDAO();
     Mission mission = missionDAO.findByMissionName(keywords.get("missionName"));
     if (mission != null) {
       criteria.add(Restrictions.eq("mission", mission));
     }
 
     PositionDAO positionDAO = new PositionDAO();
     Position position = positionDAO.findByCode(keywords.get("positionCode"));
     if (position != null) {
       criteria.add(Restrictions.eq("position", position));
     }
 
     if (!keywords.get("worktime").equals("")) {
      criteria.add(Restrictions.eq("worktime", Integer.parseInt(keywords.get("worktime"))));
     }
     return criteria;
   }
 }
