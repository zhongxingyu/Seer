 package cz.muni.fi.pv243.et.service.impl;
 
 import cz.muni.fi.pv243.et.data.ExpenseReportListProducer;
 import cz.muni.fi.pv243.et.data.ExpenseReportRepository;
 import cz.muni.fi.pv243.et.model.ExpenseReport;
 import cz.muni.fi.pv243.et.model.Person;
 import cz.muni.fi.pv243.et.model.PersonRole;
 import cz.muni.fi.pv243.et.model.ReportStatus;
 import cz.muni.fi.pv243.et.security.annotation.Authenticated;
 import cz.muni.fi.pv243.et.security.annotation.Roles;
 import cz.muni.fi.pv243.et.service.ExpenseReportService;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import java.util.Collection;
 import java.util.Date;
 
 @Stateless
 @Authenticated
 public class ExpenseReportServiceImpl implements ExpenseReportService {
 
     @Inject
     private ExpenseReportRepository repository;
 
     @Inject
     private ExpenseReportListProducer listProducer;
 
     @Override
     public void save(ExpenseReport report) {
         if (report == null) {
             throw new IllegalArgumentException();
         }
 
         if (report.getId() == null) {
             repository.create(report);
         } else {
             repository.update(report);
         }
     }
 
     @Override
     public void remove(ExpenseReport report) {
         if (report == null) {
             throw new IllegalArgumentException();
         }
 
         repository.remove(report);
     }
 
     @Roles({PersonRole.VERIFIER})
     @Override
     public void claim(ExpenseReport report, Person verifier) {
         if (report == null || verifier == null) {
             throw new IllegalArgumentException();
         }
 
         if (report.getVerifier() != null) {
             throw new RuntimeException("report already has a verifier");
         }
 
         if (report.getStatus() == ReportStatus.APPROVED ||
                 report.getStatus() == ReportStatus.REJECTED ||
                 report.getStatus() == ReportStatus.SETTLED) {
             throw new RuntimeException("report status indicates that it should have already had a verifier assigned");
         }
 
         report.setVerifier(verifier);
 
         this.save(report);
     }
 
 
     @Override
     public void submit(ExpenseReport report) {
         if (report == null) {
             throw new IllegalArgumentException();
         }
 
         ReportStatus status = report.getStatus();
         if (status == ReportStatus.SUBMITTED || status == ReportStatus.APPROVED || status == ReportStatus.SETTLED ) {
             throw new RuntimeException("report has already been submitted");
         }
 
         if (report.getVerifier() != null && status != ReportStatus.REJECTED ) {
             throw new RuntimeException("report already has a verifier");
         }
 
         this.setStatus(report, ReportStatus.SUBMITTED);
     }
 
     @Override
     public void reject(ExpenseReport report) {
         if (report == null) {
             throw new IllegalArgumentException("null");
         }
         if (report.getVerifier() == null) {
             throw new RuntimeException("no verifier assigned");
         }
 
         this.setStatus(report, ReportStatus.REJECTED);
     }
 
     @Override
     public void sendMoney(ExpenseReport report) {
         if (report == null) {
             throw new IllegalArgumentException("null");
         }
         if (report.getStatus() != ReportStatus.APPROVED) {
             throw new RuntimeException("Report is not approved!");
         }
 
         this.setStatus(report, ReportStatus.SETTLED);
 
     }
 
     @Override
     public void approve(ExpenseReport report) {
         if (report == null) {
             throw new IllegalArgumentException("null");
         }
         if (report.getVerifier() == null) {
             throw new RuntimeException("no verifier assigned");
         }
 
         this.setStatus(report, ReportStatus.APPROVED);
     }
 
     @Override
     public void setStatus(ExpenseReport report, ReportStatus status) {
         if (report == null || status == null) {
             throw new IllegalArgumentException("null");
         }
         report.setStatus(status);
         report.setLastChangeDate(new Date(System.currentTimeMillis()));
         repository.update(report);
     }
 
     @Override
     public ExpenseReport get(Long id) {
         if (id == null) {
             throw new IllegalArgumentException();
         }
 
         return listProducer.get(id);
     }
 
     @Roles({PersonRole.VERIFIER})
     @Override
     public Collection<ExpenseReport> findAll() {
         return listProducer.getAll();
     }
 
     @Override
     public Collection<ExpenseReport> findForSubmitter(Person submitter) {
         if (submitter == null) {
             throw new IllegalArgumentException();
         }
 
         return listProducer.getAllForSubmitter(submitter);
     }
 
     @Roles({PersonRole.VERIFIER})
     @Override
     public Collection<ExpenseReport> findForVerifier(Person verifier) {
         if (verifier == null) {
             throw new IllegalArgumentException();
         }
 
         return listProducer.getAllForVerifier(verifier);
     }
 
     @Roles({PersonRole.VERIFIER})
     @Override
     public Collection<ExpenseReport> findWithNoVerifierAssigned() {
         return listProducer.getAllWithNoVerifierAssigned();
     }
 
     @Roles({PersonRole.VERIFIER})
     @Override
     public Collection<ExpenseReport> findByStatus(ReportStatus status) {
         if (status == null) {
             throw new IllegalArgumentException();
         }
 
         return listProducer.getAllBy(status);
     }
 
     @Roles({PersonRole.VERIFIER, PersonRole.APPLICANT})
     @Override
     public Collection<ExpenseReport> findForSubmitterWithStatus(Person submitter, ReportStatus status) {
         if (submitter == null || status == null) {
             throw new IllegalArgumentException();
         }
 
         return listProducer.getAllForSubmitterWithStatus(submitter, status);
     }
 
     @Roles({PersonRole.VERIFIER})
     @Override
     public Collection<ExpenseReport> findForVerifierWithStatus(Person verifier, ReportStatus status) {
         if (verifier == null || status == null) {
             throw new IllegalArgumentException();
         }
 
         return listProducer.getAllForVerifierWithStatus(verifier, status);
     }
 }
