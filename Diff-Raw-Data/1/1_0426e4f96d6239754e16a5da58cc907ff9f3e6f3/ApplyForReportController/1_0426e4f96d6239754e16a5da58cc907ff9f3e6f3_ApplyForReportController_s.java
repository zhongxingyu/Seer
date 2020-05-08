 package it.sevenbits.conferences.web.controller;
 
 import it.sevenbits.conferences.web.form.ApplyForReportForm;
 import it.sevenbits.conferences.web.form.JsonResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 @Controller
 public class ApplyForReportController {
 
     @Autowired
     @Qualifier("applyForReportValidator")
     private Validator validator;
 
     @RequestMapping(value = "/apply-for-report", method = RequestMethod.GET)
     public ModelAndView showForm() {
 
         ModelAndView modelAndView = new ModelAndView("apply-for-report");
         return modelAndView;
     }
 
     @RequestMapping(value = "/apply-for-report", method = RequestMethod.POST)
     @ResponseBody
     public JsonResponse submitForm(@ModelAttribute(value = "applyForReportForm") ApplyForReportForm applyForReportForm, BindingResult bindingResult) {
 
         JsonResponse response = new JsonResponse();
         validator.validate(applyForReportForm, bindingResult);
 
         if (bindingResult.hasErrors()) {
 
             response.setStatus("FAIL");
             Map<String, String> errors = new HashMap<>();
             for (FieldError fieldError: bindingResult.getFieldErrors()) {
                 if (!errors.containsKey(fieldError.getField())) {
                     errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                 }
             }
             errors.put("message", "Форма заполнена неверно.");
             response.setResult(errors);
         } else {
 
             response.setStatus("SUCCESS");
             response.setResult(Collections.singletonMap("message", "Заявка отправлена."));
         }
 
         return response;
     }
 }
