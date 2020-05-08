 package web.controller;
 
 import java.security.Principal;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import web.utils.StaticMap;
 import web.model.Property;
 import web.model.User;
 import web.service.PropertyService;
 import web.service.UserService;
 
 /**
  *
  * @author Bernard <bernard.debecker@gmail.com>
  */
 @Controller
 public class UserController {
 
     @Autowired
     private UserService userService;
     @Autowired
     private PropertyService propertyService;
 
     @RequestMapping(value = "/s/account/{username}", method = RequestMethod.GET)
     public String userView(@PathVariable String username, Model model, Principal current) {
 
         // Get User
         User user = userService.findByUsername(username);
         Integer propertyCount = propertyService.findProperty(user).size();
         List<Property> properties = propertyService.findProperty(user);
         Integer evaluation = 0;
         Integer nbEval = 0;
         for (int i = 0; i < properties.size(); i++) {
             if (properties.get(i).getNote() != null) {
                 evaluation = evaluation + properties.get(i).getNote();
                 nbEval++;
             }
         }
         if (nbEval > 0) {
             evaluation = evaluation / nbEval;
         } else {
             evaluation = -1;
         }
         String pathMap;
         pathMap = StaticMap.buildMapURL(properties);
 
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             model.addAttribute("current", u_log);
         }
         model.addAttribute("user", user);
         model.addAttribute("propertyCount", propertyCount);
         model.addAttribute("map", pathMap);
         model.addAttribute("evaluation", evaluation);
 
         return "user";
     }
     
     @RequestMapping(value = "/s/account/{username}/properties", method = RequestMethod.GET)
     public String userPropertiesView(@PathVariable String username, Model model, Principal current) {
 
         // Get User
         User user = userService.findByUsername(username);
         List<Property> properties = propertyService.findProperty(user);
         
         String pathMap;
         pathMap = StaticMap.buildMapURL(properties);
         
 
         if (current != null) {
             User u_log = userService.findByUsername(current.getName());
             model.addAttribute("current", u_log);
         }
         model.addAttribute("user", user);
        model.addAttribute("properties", properties);
         model.addAttribute("map", pathMap);
         
         
 
         return "user_properties";
     }
 }
