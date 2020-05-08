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
  * Handles requst for the patient view
  * 
  * @author Taylor
  */
 @Controller
 @RequestMapping(value = "/patient")
 public class PatientController {
 
 	@Autowired(required = false)
 	MongoDbFactory mongoDbFactory;
 
 	@Autowired(required = false)
 	MongoTemplate mongoTemplate;
 
	@RequestMapping(value = "", method = RequestMethod.GET)
 	public String patient(Model model, @RequestParam("id") String id) {
 
 		// TODO
 		// show user info and reservation details
 
 		Patient patient = mongoTemplate.findOne(new Query(where("id").is(id)), Patient.class);
 		model.addAttribute("patient", patient);
 
 		return "patient";
 	}
 
 }
