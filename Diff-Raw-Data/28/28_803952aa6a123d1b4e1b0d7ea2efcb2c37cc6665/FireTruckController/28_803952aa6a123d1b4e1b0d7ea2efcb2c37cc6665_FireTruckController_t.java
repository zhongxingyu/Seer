 package com.miniinf.OSPManager.web.conttrollers;
 
 import com.miniinf.OSPManager.data.FireTruck;
 import com.miniinf.OSPManager.data.repositories.FireTruckRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.math.BigInteger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Szubster
  * Date: 21.11.12
  * Time: 10:57
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/firetruck")
@Validated
 public class FireTruckController {
 
     @Autowired
     FireTruckRepository repository;
 
     @RequestMapping(value = "/create", method = RequestMethod.POST)
     public String create(@Valid FireTruck fireTruck, BindingResult bindingResult, Model uiModel) {
         if (bindingResult.hasErrors()) {
             uiModel.addAttribute("fireTruck", fireTruck);
            return "firetruck/create";
         }
         uiModel.asMap().clear();
         fireTruck = repository.save(fireTruck);
        return "redirect:/firetruck/" + fireTruck.getId();
     }
 
     @RequestMapping(value = "/create")
     public
     @ModelAttribute
     FireTruck form() {
         return new FireTruck();
     }
 
     @RequestMapping(value = "/{fireTruck}")
    public void show(@ModelAttribute("fireTruck") FireTruck fireTruck) {
        BigInteger id = fireTruck.getId();
     }
 
    @RequestMapping(value = "/update/{fireTruck}")
    public void edit(@ModelAttribute("fireTruck") FireTruck fireTruck) {
     }
 
     @RequestMapping()
     public
     @ModelAttribute("fireTrucks")
     Page<FireTruck> list(
            @Min(1) @RequestParam(value = "page", defaultValue = "1", required = false) int page,
            @Min(1) @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        return repository.findAll(new PageRequest(page - 1, size));
     }
 }
