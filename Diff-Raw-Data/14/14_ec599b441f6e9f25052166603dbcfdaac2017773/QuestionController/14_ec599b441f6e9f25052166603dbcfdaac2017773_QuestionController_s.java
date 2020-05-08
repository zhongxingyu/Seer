 package com.forum.controller;
 
 import com.forum.repository.Advice;
 import com.forum.repository.AdviceRepository;
 import com.forum.repository.QuestionRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 
 @Controller
 public class QuestionController {
     @Autowired
     private QuestionRepository questionRepository;
     @Autowired
     private AdviceRepository adviceRepository;
 
     @RequestMapping(value = "/question_details", method = RequestMethod.GET)
     public ModelAndView questionDetails(@RequestParam("questionId") String questionId) {
         List<Advice> answers;
         ModelAndView questionDetail = new ModelAndView("questionDetails");
         String question=questionRepository.getQuestionById(Integer.parseInt(questionId));
         answers= adviceRepository.getAdvices(Integer.parseInt(questionId));
         questionDetail.addObject("question",question);
         for (Advice answer : answers) {
             System.out.println(answer.getAdvice());
         }
         questionDetail.addObject("answers", answers);
         questionDetail.addObject("noOfAnswer",answers.size());
         return questionDetail;
     }
 }

//for(Advice answer : (List<Advice>)request.getAttribute("answers")) { %>
//<div class="answer" color="blue"> <b>
//<% out.print("Answer&nbsp" + i + " :");%> &nbsp &nbsp</b>
//<% out.println(answer.getAdvice + "<br/><br/>");%>
//</div>
//<% i++; } %>
