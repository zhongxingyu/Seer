 public class MyMap {
 
     public static void main(String []args) {
         SimpleMap sm = new SMap();
         sm.put(1, "Fliss");
         sm.put(26, "Keimi");
         sm.put(2, "Joe");
         sm.put(45, "David");
         sm.put(3, "Arron");
 
         System.out.println(sm.get(1));
         System.out.println(sm.get(45));
         System.out.println(sm.get(2));
         System.out.println(sm.get(3));
         System.out.println(sm.get(26));
 
         System.out.println(" ");
 
         sm.remove(1);
         System.out.println(sm.get(45));
         System.out.println(sm.get(2));
         System.out.println(sm.get(1));
         System.out.println(sm.get(3));
         System.out.println(sm.get(26));
 
         sm.remove(1);
     }
 }
