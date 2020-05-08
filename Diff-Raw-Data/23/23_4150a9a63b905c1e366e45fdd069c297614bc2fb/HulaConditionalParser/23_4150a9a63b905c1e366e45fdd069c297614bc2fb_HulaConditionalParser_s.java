// $ANTLR 3.5 com/hula/lang/conditional/HulaConditional.g 2013-04-08 18:27:05
 
 	package com.hula.lang.conditional;
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.antlr.runtime.tree.*;
 
 
 /**
  * Copyright 2013 Simon Curd <simoncurd@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 @SuppressWarnings("all")
 public class HulaConditionalParser extends Parser {
 	public static final String[] tokenNames = new String[] {
 		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "BOOLEAN", "IDENT", "NUMBER", 
 		"PROPERTYNAME", "STRING", "WS", "'!='", "'('", "')'", "'<'", "'<='", "'='", 
 		"'>'", "'>='", "'AND'", "'OR'"
 	};
 	public static final int EOF=-1;
 	public static final int T__10=10;
 	public static final int T__11=11;
 	public static final int T__12=12;
 	public static final int T__13=13;
 	public static final int T__14=14;
 	public static final int T__15=15;
 	public static final int T__16=16;
 	public static final int T__17=17;
 	public static final int T__18=18;
 	public static final int T__19=19;
 	public static final int BOOLEAN=4;
 	public static final int IDENT=5;
 	public static final int NUMBER=6;
 	public static final int PROPERTYNAME=7;
 	public static final int STRING=8;
 	public static final int WS=9;
 
 	// delegates
 	public Parser[] getDelegates() {
 		return new Parser[] {};
 	}
 
 	// delegators
 
 
 	public HulaConditionalParser(TokenStream input) {
 		this(input, new RecognizerSharedState());
 	}
 	public HulaConditionalParser(TokenStream input, RecognizerSharedState state) {
 		super(input, state);
 	}
 
 	protected TreeAdaptor adaptor = new CommonTreeAdaptor();
 
 	public void setTreeAdaptor(TreeAdaptor adaptor) {
 		this.adaptor = adaptor;
 	}
 	public TreeAdaptor getTreeAdaptor() {
 		return adaptor;
 	}
 	@Override public String[] getTokenNames() { return HulaConditionalParser.tokenNames; }
 	@Override public String getGrammarFileName() { return "com/hula/lang/conditional/HulaConditional.g"; }
 
 
 		@Override
 		protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException
 		{
 			throw new MismatchedTokenException(ttype, input);
 		}
 		@Override
 		public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException
 		{
 			throw e;
 		}
 
 
 
 	public static class parse_return extends ParserRuleReturnScope {
 		CommonTree tree;
 		@Override
 		public CommonTree getTree() { return tree; }
 	};
 
 
 	// $ANTLR start "parse"
 	// com/hula/lang/conditional/HulaConditional.g:61:1: parse : expression EOF !;
 	public final HulaConditionalParser.parse_return parse() throws RecognitionException {
 		HulaConditionalParser.parse_return retval = new HulaConditionalParser.parse_return();
 		retval.start = input.LT(1);
 
 		CommonTree root_0 = null;
 
 		Token EOF2=null;
 		ParserRuleReturnScope expression1 =null;
 
 		CommonTree EOF2_tree=null;
 
 		try {
 			// com/hula/lang/conditional/HulaConditional.g:62:2: ( expression EOF !)
 			// com/hula/lang/conditional/HulaConditional.g:62:4: expression EOF !
 			{
 			root_0 = (CommonTree)adaptor.nil();
 
 
 			pushFollow(FOLLOW_expression_in_parse75);
 			expression1=expression();
 			state._fsp--;
 
 			adaptor.addChild(root_0, expression1.getTree());
 
 			EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_parse77); 
 			}
 
 			retval.stop = input.LT(-1);
 
 			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
 			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
 		}
 
 			catch (RecognitionException e) 
 			{
 				throw e;
 			}
 
 		finally {
 			// do for sure before leaving
 		}
 		return retval;
 	}
 	// $ANTLR end "parse"
 
 
 	public static class term_return extends ParserRuleReturnScope {
 		CommonTree tree;
 		@Override
 		public CommonTree getTree() { return tree; }
 	};
 
 
 	// $ANTLR start "term"
 	// com/hula/lang/conditional/HulaConditional.g:65:1: term : ( IDENT | '(' ! expression ')' !| STRING | NUMBER | BOOLEAN );
 	public final HulaConditionalParser.term_return term() throws RecognitionException {
 		HulaConditionalParser.term_return retval = new HulaConditionalParser.term_return();
 		retval.start = input.LT(1);
 
 		CommonTree root_0 = null;
 
 		Token IDENT3=null;
 		Token char_literal4=null;
 		Token char_literal6=null;
 		Token STRING7=null;
 		Token NUMBER8=null;
 		Token BOOLEAN9=null;
 		ParserRuleReturnScope expression5 =null;
 
 		CommonTree IDENT3_tree=null;
 		CommonTree char_literal4_tree=null;
 		CommonTree char_literal6_tree=null;
 		CommonTree STRING7_tree=null;
 		CommonTree NUMBER8_tree=null;
 		CommonTree BOOLEAN9_tree=null;
 
 		try {
 			// com/hula/lang/conditional/HulaConditional.g:66:2: ( IDENT | '(' ! expression ')' !| STRING | NUMBER | BOOLEAN )
 			int alt1=5;
 			switch ( input.LA(1) ) {
 			case IDENT:
 				{
 				alt1=1;
 				}
 				break;
 			case 11:
 				{
 				alt1=2;
 				}
 				break;
 			case STRING:
 				{
 				alt1=3;
 				}
 				break;
 			case NUMBER:
 				{
 				alt1=4;
 				}
 				break;
 			case BOOLEAN:
 				{
 				alt1=5;
 				}
 				break;
 			default:
 				NoViableAltException nvae =
 					new NoViableAltException("", 1, 0, input);
 				throw nvae;
 			}
 			switch (alt1) {
 				case 1 :
 					// com/hula/lang/conditional/HulaConditional.g:66:4: IDENT
 					{
 					root_0 = (CommonTree)adaptor.nil();
 
 
 					IDENT3=(Token)match(input,IDENT,FOLLOW_IDENT_in_term89); 
 					IDENT3_tree = (CommonTree)adaptor.create(IDENT3);
 					adaptor.addChild(root_0, IDENT3_tree);
 
 					}
 					break;
 				case 2 :
 					// com/hula/lang/conditional/HulaConditional.g:67:4: '(' ! expression ')' !
 					{
 					root_0 = (CommonTree)adaptor.nil();
 
 
 					char_literal4=(Token)match(input,11,FOLLOW_11_in_term94); 
 					pushFollow(FOLLOW_expression_in_term97);
 					expression5=expression();
 					state._fsp--;
 
 					adaptor.addChild(root_0, expression5.getTree());
 
 					char_literal6=(Token)match(input,12,FOLLOW_12_in_term99); 
 					}
 					break;
 				case 3 :
 					// com/hula/lang/conditional/HulaConditional.g:68:4: STRING
 					{
 					root_0 = (CommonTree)adaptor.nil();
 
 
 					STRING7=(Token)match(input,STRING,FOLLOW_STRING_in_term105); 
 					STRING7_tree = (CommonTree)adaptor.create(STRING7);
 					adaptor.addChild(root_0, STRING7_tree);
 
 					}
 					break;
 				case 4 :
 					// com/hula/lang/conditional/HulaConditional.g:69:4: NUMBER
 					{
 					root_0 = (CommonTree)adaptor.nil();
 
 
 					NUMBER8=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_term110); 
 					NUMBER8_tree = (CommonTree)adaptor.create(NUMBER8);
 					adaptor.addChild(root_0, NUMBER8_tree);
 
 					}
 					break;
 				case 5 :
 					// com/hula/lang/conditional/HulaConditional.g:70:4: BOOLEAN
 					{
 					root_0 = (CommonTree)adaptor.nil();
 
 
 					BOOLEAN9=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_term115); 
 					BOOLEAN9_tree = (CommonTree)adaptor.create(BOOLEAN9);
 					adaptor.addChild(root_0, BOOLEAN9_tree);
 
 					}
 					break;
 
 			}
 			retval.stop = input.LT(-1);
 
 			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
 			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
 		}
 
 			catch (RecognitionException e) 
 			{
 				throw e;
 			}
 
 		finally {
 			// do for sure before leaving
 		}
 		return retval;
 	}
 	// $ANTLR end "term"
 
 
 	public static class relation_return extends ParserRuleReturnScope {
 		CommonTree tree;
 		@Override
 		public CommonTree getTree() { return tree; }
 	};
 
 
 	// $ANTLR start "relation"
 	// com/hula/lang/conditional/HulaConditional.g:73:1: relation : term ( ( '=' ^| '>' ^| '<' ^| '>=' ^| '<=' ^| '!=' ^) term )* ;
 	public final HulaConditionalParser.relation_return relation() throws RecognitionException {
 		HulaConditionalParser.relation_return retval = new HulaConditionalParser.relation_return();
 		retval.start = input.LT(1);
 
 		CommonTree root_0 = null;
 
 		Token char_literal11=null;
 		Token char_literal12=null;
 		Token char_literal13=null;
 		Token string_literal14=null;
 		Token string_literal15=null;
 		Token string_literal16=null;
 		ParserRuleReturnScope term10 =null;
 		ParserRuleReturnScope term17 =null;
 
 		CommonTree char_literal11_tree=null;
 		CommonTree char_literal12_tree=null;
 		CommonTree char_literal13_tree=null;
 		CommonTree string_literal14_tree=null;
 		CommonTree string_literal15_tree=null;
 		CommonTree string_literal16_tree=null;
 
 		try {
 			// com/hula/lang/conditional/HulaConditional.g:74:2: ( term ( ( '=' ^| '>' ^| '<' ^| '>=' ^| '<=' ^| '!=' ^) term )* )
 			// com/hula/lang/conditional/HulaConditional.g:74:4: term ( ( '=' ^| '>' ^| '<' ^| '>=' ^| '<=' ^| '!=' ^) term )*
 			{
 			root_0 = (CommonTree)adaptor.nil();
 
 
 			pushFollow(FOLLOW_term_in_relation126);
 			term10=term();
 			state._fsp--;
 
 			adaptor.addChild(root_0, term10.getTree());
 
 			// com/hula/lang/conditional/HulaConditional.g:74:9: ( ( '=' ^| '>' ^| '<' ^| '>=' ^| '<=' ^| '!=' ^) term )*
 			loop3:
 			while (true) {
 				int alt3=2;
 				int LA3_0 = input.LA(1);
 				if ( (LA3_0==10||(LA3_0 >= 13 && LA3_0 <= 17)) ) {
 					alt3=1;
 				}
 
 				switch (alt3) {
 				case 1 :
 					// com/hula/lang/conditional/HulaConditional.g:74:11: ( '=' ^| '>' ^| '<' ^| '>=' ^| '<=' ^| '!=' ^) term
 					{
 					// com/hula/lang/conditional/HulaConditional.g:74:11: ( '=' ^| '>' ^| '<' ^| '>=' ^| '<=' ^| '!=' ^)
 					int alt2=6;
 					switch ( input.LA(1) ) {
 					case 15:
 						{
 						alt2=1;
 						}
 						break;
 					case 16:
 						{
 						alt2=2;
 						}
 						break;
 					case 13:
 						{
 						alt2=3;
 						}
 						break;
 					case 17:
 						{
 						alt2=4;
 						}
 						break;
 					case 14:
 						{
 						alt2=5;
 						}
 						break;
 					case 10:
 						{
 						alt2=6;
 						}
 						break;
 					default:
 						NoViableAltException nvae =
 							new NoViableAltException("", 2, 0, input);
 						throw nvae;
 					}
 					switch (alt2) {
 						case 1 :
 							// com/hula/lang/conditional/HulaConditional.g:74:12: '=' ^
 							{
 							char_literal11=(Token)match(input,15,FOLLOW_15_in_relation131); 
 							char_literal11_tree = (CommonTree)adaptor.create(char_literal11);
 							root_0 = (CommonTree)adaptor.becomeRoot(char_literal11_tree, root_0);
 
 							}
 							break;
 						case 2 :
 							// com/hula/lang/conditional/HulaConditional.g:74:19: '>' ^
 							{
 							char_literal12=(Token)match(input,16,FOLLOW_16_in_relation136); 
 							char_literal12_tree = (CommonTree)adaptor.create(char_literal12);
 							root_0 = (CommonTree)adaptor.becomeRoot(char_literal12_tree, root_0);
 
 							}
 							break;
 						case 3 :
 							// com/hula/lang/conditional/HulaConditional.g:74:26: '<' ^
 							{
 							char_literal13=(Token)match(input,13,FOLLOW_13_in_relation141); 
 							char_literal13_tree = (CommonTree)adaptor.create(char_literal13);
 							root_0 = (CommonTree)adaptor.becomeRoot(char_literal13_tree, root_0);
 
 							}
 							break;
 						case 4 :
 							// com/hula/lang/conditional/HulaConditional.g:74:33: '>=' ^
 							{
 							string_literal14=(Token)match(input,17,FOLLOW_17_in_relation146); 
 							string_literal14_tree = (CommonTree)adaptor.create(string_literal14);
 							root_0 = (CommonTree)adaptor.becomeRoot(string_literal14_tree, root_0);
 
 							}
 							break;
 						case 5 :
 							// com/hula/lang/conditional/HulaConditional.g:74:41: '<=' ^
 							{
 							string_literal15=(Token)match(input,14,FOLLOW_14_in_relation151); 
 							string_literal15_tree = (CommonTree)adaptor.create(string_literal15);
 							root_0 = (CommonTree)adaptor.becomeRoot(string_literal15_tree, root_0);
 
 							}
 							break;
 						case 6 :
 							// com/hula/lang/conditional/HulaConditional.g:74:49: '!=' ^
 							{
 							string_literal16=(Token)match(input,10,FOLLOW_10_in_relation156); 
 							string_literal16_tree = (CommonTree)adaptor.create(string_literal16);
 							root_0 = (CommonTree)adaptor.becomeRoot(string_literal16_tree, root_0);
 
 							}
 							break;
 
 					}
 
 					pushFollow(FOLLOW_term_in_relation161);
 					term17=term();
 					state._fsp--;
 
 					adaptor.addChild(root_0, term17.getTree());
 
 					}
 					break;
 
 				default :
 					break loop3;
 				}
 			}
 
 			}
 
 			retval.stop = input.LT(-1);
 
 			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
 			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
 		}
 
 			catch (RecognitionException e) 
 			{
 				throw e;
 			}
 
 		finally {
 			// do for sure before leaving
 		}
 		return retval;
 	}
 	// $ANTLR end "relation"
 
 
 	public static class expression_return extends ParserRuleReturnScope {
 		CommonTree tree;
 		@Override
 		public CommonTree getTree() { return tree; }
 	};
 
 
 	// $ANTLR start "expression"
 	// com/hula/lang/conditional/HulaConditional.g:77:1: expression : relation ( ( 'AND' ^| 'OR' ^) relation )* ;
 	public final HulaConditionalParser.expression_return expression() throws RecognitionException {
 		HulaConditionalParser.expression_return retval = new HulaConditionalParser.expression_return();
 		retval.start = input.LT(1);
 
 		CommonTree root_0 = null;
 
 		Token string_literal19=null;
 		Token string_literal20=null;
 		ParserRuleReturnScope relation18 =null;
 		ParserRuleReturnScope relation21 =null;
 
 		CommonTree string_literal19_tree=null;
 		CommonTree string_literal20_tree=null;
 
 		try {
 			// com/hula/lang/conditional/HulaConditional.g:78:2: ( relation ( ( 'AND' ^| 'OR' ^) relation )* )
 			// com/hula/lang/conditional/HulaConditional.g:78:4: relation ( ( 'AND' ^| 'OR' ^) relation )*
 			{
 			root_0 = (CommonTree)adaptor.nil();
 
 
 			pushFollow(FOLLOW_relation_in_expression174);
 			relation18=relation();
 			state._fsp--;
 
 			adaptor.addChild(root_0, relation18.getTree());
 
 			// com/hula/lang/conditional/HulaConditional.g:78:14: ( ( 'AND' ^| 'OR' ^) relation )*
 			loop5:
 			while (true) {
 				int alt5=2;
 				int LA5_0 = input.LA(1);
 				if ( ((LA5_0 >= 18 && LA5_0 <= 19)) ) {
 					alt5=1;
 				}
 
 				switch (alt5) {
 				case 1 :
 					// com/hula/lang/conditional/HulaConditional.g:78:15: ( 'AND' ^| 'OR' ^) relation
 					{
 					// com/hula/lang/conditional/HulaConditional.g:78:15: ( 'AND' ^| 'OR' ^)
 					int alt4=2;
 					int LA4_0 = input.LA(1);
 					if ( (LA4_0==18) ) {
 						alt4=1;
 					}
 					else if ( (LA4_0==19) ) {
 						alt4=2;
 					}
 
 					else {
 						NoViableAltException nvae =
 							new NoViableAltException("", 4, 0, input);
 						throw nvae;
 					}
 
 					switch (alt4) {
 						case 1 :
 							// com/hula/lang/conditional/HulaConditional.g:78:16: 'AND' ^
 							{
 							string_literal19=(Token)match(input,18,FOLLOW_18_in_expression179); 
 							string_literal19_tree = (CommonTree)adaptor.create(string_literal19);
 							root_0 = (CommonTree)adaptor.becomeRoot(string_literal19_tree, root_0);
 
 							}
 							break;
 						case 2 :
 							// com/hula/lang/conditional/HulaConditional.g:78:25: 'OR' ^
 							{
 							string_literal20=(Token)match(input,19,FOLLOW_19_in_expression184); 
 							string_literal20_tree = (CommonTree)adaptor.create(string_literal20);
 							root_0 = (CommonTree)adaptor.becomeRoot(string_literal20_tree, root_0);
 
 							}
 							break;
 
 					}
 
 					pushFollow(FOLLOW_relation_in_expression188);
 					relation21=relation();
 					state._fsp--;
 
 					adaptor.addChild(root_0, relation21.getTree());
 
 					}
 					break;
 
 				default :
 					break loop5;
 				}
 			}
 
 			}
 
 			retval.stop = input.LT(-1);
 
 			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
 			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
 		}
 
 			catch (RecognitionException e) 
 			{
 				throw e;
 			}
 
 		finally {
 			// do for sure before leaving
 		}
 		return retval;
 	}
 	// $ANTLR end "expression"
 
 	// Delegated rules
 
 
 
 	public static final BitSet FOLLOW_expression_in_parse75 = new BitSet(new long[]{0x0000000000000000L});
 	public static final BitSet FOLLOW_EOF_in_parse77 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_IDENT_in_term89 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_11_in_term94 = new BitSet(new long[]{0x0000000000000970L});
 	public static final BitSet FOLLOW_expression_in_term97 = new BitSet(new long[]{0x0000000000001000L});
 	public static final BitSet FOLLOW_12_in_term99 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_STRING_in_term105 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_NUMBER_in_term110 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_BOOLEAN_in_term115 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_term_in_relation126 = new BitSet(new long[]{0x000000000003E402L});
 	public static final BitSet FOLLOW_15_in_relation131 = new BitSet(new long[]{0x0000000000000970L});
 	public static final BitSet FOLLOW_16_in_relation136 = new BitSet(new long[]{0x0000000000000970L});
 	public static final BitSet FOLLOW_13_in_relation141 = new BitSet(new long[]{0x0000000000000970L});
 	public static final BitSet FOLLOW_17_in_relation146 = new BitSet(new long[]{0x0000000000000970L});
 	public static final BitSet FOLLOW_14_in_relation151 = new BitSet(new long[]{0x0000000000000970L});
 	public static final BitSet FOLLOW_10_in_relation156 = new BitSet(new long[]{0x0000000000000970L});
 	public static final BitSet FOLLOW_term_in_relation161 = new BitSet(new long[]{0x000000000003E402L});
 	public static final BitSet FOLLOW_relation_in_expression174 = new BitSet(new long[]{0x00000000000C0002L});
 	public static final BitSet FOLLOW_18_in_expression179 = new BitSet(new long[]{0x0000000000000970L});
 	public static final BitSet FOLLOW_19_in_expression184 = new BitSet(new long[]{0x0000000000000970L});
 	public static final BitSet FOLLOW_relation_in_expression188 = new BitSet(new long[]{0x00000000000C0002L});
 }
