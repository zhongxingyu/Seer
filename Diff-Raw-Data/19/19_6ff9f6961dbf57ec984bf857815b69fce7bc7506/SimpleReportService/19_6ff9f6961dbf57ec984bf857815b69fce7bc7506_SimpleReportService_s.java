 package it.sevenbits.conferences.service.common;
 
 import it.sevenbits.conferences.dao.ReportDao;
 import it.sevenbits.conferences.domain.Conference;
 import it.sevenbits.conferences.domain.Report;
 import it.sevenbits.conferences.domain.User;
 import it.sevenbits.conferences.service.ReportService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Simple implementation of the ReportService.
  */
 @Service
 public class SimpleReportService implements ReportService {
 
     @Autowired
     private ReportDao reportDao;
 
     @Transactional
     @Override
     public Report addReport(Report report) {
 
         return reportDao.add(report);
     }
 
     @Transactional
     @Override
     public boolean removeReport(Long id) {
 
         return reportDao.remove(id);
     }
 
     @Transactional
     @Override
     public Report updateReport(Report report) {
 
         return reportDao.update(report);
     }
 
     @Transactional
     @Override
     public List<Report> findAllReports() {
 
         return reportDao.findAll();
     }
 
     @Transactional
     @Override
     public Report findReportById(Long id) {
 
         return reportDao.findById(id);
     }
 
     @Override
     public List<Report> findAllReportsByConference(Conference conference) {
 
         Map<String, Object> params = new HashMap<>();
         params.put("conference", conference);
         return reportDao.findByQuery("select r from Report r where r.conference = :conference", params);
     }
 
     @Override
    public List<Report> findAllReportsByUser(User user) {
 
         Map<String, Object> params = new HashMap<>();
         params.put("user", user);
        return reportDao.findByQuery("select r from Report r where r.user = :user", params);
     }
 }
