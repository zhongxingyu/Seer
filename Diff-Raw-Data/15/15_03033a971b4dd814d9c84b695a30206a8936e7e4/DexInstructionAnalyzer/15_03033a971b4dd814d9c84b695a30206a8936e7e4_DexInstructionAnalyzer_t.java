 package com.rx201.dx.translator;
 
 import java.util.List;
 
 import org.jf.dexlib.Code.Analysis.ValidationException;
 
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayLength;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CheckCast;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Compare;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Convert;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArrayData;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FilledNewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceOf;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Monitor;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveException;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Switch;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Throw;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Unknown;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexStandardRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.RegisterType;
 import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;
 import uk.ac.cam.db538.dexter.dex.method.DexMethod;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.DexType;
 import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
 
 import com.rx201.dx.translator.TypeSolver.CascadeType;
 
 public class DexInstructionAnalyzer implements DexInstructionVisitor{
 
 	private AnalyzedDexInstruction analyzedInst;
 	private DexTypeCache typeCache;
 	private DexType methodReturnType;
 
 	public DexInstructionAnalyzer(DexMethod method) {
 		this.typeCache = method.getParentFile().getTypeCache();
 		this.methodReturnType = method.getMethodDef().getMethodId().getPrototype().getReturnType();
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
 	public void visit(DexInstruction_Move instruction) {
 		RegisterType regType = instruction.getType();
 		RopType type;
 		
 		if (regType == RegisterType.SINGLE_PRIMITIVE)
 			type = RopType.Primitive;
 		else if (regType == RegisterType.WIDE_PRIMITIVE)
 			type = RopType.Wide;
 		else if (regType == RegisterType.REFERENCE)
 			type = RopType.WildcardReference;
 		else 
 			throw new ValidationException("Bad type");
 		
 		moveRegister(instruction.getRegFrom(), instruction.getRegTo(), type);
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
         	resultRopType = RopType.getRopType((DexRegisterType)i.getMethodId().getPrototype().getReturnType());
         } else if (prevAnalyzedInst.instruction instanceof DexInstruction_FilledNewArray) {
         	DexInstruction_FilledNewArray i = (DexInstruction_FilledNewArray)prevAnalyzedInst.instruction;
         	resultRopType = RopType.getRopType(i.getArrayType());
         } else {
             throw new ValidationException(analyzedInst.instruction.toString() + " must occur after an " +
                     "invoke-*/fill-new-array instruction");
         }
         
         return resultRopType;
 	}
 	@Override
 	public void visit(DexInstruction_MoveResult instruction) {
 		defineFreezedRegister(instruction.getRegTo(), analyzeMoveResult(instruction.getRegTo()));
 	}
 
 	@Override
 	public void visit(DexInstruction_MoveException instruction) {
 		assert analyzedInst.getPredecessorCount() == 1;
 		DexCodeElement catchElement = analyzedInst.getPredecessors().get(0).auxillaryElement;
 		assert catchElement != null && (catchElement instanceof DexCatch || catchElement instanceof DexCatchAll);
 		DexClassType exception = null;
 		if (catchElement instanceof DexCatch)
 			exception = ((DexCatch)catchElement).getExceptionType();
 		else 
 			exception = DexClassType.parse("Ljava/lang/Throwable;", typeCache);
 				
 		defineRegister(instruction.getRegTo(), RopType.getRopType(exception));
 	}
 	@Override
 	public void visit(DexInstruction_ReturnVoid instruction) {}
 	
 	@Override
 	public void visit(DexInstruction_Return instruction) {
 		useFreezedRegister(instruction.getRegFrom(), RopType.getRopType((DexRegisterType)methodReturnType));
 	}
 	
 	@Override
 	public void visit(DexInstruction_Const instruction) {
 		RopType type;
 
 		if (instruction.getRegTo().getWidth() == RegisterWidth.SINGLE) {
 			long value = instruction.getValue();
 			if (value == 0)
 				type = RopType.Zero;
 			else if (value == 1)
 				type = RopType.One;
 			else
 				type = RopType.Integer;
 		} else {
 			type = RopType.Wide;
 		} 
 
 		defineRegister(instruction.getRegTo(), type);
 	}
 
 	@Override
 	public void visit(DexInstruction_ConstString instruction) {
 		RopType type = RopType.getRopType(DexClassType.parse("Ljava/lang/String;", typeCache));
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	@Override
 	public void visit(DexInstruction_ConstClass instruction) {
 		RopType type = RopType.getRopType(instruction.getValue());
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	@Override
 	public void visit(DexInstruction_Monitor instruction) {
 		useRegister(instruction.getRegMonitor(), RopType.WildcardReference);
 	}
 	@Override
 	public void visit(DexInstruction_CheckCast instruction) {
 		RopType type = RopType.getRopType(instruction.getValue());
 		useFreezedRegister(instruction.getRegObject(), type);
 		defineFreezedRegister(instruction.getRegObject(), type);
 	}
 	@Override
 	public void visit(DexInstruction_InstanceOf instruction) {
 		defineRegister(instruction.getRegTo(), RopType.Boolean);
         //??
 		useFreezedRegister(instruction.getRegObject(), RopType.getRopType(instruction.getValue()));
 	}
 	@Override
 	public void visit(DexInstruction_ArrayLength instruction) {
         //??
 		useRegister(instruction.getRegArray(), RopType.Array);
 		defineFreezedRegister(instruction.getRegTo(), RopType.Integer);
 	}
 	@Override
 	public void visit(DexInstruction_NewInstance instruction) {
 		RopType type = RopType.getRopType(instruction.getValue());
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	@Override
 	public void visit(DexInstruction_NewArray instruction) {
 		RopType type = RopType.getRopType(instruction.getValue());
 		useFreezedRegister(instruction.getRegSize(), RopType.Integer);
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	
 	@Override
 	public void visit(DexInstruction_FilledNewArray instruction) {
 		RopType elementType = RopType.getRopType(instruction.getArrayType().getElementType());
 		for(DexRegister argument : instruction.getArgumentRegisters()) {
 			useFreezedRegister(argument, elementType);
 		}
 	}
 	
 	@Override
 	public void visit(DexInstruction_FillArrayData instruction) {
 		useRegister(instruction.getRegArray(), RopType.Array);
 	}
 	
 	@Override
 	public void visit(DexInstruction_Throw instruction) {
 		useRegister(instruction.getRegFrom(), RopType.WildcardReference);
 	}
 	
 	@Override
 	public void visit(DexInstruction_Goto instruction) {}
 	
 	@Override
 	public void visit(DexInstruction_Switch instruction) {
 		useRegister(instruction.getRegTest(), RopType.Integer);
 	}
 	
 	@Override
 	public void visit(DexInstruction_Compare instruction) {
 		switch(instruction.getOpcode()) {
 		case CmpgFloat:
 		case CmplFloat:
 			useFreezedRegister(instruction.getRegSourceA(), RopType.Float);
 			useFreezedRegister(instruction.getRegSourceB(), RopType.Float);
 			break;
 		case CmpLong:
 			useFreezedRegister(instruction.getRegSourceA(), RopType.LongLo);
 			useFreezedRegister(instruction.getRegSourceB(), RopType.LongLo);
 			break;
 		case CmplDouble:
 		case CmpgDouble:
 			useFreezedRegister(instruction.getRegSourceA(), RopType.DoubleLo);
 			useFreezedRegister(instruction.getRegSourceB(), RopType.DoubleLo);
 			break;
 		default:
 			assert false;
 			break;
 		
 		}
 		defineFreezedRegister(instruction.getRegTo(), RopType.Byte);
 	}
 
 	@Override
 	public void visit(DexInstruction_IfTest instruction) {
 		useRegister(instruction.getRegA(), RopType.Unknown);
 		useRegister(instruction.getRegB(), RopType.Unknown);
 		analyzedInst.addRegisterConstraint(instruction.getRegA(), instruction.getRegB(), CascadeType.Equivalent);
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
 	    	defineRegister(inst.getRegTo(), RopType.IntFloat);
 	    	break;
 		case Short:
 	    	defineFreezedRegister(inst.getRegTo(), RopType.Short);
 			break;
 		case Object:
     		defineRegister(inst.getRegTo(), RopType.WildcardReference);
 			break;
 		case Wide:
 			defineRegister(inst.getRegTo(), RopType.Wide);
 			break;
 		default:
 			throw new ValidationException("wrong type AGET");
     	}
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
 			useRegister(instruction.getRegFrom(), RopType.WildcardReference);
 			break;
 		case Short:
 			useFreezedRegister(instruction.getRegFrom(), RopType.Short);
 			break;
 		case Wide:
 			useRegister(instruction.getRegFrom(), RopType.Wide);
 		default:
 			assert false;
 			break;
 		}
 	}
 	
 	@Override
 	public void visit(DexInstruction_InstanceGet instruction) {
 		useFreezedRegister(instruction.getRegObject(), RopType.getRopType(
 				instruction.getFieldDef().getParentClass().getType()));
 		defineFreezedRegister(instruction.getRegTo(), RopType.getRopType(
 				instruction.getFieldDef().getFieldId().getType()));
 	}
 	
 	@Override
 	public void visit(DexInstruction_InstancePut instruction) {
 		useFreezedRegister(instruction.getRegObject(), RopType.getRopType(
 				instruction.getFieldDef().getParentClass().getType()));
 		useFreezedRegister(instruction.getRegFrom(), RopType.getRopType(
 				instruction.getFieldDef().getFieldId().getType()));
 	}
 	
 	@Override
 	public void visit(DexInstruction_StaticGet instruction) {
 		RopType type = RopType.getRopType(instruction.getFieldDef().getFieldId().getType());
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 	
 	@Override
 	public void visit(DexInstruction_StaticPut instruction) {
 		RopType type = RopType.getRopType(instruction.getFieldDef().getFieldId().getType());
 		useFreezedRegister(instruction.getRegFrom(), type);
 	}
 	
 	@Override
 	public void visit(DexInstruction_Invoke instruction) {
 		List<DexStandardRegister> arguments = instruction.getArgumentRegisters();
 		DexPrototype prototype = instruction.getMethodId().getPrototype();
 		
 		int regIndex = 0;
 		if (!instruction.getCallType().isStatic()) {
 			useFreezedRegister(arguments.get(regIndex++), RopType.getRopType(instruction.getClassType()));
 		}
 		
 		// We don't want to see 'this' parameter here.
		for(int i=0 ;i<prototype.getParameterCount(true); i++) {
			DexRegisterType paramType = prototype.getParameterType(i, true, null);
 			useFreezedRegister(arguments.get(regIndex), RopType.getRopType(paramType));
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
 			case NegDouble:
 				type = RopType.DoubleLo;
 		    	break;
 			case NegLong:
 			case NotLong:
 				type = RopType.LongLo;
 		    	break;
 		    default:
 		    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOp");
 		}
 		useFreezedRegister(instruction.getRegFrom(), type);
 		defineFreezedRegister(instruction.getRegTo(), type);
 	}
 
 	@Override
 	public void visit(DexInstruction_Convert instruction) {
 		RopType fromType, toType;
 	    switch (instruction.getInsnOpcode()) {
 		case FloatToInt:
 			fromType = RopType.Float;
 			toType = RopType.Integer;
 			break;
 		case IntToByte:
 			fromType = RopType.Integer;
 			toType = RopType.Byte;
 			break;
 		case IntToChar:
 			fromType = RopType.Integer;
 			toType = RopType.Char;
 			break;
 		case IntToFloat:
 			fromType = RopType.Integer;
 			toType = RopType.Float;
 			break;
 		case IntToShort:
 			fromType = RopType.Integer;
 			toType = RopType.Short;
 			break;
 		case DoubleToFloat:
 			fromType = RopType.DoubleLo;
 			toType = RopType.Float;
 			break;
 		case DoubleToInt:
 			fromType = RopType.DoubleLo;
 			toType = RopType.Integer;
 			break;
 		case DoubleToLong:
 			fromType = RopType.DoubleLo;
 			toType = RopType.LongLo;
 			break;
 		case FloatToDouble:
 			fromType = RopType.Float;
 			toType = RopType.DoubleLo;
 			break;
 		case FloatToLong:
 			fromType = RopType.Float;
 			toType = RopType.LongLo;
 			break;
 		case IntToDouble:
 			fromType = RopType.Integer;
 			toType = RopType.DoubleLo;
 			break;
 		case IntToLong:
 			fromType = RopType.Integer;
 			toType = RopType.LongLo;
 			break;
 		case LongToDouble:
 			fromType = RopType.LongLo;
 			toType = RopType.DoubleLo;
 			break;
 		case LongToFloat:
 			fromType = RopType.LongLo;
 			toType = RopType.Float;
 			break;
 		case LongToInt:
 			fromType = RopType.LongLo;
 			toType = RopType.Integer;
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_Convert");
 	    }
 		useFreezedRegister(instruction.getRegFrom(), fromType);
 		defineFreezedRegister(instruction.getRegTo(), toType);
 	}
 
 	@Override
 	public void visit(DexInstruction_BinaryOp instruction) {
 		RopType type;
 		boolean freezed = false; 
 		boolean isShiftOperator = false;
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
 		case AddDouble:
 		case SubDouble:
 		case MulDouble:
 		case DivDouble:
 		case RemDouble:
 			type = RopType.DoubleLo;
 			freezed = true;
 			break;
 		case AddLong:
 		case AndLong:
 		case DivLong:
 		case MulLong:
 		case OrLong:
 		case RemLong:
 		case SubLong:
 		case XorLong:
 			type = RopType.LongLo;
 			freezed = true;
 			break;
 		case ShlLong:
 		case ShrLong:
 		case UshrLong:
 			type = RopType.LongLo;
 			freezed = true;
 			isShiftOperator = true;
 			break;
 		default:
 			throw new ValidationException("Unknown opcode for DexInstruction_BinaryOp");
 		}
 		if (freezed) {
 			useFreezedRegister(instruction.getRegSourceA(), type);
 			
 			if (!isShiftOperator)
 				useFreezedRegister(instruction.getRegSourceB(), type);
 			else
 				useFreezedRegister(instruction.getRegSourceB(), RopType.Integer);
 			
 			defineFreezedRegister(instruction.getRegTarget(), type);
 		} else {
 			useRegister(instruction.getRegSourceA(), type);
 			
 			if (!isShiftOperator)
 				useRegister(instruction.getRegSourceB(), type);
 			else
 				useRegister(instruction.getRegSourceB(), RopType.Integer);
 			
 			defineRegister(instruction.getRegTarget(), type);
 		}
 	}
     		
 	@Override
 	public void visit(DexInstruction_BinaryOpLiteral instruction) {
 		useRegister(instruction.getRegSource(), RopType.Integer);
 		defineRegister(instruction.getRegTarget(), RopType.Integer);
 	}
 
 	@Override
 	public void visit(DexInstruction_Unknown instruction) {
 		assert false;
 	}
 };
