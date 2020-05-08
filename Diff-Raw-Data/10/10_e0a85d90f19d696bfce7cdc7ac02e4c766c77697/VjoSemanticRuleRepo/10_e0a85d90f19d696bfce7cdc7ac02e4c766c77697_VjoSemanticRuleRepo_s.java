 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules;
 
 
 import static org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticRulePolicy.GLOBAL_ERROR_POLICY;
 import static org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticRulePolicy.GLOBAL_FATAL_POLICY;
 import static org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticRulePolicy.GLOBAL_WARNING_POLICY;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.vjet.dsf.jsgen.shared.ids.FieldProbIds;
 import org.eclipse.vjet.dsf.jsgen.shared.ids.MethodProbIds;
 import org.eclipse.vjet.dsf.jsgen.shared.ids.TypeProbIds;
 import org.eclipse.vjet.dsf.jsgen.shared.ids.VarProbIds;
 import org.eclipse.vjet.dsf.jsgen.shared.ids.VjoSyntaxProbIds;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.IVjoSemanticRule;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.IVjoSemanticRuleSet;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticRuleSet;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.problem.ProblemErrorMessageBundle;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.BaseVjoSemanticRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.ClassBetterStartWithCapitalLetterRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.InvalidIdentifierNameRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.InvalidIdentifierNameWithKeywordRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.MTypeShouldNotBeAsInnerTypeRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.MTypeShouldNotHaveInnerTypesRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.MethodAndReturnFlowRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.TypeNameShouldNotBeEmptyRuleCtx;
 import org.eclipse.vjet.dsf.jst.JstProblemId;
 
 public class VjoSemanticRuleRepo {
 
 	private static final VjoSemanticRuleRepo s_instance = new VjoSemanticRuleRepo();
 	
 	public static synchronized final VjoSemanticRuleRepo getInstance(){
 		return s_instance;
 	}
 
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> QUALIFIER_SHOULD_NOT_BE_NULL = new QualifierShouldNotBeNullRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> QUALIFIER_SHOULD_BE_DEFINED = new QualifierShouldBeDefinedRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> PROPERTY_SHOULD_BE_DEFINED = new PropertyShouldBeDefinedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> PROPERTY_SHOULD_BE_VISIBLE = new PropertyShouldBeVisibleRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> PROPERTY_SHOULD_NOT_HIDE_PARENT_PROPERTY = new PropertyShouldNotHideParentPropertyRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE = new StaticPropertyShouldNotBeAccessedFromNoneStaticScopeRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> NONE_STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE = new NoneStaticPropertyShouldNotBeAccessedFromStaticScopeRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> STATIC_REFERENCE_TO_NON_STATIC_TYPE = new StaticReferenceToNonStaticType();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> METHOD_SHOULD_BE_DEFINED = new MethodShouldBeDefinedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> INFERRED_TYPE_METHOD_SHOULD_BE_DEFINED = new MethodShouldBeDefinedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> FUNCTION_SHOULD_BE_DEFINED = new FunctionShouldBeDefinedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> METHOD_SHOULD_BE_VISIBLE = new MethodShouldBeVisibleRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CONSTRUCTOR_SHOULD_BE_VISIBLE = new ConstructorShouldBeVisibleRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> NONE_VOID_METHOD_SHOULD_HAVE_RETURN = new NoneVoidMethodShouldHaveReturnRule();
 	public final IVjoSemanticRule<MethodAndReturnFlowRuleCtx> VOID_METHOD_SHOULD_NOT_HAVE_RETURN = new VoidMethodShouldNotHaveReturnRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> METHOD_RETURN_VALUE_SHOULD_COMPLY = new MethodReturnValueShouldComplyRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> THROW_TYPE_SHOULD_COMPLY = new ThrowTypeShouldComplyRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> METHOD_ARGS_TYPE_SHOULD_MATCH = new MethodArgsTypeShouldMatchRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> METHOD_ARGS_TYPE_SHOULD_NOT_BE_VOID = new MethodArgsTypeShouldNotBeVoid();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> METHOD_WRONG_NUMBER_OF_ARGS = new MethodWrongNumberOfArgsRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE = new StaticMethodShouldNotBeAccessedFromNoneStaticScopeRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> NONE_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE = new NoneStaticMethodShouldNotBeAccessedFromStaticScopeRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> DUPLICATE_METHOD = new DuplicateMethodRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> DUPLICATE_PROPERTY = new DuplicatePropertyRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OVERLAP_STATIC_AND_NONE_STATIC_METHOD = new OverlapStaticAndNonStaticMethodRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OVERLAP_STATIC_AND_NONE_STATIC_PROPERTY = new OverlapStaticAndNonStaticPropertyRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ABSTRACT_METHOD_MUST_BE_IMPLEMENTED = new AbstractMethodMustBeImplementedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> FINAL_METHOD_SHOULD_NOT_BE_OVERRIDEN = new FinalMethodShouldNotBeOverridenRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> STATIC_METHOD_SHOULD_NOT_BE_OVERRIDEN = new StaticMethodShouldNotBeOverridenRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> SUPER_CLASS_MUST_PROVIDE_CONSTRUCTOR = new SuperClassMustProvideConstructorRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> SUPER_CLASS_SHOULD_NOT_BE_FINAL = new SuperClassShouldNotBeFinalRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> SUPER_CLASS_SHOULD_NOT_BE_THE_SAME = new SuperClassShouldNotBeFinalRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CLASS_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT = new ClassShouldNotBeBothFinalAndAbstractRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> INTERFACE_SHOULD_NOT_BE_FINAL = new ClassShouldNotBeBothFinalAndAbstractRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT = new MemberShouldNotBeBothFinalAndAbstractRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_STATIC_AND_ABSTRACT = new MemberShouldNotBeBothStaticAndAbstractRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_PRIVATE_AND_ABSTRACT = new MemberShouldNotBeBothPrivateAndAbstractRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CLASS_SHOULD_BE_ABSTRACT = new ClassShouldBeAbstractRule();
 	public final IVjoSemanticRule<ClassBetterStartWithCapitalLetterRuleCtx> CLASS_BETTER_START_WITH_NONE_CAPITAL_LETTER = new ClassBetterStartWithCapitalLetterRule();
 	public final IVjoSemanticRule<TypeNameShouldNotBeEmptyRuleCtx> TYPE_NAME_SHOULD_NOT_BE_EMPTY = new TypeNameShouldNotBeEmptyRule();
 	
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ETYPE_SHOULD_NOT_BE_ABSTRACT_OR_PRIVATE_OR_PROTECTED = new ETypeShouldNotBeAbstractOrPrivateOrProtectedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ETYPE_SHOULD_NOT_DEFINE_PUBLIC_OR_PROTECTED_CONSTRUCTOR = new ETypeShouldNotDefinePublicOrProtectedConstructorRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ETYPE_SHOULD_NOT_DEFINE_ABSTRACT_METHOD_OR_PROPERTY = new ETypeShouldNotDefineAbstractMethodRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ETYPE_SHOULD_NOT_HAVE_DUP_ENUM_VALUES = new ETypeShouldNotHaveDupEnumValuesRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ETYPE_VALUES_MUST_BE_ARRAY = new ETypeValuesShouldArrayRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ITYPE_OR_MTYPE_SHOULD_NOT_HAVE_CONSTRUCTOR = new ITypeOrMTypeShouldNotHaveConstructorRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ITYPE_SHOULD_NOT_DEFINE_STATIC_METHODS = new ITypeShouldNotDefineStaticMethodsRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ABSTRACT_MEMBER_SHOULD_NOT_HAVE_DEFINITION = new AbstractMemberShouldNotHaveDefinitionRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OTYPE_SHOULD_NOT_HAVE_NONE_OBJ_LITERAL_PROPERTY = new OTypeShouldNotHaveNoneObjLiteralPropertyRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OTYPE_SHOULD_NOT_HAVE_INNER_TYPES = new OTypeShouldNotHaveInnerTypesRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OTYPE_SHOULD_NOT_BE_AS_INNER_TYPE = new OTypeShouldNotBeAsInnerTypeRule();
 	public final IVjoSemanticRule<MTypeShouldNotHaveInnerTypesRuleCtx> MTYPE_SHOULD_NOT_HAVE_INNER_TYPES = new MTypeShouldNotHaveInnerTypesRule();
 	public final IVjoSemanticRule<MTypeShouldNotBeAsInnerTypeRuleCtx> MTYPE_SHOULD_NOT_BE_AS_INNER_TYPE = new MTypeShouldNotBeAsInnerTypeRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ITYPE_SHOULD_NOT_HAVE_INSTANCE_PROPERTY = new ITypeShouldNotHaveInstancePropertyRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ITYPE_SHOULD_NOT_EXTEND_NONE_ITYPE_CLASS = new ITypeShouldNotExtendFromNoneITypeClassRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ITYPE_ALLOWS_ONLY_PUBLIC_MODIFIER = new ITypeAllowsOnlyPublicModifierRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CTYPE_SHOULD_NOT_EXTEND_NONE_CTYPE_CLASS = new CTypeShouldNotExtendFromNoneCTypeClassRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CTYPE_SHOULD_NOT_IMPLEMENT_NONE_ITYPE_INTERFACE = new CTypeShouldNotImplementNoneITypeInterfaceRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OVERRIDE_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE = new OverrideMethodShouldHaveCompatibleSignatureRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> IMPLEMENT_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE = new ImplementMethodShouldHaveCompatibleSignatureRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MIXIN_EXPECTS_STATIC_METHOD_MUST_BE_SATISFIED = new MixinExpectsMustBeSatisfiedRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MIXIN_EXPECTS_STATIC_PROP_MUST_BE_SATISFIED = new MixinExpectsMustBeSatisfiedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MIXIN_EXPECTS_INSTANCE_METHOD_MUST_BE_SATISFIED = new MixinExpectsMustBeSatisfiedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MIXIN_EXPECTS_INSTANCE_PROP_MUST_BE_SATISFIED = new MixinExpectsMustBeSatisfiedRule();
 	
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MIXIN_EXPECTS_MUST_BE_SATISFIED = new MixinExpectsMustBeSatisfiedRule();
     public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> EXPECTS_MUST_BE_CTYPE_ITYPE = new MixinExpectMustBeCtypeMtype();
 
     public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MIXINED_TYPE_MUST_NOT_BE_ITSELF = new MixinedTypeMustNotBeItSelf();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MTYPE_EXPECTS_SHOULD_NOT_BE_OVERWRITTEN = new MTypeExpectsShouldNotBeOverwrittenRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MTYPE_SHOULD_NOT_BE_REFERENCED_DIRECTLY = new MTypeShouldNotBeReferencedDirectlyRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MTYPE_SHOULD_ONLY_BE_MIXINED = new MTypeShouldOnlyBeMixinedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> INNER_TYPE_SHOULD_NOT_HAVE_NAME_SAME_AS_EMBEDDING_TYPE = new InnerTypeShouldNotHaveSameNameAsEmbeddingTypeRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> INSTANCE_INNER_TYPE_SHOULD_NOT_HAVE_STATIC_MEMBERS = new InstanceInnerTypeShouldNotHaveStaticMembersRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OVERLOAD_METHOD_SHOULD_NOT_OVERLAP = new OverloadMethodShouldNotOverlapRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OVERLOAD_METHOD_SHOULD_NOT_HAVE_VARIABLE_MODIFIER = new OverloadMethodShouldNotHaveVariableModifierRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OVERRIDE_METHOD_SHOULD_NOT_REDUCE_VISIBILITY = new OverrideMethodShouldNotReduceVisibilityRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> VARIABLE_ALREADY_DEFINED = new VariableAlreadyDefinedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> VARIABLE_SHOULD_BE_DEFINED = new VariableShouldBeDefinedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> IMPLICIT_GLOBAL_VARIABLE_DECLARED = new ImplicitGlobalVariableDeclaredRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CLASS_OBJ_SHOULD_BE_DEFINED = new ClassObjShouldBeDefinedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> PRIVATE_METHOD_REFERENCED_NOWHERE = new PrivateMethodInvokedNowhereRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> PRIVATE_PROPERTY_REFERENCED_NOWHERE = new PrivatePropertyReferencedNowhereRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ARRAY_INDEX_SHOULD_BE_INT_OR_STRING_TYPE = new ArrayIndexShouldBeIntOrStringTypeRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ARRAY_CREATION_VAL_SHOULD_BE_DEFINED = new ArrayCreationValShouldBeDefinedRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ITEM_TYPE_SHOULD_MATCH_ARRAY_TYPE = new ItemTypeShouldMatchArrayTypeRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ARRAY_CREATION_DIMENSION_TYPE_SHOULD_BE_INTEGER = new ArrayCreationDimensionTypeShouldBeIntegerRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OBJ_SHOULD_BE_CLASS_TYPE = new ObjShouldBeClassTypeRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CLASS_SHOULD_BE_INSTANTIATABLE = new ClassShouldBeInstantiatableRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> FINAL_PROPERTY_SHOULD_BE_INITIALIZED = new FinalPropertyShouldBeInitializedRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ASSIGNABLE = new AssignableRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OBJLITERAL_ASSIGNABLE = new ObjLiteralAssignableRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> SHOULD_NOT_ASSIGN_TO_ENUM = new ShouldNotAssignToEnumRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> SHOULD_NOT_ASSIGN_TO_FINAL = new ShouldNotAssignToFinalRule();
 //	public final IVjoSemanticRule<CastableRuleCtx> CASTABLE = new CastableRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> DUPLICATE_LABEL = new LabelNameShoudBeUniqueRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> INVALID_LABEL_DECORATION = new LabelShouldDecorateLoopStmtRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> BREAK_NONE_EXIST_LABEL = new BreakLabelShouldExistRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CONTINUE_NONE_EXIST_LABEL = new ContinueLabelShouldExistRule();
 	
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> STMT_SHOULD_BE_REACHABLE = new StmtShouldBeReachableRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> EXPR_SHOULD_BE_BOOL = new ExprShouldBeBoolRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> EXPR_SHOULD_BE_BOOL_OR_NUMBER = new ExprShouldBeBoolOrNumberRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> EXPR_SHOULD_BE_NUMBER = new ExprShouldBeNumberRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> EXPR_SHOULD_BE_NUMBER_OR_STRING = new ExprShouldBeNumberOrStringRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> TWO_EXPRS_SHOULD_BE_CONSISTENT = new TwoExprsShouldBeConsistentRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> PACKAGE_SHOULD_BE_DEFINED = new PackageShouldBeDefinedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> UNKNOWN_TYPE_MISSING_IMPORT = new UnknownTypeMissingImportRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> UNKNOWN_TYPE_NOT_IN_TYPE_SPACE = new UnknownTypeNotInTypeSpaceRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> UNKNOWN_TYPE_NOT_IN_TYPE_SPACE_INACTIVENEEDS = new UnknownTypeNotInInactiveNeedsRule();
 	
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> WITH_SCOPE_SHOULD_NOT_CONFLICT_LOCAL = new WithScopeShouldNotConflictLocalRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> DISCOURAGED_NESTED_WITH = new NestedWithDiscouragedRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> OBJECT_LITERAL_SHOULD_HAVE_UNIQUE_KEY = new ObjectLiteralShouldHaveUniqueKeyRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> VJO_SYNTAX_CORRECTNESS = new VjoSyntaxNotCorrectRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MISSING_ENDTYPE = new VjoMissingEndTypeRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MULTIPLE_INHERITS = new VjoMultipleInheritsRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MULTIPLE_PROTOS = new VjoMultipleProtosRule();
 //	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MULTIPLE_PROPS = new VjoMultiplePropsRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> NAME_SPACE_COLLISION = new VjoNameSpaceCollisionRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> REDUNDANT_IMPORT = new VjoRedundantImportRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> PUBLIC_CLASS_SHOULD_RESIDE_IN_CORRESPONDING_FILE = new VjoPublicClassNotInCorrespondingFileRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> MAIN_CLASS_SHOULD_BE_PUBLIC = new VjoMainClassShouldBePublicRule();
 	public final IVjoSemanticRule<InvalidIdentifierNameRuleCtx> INVALID_IDENTIFIER = new VjoInvalidIdentifierRule();
 	public final IVjoSemanticRule<InvalidIdentifierNameWithKeywordRuleCtx> INVALID_IDENTIFIER_WITH_KEYWORD = new VjoInvalidIdentifierWithKeywordRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CLASS_NAME_CANNOT_HAVE_ILLEGAL_TOKEN = new VjoClassNameCannotHaveNumericTokenRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CANNOT_USE_UNINTIALIZED_TYPE = new CannotUseUninitializedTypeRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> CANNOT_USE_INACTIVE_NEED_ACTIVELY = new CannotUseInactiveNeedActivelyRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> UNUSED_ACTIVE_NEEDS = new UnusedActiveNeedsRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> GENERIC_PARAM_TYPE_MISMATCH = new GenericParamTypeMismatchRule();
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> GENERIC_PARAM_NUM_MISMATCH = new GenericParamNumMismatchRule();
 	
 	//attributed type rule added
 	public final IVjoSemanticRule<BaseVjoSemanticRuleCtx> ATTRIBUTOR_SHOULD_NOT_USE_VJ_RUNTIME = new AttributorShouldNotUseVjRuntimeRule();
 	
 	public final IVjoSemanticRuleSet TYPE_CHECK = new VjoSemanticRuleSet();
 	public final IVjoSemanticRuleSet GLOBAL_RULES = new VjoSemanticRuleSet();
 //	public final IVjoSemanticRuleSet HIERARCHY_AND_STRUCTURE = new VjoSemanticRuleSet();
 //	public final IVjoSemanticRuleSet UNIQUENESS = new VjoSemanticRuleSet();
 	public final IVjoSemanticRuleSet JAVASCRIPT_EXTENSIONS = new VjoSemanticRuleSet();
 	public final IVjoSemanticRuleSet JAVA_COMPAT_TYPE_CHECK = new VjoSemanticRuleSet();
 //	public final IVjoSemanticRuleSet ACCESSIBILITY = new VjoSemanticRuleSet();
 //	public final IVjoSemanticRuleSet MISC = new VjoSemanticRuleSet();
 	public final IVjoSemanticRuleSet VJO_SYNTAX = new VjoSemanticRuleSet();
 
 	private Map<String, IVjoSemanticRuleSet> m_ruleSetMap;
 	
 	private VjoSemanticRuleRepo(){
 		initRuleSets();
 		initRules();
 		categorizeRules();
 		initRulePolicies();
 	}
 	
 	public void restoreDefaultPolicies(){
 		initRulePolicies();
 	}
 	
 	private void initRulePolicies(){
 //		QUALIFIER_SHOULD_NOT_BE_NULL.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		QUALIFIER_SHOULD_BE_DEFINED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		PROPERTY_SHOULD_BE_DEFINED.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		PROPERTY_SHOULD_BE_VISIBLE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		PROPERTY_SHOULD_NOT_HIDE_PARENT_PROPERTY.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		NONE_STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		STATIC_REFERENCE_TO_NON_STATIC_TYPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		METHOD_SHOULD_BE_DEFINED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		INFERRED_TYPE_METHOD_SHOULD_BE_DEFINED.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		FUNCTION_SHOULD_BE_DEFINED.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		METHOD_SHOULD_BE_VISIBLE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		CONSTRUCTOR_SHOULD_BE_VISIBLE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		NONE_VOID_METHOD_SHOULD_HAVE_RETURN.setGlobalPolicy(GLOBAL_WARNING_POLICY); 
 		VOID_METHOD_SHOULD_NOT_HAVE_RETURN.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		METHOD_RETURN_VALUE_SHOULD_COMPLY.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		THROW_TYPE_SHOULD_COMPLY.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		METHOD_ARGS_TYPE_SHOULD_MATCH.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		METHOD_ARGS_TYPE_SHOULD_NOT_BE_VOID.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		METHOD_WRONG_NUMBER_OF_ARGS.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		NONE_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		DUPLICATE_METHOD.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		DUPLICATE_PROPERTY.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		OVERLAP_STATIC_AND_NONE_STATIC_METHOD.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		OVERLAP_STATIC_AND_NONE_STATIC_PROPERTY.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		ABSTRACT_METHOD_MUST_BE_IMPLEMENTED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		FINAL_METHOD_SHOULD_NOT_BE_OVERRIDEN.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		STATIC_METHOD_SHOULD_NOT_BE_OVERRIDEN.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		SUPER_CLASS_MUST_PROVIDE_CONSTRUCTOR.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		SUPER_CLASS_SHOULD_NOT_BE_FINAL.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		SUPER_CLASS_SHOULD_NOT_BE_THE_SAME.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		CLASS_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		INTERFACE_SHOULD_NOT_BE_FINAL.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_STATIC_AND_ABSTRACT.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_PRIVATE_AND_ABSTRACT.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		CLASS_SHOULD_BE_ABSTRACT.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		CLASS_BETTER_START_WITH_NONE_CAPITAL_LETTER.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		TYPE_NAME_SHOULD_NOT_BE_EMPTY.setGlobalPolicy(GLOBAL_FATAL_POLICY);
 		ETYPE_SHOULD_NOT_BE_ABSTRACT_OR_PRIVATE_OR_PROTECTED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ETYPE_SHOULD_NOT_DEFINE_PUBLIC_OR_PROTECTED_CONSTRUCTOR.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ETYPE_SHOULD_NOT_DEFINE_ABSTRACT_METHOD_OR_PROPERTY.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ETYPE_SHOULD_NOT_HAVE_DUP_ENUM_VALUES.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ETYPE_VALUES_MUST_BE_ARRAY.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ITYPE_OR_MTYPE_SHOULD_NOT_HAVE_CONSTRUCTOR.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ITYPE_SHOULD_NOT_DEFINE_STATIC_METHODS.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ABSTRACT_MEMBER_SHOULD_NOT_HAVE_DEFINITION.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		OTYPE_SHOULD_NOT_HAVE_NONE_OBJ_LITERAL_PROPERTY.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		OTYPE_SHOULD_NOT_HAVE_INNER_TYPES.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		OTYPE_SHOULD_NOT_BE_AS_INNER_TYPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		MTYPE_SHOULD_NOT_HAVE_INNER_TYPES.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		MTYPE_SHOULD_NOT_BE_AS_INNER_TYPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ITYPE_SHOULD_NOT_HAVE_INSTANCE_PROPERTY.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ITYPE_SHOULD_NOT_EXTEND_NONE_ITYPE_CLASS.setGlobalPolicy(GLOBAL_ERROR_POLICY);
         EXPECTS_MUST_BE_CTYPE_ITYPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ITYPE_ALLOWS_ONLY_PUBLIC_MODIFIER.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		CTYPE_SHOULD_NOT_EXTEND_NONE_CTYPE_CLASS.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		CTYPE_SHOULD_NOT_IMPLEMENT_NONE_ITYPE_INTERFACE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		OVERRIDE_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 //		IMPLEMENT_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 
 //		MIXIN_EXPECTS_STATIC_METHOD_MUST_BE_SATISFIED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		MIXIN_EXPECTS_STATIC_PROP_MUST_BE_SATISFIED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		MIXIN_EXPECTS_INSTANCE_METHOD_MUST_BE_SATISFIED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		MIXIN_EXPECTS_INSTANCE_PROP_MUST_BE_SATISFIED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 				
         MIXINED_TYPE_MUST_NOT_BE_ITSELF.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		MTYPE_EXPECTS_SHOULD_NOT_BE_OVERWRITTEN.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		MTYPE_SHOULD_NOT_BE_REFERENCED_DIRECTLY.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		MTYPE_SHOULD_ONLY_BE_MIXINED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		INNER_TYPE_SHOULD_NOT_HAVE_NAME_SAME_AS_EMBEDDING_TYPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		INSTANCE_INNER_TYPE_SHOULD_NOT_HAVE_STATIC_MEMBERS.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		OVERLOAD_METHOD_SHOULD_NOT_OVERLAP.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 //		OVERLOAD_METHOD_SHOULD_NOT_HAVE_VARIABLE_MODIFIER.setGlobalPolicy(GLOBAL_ERROR_POLICY);
		OVERRIDE_METHOD_SHOULD_NOT_REDUCE_VISIBILITY.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		VARIABLE_ALREADY_DEFINED.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		VARIABLE_SHOULD_BE_DEFINED.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		IMPLICIT_GLOBAL_VARIABLE_DECLARED.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 //		CLASS_OBJ_SHOULD_BE_DEFINED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		ARRAY_INDEX_SHOULD_BE_INT_OR_STRING_TYPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		ARRAY_CREATION_VAL_SHOULD_BE_DEFINED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		ITEM_TYPE_SHOULD_MATCH_ARRAY_TYPE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		ARRAY_CREATION_DIMENSION_TYPE_SHOULD_BE_INTEGER.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		OBJ_SHOULD_BE_CLASS_TYPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		CLASS_SHOULD_BE_INSTANTIATABLE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		FINAL_PROPERTY_SHOULD_BE_INITIALIZED.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		ASSIGNABLE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		OBJLITERAL_ASSIGNABLE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		SHOULD_NOT_ASSIGN_TO_ENUM.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		SHOULD_NOT_ASSIGN_TO_FINAL.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		CASTABLE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		EXPR_SHOULD_BE_BOOL.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		DUPLICATE_LABEL.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		INVALID_LABEL_DECORATION.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		BREAK_NONE_EXIST_LABEL.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		CONTINUE_NONE_EXIST_LABEL.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		STMT_SHOULD_BE_REACHABLE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		EXPR_SHOULD_BE_BOOL_OR_NUMBER.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		EXPR_SHOULD_BE_NUMBER.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 //		EXPR_SHOULD_BE_NUMBER_OR_STRING.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 //		TWO_EXPRS_SHOULD_BE_CONSISTENT.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 //		PACKAGE_SHOULD_BE_DEFINED.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		UNKNOWN_TYPE_MISSING_IMPORT.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		UNKNOWN_TYPE_NOT_IN_TYPE_SPACE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		UNKNOWN_TYPE_NOT_IN_TYPE_SPACE_INACTIVENEEDS.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 //		WITH_SCOPE_SHOULD_NOT_CONFLICT_LOCAL.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		DISCOURAGED_NESTED_WITH.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		OBJECT_LITERAL_SHOULD_HAVE_UNIQUE_KEY.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		VJO_SYNTAX_CORRECTNESS.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		INVALID_IDENTIFIER.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		INVALID_IDENTIFIER_WITH_KEYWORD.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		CLASS_NAME_CANNOT_HAVE_ILLEGAL_TOKEN.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		MISSING_ENDTYPE.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		MULTIPLE_INHERITS.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		MULTIPLE_PROPS.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 //		MULTIPLE_PROTOS.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 		NAME_SPACE_COLLISION.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		REDUNDANT_IMPORT.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		PRIVATE_METHOD_REFERENCED_NOWHERE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		PRIVATE_PROPERTY_REFERENCED_NOWHERE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		PUBLIC_CLASS_SHOULD_RESIDE_IN_CORRESPONDING_FILE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		MAIN_CLASS_SHOULD_BE_PUBLIC.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		CANNOT_USE_UNINTIALIZED_TYPE.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		CANNOT_USE_INACTIVE_NEED_ACTIVELY.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		UNUSED_ACTIVE_NEEDS.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		GENERIC_PARAM_TYPE_MISMATCH.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		GENERIC_PARAM_NUM_MISMATCH.setGlobalPolicy(GLOBAL_WARNING_POLICY);
 		
 		ATTRIBUTOR_SHOULD_NOT_USE_VJ_RUNTIME.setGlobalPolicy(GLOBAL_ERROR_POLICY);
 	}
 
 	private void categorizeRules() {
 		TYPE_CHECK.addRule(ASSIGNABLE);
 		TYPE_CHECK.addRule(OBJLITERAL_ASSIGNABLE);
 //		TYPE_CHECK.addRule(CASTABLE);
 		TYPE_CHECK.addRule(EXPR_SHOULD_BE_BOOL);
 		TYPE_CHECK.addRule(EXPR_SHOULD_BE_BOOL_OR_NUMBER);
 		TYPE_CHECK.addRule(EXPR_SHOULD_BE_NUMBER);
 //		TYPE_CHECK.addRule(EXPR_SHOULD_BE_NUMBER_OR_STRING);
 //		TYPE_CHECK.addRule(TWO_EXPRS_SHOULD_BE_CONSISTENT);
 		TYPE_CHECK.addRule(STATIC_REFERENCE_TO_NON_STATIC_TYPE);
 		TYPE_CHECK.addRule(METHOD_ARGS_TYPE_SHOULD_MATCH);
 		TYPE_CHECK.addRule(METHOD_ARGS_TYPE_SHOULD_NOT_BE_VOID);
 		TYPE_CHECK.addRule(ARRAY_CREATION_DIMENSION_TYPE_SHOULD_BE_INTEGER);
 //		TYPE_CHECK.addRule(ITEM_TYPE_SHOULD_MATCH_ARRAY_TYPE);
 		TYPE_CHECK.addRule(ARRAY_INDEX_SHOULD_BE_INT_OR_STRING_TYPE);
 		TYPE_CHECK.addRule(METHOD_RETURN_VALUE_SHOULD_COMPLY);
 		TYPE_CHECK.addRule(THROW_TYPE_SHOULD_COMPLY);
 		TYPE_CHECK.addRule(OBJ_SHOULD_BE_CLASS_TYPE);
 		TYPE_CHECK.addRule(CLASS_SHOULD_BE_INSTANTIATABLE);
 		TYPE_CHECK.addRule(GENERIC_PARAM_TYPE_MISMATCH);
 		TYPE_CHECK.addRule(GENERIC_PARAM_NUM_MISMATCH);
 		
 //		HIERARCHY_AND_STRUCTURE.addRule(SUPER_CLASS_MUST_PROVIDE_CONSTRUCTOR);
 		TYPE_CHECK.addRule(SUPER_CLASS_SHOULD_NOT_BE_FINAL);
 		TYPE_CHECK.addRule(SUPER_CLASS_SHOULD_NOT_BE_THE_SAME);
 		TYPE_CHECK.addRule(CLASS_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT);
 		TYPE_CHECK.addRule(INTERFACE_SHOULD_NOT_BE_FINAL);
 		TYPE_CHECK.addRule(METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT);
 		TYPE_CHECK.addRule(METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_STATIC_AND_ABSTRACT);
 		TYPE_CHECK.addRule(METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_PRIVATE_AND_ABSTRACT);
 		TYPE_CHECK.addRule(CLASS_SHOULD_BE_ABSTRACT);
 		TYPE_CHECK.addRule(ETYPE_SHOULD_NOT_BE_ABSTRACT_OR_PRIVATE_OR_PROTECTED);
 		TYPE_CHECK.addRule(ETYPE_SHOULD_NOT_DEFINE_PUBLIC_OR_PROTECTED_CONSTRUCTOR);
 		TYPE_CHECK.addRule(ETYPE_SHOULD_NOT_DEFINE_ABSTRACT_METHOD_OR_PROPERTY);
 		TYPE_CHECK.addRule(ITYPE_OR_MTYPE_SHOULD_NOT_HAVE_CONSTRUCTOR);
 		TYPE_CHECK.addRule(ITYPE_SHOULD_NOT_DEFINE_STATIC_METHODS);
 		TYPE_CHECK.addRule(ITYPE_SHOULD_NOT_EXTEND_NONE_ITYPE_CLASS);
 		TYPE_CHECK.addRule(CTYPE_SHOULD_NOT_EXTEND_NONE_CTYPE_CLASS);
 		TYPE_CHECK.addRule(CTYPE_SHOULD_NOT_IMPLEMENT_NONE_ITYPE_INTERFACE);
 		TYPE_CHECK.addRule(OVERRIDE_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE);
 //		HIERARCHY_AND_STRUCTURE.addRule(IMPLEMENT_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE);
 		TYPE_CHECK.addRule(MIXIN_EXPECTS_INSTANCE_METHOD_MUST_BE_SATISFIED);
 		TYPE_CHECK.addRule(MIXIN_EXPECTS_INSTANCE_PROP_MUST_BE_SATISFIED);
 //		HIERARCHY_AND_STRUCTURE.addRule(MIXIN_EXPECTS_STATIC_METHOD_MUST_BE_SATISFIED);
 //		HIERARCHY_AND_STRUCTURE.addRule(MIXIN_EXPECTS_STATIC_PROP_MUST_BE_SATISFIED);
 //		HIERARCHY_AND_STRUCTURE.addRule(MTYPE_EXPECTS_SHOULD_NOT_BE_OVERWRITTEN);
 		TYPE_CHECK.addRule(MIXINED_TYPE_MUST_NOT_BE_ITSELF);
 //		HIERARCHY_AND_STRUCTURE.addRule(MTYPE_SHOULD_NOT_BE_REFERENCED_DIRECTLY);
 		TYPE_CHECK.addRule(MTYPE_SHOULD_ONLY_BE_MIXINED);
 		TYPE_CHECK.addRule(EXPECTS_MUST_BE_CTYPE_ITYPE);
 		TYPE_CHECK.addRule(INNER_TYPE_SHOULD_NOT_HAVE_NAME_SAME_AS_EMBEDDING_TYPE);
 		TYPE_CHECK.addRule(INSTANCE_INNER_TYPE_SHOULD_NOT_HAVE_STATIC_MEMBERS);
 		TYPE_CHECK.addRule(OVERRIDE_METHOD_SHOULD_NOT_REDUCE_VISIBILITY);
 		TYPE_CHECK.addRule(ABSTRACT_METHOD_MUST_BE_IMPLEMENTED);
 		TYPE_CHECK.addRule(FINAL_METHOD_SHOULD_NOT_BE_OVERRIDEN);
 		TYPE_CHECK.addRule(STATIC_METHOD_SHOULD_NOT_BE_OVERRIDEN);
 		
 //		MISC.addRule(QUALIFIER_SHOULD_NOT_BE_NULL);
 //		MISC.addRule(QUALIFIER_SHOULD_BE_DEFINED);
 		JAVASCRIPT_EXTENSIONS.addRule(PROPERTY_SHOULD_BE_DEFINED);
 		JAVASCRIPT_EXTENSIONS.addRule(METHOD_SHOULD_BE_DEFINED);
 		JAVASCRIPT_EXTENSIONS.addRule(INFERRED_TYPE_METHOD_SHOULD_BE_DEFINED);
 		JAVASCRIPT_EXTENSIONS.addRule(FUNCTION_SHOULD_BE_DEFINED);
 		JAVASCRIPT_EXTENSIONS.addRule(METHOD_WRONG_NUMBER_OF_ARGS);
 		JAVASCRIPT_EXTENSIONS.addRule(NONE_VOID_METHOD_SHOULD_HAVE_RETURN);
 		JAVASCRIPT_EXTENSIONS.addRule(VOID_METHOD_SHOULD_NOT_HAVE_RETURN);
 //		MISC.addRule(ARRAY_CREATION_VAL_SHOULD_BE_DEFINED);
 //		MISC.addRule(CLASS_OBJ_SHOULD_BE_DEFINED);
 		TYPE_CHECK.addRule(FINAL_PROPERTY_SHOULD_BE_INITIALIZED);
 		JAVASCRIPT_EXTENSIONS.addRule(VARIABLE_SHOULD_BE_DEFINED);
 		GLOBAL_RULES.addRule(IMPLICIT_GLOBAL_VARIABLE_DECLARED);
 		TYPE_CHECK.addRule(SHOULD_NOT_ASSIGN_TO_ENUM);
 		TYPE_CHECK.addRule(SHOULD_NOT_ASSIGN_TO_FINAL);
 		TYPE_CHECK.addRule(PRIVATE_METHOD_REFERENCED_NOWHERE);
 		TYPE_CHECK.addRule(PRIVATE_PROPERTY_REFERENCED_NOWHERE);
 		TYPE_CHECK.addRule(OVERLOAD_METHOD_SHOULD_NOT_OVERLAP);
 //		MISC.addRule(OVERLOAD_METHOD_SHOULD_NOT_HAVE_VARIABLE_MODIFIER);
 		JAVASCRIPT_EXTENSIONS.addRule(STMT_SHOULD_BE_REACHABLE);
 		TYPE_CHECK.addRule(CANNOT_USE_UNINTIALIZED_TYPE);
 		TYPE_CHECK.addRule(CANNOT_USE_INACTIVE_NEED_ACTIVELY);
 		TYPE_CHECK.addRule(UNUSED_ACTIVE_NEEDS);
 //		MISC.addRule(INVALID_LABEL_DECORATION);
 		JAVASCRIPT_EXTENSIONS.addRule(BREAK_NONE_EXIST_LABEL);
 		JAVASCRIPT_EXTENSIONS.addRule(CONTINUE_NONE_EXIST_LABEL);
 		TYPE_CHECK.addRule(CLASS_BETTER_START_WITH_NONE_CAPITAL_LETTER);
 		TYPE_CHECK.addRule(TYPE_NAME_SHOULD_NOT_BE_EMPTY);
 		JAVASCRIPT_EXTENSIONS.addRule(DISCOURAGED_NESTED_WITH);
 		VJO_SYNTAX.addRule(ATTRIBUTOR_SHOULD_NOT_USE_VJ_RUNTIME);
 		
 		JAVASCRIPT_EXTENSIONS.addRule(DUPLICATE_LABEL);
 		JAVASCRIPT_EXTENSIONS.addRule(OBJECT_LITERAL_SHOULD_HAVE_UNIQUE_KEY);
 		JAVASCRIPT_EXTENSIONS.addRule(VARIABLE_ALREADY_DEFINED);
 		
 //		UNIQUENESS.addRule(WITH_SCOPE_SHOULD_NOT_CONFLICT_LOCAL);
 		TYPE_CHECK.addRule(DUPLICATE_METHOD);
 		TYPE_CHECK.addRule(DUPLICATE_PROPERTY);
 		TYPE_CHECK.addRule(OVERLAP_STATIC_AND_NONE_STATIC_METHOD);
 		TYPE_CHECK.addRule(OVERLAP_STATIC_AND_NONE_STATIC_PROPERTY);
 		TYPE_CHECK.addRule(ETYPE_SHOULD_NOT_HAVE_DUP_ENUM_VALUES);
 		
 		JAVA_COMPAT_TYPE_CHECK.addRule(PROPERTY_SHOULD_NOT_HIDE_PARENT_PROPERTY);
 		
 //		ACCESSIBILITY.addRule(PACKAGE_SHOULD_BE_DEFINED);
 		TYPE_CHECK.addRule(PROPERTY_SHOULD_BE_VISIBLE);
 		TYPE_CHECK.addRule(STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE);
 		TYPE_CHECK.addRule(NONE_STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE);
 		TYPE_CHECK.addRule(METHOD_SHOULD_BE_VISIBLE);
 		TYPE_CHECK.addRule(CONSTRUCTOR_SHOULD_BE_VISIBLE);
 		TYPE_CHECK.addRule(STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE);
 		TYPE_CHECK.addRule(NONE_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE);
 		
 		VJO_SYNTAX.addRule(VJO_SYNTAX_CORRECTNESS);
 		VJO_SYNTAX.addRule(MISSING_ENDTYPE);
 //		VJO_SYNTAX.addRule(MULTIPLE_INHERITS);
 //		VJO_SYNTAX.addRule(MULTIPLE_PROPS);
 //		VJO_SYNTAX.addRule(MULTIPLE_PROTOS);
 		VJO_SYNTAX.addRule(NAME_SPACE_COLLISION);
 		VJO_SYNTAX.addRule(ETYPE_VALUES_MUST_BE_ARRAY);
 		VJO_SYNTAX.addRule(ABSTRACT_MEMBER_SHOULD_NOT_HAVE_DEFINITION);
 		VJO_SYNTAX.addRule(OTYPE_SHOULD_NOT_HAVE_NONE_OBJ_LITERAL_PROPERTY);
 		VJO_SYNTAX.addRule(OTYPE_SHOULD_NOT_HAVE_INNER_TYPES);
 		VJO_SYNTAX.addRule(OTYPE_SHOULD_NOT_BE_AS_INNER_TYPE);
 		VJO_SYNTAX.addRule(MTYPE_SHOULD_NOT_HAVE_INNER_TYPES);
 		VJO_SYNTAX.addRule(MTYPE_SHOULD_NOT_BE_AS_INNER_TYPE);
 		VJO_SYNTAX.addRule(ITYPE_SHOULD_NOT_HAVE_INSTANCE_PROPERTY);
 		VJO_SYNTAX.addRule(UNKNOWN_TYPE_MISSING_IMPORT);
 		VJO_SYNTAX.addRule(UNKNOWN_TYPE_NOT_IN_TYPE_SPACE);
 		VJO_SYNTAX.addRule(UNKNOWN_TYPE_NOT_IN_TYPE_SPACE_INACTIVENEEDS);
 		VJO_SYNTAX.addRule(REDUNDANT_IMPORT);
 		VJO_SYNTAX.addRule(ITYPE_ALLOWS_ONLY_PUBLIC_MODIFIER);
 		VJO_SYNTAX.addRule(PUBLIC_CLASS_SHOULD_RESIDE_IN_CORRESPONDING_FILE);
 		VJO_SYNTAX.addRule(MAIN_CLASS_SHOULD_BE_PUBLIC);
 		VJO_SYNTAX.addRule(INVALID_IDENTIFIER);
 		VJO_SYNTAX.addRule(INVALID_IDENTIFIER_WITH_KEYWORD);
 		VJO_SYNTAX.addRule(CLASS_NAME_CANNOT_HAVE_ILLEGAL_TOKEN);
 	}
 	
 	public void loadErrMsgs(){
 		loadErrMsgs(true);
 	}
 	
 	public void loadErrMsgs(boolean verbose){
 		final ProblemErrorMessageBundle bundle = ProblemErrorMessageBundle.getInstance();
 		VARIABLE_SHOULD_BE_DEFINED.setErrMsg(bundle.getErrorMessage("VARIABLE_SHOULD_BE_DEFINED", verbose));
 		IMPLICIT_GLOBAL_VARIABLE_DECLARED.setErrMsg(bundle.getErrorMessage("IMPLICIT_GLOBAL_VARIABLE_DECLARED", verbose));
 		VARIABLE_ALREADY_DEFINED.setErrMsg(bundle.getErrorMessage("VARIABLE_ALREADY_DEFINED", verbose));
 		DUPLICATE_METHOD.setErrMsg(bundle.getErrorMessage("DUPLICATE_METHOD", verbose));
 		DUPLICATE_LABEL.setErrMsg(bundle.getErrorMessage("DUPLICATE_LABEL", verbose));
 //		INVALID_LABEL_DECORATION.setErrMsg(bundle.getErrorMessage("INVALID_LABEL_DECORATION", verbose));
 		BREAK_NONE_EXIST_LABEL.setErrMsg(bundle.getErrorMessage("BREAK_NONE_EXIST_LABEL", verbose));
 		CONTINUE_NONE_EXIST_LABEL.setErrMsg(bundle.getErrorMessage("CONTINUE_NONE_EXIST_LABEL", verbose));
 		DUPLICATE_PROPERTY.setErrMsg(bundle.getErrorMessage("DUPLICATE_PROPERTY", verbose));
 		OVERLAP_STATIC_AND_NONE_STATIC_METHOD.setErrMsg(bundle.getErrorMessage("OVERLAP_STATIC_AND_NONE_STATIC_METHOD", verbose));
 		OVERLAP_STATIC_AND_NONE_STATIC_PROPERTY.setErrMsg(bundle.getErrorMessage("OVERLAP_STATIC_AND_NONE_STATIC_PROPERTY", verbose));
 		ABSTRACT_METHOD_MUST_BE_IMPLEMENTED.setErrMsg(bundle.getErrorMessage("ABSTRACT_METHOD_MUST_BE_IMPLEMENTED", verbose));
 		FINAL_METHOD_SHOULD_NOT_BE_OVERRIDEN.setErrMsg(bundle.getErrorMessage("FINAL_METHOD_SHOULD_NOT_BE_OVERRIDEN", verbose));
 		STATIC_METHOD_SHOULD_NOT_BE_OVERRIDEN.setErrMsg(bundle.getErrorMessage("STATIC_METHOD_SHOULD_NOT_BE_OVERRIDEN", verbose));
 //		SUPER_CLASS_MUST_PROVIDE_CONSTRUCTOR.setErrMsg(bundle.getErrorMessage("SUPER_CLASS_MUST_PROVIDE_CONSTRUCTOR", verbose));
 		SUPER_CLASS_SHOULD_NOT_BE_FINAL.setErrMsg(bundle.getErrorMessage("SUPER_CLASS_SHOULD_NOT_BE_FINAL", verbose));
 		SUPER_CLASS_SHOULD_NOT_BE_THE_SAME.setErrMsg(bundle.getErrorMessage("SUPER_CLASS_SHOULD_NOT_BE_THE_SAME", verbose));
 		CLASS_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT.setErrMsg(bundle.getErrorMessage("CLASS_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT", verbose));
 		INTERFACE_SHOULD_NOT_BE_FINAL.setErrMsg(bundle.getErrorMessage("INTERFACE_SHOULD_NOT_BE_FINAL", verbose));
 		METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT.setErrMsg(bundle.getErrorMessage("METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT", verbose));
 		METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_STATIC_AND_ABSTRACT.setErrMsg(bundle.getErrorMessage("METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_STATIC_AND_ABSTRACT", verbose));
 		METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_PRIVATE_AND_ABSTRACT.setErrMsg(bundle.getErrorMessage("METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_PRIVATE_AND_ABSTRACT", verbose));
 		CLASS_SHOULD_BE_ABSTRACT.setErrMsg(bundle.getErrorMessage("CLASS_SHOULD_BE_ABSTRACT", verbose));
 		CLASS_BETTER_START_WITH_NONE_CAPITAL_LETTER.setErrMsg(bundle.getErrorMessage("CLASS_BETTER_START_WITH_NONE_CAPITAL_LETTER", verbose));
 		TYPE_NAME_SHOULD_NOT_BE_EMPTY.setErrMsg(bundle.getErrorMessage("TYPE_NAME_SHOULD_NOT_BE_EMPTY", verbose));
 		ETYPE_SHOULD_NOT_BE_ABSTRACT_OR_PRIVATE_OR_PROTECTED.setErrMsg(bundle.getErrorMessage("ETYPE_SHOULD_NOT_BE_ABSTRACT_OR_PRIVATE_OR_PROTECTED", verbose));
 		ETYPE_SHOULD_NOT_DEFINE_PUBLIC_OR_PROTECTED_CONSTRUCTOR.setErrMsg(bundle.getErrorMessage("ETYPE_SHOULD_NOT_DEFINE_PUBLIC_OR_PROTECTED_CONSTRUCTOR", verbose));
 		ETYPE_SHOULD_NOT_DEFINE_ABSTRACT_METHOD_OR_PROPERTY.setErrMsg(bundle.getErrorMessage("ETYPE_SHOULD_NOT_DEFINE_ABSTRACT_METHOD_OR_PROPERTY", verbose));
         EXPECTS_MUST_BE_CTYPE_ITYPE.setErrMsg(bundle.getErrorMessage("EXPECTS_MUST_BE_CTYPE_ITYPE", verbose));
         MIXINED_TYPE_MUST_NOT_BE_ITSELF.setErrMsg(bundle.getErrorMessage("MIXINED_TYPE_MUST_NOT_BE_ITSELF", verbose));
 		ETYPE_SHOULD_NOT_HAVE_DUP_ENUM_VALUES.setErrMsg(bundle.getErrorMessage("ETYPE_SHOULD_NOT_HAVE_DUP_ENUM_VALUES", verbose));
 		ETYPE_VALUES_MUST_BE_ARRAY.setErrMsg(bundle.getErrorMessage("ETYPE_VALUES_MUST_BE_ARRAY"));
 		ITYPE_OR_MTYPE_SHOULD_NOT_HAVE_CONSTRUCTOR.setErrMsg(bundle.getErrorMessage("ITYPE_OR_MTYPE_SHOULD_NOT_HAVE_CONSTRUCTOR", verbose));
 		ITYPE_SHOULD_NOT_DEFINE_STATIC_METHODS.setErrMsg(bundle.getErrorMessage("ITYPE_SHOULD_NOT_DEFINE_STATIC_METHODS", verbose));
 		ABSTRACT_MEMBER_SHOULD_NOT_HAVE_DEFINITION.setErrMsg(bundle.getErrorMessage("ABSTRACT_MEMBER_SHOULD_NOT_HAVE_DEFINITION", verbose));
 		OTYPE_SHOULD_NOT_HAVE_NONE_OBJ_LITERAL_PROPERTY.setErrMsg(bundle.getErrorMessage("OTYPE_SHOULD_NOT_HAVE_NONE_OBJ_LITERAL_PROPERTY", verbose));
 		OTYPE_SHOULD_NOT_HAVE_INNER_TYPES.setErrMsg(bundle.getErrorMessage("OTYPE_SHOULD_NOT_HAVE_INNER_TYPES", verbose));
 		OTYPE_SHOULD_NOT_BE_AS_INNER_TYPE.setErrMsg(bundle.getErrorMessage("OTYPE_SHOULD_NOT_BE_AS_INNER_TYPE", verbose));
 		MTYPE_SHOULD_NOT_HAVE_INNER_TYPES.setErrMsg(bundle.getErrorMessage("MTYPE_SHOULD_NOT_HAVE_INNER_TYPES", verbose));
 		MTYPE_SHOULD_NOT_BE_AS_INNER_TYPE.setErrMsg(bundle.getErrorMessage("MTYPE_SHOULD_NOT_BE_AS_INNER_TYPE", verbose));
 		ITYPE_SHOULD_NOT_HAVE_INSTANCE_PROPERTY.setErrMsg(bundle.getErrorMessage("ITYPE_SHOULD_NOT_HAVE_INSTANCE_PROPERTY", verbose));
 		ITYPE_SHOULD_NOT_EXTEND_NONE_ITYPE_CLASS.setErrMsg(bundle.getErrorMessage("ITYPE_SHOULD_NOT_EXTEND_NONE_ITYPE_CLASS", verbose));
 //		MTYPE_EXPECTS_SHOULD_NOT_BE_OVERWRITTEN.setErrMsg(bundle.getErrorMessage("ITYPE_SHOULD_NOT_EXTEND_NONE_ITYPE_CLASS", verbose));
 		ITYPE_ALLOWS_ONLY_PUBLIC_MODIFIER.setErrMsg(bundle.getErrorMessage("ITYPE_ALLOWS_ONLY_PUBLIC_MODIFIER", verbose));
 		CTYPE_SHOULD_NOT_EXTEND_NONE_CTYPE_CLASS.setErrMsg(bundle.getErrorMessage("CTYPE_SHOULD_NOT_EXTEND_NONE_CTYPE_CLASS", verbose));
 		CTYPE_SHOULD_NOT_IMPLEMENT_NONE_ITYPE_INTERFACE.setErrMsg(bundle.getErrorMessage("CTYPE_SHOULD_NOT_IMPLEMENT_NONE_ITYPE_INTERFACE", verbose));
 		OVERRIDE_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE.setErrMsg(bundle.getErrorMessage("OVERRIDE_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE", verbose));
 //		IMPLEMENT_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE.setErrMsg(bundle.getErrorMessage("IMPLEMENT_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE", verbose));
 		MIXIN_EXPECTS_INSTANCE_METHOD_MUST_BE_SATISFIED.setErrMsg(bundle.getErrorMessage("MIXIN_EXPECTS_INSTANCE_METHOD_MUST_BE_SATISFIED", verbose));
 		MIXIN_EXPECTS_INSTANCE_PROP_MUST_BE_SATISFIED.setErrMsg(bundle.getErrorMessage("MIXIN_EXPECTS_INSTANCE_PROP_MUST_BE_SATISFIED", verbose));
 //		MIXIN_EXPECTS_STATIC_METHOD_MUST_BE_SATISFIED.setErrMsg(bundle.getErrorMessage("MIXIN_EXPECTS_STATIC_METHOD_MUST_BE_SATISFIED", verbose));
 //		MIXIN_EXPECTS_STATIC_PROP_MUST_BE_SATISFIED.setErrMsg(bundle.getErrorMessage("MIXIN_EXPECTS_STATIC_PROP_MUST_BE_SATISFIED", verbose));
 //		MTYPE_EXPECTS_SHOULD_NOT_BE_OVERWRITTEN.setErrMsg(bundle.getErrorMessage("MTYPE_EXPECTS_SHOULD_NOT_BE_OVERWRITTEN", verbose));
 //		MTYPE_SHOULD_NOT_BE_REFERENCED_DIRECTLY.setErrMsg(bundle.getErrorMessage("MTYPE_SHOULD_NOT_BE_REFERENCED_DIRECTLY", verbose));
 		MTYPE_SHOULD_ONLY_BE_MIXINED.setErrMsg(bundle.getErrorMessage("MTYPE_SHOULD_ONLY_BE_MIXINED", verbose));
 		INNER_TYPE_SHOULD_NOT_HAVE_NAME_SAME_AS_EMBEDDING_TYPE.setErrMsg(bundle.getErrorMessage("INNER_TYPE_SHOULD_NOT_HAVE_NAME_SAME_AS_EMBEDDING_TYPE", verbose));
 		INSTANCE_INNER_TYPE_SHOULD_NOT_HAVE_STATIC_MEMBERS.setErrMsg(bundle.getErrorMessage("INSTANCE_INNER_TYPE_SHOULD_NOT_HAVE_STATIC_MEMBERS", verbose));
 		OVERLOAD_METHOD_SHOULD_NOT_OVERLAP.setErrMsg(bundle.getErrorMessage("OVERLOAD_METHOD_SHOULD_NOT_OVERLAP", verbose));
 //		OVERLOAD_METHOD_SHOULD_NOT_HAVE_VARIABLE_MODIFIER.setErrMsg(bundle.getErrorMessage("OVERLOAD_METHOD_SHOULD_NOT_HAVE_VARIABLE_MODIFIER", verbose));
 		OVERRIDE_METHOD_SHOULD_NOT_REDUCE_VISIBILITY.setErrMsg(bundle.getErrorMessage("OVERRIDE_METHOD_SHOULD_NOT_REDUCE_VISIBILITY", verbose));
 //		TWO_EXPRS_SHOULD_BE_CONSISTENT.setErrMsg(bundle.getErrorMessage("TWO_EXPRS_SHOULD_BE_CONSISTENT", verbose));
 		EXPR_SHOULD_BE_BOOL.setErrMsg(bundle.getErrorMessage("EXPR_SHOULD_BE_BOOL", verbose));
 		STMT_SHOULD_BE_REACHABLE.setErrMsg(bundle.getErrorMessage("STMT_SHOULD_BE_REACHABLE", verbose));
 		EXPR_SHOULD_BE_BOOL_OR_NUMBER.setErrMsg(bundle.getErrorMessage("EXPR_SHOULD_BE_BOOL", verbose));
 		EXPR_SHOULD_BE_NUMBER.setErrMsg(bundle.getErrorMessage("EXPR_SHOULD_BE_NUMBER", verbose));
 //		EXPR_SHOULD_BE_NUMBER_OR_STRING.setErrMsg(bundle.getErrorMessage("EXPR_SHOULD_BE_NUMBER_OR_STRING", verbose));
 		ASSIGNABLE.setErrMsg(bundle.getErrorMessage("ASSIGNABLE", verbose));
 		OBJLITERAL_ASSIGNABLE.setErrMsg(bundle.getErrorMessage("OBJLITERAL_ASSIGNABLE", verbose));
 		SHOULD_NOT_ASSIGN_TO_ENUM.setErrMsg(bundle.getErrorMessage("SHOULD_NOT_ASSIGN_TO_ENUM", verbose));
 		SHOULD_NOT_ASSIGN_TO_FINAL.setErrMsg(bundle.getErrorMessage("SHOULD_NOT_ASSIGN_TO_FINAL", verbose));
 //		CASTABLE.setErrMsg(bundle.getErrorMessage("CASTABLE", verbose));
 //		QUALIFIER_SHOULD_NOT_BE_NULL.setErrMsg(bundle.getErrorMessage("QUALIFIER_SHOULD_NOT_BE_NULL", verbose));
 //		QUALIFIER_SHOULD_BE_DEFINED.setErrMsg(bundle.getErrorMessage("QUALIFIER_SHOULD_BE_DEFINED", verbose));
 		METHOD_SHOULD_BE_DEFINED.setErrMsg(bundle.getErrorMessage("METHOD_SHOULD_BE_DEFINED", verbose));
 		INFERRED_TYPE_METHOD_SHOULD_BE_DEFINED.setErrMsg(bundle.getErrorMessage("METHOD_SHOULD_BE_DEFINED", verbose));
 		FUNCTION_SHOULD_BE_DEFINED.setErrMsg(bundle.getErrorMessage("FUNCTION_SHOULD_BE_DEFINED", verbose));
 		METHOD_SHOULD_BE_VISIBLE.setErrMsg(bundle.getErrorMessage("METHOD_SHOULD_BE_VISIBLE", verbose));
 		CONSTRUCTOR_SHOULD_BE_VISIBLE.setErrMsg(bundle.getErrorMessage("CONSTRUCTOR_SHOULD_BE_VISIBLE", verbose));
 		NONE_VOID_METHOD_SHOULD_HAVE_RETURN.setErrMsg(bundle.getErrorMessage("NONE_VOID_METHOD_SHOULD_HAVE_RETURN", verbose));
 		VOID_METHOD_SHOULD_NOT_HAVE_RETURN.setErrMsg(bundle.getErrorMessage("VOID_METHOD_SHOULD_NOT_HAVE_RETURN", verbose));
 		METHOD_RETURN_VALUE_SHOULD_COMPLY.setErrMsg(bundle.getErrorMessage("METHOD_RETURN_VALUE_SHOULD_COMPLY", verbose));
 		THROW_TYPE_SHOULD_COMPLY.setErrMsg(bundle.getErrorMessage("THROW_TYPE_SHOULD_COMPLY"));
 		METHOD_ARGS_TYPE_SHOULD_MATCH.setErrMsg(bundle.getErrorMessage("METHOD_ARGS_TYPE_SHOULD_MATCH", verbose));
 		METHOD_ARGS_TYPE_SHOULD_NOT_BE_VOID.setErrMsg(bundle.getErrorMessage("METHOD_ARGS_TYPE_SHOULD_NOT_BE_VOID", verbose));
 		METHOD_WRONG_NUMBER_OF_ARGS.setErrMsg(bundle.getErrorMessage("METHOD_WRONG_NUMBER_OF_ARGS", verbose));
 		STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE.setErrMsg(bundle.getErrorMessage("STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE", verbose));
 		NONE_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE.setErrMsg(bundle.getErrorMessage("NONE_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE", verbose));
 //		ARRAY_CREATION_VAL_SHOULD_BE_DEFINED.setErrMsg(bundle.getErrorMessage("ARRAY_CREATION_VAL_SHOULD_BE_DEFINED", verbose));
 		ARRAY_CREATION_DIMENSION_TYPE_SHOULD_BE_INTEGER.setErrMsg(bundle.getErrorMessage("ARRAY_CREATION_DIMENSION_TYPE_SHOULD_BE_INTEGER", verbose));
 //		CLASS_OBJ_SHOULD_BE_DEFINED.setErrMsg(bundle.getErrorMessage("CLASS_OBJ_SHOULD_BE_DEFINED", verbose));
 		OBJ_SHOULD_BE_CLASS_TYPE.setErrMsg(bundle.getErrorMessage("OBJ_SHOULD_BE_CLASS_TYPE", verbose));
 		CLASS_SHOULD_BE_INSTANTIATABLE.setErrMsg(bundle.getErrorMessage("CLASS_SHOULD_BE_INSTANTIATABLE", verbose));
 		FINAL_PROPERTY_SHOULD_BE_INITIALIZED.setErrMsg(bundle.getErrorMessage("FINAL_PROPERTY_SHOULD_BE_INITIALIZED", verbose));
 //		ITEM_TYPE_SHOULD_MATCH_ARRAY_TYPE.setErrMsg(bundle.getErrorMessage("ITEM_TYPE_SHOULD_MATCH_ARRAY_TYPE", verbose));
 		ARRAY_INDEX_SHOULD_BE_INT_OR_STRING_TYPE.setErrMsg(bundle.getErrorMessage("ARRAY_INDEX_SHOULD_BE_INT_OR_STRING_TYPE", verbose));
 //		PACKAGE_SHOULD_BE_DEFINED.setErrMsg(bundle.getErrorMessage("PACKAGE_SHOULD_BE_DEFINED", verbose));
 		PROPERTY_SHOULD_BE_DEFINED.setErrMsg(bundle.getErrorMessage("PROPERTY_SHOULD_BE_DEFINED", verbose));
 		PROPERTY_SHOULD_BE_VISIBLE.setErrMsg(bundle.getErrorMessage("PROPERTY_SHOULD_BE_VISIBLE", verbose));
 		PROPERTY_SHOULD_NOT_HIDE_PARENT_PROPERTY.setErrMsg(bundle.getErrorMessage("PROPERTY_SHOULD_NOT_HIDE_PARENT_PROPERTY", verbose));
 		STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE.setErrMsg(bundle.getErrorMessage("STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE", verbose));
 		NONE_STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE.setErrMsg(bundle.getErrorMessage("NONE_STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE", verbose));
 		STATIC_REFERENCE_TO_NON_STATIC_TYPE.setErrMsg(bundle.getErrorMessage("STATIC_REFERENCE_TO_NON_STATIC_TYPE", verbose));
 //		WITH_SCOPE_SHOULD_NOT_CONFLICT_LOCAL.setErrMsg(bundle.getErrorMessage("WITH_SCOPE_SHOULD_NOT_CONFLICT_LOCAL", verbose));
 		DISCOURAGED_NESTED_WITH.setErrMsg(bundle.getErrorMessage("DISCOURAGED_NESTED_WITH", verbose));
 		OBJECT_LITERAL_SHOULD_HAVE_UNIQUE_KEY.setErrMsg(bundle.getErrorMessage("OBJECT_LITERAL_SHOULD_HAVE_UNIQUE_KEY", verbose));
 		VJO_SYNTAX_CORRECTNESS.setErrMsg(bundle.getErrorMessage("VJO_SYNTAX_CORRECTNESS", verbose));
 		MISSING_ENDTYPE.setErrMsg(bundle.getErrorMessage("MISSING_ENDTYPE", verbose));
 //		MULTIPLE_INHERITS.setErrMsg(bundle.getErrorMessage("MULTIPLE_INHERITS", verbose));
 //		MULTIPLE_PROPS.setErrMsg(bundle.getErrorMessage("MULTIPLE_PROPS", verbose));
 //		MULTIPLE_PROTOS.setErrMsg(bundle.getErrorMessage("MULTIPLE_PROTOS", verbose));
 		NAME_SPACE_COLLISION.setErrMsg(bundle.getErrorMessage("NAME_SPACE_COLLISION", verbose));
 		REDUNDANT_IMPORT.setErrMsg(bundle.getErrorMessage("REDUNDANT_IMPORT", verbose));
 		PUBLIC_CLASS_SHOULD_RESIDE_IN_CORRESPONDING_FILE.setErrMsg(bundle.getErrorMessage("PUBLIC_CLASS_SHOULD_RESIDE_IN_CORRESPONDING_FILE", verbose));
 		MAIN_CLASS_SHOULD_BE_PUBLIC.setErrMsg(bundle.getErrorMessage("MAIN_CLASS_SHOULD_BE_PUBLIC", verbose));
 		UNKNOWN_TYPE_MISSING_IMPORT.setErrMsg(bundle.getErrorMessage("UNKNOWN_TYPE_MISSING_IMPORT", verbose));
 		UNKNOWN_TYPE_NOT_IN_TYPE_SPACE.setErrMsg(bundle.getErrorMessage("UNKNOWN_TYPE_NOT_IN_TYPE_SPACE", verbose));
 		UNKNOWN_TYPE_NOT_IN_TYPE_SPACE_INACTIVENEEDS.setErrMsg(bundle.getErrorMessage("UNKNOWN_TYPE_NOT_IN_TYPE_SPACE", verbose));
 		PRIVATE_METHOD_REFERENCED_NOWHERE.setErrMsg(bundle.getErrorMessage("PRIVATE_METHOD_REFERENCED_NOWHERE", verbose));
 		PRIVATE_PROPERTY_REFERENCED_NOWHERE.setErrMsg(bundle.getErrorMessage("PRIVATE_PROPERTY_REFERENCED_NOWHERE", verbose));
 		INVALID_IDENTIFIER.setErrMsg(bundle.getErrorMessage("INVALID_IDENTIFIER", verbose));
 		INVALID_IDENTIFIER_WITH_KEYWORD.setErrMsg(bundle.getErrorMessage("INVALID_IDENTIFIER", verbose));
 		CLASS_NAME_CANNOT_HAVE_ILLEGAL_TOKEN.setErrMsg(bundle.getErrorMessage("CLASS_NAME_CANNOT_HAVE_ILLEGAL_TOKEN", verbose));
 		CANNOT_USE_UNINTIALIZED_TYPE.setErrMsg(bundle.getErrorMessage("CANNOT_USE_UNINTIALIZED_TYPE", verbose));
 		CANNOT_USE_INACTIVE_NEED_ACTIVELY.setErrMsg(bundle.getErrorMessage("CANNOT_USE_INACTIVE_NEED_ACTIVELY", verbose));
 		UNUSED_ACTIVE_NEEDS.setErrMsg(bundle.getErrorMessage("UNUSED_ACTIVE_NEEDS", verbose));
 		GENERIC_PARAM_TYPE_MISMATCH.setErrMsg(bundle.getErrorMessage("GENERIC_PARAM_TYPE_MISMATCH", verbose));
 		GENERIC_PARAM_NUM_MISMATCH.setErrMsg(bundle.getErrorMessage("GENERIC_PARAM_NUM_MISMATCH", verbose));
 		ATTRIBUTOR_SHOULD_NOT_USE_VJ_RUNTIME.setErrMsg(bundle.getErrorMessage("ATTRIBUTOR_SHOULD_NOT_USE_VJ_RUNTIME", verbose));
 	}
 
 	private void setUpRule(final IVjoSemanticRule<?> rule, 
 			final JstProblemId probId, 
 			final String ruleName, 
 			final String ruleDesc){
 		rule.setProblemId(probId);
 		rule.setRuleName(ruleName);
 		rule.setRuleDescription(ruleDesc);
 	}
 	
 	private void initRules() {
 		setUpRule(VARIABLE_SHOULD_BE_DEFINED, 
 				VarProbIds.UndefinedName, 
 				"Variable Should Be Defined", 
 				"cannot find defined variable");
 		
 		setUpRule(IMPLICIT_GLOBAL_VARIABLE_DECLARED,
 				VarProbIds.LooseVarDecl,
 				"Implicit Global Variable Declared",
 				"Cannot implicitly declare global variable");
 		
 		setUpRule(VARIABLE_ALREADY_DEFINED, 
 				VarProbIds.RedefinedLocal, 
 				"Variable_Already_Defined", 
 				"Cant redefine variable");
 		
 		setUpRule(DUPLICATE_METHOD, 
 				MethodProbIds.AmbiguousMethod, 
 				"Duplicate_Methods", 
 				"Duplicate methods with same name");
 		
 		setUpRule(DUPLICATE_LABEL,
 				VarProbIds.LocalVariableHidingLocalVariable,
 				"Duplicate_Labels",
 				"Duplicate labels with same name");
 		
 		setUpRule(STATIC_REFERENCE_TO_NON_STATIC_TYPE,
                 FieldProbIds.StaticReferenceToNonStaticType,
                 "STATIC_REFERENCE_TO_NON_STATIC_TYPE",
                 "Static reference to non static type");
 		
 //		setUpRule(INVALID_LABEL_DECORATION,
 //				VjoSyntaxProbIds.InvalidLabelDecoration,
 //				"Invalid_Label_Decoration",
 //				"Invalid label decoration in use");
 //		
 		setUpRule(BREAK_NONE_EXIST_LABEL, 
 				VjoSyntaxProbIds.BreakNoneExistLabel, 
 				"Break_none_exist_label", 
 				"Break none exist label");
 		
 		setUpRule(CONTINUE_NONE_EXIST_LABEL, 
 				VjoSyntaxProbIds.ContinueNoneExistLabel, 
 				"Break_none_exist_label", 
 				"Break none exist label");
 		
 		setUpRule(DUPLICATE_PROPERTY, 
 				FieldProbIds.AmbiguousField, 
 				"Duplicate_Properties", 
 				"Duplicate properties with same name");
 		
 		setUpRule(OVERLAP_STATIC_AND_NONE_STATIC_METHOD,
 				MethodProbIds.AmbiguousMethod,
 				"Overlap_Static_And_None_Static_Method",
 				"Overlap static and nont-static method");
 		
 		setUpRule(OVERLAP_STATIC_AND_NONE_STATIC_PROPERTY, 
 				FieldProbIds.AmbiguousField, 
 				"Overlap_Static_And_None_Static_Property", 
 				"Overlap static and none-static property");
 		
 		setUpRule(ABSTRACT_METHOD_MUST_BE_IMPLEMENTED, 
 				MethodProbIds.UndefinedMethod, 
 				"Abstract_Method_Must_Be_Implemented", 
 				"None abstract class should implement the abstract methods");
 		
 		setUpRule(FINAL_METHOD_SHOULD_NOT_BE_OVERRIDEN, 
 				MethodProbIds.OverrideSuperFinalMethod, 
 				"Final_Method_Should_Not_Be_Overriden", 
 				"Final method shouldn't be overriden");
 		
 		setUpRule(STATIC_METHOD_SHOULD_NOT_BE_OVERRIDEN,
 				MethodProbIds.OverrideSuperStaticMethod,
 				"Static_Method_Should_Not_Be_OVerriden",
 				"Static Method should not be overriden");
 		
 //		setUpRule(SUPER_CLASS_MUST_PROVIDE_CONSTRUCTOR, 
 //				ConstructorProbIds.UndefinedConstructor, 
 //				"Super_Class_Must_Provide_Constructor", 
 //				"Super class must have a contructor");
 //		
 		setUpRule(SUPER_CLASS_SHOULD_NOT_BE_FINAL,
 				TypeProbIds.ClassExtendFinalClass,
 				"Super_Class_Should_Not_Be_Final",
 				"Super class shouldn't be finalized");
 		
 		setUpRule(SUPER_CLASS_SHOULD_NOT_BE_THE_SAME,
 				TypeProbIds.ClassExtendItself,
 				"Super_Class_Should_Not_Be_The_Same",
 				"Super class should not be the same as the current type");
 		
 		setUpRule(CLASS_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT,
 				TypeProbIds.IllegalModifierForClass,
 				"Class_Should_Not_Be_Both_Final_And_Abstract",
 				"Class shouldn't be both final and abstract");
 		
 		setUpRule(INTERFACE_SHOULD_NOT_BE_FINAL,
 				TypeProbIds.IllegalModifierForInterface,
 				"Interface_should_not_be_final",
 				"Interface should not be final");
 		
 		setUpRule(METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT,
 				MethodProbIds.MethodBothFinalAndAbstract,
 				"Method_Should_Not_Be_Both_Final_And_Abstract",
 				"Method shouldn't be both final and abstract");
 		
 		setUpRule(METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_STATIC_AND_ABSTRACT,
 				MethodProbIds.MethodBothStaticAndAbstract,
 				"Method_Should_Not_Be_Both_Static_And_Abstract",
 				"Method should not be both static and abstract");
 		
 		setUpRule(METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_PRIVATE_AND_ABSTRACT,
 				MethodProbIds.MethodBothPrivateAndAbstract,
 				"Method_Should_Not_Be_Both_Private_And_Abstract",
 				"Method should not be both private and abstract");
 		
 		setUpRule(CLASS_SHOULD_BE_ABSTRACT,
 				TypeProbIds.IllegalModifierForClass,
 				"Class_Should_Be_Abstract",
 				"Class should be abstract if there's abstract method");
 		
 		setUpRule(CLASS_BETTER_START_WITH_NONE_CAPITAL_LETTER,
 				TypeProbIds.ClassBetterStartsWithCapitalLetter,
 				"Class_Better_Start_With_Capital_Letter",
 				"Class better start with capital letter");
 		
 		setUpRule(TYPE_NAME_SHOULD_NOT_BE_EMPTY,
 				TypeProbIds.ClassNameShouldNotBeEmpty,
 				"Type_Name_Should_Not_Be_Empty",
 				"Type name should not be empty");
 		
 		setUpRule(ETYPE_SHOULD_NOT_BE_ABSTRACT_OR_PRIVATE_OR_PROTECTED,
 				TypeProbIds.IllegalModifierForClass,
 				"Enum_Should_Not_Be_Abstract",
 				"Enum should not be abstract as it cannot be extended");
 		
 		setUpRule(ETYPE_SHOULD_NOT_DEFINE_PUBLIC_OR_PROTECTED_CONSTRUCTOR,
 				MethodProbIds.OverlyVisibleMethod,
 				"Enum_Should_Not_Define_Public_Or_Protected_Constructor",
 				"Enum should not define public or protected constructor");
 		
 		setUpRule(ETYPE_SHOULD_NOT_DEFINE_ABSTRACT_METHOD_OR_PROPERTY,
 				MethodProbIds.MethodBothFinalAndAbstract,
 				"Enum_Should_Not_Define_Method_Abstract",
 				"Enum method should not be abstract as it is implicitly final");
 		
 		setUpRule(ETYPE_SHOULD_NOT_HAVE_DUP_ENUM_VALUES,
 				FieldProbIds.DuplicateField,
 				"Enum_Should_Not_Have_Duplicate_Values",
 				"Enum should not have duplicate values");
 		
 		setUpRule(ETYPE_VALUES_MUST_BE_ARRAY,
 		        MethodProbIds.ParameterMismatch,
 		        "ETYPE_VALUES_MUST_BE_ARRAY",
 		        "ETYPE_VALUES_MUST_BE_ARRAY"
 		        );
 		
 		setUpRule(ITYPE_OR_MTYPE_SHOULD_NOT_HAVE_CONSTRUCTOR, 
 				TypeProbIds.InvalidClassInstantiation, 
 				"itype_or_mtype_cant_have_constructor", 
 				"IType or MType shouldn't have constructors");
 		
 		setUpRule(ITYPE_SHOULD_NOT_DEFINE_STATIC_METHODS, 
 				TypeProbIds.IllegalModifierForInterface, 
 				"itype_cannot_define_static_methods", 
 				"IType shouldn't define static methods");
 		
 		setUpRule(ABSTRACT_MEMBER_SHOULD_NOT_HAVE_DEFINITION,
 				MethodProbIds.BodyForAbstractMethod,
 				"itype_cannot_have_method_body",
 				"IType shouldn't define method body");
 		
 		setUpRule(OTYPE_SHOULD_NOT_HAVE_NONE_OBJ_LITERAL_PROPERTY, 
 				VjoSyntaxProbIds.OTypeWithNoneObjLiteralProperty, 
 				"otype_should_not_have_none_obj_literal_property", 
 				"OType should not have none object literal typed property");
 		
 		setUpRule(OTYPE_SHOULD_NOT_HAVE_INNER_TYPES,
 				VjoSyntaxProbIds.OTypeWithInnerTypes,
 				"otype_should_not_have_inner_type",
 				"OType should not have inner type");
 		
 		setUpRule(OTYPE_SHOULD_NOT_BE_AS_INNER_TYPE,
 				VjoSyntaxProbIds.OTypeAsInnerType,
 				"otype_should_not_be_as_inner_type",
 				"OType should not be as inner type");
 		
 		setUpRule(MTYPE_SHOULD_NOT_HAVE_INNER_TYPES,
 				VjoSyntaxProbIds.MTypeWithInnerTypes,
 				"mtype_should_not_have_inner_type",
 				"MType should not have inner type");
 		
 		setUpRule(MTYPE_SHOULD_NOT_BE_AS_INNER_TYPE,
 				VjoSyntaxProbIds.MTypeAsInnerType,
 				"mtype_should_not_be_as_inner_type",
 				"MType should not be as inner type");
 		
 		setUpRule(ITYPE_SHOULD_NOT_HAVE_INSTANCE_PROPERTY,
 				VjoSyntaxProbIds.ITypeWithInstanceProperty,
 				"itype_cannot_have_instance_property",
 				"IType shouldn't define instance property");
 		
 		setUpRule(ITYPE_SHOULD_NOT_EXTEND_NONE_ITYPE_CLASS, 
 				TypeProbIds.SuperInterfaceMustBeAnInterface, 
 				"itype_cannot_extend_from_none_itype", 
 				"IType can't extend from none itype class");
 		
         setUpRule(EXPECTS_MUST_BE_CTYPE_ITYPE,
                 TypeProbIds.ExpectsMustBeMtypeOrItype,
                 "expects_must_be_mtype_or_ctype",
                 "Only mtype and ctype can be expect");
 
         setUpRule(MIXINED_TYPE_MUST_NOT_BE_ITSELF,
                 TypeProbIds.MixinedTypeShouldNotBeItself,
                 "mixined_type_should_not_be_itself",
                 "Mixined type should not be itself");
 
 		setUpRule(ITYPE_ALLOWS_ONLY_PUBLIC_MODIFIER,
 				VjoSyntaxProbIds.ITypeAllowsOnlyPublicModifier,
 				"itype_allows_only_public_modifier",
 				"IType can't have none-public modifier");
 		
 		setUpRule(CTYPE_SHOULD_NOT_EXTEND_NONE_CTYPE_CLASS,
 				TypeProbIds.SuperclassMustBeAClass,
 				"ctype_cannot_extend_from_none_ctype",
 				"CType can't extend from none ctype class");
 		
 		setUpRule(CTYPE_SHOULD_NOT_IMPLEMENT_NONE_ITYPE_INTERFACE,
 				TypeProbIds.SuperInterfaceMustBeAnInterface,
 				"ctype_cannot_implement_none_itype",
 				"CType cannot implement none itype interface");
 		
 		setUpRule(OVERRIDE_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE, 
 				MethodProbIds.AmbiguousMethod, 
 				"Override_Methods_Should_Have_Compatible_Signatures", 
 				"Overriding method with an incompatible signature");
 
 //		setUpRule(IMPLEMENT_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE,
 //				MethodProbIds.AmbiguousMethod,
 //				"Implement_Methods_Should_Have_Compatible_Signatures",
 //				"Implement method with an incomptable signature");
 		
 		setUpRule(MIXIN_EXPECTS_INSTANCE_METHOD_MUST_BE_SATISFIED,
 				TypeProbIds.MixinExpectsMustBeSatisfied,
 				"Mixin_Expects_Must_Be_Satisfies",
 				"Mixin expects must be satisfies");
 		
 		setUpRule(MIXIN_EXPECTS_INSTANCE_PROP_MUST_BE_SATISFIED,
 				TypeProbIds.MixinExpectsMustBeSatisfied,
 				"Mixin_Expects_Must_Be_Satisfies",
 		"Mixin expects must be satisfies");
 		
 //		setUpRule(MIXIN_EXPECTS_STATIC_METHOD_MUST_BE_SATISFIED,
 //				TypeProbIds.MixinExpectsMustBeSatisfied,
 //				"Mixin_Expects_Must_Be_Satisfies",
 //		"Mixin expects must be satisfies");
 		
 //		setUpRule(MIXIN_EXPECTS_STATIC_PROP_MUST_BE_SATISFIED,
 //				TypeProbIds.MixinExpectsMustBeSatisfied,
 //				"Mixin_Expects_Must_Be_Satisfies",
 //		"Mixin expects must be satisfies");
 //		
 //		setUpRule(MTYPE_EXPECTS_SHOULD_NOT_BE_OVERWRITTEN,
 //				TypeProbIds.MTypeExpectsCannotBeOverwritten,
 //				"MType_Expects_Cannot_Be_Overwritten",
 //				"MType expects cannot be overwritten");
 //		
 //		setUpRule(MTYPE_SHOULD_NOT_BE_REFERENCED_DIRECTLY,
 //				TypeProbIds.MTypeShouldNotBeReferencedDirectly,
 //				"MType_Should_Not_Be_Referenced_Directly",
 //				"MType should not be referenced directly");
 		
 		setUpRule(MTYPE_SHOULD_ONLY_BE_MIXINED, 
 				VjoSyntaxProbIds.MTypeShouldOnlyBeMixined,
 				"MType_Should_Only_Be_Mixined",
 				"MType should only be mixined");
 		
 		setUpRule(INNER_TYPE_SHOULD_NOT_HAVE_NAME_SAME_AS_EMBEDDING_TYPE,
 				TypeProbIds.HidingEnclosingType,
 				"Inner_type_should_not_have_name_same_as_embedding_type",
 				"Inner Type should not have same name as embedding type");
 		
 		setUpRule(INSTANCE_INNER_TYPE_SHOULD_NOT_HAVE_STATIC_MEMBERS,
 				TypeProbIds.CannotDefineStaticMembersInInstanceInnerType,
 				"Instance_inner_type_should_not_have_static_members",
 				"Instance inner type should not have static members");
 		
 		setUpRule(OVERLOAD_METHOD_SHOULD_NOT_OVERLAP,
 				MethodProbIds.AmbiguousOverloadingMethods,
 				"Overload_Methods_Should_not_Overlap",
 				"Overloading method having ambiguous invocation");
 		
 //		setUpRule(OVERLOAD_METHOD_SHOULD_NOT_HAVE_VARIABLE_MODIFIER,
 //				MethodProbIds.OverloadMethodWithVariableModifiers,
 //				"Overload_Methods_Should_not_have_variable_modifiers",
 //				"Overload methods having varible modifiers");
 //		
 		setUpRule(OVERRIDE_METHOD_SHOULD_NOT_REDUCE_VISIBILITY,
 				MethodProbIds.OverrideSuperMethodWithReducedVisibility,
 				"Override_Methods_Should_Not_Reduce_Visibility",
 				"Overriding method with a reduced visibility");
 		
 //		setUpRule(TWO_EXPRS_SHOULD_BE_CONSISTENT, 
 //				TypeProbIds.IncompatibleTypesInConditionalOperator, 
 //				"Two_Exprs_Should_Be_Consistent", 
 //				"Require expressions to be consistent here");
 //		
 		setUpRule(EXPR_SHOULD_BE_BOOL, 
 				TypeProbIds.TypeMismatch, 
 				"Expr_Should_Be_Boolean", 
 				"Require boolean expression here");
 		
 		setUpRule(STMT_SHOULD_BE_REACHABLE,
 				MethodProbIds.UnreachableStmt,
 				"Stmt_is_Unreachable",
 				"Statement is unreachable");
 		
 		setUpRule(EXPR_SHOULD_BE_BOOL_OR_NUMBER, 
 				TypeProbIds.TypeMismatch, 
 				"Expr_Should_Be_Boolean_Or_Number", 
 				"Require boolean|number expression here");
 		
 		setUpRule(EXPR_SHOULD_BE_NUMBER, 
 				TypeProbIds.TypeMismatch, 
 				"Expr_Should_Be_Number", 
 				"Require number expression here");
 		
 //		setUpRule(EXPR_SHOULD_BE_NUMBER_OR_STRING, 
 //				TypeProbIds.TypeMismatch, 
 //				"Expr_Should_Be_Number_Or_String", 
 //				"Require number|string expression here");
 //		
 		setUpRule(ASSIGNABLE, 
 				TypeProbIds.IncompatibleTypesInEqualityOperator, 
 				"Illegal_Assignement", 
 				"Assignement should have compatible type");
 		
 		setUpRule(OBJLITERAL_ASSIGNABLE, 
 				TypeProbIds.IncompatibleTypesInEqualityOperator, 
 				"Illegal_Assignement_ObjectLiteral", 
 				"Assignement should have compatible type");
 		
 		setUpRule(SHOULD_NOT_ASSIGN_TO_ENUM, 
 				FieldProbIds.FinalFieldAssignment, 
 				"Enum_Cannot_Be_Reassigned", 
 				"Should not assign to enum type");
 		
 		setUpRule(SHOULD_NOT_ASSIGN_TO_FINAL, 
 				FieldProbIds.FinalFieldAssignment, 
 				"Final_Field_Cannot_Be_Assigned", 
 				"Should not assign to final field");
 		
 //		setUpRule(CASTABLE, 
 //				TypeProbIds.TypeMismatch, 
 //				"Illegal_Cast", 
 //				"Cast should have compatible type");
 //		
 //		setUpRule(QUALIFIER_SHOULD_NOT_BE_NULL, 
 //				FieldProbIds.UnqualifiedFieldAccess, 
 //				"Qualifier_Should_Not_Be_Null", 
 //				"Qualifier shouldn't be null");
 //		
 //		setUpRule(QUALIFIER_SHOULD_BE_DEFINED, 
 //				VarProbIds.UndefinedName, 
 //				"Qualifier_Should_Be_Defined", 
 //				"Qualifier is not found in the context");
 		
 		setUpRule(METHOD_SHOULD_BE_DEFINED, 
 				MethodProbIds.UndefinedMethod, 
 				"Method_Should_Be_Defined", 
 				"Method is not found in the context");
 		
 		setUpRule(INFERRED_TYPE_METHOD_SHOULD_BE_DEFINED, 
 				MethodProbIds.UndefinedMethod, 
 				"Inferred_Type_Method_Should_Be_Defined", 
 				"Method is not found in the context");
 		
 		setUpRule(FUNCTION_SHOULD_BE_DEFINED,
 				MethodProbIds.UndefinedFunction,
 				"Function_Should_Be_Defined",
 				"Function is not found in the scope or global context");
 		
 		setUpRule(METHOD_SHOULD_BE_VISIBLE, 
 				MethodProbIds.NotVisibleMethod, 
 				"Method_Is_Not_Visible", 
 				"Method is not visible in the context");
 		
 		setUpRule(CONSTRUCTOR_SHOULD_BE_VISIBLE, 
 				MethodProbIds.NotVisibleConstructor, 
 				"Constructor_Is_Not_Visible", 
 				"Constructor is not visible in the context");
 		
 		setUpRule(NONE_VOID_METHOD_SHOULD_HAVE_RETURN, 
 				MethodProbIds.ShouldReturnValue, 
 				"Method_Missing_Return", 
 				"None void method requires return value");
 		
 		setUpRule(VOID_METHOD_SHOULD_NOT_HAVE_RETURN, 
 				MethodProbIds.VoidMethodReturnsValue, 
 				"Void_Method_Returns_Value", 
 				"Void method can't return value");
 		
 		setUpRule(METHOD_RETURN_VALUE_SHOULD_COMPLY, 
 				TypeProbIds.TypeMismatch, 
 				"Method_Return_Value_Should_Comply_With_Definition", 
 				"Method should return value compatible with its definition");
 		
 		setUpRule(THROW_TYPE_SHOULD_COMPLY, 
                 TypeProbIds.TypeMismatch, 
                 "THROW_TYPE_SHOULD_COMPLY", 
                 "The type of throw statement should be inherits from Error type");
 		
 		setUpRule(METHOD_ARGS_TYPE_SHOULD_MATCH, 
 				MethodProbIds.ParameterMismatch, 
 				"Arguments_Type_Of_Method_Should_Match", 
 				"The arguments type of the method don't match");
 		
 		setUpRule(METHOD_ARGS_TYPE_SHOULD_NOT_BE_VOID, 
 				MethodProbIds.ParameterMismatch, 
 				"Parameters_Type_Of_Method_Should_Not_Be_Void", 
 				"The parameters type of the method should not be void");
 		
 		setUpRule(METHOD_WRONG_NUMBER_OF_ARGS, 
 				MethodProbIds.WrongNumberOfArguments, 
 				"Wrong_Number_Of_Arguments_For_Method", 
 				"The number of method's arguments don't match");
 		
 		setUpRule(STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE, 
 				MethodProbIds.UndefinedMethod, 
 				"Accessing_Static_Method_From_None_Static_Context", 
 				"Can't access static method from none-static context");
 		
 		setUpRule(NONE_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE, 
 				MethodProbIds.UndefinedMethod, 
 				"Accessing_None_Static_Method_From_Static_Context", 
 				"Can't access none-static method from static context");
 		
 //		setUpRule(ARRAY_CREATION_VAL_SHOULD_BE_DEFINED, 
 //				FieldProbIds.UndefinedField, 
 //				"Array_Creation_Val_Should_Be_Defined", 
 //				"val should be defined if using vjo.create(val,dimension)");
 //		
 		setUpRule(ARRAY_CREATION_DIMENSION_TYPE_SHOULD_BE_INTEGER, 
 				MethodProbIds.ParameterMismatch, 
 				"Array_Creation_Dimension_Should_Be_Interger", 
 				"dimension should be integer if using vjo.create(val,dimension)");
 //		
 //		setUpRule(CLASS_OBJ_SHOULD_BE_DEFINED, 
 //				VarProbIds.UndefinedName, 
 //				"Class_Object_Should_Be_Defined", 
 //				"class obj should be defined");
 		
 		setUpRule(OBJ_SHOULD_BE_CLASS_TYPE, 
 				TypeProbIds.ObjectMustBeClass, 
 				"Obj_Should_Be_Class_Type", 
 				"object should be class type");
 		
 		setUpRule(CLASS_SHOULD_BE_INSTANTIATABLE,
 				TypeProbIds.InvalidClassInstantiation,
 				"Class_Should_Be_Instantiatable",
 				"class should be able to instantiate");
 		
 		setUpRule(FINAL_PROPERTY_SHOULD_BE_INITIALIZED, 
 				FieldProbIds.UninitializedBlankFinalField, 
 				"Final_Property_In_the_Constructs_Method_Should_Be_Initialized", 
 				"final property in the constructs method should be initialized");
 		
 //		setUpRule(ITEM_TYPE_SHOULD_MATCH_ARRAY_TYPE, 
 //				TypeProbIds.TypeMismatch, 
 //				"Item_Type_Should_Match_Array_Type", 
 //				"item type should compatible with array type");
 //		
 		setUpRule(ARRAY_INDEX_SHOULD_BE_INT_OR_STRING_TYPE, 
 				TypeProbIds.TypeMismatch, 
 				"Array_Index_Should_Be_Integer_Or_String_Type", 
 				"The index of the array should with integer or string type");
 		
 //		setUpRule(PACKAGE_SHOULD_BE_DEFINED, 
 //				VarProbIds.UndefinedName, 
 //				"Package_Should_Be_Defined", 
 //				"Package is not found in the scope");
 //		
 		setUpRule(PROPERTY_SHOULD_BE_DEFINED, 
 				FieldProbIds.UndefinedField, 
 				"Property_Should_Be_Defined", 
 				"Property is not found in the scope");
 		
 		setUpRule(PROPERTY_SHOULD_BE_VISIBLE, 
 				FieldProbIds.NotVisibleField, 
 				"Property_Is_Not_Visible", 
 				"Property is not visible in the context");
 		
 		setUpRule(PROPERTY_SHOULD_NOT_HIDE_PARENT_PROPERTY,
 				FieldProbIds.AmbiguousField,
 				"Property_Should_Not_Hide_Parent_Field",
 				"Property should not hide parent field");
 		
 		setUpRule(STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE, 
 				FieldProbIds.NonStaticAccessToStaticField, 
 				"Accessing_Static_Field_From_None_Static_Context", 
 				"Can't access static field from none-static context");
 		
 		setUpRule(NONE_STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE, 
 				FieldProbIds.NonStaticFieldFromStaticInvocation, 
 				"Accessing_None_Static_Field_From_Static_Context", 
 				"Can't access none-static field from static context");
 		
 //		setUpRule(WITH_SCOPE_SHOULD_NOT_CONFLICT_LOCAL, 
 //				VarProbIds.ArgumentHidingLocalVariable, 
 //				"With_Scope_Should_Not_Conflict_With_Local_Scope", 
 //				"With scope may conflicts with local scope, check with scope properties and methods");
 		
 		setUpRule(DISCOURAGED_NESTED_WITH,
 				VjoSyntaxProbIds.NestedWithDiscouraged,
 				"Nested_With_Statements_Is_Discouraged",
 				"Nested with statements is discouraged");
 		
 		setUpRule(OBJECT_LITERAL_SHOULD_HAVE_UNIQUE_KEY, 
 				FieldProbIds.DuplicateField, 
 				"Object_Literal_Should_Have_Unique_Key", 
 				"Object literal cannot have multiple keys with the same name");
 		
 		setUpRule(VJO_SYNTAX_CORRECTNESS, 
 				VjoSyntaxProbIds.IncorrectVjoSyntax, 
 				"Vjo_Syntax_Correctness", 
 				"Vjo Syntax isn't correct");
 		
 		setUpRule(MISSING_ENDTYPE, 
 				VjoSyntaxProbIds.MissingEndType, 
 				"Missing_EndType", 
 				"Missing endType");
 		
 //		setUpRule(MULTIPLE_INHERITS, 
 //				VjoSyntaxProbIds.MultipleInherits, 
 //				"Multiple_Inherits", 
 //				"Multiple inherits");
 //		
 //		setUpRule(MULTIPLE_PROPS, 
 //				VjoSyntaxProbIds.MultipleProps, 
 //				"Multiple_Props", 
 //				"Multiple props");
 //		
 //		setUpRule(MULTIPLE_PROTOS, 
 //				VjoSyntaxProbIds.MultipleProtos, 
 //				"Multiple_Protos", 
 //				"Multiple protos");
 //		
 		setUpRule(NAME_SPACE_COLLISION,
 				VjoSyntaxProbIds.NameSpaceCollision,
 				"NAME_SPACE_COLLISION",
 				"namespace collision");
 		
 		setUpRule(REDUNDANT_IMPORT,
 				VjoSyntaxProbIds.RedundantImport,
 				"REDUNDANT_IMPORT",
 				"redundant import");
 		
 		setUpRule(PUBLIC_CLASS_SHOULD_RESIDE_IN_CORRESPONDING_FILE,
 				TypeProbIds.IsClassPathCorrect,
 				"PUBLIC_CLASS_SHOULD_RESIDE_IN_CORRESPONDING_FILE",
 				"Public class should reside in corresponding file path");
 		
 		setUpRule(MAIN_CLASS_SHOULD_BE_PUBLIC,
 				TypeProbIds.IllegalModifierForClass,
 				"MAIN_CLASS_SHOULD_BE_PUBLIC",
 				"Main class should be public");
 		
 		setUpRule(UNKNOWN_TYPE_MISSING_IMPORT, 
 				VjoSyntaxProbIds.TypeUnknownMissingImport, 
 				"UNKNOWN_TYPE_MISSING_IMPORT", 
 				"Unknown type, missing import");
 		
 		setUpRule(UNKNOWN_TYPE_NOT_IN_TYPE_SPACE, 
 				VjoSyntaxProbIds.TypeUnknownNotInTypeSpace, 
 				"UNKNOWN_TYPE_NOT_IN_TYPE_SPACE", 
 				"Unknown type, not found in type space");
 		
 		setUpRule(UNKNOWN_TYPE_NOT_IN_TYPE_SPACE_INACTIVENEEDS, 
                 VjoSyntaxProbIds.TypeUnknownNotInTypeSpace, 
                 "UNKNOWN_TYPE_NOT_IN_TYPE_SPACE_INACTIVENEEDS", 
                 "Unknown type, not found in type space wtih inactive needs");
 		
 		setUpRule(PRIVATE_METHOD_REFERENCED_NOWHERE,
 				MethodProbIds.UnusedPrivateMethod,
 				"PRIVATE_METHOD_INVOKED_NOWHERE",
 				"Private method is not invoked anywhere");
 		
 		setUpRule(PRIVATE_PROPERTY_REFERENCED_NOWHERE,
 				FieldProbIds.UnusedPrivateField,
 				"PRIVATE_PROPERTY_REFERENCED_NOWHERE",
 				"Private property is not referenced anywhere");
 		
 		setUpRule(INVALID_IDENTIFIER,
 				VjoSyntaxProbIds.InvalidIdentifier,
 				"INVALID_IDENTIFIER",
 				"Invalid identifier found");
 		
 		setUpRule(INVALID_IDENTIFIER_WITH_KEYWORD,
 				VjoSyntaxProbIds.InvalidIdentifier,
 				"INVALID_IDENTIFIER_WITH_KEYWORD",
 				"Invalid identifier found");
 		
 		setUpRule(CLASS_NAME_CANNOT_HAVE_ILLEGAL_TOKEN,
 				VjoSyntaxProbIds.TypeHasIllegalToken,
 				"CLASS_NAME_CANNOT_HAVE_NUMERIC_TOKEN",
 				"Class name cannot have numeric token");
 		
 		setUpRule(CANNOT_USE_UNINTIALIZED_TYPE,
 				FieldProbIds.FieldInitializationDependsOnUnintializedTypes,
 				"Cannot_use_unintialized_type",
 				"Cannot use unintialized type");
 		
 		setUpRule(CANNOT_USE_INACTIVE_NEED_ACTIVELY,
 				TypeProbIds.InactiveNeedsInUse,
 				"CANNOT_USE_INACTIVE_NEED_ACTIVELY",
 				"Can not use inactive need type actively");
 		
 		setUpRule(UNUSED_ACTIVE_NEEDS,
 				TypeProbIds.UnusedActiveNeeds,
 				"UNUSEED_ACTIVE_NEEDS",
 				"Unused active needs");
 		
 		setUpRule(GENERIC_PARAM_TYPE_MISMATCH,
 				TypeProbIds.GenericParamTypeMismatch,
 				"GENERIC_PARAM_TYPE_MISMATCH",
 				"Generic param type mismatch");
 		
 		setUpRule(GENERIC_PARAM_NUM_MISMATCH,
 				TypeProbIds.GenericParamNumMismatch,
 				"GENERIC_PARAM_NUM_MISMATCH",
 				"Generic param number mismatch");
 		
 		setUpRule(ATTRIBUTOR_SHOULD_NOT_USE_VJ_RUNTIME,
 				TypeProbIds.AttributorTypeUsingVjRuntime,
 				"ATTRIBUTOR_SHOULD_NOT_USE_VJ_KEYWORD",
 				"Attributor type using vj$ runtime");
 		
 		//initialize the err messages without verbose
 		loadErrMsgs();
 	}
 
 	private void initRuleSets() {
 		
 		TYPE_CHECK.setRuleSetName("TYPE_CHECK");
 		TYPE_CHECK.setRuleSetDesription("Type Checking");
 		
 		JAVA_COMPAT_TYPE_CHECK.setRuleSetName("JAVA_COMPAT_TYPE_CHECK");
 		JAVA_COMPAT_TYPE_CHECK.setRuleSetDesription("Java Compatible Type Checking");
 		
 //		HIERARCHY_AND_STRUCTURE.setRuleSetName("Hierachy_And_Structure");
 //		HIERARCHY_AND_STRUCTURE.setRuleSetDesription("Hierarchical and structural");
 		
 		JAVASCRIPT_EXTENSIONS.setRuleSetName("JAVASCRIPT_SEMANTIC");
 		JAVASCRIPT_EXTENSIONS.setRuleSetDesription("JavaScript Semantic Validation");
 		
 		GLOBAL_RULES.setRuleSetName("GLOBAL_RULES");
 		GLOBAL_RULES.setRuleSetDesription("Accidental Globals");
 		
 		
 //		UNIQUENESS.setRuleSetName("Uniqueness");
 //		UNIQUENESS.setRuleSetDesription("Uniqueness violation");
 		
 //		ACCESSIBILITY.setRuleSetName("Accessibility");
 //		ACCESSIBILITY.setRuleSetDesription("Accessibility violation");
 		
 //		MISC.setRuleSetName("Misc");
 //		MISC.setRuleSetDesription("Misc Problems");
 		
 		VJO_SYNTAX.setRuleSetName("VJO_SPECIFIC");
 		VJO_SYNTAX.setRuleSetDesription("Vjo Type Library Specific");
 		
 		addRuleSet(JAVASCRIPT_EXTENSIONS);
 		addRuleSet(TYPE_CHECK);
 		addRuleSet(GLOBAL_RULES);
 		addRuleSet(JAVA_COMPAT_TYPE_CHECK);
 //		addRuleSet(UNIQUENESS);
 //		addRuleSet(ACCESSIBILITY);
 //		addRuleSet(MISC);
 		addRuleSet(VJO_SYNTAX);
 	}
 	
 	private void addRuleSet(IVjoSemanticRuleSet ruleSet){
 		if(m_ruleSetMap == null){
 			m_ruleSetMap = new LinkedHashMap<String, IVjoSemanticRuleSet>();
 		}
 		m_ruleSetMap.put(ruleSet.getRuleSetName(), ruleSet);
 	}
 	
 	public List<IVjoSemanticRuleSet> getRuleSets(){
 		final List<IVjoSemanticRuleSet> ruleSets = new ArrayList<IVjoSemanticRuleSet>();
 		ruleSets.addAll(m_ruleSetMap.values());
 		return ruleSets;
 	}
 	
 	public IVjoSemanticRule<?> getRule(String ruleSetName, String ruleName){
 		IVjoSemanticRuleSet ruleSet = m_ruleSetMap.get(ruleSetName);
 		if(ruleSet != null){
 			return ruleSet.getRule(ruleName);
 		}
 		
 		return null;
 	}
 }
 
