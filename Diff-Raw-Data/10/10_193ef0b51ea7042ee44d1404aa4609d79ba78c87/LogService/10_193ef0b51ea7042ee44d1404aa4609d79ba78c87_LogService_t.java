 package cs.wintoosa.service;
 
 import cs.wintoosa.domain.*;
 import cs.wintoosa.repository.log.ILogRepository;
 import cs.wintoosa.repository.phone.IPhoneRepository;
 import cs.wintoosa.repository.session.ISessionRepository;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.eclipse.persistence.exceptions.QueryException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author jonimake
  */
 @Service
 public class LogService implements ILogService {
 
     @Autowired
     private ILogRepository logRepositoryImpl;
     
     @Autowired
     private ISessionRepository sessionRepositoryImpl;
     
     @Autowired
     private IPhoneRepository phoneRepositoryImpl;
     
     @PersistenceContext
     EntityManager em;
 
     @Override
     @Transactional
     public boolean saveLog(Log log) {
         if (log == null || log.getPhoneId() == null) {
             return false;
         }
         log = logRepositoryImpl.save(log);
         return true;
     }
     /**
      * Todo: Put this in a repository class
      * @param cls the class of the log entry
      * @return 
      */
     @Override
     @Transactional(readOnly = true)
     public List<Log> getAll(Class cls) {
         try {
             List<Log> resultList = em.createQuery("SELECT c FROM " + cls.getSimpleName() + " c", cls).getResultList();
             System.out.println("resultList.size() = " + resultList.size());
             return resultList;
         } catch (QueryException e) {
             System.out.println("Failed query!");
             System.out.println(e.getQuery().getSQLString());
             System.out.println(e.getMessage());
         }
         catch (Exception e) {
             System.out.println(e.toString());
         }
         return null;
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Log> getAll() {
         List<Log> logs = logRepositoryImpl.findAll();
         return logs;
     }
     
     @Override
     @Transactional(readOnly = true)
     public List<SessionLog> getAllSessions() {
         List<SessionLog> sessionLogs = sessionRepositoryImpl.findAll();
         return sessionLogs;
     }
     
     @Override
     @Transactional(readOnly = true)
     public SessionLog getSessionById(long sessionId) {
         SessionLog session = sessionRepositoryImpl.findSessionById(sessionId);
         session.setLogs(logRepositoryImpl.findByPhoneIdAndTimestampBetween(session.getPhoneId(), session.getSessionStart(), session.getSessionEnd()));
         return session;
     }
     
     @Override
     @Transactional(readOnly = true)
     public List<SessionLog> getSessionByPhoneId(String phoneId){
         System.out.println("getSessionByPhoneId");
         List<SessionLog> sessions = sessionRepositoryImpl.findSessionByPhoneId(phoneId);
         System.out.println("sessions.size() = " + sessions.size());
         return sessions;
     }
     
     /**
      * Saves the given session log into db
      * @param sessionLog
      * @return the saved SessionLog or null if save failed
      */
     @Override
     @Transactional
     public SessionLog saveSessionLog(SessionLog sessionLog){
         
         String phoneId = sessionLog.getPhoneId();
         Phone phone = phoneRepositoryImpl.findOne(phoneId);
         if(phone == null) {
             phone = new Phone();
             phone.setId(phoneId);
         }
         
         sessionLog.getLogs().addAll(
                 logRepositoryImpl.findByPhoneIdAndTimestampBetweenAndSessionLogIsNull
                 (phoneId, sessionLog.getSessionStart(), sessionLog.getSessionEnd()));
         
         
         phone.getSessions().add(sessionLog);
         sessionLog.setPhone(phone);
         
        //this saves the phone into DB also because of the cascade type declared in SessionLog for phone
         sessionLog = sessionRepositoryImpl.saveAndFlush(sessionLog);
         
         return sessionLog;
     }
 
     @Override
     @Transactional(readOnly= true)
     public List<Phone> getAllPhones() {
         
         return phoneRepositoryImpl.findAll();
     }
 
     
 }
