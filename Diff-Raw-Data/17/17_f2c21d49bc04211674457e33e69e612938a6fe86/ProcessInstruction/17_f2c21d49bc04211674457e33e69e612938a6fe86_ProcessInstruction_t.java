 /*
 Copyright (c) 2008 Joe Conti
 
 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation
 files (the "Software"), to deal in the Software without
 restriction, including without limitation the rights to use,
 copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following
 conditions:
 
 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 OTHER DEALINGS IN THE SOFTWARE.
 */
 
 package org.checkthread.parser.bcel;
 
 import java.util.*;
 
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.Constants;
 import org.checkthread.parser.*;
 import org.apache.bcel.verifier.structurals.*;
 import org.apache.bcel.generic.*;
 import org.apache.bcel.classfile.Utility;
 
 import org.checkthread.config.*;
 
 /**
  * Main class for handling Java byte
  * code instructions. This class contains
  * a big switch statement for delegating
  * the JVM spec byte codes.
  */
 public class ProcessInstruction {
 	
     private static boolean wide = false;
     
 	static void logInfo(String msg) {
 		Log.logByteInfo(msg);
 	}
 	
 	static void logLocking(String msg) {
 		Log.logByteInfo(msg);
 	}
 
 	static void processInstruction(
 			short opcode,
 			HashMap<Integer,Boolean> visitedByteOffsets,
 			Queue<BranchState> branchQueue,
 			BranchState currBranch,
 			HashMap<Integer,Type> runtimeVariableTable,
 		    JavaClass jClass,
 		    ArrayList<IClassFileParseHandler> handlerList,
 		    HashMap<String,java.lang.reflect.AccessibleObject> hashMapSynthetic,
 			Method parentMethod,
 			java.lang.reflect.AccessibleObject parentMethodR,
 			ByteReader bytes,
 			IMethodInfo parentMethodInfo)
 			throws Exception {
 
 		ConstantPool constant_pool = jClass.getConstantPool();
 		Code code = parentMethod.getCode();
 		boolean parentIsSynthetic = parentMethod.isSynthetic();
 		LineNumberTable lineTable = code.getLineNumberTable();
 		OperandStack stack = currBranch.getStack();
 		LockStack lockStack = currBranch.getLockInfo();
 		//ByteReader bytes = new ByteReader(code.getCode());
 		//bytes.mark(bytes.available());
 		//bytes.reset();
 		//bytes.skipBytes(currBranch.getIndex());
 		int currOffset = bytes.getIndex();
 	
 		//ToDo: get this from BCEL
 		boolean isStaticBlock = parentMethod.getName().equals(ByteCodeConstants.STATICBLOCK_IDENTIFIER);
 		
 		//logInfo("opcode: " + opcode + " ALOAD_1: " + Constants.ALOAD_1);
 		String name, signature;
 		String className = null;
 		int default_offset = 0, low, high;
 		int index, class_index, vindex, constant;
 		int[] jump_table;
 		int no_pad_bytes = 0, offset;
         Type t;
         BranchState branchState;
         
         // varTable may be null in some situations
 		LocalVariableTable varTable = code.getLocalVariableTable();
 		
 		int line_number_ind = bytes.getIndex();
 		int invokedLineNumber;
 		if (lineTable != null) {
 			invokedLineNumber = lineTable.getSourceLine(line_number_ind);
 		} else {
 			invokedLineNumber = org.checkthread.main.Constants.NO_LINE_NUMBER;
 		}
 		
 	    Log.logByteInfo("Byte Offset: " + bytes.getIndex() + " Stack size: " + stack.size());
 		/*
 		 * Special case: Skip (0-3) padding bytes, i.e., the following bytes are
 		 * 4-byte-aligned
 		 */
 		if ((opcode == Constants.TABLESWITCH)
 				|| (opcode == Constants.LOOKUPSWITCH)) {
 			int remainder = bytes.getIndex() % 4;
 			no_pad_bytes = (remainder == 0) ? 0 : 4 - remainder;
 			for (int i = 0; i < no_pad_bytes; i++) {
 				bytes.readByte();
 			}
 			// Both cases have a field default_offset in common
 			default_offset = bytes.readInt();
 		}
 
 		switch (opcode) {
 		
 		// TABLESWITCH - Switch within given range of values, i.e., low..high
 		case Constants.TABLESWITCH:
 			logInfo("TABLESWITCH");
 			stack.pop();
 			low = bytes.readInt();
 			high = bytes.readInt();
 			offset = bytes.getIndex() - 12 - no_pad_bytes - 1;
 			default_offset += offset;
 
 			// Print switch indices in first row (and default)
 			jump_table = new int[high - low + 1];
 			for (int i = 0; i < jump_table.length; i++) {
 				jump_table[i] = offset + bytes.readInt();
 			}
 
 			break;
 
 		// AALOAD - Load reference from array
 		// Stack: ..., arrayref, index -> value
 		case Constants.AALOAD:
 			logInfo("AALOAD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.NULL);
 			break;
 
 		// AASTORE - Store into reference array
         // Stack: ..., arrayref, index, value -> ...
 		case Constants.AASTORE:
 			logInfo("AASTORE, stack size: " + stack.size());
 			stack.pop(3);
 			break;
 			
 		// ACONST_NULL - Push null reference
         // Stack: ... -> ..., null
 		case Constants.ACONST_NULL:
 			logInfo("ACONST_NULL, stack size: " + stack.size());
 			stack.push(ObjectType.NULL);
 			break;
 	
 		// ALOAD - Load reference from local variable
 	    // Stack: ... -> ..., objectref
 		case Constants.ALOAD_0: 			
 		   wide = ProcessInstruction_Util.processALOAD(0,stack, wide, bytes, varTable, runtimeVariableTable);
 		   break;	
 		case Constants.ALOAD_1: 
 		   wide = ProcessInstruction_Util.processALOAD(1,stack, wide, bytes, varTable, runtimeVariableTable);
 		   break;		
 		case Constants.ALOAD_2: 			
 		   wide = ProcessInstruction_Util.processALOAD(2,stack, wide, bytes, varTable, runtimeVariableTable);
 		   break;	
 		case Constants.ALOAD_3: 
 		   wide = ProcessInstruction_Util.processALOAD(3,stack, wide, bytes, varTable, runtimeVariableTable);
 	       break;
 		case Constants.ALOAD: 
 		   wide = ProcessInstruction_Util.processALOADn(stack, wide, bytes, varTable, runtimeVariableTable);
            break;
 	           
 		// ANEWARRAY - Create new array of references
 		// Stack: ..., count -> ..., arrayref
 		case Constants.ANEWARRAY: {
 			logInfo("ANEWARRAY, stack size: " + stack.size());
 			bytes.readShort();
 			stack.pop();
 			t = new ArrayType(ObjectType.NULL,1);
 			stack.push(t);
 			break;
 		}
 		
 		// ARETURN - Return reference from method
 		// Stack: ..., objectref -> <empty>
 		case Constants.ARETURN:
 		   logInfo("ARETURN, stack size: " + stack.size());
 		   stack.pop(stack.size());
 		   break;	
 		
 		// ARRAYLENGTH - Get length of array
 		// Stack: ..., arrayref -> ..., length
 		case Constants.ARRAYLENGTH:
 			logInfo("ARRAYLENGTH, stack size: " + stack.size());
 			stack.pop();
 		    stack.push(ObjectType.INT);
 			break;
 	
 		// ASTORE - Store reference into local variable
         // Stack ..., objectref -> ... 
 		case Constants.ASTORE_0:
             wide = ProcessInstruction_Util.processASTORE(0,stack, wide, bytes, runtimeVariableTable);
 			break;
 		case Constants.ASTORE_1:
             wide = ProcessInstruction_Util.processASTORE(1,stack, wide, bytes, runtimeVariableTable);
 			break;		
 		case Constants.ASTORE_2:
             wide = ProcessInstruction_Util.processASTORE(2,stack, wide, bytes, runtimeVariableTable);
 			break;	
 		case Constants.ASTORE_3:
             wide = ProcessInstruction_Util.processASTORE(3,stack, wide, bytes, runtimeVariableTable);
 			break;
 		case Constants.ASTORE:
             wide = ProcessInstruction_Util.processASTOREn(stack, wide, bytes, runtimeVariableTable);
 			break;		
 			
 		// ATHROW - Throw exception
         // Stack: ..., objectref -> objectref	
 		case Constants.ATHROW:
 			logInfo("ATHROW, stack size: " + stack.size());
 			stack.pop(stack.size());
 			//stack.push(ObjectType.NULL);
 			break;
 
 		// BALOAD - Load byte or boolean from array
         // Stack: ..., arrayref, index -> ..., value
 		case Constants.BALOAD:
 			logInfo("BALOAD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 		
 		// BASTORE - Store into byte or boolean array
         // Stack: ..., arrayref, index, value -> ...
 		case Constants.BASTORE:
 			logInfo("BASTORE, stack size: " + stack.size());
 			stack.pop(3);
 			break;
 			
 		// BIPUSH - Push byte on stack
         // Stack: ... -> ..., value	
 		case Constants.BIPUSH:
 			logInfo("BIPUSH, stack size: " + stack.size());
 			bytes.readByte();
 			stack.push(ObjectType.INT);
 			break;
 			
 		// CALOAD - Load char from array
         // Stack: ..., arrayref, index -> ..., value
 		case Constants.CALOAD:
 			logInfo("CALOAD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 	
 		// CASTORE - Store into char array
         // Stack: ..., arrayref, index, value -> ...
 		case Constants.CASTORE:
 			logInfo("CASTORE, stack size: " + stack.size());
 			stack.pop(3);
 			break;
 		
 		// CHECKCAST - Check whether object is of given type
         // Stack: ..., objectref -> ..., objectref
 		case Constants.CHECKCAST:
 			logInfo("CHECKCAST, stack size: " + stack.size());
 			index = bytes.readShort();
 			break;			
 		
 		// D2F - Convert double to float
         // Stack: ..., value.word1, value.word2 -> ..., result
 		case Constants.D2F:
 			logInfo("D2F, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.FLOAT);
 			break;
 		
 		// D2I - Convert double to int
         // Stack: ..., value.word1, value.word2 -> ..., result	
 		case Constants.D2I:
 			logInfo("D2I, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.INT);
 			break;
 			
 		// D2L - Convert double to long
         // Stack: ..., value.word1, value.word2 -> ..., result.word1, result.word2	
 		case Constants.D2L:
 			logInfo("D2L, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.LONG);
 			break;
 		
 		// DADD - Add doubles
         // Stack: ..., value1.word1, value1.word2, value2.word1, value2.word2 ->
         // ..., result.word1, result1.word2
 		case Constants.DADD:
 			logInfo("DADD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.DOUBLE);
 			break;
 		
 		// DALOAD - Load double from array
         // Stack: ..., arrayref, index -> ..., result.word1, result.word2	
 		case Constants.DALOAD:
 			logInfo("DALOAD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.DOUBLE);
 			break;
 		
 		// DASTORE - Store into double array
         // Stack: ..., arrayref, index, value.word1, value.word2 -> ...
 		case Constants.DASTORE:
 			logInfo("DASTORE, stack size: " + stack.size());
 			stack.pop(3);
 			break;
 		
 		// DCMPG - Compare doubles: value1 > value2
         // Stack: ..., value1.word1, value1.word2, value2.word1, value2.word2 ->
         // ..., result	
 		case Constants.DCMPG:
 			logInfo("DCMPG, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 		
 		case Constants.DCMPL:
 			logInfo("DCMPL, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 		
 		// DCONST - Push 0.0 or 1.0, other values cause an exception
         // Stack: ... -> ..., 	
 		case Constants.DCONST_0:
 			logInfo("DCONST_0, stack size: " + stack.size());
 			stack.push(ObjectType.DOUBLE);
 		   break;
 		  
 		case Constants.DCONST_1:
 			logInfo("DCONST_1, stack size: " + stack.size());
 			stack.push(ObjectType.DOUBLE);
 			break;
 			
 		// ..., value1, value2  ..., result	
 		case Constants.DDIV:
 			logInfo("DDIV, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.DOUBLE);
 			break;
  
 		// DLOAD - Load double from local variable
 	    // Stack ... -> ..., result.word1, result.word2
 		case Constants.DLOAD_0:
 			wide = ProcessInstruction_Util.processDLOAD(0, stack, wide, bytes, varTable);	
 		    break;
 		case Constants.DLOAD_1:
 			wide = ProcessInstruction_Util.processDLOAD(1, stack, wide, bytes, varTable);	
 			break;
 		case Constants.DLOAD_2:
 		    wide = ProcessInstruction_Util.processDLOAD(2, stack, wide, bytes, varTable);	
 			break;
 		case Constants.DLOAD_3:
 		    wide = ProcessInstruction_Util.processDLOAD(3, stack, wide, bytes, varTable);	
 		    break;
 		case Constants.DLOAD: 
 			wide = ProcessInstruction_Util.processDLOADn(stack, wide, bytes, varTable);
 		    break;
 		    
 		//    ..., value1, value2  ..., result		    
 		case Constants.DMUL:
 			logInfo("DMUL, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.DOUBLE);
 			break;
 		
 		// ..., value  ..., result	
 		case Constants.DNEG:
 			logInfo("DNEG, stack size: " + stack.size());
 			stack.pop(1);
 			stack.push(ObjectType.DOUBLE);
 			break;
 		
 		// ..., value1, value2  ..., result
 		case Constants.DREM:
 			logInfo("DREM, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.DOUBLE);
 			break;
 			
 	    // ..., value  [empty]
 		case Constants.DRETURN:
 			logInfo("DRETURN, stack size: " + stack.size());
 			stack.pop(stack.size());
 			break;		
 	    
 		// DSTORE - Store double into local variable
 		// Stack: ..., value.word1, value.word2 -> ...
 		case Constants.DSTORE_0:
 			wide = ProcessInstruction_Util.processDSTORE(0, stack, wide, bytes);
 		    break;
 		case Constants.DSTORE_1:
 			wide = ProcessInstruction_Util.processDSTORE(1, stack, wide, bytes);
 		    break;
 		case Constants.DSTORE_2:
 			wide = ProcessInstruction_Util.processDSTORE(2, stack, wide, bytes);
 		    break;
 		case Constants.DSTORE_3:
 			wide = ProcessInstruction_Util.processDSTORE(3, stack, wide, bytes);			
 		    break;
 		case Constants.DSTORE: 
 			wide = ProcessInstruction_Util.processDSTOREn(stack, wide, bytes);
 			break;
 			
 		// ..., value1, value2  ..., result	
 		case Constants.DSUB:
 			logInfo("DSUB, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.DOUBLE);
 			break;
 					
 		// DUP - Duplicate top operand stack word
         // Stack: ..., word -> ..., word, word
 		case Constants.DUP: {
 			logInfo("DUP, stack size: " + stack.size());
 	        if(stack.size()>=stack.maxStack()) {
 	        	Log.severe("Stack filled up: " + parentMethodInfo.getFullUniqueMethodName());
 	        }
 			stack.push(stack.peek());
 			break;
 		}	
 			
 		// ..., value2, value1  ..., value1, value2, value1
 		case Constants.DUP_X1:
 			logInfo("DUP_X1, stack size: " + stack.size());
 			stack.push(stack.peek());
 			break;
 		    
 		// Form 1:
         //  ..., value3, value2, value1 ..., value1, value3, value2, value1
        // where value1, value2, and value3 are all values of a category 1 computational type.
         //
 	    // Form 2:
         // ..., value2, value1 ..., value1, value2, value1
        // where value1 is a value of a category 1 computational type and value2 is a value of a category 2 computational type.
 		case Constants.DUP_X2:
 			logInfo("DUP_X2, stack size: " + stack.size());
 		    t = stack.peek();
 		    
 		    // category 2
 		    if(t == ObjectType.DOUBLE || t == ObjectType.LONG) {
 		       stack.push(stack.peek());
 		    // category 1	
 		    } else {
 		       stack.push(stack.peek());
 		    }
 	
 		// Form 1:
         // ..., value2, value1 ..., value2, value1, value2, value1
        // where both value1 and value2 are values of a category 1 computational type.
         // Form 2:
         // ..., value ..., value, value
        // where value is a value of a category 2 computational type
 		case Constants.DUP2:
 			logInfo("DUP2, stack size: " + stack.size());
 		    t = stack.peek();
 		    
 		    // category 2
 		    if(t == ObjectType.DOUBLE || t == ObjectType.LONG) {
 		       stack.push(stack.peek(2));
 		       stack.push(stack.peek(3));
 		       
 		    // category 1	
 		    } else {
 		       stack.push(stack.peek());
 		    }
 			break;
 		    
 		// Form 1:
         // ..., value3, value2, value1 ..., value2, value1, value3, value2, value1
        //  where value1, value2, and value3 are all values of a category 1 computational type.
         // Form 2:
         //  ..., value2, value1 ..., value1, value2, value1
        // where value1 is a value of a category 2 computational type and value2 is a value of a category 1 computational type.		
 		case Constants.DUP2_X1:
 			logInfo("DUP2_X1, stack size: " + stack.size());
 		    t = stack.peek();
 		    
 		    // category 2
 		    if(t == ObjectType.DOUBLE || t == ObjectType.LONG) {
 		       stack.push(stack.peek());
 		       
 		    // category 1	
 		    } else {
 		       stack.push(stack.peek());
 		       stack.push(stack.peek());
 		    }
 			break;
 		
 		// DUP_X2 - Duplicate top operand stack word and put three down
         // Stack: ..., word3, word2, word1 -> ..., word1, word3, word2, word1	
 		case Constants.DUP2_X2:
 			logInfo("DUP2_X2, stack size: " + stack.size());
 			stack.push(stack.peek());
 			break;
 
 		// Float operations
 		case Constants.F2D:
 			logInfo("F2D, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.DOUBLE);
 			break;	
 		case Constants.F2I:
 			logInfo("F2I, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.INT);
 			break;
         case Constants.F2L:
         	logInfo("F2L, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.LONG);
 			break;
 		case Constants.FADD:
 			logInfo("FADD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.FLOAT);
 			break;
 		case Constants.FALOAD:
 			logInfo("FALOAD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.FLOAT);
 			break;
 		case Constants.FASTORE:
 			logInfo("FASTORE, stack size: " + stack.size());
 			stack.pop(3);
 			break;
 		case Constants.FCMPG:
 			logInfo("FCMPG, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;		
 		case Constants.FCMPL:
 			logInfo("FCMPL, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 		case Constants.FCONST_0:
 			logInfo("FCONST_0, stack size: " + stack.size());
 		   stack.push(ObjectType.FLOAT);
 		   break;
 		case Constants.FCONST_1:
 			logInfo("FCONST_1, stack size: " + stack.size());
 			stack.push(ObjectType.FLOAT);
 			break;
 		case Constants.FDIV:
 			logInfo("FDIV, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.FLOAT);
 			break;
 
 	    // FLOAD - Load float from local variable
         // Stack ... -> ..., result	
 		case Constants.FLOAD_0:
 			wide = ProcessInstruction_Util.processFLOAD(0,stack, wide, bytes, varTable);
 			break;			
 		case Constants.FLOAD_1:
 			wide = ProcessInstruction_Util.processFLOAD(1,stack, wide, bytes, varTable);
 			break;	
 		case Constants.FLOAD_2:
 			wide = ProcessInstruction_Util.processFLOAD(2,stack, wide, bytes, varTable);
 			break;	
 		case Constants.FLOAD_3:
 			wide = ProcessInstruction_Util.processFLOAD(3,stack, wide, bytes, varTable);
 			break;	
 		case Constants.FLOAD: 
 			wide = ProcessInstruction_Util.processFLOADn(stack, wide, bytes, varTable);
 			break;	
 
 		// FMUL - Multiply floats
         // Stack: ..., value1, value2 -> result
 		case Constants.FMUL:
 			logInfo("FMUL, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.FLOAT);
 			break;
 		
 		// ..., value1, value2  ..., result
 		case Constants.FREM:
 			logInfo("FREM, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.FLOAT);
 			break;
 			
 	    // ..., value  [empty]
 		case Constants.FRETURN:
 			logInfo("FRETURN, stack size: " + stack.size());
 			stack.pop(stack.size());
 			break;	
 			
 		// FSTORE - Store float into local variable
 		// Stack: ..., value -> ... 
 		case Constants.FSTORE_0:
 			wide = ProcessInstruction_Util.processFSTORE(0, stack, wide, bytes);
 		    break;
 		case Constants.FSTORE_1:
 			wide = ProcessInstruction_Util.processFSTORE(1, stack, wide, bytes);
 		    break;
 		case Constants.FSTORE_2:
 			wide = ProcessInstruction_Util.processFSTORE(2, stack, wide, bytes);
 		    break;
 		case Constants.FSTORE_3:
 			wide = ProcessInstruction_Util.processFSTORE(3, stack, wide, bytes);			
 		    break;
 		case Constants.FSTORE: 
 			wide = ProcessInstruction_Util.processFSTOREn(stack, wide, bytes);
 			break;		
 		
 		// FSUB - Substract floats
         // Stack: ..., value1, value2 -> result	
 		case Constants.FSUB:
 			logInfo("FSUB, stack size: " + stack.size());
 		    stack.pop(2);
 		    stack.push(ObjectType.FLOAT);
 			
 		    
 		// GOTO - Branch always (to relative offset, not absolute address)
 		case Constants.GOTO: 
 		    index = ProcessInstruction_Util.getIndexShortIcecream(bytes);
 			logInfo("GOTO, stack size: " + stack.size() + " offset: " + index);
 			
 			// GoTo, skip opcodes
 			bytes.skipBytes(index-bytes.getIndex());
 			break;
 		 
 		
 		// GOTO_W - Branch always (to relative offset, not absolute address)
 		case Constants.GOTO_W:
 			logInfo("GOTO_W, stack size: " + stack.size());
 			int windex = ProcessInstruction_Util.getIndexIntIcecream(bytes);
 			logInfo("GOTO_W, stack size: " + stack.size() + " offset: " + windex);
 
 			// GoTo, skip opcodes
 			bytes.skipBytes(windex-bytes.getIndex());
 
 			break;
 			
 		// I2B - Convert int to byte
         // Stack: ..., value -> ..., result	
 		case Constants.I2B:
 			logInfo("I2B, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.INT);
 			break;			
 		case Constants.I2C:
 			logInfo("I2C, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.INT);
 			break;
 		case Constants.I2D:
 			logInfo("I2D, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.DOUBLE);
 			break;	
 		case Constants.I2F:
 			logInfo("I2F, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.FLOAT);
 			break;	
 		case Constants.I2L:
 			logInfo("I2L, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.LONG);
 			break;		
 		case Constants.I2S:
 			logInfo("I2S, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.INT);
 			break;
 		case Constants.IADD:
 			logInfo("IADD, stack size: " + stack.size());
 			{
 				Type t1 = stack.pop();
 				Type t2 = stack.pop();
 				Type retType = null;
 				if (t2 instanceof FieldReferenceType) {
 					//ToDo: Propogate field reference
 					stack.push(ObjectType.INT);
 				} else {
 			        stack.push(ObjectType.INT);
 				}
 			}
 			break;
 		//Stack: ..., arrayref, index -> ..., value
 		case Constants.IALOAD:
 			logInfo("IALOAD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 		//Stack: ..., value1, value2 -> ..., result
 		case Constants.IAND:
 			logInfo("IAND, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 		//Stack: ..., arrayref, index, value -> ...
 		case Constants.IASTORE:
 			logInfo("IASTORE, stack size: " + stack.size());
 			stack.pop(3);
 			break;
 		
 		// ICONST - Push value between -1, ..., 5, other values 
 		// cause an exception
         // Stack: ... -> ..., 
 		case Constants.ICONST_0: 
 		case Constants.ICONST_1: 
 		case Constants.ICONST_2: 
 		case Constants.ICONST_3: 
 		case Constants.ICONST_4: 
 		case Constants.ICONST_5:
 		case Constants.ICONST_M1:
 		   logInfo("ICONST {0,1,2,3,4,5}, stack size" + stack.size());
 		   stack.push(ObjectType.INT);		   
            break;
            
 		//Stack: ..., value1, value2 -> result
         case Constants.IDIV:
  		   logInfo("IDIV, stack size" + stack.size());
  		   stack.pop(2);
 		   stack.push(ObjectType.INT);		   
            break;
     
    		
    		// IF_ACMPEQ - Branch if reference comparison succeeds
    	    // Stack: ..., value1, value2 -> ...
    		case Constants.IF_ACMPEQ:
 
    			
    		// IF_ACMPNE - Branch if reference comparison doesn't succeed
            // Stack: ..., value1, value2 -> ...
    		case Constants.IF_ACMPNE:
    		
    		// IF_ICMPEQ - Branch if int comparison succeeds
    		// Stack: ..., value1, value2 -> ...
    		case Constants.IF_ICMPEQ:
    		case Constants.IF_ICMPGE:
    		case Constants.IF_ICMPGT:
    		case Constants.IF_ICMPLE:
    		case Constants.IF_ICMPLT:
    			
    		// IF_ICMPNE - Branch if int comparison doesn't succeed
            // Stack: ..., value1, value2 -> ...
    		case Constants.IF_ICMPNE: 
    			logInfo("IF<>, stack size: " + stack.size());
    			stack.pop(2);
    			index = ProcessInstruction_Util.getIndexShortIcecream(bytes);
    			logInfo("IF: " + stack.size() + " " + index);
    			branchState = new BranchState(stack.getClone(),
    					                                  lockStack.getClone(),
    					                                  index);
    			branchQueue.add(branchState);
    			break;
    			
 			
 		// IFEQ - Branch if int comparison with zero succeeds
 		// Stack: ..., value -> ...
 		case Constants.IFEQ:				
 		case Constants.IFGE:
 		case Constants.IFGT:			
 		case Constants.IFLE:			
 		case Constants.IFLT:			
 		case Constants.IFNE:
 			
 		// IFNONNULL - Branch if reference is not null
         // Stack: ..., reference -> ...
 		case Constants.IFNONNULL:
 		case Constants.IFNULL: 
 			logInfo("IF<>, stack size: " + stack.size());
 			if(stack.size()==0) {
 				Log.logByteInfo("ERROR: Attempting to pop empty stack");
 				Log.logByteInfo(parentMethodInfo.getFullUniqueMethodName());
 			} else {
 			    stack.pop();
 			}
 			index = ProcessInstruction_Util.getIndexShortIcecream(bytes);
 			logInfo("IF: " + stack.size() + " " + index);
 			branchState = new BranchState(stack.getClone(),
 					                                  lockStack.getClone(),
 					                                  index);
 			branchQueue.add(branchState);
 			break;
 
 	
 		// IINC - Increment local variable by constant
 		case Constants.IINC:
 			logInfo("IINC");
 			if (wide) {
 				vindex = bytes.readShort();
 				constant = bytes.readShort();
 				wide = false;
 			} else {
 				vindex = bytes.readUnsignedByte();
 				constant = bytes.readByte();
 			}
 			break;
 			
 		// ILOAD - Load int from local variable onto stack
 		// Stack: ... -> ..., result
 		case Constants.ILOAD_0:
 			wide = ProcessInstruction_Util.processILOAD(parentMethodInfo,0,stack, wide, bytes, varTable);
 			break;			
 		case Constants.ILOAD_1:
 			wide = ProcessInstruction_Util.processILOAD(parentMethodInfo,1,stack, wide, bytes, varTable);
 			break;	
 		case Constants.ILOAD_2:
 			wide = ProcessInstruction_Util.processILOAD(parentMethodInfo,2,stack, wide, bytes, varTable);
 			break;	
 		case Constants.ILOAD_3:
 			wide = ProcessInstruction_Util.processILOAD(parentMethodInfo,3,stack, wide, bytes, varTable);
 			break;	
 		case Constants.ILOAD: 
 			wide = ProcessInstruction_Util.processILOADn(parentMethodInfo,stack, wide, bytes, varTable);
 			break;
        
 	    // Stack: ..., value1, value2 -> result
 		case Constants.IMUL:
 		    logInfo("IMUL, stack size: " + stack.size());
 		    stack.pop(2);
 		    stack.push(ObjectType.INT);
 		    break;
 		    
 		// Stack: ..., value -> ..., result
 		case Constants.INEG:
 		    logInfo("INEG, stack size: " + stack.size());
 		    stack.pop(1);
 		    stack.push(ObjectType.INT);
 		    break;
 		    
 		// INSTANCEOF - Determine if object is of given type
         // Stack: ..., objectref -> ..., result
 		case Constants.INSTANCEOF:
 			logInfo("INSTANCEOF, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.INT);
 			index = bytes.readShort();
 			break;
 				
 		//Stack: ..., value1, value2 -> ..., result
 		case Constants.IOR:
 		case Constants.IREM:
 		case Constants.ISHL:
 		case Constants.ISHR:
 			logInfo("IOR, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;	
 			
 		//Stack: ..., value -> <empty>
 		case Constants.IRETURN:
 			logInfo("IRETURN, stack size: " + stack.size());
 			stack.pop(stack.size());
 			break;
 			
 		// ISTORE - Store int from stack into local variable
 		// Stack: ..., value -> ... 
 		case Constants.ISTORE_0:
 			wide = ProcessInstruction_Util.processISTORE(0, stack, wide, bytes);
 		    break;
 		case Constants.ISTORE_1:
 			wide = ProcessInstruction_Util.processISTORE(1, stack, wide, bytes);
 		    break;
 		case Constants.ISTORE_2:
 			wide = ProcessInstruction_Util.processISTORE(2, stack, wide, bytes);
 		    break;
 		case Constants.ISTORE_3:
 			wide = ProcessInstruction_Util.processISTORE(3, stack, wide, bytes);			
 		    break;
 		case Constants.ISTORE: 
 			wide = ProcessInstruction_Util.processISTOREn(stack, wide, bytes);
 			break;		
 		
 		//Stack: ..., value1, value2 -> result
 		case Constants.ISUB:
 			logInfo("ISUB, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 			
 		//Stack: ..., value1, value2 -> ..., result
 		case Constants.IUSHR:
 			logInfo("IUSHR, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 			
 		//Stack: ..., value1, value2 -> ..., result
 		case Constants.IXOR:
 			logInfo("IXOR, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;	
 		
 		case Constants.JSR: 
 			index = ProcessInstruction_Util.getIndexShortIcecream(bytes);
 			logInfo("JSR, stack size: " + stack.size() + " " + index);	
 			stack.push(ReturnaddressType.NO_TARGET);
 			break;
 			
 		case Constants.JSR_W:
 			windex = ProcessInstruction_Util.getIndexIntIcecream(bytes);
 			logInfo("JSRW, stack size: " + stack.size() + " " + windex);	
 			stack.push(ReturnaddressType.NO_TARGET);
 			break;				
 		
 		// L2D - Convert long to double
         // Stack: ..., value.word1, value.word2 -> ..., result.word1, result.word2
 		case Constants.L2D:
 			logInfo("L2D, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.DOUBLE);
 			break;
 			
 		// L2F - Convert long to float
         // Stack: ..., value.word1, value.word2 -> ..., result
 		case Constants.L2F:
 			logInfo("L2F, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.FLOAT);
 			break;	
 		
 		// L2I - Convert long to int
         // Stack: ..., value.word1, value.word2 -> ..., result	
 		case Constants.L2I:
 			logInfo("L2I, stack size: " + stack.size());
 			stack.pop();
 			stack.push(ObjectType.INT);
 			break;	
 		
 		//..., value1, value2  ..., result
 		case Constants.LADD:
 			logInfo("LADD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;	
 			
 		//..., arrayref, index  ..., value
 		case Constants.LALOAD:
 			logInfo("LALOAD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;
 			
 		// ..., value1, value2  ..., result
 		case Constants.LAND:
 			logInfo("LAND, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;	
 		
 		//	..., arrayref, index, value  ...	
 		case Constants.LASTORE:
 			logInfo("LASTORE, stack size: " + stack.size());
 			stack.pop(3);
 			break;	
 		
 		//	..., value1, value2  ..., result
 		case Constants.LCMP:
 			logInfo("LCMP, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;	
 	
 		case Constants.LCONST_0:
 		case Constants.LCONST_1:
 			logInfo("LCONST_<0,1>, stack size: " + stack.size());
 			stack.push(ObjectType.LONG);
 			break;		
 		
 		// LDC - Push item from constant pool.
         // Stack: ... -> ..., item
 		case Constants.LDC: 
 			vindex = bytes.readUnsignedByte();		
 			logInfo("LDC, stack size: " + stack.size());
 			if(stack.size()>=stack.maxStack()) {
 				Log.logByteInfo("Attempting to push stack size beyond limit");
 				Log.logByteInfo(parentMethodInfo.getFullUniqueMethodName());
 			}
 			stack.push(Type.UNKNOWN); //int, float, or long 	
 			break;
 		
 		// LDC_W - Push item from constant pool (wide index)
         // Stack: ... -> ..., item.word1, item.word2
 		case Constants.LDC_W: 
 			vindex = bytes.readShort();
 			logInfo("LDC_W, stack size: " + stack.size());
 			stack.push(Type.UNKNOWN); //int, float, or long 	
 			break;
 			
 		// LDC2_W - Push long or double from constant pool
 		// Stack: ... -> ..., item.word1, item.word2
 		case Constants.LDC2_W: 
 			vindex = bytes.readShort();
 			logInfo("LDC2_W, stack size: " + stack.size());
 			stack.push(Type.UNKNOWN); //long or double	
 			break;
 
 		//..., value1, value2  ..., result
 		case Constants.LDIV:
 			logInfo("LDIV, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;
 			
 		// LLOAD - Load long from local variable
 		// Stack ... -> ..., result.word1, result.word
 		case Constants.LLOAD_0:
 			wide = ProcessInstruction_Util.processLLOAD(0,stack, wide, bytes, varTable);
 			break;			
 		case Constants.LLOAD_1:
 			wide = ProcessInstruction_Util.processLLOAD(1,stack, wide, bytes, varTable);
 			break;	
 		case Constants.LLOAD_2:
 			wide = ProcessInstruction_Util.processLLOAD(2,stack, wide, bytes, varTable);
 			break;	
 		case Constants.LLOAD_3:
 			wide = ProcessInstruction_Util.processLLOAD(3,stack, wide, bytes, varTable);
 			break;	
 		case Constants.LLOAD: 
 			wide = ProcessInstruction_Util.processLLOADn(stack, wide, bytes, varTable);
 			break;
 			
 		//..., value1, value2  ..., result
 		case Constants.LMUL:
 			logInfo("LMUL, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;
 		
 		case Constants.LNEG:
 			logInfo("LNEG, stack size: " + stack.size());
 			stack.pop(1);
 			stack.push(ObjectType.LONG);
 			break;	
 		
 	    // LOOKUPSWITCH - Switch with unordered set of values
 		case Constants.LOOKUPSWITCH:
 			logInfo("LOOKUPSWITCH");
 			stack.pop();
 			int npairs = bytes.readInt();
 			offset = bytes.getIndex() - 8 - no_pad_bytes - 1;
 			jump_table = new int[npairs];
 			default_offset += offset;
 
 			// Print switch indices in first row (and default)
 			for (int i = 0; i < npairs; i++) {
 				int match = bytes.readInt();
 				jump_table[i] = offset + bytes.readInt();
 			}
 			break;
 		
 		// ..., value1, value2  ..., result
 		case Constants.LOR:
 			logInfo("LOR, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;
 			
 		//..., value1, value2  ..., result
 		case Constants.LREM:
 			logInfo("LREM, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;	
 		//..., value  [empty]
 		case Constants.LRETURN:
 			logInfo("LRETURN, stack size: " + stack.size());
 			stack.pop(stack.size());
 			break;
 			
 		//..., value1, value2  ..., result
 		case Constants.LSHL:
 			logInfo("LSHL, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;		
 			
 		//..., value1, value2  ..., result
 		case Constants.LSHR:
 			logInfo("LSHR, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;	
 			
 		// LSTORE - Store long into local variable
 		// Stack: ..., value.word1, value.word2 -> ... 
 		case Constants.LSTORE_0:
 			wide = ProcessInstruction_Util.processLSTORE(0, stack, wide, bytes);
 		    break;
 		case Constants.LSTORE_1:
 			wide = ProcessInstruction_Util.processLSTORE(1, stack, wide, bytes);
 		    break;
 		case Constants.LSTORE_2:
 			wide = ProcessInstruction_Util.processLSTORE(2, stack, wide, bytes);
 		    break;
 		case Constants.LSTORE_3:
 			wide = ProcessInstruction_Util.processLSTORE(3, stack, wide, bytes);			
 		    break;
 		case Constants.LSTORE: 
 			wide = ProcessInstruction_Util.processLSTOREn(stack, wide, bytes);
 			break;	
 			
 		//..., value1, value2  ..., result
 		case Constants.LSUB:
 			logInfo("LSUB, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;		
 		
 		case Constants.LUSHR:
 			logInfo("LUSHR, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;
 			
 		case Constants.LXOR:
 			logInfo("LXOR, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.LONG);
 			break;
 
 		// MONITORENTER - Enter monitor for object
         // Stack: ..., objectref -> ...
 		case Constants.MONITORENTER:
 			logLocking("MONITORENTER, stack size: " + stack.size());
             ProcessInstruction_Lock.processMonitorEnter(stack, 
             		                                    handlerList, 
             		                                    parentMethodInfo, 
             		                                    lockStack, 
             		                                    invokedLineNumber);
 			break;
 
 		// MONITOREXIT - Exit monitor for object
         // Stack: ..., objectref -> ...
 		case Constants.MONITOREXIT:
 			logLocking("MONITOREXIT, stack size: " + stack.size());
             ProcessInstruction_Lock.processMonitorExit(stack, 
             		                                   handlerList, 
             		                                   parentMethodInfo, 
             		                                   lockStack);
 			break;
 
 		// Multidimensional array of references.
 		case Constants.MULTIANEWARRAY:
 			logInfo("MULTIANEWARRAY, stack size: " + stack.size());
 			index = bytes.readShort();
 			int dimensions = bytes.readByte();
 			stack.pop(dimensions);
 			stack.push(new ArrayType(Type.UNKNOWN, dimensions));
 			break;			
 		
 		// NEW - Create new object
         // Stack: ... -> ..., objectref
 		case Constants.NEW:
 			logInfo("NEW, stack size: " + stack.size());
 			// get name of Java class
 			index = bytes.readShort();
 			ConstantClass cc = (ConstantClass)constant_pool.getConstant(index);
 			ConstantUtf8 c = (ConstantUtf8)constant_pool.getConstant(cc.getNameIndex());
 			String rawClassName = c.getBytes();
 			String fullClassName = Utility.compactClassName(rawClassName, false);
 			logInfo("NEW, stack size " + stack.size() + " " + index + " " + fullClassName);
 			
 			// push reference to stack	
 			ObjectType type = new ObjectType(fullClassName);
 			stack.push(type);
 			
 			break;
 		
 
 		// NEWARRAY - Create new array of basic type (int, short, ...)
         // Stack: ..., count -> ..., arrayref
         // type must be one of T_INT, T_SHORT, ...
 		case Constants.NEWARRAY: 
 			logInfo("NEWARRAY");
 			index = bytes.readByte();
 			stack.pop();
 			t = new ArrayType(ObjectType.UNKNOWN,1);
 			stack.push(t);
 			break;
 
 		case Constants.NOP:
 			break;
 	
 		
 		// POP - Pop top operand stack word
         // Stack: ..., word -> ...
 		case Constants.POP:
 			logInfo("POP, stack size: " + stack.size());
 			stack.pop();
 			break;
 			
 		// POP2 - Pop two top operand stack words
 		// Stack: ..., word2, word1 -> ...
 		case Constants.POP2:
 			logInfo("POP2, stack size: " + stack.size());
 			try {
 				t = stack.peek();
 				// cat2
 				if (t == ObjectType.LONG || t == ObjectType.DOUBLE) {
 					stack.pop(1);
 					// cat1
 				} else {
 					stack.pop(2);
 				}
 			} catch (Exception e) {
 				logInfo("ERROR, POP2");
 			}
 			break;
 				
 		// RET - Return from subroutine
 		// Stack: ... -> ...
 		case Constants.RET:
 			logInfo("RET, stack size: " + stack.size());
 			vindex = ProcessInstruction_Util.getVariableIndex(wide, bytes);
 			wide = false; // clear flag
 			break;
 		
 		// RETURN - Return from void method
 		// Stack: ... -> <empty>
 		case Constants.RETURN: 
 			logInfo("RETURN, stack size: " + stack.size());
 			stack.pop(stack.size());
 		    break;
 		  
 		// SALOAD - Load short from array
         // Stack: ..., arrayref, index -> ..., value
 		case Constants.SALOAD:
 			logInfo("SALOAD, stack size: " + stack.size());
 			stack.pop(2);
 			stack.push(ObjectType.INT);
 			break;
 			
 		// SASTORE - Store into short array
         // Stack: ..., arrayref, index, value -> ...	
 		case Constants.SASTORE:
 			logInfo("SASTORE, stack size: " + stack.size());
 			stack.pop(3);
 			break;	
 		
 		// SIPUSH - Push short
         // Stack: ... -> ..., value	
 		case Constants.SIPUSH:
 			logInfo("SIPUSH, stack size: " + stack.size());
 			bytes.readByte();
 			bytes.readByte();
 			stack.push(ObjectType.INT);
 			break;
 			
 		// SWAP - Swa top operand stack word
         // Stack: ..., word2, word1 -> ..., word1, word2
 		case Constants.SWAP:
 			logInfo("SWAP, stack size: " + stack.size());
 			// noop
 			break;
 			
 		// Remember wide byte which is used to form a 16-bit address in the
 		// following instruction. Relies on that the method is called again with
 		// the following opcode.
 		case Constants.WIDE:
 			logInfo("WIDE");
 			wide = true;
 			break;
 
         // handled in helper method
 		case Constants.PUTSTATIC:		
 		case Constants.PUTFIELD:
 		case Constants.GETSTATIC:
 		case Constants.GETFIELD:        
 		    ProcessInstruction_SetGetField.processField(
             		opcode,
             		stack,
             		constant_pool,bytes,
             		jClass,
         			parentMethodInfo,
         			invokedLineNumber,
         			lockStack.isSynchronized(),
         			handlerList);
 			break;
 			
 		// INVOKESPECIAL - Invoke instance method; special handling for 
 		// superclass, private and instance initialization method invocations
         // Stack: ..., objectref, [arg1, [arg2 ...]] -> ...
 		case Constants.INVOKESPECIAL:
 			logInfo("(INVOKESPECIAL)");			
 		case Constants.INVOKESTATIC:
 			logInfo("(INVOKESTATIC)");
 		case Constants.INVOKEVIRTUAL:
 			logInfo("(INVOKEVIRTUAL)");
 		case Constants.INVOKEINTERFACE:
 			logInfo("(INVOKEINTERFACE)");
 			logInfo("INVOKE, stack size: " + stack.size() + " lock stack size: " + lockStack.debugGetSize());
 			ProcessInstruction_MethodInvoke.processMethodInvoke(stack,
 					constant_pool,
 					jClass,
 				    handlerList,
 				    hashMapSynthetic,
 				    opcode, bytes, 
 				    invokedLineNumber,
 					parentMethodInfo, 
 					lockStack);
 			break;
 		
 		default:
 			logInfo("DEFAULT: " + opcode);
 		    if(true) 
 		    	throw new Exception("Missing DEFAULT: " + opcode);
 			if (Constants.NO_OF_OPERANDS[opcode] > 0) {
 				for (int i = 0; i < Constants.TYPE_OF_OPERANDS[opcode].length; i++) {
 					switch (Constants.TYPE_OF_OPERANDS[opcode][i]) {
 					case Constants.T_BYTE:
 						break;
 					case Constants.T_SHORT: // Either branch or index
 						break;
 					case Constants.T_INT:
 						break;
 					default: // Never reached
 						System.err.println("Unreachable default case reached!");
 					}
 				}
 			}
 		}
 
 	}
  
 }
