 package org.xezz.timeregistration.services.impl;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 import org.xezz.timeregistration.model.Customer;
 import org.xezz.timeregistration.model.Project;
 import org.xezz.timeregistration.model.TimeSpan;
 import org.xezz.timeregistration.repositories.ProjectRepository;
 import org.xezz.timeregistration.services.ProjectService;
 
 /**
  * User: Xezz
  * Date: 05.05.13
  * Time: 11:14
  */
@Service
 public class ProjectServiceImpl implements ProjectService {
 
     @Autowired
     ProjectRepository repo;
 
     @Override
     public Iterable<Project> getAll() {
         return repo.findAll();
     }
 
     @Override
     public Iterable<Project> getByName(String name) {
         return repo.findByNameLike("%" + name + "%");
     }
 
     @Override
     public Iterable<Project> getByCustomer(Customer c) {
         return repo.findByCustomer(c);
     }
 
     @Override
     public Project getByTimeFrame(TimeSpan t) {
         return repo.findProjectByTimeframe(t);
     }
 
     @Override
     public Project getById(Long id) {
         return repo.findOne(id);
     }
 
     @Override
     public Project addNewProject(Project p) {
         return repo.save(p);
     }
 
     @Override
     public Project updateProject(Project p) {
         // TODO: update or (as of right now) also safe (aka against REST)
         return repo.save(p);
     }
 }
