 package at.ac.tuwien.dse.fairsurgeries.web.actors;
 
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import at.ac.tuwien.dse.fairsurgeries.general.Constants;
 import at.ac.tuwien.dse.fairsurgeries.service.LogEntryService;
 import at.ac.tuwien.dse.fairsurgeries.service.OPSlotService;
 import at.ac.tuwien.dse.fairsurgeries.service.NotificationService;
 
 @Controller
 @RequestMapping("/actors/patient")
 public class ActorPatientController {
 
 	@Autowired
 	private OPSlotService opSlotService;
 	
 	@Autowired
	private NotifiactionService notificationService;
 	
 	@Autowired
 	private LogEntryService logEntryService;
 	
 	@RequestMapping(value="/slots", method = RequestMethod.GET, produces = "text/html")
 	public String listSlots(Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorPatient . listSlots()");
 		uiModel.addAttribute("heading", "List all Slots (private)");
 		uiModel.addAttribute("slots", opSlotService.findAllOPSlots());
 		return "actors/public/slots";
 	}
 	
 	@RequestMapping(value="/notifications", method = RequestMethod.GET, produces = "text/html")
 	public String listNotifications(Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorPatient . listNotifications()");
 		uiModel.addAttribute("heading", "List all Notifications (private)");
		uiModel.addAttribute("notifications", notificationService.findAllOPSlots());
 		return "actors/public/notifications";
 	}
 	
 }
