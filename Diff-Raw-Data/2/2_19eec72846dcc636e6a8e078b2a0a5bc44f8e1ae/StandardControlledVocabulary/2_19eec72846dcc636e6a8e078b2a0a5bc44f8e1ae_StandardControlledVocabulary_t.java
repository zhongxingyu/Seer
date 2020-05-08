 /* UimControlledVocabulary.java - created on Sep 3, 2011, Copyright (c) 2011 The European Library, all rights reserved */
 package eu.europeana.uim.store;
 
 /**
  * Provides information about provider and collection type and the country.
  * 
  * @author Andreas Juffinger (andreas.juffinger@kb.nl)
  * @since Sep 3, 2011
  */
 public enum StandardControlledVocabulary implements ControlledVocabularyKeyValue {
     
     /**
      * mnemonic
      */
     MNEMONIC,
     
     /**
      * name
      */
     NAME,
     
     /**
     * type of provider/collection
      */
     TYPE,
 
     /**
      * country
      */
     COUNTRY,
     
     /** language
      *
      */
     LANGUAGE,
     
     /**
      * oaiBase
      */
     INTERNAL_OAI_BASE,
     
     /**
      * set
      */
     INTERNAL_OAI_SET,
     
     /**
      * prefix
      */
     INTERNAL_OAI_PREFIX,
     
     /**
      * active
      */
     REPOX_TYPE,
     
     /**
      * active
      */
     ACTIVE
     ;
     
 
     private StandardControlledVocabulary() {
     }
 
     @Override
     public String getFieldId() {
         return this.toString();
     }
 }
