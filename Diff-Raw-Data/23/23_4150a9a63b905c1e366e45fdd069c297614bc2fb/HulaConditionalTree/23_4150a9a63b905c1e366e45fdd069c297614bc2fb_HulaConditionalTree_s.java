// $ANTLR 3.5 com/hula/lang/conditional/HulaConditionalTree.g 2013-04-08 18:27:05
 
 	package com.hula.lang.conditional;
 	
 	import com.hula.lang.runtime.RuntimeConnector;
 	import java.math.BigDecimal;
 
 
 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
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
 public class HulaConditionalTree extends TreeParser {
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
 	public TreeParser[] getDelegates() {
 		return new TreeParser[] {};
 	}
 
 	// delegators
 
 
 	public HulaConditionalTree(TreeNodeStream input) {
 		this(input, new RecognizerSharedState());
 	}
 	public HulaConditionalTree(TreeNodeStream input, RecognizerSharedState state) {
 		super(input, state);
 	}
 
 	@Override public String[] getTokenNames() { return HulaConditionalTree.tokenNames; }
 	@Override public String getGrammarFileName() { return "com/hula/lang/conditional/HulaConditionalTree.g"; }
 
 
 
 		private ThreadLocal<RuntimeConnector> rcContainer = new ThreadLocal<RuntimeConnector>();
 		public void setRuntimeConnector(RuntimeConnector connector)
 		{
 			rcContainer.set(connector);
 		}
 		private RuntimeConnector getRuntimeConnector()
 		{
 			return rcContainer.get();
 		}
 		
 
 
 
 	// $ANTLR start "test"
 	// com/hula/lang/conditional/HulaConditionalTree.g:46:1: test returns [Object result] : e= expression EOF ;
 	public final Object test() throws RecognitionException {
 		Object result = null;
 
 
 		Object e =null;
 
 		try {
 			// com/hula/lang/conditional/HulaConditionalTree.g:47:2: (e= expression EOF )
 			// com/hula/lang/conditional/HulaConditionalTree.g:47:4: e= expression EOF
 			{
 			pushFollow(FOLLOW_expression_in_test58);
 			e=expression();
 			state._fsp--;
 
 			match(input,EOF,FOLLOW_EOF_in_test60); 
 			 result = e; 
 			}
 
 		}
 		catch (RecognitionException re) {
 			reportError(re);
 			recover(input,re);
 		}
 		finally {
 			// do for sure before leaving
 		}
 		return result;
 	}
 	// $ANTLR end "test"
 
 
 
 	// $ANTLR start "expression"
 	// com/hula/lang/conditional/HulaConditionalTree.g:50:1: expression returns [Object result] : ( ^( 'AND' op1= expression op2= expression ) | ^( 'OR' op1= expression op2= expression ) | ^( '=' op1= expression op2= expression ) | ^( '>' op1= expression op2= expression ) | ^( '<' op1= expression op2= expression ) | ^( '>=' op1= expression op2= expression ) | ^( '<=' op1= expression op2= expression ) | ^( '!=' op1= expression op2= expression ) | IDENT | STRING | NUMBER | BOOLEAN );
 	public final Object expression() throws RecognitionException {
 		Object result = null;
 
 
 		CommonTree IDENT1=null;
 		CommonTree STRING2=null;
 		CommonTree NUMBER3=null;
 		CommonTree BOOLEAN4=null;
 		Object op1 =null;
 		Object op2 =null;
 
 		try {
 			// com/hula/lang/conditional/HulaConditionalTree.g:51:2: ( ^( 'AND' op1= expression op2= expression ) | ^( 'OR' op1= expression op2= expression ) | ^( '=' op1= expression op2= expression ) | ^( '>' op1= expression op2= expression ) | ^( '<' op1= expression op2= expression ) | ^( '>=' op1= expression op2= expression ) | ^( '<=' op1= expression op2= expression ) | ^( '!=' op1= expression op2= expression ) | IDENT | STRING | NUMBER | BOOLEAN )
 			int alt1=12;
 			switch ( input.LA(1) ) {
 			case 18:
 				{
 				alt1=1;
 				}
 				break;
 			case 19:
 				{
 				alt1=2;
 				}
 				break;
 			case 15:
 				{
 				alt1=3;
 				}
 				break;
 			case 16:
 				{
 				alt1=4;
 				}
 				break;
 			case 13:
 				{
 				alt1=5;
 				}
 				break;
 			case 17:
 				{
 				alt1=6;
 				}
 				break;
 			case 14:
 				{
 				alt1=7;
 				}
 				break;
 			case 10:
 				{
 				alt1=8;
 				}
 				break;
 			case IDENT:
 				{
 				alt1=9;
 				}
 				break;
 			case STRING:
 				{
 				alt1=10;
 				}
 				break;
 			case NUMBER:
 				{
 				alt1=11;
 				}
 				break;
 			case BOOLEAN:
 				{
 				alt1=12;
 				}
 				break;
 			default:
 				NoViableAltException nvae =
 					new NoViableAltException("", 1, 0, input);
 				throw nvae;
 			}
 			switch (alt1) {
 				case 1 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:51:4: ^( 'AND' op1= expression op2= expression )
 					{
 					match(input,18,FOLLOW_18_in_expression79); 
 					match(input, Token.DOWN, null); 
 					pushFollow(FOLLOW_expression_in_expression83);
 					op1=expression();
 					state._fsp--;
 
 					pushFollow(FOLLOW_expression_in_expression87);
 					op2=expression();
 					state._fsp--;
 
 					match(input, Token.UP, null); 
 
 					 result = OperatorUtil.testAND(op1, op2, getRuntimeConnector()); 
 					}
 					break;
 				case 2 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:52:4: ^( 'OR' op1= expression op2= expression )
 					{
 					match(input,19,FOLLOW_19_in_expression96); 
 					match(input, Token.DOWN, null); 
 					pushFollow(FOLLOW_expression_in_expression100);
 					op1=expression();
 					state._fsp--;
 
 					pushFollow(FOLLOW_expression_in_expression104);
 					op2=expression();
 					state._fsp--;
 
 					match(input, Token.UP, null); 
 
 					 result = OperatorUtil.testOR(op1, op2, getRuntimeConnector()); 
 					}
 					break;
 				case 3 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:53:4: ^( '=' op1= expression op2= expression )
 					{
 					match(input,15,FOLLOW_15_in_expression113); 
 					match(input, Token.DOWN, null); 
 					pushFollow(FOLLOW_expression_in_expression117);
 					op1=expression();
 					state._fsp--;
 
 					pushFollow(FOLLOW_expression_in_expression121);
 					op2=expression();
 					state._fsp--;
 
 					match(input, Token.UP, null); 
 
 					 result = OperatorUtil.testEquals(op1, op2, getRuntimeConnector()); 
 					}
 					break;
 				case 4 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:54:4: ^( '>' op1= expression op2= expression )
 					{
 					match(input,16,FOLLOW_16_in_expression130); 
 					match(input, Token.DOWN, null); 
 					pushFollow(FOLLOW_expression_in_expression134);
 					op1=expression();
 					state._fsp--;
 
 					pushFollow(FOLLOW_expression_in_expression138);
 					op2=expression();
 					state._fsp--;
 
 					match(input, Token.UP, null); 
 
 					 result = OperatorUtil.testGreaterThan(op1, op2, getRuntimeConnector()); 
 					}
 					break;
 				case 5 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:55:4: ^( '<' op1= expression op2= expression )
 					{
 					match(input,13,FOLLOW_13_in_expression147); 
 					match(input, Token.DOWN, null); 
 					pushFollow(FOLLOW_expression_in_expression151);
 					op1=expression();
 					state._fsp--;
 
 					pushFollow(FOLLOW_expression_in_expression155);
 					op2=expression();
 					state._fsp--;
 
 					match(input, Token.UP, null); 
 
 					 result = OperatorUtil.testLessThan(op1, op2, getRuntimeConnector()); 
 					}
 					break;
 				case 6 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:56:4: ^( '>=' op1= expression op2= expression )
 					{
 					match(input,17,FOLLOW_17_in_expression164); 
 					match(input, Token.DOWN, null); 
 					pushFollow(FOLLOW_expression_in_expression168);
 					op1=expression();
 					state._fsp--;
 
 					pushFollow(FOLLOW_expression_in_expression172);
 					op2=expression();
 					state._fsp--;
 
 					match(input, Token.UP, null); 
 
 					 result = OperatorUtil.testGreaterThanEqualTo(op1, op2, getRuntimeConnector()); 
 					}
 					break;
 				case 7 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:57:4: ^( '<=' op1= expression op2= expression )
 					{
 					match(input,14,FOLLOW_14_in_expression181); 
 					match(input, Token.DOWN, null); 
 					pushFollow(FOLLOW_expression_in_expression185);
 					op1=expression();
 					state._fsp--;
 
 					pushFollow(FOLLOW_expression_in_expression189);
 					op2=expression();
 					state._fsp--;
 
 					match(input, Token.UP, null); 
 
 					 result = OperatorUtil.testLessThanEqualTo(op1, op2, getRuntimeConnector()); 
 					}
 					break;
 				case 8 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:58:4: ^( '!=' op1= expression op2= expression )
 					{
 					match(input,10,FOLLOW_10_in_expression198); 
 					match(input, Token.DOWN, null); 
 					pushFollow(FOLLOW_expression_in_expression202);
 					op1=expression();
 					state._fsp--;
 
 					pushFollow(FOLLOW_expression_in_expression206);
 					op2=expression();
 					state._fsp--;
 
 					match(input, Token.UP, null); 
 
 					 result = OperatorUtil.testNotEquals(op1, op2, getRuntimeConnector()); 
 					}
 					break;
 				case 9 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:59:4: IDENT
 					{
 					IDENT1=(CommonTree)match(input,IDENT,FOLLOW_IDENT_in_expression214); 
 					 result = new VariableReference((IDENT1!=null?IDENT1.getText():null)); 
 					}
 					break;
 				case 10 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:60:4: STRING
 					{
 					STRING2=(CommonTree)match(input,STRING,FOLLOW_STRING_in_expression221); 
 					 result = new String((STRING2!=null?STRING2.getText():null)); 
 					}
 					break;
 				case 11 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:61:4: NUMBER
 					{
 					NUMBER3=(CommonTree)match(input,NUMBER,FOLLOW_NUMBER_in_expression228); 
 					 result = new BigDecimal((NUMBER3!=null?NUMBER3.getText():null)); 
 					}
 					break;
 				case 12 :
 					// com/hula/lang/conditional/HulaConditionalTree.g:62:4: BOOLEAN
 					{
 					BOOLEAN4=(CommonTree)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_expression235); 
 					 result = new Boolean((BOOLEAN4!=null?BOOLEAN4.getText():null)); 
 					}
 					break;
 
 			}
 		}
 		catch (RecognitionException re) {
 			reportError(re);
 			recover(input,re);
 		}
 		finally {
 			// do for sure before leaving
 		}
 		return result;
 	}
 	// $ANTLR end "expression"
 
 	// Delegated rules
 
 
 
 	public static final BitSet FOLLOW_expression_in_test58 = new BitSet(new long[]{0x0000000000000000L});
 	public static final BitSet FOLLOW_EOF_in_test60 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_18_in_expression79 = new BitSet(new long[]{0x0000000000000004L});
 	public static final BitSet FOLLOW_expression_in_expression83 = new BitSet(new long[]{0x00000000000FE570L});
 	public static final BitSet FOLLOW_expression_in_expression87 = new BitSet(new long[]{0x0000000000000008L});
 	public static final BitSet FOLLOW_19_in_expression96 = new BitSet(new long[]{0x0000000000000004L});
 	public static final BitSet FOLLOW_expression_in_expression100 = new BitSet(new long[]{0x00000000000FE570L});
 	public static final BitSet FOLLOW_expression_in_expression104 = new BitSet(new long[]{0x0000000000000008L});
 	public static final BitSet FOLLOW_15_in_expression113 = new BitSet(new long[]{0x0000000000000004L});
 	public static final BitSet FOLLOW_expression_in_expression117 = new BitSet(new long[]{0x00000000000FE570L});
 	public static final BitSet FOLLOW_expression_in_expression121 = new BitSet(new long[]{0x0000000000000008L});
 	public static final BitSet FOLLOW_16_in_expression130 = new BitSet(new long[]{0x0000000000000004L});
 	public static final BitSet FOLLOW_expression_in_expression134 = new BitSet(new long[]{0x00000000000FE570L});
 	public static final BitSet FOLLOW_expression_in_expression138 = new BitSet(new long[]{0x0000000000000008L});
 	public static final BitSet FOLLOW_13_in_expression147 = new BitSet(new long[]{0x0000000000000004L});
 	public static final BitSet FOLLOW_expression_in_expression151 = new BitSet(new long[]{0x00000000000FE570L});
 	public static final BitSet FOLLOW_expression_in_expression155 = new BitSet(new long[]{0x0000000000000008L});
 	public static final BitSet FOLLOW_17_in_expression164 = new BitSet(new long[]{0x0000000000000004L});
 	public static final BitSet FOLLOW_expression_in_expression168 = new BitSet(new long[]{0x00000000000FE570L});
 	public static final BitSet FOLLOW_expression_in_expression172 = new BitSet(new long[]{0x0000000000000008L});
 	public static final BitSet FOLLOW_14_in_expression181 = new BitSet(new long[]{0x0000000000000004L});
 	public static final BitSet FOLLOW_expression_in_expression185 = new BitSet(new long[]{0x00000000000FE570L});
 	public static final BitSet FOLLOW_expression_in_expression189 = new BitSet(new long[]{0x0000000000000008L});
 	public static final BitSet FOLLOW_10_in_expression198 = new BitSet(new long[]{0x0000000000000004L});
 	public static final BitSet FOLLOW_expression_in_expression202 = new BitSet(new long[]{0x00000000000FE570L});
 	public static final BitSet FOLLOW_expression_in_expression206 = new BitSet(new long[]{0x0000000000000008L});
 	public static final BitSet FOLLOW_IDENT_in_expression214 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_STRING_in_expression221 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_NUMBER_in_expression228 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_BOOLEAN_in_expression235 = new BitSet(new long[]{0x0000000000000002L});
 }
