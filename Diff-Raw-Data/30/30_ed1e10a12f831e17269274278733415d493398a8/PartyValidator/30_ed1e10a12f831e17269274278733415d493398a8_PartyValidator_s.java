 package eu.comexis.napoleon.shared.validation;
 
 import static eu.comexis.napoleon.client.resources.ValidationMessages.VALIDATION_MESSAGES;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import eu.comexis.napoleon.shared.model.Party;
 
 public class PartyValidator<T extends Party> extends AbstractValidator<T> {
 
   @Override
   public List<ValidationMessage> validate(T party) {
     List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
     
     validateName(party, messages);
     validateEmail(party.getEmail(), messages);
     //continue...
     
     return messages;
   }
 
   private void validateEmail(String email, List<ValidationMessage> messages) {
     if (isEmpty(email)){
       messages.add(new ValidationMessage(VALIDATION_MESSAGES.fieldIsMandatory("email"), "email"));
     }else if(!isValidEmail(email)){
       messages.add(new ValidationMessage(VALIDATION_MESSAGES.emailInvalid(email), "email"));
     }
     
   }
 
   private void validateName(T party, List<ValidationMessage> messages) {
    
     if (isEmpty(party.getFirstName())){
       messages.add(new ValidationMessage(VALIDATION_MESSAGES.fieldIsMandatory("prénom"), "firstName"));
     }
     
     if (isEmpty(party.getLastName())){
       messages.add(new ValidationMessage(VALIDATION_MESSAGES.fieldIsMandatory("nom"), "name"));
     }

    
   }
 
  
 
 }
