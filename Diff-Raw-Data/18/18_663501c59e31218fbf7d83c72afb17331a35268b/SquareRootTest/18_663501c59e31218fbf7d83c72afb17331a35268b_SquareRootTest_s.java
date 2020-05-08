 package org.fit.cvut.mvi.model.functions;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 public class SquareRootTest {
 
     @Test
     public void testSafeCode() {
         SquareRoot sqrt = new SquareRoot();
         List<String> args = new ArrayList<>();
 
         args.add("47");
        assertEquals("(sqrt 47)", sqrt.code(args));
     }
 
     @Test
     public void testArity() {
         SquareRoot sqrt = new SquareRoot();
         assertEquals(1, sqrt.arity());
     }
 
     @Test
     public void testName() {
         SquareRoot sqrt = new SquareRoot();
         assertEquals("SquareRoot", sqrt.name());
     }
 
 }
