 package pl.gajowy.kernelEstimator;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static pl.gajowy.kernelEstimator.ErrorClass.*;
 
 @RunWith(Parameterized.class)
 public class ErrorClassTest {
 
     ErrorClass expectedErrorClass;
     float valueToClassify;
 
     public ErrorClassTest(ErrorClass expectedErrorClass, float valueToClassify) {
         this.expectedErrorClass = expectedErrorClass;
         this.valueToClassify = valueToClassify;
     }
 
     @Test
     public void shouldClassifyErrorValueProperly() {
         assertThat(ErrorClass.forValue(valueToClassify), is(expectedErrorClass));
     }
 
     @Test
     public void shouldClassifyValuesOfSameAbsoluteValueEqually() {
         assertThat(ErrorClass.forValue(valueToClassify), is(ErrorClass.forValue(-valueToClassify)));
     }
 
     @Parameterized.Parameters
     public static List<Object[]> getClassesForValues() {
           return testParametersEnsuringThat(
                   isErrorClassForValue(E04_OR_BIGGER, 1.0f),
                   isErrorClassForValue(E04_OR_BIGGER, .23f),
                   isErrorClassForValue(E04_OR_BIGGER, .000100001f),
                   isErrorClassForValue(E04_OR_BIGGER, .0002f),
                   isErrorClassForValue(E05, .0000100001f),
                   isErrorClassForValue(E05, .00002f),
                   isErrorClassForValue(E05, .00009999999f),
                   isErrorClassForValue(E06, .00000100001f),
                   isErrorClassForValue(E06, .000002f),
                   isErrorClassForValue(E07, .0000001f),
                   isErrorClassForValue(E07, .0000002f),
                  isErrorClassForValue(E08, .0000000100001f),
                  isErrorClassForValue(E08, .00000002f),
                  isErrorClassForValue(E09_OR_SMALLER, .000000001f),
                  isErrorClassForValue(E09_OR_SMALLER, .000000002f),
                  isErrorClassForValue(E09_OR_SMALLER, .0000000001f)
           );
     }
 
     private static List<Object[]> testParametersEnsuringThat(Object[]... parameters) {
         return Arrays.asList(parameters);
     }
 
     private static Object[] isErrorClassForValue(ErrorClass errorClass, float value) {
         return new Object[]{errorClass, value};
     }
 }
