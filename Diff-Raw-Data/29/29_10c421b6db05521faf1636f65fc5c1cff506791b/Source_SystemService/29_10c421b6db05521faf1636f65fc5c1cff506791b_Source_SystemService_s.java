 package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;
 
 import java.util.List;
 
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetServiceTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_SetObjectTaint;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public class Source_SystemService extends FallbackInstrumentor {
 
   @Override
   public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
     val classHierarchy = insn.getParentFile().getClassHierarchy();
     val parsingCache = insn.getParentFile().getParsingCache();
 
     val insnInvoke = insn.getInstructionInvoke();
 
     if (insnInvoke.getCallType() != Opcode_Invoke.Virtual)
       return false;
 
     if (!insnInvoke.getMethodName().equals("getSystemService"))
       return false;
 
     if (!insn.movesResult()) // only care about assigning taint to the result
       return false;
 
     if (!classHierarchy.isAncestor(insnInvoke.getClassType(),
                                    DexClassType.parse("Landroid/content/Context;", parsingCache)))
       return false;
 
     val methodParamTypes = insnInvoke.getMethodPrototype().getParameterTypes();
     if (methodParamTypes.size() != 1 ||
         !methodParamTypes.get(0).getDescriptor().equals("Ljava/lang/String;"))
       return false;
 
     return true;
   }
 
   @Override
   public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
     val fallback = super.generateInstrumentation(insn, state);
 
     val insnInvoke = insn.getInstructionInvoke();
     val insnMoveResult = insn.getInstructionMoveResult();
 
     val methodCode = insn.getMethodCode();
     val postCode = new NoDuplicatesList<DexCodeElement>(fallback.getValB().size() + 20);
 
     DexRegister regResult = null;
     if (insnMoveResult instanceof DexInstruction_MoveResult)
       regResult = ((DexInstruction_MoveResult) insnMoveResult).getRegTo();
     else if (insnMoveResult instanceof DexInstruction_MoveResultWide)
       regResult = ((DexInstruction_MoveResultWide) insnMoveResult).getRegTo1();
 
     val regResultTaint = state.getTaintRegister(regResult);
 
     // add taint to the result
    postCode.addAll(fallback.getValB());
    postCode.add(new DexPseudoinstruction_GetServiceTaint(methodCode, regResultTaint, insnInvoke.getArgumentRegisters().get(1)));
     postCode.add(new DexPseudoinstruction_SetObjectTaint(methodCode, regResult, regResultTaint));
 
    return new Pair<List<DexCodeElement>, List<DexCodeElement>>(fallback.getValA(), postCode);
   }
 
 }
