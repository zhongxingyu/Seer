 package com.forum.controller;
 
 import com.forum.repository.Advice;
 import com.forum.repository.AdviceRepository;
 import com.forum.repository.Question;
 import com.forum.repository.QuestionRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpRequest;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.ArrayList;
 import java.util.List;
 
 
 @Controller
 public class QuestionController {
     private SecurityContext context;
     @Autowired
     private QuestionRepository questionRepository;
     @Autowired
     private AdviceRepository adviceRepository;
 
 
     @RequestMapping(value = "/question_details", method = RequestMethod.GET)
     public ModelAndView questionDetails(@RequestParam("questionId") String questionId) {
         List<Advice> answers;
         ModelAndView questionDetail = new ModelAndView("questionDetails");
         Question question=questionRepository.getQuestionById(Integer.parseInt(questionId));
         answers= adviceRepository.getAdvices(Integer.parseInt(questionId));
         questionDetail.addObject("question",question.getQuestion());
         questionDetail.addObject("question_user",question.getUserName());
         questionDetail.addObject("answers", answers);
         questionDetail.addObject("noOfAnswer",answers.size());
         return questionDetail;
     }
 
     @RequestMapping(value = "/questions_advised", method = RequestMethod.GET)
     public ModelAndView getAdvisedQuestions(){
 
         context = SecurityContextHolder.getContext();
         Object principal = context.getAuthentication().getPrincipal();
         List<Question> questions = questionRepository.getQuestions(adviceRepository.getQuestionIdAnsweredBy(principal.toString()));
         ModelAndView myAnswers=new ModelAndView("myAnswers");
         myAnswers.addObject("questions",questions);
         return myAnswers;
     }
 }
