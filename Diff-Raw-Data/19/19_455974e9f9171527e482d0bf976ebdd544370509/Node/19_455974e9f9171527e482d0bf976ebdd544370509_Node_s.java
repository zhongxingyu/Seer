 package com.group7.dragonwars.engine.GoalArbitration;
 
 import java.util.*;
 
 public class Node {
     private AtomicAction nodeAction;
     private Node nodeParent;
     private List<Node> children;
     private int min = -1,						//Min child node index defaults to -1
                 max = -1;						//Max child node index defaults to -1
     private float minValue = Float.MAX_VALUE;	//Min child node value
     private float maxValue = Float.MIN_VALUE;	//Max child node value
     private int nodeDepth;
     private float currentValue;
     private static int size = 1;						//Tree size thus far
     private static int maxSize = 300; 					//Max tree size
 
     public Node(Node parent, int depth, float currentvalue, AtomicAction action) {
         nodeAction = action;
         nodeParent = parent;
         nodeDepth = depth;
         currentValue = currentvalue;
     }
 
     public List<AtomicAction> getActions() {
         List<AtomicAction> nodes = new ArrayList<AtomicAction>();
 
         //explore tree recursively up to a defined depth
         for (Node child : children) {
             nodes.addAll((List<AtomicAction>)child.getActions());
         }
 
         if (children.size() == 0) {
             nodes.add(this.nodeAction);
         }
 
         return nodes;
     }
 
     public List<Node> getTerminals() {
         List<Node> nodes = new ArrayList<Node>();
 
         //explore tree recursively up to a defined depth
         for (Node child : children) {
             nodes.addAll((List<Node>)child.getTerminals());
         }
 
         if (children.size() == 0) {
             nodes.add(this);
         }
 
         return nodes;
     }
 
     //collapse tree from bottom to the base node
    public float Collaplse() {
         float value = currentValue;
 
         if (getMiniMax()) {
             for (Node child : children) {
                value += child.Collaplse();
                 size--;
             }
         } else {
             for (Node child : children) {
                value += child.Collaplse();
                 size--;
             }
         }
 
         children.clear();
         return value;
     }
 
     public void AddChildNode(float value, AtomicAction action) {
         children.add(new Node(this, nodeDepth+1, value, action));
 
         if (children.size() > 0) {
             if (value < minValue) {
                 minValue = value;
                 min = children.size();
             } else if (value > maxValue) {
                 maxValue = value;
                 max = children.size();
             }
         }
 
         size++;
     }
 
     public float getTotalValue() {
         float totalValue = currentValue;
 
         //recursively calculates current node value;
         if (nodeParent == null)
             return getCurrentValue();
 
         if (nodeDepth > 0)
             totalValue += nodeParent.getTotalValue();
 
         return totalValue;
     }
 
     public float getCurrentValue() {
         return currentValue;
     }
 
     public boolean getMiniMax() {
         return nodeDepth%2 == 0;
     }
 
     public void setSize(int newSize) {
         size = newSize;
     }
 
     public int getSize() {
         return size;
     }
 
     public boolean isFull() {
         return maxSize <= size;
     }
 }
