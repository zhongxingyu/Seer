 package uk.ac.cam.db538.dexter.transform.taint;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import uk.ac.cam.db538.dexter.dex.AuxiliaryDex;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayLength;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CheckCast;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTest;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
 import uk.ac.cam.db538.dexter.dex.code.macro.DexMacro;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleAuxiliaryRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexTaintRegister;
 import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
 import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
 import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 
 public final class CommonCodeGenerator {
 
 	private final AuxiliaryDex dexAux;
 	private final RuntimeHierarchy hierarchy;
 	private final DexTypeCache cache;
 	
 	private final DexClassType typeObject;
 	private final DexClassType typeClass;
 	private final DexClassType typeAnnotation;
 	private final DexClassType typeInteger;
 	private final DexClassType typeString;
 	private final DexClassType typeThreadLocal;
 	private final DexClassType typeThrowable;
 	private final DexClassType typeStackTraceElement;
 	private final DexArrayType typeIntArray;
 	private final DexArrayType typeStackTraceElementArray;
 	
 	private final MethodDefinition method_ThreadLocal_Get;
 	private final MethodDefinition method_ThreadLocal_Set;
 	private final MethodDefinition method_Integer_intValue;
 	private final MethodDefinition method_Integer_valueOf;
 	private final MethodDefinition method_Throwable_constructor;
 	private final MethodDefinition method_Throwable_getStackTrace;
 	private final MethodDefinition method_StackTraceElement_getClassName;
 	private final MethodDefinition method_Class_forName;
 	private final MethodDefinition method_Class_getAnnotation;
 	
 	private int regAuxId;
 	private int labelId;
 	
 	public CommonCodeGenerator(AuxiliaryDex dexAux) {
 		this.dexAux = dexAux;
 
 		this.hierarchy = dexAux.getHierarchy();
 		this.cache = hierarchy.getTypeCache();
 		
 		this.typeObject = DexClassType.parse("Ljava/lang/Object;", cache);
 		this.typeClass = DexClassType.parse("Ljava/lang/Class;", cache);
 		this.typeAnnotation = DexClassType.parse("Ljava/lang/annotation/Annotation;", cache);
 		this.typeInteger = DexClassType.parse("Ljava/lang/Integer;", cache);
 		this.typeString = DexClassType.parse("Ljava/lang/String;", cache);
 		this.typeThreadLocal = DexClassType.parse("Ljava/lang/ThreadLocal;", cache);
 		this.typeThrowable = DexClassType.parse("Ljava/lang/Throwable;", cache);
 		this.typeStackTraceElement = DexClassType.parse("Ljava/lang/StackTraceElement;", cache);
 		this.typeIntArray = DexArrayType.parse("[I", cache);
 		this.typeStackTraceElementArray = DexArrayType.parse("[Ljava/lang/StackTraceElement;", cache);
 	
 		DexPrototype prototype_void_to_void = DexPrototype.parse(cache.getCachedType_Void(), null, cache);
 		DexPrototype prototype_void_to_Object = DexPrototype.parse(typeObject, null, cache);
 		DexPrototype prototype_void_to_String = DexPrototype.parse(typeString, null, cache);
 		DexPrototype prototype_void_to_TraceElemArray = DexPrototype.parse(typeStackTraceElementArray, null, cache);
 		DexPrototype prototype_Object_to_void = DexPrototype.parse(cache.getCachedType_Void(), Arrays.asList(typeObject), cache);
 		DexPrototype prototype_void_to_int = DexPrototype.parse(cache.getCachedType_Integer(), null, cache);
 		DexPrototype prototype_int_to_Integer = DexPrototype.parse(typeInteger, Arrays.asList(cache.getCachedType_Integer()), cache);
 		DexPrototype prototype_String_to_Class = DexPrototype.parse(typeClass, Arrays.asList(typeString), cache);
 		DexPrototype prototype_Class_to_Annotation = DexPrototype.parse(typeAnnotation, Arrays.asList(typeClass), cache);
 		
 		DexMethodId methodId_get_void_to_Object = DexMethodId.parseMethodId("get", prototype_void_to_Object, cache);
 		DexMethodId methodId_set_Object_to_void = DexMethodId.parseMethodId("set", prototype_Object_to_void, cache);
 		DexMethodId methodId_intValue_void_to_int = DexMethodId.parseMethodId("intValue", prototype_void_to_int, cache);
 		DexMethodId methodId_valueOf_int_to_Integer = DexMethodId.parseMethodId("valueOf", prototype_int_to_Integer, cache);
 		DexMethodId methodId_constructor_void_to_void = DexMethodId.parseMethodId("<init>", prototype_void_to_void, cache);
 		DexMethodId methodId_getStackTrace_void_to_TraceElemArray = DexMethodId.parseMethodId("getStackTrace", prototype_void_to_TraceElemArray, cache);
 		DexMethodId methodId_getClassName_void_to_String = DexMethodId.parseMethodId("getClassName", prototype_void_to_String, cache);
 		DexMethodId methodId_forName_String_to_Class = DexMethodId.parseMethodId("forName", prototype_String_to_Class, cache);
 		DexMethodId methodId_getAnnotation_Class_to_Annotation = DexMethodId.parseMethodId("getAnnotation", prototype_Class_to_Annotation, cache);
 		
 		this.method_ThreadLocal_Get = lookupMethod(typeThreadLocal, methodId_get_void_to_Object);
 		this.method_ThreadLocal_Set = lookupMethod(typeThreadLocal, methodId_set_Object_to_void);
 		this.method_Integer_intValue = lookupMethod(typeInteger, methodId_intValue_void_to_int);
 		this.method_Integer_valueOf = lookupMethod(typeInteger, methodId_valueOf_int_to_Integer);
 		this.method_Throwable_constructor = lookupMethod(typeThrowable, methodId_constructor_void_to_void);
 		this.method_Throwable_getStackTrace = lookupMethod(typeThrowable, methodId_getStackTrace_void_to_TraceElemArray);
 		this.method_StackTraceElement_getClassName = lookupMethod(typeStackTraceElement, methodId_getClassName_void_to_String);
 		this.method_Class_forName = lookupMethod(typeClass, methodId_forName_String_to_Class);
 		this.method_Class_getAnnotation = lookupMethod(typeClass, methodId_getAnnotation_Class_to_Annotation);
 	}
 	
 	private MethodDefinition lookupMethod(DexReferenceType classType, DexMethodId methodId) {
 		MethodDefinition def = hierarchy.getClassDefinition(classType).getMethod(methodId);
 		
 		if (def == null)
 			throw new Error("Cannot find method " + methodId + " in class " + classType);
 		
 		return def;
 	}
 	
 	public void resetAsmIds() {
 		regAuxId = 0;
 		labelId = 0;
 	}
 	
 	public DexSingleAuxiliaryRegister auxReg() {
 		return new DexSingleAuxiliaryRegister(regAuxId++);
 	}
 	
 	public DexLabel label() {
 		return new DexLabel(labelId++);
 	}
 
 	public DexMacro getParamTaints(List<DexTaintRegister> taintRegs) {
 		DexSingleAuxiliaryRegister auxParamArray = auxReg();
 		
 		return new DexMacro(
 			getParamArray(auxParamArray),
 			getIntsFromArray(auxParamArray, taintRegs));
 	}
 	
 	public DexMacro setParamTaints(List<DexTaintRegister> taintRegs) {
 		DexSingleAuxiliaryRegister auxParamArray = auxReg();
 		
 		return new DexMacro(
 			getParamArray(auxParamArray),
 			putIntsInArray(auxParamArray, taintRegs));
 	}
 	
 	private DexMacro getParamArray(DexSingleRegister regTo) {
 		return new DexMacro(
 				// retrieve ThreadLocal<int[]> ARGS => auxReg1
 				new DexInstruction_StaticGet(regTo, dexAux.getField_CallParamTaint().getFieldDef(), hierarchy),
 				
 				// call auxReg1.get(); automatically initializes the array
 				new DexInstruction_Invoke(method_ThreadLocal_Get, Arrays.asList(regTo), hierarchy),
 				new DexInstruction_MoveResult(regTo, true, hierarchy),
 				
 				// cast auxReg1 to int[]
 				new DexInstruction_CheckCast(regTo, typeIntArray, hierarchy));
 	}
 	
 	private DexMacro getIntsFromArray(DexSingleRegister array, List<DexTaintRegister> taints) {
 		DexSingleAuxiliaryRegister auxIndex = auxReg();
 		
 		List<DexInstruction> insns = new ArrayList<DexInstruction>(taints.size() * 2);
 		
 		for (int i = 0; i < taints.size(); i++) {
 			insns.add(new DexInstruction_Const(auxIndex, i, hierarchy));
 			insns.add(new DexInstruction_ArrayGet(taints.get(i), array, auxIndex, Opcode_GetPut.IntFloat, hierarchy));
 		}
 		
 		return new DexMacro(insns);
 	}
 	
 	private DexMacro putIntsInArray(DexSingleRegister array, List<DexTaintRegister> taints) {
 		DexSingleAuxiliaryRegister auxIndex = auxReg();
 		
 		List<DexInstruction> insns = new ArrayList<DexInstruction>(taints.size() * 2);
 		
 		for (int i = 0; i < taints.size(); i++) {
 			insns.add(new DexInstruction_Const(auxIndex, i, hierarchy));
 			insns.add(new DexInstruction_ArrayPut(taints.get(i), array, auxIndex, Opcode_GetPut.IntFloat, hierarchy));
 		}
 		
 		return new DexMacro(insns);
 	}
 
 	public DexMacro getResultTaint(DexTaintRegister regTo) {
 		DexSingleAuxiliaryRegister auxReg = auxReg(); 
 		
 		return new DexMacro(
 			// retrieve ThreadLocal<Integer> RES => auxReg1
 			new DexInstruction_StaticGet(auxReg, dexAux.getField_CallResultTaint().getFieldDef(), hierarchy),
 			
 			// virtual call auxReg1.get() => auxReg1
 			new DexInstruction_Invoke(method_ThreadLocal_Get, Arrays.asList(auxReg), hierarchy),
 			new DexInstruction_MoveResult(auxReg, true, hierarchy),
 			
 			// cast auxReg1 to Integer
 			new DexInstruction_CheckCast(auxReg, typeInteger, hierarchy),
 			
 			// virtual call auxReg1.intValue()
 			new DexInstruction_Invoke(method_Integer_intValue, Arrays.asList(auxReg), hierarchy),
 			new DexInstruction_MoveResult(regTo, false, hierarchy));
 	}
 
 	public DexMacro setResultTaint(DexTaintRegister regFrom) {
 		DexSingleAuxiliaryRegister auxReg1 = auxReg(), auxReg2 = auxReg();
 		
 		return new DexMacro(
 			// static call Integer.valueOf(regFrom) => auxReg1
 			new DexInstruction_Invoke(method_Integer_valueOf, Arrays.asList(regFrom), hierarchy),
 			new DexInstruction_MoveResult(auxReg1, true, hierarchy),
 			
 			// retrieve ThreadLocal<Integer> RES => auxReg2
 			new DexInstruction_StaticGet(auxReg2, dexAux.getField_CallResultTaint().getFieldDef(), hierarchy),
 			
 			// virtual call auxReg2.set(auxReg1)
 			new DexInstruction_Invoke(method_ThreadLocal_Set, Arrays.asList(auxReg2, auxReg1), hierarchy));
 	}
 	
 	public DexMacro getMethodCaller(DexSingleRegister regName) {
 		DexSingleAuxiliaryRegister auxException = auxReg();
 		DexSingleAuxiliaryRegister auxStackTrace = auxReg();
 		DexSingleAuxiliaryRegister auxOne = auxReg();
 		DexSingleAuxiliaryRegister auxStackTraceLength = auxReg();
 		DexSingleAuxiliaryRegister auxCallerTrace = auxReg();
 		
 		DexLabel labelEmptyStack = label();
 		DexLabel labelEnd = label();
 		
 		return new DexMacro(
 			// auxException = new Exception()
 			new DexInstruction_NewInstance(auxException, typeThrowable, hierarchy),
 			new DexInstruction_Invoke(method_Throwable_constructor, Arrays.asList(auxException), hierarchy),
 			// auxStackTrace = auxException.getStackTrace()
 			new DexInstruction_Invoke(method_Throwable_getStackTrace, Arrays.asList(auxException), hierarchy),
 			new DexInstruction_MoveResult(auxStackTrace, true, hierarchy),
 			// if (auxStackTrace.length <= 1) => definitely external
 			new DexInstruction_Const(auxOne, 1, hierarchy),
 			new DexInstruction_ArrayLength(auxStackTraceLength, auxStackTrace, hierarchy),
 			new DexInstruction_IfTest(auxStackTraceLength, auxOne, labelEmptyStack, Opcode_IfTest.le, hierarchy),
 				// stack non-empty
 				// auxCallerTrace = auxStackTrace[1]
 				new DexInstruction_ArrayGet(auxCallerTrace, auxStackTrace, auxOne, Opcode_GetPut.Object, hierarchy),
 				// regName = auxCallerTrace.getClassName()
 				new DexInstruction_Invoke(method_StackTraceElement_getClassName, Arrays.asList(auxCallerTrace), hierarchy),
 				new DexInstruction_MoveResult(regName, true, hierarchy),
 				// goto L_END
 				new DexInstruction_Goto(labelEnd, hierarchy),
 			// else
 			labelEmptyStack,
 				// stack empty
 				// regTo = null
 				setZero(regName),
 			labelEnd);
 	}
 	
 	public DexMacro getClassAnnotation(DexSingleRegister regTo, DexSingleRegister regClassName, DexClassType annoType) {
 		DexSingleRegister auxInspectedClass = auxReg();
 		DexSingleRegister auxAnnoClass = auxReg();
 		
 		return new DexMacro(
 			// auxInspectedClass = Class.forName(regClassName)
 			new DexInstruction_Invoke(method_Class_forName, Arrays.asList(regClassName), hierarchy),
 			new DexInstruction_MoveResult(auxInspectedClass, true, hierarchy),
 			// auxAnnoClass class type
 			new DexInstruction_ConstClass(auxAnnoClass, annoType, hierarchy),
 			// regTo = auxInspectedClass.getAnnotation(auxAnnoClass)
 			new DexInstruction_Invoke(method_Class_getAnnotation, Arrays.asList(auxInspectedClass, auxAnnoClass), hierarchy),
 			new DexInstruction_MoveResult(regTo, true, hierarchy));
 	}
 	
 	/*
 	 * Combines taint of all the given registers. Does not matter if the given registers
 	 * are taint registers or not, because it automatically converts all of them to taint registers.
 	 */
 	public DexCodeElement combineTaint(DexRegister output, DexRegister ... inputs) {
 		DexTaintRegister outputTaint = taint(output);
 		
 		if (inputs.length == 0)
 			return setZero(outputTaint);
 		else if (inputs.length == 1)
 			return new DexInstruction_Move(outputTaint, taint(inputs[0]), false, hierarchy);
 		else if (inputs.length == 2)
 			return new DexInstruction_BinaryOp(outputTaint, taint(inputs[0]), taint(inputs[1]), Opcode_BinaryOp.OrInt, hierarchy);
 		else {
 			int count = inputs.length;
 			List<DexCodeElement> insns = new ArrayList<DexCodeElement>(count - 1);
 			
			// might have output equal to one of the inputs !
			DexSingleRegister aux = auxReg();
			
			insns.add(new DexInstruction_Move(aux, taint(inputs[0]), false, hierarchy));
			for (int i = 1; i < count; i++)
				insns.add(new DexInstruction_BinaryOp(aux, aux, taint(inputs[i]), Opcode_BinaryOp.OrInt, hierarchy));
			insns.add(new DexInstruction_Move(outputTaint, aux, false, hierarchy));			
 			
 			return new DexMacro(insns);
 		}
 	}
 	
 	private static DexTaintRegister taint(DexRegister reg) {
 		if (reg instanceof DexTaintRegister)
 			return (DexTaintRegister) reg;
 		else
 			return reg.getTaintRegister();
 	}
 	
 	public DexMacro setAllTo(List<? extends DexRegister> regs, long constant) {
 		List<DexInstruction> insns = new ArrayList<DexInstruction>(regs.size());
 		
 		for (DexRegister reg : regs)
 			insns.add(new DexInstruction_Const(reg, constant, hierarchy));
 		
 		return new DexMacro(insns);
 	}
 
 	public DexCodeElement jump(DexLabel target) {
 		return new DexInstruction_Goto(target, hierarchy);
 	}
 
 	public DexCodeElement setZero(DexRegister regTo) {
 		return new DexInstruction_Const(regTo, 0, hierarchy);
 	}
 	
 	public DexCodeElement ifZero(DexSingleRegister reg, DexLabel target) {
 		return new DexInstruction_IfTestZero(reg, target, Opcode_IfTestZero.eqz, hierarchy);		
 	}
 	
 	public DexMacro empty() {
 		return DexMacro.empty();
 	}
 }
