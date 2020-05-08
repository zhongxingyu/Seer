 package com.rx201.dx.translator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import uk.ac.cam.db538.dexter.analysis.cfg.CfgBasicBlock;
 import uk.ac.cam.db538.dexter.analysis.cfg.CfgBlock;
 import uk.ac.cam.db538.dexter.analysis.cfg.ControlFlowGraph;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode.Parameter;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
 import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
 import uk.ac.cam.db538.dexter.dex.method.DexMethod;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 
 public class DexCodeAnalyzer {
 	private DexCode code;
 	private DexMethod method;
 
     private HashMap<DexCodeElement, AnalyzedDexInstruction> instructionMap;
     private ArrayList<AnalyzedDexInstruction> instructions;
     private ArrayList<AnalyzedBasicBlock> basicBlocks;
 
     private static final int NOT_ANALYZED = 0;
     private static final int ANALYZED = 1;
     
     private int analyzerState = NOT_ANALYZED;
 
     private int maxInstructionIndex;
     
     //This is a dummy instruction that occurs immediately before the first real instruction. We can initialize the
     //register types for this instruction to the parameter types, in order to have them propagate to all of its
     //successors, e.g. the first real instruction, the first instructions in any exception handlers covering the first
     //instruction, etc.
     private AnalyzedDexInstruction startOfMethod;
 
     private AnalyzedBasicBlock startBasicBlock;
    
     public long time;
     
     private int usedIndexSlots;
 	private final HashMap<DexRegister, Integer> registerIndexer;
 
     
     public DexCodeAnalyzer(DexMethod method) {
         this.method = method;
         basicBlocks = new ArrayList<AnalyzedBasicBlock>();
         maxInstructionIndex = 0;
         buildInstructionList();
         AnalyzedDexInstruction.hierarchy = code.getHierarchy();
         
         registerIndexer = new HashMap<DexRegister, Integer>();
         usedIndexSlots = 0;
     }
 
     public boolean isAnalyzed() {
         return analyzerState >= ANALYZED;
     }
 
     private void analyzeParameters() {
     	for(Parameter param : method.getMethodBody().getParameters()) {
     		RopType regType = RopType.getRopType(param.getType());
     		startOfMethod.defineRegister(param.getRegister(), regType, true);
     	}
     }
     
     // Perform type propagation.
     public void analyze() {
         if (analyzerState == ANALYZED) {
             //the instructions have already been analyzed, so there is nothing to do
             return;
         }
 
         // Collect use/def information; initialise all TypeResolver sites.
         buildUseDefSets();
         
         // Initialise TypeResolver of StartOfMethod, add constraints from function declaration
         analyzeParameters();
 
         time = System.currentTimeMillis();
         // Compute all use-def chains and link TypeSolver together accordingly
         livenessAnalysis();
         time  = System.currentTimeMillis() - time;
         
         // Add constraints from uses and defs to TypeSolver
         typeConstraintAnalysis();
         
         analyzerState = ANALYZED;
     }
 
 
 	private void buildUseDefSets() {
 		DexInstructionAnalyzer analyzer = new DexInstructionAnalyzer(method);
 		
 		// First collect use/def information
 		for (AnalyzedDexInstruction inst : instructions) {
 			if (inst.instruction != null) {
 				analyzer.setAnalyzedInstruction(inst);
 				inst.instruction.accept(analyzer);
 			}
 		}
 	}
 
 	private void livenessAnalysis() {
 		for(AnalyzedBasicBlock basicBlock : basicBlocks) {
 			basicBlock.analyzeLiveness();
 		}
 		
 		for (AnalyzedBasicBlock basicBlock : basicBlocks) {
 			for(DexRegister usedReg : basicBlock.getUsedRegisters()) {
 				Set<TypeSolver> definers = getDefinedSites(basicBlock, usedReg);
 				TypeSolver master = null;
 				for(TypeSolver definer : definers) {
 					if (master == null)
 						master = definer;
 					else
 						master.unify(definer);
 				}
 				assert master != null;
 				basicBlock.associateDefinitionSite(usedReg, master);
 			}
 		}
 		
 		// Create the register constraint graph
 		for (AnalyzedDexInstruction inst : instructions) {
 			inst.createConstraintEdges();
 		}
 	}
 	
 	private Set<TypeSolver> getDefinedSites(AnalyzedBasicBlock basicBlock, DexRegister usedReg) {
 		HashSet<TypeSolver> result = new HashSet<TypeSolver>();
 		
 		HashSet<AnalyzedBasicBlock> visitedNormal = new HashSet<AnalyzedBasicBlock>();
 		HashSet<AnalyzedBasicBlock> visitedException = new HashSet<AnalyzedBasicBlock>();
 		ArrayList<Pair<AnalyzedBasicBlock, Boolean>> stack = new ArrayList<Pair<AnalyzedBasicBlock, Boolean>>();
 		for(AnalyzedBasicBlock pred : basicBlock.predecessors )
 			stack.add(new Pair<AnalyzedBasicBlock, Boolean>(pred, basicBlock.isExceptionPredecessor(pred)));
 		
 		while(stack.size() > 0) {
 			Pair<AnalyzedBasicBlock, Boolean> headPair = stack.remove(stack.size() - 1);
 			AnalyzedBasicBlock head = headPair.getValA();
 			Boolean isExceptionPath = headPair.getValB();
 			
 			if (isExceptionPath) {
 				if (visitedException.contains(head)) continue;
 				visitedException.add(head);
 			} else {
 				if (visitedNormal.contains(head)) continue;
 				visitedNormal.add(head);
 			}
 			
 			TypeSolver definer = head.getDefinedRegisterSolver(usedReg);
 			if (definer != null && (!isExceptionPath)) {
 				result.add(definer);
 			} else {
 				// If this register is also accessed here in the current path
 				// and we've obtained its definer, we can reuse the result.
 				// This would be most efficient if we perform liveness analysis
 				// from top to bottom.
 //				definer = head.getUsedRegisterTypeSolver(usedReg);
 //				if (definer != null) {
 //					result.add(definer);
 //				} else {
 					for(AnalyzedBasicBlock pred : head.predecessors )
 						stack.add(new Pair<AnalyzedBasicBlock, Boolean>(pred, head.isExceptionPredecessor(pred)));
 //				}
 			}
 		}
 		return result;
 	}
 	
 	private void typeConstraintAnalysis() {
 		// First add all definition constraints,
 		// then refine it with usage constraints
 		startOfMethod.initDefinitionConstraints();
 		for (AnalyzedDexInstruction inst : instructions) {
 			inst.initDefinitionConstraints();
 		}
 		for (AnalyzedDexInstruction inst : instructions) {
 			inst.propagateUsageConstraints();
 		}
 	}
 
 
 	public AnalyzedDexInstruction getStartOfMethod() {
         return startOfMethod;
     }
 
     private void buildInstructionList() {
     	instructions = new ArrayList<AnalyzedDexInstruction>();
     	instructionMap = new HashMap<DexCodeElement, AnalyzedDexInstruction>();
     	for(DexCodeElement inst : code.getInstructionList()) {
     		AnalyzedDexInstruction analyzedInst = buildFromDexCodeElement(instructions.size(), inst);
     		instructionMap.put(inst, analyzedInst);
     		instructions.add(analyzedInst);
     		
     	}
 
     	ControlFlowGraph cfg = new ControlFlowGraph(code);
     	HashMap<AnalyzedDexInstruction, AnalyzedBasicBlock> basicBlockMap = new HashMap<AnalyzedDexInstruction, AnalyzedBasicBlock>();
     	
     	for(CfgBasicBlock bb : cfg.getBasicBlocks()) {
         	AnalyzedDexInstruction prevA = null;
         	List<? extends DexCodeElement> prevExceptionSuccessors = null;
         	AnalyzedBasicBlock analyzedBB = new AnalyzedBasicBlock();
         	
         	// Connect predecessor/successor within a basic block
     		for(DexCodeElement cur: bb.getInstructions()) {
     			AnalyzedDexInstruction curA = instructionMap.get(cur);
     			analyzedBB.addInstruction(curA);
     			
     			if (prevA != null) {
     				prevA.linkToSuccessor(curA, false);
     				// Cannot have exception path within a basic block
     				assert !prevExceptionSuccessors.contains(curA.getCodeElement());
     			}
     			prevA = curA;
     			prevExceptionSuccessors = prevA.getCodeElement().cfgGetExceptionSuccessors(code.getInstructionList());
     		}
     		
     		basicBlockMap.put(analyzedBB.first, analyzedBB);
     		basicBlocks.add(analyzedBB);
     		
     		// Connect with successor basic block
     		for(CfgBlock nextBB : bb.getSuccessors()) {
     			if (nextBB instanceof CfgBasicBlock) {
     				DexCodeElement cur = ((CfgBasicBlock)nextBB).getFirstInstruction();
         			AnalyzedDexInstruction curA = instructionMap.get(cur);
     				prevA.linkToSuccessor(curA, prevExceptionSuccessors.contains(curA.getCodeElement()));
     			}
     		}
     	}
     	
     	//Connect basic blocks together, we can only do this once the basicBlockMap is complete
     	for(AnalyzedBasicBlock basicBlock : basicBlocks) {
     		for(AnalyzedDexInstruction successor : basicBlock.last.getSuccesors()) {
     			boolean exceptionPath = successor.isExceptionPredecessor(basicBlock.last);
     			basicBlock.linkToSuccessor(basicBlockMap.get(successor), exceptionPath);
     		}
     	}
     	
         //override AnalyzedInstruction and provide custom implementations of some of the methods, so that we don't
         //have to handle the case this special case of instruction being null, in the main class
         startOfMethod = new AnalyzedDexInstruction(-1, null);
         startBasicBlock = new AnalyzedBasicBlock(startOfMethod);
         basicBlocks.add(startBasicBlock);
         
         for (CfgBlock startBB: cfg.getStartBlock().getSuccessors()) {
         	if (startBB instanceof CfgBasicBlock) {
         		AnalyzedDexInstruction realHead = instructionMap.get(((CfgBasicBlock)startBB).getFirstInstruction());
         		startOfMethod.linkToSuccessor(realHead, false);
         		startBasicBlock.linkToSuccessor(basicBlockMap.get(realHead), false);
         	}
         }
     }
 
     private AnalyzedDexInstruction buildFromDexCodeElement(int index, DexCodeElement element) {
     	if (index > maxInstructionIndex)
     		maxInstructionIndex = index;
     	if (element instanceof DexInstruction) {
     		return new AnalyzedDexInstruction(index, (DexInstruction) element);
     	} else /* DexCatch, DexCatchAll, DexLabel, DexTryBlockStart, DexTryBlockEnd */ {
     		return new AnalyzedDexInstruction(index, null, element);
     	}
 	}
 
 
     public AnalyzedDexInstruction reverseLookup(DexCodeElement element) {
     	assert element != null;
     	return instructionMap.get(element);
     }
     
     public int getMaxInstructionIndex() {
     	return maxInstructionIndex;
     }
     
     public int normalizeRegister(DexRegister reg) {
     	if (registerIndexer.containsKey(reg))
     		return registerIndexer.get(reg);
     	
     	int index = usedIndexSlots;
     	registerIndexer.put(reg, index);
     	usedIndexSlots += reg.getWidth().getRegisterCount();
     	return index;
     }
 }
