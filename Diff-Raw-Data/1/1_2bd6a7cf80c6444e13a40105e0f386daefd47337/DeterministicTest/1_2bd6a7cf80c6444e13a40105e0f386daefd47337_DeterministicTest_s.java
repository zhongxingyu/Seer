 package fi.lolcatz.jproffa.test;
 
 import fi.lolcatz.jproffa.implementatios.IntegerImpl;
 import fi.lolcatz.jproffa.testproject.Example;
 import fi.lolcatz.profiler.ClassBlacklist;
 import fi.lolcatz.profiler.Util;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class DeterministicTest {
 
     IntegerImpl impl;
 
     public DeterministicTest() {
     }
 
     
     @BeforeClass
     public static void classSetup() {
         ClassBlacklist.add(DeterministicTest.class);
         Example.main(null);
         Util.loadAgent();
     }
 
     @Before
     public void testSetup() {
         impl = new IntegerImpl();
         impl.setClassName("fi.lolcatz.jproffa.testproject.IterativeExample");
     }
 
     
     /**
      * Test to determine if the function is deterministic. Allows for 25 bytecode offset.
      */
     @Test
     public void testIterativeDeterministic() throws Exception {
 
         impl.setMethodName("iterativeFunction");
 
         impl.runStatic(impl.getInput(500));
 
         long first = impl.runStatic(500);
         long second = impl.runStatic(500);
 
         long marginError = impl.getMarginOfError(first);
         
         assertTrue(first != 0 && second != 0);
         assertTrue("Suorituskerrat eivät olleet 50 sisällä toisistaan", first > second - marginError && first < second + marginError);
 
     }
 
     @Test
     public void testForLoopsDeterministic() throws Exception {
 
         impl.setMethodName("factorialForFunction");
 
         impl.runStatic(impl.getInput(500));
 
         long first = impl.runStatic(impl.getInput(500));
         long second = impl.runStatic(impl.getInput(500));
 
         long marginError = impl.getMarginOfError(first);
 
         assertTrue(first != 0 && second != 0);
         assertTrue("Suorituskerrat eivät olleet 50 sisällä toisistaan", first > second - marginError && first < second + marginError);
     }
 
     @Test
     public void testWhileLoopsDeterministic() throws Exception {
 
         impl.setMethodName("factorialWhileFunction");
 
         impl.runStatic(impl.getInput(500));
 
         long first = impl.runStatic(impl.getInput(500));
         long second = impl.runStatic(impl.getInput(500));
 
         long marginError = impl.getMarginOfError(first);
 
         assertTrue(first != 0 && second != 0);
         assertTrue("Suorituskerrat eivät olleet 50 sisällä toisistaan", first > second - marginError && first < second + marginError);
     }
 
     @Test
     public void testForEachLoopsDeterministic() throws Exception {
 
         impl.setMethodName("factorialForEachFunction");
 
         impl.runStatic(impl.getInput(500));
 
         long first = impl.runStatic(impl.getInput(500));
         long second = impl.runStatic(impl.getInput(500));
 
         long marginError = impl.getMarginOfError(first);        
         
         assertTrue(first != 0 && second != 0);
         assertTrue("Suorituskerrat eivät olleet 50 sisällä toisistaan", first > second - marginError && first < second + marginError);
     }
     
     @Test
     public void testRecursionCostIsDeterministic() throws Exception {
         impl.setMethodName("recursiveFunction");
         
         impl.runStatic(impl.getInput(500));
         
         long first = impl.runStatic(500);
         long second = impl.runStatic(500);
         System.out.println("Margin of error: " + impl.getMarginOfError(first));
         assertTrue("Suorituskerrat eivät olleet 50 sisällä toisistaan", first > second-impl.getMarginOfError(first) && first < second+impl.getMarginOfError(first));
         
     }
 }
