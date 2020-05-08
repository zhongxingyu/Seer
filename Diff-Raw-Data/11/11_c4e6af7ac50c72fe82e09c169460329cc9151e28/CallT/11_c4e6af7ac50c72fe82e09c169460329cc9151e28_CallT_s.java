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
 import org.spoofax.interpreter.core.Tools;
 import org.spoofax.interpreter.core.VarScope;
 import org.spoofax.interpreter.stratego.SDefT.SVar;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class CallT extends Strategy {
 
     protected String name;
 
     protected Strategy[] svars;
 
     protected IStrategoTerm[] tvars;
 
     private static int counter = 0;
 
     public CallT(String name, Strategy[] svars, IStrategoTerm[] tvars) {
         this.name = name;
         this.svars = svars;
         this.tvars = tvars;
     }
 
     static int depth = 0;
     
     public IConstruct eval(final IContext env) throws InterpreterException {
 
         if (DebugUtil.isDebugging()) {
             debug("CallT.eval() - ", env.current());
         }
 
     	SDefT sdef = env.lookupSVar(name); // TODO: Memoize called sdefs?
     	
         if (sdef == null)
             throw new InterpreterException("Not found '" + name + "'");
 
         if(DebugUtil.tracing) {
             System.err.println("[" + depth + "] - " + sdef.name);
             depth++;
         }
     
         String[] formalTermArgs = sdef.getTermParams();
         SVar[] formalStrategyArgs = sdef.getStrategyParams();
 
         if (DebugUtil.isDebugging()) {
             printStrategyCall(sdef.getName(), formalStrategyArgs, svars, formalTermArgs, tvars);
         }
 
         if (svars.length != formalStrategyArgs.length)
            throw new InterpreterException("Incorrect strategy arguments, expected " + formalStrategyArgs.length
               + " got " + svars.length);
 
         if (tvars.length != formalTermArgs.length)
            throw new InterpreterException("Incorrect aterm arguments, expected " + formalTermArgs.length
               + " got " + tvars.length);
 
         VarScope newScope = new VarScope(sdef.getScope());
 
         for (int i = 0; i < svars.length; i++) {
             SVar formal = formalStrategyArgs[i];
             Strategy actual = svars[i];
 
             SDefT target = null;
             if (actual instanceof CallT &&
               ((CallT)actual).getStrategyArguments().length == 0
               && ((CallT)actual).getTermArguments().length == 0) {
                 String n = ((CallT)actual).getTargetStrategyName();
                 target = env.lookupSVar(n);
                 if (target == null) {
                     if (DebugUtil.isDebugging()) {
                         debug(env.getVarScope().dump(" "));
                     }
                     System.out.println(env.getVarScope());
                     throw new InterpreterException("No strategy '" + n + "'");
                 }
             }
             else {
                 SVar[] stratArgs = new SVar[0];
                 String[] termArgs = new String[0];
                 target = new SDefT(makeTempName(formal.name), stratArgs, termArgs, actual, env.getVarScope());
             }
 
             newScope.addSVar(formal.name, target);
         }
 
         for (int i = 0; i < tvars.length; i++) {
             String formal = formalTermArgs[i];
             IStrategoTerm actual = tvars[i];
             // FIXME: This should not be here
             if (Tools.isVar(((IStrategoAppl)actual), env))
                 actual = env.lookupVar(Tools.javaStringAt((IStrategoAppl)actual, 0));
             newScope.add(formal, actual);
         }
 
         final VarScope oldVarScope = env.getVarScope();
         final CallT th = this;
         Strategy body = sdef.getBody();
         body.getHook().push(new Hook(){
         	IConstruct onSuccess(IContext env) throws InterpreterException {
                 env.restoreVarScope(oldVarScope);
         		return th.getHook().pop().onSuccess(env);
         	}
         	IConstruct onFailure(IContext env) throws InterpreterException {
         		env.restoreVarScope(oldVarScope);
         		return th.getHook().pop().onFailure(env);
         	}
         });
         env.setVarScope(newScope);
         return body;
     }
 
     public Strategy evalWithArgs(IContext env, Strategy[] sv, IStrategoTerm[] actualTVars) throws InterpreterException {
 
         System.err.println(actualTVars.length);
         
         if (DebugUtil.isDebugging()) {
             debug("CallT.eval() - ", env.current());
         }
 
         SDefT sdef = env.lookupSVar(name); //getsdef(env);
     	
         if (sdef == null)
             throw new InterpreterException("Not found '" + name + "'");
 
     
         String[] formalTermArgs = sdef.getTermParams();
         SVar[] formalStrategyArgs = sdef.getStrategyParams();
 
         if (DebugUtil.isDebugging()) {
             printStrategyCall(sdef.getName(), formalStrategyArgs, sv, formalTermArgs, actualTVars);
         }
 
         if (sv.length != formalStrategyArgs.length)
             throw new InterpreterException("Incorrect strategy arguments, expected " + formalStrategyArgs.length
               + " got " + sv.length);
 
         if (actualTVars.length != formalTermArgs.length)
             throw new InterpreterException("Incorrect aterm arguments, expected " + formalTermArgs.length
               + " got " + actualTVars.length);
 
         VarScope newScope = new VarScope(sdef.getScope());
 
         for (int i = 0; i < sv.length; ++i) {
             SVar formal = formalStrategyArgs[i];
             Strategy actual = sv[i];
 
             SDefT target = null;
             if (actual instanceof CallT &&
               ((CallT)actual).getStrategyArguments().length == 0
               && ((CallT)actual).getTermArguments().length == 0) {
                 String n = ((CallT)actual).getTargetStrategyName();
                 target = env.lookupSVar(n);
                 if (target == null) {
                     if (DebugUtil.isDebugging()) {
                         debug(env.getVarScope().dump(" "));
                     }
                     System.out.println(env.getVarScope());
                     throw new InterpreterException("No strategy '" + n + "'");
                 }
             }
             else {
                 SVar[] stratArgs = new SVar[0];
                 String[] termArgs = new String[0];
                 target = new SDefT(makeTempName(formal.name), stratArgs, termArgs, actual, env.getVarScope());
             }
 
             newScope.addSVar(formal.name, target);
         }
 
         for (int i = 0; i < actualTVars.length; i++) {
             String formal = formalTermArgs[i];
             newScope.add(formal, actualTVars[i]);
         }
 
         final VarScope oldVarScope = env.getVarScope();
         env.setVarScope(newScope);
         final CallT th = this;
         Strategy body = sdef.getBody();
         body.getHook().push(new Hook(){
         	IConstruct onSuccess(IContext env) throws InterpreterException {
                 env.restoreVarScope(oldVarScope);
         		return th.getHook().pop().onSuccess(env);
         	}
         	IConstruct onFailure(IContext env) throws InterpreterException {
         		env.restoreVarScope(oldVarScope);
         		return th.getHook().pop().onFailure(env);
         	}
         });
         return body;
     }
 
     private IStrategoTerm[] getTermArguments() {
         return tvars;
     }
 
     private static String makeTempName(String s) {
         return "<anon_" + s + "_" + counter + ">";
     }
 
     @Override
     public String toString() {
         return "CallT(\"" + name + "\"," + svars + "," + tvars + ")";
     }
 
     public void prettyPrint(StupidFormatter sf) {
         sf.append("CallT(\n");
         sf.bump(6);
         sf.append("  \"" + name + "\"\n");
         sf.append(", " + svars + "\n");
         sf.append(", " + tvars + "\n");
         sf.append(")");
     }
 
     /**
      * Renders a string describing a strategy call. <br>
      * The format is something like: <i>do_this_1_1(s = id | t = 1)</i>
      *
      * @param name strategy name
      * @param formalStrategyArgs strategy parameters (formal)
      * @param sv strategy arguments (actual)
      * @param formalTermArgs term parameters (formal)
      * @param tvarsActual term arguments (actual)
      */
     /*package*/
     static void printStrategyCall(final String name,
       final SVar[] formalStrategyArgs, final IConstruct[] sv,
       final String[] formalTermArgs, final IStrategoTerm[] tvarsActual) {
 
         // Print this at the same indentation with the associated scope.
         StringBuilder sb = DebugUtil.buildIndent(DebugUtil.INDENT_STEP);
 
         sb.append("call : ").append(name).append("( ");
 
         final String svarNoName = "<s_noname_";
         for (int i = 0; i < sv.length; i++) {
             String sVarName = formalStrategyArgs != null ? formalStrategyArgs[i].name : (svarNoName + i + ">");
             if (i > 0) {
                 sb.append(", ");
             }
             sb.append(sVarName).append(" = ").append(sv[i]);
         }
         sb.append(" | ");
 
         final String tNoName = "<t_noname_";
         for (int i = 0; i < tvarsActual.length; i++) {
             String termName = formalTermArgs != null ? formalTermArgs[i] : (tNoName + i + ">");
             if (i > 0) {
                 sb.append(", ");
             }
             sb.append(termName).append(" = ").append(tvarsActual[i]);
         }
         sb.append(" ) ");
         debug(sb); //todo: sdef is too much
     }
 
     public String getTargetStrategyName() {
         return name;
     }
 
     public Strategy[] getStrategyArguments() {
         return svars;
     }
 
     @Override
     protected String getTraceName() {
         return "call of" + "(" + name + ")";
     }
 
 	public boolean evaluateWithArgs(IContext env, Strategy[] sv, IStrategoTerm[] tv) throws InterpreterException {
     	class Finished extends InterpreterException {
 			private static final long serialVersionUID = -3346010050685062946L;
 			boolean result;
     		Finished(boolean b)
     		{
     			super("Finished");
     			result = b;
     		}
     	}
     	getHook().push(new Hook(){
 			@Override
 			IConstruct onFailure(IContext env) throws InterpreterException {
 				throw new Finished(false);
 			}
 			@Override
 			IConstruct onSuccess(IContext env) throws InterpreterException {
 				throw new Finished(true);
 			}
     	});
     	IConstruct c = evalWithArgs(env, sv, tv);
     	boolean result = false;
     	try {
     		while (true) {
     			c = c.eval(env);
     		}
     	}
     	catch (Finished f) {
     		result = f.result;
     	}
 		return result;
 	}
 }
