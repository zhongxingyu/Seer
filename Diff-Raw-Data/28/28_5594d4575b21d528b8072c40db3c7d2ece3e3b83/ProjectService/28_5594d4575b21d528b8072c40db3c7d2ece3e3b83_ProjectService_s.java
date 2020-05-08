 package org.xezz.timeregistration.services;
 
 import org.xezz.timeregistration.dao.CustomerDAO;
 import org.xezz.timeregistration.dao.ProjectDAO;
 import org.xezz.timeregistration.dao.TimeSpanDAO;
 
 /**
  * User: Xezz
  * Date: 29.04.13
  * Time: 14:37
  */
 public interface ProjectService {
     /**
      * Get all Projects
      *
      * @return List of all Projects
      */
     Iterable<ProjectDAO> getAll();
 
     /**
      * List of Projects by name
      *
      * @param name of the Project
      * @return List of all Projects with that name
      */
     Iterable<ProjectDAO> getByName(String name);
 
     /**
      * List of Projects by Customer
      *
      * @param c Customer
      * @return List of all Projects of this Customer
      */
     Iterable<ProjectDAO> getByCustomer(CustomerDAO c);
 
     /**
      * Project by TimeSpan
      *
      * @param t TimeSpan assigned to a Project
      * @return Project that contains this TimeSpan
      */
     ProjectDAO getByTimeFrame(TimeSpanDAO t);
 
     /**
      * Project by identifier
      *
      * @param id Long the id
      * @return Project with this id or null if not exist
      */
     ProjectDAO getById(Long id);
 
     /**
      * Persist a Project
      *
      * @param p Project to persist
      * @return persisted Project
      */
     ProjectDAO addNewProject(ProjectDAO p);
 
     /**
      * Updated existing Project
      *
      * @param p Project that has been updated
      * @return Project that has been persisted
      */
     ProjectDAO updateProject(ProjectDAO p);
 
 }
