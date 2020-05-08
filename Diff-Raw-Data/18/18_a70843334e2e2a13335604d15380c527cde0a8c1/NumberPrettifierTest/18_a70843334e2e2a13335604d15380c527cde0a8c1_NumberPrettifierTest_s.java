 package util;
 
 import org.junit.Test;
 import static org.fest.assertions.api.Assertions.assertThat;
 
 public class NumberPrettifierTest {
 
 
     private void validatePrettifiedNumber(double aNumber, String expectedPrettifiedValue) {
         // Given
         NumberPrettifier numberPrettifier = new NumberPrettifier();
 
         // When
         String prettifiedNumber = numberPrettifier.prettify(aNumber);
 
         // Then
         assertThat(prettifiedNumber).isEqualTo(expectedPrettifiedValue);
 
     }
 
 
     @Test
     public void shouldPrettify1000000() {
         validatePrettifiedNumber(1000000, "1M");
     }
 
 
     @Test
     public void shouldPrettify2500000point34() {
         validatePrettifiedNumber(2500000.34, "2.5M");
     }
 
     @Test
     public void shouldPrettifySmallNumber() {
         validatePrettifiedNumber(532, "532");
     }
 
 
 
     @Test
     public void shouldPrettify1123456789() {
         validatePrettifiedNumber(1123456789, "1.1B");
     }
 
 
 
 
 }
 
 
 
 
