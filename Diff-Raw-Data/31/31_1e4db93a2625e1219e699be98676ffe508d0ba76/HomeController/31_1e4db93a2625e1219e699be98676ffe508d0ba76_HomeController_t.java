 package controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.servlet.http.HttpServletResponse;
 
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.dao.DataAccessException;
 import org.springframework.data.mongodb.MongoDbFactory;
 import org.springframework.data.mongodb.core.CollectionCallback;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.mongodb.DBCollection;
 import com.mongodb.MongoException;
 
 import dse_domain.domain.Doctor;
 import dse_domain.domain.Patient;
 import dse_domain.domain.Person;
 

 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {	
 	
 	@Autowired(required = false)
 	MongoDbFactory mongoDbFactory;
 
 	@Autowired(required = false)
 	MongoTemplate mongoTemplate;
 
 	@Autowired(required = false)
 	@Qualifier(value = "serviceProperties")
 	Properties serviceProperties;
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Model model) {
 		List<String> services = new ArrayList<String>();
 		if (mongoDbFactory != null) {
 			services.add("MongoDB: " + mongoDbFactory.getDb().getMongo().getAddress());
 		}
 
 		List<Doctor> doctors = mongoTemplate.findAll(Doctor.class);
 		List<Patient> patients = mongoTemplate.findAll(Patient.class);
 
 		model.addAttribute("doctors", doctors);
 		model.addAttribute("patients", patients);
 		model.addAttribute("services", services);
 		model.addAttribute("serviceProperties", getServicePropertiesAsList());
 
 		String environmentName = (System.getenv("VCAP_APPLICATION") != null) ? "Cloud" : "Local";
 		model.addAttribute("environmentName", environmentName);
 		return "home";
 	}
 
 	/**
 	 * TODO
 	 * 
 	 * @param firstname
 	 * @param lastname
 	 * @param lat
 	 * @param lon
 	 * @return
 	 */
 	@RequestMapping(value = "/patient/create/{firstname}/{lastname}/{lat}/{lon}", method = RequestMethod.GET)
 	public String patientCreate(@PathVariable("firstname") String firstname, @PathVariable("lastname") String lastname,
 			@PathVariable("lat") double lat, @PathVariable("lon") double lon) {
 		double[] coordinates = new double[] { lat, lon };
 		Patient patient = new Patient(firstname, lastname, coordinates);
 		mongoTemplate.save(patient);
 		return "redirect:/";
 	}
 
 	@RequestMapping("/env")
 	public void env(HttpServletResponse response) throws IOException {
 		response.setContentType("text/plain");
 		PrintWriter out = response.getWriter();
 		out.println("System Properties:");
 		for (Map.Entry<Object, Object> property : System.getProperties().entrySet()) {
 			out.println(property.getKey() + ": " + property.getValue());
 		}
 		out.println();
 		out.println("System Environment:");
 		for (Map.Entry<String, String> envvar : System.getenv().entrySet()) {
 			out.println(envvar.getKey() + ": " + envvar.getValue());
 		}
 	}
 
 	@RequestMapping("/service-properties")
 	public void services(HttpServletResponse response) throws IOException {
 		response.setContentType("text/plain");
 		PrintWriter out = response.getWriter();
 		if (serviceProperties != null) {
 			out.println("Cloud Service Properties:");
 			// Map envMap = System.getenv();
 			for (Object key : serviceProperties.keySet()) {
 				out.println(key + ": " + serviceProperties.get(key));
 			}
 		} else {
 			out.println("No Cloud Service Properties found.  Check configuration file for <cloud:service-properties/> element");
 		}
 		out.println(")<a href=\"/\">Return to previous page.</a>");
 		out.println();
 	}
 
 	private List<String> getServicePropertiesAsList() {
 		List<String> propList = new ArrayList<String>();
 		if (serviceProperties != null) {
 			for (Object key : serviceProperties.keySet()) {
 				propList.add(key + ": " + serviceProperties.get(key));
 			}
 		}
 		return propList;
 	}
 
 	@RequestMapping("/deleteAll")
 	public void deleteAll(HttpServletResponse response) throws IOException {
 		response.setContentType("text/plain");
 		PrintWriter out = response.getWriter();
 		long count = mongoTemplate.execute(Person.class, new CollectionCallback<Long>() {
 			@Override
 			public Long doInCollection(DBCollection collection) throws MongoException, DataAccessException {
 				return collection.count();
 			}
 		});
 		out.println("Deleted " + count + " entries");
 		mongoTemplate.dropCollection(Person.class);
 	}
 
 }
