 package org.xezz.timeregistration.services.impl;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.xezz.timeregistration.dao.CustomerDAO;
 import org.xezz.timeregistration.dao.ProjectDAO;
 import org.xezz.timeregistration.dao.TimeSpanDAO;
 import org.xezz.timeregistration.model.Project;
 import org.xezz.timeregistration.repositories.CustomerRepository;
 import org.xezz.timeregistration.repositories.ProjectRepository;
 import org.xezz.timeregistration.repositories.TimeSpanRepository;
 import org.xezz.timeregistration.services.ProjectService;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Xezz
  * Date: 05.05.13
  * Time: 11:14
  */
 @Service
 @Transactional
 public class ProjectServiceImpl implements ProjectService {
 
     @Autowired
     ProjectRepository repo;
     @Autowired
     CustomerRepository customerRepository;
     @Autowired
     TimeSpanRepository timeSpanRepository;
 
     @Override
     public Iterable<ProjectDAO> getAll() {
         final Iterable<Project> all = repo.findAll();
         final List<ProjectDAO> daoList = new ArrayList<ProjectDAO>();
         for (Project p : all) {
             daoList.add(new ProjectDAO(p));
         }
         return daoList;
     }
 
     @Override
     public Iterable<ProjectDAO> getByName(String name) {
         final Iterable<Project> byNameLike = repo.findByNameLike("%" + name + "%");
         final List<ProjectDAO> daoList = new ArrayList<ProjectDAO>();
         for (Project p : byNameLike) {
             daoList.add(new ProjectDAO(p));
         }
         return daoList;
     }
 
     @Override
     public Iterable<ProjectDAO> getByCustomer(CustomerDAO c) {
         final Iterable<Project> byCustomer = repo.findByCustomer(customerRepository.findOne(c.getCustomerId()));
         final List<ProjectDAO> daoList = new ArrayList<ProjectDAO>();
         for (Project p : byCustomer) {
             daoList.add(new ProjectDAO(p));
         }
         return daoList;
     }
 
     // FIXME: NULL CHECK
     @Override
     public ProjectDAO getByTimeFrame(TimeSpanDAO t) {
         return new ProjectDAO(repo.findOne(t.getProjectId()));
     }
 
     @Override
     public ProjectDAO getById(Long id) {
         return new ProjectDAO(repo.findOne(id));
     }
 
     @Transactional
     @Override
     public ProjectDAO addNewProject(ProjectDAO p) {
         return new ProjectDAO(repo.save(new Project(p)));
     }
 
     @Transactional
     @Override
     public ProjectDAO updateProject(ProjectDAO p) {
         // TODO: update or (as of right now) also safe (aka against REST)
         return new ProjectDAO(repo.save(new Project(p)));
     }
 }
