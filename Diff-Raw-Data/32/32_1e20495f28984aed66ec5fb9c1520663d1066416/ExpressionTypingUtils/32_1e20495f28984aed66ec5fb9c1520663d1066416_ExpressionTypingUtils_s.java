 /*
  * Copyright 2010-2012 JetBrains s.r.o.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.compiler.lang.types.expressions;
 
 import static org.napile.compiler.lang.diagnostics.Errors.RESULT_TYPE_MISMATCH;
 import static org.napile.compiler.lang.resolve.BindingContext.CAPTURED_IN_CLOSURE;
 import static org.napile.compiler.lang.resolve.BindingContext.EXPRESSION_TYPE;
 import static org.napile.compiler.lang.resolve.BindingContext.PROCESSED;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.asm.lib.NapileLangPackage;
 import org.napile.asm.resolve.name.FqName;
 import org.napile.asm.resolve.name.Name;
 import org.napile.compiler.NapileNodeTypes;
 import org.napile.compiler.lang.descriptors.CallableDescriptor;
 import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
 import org.napile.compiler.lang.descriptors.TypeParameterDescriptor;
 import org.napile.compiler.lang.descriptors.VariableDescriptor;
 import org.napile.compiler.lang.psi.NapileExpression;
 import org.napile.compiler.lang.psi.NapileImportDirective;
 import org.napile.compiler.lang.psi.NapilePsiFactory;
 import org.napile.compiler.lang.psi.NapileSimpleNameExpression;
 import org.napile.compiler.lang.resolve.BindingContextUtils;
 import org.napile.compiler.lang.resolve.BindingTrace;
 import org.napile.compiler.lang.resolve.BindingTraceContext;
 import org.napile.compiler.lang.resolve.TraceBasedRedeclarationHandler;
 import org.napile.compiler.lang.resolve.calls.autocasts.DataFlowInfo;
 import org.napile.compiler.lang.resolve.calls.inference.ConstraintSystem;
 import org.napile.compiler.lang.resolve.calls.inference.ConstraintSystemImpl;
 import org.napile.compiler.lang.resolve.calls.inference.ConstraintsUtil;
 import org.napile.compiler.lang.resolve.processors.QualifiedExpressionResolver;
 import org.napile.compiler.lang.resolve.scopes.JetScope;
 import org.napile.compiler.lang.resolve.scopes.WritableScope;
 import org.napile.compiler.lang.resolve.scopes.WritableScopeImpl;
 import org.napile.compiler.lang.resolve.scopes.receivers.ExpressionReceiver;
 import org.napile.compiler.lang.resolve.scopes.receivers.ReceiverDescriptor;
 import org.napile.compiler.lang.types.JetType;
 import org.napile.compiler.lang.types.NamespaceType;
 import org.napile.compiler.lang.types.TypeUtils;
import org.napile.compiler.lang.types.checker.JetTypeChecker;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.psi.tree.TokenSet;
 
 /**
  * @author abreslav
  */
 public class ExpressionTypingUtils
 {
 
 	private ExpressionTypingUtils()
 	{
 	}
 
 	@Nullable
 	protected static ExpressionReceiver getExpressionReceiver(@NotNull NapileExpression expression, @Nullable JetType type)
 	{
 		if(type == null)
 			return null;
 		return new ExpressionReceiver(expression, type);
 	}
 
 	@Nullable
 	protected static ExpressionReceiver getExpressionReceiver(@NotNull ExpressionTypingFacade facade, @NotNull NapileExpression expression, ExpressionTypingContext context)
 	{
 		return getExpressionReceiver(expression, facade.getTypeInfo(expression, context).getType());
 	}
 
 	@NotNull
 	protected static ExpressionReceiver safeGetExpressionReceiver(@NotNull ExpressionTypingFacade facade, @NotNull NapileExpression expression, ExpressionTypingContext context)
 	{
 		return new ExpressionReceiver(expression, facade.safeGetTypeInfo(expression, context).getType());
 	}
 
 	@NotNull
 	public static WritableScopeImpl newWritableScopeImpl(ExpressionTypingContext context, @NotNull String scopeDebugName)
 	{
 		WritableScopeImpl scope = new WritableScopeImpl(context.scope, context.scope.getContainingDeclaration(), new TraceBasedRedeclarationHandler(context.trace), scopeDebugName);
 		scope.changeLockLevel(WritableScope.LockLevel.BOTH);
 		return scope;
 	}
 
 	public static boolean isBoolean(@NotNull JetType type)
 	{
		return JetTypeChecker.INSTANCE.isSubtypeOf(type, TypeUtils.getTypeOfClassOrErrorType(type.getMemberScope(), NapileLangPackage.BOOL, false));
 	}
 
 	public static boolean ensureBooleanResult(NapileExpression operationSign, Name name, JetType resultType, ExpressionTypingContext context)
 	{
 		return ensureBooleanResultWithCustomSubject(operationSign, resultType, "'" + name + "'", context);
 	}
 
 	public static boolean ensureBooleanResultWithCustomSubject(NapileExpression operationSign, JetType resultType, String subjectName, ExpressionTypingContext context)
 	{
 		if(resultType != null)
 		{
 			// TODO : Relax?
 			if(!isBoolean(resultType))
 			{
 				context.trace.report(RESULT_TYPE_MISMATCH.on(operationSign, subjectName, TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.BOOL, false), resultType));
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Nullable
 	public static JetType getDefaultType(IElementType constantType, JetScope jetScope)
 	{
 		if(constantType == NapileNodeTypes.INTEGER_CONSTANT)
 		{
 			return TypeUtils.getTypeOfClassOrErrorType(jetScope, NapileLangPackage.INT, false);
 		}
 		else if(constantType == NapileNodeTypes.FLOAT_CONSTANT)
 		{
 			return TypeUtils.getTypeOfClassOrErrorType(jetScope, NapileLangPackage.DOUBLE, false);
 		}
 		else if(constantType == NapileNodeTypes.BOOLEAN_CONSTANT)
 		{
 			return TypeUtils.getTypeOfClassOrErrorType(jetScope, NapileLangPackage.BOOL, false);
 		}
 		else if(constantType == NapileNodeTypes.CHARACTER_CONSTANT)
 		{
 			return TypeUtils.getTypeOfClassOrErrorType(jetScope, NapileLangPackage.CHAR, false);
 		}
 		else if(constantType == NapileNodeTypes.NULL)
 		{
 			return TypeUtils.getTypeOfClassOrErrorType(jetScope, NapileLangPackage.NULL, false);
 		}
 		else
 		{
 			throw new IllegalArgumentException("Unsupported constant type: " + constantType);
 		}
 	}
 
 	public static boolean isTypeFlexible(@Nullable NapileExpression expression)
 	{
 		if(expression == null)
 			return false;
 
 		return TokenSet.create(NapileNodeTypes.INTEGER_CONSTANT, NapileNodeTypes.FLOAT_CONSTANT).contains(expression.getNode().getElementType());
 	}
 
 	public static void checkWrappingInRef(NapileSimpleNameExpression expression, ExpressionTypingContext context)
 	{
 		VariableDescriptor variable = BindingContextUtils.extractVariableDescriptorIfAny(context.trace.getBindingContext(), expression, true);
 		if(variable != null)
 		{
 			DeclarationDescriptor containingDeclaration = variable.getContainingDeclaration();
 			if(context.scope.getContainingDeclaration() != containingDeclaration && containingDeclaration instanceof CallableDescriptor)
 			{
 				context.trace.record(CAPTURED_IN_CLOSURE, variable);
 			}
 		}
 	}
 
 	@NotNull
 	public static NapileExpression createStubExpressionOfNecessaryType(@NotNull Project project, @NotNull JetType type, @NotNull BindingTrace trace)
 	{
 		NapileExpression expression = NapilePsiFactory.createExpression(project, "$e");
 		trace.record(PROCESSED, expression);
 		trace.record(EXPRESSION_TYPE, expression, type);
 		return expression;
 	}
 
 	public static boolean isVariableIterable(@NotNull ExpressionTypingServices expressionTypingServices, @NotNull Project project, @NotNull VariableDescriptor variableDescriptor, @NotNull JetScope scope)
 	{
 		NapileExpression expression = NapilePsiFactory.createExpression(project, "fake");
 		ExpressionReceiver expressionReceiver = new ExpressionReceiver(expression, variableDescriptor.getType());
 		ExpressionTypingContext context = ExpressionTypingContext.newContext(expressionTypingServices, new BindingTraceContext(), scope, DataFlowInfo.EMPTY, TypeUtils.NO_EXPECTED_TYPE, false);
 		return ControlStructureTypingVisitor.checkIterableConvention(expressionReceiver, context) != null;
 	}
 
 	/**
 	 * Check that function or property with the given qualified name can be resolved in given scope and called on given receiver
 	 *
 	 * @param callableFQN
 	 * @param project
 	 * @param scope
 	 * @return
 	 */
 	public static List<CallableDescriptor> canFindSuitableCall(@NotNull FqName callableFQN, @NotNull Project project, @NotNull NapileExpression receiverExpression, @NotNull JetType receiverType, @NotNull JetScope scope)
 	{
 
 		NapileImportDirective importDirective = NapilePsiFactory.createImportDirective(project, callableFQN.getFqName());
 
 		Collection<? extends DeclarationDescriptor> declarationDescriptors = new QualifiedExpressionResolver().analyseImportReference(importDirective, scope, new BindingTraceContext());
 
 		List<CallableDescriptor> callableExtensionDescriptors = new ArrayList<CallableDescriptor>();
 		ReceiverDescriptor receiverDescriptor = new ExpressionReceiver(receiverExpression, receiverType);
 
 		for(DeclarationDescriptor declarationDescriptor : declarationDescriptors)
 		{
 			if(declarationDescriptor instanceof CallableDescriptor)
 			{
 				CallableDescriptor callableDescriptor = (CallableDescriptor) declarationDescriptor;
 
 				if(checkIsExtensionCallable(receiverDescriptor, callableDescriptor))
 				{
 					callableExtensionDescriptors.add(callableDescriptor);
 				}
 			}
 		}
 
 		return callableExtensionDescriptors;
 	}
 
 	/*
 		* Checks if receiver declaration could be resolved to call expected receiver.
 		*/
 	public static boolean checkIsExtensionCallable(@NotNull ReceiverDescriptor expectedReceiver, @NotNull CallableDescriptor receiverArgument)
 	{
 		JetType type = expectedReceiver.getType();
 
 		if(type instanceof NamespaceType)
 		{
 			// This fake class ruins standard algorithms
 			return false;
 		}
 
 		if(checkReceiverResolution(expectedReceiver, type, receiverArgument))
 			return true;
 		if(type.isNullable())
 		{
 			JetType notNullableType = TypeUtils.makeNotNullable(type);
 			if(checkReceiverResolution(expectedReceiver, notNullableType, receiverArgument))
 				return true;
 		}
 		return false;
 	}
 
 	private static boolean checkReceiverResolution(@NotNull ReceiverDescriptor expectedReceiver, @NotNull JetType receiverType, @NotNull CallableDescriptor receiverArgument)
 	{
 		ConstraintSystem constraintSystem = new ConstraintSystemImpl();
 		for(TypeParameterDescriptor typeParameterDescriptor : receiverArgument.getTypeParameters())
 		{
 			constraintSystem.registerTypeVariable(typeParameterDescriptor);
 		}
 
 		return constraintSystem.isSuccessful() && ConstraintsUtil.checkBoundsAreSatisfied(constraintSystem);
 	}
 }
