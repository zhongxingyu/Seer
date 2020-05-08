 package uk.ac.cam.db538.dexter.dex.code;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import lombok.val;
 
 import org.jf.dexlib.CodeItem;
 import org.jf.dexlib.CodeItem.TryItem;
 import org.jf.dexlib.Code.Instruction;
 
 import uk.ac.cam.db538.dexter.dex.code.DexCode.Parameter;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexTryEnd;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexTryStart;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
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
 import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParseError;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexStandardRegister;
 import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public abstract class CodeParser {
 
 	public static DexCode parse(MethodDefinition methodDef, CodeItem codeItem, RuntimeHierarchy hierarchy) {
 		val parserCache = new CodeParserState(codeItem, hierarchy);
 		
 		val parsedInstructions = parseInstructions(codeItem, parserCache);
 		val parsedTryBlocks = parseTryBlocks(codeItem.getTries(), parserCache);
 		val parsedTryStarts = parsedTryBlocks.getValA();
 		val parsedTryEnds = parsedTryBlocks.getValB();
 		val parsedLabels = parserCache.getListOfLabels();
 		val parsedCatches = parserCache.getListOfCatches();
 		val parsedCatchAlls = parserCache.getListOfCatchAlls();
 		
 		val instructionList = finalizeCode(parsedInstructions, parsedTryStarts, parsedTryEnds, parsedLabels, parsedCatches, parsedCatchAlls);
 		val params = parseParameters(methodDef, codeItem, parserCache);
 		
 		return new DexCode(instructionList, params, hierarchy);
 	}
 	
 	private static List<Parameter> parseParameters(MethodDefinition methodDef, CodeItem codeItem, CodeParserState parserCache) {
 		val definingClass = methodDef.getParentClass().getType();
 		val isStatic = methodDef.isStatic();
 		val prototype = methodDef.getMethodId().getPrototype();
 		
 		val paramCount = prototype.getParameterCount(isStatic);
 		val paramRegCount = prototype.countParamWords(isStatic);
 		val regCount = codeItem.getRegisterCount();
 		
 		val params = new ArrayList<Parameter>(paramCount);
		int regId = regCount - paramRegCount;
 		
 		for (int i = 0; i < paramCount; i++) {
 			val paramType = prototype.getParameterType(i, isStatic, definingClass);
 			val paramWidth = paramType.getTypeWidth(); 
 			
 			DexStandardRegister paramReg;
 			if (paramWidth == RegisterWidth.SINGLE)
 				paramReg = parserCache.getSingleRegister(regId);
 			else
 				paramReg = parserCache.getWideRegister(regId);
 			
 			regId += paramWidth.getRegisterCount();
 			
 			params.add(new Parameter(paramType, paramReg));
 		}
 		
 		return params;
 	}
 	
 	private static FragmentList<DexInstruction> parseInstructions(CodeItem codeItem, CodeParserState parserCache) {
 		val dexlibInstructions = codeItem.getInstructions();
 		val parsedCode = new FragmentList<DexInstruction>();
 		
 		long insnOffset = 0L;
 		for (val insn : dexlibInstructions) {
 			val parsedInsn = parseInstruction(insn, parserCache);
 			if (parsedInsn != null)
 				parsedCode.add(new Fragment<DexInstruction>(insnOffset, parsedInsn));
 			insnOffset += insn.getSize(0);
 		}
 		
 		return parsedCode;
 	}
 	
 	private static Pair<FragmentList<DexTryStart>, FragmentList<DexTryEnd>> parseTryBlocks(TryItem[] tryItems, CodeParserState parserCache) {
 		if (tryItems == null)
 			return Pair.create(
 					new FragmentList<DexTryStart>(),
 					new FragmentList<DexTryEnd>());
 		
 		val parsedTryStarts = new FragmentList<DexTryStart>();
 		val parsedTryEnds = new FragmentList<DexTryEnd>();
 		int counter = 1;
 		
 		for (val tryItem : tryItems) {
 
 			// start/end offsets
 			final long startOffset = tryItem.getStartCodeAddress();
 			final long endOffset = startOffset + tryItem.getTryLength();
 			
 			// CatchAll handler (null if not set)
 			val catchallOffset = tryItem.encodedCatchHandler.getCatchAllHandlerAddress();
 			DexCatchAll catchallHandler;
 			if (catchallOffset < 0)
 				catchallHandler = null;
 			else
 				catchallHandler = parserCache.getCatchAll(catchallOffset);
 			
 			// other Catch handlers
 			val encodedHandlers = tryItem.encodedCatchHandler.handlers;
 			val catchHandlers = new ArrayList<DexCatch>(encodedHandlers.length);
 			for (val encodedHandler : encodedHandlers) {
 				val catchOffset = encodedHandler.getHandlerAddress();
 				val catchType = DexClassType.parse(encodedHandler.exceptionType.getTypeDescriptor(), parserCache.getHierarchy().getTypeCache());
 				catchHandlers.add(parserCache.getCatch(catchOffset, catchType));
 			}
 			
 			// create TryStart and TryEnd
 			val tryEnd = new DexTryEnd(counter);
 			val tryStart = new DexTryStart(counter, tryEnd, catchallHandler, catchHandlers);
 			counter++;
 			
 			// add them to the fragments list
 			parsedTryStarts.add(new Fragment<DexTryStart>(startOffset, tryStart));
 			parsedTryEnds.add(new Fragment<DexTryEnd>(endOffset, tryEnd));
 		}
 		
 		return Pair.create(parsedTryStarts, parsedTryEnds);
 	}
 	
 	private static InstructionList finalizeCode(FragmentList<DexInstruction> instructions, 
 												FragmentList<DexTryStart> tryStarts,
 												FragmentList<DexTryEnd> tryEnds,
 												FragmentList<DexLabel> labels, 
 												FragmentList<DexCatch> catches, 
 												FragmentList<DexCatchAll> catchAlls) {
 
 		val finalInstructionList = new ArrayList<DexCodeElement>(instructions.size() + tryStarts.size() + tryEnds.size() + labels.size() + catches.size() + catchAlls.size());
 		
 		// Ordering is important! Reflects the desired order of combining the fragment lists
 		val nonInstructionFragments = new FragmentList<?>[] { tryEnds, catches, catchAlls, labels, tryStarts }; 
 
 		// sort everything by the absolute offset address
 		instructions.sortFragments();
 		tryStarts.sortFragments();
 		tryEnds.sortFragments();
 		labels.sortFragments();
 		catches.sortFragments();
 		catchAlls.sortFragments();
 		
 		while (!instructions.isEmpty()) {
 			val headInsn = instructions.pop();
 			val offset = headInsn.getAbsoluteOffset();
 			
 			// Check that markers don't have a lower offset than the current instruction
 			// If they do, the file is inconsistent (markers can only be placed at
 			// positions with instructions, not between).
 			for (val fragList : nonInstructionFragments) {
 				if (!fragList.isEmpty() && fragList.peek().getAbsoluteOffset() < offset) {
 					System.err.println("INSTRUCTIONS");
 					instructions.dump();
 					System.err.println("TRY STARTS");
 					tryStarts.dump();
 					System.err.println("TRY ENDS");
 					tryEnds.dump();
 					System.err.println("LABELS");
 					labels.dump();
 					System.err.println("CATCHES");
 					catches.dump();
 					System.err.println("CATCH ALLS");
 					catchAlls.dump();
 					throw new InstructionParseError("A " + fragList.peek().getValB().getClass().getSimpleName() + " marker is defined between instructions");
 				}
 			}
 			
 			// Place them in the final instruction list. Order of combining is given by the definition of the nonInstructionFragments array
 			for (val fragList : nonInstructionFragments) {
 				while (!fragList.isEmpty() && fragList.peek().getAbsoluteOffset() == offset) {
 					val fragment = fragList.pop();
 					finalInstructionList.add(fragment.getElement());
 				}
 			}
 			
 			// Add the instruction into the instruction list
 			finalInstructionList.add(headInsn.getElement());
 		}
 		
 		return new InstructionList(finalInstructionList);
 	}
 
 	  private static DexInstruction parseInstruction(Instruction insn, CodeParserState parsingCache) {
 		    switch (insn.opcode) {
 
 		    case NOP:
 		    	return null;
 		    
 		    case MOVE:
 		    case MOVE_OBJECT:
 		    case MOVE_FROM16:
 		    case MOVE_OBJECT_FROM16:
 		    case MOVE_16:
 		    case MOVE_OBJECT_16:
 		    case MOVE_WIDE:
 		    case MOVE_WIDE_FROM16:
 		    case MOVE_WIDE_16:
 		      return DexInstruction_Move.parse(insn, parsingCache);
 
 		    case MOVE_RESULT:
 		    case MOVE_RESULT_OBJECT:
 		    case MOVE_RESULT_WIDE:
 		      return DexInstruction_MoveResult.parse(insn, parsingCache);
 
 		    case MOVE_EXCEPTION:
 		      return DexInstruction_MoveException.parse(insn, parsingCache);
 
 		    case RETURN_VOID:
 		      return DexInstruction_ReturnVoid.parse(insn, parsingCache);
 
 		    case RETURN:
 		    case RETURN_OBJECT:
 		    case RETURN_WIDE:
 		      return DexInstruction_Return.parse(insn, parsingCache);
 
 		    case CONST_4:
 		    case CONST_16:
 		    case CONST:
 		    case CONST_HIGH16:
 		    case CONST_WIDE_16:
 		    case CONST_WIDE_32:
 		    case CONST_WIDE:
 		    case CONST_WIDE_HIGH16:
 		      return DexInstruction_Const.parse(insn, parsingCache);
 
 		    case CONST_STRING:
 		    case CONST_STRING_JUMBO:
 		      return DexInstruction_ConstString.parse(insn, parsingCache);
 
 		    case CONST_CLASS:
 		      return DexInstruction_ConstClass.parse(insn, parsingCache);
 
 		    case MONITOR_ENTER:
 		    case MONITOR_EXIT:
 		      return DexInstruction_Monitor.parse(insn, parsingCache);
 
 		    case CHECK_CAST:
 		      return DexInstruction_CheckCast.parse(insn, parsingCache);
 
 		    case INSTANCE_OF:
 		      return DexInstruction_InstanceOf.parse(insn, parsingCache);
 
 		    case NEW_INSTANCE:
 		      return DexInstruction_NewInstance.parse(insn, parsingCache);
 
 		    case NEW_ARRAY:
 		      return DexInstruction_NewArray.parse(insn, parsingCache);
 
 		    case ARRAY_LENGTH:
 		      return DexInstruction_ArrayLength.parse(insn, parsingCache);
 
 		    case THROW:
 		      return DexInstruction_Throw.parse(insn, parsingCache);
 
 		    case GOTO:
 		    case GOTO_16:
 		    case GOTO_32:
 		      return DexInstruction_Goto.parse(insn, parsingCache);
 
 		    case INVOKE_VIRTUAL:
 		    case INVOKE_SUPER:
 		    case INVOKE_DIRECT:
 		    case INVOKE_STATIC:
 		    case INVOKE_INTERFACE:
 		    case INVOKE_VIRTUAL_RANGE:
 		    case INVOKE_SUPER_RANGE:
 		    case INVOKE_DIRECT_RANGE:
 		    case INVOKE_STATIC_RANGE:
 		    case INVOKE_INTERFACE_RANGE:
 		      return DexInstruction_Invoke.parse(insn, parsingCache);
 		      
 		    case FILLED_NEW_ARRAY:
 		    case FILLED_NEW_ARRAY_RANGE:
 		      return DexInstruction_FilledNewArray.parse(insn, parsingCache);
 		      
 		    case IF_EQ:
 		    case IF_NE:
 		    case IF_LT:
 		    case IF_GE:
 		    case IF_GT:
 		    case IF_LE:
 		      return DexInstruction_IfTest.parse(insn, parsingCache);
 
 		    case IF_EQZ:
 		    case IF_NEZ:
 		    case IF_LTZ:
 		    case IF_GEZ:
 		    case IF_GTZ:
 		    case IF_LEZ:
 		      return DexInstruction_IfTestZero.parse(insn, parsingCache);
 
 		    case CMPL_FLOAT:
 		    case CMPG_FLOAT:
 		    case CMPL_DOUBLE:
 		    case CMPG_DOUBLE:
 		    case CMP_LONG:
 		      return DexInstruction_Compare.parse(insn, parsingCache);
 
 		    case SGET:
 		    case SGET_OBJECT:
 		    case SGET_BOOLEAN:
 		    case SGET_BYTE:
 		    case SGET_CHAR:
 		    case SGET_SHORT:
 		    case SGET_WIDE:
 		      return DexInstruction_StaticGet.parse(insn, parsingCache);
 
 		    case SPUT:
 		    case SPUT_OBJECT:
 		    case SPUT_BOOLEAN:
 		    case SPUT_BYTE:
 		    case SPUT_CHAR:
 		    case SPUT_SHORT:
 		    case SPUT_WIDE:
 		      return DexInstruction_StaticPut.parse(insn, parsingCache);
 
 		    case IGET:
 		    case IGET_OBJECT:
 		    case IGET_BOOLEAN:
 		    case IGET_BYTE:
 		    case IGET_CHAR:
 		    case IGET_SHORT:
 		    case IGET_WIDE:
 		      return DexInstruction_InstanceGet.parse(insn, parsingCache);
 
 		    case IPUT:
 		    case IPUT_OBJECT:
 		    case IPUT_BOOLEAN:
 		    case IPUT_BYTE:
 		    case IPUT_CHAR:
 		    case IPUT_SHORT:
 		    case IPUT_WIDE:
 		      return DexInstruction_InstancePut.parse(insn, parsingCache);
 
 		    case AGET:
 		    case AGET_OBJECT:
 		    case AGET_BOOLEAN:
 		    case AGET_BYTE:
 		    case AGET_CHAR:
 		    case AGET_SHORT:
 		    case AGET_WIDE:
 		      return DexInstruction_ArrayGet.parse(insn, parsingCache);
 
 		    case APUT:
 		    case APUT_OBJECT:
 		    case APUT_BOOLEAN:
 		    case APUT_BYTE:
 		    case APUT_CHAR:
 		    case APUT_SHORT:
 		    case APUT_WIDE:
 		      return DexInstruction_ArrayPut.parse(insn, parsingCache);
 
 		    case NEG_INT:
 		    case NOT_INT:
 		    case NEG_FLOAT:
 		    case NEG_LONG:
 		    case NOT_LONG:
 		    case NEG_DOUBLE:
 		      return DexInstruction_UnaryOp.parse(insn, parsingCache);
 
 		    case INT_TO_FLOAT:
 		    case FLOAT_TO_INT:
 		    case INT_TO_BYTE:
 		    case INT_TO_CHAR:
 		    case INT_TO_SHORT:
 		    case INT_TO_LONG:
 		    case INT_TO_DOUBLE:
 		    case FLOAT_TO_LONG:
 		    case FLOAT_TO_DOUBLE:
 		    case LONG_TO_INT:
 		    case DOUBLE_TO_INT:
 		    case LONG_TO_FLOAT:
 		    case DOUBLE_TO_FLOAT:
 		    case LONG_TO_DOUBLE:
 		    case DOUBLE_TO_LONG:
 		      return DexInstruction_Convert.parse(insn, parsingCache);
 
 		    case ADD_INT:
 		    case SUB_INT:
 		    case MUL_INT:
 		    case DIV_INT:
 		    case REM_INT:
 		    case AND_INT:
 		    case OR_INT:
 		    case XOR_INT:
 		    case SHL_INT:
 		    case SHR_INT:
 		    case USHR_INT:
 		    case ADD_FLOAT:
 		    case SUB_FLOAT:
 		    case MUL_FLOAT:
 		    case DIV_FLOAT:
 		    case REM_FLOAT:
 		    case ADD_INT_2ADDR:
 		    case SUB_INT_2ADDR:
 		    case MUL_INT_2ADDR:
 		    case DIV_INT_2ADDR:
 		    case REM_INT_2ADDR:
 		    case AND_INT_2ADDR:
 		    case OR_INT_2ADDR:
 		    case XOR_INT_2ADDR:
 		    case SHL_INT_2ADDR:
 		    case SHR_INT_2ADDR:
 		    case USHR_INT_2ADDR:
 		    case ADD_FLOAT_2ADDR:
 		    case SUB_FLOAT_2ADDR:
 		    case MUL_FLOAT_2ADDR:
 		    case DIV_FLOAT_2ADDR:
 		    case REM_FLOAT_2ADDR:
 		    case ADD_LONG:
 		    case SUB_LONG:
 		    case MUL_LONG:
 		    case DIV_LONG:
 		    case REM_LONG:
 		    case AND_LONG:
 		    case OR_LONG:
 		    case XOR_LONG:
 		    case SHL_LONG:
 		    case SHR_LONG:
 		    case USHR_LONG:
 		    case ADD_DOUBLE:
 		    case SUB_DOUBLE:
 		    case MUL_DOUBLE:
 		    case DIV_DOUBLE:
 		    case REM_DOUBLE:
 		    case ADD_LONG_2ADDR:
 		    case SUB_LONG_2ADDR:
 		    case MUL_LONG_2ADDR:
 		    case DIV_LONG_2ADDR:
 		    case REM_LONG_2ADDR:
 		    case AND_LONG_2ADDR:
 		    case OR_LONG_2ADDR:
 		    case XOR_LONG_2ADDR:
 		    case SHL_LONG_2ADDR:
 		    case SHR_LONG_2ADDR:
 		    case USHR_LONG_2ADDR:
 		    case ADD_DOUBLE_2ADDR:
 		    case SUB_DOUBLE_2ADDR:
 		    case MUL_DOUBLE_2ADDR:
 		    case DIV_DOUBLE_2ADDR:
 		    case REM_DOUBLE_2ADDR:
 		      return DexInstruction_BinaryOp.parse(insn, parsingCache);
 
 		    case ADD_INT_LIT16:
 		    case ADD_INT_LIT8:
 		    case RSUB_INT:
 		    case RSUB_INT_LIT8:
 		    case MUL_INT_LIT16:
 		    case MUL_INT_LIT8:
 		    case DIV_INT_LIT16:
 		    case DIV_INT_LIT8:
 		    case REM_INT_LIT16:
 		    case REM_INT_LIT8:
 		    case AND_INT_LIT16:
 		    case AND_INT_LIT8:
 		    case OR_INT_LIT16:
 		    case OR_INT_LIT8:
 		    case XOR_INT_LIT16:
 		    case XOR_INT_LIT8:
 		    case SHL_INT_LIT8:
 		    case SHR_INT_LIT8:
 		    case USHR_INT_LIT8:
 		      return DexInstruction_BinaryOpLiteral.parse(insn, parsingCache);
 
 		    case FILL_ARRAY_DATA:
 		      return DexInstruction_FillArrayData.parse(insn, parsingCache);
 
 		    case PACKED_SWITCH:
 		    case SPARSE_SWITCH:
 		      return DexInstruction_Switch.parse(insn, parsingCache);
 
 		    default:
 		    	throw new InstructionParseError("Unknown instruction " + insn.opcode.name());
 		    }
 		  }
 
 		static class Fragment<T extends DexCodeElement> extends Pair<Long, T> implements Comparable<Fragment<T>> {
 			private static final long serialVersionUID = 1L;
 			
 			public Fragment(Long valA, T valB) {
 				super(valA, valB);
 			}
 
 			public long getAbsoluteOffset() {
 				return this.getValA();
 			}
 			
 			public T getElement() {
 				return this.getValB();
 			}
 
 			@Override
 			public int compareTo(Fragment<T> other) {
 				return Long.compare(this.getAbsoluteOffset(), other.getAbsoluteOffset()); 
 			}
 			
 			@Override
 			public String toString() {
 				return getValA().toString() + ": " + getValB().toString();
 			}
 		}
 		
 		static class FragmentList<T extends DexCodeElement> extends LinkedList<Fragment<T>> { 
 			private static final long serialVersionUID = 1L;
 
 			public void sortFragments() {
 				Collections.sort(this);
 			}
 			
 			public void dump() {
 				for (val fragment : this)
 					System.err.println(fragment.toString());
 			}
 		}
 }
