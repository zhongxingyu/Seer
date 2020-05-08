 package it.geosolutions.nrl.mvc;
 
 import it.geosolutions.nrl.model.AgrometDescriptor;
 import it.geosolutions.nrl.service.AgrometDescriptorService;
 import it.geosolutions.nrl.utils.ControllerUtils;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class AgrometController {
 	@Autowired
 	AgrometDescriptorService agrometDescriptorService;
 	
	@RequestMapping(value="/agromet/", method = RequestMethod.GET)
 	public String handleGet(ModelMap model) {
 		List<AgrometDescriptor> cds =agrometDescriptorService.getAll();
 		model.addAttribute("factors",cds);
 		ControllerUtils.setCommonModel(model);
		model.addAttribute("context", "factors");
 		return "template";
  
 	}
 	@RequestMapping(value = "/agromet/create", method = RequestMethod.GET)
 	public String createCrop(ModelMap model) {
 	    AgrometDescriptor a= new AgrometDescriptor();
 		
 		model.addAttribute("factor",a);
 		model.addAttribute("context","create");
 		return "factors/create";
 
 	}
 	@RequestMapping(value = "/agromet/create", method = RequestMethod.POST)
 	public String createCrop( ModelMap model,@ModelAttribute("factor") AgrometDescriptor a) {
 		try{
 		    agrometDescriptorService.persist(a);
 		}catch(Exception e){
 			model.addAttribute("messageType", "error");
 			model.addAttribute("notLocalizedMessage", "Couldn't save factor");
 			return "common/messages";
 		}
 		model.addAttribute("messageType", "success");
 		model.addAttribute("notLocalizedMessage", "Factor created successfully");
 
 		return "common/messages";
 
 	}
 	@RequestMapping(value = "/agromet/edit/{id}", method = RequestMethod.GET)
 	public String edit(@PathVariable(value = "id") String id, ModelMap model) {
 	    AgrometDescriptor ad = agrometDescriptorService.get(id);
 		model.addAttribute("factor", ad);
 
 		return "factors/create";
 
 	}
 	
 	@RequestMapping(value = "/agromet/edit/{id}", method = RequestMethod.POST)
 	public String edit(@PathVariable(value = "id") String id,@ModelAttribute("factor") AgrometDescriptor ad, ModelMap model) {
 
 		try {
 		    ad.setFactor(id);
 			agrometDescriptorService.update(ad);
 		} catch (Exception e) {
 			model.addAttribute("messageType", "error");
 			model.addAttribute("notLocalizedMessage", "Couldn't save factor");
 			return "common/messages";
 
 		}
 		model.addAttribute("messageType", "success");
 		model.addAttribute("notLocalizedMessage", "Factor Saved successfully");
 
 		return "common/messages";
 
 	}
 
 	@RequestMapping(value = "/agromet/delete/{id}", method = RequestMethod.GET)
 	public String delete(@PathVariable(value = "id") String id, ModelMap model) {
 	    AgrometDescriptor ad = agrometDescriptorService.get(id);
 		model.addAttribute("crop", ad);
 		model.addAttribute("resource",ad.getFactor());
 
 		return "snipplets/modal/confirmdelete";
 
 	}
 	@RequestMapping(value = "/agromet/delete/{id}", method = RequestMethod.POST)
 	public String delete(@PathVariable(value = "id") String  id,@ModelAttribute("factor") AgrometController crop, ModelMap model) {
 
 		
 		try {
 		    agrometDescriptorService.delete(id);
 		} catch (Exception e) {
 			model.addAttribute("messageType", "error");
 			model.addAttribute("notLocalizedMessage", "Couldn't delete Factor");
 			return "common/messages";
 
 		}
 		model.addAttribute("messageType", "success");
 		model.addAttribute("notLocalizedMessage", "Crop Saved successfully");
 
 		return "common/messages";
 
 	}
 	
 }
