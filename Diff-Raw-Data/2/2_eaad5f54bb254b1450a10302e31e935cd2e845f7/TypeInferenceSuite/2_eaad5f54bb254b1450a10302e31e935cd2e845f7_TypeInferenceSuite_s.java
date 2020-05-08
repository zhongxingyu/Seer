 package org.eclipse.dltk.ruby.tests.typeinference;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.ddp.BasicContext;
 import org.eclipse.dltk.ddp.ExpressionGoal;
 import org.eclipse.dltk.ddp.ITypeInferencer;
 import org.eclipse.dltk.ddp.TypeInferencer;
 import org.eclipse.dltk.evaluation.types.IEvaluatedType;
 import org.eclipse.dltk.evaluation.types.RecursionTypeCall;
 import org.eclipse.dltk.evaluation.types.SimpleType;
 import org.eclipse.dltk.evaluation.types.UnknownType;
 import org.eclipse.dltk.ruby.tests.Activator;
 import org.eclipse.dltk.ruby.typeinference.OffsetTargetedASTVisitor;
 import org.eclipse.dltk.ruby.typeinference.RubyClassType;
 import org.eclipse.dltk.ruby.typeinference.RubyEvaluatorFactory;
 import org.eclipse.dltk.ruby.typeinference.RubyMetaClassType;
 
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
 								Assert.isLegal(false);
 							}
 						}
 						lineOffset += lines[i].length() + 1;
 					}
 					
 					Assert.isLegal(assertions.size() > 0);
 					
 					if("simple.rb".equals(name)) 
 						System.out.println("runTest(" + name + ")");
 					
 					ITypeInferencer inferencer = new TypeInferencer(new RubyEvaluatorFactory());
 
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
 						final Statement[] result = new Statement[1];
 						ASTVisitor visitor = new OffsetTargetedASTVisitor(namePos) {
 
 							protected boolean visitInteresting(Expression s) {
 								if (s instanceof VariableReference)
 									if (s.sourceStart() == namePos && result[0] == null) {
 										result[0] = s;
 									}
 								return true;
 							}
 							
 						};
 						rootNode.traverse(visitor);
 						Assert.isLegal(result[0] != null);
 						ExpressionGoal goal = new ExpressionGoal(new BasicContext(cu, rootNode), result[0]);
 						IEvaluatedType type = inferencer.evaluateGoal(goal, 0);
 						assertNotNull(type);
 						
 						IEvaluatedType correctType = getIntrinsicType(correctClassRef);
 						assertEquals(correctType, type);
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
 						final Statement[] result = new Statement[1];
 						ASTVisitor visitor = new OffsetTargetedASTVisitor(namePos) {
 							
 							protected boolean visitInteresting(Expression s) {
 								if (s instanceof Expression && result[0] == null)
 									if (s.sourceStart() == namePos) {
 										result[0] = s;
 									}
 								return true;
 							}
 							
 						};
 						rootNode.traverse(visitor);
 						Assert.isLegal(result[0] != null);
 						ExpressionGoal goal = new ExpressionGoal(new BasicContext(cu, rootNode), result[0]);
 						IEvaluatedType type = inferencer.evaluateGoal(goal, 0);
 						assertNotNull(type);
 						
 						IEvaluatedType correctType = getIntrinsicType(correctClassRef);
 						
 						if (correctType != null)
 							assertEquals(correctType, type);
 						else if (correctClassRef.endsWith(".new")) {
 							String correctFQN = correctClassRef.substring(0, correctClassRef.length() - 4);
 							assertTrue(type instanceof RubyClassType);
 							String realFQN = getFQN((RubyClassType) type);
 							assertEquals(correctFQN, realFQN);
 						} else {
 							assertTrue(type instanceof RubyMetaClassType);
 							RubyMetaClassType metatype = (RubyMetaClassType) type;
 							String correctFQN = correctClassRef;
 							String realFQN = getFQN((RubyClassType) metatype.getInstanceType());
 							assertEquals(correctFQN, realFQN);
 						}
 					}
 					
 					
 				}
 
 			});
 		}
 	}
 	
 	public static String getFQN(RubyClassType type) {
 		String[] fqn = type.getFQN();
 		if (fqn == null || fqn.length == 0)
 			return "<none>";
 		StringBuffer result = new StringBuffer();
 		for (int i = 0; i < fqn.length; i++) {
 			String component = fqn[i];
 			if (result.length() > 0)
 				result.append("::");
 			result.append(component);
 		}
 		return result.toString();
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
 		else if ("Fixnum".equals(correctClassRef))
 			correctType = new SimpleType(SimpleType.TYPE_NUMBER);
 		else if ("Str".equals(correctClassRef))
 			correctType = new SimpleType(SimpleType.TYPE_STRING);
 		else
 			correctType = null;
 		return correctType;
 	}
 }
