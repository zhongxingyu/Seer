 package euler.java;
 
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 /**
  * Test class for {@link Problem1}.
  *
  * @author Ryo TANAKA
  * @since 1.0
  */
 public class Problem1Test {
     private final Problem1 testee = new Problem1();
 
     /**
      * Test for {@link Problem1#fizzBuzzSum(int)}.
      *
      * @throws Exception Unexpected exception.
      */
     @Test
     public void testFizzBussSum() throws Exception {
         assertEquals("fizz buzz summation below 10 is 23", testee.fizzBuzzSum(10), 23L);
        assertThat("fizz buzz summation below 13 is 45", testee.fizzBuzzSum(13), is(45L));
         assertThat("fizz buzz summation below 1000 is ??", testee.fizzBuzzSum(1000), is(233168L));
     }
 }
