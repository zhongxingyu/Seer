 package com.example.controller;
 
 import java.util.Calendar;
 import java.util.Map;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.ws.Response;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import com.example.model.Person;
 import com.example.model.Task;
 import com.example.model.User;
 import com.example.service.PersonService;
 import com.example.service.TaskService;
 import com.example.service.UserService;
 
 @Controller
 public class MainController {
 
 	@Autowired
 	private PersonService personService;
 
 	@Autowired
 	private UserService userService;
 
 	@Autowired
 	private TaskService taskService;
 
 	private static final Logger logger = LoggerFactory.getLogger(MainController.class);
 
 	private User sam = null;
 
 	@RequestMapping("/")
 	public String listPeople(Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) {
 
 		logger.info("request is empty? " + request.getAttribute("message"));
 		
 		Cookie newCookie = null;
 		Cookie[] myCookie = request.getCookies();
 		boolean foundACookie = false;
 		
		for (Cookie cookie : myCookie) {
			if (cookie.getName().equals("session"));{
				foundACookie = true;
				return "redirect:/tasks/";
 			}
 		}
 		logger.info("Cookie found:"+foundACookie);
 		if (!foundACookie){
 			newCookie = new Cookie("session", "Sam");
 			newCookie.setMaxAge(24*60*60);
 			response.addCookie(newCookie);
 		}	
 		
 		// create a test user if new session 
 		if (null == sam) {
 			sam = new User("sam", "secr3t");
 			userService.addUser(sam);
 			map.put("user", sam);
 		} else {
 			logger.info("message: " + map.get("message"));
 		}
 		return "index";
 	}
 
 	@RequestMapping(value = "/validate", method = RequestMethod.POST)
 	public String validate(@ModelAttribute("user") User user,
 			BindingResult result, HttpServletRequest request) {
 
 		logger.info("user recieved is: " + user.getUserName() + " with passwd: " + user.getPasswd());
 		
 		if (userService.validateUser(user)) {
 			return "redirect:/tasks/";
 		} else {
 			logger.info("login failed");
 			request.setAttribute("message", "Invalid login credentials");
 			return "forward:/";
 		}
 	}
 
 /* handle tasks here */
 	
 	@RequestMapping(value = "/tasks/add", method=RequestMethod.POST)
 	public String addTask(@ModelAttribute("task") Task task ){
 		
 		logger.info("creating a task, with:"+task.getTaskName()+" "+task.getCompleteBy());
 		
 		task.setCreatedDate(Calendar.getInstance().getTime());
 		task.setCompleted(false);
 		if (null == task.getCompleteBy())
 			task.setCompleteBy(Calendar.getInstance().getTime());
 		
 		taskService.createTask(task);		
 		
 		return "redirect:/tasks/";
 	}
 	
 	@RequestMapping("/tasks/delete/{taskId}")
 	public String deleteTask(@PathVariable("taskId") Integer taskId){
 		logger.info("deleting task with id:"+taskId);
 		taskService.deleteTask(taskId);
 		return "redirect:/tasks/";
 	}
 
 	@RequestMapping("/tasks/")
 	public String listTasks(Map<String, Object> map){
 		logger.info("handling path /tasks/ ");
 		
 		map.put("task", new Task());
 		map.put("taskList", taskService.listAll());
 		
 		return "tasks";
 	}
 
 /* people methods */
 	
 	@RequestMapping(value = "/people/add", method = RequestMethod.POST)
 	public String addPerson(@ModelAttribute("person") Person person,
 			BindingResult result) {
 
 		personService.addPerson(person);
 
 		logger.info("added person");
 		return "redirect:/people/";
 	}
 
 	@RequestMapping("/people/delete/{personId}")
 	public String deletePerson(@PathVariable("personId") Integer personId) {
 
 		personService.removePerson(personId);
 		logger.info("removed person");
 		return "redirect:/people/";
 	}
 
 	@RequestMapping("/people/")
 	public String handlePeople(Map<String, Object> map) {
 
 		map.put("person", new Person());
 		map.put("peopleList", personService.listPeople());
 
 		logger.info("handled loading of /people/");
 		return "people";
 	}
 
 }
