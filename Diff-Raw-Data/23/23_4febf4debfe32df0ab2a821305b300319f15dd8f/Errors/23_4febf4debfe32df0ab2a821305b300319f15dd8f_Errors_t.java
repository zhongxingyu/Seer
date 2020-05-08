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
 
 package org.napile.compiler.lang.diagnostics;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.Collection;
 import java.util.List;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.compiler.lang.descriptors.*;
 import org.napile.compiler.lang.resolve.calls.inference.InferenceErrorData;
 import org.napile.compiler.lang.types.JetType;
 import org.napile.compiler.lexer.NapileKeywordToken;
 import org.napile.compiler.lang.psi.*;
 import com.google.common.collect.Lists;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiNameIdentifierOwner;
 import com.intellij.psi.impl.source.tree.LeafPsiElement;
 
 /**
  * For error messages, see DefaultErrorMessages and IdeErrorMessages.
  *
  * @author abreslav
  */
 public interface Errors
 {
 
 	DiagnosticFactory1<NapileFile, Throwable> EXCEPTION_WHILE_ANALYZING = DiagnosticFactory1.create(Severity.ERROR);
 
 	UnresolvedReferenceDiagnosticFactory UNRESOLVED_REFERENCE = UnresolvedReferenceDiagnosticFactory.create();
 
 	//Elements with "INVISIBLE_REFERENCE" error are marked as unresolved, unlike elements with "INVISIBLE_MEMBER" error
 	DiagnosticFactory2<NapileSimpleNameExpression, DeclarationDescriptor, DeclarationDescriptor> INVISIBLE_REFERENCE = DiagnosticFactory2.create(Severity.ERROR);
 	DiagnosticFactory2<PsiElement, DeclarationDescriptor, DeclarationDescriptor> INVISIBLE_MEMBER = DiagnosticFactory2.create(Severity.ERROR, PositioningStrategies.CALL_ELEMENT);
 
 	RedeclarationDiagnosticFactory REDECLARATION = new RedeclarationDiagnosticFactory(Severity.ERROR);
 	RedeclarationDiagnosticFactory NAME_SHADOWING = new RedeclarationDiagnosticFactory(Severity.WARNING);
 
 	DiagnosticFactory2<PsiElement, JetType, JetType> TYPE_MISMATCH = DiagnosticFactory2.create(Severity.ERROR);
 	DiagnosticFactory1<PsiElement, Collection<NapileKeywordToken>> INCOMPATIBLE_MODIFIERS = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<PsiElement, NapileKeywordToken> ILLEGAL_MODIFIER = DiagnosticFactory1.create(Severity.ERROR);
 
 	DiagnosticFactory2<PsiElement, NapileKeywordToken, NapileKeywordToken> REDUNDANT_MODIFIER = DiagnosticFactory2.create(Severity.WARNING);
 
 	SimpleDiagnosticFactory<PsiElement> REDUNDANT_MODIFIER_IN_GETTER = SimpleDiagnosticFactory.create(Severity.WARNING);
 
 	SimpleDiagnosticFactory<NapileExpression> TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileReturnExpression> RETURN_NOT_ALLOWED = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileSimpleNameExpression> LABEL_NAME_CLASH = SimpleDiagnosticFactory.create(Severity.WARNING);
 	SimpleDiagnosticFactory<NapileSimpleNameExpression> EXPRESSION_EXPECTED_NAMESPACE_FOUND = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	DiagnosticFactory1<NapileSimpleNameExpression, DeclarationDescriptor> CANNOT_IMPORT_FROM_ELEMENT = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileSimpleNameExpression, DeclarationDescriptor> CANNOT_BE_IMPORTED = DiagnosticFactory1.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> USELESS_HIDDEN_IMPORT = SimpleDiagnosticFactory.create(Severity.WARNING);
 	SimpleDiagnosticFactory<NapileExpression> USELESS_SIMPLE_IMPORT = SimpleDiagnosticFactory.create(Severity.WARNING);
 
 	SimpleDiagnosticFactory<NapilePropertyParameter> CANNOT_INFER_PARAMETER_TYPE = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileElement> NO_BACKING_FIELD_ABSTRACT_PROPERTY = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileElement> NO_BACKING_FIELD_CUSTOM_ACCESSORS = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileElement> INACCESSIBLE_BACKING_FIELD = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileElement> NOT_PROPERTY_BACKING_FIELD = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<PsiElement> MIXING_NAMED_AND_POSITIONED_ARGUMENTS = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileReferenceExpression> ARGUMENT_PASSED_TWICE = SimpleDiagnosticFactory.create(Severity.ERROR);
 	UnresolvedReferenceDiagnosticFactory NAMED_PARAMETER_NOT_FOUND = UnresolvedReferenceDiagnosticFactory.create();
 	SimpleDiagnosticFactory<NapileExpression> VARARG_OUTSIDE_PARENTHESES = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<LeafPsiElement> NON_VARARG_SPREAD = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileExpression> MANY_FUNCTION_LITERAL_ARGUMENTS = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileProperty> PROPERTY_WITH_NO_TYPE_NO_INITIALIZER = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.NAMED_ELEMENT);
 
 	SimpleDiagnosticFactory<NapileProperty> ABSTRACT_PROPERTY_NOT_IN_CLASS = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.ABSTRACT_MODIFIER);
 	SimpleDiagnosticFactory<NapileExpression> ABSTRACT_PROPERTY_WITH_INITIALIZER = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapilePropertyAccessor> ABSTRACT_PROPERTY_WITH_GETTER = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapilePropertyAccessor> ABSTRACT_PROPERTY_WITH_SETTER = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<PsiElement> PACKAGE_MEMBER_CANNOT_BE_HERITABLE = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<PsiElement> GETTER_VISIBILITY_DIFFERS_FROM_PROPERTY_VISIBILITY = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<PsiElement> INSTANCE_CALL_FROM_STATIC_CONTEXT = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileProperty> MUST_BE_INITIALIZED = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.NAMED_ELEMENT);
 	SimpleDiagnosticFactory<NapileProperty> MUST_BE_INITIALIZED_OR_BE_ABSTRACT = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.NAMED_ELEMENT);
 
 	SimpleDiagnosticFactory<NapileExpression> PROPERTY_INITIALIZER_NO_BACKING_FIELD = SimpleDiagnosticFactory.create(Severity.ERROR);
 	DiagnosticFactory2<NapileModifierListOwner, String, ClassDescriptor> ABSTRACT_PROPERTY_IN_NON_ABSTRACT_CLASS = DiagnosticFactory2.create(Severity.ERROR, PositioningStrategies.ABSTRACT_MODIFIER);
 	DiagnosticFactory2<NapileMethod, String, ClassDescriptor> ABSTRACT_FUNCTION_IN_NON_ABSTRACT_CLASS = DiagnosticFactory2.create(Severity.ERROR, PositioningStrategies.ABSTRACT_MODIFIER);
 	DiagnosticFactory1<NapileMethod, SimpleMethodDescriptor> ABSTRACT_FUNCTION_WITH_BODY = DiagnosticFactory1.create(Severity.ERROR, PositioningStrategies.ABSTRACT_MODIFIER);
 	DiagnosticFactory1<NapileMethod, SimpleMethodDescriptor> NON_ABSTRACT_OR_NATIVE_METHOD_WITH_NO_BODY = DiagnosticFactory1.create(Severity.ERROR, PositioningStrategies.NAMED_ELEMENT);
 	DiagnosticFactory1<NapileModifierListOwner, SimpleMethodDescriptor> NON_MEMBER_ABSTRACT_FUNCTION = DiagnosticFactory1.create(Severity.ERROR, PositioningStrategies.ABSTRACT_MODIFIER);
 
 	DiagnosticFactory1<NapileMethod, SimpleMethodDescriptor> NON_MEMBER_FUNCTION_NO_BODY = DiagnosticFactory1.create(Severity.ERROR, PositioningStrategies.NAMED_ELEMENT);
 
 	SimpleDiagnosticFactory<NapileNamedDeclaration> PUBLIC_MEMBER_SHOULD_SPECIFY_TYPE = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.NAMED_ELEMENT);
 
 	SimpleDiagnosticFactory<NapileDelegatorToSuperClass> SUPERTYPE_NOT_INITIALIZED = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileDelegatorToSuperClass> SUPERTYPE_NOT_INITIALIZED_DEFAULT = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileDelegatorToSuperClass> INITIALIZER_WITH_NO_ARGUMENTS = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileDelegationSpecifier> MANY_CALLS_TO_THIS = SimpleDiagnosticFactory.create(Severity.ERROR);
 	DiagnosticFactory1<NapileModifierListOwner, CallableMemberDescriptor> NOTHING_TO_OVERRIDE = DiagnosticFactory1.create(Severity.ERROR, PositioningStrategies.OVERRIDE_MODIFIER);
 	DiagnosticFactory3<PsiNameIdentifierOwner, CallableMemberDescriptor, CallableMemberDescriptor, DeclarationDescriptor> VIRTUAL_MEMBER_HIDDEN = DiagnosticFactory3.create(Severity.ERROR, PositioningStrategies.NAMED_ELEMENT);
 	DiagnosticFactory3<NapileModifierListOwner, CallableMemberDescriptor, CallableDescriptor, DeclarationDescriptor> CANNOT_OVERRIDE_INVISIBLE_MEMBER = DiagnosticFactory3.create(Severity.ERROR, PositioningStrategies.OVERRIDE_MODIFIER);
 	SimpleDiagnosticFactory<NapileDeclaration> CANNOT_INFER_VISIBILITY = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.DECLARATION);
 
 	DiagnosticFactory1<NapileClass, ClassDescriptor> ENUM_ENTRY_SHOULD_BE_INITIALIZED = DiagnosticFactory1.create(Severity.ERROR, PositioningStrategies.NAME_IDENTIFIER);
 	DiagnosticFactory1<NapileTypeReference, ClassDescriptor> ENUM_ENTRY_ILLEGAL_TYPE = DiagnosticFactory1.create(Severity.ERROR);
 
 	DiagnosticFactory1<NapileSimpleNameExpression, VariableDescriptor> UNINITIALIZED_VARIABLE = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileSimpleNameExpression, ParameterDescriptor> UNINITIALIZED_PARAMETER = DiagnosticFactory1.create(Severity.ERROR);
 	UnusedElementDiagnosticFactory<NapileProperty, VariableDescriptor> UNUSED_VARIABLE = UnusedElementDiagnosticFactory.create(Severity.WARNING, PositioningStrategies.NAME_IDENTIFIER);
 	UnusedElementDiagnosticFactory<NapilePropertyParameter, VariableDescriptor> UNUSED_PARAMETER = UnusedElementDiagnosticFactory.create(Severity.WARNING, PositioningStrategies.NAME_IDENTIFIER);
 	UnusedElementDiagnosticFactory<NapileNamedDeclaration, DeclarationDescriptor> ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE = UnusedElementDiagnosticFactory.create(Severity.WARNING, PositioningStrategies.NAME_IDENTIFIER);
 	DiagnosticFactory1<NapileExpression, DeclarationDescriptor> VARIABLE_WITH_REDUNDANT_INITIALIZER = DiagnosticFactory1.create(Severity.WARNING);
 	DiagnosticFactory2<NapileElement, NapileElement, DeclarationDescriptor> UNUSED_VALUE = DiagnosticFactory2.create(Severity.WARNING);
 	DiagnosticFactory1<NapileElement, NapileElement> UNUSED_CHANGED_VALUE = DiagnosticFactory1.create(Severity.WARNING);
 	SimpleDiagnosticFactory<NapileElement> UNUSED_EXPRESSION = SimpleDiagnosticFactory.create(Severity.WARNING);
 	SimpleDiagnosticFactory<NapileFunctionLiteralExpression> UNUSED_FUNCTION_LITERAL = SimpleDiagnosticFactory.create(Severity.WARNING);
 
	DiagnosticFactory1<NapileExpression, DeclarationDescriptor> FINAL_VAR_REASSIGNMENT = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileExpression, DeclarationDescriptor> INITIALIZATION_BEFORE_DECLARATION = DiagnosticFactory1.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> VARIABLE_EXPECTED = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	DiagnosticFactory1<NapileExpression, DeclarationDescriptor> INITIALIZATION_USING_BACKING_FIELD_CUSTOM_SETTER = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileExpression, DeclarationDescriptor> INITIALIZATION_USING_BACKING_FIELD_OPEN_SETTER = DiagnosticFactory1.create(Severity.ERROR);
 
 	DiagnosticFactory1<NapileSimpleNameExpression, DeclarationDescriptor> FUNCTION_PARAMETERS_OF_INLINE_FUNCTION = DiagnosticFactory1.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileElement> UNREACHABLE_CODE = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<PsiElement> NO_CONSTRUCTOR = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> NOT_A_CLASS = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileEscapeStringTemplateEntry> ILLEGAL_ESCAPE_SEQUENCE = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	DiagnosticFactory1<PsiElement, JetType> MISSED_SUPER_CALL = DiagnosticFactory1.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileTypeReference> INVALID_SUPER_CALL = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileTypeReference> LOCAL_EXTENSION_PROPERTY = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapilePropertyAccessor> LOCAL_VARIABLE_WITH_GETTER = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapilePropertyAccessor> LOCAL_VARIABLE_WITH_SETTER = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapilePropertyAccessor> VAL_WITH_SETTER = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileArrayAccessExpression> NO_GET_METHOD = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.ARRAY_ACCESS);
 	SimpleDiagnosticFactory<NapileArrayAccessExpression> NO_SET_METHOD = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.ARRAY_ACCESS);
 
 	SimpleDiagnosticFactory<NapileSimpleNameExpression> INC_DEC_SHOULD_NOT_RETURN_UNIT = SimpleDiagnosticFactory.create(Severity.ERROR);
 	DiagnosticFactory2<NapileSimpleNameExpression, DeclarationDescriptor, NapileSimpleNameExpression> ASSIGNMENT_OPERATOR_SHOULD_RETURN_UNIT = DiagnosticFactory2.create(Severity.ERROR);
 	AmbiguousDescriptorDiagnosticFactory ASSIGN_OPERATOR_AMBIGUITY = AmbiguousDescriptorDiagnosticFactory.create();
 
 	SimpleDiagnosticFactory<NapileSimpleNameExpression> EQUALS_MISSING = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileBinaryExpression> ASSIGNMENT_IN_EXPRESSION_CONTEXT = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileRootNamespaceExpression> NAMESPACE_IS_NOT_AN_EXPRESSION = SimpleDiagnosticFactory.create(Severity.ERROR);
 	DiagnosticFactory1<NapileSuperExpression, String> SUPER_IS_NOT_AN_EXPRESSION = DiagnosticFactory1.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileDeclaration> DECLARATION_IN_ILLEGAL_CONTEXT = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> SETTER_PARAMETER_WITH_DEFAULT_VALUE = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileThisExpression> NO_THIS = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileSuperExpression> SUPER_NOT_AVAILABLE = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileSuperExpression> AMBIGUOUS_SUPER = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> ABSTRACT_SUPER_CALL = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileTypeReference> NOT_A_SUPERTYPE = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<PsiElement> TYPE_ARGUMENTS_REDUNDANT_IN_SUPER_QUALIFIER = SimpleDiagnosticFactory.create(Severity.WARNING);
 	SimpleDiagnosticFactory<NapileSimpleNameExpression> USELESS_CAST_STATIC_ASSERT_IS_FINE = SimpleDiagnosticFactory.create(Severity.WARNING);
 	SimpleDiagnosticFactory<NapileSimpleNameExpression> USELESS_CAST = SimpleDiagnosticFactory.create(Severity.WARNING);
 	SimpleDiagnosticFactory<NapileSimpleNameExpression> CAST_NEVER_SUCCEEDS = SimpleDiagnosticFactory.create(Severity.WARNING);
 	DiagnosticFactory2<NapileTypeReference, JetType, JetType> WRONG_SETTER_PARAMETER_TYPE = DiagnosticFactory2.create(Severity.ERROR);
 	DiagnosticFactory2<NapileTypeReference, JetType, JetType> WRONG_GETTER_RETURN_TYPE = DiagnosticFactory2.create(Severity.ERROR);
 	@Deprecated
 	DiagnosticFactory1<NapileSimpleNameExpression, ClassifierDescriptor> NO_CLASS_OBJECT = DiagnosticFactory1.create(Severity.ERROR);
 	SimpleDiagnosticFactory<PsiElement> NO_GENERICS_IN_SUPERTYPE_SPECIFIER = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileExpression> HAS_NEXT_PROPERTY_AND_FUNCTION_AMBIGUITY = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> HAS_NEXT_MISSING = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> HAS_NEXT_FUNCTION_AMBIGUITY = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> HAS_NEXT_MUST_BE_READABLE = SimpleDiagnosticFactory.create(Severity.ERROR);
 	DiagnosticFactory1<NapileExpression, JetType> HAS_NEXT_PROPERTY_TYPE_MISMATCH = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileExpression, JetType> HAS_NEXT_FUNCTION_TYPE_MISMATCH = DiagnosticFactory1.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> NEXT_AMBIGUITY = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> NEXT_MISSING = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> ITERATOR_MISSING = SimpleDiagnosticFactory.create(Severity.ERROR);
 	AmbiguousDescriptorDiagnosticFactory ITERATOR_AMBIGUITY = AmbiguousDescriptorDiagnosticFactory.create();
 
 	DiagnosticFactory1<NapileSimpleNameExpression, JetType> COMPARE_TO_TYPE_MISMATCH = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileExpression, JetType> CALLEE_NOT_A_FUNCTION = DiagnosticFactory1.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileReturnExpression> RETURN_IN_FUNCTION_WITH_EXPRESSION_BODY = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileDeclarationWithBody> NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.DECLARATION_WITH_BODY);
 	DiagnosticFactory1<NapileExpression, JetType> RETURN_TYPE_MISMATCH = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileExpression, JetType> EXPECTED_TYPE_MISMATCH = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileBinaryExpression, JetType> ASSIGNMENT_TYPE_MISMATCH = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileExpression, JetType> IMPLICIT_CAST_TO_UNIT_OR_ANY = DiagnosticFactory1.create(Severity.WARNING);
 	DiagnosticFactory1<NapileExpression, NapileExpression> EXPRESSION_EXPECTED = DiagnosticFactory1.create(Severity.ERROR);
 
 	DiagnosticFactory2<NapileTypeReference, JetType, JetType> UPPER_BOUND_VIOLATED = DiagnosticFactory2.create(Severity.ERROR);
 	DiagnosticFactory1<NapileTypeReference, JetType> FINAL_CLASS_OBJECT_UPPER_BOUND = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileTypeReference, JetType> FINAL_UPPER_BOUND = DiagnosticFactory1.create(Severity.WARNING);
 	DiagnosticFactory1<NapileExpression, JetType> USELESS_ELVIS = DiagnosticFactory1.create(Severity.WARNING);
 	DiagnosticFactory1<PsiElement, TypeParameterDescriptor> CONFLICTING_UPPER_BOUNDS = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<PsiElement, TypeParameterDescriptor> CONFLICTING_CLASS_OBJECT_UPPER_BOUNDS = DiagnosticFactory1.create(Severity.ERROR);
 
 	DiagnosticFactory1<PsiElement, CallableDescriptor> TOO_MANY_ARGUMENTS = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<PsiElement, String> ERROR_COMPILE_TIME_VALUE = DiagnosticFactory1.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileWhenEntry> ELSE_MISPLACED_IN_WHEN = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.ELSE_ENTRY);
 
 	SimpleDiagnosticFactory<NapileWhenExpression> NO_ELSE_IN_WHEN = new SimpleDiagnosticFactory<NapileWhenExpression>(Severity.ERROR, PositioningStrategies.WHEN_EXPRESSION);
 	SimpleDiagnosticFactory<NapileWhenConditionInRange> TYPE_MISMATCH_IN_RANGE = new SimpleDiagnosticFactory<NapileWhenConditionInRange>(Severity.ERROR, PositioningStrategies.WHEN_CONDITION_IN_RANGE);
 	SimpleDiagnosticFactory<PsiElement> CYCLIC_INHERITANCE_HIERARCHY = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileTypeReference> SUPERTYPE_APPEARS_TWICE = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileTypeReference> FINAL_SUPERTYPE = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	DiagnosticFactory1<NapileExpression, String> ILLEGAL_SELECTOR = DiagnosticFactory1.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapilePropertyParameter> VALUE_PARAMETER_WITH_NO_TYPE_ANNOTATION = SimpleDiagnosticFactory.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileExpression> BREAK_OR_CONTINUE_OUTSIDE_A_LOOP = SimpleDiagnosticFactory.create(Severity.ERROR);
 	DiagnosticFactory1<NapileExpression, String> NOT_A_LOOP_LABEL = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileReturnExpression, String> NOT_A_RETURN_LABEL = DiagnosticFactory1.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileNullableType> NULLABLE_SUPERTYPE = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.NULLABLE_TYPE);
 	DiagnosticFactory1<PsiElement, JetType> UNSAFE_CALL = DiagnosticFactory1.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileSimpleNameExpression> AMBIGUOUS_LABEL = SimpleDiagnosticFactory.create(Severity.ERROR);
 	DiagnosticFactory1<PsiElement, String> UNSUPPORTED = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<PsiElement, JetType> UNNECESSARY_SAFE_CALL = DiagnosticFactory1.create(Severity.WARNING);
 	DiagnosticFactory1<PsiElement, JetType> UNNECESSARY_NOT_NULL_ASSERTION = DiagnosticFactory1.create(Severity.WARNING);
 	DiagnosticFactory2<NapileSimpleNameExpression, NapileTypeConstraint, NapileTypeParameterListOwner> NAME_IN_CONSTRAINT_IS_NOT_A_TYPE_PARAMETER = DiagnosticFactory2.create(Severity.ERROR);
 	DiagnosticFactory2<NapileExpression, JetType, String> AUTOCAST_IMPOSSIBLE = DiagnosticFactory2.create(Severity.ERROR);
 
 	DiagnosticFactory2<NapileTypeReference, JetType, JetType> TYPE_MISMATCH_IN_FOR_LOOP = DiagnosticFactory2.create(Severity.ERROR);
 	DiagnosticFactory1<NapileElement, JetType> TYPE_MISMATCH_IN_CONDITION = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory2<NapileTypeReference, JetType, JetType> TYPE_MISMATCH_IN_BINDING_PATTERN = DiagnosticFactory2.create(Severity.ERROR);
 	DiagnosticFactory2<NapileElement, JetType, JetType> INCOMPATIBLE_TYPES = DiagnosticFactory2.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileWhenCondition> EXPECTED_CONDITION = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	DiagnosticFactory1<NapileElement, JetType> CANNOT_CHECK_FOR_ERASED = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory2<NapileBinaryExpressionWithTypeRHS, JetType, JetType> UNCHECKED_CAST = DiagnosticFactory2.create(Severity.WARNING);
 
 	DiagnosticFactory3<NapileDelegationSpecifierList, TypeParameterDescriptor, ClassDescriptor, Collection<JetType>> INCONSISTENT_TYPE_PARAMETER_VALUES = DiagnosticFactory3.create(Severity.ERROR);
 
 	DiagnosticFactory3<NapileBinaryExpression, NapileSimpleNameExpression, JetType, JetType> EQUALITY_NOT_APPLICABLE = DiagnosticFactory3.create(Severity.ERROR);
 
 	DiagnosticFactory2<NapileBinaryExpression, NapileBinaryExpression, Boolean> SENSELESS_COMPARISON = DiagnosticFactory2.create(Severity.WARNING);
 	SimpleDiagnosticFactory<NapileElement> SENSELESS_NULL_IN_WHEN = SimpleDiagnosticFactory.create(Severity.WARNING);
 
 	DiagnosticFactory2<PsiElement, CallableMemberDescriptor, DeclarationDescriptor> OVERRIDING_FINAL_MEMBER = DiagnosticFactory2.create(Severity.ERROR);
 	DiagnosticFactory3<NapileModifierListOwner, Visibility, CallableMemberDescriptor, DeclarationDescriptor> CANNOT_WEAKEN_ACCESS_PRIVILEGE = DiagnosticFactory3.create(Severity.ERROR, PositioningStrategies.VISIBILITY_MODIFIER);
 	DiagnosticFactory3<NapileModifierListOwner, Visibility, CallableMemberDescriptor, DeclarationDescriptor> CANNOT_CHANGE_ACCESS_PRIVILEGE = DiagnosticFactory3.create(Severity.ERROR, PositioningStrategies.VISIBILITY_MODIFIER);
 
 	DiagnosticFactory2<NapileNamedDeclaration, CallableMemberDescriptor, CallableMemberDescriptor> RETURN_TYPE_MISMATCH_ON_OVERRIDE = DiagnosticFactory2.create(Severity.ERROR, PositioningStrategies.DECLARATION_RETURN_TYPE);
 
 	DiagnosticFactory2<NapileProperty, PropertyDescriptor, PropertyDescriptor> VAR_OVERRIDDEN_BY_VAL = DiagnosticFactory2.create(Severity.ERROR, new PositioningStrategy<NapileProperty>()
 	{
 		@NotNull
 		@Override
 		public List<TextRange> mark(@NotNull NapileProperty property)
 		{
 			return markNode(property.getVarNode());
 		}
 	});
 
 	DiagnosticFactory2<PsiElement, NapileLikeClass, CallableMemberDescriptor> ABSTRACT_MEMBER_NOT_IMPLEMENTED = DiagnosticFactory2.create(Severity.ERROR);
 
 	DiagnosticFactory2<PsiElement, NapileLikeClass, CallableMemberDescriptor> MANY_IMPL_MEMBER_NOT_IMPLEMENTED = DiagnosticFactory2.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapilePropertyParameter> DEFAULT_VALUE_NOT_ALLOWED_IN_OVERRIDE = SimpleDiagnosticFactory.create(Severity.ERROR, PositioningStrategies.PARAMETER_DEFAULT_VALUE);
 	DiagnosticFactory1<NapilePropertyParameter, ParameterDescriptor> MULTIPLE_DEFAULTS_INHERITED_FROM_SUPERTYPES = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileLikeClass, ParameterDescriptor> MULTIPLE_DEFAULTS_INHERITED_FROM_SUPERTYPES_WHEN_NO_EXPLICIT_OVERRIDE = DiagnosticFactory1.create(Severity.ERROR, PositioningStrategies.NAME_IDENTIFIER);
 	DiagnosticFactory2<NapilePropertyParameter, ClassDescriptor, ParameterDescriptor> PARAMETER_NAME_CHANGED_ON_OVERRIDE = DiagnosticFactory2.create(Severity.WARNING, PositioningStrategies.NAME_IDENTIFIER);
 	DiagnosticFactory2<NapileLikeClass, Collection<? extends CallableMemberDescriptor>, Integer> DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES = DiagnosticFactory2.create(Severity.WARNING, PositioningStrategies.NAME_IDENTIFIER);
 
 	DiagnosticFactory2<NapileDeclaration, CallableMemberDescriptor, String> CONFLICTING_OVERLOADS = DiagnosticFactory2.create(Severity.ERROR, PositioningStrategies.DECLARATION);
 
 	DiagnosticFactory2<NapileReferenceExpression, NapileExpression, JetType> FUNCTION_EXPECTED = DiagnosticFactory2.create(Severity.ERROR);
 	DiagnosticFactory2<NapileExpression, NapileExpression, Boolean> FUNCTION_CALL_EXPECTED = DiagnosticFactory2.create(Severity.ERROR, PositioningStrategies.CALL_EXPRESSION);
 
 	DiagnosticFactory3<NapileExpression, String, JetType, JetType> RESULT_TYPE_MISMATCH = DiagnosticFactory3.create(Severity.ERROR);
 	DiagnosticFactory3<NapileReferenceExpression, String, String, String> UNSAFE_INFIX_CALL = DiagnosticFactory3.create(Severity.ERROR);
 
 	AmbiguousDescriptorDiagnosticFactory OVERLOAD_RESOLUTION_AMBIGUITY = new AmbiguousDescriptorDiagnosticFactory();
 	AmbiguousDescriptorDiagnosticFactory NONE_APPLICABLE = new AmbiguousDescriptorDiagnosticFactory();
 	DiagnosticFactory1<PsiElement, ParameterDescriptor> NO_VALUE_FOR_PARAMETER = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<NapileReferenceExpression, JetType> MISSING_RECEIVER = DiagnosticFactory1.create(Severity.ERROR);
 	SimpleDiagnosticFactory<NapileReferenceExpression> NO_RECEIVER_ADMITTED = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<PsiElement> CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS = SimpleDiagnosticFactory.create(Severity.ERROR);
 
 	DiagnosticFactory1<PsiElement, InferenceErrorData> TYPE_INFERENCE_NO_INFORMATION_FOR_PARAMETER = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<PsiElement, InferenceErrorData> TYPE_INFERENCE_CONFLICTING_SUBSTITUTIONS = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<PsiElement, InferenceErrorData> TYPE_INFERENCE_TYPE_CONSTRUCTOR_MISMATCH = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory1<PsiElement, InferenceErrorData> TYPE_INFERENCE_UPPER_BOUND_VIOLATED = DiagnosticFactory1.create(Severity.ERROR);
 	DiagnosticFactory2<PsiElement, JetType, JetType> TYPE_INFERENCE_EXPECTED_TYPE_MISMATCH = DiagnosticFactory2.create(Severity.ERROR);
 	Collection<AbstractDiagnosticFactory> TYPE_INFERENCE_ERRORS = Lists.<AbstractDiagnosticFactory>newArrayList(TYPE_INFERENCE_NO_INFORMATION_FOR_PARAMETER, TYPE_INFERENCE_CONFLICTING_SUBSTITUTIONS, TYPE_INFERENCE_TYPE_CONSTRUCTOR_MISMATCH, TYPE_INFERENCE_UPPER_BOUND_VIOLATED, TYPE_INFERENCE_EXPECTED_TYPE_MISMATCH);
 
 	DiagnosticFactory1<NapileElement, Integer> WRONG_NUMBER_OF_TYPE_ARGUMENTS = DiagnosticFactory1.create(Severity.ERROR);
 
 	SimpleDiagnosticFactory<NapileExpression> DANGLING_FUNCTION_LITERAL_ARGUMENT_SUSPECTED = SimpleDiagnosticFactory.create(Severity.WARNING);
 
 	DiagnosticFactory1<NapileAnnotationEntry, String> NOT_AN_ANNOTATION_CLASS = DiagnosticFactory1.create(Severity.ERROR);
 
 
 	// This field is needed to make the Initializer class load (interfaces cannot have static initializers)
 	@SuppressWarnings("UnusedDeclaration")
 	Initializer __initializer = Initializer.INSTANCE;
 
 	class Initializer
 	{
 		static
 		{
 			for(Field field : Errors.class.getFields())
 			{
 				if(Modifier.isStatic(field.getModifiers()))
 				{
 					try
 					{
 						Object value = field.get(null);
 						if(value instanceof AbstractDiagnosticFactory)
 						{
 							AbstractDiagnosticFactory factory = (AbstractDiagnosticFactory) value;
 							factory.setName(field.getName());
 						}
 					}
 					catch(IllegalAccessException e)
 					{
 						throw new IllegalStateException(e);
 					}
 				}
 			}
 		}
 
 		private static final Initializer INSTANCE = new Initializer();
 
 		private Initializer()
 		{
 		}
 	}
 }
