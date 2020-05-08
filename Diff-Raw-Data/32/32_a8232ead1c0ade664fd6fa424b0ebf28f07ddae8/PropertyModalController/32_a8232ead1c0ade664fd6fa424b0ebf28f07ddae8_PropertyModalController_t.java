 package web.controller;
 
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import web.model.Property;
import web.model.PropertyOptions;
import web.model.PropertyType;
import web.service.PropertyOptionsService;
import web.service.PropertyService;
 
 /**
  *
  * @author Bernard <bernard.debecker@gmail.com>
  */
 @Controller
 public class PropertyModalController {
     
    @Autowired
    PropertyService propertyService;
     
    @Autowired
    PropertyOptionsService propertyOptionsService;
    
    
    @RequestMapping(value = "/s/property/{id}/modal/", method = RequestMethod.GET)
    public String propertyModalView(@PathVariable Integer id, Model model) {
        
        Property property = propertyService.findById(id);
        PropertyOptions options = propertyOptionsService.findByProperty(property);
        List<PropertyType> types = Arrays.asList(PropertyType.values());
        
        model.addAttribute("property", property);
        model.addAttribute("options", options);
        model.addAttribute("types", types);
        
         return "property_modal";
     }
 }
 
