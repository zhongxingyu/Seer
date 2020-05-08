 package fizzbuzz;
 
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class FizzBuzzTest {
     private FizzBuzz fizzBuzz = new FizzBuzz();
     @Test
     public void oneGivesOne() {
        assertThat(fizzBuzz.gives(1), is("1"));
     }
 
     @Test
     public void twoGivesTwo() {
        assertThat(fizzBuzz.gives(2), is("2"));
     }
 
     @Test
     public void threeGivesFizz() {
        assertThat(fizzBuzz.gives(3), is("Fizz"));
     }
 
     @Test
     public void fifteenGivesFizzBuzz() {
        assertThat(fizzBuzz.gives(15), is("FizzBuzz"));
     }
 
 }
