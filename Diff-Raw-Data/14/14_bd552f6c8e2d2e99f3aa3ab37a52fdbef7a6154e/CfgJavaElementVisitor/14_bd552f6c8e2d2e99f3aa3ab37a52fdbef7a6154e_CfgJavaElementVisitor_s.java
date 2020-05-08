 package com.github.avereshchgin.alvor.cfg;
 
 import com.github.avereshchgin.alvor.verification.VerifiableMethodsFinder;
 import com.intellij.psi.*;
 
 import java.util.*;
 
 public class CfgJavaElementVisitor extends JavaElementVisitor {
 
     private final List<CfgStatement> previousNodes;
 
     private final ControlFlowGraphBuilder cfgBuilder;
 
     private final ControlFlowGraph cfg;
 
     private final VerifiableMethodsFinder finder;
 
     private final Map<PsiIdentifier, CfgStatement> externalLabels;
 
     private PsiIdentifier lastLabel;
 
    private boolean breaked;
 
     private CfgJavaElementVisitor(ControlFlowGraphBuilder cfgBuilder, List<CfgStatement> previousNodes,
                                   Map<PsiIdentifier, CfgStatement> externalLabels) {
         this.cfgBuilder = cfgBuilder;
         this.cfg = cfgBuilder.getControlFlowGraph();
         this.finder = cfgBuilder.getFinder();
         this.previousNodes = new ArrayList<CfgStatement>(previousNodes);
         this.externalLabels = externalLabels;
     }
 
     public static CfgJavaElementVisitor processCodeBlock(ControlFlowGraphBuilder cfgBuilder,
                                                          List<CfgStatement> previousNodes,
                                                          PsiCodeBlock codeBlock,
                                                          Map<PsiIdentifier, CfgStatement> externalLabels) {
         CfgJavaElementVisitor elementVisitor = new CfgJavaElementVisitor(cfgBuilder, previousNodes, externalLabels);
         if (codeBlock != null) {
             for (PsiStatement statement : codeBlock.getStatements()) {
                if (elementVisitor.breaked) {
                     break;
                 }
                 statement.accept(elementVisitor);
             }
         }
         return elementVisitor;
     }
 
     public static CfgJavaElementVisitor processStatement(ControlFlowGraphBuilder cfgBuilder,
                                                          List<CfgStatement> previousNodes,
                                                          PsiStatement statement,
                                                          Map<PsiIdentifier, CfgStatement> externalLabels) {
         CfgJavaElementVisitor elementVisitor = new CfgJavaElementVisitor(cfgBuilder, previousNodes, externalLabels);
         if (statement != null) {
             statement.accept(elementVisitor);
         }
         return elementVisitor;
     }
 
     public List<CfgStatement> getPreviousNodes() {
         return Collections.unmodifiableList(previousNodes);
     }
 
     @Override
     public void visitBlockStatement(PsiBlockStatement statement) {
         log("Block statement");
         for (PsiStatement innerStatement : statement.getCodeBlock().getStatements()) {
            if (breaked) {
                 break;
             }
             innerStatement.accept(this);
         }
     }
 
     @Override
     public void visitEmptyStatement(PsiEmptyStatement statement) {
         log("Empty statement");
     }
 
     @Override
     public void visitReturnStatement(PsiReturnStatement statement) {
         log("Return statement");
         CfgStatement currentNode = new CfgReturnStatement();
         cfg.addNode(currentNode);
         cfg.addEdges(previousNodes, currentNode);
         previousNodes.clear();
     }
 
     @Override
     public void visitDeclarationStatement(PsiDeclarationStatement statement) {
         log("Declaration statement");
         // TODO: rewrite code
     }
 
     @Override
     public void visitExpressionStatement(PsiExpressionStatement statement) {
         log("Expression statement");
         statement.getExpression().accept(new JavaElementVisitor() {
             @Override
             public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                 PsiExpression[] argumentsExpressions = expression.getArgumentList().getExpressions();
                 if (argumentsExpressions.length > 0) {
                     // TODO: check if expression has String type
                     CfgRegularStatement currentNode = new CfgRegularStatement(argumentsExpressions[0]);
                     currentNode.setVerificationRequired(finder.isVerificationRequired(expression.resolveMethod()));
                     cfg.addNode(currentNode);
                     cfg.addEdges(previousNodes, currentNode);
                     previousNodes.clear();
                     previousNodes.add(currentNode);
                 }
             }
 
             @Override
             public void visitExpression(PsiExpression expression) {
                 CfgRegularStatement currentNode = new CfgRegularStatement(expression);
                 cfg.addNode(currentNode);
                 cfg.addEdges(previousNodes, currentNode);
                 previousNodes.clear();
                 previousNodes.add(currentNode);
             }
         });
     }
 
     @Override
     public void visitForStatement(PsiForStatement statement) {
         log("For statement");
     }
 
     @Override
     public void visitForeachStatement(PsiForeachStatement statement) {
         log("Foreach statement");
     }
 
     @Override
     public void visitIfStatement(PsiIfStatement statement) {
         log("If statement");
         CfgStatement currentNode = new CfgRegularStatement(statement.getCondition());
         cfg.addNode(currentNode);
         cfg.addEdges(previousNodes, currentNode);
         previousNodes.clear();
         previousNodes.addAll(CfgJavaElementVisitor.processStatement(
                 cfgBuilder, Collections.singletonList(currentNode), statement.getThenBranch(), externalLabels
         ).getPreviousNodes());
         previousNodes.addAll(CfgJavaElementVisitor.processStatement(
                 cfgBuilder, Collections.singletonList(currentNode), statement.getElseBranch(), externalLabels
         ).getPreviousNodes());
     }
 
     @Override
     public void visitWhileStatement(PsiWhileStatement statement) {
         log("While statement");
         CfgStatement currentNode = new CfgRegularStatement(statement.getCondition());
 
         log("Last label: " + (lastLabel != null ? lastLabel.getText() : "null"));
 
         Map<PsiIdentifier, CfgStatement> labels = new IdentityHashMap<PsiIdentifier, CfgStatement>(externalLabels);
         labels.put(lastLabel, currentNode);
         lastLabel = null;
 
         cfg.addNode(currentNode);
         cfg.addEdges(previousNodes, currentNode);
         previousNodes.clear();
         previousNodes.addAll(CfgJavaElementVisitor.processStatement(
                 cfgBuilder, Collections.singletonList(currentNode), statement.getBody(), labels
         ).getPreviousNodes());
         // back edges
         cfg.addEdges(previousNodes, currentNode);
     }
 
     @Override
     public void visitDoWhileStatement(PsiDoWhileStatement statement) {
         log("Do-while statement");
 
         CfgStatement currentNode = new CfgRegularStatement(statement.getCondition());
         currentNode.setAtLeastOnce(true);
 
         log("Last label: " + (lastLabel != null ? lastLabel.getText() : "null"));
 
         Map<PsiIdentifier, CfgStatement> labels = new IdentityHashMap<PsiIdentifier, CfgStatement>(externalLabels);
         labels.put(lastLabel, currentNode);
         lastLabel = null;
 
         cfg.addNode(currentNode);
         cfg.addEdges(previousNodes, currentNode);
         previousNodes.clear();
         previousNodes.addAll(CfgJavaElementVisitor.processStatement(
                 cfgBuilder, Collections.singletonList(currentNode), statement.getBody(), labels
         ).getPreviousNodes());
         // back edges
         cfg.addEdges(previousNodes, currentNode);
     }
 
     @Override
     public void visitContinueStatement(PsiContinueStatement statement) {
         log("Continue statement: " + statement.getText());
         PsiIdentifier labelIdentifier = statement.getLabelIdentifier();
         log(externalLabels.toString());
         CfgStatement labeledNode = externalLabels.get(labelIdentifier);
         if (labeledNode != null) {
             log(previousNodes.toString() + " " + labeledNode);
 
             cfg.addEdges(previousNodes, labeledNode);
             previousNodes.clear();
             previousNodes.add(labeledNode);
         }
        breaked = true;
     }
 
     @Override
     public void visitBreakStatement(PsiBreakStatement statement) {
         log("Break statement: " + statement.getText());
        breaked = true;
     }
 
     @Override
     public void visitLabeledStatement(PsiLabeledStatement statement) {
         log("Labeled statement");
         PsiStatement childStatement = statement.getStatement();
         if (childStatement != null) {
             lastLabel = statement.getLabelIdentifier();
             childStatement.accept(this);
         }
     }
 
     @Override
     public void visitSwitchStatement(PsiSwitchStatement statement) {
         log("Switch statement");
     }
 
     @Override
     public void visitSwitchLabelStatement(PsiSwitchLabelStatement statement) {
         log("Switch label statement: " + statement.getText());
     }
 
     @Override
     public void visitStatement(PsiStatement statement) {
         log("Unknown statement: " + statement);
     }
 
     private void log(String message) {
 //        System.out.println(message);
     }
 }
