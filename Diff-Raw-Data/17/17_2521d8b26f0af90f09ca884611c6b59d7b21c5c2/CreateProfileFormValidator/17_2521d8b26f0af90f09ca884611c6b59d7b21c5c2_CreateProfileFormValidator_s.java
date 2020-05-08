 package smartpool.web.form;
 
 import org.apache.commons.lang3.StringUtils;
 import org.joda.time.IllegalFieldValueException;
 import org.joda.time.LocalTime;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 import static org.springframework.validation.ValidationUtils.rejectIfEmpty;
 
 public class CreateProfileFormValidator implements Validator {
     public CreateProfileFormValidator() {
     }
 
     @Override
     public boolean supports(Class<?> clazz) {
         return CreateProfileForm.class.equals(clazz);
     }
 
     @Override
     public void validate(Object target, Errors errors) {
         CreateProfileForm form = (CreateProfileForm) target;
         if (StringUtils.isBlank(form.contactNumber)) {
             rejectIfEmpty(errors, "contactNumber", "field.required");
         } else if (!form.contactNumber.matches("\\d*")) {
             errors.rejectValue("contactNumber", "field.invalid");
         }
        if (!form.preferredPickupTime.matches("\\d{1,2}:\\d{2}")) {
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
