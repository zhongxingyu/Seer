 package com.sdc.kotlin;
 
 import KotlinPrinter.KotlinPrinterPackage;
 import com.sdc.abstractLanguage.AbstractMethod;
 import com.sdc.ast.controlflow.Return;
 import com.sdc.ast.controlflow.Statement;
 import com.sdc.ast.expressions.Expression;
 import com.sdc.cfg.GraphDrawer;
 import com.sdc.cfg.Node;
 import com.sdc.abstractLanguage.AbstractFrame;
 import pretty.PrettyPackage;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class KotlinMethod extends AbstractMethod {
     private final String myModifier;
     private final String myReturnType;
     private String myName;
 
     private List<String> myImports = new ArrayList<String>();
 
     private final KotlinClass myKotlinClass;
     private final List<String> myGenericTypes;
     private final List<String> myGenericIdentifiers;
 
     private List<KotlinAnnotation> myAnnotations = new ArrayList<KotlinAnnotation>();
     private Map<Integer, List<KotlinAnnotation>> myParameterAnnotations = new HashMap<Integer, List<KotlinAnnotation>>();
 
     private int myLastLocalVariableIndex;
 
     private boolean hasReceiverParameter = false;
 
     private final AbstractFrame myRootFrame = new KotlinFrame();
     private AbstractFrame myCurrentFrame = myRootFrame;
 
     private List<Statement> myBody = null;
     private List<Node> myNodes = null;
 
     private final int myTextWidth;
     private final int myNestSize;
 
     public KotlinMethod(final String modifier, final String returnType, final String name,
                       final KotlinClass kotlinClass, final List<String> genericTypes, final List<String> genericIdentifiers,
                       final int textWidth, final int nestSize) {
         this.myModifier = modifier;
         this.myReturnType = returnType;
         this.myName = name;
         this.myKotlinClass = kotlinClass;
         this.myGenericTypes = genericTypes;
         this.myGenericIdentifiers = genericIdentifiers;
         this.myTextWidth = textWidth;
         this.myNestSize = nestSize;
     }
 
     public String getModifier() {
         return myModifier;
     }
 
     public String getReturnType() {
         return myReturnType;
     }
 
     public String getName() {
         return myName;
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
 
     public List<Statement> getBody() {
         return myBody;
     }
 
     public void setBody(final List<Statement> body) {
         this.myBody = body;
     }
 
     public void setLastLocalVariableIndex(int lastLocalVariableIndex) {
         this.myLastLocalVariableIndex = lastLocalVariableIndex;
         ((KotlinFrame) myRootFrame).setLastLocalVariableIndex(lastLocalVariableIndex);
     }
 
     public AbstractFrame getCurrentFrame() {
         return myCurrentFrame;
     }
 
     public void setCurrentFrame(final AbstractFrame currentAbstractFrame) {
         this.myCurrentFrame = currentAbstractFrame;
     }
 
     public void addImport(final String importClassName) {
         myImports.add(importClassName);
     }
 
     public void addLocalVariableName(final int index, final String name) {
         myCurrentFrame.addLocalVariableName(index, name);
 
         if (index == 0 && name.equals("$receiver")) {
             hasReceiverParameter = true;
             dragReceiverFromMethodParameters();
         }
     }
 
     public void addLocalVariableType(final int index, final String type) {
         myCurrentFrame.addLocalVariableType(index, type);
     }
 
     public void addLocalVariableFromDebugInfo(final int index, final String name, final String type) {
         myRootFrame.addLocalVariableFromDebugInfo(index, name, type);
     }
 
     public List<String> getParameters() {
         List<String> parameters = new ArrayList<String>();
         final int startIndex = isNormalClassMethod() || hasReceiverParameter ? 1 : 0;
 
         for (int variableIndex = startIndex; variableIndex <= myLastLocalVariableIndex; variableIndex++) {
             if (myRootFrame.containsIndex(variableIndex)) {
                 parameters.add(myRootFrame.getLocalVariableName(variableIndex));
             }
         }
         return parameters;
     }
 
     public boolean isGenericType(final String className) {
         return myGenericTypes.contains(className) || myKotlinClass.isGenericType(className);
     }
 
     public String getGenericIdentifier(final String className) {
         if (!myGenericTypes.contains(className)) {
             return myKotlinClass.getGenericIdentifier(className);
         } else {
             return myGenericIdentifiers.get(myGenericTypes.indexOf(className));
         }
     }
 
     public List<String> getGenericDeclaration() {
         ArrayList<String> result = new ArrayList<String>();
         for (int i = 0; i < myGenericTypes.size(); i++) {
             if (!myGenericTypes.get(i).equals("java/lang/Object")) {
                 final String[] classParts = myGenericTypes.get(i).split("/");
                 result.add(myGenericIdentifiers.get(i) + " : " + classParts[classParts.length - 1]);
             } else {
                 result.add(myGenericIdentifiers.get(i));
             }
         }
         return result;
     }
 
     public void appendAnnotation(final KotlinAnnotation annotation) {
         myAnnotations.add(annotation);
     }
 
     public List<KotlinAnnotation> getAnnotations() {
         return myAnnotations;
     }
 
     public void appendParameterAnnotation(final int index, final KotlinAnnotation annotation) {
         if (!myParameterAnnotations.containsKey(index)) {
             myParameterAnnotations.put(index, new ArrayList<KotlinAnnotation>());
         }
         myParameterAnnotations.get(index).add(annotation);
     }
 
     public boolean checkParameterForAnnotation(final int index) {
         return myParameterAnnotations.containsKey(index);
     }
 
     public List<KotlinAnnotation> getParameterAnnotations(final int index) {
         return myParameterAnnotations.get(index);
     }
 
     public boolean isNormalClassMethod() {
         return myKotlinClass.isNormalClass();
     }
 
     public boolean hasEmptyBody() {
         return myBody.size() == 1 && ((Return) myBody.get(0)).getReturnValue() == null;
     }
 
     public void addInitializerToField(final String fieldName, final Expression initializer) {
         myKotlinClass.addInitializerToField(fieldName, initializer);
     }
 
     public KotlinClass getKotlinClass() {
         return myKotlinClass;
     }
 
     public void declareThisVariable() {
         myRootFrame.getLocalVariableName(0);
     }
 
     public void dragReceiverFromMethodParameters() {
         if (hasReceiverParameter) {
            addLocalVariableName(0, "this");
             declareThisVariable();
             myName = getCurrentFrame().getLocalVariableType(0) + "." + myName;
         }
     }
 
     public void setNodes(List<Node> myNodes) {
         this.myNodes = myNodes;
     }
 
     public void drawCFG() {
         GraphDrawer graphDrawer = new GraphDrawer(myNodes, myNestSize, myTextWidth);
         graphDrawer.draw();
         graphDrawer.simplyDraw();
 
     }
 
     @Override
     public String toString() {
         return PrettyPackage.pretty(myTextWidth, KotlinPrinterPackage.printKotlinMethod(this));
     }
 }
