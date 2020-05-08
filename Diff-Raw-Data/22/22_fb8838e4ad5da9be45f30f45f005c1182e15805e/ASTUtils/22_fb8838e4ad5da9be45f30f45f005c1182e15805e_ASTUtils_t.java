 package org.eclipse.dltk.ruby.internal.parsers.jruby;
 
 import java.util.Stack;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.statements.Block;
 import org.eclipse.dltk.core.DLTKCore;
 
 public class ASTUtils {
 
 	private ASTUtils() {
 		throw new AssertionError("Cannot instantiate utility class");
 	}
 
 	public static void setVisibility(MethodDeclaration methodDeclaration, int newVisibility) {
 		int modifiers = methodDeclaration.getModifiers();
 		modifiers = modifiers & ~(Modifiers.AccPublic | Modifiers.AccProtected | Modifiers.AccPrivate | Modifiers.AccDefault);
 		methodDeclaration.setModifiers(modifiers | newVisibility);
 	}
 	
 	
 	
 	public static ASTNode[] restoreWayToNode (ModuleDeclaration module, final ASTNode nde) {
 		final Stack stack = new Stack();
 		
 		ASTVisitor visitor = new ASTVisitor() {
 			boolean found = false;
 
 			public boolean visitGeneral(ASTNode node) throws Exception {
 				if (found)
 					return super.visitGeneral(node);
 				stack.push(node);
 				if (node == nde)
 					found = true;
 				return super.visitGeneral(node);
 			}
 
 			public void endvisitGeneral(ASTNode node) throws Exception {
 				super.endvisitGeneral(node);
 				if (found) 
 					return;
 				stack.pop();
 			}			
 		};
 		
 		try {
 			module.traverse(visitor);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return (ASTNode[]) stack.toArray(new ASTNode[stack.size()]);
 	}
 	
 	private static ASTNode getEnclosingElement(Class element, ASTNode[] wayToNode, ASTNode node, boolean considerGiven) {
 		int pos = -1;
 		for (int i = wayToNode.length - 1; i >= 0; i--) {
 			if (wayToNode[i] == node) {
 				pos = i;
 				break;
 			}
 		}
 		Assert.isLegal(pos != -1);
 		if (!considerGiven)
 			pos--;
 		for (int i = pos; i >= 0; i--) {		
 			if (element.isInstance(wayToNode[i]))
 				return wayToNode[i];
 		}
 		
 		return null;
 	}
 	
 	public static TypeDeclaration getEnclosingType(ASTNode[] wayToNode, ASTNode node, boolean considerGiven) {
 		return (TypeDeclaration) getEnclosingElement(TypeDeclaration.class, wayToNode, node, considerGiven);
 	}
 	
 	public static CallExpression getEnclosingCallNode(ASTNode[] wayToNode, ASTNode node, boolean considerGiven) {
 		return (CallExpression) getEnclosingElement(CallExpression.class, wayToNode, node, considerGiven);
 	}
 	
 	public static MethodDeclaration getEnclosingMethod(ASTNode[] wayToNode, ASTNode node, boolean considerGiven) {
 		return (MethodDeclaration) getEnclosingElement(MethodDeclaration.class, wayToNode, node, considerGiven);
 	}
 	
 	/**
 	 * Finds minimal ast node, that covers given position
 	 * @param unit
 	 * @param position
 	 * @return
 	 */
 	public static ASTNode findMinimalNode(ModuleDeclaration unit, int start, int end) {
 			
 		class Visitor extends ASTVisitor {
 			ASTNode result = null;
 			int start, end;
 			
 			public Visitor(int start, int end) {
 				this.start = start;
 				this.end = end;
 			}
 			
 			public ASTNode getResult () {
 				return result;
 			}			
 			
 			private int calcLen (ASTNode s) {
 				int realStart = s.sourceStart();
 				int realEnd = s.sourceEnd();
 				if (s instanceof TypeDeclaration) {
 					TypeDeclaration declaration = (TypeDeclaration) s;
 					realStart = declaration.getNameStart();
 					realEnd = declaration.getNameEnd();
 				} else if (s instanceof MethodDeclaration) {
 					MethodDeclaration declaration = (MethodDeclaration) s;
 					realStart = declaration.getNameStart();
 					realEnd = declaration.getNameEnd();
 				}
 				return realEnd - realStart;
 			}
 			
 			public boolean visitGeneral(ASTNode s) throws Exception {				
 				int realStart = s.sourceStart();
 				int realEnd = s.sourceEnd();
 				if (s instanceof Block) {
 					realStart = realEnd = -42; //never select on blocks
 				} else if (s instanceof TypeDeclaration) {
 					TypeDeclaration declaration = (TypeDeclaration) s;
 					realStart = declaration.getNameStart();
 					realEnd = declaration.getNameEnd();
 				} else if (s instanceof MethodDeclaration) {
 					MethodDeclaration declaration = (MethodDeclaration) s;
 					realStart = declaration.getNameStart();
 					realEnd = declaration.getNameEnd();
 				}
 				if (realStart <= start && realEnd >= end) {
 					if (result != null) {
 						int oldlen = calcLen(result);
 						int newlen = realEnd - realStart;
 						if (newlen < oldlen )
 							result = s;
 					} else {
 						result = s;						
 					}
 					
 					
 					if (DLTKCore.DEBUG_SELECTION)
 						System.out.println("Found " + s.getClass().getName());
 				}
 				return true;
 			}
 
 			
 		}
 		
 		Visitor visitor = new Visitor(start, end);		
 		
 		try {
 			unit.traverse(visitor);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return visitor.getResult();
 	}
 	
 	/**
 	 * Finds minimal ast node, that covers given position
 	 * 
 	 * @param unit
 	 * @param position
 	 * @return
 	 */
 	public static ASTNode findMaximalNodeEndingAt(ModuleDeclaration unit, final int boundaryOffset) {
 
 		class Visitor extends ASTVisitor {
 			ASTNode result = null;
 
 			public ASTNode getResult() {
 				return result;
 			}
 
 			public boolean visitGeneral(ASTNode s) throws Exception {
				if (s.sourceStart() < 0 || s.sourceEnd() < 0)
					return true;
 				int sourceEnd = s.sourceEnd();
 				if (Math.abs(sourceEnd - boundaryOffset) <= 1) {
 					result = s;
 					System.out.println("Found " + s.getClass().getName());
 				}
 				return true;
 			}
 
 		}
 
 		Visitor visitor = new Visitor();
 
 		try {
 			unit.traverse(visitor);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return visitor.getResult();
 	}
 	
 	
 }
