 package controller;
 
 import static org.springframework.data.mongodb.core.query.Criteria.where;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.MongoDbFactory;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import domain.Patient;
 
 /**
  * Handles requests for the public access login.
  * 
  * @author Taylor
  */
 @Controller
 @RequestMapping(value = "/login")
 public class LoginController {
 
 	@Autowired(required = false)
 	MongoDbFactory mongoDbFactory;
 
 	@Autowired(required = false)
 	MongoTemplate mongoTemplate;
 
	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String login(Model model) {
 		return "login";
 	}
 
 	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
 	public String login(Model model, @RequestParam("username") String username,
 			@RequestParam("password") String password) {
 
 		// Retrieve the person with the received username
 		Patient patient = mongoTemplate.findOne(new Query(where("username").is(username)), Patient.class);
 
 		// Check type of user
 		if (patient != null) {
 			// TODO check password
			return "redirect:/patient/id/" + patient.getId();
 		}
 
 		return "redirect:/public";
 	}
 
 }
