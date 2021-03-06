 /******************************************************************************
  * Copyright (c) 2002 - 2006 IBM Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *****************************************************************************/
 package com.ibm.wala.cast.loader;
 
 import java.util.Collection;
 
 import com.ibm.wala.cast.tree.CAstQualifier;
 import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
 import com.ibm.wala.cfg.AbstractCFG;
 import com.ibm.wala.cfg.ControlFlowGraph;
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.ssa.SymbolTable;
 import com.ibm.wala.types.Descriptor;
 import com.ibm.wala.types.MethodReference;
 import com.ibm.wala.types.Selector;
 import com.ibm.wala.types.TypeReference;
 import com.ibm.wala.util.Atom;
 import com.ibm.wala.util.collections.Pair;
 import com.ibm.wala.util.debug.Assertions;
 
 public abstract class AstMethod implements IMethod {
 
   public interface DebuggingInformation {
 
     Position getCodeBodyPosition();
       
     Position getInstructionPosition(int instructionOffset);
       
     String[][] getSourceNamesForValues();
 
   }
 
   public interface LexicalInformation {
 
     public int[] getExitExposedUses();
 	
     public int[] getExposedUses(int instructionOffset);
 
     public int[] getAllExposedUses();
 
     public Pair[] getExposedNames();
 
     public String[] getScopingParents();
 
   }
 
   public final IClass cls;
   public final Collection qualifiers;
   public final AbstractCFG cfg;
   public final SymbolTable symtab; 
   public final MethodReference ref;
   public final boolean hasCatchBlock;
   public final TypeReference[][] catchTypes;
   public final LexicalInformation lexicalInfo;
   public final DebuggingInformation debugInfo;
 
   protected AstMethod(IClass cls,
 	      Collection qualifiers,
 	      AbstractCFG cfg,
 	      SymbolTable symtab,
 	      MethodReference ref,
 	      boolean hasCatchBlock,
 	      TypeReference[][] catchTypes,
 	      LexicalInformation lexicalInfo,
 	      DebuggingInformation debugInfo)
   {
     this.cls = cls;
     this.cfg = cfg;
     this.ref = ref;
     this.symtab = symtab;
     this.qualifiers = qualifiers;
     this.catchTypes = catchTypes;
     this.hasCatchBlock = hasCatchBlock;
     this.lexicalInfo = lexicalInfo;
     this.debugInfo = debugInfo;
   }
   
   protected AstMethod(IClass cls,
 	      Collection qualifiers,
 	      MethodReference ref)
   {
     this.cls = cls;
     this.qualifiers = qualifiers;
     this.ref = ref;
 
     this.cfg = null;
     this.symtab = null;
     this.catchTypes = null;
     this.hasCatchBlock = false;
     this.lexicalInfo = null;
     this.debugInfo = null;
     
     Assertions._assert(isAbstract());
   }
 
   /**
    *  Parents of this method with respect to lexical scoping, that is, 
    * methods containing state possibly referenced lexically in this
    * method
    */
   public abstract class LexicalParent {
     public abstract String getName();
     public abstract AstMethod getMethod();
 
     public int hashCode() { 
       return getName().hashCode()*getMethod().hashCode(); 
     }
 
     public boolean equals(Object o) {
 	return (o instanceof LexicalParent) &&
 	    getName().equals(((LexicalParent)o).getName()) && 
 	    getMethod().equals(((LexicalParent)o).getMethod());
     }
   };
 
   public abstract LexicalParent[] getParents();
   
   public IClass getDeclaringClass() {
     return cls;
   }
 
   public String getSignature() {
    return ref.toString();
   }
 
   public Selector getSelector() {
     return ref.getSelector();
   }
 
   public boolean isClinit() {
     return getSelector().equals(MethodReference.clinitSelector);
   }
 
   public boolean isInit() {
     return getSelector().getName().equals(MethodReference.initAtom);
   }
 
   public Atom getName() {
     return ref.getName();
   }
 
   public Descriptor getDescriptor() {
     return ref.getDescriptor();
   }
 
   public MethodReference getReference() {
     return ref;
   }
 
   public TypeReference getReturnType() {
     return ref.getReturnType();
   }
 
   public boolean isStatic() {
     return qualifiers.contains(CAstQualifier.STATIC);
   }
 
   public boolean isSynchronized() {
     return qualifiers.contains(CAstQualifier.SYNCHRONIZED);
   }
 
   public boolean isNative() {
     return qualifiers.contains(CAstQualifier.NATIVE);
   }
 
   public boolean isSynthetic() {
     return false;
   }
 
   public boolean isAbstract() {
     return qualifiers.contains(CAstQualifier.ABSTRACT);
   }
 
   public boolean isPrivate() {
     return qualifiers.contains(CAstQualifier.PRIVATE);
   }
 
   public boolean isProtected() {
     return qualifiers.contains(CAstQualifier.PROTECTED);
   }
 
   public boolean isPublic() {
     return qualifiers.contains(CAstQualifier.PUBLIC);
   }
 
   public boolean isFinal() {
     return qualifiers.contains(CAstQualifier.FINAL);
   }
 
   public boolean isVolatile() {
     return qualifiers.contains(CAstQualifier.VOLATILE);
   }
 
   public ControlFlowGraph getControlFlowGraph() {
     return cfg;
   }
 
   public boolean hasExceptionHandler() {
     return hasCatchBlock;
   }
 
   public int getNumberOfParameters() {
     return symtab.getParameterValueNumbers().length;
   }
 
   public int getLineNumber(int instructionIndex) {
     Position pos = debugInfo.getInstructionPosition(instructionIndex);
     if (pos == null) {
       return -1;
     } else {
       return pos.getFirstLine();
     }
   }
 
   public Position getSourcePosition() {
     return debugInfo.getCodeBodyPosition();
   }
 
   public Position getSourcePosition(int instructionIndex) {
     return debugInfo.getInstructionPosition(instructionIndex);
   }
 }
