 /*
  * Created on 2. okt.. 2006
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.ecjadapter.adapter;
 
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class WrappedQualifiedName extends WrappedName {
 
     private final QualifiedName wrappee;
     private final static IStrategoConstructor CTOR = new ASTCtor("QualifiedName", 2);
     
     WrappedQualifiedName(QualifiedName wrappee) {
         super(CTOR);
         this.wrappee = wrappee;
     }
     
     @Override
     public IStrategoTerm getSubterm(int index) {
         switch(index) {
         case 0:
             return ECJFactory.wrap(wrappee.getName());
         case 1:
             return ECJFactory.wrapName(wrappee.getQualifier());
         }
         throw new ArrayIndexOutOfBoundsException();
     }
 
     @Override
     public QualifiedName getWrappee() {
         return wrappee;
     }
 
 }
