 package com.uade.pfi.web.controller.crud;
 
 import java.util.Collection;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.uade.pfi.core.beans.Transport;
 import com.uade.pfi.core.service.TransportCrudService;
 
 /**
  * CRUD Controller for transports 
  * 
  * coordinates example: -34.582062,-58.436648
  * 
  */
 @Controller
 public class TransportCrudController {
 
 	@Autowired
 	private TransportCrudService service;
 
	@ModelAttribute
	public Transport newRequest(@RequestParam(required=false) Integer id) {
 		return (id != null ? service.getTransportBy(Integer.toString(id)) : new Transport());
 	}
 
	@RequestMapping("/transport/CrudRequestForm.htm")
	public String showEmptyForm() {
		return "transportCrudRequestForm";
 	}
 
	@RequestMapping(value="/transport/form", method=RequestMethod.GET)
	public void form() {}

 	@RequestMapping(value="/transport/form.htm", method=RequestMethod.POST)
	public Transport form(Transport transport, Model model) {
 		Transport result = service.add(transport);
 		model.addAttribute("statusMessageKey", "Success");
		return result;
 	}
 
	@RequestMapping(value="/transport/search.htm", method=RequestMethod.GET)
 	public @ModelAttribute("transport") Collection<Transport> search() {
 		return service.getAllTransports();
 	}
 }
