 package util;
 
 import org.junit.Test;
 import static org.fest.assertions.api.Assertions.*;
 
 public class NumberPrettifierTest {
 
 
     @Test
     public void shouldPrettifySmallNumber() {
         // Given
         NumberPrettifier numberPrettifier = new NumberPrettifier();
 

         // When
         String prettifiedNumber = numberPrettifier.prettify(532);
 

         // Then
         String expectedPrettifiedNumber = "532";
         assertThat(prettifiedNumber).isEqualTo(expectedPrettifiedNumber);
     }
 }
 
 
 
 
