 package org.conan.fans.weibo.web.rest;
 
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.conan.fans.system.model.ConfigDTO;
 import org.conan.fans.system.service.ConfigService;
 import org.conan.fans.weibo.service.UserService;
 import org.conan.fans.weibo.web.WebController;
 import org.conan.fans.weibo.web.form.AgeForm;
 import org.conan.fans.weibo.web.form.GenderForm;
 import org.conan.fans.weibo.web.form.UserForm;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping("/api")
 public class ApiController extends WebController {
 
     final private static Logger log = LoggerFactory.getLogger(ApiController.class);
 
     @Autowired
     UserService userService;
     @Autowired
     ConfigService configService;
 
    private ConfigDTO config(String type) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("type", type);
        return configService.getConfigOne(paramMap);
    }

     // 1999250817
     @RequestMapping(value = "/wage/{uid}", method = RequestMethod.GET)
     public HttpEntity<?> age(@PathVariable(value = "uid") String uid) {
         log.debug("age => " + uid);
         ConfigDTO config = config("rest.wage");
         AgeForm form = new AgeForm();
         form.setImg(MessageFormat.format(config.getImgUrl(), uid));
         form.setTweet(MessageFormat.format(config.getTemplate(), ""));
         return new ResponseEntity<AgeForm>(form, HttpStatus.OK);
     }
 
     @RequestMapping(value = "/gender/{uid}", method = RequestMethod.GET)
     public HttpEntity<?> gender(@PathVariable(value = "uid") String uid) {
         log.debug("gender => " + uid);
         ConfigDTO config = config("rest.gender");
         GenderForm form = new GenderForm();
         form.setImg(MessageFormat.format(config.getImgUrl(), uid));
         form.setTweet(config.getTemplate());
         return new ResponseEntity<GenderForm>(form, HttpStatus.OK);
     }
 
     @RequestMapping(value = "/user/{uid}", method = RequestMethod.GET)
     public HttpEntity<?> user(@PathVariable(value = "uid") String uid) {
         log.debug("user => " + uid);
 
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("uid", uid);
         UserForm form = new UserForm(userService.getUserOne(map));
         return new ResponseEntity<UserForm>(form, HttpStatus.OK);
     }
 
 }
