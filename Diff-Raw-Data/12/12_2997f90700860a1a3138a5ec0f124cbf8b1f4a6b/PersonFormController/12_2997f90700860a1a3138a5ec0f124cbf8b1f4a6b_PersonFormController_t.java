 package com.mpower.controller;
 
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.propertyeditors.StringTrimmerEditor;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import com.mpower.domain.Person;
 import com.mpower.domain.customization.FieldDefinition;
 import com.mpower.service.PersonService;
 import com.mpower.service.SessionServiceImpl;
 import com.mpower.service.SiteService;
 import com.mpower.service.exception.PersonValidationException;
 import com.mpower.type.PageType;
 
 public class PersonFormController extends SimpleFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     private PersonService personService;
 
     public void setPersonService(PersonService personService) {
         this.personService = personService;
     }
 
     private SiteService siteService;
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     @Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
         binder.registerCustomEditor(Person.class, new PersonEditor(personService));
     }
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws ServletException {
         String personId = request.getParameter("personId");
         personId = (personId == null) ? request.getParameter("id") : personId;
         Person person = null;
         if (personId == null) {
             person = personService.createDefaultPerson(SessionServiceImpl.lookupUserSiteName());
         } else {
             person = personService.readPersonById(new Long(personId));
         }
         if (isFormSubmission(request)) {
             Map<String, String> fieldLabelMap = siteService.readFieldLabels(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(getCommandName()), SessionServiceImpl.lookupUserRoles(), request.getLocale());
             person.setFieldLabelMap(fieldLabelMap);
 
             Map<String, Object> valueMap = siteService.readFieldValues(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(getCommandName()), SessionServiceImpl.lookupUserRoles(), person);
             person.setFieldValueMap(valueMap);
  
             Map<String, FieldDefinition> typeMap = siteService.readFieldTypes(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(getCommandName()), SessionServiceImpl.lookupUserRoles());
             person.setFieldTypeMap(typeMap);
         }
         return person;
     }
 
     @Override
    public ModelAndView onSubmit(Object command, BindException errors) throws Exception {
         Person p = (Person) command;
         logger.info("**** p's first name is: " + p.getFirstName());
         
        ModelAndView mav = super.onSubmit(command, errors);
         boolean saved = true;
         Person current = null;
         try {
             current = personService.maintainPerson(p);
         } catch (PersonValidationException e) {
            saved = false;
         	current = p;
             e.createMessages(errors);
         }
 
         mav.addObject("person", current);
         mav.addObject("saved", saved);
         mav.addObject("id", current.getId());
         return mav;
     }
 }
