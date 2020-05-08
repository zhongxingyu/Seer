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
 
 package org.napile.compiler.lang.cfg;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.compiler.lang.cfg.PseudocodeVariablesData.VariableInitState;
 import org.napile.compiler.lang.cfg.PseudocodeVariablesData.VariableUseState;
 import org.napile.compiler.lang.cfg.pseudocode.*;
 import org.napile.compiler.lang.descriptors.ClassDescriptor;
 import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
 import org.napile.compiler.lang.descriptors.MethodDescriptor;
 import org.napile.compiler.lang.descriptors.Modality;
 import org.napile.compiler.lang.descriptors.PropertyDescriptor;
 import org.napile.compiler.lang.descriptors.PropertyKind;
 import org.napile.compiler.lang.descriptors.ParameterDescriptor;
 import org.napile.compiler.lang.descriptors.VariableDescriptor;
 import org.napile.compiler.lang.diagnostics.Errors;
 import org.napile.compiler.lang.psi.*;
 import org.napile.compiler.lang.resolve.BindingContext;
 import org.napile.compiler.lang.resolve.BindingContextUtils;
 import org.napile.compiler.lang.resolve.BindingTrace;
 import org.napile.compiler.lang.resolve.DescriptorUtils;
 import org.napile.compiler.lang.types.JetType;
 import org.napile.compiler.lang.types.TypeUtils;
 import org.napile.compiler.lang.types.lang.JetStandardClasses;
 import org.napile.compiler.lexer.JetTokens;
 import org.napile.compiler.plugin.JetMainDetector;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.psi.util.PsiTreeUtil;
 
 /**
  * @author svtk
  */
 public class JetFlowInformationProvider
 {
 
 	private final NapileDeclaration subroutine;
 	private final Pseudocode pseudocode;
 	private final PseudocodeVariablesData pseudocodeVariablesData;
 	private BindingTrace trace;
 
 	public JetFlowInformationProvider(@NotNull NapileDeclaration declaration, @NotNull BindingTrace trace)
 	{
 
 		subroutine = declaration;
 		this.trace = trace;
 		pseudocode = new JetControlFlowProcessor(trace).generatePseudocode(declaration);
 		pseudocodeVariablesData = new PseudocodeVariablesData(pseudocode, trace.getBindingContext());
 	}
 
 	private void collectReturnExpressions(@NotNull final Collection<NapileElement> returnedExpressions)
 	{
 		final Set<Instruction> instructions = Sets.newHashSet(pseudocode.getInstructions());
 		SubroutineExitInstruction exitInstruction = pseudocode.getExitInstruction();
 		for(Instruction previousInstruction : exitInstruction.getPreviousInstructions())
 		{
 			previousInstruction.accept(new InstructionVisitor()
 			{
 				@Override
 				public void visitReturnValue(ReturnValueInstruction instruction)
 				{
 					if(instructions.contains(instruction))
 					{ //exclude non-local return expressions
 						returnedExpressions.add(instruction.getElement());
 					}
 				}
 
 				@Override
 				public void visitReturnNoValue(ReturnNoValueInstruction instruction)
 				{
 					if(instructions.contains(instruction))
 					{
 						returnedExpressions.add(instruction.getElement());
 					}
 				}
 
 
 				@Override
 				public void visitJump(AbstractJumpInstruction instruction)
 				{
 					// Nothing
 				}
 
 				@Override
 				public void visitUnconditionalJump(UnconditionalJumpInstruction instruction)
 				{
 					redirectToPrevInstructions(instruction);
 				}
 
 				private void redirectToPrevInstructions(Instruction instruction)
 				{
 					for(Instruction previousInstruction : instruction.getPreviousInstructions())
 					{
 						previousInstruction.accept(this);
 					}
 				}
 
 				@Override
 				public void visitNondeterministicJump(NondeterministicJumpInstruction instruction)
 				{
 					redirectToPrevInstructions(instruction);
 				}
 
 				@Override
 				public void visitInstruction(Instruction instruction)
 				{
 					if(instruction instanceof JetElementInstruction)
 					{
 						JetElementInstruction elementInstruction = (JetElementInstruction) instruction;
 						returnedExpressions.add(elementInstruction.getElement());
 					}
 					else
 					{
 						throw new IllegalStateException(instruction + " precedes the exit point");
 					}
 				}
 			});
 		}
 	}
 
 	public void checkDefiniteReturn(final @NotNull JetType expectedReturnType)
 	{
 		assert subroutine instanceof NapileDeclarationWithBody;
 		NapileDeclarationWithBody function = (NapileDeclarationWithBody) subroutine;
 
 		NapileExpression bodyExpression = function.getBodyExpression();
 		if(bodyExpression == null)
 			return;
 
 		List<NapileElement> returnedExpressions = Lists.newArrayList();
 		collectReturnExpressions(returnedExpressions);
 
 		boolean nothingReturned = returnedExpressions.isEmpty();
 
 		returnedExpressions.remove(function); // This will be the only "expression" if the body is empty
 
 		if(expectedReturnType != TypeUtils.NO_EXPECTED_TYPE &&
 				!JetStandardClasses.isUnit(expectedReturnType) &&
 				returnedExpressions.isEmpty() &&
 				!nothingReturned)
 		{
 			trace.report(Errors.RETURN_TYPE_MISMATCH.on(bodyExpression, expectedReturnType));
 		}
 		final boolean blockBody = function.hasBlockBody();
 
 		final Set<NapileElement> rootUnreachableElements = collectUnreachableCode();
 		for(NapileElement element : rootUnreachableElements)
 		{
 			trace.report(Errors.UNREACHABLE_CODE.on(element));
 		}
 
 		final boolean[] noReturnError = new boolean[]{false};
 		for(NapileElement returnedExpression : returnedExpressions)
 		{
 			returnedExpression.accept(new NapileVisitorVoid()
 			{
 				@Override
 				public void visitReturnExpression(NapileReturnExpression expression)
 				{
 					if(!blockBody)
 					{
 						trace.report(Errors.RETURN_IN_FUNCTION_WITH_EXPRESSION_BODY.on(expression));
 					}
 				}
 
 				@Override
 				public void visitExpression(NapileExpression expression)
 				{
 					if(blockBody &&
 							expectedReturnType != TypeUtils.NO_EXPECTED_TYPE &&
 							!JetStandardClasses.isUnit(expectedReturnType) &&
 							!rootUnreachableElements.contains(expression))
 					{
 						noReturnError[0] = true;
 					}
 				}
 			});
 		}
 		if(noReturnError[0])
 		{
 			trace.report(Errors.NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY.on(function));
 		}
 	}
 
 	private Set<NapileElement> collectUnreachableCode()
 	{
 		Collection<NapileElement> unreachableElements = Lists.newArrayList();
 		for(Instruction deadInstruction : pseudocode.getDeadInstructions())
 		{
 			if(deadInstruction instanceof JetElementInstruction && !(deadInstruction instanceof ReadUnitValueInstruction))
 			{
 				unreachableElements.add(((JetElementInstruction) deadInstruction).getElement());
 			}
 		}
 		// This is needed in order to highlight only '1 < 2' and not '1', '<' and '2' as well
 		return NapilePsiUtil.findRootExpressions(unreachableElements);
 	}
 
 	////////////////////////////////////////////////////////////////////////////////
 	//  Uninitialized variables analysis
 
 	public void markUninitializedVariables(final boolean processLocalDeclaration)
 	{
 		final Collection<VariableDescriptor> varWithUninitializedErrorGenerated = Sets.newHashSet();
 		final Collection<VariableDescriptor> varWithValReassignErrorGenerated = Sets.newHashSet();
 		final boolean processClassOrObject = subroutine instanceof NapileLikeClass;
 
 		Map<Instruction, PseudocodeTraverser.Edges<Map<VariableDescriptor, VariableInitState>>> initializers = pseudocodeVariablesData.getVariableInitializers();
 		final Set<VariableDescriptor> declaredVariables = pseudocodeVariablesData.getDeclaredVariables(pseudocode);
 		PseudocodeTraverser.traverse(pseudocode, true, true, initializers, new PseudocodeTraverser.InstructionDataAnalyzeStrategy<Map<VariableDescriptor, PseudocodeVariablesData.VariableInitState>>()
 		{
 			@Override
 			public void execute(@NotNull Instruction instruction, @Nullable Map<VariableDescriptor, PseudocodeVariablesData.VariableInitState> in, @Nullable Map<VariableDescriptor, PseudocodeVariablesData.VariableInitState> out)
 			{
 				assert in != null && out != null;
 				VariableDescriptor variableDescriptor = PseudocodeUtil.extractVariableDescriptorIfAny(instruction, true, trace.getBindingContext());
 				if(variableDescriptor == null)
 					return;
 				if(!(instruction instanceof ReadValueInstruction) && !(instruction instanceof WriteValueInstruction))
 				{
 					return;
 				}
 				PseudocodeVariablesData.VariableInitState outInitState = out.get(variableDescriptor);
 				if(instruction instanceof ReadValueInstruction)
 				{
 					NapileElement element = ((ReadValueInstruction) instruction).getElement();
 					boolean error = checkBackingField(variableDescriptor, element);
 					if(!error && declaredVariables.contains(variableDescriptor))
 					{
 						checkIsInitialized(variableDescriptor, element, outInitState, varWithUninitializedErrorGenerated);
 					}
 					return;
 				}
 				NapileElement element = ((WriteValueInstruction) instruction).getlValue();
 				boolean error = checkBackingField(variableDescriptor, element);
 				if(!(element instanceof NapileExpression))
 					return;
 				PseudocodeVariablesData.VariableInitState inInitState = in.get(variableDescriptor);
 				if(!error && !processLocalDeclaration)
 				{ // error has been generated before, while processing outer function of this local declaration
 					error = checkValReassignment(variableDescriptor, (NapileExpression) element, inInitState, varWithValReassignErrorGenerated);
 				}
 				if(!error && processClassOrObject)
 				{
 					error = checkAssignmentBeforeDeclaration(variableDescriptor, (NapileExpression) element, inInitState, outInitState);
 				}
 				if(!error && processClassOrObject)
 				{
 					checkInitializationUsingBackingField(variableDescriptor, (NapileExpression) element, inInitState, outInitState);
 				}
 			}
 		});
 
 		Pseudocode pseudocode = pseudocodeVariablesData.getPseudocode();
 		recordInitializedVariables(pseudocode, initializers);
 		for(LocalDeclarationInstruction instruction : pseudocode.getLocalDeclarations())
 		{
 			recordInitializedVariables(instruction.getBody(), initializers);
 		}
 	}
 
 	private void checkIsInitialized(@NotNull VariableDescriptor variableDescriptor, @NotNull NapileElement element, @NotNull PseudocodeVariablesData.VariableInitState variableInitState, @NotNull Collection<VariableDescriptor> varWithUninitializedErrorGenerated)
 	{
 		if(!(element instanceof NapileSimpleNameExpression))
 			return;
 
 		boolean isInitialized = variableInitState.isInitialized;
 		if(variableDescriptor instanceof PropertyDescriptor)
 		{
 			if(!trace.get(BindingContext.BACKING_FIELD_REQUIRED, (PropertyDescriptor) variableDescriptor))
 			{
 				isInitialized = true;
 			}
 		}
 		if(!isInitialized && !varWithUninitializedErrorGenerated.contains(variableDescriptor))
 		{
 			varWithUninitializedErrorGenerated.add(variableDescriptor);
 			if(variableDescriptor instanceof ParameterDescriptor)
 			{
 				trace.report(Errors.UNINITIALIZED_PARAMETER.on((NapileSimpleNameExpression) element, (ParameterDescriptor) variableDescriptor));
 			}
 			else
 			{
 				trace.report(Errors.UNINITIALIZED_VARIABLE.on((NapileSimpleNameExpression) element, variableDescriptor));
 			}
 		}
 	}
 
 	private boolean checkValReassignment(@NotNull VariableDescriptor variableDescriptor, @NotNull NapileExpression expression, @NotNull PseudocodeVariablesData.VariableInitState enterInitState, @NotNull Collection<VariableDescriptor> varWithValReassignErrorGenerated)
 	{
 		boolean isInitializedNotHere = enterInitState.isInitialized;
 		if(expression.getParent() instanceof NapileProperty && ((NapileProperty) expression).getInitializer() != null)
 		{
 			isInitializedNotHere = false;
 		}
 		boolean hasBackingField = true;
 		if(variableDescriptor instanceof PropertyDescriptor)
 		{
 			hasBackingField = trace.get(BindingContext.BACKING_FIELD_REQUIRED, (PropertyDescriptor) variableDescriptor);
 		}
 		if((isInitializedNotHere || !hasBackingField) &&
 				variableDescriptor.getPropertyKind() != PropertyKind.VAR &&
 				!varWithValReassignErrorGenerated.contains(variableDescriptor))
 		{
 			boolean hasReassignMethodReturningUnit = false;
 			NapileSimpleNameExpression operationReference = null;
 			PsiElement parent = expression.getParent();
 			if(parent instanceof NapileBinaryExpression)
 			{
 				operationReference = ((NapileBinaryExpression) parent).getOperationReference();
 			}
 			else if(parent instanceof NapileUnaryExpression)
 			{
 				operationReference = ((NapileUnaryExpression) parent).getOperationReference();
 			}
 			if(operationReference != null)
 			{
 				DeclarationDescriptor descriptor = trace.get(BindingContext.REFERENCE_TARGET, operationReference);
 				if(descriptor instanceof MethodDescriptor)
 				{
 					if(JetStandardClasses.isUnit(((MethodDescriptor) descriptor).getReturnType()))
 					{
 						hasReassignMethodReturningUnit = true;
 					}
 				}
 				if(descriptor == null)
 				{
 					Collection<? extends DeclarationDescriptor> descriptors = trace.get(BindingContext.AMBIGUOUS_REFERENCE_TARGET, operationReference);
 					if(descriptors != null)
 					{
 						for(DeclarationDescriptor referenceDescriptor : descriptors)
 						{
 							if(JetStandardClasses.isUnit(((MethodDescriptor) referenceDescriptor).getReturnType()))
 							{
 								hasReassignMethodReturningUnit = true;
 							}
 						}
 					}
 				}
 			}
 			if(!hasReassignMethodReturningUnit)
 			{
 				varWithValReassignErrorGenerated.add(variableDescriptor);
				trace.report(Errors.FINAL_VAR_REASSIGNMENT.on(expression, variableDescriptor));
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean checkAssignmentBeforeDeclaration(@NotNull VariableDescriptor variableDescriptor, @NotNull NapileExpression expression, @NotNull PseudocodeVariablesData.VariableInitState enterInitState, @NotNull PseudocodeVariablesData.VariableInitState exitInitState)
 	{
 		if(!enterInitState.isDeclared && !exitInitState.isDeclared && !enterInitState.isInitialized && exitInitState.isInitialized)
 		{
 			trace.report(Errors.INITIALIZATION_BEFORE_DECLARATION.on(expression, variableDescriptor));
 			return true;
 		}
 		return false;
 	}
 
 	private boolean checkInitializationUsingBackingField(@NotNull VariableDescriptor variableDescriptor, @NotNull NapileExpression expression, @NotNull PseudocodeVariablesData.VariableInitState enterInitState, @NotNull PseudocodeVariablesData.VariableInitState exitInitState)
 	{
 		if(variableDescriptor instanceof PropertyDescriptor && !enterInitState.isInitialized && exitInitState.isInitialized)
 		{
 			if(variableDescriptor.getPropertyKind() != PropertyKind.VAR)
 				return false;
 			if(!trace.get(BindingContext.BACKING_FIELD_REQUIRED, (PropertyDescriptor) variableDescriptor))
 				return false;
 			PsiElement property = BindingContextUtils.descriptorToDeclaration(trace.getBindingContext(), variableDescriptor);
 			assert property instanceof NapileProperty;
 			if(((PropertyDescriptor) variableDescriptor).getModality() == Modality.FINAL && ((NapileProperty) property).getSetter() == null)
 			{
 				return false;
 			}
 			NapileExpression variable = expression;
 			if(expression instanceof NapileDotQualifiedExpression)
 			{
 				if(((NapileDotQualifiedExpression) expression).getReceiverExpression() instanceof NapileThisExpression)
 				{
 					variable = ((NapileDotQualifiedExpression) expression).getSelectorExpression();
 				}
 			}
 			if(variable instanceof NapileSimpleNameExpression)
 			{
 				NapileSimpleNameExpression simpleNameExpression = (NapileSimpleNameExpression) variable;
 				if(simpleNameExpression.getReferencedNameElementType() != JetTokens.FIELD_IDENTIFIER)
 				{
 					if(((PropertyDescriptor) variableDescriptor).getModality() != Modality.FINAL)
 					{
 						trace.report(Errors.INITIALIZATION_USING_BACKING_FIELD_OPEN_SETTER.on(expression, variableDescriptor));
 					}
 					else
 					{
 						trace.report(Errors.INITIALIZATION_USING_BACKING_FIELD_CUSTOM_SETTER.on(expression, variableDescriptor));
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean checkBackingField(@NotNull VariableDescriptor variableDescriptor, @NotNull NapileElement element)
 	{
 		boolean[] error = new boolean[1];
 		if(isBackingFieldReference((NapileElement) element.getParent(), error, false))
 		{
 			return false; // this expression has been already checked
 		}
 		if(!isBackingFieldReference(element, error, true))
 			return false;
 		if(error[0])
 			return true;
 		if(!(variableDescriptor instanceof PropertyDescriptor))
 		{
 			trace.report(Errors.NOT_PROPERTY_BACKING_FIELD.on(element));
 			return true;
 		}
 		PsiElement property = BindingContextUtils.descriptorToDeclaration(trace.getBindingContext(), variableDescriptor);
 		boolean insideSelfAccessors = PsiTreeUtil.isAncestor(property, element, false);
 		if(!trace.get(BindingContext.BACKING_FIELD_REQUIRED, (PropertyDescriptor) variableDescriptor) && !insideSelfAccessors)
 		{ // not to generate error in accessors of abstract properties, there is one: declared accessor of abstract property
 
 			if(((PropertyDescriptor) variableDescriptor).getModality() == Modality.ABSTRACT)
 			{
 				trace.report(Errors.NO_BACKING_FIELD_ABSTRACT_PROPERTY.on(element));
 			}
 			else
 			{
 				trace.report(Errors.NO_BACKING_FIELD_CUSTOM_ACCESSORS.on(element));
 			}
 			return true;
 		}
 		if(insideSelfAccessors)
 			return false;
 
 		NapileNamedDeclaration parentDeclaration = PsiTreeUtil.getParentOfType(element, NapileNamedDeclaration.class);
 		DeclarationDescriptor declarationDescriptor = trace.get(BindingContext.DECLARATION_TO_DESCRIPTOR, parentDeclaration);
 		if(declarationDescriptor == null)
 			return false;
 
 		DeclarationDescriptor containingDeclaration = variableDescriptor.getContainingDeclaration();
 		if((containingDeclaration instanceof ClassDescriptor) && DescriptorUtils.isAncestor(containingDeclaration, declarationDescriptor, false))
 		{
 			return false;
 		}
 		trace.report(Errors.INACCESSIBLE_BACKING_FIELD.on(element));
 		return true;
 	}
 
 	private boolean isBackingFieldReference(@Nullable NapileElement element, boolean[] error, boolean reportError)
 	{
 		error[0] = false;
 		if(element instanceof NapileSimpleNameExpression && ((NapileSimpleNameExpression) element).getReferencedNameElementType() == JetTokens.FIELD_IDENTIFIER)
 		{
 			return true;
 		}
 		if(element instanceof NapileDotQualifiedExpression && isBackingFieldReference(((NapileDotQualifiedExpression) element).getSelectorExpression(), error, false))
 		{
 			if(((NapileDotQualifiedExpression) element).getReceiverExpression() instanceof NapileThisExpression)
 			{
 				return true;
 			}
 			error[0] = true;
 			if(reportError)
 			{
 				trace.report(Errors.INACCESSIBLE_BACKING_FIELD.on(element));
 			}
 		}
 		return false;
 	}
 
 	private void recordInitializedVariables(@NotNull Pseudocode pseudocode, @NotNull Map<Instruction, PseudocodeTraverser.Edges<Map<VariableDescriptor, PseudocodeVariablesData.VariableInitState>>> initializersMap)
 	{
 		PseudocodeTraverser.Edges<Map<VariableDescriptor, VariableInitState>> initializers = initializersMap.get(pseudocode.getExitInstruction());
 		Set<VariableDescriptor> usedVariables = pseudocodeVariablesData.getUsedVariables(pseudocode);
 		Set<VariableDescriptor> declaredVariables = pseudocodeVariablesData.getDeclaredVariables(pseudocode);
 		for(VariableDescriptor variable : usedVariables)
 		{
 			if(variable instanceof PropertyDescriptor && declaredVariables.contains(variable))
 			{
 				PseudocodeVariablesData.VariableInitState variableInitState = initializers.in.get(variable);
 				if(variableInitState == null)
 					return;
 				trace.record(BindingContext.IS_INITIALIZED, (PropertyDescriptor) variable, variableInitState.isInitialized);
 			}
 		}
 	}
 
 	////////////////////////////////////////////////////////////////////////////////
 	//  "Unused variable" & "unused value" analyses
 
 	public void markUnusedVariables()
 	{
 		Map<Instruction, PseudocodeTraverser.Edges<Map<VariableDescriptor, VariableUseState>>> variableStatusData = pseudocodeVariablesData.getVariableUseStatusData();
 		PseudocodeTraverser.InstructionDataAnalyzeStrategy<Map<VariableDescriptor, VariableUseState>> variableStatusAnalyzeStrategy = new PseudocodeTraverser.InstructionDataAnalyzeStrategy<Map<VariableDescriptor, PseudocodeVariablesData.VariableUseState>>()
 		{
 			@Override
 			public void execute(@NotNull Instruction instruction, @Nullable Map<VariableDescriptor, PseudocodeVariablesData.VariableUseState> in, @Nullable Map<VariableDescriptor, PseudocodeVariablesData.VariableUseState> out)
 			{
 
 				assert in != null && out != null;
 				Set<VariableDescriptor> declaredVariables = pseudocodeVariablesData.getDeclaredVariables(instruction.getOwner());
 				VariableDescriptor variableDescriptor = PseudocodeUtil.extractVariableDescriptorIfAny(instruction, false, trace.getBindingContext());
 				if(variableDescriptor == null || !declaredVariables.contains(variableDescriptor) ||
 						!DescriptorUtils.isLocal(variableDescriptor.getContainingDeclaration(), variableDescriptor))
 				{
 					return;
 				}
 				PseudocodeVariablesData.VariableUseState variableUseState = in.get(variableDescriptor);
 				if(instruction instanceof WriteValueInstruction)
 				{
 					if(trace.get(BindingContext.CAPTURED_IN_CLOSURE, variableDescriptor))
 						return;
 					NapileElement element = ((WriteValueInstruction) instruction).getElement();
 					if(variableUseState != PseudocodeVariablesData.VariableUseState.LAST_READ)
 					{
 						if(element instanceof NapileBinaryExpression && ((NapileBinaryExpression) element).getOperationToken() == JetTokens.EQ)
 						{
 							NapileExpression right = ((NapileBinaryExpression) element).getRight();
 							if(right != null)
 							{
 								trace.report(Errors.UNUSED_VALUE.on(right, right, variableDescriptor));
 							}
 						}
 						else if(element instanceof NapilePostfixExpression)
 						{
 							IElementType operationToken = ((NapilePostfixExpression) element).getOperationReference().getReferencedNameElementType();
 							if(operationToken == JetTokens.PLUSPLUS || operationToken == JetTokens.MINUSMINUS)
 							{
 								trace.report(Errors.UNUSED_CHANGED_VALUE.on(element, element));
 							}
 						}
 					}
 				}
 				else if(instruction instanceof VariableDeclarationInstruction)
 				{
 					NapileDeclaration element = ((VariableDeclarationInstruction) instruction).getVariableDeclarationElement();
 					if(element instanceof NapileNamedDeclaration)
 					{
 						PsiElement nameIdentifier = ((NapileNamedDeclaration) element).getNameIdentifier();
 						if(nameIdentifier == null)
 							return;
 						if(!PseudocodeVariablesData.VariableUseState.isUsed(variableUseState))
 						{
 							if(element instanceof NapileProperty)
 							{
 								trace.report(Errors.UNUSED_VARIABLE.on((NapileProperty) element, variableDescriptor));
 							}
 							else if(element instanceof NapilePropertyParameter)
 							{
 								PsiElement psiElement = element.getParent().getParent();
 								if(psiElement instanceof NapileMethod)
 								{
 									boolean isMain = (psiElement instanceof NapileNamedFunction) && JetMainDetector.isMain((NapileNamedFunction) psiElement);
 									if(psiElement instanceof NapileFunctionLiteral)
 										return;
 									DeclarationDescriptor descriptor = trace.get(BindingContext.DECLARATION_TO_DESCRIPTOR, psiElement);
 									assert descriptor instanceof MethodDescriptor : psiElement.getText();
 									MethodDescriptor methodDescriptor = (MethodDescriptor) descriptor;
 									if(!isMain &&
 											!methodDescriptor.getModality().isOverridable() &&
 											methodDescriptor.getOverriddenDescriptors().isEmpty())
 									{
 										trace.report(Errors.UNUSED_PARAMETER.on((NapilePropertyParameter) element, variableDescriptor));
 									}
 								}
 							}
 						}
 						else if(variableUseState == PseudocodeVariablesData.VariableUseState.ONLY_WRITTEN_NEVER_READ && element instanceof NapileProperty)
 						{
 							trace.report(Errors.ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE.on((NapileNamedDeclaration) element, variableDescriptor));
 						}
 						else if(variableUseState == PseudocodeVariablesData.VariableUseState.LAST_WRITTEN && element instanceof NapileProperty)
 						{
 							NapileExpression initializer = ((NapileProperty) element).getInitializer();
 							if(initializer != null)
 							{
 								trace.report(Errors.VARIABLE_WITH_REDUNDANT_INITIALIZER.on(initializer, variableDescriptor));
 							}
 						}
 					}
 				}
 			}
 		};
 		PseudocodeTraverser.traverse(pseudocode, false, true, variableStatusData, variableStatusAnalyzeStrategy);
 	}
 
 	////////////////////////////////////////////////////////////////////////////////
 	//  "Unused literals" in block
 
 	public void markUnusedLiteralsInBlock()
 	{
 		assert pseudocode != null;
 		PseudocodeTraverser.traverse(pseudocode, true, new PseudocodeTraverser.InstructionAnalyzeStrategy()
 		{
 			@Override
 			public void execute(@NotNull Instruction instruction)
 			{
 				if(!(instruction instanceof ReadValueInstruction))
 					return;
 				NapileElement element = ((ReadValueInstruction) instruction).getElement();
 				if(!(element instanceof NapileFunctionLiteralExpression || element instanceof NapileConstantExpression || element instanceof NapileStringTemplateExpression || element instanceof NapileSimpleNameExpression))
 				{
 					return;
 				}
 				PsiElement parent = element.getParent();
 				if(parent instanceof NapileBlockExpression)
 				{
 					if(!NapilePsiUtil.isImplicitlyUsed(element))
 					{
 						if(element instanceof NapileFunctionLiteralExpression)
 						{
 							trace.report(Errors.UNUSED_FUNCTION_LITERAL.on((NapileFunctionLiteralExpression) element));
 						}
 						else
 						{
 							trace.report(Errors.UNUSED_EXPRESSION.on(element));
 						}
 					}
 				}
 			}
 		});
 	}
 }
