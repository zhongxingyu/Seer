 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import lombok.val;
 
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.Opcode;
 import org.jf.dexlib.Code.Format.Instruction12x;
 import org.jf.dexlib.Code.Format.Instruction23x;
 import org.junit.Test;
 
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.Utils;
 
 public class DexInstruction_BinaryOp_Test {
 
   @Test
   public void testParse_BinaryOp() throws InstructionParsingException {
     Utils.parseAndCompare(
       new Instruction[] {
         new Instruction23x(Opcode.ADD_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.SUB_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.MUL_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.DIV_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.REM_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.AND_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.OR_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.XOR_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.SHL_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.SHR_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.USHR_INT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.ADD_FLOAT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.SUB_FLOAT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.MUL_FLOAT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.DIV_FLOAT, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.REM_FLOAT, (short) 234, (short) 235, (short) 236)
       }, new String[] {
         "add-int v234, v235, v236",
         "sub-int v234, v235, v236",
         "mul-int v234, v235, v236",
         "div-int v234, v235, v236",
         "rem-int v234, v235, v236",
         "and-int v234, v235, v236",
         "or-int v234, v235, v236",
         "xor-int v234, v235, v236",
         "shl-int v234, v235, v236",
         "shr-int v234, v235, v236",
         "ushr-int v234, v235, v236",
         "add-float v234, v235, v236",
         "sub-float v234, v235, v236",
         "mul-float v234, v235, v236",
         "div-float v234, v235, v236",
         "rem-float v234, v235, v236"
       });
   }
 
   @Test
   public void testParse_BinaryOp2addr() throws InstructionParsingException {
     Utils.parseAndCompare(
       new Instruction[] {
         new Instruction12x(Opcode.ADD_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.SUB_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.MUL_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.DIV_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.REM_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.AND_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.OR_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.XOR_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.SHL_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.SHR_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.USHR_INT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.ADD_FLOAT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.SUB_FLOAT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.MUL_FLOAT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.DIV_FLOAT_2ADDR, (byte) 2, (byte) 10),
         new Instruction12x(Opcode.REM_FLOAT_2ADDR, (byte) 2, (byte) 10),
       }, new String[] {
         "add-int v2, v2, v10",
         "sub-int v2, v2, v10",
         "mul-int v2, v2, v10",
         "div-int v2, v2, v10",
         "rem-int v2, v2, v10",
         "and-int v2, v2, v10",
         "or-int v2, v2, v10",
         "xor-int v2, v2, v10",
         "shl-int v2, v2, v10",
         "shr-int v2, v2, v10",
         "ushr-int v2, v2, v10",
         "add-float v2, v2, v10",
         "sub-float v2, v2, v10",
         "mul-float v2, v2, v10",
         "div-float v2, v2, v10",
         "rem-float v2, v2, v10",
       });
   }
 
   @Test
   public void testAssemble_Binop() {
     val regNTarget = Utils.numFitsInto_Unsigned(8);
     val regNSourceA = Utils.numFitsInto_Unsigned(8) - 1;
     val regNSourceB = Utils.numFitsInto_Unsigned(8) - 2;
 
     val regTarget = new DexRegister(regNTarget);
     val regSourceA = new DexRegister(regNSourceA);
     val regSourceB = new DexRegister(regNSourceB);
 
     val regAlloc = Utils.genRegAlloc(regTarget, regSourceA, regSourceB);
 
     val insn = new DexInstruction_BinaryOp(null, regTarget, regSourceA, regSourceB, Opcode_BinaryOp.OrInt);
 
     val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
     assertEquals(1, asm.length);
     assertTrue(asm[0] instanceof Instruction23x);
 
     val asmInsn = (Instruction23x) asm[0];
     assertEquals(regNTarget, asmInsn.getRegisterA());
     assertEquals(regNSourceA, asmInsn.getRegisterB());
     assertEquals(regNSourceB, asmInsn.getRegisterC());
     assertEquals(Opcode.OR_INT, asmInsn.opcode);
   }
 
   @Test
   public void testAssemble_Binop2addr() {
     val regNTarget = Utils.numFitsInto_Unsigned(4);
     val regNSourceB = Utils.numFitsInto_Unsigned(4) - 2;
 
     val regTarget = new DexRegister(regNTarget);
     val regSourceB = new DexRegister(regNSourceB);
 
     val regAlloc = Utils.genRegAlloc(regTarget, regSourceB);
 
     val insn = new DexInstruction_BinaryOp(null, regTarget, regTarget, regSourceB, Opcode_BinaryOp.OrInt);
 
     val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
     assertEquals(1, asm.length);
     assertTrue(asm[0] instanceof Instruction12x);
 
     val asmInsn = (Instruction12x) asm[0];
     assertEquals(regNTarget, asmInsn.getRegisterA());
     assertEquals(regNSourceB, asmInsn.getRegisterB());
     assertEquals(Opcode.OR_INT_2ADDR, asmInsn.opcode);
   }
 
   @Test
   public void testAssemble_Binop2addr_TargetRegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(5);
     val regNSourceB = Utils.numFitsInto_Unsigned(4) - 2;
 
     val regTarget = new DexRegister(regNTarget);
     val regSourceB = new DexRegister(regNSourceB);
 
     val regAlloc = Utils.genRegAlloc(regTarget, regSourceB);
 
     val insn = new DexInstruction_BinaryOp(null, regTarget, regTarget, regSourceB, Opcode_BinaryOp.OrInt);
 
     val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
     assertEquals(1, asm.length);
     assertTrue(asm[0] instanceof Instruction23x);
 
     val asmInsn = (Instruction23x) asm[0];
     assertEquals(regNTarget, asmInsn.getRegisterA());
     assertEquals(regNTarget, asmInsn.getRegisterB());
     assertEquals(regNSourceB, asmInsn.getRegisterC());
     assertEquals(Opcode.OR_INT, asmInsn.opcode);
   }
 
   @Test
   public void testAssemble_Binop2addr_SourceBRegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(4);
     val regNSourceB = Utils.numFitsInto_Unsigned(5);
 
     val regTarget = new DexRegister(regNTarget);
     val regSourceB = new DexRegister(regNSourceB);
 
     val regAlloc = Utils.genRegAlloc(regTarget, regSourceB);
 
     val insn = new DexInstruction_BinaryOp(null, regTarget, regTarget, regSourceB, Opcode_BinaryOp.OrInt);
 
     val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
     assertEquals(1, asm.length);
     assertTrue(asm[0] instanceof Instruction23x);
 
     val asmInsn = (Instruction23x) asm[0];
     assertEquals(regNTarget, asmInsn.getRegisterA());
     assertEquals(regNTarget, asmInsn.getRegisterB());
     assertEquals(regNSourceB, asmInsn.getRegisterC());
     assertEquals(Opcode.OR_INT, asmInsn.opcode);
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_Binop_TargetRegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(9);
     val regNSourceA = Utils.numFitsInto_Unsigned(8) - 1;
     val regNSourceB = Utils.numFitsInto_Unsigned(8) - 2;
 
     val regTarget = new DexRegister(regNTarget);
     val regSourceA = new DexRegister(regNSourceA);
     val regSourceB = new DexRegister(regNSourceB);
 
     val regAlloc = Utils.genRegAlloc(regTarget, regSourceA, regSourceB);
 
     val insn = new DexInstruction_BinaryOp(null, regTarget, regSourceA, regSourceB, Opcode_BinaryOp.OrInt);
 
     insn.assembleBytecode(Utils.genAsmState(regAlloc));
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_Binop_SourceARegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(8);
     val regNSourceA = Utils.numFitsInto_Unsigned(9);
     val regNSourceB = Utils.numFitsInto_Unsigned(8) - 2;
 
     val regTarget = new DexRegister(regNTarget);
     val regSourceA = new DexRegister(regNSourceA);
     val regSourceB = new DexRegister(regNSourceB);
 
     val regAlloc = Utils.genRegAlloc(regTarget, regSourceA, regSourceB);
 
     val insn = new DexInstruction_BinaryOp(null, regTarget, regSourceA, regSourceB, Opcode_BinaryOp.OrInt);
 
     insn.assembleBytecode(Utils.genAsmState(regAlloc));
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_Binop_SourceBRegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(8);
     val regNSourceA = Utils.numFitsInto_Unsigned(8) - 1;
     val regNSourceB = Utils.numFitsInto_Unsigned(9);
 
     val regTarget = new DexRegister(regNTarget);
     val regSourceA = new DexRegister(regNSourceA);
     val regSourceB = new DexRegister(regNSourceB);
 
     val regAlloc = Utils.genRegAlloc(regTarget, regSourceA, regSourceB);
 
     val insn = new DexInstruction_BinaryOp(null, regTarget, regSourceA, regSourceB, Opcode_BinaryOp.OrInt);
 
     insn.assembleBytecode(Utils.genAsmState(regAlloc));
   }
 
   @Test
   public void testInstrument() {
     val reg1 = new DexRegister(0);
     val reg2 = new DexRegister(1);
     val reg3 = new DexRegister(2);
     val code = new DexCode();
    code.add(new DexInstruction_BinaryOp(code, reg1, reg2, reg3, Opcode_BinaryOp.XorInt));
 
     Utils.instrumentAndCompare(
       code,
       new String[] {
         "xor-int v0, v1, v2",
         "or-int v3, v4, v5"
       });
   }
 }
