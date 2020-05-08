 package com.miniinf.OSPManager.web.conttrollers;
 
 import com.miniinf.OSPManager.data.FireFighter;
 import com.miniinf.OSPManager.data.repositories.FireFighterRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 
 import javax.validation.Valid;
 import javax.validation.constraints.Min;
 import java.math.BigInteger;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Szubster
  * Date: 27.11.12
  * Time: 19:21
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/firefighter")
 @Validated
 public class FireFighterController {
 
     @Autowired
     FireFighterRepository repository;
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         dateFormat.setLenient(false);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
     }
 
     @RequestMapping(value = "/create", method = RequestMethod.POST)
     public String create(@Valid FireFighter fireFighter, BindingResult bindingResult, Model uiModel) {
         if (bindingResult.hasErrors()) {
             uiModel.addAttribute("fireFighter", fireFighter);
             return "firefighter/create";
         }
         uiModel.asMap().clear();
         fireFighter = repository.save(fireFighter);
         return "redirect:/firefighter/" + fireFighter.getId();
     }
 
     @RequestMapping(value = "/create")
     public
     @ModelAttribute
     FireFighter form() {
         return new FireFighter();
     }
 
     @RequestMapping("/{fireFighter}")
     public void show(@ModelAttribute("fireFighter") FireFighter fireFighter) {
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
     public void delete(@PathVariable BigInteger id) {
         repository.delete(id);
     }
 
     @RequestMapping("/update/{fireFighter}")
     public void edit(@ModelAttribute("fireFighter") FireFighter fireFighter) {
     }
 
     @RequestMapping(value = "/update/{fireFighter}", method = RequestMethod.PUT)
     public String update(@Valid FireFighter fireFighter, BindingResult bindingResult, Model uiModel) {
         if (bindingResult.hasErrors()) {
             uiModel.addAttribute("fireFighter", fireFighter);
             return "firefighter/update/" + fireFighter.getId();
         }
         uiModel.asMap().clear();
         fireFighter = repository.save(fireFighter);
         return "redirect:/firefighter/" + fireFighter.getId();
     }
 
     @RequestMapping()
     public
     @ModelAttribute("fireFighters")
     Page<FireFighter> list(
             @Min(1) @RequestParam(value = "page", defaultValue = "1", required = false) int page,
             @Min(1) @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
         return repository.findAll(new PageRequest(page - 1, size));
     }
 }
