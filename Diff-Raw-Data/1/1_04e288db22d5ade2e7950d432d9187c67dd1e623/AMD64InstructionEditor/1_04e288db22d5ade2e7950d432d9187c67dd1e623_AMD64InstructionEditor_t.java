 /*
  *  This file is part of the jasm project (http://code.google.com/p/jasm).
  *
  *  This file is licensed to you under the BSD License; You may not use
  *  this file except in compliance with the License. See the LICENSE.txt
  *  file distributed with this work for a copy of the License and information
  *  regarding copyright ownership.
  */
 package jasm.amd64;
 
 import jasm.AssemblyException;
 import jasm.AssemblyInstructionEditor;
 import jasm.WordWidth;
 
 /**
  * Facility to patch AMD64 assembly instructions.
  *
  * @author Laurent Daynes
  */
 public class AMD64InstructionEditor extends AMD64Assembler implements AssemblyInstructionEditor {
     // Buffer holding the instruction to edit
     final byte[] _instruction;
     // Offset to the first byte of the instruction
     final int _startOffset;
     // length (in bytes) of the instruction
     final int _length;
 
     /**
      * For editing an instruction stored in a byte array.
      * The instruction is edited in place.
      */
     public AMD64InstructionEditor(byte[] instruction) {
         super();
         _startOffset = 0;
         _length = instruction.length;
         _instruction = instruction;
     }
 
     /**
      * For editing an instruction at a particular offset in code.
      * The instruction is edited in place.
      */
     public AMD64InstructionEditor(byte[] code, int startOffset, int length) {
         super();
         _startOffset = startOffset;
         _length = length;
         _instruction = code;
     }
 
     public  int getIntDisplacement(WordWidth displacementWidth) throws AssemblyException {
         // Displacement always appended in the end. Same as immediate
         final int displacementOffset = _startOffset + _length - displacementWidth.numberOfBytes();
         switch(displacementWidth) {
             case BITS_8:
                 return _instruction[displacementOffset];
             case BITS_16:
                 return getImm16(displacementOffset);
             case BITS_32:
                 return getImm32(displacementOffset);
             default:
                 throw new AssemblyException("invalid width for a displacement");
         }
     }
 
     public int getIntImmediate(WordWidth immediateWidth) throws AssemblyException {
         final int immediateOffset = _startOffset + _length - immediateWidth.numberOfBytes();
         switch(immediateWidth) {
             case BITS_8:
                 return _instruction[immediateOffset];
             case BITS_16:
                 return getImm16(immediateOffset);
             case BITS_32:
                 return getImm32(immediateOffset);
             default:
                 throw new AssemblyException("invalid width for an integer value");
         }
     }
 
     public void fixDisplacement(WordWidth displacementWidth, boolean withIndex, byte disp8) {
         // TODO: various invariant control here to make sure that the template of the assembly
         // instruction here has a displacement of the specified width
 
         // Displacement always appended in the end.
         final int displacementOffset = _startOffset + _length - displacementWidth.numberOfBytes();
 
         // low order byte come first, so we write the offset value first, regardless of the width of the original offset.
         _instruction[displacementOffset] = disp8;
         if (displacementWidth == WordWidth.BITS_32) {
             _instruction[displacementOffset + 1] = 0;
             _instruction[displacementOffset + 2] = 0;
             _instruction[displacementOffset + 3] = 0;
         }
     }
 
     private void fixImm8(int startOffset, int imm8) {
         _instruction[startOffset] = (byte) (imm8 & 0xff);
     }
 
     private int getImm8(int startOffset) {
         return _instruction[startOffset] & 0xff;
     }
 
     private void fixImm16(int startOffset, int imm16) {
         int imm = imm16;
         _instruction[startOffset] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 1] = (byte) (imm & 0xff);
     }
 
     private int getImm16(int startOffset) {
         int imm16 = 0;
         imm16 |= getImm8(startOffset + 1) << 8;
         imm16 |= getImm8(startOffset);
         return imm16;
     }
 
     private void fixImm32(int startOffset, int imm32) {
         int imm = imm32;
         _instruction[startOffset] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 1] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 2] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 3] = (byte) (imm & 0xff);
     }
 
     private int getImm32(int startOffset) {
         int imm32 = 0;
         imm32 |= getImm16(startOffset + 2) << 16;
         imm32 |= getImm16(startOffset);
         return imm32;
     }
 
     private void fixImm64(int startOffset, long imm64) {
         long imm = imm64;
         _instruction[startOffset] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 1] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 2] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 3] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 4] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 5] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 6] = (byte) (imm & 0xff);
         imm >>= 8;
         _instruction[startOffset + 7] = (byte) (imm & 0xff);
     }
 
     private int getImm64(int startOffset) {
         int imm64 = 0;
         imm64 |= getImm32(startOffset + 4) << 32;
         imm64 |= getImm32(startOffset);
         return imm64;
     }
 
     public void fixDisplacement(WordWidth displacementWidth, boolean withIndex, int disp32) throws AssemblyException {
         if (displacementWidth != WordWidth.BITS_32) {
             throw new AssemblyException("Invalid offset width. Can't");
         }
         // index to the first byte displacement parameter (it's always appended in the end).
         final int displacementStart = _startOffset + _length - displacementWidth.numberOfBytes();
         fixImm32(displacementStart, disp32);
     }
 
     private void zeroFillFrom(int start) {
         int i = start;
         while (i < _length) {
             _instruction[i++] = 0;
         }
     }
 
     public void fixImmediateOperand(WordWidth operandWidth, byte imm8) {
         final int numBytes = operandWidth.numberOfBytes();
         final int immediateStart = _startOffset + _length - numBytes;
         _instruction[immediateStart] = imm8;
         zeroFillFrom(immediateStart + 1);
     }
 
     public void fixImmediateOperand(WordWidth operandWidth, short imm16) {
         // The Eir to AMD64 code generation uses only two width for immediate operands: 8 bits, or 32 bits.
         final WordWidth effectiveOperandWidth =  (operandWidth == WordWidth.BITS_8) ? WordWidth.BITS_8 : WordWidth.BITS_32;
         // index to the first byte of the immediate value
         final int immediateStart = _startOffset + _length - effectiveOperandWidth.numberOfBytes();
         fixImm16(immediateStart, imm16);
         zeroFillFrom(immediateStart + operandWidth.numberOfBytes());
     }
 
     /**
      * Replace the operand of the assembly instruction with a new value. The width of the immediate operand the
      *  instruction was originally generated with is specified in parameter.
      * @param operandWidth width of the operand of the assembly instruction.
      * @param imm32 the new value of the instruction's operand.
      */
     public void fixImmediateOperand(WordWidth operandWidth, int imm32) {
         // The Eir to AMD64 code generation uses only two width for immediate operands: 8 bits, or 32 bits.
         if (operandWidth == WordWidth.BITS_8) {
             // index to the first byte of the immediate value
             final int immediateStart = _startOffset + _length - WordWidth.BITS_8.numberOfBytes();
             fixImm8(immediateStart, imm32);
         } else {
             // index to the first byte of the immediate value
             final int immediateStart = _startOffset + _length - WordWidth.BITS_32.numberOfBytes();
             fixImm32(immediateStart, imm32);
         }
     }
 
     /**
      * Replace the operand of the assembly instruction with a new value. The width of the immediate operand the
      *  instruction was originally generated with must be 32 bits.
      * @param imm32 the new value of the instruction's operand.
      */
     public void fixImmediateOperand(int imm32) {
         // index to the first byte of the immediate value
         final int immediateStart = _startOffset + _length - WordWidth.BITS_32.numberOfBytes();
         fixImm32(immediateStart, imm32);
     }
 
     public void fixImmediateOperand(long imm64) {
         // index to the first byte of the immediate value
         final int immediateStart = _startOffset + _length - WordWidth.BITS_64.numberOfBytes();
         fixImm64(immediateStart, imm64);
     }
 }
