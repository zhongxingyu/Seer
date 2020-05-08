 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.core.tests.parser;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.parser.ISourceParser;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.tests.model.AbstractModelTests;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.core.tests.Activator;
 
 public abstract class AbstractASTTest extends AbstractModelTests {
 
 	protected static class CountingProblemReporter implements IProblemReporter {
 
 		public int count = 0;
 		public String lastInfo = "<no errors>";
 
 		public void reset() {
 			count = 0;
 		}
 
 		public int getCount() {
 			return count;
 		}
 		
 		public String info () {
 			return lastInfo;
 		}
 
 		public IMarker reportProblem(IProblem problem) throws CoreException {
 			count++;
 			lastInfo = problem.getMessage();
 			return null;
 		}
 
 	}
 
 	protected final static CountingProblemReporter problems = new CountingProblemReporter();
 
 	public AbstractASTTest(String testProjectName, String name) {
 		super(testProjectName, name);
 	}
 	
 	public AbstractASTTest(String name) {
 		super(Activator.PLUGIN_ID, name);
 	}
 	
 	
 
 	protected ModuleDeclaration getAST(String content) {
 		problems.reset();
 		ISourceParser parser;
 		try {
 			parser = DLTKLanguageManager.getSourceParser(RubyNature.NATURE_ID);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return parser.parse(null, content.toCharArray(), problems);
 	}
 
 	protected ASTNode getNodeAt(ASTNode root, final int start, final int end)
 			throws Exception {
 		assertTrue((start != -1) || (end != -1));
 		final ASTNode[] result = new ASTNode[] { null };
 		ASTVisitor visitor = new ASTVisitor() {
 
 			public boolean visitGeneral(ASTNode node) throws Exception {
 				if ((node.sourceStart() == start || start == -1) && 
 						(node.sourceEnd() == end || end == -1)) {
 					if (result[0] != null && !(result[0] instanceof ModuleDeclaration))
 						throw new RuntimeException("Two different nodes on "
 								+ start + ":" + end);
 					result[0] = node;
 				}
 				return true;
 			}
 
 		};
 
 		root.traverse(visitor);
 
 		return result[0];
 	}
 	
 	protected abstract Class getExpectedClass();
 	
 	protected ASTNode checkNode (String content, int start, int end) throws Exception {
 		ModuleDeclaration ast = getAST(content);
 		assertNotNull(ast);
 		if (problems.getCount() != 0) {
 			throw new RuntimeException(problems.info());
 		}
 		ASTNode nodeAt = getNodeAt(ast, start, end);
 		assertNotNull(nodeAt);
 		assertEquals(getExpectedClass(), nodeAt.getClass());
 		return nodeAt;
 	}
 
 }
