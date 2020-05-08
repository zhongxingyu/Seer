 package com.forum.web.controller;
 
 import com.forum.domain.Question;
 import com.forum.service.QuestionService;
 import com.google.gson.Gson;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 @Controller
 public class  HomeController {
 
     private QuestionService questionService;
     private Gson gson;
 
     @Autowired
     public HomeController(QuestionService questionService) {
         gson = new Gson();
 
         this.questionService = questionService;
     }
 
     @RequestMapping(value = "/", method = RequestMethod.POST)
     @ResponseBody
     public String loadMoreQuestions(@RequestParam String pageNum,
                                     @RequestParam String pageSize) {
         List<Question> questionList = questionService.latestQuestion(pageNum, pageSize);
         String result = gson.toJson(questionList);
         return result;
     }
 
     @RequestMapping("/")
     public ModelAndView activityView() {
         ModelAndView homeModelAndView = new ModelAndView("home");
         return homeModelAndView;
     }
 
 }
