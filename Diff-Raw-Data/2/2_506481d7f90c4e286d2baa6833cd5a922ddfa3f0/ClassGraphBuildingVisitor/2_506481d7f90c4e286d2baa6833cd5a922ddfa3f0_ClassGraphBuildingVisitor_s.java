 /*
  * ClassGraph - a dependency graph display
  * Copyright 2013 MeBigFatGuy.com
  * Copyright 2013 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.classgraph;
 
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.Opcodes;
 
 public class ClassGraphBuildingVisitor extends ClassVisitor {
 
     private ClassNodes classNodes;
     private String clsName;
     
 	public ClassGraphBuildingVisitor(ClassNodes nodes) {
 		super(Opcodes.ASM4);
 		classNodes = nodes;
 	}
 
 	@Override
 	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
 	    clsName = name.replace("/",  ".");
 	    String superClsName = superName.replace("/",  ".");
 	    
 	    classNodes.addRelationship(clsName, superClsName);
 	    
 	    for (String inf : interfaces) {
 	        String interfaceClsName = inf.replace("/",  ".");
 	        classNodes.addRelationship(clsName, interfaceClsName);
 	    }
 	}
 
 	@Override
 	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
 	    if (desc.startsWith("L")) {
    		String fieldClsName = desc.substring(1, desc.length() - 1);
             classNodes.addRelationship(clsName, fieldClsName);
 	    }
         return null;
 	}
 
     @Override
     public void visitInnerClass(String name, String outerName, String innerName, int access) {
         
         String outerClsName = outerName.replace("/", ".");
         String innerClsName = outerClsName + "$" + innerName.replace("/", ".");
         
         classNodes.addRelationship(outerClsName, innerClsName);
     }
 }
