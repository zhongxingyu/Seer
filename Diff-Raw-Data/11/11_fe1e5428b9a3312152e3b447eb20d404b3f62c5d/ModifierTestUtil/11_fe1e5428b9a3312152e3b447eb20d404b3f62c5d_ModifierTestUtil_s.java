 package au.net.netstorm.boost.util.reflect;
 
 import java.lang.reflect.Method;
 
 // FIXME: SC042 Tidy up ordering here.
 
 public interface ModifierTestUtil {
     boolean isPublic(Method method);
 
     boolean isPublicInstance(Method method);
 
     boolean isFinal(Method method);
 
     boolean isStatic(Method method);
 
     boolean isSynchronized(Method method);
 
     // FIXME: SC042 Add isSynchronized.
 
     boolean isPublic(Class cls);
 
     boolean isFinal(Class cls);
 
     boolean isAbstract(Class cls);
 
     boolean isSynchronized(Class cls);
 
     // FIXME: SC042 Move the check* folks out into ModifierTestChecker.
     // FIXME: SC042 These loook out of place.  Do they really belong here? If they do, make things symmetric.
     // FIXME: SC042 Remove these when code has been moved.
 
     void checkPublic(Class cls);
 
     void checkFinal(Class cls);
 
     void checkSynchronized(Class cls);
 }
