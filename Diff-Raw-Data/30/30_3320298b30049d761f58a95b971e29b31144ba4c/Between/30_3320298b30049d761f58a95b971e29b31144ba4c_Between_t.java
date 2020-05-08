 package xapn.javapro.foobarqix.dsl;
 
import static xapn.javapro.foobarqix.dsl.EachNumber.eachNumberBetween;

 /**
  * FooBarQix DSL element.
  * 
  * @author Xavier Pigeon
  */
 public class Between {
     
     /**
      * FooBarQix DSL element.
      * 
      * @author Xavier Pigeon
      */
     public class And {
         
        private int start;
        
         /**
          * Each number between... and...
          * 
          * @param end the end number
          * @return an {@link EachNumber} object
          */
         public EachNumber and(int end) {
            return eachNumberBetween(start, end);
         }
     }
     
     /**
      * Each number between...
      * 
      * @param start the start number
      * @return an {@link And} object
      */
     public And between(int start) {
        And and = new And();
        and.start = start;
        return and;
     }
 }
