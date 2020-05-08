 package com.sdc.abstractLanguage;
 
 import com.sdc.ast.expressions.identifiers.Variable;
 import org.objectweb.asm.Label;
 
 import java.util.*;
 
 public class Frame {
     protected boolean myStackChecked = false;
     protected boolean myHasStack = false;
     protected String myStackedVariableType;
 
     protected Map<Integer, Integer> myVariableIndexToArrayPosition = new HashMap<Integer, Integer>();
     protected List<Variable> myVariables = new ArrayList<Variable>();
 
     protected List<Label> myLabels = new ArrayList<Label>();
 
     protected int myLastMethodParameterIndex = -1;
 
     protected int myLastCommonVariableIndexInList = -1;
 
     public boolean isMyStackChecked() {
         return myStackChecked;
     }
 
     public void setStackChecked(final boolean stackChecked) {
         this.myStackChecked = stackChecked;
     }
 
     public List<Variable> getVariables() {
         return myVariables;
     }
 
     public void setVariables(final List<Variable> variables) {
         int pos = 0;
         for (final Variable variable : variables) {
             myVariableIndexToArrayPosition.put(variable.getIndex(), pos);
             pos++;
         }
 
         this.myVariables = variables;
     }
 
     public void addLabel(final Label label) {
         myLabels.add(label);
     }
 
     public boolean hasLabel(final Label label) {
         return myLabels.contains(label);
     }
 
     public int getLastMethodParameterIndex() {
         return myLastMethodParameterIndex;
     }
 
     public void setLastMethodParameterIndex(final int lastMethodParameterIndex) {
         this.myLastMethodParameterIndex = lastMethodParameterIndex;
        if (myLastCommonVariableIndexInList == -1) {
            myLastCommonVariableIndexInList = lastMethodParameterIndex;
        }
     }
 
     public int getLastCommonVariableIndexInList() {
         return myLastCommonVariableIndexInList;
     }
 
     public void setLastCommonVariableIndexInList(int lastCommonVariableIndexInList) {
         this.myLastCommonVariableIndexInList = lastCommonVariableIndexInList;
     }
 
     public boolean hasStack() {
         return myHasStack;
     }
 
     public void setHasStack(final boolean hasStack) {
         this.myHasStack = hasStack;
     }
 
     public String getStackedVariableType() {
         return myStackedVariableType;
     }
 
     public void setStackedVariableType(final String stackedVariableType) {
         this.myStackedVariableType = stackedVariableType;
     }
 
     public boolean checkStack() {
         if (!myStackChecked && myHasStack) {
             myStackChecked = true;
             return true;
         }
         return false;
     }
 
     public Variable createAndInsertVariable(final int index, final String type, final String name) {
         if (!containsVariable(index)) {
             final Variable variable = new Variable(index, type, name);
             variable.setIsMethodParameter(index > 0 && index <= myLastMethodParameterIndex);
 
             myVariableIndexToArrayPosition.put(index, myVariables.size());
             myVariables.add(variable);
 
             return variable;
         }
         return null;
     }
 
     public void updateVariableInformation(final int index, final String type, final String name) {
         Variable variable = getVariable(index);
         variable.setVariableType(type);
         variable.setName(name);
     }
 
     public Variable getVariable(final int variableIndex) {
         if (containsVariable(variableIndex)) {
             return myVariables.get(myVariableIndexToArrayPosition.get(variableIndex));
         } else {
             return createAndInsertVariable(variableIndex, null, null);
         }
     }
 
    public int getVariableListLength() {
        return myVariables.size();
    }

     public Frame createNextFrameWithAbsoluteBound(final int rightBound) {
         Frame newFrame = createFrame();
 
         newFrame.setVariables(getVariablesSubList(rightBound));
         newFrame.setLastCommonVariableIndexInList(rightBound - 1);
         newFrame.setLastMethodParameterIndex(myLastMethodParameterIndex);
 
         return newFrame;
     }
 
     public Frame createNextFrameWithRelativeBound(final int count) {
         return createNextFrameWithAbsoluteBound(myLastCommonVariableIndexInList + count + 1);
     }
 
     public List<Variable> getMethodParameters() {
         return getVariablesSubList(myVariableIndexToArrayPosition.get(myLastMethodParameterIndex) + 1);
     }
 
     protected List<Variable> getVariablesSubList(final int rightBound) {
         List<Variable> result = new ArrayList<Variable>();
        final int actualRightBound = rightBound > myVariables.size() ? myVariables.size() : rightBound;

        for (final Variable variable : myVariables.subList(0, actualRightBound)) {
             result.add(variable);
         }
 
         return result;
     }
 
     protected boolean containsVariable(final int index) {
         return myVariableIndexToArrayPosition.keySet().contains(index);
     }
 
     protected Frame createFrame() {
         return new Frame();
     }
 }
