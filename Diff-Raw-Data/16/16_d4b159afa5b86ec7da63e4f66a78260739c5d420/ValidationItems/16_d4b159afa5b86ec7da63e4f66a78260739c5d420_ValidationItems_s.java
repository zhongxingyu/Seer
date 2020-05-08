 package gov.nih.nci.ncicb.cadsr.loader.validator;
 
 import java.util.Set;
 import java.util.HashSet;
 
 public class ValidationItems {
 
   private Set<ValidationError> errors = new HashSet<ValidationError>();
   private Set<ValidationWarning> warnings = new HashSet<ValidationWarning>();
 
   private static ValidationItems instance = new ValidationItems();
 
   private ValidationItems() {}
 
   public static ValidationItems getInstance() {
     return instance;
   }
 
   /**
    * 
    */
   public void addItem(ValidationItem item) 
     throws IllegalArgumentException {
 
     if(item instanceof ValidationError)
       errors.add((ValidationError)item);
     else if(item instanceof ValidationWarning)
       warnings.add((ValidationWarning)item);
     else
       throw new IllegalArgumentException("Wrong ValidationItem Class: " +  item.getClass().toString());
 
   }
 
   public Set<ValidationError> getErrors() {
     return errors;
   }
 
   public Set<ValidationWarning> getWarnings() {
     return warnings;
   }
 
 }
