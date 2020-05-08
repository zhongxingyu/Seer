 package com.forum.web.controller;
 
 import com.forum.domain.Tag;
 import com.forum.service.TagService;
 import com.google.gson.Gson;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 @Controller
 public class TagController {
     private TagService tagService;
 
     @Autowired
     public TagController(TagService tagService) {
         this.tagService = tagService;
     }
    @RequestMapping(value="/postQuestion/getTagsByTerm/{term}", method = RequestMethod.GET)
     @ResponseBody
     public String getTagsByTerm(@PathVariable String term) {
 
         List<Tag> listOfTags = tagService.getTagsByTerm(term);
 
         return new Gson().toJson(listOfTags);
     }
 
     @RequestMapping(value="/tags", method = RequestMethod.GET)
     @ResponseBody
     public String getAllTags() {
         List<Tag> tags = tagService.getAllTags();
         return new Gson().toJson(tags);
     }
 
     @RequestMapping(value = "/tag/{tagName}", method = RequestMethod.GET)
     public ModelAndView showQuestionsWithThisTag(@PathVariable String tagName) {
         ModelAndView questionWithThisTagView = new ModelAndView("questionWithTag");
         questionWithThisTagView.addObject("tagName", tagName);
 
         return questionWithThisTagView;
     }
 }
