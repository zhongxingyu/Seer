 package mirah.stdlib;
 
 import static org.junit.Assert.assertTrue;
 import java.lang.reflect.Method;
 import java.util.Set;
 import org.junit.Test;
 import org.reflections.Reflections;
 
 public class MirahTester{
   public static void executeMirahTestsForPackage(String pkg) throws Exception{
     Reflections reflections = new Reflections(pkg);
     Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(TestClass.class);
     int countTests = 0;
     int failedTests = 0;
     boolean allPassed = true;

     if (annotated.isEmpty( )){
       throw new Exception("No test-classes were found.");
     }
     
     // The test class- and method can be given as an env-variable.
     String mirahTest = System.getenv("MIRAHTEST");
     String testClassName = null;
     String testMethodName = null;
     
     if (mirahTest != null){
       String[] splitted = mirahTest.split("\\.");
       if (splitted.length == 1) testClassName = splitted[0];
       if (splitted.length == 2) testMethodName = splitted[1];
     }
 
     System.err.println("Looking for test-classes.");
     for(Class<?> clazz: annotated){
       //System.err.println("Class: " + clazz.getName());
 
       try{
         Object testInstance = clazz.getConstructor().newInstance();
 
         for(Method method: clazz.getMethods()){
           boolean run = false;
           String methodName = method.getName();
           String methodNameSub = methodName.substring(0, 4);
 
           //System.err.println("Testing method: " + method.getName() + " (" + methodNameSub + ")");
 
           if (method.isAnnotationPresent(Test.class)){
             //System.err.println("Run method because Test-annotation.");
             run = true;
           }else if(methodNameSub.equals("test")){
             //System.err.println("Run method because its name starts with 'test'.");
             run = true;
           }
           
           if (run && testClassName != null && !clazz.getSimpleName().equals(testClassName)){
             System.out.println("Classname did not match: '" + clazz.getSimpleName() + "', '" + testClassName + "'.");
             run = false;
           }else if(run && testMethodName != null && !methodName.equals(testMethodName)){
             System.out.println("Methodname did not match: '" + methodName + "', '" + testMethodName + "'.");
             run = false;
           }
 
           if (run){
             System.err.println("Running " + clazz.getSimpleName() + "." + methodName + "().");
             countTests += 1;
 
             try{
               method.invoke(testInstance);
             }catch(Exception e){
               failedTests += 1;
               Throwable exc = e.getCause();
               
               System.err.println("Failed (" + exc.getClass( ).getSimpleName( ) + ": " + exc.getMessage( ));
               exc.printStackTrace(System.err);
 
               allPassed = false;
             }
           }else{
             //System.err.println("Not running.");
           }
         }
       }catch(Exception e){
         System.err.println("Could not execute test: " + e.getMessage());
         e.printStackTrace(System.err);
       }
     }
 
     if (countTests <= 0){
       throw new Exception("No tests were found to be executed.");
     }else{
       System.out.println("MirahTester: " + (countTests - failedTests) + "/" + countTests + " tests.");
     }
 
     assertTrue("All tests must pass.", allPassed);
   }
 }
