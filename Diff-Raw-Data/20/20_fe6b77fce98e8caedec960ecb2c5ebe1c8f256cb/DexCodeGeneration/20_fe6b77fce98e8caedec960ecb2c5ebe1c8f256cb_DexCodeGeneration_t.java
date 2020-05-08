 package com.rx201.dx.translator;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Stack;
 
 import org.jf.dexlib.ClassDataItem.EncodedMethod;
 import org.jf.dexlib.CodeItem;
 import org.jf.dexlib.DebugInfoItem;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.FieldIdItem;
 import org.jf.dexlib.Item;
 import org.jf.dexlib.MethodIdItem;
 import org.jf.dexlib.ProtoIdItem;
 import org.jf.dexlib.StringIdItem;
 import org.jf.dexlib.TypeIdItem;
 import org.jf.dexlib.TypeListItem;
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.InstructionWithReference;
 import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
 import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
 import org.jf.dexlib.Code.Format.Instruction20bc;
 import org.jf.dexlib.Code.Format.Instruction21c;
 import org.jf.dexlib.Code.Format.Instruction22c;
 import org.jf.dexlib.Code.Format.Instruction35c;
 import org.jf.dexlib.Code.Format.Instruction3rc;
 import org.jf.dexlib.CodeItem.EncodedCatchHandler;
 import org.jf.dexlib.CodeItem.EncodedTypeAddrPair;
 import org.jf.dexlib.CodeItem.TryItem;
 import org.jf.dexlib.Util.AccessFlags;
 import org.jf.util.IndentingWriter;
 
 import uk.ac.cam.db538.dexter.dex.DexParsingCache;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGetWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPutWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGetWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePutWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGetWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPutWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpLiteral;
 import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpWide;
 import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;
 import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 
 import com.android.dx.cf.code.ConcreteMethod;
 import com.android.dx.cf.code.LocalVariableList;
 import com.android.dx.cf.code.Ropper;
 import com.android.dx.cf.direct.DirectClassFile;
 import com.android.dx.cf.direct.StdAttributeFactory;
 import com.android.dx.dex.DexOptions;
 import com.android.dx.dex.cf.CodeStatistics;
 import com.android.dx.dex.cf.OptimizerOptions;
 import com.android.dx.dex.code.DalvCode;
 import com.android.dx.dex.code.PositionList;
 import com.android.dx.dex.code.RopTranslator;
 import com.android.dx.dex.file.ClassDefItem;
 import com.android.dx.rop.annotation.Annotations;
 import com.android.dx.rop.code.BasicBlock;
 import com.android.dx.rop.code.BasicBlockList;
 import com.android.dx.rop.code.DexTranslationAdvice;
 import com.android.dx.rop.code.Insn;
 import com.android.dx.rop.code.InsnList;
 import com.android.dx.rop.code.LocalVariableExtractor;
 import com.android.dx.rop.code.LocalVariableInfo;
 import com.android.dx.rop.code.PlainCstInsn;
 import com.android.dx.rop.code.PlainInsn;
 import com.android.dx.rop.code.RegisterSpec;
 import com.android.dx.rop.code.RegisterSpecList;
 import com.android.dx.rop.code.Rop;
 import com.android.dx.rop.code.RopMethod;
 import com.android.dx.rop.code.Rops;
 import com.android.dx.rop.code.SourcePosition;
 import com.android.dx.rop.code.TranslationAdvice;
 import com.android.dx.rop.cst.CstInteger;
 import com.android.dx.rop.cst.CstMethodRef;
 import com.android.dx.rop.cst.CstString;
 import com.android.dx.rop.cst.CstType;
 import com.android.dx.rop.type.Type;
 import com.android.dx.ssa.Optimizer;
 import com.android.dx.util.Hex;
 import com.android.dx.util.IntList;
 import com.rx201.dx.translator.util.DexRegisterHelper;
 import com.rx201.dx.translator.util.MethodParameter;
 import com.rx201.dx.translator.util.MethodPrototype;
 
 public class DexCodeGeneration {
 
 	private DexOptions dexOptions;
 	
 	private DexMethodWithCode method;
 	private int inWords;
 	private int outWords;
 	private boolean isStatic;
 	
 	private DexCodeAnalyzer analyzer;
 	
 	public DexCodeGeneration(DexMethodWithCode method, DexParsingCache cache) {
 		dexOptions = new DexOptions();
 	    dexOptions.targetApiLevel = 10;
 	    
 	    this.method = method;
		inWords = method.getPrototype().countParamWords(method.isStatic());
 		outWords = method.getCode().getOutWords();
 		isStatic = method.isStatic();
 		
 	    this.analyzer = new DexCodeAnalyzer(method, cache);
 	    this.analyzer.analyze();
 	}
 
 	private Item internReferencedItem(DexFile dexFile, Item referencedItem) {
 	    if (referencedItem instanceof FieldIdItem) {
             return DexCodeIntern.intern(dexFile, (FieldIdItem)referencedItem);
 		} else if (referencedItem instanceof MethodIdItem) {
 			return DexCodeIntern.intern(dexFile, (MethodIdItem)referencedItem);
 		} else if (referencedItem instanceof TypeIdItem) {
 			return DexCodeIntern.intern(dexFile, (TypeIdItem)referencedItem);
 		} else if (referencedItem instanceof StringIdItem) {
 			return DexCodeIntern.intern(dexFile, (StringIdItem)referencedItem);
 		} else {
 			throw new RuntimeException("Unknown Item");
 		}
 		
 	}
 	public CodeItem generateCodeItem(DexFile dexFile) {
 		
 		DalvCodeBridge translatedCode = new DalvCodeBridge(processMethod(method.getCode()), method);
 		
 		// Need to intern instructions to the new dexFile, as they are from a different dex file
 		Instruction[] tmpInstructions = translatedCode.getInstructions();
 		List<Instruction> instructions = null;
 		if (tmpInstructions != null) {
 			instructions = new ArrayList<Instruction>();
 			for(Instruction inst : translatedCode.getInstructions()) {
 				if (inst instanceof Instruction20bc) {
 					Instruction20bc i = (Instruction20bc)inst;
 					inst = new Instruction20bc(i.opcode, i.getValidationErrorType(), internReferencedItem(dexFile, i.getReferencedItem()));
 					
 				} else if (inst instanceof Instruction21c) {
 					Instruction21c i = (Instruction21c)inst;
 					inst = new Instruction21c(i.opcode, (short)i.getRegisterA(), internReferencedItem(dexFile, i.getReferencedItem())); 
 					
 				} else if (inst instanceof Instruction22c) {
 					Instruction22c i = (Instruction22c)inst;
 					inst = new Instruction22c(i.opcode, (byte)i.getRegisterA(), (byte)i.getRegisterB(), internReferencedItem(dexFile, i.getReferencedItem())); 
 					
 				} else if (inst instanceof Instruction35c) {
 					Instruction35c i = (Instruction35c)inst;
 					inst = new Instruction35c(i.opcode,  i.getRegCount(),
 							(byte)i.getRegisterD(), (byte)i.getRegisterE(), (byte)i.getRegisterF(), (byte)i.getRegisterG(), (byte)i.getRegisterA(), 
 							internReferencedItem(dexFile, i.getReferencedItem())); 
 					
 				} else if (inst instanceof Instruction3rc) {
 					Instruction3rc i = (Instruction3rc)inst;
 					inst = new Instruction3rc(i.opcode, (short)i.getRegCount(), i.getStartRegister(), internReferencedItem(dexFile, i.getReferencedItem())); 
 					
 				} else if (inst instanceof InstructionWithReference) {
 					throw new RuntimeException("Unhandled InstructionWithReference");
 				} 
 				instructions.add(inst);
 			}
 		}		
 		
 		// Perform the same interning on tryItem and CatchHandler
 		TryItem[] tmpTries = translatedCode.getTries();
 		ArrayList<TryItem> newTries = null;
 		ArrayList<EncodedCatchHandler> newCatchHandlers = null;
 		if (tmpTries != null) {
 			newTries = new ArrayList<TryItem>();
 			newCatchHandlers = new ArrayList<EncodedCatchHandler>();
 			
 			for(TryItem curTryItem : tmpTries) {
 				EncodedTypeAddrPair[] oldTypeAddrPair = curTryItem.encodedCatchHandler.handlers;
 				EncodedTypeAddrPair[] typeAddrPair = new EncodedTypeAddrPair[oldTypeAddrPair.length];
 				for (int j=0; j<typeAddrPair.length; j++) {
 					typeAddrPair[j] = new EncodedTypeAddrPair(DexCodeIntern.intern(dexFile, oldTypeAddrPair[j].exceptionType),
 																oldTypeAddrPair[j].getHandlerAddress());
 				}
 				EncodedCatchHandler newCatchHandler = new EncodedCatchHandler(typeAddrPair, curTryItem.encodedCatchHandler.getCatchAllHandlerAddress());
 				newTries.add(new TryItem(curTryItem.getStartCodeAddress(), curTryItem.getTryLength(), newCatchHandler));
 				newCatchHandlers.add(newCatchHandler);
 			}		
 		}
 		
 		
 		int registerCount = translatedCode.getRegisterCount(); 
 		
 		return CodeItem.internCodeItem(dexFile, registerCount, inWords, outWords, /* debugInfo */ null, instructions, newTries, newCatchHandlers);
 		
 	}
 
 	private DalvCode processMethod(DexCode code) {
 		if (code == null) 
 			return null;
 
 		RopMethod rmeth = toRop(code);
 		System.out.println("==== Before Optimization ====");
 		dump(rmeth);
 		
         rmeth = Optimizer.optimize(rmeth, inWords, isStatic, false, DexTranslationAdvice.THE_ONE);
 		System.out.println("==== After Optimization ====");
 		dump(rmeth);
 		
         DalvCode dcode = RopTranslator.translate(rmeth, PositionList.NONE, null, inWords, dexOptions);
         
         return dcode;
 	}
 	
 	private RopMethod toRop(DexCode code) {
         
         // Build basic blocks
         ArrayList<ArrayList<AnalyzedDexInstruction>> basicBlocks = buildBasicBlocks();
         
         DexInstructionTranslator translator = new DexInstructionTranslator(analyzer);
 
    	System.out.println("==================================================================================");
    	System.out.println(String.format("%s param reg: %d", method.getName()  + method.getPrototype().toString(), 
				inWords));
         
         // Convert basicBlocks, hold the result in the temporary map. It is indexed by the basic block's first AnalyzedInst.
         HashMap<AnalyzedDexInstruction, ArrayList<Insn>> convertedBasicBlocks = new HashMap<AnalyzedDexInstruction, ArrayList<Insn>>();
         HashMap<AnalyzedDexInstruction, DexConvertedResult> convertedBasicBlocksInfo = new HashMap<AnalyzedDexInstruction, DexConvertedResult>();
         HashSet<AnalyzedDexInstruction> auxAdded = new HashSet<AnalyzedDexInstruction>();
         for(int bi=0; bi< basicBlocks.size(); bi++) 
     		convertedBasicBlocks.put(basicBlocks.get(bi).get(0), new ArrayList<Insn>());
         	
         for(int bi=0; bi< basicBlocks.size(); bi++) {
         	ArrayList<AnalyzedDexInstruction> basicBlock = basicBlocks.get(bi);
         	AnalyzedDexInstruction bbIndex = basicBlock.get(0);
         	
         	// Process instruction in the basic block as a whole, 
         	ArrayList<Insn> insnBlock = convertedBasicBlocks.get(bbIndex);
         	DexConvertedResult lastInsn = null;
         	for(int i = 0; i < basicBlock.size(); i++) {
         		AnalyzedDexInstruction inst = basicBlock.get(i);
         		if (inst.getInstruction() != null) {
 					try {
 						writer.write(inst.getInstruction().getOriginalAssembly());
 						writer.flush();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
         		}
         		lastInsn = translator.translate(inst);
         		insnBlock.addAll(lastInsn.insns);
         		
         		if (i != basicBlock.size() - 1) {
         			assert lastInsn.auxInsns.size() == 0;
         		} else if (lastInsn.auxInsns.size() != 0) { // Propagate auxInsns to primary successors
         			AnalyzedDexInstruction s = lastInsn.primarySuccessor;
     				assert !auxAdded.contains(s);
     				for(int ai = 0; ai < lastInsn.auxInsns.size(); ai++)
     					convertedBasicBlocks.get(s).add(ai, lastInsn.auxInsns.get(ai));
     				auxAdded.add(s);
         		}
         			
 				try {
 					if (lastInsn.insns.size() > 0) {
 						writer.write("\n    --> ");
 						writer.write(lastInsn.insns.get(0).toHuman());
 						if (lastInsn.insns.size() > 1)
 							writer.write("...");
 						writer.write("\n");
 						writer.flush();
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
         	}
         	
             // Add move-params to the beginning of the first block
         	if (bi == 0) {
         		DexPrototype prototype = method.getPrototype();
        		int regOffset = 0;
         		for(int i = 0; i < prototype.getParameterCount(isStatic); i++) {
         			DexRegisterType param = prototype.getParameterType(i, isStatic, method.getParentClass());
         			int paramRegId = DexRegisterHelper.normalize(prototype.getFirstParameterRegisterIndex(i, isStatic));
 	                Type one = Type.intern(param.getDescriptor());
 	                Insn insn = new PlainCstInsn(Rops.opMoveParam(one), SourcePosition.NO_INFO, RegisterSpec.make(paramRegId, one),
 	                                             RegisterSpecList.EMPTY,
	                                             CstInteger.make(regOffset));
 	                insnBlock.add(i, insn);
	                regOffset += param.getRegisters();
                 }
         	}
         	
         	convertedBasicBlocksInfo.put(bbIndex, lastInsn);
         }
         
         
         // Finally convert to ROP's BasicBlockList form from convertedBasicBlocks
         BasicBlockList ropBasicBlocks = new BasicBlockList(convertedBasicBlocks.size());
         int bbIndex = 0;
        
         for(AnalyzedDexInstruction head : convertedBasicBlocks.keySet()) {
         	ArrayList<Insn> insnBlock = convertedBasicBlocks.get(head); 
         	DexConvertedResult lastInsn = convertedBasicBlocksInfo.get(head);
         	
         	InsnList insns;
         	int insnBlockSize = insnBlock.size();
         	//Patch up empty bb or bb without goto
         	if (insnBlockSize == 0 || insnBlock.get(insnBlockSize - 1).getOpcode().getBranchingness() == Rop.BRANCH_NONE) {
         		insns = new InsnList(insnBlockSize + 1);
         		insns.set(insnBlockSize, new PlainInsn(Rops.GOTO, SourcePosition.NO_INFO, null, RegisterSpecList.EMPTY));
         	} else {
             	insns = new InsnList(insnBlock.size());
         	}
            	// then convert them to InsnList
         	for(int i=0 ;i<insnBlock.size(); i++)
         		insns.set(i, insnBlock.get(i));
         	insns.setImmutable();
         	
         	IntList successors = new IntList();
         	for(AnalyzedDexInstruction s : lastInsn.successors) 
         		successors.add(s.getInstructionIndex());
         	successors.setImmutable();
         	
         	int label = head.getInstructionIndex();
         	BasicBlock ropBasicBlock = new BasicBlock(label, insns, successors, lastInsn.primarySuccessor != null ? lastInsn.primarySuccessor.getInstructionIndex() : -1);
         	ropBasicBlocks.set(bbIndex++, ropBasicBlock);
         }
 
 
         return new SimpleRopMethod(ropBasicBlocks, analyzer.getStartOfMethod().getSuccesors().get(0).getInstructionIndex());
 	}
 	
 	private boolean endsBasicBlock(AnalyzedDexInstruction current) {
 		if (current.getSuccessorCount() != 1)
 			return true; // More than one successor, guaranteed to end a BB
 		
 		if (current.getInstruction() == null)
 			return false; // Pseudo instructions like DexLabel, etc
 		
 		if (current.getInstruction().cfgEndsBasicBlock())
 			return true; // Sufficient condition
 		
 		// A few more special cases
 		DexInstruction i = current.getInstruction();
 		if (    i instanceof DexInstruction_StaticGet ||
 				i instanceof DexInstruction_StaticGetWide ||
 				
 				i instanceof DexInstruction_StaticPut ||
 				i instanceof DexInstruction_StaticPutWide ||
 				
 				i instanceof DexInstruction_InstanceGet ||
 				i instanceof DexInstruction_InstanceGetWide ||
 				
 				i instanceof DexInstruction_InstancePut ||
 				i instanceof DexInstruction_InstancePutWide ||
 				
 				i instanceof DexInstruction_ArrayGet ||
 				i instanceof DexInstruction_ArrayGetWide ||
 
 				i instanceof DexInstruction_ArrayPut ||
 				i instanceof DexInstruction_ArrayPutWide ||
 
 				i instanceof DexInstruction_ConstClass ||
 				
 				i instanceof DexInstruction_NewInstance 
 				)
 			return true;
 		
 		if ( i instanceof DexInstruction_BinaryOpLiteral) {
 			Opcode_BinaryOpLiteral opcode = ((DexInstruction_BinaryOpLiteral)i).getInsnOpcode();
 			if (opcode == Opcode_BinaryOpLiteral.Div || opcode == Opcode_BinaryOpLiteral.Rem)
 				return true; // Will throw Arithmetic Exception
 		}
 		
 		if ( i instanceof DexInstruction_BinaryOp) {
 			Opcode_BinaryOp opcode = ((DexInstruction_BinaryOp)i).getInsnOpcode();
 			if (opcode == Opcode_BinaryOp.DivInt || opcode == Opcode_BinaryOp.RemInt)
 				return true; // Will throw Arithmetic Exception
 		}		
 		
 		if ( i instanceof DexInstruction_BinaryOpWide) {
 			Opcode_BinaryOpWide opcode = ((DexInstruction_BinaryOpWide)i).getInsnOpcode();
 			if (opcode == Opcode_BinaryOpWide.DivLong || opcode == Opcode_BinaryOpWide.RemLong)
 				return true; // Will throw Arithmetic Exception
 		}		
 		
 		return false;
 	}
 	
 	private ArrayList<ArrayList<AnalyzedDexInstruction>> buildBasicBlocks() {
         ArrayList<ArrayList<AnalyzedDexInstruction>> basicBlocks = new ArrayList<ArrayList<AnalyzedDexInstruction>>();
         
         Stack<AnalyzedDexInstruction> leads = new Stack<AnalyzedDexInstruction>();
         assert analyzer.getStartOfMethod().getSuccesors().size() == 1;
         leads.push(analyzer.getStartOfMethod().getSuccesors().get(0));
         HashSet<Integer> visited = new HashSet<Integer>();
         
         while(!leads.empty()) {
         	AnalyzedDexInstruction first = leads.pop();
         	int id = first.getInstructionIndex();
         	if (visited.contains(id)) continue; // Already visited this basic block before.
         	visited.add(id);
         	
         	ArrayList<AnalyzedDexInstruction> block = new ArrayList<AnalyzedDexInstruction>(); 
         	// Extend this basic block as far as possible
         	AnalyzedDexInstruction current = first; // Always refer to latest-added instruction in the bb
         	block.add(current);
         	while(!endsBasicBlock(current)) { 
         		// Condition 1: current has only one successor
         		// Condition 2: next instruction has only one predecessor
         		// Condition 3: current cannot throw
         		AnalyzedDexInstruction next = current.getSuccesors().get(0);
         		if (next.getPredecessorCount() == 1) {
         			block.add(next);
         			current = next;
         		} else
         			break;
         	}
         	
         	// Add successors of current to the to-be-visit stack
         	for(AnalyzedDexInstruction i : current.getSuccesors())
         		leads.push(i);
         	
         	basicBlocks.add(block);
        }
         
         return basicBlocks;
 	}
 
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
 	private void dump(RopMethod rmeth) {
 		StringBuilder sb = new StringBuilder();
 		
         BasicBlockList blocks = rmeth.getBlocks();
         int[] order = blocks.getLabelsInOrder();
 
         sb.append("first " + Hex.u2(rmeth.getFirstLabel()) + "\n");
 
         for (int label : order) {
             BasicBlock bb = blocks.get(blocks.indexOfLabel(label));
             sb.append("block ");
             sb.append(Hex.u2(label));
             sb.append("\n");
 
             IntList preds = rmeth.labelToPredecessors(label);
             int psz = preds.size();
             for (int i = 0; i < psz; i++) {
                 sb.append("  pred ");
                 sb.append(Hex.u2(preds.get(i)));
                 sb.append("\n");
             }
 
             InsnList il = bb.getInsns();
             int ilsz = il.size();
             for (int i = 0; i < ilsz; i++) {
                 Insn one = il.get(i);
                 sb.append("  ");
                 sb.append(il.get(i).toHuman());
                 sb.append("\n");
             }
 
             IntList successors = bb.getSuccessors();
             int ssz = successors.size();
             if (ssz == 0) {
                 sb.append("  returns\n");
             } else {
                 int primary = bb.getPrimarySuccessor();
                 for (int i = 0; i < ssz; i++) {
                     int succ = successors.get(i);
                     sb.append("  next ");
                     sb.append(Hex.u2(succ));
 
                     if ((ssz != 1) && (succ == primary)) {
                         sb.append(" *");
                     }
 
                     sb.append("\n");
                 }
             }
         }
         System.out.println(sb.toString());
 	}
 
 	public void write(com.android.dx.dex.file.DexFile dexFile) {
 		Writer humanOut = new OutputStreamWriter(System.out);
         try {
 			byte[] outArray = dexFile.toDex(humanOut, true);
 	        humanOut.flush();
 	        
 	        FileOutputStream output = new FileOutputStream(new File("result.dex"));
 	        output.write(outArray);
 	        output.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 /*
 	public void addClass() {
 		com.android.dx.dex.file.DexFile dexFile = new com.android.dx.dex.file.DexFile(dexOptions);
 		
         DirectClassFile cf =
                 new DirectClassFile(bytes, filePath, cfOptions.strictNameCheck);
 
             cf.setAttributeFactory(StdAttributeFactory.THE_ONE);
             cf.getMagic();
 
             OptimizerOptions.loadOptimizeLists(cfOptions.optimizeListFile,
                     cfOptions.dontOptimizeListFile);
 
             // Build up a class to output.
 
             CstType thisClass = cf.getThisClass();
             int classAccessFlags = cf.getAccessFlags() & ~AccessFlags.ACC_SUPER;
             CstString sourceFile = (cfOptions.positionInfo == PositionList.NONE) ? null :
                 cf.getSourceFile();
             ClassDefItem out =
                 new ClassDefItem(thisClass, classAccessFlags,
                         cf.getSuperclass(), cf.getInterfaces(), sourceFile);
 
             Annotations classAnnotations =
                 AttributeTranslator.getClassAnnotations(cf, cfOptions);
             if (classAnnotations.size() != 0) {
                 out.setClassAnnotations(classAnnotations);
             }
 
             processFields(cf, out);
             processMethods(cf, cfOptions, dexOptions, out);
 
             dexFile.add(out);
 	}	
 */
 }
 
