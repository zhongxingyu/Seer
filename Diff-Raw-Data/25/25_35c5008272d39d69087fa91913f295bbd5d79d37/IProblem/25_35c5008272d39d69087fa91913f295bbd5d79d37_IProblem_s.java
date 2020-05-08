 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package descent.core.compiler;
 
 /**
  * Richer description of a D problem, as detected by the compiler or some of the underlying
  * technology reusing the compiler.
  * 
  * A problem provides access to:
  * <ul>
  * <li> its location (originating source file name, source position, line number), </li>
  * <li> its message description and a predicate to check its severity (warning or error). </li>
  * <li> its ID : a number identifying the very nature of this problem. All possible IDs for standard Java 
  * problems are listed as constants on <code>IProblem</code>, </li>
  * <li> its marker type : a string identfying the problem creator. It corresponds to the marker type
  * chosen if this problem was to be persisted. Standard Java problems are associated to marker
  * type "descent.core.problem"), </li>
  * <li> its category ID : a number identifying the category this problem belongs to. All possible IDs for 
  * standard Java problem categories are listed in this class. </li>
  * </ul>
  * 
  * Note: the compiler produces IProblems internally, which are turned into markers by the DBuilder
  * so as to persist problem descriptions. This explains why there is no API allowing to reach IProblem detected
  * when compiling. However, the D problem markers carry equivalent information to IProblem, in particular
  * their ID (attribute "id") is set to one of the IDs defined on this interface.
  */
 public interface IProblem {
 	
 	/**
 	 * Answer back the original arguments recorded into the problem.
 	 * @return the original arguments recorded into the problem
 	 */
 	String[] getArguments();
 	
 	/**
 	 * Returns the problem id
 	 * 
 	 * @return the problem id
 	 */
 	int getID();
 	
 	/**
 	 * Answer a localized, human-readable message string which describes the problem.
 	 * 
 	 * @return a localized, human-readable message string which describes the problem
 	 */
 	String getMessage();
 	
 	/**
 	 * Answer the file name in which the problem was found.
 	 * 
 	 * @return the file name in which the problem was found
 	 */
 	char[] getOriginatingFileName();
 	
 	/**
 	 * Answer the end position of the problem (inclusive), or -1 if unknown.
 	 * 
 	 * @return the end position of the problem (inclusive), or -1 if unknown
 	 */
 	int getSourceEnd();
 
 	/**
 	 * Answer the line number in source where the problem begins.
 	 * 
 	 * @return the line number in source where the problem begins
 	 */
 	int getSourceLineNumber();
 
 	/**
 	 * Answer the start position of the problem (inclusive), or -1 if unknown.
 	 * 
 	 * @return the start position of the problem (inclusive), or -1 if unknown
 	 */
 	int getSourceStart();
 	
 	/**
 	 * Checks the severity to see if the Error bit is set.
 	 * 
 	 * @return true if the Error bit is set for the severity, false otherwise
 	 */
 	boolean isError();
 
 	/**
 	 * Checks the severity to see if the Error bit is not set.
 	 * 
 	 * @return true if the Error bit is not set for the severity, false otherwise
 	 */
 	boolean isWarning();
 	
 	/**
 	 * Set the end position of the problem (inclusive), or -1 if unknown.
 	 * Used for shifting problem positions.
 	 * 
 	 * @param sourceEnd the given end position
 	 */
 	void setSourceEnd(int sourceEnd);
 
 	/**
 	 * Set the line number in source where the problem begins.
 	 * 
 	 * @param lineNumber the given line number
 	 */
 	void setSourceLineNumber(int lineNumber);
 
 	/**
 	 * Set the start position of the problem (inclusive), or -1 if unknown.
 	 * Used for shifting problem positions.
 	 * 
 	 * @param sourceStart the given start position
 	 */
 	void setSourceStart(int sourceStart);
 	
 	/** 
 	 * Returns an integer identifying the category of this problem. Categories, like problem IDs are
 	 * defined in the context of some marker type. Custom implementations of <code>IProblem</code>
 	 * may choose arbitrary values for problem/category IDs, as long as they are associated with a different
 	 * marker type.
 	 * Standard D problem markers (i.e. marker type is "descent.core.core.problem") carry an
 	 * attribute "categoryId" persisting the originating problem category ID as defined by this method.
 	 * @return id - an integer identifying the category of this problem
 	 */
 	public abstract int getCategoryID();
 
 	/**
 	 * Returns the marker type associated to this problem, if it gets persisted into a marker by the DBuilder
 	 * Standard D problems are associated to marker type "descent.core.problem").
 	 * Note: problem markers are expected to extend "org.eclipse.core.resources.problemmarker" marker type.
 	 * @return the type of the marker which would be associated to the problem
 	 */
 	public abstract String getMarkerType();
 	
 	/**
 	 * Returns the names of the extra marker attributes associated to this problem when persisted into a marker 
 	 * by the JavaBuilder. Extra attributes are only optional, and are allowing client customization of generated
 	 * markers. By default, no EXTRA attributes is persisted, and a categorized problem only persists the following attributes:
 	 * <ul>
 	 * <li>	<code>IMarker#MESSAGE</code> -&gt; {@link IProblem#getMessage()}</li>
 	 * <li>	<code>IMarker#SEVERITY</code> -&gt; <code> IMarker#SEVERITY_ERROR</code> or 
 	 *         <code>IMarker#SEVERITY_WARNING</code> depending on {@link IProblem#isError()} or {@link IProblem#isWarning()}</li>
 	 * <li>	<code>IJavaModelMarker#ID</code> -&gt; {@link IProblem#getID()}</li>
 	 * <li>	<code>IMarker#CHAR_START</code>  -&gt; {@link IProblem#getSourceStart()}</li>
 	 * <li>	<code>IMarker#CHAR_END</code>  -&gt; {@link IProblem#getSourceEnd()}</li>
 	 * <li>	<code>IMarker#LINE_NUMBER</code>  -&gt; {@link IProblem#getSourceLineNumber()}</li>
 	 * <li>	<code>IJavaModelMarker#ARGUMENTS</code>  -&gt; some <code>String[]</code> used to compute quickfixes </li>
 	 * <li>	<code>IJavaModelMarker#CATEGORY_ID</code> -&gt; {@link CategorizedProblem#getCategoryID()}</li>
 	 * </ul>
 	 * The names must be eligible for marker creation, as defined by <code>IMarker#setAttributes(String[], Object[])</code>, 
 	 * and there must be as many names as values according to {@link #getExtraMarkerAttributeValues()}.
 	 * Note that extra marker attributes will be inserted after default ones (as described in {@link CategorizedProblem#getMarkerType()},
 	 * and thus could be used to override defaults.
 	 * @return the names of the corresponding marker attributes
 	 */
 	public String[] getExtraMarkerAttributeNames();
 
 	/**
 	 * Returns the respective values for the extra marker attributes associated to this problem when persisted into 
 	 * a marker by the JavaBuilder. Each value must correspond to a matching attribute name, as defined by
 	 * {@link #getExtraMarkerAttributeNames()}. 
 	 * The values must be eligible for marker creation, as defined by <code>IMarker#setAttributes(String[], Object[])</code>.
 	 * @return the values of the corresponding extra marker attributes
 	 */
 	public Object[] getExtraMarkerAttributeValues();
 	
 	/**
 	 * List of standard category IDs used by Java problems, more categories will be added 
 	 * in the future.
 	 */
 	int CAT_UNSPECIFIED = 0;
 	/** Category for problems related to buildpath */
 	int CAT_BUILDPATH = 10;
 	/** Category for fatal problems related to syntax */
 	int CAT_SYNTAX = 20;
 	/** Category for fatal problems in import statements */
 	int CAT_IMPORT = 30;
 	/** Category for fatal problems related to types, could be addressed by some type change */
 	int CAT_TYPE = 40;
 	/** Category for fatal problems related to type members, could be addressed by some field or method change */
 	int CAT_MEMBER = 50;
 	/** Category for fatal problems which could not be addressed by external changes, but require an edit to be addressed */
 	int CAT_INTERNAL = 60;	
 	/** Category for optional problems in DDoc */
 	int CAT_JAVADOC = 70;
 	/** Category for optional problems related to coding style practices */
 	int CAT_CODE_STYLE = 80;
 	/** Category for optional problems related to potential programming flaws */
 	int CAT_POTENTIAL_PROGRAMMING_PROBLEM = 90;
 	/** Category for optional problems related to naming conflicts */
 	int CAT_NAME_SHADOWING_CONFLICT = 100;
 	/** Category for optional problems related to deprecation */
 	int CAT_DEPRECATION = 110;
 	/** Category for optional problems related to unnecessary code */
 	int CAT_UNNECESSARY_CODE = 120;
 	/** Category for optional problems related to type safety in generics */
 	int CAT_UNCHECKED_RAW = 130;
 	/** Category for optional problems related to internationalization of String literals */
 	int CAT_NLS = 140;
 	/** Category for optional problems related to access restrictions */
 	int CAT_RESTRICTION = 150;
 	
 	/* Problems during lexing, parsing, and semantic analysis, generated automatically,
 	   65 is the first semantic error in the list. */
 	int UnterminatedBlockComment = 1;
 	int UnterminatedPlusBlockComment = 2;
 	int IncorrectNumberOfHexDigitsInEscapeSequence = 3;
 	int UndefinedEscapeHexSequence = 4;
 	int UnterminatedStringConstant = 5;
 	int OddNumberOfCharactersInHexString = 6;
 	int NonHexCharacter = 7;
 	int UnterminatedCharacterConstant = 8;
 	int BinaryDigitExpected = 9;
 	int OctalDigitExpected = 10;
 	int HexDigitExpected = 11;
 	int UnsupportedCharacter = 12;
 	int InvalidUtfCharacter = 13;
 	int ThreeEqualsIsNoLongerLegal = 14;
 	int NotTwoEqualsIsNoLongerLegal = 15;
 	int LSuffixDeprecated = 16;
 	int InvalidPragmaSyntax = 17;
 	int UnrecognizedCharacterEntity = 18;
 	int UnterminatedNamedEntity = 19;
 	int UndefinedEscapeSequence = 20;
 	int InvalidUtf8Sequence = 21;
 	int IntegerOverflow = 22;
 	int SignedIntegerOverflow = 23;
 	int UnrecognizedToken = 24;
 	int BinaryExponentPartRequired = 25;
 	int ExponentExpected = 26;
 	int ISuffixDeprecated = 27;
 	int ParsingErrorInsertTokenAfter = 28;
 	int ParsingErrorDeleteToken = 29;
 	int ParsingErrorInsertToComplete = 30;
 	int EnumDeclarationIsInvalid = 31;
 	int MismatchedStringLiteralPostfixes = 32;
 	int NoIdentifierForDeclarator = 33;
 	int AliasCannotHaveInitializer = 34;
 	int CStyleCastIllegal = 35;
 	int InvalidLinkageIdentifier = 36;
 	int VariadicArgumentCannotBeOutOrRef = 37;
 	int VariadicNotAllowedInDelete = 38;
 	int NoIdentifierForTemplateValueParameter = 39;
 	int UnexpectedIdentifierInDeclarator = 40;
 	int RedundantStorageClass = 41;
 	int RedundantProtectionAttribute = 42;
 	int UseBracesForAnEmptyStatement = 43;
 	int MultipleDeclarationsMustHaveTheSameType = 44;
 	int RedundantInStatement = 45;
 	int RedundantOutStatement = 46;
 	int StatementExpectedToBeCurlies = 47;
 	int InvalidScopeIdentifier = 48;
 	int OnScopeDeprecated = 49;
 	int DollarInvalidOutsideBrackets = 50;
 	int IftypeDeprecated = 51;
 	int IfAutoDeprecated = 52;
 	int VariadicTemplateParameterMustBeTheLastOne = 53;
 	int NeedSizeOfRightmostArray = 54;
 	int ConflictingStorageClass = 55;
 	int ValueIsLargerThanAByte = 56;
 	int UnterminatedTokenStringConstant = 57;
 	int HeredocRestOfLineShouldBeBlank = 58;
 	int IdentifierExpectedForHeredoc = 59;
 	int DelimitedStringMustEndInValue = 60;
 	int TypeOnlyAllowedIfAnonymousEnumAndNoEnumType = 61;
 	int IfTypeThereMustBeAnInitializer = 62;
 	int InvariantAsAttributeIsOnlySupportedInD2 = 63;
 	int ConstAsAttributeIsOnlySupportedInD2 = 64;
 	int OnlyOneCaseAllowedForStartOfCaseRange = 65;
 	int SymbolConflictsWithSymbolAtLocation = 66;
 	int SymbolAtLocationConflictsWithSymbolAtLocation = 67;
 	int PropertyCanNotBeRedefined = 68;
 	int CircularDefinition = 69;
 	int EnumValueOverflow = 70;
 	int EnumMustHaveAtLeastOneMember = 71;
 	int EnumBaseTypeMustBeOfIntegralType = 72;
 	int ForwardReferenceOfSymbol = 73;
 	int ForwardReferenceOfEnumSymbolDotSymbol = 74;
 	int ForwardReferenceWhenLookingFor = 75;
 	int BaseEnumIsForwardReference = 76;
 	int CannotResolveForwardReference = 77;
 	int EnumIsForwardReference = 78;
 	int IntegerConstantExpressionExpected = 79;
 	int ThisNotInClassOrStruct = 80;
 	int ThisOnlyAllowedInNonStaticMemberFunctions = 81;
 	int SuperOnlyAllowedInNonStaticMemberFunctions = 82;
 	int SuperNotInClass = 83;
 	int ClassHasNoSuper = 84;
 	int BaseTypeMustBeInterface = 85;
 	int MemberIsPrivate = 86;
 	int UsedAsAType = 87;
 	int ExternSymbolsCannotHaveInitializers = 88;
 	int VoidsHaveNoValue = 89;
 	int CannotInferTypeFromThisArrayInitializer = 90;
 	int NoDefinition = 91;
 	int DuplicatedInterfaceInheritance = 92;
 	int BaseTypeMustBeClassOrInterface = 93;
 	int FieldsNotAllowedInInterfaces = 94;
 	int UndefinedIdentifier = 95;
 	int NotAMember = 96;
 	int NewAllocatorsOnlyForClassOrStruct = 97;
 	int DeleteDeallocatorsOnlyForClassOrStruct = 98;
 	int ConstructorsOnlyForClass = 99;
 	int ConstructorsOnlyForClassOrStruct = 100;
 	int DestructorsOnlyForClass = 101;
 	int InvariantsOnlyForClassStructUnion = 102;
 	int FunctionDoesNotOverrideAny = 103;
 	int CannotOverrideFinalFunctions = 104;
 	int OverrideOnlyForClassMemberFunctions = 105;
 	int FunctionMustReturnAResultOfType = 106;
 	int MoreThanOneInvariant = 107;
 	int ParameterMultiplyDefined = 108;
 	int SymbolNotFound = 109;
 	int StatementIsNotReachable = 110;
 	int VoidFunctionsHaveNoResult = 111;
 	int ReturnStatementsCannotBeInContracts = 112;
 	int NotAnAggregateType = 113;
 	int UnrecognizedPragma = 114;
 	int AnonCanOnlyBePartOfAnAggregate = 115;
 	int PragmaIsMissingClosingSemicolon = 116;
 	int CannotImplicitlyConvert = 117;
 	int ForbiddenReference = 118;
 	int DiscouragedReference = 119;
 	int Task = 120;
 	int UndefinedType = 121;
 	int IsClassPathCorrect = 122;
 	int FunctionsCannotBeConstOrAuto = 123;
 	int FunctionsCannotBeScopeOrAuto = 124;
 	int NonVirtualFunctionsCannotBeAbstract = 125;
 	int CannotBeBothAbstractAndFinal = 126;
 	int ModifierCannotBeAppliedToVariables = 127;
 	int StructsCannotBeAbstract = 128;
 	int UnionsCannotBeAbstract = 129;
 	int AliasCannotBeConst = 130;
 	int OneArgumentOfTypeExpected = 131;
 	int IllegalMainParameters = 132;
 	int MustReturnIntOrVoidFromMainFunction = 133;
 	int AtLeastOneArgumentOfTypeExpected = 134;
 	int FirstArgumentMustBeOfType = 135;
 	int StringExpectedForPragmaMsg = 136;
 	int LibPragmaMustRecieveASingleArgumentOfTypeString = 137;
 	int StringExpectedForPragmaLib = 138;
 	int CannotHaveOutOrInoutParameterOfTypeStaticArray = 139;
 	int CannotHaveParameterOfTypeVoid = 140;
 	int FunctionsCannotReturnStaticArrays = 141;
 	int UnrecongnizedTrait = 142;
 	int CanOnlyConcatenateArrays = 143;
 	int ArrayIndexOutOfBounds = 144;
 	int ArrayIndexOutOfBounds2 = 145;
 	int AssertionFailed = 146;
 	int AssertionFailedNoMessage = 147;
 	int ExpressionIsNotEvaluatableAtCompileTime = 148;
 	int UndefinedProperty = 149;
 	int DeprecatedProperty = 150;
 	int FileNameMustBeString = 151;
 	int FileImportsMustBeSpecified = 152;
 	int FileNotFound = 153;
 	int ErrorReadingFile = 154;
 	int ExpressionHasNoEffect = 155;
 	int ConstantIsNotAnLValue = 156;
 	int VersionIdentifierReserved = 157;
 	int CannotPutCatchStatementInsideFinallyBlock = 158;
 	int ExpressionDoesNotGiveABooleanResult = 159;
 	int BreakIsNotInsideALoopOrSwitch = 160;
 	int CaseIsNotInSwitch = 161;
 	int VersionDeclarationMustBeAtModuleLevel = 162;
 	int DebugDeclarationMustBeAtModuleLevel = 163;
 	int GotoCaseNotInSwitch = 164;
 	int GotoDefaultNotInSwitch = 165;
 	int LazyVariablesCannotBeLvalues = 166;
 	int DivisionByZero = 167;
 	int DefaultNotInSwitch = 168;
 	int SwitchAlreadyHasDefault = 169;
 	int ContinueNotInLoop = 170;
 	int ForeachIndexCannotBeRef = 171;
 	int ParametersDoesNotMatchParameterTypes = 172;
 	int IncompatibleParameterStorageClass = 173;
 	int OutCannotBeConst = 174;
 	int OutCannotBeInvariant = 175;
 	int ScopeCannotBeRefOrOut = 176;
 	int IncompatibleTypesForOperator = 177;
 	int IncompatibleTypesForMinus = 178;
 	int SymbolNotDefined = 179;
 	int SymbolNotATemplate = 180;
 	int CannotDeleteType = 181;
 	int NotAnLvalue = 182;
 	int CannotAliasAnExpression = 183;
 	int CannotAssignToStaticArray = 184;
 	int CannotChangeReferenceToStaticArray = 185;
 	int CannotModifyParameterInContract = 186;
 	int BothOverloadsMuchArgumentList = 187;
 	int ExpressionHasNoType = 188;
 	int SymbolNotAnExpression = 189;
 	int SymbolHasNoValue = 190;
 	int TooManyInitializers = 191;
 	int SymbolNotAStaticAndCannotHaveStaticInitializer = 192;
 	int SymbolNotAType = 193;
 	int IncompleteMixinDeclaration = 194;
 	int SymbolNotATemplateItIs = 195;
 	int SymbolCannotBeDeclaredToBeAFunction = 196;
 	int CannotHaveArrayOfType = 197;
 	int SymbolDoesNotMatchAnyTemplateDeclaration = 198;
 	int SymbolDoesNotMatchTemplateDeclaration = 199;
 	int SymbolDoesNotMatchAnyFunctionTemplateDeclaration = 200;
 	int IndexOverflowForStaticArray = 201;
 	int UnknownSize = 202;
 	int NoSizeYetForForwardReference = 203;
 	int SymbolMatchesMoreThanOneTemplateDeclaration = 204;
 	int ExpressionLeadsToStackOverflowAtCompileTime = 205;
 	int StringIndexOutOfBounds = 206;
 	int CannotCreateInstanceOfAbstractClass = 207;
 	int CannotCreateInstanceOfInterface = 208;
 	int WithExpressionsMustBeClassObject = 209;
 	int DeclarationIsAlreadyDefined = 210;
 	int DeclarationIsAlreadyDefinedInAnotherScope = 211;
 	int VersionDefinedAfterUse = 212;
 	int DebugDefinedAfterUse = 213;
 	int NotEnoughArguments = 214;
 	int CanOnlySynchronizeOnClassObjects = 215;
 	int CannotDeduceTemplateFunctionFromArgumentTypes = 216;
 	int CannotDeduceTemplateFunctionFromArgumentTypes2 = 217;
 	int ArrayDimensionExceedsMax = 218;
 	int AStructIsNotAValidInitializerFor = 219;
 	int CannotUseArrayToInitialize = 220;
 	int CircularReferenceTo = 221;
 	int ParameterIsAlreadyDefined = 222;
 	int MemberIsNotAccessible = 223;
 	int SymbolIsNotAccessible = 224;
 	int ThisForSymbolNeedsToBeType = 225;
 	int SymbolHasForwardReferences = 226;
 	int CannotHaveAssociativeArrayOfKey = 227;
 	int CannotHaveAssociativeArrayOf = 228;
 	int CannotHaveArrayOfAuto = 229;
 	int EnclosingLabelForBreakNotFound = 230;
 	int EnclosingLabelForContinueNotFound = 231;
 	int CannotAppendTypeToType = 232;
 	int CannotAppendToStaticArray = 233;
 	int ExpressionIsVoidAndHasNoValue = 234;
 	int NeedMemberFunctionOpCmpForSymbolToCompare = 235;
 	int CompareNotDefinedForComplexOperands = 236;
 	int NeedThisForAddressOfSymbol = 237;
 	int RecursiveMixinInstantiation = 238;
 	int SymbolIsNotOfIntegralType = 239;
 	int DeleteAAKeyDeprecated = 240;
 	int SymbolIsDeprecated = 241;
 	int ShadowingDeclarationIsDeprecated = 242;
 	int ReturnStatementsCannotBeInFinallyScopeExitOrScopeSuccessBodies = 243;
 	int CannotReturnExpressionFromConstructor = 244;
 	int CaseNotFound = 245;
 	int CircularInheritanceOfInterface = 246;
 	int ArgumentToMixinMustBeString = 247;
 	int CannotAccessFrameOfFunction = 248;
 	int OperationNotAllowedOnBool = 249;
 	int SymbolIsNotAScalar = 250;
 	int ImportCannotBeResolved = 251;
 	int SymbolIsNotAVariable = 252;
 	int CatchHidesCatch = 253;
 	int ArithmeticOrStringTypeExpectedForValueParameter = 254;
 	int FunctionsCannotReturnAFunction = 255;
 	int FunctionsCannotReturnATuple = 256;
 	int FunctionsCannotReturnAuto = 257;
 	int FunctionsCannotReturnScope = 258;
 	int RecursiveType = 259;
 	int VariadicFunctionsWithNonDLinkageMustHaveAtLeastOneParameter = 260;
 	int SymbolMustBeAFunction = 261;
 	int FunctionExpectedBeforeCall = 262;
 	int FunctionExpectedBeforeCallNotSymbolOfType = 263;
 	int CircularReferenceOfTypedef = 264;
 	int StringSliceIsOutOfBounds = 265;
 	int ErrorInstantiating = 266;
 	int CaseMustBeAnIntegralOrStringConstant = 267;
 	int DuplicateCaseInSwitchStatement = 268;
 	int SpecialMemberFunctionsNotAllowedForSymbol = 269;
 	int SpecialFunctionsNotAllowedInInterface = 270;
 	int FunctionBodyIsNotAbstractInInterface = 271;
 	int SuperClassConstructorCallMustBeInAConstructor = 272;
 	int ClassConstructorCallMustBeInAConstructor = 273;
 	int NoSuperClassConstructor = 274;
 	int ConstructorCallsNotAllowedInLoopsOrAfterLabels = 275;
 	int MultipleConstructorCalls = 276;
 	int ExpressionIsNotConstantOrDoesNotEvaluateToABool = 277;
 	int StaticIfConditionalCannotBeAtGlobalScope = 278;
 	int CannotBreakOutOfFinallyBlock = 279;
 	int LabelHasNoBreak = 280;
 	int LabelHasNoContinue = 281;
 	int CannotGotoInOrOutOfFinallyBlock = 282;
 	int CalledWithArgumentTypesMatchesBoth = 283;
 	int SymbolIsNotAnArithmeticType = 284;
 	int SymbolIsNotAnArithmeticTypeItIs = 285;
 	int CannotPerformModuloComplexArithmetic = 286;
 	int OperatorNotAllowedOnBoolExpression = 287;
 	int ForeachKeyTypeMustBeIntOrUint = 288;
 	int ForeachKeyTypeMustBeIntOrUintLongOrUlong = 289;
 	int ForeachKeyCannotBeOutOrRef = 290;
 	int NoReverseIterationOnAssociativeArrays = 291;
 	int OnlyOneOrTwoArgumentsForAssociativeArrayForeach = 292;
 	int OnlyOneOrTwoArgumentsForArrayForeach = 293;
 	int ForeachTargetIsNotAnArrayOf = 294;
 	int ForeachKeyCannotBeInout = 295;
 	int ForeachValueOfUTFConversionCannotBeInout = 296;
 	int CannotInferTypeForSymbol = 297;
 	int CannotInferTypeFromInitializer = 298;
 	int NoStorageClassForSymbol = 299;
 	int OnlyOneValueOrTwoKeyValueArgumentsForTupleForeach = 300;
 	int CannotUniquelyInferForeachArgumentTypes = 301;
 	int InvalidForeachAggregate = 302;
 	int NotAnAssociativeArrayInitializer = 303;
 	int ArrayInitializersAsExpressionsNotAllowed = 304;
 	int IftypeConditionCannotBeAtGlobalScope = 305;
 	int SymbolIsNotAFieldOfSymbol = 306;
 	int RecursiveTemplateExpansion = 307;
 	int RecursiveTemplateExpansionForTemplateArgument = 308;
 	int IndexIsNotATypeOrExpression = 309;
 	int CannotHavePointerToSymbol = 310;
 	int SizeOfTypeIsNotKnown = 311;
 	int CanOnlySliceTupleTypes = 312;
 	int NoPropertyForTuple = 313;
 	int CannotResolveDotProperty = 314;
 	int CannotTakeAddressOfBitInArray = 315;
 	int OnlyOneIndexAllowedToIndex = 316;
 	int NoOpIndexOperatorOverloadForType = 317;
 	int ArrayDimensionOverflow = 318;
 	int OperatorAssignmentOverloadWithOpIndexIllegal = 319;
 	int CannotHaveOutOrInoutArgumentOfBitInArray = 320;
 	int SymbolIsAliasedToAFunction = 321;
 	int LinkageDoesNotMatchInterfaceFunction = 322;
 	int InterfaceFunctionIsNotImplemented = 323;
 	int ExpectedKeyAsArgumentToRemove = 324;
 	int CyclicConstructorCall = 325;
 	int MissingOrCurruptObjectDotD = 326;
 	int CannotContinueOutOfFinallyBlock = 327;
 	int ForwardDeclaration = 328;
 	int CannotFormDelegateDueToCovariantReturnType = 329;
 	int ForeachRangeKeyCannotHaveStorageClass = 330;
 	int MultipleOverridesOfSameFunction = 331;
 	int IdentityAssignmentOperatorOverloadIsIllegal = 332;
 	int LiteralsCannotBeClassMembers = 333;
 	int NoMatchForImplicitSuperCallInConstructor = 334;
 	int NoReturnAtEndOfFunction = 335;
 	int CanOnlyDeclareTypeAliasesWithinStaticIfConditionals = 336;
 	int PackageAndModuleHaveTheSameName = 337;
 	int StringLiteralsAreImmutable = 338;
 	int ExpressionDotNewIsOnlyForAllocatingNestedClasses = 339;
 	int TooManyArgumentsForArray = 340;
 	int ReturnExpressionExpected = 341;
 	int ReturnWithoutCallingConstructor = 342;
 	int ModuleIsInMultiplePackages = 343;
 	int ModuleIsInMultipleDefined = 344;
 	int NeedUpperAndLowerBoundToSlicePointer = 345;
 	int NeedUpperAndLowerBoundToSliceTuple = 346;
 	int CannotConvertStringLiteralToVoidPointer = 347;
 	int SymbolIsNotAPreInstanceInitializableField = 348;
 	int NoCaseStatementFollowingGoto = 349;
 	int SwitchStatementHasNoDefault = 350;
 	int SymbolIsNotAFunctionTemplate = 351;
 	int TupleIsNotAValidTemplateValueArgument = 352;
 	int IncompatibleArgumentsForTemplateInstantiation = 353;
 	int ThrowStatementsCannotBeInContracts = 354;
 	int CanOnlyThrowClassObjects = 355;
 	int StringExpectedAsSecondArgument = 356;
 	int WrongNumberOfArguments = 357;
 	int StringMustBeChars = 358;
 	int InvalidFirstArgument = 359;
 	int FirstArgumentIsNotAClass = 360;
 	int ArgumentHasNoMembers = 361;
 	int SymbolHasNoMembers = 362;
 	int KindSymbolHasNoMembers = 363;
 	int DotOffsetDeprecated = 364;
 	int NoClassInfoForComInterfaceObjects = 365;
 	int NoClassInfoForCppInterfaceObjects = 366;
 	int CannotMakeReferenceToABit = 367;
 	int CannotFormTupleOfTuples = 368;
 	int MissingInitializerInStaticConstructorForConstVariable = 369;
 	int GlobalsStaticsFieldsRefAndAutoParametersCannotBeAuto = 370;
 	int GlobalsStaticsFieldsManifestConstantsRefAndAutoParametersCannotBeScope = 371;
 	int ReferenceToScopeClassMustBeScope = 372;
 	int NumberOfKeysMustMatchNumberOfValues = 373;
 	int ExpectedNumberArguments = 374;
 	int ArraySliceIfOutOfBounds = 375;
 	int InvalidUCS32Char = 376;
 	int TupleIndexExceedsBounds = 377;
 	int SliceIsOutOfRange = 378;
 	int CannotTakeAddressOf = 379;
 	int VariableIsUsedBeforeInitialization = 380;
 	int EscapingReferenceToLocal = 381;
 	int EscapingReferenceToAutoLocal = 382;
 	int EscapingReferenceToScopeLocal = 383;
 	int EscapingReferenceToLocalVariable = 384;
 	int EscapingReferenceToVariadicParameter = 385;
 	int CanOnlyCatchClassObjects = 386;
 	int BaseClassIsForwardReferenced = 387;
 	int BaseIsForwardReferenced = 388;
 	int CannotInheritFromFinalClass = 389;
 	int StaticClassCannotInheritFromNestedClass = 390;
 	int SuperClassIsNestedWithin = 391;
 	int SuperClassIsNotNestedWithin = 392;
 	int ArrayComparisonTypeMismatch = 393;
 	int ConditionalExpressionIsNotAModifiableLvalue = 394;
 	int CannotCastSymbolToSymbol = 395;
 	int CannotDeleteInstanceOfComInterface = 396;
 	int TemplateIsNotAMemberOf = 397;
 	int TemplateIdentifierIsNotAMemberOf = 398;
 	int TemplateIdentifierIsNotAMemberOfUndefined = 399;
 	int CanOnlyInitiailizeConstMemberInsideConstructor = 400;
 	int SymbolIsNotAMember = 401;
 	int SymbolIsNotATemplate = 402;
 	int DSymbolHasNoSize = 403;
 	int ExpressionOfTypeDoesNotHaveABooleanValue = 404;
 	int ImplicitConversionCanCauseLossOfData = 405;
 	int ForwardReferenceToType = 406;
 	int FloatingPointConstantExpressionExpected = 407;
 	int ExpressionIsNotAValidTemplateValueArgument = 408;
 	int InvalidRangeLowerBound = 409;
 	int InvalidRangeUpperBound = 410;
 	int SymbolIsNotAScalarType = 411;
 	int ForeachIndexMustBeType = 412;
 	int ForeachValueMustBeType = 413;
 	int OpApplyFunctionMustReturnAnInt = 414;
 	int FunctionOfTypeOverridesButIsNotCovariant = 415;
 	int CannotOverrideFinalFunction = 416;
 	int IncompatibleCovariantTypes = 417;
 	int CannotUseTemplateToAddVirtualFunctionToClass = 418;
 	int OutResultIsAlreadyDefined = 419;
 	int MissingInitializerForConstField = 420;
 	int MissingInitializerForFinalField = 421;
 	int ImportNotFound = 422;
 	int SymbolMustBeAnArrayOfPointerType = 423;
 	int RvalueOfInExpressionMustBeAnAssociativeArray = 424;
 	int InterfaceInheritsFromDuplicateInterface = 425;
 	int LabelIsAlreadyDefined = 426;
 	int CannotSubtractPointerFromSymbol = 427;
 	int ThisForNestedClassMustBeAClassType = 428;
 	int CanOnlyDereferenceAPointer = 429;
 	int OuterClassThisNeededToNewNestedClass = 430;
 	int ThisForNestedClassMustBeOfType = 431;
 	int NoConstructorForSymbol = 432;
 	int NoAllocatorForSymbol = 433;
 	int NegativeArrayIndex = 434;
 	int NewCanOnlyCreateStructsDynamicArraysAndClassObjects = 435;
 	int MismatchedFunctionReturnTypeInference = 436;
 	int ShiftLeftExceeds = 437;
 	int SymbolCannotBeSlicedWithBrackets = 438;
 	int SliceExpressionIsNotAModifiableLvalue = 439;
 	int SymbolIsNotAMemberOf = 440;
 	int MoreInitiailizersThanFields = 441;
 	int OverlappingInitiailization = 442;
 	int CannotMakeExpressionOutOfInitializer = 443;
 	int NoDefaultOrCaseInSwitchStatement = 444;
 	int SymbolIsNotASymbol = 445;
 	int ForwardReferenceToTemplate = 446;
 	int ForwardReferenceToTemplateDeclaration = 447;
 	int SpecializationNotAllowedForDeducedParameter = 448;
 	int CannotDeclareTemplateAtFunctionScope = 449;
 	int TemplateHasNoValue = 450;
 	int CannotUseLocalAsTemplateParameter = 451;
 	int NoSizeForType = 452;
 	int SymbolDotSymbolIsNotADeclaration = 453;
 	int ThisIsRequiredButIsNotABaseClassOf = 454;
 	int ForwardReferenceToSymbol = 455;
 	int IdentifierOfSymbolIsNotDefined = 456;
 	int StructIsForwardReferenced = 457;
 	int CannotUseTemplateToAddFieldToAggregate = 458;
 	int CannotModifyFinalVariable = 459;
 	int InvalidUtf8Sequence2 = 460;
 	int Utf16HighValuePastEndOfString = 461;
 	int Utf16LowValueOutOfRange = 462;
 	int UnpairedUtf16Value = 463;
 	int IllegalUtf16Value = 464;
 	int StaticConstructorCanOnlyBePartOfStructClassModule = 465;
 	int ShiftAssignIsOutsideTheRange = 466;
 	int TemplateTupleParameterMustBeLastOne = 467;
 	int SymbolIsNestedInBoth = 468;
 	int FunctionIsAbstract = 469;
 	int KindSymbolDoesNotOverload = 470;
 	int MismatchedTupleLengths = 471;
 	int DoNotUseNullWhenComparingClassTypes = 472;
 	int UseTokenInsteadOfTokenWhenComparingWithNull = 473;
 	int VoidDoesNotHaveAnInitializer = 474;
 	int FunctionNameExpectedForStartAddress = 475;
 	int TypeofReturnMustBeInsideFunction = 476;
 	int PostBlitsAreOnlyForStructUnionDefinitions = 477;
 	int CannotHaveEDotTuple = 478;
 	int CannotCreateCppClasses = 479;
 	int SwitchAndCaseAreInDifferentFinallyBlocks = 480;
 	int SwitchAndDefaultAreInDifferentFinallyBlocks = 481;
 	int CannotHaveFieldWithSameStructType = 482;
 	int WithoutThisCannotBeConstInvariant = 483;
 	int CannotModifySymbol = 484;
 	int CannotCallPublicExportFunctionFromImmutable = 485;
 	int TemplateMemberFunctionNotAllowedInInterface = 486;
 	int ArgumentToTypeofIsNotAnExpression = 487;
 	int CannotInferTypeFromOverloadedFunctionSymbol = 488;
 	int SymbolIsNotMutable = 489;
 	int SymbolForSymbolNeedsToBeType = 490;
 	int CannotModifyConstImmutable = 491;
 	int SymbolCanOnlyBeCalledOnAnInvariantObject = 492;
 	int SymbolCanOnlyBeCalledOnAMutableObject = 493;
 	int CannotCallMutableMethodOnFinalStruct = 494;
 	int ForwardReferenceOfImport = 495;
 	int TemplateDoesNotHaveProperty = 496;
 	int ExpressionDoesNotHaveProperty = 497;
 	int RecursiveOpCmpExpansion = 498;
 	int CtorIsReservedForConstructors = 499;
 	int FunctionOverridesBaseClassFunctionButIsNotMarkedWithOverride = 500;
 	int ForwardReferenceOfTypeDotMangleof = 501;
 	int ArrayLengthHidesOtherLengthNameInOuterScope = 502;
 	int OnePathSkipsConstructor = 503;
 	int PragmaLibNotAllowedAsStatement = 504;
 	int ThereCanBeOnlyOneAliasThis = 505;
 	int AliasThisCanOnlyAppearInStructOrClassDeclaration = 506;
 	int FunctionIsOverloaded = 507;
 	int PureFunctionCannotCallImpure = 508;
 	int PureFunctionCannotAccessMutableStaticData = 509;
 	int PureNestedFunctionCannotAccessMutableData = 510;
 	int ConstraintIsNotConstantOrDoesNotEvaluateToABool = 511;
 	int EscapeStringLiteralDeprecated = 512;
 	int DelimiterCannotBeWhitespace = 513;
 	int ArrayEqualityComparisonTypeMismatch = 514;
 	int GsharedNotAllowedInSafeMode = 515;
 	int InnerStructCannotBeAField = 516;
 	int OnlyParametersOfForeachDeclarationsCanBeRef = 517;
 	int ManifestConstantsMustHaveInitializers = 518;
 	int ForwardReferenceToInferredReturnTypeOfFunctionCall = 519;
 	int CaseRangesNotAllowedInFinalSwitch = 520;
 	int MoreThan256CasesInCaseRange = 521;
 	int CannotCastTuple = 522;
 	int CastNotAllowedInSafeMode = 523;
 	int SliceIsNotMutable = 524;
 	int SymbolIsNothrowYetMayThrow = 525;
 	int DefaultConstructorNotAllowedForStructs = 526;
	int VoidDoesNotHaveADefaitInitializer = 527;
 	int CannotImplicitlyConvertToImmutable = 528;
 	int CannotHaveConstInvariantOutParameterOfType = 529;
 	int OverloadSetNotAllowedInStructDeclaration = 530;
 	int CannotPerformArrayOperationsOnVoidArrays = 531;
 
 }
