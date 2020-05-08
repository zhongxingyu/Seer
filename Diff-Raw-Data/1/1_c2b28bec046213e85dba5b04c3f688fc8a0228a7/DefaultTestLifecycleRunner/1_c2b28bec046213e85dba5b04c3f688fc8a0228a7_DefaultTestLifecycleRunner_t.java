 package au.net.netstorm.boost.test.parallel;
 
 import java.util.ArrayList;
 import java.util.List;
 import au.net.netstorm.boost.test.lifecycle.LifecycleTest;
 import au.net.netstorm.boost.test.lifecycle.TestLifecycle;
 
 public class DefaultTestLifecycleRunner implements TestLifecycleRunner {
     private final TestExceptionHandler handler = new DefaultTestExceptionHandler();
     private final TestEngine engine = new DefaultTestEngine();
 
     public void run(LifecycleTest test) throws Throwable {
         List exceptions = runTest(test);
         handler.checkExceptions(exceptions);
     }
 
     private List runTest(LifecycleTest test) {
        // FIX (Nov 27, 2007) TESTING 83271 Change this to 'Errors'.
         List exceptions = new ArrayList();
         TestLifecycle lifecycle = test.lifecycle();
         try {
             engine.runTest(test, lifecycle);
         } catch (Throwable t) {
             Throwable throwable = engine.error(test, t);
             exceptions.add(throwable);
         } finally {
             engine.tryCleanup(lifecycle);
         }
         return exceptions;
     }
 }
