 package com.forum.web.controller;
 
 
 import com.forum.domain.Advice;
 import com.forum.domain.Question;
 import com.forum.service.QuestionService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.validation.Valid;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 @Controller
 public class QuestionController {
 
     private QuestionService questionService;
     private Logger logger = Logger.getLogger(QuestionController.class.getName());
 
 
     @Autowired
     public QuestionController(QuestionService questionService) {
         this.questionService = questionService;
     }
 
     @RequestMapping(value = "/postQuestion", method = RequestMethod.GET)
     public String postQuestion(Map model) {
         Question question = new Question();
         model.put("question", question);
         return "postQuestion";
     }
 
     @RequestMapping(value = "/showPostedQuestion", method = RequestMethod.POST)
     public String showPostedQuestion(@Valid Question question, BindingResult result, Map model){
 
         logger.info("question = " + question.toString());
         logger.info("result = " + result.toString());
         logger.info("model = " + model.toString());
 
         if(result.hasErrors()) {
             return "postQuestion";
         }
 
         questionService.createQuestion(question);
         List latestQuestionList = questionService.latestQuestion("1","1");
         Question latestQuestion = (Question)latestQuestionList.get(0);
         Integer questionId = latestQuestion.getId();
 
         return "redirect:/question/view/" + questionId;
     }
 
     @RequestMapping(value = "/question/view/{questionId}", method = RequestMethod.GET)
     public ModelAndView viewQuestionDetail(@PathVariable Integer questionId) {
         Question question = questionService.getById(questionId);
         ModelAndView modelAndView = new ModelAndView("questionDetail");
         modelAndView.addObject("questionId", question.getId());
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
         Advice advice = new Advice(0, null, "");
         modelAndView.addObject("advice", advice);
         return modelAndView;
     }
 
     @RequestMapping(value = "/question/like/{questionId}", method = RequestMethod.POST)
     @ResponseBody
     public String likeQuestion(@PathVariable Integer questionId, @RequestParam Map<String, Integer> params) {
         questionService.addLikesById(questionId);
         Question question = questionService.getById(questionId);
         return "(" + question.getLikes() + ") Likes";
     }
 
     @RequestMapping(value = "/question/dislike/{questionId}", method = RequestMethod.POST)
     @ResponseBody
     public String dislikeQuestion(@PathVariable Integer questionId, @RequestParam Map<String, Integer> params) {
         questionService.addDisLikesById(questionId);
         Question question = questionService.getById(questionId);
         return "(" + question.getDislikes() + ") Dislikes";
     }
 
     @RequestMapping(value = "/question/flag/{questionId}", method = RequestMethod.POST)
     @ResponseBody
     public String flagQuestion(@PathVariable Integer questionId, @RequestParam Map<String, Integer> params) {
         questionService.addFlagsByID(questionId);
         Question question = questionService.getById(questionId);
         return "(" + question.getFlags() + ") Flags";
     }
 
 }
