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
 package org.apache.myfaces.scripting.core.dependencyScan;
 
 import org.objectweb.asm.*;
 
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 class MethodScanVisitor implements MethodVisitor {
 
     // static final Logger log = Logger.getLogger("ClassScanVisitor");
 
     final Set<String> dependencies;
     final Set<String> whiteList;
 
     public MethodScanVisitor(Set<String> dependencies, Set<String> whiteList) {
         this.dependencies = dependencies;
         this.whiteList = whiteList;
     }
 
     public AnnotationVisitor visitAnnotationDefault() {
         return null;
     }
 
     public AnnotationVisitor visitAnnotation(String description, boolean b) {
         registerDependency(Type.getType(description), "registering annotation ["+description+"]");
         
 
         return null;
     }
 
     public AnnotationVisitor visitParameterAnnotation(int i, String description, boolean b) {
         registerDependency(Type.getType(description), "registering annotation ["+description+"]");
 
         return null;
     }
 
     public void visitAttribute(Attribute attribute) {
         //log.log(Level.INFO, "MethodAttr {0}:", attribute.type);
         //System.out.println(attribute.type);
     }
 
     public void visitCode() {
         //log.log(Level.INFO, "Method code");
     }
 
     public void visitFrame(int i, int i1, Object[] objects, int i2, Object[] objects1) {
     }
 
     public void visitInsn(int i) {
     }
 
     public void visitIntInsn(int i, int i1) {
     }
 
     public void visitVarInsn(int i, int i1) {
     }
 
     public void visitTypeInsn(int i, String castType) {
         //cast
         // log.log(Level.INFO, "TypeInsn: {0} ", new String[]{castType});
         registerDependency(Type.getObjectType(castType), "cast registered type["+castType+"]");
     }
 
     private void registerDependency(Type dependency, String desc) {
 
         String className = dependency.getClassName();
         if(className.endsWith("[]")) {
             className = className.substring(0, className.indexOf("["));
         }
       
         ClassScanUtils.logParmList(dependencies, whiteList, className);
     }
 
     /**
      *
      * @param i
      * @param s  hosting classname of field (always the calling class afaik)
      * @param s1 internal descriptor TODO check if it needs treatment, but I assume static imports need it
      * @param s2  field type
      */
     public void visitFieldInsn(int i, String s, String s1, String s2) {
         //    log.log(Level.INFO, "visitFieldInsn {0} {1} {2}", new Object[]{s, s1, s2});
         //we have to deal with static imports as special case of field insertions
         if (s1 != null && s1.length() > 6 && s1.startsWith("class$")) {
             //special fallback for groovy static imports which are added as fields
             s1 = "L" + s1.substring(6).replaceAll("\\$", ".") + ";";
             registerDependency(Type.getType(s1), "field insn s1 [" + s1 + "]");
         }
         if (s2 != null) {
             registerDependency(Type.getType(s2), "field insn s2 [" + s2 + "]");
         }
     }
 
     /**
      * Method call
      * @param i internal counter
      * @param s hosting classname of the method
      * @param s1 method name
      * @param s2 params list
      */
     public void visitMethodInsn(int i, String s, String s1, String s2) {
         //s2 arguments list
         if (s2 != null) {
             registerDependency(Type.getReturnType(s2), "Registering return type [" + s2 + "]");
             Type[] argumentTypes = Type.getArgumentTypes(s2);
             if (argumentTypes != null) {
                 for (Type argumentType : argumentTypes) {
                     registerDependency(argumentType, "Registering argument type [" + s2 + "]");
                 }
             }
         }
         //s1 method name, can be ignored
         //   s hosting classname
         if (s != null)
             registerDependency(Type.getObjectType(s), "registering callee type [" + s + "]");
 
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
         //log.log(Level.INFO, "visitMultiANewArrayInsn: {0}", new Object[]{s});
     }
 
     public void visitTryCatchBlock(Label label, Label label1, Label label2, String catchType) {
         //try catch block type information in the last string
         //log.log(Level.INFO, "visitTryCatchBlock: {0} {1} {2} {3}", new Object[]{label.toString(), label1.toString(), label2.toString(), catchType});
         registerDependency(Type.getObjectType(catchType), "catch registered type["+catchType+"]");
 
     }
 
     public void visitLocalVariable(String s, String referenceType, String s2, Label label, Label label1, int i) {
         //local variable on method level
         registerDependency(Type.getType(referenceType), "local variable registered type["+referenceType+"]");
     }
 
     public void visitLineNumber(int i, Label label) {
 
     }
 
     public void visitMaxs(int i, int i1) {
 
     }
 
     public void visitEnd() {
 
     }
 }
