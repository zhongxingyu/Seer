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
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class SSL_mkterm extends AbstractPrimitive {
 
     public SSL_mkterm() {
         super("SSL_mkterm", 0, 2);
     }
     
     @Override
     public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
             throws InterpreterException {
         
         switch (tvars[0].getTermType()) {
             case IStrategoTerm.STRING:
                 IStrategoString string = (IStrategoString) tvars[0];
                 return makeString(env, string) || makeAppl(env, string, tvars[1]);
             case IStrategoTerm.INT:
             case IStrategoTerm.REAL:
                 env.setCurrent(tvars[0]);
                 return true;
             case IStrategoTerm.LIST:
                 if (tvars[1].getTermType() != IStrategoTerm.LIST)
                     return false;
                 env.setCurrent(tvars[1]);
                 return true;
             default:
                 return false;
         }
     }
 
     private boolean makeString(IContext env, IStrategoString input) {
         String value = input.stringValue();
         if (!value.startsWith("\""))
             return false;
         
         env.setCurrent(env.getFactory().parseFromString(value + "\""));
         
         return true;
     }
 
     private boolean makeAppl(IContext env, IStrategoString nameTerm, IStrategoTerm argsTerm) {
         if (argsTerm.getTermType() != IStrategoTerm.LIST)
             return false;
         
         String name = nameTerm.stringValue();
         for (int i = 0; i < name.length(); i++) {
             char c = name.charAt(i);
            if (!(Character.isLetter(c) || c == '-')) {
                 name = name.substring(0, i);
                 break;
             }
         }
         
         IStrategoList args = (IStrategoList) argsTerm;
         
         if (name.length() == 0) { // tuple
             env.setCurrent(env.getFactory().makeTuple(args));
         } else {
             IStrategoConstructor cons = env.getFactory().makeConstructor(name, args.size());
             env.setCurrent(env.getFactory().makeAppl(cons, args));
         }        
         return true;
     }
 
 }
