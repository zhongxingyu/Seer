 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.tests.typeinference;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.evaluation.types.SimpleType;
 import org.eclipse.dltk.evaluation.types.UnknownType;
 import org.eclipse.dltk.ruby.tests.Activator;
 import org.eclipse.dltk.ruby.typeinference.OffsetTargetedASTVisitor;
 import org.eclipse.dltk.ruby.typeinference.RubyClassType;
 import org.eclipse.dltk.ti.BasicContext;
 import org.eclipse.dltk.ti.DLTKTypeInferenceEngine;
 import org.eclipse.dltk.ti.ITypeInferencer;
 import org.eclipse.dltk.ti.goals.ExpressionTypeGoal;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 import org.eclipse.dltk.ti.types.RecursionTypeCall;
 
 public class TypeInferenceSuite extends TestSuite {
 
 	public TypeInferenceSuite(String testsDirectory) {		
 		super(testsDirectory);
 		Enumeration entryPaths = Activator.getDefault().getBundle().getEntryPaths(testsDirectory);
 		while (entryPaths.hasMoreElements()) {
 			final String path = (String) entryPaths.nextElement();
 			URL entry = Activator.getDefault().getBundle().getEntry(path);
 			try {
 				entry.openStream().close();
 			} catch (Exception e) {
 				continue;
 			}
 			int pos = path.lastIndexOf('/');
 			final String name = (pos >= 0 ? path.substring(pos + 1) : path);
 			String x = path.substring(0, pos);
 			pos = x.lastIndexOf('/');
 			final String folder = (pos >= 0 ? x.substring(pos + 1) : x);
 			addTest(new TestCase(name) {
 
 				private Collection assertions = new ArrayList();
 
 				public void setUp() {
 				}
 
 				class MethodReturnTypeAssertion implements IAssertion {
 
 					private final String className;
 
 					private final String methodName;
 
 					private final String correctClassRef;
 
 					public MethodReturnTypeAssertion(String className, String methodName,
 							String correctClassRef) {
 						this.className = className;
 						this.methodName = methodName;
 						this.correctClassRef = correctClassRef;
 					}
 
 					public void check(ModuleDeclaration rootNode, ISourceModule cu, ITypeInferencer inferencer) {
 						fail("This type of assertion is not implemented yet");
 //						ITypeDescriptor correctType;
 //						if ("recursion".equals(correctClassRef))
 //							correctType = RecursiveCallTypeDescriptor.INSTANCE;
 //						else if ("any".equals(correctClassRef))
 //							correctType = AnyTypeDescriptor.INSTANCE;
 //						else
 //							correctType = lookupType(correctClassRef);
 //						IKnownTypeDescriptor methType = lookupType(className);
 //						assertNotNull("class " + className + " not found", methType);
 //						IMethodDescriptor method = methType.getMethodByName(methodName);
 //						assertNotNull("method " + methodName + " not found", method);
 //						ITypeDescriptor checkedType = method.getReturnType();
 //						assertEquals("Incorrect type of " + className + "." + methodName, correctType, checkedType);
 					}
 
 				}
 				
 
 				protected void runTest() throws Throwable {
 					String content = loadContent(path);
 					String[] lines = content.split("\n");
 					int lineOffset = 0;
 					for (int i = 0; i < lines.length; i++) {
 						String line = lines[i].trim();
 						int pos = line.indexOf("##");
 						if (pos >= 0) {
 							StringTokenizer tok = new StringTokenizer(line.substring(pos + 2));
 							String test = tok.nextToken();
 							if ("meth".equals(test)) {
 								String methodRef = tok.nextToken();
 								String arrow = tok.nextToken();
 								Assert.isLegal(arrow.equals("=>"));
 								String correctClassRef = tok.nextToken();
 								int dotPos = methodRef.lastIndexOf('.');
 								if (dotPos >= 0) {
 									String classRef = methodRef.substring(0, dotPos);
 									String methodName = methodRef.substring(dotPos + 1);
 									assertions.add(new MethodReturnTypeAssertion(classRef,
 											methodName, correctClassRef));
 								} else {
 									Assert.isLegal(false);
 								}
 							} else if ("localvar".equals(test)) {
 								String varName = tok.nextToken();
 								int namePos = lines[i].indexOf(varName);
 								Assert.isLegal(namePos >= 0);
 								namePos += lineOffset;
 								String arrow = tok.nextToken();
 								Assert.isLegal(arrow.equals("=>"));
 								String correctClassRef = tok.nextToken();
 								assertions.add(new VariableReturnTypeAssertion(varName, namePos, correctClassRef));
 							} else if ("expr".equals(test)) {
 								String expr = tok.nextToken();
 								int namePos = lines[i].indexOf(expr);
 								Assert.isLegal(namePos >= 0);
 								namePos += lineOffset;
 								String arrow = tok.nextToken();
 								Assert.isLegal(arrow.equals("=>"));
 								String correctClassRef = tok.nextToken();
 								assertions.add(new ExpressionTypeAssertion(expr, namePos, correctClassRef));
 							} else {
//								Assert.isLegal(false);
 							}
 						}
 						lineOffset += lines[i].length() + 1;
 					}
 					
 					if (assertions.size() == 0)
 						return;
 					
 					ITypeInferencer inferencer = new DLTKTypeInferenceEngine();
 
 					TypeInferenceTest tests = new TypeInferenceTest("ruby selection tests");
 					tests.setUpSuite();
 					try {
 						tests.executeTest(folder, name, inferencer, assertions);						
 					} finally {
 						tests.tearDownSuite();
 					}
 				}
 
 				class VariableReturnTypeAssertion implements IAssertion {
 					
 					private final String correctClassRef;
 
 					private final String varName;
 
 					private final int namePos;
 					
 					public VariableReturnTypeAssertion(String varName, int namePos, String correctClassRef) {
 						this.varName = varName;
 						this.namePos = namePos;
 						this.correctClassRef = correctClassRef;
 					}
 					
 					public void check(ModuleDeclaration rootNode, ISourceModule cu, ITypeInferencer inferencer) throws Exception {
 						final ASTNode[] result = new ASTNode[1];
 						ASTVisitor visitor = new OffsetTargetedASTVisitor(namePos) {
 
 							protected boolean visitGeneralInteresting(ASTNode s) {
 								if (s instanceof VariableReference)
 									if (s.sourceStart() == namePos && result[0] == null) {
 										result[0] = s;
 									}
 								return true;
 							}
 							
 						};
 						rootNode.traverse(visitor);
 						Assert.isLegal(result[0] != null);
 						ExpressionTypeGoal goal = new ExpressionTypeGoal(new BasicContext(cu, rootNode), result[0]);
 						IEvaluatedType type = inferencer.evaluateType(goal, -1);
 						assertNotNull(type);
 						
 						assertEquals(correctClassRef, ((RubyClassType)type).getModelKey());
 					}
 					
 					
 				}
 				
 				class ExpressionTypeAssertion implements IAssertion {
 					
 					private final String correctClassRef;
 					
 					private final String expression;
 					
 					private final int namePos;
 					
 					public ExpressionTypeAssertion(String expression, int namePos, String correctClassRef) {
 						this.expression = expression;
 						this.namePos = namePos;
 						this.correctClassRef = correctClassRef;
 					}
 					
 					public void check(ModuleDeclaration rootNode, ISourceModule cu, ITypeInferencer inferencer) throws Exception {
 						final ASTNode[] result = new ASTNode[1];
 						ASTVisitor visitor = new OffsetTargetedASTVisitor(namePos) {
 							
 							protected boolean visitGeneralInteresting(ASTNode s) {
 								if (s instanceof ASTNode && result[0] == null)
 									if (s.sourceStart() == namePos) {
 										result[0] = s;
 									}
 								return true;
 							}
 							
 						};
 						rootNode.traverse(visitor);
 						if (result[0] == null)
 							System.out.println("ExpressionTypeAssertion.check()");
 						Assert.isLegal(result[0] != null);
 						ExpressionTypeGoal goal = new ExpressionTypeGoal(new BasicContext(cu, rootNode), result[0]);
 						IEvaluatedType type = inferencer.evaluateType(goal, -1);
 						if (!correctClassRef.equals("recursion")) {
 							if (type == null)
 								throw new AssertionFailedError("null type fetched, but " + correctClassRef + " expected");
 							assertNotNull(type);
 							if (type instanceof SimpleType) {
 								IEvaluatedType intrinsicType = getIntrinsicType(correctClassRef);
 								assertEquals(intrinsicType, type);
 							} else { 
 								RubyClassType rubyType = (RubyClassType)type;
 								assertEquals(correctClassRef, rubyType.getModelKey());
 							}
 						}
 					}
 					
 					
 				}
 
 			});
 		}
 	}
 	
 	private String loadContent(String path) throws IOException {
 		StringBuffer buffer = new StringBuffer();
 		InputStream input = null;
 		try {
 			input = Activator.getDefault().openResource(path);
 			InputStreamReader reader = new InputStreamReader(input);
 			BufferedReader br = new BufferedReader(reader);
 			char[] data = new char[10*1024*1024];
 			int size = br.read(data);
 			buffer.append(data, 0, size);
 		} finally {
 			if (input != null) {
 				input.close();
 			}
 		}
 		String content = buffer.toString();
 		return content;
 	}
 
 	private static IEvaluatedType getIntrinsicType(String correctClassRef) {
 		IEvaluatedType correctType;
 		if ("recursion".equals(correctClassRef))
 			correctType = RecursionTypeCall.INSTANCE;
 		else if ("any".equals(correctClassRef))
 			correctType = UnknownType.INSTANCE;
 //		else if ("Fixnum".equals(correctClassRef))
 //			correctType = new SimpleType(SimpleType.TYPE_NUMBER);
 //		else if ("Str".equals(correctClassRef))
 //			correctType = new SimpleType(SimpleType.TYPE_STRING);
 		else
 			correctType = null;
 		return correctType;
 	}
 }
