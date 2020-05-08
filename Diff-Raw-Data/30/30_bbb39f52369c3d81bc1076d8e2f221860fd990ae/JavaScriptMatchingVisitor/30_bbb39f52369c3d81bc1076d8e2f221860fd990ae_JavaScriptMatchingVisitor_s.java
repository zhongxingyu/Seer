 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.search;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.core.search.matching2.IMatchingCollector;
 import org.eclipse.dltk.internal.javascript.parser.structure.StructureReporter2;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.ITypeInferenceContext;
 import org.eclipse.dltk.internal.javascript.ti.JSMethod;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencerVisitor;
 import org.eclipse.dltk.javascript.ast.Argument;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ObjectInitializerPart;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IMethod;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 
 public class JavaScriptMatchingVisitor extends TypeInferencerVisitor {
 
 	private final IMatchingCollector<MatchingNode> locator;
 
 	private final List<LazyCheck> unresolved = new ArrayList<LazyCheck>();
 
 	/**
 	 * @param context
 	 */
 	public JavaScriptMatchingVisitor(ITypeInferenceContext context,
 			IMatchingCollector<MatchingNode> locator) {
 		super(context);
 		this.locator = locator;
 	}
 
 	private final Stack<ASTNode> visitStack = new Stack<ASTNode>();
 
 	@Override
 	public IValueReference visit(ASTNode node) {
 		visitStack.push(node);
 		try {
 			return super.visit(node);
 		} finally {
 			visitStack.pop();
 		}
 	}
 
 	@Override
 	public IValueReference visitCallExpression(CallExpression node) {
 		final ASTNode expression = node.getExpression();
 		final IValueReference reference = visit(expression);
 		for (ASTNode argument : node.getArguments()) {
 			visit(argument);
 		}
 		if (reference != null) {
 			return reference.getChild(IValueReference.FUNCTION_OP);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public IValueReference visitIdentifier(Identifier node) {
 		final IValueReference result = peekContext().getChild(node.getName());
 		if (!checkIdentifer(node, result, true)) {
 			unresolved.add(new IdentiferCheck(node, result));
 		}
 		return result;
 	}
 
 	/**
 	 * @param node
 	 * @param result
 	 */
 	private boolean checkIdentifer(Identifier node,
 			final IValueReference result, boolean check) {
 		if (result != null && check
 				&& result.getLocation() == ReferenceLocation.UNKNOWN)
 			return false;
 
 		// if this is a function declaration, skip this, will be
 		// reported by visit functionStatement
 		if (StructureReporter2.isFunctionDeclaration(node))
 			return true;
 
 		if (result != null
 				&& result.getAttribute(IReferenceAttributes.PARAMETERS) != null) {
 			if (node.getParent() instanceof PropertyInitializer) {
 				// if property initializer then it is a variable or function
 				// declaration
 				locator.report(new MethodDeclarationNode(node, (IMethod) result
 						.getAttribute(IReferenceAttributes.PARAMETERS)));
 			} else {
 				locator.report(new MethodReferenceNode(node, result));
 			}
 		} else if (result != null && result.getKind().isLocal() && inFunction()) {
 			locator.report(new LocalVariableReferenceNode(node, result
 					.getLocation()));
 		} else {
 			if (node.getParent() instanceof PropertyInitializer) {
 				// if property initializer then it is a variable or function
 				// declaration
 				Set<Type> types = result.getTypes();
 				Type type = null;
 				if (types.size() > 0)
 					type = types.iterator().next();
 				locator.report(new FieldDeclarationNode(node, type));
 			} else {
 				ASTNode parent = node.getParent();
 				if (parent instanceof PropertyExpression
 						&& ((PropertyExpression) parent).getProperty() == node) {
 					while (parent instanceof PropertyExpression) {
 						parent = ((PropertyExpression) parent).getParent();
 					}
 				}
 				if (parent instanceof CallExpression) {
 					locator.report(new MethodReferenceNode(node, result));
 				} else {
 					locator.report(new FieldReferenceNode(node, result));
 				}
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public IValueReference visitObjectInitializer(ObjectInitializer node) {
 		IValueReference result = super.visitObjectInitializer(node);
 		for (ObjectInitializerPart part : node.getInitializers()) {
 			if (part instanceof PropertyInitializer) {
 				final PropertyInitializer pi = (PropertyInitializer) part;
 				if (pi.getName() instanceof Identifier) {
 					Identifier id = (Identifier) pi.getName();
 					final IValueReference child = result.getChild(id.getName());
 					if (!checkIdentifer(id, child, true)) {
 						unresolved.add(new IdentiferCheck(id, result));
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public IValueReference visitPropertyExpression(PropertyExpression node) {
 		final IValueReference object = super.visitPropertyExpression(node);
 		final Expression property = node.getProperty();
 		// TODO (alex) other null checks
 		if (property instanceof Identifier && object != null) {
 			if (!checkIdentifer((Identifier) property, object, true)) {
 				unresolved
 						.add(new IdentiferCheck((Identifier) property, object));
 			}
 		}
 		return object;
 	}
 
 	@Override
 	protected IValueReference createVariable(IValueCollection context,
 			VariableDeclaration declaration) {
 		final IValueReference result = super.createVariable(context,
 				declaration);
 		if (!checkVariable(declaration, result, true)) {
 			unresolved.add(new VariableCheck(declaration, result));
 		}
 		return result;
 	}
 
 	/**
 	 * @param declaration
 	 * @param result
 	 */
 	private boolean checkVariable(VariableDeclaration declaration,
 			final IValueReference result, boolean check) {
 		if (check && result.getLocation() == ReferenceLocation.UNKNOWN)
 			return false;
 
 		if (result.getAttribute(IReferenceAttributes.PARAMETERS) != null) {
 			locator.report(new MethodDeclarationNode(declaration
 					.getIdentifier(), (IMethod) result
 					.getAttribute(IReferenceAttributes.PARAMETERS)));
 		} else if (inFunction()
 				&& !(declaration.getInitializer() instanceof ObjectInitializer)) {
 			locator.report(new LocalVariableDeclarationNode(declaration
 					.getIdentifier(), getSource().getSourceModule(), result
 					.getDeclaredType()));
 		} else {
 			// make sure that all references to this field are seen as
 			// FieldReferences not Local
 			result.setKind(ReferenceKind.FIELD);
 			locator.report(new FieldDeclarationNode(
 					declaration.getIdentifier(), result.getDeclaredType()));
 		}
 
 		return true;
 	}
 
 	@Override
 	public IValueReference visitFunctionStatement(FunctionStatement node) {
 		final IValueReference result = super.visitFunctionStatement(node);
 		Identifier name = node.getName();
 		if (name != null) {
 			IMethod method = (JSMethod) result
 					.getAttribute(IReferenceAttributes.PARAMETERS);
 			locator.report(new MethodDeclarationNode(name, method));
 		}
 		name = StructureReporter2.getThisIdentifier(node);
 		if (name != null) {
 			IMethod method = (JSMethod) result
 					.getAttribute(IReferenceAttributes.PARAMETERS);
 			locator.report(new MethodDeclarationNode(name, method));
 		}
 		return result;
 	}
 
 	@Override
 	protected void visitFunctionBody(FunctionStatement node) {
 		final IValueCollection currentContext = peekContext();
 		for (Argument argument : node.getArguments()) {
 			final IValueReference reference = currentContext.getChild(argument
 					.getArgumentName());
 			locator.report(new ArgumentDeclarationNode(argument, getSource()
 					.getSourceModule(), reference.getDeclaredType()));
 		}
 		super.visitFunctionBody(node);
 	}
 
 	public void flush() {
 		for (LazyCheck check : unresolved) {
 			check.check();
 		}
 		unresolved.clear();
 	}
 
 	interface LazyCheck {
 		void check();
 	}
 
 	class IdentiferCheck implements LazyCheck {
 		private final Identifier node;
 		private final IValueReference result;
 
 		public IdentiferCheck(Identifier node, IValueReference result) {
 			this.node = node;
 			this.result = result;
 		}
 
 		public void check() {
 			checkIdentifer(node, result, false);
 		}
 	}
 
 	public class VariableCheck implements LazyCheck {
 
 		private final VariableDeclaration declaration;
 		private final IValueReference result;
 
 		public VariableCheck(VariableDeclaration declaration,
 				IValueReference result) {
 			this.declaration = declaration;
 			this.result = result;
 		}
 
 		public void check() {
 			checkVariable(declaration, result, false);
 		}
 	}
 }
