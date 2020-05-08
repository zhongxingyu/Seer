 package org.xezz.timeregistration.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.xezz.timeregistration.model.Project;
 import org.xezz.timeregistration.services.ProjectService;
 
 /**
  * User: Xezz
  * Date: 05.05.13
  * Time: 11:18
  * Handle requests for Projects
  */
 @Controller
 @RequestMapping(value = "/project")
 public class ProjectController {
 
     @Autowired
     ProjectService service;
 
     Logger logger = LoggerFactory.getLogger(this.getClass());
 
     /**
      * Request all Projects
      *
      * @return List of all Projects
      */
     @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public Iterable<Project> getAll() {
         logger.info("Requested all Projects");
         return service.getAll();
     }
 
     /**
      * Request to create a new Project
      *
      * @param project The Project to persist
      * @return Persisted Project
      */
     @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public Project create(@RequestBody Project project) {
         logger.info("Request to create a new Project");
         return service.addNewProject(project);
     }
 
     /**
      * Request to upate an existing Project
      *
      * @param project The Project to update
      * @return Updated Project
      */
     @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
     public Project update(@RequestBody Project project) {
         logger.info("Request to update an existing project");
         return service.updateProject(project);
     }
 
     /**
      * Request to get a Project by its ID
      *
      * @param id Long the ID of the Project
      * @return requested Project
      */
     @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
    public Project getById(@PathVariable("id") Long id) {
         logger.info("Request to get a Project by id");
         return service.getById(id);
     }
 
     @RequestMapping(value = "/name/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     @ResponseBody
    private Iterable<Project> getByName(@PathVariable("name") String name) {
         logger.info("Request to get Projects by name");
         return service.getByName(name);
     }
 
 
     // TODO: Add DELETE for Project
     // TODO: Decide how to add a time span etc.
 }
