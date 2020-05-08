 package descent.internal.compiler.parser;
 
 import descent.core.compiler.CharOperation;
 import descent.core.compiler.IProblem;
 
 public class Problem implements IProblem {
 	
 	public final static Object[] NO_OBJECTS = new Object[0];
 	
 	private boolean isError;
 	private int categoryId;
 	private int id;
 	private int sourceStart;
 	private int sourceEnd;
 	private int sourceLineNumber;
 	private String[] arguments;
 	
 	private Problem() { }
 	
 	public static Problem newSyntaxError(int id, int line, int start, int length, String ... arguments) {
 		Problem p = new Problem();
 		p.isError = true;
 		p.id = id;
 		p.categoryId = CAT_SYNTAX;
 		p.sourceLineNumber = line;
 		p.sourceStart = start;
 		p.sourceEnd = start + length - 1;
 		p.arguments = arguments;
 		return p;
 	}
 	
 	public static Problem newSyntaxError(int id, int line, int start, int length) {
 		return newSyntaxError(id, line, start, length, (String[]) null);
 	}
 	
 	public static Problem newSemanticMemberError(int id, int line, int start, int length, String ... arguments) {
 		Problem p = new Problem();
 		p.isError = true;
 		p.id = id;
 		p.categoryId = CAT_MEMBER;
 		p.sourceLineNumber = line;
 		p.sourceStart = start;
 		p.sourceEnd = start + length - 1;
 		p.arguments = arguments;
 		return p;
 	}
 	
 	public static Problem newSemanticMemberError(int id, int line, int start, int length) {
 		return newSemanticMemberError(id, line, start, length, (String[]) null);
 	}
 	
 	private static Problem newSemanticTypeProblem(int id, int line, int start, int length, String[] arguments, boolean isError) {
 		Problem p = new Problem();
 		p.isError = isError;
 		p.id = id;
 		p.categoryId = CAT_TYPE;
 		p.sourceLineNumber = line;
 		p.sourceStart = start;
 		p.sourceEnd = start + length - 1;
 		p.arguments = arguments;
 		return p;
 	}
 	
 	public static Problem newSemanticTypeError(int id, int line, int start, int length, String ... arguments) {
 		return newSemanticTypeProblem(id, line, start, length, arguments, true);
 	}
 	
 	public static Problem newSemanticTypeError(int id, ASTDmdNode node, String ... arguments) {
 		return newSemanticTypeProblem(id, node.getLineNumber(), node.getStart(), node.getLength(), arguments, true);
 	}
 	
 	public static Problem newSemanticTypeErrorLoc(int id, ASTDmdNode node, String ... arguments) {
 		return newSemanticTypeProblem(id, node.getLineNumber(), node.getErrorStart(), node.getErrorLength(), arguments, true);
 	}
 	
 	public static Problem newSemanticTypeError(int id, ASTDmdNode n1, ASTDmdNode n2, String ... arguments) {
 		return newSemanticTypeProblem(id, n1.getLineNumber(), n1.getStart(), n2.getStart()+ n2.getLength() - n1.getStart(), arguments, true);
 	}
 	
 	public static Problem newSemanticTypeError(int id, ASTDmdNode n1, ASTDmdNode n2) {
 		return newSemanticTypeProblem(id, n1.getLineNumber(), n1.getStart(), n2.getStart() + n2.getLength() - n1.getStart(), null, true);
 	}
 	
 	public static Problem newSemanticTypeError(int id, ASTDmdNode node) {
 		return newSemanticTypeProblem(id, node.getLineNumber(), node.getStart(), node.getLength(), null, true);
 	}
 	
 	public static Problem newSemanticTypeErrorLoc(int id, ASTDmdNode node) {
 		return newSemanticTypeProblem(id, node.getLineNumber(), node.getErrorStart(), node.getErrorLength(), null, true);
 	}
 	
 	public static Problem newSemanticTypeError(int id, int line, int start, int length) {
 		return newSemanticTypeError(id, line, start, length, (String[]) null);
 	}
 	
 	public static Problem newSemanticTypeWarning(int id, int line, int start, int length, String ... arguments) {
 		return newSemanticTypeProblem(id, line, start, length, arguments, false);
 	}
 	
 	public static Problem newSemanticTypeWarning(int id, int line, int start, int length) {
 		return newSemanticTypeWarning(id, line, start, length, (String[]) null);
 	}
 	
 	public static Problem newSemanticTypeWarning(int id, ASTDmdNode node) {
 		return newSemanticTypeWarning(id, node.getLineNumber(), node.getStart(), node.getLength(), (String[]) null);
 	}
 	
 	public static Problem newSemanticTypeWarningLoc(int id, ASTDmdNode node) {
 		return newSemanticTypeWarning(id, node.getLineNumber(), node.getErrorStart(), node.getErrorLength(), (String[]) null);
 	}
 	
 	public static Problem newSemanticTypeWarning(int id, ASTDmdNode node, String ... arguments) {
 		return newSemanticTypeWarning(id, node.getLineNumber(), node.getStart(), node.getLength(), arguments);
 	}
 	
 	public static Problem newTask(String message, int line, int start, int length) {
 		Problem p = new Problem();
 		p.arguments = new String[] { message };
 		p.isError = false;
 		p.id = IProblem.Task;
 		p.categoryId = CAT_UNSPECIFIED;
 		p.sourceLineNumber = line;
 		p.sourceStart = start;
 		p.sourceEnd = start + length - 1;
 		return p;
 	}
 	
 	public int getID() {
 		return id;
 	}
 
 	public String getMessage() {
 		switch(id) {
 		case UnterminatedBlockComment:
 			return String.format(ProblemMessages.UnterminatedBlockComment);
 		case UnterminatedPlusBlockComment:
 			return String.format(ProblemMessages.UnterminatedPlusBlockComment);
 		case IncorrectNumberOfHexDigitsInEscapeSequence:
 			return String.format(ProblemMessages.IncorrectNumberOfHexDigitsInEscapeSequence, arguments[0], arguments[1]);
 		case UndefinedEscapeHexSequence:
 			return String.format(ProblemMessages.UndefinedEscapeHexSequence);
 		case UnterminatedStringConstant:
 			return String.format(ProblemMessages.UnterminatedStringConstant);
 		case OddNumberOfCharactersInHexString:
 			return String.format(ProblemMessages.OddNumberOfCharactersInHexString, arguments[0]);
 		case NonHexCharacter:
 			return String.format(ProblemMessages.NonHexCharacter, arguments[0]);
 		case UnterminatedCharacterConstant:
 			return String.format(ProblemMessages.UnterminatedCharacterConstant);
 		case BinaryDigitExpected:
 			return String.format(ProblemMessages.BinaryDigitExpected);
 		case OctalDigitExpected:
 			return String.format(ProblemMessages.OctalDigitExpected);
 		case HexDigitExpected:
 			return String.format(ProblemMessages.HexDigitExpected);
 		case UnsupportedCharacter:
 			return String.format(ProblemMessages.UnsupportedCharacter, arguments[0]);
 		case InvalidUtfCharacter:
 			return String.format(ProblemMessages.InvalidUtfCharacter, arguments[0]);
 		case ThreeEqualsIsNoLongerLegal:
 			return String.format(ProblemMessages.ThreeEqualsIsNoLongerLegal);
 		case NotTwoEqualsIsNoLongerLegal:
 			return String.format(ProblemMessages.NotTwoEqualsIsNoLongerLegal);
 		case LSuffixDeprecated:
 			return String.format(ProblemMessages.LSuffixDeprecated);
 		case InvalidPragmaSyntax:
 			return String.format(ProblemMessages.InvalidPragmaSyntax);
 		case UnrecognizedCharacterEntity:
 			return String.format(ProblemMessages.UnrecognizedCharacterEntity);
 		case UnterminatedNamedEntity:
 			return String.format(ProblemMessages.UnterminatedNamedEntity);
 		case UndefinedEscapeSequence:
 			return String.format(ProblemMessages.UndefinedEscapeSequence);
 		case InvalidUtf8Sequence:
 			return String.format(ProblemMessages.InvalidUtf8Sequence);
 		case IntegerOverflow:
 			return String.format(ProblemMessages.IntegerOverflow);
 		case SignedIntegerOverflow:
 			return String.format(ProblemMessages.SignedIntegerOverflow);
 		case UnrecognizedToken:
 			return String.format(ProblemMessages.UnrecognizedToken);
 		case BinaryExponentPartRequired:
 			return String.format(ProblemMessages.BinaryExponentPartRequired);
 		case ExponentExpected:
 			return String.format(ProblemMessages.ExponentExpected);
 		case ISuffixDeprecated:
 			return String.format(ProblemMessages.ISuffixDeprecated);
 		case ParsingErrorInsertTokenAfter:
 			return String.format(ProblemMessages.ParsingErrorInsertTokenAfter, arguments[0], arguments[1]);
 		case ParsingErrorDeleteToken:
 			return String.format(ProblemMessages.ParsingErrorDeleteToken, arguments[0]);
 		case ParsingErrorInsertToComplete:
 			return String.format(ProblemMessages.ParsingErrorInsertToComplete, arguments[0], arguments[1]);
 		case EnumDeclarationIsInvalid:
 			return String.format(ProblemMessages.EnumDeclarationIsInvalid);
 		case MismatchedStringLiteralPostfixes:
 			return String.format(ProblemMessages.MismatchedStringLiteralPostfixes, arguments[0], arguments[1]);
 		case NoIdentifierForDeclarator:
 			return String.format(ProblemMessages.NoIdentifierForDeclarator);
 		case AliasCannotHaveInitializer:
 			return String.format(ProblemMessages.AliasCannotHaveInitializer);
 		case CStyleCastIllegal:
 			return String.format(ProblemMessages.CStyleCastIllegal);
 		case InvalidLinkageIdentifier:
 			return String.format(ProblemMessages.InvalidLinkageIdentifier);
 		case VariadicArgumentCannotBeOutOrRef:
 			return String.format(ProblemMessages.VariadicArgumentCannotBeOutOrRef);
 		case VariadicNotAllowedInDelete:
 			return String.format(ProblemMessages.VariadicNotAllowedInDelete);
 		case NoIdentifierForTemplateValueParameter:
 			return String.format(ProblemMessages.NoIdentifierForTemplateValueParameter);
 		case UnexpectedIdentifierInDeclarator:
 			return String.format(ProblemMessages.UnexpectedIdentifierInDeclarator);
 		case RedundantStorageClass:
 			return String.format(ProblemMessages.RedundantStorageClass);
 		case RedundantProtectionAttribute:
 			return String.format(ProblemMessages.RedundantProtectionAttribute);
 		case UseBracesForAnEmptyStatement:
 			return String.format(ProblemMessages.UseBracesForAnEmptyStatement);
 		case MultipleDeclarationsMustHaveTheSameType:
 			return String.format(ProblemMessages.MultipleDeclarationsMustHaveTheSameType);
 		case RedundantInStatement:
 			return String.format(ProblemMessages.RedundantInStatement);
 		case RedundantOutStatement:
 			return String.format(ProblemMessages.RedundantOutStatement);
 		case StatementExpectedToBeCurlies:
 			return String.format(ProblemMessages.StatementExpectedToBeCurlies);
 		case InvalidScopeIdentifier:
 			return String.format(ProblemMessages.InvalidScopeIdentifier);
 		case OnScopeDeprecated:
 			return String.format(ProblemMessages.OnScopeDeprecated, arguments[0]);
 		case DollarInvalidOutsideBrackets:
 			return String.format(ProblemMessages.DollarInvalidOutsideBrackets);
 		case IftypeDeprecated:
 			return String.format(ProblemMessages.IftypeDeprecated);
 		case IfAutoDeprecated:
 			return String.format(ProblemMessages.IfAutoDeprecated);
 		case VariadicTemplateParameterMustBeTheLastOne:
 			return String.format(ProblemMessages.VariadicTemplateParameterMustBeTheLastOne);
 		case NeedSizeOfRightmostArray:
 			return String.format(ProblemMessages.NeedSizeOfRightmostArray);
 		case ConflictingStorageClass:
 			return String.format(ProblemMessages.ConflictingStorageClass);
 		case ValueIsLargerThanAByte:
 			return String.format(ProblemMessages.ValueIsLargerThanAByte, arguments[0]);
 		case UnterminatedTokenStringConstant:
 			return String.format(ProblemMessages.UnterminatedTokenStringConstant);
 		case HeredocRestOfLineShouldBeBlank:
 			return String.format(ProblemMessages.HeredocRestOfLineShouldBeBlank);
 		case IdentifierExpectedForHeredoc:
 			return String.format(ProblemMessages.IdentifierExpectedForHeredoc);
 		case DelimitedStringMustEndInValue:
 			return String.format(ProblemMessages.DelimitedStringMustEndInValue, arguments[0]);
 		case TypeOnlyAllowedIfAnonymousEnumAndNoEnumType:
 			return String.format(ProblemMessages.TypeOnlyAllowedIfAnonymousEnumAndNoEnumType);
 		case IfTypeThereMustBeAnInitializer:
 			return String.format(ProblemMessages.IfTypeThereMustBeAnInitializer);
 		case InvariantAsAttributeIsOnlySupportedInD2:
 			return String.format(ProblemMessages.InvariantAsAttributeIsOnlySupportedInD2);
 		case ConstAsAttributeIsOnlySupportedInD2:
 			return String.format(ProblemMessages.ConstAsAttributeIsOnlySupportedInD2);
 		case OnlyOneCaseAllowedForStartOfCaseRange:
 			return String.format(ProblemMessages.OnlyOneCaseAllowedForStartOfCaseRange);
 		case SymbolConflictsWithSymbolAtLocation:
 			return String.format(ProblemMessages.SymbolConflictsWithSymbolAtLocation, arguments[0], arguments[1], arguments[2], arguments[3]);
 		case SymbolAtLocationConflictsWithSymbolAtLocation:
 			return String.format(ProblemMessages.SymbolAtLocationConflictsWithSymbolAtLocation, arguments[0], arguments[1], arguments[2], arguments[3]);
 		case PropertyCanNotBeRedefined:
 			return String.format(ProblemMessages.PropertyCanNotBeRedefined, arguments[0]);
 		case CircularDefinition:
 			return String.format(ProblemMessages.CircularDefinition, arguments[0]);
 		case EnumValueOverflow:
 			return String.format(ProblemMessages.EnumValueOverflow);
 		case EnumMustHaveAtLeastOneMember:
 			return String.format(ProblemMessages.EnumMustHaveAtLeastOneMember);
 		case EnumBaseTypeMustBeOfIntegralType:
 			return String.format(ProblemMessages.EnumBaseTypeMustBeOfIntegralType);
 		case ForwardReferenceOfSymbol:
 			return String.format(ProblemMessages.ForwardReferenceOfSymbol, arguments[0]);
 		case ForwardReferenceOfEnumSymbolDotSymbol:
 			return String.format(ProblemMessages.ForwardReferenceOfEnumSymbolDotSymbol, arguments[0], arguments[1]);
 		case ForwardReferenceWhenLookingFor:
 			return String.format(ProblemMessages.ForwardReferenceWhenLookingFor, arguments[0], arguments[1]);
 		case BaseEnumIsForwardReference:
 			return String.format(ProblemMessages.BaseEnumIsForwardReference);
 		case CannotResolveForwardReference:
 			return String.format(ProblemMessages.CannotResolveForwardReference);
 		case EnumIsForwardReference:
 			return String.format(ProblemMessages.EnumIsForwardReference);
 		case IntegerConstantExpressionExpected:
 			return String.format(ProblemMessages.IntegerConstantExpressionExpected);
 		case ThisNotInClassOrStruct:
 			return String.format(ProblemMessages.ThisNotInClassOrStruct);
 		case ThisOnlyAllowedInNonStaticMemberFunctions:
 			return String.format(ProblemMessages.ThisOnlyAllowedInNonStaticMemberFunctions);
 		case SuperOnlyAllowedInNonStaticMemberFunctions:
 			return String.format(ProblemMessages.SuperOnlyAllowedInNonStaticMemberFunctions);
 		case SuperNotInClass:
 			return String.format(ProblemMessages.SuperNotInClass);
 		case ClassHasNoSuper:
 			return String.format(ProblemMessages.ClassHasNoSuper, arguments[0]);
 		case BaseTypeMustBeInterface:
 			return String.format(ProblemMessages.BaseTypeMustBeInterface);
 		case MemberIsPrivate:
 			return String.format(ProblemMessages.MemberIsPrivate, arguments[0]);
 		case UsedAsAType:
 			return String.format(ProblemMessages.UsedAsAType, arguments[0]);
 		case ExternSymbolsCannotHaveInitializers:
 			return String.format(ProblemMessages.ExternSymbolsCannotHaveInitializers);
 		case VoidsHaveNoValue:
 			return String.format(ProblemMessages.VoidsHaveNoValue);
 		case CannotInferTypeFromThisArrayInitializer:
 			return String.format(ProblemMessages.CannotInferTypeFromThisArrayInitializer);
 		case NoDefinition:
 			return String.format(ProblemMessages.NoDefinition, arguments[0]);
 		case DuplicatedInterfaceInheritance:
 			return String.format(ProblemMessages.DuplicatedInterfaceInheritance, arguments[0], arguments[1]);
 		case BaseTypeMustBeClassOrInterface:
 			return String.format(ProblemMessages.BaseTypeMustBeClassOrInterface);
 		case FieldsNotAllowedInInterfaces:
 			return String.format(ProblemMessages.FieldsNotAllowedInInterfaces);
 		case UndefinedIdentifier:
 			return String.format(ProblemMessages.UndefinedIdentifier, arguments[0]);
 		case NotAMember:
 			return String.format(ProblemMessages.NotAMember, arguments[0]);
 		case NewAllocatorsOnlyForClassOrStruct:
 			return String.format(ProblemMessages.NewAllocatorsOnlyForClassOrStruct);
 		case DeleteDeallocatorsOnlyForClassOrStruct:
 			return String.format(ProblemMessages.DeleteDeallocatorsOnlyForClassOrStruct);
 		case ConstructorsOnlyForClass:
 			return String.format(ProblemMessages.ConstructorsOnlyForClass);
 		case ConstructorsOnlyForClassOrStruct:
 			return String.format(ProblemMessages.ConstructorsOnlyForClassOrStruct);
 		case DestructorsOnlyForClass:
 			return String.format(ProblemMessages.DestructorsOnlyForClass);
 		case InvariantsOnlyForClassStructUnion:
 			return String.format(ProblemMessages.InvariantsOnlyForClassStructUnion);
 		case FunctionDoesNotOverrideAny:
 			return String.format(ProblemMessages.FunctionDoesNotOverrideAny, arguments[0], arguments[1]);
 		case CannotOverrideFinalFunctions:
 			return String.format(ProblemMessages.CannotOverrideFinalFunctions, arguments[0], arguments[1]);
 		case OverrideOnlyForClassMemberFunctions:
 			return String.format(ProblemMessages.OverrideOnlyForClassMemberFunctions);
 		case FunctionMustReturnAResultOfType:
 			return String.format(ProblemMessages.FunctionMustReturnAResultOfType, arguments[0]);
 		case MoreThanOneInvariant:
 			return String.format(ProblemMessages.MoreThanOneInvariant, arguments[0]);
 		case ParameterMultiplyDefined:
 			return String.format(ProblemMessages.ParameterMultiplyDefined, arguments[0]);
 		case SymbolNotFound:
 			return String.format(ProblemMessages.SymbolNotFound, arguments[0]);
 		case StatementIsNotReachable:
 			return String.format(ProblemMessages.StatementIsNotReachable);
 		case VoidFunctionsHaveNoResult:
 			return String.format(ProblemMessages.VoidFunctionsHaveNoResult);
 		case ReturnStatementsCannotBeInContracts:
 			return String.format(ProblemMessages.ReturnStatementsCannotBeInContracts);
 		case NotAnAggregateType:
 			return String.format(ProblemMessages.NotAnAggregateType, arguments[0]);
 		case UnrecognizedPragma:
 			return String.format(ProblemMessages.UnrecognizedPragma);
 		case AnonCanOnlyBePartOfAnAggregate:
 			return String.format(ProblemMessages.AnonCanOnlyBePartOfAnAggregate);
 		case PragmaIsMissingClosingSemicolon:
 			return String.format(ProblemMessages.PragmaIsMissingClosingSemicolon);
 		case CannotImplicitlyConvert:
 			return String.format(ProblemMessages.CannotImplicitlyConvert, arguments[0], arguments[1], arguments[2]);
 		case ForbiddenReference:
 			return String.format(ProblemMessages.ForbiddenReference);
 		case DiscouragedReference:
 			return String.format(ProblemMessages.DiscouragedReference);
 		case Task:
 			return String.format(ProblemMessages.Task, arguments[0]);
 		case UndefinedType:
 			return String.format(ProblemMessages.UndefinedType);
 		case IsClassPathCorrect:
 			return String.format(ProblemMessages.IsClassPathCorrect);
 		case FunctionsCannotBeConstOrAuto:
 			return String.format(ProblemMessages.FunctionsCannotBeConstOrAuto);
 		case FunctionsCannotBeScopeOrAuto:
 			return String.format(ProblemMessages.FunctionsCannotBeScopeOrAuto);
 		case NonVirtualFunctionsCannotBeAbstract:
 			return String.format(ProblemMessages.NonVirtualFunctionsCannotBeAbstract);
 		case CannotBeBothAbstractAndFinal:
 			return String.format(ProblemMessages.CannotBeBothAbstractAndFinal);
 		case ModifierCannotBeAppliedToVariables:
 			return String.format(ProblemMessages.ModifierCannotBeAppliedToVariables, arguments[0]);
 		case StructsCannotBeAbstract:
 			return String.format(ProblemMessages.StructsCannotBeAbstract);
 		case UnionsCannotBeAbstract:
 			return String.format(ProblemMessages.UnionsCannotBeAbstract);
 		case AliasCannotBeConst:
 			return String.format(ProblemMessages.AliasCannotBeConst);
 		case OneArgumentOfTypeExpected:
 			return String.format(ProblemMessages.OneArgumentOfTypeExpected, arguments[0]);
 		case IllegalMainParameters:
 			return String.format(ProblemMessages.IllegalMainParameters);
 		case MustReturnIntOrVoidFromMainFunction:
 			return String.format(ProblemMessages.MustReturnIntOrVoidFromMainFunction);
 		case AtLeastOneArgumentOfTypeExpected:
 			return String.format(ProblemMessages.AtLeastOneArgumentOfTypeExpected, arguments[0]);
 		case FirstArgumentMustBeOfType:
 			return String.format(ProblemMessages.FirstArgumentMustBeOfType, arguments[0]);
 		case StringExpectedForPragmaMsg:
 			return String.format(ProblemMessages.StringExpectedForPragmaMsg);
 		case LibPragmaMustRecieveASingleArgumentOfTypeString:
 			return String.format(ProblemMessages.LibPragmaMustRecieveASingleArgumentOfTypeString);
 		case StringExpectedForPragmaLib:
 			return String.format(ProblemMessages.StringExpectedForPragmaLib);
 		case CannotHaveOutOrInoutParameterOfTypeStaticArray:
 			return String.format(ProblemMessages.CannotHaveOutOrInoutParameterOfTypeStaticArray);
 		case CannotHaveParameterOfTypeVoid:
 			return String.format(ProblemMessages.CannotHaveParameterOfTypeVoid);
 		case FunctionsCannotReturnStaticArrays:
 			return String.format(ProblemMessages.FunctionsCannotReturnStaticArrays);
 		case UnrecongnizedTrait:
 			return String.format(ProblemMessages.UnrecongnizedTrait, arguments[0]);
 		case CanOnlyConcatenateArrays:
 			return String.format(ProblemMessages.CanOnlyConcatenateArrays, arguments[0], arguments[1]);
 		case ArrayIndexOutOfBounds:
 			return String.format(ProblemMessages.ArrayIndexOutOfBounds, arguments[0], arguments[1]);
 		case ArrayIndexOutOfBounds2:
 			return String.format(ProblemMessages.ArrayIndexOutOfBounds2, arguments[0], arguments[1], arguments[2]);
 		case AssertionFailed:
 			return String.format(ProblemMessages.AssertionFailed, arguments[0]);
 		case AssertionFailedNoMessage:
 			return String.format(ProblemMessages.AssertionFailedNoMessage, arguments[0]);
 		case ExpressionIsNotEvaluatableAtCompileTime:
 			return String.format(ProblemMessages.ExpressionIsNotEvaluatableAtCompileTime, arguments[0]);
 		case UndefinedProperty:
 			return String.format(ProblemMessages.UndefinedProperty, arguments[0], arguments[1]);
 		case DeprecatedProperty:
 			return String.format(ProblemMessages.DeprecatedProperty, arguments[0], arguments[1]);
 		case FileNameMustBeString:
 			return String.format(ProblemMessages.FileNameMustBeString, arguments[0]);
 		case FileImportsMustBeSpecified:
 			return String.format(ProblemMessages.FileImportsMustBeSpecified, arguments[0]);
 		case FileNotFound:
 			return String.format(ProblemMessages.FileNotFound, arguments[0]);
 		case ErrorReadingFile:
 			return String.format(ProblemMessages.ErrorReadingFile, arguments[0]);
 		case ExpressionHasNoEffect:
 			return String.format(ProblemMessages.ExpressionHasNoEffect);
 		case ConstantIsNotAnLValue:
 			return String.format(ProblemMessages.ConstantIsNotAnLValue, arguments[0]);
 		case VersionIdentifierReserved:
 			return String.format(ProblemMessages.VersionIdentifierReserved, arguments[0]);
 		case CannotPutCatchStatementInsideFinallyBlock:
 			return String.format(ProblemMessages.CannotPutCatchStatementInsideFinallyBlock);
 		case ExpressionDoesNotGiveABooleanResult:
 			return String.format(ProblemMessages.ExpressionDoesNotGiveABooleanResult);
 		case BreakIsNotInsideALoopOrSwitch:
 			return String.format(ProblemMessages.BreakIsNotInsideALoopOrSwitch);
 		case CaseIsNotInSwitch:
 			return String.format(ProblemMessages.CaseIsNotInSwitch);
 		case VersionDeclarationMustBeAtModuleLevel:
 			return String.format(ProblemMessages.VersionDeclarationMustBeAtModuleLevel);
 		case DebugDeclarationMustBeAtModuleLevel:
 			return String.format(ProblemMessages.DebugDeclarationMustBeAtModuleLevel);
 		case GotoCaseNotInSwitch:
 			return String.format(ProblemMessages.GotoCaseNotInSwitch);
 		case GotoDefaultNotInSwitch:
 			return String.format(ProblemMessages.GotoDefaultNotInSwitch);
 		case LazyVariablesCannotBeLvalues:
 			return String.format(ProblemMessages.LazyVariablesCannotBeLvalues);
 		case DivisionByZero:
 			return String.format(ProblemMessages.DivisionByZero);
 		case DefaultNotInSwitch:
 			return String.format(ProblemMessages.DefaultNotInSwitch);
 		case SwitchAlreadyHasDefault:
 			return String.format(ProblemMessages.SwitchAlreadyHasDefault);
 		case ContinueNotInLoop:
 			return String.format(ProblemMessages.ContinueNotInLoop);
 		case ForeachIndexCannotBeRef:
 			return String.format(ProblemMessages.ForeachIndexCannotBeRef);
 		case ParametersDoesNotMatchParameterTypes:
 			return String.format(ProblemMessages.ParametersDoesNotMatchParameterTypes, arguments[0], arguments[1]);
 		case IncompatibleParameterStorageClass:
 			return String.format(ProblemMessages.IncompatibleParameterStorageClass);
 		case OutCannotBeConst:
 			return String.format(ProblemMessages.OutCannotBeConst);
 		case OutCannotBeInvariant:
 			return String.format(ProblemMessages.OutCannotBeInvariant);
 		case ScopeCannotBeRefOrOut:
 			return String.format(ProblemMessages.ScopeCannotBeRefOrOut);
 		case IncompatibleTypesForOperator:
 			return String.format(ProblemMessages.IncompatibleTypesForOperator, arguments[0], arguments[1], arguments[2]);
 		case IncompatibleTypesForMinus:
 			return String.format(ProblemMessages.IncompatibleTypesForMinus);
 		case SymbolNotDefined:
 			return String.format(ProblemMessages.SymbolNotDefined, arguments[0]);
 		case SymbolNotATemplate:
 			return String.format(ProblemMessages.SymbolNotATemplate, arguments[0]);
 		case CannotDeleteType:
 			return String.format(ProblemMessages.CannotDeleteType, arguments[0]);
 		case NotAnLvalue:
 			return String.format(ProblemMessages.NotAnLvalue, arguments[0]);
 		case CannotAliasAnExpression:
 			return String.format(ProblemMessages.CannotAliasAnExpression, arguments[0]);
 		case CannotAssignToStaticArray:
 			return String.format(ProblemMessages.CannotAssignToStaticArray, arguments[0]);
 		case CannotChangeReferenceToStaticArray:
 			return String.format(ProblemMessages.CannotChangeReferenceToStaticArray, arguments[0]);
 		case CannotModifyParameterInContract:
 			return String.format(ProblemMessages.CannotModifyParameterInContract, arguments[0]);
 		case BothOverloadsMuchArgumentList:
 			return String.format(ProblemMessages.BothOverloadsMuchArgumentList, arguments[0], arguments[1], arguments[2]);
 		case ExpressionHasNoType:
 			return String.format(ProblemMessages.ExpressionHasNoType, arguments[0]);
 		case SymbolNotAnExpression:
 			return String.format(ProblemMessages.SymbolNotAnExpression, arguments[0]);
 		case SymbolHasNoValue:
 			return String.format(ProblemMessages.SymbolHasNoValue, arguments[0]);
 		case TooManyInitializers:
 			return String.format(ProblemMessages.TooManyInitializers, arguments[0]);
 		case SymbolNotAStaticAndCannotHaveStaticInitializer:
 			return String.format(ProblemMessages.SymbolNotAStaticAndCannotHaveStaticInitializer, arguments[0]);
 		case SymbolNotAType:
 			return String.format(ProblemMessages.SymbolNotAType, arguments[0]);
 		case IncompleteMixinDeclaration:
 			return String.format(ProblemMessages.IncompleteMixinDeclaration, arguments[0]);
 		case SymbolNotATemplateItIs:
 			return String.format(ProblemMessages.SymbolNotATemplateItIs, arguments[0], arguments[1]);
 		case SymbolCannotBeDeclaredToBeAFunction:
 			return String.format(ProblemMessages.SymbolCannotBeDeclaredToBeAFunction, arguments[0]);
 		case CannotHaveArrayOfType:
 			return String.format(ProblemMessages.CannotHaveArrayOfType, arguments[0]);
 		case SymbolDoesNotMatchAnyTemplateDeclaration:
 			return String.format(ProblemMessages.SymbolDoesNotMatchAnyTemplateDeclaration, arguments[0]);
 		case SymbolDoesNotMatchTemplateDeclaration:
 			return String.format(ProblemMessages.SymbolDoesNotMatchTemplateDeclaration, arguments[0], arguments[1]);
 		case SymbolDoesNotMatchAnyFunctionTemplateDeclaration:
 			return String.format(ProblemMessages.SymbolDoesNotMatchAnyFunctionTemplateDeclaration, arguments[0]);
 		case IndexOverflowForStaticArray:
 			return String.format(ProblemMessages.IndexOverflowForStaticArray, arguments[0]);
 		case UnknownSize:
 			return String.format(ProblemMessages.UnknownSize);
 		case NoSizeYetForForwardReference:
 			return String.format(ProblemMessages.NoSizeYetForForwardReference);
 		case SymbolMatchesMoreThanOneTemplateDeclaration:
 			return String.format(ProblemMessages.SymbolMatchesMoreThanOneTemplateDeclaration, arguments[0], arguments[1], arguments[2]);
 		case ExpressionLeadsToStackOverflowAtCompileTime:
 			return String.format(ProblemMessages.ExpressionLeadsToStackOverflowAtCompileTime, arguments[0]);
 		case StringIndexOutOfBounds:
 			return String.format(ProblemMessages.StringIndexOutOfBounds, arguments[0], arguments[1]);
 		case CannotCreateInstanceOfAbstractClass:
 			return String.format(ProblemMessages.CannotCreateInstanceOfAbstractClass, arguments[0]);
 		case CannotCreateInstanceOfInterface:
 			return String.format(ProblemMessages.CannotCreateInstanceOfInterface, arguments[0]);
 		case WithExpressionsMustBeClassObject:
 			return String.format(ProblemMessages.WithExpressionsMustBeClassObject, arguments[0]);
 		case DeclarationIsAlreadyDefined:
 			return String.format(ProblemMessages.DeclarationIsAlreadyDefined, arguments[0]);
 		case DeclarationIsAlreadyDefinedInAnotherScope:
 			return String.format(ProblemMessages.DeclarationIsAlreadyDefinedInAnotherScope, arguments[0], arguments[1]);
 		case VersionDefinedAfterUse:
 			return String.format(ProblemMessages.VersionDefinedAfterUse, arguments[0]);
 		case DebugDefinedAfterUse:
 			return String.format(ProblemMessages.DebugDefinedAfterUse, arguments[0]);
 		case NotEnoughArguments:
 			return String.format(ProblemMessages.NotEnoughArguments);
 		case CanOnlySynchronizeOnClassObjects:
 			return String.format(ProblemMessages.CanOnlySynchronizeOnClassObjects, arguments[0]);
 		case CannotDeduceTemplateFunctionFromArgumentTypes:
 			return String.format(ProblemMessages.CannotDeduceTemplateFunctionFromArgumentTypes, arguments[0]);
 		case CannotDeduceTemplateFunctionFromArgumentTypes2:
 			return String.format(ProblemMessages.CannotDeduceTemplateFunctionFromArgumentTypes2, arguments[0], arguments[1]);
 		case ArrayDimensionExceedsMax:
 			return String.format(ProblemMessages.ArrayDimensionExceedsMax, arguments[0], arguments[1]);
 		case AStructIsNotAValidInitializerFor:
 			return String.format(ProblemMessages.AStructIsNotAValidInitializerFor, arguments[0]);
 		case CannotUseArrayToInitialize:
 			return String.format(ProblemMessages.CannotUseArrayToInitialize, arguments[0]);
 		case CircularReferenceTo:
 			return String.format(ProblemMessages.CircularReferenceTo, arguments[0]);
 		case ParameterIsAlreadyDefined:
 			return String.format(ProblemMessages.ParameterIsAlreadyDefined, arguments[0], arguments[1]);
 		case MemberIsNotAccessible:
 			return String.format(ProblemMessages.MemberIsNotAccessible, arguments[0]);
 		case SymbolIsNotAccessible:
 			return String.format(ProblemMessages.SymbolIsNotAccessible, arguments[0], arguments[1], arguments[2], arguments[3]);
 		case ThisForSymbolNeedsToBeType:
 			return String.format(ProblemMessages.ThisForSymbolNeedsToBeType, arguments[0], arguments[1], arguments[2]);
 		case SymbolHasForwardReferences:
 			return String.format(ProblemMessages.SymbolHasForwardReferences, arguments[0]);
 		case CannotHaveAssociativeArrayOfKey:
 			return String.format(ProblemMessages.CannotHaveAssociativeArrayOfKey, arguments[0]);
 		case CannotHaveAssociativeArrayOf:
 			return String.format(ProblemMessages.CannotHaveAssociativeArrayOf, arguments[0]);
 		case CannotHaveArrayOfAuto:
 			return String.format(ProblemMessages.CannotHaveArrayOfAuto, arguments[0]);
 		case EnclosingLabelForBreakNotFound:
 			return String.format(ProblemMessages.EnclosingLabelForBreakNotFound, arguments[0]);
 		case EnclosingLabelForContinueNotFound:
 			return String.format(ProblemMessages.EnclosingLabelForContinueNotFound, arguments[0]);
 		case CannotAppendTypeToType:
 			return String.format(ProblemMessages.CannotAppendTypeToType, arguments[0], arguments[1]);
 		case CannotAppendToStaticArray:
 			return String.format(ProblemMessages.CannotAppendToStaticArray, arguments[0]);
 		case ExpressionIsVoidAndHasNoValue:
 			return String.format(ProblemMessages.ExpressionIsVoidAndHasNoValue, arguments[0]);
 		case NeedMemberFunctionOpCmpForSymbolToCompare:
 			return String.format(ProblemMessages.NeedMemberFunctionOpCmpForSymbolToCompare, arguments[0], arguments[1]);
 		case CompareNotDefinedForComplexOperands:
 			return String.format(ProblemMessages.CompareNotDefinedForComplexOperands);
 		case NeedThisForAddressOfSymbol:
 			return String.format(ProblemMessages.NeedThisForAddressOfSymbol, arguments[0]);
 		case RecursiveMixinInstantiation:
 			return String.format(ProblemMessages.RecursiveMixinInstantiation);
 		case SymbolIsNotOfIntegralType:
 			return String.format(ProblemMessages.SymbolIsNotOfIntegralType, arguments[0], arguments[1]);
 		case DeleteAAKeyDeprecated:
 			return String.format(ProblemMessages.DeleteAAKeyDeprecated);
 		case SymbolIsDeprecated:
 			return String.format(ProblemMessages.SymbolIsDeprecated, arguments[0]);
 		case ShadowingDeclarationIsDeprecated:
 			return String.format(ProblemMessages.ShadowingDeclarationIsDeprecated, arguments[0]);
 		case ReturnStatementsCannotBeInFinallyScopeExitOrScopeSuccessBodies:
 			return String.format(ProblemMessages.ReturnStatementsCannotBeInFinallyScopeExitOrScopeSuccessBodies);
 		case CannotReturnExpressionFromConstructor:
 			return String.format(ProblemMessages.CannotReturnExpressionFromConstructor);
 		case CaseNotFound:
 			return String.format(ProblemMessages.CaseNotFound, arguments[0]);
 		case CircularInheritanceOfInterface:
 			return String.format(ProblemMessages.CircularInheritanceOfInterface);
 		case ArgumentToMixinMustBeString:
 			return String.format(ProblemMessages.ArgumentToMixinMustBeString, arguments[0]);
 		case CannotAccessFrameOfFunction:
 			return String.format(ProblemMessages.CannotAccessFrameOfFunction, arguments[0]);
 		case OperationNotAllowedOnBool:
 			return String.format(ProblemMessages.OperationNotAllowedOnBool, arguments[0]);
 		case SymbolIsNotAScalar:
 			return String.format(ProblemMessages.SymbolIsNotAScalar, arguments[0], arguments[1]);
 		case ImportCannotBeResolved:
 			return String.format(ProblemMessages.ImportCannotBeResolved, arguments[0]);
 		case SymbolIsNotAVariable:
 			return String.format(ProblemMessages.SymbolIsNotAVariable, arguments[0], arguments[1]);
 		case CatchHidesCatch:
 			return String.format(ProblemMessages.CatchHidesCatch, arguments[0], arguments[1]);
 		case ArithmeticOrStringTypeExpectedForValueParameter:
 			return String.format(ProblemMessages.ArithmeticOrStringTypeExpectedForValueParameter, arguments[0]);
 		case FunctionsCannotReturnAFunction:
 			return String.format(ProblemMessages.FunctionsCannotReturnAFunction);
 		case FunctionsCannotReturnATuple:
 			return String.format(ProblemMessages.FunctionsCannotReturnATuple);
 		case FunctionsCannotReturnAuto:
 			return String.format(ProblemMessages.FunctionsCannotReturnAuto, arguments[0]);
 		case FunctionsCannotReturnScope:
 			return String.format(ProblemMessages.FunctionsCannotReturnScope, arguments[0]);
 		case RecursiveType:
 			return String.format(ProblemMessages.RecursiveType);
 		case VariadicFunctionsWithNonDLinkageMustHaveAtLeastOneParameter:
 			return String.format(ProblemMessages.VariadicFunctionsWithNonDLinkageMustHaveAtLeastOneParameter);
 		case SymbolMustBeAFunction:
 			return String.format(ProblemMessages.SymbolMustBeAFunction, arguments[0]);
 		case FunctionExpectedBeforeCall:
 			return String.format(ProblemMessages.FunctionExpectedBeforeCall, arguments[0]);
 		case FunctionExpectedBeforeCallNotSymbolOfType:
 			return String.format(ProblemMessages.FunctionExpectedBeforeCallNotSymbolOfType, arguments[0], arguments[1]);
 		case CircularReferenceOfTypedef:
 			return String.format(ProblemMessages.CircularReferenceOfTypedef, arguments[0]);
 		case StringSliceIsOutOfBounds:
 			return String.format(ProblemMessages.StringSliceIsOutOfBounds, arguments[0], arguments[1]);
 		case ErrorInstantiating:
 			return String.format(ProblemMessages.ErrorInstantiating);
 		case CaseMustBeAnIntegralOrStringConstant:
 			return String.format(ProblemMessages.CaseMustBeAnIntegralOrStringConstant, arguments[0]);
 		case DuplicateCaseInSwitchStatement:
 			return String.format(ProblemMessages.DuplicateCaseInSwitchStatement, arguments[0]);
 		case SpecialMemberFunctionsNotAllowedForSymbol:
 			return String.format(ProblemMessages.SpecialMemberFunctionsNotAllowedForSymbol, arguments[0]);
 		case SpecialFunctionsNotAllowedInInterface:
 			return String.format(ProblemMessages.SpecialFunctionsNotAllowedInInterface, arguments[0]);
 		case FunctionBodyIsNotAbstractInInterface:
 			return String.format(ProblemMessages.FunctionBodyIsNotAbstractInInterface, arguments[0]);
 		case SuperClassConstructorCallMustBeInAConstructor:
 			return String.format(ProblemMessages.SuperClassConstructorCallMustBeInAConstructor);
 		case ClassConstructorCallMustBeInAConstructor:
 			return String.format(ProblemMessages.ClassConstructorCallMustBeInAConstructor);
 		case NoSuperClassConstructor:
 			return String.format(ProblemMessages.NoSuperClassConstructor, arguments[0]);
 		case ConstructorCallsNotAllowedInLoopsOrAfterLabels:
 			return String.format(ProblemMessages.ConstructorCallsNotAllowedInLoopsOrAfterLabels);
 		case MultipleConstructorCalls:
 			return String.format(ProblemMessages.MultipleConstructorCalls);
 		case ExpressionIsNotConstantOrDoesNotEvaluateToABool:
 			return String.format(ProblemMessages.ExpressionIsNotConstantOrDoesNotEvaluateToABool, arguments[0]);
 		case StaticIfConditionalCannotBeAtGlobalScope:
 			return String.format(ProblemMessages.StaticIfConditionalCannotBeAtGlobalScope);
 		case CannotBreakOutOfFinallyBlock:
 			return String.format(ProblemMessages.CannotBreakOutOfFinallyBlock);
 		case LabelHasNoBreak:
 			return String.format(ProblemMessages.LabelHasNoBreak, arguments[0]);
 		case LabelHasNoContinue:
 			return String.format(ProblemMessages.LabelHasNoContinue, arguments[0]);
 		case CannotGotoInOrOutOfFinallyBlock:
 			return String.format(ProblemMessages.CannotGotoInOrOutOfFinallyBlock);
 		case CalledWithArgumentTypesMatchesBoth:
 			return String.format(ProblemMessages.CalledWithArgumentTypesMatchesBoth, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4]);
 		case SymbolIsNotAnArithmeticType:
 			return String.format(ProblemMessages.SymbolIsNotAnArithmeticType, arguments[0]);
 		case SymbolIsNotAnArithmeticTypeItIs:
 			return String.format(ProblemMessages.SymbolIsNotAnArithmeticTypeItIs, arguments[0], arguments[1]);
 		case CannotPerformModuloComplexArithmetic:
 			return String.format(ProblemMessages.CannotPerformModuloComplexArithmetic);
 		case OperatorNotAllowedOnBoolExpression:
 			return String.format(ProblemMessages.OperatorNotAllowedOnBoolExpression, arguments[0]);
 		case ForeachKeyTypeMustBeIntOrUint:
 			return String.format(ProblemMessages.ForeachKeyTypeMustBeIntOrUint, arguments[0]);
 		case ForeachKeyTypeMustBeIntOrUintLongOrUlong:
 			return String.format(ProblemMessages.ForeachKeyTypeMustBeIntOrUintLongOrUlong, arguments[0]);
 		case ForeachKeyCannotBeOutOrRef:
 			return String.format(ProblemMessages.ForeachKeyCannotBeOutOrRef);
 		case NoReverseIterationOnAssociativeArrays:
 			return String.format(ProblemMessages.NoReverseIterationOnAssociativeArrays);
 		case OnlyOneOrTwoArgumentsForAssociativeArrayForeach:
 			return String.format(ProblemMessages.OnlyOneOrTwoArgumentsForAssociativeArrayForeach);
 		case OnlyOneOrTwoArgumentsForArrayForeach:
 			return String.format(ProblemMessages.OnlyOneOrTwoArgumentsForArrayForeach);
 		case ForeachTargetIsNotAnArrayOf:
 			return String.format(ProblemMessages.ForeachTargetIsNotAnArrayOf, arguments[0], arguments[1]);
 		case ForeachKeyCannotBeInout:
 			return String.format(ProblemMessages.ForeachKeyCannotBeInout);
 		case ForeachValueOfUTFConversionCannotBeInout:
 			return String.format(ProblemMessages.ForeachValueOfUTFConversionCannotBeInout);
 		case CannotInferTypeForSymbol:
 			return String.format(ProblemMessages.CannotInferTypeForSymbol, arguments[0]);
 		case CannotInferTypeFromInitializer:
 			return String.format(ProblemMessages.CannotInferTypeFromInitializer);
 		case NoStorageClassForSymbol:
 			return String.format(ProblemMessages.NoStorageClassForSymbol, arguments[0]);
 		case OnlyOneValueOrTwoKeyValueArgumentsForTupleForeach:
 			return String.format(ProblemMessages.OnlyOneValueOrTwoKeyValueArgumentsForTupleForeach);
 		case CannotUniquelyInferForeachArgumentTypes:
 			return String.format(ProblemMessages.CannotUniquelyInferForeachArgumentTypes);
 		case InvalidForeachAggregate:
 			return String.format(ProblemMessages.InvalidForeachAggregate, arguments[0]);
 		case NotAnAssociativeArrayInitializer:
 			return String.format(ProblemMessages.NotAnAssociativeArrayInitializer);
 		case ArrayInitializersAsExpressionsNotAllowed:
 			return String.format(ProblemMessages.ArrayInitializersAsExpressionsNotAllowed);
 		case IftypeConditionCannotBeAtGlobalScope:
 			return String.format(ProblemMessages.IftypeConditionCannotBeAtGlobalScope);
 		case SymbolIsNotAFieldOfSymbol:
 			return String.format(ProblemMessages.SymbolIsNotAFieldOfSymbol, arguments[0], arguments[1]);
 		case RecursiveTemplateExpansion:
 			return String.format(ProblemMessages.RecursiveTemplateExpansion);
 		case RecursiveTemplateExpansionForTemplateArgument:
 			return String.format(ProblemMessages.RecursiveTemplateExpansionForTemplateArgument, arguments[0]);
 		case IndexIsNotATypeOrExpression:
 			return String.format(ProblemMessages.IndexIsNotATypeOrExpression);
 		case CannotHavePointerToSymbol:
 			return String.format(ProblemMessages.CannotHavePointerToSymbol, arguments[0]);
 		case SizeOfTypeIsNotKnown:
 			return String.format(ProblemMessages.SizeOfTypeIsNotKnown, arguments[0]);
 		case CanOnlySliceTupleTypes:
 			return String.format(ProblemMessages.CanOnlySliceTupleTypes, arguments[0]);
 		case NoPropertyForTuple:
 			return String.format(ProblemMessages.NoPropertyForTuple, arguments[0], arguments[1]);
 		case CannotResolveDotProperty:
 			return String.format(ProblemMessages.CannotResolveDotProperty, arguments[0]);
 		case CannotTakeAddressOfBitInArray:
 			return String.format(ProblemMessages.CannotTakeAddressOfBitInArray);
 		case OnlyOneIndexAllowedToIndex:
 			return String.format(ProblemMessages.OnlyOneIndexAllowedToIndex, arguments[0]);
 		case NoOpIndexOperatorOverloadForType:
 			return String.format(ProblemMessages.NoOpIndexOperatorOverloadForType, arguments[0]);
 		case ArrayDimensionOverflow:
 			return String.format(ProblemMessages.ArrayDimensionOverflow);
 		case OperatorAssignmentOverloadWithOpIndexIllegal:
 			return String.format(ProblemMessages.OperatorAssignmentOverloadWithOpIndexIllegal);
 		case CannotHaveOutOrInoutArgumentOfBitInArray:
 			return String.format(ProblemMessages.CannotHaveOutOrInoutArgumentOfBitInArray);
 		case SymbolIsAliasedToAFunction:
 			return String.format(ProblemMessages.SymbolIsAliasedToAFunction, arguments[0]);
 		case LinkageDoesNotMatchInterfaceFunction:
 			return String.format(ProblemMessages.LinkageDoesNotMatchInterfaceFunction);
 		case InterfaceFunctionIsNotImplemented:
 			return String.format(ProblemMessages.InterfaceFunctionIsNotImplemented, arguments[0], arguments[1]);
 		case ExpectedKeyAsArgumentToRemove:
 			return String.format(ProblemMessages.ExpectedKeyAsArgumentToRemove);
 		case CyclicConstructorCall:
 			return String.format(ProblemMessages.CyclicConstructorCall);
 		case MissingOrCurruptObjectDotD:
 			return String.format(ProblemMessages.MissingOrCurruptObjectDotD);
 		case CannotContinueOutOfFinallyBlock:
 			return String.format(ProblemMessages.CannotContinueOutOfFinallyBlock);
 		case ForwardDeclaration:
 			return String.format(ProblemMessages.ForwardDeclaration);
 		case CannotFormDelegateDueToCovariantReturnType:
 			return String.format(ProblemMessages.CannotFormDelegateDueToCovariantReturnType);
 		case ForeachRangeKeyCannotHaveStorageClass:
 			return String.format(ProblemMessages.ForeachRangeKeyCannotHaveStorageClass);
 		case MultipleOverridesOfSameFunction:
 			return String.format(ProblemMessages.MultipleOverridesOfSameFunction);
 		case IdentityAssignmentOperatorOverloadIsIllegal:
 			return String.format(ProblemMessages.IdentityAssignmentOperatorOverloadIsIllegal);
 		case LiteralsCannotBeClassMembers:
 			return String.format(ProblemMessages.LiteralsCannotBeClassMembers);
 		case NoMatchForImplicitSuperCallInConstructor:
 			return String.format(ProblemMessages.NoMatchForImplicitSuperCallInConstructor);
 		case NoReturnAtEndOfFunction:
 			return String.format(ProblemMessages.NoReturnAtEndOfFunction);
 		case CanOnlyDeclareTypeAliasesWithinStaticIfConditionals:
 			return String.format(ProblemMessages.CanOnlyDeclareTypeAliasesWithinStaticIfConditionals);
 		case PackageAndModuleHaveTheSameName:
 			return String.format(ProblemMessages.PackageAndModuleHaveTheSameName);
 		case StringLiteralsAreImmutable:
 			return String.format(ProblemMessages.StringLiteralsAreImmutable);
 		case ExpressionDotNewIsOnlyForAllocatingNestedClasses:
 			return String.format(ProblemMessages.ExpressionDotNewIsOnlyForAllocatingNestedClasses);
 		case TooManyArgumentsForArray:
 			return String.format(ProblemMessages.TooManyArgumentsForArray);
 		case ReturnExpressionExpected:
 			return String.format(ProblemMessages.ReturnExpressionExpected);
 		case ReturnWithoutCallingConstructor:
 			return String.format(ProblemMessages.ReturnWithoutCallingConstructor);
 		case ModuleIsInMultiplePackages:
 			return String.format(ProblemMessages.ModuleIsInMultiplePackages, arguments[0]);
 		case ModuleIsInMultipleDefined:
 			return String.format(ProblemMessages.ModuleIsInMultipleDefined);
 		case NeedUpperAndLowerBoundToSlicePointer:
 			return String.format(ProblemMessages.NeedUpperAndLowerBoundToSlicePointer);
 		case NeedUpperAndLowerBoundToSliceTuple:
 			return String.format(ProblemMessages.NeedUpperAndLowerBoundToSliceTuple);
 		case CannotConvertStringLiteralToVoidPointer:
 			return String.format(ProblemMessages.CannotConvertStringLiteralToVoidPointer);
 		case SymbolIsNotAPreInstanceInitializableField:
 			return String.format(ProblemMessages.SymbolIsNotAPreInstanceInitializableField, arguments[0]);
 		case NoCaseStatementFollowingGoto:
 			return String.format(ProblemMessages.NoCaseStatementFollowingGoto);
 		case SwitchStatementHasNoDefault:
 			return String.format(ProblemMessages.SwitchStatementHasNoDefault);
 		case SymbolIsNotAFunctionTemplate:
 			return String.format(ProblemMessages.SymbolIsNotAFunctionTemplate, arguments[0]);
 		case TupleIsNotAValidTemplateValueArgument:
 			return String.format(ProblemMessages.TupleIsNotAValidTemplateValueArgument);
 		case IncompatibleArgumentsForTemplateInstantiation:
 			return String.format(ProblemMessages.IncompatibleArgumentsForTemplateInstantiation);
 		case ThrowStatementsCannotBeInContracts:
 			return String.format(ProblemMessages.ThrowStatementsCannotBeInContracts);
 		case CanOnlyThrowClassObjects:
 			return String.format(ProblemMessages.CanOnlyThrowClassObjects, arguments[0]);
 		case StringExpectedAsSecondArgument:
 			return String.format(ProblemMessages.StringExpectedAsSecondArgument);
 		case WrongNumberOfArguments:
 			return String.format(ProblemMessages.WrongNumberOfArguments, arguments[0]);
 		case StringMustBeChars:
 			return String.format(ProblemMessages.StringMustBeChars);
 		case InvalidFirstArgument:
 			return String.format(ProblemMessages.InvalidFirstArgument);
 		case FirstArgumentIsNotAClass:
 			return String.format(ProblemMessages.FirstArgumentIsNotAClass);
 		case ArgumentHasNoMembers:
 			return String.format(ProblemMessages.ArgumentHasNoMembers);
 		case SymbolHasNoMembers:
 			return String.format(ProblemMessages.SymbolHasNoMembers, arguments[0]);
 		case KindSymbolHasNoMembers:
 			return String.format(ProblemMessages.KindSymbolHasNoMembers, arguments[0], arguments[1]);
 		case DotOffsetDeprecated:
 			return String.format(ProblemMessages.DotOffsetDeprecated);
 		case NoClassInfoForComInterfaceObjects:
 			return String.format(ProblemMessages.NoClassInfoForComInterfaceObjects);
 		case NoClassInfoForCppInterfaceObjects:
 			return String.format(ProblemMessages.NoClassInfoForCppInterfaceObjects);
 		case CannotMakeReferenceToABit:
 			return String.format(ProblemMessages.CannotMakeReferenceToABit);
 		case CannotFormTupleOfTuples:
 			return String.format(ProblemMessages.CannotFormTupleOfTuples);
 		case MissingInitializerInStaticConstructorForConstVariable:
 			return String.format(ProblemMessages.MissingInitializerInStaticConstructorForConstVariable);
 		case GlobalsStaticsFieldsRefAndAutoParametersCannotBeAuto:
 			return String.format(ProblemMessages.GlobalsStaticsFieldsRefAndAutoParametersCannotBeAuto);
 		case GlobalsStaticsFieldsManifestConstantsRefAndAutoParametersCannotBeScope:
 			return String.format(ProblemMessages.GlobalsStaticsFieldsManifestConstantsRefAndAutoParametersCannotBeScope);
 		case ReferenceToScopeClassMustBeScope:
 			return String.format(ProblemMessages.ReferenceToScopeClassMustBeScope);
 		case NumberOfKeysMustMatchNumberOfValues:
 			return String.format(ProblemMessages.NumberOfKeysMustMatchNumberOfValues, arguments[0], arguments[1]);
 		case ExpectedNumberArguments:
 			return String.format(ProblemMessages.ExpectedNumberArguments, arguments[0], arguments[1]);
 		case ArraySliceIfOutOfBounds:
 			return String.format(ProblemMessages.ArraySliceIfOutOfBounds, arguments[0], arguments[1]);
 		case InvalidUCS32Char:
 			return String.format(ProblemMessages.InvalidUCS32Char, arguments[0]);
 		case TupleIndexExceedsBounds:
 			return String.format(ProblemMessages.TupleIndexExceedsBounds, arguments[0], arguments[1]);
 		case SliceIsOutOfRange:
 			return String.format(ProblemMessages.SliceIsOutOfRange, arguments[0], arguments[1], arguments[2]);
 		case CannotTakeAddressOf:
 			return String.format(ProblemMessages.CannotTakeAddressOf, arguments[0]);
 		case VariableIsUsedBeforeInitialization:
 			return String.format(ProblemMessages.VariableIsUsedBeforeInitialization, arguments[0]);
 		case EscapingReferenceToLocal:
 			return String.format(ProblemMessages.EscapingReferenceToLocal, arguments[0]);
 		case EscapingReferenceToAutoLocal:
 			return String.format(ProblemMessages.EscapingReferenceToAutoLocal, arguments[0]);
 		case EscapingReferenceToScopeLocal:
 			return String.format(ProblemMessages.EscapingReferenceToScopeLocal, arguments[0]);
 		case EscapingReferenceToLocalVariable:
 			return String.format(ProblemMessages.EscapingReferenceToLocalVariable, arguments[0]);
 		case EscapingReferenceToVariadicParameter:
 			return String.format(ProblemMessages.EscapingReferenceToVariadicParameter, arguments[0]);
 		case CanOnlyCatchClassObjects:
 			return String.format(ProblemMessages.CanOnlyCatchClassObjects, arguments[0]);
 		case BaseClassIsForwardReferenced:
 			return String.format(ProblemMessages.BaseClassIsForwardReferenced, arguments[0]);
 		case BaseIsForwardReferenced:
 			return String.format(ProblemMessages.BaseIsForwardReferenced, arguments[0]);
 		case CannotInheritFromFinalClass:
 			return String.format(ProblemMessages.CannotInheritFromFinalClass, arguments[0]);
 		case StaticClassCannotInheritFromNestedClass:
 			return String.format(ProblemMessages.StaticClassCannotInheritFromNestedClass, arguments[0]);
 		case SuperClassIsNestedWithin:
 			return String.format(ProblemMessages.SuperClassIsNestedWithin, arguments[0], arguments[1], arguments[2]);
 		case SuperClassIsNotNestedWithin:
 			return String.format(ProblemMessages.SuperClassIsNotNestedWithin, arguments[0], arguments[1], arguments[2]);
 		case ArrayComparisonTypeMismatch:
 			return String.format(ProblemMessages.ArrayComparisonTypeMismatch, arguments[0], arguments[1]);
 		case ConditionalExpressionIsNotAModifiableLvalue:
 			return String.format(ProblemMessages.ConditionalExpressionIsNotAModifiableLvalue, arguments[0]);
 		case CannotCastSymbolToSymbol:
 			return String.format(ProblemMessages.CannotCastSymbolToSymbol, arguments[0], arguments[1]);
 		case CannotDeleteInstanceOfComInterface:
 			return String.format(ProblemMessages.CannotDeleteInstanceOfComInterface, arguments[0]);
 		case TemplateIsNotAMemberOf:
 			return String.format(ProblemMessages.TemplateIsNotAMemberOf, arguments[0], arguments[1]);
 		case TemplateIdentifierIsNotAMemberOf:
 			return String.format(ProblemMessages.TemplateIdentifierIsNotAMemberOf, arguments[0], arguments[1], arguments[2]);
 		case TemplateIdentifierIsNotAMemberOfUndefined:
 			return String.format(ProblemMessages.TemplateIdentifierIsNotAMemberOfUndefined, arguments[0], arguments[1]);
 		case CanOnlyInitiailizeConstMemberInsideConstructor:
 			return String.format(ProblemMessages.CanOnlyInitiailizeConstMemberInsideConstructor, arguments[0], arguments[1], arguments[2]);
 		case SymbolIsNotAMember:
 			return String.format(ProblemMessages.SymbolIsNotAMember, arguments[0]);
 		case SymbolIsNotATemplate:
 			return String.format(ProblemMessages.SymbolIsNotATemplate, arguments[0], arguments[1]);
 		case DSymbolHasNoSize:
 			return String.format(ProblemMessages.DSymbolHasNoSize, arguments[0]);
 		case ExpressionOfTypeDoesNotHaveABooleanValue:
 			return String.format(ProblemMessages.ExpressionOfTypeDoesNotHaveABooleanValue, arguments[0], arguments[1]);
 		case ImplicitConversionCanCauseLossOfData:
 			return String.format(ProblemMessages.ImplicitConversionCanCauseLossOfData, arguments[0], arguments[1], arguments[2]);
 		case ForwardReferenceToType:
 			return String.format(ProblemMessages.ForwardReferenceToType, arguments[0]);
 		case FloatingPointConstantExpressionExpected:
 			return String.format(ProblemMessages.FloatingPointConstantExpressionExpected, arguments[0]);
 		case ExpressionIsNotAValidTemplateValueArgument:
 			return String.format(ProblemMessages.ExpressionIsNotAValidTemplateValueArgument, arguments[0]);
 		case InvalidRangeLowerBound:
 			return String.format(ProblemMessages.InvalidRangeLowerBound, arguments[0]);
 		case InvalidRangeUpperBound:
 			return String.format(ProblemMessages.InvalidRangeUpperBound, arguments[0]);
 		case SymbolIsNotAScalarType:
 			return String.format(ProblemMessages.SymbolIsNotAScalarType, arguments[0]);
 		case ForeachIndexMustBeType:
 			return String.format(ProblemMessages.ForeachIndexMustBeType, arguments[0], arguments[1]);
 		case ForeachValueMustBeType:
 			return String.format(ProblemMessages.ForeachValueMustBeType, arguments[0], arguments[1]);
 		case OpApplyFunctionMustReturnAnInt:
 			return String.format(ProblemMessages.OpApplyFunctionMustReturnAnInt, arguments[0]);
 		case FunctionOfTypeOverridesButIsNotCovariant:
 			return String.format(ProblemMessages.FunctionOfTypeOverridesButIsNotCovariant, arguments[0], arguments[1], arguments[2], arguments[3]);
 		case CannotOverrideFinalFunction:
 			return String.format(ProblemMessages.CannotOverrideFinalFunction, arguments[0]);
 		case IncompatibleCovariantTypes:
 			return String.format(ProblemMessages.IncompatibleCovariantTypes, arguments[0], arguments[1]);
 		case CannotUseTemplateToAddVirtualFunctionToClass:
 			return String.format(ProblemMessages.CannotUseTemplateToAddVirtualFunctionToClass, arguments[0]);
 		case OutResultIsAlreadyDefined:
 			return String.format(ProblemMessages.OutResultIsAlreadyDefined, arguments[0]);
 		case MissingInitializerForConstField:
 			return String.format(ProblemMessages.MissingInitializerForConstField, arguments[0]);
 		case MissingInitializerForFinalField:
 			return String.format(ProblemMessages.MissingInitializerForFinalField, arguments[0]);
 		case ImportNotFound:
 			return String.format(ProblemMessages.ImportNotFound, arguments[0]);
 		case SymbolMustBeAnArrayOfPointerType:
 			return String.format(ProblemMessages.SymbolMustBeAnArrayOfPointerType, arguments[0], arguments[1]);
 		case RvalueOfInExpressionMustBeAnAssociativeArray:
 			return String.format(ProblemMessages.RvalueOfInExpressionMustBeAnAssociativeArray, arguments[0]);
 		case InterfaceInheritsFromDuplicateInterface:
 			return String.format(ProblemMessages.InterfaceInheritsFromDuplicateInterface, arguments[0], arguments[1]);
 		case LabelIsAlreadyDefined:
 			return String.format(ProblemMessages.LabelIsAlreadyDefined, arguments[0]);
 		case CannotSubtractPointerFromSymbol:
 			return String.format(ProblemMessages.CannotSubtractPointerFromSymbol, arguments[0]);
 		case ThisForNestedClassMustBeAClassType:
 			return String.format(ProblemMessages.ThisForNestedClassMustBeAClassType, arguments[0]);
 		case CanOnlyDereferenceAPointer:
 			return String.format(ProblemMessages.CanOnlyDereferenceAPointer, arguments[0]);
 		case OuterClassThisNeededToNewNestedClass:
 			return String.format(ProblemMessages.OuterClassThisNeededToNewNestedClass, arguments[0], arguments[1]);
 		case ThisForNestedClassMustBeOfType:
 			return String.format(ProblemMessages.ThisForNestedClassMustBeOfType, arguments[0], arguments[1]);
 		case NoConstructorForSymbol:
 			return String.format(ProblemMessages.NoConstructorForSymbol, arguments[0]);
 		case NoAllocatorForSymbol:
 			return String.format(ProblemMessages.NoAllocatorForSymbol, arguments[0]);
 		case NegativeArrayIndex:
 			return String.format(ProblemMessages.NegativeArrayIndex, arguments[0]);
 		case NewCanOnlyCreateStructsDynamicArraysAndClassObjects:
 			return String.format(ProblemMessages.NewCanOnlyCreateStructsDynamicArraysAndClassObjects, arguments[0]);
 		case MismatchedFunctionReturnTypeInference:
 			return String.format(ProblemMessages.MismatchedFunctionReturnTypeInference, arguments[0], arguments[1]);
 		case ShiftLeftExceeds:
 			return String.format(ProblemMessages.ShiftLeftExceeds, arguments[0], arguments[1]);
 		case SymbolCannotBeSlicedWithBrackets:
 			return String.format(ProblemMessages.SymbolCannotBeSlicedWithBrackets, arguments[0]);
 		case SliceExpressionIsNotAModifiableLvalue:
 			return String.format(ProblemMessages.SliceExpressionIsNotAModifiableLvalue, arguments[0]);
 		case SymbolIsNotAMemberOf:
 			return String.format(ProblemMessages.SymbolIsNotAMemberOf, arguments[0], arguments[1]);
 		case MoreInitiailizersThanFields:
 			return String.format(ProblemMessages.MoreInitiailizersThanFields, arguments[0]);
 		case OverlappingInitiailization:
 			return String.format(ProblemMessages.OverlappingInitiailization, arguments[0]);
 		case CannotMakeExpressionOutOfInitializer:
 			return String.format(ProblemMessages.CannotMakeExpressionOutOfInitializer, arguments[0]);
 		case NoDefaultOrCaseInSwitchStatement:
 			return String.format(ProblemMessages.NoDefaultOrCaseInSwitchStatement, arguments[0]);
 		case SymbolIsNotASymbol:
 			return String.format(ProblemMessages.SymbolIsNotASymbol, arguments[0]);
 		case ForwardReferenceToTemplate:
 			return String.format(ProblemMessages.ForwardReferenceToTemplate, arguments[0]);
 		case ForwardReferenceToTemplateDeclaration:
 			return String.format(ProblemMessages.ForwardReferenceToTemplateDeclaration, arguments[0]);
 		case SpecializationNotAllowedForDeducedParameter:
 			return String.format(ProblemMessages.SpecializationNotAllowedForDeducedParameter, arguments[0]);
 		case CannotDeclareTemplateAtFunctionScope:
 			return String.format(ProblemMessages.CannotDeclareTemplateAtFunctionScope, arguments[0]);
 		case TemplateHasNoValue:
 			return String.format(ProblemMessages.TemplateHasNoValue, arguments[0]);
 		case CannotUseLocalAsTemplateParameter:
 			return String.format(ProblemMessages.CannotUseLocalAsTemplateParameter, arguments[0]);
 		case NoSizeForType:
 			return String.format(ProblemMessages.NoSizeForType, arguments[0]);
 		case SymbolDotSymbolIsNotADeclaration:
 			return String.format(ProblemMessages.SymbolDotSymbolIsNotADeclaration, arguments[0], arguments[1]);
 		case ThisIsRequiredButIsNotABaseClassOf:
 			return String.format(ProblemMessages.ThisIsRequiredButIsNotABaseClassOf, arguments[0], arguments[1]);
 		case ForwardReferenceToSymbol:
 			return String.format(ProblemMessages.ForwardReferenceToSymbol, arguments[0]);
 		case IdentifierOfSymbolIsNotDefined:
 			return String.format(ProblemMessages.IdentifierOfSymbolIsNotDefined, arguments[0], arguments[1]);
 		case StructIsForwardReferenced:
 			return String.format(ProblemMessages.StructIsForwardReferenced, arguments[0]);
 		case CannotUseTemplateToAddFieldToAggregate:
 			return String.format(ProblemMessages.CannotUseTemplateToAddFieldToAggregate, arguments[0]);
 		case CannotModifyFinalVariable:
 			return String.format(ProblemMessages.CannotModifyFinalVariable, arguments[0]);
 		case InvalidUtf8Sequence2:
 			return String.format(ProblemMessages.InvalidUtf8Sequence2);
 		case Utf16HighValuePastEndOfString:
 			return String.format(ProblemMessages.Utf16HighValuePastEndOfString);
 		case Utf16LowValueOutOfRange:
 			return String.format(ProblemMessages.Utf16LowValueOutOfRange);
 		case UnpairedUtf16Value:
 			return String.format(ProblemMessages.UnpairedUtf16Value);
 		case IllegalUtf16Value:
 			return String.format(ProblemMessages.IllegalUtf16Value);
 		case StaticConstructorCanOnlyBePartOfStructClassModule:
 			return String.format(ProblemMessages.StaticConstructorCanOnlyBePartOfStructClassModule);
 		case ShiftAssignIsOutsideTheRange:
 			return String.format(ProblemMessages.ShiftAssignIsOutsideTheRange, arguments[0], arguments[1]);
 		case TemplateTupleParameterMustBeLastOne:
 			return String.format(ProblemMessages.TemplateTupleParameterMustBeLastOne);
 		case SymbolIsNestedInBoth:
 			return String.format(ProblemMessages.SymbolIsNestedInBoth, arguments[0], arguments[1], arguments[2]);
 		case FunctionIsAbstract:
 			return String.format(ProblemMessages.FunctionIsAbstract, arguments[0]);
 		case KindSymbolDoesNotOverload:
 			return String.format(ProblemMessages.KindSymbolDoesNotOverload, arguments[0], arguments[1]);
 		case MismatchedTupleLengths:
 			return String.format(ProblemMessages.MismatchedTupleLengths, arguments[0], arguments[1]);
 		case DoNotUseNullWhenComparingClassTypes:
 			return String.format(ProblemMessages.DoNotUseNullWhenComparingClassTypes);
 		case UseTokenInsteadOfTokenWhenComparingWithNull:
 			return String.format(ProblemMessages.UseTokenInsteadOfTokenWhenComparingWithNull, arguments[0], arguments[1]);
 		case VoidDoesNotHaveAnInitializer:
 			return String.format(ProblemMessages.VoidDoesNotHaveAnInitializer);
 		case FunctionNameExpectedForStartAddress:
 			return String.format(ProblemMessages.FunctionNameExpectedForStartAddress);
 		case TypeofReturnMustBeInsideFunction:
 			return String.format(ProblemMessages.TypeofReturnMustBeInsideFunction);
 		case PostBlitsAreOnlyForStructUnionDefinitions:
 			return String.format(ProblemMessages.PostBlitsAreOnlyForStructUnionDefinitions);
 		case CannotHaveEDotTuple:
 			return String.format(ProblemMessages.CannotHaveEDotTuple);
 		case CannotCreateCppClasses:
 			return String.format(ProblemMessages.CannotCreateCppClasses);
 		case SwitchAndCaseAreInDifferentFinallyBlocks:
 			return String.format(ProblemMessages.SwitchAndCaseAreInDifferentFinallyBlocks);
 		case SwitchAndDefaultAreInDifferentFinallyBlocks:
 			return String.format(ProblemMessages.SwitchAndDefaultAreInDifferentFinallyBlocks);
 		case CannotHaveFieldWithSameStructType:
 			return String.format(ProblemMessages.CannotHaveFieldWithSameStructType);
 		case WithoutThisCannotBeConstInvariant:
 			return String.format(ProblemMessages.WithoutThisCannotBeConstInvariant);
 		case CannotModifySymbol:
 			return String.format(ProblemMessages.CannotModifySymbol, arguments[0]);
 		case CannotCallPublicExportFunctionFromImmutable:
 			return String.format(ProblemMessages.CannotCallPublicExportFunctionFromImmutable, arguments[0]);
 		case TemplateMemberFunctionNotAllowedInInterface:
 			return String.format(ProblemMessages.TemplateMemberFunctionNotAllowedInInterface, arguments[0]);
 		case ArgumentToTypeofIsNotAnExpression:
 			return String.format(ProblemMessages.ArgumentToTypeofIsNotAnExpression, arguments[0]);
 		case CannotInferTypeFromOverloadedFunctionSymbol:
 			return String.format(ProblemMessages.CannotInferTypeFromOverloadedFunctionSymbol, arguments[0]);
 		case SymbolIsNotMutable:
 			return String.format(ProblemMessages.SymbolIsNotMutable, arguments[0]);
 		case SymbolForSymbolNeedsToBeType:
 			return String.format(ProblemMessages.SymbolForSymbolNeedsToBeType, arguments[0], arguments[1], arguments[2], arguments[3]);
 		case CannotModifyConstImmutable:
 			return String.format(ProblemMessages.CannotModifyConstImmutable, arguments[0]);
 		case SymbolCanOnlyBeCalledOnAnInvariantObject:
 			return String.format(ProblemMessages.SymbolCanOnlyBeCalledOnAnInvariantObject, arguments[0]);
 		case SymbolCanOnlyBeCalledOnAMutableObject:
 			return String.format(ProblemMessages.SymbolCanOnlyBeCalledOnAMutableObject, arguments[0], arguments[1]);
 		case CannotCallMutableMethodOnFinalStruct:
 			return String.format(ProblemMessages.CannotCallMutableMethodOnFinalStruct);
 		case ForwardReferenceOfImport:
 			return String.format(ProblemMessages.ForwardReferenceOfImport, arguments[0]);
 		case TemplateDoesNotHaveProperty:
 			return String.format(ProblemMessages.TemplateDoesNotHaveProperty, arguments[0], arguments[1]);
 		case ExpressionDoesNotHaveProperty:
 			return String.format(ProblemMessages.ExpressionDoesNotHaveProperty, arguments[0], arguments[1]);
 		case RecursiveOpCmpExpansion:
 			return String.format(ProblemMessages.RecursiveOpCmpExpansion);
 		case CtorIsReservedForConstructors:
 			return String.format(ProblemMessages.CtorIsReservedForConstructors);
 		case FunctionOverridesBaseClassFunctionButIsNotMarkedWithOverride:
 			return String.format(ProblemMessages.FunctionOverridesBaseClassFunctionButIsNotMarkedWithOverride, arguments[0], arguments[1]);
 		case ForwardReferenceOfTypeDotMangleof:
 			return String.format(ProblemMessages.ForwardReferenceOfTypeDotMangleof, arguments[0]);
 		case ArrayLengthHidesOtherLengthNameInOuterScope:
 			return String.format(ProblemMessages.ArrayLengthHidesOtherLengthNameInOuterScope);
 		case OnePathSkipsConstructor:
 			return String.format(ProblemMessages.OnePathSkipsConstructor);
 		case PragmaLibNotAllowedAsStatement:
 			return String.format(ProblemMessages.PragmaLibNotAllowedAsStatement);
 		case ThereCanBeOnlyOneAliasThis:
 			return String.format(ProblemMessages.ThereCanBeOnlyOneAliasThis);
 		case AliasThisCanOnlyAppearInStructOrClassDeclaration:
 			return String.format(ProblemMessages.AliasThisCanOnlyAppearInStructOrClassDeclaration, arguments[0]);
 		case FunctionIsOverloaded:
 			return String.format(ProblemMessages.FunctionIsOverloaded, arguments[0]);
 		case PureFunctionCannotCallImpure:
 			return String.format(ProblemMessages.PureFunctionCannotCallImpure, arguments[0], arguments[1]);
 		case PureFunctionCannotAccessMutableStaticData:
 			return String.format(ProblemMessages.PureFunctionCannotAccessMutableStaticData, arguments[0], arguments[1]);
 		case PureNestedFunctionCannotAccessMutableData:
 			return String.format(ProblemMessages.PureNestedFunctionCannotAccessMutableData, arguments[0], arguments[1]);
 		case ConstraintIsNotConstantOrDoesNotEvaluateToABool:
 			return String.format(ProblemMessages.ConstraintIsNotConstantOrDoesNotEvaluateToABool, arguments[0]);
 		case EscapeStringLiteralDeprecated:
 			return String.format(ProblemMessages.EscapeStringLiteralDeprecated);
 		case DelimiterCannotBeWhitespace:
 			return String.format(ProblemMessages.DelimiterCannotBeWhitespace);
 		case ArrayEqualityComparisonTypeMismatch:
 			return String.format(ProblemMessages.ArrayEqualityComparisonTypeMismatch, arguments[0], arguments[1]);
 		case GsharedNotAllowedInSafeMode:
 			return String.format(ProblemMessages.GsharedNotAllowedInSafeMode);
 		case InnerStructCannotBeAField:
 			return String.format(ProblemMessages.InnerStructCannotBeAField, arguments[0]);
 		case OnlyParametersOfForeachDeclarationsCanBeRef:
 			return String.format(ProblemMessages.OnlyParametersOfForeachDeclarationsCanBeRef);
 		case ManifestConstantsMustHaveInitializers:
 			return String.format(ProblemMessages.ManifestConstantsMustHaveInitializers);
 		case ForwardReferenceToInferredReturnTypeOfFunctionCall:
 			return String.format(ProblemMessages.ForwardReferenceToInferredReturnTypeOfFunctionCall, arguments[0]);
 		case CaseRangesNotAllowedInFinalSwitch:
 			return String.format(ProblemMessages.CaseRangesNotAllowedInFinalSwitch);
 		case MoreThan256CasesInCaseRange:
 			return String.format(ProblemMessages.MoreThan256CasesInCaseRange);
 		case CannotCastTuple:
 			return String.format(ProblemMessages.CannotCastTuple);
 		case CastNotAllowedInSafeMode:
 			return String.format(ProblemMessages.CastNotAllowedInSafeMode, arguments[0], arguments[1]);
 		case SliceIsNotMutable:
 			return String.format(ProblemMessages.SliceIsNotMutable, arguments[0]);
 		case SymbolIsNothrowYetMayThrow:
 			return String.format(ProblemMessages.SymbolIsNothrowYetMayThrow, arguments[0]);
 		case DefaultConstructorNotAllowedForStructs:
 			return String.format(ProblemMessages.DefaultConstructorNotAllowedForStructs);
		case VoidDoesNotHaveADefaultInitializer:
			return String.format(ProblemMessages.VoidDoesNotHaveADefaultInitializer);
 		case CannotImplicitlyConvertToImmutable:
 			return String.format(ProblemMessages.CannotImplicitlyConvertToImmutable, arguments[0]);
 		case CannotHaveConstInvariantOutParameterOfType:
 			return String.format(ProblemMessages.CannotHaveConstInvariantOutParameterOfType, arguments[0]);
 		case OverloadSetNotAllowedInStructDeclaration:
 			return String.format(ProblemMessages.OverloadSetNotAllowedInStructDeclaration, arguments[0], arguments[1]);
 		case CannotPerformArrayOperationsOnVoidArrays:
 			return String.format(ProblemMessages.CannotPerformArrayOperationsOnVoidArrays);
 		default:
 			return "";
 		}
 	}
 	
 	public int getLength() {
 		return sourceEnd;
 	}
 
 	public int getSourceStart() {
 		return sourceStart;
 	}
 	
 	public int getSourceEnd() {
 		return sourceEnd;
 	}
 	
 	public boolean isError() {
 		return isError;
 	}
 	
 	public boolean isWarning() {
 		return !isError;
 	}
 	
 	public int getSourceLineNumber() {
 		return sourceLineNumber;
 	}
 	
 	public int getCategoryID() {
 		return categoryId;
 	}
 	
 	public String getMarkerType() {
 		return "descent.core.problem";
 	}
 	
 	public String[] getArguments() {
 		return CharOperation.NO_STRINGS;
 	}
 	
 	public char[] getOriginatingFileName() {
 		return CharOperation.NO_CHAR;
 	}
 
 	public void setSourceEnd(int sourceEnd) {
 		this.sourceEnd = sourceEnd;
 	}
 
 	public void setSourceLineNumber(int lineNumber) {
 		this.sourceLineNumber = lineNumber;
 	}
 
 	public void setSourceStart(int sourceStart) {
 		this.sourceStart = sourceStart;
 	}
 	
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
 	public String[] getExtraMarkerAttributeNames() {
 		return CharOperation.NO_STRINGS;
 	}
 
 	/**
 	 * Returns the respective values for the extra marker attributes associated to this problem when persisted into 
 	 * a marker by the JavaBuilder. Each value must correspond to a matching attribute name, as defined by
 	 * {@link #getExtraMarkerAttributeNames()}. 
 	 * The values must be eligible for marker creation, as defined by <code>IMarker#setAttributes(String[], Object[])</code>.
 	 * @return the values of the corresponding extra marker attributes
 	 */
 	public Object[] getExtraMarkerAttributeValues() {
 		return NO_OBJECTS;
 	}
 	
 	@Override
 	public String toString() {
 		return getMessage();
 	}
 
 }
