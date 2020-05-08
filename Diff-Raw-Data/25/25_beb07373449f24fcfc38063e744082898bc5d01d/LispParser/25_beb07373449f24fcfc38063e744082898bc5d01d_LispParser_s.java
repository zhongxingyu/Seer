// $ANTLR 3.5 /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g 2013-08-15 15:15:45
 
 import java.util.HashMap;
 import java.io.*;
 
 
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings("all")
 public class LispParser extends Parser {
 	public static final String[] tokenNames = new String[] {
 		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ASSN", "EOL", "ID", "LPAR", "MINUS", 
 		"NUM", "PLUS", "PUT", "QUOTIENT", "RPAR", "SETQ", "SPACE", "TIMES"
 	};
 	public static final int EOF=-1;
 	public static final int ASSN=4;
 	public static final int EOL=5;
 	public static final int ID=6;
 	public static final int LPAR=7;
 	public static final int MINUS=8;
 	public static final int NUM=9;
 	public static final int PLUS=10;
 	public static final int PUT=11;
 	public static final int QUOTIENT=12;
 	public static final int RPAR=13;
 	public static final int SETQ=14;
 	public static final int SPACE=15;
 	public static final int TIMES=16;
 
 	// delegates
 	public Parser[] getDelegates() {
 		return new Parser[] {};
 	}
 
 	// delegators
 
 
 	public LispParser(TokenStream input) {
 		this(input, new RecognizerSharedState());
 	}
 	public LispParser(TokenStream input, RecognizerSharedState state) {
 		super(input, state);
 	}
 
 	@Override public String[] getTokenNames() { return LispParser.tokenNames; }
 	@Override public String getGrammarFileName() { return "/Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g"; }
 
 
 	        
 		// private int[] store = new int[26]; // implement hashmap
 		// ... storage for variables 'a', ..., 'z'
 	        HashMap store = new HashMap(); // implementation of hashmap
 
 
 
 	// $ANTLR start "prog"
 	// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:22:1: prog : ( com )* ( EOF | EOL ) ;
 	public final void prog() throws RecognitionException {
 		try {
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:23:2: ( ( com )* ( EOF | EOL ) )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:23:4: ( com )* ( EOF | EOL )
 			{
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:23:4: ( com )*
 			loop1:
 			while (true) {
 				int alt1=2;
 				int LA1_0 = input.LA(1);
 				if ( (LA1_0==LPAR) ) {
 					alt1=1;
 				}
 
 				switch (alt1) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:23:4: com
 					{
 					pushFollow(FOLLOW_com_in_prog27);
 					com();
 					state._fsp--;
 
 					}
 					break;
 
 				default :
 					break loop1;
 				}
 			}
 
 			if ( input.LA(1)==EOF||input.LA(1)==EOL ) {
 				input.consume();
 				state.errorRecovery=false;
 			}
 			else {
 				MismatchedSetException mse = new MismatchedSetException(null,input);
 				throw mse;
 			}
 			}
 
 		}
 		catch (RecognitionException re) {
 			reportError(re);
 			recover(input,re);
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "prog"
 
 
 
 	// $ANTLR start "com"
 	// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:28:1: com : ( '(' PUT v= sexpr ')' EOL | '(' SETQ ID v= sexpr ')' EOL );
 	public final void com() throws RecognitionException {
 		Token ID1=null;
 		int v =0;
 
 		try {
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:29:2: ( '(' PUT v= sexpr ')' EOL | '(' SETQ ID v= sexpr ')' EOL )
 			int alt2=2;
 			int LA2_0 = input.LA(1);
 			if ( (LA2_0==LPAR) ) {
 				int LA2_1 = input.LA(2);
 				if ( (LA2_1==PUT) ) {
 					alt2=1;
 				}
 				else if ( (LA2_1==SETQ) ) {
 					alt2=2;
 				}
 
 				else {
 					int nvaeMark = input.mark();
 					try {
 						input.consume();
 						NoViableAltException nvae =
 							new NoViableAltException("", 2, 1, input);
 						throw nvae;
 					} finally {
 						input.rewind(nvaeMark);
 					}
 				}
 
 			}
 
 			else {
 				NoViableAltException nvae =
 					new NoViableAltException("", 2, 0, input);
 				throw nvae;
 			}
 
 			switch (alt2) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:29:4: '(' PUT v= sexpr ')' EOL
 					{
 					match(input,LPAR,FOLLOW_LPAR_in_com52); 
 					match(input,PUT,FOLLOW_PUT_in_com54); 
 					pushFollow(FOLLOW_sexpr_in_com58);
 					v=sexpr();
 					state._fsp--;
 
 					match(input,RPAR,FOLLOW_RPAR_in_com60); 
 					match(input,EOL,FOLLOW_EOL_in_com62); 
 					 LispIntRun.output.println(v); // PrintWriter
 					                                                try
 					                                                {
 					                                                PrintWriter writerout = null; 
 					                                                writerout = new PrintWriter(new BufferedWriter(new FileWriter("LispOutput", true))); // write to a file and appends the results in case there are more than 1 line
 					                                                writerout.println(v); // LispIntRun.filewriter.println();
 					                                                writerout.close();
 					                                                }
 					                                                catch (IOException ioe)
 					                                                {
 					                                                        System.out.println("File I/O error: ");
 					                                                        ioe.printStackTrace(); // print out details of where exception occurred			
 					                                                }
 					                                              
 					}
 					break;
 				case 2 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:43:4: '(' SETQ ID v= sexpr ')' EOL
 					{
 					match(input,LPAR,FOLLOW_LPAR_in_com75); 
 					match(input,SETQ,FOLLOW_SETQ_in_com77); 
 					ID1=(Token)match(input,ID,FOLLOW_ID_in_com79); 
 					pushFollow(FOLLOW_sexpr_in_com83);
 					v=sexpr();
 					state._fsp--;
 
 					match(input,RPAR,FOLLOW_RPAR_in_com85); 
 					match(input,EOL,FOLLOW_EOL_in_com87); 
 					 
 					                                                    store.put((ID1!=null?ID1.getText():null),new Integer(v));
 					                                                    //Hashmap is composed by: ID (variable name), Numeric Value 
 					                                                  
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
 	}
 	// $ANTLR end "com"
 
 
 
 	// $ANTLR start "sexpr"
 	// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:51:1: sexpr returns [int value] : (v1= term | ( '(' PLUS v1= sexpr v2= sexpr ')' | '(' MINUS v1= sexpr v2= sexpr ')' | '(' TIMES v1= sexpr v2= sexpr ')' | '(' QUOTIENT v1= sexpr v2= sexpr ')' ) );
 	public final int sexpr() throws RecognitionException {
 		int value = 0;
 
 
 		int v1 =0;
 		int v2 =0;
 
 		try {
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:52:2: (v1= term | ( '(' PLUS v1= sexpr v2= sexpr ')' | '(' MINUS v1= sexpr v2= sexpr ')' | '(' TIMES v1= sexpr v2= sexpr ')' | '(' QUOTIENT v1= sexpr v2= sexpr ')' ) )
 			int alt4=2;
 			int LA4_0 = input.LA(1);
 			if ( (LA4_0==ID||LA4_0==NUM) ) {
 				alt4=1;
 			}
 			else if ( (LA4_0==LPAR) ) {
 				int LA4_2 = input.LA(2);
 				if ( (LA4_2==MINUS||LA4_2==PLUS||LA4_2==QUOTIENT||LA4_2==TIMES) ) {
 					alt4=2;
 				}
 				else if ( ((LA4_2 >= ID && LA4_2 <= LPAR)||LA4_2==NUM) ) {
 					alt4=1;
 				}
 
 				else {
 					int nvaeMark = input.mark();
 					try {
 						input.consume();
 						NoViableAltException nvae =
 							new NoViableAltException("", 4, 2, input);
 						throw nvae;
 					} finally {
 						input.rewind(nvaeMark);
 					}
 				}
 
 			}
 
 			else {
 				NoViableAltException nvae =
 					new NoViableAltException("", 4, 0, input);
 				throw nvae;
 			}
 
 			switch (alt4) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:52:4: v1= term
 					{
 					pushFollow(FOLLOW_term_in_sexpr136);
 					v1=term();
 					state._fsp--;
 
 					 value = v1; 
 					}
 					break;
 				case 2 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:53:4: ( '(' PLUS v1= sexpr v2= sexpr ')' | '(' MINUS v1= sexpr v2= sexpr ')' | '(' TIMES v1= sexpr v2= sexpr ')' | '(' QUOTIENT v1= sexpr v2= sexpr ')' )
 					{
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:53:4: ( '(' PLUS v1= sexpr v2= sexpr ')' | '(' MINUS v1= sexpr v2= sexpr ')' | '(' TIMES v1= sexpr v2= sexpr ')' | '(' QUOTIENT v1= sexpr v2= sexpr ')' )
 					int alt3=4;
 					int LA3_0 = input.LA(1);
 					if ( (LA3_0==LPAR) ) {
 						switch ( input.LA(2) ) {
 						case PLUS:
 							{
 							alt3=1;
 							}
 							break;
 						case MINUS:
 							{
 							alt3=2;
 							}
 							break;
 						case TIMES:
 							{
 							alt3=3;
 							}
 							break;
 						case QUOTIENT:
 							{
 							alt3=4;
 							}
 							break;
 						default:
 							int nvaeMark = input.mark();
 							try {
 								input.consume();
 								NoViableAltException nvae =
 									new NoViableAltException("", 3, 1, input);
 								throw nvae;
 							} finally {
 								input.rewind(nvaeMark);
 							}
 						}
 					}
 
 					else {
 						NoViableAltException nvae =
 							new NoViableAltException("", 3, 0, input);
 						throw nvae;
 					}
 
 					switch (alt3) {
 						case 1 :
 							// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:53:5: '(' PLUS v1= sexpr v2= sexpr ')'
 							{
 							match(input,LPAR,FOLLOW_LPAR_in_sexpr157); 
 							match(input,PLUS,FOLLOW_PLUS_in_sexpr175); 
 							pushFollow(FOLLOW_sexpr_in_sexpr179);
 							v1=sexpr();
 							state._fsp--;
 
 							pushFollow(FOLLOW_sexpr_in_sexpr183);
 							v2=sexpr();
 							state._fsp--;
 
 							match(input,RPAR,FOLLOW_RPAR_in_sexpr185); 
 							value = v1 + v2;
 							}
 							break;
 						case 2 :
 							// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:55:19: '(' MINUS v1= sexpr v2= sexpr ')'
 							{
 							match(input,LPAR,FOLLOW_LPAR_in_sexpr207); 
 							match(input,MINUS,FOLLOW_MINUS_in_sexpr209); 
 							pushFollow(FOLLOW_sexpr_in_sexpr213);
 							v1=sexpr();
 							state._fsp--;
 
 							pushFollow(FOLLOW_sexpr_in_sexpr217);
 							v2=sexpr();
 							state._fsp--;
 
 							match(input,RPAR,FOLLOW_RPAR_in_sexpr219); 
 							value = v1 - v2;
 							}
 							break;
 						case 3 :
 							// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:56:19: '(' TIMES v1= sexpr v2= sexpr ')'
 							{
 							match(input,LPAR,FOLLOW_LPAR_in_sexpr242); 
 							match(input,TIMES,FOLLOW_TIMES_in_sexpr244); 
 							pushFollow(FOLLOW_sexpr_in_sexpr248);
 							v1=sexpr();
 							state._fsp--;
 
 							pushFollow(FOLLOW_sexpr_in_sexpr252);
 							v2=sexpr();
 							state._fsp--;
 
 							match(input,RPAR,FOLLOW_RPAR_in_sexpr254); 
 							value = v1 * v2;
 							}
 							break;
 						case 4 :
 							// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:57:19: '(' QUOTIENT v1= sexpr v2= sexpr ')'
 							{
 							match(input,LPAR,FOLLOW_LPAR_in_sexpr276); 
 							match(input,QUOTIENT,FOLLOW_QUOTIENT_in_sexpr278); 
 							pushFollow(FOLLOW_sexpr_in_sexpr282);
 							v1=sexpr();
 							state._fsp--;
 
 							pushFollow(FOLLOW_sexpr_in_sexpr286);
 							v2=sexpr();
 							state._fsp--;
 
 							match(input,RPAR,FOLLOW_RPAR_in_sexpr288); 
 							value = v1 / v2;
 							}
 							break;
 
 					}
 
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
 		return value;
 	}
 	// $ANTLR end "sexpr"
 
 
 
 	// $ANTLR start "term"
 	// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:61:1: term returns [int value] : ( NUM | ID | '(' v= sexpr ')' );
 	public final int term() throws RecognitionException {
 		int value = 0;
 
 
 		Token NUM2=null;
 		Token ID3=null;
 		int v =0;
 
 		try {
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:62:2: ( NUM | ID | '(' v= sexpr ')' )
 			int alt5=3;
 			switch ( input.LA(1) ) {
 			case NUM:
 				{
 				alt5=1;
 				}
 				break;
 			case ID:
 				{
 				alt5=2;
 				}
 				break;
 			case LPAR:
 				{
 				alt5=3;
 				}
 				break;
 			default:
 				NoViableAltException nvae =
 					new NoViableAltException("", 5, 0, input);
 				throw nvae;
 			}
 			switch (alt5) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:62:4: NUM
 					{
 					NUM2=(Token)match(input,NUM,FOLLOW_NUM_in_term345); 
 					 value = Integer.parseInt(
 							                         (NUM2!=null?NUM2.getText():null)); 
 					}
 					break;
 				case 2 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:64:4: ID
 					{
 					ID3=(Token)match(input,ID,FOLLOW_ID_in_term369); 
 					  
 					                                       Integer v3 = (Integer)store.get((ID3!=null?ID3.getText():null));
 					                                       if ( v3!=null ) value = v3.intValue();
 					                                       else System.err.println("undefined variable "+(ID3!=null?ID3.getText():null));
 					                                      
 					}
 					break;
 				case 3 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:69:4: '(' v= sexpr ')'
 					{
 					match(input,LPAR,FOLLOW_LPAR_in_term394); 
 					pushFollow(FOLLOW_sexpr_in_term398);
 					v=sexpr();
 					state._fsp--;
 
 					match(input,RPAR,FOLLOW_RPAR_in_term400); 
 					 value = v; 
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
 		return value;
 	}
 	// $ANTLR end "term"
 
 	// Delegated rules
 
 
 
 	public static final BitSet FOLLOW_com_in_prog27 = new BitSet(new long[]{0x00000000000000A0L});
 	public static final BitSet FOLLOW_set_in_prog30 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_com52 = new BitSet(new long[]{0x0000000000000800L});
 	public static final BitSet FOLLOW_PUT_in_com54 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_com58 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_com60 = new BitSet(new long[]{0x0000000000000020L});
 	public static final BitSet FOLLOW_EOL_in_com62 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_com75 = new BitSet(new long[]{0x0000000000004000L});
 	public static final BitSet FOLLOW_SETQ_in_com77 = new BitSet(new long[]{0x0000000000000040L});
 	public static final BitSet FOLLOW_ID_in_com79 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_com83 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_com85 = new BitSet(new long[]{0x0000000000000020L});
 	public static final BitSet FOLLOW_EOL_in_com87 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_term_in_sexpr136 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_sexpr157 = new BitSet(new long[]{0x0000000000000400L});
 	public static final BitSet FOLLOW_PLUS_in_sexpr175 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_sexpr179 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_sexpr183 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_sexpr185 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_sexpr207 = new BitSet(new long[]{0x0000000000000100L});
 	public static final BitSet FOLLOW_MINUS_in_sexpr209 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_sexpr213 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_sexpr217 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_sexpr219 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_sexpr242 = new BitSet(new long[]{0x0000000000010000L});
 	public static final BitSet FOLLOW_TIMES_in_sexpr244 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_sexpr248 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_sexpr252 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_sexpr254 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_sexpr276 = new BitSet(new long[]{0x0000000000001000L});
 	public static final BitSet FOLLOW_QUOTIENT_in_sexpr278 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_sexpr282 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_sexpr286 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_sexpr288 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_NUM_in_term345 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_ID_in_term369 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_term394 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_sexpr_in_term398 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_term400 = new BitSet(new long[]{0x0000000000000002L});
 }
