 package gov.nih.nci.camod.service.impl;
 
 import gov.nih.nci.camod.service.ExpressionLevelDescManager;
 
 /**
  * @author schroedln
  * 
 * Singleton class for the ExpressionLevelDescManagerSingleton
  */
 public class ExpressionLevelDescManagerSingleton {
 
 	
     private static ExpressionLevelDescManager ourManager = new ExpressionLevelDescManagerImpl();
 
     /**
      * @return the global instance of the ExpressionLevelDescManager
      */
     public static synchronized ExpressionLevelDescManager instance() {
         return ourManager;
     }	
 }
