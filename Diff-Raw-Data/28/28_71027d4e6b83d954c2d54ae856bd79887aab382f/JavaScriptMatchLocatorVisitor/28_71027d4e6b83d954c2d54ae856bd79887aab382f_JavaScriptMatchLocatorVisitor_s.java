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
 package org.eclipse.dltk.javascript.internal.search;
 
 import static org.eclipse.dltk.internal.javascript.parser.structure.StructureReporter3.isCallExpression;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.core.search.matching2.MatchingCollector;
 import org.eclipse.dltk.internal.javascript.parser.structure.StructureRequestor;
 import org.eclipse.dltk.internal.javascript.ti.JSDocSupport;
 import org.eclipse.dltk.internal.javascript.ti.JSDocSupport.ParameterNode;
 import org.eclipse.dltk.internal.javascript.ti.JSMethod;
 import org.eclipse.dltk.internal.javascript.ti.JSVariable;
 import org.eclipse.dltk.javascript.ast.AbstractNavigationVisitor;
 import org.eclipse.dltk.javascript.ast.Argument;
 import org.eclipse.dltk.javascript.ast.Comment;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.JSDeclaration;
 import org.eclipse.dltk.javascript.ast.JSScope;
 import org.eclipse.dltk.javascript.ast.Method;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ObjectInitializerPart;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.parser.JSProblemReporter;
 import org.eclipse.dltk.javascript.parser.jsdoc.JSDocTag;
 import org.eclipse.dltk.javascript.parser.jsdoc.JSDocTags;
 import org.eclipse.dltk.javascript.search.IMatchLocatorHandler;
 import org.eclipse.dltk.javascript.search.IMatchLocatorValue;
 import org.eclipse.dltk.javascript.search.IMatchLocatorVisitor;
 import org.eclipse.dltk.javascript.structure.FunctionDeclaration;
 import org.eclipse.dltk.javascript.structure.FunctionExpression;
 import org.eclipse.dltk.javascript.structure.FunctionNode;
 import org.eclipse.dltk.javascript.structure.IDeclaration;
 import org.eclipse.dltk.javascript.structure.IScope;
 import org.eclipse.dltk.javascript.structure.ScriptScope;
 import org.eclipse.dltk.javascript.structure.VariableNode;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IMethod;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IParameter;
 import org.eclipse.dltk.javascript.typeinfo.ITypeChecker;
 import org.eclipse.dltk.javascript.typeinfo.ReferenceSource;
 import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 
 public class JavaScriptMatchLocatorVisitor extends
 		AbstractNavigationVisitor<IMatchLocatorValue> implements
 		IMatchLocatorVisitor {
 
 	private final ReferenceSource referenceSource;
 	private final IMatchLocatorHandler[] handlers;
 
 	private JSDocSupport jsdocSupport = new JSDocSupport();
 	private final JSProblemReporter fReporter = null;
 	private final ITypeChecker fTypeChecker = null;
 
 	private final Stack<IScope> scopes = new Stack<IScope>();
 
 	private final List<MatchingNode> nodes = new ArrayList<MatchingNode>();
 
 	public JavaScriptMatchLocatorVisitor(ReferenceSource referenceSource) {
 		this.referenceSource = referenceSource;
 		final List<IMatchLocatorHandler> extensions = TypeInfoManager
 				.createExtensions(referenceSource, IMatchLocatorHandler.class,
 						this);
 		this.handlers = extensions.toArray(new IMatchLocatorHandler[extensions
 				.size()]);
 	}
 
 	public void report(MatchingCollector<MatchingNode> matchingCollector) {
 		for (MatchingNode node : nodes) {
 			matchingCollector.report(node);
 		}
 		nodes.clear();
 	}
 
 	@Override
 	public IMatchLocatorValue visit(ASTNode node) {
 		for (IMatchLocatorHandler handler : handlers) {
 			final IMatchLocatorValue value = handler.handle(node);
 			if (value != IMatchLocatorHandler.CONTINUE) {
 				return value;
 			}
 		}
 		return super.visit(node);
 	}
 
 	private void push(IScope scope) {
 		scopes.push(scope);
 	}
 
 	private IScope pop() {
 		return scopes.pop();
 	}
 
 	private IScope peek() {
 		return scopes.peek();
 	}
 
 	@Override
 	public IMatchLocatorValue visitScript(Script node) {
 		scopes.clear();
 		push(new ScriptScope());
 		handleScopeDeclarations(node);
 		super.visitScript(node);
 		pop();
 		return null;
 	}
 
 	@Override
 	public IMatchLocatorValue visitFunctionStatement(FunctionStatement node) {
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
 		addFunctionDeclaration(node.getName(), node, method);
 		visitFunctionBody(node);
 		pop();
 		return null;
 	}
 
 	public void visitFunctionBody(FunctionStatement function) {
 		handleScopeDeclarations(function);
 		super.visitFunctionStatement(function);
 	}
 
 	public void addFunctionDeclaration(Expression identifier,
 			FunctionStatement function, IMethod method) {
 		nodes.add(new MethodDeclarationNode(identifier != null ? identifier
 				: function, method));
 
 		Map<String, Argument> arguments = new HashMap<String, Argument>();
 		for (Argument argument : function.getArguments()) {
 			arguments.put(argument.getIdentifier().getName(), argument);
 		}
 		for (IParameter parameter : method.getParameters()) {
 			final Argument argument = arguments.get(parameter.getName());
 			if (argument != null) {
 				nodes.add(new ArgumentDeclarationNode(argument, method
 						.getLocation().getSourceModule(), parameter.getType()));
 			}
 		}
 		final Comment comment = JSDocSupport.getComment(function);
 		if (comment != null) {
 			final JSDocTags tags = JSDocSupport.parse(comment);
 			for (JSDocTag tag : tags.list(JSDocTag.PARAM)) {
 				final ParameterNode node = JSDocSupport.parseParameter(tag);
 				if (node != null) {
 					final IParameter parameter = method.getParameter(node.name);
 					if (parameter != null) {
 						final Identifier ref = new Identifier(null);
 						ref.setName(node.name);
						ref.setStart(node.offset);
						ref.setEnd(node.offset + node.name.length());
 						nodes.add(new LocalVariableReferenceNode(ref, parameter
 								.getLocation(), true));
 					}
 				}
 			}
 		}
 	}
 
 	public void addFieldDeclaration(Expression identifier, JSType type) {
 		nodes.add(new FieldDeclarationNode(identifier, referenceSource
 				.getSourceModule(), type));
 	}
 
 	private void handleScopeDeclarations(JSScope scope) {
 		for (JSDeclaration declaration : scope.getDeclarations()) {
 			if (declaration instanceof VariableDeclaration) {
 				createVariable((VariableDeclaration) declaration);
 			} else if (declaration instanceof FunctionStatement) {
 				// TODO Auto-generated method stub
 
 			}
 		}
 	}
 
 	private void createVariable(VariableDeclaration declaration) {
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
 		peek().addChild(variableNode);
 		if (scopes.size() == 1) {
 			// TODO (alex) option to treat it as field or local
 			addFieldDeclaration(declaration.getIdentifier(), variable.getType());
 		} else {
 			nodes.add(new LocalVariableDeclarationNode(declaration
 					.getIdentifier(), referenceSource.getSourceModule(),
 					variable.getType()));
 		}
 	}
 
 	@Override
 	public IMatchLocatorValue visitPropertyExpression(PropertyExpression node) {
 		visit(node.getObject());
 		final Expression property = node.getProperty();
 		if (property instanceof Identifier) {
 			final Identifier identifier = (Identifier) property;
 			if (isCallExpression(node)) {
 				nodes.add(new MethodReferenceNode(identifier));
 			} else {
 				nodes.add(new FieldReferenceNode(identifier));
 			}
 		} else {
 			visit(property);
 		}
 		return null;
 	}
 
 	@Override
 	public IMatchLocatorValue visitIdentifier(Identifier node) {
 		final String name = node.getName();
 		final IDeclaration resolved = peek().resolve(name);
 		if (resolved != null) {
 			if (resolved.getParent() instanceof ScriptScope) {
 				// TODO (alex) option to treat it always as local or not
 				if (resolved instanceof FunctionNode) {
 					nodes.add(new MethodReferenceNode(node, resolved
 							.getLocation()));
 				} else {
 					nodes.add(new FieldReferenceNode(node, resolved
 							.getLocation()));
 				}
 			} else {
 				nodes.add(new LocalVariableReferenceNode(node, resolved
 						.getLocation()));
 			}
 		} else {
 			if (isCallExpression(node)) {
 				nodes.add(new MethodReferenceNode(node));
 			} else {
 				nodes.add(new FieldReferenceNode(node));
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public IMatchLocatorValue visitObjectInitializer(ObjectInitializer node) {
 		for (ObjectInitializerPart part : node.getInitializers()) {
 			if (part instanceof Method) {
 				// TODO (alex) handle get & set methods
 				visitMethod((Method) part);
 			} else if (part instanceof PropertyInitializer) {
 				final PropertyInitializer pi = (PropertyInitializer) part;
 				// TODO (alex) handle jsdoc
 				// TODO (alex) treat as method declaration if function
 				addFieldDeclaration(pi.getName(), null /* declaredType */);
 				visit(pi.getValue());
 			}
 		}
 		return null;
 	}
 
 	public void acceptTypeReference(ASTNode node, String typeName) {
 		nodes.add(new TypeReferenceNode(node, typeName));
 	}
 
 	public void acceptTypeReference(ASTNode node, JSType type) {
 		nodes.add(new TypeReferenceNode(node, StructureRequestor
 				.collectContainedTypeNames(type)));
 	}
 }
