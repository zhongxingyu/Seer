// $ANTLR 3.4 C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g 2012-01-31 22:39:31
 
 	package parser;
     import java.util.LinkedList;
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class WhileLanguageLexer extends Lexer {
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
     public static final int BOOL_LITERAL=4;
     public static final int COMMENT=5;
     public static final int IDENT=6;
     public static final int INT_LITERAL=7;
     public static final int QUANTIFIER=8;
     public static final int WS=9;
 
         private LinkedList<String> reporter;
 
         public void setErrorReporter(LinkedList<String> reporter) {
             this.reporter = reporter;
         }
 
         @Override
         public void emitErrorMessage(String msg) {
             reporter.add(msg);
         }
 
 
     // delegates
     // delegators
     public Lexer[] getDelegates() {
         return new Lexer[] {};
     }
 
     public WhileLanguageLexer() {} 
     public WhileLanguageLexer(CharStream input) {
         this(input, new RecognizerSharedState());
     }
     public WhileLanguageLexer(CharStream input, RecognizerSharedState state) {
         super(input,state);
     }
     public String getGrammarFileName() { return "C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g"; }
 
     // $ANTLR start "T__10"
     public final void mT__10() throws RecognitionException {
         try {
             int _type = T__10;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:19:7: ( '!' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:19:9: '!'
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
     // $ANTLR end "T__10"
 
     // $ANTLR start "T__11"
     public final void mT__11() throws RecognitionException {
         try {
             int _type = T__11;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:20:7: ( '!=' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:20:9: '!='
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
     // $ANTLR end "T__11"
 
     // $ANTLR start "T__12"
     public final void mT__12() throws RecognitionException {
         try {
             int _type = T__12;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:21:7: ( '%' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:21:9: '%'
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
     // $ANTLR end "T__12"
 
     // $ANTLR start "T__13"
     public final void mT__13() throws RecognitionException {
         try {
             int _type = T__13;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:22:7: ( '&' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:22:9: '&'
             {
             match('&'); 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:23:7: ( '(' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:23:9: '('
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
     // $ANTLR end "T__14"
 
     // $ANTLR start "T__15"
     public final void mT__15() throws RecognitionException {
         try {
             int _type = T__15;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:24:7: ( ')' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:24:9: ')'
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
     // $ANTLR end "T__15"
 
     // $ANTLR start "T__16"
     public final void mT__16() throws RecognitionException {
         try {
             int _type = T__16;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:25:7: ( '*' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:25:9: '*'
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
     // $ANTLR end "T__16"
 
     // $ANTLR start "T__17"
     public final void mT__17() throws RecognitionException {
         try {
             int _type = T__17;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:26:7: ( '+' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:26:9: '+'
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
     // $ANTLR end "T__17"
 
     // $ANTLR start "T__18"
     public final void mT__18() throws RecognitionException {
         try {
             int _type = T__18;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:27:7: ( ',' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:27:9: ','
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
     // $ANTLR end "T__18"
 
     // $ANTLR start "T__19"
     public final void mT__19() throws RecognitionException {
         try {
             int _type = T__19;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:28:7: ( '-' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:28:9: '-'
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
     // $ANTLR end "T__19"
 
     // $ANTLR start "T__20"
     public final void mT__20() throws RecognitionException {
         try {
             int _type = T__20;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:29:7: ( '/' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:29:9: '/'
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
     // $ANTLR end "T__20"
 
     // $ANTLR start "T__21"
     public final void mT__21() throws RecognitionException {
         try {
             int _type = T__21;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:30:7: ( ';' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:30:9: ';'
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
     // $ANTLR end "T__21"
 
     // $ANTLR start "T__22"
     public final void mT__22() throws RecognitionException {
         try {
             int _type = T__22;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:31:7: ( '<' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:31:9: '<'
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
     // $ANTLR end "T__22"
 
     // $ANTLR start "T__23"
     public final void mT__23() throws RecognitionException {
         try {
             int _type = T__23;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:32:7: ( '<=' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:32:9: '<='
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
     // $ANTLR end "T__23"
 
     // $ANTLR start "T__24"
     public final void mT__24() throws RecognitionException {
         try {
             int _type = T__24;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:33:7: ( '=' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:33:9: '='
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
     // $ANTLR end "T__24"
 
     // $ANTLR start "T__25"
     public final void mT__25() throws RecognitionException {
         try {
             int _type = T__25;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:34:7: ( '==' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:34:9: '=='
             {
             match("=="); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:35:7: ( '>' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:35:9: '>'
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
     // $ANTLR end "T__26"
 
     // $ANTLR start "T__27"
     public final void mT__27() throws RecognitionException {
         try {
             int _type = T__27;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:36:7: ( '>=' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:36:9: '>='
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
     // $ANTLR end "T__27"
 
     // $ANTLR start "T__28"
     public final void mT__28() throws RecognitionException {
         try {
             int _type = T__28;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:37:7: ( '[' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:37:9: '['
             {
             match('['); 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:38:7: ( ']' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:38:9: ']'
             {
             match(']'); 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:39:7: ( 'array' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:39:9: 'array'
             {
             match("array"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:40:7: ( 'assert' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:40:9: 'assert'
             {
             match("assert"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:41:7: ( 'assume' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:41:9: 'assume'
             {
             match("assume"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:42:7: ( 'axiom' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:42:9: 'axiom'
             {
             match("axiom"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:43:7: ( 'bool' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:43:9: 'bool'
             {
             match("bool"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:44:7: ( 'else' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:44:9: 'else'
             {
             match("else"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:45:7: ( 'ensure' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:45:9: 'ensure'
             {
             match("ensure"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:46:7: ( 'if' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:46:9: 'if'
             {
             match("if"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:47:7: ( 'int' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:47:9: 'int'
             {
             match("int"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:48:7: ( 'invariant' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:48:9: 'invariant'
             {
             match("invariant"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:49:7: ( 'main' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:49:9: 'main'
             {
             match("main"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:50:7: ( 'return' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:50:9: 'return'
             {
             match("return"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:51:7: ( 'while' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:51:9: 'while'
             {
             match("while"); 
 
 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:52:7: ( '{' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:52:9: '{'
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
     // $ANTLR end "T__43"
 
     // $ANTLR start "T__44"
     public final void mT__44() throws RecognitionException {
         try {
             int _type = T__44;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:53:7: ( '|' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:53:9: '|'
             {
             match('|'); 
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:54:7: ( '}' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:54:9: '}'
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
     // $ANTLR end "T__45"
 
     // $ANTLR start "INT_LITERAL"
     public final void mINT_LITERAL() throws RecognitionException {
         try {
             int _type = INT_LITERAL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:426:14: ( ( '0' .. '9' )+ )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:426:16: ( '0' .. '9' )+
             {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:426:16: ( '0' .. '9' )+
             int cnt1=0;
             loop1:
             do {
                 int alt1=2;
                 int LA1_0 = input.LA(1);
 
                 if ( ((LA1_0 >= '0' && LA1_0 <= '9')) ) {
                     alt1=1;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:
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
             	    if ( cnt1 >= 1 ) break loop1;
                         EarlyExitException eee =
                             new EarlyExitException(1, input);
                         throw eee;
                 }
                 cnt1++;
             } while (true);
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "INT_LITERAL"
 
     // $ANTLR start "BOOL_LITERAL"
     public final void mBOOL_LITERAL() throws RecognitionException {
         try {
             int _type = BOOL_LITERAL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:427:14: ( 'true' | 'false' )
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
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:427:16: 'true'
                     {
                     match("true"); 
 
 
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:427:25: 'false'
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
     // $ANTLR end "BOOL_LITERAL"
 
     // $ANTLR start "QUANTIFIER"
     public final void mQUANTIFIER() throws RecognitionException {
         try {
             int _type = QUANTIFIER;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:429:9: ( 'forall' | 'exists' )
             int alt3=2;
             int LA3_0 = input.LA(1);
 
             if ( (LA3_0=='f') ) {
                 alt3=1;
             }
             else if ( (LA3_0=='e') ) {
                 alt3=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 3, 0, input);
 
                 throw nvae;
 
             }
             switch (alt3) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:429:11: 'forall'
                     {
                     match("forall"); 
 
 
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:430:11: 'exists'
                     {
                     match("exists"); 
 
 
 
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
     // $ANTLR end "QUANTIFIER"
 
     // $ANTLR start "COMMENT"
     public final void mCOMMENT() throws RecognitionException {
         try {
             int _type = COMMENT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:432:14: ( '#' ( . )* ( '\\n' | '\\r' ) )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:432:16: '#' ( . )* ( '\\n' | '\\r' )
             {
             match('#'); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:432:20: ( . )*
             loop4:
             do {
                 int alt4=2;
                 int LA4_0 = input.LA(1);
 
                 if ( (LA4_0=='\n'||LA4_0=='\r') ) {
                     alt4=2;
                 }
                 else if ( ((LA4_0 >= '\u0000' && LA4_0 <= '\t')||(LA4_0 >= '\u000B' && LA4_0 <= '\f')||(LA4_0 >= '\u000E' && LA4_0 <= '\uFFFF')) ) {
                     alt4=1;
                 }
 
 
                 switch (alt4) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:432:20: .
             	    {
             	    matchAny(); 
 
             	    }
             	    break;
 
             	default :
             	    break loop4;
                 }
             } while (true);
 
 
             if ( input.LA(1)=='\n'||input.LA(1)=='\r' ) {
                 input.consume();
             }
             else {
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;
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
     // $ANTLR end "COMMENT"
 
     // $ANTLR start "WS"
     public final void mWS() throws RecognitionException {
         try {
             int _type = WS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:433:14: ( ( '\\n' | '\\r' | ' ' | '\\t' )+ )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:433:16: ( '\\n' | '\\r' | ' ' | '\\t' )+
             {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:433:16: ( '\\n' | '\\r' | ' ' | '\\t' )+
             int cnt5=0;
             loop5:
             do {
                 int alt5=2;
                 int LA5_0 = input.LA(1);
 
                 if ( ((LA5_0 >= '\t' && LA5_0 <= '\n')||LA5_0=='\r'||LA5_0==' ') ) {
                     alt5=1;
                 }
 
 
                 switch (alt5) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:
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
             	    if ( cnt5 >= 1 ) break loop5;
                         EarlyExitException eee =
                             new EarlyExitException(5, input);
                         throw eee;
                 }
                 cnt5++;
             } while (true);
 
 
             skip();
 
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
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:434:14: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:434:16: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
             {
             if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                 input.consume();
             }
             else {
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;
             }
 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:434:44: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
             loop6:
             do {
                 int alt6=2;
                 int LA6_0 = input.LA(1);
 
                 if ( ((LA6_0 >= '0' && LA6_0 <= '9')||(LA6_0 >= 'A' && LA6_0 <= 'Z')||LA6_0=='_'||(LA6_0 >= 'a' && LA6_0 <= 'z')) ) {
                     alt6=1;
                 }
 
 
                 switch (alt6) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:
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
         // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:8: ( T__10 | T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | INT_LITERAL | BOOL_LITERAL | QUANTIFIER | COMMENT | WS | IDENT )
         int alt7=42;
         alt7 = dfa7.predict(input);
         switch (alt7) {
             case 1 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:10: T__10
                 {
                 mT__10(); 
 
 
                 }
                 break;
             case 2 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:16: T__11
                 {
                 mT__11(); 
 
 
                 }
                 break;
             case 3 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:22: T__12
                 {
                 mT__12(); 
 
 
                 }
                 break;
             case 4 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:28: T__13
                 {
                 mT__13(); 
 
 
                 }
                 break;
             case 5 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:34: T__14
                 {
                 mT__14(); 
 
 
                 }
                 break;
             case 6 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:40: T__15
                 {
                 mT__15(); 
 
 
                 }
                 break;
             case 7 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:46: T__16
                 {
                 mT__16(); 
 
 
                 }
                 break;
             case 8 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:52: T__17
                 {
                 mT__17(); 
 
 
                 }
                 break;
             case 9 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:58: T__18
                 {
                 mT__18(); 
 
 
                 }
                 break;
             case 10 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:64: T__19
                 {
                 mT__19(); 
 
 
                 }
                 break;
             case 11 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:70: T__20
                 {
                 mT__20(); 
 
 
                 }
                 break;
             case 12 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:76: T__21
                 {
                 mT__21(); 
 
 
                 }
                 break;
             case 13 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:82: T__22
                 {
                 mT__22(); 
 
 
                 }
                 break;
             case 14 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:88: T__23
                 {
                 mT__23(); 
 
 
                 }
                 break;
             case 15 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:94: T__24
                 {
                 mT__24(); 
 
 
                 }
                 break;
             case 16 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:100: T__25
                 {
                 mT__25(); 
 
 
                 }
                 break;
             case 17 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:106: T__26
                 {
                 mT__26(); 
 
 
                 }
                 break;
             case 18 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:112: T__27
                 {
                 mT__27(); 
 
 
                 }
                 break;
             case 19 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:118: T__28
                 {
                 mT__28(); 
 
 
                 }
                 break;
             case 20 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:124: T__29
                 {
                 mT__29(); 
 
 
                 }
                 break;
             case 21 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:130: T__30
                 {
                 mT__30(); 
 
 
                 }
                 break;
             case 22 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:136: T__31
                 {
                 mT__31(); 
 
 
                 }
                 break;
             case 23 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:142: T__32
                 {
                 mT__32(); 
 
 
                 }
                 break;
             case 24 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:148: T__33
                 {
                 mT__33(); 
 
 
                 }
                 break;
             case 25 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:154: T__34
                 {
                 mT__34(); 
 
 
                 }
                 break;
             case 26 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:160: T__35
                 {
                 mT__35(); 
 
 
                 }
                 break;
             case 27 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:166: T__36
                 {
                 mT__36(); 
 
 
                 }
                 break;
             case 28 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:172: T__37
                 {
                 mT__37(); 
 
 
                 }
                 break;
             case 29 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:178: T__38
                 {
                 mT__38(); 
 
 
                 }
                 break;
             case 30 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:184: T__39
                 {
                 mT__39(); 
 
 
                 }
                 break;
             case 31 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:190: T__40
                 {
                 mT__40(); 
 
 
                 }
                 break;
             case 32 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:196: T__41
                 {
                 mT__41(); 
 
 
                 }
                 break;
             case 33 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:202: T__42
                 {
                 mT__42(); 
 
 
                 }
                 break;
             case 34 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:208: T__43
                 {
                 mT__43(); 
 
 
                 }
                 break;
             case 35 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:214: T__44
                 {
                 mT__44(); 
 
 
                 }
                 break;
             case 36 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:220: T__45
                 {
                 mT__45(); 
 
 
                 }
                 break;
             case 37 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:226: INT_LITERAL
                 {
                 mINT_LITERAL(); 
 
 
                 }
                 break;
             case 38 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:238: BOOL_LITERAL
                 {
                 mBOOL_LITERAL(); 
 
 
                 }
                 break;
             case 39 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:251: QUANTIFIER
                 {
                 mQUANTIFIER(); 
 
 
                 }
                 break;
             case 40 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:262: COMMENT
                 {
                 mCOMMENT(); 
 
 
                 }
                 break;
             case 41 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:270: WS
                 {
                 mWS(); 
 
 
                 }
                 break;
             case 42 :
                 // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:1:273: IDENT
                 {
                 mIDENT(); 
 
 
                 }
                 break;
 
         }
 
     }
 
 
     protected DFA7 dfa7 = new DFA7(this);
     static final String DFA7_eotS =
         "\1\uffff\1\42\12\uffff\1\44\1\46\1\50\2\uffff\7\40\4\uffff\2\40"+
         "\13\uffff\7\40\1\77\16\40\1\uffff\1\120\13\40\1\134\1\135\2\40\1"+
         "\uffff\1\40\1\141\2\40\1\144\2\40\1\147\2\40\1\152\2\uffff\3\40"+
         "\1\uffff\1\40\1\157\1\uffff\1\144\1\40\1\uffff\1\161\1\162\1\uffff"+
         "\1\163\1\164\1\40\1\166\1\uffff\1\164\4\uffff\1\40\1\uffff\1\40"+
         "\1\171\1\uffff";
     static final String DFA7_eofS =
         "\172\uffff";
     static final String DFA7_minS =
         "\1\11\1\75\12\uffff\3\75\2\uffff\1\162\1\157\1\154\1\146\1\141\1"+
         "\145\1\150\4\uffff\1\162\1\141\13\uffff\1\162\1\163\1\151\1\157"+
         "\2\163\1\151\1\60\1\164\1\151\1\164\1\151\1\165\1\154\1\162\1\141"+
         "\1\145\1\157\1\154\1\145\1\165\1\163\1\uffff\1\60\1\141\1\156\1"+
         "\165\1\154\1\145\1\163\1\141\1\171\1\162\2\155\2\60\1\162\1\164"+
         "\1\uffff\1\162\1\60\1\162\1\145\1\60\1\145\1\154\1\60\1\164\1\145"+
         "\1\60\2\uffff\1\145\1\163\1\151\1\uffff\1\156\1\60\1\uffff\1\60"+
         "\1\154\1\uffff\2\60\1\uffff\2\60\1\141\1\60\1\uffff\1\60\4\uffff"+
         "\1\156\1\uffff\1\164\1\60\1\uffff";
     static final String DFA7_maxS =
         "\1\175\1\75\12\uffff\3\75\2\uffff\1\170\1\157\1\170\1\156\1\141"+
         "\1\145\1\150\4\uffff\1\162\1\157\13\uffff\1\162\1\163\1\151\1\157"+
         "\2\163\1\151\1\172\1\166\1\151\1\164\1\151\1\165\1\154\1\162\1\141"+
         "\1\165\1\157\1\154\1\145\1\165\1\163\1\uffff\1\172\1\141\1\156\1"+
         "\165\1\154\1\145\1\163\1\141\1\171\1\162\2\155\2\172\1\162\1\164"+
         "\1\uffff\1\162\1\172\1\162\1\145\1\172\1\145\1\154\1\172\1\164\1"+
         "\145\1\172\2\uffff\1\145\1\163\1\151\1\uffff\1\156\1\172\1\uffff"+
         "\1\172\1\154\1\uffff\2\172\1\uffff\2\172\1\141\1\172\1\uffff\1\172"+
         "\4\uffff\1\156\1\uffff\1\164\1\172\1\uffff";
     static final String DFA7_acceptS =
         "\2\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\3\uffff\1"+
         "\23\1\24\7\uffff\1\42\1\43\1\44\1\45\2\uffff\1\50\1\51\1\52\1\2"+
         "\1\1\1\16\1\15\1\20\1\17\1\22\1\21\26\uffff\1\34\20\uffff\1\35\13"+
         "\uffff\1\31\1\32\3\uffff\1\37\2\uffff\1\46\2\uffff\1\25\2\uffff"+
         "\1\30\4\uffff\1\41\1\uffff\1\26\1\27\1\33\1\47\1\uffff\1\40\2\uffff"+
         "\1\36";
     static final String DFA7_specialS =
         "\172\uffff}>";
     static final String[] DFA7_transitionS = {
             "\2\37\2\uffff\1\37\22\uffff\1\37\1\1\1\uffff\1\36\1\uffff\1"+
             "\2\1\3\1\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\uffff\1\12\12\33"+
             "\1\uffff\1\13\1\14\1\15\1\16\2\uffff\32\40\1\17\1\uffff\1\20"+
             "\1\uffff\1\40\1\uffff\1\21\1\22\2\40\1\23\1\35\2\40\1\24\3\40"+
             "\1\25\4\40\1\26\1\40\1\34\2\40\1\27\3\40\1\30\1\31\1\32",
             "\1\41",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "\1\43",
             "\1\45",
             "\1\47",
             "",
             "",
             "\1\51\1\52\4\uffff\1\53",
             "\1\54",
             "\1\55\1\uffff\1\56\11\uffff\1\57",
             "\1\60\7\uffff\1\61",
             "\1\62",
             "\1\63",
             "\1\64",
             "",
             "",
             "",
             "",
             "\1\65",
             "\1\66\15\uffff\1\67",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "\1\70",
             "\1\71",
             "\1\72",
             "\1\73",
             "\1\74",
             "\1\75",
             "\1\76",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\1\100\1\uffff\1\101",
             "\1\102",
             "\1\103",
             "\1\104",
             "\1\105",
             "\1\106",
             "\1\107",
             "\1\110",
             "\1\111\17\uffff\1\112",
             "\1\113",
             "\1\114",
             "\1\115",
             "\1\116",
             "\1\117",
             "",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\1\121",
             "\1\122",
             "\1\123",
             "\1\124",
             "\1\125",
             "\1\126",
             "\1\127",
             "\1\130",
             "\1\131",
             "\1\132",
             "\1\133",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\1\136",
             "\1\137",
             "",
             "\1\140",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\1\142",
             "\1\143",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\1\145",
             "\1\146",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\1\150",
             "\1\151",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "",
             "",
             "\1\153",
             "\1\154",
             "\1\155",
             "",
             "\1\156",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\1\160",
             "",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "\1\165",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             "",
             "",
             "",
             "",
             "\1\167",
             "",
             "\1\170",
             "\12\40\7\uffff\32\40\4\uffff\1\40\1\uffff\32\40",
             ""
     };
 
     static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
     static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
     static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
     static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
     static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
     static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
     static final short[][] DFA7_transition;
 
     static {
         int numStates = DFA7_transitionS.length;
         DFA7_transition = new short[numStates][];
         for (int i=0; i<numStates; i++) {
             DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
         }
     }
 
     class DFA7 extends DFA {
 
         public DFA7(BaseRecognizer recognizer) {
             this.recognizer = recognizer;
             this.decisionNumber = 7;
             this.eot = DFA7_eot;
             this.eof = DFA7_eof;
             this.min = DFA7_min;
             this.max = DFA7_max;
             this.accept = DFA7_accept;
             this.special = DFA7_special;
             this.transition = DFA7_transition;
         }
         public String getDescription() {
             return "1:1: Tokens : ( T__10 | T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | INT_LITERAL | BOOL_LITERAL | QUANTIFIER | COMMENT | WS | IDENT );";
         }
     }
  
 
 }
