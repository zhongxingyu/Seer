 package com.jclarity.had_one_dismissal.web;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.jclarity.had_one_dismissal.domain.Company;
 import com.jclarity.had_one_dismissal.domain.CompanyAndJob;
 import com.jclarity.had_one_dismissal.domain.JobListing;
 import com.jclarity.had_one_dismissal.jmx.PerformanceProblems;
 
 // Naively avoid database concurrency issues by aggressively locking
 // Potential deadlock between the delete and save methods which perform
 // lock acquisition/release in a different order
 @RequestMapping("/companyandjob/**")
 @Controller
 public class CompanyAndJobController {
 
     @Autowired
     private PerformanceProblems problems;
 
     @PersistenceContext
     private EntityManager em;
 
     @RequestMapping(value="{jobId}/", method = RequestMethod.DELETE)
     public String delete(@PathVariable("jobId") Long jobId, Model uiModel) {
         if (problems.isDeadlockEnabled()) {
             JobListing.LOCK.lock();
             try {
                 Company.LOCK.lock();
                 try {
                     remoteListing(jobId);
                 } finally {
                     Company.LOCK.unlock();
                 }
             } finally {
                 JobListing.LOCK.unlock();
             }
         } else {
             remoteListing(jobId);
         }
         return index(uiModel);
     }
 
     private void remoteListing(Long jobId) {
         JobListing job = JobListing.findJobListing(jobId);
         Company company = job.getCompany();
         company.remove();
         job.remove();
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public String save(CompanyAndJob object, Model uiModel) {
         Company company = object.getCompany();
         JobListing job = object.getJob();
 
         if (problems.isDeadlockEnabled()) {
             Company.LOCK.lock();
             try {
                 JobListing.LOCK.lock();
                 try {
                     persist(company, job);
                 } finally {
                     JobListing.LOCK.unlock();
                 }
             } finally {
                 Company.LOCK.unlock();
             }
         } else {
             persist(company, job);
         }
        return "index";
     }
 
     private void persist(Company company, JobListing job) {
         company.persist();
         job.persist();
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String index(Model uiModel) {
         uiModel.addAttribute("object", new CompanyAndJob());
         return "companyandjob/index";
     }
 
 }
