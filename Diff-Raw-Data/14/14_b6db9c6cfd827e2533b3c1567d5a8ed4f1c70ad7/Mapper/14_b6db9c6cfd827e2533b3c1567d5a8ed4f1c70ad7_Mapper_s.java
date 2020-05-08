 package org.riviti.EnterpriseTest.Common;
 
 import org.modelmapper.ModelMapper;
 
 /**
  * Singleton holder for a ModelMapper instance.
  *
  * @see ModelMapper
  */
public class Mapper {
     private static final ModelMapper instance = new ModelMapper();
 
     /**
      * @return a ModelMapper instance.
      */
     public static ModelMapper getInstance() {
         return instance;
     }
 }
