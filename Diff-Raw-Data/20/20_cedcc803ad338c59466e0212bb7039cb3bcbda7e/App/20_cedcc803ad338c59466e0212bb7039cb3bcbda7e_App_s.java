 package fi.helsinki.coderodde.util;
 
 /**
  * Hello world!
  *
  */
 public class App
 {
     public static void main( String[] args )
     {
         MyTreeList<Integer> list = new MyTreeList<Integer>(1);
 
         System.out.println(list.isEmpty());
         boolean b = list.isHealthy();
         System.out.println(list.isHealthy());
 
         list.add(1);
 
         System.out.println(list.isEmpty());
         System.out.println(list.isHealthy());
 
         list.clear();
 
         System.out.println(list.isEmpty());
         System.out.println(list.isHealthy());
 
         String yo = "Hello, y'all!";
         String sayonara = "Gomen nasai, demoo sayonara, ahondara! ^^";
         MyTreeList<String> list2 = new MyTreeList<String>(1);
         list2.add(yo);
         System.out.println(list2.contains(yo));
         System.out.println(list2.contains(sayonara));
 
         System.out.println("clear and size()");
         list2.clear();
         System.out.println(list2.isEmpty());
         System.out.println(list2.isHealthy());
         System.out.println(list2.size());
 
         System.out.println("---");
         MyTreeList<Integer> list3 = new MyTreeList<Integer>(1);
         for (int i = 0; i < 10; i++) {
             list3.add(i);
         }
         System.out.println(list3.size());
         list.clear();
         System.out.println(list3.size());
         System.out.println(list3.isEmpty());
         System.out.println("get()");
         MyTreeList<Integer> list4 = new MyTreeList<Integer>(2);
         for (int i = 0; i < 10; i++) {
             list4.add(i);
         }
         System.out.println(list4.isHealthy());
         for (int i = 0; i < 10; i++) {
             System.out.println(list4.get(i));
         }
     }
 }
