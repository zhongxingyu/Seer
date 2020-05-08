 package util;
 
 import org.junit.Test;
 import static org.fest.assertions.api.Assertions.assertThat;
 
 public class NumberPrettifierTest {
 
 
     @Test
     public void shouldPrettifySmallNumber() {
         // Given
         NumberPrettifier numberPrettifier = new NumberPrettifier();
 
         // When
         String prettifiedNumber = numberPrettifier.prettify(new Double(532));
 
         // Then
         String expectedPrettifiedNumber = "532";
         assertThat(prettifiedNumber).isEqualTo(expectedPrettifiedNumber);
     }
 
 
 
     @Test
     public void shouldPrettify1000000() {
         // Given
         NumberPrettifier numberPrettifier = new NumberPrettifier();
 
         // When
         String prettifiedNumber = numberPrettifier.prettify(new Double(1000000));
 
         // Then
         String expectedPrettifiedNumber = "1M";
         assertThat(prettifiedNumber).isEqualTo(expectedPrettifiedNumber);
     }
 
 
     @Test
     public void shouldPrettify2500000point34() {
         // Given
         NumberPrettifier numberPrettifier = new NumberPrettifier();
 
         // When
         String prettifiedNumber = numberPrettifier.prettify(2500000.34);
 
         // Then
         String expectedPrettifiedNumber = "2.5M";
         assertThat(prettifiedNumber).isEqualTo(expectedPrettifiedNumber);
     }
 
 }
 
 
 
 
