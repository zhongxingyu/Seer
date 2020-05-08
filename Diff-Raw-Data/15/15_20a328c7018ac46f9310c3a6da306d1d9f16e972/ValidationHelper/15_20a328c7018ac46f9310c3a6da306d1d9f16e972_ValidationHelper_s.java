 package com.amee.base.validation;
 
 import com.amee.base.resource.ValidationResult;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.PropertyValues;
 import org.springframework.validation.DataBinder;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 import java.util.Map;
 
 public abstract class ValidationHelper {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private DataBinder dataBinder;
 
     public ValidationHelper() {
         super();
     }
 
     /**
      * Validates the supplied form.
      *
      * @param values to validate
      * @return true if form is valid, otherwise false
      */
     public boolean isValid(Map<String, String> values) {
         log.debug("isValid()");
         dataBinder = createDataBinder();
         prepareDataBinder(dataBinder);
         beforeBind(values);
         dataBinder.bind(createPropertyValues(values));
         Errors errors = getErrors();
         ValidationUtils.invokeValidator(getValidator(), dataBinder.getTarget(), errors);
         if (!errors.hasErrors()) {
             log.debug("isValid() - No validation errors.");
             return true;
         }
        log.debug("isValid() - Has validation errors.");
        return false;
     }
 
     /**
      * Prepare the DataBinder before it is used for validation. Default implementation updates the DataBinder
      * allowedFields and calls registerCustomEditors with the DataBinder.
      *
      * @param dataBinder to be prepared
      */
     protected void prepareDataBinder(DataBinder dataBinder) {
         dataBinder.setAllowedFields(getAllowedFields());
         registerCustomEditors(dataBinder);
     }
 
     /**
      * Hook for registering custom PropertyEditors into the DataBinder. Default implementation does
      * nothing. Extending classes should override this method to supply required PropertyEditors to
      * the DataBinder.
      *
      * @param dataBinder to set editors on
      */
     protected void registerCustomEditors(DataBinder dataBinder) {
         // do nothing
     }
 
     protected DataBinder createDataBinder() {
         return new DataBinder(getObject(), getName());
     }
 
     /**
      * Hook for pre-processing the Form before binding happens.
      *
      * @param values to be validated
      */
     protected void beforeBind(Map<String, String> values) {
         // do nothing
     }
 
     /**
      * Renames all parameters in the Form that match oldName to newName. Useful for re-implementations of beforeBind.
      *
      * @param values  to consider for renaming
      * @param oldName old name for value
      * @param newName new name for value
      */
     protected void renameValue(Map<String, String> values, String oldName, String newName) {
         String value = values.remove(oldName);
         if (value != null) {
             values.put(newName, value);
         }
     }
 
     protected PropertyValues createPropertyValues(Map<String, String> values) {
         return new MutablePropertyValues(values);
     }
 
     protected Validator getValidator() {
         throw new UnsupportedOperationException();
     }
 
     public DataBinder getDataBinder() {
         return dataBinder;
     }
 
     public Errors getErrors() {
         return dataBinder.getBindingResult();
     }
 
     public Object getObject() {
         throw new UnsupportedOperationException();
     }
 
     public String getName() {
         throw new UnsupportedOperationException();
     }
 
     public String[] getAllowedFields() {
         return new String[]{};
     }
 
     public ValidationResult getValidationResult() {
         // We always want a new ValidationResult.
         ValidationResult validationResult = new ValidationResult();
         // Add the errors.
         validationResult.setErrors(getErrors());
         // Add the values.
         for (String field : getAllowedFields()) {
             Object value = getErrors().getFieldValue(field);
             if ((value != null) && (value instanceof String)) {
                 validationResult.addValue(field, (String) value);
             }
         }
         return validationResult;
     }
 }
