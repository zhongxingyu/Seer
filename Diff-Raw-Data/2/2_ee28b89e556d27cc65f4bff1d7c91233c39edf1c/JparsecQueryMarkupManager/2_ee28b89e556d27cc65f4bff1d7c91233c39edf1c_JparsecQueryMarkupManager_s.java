 /*
  * QlParser.java
  * Created on Jul 1, 2013
  *
  * Copyright 2013 Lithium Technologies, Inc. 
  * Emeryville, California, U.S.A.  All Rights Reserved.
  *
  * This software is the  confidential and proprietary information
  * of  Lithium  Technologies,  Inc.  ("Confidential Information")
  * You shall not disclose such Confidential Information and shall 
  * use  it  only in  accordance  with  the terms of  the  license 
  * agreement you entered into with Lithium.
  */
 
 package lithium.ldn.starql.parsers;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import lithium.ldn.starql.exceptions.InvalidQueryException;
 import lithium.ldn.starql.model.QlBooleanConstraintNode;
 import lithium.ldn.starql.model.QlConstraint;
 import lithium.ldn.starql.model.QlConstraintOperator;
 import lithium.ldn.starql.model.QlConstraintPairOperator;
 import lithium.ldn.starql.model.QlConstraintValue;
 import lithium.ldn.starql.model.QlConstraintValueCollection;
 import lithium.ldn.starql.model.QlConstraintValueDate;
 import lithium.ldn.starql.model.QlConstraintValueNumber;
 import lithium.ldn.starql.model.QlConstraintValueString;
 import lithium.ldn.starql.model.QlField;
 import lithium.ldn.starql.model.QlPageConstraints;
 import lithium.ldn.starql.model.QlSelectStatement;
 import lithium.ldn.starql.model.QlSortClause;
 import lithium.ldn.starql.model.QlSortOrderType;
 
 import org.codehaus.jparsec.Parser;
 import org.codehaus.jparsec.Parsers;
 import org.codehaus.jparsec.Scanners;
 import org.codehaus.jparsec.error.ParserException;
 import org.codehaus.jparsec.functors.Map;
 import org.codehaus.jparsec.functors.Pair;
 import org.codehaus.jparsec.functors.Tuple3;
 import org.codehaus.jparsec.functors.Tuple5;
 import org.codehaus.jparsec.pattern.Patterns;
 
 import com.fasterxml.jackson.databind.util.ISO8601Utils;
 import com.google.common.collect.Lists;
 
 /**
  * When making changes to this file, make sure to run {@link JparsecQlParserTest}.
  * 
  * @author David Esposito
  */
 public class JparsecQueryMarkupManager implements QueryMarkupManager {
 	
 	@Override
 	public QlSelectStatement parseQlSelect(String query) throws InvalidQueryException {
 		try {
 			return qlSelectParser().parse(query);
 		} catch (ParserException e) {
 			throw new InvalidQueryException(e.getMessage(), query);
 		}
 	}
 	
 	/*
 	 * ====================================================
 	 * 		SELECT STATEMENT
 	 * ====================================================
 	 */	
 	protected Parser<QlSelectStatement> qlSelectParser() {
 		return paddedKeyword("SELECT", true).next(Parsers.tuple(fieldsParser().followedBy(paddedKeyword("FROM", true)), 
 				alphaNumeric(), 
 				padWithWhitespace(whereClauseParser().optional(), true), 
 				padWithWhitespace(orderByParser().optional(), false),
 				padWithWhitespace(pageConstraintParser().optional(), false))
 			.map(new Map<Tuple5<List<QlField>, String, QlBooleanConstraintNode, QlSortClause, QlPageConstraints>, 
 					QlSelectStatement>() {
 						@Override
 						public QlSelectStatement map(Tuple5<List<QlField>, String, QlBooleanConstraintNode, 
 								QlSortClause, QlPageConstraints> arg0) {
 							return new QlSelectStatement(arg0.a, arg0.b, arg0.c, arg0.d, arg0.e);
 						}
 			}));
 	}
 
 	/*
 	 * ====================================================
 	 * 		FIELDS
 	 * ====================================================
 	 */
 	protected Parser<List<QlField>> fieldsParser() {
 		return Parsers.or(fieldStarParser(), fieldCollectionParser());
 	}
 	
 	protected Parser<List<QlField>> fieldCollectionParser() {
 		return fieldParser().sepBy1(padWithWhitespace(keyword(","), false));
 	}
 	
 	protected Parser<QlField> fieldParser() {
 		return alphaNumeric().sepBy1(keyword("."))
 				.map(new Map<List<String>, QlField>() {
 					@Override
 					public QlField map(List<String> fieldNames) {
 						if (fieldNames.size() == 0) {
 							return null;
 						}
 						String[] names = new String[fieldNames.size()-1];
 						for (int i=1;i<fieldNames.size();i++) {
 							names[i-1] = fieldNames.get(i);
 						}
 						return new QlField(fieldNames.get(0), names);
 					}
 				});
 	}
 	
 	protected Parser<List<QlField>> fieldStarParser() {
 		return paddedKeyword("*", true)
 				.map(new Map<String, List<QlField>>() {
 					@Override
 					public List<QlField> map(String arg0) {
 						return Lists.newArrayList(new QlField(arg0));
 					}
 				});
 	}
 	
 	/*
 	 * ====================================================
 	 * 		SORT BY
 	 * ====================================================
 	 */
 	protected Parser<QlSortClause> orderByParser() {
 		return paddedKeyword("SORT BY", false)
 				.next(Parsers.tuple(fieldParser(), sortOrderTypeParser()))
 				.map(new Map<Pair<QlField, QlSortOrderType>, QlSortClause>() {
 						@Override
 						public QlSortClause map(Pair<QlField, QlSortOrderType> arg0) {
 							return new QlSortClause(arg0.a, arg0.b);
 						}
 				});
 	}
 	
 	protected Parser<QlSortOrderType> sortOrderTypeParser() {
 		return paddedRegex("[a-zA-Z]+", true)
 				.map(new Map<String, QlSortOrderType>() {
 					@Override
 					public QlSortOrderType map(String arg0) {
 						return QlSortOrderType.get(arg0);
 					}
 				});
 	}
 	
 	/*
 	 * ====================================================
 	 * 		LIMIT OFFSET
 	 * ====================================================
 	 */
 	protected Parser<QlPageConstraints> pageConstraintParser() {
 		return Parsers.tuple(padWithWhitespace(limitParser().optional(-1), false), 
 				offsetParser().optional(-1))
 				.map(new Map<Pair<Integer, Integer>, QlPageConstraints>() {
 					@Override
 					public QlPageConstraints map(Pair<Integer, Integer> arg0) {
 						return new QlPageConstraints(arg0.a, arg0.b);
 					}
 				});
 	}
 	
 	protected Parser<Integer> limitParser() {
 		return keywordIntegerPair("LIMIT", false);
 	}
 	
 	protected Parser<Integer> offsetParser() {
 		return keywordIntegerPair("OFFSET", false);
 	}
 	
 	/*
 	 * ====================================================
 	 * 		WHERE
 	 * ====================================================
 	 */
 	protected Parser<QlBooleanConstraintNode> whereClauseParser() {
 		return paddedKeyword("WHERE", false).next(constraintsParser());
 	}
 
 	protected Parser<QlBooleanConstraintNode> constraintsParser() {
 		return padWithWhitespace(constraintParser(), false)
 				.infixl(constraintPairOperatorParser());
 	}
 	
 	protected Parser<QlConstraintPairOperator> constraintPairOperatorParser() {
 		return padWithWhitespace(regex("(AND|OR)"), false)
 				.map(new Map<String, QlConstraintPairOperator>() {
 					@Override
 					public QlConstraintPairOperator map(String arg0) {
 						return QlConstraintPairOperator.get(arg0);
 					}
 				});
 	}
 	
 	protected Parser<QlBooleanConstraintNode> constraintParser() {
 		return Parsers.tuple(padWithWhitespace(fieldParser(), false), constraintOperatorParser(), 
 				constraintValueParser())
 				.map(new Map<Tuple3<QlField, QlConstraintOperator, QlConstraintValue>, QlBooleanConstraintNode>() {
 					@Override
 					public QlBooleanConstraintNode map(Tuple3<QlField, QlConstraintOperator, QlConstraintValue> arg0) {
 						return new QlConstraint(arg0.a, arg0.c, arg0.b);
 					}
 				});
 	}
 	
 	protected Parser<QlConstraintOperator> constraintOperatorParser() {
 		return paddedRegex("(!=|=|<=|>=|<|>|IN)", false)
 				.map(new Map<String, QlConstraintOperator>() {
 					@Override
 					public QlConstraintOperator map(String arg0) {
 						return QlConstraintOperator.get(arg0.toString());
 					}
 				});
 	}
 
 	/*
 	 * ====================================================
 	 * 		MISC
 	 * ====================================================
 	 */
 	
 	/**
 	 * All added whitespace is optional. To require white space, you should add it to your parser inline.
 	 * @param parser The parser to wrap in optional white space.
 	 * @param leadingWhitespaces If optional white space should prepend the provided parser.
 	 * @return The parser that was wrapped in optional white space.
 	 */
 	protected <T> Parser<T> padWithWhitespace(Parser<T> parser, boolean leadingWhitespaces) {
 		return leadingWhitespaces 
 				? Scanners.WHITESPACES.optional().next(parser).followedBy(Scanners.WHITESPACES.optional())
 				: parser.followedBy(Scanners.WHITESPACES.optional());
 	}
 	
 	/**
 	 * This value is not padded with leading/trailing whitespace. Values can start with [a-zA-Z_].
 	 * 
 	 * @return The alpha-numberic string that was parsed.
 	 */
 	protected Parser<String> alphaNumeric() {
 		return regex("[a-zA-Z_][a-zA-Z0-9_]*");
 	}
 	
 	/**
 	 * This is a non-padded keyword literal that is case sensitive.
 	 * @param keyword
 	 * @return The keyword that was parsed.
 	 */
 	protected Parser<String> keyword(String keyword) {
 		return Scanners.string(keyword).source();
 	}
 	
 	protected Parser<String> paddedKeyword(String keyword, boolean leadingWhitespace) {
 		return padWithWhitespace(keyword(keyword), leadingWhitespace);
 	}
 
 	protected Parser<Integer> keywordIntegerPair(String keyword, boolean leadingWhiteSpace) {
 		return paddedKeyword(keyword, leadingWhiteSpace).next(Scanners.INTEGER)
 				.map(new Map<String, Integer>() {
 					@Override
 					public Integer map(String arg0) {
 						return Integer.parseInt(arg0);
 					}
 				});
 	}
 	
 	protected Parser<String> paddedRegex(String pattern, boolean leadingWhitespace) {
 		return paddedRegex(pattern, "regexParser: " + pattern, leadingWhitespace);
 	}
 	
 	protected Parser<String> regex(String pattern) {
 		return regex(pattern, "regexParser: " + pattern);
 	}
 	
 	protected Parser<String> paddedRegex(String pattern, String name, boolean leadingWhitespace) {
 		return padWithWhitespace(Scanners.pattern(Patterns.regex(pattern), name).source(), leadingWhitespace);
 	}
 	
 	protected Parser<String> regex(String pattern, String name) {
 		return Scanners.pattern(Patterns.regex(pattern), name).source();
 	}
 	
 	/**
 	 * Parser for any type of constraint value.
 	 * 
 	 * @return The value, an integer, quoted string without the quotes, or collection.
 	 */
 	protected Parser<QlConstraintValue> constraintValueParser() {
 		return Parsers.or(dateValueParser(),
 				numericalValueParser(), 
 				collectionValueParser(),
 				stringValueParser()
 				);
 	}
 	
 	/**
 	 * String variables may be in single or double quotes (regardless, quotes on either end must match).
 	 * Single quoted strings have single quotes escaped by repeating, so use two single quotes ('').
 	 * Double quoted strings are escaped with the character '\'.  Only the double quote character needs escaping.
 	 * 
 	 * @return A quoted string without the surrounding single quotes.
 	 */
 	protected Parser<QlConstraintValueString> stringValueParser() {
 		return Parsers.or(Scanners.DOUBLE_QUOTE_STRING
 				.map(new Map<String, QlConstraintValueString>() {
 					@Override
 					public QlConstraintValueString map(String arg0) {
 						String ret = arg0;
 						// Remove beginning and ending double quote.
 						ret = ret.substring(1);
 						ret = ret.substring(0, ret.length()-1);
 						// Unescape double quotes, ie. "\"" becomes """, "\\"" becomes "\"".
 						Pattern p = Pattern.compile("(\\\\(.))");
 						Matcher m = p.matcher(ret);
 						StringBuffer sb = new StringBuffer();
 						while(m.find()) {
 							if (m.group(2).equals("\"")) {
 								m.appendReplacement(sb, "\"");
 							} else if (m.group(2).equals("\\")) {
 								m.appendReplacement(sb, "\\");
 							} else {
 								throw new ParserException(null, "stringValueParser", null);
 							}
 						}
 						m.appendTail(sb);
 						ret = sb.toString();
 						return new QlConstraintValueString(ret);
 					}
 				}),Scanners.SINGLE_QUOTE_STRING
 				.map(new Map<String, QlConstraintValueString>(){
 					@Override
 					public QlConstraintValueString map(String arg0) {
 						String ret = arg0;
 						// Remove beginning and ending single quote.
 						ret = ret.substring(1);
 						ret = ret.substring(0, ret.length()-1);
 						// Unescape single quotes, ie. "''" becomes "'".
 						ret = ret.replaceAll("''", "'");
 						return new QlConstraintValueString(ret);
 					}
 				})
 				);
 	}
 	
 	/**
 	 * A collection is a constraint value consisting of a pair of parentheses around a comma-separated
 	 * list of constraint values, as defined above excluding collections, so it is not recursive.
 	 * 
 	 * @return ConstraintValueCollection containing ConstraintValues *MAY BE OF DIFFERENT TYPES*
 	 */
 	protected Parser<QlConstraintValueCollection<? extends QlConstraintValue>> collectionValueParser() {
 		return padWithWhitespace(Parsers.or(stringValueParser(), numericalValueParser()), false).sepBy(padWithWhitespace(keyword(","), false))
 				.between(padWithWhitespace(keyword("("), false), padWithWhitespace(keyword(")"), false))
 				.map(new Map<List<QlConstraintValue>, QlConstraintValueCollection<? extends QlConstraintValue>>(){
 
 					@Override
 					public QlConstraintValueCollection<? extends QlConstraintValue> map(List<QlConstraintValue> arg0) {
 						return new QlConstraintValueCollection<QlConstraintValue>(arg0);
 					}
 				});
 	}
 	
 	/**
 	 * Supports 
 	 *   - int: [1-9][0-9]*
 	 *   - long: [1-9][0-9]*L
 	 *   - double: [0-9]+\.[0-9]+
 	 *   - float: [0-9]+\.[0-9]+f
 	 * 
 	 * @return The matched string
 	 */
 	protected Parser<QlConstraintValueNumber> numericalValueParser() {
 		return regex("([0-9]+\\.[0-9]+f?|[1-9][0-9]*L?)")
 				.map(new Map<String, QlConstraintValueNumber>(){
 					@Override
 					public QlConstraintValueNumber map(String arg0) {
 						if (arg0.contains(".")) {
 							if (arg0.contains("f")) {
 								return new QlConstraintValueNumber(Float.parseFloat(arg0));
 							}
 							return new QlConstraintValueNumber(Double.parseDouble(arg0));
 						}
 						if (arg0.contains("L")) {
 							return new QlConstraintValueNumber(Long.parseLong(arg0.substring(0, arg0.length()-1)));
 						}
 						try {
 							return new QlConstraintValueNumber(Integer.parseInt(arg0));
 						}
 						catch(NumberFormatException e) {
							return new QlConstraintValueNumber(Long.parseLong(arg0.substring(0, arg0.length()-1)));
 						}
 					}
 				});
 	}
 	
 	
 	protected Parser<QlConstraintValueDate> dateValueParser() {
 		return regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(Z|(\\+|-)[0-9]{2}:[0-9]{2})?")
 				.map(new Map<String, QlConstraintValueDate>(){
 					@Override
 					public QlConstraintValueDate map(String arg0) {
 						return new QlConstraintValueDate(ISO8601Utils.parse(arg0));
 					}
 				});
 	}
 }
