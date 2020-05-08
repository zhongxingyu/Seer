 /**
  * 
  */
 package xapn.javapro.foobarqix.dsl;
 
 /**
  * FooBarQix facade.
  * 
  * @author Xavier Pigeon
  */
 public class FooBarQix {
     
     /**
      * Initiate a DSL-based phrase for each integer into the range.
      * 
      * @return a {@link Between} object
      */
     public static Between eachNumber() {
        throw new RuntimeException("Not yet implemented");
     }
     
     /**
      * Initiate a DSL-based phrase for each integer into the range.
      * 
      * @param start the range start
      * @param end the end range
      * @return a {@link EachNumber} object
      */
     public static EachNumber eachNumberBetween(int start, int end) {
         return EachNumber.eachNumberBetween(start, end);
     }
     
     /**
      * Initiate a DSL-based phrase for a given number.
      * 
      * @param number the number to be analyzed
      * @return a {@link ThisNumber} object
      */
     public static ThisNumber thisNumber(int number) {
         return ThisNumber.thisNumber(number);
     }
 }
