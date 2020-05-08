 package com.unisys.ch.jax.costserver.controller;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Collection;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import com.unisys.ch.jax.costserver.model.Cost;
 import com.unisys.ch.jax.costserver.service.CostService;
 
 @Controller
 @RequestMapping("/costs")
 public class CostController {
 
     @Autowired
     CostService toDoService;
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     @ResponseBody
     public Cost read(@PathVariable Long id) {
         Cost todo = toDoService.findById(id);
         if (todo == null) {
             throw new IllegalArgumentException(String.format("Unable to find ToDo with id: %d", id));
         }
         return todo;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     @ResponseBody
     public Collection<Cost> list(@RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "pageSize", required = false) Integer pageSize) {
         if (page == null) {
             page = 1;
         }
         if (pageSize == null){
            pageSize = Integer.MAX_VALUE;
         }
         return toDoService.findAll(pageSize * (page - 1), pageSize);
     }
 
     @RequestMapping(method = RequestMethod.POST)
     @ResponseBody
     public Cost create(@RequestBody Cost todo) {
         return toDoService.save(todo);
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.NO_CONTENT)
     public void update(@PathVariable(value = "id") Long id, @RequestBody Cost todo) {
         if (todo.getId() == null || !todo.getId().equals(id)) {
             throw new IllegalArgumentException(String.format("The id of given ToDo doesn't match URL id: %d", id));
         }
         toDoService.save(todo);
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.NO_CONTENT)
     public void delete(@PathVariable(value = "id") Long id) {
         toDoService.delete(id);
     }
 
     @ExceptionHandler(IllegalArgumentException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     @ResponseBody
     public String handleClientErrors(Exception ex) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         ex.printStackTrace(pw);
         return String.format("<html><head></head><body><h1>Client Error occured: %s</h1><p>%s</p>", ex.getMessage(), sw.toString());
     }
 
     @ExceptionHandler(Exception.class)
     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
     @ResponseBody
     public String handleServerErrors(Exception ex) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         ex.printStackTrace(pw);
         return String.format("<html><head></head><body><h1>Server Error occured: %s</h1><p>%s</p>", ex.getMessage(), sw.toString());
     }
 }
