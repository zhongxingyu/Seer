 package fr.cg95.cvq.util.translation;
 
 import java.util.Locale;
 
 /**
  * @author jsb@zenexity.fr
  *
  */
 public interface ITranslationService {
 
     String translate(String code);
 
     String translate(String code, Object[] args);
 
     String translate(String code, Locale locale);
 
     String translate(String code, Object[] args, Locale locale);
 
     String generateInitialism(String label);
 
     String translateRequestTypeLabel(String label);
 
     String translateRequestTypeLabel(String label, Locale locale);
 }
