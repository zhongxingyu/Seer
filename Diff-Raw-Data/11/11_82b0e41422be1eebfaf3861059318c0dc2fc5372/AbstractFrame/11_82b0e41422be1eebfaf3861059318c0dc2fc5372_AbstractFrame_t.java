 package com.sdc.abstractLanguage;
 
 import java.util.*;
 
 public abstract class AbstractFrame {
     protected String myStackedVariableType = "";
     protected int myStackedVariableIndex = 0;
     protected boolean myStackChecked = false;
 
     protected Map<Integer, String> myLocalVariableNames = new HashMap<Integer, String>();
     protected Map<Integer, String> myLocalVariableTypes = new HashMap<Integer, String>();
     protected List<Integer> myDeclaredVariables = new ArrayList<Integer>();
     protected Set<Integer> myLocalVariablesFromDebugInfo = new HashSet<Integer>();
 
     protected AbstractFrame myParent = null;
     protected List<AbstractFrame> myChildren = new ArrayList<AbstractFrame>();
     protected AbstractFrame mySameAbstractFrame = null;
 
     public String getStackedVariableType() {
         return myStackedVariableType;
     }
 
     public void setStackedVariableType(String stackedVariableType) {
         this.myStackedVariableType = stackedVariableType;
     }
 
     public void setStackedVariableIndex(int stackedVariableIndex) {
         this.myStackedVariableIndex = stackedVariableIndex;
     }
 
     public int getStackedVariableIndex() {
         return myStackedVariableIndex;
     }
 
     public AbstractFrame getParent() {
         return myParent;
     }
 
     public void setParent(final AbstractFrame parent) {
         this.myParent = parent;
     }
 
     public void setSameFrame(final AbstractFrame sameAbstractFrame) {
         this.mySameAbstractFrame = sameAbstractFrame;
     }
 
     public boolean checkStack() {
         if (!myStackChecked && !myStackedVariableType.isEmpty()) {
             myStackChecked = true;
             return true;
         }
         return false;
     }
 
     public boolean hasStack() {
         return !myStackedVariableType.isEmpty();
     }
 
     public void addChild(final AbstractFrame child) {
         myChildren.add(child);
     }
 
     public void addLocalVariableName(final int index, final String name) {
         myLocalVariableNames.put(index, name);
     }
 
     public void addLocalVariableType(final int index, final String type) {
         myLocalVariableTypes.put(index, type);
     }
 
     public boolean addLocalVariableFromDebugInfo(final int index, final String name, final String type) {
         if (!containsIndex(index) || myLocalVariablesFromDebugInfo.contains(index)) {
             for (final AbstractFrame abstractFrame : myChildren) {
                 if (abstractFrame.addLocalVariableFromDebugInfo(index, name, type)) {
                     return true;
                 }
             }
             return false;
         } else {
             myLocalVariablesFromDebugInfo.add(index);
             addLocalVariableName(index, name);
            if (!type.equals("Object")) {
                addLocalVariableType(index, type);
            }
             return true;
         }
     }
 
     public String getLocalVariableName(final int index) {
         if (containsIndex(index)) {
             if (myDeclaredVariables.contains(index)) {
                 return myLocalVariableNames.get(index);
             } else {
                 myDeclaredVariables.add(index);
                 return getVariableNameForDeclaration(index);
             }
         } else {
             if (mySameAbstractFrame == null) {
                 return myParent.getLocalVariableName(index);
             } else {
                 return mySameAbstractFrame.getLocalVariableName(index);
             }
         }
     }
 
     public String getLocalVariableType(final int index) {
         if (containsIndex(index)) {
             return myLocalVariableTypes.get(index);
         } else {
             if (mySameAbstractFrame == null) {
                 return myParent.getLocalVariableType(index);
             } else {
                 return mySameAbstractFrame.getLocalVariableType(index);
             }
         }
     }
 
     public boolean containsIndex(final int index) {
         return myLocalVariableTypes.containsKey(index);
     }
 
     abstract protected String getVariableNameForDeclaration(final int index);
 }
