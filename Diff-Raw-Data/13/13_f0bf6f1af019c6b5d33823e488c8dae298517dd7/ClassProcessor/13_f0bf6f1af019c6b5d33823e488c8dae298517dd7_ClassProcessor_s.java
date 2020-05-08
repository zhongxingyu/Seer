 package verityco.agent;
 
 import java.lang.instrument.Instrumentation;
 import java.lang.instrument.UnmodifiableClassException;
 
 public class ClassProcessor {
 
   public static void premain(String agentArgs, Instrumentation inst) {
     @SuppressWarnings("rawtypes")
     Class hashmapClass = null;
     for (int i = 0; i < inst.getAllLoadedClasses().length; i++) {
       String name = inst.getAllLoadedClasses()[i].getName();
       if (name.startsWith("java.util") || name.startsWith("java.lang")) {
         System.out.println(name + " is modifiable "
             + inst.isModifiableClass(inst.getAllLoadedClasses()[i]));
        if (name.equals("java.util.HashMap")) {
           hashmapClass = inst.getAllLoadedClasses()[i];
         }
       }
     }
     ClassTransformer ct = new ClassTransformer();
     inst.addTransformer(ct, true);
     try {
       // byte[] hashClassBytes = ct.transformClass("java.util.HashMap");
       // inst.redefineClasses(new ClassDefinition(hashmapClass,
       // hashClassBytes));
       inst.retransformClasses(hashmapClass);
     } catch (UnmodifiableClassException e) {
       System.out.println(":-( SAD FACE!!!!!!!!!!!!!!");
       // e.printStackTrace();
       // } catch (IOException e) {
       // System.out.println(":-(( SAD FACE!!!!!!!!!!!!!!");
       // e.printStackTrace();
      // } catch (ClassNotFoundException e) {
       // System.out.println(":-((( SAD FACE!!!!!!!!!!!!!!");
       e.printStackTrace();
     }
   }
 
   public static void main(String[] args) {
     System.out.println("In main method, when we should be in premain!");
   }
 }
