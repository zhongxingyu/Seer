 package org.mailoverlord.server.controllers;
 
 import org.mailoverlord.server.entities.Message;
 import org.mailoverlord.server.repositories.MessageRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.List;
 
 /**
 * Table Controller.
  */
 @Controller
 public class RestController {
 
     @Autowired
     private MessageRepository messageRepository;
 
     @RequestMapping(value = "/messages", produces = {"application/xml", "application/json"})
     public @ResponseBody List<Message> getTableData(Pageable pageable) {
         Page<Message> page = messageRepository.findAll(pageable);
         return page.getContent();
     }
 
 }
