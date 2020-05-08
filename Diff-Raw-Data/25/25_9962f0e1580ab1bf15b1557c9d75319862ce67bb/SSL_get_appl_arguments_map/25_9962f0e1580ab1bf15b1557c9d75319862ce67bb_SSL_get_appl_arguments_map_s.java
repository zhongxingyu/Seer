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
 import org.spoofax.interpreter.core.Tools;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class SSL_get_appl_arguments_map extends AbstractPrimitive {
 
     SSL_get_appl_arguments_map() {
         super("SSL_get_appl_arguments_map", 1, 1);
     }
     
     @Override
     public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
             throws InterpreterException {
         
        if(!Tools.isTermAppl(tvars[0]))
             return false;
         
        IStrategoAppl a = (IStrategoAppl) tvars[0];
         Strategy c = svars[0];
        final int arity = a.getConstructor().getArity();
         IStrategoTerm[] result = new IStrategoTerm[arity];
        IStrategoTerm[] applArgs = a.getAllSubterms();
         for(int i = 0; i < arity; i++) {
             env.setCurrent(applArgs[i]);
             if(!c.evaluate(env))
                 return false;
             result[i] = env.current();
         }
         env.setCurrent(env.getFactory().makeList(result));
         return true;
     }
 
 }
