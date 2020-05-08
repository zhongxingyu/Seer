 package galapagos.tests;
 
 import galapagos.*;
 import junit.framework.*;
 import java.util.*;
 
 public class BiotopeTest extends TestCase {
 
     public void testBiotope () {
         ArrayList<Behavior> list = new ArrayList<Behavior>();
         list.add(new Samaritan());
         list.add(new Grudger());
         list.add(new FlipFlopper());
         Biotope b = new Biotope(list);
        b.runRound();
     }
 }
