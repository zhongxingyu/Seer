 package com.ssm.llp.web.controller;
 
 import com.ssm.llp.biz.validation.PoisonValidator;
 import com.ssm.llp.biz.validation.SearchValidator;
 import com.ssm.llp.biz.validation.script.ScriptUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.*;
 
 /**
  * @author rafizan.baharum
  * @since 9/9/13
  */
 @Controller
 @RequestMapping("/validate")
@Transactional
 public class ValidateController {
 
     private Logger log = LoggerFactory.getLogger(ValidateController.class);
 
     @Autowired
     private PoisonValidator poisonValidator;
 
     @Autowired
     private SearchValidator searchValidator;
 
     @Autowired
     private ScriptUtil scriptUtil;
 
     /**
      * fail fast validation
      *
      * @param name
      * @param type
      * @param model
      * @return
      */
     @RequestMapping(method = RequestMethod.GET)
     public String validate(@RequestParam("name") String name, @RequestParam("type") String type, ModelMap model) {
         name = scriptUtil.scrubName(name);
         log.debug("validate: " + name);
         boolean poisoned = poisonValidator.isPoisoned(name);
         boolean existed = searchValidator.isExisted(name);
         boolean valid = !(poisoned | existed);
         log.debug("poisoned?: " + poisoned);
         log.debug("existed?: " + existed);
         log.debug("valid?: " + valid);
 
         model.put("valid", valid);
         model.put("name", name);
         model.put("type", type);
         return "index";
     }
 
     @RequestMapping(value = "dashboard", method = RequestMethod.GET)
     public String validateDashboard(@RequestParam("name") String name, @RequestParam("type") String type, ModelMap model) {
         validate(name, type, model);
         return "secure/dashboard";
     }
 
     @RequestMapping(value = "/json/{name}", method = RequestMethod.GET)
     @ResponseBody
     public String validateJson(@PathVariable("name") String name, ModelMap model) {
         name = scriptUtil.scrubName(name);
         log.debug("validate: " + name);
         boolean poisoned = poisonValidator.isPoisoned(name);
         boolean existed = searchValidator.isExisted(name);
        boolean valid = !(poisoned | existed);
         log.debug("poisoned?: " + poisoned);
         log.debug("existed?: " + existed);
         if (valid) return "Name not valid";
         else return "Name is valid";
     }
 }
