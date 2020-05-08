 package org.aidos.tree;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.aidos.tree.analyzer.BytecodeAnalyzer;
 import org.aidos.tree.analyzer.flow.FlowAnalyzer;
 import org.aidos.tree.analyzer.flow.FlowBlock;
 import org.aidos.tree.node.MethodDecNode;
 import org.aidos.tree.util.InstructionPointer;
 
 /**
  * Represents our abstract syntax tree.
  * @author `Discardedx2
  */
 public class NodeTree implements Iterable<ClassFile> {
 
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = Logger.getLogger(NodeTree.class.getName());
 	/**
 	 * The jar file to load.
 	 */
 	private Jar jar;
 	/**
 	 * The loaded nodes.
 	 */
 	private Map<String, ClassFile> classFiles = new HashMap<String, ClassFile>();
 	/**
 	 * The bytecode analyzers.
 	 */
 	private List<BytecodeAnalyzer<?>> analyzers = new ArrayList<BytecodeAnalyzer<?>>();
 
 	/**
 	 * Builds a new {@link NodeTree} hierarchy.
 	 * @param jar The jar to load from.
 	 */
 	public NodeTree(Jar jar) {
 		this.jar = jar;
 		analyzers.add(new FlowAnalyzer());
 		LOGGER.info("Parsing nodes into the tree.");
 		for (ClassFile node : jar.getClassFiles().values()) {
 			classFiles.put(node.getName(), node);
 		}
 		LOGGER.info("Parsed a total of "+classFiles.size()+" class files into our syntax tree.");
 		LOGGER.info("Beginning to analyze flow block...");
 		analyze();
 	}
 
 	/**
 	 * Analyzes the bytecode.
 	 */
 	public void analyze() {
 		for (ClassFile cf : classFiles.values()) {
 			for (MethodDecNode method : cf.getMethods()) {
 				InstructionPointer pointer = new InstructionPointer(method.getInstructions());
 				for (BytecodeAnalyzer<?> analyzer : analyzers) {
 					for (FlowBlock block : method.getFlowBlocks()) {
 						if (block != null) {
 							if (block.isAnalyzed()) {
 								continue;
 							}
 							block.setAnalyzed(true);
 						}
 						if (analyzer.onAnalyze(block, method, pointer)) {
 							LOGGER.info("Successfully analyzed the flow block in "+cf.getName()+"."+method.getName()+" "+method.getDescriptor());
 						} else {
 							//throw new AnalyzerException("Failed to analyze the flow block for: "+cf.getName()+"."+method.getName()+" "+method.getDescriptor());
 						}
 					}
 					if (!BytecodeAnalyzer.queue.isEmpty()) {
 						for (Object o : BytecodeAnalyzer.queue) {
 							if (o instanceof FlowBlock) {
 								FlowBlock block = (FlowBlock) o;
 								if (block.isAnalyzed()) {
 									continue;
 								}
 								block.setAnalyzed(true);
 								if (block.getAnalyzer().onAnalyze(block, method, pointer)) {
 									LOGGER.info("Successfully analyzed the sub flow block in "+cf.getName()+"."+method.getName()+" "+method.getDescriptor());
 								} else {
 									//throw new AnalyzerException("Failed to analyze the sub flow block for: "+cf.getName()+"."+method.getName()+" "+method.getDescriptor());
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		LOGGER.info("Flow block fully analyzed! The analyzer only failed because it is incomplete. Don't worry.");
 	}
 
 	@Override
 	public Iterator<ClassFile> iterator() {
 		return classFiles.values().iterator();
 	}
 
 	/**
 	 * Gets the loaded jar file.
 	 * @return The jar file.
 	 */
 	public Jar getJar() {
 		return jar;
 	}
 
 	/**
 	 * Gets the loaded class files.
 	 * @return The class files.
 	 */
 	public Map<String, ClassFile> getClassFiles() {
 		return classFiles;
 	}
 
 	/**
 	 * Gets the bytecode analyzers.
 	 * @return The analyzers.
 	 */
 	public List<BytecodeAnalyzer<?>> getAnalyzers() {
 		return analyzers;
 	}
 }
