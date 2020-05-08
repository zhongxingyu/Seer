 package com.vaadin.training.bugrap.service;
 
 import com.vaadin.training.bugrap.domain.entity.*;
 import com.vaadin.training.bugrap.domain.repository.ProjectRepository;
 import com.vaadin.training.bugrap.domain.repository.ReportRepository;
 import com.vaadin.training.bugrap.domain.repository.UserRepository;
 
 import javax.ejb.Singleton;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.inject.Inject;
 import java.util.Date;
 import java.util.Random;
 
 /**
  * @author Marcus Hellberg (marcus@vaadin.com)
  */
 @Singleton
 public class DummyDataService {
 
     @Inject
     private ProjectRepository projectRepository;
 
     @Inject
     private UserRepository userRepository;
 
     @Inject
     private ReportRepository reportRepository;
 
     private User author;
     private User assigned;
     private Project project;
 
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void initDB() {
         if (projectRepository.findAll().isEmpty()) {
             createUsers();
             createProject();
             generateReports();
         }
     }
 
     private void createUsers() {
         assigned = new User();
         assigned.setUsername("assigned");
         assigned.setName("Assigned User");
 
         author = new User();
         author.setUsername("author");
         author.setName("Author User");
 
         assigned = userRepository.save(assigned);
         author = userRepository.save(author);
     }
 
     private void createProject() {
         project = new Project();
         project.setName("Project name that is rather long blah blah blah blah");
         project.addProjectVersion("0.0.1");
         project.addProjectVersion("0.0.2");
         project.addProjectVersion("0.0.3");
         project.addProjectVersion("0.0.4");
         project.addParticipant(assigned);
         project.addParticipant(author);
 
         project = projectRepository.save(project);
     }
 
     public void generateReports() {
 
         for (ProjectVersion version : project.getProjectVersions()) {
             Random random = new Random(123);
 
             for (int i = 1; i <= random.nextInt(20) + 1; i++) {
                 Report report = new Report();
                 report.setType(ReportType.values()[random.nextInt(ReportType.values().length)]);
                 report.setStatus(ReportStatus.values()[random.nextInt(ReportStatus.values().length)]);
                 report.setSummary("This is bug #" + i);
                 report.setDescription("This is a detailed description. It is very long. This is a detailed description. It is very long. This is a detailed description. It is very long. This is a detailed description. It is very long.");
                 report.setProjectVersion(version);
                 report.setAssigned(assigned);
                 report.setAuthor(author);
                 report.setTimestamp(new Date());
                 reportRepository.save(report);
             }
         }
     }
 }
