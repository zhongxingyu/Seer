 /*
  * Created on 07.aug.2005
  *
  * Copyright (c) 2004, Karl Trygve Kalleberg <karltk near strategoxt.org>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.stratego;
 
 import org.spoofax.DebugUtil;
 import org.spoofax.interpreter.core.IConstruct;
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.IStrategoTuple;
 
 public class Some extends Strategy {
 
     protected Strategy body;
     
     public Some(Strategy body) {
         this.body = body;
     }
 
     public IConstruct eval(IContext env) throws InterpreterException {
         if (DebugUtil.isDebugging()) {
             debug("Some.eval() - ", env.current());
         }
 
         IStrategoTerm t = env.current();
 
         switch (t.getTermType()) {
             case IStrategoTerm.INT:
             case IStrategoTerm.REAL:
             case IStrategoTerm.STRING:
             	return getHook().pop().onFailure(env);
             case IStrategoTerm.APPL:
             case IStrategoTerm.LIST:
             case IStrategoTerm.TUPLE:
             	return eval(env, 0, false, t.getAllSubterms());
             default:
                 throw new InterpreterException("Unknown ATerm type " + t.getTermType());
         }
     }
 
    protected IConstruct eval(final IContext env, final int n, final boolean hadsome, final IStrategoTerm[] list) throws InterpreterException
     {
     	final IStrategoTerm old = env.current();
     	if (n >= old.getSubtermCount()) {
     		if (hadsome) {
     			switch (old.getTermType()) {
    	    		case IStrategoTerm.APPL:
    	    			env.setCurrent(env.getFactory().replaceAppl(((IStrategoAppl)old).getConstructor(), list, (IStrategoAppl)old));
    	    			break ;
    	    		case IStrategoTerm.LIST:
    	    			env.setCurrent(env.getFactory().replaceList(list, (IStrategoList)old));
    	    			break ;
    	    		case IStrategoTerm.TUPLE:
    	    			env.setCurrent(env.getFactory().replaceTuple(list, (IStrategoTuple)old));        			
    	    		}
     	    	return getHook().pop().onSuccess(env);
     		}
     		else
     			return getHook().pop().onFailure(env);
     	}
     	env.setCurrent(list[n]);
     	body.getHook().push(new Hook(){
 			@Override
 			public IConstruct onFailure(IContext env) throws InterpreterException {
 				env.setCurrent(old);
 				return eval(env, n+1, hadsome, list);
 			}
 			@Override
 			public IConstruct onSuccess(IContext env) throws InterpreterException {
 				list[n] = env.current();
 				env.setCurrent(old);
 				return eval(env, n+1, true, list);
 			}
     	});
 		return body;
     }
 
     public void prettyPrint(StupidFormatter sf) {
         sf.append("Some(");
         body.prettyPrint(sf);
         sf.append(")");
     }
 }
