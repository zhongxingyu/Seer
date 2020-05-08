 package org.tomis.mvc.controller;
 
 import java.beans.Introspector;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 import javax.validation.groups.Default;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tomis.mvc.controller.binder.Binder;
 import org.tomis.mvc.controller.helper.RequestHelper;
 import org.tomis.mvc.model.dto.SessionForm;
 import org.tomis.mvc.model.dto.ClassFromString;
 import org.tomis.mvc.model.dto.Dto;
 import org.tomis.mvc.model.dto.ValidationErrors;
 import org.tomis.mvc.model.entity.PersistentEntity;
 import org.tomis.mvc.model.service.jpa.EditableService;
 import org.tomis.mvc.validation.ErrorAppender;
 
 /**
  * Project: tomis-mvc
  * @since 12.03.2010
  * @author Dan Persa
  */
 public abstract class FormController<CommandObject extends Dto> extends PageFragmentController {
 
     private static Logger logger = LoggerFactory.getLogger(FormController.class);
     public static final String COMMAND_OBJECT_NAME_PARAM = "command-object-name";
     public static final String SUBMIT_PARAMETER_NAME = "submit-parameter-name";
     public static final String IS_SESSION_FORM_PARAM = "is-session-form";
     public static final String REDIRECT_ON_SUCCESS_PARAM = "redirect-on-success";
     private String submitParameterName;
     private String commandObjectName;
     private String redirectOnSuccessUrl;
     private boolean sessionform;
 
     @Override
     public void init() throws ServletException {
         super.init();
         setCommandObjectName(getServletConfig().getInitParameter(COMMAND_OBJECT_NAME_PARAM));
         setSubmitParameterName(getServletConfig().getInitParameter(SUBMIT_PARAMETER_NAME));
         setRedirectOnSuccessUrl(getServletConfig().getInitParameter(REDIRECT_ON_SUCCESS_PARAM));
         String isSessionFormString = getServletConfig().getInitParameter(IS_SESSION_FORM_PARAM);
         if (isSessionFormString != null && isSessionFormString.toLowerCase().equals(Boolean.TRUE.toString())) {
             setSessionForm(true);
         } else {
             setSessionForm(false);
         }
         logger.trace("init: " + getCommandObjectName());
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         populatePageFragments(request, response);
         referenceData(request, response);
         populateInitialForm(request);
         getFormStateFromSession(request);
     }
 
     protected void populateInitialForm(HttpServletRequest request) {
         logger.info("Adding attribute with name: " + getCommandObjectName());
         request.setAttribute(getCommandObjectName(), getNewCommandObject());
         // if we have a service to retreive the commandObject for edit
         if (getEditableService() != null) {
             Long id = RequestHelper.getLongParameter(request, "id");
             if (id == null || id == 0l) {
                 request.setAttribute(getCommandObjectName(), getNewCommandObject());
             } else {
                 CommandObject commandObject = getEditableService().getForEdit(id);
                 logger.info("commandObject: " + commandObject);
                 if (commandObject == null) {
                     request.setAttribute(getCommandObjectName(), getNewCommandObject());
                     return;
                 }
                 request.setAttribute(getCommandObjectName(), commandObject);
             }
         }
     }
 
     protected void populateFrom(HttpServletRequest request, CommandObject commandObject) {
         logger.trace("Adding attribute with name: " + getCommandObjectName());
         request.setAttribute(getCommandObjectName(), commandObject);
     }
 
     protected void populateFrom(HttpServletRequest request, CommandObject commandObject, ValidationErrors<CommandObject> bindingErrors,
             ValidationErrors<CommandObject> validationErrors) {
         logger.trace("Adding attribute with name: " + getCommandObjectName());
         request.setAttribute(getCommandObjectName(), commandObject);
         if (bindingErrors != null && !bindingErrors.isEmpty()) {
             request.setAttribute(getCommandObjectName() + "Errors", bindingErrors);
             return;
         }
         if (validationErrors != null && !validationErrors.isEmpty()) {
             request.setAttribute(getCommandObjectName() + "Errors", validationErrors);
         }
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // try to bind the form parameters to an object
         // doBind returns a bind result
         // if the result contains errors
         // we populate the form and the error objects with the errors from
         // binding results
         // if the result contains no errors, we have a populated bean which we
         // validate
         // if validation fails, we we populate the form and the error objects
         // with the errors from validation
         // else we process the bean
         if (!isFormSubmited(request)) {
             doGet(request, response);
             return;
         }
         CommandObject commandObject = getNewCommandObject();
         ValidationErrors<CommandObject> bindErrors = new ValidationErrors<CommandObject>();
         doBind(request, response, commandObject, bindErrors);
         logger.info("commandObject after bind: " + commandObject);
         if (!bindErrors.isEmpty()) {
             logger.info("bindErrors: " + bindErrors);
             request.setAttribute(getCommandObjectName() + "Errors", bindErrors);
             populateFrom(request, commandObject);
             saveFormStateToSession(request, commandObject, bindErrors, null);
             referenceData(request, response);
             return;
         }
         ValidationErrors<CommandObject> validationErrors = new ValidationErrors<CommandObject>();
         doValidate(request, response, commandObject, validationErrors);
         logger.info("validationErrors1: " + validationErrors);
         if (!validationErrors.isEmpty()) {
             logger.info("validationErrors: " + validationErrors);
             request.setAttribute(getCommandObjectName() + "Errors", validationErrors);
             populateFrom(request, commandObject);
             saveFormStateToSession(request, commandObject, null, validationErrors);
             referenceData(request, response);
             return;
         }
         logger.info("Processing command object: " + commandObject);
         processCommandObject(request, response, commandObject);
         if (isRedirect(request)) {
             logger.info("redirect");
             return;
         }
         populateFrom(request, commandObject);
         saveFormStateToSession(request, commandObject, bindErrors, validationErrors);
         referenceData(request, response);
     }
 
     private void saveFormStateToSession(HttpServletRequest request, CommandObject commandObject, ValidationErrors<CommandObject> bindingErrors,
             ValidationErrors<CommandObject> validationErrors) {
         if (isSessionForm()) {
             HttpSession session = request.getSession();
             SessionForm<CommandObject> sessionForm = new SessionForm<CommandObject>(commandObject, bindingErrors, validationErrors);
             session.setAttribute(getCommandObjectName() + "SessionForm", sessionForm);
         }
     }
 
     @SuppressWarnings("unchecked")
     private void getFormStateFromSession(HttpServletRequest request) {
         if (!isSessionForm()) {
             logger.warn("This is not a session form!");
             return;
         }
         HttpSession session = request.getSession(false);
         if (session == null) {
             logger.warn("Session is null!");
             return;
         }
         SessionForm<CommandObject> sessionForm = (SessionForm<CommandObject>) session.getAttribute(getCommandObjectName() + "SessionForm");
         if (sessionForm == null) {
             logger.warn("sessionForm is null");
             return;
         }
         populateFrom(request, sessionForm.getCommandObject(), sessionForm.getBindErrors(), sessionForm.getValidationErrors());
 
     }
 
     private void getAllFieldsForClass(Class theClass, List<Field> fields) {
         if (theClass == null || theClass.equals(Object.class)) {
             return;
         }
         Field[] fs = theClass.getDeclaredFields();
         fields.addAll(Arrays.asList(fs));
         getAllFieldsForClass(theClass.getSuperclass(), fields);
     }
 
     protected void doBind(HttpServletRequest request, HttpServletResponse response, final CommandObject commandObject, final ValidationErrors<CommandObject> errors) {
         Class commandObjectClass = getCommandObjectClass();
         Binder<CommandObject> binder = new Binder<CommandObject>();
 
         List<Field> commandObjectFields = new ArrayList<Field>();
         getAllFieldsForClass(commandObjectClass, commandObjectFields);
         logger.info("commandObjectFields size: " + commandObjectFields.size());
 
         for (Field commandObjectField : commandObjectFields) {
             logger.info("Try to bind field name: " + commandObjectField.getName()
                     + " on value: " + request.getParameter(commandObjectField.getName()));
 //                logger.info("getGenericType: " + commandObjectField.getGenericType());
             Class fieldClass = commandObjectField.getType();
             String fieldName = commandObjectField.getName();
             String fieldValue = request.getParameter(commandObjectField.getName());
             if (fieldValue == null || fieldValue.trim().isEmpty()) {
                 continue;
             }
             if (fieldClass.equals(String.class)) {
                 logger.info("binding string field: " + fieldName);
                 binder.bindString(commandObject, fieldName, fieldValue.trim());
             } else if (fieldClass.equals(Long.class)) {
                 logger.info("binding long field: " + fieldName);
                 binder.bindLong(commandObject, fieldName, fieldValue, errors);
             } else if (fieldClass.equals(Integer.class)) {
                 logger.info("binding integer field: " + fieldName);
                 binder.bindInteger(commandObject, fieldName, fieldValue, errors);
             } else if (fieldClass.equals(Date.class)) {
                 logger.info("binding date field: " + fieldName);
                 binder.bindDate(commandObject, fieldName, fieldValue, errors);
             } else if (getDependentEntities() != null && getDependentEntities().containsKey(fieldName)) {
                 logger.info("binding dependent entity field: " + fieldName);
                 PersistentEntity<Long> dependentEntity = getDependentEntities().get(fieldName);
                 binder.bindEntity(commandObject, fieldName, fieldValue, dependentEntity, errors);
             } else if (getDependentClasses() != null && getDependentClasses().containsKey(fieldName)) {
                 logger.info("binding dependent class field: " + fieldName);
                 ClassFromString dependentClass = getDependentClasses().get(fieldName);
                 binder.bindClassFromString(commandObject, fieldName, fieldValue, dependentClass, errors);
             }
         }
         logger.info("commandObject: " + commandObject);
     }
 
     protected Map<String, PersistentEntity<Long>> getDependentEntities() {
         return null;
     }
 
     protected Map<String, ClassFromString> getDependentClasses() {
         return null;
     }
 
     protected Class[] getValidationGroups() {
         logger.info("getBaseValidationGroups");
         return new Class[]{Default.class};
     }
 
     protected void doValidate(final HttpServletRequest request, final HttpServletResponse response, final CommandObject commandObject,
             final ValidationErrors<CommandObject> errors) {
         ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
         Validator validator = factory.getValidator();
         Set<ConstraintViolation<CommandObject>> constraintViolations = validator.validate(commandObject, getValidationGroups());
         if (!constraintViolations.isEmpty()) {
             ErrorAppender<CommandObject> errorAppender = new ErrorAppender<CommandObject>();
             errorAppender.addAll(constraintViolations, errors);
 
         }
         logger.info("validationErrors: " + errors);
     }
 
     protected boolean isFormSubmited(HttpServletRequest request) {
         if (request.getParameter(getSubmitParameterName()) != null) {
             logger.info("The form for the servlet: " + getServletName() + " was submited!");
             return true;
         }
         logger.info("The form for the servlet: " + getServletName() + " was not submited!");
         return false;
     }
 
     protected void processCommandObject(final HttpServletRequest request, final HttpServletResponse response, final CommandObject commandObject) {
         if (getEditableService() != null) {
             logger.info("CommandObject to save: " + commandObject);
             getEditableService().save(commandObject);
         }
        logger.info("redirect to: " + getRedirectOnSuccessUrl());
        if (getRedirectOnSuccessUrl() != null) {
            logger.info("redirect to: " + getRedirectOnSuccessUrl());
            redirect(request, getRedirectOnSuccessUrl());
        }
     }
 
     @Override
     protected void referenceData(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     }
 
     protected abstract CommandObject getNewCommandObject();
 
     protected EditableService<CommandObject> getEditableService() {
         return null;
     }
 
     @SuppressWarnings("unchecked")
     protected Class<CommandObject> getCommandObjectClass() {
         return (Class<CommandObject>) getNewCommandObject().getClass();
     }
 
     private void setSubmitParameterName(String submitParameterName) {
         this.submitParameterName = submitParameterName;
     }
 
     /**
      * submitParameterName must be unique within a page
      */
     protected String getSubmitParameterName() {
         if (submitParameterName == null) {
             submitParameterName = getCommandObjectName() + "Submit";
         }
         return submitParameterName;
     }
 
     private void setCommandObjectName(String commandObjectName) {
         this.commandObjectName = commandObjectName;
     }
 
     /**
      * commandObjectName must be unique witin a session if the form is a session
      * form or unique within a page else
      * 
      * @return
      */
     protected String getCommandObjectName() {
 
         if (commandObjectName != null && !commandObjectName.trim().isEmpty()) {
             logger.trace("Command: " + commandObjectName);
             return commandObjectName;
         }
         logger.trace("Command: " + Introspector.decapitalize(getCommandObjectClass().getSimpleName()));
         commandObjectName = Introspector.decapitalize(getCommandObjectClass().getSimpleName());
         return commandObjectName;
     }
 
     protected boolean isSessionForm() {
         return sessionform;
     }
 
     private void setSessionForm(boolean sessionForm) {
         this.sessionform = sessionForm;
     }
 
     protected void setRedirectOnSuccessUrl(String url) {
         this.redirectOnSuccessUrl = url;
     }
 
     protected String getRedirectOnSuccessUrl() {
         return redirectOnSuccessUrl;
     }
 }
