 /**
  *
  * Copyright 2010-2011 (C) The original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.toolazydogs.aunit.internal;
 
 import java.lang.reflect.Constructor;
 
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.Lexer;
 import org.antlr.runtime.RecognizerSharedState;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 
 /**
  *
  */
 public class LexerFactory implements Opcodes
 {
     private final Class<? extends Lexer> lexerClass;
     private final boolean failOnError;
     private Class<? extends Lexer> wrapperClass;
 
     public LexerFactory(Class<? extends Lexer> lexerClass, boolean failOnError)
     {
         this.lexerClass = lexerClass;
         this.failOnError = failOnError;
     }
 
     public Lexer generate() throws Exception
     {
         return getWrapperClass().newInstance();
     }
 
     public Lexer generate(CharStream input) throws Exception
     {
         Class<? extends Lexer> wc = getWrapperClass();
         Constructor c = wc.getConstructor(CharStream.class);
         LexerWrapper wrapper = (LexerWrapper)c.newInstance(input);
 
         wrapper.setFailOnError(failOnError);
 
         return (Lexer)wrapper;
     }
 
     public Lexer generate(CharStream input, RecognizerSharedState state) throws Exception
     {
         Class<? extends Lexer> wc = getWrapperClass();
         Constructor c = wc.getConstructor(CharStream.class, RecognizerSharedState.class);
         LexerWrapper wrapper = (LexerWrapper)c.newInstance(input, state);
 
         wrapper.setFailOnError(failOnError);
 
         return (Lexer)wrapper;
     }
 
     private Class<? extends Lexer> getWrapperClass()
     {
         if (wrapperClass == null) wrapperClass = loadClass();
         return wrapperClass;
     }
 
     private Class<? extends Lexer> loadClass()
     {
         final String parent = lexerClass.getName().replace('.', '/');
         final String name = "com/toolazydogs/aunit/asm/" + parent;
 
         ClassWriter cw = new ClassWriter(0);
         FieldVisitor fv;
         MethodVisitor mv;
 
         cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, name, null, parent, new String[]{"com/toolazydogs/aunit/internal/LexerWrapper"});
 
         {
             fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "errors", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/String;>;", null);
             fv.visitEnd();
         }
         {
             fv = cw.visitField(ACC_PRIVATE, "failOnError", "Z", null, null);
             fv.visitEnd();
         }
         {
             mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
             mv.visitCode();
             mv.visitVarInsn(ALOAD, 0);
             mv.visitMethodInsn(INVOKESPECIAL, parent, "<init>", "()V");
             mv.visitVarInsn(ALOAD, 0);
             mv.visitTypeInsn(NEW, "java/util/ArrayList");
             mv.visitInsn(DUP);
             mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
             mv.visitFieldInsn(PUTFIELD, name, "errors", "Ljava/util/List;");
             mv.visitVarInsn(ALOAD, 0);
             mv.visitInsn(ICONST_1);
             mv.visitFieldInsn(PUTFIELD, name, "failOnError", "Z");
             mv.visitInsn(RETURN);
             mv.visitMaxs(3, 1);
             mv.visitEnd();
         }
         {
             mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lorg/antlr/runtime/CharStream;)V", null, null);
             mv.visitCode();
             mv.visitVarInsn(ALOAD, 0);
             mv.visitVarInsn(ALOAD, 1);
             mv.visitMethodInsn(INVOKESPECIAL, parent, "<init>", "(Lorg/antlr/runtime/CharStream;)V");
             mv.visitVarInsn(ALOAD, 0);
             mv.visitTypeInsn(NEW, "java/util/ArrayList");
             mv.visitInsn(DUP);
             mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
             mv.visitFieldInsn(PUTFIELD, name, "errors", "Ljava/util/List;");
             mv.visitVarInsn(ALOAD, 0);
             mv.visitInsn(ICONST_1);
             mv.visitFieldInsn(PUTFIELD, name, "failOnError", "Z");
             mv.visitInsn(RETURN);
             mv.visitMaxs(3, 2);
             mv.visitEnd();
         }
         {
             mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lorg/antlr/runtime/CharStream;Lorg/antlr/runtime/RecognizerSharedState;)V", null, null);
             mv.visitCode();
             mv.visitVarInsn(ALOAD, 0);
             mv.visitVarInsn(ALOAD, 1);
             mv.visitVarInsn(ALOAD, 2);
             mv.visitMethodInsn(INVOKESPECIAL, parent, "<init>", "(Lorg/antlr/runtime/CharStream;Lorg/antlr/runtime/RecognizerSharedState;)V");
             mv.visitVarInsn(ALOAD, 0);
             mv.visitTypeInsn(NEW, "java/util/ArrayList");
             mv.visitInsn(DUP);
             mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
             mv.visitFieldInsn(PUTFIELD, name, "errors", "Ljava/util/List;");
             mv.visitVarInsn(ALOAD, 0);
             mv.visitInsn(ICONST_1);
             mv.visitFieldInsn(PUTFIELD, name, "failOnError", "Z");
             mv.visitInsn(RETURN);
             mv.visitMaxs(3, 3);
             mv.visitEnd();
         }
         {
             mv = cw.visitMethod(ACC_PUBLIC, "emitErrorMessage", "(Ljava/lang/String;)V", null, null);
             mv.visitCode();
             mv.visitVarInsn(ALOAD, 0);
             mv.visitFieldInsn(GETFIELD, name, "errors", "Ljava/util/List;");
             mv.visitVarInsn(ALOAD, 1);
             mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
             mv.visitInsn(POP);
             mv.visitInsn(RETURN);
             mv.visitMaxs(2, 2);
             mv.visitEnd();
         }
         {
             mv = cw.visitMethod(ACC_PUBLIC, "getErrors", "()Ljava/util/List;", "()Ljava/util/List<Ljava/lang/String;>;", null);
             mv.visitCode();
             mv.visitVarInsn(ALOAD, 0);
             mv.visitFieldInsn(GETFIELD, name, "errors", "Ljava/util/List;");
             mv.visitInsn(ARETURN);
             mv.visitMaxs(1, 1);
             mv.visitEnd();
         }
         {
             mv = cw.visitMethod(ACC_PUBLIC, "isFailOnError", "()Z", null, null);
             mv.visitCode();
             mv.visitVarInsn(ALOAD, 0);
             mv.visitFieldInsn(GETFIELD, name, "failOnError", "Z");
             mv.visitInsn(IRETURN);
             mv.visitMaxs(1, 1);
             mv.visitEnd();
         }
         {
             mv = cw.visitMethod(ACC_PUBLIC, "setFailOnError", "(Z)V", null, null);
             mv.visitCode();
             mv.visitVarInsn(ALOAD, 0);
             mv.visitVarInsn(ILOAD, 1);
             mv.visitFieldInsn(PUTFIELD, name, "failOnError", "Z");
             mv.visitInsn(RETURN);
             mv.visitMaxs(2, 2);
             mv.visitEnd();
         }
         cw.visitEnd();
 
         byte[] b = cw.toByteArray();
 
        AunitClassLoader ac = new AunitClassLoader(lexerClass.getClassLoader());
         return ac.defineClass(name.replace('/', '.'), b);
     }
 }
