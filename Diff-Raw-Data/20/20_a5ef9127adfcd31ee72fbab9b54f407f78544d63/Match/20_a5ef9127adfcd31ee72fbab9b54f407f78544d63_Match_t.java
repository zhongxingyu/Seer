 /*
  * Created on 07.aug.2005
  *
  * Copyright (c) 2004, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.stratego;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.spoofax.interpreter.Context;
 import org.spoofax.interpreter.FatalError;
 import org.spoofax.interpreter.IContext;
 import org.spoofax.interpreter.Pair;
 import org.spoofax.interpreter.Tools;
 
 import aterm.AFun;
 import aterm.ATerm;
 import aterm.ATermAppl;
 import aterm.ATermInt;
 import aterm.ATermList;
 import aterm.ATermReal;
 
 public class Match extends Strategy {
 
     protected ATermAppl pattern;
 
     public Match(ATermAppl pattern) {
         this.pattern = pattern;
     }
 
     public boolean eval(IContext env) throws FatalError {
         debug("Match.eval() - " + env.current());
 
         ATerm current = env.current();
 
         debug(" pattern : " + pattern);
         debug(" current : " + current);
 
         List<Pair<String, ATerm>> r = match(env, current, pattern);
 
         debug(" to bind: " + r);
 
         Context.debug(" !" + current + " ; ?" + pattern);
 
         if (r == null) {
             debug(" failure : no match!");
             return false;
         } else {
             boolean b = env.bindVars(r);
 
             if (b)
                 debug(" success : " + r);
             else
                 debug(" failure : no match!");
 
             return b;
         }
     }
 
     public List<Pair<String, ATerm>> emptyList() {
         return new ArrayList<Pair<String, ATerm>>();
     }
 
     public List<Pair<String, ATerm>> matchApplAnno(IContext env, ATermAppl t,
             ATermAppl anno) throws FatalError {
         // FIXME: Actually process anno
         ATermAppl p = Tools.applAt(anno, 0);
         return match(env, t, p);
     }
 
     public List<Pair<String, ATerm>> matchAppl(IContext env, ATermAppl t,
             ATermAppl p) throws FatalError {
         debug(" ?: '" + t.getName() + "' / " + p.getName());
 
         if (Tools.isAnno(p)) {
             return matchApplAnno(env, t, p);
         } else if (Tools.isOp(p)) {
             return matchApplOp(env, t, p);
         } else if (Tools.isInt(p)) {
             return matchApplInt(env, t, p);
         } else if (Tools.isStr(p)) {
             return matchApplStr(t, p);
         } else if (Tools.isVar(p)) {
             return matchApplVar(t, p);
         } else if (Tools.isExplode(p)) {
             return matchApplExplode(env, t, p);
         } else if (Tools.isAs(p)) {
             return matchApplAs(env, t, p);
         } else if (Tools.isWld(p)) {
             return emptyList();
         }
 
         throw new FatalError("Unknown Appl case '" + p + "'");
     }
 
     protected List<Pair<String, ATerm>> matchApplInt(IContext env, ATermAppl t,
             ATermAppl p) throws FatalError {
         if (t.getType() == ATerm.INT)
             return match(env, Tools.intAt(t, 0), Tools.applAt(p, 0));
         return null;
     }
 
     protected List<Pair<String, ATerm>> matchApplStr(ATermAppl t, ATermAppl p) {
         if (t.getName().equals(Tools.stringAt(p, 0)))
             return emptyList();
         return null;
     }
 
     protected List<Pair<String, ATerm>> matchApplVar(ATermAppl t, ATermAppl p) {
         List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
         r.add(new Pair<String, ATerm>(Tools.stringAt(p, 0), t));
         return r;
     }
 
     protected List<Pair<String, ATerm>> matchApplAs(IContext env, ATermAppl t,
             ATermAppl p) throws FatalError {
         List<Pair<String, ATerm>> r = match(env, t, Tools.applAt(p, 1));
         if (r == null)
             return null;
         debug("" + p);
         String varName = Tools.stringAt(Tools.applAt(p, 0), 0);
         r.add(new Pair<String, ATerm>(varName, t));
         return r;
     }
 
     protected List<Pair<String, ATerm>> matchApplExplode(IContext env,
             ATermAppl t, ATermAppl p) throws FatalError {
         debug("Appl:Explode");
 
         AFun ctor = t.getAFun();
         ATermList args = t.getArguments();
 
         ATermAppl ctor_p = Tools.applAt(p, 0);
         ATerm ctor_t = env.makeTerm("\"" + ctor.getName() + "\"");
 
         debug(" " + ctor_t + " / " + ctor_p);
 
         List<Pair<String, ATerm>> r = match(env, ctor_t, ctor_p);
         if (r == null)
             return null;
 
         ATermAppl appl_p = Tools.applAt(p, 1);
 
         List<Pair<String, ATerm>> x = match(env, env.makeList(args), appl_p);
         if (x == null)
             return null;
         r.addAll(x);
         return r;
     }
 
     protected List<Pair<String, ATerm>> matchApplOp(IContext env, ATermAppl t,
             ATermAppl p) throws FatalError {
 
         ATermList ctorArgs = Tools.listAt(p, 1);
 
         // Check if arity of the pattern matches that
         // of the term
         if (ctorArgs.getChildCount() != t.getChildCount())
             return null;
 
         // Check if the constructor name in the pattern
         // matches that of the term
         ATermAppl c = Tools.applAt(p, 0);
         if (!t.getName().equals(c.getName()))
             return null;
 
         // Recursively match all arguments to term
         List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
         for (int i = 0; i < t.getChildCount(); i++) {
             List<Pair<String, ATerm>> m = match(env, (ATerm) t.getChildAt(i),
                                                 (ATermAppl) ctorArgs
                                                         .getChildAt(i));
             if (m != null)
                 r.addAll(m);
             else
                 return null;
         }
         return r;
     }
 
     protected List<Pair<String, ATerm>> matchInt(IContext env, ATermInt t,
             ATermAppl p) throws FatalError {
 
         if (Tools.isAnno(p)) {
             return matchIntAnno(env, t, p);
         } else if (Tools.isInt(p)) {
             return matchIntInt(t, p);
        } else if (Tools.isReal(p)) {
            return null;
         } else if (Tools.isVar(p)) {
             return matchIntVar(t, p);
         } else if (Tools.isOp(p)) {
             return null;
         } else if (Tools.isExplode(p)) {
             return matchIntExplode(env, p);
         } else if (Tools.isWld(p)) {
             return matchIntWld(p);
         } else if (Tools.isAs(p)) {
             return matchIntAs(t, p);
         }
 
         throw new FatalError("Unknown Int case '" + p + "'");
     }
 
     protected List<Pair<String, ATerm>> matchReal(IContext env, ATermReal t,
             ATermAppl p) throws FatalError {
 
         if (Tools.isAnno(p)) {
             return matchRealAnno(env, t, p);
         } else if (Tools.isInt(p)) {
            return null;
        } else if (Tools.isReal(p)) {
            return matchRealReal(t, p);
         } else if (Tools.isVar(p)) {
             return matchRealVar(t, p);
         } else if (Tools.isOp(p)) {
             return null;
         } else if (Tools.isExplode(p)) {
             return matchRealExplode(env, p);
         } else if (Tools.isWld(p)) {
             return matchRealWld(p);
         } else if (Tools.isAs(p)) {
             return matchRealAs(t, p);
         }
 
         throw new FatalError("Unknown Real case '" + p + "'");
     }
 
    private List<Pair<String, ATerm>> matchRealReal(ATermReal t, ATermAppl p) {
        Double realVal = new Double(Tools.stringAt(p, 0));
        if (realVal == t.getReal())
            return emptyList();
        return null;
    }

     private List<Pair<String, ATerm>> matchRealWld(ATermAppl p) {
         return emptyList();
     }
 
     private List<Pair<String, ATerm>> matchIntWld(ATermAppl p) {
         return emptyList();
     }
 
     protected List<Pair<String, ATerm>> matchIntAnno(IContext env, ATermInt t,
             ATermAppl p) throws FatalError {
         // FIXME: Do real match of annotations
         return match(env, t, Tools.applAt(p, 0));
     }
 
     protected List<Pair<String, ATerm>> matchRealAnno(IContext env, ATermInt t,
             ATermAppl p) throws FatalError {
         // FIXME: Do real match of annotations
         return match(env, t, Tools.applAt(p, 0));
     }
 
     protected List<Pair<String, ATerm>> matchRealAnno(IContext env,
             ATermReal t, ATermAppl p) throws FatalError {
         return match(env, t, Tools.applAt(p, 0));
     }
 
     protected List<Pair<String, ATerm>> matchIntInt(ATermInt t, ATermAppl p) {
         Integer intVal = new Integer(Tools.stringAt(p, 0));
         if (intVal == t.getInt())
             return emptyList();
         return null;
     }
 
     protected List<Pair<String, ATerm>> matchIntVar(ATermInt t, ATermAppl p) {
         List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
         r.add(new Pair<String, ATerm>(((ATermAppl) p.getChildAt(0)).getName(),
                                       t));
         return r;
     }
 
     protected List<Pair<String, ATerm>> matchRealVar(ATermReal t, ATermAppl p) {
         List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
         r.add(new Pair<String, ATerm>(((ATermAppl) p.getChildAt(0)).getName(),
                                       t));
         return r;
     }
 
     protected List<Pair<String, ATerm>> matchIntAs(ATermInt t, ATermAppl p) {
         List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
         String varName = Tools.stringAt(Tools.applAt(p, 0), 0);
         r.add(new Pair<String, ATerm>(varName, t));
         return r;
     }
 
     protected List<Pair<String, ATerm>> matchRealAs(ATermReal t, ATermAppl p) {
         List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
         String varName = Tools.stringAt(Tools.applAt(p, 0), 0);
         r.add(new Pair<String, ATerm>(varName, t));
         return r;
     }
 
     protected List<Pair<String, ATerm>> matchIntExplode(IContext env,
             ATermAppl p) {
         if (Tools.isWld(Tools.applAt(p, 0))) {
             List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
             String varName = Tools.stringAt(Tools.applAt(p, 1), 0);
             r.add(new Pair<String, ATerm>(varName, env.makeList()));
             return r;
         } else
             return null;
     }
 
     protected List<Pair<String, ATerm>> matchRealExplode(IContext env,
             ATermAppl p) {
         if (Tools.isWld(Tools.applAt(p, 0))) {
             List<Pair<String, ATerm>> r = new ArrayList<Pair<String, ATerm>>();
             String varName = Tools.stringAt(Tools.applAt(p, 1), 0);
             r.add(new Pair<String, ATerm>(varName, env.makeList()));
             return r;
         } else
             return null;
     }
 
     public List<Pair<String, ATerm>> match(IContext env, ATerm t, ATermAppl p)
             throws FatalError {
 
         switch (t.getType()) {
         case ATerm.APPL:
             return matchAppl(env, (ATermAppl) t, p);
         case ATerm.INT:
             return matchInt(env, (ATermInt) t, p);
         case ATerm.REAL:
             return matchReal(env, (ATermReal) t, p);
         default:
             throw new FatalError("Unsupported term type : "
                     + t.getClass().toString() + " [" + t.getType() + "]");
         }
     }
 }
