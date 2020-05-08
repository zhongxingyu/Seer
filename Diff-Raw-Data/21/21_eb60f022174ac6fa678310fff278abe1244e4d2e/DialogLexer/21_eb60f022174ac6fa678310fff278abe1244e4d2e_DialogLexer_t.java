// $ANTLR 3.4 Dialog.g 2013-01-26 17:44:56
 
 package net.citizensnpcs.adventures.dialog;
 import java.util.concurrent.TimeUnit;
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class DialogLexer extends Lexer {
     public static final int EOF=-1;
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
     public static final int T__30=30;
     public static final int T__31=31;
     public static final int T__32=32;
     public static final int T__33=33;
     public static final int T__34=34;
     public static final int T__35=35;
     public static final int T__36=36;
     public static final int T__37=37;
     public static final int T__38=38;
     public static final int T__39=39;
     public static final int T__40=40;
     public static final int T__41=41;
     public static final int T__42=42;
     public static final int T__43=43;
     public static final int T__44=44;
     public static final int T__45=45;
     public static final int T__46=46;
     public static final int T__47=47;
     public static final int T__48=48;
     public static final int BOOLEAN_LITERAL=4;
     public static final int DIGIT=5;
     public static final int IDENT=6;
     public static final int INTEGER=7;
     public static final int LETTER=8;
     public static final int LINE_COMMENT=9;
     public static final int ML_COMMENT=10;
     public static final int NEWLINE=11;
     public static final int NUMBER=12;
     public static final int QUERY_STRING=13;
     public static final int STRING_LITERAL=14;
     public static final int WS=15;
 
     /*  @Override
         public void reportError(RecognitionException e) {
             throw e;
         }
     */
 
 
     // delegates
     // delegators
     public Lexer[] getDelegates() {
         return new Lexer[] {};
     }
 
     public DialogLexer() {} 
     public DialogLexer(CharStream input) {
         this(input, new RecognizerSharedState());
     }
     public DialogLexer(CharStream input, RecognizerSharedState state) {
         super(input,state);
     }
     public String getGrammarFileName() { return "Dialog.g"; }
 
     // $ANTLR start "T__16"
     public final void mT__16() throws RecognitionException {
         try {
             int _type = T__16;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:19:7: ( '!=' )
             // Dialog.g:19:9: '!='
             {
             match("!="); 
 
 
 
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
             // Dialog.g:20:7: ( '%' )
             // Dialog.g:20:9: '%'
             {
             match('%'); 
 
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
             // Dialog.g:21:7: ( '(' )
             // Dialog.g:21:9: '('
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
     // $ANTLR end "T__18"
 
     // $ANTLR start "T__19"
     public final void mT__19() throws RecognitionException {
         try {
             int _type = T__19;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:22:7: ( ')' )
             // Dialog.g:22:9: ')'
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
     // $ANTLR end "T__19"
 
     // $ANTLR start "T__20"
     public final void mT__20() throws RecognitionException {
         try {
             int _type = T__20;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:23:7: ( '*' )
             // Dialog.g:23:9: '*'
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
     // $ANTLR end "T__20"
 
     // $ANTLR start "T__21"
     public final void mT__21() throws RecognitionException {
         try {
             int _type = T__21;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:24:7: ( '**' )
             // Dialog.g:24:9: '**'
             {
             match("**"); 
 
 
 
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
             // Dialog.g:25:7: ( '+' )
             // Dialog.g:25:9: '+'
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
     // $ANTLR end "T__22"
 
     // $ANTLR start "T__23"
     public final void mT__23() throws RecognitionException {
         try {
             int _type = T__23;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:26:7: ( ',' )
             // Dialog.g:26:9: ','
             {
             match(','); 
 
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
             // Dialog.g:27:7: ( '-' )
             // Dialog.g:27:9: '-'
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
     // $ANTLR end "T__24"
 
     // $ANTLR start "T__25"
     public final void mT__25() throws RecognitionException {
         try {
             int _type = T__25;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:28:7: ( '-p' )
             // Dialog.g:28:9: '-p'
             {
             match("-p"); 
 
 
 
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
             // Dialog.g:29:7: ( '/' )
             // Dialog.g:29:9: '/'
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
     // $ANTLR end "T__26"
 
     // $ANTLR start "T__27"
     public final void mT__27() throws RecognitionException {
         try {
             int _type = T__27;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:30:7: ( ';' )
             // Dialog.g:30:9: ';'
             {
             match(';'); 
 
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
             // Dialog.g:31:7: ( '<' )
             // Dialog.g:31:9: '<'
             {
             match('<'); 
 
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
             // Dialog.g:32:7: ( '<=' )
             // Dialog.g:32:9: '<='
             {
             match("<="); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__29"
 
     // $ANTLR start "T__30"
     public final void mT__30() throws RecognitionException {
         try {
             int _type = T__30;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:33:7: ( '=' )
             // Dialog.g:33:9: '='
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
     // $ANTLR end "T__30"
 
     // $ANTLR start "T__31"
     public final void mT__31() throws RecognitionException {
         try {
             int _type = T__31;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:34:7: ( '>' )
             // Dialog.g:34:9: '>'
             {
             match('>'); 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__31"
 
     // $ANTLR start "T__32"
     public final void mT__32() throws RecognitionException {
         try {
             int _type = T__32;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:35:7: ( '>=' )
             // Dialog.g:35:9: '>='
             {
             match(">="); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__32"
 
     // $ANTLR start "T__33"
     public final void mT__33() throws RecognitionException {
         try {
             int _type = T__33;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:36:7: ( 'criteria' )
             // Dialog.g:36:9: 'criteria'
             {
             match("criteria"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__33"
 
     // $ANTLR start "T__34"
     public final void mT__34() throws RecognitionException {
         try {
             int _type = T__34;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:37:7: ( 'd' )
             // Dialog.g:37:9: 'd'
             {
             match('d'); 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__34"
 
     // $ANTLR start "T__35"
     public final void mT__35() throws RecognitionException {
         try {
             int _type = T__35;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:38:7: ( 'events=' )
             // Dialog.g:38:9: 'events='
             {
             match("events="); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__35"
 
     // $ANTLR start "T__36"
     public final void mT__36() throws RecognitionException {
         try {
             int _type = T__36;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:39:7: ( 'h' )
             // Dialog.g:39:9: 'h'
             {
             match('h'); 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__36"
 
     // $ANTLR start "T__37"
     public final void mT__37() throws RecognitionException {
         try {
             int _type = T__37;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:40:7: ( 'log' )
             // Dialog.g:40:9: 'log'
             {
             match("log"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__37"
 
     // $ANTLR start "T__38"
     public final void mT__38() throws RecognitionException {
         try {
             int _type = T__38;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:41:7: ( 'm' )
             // Dialog.g:41:9: 'm'
             {
             match('m'); 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__38"
 
     // $ANTLR start "T__39"
     public final void mT__39() throws RecognitionException {
         try {
             int _type = T__39;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:42:7: ( 'ms' )
             // Dialog.g:42:9: 'ms'
             {
             match("ms"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__39"
 
     // $ANTLR start "T__40"
     public final void mT__40() throws RecognitionException {
         try {
             int _type = T__40;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:43:7: ( 'ns' )
             // Dialog.g:43:9: 'ns'
             {
             match("ns"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__40"
 
     // $ANTLR start "T__41"
     public final void mT__41() throws RecognitionException {
         try {
             int _type = T__41;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:44:7: ( 'remember' )
             // Dialog.g:44:9: 'remember'
             {
             match("remember"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__41"
 
     // $ANTLR start "T__42"
     public final void mT__42() throws RecognitionException {
         try {
             int _type = T__42;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:45:7: ( 'response' )
             // Dialog.g:45:9: 'response'
             {
             match("response"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__42"
 
     // $ANTLR start "T__43"
     public final void mT__43() throws RecognitionException {
         try {
             int _type = T__43;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:46:7: ( 'rule' )
             // Dialog.g:46:9: 'rule'
             {
             match("rule"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__43"
 
     // $ANTLR start "T__44"
     public final void mT__44() throws RecognitionException {
         try {
             int _type = T__44;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:47:7: ( 's' )
             // Dialog.g:47:9: 's'
             {
             match('s'); 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__44"
 
     // $ANTLR start "T__45"
     public final void mT__45() throws RecognitionException {
         try {
             int _type = T__45;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:48:7: ( 'then' )
             // Dialog.g:48:9: 'then'
             {
             match("then"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__45"
 
     // $ANTLR start "T__46"
     public final void mT__46() throws RecognitionException {
         try {
             int _type = T__46;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:49:7: ( 'us' )
             // Dialog.g:49:9: 'us'
             {
             match("us"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__46"
 
     // $ANTLR start "T__47"
     public final void mT__47() throws RecognitionException {
         try {
             int _type = T__47;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:50:7: ( '{' )
             // Dialog.g:50:9: '{'
             {
             match('{'); 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__47"
 
     // $ANTLR start "T__48"
     public final void mT__48() throws RecognitionException {
         try {
             int _type = T__48;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:51:7: ( '}' )
             // Dialog.g:51:9: '}'
             {
             match('}'); 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__48"
 
     // $ANTLR start "ML_COMMENT"
     public final void mML_COMMENT() throws RecognitionException {
         try {
             int _type = ML_COMMENT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:173:12: ( '/*' ( options {greedy=false; } : . )* '*/' )
             // Dialog.g:174:5: '/*' ( options {greedy=false; } : . )* '*/'
             {
             match("/*"); 
 
 
 
             // Dialog.g:174:10: ( options {greedy=false; } : . )*
             loop1:
             do {
                 int alt1=2;
                 int LA1_0 = input.LA(1);
 
                 if ( (LA1_0=='*') ) {
                     int LA1_1 = input.LA(2);
 
                     if ( (LA1_1=='/') ) {
                         alt1=2;
                     }
                     else if ( ((LA1_1 >= '\u0000' && LA1_1 <= '.')||(LA1_1 >= '0' && LA1_1 <= '\uFFFF')) ) {
                         alt1=1;
                     }
 
 
                 }
                 else if ( ((LA1_0 >= '\u0000' && LA1_0 <= ')')||(LA1_0 >= '+' && LA1_0 <= '\uFFFF')) ) {
                     alt1=1;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // Dialog.g:174:38: .
             	    {
             	    matchAny(); 
 
             	    }
             	    break;
 
             	default :
             	    break loop1;
                 }
             } while (true);
 
 
             match("*/"); 
 
 
 
              _channel = HIDDEN; 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "ML_COMMENT"
 
     // $ANTLR start "LINE_COMMENT"
     public final void mLINE_COMMENT() throws RecognitionException {
         try {
             int _type = LINE_COMMENT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:176:14: ( '//' (~ NEWLINE )* )
             // Dialog.g:177:5: '//' (~ NEWLINE )*
             {
             match("//"); 
 
 
 
             // Dialog.g:177:10: (~ NEWLINE )*
             loop2:
             do {
                 int alt2=2;
                 int LA2_0 = input.LA(1);
 
                 if ( ((LA2_0 >= '\u0000' && LA2_0 <= '\t')||(LA2_0 >= '\u000B' && LA2_0 <= '\f')||(LA2_0 >= '\u000E' && LA2_0 <= '\uFFFF')) ) {
                     alt2=1;
                 }
 
 
                 switch (alt2) {
             	case 1 :
             	    // Dialog.g:
             	    {
             	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
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
 
 
              _channel = HIDDEN; 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "LINE_COMMENT"
 
     // $ANTLR start "NEWLINE"
     public final void mNEWLINE() throws RecognitionException {
         try {
             // Dialog.g:179:18: ( '\\n' | '\\r' )
             // Dialog.g:
             {
             if ( input.LA(1)=='\n'||input.LA(1)=='\r' ) {
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
     // $ANTLR end "NEWLINE"
 
     // $ANTLR start "WS"
     public final void mWS() throws RecognitionException {
         try {
             int _type = WS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // Dialog.g:183:4: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
             // Dialog.g:184:5: ( ' ' | '\\t' | '\\n' | '\\r' )+
             {
             // Dialog.g:184:5: ( ' ' | '\\t' | '\\n' | '\\r' )+
             int cnt3=0;
             loop3:
             do {
                 int alt3=2;
                 int LA3_0 = input.LA(1);
 
                 if ( ((LA3_0 >= '\t' && LA3_0 <= '\n')||LA3_0=='\r'||LA3_0==' ') ) {
                     alt3=1;
                 }
 
 
                 switch (alt3) {
             	case 1 :
             	    // Dialog.g:
             	    {
             	    if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
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
 
 
              _channel = HIDDEN; 
 
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
             // Dialog.g:186:16: ( LETTER ( LETTER | '_' | DIGIT )* )
             // Dialog.g:187:5: LETTER ( LETTER | '_' | DIGIT )*
             {
             mLETTER(); 
 
 
             // Dialog.g:187:12: ( LETTER | '_' | DIGIT )*
             loop4:
             do {
                 int alt4=2;
                 int LA4_0 = input.LA(1);
 
                 if ( ((LA4_0 >= '0' && LA4_0 <= '9')||(LA4_0 >= 'A' && LA4_0 <= 'Z')||LA4_0=='_'||(LA4_0 >= 'a' && LA4_0 <= 'z')) ) {
                     alt4=1;
                 }
 
 
                 switch (alt4) {
             	case 1 :
             	    // Dialog.g:
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
             	    break loop4;
                 }
             } while (true);
 
 
             }
 
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "IDENT"
 
     // $ANTLR start "QUERY_STRING"
     public final void mQUERY_STRING() throws RecognitionException {
         try {
             // Dialog.g:189:23: ( IDENT ( '.' IDENT )* )
             // Dialog.g:190:5: IDENT ( '.' IDENT )*
             {
             mIDENT(); 
 
 
             // Dialog.g:190:11: ( '.' IDENT )*
             loop5:
             do {
                 int alt5=2;
                 int LA5_0 = input.LA(1);
 
                 if ( (LA5_0=='.') ) {
                     alt5=1;
                 }
 
 
                 switch (alt5) {
             	case 1 :
             	    // Dialog.g:190:12: '.' IDENT
             	    {
             	    match('.'); 
 
             	    mIDENT(); 
 
 
             	    }
             	    break;
 
             	default :
             	    break loop5;
                 }
             } while (true);
 
 
             }
 
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "QUERY_STRING"
 
     // $ANTLR start "LETTER"
     public final void mLETTER() throws RecognitionException {
         try {
             // Dialog.g:192:17: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
             // Dialog.g:
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
 
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "LETTER"
 
     // $ANTLR start "DIGIT"
     public final void mDIGIT() throws RecognitionException {
         try {
             // Dialog.g:195:16: ( ( '0' .. '9' ) )
             // Dialog.g:
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
 
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "DIGIT"
 
     // $ANTLR start "INTEGER"
     public final void mINTEGER() throws RecognitionException {
         try {
             // Dialog.g:198:18: ( ( DIGIT )+ )
             // Dialog.g:199:5: ( DIGIT )+
             {
             // Dialog.g:199:5: ( DIGIT )+
             int cnt6=0;
             loop6:
             do {
                 int alt6=2;
                 int LA6_0 = input.LA(1);
 
                 if ( ((LA6_0 >= '0' && LA6_0 <= '9')) ) {
                     alt6=1;
                 }
 
 
                 switch (alt6) {
             	case 1 :
             	    // Dialog.g:
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
             	    if ( cnt6 >= 1 ) break loop6;
                         EarlyExitException eee =
                             new EarlyExitException(6, input);
                         throw eee;
                 }
                 cnt6++;
             } while (true);
 
 
             }
 
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "INTEGER"
 
     // $ANTLR start "BOOLEAN_LITERAL"
     public final void mBOOLEAN_LITERAL() throws RecognitionException {
         try {
             // Dialog.g:201:26: ( ( 'true' | 'false' ) )
             // Dialog.g:202:5: ( 'true' | 'false' )
             {
             // Dialog.g:202:5: ( 'true' | 'false' )
             int alt7=2;
             int LA7_0 = input.LA(1);
 
             if ( (LA7_0=='t') ) {
                 alt7=1;
             }
             else if ( (LA7_0=='f') ) {
                 alt7=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 7, 0, input);
 
                 throw nvae;
 
             }
             switch (alt7) {
                 case 1 :
                     // Dialog.g:202:6: 'true'
                     {
                     match("true"); 
 
 
 
                     }
                     break;
                 case 2 :
                     // Dialog.g:202:15: 'false'
                     {
                     match("false"); 
 
 
 
                     }
                     break;
 
             }
 
 
             }
 
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "BOOLEAN_LITERAL"
 
     // $ANTLR start "NUMBER"
     public final void mNUMBER() throws RecognitionException {
         try {
             // Dialog.g:204:17: ( ( '-' )? ( DIGIT )+ ( '.' ( DIGIT )+ )? )
             // Dialog.g:205:5: ( '-' )? ( DIGIT )+ ( '.' ( DIGIT )+ )?
             {
             // Dialog.g:205:5: ( '-' )?
             int alt8=2;
             int LA8_0 = input.LA(1);
 
             if ( (LA8_0=='-') ) {
                 alt8=1;
             }
             switch (alt8) {
                 case 1 :
                     // Dialog.g:205:5: '-'
                     {
                     match('-'); 
 
                     }
                     break;
 
             }
 
 
             // Dialog.g:205:10: ( DIGIT )+
             int cnt9=0;
             loop9:
             do {
                 int alt9=2;
                 int LA9_0 = input.LA(1);
 
                 if ( ((LA9_0 >= '0' && LA9_0 <= '9')) ) {
                     alt9=1;
                 }
 
 
                 switch (alt9) {
             	case 1 :
             	    // Dialog.g:
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
             	    if ( cnt9 >= 1 ) break loop9;
                         EarlyExitException eee =
                             new EarlyExitException(9, input);
                         throw eee;
                 }
                 cnt9++;
             } while (true);
 
 
             // Dialog.g:205:17: ( '.' ( DIGIT )+ )?
             int alt11=2;
             int LA11_0 = input.LA(1);
 
             if ( (LA11_0=='.') ) {
                 alt11=1;
             }
             switch (alt11) {
                 case 1 :
                     // Dialog.g:205:18: '.' ( DIGIT )+
                     {
                     match('.'); 
 
                     // Dialog.g:205:22: ( DIGIT )+
                     int cnt10=0;
                     loop10:
                     do {
                         int alt10=2;
                         int LA10_0 = input.LA(1);
 
                         if ( ((LA10_0 >= '0' && LA10_0 <= '9')) ) {
                             alt10=1;
                         }
 
 
                         switch (alt10) {
                     	case 1 :
                     	    // Dialog.g:
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
                     	    if ( cnt10 >= 1 ) break loop10;
                                 EarlyExitException eee =
                                     new EarlyExitException(10, input);
                                 throw eee;
                         }
                         cnt10++;
                     } while (true);
 
 
                     }
                     break;
 
             }
 
 
             }
 
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "NUMBER"
 
     // $ANTLR start "STRING_LITERAL"
     public final void mSTRING_LITERAL() throws RecognitionException {
         try {
             // Dialog.g:207:25: ( '\"' ( . )+ '\"' | '\\'' ( . )+ '\\'' )
             int alt14=2;
             int LA14_0 = input.LA(1);
 
             if ( (LA14_0=='\"') ) {
                 alt14=1;
             }
             else if ( (LA14_0=='\'') ) {
                 alt14=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 14, 0, input);
 
                 throw nvae;
 
             }
             switch (alt14) {
                 case 1 :
                     // Dialog.g:208:5: '\"' ( . )+ '\"'
                     {
                     match('\"'); 
 
                     // Dialog.g:208:9: ( . )+
                     int cnt12=0;
                     loop12:
                     do {
                         int alt12=2;
                         int LA12_0 = input.LA(1);
 
                         if ( (LA12_0=='\"') ) {
                             alt12=2;
                         }
                         else if ( ((LA12_0 >= '\u0000' && LA12_0 <= '!')||(LA12_0 >= '#' && LA12_0 <= '\uFFFF')) ) {
                             alt12=1;
                         }
 
 
                         switch (alt12) {
                     	case 1 :
                     	    // Dialog.g:208:9: .
                     	    {
                     	    matchAny(); 
 
                     	    }
                     	    break;
 
                     	default :
                     	    if ( cnt12 >= 1 ) break loop12;
                                 EarlyExitException eee =
                                     new EarlyExitException(12, input);
                                 throw eee;
                         }
                         cnt12++;
                     } while (true);
 
 
                     match('\"'); 
 
                     }
                     break;
                 case 2 :
                     // Dialog.g:209:7: '\\'' ( . )+ '\\''
                     {
                     match('\''); 
 
                     // Dialog.g:209:12: ( . )+
                     int cnt13=0;
                     loop13:
                     do {
                         int alt13=2;
                         int LA13_0 = input.LA(1);
 
                         if ( (LA13_0=='\'') ) {
                             alt13=2;
                         }
                         else if ( ((LA13_0 >= '\u0000' && LA13_0 <= '&')||(LA13_0 >= '(' && LA13_0 <= '\uFFFF')) ) {
                             alt13=1;
                         }
 
 
                         switch (alt13) {
                     	case 1 :
                     	    // Dialog.g:209:12: .
                     	    {
                     	    matchAny(); 
 
                     	    }
                     	    break;
 
                     	default :
                     	    if ( cnt13 >= 1 ) break loop13;
                                 EarlyExitException eee =
                                     new EarlyExitException(13, input);
                                 throw eee;
                         }
                         cnt13++;
                     } while (true);
 
 
                     match('\''); 
 
                     }
                     break;
 
             }
 
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "STRING_LITERAL"
 
     public void mTokens() throws RecognitionException {
         // Dialog.g:1:8: ( T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | ML_COMMENT | LINE_COMMENT | WS )
         int alt15=36;
         switch ( input.LA(1) ) {
         case '!':
             {
             alt15=1;
             }
             break;
         case '%':
             {
             alt15=2;
             }
             break;
         case '(':
             {
             alt15=3;
             }
             break;
         case ')':
             {
             alt15=4;
             }
             break;
         case '*':
             {
             int LA15_5 = input.LA(2);
 
             if ( (LA15_5=='*') ) {
                 alt15=6;
             }
             else {
                 alt15=5;
             }
             }
             break;
         case '+':
             {
             alt15=7;
             }
             break;
         case ',':
             {
             alt15=8;
             }
             break;
         case '-':
             {
             int LA15_8 = input.LA(2);
 
             if ( (LA15_8=='p') ) {
                 alt15=10;
             }
             else {
                 alt15=9;
             }
             }
             break;
         case '/':
             {
             switch ( input.LA(2) ) {
             case '*':
                 {
                 alt15=34;
                 }
                 break;
             case '/':
                 {
                 alt15=35;
                 }
                 break;
             default:
                 alt15=11;
             }
 
             }
             break;
         case ';':
             {
             alt15=12;
             }
             break;
         case '<':
             {
             int LA15_11 = input.LA(2);
 
             if ( (LA15_11=='=') ) {
                 alt15=14;
             }
             else {
                 alt15=13;
             }
             }
             break;
         case '=':
             {
             alt15=15;
             }
             break;
         case '>':
             {
             int LA15_13 = input.LA(2);
 
             if ( (LA15_13=='=') ) {
                 alt15=17;
             }
             else {
                 alt15=16;
             }
             }
             break;
         case 'c':
             {
             alt15=18;
             }
             break;
         case 'd':
             {
             alt15=19;
             }
             break;
         case 'e':
             {
             alt15=20;
             }
             break;
         case 'h':
             {
             alt15=21;
             }
             break;
         case 'l':
             {
             alt15=22;
             }
             break;
         case 'm':
             {
             int LA15_19 = input.LA(2);
 
             if ( (LA15_19=='s') ) {
                 alt15=24;
             }
             else {
                 alt15=23;
             }
             }
             break;
         case 'n':
             {
             alt15=25;
             }
             break;
         case 'r':
             {
             int LA15_21 = input.LA(2);
 
             if ( (LA15_21=='e') ) {
                 int LA15_41 = input.LA(3);
 
                 if ( (LA15_41=='m') ) {
                     alt15=26;
                 }
                 else if ( (LA15_41=='s') ) {
                     alt15=27;
                 }
                 else {
                     NoViableAltException nvae =
                         new NoViableAltException("", 15, 41, input);
 
                     throw nvae;
 
                 }
             }
             else if ( (LA15_21=='u') ) {
                 alt15=28;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 15, 21, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 's':
             {
             alt15=29;
             }
             break;
         case 't':
             {
             alt15=30;
             }
             break;
         case 'u':
             {
             alt15=31;
             }
             break;
         case '{':
             {
             alt15=32;
             }
             break;
         case '}':
             {
             alt15=33;
             }
             break;
         case '\t':
         case '\n':
         case '\r':
         case ' ':
             {
             alt15=36;
             }
             break;
         default:
             NoViableAltException nvae =
                 new NoViableAltException("", 15, 0, input);
 
             throw nvae;
 
         }
 
         switch (alt15) {
             case 1 :
                 // Dialog.g:1:10: T__16
                 {
                 mT__16(); 
 
 
                 }
                 break;
             case 2 :
                 // Dialog.g:1:16: T__17
                 {
                 mT__17(); 
 
 
                 }
                 break;
             case 3 :
                 // Dialog.g:1:22: T__18
                 {
                 mT__18(); 
 
 
                 }
                 break;
             case 4 :
                 // Dialog.g:1:28: T__19
                 {
                 mT__19(); 
 
 
                 }
                 break;
             case 5 :
                 // Dialog.g:1:34: T__20
                 {
                 mT__20(); 
 
 
                 }
                 break;
             case 6 :
                 // Dialog.g:1:40: T__21
                 {
                 mT__21(); 
 
 
                 }
                 break;
             case 7 :
                 // Dialog.g:1:46: T__22
                 {
                 mT__22(); 
 
 
                 }
                 break;
             case 8 :
                 // Dialog.g:1:52: T__23
                 {
                 mT__23(); 
 
 
                 }
                 break;
             case 9 :
                 // Dialog.g:1:58: T__24
                 {
                 mT__24(); 
 
 
                 }
                 break;
             case 10 :
                 // Dialog.g:1:64: T__25
                 {
                 mT__25(); 
 
 
                 }
                 break;
             case 11 :
                 // Dialog.g:1:70: T__26
                 {
                 mT__26(); 
 
 
                 }
                 break;
             case 12 :
                 // Dialog.g:1:76: T__27
                 {
                 mT__27(); 
 
 
                 }
                 break;
             case 13 :
                 // Dialog.g:1:82: T__28
                 {
                 mT__28(); 
 
 
                 }
                 break;
             case 14 :
                 // Dialog.g:1:88: T__29
                 {
                 mT__29(); 
 
 
                 }
                 break;
             case 15 :
                 // Dialog.g:1:94: T__30
                 {
                 mT__30(); 
 
 
                 }
                 break;
             case 16 :
                 // Dialog.g:1:100: T__31
                 {
                 mT__31(); 
 
 
                 }
                 break;
             case 17 :
                 // Dialog.g:1:106: T__32
                 {
                 mT__32(); 
 
 
                 }
                 break;
             case 18 :
                 // Dialog.g:1:112: T__33
                 {
                 mT__33(); 
 
 
                 }
                 break;
             case 19 :
                 // Dialog.g:1:118: T__34
                 {
                 mT__34(); 
 
 
                 }
                 break;
             case 20 :
                 // Dialog.g:1:124: T__35
                 {
                 mT__35(); 
 
 
                 }
                 break;
             case 21 :
                 // Dialog.g:1:130: T__36
                 {
                 mT__36(); 
 
 
                 }
                 break;
             case 22 :
                 // Dialog.g:1:136: T__37
                 {
                 mT__37(); 
 
 
                 }
                 break;
             case 23 :
                 // Dialog.g:1:142: T__38
                 {
                 mT__38(); 
 
 
                 }
                 break;
             case 24 :
                 // Dialog.g:1:148: T__39
                 {
                 mT__39(); 
 
 
                 }
                 break;
             case 25 :
                 // Dialog.g:1:154: T__40
                 {
                 mT__40(); 
 
 
                 }
                 break;
             case 26 :
                 // Dialog.g:1:160: T__41
                 {
                 mT__41(); 
 
 
                 }
                 break;
             case 27 :
                 // Dialog.g:1:166: T__42
                 {
                 mT__42(); 
 
 
                 }
                 break;
             case 28 :
                 // Dialog.g:1:172: T__43
                 {
                 mT__43(); 
 
 
                 }
                 break;
             case 29 :
                 // Dialog.g:1:178: T__44
                 {
                 mT__44(); 
 
 
                 }
                 break;
             case 30 :
                 // Dialog.g:1:184: T__45
                 {
                 mT__45(); 
 
 
                 }
                 break;
             case 31 :
                 // Dialog.g:1:190: T__46
                 {
                 mT__46(); 
 
 
                 }
                 break;
             case 32 :
                 // Dialog.g:1:196: T__47
                 {
                 mT__47(); 
 
 
                 }
                 break;
             case 33 :
                 // Dialog.g:1:202: T__48
                 {
                 mT__48(); 
 
 
                 }
                 break;
             case 34 :
                 // Dialog.g:1:208: ML_COMMENT
                 {
                 mML_COMMENT(); 
 
 
                 }
                 break;
             case 35 :
                 // Dialog.g:1:219: LINE_COMMENT
                 {
                 mLINE_COMMENT(); 
 
 
                 }
                 break;
             case 36 :
                 // Dialog.g:1:232: WS
                 {
                 mWS(); 
 
 
                 }
                 break;
 
         }
 
     }
 
 
  
 
 }
