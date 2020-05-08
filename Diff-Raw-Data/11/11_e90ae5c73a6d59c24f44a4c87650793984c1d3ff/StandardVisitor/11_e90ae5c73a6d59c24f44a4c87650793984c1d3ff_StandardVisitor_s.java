 /**
  * SMART - State Machine ARchiTecture
  *
  * Copyright (C) 2012 Individual contributors as indicated by
  * the @authors tag
  *
  * This file is a part of SMART.
  *
  * SMART is a free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SMART is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  *
  *
  * */
  
 /**
  * ************************************************************
  * HEADERS
  * ************************************************************
  * File:                org.anon.smart.base.stt.asm.StandardVisitor
  * Author:              rsankar
  * Revision:            1.0
  * Date:                28-12-2012
  *
  * ************************************************************
  * REVISIONS
  * ************************************************************
  * A standard visitor that templatizes
  *
  * ************************************************************
  * */
 
 package org.anon.smart.base.stt.asm;
 
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.MethodVisitor;
 
 import org.anon.smart.base.stt.STTVisitor;
 import org.anon.smart.base.stt.ClazzDescriptor;
 
 public class StandardVisitor extends ClassVisitor
 {
     private STTVisitor _visitor;
     private ClazzDescriptor _descriptor;
     private ASMClazzContext _clazzContext;
 
     public StandardVisitor(ClassVisitor cv, STTVisitor visit)
     {
        super(1, cv);
         _visitor = visit;
         _descriptor = visit.descriptor();
     }
 
     public void visit (int version, int access, String name, String signature, String superName, String[] interfaces)
     {
         ASMClazzContext ctx = new ASMClazzContext(_descriptor, name, signature, access, version, superName, interfaces, null);
         _visitor.visit(ctx);
         _clazzContext = ctx;
 
         super.visit(ctx.version(), ctx.access(), ctx.name(), ctx.signature(), ctx.superClazz(), ctx.interfaces());
     }
 
     public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
     {
         FieldVisitor fv = super.visitField(access, name, desc, signature, value);
         ASMFieldContext ctx = new ASMFieldContext(_descriptor, name, signature, access, desc, value, fv);
         _visitor.visit(ctx);
         return fv;
     }
 
     public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
     {
         MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
         ASMMethodContext ctx = new ASMMethodContext(_descriptor, name, signature, access, desc, exceptions, mv);
         MethodVisitor p = new MethodEncloser(ctx);
 
         ctx.changeVisitor(p);
         _visitor.visit(ctx);
 
         return p;
     }
 
     public void visitEnd()
     {
         _clazzContext.setClassVisitor(cv);
         _visitor.visitEnd(_clazzContext);
         super.visitEnd();
     }
 
 }
 
