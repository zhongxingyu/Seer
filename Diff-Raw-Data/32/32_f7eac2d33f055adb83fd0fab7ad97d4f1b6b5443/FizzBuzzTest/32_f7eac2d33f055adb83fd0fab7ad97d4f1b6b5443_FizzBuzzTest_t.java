 package fizzbuzz;
 
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class FizzBuzzTest {
     private FizzBuzz fizzBuzz = new FizzBuzz();
    private FizzBuzz sevenNiner = new FizzBuzz(7, 9);

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
 
    @Test
    public void fizzBuzzWithDifferentFactors() {
       assertThat(sevenNiner.gives(14), is("Fizz"));
       assertThat(sevenNiner.gives(15), is("15"));
       assertThat(sevenNiner.gives(18), is("Buzz"));
       assertThat(sevenNiner.gives(63), is("FizzBuzz"));
    }
 }
