 package au.net.netstorm.boost.test.atom;
 
import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Set;
 import au.net.netstorm.boost.util.type.Immutable;
 
 public final class DefaultImmutableDeterminer implements ImmutableDeterminer {
     private final Set registered = new HashSet();
     private PrimitiveBoxer primitiveBoxer = new DefaultPrimitiveBoxer();
 
     {
         registered.add(String.class);
         registered.add(Class.class);
        registered.add(Method.class);
     }
 
     public boolean isImmutable(Class cls) {
         if (implementsImmutable(cls)) {
             return true;
         }
         if (isPrimitive(cls)) {
             return true;
         }
         if (isBoxedPrimitive(cls)) {
             return true;
         }
         return isRegistered(cls);
     }
 
     private boolean isBoxedPrimitive(Class cls) {
         return primitiveBoxer.isBoxed(cls);
     }
 
     private boolean isPrimitive(Class cls) {
         return primitiveBoxer.isPrimitive(cls);
     }
 
     private boolean implementsImmutable(Class cls) {
         return Immutable.class.isAssignableFrom(cls);
     }
 
     private boolean isRegistered(Class cls) {
         return registered.contains(cls);
     }
 }
