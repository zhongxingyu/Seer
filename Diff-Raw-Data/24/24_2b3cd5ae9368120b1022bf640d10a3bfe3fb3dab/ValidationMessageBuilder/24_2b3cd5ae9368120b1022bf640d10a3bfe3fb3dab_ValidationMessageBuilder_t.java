 package org.burgers.email.service.validation;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class ValidationMessageBuilder {
     public String build(ValidationContext context){
         StringBuilder builder = new StringBuilder();
        builder.append("\nMessage Validation Failed:\n");
         for (String field : context.getFieldsInError()) {
             builder.append(String.format("\t%s:\n", field));
             for (String error : context.getErrorsForField(field)) {
                 builder.append(String.format("\t\t%s\n", error));
             }
         }
        return builder.toString();
     }
 }
