 package com.bajoneando.lnramirez.web.controllers;
 
 import com.bajoneando.lnramirez.blog.BlogEntry;
 import com.bajoneando.lnramirez.blog.services.BlogEntryRepository;
 import com.petebevin.markdown.MarkdownProcessor;
 import java.util.Date;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.web.PageableDefaults;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author lrmonterosa
  */
 @Controller
 @RequestMapping("/blog")
 public class BlogController {
     
     @RequestMapping(method=RequestMethod.GET)
     public ModelAndView list(@PageableDefaults(pageNumber=0, value=5) Pageable pageableRequest) {
         Pageable pageable = new PageRequest(pageableRequest.getPageNumber(), pageableRequest.getPageSize(), Sort.Direction.DESC, "date");
         Page<BlogEntry> blogEntriesPage = blogEntryRepository.findAll(pageable);
         ModelAndView modelAndView = new ModelAndView("/blog/list");
         modelAndView.addObject("blogEntryPage", blogEntriesPage);
         modelAndView.addObject("blogEntry", new BlogEntry());
         return modelAndView;
     }
     
     @RequestMapping(method=RequestMethod.POST)
     public String addEntry(@ModelAttribute(value="blogEntry") BlogEntry blogEntry) {
         blogEntry.setDate(new Date());
         blogEntry.setPrintableHtml(markdownProcessor.markdown(blogEntry.getArticle()));
         blogEntryRepository.save(blogEntry);
         return "redirect:/blog";
     }
     
     @RequestMapping(value="/update", method=RequestMethod.PUT, headers="Accept=application/json")
     @ResponseStatus(HttpStatus.OK)
     public void updateEntry(@RequestBody BlogEntry blogEntry) {
         blogEntry.setDate(new Date());
         blogEntry.setPrintableHtml(markdownProcessor.markdown(blogEntry.getArticle()));
         blogEntryRepository.save(blogEntry);
     }
     
     @RequestMapping(value="/single/{id}", method=RequestMethod.GET, headers="Accept=application/json")
     @ResponseBody
     public BlogEntry getEntry(@PathVariable("id") String id) {
         return blogEntryRepository.findOne(id);
     }
         
     @Autowired
     private BlogEntryRepository blogEntryRepository;
     
     @Autowired
     private MarkdownProcessor markdownProcessor;
     
 }
