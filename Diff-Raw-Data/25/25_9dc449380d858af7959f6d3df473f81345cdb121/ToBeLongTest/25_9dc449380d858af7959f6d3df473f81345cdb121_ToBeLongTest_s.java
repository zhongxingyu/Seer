 package com.jexpect;
 
 import org.junit.Test;
 
 import static com.jexpect.Expect.expect;
 
 public class ToBeLongTest {
 
   @Test
   public void To_Be_Actual() throws Exception {
     expect(10L).toBe(10L);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void To_Be_Actual_Fail() throws Exception {
     expect(10L).toBe(9L);
   }
 
   @Test
   public void To_Be_Less_Than() throws Exception {
     expect(1L).toBeLessThan(9L);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void To_Be_Less_Than_Fail() throws Exception {
     expect(11L).toBeLessThan(9L);
   }
 
   @Test
   public void To_Be_Greater_Than() throws Exception {
    expect(1L).toBeLessThan(9L);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void To_Be_Greater_Than_Fail() throws Exception {
    expect(10L).toBeLessThan(9L);
   }
 }
