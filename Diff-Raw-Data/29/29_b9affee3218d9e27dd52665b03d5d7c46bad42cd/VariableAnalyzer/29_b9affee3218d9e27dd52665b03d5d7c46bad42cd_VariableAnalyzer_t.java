 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.sc;
 import java.util.*;
 import org.openlaszlo.sc.parser.SimpleNode;
 import org.openlaszlo.sc.parser.*;
 
 public class VariableAnalyzer {
   // A ref will cause an auto_reg to be declared
   // compiler magic variables (e.g., $flasm) are always "available"
   static Set AVAILABLE = new HashSet(Instructions.Register.AUTO_REG);
   static {
     AVAILABLE.add("$flasm");
   }
 
   // _Must_ be in order
   public LinkedHashSet parameters;
   // Kept in order for deterministic code generation
   public LinkedHashSet variables;
   public LinkedHashMap fundefs;
   // Order unimportant for the rest
   public Map used;
   Set innerFree;
   public Set closed;
   public Set free;
   // Contains `.`, `[]`, or `()` expression(s)
   public boolean dereferenced = false;
 
   boolean ignoreFlasm;
  boolean hasSuper;
   Set locals;
 
  public VariableAnalyzer(SimpleNode params, boolean ignoreFlasm, boolean hasSuper) {
     // Parameter order is significant
     parameters = new LinkedHashSet();
     for (int i = 0, len = params.size(); i < len; i++) {
       SimpleNode param = params.get(i);
       // might also be an initializer
       // TODO: [2007-12-20 dda] if it is an default parameter initializer,
       // should call visit() on the initializer expression?
       if (param instanceof ASTIdentifier) {
         parameters.add(((ASTIdentifier)param).getName());
       }
     }
     locals = new LinkedHashSet(parameters);
     innerFree = new HashSet();
     fundefs = new LinkedHashMap();
     used = new HashMap();
     this.ignoreFlasm = ignoreFlasm;
   }
 
  public VariableAnalyzer(SimpleNode params, boolean ignoreFlasm) {
    this(params, ignoreFlasm, false);
  }

   public void incrementUsed(String variable) {
     String var = variable.intern();
     if (used.containsKey(var)) {
       // Gaak!
       used.put(var, new Integer(((Integer)used.get(var)).intValue() + 1));
     } else {
       used.put(var, new Integer(1));
     }
   }
 
   // Computes parameters, variables, fundefs, used, closed, free
   public void computeReferences() {
     // variables (locals - parameters)
     variables = new LinkedHashSet(locals);
     variables.removeAll(parameters);
     // available (locals + AVAILABLE)
     Set available = new HashSet(locals);
     available.addAll(AVAILABLE);
     // Closing over a variable counts as a use
     for (Iterator i = innerFree.iterator(); i.hasNext(); ) {
       incrementUsed((String)i.next());
     }
     // Calculate actual closed (innerFree & available)
     closed = new HashSet(innerFree);
     closed.retainAll(available);
     // Calculate free references (used - available)
     free = new HashSet(used.keySet());
     free.removeAll(available);
   }
 
   public void visit(SimpleNode node) {
     SimpleNode[] children;
     // Calculate children for recursive visiting
     if (node instanceof ASTPropertyIdentifierReference) {
       // For `a.b`, only `a` is a reference, not `b`.  (Cf., `a[b]`,
       // where _both_ are references).
       SimpleNode[] c = {node.get(0)};
       children = c;
     } else if (node instanceof ASTIfStatement) {
       // Don't analyze flasm
       SimpleNode test = node.get(0);
       if (test instanceof ASTIdentifier &&
           ("$flasm".equals(((ASTIdentifier)test).getName())) &&
           ignoreFlasm) {
         SimpleNode[] c = {test};
         children = c;
       } else {
         children = node.getChildren();
       }
     } else {
       children = node.getChildren();
     }
     // Calculate locals, fundefs, and used
     // (ForVar has a VariableDeclaration as a child, so we don"t
     // need to handle it specially, but ForVarIn does not.)
     if (node instanceof ASTVariableDeclaration ||
         node instanceof ASTForVarInStatement) {
       String v = ((ASTIdentifier)children[0]).getName();
       // In ECMAscript you can re-declare variables and
       // parameters and not shadow them
       if (! locals.contains(v)) {
         locals.add(v);
       }
     } else if (node instanceof ASTFunctionDeclaration) {
       String v = ((ASTIdentifier)children[0]).getName();
       fundefs.put(v, node);
       if (! locals.contains(v)) {
         locals.add(v);
       }
     } else if (node instanceof ASTThisReference ||
                node instanceof ASTIdentifier) {
       if (node instanceof ASTThisReference) {
         incrementUsed("this");
       } else {
         incrementUsed(((ASTIdentifier)node).getName());
       }
     }
     // Calculate dereferenced
     if ((node instanceof ASTPropertyIdentifierReference) ||
         (node instanceof ASTPropertyValueReference) ||
         (node instanceof ASTCallExpression)) {
       dereferenced = true;
     }
    // For JS1 runtimes, super calls are translated into runtime calls
    // that reference `this` and `arguments`.  Chicken/egg problem --
    // we'd like to translate before analyzing, but translation uses
    // analysis.  We need a separate transformer phase...
    if ((node instanceof ASTSuperCallExpression) &&
         (! hasSuper)) {
      incrementUsed("this");
      incrementUsed("arguments");
    }
     // Now descend into children.  Closures get special treatment.
     if (node instanceof ASTFunctionDeclaration ||
         node instanceof ASTFunctionExpression) {
       SimpleNode params = children[children.length - 2];
       SimpleNode stmts = children[children.length - 1];
       VariableAnalyzer analyzer = new VariableAnalyzer(params, ignoreFlasm);
       for (int i = 0, len = stmts.size(); i < len; i++) {
         SimpleNode stmt = stmts.get(i);
         analyzer.visit(stmt);
       }
       analyzer.computeReferences();
       for (Iterator i = analyzer.free.iterator(); i.hasNext(); ) {
         String v = (String)i.next();
         if (! innerFree.contains(v)) {
           innerFree.add(v);
         }
       }
     } else {
       for (int i = 0; i < children.length; i++) {
         visit(children[i]);
       }
     }
   }
 }
