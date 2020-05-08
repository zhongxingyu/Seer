 package edu.northwestern.bioinformatics.studycalendar;
 
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
 }
