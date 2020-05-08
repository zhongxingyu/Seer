 package com.infomancers.collections.yield.asm.delayed;
 
 import org.objectweb.asm.MethodAdapter;
 import org.objectweb.asm.MethodVisitor;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Stack;
 
 /**
  * Copyright (c) 2007, Aviad Ben Dov
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
 public class DelayedMethodVisitor extends MethodAdapter {
     private static class MiniFrame {
         public Queue<DelayedInstructionEmitter> workQueue = new LinkedList<DelayedInstructionEmitter>();
         int stackSize = 0;
     }
 
     private static HashSet<Integer> poppingCodes = new HashSet<Integer>();
     private static HashSet<Integer> pushingCodes = new HashSet<Integer>();
 
     private Stack<MiniFrame> miniFrames = new Stack<MiniFrame>();
     private MiniFrame currentMiniFrame = null;
 
 
     /**
      * Constructs a new {@link org.objectweb.asm.MethodAdapter} object.
      *
      * @param mv the code visitor to which this adapter must delegate calls.
      */
     public DelayedMethodVisitor(MethodVisitor mv) {
         super(mv);
     }
 
 
     @Override
     public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
         super.visitMethodInsn(opcode, owner, name, desc);
 
         delayInsn(opcode, DelayedInstruction.METHOD.createEmitter(opcode, owner, name, desc));
     }
 
 
     @Override
     public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
         super.visitFieldInsn(opcode, owner, name, desc);
 
         delayInsn(opcode, DelayedInstruction.FIELD.createEmitter(opcode, owner, name, desc));
     }
 
     protected final void startMiniFrame() {
         if (currentMiniFrame != null) {
             miniFrames.push(currentMiniFrame);
         }
 
         currentMiniFrame = new MiniFrame();
     }
 
     private void delayInsn(int opcode, DelayedInstructionEmitter emitter) {
         currentMiniFrame.workQueue.offer(emitter);
 
         if (poppingCodes.contains(opcode)) {
             if (currentMiniFrame.stackSize == 0) {
                 throw new IllegalStateException("Popping from stack when the stack is empty? " +
                         "Probably missing popping/pushing instruction");
             }
             currentMiniFrame.stackSize--;
        } else {
             currentMiniFrame.stackSize++;
         }
     }
 
     protected final void emit(MethodVisitor mv, int count) {
         if (count <= 0) {
             throw new IllegalArgumentException("count <= 0");
         }
 
         while (count-- > 0) {
             currentMiniFrame.workQueue.poll().emit(mv);
         }
     }
 
     protected final void emitAll(MethodVisitor mv) {
         emit(mv, currentMiniFrame.workQueue.size());
     }
 
     protected final void endMiniFrame() {
         if (currentMiniFrame.stackSize > 0) {
             throw new IllegalStateException("Ending mini-frame when stack still has values!");
         }
 
         currentMiniFrame = miniFrames.isEmpty() ? null : miniFrames.pop();
     }
 }
