 /*
  * Created on 13. jan.. 2012
  *
  * Copyright (c) 2012, Karl Trygve Kalleberg <karltk near strategoxt dot org>
  *
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.library.spx;
 
 import java.util.LinkedList;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.VarScope;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.stratego.SDefT;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 
 public class SPX_interpreter_introspect_strategies extends AbstractPrimitive {
 
     public SPX_interpreter_introspect_strategies() {
         super("SPX_interpreter_introspect_strategies", 0, 0);
     }
 
     @Override
     public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
             throws InterpreterException {
        final ITermFactory f = env.getFactory();
        final LinkedList<IStrategoAppl> as = new LinkedList<IStrategoAppl>();

        // see also Context.getStrategyNames()
        VarScope v = env.getVarScope();
        while(v.getParent() != null)
            v = v.getParent();

        for(SDefT sdef : v.getSVars()) {
            // FIXME: this throws NotImplementedException
             as.addFirst(sdef.toExternalDef(f));
         }

         env.setCurrent(f.makeList(as));
         return true;
     }
 
 }
