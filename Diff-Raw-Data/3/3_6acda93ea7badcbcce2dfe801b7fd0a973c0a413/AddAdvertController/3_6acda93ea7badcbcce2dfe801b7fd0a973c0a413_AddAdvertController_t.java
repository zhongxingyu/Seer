 package com.epam.adzhiametov.controller;
 
 import com.epam.adzhiametov.dao.AdvertDao;
 import com.epam.adzhiametov.enumeration.Operation;
 import com.epam.adzhiametov.enumeration.Section;
 import com.epam.adzhiametov.model.Advert;
 import com.epam.adzhiametov.validator.AdvertValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 
 import javax.validation.Valid;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Created by Arsen Adzhiametov on 7/31/13.
  */
 @Controller
 public class AddAdvertController {
 
     @Autowired
     AdvertDao advertDao;
 
     @Autowired
     AdvertValidator advertValidator;
 
     @InitBinder
     protected void initBinder(WebDataBinder binder) {
         binder.setValidator(advertValidator);
     }
 
     @RequestMapping(value = "/addadvert", method = RequestMethod.POST)
     public String addAdvert(@Valid @ModelAttribute("advert") Advert advert, BindingResult result, Model model) {
         if(result.hasErrors()) {
             model.addAttribute("sectionValues", Section.values());
             model.addAttribute("operationValues", Operation.values());
             return "add_advert";
         }
         advert.setTime(Calendar.getInstance());
         advertDao.create(advert);
        model.addAttribute("adverts", advertDao.findRange(1, RedirectController.ITEMS_ON_PAGE));
        model.addAttribute("page", 1);
         return "advert_list";
     }
 
 }
