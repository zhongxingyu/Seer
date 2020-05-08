 package com.personnelmanager.controller;
 
 import com.personnelmanager.domain.db.Person;
 import com.personnelmanager.service.PersonnelService;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 
 import javax.validation.Valid;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * Controller for personnel, the matching of the url is done after personnelManager.
  * User: MikeChen
  * Date: Nov 24, 2010
  * Time: 5:39:56 PM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 public class PersonnelController {
     Logger logger = Logger.getLogger(PersonnelController.class);
 
    @Autowired
    private Validator validator;
 
     @Autowired
     private PersonnelService service;
 
     public PersonnelService getService() {
         return service;
     }
 
     public void setService(PersonnelService service) {
         this.service = service;
     }
 
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(validator);
    }
     /**
      * For every request for this controller, this will
      * create a person instance for the form.
      */
     @ModelAttribute
     public Person newRequest(@RequestParam(required=false) Long id) {
         System.out.println("Id is " + id);
         return (id != null ? service.getPerson(id) : new Person());
     }
 
     @RequestMapping(value="/welcome.htm")
     public String welcome(Model model) {
         return "welcome";
     }
 
     @RequestMapping(value="/addPerson.htm", method = RequestMethod.GET)
     public String addPerson(Model model) {
         return "addPerson";
     }
 
     @RequestMapping(value="/savePerson.htm", method = RequestMethod.POST)
     public String savePerson(@Valid Person person, BindingResult result) {
         Person personToBeSaved = null;
         if (result.hasErrors()) {
             return "addPerson";
         }
         if (person.getPersonId() != null) {
             personToBeSaved = service.getPerson(person.getPersonId());
             updatePerson(personToBeSaved, person);
         } else {
             personToBeSaved = person;
         }
 
         service.save(personToBeSaved);
         return "welcome";
     }
 
     private void updatePerson(Person personToBeSaved, Person person) {
         personToBeSaved.setSalutation(person.getSalutation());
         personToBeSaved.setFirstName(person.getFirstName());
         personToBeSaved.setMiddleName(person.getMiddleName());
         personToBeSaved.setLastName(person.getLastName());
         personToBeSaved.setDob(person.getDob());
         personToBeSaved.setRole(person.getRole());
 
     }
 
     @RequestMapping(value="/searchPerson.htm", method = RequestMethod.GET)
     public String searchPerson() {
         return "searchPerson";
     }
 
     @RequestMapping(value="/searchPerson.htm", method = RequestMethod.POST)
     public String showSearchPerson(@ModelAttribute Person criteria, Model model) {
         List searchResult =  service.findPerson(criteria);
         model.addAttribute("searchResult", searchResult);
         return "searchPersonResult";
     }
 
     @RequestMapping(value="/editPerson.htm", method = RequestMethod.GET)
     public String editPerson() {
         return "addPerson";
     }
 
 }
