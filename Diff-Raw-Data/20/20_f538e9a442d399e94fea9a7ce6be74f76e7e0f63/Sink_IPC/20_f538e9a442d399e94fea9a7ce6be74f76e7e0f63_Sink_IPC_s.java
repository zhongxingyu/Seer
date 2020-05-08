 package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;
 
 import java.util.List;
 
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintInteger;
 import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintStringConst;
 import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public class Sink_IPC extends FallbackInstrumentor {
 
   private boolean hasIntentParam(DexPrototype prototype) {
     for (val paramType : prototype.getParameterTypes())
       if (paramType.getDescriptor().equals("Landroid/content/Intent;") || paramType.getDescriptor().equals("[Landroid/content/Intent;"))
         return true;
     return false;
   }
 
   @Override
   public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
     val classHierarchy = insn.getParentFile().getClassHierarchy();
     val parsingCache = insn.getParentFile().getParsingCache();
 
     val insnInvoke = insn.getInstructionInvoke();
 
    if (insnInvoke.getCallType() != Opcode_Invoke.Virtual)
      return false;

    if (!classHierarchy.isAncestor(insnInvoke.getClassType(), DexClassType.parse("Landroid/content/Context;", parsingCache)))
      return false;

    if (!hasIntentParam(insnInvoke.getMethodPrototype()))
      return false;

    return true;
   }
 
   @Override
   public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
     val fallback = super.generateInstrumentation(insn, state);
     val regCombinedTaint = this.regCombinedTaint;
 
     val methodCode = insn.getMethodCode();
     val preCode = new NoDuplicatesList<DexCodeElement>(fallback.getValA().size() + 20);
 
     val methodName = insn.getInstructionInvoke().getMethodName();
 
     // check the combined taint
     val labelAfter = new DexLabel(methodCode);
     preCode.addAll(fallback.getValA());
     preCode.add(new DexInstruction_IfTestZero(methodCode, regCombinedTaint, labelAfter, Opcode_IfTestZero.eqz));
     preCode.add(new DexPseudoinstruction_PrintStringConst(methodCode, "$! tainted data passed to Context." + methodName + " => T=", false));
     preCode.add(new DexPseudoinstruction_PrintInteger(methodCode, regCombinedTaint, true));
     preCode.add(labelAfter);
 
     return new Pair<List<DexCodeElement>, List<DexCodeElement>>(preCode, fallback.getValB());
   }
 
 }
