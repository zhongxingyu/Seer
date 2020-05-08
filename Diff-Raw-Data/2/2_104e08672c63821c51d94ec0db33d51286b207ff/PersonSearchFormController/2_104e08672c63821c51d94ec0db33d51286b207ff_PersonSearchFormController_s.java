 package com.mpower.controller;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import com.mpower.domain.Person;
 import com.mpower.service.PersonService;
 import com.mpower.web.common.SessionUtils;
 
 public class PersonSearchFormController extends SimpleFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     private PersonService personService;
 
     public void setPersonService(PersonService personService) {
         this.personService = personService;
     }
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws ServletException {
         logger.info("**** in formBackingObject");
 
         Person p = new Person();
         p.setSite(SessionUtils.lookupUser(request).getSite());
         return p;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
         logger.info("**** in onSubmit()");
         Person person = (Person) command;
         BeanWrapper bw = new BeanWrapperImpl(person);
         Map<String, Object> params = new HashMap<String, Object>();
         Enumeration<String> enu = request.getParameterNames();
         while (enu.hasMoreElements()) {
             String param = enu.nextElement();
             if (StringUtils.trimToNull(request.getParameter(param)) != null) {
                 try {
                    String obj = (String) bw.getPropertyValue(param);
                     params.put(param, obj);
                 } catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
 
         List<Person> personList = personService.readPersons(SessionUtils.lookupUser(request).getSite().getId(), params);
         // TODO: Adding errors.getModel() to our ModelAndView is a "hack" to allow our
         // form to post results back to the same page. We need to get the
         // command from errors and then add our search results to the model.
         ModelAndView mav = new ModelAndView(getSuccessView(), errors.getModel());
         mav.addObject("personList", personList);
         mav.addObject("personListSize", personList.size());
         return mav;
     }
 }
