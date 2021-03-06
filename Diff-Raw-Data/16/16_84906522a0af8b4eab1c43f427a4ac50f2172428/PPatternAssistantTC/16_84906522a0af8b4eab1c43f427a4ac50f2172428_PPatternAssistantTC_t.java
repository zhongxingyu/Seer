 package org.overture.typechecker.assistant.pattern;
 
 import java.util.List;
 import java.util.Vector;
 
 import org.overture.ast.analysis.AnalysisException;
 import org.overture.ast.analysis.QuestionAnswerAdaptor;
 import org.overture.ast.assistant.pattern.ASetPatternAssistant;
 import org.overture.ast.assistant.pattern.PPatternAssistant;
 import org.overture.ast.definitions.PDefinition;
 import org.overture.ast.expressions.PExp;
 import org.overture.ast.patterns.ABooleanPattern;
 import org.overture.ast.patterns.ACharacterPattern;
 import org.overture.ast.patterns.AConcatenationPattern;
 import org.overture.ast.patterns.AExpressionPattern;
 import org.overture.ast.patterns.AIdentifierPattern;
 import org.overture.ast.patterns.AIgnorePattern;
 import org.overture.ast.patterns.AIntegerPattern;
 import org.overture.ast.patterns.AMapPattern;
 import org.overture.ast.patterns.AMapUnionPattern;
 import org.overture.ast.patterns.ANilPattern;
 import org.overture.ast.patterns.AQuotePattern;
 import org.overture.ast.patterns.ARealPattern;
 import org.overture.ast.patterns.ARecordPattern;
 import org.overture.ast.patterns.ASeqPattern;
 import org.overture.ast.patterns.ASetPattern;
 import org.overture.ast.patterns.AStringPattern;
 import org.overture.ast.patterns.ATuplePattern;
 import org.overture.ast.patterns.AUnionPattern;
 import org.overture.ast.patterns.PPattern;
 import org.overture.ast.typechecker.NameScope;
 import org.overture.ast.types.PType;
 import org.overture.typechecker.TypeCheckInfo;
 import org.overture.typechecker.TypeComparator;
 import org.overture.typechecker.assistant.definition.PDefinitionSet;
 
 public class PPatternAssistantTC extends PPatternAssistant
 {
 
 	/**
 	 * Get a set of definitions for the pattern's variables. Note that if the
 	 * pattern includes duplicate variable names, these are collapse into one.
 	 */
 	public static List<PDefinition> getDefinitions(PPattern rp, PType ptype,
 			NameScope scope)
 	{
 		PDefinitionSet set = new PDefinitionSet();
 		set.addAll(getAllDefinitions(rp,ptype, scope));
 		List<PDefinition> result = new Vector<PDefinition>(set);
 		return result;
 	}
 
 	
 	
 	/**
 	 * Get a complete list of all definitions, including duplicates. This method should only be used 
 	 * only by PP
 	 */
 	private static List<PDefinition> getAllDefinitions(PPattern rp, PType ptype,
 			NameScope scope)
 	{
 		switch (rp.kindPPattern())
 		{
 			case IDENTIFIER:
 				return AIdentifierPatternAssistantTC.getAllDefinitions((AIdentifierPattern) rp,ptype,scope);
 			case BOOLEAN:
 			case CHARACTER:
 			case EXPRESSION:
 			case IGNORE:
 			case INTEGER:
 			case NIL:
 			case QUOTE:
 			case REAL:
 			case STRING:
 				return new Vector<PDefinition>();
 			case CONCATENATION:
 				return AConcatenationPatternAssistantTC.getAllDefinitions((AConcatenationPattern) rp, ptype, scope);
 			case RECORD:
 				return ARecordPatternAssistantTC.getAllDefinitions((ARecordPattern) rp, ptype, scope);
 			case SEQ:
 				return ASeqPatternAssistantTC.getAllDefinitions((ASeqPattern) rp, ptype, scope);
 			case SET:
 				return ASetPatternAssistantTC.getAllDefinitions((ASetPattern) rp, ptype, scope);
 			case TUPLE:
 				return ATuplePatternAssistantTC.getAllDefinitions((ATuplePattern) rp, ptype, scope);
 			case UNION:
 				return AUnionPatternAssistantTC.getAllDefinitions((AUnionPattern) rp, ptype, scope);
 			case MAPUNION:
 				return AMapUnionPatternAssistantTC.getAllDefinitions((AMapUnionPattern)rp,ptype,scope);
 			case MAP:
 				return AMapPatternAssistantTC.getAllDefinitions((AMapPattern)rp,ptype,scope);
 			default:
 				assert false : "PPatternAssistant.getDefinitions - should not hit this case";
 				return null;
 		}
 
 	}
 
 	public static void typeResolve(PPattern pattern,
 			QuestionAnswerAdaptor<TypeCheckInfo, PType> rootVisitor,
 			TypeCheckInfo question) throws AnalysisException
 	{
 		switch (pattern.kindPPattern())
 		{
 			case CONCATENATION:
 				AConcatenationPatternAssistantTC.typeResolve((AConcatenationPattern) pattern, rootVisitor, question);
 				break;
 			case EXPRESSION:
 				AExpressionPatternAssistantTC.typeResolve((AExpressionPattern) pattern, rootVisitor, question);
 				break;
 			case RECORD:
 				ARecordPatternAssistantTC.typeResolve((ARecordPattern) pattern, rootVisitor, question);
 				break;
 			case SEQ:
 				ASeqPatternAssistantTC.typeResolve((ASeqPattern) pattern, rootVisitor, question);
 				break;
 			case SET:
 				ASetPatternAssistantTC.typeResolve((ASetPattern) pattern, rootVisitor, question);
 				break;
 			case TUPLE:
 				ATuplePatternAssistantTC.typeResolve((ATuplePattern) pattern, rootVisitor, question);
 				break;
 			case UNION:
 				AUnionPatternAssistantTC.typeResolve((AUnionPattern) pattern, rootVisitor, question);
 				break;
 			case MAP:
 				AMapPatternAssistantTC.typeResolve((AMapPattern)pattern,rootVisitor, question);
 				break;
 			case MAPUNION:
 				AMapUnionPatternAssistantTC.typeResolve((AMapUnionPattern) pattern,rootVisitor, question);
 				break;
 			default:
 				pattern.setResolved(true);
 				break;
 		}
 	}
 
 	public static void unResolve(PPattern pattern)
 	{
 		switch (pattern.kindPPattern())
 		{
 			case CONCATENATION:
 				AConcatenationPatternAssistantTC.unResolve((AConcatenationPattern) pattern);
 				break;
 			case RECORD:
 				ARecordPatternAssistantTC.unResolve((ARecordPattern) pattern);
 				break;
 			case SEQ:
 				ASeqPatternAssistantTC.unResolve((ASeqPattern) pattern);
 				break;
 			case SET:
 				ASetPatternAssistantTC.unResolve((ASetPattern) pattern);
 				break;
 			case TUPLE:
 				ATuplePatternAssistantTC.unResolve((ATuplePattern) pattern);
 				break;
 			case UNION:
 				AUnionPatternAssistantTC.unResolve((AUnionPattern) pattern);
 				break;
 			case MAPUNION:
 				AMapUnionPatternAssistantTC.unResolve((AMapUnionPattern) pattern);
 				break;
 			case MAP:
 				AMapPatternAssistantTC.unResolve((AMapPattern) pattern);
 			default:
 				pattern.setResolved(false);
 				break;
 		}
 	}
 
 	public static PType getPossibleType(PPattern pattern)
 	{
 		switch (pattern.kindPPattern())
 		{
 			case BOOLEAN:
 				return ABooleanPatternAssistantTC.getPossibleType((ABooleanPattern) pattern);				
 			case CHARACTER:
 				return ACharacterPatternAssistantTC.getPossibleType((ACharacterPattern) pattern);	
 			case CONCATENATION:
 				return AConcatenationPatternAssistantTC.getPossibleType((AConcatenationPattern) pattern);			
 			case EXPRESSION:
 				return AExpressionPatternAssistantTC.getPossibleTypes((AExpressionPattern) pattern);
 			case IDENTIFIER:
 				return AIdentifierPatternAssistantTC.getPossibleTypes((AIdentifierPattern) pattern);
 			case IGNORE:
 				return AIgnorePatternAssistantTC.getPossibleTypes((AIgnorePattern) pattern);
 			case INTEGER:
 				return AIntegerPatternAssistantTC.getPossibleTypes((AIntegerPattern) pattern);
 			case NIL:
 				return ANilPatternAssistantTC.getPossibleTypes((ANilPattern) pattern);
 			case QUOTE:
 				return AQuotePatternAssistantTC.getPossibleTypes((AQuotePattern) pattern);
 			case REAL:
 				return ARealPatternAssistantTC.getPossibleTypes((ARealPattern) pattern);
 			case RECORD:
 				return ARecordPatternAssistantTC.getPossibleTypes((ARecordPattern) pattern);
 			case SET:
 				return ASetPatternAssistantTC.getPossibleTypes((ASetPattern) pattern);			
 			case SEQ:
 				return ASeqPatternAssistantTC.getPossibleTypes((ASeqPattern) pattern);
 			case STRING:
 				return AStringPatternAssistantTC.getPossibleTypes((AStringPattern) pattern);			
 			case TUPLE:
 				return ATuplePatternAssistantTC.getPossibleTypes((ATuplePattern) pattern);
 			case UNION:
 				return AUnionPatternAssistantTC.getPossibleTypes((AUnionPattern) pattern);
 			default:
 				assert false : "Should not happen";
 		}
 		return null;
 	}
 
 	public static boolean matches(PPattern pattern, PType expType)
 	{
 		return TypeComparator.compatible(getPossibleType(pattern), expType);
 	}
 
 	
 	
 	public static PExp getMatchingExpression(PPattern p) {
 		switch (p.kindPPattern()) {
 		case BOOLEAN:
 			return ABooleanPatternAssistantTC.getMatchingExpression((ABooleanPattern) p);
 		case CHARACTER:
 			return ACharacterPatternAssistantTC.getMatchingExpression((ACharacterPattern)p);
 		case CONCATENATION:
 			return AConcatenationPatternAssistantTC.getMatchingExpression((AConcatenationPattern)p);
 		case EXPRESSION:
 			return AExpressionPatternAssistantTC.getMatchingExpression((AExpressionPattern) p);
 		case IDENTIFIER:
 			return AIdentifierPatternAssistantTC.getMatchingExpression((AIdentifierPattern) p);
 		case IGNORE:
 			return AIgnorePatternAssistantTC.getMatchingExpression((AIgnorePattern) p);
 		case INTEGER:
 			return AIntegerPatternAssistantTC.getMatchingExpression((AIntegerPattern) p);
 		case NIL:
 			return ANilPatternAssistantTC.getMatchingExpression((ANilPattern) p);
 		case QUOTE:
 			return AQuotePatternAssistantTC.getMatchingExpression((AQuotePattern) p);
 		case REAL:
 			return ARealPatternAssistantTC.getMatchingExpression((ARealPattern) p);
 		case RECORD:
 			return ARecordPatternAssistantTC.getMatchingExpression((ARecordPattern) p);
 		case SEQ:
 			return ASeqPatternAssistantTC.getMatchingExpression((ASeqPattern) p);
 		case SET:
 			return ASetPatternAssistantTC.getMatchingExpression((ASetPattern) p);
 		case STRING:
 			return AStringPatternAssistantTC.getMatchingExpression((AStringPattern) p);
 		case TUPLE:
 			return ATuplePatternAssistantTC.getMatchingExpression((ATuplePattern) p);
 		case UNION:
 			return AUnionPatternAssistantTC.getMatchingExpression((AUnionPattern) p);
 		default:
 			assert false : "Should not happen";
 			return null;
 		}
 	}
 	
 	public static boolean isSimple(PPattern p)
 	{
 		
 		switch (p.kindPPattern()) {
 		case CONCATENATION:
 			return AConcatenationPatternAssistantTC.isSimple((AConcatenationPattern) p);
 		case IDENTIFIER:
 			return AIdentifierPatternAssistantTC.isSimple((AIdentifierPattern) p);
 		case IGNORE:
 			return AIgnorePatternAssistantTC.isSimple((AIgnorePattern) p);
 		case MAPUNION:
 			return AMapUnionPatternAssistantTC.isSimple((AMapUnionPattern) p);
 		case RECORD:
 			return ARecordPatternAssistantTC.isSimple((ARecordPattern) p);
 		case SEQ:
 			return ASeqPatternAssistantTC.isSimple((ASeqPattern) p);
 		case SET:
 			return ASetPatternAssistantTC.isSimple((ASetPattern) p);
 		case TUPLE:
 			return ATuplePatternAssistantTC.isSimple((ATuplePattern) p);
 		case UNION:
 			return AUnionPatternAssistantTC.isSimple((AUnionPattern) p);
		case MAP:
			return AMapPatternAssistantTC.isSimple((AMapPattern) p);
 		default:
 			/*
 			 * True if the pattern is a simple value that can match only
 			 * one value for certain. Most pattern types are like this, but any
 			 * that include variables or ignore patterns are not.
 			 */ 
 			return true;
 		}
 	}
 	
 }
