 package com.mycompany.app;
 
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class AppTest {
     @Test
     public void testApp() {
         App app = new App();
         assertEquals(App.HELLO, app.sayHello());
     }
 
     @Test
     public void testApp1() {
         App app = new App();
         assertEquals(App.AWAY, app.sayGoAway());
     }
 
     @Test
     public void testBitStream() {
         BitStream bs = new BitStream(new boolean[8]);
         assertTrue(bs.getLength()==8);
         bs = new BitStream();
         assertTrue(bs.getLength()==0);
     }
 
     @Test
     public void testBit() {
         Bit b0 = new Bit(false);
         Bit b1 = new Bit(1);
         assertTrue(b1.value());
         assertTrue(b0.valueAsChar() == '0');
     }
 
     @Test
     public void testBitExtended() {
         Bit b0 = new Bit(0);
         Bit b1 = new Bit(true);
         Bit bx1 = new Bit(50);
         assertTrue("value@bx1",bx1.value());
         assertFalse("value@b0",b0.value());
         assertTrue("valueAsChar@b1",b1.valueAsChar()=='1');
         assertTrue("valueAsInt@b0",b0.valueAsInt()==0);
         assertTrue("valueAsInt@b1",b1.valueAsInt()==1);
     }
 
     @Test
     public void testBitCustomConstructor() {
         Bit b1 = Bit.newOne();
         assertTrue(b1.value());
         Bit b0 = Bit.newZero();
         assertFalse(b0.value());
     }
 
     /*@Test
     public void testBitFail() {
         Bit b1 = Bit.newOne();
         assertTrue(b1.value());
         Bit b0 = Bit.newZero(); 
         assertTrue(b0.value());
     }*/
 
     @Test
     public void testBitnegate() {
         Bit b1 = Bit.newOne();
         assertFalse(b1.negate());
         assertTrue(b1.negate());
     }
 
     @Test
     public void testBitXor() {
         Bit b1 = Bit.newOne();
         Bit b2 = new Bit(false);
         assertTrue(b1.xor(b2));
         Bit b3 = new Bit(true);
         assertFalse(b1.xor(b3));
         assertTrue(b2.xor(b3));
         Bit b4 = new Bit(false);
         Bit b5 = new Bit(false);
         assertFalse(b4.xor(b5));
     }
 
     @Test
     public void testBit2() {
         Bit2 b0 = new Bit2(false);
         Bit2 b1 = new Bit2(1);
         assertTrue(b1.value());
         assertTrue(b0.valueAsChar() == '0');
     }
}
