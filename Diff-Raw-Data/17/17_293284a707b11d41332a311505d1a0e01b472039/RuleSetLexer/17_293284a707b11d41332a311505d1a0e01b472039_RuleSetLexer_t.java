// $ANTLR 3.2 Sep 23, 2009 12:02:23 RuleSet.g 2010-09-08 11:21:42
 
 	package com.kynetx;
 	
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 public class RuleSetLexer extends Lexer {
     public static final int FUNCTION=50;
     public static final int ARROW_RIGHT=29;
     public static final int EXPONENT=86;
     public static final int LEFT_BRACKET=63;
     public static final int EMIT=70;
     public static final int OCTAL_ESC=89;
     public static final int FOR=26;
     public static final int PRE=37;
     public static final int FLOAT=60;
     public static final int NOT=41;
     public static final int HTML=49;
     public static final int CALLBACKS=15;
     public static final int AND=53;
     public static final int EOF=-1;
     public static final int REQUIRE=74;
     public static final int META=71;
     public static final int IF=14;
     public static final int LEFT_CURL=5;
     public static final int HISTORY=66;
     public static final int SUCCESS=16;
     public static final int RULE_SET=4;
     public static final int RIGHT_PAREN=32;
     public static final int ESC_SEQ=82;
     public static final int REX=57;
     public static final int SETTING=36;
     public static final int CSS=68;
     public static final int USING=35;
     public static final int COMMA=31;
     public static final int OFF=76;
     public static final int REPLACE=11;
     public static final int AND_AND=33;
     public static final int EQUAL=18;
     public static final int FAILURE=17;
     public static final int RIGHT_SMALL_ARROW=81;
     public static final int RIGHT_BRACKET=64;
     public static final int PIPE=51;
     public static final int LEFT_SMALL_ARROW=48;
     public static final int RIGHT_CURL=6;
     public static final int VAR=7;
     public static final int PREDOP=54;
     public static final int DOT=59;
     public static final int COMMENT=83;
     public static final int WITH=25;
     public static final int VAR_DOMAIN=20;
     public static final int LIKE=10;
     public static final int AUTHZ=73;
     public static final int OR_OR=40;
     public static final int OTHER_OPERATORS=9;
     public static final int MULT_OP=56;
     public static final int CHOOSE=28;
     public static final int MARK=24;
     public static final int POUND=85;
     public static final int KEY=72;
     public static final int WEB=43;
     public static final int UNICODE_ESC=88;
     public static final int JS=34;
     public static final int ADD_OP=55;
     public static final int EVERY=27;
     public static final int ON=45;
     public static final int CACHABLE=69;
     public static final int HEX_DIGIT=87;
     public static final int MATCH=12;
     public static final int INT=8;
     public static final int LOGGING=75;
     public static final int MODULE=79;
     public static final int SEMI=13;
     public static final int TRUE=61;
     public static final int DTYPE=47;
     public static final int SEEN=58;
     public static final int CURRENT=65;
     public static final int COLON=21;
     public static final int COUNTER_OP=22;
     public static final int WS=84;
     public static final int WHEN=39;
     public static final int JAVASCRIPT=78;
     public static final int OR=52;
     public static final int PAGEVIEW=44;
     public static final int ALIAS=80;
     public static final int WITHIN=67;
     public static final int LEFT_PAREN=30;
     public static final int FORGET=23;
     public static final int FOREACH=38;
     public static final int USE=77;
     public static final int GLOBAL=46;
     public static final int FALSE=62;
     public static final int BETWEEN=42;
     public static final int STRING=19;
 
     	public boolean canbeReg = true;
 
 
     // delegates
     // delegators
 
     public RuleSetLexer() {;} 
     public RuleSetLexer(CharStream input) {
         this(input, new RecognizerSharedState());
     }
     public RuleSetLexer(CharStream input, RecognizerSharedState state) {
         super(input,state);
 
     }
     public String getGrammarFileName() { return "RuleSet.g"; }
 
     // $ANTLR start "REX"
     public final void mREX() throws RecognitionException {
         try {
             int _type = REX;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1723:6: ( 're/' ( ( ESC_SEQ )=> ESC_SEQ | '\\\\/' | ~ ( '/' ) )* '/' ( 'g' | 'i' | 'm' )* | '#' ( ( ESC_SEQ )=> ESC_SEQ | '\\\\#' | ~ ( '#' ) )* '#' ( 'g' | 'i' | 'm' )* )
             int alt5=2;
             int LA5_0 = input.LA(1);
 
             if ( (LA5_0=='r') ) {
                 alt5=1;
             }
             else if ( (LA5_0=='#') ) {
                 alt5=2;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 5, 0, input);
 
                 throw nvae;
             }
             switch (alt5) {
                 case 1 :
                     // RuleSet.g:1723:8: 're/' ( ( ESC_SEQ )=> ESC_SEQ | '\\\\/' | ~ ( '/' ) )* '/' ( 'g' | 'i' | 'm' )*
                     {
                     match("re/"); if (state.failed) return ;
 
                     // RuleSet.g:1723:14: ( ( ESC_SEQ )=> ESC_SEQ | '\\\\/' | ~ ( '/' ) )*
                     loop1:
                     do {
                         int alt1=4;
                         alt1 = dfa1.predict(input);
                         switch (alt1) {
                     	case 1 :
                     	    // RuleSet.g:1723:15: ( ESC_SEQ )=> ESC_SEQ
                     	    {
                     	    mESC_SEQ(); if (state.failed) return ;
 
                     	    }
                     	    break;
                     	case 2 :
                     	    // RuleSet.g:1723:36: '\\\\/'
                     	    {
                     	    match("\\/"); if (state.failed) return ;
 
 
                     	    }
                     	    break;
                     	case 3 :
                     	    // RuleSet.g:1723:44: ~ ( '/' )
                     	    {
                     	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='.')||(input.LA(1)>='0' && input.LA(1)<='\uFFFF') ) {
                     	        input.consume();
                     	    state.failed=false;
                     	    }
                     	    else {
                     	        if (state.backtracking>0) {state.failed=true; return ;}
                     	        MismatchedSetException mse = new MismatchedSetException(null,input);
                     	        recover(mse);
                     	        throw mse;}
 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop1;
                         }
                     } while (true);
 
                     match('/'); if (state.failed) return ;
                     // RuleSet.g:1723:59: ( 'g' | 'i' | 'm' )*
                     loop2:
                     do {
                         int alt2=2;
                         int LA2_0 = input.LA(1);
 
                         if ( (LA2_0=='g'||LA2_0=='i'||LA2_0=='m') ) {
                             alt2=1;
                         }
 
 
                         switch (alt2) {
                     	case 1 :
                     	    // RuleSet.g:
                     	    {
                     	    if ( input.LA(1)=='g'||input.LA(1)=='i'||input.LA(1)=='m' ) {
                     	        input.consume();
                     	    state.failed=false;
                     	    }
                     	    else {
                     	        if (state.backtracking>0) {state.failed=true; return ;}
                     	        MismatchedSetException mse = new MismatchedSetException(null,input);
                     	        recover(mse);
                     	        throw mse;}
 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop2;
                         }
                     } while (true);
 
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1724:9: '#' ( ( ESC_SEQ )=> ESC_SEQ | '\\\\#' | ~ ( '#' ) )* '#' ( 'g' | 'i' | 'm' )*
                     {
                     match('#'); if (state.failed) return ;
                     // RuleSet.g:1724:13: ( ( ESC_SEQ )=> ESC_SEQ | '\\\\#' | ~ ( '#' ) )*
                     loop3:
                     do {
                         int alt3=4;
                         alt3 = dfa3.predict(input);
                         switch (alt3) {
                     	case 1 :
                     	    // RuleSet.g:1724:14: ( ESC_SEQ )=> ESC_SEQ
                     	    {
                     	    mESC_SEQ(); if (state.failed) return ;
 
                     	    }
                     	    break;
                     	case 2 :
                     	    // RuleSet.g:1724:35: '\\\\#'
                     	    {
                     	    match("\\#"); if (state.failed) return ;
 
 
                     	    }
                     	    break;
                     	case 3 :
                     	    // RuleSet.g:1724:43: ~ ( '#' )
                     	    {
                     	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\"')||(input.LA(1)>='$' && input.LA(1)<='\uFFFF') ) {
                     	        input.consume();
                     	    state.failed=false;
                     	    }
                     	    else {
                     	        if (state.backtracking>0) {state.failed=true; return ;}
                     	        MismatchedSetException mse = new MismatchedSetException(null,input);
                     	        recover(mse);
                     	        throw mse;}
 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop3;
                         }
                     } while (true);
 
                     match('#'); if (state.failed) return ;
                     // RuleSet.g:1724:58: ( 'g' | 'i' | 'm' )*
                     loop4:
                     do {
                         int alt4=2;
                         int LA4_0 = input.LA(1);
 
                         if ( (LA4_0=='g'||LA4_0=='i'||LA4_0=='m') ) {
                             alt4=1;
                         }
 
 
                         switch (alt4) {
                     	case 1 :
                     	    // RuleSet.g:
                     	    {
                     	    if ( input.LA(1)=='g'||input.LA(1)=='i'||input.LA(1)=='m' ) {
                     	        input.consume();
                     	    state.failed=false;
                     	    }
                     	    else {
                     	        if (state.backtracking>0) {state.failed=true; return ;}
                     	        MismatchedSetException mse = new MismatchedSetException(null,input);
                     	        recover(mse);
                     	        throw mse;}
 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop4;
                         }
                     } while (true);
 
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "REX"
 
     // $ANTLR start "ARROW_RIGHT"
     public final void mARROW_RIGHT() throws RecognitionException {
         try {
             int _type = ARROW_RIGHT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1742:2: ( '=>' )
             // RuleSet.g:1742:4: '=>'
             {
             match("=>"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "ARROW_RIGHT"
 
     // $ANTLR start "PIPE"
     public final void mPIPE() throws RecognitionException {
         try {
             int _type = PIPE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1743:6: ( '|' )
             // RuleSet.g:1744:1: '|'
             {
             match('|'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "PIPE"
 
     // $ANTLR start "SEMI"
     public final void mSEMI() throws RecognitionException {
         try {
             int _type = SEMI;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1746:6: ( ';' )
             // RuleSet.g:1746:8: ';'
             {
             match(';'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "SEMI"
 
     // $ANTLR start "FUNCTION"
     public final void mFUNCTION() throws RecognitionException {
         try {
             int _type = FUNCTION;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1747:10: ( 'function' )
             // RuleSet.g:1747:12: 'function'
             {
             match("function"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "FUNCTION"
 
     // $ANTLR start "EQUAL"
     public final void mEQUAL() throws RecognitionException {
         try {
             int _type = EQUAL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1748:7: ( '=' )
             // RuleSet.g:1748:8: '='
             {
             match('='); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "EQUAL"
 
     // $ANTLR start "OR"
     public final void mOR() throws RecognitionException {
         try {
             int _type = OR;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1751:2: ( '||' )
             // RuleSet.g:1751:5: '||'
             {
             match("||"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "OR"
 
     // $ANTLR start "AND"
     public final void mAND() throws RecognitionException {
         try {
             int _type = AND;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1753:2: ( '&&' )
             // RuleSet.g:1753:5: '&&'
             {
             match("&&"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "AND"
 
     // $ANTLR start "VAR_DOMAIN"
     public final void mVAR_DOMAIN() throws RecognitionException {
         try {
             int _type = VAR_DOMAIN;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1755:11: ( 'ent' | 'app' )
             int alt6=2;
             int LA6_0 = input.LA(1);
 
             if ( (LA6_0=='e') ) {
                 alt6=1;
             }
             else if ( (LA6_0=='a') ) {
                 alt6=2;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 6, 0, input);
 
                 throw nvae;
             }
             switch (alt6) {
                 case 1 :
                     // RuleSet.g:1755:13: 'ent'
                     {
                     match("ent"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1755:21: 'app'
                     {
                     match("app"); if (state.failed) return ;
 
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "VAR_DOMAIN"
 
     // $ANTLR start "WITH"
     public final void mWITH() throws RecognitionException {
         try {
             int _type = WITH;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1758:6: ( 'with' )
             // RuleSet.g:1758:8: 'with'
             {
             match("with"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "WITH"
 
     // $ANTLR start "USING"
     public final void mUSING() throws RecognitionException {
         try {
             int _type = USING;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1759:7: ( 'using' )
             // RuleSet.g:1759:9: 'using'
             {
             match("using"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "USING"
 
     // $ANTLR start "SETTING"
     public final void mSETTING() throws RecognitionException {
         try {
             int _type = SETTING;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1762:2: ( 'setting' )
             // RuleSet.g:1762:5: 'setting'
             {
             match("setting"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "SETTING"
 
     // $ANTLR start "PRE"
     public final void mPRE() throws RecognitionException {
         try {
             int _type = PRE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1763:5: ( 'pre' )
             // RuleSet.g:1763:7: 'pre'
             {
             match("pre"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "PRE"
 
     // $ANTLR start "FOREACH"
     public final void mFOREACH() throws RecognitionException {
         try {
             int _type = FOREACH;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1765:8: ( 'foreach' )
             // RuleSet.g:1765:10: 'foreach'
             {
             match("foreach"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "FOREACH"
 
     // $ANTLR start "WHEN"
     public final void mWHEN() throws RecognitionException {
         try {
             int _type = WHEN;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1767:6: ( 'when' )
             // RuleSet.g:1767:7: 'when'
             {
             match("when"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "WHEN"
 
     // $ANTLR start "OR_OR"
     public final void mOR_OR() throws RecognitionException {
         try {
             int _type = OR_OR;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1768:7: ( 'or' )
             // RuleSet.g:1768:9: 'or'
             {
             match("or"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "OR_OR"
 
     // $ANTLR start "AND_AND"
     public final void mAND_AND() throws RecognitionException {
         try {
             int _type = AND_AND;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1770:9: ( 'and' )
             // RuleSet.g:1770:11: 'and'
             {
             match("and"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "AND_AND"
 
     // $ANTLR start "BETWEEN"
     public final void mBETWEEN() throws RecognitionException {
         try {
             int _type = BETWEEN;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1772:9: ( 'between' )
             // RuleSet.g:1772:11: 'between'
             {
             match("between"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "BETWEEN"
 
     // $ANTLR start "WEB"
     public final void mWEB() throws RecognitionException {
         try {
             int _type = WEB;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1774:4: ( 'web' )
             // RuleSet.g:1774:6: 'web'
             {
             match("web"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "WEB"
 
     // $ANTLR start "PAGEVIEW"
     public final void mPAGEVIEW() throws RecognitionException {
         try {
             int _type = PAGEVIEW;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1775:10: ( 'pageview' )
             // RuleSet.g:1775:11: 'pageview'
             {
             match("pageview"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "PAGEVIEW"
 
     // $ANTLR start "LEFT_SMALL_ARROW"
     public final void mLEFT_SMALL_ARROW() throws RecognitionException {
         try {
             int _type = LEFT_SMALL_ARROW;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1779:2: ( '<-' )
             // RuleSet.g:1779:5: '<-'
             {
             match("<-"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "LEFT_SMALL_ARROW"
 
     // $ANTLR start "RIGHT_SMALL_ARROW"
     public final void mRIGHT_SMALL_ARROW() throws RecognitionException {
         try {
             int _type = RIGHT_SMALL_ARROW;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1782:2: ( '->' )
             // RuleSet.g:1782:5: '->'
             {
             match("->"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "RIGHT_SMALL_ARROW"
 
     // $ANTLR start "GLOBAL"
     public final void mGLOBAL() throws RecognitionException {
         try {
             int _type = GLOBAL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1783:7: ( 'global' )
             // RuleSet.g:1783:9: 'global'
             {
             match("global"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "GLOBAL"
 
     // $ANTLR start "DTYPE"
     public final void mDTYPE() throws RecognitionException {
         try {
             int _type = DTYPE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1785:2: ( ( 'JSON' | 'XML' | 'RSS' | 'HTML' ) )
             // RuleSet.g:1785:3: ( 'JSON' | 'XML' | 'RSS' | 'HTML' )
             {
             // RuleSet.g:1785:3: ( 'JSON' | 'XML' | 'RSS' | 'HTML' )
             int alt7=4;
             switch ( input.LA(1) ) {
             case 'J':
                 {
                 alt7=1;
                 }
                 break;
             case 'X':
                 {
                 alt7=2;
                 }
                 break;
             case 'R':
                 {
                 alt7=3;
                 }
                 break;
             case 'H':
                 {
                 alt7=4;
                 }
                 break;
             default:
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 7, 0, input);
 
                 throw nvae;
             }
 
             switch (alt7) {
                 case 1 :
                     // RuleSet.g:1785:4: 'JSON'
                     {
                     match("JSON"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1785:11: 'XML'
                     {
                     match("XML"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 3 :
                     // RuleSet.g:1785:17: 'RSS'
                     {
                     match("RSS"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 4 :
                     // RuleSet.g:1785:23: 'HTML'
                     {
                     match("HTML"); if (state.failed) return ;
 
 
                     }
                     break;
 
             }
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "DTYPE"
 
     // $ANTLR start "LIKE"
     public final void mLIKE() throws RecognitionException {
         try {
             int _type = LIKE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1787:6: ( 'like' )
             // RuleSet.g:1787:8: 'like'
             {
             match("like"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "LIKE"
 
     // $ANTLR start "PREDOP"
     public final void mPREDOP() throws RecognitionException {
         try {
             int _type = PREDOP;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1788:7: ( '<=' | '>=' | '<' | '>' | '==' | '!=' | 'eq' | 'neq' )
             int alt8=8;
             alt8 = dfa8.predict(input);
             switch (alt8) {
                 case 1 :
                     // RuleSet.g:1788:9: '<='
                     {
                     match("<="); if (state.failed) return ;
 
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1788:16: '>='
                     {
                     match(">="); if (state.failed) return ;
 
 
                     }
                     break;
                 case 3 :
                     // RuleSet.g:1788:23: '<'
                     {
                     match('<'); if (state.failed) return ;
 
                     }
                     break;
                 case 4 :
                     // RuleSet.g:1788:29: '>'
                     {
                     match('>'); if (state.failed) return ;
 
                     }
                     break;
                 case 5 :
                     // RuleSet.g:1788:35: '=='
                     {
                     match("=="); if (state.failed) return ;
 
 
                     }
                     break;
                 case 6 :
                     // RuleSet.g:1788:42: '!='
                     {
                     match("!="); if (state.failed) return ;
 
 
                     }
                     break;
                 case 7 :
                     // RuleSet.g:1788:49: 'eq'
                     {
                     match("eq"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 8 :
                     // RuleSet.g:1788:56: 'neq'
                     {
                     match("neq"); if (state.failed) return ;
 
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "PREDOP"
 
     // $ANTLR start "ADD_OP"
     public final void mADD_OP() throws RecognitionException {
         try {
             int _type = ADD_OP;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1790:7: ( '+' | '-' )
             // RuleSet.g:
             {
             if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                 input.consume();
             state.failed=false;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;}
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "ADD_OP"
 
     // $ANTLR start "CALLBACKS"
     public final void mCALLBACKS() throws RecognitionException {
         try {
             int _type = CALLBACKS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1792:11: ( 'callbacks' )
             // RuleSet.g:1792:13: 'callbacks'
             {
             match("callbacks"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "CALLBACKS"
 
     // $ANTLR start "SUCCESS"
     public final void mSUCCESS() throws RecognitionException {
         try {
             int _type = SUCCESS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1793:9: ( 'success' )
             // RuleSet.g:1793:11: 'success'
             {
             match("success"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "SUCCESS"
 
     // $ANTLR start "FAILURE"
     public final void mFAILURE() throws RecognitionException {
         try {
             int _type = FAILURE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1795:2: ( 'failure' )
             // RuleSet.g:1795:5: 'failure'
             {
             match("failure"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "FAILURE"
 
     // $ANTLR start "FORGET"
     public final void mFORGET() throws RecognitionException {
         try {
             int _type = FORGET;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1797:7: ( 'forget' )
             // RuleSet.g:1797:9: 'forget'
             {
             match("forget"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "FORGET"
 
     // $ANTLR start "MARK"
     public final void mMARK() throws RecognitionException {
         try {
             int _type = MARK;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1798:5: ( 'mark' )
             // RuleSet.g:1798:6: 'mark'
             {
             match("mark"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "MARK"
 
     // $ANTLR start "COUNTER_OP"
     public final void mCOUNTER_OP() throws RecognitionException {
         try {
             int _type = COUNTER_OP;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1801:2: ( '+=' | '-=' )
             int alt9=2;
             int LA9_0 = input.LA(1);
 
             if ( (LA9_0=='+') ) {
                 alt9=1;
             }
             else if ( (LA9_0=='-') ) {
                 alt9=2;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 9, 0, input);
 
                 throw nvae;
             }
             switch (alt9) {
                 case 1 :
                     // RuleSet.g:1802:2: '+='
                     {
                     match("+="); if (state.failed) return ;
 
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1803:11: '-='
                     {
                     match("-="); if (state.failed) return ;
 
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "COUNTER_OP"
 
     // $ANTLR start "IF"
     public final void mIF() throws RecognitionException {
         try {
             int _type = IF;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1805:3: ( 'if' )
             // RuleSet.g:1805:4: 'if'
             {
             match("if"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "IF"
 
     // $ANTLR start "CHOOSE"
     public final void mCHOOSE() throws RecognitionException {
         try {
             int _type = CHOOSE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1806:9: ( 'choose' )
             // RuleSet.g:1806:11: 'choose'
             {
             match("choose"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "CHOOSE"
 
     // $ANTLR start "EVERY"
     public final void mEVERY() throws RecognitionException {
         try {
             int _type = EVERY;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1807:8: ( 'every' )
             // RuleSet.g:1807:10: 'every'
             {
             match("every"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "EVERY"
 
     // $ANTLR start "COMMENT"
     public final void mCOMMENT() throws RecognitionException {
         try {
             int _type = COMMENT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1812:5: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' | '/*' ( options {greedy=false; } : . )* '*/' )
             int alt13=2;
             int LA13_0 = input.LA(1);
 
             if ( (LA13_0=='/') ) {
                 int LA13_1 = input.LA(2);
 
                 if ( (LA13_1=='/') ) {
                     alt13=1;
                 }
                 else if ( (LA13_1=='*') ) {
                     alt13=2;
                 }
                 else {
                     if (state.backtracking>0) {state.failed=true; return ;}
                     NoViableAltException nvae =
                         new NoViableAltException("", 13, 1, input);
 
                     throw nvae;
                 }
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 13, 0, input);
 
                 throw nvae;
             }
             switch (alt13) {
                 case 1 :
                     // RuleSet.g:1812:9: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
                     {
                     match("//"); if (state.failed) return ;
 
                     // RuleSet.g:1812:14: (~ ( '\\n' | '\\r' ) )*
                     loop10:
                     do {
                         int alt10=2;
                         int LA10_0 = input.LA(1);
 
                         if ( ((LA10_0>='\u0000' && LA10_0<='\t')||(LA10_0>='\u000B' && LA10_0<='\f')||(LA10_0>='\u000E' && LA10_0<='\uFFFF')) ) {
                             alt10=1;
                         }
 
 
                         switch (alt10) {
                     	case 1 :
                     	    // RuleSet.g:1812:14: ~ ( '\\n' | '\\r' )
                     	    {
                     	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
                     	        input.consume();
                     	    state.failed=false;
                     	    }
                     	    else {
                     	        if (state.backtracking>0) {state.failed=true; return ;}
                     	        MismatchedSetException mse = new MismatchedSetException(null,input);
                     	        recover(mse);
                     	        throw mse;}
 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop10;
                         }
                     } while (true);
 
                     // RuleSet.g:1812:28: ( '\\r' )?
                     int alt11=2;
                     int LA11_0 = input.LA(1);
 
                     if ( (LA11_0=='\r') ) {
                         alt11=1;
                     }
                     switch (alt11) {
                         case 1 :
                             // RuleSet.g:1812:28: '\\r'
                             {
                             match('\r'); if (state.failed) return ;
 
                             }
                             break;
 
                     }
 
                     match('\n'); if (state.failed) return ;
                     if ( state.backtracking==0 ) {
                       _channel=HIDDEN;
                     }
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1813:9: '/*' ( options {greedy=false; } : . )* '*/'
                     {
                     match("/*"); if (state.failed) return ;
 
                     // RuleSet.g:1813:14: ( options {greedy=false; } : . )*
                     loop12:
                     do {
                         int alt12=2;
                         int LA12_0 = input.LA(1);
 
                         if ( (LA12_0=='*') ) {
                             int LA12_1 = input.LA(2);
 
                             if ( (LA12_1=='/') ) {
                                 alt12=2;
                             }
                             else if ( ((LA12_1>='\u0000' && LA12_1<='.')||(LA12_1>='0' && LA12_1<='\uFFFF')) ) {
                                 alt12=1;
                             }
 
 
                         }
                         else if ( ((LA12_0>='\u0000' && LA12_0<=')')||(LA12_0>='+' && LA12_0<='\uFFFF')) ) {
                             alt12=1;
                         }
 
 
                         switch (alt12) {
                     	case 1 :
                     	    // RuleSet.g:1813:42: .
                     	    {
                     	    matchAny(); if (state.failed) return ;
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop12;
                         }
                     } while (true);
 
                     match("*/"); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                       _channel=HIDDEN;
                     }
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "COMMENT"
 
     // $ANTLR start "WS"
     public final void mWS() throws RecognitionException {
         try {
             int _type = WS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1817:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
             // RuleSet.g:1817:9: ( ' ' | '\\t' | '\\r' | '\\n' )
             {
             if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                 input.consume();
             state.failed=false;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;}
 
             if ( state.backtracking==0 ) {
               _channel=HIDDEN;
             }
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "WS"
 
     // $ANTLR start "STRING"
     public final void mSTRING() throws RecognitionException {
         try {
             int _type = STRING;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1825:5: ( '\"' ( '\\\\\"' | ~ ( '\"' ) )* '\"' | '\\'' ( '\\\\\\'' | ~ ( '\\'' ) )* '\\'' )
             int alt16=2;
             int LA16_0 = input.LA(1);
 
             if ( (LA16_0=='\"') ) {
                 alt16=1;
             }
             else if ( (LA16_0=='\'') ) {
                 alt16=2;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 16, 0, input);
 
                 throw nvae;
             }
             switch (alt16) {
                 case 1 :
                     // RuleSet.g:1825:8: '\"' ( '\\\\\"' | ~ ( '\"' ) )* '\"'
                     {
                     match('\"'); if (state.failed) return ;
                     // RuleSet.g:1825:12: ( '\\\\\"' | ~ ( '\"' ) )*
                     loop14:
                     do {
                         int alt14=3;
                         int LA14_0 = input.LA(1);
 
                         if ( (LA14_0=='\\') ) {
                             int LA14_2 = input.LA(2);
 
                             if ( (LA14_2=='\"') ) {
                                 int LA14_4 = input.LA(3);
 
                                 if ( ((LA14_4>='\u0000' && LA14_4<='\uFFFF')) ) {
                                     alt14=1;
                                 }
 
                                 else {
                                     alt14=2;
                                 }
 
                             }
                             else if ( ((LA14_2>='\u0000' && LA14_2<='!')||(LA14_2>='#' && LA14_2<='\uFFFF')) ) {
                                 alt14=2;
                             }
 
 
                         }
                         else if ( ((LA14_0>='\u0000' && LA14_0<='!')||(LA14_0>='#' && LA14_0<='[')||(LA14_0>=']' && LA14_0<='\uFFFF')) ) {
                             alt14=2;
                         }
 
 
                         switch (alt14) {
                     	case 1 :
                     	    // RuleSet.g:1825:14: '\\\\\"'
                     	    {
                     	    match("\\\""); if (state.failed) return ;
 
 
                     	    }
                     	    break;
                     	case 2 :
                     	    // RuleSet.g:1825:22: ~ ( '\"' )
                     	    {
                     	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
                     	        input.consume();
                     	    state.failed=false;
                     	    }
                     	    else {
                     	        if (state.backtracking>0) {state.failed=true; return ;}
                     	        MismatchedSetException mse = new MismatchedSetException(null,input);
                     	        recover(mse);
                     	        throw mse;}
 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop14;
                         }
                     } while (true);
 
                     match('\"'); if (state.failed) return ;
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1825:39: '\\'' ( '\\\\\\'' | ~ ( '\\'' ) )* '\\''
                     {
                     match('\''); if (state.failed) return ;
                     // RuleSet.g:1825:44: ( '\\\\\\'' | ~ ( '\\'' ) )*
                     loop15:
                     do {
                         int alt15=3;
                         int LA15_0 = input.LA(1);
 
                         if ( (LA15_0=='\\') ) {
                             int LA15_2 = input.LA(2);
 
                             if ( (LA15_2=='\'') ) {
                                 int LA15_4 = input.LA(3);
 
                                 if ( ((LA15_4>='\u0000' && LA15_4<='\uFFFF')) ) {
                                     alt15=1;
                                 }
 
                                 else {
                                     alt15=2;
                                 }
 
                             }
                             else if ( ((LA15_2>='\u0000' && LA15_2<='&')||(LA15_2>='(' && LA15_2<='\uFFFF')) ) {
                                 alt15=2;
                             }
 
 
                         }
                         else if ( ((LA15_0>='\u0000' && LA15_0<='&')||(LA15_0>='(' && LA15_0<='[')||(LA15_0>=']' && LA15_0<='\uFFFF')) ) {
                             alt15=2;
                         }
 
 
                         switch (alt15) {
                     	case 1 :
                     	    // RuleSet.g:1825:46: '\\\\\\''
                     	    {
                     	    match("\\'"); if (state.failed) return ;
 
 
                     	    }
                     	    break;
                     	case 2 :
                     	    // RuleSet.g:1825:55: ~ ( '\\'' )
                     	    {
                     	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
                     	        input.consume();
                     	    state.failed=false;
                     	    }
                     	    else {
                     	        if (state.backtracking>0) {state.failed=true; return ;}
                     	        MismatchedSetException mse = new MismatchedSetException(null,input);
                     	        recover(mse);
                     	        throw mse;}
 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop15;
                         }
                     } while (true);
 
                     match('\''); if (state.failed) return ;
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "STRING"
 
     // $ANTLR start "POUND"
     public final void mPOUND() throws RecognitionException {
         try {
             // RuleSet.g:1829:2: ( '#' )
             // RuleSet.g:1829:5: '#'
             {
             match('#'); if (state.failed) return ;
 
             }
 
         }
         finally {
         }
     }
     // $ANTLR end "POUND"
 
     // $ANTLR start "HTML"
     public final void mHTML() throws RecognitionException {
         try {
             int _type = HTML;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1834:2: ( '<<' ( options {greedy=false; } : . )* '>>' )
             // RuleSet.g:1834:4: '<<' ( options {greedy=false; } : . )* '>>'
             {
             match("<<"); if (state.failed) return ;
 
             // RuleSet.g:1834:9: ( options {greedy=false; } : . )*
             loop17:
             do {
                 int alt17=2;
                 int LA17_0 = input.LA(1);
 
                 if ( (LA17_0=='>') ) {
                     int LA17_1 = input.LA(2);
 
                     if ( (LA17_1=='>') ) {
                         alt17=2;
                     }
                     else if ( ((LA17_1>='\u0000' && LA17_1<='=')||(LA17_1>='?' && LA17_1<='\uFFFF')) ) {
                         alt17=1;
                     }
 
 
                 }
                 else if ( ((LA17_0>='\u0000' && LA17_0<='=')||(LA17_0>='?' && LA17_0<='\uFFFF')) ) {
                     alt17=1;
                 }
 
 
                 switch (alt17) {
             	case 1 :
             	    // RuleSet.g:1834:37: .
             	    {
             	    matchAny(); if (state.failed) return ;
 
             	    }
             	    break;
 
             	default :
             	    break loop17;
                 }
             } while (true);
 
             match(">>"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "HTML"
 
     // $ANTLR start "JS"
     public final void mJS() throws RecognitionException {
         try {
             int _type = JS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1838:2: ( '<|' ( options {greedy=false; } : . )* '|>' )
             // RuleSet.g:1838:4: '<|' ( options {greedy=false; } : . )* '|>'
             {
             match("<|"); if (state.failed) return ;
 
             // RuleSet.g:1838:9: ( options {greedy=false; } : . )*
             loop18:
             do {
                 int alt18=2;
                 int LA18_0 = input.LA(1);
 
                 if ( (LA18_0=='|') ) {
                     int LA18_1 = input.LA(2);
 
                     if ( (LA18_1=='>') ) {
                         alt18=2;
                     }
                     else if ( ((LA18_1>='\u0000' && LA18_1<='=')||(LA18_1>='?' && LA18_1<='\uFFFF')) ) {
                         alt18=1;
                     }
 
 
                 }
                 else if ( ((LA18_0>='\u0000' && LA18_0<='{')||(LA18_0>='}' && LA18_0<='\uFFFF')) ) {
                     alt18=1;
                 }
 
 
                 switch (alt18) {
             	case 1 :
             	    // RuleSet.g:1838:37: .
             	    {
             	    matchAny(); if (state.failed) return ;
 
             	    }
             	    break;
 
             	default :
             	    break loop18;
                 }
             } while (true);
 
             match("|>"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "JS"
 
     // $ANTLR start "EXPONENT"
     public final void mEXPONENT() throws RecognitionException {
         try {
             // RuleSet.g:1842:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
             // RuleSet.g:1842:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
             {
             if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                 input.consume();
             state.failed=false;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;}
 
             // RuleSet.g:1842:22: ( '+' | '-' )?
             int alt19=2;
             int LA19_0 = input.LA(1);
 
             if ( (LA19_0=='+'||LA19_0=='-') ) {
                 alt19=1;
             }
             switch (alt19) {
                 case 1 :
                     // RuleSet.g:
                     {
                     if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                         input.consume();
                     state.failed=false;
                     }
                     else {
                         if (state.backtracking>0) {state.failed=true; return ;}
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;}
 
 
                     }
                     break;
 
             }
 
             // RuleSet.g:1842:33: ( '0' .. '9' )+
             int cnt20=0;
             loop20:
             do {
                 int alt20=2;
                 int LA20_0 = input.LA(1);
 
                 if ( ((LA20_0>='0' && LA20_0<='9')) ) {
                     alt20=1;
                 }
 
 
                 switch (alt20) {
             	case 1 :
             	    // RuleSet.g:1842:34: '0' .. '9'
             	    {
             	    matchRange('0','9'); if (state.failed) return ;
 
             	    }
             	    break;
 
             	default :
             	    if ( cnt20 >= 1 ) break loop20;
             	    if (state.backtracking>0) {state.failed=true; return ;}
                         EarlyExitException eee =
                             new EarlyExitException(20, input);
                         throw eee;
                 }
                 cnt20++;
             } while (true);
 
 
             }
 
         }
         finally {
         }
     }
     // $ANTLR end "EXPONENT"
 
     // $ANTLR start "HEX_DIGIT"
     public final void mHEX_DIGIT() throws RecognitionException {
         try {
             // RuleSet.g:1845:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
             // RuleSet.g:1845:13: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
             {
             if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                 input.consume();
             state.failed=false;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;}
 
 
             }
 
         }
         finally {
         }
     }
     // $ANTLR end "HEX_DIGIT"
 
     // $ANTLR start "ESC_SEQ"
     public final void mESC_SEQ() throws RecognitionException {
         try {
             int _type = ESC_SEQ;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1850:5: ( '\\\\' ( 'b' | 'd' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' | '.' | 'w' | 's' | '?' | '(' | ')' | '-' ) | UNICODE_ESC | OCTAL_ESC )
             int alt21=3;
             int LA21_0 = input.LA(1);
 
             if ( (LA21_0=='\\') ) {
                 switch ( input.LA(2) ) {
                 case '\"':
                 case '\'':
                 case '(':
                 case ')':
                 case '-':
                 case '.':
                 case '?':
                 case '\\':
                 case 'b':
                 case 'd':
                 case 'f':
                 case 'n':
                 case 'r':
                 case 's':
                 case 't':
                 case 'w':
                     {
                     alt21=1;
                     }
                     break;
                 case 'u':
                     {
                     alt21=2;
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
                     alt21=3;
                     }
                     break;
                 default:
                     if (state.backtracking>0) {state.failed=true; return ;}
                     NoViableAltException nvae =
                         new NoViableAltException("", 21, 1, input);
 
                     throw nvae;
                 }
 
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 21, 0, input);
 
                 throw nvae;
             }
             switch (alt21) {
                 case 1 :
                     // RuleSet.g:1850:9: '\\\\' ( 'b' | 'd' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' | '.' | 'w' | 's' | '?' | '(' | ')' | '-' )
                     {
                     match('\\'); if (state.failed) return ;
                     if ( input.LA(1)=='\"'||(input.LA(1)>='\'' && input.LA(1)<=')')||(input.LA(1)>='-' && input.LA(1)<='.')||input.LA(1)=='?'||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='d'||input.LA(1)=='f'||input.LA(1)=='n'||(input.LA(1)>='r' && input.LA(1)<='t')||input.LA(1)=='w' ) {
                         input.consume();
                     state.failed=false;
                     }
                     else {
                         if (state.backtracking>0) {state.failed=true; return ;}
                         MismatchedSetException mse = new MismatchedSetException(null,input);
                         recover(mse);
                         throw mse;}
 
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1851:9: UNICODE_ESC
                     {
                     mUNICODE_ESC(); if (state.failed) return ;
 
                     }
                     break;
                 case 3 :
                     // RuleSet.g:1852:9: OCTAL_ESC
                     {
                     mOCTAL_ESC(); if (state.failed) return ;
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "ESC_SEQ"
 
     // $ANTLR start "OCTAL_ESC"
     public final void mOCTAL_ESC() throws RecognitionException {
         try {
             // RuleSet.g:1865:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
             int alt22=3;
             int LA22_0 = input.LA(1);
 
             if ( (LA22_0=='\\') ) {
                 int LA22_1 = input.LA(2);
 
                 if ( ((LA22_1>='0' && LA22_1<='3')) ) {
                     int LA22_2 = input.LA(3);
 
                     if ( ((LA22_2>='0' && LA22_2<='7')) ) {
                         int LA22_5 = input.LA(4);
 
                         if ( ((LA22_5>='0' && LA22_5<='7')) ) {
                             alt22=1;
                         }
                         else {
                             alt22=2;}
                     }
                     else {
                         alt22=3;}
                 }
                 else if ( ((LA22_1>='4' && LA22_1<='7')) ) {
                     int LA22_3 = input.LA(3);
 
                     if ( ((LA22_3>='0' && LA22_3<='7')) ) {
                         alt22=2;
                     }
                     else {
                         alt22=3;}
                 }
                 else {
                     if (state.backtracking>0) {state.failed=true; return ;}
                     NoViableAltException nvae =
                         new NoViableAltException("", 22, 1, input);
 
                     throw nvae;
                 }
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 22, 0, input);
 
                 throw nvae;
             }
             switch (alt22) {
                 case 1 :
                     // RuleSet.g:1865:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                     {
                     match('\\'); if (state.failed) return ;
                     // RuleSet.g:1865:14: ( '0' .. '3' )
                     // RuleSet.g:1865:15: '0' .. '3'
                     {
                     matchRange('0','3'); if (state.failed) return ;
 
                     }
 
                     // RuleSet.g:1865:25: ( '0' .. '7' )
                     // RuleSet.g:1865:26: '0' .. '7'
                     {
                     matchRange('0','7'); if (state.failed) return ;
 
                     }
 
                     // RuleSet.g:1865:36: ( '0' .. '7' )
                     // RuleSet.g:1865:37: '0' .. '7'
                     {
                     matchRange('0','7'); if (state.failed) return ;
 
                     }
 
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1866:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                     {
                     match('\\'); if (state.failed) return ;
                     // RuleSet.g:1866:14: ( '0' .. '7' )
                     // RuleSet.g:1866:15: '0' .. '7'
                     {
                     matchRange('0','7'); if (state.failed) return ;
 
                     }
 
                     // RuleSet.g:1866:25: ( '0' .. '7' )
                     // RuleSet.g:1866:26: '0' .. '7'
                     {
                     matchRange('0','7'); if (state.failed) return ;
 
                     }
 
 
                     }
                     break;
                 case 3 :
                     // RuleSet.g:1867:9: '\\\\' ( '0' .. '7' )
                     {
                     match('\\'); if (state.failed) return ;
                     // RuleSet.g:1867:14: ( '0' .. '7' )
                     // RuleSet.g:1867:15: '0' .. '7'
                     {
                     matchRange('0','7'); if (state.failed) return ;
 
                     }
 
 
                     }
                     break;
 
             }
         }
         finally {
         }
     }
     // $ANTLR end "OCTAL_ESC"
 
     // $ANTLR start "UNICODE_ESC"
     public final void mUNICODE_ESC() throws RecognitionException {
         try {
             // RuleSet.g:1872:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
             // RuleSet.g:1872:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
             {
             match('\\'); if (state.failed) return ;
             match('u'); if (state.failed) return ;
             mHEX_DIGIT(); if (state.failed) return ;
             mHEX_DIGIT(); if (state.failed) return ;
             mHEX_DIGIT(); if (state.failed) return ;
             mHEX_DIGIT(); if (state.failed) return ;
 
             }
 
         }
         finally {
         }
     }
     // $ANTLR end "UNICODE_ESC"
 
     // $ANTLR start "RULE_SET"
     public final void mRULE_SET() throws RecognitionException {
         try {
             int _type = RULE_SET;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1884:2: ( 'ruleset' )
             // RuleSet.g:1884:4: 'ruleset'
             {
             match("ruleset"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "RULE_SET"
 
     // $ANTLR start "MULT_OP"
     public final void mMULT_OP() throws RecognitionException {
         try {
             int _type = MULT_OP;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1887:8: ( '*' | '/' | '%' )
             // RuleSet.g:
             {
             if ( input.LA(1)=='%'||input.LA(1)=='*'||input.LA(1)=='/' ) {
                 input.consume();
             state.failed=false;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;}
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "MULT_OP"
 
     // $ANTLR start "DOT"
     public final void mDOT() throws RecognitionException {
         try {
             int _type = DOT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1892:2: ( '.' )
             // RuleSet.g:1892:5: '.'
             {
             match('.'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "DOT"
 
     // $ANTLR start "MATCH"
     public final void mMATCH() throws RecognitionException {
         try {
             int _type = MATCH;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1895:2: ( 'match' )
             // RuleSet.g:1895:4: 'match'
             {
             match("match"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "MATCH"
 
     // $ANTLR start "REPLACE"
     public final void mREPLACE() throws RecognitionException {
         try {
             int _type = REPLACE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1898:2: ( 'replace' )
             // RuleSet.g:1898:4: 'replace'
             {
             match("replace"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "REPLACE"
 
     // $ANTLR start "OTHER_OPERATORS"
     public final void mOTHER_OPERATORS() throws RecognitionException {
         try {
             int _type = OTHER_OPERATORS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1901:2: ( 'pick' | 'length' | 'as' | 'head' | 'tail' | 'sort' | 'filter' | 'map' | 'uc' | 'lc' | 'split' | 'join' | 'query' | 'has' | 'union' | 'difference' | 'intersection' | 'unique' | 'once' | 'duplicates' )
             int alt23=20;
             alt23 = dfa23.predict(input);
             switch (alt23) {
                 case 1 :
                     // RuleSet.g:1901:5: 'pick'
                     {
                     match("pick"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:1901:12: 'length'
                     {
                     match("length"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 3 :
                     // RuleSet.g:1901:21: 'as'
                     {
                     match("as"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 4 :
                     // RuleSet.g:1901:26: 'head'
                     {
                     match("head"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 5 :
                     // RuleSet.g:1901:33: 'tail'
                     {
                     match("tail"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 6 :
                     // RuleSet.g:1901:40: 'sort'
                     {
                     match("sort"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 7 :
                     // RuleSet.g:1902:9: 'filter'
                     {
                     match("filter"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 8 :
                     // RuleSet.g:1902:18: 'map'
                     {
                     match("map"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 9 :
                     // RuleSet.g:1902:24: 'uc'
                     {
                     match("uc"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 10 :
                     // RuleSet.g:1902:29: 'lc'
                     {
                     match("lc"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 11 :
                     // RuleSet.g:1902:35: 'split'
                     {
                     match("split"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 12 :
                     // RuleSet.g:1902:45: 'join'
                     {
                     match("join"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 13 :
                     // RuleSet.g:1902:54: 'query'
                     {
                     match("query"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 14 :
                     // RuleSet.g:1903:10: 'has'
                     {
                     match("has"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 15 :
                     // RuleSet.g:1903:18: 'union'
                     {
                     match("union"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 16 :
                     // RuleSet.g:1903:28: 'difference'
                     {
                     match("difference"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 17 :
                     // RuleSet.g:1903:43: 'intersection'
                     {
                     match("intersection"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 18 :
                     // RuleSet.g:1903:60: 'unique'
                     {
                     match("unique"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 19 :
                     // RuleSet.g:1903:71: 'once'
                     {
                     match("once"); if (state.failed) return ;
 
 
                     }
                     break;
                 case 20 :
                     // RuleSet.g:1904:10: 'duplicates'
                     {
                     match("duplicates"); if (state.failed) return ;
 
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "OTHER_OPERATORS"
 
     // $ANTLR start "TRUE"
     public final void mTRUE() throws RecognitionException {
         try {
             int _type = TRUE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1907:6: ( 'true' )
             // RuleSet.g:1907:7: 'true'
             {
             match("true"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "TRUE"
 
     // $ANTLR start "FALSE"
     public final void mFALSE() throws RecognitionException {
         try {
             int _type = FALSE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1908:7: ( 'false' )
             // RuleSet.g:1908:8: 'false'
             {
             match("false"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "FALSE"
 
     // $ANTLR start "CURRENT"
     public final void mCURRENT() throws RecognitionException {
         try {
             int _type = CURRENT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1909:8: ( 'current' )
             // RuleSet.g:1909:10: 'current'
             {
             match("current"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "CURRENT"
 
     // $ANTLR start "KEY"
     public final void mKEY() throws RecognitionException {
         try {
             int _type = KEY;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1912:2: ( 'key' )
             // RuleSet.g:1912:4: 'key'
             {
             match("key"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "KEY"
 
     // $ANTLR start "AUTHZ"
     public final void mAUTHZ() throws RecognitionException {
         try {
             int _type = AUTHZ;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1915:2: ( 'authz' )
             // RuleSet.g:1915:4: 'authz'
             {
             match("authz"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "AUTHZ"
 
     // $ANTLR start "REQUIRE"
     public final void mREQUIRE() throws RecognitionException {
         try {
             int _type = REQUIRE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1919:2: ( 'require' )
             // RuleSet.g:1920:3: 'require'
             {
             match("require"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "REQUIRE"
 
     // $ANTLR start "LOGGING"
     public final void mLOGGING() throws RecognitionException {
         try {
             int _type = LOGGING;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1923:2: ( 'logging' )
             // RuleSet.g:1923:3: 'logging'
             {
             match("logging"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "LOGGING"
 
     // $ANTLR start "USE"
     public final void mUSE() throws RecognitionException {
         try {
             int _type = USE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1926:2: ( 'use' )
             // RuleSet.g:1926:4: 'use'
             {
             match("use"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "USE"
 
     // $ANTLR start "CSS"
     public final void mCSS() throws RecognitionException {
         try {
             int _type = CSS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1929:2: ( 'css' )
             // RuleSet.g:1929:3: 'css'
             {
             match("css"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "CSS"
 
     // $ANTLR start "JAVASCRIPT"
     public final void mJAVASCRIPT() throws RecognitionException {
         try {
             int _type = JAVASCRIPT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1932:2: ( 'javascript' )
             // RuleSet.g:1932:3: 'javascript'
             {
             match("javascript"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "JAVASCRIPT"
 
     // $ANTLR start "META"
     public final void mMETA() throws RecognitionException {
         try {
             int _type = META;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1935:2: ( 'meta' )
             // RuleSet.g:1935:4: 'meta'
             {
             match("meta"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "META"
 
     // $ANTLR start "ON"
     public final void mON() throws RecognitionException {
         try {
             int _type = ON;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1939:2: ( 'on' )
             // RuleSet.g:1939:5: 'on'
             {
             match("on"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "ON"
 
     // $ANTLR start "OFF"
     public final void mOFF() throws RecognitionException {
         try {
             int _type = OFF;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1941:6: ( 'off' )
             // RuleSet.g:1941:8: 'off'
             {
             match("off"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "OFF"
 
     // $ANTLR start "MODULE"
     public final void mMODULE() throws RecognitionException {
         try {
             int _type = MODULE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1945:2: ( 'module' )
             // RuleSet.g:1945:4: 'module'
             {
             match("module"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "MODULE"
 
     // $ANTLR start "ALIAS"
     public final void mALIAS() throws RecognitionException {
         try {
             int _type = ALIAS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1948:2: ( 'alias' )
             // RuleSet.g:1948:3: 'alias'
             {
             match("alias"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "ALIAS"
 
     // $ANTLR start "EMIT"
     public final void mEMIT() throws RecognitionException {
         try {
             int _type = EMIT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1953:2: ( 'emit' )
             // RuleSet.g:1953:4: 'emit'
             {
             match("emit"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "EMIT"
 
     // $ANTLR start "CACHABLE"
     public final void mCACHABLE() throws RecognitionException {
         try {
             int _type = CACHABLE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1957:2: ( 'cachable' )
             // RuleSet.g:1957:3: 'cachable'
             {
             match("cachable"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "CACHABLE"
 
     // $ANTLR start "FOR"
     public final void mFOR() throws RecognitionException {
         try {
             int _type = FOR;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1961:2: ( 'for' )
             // RuleSet.g:1961:4: 'for'
             {
             match("for"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "FOR"
 
     // $ANTLR start "WITHIN"
     public final void mWITHIN() throws RecognitionException {
         try {
             int _type = WITHIN;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1965:2: ( 'within' )
             // RuleSet.g:1965:4: 'within'
             {
             match("within"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "WITHIN"
 
     // $ANTLR start "COLON"
     public final void mCOLON() throws RecognitionException {
         try {
             int _type = COLON;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1969:2: ( ':' )
             // RuleSet.g:1969:5: ':'
             {
             match(':'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "COLON"
 
     // $ANTLR start "HISTORY"
     public final void mHISTORY() throws RecognitionException {
         try {
             int _type = HISTORY;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1972:2: ( 'history' )
             // RuleSet.g:1973:2: 'history'
             {
             match("history"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "HISTORY"
 
     // $ANTLR start "RIGHT_PAREN"
     public final void mRIGHT_PAREN() throws RecognitionException {
         try {
             int _type = RIGHT_PAREN;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1976:2: ( ')' )
             // RuleSet.g:1976:4: ')'
             {
             match(')'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "RIGHT_PAREN"
 
     // $ANTLR start "LEFT_PAREN"
     public final void mLEFT_PAREN() throws RecognitionException {
         try {
             int _type = LEFT_PAREN;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1979:2: ( '(' )
             // RuleSet.g:1979:4: '('
             {
             match('('); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "LEFT_PAREN"
 
     // $ANTLR start "RIGHT_BRACKET"
     public final void mRIGHT_BRACKET() throws RecognitionException {
         try {
             int _type = RIGHT_BRACKET;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1983:2: ( ']' )
             // RuleSet.g:1983:5: ']'
             {
             match(']'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "RIGHT_BRACKET"
 
     // $ANTLR start "LEFT_BRACKET"
     public final void mLEFT_BRACKET() throws RecognitionException {
         try {
             int _type = LEFT_BRACKET;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1986:2: ( '[' )
             // RuleSet.g:1986:4: '['
             {
             match('['); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "LEFT_BRACKET"
 
     // $ANTLR start "COMMA"
     public final void mCOMMA() throws RecognitionException {
         try {
             int _type = COMMA;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1990:2: ( ',' )
             // RuleSet.g:1990:4: ','
             {
             match(','); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "COMMA"
 
     // $ANTLR start "LEFT_CURL"
     public final void mLEFT_CURL() throws RecognitionException {
         try {
             int _type = LEFT_CURL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1993:2: ( '{' )
             // RuleSet.g:1993:5: '{'
             {
             match('{'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "LEFT_CURL"
 
     // $ANTLR start "RIGHT_CURL"
     public final void mRIGHT_CURL() throws RecognitionException {
         try {
             int _type = RIGHT_CURL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1995:2: ( '}' )
             // RuleSet.g:1995:4: '}'
             {
             match('}'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "RIGHT_CURL"
 
     // $ANTLR start "NOT"
     public final void mNOT() throws RecognitionException {
         try {
             int _type = NOT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1997:5: ( 'not' )
             // RuleSet.g:1997:7: 'not'
             {
             match("not"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "NOT"
 
     // $ANTLR start "SEEN"
     public final void mSEEN() throws RecognitionException {
         try {
             int _type = SEEN;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:1998:6: ( 'seen' )
             // RuleSet.g:1998:7: 'seen'
             {
             match("seen"); if (state.failed) return ;
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "SEEN"
 
     // $ANTLR start "VAR"
     public final void mVAR() throws RecognitionException {
         try {
             int _type = VAR;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:2000:6: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
             // RuleSet.g:2000:8: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
             {
             if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                 input.consume();
             state.failed=false;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;}
 
             // RuleSet.g:2000:32: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
             loop24:
             do {
                 int alt24=2;
                 int LA24_0 = input.LA(1);
 
                 if ( ((LA24_0>='0' && LA24_0<='9')||(LA24_0>='A' && LA24_0<='Z')||LA24_0=='_'||(LA24_0>='a' && LA24_0<='z')) ) {
                     alt24=1;
                 }
 
 
                 switch (alt24) {
             	case 1 :
             	    // RuleSet.g:
             	    {
             	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
             	        input.consume();
             	    state.failed=false;
             	    }
             	    else {
             	        if (state.backtracking>0) {state.failed=true; return ;}
             	        MismatchedSetException mse = new MismatchedSetException(null,input);
             	        recover(mse);
             	        throw mse;}
 
 
             	    }
             	    break;
 
             	default :
             	    break loop24;
                 }
             } while (true);
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "VAR"
 
     // $ANTLR start "INT"
     public final void mINT() throws RecognitionException {
         try {
             int _type = INT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:2004:5: ( ( '-' )? ( '0' .. '9' )+ )
             // RuleSet.g:2004:7: ( '-' )? ( '0' .. '9' )+
             {
             // RuleSet.g:2004:7: ( '-' )?
             int alt25=2;
             int LA25_0 = input.LA(1);
 
             if ( (LA25_0=='-') ) {
                 alt25=1;
             }
             switch (alt25) {
                 case 1 :
                     // RuleSet.g:2004:7: '-'
                     {
                     match('-'); if (state.failed) return ;
 
                     }
                     break;
 
             }
 
             // RuleSet.g:2004:12: ( '0' .. '9' )+
             int cnt26=0;
             loop26:
             do {
                 int alt26=2;
                 int LA26_0 = input.LA(1);
 
                 if ( ((LA26_0>='0' && LA26_0<='9')) ) {
                     alt26=1;
                 }
 
 
                 switch (alt26) {
             	case 1 :
             	    // RuleSet.g:2004:12: '0' .. '9'
             	    {
             	    matchRange('0','9'); if (state.failed) return ;
 
             	    }
             	    break;
 
             	default :
             	    if ( cnt26 >= 1 ) break loop26;
             	    if (state.backtracking>0) {state.failed=true; return ;}
                         EarlyExitException eee =
                             new EarlyExitException(26, input);
                         throw eee;
                 }
                 cnt26++;
             } while (true);
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "INT"
 
     // $ANTLR start "FLOAT"
     public final void mFLOAT() throws RecognitionException {
         try {
             int _type = FLOAT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // RuleSet.g:2008:5: ( ( '-' )? ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )? | ( '-' )? '.' ( '0' .. '9' )* ( EXPONENT )? )
             int alt34=2;
             switch ( input.LA(1) ) {
             case '-':
                 {
                 int LA34_1 = input.LA(2);
 
                 if ( (LA34_1=='.') ) {
                     alt34=2;
                 }
                 else if ( ((LA34_1>='0' && LA34_1<='9')) ) {
                     alt34=1;
                 }
                 else {
                     if (state.backtracking>0) {state.failed=true; return ;}
                     NoViableAltException nvae =
                         new NoViableAltException("", 34, 1, input);
 
                     throw nvae;
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
                 alt34=1;
                 }
                 break;
             case '.':
                 {
                 alt34=2;
                 }
                 break;
             default:
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 34, 0, input);
 
                 throw nvae;
             }
 
             switch (alt34) {
                 case 1 :
                     // RuleSet.g:2008:9: ( '-' )? ( '0' .. '9' )+ '.' ( '0' .. '9' )* ( EXPONENT )?
                     {
                     // RuleSet.g:2008:9: ( '-' )?
                     int alt27=2;
                     int LA27_0 = input.LA(1);
 
                     if ( (LA27_0=='-') ) {
                         alt27=1;
                     }
                     switch (alt27) {
                         case 1 :
                             // RuleSet.g:2008:9: '-'
                             {
                             match('-'); if (state.failed) return ;
 
                             }
                             break;
 
                     }
 
                     // RuleSet.g:2008:14: ( '0' .. '9' )+
                     int cnt28=0;
                     loop28:
                     do {
                         int alt28=2;
                         int LA28_0 = input.LA(1);
 
                         if ( ((LA28_0>='0' && LA28_0<='9')) ) {
                             alt28=1;
                         }
 
 
                         switch (alt28) {
                     	case 1 :
                     	    // RuleSet.g:2008:15: '0' .. '9'
                     	    {
                     	    matchRange('0','9'); if (state.failed) return ;
 
                     	    }
                     	    break;
 
                     	default :
                     	    if ( cnt28 >= 1 ) break loop28;
                     	    if (state.backtracking>0) {state.failed=true; return ;}
                                 EarlyExitException eee =
                                     new EarlyExitException(28, input);
                                 throw eee;
                         }
                         cnt28++;
                     } while (true);
 
                     match('.'); if (state.failed) return ;
                     // RuleSet.g:2008:30: ( '0' .. '9' )*
                     loop29:
                     do {
                         int alt29=2;
                         int LA29_0 = input.LA(1);
 
                         if ( ((LA29_0>='0' && LA29_0<='9')) ) {
                             alt29=1;
                         }
 
 
                         switch (alt29) {
                     	case 1 :
                     	    // RuleSet.g:2008:31: '0' .. '9'
                     	    {
                     	    matchRange('0','9'); if (state.failed) return ;
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop29;
                         }
                     } while (true);
 
                     // RuleSet.g:2008:42: ( EXPONENT )?
                     int alt30=2;
                     int LA30_0 = input.LA(1);
 
                     if ( (LA30_0=='E'||LA30_0=='e') ) {
                         alt30=1;
                     }
                     switch (alt30) {
                         case 1 :
                             // RuleSet.g:2008:42: EXPONENT
                             {
                             mEXPONENT(); if (state.failed) return ;
 
                             }
                             break;
 
                     }
 
 
                     }
                     break;
                 case 2 :
                     // RuleSet.g:2009:9: ( '-' )? '.' ( '0' .. '9' )* ( EXPONENT )?
                     {
                     // RuleSet.g:2009:9: ( '-' )?
                     int alt31=2;
                     int LA31_0 = input.LA(1);
 
                     if ( (LA31_0=='-') ) {
                         alt31=1;
                     }
                     switch (alt31) {
                         case 1 :
                             // RuleSet.g:2009:9: '-'
                             {
                             match('-'); if (state.failed) return ;
 
                             }
                             break;
 
                     }
 
                     match('.'); if (state.failed) return ;
                     // RuleSet.g:2009:18: ( '0' .. '9' )*
                     loop32:
                     do {
                         int alt32=2;
                         int LA32_0 = input.LA(1);
 
                         if ( ((LA32_0>='0' && LA32_0<='9')) ) {
                             alt32=1;
                         }
 
 
                         switch (alt32) {
                     	case 1 :
                     	    // RuleSet.g:2009:19: '0' .. '9'
                     	    {
                     	    matchRange('0','9'); if (state.failed) return ;
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop32;
                         }
                     } while (true);
 
                     // RuleSet.g:2009:30: ( EXPONENT )?
                     int alt33=2;
                     int LA33_0 = input.LA(1);
 
                     if ( (LA33_0=='E'||LA33_0=='e') ) {
                         alt33=1;
                     }
                     switch (alt33) {
                         case 1 :
                             // RuleSet.g:2009:30: EXPONENT
                             {
                             mEXPONENT(); if (state.failed) return ;
 
                             }
                             break;
 
                     }
 
 
                     }
                     break;
 
             }
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         }
     }
     // $ANTLR end "FLOAT"
 
     public void mTokens() throws RecognitionException {
         // RuleSet.g:1:8: ( REX | ARROW_RIGHT | PIPE | SEMI | FUNCTION | EQUAL | OR | AND | VAR_DOMAIN | WITH | USING | SETTING | PRE | FOREACH | WHEN | OR_OR | AND_AND | BETWEEN | WEB | PAGEVIEW | LEFT_SMALL_ARROW | RIGHT_SMALL_ARROW | GLOBAL | DTYPE | LIKE | PREDOP | ADD_OP | CALLBACKS | SUCCESS | FAILURE | FORGET | MARK | COUNTER_OP | IF | CHOOSE | EVERY | COMMENT | WS | STRING | HTML | JS | ESC_SEQ | RULE_SET | MULT_OP | DOT | MATCH | REPLACE | OTHER_OPERATORS | TRUE | FALSE | CURRENT | KEY | AUTHZ | REQUIRE | LOGGING | USE | CSS | JAVASCRIPT | META | ON | OFF | MODULE | ALIAS | EMIT | CACHABLE | FOR | WITHIN | COLON | HISTORY | RIGHT_PAREN | LEFT_PAREN | RIGHT_BRACKET | LEFT_BRACKET | COMMA | LEFT_CURL | RIGHT_CURL | NOT | SEEN | VAR | INT | FLOAT )
         int alt35=81;
         alt35 = dfa35.predict(input);
         switch (alt35) {
             case 1 :
                 // RuleSet.g:1:10: REX
                 {
                 mREX(); if (state.failed) return ;
 
                 }
                 break;
             case 2 :
                 // RuleSet.g:1:14: ARROW_RIGHT
                 {
                 mARROW_RIGHT(); if (state.failed) return ;
 
                 }
                 break;
             case 3 :
                 // RuleSet.g:1:26: PIPE
                 {
                 mPIPE(); if (state.failed) return ;
 
                 }
                 break;
             case 4 :
                 // RuleSet.g:1:31: SEMI
                 {
                 mSEMI(); if (state.failed) return ;
 
                 }
                 break;
             case 5 :
                 // RuleSet.g:1:36: FUNCTION
                 {
                 mFUNCTION(); if (state.failed) return ;
 
                 }
                 break;
             case 6 :
                 // RuleSet.g:1:45: EQUAL
                 {
                 mEQUAL(); if (state.failed) return ;
 
                 }
                 break;
             case 7 :
                 // RuleSet.g:1:51: OR
                 {
                 mOR(); if (state.failed) return ;
 
                 }
                 break;
             case 8 :
                 // RuleSet.g:1:54: AND
                 {
                 mAND(); if (state.failed) return ;
 
                 }
                 break;
             case 9 :
                 // RuleSet.g:1:58: VAR_DOMAIN
                 {
                 mVAR_DOMAIN(); if (state.failed) return ;
 
                 }
                 break;
             case 10 :
                 // RuleSet.g:1:69: WITH
                 {
                 mWITH(); if (state.failed) return ;
 
                 }
                 break;
             case 11 :
                 // RuleSet.g:1:74: USING
                 {
                 mUSING(); if (state.failed) return ;
 
                 }
                 break;
             case 12 :
                 // RuleSet.g:1:80: SETTING
                 {
                 mSETTING(); if (state.failed) return ;
 
                 }
                 break;
             case 13 :
                 // RuleSet.g:1:88: PRE
                 {
                 mPRE(); if (state.failed) return ;
 
                 }
                 break;
             case 14 :
                 // RuleSet.g:1:92: FOREACH
                 {
                 mFOREACH(); if (state.failed) return ;
 
                 }
                 break;
             case 15 :
                 // RuleSet.g:1:100: WHEN
                 {
                 mWHEN(); if (state.failed) return ;
 
                 }
                 break;
             case 16 :
                 // RuleSet.g:1:105: OR_OR
                 {
                 mOR_OR(); if (state.failed) return ;
 
                 }
                 break;
             case 17 :
                 // RuleSet.g:1:111: AND_AND
                 {
                 mAND_AND(); if (state.failed) return ;
 
                 }
                 break;
             case 18 :
                 // RuleSet.g:1:119: BETWEEN
                 {
                 mBETWEEN(); if (state.failed) return ;
 
                 }
                 break;
             case 19 :
                 // RuleSet.g:1:127: WEB
                 {
                 mWEB(); if (state.failed) return ;
 
                 }
                 break;
             case 20 :
                 // RuleSet.g:1:131: PAGEVIEW
                 {
                 mPAGEVIEW(); if (state.failed) return ;
 
                 }
                 break;
             case 21 :
                 // RuleSet.g:1:140: LEFT_SMALL_ARROW
                 {
                 mLEFT_SMALL_ARROW(); if (state.failed) return ;
 
                 }
                 break;
             case 22 :
                 // RuleSet.g:1:157: RIGHT_SMALL_ARROW
                 {
                 mRIGHT_SMALL_ARROW(); if (state.failed) return ;
 
                 }
                 break;
             case 23 :
                 // RuleSet.g:1:175: GLOBAL
                 {
                 mGLOBAL(); if (state.failed) return ;
 
                 }
                 break;
             case 24 :
                 // RuleSet.g:1:182: DTYPE
                 {
                 mDTYPE(); if (state.failed) return ;
 
                 }
                 break;
             case 25 :
                 // RuleSet.g:1:188: LIKE
                 {
                 mLIKE(); if (state.failed) return ;
 
                 }
                 break;
             case 26 :
                 // RuleSet.g:1:193: PREDOP
                 {
                 mPREDOP(); if (state.failed) return ;
 
                 }
                 break;
             case 27 :
                 // RuleSet.g:1:200: ADD_OP
                 {
                 mADD_OP(); if (state.failed) return ;
 
                 }
                 break;
             case 28 :
                 // RuleSet.g:1:207: CALLBACKS
                 {
                 mCALLBACKS(); if (state.failed) return ;
 
                 }
                 break;
             case 29 :
                 // RuleSet.g:1:217: SUCCESS
                 {
                 mSUCCESS(); if (state.failed) return ;
 
                 }
                 break;
             case 30 :
                 // RuleSet.g:1:225: FAILURE
                 {
                 mFAILURE(); if (state.failed) return ;
 
                 }
                 break;
             case 31 :
                 // RuleSet.g:1:233: FORGET
                 {
                 mFORGET(); if (state.failed) return ;
 
                 }
                 break;
             case 32 :
                 // RuleSet.g:1:240: MARK
                 {
                 mMARK(); if (state.failed) return ;
 
                 }
                 break;
             case 33 :
                 // RuleSet.g:1:245: COUNTER_OP
                 {
                 mCOUNTER_OP(); if (state.failed) return ;
 
                 }
                 break;
             case 34 :
                 // RuleSet.g:1:256: IF
                 {
                 mIF(); if (state.failed) return ;
 
                 }
                 break;
             case 35 :
                 // RuleSet.g:1:259: CHOOSE
                 {
                 mCHOOSE(); if (state.failed) return ;
 
                 }
                 break;
             case 36 :
                 // RuleSet.g:1:266: EVERY
                 {
                 mEVERY(); if (state.failed) return ;
 
                 }
                 break;
             case 37 :
                 // RuleSet.g:1:272: COMMENT
                 {
                 mCOMMENT(); if (state.failed) return ;
 
                 }
                 break;
             case 38 :
                 // RuleSet.g:1:280: WS
                 {
                 mWS(); if (state.failed) return ;
 
                 }
                 break;
             case 39 :
                 // RuleSet.g:1:283: STRING
                 {
                 mSTRING(); if (state.failed) return ;
 
                 }
                 break;
             case 40 :
                 // RuleSet.g:1:290: HTML
                 {
                 mHTML(); if (state.failed) return ;
 
                 }
                 break;
             case 41 :
                 // RuleSet.g:1:295: JS
                 {
                 mJS(); if (state.failed) return ;
 
                 }
                 break;
             case 42 :
                 // RuleSet.g:1:298: ESC_SEQ
                 {
                 mESC_SEQ(); if (state.failed) return ;
 
                 }
                 break;
             case 43 :
                 // RuleSet.g:1:306: RULE_SET
                 {
                 mRULE_SET(); if (state.failed) return ;
 
                 }
                 break;
             case 44 :
                 // RuleSet.g:1:315: MULT_OP
                 {
                 mMULT_OP(); if (state.failed) return ;
 
                 }
                 break;
             case 45 :
                 // RuleSet.g:1:323: DOT
                 {
                 mDOT(); if (state.failed) return ;
 
                 }
                 break;
             case 46 :
                 // RuleSet.g:1:327: MATCH
                 {
                 mMATCH(); if (state.failed) return ;
 
                 }
                 break;
             case 47 :
                 // RuleSet.g:1:333: REPLACE
                 {
                 mREPLACE(); if (state.failed) return ;
 
                 }
                 break;
             case 48 :
                 // RuleSet.g:1:341: OTHER_OPERATORS
                 {
                 mOTHER_OPERATORS(); if (state.failed) return ;
 
                 }
                 break;
             case 49 :
                 // RuleSet.g:1:357: TRUE
                 {
                 mTRUE(); if (state.failed) return ;
 
                 }
                 break;
             case 50 :
                 // RuleSet.g:1:362: FALSE
                 {
                 mFALSE(); if (state.failed) return ;
 
                 }
                 break;
             case 51 :
                 // RuleSet.g:1:368: CURRENT
                 {
                 mCURRENT(); if (state.failed) return ;
 
                 }
                 break;
             case 52 :
                 // RuleSet.g:1:376: KEY
                 {
                 mKEY(); if (state.failed) return ;
 
                 }
                 break;
             case 53 :
                 // RuleSet.g:1:380: AUTHZ
                 {
                 mAUTHZ(); if (state.failed) return ;
 
                 }
                 break;
             case 54 :
                 // RuleSet.g:1:386: REQUIRE
                 {
                 mREQUIRE(); if (state.failed) return ;
 
                 }
                 break;
             case 55 :
                 // RuleSet.g:1:394: LOGGING
                 {
                 mLOGGING(); if (state.failed) return ;
 
                 }
                 break;
             case 56 :
                 // RuleSet.g:1:402: USE
                 {
                 mUSE(); if (state.failed) return ;
 
                 }
                 break;
             case 57 :
                 // RuleSet.g:1:406: CSS
                 {
                 mCSS(); if (state.failed) return ;
 
                 }
                 break;
             case 58 :
                 // RuleSet.g:1:410: JAVASCRIPT
                 {
                 mJAVASCRIPT(); if (state.failed) return ;
 
                 }
                 break;
             case 59 :
                 // RuleSet.g:1:421: META
                 {
                 mMETA(); if (state.failed) return ;
 
                 }
                 break;
             case 60 :
                 // RuleSet.g:1:426: ON
                 {
                 mON(); if (state.failed) return ;
 
                 }
                 break;
             case 61 :
                 // RuleSet.g:1:429: OFF
                 {
                 mOFF(); if (state.failed) return ;
 
                 }
                 break;
             case 62 :
                 // RuleSet.g:1:433: MODULE
                 {
                 mMODULE(); if (state.failed) return ;
 
                 }
                 break;
             case 63 :
                 // RuleSet.g:1:440: ALIAS
                 {
                 mALIAS(); if (state.failed) return ;
 
                 }
                 break;
             case 64 :
                 // RuleSet.g:1:446: EMIT
                 {
                 mEMIT(); if (state.failed) return ;
 
                 }
                 break;
             case 65 :
                 // RuleSet.g:1:451: CACHABLE
                 {
                 mCACHABLE(); if (state.failed) return ;
 
                 }
                 break;
             case 66 :
                 // RuleSet.g:1:460: FOR
                 {
                 mFOR(); if (state.failed) return ;
 
                 }
                 break;
             case 67 :
                 // RuleSet.g:1:464: WITHIN
                 {
                 mWITHIN(); if (state.failed) return ;
 
                 }
                 break;
             case 68 :
                 // RuleSet.g:1:471: COLON
                 {
                 mCOLON(); if (state.failed) return ;
 
                 }
                 break;
             case 69 :
                 // RuleSet.g:1:477: HISTORY
                 {
                 mHISTORY(); if (state.failed) return ;
 
                 }
                 break;
             case 70 :
                 // RuleSet.g:1:485: RIGHT_PAREN
                 {
                 mRIGHT_PAREN(); if (state.failed) return ;
 
                 }
                 break;
             case 71 :
                 // RuleSet.g:1:497: LEFT_PAREN
                 {
                 mLEFT_PAREN(); if (state.failed) return ;
 
                 }
                 break;
             case 72 :
                 // RuleSet.g:1:508: RIGHT_BRACKET
                 {
                 mRIGHT_BRACKET(); if (state.failed) return ;
 
                 }
                 break;
             case 73 :
                 // RuleSet.g:1:522: LEFT_BRACKET
                 {
                 mLEFT_BRACKET(); if (state.failed) return ;
 
                 }
                 break;
             case 74 :
                 // RuleSet.g:1:535: COMMA
                 {
                 mCOMMA(); if (state.failed) return ;
 
                 }
                 break;
             case 75 :
                 // RuleSet.g:1:541: LEFT_CURL
                 {
                 mLEFT_CURL(); if (state.failed) return ;
 
                 }
                 break;
             case 76 :
                 // RuleSet.g:1:551: RIGHT_CURL
                 {
                 mRIGHT_CURL(); if (state.failed) return ;
 
                 }
                 break;
             case 77 :
                 // RuleSet.g:1:562: NOT
                 {
                 mNOT(); if (state.failed) return ;
 
                 }
                 break;
             case 78 :
                 // RuleSet.g:1:566: SEEN
                 {
                 mSEEN(); if (state.failed) return ;
 
                 }
                 break;
             case 79 :
                 // RuleSet.g:1:571: VAR
                 {
                 mVAR(); if (state.failed) return ;
 
                 }
                 break;
             case 80 :
                 // RuleSet.g:1:575: INT
                 {
                 mINT(); if (state.failed) return ;
 
                 }
                 break;
             case 81 :
                 // RuleSet.g:1:579: FLOAT
                 {
                 mFLOAT(); if (state.failed) return ;
 
                 }
                 break;
 
         }
 
     }
 
     // $ANTLR start synpred1_RuleSet
     public final void synpred1_RuleSet_fragment() throws RecognitionException {   
         // RuleSet.g:1723:15: ( ESC_SEQ )
         // RuleSet.g:1723:16: ESC_SEQ
         {
         mESC_SEQ(); if (state.failed) return ;
 
         }
     }
     // $ANTLR end synpred1_RuleSet
 
     // $ANTLR start synpred2_RuleSet
     public final void synpred2_RuleSet_fragment() throws RecognitionException {   
         // RuleSet.g:1724:14: ( ESC_SEQ )
         // RuleSet.g:1724:15: ESC_SEQ
         {
         mESC_SEQ(); if (state.failed) return ;
 
         }
     }
     // $ANTLR end synpred2_RuleSet
 
     public final boolean synpred2_RuleSet() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred2_RuleSet_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred1_RuleSet() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred1_RuleSet_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
 
 
     protected DFA1 dfa1 = new DFA1(this);
     protected DFA3 dfa3 = new DFA3(this);
     protected DFA8 dfa8 = new DFA8(this);
     protected DFA23 dfa23 = new DFA23(this);
     protected DFA35 dfa35 = new DFA35(this);
     static final String DFA1_eotS =
         "\6\uffff\1\3\5\uffff\1\3\4\uffff";
     static final String DFA1_eofS =
         "\21\uffff";
     static final String DFA1_minS =
         "\1\0\1\uffff\1\0\1\uffff\6\0\1\uffff\2\0\1\uffff\3\0";
     static final String DFA1_maxS =
         "\1\uffff\1\uffff\1\uffff\1\uffff\1\0\2\uffff\3\0\1\uffff\2\uffff"+
         "\1\uffff\2\uffff\1\0";
     static final String DFA1_acceptS =
         "\1\uffff\1\4\1\uffff\1\3\6\uffff\1\1\2\uffff\1\2\3\uffff";
     static final String DFA1_specialS =
         "\1\4\1\uffff\1\0\1\uffff\1\12\1\5\1\7\1\10\1\11\1\6\1\uffff\1\1"+
         "\1\13\1\uffff\1\2\1\3\1\14}>";
     static final String[] DFA1_transitionS = {
             "\57\3\1\1\54\3\1\2\uffa3\3",
             "",
             "\42\3\1\11\4\3\3\11\3\3\2\11\1\6\4\7\4\10\7\3\1\11\34\3\1\4"+
             "\5\3\1\11\1\3\1\11\1\3\1\11\7\3\1\11\3\3\3\11\1\5\1\3\1\11\uff88"+
             "\3",
             "",
             "\1\uffff",
             "\60\3\12\13\7\3\6\13\32\3\6\13\uff99\3",
             "\147\15\1\14\1\15\1\14\3\15\1\14\uff92\15",
             "\1\uffff",
             "\1\uffff",
             "\1\uffff",
             "",
             "\60\3\12\16\7\3\6\16\32\3\6\16\uff99\3",
             "\147\15\1\14\1\15\1\14\3\15\1\14\uff92\15",
             "",
             "\60\3\12\17\7\3\6\17\32\3\6\17\uff99\3",
             "\60\3\12\20\7\3\6\20\32\3\6\20\uff99\3",
             "\1\uffff"
     };
 
     static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);
     static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
     static final char[] DFA1_min = DFA.unpackEncodedStringToUnsignedChars(DFA1_minS);
     static final char[] DFA1_max = DFA.unpackEncodedStringToUnsignedChars(DFA1_maxS);
     static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);
     static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);
     static final short[][] DFA1_transition;
 
     static {
         int numStates = DFA1_transitionS.length;
         DFA1_transition = new short[numStates][];
         for (int i=0; i<numStates; i++) {
             DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
         }
     }
 
     class DFA1 extends DFA {
 
         public DFA1(BaseRecognizer recognizer) {
             this.recognizer = recognizer;
             this.decisionNumber = 1;
             this.eot = DFA1_eot;
             this.eof = DFA1_eof;
             this.min = DFA1_min;
             this.max = DFA1_max;
             this.accept = DFA1_accept;
             this.special = DFA1_special;
             this.transition = DFA1_transition;
         }
         public String getDescription() {
             return "()* loopback of 1723:14: ( ( ESC_SEQ )=> ESC_SEQ | '\\\\/' | ~ ( '/' ) )*";
         }
         public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
             IntStream input = _input;
         	int _s = s;
             switch ( s ) {
                     case 0 : 
                         int LA1_2 = input.LA(1);
 
                         s = -1;
                         if ( (LA1_2=='\\') ) {s = 4;}
 
                         else if ( (LA1_2=='u') ) {s = 5;}
 
                         else if ( (LA1_2=='/') ) {s = 6;}
 
                         else if ( ((LA1_2>='0' && LA1_2<='3')) ) {s = 7;}
 
                         else if ( ((LA1_2>='4' && LA1_2<='7')) ) {s = 8;}
 
                         else if ( (LA1_2=='\"'||(LA1_2>='\'' && LA1_2<=')')||(LA1_2>='-' && LA1_2<='.')||LA1_2=='?'||LA1_2=='b'||LA1_2=='d'||LA1_2=='f'||LA1_2=='n'||(LA1_2>='r' && LA1_2<='t')||LA1_2=='w') ) {s = 9;}
 
                         else if ( ((LA1_2>='\u0000' && LA1_2<='!')||(LA1_2>='#' && LA1_2<='&')||(LA1_2>='*' && LA1_2<=',')||(LA1_2>='8' && LA1_2<='>')||(LA1_2>='@' && LA1_2<='[')||(LA1_2>=']' && LA1_2<='a')||LA1_2=='c'||LA1_2=='e'||(LA1_2>='g' && LA1_2<='m')||(LA1_2>='o' && LA1_2<='q')||LA1_2=='v'||(LA1_2>='x' && LA1_2<='\uFFFF')) ) {s = 3;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 1 : 
                         int LA1_11 = input.LA(1);
 
                         s = -1;
                         if ( ((LA1_11>='0' && LA1_11<='9')||(LA1_11>='A' && LA1_11<='F')||(LA1_11>='a' && LA1_11<='f')) ) {s = 14;}
 
                         else if ( ((LA1_11>='\u0000' && LA1_11<='/')||(LA1_11>=':' && LA1_11<='@')||(LA1_11>='G' && LA1_11<='`')||(LA1_11>='g' && LA1_11<='\uFFFF')) ) {s = 3;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 2 : 
                         int LA1_14 = input.LA(1);
 
                         s = -1;
                         if ( ((LA1_14>='0' && LA1_14<='9')||(LA1_14>='A' && LA1_14<='F')||(LA1_14>='a' && LA1_14<='f')) ) {s = 15;}
 
                         else if ( ((LA1_14>='\u0000' && LA1_14<='/')||(LA1_14>=':' && LA1_14<='@')||(LA1_14>='G' && LA1_14<='`')||(LA1_14>='g' && LA1_14<='\uFFFF')) ) {s = 3;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 3 : 
                         int LA1_15 = input.LA(1);
 
                         s = -1;
                         if ( ((LA1_15>='0' && LA1_15<='9')||(LA1_15>='A' && LA1_15<='F')||(LA1_15>='a' && LA1_15<='f')) ) {s = 16;}
 
                         else if ( ((LA1_15>='\u0000' && LA1_15<='/')||(LA1_15>=':' && LA1_15<='@')||(LA1_15>='G' && LA1_15<='`')||(LA1_15>='g' && LA1_15<='\uFFFF')) ) {s = 3;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 4 : 
                         int LA1_0 = input.LA(1);
 
                         s = -1;
                         if ( (LA1_0=='/') ) {s = 1;}
 
                         else if ( (LA1_0=='\\') ) {s = 2;}
 
                         else if ( ((LA1_0>='\u0000' && LA1_0<='.')||(LA1_0>='0' && LA1_0<='[')||(LA1_0>=']' && LA1_0<='\uFFFF')) ) {s = 3;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 5 : 
                         int LA1_5 = input.LA(1);
 
                         s = -1;
                         if ( ((LA1_5>='0' && LA1_5<='9')||(LA1_5>='A' && LA1_5<='F')||(LA1_5>='a' && LA1_5<='f')) ) {s = 11;}
 
                         else if ( ((LA1_5>='\u0000' && LA1_5<='/')||(LA1_5>=':' && LA1_5<='@')||(LA1_5>='G' && LA1_5<='`')||(LA1_5>='g' && LA1_5<='\uFFFF')) ) {s = 3;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 6 : 
                         int LA1_9 = input.LA(1);
 
                          
                         int index1_9 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred1_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index1_9);
                         if ( s>=0 ) return s;
                         break;
                     case 7 : 
                         int LA1_6 = input.LA(1);
 
                         s = -1;
                         if ( (LA1_6=='g'||LA1_6=='i'||LA1_6=='m') ) {s = 12;}
 
                         else if ( ((LA1_6>='\u0000' && LA1_6<='f')||LA1_6=='h'||(LA1_6>='j' && LA1_6<='l')||(LA1_6>='n' && LA1_6<='\uFFFF')) ) {s = 13;}
 
                         else s = 3;
 
                         if ( s>=0 ) return s;
                         break;
                     case 8 : 
                         int LA1_7 = input.LA(1);
 
                          
                         int index1_7 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred1_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index1_7);
                         if ( s>=0 ) return s;
                         break;
                     case 9 : 
                         int LA1_8 = input.LA(1);
 
                          
                         int index1_8 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred1_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index1_8);
                         if ( s>=0 ) return s;
                         break;
                     case 10 : 
                         int LA1_4 = input.LA(1);
 
                          
                         int index1_4 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred1_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index1_4);
                         if ( s>=0 ) return s;
                         break;
                     case 11 : 
                         int LA1_12 = input.LA(1);
 
                         s = -1;
                         if ( (LA1_12=='g'||LA1_12=='i'||LA1_12=='m') ) {s = 12;}
 
                         else if ( ((LA1_12>='\u0000' && LA1_12<='f')||LA1_12=='h'||(LA1_12>='j' && LA1_12<='l')||(LA1_12>='n' && LA1_12<='\uFFFF')) ) {s = 13;}
 
                         else s = 3;
 
                         if ( s>=0 ) return s;
                         break;
                     case 12 : 
                         int LA1_16 = input.LA(1);
 
                          
                         int index1_16 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred1_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index1_16);
                         if ( s>=0 ) return s;
                         break;
             }
             if (state.backtracking>0) {state.failed=true; return -1;}
             NoViableAltException nvae =
                 new NoViableAltException(getDescription(), 1, _s, input);
             error(nvae);
             throw nvae;
         }
     }
     static final String DFA3_eotS =
         "\6\uffff\1\3\6\uffff\1\3\3\uffff";
     static final String DFA3_eofS =
         "\21\uffff";
     static final String DFA3_minS =
         "\1\0\1\uffff\1\0\1\uffff\6\0\1\uffff\1\0\1\uffff\4\0";
     static final String DFA3_maxS =
         "\1\uffff\1\uffff\1\uffff\1\uffff\1\0\2\uffff\3\0\1\uffff\1\uffff"+
         "\1\uffff\3\uffff\1\0";
     static final String DFA3_acceptS =
         "\1\uffff\1\4\1\uffff\1\3\6\uffff\1\1\1\uffff\1\2\4\uffff";
     static final String DFA3_specialS =
         "\1\5\1\uffff\1\12\1\uffff\1\13\1\10\1\4\1\11\1\6\1\1\1\uffff\1\2"+
         "\1\uffff\1\14\1\3\1\0\1\7}>";
     static final String[] DFA3_transitionS = {
             "\43\3\1\1\70\3\1\2\uffa3\3",
             "",
             "\42\3\1\10\1\6\3\3\3\10\3\3\2\10\1\3\4\7\4\11\7\3\1\10\34\3"+
             "\1\4\5\3\1\10\1\3\1\10\1\3\1\10\7\3\1\10\3\3\3\10\1\5\1\3\1"+
             "\10\uff88\3",
             "",
             "\1\uffff",
             "\60\3\12\13\7\3\6\13\32\3\6\13\uff99\3",
             "\147\14\1\15\1\14\1\15\3\14\1\15\uff92\14",
             "\1\uffff",
             "\1\uffff",
             "\1\uffff",
             "",
             "\60\3\12\16\7\3\6\16\32\3\6\16\uff99\3",
             "",
             "\147\14\1\15\1\14\1\15\3\14\1\15\uff92\14",
             "\60\3\12\17\7\3\6\17\32\3\6\17\uff99\3",
             "\60\3\12\20\7\3\6\20\32\3\6\20\uff99\3",
             "\1\uffff"
     };
 
     static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
     static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
     static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
     static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
     static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
     static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
     static final short[][] DFA3_transition;
 
     static {
         int numStates = DFA3_transitionS.length;
         DFA3_transition = new short[numStates][];
         for (int i=0; i<numStates; i++) {
             DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
         }
     }
 
     class DFA3 extends DFA {
 
         public DFA3(BaseRecognizer recognizer) {
             this.recognizer = recognizer;
             this.decisionNumber = 3;
             this.eot = DFA3_eot;
             this.eof = DFA3_eof;
             this.min = DFA3_min;
             this.max = DFA3_max;
             this.accept = DFA3_accept;
             this.special = DFA3_special;
             this.transition = DFA3_transition;
         }
         public String getDescription() {
             return "()* loopback of 1724:13: ( ( ESC_SEQ )=> ESC_SEQ | '\\\\#' | ~ ( '#' ) )*";
         }
         public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
             IntStream input = _input;
         	int _s = s;
             switch ( s ) {
                     case 0 : 
                         int LA3_15 = input.LA(1);
 
                         s = -1;
                         if ( ((LA3_15>='\u0000' && LA3_15<='/')||(LA3_15>=':' && LA3_15<='@')||(LA3_15>='G' && LA3_15<='`')||(LA3_15>='g' && LA3_15<='\uFFFF')) ) {s = 3;}
 
                         else if ( ((LA3_15>='0' && LA3_15<='9')||(LA3_15>='A' && LA3_15<='F')||(LA3_15>='a' && LA3_15<='f')) ) {s = 16;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 1 : 
                         int LA3_9 = input.LA(1);
 
                          
                         int index3_9 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred2_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index3_9);
                         if ( s>=0 ) return s;
                         break;
                     case 2 : 
                         int LA3_11 = input.LA(1);
 
                         s = -1;
                         if ( ((LA3_11>='\u0000' && LA3_11<='/')||(LA3_11>=':' && LA3_11<='@')||(LA3_11>='G' && LA3_11<='`')||(LA3_11>='g' && LA3_11<='\uFFFF')) ) {s = 3;}
 
                         else if ( ((LA3_11>='0' && LA3_11<='9')||(LA3_11>='A' && LA3_11<='F')||(LA3_11>='a' && LA3_11<='f')) ) {s = 14;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 3 : 
                         int LA3_14 = input.LA(1);
 
                         s = -1;
                         if ( ((LA3_14>='\u0000' && LA3_14<='/')||(LA3_14>=':' && LA3_14<='@')||(LA3_14>='G' && LA3_14<='`')||(LA3_14>='g' && LA3_14<='\uFFFF')) ) {s = 3;}
 
                         else if ( ((LA3_14>='0' && LA3_14<='9')||(LA3_14>='A' && LA3_14<='F')||(LA3_14>='a' && LA3_14<='f')) ) {s = 15;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 4 : 
                         int LA3_6 = input.LA(1);
 
                         s = -1;
                         if ( ((LA3_6>='\u0000' && LA3_6<='f')||LA3_6=='h'||(LA3_6>='j' && LA3_6<='l')||(LA3_6>='n' && LA3_6<='\uFFFF')) ) {s = 12;}
 
                         else if ( (LA3_6=='g'||LA3_6=='i'||LA3_6=='m') ) {s = 13;}
 
                         else s = 3;
 
                         if ( s>=0 ) return s;
                         break;
                     case 5 : 
                         int LA3_0 = input.LA(1);
 
                         s = -1;
                         if ( (LA3_0=='#') ) {s = 1;}
 
                         else if ( (LA3_0=='\\') ) {s = 2;}
 
                         else if ( ((LA3_0>='\u0000' && LA3_0<='\"')||(LA3_0>='$' && LA3_0<='[')||(LA3_0>=']' && LA3_0<='\uFFFF')) ) {s = 3;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 6 : 
                         int LA3_8 = input.LA(1);
 
                          
                         int index3_8 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred2_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index3_8);
                         if ( s>=0 ) return s;
                         break;
                     case 7 : 
                         int LA3_16 = input.LA(1);
 
                          
                         int index3_16 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred2_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index3_16);
                         if ( s>=0 ) return s;
                         break;
                     case 8 : 
                         int LA3_5 = input.LA(1);
 
                         s = -1;
                         if ( ((LA3_5>='\u0000' && LA3_5<='/')||(LA3_5>=':' && LA3_5<='@')||(LA3_5>='G' && LA3_5<='`')||(LA3_5>='g' && LA3_5<='\uFFFF')) ) {s = 3;}
 
                         else if ( ((LA3_5>='0' && LA3_5<='9')||(LA3_5>='A' && LA3_5<='F')||(LA3_5>='a' && LA3_5<='f')) ) {s = 11;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 9 : 
                         int LA3_7 = input.LA(1);
 
                          
                         int index3_7 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred2_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index3_7);
                         if ( s>=0 ) return s;
                         break;
                     case 10 : 
                         int LA3_2 = input.LA(1);
 
                         s = -1;
                         if ( (LA3_2=='\\') ) {s = 4;}
 
                         else if ( (LA3_2=='u') ) {s = 5;}
 
                         else if ( (LA3_2=='#') ) {s = 6;}
 
                         else if ( ((LA3_2>='0' && LA3_2<='3')) ) {s = 7;}
 
                         else if ( (LA3_2=='\"'||(LA3_2>='\'' && LA3_2<=')')||(LA3_2>='-' && LA3_2<='.')||LA3_2=='?'||LA3_2=='b'||LA3_2=='d'||LA3_2=='f'||LA3_2=='n'||(LA3_2>='r' && LA3_2<='t')||LA3_2=='w') ) {s = 8;}
 
                         else if ( ((LA3_2>='4' && LA3_2<='7')) ) {s = 9;}
 
                         else if ( ((LA3_2>='\u0000' && LA3_2<='!')||(LA3_2>='$' && LA3_2<='&')||(LA3_2>='*' && LA3_2<=',')||LA3_2=='/'||(LA3_2>='8' && LA3_2<='>')||(LA3_2>='@' && LA3_2<='[')||(LA3_2>=']' && LA3_2<='a')||LA3_2=='c'||LA3_2=='e'||(LA3_2>='g' && LA3_2<='m')||(LA3_2>='o' && LA3_2<='q')||LA3_2=='v'||(LA3_2>='x' && LA3_2<='\uFFFF')) ) {s = 3;}
 
                         if ( s>=0 ) return s;
                         break;
                     case 11 : 
                         int LA3_4 = input.LA(1);
 
                          
                         int index3_4 = input.index();
                         input.rewind();
                         s = -1;
                         if ( (synpred2_RuleSet()) ) {s = 10;}
 
                         else if ( (true) ) {s = 3;}
 
                          
                         input.seek(index3_4);
                         if ( s>=0 ) return s;
                         break;
                     case 12 : 
                         int LA3_13 = input.LA(1);
 
                         s = -1;
                         if ( ((LA3_13>='\u0000' && LA3_13<='f')||LA3_13=='h'||(LA3_13>='j' && LA3_13<='l')||(LA3_13>='n' && LA3_13<='\uFFFF')) ) {s = 12;}
 
                         else if ( (LA3_13=='g'||LA3_13=='i'||LA3_13=='m') ) {s = 13;}
 
                         else s = 3;
 
                         if ( s>=0 ) return s;
                         break;
             }
             if (state.backtracking>0) {state.failed=true; return -1;}
             NoViableAltException nvae =
                 new NoViableAltException(getDescription(), 3, _s, input);
             error(nvae);
             throw nvae;
         }
     }
     static final String DFA8_eotS =
         "\1\uffff\1\10\1\12\10\uffff";
     static final String DFA8_eofS =
         "\13\uffff";
     static final String DFA8_minS =
         "\1\41\2\75\10\uffff";
     static final String DFA8_maxS =
         "\1\156\2\75\10\uffff";
     static final String DFA8_acceptS =
         "\3\uffff\1\5\1\6\1\7\1\10\1\1\1\3\1\2\1\4";
     static final String DFA8_specialS =
         "\13\uffff}>";
     static final String[] DFA8_transitionS = {
             "\1\4\32\uffff\1\1\1\3\1\2\46\uffff\1\5\10\uffff\1\6",
             "\1\7",
             "\1\11",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             ""
     };
 
     static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
     static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
     static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
     static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
     static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
     static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
     static final short[][] DFA8_transition;
 
     static {
         int numStates = DFA8_transitionS.length;
         DFA8_transition = new short[numStates][];
         for (int i=0; i<numStates; i++) {
             DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
         }
     }
 
     class DFA8 extends DFA {
 
         public DFA8(BaseRecognizer recognizer) {
             this.recognizer = recognizer;
             this.decisionNumber = 8;
             this.eot = DFA8_eot;
             this.eof = DFA8_eof;
             this.min = DFA8_min;
             this.max = DFA8_max;
             this.accept = DFA8_accept;
             this.special = DFA8_special;
             this.transition = DFA8_transition;
         }
         public String getDescription() {
             return "1788:1: PREDOP : ( '<=' | '>=' | '<' | '>' | '==' | '!=' | 'eq' | 'neq' );";
         }
     }
     static final String DFA23_eotS =
         "\34\uffff";
     static final String DFA23_eofS =
         "\34\uffff";
     static final String DFA23_minS =
         "\1\141\1\uffff\1\143\1\uffff\1\141\1\uffff\1\157\2\uffff\1\143\2"+
         "\uffff\1\151\11\uffff\1\151\2\uffff\1\157\2\uffff";
     static final String DFA23_maxS =
         "\1\165\1\uffff\1\145\1\uffff\1\145\1\uffff\1\160\2\uffff\1\156\2"+
         "\uffff\1\165\11\uffff\1\151\2\uffff\1\161\2\uffff";
     static final String DFA23_acceptS =
         "\1\uffff\1\1\1\uffff\1\3\1\uffff\1\5\1\uffff\1\7\1\10\1\uffff\1"+
         "\14\1\15\1\uffff\1\21\1\23\1\2\1\12\1\4\1\16\1\6\1\13\1\11\1\uffff"+
         "\1\20\1\24\1\uffff\1\17\1\22";
     static final String DFA23_specialS =
         "\34\uffff}>";
     static final String[] DFA23_transitionS = {
             "\1\3\2\uffff\1\14\1\uffff\1\7\1\uffff\1\4\1\15\1\12\1\uffff"+
             "\1\2\1\10\1\uffff\1\16\1\1\1\13\1\uffff\1\6\1\5\1\11",
             "",
             "\1\20\1\uffff\1\17",
             "",
             "\1\22\3\uffff\1\21",
             "",
             "\1\23\1\24",
             "",
             "",
             "\1\25\12\uffff\1\26",
             "",
             "",
             "\1\27\13\uffff\1\30",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "\1\31",
             "",
             "",
             "\1\32\1\uffff\1\33",
             "",
             ""
     };
 
     static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
     static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
     static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
     static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
     static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
     static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
     static final short[][] DFA23_transition;
 
     static {
         int numStates = DFA23_transitionS.length;
         DFA23_transition = new short[numStates][];
         for (int i=0; i<numStates; i++) {
             DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
         }
     }
 
     class DFA23 extends DFA {
 
         public DFA23(BaseRecognizer recognizer) {
             this.recognizer = recognizer;
             this.decisionNumber = 23;
             this.eot = DFA23_eot;
             this.eof = DFA23_eof;
             this.min = DFA23_min;
             this.max = DFA23_max;
             this.accept = DFA23_accept;
             this.special = DFA23_special;
             this.transition = DFA23_transition;
         }
         public String getDescription() {
             return "1900:1: OTHER_OPERATORS : ( 'pick' | 'length' | 'as' | 'head' | 'tail' | 'sort' | 'filter' | 'map' | 'uc' | 'lc' | 'split' | 'join' | 'query' | 'has' | 'union' | 'difference' | 'intersection' | 'unique' | 'once' | 'duplicates' );";
         }
     }
     static final String DFA35_eotS =
         "\1\uffff\1\62\1\uffff\1\67\1\71\1\uffff\1\62\1\uffff\10\62\1\30"+
         "\1\135\6\62\1\uffff\1\62\1\135\3\62\1\42\4\uffff\1\164\6\62\11\uffff"+
         "\1\u0080\2\62\4\uffff\5\62\1\30\4\62\1\u008e\6\62\1\u008e\10\62"+
         "\1\u009f\1\u00a1\2\62\7\uffff\7\62\1\u008e\12\62\1\u00b8\1\62\2"+
         "\uffff\13\62\1\uffff\4\62\1\u00cb\3\62\1\u00cf\2\62\1\u00cf\1\u00d2"+
         "\1\uffff\4\62\1\u00d7\1\62\1\u00d9\6\62\1\u00e1\2\62\1\uffff\1\62"+
         "\1\uffff\1\u00e5\3\62\2\u00e9\4\62\1\30\1\u00ee\4\62\1\u00f3\2\62"+
         "\1\u008e\2\62\1\uffff\2\62\1\u008e\10\62\1\u0102\6\62\1\uffff\3"+
         "\62\1\uffff\1\62\1\u010d\1\uffff\2\62\1\u0111\1\u0112\1\uffff\1"+
         "\62\1\uffff\3\62\1\u0117\1\62\1\u008e\1\62\1\uffff\1\62\2\u008e"+
         "\1\uffff\2\62\1\u00e9\1\uffff\1\u00e9\1\u011d\2\62\1\uffff\4\62"+
         "\1\uffff\1\u0124\1\62\1\u0126\2\62\1\u008e\1\62\1\u008e\1\u012a"+
         "\1\u008e\4\62\1\uffff\7\62\1\u0136\1\62\1\u0138\1\uffff\1\u0139"+
         "\1\u013a\1\62\2\uffff\1\u013c\1\u008e\2\62\1\uffff\1\62\1\u008e"+
         "\3\62\1\uffff\6\62\1\uffff\1\u0149\1\uffff\3\62\1\uffff\1\62\1\u008e"+
         "\7\62\1\u0155\1\62\1\uffff\1\u008e\3\uffff\1\u0157\1\uffff\1\u008e"+
         "\4\62\1\u015c\1\u008e\3\62\1\u0160\1\62\1\uffff\1\u0162\5\62\1\u0168"+
         "\1\u0169\1\u016a\1\62\1\u016c\1\uffff\1\u016d\1\uffff\1\u016e\1"+
         "\u016f\1\62\1\u0171\1\uffff\1\u0172\2\62\1\uffff\1\u0175\1\uffff"+
         "\1\62\1\u0177\3\62\3\uffff\1\u017b\4\uffff\1\u017c\2\uffff\1\62"+
         "\1\u017e\1\uffff\1\62\1\uffff\3\62\2\uffff\1\u0183\1\uffff\4\62"+
         "\1\uffff\1\62\1\u0189\2\u008e\1\62\1\uffff\1\u008e";
     static final String DFA35_eofS =
         "\u018b\uffff";
     static final String DFA35_minS =
         "\1\11\1\145\1\uffff\1\75\1\174\1\uffff\1\141\1\uffff\1\155\1\154"+
         "\1\145\1\143\1\145\1\141\1\146\1\145\1\55\1\56\1\154\1\123\1\115"+
         "\1\123\1\124\1\143\1\uffff\1\145\1\75\2\141\1\146\1\52\4\uffff\1"+
         "\60\3\141\1\165\1\151\1\145\11\uffff\1\56\1\57\1\154\4\uffff\1\156"+
         "\1\162\1\151\1\154\1\164\1\60\1\145\1\151\1\160\1\144\1\60\1\164"+
         "\1\151\1\164\1\145\1\142\1\145\1\60\1\151\1\145\1\143\1\162\1\154"+
         "\1\145\1\147\1\143\2\60\1\146\1\164\7\uffff\1\157\1\117\1\114\1"+
         "\123\1\115\1\153\1\156\1\60\1\147\1\161\1\164\1\143\1\157\1\162"+
         "\1\163\1\160\1\164\1\144\1\60\1\164\2\uffff\1\141\2\163\1\151\1"+
         "\165\1\151\1\166\1\145\1\146\1\160\1\171\1\uffff\1\154\1\165\1\145"+
         "\1\143\1\60\1\154\1\163\1\164\1\60\1\162\1\164\2\60\1\uffff\1\150"+
         "\1\141\1\150\1\156\1\60\1\156\1\60\1\157\1\164\1\156\1\143\1\164"+
         "\1\151\1\60\1\145\1\153\1\uffff\1\145\1\uffff\1\60\1\167\1\142\1"+
         "\116\2\60\1\114\1\145\2\147\2\60\1\154\1\150\1\157\1\162\1\60\1"+
         "\153\1\143\1\60\1\141\1\165\1\uffff\1\145\1\144\1\60\1\164\1\154"+
         "\1\145\1\156\1\141\1\162\1\146\1\154\1\60\1\141\1\151\1\163\1\164"+
         "\1\141\1\145\1\uffff\1\165\2\145\1\uffff\1\171\1\60\1\uffff\1\172"+
         "\1\163\2\60\1\uffff\1\147\1\uffff\1\156\1\165\1\151\1\60\1\145\1"+
         "\60\1\164\1\uffff\1\166\2\60\1\uffff\1\145\1\141\1\60\1\uffff\2"+
         "\60\1\164\1\151\1\uffff\1\142\1\141\1\163\1\145\1\uffff\1\60\1\150"+
         "\1\60\1\154\1\162\1\60\1\157\3\60\1\163\1\171\1\145\1\151\1\uffff"+
         "\1\143\1\162\1\145\1\151\1\143\1\164\1\162\1\60\1\162\1\60\1\uffff"+
         "\2\60\1\156\2\uffff\2\60\1\145\1\156\1\uffff\1\163\1\60\1\151\1"+
         "\145\1\154\1\uffff\1\150\1\156\1\141\1\142\1\145\1\156\1\uffff\1"+
         "\60\1\uffff\1\145\1\163\1\162\1\uffff\1\143\1\60\1\162\1\143\2\145"+
         "\1\164\1\157\1\150\1\60\1\145\1\uffff\1\60\3\uffff\1\60\1\uffff"+
         "\1\60\1\147\1\163\1\145\1\156\2\60\1\147\1\143\1\154\1\60\1\164"+
         "\1\uffff\1\60\1\145\1\171\1\162\1\145\1\141\3\60\1\156\1\60\1\uffff"+
         "\1\60\1\uffff\2\60\1\167\1\60\1\uffff\1\60\1\153\1\145\1\uffff\1"+
         "\60\1\uffff\1\143\1\60\1\151\1\156\1\164\3\uffff\1\60\4\uffff\1"+
         "\60\2\uffff\1\163\1\60\1\uffff\1\164\1\uffff\1\160\1\143\1\145\2"+
         "\uffff\1\60\1\uffff\1\151\1\164\1\145\1\163\1\uffff\1\157\3\60\1"+
         "\156\1\uffff\1\60";
     static final String DFA35_maxS =
         "\1\175\1\165\1\uffff\1\76\1\174\1\uffff\1\165\1\uffff\1\166\1\165"+
         "\1\151\1\163\1\165\2\162\1\145\1\174\1\76\1\154\1\123\1\115\1\123"+
         "\1\124\1\157\1\uffff\1\157\1\75\1\165\1\157\1\156\1\57\4\uffff\1"+
         "\145\1\151\1\162\1\157\2\165\1\145\11\uffff\1\71\1\161\1\154\4\uffff"+
         "\1\156\1\162\2\154\1\164\1\172\1\145\1\151\1\160\1\144\1\172\1\164"+
         "\1\151\1\164\1\145\1\142\1\151\1\172\1\151\1\164\1\143\1\162\1\154"+
         "\1\145\1\147\1\143\2\172\1\146\1\164\7\uffff\1\157\1\117\1\114\1"+
         "\123\1\115\1\153\1\156\1\172\1\147\1\161\1\164\1\154\1\157\1\162"+
         "\1\163\2\164\1\144\1\172\1\164\2\uffff\1\141\2\163\1\151\1\165\1"+
         "\151\1\166\1\145\1\146\1\160\1\171\1\uffff\1\154\1\165\1\145\1\143"+
         "\1\172\1\154\1\163\1\164\1\172\1\162\1\164\2\172\1\uffff\1\150\1"+
         "\141\1\150\1\156\1\172\1\156\1\172\1\161\1\164\1\156\1\143\1\164"+
         "\1\151\1\172\1\145\1\153\1\uffff\1\145\1\uffff\1\172\1\167\1\142"+
         "\1\116\2\172\1\114\1\145\2\147\2\172\1\154\1\150\1\157\1\162\1\172"+
         "\1\153\1\143\1\172\1\141\1\165\1\uffff\1\145\1\144\1\172\1\164\1"+
         "\154\1\145\1\156\1\141\1\162\1\146\1\154\1\172\1\141\1\151\1\163"+
         "\1\164\1\141\1\145\1\uffff\1\165\2\145\1\uffff\1\171\1\172\1\uffff"+
         "\1\172\1\163\2\172\1\uffff\1\147\1\uffff\1\156\1\165\1\151\1\172"+
         "\1\145\1\172\1\164\1\uffff\1\166\2\172\1\uffff\1\145\1\141\1\172"+
         "\1\uffff\2\172\1\164\1\151\1\uffff\1\142\1\141\1\163\1\145\1\uffff"+
         "\1\172\1\150\1\172\1\154\1\162\1\172\1\157\3\172\1\163\1\171\1\145"+
         "\1\151\1\uffff\1\143\1\162\1\145\1\151\1\143\1\164\1\162\1\172\1"+
         "\162\1\172\1\uffff\2\172\1\156\2\uffff\2\172\1\145\1\156\1\uffff"+
         "\1\163\1\172\1\151\1\145\1\154\1\uffff\1\150\1\156\1\141\1\142\1"+
         "\145\1\156\1\uffff\1\172\1\uffff\1\145\1\163\1\162\1\uffff\1\143"+
         "\1\172\1\162\1\143\2\145\1\164\1\157\1\150\1\172\1\145\1\uffff\1"+
         "\172\3\uffff\1\172\1\uffff\1\172\1\147\1\163\1\145\1\156\2\172\1"+
         "\147\1\143\1\154\1\172\1\164\1\uffff\1\172\1\145\1\171\1\162\1\145"+
         "\1\141\3\172\1\156\1\172\1\uffff\1\172\1\uffff\2\172\1\167\1\172"+
         "\1\uffff\1\172\1\153\1\145\1\uffff\1\172\1\uffff\1\143\1\172\1\151"+
         "\1\156\1\164\3\uffff\1\172\4\uffff\1\172\2\uffff\1\163\1\172\1\uffff"+
         "\1\164\1\uffff\1\160\1\143\1\145\2\uffff\1\172\1\uffff\1\151\1\164"+
         "\1\145\1\163\1\uffff\1\157\3\172\1\156\1\uffff\1\172";
     static final String DFA35_acceptS =
         "\2\uffff\1\1\2\uffff\1\4\1\uffff\1\10\20\uffff\1\32\6\uffff\1\46"+
         "\1\47\1\52\1\54\7\uffff\1\104\1\106\1\107\1\110\1\111\1\112\1\113"+
         "\1\114\1\117\3\uffff\1\2\1\6\1\7\1\3\36\uffff\1\25\1\50\1\51\1\26"+
         "\1\41\1\33\1\121\24\uffff\1\45\1\55\13\uffff\1\120\15\uffff\1\60"+
         "\20\uffff\1\20\1\uffff\1\74\26\uffff\1\42\22\uffff\1\102\3\uffff"+
         "\1\11\2\uffff\1\21\4\uffff\1\23\1\uffff\1\70\7\uffff\1\15\3\uffff"+
         "\1\75\3\uffff\1\30\4\uffff\1\115\4\uffff\1\71\16\uffff\1\64\12\uffff"+
         "\1\100\3\uffff\1\12\1\17\4\uffff\1\116\5\uffff\1\31\6\uffff\1\40"+
         "\1\uffff\1\73\3\uffff\1\61\13\uffff\1\62\1\uffff\1\44\1\65\1\77"+
         "\1\uffff\1\13\14\uffff\1\56\13\uffff\1\37\1\uffff\1\103\4\uffff"+
         "\1\27\3\uffff\1\43\1\uffff\1\76\5\uffff\1\57\1\66\1\53\1\uffff\1"+
         "\16\1\36\1\14\1\35\1\uffff\1\22\1\67\2\uffff\1\63\1\uffff\1\105"+
         "\3\uffff\1\5\1\24\1\uffff\1\101\4\uffff\1\34\5\uffff\1\72\1\uffff";
     static final String DFA35_specialS =
         "\u018b\uffff}>";
     static final String[] DFA35_transitionS = {
             "\2\37\2\uffff\1\37\22\uffff\1\37\1\30\1\40\1\2\1\uffff\1\42"+
             "\1\7\1\40\1\54\1\53\1\42\1\32\1\57\1\21\1\43\1\36\12\63\1\52"+
             "\1\5\1\20\1\3\1\30\2\uffff\7\62\1\26\1\62\1\23\7\62\1\25\5\62"+
             "\1\24\2\62\1\56\1\41\1\55\1\uffff\1\62\1\uffff\1\11\1\17\1\33"+
             "\1\50\1\10\1\6\1\22\1\44\1\35\1\46\1\51\1\27\1\34\1\31\1\16"+
             "\1\15\1\47\1\1\1\14\1\45\1\13\1\62\1\12\3\62\1\60\1\4\1\61",
             "\1\64\17\uffff\1\65",
             "",
             "\1\30\1\66",
             "\1\70",
             "",
             "\1\74\7\uffff\1\75\5\uffff\1\73\5\uffff\1\72",
             "",
             "\1\101\1\76\2\uffff\1\77\4\uffff\1\100",
             "\1\106\1\uffff\1\103\1\uffff\1\102\2\uffff\1\104\1\uffff\1"+
             "\105",
             "\1\111\2\uffff\1\110\1\107",
             "\1\113\12\uffff\1\114\4\uffff\1\112",
             "\1\115\11\uffff\1\117\1\120\4\uffff\1\116",
             "\1\122\7\uffff\1\123\10\uffff\1\121",
             "\1\126\7\uffff\1\125\3\uffff\1\124",
             "\1\127",
             "\1\130\16\uffff\1\131\77\uffff\1\132",
             "\1\136\1\uffff\12\63\3\uffff\1\134\1\133",
             "\1\137",
             "\1\140",
             "\1\141",
             "\1\142",
             "\1\143",
             "\1\146\1\uffff\1\145\3\uffff\1\144\5\uffff\1\147",
             "",
             "\1\150\11\uffff\1\151",
             "\1\134",
             "\1\152\6\uffff\1\153\12\uffff\1\155\1\uffff\1\154",
             "\1\156\3\uffff\1\157\11\uffff\1\160",
             "\1\161\7\uffff\1\162",
             "\1\163\4\uffff\1\163",
             "",
             "",
             "",
             "",
             "\12\136\13\uffff\1\136\37\uffff\1\136",
             "\1\166\3\uffff\1\165\3\uffff\1\167",
             "\1\170\20\uffff\1\171",
             "\1\173\15\uffff\1\172",
             "\1\174",
             "\1\175\13\uffff\1\176",
             "\1\177",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "\1\136\1\uffff\12\63",
             "\1\2\100\uffff\1\u0081\1\u0082",
             "\1\u0083",
             "",
             "",
             "",
             "",
             "\1\u0084",
             "\1\u0085",
             "\1\u0086\2\uffff\1\u0087",
             "\1\u0088",
             "\1\u0089",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u008a",
             "\1\u008b",
             "\1\u008c",
             "\1\u008d",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u008f",
             "\1\u0090",
             "\1\u0091",
             "\1\u0092",
             "\1\u0093",
             "\1\u0095\3\uffff\1\u0094",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0096",
             "\1\u0098\16\uffff\1\u0097",
             "\1\u0099",
             "\1\u009a",
             "\1\u009b",
             "\1\u009c",
             "\1\u009d",
             "\1\u009e",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\2\62\1\u00a0\27\62",
             "\1\u00a2",
             "\1\u00a3",
             "",
             "",
             "",
             "",
             "",
             "",
             "",
             "\1\u00a4",
             "\1\u00a5",
             "\1\u00a6",
             "\1\u00a7",
             "\1\u00a8",
             "\1\u00a9",
             "\1\u00aa",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00ab",
             "\1\u00ac",
             "\1\u00ad",
             "\1\u00af\10\uffff\1\u00ae",
             "\1\u00b0",
             "\1\u00b1",
             "\1\u00b2",
             "\1\u00b5\1\uffff\1\u00b3\1\uffff\1\u00b4",
             "\1\u00b6",
             "\1\u00b7",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00b9",
             "",
             "",
             "\1\u00ba",
             "\1\u00bb",
             "\1\u00bc",
             "\1\u00bd",
             "\1\u00be",
             "\1\u00bf",
             "\1\u00c0",
             "\1\u00c1",
             "\1\u00c2",
             "\1\u00c3",
             "\1\u00c4",
             "",
             "\1\u00c5",
             "\1\u00c6",
             "\1\u00c7",
             "\1\u00c8",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\4\62\1\u00c9\1\62"+
             "\1\u00ca\23\62",
             "\1\u00cc",
             "\1\u00cd",
             "\1\u00ce",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00d0",
             "\1\u00d1",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\1\u00d3",
             "\1\u00d4",
             "\1\u00d5",
             "\1\u00d6",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00d8",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00da\1\uffff\1\u00db",
             "\1\u00dc",
             "\1\u00dd",
             "\1\u00de",
             "\1\u00df",
             "\1\u00e0",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00e2",
             "\1\u00e3",
             "",
             "\1\u00e4",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00e6",
             "\1\u00e7",
             "\1\u00e8",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00ea",
             "\1\u00eb",
             "\1\u00ec",
             "\1\u00ed",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00ef",
             "\1\u00f0",
             "\1\u00f1",
             "\1\u00f2",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00f4",
             "\1\u00f5",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00f6",
             "\1\u00f7",
             "",
             "\1\u00f8",
             "\1\u00f9",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u00fa",
             "\1\u00fb",
             "\1\u00fc",
             "\1\u00fd",
             "\1\u00fe",
             "\1\u00ff",
             "\1\u0100",
             "\1\u0101",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0103",
             "\1\u0104",
             "\1\u0105",
             "\1\u0106",
             "\1\u0107",
             "\1\u0108",
             "",
             "\1\u0109",
             "\1\u010a",
             "\1\u010b",
             "",
             "\1\u010c",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\1\u010e",
             "\1\u010f",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\10\62\1\u0110\21"+
             "\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\1\u0113",
             "",
             "\1\u0114",
             "\1\u0115",
             "\1\u0116",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0118",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0119",
             "",
             "\1\u011a",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\1\u011b",
             "\1\u011c",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u011e",
             "\1\u011f",
             "",
             "\1\u0120",
             "\1\u0121",
             "\1\u0122",
             "\1\u0123",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0125",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0127",
             "\1\u0128",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0129",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u012b",
             "\1\u012c",
             "\1\u012d",
             "\1\u012e",
             "",
             "\1\u012f",
             "\1\u0130",
             "\1\u0131",
             "\1\u0132",
             "\1\u0133",
             "\1\u0134",
             "\1\u0135",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0137",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u013b",
             "",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u013d",
             "\1\u013e",
             "",
             "\1\u013f",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0140",
             "\1\u0141",
             "\1\u0142",
             "",
             "\1\u0143",
             "\1\u0144",
             "\1\u0145",
             "\1\u0146",
             "\1\u0147",
             "\1\u0148",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\1\u014a",
             "\1\u014b",
             "\1\u014c",
             "",
             "\1\u014d",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u014e",
             "\1\u014f",
             "\1\u0150",
             "\1\u0151",
             "\1\u0152",
             "\1\u0153",
             "\1\u0154",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0156",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0158",
             "\1\u0159",
             "\1\u015a",
             "\1\u015b",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u015d",
             "\1\u015e",
             "\1\u015f",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0161",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0163",
             "\1\u0164",
             "\1\u0165",
             "\1\u0166",
             "\1\u0167",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u016b",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0170",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0173",
             "\1\u0174",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\1\u0176",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u0178",
             "\1\u0179",
             "\1\u017a",
             "",
             "",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "",
             "",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "",
             "\1\u017d",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\1\u017f",
             "",
             "\1\u0180",
             "\1\u0181",
             "\1\u0182",
             "",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "",
             "\1\u0184",
             "\1\u0185",
             "\1\u0186",
             "\1\u0187",
             "",
             "\1\u0188",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62",
             "\1\u018a",
             "",
             "\12\62\7\uffff\32\62\4\uffff\1\62\1\uffff\32\62"
     };
 
     static final short[] DFA35_eot = DFA.unpackEncodedString(DFA35_eotS);
     static final short[] DFA35_eof = DFA.unpackEncodedString(DFA35_eofS);
     static final char[] DFA35_min = DFA.unpackEncodedStringToUnsignedChars(DFA35_minS);
     static final char[] DFA35_max = DFA.unpackEncodedStringToUnsignedChars(DFA35_maxS);
     static final short[] DFA35_accept = DFA.unpackEncodedString(DFA35_acceptS);
     static final short[] DFA35_special = DFA.unpackEncodedString(DFA35_specialS);
     static final short[][] DFA35_transition;
 
     static {
         int numStates = DFA35_transitionS.length;
         DFA35_transition = new short[numStates][];
         for (int i=0; i<numStates; i++) {
             DFA35_transition[i] = DFA.unpackEncodedString(DFA35_transitionS[i]);
         }
     }
 
     class DFA35 extends DFA {
 
         public DFA35(BaseRecognizer recognizer) {
             this.recognizer = recognizer;
             this.decisionNumber = 35;
             this.eot = DFA35_eot;
             this.eof = DFA35_eof;
             this.min = DFA35_min;
             this.max = DFA35_max;
             this.accept = DFA35_accept;
             this.special = DFA35_special;
             this.transition = DFA35_transition;
         }
         public String getDescription() {
             return "1:1: Tokens : ( REX | ARROW_RIGHT | PIPE | SEMI | FUNCTION | EQUAL | OR | AND | VAR_DOMAIN | WITH | USING | SETTING | PRE | FOREACH | WHEN | OR_OR | AND_AND | BETWEEN | WEB | PAGEVIEW | LEFT_SMALL_ARROW | RIGHT_SMALL_ARROW | GLOBAL | DTYPE | LIKE | PREDOP | ADD_OP | CALLBACKS | SUCCESS | FAILURE | FORGET | MARK | COUNTER_OP | IF | CHOOSE | EVERY | COMMENT | WS | STRING | HTML | JS | ESC_SEQ | RULE_SET | MULT_OP | DOT | MATCH | REPLACE | OTHER_OPERATORS | TRUE | FALSE | CURRENT | KEY | AUTHZ | REQUIRE | LOGGING | USE | CSS | JAVASCRIPT | META | ON | OFF | MODULE | ALIAS | EMIT | CACHABLE | FOR | WITHIN | COLON | HISTORY | RIGHT_PAREN | LEFT_PAREN | RIGHT_BRACKET | LEFT_BRACKET | COMMA | LEFT_CURL | RIGHT_CURL | NOT | SEEN | VAR | INT | FLOAT );";
         }
     }
  
 
 }
