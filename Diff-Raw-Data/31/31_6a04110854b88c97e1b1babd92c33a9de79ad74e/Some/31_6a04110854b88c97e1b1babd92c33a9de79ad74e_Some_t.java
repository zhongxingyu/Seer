 /*
  * Created on 07.aug.2005
  *
  * Copyright (c) 2004, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.stratego;
 
 import org.spoofax.DebugUtil;
 import org.spoofax.interpreter.IContext;
 import org.spoofax.interpreter.InterpreterException;
 import org.spoofax.interpreter.Tools;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.IStrategoTuple;
 
 public class Some extends Strategy {
 
     protected Strategy body;
     
     public Some(Strategy body) {
         this.body = body;
     }
 
     public boolean eval(IContext env) throws InterpreterException {
         if (DebugUtil.isDebugging()) {
            debug("Some.eval() - ", env.current());
         }
 
         IStrategoTerm t = env.current();
 
         switch (t.getTermType()) {
             case IStrategoTerm.INT:
                return DebugUtil.traceReturn(false, env.current(), this);
             case IStrategoTerm.REAL:
                return DebugUtil.traceReturn(false, env.current(), this);
             case IStrategoTerm.STRING:
                return DebugUtil.traceReturn(false, env.current(), this);
             case IStrategoTerm.APPL:
                 return DebugUtil.traceReturn(evalSome(env, (IStrategoAppl)t), env.current(), this);
             case IStrategoTerm.LIST:
                 return DebugUtil.traceReturn(evalSome(env, (IStrategoList)t), env.current(), this);
             case IStrategoTerm.TUPLE:
                 return DebugUtil.traceReturn(evalSome(env, (IStrategoTuple)t), env.current(), this);
             default:
                 throw new InterpreterException("Unknown ATerm type " + t.getTermType());
         }
     }
 
 	private boolean evalSome(IContext env, IStrategoList list) throws InterpreterException {
         IStrategoTerm[] l = new IStrategoTerm[list.size()];
         boolean success = false;
 
         for(int i = 0, sz = list.getSubtermCount(); i < sz; i++) {
         	l[i] = list.get(i);
         	env.setCurrent(Tools.termAt(list, i));
             if(body.eval(env)) {
             	l[i] = env.current();
             	success = true;
             }
         }
 
     	IStrategoList t2 = env.getFactory().makeList(l);            
         env.setCurrent(t2);
         return success;
 	}
 
 	private boolean evalSome(IContext env, IStrategoTuple list) throws InterpreterException {
         IStrategoTerm[] l = new IStrategoTerm[list.size()];
         boolean success = false;
 
         for(int i = 0, sz = list.getSubtermCount(); i < sz; i++) {
         	l[i] = list.get(i);
         	env.setCurrent(Tools.termAt(list, i));
             if(body.eval(env)) {
             	l[i] = env.current();
             	success = true;
             }
         }
 
     	IStrategoList t2 = env.getFactory().makeList(l);            
         env.setCurrent(t2);
         return success;
 	}
 
 	private boolean evalSome(IContext env, IStrategoAppl t) throws InterpreterException {
 		IStrategoConstructor ctor = t.getConstructor();
 		IStrategoTerm[] list = t.getArguments();
         boolean success = false;
 
         for(int i = 0, sz = list.length; i < sz; i++) {
         	env.setCurrent(list[i]);
             if(body.eval(env)) {
             	list[i] = env.current();
             	success = true;
             }
         }
 
     	IStrategoAppl t2 = env.getFactory().makeAppl(ctor, list);            
         env.setCurrent(t2);
         return success;
 	}
 
     public void prettyPrint(StupidFormatter sf) {
         sf.append("Some(");
         body.prettyPrint(sf);
         sf.append(")");
     }
 }
