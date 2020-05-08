 package smartpool.web.form;
 
 import org.apache.commons.lang3.StringUtils;
 import org.joda.time.IllegalFieldValueException;
 import org.joda.time.LocalTime;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 import smartpool.common.Constants;
 
 public class JoinRequestFormValidator implements Validator {
     public JoinRequestFormValidator() {
     }
 
     @Override
     public boolean supports(Class<?> clazz) {
         return JoinRequestForm.class.equals(clazz);
     }
 
     @Override
     public void validate(Object target, Errors errors) {
         JoinRequestForm form = (JoinRequestForm) target;
         if (StringUtils.isBlank(form.preferredPickupTime)) {
             ValidationUtils.rejectIfEmpty(errors, "preferredPickupTime", "field.required");
        } else if (StringUtils.isBlank(form.pickupPoint)) {
            ValidationUtils.rejectIfEmpty(errors, "pickupPoint", "field.required");
        } else if (!form.contactNumber.matches("\\d*")) {
            errors.rejectValue("contactNumber", "field.invalid");
         } else if (!form.preferredPickupTime.matches("\\d{1,2}:\\d{2}")) {
             errors.rejectValue("preferredPickupTime", "field.invalid");
         } else {
             try {
                 LocalTime.parse(form.preferredPickupTime);
             } catch (IllegalFieldValueException e) {
                 errors.rejectValue("preferredPickupTime", "field.invalid");
             }
         }
     }
 }
