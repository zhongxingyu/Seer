 package uk.ac.cam.db538.dexter.transform.taint;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import lombok.val;
 
 import org.jf.dexlib.AnnotationVisibility;
 
 import uk.ac.cam.db538.dexter.ProgressCallback;
 import uk.ac.cam.db538.dexter.dex.AuxiliaryDex;
 import uk.ac.cam.db538.dexter.dex.Dex;
 import uk.ac.cam.db538.dexter.dex.DexAnnotation;
 import uk.ac.cam.db538.dexter.dex.DexClass;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode.Parameter;
 import uk.ac.cam.db538.dexter.dex.code.InstructionList;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.macro.DexMacro;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexTaintRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.RegisterType;
 import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
 import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition.CallDestinationType;
 import uk.ac.cam.db538.dexter.transform.Transform;
 
 public class DexterTransform extends Transform {
 
 	public DexterTransform() { }
 
 	public DexterTransform(ProgressCallback progressCallback) {
 		super(progressCallback);
 	}
 
 	private AuxiliaryDex dexAux;
 	private CommonCodeGenerator codeGen;
 
 	@Override
 	public void doFirst(Dex dex) {
 		super.doFirst(dex);
 		
 		dexAux = dex.getAuxiliaryDex();
 		codeGen = new CommonCodeGenerator(dexAux);
 	}
 
 	private Map<DexInstruction_Invoke, CallDestinationType> invokeClassification;
 	
 	@Override
 	public DexCode doFirst(DexCode code) {
 		code = super.doFirst(code);
 
 		codeGen.resetAsmIds(); // purely for esthetic reasons (each method will start with a0)
 		
 		val classification = InvokeClassifier.classifyMethodCalls(code);
 		invokeClassification = classification.getValB();
 		code = classification.getValA();
 		
 		return code;
 	}
 
 	@Override
 	public DexCodeElement doFirst(DexCodeElement element, DexCode code) {
 		element = super.doFirst(element, code);
 		
 		if (element instanceof DexInstruction_Const)
 			return instrument_Const((DexInstruction_Const) element);
 		
 		if (element instanceof DexInstruction_Invoke) {
 			
 			DexCodeElement nextElement = code.getInstructionList().getNextInstruction(element);
 			if (!(nextElement instanceof DexInstruction_MoveResult))
 				nextElement = null;
 			
 			CallDestinationType type = invokeClassification.get(element);
 			if (type == CallDestinationType.Internal)
 				return instrument_Invoke_Internal((DexInstruction_Invoke) element, (DexInstruction_MoveResult) nextElement);
 			else if (type == CallDestinationType.External)
 				return instrument_Invoke_External((DexInstruction_Invoke) element, (DexInstruction_MoveResult) nextElement);
 			else
 				throw new Error("Calls should never be classified as undecidable by this point");
 		}
 		
 		if (element instanceof DexInstruction_MoveResult)
 			return DexMacro.empty(); // handled by Invoke and FillArray
 
 		if (element instanceof DexInstruction_Return)
 			return instrument_Return((DexInstruction_Return) element);
 
 		if (element instanceof DexInstruction_Move)
 			return instrument_Move((DexInstruction_Move) element);
 
 		if (element instanceof DexInstruction_BinaryOp)
 			return instrument_BinaryOp((DexInstruction_BinaryOp) element);
 
 		if (element instanceof DexInstruction_BinaryOpLiteral)
 			return instrument_BinaryOpLiteral((DexInstruction_BinaryOpLiteral) element);
 
 		return element;
 	}
 
 	@Override
 	public DexCode doLast(DexCode code) {
 
 		if (hasPrimitiveArgument(code)) {
 			
 			// insert taint register initialization
 			
 			DexSingleRegister regAnnotation = codeGen.auxReg();
 			DexSingleRegister regCallerName = codeGen.auxReg();
 			
 			DexLabel labelExternal = codeGen.label();
 			DexLabel labelEnd = codeGen.label();
 			
 			List<DexTaintRegister> primitiveTaints = filterPrimitiveTaintRegisters(code.getParameters());
 			
 			DexMacro init = new DexMacro(
 				codeGen.getMethodCaller(regCallerName),
 				codeGen.ifZero(regCallerName, labelExternal),
 					codeGen.getClassAnnotation(regAnnotation, regCallerName, dexAux.getAnno_InternalClass().getType()),
 					codeGen.ifZero(regAnnotation, labelExternal),
 						codeGen.getParamTaints(primitiveTaints),
 						codeGen.jump(labelEnd),
 				labelExternal,
 					codeGen.setAllTo(primitiveTaints, 0),
 				labelEnd);
 			
 			// insert at the beginning of the method
 			code = new DexCode(code, new InstructionList(concat(init.getInstructions(), code.getInstructionList())));
 			
 		}
 		
 		// get rid of the method call classification
 		invokeClassification = null;
 		
 		return super.doLast(code);
 	}
 	
 	@Override
 	public void doLast(DexClass clazz) {
 
 		// add InternalClassAnnotation
 		clazz.replaceAnnotations(concat(
 				clazz.getAnnotations(),
 				new DexAnnotation(dexAux.getAnno_InternalClass().getType(), AnnotationVisibility.RUNTIME)));
 		
 		super.doLast(clazz);
 	}
 
 	private DexCodeElement instrument_Const(DexInstruction_Const insn) {
 		return new DexMacro(
 				codeGen.setZero(insn.getRegTo().getTaintRegister()),
 				insn);
 	}
 
 	private DexCodeElement instrument_Invoke_Internal(DexInstruction_Invoke insnInvoke, DexInstruction_MoveResult insnMoveResult) {
 		DexPrototype prototype = insnInvoke.getMethodId().getPrototype();
 		
 		// need to store taints in the ThreadLocal ARGS array?
 		DexMacro macroSetParamTaints;
 		if (prototype.hasPrimitiveArgument())
 			macroSetParamTaints = codeGen.setParamTaints(filterPrimitiveTaintRegisters(insnInvoke));
 		else
 			macroSetParamTaints = codeGen.empty();
 		
 		// need to retrieve taint from the ThreadLocal RES field?
 		DexMacro macroGetResultTaint;
 		if (insnMoveResult != null && prototype.getReturnType() instanceof DexPrimitiveType)
 			macroGetResultTaint = codeGen.getResultTaint(insnMoveResult.getRegTo().getTaintRegister()); 
 		else
 			macroGetResultTaint = codeGen.empty();
 		
 		// generate instrumentation
 		return new DexMacro(macroSetParamTaints, generateInvoke(insnInvoke, insnMoveResult), macroGetResultTaint);
 	}
 
 	private DexCodeElement instrument_Invoke_External(DexInstruction_Invoke insnInvoke, DexInstruction_MoveResult insnMoveResult) {
 		DexPrototype prototype = insnInvoke.getMethodId().getPrototype();
 		
 		// NEED TO FINISH THIS!!! ASSIGNS ZERO TAINT TO RESULT!!!
 		
 		DexCodeElement macroGetResultTaint;
 		if (insnMoveResult != null && prototype.getReturnType() instanceof DexPrimitiveType)
 			macroGetResultTaint = codeGen.setZero(insnMoveResult.getRegTo().getTaintRegister()); 
 		else
 			macroGetResultTaint = codeGen.empty();
 		
 		return new DexMacro(generateInvoke(insnInvoke, insnMoveResult), macroGetResultTaint);
 	}
 	
 	private DexCodeElement instrument_Return(DexInstruction_Return insnReturn) {
 		if (insnReturn.getOpcode() == RegisterType.REFERENCE)
 			return insnReturn;
 		else
 			return new DexMacro(
 				codeGen.setResultTaint(insnReturn.getRegFrom().getTaintRegister()),
 				insnReturn);
 	}
 
 	private DexCodeElement instrument_Move(DexInstruction_Move insn) {
		if (insn.getType() == RegisterType.REFERENCE)
 			return insn;
 		else
 			return new DexMacro(
 				codeGen.combineTaint(insn.getRegTo(), insn.getRegFrom()),
 				insn);
 	}
 
 	private DexCodeElement instrument_BinaryOp(DexInstruction_BinaryOp insn) {
 		return new DexMacro(
 				codeGen.combineTaint(insn.getRegTo(), insn.getRegArgA(), insn.getRegArgB()),
 				insn);
 	}
 	
 	private DexCodeElement instrument_BinaryOpLiteral(DexInstruction_BinaryOpLiteral insn) {
 		return new DexMacro(
 				codeGen.combineTaint(insn.getRegTo(), insn.getRegArgA()),
 				insn);
 	}
 
 	// UTILS
 	
 	private static DexCodeElement generateInvoke(DexInstruction_Invoke invoke, DexInstruction_MoveResult moveResult) {
 		if (moveResult == null)
 			return invoke;
 		else
 			return new DexMacro(invoke, moveResult);
 	}
 	
 	protected static <T> List<? extends T> concat(Collection<? extends T> list1, Collection<? extends T> list2) {
 		List<T> result = new ArrayList<T>(list1.size() + list2.size());
 		result.addAll(list1);
 		result.addAll(list2);
 		return result;
 	}
 
 	protected static <T> List<? extends T> concat(Collection<? extends T> list1, T elem) {
 		List<T> result = new ArrayList<T>(list1.size() + 1);
 		result.addAll(list1);
 		result.add(elem);
 		return result;
 	}
 
 	private static boolean hasPrimitiveArgument(DexCode code) {
 		for (Parameter param : code.getParameters())
 			if (param.getType() instanceof DexPrimitiveType)
 				return true;
 		return false;
 	}
 	
 	private static List<DexTaintRegister> filterPrimitiveTaintRegisters(DexInstruction_Invoke insnInvoke) {
 		DexPrototype prototype = insnInvoke.getMethodId().getPrototype();
 		boolean isStatic = insnInvoke.getCallType() == Opcode_Invoke.Static;
 		int paramCount = prototype.getParameterCount(isStatic);
 		
 		List<DexTaintRegister> taintRegs = new ArrayList<DexTaintRegister>(paramCount);
 		
 		for (int i = 0; i < paramCount; i++) {
 			DexRegisterType paramType = prototype.getParameterType(i, isStatic, insnInvoke.getClassType());
 			if (paramType instanceof DexPrimitiveType)
 				taintRegs.add(insnInvoke.getArgumentRegisters().get(i).getTaintRegister());
 		}
 		
 		return taintRegs;
 	}
 	
 	private static List<DexTaintRegister> filterPrimitiveTaintRegisters(List<Parameter> params) {
 		List<DexTaintRegister> taintRegs = new ArrayList<DexTaintRegister>(params.size());
 
 		for (Parameter param : params)
 			if (param.getType() instanceof DexPrimitiveType)
 				taintRegs.add(param.getRegister().getTaintRegister());
 		
 		return taintRegs;
 	}
 }
