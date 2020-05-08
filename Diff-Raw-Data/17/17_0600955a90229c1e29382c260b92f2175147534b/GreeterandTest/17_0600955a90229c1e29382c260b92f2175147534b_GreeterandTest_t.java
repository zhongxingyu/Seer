import java.util.ArrayList;

 class TestOriginal {
     public static void main(String[] args) {
         Greeter greeter = new Greeter();
 
         int[] ints = new int[42];
 
         // TODO: Fix this
//        Object: [I@2f57d162
//        Method name: clone
//        Exception in thread "main" java.lang.ClassCastException: required class TestOriginal but encountered class [I
//        at java.lang.invoke.MethodHandleNatives.raiseException(MethodHandleNatives.java:374)
//        at java.lang.invoke.MethodHandle.invokeWithArguments(MethodHandle.java:566)
//        at main.scala.com.dindane.mireille.InliningCacheCallSite.fallback(RunTime.java:102)
//        at TestOriginal.main(GreeterandTest.java:10)
         // int[] foo = ints.clone();
 
         // TODO: ASM verification of this call fails
         // greeter.toString();
 
        ArrayList<Integer> list = new ArrayList<>();
        list.isEmpty();
        list.contains("bla");

         System.out.println(greeter.inEnglish("Samy"));
         System.out.println(greeter.inFrenchPublic("Samy"));
     }
 }
 
 class Greeter {
     public String inEnglish(String name) {
         return "Hello " + name + "!";
     }
 
     public String inFrenchPublic(String name) {
         return inFrench(name);
     }
 
     private String inFrench(String name) {
         return "Bonjour " + name + "!";
     }
 }
