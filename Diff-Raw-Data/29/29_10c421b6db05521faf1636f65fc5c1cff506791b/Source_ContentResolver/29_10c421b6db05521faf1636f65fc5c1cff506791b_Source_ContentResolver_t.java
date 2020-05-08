 package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;
 
 import java.util.List;
 
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetQueryTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_SetObjectTaint;
 import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public class Source_ContentResolver extends FallbackInstrumentor {
 
   private boolean fitsAPI1(DexPrototype methodPrototype) {
     val methodParamTypes = methodPrototype.getParameterTypes();
     return methodParamTypes.size() == 5 &&
            methodParamTypes.get(0).getDescriptor().equals("Landroid/net/Uri;") && // uri
            methodParamTypes.get(1).getDescriptor().equals("[Ljava/lang/String;") && // projection
            methodParamTypes.get(2).getDescriptor().equals("Ljava/lang/String;") && // selection
            methodParamTypes.get(3).getDescriptor().equals("[Ljava/lang/String;") && // selectionArgs
            methodParamTypes.get(4).getDescriptor().equals("Ljava/lang/String;"); // sortOrder
   }
 
   private boolean fitsAPI16(DexPrototype methodPrototype) {
     val methodParamTypes = methodPrototype.getParameterTypes();
     return methodParamTypes.size() == 6 &&
            methodParamTypes.get(0).getDescriptor().equals("Landroid/net/Uri;") && // uri
            methodParamTypes.get(1).getDescriptor().equals("[Ljava/lang/String;") && // projection
            methodParamTypes.get(2).getDescriptor().equals("Ljava/lang/String;") && // selection
            methodParamTypes.get(3).getDescriptor().equals("[Ljava/lang/String;") && // selectionArgs
            methodParamTypes.get(4).getDescriptor().equals("Ljava/lang/String;") && // sortOrder
            methodParamTypes.get(5).getDescriptor().equals("Landroid/os/CancellationSignal;"); // cancellationSignal
   }
 
   @Override
   public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
     val classHierarchy = insn.getParentFile().getClassHierarchy();
     val parsingCache = insn.getParentFile().getParsingCache();
 
     val insnInvoke = insn.getInstructionInvoke();
 
     if (insnInvoke.getCallType() != Opcode_Invoke.Virtual)
       return false;
 
     if (!insnInvoke.getMethodName().equals("query"))
       return false;
 
     if (!insn.movesResult()) // only care about assigning taint to the result
       return false;
 
     if (!classHierarchy.isAncestor(insnInvoke.getClassType(),
                                    DexClassType.parse("Landroid/content/ContentResolver;", parsingCache)))
       return false;
 
     if (!fitsAPI1(insnInvoke.getMethodPrototype()) && !fitsAPI16(insnInvoke.getMethodPrototype()))
       return false;
 
     return true;
   }
 
   @Override
   public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
     val fallback = super.generateInstrumentation(insn, state);
 
     val insnInvoke = insn.getInstructionInvoke();
     val insnMoveResult = insn.getInstructionMoveResult();
 
     val methodCode = insn.getMethodCode();
    val preCode = new NoDuplicatesList<DexCodeElement>(fallback.getValA().size() + 20);
     val postCode = new NoDuplicatesList<DexCodeElement>(fallback.getValB().size() + 20);
 
     DexRegister regResult = null;
     if (insnMoveResult instanceof DexInstruction_MoveResult)
       regResult = ((DexInstruction_MoveResult) insnMoveResult).getRegTo();
     else if (insnMoveResult instanceof DexInstruction_MoveResultWide)
       regResult = ((DexInstruction_MoveResultWide) insnMoveResult).getRegTo1();
 
     val regResultTaint = state.getTaintRegister(regResult);
 
     // add taint to the result
    preCode.addAll(fallback.getValA());
    preCode.add(new DexPseudoinstruction_GetQueryTaint(methodCode, regResultTaint, insnInvoke.getArgumentRegisters().get(1)));

     postCode.add(new DexPseudoinstruction_SetObjectTaint(methodCode, regResult, regResultTaint));
    postCode.addAll(fallback.getValB());
 
    return new Pair<List<DexCodeElement>, List<DexCodeElement>>(preCode, postCode);
   }
 
 }
