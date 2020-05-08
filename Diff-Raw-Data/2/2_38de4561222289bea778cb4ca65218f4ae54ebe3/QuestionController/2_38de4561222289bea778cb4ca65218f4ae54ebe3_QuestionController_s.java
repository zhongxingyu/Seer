 package com.forum.web.controller;
 
 
 import com.forum.domain.Question;
 import com.forum.service.QuestionService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 @Controller
 public class QuestionController {
 
     private QuestionService questionService;
 
     @Autowired
     public QuestionController(QuestionService questionService) {
         this.questionService = questionService;
     }
 
     @RequestMapping(value = "/postQuestion", method = RequestMethod.GET)
     public ModelAndView postQuestion() {
         return new ModelAndView("postQuestion");
     }
 
     @RequestMapping(value = "/showPostedQuestion", method = RequestMethod.POST)
     public ModelAndView showPostedQuestion(@RequestParam Map<String, String> params){
         questionService.createQuestion(params);
         ModelAndView modelAndView = new ModelAndView("showPostedQuestion");
         modelAndView.addObject("questionTitle",params.get("questionTitle"));
         modelAndView.addObject("questionDescription",params.get("editor"));
         return modelAndView;
     }
 
     @RequestMapping(value = "/question/view/{questionId}", method = RequestMethod.GET)
     public ModelAndView viewQuestionDetail(@PathVariable Integer questionId) {
         Question question = questionService.getById(questionId);
        ModelAndView modelAndView = new ModelAndView("questiondetail");
         modelAndView.addObject("questionTitle", question.getTitle());
         modelAndView.addObject("questionDescription", question.getDescription());
         modelAndView.addObject("username", question.getUser().getName());
         modelAndView.addObject("dateCreatedAt", new SimpleDateFormat("dd/MM/yyyy").format(question.getCreatedAt()));
         modelAndView.addObject("timeCreatedAt", new SimpleDateFormat("kk:mm:ss").format(question.getCreatedAt()));
         modelAndView.addObject("likes", question.getLikes());
         modelAndView.addObject("dislikes", question.getDislikes());
         modelAndView.addObject("views", question.getViews());
         modelAndView.addObject("flags", question.getFlags());
 
 
         return modelAndView;
     }
 }
