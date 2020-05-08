 package org.xezz.reddit;
 
 import org.junit.Test;
 
 import java.util.List;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 /**
  * User: Xezz
  * Date: 18.06.13
  * Time: 12:48
  */
 public class DieTest {
 
     @Test
     public void testConstructor() throws Exception {
         final int times = 1;
         final int faces = 2;
         Die testee = new Die(times, faces);
         assertThat("Times was not the same", times, is(testee.getTimes()));
         assertThat("Faces was not the same", faces, is(testee.getFaces()));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorFailsWithIAETimes() throws Exception {
         new Die(0, 2);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorFailsWithIAEFaces() throws Exception {
         new Die(1, 1);
     }
 
     @Test
     public void testRollDice() throws Exception {
         final int times = 10;
         final int faces = 15;
         Die testee = new Die(times, faces);
         // Do a lot rolls to get a more reliable test
        for (int j = Integer.MIN_VALUE; j <= Integer.MIN_VALUE; j++) {
             final List<Integer> rolls = testee.rollDice();
             assertThat("Size of the result does not match", times, is(rolls.size()));
             for (Integer i : rolls) {
                 assertThat("Result is not within lower boundaries", i, is(greaterThan(0)));
                 assertThat("Result is not within upper boundaries", i, is(lessThanOrEqualTo(faces)));
             }
         }
     }
 }
