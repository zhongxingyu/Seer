 /*
  * Created on 08.aug.2005
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
 import org.spoofax.interpreter.terms.IStrategoInt;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class SSL_implode_string extends AbstractPrimitive {
 
     protected SSL_implode_string() {
         super("SSL_implode_string", 0, 1);
     }
 
     public boolean call(IContext env, Strategy[] sargs, IStrategoTerm[] targs) throws InterpreterException {
 
         IStrategoTerm t = targs[0];
         if(!Tools.isTermList(t))
             return false;
 
        StringBuilder result = new StringBuilder(targs.length);
 
        for (IStrategoList l = (IStrategoList) t; !l.isEmpty(); l = l.tail()) {
            IStrategoInt v = (IStrategoInt) l.head();
            result.append((char)v.intValue());
         }
        env.setCurrent(env.getFactory().makeString(result.toString()));
        return true;
     }
 }
