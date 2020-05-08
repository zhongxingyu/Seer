 package cz.muni.fi.pv243.et.controller;
 
import cz.muni.fi.pv243.et.model.PersonWrapper;
 import cz.muni.fi.pv243.et.model.*;
 import cz.muni.fi.pv243.et.service.ExpenseReportService;
 import cz.muni.fi.pv243.et.util.CurrentPerson;
 
 import javax.enterprise.inject.Model;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 @Model
 public class ExpenseController {
 
     @Inject
     private ExpenseModel model;
 
     @Inject
     private ExpenseReportService service;
 
     @Inject
     @CurrentPerson
     private PersonWrapper currentPerson;
 
     @Produces
     @Named("expenseReports")
     public Collection<ExpenseReport> getAllReports() {
         return service.findAll();
     }
 
     public String showSingleReport(Long id) {
         model.setReport(service.get(id));
 
         return "/secured/report";
     }
 
     public String editReport(Long id) {
         model.setReport(service.get(id));
 
         return "/secured/editReport";
     }
 
     public String createReport() {
         model.setReport(new ExpenseReport());
 
         return "/secured/createReport";
     }
 
     public String saveReport() {
         ExpenseReport r = model.getReport();
         boolean isNew = r.getId() == null;
 
         r.setLastChangeDate(new Date());
         if (isNew) {
             r.setStatus(ReportStatus.OPEN);
             r.setSubmitter(currentPerson.getPerson());
             r.setMoneyTransfers(new ArrayList<MoneyTransfer>());
             r.setPayments(new ArrayList<Payment>());
             r.setLastSubmittedDate(new Date());
         }
         service.save(r);
 
         model.setReport(r);
 
         return "/secured/report?faces-redirect=true";
     }
 
 }
