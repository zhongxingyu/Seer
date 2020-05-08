 package com.thoughtworks.controller;
 
 import com.thoughtworks.model.Answer;
 import com.thoughtworks.model.Customer;
 import com.thoughtworks.model.Menu;
 import com.thoughtworks.model.Question;
 import com.thoughtworks.repository.CustomerRepository;
 import com.thoughtworks.repository.QuestionRepository;
 import org.neo4j.graphdb.Node;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.springframework.web.bind.annotation.RequestMethod.*;
 
 @Controller
 @RequestMapping("/customers")
 public class CustomerController {
 
     @RequestMapping(method = GET)
     public String customers(Model model) throws Exception {
         CustomerRepository customerRepository = new CustomerRepository();
         List<Customer> customers = customerRepository.getCustomers();
 
         model.addAttribute("customers",customers);
         return "customers";
     }
 
     @RequestMapping(value = "{customername}/questions", method = GET)
     public String getNextQuestion(@PathVariable String customername, Model model) throws Exception {
         QuestionRepository questionRepository = new QuestionRepository();
         CustomerRepository customerRepository = new CustomerRepository();
         Node customerNode = customerRepository.getCustomer(customername);
         List<Question> listOfNextQuestions = questionRepository.getNextQuestions(customerNode);
         model.addAttribute("nextQuestions", listOfNextQuestions);
         model.addAttribute("customername",customername);
 
         return "customerQuestion";
     }
 
     @RequestMapping(value = "{customername}/questions/{questionId}", method = GET)
     public String getAnswers(@PathVariable String customername, @PathVariable String questionId, Model model) throws Exception {
         QuestionRepository questionRepository = new QuestionRepository();
         Node theQuestion = questionRepository.getQuestionById(Long.parseLong(questionId));
        String questionText = theQuestion.getProperty("name").toString();
 
         List<Answer> listOfAnswers = questionRepository.getAnswers(questionText);
         model.addAttribute("answers",listOfAnswers);
         model.addAttribute("customername",customername);
         model.addAttribute("questionText",questionText);
         model.addAttribute("questionId",questionId);
 
         return "answers";
     }
 
     @RequestMapping(value = "answer", method = POST)
     public String answer(@RequestParam String answerId, @RequestParam String customername, @RequestParam String questionId, Model model) throws Exception {
         CustomerRepository customerRepository = new CustomerRepository();
         List<Answer> listOfAnswers = new ArrayList<Answer>();
         Answer answer = new Answer();
         answer.setId(Long.parseLong(answerId));
         listOfAnswers.add(answer);
 
         customerRepository.answerQuestion(customername,Long.parseLong(questionId),listOfAnswers);
 
         Node customer = customerRepository.getCustomer(customername);
         List<Menu> customerPersonalisedMenu = customerRepository.getPersonalisedMenu(customer);
         model.addAttribute("personalisedMenus", customerPersonalisedMenu);
         model.addAttribute("customername",customername);
 
         return "customerMenu";
     }
 
     @RequestMapping(value = "menu/{customername}", method = GET)
     public String getPersonalisedMenu(@PathVariable String customername, Model model) throws Exception {
         CustomerRepository customerRepository = new CustomerRepository();
         Node customer = customerRepository.getCustomer(customername);
         List<Menu> customerPersonalisedMenu = customerRepository.getPersonalisedMenu(customer);
         model.addAttribute("personalisedMenus", customerPersonalisedMenu);
         model.addAttribute("customername",customername);
 
         return "customerMenu";
     }
 }
