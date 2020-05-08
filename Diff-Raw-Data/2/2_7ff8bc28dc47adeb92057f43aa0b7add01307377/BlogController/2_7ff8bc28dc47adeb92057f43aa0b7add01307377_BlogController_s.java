 package com.bajoneando.lnramirez.web.controllers;
 
 import com.bajoneando.lnramirez.blog.BlogEntry;
 import com.bajoneando.lnramirez.blog.services.BlogEntryRepository;
 import java.util.Date;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author lrmonterosa
  */
 @Controller
 @RequestMapping("/blog/")
 public class BlogController {
     
     @RequestMapping(method=RequestMethod.GET)
     public ModelAndView init() {
        Sort sort = new Sort("date");
         List<BlogEntry> blogEntries = blogEntryRepository.findAll(sort);
         ModelAndView modelAndView = new ModelAndView("/blog/list");
         modelAndView.addObject(blogEntries);
         modelAndView.addObject("blogEntry", new BlogEntry());
         return modelAndView;
     }
     
     @RequestMapping(method=RequestMethod.POST)
     public String addEntry(@ModelAttribute(value="blogEntry") BlogEntry blogEntry) {
         blogEntry.setDate(new Date());
         blogEntryRepository.save(blogEntry);
         return "redirect:/blog/";
     }
         
     @Autowired
     private BlogEntryRepository blogEntryRepository;
     
 }
