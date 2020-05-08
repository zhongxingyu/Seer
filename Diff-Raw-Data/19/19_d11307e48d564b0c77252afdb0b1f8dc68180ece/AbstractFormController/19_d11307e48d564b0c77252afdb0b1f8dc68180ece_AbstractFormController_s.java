 package com.rcc.web.controller;
 
 import com.rcc.web.FlashUtils;
 import com.rcc.web.View;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public abstract class AbstractFormController
     extends org.springframework.web.servlet.mvc.AbstractFormController
 {
     private static final Log log = LogFactory.getLog(AbstractFormController.class);
 
     private String content;
 
     public void setContent(String content) {
         this.content = content;
     }
 
     protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response,
             BindException errors)
         throws Exception
     {
         // The view will be set with the LayoutHandlerInterceptor
         return this.showForm(request, errors, null);
     }
 
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors)
         throws Exception
     {
         Map map = new HashMap();
         map.put("content", this.content);
         this.referenceData(map, request, command, errors);
         return map;
     }
 
     protected void referenceData(Map map, HttpServletRequest request, Object command, Errors errors)
         throws Exception
     {
         this.referenceData(map);
     }
 
     protected void referenceData(Map map) throws Exception { ; }
 
     protected ModelAndView processFormSubmission(HttpServletRequest request,
             HttpServletResponse response, Object command, BindException errors)
         throws Exception
     {
         if (errors.hasErrors()) {
             FlashUtils.errorCode("validation.fail", request);
             return this.showForm(request, response, errors);
         }
 
         return internalProcessFormSubmission(request, response, command, errors);
     }
 
     protected abstract ModelAndView internalProcessFormSubmission(HttpServletRequest request,
             HttpServletResponse response, Object command, BindException errors)
         throws Exception;
 }
