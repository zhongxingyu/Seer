 /*
  * Copyright (c) 2006, 2008 Borland Software Corporation and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     committers of openArchitectureWare - Xpand language syntax
  *     Artem Tikhomirov (Borland) - LALR grammar
  *                                - Migration to OCL expressions
  */
 package org.eclipse.gmf.internal.xpand.parser;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import lpg.lpgjavaruntime.BadParseException;
 import lpg.lpgjavaruntime.BadParseSymFileException;
 import lpg.lpgjavaruntime.DeterministicParser;
 import lpg.lpgjavaruntime.DiagnoseParser;
 import lpg.lpgjavaruntime.ErrorToken;
 import lpg.lpgjavaruntime.IToken;
 import lpg.lpgjavaruntime.LexStream;
 import lpg.lpgjavaruntime.Monitor;
 import lpg.lpgjavaruntime.NotDeterministicParseTableException;
 import lpg.lpgjavaruntime.NullExportedSymbolsException;
 import lpg.lpgjavaruntime.NullTerminalSymbolsException;
 import lpg.lpgjavaruntime.ParseTable;
 import lpg.lpgjavaruntime.PrsStream;
 import lpg.lpgjavaruntime.RuleAction;
 import lpg.lpgjavaruntime.TokenStream;
 import lpg.lpgjavaruntime.UndefinedEofSymbolException;
 import lpg.lpgjavaruntime.UnimplementedTerminalsException;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.gmf.internal.xpand.Activator;
 import org.eclipse.gmf.internal.xpand.ast.Advice;
 import org.eclipse.gmf.internal.xpand.ast.Definition;
 import org.eclipse.gmf.internal.xpand.ast.IfStatement;
 import org.eclipse.gmf.internal.xpand.ast.Template;
 import org.eclipse.gmf.internal.xpand.expression.ast.Identifier;
 import org.eclipse.gmf.internal.xpand.util.ParserException.ErrorLocationInfo;
 import org.eclipse.m2m.internal.qvt.oml.cst.ImperativeIterateExpCS;
 import org.eclipse.m2m.internal.qvt.oml.cst.NewRuleCallExpCS;
 import org.eclipse.m2m.internal.qvt.oml.cst.temp.TempFactory;
 import org.eclipse.ocl.cst.BooleanLiteralExpCS;
 import org.eclipse.ocl.cst.CSTFactory;
 import org.eclipse.ocl.cst.CSTNode;
 import org.eclipse.ocl.cst.CallExpCS;
 import org.eclipse.ocl.cst.CollectionLiteralExpCS;
 import org.eclipse.ocl.cst.CollectionLiteralPartCS;
 import org.eclipse.ocl.cst.CollectionRangeCS;
 import org.eclipse.ocl.cst.CollectionTypeCS;
 import org.eclipse.ocl.cst.CollectionTypeIdentifierEnum;
 import org.eclipse.ocl.cst.DotOrArrowEnum;
 import org.eclipse.ocl.cst.EnumLiteralExpCS;
 import org.eclipse.ocl.cst.FeatureCallExpCS;
 import org.eclipse.ocl.cst.IfExpCS;
 import org.eclipse.ocl.cst.IntegerLiteralExpCS;
 import org.eclipse.ocl.cst.InvalidLiteralExpCS;
 import org.eclipse.ocl.cst.IsMarkedPreCS;
 import org.eclipse.ocl.cst.IterateExpCS;
 import org.eclipse.ocl.cst.IteratorExpCS;
 import org.eclipse.ocl.cst.LetExpCS;
 import org.eclipse.ocl.cst.MessageExpCS;
 import org.eclipse.ocl.cst.MessageExpKind;
 import org.eclipse.ocl.cst.NullLiteralExpCS;
 import org.eclipse.ocl.cst.OCLExpressionCS;
 import org.eclipse.ocl.cst.OCLMessageArgCS;
 import org.eclipse.ocl.cst.OperationCallExpCS;
 import org.eclipse.ocl.cst.PathNameCS;
 import org.eclipse.ocl.cst.PrimitiveTypeCS;
 import org.eclipse.ocl.cst.RealLiteralExpCS;
 import org.eclipse.ocl.cst.SimpleNameCS;
 import org.eclipse.ocl.cst.SimpleTypeEnum;
 import org.eclipse.ocl.cst.StateExpCS;
 import org.eclipse.ocl.cst.StringLiteralExpCS;
 import org.eclipse.ocl.cst.TupleLiteralExpCS;
 import org.eclipse.ocl.cst.TupleTypeCS;
 import org.eclipse.ocl.cst.TypeCS;
 import org.eclipse.ocl.cst.UnlimitedNaturalLiteralExpCS;
 import org.eclipse.ocl.cst.VariableCS;
 import org.eclipse.ocl.cst.VariableExpCS;
 import org.eclipse.ocl.util.OCLStandardLibraryUtil;
 import org.eclipse.ocl.utilities.PredefinedType;
 
 public class XpandParser extends PrsStream implements RuleAction {
     private static ParseTable prs = new XpandParserprs();
     private DeterministicParser dtParser;
 
     public DeterministicParser getParser() { return dtParser; }
     private void setResult(Object object) { dtParser.setSym1(object); }
     public Object getRhsSym(int i) { return dtParser.getSym(i); }
 
     public int getRhsTokenIndex(int i) { return dtParser.getToken(i); }
     public IToken getRhsIToken(int i) { return super.getIToken(getRhsTokenIndex(i)); }
     
     public int getRhsFirstTokenIndex(int i) { return dtParser.getFirstToken(i); }
     public IToken getRhsFirstIToken(int i) { return super.getIToken(getRhsFirstTokenIndex(i)); }
 
     public int getRhsLastTokenIndex(int i) { return dtParser.getLastToken(i); }
     public IToken getRhsLastIToken(int i) { return super.getIToken(getRhsLastTokenIndex(i)); }
 
     public int getLeftSpan() { return dtParser.getFirstToken(); }
     public IToken getLeftIToken()  { return super.getIToken(getLeftSpan()); }
 
     public int getRightSpan() { return dtParser.getLastToken(); }
     public IToken getRightIToken() { return super.getIToken(getRightSpan()); }
 
     public int getRhsErrorTokenIndex(int i) {
         int index = dtParser.getToken(i);
         IToken err = super.getIToken(index);
         return (err instanceof ErrorToken ? index : 0);
     }
     public ErrorToken getRhsErrorIToken(int i) {
         int index = dtParser.getToken(i);
         IToken err = super.getIToken(index);
         return (ErrorToken) (err instanceof ErrorToken ? err : null);
     }
 
     public XpandParser(LexStream lexStream) {
         super(lexStream);
         xpandFactory = new XpandFactory(lexStream.getFileName());
 
         try {
             super.remapTerminalSymbols(orderedTerminalSymbols(), XpandParserprs.EOFT_SYMBOL);
         }
         catch(NullExportedSymbolsException e) {
         }
         catch(NullTerminalSymbolsException e) {
         }
         catch(UnimplementedTerminalsException e) {
             java.util.ArrayList<?> unimplemented_symbols = e.getSymbols();
             System.out.println("The Lexer will not scan the following token(s):");
             for (int i = 0; i < unimplemented_symbols.size(); i++)
             {
                 Integer id = (Integer) unimplemented_symbols.get(i);
                 System.out.println("    " + XpandParsersym.orderedTerminalSymbols[id.intValue()]);               
             }
             System.out.println();                        
         }
         catch(UndefinedEofSymbolException e) {
             throw new Error(new UndefinedEofSymbolException
                                 ("The Lexer does not implement the Eof symbol " +
                                  XpandParsersym.orderedTerminalSymbols[XpandParserprs.EOFT_SYMBOL]));
         } 
     }
 
     public String[] orderedTerminalSymbols() { return XpandParsersym.orderedTerminalSymbols; }
     public String getTokenKindName(int kind) { return XpandParsersym.orderedTerminalSymbols[kind]; }            
     public int getEOFTokenKind() { return XpandParserprs.EOFT_SYMBOL; }
     public PrsStream getParseStream() { return (PrsStream) this; }
 
     public Template parser() {
         return parser(null, 0);
     }
         
     public Template parser(Monitor monitor) {
         return parser(monitor, 0);
     }
         
     public Template parser(int error_repair_count) {
         return parser(null, error_repair_count);
     }
         
     public Template parser(Monitor monitor, int error_repair_count) {
         try {
         	resetErrors();
             dtParser = new DeterministicParser(monitor, (TokenStream)this, prs, (RuleAction)this);
         }
         catch (NotDeterministicParseTableException e) {
             throw new Error(new NotDeterministicParseTableException
                                 ("Regenerate XpandParserprs.java with -NOBACKTRACK option"));
         }
         catch (BadParseSymFileException e) {
             throw new Error(new BadParseSymFileException("Bad Parser Symbol File -- XpandParsersym.java. Regenerate XpandParserprs.java"));
         }
 
         try {
             return (Template) dtParser.parse();
         }
         catch (BadParseException e) {
             reset(e.error_token); // point to error token
 
             DiagnoseParser diagnoseParser = new DiagnoseParser(this, prs);
             diagnoseParser.diagnose(e.error_token);
         }
 
         return null;
     }
 
 
 	public ErrorLocationInfo[] getErrors() {
 		return errors.toArray(new ErrorLocationInfo[errors.size()]);
 	}
 
 	private void resetErrors() {
 		errors.clear();
 	}
 
 	private final List<ErrorLocationInfo> errors = new LinkedList<ErrorLocationInfo>();
 
 	@Override
 	public void reportError(int i, String code) {
 		Activator.logWarn("Unexpected #reportError(int,String)");
 		reportError(i, i);
 	}
 
 	@Override
 	public void reportError(int leftToken, int rightToken) {
 		final int errorCode = (rightToken >= getStreamLength() ? EOF_CODE : leftToken == rightToken ? LEX_ERROR_CODE : INVALID_TOKEN_CODE);
 		final int endToken = (leftToken == rightToken ? rightToken : rightToken - 1);
 		reportError(errorCode, null, leftToken, endToken, getName(leftToken));
 	}
 
 	@Override
 	public void reportError(int errorCode, String locationInfo, String tokenText) {
 		try {
 			Matcher m = Pattern.compile("(?:[^:]+::)*[^:]+:(\\d+):(\\d+):(\\d+):(\\d+):.*").matcher(locationInfo);
 			boolean t = m.matches(); // ignore return value, rely on exception if anything wrong
 			assert t;
 			final int leftTokenLine = Integer.parseInt(m.group(1));
 			final int leftTokenColumn = Integer.parseInt(m.group(2));
 			final int rightTokenLine = Integer.parseInt(m.group(3));
 			final int rightTokenColumn = Integer.parseInt(m.group(4));
 			final String msg = tokenText + errorMsgText[errorCode];
 			errors.add(new ErrorLocationInfo(msg, leftTokenLine, leftTokenColumn, rightTokenLine, rightTokenColumn));
 		} catch (Throwable ex) {
 			// ignore
 			errors.add(new ErrorLocationInfo(tokenText + errorMsgText[errorCode]));
 		}
 	}
 
 	@Override
 	public void reportError(int errorCode, String locationInfo, int leftToken, int rightToken, String tokenText) {
 		final int leftTokenLine = getLine(leftToken);
 		final int leftTokenColumn = getColumn(leftToken);
 		final int rightTokenLine = getEndLine(rightToken);
 		final int rightTokenColumn = getEndColumn(rightToken);
 		final String msg = tokenText + errorMsgText[errorCode] + (locationInfo != null && locationInfo.length() > 0 ?  '(' + locationInfo + ')' : "");
 		final int startOffset = getStartOffset(leftToken);
 		final int endOffset = getEndOffset(rightToken);
 		errors.add(new ErrorLocationInfo(msg, leftTokenLine, leftTokenColumn, rightTokenLine, rightTokenColumn, startOffset, endOffset));
 	}
 
 	private final XpandFactory xpandFactory;
 
 
 
 	/**
 	 * 
 	 * QVT Operational specific part
 	 *
 	 */
 	
 	
 	@SuppressWarnings("unchecked")
 	private static final EList ourEmptyEList = new BasicEList.UnmodifiableEList(0, new Object[0]);								
 							
 	
 	private void diagnozeErrorToken(int token_index) {
 		IToken token = getIToken(token_index);
 		if (token instanceof lpg.lpgjavaruntime.ErrorToken) {
 			token = ((lpg.lpgjavaruntime.ErrorToken) token).getErrorToken();
 		}
 		
 		reportError(lpg.lpgjavaruntime.ParseErrorCodes.MISPLACED_CODE, "", token.getTokenIndex(), token.getTokenIndex(), //$NON-NLS-1$ 
 				"'" + token.toString() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
 		reset(token.getTokenIndex()); // point to error token
 		DiagnoseParser diagnoseParser = new DiagnoseParser(this, prs);
 		diagnoseParser.diagnose(token.getTokenIndex());
 		dtParser.setSym1(null);
 	}
 
 private ImperativeIterateExpCS createImperativeIterateExpCS(
         SimpleNameCS simpleNameCS,
         EList<VariableCS> iterators,
         VariableCS target,
         OCLExpressionCS body,
         OCLExpressionCS condition) {
     ImperativeIterateExpCS result = org.eclipse.m2m.internal.qvt.oml.cst.CSTFactory.eINSTANCE.createImperativeIterateExpCS();
     result.setSimpleNameCS(simpleNameCS);
     if (iterators.size() > 0) {
         result.setVariable1(iterators.get(0));
         if (iterators.size() > 1) {
             result.setVariable2(iterators.get(1));
         }
     }
     if (target != null) {
         result.setTarget(target);
     }
     result.setBody(body);
     result.setCondition(condition);
     return result;
 }
 
 private NewRuleCallExpCS createNewRuleCallExpCS(PathNameCS pathNameCS, List<OCLExpressionCS> arguments) {
 	NewRuleCallExpCS call = org.eclipse.m2m.internal.qvt.oml.cst.CSTFactory.eINSTANCE.createNewRuleCallExpCS();
	call.setScopedIdentifier(pathNameCS);
 	call.getArguments().addAll(arguments);
 	return call;
 }
 
 private boolean isTokenOfType(IToken token, int kind) {
 	return (token != null) && (token.getKind() == kind);
 }
 
 private OperationCallExpCS createOperationCallExpCS(
 		OCLExpressionCS oclExpressionCS,
 		SimpleNameCS simpleNameCS,
 		IsMarkedPreCS isMarkedPreCS,
 		EList<OCLExpressionCS> arguments) {
 	OperationCallExpCS result = CSTFactory.eINSTANCE.createOperationCallExpCS();
 	result.setSource(oclExpressionCS);
 	result.setSimpleNameCS(simpleNameCS);
 	result.setIsMarkedPreCS(isMarkedPreCS);
 	result.getArguments().addAll(arguments);
 	return result;
 }
 
 private OperationCallExpCS createOperationCallExpCS(
 		OCLExpressionCS oclExpressionCS,
 		SimpleNameCS simpleNameCS,
 		EList<OCLExpressionCS> arguments) {
 	return createOperationCallExpCS(oclExpressionCS, simpleNameCS,
 			createIsMarkedPreCS(false), arguments);
 }
 
 private OperationCallExpCS createOperationCallExpCS(
 		SimpleNameCS simpleNameCS,
 		IsMarkedPreCS isMarkedPreCS,
 		EList<OCLExpressionCS> arguments) {
 	return createOperationCallExpCS(null, simpleNameCS, isMarkedPreCS, arguments);
 }
 
 private OperationCallExpCS createOperationCallExpCS(
 		SimpleNameCS simpleNameCS,
 		IsMarkedPreCS isMarkedPreCS,
 		StateExpCS stateExpCS) {
 	OperationCallExpCS result = CSTFactory.eINSTANCE.createOperationCallExpCS();
 	result.setSimpleNameCS(simpleNameCS);
 	result.setIsMarkedPreCS(isMarkedPreCS);
 	result.getArguments().add(stateExpCS);
 	return result;
 }
 
 private StateExpCS createStateExpCS(PathNameCS pathName) {
 	StateExpCS result = CSTFactory.eINSTANCE.createStateExpCS();
 	result.getSequenceOfNames().addAll(pathName.getSequenceOfNames());
 	return result;
 }
 
 private VariableExpCS createVariableExpCS(
 		SimpleNameCS simpleNameCS,
 		EList<OCLExpressionCS> arguments,
 		IsMarkedPreCS isMarkedPreCS) {
 	VariableExpCS result = CSTFactory.eINSTANCE.createVariableExpCS();
 	result.setSimpleNameCS(simpleNameCS);
 	result.getArguments().addAll(arguments);
 	result.setIsMarkedPreCS(isMarkedPreCS);
 	return result;
 }
 
 private SimpleNameCS createSimpleNameCS(
 		SimpleTypeEnum type,
 		String value) {
 	SimpleNameCS result = CSTFactory.eINSTANCE.createSimpleNameCS();
 	result.setType(type);
 	result.setValue(unquote(value));
 	return result;
 }
 
 private PrimitiveTypeCS createPrimitiveTypeCS(
 		SimpleTypeEnum type,
 		String value) {
 	PrimitiveTypeCS result = CSTFactory.eINSTANCE.createPrimitiveTypeCS();
 	result.setType(type);
 	result.setValue(value);
 	return result;
 }
 
 private PathNameCS createPathNameCS(String pathName) {
 	PathNameCS result = CSTFactory.eINSTANCE.createPathNameCS();
 	result.getSequenceOfNames().add(unquote(pathName));
 	return result;
 }
 
 private PathNameCS extendPathNameCS(PathNameCS path, String name) {
 	path.getSequenceOfNames().add(unquote(name));
 	return path;
 }
 
 private PathNameCS createPathNameCS() {
 	return CSTFactory.eINSTANCE.createPathNameCS();
 }
 
 private EnumLiteralExpCS createEnumLiteralExpCS(
 		PathNameCS pathNameCS,
 		SimpleNameCS simpleNameCS) {
 	EnumLiteralExpCS result = CSTFactory.eINSTANCE.createEnumLiteralExpCS();
 	result.setPathNameCS(pathNameCS);
 	result.setSimpleNameCS(simpleNameCS);
 	return result;
 }
 
 private EnumLiteralExpCS createEnumLiteralExpCS(
 		PathNameCS pathNameCS,
 		String simpleName) {
 	return createEnumLiteralExpCS(pathNameCS,
 		createSimpleNameCS(SimpleTypeEnum.IDENTIFIER_LITERAL, simpleName));
 }
 
 private CollectionLiteralExpCS createCollectionLiteralExpCS(
 		CollectionTypeIdentifierEnum type,
 		EList<CollectionLiteralPartCS> collectionLiteralParts) {
 	CollectionLiteralExpCS result = CSTFactory.eINSTANCE.createCollectionLiteralExpCS();
 	result.setCollectionType(type);
 	result.getCollectionLiteralParts().addAll(collectionLiteralParts);
 	return result;
 }
 
 private CollectionLiteralPartCS createCollectionLiteralPartCS(OCLExpressionCS oclExpressionCS) {
 	CollectionLiteralPartCS result = CSTFactory.eINSTANCE.createCollectionLiteralPartCS();
 	result.setExpressionCS(oclExpressionCS);
 	return result;
 }
 
 private CollectionRangeCS createCollectionRangeCS(
 		OCLExpressionCS oclExpressionCS,
 		OCLExpressionCS lastOCLExpressionCS) {
 	CollectionRangeCS result = CSTFactory.eINSTANCE.createCollectionRangeCS();
 	result.setExpressionCS(oclExpressionCS);
 	result.setLastExpressionCS(lastOCLExpressionCS);
 	return result;
 }
 
 private IntegerLiteralExpCS createRangeStart(
 		String integerDotDot,
 		boolean isNegative) {
 	String intToken = integerDotDot.substring(0, integerDotDot.indexOf('.'));
 	int intValue = Integer.parseInt(intToken);
 	if (isNegative) {
 		intValue = -intValue;
 	}
 	
 	IntegerLiteralExpCS result = CSTFactory.eINSTANCE.createIntegerLiteralExpCS();
 	result.setIntegerSymbol(new Integer(intValue));
 	result.setSymbol(Integer.toString(intValue));
 	
 	return result;
 }
 
 private TupleLiteralExpCS createTupleLiteralExpCS(EList<VariableCS> variables) {
 	TupleLiteralExpCS result = CSTFactory.eINSTANCE.createTupleLiteralExpCS();
 	result.getVariables().addAll(variables);
 	return result;
 }
 
 private IntegerLiteralExpCS createIntegerLiteralExpCS(String string) {
 	IntegerLiteralExpCS result = CSTFactory.eINSTANCE.createIntegerLiteralExpCS();
 	result.setSymbol(string);
 	result.setIntegerSymbol(Integer.valueOf(string));
 	return result;
 }
 
 @SuppressWarnings("nls")
 private UnlimitedNaturalLiteralExpCS createUnlimitedNaturalLiteralExpCS(String string) {
 	UnlimitedNaturalLiteralExpCS result = CSTFactory.eINSTANCE.createUnlimitedNaturalLiteralExpCS();
 	result.setSymbol(string);
 	if ("*".equals(string)) {
 		result.setIntegerSymbol(-1);
 	} else {
 		result.setIntegerSymbol(Integer.valueOf(string));
 	}
 	return result;
 }
 
 private RealLiteralExpCS createRealLiteralExpCS(String string) {
 	RealLiteralExpCS result = CSTFactory.eINSTANCE.createRealLiteralExpCS();
 	result.setSymbol(string);
 	result.setRealSymbol(Double.valueOf(string));
 	return result;
 }
 
 private StringLiteralExpCS createStringLiteralExpCS(String string) {
 	StringLiteralExpCS result = CSTFactory.eINSTANCE.createStringLiteralExpCS();
 	result.setSymbol(string);
 	result.setStringSymbol(string);
 	return result;
 }
 
 private BooleanLiteralExpCS createBooleanLiteralExpCS(String string) {
 	BooleanLiteralExpCS result = CSTFactory.eINSTANCE.createBooleanLiteralExpCS();
 	result.setSymbol(string);
 	result.setBooleanSymbol(Boolean.valueOf(string));
 	return result;
 }
 
 private NullLiteralExpCS createNullLiteralExpCS(String string) {
 	NullLiteralExpCS result = CSTFactory.eINSTANCE.createNullLiteralExpCS();
 	result.setSymbol(string);
 	return result;
 }
 
 private InvalidLiteralExpCS createInvalidLiteralExpCS(String string) {
 	InvalidLiteralExpCS result = CSTFactory.eINSTANCE.createInvalidLiteralExpCS();
 	result.setSymbol(string);
 	return result;
 }
 
 private IteratorExpCS createIteratorExpCS(
 		SimpleNameCS simpleNameCS,
 		VariableCS variable1,
 		VariableCS variable2,
 		OCLExpressionCS oclExpressionCS) {
 	IteratorExpCS result = CSTFactory.eINSTANCE.createIteratorExpCS();
 	result.setSimpleNameCS(simpleNameCS);
 	result.setVariable1(variable1);
 	result.setVariable2(variable2);
 	result.setBody(oclExpressionCS);
 	return result;
 }
 
 private IterateExpCS createIterateExpCS(
 		SimpleNameCS simpleNameCS,
 		VariableCS variable1,
 		VariableCS variable2,
 		OCLExpressionCS oclExpressionCS) {
 	IterateExpCS result = CSTFactory.eINSTANCE.createIterateExpCS();
 	result.setSimpleNameCS(simpleNameCS);
 	result.setVariable1(variable1);
 	result.setVariable2(variable2);
 	result.setBody(oclExpressionCS);
 	return result;
 }
 
 private VariableCS createVariableCS(
 		String varName,
 		TypeCS typeCS,
 		OCLExpressionCS oclExpressionCS) {
 	VariableCS result = CSTFactory.eINSTANCE.createVariableCS();
 	result.setName(unquote(varName));
 	result.setTypeCS(typeCS);
 	result.setInitExpression(oclExpressionCS);
 	return result;
 }
 
 private CollectionTypeCS createCollectionTypeCS(
 		CollectionTypeIdentifierEnum collectionType,
 		TypeCS typeCS) {
 	CollectionTypeCS result = CSTFactory.eINSTANCE.createCollectionTypeCS();
 	result.setCollectionTypeIdentifier(collectionType);
 	result.setTypeCS(typeCS);
 	return result;
 }
 
 private TupleTypeCS createTupleTypeCS(EList<VariableCS> variables) {
 	TupleTypeCS result = CSTFactory.eINSTANCE.createTupleTypeCS();
 	result.getVariables().addAll(variables);
 	return result;
 }
 
 private FeatureCallExpCS createFeatureCallExpCS(
 		SimpleNameCS simpleNameCS,
 		EList<OCLExpressionCS> arguments,
 		IsMarkedPreCS isMarkedPreCS) {
 	FeatureCallExpCS result = CSTFactory.eINSTANCE.createFeatureCallExpCS();
 	result.setSimpleNameCS(simpleNameCS);
 	result.getArguments().addAll(arguments);
 	result.setIsMarkedPreCS(isMarkedPreCS);
 	return result;
 }
 
 private IsMarkedPreCS createIsMarkedPreCS(boolean isMarkedPre) {
 	IsMarkedPreCS result = CSTFactory.eINSTANCE.createIsMarkedPreCS();
 	result.setPre(isMarkedPre);
 	return result;
 }
 
 private LetExpCS createLetExpCS(
 		EList<VariableCS> variables,
 		OCLExpressionCS oclExpressionCS) {
 	LetExpCS result = CSTFactory.eINSTANCE.createLetExpCS();
 	result.getVariables().addAll(variables);
 	result.setInExpression(oclExpressionCS);
 	return result;
 }
 
 private IfExpCS createIfExpCS(
 		OCLExpressionCS condition,
 		OCLExpressionCS thenExpression,
 		OCLExpressionCS elseExpression) {
 	IfExpCS result = CSTFactory.eINSTANCE.createIfExpCS();
 	result.setCondition(condition);
 	result.setThenExpression(thenExpression);
 	result.setElseExpression(elseExpression);
 	return result;
 }
 
 private MessageExpCS createMessageExpCS(
 		boolean hasSent,
 		SimpleNameCS simpleNameCS,
 		EList<OCLMessageArgCS> oclMessageArgs) {
 	MessageExpCS result = CSTFactory.eINSTANCE.createMessageExpCS();
 	result.setKind(hasSent ? MessageExpKind.HAS_SENT_LITERAL : MessageExpKind.SENT_LITERAL);
 	result.setSimpleNameCS(simpleNameCS);
 	result.getArguments().addAll(oclMessageArgs);
 	return result;
 }
 
 private OCLMessageArgCS createOCLMessageArgCS(
 		TypeCS typeCS,
 		OCLExpressionCS oclExpressionCS) {
 	OCLMessageArgCS result = CSTFactory.eINSTANCE.createOCLMessageArgCS();
 	result.setTypeCS(typeCS);
 	result.setExpression(oclExpressionCS);
 	return result;
 }
 
 /**
  * Sets the start and end offsets of the given <code>CSTNode</code>
  * to the start and end offsets of the given <code>IToken</code>
  *
  * @param cstNode <code>CSTNode</code> to set offsets
  * @param startEnd <code>IToken</code> to retrieve offsets from
  */
 private void setOffsets(CSTNode cstNode, IToken startEnd) {
 	cstNode.setStartOffset(startEnd.getStartOffset());
 	cstNode.setEndOffset(startEnd.getEndOffset());
 }
 
 /**
  * Sets the start and end offsets of the given <code>CSTNode</code>
  * to the start and end offsets of the 2nd given <code>CSTNode</code>
  *
  * @param cstNode <code>CSTNode</code> to set offsets
  * @param startEnd <code>CSTNode</code> to retrieve offsets from
  */
 private void setOffsets(CSTNode cstNode, CSTNode startEnd) {
 	cstNode.setStartOffset(startEnd.getStartOffset());
 	cstNode.setEndOffset(startEnd.getEndOffset());
 }
 
 /**
  * Sets the start and end offsets of the given <code>CSTNode</code>
  * to the start offset of the 2nd given <code>CSTNode</code> and the
  * end offset of the 3rd given <code>CSTNode</code>
  *
  * @param cstNode <code>CSTNode</code> to set offsets
  * @param start <code>CSTNode</code> to retrieve start offset from
  * @param end <code>CSTNode</code> to retrieve end offset from
  */
 private void setOffsets(CSTNode cstNode, CSTNode start, CSTNode end) {
 	cstNode.setStartOffset(start.getStartOffset());
 	cstNode.setEndOffset(end.getEndOffset());
 }
 
 /**
  * Sets the start and end offsets of the given <code>CSTNode</code>
  * to the start offset of the 2nd given <code>CSTNode</code> and the
  * end offset of the given <code>IToken</code>
  *
  * @param cstNode <code>CSTNode</code> to set offsets
  * @param start <code>CSTNode</code> to retrieve start offset from
  * @param end <code>IToken</code> to retrieve end offset from
  */
 private void setOffsets(CSTNode cstNode, CSTNode start, IToken end) {
 	cstNode.setStartOffset(start.getStartOffset());
 	cstNode.setEndOffset(end.getEndOffset());
 }
 
 /**
  * Sets the start and end offsets of the given <code>CSTNode</code>
  * to the start offset of the given <code>IToken</code> and the
  * end offset of the 2nd given <code>CSTNode</code>
  *
  * @param cstNode <code>CSTNode</code> to set offsets
  * @param start <code>IToken</code> to retrieve start offset from
  * @param end <code>CSTNode</code> to retrieve end offset from
  */
 private void setOffsets(CSTNode cstNode, IToken start, CSTNode end) {
 	cstNode.setStartOffset(start.getStartOffset());
 	cstNode.setEndOffset(end.getEndOffset());
 }
 
 /**
  * Sets the start and end offsets of the given <code>CSTNode</code>
  * to the start offset of the 1std given <code>IToken</code> and the
  * end offset of the 2nd given <code>IToken</code>
  *
  * @param cstNode <code>CSTNode</code> to set offsets
  * @param start <code>IToken</code> to retrieve start offset from
  * @param end <code>IToken</code> to retrieve end offset from
  */
 private void setOffsets(CSTNode cstNode, IToken start, IToken end) {
 	cstNode.setStartOffset(start.getStartOffset());
 	cstNode.setEndOffset(end.getEndOffset());
 }
 
 /**
  * Removes the "s surrounding a quoted string, if any.
  * 
  * @param quoted a possibly quoted string
  * @return <code>quoted</code> without the surrounding quotes, or just
  *	 <code>quoted</code> verbatim if there were none
  */
 private String unquote(String quoted) {
 	String result = quoted;
 
 	if ((result != null) && (result.length() > 1)) {
 		int max = result.length() - 1;
 
 		if ((result.charAt(0) == '"') && (quoted.charAt(max) == '"')) {
 			result = result.substring(1, max);
 		}
 		
 		// this is a regexp, so the backslash needs to be
 		//   re-escaped, thus "\\" is rendered in a Java
 		//   string literal as "\\\\"
 		result = result.replaceAll("\\\\\"", "\"");  //$NON-NLS-2$//$NON-NLS-1$
 		/*
 		 * [artem] removed extra error handling fon non-spec escape processing 
 		 */
 	}
 
 	return result;
 }
 
 	@SuppressWarnings("unchecked")
 	public void ruleAction(int ruleNumber) {
 		switch (ruleNumber) {
 
  
 			//
 			// Rule 28:  impliesExpCS ::= impliesExpCS implies andOrXorExpCS
 			//
 			case 28:
  
 			//
 			// Rule 29:  impliesWithLet ::= impliesExpCS implies andOrXorWithLet
 			//
 			case 29:
  
 			//
 			// Rule 32:  andOrXorExpCS ::= andOrXorExpCS and equalityExpCS
 			//
 			case 32:
  
 			//
 			// Rule 33:  andOrXorExpCS ::= andOrXorExpCS or equalityExpCS
 			//
 			case 33:
  
 			//
 			// Rule 34:  andOrXorExpCS ::= andOrXorExpCS xor equalityExpCS
 			//
 			case 34:
  
 			//
 			// Rule 35:  andOrXorWithLet ::= andOrXorExpCS and equalityWithLet
 			//
 			case 35:
  
 			//
 			// Rule 36:  andOrXorWithLet ::= andOrXorExpCS or equalityWithLet
 			//
 			case 36:
  
 			//
 			// Rule 37:  andOrXorWithLet ::= andOrXorExpCS xor equalityWithLet
 			//
 			case 37: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							getTokenText(dtParser.getToken(2))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 40:  equalityExpCS ::= equalityExpCS = relationalExpCS
 			//
 			case 40:
  
 			//
 			// Rule 41:  equalityWithLet ::= equalityExpCS = relationalWithLet
 			//
 			case 41: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.EQUAL)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 42:  equalityExpCS ::= equalityExpCS <> relationalExpCS
 			//
 			case 42:
  
 			//
 			// Rule 43:  equalityWithLet ::= equalityExpCS <> relationalWithLet
 			//
 			case 43: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.NOT_EQUAL)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 46:  relationalExpCS ::= relationalExpCS > ifExpCSPrec
 			//
 			case 46:
  
 			//
 			// Rule 47:  relationalWithLet ::= relationalExpCS > additiveWithLet
 			//
 			case 47: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.GREATER_THAN)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 48:  relationalExpCS ::= relationalExpCS < ifExpCSPrec
 			//
 			case 48:
  
 			//
 			// Rule 49:  relationalWithLet ::= relationalExpCS < additiveWithLet
 			//
 			case 49: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.LESS_THAN)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 50:  relationalExpCS ::= relationalExpCS >= ifExpCSPrec
 			//
 			case 50:
  
 			//
 			// Rule 51:  relationalWithLet ::= relationalExpCS >= additiveWithLet
 			//
 			case 51: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.GREATER_THAN_EQUAL)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 52:  relationalExpCS ::= relationalExpCS <= ifExpCSPrec
 			//
 			case 52:
  
 			//
 			// Rule 53:  relationalWithLet ::= relationalExpCS <= additiveWithLet
 			//
 			case 53: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.LESS_THAN_EQUAL)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 57:  additiveExpCS ::= additiveExpCS + multiplicativeExpCS
 			//
 			case 57:
  
 			//
 			// Rule 58:  additiveWithLet ::= additiveExpCS + multiplicativeWithLet
 			//
 			case 58: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.PLUS)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 59:  additiveExpCS ::= additiveExpCS - multiplicativeExpCS
 			//
 			case 59:
  
 			//
 			// Rule 60:  additiveWithLet ::= additiveExpCS - multiplicativeWithLet
 			//
 			case 60: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.MINUS)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 63:  multiplicativeExpCS ::= multiplicativeExpCS * unaryExpCS
 			//
 			case 63:
  
 			//
 			// Rule 64:  multiplicativeWithLet ::= multiplicativeExpCS * unaryWithLet
 			//
 			case 64: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.TIMES)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 65:  multiplicativeExpCS ::= multiplicativeExpCS / unaryExpCS
 			//
 			case 65:
  
 			//
 			// Rule 66:  multiplicativeWithLet ::= multiplicativeExpCS / unaryWithLet
 			//
 			case 66: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.DIVIDE)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 69:  unaryExpCS ::= - unaryExpCS
 			//
 			case 69: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.MINUS)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(2),
 						simpleNameCS,
 						new BasicEList()
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(2));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 70:  unaryExpCS ::= not unaryExpCS
 			//
 			case 70: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(2),
 						simpleNameCS,
 						new BasicEList()
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(2));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 72:  dotArrowExpCS ::= dotArrowExpCS callExpCS
 			//
 			case 72: {
 				
 				CallExpCS result = (CallExpCS)dtParser.getSym(2);
 				result.setSource((OCLExpressionCS)dtParser.getSym(1));
 				setOffsets(result, (CSTNode)dtParser.getSym(1), result);
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 73:  dotArrowExpCS ::= dotArrowExpCS messageExpCS
 			//
 			case 73: {
 				
 				MessageExpCS result = (MessageExpCS)dtParser.getSym(2);
 				result.setTarget((OCLExpressionCS)dtParser.getSym(1));
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(2));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 74:  dotArrowExpCS ::= NUMERIC_OPERATION ( argumentsCSopt )
 			//
 			case 74: {
 				
 				// NUMERIC_OPERATION -> Integer '.' Identifier
 				String text = getTokenText(dtParser.getToken(1));
 				int index = text.indexOf('.');
 				String integer = text.substring(0, index);
 				String simpleName = text.substring(index + 1);
 
 				// create the IntegerLiteralExpCS
 				int startOffset = getIToken(dtParser.getToken(1)).getStartOffset();
 				int endOffset = startOffset + integer.length() - 1; // inclusive
 
 				IntegerLiteralExpCS integerLiteralExpCS = createIntegerLiteralExpCS(integer);
 				integerLiteralExpCS.setStartOffset(startOffset);
 				integerLiteralExpCS.setEndOffset(endOffset);
 
 				startOffset = endOffset + 2; // end of integerLiteral + 1('.') + 1(start of simpleName)
 				endOffset = getIToken(dtParser.getToken(1)).getEndOffset();
 
 				// create the SimpleNameCS
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.IDENTIFIER_LITERAL,
 							simpleName
 						);
 				simpleNameCS.setStartOffset(startOffset);
 				simpleNameCS.setEndOffset(endOffset);
 
 				// create the OperationCallExpCS
 				CSTNode result = createOperationCallExpCS(
 						integerLiteralExpCS,
 						simpleNameCS,
 						(EList)dtParser.getSym(3)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(4)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 75:  dotArrowExpCS ::= pathNameCS :: simpleNameCS ( argumentsCSopt )
 			//
 			case 75: {
 				
 				OperationCallExpCS result = createOperationCallExpCS(
 						(PathNameCS)dtParser.getSym(1),
 						(SimpleNameCS)dtParser.getSym(3),
 						(EList)dtParser.getSym(5)
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), getIToken(dtParser.getToken(6)));
 				result.setAccessor(DotOrArrowEnum.DOT_LITERAL);
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 80:  oclExpCS ::= ( oclExpressionCS )
 			//
 			case 80: {
 				
 				CSTNode result = (CSTNode)dtParser.getSym(2);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(3)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 81:  variableExpCS ::= simpleNameCS isMarkedPreCS
 			//
 			case 81: {
 				
 				IsMarkedPreCS isMarkedPreCS = (IsMarkedPreCS)dtParser.getSym(2);
 				CSTNode result = createVariableExpCS(
 						(SimpleNameCS)dtParser.getSym(1),
 						new BasicEList(),
 						isMarkedPreCS
 					);
 				if (isMarkedPreCS.isPre()) {
 					setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(2));
 				} else {
 					setOffsets(result, (CSTNode)dtParser.getSym(1));
 				}
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 82:  variableExpCS ::= keywordAsIdentifier1 isMarkedPreCS
 			//
 			case 82: {
 				
 				IsMarkedPreCS isMarkedPreCS = (IsMarkedPreCS)dtParser.getSym(2);
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.IDENTIFIER_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createVariableExpCS(
 						simpleNameCS,
 						new BasicEList(),
 						isMarkedPreCS
 					);
 				if (isMarkedPreCS.isPre()) {
 					setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(2));
 				} else {
 					setOffsets(result, getIToken(dtParser.getToken(1)));
 				}
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 84:  simpleNameCS ::= self
 			//
 			case 84: {
 				
 				CSTNode result = createSimpleNameCS(
 						SimpleTypeEnum.SELF_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 85:  simpleNameCS ::= IDENTIFIER
 			//
 			case 85: {
 				
 				CSTNode result = createSimpleNameCS(
 						SimpleTypeEnum.IDENTIFIER_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 86:  primitiveTypeCS ::= Integer
 			//
 			case 86: {
 				
 				CSTNode result = createPrimitiveTypeCS(
 						SimpleTypeEnum.INTEGER_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 87:  primitiveTypeCS ::= UnlimitedNatural
 			//
 			case 87: {
 				
 				CSTNode result = createPrimitiveTypeCS(
 						SimpleTypeEnum.UNLIMITED_NATURAL_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 88:  primitiveTypeCS ::= String
 			//
 			case 88: {
 				
 				CSTNode result = createPrimitiveTypeCS(
 						SimpleTypeEnum.STRING_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 89:  primitiveTypeCS ::= Real
 			//
 			case 89: {
 				
 				CSTNode result = createPrimitiveTypeCS(
 						SimpleTypeEnum.REAL_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 90:  primitiveTypeCS ::= Boolean
 			//
 			case 90: {
 				
 				CSTNode result = createPrimitiveTypeCS(
 						SimpleTypeEnum.BOOLEAN_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 91:  primitiveTypeCS ::= OclAny
 			//
 			case 91: {
 				
 				CSTNode result = createPrimitiveTypeCS(
 						SimpleTypeEnum.OCL_ANY_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 92:  primitiveTypeCS ::= OclVoid
 			//
 			case 92: {
 				
 				CSTNode result = createPrimitiveTypeCS(
 						SimpleTypeEnum.OCL_VOID_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 93:  primitiveTypeCS ::= Invalid
 			//
 			case 93: {
 				
 				CSTNode result = createPrimitiveTypeCS(
 						SimpleTypeEnum.INVALID_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 94:  primitiveTypeCS ::= OclMessage
 			//
 			case 94: {
 				
 				CSTNode result = createPrimitiveTypeCS(
 						SimpleTypeEnum.OCL_MESSAGE_LITERAL,
 						getTokenText(dtParser.getToken(1))
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 95:  pathNameCS ::= IDENTIFIER
 			//
 			case 95: {
 				
 				CSTNode result = createPathNameCS(getTokenText(dtParser.getToken(1)));
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 96:  pathNameCS ::= pathNameCS :: simpleNameCS
 			//
 			case 96: {
 				
 				PathNameCS result = (PathNameCS)dtParser.getSym(1);
 				result = extendPathNameCS(result, getTokenText(dtParser.getToken(3)));
 				setOffsets(result, result, (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 97:  pathNameCSOpt ::= $Empty
 			//
 			case 97: {
 				
 				CSTNode result = createPathNameCS();
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 105:  enumLiteralExpCS ::= pathNameCS :: keywordAsIdentifier
 			//
 			case 105: {
 				
 				CSTNode result = createEnumLiteralExpCS(
 						(PathNameCS)dtParser.getSym(1),
 						getTokenText(dtParser.getToken(3))
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), getIToken(dtParser.getToken(3)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 106:  enumLiteralExpCS ::= pathNameCS :: simpleNameCS
 			//
 			case 106: {
 				
 				CSTNode result = createEnumLiteralExpCS(
 						(PathNameCS)dtParser.getSym(1),
 						(SimpleNameCS)dtParser.getSym(3)
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 107:  collectionLiteralExpCS ::= collectionTypeIdentifierCS { collectionLiteralPartsCSopt }
 			//
 			case 107: {
 				
 				Object[] objs = (Object[])dtParser.getSym(1);
 				CSTNode result = createCollectionLiteralExpCS(
 						(CollectionTypeIdentifierEnum)objs[1],
 						(EList)dtParser.getSym(3)
 					);
 				setOffsets(result, (IToken)objs[0], getIToken(dtParser.getToken(4)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 108:  collectionTypeIdentifierCS ::= Set
 			//
 			case 108: {
 				
 				dtParser.setSym1(new Object[]{getIToken(dtParser.getToken(1)), CollectionTypeIdentifierEnum.SET_LITERAL});
 	  		  break;
 			}
 	 
 			//
 			// Rule 109:  collectionTypeIdentifierCS ::= Bag
 			//
 			case 109: {
 				
 				dtParser.setSym1(new Object[]{getIToken(dtParser.getToken(1)), CollectionTypeIdentifierEnum.BAG_LITERAL});
 	  		  break;
 			}
 	 
 			//
 			// Rule 110:  collectionTypeIdentifierCS ::= Sequence
 			//
 			case 110: {
 				
 				dtParser.setSym1(new Object[]{getIToken(dtParser.getToken(1)), CollectionTypeIdentifierEnum.SEQUENCE_LITERAL});
 	  		  break;
 			}
 	 
 			//
 			// Rule 111:  collectionTypeIdentifierCS ::= Collection
 			//
 			case 111: {
 				
 				dtParser.setSym1(new Object[]{getIToken(dtParser.getToken(1)), CollectionTypeIdentifierEnum.COLLECTION_LITERAL});
 	  		  break;
 			}
 	 
 			//
 			// Rule 112:  collectionTypeIdentifierCS ::= OrderedSet
 			//
 			case 112: {
 				
 				dtParser.setSym1(new Object[]{getIToken(dtParser.getToken(1)), CollectionTypeIdentifierEnum.ORDERED_SET_LITERAL});
 	  		  break;
 			}
 	 
 			//
 			// Rule 113:  collectionLiteralPartsCSopt ::= $Empty
 			//
 			case 113:
 				dtParser.setSym1(new BasicEList());
 				break;
  
 			//
 			// Rule 115:  collectionLiteralPartsCS ::= collectionLiteralPartCS
 			//
 			case 115: {
 				
 				EList result = new BasicEList();
 				result.add(dtParser.getSym(1));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 116:  collectionLiteralPartsCS ::= collectionLiteralPartsCS , collectionLiteralPartCS
 			//
 			case 116: {
 				
 				EList result = (EList)dtParser.getSym(1);
 				result.add(dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 118:  collectionLiteralPartCS ::= oclExpressionCS
 			//
 			case 118: {
 				
 				CSTNode result = createCollectionLiteralPartCS(
 						(OCLExpressionCS)dtParser.getSym(1)
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 119:  collectionRangeCS ::= - INTEGER_RANGE_START oclExpressionCS
 			//
 			case 119: {
 				
 				OCLExpressionCS rangeStart = createRangeStart(
 						getTokenText(dtParser.getToken(2)), true);
 				CSTNode result = createCollectionRangeCS(
 						rangeStart,
 						(OCLExpressionCS)dtParser.getSym(3)
 					);
 				setOffsets(result, rangeStart, (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 120:  collectionRangeCS ::= INTEGER_RANGE_START oclExpressionCS
 			//
 			case 120: {
 				
 				OCLExpressionCS rangeStart = createRangeStart(
 						getTokenText(dtParser.getToken(1)), false);
 				CSTNode result = createCollectionRangeCS(
 						rangeStart,
 						(OCLExpressionCS)dtParser.getSym(2)
 					);
 				setOffsets(result, rangeStart, (CSTNode)dtParser.getSym(2));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 121:  collectionRangeCS ::= oclExpressionCS .. oclExpressionCS
 			//
 			case 121: {
 				
 				CSTNode result = createCollectionRangeCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						(OCLExpressionCS)dtParser.getSym(3)
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 127:  tupleLiteralExpCS ::= Tuple { variableListCS2 }
 			//
 			case 127: {
 				
 				CSTNode result = createTupleLiteralExpCS((EList)dtParser.getSym(3));
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(4)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 128:  integerLiteralExpCS ::= INTEGER_LITERAL
 			//
 			case 128: {
 				
 				CSTNode result = createIntegerLiteralExpCS(getTokenText(dtParser.getToken(1)));
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 129:  unlimitedNaturalLiteralExpCS ::= *
 			//
 			case 129: {
 				
 				CSTNode result = createUnlimitedNaturalLiteralExpCS(getTokenText(dtParser.getToken(1)));
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 130:  realLiteralExpCS ::= REAL_LITERAL
 			//
 			case 130: {
 				
 				CSTNode result = createRealLiteralExpCS(getTokenText(dtParser.getToken(1)));
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 131:  stringLiteralExpCS ::= STRING_LITERAL
 			//
 			case 131: {
 				
 				CSTNode result = createStringLiteralExpCS(getTokenText(dtParser.getToken(1)));
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 132:  booleanLiteralExpCS ::= true
 			//
 			case 132: {
 				
 				CSTNode result = createBooleanLiteralExpCS(getTokenText(dtParser.getToken(1)));
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 133:  booleanLiteralExpCS ::= false
 			//
 			case 133: {
 				
 				CSTNode result = createBooleanLiteralExpCS(getTokenText(dtParser.getToken(1)));
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 134:  nullLiteralExpCS ::= null
 			//
 			case 134: {
 				
 				CSTNode result = createNullLiteralExpCS(getTokenText(dtParser.getToken(1)));
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 135:  invalidLiteralExpCS ::= OclInvalid
 			//
 			case 135: {
 				
 				CSTNode result = createInvalidLiteralExpCS(getTokenText(dtParser.getToken(1)));
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 136:  callExpCS ::= -> featureCallExpCS
 			//
 			case 136:
  
 			//
 			// Rule 137:  callExpCS ::= -> loopExpCS
 			//
 			case 137: {
 				
 				CallExpCS result = (CallExpCS)dtParser.getSym(2);
 				result.setAccessor(DotOrArrowEnum.ARROW_LITERAL);
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 138:  callExpCS ::= . keywordOperationCallExpCS
 			//
 			case 138:
  
 			//
 			// Rule 139:  callExpCS ::= . featureCallExpCS
 			//
 			case 139: {
 				
 				CallExpCS result = (CallExpCS)dtParser.getSym(2);
 				result.setAccessor(DotOrArrowEnum.DOT_LITERAL);
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 142:  iterContents ::= oclExpressionCS
 			//
 			case 142: {
 				
 				dtParser.setSym1(new Object[] {
 						null,
 						null,
 						dtParser.getSym(1)
 					});
 	  		  break;
 			}
 	 
 			//
 			// Rule 143:  iterContents ::= variableCS | oclExpressionCS
 			//
 			case 143: {
 				
 				dtParser.setSym1(new Object[] {
 						dtParser.getSym(1),
 						null,
 						dtParser.getSym(3)
 					});
 	  		  break;
 			}
 	 
 			//
 			// Rule 144:  iterContents ::= variableCS , variableCS | oclExpressionCS
 			//
 			case 144: {
 				
 				dtParser.setSym1(new Object[] {
 						dtParser.getSym(1),
 						dtParser.getSym(3),
 						dtParser.getSym(5)
 					});
 	  		  break;
 			}
 	 
 			//
 			// Rule 145:  iterateExpCS ::= iterate ( variableCS | oclExpressionCS )
 			//
 			case 145: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createIterateExpCS(
 						simpleNameCS,
 						(VariableCS)dtParser.getSym(3),
 						null,
 						(OCLExpressionCS)dtParser.getSym(5)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(6)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 146:  iterateExpCS ::= iterate ( variableCS ; variableCS | oclExpressionCS )
 			//
 			case 146: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createIterateExpCS(
 						simpleNameCS,
 						(VariableCS)dtParser.getSym(3),
 						(VariableCS)dtParser.getSym(5),
 						(OCLExpressionCS)dtParser.getSym(7)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(8)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 147:  variableCS ::= IDENTIFIER
 			//
 			case 147: {
 				
 				CSTNode result = createVariableCS(
 						getTokenText(dtParser.getToken(1)),
 						null,
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 148:  variableCS ::= IDENTIFIER : typeCS
 			//
 			case 148: {
 				
 				CSTNode result = createVariableCS(
 						getTokenText(dtParser.getToken(1)),
 						(TypeCS)dtParser.getSym(3),
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 149:  variableCS ::= IDENTIFIER : typeCS = oclExpressionCS
 			//
 			case 149: {
 				
 				CSTNode result = createVariableCS(
 						getTokenText(dtParser.getToken(1)),
 						(TypeCS)dtParser.getSym(3),
 						(OCLExpressionCS)dtParser.getSym(5)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(5));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 150:  variableCS2 ::= IDENTIFIER = oclExpressionCS
 			//
 			case 150: {
 				
 				CSTNode result = createVariableCS(
 						getTokenText(dtParser.getToken(1)),
 						null,
 						(OCLExpressionCS)dtParser.getSym(3)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 155:  collectionTypeCS ::= collectionTypeIdentifierCS ( typeCS )
 			//
 			case 155: {
 				
 				Object[] objs = (Object[])dtParser.getSym(1);
 				CSTNode result = createCollectionTypeCS(
 						(CollectionTypeIdentifierEnum)objs[1],
 						(TypeCS)dtParser.getSym(3)
 					);
 				setOffsets(result, (IToken)objs[0], getIToken(dtParser.getToken(4)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 156:  tupleTypeCS ::= Tuple ( variableListCSopt )
 			//
 			case 156: {
 				
 				CSTNode result = createTupleTypeCS((EList)dtParser.getSym(3));
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(4)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 157:  variableListCSopt ::= $Empty
 			//
 			case 157:
 				dtParser.setSym1(new BasicEList());
 				break;
  
 			//
 			// Rule 159:  variableListCS ::= variableCS
 			//
 			case 159: {
 				
 				EList result = new BasicEList();
 				result.add(dtParser.getSym(1));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 160:  variableListCS ::= variableListCS , variableCS
 			//
 			case 160: {
 				
 				EList result = (EList)dtParser.getSym(1);
 				result.add(dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 161:  variableListCS2 ::= variableCS2
 			//
 			case 161:
  
 			//
 			// Rule 162:  variableListCS2 ::= variableCS
 			//
 			case 162: {
 				
 				EList result = new BasicEList();
 				result.add(dtParser.getSym(1));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 163:  variableListCS2 ::= variableListCS2 , variableCS2
 			//
 			case 163:
  
 			//
 			// Rule 164:  variableListCS2 ::= variableListCS2 , variableCS
 			//
 			case 164: {
 				
 				EList result = (EList)dtParser.getSym(1);
 				result.add(dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 167:  featureCallExpCS ::= MINUS isMarkedPreCS ( argumentsCSopt )
 			//
 			case 167:
  
 			//
 			// Rule 168:  featureCallExpCS ::= not isMarkedPreCS ( argumentsCSopt )
 			//
 			case 168: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.IDENTIFIER_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createOperationCallExpCS(
 						simpleNameCS,
 						(IsMarkedPreCS)dtParser.getSym(2),
 						(EList)dtParser.getSym(4)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(5)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 169:  operationCallExpCS ::= simpleNameCS isMarkedPreCS ( argumentsCSopt )
 			//
 			case 169: {
 				
 				CSTNode result = createOperationCallExpCS(
 						(SimpleNameCS)dtParser.getSym(1),
 						(IsMarkedPreCS)dtParser.getSym(2),
 						(EList)dtParser.getSym(4)
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), getIToken(dtParser.getToken(5)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 170:  operationCallExpCS ::= oclIsUndefined isMarkedPreCS ( argumentsCSopt )
 			//
 			case 170:
  
 			//
 			// Rule 171:  operationCallExpCS ::= oclIsInvalid isMarkedPreCS ( argumentsCSopt )
 			//
 			case 171:
  
 			//
 			// Rule 172:  operationCallExpCS ::= oclIsNew isMarkedPreCS ( argumentsCSopt )
 			//
 			case 172:
  
 			//
 			// Rule 173:  operationCallExpCS ::= EQUAL isMarkedPreCS ( argumentsCSopt )
 			//
 			case 173:
  
 			//
 			// Rule 174:  operationCallExpCS ::= NOT_EQUAL isMarkedPreCS ( argumentsCSopt )
 			//
 			case 174:
  
 			//
 			// Rule 175:  operationCallExpCS ::= PLUS isMarkedPreCS ( argumentsCSopt )
 			//
 			case 175:
  
 			//
 			// Rule 176:  operationCallExpCS ::= MULTIPLY isMarkedPreCS ( argumentsCSopt )
 			//
 			case 176:
  
 			//
 			// Rule 177:  operationCallExpCS ::= DIVIDE isMarkedPreCS ( argumentsCSopt )
 			//
 			case 177:
  
 			//
 			// Rule 178:  operationCallExpCS ::= GREATER isMarkedPreCS ( argumentsCSopt )
 			//
 			case 178:
  
 			//
 			// Rule 179:  operationCallExpCS ::= LESS isMarkedPreCS ( argumentsCSopt )
 			//
 			case 179:
  
 			//
 			// Rule 180:  operationCallExpCS ::= GREATER_EQUAL isMarkedPreCS ( argumentsCSopt )
 			//
 			case 180:
  
 			//
 			// Rule 181:  operationCallExpCS ::= LESS_EQUAL isMarkedPreCS ( argumentsCSopt )
 			//
 			case 181:
  
 			//
 			// Rule 182:  operationCallExpCS ::= and isMarkedPreCS ( argumentsCSopt )
 			//
 			case 182:
  
 			//
 			// Rule 183:  operationCallExpCS ::= or isMarkedPreCS ( argumentsCSopt )
 			//
 			case 183:
  
 			//
 			// Rule 184:  operationCallExpCS ::= xor isMarkedPreCS ( argumentsCSopt )
 			//
 			case 184:
  
 			//
 			// Rule 185:  keywordOperationCallExpCS ::= keywordAsIdentifier isMarkedPreCS ( argumentsCSopt )
 			//
 			case 185: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.IDENTIFIER_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createOperationCallExpCS(
 						simpleNameCS,
 						(IsMarkedPreCS)dtParser.getSym(2),
 						(EList)dtParser.getSym(4)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(5)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 186:  operationCallExpCS ::= oclIsInState isMarkedPreCS ( pathNameCSOpt )
 			//
 			case 186: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 
 				PathNameCS pathNameCS = (PathNameCS) dtParser.getSym(4);
 				StateExpCS stateExpCS = createStateExpCS(pathNameCS);
 				setOffsets(stateExpCS, pathNameCS);
 			
 				CSTNode result = createOperationCallExpCS(
 						simpleNameCS,
 						(IsMarkedPreCS)dtParser.getSym(2),
 						stateExpCS
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(5)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 187:  attrOrNavCallExpCS ::= simpleNameCS isMarkedPreCS
 			//
 			case 187: {
 				
 				IsMarkedPreCS isMarkedPreCS = (IsMarkedPreCS)dtParser.getSym(2);
 				CSTNode result = createFeatureCallExpCS(
 						(SimpleNameCS)dtParser.getSym(1),
 						new BasicEList(),
 						isMarkedPreCS
 					);
 				if (isMarkedPreCS.isPre()) {
 					setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(2));
 				} else {
 					setOffsets(result, (CSTNode)dtParser.getSym(1));
 				}
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 188:  attrOrNavCallExpCS ::= keywordAsIdentifier isMarkedPreCS
 			//
 			case 188: {
 				
 				IsMarkedPreCS isMarkedPreCS = (IsMarkedPreCS)dtParser.getSym(2);
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.IDENTIFIER_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createFeatureCallExpCS(
 						simpleNameCS,
 						new BasicEList(),
 						isMarkedPreCS
 					);
 				if (isMarkedPreCS.isPre()) {
 					setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(2));
 				} else {
 					setOffsets(result, getIToken(dtParser.getToken(1)));
 				}
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 189:  isMarkedPreCS ::= $Empty
 			//
 			case 189: {
 				
 				CSTNode result = createIsMarkedPreCS(false);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 190:  isMarkedPreCS ::= @pre
 			//
 			case 190: {
 				
 				CSTNode result = createIsMarkedPreCS(true);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 191:  argumentsCSopt ::= $Empty
 			//
 			case 191:
 				dtParser.setSym1(new BasicEList());
 				break;
  
 			//
 			// Rule 193:  argumentsCS ::= oclExpressionCS
 			//
 			case 193: {
 				
 				EList result = new BasicEList();
 				result.add(dtParser.getSym(1));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 194:  argumentsCS ::= argumentsCS , oclExpressionCS
 			//
 			case 194: {
 				
 				EList result = (EList)dtParser.getSym(1);
 				result.add(dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 195:  letExpCS ::= let variableCS letExpSubCSopt in oclExpressionCS
 			//
 			case 195: {
 				
 				EList variables = (EList)dtParser.getSym(3);
 				variables.add(0, dtParser.getSym(2));
 				CSTNode result = createLetExpCS(
 						variables,
 						(OCLExpressionCS)dtParser.getSym(5)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(5));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 196:  letExpSubCSopt ::= $Empty
 			//
 			case 196:
 				dtParser.setSym1(new BasicEList());
 				break;
  
 			//
 			// Rule 198:  letExpSubCS ::= , variableCS
 			//
 			case 198: {
 				
 				EList result = new BasicEList();
 				result.add(dtParser.getSym(2));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 199:  letExpSubCS ::= letExpSubCS , variableCS
 			//
 			case 199: {
 				
 				EList result = (EList)dtParser.getSym(1);
 				result.add(dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 200:  messageExpCS ::= ^ simpleNameCS ( oclMessageArgumentsCSopt )
 			//
 			case 200:
  
 			//
 			// Rule 201:  messageExpCS ::= ^^ simpleNameCS ( oclMessageArgumentsCSopt )
 			//
 			case 201: {
 				
 				CSTNode result = createMessageExpCS(
 						getIToken(dtParser.getToken(1)).getKind() == XpandParsersym.TK_CARET,
 						(SimpleNameCS)dtParser.getSym(2),
 						(EList<OCLMessageArgCS>)dtParser.getSym(4)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(5)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 202:  oclMessageArgumentsCSopt ::= $Empty
 			//
 			case 202:
 				dtParser.setSym1(new BasicEList());
 				break;
  
 			//
 			// Rule 204:  oclMessageArgumentsCS ::= oclMessageArgCS
 			//
 			case 204: {
 				
 				EList result = new BasicEList();
 				result.add(dtParser.getSym(1));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 205:  oclMessageArgumentsCS ::= oclMessageArgumentsCS , oclMessageArgCS
 			//
 			case 205: {
 				
 				EList result = (EList)dtParser.getSym(1);
 				result.add(dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 206:  oclMessageArgCS ::= oclExpressionCS
 			//
 			case 206: {
 				
 				CSTNode result = createOCLMessageArgCS(
 						null,
 						(OCLExpressionCS)dtParser.getSym(1)
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 207:  oclMessageArgCS ::= ?
 			//
 			case 207: {
 				
 				CSTNode result = createOCLMessageArgCS(
 						null,
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 208:  oclMessageArgCS ::= ? : typeCS
 			//
 			case 208: {
 				
 				CSTNode result = createOCLMessageArgCS(
 						(TypeCS)dtParser.getSym(3),
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 209:  declarator ::= IDENTIFIER : typeCS
 			//
 			case 209: {
 				
 				CSTNode result = createVariableCS(
 						getTokenText(dtParser.getToken(1)),
 						(TypeCS)dtParser.getSym(3),
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 210:  declarator ::= IDENTIFIER : typeCS = oclExpressionCS
 			//
 			case 210: {
 				
 				CSTNode result = createVariableCS(
 						getTokenText(dtParser.getToken(1)),
 						(TypeCS)dtParser.getSym(3),
 						(OCLExpressionCS)dtParser.getSym(5)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(5));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 211:  declarator ::= IDENTIFIER : typeCS := oclExpressionCS
 			//
 			case 211: {
 				
 				CSTNode result = createVariableCS(
 						getTokenText(dtParser.getToken(1)),
 						(TypeCS)dtParser.getSym(3),
 						(OCLExpressionCS)dtParser.getSym(5)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(5));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 212:  declarator ::= IDENTIFIER := oclExpressionCS
 			//
 			case 212: {
 				
 				CSTNode result = createVariableCS(
 						getTokenText(dtParser.getToken(1)),
 						null,
 						(OCLExpressionCS)dtParser.getSym(3)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 214:  letExpSubCS3 ::= variableCS2
 			//
 			case 214: {
 				
 				EList result = new BasicEList();
 				result.add(dtParser.getSym(1));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 215:  letExpSubCS3 ::= letExpSubCS3 , variableCS2
 			//
 			case 215: {
 				
 				EList result = (EList)dtParser.getSym(1);
 				result.add(dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 216:  letExpCS ::= let letExpSubCS3 in oclExpressionCS
 			//
 			case 216: {
 				
 				EList variables = (EList)dtParser.getSym(2);
 				CSTNode result = createLetExpCS(
 						variables,
 						(OCLExpressionCS)dtParser.getSym(4)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(4));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 217:  letExpCS ::= let letExpSubCS3 in qvtErrorToken
 			//
 			case 217: {
 				
 				EList variables = (EList)dtParser.getSym(2);
 				CSTNode result = createLetExpCS(
 						variables,
 						createSimpleNameCS(SimpleTypeEnum.IDENTIFIER_LITERAL, "") //$NON-NLS-1$
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 219:  qvtStringLiteralExpCS ::= QUOTE_STRING_LITERAL
 			//
 			case 219: {
 				
 				CSTNode result = createStringLiteralExpCS("'" + unquote(getTokenText(dtParser.getToken(1))) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 222:  qvtErrorToken ::= ERROR_TOKEN
 			//
 			case 222: {
 				
 				diagnozeErrorToken(dtParser.getToken(1));
 	  		  break;
 			}
 	 
 			//
 			// Rule 223:  iterContents ::= variableCS | qvtErrorToken
 			//
 			case 223: {
 				
 				CSTNode fakeCS = createSimpleNameCS(SimpleTypeEnum.IDENTIFIER_LITERAL, ""); //$NON-NLS-1$
 				setOffsets(fakeCS, getIToken(dtParser.getToken(3)));
 				dtParser.setSym1(new Object[] {
 						dtParser.getSym(1),
 						null,
 						fakeCS
 					});
 	  		  break;
 			}
 	 
 			//
 			// Rule 224:  callExpCS ::= . qvtErrorToken
 			//
 			case 224: {
 				
 				CallExpCS result = TempFactory.eINSTANCE.createErrorCallExpCS();
 	 			result.setAccessor(DotOrArrowEnum.DOT_LITERAL);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 225:  callExpCS ::= -> qvtErrorToken
 			//
 			case 225: {
 				
 				CallExpCS result = TempFactory.eINSTANCE.createErrorCallExpCS();
 	 			result.setAccessor(DotOrArrowEnum.ARROW_LITERAL);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 226:  argumentsCS ::= qvtErrorToken
 			//
 			case 226:
 				dtParser.setSym1(new BasicEList());
 				break;
  
 			//
 			// Rule 229:  ifExpCS ::= if oclExpressionCS then ifExpBodyCS else ifExpBodyCS endif
 			//
 			case 229: {
 				
 				CSTNode result = createIfExpCS(
 						(OCLExpressionCS)dtParser.getSym(2),
 						(OCLExpressionCS)dtParser.getSym(4),
 						(OCLExpressionCS)dtParser.getSym(6)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(7)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 230:  ifExpCS ::= if oclExpressionCS then ifExpBodyCS endif
 			//
 			case 230: {
 				
 				CSTNode result = createIfExpCS(
 						(OCLExpressionCS)dtParser.getSym(2),
 						(OCLExpressionCS)dtParser.getSym(4),
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(5)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 231:  ifExpCS ::= if oclExpressionCS then ifExpBodyCS else ifExpBodyCS qvtErrorToken
 			//
 			case 231: {
 				
 				CSTNode result = createIfExpCS(
 						(OCLExpressionCS)dtParser.getSym(2),
 						(OCLExpressionCS)dtParser.getSym(4),
 						(OCLExpressionCS)dtParser.getSym(6)
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(6));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 232:  ifExpCS ::= if oclExpressionCS then ifExpBodyCS else qvtErrorToken
 			//
 			case 232: {
 				
 				CSTNode result = createIfExpCS(
 						(OCLExpressionCS)dtParser.getSym(2),
 						(OCLExpressionCS)dtParser.getSym(4),
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(5)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 233:  ifExpCS ::= if oclExpressionCS then ifExpBodyCS qvtErrorToken
 			//
 			case 233: {
 				
 				CSTNode result = createIfExpCS(
 						(OCLExpressionCS)dtParser.getSym(2),
 						(OCLExpressionCS)dtParser.getSym(4),
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(4));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 234:  ifExpCS ::= if oclExpressionCS then qvtErrorToken
 			//
 			case 234: {
 				
 				CSTNode result = createIfExpCS(
 						(OCLExpressionCS)dtParser.getSym(2),
 						null,
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(3)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 235:  ifExpCS ::= if oclExpressionCS qvtErrorToken
 			//
 			case 235: {
 				
 				CSTNode result = createIfExpCS(
 						(OCLExpressionCS)dtParser.getSym(2),
 						null,
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode)dtParser.getSym(2));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 236:  ifExpCS ::= if qvtErrorToken
 			//
 			case 236: {
 				
 				CSTNode result = createIfExpCS(
 						null,
 						null,
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 248:  iteratorExpCS ::= iteratorExpCSToken ( iterContents )
 			//
 			case 248: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				Object[] iterContents = (Object[])dtParser.getSym(3);
 				CSTNode result = createIteratorExpCS(
 						simpleNameCS,
 						(VariableCS)iterContents[0],
 						(VariableCS)iterContents[1],
 						(OCLExpressionCS)iterContents[2]
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(4)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 249:  iteratorExpCS ::= iteratorExpCSToken ( iterContents qvtErrorToken
 			//
 			case 249: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				Object[] iterContents = (Object[])dtParser.getSym(3);
 				CSTNode result = createIteratorExpCS(
 						simpleNameCS,
 						(VariableCS)iterContents[0],
 						(VariableCS)iterContents[1],
 						(OCLExpressionCS)iterContents[2]
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(2)));
 		        for (int i = iterContents.length - 1; i >= 0; i--) {
 		        	if (iterContents[i] instanceof CSTNode) {
 		        		setOffsets(result, getIToken(dtParser.getToken(1)), (CSTNode) iterContents[i]);
 		        		break;
 		        	}
 		        }
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 250:  iteratorExpCS ::= iteratorExpCSToken ( qvtErrorToken
 			//
 			case 250: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createIteratorExpCS(
 						simpleNameCS,
 						null,
 						null,
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(2)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 251:  operationCallExpCS ::= oclAsType isMarkedPreCS ( typeCS )
 			//
 			case 251:
  
 			//
 			// Rule 252:  operationCallExpCS ::= oclIsKindOf isMarkedPreCS ( typeCS )
 			//
 			case 252:
  
 			//
 			// Rule 253:  operationCallExpCS ::= oclIsTypeOf isMarkedPreCS ( typeCS )
 			//
 			case 253: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.IDENTIFIER_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				EList params = new BasicEList();
 				params.add(dtParser.getSym(4));
 				CSTNode result = createOperationCallExpCS(
 						simpleNameCS,
 						(IsMarkedPreCS)dtParser.getSym(2),
 						params
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(5)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 263:  imperativeIterateExpCS ::= imperativeIteratorExpCSToken12 ( imperativeIterContents12 )
 			//
 			case 263:
  
 			//
 			// Rule 264:  imperativeIterateExpCS ::= imperativeIteratorExpCSToken3 ( imperativeIterContents3 )
 			//
 			case 264: {
 				
 				String opCode = getTokenText(dtParser.getToken(1));
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							opCode
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				Object[] iterContents = (Object[]) dtParser.getSym(3);
 				OCLExpressionCS bodyCS = null;
 				OCLExpressionCS conditionCS = null;
 				if ("xcollect".equals(opCode) || "collectOne".equals(opCode)) { //$NON-NLS-1$ //$NON-NLS-2$ 
 				    bodyCS = (OCLExpressionCS) iterContents[2];
 				} else {
 				    conditionCS = (OCLExpressionCS) iterContents[2];
 				}
 				CSTNode result = createImperativeIterateExpCS(
 						simpleNameCS,
 						(EList<VariableCS>)iterContents[0],
 						(VariableCS)iterContents[1],
 						bodyCS,
 						conditionCS
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(4)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 265:  imperativeIterateExpCS ::= imperativeIteratorExpCSToken qvtErrorToken
 			//
 			case 265: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							getTokenText(dtParser.getToken(1))
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(1)));
 				CSTNode result = createImperativeIterateExpCS(
 						simpleNameCS,
 						ourEmptyEList,
 						null,
 						null,
 						null
 					);
 				setOffsets(result, getIToken(dtParser.getToken(1)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 266:  imperativeIterContents12 ::= oclExpressionCS
 			//
 			case 266: {
 				
 				dtParser.setSym1(new Object[] {
 						ourEmptyEList,
 						null,
 						dtParser.getSym(1)
 					});
 	  		  break;
 			}
 	 
 			//
 			// Rule 267:  imperativeIterContents12 ::= variableListCS | oclExpressionCS
 			//
 			case 267: {
 				
 				dtParser.setSym1(new Object[] {
 						dtParser.getSym(1),
 						null,
 						dtParser.getSym(3)
 					});
 	  		  break;
 			}
 	 
 			//
 			// Rule 268:  imperativeIterContents3 ::= variableListCS ; variableCS2 | oclExpressionCS
 			//
 			case 268: {
 				
 				dtParser.setSym1(new Object[] {
 						dtParser.getSym(1),
 						dtParser.getSym(3),
 						dtParser.getSym(5)
 					});
 	  		  break;
 			}
 	 
 			//
 			// Rule 269:  exclamationOpt ::= $Empty
 			//
 			case 269:
 				dtParser.setSym1(null);
 				break;
  
 			//
 			// Rule 271:  declarator_vsep ::= IDENTIFIER |
 			//
 			case 271: {
 				
 		CSTNode result = createVariableCS(
 					getTokenText(dtParser.getToken(1)),
                                             null,
 					null
 					);
                     setOffsets(result, getIToken(dtParser.getToken(1)));
                     dtParser.setSym1(result);
           		  break;
 			}
     	 
 			//
 			// Rule 272:  declarator_vsepOpt ::= $Empty
 			//
 			case 272:
 				dtParser.setSym1(null);
 				break;
  
 			//
 			// Rule 274:  callExpCS ::= -> featureCallExpCS exclamationOpt [ declarator_vsepOpt oclExpressionCS ]
 			//
 			case 274: {
 				
 	        String opCode = isTokenOfType(getIToken(dtParser.getToken(3)), XpandParsersym.TK_EXCLAMATION_MARK) ?  "collectselectOne" : "collectselect"; //$NON-NLS-1$ //$NON-NLS-2$ 
 		SimpleNameCS simpleNameCS = createSimpleNameCS(
 				SimpleTypeEnum.KEYWORD_LITERAL,
 				opCode
 				);
 		setOffsets(simpleNameCS, getIToken(dtParser.getToken(4)), getIToken(dtParser.getToken(7)));
 		VariableCS variableCS = (VariableCS) dtParser.getSym(5);
 		CSTNode result = createImperativeIterateExpCS(
 					simpleNameCS,
 					ourEmptyEList,
 					variableCS,
 					(OCLExpressionCS) dtParser.getSym(2),
 					(OCLExpressionCS) dtParser.getSym(6)
 				);
 		setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(7)));
 		dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 275:  oclExpCS ::= oclExpCS exclamationOpt [ oclExpressionCS ]
 			//
 			case 275: {
 				
 			        String opCode = isTokenOfType(getIToken(dtParser.getToken(2)), XpandParsersym.TK_EXCLAMATION_MARK) ?  "selectOne" : "xselect"; //$NON-NLS-1$ //$NON-NLS-2$ 
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							opCode
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(3)), getIToken(dtParser.getToken(5)));
 				CallExpCS result = createImperativeIterateExpCS(
 						simpleNameCS,
 						ourEmptyEList,
 						null,
 						null,
 						(OCLExpressionCS) dtParser.getSym(4)
 					);
 				result.setSource((OCLExpressionCS)dtParser.getSym(1));
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(5)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 276:  dotArrowExpCS ::= dotArrowExpCS . featureCallExpCS exclamationOpt [ oclExpressionCS ]
 			//
 			case 276: {
 				
 				CallExpCS callExpCS = (CallExpCS)dtParser.getSym(3);
 				callExpCS.setSource((OCLExpressionCS)dtParser.getSym(1));
 				callExpCS.setAccessor(DotOrArrowEnum.DOT_LITERAL);
 				setOffsets(callExpCS, (CSTNode)dtParser.getSym(1), callExpCS);
 
 
 			        String opCode = isTokenOfType(getIToken(dtParser.getToken(4)), XpandParsersym.TK_EXCLAMATION_MARK) ?  "selectOne" : "xselect"; //$NON-NLS-1$ //$NON-NLS-2$ 
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.KEYWORD_LITERAL,
 							opCode
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(5)), getIToken(dtParser.getToken(7)));
 				CallExpCS result = createImperativeIterateExpCS(
 						simpleNameCS,
 						ourEmptyEList,
 						null,
 						null,
 						(OCLExpressionCS) dtParser.getSym(6)
 					);
 				result.setSource(callExpCS);
 				setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(7)));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 277:  equalityExpCS ::= equalityExpCS != relationalExpCS
 			//
 			case 277:
  
 			//
 			// Rule 278:  equalityWithLet ::= equalityExpCS != relationalWithLet
 			//
 			case 278: {
 				
 				SimpleNameCS simpleNameCS = createSimpleNameCS(
 							SimpleTypeEnum.STRING_LITERAL,
 							OCLStandardLibraryUtil.getOperationName(PredefinedType.NOT_EQUAL)
 						);
 				setOffsets(simpleNameCS, getIToken(dtParser.getToken(2)));
 				EList args = new BasicEList();
 				args.add(dtParser.getSym(3));
 				CSTNode result = createOperationCallExpCS(
 						(OCLExpressionCS)dtParser.getSym(1),
 						simpleNameCS,
 						args
 					);
 				setOffsets(result, (CSTNode)dtParser.getSym(1), (CSTNode)dtParser.getSym(3));
 				dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 280:  newExpCS ::= new pathNameCS ( argumentsCSopt )
 			//
 			case 280: {
 				
 			OCLExpressionCS result = createNewRuleCallExpCS((PathNameCS)dtParser.getSym(2),(EList)dtParser.getSym(4));
 			setOffsets(result, getIToken(dtParser.getToken(1)), getIToken(dtParser.getToken(5)));
 			dtParser.setSym1(result);
 	  		  break;
 			}
 	 
 			//
 			// Rule 281:  template ::= emptyTemplate
 			//
 			case 281: {
 				
 		setResult(xpandFactory.createTemplate(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, getRightIToken()));
 			  break;
 			} 
 			//
 			// Rule 284:  template ::= LG commentTextPairAny imports extensionImports defineOrAroundSeq
 			//
 			case 284: {
 				
 		List imports = (List) getRhsSym(3);
 		List extensionImports = (List) getRhsSym(4);
 		List defineOrAround = (List) getRhsSym(5);
 		List<Advice> advices = new LinkedList<Advice>();
 		List<Definition> defines = new LinkedList<Definition>();
 		for (Object o : defineOrAround) {
 			if (o instanceof Definition) {
 				defines.add((Definition) o);
 			} else if (o instanceof Advice) {
 				advices.add((Advice) o);
 			} else {
 				throw new IllegalStateException();// assert false?
 			}
 		}
 		setResult(xpandFactory.createTemplate(imports, extensionImports, defines, advices, getRightIToken()));
 			  break;
 			} 
 			//
 			// Rule 285:  defineOrAroundSeq ::= define TEXT commentTextPairAny defineOrAroundSuffix
 			//
 			case 285: {
 				
 		List result = new LinkedList();
 		result.add(getRhsSym(1));
 		result.addAll((List) getRhsSym(4));
 		setResult(result);
 			  break;
 			} 
 			//
 			// Rule 286:  defineOrAroundSeq ::= around TEXT commentTextPairAny defineOrAroundSuffix
 			//
 			case 286: {
 				
 		List result = new LinkedList();
 		result.add(getRhsSym(1));
 		result.addAll((List) getRhsSym(4));
 		setResult(result);
 			  break;
 			} 
 			//
 			// Rule 287:  defineOrAroundSuffix ::= $Empty
 			//
 			case 287: {
 				
 		setResult(Collections.EMPTY_LIST);
 			  break;
 			} 
 			//
 			// Rule 293:  imports ::= $Empty
 			//
 			case 293: {
 				
 		setResult(Collections.EMPTY_LIST);
 			  break;
 			} 
 			//
 			// Rule 294:  imports ::= anImport imports
 			//
 			case 294: {
 				
 		List res = new LinkedList();
 		res.add(getRhsSym(1));
 		res.addAll((List) getRhsSym(2));
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 295:  anImport ::= IMPORT qvtStringLiteralExpCS TEXT commentTextPairAny
 			//
 			case 295: {
 				
 		setResult(xpandFactory.createNamespaceImport(getLeftIToken(), (StringLiteralExpCS) getRhsSym(2)));
 			  break;
 			} 
 			//
 			// Rule 296:  extensionImports ::= $Empty
 			//
 			case 296: {
 				
 		setResult(Collections.EMPTY_LIST);
 			  break;
 			} 
 			//
 			// Rule 297:  extensionImports ::= anExtensionImport extensionImports
 			//
 			case 297: {
 				
 		List res = new LinkedList();
 		res.add(getRhsSym(1));
 		res.addAll((List) getRhsSym(2));
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 298:  anExtensionImport ::= EXTENSION pathNameCS TEXT commentTextPairAny
 			//
 			case 298: {
 				
 		setResult(xpandFactory.createImportDeclaration(getLeftIToken(), (PathNameCS) getRhsSym(2)));
 			  break;
 			} 
 			//
 			// Rule 299:  around ::= AROUND pointcut FOR typeCS sequence ENDAROUND
 			//
 			case 299: {
 				
 		setResult(xpandFactory.createAround(getLeftIToken(), getRightIToken(), (Identifier) getRhsSym(2), Collections.<VariableCS>emptyList(), false, (TypeCS) getRhsSym(4), (List) getRhsSym(5)));
 			  break;
 			} 
 			//
 			// Rule 300:  around ::= AROUND pointcut LPAREN parametersList RPAREN FOR typeCS sequence ENDAROUND
 			//
 			case 300: {
 				
 		setResult(xpandFactory.createAround(getLeftIToken(), getRightIToken(), (Identifier) getRhsSym(2), (List<VariableCS>) getRhsSym(4), false, (TypeCS) getRhsSym(7), (List) getRhsSym(8)));
 			  break;
 			} 
 			//
 			// Rule 301:  around ::= AROUND pointcut LPAREN parametersList COMMA MULTIPLY RPAREN FOR typeCS sequence ENDAROUND
 			//
 			case 301: {
 				
 		setResult(xpandFactory.createAround(getLeftIToken(), getRightIToken(), (Identifier) getRhsSym(2), (List<VariableCS>) getRhsSym(4), true, (TypeCS) getRhsSym(9), (List) getRhsSym(10)));
 			  break;
 			} 
 			//
 			// Rule 302:  around ::= AROUND pointcut LPAREN MULTIPLY RPAREN FOR typeCS sequence ENDAROUND
 			//
 			case 302: {
 				
 		setResult(xpandFactory.createAround(getLeftIToken(), getRightIToken(), (Identifier) getRhsSym(2), Collections.<VariableCS>emptyList(), true, (TypeCS) getRhsSym(7), (List) getRhsSym(8)));
 			  break;
 			} 
 			//
 			// Rule 303:  pointcut ::= MULTIPLY pointcutSuffix
 			//
 			case 303: {
 				
 //			FIXME: may use SimpleNameCS here, though need more sophisticated code to update end position
 //			SimpleNameCS simpleNameCS = createSimpleNameCS(SimpleTypeEnum.IDENTIFIER_LITERAL, getTokenText(getRhsTokenIndex(1)));
 //			setOffsets(simpleNameCS, getLeftIToken());
 		Identifier res = xpandFactory.createIdentifier(getLeftIToken());
 		if (getRhsSym(2) != null) {
 			res = res.append((Identifier) getRhsSym(2));
 		}
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 304:  pointcut ::= IDENTIFIER pointcutSuffix
 			//
 			case 304: {
 				
 		Identifier res = xpandFactory.createIdentifier(getLeftIToken());
 		if (getRhsSym(2) != null) {
 			res = res.append((Identifier) getRhsSym(2));
 		}
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 305:  pointcutSuffix ::= $Empty
 			//
 			case 305: {
 				
 		setResult(null);
 			  break;
 			} 
 			//
 			// Rule 307:  pointcutSuffix ::= COLONCOLON pointcutSuffix
 			//
 			case 307: {
 				
 		Identifier res = xpandFactory.createIdentifier(getLeftIToken());
 		if (getRhsSym(2) != null) {
 			res = res.append((Identifier) getRhsSym(2));
 		}
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 308:  define ::= DEFINE IDENTIFIER FOR typeCS sequence ENDDEFINE
 			//
 			case 308: {
 				
 		setResult(xpandFactory.createDefinition(getLeftIToken(), getRightIToken(), getRhsIToken(2), Collections.<VariableCS>emptyList(), (TypeCS) getRhsSym(4), (List) getRhsSym(5)));
 			  break;
 			} 
 			//
 			// Rule 309:  define ::= DEFINE IDENTIFIER LPAREN parametersList RPAREN FOR typeCS sequence ENDDEFINE
 			//
 			case 309: {
 				
 		setResult(xpandFactory.createDefinition(getLeftIToken(), getRightIToken(), getRhsIToken(2), (List<VariableCS>) getRhsSym(4), (TypeCS) getRhsSym(7), (List) getRhsSym(8)));
 			  break;
 			} 
 			//
 			// Rule 310:  parametersList ::= parameter
 			//
 			case 310: {
 				
 		VariableCS param = (VariableCS) getRhsSym(1);
 		LinkedList res = new LinkedList();
 		res.add(param);
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 311:  parametersList ::= parametersList , parameter
 			//
 			case 311: {
 				
 		VariableCS param = (VariableCS) getRhsSym(3);
 		LinkedList res = new LinkedList();
 		res.addAll((List) getRhsSym(1));
 		res.add(param);
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 313:  parameter ::= typeCS IDENTIFIER
 			//
 			case 313: {
 				
 		VariableCS result = createVariableCS(getRhsIToken(2).toString(), (TypeCS) getRhsSym(1), null);
 		setOffsets(result, (TypeCS) getRhsSym(1), getRhsIToken(2));
 		setResult(result);
 			  break;
 			} 
 			//
 			// Rule 314:  sequence ::= text sequenceSuffix
 			//
 			case 314: {
 				
 		List res = new LinkedList();
 		res.addAll((List) getRhsSym(1));
 		res.addAll((List) getRhsSym(2));
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 315:  sequenceSuffix ::= $Empty
 			//
 			case 315: {
 				
 		setResult(Collections.EMPTY_LIST);
 			  break;
 			} 
 			//
 			// Rule 316:  sequenceSuffix ::= statement text sequenceSuffix
 			//
 			case 316: {
 				
 		List res = new LinkedList();
 		res.add(getRhsSym(1));
 		res.addAll((List) getRhsSym(2));
 		res.addAll((List) getRhsSym(3));
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 323:  text ::= minusOpt TEXT textSuffix
 			//
 			case 323: {
 				
 		List res = new LinkedList();
 		res.add(xpandFactory.createTextStatement(getRhsIToken(2), (IToken) getRhsSym(1)));
 		res.addAll((List) getRhsSym(3));
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 324:  textSuffix ::= $Empty
 			//
 			case 324: {
 				
 		setResult(Collections.EMPTY_LIST);
 			  break;
 			} 
 			//
 			// Rule 325:  textSuffix ::= minusOpt TEXT textSuffix
 			//
 			case 325: {
 				
 		List res = new LinkedList();
 		res.add(xpandFactory.createTextStatement(getRhsIToken(2), (IToken) getRhsSym(1)));
 		res.addAll((List) getRhsSym(3));
 		setResult(res);
 			  break;
 			} 
 			//
 			// Rule 326:  minusOpt ::= $Empty
 			//
 			case 326: {
 				
 		setResult(null);
 			  break;
 			} 
 			//
 			// Rule 327:  minusOpt ::= MINUS
 			//
 			case 327: {
 				
 		setResult(getLeftIToken());
 			  break;
 			} 
 			//
 			// Rule 331:  errorStatement ::= ERROR oclExpressionCS
 			//
 			case 331: {
 				
 		setResult(xpandFactory.createErrorStatement(getLeftIToken(), (OCLExpressionCS) getRhsSym(2)));
 			  break;
 			} 
 			//
 			// Rule 332:  expandStatement ::= EXPAND definitionName parameterListOpt
 			//
 			case 332: {
 				
 		setResult(xpandFactory.createExpandStatement(getLeftIToken(), (PathNameCS) getRhsSym(2), (List) getRhsSym(3), null, false, null));
 			  break;
 			} 
 			//
 			// Rule 333:  expandStatement ::= EXPAND definitionName parameterListOpt FOR oclExpressionCS
 			//
 			case 333: {
 				
 		setResult(xpandFactory.createExpandStatement(getLeftIToken(), (PathNameCS) getRhsSym(2), (List) getRhsSym(3), (OCLExpressionCS) getRhsSym(5), false, null));
 			  break;
 			} 
 			//
 			// Rule 334:  expandStatement ::= EXPAND definitionName parameterListOpt FOREACH oclExpressionCS separatorOpt
 			//
 			case 334: {
 				
 		setResult(xpandFactory.createExpandStatement(getLeftIToken(), (PathNameCS) getRhsSym(2), (List) getRhsSym(3), (OCLExpressionCS) getRhsSym(5), true, (OCLExpressionCS) getRhsSym(6)));
 			  break;
 			} 
 			//
 			// Rule 335:  parameterListOpt ::= $Empty
 			//
 			case 335: {
 				
 		setResult(Collections.EMPTY_LIST);
 			  break;
 			} 
 			//
 			// Rule 336:  parameterListOpt ::= LPAREN argumentsCS RPAREN
 			//
 			case 336: {
 				
 		setResult(getRhsSym(2));
 			  break;
 			} 
 			//
 			// Rule 338:  expressionStmt ::= oclExpressionCS
 			//
 			case 338: {
 				
 		// XXX OCL CST doesn't keep track of line numbers, but we use them (perhaps, might refactor to stop using?)
 		int lineNumber = getLeftIToken().getLine();
 		setResult(xpandFactory.createExpressionStatement((OCLExpressionCS) getRhsSym(1), lineNumber));
 			  break;
 			} 
 			//
 			// Rule 339:  fileStatement ::= FILE oclExpressionCS identOpt sequence ENDFILE
 			//
 			case 339: {
 				
 		setResult(xpandFactory.createFileStatement(getLeftIToken(), getRightIToken(), (OCLExpressionCS) getRhsSym(2), (Identifier) getRhsSym(3), (List) getRhsSym(4)));
 			  break;
 			} 
 			//
 			// Rule 340:  identOpt ::= $Empty
 			//
 			case 340: {
 				
 		setResult(null);
 			  break;
 			} 
 			//
 			// Rule 341:  identOpt ::= IDENTIFIER
 			//
 			case 341: {
 				
 		setResult(xpandFactory.createIdentifier(getLeftIToken()));
 			  break;
 			} 
 			//
 			// Rule 342:  foreachStatement ::= FOREACH oclExpressionCS AS IDENTIFIER iteratorOpt separatorOpt sequence ENDFOREACH
 			//
 			case 342: {
 				
 		setResult(xpandFactory.createForEachStatement(getLeftIToken(), getRightIToken(), (OCLExpressionCS) getRhsSym(2), getRhsIToken(4), (OCLExpressionCS) getRhsSym(6), (IToken) getRhsSym(5), (List) getRhsSym(7)));
 			  break;
 			} 
 			//
 			// Rule 343:  iteratorOpt ::= $Empty
 			//
 			case 343: {
 				
 		setResult(null);
 			  break;
 			} 
 			//
 			// Rule 344:  iteratorOpt ::= ITERATOR IDENTIFIER
 			//
 			case 344: {
 				
 		setResult(getRightIToken());
 			  break;
 			} 
 			//
 			// Rule 345:  separatorOpt ::= $Empty
 			//
 			case 345: {
 				
 		setResult(null);
 			  break;
 			} 
 			//
 			// Rule 346:  separatorOpt ::= SEPARATOR oclExpressionCS
 			//
 			case 346: {
 				
 		setResult(getRhsSym(2));
 			  break;
 			} 
 			//
 			// Rule 347:  ifStatement ::= IF oclExpressionCS sequence elseifAny elseOpt ENDIF
 			//
 			case 347: {
 				
 		IfStatement i = xpandFactory.createIfStatement(getLeftIToken(), (OCLExpressionCS) getRhsSym(2), (List) getRhsSym(3), null);
 		IfStatement elseIf = (IfStatement) getRhsSym(4);
 		IfStatement elseStmt = (IfStatement) getRhsSym(5);
 		if (elseIf != null) {
 			i.setElseIf(elseIf);
 			IfStatement curElseIf = elseIf;
 			// get the latest one in the chain
 			while (curElseIf.getElseIf() != null) {
 				curElseIf = curElseIf.getElseIf();
 			}
 			curElseIf.setElseIf(elseStmt);
 		} else {
 			i.setElseIf(elseStmt);
 		}
 		setResult(i);
 			  break;
 			} 
 			//
 			// Rule 348:  elseifAny ::= $Empty
 			//
 			case 348: {
 				
 		setResult(null);
 			  break;
 			} 
 			//
 			// Rule 349:  elseifAny ::= ELSEIF oclExpressionCS sequence elseifAny
 			//
 			case 349: {
 				
 		IfStatement elseIf = xpandFactory.createIfStatement(getLeftIToken(), (OCLExpressionCS) getRhsSym(2), (List) getRhsSym(3), null);
 		IfStatement restElseIf = (IfStatement) getRhsSym(4);
 		elseIf.setElseIf(restElseIf);
 		setResult(elseIf);
 			  break;
 			} 
 			//
 			// Rule 350:  elseOpt ::= $Empty
 			//
 			case 350: {
 				
 		setResult(null);
 			  break;
 			} 
 			//
 			// Rule 351:  elseOpt ::= ELSE sequence
 			//
 			case 351: {
 				
 		setResult(xpandFactory.createIfStatement(getLeftIToken(), null, (List) getRhsSym(2), null));
 			  break;
 			} 
 			//
 			// Rule 352:  letStatement ::= LET oclExpressionCS AS IDENTIFIER sequence ENDLET
 			//
 			case 352: {
 				
 		setResult(xpandFactory.createLetStatement(getLeftIToken(), getRightIToken(), (OCLExpressionCS) getRhsSym(2), getRhsIToken(4), (List) getRhsSym(5)));
 			  break;
 			} 
 			//
 			// Rule 353:  protectStatement ::= PROTECT CSTART oclExpressionCS CEND oclExpressionCS ID oclExpressionCS disabledOpt sequence ENDPROTECT
 			//
 			case 353: {
 				
 		setResult(xpandFactory.createProtectStatement(getLeftIToken(), getRightIToken(), (OCLExpressionCS) getRhsSym(3), (OCLExpressionCS) getRhsSym(5), (OCLExpressionCS) getRhsSym(7), (IToken) getRhsSym(8), (List) getRhsSym(9)));
 			  break;
 			} 
 			//
 			// Rule 354:  disabledOpt ::= $Empty
 			//
 			case 354: {
 				
 		setResult(null);
 			  break;
 			} 
 			//
 			// Rule 355:  disabledOpt ::= DISABLE
 			//
 			case 355: {
 				
 		setResult(getLeftIToken());
 			  break;
 			}
 	
 			default:
 				break;
 		}
 		return;
 	}
 }
 
