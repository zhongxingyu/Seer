 package it.geosolutions.nrl.mvc;
 
 import it.geosolutions.nrl.model.CropDescriptor;
 import it.geosolutions.nrl.service.CropDescriptorService;
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
 public class CropController {
 	@Autowired
 	CropDescriptorService cropDescriptorService;
 	
	@RequestMapping(value="/crops/", method = RequestMethod.GET)
 	public String handleGet(ModelMap model) {
 		List<CropDescriptor> cds =cropDescriptorService.getAll();
 		model.addAttribute("crops",cds);
 		ControllerUtils.setCommonModel(model);
		model.addAttribute("context", "crops");
 		return "template";
  
 	}
 	@RequestMapping(value = "/crops/create", method = RequestMethod.GET)
 	public String createCrop(ModelMap model) {
 		CropDescriptor c= new CropDescriptor();
 		
 		model.addAttribute("crop",c);
 		model.addAttribute("context","create");
 		return "crops/create";
 
 	}
 	@RequestMapping(value = "/crops/create", method = RequestMethod.POST)
 	public String createCrop( ModelMap model,@ModelAttribute("crop") CropDescriptor c) {
 		try{
 			cropDescriptorService.persist(c);
 		}catch(Exception e){
 			model.addAttribute("messageType", "error");
 			model.addAttribute("notLocalizedMessage", "Couldn't save Crop");
 			return "common/messages";
 		}
 		model.addAttribute("messageType", "success");
 		model.addAttribute("notLocalizedMessage", "Crop created successfully");
 
 		return "common/messages";
 
 	}
 	@RequestMapping(value = "/crops/edit/{id}", method = RequestMethod.GET)
 	public String edit(@PathVariable(value = "id") String id, ModelMap model) {
 		CropDescriptor cd = cropDescriptorService.get(id);
 		model.addAttribute("crop", cd);
 
 		return "crops/create";
 
 	}
 	
 	@RequestMapping(value = "/crops/edit/{id}", method = RequestMethod.POST)
 	public String edit(@PathVariable(value = "id") String id,@ModelAttribute("crop") CropDescriptor crop, ModelMap model) {
 
 		try {
 			crop.setId(id);
 			cropDescriptorService.update(crop);
 		} catch (Exception e) {
 			model.addAttribute("messageType", "error");
 			model.addAttribute("notLocalizedMessage", "Couldn't save Crop");
 			return "common/messages";
 
 		}
 		model.addAttribute("messageType", "success");
 		model.addAttribute("notLocalizedMessage", "Crop Saved successfully");
 
 		return "common/messages";
 
 	}
 
 	@RequestMapping(value = "/crops/delete/{id}", method = RequestMethod.GET)
 	public String delete(@PathVariable(value = "id") String id, ModelMap model) {
 		CropDescriptor cd = cropDescriptorService.get(id);
 		model.addAttribute("crop", cd);
 		model.addAttribute("resource",cd.getId());
 
 		return "snipplets/modal/confirmdelete";
 
 	}
 	@RequestMapping(value = "/crops/delete/{id}", method = RequestMethod.POST)
 	public String delete(@PathVariable(value = "id") String  id,@ModelAttribute("crop") CropController crop, ModelMap model) {
 
 		
 		try {
 			cropDescriptorService.delete(id);
 		} catch (Exception e) {
 			model.addAttribute("messageType", "error");
 			model.addAttribute("notLocalizedMessage", "Couldn't delete Crop");
 			return "common/messages";
 
 		}
 		model.addAttribute("messageType", "success");
 		model.addAttribute("notLocalizedMessage", "Crop Saved successfully");
 
 		return "common/messages";
 
 	}
 	
 }
