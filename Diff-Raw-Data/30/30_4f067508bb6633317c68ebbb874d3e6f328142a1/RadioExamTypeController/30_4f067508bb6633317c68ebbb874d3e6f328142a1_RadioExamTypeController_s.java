 package org.openmrs.module.radiotest.web.controller;
 
 import java.util.List;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.radiotest.RadioExamType;
 import org.openmrs.module.radiotest.api.RadioExamService;
 import org.openmrs.module.radiotest.propertyeditor.RadioExamTypePropertyEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class RadioExamTypeController {
 	
 	private final String EXAM_TYPE_FORM = "/module/radiotest/examTypeForm";
 	
 	@InitBinder
 	public void initBinder(WebRequest request, WebDataBinder binder){
 		binder.registerCustomEditor(RadioExamType.class, new RadioExamTypePropertyEditor());
 	}
 	
 	@RequestMapping(value = EXAM_TYPE_FORM, method = RequestMethod.GET)
 	public void showExamTypeForm(Model model){
 		// populate model with existing exam types
 	}
 	
 	@ModelAttribute("examType")
 	public RadioExamType getExamType(){
 		return new RadioExamType();
 	}
 	
 	@ModelAttribute("examTypes")
 	public List<RadioExamType> getExamTypes(){
 		return Context.getService(RadioExamService.class).getAllExamTypes(true);
 	}
 	
 	@RequestMapping(value = EXAM_TYPE_FORM, method = RequestMethod.POST)
 	public ModelAndView saveExamType(ModelMap modelMap, @ModelAttribute RadioExamType examType){
 		Context.getService(RadioExamService.class).saveExamType(examType);
 		
 		return new ModelAndView("redirect:" + EXAM_TYPE_FORM + ".htm");
 	}
 	
 	@RequestMapping(value = "/module/radiotest/nullExamType", method = RequestMethod.POST)
 	public ModelAndView voidExamType(@RequestParam("eid") RadioExamType type, @RequestParam("action") String action){
 		RadioExamService es = Context.getService(RadioExamService.class);
 		if (action.equalsIgnoreCase("void")){
 			type.setVoided(!type.getVoided());
 			es.saveExamType(type);
 		} else if (action.equalsIgnoreCase("delete")){
 			es.deleteExamType(type);
 		}
 		
 		return new ModelAndView("redirect:" + EXAM_TYPE_FORM + ".htm");
 	}
 }
