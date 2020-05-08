 package xapn.javapro.foobarqix.dsl;
 
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
         
         /**
          * Each number between... and...
          * 
          * @param end the end number
          * @return an {@link EachNumber} object
          */
         public EachNumber and(int end) {
            throw new RuntimeException("Not yet implemented");
         }
     }
     
    private int start;
    
     /**
      * Each number between...
      * 
      * @param start the start number
      * @return an {@link And} object
      */
     public And between(int start) {
        throw new RuntimeException("Not yet implemented");
     }
 }
