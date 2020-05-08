 package com.imaginea.javatooling.helpers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.tools.DiagnosticCollector;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaCompiler.CompilationTask;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 import com.imaginea.javatooling.entity.CompareResponse;
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.source.tree.LineMap;
 import com.sun.source.tree.MethodTree;
 import com.sun.source.tree.StatementTree;
 import com.sun.source.util.JavacTask;
 import com.sun.source.util.SourcePositions;
 import com.sun.source.util.TreeScanner;
 import com.sun.source.util.Trees;
 
 /**
  * this class is used to compare two java source files
  * 
  * @author adityap
  * 
  */
 public class JavaDiff {
 	private Map<String, MethodTree> ver1 = new HashMap<String, MethodTree>();
 	private Map<String, MethodTree> ver2 = new HashMap<String, MethodTree>();
 
 	private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
 	private DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
 	private StandardJavaFileManager fileManager = compiler
 			.getStandardFileManager(diagnosticsCollector, null, null);
 	private Iterable<? extends JavaFileObject> fileObjects;
 	private Iterable<? extends CompilationUnitTree> parseResult = null;
 
 	public JavaDiff(String file1Path, String file2Path) {
 		fileObjects = fileManager.getJavaFileObjects(file1Path, file2Path);
 	}
 
 	private void gatherInfo() throws IOException {
 		boolean firstFile = true;

 		CompilationTask task = compiler.getTask(null, fileManager,
 				diagnosticsCollector, null, null, fileObjects);
		com.sun.tools.javac.api.JavacTaskImpl javacTask = (com.sun.tools.javac.api.JavacTaskImpl) task;
		// JavacTask javacTask = (JavacTask) task;
 		SourcePositions sourcePositions = Trees.instance(javacTask)
 				.getSourcePositions();
 		parseResult = javacTask.parse();
 		for (CompilationUnitTree compilationUnitTree : parseResult) {
 			MethodScanner ms = new MethodScanner(compilationUnitTree,
 					sourcePositions);
 			compilationUnitTree.accept(ms, null);
 			// adding methods versions
 			if (firstFile) {
 				for (MethodTree m : ms.getMethods()) {
 					ver1.put(m.getName().toString(), m);
 				}
 				firstFile = false;
 			} else {
 				for (MethodTree m : ms.getMethods()) {
 					ver2.put(m.getName().toString(), m);
 				}
 			}
 		}
 
 	}
 
 	public CompareResponse compare() throws Exception {
 		CompareResponse cr = new CompareResponse();
 		List<String> methodsAdded = new ArrayList<String>();
 		List<String> methodsDeleted = new ArrayList<String>();
 		StringBuffer buf = new StringBuffer();
 
 		gatherInfo();
 		buf.append("--------------deletions from version1-----------\n");
 		System.out.println("--------------deletions from version1-----------");
 		for (String s : ver1.keySet()) {
 			if (!ver2.containsKey(s)) {
 				methodsDeleted.add(s);
 				buf.append(s + "\n");
 				System.out.println(s);
 			}
 		}
 		buf.append("--------------additions in version2------------\n");
 		System.out.println("--------------additions in version2------------");
 		for (String s : ver2.keySet()) {
 			if (!ver1.containsKey(s)) {
 				methodsAdded.add(s);
 				buf.append(s + "\n");
 				System.out.println(s);
 			}
 		}
 		System.out
 				.println("-----------modifications in version2 << : deleted | >>: added------------");
 		for (String method : ver1.keySet()) {
 			if (ver2.containsKey(method)) {
 				List<? extends StatementTree> s1 = ver1.get(method).getBody()
 						.getStatements();
 				List<? extends StatementTree> s2 = ver2.get(method).getBody()
 						.getStatements();
 				boolean changed = false;
 				if (s1.size() < s2.size()) {
 					for (int i = 0; i < s1.size(); i++) {
 						if (!s1.get(i).toString().equals(s2.get(i).toString())) {
 							System.out.println("<< " + s1.get(i));
 							System.out.println(">> " + s2.get(i));
 						}
 					}
 					for (int j = s1.size(); j < s2.size(); j++) {
 						System.out.println(">> " + s2.get(j));
 					}
 				} else {
 					for (int i = 0; i < s2.size(); i++) {
 						if (!s1.get(i).toString().equals(s2.get(i).toString())) {
 							System.out.println("<< " + s1.get(i));
 							System.out.println(">> " + s2.get(i));
 						}
 					}
 					for (int j = s2.size(); j < s1.size(); j++) {
 						System.out.println("<< " + s1.get(j));
 					}
 				}
 			}
 		}
 		cr.setMethodsAdded(methodsAdded);
 		cr.setMethodsDeleted(methodsDeleted);
 		return cr;
 	}
 
 	private static class MethodScanner extends TreeScanner<Void, Void> {
 		private final CompilationUnitTree compilationUnitTree;
 		private final SourcePositions sourcePositions;
 		private final LineMap lineMap;
 		private final List<MethodTree> methods = new ArrayList<MethodTree>();
 
 		private MethodScanner(CompilationUnitTree compilationUnitTree,
 				SourcePositions sourcePositions) {
 			this.compilationUnitTree = compilationUnitTree;
 			this.sourcePositions = sourcePositions;
 			this.lineMap = compilationUnitTree.getLineMap();
 		}
 
 		@Override
 		public Void visitMethod(MethodTree arg0, Void arg1) {
 			methods.add(arg0);
 			/*
 			 * long startPosition = sourcePositions.getStartPosition(
 			 * compilationUnitTree, arg0); long startLine =
 			 * lineMap.getLineNumber(startPosition); long endPosition =
 			 * sourcePositions.getEndPosition( compilationUnitTree, arg0); long
 			 * endLine = lineMap.getLineNumber(endPosition); // Voila!
 			 * System.out.println("Found method " + arg0.getName() +
 			 * " from line " + startLine + " to line " + endLine + ".");
 			 */
 			return super.visitMethod(arg0, arg1);
 		}
 
 		public List<MethodTree> getMethods() {
 			return methods;
 		}
 	}
 }
