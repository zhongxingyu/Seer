 package com.infomancers.collections.yield.asmtree.enhancers;
 
 import com.infomancers.collections.yield.asmbase.YielderInformationContainer;
 import com.infomancers.collections.yield.asmtree.CodeStack;
 import com.infomancers.collections.yield.asmtree.Util;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.tree.*;
 
 /**
  * Copyright (c) 2009, Aviad Ben Dov
  * <p/>
  * All rights reserved.
  * <p/>
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  * <p/>
  * 1. Redistributions of source code must retain the above copyright notice, this list
  * of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, this
  * list of conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  * 3. Neither the name of Infomancers, Ltd. nor the names of its contributors may be
  * used to endorse or promote products derived from this software without specific
  * prior written permission.
  * <p/>
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 /**
  * Stack should be [..., array, index].
  * <p/>
  * Note: Special case for the BALOAD opcode, which might load from a [B or a [Z (boolean array)!
  * <p/>
  * in that case, the bytecode will be replaced to:
  * <code>
  * if (arr.getClass().getComponentType().equals(Byte.TYPE)) {
  * CHECKCAST [B
  * else
  * CHECKCAST [Z
  * <p/>
  * BALOAD
  * </code>
  */
 public final class ArrayLoadEnhancer implements PredicatedInsnEnhancer {
     private static final String[] descs = "[I,[J,[F,[D,[Ljava/lang/Object;,[B,[C,[S".split(",");
 
     public AbstractInsnNode enhance(ClassNode clz, InsnList instructions, YielderInformationContainer info, AbstractInsnNode instruction) {
         if (instruction.getOpcode() == Opcodes.BALOAD) {
             LabelNode l1 = new LabelNode();
             LabelNode l2 = new LabelNode();
 
             final AbstractInsnNode ret;
 
             InsnList testAndCast = com.infomancers.collections.yield.asmtree.Util.createList(
                     ret = new InsnNode(Opcodes.DUP_X1),   // stack: [..., index, array, index]
                     new InsnNode(Opcodes.POP),      // stack: [..., index, array]
                     new InsnNode(Opcodes.DUP_X1),   // stack: [..., array, index, array]
                     new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;"),
                     new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getComponentType", "()Ljava/lang/Class;"),
                    new MethodInsnNode(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;"),
                     new JumpInsnNode(Opcodes.IF_ACMPNE, l1),
                     new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/reflect/Array", "getByte", "(Ljava/lang/Object;I)B"),
                     new JumpInsnNode(Opcodes.GOTO, l2),
                     l1,
                     new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/reflect/Array", "getBoolean", "(Ljava/lang/Object;I)Z"),
                     l2
             );
 
             Util.insertOrAdd(instructions, instruction, testAndCast);
             instructions.remove(instruction);
 
             return ret;
         } else {
            AbstractInsnNode prev = CodeStack.backUntilStackSizedAt(instruction, 0, true);
 
             TypeInsnNode checkcast = new TypeInsnNode(Opcodes.CHECKCAST, descs[instruction.getOpcode() - Opcodes.IALOAD]);
 
             Util.insertOrAdd(instructions, prev, checkcast);
 
             return instruction;
         }
     }
 
     public boolean shouldEnhance(AbstractInsnNode node) {
         return node.getOpcode() >= Opcodes.IALOAD && node.getOpcode() <= Opcodes.SALOAD;
     }
 }
