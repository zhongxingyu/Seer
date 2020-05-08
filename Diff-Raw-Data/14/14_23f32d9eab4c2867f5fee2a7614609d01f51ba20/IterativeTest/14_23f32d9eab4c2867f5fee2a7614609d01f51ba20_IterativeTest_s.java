 package com.mycompany.testproject.iterativeTests;
 
 import com.mycompany.testproject.Example;
 import com.mycompany.testproject.iteratives.IterativeExample;
 import fi.lolcatz.profiledata.ProfileData;
 import fi.lolcatz.profiler.ClassBlacklist;
 import fi.lolcatz.profiler.Util;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class IterativeTest {
     
     IntegerImpl impl;
     
     public IterativeTest() {
     }
     
     
     @BeforeClass
     public static void classSetup() {
         ClassBlacklist.add(IterativeTest.class);
         Example.main(null);
         Util.loadAgent();      
         ProfileData.initialize(); 
     } 
     
     @Before
     public void testSetup() {
         impl = new IntegerImpl();
     }
     
     
     /**
      * Test to determine if the function is deterministic. Allows for 25 bytecode
      * offset.
      */
     @Test
     public void testIterativeCostIsDeterministic() {
         
         impl.setClassName("com.mycompany.testproject.iteratives.IterativeExample");
         impl.setMethodName("iterativeFunction");
         impl.run(impl.getInput(5));
         
        long first = impl.run(impl.getInput(5));
        long second = impl.run(impl.getInput(5));
         System.out.println("fist: " + first);
         System.out.println("sist: " + second);
         
         assertTrue("Suorituskerrat eivät olleet 25 sisällä toisistaan", first > second-25 && first < second+25);
         
     }
     
 //    @Test
 //    public void testForLoops() {
 //        impl.setClassName("IterativeExample");
 //        impl.setMethodName("factorialForFunction");
 //        long first = impl.run(impl.getInput(5));
 //        long second = impl.run(impl.getInput(5));
 //        assertEquals(second, first);
 //    }
 //    
 //    @Test
 //    public void testWhileLoops() {
 //        impl.setClassName("IterativeExample");
 //        impl.setMethodName("factorialWhileFunction");
 //        long first = impl.run(impl.getInput(5));
 //        long second = impl.run(impl.getInput(5));
 //        assertEquals(second, first);
 //    }
 //    
 //    @Test
 //    public void testForEachLoops() {
 //        impl.setClassName("IterativeExample");
 //        impl.setMethodName("factorialForEachFunction");
 //        long first = impl.run(impl.getInput(5));
 //        long second = impl.run(impl.getInput(5));
 //        assertEquals(second, first);
 //    }
     
     
     
 
 }
