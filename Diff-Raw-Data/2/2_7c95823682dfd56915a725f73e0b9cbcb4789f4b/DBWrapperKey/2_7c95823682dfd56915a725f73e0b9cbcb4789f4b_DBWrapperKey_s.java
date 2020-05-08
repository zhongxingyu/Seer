 package org.jboss.pressgang.ccms.wrapper.structures;
 
 import org.hibernate.internal.util.compare.EqualsHelper;
 
 public class DBWrapperKey {
     private final Object o;
     private final Class<?> wrapperClass;
 
     public DBWrapperKey(final Object o) {
         this(o, null);
     }
 
     public DBWrapperKey(final Object o, final Class<?> wrapperClass) {
         this.o = o;
         this.wrapperClass = wrapperClass;
     }
 
     @Override
     public boolean equals(Object o) {
         if (o == null) return false;
         if (!(o instanceof DBWrapperKey)) return false;
 
         final DBWrapperKey other = (DBWrapperKey) o;
         if (!EqualsHelper.equals(wrapperClass, other.wrapperClass)) return false;
 
        return EqualsHelper.equals(o, other.o);
     }
 }
