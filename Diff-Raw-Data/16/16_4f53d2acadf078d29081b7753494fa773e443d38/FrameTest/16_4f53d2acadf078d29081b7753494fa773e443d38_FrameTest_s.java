 package de.winkler.bowling;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fest.assertions.Fail.fail;
 
 import org.junit.Test;
 
 public class FrameTest {
 
     @Test
     public void testFrame_two_throws() {
         Frame frame = new Frame();
         frame.roll(5);
         frame.roll(3);
         assertThat(frame.getFirstThrow()).isEqualTo(5);
         assertThat(frame.getSecondThrow()).isEqualTo(3);
     }
 
     @Test
     public void testFrame_illegal_second_throw() {
         Frame frame = new Frame();
         frame.roll(5);
         try {
             frame.roll(6);
             fail("Expected an exception.");
         } catch (Exception ex) {
             // ok
         }
 
         try {
             frame.roll(7);
             fail("Expected an exception.");
         } catch (Exception ex) {
             // ok
         }
 
         frame.roll(5);
         assertThat(frame.getSecondThrow()).isEqualTo(5);
 
         try {
             frame.roll(2);
         } catch (Exception ex) {
             // ok
         }
     }
 
     @Test
     public void testFrame_strike_illegal_second_throw() {
         Frame frame = new Frame();
         frame.roll(10);
         try {
             frame.roll(4);
             fail("Expected an exception.");
         } catch (Exception ex) {
             // ok
         }
     }
 
     @Test
     public void testFrame_is_a_strike() {
         Frame frame = new Frame();
         frame.roll(10);
         assertThat(frame.isStrike()).isTrue();
     }
 
     @Test
     public void testFrame_is_not_a_strike() {
         Frame frame = new Frame();
         frame.roll(5);
         frame.roll(3);
         assertThat(frame.isStrike()).isFalse();
     }
 
     @Test
     public void testFrame_is_spare() {
         Frame frame = new Frame();
         frame.roll(5);
         frame.roll(5);
         assertThat(frame.isSpare()).isTrue();
     }
 
     @Test
     public void testFrame_is_not_a_spare() {
         Frame frame = new Frame();
         frame.roll(5);
         frame.roll(3);
         assertThat(frame.isSpare()).isFalse();
     }
 
 }
