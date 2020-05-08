 /*
  * File name: CarrierFormulaFormat.java
  *    Author: Matej Urbas [matej.urbas@gmail.com]
  * 
  *  Copyright © 2012 Matej Urbas
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package diabelli.logic;
 
 /**
  * Formulae of this format support encoding of foreign embedded formulae within
  * its language.
  * @param <T> the {@link FormulaFormat#getRawFormulaType() type of the raw formula}.
  * @author Matej Urbas [matej.urbas@gmail.com]
  */
 public interface CarrierFormulaFormat<T> extends FormulaFormat<T> {
 
     // <editor-fold defaultstate="collapsed" desc="Public Abstract Methods">
     /**
      * Encodes the formula in the placeholder (i.e., the foreign formula to be
      * embedded into the given goal).
      * 
      * @param placeholder the foreign formula we want to encode in this format.
      * @param context the goal into which we want to encode the formula. There
      * may be no context, in which case this parameter may be {@code null}.
      * @return the formula (in this format) that encodes the placeholder in
      * the context of the given goal. This formula may now be added as a
      * premise to the goal.
      * @throws diabelli.logic.CarrierFormulaFormat.PlaceholderEmbeddingException 
      * thrown if an error happened during the encoding of the placeholder. The
      * message of this exception will be shown to the user in the GUI, it is 
      * therefore desired that the message is human-readable.
      */
    public abstract Formula<T> encodePlaceholder(Placeholder placeholder, Goal context) throws PlaceholderEmbeddingException;
     /**
      * Looks at the formula, recognises whether it encodes a placeholder, and
      * extracts it if so.
      * 
      * @param formula the formula from which we want to extract an embedded
      * foreign formula.
      * @param context the goal from which we want to decode the placeholder. There
      * may be no context, in which case this parameter may be {@code null}.
      * @return the placeholder, as extracted from the given formula.
      * @throws diabelli.logic.CarrierFormulaFormat.PlaceholderEmbeddingException
      * thrown if an error happened during the decoding of the placeholder. The
      * message of this exception will be shown to the user in the GUI, it is 
      * therefore desired that the message is human-readable.
      */
    public abstract Placeholder decodePlaceholder(Formula<T> formula, Goal context) throws PlaceholderEmbeddingException;
     // </editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Exception Classes">
     /**
      * This exception is thrown if an error happened during the encoding or
      * decoding of a placeholder. An example is when a placeholder references a
      * variable which it should not.
      * 
      * TODO: maybe variable name clashed (where a free variable has the same
      * name as a globally meta-quantified one) or mentioning a variable that
      * is not in the context.
      */
     public static class PlaceholderEmbeddingException extends Exception {
         
         public PlaceholderEmbeddingException(Throwable cause) {
             super(cause);
         }
         
         public PlaceholderEmbeddingException(String message, Throwable cause) {
             super(message, cause);
         }
         
         public PlaceholderEmbeddingException(String message) {
             super(message);
         }
         
         public PlaceholderEmbeddingException() {
         }
     }
     //</editor-fold>
     
 }
