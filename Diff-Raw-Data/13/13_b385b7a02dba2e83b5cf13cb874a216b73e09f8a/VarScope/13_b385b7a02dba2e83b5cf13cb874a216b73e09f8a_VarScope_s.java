 /*
  * Created on 26.jun.2005
  *
  * Copyright (c) 2004, Karl Trygve Kalleberg <karltk near strategoxt.org>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.core;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.spoofax.DebugUtil;
 import org.spoofax.interpreter.stratego.SDefT;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class VarScope {
 
     private VarScope parent;
 
     private Map<String, IStrategoTerm> vars;
 
     private Map<String, SDefT> svars;
 
     public VarScope(VarScope parent) {
         this.parent = parent;
 
         vars = new HashMap<String, IStrategoTerm>(0); //todo: create these on demand
         svars = new HashMap<String, SDefT>(0);
     }
 
     public IStrategoTerm lookup(String name) {
 
         IStrategoTerm t = vars.get(name);
 
         if (t == null && parent != null)
             t = parent.lookup(name);
 
         if(DebugUtil.isDebugging()) {
             Context.debug(DebugUtil.buildIndent(DebugUtil.INDENT_STEP), "lookup : ", name, " = ", t);
         }
 
         return t;
     }
 
     public SDefT lookupSVar(String name) {
 
         SDefT t = svars.get(name);
 
         if (t == null && parent != null)
             return parent.lookupSVar(name);
 
         return t;
     }
 
     public void addSVar(String svar, SDefT strat) {
         svars.put(svar, strat);
     }
 
     public void addSVars(SDefT[] sdefs) {
         for(int i = 0, sz = sdefs.length; i < sz; i++) 
             svars.put(sdefs[i].getName(), sdefs[i]);
     }
 
     public void add(String var, IStrategoTerm t) {
         vars.put(var, t);
     }
 
     public void addVars(List<String> vars) {
         for (String var : vars) {
             this.vars.put(var, null);
         }
     }
 
     public String printVars() {
         StringBuilder sb = new StringBuilder("");
         Iterator<String> it = vars.keySet().iterator();
         while (it.hasNext()) {
             sb.append(it.next());
             if(it.hasNext()) {
                 sb.append(", ");
             }
         }
         sb.append(":");
         return sb.toString();
     }
 
     public boolean hasVarInLocalScope(String name) {
         return vars.containsKey(name) && vars.get(name) != null;
     }
 
     public VarScope getParent() {
         return parent;
     }
 
     public VarScope scopeOf(String name) {
 
         if (vars.containsKey(name))
             return this;
 
         if (parent != null)
             return parent.scopeOf(name);
 
         return null;
     }
 
     public String dump(String prefix, boolean localOnly) {
         String pre = prefix;
 
         if (!localOnly && parent != null) {
             pre = parent.dump(prefix);
         }
 
         if (DebugUtil.isDebugging()) {
             debug(pre, "=== ", this);
 
             for (String t : vars.keySet()) {
                 debug(pre, "[v] ", t, "   ", vars.get(t));
             }
             for (String t : svars.keySet()) {
                 debug(pre, "[s] ", t, "   ", svars.get(t));
             }
             if (svars.size() == 0 && vars.size() == 0) {
                 debug(pre, "<empty>");
             }
         }
         return pre + "  ";
     }
     public String dump(String prefix) {
         return dump(prefix, false);
     }
 
     private void debug(Object... s) {
         DebugUtil.debug(s);
     }
     
     public BindingInfo saveUnboundVars() {
         return saveUnboundVars(new BindingInfo());
     }
 
     private BindingInfo saveUnboundVars(BindingInfo bi) {
         
         for (String k : vars.keySet()) {
             if (vars.get(k) == null)
                 bi.add(this, k);
         }
         
         if (parent != null)
             return parent.saveUnboundVars(bi);
         
         return bi;
     }
 
     public void restoreUnboundVars(BindingInfo bi) {
         
         List<Pair<VarScope, String>> bindings = bi.getBindings();
         
         for (Pair<VarScope, String> p : bindings) {
             p.first.resetVar(p.second);
         }
     }
 
     private void resetVar(String name) {
         vars.put(name, null);
     }
 
     public Collection<SDefT> getStrategyDefinitions() {
         return svars.values();
     }
 }
