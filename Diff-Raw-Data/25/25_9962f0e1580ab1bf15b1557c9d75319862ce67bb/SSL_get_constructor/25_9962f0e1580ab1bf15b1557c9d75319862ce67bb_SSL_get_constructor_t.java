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
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 
 public class SSL_get_constructor extends AbstractPrimitive {
 
     public SSL_get_constructor() {
         super("SSL_get_constructor", 0, 1);
     }
     
     @Override
     public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
             throws InterpreterException {
 
         ITermFactory factory = env.getFactory();
         switch(tvars[0].getTermType()) {
         case IStrategoTerm.APPL:
             IStrategoAppl a = (IStrategoAppl) tvars[0];
             env.setCurrent(factory.makeString(a.getConstructor().getName()));
             return true;
         case IStrategoTerm.INT:
             return true;
         case IStrategoTerm.REAL:
             return true;
         case IStrategoTerm.LIST:
             env.setCurrent(factory.makeList());
             return true;
         case IStrategoTerm.STRING:
             IStrategoString current = (IStrategoString) factory.annotateTerm(tvars[0], factory.makeList());
             env.setCurrent(factory.makeString(current.toString()));
             return true;
         case IStrategoTerm.TUPLE:
             env.setCurrent(factory.makeString(""));
             return true;
        case IStrategoTerm.PLACEHOLDER:
            env.setCurrent(factory.makePlaceholder(factory.makeList()));
            return true;
         default:
             throw new IllegalStateException("SSL_get_constructor failed for " + tvars[0]);
         }
     }
 
 }
