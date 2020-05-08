 package core.common.svd.path;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 
 import core.common.cfg.interfaces.IBasicBlock;
 import core.common.cfg.javacfg.ControlFlowGraphBuilder;
 import core.common.cfg.javacfg.JavaBranchNode;
 import core.common.cfg.javacfg.JavaConnectorNode;
 import core.common.cfg.javacfg.JavaControlFlowGraph;
 import core.common.cfg.javacfg.JavaDecisionNode;
 import core.common.cfg.javacfg.JavaExitNode;
 import core.common.cfg.javacfg.JavaJumpNode;
 import core.common.cfg.javacfg.JavaPlainNode;
 import core.common.cfg.javacfg.JavaStartNode;
import core.common.cfg.model.AbstractBasicBlock;
import core.common.cfg.model.BranchNode;
import core.common.cfg.model.ConnectorNode;
import core.common.cfg.model.ControlFlowGraph;
 
 public class PathGenerator {
 
 	public static LinkedList<Path> getPaths(JavaControlFlowGraph cfg) {
 		LinkedList<Path> paths = new LinkedList<>();
 		// TODO
 		return paths;
 	}
 
 	public static void main(String[] args) throws IOException{
 		String filePath = "D:\\GitHub\\Jtester\\src\\core\\common\\svd\\test\\SVDTest.java";
 		JavaControlFlowGraph[] cfgsArray = read(filePath);
 		JavaControlFlowGraph cfgInstance;
 		for (int i = 0; i < cfgsArray.length; i++) {
 			System.out.println("Begin to analyse CFG: "+cfgsArray[i].getSignature());
 			// ȡ·
 			cfgInstance = cfgsArray[i];
 			Path[] paths = abstractPath(cfgInstance);
 			System.out.println(paths.length);
 			// ӡpath
 			for (int j = 0; j < paths.length; j++) {
 				System.out.println("Print path"+i);
 				printPath(paths[i].pathNodes);
 			}
 		}
 	}
 
 	public static Path[] abstractPath(JavaControlFlowGraph cfg){
 		ArrayList<Path> paths = new ArrayList<>();
 		LinkedList<IBasicBlock> path = new LinkedList<>();
 		IBasicBlock start = cfg.getStartNode();
 		IBasicBlock currentNode = start;
 		IBasicBlock nextBasicBlock;
 		// Ըcfg·ȡ
 		System.out.println("Begin path abstraction");
 		
 		while (currentNode!=null || path.size()!=0) {// To be consider
 			if (currentNode instanceof JavaPlainNode) {
 				JavaPlainNode tempNode = (JavaPlainNode)currentNode;
 				path.add(currentNode);
 				currentNode = ((JavaPlainNode) currentNode).getOutgoing();
 				
 			}else if (currentNode instanceof JavaDecisionNode) {// άdetectedϢ
 				JavaDecisionNode tempNode = (JavaDecisionNode)currentNode;
 				if (tempNode.isDetected()) {// DecisionNodeķ֧Ѿȫ̽꣬ݵһDecisionNode
 					tempNode.cleanBranch();//branchnode detectedΪfalse
 					currentNode = findUpperDecision(path);
 				}else {
 					path.add(tempNode);
 					currentNode = tempNode.getUndetectedBranch();
 				}
 				/*BranchNode branchNode = tempNode.getUndetectedBranch();
 				if (branchNode != null) {
 					path.add(currentNode);
 					currentNode=branchNode;
 					continue;
 				}else{// branchʹǱǴdecisionҲʹ
 					tempNode.setDetected(true);
 				}*/
 				
 			}else if (currentNode instanceof JavaBranchNode) {
 				JavaBranchNode tempNode = (JavaBranchNode)currentNode;
 				if (tempNode.isDetected()) {
 					currentNode=findUpperDecision(path);
 				}else {
 					tempNode.setDetected(true);
 					path.add(tempNode);
 					currentNode = tempNode.getOutgoing();
 				}
 				
 			}else if (currentNode instanceof JavaJumpNode) {// TODO
 				JavaJumpNode tempNode = (JavaJumpNode)currentNode;
 				/*if (tempNode.isBackwardArc()) {
 					
 				}*/
 				currentNode = tempNode.getOutgoing();
 				
 			}else if (currentNode instanceof JavaExitNode) {
 				JavaExitNode tempNode = (JavaExitNode)currentNode;
 				path.add(tempNode);
 				LinkedList<IBasicBlock> completePath = (LinkedList<IBasicBlock>)path.clone();
 				paths.add(new Path(completePath));
 				currentNode = findUpperDecision(path);
 				
 			}else if (currentNode instanceof JavaConnectorNode) {
 				JavaPlainNode tempNode = (JavaPlainNode)currentNode;
 				currentNode = tempNode.getOutgoing();
 				
 			}else if (currentNode instanceof JavaStartNode) {
 				path.add(currentNode);
 				currentNode = ((JavaStartNode) currentNode).getOutgoing();
 			}
 		}
 		
 		// ·
 		Path[] pathArray = new Path[paths.size()];
 		return paths.toArray(pathArray);
 	}
 	
 	
 	private static void printPath(LinkedList<IBasicBlock> path){
 		System.out.println("find a path, its size is: " + path.size()+" and its content is: ");
 		for (int i = 0; i < path.size(); i++) {// TODO extracted
 			IBasicBlock dataBasicBlock=path.get(i);
 			if (dataBasicBlock instanceof JavaPlainNode) {
 				System.out.print("node "+i+"is: "+((JavaPlainNode)dataBasicBlock).getData());
 			}else if(dataBasicBlock instanceof JavaBranchNode){
 				System.out.print("node "+i+"is: "+((JavaBranchNode)dataBasicBlock).getData());
 			}else if(dataBasicBlock instanceof JavaExitNode){
 				JavaExitNode dataNode = (JavaExitNode)dataBasicBlock;
 				if (dataNode.getData()!=null) {
 					System.out.print("node "+i+"is: "+dataNode.getData());
 				}
 			}
 		}
 	}
 
 	// pathеһDecisionNodeɾDecisionNode֮нڵ
 	private static  JavaDecisionNode findUpperDecision(LinkedList<IBasicBlock> path){
 		IBasicBlock tempBasicBlock;
 		for (int i = path.size(); i>0 ; i--) {
 			tempBasicBlock = path.remove();
 			if (tempBasicBlock instanceof JavaDecisionNode) {
 				return (JavaDecisionNode)tempBasicBlock;
 			}
 		}
 		return null;
 	}
 	/**
 	 * ȡjavaԴļ
 	 * @param filePath
 	 * @return
 	 * @throws IOException
 	 */
 	private static JavaControlFlowGraph[] read(String filePath) throws IOException {
 		// ȡԴļ
 		File file = new File(filePath);
 		byte[] b = new byte[(int) file.length()];
 		FileInputStream fis = new FileInputStream(file);
 		fis.read(b);
 		fis.close();
 		String content = new String(b);
 		// Դļɱ뵥Ԫ
 		ASTParser parser = ASTParser.newParser(AST.JLS3);
 		parser.setSource(content.toCharArray());
 		CompilationUnit resultUnit = (CompilationUnit) parser.createAST(null);
 		// ȡһ
 		List types = resultUnit.types();
 		TypeDeclaration typeDec = (TypeDeclaration) types.get(0);
 		// ȡõһķ
 		MethodDeclaration[] methods = typeDec.getMethods();
 		// ظɵCFG
 		LinkedList<JavaControlFlowGraph> cfgs = new LinkedList<>();
 		ControlFlowGraphBuilder builder = new ControlFlowGraphBuilder();
 		for (int i = 0; i < methods.length; i++) {
 			cfgs.add(builder.build(methods[i]));
 		}
 		JavaControlFlowGraph[] cfgArrayControlFlowGraphs = new JavaControlFlowGraph[methods.length];
 		return cfgs.toArray(cfgArrayControlFlowGraphs);
 	}
}

