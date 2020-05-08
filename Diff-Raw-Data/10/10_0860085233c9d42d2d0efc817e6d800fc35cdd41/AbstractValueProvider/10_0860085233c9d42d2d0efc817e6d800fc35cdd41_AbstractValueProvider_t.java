 package com.mockgenerator.provider;
 
 import com.mockgenerator.configuration.Field;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rixon
  * Date: 22/1/13
  * Time: 3:19 PM
  * This class is the abstract implementation of the ValueProvider interface.All value providers should extend
  * AbstractValueProvider
  */
 abstract class AbstractValueProvider<TYPE> implements ValueProvider<TYPE> {
 
     final Random random = new Random();
 
 
     /**
      * This method allows the provider to fetch a random value from the given range that user has supplied.
      *
      * @param values list of values that user has specified
      * @return a value from within the list
      */
     public TYPE randomValueFromRange(List<TYPE> values) {
         TYPE randomValue=null;
         if (values!=null && !values.isEmpty()){
             int size = values.size();
             randomValue = values.get(random.nextInt(size));
         }
         return randomValue;
     }
 
 
     @Override
     public TYPE randomValue(Field<TYPE> field) {
         TYPE value=null;
         if(field.getRange()!=null) {
             List<TYPE> values = (List<TYPE>) Arrays.asList(field.getRange().split(","));
             value = randomValueFromRange(values);
             return value;
         }
         //TODO how to make this cleaner?
         if(field.getFormatMask()!=null) {
             String stringValue = formattedRandomValue(field.getMinLength(),field.getMaxLength(),field.getFormatMask());
             value = formatValueFromString(stringValue);
             return value;
         }
 
         long minLength=field.getMinLength()!=0?field.getMinLength():field.getFixedLength();
         long maxLength=field.getMaxLength()!=0?field.getMaxLength():field.getFixedLength();
 
         if (field.getPrefix() != null) {
             value = randomValueWithPrefix(minLength, maxLength, field.getPrefix());
             return value;
         }
 
         if (field.getSuffix() != null) {
             value = randomValueWithSuffix(minLength,maxLength, field.getSuffix());
             return value;
         }
 
         value = randomValue(minLength,maxLength);
         if(value==null){
             value = field.getDefaultValue();
         }
         return value;
     }
 
     /**
      * This method can be used by Type specific value providers to convert a string value to a Type specifiv value
      * @param stringValue
      * @return
      */
     abstract TYPE formatValueFromString(String stringValue);
 
     /**
      * This overloaded method will generate a random value conforming to the input parameters passed
      * @param minLength the min length of the random value. a value of -1 means that length is ignored
      * @param maxLength the max length of the random value. a value of -1 means that length is ignored
      * @return the random value;
      */
     abstract TYPE randomValue(long minLength,long maxLength);
 
     /**
      * This method will generate a random value which begins with a given prefix.The default implementation
      * ignores the prefix. Specific value providers can override if required
      * @param minLength the min length of the random value. a value of -1 means that length is ignored
      * @param maxLength the max length of the random value. a value of -1 means that length is ignored
      * @param prefix value to be prefixed
      * @return the random value
      */
     protected TYPE randomValueWithPrefix(long minLength,long maxLength,TYPE prefix) {
         return randomValue(minLength,maxLength);
     }
 
     /**
      * This method will generate a random value which ends with a given suffix.The default implementation
      * ignores the prefix. Specific value providers can override if required
      * @param minLength the min length of the random value. a value of -1 means that length is ignored
      * @param maxLength the max length of the random value. a value of -1 means that length is ignored
      * @param suffix value to be suffixed
      * @return the random value
      */
     protected TYPE randomValueWithSuffix(long minLength,long maxLength,TYPE suffix) {
         return randomValue(minLength,maxLength);
     }
 
     /**
      * This method will provide the random value formatted as a String per a given format mask.
      * @param minLength the min length of the random value. a value of -1 means that length is ignored
      * @param maxLength the max length of the random value. a value of -1 means that length is ignored
      * @param formatMask the format of the output data. e.g for Date it could be DD/MM/YYYY. For ssn's it could be
      *                   ###-##-####. for telephone numbers it could be ###-###-####. This allows the value being
      *                   returned to be formatted as per user requirements
      * @return the random value as per a given format
      */
     abstract String formattedRandomValue(long minLength,long maxLength,String formatMask);
 
 }
