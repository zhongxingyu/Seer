 package de.javauni.jarcade.utils.visit;
 
 import static de.javauni.jarcade.utils.visit.VisitAdapter.*;
 
public abstract class AbstractVisitAdapter<V extends FixV<?,? extends V>> implements VisitAdapter<V> {
 
     protected static interface Visit<V extends FixV<R,?>, E, R> {
         R apply(V v, E e);
     }
 
     protected static abstract class Adapter<E, V extends FixV<?, ?>> {
         private final E e;
         protected Adapter(E e) {
             this.e = e;
         }
 
         abstract protected <R> Visit<? extends V,E,R> fix();
 
         @SuppressWarnings("unchecked")
         final <R, Vr extends FixV<R,? extends V>> R accept(Vr visitor) {
             return ((Visit<Vr,E,R>)fix()).apply(visitor,e);
         }
     }
 
     abstract protected Adapter<?,V> get();
 
     public <R, Vr extends FixV<R,? extends V>> R accept(Vr visitor) {
         return get().accept(visitor);
     };
 }
