 package org.datasetdiff;
 
 /**
  * Function which can convert one value to another.
  *
 * @author agustafson
  *
  * @param <I> Input type
  * @param <O> Output type; result of the conversion
  */
 public interface ConverterFunction<I, O> {
     /**
      * Convert the input to output.
      *
      * @param input The input
      * @return The result of the conversion
      */
     O convert(I input);
 }
