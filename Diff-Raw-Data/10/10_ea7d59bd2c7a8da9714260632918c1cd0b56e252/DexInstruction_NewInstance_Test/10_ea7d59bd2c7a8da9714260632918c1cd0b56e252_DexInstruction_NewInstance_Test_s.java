 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import lombok.val;
 
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.TypeIdItem;
 import org.jf.dexlib.Code.Opcode;
 import org.jf.dexlib.Code.Format.Instruction21c;
 import org.junit.Test;
 
 import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
 import uk.ac.cam.db538.dexter.dex.DexParsingCache;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.Utils;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 
 public class DexInstruction_NewInstance_Test {
 
   @Test
   public void testParse_NewInstance() throws InstructionParsingException {
     Utils.parseAndCompare(
      new Instruction21c(Opcode.NEW_INSTANCE, (short) 236, Utils.getTypeItem("Ljava.lang.String;")),
      "new-instance v236, Ljava.lang.String;");
   }
 
   @Test
   public void testAssemble_NewInstance() {
     val cache = new DexParsingCache();
 
     val regNTo = Utils.numFitsInto_Unsigned(8);
     val regTo = new DexRegister(regNTo);
     val regAlloc = Utils.genRegAlloc(regTo);
 
    val insn = new DexInstruction_NewInstance(null, regTo, DexClassType.parse("Ljava.lang.Object;", cache));
 
     val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
     assertEquals(1, asm.length);
     assertTrue(asm[0] instanceof Instruction21c);
 
     val asmInsn = (Instruction21c) asm[0];
     assertEquals(regNTo, asmInsn.getRegisterA());
    assertEquals("Ljava.lang.Object;", ((TypeIdItem) asmInsn.getReferencedItem()).getTypeDescriptor());
   }
 
   @Test(expected=InstructionAssemblyException.class)
   public void testAssemble_NewInstance_WrongAllocation() {
     val cache = new DexParsingCache();
 
     val regNTo = Utils.numFitsInto_Unsigned(9);
     val regTo = new DexRegister(regNTo);
     val regAlloc = Utils.genRegAlloc(regTo);
 
     val insn = new DexInstruction_NewInstance(null, regTo, DexClassType.parse("Ljava.lang.Object;", cache));
 
     insn.assembleBytecode(regAlloc, null);
   }
 }
