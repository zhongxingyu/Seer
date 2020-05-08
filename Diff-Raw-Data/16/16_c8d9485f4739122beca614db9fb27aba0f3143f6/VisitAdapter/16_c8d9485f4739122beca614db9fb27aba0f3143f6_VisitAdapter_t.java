 package de.javauni.jarcade.utils.visit;
 
 import static de.javauni.jarcade.utils.visit.VisitAdapter.*;
 
public interface VisitAdapter<V extends FixV<?, ? extends V>> {
     interface FixV<R, V extends FixV<R,V>> {}
 
     <R, Vr extends FixV<R, ? extends V>> R accept(Vr visitor);
 }
