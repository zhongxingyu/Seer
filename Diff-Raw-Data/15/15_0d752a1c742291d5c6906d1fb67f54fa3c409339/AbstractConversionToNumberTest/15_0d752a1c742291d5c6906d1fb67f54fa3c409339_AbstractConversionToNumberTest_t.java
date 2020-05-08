 package http.conversion;
 
 import org.junit.Test;
 
 /**
  * This is an abstract class that is implemented to test the generic conversion behaviour of the different
  * {@link Conversion}s when converting to a {@link Number}.
  *
  * @author Karl Bennett
  */
 public abstract class AbstractConversionToNumberTest<I, O extends Number> extends AbstractConversionTest<I, O> {
 
     private Conversion<I, O> conversion;
 
 
     /**
      * Create a new {@code AbstractConversionToNumberTest} that will use the supplied conversion, expected output, and
      * input objects.
      *
      * @param conversion the conversion instance to use to carry out the conversion.
      * @param output     the expected output from the conversion.
      * @param input      the input to be converted.
      */
     protected AbstractConversionToNumberTest(Conversion<I, O> conversion, O output, I input) {
         super(conversion, output, input);

        this.conversion = conversion;
     }
 
 
     @Test(expected = NumberFormatException.class)
     public void testConvertNull() throws Exception {
 
         conversion.convert(null);
     }
 }
