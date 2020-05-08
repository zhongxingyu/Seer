// $ANTLR 3.4 /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g 2012-02-11 16:46:37
 
   package org.simplesql.parser;
   
   import org.simplesql.parser.tree.*;
   
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class SQLLexer extends Lexer {
     public static final int EOF=-1;
     public static final int T__32=32;
     public static final int T__33=33;
     public static final int T__34=34;
     public static final int T__35=35;
     public static final int T__36=36;
     public static final int T__37=37;
     public static final int T__38=38;
     public static final int T__39=39;
     public static final int T__40=40;
     public static final int BOOL=4;
     public static final int BY=5;
     public static final int DOUBLE=6;
     public static final int EXPRESSION=7;
     public static final int FROM=8;
     public static final int FUNC=9;
     public static final int GROUP=10;
     public static final int IDENT=11;
     public static final int INTEGER=12;
     public static final int LIMIT=13;
     public static final int LOGICAL=14;
     public static final int LOGICAL_TOKEN=15;
     public static final int MINUS=16;
     public static final int MULT_TOKEN=17;
     public static final int NEGATION_TOKEN=18;
     public static final int NUMBER=19;
     public static final int ORDER=20;
     public static final int PLUS=21;
     public static final int REAL_NUMBER=22;
     public static final int RELATION=23;
     public static final int RELATION_TOKEN=24;
     public static final int SELECT=25;
     public static final int STRING=26;
     public static final int STRING_LITERAL=27;
     public static final int UNARY=28;
     public static final int VARIABLE=29;
     public static final int WHERE=30;
     public static final int WS=31;
 
     // delegates
     // delegators
     public Lexer[] getDelegates() {
         return new Lexer[] {};
     }
 
     public SQLLexer() {} 
     public SQLLexer(CharStream input) {
         this(input, new RecognizerSharedState());
     }
     public SQLLexer(CharStream input, RecognizerSharedState state) {
         super(input,state);
     }
     public String getGrammarFileName() { return "/home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g"; }
 
     // $ANTLR start "T__32"
     public final void mT__32() throws RecognitionException {
         try {
             int _type = T__32;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:14:7: ( '(' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:14:9: '('
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
     // $ANTLR end "T__32"
 
     // $ANTLR start "T__33"
     public final void mT__33() throws RecognitionException {
         try {
             int _type = T__33;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:15:7: ( ')' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:15:9: ')'
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
     // $ANTLR end "T__33"
 
     // $ANTLR start "T__34"
     public final void mT__34() throws RecognitionException {
         try {
             int _type = T__34;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:16:7: ( '*' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:16:9: '*'
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
     // $ANTLR end "T__34"
 
     // $ANTLR start "T__35"
     public final void mT__35() throws RecognitionException {
         try {
             int _type = T__35;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:17:7: ( '+' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:17:9: '+'
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
     // $ANTLR end "T__35"
 
     // $ANTLR start "T__36"
     public final void mT__36() throws RecognitionException {
         try {
             int _type = T__36;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:18:7: ( ',' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:18:9: ','
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
     // $ANTLR end "T__36"
 
     // $ANTLR start "T__37"
     public final void mT__37() throws RecognitionException {
         try {
             int _type = T__37;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:19:7: ( '-' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:19:9: '-'
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
     // $ANTLR end "T__37"
 
     // $ANTLR start "T__38"
     public final void mT__38() throws RecognitionException {
         try {
             int _type = T__38;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:20:7: ( '/' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:20:9: '/'
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
     // $ANTLR end "T__38"
 
     // $ANTLR start "T__39"
     public final void mT__39() throws RecognitionException {
         try {
             int _type = T__39;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:21:7: ( ';' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:21:9: ';'
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
     // $ANTLR end "T__39"
 
     // $ANTLR start "T__40"
     public final void mT__40() throws RecognitionException {
         try {
             int _type = T__40;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:22:7: ( 'mod' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:22:9: 'mod'
             {
             match("mod"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "T__40"
 
     // $ANTLR start "RELATION"
     public final void mRELATION() throws RecognitionException {
         try {
             int _type = RELATION;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:134:10: ( ( '<' | '>' | '<=' | '>=' | '!=' | '=' ) )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:134:12: ( '<' | '>' | '<=' | '>=' | '!=' | '=' )
             {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:134:12: ( '<' | '>' | '<=' | '>=' | '!=' | '=' )
             int alt1=6;
             switch ( input.LA(1) ) {
             case '<':
                 {
                 int LA1_1 = input.LA(2);
 
                 if ( (LA1_1=='=') ) {
                     alt1=3;
                 }
                 else {
                     alt1=1;
                 }
                 }
                 break;
             case '>':
                 {
                 int LA1_2 = input.LA(2);
 
                 if ( (LA1_2=='=') ) {
                     alt1=4;
                 }
                 else {
                     alt1=2;
                 }
                 }
                 break;
             case '!':
                 {
                 alt1=5;
                 }
                 break;
             case '=':
                 {
                 alt1=6;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 1, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt1) {
                 case 1 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:134:13: '<'
                     {
                     match('<'); 
 
                     }
                     break;
                 case 2 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:134:19: '>'
                     {
                     match('>'); 
 
                     }
                     break;
                 case 3 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:134:25: '<='
                     {
                     match("<="); 
 
 
 
                     }
                     break;
                 case 4 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:134:32: '>='
                     {
                     match(">="); 
 
 
 
                     }
                     break;
                 case 5 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:134:39: '!='
                     {
                     match("!="); 
 
 
 
                     }
                     break;
                 case 6 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:134:46: '='
                     {
                     match('='); 
 
                     }
                     break;
 
             }
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "RELATION"
 
     // $ANTLR start "LOGICAL"
     public final void mLOGICAL() throws RecognitionException {
         try {
             int _type = LOGICAL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:135:9: ( ( '&&' | '||' | ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) ) | ( 'O' | 'o' ) ( 'R' | 'r' ) )
             int alt3=2;
             int LA3_0 = input.LA(1);
 
             if ( (LA3_0=='&'||LA3_0=='A'||LA3_0=='a'||LA3_0=='|') ) {
                 alt3=1;
             }
             else if ( (LA3_0=='O'||LA3_0=='o') ) {
                 alt3=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 3, 0, input);
 
                 throw nvae;
 
             }
             switch (alt3) {
                 case 1 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:135:11: ( '&&' | '||' | ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
                     {
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:135:11: ( '&&' | '||' | ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
                     int alt2=3;
                     switch ( input.LA(1) ) {
                     case '&':
                         {
                         alt2=1;
                         }
                         break;
                     case '|':
                         {
                         alt2=2;
                         }
                         break;
                     case 'A':
                     case 'a':
                         {
                         alt2=3;
                         }
                         break;
                     default:
                         NoViableAltException nvae =
                             new NoViableAltException("", 2, 0, input);
 
                         throw nvae;
 
                     }
 
                     switch (alt2) {
                         case 1 :
                             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:135:12: '&&'
                             {
                             match("&&"); 
 
 
 
                             }
                             break;
                         case 2 :
                             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:135:19: '||'
                             {
                             match("||"); 
 
 
 
                             }
                             break;
                         case 3 :
                             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:135:26: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
                             {
                             if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                                 input.consume();
                             }
                             else {
                                 MismatchedSetException mse = new MismatchedSetException(null,input);
                                 recover(mse);
                                 throw mse;
                             }
 
 
                             if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                                 input.consume();
                             }
                             else {
                                 MismatchedSetException mse = new MismatchedSetException(null,input);
                                 recover(mse);
                                 throw mse;
                             }
 
 
                             if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
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
                     break;
                 case 2 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:135:57: ( 'O' | 'o' ) ( 'R' | 'r' )
                     {
                     if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                         input.consume();
                     }
                     else {
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;
                     }
 
 
                     if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
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
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "LOGICAL"
 
     // $ANTLR start "SELECT"
     public final void mSELECT() throws RecognitionException {
         try {
             int _type = SELECT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:137:8: ( 'SELECT' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:137:10: 'SELECT'
             {
             match("SELECT"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "SELECT"
 
     // $ANTLR start "WHERE"
     public final void mWHERE() throws RecognitionException {
         try {
             int _type = WHERE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:138:7: ( 'WHERE' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:138:9: 'WHERE'
             {
             match("WHERE"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "WHERE"
 
     // $ANTLR start "ORDER"
     public final void mORDER() throws RecognitionException {
         try {
             int _type = ORDER;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:139:6: ( 'ORDER' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:139:8: 'ORDER'
             {
             match("ORDER"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "ORDER"
 
     // $ANTLR start "FROM"
     public final void mFROM() throws RecognitionException {
         try {
             int _type = FROM;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:140:6: ( 'FROM' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:140:8: 'FROM'
             {
             match("FROM"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "FROM"
 
     // $ANTLR start "GROUP"
     public final void mGROUP() throws RecognitionException {
         try {
             int _type = GROUP;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:141:7: ( 'GROUP' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:141:9: 'GROUP'
             {
             match("GROUP"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "GROUP"
 
     // $ANTLR start "BY"
     public final void mBY() throws RecognitionException {
         try {
             int _type = BY;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:142:4: ( 'BY' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:142:6: 'BY'
             {
             match("BY"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "BY"
 
     // $ANTLR start "LIMIT"
     public final void mLIMIT() throws RecognitionException {
         try {
             int _type = LIMIT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:143:7: ( 'LIMIT' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:143:9: 'LIMIT'
             {
             match("LIMIT"); 
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "LIMIT"
 
     // $ANTLR start "DOUBLE"
     public final void mDOUBLE() throws RecognitionException {
         try {
             int _type = DOUBLE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:145:8: ( INTEGER '.' INTEGER )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:145:10: INTEGER '.' INTEGER
             {
             mINTEGER(); 
 
 
             match('.'); 
 
             mINTEGER(); 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "DOUBLE"
 
     // $ANTLR start "INTEGER"
     public final void mINTEGER() throws RecognitionException {
         try {
             int _type = INTEGER;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:146:9: ( ( '0' .. '9' )+ )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:146:11: ( '0' .. '9' )+
             {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:146:11: ( '0' .. '9' )+
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
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:
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
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "INTEGER"
 
     // $ANTLR start "BOOL"
     public final void mBOOL() throws RecognitionException {
         try {
             int _type = BOOL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:6: ( ( 'true' ) | ( 'TRUE' ) | ( 'false' ) | ( 'FALSE' ) | '0' | '1' )
             int alt5=6;
             switch ( input.LA(1) ) {
             case 't':
                 {
                 alt5=1;
                 }
                 break;
             case 'T':
                 {
                 alt5=2;
                 }
                 break;
             case 'f':
                 {
                 alt5=3;
                 }
                 break;
             case 'F':
                 {
                 alt5=4;
                 }
                 break;
             case '0':
                 {
                 alt5=5;
                 }
                 break;
             case '1':
                 {
                 alt5=6;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 5, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt5) {
                 case 1 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:8: ( 'true' )
                     {
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:8: ( 'true' )
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:9: 'true'
                     {
                     match("true"); 
 
 
 
                     }
 
 
                     }
                     break;
                 case 2 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:19: ( 'TRUE' )
                     {
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:19: ( 'TRUE' )
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:20: 'TRUE'
                     {
                     match("TRUE"); 
 
 
 
                     }
 
 
                     }
                     break;
                 case 3 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:31: ( 'false' )
                     {
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:31: ( 'false' )
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:32: 'false'
                     {
                     match("false"); 
 
 
 
                     }
 
 
                     }
                     break;
                 case 4 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:43: ( 'FALSE' )
                     {
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:43: ( 'FALSE' )
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:44: 'FALSE'
                     {
                     match("FALSE"); 
 
 
 
                     }
 
 
                     }
                     break;
                 case 5 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:55: '0'
                     {
                     match('0'); 
 
                     }
                     break;
                 case 6 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:147:61: '1'
                     {
                     match('1'); 
 
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
 
     // $ANTLR start "STRING_LITERAL"
     public final void mSTRING_LITERAL() throws RecognitionException {
         try {
             int _type = STRING_LITERAL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:149:16: ( '\"' (~ ( '\"' | '\\n' | '\\r' ) )* '\"' | '\\'' (~ ( '\\'' | '\\n' | '\\r' ) )* '\\'' )
             int alt8=2;
             int LA8_0 = input.LA(1);
 
             if ( (LA8_0=='\"') ) {
                 alt8=1;
             }
             else if ( (LA8_0=='\'') ) {
                 alt8=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 8, 0, input);
 
                 throw nvae;
 
             }
             switch (alt8) {
                 case 1 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:150:3: '\"' (~ ( '\"' | '\\n' | '\\r' ) )* '\"'
                     {
                     match('\"'); 
 
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:150:7: (~ ( '\"' | '\\n' | '\\r' ) )*
                     loop6:
                     do {
                         int alt6=2;
                         int LA6_0 = input.LA(1);
 
                         if ( ((LA6_0 >= '\u0000' && LA6_0 <= '\t')||(LA6_0 >= '\u000B' && LA6_0 <= '\f')||(LA6_0 >= '\u000E' && LA6_0 <= '!')||(LA6_0 >= '#' && LA6_0 <= '\uFFFF')) ) {
                             alt6=1;
                         }
 
 
                         switch (alt6) {
                     	case 1 :
                     	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:
                     	    {
                     	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '\uFFFF') ) {
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
 
 
                     match('\"'); 
 
                     }
                     break;
                 case 2 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:150:33: '\\'' (~ ( '\\'' | '\\n' | '\\r' ) )* '\\''
                     {
                     match('\''); 
 
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:150:38: (~ ( '\\'' | '\\n' | '\\r' ) )*
                     loop7:
                     do {
                         int alt7=2;
                         int LA7_0 = input.LA(1);
 
                         if ( ((LA7_0 >= '\u0000' && LA7_0 <= '\t')||(LA7_0 >= '\u000B' && LA7_0 <= '\f')||(LA7_0 >= '\u000E' && LA7_0 <= '&')||(LA7_0 >= '(' && LA7_0 <= '\uFFFF')) ) {
                             alt7=1;
                         }
 
 
                         switch (alt7) {
                     	case 1 :
                     	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:
                     	    {
                     	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '\uFFFF') ) {
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
                     	    break loop7;
                         }
                     } while (true);
 
 
                     match('\''); 
 
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
     // $ANTLR end "STRING_LITERAL"
 
     // $ANTLR start "IDENT"
     public final void mIDENT() throws RecognitionException {
         try {
             int _type = IDENT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:152:7: ( ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '-' | '$' | INTEGER )* )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:152:9: ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '-' | '$' | INTEGER )*
             {
             if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                 input.consume();
             }
             else {
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;
             }
 
 
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:152:30: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '-' | '$' | INTEGER )*
             loop9:
             do {
                 int alt9=7;
                 switch ( input.LA(1) ) {
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
                 case 'p':
                 case 'q':
                 case 'r':
                 case 's':
                 case 't':
                 case 'u':
                 case 'v':
                 case 'w':
                 case 'x':
                 case 'y':
                 case 'z':
                     {
                     alt9=1;
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
                     {
                     alt9=2;
                     }
                     break;
                 case '_':
                     {
                     alt9=3;
                     }
                     break;
                 case '-':
                     {
                     alt9=4;
                     }
                     break;
                 case '$':
                     {
                     alt9=5;
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
                     alt9=6;
                     }
                     break;
 
                 }
 
                 switch (alt9) {
             	case 1 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:152:31: 'a' .. 'z'
             	    {
             	    matchRange('a','z'); 
 
             	    }
             	    break;
             	case 2 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:152:42: 'A' .. 'Z'
             	    {
             	    matchRange('A','Z'); 
 
             	    }
             	    break;
             	case 3 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:152:53: '_'
             	    {
             	    match('_'); 
 
             	    }
             	    break;
             	case 4 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:152:59: '-'
             	    {
             	    match('-'); 
 
             	    }
             	    break;
             	case 5 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:152:65: '$'
             	    {
             	    match('$'); 
 
             	    }
             	    break;
             	case 6 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:152:71: INTEGER
             	    {
             	    mINTEGER(); 
 
 
             	    }
             	    break;
 
             	default :
             	    break loop9;
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
 
     // $ANTLR start "WS"
     public final void mWS() throws RecognitionException {
         try {
             int _type = WS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:154:4: ( ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+ )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:154:6: ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+
             {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:154:6: ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+
             int cnt10=0;
             loop10:
             do {
                 int alt10=2;
                 int LA10_0 = input.LA(1);
 
                 if ( ((LA10_0 >= '\t' && LA10_0 <= '\n')||(LA10_0 >= '\f' && LA10_0 <= '\r')||LA10_0==' ') ) {
                     alt10=1;
                 }
 
 
                 switch (alt10) {
             	case 1 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:
             	    {
             	    if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
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
 
     public void mTokens() throws RecognitionException {
         // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:8: ( T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | RELATION | LOGICAL | SELECT | WHERE | ORDER | FROM | GROUP | BY | LIMIT | DOUBLE | INTEGER | BOOL | STRING_LITERAL | IDENT | WS )
         int alt11=24;
         alt11 = dfa11.predict(input);
         switch (alt11) {
             case 1 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:10: T__32
                 {
                 mT__32(); 
 
 
                 }
                 break;
             case 2 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:16: T__33
                 {
                 mT__33(); 
 
 
                 }
                 break;
             case 3 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:22: T__34
                 {
                 mT__34(); 
 
 
                 }
                 break;
             case 4 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:28: T__35
                 {
                 mT__35(); 
 
 
                 }
                 break;
             case 5 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:34: T__36
                 {
                 mT__36(); 
 
 
                 }
                 break;
             case 6 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:40: T__37
                 {
                 mT__37(); 
 
 
                 }
                 break;
             case 7 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:46: T__38
                 {
                 mT__38(); 
 
 
                 }
                 break;
             case 8 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:52: T__39
                 {
                 mT__39(); 
 
 
                 }
                 break;
             case 9 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:58: T__40
                 {
                 mT__40(); 
 
 
                 }
                 break;
             case 10 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:64: RELATION
                 {
                 mRELATION(); 
 
 
                 }
                 break;
             case 11 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:73: LOGICAL
                 {
                 mLOGICAL(); 
 
 
                 }
                 break;
             case 12 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:81: SELECT
                 {
                 mSELECT(); 
 
 
                 }
                 break;
             case 13 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:88: WHERE
                 {
                 mWHERE(); 
 
 
                 }
                 break;
             case 14 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:94: ORDER
                 {
                 mORDER(); 
 
 
                 }
                 break;
             case 15 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:100: FROM
                 {
                 mFROM(); 
 
 
                 }
                 break;
             case 16 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:105: GROUP
                 {
                 mGROUP(); 
 
 
                 }
                 break;
             case 17 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:111: BY
                 {
                 mBY(); 
 
 
                 }
                 break;
             case 18 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:114: LIMIT
                 {
                 mLIMIT(); 
 
 
                 }
                 break;
             case 19 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:120: DOUBLE
                 {
                 mDOUBLE(); 
 
 
                 }
                 break;
             case 20 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:127: INTEGER
                 {
                 mINTEGER(); 
 
 
                 }
                 break;
             case 21 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:135: BOOL
                 {
                 mBOOL(); 
 
 
                 }
                 break;
             case 22 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:140: STRING_LITERAL
                 {
                 mSTRING_LITERAL(); 
 
 
                 }
                 break;
             case 23 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:155: IDENT
                 {
                 mIDENT(); 
 
 
                 }
                 break;
             case 24 :
                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:1:161: WS
                 {
                 mWS(); 
 
 
                 }
                 break;
 
         }
 
     }
 
 
     protected DFA11 dfa11 = new DFA11(this);
     static final String DFA11_eotS =
         "\11\uffff\1\34\2\uffff\11\34\1\53\3\34\2\53\3\uffff\3\34\2\13\2"+
         "\34\1\13\3\34\1\71\1\34\2\uffff\3\34\1\76\2\13\6\34\1\uffff\4\34"+
         "\1\uffff\3\34\1\114\3\34\2\120\1\34\1\122\1\34\1\124\1\uffff\1\120"+
         "\1\125\1\126\1\uffff\1\120\1\uffff\1\127\4\uffff";
     static final String DFA11_eofS =
         "\130\uffff";
     static final String DFA11_minS =
         "\1\11\10\uffff\1\157\2\uffff\1\116\1\122\1\105\1\110\1\122\1\101"+
         "\1\122\1\131\1\111\1\56\1\162\1\122\1\141\2\56\3\uffff\1\144\2\104"+
         "\2\44\1\114\1\105\1\44\1\117\1\114\1\117\1\44\1\115\2\uffff\1\165"+
         "\1\125\1\154\3\44\2\105\1\122\1\115\1\123\1\125\1\uffff\1\111\1"+
         "\145\1\105\1\163\1\uffff\1\122\1\103\1\105\1\44\1\105\1\120\1\124"+
         "\2\44\1\145\1\44\1\124\1\44\1\uffff\3\44\1\uffff\1\44\1\uffff\1"+
         "\44\4\uffff";
     static final String DFA11_maxS =
         "\1\174\10\uffff\1\157\2\uffff\1\156\1\162\1\105\1\110\1\162\2\122"+
         "\1\131\1\111\1\71\1\162\1\122\1\141\2\71\3\uffff\3\144\2\172\1\114"+
         "\1\105\1\172\1\117\1\114\1\117\1\172\1\115\2\uffff\1\165\1\125\1"+
         "\154\3\172\2\105\1\122\1\115\1\123\1\125\1\uffff\1\111\1\145\1\105"+
         "\1\163\1\uffff\1\122\1\103\1\105\1\172\1\105\1\120\1\124\2\172\1"+
         "\145\1\172\1\124\1\172\1\uffff\3\172\1\uffff\1\172\1\uffff\1\172"+
         "\4\uffff";
     static final String DFA11_acceptS =
         "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\uffff\1\12\1\13\17\uffff"+
         "\1\26\1\27\1\30\15\uffff\1\24\1\23\14\uffff\1\21\4\uffff\1\11\15"+
         "\uffff\1\17\3\uffff\1\25\1\uffff\1\16\1\uffff\1\15\1\20\1\22\1\14";
     static final String DFA11_specialS =
         "\130\uffff}>";
     static final String[] DFA11_transitionS = {
             "\2\35\1\uffff\2\35\22\uffff\1\35\1\12\1\33\3\uffff\1\13\1\33"+
             "\1\1\1\2\1\3\1\4\1\5\1\6\1\uffff\1\7\1\25\1\31\10\32\1\uffff"+
             "\1\10\3\12\2\uffff\1\14\1\23\3\34\1\21\1\22\4\34\1\24\2\34\1"+
             "\15\3\34\1\16\1\27\2\34\1\17\3\34\6\uffff\1\14\4\34\1\30\6\34"+
             "\1\11\1\34\1\20\4\34\1\26\6\34\1\uffff\1\13",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "\1\36",
             "",
             "",
             "\1\40\37\uffff\1\37",
             "\1\41\37\uffff\1\42",
             "\1\43",
             "\1\44",
             "\1\45\37\uffff\1\42",
             "\1\47\20\uffff\1\46",
             "\1\50",
             "\1\51",
             "\1\52",
             "\1\54\1\uffff\12\32",
             "\1\55",
             "\1\56",
             "\1\57",
             "\1\54\1\uffff\12\32",
             "\1\54\1\uffff\12\32",
             "",
             "",
             "",
             "\1\60",
             "\1\62\37\uffff\1\61",
             "\1\62\37\uffff\1\61",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\3\34\1\63\26\34\4"+
             "\uffff\1\34\1\uffff\32\34",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\64",
             "\1\65",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\66",
             "\1\67",
             "\1\70",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\72",
             "",
             "",
             "\1\73",
             "\1\74",
             "\1\75",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\77",
             "\1\100",
             "\1\101",
             "\1\102",
             "\1\103",
             "\1\104",
             "",
             "\1\105",
             "\1\106",
             "\1\107",
             "\1\110",
             "",
             "\1\111",
             "\1\112",
             "\1\113",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\115",
             "\1\116",
             "\1\117",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\121",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\123",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "",
             "\1\34\10\uffff\1\34\2\uffff\12\34\7\uffff\32\34\4\uffff\1\34"+
             "\1\uffff\32\34",
             "",
             "",
             "",
             ""
     };
 
     static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
     static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
     static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
     static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
     static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
     static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
     static final short[][] DFA11_transition;
 
     static {
         int numStates = DFA11_transitionS.length;
         DFA11_transition = new short[numStates][];
         for (int i=0; i<numStates; i++) {
             DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
         }
     }
 
     class DFA11 extends DFA {
 
         public DFA11(BaseRecognizer recognizer) {
             this.recognizer = recognizer;
             this.decisionNumber = 11;
             this.eot = DFA11_eot;
             this.eof = DFA11_eof;
             this.min = DFA11_min;
             this.max = DFA11_max;
             this.accept = DFA11_accept;
             this.special = DFA11_special;
             this.transition = DFA11_transition;
         }
         public String getDescription() {
             return "1:1: Tokens : ( T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | RELATION | LOGICAL | SELECT | WHERE | ORDER | FROM | GROUP | BY | LIMIT | DOUBLE | INTEGER | BOOL | STRING_LITERAL | IDENT | WS );";
         }
     }
  
 
 }
