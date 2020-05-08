 /*
  * Created on 07.aug.2005
  *
  * Copyright (c) 2004, Karl Trygve Kalleberg <karltk near strategoxt.org>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.stratego;
 
 import java.util.List;
 
 import org.spoofax.DebugUtil;
 import org.spoofax.interpreter.core.BindingInfo;
 import org.spoofax.interpreter.core.IConstruct;
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.Pair;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class GuardedLChoice extends Strategy {
 
 	private Pair<Strategy,Strategy>[] children;
     
     public GuardedLChoice(Pair<Strategy,Strategy>[] strs) {
     	children = strs;
     }
 
     public IConstruct eval(IContext env) throws InterpreterException {
         if (DebugUtil.isDebugging()) {
             debug("GuardedLChoice.eval() - ", env.current());
         }
 
         return eval(env, 0);
     }
 
    protected IConstruct eval(IContext env, final int n) throws InterpreterException {
     	if (n == (children.length - 1)) {
     		Strategy s = children[n].first;
     		s.getHook().push(getHook().pop());
     		return s;
     	}
     	else {
     	    // TODO: test variable binding guarded choice
             //       should succeed: (!1; ?a; fail <+      !2; ?a)
     	    //       should fail:    (!1; ?a; fail <+ id); !2; ?a
     		final List<BindingInfo> bi = env.getVarScope().saveUnboundVars();
         	final IStrategoTerm oldCurrent = env.current();
         	final Strategy second = children[n].second;
         	Strategy first = children[n].first;
         	first.getHook().push(new Hook(){
         		public IConstruct onSuccess(IContext env) {
                 	second.getHook().push(getHook().pop());
     				return second;
     			}
         		public IConstruct onFailure(IContext env) throws InterpreterException {
     	        	env.setCurrent(oldCurrent);
     	        	env.getVarScope().restoreUnboundVars(bi);    				
     				IConstruct result = eval(env, n+1);
     				env.getVarScope().setBoundVarsAfterBacktracking(bi);
                     return result;
     			}
         	});
         	return first;
     	}
     }
     
     public void prettyPrint(StupidFormatter sf) {
         sf.first("GuardedLChoice(");
         sf.bump(15);
         sf.append("  ");
         sf.bump(2);
         //cond.prettyPrint(sf);
         sf.unbump(2);
         sf.append(", ");
         sf.bump(2);
         //ifClause.prettyPrint(sf);
         sf.unbump(2);
         sf.append(", ");
         sf.bump(2);
         //thenClause.prettyPrint(sf);
         sf.unbump(2);
         sf.unbump(15);
         sf.line(")");
         
     }
 }
