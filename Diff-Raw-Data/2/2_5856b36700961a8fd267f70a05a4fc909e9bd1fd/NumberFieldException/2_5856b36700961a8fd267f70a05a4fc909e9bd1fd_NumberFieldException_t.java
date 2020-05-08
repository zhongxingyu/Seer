 /** Exception that is thrown when the user enters an invalid value for a 
  * number-based text field.
  * @author Qasim Ali
  */
 public class NumberFieldException extends Exception {
   private int value; // the value of the field that that caused this to be thrown 
   private String valueName;
 
   /** Creates this object with the specified value
    * @param value    the value of the text field 
    * @param valueName   the purpose of the variable that is read from the field
   *                    (i.e. 'category total' or 'category mark')
    */
   public NumberFieldException(int value, String valueName) {
     this.value = value;
     this.valueName = valueName;
   }
 
   public String toString() {
     return "Error reading " + this.valueName + ": \'" + this.value 
             + "\' is not a valid value.";
   }
 } 
