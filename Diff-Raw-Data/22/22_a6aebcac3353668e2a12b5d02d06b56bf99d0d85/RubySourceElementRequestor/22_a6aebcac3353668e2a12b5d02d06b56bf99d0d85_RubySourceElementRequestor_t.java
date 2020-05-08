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
 
 import org.eclipse.dltk.ast.ASTListNode;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.PositionInformation;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.CallArgumentsList;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.compiler.SourceElementRequestVisitor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.ruby.ast.RubyAliasExpression;
 import org.eclipse.dltk.ruby.ast.RubyAssignment;
 import org.eclipse.dltk.ruby.ast.RubyColonExpression;
 import org.eclipse.dltk.ruby.ast.RubyConstantDeclaration;
 import org.eclipse.dltk.ruby.core.IRubyConstants;
 
 public class RubySourceElementRequestor extends SourceElementRequestVisitor {
 
	private static final String NEW_CALL = "new";
 	private static final String ATTR = "attr";
 	private static final String VALUE = "value";
 	private static final String ATTR_ACCESSOR = "attr_accessor";
 	private static final String ATTR_WRITER = "attr_writer";
 	private static final String ATTR_READER = "attr_reader";
 
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
 					"addVariable expression can't be null");
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
 
 	protected String[] processSuperClasses(TypeDeclaration type) {
 		ASTListNode list = type.getSuperClasses();
 
 		if (list == null) {
 			return new String[0];
 		}
 
 		List expressions = list.getChilds();
 		List names = new ArrayList();
 		for (Iterator iter = expressions.iterator(); iter.hasNext();) {
 			ASTNode expr = (ASTNode) iter.next();
 			if (expr instanceof SimpleReference) {
 				names.add(((SimpleReference) expr).getName());
 			} else if (expr instanceof RubyColonExpression) { // FIXME
 				String name = "";
 
 				while (expr instanceof RubyColonExpression) {
 					RubyColonExpression colonExpression = (RubyColonExpression) expr;
 					name = "::" + colonExpression.getName();
 					ASTNode left = colonExpression.getLeft();
 					if (!colonExpression.isFull() && left == null) {
 						name = name.substring(2);
 					}
 					expr = left;
 				}
 
 				if (expr instanceof ConstantReference) {
 					ConstantReference constant = (ConstantReference) expr;
 					name = constant.getName() + name;
 				}
 				names.add(name);
 			}
 		}
 
 		return (String[]) names.toArray(new String[names.size()]);
 	}
 
 	protected String makeLanguageDependentValue(ASTNode value) {
 		String outValue = "";
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
 			System.out.println("==> Method: " + method.getName());
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
 
 	protected static boolean isAttrLike(String name) {
 		return name.equals(ATTR_READER) || name.equals(ATTR_WRITER)
 				|| name.equals(ATTR_ACCESSOR) || name.equals(ATTR);
 	}
 
 	// Visiting expressions
 	public boolean visit(ASTNode expression) throws Exception {
 		if (DLTKCore.DEBUG) {
 			System.out.println("==> Expression: " + expression.toString());
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
 					mi.modifiers = IRubyConstants.RubyAttributeModifier;
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
 					mi.name = attr + "=";
 					mi.modifiers = IRubyConstants.RubyAttributeModifier;
 					mi.nameSourceStart = n.sourceStart();
 					mi.nameSourceEnd = n.sourceEnd() - 1;
 					mi.declarationStart = n.sourceStart();
 
 					fRequestor.enterMethod(mi);
 					fRequestor.exitMethod(n.sourceEnd());
 				}
 			}
 
 			if (name.equals("require")) {
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
			if( callExpression.getName().equals(NEW_CALL)) {
				ASTNode receiver = callExpression.getReceiver();
				if( receiver instanceof ConstantReference ) {
					fRequestor.acceptTypeReference(((ConstantReference)receiver).getName().toCharArray(), receiver.sourceStart());
				}
			}
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
 			if (!oldValue.startsWith("$")) {
 				String newValue = alias.getNewValue();
 				ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 
 				mi.name = newValue;
 				mi.modifiers = IRubyConstants.RubyAliasModifier;
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
 
 }
