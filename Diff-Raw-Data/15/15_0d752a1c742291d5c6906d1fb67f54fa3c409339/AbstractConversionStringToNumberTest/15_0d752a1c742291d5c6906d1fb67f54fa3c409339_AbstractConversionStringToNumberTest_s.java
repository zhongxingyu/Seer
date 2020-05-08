 package http.conversion;
 
 import org.junit.Test;
 
 /**
  * This is an abstract class that is implemented to test the generic conversion behaviour of the different
  * {@link http.conversion.Conversion}s when converting from a {@link String} to a {@link Number}.
  *
  * @author Karl Bennett
  */
 public abstract class AbstractConversionStringToNumberTest<N extends Number> extends AbstractConversionTest<String, N> {
 
     private Conversion<String, N> conversion;
    private String input;
    private N output;
 
 
     /**
      * Create a new {@code AbstractConversionStringToNumberTest} that will use the supplied conversion, expected output,
      * and input objects.
      *
      * @param conversion the conversion instance to use to carry out the conversion.
      * @param output     the expected output from the conversion.
      * @param input      the input to be converted.
      */
     protected AbstractConversionStringToNumberTest(Conversion<String, N> conversion, N output, String input) {
         super(conversion, output, input);
     }
 
 
     @Test(expected = NumberFormatException.class)
     public void testConvertInvalidString() throws Exception {
 
         conversion.convert("one");
     }
 
     @Test(expected = NumberFormatException.class)
     public void testConvertEmptyString() throws Exception {
 
         conversion.convert("");
     }
 }
