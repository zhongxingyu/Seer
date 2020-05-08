 package jivko.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Sergii Smehov (smehov.com)
  */
 public class TestManager {
   private static List<Testable> tests = new ArrayList<>();
   static {
     //tests.add(new StdIn());
     //tests.add(new StateMachine());
    tests.add(new Commands());    
     //tests.add(new JokesCreator());
   }
   
   public static void run() throws Exception {
     for (Testable t : tests) {
       t.test();
     }
   }
 }
