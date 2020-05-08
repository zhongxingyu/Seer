 /*
  * Created on 29.aug.2008
  *
  * Copyright (c) 2008, Sander Vermolen <sandervermolen near gmail.com>
  * Copyright (c) 2008, Karl Trygve Kalleberg <karltk near strategoxt dot org> 
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.library.ssl;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.Tools;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoReal;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  * @author Sander Vermolen <sandervermolen near gmail.com>
  */
 public class SSL_int extends AbstractPrimitive {
 
     protected SSL_int() {
         super("SSL_int", 0, 1);
     }
 
     public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
         if(Tools.isTermInt(tvars[0])) {
             env.setCurrent(tvars[0]);
             return true;
         }
 
         if(Tools.isTermReal(tvars[0])) {
             IStrategoReal a = (IStrategoReal) tvars[0];
            env.setCurrent(env.getFactory().makeReal(a.realValue()));
             return true;
         }
         
         return false;
    }
 }
