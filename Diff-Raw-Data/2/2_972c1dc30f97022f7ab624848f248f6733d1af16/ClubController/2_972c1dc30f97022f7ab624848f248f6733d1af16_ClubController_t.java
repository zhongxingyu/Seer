 package com.genfersco.sepbas.web.controller;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.genfersco.sepbas.app.services.ServicesManager;
 import com.genfersco.sepbas.domain.model.Club;
 import com.genfersco.sepbas.web.constants.WebAppConstants;
 import com.genfersco.sepbas.web.form.ClubForm;
 import com.genfersco.sepbas.web.json.DefaultJSONResponse;
 import com.genfersco.sepbas.web.json.JSONResponse;
 
 @Controller
 public class ClubController extends BaseController {
 
 	@Autowired
 	private ServicesManager servicesManager;
 
 	@RequestMapping(value = "/clubes/add", method = RequestMethod.GET)
 	public String agregarClub(ModelMap map) {
 		// shows view
 		map.addAttribute("clubForm", new ClubForm());
 		return WebAppConstants.AGREGAR_CLUB;
 	}
 
 	@RequestMapping(value = "/clubes/add", method = RequestMethod.POST)
 	public String agregarClub(ModelMap map, @ModelAttribute ClubForm clubForm) {
 		// TODO: some validation here
 		Club club = new Club();
 		club.setNombre(clubForm.getNombre());
 		servicesManager.addClub(club);
 		map.addAttribute("clubForm", clubForm);
 		return "redirect:/clubes/list";
 	}
 
 	@RequestMapping(value = "/clubes/list", method = RequestMethod.GET)
 	public String getClubes(Model map) {
 		List<Club> clubes = servicesManager.getClubes();
 		map.addAttribute("clubes", clubes);
 		return WebAppConstants.CLUBES;
 	}
 	
 	@RequestMapping(value = "/clubes/delete.json", method = RequestMethod.POST)
 	public @ResponseBody
 	JSONResponse jsonDeleteJugador(@RequestParam("id") String id, Model map) {
 		JSONResponse response = null;
 		try {
 			if (StringUtils.hasText(id)) {
 				Integer iId = Integer.parseInt(id);
				servicesManager.deleteClub(iId);
 				response = new DefaultJSONResponse("OK", "Club eliminado");
 			} else {
 				response = new DefaultJSONResponse("ERROR", "Id club vacio");
 			}
 		} catch (NumberFormatException nfe) {
 			response = new DefaultJSONResponse("ERROR",
 					"Id jugador no es un entero");
 		}
 		return response;
 	}
 
 }
