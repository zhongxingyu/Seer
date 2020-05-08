 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.internal.core.parser.visitors;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.PositionInformation;
 import org.eclipse.dltk.ast.declarations.Argument;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.expressions.ExpressionList;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.compiler.SourceElementRequestVisitor;
 import org.eclipse.dltk.python.parser.ast.PythonImportStatement;
 import org.eclipse.dltk.python.parser.ast.expressions.Assignment;
 import org.eclipse.dltk.python.parser.ast.expressions.ExtendedVariableReference;
 import org.eclipse.dltk.python.parser.ast.expressions.PythonImportAsExpression;
 import org.eclipse.dltk.python.parser.ast.expressions.PythonImportExpression;
 import org.eclipse.dltk.python.parser.ast.expressions.PythonLambdaExpression;
 import org.eclipse.dltk.utils.CorePrinter;
 
 public class PythonSourceElementRequestor extends SourceElementRequestVisitor {
 
 	private static class TypeField {
 		private String fName;
 
 		private String fInitValue;
 
 		private PositionInformation fPos;
 
 		private Expression fExpression;
 
 		private ASTNode fToNode;
 
 		private ASTNode declaredIn; // The node where the declaration was found
 
 		// (should be either class or method node)
 		TypeField(String name, String initValue, PositionInformation pos,
 				Expression expression, ASTNode toNode, ASTNode declaredIn) {
 
 			this.fName = name;
 			this.fInitValue = initValue;
 			this.fPos = pos;
 			this.fExpression = expression;
 			this.fToNode = toNode;
 			this.declaredIn = declaredIn;
 		}
 
 		String getName() {
 
 			return this.fName;
 		}
 
 		String getInitValue() {
 
 			return this.fInitValue;
 		}
 
 		PositionInformation getPos() {
 
 			return this.fPos;
 		}
 
 		Expression getExpression() {
 
 			return this.fExpression;
 		}
 
 		ASTNode getToNode() {
 
 			return this.fToNode;
 		}
 
 		public boolean equals(Object obj) {
 
 			if (obj instanceof TypeField) {
 				TypeField second = (TypeField) obj;
 				return second.fName.equals(this.fName)
 						&& second.fToNode.equals(this.fToNode);
 			}
 			return super.equals(obj);
 		}
 
 		public String toString() {
 
 			return this.fName;
 		}
 
 		public ASTNode getDeclaredIn() {
 			return declaredIn;
 		}
 
 	}
 
 	private List fNotAddedFields = new ArrayList();
 
 	/**
 	 * Used to determine duplicate names.
 	 */
 	private Map fTypeVariables = new HashMap();
 
 	public PythonSourceElementRequestor(ISourceElementRequestor requestor) {
 
 		super(requestor);
 	}
 
 	/**
 	 * Used to create Call value in python syntax.
 	 */
 	protected String makeLanguageDependentValue(Expression value) {
 
 		String outValue = "";
 		if (value instanceof ExtendedVariableReference) {
 			StringWriter stringWriter = new StringWriter();
 			CorePrinter printer = new CorePrinter(stringWriter);
 			value.printNode(printer);
 			printer.flush();
 			return stringWriter.getBuffer().toString();
 		}
 		return outValue;
 	}
 
 	private void onVisitLambdaAssignnment(String ref,
 			PythonLambdaExpression lambdaExpression) {
 		// Declare new Method.
 		List/* < Argument > */args = lambdaExpression.getArguments();
 
 		String[] parameters = new String[args.size()];
 		// IParameter[] parameter = new IParameter [ args.size( ) ];
 		for (int a = 0; a < args.size(); a++) {
 			Argument arg = (Argument) args.get(a);
 			// parameter[ a ] = builder.declareParameter( arg.getName(
 			// ), arg.getModifiers( ) );
 			parameters[a] = arg.getName();
 		}
 
 		ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 		mi.parameterNames = parameters;
 		mi.name = ref;
 		mi.modifiers = 0;
 		mi.nameSourceStart = lambdaExpression.sourceStart();
 		mi.nameSourceEnd = lambdaExpression.sourceEnd() - 1;
 		mi.declarationStart = lambdaExpression.sourceStart();
 
 		this.fRequestor.enterMethod(mi);
 		this.fRequestor.exitMethod(lambdaExpression.sourceEnd());
 	}
 
 	private void onVisitStaticVariableAssignment(SimpleReference var,
 			Statement val) {
 
 		if (canAddVariables((ASTNode) this.fNodes.peek(), var.getName())) {
 			ISourceElementRequestor.FieldInfo info = new ISourceElementRequestor.FieldInfo();
 			info.modifiers = Modifiers.AccStatic;
 			info.name = var.getName();
 			info.nameSourceEnd = var.sourceEnd() - 1;
 			info.nameSourceStart = var.sourceStart();
 			info.declarationStart = var.sourceStart();
 			this.fRequestor.enterField(info);
 			if (val != null) {
 				this.fRequestor.exitField(val.sourceEnd() - 1);
 			} else {
 				this.fRequestor.exitField(var.sourceEnd() - 1);
 			}
 		}
 	}
 
 	private void onVisitInstanceVariableAssignment(
 			ExtendedVariableReference extendedVariable, Statement right) {
 		List varParts = extendedVariable.getExpressions();
 		if (extendedVariable.isDot(0)) {
 			Expression first = (Expression) varParts.get(0);
 			Expression second = (Expression) varParts.get(1);
 
 			if (first instanceof VariableReference
 					&& second instanceof VariableReference) {
 				String varName = ((VariableReference) first).getName();
 				MethodDeclaration currentMethod = this.getCurrentMethod();
 				List arguments = currentMethod.getArguments();
 				if (arguments != null && arguments.size() > 0) {
 					Argument firstArgument = (Argument) arguments.get(0);
 					String argumentName = firstArgument.getName();
 					if (argumentName.equals(varName)) {
 						VariableReference var = (VariableReference) second;
 						int initialValueStart = 0;
 						int initialValueEnd = 0;
 						if (right != null) {
 							initialValueStart = right.sourceStart();
 							initialValueEnd = right.sourceEnd();
 						}
 						PositionInformation pos = new PositionInformation(var
 								.sourceStart(), var.sourceEnd(),
 								initialValueStart, initialValueEnd);
 						String initialString = this.makeValue(right);
 						ASTNode method = (ASTNode) this.fNodes.pop();
 						ASTNode toClass = (ASTNode) this.fNodes.peek();
 						this.fNodes.push(method);
 						if (toClass instanceof TypeDeclaration) {
 							List decorators = ((MethodDeclaration) method)
 									.getDecorators();
 							if (null == decorators || null != decorators
 									&& decorators.size() == 0) {
 								TypeField field = new TypeField(var.getName(),
 										initialString, pos, extendedVariable,
 										toClass, method);
 								this.fNotAddedFields.add(field);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void onVisitTestListAssignment(ExpressionList left, Statement right) {
 		Iterator iter = left.getChilds().iterator();
 		if (right instanceof ExpressionList) {
 			ExpressionList exprs = (ExpressionList) right;
 			Iterator j = exprs.getChilds().iterator();
 			while (iter.hasNext() && j.hasNext()) {
 				Expression expr = (Expression) iter.next();
 				processAssignment(expr, (Expression) j.next());
 			}
 
 		} else {
 			while (iter.hasNext()) {
 				Expression expr = (Expression) iter.next();
 				processAssignment(expr, right);
 			}
 		}
 	}
 
 	private void processAssignment(Statement left, Statement right) {
 		if (left instanceof Assignment) {
 			Assignment assignment = (Assignment) left;
 			processAssignment(assignment.getLeft(), right);
 			processAssignment(assignment.getRight(), right);
 		} else if (left instanceof SimpleReference
 				&& right instanceof PythonLambdaExpression)
 			onVisitLambdaAssignnment(((SimpleReference) left).getName(),
 					(PythonLambdaExpression) right);
 		else if (left instanceof VariableReference && !this.fInMethod) // Handle
 			// static
 			// variables
 			onVisitStaticVariableAssignment((VariableReference) left, right);
 		else if (left instanceof ExtendedVariableReference && this.fInClass
 				&& this.fInMethod) // This is for in class and in method.
 			onVisitInstanceVariableAssignment((ExtendedVariableReference) left,
 					right);
 		else if (left instanceof ExpressionList)
 			onVisitTestListAssignment((ExpressionList) left, right);
 		else {// TODO: dynamic variable handling not yet
 			// supported.
 		}
 	}
 
 	public boolean visit(Expression expression) throws Exception {
 		if (expression instanceof Assignment) {
 			// this is static variable assignment.
 			Assignment assignment = (Assignment) expression;
 			Statement left = assignment.getLeft();
 			Statement right = assignment.getRight();
 			if (left == null) {
 				return true;
 				// throw new RuntimeException("addVariable expression can't be
 				// null");
 			}
 			processAssignment(left, right);
 			return true;
 		} else if (expression instanceof ExtendedVariableReference) {
 			ExtendedVariableReference ref = (ExtendedVariableReference) expression;
 			int expressionCount = ref.getExpressionCount();
 			for (int i = 0; i < expressionCount; i++) {
 				Expression e = ref.getExpression(i);
 				if (ref.isCall(i) && e instanceof VariableReference) {
 					this.fRequestor.acceptMethodReference(
 							((VariableReference) e).getName().toCharArray(), 0,
 							e.sourceStart(), e.sourceEnd());
 				} else if (e instanceof VariableReference) {
 					this.fRequestor.acceptFieldReference(
 							((VariableReference) e).getName().toCharArray(), e
 									.sourceStart());
 				}
 			}
 		} else if (expression instanceof VariableReference) {
 			this.fRequestor.acceptFieldReference(
 					((VariableReference) expression).getName().toCharArray(),
 					expression.sourceStart());
 		}
 		return true;
 	}
 
 	public boolean endvisit(Expression expression) throws Exception {
 		return true;
 	}
 
 	protected void onEndVisitMethod(MethodDeclaration method) {
 
 		if (fNotAddedFields.size() >= 1) {
 			TypeField typeField = (TypeField) fNotAddedFields.get(0);
 			if (null != typeField && typeField.getDeclaredIn().equals(method)) {
 				Iterator i = this.fNotAddedFields.iterator();
 				while (i.hasNext()) {
 					TypeField field = (TypeField) i.next();
 					if (canAddVariables(field.getToNode(), field.getName())) {
 
 						PositionInformation pos = field.getPos();
 
 						ISourceElementRequestor.FieldInfo info = new ISourceElementRequestor.FieldInfo();
 						info.modifiers = Modifiers.AccStatic;
 						info.name = field.getName();
 						info.nameSourceEnd = pos.nameEnd - 1;
 						info.nameSourceStart = pos.nameStart;
 						info.declarationStart = pos.sourceStart;
 						this.fRequestor.enterField(info);
 						this.fRequestor.exitField(pos.sourceEnd);
 					}
 				}
 				this.fNotAddedFields.clear();
 			}
 		}
 	}
 
 	public boolean visit(Statement statement) throws Exception {
 
 		if (statement instanceof PythonImportStatement) {
 			PythonImportStatement importStatement = (PythonImportStatement) statement;
 			List/* < Expression > */exprs = importStatement.getImports();
 
 			// PositionInformation position = importStatement.getPosition();
 			// TODO: Add correct positioning for each as and multiple import per
 			// statement.
 
 			Iterator i = exprs.iterator();
 			while (i.hasNext()) {
 				Expression ex = (Expression) i.next();
 				if (ex instanceof PythonImportAsExpression) {
 					// PythonImportAsExpression importAs =
 					// (PythonImportAsExpression) ex;
 					// TODO: Add correct import declarations here? Or may be not
 					// add it here?
 					// this.builder.declareField( Modifiers.AccModule,
 					// importAs.getName(), null, position);
 					// this.builder.declareImport(ex, position);
 				} else if (ex instanceof PythonImportExpression) {
 					PythonImportExpression importName = (PythonImportExpression) ex;
 
 					String name = importName.getName();
 
 					if (name.indexOf(".") != -1) { // also adding modules like
 						// a.b.c and a and a.b
 						String sname = name;
 						while (sname.indexOf(".") != -1) {
 							sname = sname.substring(0, sname.lastIndexOf("."));
 						}
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	private boolean canAddVariables(ASTNode type, String name) {
 
 		if (this.fTypeVariables.containsKey(type)) {
 			List variables = (List) this.fTypeVariables.get(type);
 			if (variables.contains(name)) {
 				return false;
 			}
 			variables.add(name);
 			return true;
 		} else {
 			List variables = new ArrayList();
 			variables.add(name);
 			this.fTypeVariables.put(type, variables);
 			return true;
 		}
 	}
 
 	public boolean endvisit(Statement s) throws Exception {
 		return true;
 	}
 
 	public boolean visit(MethodDeclaration method) throws Exception {
 		this.fNodes.push(method);
 		List/* < Argument > */args = method.getArguments();
 		String[] parameter = new String[args.size()];
 		for (int a = 0; a < args.size(); a++) {
 			Argument arg = (Argument) args.get(a);
 			parameter[a] = arg.getName();
 		}
 
 		ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 		mi.parameterNames = parameter;
 		mi.name = method.getName();
 		mi.modifiers = method.getModifiers();
 		mi.nameSourceStart = method.getNameStart();
 		mi.nameSourceEnd = method.getNameEnd() - 1;
 		mi.declarationStart = method.sourceStart();
 
		this.fRequestor.enterMethodRemoveSame(mi);
 
 		this.fInMethod = true;
 		this.fCurrentMethod = method;
 		return true;
 	}
 }
