 package com.jexpect;
 
 import org.junit.Test;
 
 import static com.jexpect.Expect.expect;
 
 public class ToBeDoubleTest {
 
   @Test
   public void To_Be_Actual() throws Exception {
     expect(10.0d).toBe(10.0d);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void To_Be_Actual_Fail() throws Exception {
     expect(10.0d).toBe(9.0d);
   }
 
   @Test
   public void To_Be_Less_Than() throws Exception {
     expect(1.0d).toBeLessThan(9.0d);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void To_Be_Less_Than_Fail() throws Exception {
     expect(11.0d).toBeLessThan(9.0d);
   }
 
   @Test
   public void To_Be_Greater_Than() throws Exception {
    expect(11.0d).toBeGreaterThan(9.0d);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void To_Be_Greater_Than_Fail() throws Exception {
    expect(1.0d).toBeGreaterThan(9.0d);
   }
 }
