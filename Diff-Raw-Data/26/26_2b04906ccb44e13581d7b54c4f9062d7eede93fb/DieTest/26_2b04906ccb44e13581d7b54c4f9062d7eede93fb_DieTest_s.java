 package Player;
 
 import org.junit.Test;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 
 public class DieTest {
     @Test
     public void its_return_value_should_between_0_and_6() {
         //given
         Die die = new Die();
         //when
         int value = die.roll();
         //then
        assertThat(value, allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(6)));
     }
 }
