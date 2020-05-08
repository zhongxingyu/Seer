 package com.fourneth.ims.controller;
 
 import com.fourneth.ims.domain.Employee;
 import com.fourneth.ims.repository.EmployeeRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Controller
 public class SampleController {
 
     private static final Logger logger = LoggerFactory.getLogger(SampleController.class);
 
     @Autowired
     private EmployeeRepository er;
 
     @RequestMapping(method = RequestMethod.GET, value = "/employee/{id}", produces = "application/json")
     public @ResponseBody Employee employee(@PathVariable int id) {
 
         logger.info("Employee get request received id [{}]", id);
 
         return er.findById(id);
     }
 
     @RequestMapping(method = RequestMethod.PUT, value = "/employee/",
             consumes = "application/json", produces = "application/json")
    public @ResponseBody Employee create(@RequestBody Employee e) {
         logger.info("Employee create request received [{}]", e);
         er.save(e);
         return e;
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/employees", produces = "application/json")
     public @ResponseBody List<Employee> findAll() {
         logger.info("Employee get request received.....");
         return er.findAll();
     }
 }
