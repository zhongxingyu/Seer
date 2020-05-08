 /*
  * Created on 3. okt.. 2006
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.spoofax.DebugUtil;
 import org.spoofax.interpreter.stratego.All;
 import org.spoofax.interpreter.stratego.Build;
 import org.spoofax.interpreter.stratego.CallT;
 import org.spoofax.interpreter.stratego.ExtSDef;
 import org.spoofax.interpreter.stratego.Fail;
 import org.spoofax.interpreter.stratego.GuardedLChoice;
 import org.spoofax.interpreter.stratego.Id;
 import org.spoofax.interpreter.stratego.Let;
 import org.spoofax.interpreter.stratego.Match;
 import org.spoofax.interpreter.stratego.One;
 import org.spoofax.interpreter.stratego.OpDecl;
 import org.spoofax.interpreter.stratego.PrimT;
 import org.spoofax.interpreter.stratego.SDefT;
 import org.spoofax.interpreter.stratego.Scope;
 import org.spoofax.interpreter.stratego.Seq;
 import org.spoofax.interpreter.stratego.Some;
 import org.spoofax.interpreter.stratego.Strategy;
 import org.spoofax.interpreter.stratego.SDefT.ArgType;
 import org.spoofax.interpreter.stratego.SDefT.ConstType;
 import org.spoofax.interpreter.stratego.SDefT.FunType;
 import org.spoofax.interpreter.stratego.SDefT.SVar;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.StrategoSignature;
 
 public class StrategoCoreLoader {
 
     private Context context;
     
     StrategoCoreLoader(Context context) {
         this.context = context;
     }
     
     private ExtSDef parseExtSDef(IStrategoAppl t) {
 
         String name = Tools.javaStringAt(t, 0);
         IStrategoList svars = Tools.listAt(t, 1);
         IStrategoList tvars = Tools.listAt(t, 2);
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug("name  : ", name);
         }
 
         SVar[] realsvars = makeSVars(svars);
         String[] realtvars = makeVars(tvars);
 
         VarScope oldScope = context.getVarScope();
         VarScope newScope = new VarScope(oldScope);
 
         return new ExtSDef(name, realsvars, realtvars, newScope);
     }
 
     private Strategy parseStrategy(IStrategoAppl appl) throws InterpreterException {
 
         IStrategoConstructor ctor = appl.getConstructor();
         StrategoSignature sign = context.getStrategoSignature();
 
         if (ctor.equals(sign.getBuild())) {
             return parseBuild(appl);
         } else if (ctor.equals(sign.getScope())) {
             return parseScope(appl);
         } else if (ctor.equals(sign.getSeq())) {
             return parseSeq(appl);
         } else if (ctor.equals(sign.getGuardedLChoice())) {
             return parseGuardedLChoice(appl);
         } else if (ctor.equals(sign.getMatch())) {
             return parseMatch(appl);
         } else if (ctor.equals(sign.getId())) {
             return parseId(appl);
         } else if (ctor.equals(sign.getCallT())) {
             return parseCallT(appl);
         } else if (ctor.equals(sign.getPrimT())) {
             return parsePrimT(appl);
         } else if (ctor.equals(sign.getLet())) {
             return parseLet(appl);
         } else if (ctor.equals(sign.getFail())) {
             return makeFail(appl);
         } else if (ctor.equals(sign.getId())) {
             return makeId(appl);
         } else if (ctor.equals(sign.getAll())) {
             return makeAll(appl);
         } else if (ctor.equals(sign.getOne())) {
             return makeOne(appl);
         } else if (ctor.equals(sign.getSome())) {
             return makeSome(appl);
         }
 
         throw new InterpreterException("Unknown op '" + ctor + "'");
     }
 
     private Strategy makeId(IStrategoAppl appl) {
         return new Id();
     }
 
     private Some makeSome(IStrategoAppl t) throws InterpreterException {
         Strategy body = parseStrategy(Tools.applAt(t, 0));
         return new Some(body);
     }
 
     private One makeOne(IStrategoAppl t) throws InterpreterException {
         Strategy body = parseStrategy(Tools.applAt(t, 0));
         return new One(body);
     }
 
     private All makeAll(IStrategoAppl t) throws InterpreterException {
         Strategy body = parseStrategy(Tools.applAt(t, 0));
         return new All(body);
     }
 
     private Strategy makeFail(IStrategoAppl appl) {
         return new Fail();
     }
 
     private Let parseLet(IStrategoAppl t) throws InterpreterException {
 
         IStrategoTerm[] l = Tools.listAt(t, 0).getAllSubterms();
         SDefT[] defs = new SDefT[l.length];
 
         for (int i = 0; i < l.length; i++) {
             defs[i] = parseSDefT((IStrategoAppl)l[i]);
         }
 
         Strategy body = parseStrategy(Tools.applAt(t, 1));
 
         return new Let(defs, body);
     }
 
     private SDefT parseSDefT(IStrategoAppl t) throws InterpreterException {
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug("parseSDefT()");
         }
 
         String name = Tools.javaStringAt(t, 0);
         IStrategoList svars = Tools.listAt(t, 1);
         IStrategoList tvars = Tools.listAt(t, 2);
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" name  : ", name);
         }
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" svars : ", svars);
         }
         SVar[] realsvars = makeSVars(svars);
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" svars : ", realsvars);
         }
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" tvars : ", tvars);
         }
         
         String[] realtvars = makeVars(tvars);
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" tvars : ", realtvars);
         }
 
         VarScope newScope = new VarScope(context.getVarScope());
 
         context.setVarScope(newScope);
         Strategy body = parseStrategy(Tools.applAt(t, 3));
 
         context.popVarScope();
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" +name: ", name);
         }
 
         return new SDefT(name, realsvars, realtvars, body, newScope);
     }
 
     private String[] makeVars(IStrategoList svars) {
 
         IStrategoTerm[] sv = svars.getAllSubterms();
         String[] realsvars = new String[sv.length];
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" vars  : ", svars);
         }
 
         for (int j = 0; j < svars.size(); j++) {
             realsvars[j]  = Tools.javaStringAt(sv[j], 0);
         }
 
         return realsvars;
     }
 
     private SVar[] makeSVars(IStrategoList svars) {
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug("makeSVars()");
         }
 
         IStrategoTerm[] sv = svars.getAllSubterms();
         SVar[] realsvars = new SVar[sv.length];
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" vars  : ", svars);
         }
 
         for (int j = 0; j < sv.length; j++) {
             IStrategoAppl t = (IStrategoAppl) sv[j];
             ArgType type = parseArgType(Tools.applAt(t, 1));
             String name = Tools.javaStringAt(t, 0);
             realsvars[j] = new SVar(name, type);
         }
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug("       : ", realsvars);
         }
         return realsvars;
     }
 
     private ArgType parseArgType(IStrategoAppl t) {
         if(Tools.isFunType(t, context)) {
             IStrategoList l = Tools.listAt(t, 0);
             List<ArgType> ch = new ArrayList<ArgType>();
             for (int i = 0; i < l.size(); i++) {
                 ch.add(parseArgType(Tools.applAt(l, i)));
             }
             return new FunType(ch);
         } else if (Tools.isConstType(t, context)) {
             return new ConstType();
         }
         return null;
     }
 
     private PrimT parsePrimT(IStrategoAppl t) throws InterpreterException {
 
         String name = Tools.javaStringAt(t, 0);
         Strategy[] svars = parseStrategyList(Tools.listAt(t, 1));
         IStrategoTerm[] tvars = parseTermList(Tools.listAt(t, 2));
 
         return new PrimT(name, svars, tvars);
     }
 
     private Strategy parseCallT(IStrategoAppl t) throws InterpreterException {
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug("parseCallT()");
         }
         String name = Tools.javaStringAt(Tools.applAt(t, 0), 0);
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" name  : ", name);
         }
 
         IStrategoList svars = Tools.listAt(t, 1);
         Strategy[] realsvars = parseStrategyList(svars);
 
         IStrategoTerm[] realtvars = parseTermList(Tools.listAt(t, 2));
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" -svars : ", realsvars);
         }
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(" -tvars : ", realtvars);
         }
         return new CallT(name, realsvars, realtvars);
     }
 
     private IStrategoTerm[] parseTermList(IStrategoList tvars) {
         return tvars.getAllSubterms();
     }
 /*
         IStrategoTerm[] tv = tvars.getAllSubterms()
         List<IStrategoTerm> v = new IStrategoTerm[.size());
         for (int i = 0; i < tvars.size(); i++) {
             v.add(Tools.termAt(tvars, i));
         }
         return v;
     }
 */
     private Strategy[] parseStrategyList(IStrategoList svars) throws InterpreterException {
         IStrategoTerm[] sv = svars.getAllSubterms();
         Strategy[] v = new Strategy[sv.length];
         for (int i = 0; i < sv.length; i++) {
             v[i] = parseStrategy((IStrategoAppl)sv[i]);
         }
         return v;
     }
 
     private Id parseId(IStrategoAppl t) {
         return new Id();
     }
 
     private Match parseMatch(IStrategoAppl t) {
         IStrategoAppl u = Tools.applAt(t, 0);
         return new Match(u);
     }
 
     @SuppressWarnings("unchecked")
     private GuardedLChoice parseGuardedLChoice(IStrategoAppl t) throws InterpreterException {
 
     	LinkedList<Pair<Strategy,Strategy>> s = new LinkedList<Pair<Strategy,Strategy>>();
         IStrategoConstructor ctor = context.getStrategoSignature().getGuardedLChoice();
 
     	while (t.getConstructor().equals(ctor)) {
           s.add(new Pair<Strategy,Strategy>(parseStrategy(Tools.applAt(t, 0)), parseStrategy(Tools.applAt(t, 1))));
           t = Tools.applAt(t, 2);
     	}
 
     	s.add(new Pair<Strategy,Strategy>(parseStrategy(t), null));
 
         return new GuardedLChoice(s.toArray(new Pair[0]));
     }
 
     private Seq parseSeq(IStrategoAppl t) throws InterpreterException {
     	LinkedList<Strategy> s = new LinkedList<Strategy>();
         StrategoSignature sign = context.getStrategoSignature();
     	
     	while (t.getConstructor().equals(sign.getSeq())) {
           s.add(parseStrategy(Tools.applAt(t, 0)));
           t = Tools.applAt(t, 1);
     	}
 
     	s.add(parseStrategy(t));
 
         return new Seq(s.toArray(new Strategy[0]));
     }
 
     private Scope parseScope(IStrategoAppl t) throws InterpreterException {
 
         IStrategoList vars = Tools.listAt(t, 0);
         List<String> realvars = new ArrayList<String>(vars.size());
 
         for (int i = 0; i < vars.size(); i++) {
             realvars.add(Tools.javaStringAt(vars, i));
         }
 
         Strategy body = parseStrategy(Tools.applAt(t, 1));
 
         return new Scope(realvars, body);
     }
 
     private Build parseBuild(IStrategoAppl t) {
         IStrategoAppl u = Tools.applAt(t, 0);
         return new Build(u);
     }
     
     public void load(String path) throws IOException, InterpreterException {
        doLoad(context.getProgramFactory().parseFromFile(path));
     }
     
     public void load(IStrategoTerm prg) throws InterpreterException {
     	doLoad(prg);
     }
 
     private void doLoad(IStrategoTerm prg) throws InterpreterException {
         
         IStrategoAppl sign = Tools.applAt(Tools.listAt(prg, 0), 0);
         IStrategoAppl strats = Tools.applAt(Tools.listAt(prg, 0), 1);
 
         if (DebugUtil.isDebugging()) {
             DebugUtil.debug(prg);
         }
 
         loadConstructors(Tools.listAt(Tools.applAt(Tools.listAt(sign, 0), 0), 0));
         loadStrategies(Tools.listAt(strats, 0));
     }
 
     private void loadConstructors(IStrategoList list) {
         for (int i = 0; i < list.size(); i++) {
             String name = Tools.javaStringAt(Tools.applAt(list, i), 0);
             context.addOpDecl(name, new OpDecl(name));
         }
     }
 
     private void loadStrategies(IStrategoList list) throws InterpreterException {
         for (int i = 0; i < list.size(); i++) {
             IStrategoAppl t = Tools.applAt(list, i);
             if(Tools.isSDefT(t, context)) {
                 SDefT def = parseSDefT(t);
                 context.addSVar(def.getName(), def);
             } else if(Tools.isExtSDef(t, context)) {
                 ExtSDef def = parseExtSDef(t);
                 context.addSVar(def.getName(), def);
                 // FIXME: Come up with a good solution for external
                 // definitions
                 throw new InterpreterException("Illegal ExtSDef in StrategoCore file");
             }
         }
 
     }
 
     public void load(InputStream stream) throws InterpreterException, IOException {
         doLoad(context.getFactory().parseFromStream(stream));
     }
 
 }
