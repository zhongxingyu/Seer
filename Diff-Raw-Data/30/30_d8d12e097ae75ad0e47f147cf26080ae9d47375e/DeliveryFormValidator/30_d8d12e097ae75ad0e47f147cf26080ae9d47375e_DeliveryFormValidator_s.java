 package org.motechproject.ghana.national.validator;
 
 import org.motechproject.mobileforms.api.domain.FormBean;
 import org.motechproject.mobileforms.api.domain.FormError;
 import org.motechproject.mobileforms.api.validator.FormValidator;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 @Component
public class DeliveryFormValidator implements FormValidator{
     @Override
     public List<FormError> validate(FormBean formBean) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
