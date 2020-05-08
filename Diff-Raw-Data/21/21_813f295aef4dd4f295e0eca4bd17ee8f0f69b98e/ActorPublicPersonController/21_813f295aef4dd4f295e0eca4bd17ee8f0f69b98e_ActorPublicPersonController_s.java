 package at.ac.tuwien.dse.fairsurgeries.web.actors;
 
 import java.io.PrintWriter;
 import java.util.Arrays;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import at.ac.tuwien.dse.fairsurgeries.domain.OPSlot;
 import at.ac.tuwien.dse.fairsurgeries.domain.SurgeryType;
 import at.ac.tuwien.dse.fairsurgeries.general.Constants;
 import at.ac.tuwien.dse.fairsurgeries.service.LogEntryService;
 import at.ac.tuwien.dse.fairsurgeries.service.OPSlotService;
 
 @Controller
 @RequestMapping("/actors/public")
 public class ActorPublicPersonController {
 	
 	@Autowired
 	private OPSlotService opSlotService;
 	@Autowired
 	private LogEntryService logEntryService;
 
 	@RequestMapping(value="/slots", method = RequestMethod.GET, produces = "text/html")
 	public String listSlots(Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorPublicPersonController . listSlots()");
 		uiModel.addAttribute("opSlots", opSlotService.findAllOPSlots());
 		uiModel.addAttribute("surgeryTypes", Arrays.asList(SurgeryType.values()));
 		uiModel.addAttribute("opSlotExample", new OPSlot());
 		return "actors/public/slots";
 	}
 	
 	@RequestMapping(value="/slots", method = RequestMethod.POST, produces = "text/html")
 	public String listFilteredSlots(@ModelAttribute OPSlot opSlot, Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorPublicPersonController . listFilteredSlots() example=" + opSlot);
		uiModel.addAttribute("opSlotExample", opSlotService.findByExample(opSlot));
		//uiModel.addAttribute("opSlots", opSlotService.findAllOPSlots());
 		uiModel.addAttribute("surgeryTypes", Arrays.asList(SurgeryType.values()));
 		uiModel.addAttribute("opSlotExample", opSlot);
 		return "actors/public/slots";
 	}
 	
 }
