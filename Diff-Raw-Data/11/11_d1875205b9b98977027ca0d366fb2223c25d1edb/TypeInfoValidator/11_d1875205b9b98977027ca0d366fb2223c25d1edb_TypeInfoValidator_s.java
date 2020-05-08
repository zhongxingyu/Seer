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
 import static org.eclipse.dltk.javascript.typeinfo.RUtils.locationOf;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.WeakHashMap;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.annotations.NonNull;
 import org.eclipse.dltk.annotations.Nullable;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.compiler.problem.IValidationStatus;
 import org.eclipse.dltk.compiler.problem.ValidationMultiStatus;
 import org.eclipse.dltk.compiler.problem.ValidationStatus;
 import org.eclipse.dltk.core.ISourceNode;
 import org.eclipse.dltk.core.builder.IBuildContext;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.core.builder.IBuildParticipantExtension;
 import org.eclipse.dltk.core.builder.IBuildParticipantExtension4;
 import org.eclipse.dltk.internal.javascript.parser.JSDocValidatorFactory.TypeChecker;
 import org.eclipse.dltk.internal.javascript.ti.ConstantValue;
 import org.eclipse.dltk.internal.javascript.ti.ElementValue;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.ITypeInferenceContext;
 import org.eclipse.dltk.internal.javascript.ti.JSMethod;
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
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.UnaryOperation;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.core.JSBindings;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.internal.core.TemporaryBindings;
 import org.eclipse.dltk.javascript.internal.core.ThreadTypeSystemImpl;
 import org.eclipse.dltk.javascript.parser.ISuppressWarningsState;
 import org.eclipse.dltk.javascript.parser.JSParser;
 import org.eclipse.dltk.javascript.parser.JSProblemReporter;
 import org.eclipse.dltk.javascript.parser.PropertyExpressionUtils;
 import org.eclipse.dltk.javascript.parser.Reporter;
 import org.eclipse.dltk.javascript.typeinference.IAssignProtection;
 import org.eclipse.dltk.javascript.typeinference.IAssignProtection2;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.PhantomValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinference.ValueReferenceUtil;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IVariable;
 import org.eclipse.dltk.javascript.typeinfo.IRArrayType;
 import org.eclipse.dltk.javascript.typeinfo.IRClassType;
 import org.eclipse.dltk.javascript.typeinfo.IRConstructor;
 import org.eclipse.dltk.javascript.typeinfo.IRElement;
 import org.eclipse.dltk.javascript.typeinfo.IRFunctionType;
 import org.eclipse.dltk.javascript.typeinfo.IRMapType;
 import org.eclipse.dltk.javascript.typeinfo.IRMember;
 import org.eclipse.dltk.javascript.typeinfo.IRMethod;
 import org.eclipse.dltk.javascript.typeinfo.IRParameter;
 import org.eclipse.dltk.javascript.typeinfo.IRProperty;
 import org.eclipse.dltk.javascript.typeinfo.IRRecordMember;
 import org.eclipse.dltk.javascript.typeinfo.IRRecordType;
 import org.eclipse.dltk.javascript.typeinfo.IRType;
 import org.eclipse.dltk.javascript.typeinfo.IRTypeDeclaration;
 import org.eclipse.dltk.javascript.typeinfo.IRTypeExtension;
 import org.eclipse.dltk.javascript.typeinfo.IRVariable;
 import org.eclipse.dltk.javascript.typeinfo.ITypeChecker;
 import org.eclipse.dltk.javascript.typeinfo.ITypeCheckerExtension;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.ITypeSystem;
 import org.eclipse.dltk.javascript.typeinfo.JSTypeSet;
 import org.eclipse.dltk.javascript.typeinfo.MemberPredicates;
 import org.eclipse.dltk.javascript.typeinfo.RTypes;
 import org.eclipse.dltk.javascript.typeinfo.TypeCompatibility;
 import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
 import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.ParameterKind;
 import org.eclipse.dltk.javascript.typeinfo.model.Property;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.dltk.javascript.typeinfo.model.Visibility;
 import org.eclipse.dltk.javascript.validation.IValidatorExtension;
 import org.eclipse.osgi.util.NLS;
 
 public class TypeInfoValidator implements IBuildParticipant,
 		IBuildParticipantExtension, IBuildParticipantExtension4 {
 
 	/**
 	 * Public identifier of this build participant.
 	 */
 	public static final String ID = "org.eclipse.dltk.javascript.core.buildParticipant.typeinfo";
 
 	private boolean hasDependents;
 
 	public boolean beginBuild(int buildType) {
 		return true;
 	}
 
 	public void notifyDependents(IBuildParticipant[] dependents) {
 		hasDependents = true;
 	}
 
 	private TypeInferencer2 inferencer;
 
 	public void build(IBuildContext context) throws CoreException {
 		final Script script = JavaScriptValidations.parse(context);
 		if (script == null) {
 			return;
 		}
 		if (inferencer == null) {
 			inferencer = createTypeInferencer();
 		}
 		inferencer.setModelElement(context.getSourceModule());
 		final JSProblemReporter reporter = JavaScriptValidations
 				.createReporter(context);
 		@SuppressWarnings("unchecked")
 		@Nullable
 		final Set<FunctionStatement> inconsistentReturns = (Set<FunctionStatement>) context
 				.get(JavaScriptValidations.ATTR_INCONSISTENT_RETURNS);
 		final ValidationVisitor visitor = new ValidationVisitor(inferencer,
 				reporter, inconsistentReturns, hasDependents);
 		inferencer.setVisitor(visitor);
 		inferencer.doInferencing(script);
 		if (hasDependents) {
 			inferencer.resetLocalState();
 			context.set(TypeInfoValidator.ATTR_BINDINGS, visitor.bindings);
 			saveCachedBindings(script, new TemporaryBindings(inferencer,
 					visitor.bindings));
 			((ThreadTypeSystemImpl) ITypeSystem.CURRENT).set(inferencer);
 		}
 	}
 
 	public void afterBuild(IBuildContext context) {
 		if (hasDependents) {
 			((ThreadTypeSystemImpl) ITypeSystem.CURRENT).set(null);
 		}
 	}
 
 	public void endBuild(IProgressMonitor monitor) {
 		removeCachedBindings();
 		inferencer = null;
 	}
 
 	/**
 	 * The name of the {@link IBuildContext} attribute containing "bindings"
 	 * <code>(Map&lt;ASTNode,IValueReference&gt;)</code>
 	 */
 	public static final String ATTR_BINDINGS = TypeInfoValidator.class
 			.getName() + ".BINDINGS";
 
 	/**
 	 * Thread specific bindings, so methods from {@link JSBindings} called from
 	 * {@link IBuildParticipant} will return validation specific bindings.
 	 */
 	private static final ThreadLocal<WeakHashMap<Script, JSBindings>> CACHED_BINDINGS = new ThreadLocal<WeakHashMap<Script, JSBindings>>();
 
 	private static void saveCachedBindings(Script script, JSBindings bindings) {
 		WeakHashMap<Script, JSBindings> map = CACHED_BINDINGS.get();
 		if (map == null) {
 			map = new WeakHashMap<Script, JSBindings>();
 			CACHED_BINDINGS.set(map);
 		}
 		map.put(script, bindings);
 	}
 
 	private static void removeCachedBindings() {
 		final WeakHashMap<Script, JSBindings> map = CACHED_BINDINGS.get();
 		if (map != null) {
 			map.clear();
 		}
 	}
 
 	/**
 	 * Returns cached "bindings" for the specified script, if any.
 	 * 
 	 * @param script
 	 * @return
 	 */
 	public static JSBindings getCachedBindings(Script script) {
 		final WeakHashMap<Script, JSBindings> map = CACHED_BINDINGS.get();
 		if (map != null) {
 			return map.get(script);
 		}
 		return null;
 	}
 
 	protected TypeInferencer2 createTypeInferencer() {
 		return new TypeInferencer2();
 	}
 
 	private static enum VisitorMode {
 		NORMAL, CALL
 	}
 
 	private static abstract class ExpressionValidator {
 		abstract void call(ValidationVisitor visitor);
 
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
 		private final IValueReference[] arguments;
 		private final List<IRMethod> methods;
 
 		public CallExpressionValidator(FunctionScope scope,
 				CallExpression node, IValueReference reference,
 				IValueReference[] arguments, List<IRMethod> methods) {
 			this.scope = scope;
 			this.node = node;
 			this.reference = reference;
 			this.arguments = arguments;
 			this.methods = methods;
 		}
 
 		@Override
 		public void call(ValidationVisitor visitor) {
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
 		private final IRMethod jsMethod;
 
 		public TestReturnStatement(IRMethod jsMethod, List<ReturnNode> lst) {
 			this.jsMethod = jsMethod;
 			this.lst = lst;
 		}
 
 		@Override
 		public void call(ValidationVisitor visitor) {
 			final IRType methodType = jsMethod.getType();
 			IRType firstType = null;
 			ReturnNode firstNode = null;
 			for (ReturnNode element : lst) {
 				if (element.returnValueReference == null)
 					continue;
 				final IRType type = JavaScriptValidations
 						.typeOf(element.returnValueReference);
 				TypeCompatibility compatibility = null;
 				if (methodType instanceof IRTypeExtension) {
 					final IValidationStatus status = ((IRTypeExtension) methodType)
 							.isAssignableFrom(element.returnValueReference);
 					if (status != null) {
 						if (status instanceof TypeCompatibility) {
 							compatibility = (TypeCompatibility) status;
 						} else if (status != ValidationStatus.OK) {
 							JavaScriptValidations
 									.reportValidationStatus(
 											visitor.getProblemReporter(),
 											status,
 											element.node,
 											JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 											ValidationMessages.DeclarationMismatchWithActualReturnType,
 											jsMethod.getName());
 						}
 					}
 				} else if (type != null && methodType != null) {
 					compatibility = methodType.isAssignableFrom(type);
 				}
 				if (compatibility != null
 						&& compatibility != TypeCompatibility.TRUE) {
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
 				if (methodType == null && firstType == null && type != null) {
 					// remember first type only if return type is not declared.
 					// consistency check makes sense only if no return type
 					// declaration.
 					firstType = type.normalize();
 					firstNode = element;
 				}
 			}
 
 			if (firstType != null) {
 				for (ReturnNode next : lst) {
 					if (next == firstNode) {
 						continue;
 					}
 					IRType nextType = JavaScriptValidations
 							.typeOf(next.returnValueReference);
 					if (nextType != null) {
 						nextType = nextType.normalize();
 						if (!nextType.isAssignableFrom(firstType).ok()
 								&& !firstType.isAssignableFrom(nextType).ok()) {
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
 	}
 
 	private static class NotExistingIdentiferValidator extends
 			ExpressionValidator {
 		private final FunctionScope scope;
 		private final Expression identifer;
 		private final IValueReference reference;
 
 		public NotExistingIdentiferValidator(FunctionScope scope,
 				Expression identifer, IValueReference reference) {
 			this.scope = scope;
 			this.identifer = identifer;
 			this.reference = reference;
 		}
 
 		@Override
 		public void call(ValidationVisitor visitor) {
 			visitor.validate(scope, identifer, reference);
 		}
 	}
 
 	private static class NewExpressionValidator extends ExpressionValidator {
 		private final FunctionScope scope;
 		private final NewExpression node;
 		private final IValueReference reference;
 		private final IValueReference typeReference;
 		private final IValueReference[] arguments;
 		private final IValueCollection collection;
 
 		public NewExpressionValidator(FunctionScope scope, NewExpression node,
 				IValueReference reference, IValueReference typeReference,
 				IValueReference[] arguments, IValueCollection collection) {
 			this.scope = scope;
 			this.node = node;
 			this.reference = reference;
 			this.typeReference = typeReference;
 			this.arguments = arguments;
 			this.collection = collection;
 		}
 
 		@Override
 		public void call(ValidationVisitor visitor) {
 			visitor.validateNewExpression(scope, collection,
 					node.getObjectClass(), reference, typeReference, arguments);
 		}
 
 	}
 
 	private static class PropertyExpressionHolder extends ExpressionValidator {
 		private final FunctionScope scope;
 		private final PropertyExpression node;
 		private final IValueReference reference;
 		private final boolean exists;
 
 		public PropertyExpressionHolder(FunctionScope scope,
 				PropertyExpression node, IValueReference reference,
 				boolean exists) {
 			this.scope = scope;
 			this.node = node;
 			this.reference = reference;
 			this.exists = exists;
 		}
 
 		@Override
 		public void call(ValidationVisitor visitor) {
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
 		private final Set<FunctionStatement> inconsistentReturns;
 
 		public ValidationVisitor(ITypeInferenceContext context,
 				JSProblemReporter reporter,
 				Set<FunctionStatement> inconsistentReturns) {
 			this(context, reporter, inconsistentReturns, false);
 		}
 
 		public ValidationVisitor(ITypeInferenceContext context,
 				JSProblemReporter reporter,
 				Set<FunctionStatement> inconsistentReturns,
 				boolean hasDependents) {
 			super(context);
 			this.reporter = reporter;
 			this.inconsistentReturns = inconsistentReturns;
 			this.bindings = hasDependents ? new HashMap<ASTNode, IValueReference>()
 					: null;
 		}
 
 		private final Map<ASTNode, VisitorMode> modes = new IdentityHashMap<ASTNode, VisitorMode>();
 
 		private final Stack<ASTNode> visitStack = new Stack<ASTNode>();
 
 		final Map<ASTNode, IValueReference> bindings;
 
 		@Override
 		public IValueReference visit(ASTNode node) {
 			visitStack.push(node);
 			try {
 				final IValueReference value = super.visit(node);
 				if (bindings != null && value != null) {
 					bindings.put(node, value);
 				}
 				return value;
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
 			final List<IValidatorExtension> extensions = TypeInfoManager
 					.createExtensions(context, IValidatorExtension.class, null);
 			if (!extensions.isEmpty()) {
 				this.extensions = extensions
 						.toArray(new IValidatorExtension[extensions.size()]);
 			} else {
 				this.extensions = null;
 			}
 			if (getTypeChecker() instanceof ITypeCheckerExtension) {
 				((ITypeCheckerExtension) getTypeChecker())
 						.setExtensions(this.extensions);
 			}
 		}
 
 		private IValidatorExtension[] extensions;
 
 		/**
 		 * Returns {@link ITypeChecker} which can be used for type validations.
 		 */
 		@NonNull
 		@Override
 		public ITypeChecker getTypeChecker() {
 			ITypeChecker result = super.getTypeChecker();
 			if (result == null) {
 				result = new TypeChecker(context, reporter);
 				typeChecker = result;
 			}
 			return result;
 		}
 
 		@Override
 		public void done() {
 			super.done();
 			if (inconsistentReturns != null && !inconsistentReturns.isEmpty()
 					&& reporter instanceof Reporter) {
 				final Reporter r = (Reporter) reporter;
 				for (FunctionStatement statement : inconsistentReturns) {
 					FlowValidation.reportInconsistentReturn(r, statement);
 				}
 			}
 			runDelayedValidations();
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
 			((TypeChecker) typeChecker).validate();
 		}
 
 		/**
 		 * Executes all the delayed validations collected so far.
 		 */
 		public void runDelayedValidations() {
 			final ISuppressWarningsState suppressWarnings = reporter
 					.getSuppressWarnings();
 			try {
 				final ExpressionValidator[] copy = expressionValidators
 						.toArray(new ExpressionValidator[expressionValidators
 								.size()]);
 				expressionValidators.clear();
 				for (ExpressionValidator call : copy) {
 					reporter.restoreSuppressWarnings(call.getSuppressed());
 					call.call(this);
 				}
 			} finally {
 				reporter.restoreSuppressWarnings(suppressWarnings);
 			}
 		}
 
 		private VisitorMode currentMode() {
 			final VisitorMode mode = modes.get(visitStack.peek());
 			return mode != null ? mode : VisitorMode.NORMAL;
 		}
 
 		@Override
 		public IValueReference visitNewExpression(NewExpression node) {
 			final VisitNewResult result = visitNew(node);
 			if (result.getTypeValue() != null) {
 				pushExpressionValidator(new NewExpressionValidator(
 						peekFunctionScope(), node, result.getValue(),
 						result.getTypeValue(), result.getArguments(),
 						peekContext()));
 			}
 			return result.getValue();
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
 				} else if (expression instanceof GetArrayItemExpression) {
 					expression = ((GetArrayItemExpression) expression)
 							.getArray();
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
 
 		public void leaveFunctionScope(IRMethod method,
 				FunctionStatement function) {
 			final FunctionScope scope = functionScopes.pop();
 			if (method != null) {
 				if (inconsistentReturns != null && method.getType() != null
 						&& RTypes.isUndefined(method.getType())) {
 					inconsistentReturns.remove(function);
 				}
 				if (!scope.returnNodes.isEmpty()) {
 					// method.setType(context.resolveTypeRef(method.getType()));
 					pushExpressionValidator(new TestReturnStatement(method,
 							scope.returnNodes));
 				} else if (!scope.throwsException && method.getType() != null
 						&& !RTypes.isUndefined(method.getType())) {
 					final ReferenceLocation location = locationOf(method);
 					if (location == null) {
 						return;
 					}
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
 			enterFunctionScope();
 			IValueReference reference = super.visitFunctionStatement(node);
 			final IRMethod method = (IRMethod) reference.getAttribute(R_METHOD);
 			leaveFunctionScope(method, node);
 
 			return reference;
 		}
 
 		@Override
 		protected JSMethod createMethod(FunctionStatement node) {
 			validateHidesByFunction(node);
 			return super.createMethod(node);
 		}
 
 		private void validateHidesByFunction(FunctionStatement node) {
 			List<Argument> args = node.getArguments();
 			IValueCollection peekContext = peekContext();
 			for (Argument argument : args) {
 				IValueReference child = peekContext.getChild(argument
 						.getArgumentName());
 				if (child.exists()) {
 					if (child.getKind() == ReferenceKind.PROPERTY) {
 						final Property property = ValueReferenceUtil
 								.extractElement(child, Property.class);
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
 						final Property property = ValueReferenceUtil
 								.extractElement(child, Property.class);
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
 				if (reference.getDeclaredTypes().contains(RTypes.FUNCTION)) {
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
 			final List<IRMethod> methods = ValueReferenceUtil.extractElements(
 					reference, IRMethod.class);
 			if (methods != null && methods.size() == 1) {
 				final IRMethod method = methods.get(0);
 				if (method.isGeneric()) {
 					if (!JavaScriptValidations.checkParameterCount(method,
 							args.size())) {
 						final Expression methodNode = expression instanceof PropertyExpression ? ((PropertyExpression) expression)
 								.getProperty() : expression;
 						reportMethodParameterError(methodNode, arguments,
 								method);
 						return null;
 					}
 					final IRType result = evaluateGenericCall(method, arguments);
 					return ConstantValue.of(result);
 				} else {
 					pushExpressionValidator(new CallExpressionValidator(
 							peekFunctionScope(), node, reference, arguments,
 							methods));
 					return ConstantValue.of(method.getType());
 				}
 			} else {
 				pushExpressionValidator(new CallExpressionValidator(
 						peekFunctionScope(), node, reference, arguments,
 						methods));
 				final IRType expressionType = JavaScriptValidations
 						.typeOf(reference);
 				if (expressionType != null) {
 					if (expressionType instanceof IRFunctionType) {
 						return ConstantValue
 								.of(((IRFunctionType) expressionType)
 										.getReturnType());
 					} else if (expressionType instanceof IRClassType) {
 						final IRTypeDeclaration target = ((IRClassType) expressionType)
 								.getDeclaration();
 						if (target != null) {
 							final IRConstructor constructor = target
 									.getStaticConstructor();
							if (constructor != null) {
 								return new ConstantValue(constructor.getType());
 							}
 						}
 					}
 				}
 			}
 			return reference.getChild(IValueReference.FUNCTION_OP);
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
 				IValueReference[] arguments, List<IRMethod> methods) {
 
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
 				methods = ValueReferenceUtil.extractElements(reference,
 						IRMethod.class);
 			if (methods != null) {
 				IRMethod method = JavaScriptValidations.selectMethod(methods,
 						arguments, true);
 				if (method == null) {
 					final IRType type = JavaScriptValidations.typeOf(reference
 							.getParent());
 					if (type != null) {
 						if (TypeUtil.kind(type) == TypeKind.JAVA) {
 							reporter.reportProblem(
 									JavaScriptProblems.WRONG_JAVA_PARAMETERS,
 									NLS.bind(
 											ValidationMessages.MethodNotSelected,
 											new String[] {
 													reference.getName(),
 													type.getName(),
 													describeArguments(arguments) }),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 
 						} else {
 							// TODO also a JS error (that should be
 							// configurable)
 						}
 					}
 				} else {
 					validateCallExpressionMethod(node, reference, arguments,
 							methodNode, method);
 				}
 				return;
 			}
 			final Object attrRMethod = reference.getAttribute(R_METHOD, true);
 			if (attrRMethod instanceof IRMethod) {
 				validateCallExpressionRMethod(node, reference, arguments,
 						methodNode, (IRMethod) attrRMethod);
 				return;
 			}
 			final IRType expressionType = JavaScriptValidations
 					.typeOf(reference);
 			if (expressionType != null) {
 				if (expressionType instanceof IRFunctionType) {
 					validateCallExpressionRMethod(node, reference, arguments,
 							methodNode, new RMethodFunctionWrapper(
 									(IRFunctionType) expressionType));
 					return;
 				} else if (expressionType instanceof IRClassType) {
 					final IRTypeDeclaration target = ((IRClassType) expressionType)
 							.getDeclaration();
 					if (target != null) {
 						final IRConstructor constructor = target
 								.getStaticConstructor();
 						if (constructor != null) {
 							validateCallExpressionMethod(node, reference,
 									arguments, methodNode, constructor);
 							return;
 						}
 					}
 				} else if (expressionType != RTypes.any()
 						&& expressionType != RTypes.none()
 						&& !RTypes.FUNCTION.isAssignableFrom(expressionType)
 								.ok()) {
 					reporter.reportProblem(
 							JavaScriptProblems.WRONG_FUNCTION,
 							isIdentifier(expression) ? NLS.bind(
 									ValidationMessages.WrongFunction,
 									reference.getName())
 									: ValidationMessages.WrongFunctionExpression,
 							methodNode.sourceStart(), methodNode.sourceEnd());
 					return;
 				}
 				// we've got expressionType, reference exists, so return.
 				return;
 			}
 			scope.add(path);
 			if (!isDynamicArrayAccess(reference)
 					&& !isUntypedParameter(reference)) {
 
 				final IRType type = JavaScriptValidations.typeOf(reference
 						.getParent());
 				if (type != null) {
 					if (type == RTypes.any()) {
 						return;
 					}
 					if (TypeUtil.kind(type) == TypeKind.JAVA) {
 						reporter.reportProblem(
 								JavaScriptProblems.UNDEFINED_JAVA_METHOD,
 								NLS.bind(ValidationMessages.UndefinedMethod,
 										reference.getName(), type.getName()),
 								methodNode.sourceStart(), methodNode
 										.sourceEnd());
 					} else if (!reference.exists()) {
 						reporter.reportProblem(
 								JavaScriptProblems.UNDEFINED_METHOD,
 								NLS.bind(
 										ValidationMessages.UndefinedMethodOnObject,
 										reference.getName(), reference
 												.getParent().getName()),
 								methodNode.sourceStart(), methodNode
 										.sourceEnd());
 					}
 				} else {
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
 						if (reference.getParent() == null) {
 							if (isIdentifier(expression) && !reference.exists()) {
 								reporter.reportProblem(
 										JavaScriptProblems.UNDEFINED_FUNCTION,
 										NLS.bind(
 												ValidationMessages.UndefinedMethodInScript,
 												reference.getName()),
 										methodNode.sourceStart(), methodNode
 												.sourceEnd());
 							} else {
 								reporter.reportProblem(
 										JavaScriptProblems.WRONG_FUNCTION,
 										isIdentifier(expression) ? NLS
 												.bind(ValidationMessages.WrongFunction,
 														reference.getName())
 												: ValidationMessages.WrongFunctionExpression,
 										methodNode.sourceStart(), methodNode
 												.sourceEnd());
 							}
 						} else {
 							reporter.reportProblem(
 									JavaScriptProblems.UNDEFINED_METHOD,
 									NLS.bind(
 											ValidationMessages.UndefinedMethodOnObject,
 											reference.getName(), reference
 													.getParent().getName()),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 						}
 					}
 				}
 			}
 		}
 
 		private void validateCallExpressionRMethod(CallExpression node,
 				final IValueReference reference, IValueReference[] arguments,
 				final Expression methodNode, IRMethod method) {
 			if (method.isDeprecated()) {
 				reporter.reportProblem(JavaScriptProblems.DEPRECATED_FUNCTION,
 						NLS.bind(ValidationMessages.DeprecatedFunction,
 								reference.getName()), methodNode.sourceStart(),
 						methodNode.sourceEnd());
 			}
 			validateAccessibility(methodNode, reference, method);
 			final List<IRParameter> parameters = method.getParameters();
 			final TypeCompatibility compatibility = validateParameters(
 					parameters, arguments, methodNode);
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
 				reporter.reportProblem(problemId, NLS.bind(
 						ValidationMessages.MethodNotApplicableInScript,
 						new String[] { name, describeParamTypes(parameters),
 								describeArguments(arguments, parameters) }),
 						methodNode.sourceStart(), methodNode.sourceEnd());
 			}
 		}
 
 		private void validateCallExpressionMethod(CallExpression node,
 				final IValueReference reference, IValueReference[] arguments,
 				final Expression methodNode, IRMethod method) {
 			if (method.getVisibility() != Visibility.PUBLIC) {
 				if (!validateAccessibility(methodNode, method)) {
 					return;
 				}
 			}
 			if (method.isDeprecated()) {
 				reportDeprecatedMethod(methodNode, reference, method);
 			}
 			if (!JavaScriptValidations.checkParameterCount(method, node
 					.getArguments().size())) {
 				reportMethodParameterError(methodNode, arguments, method);
 				return;
 			}
 			final List<IRParameter> parameters = method.getParameters();
 			final TypeCompatibility compatibility = validateParameters(
 					parameters, arguments, methodNode);
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
 										describeArguments(arguments, parameters) }),
 						methodNode.sourceStart(), methodNode.sourceEnd());
 			}
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
 					if (type == null || type == RTypes.any()) {
 						return true;
 					}
 				} else if (kind == ReferenceKind.THIS
 						&& reference.getDeclaredType() == null
 						&& reference.getDirectChildren().isEmpty()) {
 					return true;
 				}
 				reference = reference.getParent();
 			}
 			return false;
 		}
 
 		private boolean hasInstanceMethod(IRType type, String name) {
 			return ElementValue.findMember(getContext(), type, name,
 					MemberPredicates.NON_STATIC) != null;
 		}
 
 		/**
 		 * Tests if reference has array access somewhere above which is not
 		 * applied to {@link IRArrayType} or {@link IRMapType} types.
 		 */
 		private boolean isDynamicArrayAccess(IValueReference reference) {
 			for (; reference != null; reference = reference.getParent()) {
 				if (reference.getName() == IValueReference.ARRAY_OP) {
 					final IRType containerType = JavaScriptValidations
 							.typeOf(reference.getParent());
 					if (containerType instanceof IRArrayType
 							|| containerType instanceof IRMapType) {
 						break;
 					}
 					// ignore array lookup function calls
 					// like: array[1](),
 					// those are dynamic.
 					return true;
 				}
 			}
 			return false;
 		}
 
 		private void reportDeprecatedMethod(ASTNode methodNode,
 				IValueReference reference, IRMethod method) {
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
 				IValueReference[] arguments, IRMethod method) {
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
 								describeArguments(arguments) }), methodNode
 						.sourceStart(), methodNode.sourceEnd());
 			} else {
 				reporter.reportProblem(JavaScriptProblems.WRONG_PARAMETERS, NLS
 						.bind(ValidationMessages.TopLevelMethodNotApplicable,
 								new String[] {
 										method.getName(),
 										describeParamTypes(method
 												.getParameters()),
 										describeArguments(arguments) }),
 						methodNode.sourceStart(), methodNode.sourceEnd());
 			}
 		}
 
 		private final List<ValidationStatus> statuses = new ArrayList<ValidationStatus>();
 
 		private TypeCompatibility validateParameters(
 				List<IRParameter> parameters, IValueReference[] arguments,
 				ISourceNode problemNode) {
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
 
 			statuses.clear();
 			TypeCompatibility result = TypeCompatibility.TRUE;
 			for (int i = 0; i < testTypesSize; i++) {
 				final IValueReference argument = arguments[i];
 				final IRType argumentType = JavaScriptValidations
 						.typeOf(argument);
 				final IRParameter parameter = parameters.get(i);
 				if (parameter.getType() instanceof IRTypeExtension) {
 					final IValidationStatus status = ((IRTypeExtension) parameter
 							.getType()).isAssignableFrom(argument);
 					if (status instanceof TypeCompatibility) {
 						final TypeCompatibility pResult = (TypeCompatibility) status;
 						if (pResult.after(result)) {
 							if (pResult == TypeCompatibility.FALSE
 									&& statuses.isEmpty()) {
 								return pResult;
 							}
 							result = pResult;
 						}
 					} else if (status instanceof ValidationStatus) {
 						statuses.add((ValidationStatus) status);
 					} else if (status instanceof ValidationMultiStatus) {
 						Collections.addAll(statuses,
 								((ValidationMultiStatus) status).getChildren());
 					}
 				} else {
 					final TypeCompatibility pResult = testArgumentType(
 							parameter.getType(), argument);
 					if (pResult.after(result)) {
 						if (pResult == TypeCompatibility.FALSE
 								&& statuses.isEmpty()) {
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
 						if (pResult == TypeCompatibility.FALSE
 								&& statuses.isEmpty()) {
 							return pResult;
 						}
 						result = pResult;
 					}
 				}
 
 			}
 			if (!statuses.isEmpty()) {
 				for (ValidationStatus status : statuses) {
 					final int start;
 					final int end;
 					if (status.hasRange()) {
 						start = status.start();
 						end = status.end();
 					} else {
 						start = problemNode.start();
 						end = problemNode.end();
 					}
 					reporter.reportProblem(status.identifier(),
 							status.message(), start, end);
 				}
 				return TypeCompatibility.TRUE;
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
 				final IRType argumentType = JavaScriptValidations
 						.typeOf(argument);
 				if (argumentType != null) {
 					return paramType.isAssignableFrom(argumentType);
 				}
 			}
 			return TypeCompatibility.TRUE;
 		}
 
 		private String describeParamTypes(List<IRParameter> parameters) {
 			StringBuilder sb = new StringBuilder();
 			for (IRParameter parameter : parameters) {
 				if (sb.length() != 0) {
 					sb.append(',');
 				}
 				if (parameter.getType() instanceof IRRecordType) {
 					sb.append('{');
 					for (IRRecordMember member : ((IRRecordType) parameter
 							.getType()).getMembers()) {
 						if (sb.length() > 1)
 							sb.append(", ");
 						final boolean optional = member.isOptional();
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
 		 * Describes the specified arguments.
 		 */
 		private String describeArguments(IValueReference[] arguments) {
 			return describeArguments(arguments,
 					Collections.<IRParameter> emptyList());
 		}
 
 		/**
 		 * Describes the specified arguments, expanding properties for those
 		 * where record type parameter is expected.
 		 */
 		private String describeArguments(IValueReference[] arguments,
 				List<IRParameter> parameters) {
 			final StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < arguments.length; i++) {
 				final IValueReference argument = arguments[i];
 				final IRParameter parameter = i < parameters.size() ? parameters
 						.get(i) : null;
 				if (sb.length() != 0) {
 					sb.append(',');
 				}
 				if (argument == null) {
 					sb.append("null");
 				} else if (parameter != null
 						&& parameter.getType() instanceof IRRecordType) {
 					describeRecordTypeArgument(sb, argument,
 							(IRRecordType) parameter.getType());
 				} else if (argument.getDeclaredType() != null) {
 					sb.append(argument.getDeclaredType().getName());
 				} else {
 					final JSTypeSet types = argument.getTypes();
 					if (types.size() == 1) {
 						sb.append(types.toRType().getName());
 					} else {
 						sb.append('?');
 					}
 				}
 			}
 			return sb.toString();
 		}
 
 		/**
 		 * Describes the specified argument which is expected to be of the
 		 * record type.
 		 */
 		private void describeRecordTypeArgument(StringBuilder sb,
 				IValueReference argument, @Nullable IRRecordType expectedType) {
 			sb.append('{');
 			boolean appendComma = false;
 			// TODO (alex) direct children and typed children.
 			for (String childName : argument.getDirectChildren()) {
 				if (appendComma)
 					sb.append(", ");
 				appendComma = true;
 				sb.append(childName);
 
 				final IRType expectedMemberType;
 				if (expectedType != null) {
 					final IRRecordMember member = expectedType
 							.getMember(childName);
 					expectedMemberType = member != null ? member.getType()
 							: null;
 				} else {
 					expectedMemberType = null;
 				}
 				if (expectedMemberType instanceof IRRecordType) {
 					sb.append(": ");
 					describeRecordTypeArgument(sb,
 							argument.getChild(childName),
 							(IRRecordType) expectedMemberType);
 				} else {
 					final IValueReference child = argument.getChild(childName);
 					final IRType type = JavaScriptValidations.typeOf(child);
 					if (type != null) {
 						if (expectedType != null
 								&& type.getName().equals(ITypeNames.OBJECT)
 								&& !child.getDirectChildren().isEmpty()) {
 							sb.append(": ");
 							describeRecordTypeArgument(sb, child, null);
 						} else {
 							sb.append(':');
 							sb.append(type.getName());
 						}
 					}
 				}
 			}
 			sb.append('}');
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
 						peekFunctionScope(), node, result, result.exists()));
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
 					validateAccessibility(expr, reference, null);
 
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
 
 		private static boolean isVarOrFunction(IValueReference reference) {
 			final ReferenceKind kind = reference.getKind();
 			return kind.isVariable() || kind == ReferenceKind.FUNCTION;
 		}
 
 		private static boolean isAccess(Identifier node) {
 			return isAccess(node, node.getParent());
 		}
 
 		private static boolean isAccess(Identifier node, final ASTNode parent) {
 			if (parent instanceof BinaryOperation) {
 				return !((BinaryOperation) parent).isAssignmentTo(node);
 			} else if (parent instanceof StatementBlock
 					|| parent instanceof Script) {
 				return false;
 			} else if (parent instanceof UnaryOperation) {
 				final UnaryOperation operation = (UnaryOperation) parent;
 				final int op = operation.getOperation();
 				return op != JSParser.INC && op != JSParser.DEC
 						&& op != JSParser.PINC && op != JSParser.PDEC
 						|| isAccess(node, operation.getParent());
 			} else {
 				return true;
 			}
 		}
 
 		@Override
 		public IValueReference visitIdentifier(Identifier node) {
 			final IValueReference result = super.visitIdentifier(node);
 			if (isAccess(node) && isVarOrFunction(result)
 					&& getSource().equals(result.getLocation().getSource())) {
 				if (result.getAttribute(IReferenceAttributes.ACCESS) == null) {
 					result.setAttribute(IReferenceAttributes.ACCESS,
 							Boolean.TRUE);
 				}
 			}
 			final IRProperty property = ValueReferenceUtil.extractElement(
 					result, IRProperty.class);
 			if (property != null && property.isDeprecated()) {
 				reportDeprecatedProperty(property, null, node);
 			} else {
 				if (!result.exists()
 						&& !(node.getParent() instanceof CallExpression && ((CallExpression) node
 								.getParent()).getExpression() == node)) {
 					pushExpressionValidator(new NotExistingIdentiferValidator(
 							peekFunctionScope(), node, result));
 				} else {
 					validateAccessibility(node, result, null);
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
 
 		private final List<IValueReference> variables = new ArrayList<IValueReference>();
 
 		@Override
 		protected IValueReference createVariable(IValueCollection context,
 				VariableDeclaration declaration) {
 			validateHidesByVariable(context, declaration);
 			final IValueReference variable = super.createVariable(context,
 					declaration);
 			if (context.getParent() != null
 					|| canValidateUnusedVariable(context, variable)) {
 				variables.add(variable);
 			}
 			return variable;
 		}
 
 		private boolean canValidateUnusedVariable(IValueCollection collection,
 				IValueReference reference) {
 			if (extensions != null) {
 				for (IValidatorExtension extension : extensions) {
 					final IValidatorExtension.UnusedVariableValidation result = extension
 							.canValidateUnusedVariable(collection, reference);
 					if (result != null) {
 						return result == IValidatorExtension.UnusedVariableValidation.TRUE;
 					}
 				}
 			}
 			return isPrivate(reference);
 		}
 
 		private static boolean isPrivate(IValueReference reference) {
 			final IVariable variable = (IVariable) reference
 					.getAttribute(IReferenceAttributes.VARIABLE);
 			return variable != null
 					&& variable.getVisibility() == Visibility.PRIVATE;
 		}
 
 		private void checkAssign(IValueReference reference, ASTNode node) {
 			final Object value = reference
 					.getAttribute(IAssignProtection.ATTRIBUTE);
 			if (value != null) {
 				final IAssignProtection assign;
 				if (value instanceof IAssignProtection2) {
 					if (((IAssignProtection2) value).isReadOnly(reference)) {
 						assign = (IAssignProtection) value;
 					} else {
 						return;
 					}
 				} else {
 					assign = value instanceof IAssignProtection ? (IAssignProtection) value
 							: PROTECT_CONST;
 				}
 				reporter.reportProblem(assign.problemId(),
 						assign.problemMessage(), node.sourceStart(),
 						node.sourceEnd());
 			} else if (reference.getKind() == ReferenceKind.FUNCTION) {
 				reporter.reportProblem(JavaScriptProblems.UNASSIGNABLE_ELEMENT,
 						ValidationMessages.UnassignableFunction,
 						node.sourceStart(), node.sourceEnd());
 			}
 		}
 
 		@Override
 		protected void initializeVariable(IValueReference reference,
 				VariableDeclaration declaration) {
 			if (declaration.getInitializer() != null
 					&& declaration.getParent() instanceof VariableStatement) {
 				checkAssign(reference, declaration);
 			}
 			super.initializeVariable(reference, declaration);
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
 					final Property property = ValueReferenceUtil
 							.extractElement(child, Property.class);
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
 					final IRMethod method = ValueReferenceUtil.extractElement(
 							child, IRMethod.class);
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
 			final IRMember member = ValueReferenceUtil.extractElement(result,
 					IRMember.class);
 			if (member != null) {
 				if (member.isDeprecated()) {
 					final IRProperty parentProperty = ValueReferenceUtil
 							.extractElement(result.getParent(),
 									IRProperty.class);
 					if (parentProperty != null
 							&& parentProperty.getDeclaringType() == null) {
 						if (member instanceof IRProperty)
 							reportDeprecatedProperty((IRProperty) member,
 									parentProperty, propName);
 						else if (member instanceof IRMethod)
 							reportDeprecatedMethod(propName, result,
 									(IRMethod) member);
 					} else {
 						if (member instanceof IRProperty)
 							reportDeprecatedProperty((IRProperty) member,
 									member.getDeclaringType(), propName);
 						else if (member instanceof IRMethod)
 							reportDeprecatedMethod(propName, result,
 									(IRMethod) member);
 					}
 				} else if (!member.isVisible()) {
 					final IRProperty parentProperty = ValueReferenceUtil
 							.extractElement(result.getParent(),
 									IRProperty.class);
 					if (parentProperty != null
 							&& parentProperty.getDeclaringType() == null) {
 						if (member instanceof IRProperty)
 							reportHiddenProperty((IRProperty) member,
 									parentProperty, propName);
 						// else if (member instanceof Method)
 						// reportDeprecatedMethod(propName, result,
 						// (Method) member);
 					} else if (member instanceof IRProperty) {
 						reportHiddenProperty((IRProperty) member,
 								member.getDeclaringType(), propName);
 					}
 					// } else if
 					// (JavaScriptValidations.isStatic(result.getParent())
 					// && !member.isStatic()) {
 					// IRType type = JavaScriptValidations.typeOf(result
 					// .getParent());
 					// reporter.reportProblem(
 					// JavaScriptProblems.INSTANCE_PROPERTY,
 					// NLS.bind(
 					// ValidationMessages.StaticReferenceToNoneStaticProperty,
 					// result.getName(), TypeUtil.getName(type)),
 					// propName.sourceStart(), propName.sourceEnd());
 					// } else if
 					// (!JavaScriptValidations.isStatic(result.getParent())
 					// && member.isStatic()) {
 					// IRType type = JavaScriptValidations.typeOf(result
 					// .getParent());
 					// reporter.reportProblem(
 					// JavaScriptProblems.STATIC_PROPERTY,
 					// NLS.bind(
 					// ValidationMessages.ReferenceToStaticProperty,
 					// result.getName(), type.getName()), propName
 					// .sourceStart(), propName.sourceEnd());
 				} else if (member.getVisibility() != Visibility.PUBLIC) {
 					validateAccessibility(propName, member);
 				}
 			} else if ((!exists && !result.exists())
 					&& !isDynamicArrayAccess(result)) {
 				scope.add(path);
 				final IRType parentType = typeOf(result.getParent());
 				if (parentType != null && parentType.isExtensible()) {
 					return;
 				}
 				final TypeKind parentKind = TypeUtil.kind(parentType);
 				if (parentType != null && parentKind == TypeKind.JAVA) {
 					reporter.reportProblem(
 							JavaScriptProblems.UNDEFINED_JAVA_PROPERTY, NLS
 									.bind(ValidationMessages.UndefinedProperty,
 											result.getName(),
 											parentType.getName()), propName
 									.sourceStart(), propName.sourceEnd());
 				} else if (!belongsToLogicalExpression(propertyExpression)) {
 					if (parentType != null
 							&& (parentKind == TypeKind.JAVASCRIPT || parentKind == TypeKind.PREDEFINED)) {
 						reporter.reportProblem(
 								JavaScriptProblems.UNDEFINED_PROPERTY,
 								NLS.bind(
 										ValidationMessages.UndefinedPropertyInScriptType,
 										result.getName(), parentType.getName()),
 								propName.sourceStart(), propName.sourceEnd());
 					} else {
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
 					validateAccessibility(propName, result, variable);
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
 						validateAccessibility(propName, result, method);
 						return;
 
 					}
 				}
 			}
 		}
 
 		/**
 		 * Tests if the specified expression is part of {@link BinaryOperation}
 		 * combining parts with logical AND or OR.
 		 */
 		private boolean belongsToLogicalExpression(Expression expression) {
 			if (expression.getParent() instanceof BinaryOperation) {
 				final BinaryOperation bo = (BinaryOperation) expression
 						.getParent();
 				return bo.getOperation() == JSParser.LAND
 						|| bo.getOperation() == JSParser.LOR;
 			}
 			return false;
 		}
 
 		private void reportDeprecatedProperty(IRProperty property,
 				IRElement owner, ASTNode node) {
 			final String msg;
 			if (owner instanceof IRType) {
 				msg = NLS.bind(ValidationMessages.DeprecatedProperty,
 						property.getName(), owner.getName());
 			} else if (owner instanceof IRProperty) {
 				msg = NLS.bind(ValidationMessages.DeprecatedPropertyOfInstance,
 						property.getName(), owner.getName());
 			} else {
 				msg = NLS.bind(ValidationMessages.DeprecatedPropertyNoType,
 						property.getName());
 			}
 			reporter.reportProblem(JavaScriptProblems.DEPRECATED_PROPERTY, msg,
 					node.sourceStart(), node.sourceEnd());
 		}
 
 		private void reportHiddenProperty(IRProperty property, IRElement owner,
 				ASTNode node) {
 			final String msg;
 			if (owner instanceof IRType) {
 				msg = NLS.bind(ValidationMessages.HiddenProperty,
 						property.getName(), owner.getName());
 			} else if (owner instanceof IRProperty) {
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
 
 		private IRTypeDeclaration extractClassType(IValueReference typeReference) {
 			IRType type = typeReference.getDeclaredType();
 			if (type == null) {
 				final JSTypeSet types = typeReference.getTypes();
 				if (types.size() > 0) {
 					type = types.getFirst();
 				}
 			}
 			if (type != null && type instanceof IRClassType) {
 				return ((IRClassType) type).getDeclaration();
 			} else {
 				return null;
 			}
 		}
 
 		/**
 		 * Lazy validation of the {@link NewExpression}.
 		 */
 		protected void validateNewExpression(FunctionScope scope,
 				IValueCollection collection, Expression node,
 				IValueReference reference, IValueReference typeReference,
 				IValueReference[] arguments) {
 			final Identifier identifier = getIdentifier(node);
 			final Expression problemNode = identifier != null ? identifier
 					: node;
 			if (typeReference.getParent() == null && isIdentifier(node)
 					&& !typeReference.exists()) {
 				scope.add(path(node, typeReference));
 				reportUnknownType(JavaScriptProblems.UNDECLARED_VARIABLE,
 						problemNode, identifier != null ? identifier.getName()
 								: "?");
 				return;
 			}
 			final IRTypeDeclaration type = extractClassType(typeReference);
 			if (type != null) {
 				if (type.getKind() != TypeKind.UNKNOWN) {
 					if (!validateInstantiability(problemNode, type.getSource(),
 							typeReference)) {
 						return;
 					}
 					checkTypeReference(problemNode, type);
 					final List<IRConstructor> constructors = TypeUtil
 							.findConstructors(type);
 					if (!constructors.isEmpty()) {
 						final IRConstructor constructor = JavaScriptValidations
 								.selectMethod(constructors, arguments, false);
 						if (constructor == null) {
 							reporter.reportProblem(
 									JavaScriptProblems.WRONG_PARAMETERS,
 									NLS.bind(
 											"The constructor {0}({1}) is undefined",
 											new String[] {
 													typeReference.getName(),
 													describeArguments(arguments) }),
 									problemNode.sourceStart(), problemNode
 											.sourceEnd());
 							return;
 						}
 						if (constructor.isDeprecated()) {
 							reportDeprecatedMethod(problemNode, typeReference,
 									constructor);
 						}
 						final List<IRParameter> parameters = context
 								.contextualize(constructor, type)
 								.getParameters();
 						final TypeCompatibility compatibility = validateParameters(
 								parameters, arguments, problemNode);
 						if (compatibility != TypeCompatibility.TRUE) {
 							reporter.reportProblem(
 									compatibility == TypeCompatibility.FALSE ? JavaScriptProblems.WRONG_PARAMETERS
 											: JavaScriptProblems.WRONG_PARAMETERS_PARAMETERIZATION,
 									NLS.bind(
 											"The constructor {0}({1}) is not applicable for the arguments ({2})",
 											new String[] {
 													typeReference.getName(),
 													describeParamTypes(parameters),
 													describeArguments(
 															arguments,
 															parameters) }),
 									problemNode.sourceStart(), problemNode
 											.sourceEnd());
 						}
 					}
 				}
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
 			if (type instanceof IRClassType) {
 				final IRTypeDeclaration t = ((IRClassType) type)
 						.getDeclaration();
 				if (t != null && t.getKind() != TypeKind.UNKNOWN) {
 					checkTypeReference(node, t);
 				}
 			}
 		}
 
 		private void checkTypeReference(ASTNode node, final IRTypeDeclaration t) {
 			if (t.isDeprecated()) {
 				reporter.reportProblem(JavaScriptProblems.DEPRECATED_TYPE, NLS
 						.bind(ValidationMessages.DeprecatedType, t.getName()),
 						node.sourceStart(), node.sourceEnd());
 			}
 		}
 
 		/**
 		 * Validates instantiability of the specified type. Returns
 		 * <code>true</code> if type could be instantiated and
 		 * <code>false</code> otherwise.
 		 * 
 		 * @param node
 		 * @param type
 		 * @param typeReference
 		 * @return
 		 */
 		private boolean validateInstantiability(ASTNode node, final Type type,
 				IValueReference typeReference) {
 			if (extensions != null) {
 				for (IValidatorExtension extension : extensions) {
 					final IValidationStatus result = extension.canInstantiate(
 							type, typeReference);
 					if (result != null) {
 						if (result == ValidationStatus.OK) {
 							return true;
 						} else {
 							JavaScriptValidations.reportValidationStatus(
 									reporter, result, node,
 									JavaScriptProblems.NON_INSTANTIABLE_TYPE,
 									ValidationMessages.NonInstantiableType,
 									type.getName());
 							return false;
 						}
 					}
 				}
 			}
 			if (!type.isInstantiable()) {
 				reporter.reportProblem(
 						JavaScriptProblems.NON_INSTANTIABLE_TYPE,
 						NLS.bind(ValidationMessages.NonInstantiableType,
 								type.getName()), node.sourceStart(),
 						node.sourceEnd());
 				return false;
 			}
 			return true;
 		}
 
 		/**
 		 * Tests if the member is accessible. Returns <code>true</code> if
 		 * access is allowed and <code>false</code> otherwise.
 		 * 
 		 * @param node
 		 * @param member
 		 * @return
 		 */
 		private boolean validateAccessibility(ASTNode node, IRMember member) {
 			if (extensions != null && member.getSource() instanceof Member) {
 				final Member source = (Member) member.getSource();
 				for (IValidatorExtension extension : extensions) {
 					final IValidationStatus result = extension
 							.validateAccessibility(source);
 					if (result != null) {
 						if (result == ValidationStatus.OK) {
 							return true;
 						} else {
 							JavaScriptValidations.reportValidationStatus(
 									reporter, result, node,
 									JavaScriptProblems.INACCESSIBLE_MEMBER,
 									ValidationMessages.InaccessibleMember,
 									member.getName());
 							return false;
 						}
 					}
 				}
 			}
 			return true;
 		}
 
 		private MemberValidationEvent memberValidationEvent;
 
 		/**
 		 * Tests if the specified member is accessible.
 		 * 
 		 * @param expression
 		 *            AST node
 		 * @param reference
 		 *            evaluated value reference
 		 * @param member
 		 *            runtime variable/function reference if already evaluated
 		 *            or <code>null</code> if not evaluated yet
 		 */
 		private void validateAccessibility(Expression expression,
 				IValueReference reference, IRMember member) {
 			if (extensions != null) {
 				if (memberValidationEvent == null) {
 					memberValidationEvent = new MemberValidationEvent();
 				}
 				memberValidationEvent.set(reference, member);
 				for (IValidatorExtension extension : extensions) {
 					final IValidationStatus result = extension
 							.validateAccessibility(memberValidationEvent);
 					if (result != null) {
 						if (result == ValidationStatus.OK) {
 							return;
 						} else {
 							IRMember rMember = memberValidationEvent
 									.getRMember();
 							JavaScriptValidations.reportValidationStatus(
 									reporter, result, expression,
 									JavaScriptProblems.INACCESSIBLE_MEMBER,
 									ValidationMessages.InaccessibleMember,
 									rMember == null ? reference.getName()
 											: rMember.getName());
 							return;
 						}
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
 
 		private static boolean stronglyTyped(IValueReference reference) {
 			final IRType parentType = typeOf(reference);
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
 					&& node.getCondition() instanceof PropertyExpression) {
 				final IValueReference parent = condition.getParent();
 				if (parent != null && parent.exists() && !stronglyTyped(parent)) {
 					if (DEBUG) {
 						System.out.println("visitIfStatement("
 								+ node.getCondition() + ") doesn't exist "
 								+ condition + " - create it");
 					}
 					condition.setValue(PhantomValueReference.REFERENCE);
 				}
 			}
 			visitIfStatements(node);
 			return null;
 		}
 	}
 
 	static final boolean DEBUG = false;
 
 }
