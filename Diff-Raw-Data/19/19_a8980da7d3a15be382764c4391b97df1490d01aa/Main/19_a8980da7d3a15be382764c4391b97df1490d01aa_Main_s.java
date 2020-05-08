 package home;
 
 import view.HumanView;
 
 public class Main
 {
     public static void main(String... args) throws Exception
     {
        new HumanView("Kevin").start();
        new HumanView("dummy 1").start();
        new HumanView("dummy 2").start();
        new HumanView("dummy 3").start();
     }
 }
