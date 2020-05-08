 package com.sdc.util;
 
 import com.sdc.cfg.constructions.ConditionalBlock;
 import com.sdc.cfg.constructions.Construction;
 import com.sdc.cfg.constructions.ElementaryBlock;
 import com.sdc.cfg.nodes.DoWhile;
 import com.sdc.cfg.nodes.Node;
 import com.sdc.cfg.nodes.Switch;
 import com.sdc.cfg.nodes.SwitchCase;
 
 import java.util.List;
 
 public class ConstructionBuilder {
     private List<Node> myNodes;
     private final DominatorTreeGenerator gen;
     private final int size;
     private final int[] domi;
 
     public ConstructionBuilder(final List<Node> myNodes, final DominatorTreeGenerator gen) {
         this.myNodes = myNodes;
         this.gen = gen;
         this.domi = gen.getDomi();
         this.size = myNodes.size();
     }
 
     protected ConstructionBuilder createConstructionBuilder(final List<Node> myNodes, final DominatorTreeGenerator gen) {
         return new ConstructionBuilder(myNodes, gen);
     }
 
     public Construction build() {
         return build(myNodes.get(0));
     }
 
     private Construction build(Node node) {
         final Node doWhileNode = checkForDoWhileLoop(node);
 
         if (doWhileNode == null) {
             Construction elementaryBlock = extractElementaryBlock(node);
             Construction currentConstruction = extractConstruction(node);
 
             if (node.getCondition() == null && !(node instanceof Switch)) {
                 node.setConstruction(elementaryBlock);
             } else {
                 node.setConstruction(currentConstruction);
             }
 
             elementaryBlock.setNextConstruction(currentConstruction);
             return elementaryBlock;
         } else {
             return extractDoWhile(node, doWhileNode);
         }
     }
 
     private Construction extractConstruction(Node node) {
         Construction result;
 
         result = extractConditionBlock(node);
         if (result != null) {
             return result;
         }
 
         result = extractSwitch(node);
         if (result != null) {
             return result;
         }
 
         return null;
     }
 
     private Node checkForDoWhileLoop(final Node node) {
         for (int i = node.getAncestors().size() - 1; i >= 0; i--) {
             Node ancestor = node.getAncestors().get(i);
             if (ancestor instanceof DoWhile && node.getIndex() < ancestor.getIndex() && ancestor.getIndex() >= myNodes.get(0).getIndex() && ancestor.getIndex() <= myNodes.get(size - 1).getIndex()) {
                 return ancestor;
             }
         }
         return null;
     }
 
     private Construction extractDoWhile(Node begin, Node node) {
         ElementaryBlock elementaryBlock = new ElementaryBlock();
         elementaryBlock.setStatements(node.getStatements());
 
         com.sdc.cfg.constructions.DoWhile doWhileConstruction = new com.sdc.cfg.constructions.DoWhile(node.getCondition());
 
         begin.removeAncestor(node);
 
         final int leftBound = getRelativeIndex(begin.getIndex());
         final int rightBound = getRelativeIndex(node.getIndex());
         doWhileConstruction.setBody(createConstructionBuilder(myNodes.subList(leftBound, rightBound), gen).build());
 
         final int nextNodeIndex = node.getIndex() + 1;
         if (nextNodeIndex <= myNodes.get(size - 1).getIndex()) {
             node.setNextNode(myNodes.get(getRelativeIndex(nextNodeIndex)));
         }
 
         elementaryBlock.setNextConstruction(doWhileConstruction);
 
         if (node.getNextNode() != null) {
             extractNextConstruction(doWhileConstruction, node);
         }
 
         return elementaryBlock;
     }
 
     private Construction extractElementaryBlock(final Node node) {
         ElementaryBlock elementaryBlock = new ElementaryBlock();
         elementaryBlock.setStatements(node.getStatements());
         return elementaryBlock;
     }
 
     private Construction extractSwitch(Node node) {
         if (node instanceof Switch) {
             Switch switchNode = (Switch) node;
             com.sdc.cfg.constructions.Switch switchConstruction = new com.sdc.cfg.constructions.Switch(switchNode.getExpr());
 
             Node nextNode = findNextNodeToSwitchWithDefaultCase(switchNode);
 
             if (!switchNode.hasRealDefaultCase()) {
                 switchNode.removeFakeDefaultCase();
                 nextNode = findNextNodeToSwitchWithoutDefaultCase(switchNode);
             }
 
             final List<SwitchCase> switchCases = switchNode.getCases();
 
             for (int i = 0; i < switchCases.size(); i++) {
                 final int leftBound = getRelativeIndex(switchCases.get(i).getCaseBody().getIndex());
                 final int rightBound = i != switchCases.size() - 1
                         ? getRelativeIndex(switchCases.get(i + 1).getCaseBody().getIndex())
                         : nextNode == null ? size : getRelativeIndex(nextNode.getIndex());
 
                 final Construction caseBody = createConstructionBuilder(myNodes.subList(leftBound, rightBound), gen).build();
 
                 com.sdc.cfg.constructions.SwitchCase switchCase = new com.sdc.cfg.constructions.SwitchCase(caseBody);
                 switchCase.setKeys(switchCases.get(i).getKeys());
 
                 switchConstruction.addCase(switchCase);
             }
 
             if (nextNode == null) {
                 addBreakToAllOutgoingLinks();
             } else {
                 addBreakToAncestors(nextNode);
             }
 
             if (nextNode != null && getRelativeIndex(nextNode.getIndex()) < myNodes.size()) {
                 switchNode.setNextNode(nextNode);
                 extractNextConstruction(switchConstruction, switchNode);
             }
 
             return switchConstruction;
         }
         return null;
     }
 
     private Node findNextNode(final Node node) {
         Node result = null;
 
         for (int i = 0; i < domi.length; i++) {
             if (domi[i] == node.getIndex()) {
                 boolean isTail = false;
                 for (final Node tail : node.getListOfTails()) {
                     if (i == tail.getIndex()) {
                         isTail = true;
                         break;
                     }
                 }
                 if (!isTail) {
                     if (getRelativeIndex(i) >= 0 && getRelativeIndex(i) < size) {
                         result = myNodes.get(getRelativeIndex(i));
                     }
                     break;
                 }
             }
         }
 
         return result;
     }
 
     private Node findNextNodeToSwitchWithDefaultCase(Switch switchNode) {
         return findNextNode(switchNode);
     }
 
     private Node findNextNodeToSwitchWithoutDefaultCase(Switch switchNode) {
         Node defaultBranch = switchNode.getNodeByKeyIndex(-1);
 
         switchNode.removeChild(defaultBranch);
         defaultBranch.removeAncestor(switchNode);
 
         return defaultBranch;
     }
 
     private Construction extractConditionBlock(Node node) {
         if (node.getCondition() != null) {
             for (Node ancestor : node.getAncestors()) {
                 if (node.getIndex() < ancestor.getIndex()) {
                    if (domi[node.getIndex()] != domi[node.getListOfTails().get(1).getIndex()] && getRelativeIndex(node.getIndex()) < size && getRelativeIndex(node.getIndex()) > 0) {
                         node.setNextNode(node.getListOfTails().get(1));
                     }
                     com.sdc.cfg.constructions.While whileConstruction = new com.sdc.cfg.constructions.While(node.getCondition());
                     whileConstruction.setBody(createConstructionBuilder(myNodes.subList(getRelativeIndex(node.getListOfTails().get(0)), getRelativeIndex(gen.getRightIndexForLoop(node.getIndex()))), gen).build());
                     if (node.getNextNode() != null && getRelativeIndex(node.getNextNode()) < size) {
                         extractNextConstruction(whileConstruction, node);
                     }
                     return whileConstruction;
                 }
             }
 
             /// IF
             com.sdc.cfg.constructions.ConditionalBlock conditionalBlock = new ConditionalBlock(node.getCondition());
 
             boolean fl = false;
             for (Node ancestor : node.getAncestors()) {
                 if (ancestor.getIndex() > node.getIndex()) {
                     fl = true;
                     break;
                 }
             }
 
             if (!fl) {
                 Node nextNode = findNextNode(node);
                 node.setNextNode(nextNode);
 
                 Node leftNode = node.getListOfTails().get(0);
                 Node rightNode = node.getListOfTails().get(1);
                 int rightIndex = getRelativeIndex(rightNode);
 
                 if (node.getNextNode() == null) {
                     if (hasElse(rightNode)) {
                         if (rightNode.getIndex() <= myNodes.get(size - 1).getIndex()) {
                             node.setNextNode(rightNode);
                         }
                         conditionalBlock.setThenBlock(createConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex > size ? size : rightIndex), gen).build());
                     } else {
                         conditionalBlock.setThenBlock(createConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex), gen).build());
                         conditionalBlock.setElseBlock(createConstructionBuilder(myNodes.subList(getRelativeIndex(rightNode), size), gen).build());
                     }
                 } else {
                     conditionalBlock.setThenBlock(createConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex), gen).build());
                     conditionalBlock.setElseBlock(createConstructionBuilder(myNodes.subList(rightIndex, getRelativeIndex(node.getNextNode())), gen).build());
                 }
             }
             // TODO: test second condition for switch with if in a case
             if (node.getNextNode() != null && getRelativeIndex(node.getNextNode()) < size) {
                 extractNextConstruction(conditionalBlock, node);
             }
             return conditionalBlock;
 //            }
         }
         return null;
     }
 
     private boolean hasElse(final Node node) {
         int count = 0;
 
         for (final Node ancestor : node.getAncestors()) {
             if (node.getIndex() > ancestor.getIndex()) {
                 count++;
             }
         }
 
         return count > 1;
     }
 
     private int getRelativeIndex(Node node) {
         return getRelativeIndex(node.getIndex());
     }
 
     private int getRelativeIndex(int index) {
         return index - myNodes.get(0).getIndex();
     }
 
     private void extractNextConstruction(Construction construction, final Node currentNode) {
         construction.setNextConstruction(build(currentNode.getNextNode()));
     }
 
     private void addBreakToAncestors(final Node child) {
         for (final Node parent : child.getAncestors()) {
             if (parent.getConstruction() != null) {
                 parent.getConstruction().setBreak("");
             }
         }
     }
 
     private void addBreakToAllOutgoingLinks() {
         final int firstNodeIndex = myNodes.get(0).getIndex();
         final int lastNodeIndex = myNodes.get(size - 1).getIndex();
 
         for (final Node node : myNodes) {
             for (final Node tail : node.getListOfTails()) {
                 final int tailIndex = tail.getIndex();
                 if ((tailIndex < firstNodeIndex || tail.getIndex() > lastNodeIndex) && node.getConstruction() != null) {
                     node.getConstruction().setBreak("");
                 }
             }
         }
     }
 }
