 package org.xezz.timeregistration.services.impl;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.xezz.timeregistration.model.Coworker;
 import org.xezz.timeregistration.model.Project;
 import org.xezz.timeregistration.model.TimeSpan;
 import org.xezz.timeregistration.repositories.TimeSpanRepository;
 import org.xezz.timeregistration.services.TimeSpanService;
 
 import java.util.Date;
 
 /**
  * User: Xezz
  * Date: 29.04.13
  * Time: 15:55
  * Implementation of the TimeSpanService with Spring repository functionality
  */
 @Service
 public class TimeSpanServiceImpl implements TimeSpanService {
 
     @Autowired
     TimeSpanRepository repo;
 
     @Override
     public Iterable<TimeSpan> timeSpansForCoworker(Coworker coworker) {
         return repo.findByCoworker(coworker);
     }
 
     @Override
     public Iterable<TimeSpan> timeSpansForProject(Project project) {
         return repo.findByProject(project);
     }
 
     @Override
     public TimeSpan addNewTimeSpan(Coworker c, Project p, Date startTime, Date endTime) {
         TimeSpan timeSpan = new TimeSpan();
         timeSpan.setCoworker(c);
         timeSpan.setProject(p);
         timeSpan.setStartTime(startTime);
         timeSpan.setEndTime(endTime);
         return repo.save(timeSpan);
     }
 
     @Override
     public TimeSpan getTimeSpanById(Long id) {
         return repo.findOne(id);
     }
 
     @Override
     public TimeSpan createNewTimeSpan(TimeSpan timeSpan) {
         return repo.save(timeSpan);
     }
 
     @Override
     public TimeSpan updateTimeSpan(TimeSpan timeSpan) {
         return repo.save(timeSpan);
     }
 
     @Override
     public Iterable<TimeSpan> findAllTimeSpans() {
         return repo.findAll();
     }
 
     public TimeSpanRepository getRepo() {
         return repo;
     }
 
     public void setRepo(TimeSpanRepository repo) {
         this.repo = repo;
     }
 
 
 }
