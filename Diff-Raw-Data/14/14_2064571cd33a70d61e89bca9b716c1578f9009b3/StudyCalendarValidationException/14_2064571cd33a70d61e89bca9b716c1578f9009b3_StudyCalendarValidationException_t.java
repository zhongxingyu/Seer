 package edu.northwestern.bioinformatics.studycalendar;
 
import org.springframework.validation.Errors;

 import static java.lang.String.format;
 
 /**
  * @author Rhett Sutphin
  */
 public class StudyCalendarValidationException extends RuntimeException {
     public StudyCalendarValidationException(String message, Object... messageParameters) {
         super(format(message, messageParameters));
     }
 
     public StudyCalendarValidationException(String message, Throwable cause, Object... messageParameters) {
         super(format(message, messageParameters), cause);
     }

    public void rejectInto(Errors errors) {
        errors.reject("error.literal", getMessage());
    }
 }
