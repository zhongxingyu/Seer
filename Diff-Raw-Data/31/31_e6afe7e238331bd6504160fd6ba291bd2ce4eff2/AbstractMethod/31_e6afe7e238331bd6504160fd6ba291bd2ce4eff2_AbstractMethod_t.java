 package com.sdc.abstractLanguage;
 
import com.sdc.ast.controlflow.Return;
import com.sdc.ast.controlflow.Statement;
 import com.sdc.ast.expressions.Expression;
 import com.sdc.ast.expressions.identifiers.Variable;
 import com.sdc.cfg.constructions.Construction;
import com.sdc.cfg.constructions.ElementaryBlock;
 import org.objectweb.asm.Label;
 
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
 
     protected List<AbstractFrame> myFrames = new ArrayList<AbstractFrame>();
 
     protected int myLastLocalVariableIndex;
 
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
 
         this.myFrames.add(createFrame());
     }
 
     protected abstract String getInheritanceIdentifier();
 
     protected abstract int getParametersStartIndex();
 
     public abstract AbstractFrame createFrame();
 
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
 
     public AbstractClass getDecompiledClass() {
         return myAbstractClass;
     }
 
     public boolean isNormalClassMethod() {
         return myAbstractClass.isNormalClass();
     }
 
     public void addImport(final String importClassName) {
         myImports.add(importClassName);
     }
 
     public AbstractFrame getCurrentFrame() {
         if (!myFrames.isEmpty()) {
             return myFrames.get(myFrames.size() - 1);
         }
         return null;
     }
 
     public AbstractFrame getRootFrame() {
         if (!myFrames.isEmpty()) {
             return myFrames.get(0);
         }
         return null;
     }
 
     public void addNewFrame(final AbstractFrame frame) {
         myFrames.add(frame);
     }
 
     public void addThisVariable(final String type) {
         getCurrentFrame().createAndInsertVariable(0, type, "this");
     }
 
     public void updateVariableInformation(final int index, final String type, final String name) {
         getCurrentFrame().updateVariableInformation(index, type, name);
     }
 
     public void updateVariableInformationFromDebugInfo(final int index, final String type, final String name, final Label start, final Label end) {
         boolean started = false;
 
         for (final AbstractFrame frame : myFrames) {
             if (started || frame.hasLabel(start)) {
                 if (!started) {
                     frame.getVariable(index).cutParent();
                 }
                 frame.updateVariableInformation(index, type, name);
 
                 started = true;
                 if (frame.hasLabel(end)) {
                     return;
                 }
             }
         }
     }
 
     public void updateVariableNameFromDebugInfo(final int index, final String name, final Label start, final Label end) {
         for (final AbstractFrame frame : myFrames) {
             if (frame.hasLabel(start)) {
                 Variable variable = frame.getVariable(index);
                 updateVariableInformationFromDebugInfo(index, variable.getType(), name, start, end);
             }
         }
     }
 
     public void declareThisVariable() {
         getCurrentFrame().getVariable(0).declare();
     }
 
     public List<Variable> getParameters() {
         return getRootFrame().getMethodParameters(getParametersStartIndex());
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
 
     public boolean hasFieldInitializer(final String fieldName) {
         return myAbstractClass.hasFieldInitializer(fieldName);
     }
 
     public void setBegin(Construction myBegin) {
         this.myBegin = myBegin;
     }
 
     public Construction getBegin() {
         return myBegin;
     }
 
    public boolean hasEmptyBody() {
        if (myBegin instanceof ElementaryBlock) {
            final List<Statement> statements = ((ElementaryBlock) myBegin).getStatements();
            return statements.isEmpty() || statements.size() == 1 && statements.get(0) instanceof Return && ((Return) statements.get(0)).getReturnValue() == null;
        }
        return false;
    }

 /*
     public void drawCFG() {
         GraphDrawer graphDrawer = new GraphDrawer(myNodes, myNestSize, myTextWidth);
         graphDrawer.draw();
         graphDrawer.simplyDraw();
     }
 */
 }
