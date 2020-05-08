 package org.openmrs.module.radiotest.web.controller;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.radiotest.RadioCategory;
 import org.openmrs.module.radiotest.RadioPatient;
 import org.openmrs.module.radiotest.api.RadioPatientService;
 import org.openmrs.module.radiotest.model.RadioPatientModel;
 import org.openmrs.module.radiotest.propertyeditor.RadioCategoryPropertyEditor;
 import org.openmrs.module.radiotest.propertyeditor.RadioStringPropertyEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class RadioPatientFormController {
 
 	private final String PATIENT_FORM = "/module/radiotest/patientForm";
 	private final String PATIENT_PROFILE = "/module/radiotest/patientProfile";
 	private final String EDIT_PATIENT = "/module/radiotest/editPatient";
 	
 	@InitBinder
 	public void initBinder(WebRequest request, WebDataBinder binder){
 		binder.registerCustomEditor(String.class, new RadioStringPropertyEditor());
 		binder.registerCustomEditor(RadioCategory.class, new RadioCategoryPropertyEditor());
 	}
 	
 	@RequestMapping(value = PATIENT_FORM, method = RequestMethod.GET)
 	public void showForm(HttpSession session, ModelMap model){
 		session.removeAttribute("patient");
 	}
 	
 	@ModelAttribute("categories")
 	public List<RadioCategory> getCategories(){
 		return Context.getService(RadioPatientService.class).getAllCategories();
 	}
 	
 	@ModelAttribute("patientModel")
 	public RadioPatientModel getPatientModel(){
 		return new RadioPatientModel();
 	}
 	
 	@RequestMapping(value = PATIENT_PROFILE, method = RequestMethod.GET)
 	public void showProfile(HttpSession session, ModelMap model){
 		RadioPatient patient = (RadioPatient) session.getAttribute("patient");
 		patient = Context.getService(RadioPatientService.class).updatePatient(patient);
 		
 		model.addAttribute("patient", patient);
 	}
 	
 	@RequestMapping(value = {PATIENT_FORM, EDIT_PATIENT}, method = RequestMethod.POST)
 	public ModelAndView getPatientFromForm(@ModelAttribute("patientModel") RadioPatientModel pm, 
 												HttpServletRequest request, HttpSession session, ModelMap model){
 		RadioPatientService ps = Context.getService(RadioPatientService.class);
 		RadioPatient patient = pm.getFullPatient();
 		try {
 			patient.setUpdateDate(new Date());
			if(patient.getId() != null){
				patient = ps.updatePatient(patient);
			}
 			session.setAttribute("patient", ps.savePatient(patient));
 		} catch (Exception ex) {
 			System.out.println("Exception!");
 		}
 		
 		ModelAndView page;
 		String currPage = request.getServletPath();
 		
 		if(currPage.contains(EDIT_PATIENT)){
 			page = new ModelAndView("redirect:/module/radiotest/patientProfile.htm");
 		} else {
 			page = new ModelAndView("redirect:/module/radiotest/transExamForm.htm");
 		}
 		
 		return page;
 	}
 	
 	@RequestMapping(value = EDIT_PATIENT, method = RequestMethod.GET)
 	public ModelAndView editPatient(HttpSession session, ModelMap model){
 		RadioPatient patient = (RadioPatient) session.getAttribute("patient");
 		patient = Context.getService(RadioPatientService.class).updatePatient(patient);
 		model.addAttribute("patientModel", new RadioPatientModel(patient));
 		
 		return new ModelAndView(PATIENT_FORM, model);
 	}
 }
  
