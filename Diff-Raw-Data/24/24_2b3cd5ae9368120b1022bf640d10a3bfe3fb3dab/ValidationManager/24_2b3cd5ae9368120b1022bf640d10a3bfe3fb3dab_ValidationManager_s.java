 package org.burgers.email.service.validation;
 
 import org.burgers.email.client.TemplateEmailRequest;
 import org.burgers.email.service.ValidationException;
 import org.burgers.email.service.validation.strategy.FromAddressStrategy;
 import org.burgers.email.service.validation.strategy.TemplateNameStrategy;
 import org.burgers.email.service.validation.strategy.ToAddressStrategy;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ValidationManager {
     @Autowired
     private ToAddressStrategy toAddressStrategy;
     @Autowired
     private FromAddressStrategy fromAddressStrategy;
     @Autowired
     private ValidationMessageBuilder validationMessageBuilder;
     @Autowired
     private TemplateNameStrategy templateNameStrategy;
 
     public void validate(TemplateEmailRequest request) {
         SimpleValidationContext context = new SimpleValidationContext();
 
         toAddressStrategy.validate(request, context);
         fromAddressStrategy.validate(request, context);
         templateNameStrategy.validate(request, context);
 
         if (context.hasErrors()) {
             String message = validationMessageBuilder.build(context);
             throw new ValidationException(message);
         }
     }
 
    public void setToAddressStrategy(ToAddressStrategy toAddressStrategy) {
        this.toAddressStrategy = toAddressStrategy;
    }

    public void setValidationMessageBuilder(ValidationMessageBuilder validationMessageBuilder) {
        this.validationMessageBuilder = validationMessageBuilder;
    }
 }
