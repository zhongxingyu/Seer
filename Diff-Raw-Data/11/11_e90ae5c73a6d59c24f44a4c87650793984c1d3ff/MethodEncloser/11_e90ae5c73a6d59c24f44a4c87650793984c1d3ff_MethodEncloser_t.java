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
  * File:                org.anon.smart.base.stt.asm.MethodEncloser
  * Author:              rsankar
  * Revision:            1.0
  * Date:                28-12-2012
  *
  * ************************************************************
  * REVISIONS
  * ************************************************************
  * An encloser for methods
  *
  * ************************************************************
  * */
 
 package org.anon.smart.base.stt.asm;
 
 import java.util.List;
 
 import org.objectweb.asm.commons.AdviceAdapter;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 import org.anon.smart.base.stt.MethodDet;
 
 public class MethodEncloser extends AdviceAdapter
 {
     private ASMMethodContext _context;
 
     public MethodEncloser(ASMMethodContext ctx)
     {
        super(Opcodes.ASM4, ctx.visitor(), ctx.access(), ctx.name(), ctx.description());
         _context = ctx;
     }
 
     private void bcicall(MethodDet mthd)
     {
         if (_context.shouldBCI(mthd))
         {
             super.visitVarInsn(ALOAD, 0);
             if (mthd.hasClazzParam())
                 super.visitLdcInsn(_context.descriptor().clazzName());
             if (mthd.hasMthdParam())
                 super.visitLdcInsn(_context.name());
             if ((mthd.access() & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)
                 super.visitMethodInsn(INVOKESPECIAL, _context.descriptor().clazzName(), mthd.name(), mthd.signature());
             else
                 super.visitMethodInsn(INVOKEVIRTUAL, _context.descriptor().clazzName(), mthd.name(), mthd.signature());
         }
     }
 
     protected void onMethodEnter()
     {
         if (!_context.shouldBCI(true))
             return;
 
         List<MethodDet> mthds = _context.enterMethods();
         for (MethodDet mthd : mthds)
             bcicall(mthd);
     }
 
     protected void onMethodExit(int opcode)
     {
         if (!_context.shouldBCI(false))
             return;
 
         List<MethodDet> mthds = _context.exitMethods();
         for (MethodDet mthd : mthds)
             bcicall(mthd);
     }
 
     public void visitMaxs(int stack, int locals)
     {
         super.visitMaxs(stack, locals);
     }
 }
 
