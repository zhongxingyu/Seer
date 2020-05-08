 class TestOriginal {
     public static void main(String[] args) {
         Greeter greeter = new Greeter();
 
         int[] ints = new int[42];
 
         // TODO: Fix this
        // Exception in thread "main" java.lang.IllegalAccessException:
        //     member is protected: [I.clone()Object, from TestOriginal
         // int[] foo = ints.clone();
 
         // TODO: ASM verification of this call fails
         // greeter.toString();
 
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
