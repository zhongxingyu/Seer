 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.core.dependencyScan.core;
 
 import org.apache.myfaces.scripting.core.dependencyScan.api.DependencyRegistry;
 import org.objectweb.asm.*;
 import org.objectweb.asm.signature.SignatureReader;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 class MethodScanVisitor implements MethodVisitor {
 
     // static final Logger log = Logger.getLogger("ClassScanVisitor");
 
     String _currentlyVisitedClass = null;
     String _rootClass;
     Integer _engineType = null;
     DependencyRegistry _dependencyRegistry = null;
 
     static Logger _log = Logger.getLogger(MethodScanVisitor.class.getName());
 
     public MethodScanVisitor(Integer engineType, String rootClass, String currentlyVisitedClass, DependencyRegistry registry) {
         _currentlyVisitedClass = currentlyVisitedClass;
         _dependencyRegistry = registry;
         _engineType = engineType;
         _rootClass = rootClass;
     }
 
     public AnnotationVisitor visitAnnotationDefault() {
         return null;
     }
 
     public AnnotationVisitor visitAnnotation(String description, boolean b) {
         registerDependency(Type.getType(description), "registering annotation [" + description + "]");
 
         return null;
     }
 
     public AnnotationVisitor visitParameterAnnotation(int opCode, String description, boolean b) {
         registerDependency(Type.getType(description), "registering annotation [" + description + "]");
 
         return null;
     }
 
     public void visitAttribute(Attribute attribute) {
         if (_log.isLoggable(Level.FINEST))
             _log.log(Level.FINEST, "visitAttribute {0}", attribute.type);
     }
 
     public void visitCode() {
         //log.log(Level.INFO, "Method code");
     }
 
     public void visitFrame(int opCode1, int opCode2, Object[] objects, int opCode3, Object[] objects1) {
         if (_log.isLoggable(Level.FINEST))
             _log.log(Level.FINEST, "visitFrame {0}", "");
 
     }
 
     public void visitInsn(int opCode) {
     }
 
     public void visitIntInsn(int opCode1, int opCode2) {
     }
 
     public void visitVarInsn(int opCode1, int opCode2) {
     }
 
     public void visitTypeInsn(int opCode, String castType) {
         //cast
         // log.log(Level.INFO, "TypeInsn: {0} ", new String[]{castType});
         registerDependency(Type.getObjectType(castType), "cast registered type[" + castType + "]");
         if (_log.isLoggable(Level.FINEST))
             _log.log(Level.FINEST, "visitTypeInsn {0}", castType);
 
     }
 
    private void registerDependency(Type dependency, String desc) {
 
         String className = dependency.getClassName();
         if (className.endsWith("[]")) {
             className = className.substring(0, className.indexOf("["));
         }
 
         if (_dependencyRegistry != null) {
             _dependencyRegistry.addDependency(_engineType, _rootClass, _currentlyVisitedClass, className);
         }
     }
 
     /**
     * @param opCode
      * @param owner      hosting classname of field (always the calling class afaik)
      * @param name       internal descriptor
      * @param descriptor field type
      */
     public void visitFieldInsn(int opCode, String owner, String name, String descriptor) {
         //    log.log(Level.INFO, "visitFieldInsn {0} {1} {2}", new Object[]{owner, name, descriptor});
         //we have to deal with static imports as special case of field insertions
         if (name != null && name.length() > 6 && name.startsWith("class$")) {
             //special fallback for groovy static imports which are added as fields
             name = "L" + name.substring(6).replaceAll("\\$", ".") + ";";
             registerDependency(Type.getType(name), "field insn name [" + name + "]");
         }
         if (descriptor != null) {
             registerDependency(Type.getType(descriptor), "field insn descriptor [" + descriptor + "]");
         }
 
         if (_log.isLoggable(Level.FINEST))
             _log.log(Level.FINEST, "visitFieldInsn {0}", descriptor);
 
     }
 
     /**
      * Method call
      *
      * @param opc   internal opcode
      * @param owner hosting classname of the method
      * @param name  method name
      * @param desc  descriptor string
      */
     public void visitMethodInsn(int opc, String owner, String name, String desc) {
         //s2 arguments list
         if (desc != null) {
             registerDependency(Type.getReturnType(desc), "Registering return type [" + desc + "]");
             Type[] argumentTypes = Type.getArgumentTypes(desc);
             if (argumentTypes != null) {
                 for (Type argumentType : argumentTypes) {
                     registerDependency(argumentType, "Registering argument type [" + desc + "]");
                 }
             }
         }
 
         //s1 method name, can be ignored
         //   s hosting classname
         if (owner != null)
             registerDependency(Type.getObjectType(owner), "registering callee type [" + owner + "]");
 
     }
 
     public void visitJumpInsn(int i, Label label) {
 
     }
 
     public void visitLabel(Label label) {
 
     }
 
     public void visitLdcInsn(Object o) {
 
     }
 
     public void visitIincInsn(int i, int i1) {
 
     }
 
     public void visitTableSwitchInsn(int i, int i1, Label label, Label[] labels) {
 
     }
 
     public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {
 
     }
 
     public void visitMultiANewArrayInsn(String s, int i) {
         if (_log.isLoggable(Level.FINEST))
             _log.log(Level.FINEST, "visitMultiANewArrayInsn {0}", s);
     }
 
     public void visitTryCatchBlock(Label label, Label label1, Label label2, String catchType) {
         //try catch block type information in the last string
         //log.log(Level.INFO, "visitTryCatchBlock: {0} {1} {2} {3}", new Object[]{label.toString(), label1.toString(), label2.toString(), catchType});
         registerDependency(Type.getObjectType(catchType), "catch registered type[" + catchType + "]");
 
     }
 
     public void visitLocalVariable(String name, String description, String signature, Label label, Label label1, int i) {
         //local variable on method level
         registerDependency(Type.getType(description), "local variable registered type[" + description + "]");
         handleGenerics(signature);
     }
 
     public void visitLineNumber(int i, Label label) {
 
     }
 
     public void visitMaxs(int i, int i1) {
 
     }
 
     public void visitEnd() {
 
     }
 
     private void handleGenerics(String signature) {
         if (signature != null && signature.contains("<")) {
             SignatureReader reader = new SignatureReader(signature);
             reader.acceptType(new DependencySignatureVisitor(_dependencyRegistry, _engineType, _rootClass, _currentlyVisitedClass));
         }
     }
 }
