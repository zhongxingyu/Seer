 /*
  * Copyright 2012, Gene McCulley
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *   Redistributions of source code must retain the above copyright notice, 
  *   this list of conditions and the following disclaimer.
  *
  *   Redistributions in binary form must reproduce the above copyright 
  *   notice, this list of conditions and the following disclaimer in the 
  *   documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 package com.stackframe.pattymelt;
 
 /**
  * An implementation of DPCU16 that is a simple emulator.
  *
  * This started out as a simple port of Brian Swetland's C version.
  *
  * @author mcculley
  */
 public class DCPU16Emulator implements DCPU16 {
 
     // FIXME: Need to count cycles.
     private boolean SKIP;
     private final short[] memory = new short[0x1002B];
     /*
      * The registers and literals are mapped in above reachable RAM. This let's
      * us use Brian Swetland's neat trick to keep dcpu_opr very general and
      * always just return a pointer. This may change in order to improve
      * performance.
      */
     private final int A = 0x10000;
     private final int B = 0x10001;
     private final int C = 0x10002;
     private final int X = 0x10003;
     private final int Y = 0x10004;
     private final int Z = 0x10005;
     private final int I = 0x10006;
     private final int J = 0x10007;
     private final int SP = 0x10008;
     private final int PC = 0x10009;
     private final int O = 0x1000A;
 
     public DCPU16Emulator() {
         SP((short) 0xffff); // FIXME: I think SP should be zero at first?
         for (short i = 0; i < 0x20; i++) {
             memory[0x1000B + i] = i;
         }
     }
 
     @Override
     public synchronized short[] memory() {
         return memory;
     }
 
     @Override
     public synchronized short PC() {
         return memory[PC];
     }
 
     @Override
     public synchronized short SP() {
         return memory[SP];
     }
 
     @Override
     public synchronized short O() {
         return memory[O];
     }
 
     @Override
     public synchronized short register(Register r) {
         return memory[0x10000 + r.ordinal()];
     }
 
     @Override
     public synchronized short A() {
         return memory[A];
     }
 
     @Override
     public synchronized short B() {
         return memory[B];
     }
 
     @Override
     public synchronized short C() {
         return memory[C];
     }
 
     @Override
     public synchronized short I() {
         return memory[I];
     }
 
     @Override
     public synchronized short J() {
         return memory[J];
     }
 
     @Override
     public synchronized short X() {
         return memory[X];
     }
 
     @Override
     public synchronized short Y() {
         return memory[Y];
     }
 
     @Override
     public synchronized short Z() {
         return memory[Z];
     }
 
     private int dcpu_opr(short code) {
         switch (code) {
             case 0x00:
             case 0x01:
             case 0x02:
             case 0x03:
             case 0x04:
             case 0x05:
             case 0x06:
             case 0x07:
                 return 0x10000 + code;
             case 0x08:
             case 0x09:
             case 0x0a:
             case 0x0b:
             case 0x0c:
             case 0x0d:
             case 0x0e:
             case 0x0f:
                 return memory[0x10000 + (code & 7)];
             case 0x10:
             case 0x11:
             case 0x12:
             case 0x13:
             case 0x14:
             case 0x15:
             case 0x16:
             case 0x17: {
                 short pc = PC();
                 PC((short) (PC() + 1));
                 return (memory[0x10000 + (code & 7)] + memory[pc]) & 0xffff;
             }
             case 0x18: {
                 int sp = SP();
                 SP((short) (sp + 1));
                 return sp & 0xffff;
             }
             case 0x19:
                 return SP();
             case 0x1a:
                 short sp = (short) (SP() - 1);
                 SP(sp);
                return sp;
             case 0x1b:
                 return SP;
             case 0x1c:
                 return PC;
             case 0x1d:
                 return O;
             case 0x1e: {
                 short pc = PC();
                 short v = memory[pc];
                 PC((short) (pc + 1));
                 return v;
             }
             case 0x1f:
                 short pc = PC();
                 PC((short) (pc + 1));
                 return pc;
             default:
                 return 0x1000B + (code & 0x1F);
         }
     }
 
     private void PC(short pc) {
         memory[PC] = pc;
     }
 
     private void SP(short sp) {
         memory[SP] = sp;
     }
 
     @Override
     public synchronized void step() throws IllegalOpcodeException {
         int pc = PC() & 0xffff;
         short op = memory[pc];
         PC((short) (PC() + 1));
         short dst;
         int a, b, aa;
 
         if ((op & 0xF) == 0) {
             switch ((op >> 4) & 0x3F) {
                 case 0x01:
                     a = memory[dcpu_opr((short) (op >> 10))];
                     if (SKIP) {
                         SKIP = false;
                     } else {
                         int sp = (SP() - 1) & 0xffff;
                         SP((short) sp);
                         memory[sp] = PC();
                         PC((short) a);
                     }
 
                     return;
                 default:
                     throw new IllegalOpcodeException(op);
             }
         }
 
         dst = (short) ((op >> 4) & 0x3F);
         aa = dcpu_opr(dst);
         a = memory[aa];
         short b_op = (short) (op >> 10);
         int b_addr = dcpu_opr(b_op);
         b = memory[b_addr];
 
         int res;
         switch (op & 0xF) {
             case 0x1:
                 res = b;
                 break;
             case 0x2:
                 res = a + b;
                 break;
             case 0x3:
                 res = a - b;
                 break;
             case 0x4:
                 res = a * b;
                 break;
             case 0x5:
                 if (b != 0) {
                     res = a / b;
                 } else {
                     res = 0;
                 }
 
                 break;
             case 0x6:
                 if (b != 0) {
                     res = a % b;
                 } else {
                     res = 0;
                 }
 
                 break;
             case 0x7:
                 res = a << b;
                 break;
             case 0x8:
                 res = a >> b;
                 break;
             case 0x9:
                 res = a & b;
                 break;
             case 0xA:
                 res = a | b;
                 break;
             case 0xB:
                 res = a ^ b;
                 break;
             case 0xC:
                 res = (a == b) ? 1 : 0;
                 if (SKIP) {
                     SKIP = false;
                     return;
                 }
 
                 SKIP = res == 0;
                 return;
             case 0xD:
                 res = (a != b) ? 1 : 0;
                 if (SKIP) {
                     SKIP = false;
                     return;
                 }
 
                 SKIP = res == 0;
                 return;
             case 0xE:
                 res = (a > b) ? 1 : 0;
                 if (SKIP) {
                     SKIP = false;
                     return;
                 }
 
                 SKIP = res == 0;
                 return;
             case 0xF:
                 res = ((a & b) != 0) ? 1 : 0;
                 if (SKIP) {
                     SKIP = false;
                     return;
                 }
 
                 SKIP = res == 0;
                 return;
             default:
                 res = -1;
                 throw new AssertionError("Shouldn't be able to get here");
         }
 
         if (SKIP) {
             SKIP = false;
             return;
         }
 
         switch (op & 0xF) {
             case 0x2:
             case 0x3:
             case 0x4:
             case 0x5:
             case 0x7:
             case 0x8:
                 memory[O] = (short) (res >> 16);
             case 0x1:
             case 0x6:
             case 0x9:
             case 0xA:
             case 0xB:
                 if (dst < 0x1f) {
                     memory[aa] = (short) res;
                 }
 
                 break;
             case 0xC:
             case 0xD:
             case 0xE:
             case 0xF:
                 SKIP = res == 0;
         }
     }
 
     @Override
     public void run() {
         while (true) {
             try {
                 step();
             } catch (IllegalOpcodeException ioe) {
                 throw new RuntimeException(ioe);
             }
         }
     }
 }
