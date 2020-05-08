 package org.motechproject.ghana.mtn.domain;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.mtn.validation.ValidationError;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 import java.util.Properties;
 
 @Component
 public class MessageBundle {
     private Properties values;
     public static final String PROGRAM_NAME_MARKER = "${p}";
     public static final String ENROLLMENT_SUCCESS = "enrollment.success";
     public static final String ENROLLMENT_FAILURE = "enrollment.failure";
     public static final String ENROLLMENT_STOPPED = "enrollment.stopped";
     public static final String ENROLLMENT_ROLlOVER = "enrollment.rollover";
 
     public static final String ACTIVE_SUBSCRIPTION_PRESENT = "enrollment.active.subscription.present";
     public static final String BILLING_SUCCESS = "billing.success";
     public static final String BILLING_FAILURE = "billing.failure";
     public static final String BILLING_STOPPED = "billing.stopped";
     public static final String INVALID_MOBILE_NUMBER = "enrollment.invalid.mobile.number";
 
     public static final String STOP_NOT_ENROLLED = "stop.notenrolled";
     public static final String STOP_SPECIFY_PROGRAM = "stop.specify.program";
     public static final String STOP_SUCCESS = "stop.success";
 
     @Autowired
     public MessageBundle(@Qualifier("bundleProperties") Properties values) {
         this.values = values;
     }
 
     public String get(String key) {
         Object value = values.get(key);
         return value != null ? (String) value : StringUtils.EMPTY;
     }
 
     public String get(ValidationError error) {
         Object value = values.get(error.key());
         return value != null ? (String) value : StringUtils.EMPTY;
     }
 
     public String get(List<ValidationError> errors) {
         StringBuilder builder = new StringBuilder();
         for (ValidationError error : errors) {
             builder.append(get(error));
         }
         return builder.toString();
     }
 }
