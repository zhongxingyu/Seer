 package com.ffbit.todo.web;
 
 import com.ffbit.todo.domain.ToDo;
 import com.ffbit.todo.repository.ToDoRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.List;
 import java.util.Map;
 
 @Controller
 @RequestMapping("/todos")
@Transactional
 public class ToDoController {
     private Logger logger = LoggerFactory.getLogger(getClass());
 
     @Autowired
     private ToDoRepository repository;
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         binder.setDisallowedFields("id");
     }
 
     @RequestMapping(method = RequestMethod.GET)
     @ResponseBody
     public List<?> index() {
         logger.info("index");
 
         return repository.findAll();
     }
 
     @RequestMapping(method = RequestMethod.POST)
     @ResponseBody
     public Object create(@RequestBody ToDo todo) {
         logger.info("create {}", todo);
         repository.save(todo);
         logger.info("created {}", todo);
 
         return todo;
     }
 
     @RequestMapping(value = "{id}", method = RequestMethod.PUT)
     @ResponseBody
     public ToDo update(@RequestBody ToDo todo, @PathVariable Integer id) {
         logger.info("update todo: {} {}", id, todo);
 
         if (repository.exists(id)) {
            ToDo existent = repository.findOne(id);
 
            existent.setTitle(todo.getTitle());
            existent.setOrder(todo.getOrder());
            existent.setDone(todo.getDone());

            repository.save(existent);
 
            logger.info("updated todo: {} {}", id, existent);
            return existent;
        }
 
         return todo;
     }
 
     @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
     @ResponseBody
     public HttpStatus destroy(@PathVariable Integer id) {
         logger.info("destroy {}", id);
 
         if (!repository.exists(id)) {
             logger.info("not found {}", id);
 
             return HttpStatus.NOT_FOUND;
         }
 
         repository.delete(id);
 
         logger.info("destroyed {}", id);
 
         return HttpStatus.OK;
     }
 
 }
