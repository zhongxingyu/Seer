 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rwth.travelTogether.controller;
 
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import com.rwth.travelTogether.model.Group;
 import com.rwth.travelTogether.persistence.GroupDAO;
 import com.rwth.travelTogether.persistence.impl.GroupDAOImpl;
 import java.util.Calendar;
import java.util.Date;
import javax.xml.crypto.Data;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  *
  * @author marco
  */
 @org.springframework.stereotype.Controller
 @SessionAttributes
 public class CreateGroupController{

     @Autowired
     private GroupDAO groupDAO;
 
     //We are binding our request into the group instance of Group
     @RequestMapping(value = "/createGroup", method = RequestMethod.POST)
        public String createGroup(@ModelAttribute("group") Group group, BindingResult result) {
 
         groupDAO = new GroupDAOImpl();
 
         group.setFounder("TestFounder");
 
         Calendar calendar = Calendar.getInstance();
         group.setCreationDate(calendar.getTime());
 
         try{
             groupDAO.persist(group);
         }catch(Error e){
             e.printStackTrace();
         }
 
        return "redirect:groups.htm";
     }
 }
