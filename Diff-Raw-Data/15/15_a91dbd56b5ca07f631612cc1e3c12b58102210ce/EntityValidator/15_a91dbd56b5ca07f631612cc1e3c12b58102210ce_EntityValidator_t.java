 package com.mpower.controller.validator;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
import com.mpower.domain.CustomField;
 import com.mpower.domain.Customizable;
 import com.mpower.domain.Gift;
 import com.mpower.domain.Person;
 import com.mpower.service.SessionServiceImpl;
 import com.mpower.service.SiteService;
 import com.mpower.type.PageType;
 
 public class EntityValidator implements Validator {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     private PageType pageType;
 
     private SiteService siteService;
 
     public void setPageType(PageType pageType) {
         this.pageType = pageType;
     }
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public boolean supports(Class clazz) {
         return Person.class.equals(clazz) || Gift.class.equals(clazz);
     }
 
     @Override
     public void validate(Object target, Errors errors) {
         Customizable customizableEntity = (Customizable) target;
 
         Set<String> requiredFieldSet = siteService.readRequiredFields(SessionServiceImpl.lookupUserSiteName(), pageType, SessionServiceImpl.lookupUserRoles());
 
         Map<String, String> fieldLabelMap = siteService.readFieldLabels(SessionServiceImpl.lookupUserSiteName(), pageType, SessionServiceImpl.lookupUserRoles(), null);
 
         Map<String, String> fieldValidationMap = siteService.readFieldValidations(SessionServiceImpl.lookupUserSiteName(), pageType, SessionServiceImpl.lookupUserRoles());
 
         // used as a cache to prevent having to use reflection if the value has already been read
         Map<String, Object> fieldValueMap = new HashMap<String, Object>();
 
         // used to know that a field already has an error, so don't add another
         Set<String> errorSet = new HashSet<String>();
 
         // validate required fields
         if (requiredFieldSet != null) {
             for (String key : requiredFieldSet) {
                 if (errorSet.contains(key)) {
                     continue;
                 }
                 Object property = fieldValueMap.get(key);
                 if (property == null) {
                     BeanWrapper beanWrapper = new BeanWrapperImpl(customizableEntity);
                     property = beanWrapper.getPropertyValue(key);
                    if (property instanceof CustomField) {
                        property = ((CustomField) property).getValue();
                    }
                     fieldValueMap.put(key, property);
                 }
                 String propertyString = property == null ? "" : property.toString();
                 if (StringUtils.isEmpty(propertyString) && !errorSet.contains(key)) {
                     errors.rejectValue(key, "fieldRequiredFailure", new String[] { fieldLabelMap.get(key) }, "no message provided for the validation error: fieldRequiredFailure");
                     errorSet.add(key);
                 }
             }
         }
 
         // validate regex
         if (fieldValidationMap != null) {
             for (String key : fieldValidationMap.keySet()) {
                 if (errorSet.contains(key)) {
                     continue;
                 }
                 Object property = fieldValueMap.get(key);
                 if (property == null) {
                     BeanWrapper beanWrapper = new BeanWrapperImpl(customizableEntity);
                     property = beanWrapper.getPropertyValue(key);
                    if (property instanceof CustomField) {
                        property = ((CustomField) property).getValue();
                    }
                     fieldValueMap.put(key, property);
                 }
                 String propertyString = property == null ? "" : property.toString();
                 String regex = fieldValidationMap.get(key);
                 boolean matches = propertyString.matches(regex);
                 if (!matches && !errorSet.contains(key)) {
                     // String defaultMessage = messageService.lookupMessage(SessionServiceImpl., MessageResourceType.FIELD_VALIDATION, "fieldValidationFailure", null);
                     errors.rejectValue(key, "fieldValidationFailure", new String[] { fieldLabelMap.get(key), propertyString }, "no message provided for the validation error: fieldValidationFailure");
                     // errors.reject("fieldValidationFailure", new String[] { key, propertyString }, "no message provided for the validation error: fieldValidationFailure");
                 }
             }
         }
     }
 }
