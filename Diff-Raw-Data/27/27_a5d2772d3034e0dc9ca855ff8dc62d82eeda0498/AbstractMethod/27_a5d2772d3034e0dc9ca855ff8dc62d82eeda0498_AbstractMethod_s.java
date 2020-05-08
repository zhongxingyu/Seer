 package com.sdc.abstractLanguage;
 
 import com.sdc.ast.expressions.Expression;
 import com.sdc.cfg.constructions.Construction;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class AbstractMethod {
     protected final String myModifier;
     protected final String myReturnType;
     protected String myName;
     protected final String mySignature;
     protected final String[] myExceptions;
 
     protected List<String> myImports = new ArrayList<String>();
 
     protected final AbstractClass myAbstractClass;
     protected final List<String> myGenericTypes;
     protected final List<String> myGenericIdentifiers;
 
     protected List<AbstractAnnotation> myAnnotations = new ArrayList<AbstractAnnotation>();
     protected Map<Integer, List<AbstractAnnotation>> myParameterAnnotations = new HashMap<Integer, List<AbstractAnnotation>>();
 
     protected int myLastLocalVariableIndex;
 
     protected AbstractFrame myRootAbstractFrame;
     protected AbstractFrame myCurrentAbstractFrame;
 
     protected MethodVisitorStub.DecompilerException myError = null;
 
     protected Construction myBegin;
 
     protected final int myTextWidth;
     protected final int myNestSize;
 
     public AbstractMethod(final String modifier, final String returnType, final String name, final String signature, final String[] exceptions,
                           final AbstractClass abstractClass, final List<String> genericTypes, final List<String> genericIdentifiers,
                           final int textWidth, final int nestSize) {
         this.myModifier = modifier;
         this.myReturnType = returnType;
         this.myName = name;
         this.mySignature = signature;
         this.myExceptions = exceptions;
         this.myAbstractClass = abstractClass;
         this.myGenericTypes = genericTypes;
         this.myGenericIdentifiers = genericIdentifiers;
         this.myTextWidth = textWidth;
         this.myNestSize = nestSize;
     }
 
     protected abstract String getInheritanceIdentifier();
 
     protected abstract int getParametersStartIndex();
 
     public String getModifier() {
         return myModifier;
     }
 
     public String getReturnType() {
         return myReturnType;
     }
 
     public String getName() {
         return myName;
     }
 
     public String getSignature() {
         return mySignature;
     }
 
     public void setName(final String name) {
         this.myName = name;
     }
 
     public String[] getExceptions() {
         return myExceptions;
     }
 
     public List<String> getImports() {
         return myImports;
     }
 
     public int getLastLocalVariableIndex() {
         return myLastLocalVariableIndex;
     }
 
     public int getNestSize() {
         return myNestSize;
     }
 
     public int getTextWidth() {
         return myTextWidth;
     }
 
     public MethodVisitorStub.DecompilerException getError() {
         return myError;
     }
 
     public void setError(final MethodVisitorStub.DecompilerException error) {
         this.myError = error;
     }
 
     public void setLastLocalVariableIndex(int lastLocalVariableIndex) {
         this.myLastLocalVariableIndex = lastLocalVariableIndex;
     }
 
     public AbstractFrame getCurrentFrame() {
         return myCurrentAbstractFrame;
     }
 
     public void setCurrentFrame(final AbstractFrame currentAbstractFrame) {
         this.myCurrentAbstractFrame = currentAbstractFrame;
     }
 
     public AbstractClass getDecompiledClass() {
         return myAbstractClass;
     }
 
     public boolean isNormalClassMethod() {
         return myAbstractClass.isNormalClass();
     }
 
     public void addImport(final String importClassName) {
         myImports.add(importClassName);
     }
 
     public void addLocalVariableName(final int index, final String name) {
         myCurrentAbstractFrame.addLocalVariableName(index, name);
     }
 
     public void addLocalVariableType(final int index, final String type) {
         myCurrentAbstractFrame.addLocalVariableType(index, type);
     }
 
     public void addLocalVariableFromDebugInfo(final int index, final String name, final String type) {
         myRootAbstractFrame.setLastLocalVariableIndex(myLastLocalVariableIndex);
         myRootAbstractFrame.addLocalVariableFromDebugInfo(index, name, type);
     }
 
     public List<String> getParameters() {
         List<String> parameters = new ArrayList<String>();
         final int startIndex = getParametersStartIndex();
 
         for (int variableIndex = startIndex; variableIndex <= myLastLocalVariableIndex; variableIndex++) {
             if (myRootAbstractFrame.containsIndex(variableIndex)) {
                 parameters.add(myRootAbstractFrame.getLocalVariableName(variableIndex));
             }
         }
         return parameters;
     }
 
     public boolean isGenericType(final String className) {
         return myGenericTypes.contains(className) || myAbstractClass.isGenericType(className);
     }
 
     public String getGenericIdentifier(final String className) {
         if (!myGenericTypes.contains(className)) {
             return myAbstractClass.getGenericIdentifier(className);
         } else {
             return myGenericIdentifiers.get(myGenericTypes.indexOf(className));
         }
     }
 
     public List<String> getGenericDeclaration() {
         List<String> result = new ArrayList<String>();
         for (int i = 0; i < myGenericTypes.size(); i++) {
             final String genericType = myGenericTypes.get(i);
             if (!genericType.equals("Object ") && !genericType.equals("Any?")) {
                 result.add(myGenericIdentifiers.get(i) + " " + getInheritanceIdentifier() + " " + genericType.trim());
             } else {
                 result.add(myGenericIdentifiers.get(i));
             }
         }
         return result;
     }
 
     public void appendAnnotation(final AbstractAnnotation annotation) {
         myAnnotations.add(annotation);
     }
 
     public List<AbstractAnnotation> getAnnotations() {
         return myAnnotations;
     }
 
     public void appendParameterAnnotation(final int index, final AbstractAnnotation annotation) {
         if (!myParameterAnnotations.containsKey(index)) {
             myParameterAnnotations.put(index, new ArrayList<AbstractAnnotation>());
         }
         myParameterAnnotations.get(index).add(annotation);
     }
 
     public boolean checkParameterForAnnotation(final int index) {
         return myParameterAnnotations.containsKey(index);
     }
 
     public List<AbstractAnnotation> getParameterAnnotations(final int index) {
         return myParameterAnnotations.get(index);
     }
 
     public void addInitializerToField(final String fieldName, final Expression initializer) {
         myAbstractClass.addInitializerToField(fieldName, initializer);
     }
 
     public void declareThisVariable() {
         myRootAbstractFrame.getLocalVariableName(0);
     }
 
     public void setBegin(Construction myBegin) {
         this.myBegin = myBegin;
     }
 
     public Construction getBegin() {
         return myBegin;
     }
 
 /*
     public void drawCFG() {
         GraphDrawer graphDrawer = new GraphDrawer(myNodes, myNestSize, myTextWidth);
         graphDrawer.draw();
         graphDrawer.simplyDraw();
     }
 */
 }
