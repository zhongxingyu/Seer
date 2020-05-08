 package ee.itcollege.i377.iseseisev3;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
import ee.itcollege.i377.iseseisev3.IncidentSimulatorServiceImpl;

 @Controller
 public class IncidentController {
 
 	@Autowired
 	private IncidentSimulatorServiceImpl incidentSimulatorServiceImpl;
 	
 	@RequestMapping("/listAll")
 	public String allIncidents(Model model){
 		model.addAttribute("incidents", incidentSimulatorServiceImpl.getUnresolvedIncidents());
 		return "listAll";
 	}
 	@RequestMapping(value="/reportIncident", method=RequestMethod.GET)
 	public String reportIncident(ModelMap model){
 		model.addAttribute("incident", new Incident());
 		return "reportIncident";
 	}
 	
 	@RequestMapping(value="/reportIncident", method=RequestMethod.POST)
	public String reportIncidents(@ModelAttribute Incident incident, Model model){
 		model.addAttribute("msg", incident.getLocation());
 		return "showIncident";
 	}
 }
