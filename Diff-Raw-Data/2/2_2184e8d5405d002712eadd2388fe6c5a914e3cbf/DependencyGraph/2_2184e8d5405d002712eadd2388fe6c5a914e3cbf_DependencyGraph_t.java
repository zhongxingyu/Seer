 package jsclassloader.dependency;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import jsclassloader.Config;
 import jsclassloader.classes.ClassFileSet;
 
 public class DependencyGraph {
 	
 	private Map<String, ClassNode> nodeMap;
 	
 	private DependencyParser depParser;
 	private ClassFileSet classFileSet;
 	
 	public ClassFileSet getClassFileSet() {
 		return classFileSet;
 	}
 
 	public DependencyGraph(Config config) throws IOException {
 		classFileSet = new ClassFileSet(config);
 
 		depParser = new DependencyParser(config);
 		
 		depParser.setClassFileSet(classFileSet);
 		
 		nodeMap = new HashMap<String, ClassNode>();
 		for (String className : classFileSet.getAllJsClasses()) {
 			nodeMap.put(className, new ClassNode(className));
 		}
 		for (ClassNode node : nodeMap.values()) {
 			processDependencies(node);
 		}
 	}
 	
 	public void processDependencies(ClassNode node) throws IOException {
 		if (!node.isProcessed()) {
 			node.setProcessed(true);
 			
 			File file = classFileSet.getFileFromClassname(node.getValue());
 			InputStream in = new BufferedInputStream(new FileInputStream(file));
 			for (DependencyParser.Match matched : depParser.parse(in))
 			{
 				if(matched != null && !matched.getClassname().equals(node.getValue())){
 					
 					if (nodeMap.get(matched.getClassname()) == null) {
 						//logger.warn("Classname not found in classes: " + matched.getClassname());
 					}
 					else {
 						node.addDependency(nodeMap.get(matched.getClassname()), matched.isStaticDependency());
 					}
 				}
 			}
 			in.close();
 		
 			for (ClassNode dep : node.getStaticDependencies()) {
 				processDependencies(dep);
 			}
 			for (ClassNode dep : node.getRunTimeDependencies()) {
 				processDependencies(dep);
 			}
 		}
 	}
 	
 	public String renderDotFile(List<String> classNames) {
 		Map<ClassNode, Boolean> done = new HashMap<ClassNode, Boolean>();
 		StringBuffer graph = new StringBuffer("digraph G {\n");
 		
 		for (String className : classNames) {
 			ClassNode parentNode = getNode(className);
 			addNodeToDotFile(parentNode, graph, done);
 		}
 		graph.append("}\n");
 		return graph.toString();
 	};
 	
 	private void addNodeToDotFile(ClassNode node, StringBuffer graph, Map<ClassNode, Boolean> done) {
 		if (done.get(node) == null) {
 			done.put(node, true);
  
 			graph.append("\"" + node.getValue() + "\";\n");
 			
 			for (ClassNode child : node.getStaticDependencies()) {
 				graph.append("edge [color=red];\n");
 				graph.append("\"" + node.getValue() + "\" -> \"" + child.getValue() + "\";\n");
 				addNodeToDotFile(child, graph, done);
 			}
 			
 			for (ClassNode child : node.getRunTimeDependencies()) {
 				graph.append("edge [color=black];\n");
 				graph.append("\"" +node.getValue() + "\" -> \"" + child.getValue() + "\";\n");
 				addNodeToDotFile(child, graph, done);
 			}
 		}
 	}
 
 	public String renderModuleDotFile() {
 		Map<String, Boolean> moduleDone = new HashMap<String, Boolean>();
 		Map<String, Boolean> linkDone = new HashMap<String, Boolean>();
 		Map<ClassNode, Boolean> classDone = new HashMap<ClassNode, Boolean>();
 		
 		StringBuffer graph = new StringBuffer("digraph G {\n");
 		
 		for (String className : classFileSet.getAllJsClasses()) {
 			addModuleDependencyToDotFile(getNode(className), graph, classDone, moduleDone, linkDone);
 		}
 		graph.append("}\n");
 		return graph.toString();
 	};
 	
 	private void addModuleDependencyToDotFile(ClassNode node, StringBuffer graph, Map<ClassNode, Boolean> classDone, Map<String, Boolean> moduleDone, Map<String, Boolean> linkDone) {
 		String moduleName = classFileSet.getSrcDirFromClassname(node.getValue());
 		
 		//put in the modules on their own just in case there are no links:
 		if (moduleDone.get(moduleName) == null) {
 			moduleDone.put(moduleName, true);
 			graph.append("\"" + moduleName +"\";\n");
 		}
 		
 		for (ClassNode child : node.getStaticDependencies()) {
 			if (classDone.get(child) == null) {
 				classDone.put(child, true);
 				String childModuleName = classFileSet.getSrcDirFromClassname(child.getValue());
 				addModuleLinkToDotFile(moduleName, childModuleName, graph, linkDone);
 				addModuleDependencyToDotFile(child, graph, classDone, moduleDone, linkDone);
 			}
 		}
 		for (ClassNode child : node.getRunTimeDependencies()) {
 			if (classDone.get(child) == null) {
 				classDone.put(child, true);
 				String childModuleName = classFileSet.getSrcDirFromClassname(child.getValue());
 				addModuleLinkToDotFile(moduleName, childModuleName, graph, linkDone);
 				addModuleDependencyToDotFile(child, graph, classDone, moduleDone, linkDone);
 			}
 		}
 	}
 	
 	private void addModuleLinkToDotFile(String parentModule, String childModule, StringBuffer graph, Map<String, Boolean> linkDone) {
 		
 		if (!parentModule.equals(childModule)) {
 			
 			String link = parentModule + "&" + childModule;
 			
			if (linkDone.get(link) != null) {
 				linkDone.put(link, true);
 				graph.append("  \"" + parentModule + "\" -> \"" + childModule + "\";\n");
 			}
 		}
 	}
 
 	
 	
 	public ClassNode getNode(String className) {
 		return nodeMap.get(className);
 	}
 	
 	public List<String> getSeedClassesFromFiles(List<File> seedSources) throws IOException {
 		
 		List<String> results = new ArrayList<String>();
 		
 		for (File file: seedSources) {
 			if (!file.isDirectory() && file.canRead()) {
 				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
 				
 				for (DependencyParser.Match match : depParser.parse(in)) {
 					results.add(match.getClassname());
 				}
 				in.close();
 			}
 		}
 		return results;
 	}
 }
