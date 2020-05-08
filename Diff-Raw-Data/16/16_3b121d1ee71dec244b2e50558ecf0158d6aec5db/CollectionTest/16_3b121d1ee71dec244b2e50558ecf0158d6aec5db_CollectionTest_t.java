 package checkers.jimmu.examples;
 
 import checkers.jimmu.quals.Rep;
 import checkers.jimmu.quals.Safe;
 
 /**
  * Example of a collection using @Rep and @Peer annotations. 
  * 
  * This is to show that @Rep and @Peer ownership is enough to build complex structures.
  *
  * @author saf
  */
 public class CollectionTest {
 
     public static class Replicator {
         static String copy(@Safe String s) {
             return s.substring(0);
         }
     }
 
     public static class Tester {
        private @Rep PeerStack<String> stack;
 
         public Tester() {
             stack = new /*@Rep*/ PeerStack<String>();
         }
 
         public void test(String [] strings) {
             for (String s : strings) {
                 @Rep String c = new /*@Rep*/ String(s);
                 stack.push(c);
             }
             while (!stack.isEmpty()) {
                 @Rep String s = stack.pop();
                 String sc = Replicator.copy(s);
                 System.out.println(sc);
             }
         }
     }
 
     public static void main(String [] args) {
         Tester t = new Tester();
         t.test(args);
     }
 
 }
