 package com.mockgenerator.provider;
 
 import com.mockgenerator.configuration.Field;
 
 import java.util.List;
 
 /**
  * User: rixonmathew
  * Date: 20/01/13
  * Time: 9:38 PM
  * This interface is used for providing a random value of the given type
  */
 public interface ValueProvider<TYPE> {
 
     /**
      * This method will provide a random value. This is the simplest method that provides a type value
      * without check for min,max or a constant seed value
      * @return the random value;
      */
     public TYPE randomValue();
 
     /**
      * This method will provide a random value based on properties of field like
      * minLength,maxLength,prefix,suffix,format mask,default value etc. The intent of this method is to provide
      * a simple interface for the consumer to get a random value based on the constraints that have been defined
      * in the field instance
      * @param field the field property
      * @return
      */
     public TYPE randomValue(Field<TYPE> field);


    //TODO all these methods should be moved as package private methods
 }
