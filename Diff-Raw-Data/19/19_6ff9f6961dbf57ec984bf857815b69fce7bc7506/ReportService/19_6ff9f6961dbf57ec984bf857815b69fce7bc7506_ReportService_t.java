 package it.sevenbits.conferences.service;
 
 import it.sevenbits.conferences.domain.Conference;
 import it.sevenbits.conferences.domain.Report;
 import it.sevenbits.conferences.domain.User;
 
 import java.util.List;
 
 /**
  * Interface for service layer, which works with EntityDao.
  * Report is the primary Entity for that Service.
  */
 public interface ReportService {
 
     /**
      * @see it.sevenbits.conferences.dao.EntityDao#add(Object)
      */
     Report addReport(final Report report);
 
     /**
      * @see it.sevenbits.conferences.dao.EntityDao#remove(Long)
      */
     boolean removeReport(final Long id);
 
     /**
      * @see it.sevenbits.conferences.dao.EntityDao#update(Object)
      */
     Report updateReport(final Report report);
 
     /**
      * @see it.sevenbits.conferences.dao.EntityDao#findAll()
      */
     List<Report> findAllReports();
 
     /**
      * @see it.sevenbits.conferences.dao.EntityDao#findById(Long)
      */
     Report findReportById(final Long id);
 
     /**
      * Finds all Reports, which belong to the Conference.
      * @param conference a Conference whose reports should be find.
      * @return the found Reports list, or null in case of an error.
      * @see it.sevenbits.conferences.dao.EntityDao#findByQuery(String, java.util.Map)
      */
     List<Report> findAllReportsByConference(final Conference conference);
 
     /**
      * Finds all Reports, which belong to the Reporter.
      * @param user a User whose reports should be find.
      * @return the found Reports list, or null in case of an error.
      * @see it.sevenbits.conferences.dao.EntityDao#findByQuery(String, java.util.Map)
      */
    List<Report> findAllPresentedReportsByUser(final User user);
 }
