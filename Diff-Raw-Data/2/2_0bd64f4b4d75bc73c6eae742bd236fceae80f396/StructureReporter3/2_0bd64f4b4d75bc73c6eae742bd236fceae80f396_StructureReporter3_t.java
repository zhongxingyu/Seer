 /*******************************************************************************
  * Copyright (c) 2012 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.parser.structure;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Stack;
 
 import org.eclipse.dltk.annotations.Nullable;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.core.ISourceNode;
 import org.eclipse.dltk.internal.javascript.ti.JSDocSupport;
 import org.eclipse.dltk.internal.javascript.ti.JSMethod;
 import org.eclipse.dltk.internal.javascript.ti.JSVariable;
 import org.eclipse.dltk.javascript.ast.AbstractNavigationVisitor;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.GetMethod;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.JSNode;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ObjectInitializerPart;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.SetMethod;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.ast.VoidExpression;
 import org.eclipse.dltk.javascript.parser.JSParser;
 import org.eclipse.dltk.javascript.parser.JSProblemReporter;
 import org.eclipse.dltk.javascript.structure.FunctionDeclaration;
 import org.eclipse.dltk.javascript.structure.FunctionExpression;
 import org.eclipse.dltk.javascript.structure.FunctionNode;
 import org.eclipse.dltk.javascript.structure.IDeclaration;
 import org.eclipse.dltk.javascript.structure.IParentNode;
 import org.eclipse.dltk.javascript.structure.IStructureHandler;
 import org.eclipse.dltk.javascript.structure.IStructureNode;
 import org.eclipse.dltk.javascript.structure.IStructureVisitor;
 import org.eclipse.dltk.javascript.structure.ObjectDeclaration;
 import org.eclipse.dltk.javascript.structure.PropertyDeclaration;
 import org.eclipse.dltk.javascript.structure.ScriptScope;
 import org.eclipse.dltk.javascript.structure.VariableNode;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinfo.ITypeChecker;
 import org.eclipse.dltk.javascript.typeinfo.ReferenceSource;
 import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
 
 public class StructureReporter3 extends
 		AbstractNavigationVisitor<IStructureNode> implements IStructureVisitor {
 
 	private final ReferenceSource referenceSource;
 	private final Stack<IParentNode> parents = new Stack<IParentNode>();
 	private final IStructureHandler[] handlers;
 
 	private JSDocSupport jsdocSupport = new JSDocSupport();
 	private final JSProblemReporter fReporter = null;
 	private final ITypeChecker fTypeChecker = null;
 
 	public StructureReporter3(ReferenceSource referenceSource) {
 		this.referenceSource = referenceSource;
 		final List<IStructureHandler> extensions = TypeInfoManager
 				.createExtensions(referenceSource, IStructureHandler.class,
 						this);
 		this.handlers = extensions.toArray(new IStructureHandler[extensions
 				.size()]);
 	}
 
 	protected boolean isStructure() {
 		return true;
 	}
 
 	@Override
 	public IStructureNode visit(ASTNode node) {
 		for (IStructureHandler handler : handlers) {
 			final IStructureNode value = handler.handle(node);
 			if (value != IStructureHandler.CONTINUE) {
 				return addToParent(value);
 			}
 		}
 		return addToParent(super.visit(node));
 	}
 
 	private IStructureNode addToParent(@Nullable final IStructureNode value) {
 		if (value != null) {
 			if (!parents.isEmpty()) {
 				final IParentNode parent = parents.peek();
 				parent.addToScope(value);
 			}
 		}
 		return value;
 	}
 
 	public void push(IParentNode declaration) {
 		parents.push(declaration);
 	}
 
 	public IParentNode pop() {
 		return parents.pop();
 	}
 
 	public IParentNode peek() {
 		return parents.peek();
 	}
 
 	@Override
 	public IStructureNode visitScript(Script node) {
 		push(new ScriptScope());
 		// TODO visit scope declarations first?
 		super.visitScript(node);
 		return pop();
 	}
 
 	@Override
 	public IStructureNode visitFunctionStatement(FunctionStatement node) {
		final JSMethod method = new JSMethod(node, referenceSource);
 		jsdocSupport.processMethod(node, method, fReporter, fTypeChecker);
 		final FunctionNode functionNode;
 		if (node.isDeclaration()) {
 			functionNode = new FunctionDeclaration(peek(), node, method);
 		} else {
 			functionNode = new FunctionExpression(peek(), node, method);
 		}
 		method.setLocation(ReferenceLocation.create(referenceSource,
 				node.start(), node.end(), functionNode.getNameNode()));
 		functionNode.buildArgumentNodes();
 		push(functionNode);
 		// TODO visit scope declarations?
 		super.visitFunctionStatement(node);
 		return pop();
 	}
 
 	@Override
 	protected void processVariable(VariableDeclaration declaration) {
 		if (isStructure()) {
 			for (IStructureHandler handler : handlers) {
 				final IStructureNode value = handler.handle(declaration);
 				if (value != IStructureHandler.CONTINUE) {
 					if (value != null) {
 						peek().getScope().addChild(value);
 					}
 					return;
 				}
 			}
 		}
 		if (declaration.getInitializer() instanceof FunctionStatement) {
 			peek().getScope().addChild(
 					buildFunctionDeclarationFromAssignment(declaration,
 							(FunctionStatement) declaration.getInitializer(),
 							Collections.<Expression> singletonList(declaration
 									.getIdentifier())));
 			return;
 		}
 		final JSVariable variable = new JSVariable(
 				declaration.getVariableName());
 		variable.setLocation(declaration.getInitializer() != null ? ReferenceLocation
 				.create(referenceSource, declaration.start(),
 						declaration.end(), declaration.getIdentifier())
 				: ReferenceLocation.create(referenceSource,
 						declaration.start(), declaration.end()));
 		jsdocSupport.processVariable(declaration, variable, fReporter,
 				fTypeChecker);
 		final VariableNode variableNode = new VariableNode(peek(), declaration,
 				variable);
 		peek().getScope().addChild(variableNode);
 		final Expression initializer = declaration.getInitializer();
 		if (initializer != null) {
 			push(variableNode);
 			variableNode.setValue(visit(initializer));
 			pop();
 		}
 	}
 
 	@Override
 	public IStructureNode visitObjectInitializer(ObjectInitializer node) {
 		final ObjectDeclaration object = new ObjectDeclaration(peek());
 		for (ObjectInitializerPart part : node.getInitializers()) {
 			if (part instanceof GetMethod) {
 				visitMethod((GetMethod) part);
 				// TODO (alex) handle GetMethod
 			} else if (part instanceof SetMethod) {
 				visitMethod((SetMethod) part);
 				// TODO (alex) handle SetMethod
 			} else if (part instanceof PropertyInitializer) {
 				final PropertyInitializer pi = (PropertyInitializer) part;
 				final String name;
 				if (pi.getName() instanceof Identifier) {
 					name = ((Identifier) pi.getName()).getName();
 				} else if (pi.getName() instanceof StringLiteral) {
 					name = ((StringLiteral) pi.getName()).getValue();
 				} else if (pi.getName() instanceof DecimalLiteral) {
 					name = ((DecimalLiteral) pi.getName()).getText();
 				} else {
 					name = "";
 					visit(pi.getName());
 				}
 				final PropertyDeclaration propertyDeclaration = new PropertyDeclaration(
 						peek(), name, pi, ReferenceLocation.create(
 								referenceSource, pi.start(), pi.end(),
 								pi.getName()));
 				object.addChild(propertyDeclaration);
 				push(propertyDeclaration);
 				propertyDeclaration.setValue(visit(pi.getValue()));
 				pop();
 			}
 		}
 		return !object.getChildren().isEmpty() ? object : null;
 	}
 
 	@Override
 	public IStructureNode visitPropertyExpression(PropertyExpression node) {
 		visit(node.getObject());
 		final Expression property = node.getProperty();
 		if (property instanceof Identifier) {
 			final Identifier identifier = (Identifier) property;
 			if (isCallExpression(node)) {
 				peek().addMethodReference(identifier,
 						getCallArgumentCount(node));
 			} else {
 				peek().addFieldReference(identifier);
 			}
 		} else {
 			visit(property);
 		}
 		return null;
 	}
 
 	@Override
 	public IStructureNode visitIdentifier(Identifier node) {
 		final String name = node.getName();
 		final IDeclaration resolved = peek().getScope().resolve(name);
 		if (resolved != null) {
 			if (resolved.getParent() instanceof ScriptScope) {
 				// TODO (alex) option to treat it always as local or not
 				if (resolved instanceof FunctionNode) {
 					peek().addMethodReference(
 							node,
 							isCallExpression(node) ? getCallArgumentCount(node)
 									: 0);
 				} else {
 					peek().addFieldReference(node);
 				}
 			} else {
 				peek().addLocalReference(node, resolved);
 			}
 		} else {
 			if (isCallExpression(node)) {
 				peek().addMethodReference(node, getCallArgumentCount(node));
 			} else {
 				peek().addFieldReference(node);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public IStructureNode visitVoidExpression(VoidExpression node) {
 		final Expression expression = node.getExpression();
 		if (expression instanceof FunctionStatement
 				|| expression instanceof VariableStatement
 				|| expression instanceof BinaryOperation
 				&& isAssignment((BinaryOperation) expression)) {
 			visit(expression);
 		} else {
 			final ExpressionNode expressionNode = new ExpressionNode(peek());
 			push(expressionNode);
 			visit(expression);
 			pop();
 		}
 		return null;
 	}
 
 	private boolean isAssignment(BinaryOperation operation) {
 		return operation.getOperation() == JSParser.ASSIGN;
 	}
 
 	@Override
 	public IStructureNode visitBinaryOperation(BinaryOperation node) {
 		if (node.getOperation() == JSParser.ASSIGN) {
 			final Expression left = node.getLeftExpression();
 			final Expression right = node.getRightExpression();
 			if (left instanceof PropertyExpression) {
 				final List<Expression> path = ((PropertyExpression) left)
 						.getPath();
 				boolean thisReference = false;
 				if (path.get(0) instanceof ThisExpression) {
 					path.remove(0);
 					thisReference = true;
 				}
 				if (isValidPath(path)) {
 					if (right instanceof FunctionStatement) {
 						return buildFunctionDeclarationFromAssignment(node,
 								(FunctionStatement) right, path);
 					} else if (thisReference) {
 						final FieldNode fieldNode = new FieldNode(peek(), node,
 								joinPath(path), ReferenceLocation.create(
 										referenceSource, node.start(),
 										node.end(), getNameNode(path)));
 						push(fieldNode);
 						visit(right);
 						return pop();
 					}
 				}
 			} else if (left instanceof Identifier
 					&& right instanceof FunctionStatement) {
 				return buildFunctionDeclarationFromAssignment(node,
 						(FunctionStatement) right,
 						Collections.singletonList(left));
 			}
 		}
 		return super.visitBinaryOperation(node);
 	}
 
 	private IStructureNode buildFunctionDeclarationFromAssignment(JSNode node,
 			FunctionStatement function, final List<Expression> path) {
 		final JSMethod method = new JSMethod(function, ReferenceSource.UNKNOWN);
 		jsdocSupport.processMethod(function, method, fReporter, fTypeChecker);
 		final FunctionNode functionNode = new FunctionDeclarationExpressionLike(
 				peek(), function, method, joinPath(path), getNameNode(path));
 		method.setLocation(ReferenceLocation.create(referenceSource,
 				node.start(), node.end(), functionNode.getNameNode()));
 		functionNode.buildArgumentNodes();
 		push(functionNode);
 		// TODO visit scope declarations?
 		super.visitFunctionStatement(function);
 		return pop();
 	}
 
 	private static ISourceNode getNameNode(List<Expression> path) {
 		if (path.size() == 1) {
 			return path.get(0);
 		}
 		return new NameNode(path.get(0).start(), path.get(path.size() - 1)
 				.end());
 	}
 
 	private static String joinPath(List<Expression> path) {
 		if (path.size() == 1) {
 			return ((Identifier) path.get(0)).getName();
 		}
 		final StringBuilder sb = new StringBuilder();
 		for (Expression expression : path) {
 			if (sb.length() != 0) {
 				sb.append('.');
 			}
 			sb.append(((Identifier) expression).getName());
 		}
 		return sb.toString();
 	}
 
 	private boolean isValidPath(List<Expression> path) {
 		for (Expression expression : path) {
 			if (!(expression instanceof Identifier)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean isCallExpression(Expression node) {
 		final ASTNode parent = node.getParent();
 		return parent instanceof CallExpression
 				&& ((CallExpression) parent).getExpression() == node;
 	}
 
 	private int getCallArgumentCount(Expression node) {
 		return ((CallExpression) node.getParent()).getArguments().size();
 	}
 
 }
