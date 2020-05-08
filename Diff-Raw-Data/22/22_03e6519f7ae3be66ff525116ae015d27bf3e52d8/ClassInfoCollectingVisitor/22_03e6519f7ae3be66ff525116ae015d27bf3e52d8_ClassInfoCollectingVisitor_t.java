 /*
  * junitflood - An automatic junit test generator
  * Copyright 2011 MeBigFatGuy.com
  * Copyright 2011 Dave Brosius
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
 package com.mebigfatguy.junitflood.classpath;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.objectweb.asm.AnnotationVisitor;
 import org.objectweb.asm.Attribute;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 public class ClassInfoCollectingVisitor implements ClassVisitor {
 
 	private final Map<LookupType, Map<String, Access>> clsInfo;
 
 	public ClassInfoCollectingVisitor(Map<LookupType, Map<String, Access>> info) {
 		clsInfo = info;
 	}
 
 	@Override
 	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
 		Map<String, Access> clsInterfaces = clsInfo.get(LookupType.INTERFACE);
 		if (clsInterfaces == null) {
 			clsInterfaces = new HashMap<String, Access>();
 			clsInfo.put(LookupType.INTERFACE, clsInterfaces);
 		}
 		for (String inf : interfaces) {
 			clsInterfaces.put(inf, Access.PUBLIC);
 		}
 
 		Map<String, Access> clsSupers = clsInfo.get(LookupType.SUPERCLASS);
 		if (clsSupers == null) {
 			clsSupers = new HashMap<String, Access>();
 			clsInfo.put(LookupType.SUPERCLASS, clsSupers);
 		}
 		clsSupers.put(superName, Access.PUBLIC);
 	}
 
 	@Override
 	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
 		return null;
 	}
 
 	@Override
 	public void visitAttribute(Attribute attr) {
 	}
 
 	@Override
 	public void visitEnd() {
 		Map<String, Access> clsConstructors = clsInfo.get(LookupType.CONSTRUCTOR);
		if (clsConstructors == null) {
			clsConstructors = new HashMap<String, Access>();
			clsInfo.put(LookupType.CONSTRUCTOR, clsConstructors);
 			clsConstructors.put("()V", Access.PUBLIC);
 		}
 	}
 
 	@Override
 	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
 		return null;
 	}
 
 	@Override
 	public void visitInnerClass(String name, String outerName, String innerName, int access) {
 	}
 
 	@Override
 	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
 		if ("<init>".equals(name)) {
 			Map<String, Access> clsConstructors = clsInfo.get(LookupType.CONSTRUCTOR);
 			if (clsConstructors == null) {
 				clsConstructors = new HashMap<String, Access>();
 				clsInfo.put(LookupType.CONSTRUCTOR, clsConstructors);
 			}
 
 			Access clsAccess = convertAccess(access);
 			clsConstructors.put(desc, clsAccess);
 		} else if (!"<clinit>".equals(name)) {
 			Map<String, Access> clsMethods = clsInfo.get(LookupType.METHOD);
 			if (clsMethods == null) {
 				clsMethods = new HashMap<String, Access>();
 				clsInfo.put(LookupType.METHOD, clsMethods);
 			}
 
 			Access clsAccess = convertAccess(access);
 			clsMethods.put(name + desc, clsAccess);
 		}
 		return null;
 	}
 
 	@Override
 	public void visitOuterClass(String owner, String name, String desc) {
 	}
 
 	@Override
 	public void visitSource(String source, String debug) {
 	}
 
 	private Access convertAccess(int access) {
 		switch (access) {
 			case Opcodes.ACC_PRIVATE:
 				return Access.PRIVATE;
 			case Opcodes.ACC_PROTECTED:
 				return Access.PROTECTED;
 			case Opcodes.ACC_PUBLIC:
 				return Access.PUBLIC;
 		    default:
 		    	return Access.PACKAGE;
 		}
 	}
 }
