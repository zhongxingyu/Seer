 package com.forum.web.controller;
 
 
 import com.forum.domain.Question;
import com.forum.domain.User;
 import com.forum.service.QuestionService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Date;
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
         ModelAndView modelAndView=new ModelAndView("postQuestion");
         return modelAndView;
     }
 
     @RequestMapping(value = "/showPostedQuestion", method = RequestMethod.POST)
     public ModelAndView showPostedQuestion(@RequestParam Map<String, String> params){
         Date date = new Date();
         Timestamp timestamp = new Timestamp(date.getTime());
 
        Question question = new Question(params.get("questionTitle"), params.get("questionDescription"), new User(), timestamp);
         questionService.createQuestion(question);
         ModelAndView modelAndView = new ModelAndView("showPostedQuestion");
         modelAndView.addObject("questionTitle",params.get("questionTitle"));
         modelAndView.addObject("questionDescription",params.get("questionDescription"));
         return modelAndView;
     }
 
     @RequestMapping(value = "/question/view/{questionId}", method = RequestMethod.GET)
     public ModelAndView viewQuestionDetail(@PathVariable Integer questionId) {
         Question question = questionService.getById(questionId);
         ModelAndView modelAndView = new ModelAndView("questionDetail");
         modelAndView.addObject("questionTitle", question.getTitle());
         modelAndView.addObject("questionDescription", question.getDescription());
         modelAndView.addObject("username", question.getUser().getName());
         modelAndView.addObject("dateCreatedAt", new SimpleDateFormat("MMMM dd,yyyy").format(question.getCreatedAt()));
         modelAndView.addObject("timeCreatedAt", new SimpleDateFormat("hh:mm:ss a").format(question.getCreatedAt()));
         modelAndView.addObject("likes", question.getLikes());
         modelAndView.addObject("dislikes", question.getDislikes());
         modelAndView.addObject("views", question.getViews());
         modelAndView.addObject("flags", question.getFlags());
         modelAndView.addObject("responses", question.getResponses());
         return modelAndView;
     }
 
 }
