 package com.carlos.projects.billing.ui.controllers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import com.carlos.projects.billing.domain.FileUpload;
 
 /**
  * @author Carlos Fernandez
  *
  * @date 19 Jul 2009
  * 
  * Controller to import the document with the list of components
  *
  */
 public class ImportComponentsController extends SimpleFormController {
 	
 	public ImportComponentsController() {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(java.lang.Object)
 	 */
 	@Override
 	protected ModelAndView onSubmit(Object command)
 			throws Exception {
 		//Cast the bean
 		FileUpload bean = (FileUpload) command;
 		
 		MultipartFile file = bean.getFile();
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("file", file);
		return new ModelAndView("storeComponents", model);
 	}
 
 }
