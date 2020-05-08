 package uk.ac.cam.db538.dexter.transform.taint;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.ac.cam.db538.dexter.ProgressCallback;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.InstructionList;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
 import uk.ac.cam.db538.dexter.dex.code.macro.DexMacro;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
 import uk.ac.cam.db538.dexter.dex.method.DexMethod;
 
 public class UnitTestTransform extends DexterTransform {
 
 	public UnitTestTransform() {
 	}
 
 	public UnitTestTransform(ProgressCallback progressCallback) {
 		super(progressCallback);
 	}
 
 	@Override
 	public DexMethod doLast(DexMethod method) {
 		if (isTaintCheckMethod(method)) {
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
 		}
 		
 		return super.doFirst(method);
 	}
 	
 	@Override
 	public DexCodeElement doFirst(DexCodeElement element, DexCode code) {
 		if (element instanceof DexInstruction_Const) {
 			DexInstruction_Const insnConst = (DexInstruction_Const) element;
 			if (insnConst.getValue() == 0xDEC0DEDL)
 				return new DexMacro(
 					new DexInstruction_Const(insnConst.getRegTo().getTaintRegister(), 1L, code.getHierarchy()),
 					insnConst);
 		}
 		
 		return super.doFirst(element, code);
 	}
 
 	private static final String TAINTCHECK_CLASS = "Luk/ac/cam/db538/dexter/tests/TaintChecker;";
 	private static final String TAINTCHECK_METHOD = "isTainted";
 	private static final String TAINTCHECK_PROTOTYPE = "(I)Z";
 
 	private boolean isTaintCheckMethod(DexMethod method) {
 		return
 			method.getParentClass().getClassDef().getType().getDescriptor().equals(TAINTCHECK_CLASS) &&
 			method.getMethodDef().getMethodId().getName().equals(TAINTCHECK_METHOD) &&
 			method.getMethodDef().getMethodId().getPrototype().getDescriptor().equals(TAINTCHECK_PROTOTYPE) &&
 			method.getMethodDef().isStatic() &&
 			method.getMethodBody() != null;
 	}
 }
