 package com.github.stefanliebenberg.compiler.soy;
 
 
 import com.google.common.collect.*;
 import com.google.javascript.jscomp.Compiler;
 import com.google.javascript.jscomp.*;
 import com.google.javascript.rhino.Node;
 import com.google.javascript.rhino.Token;
 
 import java.util.HashMap;
 import java.util.HashSet;
import java.util.Map;
 
 /**
  * @author Johannes Nel
  * @author Stefan Liebenberg
  */
 public class SoyDelegateOptimizer implements CompilerPass {
 
     private Compiler compiler;
 
     private final DelegateFinder delegateFinder = new DelegateFinder();
 
     private final DelegateOptimiser delegateOptimizer = new DelegateOptimiser();
 
 
     public SoyDelegateOptimizer(final Compiler compiler) {
         this.compiler = compiler;
     }
 
     private final HashMap<String, Double> dataMap =
             new HashMap<String, Double>();
 
     private final HashMap<String, String> usedFunctionMap =
             new HashMap<String, String>();
 
     private final HashSet<String> allFunctions =
             new HashSet<String>();
 
     protected void reset() {
         dataMap.clear();
         usedFunctionMap.clear();
         allFunctions.clear();
         frozenAllFunctions = null;
         frozenUsedFunctions = null;
         frozenDataMap = null;
     }
 
     private ImmutableMap<String, Double> frozenDataMap;
     private ImmutableCollection<String> frozenUsedFunctions;
     private ImmutableCollection<String> frozenAllFunctions;
 
     protected void freeze() {
         frozenDataMap = ImmutableMap.copyOf(dataMap);
         frozenUsedFunctions = ImmutableList.copyOf(usedFunctionMap.values());
         frozenAllFunctions = ImmutableList.copyOf(allFunctions);
     }
 
     @Override
     public void process(final Node externs, final Node root) {
         reset();
         NodeTraversal.traverse(compiler, root, delegateFinder);
 
 
         freeze();
         NodeTraversal.traverse(compiler, root, delegateOptimizer);
     }
 
     private void remember(String key, Double priority, String templateName) {
         dataMap.put(key, priority);
         usedFunctionMap.put(key, templateName);
         allFunctions.add(templateName);
     }
 
     private class DelegateFinder extends NodeTraversal
             .AbstractPostOrderCallback {
 
         @Override
         public void visit(final NodeTraversal t, final Node n,
                           final Node parent) {
             if (isDelegateCallNode(n)) {
                 Double priority = getPriority(n);
                 String key = getDelegateId(n);
                 Double currentPriorityInMap = dataMap.get(key);
                 if (currentPriorityInMap == null ||
                         currentPriorityInMap < priority) {
                     remember(key, priority, n.getChildAtIndex(4)
                             .getQualifiedName());
                 }
 
             }
         }
     }
 
     public class DelegateOptimiser extends NodeTraversal
             .AbstractPostOrderCallback {
 
         @Override
         public void visit(final NodeTraversal t,
                           final Node n,
                           final Node parent) {
             switch (n.getType()) {
                 case Token.CALL:
                     if (isDelegateCallNode(n)) {
                         Double priority = getPriority(n);
                         String key = getDelegateId(n);
                         Double highestPriorityInMap = frozenDataMap.get(key);
                         if (priority < highestPriorityInMap) {
                             parent.detachFromParent();
                             compiler.reportCodeChange();
                         }
                     }
                     break;
                 case Token.FUNCTION:
                     Object property = n.getProp(Node.ORIGINALNAME_PROP);
                     if (property != null) {
                         String qualifiedName = property.toString();
                         if (frozenAllFunctions.contains(qualifiedName)
                                 && !frozenUsedFunctions.contains
                                 (qualifiedName)) {
                             parent.getParent().detachFromParent();
                             compiler.reportCodeChange();
                         }
                     }
                     break;
             }
 
 
         }
     }
 
     static private String getName(final Node node) {
         return node.getFirstChild().getNext().getLastChild()
                 .getString();
     }
 
     static private String getVariant(final Node node) {
         return node.getChildAtIndex(1).getNext().getString();
     }
 
     static private Double getPriority(final Node node) {
         return node.getChildAtIndex(3).getDouble();
     }
 
     static private String getKey(final String name, final String variant) {
         return name + ":" + variant;
     }
 
     static private String getDelegateId(final Node node) {
         return getKey(getName(node), getVariant(node));
     }
 
     static private final String DELEGATE_FN_NAME = "soy.$$registerDelegateFn";
 
     static private Boolean isDelegateCallNode(final Node node) {
         return node.getType() == Token.CALL &&
                 DELEGATE_FN_NAME.equals(node.getFirstChild()
                         .getQualifiedName());
     }
 
 
     public static void addToCompile(final Compiler compiler,
                                     final CompilerOptions compilerOptions) {
         Multimap<CustomPassExecutionTime, CompilerPass> customPasses =
                 compilerOptions.customPasses;
         if (customPasses == null) {
             customPasses = LinkedListMultimap.create();
         }
         customPasses.put(CustomPassExecutionTime.BEFORE_CHECKS,
                 new SoyDelegateOptimizer(compiler));
         compilerOptions.setCustomPasses(customPasses);
     }
 }
