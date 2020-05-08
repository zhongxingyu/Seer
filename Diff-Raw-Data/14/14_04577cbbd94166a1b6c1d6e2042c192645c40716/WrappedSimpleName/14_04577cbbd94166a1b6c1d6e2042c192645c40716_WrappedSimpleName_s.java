 /*
  * Created on 1. okt.. 2006
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.ecjadapter.adapter;
 
import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class WrappedSimpleName extends WrappedName {
 
     private final SimpleName wrappee;
     private final static IStrategoConstructor CTOR = new ASTCtor("SimpleName", 1); 
 
     WrappedSimpleName(SimpleName wrappee) {
         super(CTOR);
         this.wrappee = wrappee;
     }
     
     @Override
     public IStrategoTerm getSubterm(int index) {
         if(index == 0)
             return ECJFactory.wrap(wrappee.getIdentifier());
         throw new ArrayIndexOutOfBoundsException();
     }
 
     @Override
     public SimpleName getWrappee() {
         return wrappee;
     }
 }
