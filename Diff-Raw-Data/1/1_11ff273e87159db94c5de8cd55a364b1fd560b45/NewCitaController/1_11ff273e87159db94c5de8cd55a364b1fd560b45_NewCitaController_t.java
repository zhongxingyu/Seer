 package org.upsam.sypweb.controller.citas;
 
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 import org.upsam.sypweb.domain.citas.CitacionService;
 import org.upsam.sypweb.domain.mujer.Mujer;
 import org.upsam.sypweb.domain.servicio.Servicio;
 import org.upsam.sypweb.domain.servicio.ServicioService;
 import org.upsam.sypweb.facade.MujerServiceFacade;
 import org.upsam.sypweb.view.CitacionView;
 
 @Controller
 @SessionAttributes({"citacion", "citaciones"})
 public class NewCitaController {
 	/**
 	 * Servicio de fachada para la gestión de {@link Mujer}
 	 */
 	private MujerServiceFacade mujerServiceFacade;
 	/**
 	 * servicio de gestión de la entidad {@link Servicio}
 	 */
 	private ServicioService servicioService;
 	/**
 	 * 
 	 */
 	private CitacionService citacionService;
 
 	/**
 	 * @param mujerService
 	 */
 	@Inject
 	public NewCitaController(MujerServiceFacade mujerServiceFacade, ServicioService servicioService, CitacionService citacionService) {
 		super();
 		this.mujerServiceFacade = mujerServiceFacade;
 		this.servicioService = servicioService;		
 		this.citacionService = citacionService;
 	}
 
 	@ModelAttribute("citacion")
 	public CitacionView modelAttribute() {
 		return new CitacionView();
 	}
 	
 	@RequestMapping(value = "/cita/new", method = RequestMethod.GET)
 	public String newCitaForm(@RequestParam(required = true) Long mujerId, Model model, HttpSession session) {
 		session.setAttribute("mujerId", mujerId);
 		referenceData(mujerId, model);
 		return "newCita";
 	}
 	
 	@RequestMapping(value = "/cita/new", method = RequestMethod.POST)
 	public String submitServiceSelection(@ModelAttribute("citacion") CitacionView citacion, @RequestParam(required = false) Long mujerId, Model model, HttpSession session) {
 		mujerId = (Long) (mujerId != null ? mujerId : session.getAttribute("mujerId"));
 		referenceData(mujerId, model);
		model.addAttribute("citaciones", citacionService.getCitasDisponibles(citacion.getServicioId()));
 		return "newCita";
 	}
 	
 	@RequestMapping(value = "/cita/new/finish", method = RequestMethod.POST)
 	@SuppressWarnings("unchecked")
 	public String submitCitaSelection(@ModelAttribute("citacion") CitacionView citacion, @RequestParam(required = false) Long mujerId, @RequestParam Integer citasel, Model model, HttpSession session, SessionStatus status) {
 		mujerId = (Long) (mujerId != null ? mujerId : session.getAttribute("mujerId"));
 		List<CitacionView> citaciones = (List<CitacionView>) session.getAttribute("citaciones");
 		if (citaciones != null && citasel != null) {
 			CitacionView citaSelected = citaciones.get(citasel);
 			citacionService.citar(mujerId, citaSelected);
 			status.setComplete();
 			return "redirect:/listarCitas?mujerId=" + mujerId;
 			
 		} else {
 			referenceData(mujerId, model);
 			model.addAttribute("citaciones", citacionService.getCitasDisponibles(citacion.getServicioId()));
 		}
 		return "newCita";
 	}
 
 	private void referenceData(Long mujerId, Model model) {
 		model.addAttribute("details", mujerServiceFacade.find(mujerId));
 		model.addAttribute("listServicios", servicioService.getServicesBy(null));
 	}
 }
