 package com.forum.controller;
 
 import com.forum.repository.PostQuestion;
 import com.forum.repository.QuestionValidation;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
 
 @Controller
 public class PostQuestionController {
 
     @Autowired
     private QuestionValidation questionValidation;
 
     @Autowired
     private PostQuestion post;
 
     @RequestMapping(value = "/postQuestion", method = RequestMethod.GET)
     public void postQuestion() {

     }
 
     @RequestMapping(value = "/postedQuestion", method = RequestMethod.POST)
     public ModelAndView postedQuestion(@RequestParam("textareas") String textarea) {
         ModelAndView mv;
         if (questionValidation.isQuestionValid(textarea)) {
             post.insert(textarea);
             mv = new ModelAndView("activityWall");
         } else {
             mv = new ModelAndView("postQuestion");
             mv.addObject("error", "Question length must be of at least 20 characters, and should not contain all spaces");
             mv.addObject("askedQuestion", textarea);
         }
         return mv;
     }

 }
