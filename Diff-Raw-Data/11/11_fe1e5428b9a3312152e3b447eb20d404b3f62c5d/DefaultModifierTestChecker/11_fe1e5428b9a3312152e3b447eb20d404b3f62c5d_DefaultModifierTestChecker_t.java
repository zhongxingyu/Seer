 package au.net.netstorm.boost.util.reflect;
 
 import java.lang.reflect.Method;
 
 import junit.framework.Assert;
 
 public final class DefaultModifierTestChecker implements ModifierTestChecker {
     private static final String[] EXCLUSIONS = {"hashCode", "getClass", "equals", "toString", "wait", "notify", "notifyAll"};
     private final ClassMaster classMaster = new DefaultClassMaster();
     private final ModifierTestUtil modifier = new DefaultModifierTestUtil();
 
     public void checkSynchronized(Method method) {
         boolean isExclusion = isExclusion(method);
         if (isExclusion) return;
         boolean isSynchronized = modifier.isSynchronized(method);
         Assert.assertTrue(getName(method), isSynchronized);
     }
 
     public void checkPublic(Class cls) {
         boolean isPublic = modifier.isPublic(cls);
         check(cls, isPublic);
     }
 
     public void checkFinal(Class cls) {
         boolean isFinal = modifier.isFinal(cls);
         check(cls, isFinal);
     }
 
     public void checkSynchronized(Class cls) {
         Method[] methods = cls.getMethods();
         for (int i = 0; i < methods.length; i++) {
             checkSynchronized(methods[i]);
         }
     }
 
     // FIXME: SC042 Sort out 2 exclusion methods.
     private boolean isExclusion(String methodName) {
         for (int i = 0; i < EXCLUSIONS.length; i++) {
             if (methodName.equals(EXCLUSIONS[i])) return true;
         }
         return false;
     }
 
     private boolean isExclusion(Method method) {
         String name = method.getName();
         return isExclusion(name);
     }
 
     private String getName(Method method) {
         return "" + method;
     }
 
     private String getName(Class cls) {
         return classMaster.getShortName(cls);
     }
 
     private void check(Class cls, boolean ok) {
         Assert.assertTrue(getName(cls), ok);
     }
 }
