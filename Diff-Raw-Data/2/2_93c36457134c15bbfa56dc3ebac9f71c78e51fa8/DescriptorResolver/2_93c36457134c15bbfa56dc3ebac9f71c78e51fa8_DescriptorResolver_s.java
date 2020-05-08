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
 
 package org.napile.compiler.lang.resolve;
 
 import static org.napile.compiler.lang.resolve.BindingContext.CONSTRUCTOR;
 import static org.napile.compiler.lang.diagnostics.Errors.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.compiler.lang.descriptors.annotations.AnnotationDescriptor;
 import org.napile.compiler.lang.descriptors.*;
 import org.napile.compiler.lang.diagnostics.DiagnosticUtils;
 import org.napile.compiler.lang.resolve.calls.autocasts.DataFlowInfo;
 import org.jetbrains.jet.lang.resolve.name.Name;
 import org.napile.compiler.lang.psi.*;
 import org.napile.compiler.lang.resolve.scopes.JetScope;
 import org.napile.compiler.lang.resolve.scopes.WritableScope;
 import org.napile.compiler.lang.resolve.scopes.WritableScopeImpl;
 import org.napile.compiler.lang.resolve.scopes.receivers.ExtensionReceiver;
 import org.napile.compiler.lang.resolve.scopes.receivers.ReceiverDescriptor;
 import org.napile.compiler.lang.types.DeferredType;
 import org.napile.compiler.lang.types.ErrorUtils;
 import org.napile.compiler.lang.types.JetType;
 import org.napile.compiler.lang.types.TypeSubstitutor;
 import org.napile.compiler.lang.types.TypeUtils;
 import org.napile.compiler.lang.types.checker.JetTypeChecker;
 import org.napile.compiler.lang.types.expressions.ExpressionTypingServices;
 import org.napile.compiler.lang.types.lang.JetStandardClasses;
 import org.jetbrains.jet.lang.types.lang.rt.NapileLangPackage;
 import org.napile.compiler.lexer.JetTokens;
 import org.napile.compiler.util.lazy.LazyValue;
 import org.napile.compiler.util.lazy.LazyValueWithDefault;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.util.PsiTreeUtil;
 
 /**
  * @author abreslav
  */
 public class DescriptorResolver
 {
 	@NotNull
 	private TypeResolver typeResolver;
 	@NotNull
 	private AnnotationResolver annotationResolver;
 	@NotNull
 	private ExpressionTypingServices expressionTypingServices;
 
 	@Inject
 	public void setTypeResolver(@NotNull TypeResolver typeResolver)
 	{
 		this.typeResolver = typeResolver;
 	}
 
 	@Inject
 	public void setAnnotationResolver(@NotNull AnnotationResolver annotationResolver)
 	{
 		this.annotationResolver = annotationResolver;
 	}
 
 	@Inject
 	public void setExpressionTypingServices(@NotNull ExpressionTypingServices expressionTypingServices)
 	{
 		this.expressionTypingServices = expressionTypingServices;
 	}
 
 
 	public void resolveMutableClassDescriptor(@NotNull NapileClass classElement, @NotNull MutableClassDescriptor descriptor, BindingTrace trace)
 	{
 		// TODO : Where-clause
 		List<TypeParameterDescriptor> typeParameters = Lists.newArrayList();
 		int index = 0;
 		for(NapileTypeParameter typeParameter : classElement.getTypeParameters())
 		{
 			TypeParameterDescriptor typeParameterDescriptor = TypeParameterDescriptorImpl.createForFurtherModification(descriptor, annotationResolver.createAnnotationStubs(typeParameter.getModifierList(), trace), typeParameter.hasModifier(JetTokens.REIFIED_KEYWORD), NapilePsiUtil.safeName(typeParameter.getName()), index);
 			trace.record(BindingContext.TYPE_PARAMETER, typeParameter, typeParameterDescriptor);
 			typeParameters.add(typeParameterDescriptor);
 			index++;
 		}
 		descriptor.setTypeParameterDescriptors(typeParameters);
 
 		descriptor.setModality(resolveModalityFromModifiers(classElement.getModifierList(), Modality.OPEN));
 		descriptor.setVisibility(resolveVisibilityFromModifiers(classElement.getModifierList()));
 
 		trace.record(BindingContext.CLASS, classElement, descriptor);
 	}
 
 	public void resolveSupertypesForMutableClassDescriptor(@NotNull NapileClassOrObject jetClass, @NotNull MutableClassDescriptor descriptor, BindingTrace trace)
 	{
 		for(JetType supertype : resolveSupertypes(descriptor.getScopeForSupertypeResolution(), jetClass, trace))
 		{
 			descriptor.addSupertype(supertype);
 		}
 	}
 
 	public List<JetType> resolveSupertypes(@NotNull JetScope scope, @NotNull NapileClassOrObject jetClass, BindingTrace trace)
 	{
		if(jetClass.getFqName().equals(NapileLangPackage.ANY))  // master object dont have super classes
 			return Collections.emptyList();
 
 		List<JetType> result = Lists.newArrayList();
 		List<NapileDelegationSpecifier> delegationSpecifiers = jetClass.getDelegationSpecifiers();
 		if(delegationSpecifiers.isEmpty())
 		{
 			result.add(getDefaultSupertype(scope, jetClass, trace));
 		}
 		else
 		{
 			Collection<JetType> supertypes = resolveDelegationSpecifiers(scope, delegationSpecifiers, typeResolver, trace, false);
 			for(JetType supertype : supertypes)
 			{
 				result.add(supertype);
 			}
 		}
 		return result;
 	}
 
 	private JetType getDefaultSupertype(JetScope jetScope, NapileClassOrObject jetClass, BindingTrace trace)
 	{
 		// TODO : beautify
 		if(jetClass instanceof NapileEnumEntry)
 		{
 			NapileClassOrObject parent = PsiTreeUtil.getParentOfType(jetClass, NapileClassOrObject.class);
 			ClassDescriptor parentDescriptor = trace.getBindingContext().get(BindingContext.CLASS, parent);
 			if(parentDescriptor.getTypeConstructor().getParameters().isEmpty())
 			{
 				return parentDescriptor.getDefaultType();
 			}
 			else
 			{
 				trace.report(NO_GENERICS_IN_SUPERTYPE_SPECIFIER.on(jetClass.getNameIdentifier()));
 				return ErrorUtils.createErrorType("Supertype not specified");
 			}
 		}
 		return TypeUtils.getTypeOfClassOrErrorType(jetScope, NapileLangPackage.ANY, false);
 	}
 
 	public Collection<JetType> resolveDelegationSpecifiers(JetScope extensibleScope, List<NapileDelegationSpecifier> delegationSpecifiers, @NotNull TypeResolver resolver, BindingTrace trace, boolean checkBounds)
 	{
 		if(delegationSpecifiers.isEmpty())
 		{
 			return Collections.emptyList();
 		}
 		Collection<JetType> result = Lists.newArrayList();
 		for(NapileDelegationSpecifier delegationSpecifier : delegationSpecifiers)
 		{
 			NapileTypeReference typeReference = delegationSpecifier.getTypeReference();
 			if(typeReference != null)
 			{
 				result.add(resolver.resolveType(extensibleScope, typeReference, trace, checkBounds));
 				NapileTypeElement typeElement = typeReference.getTypeElement();
 				while(typeElement instanceof NapileNullableType)
 				{
 					NapileNullableType nullableType = (NapileNullableType) typeElement;
 					trace.report(NULLABLE_SUPERTYPE.on(nullableType));
 					typeElement = nullableType.getInnerType();
 				}
 			}
 			else
 			{
 				result.add(ErrorUtils.createErrorType("No type reference"));
 			}
 		}
 		return result;
 	}
 
 	@NotNull
 	public SimpleFunctionDescriptor resolveFunctionDescriptor(DeclarationDescriptor containingDescriptor, final JetScope scope, final NapileNamedFunction function, final BindingTrace trace)
 	{
 		NapileModifierList modifierList = function.getModifierList();
 		final SimpleFunctionDescriptorImpl functionDescriptor = new SimpleFunctionDescriptorImpl(containingDescriptor, annotationResolver.resolveAnnotations(scope, function.getModifierList(), trace), NapilePsiUtil.safeName(function.getName()), CallableMemberDescriptor.Kind.DECLARATION, modifierList != null && modifierList.hasModifier(JetTokens.STATIC_KEYWORD));
 		WritableScope innerScope = new WritableScopeImpl(scope, functionDescriptor, new TraceBasedRedeclarationHandler(trace), "Function descriptor header scope");
 		innerScope.addLabeledDeclaration(functionDescriptor);
 
 		List<TypeParameterDescriptorImpl> typeParameterDescriptors = resolveTypeParameters(functionDescriptor, innerScope, function.getTypeParameters(), trace);
 		innerScope.changeLockLevel(WritableScope.LockLevel.BOTH);
 		resolveGenericBounds(function, innerScope, typeParameterDescriptors, trace);
 
 		JetType receiverType = null;
 		NapileTypeReference receiverTypeRef = function.getReceiverTypeRef();
 		if(receiverTypeRef != null)
 		{
 			JetScope scopeForReceiver = function.hasTypeParameterListBeforeFunctionName() ? innerScope : scope;
 			receiverType = typeResolver.resolveType(scopeForReceiver, receiverTypeRef, trace, true);
 		}
 
 		List<ValueParameterDescriptor> valueParameterDescriptors = resolveValueParameters(functionDescriptor, innerScope, function.getValueParameters(), trace);
 
 		innerScope.changeLockLevel(WritableScope.LockLevel.READING);
 
 		NapileTypeReference returnTypeRef = function.getReturnTypeRef();
 		JetType returnType;
 		if(returnTypeRef != null)
 		{
 			returnType = typeResolver.resolveType(innerScope, returnTypeRef, trace, true);
 		}
 		else if(function.hasBlockBody())
 		{
 			returnType = JetStandardClasses.getUnitType();
 		}
 		else
 		{
 			final NapileExpression bodyExpression = function.getBodyExpression();
 			if(bodyExpression != null)
 			{
 				returnType = DeferredType.create(trace, new LazyValueWithDefault<JetType>(ErrorUtils.createErrorType("Recursive dependency"))
 				{
 					@Override
 					protected JetType compute()
 					{
 						//JetFlowInformationProvider flowInformationProvider = computeFlowData(function, bodyExpression);
 						return expressionTypingServices.inferFunctionReturnType(scope, function, functionDescriptor, trace);
 					}
 				});
 			}
 			else
 			{
 				returnType = ErrorUtils.createErrorType("No type, no body");
 			}
 		}
 		boolean hasBody = function.getBodyExpression() != null;
 		Modality modality = resolveModalityFromModifiers(function.getModifierList(), Modality.OPEN);
 		Visibility visibility = resolveVisibilityFromModifiers(function.getModifierList());
 
 		functionDescriptor.initialize(receiverType, DescriptorUtils.getExpectedThisObjectIfNeeded(containingDescriptor), typeParameterDescriptors, valueParameterDescriptors, returnType, modality, visibility);
 
 		BindingContextUtils.recordFunctionDeclarationToDescriptor(trace, function, functionDescriptor);
 		return functionDescriptor;
 	}
 
 	@NotNull
 	private List<ValueParameterDescriptor> resolveValueParameters(FunctionDescriptor functionDescriptor, WritableScope parameterScope, List<NapileParameter> valueParameters, BindingTrace trace)
 	{
 		List<ValueParameterDescriptor> result = new ArrayList<ValueParameterDescriptor>();
 		for(int i = 0, valueParametersSize = valueParameters.size(); i < valueParametersSize; i++)
 		{
 			NapileParameter valueParameter = valueParameters.get(i);
 			NapileTypeReference typeReference = valueParameter.getTypeReference();
 
 			JetType type;
 			if(typeReference == null)
 			{
 				trace.report(VALUE_PARAMETER_WITH_NO_TYPE_ANNOTATION.on(valueParameter));
 				type = ErrorUtils.createErrorType("Type annotation was missing");
 			}
 			else
 			{
 				type = typeResolver.resolveType(parameterScope, typeReference, trace, true);
 			}
 
 			ValueParameterDescriptor valueParameterDescriptor = resolveValueParameterDescriptor(parameterScope, functionDescriptor, valueParameter, i, type, trace);
 			parameterScope.addVariableDescriptor(valueParameterDescriptor);
 			result.add(valueParameterDescriptor);
 		}
 		return result;
 	}
 
 	@NotNull
 	public MutableValueParameterDescriptor resolveValueParameterDescriptor(JetScope scope, DeclarationDescriptor declarationDescriptor, NapileParameter valueParameter, int index, JetType type, BindingTrace trace)
 	{
 		JetType varargElementType = null;
 		JetType variableType = type;
 
 		MutableValueParameterDescriptor valueParameterDescriptor = new ValueParameterDescriptorImpl(declarationDescriptor, index, annotationResolver.resolveAnnotations(scope, valueParameter.getModifierList(), trace), NapilePsiUtil.safeName(valueParameter.getName()), valueParameter.isMutable(), variableType, valueParameter.getDefaultValue() != null, varargElementType);
 
 		trace.record(BindingContext.VALUE_PARAMETER, valueParameter, valueParameterDescriptor);
 		return valueParameterDescriptor;
 	}
 
 	public List<TypeParameterDescriptorImpl> resolveTypeParameters(DeclarationDescriptor containingDescriptor, WritableScope extensibleScope, List<NapileTypeParameter> typeParameters, BindingTrace trace)
 	{
 		List<TypeParameterDescriptorImpl> result = new ArrayList<TypeParameterDescriptorImpl>();
 		for(int i = 0, typeParametersSize = typeParameters.size(); i < typeParametersSize; i++)
 		{
 			NapileTypeParameter typeParameter = typeParameters.get(i);
 			result.add(resolveTypeParameter(containingDescriptor, extensibleScope, typeParameter, i, trace));
 		}
 		return result;
 	}
 
 	private TypeParameterDescriptorImpl resolveTypeParameter(DeclarationDescriptor containingDescriptor, WritableScope extensibleScope, NapileTypeParameter typeParameter, int index, BindingTrace trace)
 	{
 		//        NapileTypeReference extendsBound = typeParameter.getExtendsBound();
 		//        JetType bound = extendsBound == null
 		//                ? JetStandardClasses.getDefaultBound()
 		//                : typeResolver.resolveType(extensibleScope, extendsBound);
 		TypeParameterDescriptorImpl typeParameterDescriptor = TypeParameterDescriptorImpl.createForFurtherModification(containingDescriptor, annotationResolver.createAnnotationStubs(typeParameter.getModifierList(), trace), typeParameter.hasModifier(JetTokens.REIFIED_KEYWORD), NapilePsiUtil.safeName(typeParameter.getName()), index);
 		//        typeParameterDescriptor.addUpperBound(bound);
 		extensibleScope.addTypeParameterDescriptor(typeParameterDescriptor);
 		trace.record(BindingContext.TYPE_PARAMETER, typeParameter, typeParameterDescriptor);
 		return typeParameterDescriptor;
 	}
 
 	public static ConstructorDescriptor createConstructorForObject(@Nullable PsiElement object, @NotNull ClassDescriptor classDescriptor, @NotNull BindingTrace trace)
 	{
 		ConstructorDescriptor constructorDescriptor = new ConstructorDescriptor(classDescriptor, Collections.<AnnotationDescriptor>emptyList());
 
 		// TODO : make the constructor private?
 		// TODO check set classDescriptor.getVisibility()
 		constructorDescriptor.initialize(Collections.<TypeParameterDescriptor>emptyList(), Collections.<ValueParameterDescriptor>emptyList(), Visibilities.PUBLIC);
 
 		if(object != null)
 		{
 			try
 			{
 				trace.record(CONSTRUCTOR, object, constructorDescriptor);
 			}
 			catch(RuntimeException e)
 			{
 				throw new RuntimeException(e.getMessage() + " at " + DiagnosticUtils.atLocation(object), e);
 			}
 		}
 		return constructorDescriptor;
 	}
 
 	final class UpperBoundCheckerTask
 	{
 		NapileTypeReference upperBound;
 		JetType upperBoundType;
 		boolean isClassObjectConstraint;
 
 		private UpperBoundCheckerTask(NapileTypeReference upperBound, JetType upperBoundType, boolean classObjectConstraint)
 		{
 			this.upperBound = upperBound;
 			this.upperBoundType = upperBoundType;
 			isClassObjectConstraint = classObjectConstraint;
 		}
 	}
 
 	public void resolveGenericBounds(@NotNull NapileTypeParameterListOwner declaration, JetScope scope, List<TypeParameterDescriptorImpl> parameters, BindingTrace trace)
 	{
 		List<UpperBoundCheckerTask> deferredUpperBoundCheckerTasks = Lists.newArrayList();
 
 		List<NapileTypeParameter> typeParameters = declaration.getTypeParameters();
 		Map<Name, TypeParameterDescriptorImpl> parameterByName = Maps.newHashMap();
 		for(int i = 0; i < typeParameters.size(); i++)
 		{
 			NapileTypeParameter jetTypeParameter = typeParameters.get(i);
 			TypeParameterDescriptorImpl typeParameterDescriptor = parameters.get(i);
 
 			parameterByName.put(typeParameterDescriptor.getName(), typeParameterDescriptor);
 
 			NapileTypeReference extendsBound = jetTypeParameter.getExtendsBound();
 			if(extendsBound != null)
 			{
 				JetType type = typeResolver.resolveType(scope, extendsBound, trace, false);
 				typeParameterDescriptor.addUpperBound(type);
 				deferredUpperBoundCheckerTasks.add(new UpperBoundCheckerTask(extendsBound, type, false));
 			}
 		}
 		for(NapileTypeConstraint constraint : declaration.getTypeConstraints())
 		{
 			NapileSimpleNameExpression subjectTypeParameterName = constraint.getSubjectTypeParameterName();
 			if(subjectTypeParameterName == null)
 			{
 				continue;
 			}
 			Name referencedName = subjectTypeParameterName.getReferencedNameAsName();
 			if(referencedName == null)
 			{
 				continue;
 			}
 			TypeParameterDescriptorImpl typeParameterDescriptor = parameterByName.get(referencedName);
 			NapileTypeReference boundTypeReference = constraint.getBoundTypeReference();
 			JetType bound = null;
 			if(boundTypeReference != null)
 			{
 				bound = typeResolver.resolveType(scope, boundTypeReference, trace, false);
 				deferredUpperBoundCheckerTasks.add(new UpperBoundCheckerTask(boundTypeReference, bound, constraint.isClassObjectContraint()));
 			}
 
 			if(typeParameterDescriptor == null)
 			{
 				// To tell the user that we look only for locally defined type parameters
 				ClassifierDescriptor classifier = scope.getClassifier(referencedName);
 				if(classifier != null)
 				{
 					trace.report(NAME_IN_CONSTRAINT_IS_NOT_A_TYPE_PARAMETER.on(subjectTypeParameterName, constraint, declaration));
 					trace.record(BindingContext.REFERENCE_TARGET, subjectTypeParameterName, classifier);
 				}
 				else
 				{
 					trace.report(UNRESOLVED_REFERENCE.on(subjectTypeParameterName));
 				}
 			}
 			else
 			{
 				trace.record(BindingContext.REFERENCE_TARGET, subjectTypeParameterName, typeParameterDescriptor);
 				if(bound != null)
 				{
 					if(constraint.isClassObjectContraint())
 					{
 						typeParameterDescriptor.addClassObjectBound(bound);
 					}
 					else
 					{
 						typeParameterDescriptor.addUpperBound(bound);
 					}
 				}
 			}
 		}
 
 		for(TypeParameterDescriptorImpl parameter : parameters)
 		{
 			parameter.addDefaultUpperBound(scope);
 
 			parameter.setInitialized();
 
 			if(JetStandardClasses.isNothing(parameter.getUpperBoundsAsType()))
 			{
 				PsiElement nameIdentifier = typeParameters.get(parameter.getIndex()).getNameIdentifier();
 				if(nameIdentifier != null)
 				{
 					trace.report(CONFLICTING_UPPER_BOUNDS.on(nameIdentifier, parameter));
 				}
 			}
 
 			JetType classObjectType = parameter.getClassObjectType();
 			if(classObjectType != null && JetStandardClasses.isNothing(classObjectType))
 			{
 				PsiElement nameIdentifier = typeParameters.get(parameter.getIndex()).getNameIdentifier();
 				if(nameIdentifier != null)
 				{
 					trace.report(CONFLICTING_CLASS_OBJECT_UPPER_BOUNDS.on(nameIdentifier, parameter));
 				}
 			}
 		}
 
 		for(UpperBoundCheckerTask checkerTask : deferredUpperBoundCheckerTasks)
 		{
 			checkUpperBoundType(checkerTask.upperBound, checkerTask.upperBoundType, checkerTask.isClassObjectConstraint, trace);
 		}
 	}
 
 	private static void checkUpperBoundType(NapileTypeReference upperBound, JetType upperBoundType, boolean isClassObjectConstraint, BindingTrace trace)
 	{
 		if(!TypeUtils.canHaveSubtypes(JetTypeChecker.INSTANCE, upperBoundType))
 		{
 			if(isClassObjectConstraint)
 			{
 				trace.report(FINAL_CLASS_OBJECT_UPPER_BOUND.on(upperBound, upperBoundType));
 			}
 			else
 			{
 				trace.report(FINAL_UPPER_BOUND.on(upperBound, upperBoundType));
 			}
 		}
 	}
 
 	@NotNull
 	public VariableDescriptor resolveLocalVariableDescriptor(@NotNull DeclarationDescriptor containingDeclaration, @NotNull JetScope scope, @NotNull NapileParameter parameter, BindingTrace trace)
 	{
 		JetType type = resolveParameterType(scope, parameter, trace);
 		return resolveLocalVariableDescriptor(containingDeclaration, parameter, type, trace);
 	}
 
 	private JetType resolveParameterType(JetScope scope, NapileParameter parameter, BindingTrace trace)
 	{
 		NapileTypeReference typeReference = parameter.getTypeReference();
 		JetType type;
 		if(typeReference != null)
 		{
 			type = typeResolver.resolveType(scope, typeReference, trace, true);
 		}
 		else
 		{
 			// Error is reported by the parser
 			type = ErrorUtils.createErrorType("Annotation is absent");
 		}
 
 		return type;
 	}
 
 	public VariableDescriptor resolveLocalVariableDescriptor(@NotNull DeclarationDescriptor containingDeclaration, @NotNull NapileParameter parameter, @NotNull JetType type, BindingTrace trace)
 	{
 		VariableDescriptor variableDescriptor = new LocalVariableDescriptor(containingDeclaration, annotationResolver.createAnnotationStubs(parameter.getModifierList(), trace), NapilePsiUtil.safeName(parameter.getName()), type, parameter.isMutable());
 		trace.record(BindingContext.VALUE_PARAMETER, parameter, variableDescriptor);
 		return variableDescriptor;
 	}
 
 	@NotNull
 	public VariableDescriptor resolveLocalVariableDescriptor(DeclarationDescriptor containingDeclaration, JetScope scope, NapileProperty property, DataFlowInfo dataFlowInfo, BindingTrace trace)
 	{
 		VariableDescriptorImpl variableDescriptor = resolveLocalVariableDescriptorWithType(containingDeclaration, property, null, trace);
 
 		JetType type = getVariableType(scope, property, dataFlowInfo, false, trace); // For a local variable the type must not be deferred
 		variableDescriptor.setOutType(type);
 		return variableDescriptor;
 	}
 
 	@NotNull
 	public VariableDescriptorImpl resolveLocalVariableDescriptorWithType(DeclarationDescriptor containingDeclaration, NapileProperty property, JetType type, BindingTrace trace)
 	{
 		VariableDescriptorImpl variableDescriptor = new LocalVariableDescriptor(containingDeclaration, annotationResolver.createAnnotationStubs(property.getModifierList(), trace), NapilePsiUtil.safeName(property.getName()), type, property.isVar());
 		trace.record(BindingContext.VARIABLE, property, variableDescriptor);
 		return variableDescriptor;
 	}
 
 	@NotNull
 	public VariableDescriptor resolveObjectDeclaration(@NotNull DeclarationDescriptor containingDeclaration, @NotNull NapileClassOrObject objectDeclaration, @NotNull ClassDescriptor classDescriptor, BindingTrace trace)
 	{
 		boolean isProperty = (containingDeclaration instanceof NamespaceDescriptor) || (containingDeclaration instanceof ClassDescriptor);
 		if(isProperty)
 		{
 			return resolveObjectDeclarationAsPropertyDescriptor(containingDeclaration, objectDeclaration, classDescriptor, trace);
 		}
 		else
 		{
 			return resolveObjectDeclarationAsLocalVariable(containingDeclaration, objectDeclaration, classDescriptor, trace);
 		}
 	}
 
 	@NotNull
 	public PropertyDescriptor resolveObjectDeclarationAsPropertyDescriptor(@NotNull DeclarationDescriptor containingDeclaration, @NotNull NapileClassOrObject objectDeclaration, @NotNull ClassDescriptor classDescriptor, BindingTrace trace)
 	{
 		NapileModifierList modifierList = objectDeclaration.getModifierList();
 		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(containingDeclaration, annotationResolver.createAnnotationStubs(modifierList, trace), Modality.FINAL, resolveVisibilityFromModifiers(modifierList), false, true, NapilePsiUtil.safeName(objectDeclaration.getName()), CallableMemberDescriptor.Kind.DECLARATION, false);
 		propertyDescriptor.setType(classDescriptor.getDefaultType(), Collections.<TypeParameterDescriptor>emptyList(), DescriptorUtils.getExpectedThisObjectIfNeeded(containingDeclaration), ReceiverDescriptor.NO_RECEIVER);
 		propertyDescriptor.initialize(createDefaultGetter(propertyDescriptor), null);
 		NapileObjectDeclarationName nameAsDeclaration = objectDeclaration.getNameAsDeclaration();
 		if(nameAsDeclaration != null)
 		{
 			trace.record(BindingContext.OBJECT_DECLARATION, nameAsDeclaration, propertyDescriptor);
 		}
 		return propertyDescriptor;
 	}
 
 	@NotNull
 	private VariableDescriptor resolveObjectDeclarationAsLocalVariable(@NotNull DeclarationDescriptor containingDeclaration, @NotNull NapileClassOrObject objectDeclaration, @NotNull ClassDescriptor classDescriptor, BindingTrace trace)
 	{
 		VariableDescriptorImpl variableDescriptor = new LocalVariableDescriptor(containingDeclaration, annotationResolver.createAnnotationStubs(objectDeclaration.getModifierList(), trace), NapilePsiUtil.safeName(objectDeclaration.getName()), classDescriptor.getDefaultType(),						/*isVar =*/ false);
 		NapileObjectDeclarationName nameAsDeclaration = objectDeclaration.getNameAsDeclaration();
 		if(nameAsDeclaration != null)
 		{
 			trace.record(BindingContext.VARIABLE, nameAsDeclaration, variableDescriptor);
 		}
 		return variableDescriptor;
 	}
 
 	public JetScope getPropertyDeclarationInnerScope(@NotNull JetScope outerScope, @NotNull List<? extends TypeParameterDescriptor> typeParameters, @NotNull ReceiverDescriptor receiver, BindingTrace trace)
 	{
 		WritableScopeImpl result = new WritableScopeImpl(outerScope, outerScope.getContainingDeclaration(), new TraceBasedRedeclarationHandler(trace), "Property declaration inner scope");
 		for(TypeParameterDescriptor typeParameterDescriptor : typeParameters)
 		{
 			result.addTypeParameterDescriptor(typeParameterDescriptor);
 		}
 		if(receiver.exists())
 		{
 			result.setImplicitReceiver(receiver);
 		}
 		result.changeLockLevel(WritableScope.LockLevel.READING);
 		return result;
 	}
 
 	@NotNull
 	public PropertyDescriptor resolvePropertyDescriptor(@NotNull DeclarationDescriptor containingDeclaration, @NotNull JetScope scope, NapileProperty property, BindingTrace trace)
 	{
 
 		NapileModifierList modifierList = property.getModifierList();
 		boolean isVar = property.isVar();
 
 		boolean hasBody = hasBody(property);
 		Modality defaultModality = Modality.OPEN;
 		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(containingDeclaration, annotationResolver.resolveAnnotations(scope, modifierList, trace), resolveModalityFromModifiers(property.getModifierList(), defaultModality), resolveVisibilityFromModifiers(property.getModifierList()), isVar, false, NapilePsiUtil.safeName(property.getName()), CallableMemberDescriptor.Kind.DECLARATION, modifierList != null && modifierList.hasModifier(JetTokens.STATIC_KEYWORD));
 
 		List<TypeParameterDescriptorImpl> typeParameterDescriptors;
 		JetScope scopeWithTypeParameters;
 		JetType receiverType = null;
 
 		{
 			List<NapileTypeParameter> typeParameters = property.getTypeParameters();
 			if(typeParameters.isEmpty())
 			{
 				scopeWithTypeParameters = scope;
 				typeParameterDescriptors = Collections.emptyList();
 			}
 			else
 			{
 				WritableScope writableScope = new WritableScopeImpl(scope, containingDeclaration, new TraceBasedRedeclarationHandler(trace), "Scope with type parameters of a property");
 				typeParameterDescriptors = resolveTypeParameters(containingDeclaration, writableScope, typeParameters, trace);
 				writableScope.changeLockLevel(WritableScope.LockLevel.READING);
 				resolveGenericBounds(property, writableScope, typeParameterDescriptors, trace);
 				scopeWithTypeParameters = writableScope;
 			}
 
 			NapileTypeReference receiverTypeRef = property.getReceiverTypeRef();
 			if(receiverTypeRef != null)
 			{
 				receiverType = typeResolver.resolveType(scopeWithTypeParameters, receiverTypeRef, trace, true);
 			}
 		}
 
 		ReceiverDescriptor receiverDescriptor = receiverType == null ? ReceiverDescriptor.NO_RECEIVER : new ExtensionReceiver(propertyDescriptor, receiverType);
 
 		JetScope propertyScope = getPropertyDeclarationInnerScope(scope, typeParameterDescriptors, ReceiverDescriptor.NO_RECEIVER, trace);
 
 		JetType type = getVariableType(propertyScope, property, DataFlowInfo.EMPTY, true, trace);
 
 		propertyDescriptor.setType(type, typeParameterDescriptors, DescriptorUtils.getExpectedThisObjectIfNeeded(containingDeclaration), receiverDescriptor);
 
 		PropertyGetterDescriptor getter = resolvePropertyGetterDescriptor(scopeWithTypeParameters, property, propertyDescriptor, trace);
 		PropertySetterDescriptor setter = resolvePropertySetterDescriptor(scopeWithTypeParameters, property, propertyDescriptor, trace);
 
 		propertyDescriptor.initialize(getter, setter);
 
 		trace.record(BindingContext.VARIABLE, property, propertyDescriptor);
 		return propertyDescriptor;
 	}
 
 	/*package*/
 	static boolean hasBody(NapileProperty property)
 	{
 		boolean hasBody = property.getInitializer() != null;
 		if(!hasBody)
 		{
 			NapilePropertyAccessor getter = property.getGetter();
 			if(getter != null && getter.getBodyExpression() != null)
 			{
 				hasBody = true;
 			}
 			NapilePropertyAccessor setter = property.getSetter();
 			if(!hasBody && setter != null && setter.getBodyExpression() != null)
 			{
 				hasBody = true;
 			}
 		}
 		return hasBody;
 	}
 
 	@NotNull
 	private JetType getVariableType(@NotNull final JetScope scope, @NotNull final NapileProperty property, @NotNull final DataFlowInfo dataFlowInfo, boolean allowDeferred, final BindingTrace trace)
 	{
 		// TODO : receiver?
 		NapileTypeReference propertyTypeRef = property.getPropertyTypeRef();
 
 		if(propertyTypeRef == null)
 		{
 			final NapileExpression initializer = property.getInitializer();
 			if(initializer == null)
 			{
 				return ErrorUtils.createErrorType("No type, no body");
 			}
 			else
 			{
 				// TODO : a risk of a memory leak
 				LazyValue<JetType> lazyValue = new LazyValueWithDefault<JetType>(ErrorUtils.createErrorType("Recursive dependency"))
 				{
 					@Override
 					protected JetType compute()
 					{
 						return expressionTypingServices.safeGetType(scope, initializer, TypeUtils.NO_EXPECTED_TYPE, dataFlowInfo, trace);
 					}
 				};
 				if(allowDeferred)
 				{
 					return DeferredType.create(trace, lazyValue);
 				}
 				else
 				{
 					return lazyValue.get();
 				}
 			}
 		}
 		else
 		{
 			return typeResolver.resolveType(scope, propertyTypeRef, trace, true);
 		}
 	}
 
 	@NotNull
 	public static Modality resolveModalityFromModifiers(@Nullable NapileModifierList modifierList, @NotNull Modality defaultModality)
 	{
 		if(modifierList == null)
 			return defaultModality;
 		boolean hasAbstractModifier = modifierList.hasModifier(JetTokens.ABSTRACT_KEYWORD);
 		boolean hasOverrideModifier = modifierList.hasModifier(JetTokens.OVERRIDE_KEYWORD);
 
 		if(hasAbstractModifier)
 		{
 			return Modality.ABSTRACT;
 		}
 		boolean hasFinalModifier = modifierList.hasModifier(JetTokens.FINAL_KEYWORD);
 		if(hasOverrideModifier && !hasFinalModifier && !(defaultModality == Modality.ABSTRACT))
 		{
 			return Modality.OPEN;
 		}
 		if(hasFinalModifier)
 		{
 			return Modality.FINAL;
 		}
 		return defaultModality;
 	}
 
 	@NotNull
 	public static Visibility resolveVisibilityFromModifiers(@Nullable NapileModifierList modifierList)
 	{
 		if(modifierList == null)
 			return Visibilities.PUBLIC;
 		if(modifierList.hasModifier(JetTokens.LOCAL_KEYWORD))
 			return Visibilities.LOCAL;
 		if(modifierList.hasModifier(JetTokens.COVERED_KEYWORD))
 			return Visibilities.COVERED;
 		if(modifierList.hasModifier(JetTokens.HERITABLE_KEYWORD))
 			return Visibilities.HERITABLE;
 		return Visibilities.PUBLIC;
 	}
 
 	@Nullable
 	private PropertySetterDescriptor resolvePropertySetterDescriptor(@NotNull JetScope scope, @NotNull NapileProperty property, @NotNull PropertyDescriptor propertyDescriptor, BindingTrace trace)
 	{
 		NapilePropertyAccessor setter = property.getSetter();
 		PropertySetterDescriptor setterDescriptor = null;
 		if(setter != null)
 		{
 			List<AnnotationDescriptor> annotations = annotationResolver.resolveAnnotations(scope, setter.getModifierList(), trace);
 			NapileParameter parameter = setter.getParameter();
 
 			setterDescriptor = new PropertySetterDescriptor(propertyDescriptor, annotations, resolveModalityFromModifiers(setter.getModifierList(), propertyDescriptor.getModality()), resolveVisibilityFromModifiers(setter.getModifierList()), setter.getBodyExpression() != null, false, CallableMemberDescriptor.Kind.DECLARATION, propertyDescriptor.isStatic());
 			if(parameter != null)
 			{
 
 				// This check is redundant: the parser does not allow a default value, but we'll keep it just in case
 				NapileExpression defaultValue = parameter.getDefaultValue();
 				if(defaultValue != null)
 				{
 					trace.report(SETTER_PARAMETER_WITH_DEFAULT_VALUE.on(defaultValue));
 				}
 
 				JetType type;
 				NapileTypeReference typeReference = parameter.getTypeReference();
 				if(typeReference == null)
 				{
 					type = propertyDescriptor.getType(); // TODO : this maybe unknown at this point
 				}
 				else
 				{
 					type = typeResolver.resolveType(scope, typeReference, trace, true);
 					JetType inType = propertyDescriptor.getType();
 					if(inType != null)
 					{
 						if(!TypeUtils.equalTypes(type, inType))
 						{
 							trace.report(WRONG_SETTER_PARAMETER_TYPE.on(typeReference, inType, type));
 						}
 					}
 					else
 					{
 						// TODO : the same check may be needed later???
 					}
 				}
 
 				MutableValueParameterDescriptor valueParameterDescriptor = resolveValueParameterDescriptor(scope, setterDescriptor, parameter, 0, type, trace);
 				setterDescriptor.initialize(valueParameterDescriptor);
 			}
 			else
 			{
 				setterDescriptor.initializeDefault();
 			}
 
 			trace.record(BindingContext.PROPERTY_ACCESSOR, setter, setterDescriptor);
 		}
 		else if(property.isVar())
 		{
 			setterDescriptor = createDefaultSetter(propertyDescriptor);
 		}
 
 		if(!property.isVar())
 		{
 			if(setter != null)
 			{
 				//                trace.getErrorHandler().genericError(setter.asElement().getNode(), "A 'val'-property cannot have a setter");
 				trace.report(VAL_WITH_SETTER.on(setter));
 			}
 		}
 		return setterDescriptor;
 	}
 
 	private PropertySetterDescriptor createDefaultSetter(PropertyDescriptor propertyDescriptor)
 	{
 		PropertySetterDescriptor setterDescriptor;
 		setterDescriptor = new PropertySetterDescriptor(propertyDescriptor, Collections.<AnnotationDescriptor>emptyList(), propertyDescriptor.getModality(), propertyDescriptor.getVisibility(), false, true, CallableMemberDescriptor.Kind.DECLARATION, propertyDescriptor.isStatic());
 		setterDescriptor.initializeDefault();
 		return setterDescriptor;
 	}
 
 	@Nullable
 	private PropertyGetterDescriptor resolvePropertyGetterDescriptor(@NotNull JetScope scope, @NotNull NapileProperty property, @NotNull PropertyDescriptor propertyDescriptor, BindingTrace trace)
 	{
 		PropertyGetterDescriptor getterDescriptor;
 		NapilePropertyAccessor getter = property.getGetter();
 		if(getter != null)
 		{
 			List<AnnotationDescriptor> annotations = annotationResolver.resolveAnnotations(scope, getter.getModifierList(), trace);
 
 			JetType outType = propertyDescriptor.getType();
 			JetType returnType = outType;
 			NapileTypeReference returnTypeReference = getter.getReturnTypeReference();
 			if(returnTypeReference != null)
 			{
 				returnType = typeResolver.resolveType(scope, returnTypeReference, trace, true);
 				if(outType != null && !TypeUtils.equalTypes(returnType, outType))
 				{
 					trace.report(WRONG_GETTER_RETURN_TYPE.on(returnTypeReference, propertyDescriptor.getReturnType(), outType));
 				}
 			}
 
 			getterDescriptor = new PropertyGetterDescriptor(propertyDescriptor, annotations, resolveModalityFromModifiers(getter.getModifierList(), propertyDescriptor.getModality()), resolveVisibilityFromModifiers(getter.getModifierList()), getter.getBodyExpression() != null, false, CallableMemberDescriptor.Kind.DECLARATION, propertyDescriptor.isStatic());
 			getterDescriptor.initialize(returnType);
 			trace.record(BindingContext.PROPERTY_ACCESSOR, getter, getterDescriptor);
 		}
 		else
 		{
 			getterDescriptor = createDefaultGetter(propertyDescriptor);
 			getterDescriptor.initialize(propertyDescriptor.getType());
 		}
 		return getterDescriptor;
 	}
 
 	public static PropertyGetterDescriptor createDefaultGetter(PropertyDescriptor propertyDescriptor)
 	{
 		PropertyGetterDescriptor getterDescriptor;
 		getterDescriptor = new PropertyGetterDescriptor(propertyDescriptor, Collections.<AnnotationDescriptor>emptyList(), propertyDescriptor.getModality(), propertyDescriptor.getVisibility(), false, true, CallableMemberDescriptor.Kind.DECLARATION, propertyDescriptor.isStatic());
 		return getterDescriptor;
 	}
 
 	@NotNull
 	public ConstructorDescriptor resolveConstructorDescriptor(@NotNull JetScope scope, @NotNull ClassDescriptor classDescriptor, @NotNull NapileConstructor constructor, BindingTrace trace)
 	{
 		NapileModifierList modifierList = constructor.getModifierList();
 		ConstructorDescriptor constructorDescriptor = new ConstructorDescriptor(classDescriptor, annotationResolver.resolveAnnotations(scope, modifierList, trace));
 		trace.record(BindingContext.CONSTRUCTOR, constructor, constructorDescriptor);
 		WritableScopeImpl parameterScope = new WritableScopeImpl(scope, constructorDescriptor, new TraceBasedRedeclarationHandler(trace), "Scope with value parameters of a constructor");
 		parameterScope.changeLockLevel(WritableScope.LockLevel.BOTH);
 
 		resolveDelegationSpecifiers(scope, constructor.getDelegationSpecifiers(), typeResolver, trace, true);
 
 		return constructorDescriptor.initialize(classDescriptor.getTypeConstructor().getParameters(), resolveValueParameters(constructorDescriptor, parameterScope, constructor.getValueParameters(), trace), resolveVisibilityFromModifiers(modifierList));
 	}
 
 	public static void checkBounds(@NotNull NapileTypeReference typeReference, @NotNull JetType type, BindingTrace trace)
 	{
 		if(ErrorUtils.isErrorType(type))
 			return;
 
 		NapileTypeElement typeElement = typeReference.getTypeElement();
 		if(typeElement == null)
 			return;
 
 		List<TypeParameterDescriptor> parameters = type.getConstructor().getParameters();
 		List<JetType> arguments = type.getArguments();
 		assert parameters.size() == arguments.size();
 
 		List<NapileTypeReference> jetTypeArguments = typeElement.getTypeArguments();
 		assert jetTypeArguments.size() == arguments.size() : typeElement.getText();
 
 		TypeSubstitutor substitutor = TypeSubstitutor.create(type);
 		for(int i = 0; i < jetTypeArguments.size(); i++)
 		{
 			NapileTypeReference jetTypeArgument = jetTypeArguments.get(i);
 
 			if(jetTypeArgument == null)
 				continue;
 
 			JetType typeArgument = arguments.get(i);
 			checkBounds(jetTypeArgument, typeArgument, trace);
 
 			TypeParameterDescriptor typeParameterDescriptor = parameters.get(i);
 			checkBounds(jetTypeArgument, typeArgument, typeParameterDescriptor, substitutor, trace);
 		}
 	}
 
 	public static void checkBounds(@NotNull NapileTypeReference jetTypeArgument, @NotNull JetType typeArgument, @NotNull TypeParameterDescriptor typeParameterDescriptor, @NotNull TypeSubstitutor substitutor, BindingTrace trace)
 	{
 		for(JetType bound : typeParameterDescriptor.getUpperBounds())
 		{
 			JetType substitutedBound = substitutor.safeSubstitute(bound);
 			if(!JetTypeChecker.INSTANCE.isSubtypeOf(typeArgument, substitutedBound))
 			{
 				trace.report(UPPER_BOUND_VIOLATED.on(jetTypeArgument, substitutedBound, typeArgument));
 			}
 		}
 	}
 }
