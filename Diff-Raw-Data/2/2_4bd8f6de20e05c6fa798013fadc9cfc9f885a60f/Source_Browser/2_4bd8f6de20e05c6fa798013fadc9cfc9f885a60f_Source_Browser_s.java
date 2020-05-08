 package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;
 
 import java.util.List;
 
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintStringConst;
 import uk.ac.cam.db538.dexter.merge.TaintConstants;
 import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public class Source_Browser extends FallbackInstrumentor {
 
   @Override
   public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
     val insnInvoke = insn.getInstructionInvoke();
 
     if (insnInvoke.getCallType() != Opcode_Invoke.Static)
       return false;
 
     if (!insn.movesResult()) // only care about assigning taint to the result
       return false;
 
     if (!insnInvoke.getClassType().getDescriptor().equals("Landroid/provider/Browser;"))
       return false;
 
     return true;
   }
 
   @Override
   public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
     val fallback = super.generateInstrumentation(insn, state);
 
     val methodCode = insn.getMethodCode();
     val postCode = new NoDuplicatesList<DexCodeElement>(fallback.getValB().size() + 20);
 
     val regBrowserTaint = new DexRegister();
    postCode.add(new DexInstruction_BinaryOpLiteral(methodCode, regCombinedTaint, regCombinedTaint, TaintConstants.TAINT_SOURCE_BROWSER, Opcode_BinaryOpLiteral.OrInt));
     postCode.add(new DexPseudoinstruction_PrintStringConst(methodCode, "browser data => " + TaintConstants.TAINT_SOURCE_BROWSER, true));
     postCode.addAll(fallback.getValB());
 
     return new Pair<List<DexCodeElement>, List<DexCodeElement>>(fallback.getValA(), postCode);
   }
 
 }
