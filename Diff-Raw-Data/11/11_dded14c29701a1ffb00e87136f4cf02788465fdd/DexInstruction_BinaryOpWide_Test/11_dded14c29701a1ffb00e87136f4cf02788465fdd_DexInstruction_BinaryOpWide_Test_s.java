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
 
 public class DexInstruction_BinaryOpWide_Test {
 
   @Test
   public void testParse_BinaryOpWide() throws InstructionParsingException {
     Utils.parseAndCompare(
       new Instruction[] {
         new Instruction23x(Opcode.ADD_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.SUB_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.MUL_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.DIV_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.REM_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.AND_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.OR_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.XOR_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.SHL_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.SHR_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.USHR_LONG, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.ADD_DOUBLE, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.SUB_DOUBLE, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.MUL_DOUBLE, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.DIV_DOUBLE, (short) 234, (short) 235, (short) 236),
         new Instruction23x(Opcode.REM_DOUBLE, (short) 234, (short) 235, (short) 236)
       }, new String[] {
         "add-long v234, v235, v236",
         "sub-long v234, v235, v236",
         "mul-long v234, v235, v236",
         "div-long v234, v235, v236",
         "rem-long v234, v235, v236",
         "and-long v234, v235, v236",
         "or-long v234, v235, v236",
         "xor-long v234, v235, v236",
         "shl-long v234, v235, v236",
         "shr-long v234, v235, v236",
         "ushr-long v234, v235, v236",
         "add-double v234, v235, v236",
         "sub-double v234, v235, v236",
         "mul-double v234, v235, v236",
         "div-double v234, v235, v236",
         "rem-double v234, v235, v236"
       });
 
     val insn = (DexInstruction_BinaryOpWide)
                Utils.parseAndCompare(new Instruction23x(Opcode.ADD_LONG, (short) 2, (short) 12, (short) 112),
                                      "add-long v2, v12, v112");
     assertEquals(2, insn.getRegTarget1().getOriginalIndex());
     assertEquals(3, insn.getRegTarget2().getOriginalIndex());
     assertEquals(12, insn.getRegSourceA1().getOriginalIndex());
     assertEquals(13, insn.getRegSourceA2().getOriginalIndex());
     assertEquals(112, insn.getRegSourceB1().getOriginalIndex());
     assertEquals(113, insn.getRegSourceB2().getOriginalIndex());
   }
 
   @Test
   public void testParse_BinaryOpWide2addr() throws InstructionParsingException {
     Utils.parseAndCompare(
       new Instruction[] {
         new Instruction12x(Opcode.ADD_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.SUB_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.MUL_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.DIV_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.REM_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.AND_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.OR_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.XOR_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.SHL_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.SHR_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.USHR_LONG_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.ADD_DOUBLE_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.SUB_DOUBLE_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.MUL_DOUBLE_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.DIV_DOUBLE_2ADDR, (byte) 4, (byte) 14),
         new Instruction12x(Opcode.REM_DOUBLE_2ADDR, (byte) 4, (byte) 14)
       }, new String[] {
         "add-long v4, v4, v14",
         "sub-long v4, v4, v14",
         "mul-long v4, v4, v14",
         "div-long v4, v4, v14",
         "rem-long v4, v4, v14",
         "and-long v4, v4, v14",
         "or-long v4, v4, v14",
         "xor-long v4, v4, v14",
         "shl-long v4, v4, v14",
         "shr-long v4, v4, v14",
         "ushr-long v4, v4, v14",
         "add-double v4, v4, v14",
         "sub-double v4, v4, v14",
         "mul-double v4, v4, v14",
         "div-double v4, v4, v14",
         "rem-double v4, v4, v14"
       });
 
     val insn = (DexInstruction_BinaryOpWide)
                Utils.parseAndCompare(new Instruction12x(Opcode.ADD_LONG_2ADDR, (byte) 3, (byte) 6),
                                      "add-long v3, v3, v6");
     assertEquals(3, insn.getRegTarget1().getOriginalIndex());
     assertEquals(4, insn.getRegTarget2().getOriginalIndex());
     assertEquals(3, insn.getRegSourceA1().getOriginalIndex());
     assertEquals(4, insn.getRegSourceA2().getOriginalIndex());
     assertEquals(6, insn.getRegSourceB1().getOriginalIndex());
     assertEquals(7, insn.getRegSourceB2().getOriginalIndex());
   }
 
   @Test
   public void testAssemble_BinopWide() {
     val regNTarget = Utils.numFitsInto_Unsigned(8);
     val regNSourceA = Utils.numFitsInto_Unsigned(8) - 2;
     val regNSourceB = Utils.numFitsInto_Unsigned(8) - 4;
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 1);
     val regSourceA1 = new DexRegister(regNSourceA);
     val regSourceA2 = new DexRegister(regNSourceA + 1);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 1);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2,
                                      regSourceA1, regSourceA2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2,
         regSourceA1, regSourceA2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
     assertEquals(1, asm.length);
     assertTrue(asm[0] instanceof Instruction23x);
 
     val asmInsn = (Instruction23x) asm[0];
     assertEquals(regNTarget, asmInsn.getRegisterA());
     assertEquals(regNSourceA, asmInsn.getRegisterB());
     assertEquals(regNSourceB, asmInsn.getRegisterC());
     assertEquals(Opcode.OR_LONG, asmInsn.opcode);
   }
 
   @Test
   public void testAssemble_Binop2addrWide() {
     val regNTarget = Utils.numFitsInto_Unsigned(4);
     val regNSourceB = Utils.numFitsInto_Unsigned(4) - 2;
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 1);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 1);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2, regTarget1, regTarget2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
     assertEquals(1, asm.length);
     assertTrue(asm[0] instanceof Instruction12x);
 
     val asmInsn = (Instruction12x) asm[0];
     assertEquals(regNTarget, asmInsn.getRegisterA());
     assertEquals(regNSourceB, asmInsn.getRegisterB());
     assertEquals(Opcode.OR_LONG_2ADDR, asmInsn.opcode);
   }
 
   @Test
   public void testAssemble_Binop2addrWide_TargetRegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(5);
     val regNSourceB = Utils.numFitsInto_Unsigned(4) - 2;
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 1);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 1);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2, regTarget1, regTarget2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
     assertEquals(1, asm.length);
     assertTrue(asm[0] instanceof Instruction23x);
 
     val asmInsn = (Instruction23x) asm[0];
     assertEquals(regNTarget, asmInsn.getRegisterA());
     assertEquals(regNTarget, asmInsn.getRegisterB());
     assertEquals(regNSourceB, asmInsn.getRegisterC());
     assertEquals(Opcode.OR_LONG, asmInsn.opcode);
   }
 
   @Test
   public void testAssemble_Binop2addrWide_SourceBRegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(4);
     val regNSourceB = Utils.numFitsInto_Unsigned(5);
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 1);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 1);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2, regTarget1, regTarget2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
     assertEquals(1, asm.length);
     assertTrue(asm[0] instanceof Instruction23x);
 
     val asmInsn = (Instruction23x) asm[0];
     assertEquals(regNTarget, asmInsn.getRegisterA());
     assertEquals(regNTarget, asmInsn.getRegisterB());
     assertEquals(regNSourceB, asmInsn.getRegisterC());
     assertEquals(Opcode.OR_LONG, asmInsn.opcode);
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_BinopWide_TargetRegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(9);
     val regNSourceA = Utils.numFitsInto_Unsigned(8) - 2;
     val regNSourceB = Utils.numFitsInto_Unsigned(8) - 4;
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 1);
     val regSourceA1 = new DexRegister(regNSourceA);
     val regSourceA2 = new DexRegister(regNSourceA + 1);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 1);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2,
                                      regSourceA1, regSourceA2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2,
         regSourceA1, regSourceA2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     insn.assembleBytecode(Utils.genAsmState(regAlloc));
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_BinopWide_SourceARegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(8);
     val regNSourceA = Utils.numFitsInto_Unsigned(9);
     val regNSourceB = Utils.numFitsInto_Unsigned(8) - 4;
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 1);
     val regSourceA1 = new DexRegister(regNSourceA);
     val regSourceA2 = new DexRegister(regNSourceA + 1);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 1);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2,
                                      regSourceA1, regSourceA2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2,
         regSourceA1, regSourceA2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     insn.assembleBytecode(Utils.genAsmState(regAlloc));
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_BinopWide_SourceBRegTooBig() {
     val regNTarget = Utils.numFitsInto_Unsigned(8);
     val regNSourceA = Utils.numFitsInto_Unsigned(8) - 2;
     val regNSourceB = Utils.numFitsInto_Unsigned(9);
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 1);
     val regSourceA1 = new DexRegister(regNSourceA);
     val regSourceA2 = new DexRegister(regNSourceA + 1);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 1);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2,
                                      regSourceA1, regSourceA2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2,
         regSourceA1, regSourceA2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     insn.assembleBytecode(Utils.genAsmState(regAlloc));
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_BinopWide_TargetRegNotWide() {
     val regNTarget = Utils.numFitsInto_Unsigned(8);
     val regNSourceA = Utils.numFitsInto_Unsigned(8) - 2;
     val regNSourceB = Utils.numFitsInto_Unsigned(8) - 4;
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 2);
     val regSourceA1 = new DexRegister(regNSourceA);
     val regSourceA2 = new DexRegister(regNSourceA + 1);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 1);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2,
                                      regSourceA1, regSourceA2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2,
         regSourceA1, regSourceA2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     insn.assembleBytecode(Utils.genAsmState(regAlloc));
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_BinopWide_SourceARegNotWide() {
     val regNTarget = Utils.numFitsInto_Unsigned(8);
     val regNSourceA = Utils.numFitsInto_Unsigned(8) - 2;
     val regNSourceB = Utils.numFitsInto_Unsigned(8) - 4;
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 1);
     val regSourceA1 = new DexRegister(regNSourceA);
     val regSourceA2 = new DexRegister(regNSourceA + 2);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 1);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2,
                                      regSourceA1, regSourceA2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2,
         regSourceA1, regSourceA2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     insn.assembleBytecode(Utils.genAsmState(regAlloc));
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_BinopWide_SourceBRegNotWide() {
     val regNTarget = Utils.numFitsInto_Unsigned(8);
     val regNSourceA = Utils.numFitsInto_Unsigned(8) - 2;
     val regNSourceB = Utils.numFitsInto_Unsigned(8) - 4;
 
     val regTarget1 = new DexRegister(regNTarget);
     val regTarget2 = new DexRegister(regNTarget + 1);
     val regSourceA1 = new DexRegister(regNSourceA);
     val regSourceA2 = new DexRegister(regNSourceA + 1);
     val regSourceB1 = new DexRegister(regNSourceB);
     val regSourceB2 = new DexRegister(regNSourceB + 2);
 
     val regAlloc = Utils.genRegAlloc(regTarget1, regTarget2,
                                      regSourceA1, regSourceA2, regSourceB1, regSourceB2);
 
     val insn = new DexInstruction_BinaryOpWide(null, regTarget1, regTarget2,
         regSourceA1, regSourceA2, regSourceB1, regSourceB2, Opcode_BinaryOpWide.OrLong);
 
     insn.assembleBytecode(Utils.genAsmState(regAlloc));
   }
 
   @Test
   public void testInstrument() {
     val reg0 = new DexRegister(0);
     val reg1 = new DexRegister(1);
     val reg2 = new DexRegister(2);
     val reg3 = new DexRegister(3);
     val reg4 = new DexRegister(4);
     val reg5 = new DexRegister(5);
     val code = new DexCode();
    code.add(new DexInstruction_BinaryOpWide(null, reg0, reg1, reg2, reg3, reg4, reg5, Opcode_BinaryOpWide.SubLong));
 
     Utils.instrumentAndCompare(
       code,
       new String[] {
         "sub-long v0, v2, v4",
         "or-int v6, v8, v9",
         "or-int v6, v6, v10",
         "or-int v6, v6, v11",
         "move v7, v6"
       });
   }
 }
