 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.parser.visitors;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.PositionInformation;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.expressions.BigNumericLiteral;
 import org.eclipse.dltk.ast.expressions.BooleanLiteral;
 import org.eclipse.dltk.ast.expressions.CallArgumentsList;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.expressions.FloatNumericLiteral;
 import org.eclipse.dltk.ast.expressions.Literal;
 import org.eclipse.dltk.ast.expressions.NilLiteral;
 import org.eclipse.dltk.ast.expressions.NumericLiteral;
 import org.eclipse.dltk.ast.expressions.StringLiteral;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.Reference;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.compiler.SourceElementRequestVisitor;
import org.eclipse.dltk.compiler.IElementRequestor.MethodInfo;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.ruby.ast.IRubyASTVisitor;
 import org.eclipse.dltk.ruby.ast.RubyAliasExpression;
 import org.eclipse.dltk.ruby.ast.RubyAssignment;
 import org.eclipse.dltk.ruby.ast.RubyCallArgument;
 import org.eclipse.dltk.ruby.ast.RubyColonExpression;
 import org.eclipse.dltk.ruby.ast.RubyConstantDeclaration;
 import org.eclipse.dltk.ruby.ast.RubyHashExpression;
 import org.eclipse.dltk.ruby.ast.RubyHashPairExpression;
 import org.eclipse.dltk.ruby.ast.RubyRegexpExpression;
 import org.eclipse.dltk.ruby.ast.RubySymbolReference;
 import org.eclipse.dltk.ruby.core.RubyConstants;
 
 public class RubySourceElementRequestor extends SourceElementRequestVisitor
 		implements IRubyASTVisitor {
 
 	private static final String VALUE = "value"; //$NON-NLS-1$
 	private static final String INITIALIZE = "initialize"; //$NON-NLS-1$
 
 	private static class TypeField {
 		private String fName;
 
 		private String fInitValue;
 
 		private PositionInformation fPos;
 
 		private ASTNode fExpression;
 
 		private ASTNode fToNode;
 
 		public TypeField(String name, String initValue,
 				PositionInformation pos, ASTNode expression, ASTNode toNode) {
 
 			this.fName = name;
 			this.fInitValue = initValue;
 			this.fPos = pos;
 			this.fExpression = expression;
 			this.fToNode = toNode;
 		}
 
 		public String getName() {
 			return fName;
 		}
 
 		public String getInitValue() {
 			return fInitValue;
 		}
 
 		public PositionInformation getPosition() {
 			return fPos;
 		}
 
 		public ASTNode getExpression() {
 			return fExpression;
 		}
 
 		public ASTNode getASTNode() {
 			return fToNode;
 		}
 
 		public boolean equals(Object obj) {
 			if (obj instanceof TypeField) {
 				TypeField typeFileld = (TypeField) obj;
 				return typeFileld.fName.equals(fName)
 						&& typeFileld.fToNode.equals(fToNode);
 			}
 
 			return false;
 		}
 
 		public String toString() {
 			return fName;
 		}
 	}
 
 	private List fNotAddedFields = new ArrayList(); // Used to prehold fields if
 	// adding in methods.
 	private Map fTypeVariables = new HashMap(); // Used to depermine duplicate
 
 	// names, ASTNode -> List of
 	// variables
 
 	private boolean canAddVariables(ASTNode type, String name) {
 		if (fTypeVariables.containsKey(type)) {
 			List variables = (List) fTypeVariables.get(type);
 			if (variables.contains(name)) {
 				return false;
 			}
 			variables.add(name);
 			return true;
 		} else {
 			List variables = new ArrayList();
 			variables.add(name);
 			fTypeVariables.put(type, variables);
 			return true;
 		}
 	}
 
 	/**
 	 * Parsers Expresssion and extract correct variable reference.
 	 */
 	private void addVariableReference(ASTNode left, ASTNode right,
 			boolean inClass, boolean inMethod) {
 
 		if (left == null) {
 			throw new IllegalArgumentException(
 					Messages.RubySourceElementRequestor_addVariableExpressionCantBeNull);
 		}
 
 		if (left instanceof VariableReference) {
 			VariableReference var = (VariableReference) left;
 
 			if (!inMethod) {
 				// For module static of class static variables.
 				if (canAddVariables((ASTNode) fNodes.peek(), var.getName())) {
 					ISourceElementRequestor.FieldInfo info = new ISourceElementRequestor.FieldInfo();
 					info.modifiers = Modifiers.AccStatic;
 					info.name = var.getName();
 					info.nameSourceEnd = var.sourceEnd() - 1;
 					info.nameSourceStart = var.sourceStart();
 					info.declarationStart = var.sourceStart();
 					fRequestor.enterField(info);
 					if (right != null) {
 						fRequestor.exitField(right.sourceEnd() - 1);
 					} else {
 						fRequestor.exitField(var.sourceEnd() - 1);
 					}
 				}
 			} else {
 
 			}
 
 		}
 	}
 
 	protected String makeLanguageDependentValue(ASTNode value) {
 		String outValue = ""; //$NON-NLS-1$
 		/*
 		 * if (value instanceof ExtendedVariableReference) { StringWriter
 		 * stringWriter = new StringWriter(); CorePrinter printer = new
 		 * CorePrinter(stringWriter); value.printNode(printer); printer.flush();
 		 * return stringWriter.getBuffer().toString(); }
 		 */
 		return outValue;
 	}
 
 	public RubySourceElementRequestor(ISourceElementRequestor requestor) {
 		super(requestor);
 	}
 
 	// Visiting methods
 	protected void onEndVisitMethod(MethodDeclaration method) {
 		if (DLTKCore.DEBUG) {
 			System.out.println("==> Method: " + method.getName()); //$NON-NLS-1$
 		}
 
 		Iterator it = fNotAddedFields.iterator();
 
 		while (it.hasNext()) {
 			TypeField field = (TypeField) it.next();
 
 			if (canAddVariables(field.getASTNode(), field.getName())) {
 				PositionInformation pos = field.getPosition();
 
 				ISourceElementRequestor.FieldInfo info = new ISourceElementRequestor.FieldInfo();
 				info.modifiers = Modifiers.AccStatic;
 				info.name = field.getName();
 				info.nameSourceEnd = pos.nameEnd - 1;
 				info.nameSourceStart = pos.nameStart;
 				info.declarationStart = pos.sourceStart;
 
 				fRequestor.enterField(info);
 				fRequestor.exitField(pos.sourceEnd);
 			}
 		}
 
 		fNotAddedFields.clear();
 	}
 
 	private void reportTypeReferences(ASTNode node) {
 		String typeName;
 		while (node != null) {
 			if (node instanceof RubyColonExpression) {
 				typeName = ((RubyColonExpression) node).getName();
 				fRequestor.acceptTypeReference(typeName.toCharArray(), node
 						.sourceStart());
 				node = ((RubyColonExpression) node).getLeft();
 			} else if (node instanceof ConstantReference) {
 				typeName = ((ConstantReference) node).getName();
 				fRequestor.acceptTypeReference(typeName.toCharArray(), node
 						.sourceStart());
 				node = null;
 			} else {
 				node = null;
 			}
 		}
 	}
 
 	public void visitTypeName(ASTNode node) {
 		// empty
 	}
 
 	// Visiting expressions
 	public boolean visit(ASTNode expression) throws Exception {
 		if (DLTKCore.DEBUG) {
 			System.out.println("==> Expression: " + expression.toString()); //$NON-NLS-1$
 		}
 
 		if (expression instanceof RubyAssignment) {
 			// Assignment handling (this is static variable assignment.)
 
 			RubyAssignment assignment = (RubyAssignment) expression;
 			ASTNode left = assignment.getLeft();
 			ASTNode right = assignment.getRight();
 
 			// Handle static variables
 			addVariableReference(left, right, fInClass, fInMethod);
 		} else if (expression instanceof CallExpression) {
 			// CallExpression handling
 			CallExpression callExpression = (CallExpression) expression;
 
 			String name = callExpression.getName();
 
 			if (RubyAttributeHandler.isAttributeCreationCall(callExpression)) {
 				RubyAttributeHandler info = new RubyAttributeHandler(
 						callExpression);
 				List readers = info.getReaders();
 				for (Iterator iterator = readers.iterator(); iterator.hasNext();) {
 					ASTNode n = (ASTNode) iterator.next();
 					String attr = RubyAttributeHandler.getText(n);
 					ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 					mi.name = attr;
 					mi.modifiers = RubyConstants.RubyAttributeModifier;
 					mi.nameSourceStart = n.sourceStart();
 					mi.nameSourceEnd = n.sourceEnd() - 1;
 					mi.declarationStart = n.sourceStart();
 
 					fRequestor.enterMethod(mi);
 					fRequestor.exitMethod(n.sourceEnd());
 				}
 				List writers = info.getWriters();
 				for (Iterator iterator = writers.iterator(); iterator.hasNext();) {
 					ASTNode n = (ASTNode) iterator.next();
 					String attr = RubyAttributeHandler.getText(n);
 					ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 					mi.parameterNames = new String[] { VALUE };
 					mi.name = attr + "="; //$NON-NLS-1$
 					mi.modifiers = RubyConstants.RubyAttributeModifier;
 					mi.nameSourceStart = n.sourceStart();
 					mi.nameSourceEnd = n.sourceEnd() - 1;
 					mi.declarationStart = n.sourceStart();
 
 					fRequestor.enterMethod(mi);
 					fRequestor.exitMethod(n.sourceEnd());
 				}
 			}
 			else if ("delegate".equals(callExpression.getName())) { //$NON-NLS-1$
 				RubyCallArgument argNode;
 				RubyHashPairExpression hashNode;
 				String oldName = ""; //$NON-NLS-1$
 				String dName;
 				for (Iterator iterator = callExpression.getArgs().getChilds()
 						.iterator(); iterator.hasNext();) {
 					argNode = (RubyCallArgument) iterator.next();
 					if (argNode.getValue() instanceof RubyHashExpression) {
 						for (Iterator iterator2 = argNode.getValue()
 								.getChilds().iterator(); iterator2.hasNext();) {
 							hashNode = (RubyHashPairExpression) iterator2
 									.next();
 							if (hashNode.getValue() instanceof RubySymbolReference) {
 								oldName = ((RubySymbolReference) hashNode
 										.getValue()).getName();
 							} else if (hashNode.getValue() instanceof StringLiteral) {
 								oldName = ((StringLiteral) hashNode.getValue())
 										.getValue();
 							}
 						}
 					}
 				}
 				for (Iterator iterator = callExpression.getArgs().getChilds()
 						.iterator(); iterator.hasNext();) {
 					argNode = (RubyCallArgument) iterator.next();
 					dName = null;
 					if (argNode.getValue() instanceof RubySymbolReference) {
 						dName = ((RubySymbolReference) argNode.getValue())
 								.getName();
 					} else if (argNode.getValue() instanceof StringLiteral) {
 						dName = ((StringLiteral) argNode.getValue()).getValue();
 					}
 					if (dName != null) {
 						ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 						mi.name = dName;
 						mi.modifiers = RubyConstants.RubyAliasModifier;
 						mi.nameSourceStart = argNode.sourceStart();
 						mi.nameSourceEnd = argNode.sourceEnd() - 1;
 						mi.declarationStart = argNode.sourceStart();
 						mi.parameterNames = new String[] { oldName };
 
 						fRequestor.enterMethod(mi);
 						fRequestor.exitMethod(argNode.sourceEnd());
 					}
 				}
 			}
 
 			if (name.equals("require")) { //$NON-NLS-1$
 				// TODO
 			}
 
 			// Arguments
 			int argsCount = 0;
 			CallArgumentsList args = callExpression.getArgs();
 			if (args != null && args.getChilds() != null) {
 				argsCount = args.getChilds().size();
 			}
 
 			// Start
 			int start = callExpression.sourceStart();
 			if (start < 0) {
 				start = 0;
 			}
 
 			// End
 			int end = callExpression.sourceEnd();
 			if (end < 0) {
 				end = 1;
 			}
 
 			// Accept
 			fRequestor.acceptMethodReference(callExpression.getName()
 					.toCharArray(), argsCount, start, end);
 		} else if (expression instanceof Literal) {
 			if (expression instanceof RubyRegexpExpression) {
 				fRequestor.acceptTypeReference(
 						"Regexp".toCharArray(), expression.sourceStart()); //$NON-NLS-1$
 			} else if (expression instanceof StringLiteral) {
 				fRequestor.acceptTypeReference(
 						"String".toCharArray(), expression.sourceStart()); //$NON-NLS-1$
 			} else if (expression instanceof BooleanLiteral) {
 				BooleanLiteral boolLit = (BooleanLiteral) expression;
 				if (boolLit.boolValue()) {
 					fRequestor
 							.acceptTypeReference(
 									"TrueClass".toCharArray(), expression.sourceStart()); //$NON-NLS-1$$
 				} else {
 					fRequestor
 							.acceptTypeReference(
 									"FalseClass".toCharArray(), expression.sourceStart()); //$NON-NLS-1$
 				}
 			} else if (expression instanceof NumericLiteral) {
 				fRequestor.acceptTypeReference(
 						"Fixnum".toCharArray(), expression.sourceStart()); //$NON-NLS-1$
 			} else if (expression instanceof NilLiteral) {
 				fRequestor.acceptTypeReference(
 						"NilClass".toCharArray(), expression.sourceStart()); //$NON-NLS-1$
 			} else if (expression instanceof FloatNumericLiteral) {
 				fRequestor.acceptTypeReference(
 						"Float".toCharArray(), expression.sourceStart()); //$NON-NLS-1$
 			} else if (expression instanceof BigNumericLiteral) {
 				fRequestor.acceptTypeReference(
 						"Bignum".toCharArray(), expression.sourceStart()); //$NON-NLS-1$
 			}
 		} else if (expression instanceof Reference) {
 			if (expression instanceof RubySymbolReference) {
 				fRequestor.acceptTypeReference(
 						"Symbol".toCharArray(), expression.sourceStart()); //$NON-NLS-1$
 			} else if (expression instanceof VariableReference) {
 				// VariableReference handling
 				VariableReference variableReference = (VariableReference) expression;
 
 				int pos = variableReference.sourceStart();
 				if (pos < 0) {
 					pos = 0;
 				}
 
 				// Accept
 				fRequestor.acceptFieldReference(variableReference.getName()
 						.toCharArray(), pos);
 			} else if (expression instanceof ConstantReference) {
 				reportTypeReferences(expression);
 			}
 		} else if (expression instanceof RubyColonExpression) {
 			reportTypeReferences(expression);
 		} else if (expression instanceof RubyConstantDeclaration) {
 			RubyConstantDeclaration constant = (RubyConstantDeclaration) expression;
 			SimpleReference constName = constant.getName();
 
 			ISourceElementRequestor.FieldInfo info = new ISourceElementRequestor.FieldInfo();
 			info.modifiers = Modifiers.AccConstant;
 			info.name = constName.getName();
 			info.nameSourceEnd = constName.sourceEnd() - 1;
 			info.nameSourceStart = constName.sourceStart();
 			info.declarationStart = constName.sourceStart();
 
 			fRequestor.enterField(info);
 			fRequestor.exitField(constName.sourceEnd() - 1);
 		} else if (expression instanceof RubyAliasExpression) {
 			RubyAliasExpression alias = (RubyAliasExpression) expression;
 			String oldValue = alias.getOldValue();
 			if (!oldValue.startsWith("$")) { //$NON-NLS-1$
 				String newValue = alias.getNewValue();
 				ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 
 				mi.name = newValue;
 				mi.modifiers = RubyConstants.RubyAliasModifier;
 				mi.nameSourceStart = alias.sourceStart();
 				mi.nameSourceEnd = alias.sourceEnd() - 1;
 				mi.declarationStart = alias.sourceStart();
 				mi.parameterNames = new String[] { oldValue };
 
 				fRequestor.enterMethod(mi);
 				fRequestor.exitMethod(alias.sourceEnd());
 			}
 		}
 
 		return true;
 	}
 
 	public boolean endvisit(ASTNode expression) throws Exception {
 		return true;
 	}
 
 	public boolean visit(Expression expression) throws Exception {
 		super.visit(expression);
 		return visit((ASTNode) expression);
 	}
 
 	public boolean visit(Statement statement) throws Exception {
 		super.visit(statement);
 		return visit((ASTNode) statement);
 	}
 
 	protected void modifyMethodInfo(MethodDeclaration methodDeclaration,
 			MethodInfo mi) {
 		if (fInClass) {
 			mi.isConstructor = methodDeclaration.getName().equals(INITIALIZE);
 		}
 	}
 }
