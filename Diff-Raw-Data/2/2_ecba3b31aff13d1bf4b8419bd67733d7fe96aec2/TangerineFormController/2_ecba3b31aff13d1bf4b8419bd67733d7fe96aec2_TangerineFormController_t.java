 package com.mpower.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import com.mpower.domain.Viewable;
 import com.mpower.domain.customization.FieldDefinition;
 import com.mpower.service.PersonService;
 import com.mpower.service.SiteService;
 import com.mpower.service.impl.SessionServiceImpl;
 import com.mpower.type.PageType;
 import com.mpower.util.StringConstants;
 
 public abstract class TangerineFormController extends SimpleFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     protected PersonService personService;
     protected SiteService siteService;    
     protected String pageType;    
 
     public void setPersonService(PersonService personService) {
         this.personService = personService;
     }
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     /**
      * The default page type is the commandName.  Override for specific page types
      * @return pageType, the commandName
      */
     protected String getPageType() {
         return StringUtils.hasText(pageType) ? pageType: getCommandName();
     }
 
     public void setPageType(String pageType) {
         this.pageType = pageType;
     }
 
     public Long getIdAsLong(HttpServletRequest request, String id) {
         if (logger.isDebugEnabled()) {
             logger.debug("getIdAsLong: id = " + id);
         }
         String paramId = request.getParameter(id);
         if (StringUtils.hasText(paramId)) {
             return Long.valueOf(request.getParameter(id));
         }
         return null;
     }
 
     protected Long getPersonId(HttpServletRequest request) {
         return this.getIdAsLong(request, StringConstants.PERSON_ID);
     }
 
     protected String getPersonIdString(HttpServletRequest request) {
         return request.getParameter(StringConstants.PERSON_ID);
     }
 
     @Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         super.initBinder(request, binder);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("MM/dd/yyyy"), true)); // TODO: custom date format
         binder.registerCustomEditor(String.class, new NoneStringTrimmerEditor(true));
     }
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws ServletException {
         request.setAttribute(StringConstants.COMMAND_OBJECT, this.getCommandName()); // To be used by input.jsp to check for errors
         Viewable viewable = findViewable(request);
         this.createFieldMaps(request, viewable);
         return viewable;
     }
 
     protected void createFieldMaps(HttpServletRequest request, Viewable viewable) {
         if (isFormSubmission(request)) {
             Map<String, String> fieldLabelMap = siteService.readFieldLabels(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(this.getPageType()), SessionServiceImpl.lookupUserRoles(), request.getLocale());
             viewable.setFieldLabelMap(fieldLabelMap);
 
             Map<String, Object> valueMap = siteService.readFieldValues(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(this.getPageType()), SessionServiceImpl.lookupUserRoles(), viewable);
             viewable.setFieldValueMap(valueMap);
 
            Map<String, FieldDefinition> typeMap = siteService.readFieldTypes(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(this.getPageType()), SessionServiceImpl.lookupUserRoles());
             viewable.setFieldTypeMap(typeMap);
         }
     }
     
     protected String appendSaved(String url) {
         return new StringBuilder(url).append("&").append(StringConstants.SAVED_EQUALS_TRUE).toString();
     }
 
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
         return new ModelAndView(appendSaved(new StringBuilder().append(getSuccessView()).append("?").append(StringConstants.PERSON_ID).append("=").append(getPersonId(request)).toString()));
     }
 
     protected abstract Viewable findViewable(HttpServletRequest request);
 }
