 package uk.ac.cam.db538.dexter.transform.taint;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.ac.cam.db538.dexter.ProgressCallback;
 import uk.ac.cam.db538.dexter.dex.DexClass;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.InstructionList;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexTaintRegister;
 import uk.ac.cam.db538.dexter.dex.method.DexMethod;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy.TypeClassification;
 
 public class TestingTaintTransform extends TaintTransform {
 
     public TestingTaintTransform() {
     }
 
     public TestingTaintTransform(ProgressCallback progressCallback) {
         super(progressCallback);
     }
 
     @Override
     public boolean exclude(DexClass clazz) {
     	return false;
 //        String name = clazz.getClassDef().getType().getDescriptor();
 //        return name.equals("LTestList;");
     }
 
     @Override
     public DexMethod doLast(DexMethod method) {
         if (isGivenUtilsMethod(method, NAME_ISTAINTED, PROTOTYPE_ISTAINTED_PRIMITIVE)) {
         	
             DexCode oldCode = method.getMethodBody();
             DexRegister paramReg = oldCode.getParameters().get(0).getRegister();
 
             List<DexCodeElement> newInstructions = new ArrayList<DexCodeElement>();
             for (DexCodeElement insn : oldCode.getInstructionList())
                 if (insn instanceof DexInstruction_Return)
                     newInstructions.add(new DexInstruction_Return(paramReg.getTaintRegister(), false, oldCode.getHierarchy()));
                 else
                     newInstructions.add(insn);
 
             DexCode newCode = new DexCode(oldCode, new InstructionList(newInstructions));
             method = new DexMethod(method, newCode);
             
         } else if (isGivenUtilsMethod(method, NAME_ISTAINTED, PROTOTYPE_ISTAINTED_REFERENCE)) {
         	
             DexCode oldCode = method.getMethodBody();
             DexSingleRegister paramReg = (DexSingleRegister) oldCode.getParameters().get(0).getRegister();
             DexTaintRegister paramRegTaint = paramReg.getTaintRegister();
 
             List<DexCodeElement> newInstructions = new ArrayList<DexCodeElement>();
             for (DexCodeElement insn : oldCode.getInstructionList())
                 if (insn instanceof DexInstruction_Return) {
                 	newInstructions.add(codeGen.taintClearVisited());
                 	newInstructions.add(codeGen.getTaint(paramRegTaint, paramReg));
                     newInstructions.add(new DexInstruction_Return(paramRegTaint, false, oldCode.getHierarchy()));
                 } else
                     newInstructions.add(insn);
 
             DexCode newCode = new DexCode(oldCode, new InstructionList(newInstructions));
             method = new DexMethod(method, newCode);
 
         } else if (isGivenUtilsMethod(method, NAME_TAINT, PROTOTYPE_TAINT_PRIMITIVE)) {
 
             DexCode oldCode = method.getMethodBody();
             DexRegister paramReg = oldCode.getParameters().get(0).getRegister();
             DexTaintRegister paramRegTaint = paramReg.getTaintRegister();
             
             List<DexCodeElement> newInstructions = new ArrayList<DexCodeElement>();
             for (DexCodeElement insn : oldCode.getInstructionList())
                 if (insn instanceof DexInstruction_ArrayGet && ((DexInstruction_ArrayGet) insn).getRegTo().equals(paramRegTaint))
                     newInstructions.add(new DexInstruction_Const(paramRegTaint, 1, oldCode.getHierarchy()));
                 else
                     newInstructions.add(insn);
 
             DexCode newCode = new DexCode(oldCode, new InstructionList(newInstructions));
             method = new DexMethod(method, newCode);
             
         } else if (isGivenUtilsMethod(method, NAME_TAINT, PROTOTYPE_TAINT_REFERENCE)) {
 
         	DexCode oldCode = method.getMethodBody();
             DexSingleRegister paramReg = (DexSingleRegister) oldCode.getParameters().get(0).getRegister();
             DexTaintRegister paramRegTaint = paramReg.getTaintRegister();
             DexLabel lNull = codeGen.label(), lAfter = codeGen.label();
             
             List<DexCodeElement> newInstructions = new ArrayList<DexCodeElement>();
             for (DexCodeElement insn : oldCode.getInstructionList())
                if (insn instanceof DexInstruction_Const && ((DexInstruction_Const) insn).getValue() == 1234L) { 
                 	DexSingleRegister regTaint = codeGen.auxReg();
                 	newInstructions.add(codeGen.constant(regTaint, 1));
                 	newInstructions.add(codeGen.ifZero(paramReg, lNull));
                 	newInstructions.add(codeGen.taintClearVisited());
                     newInstructions.add(codeGen.setTaint(regTaint, paramRegTaint));
                     newInstructions.add(codeGen.jump(lAfter));
                     newInstructions.add(lNull);
                     newInstructions.add(codeGen.taintNull(paramRegTaint, paramReg, regTaint, TypeClassification.REF_EXTERNAL));                    
                     newInstructions.add(lAfter);
                 } else
                     newInstructions.add(insn);
 
             DexCode newCode = new DexCode(oldCode, new InstructionList(newInstructions));
             method = new DexMethod(method, newCode);
         	
         }
         
         if (method.getMethodBody() != null && method.getMethodDef().getMethodId().getName().equals("generate"))
         	method.getMethodBody().getInstructionList().dump();
         
         return super.doLast(method);
     }
 
     private static final String TAINTUTILS_CLASS = "LTaintUtils;";
     private static final String NAME_ISTAINTED = "isTainted";
     private static final String PROTOTYPE_ISTAINTED_PRIMITIVE = "(I)Z";
     private static final String PROTOTYPE_ISTAINTED_REFERENCE = "(Ljava/lang/Object;)Z";
     private static final String NAME_TAINT = "taint";
     private static final String PROTOTYPE_TAINT_PRIMITIVE = "(I)I";
     private static final String PROTOTYPE_TAINT_REFERENCE = "(Ljava/lang/Object;)Ljava/lang/Object;";
 
     private boolean isGivenUtilsMethod(DexMethod method, String methodName, String methodPrototype) {
         return
             method.getParentClass().getClassDef().getType().getDescriptor().equals(TAINTUTILS_CLASS) &&
             method.getMethodDef().getMethodId().getName().equals(methodName) &&
             method.getMethodDef().getMethodId().getPrototype().getDescriptor().equals(methodPrototype) &&
             method.getMethodDef().isStatic() &&
             method.getMethodBody() != null;
     }
 }
