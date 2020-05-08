 package org.xezz.timeregistration.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.xezz.timeregistration.dao.ProjectDAO;
 import org.xezz.timeregistration.services.ProjectService;
 
 /**
  * User: Xezz
  * Date: 05.05.13
  * Time: 11:18
  * Handle requests for Projects
  */
 @Controller
 @RequestMapping(value = "api/project")
 public class ProjectController {
 
     @Autowired
     ProjectService service;
 
     private final static Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);
 
     /**
      * Request all Projects
      *
      * @return List of all Projects
      */
     @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public Iterable<ProjectDAO> getAll() {
         LOGGER.info("Requested all Projects");
         return service.getAll();
     }
 
     /**
      * Request to create a new Project
      *
      * @param projectDAO The Project to persist
      * @return Persisted Project
      */
     @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public ProjectDAO create(@RequestBody ProjectDAO projectDAO) {
         LOGGER.info("Request to create a new Project");
         return service.addNewProject(projectDAO);
     }
 
     /**
      * Request to upate an existing Project
      *
      * @param projectDAO The Project to update
      * @return Updated Project
      */
     @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public ProjectDAO update(@RequestBody ProjectDAO projectDAO) {
         LOGGER.info("Request to update an existing project");
         return service.updateProject(projectDAO);
     }
 
     /**
      * Request to get a Project by its ID
      *
      * @param id Long the ID of the Project
      * @return requested Project
      */
     @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public ProjectDAO getById(@PathVariable("id") Long id) {
         LOGGER.info("Request to get a Project by id");
         return service.getById(id);
     }
 
     @RequestMapping(value = "/name/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     private Iterable<ProjectDAO> getByName(@PathVariable("name") String name) {
         LOGGER.info("Request to get Projects by name");
         return service.getByName(name);
     }
 
     @RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     private void delete(@RequestBody ProjectDAO projectDAO) {
         LOGGER.info("Request to delete a Project");
         service.deleteProject(projectDAO);
     }
 
 
     // TODO: Decide how to add a time span etc.
 }
