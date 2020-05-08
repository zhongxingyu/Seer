 package org.kalidasya.sonar.erlang.parser;
 
 import org.kalidasya.sonar.erlang.api.ErlangGrammar;
 import org.kalidasya.sonar.erlang.api.ErlangKeyword;
 import org.kalidasya.sonar.erlang.api.ErlangPunctator;
 import org.kalidasya.sonar.erlang.api.ErlangTokenType;
 
 import com.sonar.sslr.api.GenericTokenType;
 
 import static com.sonar.sslr.api.GenericTokenType.EOF;
 import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
 import static com.sonar.sslr.api.GenericTokenType.LITERAL;
 import static com.sonar.sslr.impl.matcher.GrammarFunctions.Predicate.next;
 import static com.sonar.sslr.impl.matcher.GrammarFunctions.Predicate.not;
 import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.and;
 import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.o2n;
 import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.one2n;
 import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.opt;
 import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.or;
 
 public class ErlangGrammarImpl extends ErlangGrammar {
 
 	
 
 	public ErlangGrammarImpl() {
 		module();
 		functions();
 		statements();
 		dataTypes();
 		operators();
 		guards();
 	}
 
 	private void module() {
		module.is(one2n(or(moduleAttribute, typeFunctionSpec)), one2n(functionDeclaration), EOF);
 		moduleAttribute.is(
 				ErlangPunctator.MINUS, 
 				IDENTIFIER, 
 				ErlangPunctator.LPARENTHESIS, 
 				or(
 					funcExport,
 					listedTermsOrFunCalls,
 					LITERAL,
 					IDENTIFIER
 				),
 				ErlangPunctator.RPARENTHESIS, 
 			ErlangPunctator.DOT
 		);
 		typeFunctionSpec.is(
 			ErlangPunctator.MINUS, 
 			IDENTIFIER,
 			funcCall,
 			or(
 				and(
 					ErlangPunctator.COLON,
 					ErlangPunctator.COLON,
 					funcCall,
 					o2n(
 						ErlangPunctator.PIPE,
 						funcCall
 					)
 				),
 				and(
 					ErlangPunctator.ARROW,
 					funcCall
 				)
 			),
 			ErlangPunctator.DOT
 		);
 		//TODO: is it possible to have something like: -export().?
 		funcExport.is(
 			or(
 				and(
 					ErlangPunctator.LBRACKET,
 					o2n(
 						funcArity,
 						o2n(
 							ErlangPunctator.COMMA,
 							funcArity
 						)
 					),
 					ErlangPunctator.RBRACKET
 				), 
 				funcArity
 			)
 		);
 	}
 
 	private void statements(){
 		arithmeticExp.is(
 			opt(
 				or(
 					ErlangTokenType.NUMERIC_LITERAL, 
 					funcCall,
 					IDENTIFIER
 				)
 			), 
 			arithmeticOp, 
 			or(
 				ErlangTokenType.NUMERIC_LITERAL,
 				funcCall,
 				IDENTIFIER
 			)
 		);
 		
 		termCompareExp.is(term, termCompOp, term);
 		
 		booleanExp.is(opt(expression), booleanOp, expression);
 		
 		shortcircuitExp.is(
 			opt(ErlangPunctator.LPARENTHESIS), 
 			expression, 
 			o2n(shortcircuitOp, shortcircuitExp), 
 			opt(ErlangPunctator.RPARENTHESIS)
 		);
 		listExp.is(list, listOp, list);
 		expression.is(or(arithmeticExp, listExp, flowExp, funcCall, term ));
 		flowExp.is(or(ifExp, caseExp, receiveExp));
 		caseExp.is(
 			ErlangKeyword.CASE, 
 			expression, 
 			ErlangKeyword.OF, 
 			one2n(or(pattern,ErlangPunctator.SEMI)),
 			ErlangKeyword.END
 		);
 		
 		ifExp.is(
 			ErlangKeyword.IF, 
 			one2n(
 				branchPatternExp
 			)
 		);
 		
 		receiveExp.is(ErlangKeyword.RECEIVE, one2n(branchPatternExp), ErlangKeyword.END);
 		
 		branchPatternExp.is(
 			or(
 				and(
 					guardExpression, 
 					ErlangPunctator.ARROW, 
 					one2n(branchExpression)
 				), 
 				ErlangPunctator.SEMI
 			)
 		);
 		
 		branchExpression.is(
 			or(
 				expression,
 				term
 			),
 			ErlangPunctator.SEMI
 		);
 		
 		pattern.is(
 			expression,
 			opt(guardSequenceStart),
 			ErlangPunctator.ARROW,
 			one2n(
 				or(
 					branchExpression,
 					ErlangPunctator.SEMI
 				)
 			)
 		);
 	}
 	
 	private void guards(){
 		guardSequenceStart.is(
 				ErlangKeyword.WHEN, 
 				guardSequence
 			);
 		guardSequence.is(one2n(or(guard,ErlangPunctator.SEMI)));
 		guard.is(one2n(or(guardExpression, ErlangPunctator.COMMA)));
 		guardExpression.is(
 			or(
 				termCompareExp, 
 				arithmeticExp, 
 				booleanExp, 
 				shortcircuitExp, 
 				funcCall,
 				IDENTIFIER
 			)
 		);
 	}
 	
 	private void dataTypes(){
 		term.is(or(LITERAL, ErlangTokenType.NUMERIC_LITERAL, list, tuple, IDENTIFIER));
 		termsOrFunCalls.is(
 			or(
 				funcCall,
 				term
 			)
 		);
 		listedTermsOrFunCalls.is(
 			and(
 				termsOrFunCalls,
 				o2n(
 					ErlangPunctator.COMMA,
 					termsOrFunCalls
 				)
 			)
 		);
 		tuple.is(
 			ErlangPunctator.LCURLYBRACE, 
 			o2n(
 				listedTermsOrFunCalls
 			),
 			ErlangPunctator.RCURLYBRACE
 		);
 		list.is(
 			ErlangPunctator.LBRACKET, 
 			o2n(
 				listedTermsOrFunCalls
 			),
 			ErlangPunctator.RBRACKET
 		);
 	}
 	
 	private void operators(){
 		termCompOp.is(
 			or(
 				ErlangPunctator.EQUAL, 
 				ErlangPunctator.EQUAL2, 
 				ErlangPunctator.NOTEQUAL, 
 				ErlangPunctator.NOTEQUAL2, 
 				ErlangPunctator.LT, 
 				ErlangPunctator.GT, 
 				ErlangPunctator.LE, 
 				ErlangPunctator.GE
 			)
 		);
 		booleanOp.is(or(
 			ErlangKeyword.NOT, 
 			ErlangKeyword.AND, 
 			ErlangKeyword.OR, 
 			ErlangKeyword.XOR
 		));
 		arithmeticOp.is(or(
 			ErlangPunctator.PLUS, 
 			ErlangPunctator.MINUS, 
 			ErlangPunctator.STAR, 
 			ErlangPunctator.DIV, 
 			ErlangKeyword.BNOT, 
 			ErlangKeyword.DIV, 
 			ErlangKeyword.REM, 
 			ErlangKeyword.BAND, 
 			ErlangKeyword.BOR,
 			ErlangKeyword.BXOR, 
 			ErlangKeyword.BSL, 
 			ErlangKeyword.BSR,
 			ErlangPunctator.NUMBERSIGN
 		));
 		listOp.is(or(ErlangPunctator.PLUSPLUS, ErlangPunctator.MINUSMINUS));
 		shortcircuitOp.is(or(ErlangKeyword.ANDALSO, ErlangKeyword.ORELSE));
 	}
 	
 	private void functions() {
 		functionDeclaration.is(
 			functionClause, 
 			o2n(
 				ErlangPunctator.SEMI,
 				functionClause
 			), 
 			ErlangPunctator.DOT
 		);
 		functionClause.is(clauseHead, ErlangPunctator.ARROW, clauseBody);
 		clauseHead.is(
 			funcCall,
 			opt(guardSequenceStart)
 		);
 		clauseBody.is(
 			or(expression, term),
 			
 			o2n(
 				ErlangPunctator.COMMA,
 				or(
 					expression,
 					term
 				) 
 			)
 		);
 		
 		funcArity.is(IDENTIFIER,
 					ErlangPunctator.DIV,
 					ErlangTokenType.NUMERIC_LITERAL);
 		funcCall.is(
 			opt(IDENTIFIER, ErlangPunctator.COLON),
 			IDENTIFIER, 
 			ErlangPunctator.LPARENTHESIS, 
 			opt(
 				or(
 					ErlangTokenType.NUMERIC_LITERAL,
 					GenericTokenType.LITERAL,
 					expression,
 					IDENTIFIER
 				),
 				o2n(
 					ErlangPunctator.COMMA,
 					or(
 						ErlangTokenType.NUMERIC_LITERAL,
 						GenericTokenType.LITERAL,
 						expression,
 						IDENTIFIER
 					)
 				)
 			), 
 			ErlangPunctator.RPARENTHESIS);
 	}
 }
