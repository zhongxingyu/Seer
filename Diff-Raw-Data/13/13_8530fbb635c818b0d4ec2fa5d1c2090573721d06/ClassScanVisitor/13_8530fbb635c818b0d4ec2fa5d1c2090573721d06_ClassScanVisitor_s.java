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
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 class ClassScanVisitor implements ClassVisitor {
 
     Set<String> dependencies;
     Set<String> whiteList;
     static final Logger log = Logger.getLogger("ClassScanVisitor");
 
     public ClassScanVisitor() {
 
     }
 
     public ClassScanVisitor(Set<String> dependencies) {
         super();
         this.dependencies = dependencies;
     }
 
     public void setDependencyTarget(Set<String> dependencyTarget) {
         dependencies = dependencyTarget;
     }
 
     public void visit(int version, int access, String name,
                       String signature, String superName, String[] interfaces) {
        //log.log(Level.INFO, "{0} extends {1} ", new String[]{name, superName});
         if(name.contains("BlogService")) {
            System.out.println("Debugpint found");
        }
 
         registerDependency(Type.getObjectType(superName), "Super name[" + superName + "]");
         if (interfaces != null && interfaces.length > 0) {
             for (String currInterface : interfaces) {
                 registerDependency(Type.getObjectType(currInterface), "interface [" + superName + "]");
             }
         }
     }
 
     public void visitSource(String source, String debug) {
         //log.log(Level.INFO, "source: {0}", source);
     }
 
     public void visitOuterClass(String owner, String name, String desc) {
         //nothing has to be done here I guess because
         //we only try to fetch the dependencies
     }
 
     public AnnotationVisitor visitAnnotation(String desc,
                                              boolean visible) {
         registerDependency(Type.getType(desc), "registering annotation [" + desc + "]");
 
         return null;
     }
 
     public void visitAttribute(Attribute attr) {
         //log.log(Level.INFO, "Attribute: {0}", attr.type);
         System.out.println(attr.getClass().getName());
     }
 
     public void visitInnerClass(String name, String outerName,
                                 String innerName, int access) {
         //same as outer class
     }
 
     public FieldVisitor visitField(int access, String name, String desc,
                                    String signature, Object value) {
         //log.log(Level.INFO, "Field:{0} {1} ", new Object[]{desc, name});
         registerDependency(Type.getType(desc), "field type  [" + desc + "]");
 
         return null;
     }
 
     private void registerDependency(Type dependency, String desc) {
         String className = dependency.getClassName();
         if (className.endsWith("[]")) {
             className = className.substring(0, className.indexOf("["));
         }
         ClassScanUtils.logParmList(dependencies, whiteList, className);
     }
 
     public MethodVisitor visitMethod(int access, String name,
                                      String desc, String signature, String[] exceptions) {
 
         registerDependency(Type.getReturnType(desc), "Return type of the method [" + name + "]");
 
         for (Type argumentType : Type.getArgumentTypes(desc)) {
             registerDependency(argumentType, "Argument type of the method [" + name + "]");
         }
 
         return new MethodScanVisitor(dependencies, whiteList);
     }
 
     public void visitEnd() {
         //log.info("}");
     }
 
     public void setWhiteList(Set<String> whiteList) {
         this.whiteList = whiteList;
     }
 }
 
