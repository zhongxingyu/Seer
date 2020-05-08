 package jpower.core.test;
 
 import jpower.core.PowerClassLoader;
import jpower.core.internal.PowerInternalSystem;
 import org.junit.Test;
 
 import static org.junit.Assert.assertNotNull;
 
 public class PowerLoaderTest {
     @Test
     public void testLoaderAutoLoading() {
         PowerClassLoader loader = new PowerClassLoader();
         loader.enableAutoLoading();
         loader.onClassFound(className -> System.out.println(className + " was found"));
         loader.onClassLoaded(clazz -> System.out.println(clazz.getName() + " was loaded"));
        loader.addURL(getClass().getClassLoader().getResource("TestA.jar"));
        Class[] loaded = PowerInternalSystem.getLoadedClasses(loader);
         assertNotNull(loaded);
     }
 }
