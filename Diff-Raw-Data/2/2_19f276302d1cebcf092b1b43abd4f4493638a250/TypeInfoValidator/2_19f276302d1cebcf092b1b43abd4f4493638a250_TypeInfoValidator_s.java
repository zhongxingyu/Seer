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
 package org.eclipse.dltk.internal.javascript.validation;
 
 import static org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes.PHANTOM;
 import static org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes.R_METHOD;
 import static org.eclipse.dltk.internal.javascript.validation.JavaScriptValidations.typeOf;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.core.builder.IBuildContext;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.internal.javascript.parser.JSDocValidatorFactory.TypeChecker;
 import org.eclipse.dltk.internal.javascript.ti.ConstantValue;
 import org.eclipse.dltk.internal.javascript.ti.ElementValue;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.ITypeInferenceContext;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencerVisitor;
 import org.eclipse.dltk.javascript.ast.Argument;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.GetArrayItemExpression;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.IfStatement;
 import org.eclipse.dltk.javascript.ast.JSNode;
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.parser.ISuppressWarningsState;
 import org.eclipse.dltk.javascript.parser.JSParser;
 import org.eclipse.dltk.javascript.parser.JSProblemReporter;
 import org.eclipse.dltk.javascript.parser.PropertyExpressionUtils;
 import org.eclipse.dltk.javascript.typeinference.IAssignProtection;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.PhantomValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinference.ValueReferenceUtil;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IVariable;
 import org.eclipse.dltk.javascript.typeinfo.IRAnyType;
 import org.eclipse.dltk.javascript.typeinfo.IRClassType;
 import org.eclipse.dltk.javascript.typeinfo.IRMember;
 import org.eclipse.dltk.javascript.typeinfo.IRMethod;
 import org.eclipse.dltk.javascript.typeinfo.IRParameter;
 import org.eclipse.dltk.javascript.typeinfo.IRRecordMember;
 import org.eclipse.dltk.javascript.typeinfo.IRRecordType;
 import org.eclipse.dltk.javascript.typeinfo.IRSimpleType;
 import org.eclipse.dltk.javascript.typeinfo.IRType;
 import org.eclipse.dltk.javascript.typeinfo.IRVariable;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.JSTypeSet;
 import org.eclipse.dltk.javascript.typeinfo.MemberPredicate;
 import org.eclipse.dltk.javascript.typeinfo.RModelBuilder;
 import org.eclipse.dltk.javascript.typeinfo.TypeCompatibility;
 import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
 import org.eclipse.dltk.javascript.typeinfo.model.Element;
 import org.eclipse.dltk.javascript.typeinfo.model.GenericMethod;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Method;
 import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
 import org.eclipse.dltk.javascript.typeinfo.model.ParameterKind;
 import org.eclipse.dltk.javascript.typeinfo.model.Property;
 import org.eclipse.dltk.javascript.typeinfo.model.RecordMember;
 import org.eclipse.dltk.javascript.typeinfo.model.RecordType;
 import org.eclipse.dltk.javascript.typeinfo.model.SimpleType;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelLoader;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.osgi.util.NLS;
 
 public class TypeInfoValidator implements IBuildParticipant {
 
 	public void build(IBuildContext context) throws CoreException {
 		final Script script = JavaScriptValidations.parse(context);
 		if (script == null) {
 			return;
 		}
 		final TypeInferencer2 inferencer = createTypeInferencer();
 		inferencer.setModelElement(context.getSourceModule());
 		final JSProblemReporter reporter = JavaScriptValidations
 				.createReporter(context);
 		final ValidationVisitor visitor = new ValidationVisitor(inferencer,
 				reporter);
 		inferencer.setVisitor(visitor);
 		final TypeChecker typeChecker = new TypeChecker(inferencer, reporter);
 		visitor.setTypeChecker(typeChecker);
 		inferencer.doInferencing(script);
 		typeChecker.validate();
 	}
 
 	protected TypeInferencer2 createTypeInferencer() {
 		return new TypeInferencer2();
 	}
 
 	private static enum VisitorMode {
 		NORMAL, CALL
 	}
 
 	private static abstract class ExpressionValidator {
 		abstract void call();
 
 		public ExpressionValidator() {
 		}
 
 		private ISuppressWarningsState suppressed;
 
 		public ISuppressWarningsState getSuppressed() {
 			return suppressed;
 		}
 
 		public void setSuppressed(ISuppressWarningsState suppressed) {
 			this.suppressed = suppressed;
 		}
 	}
 
 	private static class CallExpressionValidator extends ExpressionValidator {
 		private final FunctionScope scope;
 		private final CallExpression node;
 		private final IValueReference reference;
 		private final ValidationVisitor visitor;
 		private final IValueReference[] arguments;
 		private final List<Method> methods;
 
 		public CallExpressionValidator(FunctionScope scope,
 				CallExpression node, IValueReference reference,
 				IValueReference[] arguments, List<Method> methods,
 				ValidationVisitor visitor) {
 			this.scope = scope;
 			this.node = node;
 			this.reference = reference;
 			this.arguments = arguments;
 			this.methods = methods;
 			this.visitor = visitor;
 		}
 
 		public void call() {
 			visitor.validateCallExpression(scope, node, reference, arguments,
 					methods);
 		}
 	}
 
 	private static class ReturnNode {
 
 		final ReturnStatement node;
 		final IValueReference returnValueReference;
 
 		public ReturnNode(ReturnStatement node,
 				IValueReference returnValueReference) {
 			this.node = node;
 			this.returnValueReference = returnValueReference;
 		}
 
 		@Override
 		public String toString() {
 			return String.valueOf(node).trim() + " -> " + returnValueReference;
 		}
 	}
 
 	private static class TestReturnStatement extends ExpressionValidator {
 
 		private final List<ReturnNode> lst;
 		private final ValidationVisitor visitor;
 		private final IRMethod jsMethod;
 
 		public TestReturnStatement(IRMethod jsMethod, List<ReturnNode> lst,
 				ValidationVisitor visitor) {
 			this.jsMethod = jsMethod;
 			this.lst = lst;
 			this.visitor = visitor;
 		}
 
 		public void call() {
 			IRType firstType = null;
 			for (ReturnNode element : lst) {
 				if (element.returnValueReference == null)
 					continue;
 				IRType methodType = jsMethod.getType();
 				if (methodType != null && methodType instanceof IRRecordType) {
 					String failedPropertyTypeString = visitor
 							.testObjectPropertyType(
 									element.returnValueReference,
 									(IRRecordType) methodType);
 					if (failedPropertyTypeString != null) {
 						visitor.getProblemReporter()
 								.reportProblem(
 										JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 										NLS.bind(
 												ValidationMessages.DeclarationMismatchWithActualReturnType,
 												new String[] {
 														jsMethod.getName(),
 														TypeUtil.getName(methodType),
 														failedPropertyTypeString }),
 										element.node.sourceStart(),
 										element.node.sourceEnd());
 					}
 					return;
 				}
 				IRType type = JavaScriptValidations
 						.typeOf(element.returnValueReference);
 
 				if (type != null && methodType != null) {
 					final TypeCompatibility compatibility = methodType
 							.isAssignableFrom(type);
 					if (compatibility != TypeCompatibility.TRUE) {
 						final ReturnStatement node = element.node;
 						visitor.getProblemReporter()
 								.reportProblem(
 										compatibility == TypeCompatibility.FALSE ? JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE
 												: JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE_PARAMETERIZATION,
 										NLS.bind(
 												ValidationMessages.DeclarationMismatchWithActualReturnType,
 												new String[] {
 														jsMethod.getName(),
 														TypeUtil.getName(methodType),
 														TypeUtil.getName(type) }),
 										node.sourceStart(), node.sourceEnd());
 					}
 				}
 
 				if (firstType == null && type != null) {
 					firstType = type;
 				}
 			}
 
 			if (firstType != null) {
 				for (int i = 1; i < lst.size(); i++) {
 					ReturnNode next = lst.get(i);
 					IRType nextType = JavaScriptValidations
 							.typeOf(next.returnValueReference);
 					if (nextType != null
 							&& (!nextType.isAssignableFrom(firstType).ok() && !firstType
 									.isAssignableFrom(nextType).ok())) {
 						visitor.getProblemReporter()
 								.reportProblem(
 										JavaScriptProblems.RETURN_INCONSISTENT,
 										NLS.bind(
 												ValidationMessages.ReturnTypeInconsistentWithPreviousReturn,
 												new String[] {
 														TypeUtil.getName(nextType),
 														TypeUtil.getName(firstType) }),
 										next.node.sourceStart(),
 										next.node.sourceEnd());
 
 					}
 				}
 			}
 		}
 
 	}
 
 	private static class NotExistingIdentiferValidator extends
 			ExpressionValidator {
 		private final FunctionScope scope;
 		private final Expression identifer;
 		private final IValueReference reference;
 		private final ValidationVisitor visitor;
 
 		public NotExistingIdentiferValidator(FunctionScope scope,
 				Expression identifer, IValueReference reference,
 				ValidationVisitor visitor) {
 			this.scope = scope;
 			this.identifer = identifer;
 			this.reference = reference;
 			this.visitor = visitor;
 		}
 
 		public void call() {
 			visitor.validate(scope, identifer, reference);
 		}
 	}
 
 	private static class NewExpressionValidator extends ExpressionValidator {
 		private final FunctionScope scope;
 		private final NewExpression node;
 		private final IValueReference reference;
 		final IValueCollection collection;
 		private final ValidationVisitor validator;
 
 		public NewExpressionValidator(FunctionScope scope, NewExpression node,
 				IValueReference reference, IValueCollection collection,
 				ValidationVisitor validator) {
 			this.scope = scope;
 			this.node = node;
 			this.reference = reference;
 			this.collection = collection;
 			this.validator = validator;
 		}
 
 		public void call() {
 			validator.checkExpressionType(scope, collection,
 					node.getObjectClass(), reference);
 		}
 
 	}
 
 	private static class PropertyExpressionHolder extends ExpressionValidator {
 		private final FunctionScope scope;
 		private final PropertyExpression node;
 		private final IValueReference reference;
 		private final ValidationVisitor visitor;
 		private final boolean exists;
 
 		public PropertyExpressionHolder(FunctionScope scope,
 				PropertyExpression node, IValueReference reference,
 				ValidationVisitor visitor, boolean exists) {
 			this.scope = scope;
 			this.node = node;
 			this.reference = reference;
 			this.visitor = visitor;
 			this.exists = exists;
 		}
 
 		public void call() {
 			visitor.validateProperty(scope, node, reference, exists);
 		}
 	}
 
 	static class FunctionScope {
 		// Set<Expression or IValueReference>
 		final Set<Object> reported = new HashSet<Object>();
 		final List<ReturnNode> returnNodes = new ArrayList<ReturnNode>();
 		boolean throwsException;
 
 		void add(Path path) {
 			if (path != null) {
 				reported.add(path.start);
 				reported.add(path.references[0]);
 			}
 		}
 
 		boolean contains(Path path) {
 			if (path != null) {
 				if (reported.contains(path.start)) {
 					return true;
 				}
 				for (IValueReference reference : path.references) {
 					if (reported.contains(reference)) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 	}
 
 	static class Path {
 		final Expression start;
 		final IValueReference[] references;
 
 		public Path(Expression start, IValueReference[] references) {
 			this.start = start;
 			this.references = references;
 		}
 	}
 
 	public static class ValidationVisitor extends TypeInferencerVisitor {
 
 		private final List<ExpressionValidator> expressionValidators = new ArrayList<ExpressionValidator>();
 
 		public ValidationVisitor(ITypeInferenceContext context,
 				JSProblemReporter reporter) {
 			super(context);
 			this.reporter = reporter;
 		}
 
 		private final Map<ASTNode, VisitorMode> modes = new IdentityHashMap<ASTNode, VisitorMode>();
 
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
 		public void initialize() {
 			super.initialize();
 			modes.clear();
 			visitStack.clear();
 			expressionValidators.clear();
 			variables.clear();
 			functionScopes.clear();
 			functionScopes.add(new FunctionScope());
 		}
 
 		@Override
 		public void done() {
 			super.done();
 			for (ExpressionValidator call : expressionValidators
 					.toArray(new ExpressionValidator[expressionValidators
 							.size()])) {
 				final ISuppressWarningsState suppressWarnings = reporter
 						.getSuppressWarnings();
 				try {
 					reporter.restoreSuppressWarnings(call.getSuppressed());
 					call.call();
 				} finally {
 					reporter.restoreSuppressWarnings(suppressWarnings);
 				}
 			}
 			for (IValueReference variable : variables) {
 				if (variable.getAttribute(IReferenceAttributes.ACCESS) == null) {
 					final IRVariable jsVariable = (IRVariable) variable
 							.getAttribute(IReferenceAttributes.R_VARIABLE);
 					if (jsVariable != null
 							&& jsVariable
 									.isSuppressed(JavaScriptProblems.UNUSED_VARIABLE))
 						continue;
 					final ReferenceLocation location = variable.getLocation();
 					reporter.reportProblem(
 							JavaScriptProblems.UNUSED_VARIABLE,
 							NLS.bind("Variable {0} is never used",
 									variable.getName()),
 							location.getNameStart(), location.getNameEnd());
 				}
 			}
 		}
 
 		private VisitorMode currentMode() {
 			final VisitorMode mode = modes.get(visitStack.peek());
 			return mode != null ? mode : VisitorMode.NORMAL;
 		}
 
 		@Override
 		public IValueReference visitNewExpression(NewExpression node) {
 			IValueReference reference = super.visitNewExpression(node);
 			pushExpressionValidator(new NewExpressionValidator(
 					peekFunctionScope(), node, reference, peekContext(), this));
 			return reference;
 		}
 
 		private final Stack<FunctionScope> functionScopes = new Stack<FunctionScope>();
 
 		private static Path path(Expression expression,
 				IValueReference reference) {
 			final List<IValueReference> refs = new ArrayList<IValueReference>(8);
 			for (;;) {
 				if (expression instanceof PropertyExpression) {
 					expression = ((PropertyExpression) expression).getObject();
 				} else if (expression instanceof CallExpression) {
 					expression = ((CallExpression) expression).getExpression();
 				} else {
 					break;
 				}
 				refs.add(reference);
 				reference = reference.getParent();
 				if (reference == null) {
 					return null;
 				}
 			}
 			refs.add(reference);
 			return new Path(expression, refs.toArray(new IValueReference[refs
 					.size()]));
 		}
 
 		protected final FunctionScope peekFunctionScope() {
 			return functionScopes.peek();
 		}
 
 		public void enterFunctionScope() {
 			functionScopes.push(new FunctionScope());
 		}
 
 		public void leaveFunctionScope(IRMethod method) {
 			final FunctionScope scope = functionScopes.pop();
 			if (method != null) {
 				if (!scope.returnNodes.isEmpty()) {
 					// method.setType(context.resolveTypeRef(method.getType()));
 					pushExpressionValidator(new TestReturnStatement(method,
 							scope.returnNodes, this));
 				} else if (!scope.throwsException && method.getType() != null) {
 					final ReferenceLocation location = method.getLocation();
 					reporter.reportProblem(
 							JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 							NLS.bind(
 									ValidationMessages.DeclarationMismatchNoReturnType,
 									new String[] { method.getName(),
 											TypeUtil.getName(method.getType()) }),
 							location.getNameStart(), location.getNameEnd());
 				}
 			}
 		}
 
 		@Override
 		public IValueReference visitFunctionStatement(FunctionStatement node) {
 			validateHidesByFunction(node);
 
 			enterFunctionScope();
 			IValueReference reference = super.visitFunctionStatement(node);
 			final IRMethod method = (IRMethod) reference.getAttribute(R_METHOD);
 			leaveFunctionScope(method);
 
 			return reference;
 		}
 
 		private void validateHidesByFunction(FunctionStatement node) {
 			List<Argument> args = node.getArguments();
 			IValueCollection peekContext = peekContext();
 			for (Argument argument : args) {
 				IValueReference child = peekContext.getChild(argument
 						.getArgumentName());
 				if (child.exists()) {
 					if (child.getKind() == ReferenceKind.PROPERTY) {
 						Property property = (Property) child
 								.getAttribute(IReferenceAttributes.ELEMENT);
 						if (!property.isHideAllowed()) {
 							if (property.getDeclaringType() != null) {
 								reporter.reportProblem(
 										JavaScriptProblems.PARAMETER_HIDES_VARIABLE,
 										NLS.bind(
 												ValidationMessages.ParameterHidesPropertyOfType,
 												new String[] {
 														argument.getArgumentName(),
 														property.getDeclaringType()
 																.getName() }),
 										argument.sourceStart(), argument
 												.sourceEnd());
 							} else {
 								reporter.reportProblem(
 										JavaScriptProblems.PARAMETER_HIDES_VARIABLE,
 										NLS.bind(
 												ValidationMessages.ParameterHidesProperty,
 												argument.getArgumentName()),
 										argument.sourceStart(), argument
 												.sourceEnd());
 							}
 						}
 					} else if (!Boolean.TRUE.equals(child
 							.getAttribute(IReferenceAttributes.HIDE_ALLOWED))) {
 						if (child.getKind() == ReferenceKind.FUNCTION) {
 							reporter.reportProblem(
 									JavaScriptProblems.PARAMETER_HIDES_FUNCTION,
 									NLS.bind(
 											ValidationMessages.ParameterHidesFunction,
 											argument.getArgumentName()),
 									argument.sourceStart(), argument
 											.sourceEnd());
 						} else {
 							reporter.reportProblem(
 									JavaScriptProblems.PARAMETER_HIDES_VARIABLE,
 									NLS.bind(
 											ValidationMessages.ParameterHidesVariable,
 											argument.getArgumentName()),
 									argument.sourceStart(), argument
 											.sourceEnd());
 						}
 					}
 				}
 			}
 			if (node.isDeclaration()) {
 				final IValueReference child;
 				final IValueCollection parentScope = getParentScope(peekContext);
 				if (parentScope == null) {
 					child = peekContext.getChild(node.getName().getName());
 					if (getSource().equals(child.getLocation().getSource())) {
 						return;
 					}
 				} else {
 					child = parentScope.getChild(node.getName().getName());
 				}
 				if (child.exists()) {
 					if (child.getKind() == ReferenceKind.PROPERTY) {
 						Property property = (Property) child
 								.getAttribute(IReferenceAttributes.ELEMENT);
 						if (!property.isHideAllowed()) {
 							if (property.getDeclaringType() != null) {
 								reporter.reportProblem(
 										JavaScriptProblems.FUNCTION_HIDES_VARIABLE,
 										NLS.bind(
 												ValidationMessages.FunctionHidesPropertyOfType,
 												new String[] {
 														node.getName()
 																.getName(),
 														property.getDeclaringType()
 																.getName() }),
 										node.getName().sourceStart(), node
 												.getName().sourceEnd());
 							} else {
 								reporter.reportProblem(
 										JavaScriptProblems.FUNCTION_HIDES_VARIABLE,
 										NLS.bind(
 												ValidationMessages.FunctionHidesProperty,
 												node.getName().getName()), node
 												.getName().sourceStart(), node
 												.getName().sourceEnd());
 							}
 						}
 					} else if (!Boolean.TRUE.equals(child
 							.getAttribute(IReferenceAttributes.HIDE_ALLOWED))) {
 						if (child.getKind() == ReferenceKind.FUNCTION) {
 							reporter.reportProblem(
 									JavaScriptProblems.FUNCTION_HIDES_FUNCTION,
 									NLS.bind(
 											ValidationMessages.FunctionHidesFunction,
 											node.getName().getName()), node
 											.getName().sourceStart(), node
 											.getName().sourceEnd());
 						} else {
 							reporter.reportProblem(
 									JavaScriptProblems.FUNCTION_HIDES_VARIABLE,
 									NLS.bind(
 											ValidationMessages.FunctionHidesVariable,
 											node.getName().getName()), node
 											.getName().sourceStart(), node
 											.getName().sourceEnd());
 						}
 					}
 				}
 			}
 		}
 
 		@Override
 		public IValueReference visitReturnStatement(ReturnStatement node) {
 			IValueReference returnValueReference = super
 					.visitReturnStatement(node);
 			if (node.getValue() != null) {
 				peekFunctionScope().returnNodes.add(new ReturnNode(node,
 						returnValueReference));
 			}
 			return returnValueReference;
 		}
 
 		@Override
 		public IValueReference visitThrowStatement(ThrowStatement node) {
 			peekFunctionScope().throwsException = true;
 			return super.visitThrowStatement(node);
 		}
 
 		private final IRType functionTypeRef = JSTypeSet
 				.ref(TypeInfoModelLoader.getInstance().getType(
 						ITypeNames.FUNCTION));
 
 		@Override
 		public IValueReference visitCallExpression(CallExpression node) {
 			final Expression expression = node.getExpression();
 			modes.put(expression, VisitorMode.CALL);
 
 			final IValueReference reference = visit(expression);
 			modes.remove(expression);
 			if (reference == null) {
 				visitList(node.getArguments());
 				return null;
 			}
 			if (reference.getAttribute(PHANTOM, true) != null) {
 				visitList(node.getArguments());
 				return PhantomValueReference.REFERENCE;
 			}
 			if (isUntyped(reference)) {
 				visitList(node.getArguments());
 				return null;
 			}
 			if (reference.getKind() == ReferenceKind.ARGUMENT) {
 				if (reference.getDeclaredTypes().contains(functionTypeRef)) {
 					for (ASTNode argument : node.getArguments()) {
 						visit(argument);
 					}
 					// don't validate function pointer
 					return null;
 				}
 			}
 			final List<ASTNode> args = node.getArguments();
 			final IValueReference[] arguments = new IValueReference[args.size()];
 			for (int i = 0, size = args.size(); i < size; ++i) {
 				arguments[i] = visit(args.get(i));
 			}
 			final List<Method> methods = JavaScriptValidations.extractElements(
 					reference, Method.class);
 			if (methods != null && methods.size() == 1
 					&& methods.get(0) instanceof GenericMethod) {
 				final GenericMethod method = (GenericMethod) methods.get(0);
 				if (!validateParameterCount(method, args)) {
 					final Expression methodNode = expression instanceof PropertyExpression ? ((PropertyExpression) expression)
 							.getProperty() : expression;
 					reportMethodParameterError(methodNode, arguments, method);
 					return null;
 				}
 				final JSTypeSet result = evaluateGenericCall(method, arguments);
 				return result != null ? new ConstantValue(result) : null;
 			} else {
 				pushExpressionValidator(new CallExpressionValidator(
 						peekFunctionScope(), node, reference, arguments,
 						methods, this));
 				return reference.getChild(IValueReference.FUNCTION_OP);
 			}
 		}
 
 		private void pushExpressionValidator(
 				ExpressionValidator expressionValidator) {
 			expressionValidator.setSuppressed(reporter.getSuppressWarnings());
 			expressionValidators.add(expressionValidator);
 		}
 
 		/**
 		 * @param node
 		 * @param reference
 		 * @param methods
 		 * @return
 		 */
 		protected void validateCallExpression(FunctionScope scope,
 				CallExpression node, final IValueReference reference,
 				IValueReference[] arguments, List<Method> methods) {
 
 			final Expression expression = node.getExpression();
 			final Path path = path(expression, reference);
 			if (scope.contains(path)) {
 				return;
 			}
 			final Expression methodNode;
 			if (expression instanceof PropertyExpression) {
 				methodNode = ((PropertyExpression) expression).getProperty();
 			} else {
 				methodNode = expression;
 			}
 
 			if (methods == null || methods.size() == 0)
 				methods = JavaScriptValidations.extractElements(reference,
 						Method.class);
 			if (methods != null) {
 				Method method = JavaScriptValidations.selectMethod(
 						getContext(), methods, arguments);
 				if (method == null) {
 					final IRType type = JavaScriptValidations.typeOf(reference
 							.getParent());
 					if (type != null) {
 						if (TypeUtil.kind(type) == TypeKind.JAVA) {
 							reporter.reportProblem(
 									JavaScriptProblems.WRONG_JAVA_PARAMETERS,
 									NLS.bind(
 											ValidationMessages.MethodNotSelected,
 											new String[] { reference.getName(),
 													type.getName(),
 													describeArgTypes(arguments) }),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 
 						} else {
 							// TODO also a JS error (that should be
 							// configurable)
 						}
 					}
 					return;
 				}
 				if (method.isDeprecated()) {
 					reportDeprecatedMethod(methodNode, reference, method);
 				}
 				if (!validateParameterCount(method, node.getArguments())) {
 					reportMethodParameterError(methodNode, arguments, method);
 					return;
 				}
 				if (JavaScriptValidations.isStatic(reference.getParent())
 						&& !method.isStatic()) {
 					IRType type = JavaScriptValidations.typeOf(reference
 							.getParent());
 					reporter.reportProblem(
 							JavaScriptProblems.INSTANCE_METHOD,
 							NLS.bind(
 									ValidationMessages.StaticReferenceToNoneStaticMethod,
 									reference.getName(), TypeUtil.getName(type)),
 							methodNode.sourceStart(), methodNode.sourceEnd());
 				} else if (reference.getParent() != null
 						&& !JavaScriptValidations.isStatic(reference
 								.getParent()) && method.isStatic()) {
 					IRType type = JavaScriptValidations.typeOf(reference
 							.getParent());
 					reporter.reportProblem(JavaScriptProblems.STATIC_METHOD,
 							NLS.bind(
 									ValidationMessages.ReferenceToStaticMethod,
 									reference.getName(), type.getName()),
 							methodNode.sourceStart(), methodNode.sourceEnd());
 				}
 				final List<IRParameter> parameters = RModelBuilder.convert(
 						getContext(), method.getParameters());
 				final TypeCompatibility compatibility = validateParameters(
 						parameters, arguments);
 				if (compatibility != TypeCompatibility.TRUE) {
 					String name = method.getName();
 					if (name == null) {
 						Identifier identifier = PropertyExpressionUtils
 								.getIdentifier(methodNode);
 						if (identifier != null)
 							name = identifier.getName();
 					}
 					reporter.reportProblem(
 							compatibility == TypeCompatibility.FALSE ? JavaScriptProblems.WRONG_PARAMETERS
 									: JavaScriptProblems.WRONG_PARAMETERS_PARAMETERIZATION,
 							NLS.bind(
 									ValidationMessages.MethodNotApplicableInScript,
 									new String[] {
 											name,
 											describeParamTypes(parameters),
 											describeArgTypes(arguments,
 													parameters) }), methodNode
 									.sourceStart(), methodNode.sourceEnd());
 				}
 
 			} else {
 				Object attribute = reference.getAttribute(R_METHOD, true);
 				if (attribute instanceof IRMethod) {
 					IRMethod method = (IRMethod) attribute;
 					if (method.isDeprecated()) {
 						reporter.reportProblem(
 								JavaScriptProblems.DEPRECATED_FUNCTION,
 								NLS.bind(ValidationMessages.DeprecatedFunction,
 										reference.getName()), methodNode
 										.sourceStart(), methodNode.sourceEnd());
 					}
 					if (testVisibility(expression, reference, method)) {
 						reporter.reportProblem(
 								JavaScriptProblems.PRIVATE_FUNCTION, NLS.bind(
 										ValidationMessages.PrivateFunction,
 										reference.getName()), methodNode
 										.sourceStart(), methodNode.sourceEnd());
 					}
 					List<IRParameter> parameters = method.getParameters();
 					final TypeCompatibility compatibility = validateParameters(
 							parameters, arguments);
 					if (compatibility != TypeCompatibility.TRUE) {
 						String name = method.getName();
 						if (name == null) {
 							Identifier identifier = PropertyExpressionUtils
 									.getIdentifier(methodNode);
 							if (identifier != null)
 								name = identifier.getName();
 						}
 						final IProblemIdentifier problemId;
 						if (method.isTyped()) {
 							if (compatibility == TypeCompatibility.FALSE) {
 								problemId = JavaScriptProblems.WRONG_PARAMETERS;
 							} else {
 								problemId = JavaScriptProblems.WRONG_PARAMETERS_PARAMETERIZATION;
 							}
 						} else {
 							problemId = JavaScriptProblems.WRONG_PARAMETERS_UNTYPED;
 						}
 						reporter.reportProblem(
 								problemId,
 								NLS.bind(
 										ValidationMessages.MethodNotApplicableInScript,
 										new String[] {
 												name,
 												describeParamTypes(parameters),
 												describeArgTypes(arguments,
 														parameters) }),
 								methodNode.sourceStart(), methodNode
 										.sourceEnd());
 					}
 				} else if (!isArrayLookup(expression)
 						&& !isUntypedParameter(reference)) {
 					scope.add(path);
 
 					final IRType type = JavaScriptValidations.typeOf(reference
 							.getParent());
 					if (type != null) {
 						if (TypeUtil.kind(type) == TypeKind.JAVA) {
 							reporter.reportProblem(
 									JavaScriptProblems.UNDEFINED_JAVA_METHOD,
 									NLS.bind(
 											ValidationMessages.UndefinedMethod,
 											reference.getName(), type.getName()),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 						} else if (JavaScriptValidations.isStatic(reference
 								.getParent())
 								&& hasInstanceMethod(type, reference.getName())) {
 							reporter.reportProblem(
 									JavaScriptProblems.INSTANCE_METHOD,
 									NLS.bind(
 											ValidationMessages.StaticReferenceToNoneStaticMethod,
 											reference.getName(), type.getName()),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 						} else if (!reference.exists()) {
 							reporter.reportProblem(
 									JavaScriptProblems.UNDEFINED_METHOD,
 									NLS.bind(
 											ValidationMessages.UndefinedMethodInScript,
 											reference.getName()), methodNode
 											.sourceStart(), methodNode
 											.sourceEnd());
 						}
 					} else {
 						IRType referenceType = JavaScriptValidations
 								.typeOf(reference);
 						if (referenceType instanceof SimpleType) {
 							Type t = ((SimpleType) referenceType).getTarget();
 							while (t != null) {
 								if (t.getName().equals(ITypeNames.FUNCTION)) {
 									return;
 								}
 								t = t.getSuperType();
 							}
 						}
 						if (expression instanceof NewExpression) {
 							if (reference.getKind() == ReferenceKind.TYPE) {
 								return;
 							}
 							IRType newType = JavaScriptValidations
 									.typeOf(reference);
 							if (newType != null) {
 								return;
 							}
 
 						}
 						IValueReference parent = reference;
 						while (parent != null) {
 							if (parent.getName() == IValueReference.ARRAY_OP) {
 								// ignore array lookup function calls
 								// like: array[1](),
 								// those are dynamic.
 								return;
 							}
 							parent = parent.getParent();
 						}
 						if (expression instanceof NewExpression) {
 
 							reporter.reportProblem(
 									JavaScriptProblems.WRONG_TYPE_EXPRESSION,
 									NLS.bind(
 											ValidationMessages.UndefinedJavascriptType,
 											((NewExpression) expression)
 													.getObjectClass()
 													.toSourceString("")),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 
 						} else {
 							reporter.reportProblem(
 									reference.getParent() == null
 											&& isIdentifier(expression)
 											&& !reference.exists() ? JavaScriptProblems.UNDECLARED_VARIABLE
 											: JavaScriptProblems.UNDEFINED_METHOD,
 									NLS.bind(
 											ValidationMessages.UndefinedMethodInScript,
 											reference.getName()), methodNode
 											.sourceStart(), methodNode
 											.sourceEnd());
 						}
 					}
 				}
 			}
 			return;
 		}
 
 		/**
 		 * Checks if the passed reference is an untyped parameter. This method
 		 * helps to identify the common case of callbacks.
 		 * 
 		 * @param reference
 		 * @return
 		 */
 		private boolean isUntypedParameter(IValueReference reference) {
 			return reference.getKind() == ReferenceKind.ARGUMENT
 					&& reference.getDeclaredType() == null;
 		}
 
 		public static boolean isUntyped(IValueReference reference) {
 			while (reference != null) {
 				final ReferenceKind kind = reference.getKind();
 				if (kind == ReferenceKind.ARGUMENT) {
 					final IRType type = reference.getDeclaredType();
 					if (type == null || type instanceof IRAnyType) {
 						return true;
 					}
 				} else if (kind == ReferenceKind.THIS
 						&& reference.getDeclaredType() == null
 						&& reference.getDirectChildren().isEmpty()) {
 					return true;
 				} else if (kind == ReferenceKind.PROPERTY
 						&& reference.getDeclaredType() == null) {
 					return true;
 				}
 				reference = reference.getParent();
 			}
 			return false;
 		}
 
 		private boolean isThisCall(Expression expression) {
 			return expression instanceof PropertyExpression
 					&& ((PropertyExpression) expression).getObject() instanceof ThisExpression
 					&& ((PropertyExpression) expression).getProperty() instanceof Identifier;
 		}
 
 		private boolean hasInstanceMethod(IRType type, String name) {
 			return ElementValue.findMember(getContext(), type, name,
 					MemberPredicate.NON_STATIC) != null;
 		}
 
 		private boolean isArrayLookup(ASTNode expression) {
 			ASTNode walker = expression;
 			while (walker != null) {
 				if (walker instanceof GetArrayItemExpression)
 					return true;
 				if (walker instanceof PropertyExpression) {
 					if (((PropertyExpression) walker).getObject() instanceof GetArrayItemExpression)
 						return true;
 				}
 				if (walker instanceof JSNode) {
 					walker = ((JSNode) walker).getParent();
 				} else
 					return false;
 			}
 			return false;
 		}
 
 		private void reportDeprecatedMethod(ASTNode methodNode,
 				IValueReference reference, Method method) {
 			if (method.getDeclaringType() != null) {
 				reporter.reportProblem(
 						JavaScriptProblems.DEPRECATED_METHOD,
 						NLS.bind(ValidationMessages.DeprecatedMethod, reference
 								.getName(), method.getDeclaringType().getName()),
 						methodNode.sourceStart(), methodNode.sourceEnd());
 			} else {
 				reporter.reportProblem(JavaScriptProblems.DEPRECATED_METHOD,
 						NLS.bind(ValidationMessages.DeprecatedTopLevelMethod,
 								reference.getName()), methodNode.sourceStart(),
 						methodNode.sourceEnd());
 			}
 		}
 
 		private void reportMethodParameterError(ASTNode methodNode,
 				IValueReference[] arguments, Method method) {
 			if (method.getDeclaringType() != null) {
 				IProblemIdentifier problemId = JavaScriptProblems.WRONG_PARAMETERS;
 				if (method.getDeclaringType().getKind() == TypeKind.JAVA) {
 					problemId = JavaScriptProblems.WRONG_JAVA_PARAMETERS;
 				}
 				reporter.reportProblem(problemId, NLS.bind(
 						ValidationMessages.MethodNotApplicable,
 						new String[] { method.getName(),
 								describeParamTypes(method.getParameters()),
 								method.getDeclaringType().getName(),
 								describeArgTypes(arguments) }), methodNode
 						.sourceStart(), methodNode.sourceEnd());
 			} else {
 				reporter.reportProblem(JavaScriptProblems.WRONG_PARAMETERS, NLS
 						.bind(ValidationMessages.TopLevelMethodNotApplicable,
 								new String[] {
 										method.getName(),
 										describeParamTypes(method
 												.getParameters()),
 										describeArgTypes(arguments) }),
 						methodNode.sourceStart(), methodNode.sourceEnd());
 			}
 		}
 
 		private TypeCompatibility validateParameters(
 				List<IRParameter> parameters, IValueReference[] arguments) {
 			if (arguments.length > parameters.size()
 					&& !(parameters.size() > 0 && parameters.get(
 							parameters.size() - 1).getKind() == ParameterKind.VARARGS))
 				return TypeCompatibility.FALSE;
 			int testTypesSize = parameters.size();
 			if (parameters.size() > arguments.length) {
 				for (int i = arguments.length; i < parameters.size(); i++) {
 					final IRParameter p = parameters.get(i);
 					if (!p.isOptional() && !p.isVarargs())
 						return TypeCompatibility.FALSE;
 				}
 				testTypesSize = arguments.length;
 			} else if (parameters.size() < arguments.length) {
 				// is var args..
 				testTypesSize = parameters.size() - 1;
 			}
 
 			TypeCompatibility result = TypeCompatibility.TRUE;
 			for (int i = 0; i < testTypesSize; i++) {
 				IValueReference argument = arguments[i];
 				IRParameter parameter = parameters.get(i);
 				if (parameter.getType() instanceof IRRecordType
 						&& argument != null
 						&& !(argument.getDeclaredType() instanceof IRRecordType)) {
 					Set<String> argumentsChildren = argument
 							.getDirectChildren();
 					for (IRRecordMember member : ((IRRecordType) parameter
 							.getType()).getMembers()) {
 						if (argumentsChildren.contains(member.getName())) {
 							if (member.getType() != null) {
 								IValueReference child = argument
 										.getChild(member.getName());
 								final TypeCompatibility pResult = testArgumentType(
 										member.getType(), child);
 								if (pResult.after(result)) {
 									if (pResult == TypeCompatibility.FALSE) {
 										return pResult;
 									}
 									result = pResult;
 								}
 							}
 						} else if (!member.isOptional()) {
 							return TypeCompatibility.FALSE;
 						}
 					}
 
 				} else {
 					final TypeCompatibility pResult = testArgumentType(
 							parameter.getType(), argument);
 					if (pResult.after(result)) {
 						if (pResult == TypeCompatibility.FALSE) {
 							return pResult;
 						}
 						result = pResult;
 					}
 				}
 			}
 			// test var args
 			if (parameters.size() < arguments.length) {
 				int varargsParameter = parameters.size() - 1;
 				IRType paramType = parameters.get(varargsParameter).getType();
 
 				for (int i = varargsParameter; i < arguments.length; i++) {
 					IValueReference argument = arguments[i];
 					final TypeCompatibility pResult = testArgumentType(
 							paramType, argument);
 					if (pResult.after(result)) {
 						if (pResult == TypeCompatibility.FALSE) {
 							return pResult;
 						}
 						result = pResult;
 					}
 				}
 
 			}
 			return result;
 		}
 
 		/**
 		 * @param paramType
 		 * @param argument
 		 * @return
 		 */
 		private TypeCompatibility testArgumentType(IRType paramType,
 				IValueReference argument) {
 			if (argument != null && paramType != null) {
 				if (paramType instanceof IRRecordType) {
 					return TypeCompatibility.valueOf(testObjectPropertyType(
 							argument, (IRRecordType) paramType) == null);
 				}
 
 				IRType argumentType = argument.getDeclaredType();
 				if (argumentType == null && !argument.getTypes().isEmpty()) {
 					argumentType = argument.getTypes().getFirst();
 				}
 				if (argumentType != null) {
 					return paramType.isAssignableFrom(argumentType);
 				}
 			}
 			return TypeCompatibility.TRUE;
 		}
 
 		/**
 		 * @param element
 		 * @param type
 		 */
 		protected String testObjectPropertyType(IValueReference reference,
 				IRRecordType type) {
 			for (IRRecordMember member : type.getMembers()) {
 				IValueReference child = reference.getChild(member.getName());
 				IRType referenceType = JavaScriptValidations.typeOf(child);
 				if (!child.exists()
						|| !(referenceType != null && member.getType() != null && member
 								.getType().isAssignableFrom(referenceType).ok())) {
 					Set<String> children = reference.getDirectChildren();
 					if (children.size() == 0)
 						return "{}";
 					StringBuilder typeString = new StringBuilder();
 					typeString.append('{');
 					for (String childName : children) {
 						typeString.append(childName);
 						typeString.append(':');
 						IRType childType = JavaScriptValidations
 								.typeOf(reference.getChild(childName));
 						String typeName = TypeUtil.getName(childType);
 						typeString.append(typeName == null ? "Object"
 								: typeName);
 						typeString.append(',');
 					}
 					typeString.setLength(typeString.length() - 1);
 					typeString.append('}');
 
 					return typeString.toString();
 				}
 			}
 			return null;
 		}
 
 		/**
 		 * @param parameters
 		 * @return
 		 */
 		private String describeParamTypes(EList<Parameter> parameters) {
 			StringBuilder sb = new StringBuilder();
 			for (Parameter parameter : parameters) {
 				if (sb.length() != 0) {
 					sb.append(',');
 				}
 				if (parameter.getKind() == ParameterKind.OPTIONAL)
 					sb.append('[');
 				if (parameter.getType() != null) {
 					sb.append(parameter.getType().getName());
 				} else {
 					sb.append('?');
 				}
 				if (parameter.getKind() == ParameterKind.OPTIONAL)
 					sb.append(']');
 				if (parameter.getKind() == ParameterKind.VARARGS) {
 					sb.append("...");
 				}
 			}
 			return sb.toString();
 		}
 
 		private String describeParamTypes(List<IRParameter> parameters) {
 			StringBuilder sb = new StringBuilder();
 			for (IRParameter parameter : parameters) {
 				if (sb.length() != 0) {
 					sb.append(',');
 				}
 				if (parameter.getType() instanceof RecordType) {
 					sb.append('{');
 					for (Member member : ((RecordType) parameter.getType())
 							.getMembers()) {
 						if (sb.length() > 1)
 							sb.append(", ");
 						final boolean optional = member instanceof RecordMember
 								&& ((RecordMember) member).isOptional();
 						if (optional)
 							sb.append('[');
 						sb.append(member.getName());
 						if (member.getType() != null) {
 							sb.append(':');
 							sb.append(member.getType().getName());
 						}
 						if (optional)
 							sb.append(']');
 					}
 					sb.append('}');
 				} else if (parameter.getType() != null) {
 					if (parameter.getKind() == ParameterKind.OPTIONAL)
 						sb.append("[");
 					if (parameter.getKind() == ParameterKind.VARARGS)
 						sb.append("...");
 					sb.append(parameter.getType().getName());
 					if (parameter.getKind() == ParameterKind.OPTIONAL)
 						sb.append("]");
 				} else {
 					sb.append('?');
 				}
 			}
 			return sb.toString();
 		}
 
 		/**
 		 * @param arguments
 		 * @return
 		 */
 		private String describeArgTypes(IValueReference[] arguments) {
 			return describeArgTypes(arguments,
 					Collections.<IRParameter> emptyList());
 		}
 
 		/**
 		 * @param arguments
 		 * @param parameters
 		 * @return
 		 */
 		private String describeArgTypes(IValueReference[] arguments,
 				List<IRParameter> parameters) {
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < arguments.length; i++) {
 				IValueReference argument = arguments[i];
 				IRParameter parameter = parameters.size() > i ? parameters
 						.get(i) : null;
 				if (sb.length() != 0) {
 					sb.append(',');
 				}
 				if (argument == null) {
 					sb.append("null");
 				} else if (parameter != null
 						&& parameter.getType() instanceof RecordType) {
 					Set<String> directChildren = argument.getDirectChildren();
 					sb.append('{');
 					for (String childName : directChildren) {
 						if (sb.length() > 1)
 							sb.append(", ");
 						sb.append(childName);
 						IRType type = JavaScriptValidations.typeOf(argument
 								.getChild(childName));
 						if (type != null) {
 							sb.append(':');
 							sb.append(type.getName());
 						}
 					}
 					sb.append('}');
 				} else if (argument.getDeclaredType() != null) {
 					sb.append(argument.getDeclaredType().getName());
 				} else {
 					final JSTypeSet types = argument.getTypes();
 					if (types.size() == 1) {
 						sb.append(types.getFirst().getName());
 					} else {
 						sb.append('?');
 					}
 				}
 			}
 			return sb.toString();
 		}
 
 		private <E extends Member> E extractElement(IValueReference reference,
 				Class<E> elementType, Boolean staticModifierValue) {
 			final List<E> elements = JavaScriptValidations.extractElements(
 					reference, elementType);
 			if (staticModifierValue != null && elements != null
 					&& elements.size() > 1) {
 				for (E e : elements) {
 					if (e.isStatic() == staticModifierValue.booleanValue())
 						return e;
 				}
 			}
 
 			return elements != null ? elements.get(0) : null;
 		}
 
 		/**
 		 * Validates the parameter count, returns <code>true</code> if correct.
 		 * 
 		 * @param method
 		 * @param callArgs
 		 * 
 		 * @return
 		 */
 		private boolean validateParameterCount(Method method, List<?> callArgs) {
 			final EList<Parameter> params = method.getParameters();
 			if (params.size() == callArgs.size()) {
 				return true;
 			}
 			if (params.size() < callArgs.size()
 					&& !params.isEmpty()
 					&& params.get(params.size() - 1).getKind() == ParameterKind.VARARGS) {
 				return true;
 			}
 			if (params.size() > callArgs.size()
 					&& (params.get(callArgs.size()).getKind() == ParameterKind.OPTIONAL || params
 							.get(callArgs.size()).getKind() == ParameterKind.VARARGS)) {
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		public IValueReference visitPropertyExpression(PropertyExpression node) {
 			final IValueReference result = super.visitPropertyExpression(node);
 			if (result == null || result.getAttribute(PHANTOM, true) != null
 					|| isUntyped(result)) {
 				return result;
 			}
 			if (currentMode() != VisitorMode.CALL) {
 				pushExpressionValidator(new PropertyExpressionHolder(
 						peekFunctionScope(), node, result, this,
 						result.exists()));
 			}
 			return result;
 		}
 
 		@Override
 		protected IValueReference visitAssign(IValueReference left,
 				IValueReference right, BinaryOperation node) {
 			if (left != null) {
 				checkAssign(left, node);
 				validate(peekFunctionScope(), node.getLeftExpression(), left);
 			}
 			return super.visitAssign(left, right, node);
 		}
 
 		protected boolean validate(FunctionScope scope, Expression expr,
 				IValueReference reference) {
 			final IValueReference parent = reference.getParent();
 			if (parent == null) {
 				// top level
 				if (expr instanceof Identifier && !reference.exists()) {
 					scope.add(path(expr, reference));
 					reporter.reportProblem(
 							JavaScriptProblems.UNDECLARED_VARIABLE, NLS.bind(
 									ValidationMessages.UndeclaredVariable,
 									reference.getName()), expr.sourceStart(),
 							expr.sourceEnd());
 					return false;
 				} else
 					testPrivate(expr, reference);
 
 			} else if (expr instanceof PropertyExpression
 					&& validate(scope, ((PropertyExpression) expr).getObject(),
 							parent)) {
 				final IRType type = JavaScriptValidations.typeOf(parent);
 				if (type != null && TypeUtil.kind(type) == TypeKind.JAVA
 						&& !reference.exists()) {
 					reporter.reportProblem(
 							JavaScriptProblems.UNDEFINED_JAVA_PROPERTY,
 							NLS.bind(ValidationMessages.UndefinedProperty,
 									reference.getName(), type.getName()), expr
 									.sourceStart(), expr.sourceEnd());
 					return false;
 				}
 			} else if (expr instanceof GetArrayItemExpression
 					&& !validate(scope,
 							((GetArrayItemExpression) expr).getArray(), parent)) {
 				return false;
 			}
 			return true;
 		}
 
 		/**
 		 * @param expr
 		 * @param reference
 		 */
 		public void testPrivate(Expression expr, IValueReference reference) {
 			if (reference.getAttribute(IReferenceAttributes.PRIVATE) == Boolean.TRUE) {
 				Object attribute = reference
 						.getAttribute(IReferenceAttributes.R_VARIABLE);
 				if (attribute instanceof IRVariable) {
 					final IRVariable variable = (IRVariable) attribute;
 					reporter.reportProblem(JavaScriptProblems.PRIVATE_VARIABLE,
 							NLS.bind(ValidationMessages.PrivateVariable,
 									variable.getName()), expr.sourceStart(),
 							expr.sourceEnd());
 				} else {
 					attribute = reference.getAttribute(R_METHOD);
 					if (attribute instanceof IRMethod) {
 						IRMethod method = (IRMethod) attribute;
 						reporter.reportProblem(
 								JavaScriptProblems.PRIVATE_FUNCTION, NLS.bind(
 										ValidationMessages.PrivateFunction,
 										method.getName()), expr.sourceStart(),
 								expr.sourceEnd());
 					}
 				}
 			}
 		}
 
 		private static boolean isVarOrFunction(IValueReference reference) {
 			final ReferenceKind kind = reference.getKind();
 			return kind.isVariable() || kind == ReferenceKind.FUNCTION;
 		}
 
 		@Override
 		public IValueReference visitIdentifier(Identifier node) {
 			final IValueReference result = super.visitIdentifier(node);
 			if (!(node.getParent() instanceof BinaryOperation && ((BinaryOperation) node
 					.getParent()).isAssignmentTo(node))
 					&& isVarOrFunction(result)
 					&& getSource().equals(result.getLocation().getSource())) {
 				if (result.getAttribute(IReferenceAttributes.ACCESS) == null) {
 					result.setAttribute(IReferenceAttributes.ACCESS,
 							Boolean.TRUE);
 				}
 			}
 			final Property property = extractElement(result, Property.class,
 					null);
 			if (property != null && property.isDeprecated()) {
 				reportDeprecatedProperty(property, null, node);
 			} else {
 				if (!result.exists()
 						&& !(node.getParent() instanceof CallExpression && ((CallExpression) node
 								.getParent()).getExpression() == node)) {
 					pushExpressionValidator(new NotExistingIdentiferValidator(
 							peekFunctionScope(), node, result, this));
 				} else {
 					testPrivate(node, result);
 					if (result.exists()
 							&& node.getParent() instanceof BinaryOperation
 							&& ((BinaryOperation) node.getParent())
 									.getOperation() == JSParser.INSTANCEOF
 							&& ((BinaryOperation) node.getParent())
 									.getRightExpression() == node) {
 						checkTypeReference(node,
 								JavaScriptValidations.typeOf(result),
 								peekContext());
 					}
 				}
 			}
 			return result;
 		}
 
 		private static IValueCollection getParentScope(
 				final IValueCollection collection) {
 			IValueCollection c = collection;
 			while (c != null && !c.isScope()) {
 				c = c.getParent();
 			}
 			if (c != null) {
 				c = c.getParent();
 				if (c != null) {
 					return c;
 				}
 			}
 			return null;
 		}
 
 		private final Set<IValueReference> variables = new HashSet<IValueReference>();
 
 		@Override
 		protected IValueReference createVariable(IValueCollection context,
 				VariableDeclaration declaration) {
 			validateHidesByVariable(context, declaration);
 			final IValueReference variable = super.createVariable(context,
 					declaration);
 			variables.add(variable);
 			return variable;
 		}
 
 		private void checkAssign(IValueReference reference, ASTNode node) {
 			final Object value = reference
 					.getAttribute(IAssignProtection.ATTRIBUTE);
 			if (value != null) {
 				final IAssignProtection assign = value instanceof IAssignProtection ? (IAssignProtection) value
 						: PROTECT_CONST;
 				reporter.reportProblem(assign.problemId(),
 						assign.problemMessage(), node.sourceStart(),
 						node.sourceEnd());
 			}
 		}
 
 		@Override
 		protected void initializeVariable(IValueReference reference,
 				VariableDeclaration declaration, IVariable variable) {
 			if (declaration.getInitializer() != null) {
 				checkAssign(reference, declaration);
 			}
 			super.initializeVariable(reference, declaration, variable);
 		}
 
 		private void validateHidesByVariable(IValueCollection context,
 				VariableDeclaration declaration) {
 			final IValueReference child;
 			final Identifier identifier = declaration.getIdentifier();
 			final IValueCollection parentScope = getParentScope(context);
 			if (parentScope == null) {
 				child = context.getChild(identifier.getName());
 				if (getSource().equals(child.getLocation().getSource())) {
 					return;
 				}
 			} else {
 				child = parentScope.getChild(identifier.getName());
 			}
 			if (child.exists()) {
 				if (child.getKind() == ReferenceKind.ARGUMENT) {
 					reporter.reportProblem(
 							JavaScriptProblems.VAR_HIDES_PARAMETER, NLS.bind(
 									ValidationMessages.VariableHidesParameter,
 									declaration.getVariableName()), identifier
 									.sourceStart(), identifier.sourceEnd());
 				} else if (child.getKind() == ReferenceKind.FUNCTION) {
 					reporter.reportProblem(
 							JavaScriptProblems.VAR_HIDES_FUNCTION, NLS.bind(
 									ValidationMessages.VariableHidesFunction,
 									declaration.getVariableName()), identifier
 									.sourceStart(), identifier.sourceEnd());
 				} else if (child.getKind() == ReferenceKind.PROPERTY) {
 					Property property = (Property) child
 							.getAttribute(IReferenceAttributes.ELEMENT);
 					if (property != null && property.getDeclaringType() != null) {
 						reporter.reportProblem(
 								JavaScriptProblems.VAR_HIDES_PROPERTY,
 								NLS.bind(
 										ValidationMessages.VariableHidesPropertyOfType,
 										declaration.getVariableName(), property
 												.getDeclaringType().getName()),
 								identifier.sourceStart(), identifier
 										.sourceEnd());
 					} else {
 						reporter.reportProblem(
 								JavaScriptProblems.VAR_HIDES_PROPERTY,
 								NLS.bind(
 										ValidationMessages.VariableHidesProperty,
 										declaration.getVariableName()),
 								identifier.sourceStart(), identifier
 										.sourceEnd());
 
 					}
 				} else if (child.getKind() == ReferenceKind.METHOD) {
 					Method method = (Method) child
 							.getAttribute(IReferenceAttributes.ELEMENT);
 					if (method != null && method.getDeclaringType() != null) {
 						reporter.reportProblem(
 								JavaScriptProblems.VAR_HIDES_METHOD,
 								NLS.bind(
 										ValidationMessages.VariableHidesMethodOfType,
 										declaration.getVariableName(), method
 												.getDeclaringType().getName()),
 								identifier.sourceStart(), identifier
 										.sourceEnd());
 					} else {
 						reporter.reportProblem(
 								JavaScriptProblems.VAR_HIDES_METHOD, NLS.bind(
 										ValidationMessages.VariableHidesMethod,
 										declaration.getVariableName()),
 								identifier.sourceStart(), identifier
 										.sourceEnd());
 					}
 				} else {
 					reporter.reportProblem(
 							JavaScriptProblems.DUPLICATE_VAR_DECLARATION,
 							NLS.bind(ValidationMessages.VariableHidesVariable,
 									declaration.getVariableName()), identifier
 									.sourceStart(), identifier.sourceEnd());
 				}
 
 			}
 		}
 
 		protected void validateProperty(final FunctionScope scope,
 				PropertyExpression propertyExpression, IValueReference result,
 				boolean exists) {
 			final Path path = path(propertyExpression, result);
 			if (scope.contains(path)) {
 				return;
 			}
 			final Expression propName = propertyExpression.getProperty();
 			final Member member = extractElement(result, Member.class,
 					JavaScriptValidations.isStatic(result.getParent()));
 			if (member != null) {
 				if (member.isDeprecated()) {
 					final Property parentProperty = extractElement(
 							result.getParent(), Property.class, null);
 					if (parentProperty != null
 							&& parentProperty.getDeclaringType() == null) {
 						if (member instanceof Property)
 							reportDeprecatedProperty((Property) member,
 									parentProperty, propName);
 						else if (member instanceof Method)
 							reportDeprecatedMethod(propName, result,
 									(Method) member);
 					} else {
 						if (member instanceof Property)
 							reportDeprecatedProperty((Property) member,
 									member.getDeclaringType(), propName);
 						else if (member instanceof Method)
 							reportDeprecatedMethod(propName, result,
 									(Method) member);
 					}
 				} else if (!member.isVisible()) {
 					final Property parentProperty = extractElement(
 							result.getParent(), Property.class, null);
 					if (parentProperty != null
 							&& parentProperty.getDeclaringType() == null) {
 						if (member instanceof Property)
 							reportHiddenProperty((Property) member,
 									parentProperty, propName);
 						// else if (member instanceof Method)
 						// reportDeprecatedMethod(propName, result,
 						// (Method) member);
 					} else if (member instanceof Property) {
 						reportHiddenProperty((Property) member,
 								member.getDeclaringType(), propName);
 					}
 				} else if (JavaScriptValidations.isStatic(result.getParent())
 						&& !member.isStatic()) {
 					IRType type = JavaScriptValidations.typeOf(result
 							.getParent());
 					reporter.reportProblem(
 							JavaScriptProblems.INSTANCE_PROPERTY,
 							NLS.bind(
 									ValidationMessages.StaticReferenceToNoneStaticProperty,
 									result.getName(), TypeUtil.getName(type)),
 							propName.sourceStart(), propName.sourceEnd());
 				} else if (!JavaScriptValidations.isStatic(result.getParent())
 						&& member.isStatic()) {
 					IRType type = JavaScriptValidations.typeOf(result
 							.getParent());
 					reporter.reportProblem(
 							JavaScriptProblems.STATIC_PROPERTY,
 							NLS.bind(
 									ValidationMessages.ReferenceToStaticProperty,
 									result.getName(), type.getName()), propName
 									.sourceStart(), propName.sourceEnd());
 				}
 			} else if ((!exists && !result.exists())
 					&& !isArrayLookup(propertyExpression)) {
 				scope.add(path);
 				final IRType type = typeOf(result.getParent());
 				final TypeKind kind = TypeUtil.kind(type);
 				if (type != null && kind == TypeKind.JAVA) {
 					reporter.reportProblem(
 							JavaScriptProblems.UNDEFINED_JAVA_PROPERTY, NLS
 									.bind(ValidationMessages.UndefinedProperty,
 											result.getName(), type.getName()),
 							propName.sourceStart(), propName.sourceEnd());
 				} else if (type != null
 						&& shouldBeDefined(propertyExpression)
 						&& (kind == TypeKind.JAVASCRIPT || kind == TypeKind.PREDEFINED)) {
 					reporter.reportProblem(
 							JavaScriptProblems.UNDEFINED_PROPERTY,
 							NLS.bind(
 									ValidationMessages.UndefinedPropertyInScriptType,
 									result.getName(), type.getName()), propName
 									.sourceStart(), propName.sourceEnd());
 				} else if (shouldBeDefined(propertyExpression)) {
 					final String parentPath = PropertyExpressionUtils
 							.getPath(propertyExpression.getObject());
 					reporter.reportProblem(
 							JavaScriptProblems.UNDEFINED_PROPERTY,
 							NLS.bind(
 									ValidationMessages.UndefinedPropertyInScript,
 									result.getName(),
 									parentPath != null ? parentPath
 											: "javascript"), propName
 									.sourceStart(), propName.sourceEnd());
 				}
 			} else {
 				IRVariable variable = (IRVariable) result
 						.getAttribute(IReferenceAttributes.R_VARIABLE);
 				if (variable != null) {
 					if (variable.isDeprecated()) {
 						reporter.reportProblem(
 								JavaScriptProblems.DEPRECATED_VARIABLE,
 								NLS.bind(ValidationMessages.DeprecatedVariable,
 										variable.getName()), propName
 										.sourceStart(), propName.sourceEnd());
 					}
 					if (testVisibility(propertyExpression, result, variable)) {
 						reporter.reportProblem(
 								JavaScriptProblems.PRIVATE_VARIABLE, NLS.bind(
 										ValidationMessages.PrivateVariable,
 										variable.getName()), propName
 										.sourceStart(), propName.sourceEnd());
 					}
 					return;
 				} else {
 					IRMethod method = (IRMethod) result.getAttribute(R_METHOD);
 					if (method != null) {
 						if (method.isDeprecated()) {
 							reporter.reportProblem(
 									JavaScriptProblems.DEPRECATED_FUNCTION,
 									NLS.bind(
 											ValidationMessages.DeprecatedFunction,
 											method.getName()), propName
 											.sourceStart(), propName
 											.sourceEnd());
 						}
 						if (testVisibility(propertyExpression, result, method)) {
 							reporter.reportProblem(
 									JavaScriptProblems.PRIVATE_FUNCTION,
 									NLS.bind(
 											ValidationMessages.PrivateFunction,
 											method.getName()), propName
 											.sourceStart(), propName
 											.sourceEnd());
 						}
 						return;
 
 					}
 				}
 			}
 		}
 
 		/**
 		 * @param expression
 		 * @param result
 		 * @param method
 		 * @return
 		 */
 		public boolean testVisibility(Expression expression,
 				IValueReference result, IRMember method) {
 			return (method.isPrivate() || (method.isProtected()
 					&& result.getParent() != null && result.getParent()
 					.getAttribute(IReferenceAttributes.SUPER_SCOPE) == null))
 					&& (result.getParent() != null || result
 							.getAttribute(IReferenceAttributes.PRIVATE) == Boolean.TRUE)
 					&& !isThisCall(expression);
 		}
 
 		private boolean shouldBeDefined(PropertyExpression propertyExpression) {
 			if (propertyExpression.getParent() instanceof BinaryOperation) {
 				final BinaryOperation bo = (BinaryOperation) propertyExpression
 						.getParent();
 				return bo.getOperation() != JSParser.LAND
 						&& bo.getOperation() != JSParser.LOR;
 			}
 			return true;
 		}
 
 		private void reportDeprecatedProperty(Property property, Element owner,
 				ASTNode node) {
 			final String msg;
 			if (owner instanceof Type) {
 				msg = NLS.bind(ValidationMessages.DeprecatedProperty,
 						property.getName(), owner.getName());
 			} else if (owner instanceof Property) {
 				msg = NLS.bind(ValidationMessages.DeprecatedPropertyOfInstance,
 						property.getName(), owner.getName());
 			} else {
 				msg = NLS.bind(ValidationMessages.DeprecatedPropertyNoType,
 						property.getName());
 			}
 			reporter.reportProblem(JavaScriptProblems.DEPRECATED_PROPERTY, msg,
 					node.sourceStart(), node.sourceEnd());
 		}
 
 		private void reportHiddenProperty(Property property, Element owner,
 				ASTNode node) {
 			final String msg;
 			if (owner instanceof Type) {
 				msg = NLS.bind(ValidationMessages.HiddenProperty,
 						property.getName(), owner.getName());
 			} else if (owner instanceof Property) {
 				msg = NLS.bind(ValidationMessages.HiddenPropertyOfInstance,
 						property.getName(), owner.getName());
 			} else {
 				msg = NLS.bind(ValidationMessages.HiddenPropertyNoType,
 						property.getName());
 			}
 			reporter.reportProblem(JavaScriptProblems.HIDDEN_PROPERTY, msg,
 					node.sourceStart(), node.sourceEnd());
 		}
 
 		private static boolean isIdentifier(Expression node) {
 			return node instanceof Identifier || node instanceof CallExpression
 					&& isIdentifier(((CallExpression) node).getExpression());
 		}
 
 		private static Identifier getIdentifier(Expression node) {
 			if (node instanceof Identifier) {
 				return (Identifier) node;
 			} else if (node instanceof CallExpression) {
 				return getIdentifier(((CallExpression) node).getExpression());
 			} else {
 				return null;
 			}
 		}
 
 		protected void checkExpressionType(FunctionScope scope,
 				IValueCollection collection, Expression node,
 				IValueReference reference) {
 			if (reference.getParent() == null && isIdentifier(node)
 					&& !reference.exists()) {
 				scope.add(path(node, reference));
 				final Identifier identifier = getIdentifier(node);
 				reportUnknownType(JavaScriptProblems.UNDECLARED_VARIABLE,
 						identifier != null ? identifier : node,
 						identifier != null ? identifier.getName() : "?");
 				return;
 			}
 			JSTypeSet types = reference.getTypes();
 			if (types.size() > 0) {
 				checkTypeReference(node, types.getFirst(), collection);
 			} else if (reference.getDeclaredType() != null) {
 				checkTypeReference(node, reference.getDeclaredType(),
 						collection);
 			} else {
 				final String lazyName = ValueReferenceUtil
 						.getLazyName(reference);
 				if (lazyName != null) {
 					reportUnknownType(JavaScriptProblems.WRONG_TYPE_EXPRESSION,
 							ValidationMessages.UndefinedJavascriptType, node,
 							lazyName);
 				}
 			}
 		}
 
 		/**
 		 * @param node
 		 * @param type
 		 */
 		protected void checkTypeReference(ASTNode node, IRType type,
 				IValueCollection collection) {
 			if (type == null) {
 				return;
 			}
 			if (type instanceof IRSimpleType) {
 				final Type t = ((IRSimpleType) type).getTarget();
 				if (t.getKind() != TypeKind.UNKNOWN) {
 					if (t.isDeprecated()) {
 						reporter.reportProblem(
 								JavaScriptProblems.DEPRECATED_TYPE, NLS.bind(
 										ValidationMessages.DeprecatedType,
 										TypeUtil.getName(type)), node
 										.sourceStart(), node.sourceEnd());
 					}
 				}
 			} else if (type instanceof IRClassType) {
 				final Type t = ((IRClassType) type).getTarget();
 				if (t != null && t.getKind() != TypeKind.UNKNOWN) {
 					if (t.isDeprecated()) {
 						reporter.reportProblem(
 								JavaScriptProblems.DEPRECATED_TYPE, NLS.bind(
 										ValidationMessages.DeprecatedType,
 										TypeUtil.getName(type)), node
 										.sourceStart(), node.sourceEnd());
 					}
 				}
 			}
 		}
 
 		public void reportUnknownType(IProblemIdentifier identifier,
 				String message, ASTNode node, String name) {
 			reporter.reportProblem(identifier, NLS.bind(message, name),
 					node.sourceStart(), node.sourceEnd());
 		}
 
 		public void reportUnknownType(IProblemIdentifier identifier,
 				ASTNode node, String name) {
 			reportUnknownType(identifier, ValidationMessages.UnknownType, node,
 					name);
 		}
 
 		private boolean stronglyTyped(IValueReference reference) {
 			final IRType parentType = typeOf(reference.getParent());
 			if (parentType != null) {
 				if (parentType instanceof IRRecordType) {
 					return true;
 				}
 				return TypeUtil.kind(parentType) == TypeKind.JAVA;
 			}
 			return false;
 		}
 
 		@Override
 		public IValueReference visitIfStatement(IfStatement node) {
 			final IValueReference condition = visit(node.getCondition());
 			if (condition != null && !condition.exists()
 					&& node.getCondition() instanceof PropertyExpression
 					&& !stronglyTyped(condition)) {
 				if (DEBUG) {
 					System.out.println("visitIfStatement("
 							+ node.getCondition() + ") doesn't exist "
 							+ condition + " - create it");
 				}
 				condition.setValue(PhantomValueReference.REFERENCE);
 			}
 			visitIfStatements(node);
 			return null;
 		}
 	}
 
 	static final boolean DEBUG = false;
 
 }
