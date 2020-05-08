 package com.infomancers.tests.enhancers;
 
 import com.infomancers.collections.yield.asm.NewMember;
 import com.infomancers.collections.yield.asm.TypeDescriptor;
 import com.infomancers.collections.yield.asmbase.YielderInformationContainer;
 import com.infomancers.collections.yield.asmtree.InsnEnhancer;
 import com.infomancers.collections.yield.asmtree.enhancers.ArrayLoadEnhancer;
 import com.infomancers.tests.TestYIC;
 import static com.infomancers.tests.enhancers.TestUtil.compareLists;
 import static com.infomancers.tests.enhancers.TestUtil.createList;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.tree.*;
 import org.objectweb.asm.util.ASMifierClassVisitor;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Collection;
 
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
 
 @RunWith(Parameterized.class)
 public class ArrayLoadEnhancerTests extends EnhancerTestsBase {
 
     @Parameterized.Parameters
     public static Collection<Object[]> parameters() {
         return Arrays.asList(
                 new Object[]{Opcodes.IALOAD, "[I"},
                 new Object[]{Opcodes.FALOAD, "[F"},
                 new Object[]{Opcodes.DALOAD, "[D"},
                new Object[]{Opcodes.LALOAD, "[J"},
                new Object[]{Opcodes.AALOAD, "[Ljava/lang/Object;"},
                 new Object[]{Opcodes.BALOAD, "[B"},
                 new Object[]{Opcodes.CALOAD, "[C"},
                 new Object[]{Opcodes.SALOAD, "[S"}
         );
     }
 
     private final int opcode;
     private final String desc;
 
     public ArrayLoadEnhancerTests(int opcode, String desc) {
         this.opcode = opcode;
         this.desc = desc;
     }
 
     @Test
     public void arrayLoad_fromConst() {
         YielderInformationContainer info = new TestYIC(1);
 
         final InsnNode insn = new InsnNode(opcode);
         InsnList original = createList(
                 new VarInsnNode(Opcodes.ALOAD, 1),
                 new InsnNode(Opcodes.ICONST_0),
                 insn);
 
         InsnList expected = createList(
                 new VarInsnNode(Opcodes.ALOAD, 1),
                 new TypeInsnNode(Opcodes.CHECKCAST, desc),
                 new InsnNode(Opcodes.ICONST_0),
                 new InsnNode(opcode)
         );
 
         InsnEnhancer enhancer = new ArrayLoadEnhancer();
 
         enhancer.enhance(owner, original, info, insn);
 
         compareLists(expected, original);
     }
 
     @Test
     public void arrayLoad_fromField() {
         YielderInformationContainer info = new TestYIC(1,
                 new NewMember(1, TypeDescriptor.Integer));
 
         final InsnNode insn = new InsnNode(opcode);
         final NewMember slot = info.getSlot(1);
         InsnList original = createList(
                 new VarInsnNode(Opcodes.ALOAD, 1),
                 new VarInsnNode(Opcodes.ALOAD, 0),
                 new FieldInsnNode(Opcodes.GETFIELD, owner.name, slot.getName(), slot.getDesc()),
                 insn);
 
         InsnList expected = createList(
                 new VarInsnNode(Opcodes.ALOAD, 1),
                 new TypeInsnNode(Opcodes.CHECKCAST, desc),
                 new VarInsnNode(Opcodes.ALOAD, 0),
                 new FieldInsnNode(Opcodes.GETFIELD, owner.name, slot.getName(), slot.getDesc()),
                 new InsnNode(opcode)
         );
 
         InsnEnhancer enhancer = new ArrayLoadEnhancer();
 
         enhancer.enhance(owner, original, info, insn);
 
         compareLists(expected, original);
     }
 
     public static void main(String[] args) throws IOException {
         Object c = new Object() {
             Object a = new int[]{22};
 
             void method() {
                 ((int[]) a)[0]++;
             }
         };
 
         ASMifierClassVisitor visit = new ASMifierClassVisitor(new PrintWriter(System.out));
         ClassReader reader = new ClassReader(c.getClass().getName());
 
         reader.accept(visit, 0);
     }
 }
