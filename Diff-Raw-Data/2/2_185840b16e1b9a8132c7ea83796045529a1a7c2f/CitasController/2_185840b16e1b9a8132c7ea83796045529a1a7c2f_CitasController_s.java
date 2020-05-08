 package org.upsam.sypweb.controller.citas;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.upsam.sypweb.controller.AbstractController;
 import org.upsam.sypweb.domain.citas.ejb.AppointmentHistoryBeanLocal;
 import org.upsam.sypweb.domain.citas.ejb.CitacionServiceBeanLocal;
 import org.upsam.sypweb.domain.user.UserDTO;
 import org.upsam.sypweb.facade.MujerServiceFacade;
 
 @Controller
 public class CitasController extends AbstractController {
 	/**
 	 * 
 	 */
 	private CitacionServiceBeanLocal citacionService;
 
 	/**
 	 * 
 	 * @param mujerServiceFacade
 	 * @param citacionService
 	 */
 	@Inject
 	public CitasController(MujerServiceFacade mujerServiceFacade, CitacionServiceBeanLocal citacionService) {
 		super(mujerServiceFacade);
		this.mujerServiceFacade = mujerServiceFacade;
 	}
 
 	@RequestMapping("/cita/list")
 	public String listarCitas(@RequestParam(required = true) Long mujerId, Model model) {
 		referenceData(mujerId, model);
 		return "listarCitas";
 	}
 	
 	@RequestMapping("/cita/history")
 	public String historicoCitas(Model model, HttpSession session) {
 	AppointmentHistoryBeanLocal citasHistory = (AppointmentHistoryBeanLocal) session.getAttribute("history");
 		model.addAttribute("history", citasHistory.getAppointments());
 		return "historicoCitas";
 	}
 
 	@RequestMapping({"/", "/cita/inicio"})
 	public String inicio(Model model, HttpSession session) {
 		model.addAttribute("citasParaHoy", citacionService.getDailyAppointment(((UserDTO) session.getAttribute("user")).getUserName()));
 		return "inicio";
 	}
 }
