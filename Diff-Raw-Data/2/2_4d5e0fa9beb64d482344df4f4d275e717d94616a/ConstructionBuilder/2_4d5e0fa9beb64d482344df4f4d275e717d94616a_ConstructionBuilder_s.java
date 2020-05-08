 package com.sdc.util;
 
 import com.sdc.cfg.constructions.ConditionalBlock;
 import com.sdc.cfg.constructions.Construction;
 import com.sdc.cfg.constructions.ElementaryBlock;
 import com.sdc.cfg.nodes.Node;
 import com.sdc.cfg.nodes.Switch;
 import com.sdc.cfg.nodes.SwitchCase;
 //import com.sdc.cfg.nodes.While;
 
 import java.util.*;
 
 public class ConstructionBuilder {
     private List<Node> myNodes;
     private final int size;
     private final int[] domi;
     private final int[] post;
     private int[] intersection;
 
     public ConstructionBuilder(final List<Node> myNodes, final int[] domi, final int[] post) {
         this.myNodes = myNodes;
         this.domi = domi;
         this.post = post;
         this.size = myNodes.size();
         if (domi != null && domi.length > 1) {
             this.intersection = new int[domi.length];
             for (int i = 0; i < domi.length; i++) {
                 intersection[i] = domi[i] == post[i] ? domi[i] : -1;
             }
         }
     }
 
     public Construction build() {
         return build(myNodes.get(0));
     }
 
     private Construction build(Node node) {
         Construction elementaryBlock = extractElementaryBlock(node);
         Construction currentConstruction = extractConstruction(node);
 
        if (node.getCondition() == null) {
             node.setConstruction(elementaryBlock);
         } else {
             node.setConstruction(currentConstruction);
         }
 
         elementaryBlock.setNextConstruction(currentConstruction);
         return elementaryBlock;
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
 
                 final Construction caseBody = new ConstructionBuilder(myNodes.subList(leftBound, rightBound), domi, post).build();
 
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
             boolean fl1 = true;
             for (Node ancestor : node.getAncestors()) {
                 if (myNodes.indexOf(node) < myNodes.indexOf(ancestor)) {
                     if (domi[myNodes.indexOf(node)] != domi[myNodes.indexOf(node.getListOfTails().get(1))]) {
                         node.setNextNode(node.getListOfTails().get(1));
                     }
                     fl1 = false;
                     com.sdc.cfg.constructions.While whileConstruction = new com.sdc.cfg.constructions.While(node.getCondition());
 //                    myNodes.set(myNodes.indexOf(node), new While(node));
                     break;
                 }
             }
 
 
             /// IF
             if (fl1 && node.getIndex() < node.getListOfTails().get(1).getIndex()) {
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
                         if (rightNode.getAncestors().size() > 1) {
                             if (rightNode.getIndex() <= myNodes.get(size - 1).getIndex()) {
                                 node.setNextNode(rightNode);
                             }
 //                            int rightIndex = getRelativeIndex(rightNode);
                             conditionalBlock.setThenBlock(new ConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex > size ? size : rightIndex), domi, post).build());
                         } else {
                             conditionalBlock.setThenBlock(new ConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex), domi, post).build());
                             conditionalBlock.setElseBlock(new ConstructionBuilder(myNodes.subList(getRelativeIndex(rightNode), size), domi, post).build());
                         }
                     } else {
                         conditionalBlock.setThenBlock(new ConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex), domi, post).build());
                         conditionalBlock.setElseBlock(new ConstructionBuilder(myNodes.subList(rightIndex, getRelativeIndex(node.getNextNode())), domi, post).build());
                     }
                 }
                 // TODO: test second condition for switch with if in a case
                 if (node.getNextNode() != null && getRelativeIndex(node.getNextNode()) < size) {
                     extractNextConstruction(conditionalBlock, node);
                 }
                 return conditionalBlock;
             }
         }
         return null;
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
