 package sw10.animus.analysis;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import net.sf.javailp.Constraint;
 import net.sf.javailp.Linear;
 import net.sf.javailp.Operator;
 import net.sf.javailp.OptType;
 import net.sf.javailp.Problem;
 import net.sf.javailp.Result;
 import net.sf.javailp.Solver;
 import net.sf.javailp.SolverFactory;
 import net.sf.javailp.SolverFactoryLpSolve;
 import sw10.animus.analysis.loopanalysis.CFGLoopAnalyzer;
 import sw10.animus.build.AnalysisEnvironment;
 import sw10.animus.build.JVMModel;
 import sw10.animus.program.AnalysisSpecification;
 import sw10.animus.reports.ReportGenerator;
 import sw10.animus.util.Util;
 import sw10.animus.util.annotationextractor.extractor.AnnotationExtractor;
 import sw10.animus.util.annotationextractor.parser.Annotation;
 
 import com.ibm.wala.classLoader.CallSiteReference;
 import com.ibm.wala.classLoader.IBytecodeMethod;
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.shrikeCT.InvalidClassFileException;
 import com.ibm.wala.ssa.IR;
 import com.ibm.wala.ssa.ISSABasicBlock;
 import com.ibm.wala.ssa.SSAInstruction;
 import com.ibm.wala.ssa.SSAInvokeInstruction;
 import com.ibm.wala.types.MethodReference;
 import com.ibm.wala.types.TypeName;
 import com.ibm.wala.util.WalaException;
 import com.ibm.wala.util.collections.Iterator2Iterable;
 import com.ibm.wala.util.collections.Pair;
 import com.ibm.wala.util.graph.Graph;
 import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;
 import com.ibm.wala.util.graph.traverse.BFSIterator;
 import com.ibm.wala.util.intset.IntIterator;
 import com.ibm.wala.util.intset.IntSet;
 
 public class Analyzer {
 
 	private AnalysisSpecification specification;
 	private AnnotationExtractor extractor;
 	private ICostComputer<ICostResult> costComputer;
 	private AnalysisEnvironment environment;
 	private AnalysisResults results;
 	
 	private Analyzer() {
 		this.environment = AnalysisEnvironment.getAnalysisEnvironment();
 		this.specification = AnalysisSpecification.getAnalysisSpecification();
 		this.extractor = new AnnotationExtractor();		
 		this.results = AnalysisResults.getAnalysisResults();
 	}
 
 	public static Analyzer makeAnalyzer() {
 		return new Analyzer();
 	}
 
 	public void start(Class<? extends ICostComputer<ICostResult>> costComputerType) throws InstantiationException, IllegalAccessException, IllegalArgumentException, WalaException, IOException, SecurityException, InvocationTargetException, NoSuchMethodException {
 		this.costComputer = costComputerType.getDeclaredConstructor(JVMModel.class).newInstance(specification.getJvmModel());
 
 		LinkedList<CGNode> entryCGNodes = specification.getEntryPointCGNodes();	
 		
 		for(CGNode entryNode : entryCGNodes) {
 			ICostResult results = analyzeNode(entryNode);
 			CostResultMemory memRes = (CostResultMemory)results;				
 			System.out.println("Worst case allocation for " + entryNode.getMethod().toString() + ":" + results.getCostScalar());
 			for(Entry<TypeName, Integer> i : memRes.countByTypename.entrySet()) {
 				System.out.println("\t" + i.getKey().toString() + " - " + i.getValue());
 			}
 		}
 		
 		/*
 		if (entryPoints == null) {
 			CGNode entryNode = environment.getCallGraph().getEntrypointNodes().iterator().next();
 			this.entryPoints.add(entryNode);
 			System.out.println(entryNode.getMethod().toString());
 			ICostResult results = analyzeNode(entryNode);
 			System.out.println("Worst case allocation:" + results.getCostScalar());
 		}
 		else
 		{
 			for(String entryPoint : entryPoints) {
 				MethodReference mr = StringStuff.makeMethodReference(entryPoint);
 				CGNode entryNode = null;
 				try {
 					entryNode = environment.getCallGraph().getNodes(mr).iterator().next();
 					this.entryPoints.add(entryNode);
 				}
 				catch (NoSuchElementException e) {
 					System.err.println(entryPoint + " is not part of the call graph!");
 					continue;
 				}
 				ICostResult results = analyzeNode(entryNode);
 				CostResultMemory memRes = (CostResultMemory)results;				
 				System.out.println("Worst case allocation for " + entryNode.getMethod().toString() + ":" + results.getCostScalar());
 				for(Entry<TypeName, Integer> i : memRes.countByTypename.entrySet()) {
 					System.out.println("\t" + i.getKey().toString() + " - " + i.getValue());
 				}
 			}
 		}
 		*/
 		
 		ReportGenerator gen = new ReportGenerator();
 		gen.Generate(AnalysisResults.getAnalysisResults().getReportEntries());
 	}
 
 	public ICostResult analyzeNode(CGNode cgNode) {
 		if (results.isNodeProcessed(cgNode)) {
 			return results.getResultsForNode(cgNode);
 		}
 		
 		IMethod method = cgNode.getMethod();
 		IR ir = cgNode.getIR();
 		
 		Pair<SlowSparseNumberedLabeledGraph<ISSABasicBlock, String>, Map<String, Pair<Integer, Integer>>> sanitized = null;
 		try {
 			sanitized = Util.sanitize(ir, environment.getClassHierarchy());
 		} catch (IllegalArgumentException e) {
			
 		} catch (WalaException e) {
			
 		}
 		SlowSparseNumberedLabeledGraph<ISSABasicBlock, String> cfg = sanitized.fst;
 		Map<String, Pair<Integer, Integer>> edgeLabelToNodesIDs = sanitized.snd;
 
 		Map<Integer, Annotation> annotationByLineNumber = getAnnotations(method);
 		Map<Integer, ArrayList<Integer>> loopBlocksByHeaderBlockId = getLoops(cfg, ir.getControlFlowGraph().entry());
 
 		/* LPSolver */
 		SolverFactory factory = new SolverFactoryLpSolve();
 		factory.setParameter(Solver.VERBOSE, 0);
 		Problem problem = new Problem();
 		Linear objective = new Linear();
 		Linear linear;
 		Constraint constraint;
 		String variable;
 
 		BFSIterator<ISSABasicBlock> iteratorBFSOrdering = new BFSIterator<ISSABasicBlock>(cfg);
 		Map<Integer, ICostResult> calleeNodeResultsByBlockGraphId = new HashMap<Integer, ICostResult>();
 		Set<ICostResult> calleeNodeResultsAlreadyFound = new HashSet<ICostResult>();
 		
 		ICostResult intermediateResults = null;
 
 		while(iteratorBFSOrdering.hasNext()) {
 			ISSABasicBlock currentBlock = iteratorBFSOrdering.next();
 			variable = "bb" + currentBlock.getGraphNodeId();
 			objective.add(1, variable);
 			problem.setVarType(variable, Integer.class);
 
 			Iterator<? extends String> IteratorOutgoingLabels = (Iterator<? extends String>)cfg.getSuccLabels(currentBlock);
 			Iterator<? extends String> IteratorIncomingLabels = (Iterator<? extends String>)cfg.getPredLabels(currentBlock);
 			List<String> outgoing = new ArrayList<String>();
 			List<String> incoming = new ArrayList<String>();
 
 			while (IteratorOutgoingLabels.hasNext()) {
 				String edgeLabel = IteratorOutgoingLabels.next();
 				outgoing.add(edgeLabel);
 				problem.setVarType(edgeLabel, Integer.class);
 			}
 
 			while (IteratorIncomingLabels.hasNext()) {
 				String edgeLabel = IteratorIncomingLabels.next();
 				incoming.add(edgeLabel);
 				problem.setVarType(edgeLabel, Integer.class);
 			}
 
 			if (currentBlock.isEntryBlock()) {
 				linear = new Linear();
 				linear.add(1, "f0");
 				constraint = new Constraint(linear, Operator.EQ, 1);
 				problem.add(constraint);
 				problem.setVarType("f0", Integer.class);
 				linear = new Linear();
 				linear.add(0, "f0");
 				linear.add(-1, "bb0");
 				constraint = new Constraint(linear, Operator.EQ, 0);		
 				problem.add(constraint);
 			}
 			else if (currentBlock.isExitBlock()) { 
 				Linear alloc = new Linear();
 				Linear flow = new Linear();
 				variable = "bb" + currentBlock.getGraphNodeId();
 				alloc.add(-1, variable);
 				Iterator<String> IteratorIncoming = incoming.iterator();
 				while(IteratorIncoming.hasNext()) {
 					String incommingLabel = IteratorIncoming.next();
 					flow.add(1, incommingLabel);
 					alloc.add(0, incommingLabel);
 				} 			
 				constraint = new Constraint(alloc, Operator.EQ, 0);
 				problem.add(constraint);
 				constraint = new Constraint(flow, Operator.EQ, 1);
 				problem.add(constraint);
 			}
 			else
 			{
 				ICostResult costForBlock = analyzeBasicBlock(currentBlock, cgNode);
 				if (costForBlock != null) {
 					
 					if (costForBlock.isFinalNodeResult() && !calleeNodeResultsAlreadyFound.contains(costForBlock)) {
 						calleeNodeResultsByBlockGraphId.put(currentBlock.getGraphNodeId(), costForBlock);
 						calleeNodeResultsAlreadyFound.add(costForBlock);
 					}
 					
 					if (intermediateResults != null) {
 						if (costForBlock.isFinalNodeResult()) {
 							costComputer.addCost(costForBlock, intermediateResults);	
 						}
 						else
 						{
 							costComputer.addCostAndContext(costForBlock, intermediateResults);
 						}
 						
 					}
 					else {
 						if (costForBlock.isFinalNodeResult()) {
 							intermediateResults = costForBlock.clone();
 						}
 						else {
 							intermediateResults = costForBlock.cloneTemporaryResult();
 						}
 												
 					}
 				}
 
 				Linear flow = new Linear();
 				Linear alloc = new Linear();
 				Linear loop = new Linear();
 
 				for (String incomingLabel : incoming) {
 					flow.add(1, incomingLabel);
 					if (costForBlock != null) {
 						alloc.add(costForBlock.getCostScalar(), incomingLabel);
 					}
 					else {
 						alloc.add(0, incomingLabel);
 					}
 				}
 
 				for (String outgoingLabel : outgoing) {
 					flow.add(-1, outgoingLabel);
 				}
 
 				if (loopBlocksByHeaderBlockId.containsKey(currentBlock.getGraphNodeId())) {
 					ArrayList<Integer> loopBlocks = loopBlocksByHeaderBlockId.get(currentBlock.getGraphNodeId());
 					IntSet loopHeaderSuccessors = cfg.getSuccNodeNumbers(currentBlock);
 					IntSet loopHeaderAncestors = cfg.getPredNodeNumbers(currentBlock);
 
 					int lineNumberForLoop = 0;
 					String boundForLoop = "";
 					try {
 						IBytecodeMethod bytecodeMethod = (IBytecodeMethod)cgNode.getMethod();
 						lineNumberForLoop = bytecodeMethod.getLineNumber(bytecodeMethod.getBytecodeIndex(currentBlock.getFirstInstructionIndex()));
 						if (annotationByLineNumber == null || !annotationByLineNumber.containsKey(lineNumberForLoop)) {
 							System.err.println("No bound for loop detected in " + method.getName());
 						} else {
 							boundForLoop = annotationByLineNumber.get(lineNumberForLoop).getAnnotationValue();
 						}
 					} catch (InvalidClassFileException e) {
 					}    	
 
 					for(int i : loopBlocks) {
 						if (loopHeaderSuccessors.contains(i)) {
 							loop.add(-1, cfg.getEdgeLabels(currentBlock, cfg.getNode(i)).iterator().next());
 							break;
 						}
 					}
 
 					IntIterator ancestorGraphIds = loopHeaderAncestors.intIterator();
 					while (ancestorGraphIds.hasNext()) {
 						int ancestorID = ancestorGraphIds.next();
 						if (!loopBlocks.contains(ancestorID)) {
 							loop.add(Integer.parseInt(boundForLoop), cfg.getEdgeLabels(cfg.getNode(ancestorID), currentBlock).iterator().next());
 						}
 					}
 
 					constraint = new Constraint(loop, Operator.EQ, 0);
 					problem.add(constraint);
 				}
 
 				constraint = new Constraint(flow, Operator.EQ, 0);
 				problem.add(constraint);
 
 				alloc.add(-1, "bb" + currentBlock.getGraphNodeId());
 				constraint = new Constraint(alloc, Operator.EQ, 0);
 				problem.add(constraint);  			
 			}
 		}
 
 		problem.setObjective(objective, OptType.MAX);
 		Solver solver = factory.get();
 		Result result = solver.solve(problem);
 				
 		ICostResult finalResults;
 		finalResults = costComputer.getFinalResultsFromContextResultsAndLPSolutions(intermediateResults, result, 
 				problem, edgeLabelToNodesIDs, calleeNodeResultsByBlockGraphId, cgNode);
 		results.saveResultForNode(cgNode, finalResults);
 	
 		return finalResults;
 	}
 
 	private ICostResult analyzeBasicBlock(ISSABasicBlock block, CGNode node) {
 		ICostResult costForBlock = null;
 
 		for(SSAInstruction instruction : Iterator2Iterable.make(block.iterator())) {
 			ICostResult costForInstruction = analyzeInstruction(instruction, block, node);
 
 			if (costForInstruction != null) {
 				if (costForBlock != null) {
 					costComputer.addCostAndContext(costForInstruction, costForBlock);
 				}
 				else {
 					costForBlock = costForInstruction;
 				}
 			}
 		}
 
 		return costForBlock;
 	}
 
 	private ICostResult analyzeInstruction(SSAInstruction instruction, ISSABasicBlock block, CGNode node) {
 		ICostResult costForInstruction = null;
 
 		if(instruction instanceof SSAInvokeInstruction) {
 			SSAInvokeInstruction inst = (SSAInvokeInstruction)instruction;
 			if(inst.isDispatch()) {	// invokevirtual
 				CallSiteReference callSiteRef = inst.getCallSite();
 				Set<CGNode> possibleTargets = environment.getCallGraph().getPossibleTargets(node, callSiteRef);
 				ICostResult maximumResult = null;
 				ICostResult tempResult = null;
 				for(CGNode target : Iterator2Iterable.make(possibleTargets.iterator())) {
 					tempResult = analyzeNode(target);
 					if(maximumResult == null || tempResult.getCostScalar() > maximumResult.getCostScalar())
 						maximumResult = tempResult;
 				}
 				return maximumResult;
 			} else { // invokestatic or invokespecial
 				MethodReference targetRef = inst.getDeclaredTarget();
 				Set<CGNode> targets = environment.getCallGraph().getNodes(targetRef);
 				CGNode target = targets.iterator().next();
 				return analyzeNode(target);
 			}
 		} else if(costComputer.isInstructionInteresting(instruction)) {
 			costForInstruction = costComputer.getCostForInstructionInBlock(instruction, block, node);
 		}
 
 		return costForInstruction;
 	}
 
 	private Map<Integer, ArrayList<Integer>> getLoops(Graph<ISSABasicBlock> graph, ISSABasicBlock entry) {
 		CFGLoopAnalyzer loopAnalyzer = CFGLoopAnalyzer.makeAnalyzerForCFG(graph);
 		loopAnalyzer.runDfsOrdering(entry);
 
 		return loopAnalyzer.getLoopHeaderBasicBlocksGraphIds();
 	}
 
 	private Map<Integer, Annotation> getAnnotations(IMethod method) {
 		IClass declaringClass = method.getDeclaringClass();
 		String packageName = declaringClass.getName().toString();
 		packageName = Util.getClassNameOrOuterMostClassNameIfNestedClass(packageName);
 		packageName = (packageName.contains("/") ? packageName.substring(1, packageName.lastIndexOf('/')) : "");
 
 		String path = specification.getSourceFilesRootDir() + '/';
 		path = (packageName.isEmpty() ? path : path + packageName + '/');
 
 		String sourceFileName = declaringClass.getSourceFileName();
 		Map<Integer, Annotation> annotationsForMethod = null;
 		try {
 			annotationsForMethod = extractor.retrieveAnnotations(path, sourceFileName);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return annotationsForMethod;
 	}
 }
 
