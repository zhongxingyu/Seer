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
 
 public class One extends Strategy {
 
     protected Strategy body;
     
     public One(Strategy body) {
         this.body = body;
     }
 
     public boolean eval(IContext env) throws InterpreterException {
         if (DebugUtil.isDebugging()) {
             debug("One.eval() - ", env.current());
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
                 return DebugUtil.traceReturn(evalOne(env, (IStrategoAppl)t), env.current(), this);
             case IStrategoTerm.LIST:
                 return DebugUtil.traceReturn(evalOne(env, (IStrategoList)t), env.current(), this);
             case IStrategoTerm.TUPLE:
                 return DebugUtil.traceReturn(evalOne(env, (IStrategoTuple)t), env.current(), this);
             default:
                 throw new InterpreterException("Unknown ATerm type " + t.getTermType());
         }
     }
 
     private boolean evalOne(IContext env, IStrategoTuple tuple) throws InterpreterException {
     	for(int i = 0, sz = tuple.getSubtermCount(); i < sz; i++) {
             env.setCurrent(Tools.termAt(tuple, i));
             if(body.eval(env)) {
                 IStrategoTerm[] l = new IStrategoTerm[tuple.size()];
                 for (int j = 0; j < tuple.size(); j++)
                 	l[j] = tuple.get(j);
                 l[i] = env.current();
             	IStrategoTuple t2 = env.getFactory().makeTuple(l);            
                 env.setCurrent(t2);
                 return true;
             }
         }
 		return false;
 	}
 
 	private boolean evalOne(IContext env, IStrategoList list) throws InterpreterException {
         for(int i = 0, sz = list.getSubtermCount(); i < sz; i++) {
             env.setCurrent(Tools.termAt(list, i));
             if(body.eval(env)) {
                 IStrategoTerm[] l = new IStrategoTerm[list.size()];
                 for (int j = 0; j < list.size(); j++)
                 	l[j] = list.get(j);
                 l[i] = env.current();
             	IStrategoList t2 = env.getFactory().makeList(l);            
                 env.setCurrent(t2);
                 return true;
             }
         }
         
         env.setCurrent(list);
         
         return false;
 	}
 
 	private boolean evalOne(IContext env, IStrategoAppl t) throws InterpreterException {
         IStrategoConstructor ctor = t.getConstructor();
         
         for(int i = 0, sz = t.getSubtermCount(); i < sz; i++) {
             env.setCurrent(Tools.termAt(t, i));
             if(body.eval(env)) {
                 IStrategoTerm[] xt = t.getArguments();
                 xt[i] = env.current();
             	IStrategoAppl t2 = env.getFactory().makeAppl(ctor, xt);            
                 env.setCurrent(t2);
                 return true;
             }
         }
         
         env.setCurrent(t);
         
         return false;
 	}
 
 	public void prettyPrint(StupidFormatter sf) {
         sf.append("One(\n");
         sf.bump(4);
         body.prettyPrint(sf);
         sf.append(")");
         sf.unbump(4);
     }
 }
