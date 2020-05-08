 package com.miniinf.OSPManager.web.conttrollers;
 
 import com.miniinf.OSPManager.data.FireTruck;
 import com.miniinf.OSPManager.data.repositories.FireTruckRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.validation.Valid;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Szubster
  * Date: 21.11.12
  * Time: 10:57
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/firetruck")
 public class FireTruckController {
 
     @Autowired
     FireTruckRepository repository;
 
     @RequestMapping(value = "/create", method = RequestMethod.POST)
     public String create(@Valid FireTruck fireTruck, BindingResult bindingResult, Model uiModel) {
         if (bindingResult.hasErrors()) {
             uiModel.addAttribute("fireTruck", fireTruck);
            return "fireTruck/create";
         }
         uiModel.asMap().clear();
         fireTruck = repository.save(fireTruck);
        return "redirect:/fireTruck/" + fireTruck.getId();
     }
 
     @RequestMapping(value = "/create")
     public
     @ModelAttribute
     FireTruck form() {
         return new FireTruck();
     }
 
     @RequestMapping(value = "/{fireTruck}")
    public void show(@ModelAttribute FireTruck fireTruck) {
     }
 
    @RequestMapping(value = "/{fireTruck}/update")
    public void edit(@ModelAttribute FireTruck fireTruck) {
     }
 
     @RequestMapping()
     public
     @ModelAttribute("fireTrucks")
     Page<FireTruck> list(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        return repository.findAll(new PageRequest(page, size));
     }
 }
