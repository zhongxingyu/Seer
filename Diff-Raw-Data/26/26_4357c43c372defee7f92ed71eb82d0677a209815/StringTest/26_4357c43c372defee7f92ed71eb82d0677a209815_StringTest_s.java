 package fi.lolcatz.jproffa.test;
 
 import fi.lolcatz.jproffa.implementations.StringImpl;
 import fi.lolcatz.jproffa.testproject.Example;
 import fi.lolcatz.profiledata.ProfileData;
 import fi.lolcatz.profiler.ClassBlacklist;
 import fi.lolcatz.profiler.Util;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class StringTest {
     
     StringImpl impl;
     
     public StringTest() {
     }
     
     @BeforeClass
     public static void classSetup() {
         ClassBlacklist.add(StringTest.class);
         Example.main(null);
         Util.loadAgent();
     }
     
     
     @Before
     public void testSetup() {
         impl = new StringImpl();
         impl.setClassName("fi.lolcatz.jproffa.testproject.StringExample");
         ProfileData.resetCounters();
     }
     
     @Test
     public void testStringReplaceLinear() throws Exception {
         impl.setMethodName("stringReplace");
         impl.runStatic(impl.getInput(25), "a", "b");
         
         long[] totalCost = new long[5];
         int syote = 100;
         
         totalCost[0] = impl.runStatic(impl.getInput(syote), "a", "b");
         
         for (int i = 1; i < totalCost.length; i++) {
             syote = 2*syote;
             totalCost[i] = impl.runStatic(impl.getInput(syote), "a", "b");    
         }
         
         printResults("testStringReplaceLinear", totalCost);
         
         assertTrue(totalCost[0]*2 >= totalCost[1]);
         assertTrue(totalCost[1]*2 >= totalCost[2]);
         assertTrue(totalCost[2]*2 >= totalCost[3]);
         assertTrue(totalCost[3]*2 >= totalCost[4]);
         
         assertTrue(totalCost[1] > totalCost[0]);
         assertTrue(totalCost[2] > totalCost[1]);
         assertTrue(totalCost[3] > totalCost[2]);
         assertTrue(totalCost[4] > totalCost[3]);
     }
     
     
     @Test
     public void testStringReplaceLinearLarge() throws Exception {
         impl.setMethodName("stringReplace");
         impl.runStatic(impl.getInput(25), "a", "b");
         
         long[] totalCost = new long[5];
         int syote = 10000;
         
         totalCost[0] = impl.runStatic(impl.getInput(syote), "a", "b");
         
         for (int i = 1; i < totalCost.length; i++) {
             syote = 2*syote;
             totalCost[i] = impl.runStatic(impl.getInput(syote), "a", "b");    
         }
         
         printResults("testStringReplaceLinearLarge", totalCost);
         
         assertTrue(totalCost[0]*2 >= totalCost[1]);
         assertTrue(totalCost[1]*2 >= totalCost[2]);
         assertTrue(totalCost[2]*2 >= totalCost[3]);
         assertTrue(totalCost[3]*2 >= totalCost[4]);
         
         assertTrue(totalCost[1] > totalCost[0]);
         assertTrue(totalCost[2] > totalCost[1]);
         assertTrue(totalCost[3] > totalCost[2]);
         assertTrue(totalCost[4] > totalCost[3]);
     }
     
     @Test
     public void testStringReplaceLinearHUGE() throws Exception {
         impl.setMethodName("stringReplace");
         impl.runStatic(impl.getInput(25), "a", "b");
         
         long[] totalCost = new long[5];
        int syote = 10000000;
         
         totalCost[0] = impl.runStatic(impl.getInput(syote), "a", "b");
         
         for (int i = 1; i < totalCost.length; i++) {
             syote = 2*syote;
             totalCost[i] = impl.runStatic(impl.getInput(syote), "a", "b");    
         }
         
         printResults("testStringReplaceLinearHUGE", totalCost);
         
         assertTrue(totalCost[0]*2 >= totalCost[1]);
         assertTrue(totalCost[1]*2 >= totalCost[2]);
         assertTrue(totalCost[2]*2 >= totalCost[3]);
         assertTrue(totalCost[3]*2 >= totalCost[4]);
         
         assertTrue(totalCost[1] > totalCost[0]);
         assertTrue(totalCost[2] > totalCost[1]);
         assertTrue(totalCost[3] > totalCost[2]);
         assertTrue(totalCost[4] > totalCost[3]);
     }
     
     @Test
     public void testStringReplaceIsDeterministic() throws Exception {
         impl.setMethodName("stringReplace");
         impl.runStatic("aaaaaaaaas", "s", "a");
         
         long total1 = impl.runStatic("aaaaaaaaas", "s", "a");
         long total2 = impl.runStatic("aaaaaaaaas", "s", "a");
         
         assertTrue(total1 > total2 -100 && total1 < total2 + 100);
     }
     
     public void printResults(String testname, long[] results) {
         System.out.println("---" + testname + "---");
         int i = 0;
         for (long l : results) {
             i++;
             System.out.println(i + ": " + l);
         }
     }
     
 }
