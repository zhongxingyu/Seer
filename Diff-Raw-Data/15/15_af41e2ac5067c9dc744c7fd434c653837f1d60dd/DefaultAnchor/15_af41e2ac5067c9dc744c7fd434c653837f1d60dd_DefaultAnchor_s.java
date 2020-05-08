 package au.net.netstorm.boost.gunge.type;
 
 import au.net.netstorm.boost.bullet.primordial.Primordial;
 
 import java.lang.reflect.Field;
 
 // FIX 2328 Expecting a FIX if this is intended be part of something bigger.
 
 // FIX 2328 Can be tested with the AtomTestChecker.
 public final class DefaultAnchor extends Primordial implements Anchor {
     private final Field field;
 
     public DefaultAnchor(Field field) {
         this.field = field;
        if (field == null) throw new IllegalArgumentException();
     }
 
     public Field getField() {
         return field;
     }
 }
