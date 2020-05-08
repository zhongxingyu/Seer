// $ANTLR 3.5 /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g 2013-08-28 10:23:25
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings("all")
 public class LispLexer extends Lexer {
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
 	// delegators
 	public Lexer[] getDelegates() {
 		return new Lexer[] {};
 	}
 
 	public LispLexer() {} 
 	public LispLexer(CharStream input) {
 		this(input, new RecognizerSharedState());
 	}
 	public LispLexer(CharStream input, RecognizerSharedState state) {
 		super(input,state);
 	}
 	@Override public String getGrammarFileName() { return "/Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g"; }
 
 	// $ANTLR start "PUT"
 	public final void mPUT() throws RecognitionException {
 		try {
 			int _type = PUT;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:85:5: ( 'put' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:85:7: 'put'
 			{
 			match("put"); 
 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "PUT"
 
 	// $ANTLR start "SETQ"
 	public final void mSETQ() throws RecognitionException {
 		try {
 			int _type = SETQ;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:86:6: ( 'setq' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:86:8: 'setq'
 			{
 			match("setq"); 
 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "SETQ"
 
 	// $ANTLR start "ASSN"
 	public final void mASSN() throws RecognitionException {
 		try {
 			int _type = ASSN;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:88:6: ( '=' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:88:8: '='
 			{
 			match('='); 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "ASSN"
 
 	// $ANTLR start "PLUS"
 	public final void mPLUS() throws RecognitionException {
 		try {
 			int _type = PLUS;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:89:6: ( '+' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:89:8: '+'
 			{
 			match('+'); 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "PLUS"
 
 	// $ANTLR start "MINUS"
 	public final void mMINUS() throws RecognitionException {
 		try {
 			int _type = MINUS;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:90:7: ( '-' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:90:9: '-'
 			{
 			match('-'); 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "MINUS"
 
 	// $ANTLR start "TIMES"
 	public final void mTIMES() throws RecognitionException {
 		try {
 			int _type = TIMES;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:91:7: ( '*' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:91:9: '*'
 			{
 			match('*'); 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "TIMES"
 
 	// $ANTLR start "QUOTIENT"
 	public final void mQUOTIENT() throws RecognitionException {
 		try {
 			int _type = QUOTIENT;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:92:13: ( '/' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:92:17: '/'
 			{
 			match('/'); 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "QUOTIENT"
 
 	// $ANTLR start "LPAR"
 	public final void mLPAR() throws RecognitionException {
 		try {
 			int _type = LPAR;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:94:6: ( '(' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:94:8: '('
 			{
 			match('('); 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "LPAR"
 
 	// $ANTLR start "RPAR"
 	public final void mRPAR() throws RecognitionException {
 		try {
 			int _type = RPAR;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:95:6: ( ')' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:95:8: ')'
 			{
 			match(')'); 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "RPAR"
 
 	// $ANTLR start "ID"
 	public final void mID() throws RecognitionException {
 		try {
 			int _type = ID;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:97:4: ( ( 'a' .. 'z' | 'A' .. 'Z' )+ )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:97:6: ( 'a' .. 'z' | 'A' .. 'Z' )+
 			{
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:97:6: ( 'a' .. 'z' | 'A' .. 'Z' )+
 			int cnt1=0;
 			loop1:
 			while (true) {
 				int alt1=2;
 				int LA1_0 = input.LA(1);
 				if ( ((LA1_0 >= 'A' && LA1_0 <= 'Z')||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
 					alt1=1;
 				}
 
 				switch (alt1) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:
 					{
 					if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
 						input.consume();
 					}
 					else {
 						MismatchedSetException mse = new MismatchedSetException(null,input);
 						recover(mse);
 						throw mse;
 					}
 					}
 					break;
 
 				default :
 					if ( cnt1 >= 1 ) break loop1;
 					EarlyExitException eee = new EarlyExitException(1, input);
 					throw eee;
 				}
 				cnt1++;
 			}
 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "ID"
 
 	// $ANTLR start "NUM"
 	public final void mNUM() throws RecognitionException {
 		try {
 			int _type = NUM;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:98:5: ( ( '0' .. '9' )+ )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:98:7: ( '0' .. '9' )+
 			{
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:98:7: ( '0' .. '9' )+
 			int cnt2=0;
 			loop2:
 			while (true) {
 				int alt2=2;
 				int LA2_0 = input.LA(1);
 				if ( ((LA2_0 >= '0' && LA2_0 <= '9')) ) {
 					alt2=1;
 				}
 
 				switch (alt2) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:
 					{
 					if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
 						input.consume();
 					}
 					else {
 						MismatchedSetException mse = new MismatchedSetException(null,input);
 						recover(mse);
 						throw mse;
 					}
 					}
 					break;
 
 				default :
 					if ( cnt2 >= 1 ) break loop2;
 					EarlyExitException eee = new EarlyExitException(2, input);
 					throw eee;
 				}
 				cnt2++;
 			}
 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "NUM"
 
 	// $ANTLR start "EOL"
 	public final void mEOL() throws RecognitionException {
 		try {
 			int _type = EOL;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:100:5: ( ( '\\r' )? '\\n' )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:100:7: ( '\\r' )? '\\n'
 			{
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:100:7: ( '\\r' )?
 			int alt3=2;
 			int LA3_0 = input.LA(1);
 			if ( (LA3_0=='\r') ) {
 				alt3=1;
 			}
 			switch (alt3) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:100:7: '\\r'
 					{
 					match('\r'); 
 					}
 					break;
 
 			}
 
 			match('\n'); 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "EOL"
 
 	// $ANTLR start "SPACE"
 	public final void mSPACE() throws RecognitionException {
 		try {
 			int _type = SPACE;
 			int _channel = DEFAULT_TOKEN_CHANNEL;
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:102:7: ( ( ' ' | '\\t' )+ )
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:102:9: ( ' ' | '\\t' )+
 			{
 			// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:102:9: ( ' ' | '\\t' )+
 			int cnt4=0;
 			loop4:
 			while (true) {
 				int alt4=2;
 				int LA4_0 = input.LA(1);
 				if ( (LA4_0=='\t'||LA4_0==' ') ) {
 					alt4=1;
 				}
 
 				switch (alt4) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:
 					{
 					if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
 						input.consume();
 					}
 					else {
 						MismatchedSetException mse = new MismatchedSetException(null,input);
 						recover(mse);
 						throw mse;
 					}
 					}
 					break;
 
 				default :
 					if ( cnt4 >= 1 ) break loop4;
 					EarlyExitException eee = new EarlyExitException(4, input);
 					throw eee;
 				}
 				cnt4++;
 			}
 
 			 skip(); 
 			}
 
 			state.type = _type;
 			state.channel = _channel;
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "SPACE"
 
 	@Override
 	public void mTokens() throws RecognitionException {
 		// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:8: ( PUT | SETQ | ASSN | PLUS | MINUS | TIMES | QUOTIENT | LPAR | RPAR | ID | NUM | EOL | SPACE )
 		int alt5=13;
 		switch ( input.LA(1) ) {
 		case 'p':
 			{
 			int LA5_1 = input.LA(2);
 			if ( (LA5_1=='u') ) {
 				int LA5_14 = input.LA(3);
 				if ( (LA5_14=='t') ) {
 					int LA5_16 = input.LA(4);
 					if ( ((LA5_16 >= 'A' && LA5_16 <= 'Z')||(LA5_16 >= 'a' && LA5_16 <= 'z')) ) {
 						alt5=10;
 					}
 
 					else {
 						alt5=1;
 					}
 
 				}
 
 				else {
 					alt5=10;
 				}
 
 			}
 
 			else {
 				alt5=10;
 			}
 
 			}
 			break;
 		case 's':
 			{
 			int LA5_2 = input.LA(2);
 			if ( (LA5_2=='e') ) {
 				int LA5_15 = input.LA(3);
 				if ( (LA5_15=='t') ) {
 					int LA5_17 = input.LA(4);
 					if ( (LA5_17=='q') ) {
 						int LA5_19 = input.LA(5);
 						if ( ((LA5_19 >= 'A' && LA5_19 <= 'Z')||(LA5_19 >= 'a' && LA5_19 <= 'z')) ) {
 							alt5=10;
 						}
 
 						else {
 							alt5=2;
 						}
 
 					}
 
 					else {
 						alt5=10;
 					}
 
 				}
 
 				else {
 					alt5=10;
 				}
 
 			}
 
 			else {
 				alt5=10;
 			}
 
 			}
 			break;
 		case '=':
 			{
 			alt5=3;
 			}
 			break;
 		case '+':
 			{
 			alt5=4;
 			}
 			break;
 		case '-':
 			{
 			alt5=5;
 			}
 			break;
 		case '*':
 			{
 			alt5=6;
 			}
 			break;
 		case '/':
 			{
 			alt5=7;
 			}
 			break;
 		case '(':
 			{
 			alt5=8;
 			}
 			break;
 		case ')':
 			{
 			alt5=9;
 			}
 			break;
 		case 'A':
 		case 'B':
 		case 'C':
 		case 'D':
 		case 'E':
 		case 'F':
 		case 'G':
 		case 'H':
 		case 'I':
 		case 'J':
 		case 'K':
 		case 'L':
 		case 'M':
 		case 'N':
 		case 'O':
 		case 'P':
 		case 'Q':
 		case 'R':
 		case 'S':
 		case 'T':
 		case 'U':
 		case 'V':
 		case 'W':
 		case 'X':
 		case 'Y':
 		case 'Z':
 		case 'a':
 		case 'b':
 		case 'c':
 		case 'd':
 		case 'e':
 		case 'f':
 		case 'g':
 		case 'h':
 		case 'i':
 		case 'j':
 		case 'k':
 		case 'l':
 		case 'm':
 		case 'n':
 		case 'o':
 		case 'q':
 		case 'r':
 		case 't':
 		case 'u':
 		case 'v':
 		case 'w':
 		case 'x':
 		case 'y':
 		case 'z':
 			{
 			alt5=10;
 			}
 			break;
 		case '0':
 		case '1':
 		case '2':
 		case '3':
 		case '4':
 		case '5':
 		case '6':
 		case '7':
 		case '8':
 		case '9':
 			{
 			alt5=11;
 			}
 			break;
 		case '\n':
 		case '\r':
 			{
 			alt5=12;
 			}
 			break;
 		case '\t':
 		case ' ':
 			{
 			alt5=13;
 			}
 			break;
 		default:
 			NoViableAltException nvae =
 				new NoViableAltException("", 5, 0, input);
 			throw nvae;
 		}
 		switch (alt5) {
 			case 1 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:10: PUT
 				{
 				mPUT(); 
 
 				}
 				break;
 			case 2 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:14: SETQ
 				{
 				mSETQ(); 
 
 				}
 				break;
 			case 3 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:19: ASSN
 				{
 				mASSN(); 
 
 				}
 				break;
 			case 4 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:24: PLUS
 				{
 				mPLUS(); 
 
 				}
 				break;
 			case 5 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:29: MINUS
 				{
 				mMINUS(); 
 
 				}
 				break;
 			case 6 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:35: TIMES
 				{
 				mTIMES(); 
 
 				}
 				break;
 			case 7 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:41: QUOTIENT
 				{
 				mQUOTIENT(); 
 
 				}
 				break;
 			case 8 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:50: LPAR
 				{
 				mLPAR(); 
 
 				}
 				break;
 			case 9 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:55: RPAR
 				{
 				mRPAR(); 
 
 				}
 				break;
 			case 10 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:60: ID
 				{
 				mID(); 
 
 				}
 				break;
 			case 11 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:63: NUM
 				{
 				mNUM(); 
 
 				}
 				break;
 			case 12 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:67: EOL
 				{
 				mEOL(); 
 
 				}
 				break;
 			case 13 :
 				// /Users/jorgejaso/NetBeansProjects/LispInt_Arith/src/Lisp.g:1:71: SPACE
 				{
 				mSPACE(); 
 
 				}
 				break;
 
 		}
 	}
 
 
 
 }
