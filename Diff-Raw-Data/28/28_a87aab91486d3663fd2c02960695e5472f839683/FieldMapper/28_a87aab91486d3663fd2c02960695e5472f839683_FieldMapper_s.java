 package com.dottydingo.hyperion.service.translation;
 
 /**
  */
 public interface FieldMapper <C,P>
 {
     String getClientFieldName();
 
    String getPersistentFieldName();

     void convertToClient(P persistentObject, C clientObject, TranslationContext context);
 
     void convertToPersistent(C clientObject, P persistentObject, TranslationContext context);
 }
