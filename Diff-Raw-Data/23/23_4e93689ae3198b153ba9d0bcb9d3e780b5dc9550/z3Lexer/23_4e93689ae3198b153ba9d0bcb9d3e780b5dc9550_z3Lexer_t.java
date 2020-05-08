// $ANTLR 3.4 ../src/grammar/z3.g 2012-01-18 11:45:52
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class z3Lexer extends Lexer {
     public static final int EOF=-1;
     public static final int T__14=14;
     public static final int T__15=15;
     public static final int T__16=16;
     public static final int T__17=17;
     public static final int T__18=18;
     public static final int T__19=19;
     public static final int T__20=20;
     public static final int T__21=21;
     public static final int T__22=22;
     public static final int T__23=23;
     public static final int T__24=24;
     public static final int T__25=25;
     public static final int T__26=26;
     public static final int T__27=27;
     public static final int T__28=28;
     public static final int T__29=29;
     public static final int BOOL=4;
     public static final int CHAR=5;
     public static final int ESC_SEQ=6;
     public static final int HEX_DIGIT=7;
     public static final int IDENT=8;
     public static final int INT=9;
     public static final int OCTAL_ESC=10;
     public static final int TYPE=11;
     public static final int UNICODE_ESC=12;
     public static final int WS=13;
 
     // delegates
     // delegators
     public Lexer[] getDelegates() {
         return new Lexer[] {};
     }
 
     public z3Lexer() {} 
     public z3Lexer(CharStream input) {
         this(input, new RecognizerSharedState());
     }
     public z3Lexer(CharStream input, RecognizerSharedState state) {
         super(input,state);
     }
     public String getGrammarFileName() { return "../src/grammar/z3.g"; }
 
     // $ANTLR start "T__14"
     public final void mT__14() throws RecognitionException {
         try {
             int _type = T__14;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:2:7: ( '!' )
             // ../src/grammar/z3.g:2:9: '!'
             {
             match('!'); 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__14"
 
     // $ANTLR start "T__15"
     public final void mT__15() throws RecognitionException {
         try {
             int _type = T__15;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:3:7: ( '(' )
             // ../src/grammar/z3.g:3:9: '('
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
     // $ANTLR end "T__15"
 
     // $ANTLR start "T__16"
     public final void mT__16() throws RecognitionException {
         try {
             int _type = T__16;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:4:7: ( '()' )
             // ../src/grammar/z3.g:4:9: '()'
             {
             match("()"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__16"
 
     // $ANTLR start "T__17"
     public final void mT__17() throws RecognitionException {
         try {
             int _type = T__17;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:5:7: ( '(=' )
             // ../src/grammar/z3.g:5:9: '(='
             {
             match("(="); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__17"
 
     // $ANTLR start "T__18"
     public final void mT__18() throws RecognitionException {
         try {
             int _type = T__18;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:6:7: ( '(Array' )
             // ../src/grammar/z3.g:6:9: '(Array'
             {
             match("(Array"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__18"
 
     // $ANTLR start "T__19"
     public final void mT__19() throws RecognitionException {
         try {
             int _type = T__19;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:7:7: ( '(_as-array' )
             // ../src/grammar/z3.g:7:9: '(_as-array'
             {
             match("(_as-array"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__19"
 
     // $ANTLR start "T__20"
     public final void mT__20() throws RecognitionException {
         try {
             int _type = T__20;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:8:7: ( '(and' )
             // ../src/grammar/z3.g:8:9: '(and'
             {
             match("(and"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__20"
 
     // $ANTLR start "T__21"
     public final void mT__21() throws RecognitionException {
         try {
             int _type = T__21;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:9:7: ( '(define-fun' )
             // ../src/grammar/z3.g:9:9: '(define-fun'
             {
             match("(define-fun"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__21"
 
     // $ANTLR start "T__22"
     public final void mT__22() throws RecognitionException {
         try {
             int _type = T__22;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:10:7: ( '(model' )
             // ../src/grammar/z3.g:10:9: '(model'
             {
             match("(model"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__22"
 
     // $ANTLR start "T__23"
     public final void mT__23() throws RecognitionException {
         try {
             int _type = T__23;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:11:7: ( ')' )
             // ../src/grammar/z3.g:11:9: ')'
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
     // $ANTLR end "T__23"
 
     // $ANTLR start "T__24"
     public final void mT__24() throws RecognitionException {
         try {
             int _type = T__24;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:12:7: ( '))' )
             // ../src/grammar/z3.g:12:9: '))'
             {
             match("))"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__24"
 
     // $ANTLR start "T__25"
     public final void mT__25() throws RecognitionException {
         try {
             int _type = T__25;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:13:7: ( 'Int' )
             // ../src/grammar/z3.g:13:9: 'Int'
             {
             match("Int"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__25"
 
     // $ANTLR start "T__26"
     public final void mT__26() throws RecognitionException {
         try {
             int _type = T__26;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:14:7: ( 'define-fun' )
             // ../src/grammar/z3.g:14:9: 'define-fun'
             {
             match("define-fun"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__26"
 
     // $ANTLR start "T__27"
     public final void mT__27() throws RecognitionException {
         try {
             int _type = T__27;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:15:7: ( 'sat' )
             // ../src/grammar/z3.g:15:9: 'sat'
             {
             match("sat"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__27"
 
     // $ANTLR start "T__28"
     public final void mT__28() throws RecognitionException {
         try {
             int _type = T__28;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:16:7: ( 'unknown' )
             // ../src/grammar/z3.g:16:9: 'unknown'
             {
             match("unknown"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__28"
 
     // $ANTLR start "T__29"
     public final void mT__29() throws RecognitionException {
         try {
             int _type = T__29;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:17:7: ( 'unsat' )
             // ../src/grammar/z3.g:17:9: 'unsat'
             {
             match("unsat"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__29"
 
     // $ANTLR start "TYPE"
     public final void mTYPE() throws RecognitionException {
         try {
             int _type = TYPE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:32:6: ( 'Int' | 'Bool' )
             int alt1=2;
             int LA1_0 = input.LA(1);
 
             if ( (LA1_0=='I') ) {
                 alt1=1;
             }
             else if ( (LA1_0=='B') ) {
                 alt1=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 1, 0, input);
 
                 throw nvae;
 
             }
             switch (alt1) {
                 case 1 :
                     // ../src/grammar/z3.g:32:8: 'Int'
                     {
                     match("Int"); 
 
 
 
                     }
                     break;
                 case 2 :
                     // ../src/grammar/z3.g:33:4: 'Bool'
                     {
                     match("Bool"); 
 
 
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "TYPE"
 
     // $ANTLR start "IDENT"
     public final void mIDENT() throws RecognitionException {
         try {
             int _type = IDENT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:37:6: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
             // ../src/grammar/z3.g:37:8: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
             {
             if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                 input.consume();
             }
             else {
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;
             }
 
 
             // ../src/grammar/z3.g:37:32: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
             loop2:
             do {
                 int alt2=2;
                 int LA2_0 = input.LA(1);
 
                 if ( ((LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'Z')||LA2_0=='_'||(LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
                     alt2=1;
                 }
 
 
                 switch (alt2) {
             	case 1 :
             	    // ../src/grammar/z3.g:
             	    {
             	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
             	    break loop2;
                 }
             } while (true);
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "IDENT"
 
     // $ANTLR start "INT"
     public final void mINT() throws RecognitionException {
         try {
             int _type = INT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:40:5: ( ( '0' .. '9' )+ )
             // ../src/grammar/z3.g:40:7: ( '0' .. '9' )+
             {
             // ../src/grammar/z3.g:40:7: ( '0' .. '9' )+
             int cnt3=0;
             loop3:
             do {
                 int alt3=2;
                 int LA3_0 = input.LA(1);
 
                 if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
                     alt3=1;
                 }
 
 
                 switch (alt3) {
             	case 1 :
             	    // ../src/grammar/z3.g:
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
             	    if ( cnt3 >= 1 ) break loop3;
                         EarlyExitException eee =
                             new EarlyExitException(3, input);
                         throw eee;
                 }
                 cnt3++;
             } while (true);
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "INT"
 
     // $ANTLR start "BOOL"
     public final void mBOOL() throws RecognitionException {
         try {
             int _type = BOOL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:43:6: ( 'true' | 'false' )
             int alt4=2;
             int LA4_0 = input.LA(1);
 
             if ( (LA4_0=='t') ) {
                 alt4=1;
             }
             else if ( (LA4_0=='f') ) {
                 alt4=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 0, input);
 
                 throw nvae;
 
             }
             switch (alt4) {
                 case 1 :
                     // ../src/grammar/z3.g:43:8: 'true'
                     {
                     match("true"); 
 
 
 
                     }
                     break;
                 case 2 :
                     // ../src/grammar/z3.g:43:17: 'false'
                     {
                     match("false"); 
 
 
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "BOOL"
 
     // $ANTLR start "WS"
     public final void mWS() throws RecognitionException {
         try {
             int _type = WS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:46:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
             // ../src/grammar/z3.g:46:9: ( ' ' | '\\t' | '\\r' | '\\n' )
             {
             if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                 input.consume();
             }
             else {
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;
             }
 
 
             _channel=HIDDEN;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "WS"
 
     // $ANTLR start "CHAR"
     public final void mCHAR() throws RecognitionException {
         try {
             int _type = CHAR;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // ../src/grammar/z3.g:53:5: ( '\\'' ( ESC_SEQ |~ ( '\\'' | '\\\\' ) ) '\\'' )
             // ../src/grammar/z3.g:53:8: '\\'' ( ESC_SEQ |~ ( '\\'' | '\\\\' ) ) '\\''
             {
             match('\''); 
 
             // ../src/grammar/z3.g:53:13: ( ESC_SEQ |~ ( '\\'' | '\\\\' ) )
             int alt5=2;
             int LA5_0 = input.LA(1);
 
             if ( (LA5_0=='\\') ) {
                 alt5=1;
             }
             else if ( ((LA5_0 >= '\u0000' && LA5_0 <= '&')||(LA5_0 >= '(' && LA5_0 <= '[')||(LA5_0 >= ']' && LA5_0 <= '\uFFFF')) ) {
                 alt5=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 5, 0, input);
 
                 throw nvae;
 
             }
             switch (alt5) {
                 case 1 :
                     // ../src/grammar/z3.g:53:15: ESC_SEQ
                     {
                     mESC_SEQ(); 
 
 
                     }
                     break;
                 case 2 :
                     // ../src/grammar/z3.g:53:25: ~ ( '\\'' | '\\\\' )
                     {
                     if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
                         input.consume();
                     }
                     else {
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;
                     }
 
 
                     }
                     break;
 
             }
 
 
             match('\''); 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "CHAR"
 
     // $ANTLR start "HEX_DIGIT"
     public final void mHEX_DIGIT() throws RecognitionException {
         try {
             // ../src/grammar/z3.g:58:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
             // ../src/grammar/z3.g:
             {
             if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
                 input.consume();
             }
             else {
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;
             }
 
 
             }
 
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "HEX_DIGIT"
 
     // $ANTLR start "ESC_SEQ"
     public final void mESC_SEQ() throws RecognitionException {
         try {
             // ../src/grammar/z3.g:62:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
             int alt6=3;
             int LA6_0 = input.LA(1);
 
             if ( (LA6_0=='\\') ) {
                 switch ( input.LA(2) ) {
                 case '\"':
                 case '\'':
                 case '\\':
                 case 'b':
                 case 'f':
                 case 'n':
                 case 'r':
                 case 't':
                     {
                     alt6=1;
                     }
                     break;
                 case 'u':
                     {
                     alt6=2;
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
                     {
                     alt6=3;
                     }
                     break;
                 default:
                     NoViableAltException nvae =
                         new NoViableAltException("", 6, 1, input);
 
                     throw nvae;
 
                 }
 
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 6, 0, input);
 
                 throw nvae;
 
             }
             switch (alt6) {
                 case 1 :
                     // ../src/grammar/z3.g:62:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                     {
                     match('\\'); 
 
                     if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                         input.consume();
                     }
                     else {
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;
                     }
 
 
                     }
                     break;
                 case 2 :
                     // ../src/grammar/z3.g:63:9: UNICODE_ESC
                     {
                     mUNICODE_ESC(); 
 
 
                     }
                     break;
                 case 3 :
                     // ../src/grammar/z3.g:64:9: OCTAL_ESC
                     {
                     mOCTAL_ESC(); 
 
 
                     }
                     break;
 
             }
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "ESC_SEQ"
 
     // $ANTLR start "OCTAL_ESC"
     public final void mOCTAL_ESC() throws RecognitionException {
         try {
             // ../src/grammar/z3.g:69:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
             int alt7=3;
             int LA7_0 = input.LA(1);
 
             if ( (LA7_0=='\\') ) {
                 int LA7_1 = input.LA(2);
 
                 if ( ((LA7_1 >= '0' && LA7_1 <= '3')) ) {
                     int LA7_2 = input.LA(3);
 
                     if ( ((LA7_2 >= '0' && LA7_2 <= '7')) ) {
                         int LA7_4 = input.LA(4);
 
                         if ( ((LA7_4 >= '0' && LA7_4 <= '7')) ) {
                             alt7=1;
                         }
                         else {
                             alt7=2;
                         }
                     }
                     else {
                         alt7=3;
                     }
                 }
                 else if ( ((LA7_1 >= '4' && LA7_1 <= '7')) ) {
                     int LA7_3 = input.LA(3);
 
                     if ( ((LA7_3 >= '0' && LA7_3 <= '7')) ) {
                         alt7=2;
                     }
                     else {
                         alt7=3;
                     }
                 }
                 else {
                     NoViableAltException nvae =
                         new NoViableAltException("", 7, 1, input);
 
                     throw nvae;
 
                 }
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 7, 0, input);
 
                 throw nvae;
 
             }
             switch (alt7) {
                 case 1 :
                     // ../src/grammar/z3.g:69:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                     {
                     match('\\'); 
 
                     if ( (input.LA(1) >= '0' && input.LA(1) <= '3') ) {
                         input.consume();
                     }
                     else {
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;
                     }
 
 
                     if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                         input.consume();
                     }
                     else {
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;
                     }
 
 
                     if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                         input.consume();
                     }
                     else {
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;
                     }
 
 
                     }
                     break;
                 case 2 :
                     // ../src/grammar/z3.g:70:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                     {
                     match('\\'); 
 
                     if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                         input.consume();
                     }
                     else {
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;
                     }
 
 
                     if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                         input.consume();
                     }
                     else {
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;
                     }
 
 
                     }
                     break;
                 case 3 :
                     // ../src/grammar/z3.g:71:9: '\\\\' ( '0' .. '7' )
                     {
                     match('\\'); 
 
                     if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                         input.consume();
                     }
                     else {
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;
                     }
 
 
                     }
                     break;
 
             }
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "OCTAL_ESC"
 
     // $ANTLR start "UNICODE_ESC"
     public final void mUNICODE_ESC() throws RecognitionException {
         try {
             // ../src/grammar/z3.g:76:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
             // ../src/grammar/z3.g:76:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
             {
             match('\\'); 
 
             match('u'); 
 
             mHEX_DIGIT(); 
 
 
             mHEX_DIGIT(); 
 
 
             mHEX_DIGIT(); 
 
 
             mHEX_DIGIT(); 
 
 
             }
 
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "UNICODE_ESC"
 
     public void mTokens() throws RecognitionException {
         // ../src/grammar/z3.g:1:8: ( T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | TYPE | IDENT | INT | BOOL | WS | CHAR )
         int alt8=22;
         switch ( input.LA(1) ) {
         case '!':
             {
             alt8=1;
             }
             break;
         case '(':
             {
             switch ( input.LA(2) ) {
             case ')':
                 {
                 alt8=3;
                 }
                 break;
             case '=':
                 {
                 alt8=4;
                 }
                 break;
             case 'A':
                 {
                 alt8=5;
                 }
                 break;
             case '_':
                 {
                 alt8=6;
                 }
                 break;
             case 'a':
                 {
                 alt8=7;
                 }
                 break;
             case 'd':
                 {
                 alt8=8;
                 }
                 break;
             case 'm':
                 {
                 alt8=9;
                 }
                 break;
             default:
                 alt8=2;
             }
 
             }
             break;
         case ')':
             {
             int LA8_3 = input.LA(2);
 
             if ( (LA8_3==')') ) {
                 alt8=11;
             }
             else {
                 alt8=10;
             }
             }
             break;
         case 'I':
             {
             int LA8_4 = input.LA(2);
 
             if ( (LA8_4=='n') ) {
                 int LA8_25 = input.LA(3);
 
                 if ( (LA8_25=='t') ) {
                     int LA8_32 = input.LA(4);
 
                     if ( ((LA8_32 >= '0' && LA8_32 <= '9')||(LA8_32 >= 'A' && LA8_32 <= 'Z')||LA8_32=='_'||(LA8_32 >= 'a' && LA8_32 <= 'z')) ) {
                         alt8=18;
                     }
                     else {
                         alt8=12;
                     }
                 }
                 else {
                     alt8=18;
                 }
             }
             else {
                 alt8=18;
             }
             }
             break;
         case 'd':
             {
             int LA8_5 = input.LA(2);
 
             if ( (LA8_5=='e') ) {
                 int LA8_26 = input.LA(3);
 
                 if ( (LA8_26=='f') ) {
                     int LA8_33 = input.LA(4);
 
                     if ( (LA8_33=='i') ) {
                         int LA8_41 = input.LA(5);
 
                         if ( (LA8_41=='n') ) {
                             int LA8_48 = input.LA(6);
 
                             if ( (LA8_48=='e') ) {
                                 int LA8_53 = input.LA(7);
 
                                 if ( (LA8_53=='-') ) {
                                     alt8=13;
                                 }
                                 else {
                                     alt8=18;
                                 }
                             }
                             else {
                                 alt8=18;
                             }
                         }
                         else {
                             alt8=18;
                         }
                     }
                     else {
                         alt8=18;
                     }
                 }
                 else {
                     alt8=18;
                 }
             }
             else {
                 alt8=18;
             }
             }
             break;
         case 's':
             {
             int LA8_6 = input.LA(2);
 
             if ( (LA8_6=='a') ) {
                 int LA8_27 = input.LA(3);
 
                 if ( (LA8_27=='t') ) {
                     int LA8_34 = input.LA(4);
 
                     if ( ((LA8_34 >= '0' && LA8_34 <= '9')||(LA8_34 >= 'A' && LA8_34 <= 'Z')||LA8_34=='_'||(LA8_34 >= 'a' && LA8_34 <= 'z')) ) {
                         alt8=18;
                     }
                     else {
                         alt8=14;
                     }
                 }
                 else {
                     alt8=18;
                 }
             }
             else {
                 alt8=18;
             }
             }
             break;
         case 'u':
             {
             int LA8_7 = input.LA(2);
 
             if ( (LA8_7=='n') ) {
                 switch ( input.LA(3) ) {
                 case 'k':
                     {
                     int LA8_35 = input.LA(4);
 
                     if ( (LA8_35=='n') ) {
                         int LA8_43 = input.LA(5);
 
                         if ( (LA8_43=='o') ) {
                             int LA8_49 = input.LA(6);
 
                             if ( (LA8_49=='w') ) {
                                 int LA8_54 = input.LA(7);
 
                                 if ( (LA8_54=='n') ) {
                                     int LA8_57 = input.LA(8);
 
                                     if ( ((LA8_57 >= '0' && LA8_57 <= '9')||(LA8_57 >= 'A' && LA8_57 <= 'Z')||LA8_57=='_'||(LA8_57 >= 'a' && LA8_57 <= 'z')) ) {
                                         alt8=18;
                                     }
                                     else {
                                         alt8=15;
                                     }
                                 }
                                 else {
                                     alt8=18;
                                 }
                             }
                             else {
                                 alt8=18;
                             }
                         }
                         else {
                             alt8=18;
                         }
                     }
                     else {
                         alt8=18;
                     }
                     }
                     break;
                 case 's':
                     {
                     int LA8_36 = input.LA(4);
 
                     if ( (LA8_36=='a') ) {
                         int LA8_44 = input.LA(5);
 
                         if ( (LA8_44=='t') ) {
                             int LA8_50 = input.LA(6);
 
                             if ( ((LA8_50 >= '0' && LA8_50 <= '9')||(LA8_50 >= 'A' && LA8_50 <= 'Z')||LA8_50=='_'||(LA8_50 >= 'a' && LA8_50 <= 'z')) ) {
                                 alt8=18;
                             }
                             else {
                                 alt8=16;
                             }
                         }
                         else {
                             alt8=18;
                         }
                     }
                     else {
                         alt8=18;
                     }
                     }
                     break;
                 default:
                     alt8=18;
                 }
 
             }
             else {
                 alt8=18;
             }
             }
             break;
         case 'B':
             {
             int LA8_8 = input.LA(2);
 
             if ( (LA8_8=='o') ) {
                 int LA8_29 = input.LA(3);
 
                 if ( (LA8_29=='o') ) {
                     int LA8_37 = input.LA(4);
 
                     if ( (LA8_37=='l') ) {
                         int LA8_45 = input.LA(5);
 
                         if ( ((LA8_45 >= '0' && LA8_45 <= '9')||(LA8_45 >= 'A' && LA8_45 <= 'Z')||LA8_45=='_'||(LA8_45 >= 'a' && LA8_45 <= 'z')) ) {
                             alt8=18;
                         }
                         else {
                             alt8=17;
                         }
                     }
                     else {
                         alt8=18;
                     }
                 }
                 else {
                     alt8=18;
                 }
             }
             else {
                 alt8=18;
             }
             }
             break;
         case 't':
             {
             int LA8_9 = input.LA(2);
 
             if ( (LA8_9=='r') ) {
                 int LA8_30 = input.LA(3);
 
                 if ( (LA8_30=='u') ) {
                     int LA8_38 = input.LA(4);
 
                     if ( (LA8_38=='e') ) {
                         alt8=18;
                     }
                     else {
                         alt8=18;
                     }
                 }
                 else {
                     alt8=18;
                 }
             }
             else {
                 alt8=18;
             }
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
             alt8=19;
             }
             break;
         case 'f':
             {
             int LA8_11 = input.LA(2);
 
             if ( (LA8_11=='a') ) {
                 int LA8_31 = input.LA(3);
 
                 if ( (LA8_31=='l') ) {
                     int LA8_39 = input.LA(4);
 
                     if ( (LA8_39=='s') ) {
                         int LA8_47 = input.LA(5);
 
                         if ( (LA8_47=='e') ) {
                             alt8=18;
                         }
                         else {
                             alt8=18;
                         }
                     }
                     else {
                         alt8=18;
                     }
                 }
                 else {
                     alt8=18;
                 }
             }
             else {
                 alt8=18;
             }
             }
             break;
         case 'A':
         case 'C':
         case 'D':
         case 'E':
         case 'F':
         case 'G':
         case 'H':
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
         case '_':
         case 'a':
         case 'b':
         case 'c':
         case 'e':
         case 'g':
         case 'h':
         case 'i':
         case 'j':
         case 'k':
         case 'l':
         case 'm':
         case 'n':
         case 'o':
         case 'p':
         case 'q':
         case 'r':
         case 'v':
         case 'w':
         case 'x':
         case 'y':
         case 'z':
             {
             alt8=18;
             }
             break;
         case '\t':
         case '\n':
         case '\r':
         case ' ':
             {
             alt8=21;
             }
             break;
         case '\'':
             {
             alt8=22;
             }
             break;
         default:
             NoViableAltException nvae =
                 new NoViableAltException("", 8, 0, input);
 
             throw nvae;
 
         }
 
         switch (alt8) {
             case 1 :
                 // ../src/grammar/z3.g:1:10: T__14
                 {
                 mT__14(); 
 
 
                 }
                 break;
             case 2 :
                 // ../src/grammar/z3.g:1:16: T__15
                 {
                 mT__15(); 
 
 
                 }
                 break;
             case 3 :
                 // ../src/grammar/z3.g:1:22: T__16
                 {
                 mT__16(); 
 
 
                 }
                 break;
             case 4 :
                 // ../src/grammar/z3.g:1:28: T__17
                 {
                 mT__17(); 
 
 
                 }
                 break;
             case 5 :
                 // ../src/grammar/z3.g:1:34: T__18
                 {
                 mT__18(); 
 
 
                 }
                 break;
             case 6 :
                 // ../src/grammar/z3.g:1:40: T__19
                 {
                 mT__19(); 
 
 
                 }
                 break;
             case 7 :
                 // ../src/grammar/z3.g:1:46: T__20
                 {
                 mT__20(); 
 
 
                 }
                 break;
             case 8 :
                 // ../src/grammar/z3.g:1:52: T__21
                 {
                 mT__21(); 
 
 
                 }
                 break;
             case 9 :
                 // ../src/grammar/z3.g:1:58: T__22
                 {
                 mT__22(); 
 
 
                 }
                 break;
             case 10 :
                 // ../src/grammar/z3.g:1:64: T__23
                 {
                 mT__23(); 
 
 
                 }
                 break;
             case 11 :
                 // ../src/grammar/z3.g:1:70: T__24
                 {
                 mT__24(); 
 
 
                 }
                 break;
             case 12 :
                 // ../src/grammar/z3.g:1:76: T__25
                 {
                 mT__25(); 
 
 
                 }
                 break;
             case 13 :
                 // ../src/grammar/z3.g:1:82: T__26
                 {
                 mT__26(); 
 
 
                 }
                 break;
             case 14 :
                 // ../src/grammar/z3.g:1:88: T__27
                 {
                 mT__27(); 
 
 
                 }
                 break;
             case 15 :
                 // ../src/grammar/z3.g:1:94: T__28
                 {
                 mT__28(); 
 
 
                 }
                 break;
             case 16 :
                 // ../src/grammar/z3.g:1:100: T__29
                 {
                 mT__29(); 
 
 
                 }
                 break;
             case 17 :
                 // ../src/grammar/z3.g:1:106: TYPE
                 {
                 mTYPE(); 
 
 
                 }
                 break;
             case 18 :
                 // ../src/grammar/z3.g:1:111: IDENT
                 {
                 mIDENT(); 
 
 
                 }
                 break;
             case 19 :
                 // ../src/grammar/z3.g:1:117: INT
                 {
                 mINT(); 
 
 
                 }
                 break;
             case 20 :
                 // ../src/grammar/z3.g:1:121: BOOL
                 {
                 mBOOL(); 
 
 
                 }
                 break;
             case 21 :
                 // ../src/grammar/z3.g:1:126: WS
                 {
                 mWS(); 
 
 
                 }
                 break;
             case 22 :
                 // ../src/grammar/z3.g:1:129: CHAR
                 {
                 mCHAR(); 
 
 
                 }
                 break;
 
         }
 
     }
 
 
  
 
 }
