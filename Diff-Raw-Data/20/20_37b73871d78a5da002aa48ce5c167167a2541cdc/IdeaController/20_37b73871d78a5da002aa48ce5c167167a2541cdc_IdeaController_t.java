 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.domain.*;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.io.IOException;
 
 @Controller
 public class IdeaController
 {
     @RequestMapping(value = "createIdea", method = RequestMethod.GET)
     public ModelAndView showCreateIdeaPage()
     {
         ModelAndView modelAndView = new ModelAndView("example/createIdea");
         return modelAndView;
     }
     @RequestMapping(value = "createIdea", method = RequestMethod.POST)
     public ModelAndView createIdea(@ModelAttribute() IdeaForm ideaForm) throws IOException {
         ModelAndView modelAndView = new ModelAndView("example/createIdea");
         if(ideaForm.isValid())
         {
             Collaborator collaborator = new Collaborator(new RequestSender("http://10.10.15.130:3000"));
             //Idea idea = collaborator.createIdea(Integer.parseInt(ideaForm.getSectionId()),ideaForm.getIdeaText());
             Idea idea = collaborator.createIdea(3,ideaForm.getIdeaText());
            modelAndView.addObject("ideaForm", ideaForm);
             modelAndView.addObject("idea",idea);
         }
         return modelAndView;
     }
 }
