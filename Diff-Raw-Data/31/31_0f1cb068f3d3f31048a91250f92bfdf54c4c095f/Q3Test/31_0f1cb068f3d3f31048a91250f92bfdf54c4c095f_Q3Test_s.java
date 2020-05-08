 package test.chap1;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import code.chap1.Q3;
 
 public class Q3Test {
 
     @Test
     public void test001() {
         assertTrue(Q3.isSorted("hoooge", "eghooo"));
     }
 
     @Test
     public void test002() {
         assertTrue(Q3.isSorted("koreka", "aekkor"));
     }
 
     @Test
    public void test901() {
        assertFalse(Q3.isSorted("jelagjla", "fjawl"));
     }
 
     @Test
    public void test902() {
        assertFalse(Q3.isSorted("eefaaafga", "aaaagffee"));
     }
 
 }
