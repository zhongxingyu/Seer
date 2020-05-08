 package com.beayoscar.babynames.web;
 
 import java.util.Collection;
 
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.beayoscar.babynames.domain.Name;
 
 @RooWebScaffold(path = "names", formBackingObject = Name.class)
 @RequestMapping("/names")
 @Controller
 public class NameController {
 
 	@ModelAttribute("names")
     public Collection<Name> populateNames() {
         //return Name.findAllNames();
 		return null;
     }
 	
     @RequestMapping(value = "/listgenders", method = RequestMethod.GET)
     public String list(Model uiModel) {
         return "names/listbygender";
     }
     
     @RequestMapping(value = "/{id}/liked", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonw(@PathVariable("id") Long id) {
     	Name name = Name.findName(id);
     	name.setVote(name.getVote() + 1);
         HttpHeaders headers= new HttpHeaders();
         headers.add("Content-Type", "application/text");
         if (name.merge() == null) {
             return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
         }
         return new ResponseEntity<String>(name.toJson(), headers, HttpStatus.OK);
     }
 }
