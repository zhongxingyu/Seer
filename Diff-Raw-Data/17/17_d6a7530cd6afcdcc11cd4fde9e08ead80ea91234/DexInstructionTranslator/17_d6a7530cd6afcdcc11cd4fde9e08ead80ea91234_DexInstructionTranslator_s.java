 package com.rx201.dx.translator;
 
 import static com.android.dx.rop.type.Type.BT_BYTE;
 import static com.android.dx.rop.type.Type.BT_CHAR;
 import static com.android.dx.rop.type.Type.BT_INT;
 import static com.android.dx.rop.type.Type.BT_SHORT;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.ac.cam.db538.dexter.dex.code.InstructionList;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
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
 import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.hierarchy.InstanceFieldDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.StaticFieldDefinition;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 import com.android.dx.rop.code.FillArrayDataInsn;
 import com.android.dx.rop.code.Insn;
 import com.android.dx.rop.code.PlainCstInsn;
 import com.android.dx.rop.code.PlainInsn;
 import com.android.dx.rop.code.RegOps;
 import com.android.dx.rop.code.RegisterSpec;
 import com.android.dx.rop.code.RegisterSpecList;
 import com.android.dx.rop.code.Rop;
 import com.android.dx.rop.code.Rops;
 import com.android.dx.rop.code.SourcePosition;
 import com.android.dx.rop.code.SwitchInsn;
 import com.android.dx.rop.code.ThrowingCstInsn;
 import com.android.dx.rop.code.ThrowingInsn;
 import com.android.dx.rop.cst.Constant;
 import com.android.dx.rop.cst.CstBaseMethodRef;
 import com.android.dx.rop.cst.CstBoolean;
 import com.android.dx.rop.cst.CstByte;
 import com.android.dx.rop.cst.CstChar;
 import com.android.dx.rop.cst.CstDouble;
 import com.android.dx.rop.cst.CstFieldRef;
 import com.android.dx.rop.cst.CstFloat;
 import com.android.dx.rop.cst.CstInteger;
 import com.android.dx.rop.cst.CstKnownNull;
 import com.android.dx.rop.cst.CstLong;
 import com.android.dx.rop.cst.CstMethodRef;
 import com.android.dx.rop.cst.CstNat;
 import com.android.dx.rop.cst.CstShort;
 import com.android.dx.rop.cst.CstString;
 import com.android.dx.rop.cst.CstType;
 import com.android.dx.rop.cst.TypedConstant;
 import com.android.dx.rop.type.StdTypeList;
 import com.android.dx.rop.type.Type;
 import com.android.dx.rop.type.TypeBearer;
 import com.android.dx.rop.type.TypeList;
 import com.android.dx.util.IntList;
 
 
 class DexConvertedResult {
     public ArrayList<Insn> insns;
     public ArrayList<Insn> auxInsns; // Insns that needs to be propagated to successor basic blocks.
     public AnalyzedDexInstruction primarySuccessor;
     public ArrayList<AnalyzedDexInstruction> successors;
 
     public DexConvertedResult() {
         insns = new ArrayList<Insn>();
         auxInsns = new ArrayList<Insn>();
         primarySuccessor = null;
         successors = new ArrayList<AnalyzedDexInstruction>();
     }
 
     public DexConvertedResult setPrimarySuccessor(AnalyzedDexInstruction successor) {
         primarySuccessor = successor;
         return this;
     }
 
     public DexConvertedResult addSuccessor(AnalyzedDexInstruction s) {
         successors.add(s);
         return this;
     }
 
     public DexConvertedResult addInstruction(Insn insn) {
         insns.add(insn);
         return this;
     }
 
     public DexConvertedResult addAuxInstruction(Insn insn) {
         auxInsns.add(insn);
         return this;
     }
 }
 
 
 public class DexInstructionTranslator implements DexInstructionVisitor {
 
     private DexCodeAnalyzer analyzer;
     private DexConvertedResult result;
     private AnalyzedDexInstruction curInst;
     private InstructionList instructionList;
 
     public DexInstructionTranslator(DexCodeAnalyzer analyzer, InstructionList instructionList) {
         this.analyzer = analyzer;
         this.instructionList = instructionList;
     }
 
 
     public DexConvertedResult translate(AnalyzedDexInstruction inst) {
         result = new DexConvertedResult();
         curInst = inst;
 
         // Instruction visitor is free to patch up successor/primarysuccessor
         // this is currently done in Switch inst.
         if (inst.getInstruction() != null)
             inst.getInstruction().accept(this);
 
         List<AnalyzedDexInstruction> successors = inst.getSuccesors();
         if (successors.size() > 1) {
             // Because AnalyzedDexInstruction.Successors are not in any particular order,
             // individual instruction need to set their primary successor itself.
             assert result.primarySuccessor != null || result.successors.size() != 0;
         } else if (successors.size() == 1) {
             // Fill in default value only if individual handler does not override it.
             if (result.primarySuccessor == null && result.successors.size() == 0) {
                 result.setPrimarySuccessor(successors.get(0));
                 result.addSuccessor(result.primarySuccessor);
             }
         } else {
             assert inst.getInstruction() == null || inst.getInstruction().cfgExitsMethod(null);
         }
 
         return result;
     }
 
     private Type toType(RopType t) {
         switch(t.category) {
         case Boolean:
         case One: // happened in translation stage of switch test case
             return Type.intern("Z");
         case Byte:
             return Type.intern("B");
         case Short:
             return Type.intern("S");
         case Char:
             return Type.intern("C");
        case Null:
            System.out.println("Warning: Ambiguous null value.");
         case Integer:
             return Type.intern("I");
         case Float:
             return Type.intern("F");
         case LongLo:
         case LongHi:
         case Wide:
             return Type.intern("J");
         case DoubleLo:
         case DoubleHi:
             return Type.intern("D");
         case WildcardRef: {
             StringBuilder sb = new StringBuilder();
             for(int i=0; i<t.arrayDepth; i++)
                 sb.append('[');
             sb.append("Ljava/lang/Object;");
             return Type.intern(sb.toString());
         }
         case Reference:
             return Type.intern(t.type.getDescriptor());
 
         default:
             throw new UnsupportedOperationException("Unknown register type");
         }
     }
 
     private RegisterSpec toRegSpec(DexRegister reg, RopType registerType) {
         return RegisterSpec.make(analyzer.normalizeRegister(reg), toType(registerType));
     }
 
     private RegisterSpec getSourceRegSpec(DexRegister reg) {
         return toRegSpec(reg, curInst.getUsedRegisterType(reg));
     }
 
     private RegisterSpec getDestRegSpec(DexRegister reg) {
         return toRegSpec(reg, curInst.getDefinedRegisterType(reg));
     }
 
     private List<AnalyzedDexInstruction> getCatchers(Rop opcode) {
         ArrayList<AnalyzedDexInstruction> result = new ArrayList<AnalyzedDexInstruction>();
 
         boolean canThrow = opcode == null || opcode.getExceptions().size() > 0;
 
         if (canThrow) {
             for(DexCodeElement successor : curInst.getInstruction().cfgGetExceptionSuccessors(instructionList)) {
                 result.add(analyzer.reverseLookup(successor));
             }
         }
 
         return result;
     }
 
 //	// Even though this is more precise, it is not consistent with what dx is doing
 //	// See DexInstruction.cfgGetExceptionSuccessors()
 //	private List<AnalyzedDexInstruction> getCatchers_precise(Rop opcode) {
 //		ArrayList<AnalyzedDexInstruction> result = new ArrayList<AnalyzedDexInstruction>();
 //		DexCode code = curInst.getInstruction().getMethodCode();
 //		val classHierarchy = code.getParentFile().getHierarchy();
 //
 //		ArrayList<DexClassType> thrownExceptions = new ArrayList<DexClassType>();
 //		TypeList exceptions = opcode.getExceptions();
 //		for(int i=0; i<exceptions.size(); i++)
 //			thrownExceptions.add(DexClassType.parse(exceptions.getType(i).getDescriptor(), code.getParentFile().getParsingCache()));
 //
 //		// Order of catch matters, so we need to preserve that, which means
 //		// we cannot just iterate through the successors ( which is unordered)
 //		// The following code is duplicated from DexInstruction, as we need the result to be ordered.
 //	    for (val tryBlockEnd : code.getTryBlocks()) {
 //	        val tryBlockStart = tryBlockEnd.getBlockStart();
 //
 //	        // check that the instruction is in this try block
 //	        if (code.isBetween(tryBlockStart, tryBlockEnd, curInst.getInstruction())) {
 //
 //	            for (val catchBlock : tryBlockStart.getCatchHandlers()) {
 //
 //					val catchException = classHierarchy.getClassDefinition(catchBlock.getExceptionType());
 //
 //					boolean canCatch = false;
 //					// Check if the exception thrown by the given Rop can potentially
 //					// be caught by the current catch block.
 //				    // which is either the catch block catches the given exception type or its ancestor (a guaranteed catch)
 //			        // or if the catch is the subclass of the thrown exception (a potential catch)
 // 					// **Logic duplicated from DexInstruction.throwingInsn_CatchHandlers
 //					for(DexClassType thrown : thrownExceptions) {
 //						val thrownException = classHierarchy.getClassDefinition(thrown);
 //						if (thrownException.isChildOf(catchException) ||
 //							catchException.isChildOf(thrownException)) {
 //							canCatch = true;
 //							break;
 //						}
 //					}
 //					if (canCatch)
 //						result.add(analyzer.reverseLookup(catchBlock));
 //	            }
 //				// if the block has CatchAll handler, it can jump to it
 //				val catchAllHandler = tryBlockStart.getCatchAllHandler();
 //				if (catchAllHandler != null)
 //					result.add(analyzer.reverseLookup(catchAllHandler));
 //	        }
 //		}
 //		return result;
 //	}
 
     private TypeList getCaughtExceptions(List<AnalyzedDexInstruction> catchers) {
         TypeList result = StdTypeList.EMPTY;
         for (AnalyzedDexInstruction i : catchers) {
             DexCodeElement aux = i.auxillaryElement;
             assert aux != null;
 
             if (aux instanceof DexCatch) {
                 result = result.withAddedType(Type.intern(((DexCatch)aux).getExceptionType().getDescriptor()));
             } else if (aux instanceof DexCatchAll) {
                 //result = result.withAddedType(Type.intern("Ljava/lang/Exception;"));Type.OBJECT
                 result = result.withAddedType(Type.OBJECT);
             }
         }
         return result;
     }
 
     private RegisterSpecList makeOperands(DexRegister ... operands) {
         if (operands.length == 0)
             return RegisterSpecList.EMPTY;
         RegisterSpecList result = new RegisterSpecList(operands.length);
         for(int i=0; i <operands.length; i++)
             result.set(i, getSourceRegSpec(operands[i]));
         return result;
     }
 
 
     private CstInteger makeCstInteger(int value) {
         return  CstInteger.make(value);
     }
 
     private CstString makeCstString(String stringConstant) {
         return new CstString(stringConstant);
     }
 
     private CstType makeCstType(DexReferenceType value) {
         return new CstType(Type.intern(value.getDescriptor()));
     }
 
     private CstNat makeCstNat(String name, String type) {
         return new CstNat(makeCstString(name), makeCstString(type));
     }
 
     private CstFieldRef makeFieldRef(DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
         return new CstFieldRef(
                    makeCstType(fieldClass),
                    makeCstNat(fieldName, fieldType.getDescriptor())
                );
     }
 
 ////////////////////////////////////////////////////////////////////////////////
     private void doPlainInsn(Rop opcode, RegisterSpec dst, DexRegister ... srcs) {
         result.addInstruction(new PlainInsn(opcode, SourcePosition.NO_INFO, dst, makeOperands(srcs)));
     }
 
     private void doPlainCstInsn(Rop opcode, RegisterSpec dst, Constant constant, DexRegister ... srcs) {
         result.addInstruction(new PlainCstInsn(opcode, SourcePosition.NO_INFO, dst, makeOperands(srcs), constant));
     }
 
     private void doThrowingInsn(Rop opcode, DexRegister ... srcs) {
         List<AnalyzedDexInstruction> catchers = getCatchers(opcode);
         result.addInstruction(new ThrowingInsn(opcode, SourcePosition.NO_INFO, makeOperands(srcs), getCaughtExceptions(catchers)));
         doThrowingSuccessors(catchers);
     }
 
     private void doThrowingCstInsn(Rop opcode, Constant constant, DexRegister ... srcs) {
         List<AnalyzedDexInstruction> catchers = getCatchers(opcode);
         result.addInstruction(new ThrowingCstInsn(opcode, SourcePosition.NO_INFO, makeOperands(srcs), getCaughtExceptions(catchers), constant));
         doThrowingSuccessors(catchers);
     }
 
     // instructions like INSTANCE_OF, ARRAY_LENGTH needs a macro move-result Insn, which (I guess) only
     // helps with flow analysis and does not contribute to the actual assembled code.
     private void doPseudoMoveResult(DexRegister to) {
         RegisterSpec dst = getDestRegSpec(to);
         result.addAuxInstruction(new PlainInsn(Rops.opMoveResultPseudo(dst), SourcePosition.NO_INFO, dst, RegisterSpecList.EMPTY));
     }
 
     private CstBaseMethodRef makeMethodRef(DexReferenceType classType, String methodName, DexPrototype methodPrototype) {
         CstType clazz = makeCstType(classType);
         CstNat method = makeCstNat(methodName, methodPrototype.getDescriptor());
         return new CstMethodRef(clazz, method);
     }
 
 ////////////////////////////////////////////////////////////////////////////////
 
     private void doIfSuccessors(DexLabel target) {
         AnalyzedDexInstruction primary = null, secondary = null;
 
         // assert curInst.getSuccessorCount() == 2;
         // This assertion turns out to be INCORRECT... We can have dummy IF where
         // primary and secondary successors are the same: charSubTest()@aosp/003-omnibus-opcodes/IntMath.java
         if (curInst.getSuccessorCount() == 1) {
             primary = secondary = curInst.getOnlySuccesor();
         } else {
             for(AnalyzedDexInstruction successor : curInst.getSuccesors()) {
                 if (successor.auxillaryElement != target) // Primary successor for If is the fall over block
                     primary = successor;
                 else
                     secondary = successor;
             }
         }
         result.setPrimarySuccessor(primary);
         // Order matters?? Yes they do.. try assertion unit test.
         // It is strange that in IFs primary successor comes first but for
         // throwing (below) it comes last
         result.addSuccessor(primary).addSuccessor(secondary);
     }
 
 
     private void doThrowingSuccessors(List<AnalyzedDexInstruction> catchers) {
         DexInstruction instruction = curInst.getInstruction();
         AnalyzedDexInstruction primarySuccessor;
         if (instruction instanceof DexInstruction_Throw) {
             primarySuccessor = null;
         } else {
             primarySuccessor = analyzer.reverseLookup(instructionList.getNextInstruction(instruction));
         }
         result.setPrimarySuccessor(primarySuccessor);
 
         // Make sure that the successor list is consistent with catchers
         for(AnalyzedDexInstruction successor : curInst.getSuccesors()) {
             if (successor == primarySuccessor)
                 continue;
 
             assert catchers.contains(successor);
         }
         for(AnalyzedDexInstruction catcher: catchers)
             result.addSuccessor(catcher);
 
         /*** StdCatcherBuilder.java: ***
          * Blocks that throw are supposed to list their primary
          * successor -- if any -- last in the successors list, but
          * that constraint appears to be violated here.
          */
         if (primarySuccessor != null)
             result.addSuccessor(primarySuccessor);
     }
 ////////////////////////////////////////////////////////////////////////////////
 
     private void doMove(DexRegister to, DexRegister from) {
         RegisterSpec dst = getDestRegSpec(to);
         doPlainInsn(Rops.opMove(dst), dst, from);
     }
 
 
     @Override
     public void visit(DexInstruction_Move instruction) {
         doMove(instruction.getRegTo(), instruction.getRegFrom());
     }
 
 
     private void doMoveResult(DexRegister to) {
         // opMoveResultmacro if afterNonInvokeThrowingInsn, or FilledNewArray
         // Skip TryBlockEnd and other auxiliary instructions in between.
         AnalyzedDexInstruction prev = curInst;
         do {
             assert prev.getPredecessorCount() == 1;
             prev = prev.getPredecessors().get(0);
         } while(prev.getInstruction() == null);
 
         boolean legitimate = prev.getInstruction() instanceof DexInstruction_Invoke || prev.getInstruction() instanceof DexInstruction_FilledNewArray ;
         assert legitimate;
 
         RegisterSpec dst = getDestRegSpec(to);
         doPlainInsn(Rops.opMoveResult(dst), dst);
     }
 
     @Override
     public void visit(DexInstruction_MoveResult instruction) {
         doMoveResult(instruction.getRegTo());
     }
 
 
     @Override
     public void visit(DexInstruction_MoveException instruction) {
         RegisterSpec dst = getDestRegSpec(instruction.getRegTo());
         doPlainInsn(Rops.opMoveException(dst), dst);
     }
 
 
     @Override
     public void visit(DexInstruction_ReturnVoid instruction) {
         doPlainInsn(Rops.RETURN_VOID, null);
     }
 
 
     private void doReturn(DexRegister from) {
         doPlainInsn(Rops.opReturn(getSourceRegSpec(from)), null, from);
     }
 
     @Override
     public void visit(DexInstruction_Return instruction) {
         doReturn(instruction.getRegFrom());
     }
 
 
     private void doConst(DexRegister to, long value) {
         TypedConstant constant;
         RopType type = curInst.getDefinedRegisterType(to);
 
         switch (type.category) {
         case Boolean:
         case One:
         case Byte:
         case Char:
         case Short:
         case Integer:
             constant = CstInteger.make((int)value);
             break;
         case Float:
             constant = CstFloat.make((int)value);
             break;
         case LongLo:
         case LongHi:
         case Wide:
             constant = CstLong.make(value);
             break;
         case DoubleLo:
         case DoubleHi:
             constant = CstDouble.make(value);
             break;
         case Null:
             constant = CstKnownNull.THE_ONE;
             break;
         case Reference:
             if (value == 0) {
                 constant = CstKnownNull.THE_ONE;
                 break;
             } else
                 throw new RuntimeException("Bad reference type.");
         default:
             throw new RuntimeException("Unknown constant type.");
         }
         RegisterSpec dst = RegisterSpec.make(analyzer.normalizeRegister(to), constant.getType());
         doPlainCstInsn(Rops.opConst(dst), dst, constant);
     }
 
     @Override
     public void visit(DexInstruction_Const instruction) {
         doConst(instruction.getRegTo(), instruction.getValue());
     }
 
 
     @Override
     public void visit(DexInstruction_ConstString instruction) {
         // ConstString can throw.
         doThrowingCstInsn(Rops.CONST_OBJECT, makeCstString(instruction.getStringConstant()));
         doPseudoMoveResult(instruction.getRegTo());
     }
 
 
     @Override
     public void visit(DexInstruction_ConstClass instruction) {
         // ConstClass can throw.
         doThrowingCstInsn(Rops.CONST_OBJECT, makeCstType(instruction.getValue()));
         doPseudoMoveResult(instruction.getRegTo());
     }
 
 
     @Override
     public void visit(DexInstruction_Monitor instruction) {
         doThrowingInsn(instruction.isEnter() ? Rops.MONITOR_ENTER : Rops.MONITOR_EXIT, instruction.getRegMonitor());
     }
 
 
     @Override
     public void visit(DexInstruction_CheckCast instruction) {
         doThrowingCstInsn(Rops.CHECK_CAST, makeCstType(instruction.getValue()), instruction.getRegObject());
         doPseudoMoveResult(instruction.getRegObject()); // Check-cast changes the source register's type
     }
 
 
     @Override
     public void visit(DexInstruction_InstanceOf instruction) {
         doThrowingCstInsn(Rops.INSTANCE_OF, makeCstType(instruction.getValue()), instruction.getRegObject());
         doPseudoMoveResult(instruction.getRegTo());
     }
 
 
     @Override
     public void visit(DexInstruction_ArrayLength instruction) {
         doThrowingInsn(Rops.ARRAY_LENGTH, instruction.getRegArray());
         doPseudoMoveResult(instruction.getRegTo());
     }
 
 
     @Override
     public void visit(DexInstruction_NewInstance instruction) {
         doThrowingCstInsn(Rops.NEW_INSTANCE, makeCstType(instruction.getTypeDef().getType()));
         doPseudoMoveResult(instruction.getRegTo());
     }
 
 
     @Override
     public void visit(DexInstruction_NewArray instruction) {
         DexArrayType arrayType = instruction.getValue();
         doThrowingCstInsn(Rops.opNewArray(Type.intern(arrayType.getDescriptor())),
                           makeCstType(arrayType), instruction.getRegSize());
         doPseudoMoveResult(instruction.getRegTo());
     }
 
 
     @Override
     public void visit(DexInstruction_FilledNewArray instruction) {
         DexArrayType arrayType = instruction.getArrayType();
         assert arrayType.getDescriptor().equals("[I");
 
         int arrayLen = instruction.getArgumentRegisters().size();
         DexRegister[] parameters = instruction.getArgumentRegisters().toArray(new DexRegister[arrayLen]);
 
         Rop opcode = Rops.opFilledNewArray(Type.INT_ARRAY, arrayLen);
         doThrowingCstInsn(opcode, CstType.INT_ARRAY, parameters);
 
         /*
         DexArrayType arrayType = instruction.getArrayType();
         Type elementType = Type.intern(arrayType.getElementType().getDescriptor());
         int arrayLen = instruction.getArgumentRegisters().size();
 
         DexRegister tmp0 = DexRegisterHelper.getTempRegister(0);
         RegisterSpec tmp0Spec = RegisterSpec.make(DexRegisterHelper.normalize(tmp0), Type.INT);
 
         // add Instruction manually because type information is not available in analyzer
         result.addInstruction(new PlainCstInsn(Rops.CONST_INT,
         		SourcePosition.NO_INFO,
         		tmp0Spec,
         		RegisterSpecList.EMPTY,
         		makeCstInteger(arrayLen)));
         Rop opcode = Rops.opNewArray(Type.intern(arrayType.getDescriptor()));
         result.addInstruction(new ThrowingCstInsn(opcode,
         		SourcePosition.NO_INFO,
         		RegisterSpecList.make(tmp0Spec),
         		getCaughtExceptions(getCatchers(opcode)),
         		makeCstType(arrayType)));
 
         // add array assignments to primary successor basic blocks
         AnalyzedDexInstruction primSuccessor = curInst.getOnlySuccesor();
         DexRegister dstReg = primSuccessor.getDestinationRegister();
         RegisterSpec dstRegSpec = RegisterSpec.make(DexRegisterHelper.normalize(dstReg), Type.intern(arrayType.getDescriptor()));
 
         opcode = Rops.opAput(Type.intern(elementType.getDescriptor()));
         TypeList exceptionList = getCaughtExceptions(getCatchers(opcode));
 
         for(int i=0; i<arrayLen; i++) {
         	result.addAuxInstruction(new PlainCstInsn(Rops.CONST_INT,
         			SourcePosition.NO_INFO,
         			tmp0Spec,
         			RegisterSpecList.EMPTY,
         			makeCstInteger(i)));
         	result.addAuxInstruction(new ThrowingInsn(opcode,
         			SourcePosition.NO_INFO,
         			RegisterSpecList.make(
         					RegisterSpec.make(DexRegisterHelper.normalize(instruction.getArgumentRegisters().get(i)), elementType),
         					dstRegSpec,
         					tmp0Spec),
         					exceptionList));
         }
         */
     }
 
 
     @Override
     public void visit(DexInstruction_FillArrayData instruction) {
 
         Type arrayElementType = getSourceRegSpec(instruction.getRegArray()).getType().getComponentType();
         Constant arrayType = null;
         ArrayList<Constant> values = new ArrayList<Constant>();
         for(byte[] element : instruction.getElementData()) {
             long v = 0;
             for(int i=0; i<element.length; i++)
                 v |= ((long)(element[i]&0xFF)) << (i*8);
 
             if (arrayElementType == Type.BYTE) {
                 assert element.length == 1;
                 values.add(CstByte.make((byte)v));
                 arrayType = CstType.BYTE_ARRAY;
             } else if (arrayElementType == Type.BOOLEAN) {
                 assert element.length == 1;
                 values.add(CstBoolean.make((int)v));
                 arrayType = CstType.BYTE_ARRAY;
             } else if (arrayElementType == Type.SHORT) {
                 assert element.length == 2;
                 values.add(CstShort.make((short)v));
                 arrayType = CstType.SHORT_ARRAY;
             } else if (arrayElementType == Type.CHAR) {
                 assert element.length == 2;
                 values.add(CstChar.make((char)v));
                 arrayType = CstType.SHORT_ARRAY;
             } else if (arrayElementType == Type.INT) {
                 assert element.length == 4;
                 values.add(CstInteger.make((int)v));
                 arrayType = CstType.INT_ARRAY;
             } else if (arrayElementType == Type.FLOAT) {
                 assert element.length == 4;
                 values.add(CstFloat.make((int)v));
                 arrayType = CstType.INT_ARRAY;
             } else if (arrayElementType == Type.LONG) {
                 assert element.length == 8;
                 values.add(CstLong.make(v));
                 arrayType = CstType.LONG_ARRAY;
             } else if (arrayElementType == Type.DOUBLE) {
                 assert element.length == 8;
                 values.add(CstDouble.make(v));
                 arrayType = CstType.LONG_ARRAY;
             } else {
                 throw new IllegalArgumentException("Unexpected constant type");
             }
         }
 
         result.addInstruction(new FillArrayDataInsn(Rops.FILL_ARRAY_DATA, SourcePosition.NO_INFO,
                               makeOperands(instruction.getRegArray()), values, arrayType));
         // Rops.FILL_ARRAY_DATA claims it is non-throwing, but experiments suggest otherwise.
         doThrowingSuccessors(getCatchers(null));
     }
 
 
     @Override
     public void visit(DexInstruction_Throw instruction) {
         doThrowingInsn(Rops.THROW, instruction.getRegFrom());
     }
 
 
     @Override
     public void visit(DexInstruction_Goto instruction) {
         doPlainInsn(Rops.GOTO, null);
     }
 
 
     @Override
     public void visit(DexInstruction_Switch instruction) {
         IntList caseList = new IntList();
         List<DexLabel> targetList;
 
         // Note: targetList may have duplicated entries.
         targetList = new ArrayList<DexLabel>();
         for(Pair<Integer, DexLabel> pair : instruction.getSwitchTable()) {
             caseList.add(pair.getValA());
             targetList.add(pair.getValB());
         }
 
         AnalyzedDexInstruction defaultSuccessor = analyzer.reverseLookup(instructionList.getNextInstruction(instruction));
 
         // Overwrite result.successor here, according to the requirement of Rops.SWITCH
         result.successors.clear();
         for(int i=0; i < targetList.size(); i++)
             result.addSuccessor(analyzer.reverseLookup(targetList.get(i)));
         result.addSuccessor(defaultSuccessor);
         result.setPrimarySuccessor(defaultSuccessor);
 
         result.addInstruction(new SwitchInsn(Rops.SWITCH, SourcePosition.NO_INFO, null, makeOperands(instruction.getRegTest()), caseList));
     }
 
 
     @Override
     public void visit(DexInstruction_Compare instruction) {
         Rop opcode;
         switch(instruction.getOpcode()) {
         case CmpLong:
             opcode = Rops.CMPL_LONG;
             break;
         case CmpgDouble:
             opcode = Rops.CMPG_DOUBLE;
             break;
         case CmplDouble:
             opcode = Rops.CMPL_DOUBLE;
             break;
         case CmpgFloat:
             opcode = Rops.CMPG_FLOAT;
             break;
         case CmplFloat:
             opcode = Rops.CMPL_FLOAT;
             break;
         default:
             throw new RuntimeException("Unknown cmp opcode");
         }
         doPlainInsn(opcode, getDestRegSpec(instruction.getRegTo()), instruction.getRegSourceA(), instruction.getRegSourceB());
     }
 
 
     @Override
     public void visit(DexInstruction_IfTest instruction) {
         RegisterSpecList operands = makeOperands(instruction.getRegA(), instruction.getRegB());
         Rop opcode = null;
         switch(instruction.getInsnOpcode()) {
         case eq:
             opcode = Rops.opIfEq(operands);
             break;
         case ge:
             opcode = Rops.opIfGe(operands);
             break;
         case gt:
             opcode = Rops.opIfGt(operands);
             break;
         case le:
             opcode = Rops.opIfLe(operands);
             break;
         case lt:
             opcode = Rops.opIfLt(operands);
             break;
         case ne:
             opcode = Rops.opIfNe(operands);
             break;
         }
         doPlainInsn(opcode, null, instruction.getRegA(), instruction.getRegB());
         doIfSuccessors(instruction.getTarget());
     }
 
 
     @Override
     public void visit(DexInstruction_IfTestZero instruction) {
         RegisterSpecList operands = makeOperands(instruction.getReg());
         Rop opcode = null;
         switch(instruction.getInsnOpcode()) {
         case eqz:
             opcode = Rops.opIfEq(operands);
             break;
         case gez:
             opcode = Rops.opIfGe(operands);
             break;
         case gtz:
             opcode = Rops.opIfGt(operands);
             break;
         case lez:
             opcode = Rops.opIfLe(operands);
             break;
         case ltz:
             opcode = Rops.opIfLt(operands);
             break;
         case nez:
             opcode = Rops.opIfNe(operands);
             break;
         }
         doPlainInsn(opcode, null, instruction.getReg());
         doIfSuccessors(instruction.getTarget());
     }
 
 
     private void doAget(DexRegister to, DexRegister array, DexRegister index) {
         doThrowingInsn(Rops.opAget(getDestRegSpec(to)), array, index);
         doPseudoMoveResult(to);
     }
     @Override
     public void visit(DexInstruction_ArrayGet instruction) {
         doAget(instruction.getRegTo(), instruction.getRegArray(), instruction.getRegIndex());
     }
 
 
     private void doAput(DexRegister src, DexRegister array, DexRegister index) {
         doThrowingInsn(Rops.opAput(getSourceRegSpec(src)), src, array, index);
     }
     @Override
     public void visit(DexInstruction_ArrayPut instruction) {
         doAput(instruction.getRegFrom(), instruction.getRegArray(), instruction.getRegIndex());
     }
 
 
     private void doIget(DexRegister to, DexRegister object, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
         RegisterSpec dst = getDestRegSpec(to);
         Constant fieldRef = makeFieldRef(fieldClass, fieldType, fieldName);
         doThrowingCstInsn(Rops.opGetField(dst), fieldRef, object);
         doPseudoMoveResult(to);
     }
 
     @Override
     public void visit(DexInstruction_InstanceGet instruction) {
         InstanceFieldDefinition field = instruction.getFieldDef();
         doIget(instruction.getRegTo(), instruction.getRegObject(),
                field.getParentClass().getType(),
                field.getFieldId().getType(),
                field.getFieldId().getName());
     }
 
 
     private void doIput(DexRegister from, DexRegister object, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
         RegisterSpec src = getSourceRegSpec(from);
         Constant fieldRef = makeFieldRef(fieldClass, fieldType, fieldName);
         doThrowingCstInsn(Rops.opPutField(src), fieldRef, from, object);
     }
     @Override
     public void visit(DexInstruction_InstancePut instruction) {
         InstanceFieldDefinition field = instruction.getFieldDef();
         doIput(instruction.getRegFrom(), instruction.getRegObject(),
                field.getParentClass().getType(),
                field.getFieldId().getType(),
                field.getFieldId().getName());
     }
 
 
     private void doSget(DexRegister to,  DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
         RegisterSpec dst = getDestRegSpec(to);
         Constant fieldRef = makeFieldRef(fieldClass, fieldType, fieldName);
         doThrowingCstInsn(Rops.opGetStatic(dst), fieldRef);
         doPseudoMoveResult(to);
     }
     @Override
     public void visit(DexInstruction_StaticGet instruction) {
         StaticFieldDefinition field = instruction.getFieldDef();
         doSget(instruction.getRegTo(),
                field.getParentClass().getType(),
                field.getFieldId().getType(),
                field.getFieldId().getName());
     }
 
 
     private void doSput(DexRegister from, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
         RegisterSpec src = getSourceRegSpec(from);
         Constant fieldRef = makeFieldRef(fieldClass, fieldType, fieldName);
         doThrowingCstInsn(Rops.opPutStatic(src), fieldRef, from);
     }
     @Override
     public void visit(DexInstruction_StaticPut instruction) {
         StaticFieldDefinition field = instruction.getFieldDef();
         doSput(instruction.getRegFrom(),
                field.getParentClass().getType(),
                field.getFieldId().getType(),
                field.getFieldId().getName());
     }
 
 
     @Override
     public void visit(DexInstruction_Invoke instruction) {
         Rop opcode = null;
         List<DexRegister> arguments = instruction.getArgumentRegisters();
 
         DexRegister[] operands_array = arguments.toArray(new DexRegister[arguments.size()]);
         RegisterSpecList operands = makeOperands(operands_array);
 
         switch(instruction.getCallType()) {
         case Direct:
             opcode = new Rop(RegOps.INVOKE_DIRECT, operands, StdTypeList.THROWABLE);
             break;
         case Interface:
             opcode = new Rop(RegOps.INVOKE_INTERFACE, operands, StdTypeList.THROWABLE);
             break;
         case Static:
             opcode = new Rop(RegOps.INVOKE_STATIC, operands, StdTypeList.THROWABLE);
             break;
         case Super:
             opcode = new Rop(RegOps.INVOKE_SUPER, operands, StdTypeList.THROWABLE);
             break;
         case Virtual:
             opcode = new Rop(RegOps.INVOKE_VIRTUAL, operands, StdTypeList.THROWABLE);
             break;
         }
         doThrowingCstInsn(opcode,
                           makeMethodRef(instruction.getClassType(),
                                         instruction.getMethodId().getName(),
                                         instruction.getMethodId().getPrototype()),
                           operands_array);
     }
 
 
     @Override
     public void visit(DexInstruction_UnaryOp instruction) {
         Rop opcode = null;
         switch( instruction.getInsnOpcode()) {
         case NegFloat:
             opcode = Rops.NEG_FLOAT;
             break;
         case NegInt:
             opcode = Rops.NEG_INT;
             break;
         case NotInt:
             opcode = Rops.NOT_INT;
             break;
         case NegDouble:
             opcode = Rops.NEG_DOUBLE;
             break;
         case NegLong:
             opcode = Rops.NEG_LONG;
             break;
         case NotLong:
             opcode = Rops.NOT_LONG;
             break;
         default:
             break;
         }
         doPlainInsn(opcode, getDestRegSpec(instruction.getRegTo()), instruction.getRegFrom());
     }
 
 
     private void doConvert(DexRegister dst, DexRegister src) {
         TypeBearer sourceType = getSourceRegSpec(src);
         TypeBearer targetType = getDestRegSpec(dst);
         Rop opcode = null;
         if (sourceType.getBasicFrameType() == BT_INT) {
             switch (targetType.getBasicType()) {
             case BT_SHORT:
                 opcode = Rops.TO_SHORT;
                 break;
             case BT_CHAR:
                 opcode = Rops.TO_CHAR;
                 break;
             case BT_BYTE:
                 opcode = Rops.TO_BYTE;
                 break;
             }
         }
         if (opcode == null)
             opcode = Rops.opConv(targetType, sourceType);
 
         doPlainInsn(opcode, getDestRegSpec(dst), src);
     }
 
     @Override
     public void visit(DexInstruction_Convert instruction) {
         doConvert(instruction.getRegTo(), instruction.getRegFrom());
     }
 
 
     @Override
     public void visit(DexInstruction_BinaryOp instruction) {
         Rop opcode = null;
         switch(instruction.getInsnOpcode()) {
         case AddFloat:
             opcode = Rops.ADD_FLOAT;
             break;
         case AddInt:
             opcode = Rops.ADD_INT;
             break;
         case AndInt:
             opcode = Rops.AND_INT;
             break;
         case DivFloat:
             opcode = Rops.DIV_FLOAT;
             break;
         case DivInt:
             opcode = Rops.DIV_INT;
             break;
         case MulFloat:
             opcode = Rops.MUL_FLOAT;
             break;
         case MulInt:
             opcode = Rops.MUL_INT;
             break;
         case OrInt:
             opcode = Rops.OR_INT;
             break;
         case RemFloat:
             opcode = Rops.REM_FLOAT;
             break;
         case RemInt:
             opcode = Rops.REM_INT;
             break;
         case ShlInt:
             opcode = Rops.SHL_INT;
             break;
         case ShrInt:
             opcode = Rops.SHR_INT;
             break;
         case SubFloat:
             opcode = Rops.SUB_FLOAT;
             break;
         case SubInt:
             opcode = Rops.SUB_INT;
             break;
         case UshrInt:
             opcode = Rops.USHR_INT;
             break;
         case XorInt:
             opcode = Rops.XOR_INT;
             break;
         case AddDouble:
             opcode = Rops.ADD_DOUBLE;
             break;
         case AddLong:
             opcode = Rops.ADD_LONG;
             break;
         case AndLong:
             opcode = Rops.AND_LONG;
             break;
         case DivDouble:
             opcode = Rops.DIV_DOUBLE;
             break;
         case DivLong:
             opcode = Rops.DIV_LONG;
             break;
         case MulDouble:
             opcode = Rops.MUL_DOUBLE;
             break;
         case MulLong:
             opcode = Rops.MUL_LONG;
             break;
         case OrLong:
             opcode = Rops.OR_LONG;
             break;
         case RemDouble:
             opcode = Rops.REM_DOUBLE;
             break;
         case RemLong:
             opcode = Rops.REM_LONG;
             break;
         case ShlLong:
             opcode = Rops.SHL_LONG;
             break;
         case ShrLong:
             opcode = Rops.SHR_LONG;
             break;
         case SubDouble:
             opcode = Rops.SUB_DOUBLE;
             break;
         case SubLong:
             opcode = Rops.SUB_LONG;
             break;
         case UshrLong:
             opcode = Rops.USHR_LONG;
             break;
         case XorLong:
             opcode = Rops.XOR_LONG;
             break;
         default:
             assert false;
             break;
         }
         if (opcode.getBranchingness() == Rop.BRANCH_NONE) {
             doPlainInsn(opcode, getDestRegSpec(instruction.getRegTo()), instruction.getRegArgA(), instruction.getRegArgB());
         } else { // Integer division/reminder will throw exception
             doThrowingInsn(opcode, instruction.getRegArgA(), instruction.getRegArgB());
             doPseudoMoveResult(instruction.getRegTo());
         }
     }
 
 
     @Override
     public void visit(DexInstruction_BinaryOpLiteral instruction) {
         Rop opcode = null;
         switch(instruction.getInsnOpcode()) {
         case Add:
             opcode = Rops.ADD_CONST_INT; // Rop allow CONST operation on long/double/float register, but DexInstruction does not.
             break;
         case And:
             opcode = Rops.AND_CONST_INT;
             break;
         case Div:
             opcode = Rops.DIV_CONST_INT;
             break;
         case Mul:
             opcode = Rops.MUL_CONST_INT;
             break;
         case Or:
             opcode = Rops.OR_CONST_INT;
             break;
         case Rem:
             opcode = Rops.REM_CONST_INT;
             break;
         case Rsub:
             opcode = Rops.SUB_CONST_INT; // SUB_CONST_INT already has the semantic of reverse sub. so no further patch up necessary.
             break;
         case Shl:
             opcode = Rops.SHL_CONST_INT;
             break;
         case Shr:
             opcode = Rops.SHR_CONST_INT;
             break;
         case Ushr:
             opcode = Rops.USHR_CONST_INT;
             break;
         case Xor:
             opcode = Rops.XOR_CONST_INT;
             break;
         }
 
         if (opcode.getBranchingness() == Rop.BRANCH_NONE) {
 
             doPlainCstInsn(opcode, getDestRegSpec(instruction.getRegTo()), makeCstInteger((int)instruction.getArgB()), instruction.getRegArgA());
 
         } else { // Integer division/reminder will throw exception
             doThrowingCstInsn(opcode, makeCstInteger((int)instruction.getArgB()), instruction.getRegArgA());
             doPseudoMoveResult(instruction.getRegTo());
         }
     }
 
 
     @Override
     public void visit(DexInstruction_Unknown instruction) {
         // should not happen
         assert false;
     }
 }
