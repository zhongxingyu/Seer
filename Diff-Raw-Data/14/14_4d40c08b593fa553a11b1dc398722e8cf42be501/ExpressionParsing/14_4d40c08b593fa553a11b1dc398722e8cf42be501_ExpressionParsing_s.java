 /*
  * Copyright 2006 Dmitry Jemerov (yole)
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
 
 package org.consulo.python.parsing;
 
 import com.intellij.lang.PsiBuilder;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.psi.tree.IElementType;
 import org.consulo.python.PythonTokenSets;
 
 public class ExpressionParsing extends Parsing {
 	private static final Logger LOG = Logger.getInstance("#ru.yole.pythonlanguage.parsing.ExpressionParsing");
 
 	public ExpressionParsing(ParsingContext context) {
 		super(context);
 	}
 
 	public boolean parsePrimaryExpression(PsiBuilder builder, boolean isTargetExpression) {
 		IElementType firstToken = builder.getTokenType();
 		if (firstToken == org.consulo.python.PyTokenTypes.IDENTIFIER) {
 			if (isTargetExpression) {
 				buildTokenElement(org.consulo.python.PyElementTypes.TARGET_EXPRESSION, builder);
 			} else {
 				buildTokenElement(org.consulo.python.PyElementTypes.REFERENCE_EXPRESSION, builder);
 			}
 			return true;
 		}
 		if (firstToken == org.consulo.python.PyTokenTypes.INTEGER_LITERAL) {
 			buildTokenElement(org.consulo.python.PyElementTypes.INTEGER_LITERAL_EXPRESSION, builder);
 			return true;
 		}
 		if (firstToken == org.consulo.python.PyTokenTypes.FLOAT_LITERAL) {
 			buildTokenElement(org.consulo.python.PyElementTypes.FLOAT_LITERAL_EXPRESSION, builder);
 			return true;
 		}
 		if (firstToken == org.consulo.python.PyTokenTypes.IMAGINARY_LITERAL) {
 			buildTokenElement(org.consulo.python.PyElementTypes.IMAGINARY_LITERAL_EXPRESSION, builder);
 			return true;
 		}
 		if (firstToken == org.consulo.python.PyTokenTypes.STRING_LITERAL) {
 			PsiBuilder.Marker marker = builder.mark();
 			while (builder.getTokenType() == org.consulo.python.PyTokenTypes.STRING_LITERAL) {
 				builder.advanceLexer();
 			}
 			marker.done(org.consulo.python.PyElementTypes.STRING_LITERAL_EXPRESSION);
 			return true;
 		}
 		if (firstToken == org.consulo.python.PyTokenTypes.LPAR) {
 			parseParenthesizedExpression(builder, isTargetExpression);
 			return true;
 		}
 		if (firstToken == org.consulo.python.PyTokenTypes.LBRACKET) {
 			parseListLiteralExpression(builder, isTargetExpression);
 			return true;
 		}
 		if (firstToken == org.consulo.python.PyTokenTypes.LBRACE) {
 			parseDictLiteralExpression(builder);
 			return true;
 		}
 		if (firstToken == org.consulo.python.PyTokenTypes.TICK) {
 			parseReprExpression(builder);
 			return true;
 		}
 		return false;
 	}
 
 	private void parseListLiteralExpression(PsiBuilder builder, boolean isTargetExpression) {
 		LOG.assertTrue(builder.getTokenType() == org.consulo.python.PyTokenTypes.LBRACKET);
 		PsiBuilder.Marker expr = builder.mark();
 		builder.advanceLexer();
 		if (builder.getTokenType() == org.consulo.python.PyTokenTypes.RBRACKET) {
 			builder.advanceLexer();
 			expr.done(org.consulo.python.PyElementTypes.LIST_LITERAL_EXPRESSION);
 			return;
 		}
 		if (!parseSingleExpression(builder, isTargetExpression)) {
 			builder.error("expression expected");
 		}
 		if (builder.getTokenType() == org.consulo.python.PyTokenTypes.FOR_KEYWORD) {
 			parseListCompExpression(builder, expr, org.consulo.python.PyTokenTypes.RBRACKET, org.consulo.python.PyElementTypes.LIST_COMP_EXPRESSION);
 		} else {
 			while (builder.getTokenType() != org.consulo.python.PyTokenTypes.RBRACKET) {
 				if (builder.getTokenType() == org.consulo.python.PyTokenTypes.COMMA) {
 					builder.advanceLexer();
 				} else if (!parseSingleExpression(builder, isTargetExpression)) {
 					builder.error("expression or , or ] expected");
 				}
 			}
 
 			checkMatches(builder, org.consulo.python.PyTokenTypes.RBRACKET, "] expected");
 			expr.done(org.consulo.python.PyElementTypes.LIST_LITERAL_EXPRESSION);
 		}
 	}
 
 	private void parseListCompExpression(PsiBuilder builder, PsiBuilder.Marker expr, IElementType endToken, IElementType exprType) {
 		LOG.assertTrue(builder.getTokenType() == org.consulo.python.PyTokenTypes.FOR_KEYWORD);
 
 		label: {
 			while (true) {
 				builder.advanceLexer();
 				parseExpression(builder, true, false);
 				checkMatches(builder, org.consulo.python.PyTokenTypes.IN_KEYWORD, "'in' expected");
 				if (!parseSingleExpression(builder, false)) {
 					builder.error("expression expected");
 				}
 				while (builder.getTokenType() == org.consulo.python.PyTokenTypes.IF_KEYWORD) {
 					builder.advanceLexer();
 					parseExpression(builder);
 				}
 
 				if (builder.getTokenType() == endToken) {
 					builder.advanceLexer();
 					break label;
 				}
 				if (builder.getTokenType() != org.consulo.python.PyTokenTypes.FOR_KEYWORD) break;
 				expr.done(exprType);
 				expr = expr.precede();
 			}
 
 			builder.error("closing bracket or 'for' expected");
 		}
 
 
 		expr.done(exprType);
 	}
 
 	private void parseDictLiteralExpression(PsiBuilder builder) {
 		LOG.assertTrue(builder.getTokenType() == org.consulo.python.PyTokenTypes.LBRACE);
 		PsiBuilder.Marker expr = builder.mark();
 		builder.advanceLexer();
 		while ((builder.getTokenType() != org.consulo.python.PyTokenTypes.RBRACE) &&
 				(parseKeyValueExpression(builder))) {
 			if (builder.getTokenType() != org.consulo.python.PyTokenTypes.RBRACE) {
 				checkMatches(builder, org.consulo.python.PyTokenTypes.COMMA, "comma expected");
 			}
 		}
 		builder.advanceLexer();
 		expr.done(org.consulo.python.PyElementTypes.DICT_LITERAL_EXPRESSION);
 	}
 
 	private boolean parseKeyValueExpression(PsiBuilder builder) {
 		PsiBuilder.Marker marker = builder.mark();
 		if (!parseSingleExpression(builder, false)) {
 			marker.drop();
 			return false;
 		}
 		checkMatches(builder, org.consulo.python.PyTokenTypes.COLON, ": expected");
 		if (!parseSingleExpression(builder, false)) {
 			marker.drop();
 			return false;
 		}
 		marker.done(org.consulo.python.PyElementTypes.KEY_VALUE_EXPRESSION);
 		return true;
 	}
 
 	private void parseParenthesizedExpression(PsiBuilder builder, boolean isTargetExpression) {
 		LOG.assertTrue(builder.getTokenType() == org.consulo.python.PyTokenTypes.LPAR);
 		PsiBuilder.Marker expr = builder.mark();
 		builder.advanceLexer();
 		if (builder.getTokenType() == org.consulo.python.PyTokenTypes.RPAR) {
 			builder.advanceLexer();
 			expr.done(org.consulo.python.PyElementTypes.TUPLE_EXPRESSION);
 		} else {
 			parseExpressionOptional(builder, isTargetExpression);
 			if (builder.getTokenType() == org.consulo.python.PyTokenTypes.FOR_KEYWORD) {
 				parseListCompExpression(builder, expr, org.consulo.python.PyTokenTypes.RPAR, org.consulo.python.PyElementTypes.GENERATOR_EXPRESSION);
 			} else {
 				checkMatches(builder, org.consulo.python.PyTokenTypes.RPAR, ") expected");
 				expr.done(org.consulo.python.PyElementTypes.PARENTHESIZED_EXPRESSION);
 			}
 		}
 	}
 
 	private void parseReprExpression(PsiBuilder builder) {
 		LOG.assertTrue(builder.getTokenType() == org.consulo.python.PyTokenTypes.TICK);
 		PsiBuilder.Marker expr = builder.mark();
 		builder.advanceLexer();
 		parseExpression(builder);
 		checkMatches(builder, org.consulo.python.PyTokenTypes.TICK, "` expected");
 		expr.done(org.consulo.python.PyElementTypes.REPR_EXPRESSION);
 	}
 
 	public boolean parseMemberExpression(PsiBuilder builder, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parsePrimaryExpression(builder, false)) {
 			expr.drop();
 			return false;
 		}
 		while (true) {
 			IElementType tokenType = builder.getTokenType();
 			if (tokenType == org.consulo.python.PyTokenTypes.DOT) {
 				builder.advanceLexer();
 				checkMatches(builder, org.consulo.python.PyTokenTypes.IDENTIFIER, "name expected");
 				if ((isTargetExpression) && (builder.getTokenType() != org.consulo.python.PyTokenTypes.DOT)) {
 					expr.done(org.consulo.python.PyElementTypes.TARGET_EXPRESSION);
 				} else {
 					expr.done(org.consulo.python.PyElementTypes.REFERENCE_EXPRESSION);
 				}
 				expr = expr.precede();
 			} else if (tokenType == org.consulo.python.PyTokenTypes.LPAR) {
 				parseArgumentList(builder);
 				expr.done(org.consulo.python.PyElementTypes.CALL_EXPRESSION);
 				expr = expr.precede();
 			} else if (tokenType == org.consulo.python.PyTokenTypes.LBRACKET) {
 				builder.advanceLexer();
 				if (builder.getTokenType() == org.consulo.python.PyTokenTypes.COLON) {
 					PsiBuilder.Marker sliceMarker = builder.mark();
 					sliceMarker.done(org.consulo.python.PyElementTypes.EMPTY_EXPRESSION);
 					parseSliceEnd(builder, expr);
 				} else {
 					parseExpressionOptional(builder);
 					if (builder.getTokenType() == org.consulo.python.PyTokenTypes.COLON) {
 						parseSliceEnd(builder, expr);
 					} else {
 						checkMatches(builder, org.consulo.python.PyTokenTypes.RBRACKET, "] expected");
 						expr.done(org.consulo.python.PyElementTypes.SUBSCRIPTION_EXPRESSION);
 					}
 				}
 				expr = expr.precede();
 			} else {
 				expr.drop();
 				break;
 			}
 		}
 
 		return true;
 	}
 
 	private void parseSliceEnd(PsiBuilder builder, PsiBuilder.Marker expr) {
 		builder.advanceLexer();
 		if (builder.getTokenType() == org.consulo.python.PyTokenTypes.RBRACKET) {
 			PsiBuilder.Marker sliceMarker = builder.mark();
 			sliceMarker.done(org.consulo.python.PyElementTypes.EMPTY_EXPRESSION);
 			builder.advanceLexer();
 		} else {
 			if (builder.getTokenType() == org.consulo.python.PyTokenTypes.COLON) {
 				PsiBuilder.Marker sliceMarker = builder.mark();
 				sliceMarker.done(org.consulo.python.PyElementTypes.EMPTY_EXPRESSION);
 			} else {
 				parseExpression(builder);
 			}
 			if ((builder.getTokenType() != org.consulo.python.PyTokenTypes.RBRACKET) && (builder.getTokenType() != org.consulo.python.PyTokenTypes.COLON)) {
 				builder.error(": or ] expected");
 			}
 			if (builder.getTokenType() == org.consulo.python.PyTokenTypes.COLON) {
 				builder.advanceLexer();
 				parseExpressionOptional(builder);
 			}
 			checkMatches(builder, org.consulo.python.PyTokenTypes.RBRACKET, "] expected");
 		}
 		expr.done(org.consulo.python.PyElementTypes.SLICE_EXPRESSION);
 	}
 
 	public void parseArgumentList(PsiBuilder builder) {
 		LOG.assertTrue(builder.getTokenType() == org.consulo.python.PyTokenTypes.LPAR);
 		PsiBuilder.Marker arglist = builder.mark();
 		PsiBuilder.Marker genexpr = builder.mark();
 		builder.advanceLexer();
 		int argNumber = 0;
 		boolean needBracket = true;
 		while (builder.getTokenType() != org.consulo.python.PyTokenTypes.RPAR) {
 			argNumber++;
 			if (argNumber > 1) {
 				if ((argNumber == 2) && (builder.getTokenType() == org.consulo.python.PyTokenTypes.FOR_KEYWORD)) {
 					parseListCompExpression(builder, genexpr, org.consulo.python.PyTokenTypes.RPAR, org.consulo.python.PyElementTypes.GENERATOR_EXPRESSION);
 					needBracket = false;
 					break;
 				}
 				if (builder.getTokenType() == org.consulo.python.PyTokenTypes.COMMA) {
 					builder.advanceLexer();
 					if (builder.getTokenType() == org.consulo.python.PyTokenTypes.RPAR)
 						break;
 				} else {
 					builder.error(", or ) expected");
 					break;
 				}
 			}
 			if ((builder.getTokenType() == org.consulo.python.PyTokenTypes.MULT) || (builder.getTokenType() == org.consulo.python.PyTokenTypes.EXP)) {
 				PsiBuilder.Marker starArgMarker = builder.mark();
 				builder.advanceLexer();
 				if (!parseSingleExpression(builder, false)) {
 					builder.error("expression expected");
 				}
 				starArgMarker.done(org.consulo.python.PyElementTypes.STAR_ARGUMENT_EXPRESSION);
 			} else if (builder.getTokenType() == org.consulo.python.PyTokenTypes.IDENTIFIER) {
 				PsiBuilder.Marker keywordArgMarker = builder.mark();
 				builder.advanceLexer();
 				if (builder.getTokenType() == org.consulo.python.PyTokenTypes.EQ) {
 					builder.advanceLexer();
 					if (!parseSingleExpression(builder, false)) {
 						builder.error("expression expected");
 					}
 					keywordArgMarker.done(org.consulo.python.PyElementTypes.KEYWORD_ARGUMENT_EXPRESSION);
 				} else {
 					keywordArgMarker.rollbackTo();
 				}
 			} else if (!parseSingleExpression(builder, false)) {
 				builder.error("expression expected");
 			}
 
 		}
 
 		if (needBracket) {
 			genexpr.drop();
 			checkMatches(builder, org.consulo.python.PyTokenTypes.RPAR, ") expected");
 		}
 		arglist.done(org.consulo.python.PyElementTypes.ARGUMENT_LIST);
 	}
 
 	public boolean parseExpressionOptional(PsiBuilder builder) {
 		return parseTupleExpression(builder, false, false);
 	}
 
 	public boolean parseExpressionOptional(PsiBuilder builder, boolean isTargetExpression) {
 		return parseTupleExpression(builder, false, isTargetExpression);
 	}
 
 	public void parseExpression(PsiBuilder builder) {
 		if (!parseExpressionOptional(builder))
 			builder.error("expression expected");
 	}
 
 	public void parseExpression(PsiBuilder builder, boolean stopOnIn, boolean isTargetExpression) {
 		if (!parseTupleExpression(builder, stopOnIn, isTargetExpression))
 			builder.error("expression expected");
 	}
 
 	private boolean parseTupleExpression(PsiBuilder builder, boolean stopOnIn, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseLambdaExpression(builder, stopOnIn, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 		if (builder.getTokenType() == org.consulo.python.PyTokenTypes.COMMA) {
 			while (builder.getTokenType() == org.consulo.python.PyTokenTypes.COMMA) {
 				builder.advanceLexer();
 				PsiBuilder.Marker expr2 = builder.mark();
 				if (!parseLambdaExpression(builder, stopOnIn, isTargetExpression)) {
 					expr2.rollbackTo();
 					break;
 				}
 				expr2.drop();
 			}
 			expr.done(org.consulo.python.PyElementTypes.TUPLE_EXPRESSION);
 		} else {
 			expr.drop();
 		}
 		return true;
 	}
 
 	public boolean parseSingleExpression(PsiBuilder builder, boolean isTargetExpression) {
 		return parseLambdaExpression(builder, false, isTargetExpression);
 	}
 
 	private boolean parseLambdaExpression(PsiBuilder builder, boolean stopOnIn, boolean isTargetExpression) {
 		if (builder.getTokenType() == org.consulo.python.PyTokenTypes.LAMBDA_KEYWORD) {
 			PsiBuilder.Marker expr = builder.mark();
 			builder.advanceLexer();
 			getFunctionParser().parseParameterListContents(builder, org.consulo.python.PyTokenTypes.COLON, false);
 			if (!parseSingleExpression(builder, false)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.LAMBDA_EXPRESSION);
 			return true;
 		}
 		return parseORTestExpression(builder, stopOnIn, isTargetExpression);
 	}
 
 	private boolean parseORTestExpression(PsiBuilder builder, boolean stopOnIn, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseANDTestExpression(builder, stopOnIn, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 		while (builder.getTokenType() == org.consulo.python.PyTokenTypes.OR_KEYWORD) {
 			builder.advanceLexer();
 			if (!parseANDTestExpression(builder, stopOnIn, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 			expr = expr.precede();
 		}
 
 		expr.drop();
 		return true;
 	}
 
 	private boolean parseANDTestExpression(PsiBuilder builder, boolean stopOnIn, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseNOTTestExpression(builder, stopOnIn, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 		while (builder.getTokenType() == org.consulo.python.PyTokenTypes.AND_KEYWORD) {
 			builder.advanceLexer();
 			if (!parseNOTTestExpression(builder, stopOnIn, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 			expr = expr.precede();
 		}
 
 		expr.drop();
 		return true;
 	}
 
 	private boolean parseNOTTestExpression(PsiBuilder builder, boolean stopOnIn, boolean isTargetExpression) {
 		if (builder.getTokenType() == org.consulo.python.PyTokenTypes.NOT_KEYWORD) {
 			PsiBuilder.Marker expr = builder.mark();
 			builder.advanceLexer();
 			if (!parseNOTTestExpression(builder, stopOnIn, isTargetExpression)) {
 				builder.error("Expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.PREFIX_EXPRESSION);
 			return true;
 		}
 		return parseComparisonExpression(builder, stopOnIn, isTargetExpression);
 	}
 
 	private boolean parseComparisonExpression(PsiBuilder builder, boolean stopOnIn, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseBitwiseORExpression(builder, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 		if ((stopOnIn) && (builder.getTokenType() == org.consulo.python.PyTokenTypes.IN_KEYWORD)) {
 			expr.drop();
 			return true;
 		}
 		while (PythonTokenSets.COMPARISON_OPERATIONS.contains(builder.getTokenType())) {
 			if (builder.getTokenType() == org.consulo.python.PyTokenTypes.NOT_KEYWORD) {
 				PsiBuilder.Marker notMarker = builder.mark();
 				builder.advanceLexer();
 				if (builder.getTokenType() != org.consulo.python.PyTokenTypes.IN_KEYWORD) {
 					notMarker.rollbackTo();
 					break;
 				}
 				notMarker.drop();
 				builder.advanceLexer();
 			} else if (builder.getTokenType() == org.consulo.python.PyTokenTypes.IS_KEYWORD) {
 				builder.advanceLexer();
 				if (builder.getTokenType() == org.consulo.python.PyTokenTypes.NOT_KEYWORD)
 					builder.advanceLexer();
 			} else {
 				builder.advanceLexer();
 			}
 
 			if (!parseBitwiseORExpression(builder, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 			expr = expr.precede();
 		}
 
 		expr.drop();
 		return true;
 	}
 
 	private boolean parseBitwiseORExpression(PsiBuilder builder, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseBitwiseXORExpression(builder, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 		while (builder.getTokenType() == org.consulo.python.PyTokenTypes.OR) {
 			builder.advanceLexer();
 			if (!parseBitwiseXORExpression(builder, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 			expr = expr.precede();
 		}
 
 		expr.drop();
 		return true;
 	}
 
 	private boolean parseBitwiseXORExpression(PsiBuilder builder, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseBitwiseANDExpression(builder, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 		while (builder.getTokenType() == org.consulo.python.PyTokenTypes.XOR) {
 			builder.advanceLexer();
 			if (!parseBitwiseANDExpression(builder, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 			expr = expr.precede();
 		}
 
 		expr.drop();
 		return true;
 	}
 
 	private boolean parseBitwiseANDExpression(PsiBuilder builder, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseShiftExpression(builder, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 		while (builder.getTokenType() == org.consulo.python.PyTokenTypes.AND) {
 			builder.advanceLexer();
 			if (!parseShiftExpression(builder, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 			expr = expr.precede();
 		}
 
 		expr.drop();
 		return true;
 	}
 
 	private boolean parseShiftExpression(PsiBuilder builder, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseAdditiveExpression(builder, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 		while (PythonTokenSets.SHIFT_OPERATIONS.contains(builder.getTokenType())) {
 			builder.advanceLexer();
 			if (!parseAdditiveExpression(builder, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 			expr = expr.precede();
 		}
 
 		expr.drop();
 		return true;
 	}
 
 	private boolean parseAdditiveExpression(PsiBuilder builder, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseMultiplicativeExpression(builder, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 		while (PythonTokenSets.ADDITIVE_OPERATIONS.contains(builder.getTokenType())) {
 			builder.advanceLexer();
 			if (!parseMultiplicativeExpression(builder, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 			expr = expr.precede();
 		}
 
 		expr.drop();
 		return true;
 	}
 
 	private boolean parseMultiplicativeExpression(PsiBuilder builder, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseUnaryExpression(builder, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 
 		while (PythonTokenSets.MULTIPLICATIVE_OPERATIONS.contains(builder.getTokenType())) {
 			builder.advanceLexer();
 			if (!parseUnaryExpression(builder, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 			expr = expr.precede();
 		}
 
 		expr.drop();
 		return true;
 	}
 
 	private boolean parseUnaryExpression(PsiBuilder builder, boolean isTargetExpression) {
 		IElementType tokenType = builder.getTokenType();
 		if (PythonTokenSets.UNARY_OPERATIONS.contains(tokenType)) {
 			PsiBuilder.Marker expr = builder.mark();
 			builder.advanceLexer();
 			if (!parseUnaryExpression(builder, isTargetExpression)) {
 				builder.error("Expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.PREFIX_EXPRESSION);
 			return true;
 		}
 		return parsePowerExpression(builder, isTargetExpression);
 	}
 
 	private boolean parsePowerExpression(PsiBuilder builder, boolean isTargetExpression) {
 		PsiBuilder.Marker expr = builder.mark();
 		if (!parseMemberExpression(builder, isTargetExpression)) {
 			expr.drop();
 			return false;
 		}
 
 		if (builder.getTokenType() == org.consulo.python.PyTokenTypes.EXP) {
 			builder.advanceLexer();
 			if (!parseUnaryExpression(builder, isTargetExpression)) {
 				builder.error("expression expected");
 			}
 			expr.done(org.consulo.python.PyElementTypes.BINARY_EXPRESSION);
 		} else {
 			expr.drop();
 		}
 
 		return true;
 	}
 
 	private void buildTokenElement(IElementType type, PsiBuilder builder) {
 		PsiBuilder.Marker marker = builder.mark();
 		builder.advanceLexer();
 		marker.done(type);
 	}
 }
