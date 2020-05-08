 package org.motechproject.carereporting.web.controller;
 
 import org.motechproject.carereporting.domain.FormEntity;
 import org.motechproject.carereporting.domain.views.BaseView;
 import org.motechproject.carereporting.domain.views.IndicatorJsonView;
 import org.motechproject.carereporting.exception.CareApiRuntimeException;
 import org.motechproject.carereporting.initializers.ComputedFieldEntityInitializer;
 import org.motechproject.carereporting.service.ComputedFieldService;
 import org.motechproject.carereporting.service.FormsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import javax.validation.Valid;
 
 @Controller
 @RequestMapping("/api/forms")
 public class FormsController extends BaseController {
 
     @Autowired
     private FormsService formsService;
 
     @Autowired
     private ComputedFieldService computedFieldService;
 
     @Autowired
     private ComputedFieldEntityInitializer formEntityInitializer;
 
     @RequestMapping(value = "/list", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getAllFormsWithDto() {
         return this.writeAsString(IndicatorJsonView.ListFormNames.class, formsService.getAllFormsFromDto());
     }
 
     @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getAllForms() {
         return this.writeAsString(IndicatorJsonView.ListFormNames.class, formsService.getAllForms());
     }
 
     @RequestMapping(value = "/{formId}", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getForm(@PathVariable Integer formId) {
        return writeAsString(BaseView.class,
                 formsService.getFormByIdWithFields(formId, "computedFields", "fields"));
     }
 
     @RequestMapping(value = "/{formId}", method = RequestMethod.PUT)
     @ResponseStatus(HttpStatus.OK)
     public void updateForm(@PathVariable Integer formId, @RequestBody @Valid FormEntity form, BindingResult result) {
         if (result.hasErrors()) {
             throw new CareApiRuntimeException(result.getFieldErrors());
         }
 
         formsService.updateForm(form);
     }
 
     @RequestMapping(value = "/{formId}/computedfields", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getComputedFieldsByFormId(@PathVariable Integer formId) {
         return this.writeAsString(BaseView.class, computedFieldService.getComputedFieldsByFormId(formId));
     }
 
     @RequestMapping(value = "/{formId}/computedfields/all", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getAllComputedFieldsByFormId(@PathVariable Integer formId) {
         return this.writeAsString(BaseView.class, computedFieldService.getAllComputedFieldsByFormId(formId));
     }
 
     @RequestMapping(value = "/reload", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     public void reloadFormEntitiesFromDB() {
         formEntityInitializer.loadFormsFromDB();
     }
 }
