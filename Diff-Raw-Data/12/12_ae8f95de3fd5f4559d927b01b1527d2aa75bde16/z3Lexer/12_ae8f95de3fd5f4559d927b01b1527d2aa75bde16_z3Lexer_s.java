// $ANTLR 3.4 /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g 2012-02-21 19:21:24
 
     package verifier.smtlib.z3;
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class z3Lexer extends Lexer {
     public static final int EOF=-1;
     public static final int T__9=9;
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
     public static final int T__20=20;
     public static final int T__21=21;
     public static final int T__22=22;
     public static final int BOOL=4;
     public static final int IDENT=5;
     public static final int INT=6;
     public static final int TYPE=7;
     public static final int WS=8;
 
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
     public String getGrammarFileName() { return "/Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g"; }
 
     // $ANTLR start "T__9"
     public final void mT__9() throws RecognitionException {
         try {
             int _type = T__9;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:6:6: ( '!' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:6:8: '!'
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
     // $ANTLR end "T__9"
 
     // $ANTLR start "T__10"
     public final void mT__10() throws RecognitionException {
         try {
             int _type = T__10;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:7:7: ( '(' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:7:9: '('
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
     // $ANTLR end "T__10"
 
     // $ANTLR start "T__11"
     public final void mT__11() throws RecognitionException {
         try {
             int _type = T__11;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:8:7: ( '(Array' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:8:9: '(Array'
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
     // $ANTLR end "T__11"
 
     // $ANTLR start "T__12"
     public final void mT__12() throws RecognitionException {
         try {
             int _type = T__12;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:9:7: ( '(define-fun' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:9:9: '(define-fun'
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
     // $ANTLR end "T__12"
 
     // $ANTLR start "T__13"
     public final void mT__13() throws RecognitionException {
         try {
             int _type = T__13;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:10:7: ( '(model' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:10:9: '(model'
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
     // $ANTLR end "T__13"
 
     // $ANTLR start "T__14"
     public final void mT__14() throws RecognitionException {
         try {
             int _type = T__14;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:11:7: ( ')' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:11:9: ')'
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
     // $ANTLR end "T__14"
 
     // $ANTLR start "T__15"
     public final void mT__15() throws RecognitionException {
         try {
             int _type = T__15;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:12:7: ( '=' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:12:9: '='
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
     // $ANTLR end "T__15"
 
     // $ANTLR start "T__16"
     public final void mT__16() throws RecognitionException {
         try {
             int _type = T__16;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:13:7: ( '_' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:13:9: '_'
             {
             match('_'); 
 
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
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:14:7: ( 'and' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:14:9: 'and'
             {
             match("and"); 
 
 
 
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
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:15:7: ( 'as-array' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:15:9: 'as-array'
             {
             match("as-array"); 
 
 
 
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
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:16:7: ( 'ite' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:16:9: 'ite'
             {
             match("ite"); 
 
 
 
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
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:17:7: ( 'sat' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:17:9: 'sat'
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
     // $ANTLR end "T__20"
 
     // $ANTLR start "T__21"
     public final void mT__21() throws RecognitionException {
         try {
             int _type = T__21;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:18:7: ( 'unknown' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:18:9: 'unknown'
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
     // $ANTLR end "T__21"
 
     // $ANTLR start "T__22"
     public final void mT__22() throws RecognitionException {
         try {
             int _type = T__22;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:19:7: ( 'unsat' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:19:9: 'unsat'
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
     // $ANTLR end "T__22"
 
     // $ANTLR start "TYPE"
     public final void mTYPE() throws RecognitionException {
         try {
             int _type = TYPE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:54:6: ( 'Int' | 'Bool' )
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
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:54:8: 'Int'
                     {
                     match("Int"); 
 
 
 
                     }
                     break;
                 case 2 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:55:4: 'Bool'
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
 
     // $ANTLR start "BOOL"
     public final void mBOOL() throws RecognitionException {
         try {
             int _type = BOOL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:59:6: ( 'true' | 'false' )
             int alt2=2;
             int LA2_0 = input.LA(1);
 
             if ( (LA2_0=='t') ) {
                 alt2=1;
             }
             else if ( (LA2_0=='f') ) {
                 alt2=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 2, 0, input);
 
                 throw nvae;
 
             }
             switch (alt2) {
                 case 1 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:59:8: 'true'
                     {
                     match("true"); 
 
 
 
                     }
                     break;
                 case 2 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:59:17: 'false'
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
 
     // $ANTLR start "INT"
     public final void mINT() throws RecognitionException {
         try {
             int _type = INT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:62:5: ( ( '0' .. '9' )+ | ( '(- ' ( '0' .. '9' )+ ')' ) )
             int alt5=2;
             int LA5_0 = input.LA(1);
 
             if ( ((LA5_0 >= '0' && LA5_0 <= '9')) ) {
                 alt5=1;
             }
             else if ( (LA5_0=='(') ) {
                 alt5=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 5, 0, input);
 
                 throw nvae;
 
             }
             switch (alt5) {
                 case 1 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:62:7: ( '0' .. '9' )+
                     {
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:62:7: ( '0' .. '9' )+
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
                     	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:
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
                     break;
                 case 2 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:62:21: ( '(- ' ( '0' .. '9' )+ ')' )
                     {
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:62:21: ( '(- ' ( '0' .. '9' )+ ')' )
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:62:22: '(- ' ( '0' .. '9' )+ ')'
                     {
                     match("(- "); 
 
 
 
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:62:28: ( '0' .. '9' )+
                     int cnt4=0;
                     loop4:
                     do {
                         int alt4=2;
                         int LA4_0 = input.LA(1);
 
                         if ( ((LA4_0 >= '0' && LA4_0 <= '9')) ) {
                             alt4=1;
                         }
 
 
                         switch (alt4) {
                     	case 1 :
                     	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:
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
                     	    if ( cnt4 >= 1 ) break loop4;
                                 EarlyExitException eee =
                                     new EarlyExitException(4, input);
                                 throw eee;
                         }
                         cnt4++;
                     } while (true);
 
 
                     match(')'); 
 
                     }
 
 
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
     // $ANTLR end "INT"
 
     // $ANTLR start "WS"
     public final void mWS() throws RecognitionException {
         try {
             int _type = WS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:65:5: ( ( ' ' | '\\t' | '\\r' | '\\n' | '\"' | ':' ) )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:65:9: ( ' ' | '\\t' | '\\r' | '\\n' | '\"' | ':' )
             {
             if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' '||input.LA(1)=='\"'||input.LA(1)==':' ) {
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
 
     // $ANTLR start "IDENT"
     public final void mIDENT() throws RecognitionException {
         try {
             int _type = IDENT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:75:6: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '#' | '$' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '#' | '$' )* )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:75:8: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '#' | '$' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '#' | '$' )*
             {
             if ( (input.LA(1) >= '#' && input.LA(1) <= '$')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                 input.consume();
             }
             else {
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;
             }
 
 
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:75:40: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '#' | '$' )*
             loop6:
             do {
                 int alt6=2;
                 int LA6_0 = input.LA(1);
 
                 if ( ((LA6_0 >= '#' && LA6_0 <= '$')||(LA6_0 >= '0' && LA6_0 <= '9')||(LA6_0 >= 'A' && LA6_0 <= 'Z')||LA6_0=='_'||(LA6_0 >= 'a' && LA6_0 <= 'z')) ) {
                     alt6=1;
                 }
 
 
                 switch (alt6) {
             	case 1 :
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:
             	    {
             	    if ( (input.LA(1) >= '#' && input.LA(1) <= '$')||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
             	    break loop6;
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
 
     public void mTokens() throws RecognitionException {
         // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:8: ( T__9 | T__10 | T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | TYPE | BOOL | INT | WS | IDENT )
         int alt7=19;
         switch ( input.LA(1) ) {
         case '!':
             {
             alt7=1;
             }
             break;
         case '(':
             {
             switch ( input.LA(2) ) {
             case 'A':
                 {
                 alt7=3;
                 }
                 break;
             case 'd':
                 {
                 alt7=4;
                 }
                 break;
             case 'm':
                 {
                 alt7=5;
                 }
                 break;
             case '-':
                 {
                 alt7=17;
                 }
                 break;
             default:
                 alt7=2;
             }
 
             }
             break;
         case ')':
             {
             alt7=6;
             }
             break;
         case '=':
             {
             alt7=7;
             }
             break;
         case '_':
             {
             int LA7_5 = input.LA(2);
 
             if ( ((LA7_5 >= '#' && LA7_5 <= '$')||(LA7_5 >= '0' && LA7_5 <= '9')||(LA7_5 >= 'A' && LA7_5 <= 'Z')||LA7_5=='_'||(LA7_5 >= 'a' && LA7_5 <= 'z')) ) {
                 alt7=19;
             }
             else {
                 alt7=8;
             }
             }
             break;
         case 'a':
             {
             switch ( input.LA(2) ) {
             case 'n':
                 {
                 int LA7_22 = input.LA(3);
 
                 if ( (LA7_22=='d') ) {
                     int LA7_31 = input.LA(4);
 
                     if ( ((LA7_31 >= '#' && LA7_31 <= '$')||(LA7_31 >= '0' && LA7_31 <= '9')||(LA7_31 >= 'A' && LA7_31 <= 'Z')||LA7_31=='_'||(LA7_31 >= 'a' && LA7_31 <= 'z')) ) {
                         alt7=19;
                     }
                     else {
                         alt7=9;
                     }
                 }
                 else {
                     alt7=19;
                 }
                 }
                 break;
             case 's':
                 {
                 int LA7_23 = input.LA(3);
 
                 if ( (LA7_23=='-') ) {
                     alt7=10;
                 }
                 else {
                     alt7=19;
                 }
                 }
                 break;
             default:
                 alt7=19;
             }
 
             }
             break;
         case 'i':
             {
             int LA7_7 = input.LA(2);
 
             if ( (LA7_7=='t') ) {
                 int LA7_24 = input.LA(3);
 
                 if ( (LA7_24=='e') ) {
                     int LA7_33 = input.LA(4);
 
                     if ( ((LA7_33 >= '#' && LA7_33 <= '$')||(LA7_33 >= '0' && LA7_33 <= '9')||(LA7_33 >= 'A' && LA7_33 <= 'Z')||LA7_33=='_'||(LA7_33 >= 'a' && LA7_33 <= 'z')) ) {
                         alt7=19;
                     }
                     else {
                         alt7=11;
                     }
                 }
                 else {
                     alt7=19;
                 }
             }
             else {
                 alt7=19;
             }
             }
             break;
         case 's':
             {
             int LA7_8 = input.LA(2);
 
             if ( (LA7_8=='a') ) {
                 int LA7_25 = input.LA(3);
 
                 if ( (LA7_25=='t') ) {
                     int LA7_34 = input.LA(4);
 
                     if ( ((LA7_34 >= '#' && LA7_34 <= '$')||(LA7_34 >= '0' && LA7_34 <= '9')||(LA7_34 >= 'A' && LA7_34 <= 'Z')||LA7_34=='_'||(LA7_34 >= 'a' && LA7_34 <= 'z')) ) {
                         alt7=19;
                     }
                     else {
                         alt7=12;
                     }
                 }
                 else {
                     alt7=19;
                 }
             }
             else {
                 alt7=19;
             }
             }
             break;
         case 'u':
             {
             int LA7_9 = input.LA(2);
 
             if ( (LA7_9=='n') ) {
                 switch ( input.LA(3) ) {
                 case 'k':
                     {
                     int LA7_35 = input.LA(4);
 
                     if ( (LA7_35=='n') ) {
                         int LA7_44 = input.LA(5);
 
                         if ( (LA7_44=='o') ) {
                             int LA7_50 = input.LA(6);
 
                             if ( (LA7_50=='w') ) {
                                 int LA7_54 = input.LA(7);
 
                                 if ( (LA7_54=='n') ) {
                                     int LA7_56 = input.LA(8);
 
                                     if ( ((LA7_56 >= '#' && LA7_56 <= '$')||(LA7_56 >= '0' && LA7_56 <= '9')||(LA7_56 >= 'A' && LA7_56 <= 'Z')||LA7_56=='_'||(LA7_56 >= 'a' && LA7_56 <= 'z')) ) {
                                         alt7=19;
                                     }
                                     else {
                                         alt7=13;
                                     }
                                 }
                                 else {
                                     alt7=19;
                                 }
                             }
                             else {
                                 alt7=19;
                             }
                         }
                         else {
                             alt7=19;
                         }
                     }
                     else {
                         alt7=19;
                     }
                     }
                     break;
                 case 's':
                     {
                     int LA7_36 = input.LA(4);
 
                     if ( (LA7_36=='a') ) {
                         int LA7_45 = input.LA(5);
 
                         if ( (LA7_45=='t') ) {
                             int LA7_51 = input.LA(6);
 
                             if ( ((LA7_51 >= '#' && LA7_51 <= '$')||(LA7_51 >= '0' && LA7_51 <= '9')||(LA7_51 >= 'A' && LA7_51 <= 'Z')||LA7_51=='_'||(LA7_51 >= 'a' && LA7_51 <= 'z')) ) {
                                 alt7=19;
                             }
                             else {
                                 alt7=14;
                             }
                         }
                         else {
                             alt7=19;
                         }
                     }
                     else {
                         alt7=19;
                     }
                     }
                     break;
                 default:
                     alt7=19;
                 }
 
             }
             else {
                 alt7=19;
             }
             }
             break;
         case 'I':
             {
             int LA7_10 = input.LA(2);
 
             if ( (LA7_10=='n') ) {
                 int LA7_27 = input.LA(3);
 
                 if ( (LA7_27=='t') ) {
                     int LA7_37 = input.LA(4);
 
                     if ( ((LA7_37 >= '#' && LA7_37 <= '$')||(LA7_37 >= '0' && LA7_37 <= '9')||(LA7_37 >= 'A' && LA7_37 <= 'Z')||LA7_37=='_'||(LA7_37 >= 'a' && LA7_37 <= 'z')) ) {
                         alt7=19;
                     }
                     else {
                         alt7=15;
                     }
                 }
                 else {
                     alt7=19;
                 }
             }
             else {
                 alt7=19;
             }
             }
             break;
         case 'B':
             {
             int LA7_11 = input.LA(2);
 
             if ( (LA7_11=='o') ) {
                 int LA7_28 = input.LA(3);
 
                 if ( (LA7_28=='o') ) {
                     int LA7_38 = input.LA(4);
 
                     if ( (LA7_38=='l') ) {
                         int LA7_47 = input.LA(5);
 
                         if ( ((LA7_47 >= '#' && LA7_47 <= '$')||(LA7_47 >= '0' && LA7_47 <= '9')||(LA7_47 >= 'A' && LA7_47 <= 'Z')||LA7_47=='_'||(LA7_47 >= 'a' && LA7_47 <= 'z')) ) {
                             alt7=19;
                         }
                         else {
                             alt7=15;
                         }
                     }
                     else {
                         alt7=19;
                     }
                 }
                 else {
                     alt7=19;
                 }
             }
             else {
                 alt7=19;
             }
             }
             break;
         case 't':
             {
             int LA7_12 = input.LA(2);
 
             if ( (LA7_12=='r') ) {
                 int LA7_29 = input.LA(3);
 
                 if ( (LA7_29=='u') ) {
                     int LA7_39 = input.LA(4);
 
                     if ( (LA7_39=='e') ) {
                         int LA7_48 = input.LA(5);
 
                         if ( ((LA7_48 >= '#' && LA7_48 <= '$')||(LA7_48 >= '0' && LA7_48 <= '9')||(LA7_48 >= 'A' && LA7_48 <= 'Z')||LA7_48=='_'||(LA7_48 >= 'a' && LA7_48 <= 'z')) ) {
                             alt7=19;
                         }
                         else {
                             alt7=16;
                         }
                     }
                     else {
                         alt7=19;
                     }
                 }
                 else {
                     alt7=19;
                 }
             }
             else {
                 alt7=19;
             }
             }
             break;
         case 'f':
             {
             int LA7_13 = input.LA(2);
 
             if ( (LA7_13=='a') ) {
                 int LA7_30 = input.LA(3);
 
                 if ( (LA7_30=='l') ) {
                     int LA7_40 = input.LA(4);
 
                     if ( (LA7_40=='s') ) {
                         int LA7_49 = input.LA(5);
 
                         if ( (LA7_49=='e') ) {
                             int LA7_53 = input.LA(6);
 
                             if ( ((LA7_53 >= '#' && LA7_53 <= '$')||(LA7_53 >= '0' && LA7_53 <= '9')||(LA7_53 >= 'A' && LA7_53 <= 'Z')||LA7_53=='_'||(LA7_53 >= 'a' && LA7_53 <= 'z')) ) {
                                 alt7=19;
                             }
                             else {
                                 alt7=16;
                             }
                         }
                         else {
                             alt7=19;
                         }
                     }
                     else {
                         alt7=19;
                     }
                 }
                 else {
                     alt7=19;
                 }
             }
             else {
                 alt7=19;
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
             alt7=17;
             }
             break;
         case '\t':
         case '\n':
         case '\r':
         case ' ':
         case '\"':
         case ':':
             {
             alt7=18;
             }
             break;
         case '#':
         case '$':
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
         case 'b':
         case 'c':
         case 'd':
         case 'e':
         case 'g':
         case 'h':
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
             alt7=19;
             }
             break;
         default:
             NoViableAltException nvae =
                 new NoViableAltException("", 7, 0, input);
 
             throw nvae;
 
         }
 
         switch (alt7) {
             case 1 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:10: T__9
                 {
                 mT__9(); 
 
 
                 }
                 break;
             case 2 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:15: T__10
                 {
                 mT__10(); 
 
 
                 }
                 break;
             case 3 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:21: T__11
                 {
                 mT__11(); 
 
 
                 }
                 break;
             case 4 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:27: T__12
                 {
                 mT__12(); 
 
 
                 }
                 break;
             case 5 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:33: T__13
                 {
                 mT__13(); 
 
 
                 }
                 break;
             case 6 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:39: T__14
                 {
                 mT__14(); 
 
 
                 }
                 break;
             case 7 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:45: T__15
                 {
                 mT__15(); 
 
 
                 }
                 break;
             case 8 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:51: T__16
                 {
                 mT__16(); 
 
 
                 }
                 break;
             case 9 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:57: T__17
                 {
                 mT__17(); 
 
 
                 }
                 break;
             case 10 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:63: T__18
                 {
                 mT__18(); 
 
 
                 }
                 break;
             case 11 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:69: T__19
                 {
                 mT__19(); 
 
 
                 }
                 break;
             case 12 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:75: T__20
                 {
                 mT__20(); 
 
 
                 }
                 break;
             case 13 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:81: T__21
                 {
                 mT__21(); 
 
 
                 }
                 break;
             case 14 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:87: T__22
                 {
                 mT__22(); 
 
 
                 }
                 break;
             case 15 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:93: TYPE
                 {
                 mTYPE(); 
 
 
                 }
                 break;
             case 16 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:98: BOOL
                 {
                 mBOOL(); 
 
 
                 }
                 break;
             case 17 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:103: INT
                 {
                 mINT(); 
 
 
                 }
                 break;
             case 18 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:107: WS
                 {
                 mWS(); 
 
 
                 }
                 break;
             case 19 :
                 // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:1:110: IDENT
                 {
                 mIDENT(); 
 
 
                 }
                 break;
 
         }
 
     }
 
 
  
 
 }
