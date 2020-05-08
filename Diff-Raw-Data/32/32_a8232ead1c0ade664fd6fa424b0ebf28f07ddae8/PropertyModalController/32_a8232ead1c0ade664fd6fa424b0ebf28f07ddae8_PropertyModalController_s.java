 package web.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author Bernard <bernard.debecker@gmail.com>
  */
 @Controller
 public class PropertyModalController {
     
     
    @RequestMapping(value = "/s/property/modal/", method = RequestMethod.GET)
    public String propertyModalView(Model model) {
         return "property_modal";
     }
 }
 
