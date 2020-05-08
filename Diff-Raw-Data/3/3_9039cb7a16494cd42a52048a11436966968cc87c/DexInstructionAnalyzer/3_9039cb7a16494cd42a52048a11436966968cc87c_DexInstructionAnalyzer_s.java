 package com.rx201.dx.translator;
 
 import java.util.List;
 
 import org.jf.dexlib.Code.Analysis.ValidationException;
 
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGetWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayLength;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPutWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CheckCast;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CompareFloat;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CompareWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Convert;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertFromWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertToWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArrayData;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FilledNewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGetWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceOf;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePutWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Monitor;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveException;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Nop;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_PackedSwitchData;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_SparseSwitchData;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGetWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPutWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Switch;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Throw;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOpWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Unknown;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_ConvertWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_FilledNewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetInternalClassAnnotation;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetInternalMethodAnnotation;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetMethodCaller;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetQueryTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetServiceTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintInteger;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintIntegerConst;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintString;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
 import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
 import uk.ac.cam.db538.dexter.dex.code.insn.invoke.DexPseudoinstruction_Invoke;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 
 import com.rx201.dx.translator.TypeSolver.CascadeType;
 import com.rx201.dx.translator.util.DexRegisterHelper;
 
 public class DexInstructionAnalyzer implements DexInstructionVisitor{
 
 	private AnalyzedDexInstruction analyzedInst;
 	
 	public DexInstructionAnalyzer(DexCodeAnalyzer analyzer) {
 	}
 	
 	public void setAnalyzedInstruction(AnalyzedDexInstruction i) {
 		this.analyzedInst = i;
 	}
 	
 	public void defineRegister(DexRegister regTo, RopType registerType) {
 		analyzedInst.defineRegister(regTo, registerType, false);
 	}
 	public void defineFreezedRegister(DexRegister regTo, RopType registerType) {
 		analyzedInst.defineRegister(regTo, registerType, true);
 	}
 	public void useRegister(DexRegister regFrom, RopType registerType) {
 		analyzedInst.useRegister(regFrom, registerType, false);
 	}
 	public void useFreezedRegister(DexRegister regFrom, RopType registerType) {
 		analyzedInst.useRegister(regFrom, registerType, true);
 	}
 	public void moveRegister(DexRegister regFrom, DexRegister regTo, RopType registerType) {
 		analyzedInst.useRegister(regFrom, registerType, false);
 		analyzedInst.defineRegister(regTo, registerType, false);
 		analyzedInst.addRegisterConstraint(regTo, regFrom, CascadeType.Equivalent);
 	}
 	    
     
 	@Override
 	public void visit(DexInstruction_Nop instruction) {}
 	
 	@Override
 	public void visit(DexInstruction_Move instruction) {
 		moveRegister(instruction.getRegFrom(), instruction.getRegTo(), instruction.isObjectMoving()? RopType.Reference : RopType.Primitive);
 	}
 	@Override
 	public void visit(DexInstruction_MoveWide instruction) {
 		assert DexRegisterHelper.isPair(instruction.getRegFrom1(),  instruction.getRegFrom2());
 		assert DexRegisterHelper.isPair(instruction.getRegTo1(),  instruction.getRegTo2());
 
 		moveRegister(instruction.getRegFrom1(), instruction.getRegTo1(), RopType.Wide);
 		moveRegister(instruction.getRegFrom2(), instruction.getRegTo2(), RopType.Wide);
 
 	}
 	
 	private RopType analyzeMoveResult(DexRegister srcReg) {
 		
         AnalyzedDexInstruction prevAnalyzedInst = analyzedInst;
         //Skip auxillary blocks like TryBlockEnd etc.
         do {
     		assert prevAnalyzedInst.getPredecessorCount() == 1;
     		prevAnalyzedInst = prevAnalyzedInst.getPredecessors().get(0);
         } while (prevAnalyzedInst.instruction == null);
         
         RopType resultRopType;
         if (prevAnalyzedInst.instruction instanceof DexInstruction_Invoke) {
         	DexInstruction_Invoke i = (DexInstruction_Invoke) prevAnalyzedInst.instruction;
         	resultRopType = RopType.getRopType(i.getMethodPrototype().getReturnType().getDescriptor());
         } else if (prevAnalyzedInst.instruction instanceof DexInstruction_FilledNewArray) {
         	DexInstruction_FilledNewArray i = (DexInstruction_FilledNewArray)prevAnalyzedInst.instruction;
         	resultRopType = RopType.getRopType(i.getArrayType().getDescriptor());
         } else {
             throw new ValidationException(analyzedInst.instruction.getOriginalAssembly() + " must occur after an " +
                     "invoke-*/fill-new-array instruction");
         }
         
         return resultRopType;
 	}
 	@Override
 	public void visit(DexInstruction_MoveResult instruction) {
 		defineFreezedRegister(instruction.getRegTo(), 
 				analyzeMoveResult(instruction.getRegTo()));
 	}
 	@Override
 	public void visit(DexInstruction_MoveResultWide instruction) {
 		assert DexRegisterHelper.isPair(instruction.getRegTo1(),  instruction.getRegTo2());
 		RopType type = analyzeMoveResult(instruction.getRegTo1());
 		defineFreezedRegister(instruction.getRegTo1(), type);
 		defineFreezedRegister(instruction.getRegTo2(), type.lowToHigh());
 	}
 	@Override
 	public void visit(DexInstruction_MoveException instruction) {
 		assert analyzedInst.getPredecessorCount() == 1;
 		DexCodeElement catchElement = analyzedInst.getPredecessors().get(0).auxillaryElement;
 		assert catchElement != null && (catchElement instanceof DexCatch || catchElement instanceof DexCatchAll);
 		String exception = null;
 		if (catchElement instanceof DexCatch)
 			exception = ((DexCatch)catchElement).getExceptionType().getDescriptor();
 		else 
 			exception = "Ljava/lang/Throwable;";
 				
 		RopType type = RopType.getRopType(exception);
 		defineRegister(instruction.getRegTo(), type);
 	}
 	@Override
 	public void visit(DexInstruction_ReturnVoid instruction) {}
 	
 	@Override
 	public void visit(DexInstruction_Return instruction) {
 		String returnType = instruction.getParentMethod().getPrototype().getReturnType().getDescriptor();
 		useFreezedRegister(instruction.getRegFrom(), RopType.getRopType(returnType));
 	}
 	
 	@Override
 	public void visit(DexInstruction_ReturnWide instruction) {
 		if (instruction.getParentMethod().getPrototype().getReturnType().getDescriptor().equals("J")) {
 			useFreezedRegister(instruction.getRegFrom1(), RopType.LongLo);
 			useFreezedRegister(instruction.getRegFrom2(), RopType.LongHi);
 		} else {
 			useFreezedRegister(instruction.getRegFrom1(), RopType.DoubleLo);
 			useFreezedRegister(instruction.getRegFrom2(), RopType.DoubleHi);
 		}
 	}
 	
 	@Override
 	public void visit(DexInstruction_Const instruction) {
 		long value = instruction.getValue();
 		RopType type;
 		if (value == 0)
 			type = RopType.Zero;
 		else if (value == 1)
 			type = RopType.One;
 		else
 			type = RopType.Integer;
 		//??
 		defineRegister(instruction.getRegTo(), type);
 	}
 
 	@Override
 	public void visit(DexInstruction_ConstWide instruction) {
 		//??
 		defineRegister(instruction.getRegTo1(), RopType.Wide);
 		defineRegister(instruction.getRegTo2(), RopType.Wide);
 	}
 	
 	@Override
 	public void visit(DexInstruction_ConstString instruction) {
 		RopType type = RopType.getRopType("Ljava/lang/String;");
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	@Override
 	public void visit(DexInstruction_ConstClass instruction) {
 		RopType type = RopType.getRopType(instruction.getValue().getDescriptor());
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	@Override
 	public void visit(DexInstruction_Monitor instruction) {
 		useRegister(instruction.getRegMonitor(), RopType.Reference);
 	}
 	@Override
 	public void visit(DexInstruction_CheckCast instruction) {
 		RopType type = RopType.getRopType(instruction.getValue().getDescriptor());
 		useFreezedRegister(instruction.getRegObject(), type);
 		defineFreezedRegister(instruction.getRegObject(), type);
 	}
 	@Override
 	public void visit(DexInstruction_InstanceOf instruction) {
 		defineRegister(instruction.getRegTo(), RopType.Boolean);
         //??
 		useFreezedRegister(instruction.getRegObject(), RopType.getRopType(instruction.getValue().getDescriptor()));
 	}
 	@Override
 	public void visit(DexInstruction_ArrayLength instruction) {
         //??
 		useRegister(instruction.getRegArray(), RopType.Array);
 		defineFreezedRegister(instruction.getRegTo(), RopType.Integer);
 	}
 	@Override
 	public void visit(DexInstruction_NewInstance instruction) {
 		RopType type = RopType.getRopType(instruction.getValue().getDescriptor());
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	@Override
 	public void visit(DexInstruction_NewArray instruction) {
 		RopType type = RopType.getRopType(instruction.getValue().getDescriptor());
 		useFreezedRegister(instruction.getRegSize(), RopType.Integer);
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	
 	@Override
 	public void visit(DexInstruction_FilledNewArray instruction) {
 		RopType elementType = RopType.getRopType(instruction.getArrayType().getElementType().getDescriptor());
 		for(DexRegister argument : instruction.getArgumentRegisters()) {
 			useFreezedRegister(argument, elementType);
 		}
 	}
 	
 	@Override
 	public void visit(DexInstruction_FillArray instruction) {
 		useRegister(instruction.getRegArray(), RopType.Array);
 	}
 	
 	@Override
 	public void visit(DexInstruction_FillArrayData instruction) {}
 	
 	@Override
 	public void visit(DexInstruction_Throw instruction) {
 		useRegister(instruction.getRegFrom(), RopType.Reference);
 	}
 	
 	@Override
 	public void visit(DexInstruction_Goto instruction) {}
 	
 	@Override
 	public void visit(DexInstruction_Switch instruction) {
 		useRegister(instruction.getRegTest(), RopType.Integer);
 	}
 	
 	@Override
 	public void visit(DexInstruction_PackedSwitchData instruction) {}
 	
 	@Override
 	public void visit(DexInstruction_SparseSwitchData instruction) {}
 	
 	@Override
 	public void visit(DexInstruction_CompareFloat instruction) {
 		useFreezedRegister(instruction.getRegSourceA(), RopType.Float);
 		useFreezedRegister(instruction.getRegSourceB(), RopType.Float);
 		defineFreezedRegister(instruction.getRegTo(), RopType.Byte);
 	}
 	@Override
 	public void visit(DexInstruction_CompareWide instruction) {
 		switch(instruction.getInsnOpcode()) {
 		case CmpLong:
 			useFreezedRegister(instruction.getRegSourceA1(), RopType.LongLo);
 			useFreezedRegister(instruction.getRegSourceA2(), RopType.LongHi);
 			useFreezedRegister(instruction.getRegSourceB1(), RopType.LongLo);
 			useFreezedRegister(instruction.getRegSourceB2(), RopType.LongHi);
 			break;
 		case CmpgDouble:
 		case CmplDouble:
 			useFreezedRegister(instruction.getRegSourceA1(), RopType.DoubleLo);
 			useFreezedRegister(instruction.getRegSourceA2(), RopType.DoubleHi);
 			useFreezedRegister(instruction.getRegSourceB1(), RopType.DoubleLo);
 			useFreezedRegister(instruction.getRegSourceB2(), RopType.DoubleHi);
 			break;
 		default:
 			assert false;
 			break;
 		}
 		defineFreezedRegister(instruction.getRegTo(), RopType.Byte);
 	}
 	@Override
 	public void visit(DexInstruction_IfTest instruction) {
 		useRegister(instruction.getRegA(), RopType.Primitive);
 		useRegister(instruction.getRegB(), RopType.Primitive);
 	}
 	
 	@Override
 	public void visit(DexInstruction_IfTestZero instruction) {
 		useRegister(instruction.getReg(), RopType.Unknown); //TODO
 	}
 	
 	@Override
 	public void visit(DexInstruction_ArrayGet inst) {
 		useFreezedRegister(inst.getRegIndex(), RopType.Integer);
 		useRegister(inst.getRegArray(), RopType.Array);
 		analyzedInst.addRegisterConstraint(inst.getRegTo(), inst.getRegArray(), CascadeType.ArrayToElement);
 		
     	if (inst.getOpcode() == Opcode_GetPut.Object) {
     		defineRegister(inst.getRegTo(), RopType.Reference);
     	} else {
 	    	switch (inst.getOpcode()) {
 			case Boolean:
 		    	defineFreezedRegister(inst.getRegTo(), RopType.Boolean);
 				break;
 			case Byte:
 		    	defineFreezedRegister(inst.getRegTo(), RopType.Byte);
 				break;
 			case Char:
 		    	defineFreezedRegister(inst.getRegTo(), RopType.Char);
 				break;
 			case IntFloat:
 				//??
 		    	defineRegister(inst.getRegTo(), RopType.IntFloat);
 			case Short:
 		    	defineFreezedRegister(inst.getRegTo(), RopType.Short);
 				break;
 			default:
 				throw new ValidationException("wrong type AGET");
 	    	}
     	}
 	}		
 	@Override
 	public void visit(DexInstruction_ArrayGetWide instruction) {
 		useRegister(instruction.getRegArray(), RopType.Array);
 		useFreezedRegister(instruction.getRegIndex(), RopType.Integer);
 		analyzedInst.addRegisterConstraint(instruction.getRegTo1(), instruction.getRegArray(), CascadeType.ArrayToElement);
 		analyzedInst.addRegisterConstraint(instruction.getRegTo2(), instruction.getRegArray(), CascadeType.ArrayToElement);
 
 		defineRegister(instruction.getRegTo1(), RopType.Wide);
 		defineRegister(instruction.getRegTo2(), RopType.Wide);
 	}
 	@Override
 	public void visit(DexInstruction_ArrayPut instruction) {
 		useRegister(instruction.getRegArray(), RopType.Array);
 		useFreezedRegister(instruction.getRegIndex(), RopType.Integer);
 //		analyzedInst.addRegisterConstraint(instruction.getRegFrom(), instruction.getRegArray(), CascadeType.ArrayToElement);
 		
 		switch(instruction.getOpcode()) {
 		case Boolean:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Boolean);
 			break;
 		case Byte:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Byte);
 			break;
 		case Char:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Char);
 			break;
 		case IntFloat:
 			useRegister(instruction.getRegFrom(), RopType.IntFloat);
 			break;
 		case Object:
 			useRegister(instruction.getRegFrom(), RopType.Reference);
 			break;
 		case Short:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Short);
 			break;
 		default:
 			assert false;
 			break;
 		}
 	}
 	
 	@Override
 	public void visit(DexInstruction_ArrayPutWide instruction) {
 		useRegister(instruction.getRegArray(), RopType.Array);
 		useFreezedRegister(instruction.getRegIndex(), RopType.Integer);
 		analyzedInst.addRegisterConstraint(instruction.getRegFrom1(), instruction.getRegArray(), CascadeType.ArrayToElement);
 		analyzedInst.addRegisterConstraint(instruction.getRegFrom2(), instruction.getRegArray(), CascadeType.ArrayToElement);
 		useRegister(instruction.getRegFrom1(), RopType.Wide);
 		useRegister(instruction.getRegFrom2(), RopType.Wide);
 	}
 	
 	@Override
 	public void visit(DexInstruction_InstanceGet instruction) {
 		useFreezedRegister(instruction.getRegObject(), RopType.getRopType(instruction.getFieldClass().getDescriptor()));
 		defineFreezedRegister(instruction.getRegTo(), RopType.getRopType(instruction.getFieldType().getDescriptor()));
 	}
 	@Override
 	public void visit(DexInstruction_InstanceGetWide instruction) {
 		useFreezedRegister(instruction.getRegObject(), RopType.getRopType(instruction.getFieldClass().getDescriptor()));
 		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
 		defineFreezedRegister(instruction.getRegTo1(), type);
 		defineFreezedRegister(instruction.getRegTo2(), type.lowToHigh());
 		
 	}
 	@Override
 	public void visit(DexInstruction_InstancePut instruction) {
 		useFreezedRegister(instruction.getRegObject(), RopType.getRopType(instruction.getFieldClass().getDescriptor()));
 		useFreezedRegister(instruction.getRegFrom(), RopType.getRopType(instruction.getFieldType().getDescriptor()));
 	}
 	
 	@Override
 	public void visit(DexInstruction_InstancePutWide instruction) {
 		useFreezedRegister(instruction.getRegObject(), RopType.getRopType(instruction.getFieldClass().getDescriptor()));
 		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
 		useFreezedRegister(instruction.getRegFrom1(), type);
 		useFreezedRegister(instruction.getRegFrom2(), type.lowToHigh());
 	}
 	
 	@Override
 	public void visit(DexInstruction_StaticGet instruction) {
 		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	@Override
 	public void visit(DexInstruction_StaticGetWide instruction) {
 		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
 		defineFreezedRegister(instruction.getRegTo1(), type);
 		defineFreezedRegister(instruction.getRegTo2(), type.lowToHigh());
 
 	}
 	@Override
 	public void visit(DexInstruction_StaticPut instruction) {
 		useFreezedRegister(instruction.getRegFrom(), RopType.getRopType(instruction.getFieldType().getDescriptor()));
 	}
 	
 	@Override
 	public void visit(DexInstruction_StaticPutWide instruction) {
 		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
 		
 		useFreezedRegister(instruction.getRegFrom1(), type);
 		useFreezedRegister(instruction.getRegFrom2(), type.lowToHigh());
 	}
 	
 	
 	@Override
 	public void visit(DexInstruction_Invoke instruction) {
 		List<DexRegister> arguments = instruction.getArgumentRegisters();
 		List<DexRegisterType> parameterTypes = instruction.getMethodPrototype().getParameterTypes();
 		
 		int regIndex = 0;
 		if (!instruction.isStaticCall()) {
 			useFreezedRegister(arguments.get(regIndex++), RopType.getRopType(instruction.getClassType().getDescriptor()));
 		}
 		
 		for(int i=0 ;i<parameterTypes.size(); i++) {
 			DexRegisterType paramType = parameterTypes.get(i);
 			useFreezedRegister(arguments.get(regIndex), RopType.getRopType(paramType.getDescriptor()));
 			regIndex += paramType.getRegisters();
 		}
 	}
 
 	@Override
 	public void visit(DexInstruction_UnaryOp instruction) {
 		RopType type;
 		switch (instruction.getInsnOpcode()) {
 			case NegFloat:
 				type = RopType.Float;
 				break;
 			case NegInt:
 			case NotInt:
 				type = RopType.Integer;
 				break;
 		    default:
 		    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOp");
 		}
 		useFreezedRegister(instruction.getRegFrom(), type);
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	@Override
 	public void visit(DexInstruction_UnaryOpWide instruction) {
 		assert DexRegisterHelper.isPair(instruction.getRegFrom1(), instruction.getRegFrom2());
 		assert DexRegisterHelper.isPair(instruction.getRegTo1(), instruction.getRegTo2());
 
 		switch (instruction.getInsnOpcode()) {
 	    case NegDouble:
 			useFreezedRegister(instruction.getRegFrom1(), RopType.DoubleLo);
 			useFreezedRegister(instruction.getRegFrom2(), RopType.DoubleHi);
 			defineFreezedRegister(instruction.getRegTo1(), RopType.DoubleLo);
 			defineFreezedRegister(instruction.getRegTo2(), RopType.DoubleHi);
 	    	break;
 	    case NegLong:
 	    case NotLong:
 			useFreezedRegister(instruction.getRegFrom1(), RopType.LongLo);
 			useFreezedRegister(instruction.getRegFrom2(), RopType.LongHi);
 			defineFreezedRegister(instruction.getRegTo1(), RopType.LongLo);
 			defineFreezedRegister(instruction.getRegTo2(), RopType.LongHi);
 	    	break;
 	    default:
 	    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOpWide");
 	    }
 	}
 	@Override
 	public void visit(DexInstruction_Convert instruction) {
 	    switch (instruction.getInsnOpcode()) {
 		case FloatToInt:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Float);
 			defineFreezedRegister(instruction.getRegTo(), RopType.Integer);
 			break;
 		case IntToByte:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Integer);
 			defineFreezedRegister(instruction.getRegTo(), RopType.Byte);
 			break;
 		case IntToChar:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Integer);
 			defineFreezedRegister(instruction.getRegTo(), RopType.Char);
 			break;
 		case IntToFloat:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Integer);
 			defineFreezedRegister(instruction.getRegTo(), RopType.Float);
 			break;
 		case IntToShort:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Integer);
 			defineFreezedRegister(instruction.getRegTo(), RopType.Short);
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_Convert");
 	    
 	    }
 	}
 	@Override
 	public void visit(DexInstruction_ConvertWide instruction) {
 		assert DexRegisterHelper.isPair(instruction.getRegFrom1(), instruction.getRegFrom2());
 		assert DexRegisterHelper.isPair(instruction.getRegTo1(), instruction.getRegTo2());
 
 		if (instruction.getInsnOpcode() == Opcode_ConvertWide.DoubleToLong) {
 			useFreezedRegister(instruction.getRegFrom1(), RopType.DoubleLo);
 			useFreezedRegister(instruction.getRegFrom2(), RopType.DoubleHi);
 			defineFreezedRegister(instruction.getRegTo1(), RopType.LongLo);
 			defineFreezedRegister(instruction.getRegTo2(), RopType.LongHi);
 	    } else {
 			useFreezedRegister(instruction.getRegFrom1(), RopType.LongLo);
 			useFreezedRegister(instruction.getRegFrom2(), RopType.LongHi);
 			defineFreezedRegister(instruction.getRegTo1(), RopType.DoubleLo);
 			defineFreezedRegister(instruction.getRegTo2(), RopType.DoubleHi);
 	    }
 	}
 	@Override
 	public void visit(DexInstruction_ConvertFromWide instruction) {
 		assert DexRegisterHelper.isPair(instruction.getRegFrom1(), instruction.getRegFrom2());
 
 		RopType srcType, dstType;
 		
 		switch (instruction.getInsnOpcode()) {
 		case DoubleToFloat:
 			srcType = RopType.DoubleLo;
 			dstType = RopType.Float;
 			break;
 		case LongToFloat:
 			srcType = RopType.LongLo;
 			dstType = RopType.Float;
 			break;
 		case DoubleToInt:
 			srcType = RopType.DoubleLo;
 			dstType = RopType.Integer;
 			break;
 		case LongToInt:
 			srcType = RopType.LongLo;
 			dstType = RopType.Integer;
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_ConvertFromWide");
 	    }
 		useFreezedRegister(instruction.getRegFrom1(), srcType);
 		useFreezedRegister(instruction.getRegFrom2(), srcType.lowToHigh());
 		defineFreezedRegister(instruction.getRegTo(), dstType);
 	}
 	@Override
 	public void visit(DexInstruction_ConvertToWide instruction) {
 		assert DexRegisterHelper.isPair(instruction.getRegTo1(), instruction.getRegTo2());
 
 		RopType srcType, dstType;
 	    switch (instruction.getInsnOpcode()) {
 	    case FloatToDouble:
 			srcType = RopType.Float;
 			dstType = RopType.DoubleLo;
 			break;
 		case IntToDouble:
 			srcType = RopType.Integer;
 			dstType = RopType.DoubleLo;
 			break;
 		case FloatToLong:
 			srcType = RopType.Float;
 			dstType = RopType.LongLo;
 			break;
 		case IntToLong:
 			srcType = RopType.Integer;
 			dstType = RopType.LongLo;
 			break;
 		default:
 	    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOpWide");
 	    }
 		useFreezedRegister(instruction.getRegFrom(), srcType);
 		defineFreezedRegister(instruction.getRegTo1(), dstType);
 		defineFreezedRegister(instruction.getRegTo2(), dstType.lowToHigh());
 	}
     
 	@Override
 	public void visit(DexInstruction_BinaryOp instruction) {
 		RopType type;
 		boolean freezed = false; 
 		switch(instruction.getInsnOpcode()) {
 		case AddFloat:
 		case SubFloat:
 		case DivFloat:
 		case MulFloat:
 		case RemFloat:
 			type = RopType.Float;
 			freezed = true;
 			break;
 		case AddInt:
 		case SubInt:
 		case MulInt:
 		case DivInt:
 		case RemInt:
 		case ShlInt:
 		case ShrInt:
 		case UshrInt:
 			type = RopType.Integer;
 			freezed = true;
 			break;
 		case AndInt:
 		case OrInt:
 		case XorInt:
 			type = RopType.Integer;
 			freezed = false;
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_BinaryOp");
 		}
 		if (freezed) {
 			useFreezedRegister(instruction.getRegSourceA(), type);
 			useFreezedRegister(instruction.getRegSourceB(), type);
 			defineFreezedRegister(instruction.getRegTarget(), type);
 		} else {
 			useRegister(instruction.getRegSourceA(), type);
 			useRegister(instruction.getRegSourceB(), type);
 			defineRegister(instruction.getRegTarget(), type);
 		}
 	}
     		
 	@Override
 	public void visit(DexInstruction_BinaryOpLiteral instruction) {
 		useRegister(instruction.getRegSource(), RopType.Integer);
 		defineRegister(instruction.getRegTarget(), RopType.Integer);
 	}
 	@Override
 	public void visit(DexInstruction_BinaryOpWide instruction) {
 		assert DexRegisterHelper.isPair(instruction.getRegSourceA1(), instruction.getRegSourceA2());
 		assert DexRegisterHelper.isPair(instruction.getRegSourceB1(), instruction.getRegSourceB2());
 		assert DexRegisterHelper.isPair(instruction.getRegTarget1(), instruction.getRegTarget2());
 		
 		switch(instruction.getInsnOpcode()){
 		case AddDouble:
 		case SubDouble:
 		case MulDouble:
 		case DivDouble:
 		case RemDouble:
 			useFreezedRegister(instruction.getRegSourceA1(), RopType.DoubleLo);
 			useFreezedRegister(instruction.getRegSourceA2(), RopType.DoubleHi);
 			useFreezedRegister(instruction.getRegSourceB1(), RopType.DoubleLo);
 			useFreezedRegister(instruction.getRegSourceB2(), RopType.DoubleHi);
 			defineFreezedRegister(instruction.getRegTarget1(), RopType.DoubleLo);
 			defineFreezedRegister(instruction.getRegTarget2(), RopType.DoubleHi);
 			break;
 		case AddLong:
 		case AndLong:
 		case DivLong:
 		case MulLong:
 		case OrLong:
 		case RemLong:
 		case SubLong:
		case UshrLong:
 		case XorLong:
 			useFreezedRegister(instruction.getRegSourceA1(), RopType.LongLo);
 			useFreezedRegister(instruction.getRegSourceA2(), RopType.LongHi);
 			useFreezedRegister(instruction.getRegSourceB1(), RopType.LongLo);
 			useFreezedRegister(instruction.getRegSourceB2(), RopType.LongHi);
 			defineFreezedRegister(instruction.getRegTarget1(), RopType.LongLo);
 			defineFreezedRegister(instruction.getRegTarget2(), RopType.LongHi);
 			break;
 		case ShlLong:
 		case ShrLong:
 			useFreezedRegister(instruction.getRegSourceA1(), RopType.LongLo);
 			useFreezedRegister(instruction.getRegSourceA2(), RopType.LongHi);
 			useFreezedRegister(instruction.getRegSourceB1(), RopType.Integer);
 			defineFreezedRegister(instruction.getRegTarget1(), RopType.LongLo);
 			defineFreezedRegister(instruction.getRegTarget2(), RopType.LongHi);
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_BinaryOpWide");
 		}
 	}
 	@Override
 	public void visit(DexInstruction_Unknown instruction) {
 		assert false;
 	}
 
 	@Override
 	public void visit(DexMacro_FilledNewArray DexMacro_FilledNewArray) {}
 
 	@Override
 	public void visit(DexMacro_GetInternalClassAnnotation DexMacro_GetInternalClassAnnotation) {}
 
 	@Override
 	public void visit(DexMacro_GetInternalMethodAnnotation DexMacro_GetInternalMethodAnnotation) {}
 
 	@Override
 	public void visit(DexMacro_GetMethodCaller DexMacro_GetMethodCaller) {}
 
 	@Override
 	public void visit(DexMacro_GetObjectTaint DexMacro_GetObjectTaint) {}
 
 	@Override
 	public void visit(DexMacro_GetQueryTaint DexMacro_GetQueryTaint) {}
 
 	@Override
 	public void visit(DexMacro_GetServiceTaint DexMacro_GetServiceTaint) {}	
 	
 	@Override
 	public void visit(DexMacro_PrintInteger DexMacro_PrintInteger) {}
 
 	@Override
 	public void visit(DexMacro_PrintIntegerConst DexMacro_PrintIntegerConst) {}
 
 	@Override
 	public void visit(DexMacro_PrintString DexMacro_PrintString) {}	
 	
 	@Override
 	public void visit(DexMacro_PrintStringConst DexMacro_PrintStringConst) {}
 
 	@Override
 	public void visit(DexMacro_SetObjectTaint DexMacro_SetObjectTaint) {}
 	
 	@Override
 	public void visit(DexPseudoinstruction_Invoke DexMacro_Invoke) {}
 	
 };
