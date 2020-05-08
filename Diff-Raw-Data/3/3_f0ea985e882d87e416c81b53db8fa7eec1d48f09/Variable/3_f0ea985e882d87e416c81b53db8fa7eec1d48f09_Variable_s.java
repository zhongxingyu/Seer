 package com.sdc.ast.expressions.identifiers;
 
 import com.sdc.ast.OperationType;
 import com.sdc.ast.expressions.Constant;
 import com.sdc.ast.expressions.Expression;
 
 public class Variable extends Identifier {
     protected final int myIndex;
 
     protected Expression myName;
     protected String myVariableType;
 
     protected boolean myIsMethodParameter = false;
     protected boolean myIsDeclared = false;
 
     protected Variable myParentCopy;
     protected Variable myChildCopy;
 
     public Variable(final int index, final String variableType, final String name) {
         this.myIndex = index;
         this.myName = new Constant(name, false);
         this.myVariableType = variableType;
 
         myType = OperationType.VARIABLE;
     }
 
     public void setIsMethodParameter(final boolean isMethodParameter) {
         this.myIsMethodParameter = isMethodParameter;
     }
 
     public boolean isMethodParameter() {
         return myIsMethodParameter;
     }
 
     public boolean isDeclared() {
         return myIsDeclared;
     }
 
     public void declare() {
         myIsDeclared = true;
         if (myChildCopy != null) {
             myChildCopy.declare();
         }
     }
 
     public void cutParent() {
         if (myParentCopy != null) {
             myParentCopy.cutChildCopy();
         }
 
         myParentCopy = null;
     }
 
     public void setName(final String name) {
         this.myName = new Constant(name, false);
     }
 
     public void setVariableType(final String variableType) {
         this.myVariableType = variableType;
     }
 
     public Variable createCopy() {
         Variable copy = createVariable(myIndex, myVariableType,((Constant) myName).getValue().toString());
         myChildCopy = copy;
         copy.setParentCopy(this);
 
         return copy;
     }
 
     @Override
     public Expression getName() {
         return myName;
     }
 
     @Override
     public String getType() {
         return myVariableType;
     }
 
     public int getIndex() {
         return myIndex;
     }
 
     @Override
     public String toString() {
         Expression name;
         try {
             name = getName();
         } catch (NullPointerException e) {
             name = null;
         }
         return "Variable{" +
                 "myIndex=" + myIndex +
                 ", myName=" + (name != null ? name : " no myName yet") + "}";
     }
 
     @Override
     public boolean isBoolean() {
         return getType().contains("boolean");
     }
 
     public boolean isThis() {
         return getName() instanceof Constant && ((Constant) getName()).isThis();
     }
 
     protected void setParentCopy(final Variable parent) {
         this.myParentCopy = parent;
     }
 
     protected void cutChildCopy() {
         myChildCopy = null;
     }
 
     protected Variable createVariable(final int index, final String variableType, final String name) {
         return new Variable(index, variableType, name);
     }
 }
