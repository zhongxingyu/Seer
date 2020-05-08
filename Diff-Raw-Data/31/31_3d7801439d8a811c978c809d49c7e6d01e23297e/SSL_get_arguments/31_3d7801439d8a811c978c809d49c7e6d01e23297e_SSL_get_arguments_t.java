 /*
  * Created on 9. jan.. 2007
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.library.ssl;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.IStrategoTuple;
 
 public class SSL_get_arguments extends AbstractPrimitive {
 
     public SSL_get_arguments() {
         super("SSL_get_arguments", 0, 1);
     }
     
     @Override
     public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
             throws InterpreterException {
         switch(tvars[0].getTermType()) {
         case IStrategoTerm.APPL:
             IStrategoAppl a = (IStrategoAppl) tvars[0];
            env.setCurrent(env.getFactory().makeList(a.getAllSubterms()));
             return true;
         case IStrategoTerm.INT:
         case IStrategoTerm.STRING:
         case IStrategoTerm.REAL:
             env.setCurrent(env.getFactory().makeList());
             return true;
         case IStrategoTerm.LIST:
             env.setCurrent(tvars[0]);
             return true;
         case IStrategoTerm.TUPLE:
             IStrategoTuple tuple = (IStrategoTuple) tvars[0];
             env.setCurrent(env.getFactory().makeList(tuple.getAllSubterms()));
             return true;
         default:
             throw new IllegalStateException("SSL_get_arguments failed for " + tvars[0]);
         }
     }
 
 }
