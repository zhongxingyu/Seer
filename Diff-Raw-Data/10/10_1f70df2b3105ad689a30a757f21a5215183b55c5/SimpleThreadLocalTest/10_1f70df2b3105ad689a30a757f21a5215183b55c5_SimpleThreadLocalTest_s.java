 package ch.trick17.betterchecks.util;
 
 import static junit.framework.Assert.*;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.junit.Test;
 
 import ch.trick17.betterchecks.fluent.ObjectCheck;
 
 public class SimpleThreadLocalTest {
     
     @Test
     @SuppressWarnings("null")
     public void testNoArgConstructorThreadLocal() {
         new SimpleThreadLocal<Object>(Object.class).get();
         new SimpleThreadLocal<Object>(String.class).get();
         new SimpleThreadLocal<Object>(ObjectCheck.class).get();
         
         RuntimeException thrown = null;
         try {
             final Object threadLocal = new SimpleThreadLocal<Integer>(
                     Integer.class);
             threadLocal.toString();
         } catch(final RuntimeException e) {
             thrown = e;
         }
         assertNotNull(thrown);
        assertEquals(thrown.getMessage(),
                "Could not access the no-arg constructor of class java.lang.Integer");
         assertTrue(thrown.getCause() instanceof NoSuchMethodException);
         
         thrown = null;
         final ThreadLocal<ThrowingHelper> threadLocal = new SimpleThreadLocal<ThrowingHelper>(
                 ThrowingHelper.class);
         try {
             threadLocal.get();
         } catch(final RuntimeException e) {
             thrown = e;
         }
         assertNotNull(thrown);
         assertEquals(
                thrown.getMessage(),
                "Could not create initial value for ThreadLocal with class: ch.trick17.betterchecks.util.SimpleThreadLocalTest$ThrowingHelper");
         assertTrue(thrown.getCause() instanceof InvocationTargetException);
     }
     
     private static class ThrowingHelper {
         
         @SuppressWarnings("unused")
         public ThrowingHelper() {
             throw new RuntimeException();
         }
     }
 }
