 package at.ac.tuwien.dse.fairsurgeries.web.actors;
 
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import at.ac.tuwien.dse.fairsurgeries.dto.ReservationDTO;
 import at.ac.tuwien.dse.fairsurgeries.general.Constants;
 import at.ac.tuwien.dse.fairsurgeries.service.LogEntryService;
 import at.ac.tuwien.dse.fairsurgeries.service.OPSlotService;
 
 @Controller
 @RequestMapping("/actors/doctor")
 public class ActorDoctorController {
 	
 	@Autowired
 	private OPSlotService opSlotService;
 	@Autowired
 	private LogEntryService logEntryService;
 
 	@RequestMapping(value="/reservations", method = RequestMethod.GET, produces = "text/html")
 	public String manageReservation(Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorDoctorController . manageReservations()");
 		//uiModel.addAttribute("heading", "List all feckin Slots");
 		//uiModel.addAttribute("slots", opSlotService.findAllOPSlots());
		ReservationDTO reservation = new ReservationDTO(null, null, null, 27., null, null);
		uiModel.addAttribute("reservation", reservation);
 		return "actors/doctor/reservations";
 	}
 	
 	/*@RequestMapping(value="/slots", method = RequestMethod.GET, produces = "text/html")
 	public void listSlots(HttpServletResponse response) throws Exception {
 		PrintWriter out = response.getWriter();
 
 		out.println("feckin shit");
 	}*/
 }
