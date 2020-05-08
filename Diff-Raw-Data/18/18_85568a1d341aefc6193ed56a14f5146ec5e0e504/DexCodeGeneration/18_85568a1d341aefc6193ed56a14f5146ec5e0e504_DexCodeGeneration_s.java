 package com.rx201.dx.translator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Stack;
 
 import org.jf.dexlib.CodeItem;
 import org.jf.dexlib.CodeItem.EncodedCatchHandler;
 import org.jf.dexlib.CodeItem.EncodedTypeAddrPair;
 import org.jf.dexlib.CodeItem.TryItem;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.FieldIdItem;
 import org.jf.dexlib.Item;
 import org.jf.dexlib.MethodIdItem;
 import org.jf.dexlib.StringIdItem;
 import org.jf.dexlib.TypeIdItem;
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.InstructionWithReference;
 import org.jf.dexlib.Code.Format.Instruction20bc;
 import org.jf.dexlib.Code.Format.Instruction21c;
 import org.jf.dexlib.Code.Format.Instruction22c;
 import org.jf.dexlib.Code.Format.Instruction35c;
 import org.jf.dexlib.Code.Format.Instruction3rc;
 
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode.Parameter;
 import uk.ac.cam.db538.dexter.dex.method.DexMethod;
 import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
 import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
 
 import com.android.dx.dex.DexOptions;
 import com.android.dx.dex.code.DalvCode;
 import com.android.dx.dex.code.PositionList;
 import com.android.dx.dex.code.RopTranslator;
 import com.android.dx.rop.code.BasicBlock;
 import com.android.dx.rop.code.BasicBlockList;
 import com.android.dx.rop.code.DexTranslationAdvice;
 import com.android.dx.rop.code.Insn;
 import com.android.dx.rop.code.InsnList;
 import com.android.dx.rop.code.PlainCstInsn;
 import com.android.dx.rop.code.PlainInsn;
 import com.android.dx.rop.code.RegisterSpec;
 import com.android.dx.rop.code.RegisterSpecList;
 import com.android.dx.rop.code.Rop;
 import com.android.dx.rop.code.RopMethod;
 import com.android.dx.rop.code.Rops;
 import com.android.dx.rop.code.SourcePosition;
 import com.android.dx.rop.cst.CstInteger;
 import com.android.dx.rop.type.Type;
 import com.android.dx.ssa.Optimizer;
 import com.android.dx.util.Hex;
 import com.android.dx.util.IntList;
 
 public class DexCodeGeneration {
 
     private DexOptions dexOptions;
 
     private DexMethod method;
     private int inWords;
     private int outWords;
     private boolean isStatic;
 
     private DexCodeAnalyzer analyzer;
 
     public static boolean DEBUG = true;
     public static boolean INFO = true;
 
     public static long totalAnalysisTime = 0;
     public static long totalCGTime = 0;
     public static long totalDxTime = 0;
     public DexCodeGeneration(DexMethod method) {
         MethodDefinition methodDef = method.getMethodDef();
         DexMethodId methodId = methodDef.getMethodId();
 
         if (INFO) {
             System.out.println("==================================================================================");
             System.out.println(String.format("%s param reg: %d", method.getMethodDef().toString(), inWords));
         }
 
         dexOptions = new DexOptions();
         dexOptions.targetApiLevel = 10;
 
         this.method = method;
         inWords = methodId.getPrototype().countParamWords(methodDef.isStatic());
         outWords = method.getMethodBody().getOutWords();
         isStatic = methodDef.isStatic();
 
         long analysisTime = System.currentTimeMillis();
         this.analyzer = new DexCodeAnalyzer(method.getMethodBody());
         this.analyzer.analyze();
         analysisTime = System.currentTimeMillis() - analysisTime;
 
         totalAnalysisTime += analysisTime;
 
         Runtime runtime = Runtime.getRuntime();
         long usedMemory = runtime.totalMemory() - runtime.freeMemory();
         if (INFO) {
             System.out.println("===2=== LivenessTime:" + analyzer.time + ", AnalysisTime:" + analysisTime
                                + ", Code Size:" + analyzer.getMaxInstructionIndex()
                                + ", Memory:" + usedMemory);
         }
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
         long time = System.currentTimeMillis();
 
         DalvCodeBridge translatedCode = new DalvCodeBridge(processMethod(method.getMethodBody()), method);
 
         // Need to intern instructions to the new dexFile, as they are from a different dex file
         Instruction[] tmpInstructions = translatedCode.getInstructions();
         List<Instruction> instructions = null;
         if (tmpInstructions != null) {
             instructions = new ArrayList<Instruction>();
             for(Instruction inst : tmpInstructions) {
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
 
         time = System.currentTimeMillis() - time;
 //	    System.out.println("Translation time: " + time);
         totalCGTime += time;

         return CodeItem.internCodeItem(dexFile, registerCount, inWords, outWords, /* debugInfo */ null, instructions, newTries, newCatchHandlers);
 
     }
 
     private DalvCode processMethod(DexCode code) {
         if (code == null)
             return null;
 
         RopMethod rmeth = toRop(code);
         if (DEBUG) {
             System.out.println("==== Before Optimization ====");
             dump(rmeth);
         }
         long time = System.currentTimeMillis();
         rmeth = Optimizer.optimize(rmeth, inWords, isStatic, false, DexTranslationAdvice.THE_ONE);
         if (DEBUG) {
             System.out.println("==== After Optimization ====");
             dump(rmeth);
         }
 
         DalvCode dcode = RopTranslator.translate(rmeth, PositionList.NONE, null, inWords, dexOptions);
         time = System.currentTimeMillis() - time;
         totalDxTime += time;
 
         return dcode;
     }
 
     private RopMethod toRop(DexCode code) {
 
         // Build basic blocks
         ArrayList<ArrayList<AnalyzedDexInstruction>> basicBlocks = buildBasicBlocks();
 
         // Convert basicBlocks, hold the result in the temporary map. It is indexed by the basic block's first AnalyzedInst.
         HashMap<AnalyzedDexInstruction, ArrayList<Insn>> translatedBasicBlocks = new HashMap<AnalyzedDexInstruction, ArrayList<Insn>>();
         HashMap<AnalyzedDexInstruction, DexConvertedResult> translatedBasicBlocksInfo = new HashMap<AnalyzedDexInstruction, DexConvertedResult>();
 
         translateBasicBlocks(basicBlocks, translatedBasicBlocks, translatedBasicBlocksInfo);
 
         // Finally convert to ROP's BasicBlockList form from convertedBasicBlocks
         return createRopMethod(translatedBasicBlocks, translatedBasicBlocksInfo);
     }
 
     private void translateBasicBlocks(
         ArrayList<ArrayList<AnalyzedDexInstruction>> basicBlocks,
         HashMap<AnalyzedDexInstruction, ArrayList<Insn>> translatedBasicBlocks,
         HashMap<AnalyzedDexInstruction, DexConvertedResult> translatedBasicBlocksInfo) {
 
         DexInstructionTranslator translator = new DexInstructionTranslator(analyzer, method.getMethodBody().getInstructionList());
 
         for(int bi=0; bi< basicBlocks.size(); bi++)
             translatedBasicBlocks.put(basicBlocks.get(bi).get(0), new ArrayList<Insn>());
 
         // In case we need more basic blocks hence more dummy AnalyzedDexInstruction instance,
         // we use this index incrementally.
         int dummyInstructionIndex = analyzer.getMaxInstructionIndex() + 1;
 
         for(int bi=0; bi< basicBlocks.size(); bi++) {
             ArrayList<AnalyzedDexInstruction> basicBlock = basicBlocks.get(bi);
             AnalyzedDexInstruction bbIndex = basicBlock.get(0);
 
             // Process instruction in the basic block as a whole,
             ArrayList<Insn> insnBlock = translatedBasicBlocks.get(bbIndex);
             DexConvertedResult lastInsn = null;
             for(int i = 0; i < basicBlock.size(); i++) {
                 AnalyzedDexInstruction inst = basicBlock.get(i);
                 if (DEBUG && inst.getInstruction() != null) {
                     System.out.println(inst.getInstruction());
                 }
                 lastInsn = translator.translate(inst);
                 insnBlock.addAll(lastInsn.insns);
 
                 if (i != basicBlock.size() - 1) {
                     // auxInsn can only appear at the tail of a bb (move-result-pseudo etc)
                     assert lastInsn.auxInsns.size() == 0;
                     // Verify instructions in basic block is indeed chaining together
                     assert lastInsn.primarySuccessor == basicBlock.get( i + 1);
                 } else if (lastInsn.auxInsns.size() != 0) {
                     // Need to create an extra basic block to accommodate auxInsns
                     AnalyzedDexInstruction extraBB_head = new AnalyzedDexInstruction(dummyInstructionIndex++,
                             null, null);
                     DexConvertedResult extraBB_Info = new DexConvertedResult();
 
                     // Chain this extra BB to original BB's primary successor.
                     extraBB_Info.primarySuccessor = lastInsn.primarySuccessor;
                     extraBB_Info.addSuccessor(lastInsn.primarySuccessor);
 
                     // Let the original BB point to us
                     for(int j = 0; j<lastInsn.successors.size(); j++)
                         if (lastInsn.successors.get(j) == lastInsn.primarySuccessor)
                             lastInsn.successors.set(j, extraBB_head);
                     lastInsn.primarySuccessor = extraBB_head;
 
                     translatedBasicBlocks.put(extraBB_head, lastInsn.auxInsns);
                     translatedBasicBlocksInfo.put(extraBB_head, extraBB_Info);
                 }
 
                 if (DEBUG && lastInsn.insns.size() > 0) {
                     System.out.print("    --> ");
                     System.out.print(lastInsn.insns.get(0).toHuman());
                     if (lastInsn.insns.size() > 1)
                         System.out.print("...");
                     System.out.println();
                 }
             }
 
             // Add move-params to the beginning of the first block
             if (bi == 0) {
                 int paramOffset = 0;
                 int insnIndex = 0;
                 for(Parameter param : method.getMethodBody().getParameters()) {
                     int regIndex = analyzer.normalizeRegister(param.getRegister());
                     Type paramType = Type.intern(param.getType().getDescriptor());
 
                     Insn insn = new PlainCstInsn(Rops.opMoveParam(paramType), SourcePosition.NO_INFO,
                                                  RegisterSpec.make(regIndex, paramType),
                                                  RegisterSpecList.EMPTY,
                                                  CstInteger.make(paramOffset));
 
                     insnBlock.add(insnIndex++, insn);
                     paramOffset += param.getType().getRegisters();
                 }
             }
 
             translatedBasicBlocksInfo.put(bbIndex, lastInsn);
         }
     }
 
     private RopMethod createRopMethod(
         HashMap<AnalyzedDexInstruction, ArrayList<Insn>> translatedBasicBlocks,
         HashMap<AnalyzedDexInstruction, DexConvertedResult> translatedBasicBlocksInfo) {
 
         BasicBlockList ropBasicBlocks = new BasicBlockList(translatedBasicBlocks.size());
         int bbIndex = 0;
 
         for(AnalyzedDexInstruction head : translatedBasicBlocks.keySet()) {
             ArrayList<Insn> insnBlock = translatedBasicBlocks.get(head);
             DexConvertedResult lastInsn = translatedBasicBlocksInfo.get(head);
 
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
             for(int i=0 ; i<insnBlock.size(); i++)
                 insns.set(i, insnBlock.get(i));
             insns.setImmutable();
 
             IntList successors = new IntList();
             for(AnalyzedDexInstruction s : lastInsn.successors) {
                 // Make sure the successor is in the basic block list
                 assert translatedBasicBlocks.get(s) != null;
                 successors.add(s.getInstructionIndex());
             }
             successors.setImmutable();
 
             // Make sure primary Successor is valid as well.
             assert lastInsn.primarySuccessor == null || translatedBasicBlocks.get(lastInsn.primarySuccessor) != null;
 
             int label = head.getInstructionIndex();
             BasicBlock ropBasicBlock = new BasicBlock(label, insns, successors, lastInsn.primarySuccessor != null ? lastInsn.primarySuccessor.getInstructionIndex() : -1);
             ropBasicBlocks.set(bbIndex++, ropBasicBlock);
         }
 
 
         return new RopMethod(ropBasicBlocks, analyzer.getStartOfMethod().getOnlySuccesor().getInstructionIndex());
     }
 
     private boolean endsBasicBlock(AnalyzedDexInstruction current) {
         if (current.getSuccessorCount() != 1)
             return true; // More than one successor, guaranteed to end a BB
 
         if (current.getInstruction() == null)
             return false; // Pseudo instructions like DexLabel, etc
 
         if (current.getInstruction().cfgEndsBasicBlock())
             return true; // Sufficient condition
 
         return false;
     }
 
     private ArrayList<ArrayList<AnalyzedDexInstruction>> buildBasicBlocks() {
         ArrayList<ArrayList<AnalyzedDexInstruction>> basicBlocks = new ArrayList<ArrayList<AnalyzedDexInstruction>>();
 
         Stack<AnalyzedDexInstruction> leads = new Stack<AnalyzedDexInstruction>();
         leads.push(analyzer.getStartOfMethod().getOnlySuccesor());
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
                 AnalyzedDexInstruction next = current.getOnlySuccesor();
                 if (next.getPredecessorCount() == 1) {
                     block.add(next);
                     current = next;
                 } else
                     break;
             }
 
             //TODO: To be deleted??
             // Tweak Switch instruction's successors, collapsing the SwitchData that follows it
 //        	if (current.getInstruction() instanceof DexInstruction_Switch) {
 //        		DexLabel switchLabel = ((DexInstruction_Switch)current.instruction).getSwitchTable();
 //        		assert current.getSuccessorCount() == 2;
 //        		for (AnalyzedDexInstruction successor : current.getSuccesors()) {
 //        			if (successor.auxillaryElement != switchLabel) { // This is the default case successor
 //        				leads.push(successor);
 //        			} else { // This is a DexLabel, which is followed by SwitchData
 //        				for (AnalyzedDexInstruction switchSuccessor : successor.getOnlySuccesor().getSuccesors())
 //        					leads.push(switchSuccessor);
 //        			}
 //        		}
 //        	} else if (current.getInstruction() instanceof DexInstruction_FillArrayData) {
 //        		// Collapse the following DexLabel and DexInstruction_FilledArrayData
 //        		AnalyzedDexInstruction next = current.getOnlySuccesor(); // This a DexLabel
 //        		leads.push(next.getOnlySuccesor().getOnlySuccesor()); // Whatever follows FilledArrayData
 //        	} else {
             // Add successors of current to the to-be-visit stack
             for(AnalyzedDexInstruction i : current.getSuccesors())
                 leads.push(i);
 
 //        	}
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
 
 }
 
