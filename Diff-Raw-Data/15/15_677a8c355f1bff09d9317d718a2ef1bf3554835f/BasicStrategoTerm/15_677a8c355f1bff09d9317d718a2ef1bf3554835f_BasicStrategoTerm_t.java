 /*
  * Created on 2. feb.. 2007
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.terms;
 
 public abstract class BasicStrategoTerm implements IStrategoTerm, Cloneable {
 
     private IStrategoList annotations;
     
     protected BasicStrategoTerm(IStrategoList annotations) {
         this.annotations = annotations;
     }
     
     public int getStorageType() {
         return MUTABLE; // provides no guarantees (and does not cache hashCode())
     }
 
     public boolean match(IStrategoTerm second) {
         return this == second || doSlowMatch(second);
     }
 
     protected abstract boolean doSlowMatch(IStrategoTerm second);
     
     @Override
     public boolean equals(Object obj) {
         if(!(obj instanceof IStrategoTerm))
             return false;
         return match((IStrategoTerm)obj);
     }
     
     @Override
     public abstract int hashCode();
     
     @Override
     public abstract String toString();
     
     protected void appendAnnotations(StringBuilder sb) {
         IStrategoList annos = getAnnotations();
         if (annos.size() == 0) return;
         
         sb.append('{');
         sb.append(annos.get(0));
        for (annos = annos.tail(); !annos.isEmpty(); annos = annos.tail()) {
            sb.append(',');
            sb.append(annos.head().toString());         
         }
         sb.append('}');
     }
     
     protected void printAnnotations(ITermPrinter pp) {
         IStrategoList annos = getAnnotations();
         if (annos.size() == 0) return;
         
         pp.print("{");
        annos.head().prettyPrint(pp);
        for (annos = annos.tail(); !annos.isEmpty(); annos = annos.tail()) {
            pp.print(",");
            annos.head().prettyPrint(pp);           
         }
         pp.print("}");
     }
     
     @Override
     protected BasicStrategoTerm clone() {
         try {
             return (BasicStrategoTerm) super.clone();
         } catch (CloneNotSupportedException e) {
             throw new RuntimeException(e); // silly checked exceptions...
         }
     }
     
     public IStrategoList getAnnotations() {
         return annotations == null ? BasicTermFactory.EMPTY_LIST : annotations;
     }
     
     void internalSetAnnotations(IStrategoList annotations) {
         this.annotations = annotations;
     }
 }
