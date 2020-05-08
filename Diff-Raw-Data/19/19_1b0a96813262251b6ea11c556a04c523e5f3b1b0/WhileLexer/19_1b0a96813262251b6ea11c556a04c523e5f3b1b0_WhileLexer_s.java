 package analyze;
// $ANTLR 3.4 WhileLexer.g 2013-01-11 08:24:22
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class WhileLexer extends Lexer {
     public static final int EOF=-1;
     public static final int AFF=4;
     public static final int AFFECTATION=5;
     public static final int ALTCONDITION=6;
     public static final int CALL=7;
     public static final int COMMA=8;
     public static final int COMMANDE=9;
     public static final int COMMANDES=10;
     public static final int CONDITION=11;
     public static final int CONS=12;
     public static final int CONTENU=13;
     public static final int DEBUTCOMMANDES=14;
     public static final int DEFINITION=15;
     public static final int DO=16;
     public static final int DP=17;
     public static final int ELSE=18;
     public static final int ENTREE=19;
     public static final int EXPRESSION=20;
     public static final int FI=21;
     public static final int FINCOMMANDES=22;
     public static final int FINCONDITION=23;
     public static final int FOR=24;
     public static final int HD=25;
     public static final int ID=26;
     public static final int IF=27;
     public static final int INITIALISATION=28;
     public static final int INPUT=29;
     public static final int ISEQUAL=30;
     public static final int LINEBREAK=31;
     public static final int MIN=32;
     public static final int MUL=33;
     public static final int NIL=34;
     public static final int NOM=35;
     public static final int NOP=36;
     public static final int NUM=37;
     public static final int OD=38;
     public static final int OUTPUT=39;
     public static final int PF=40;
     public static final int PLUS=41;
     public static final int PO=42;
     public static final int PR=43;
     public static final int PROGRAM=44;
     public static final int PROGRAMME=45;
     public static final int PV=46;
     public static final int SEPCOMMANDES=47;
     public static final int SKIP=48;
     public static final int SORTIE=49;
     public static final int THEN=50;
     public static final int TL=51;
     public static final int VAR=52;
     public static final int WHILE=53;
     public static final int WS=54;
 
     // delegates
     // delegators
     public Lexer[] getDelegates() {
         return new Lexer[] {};
     }
 
     public WhileLexer() {} 
     public WhileLexer(CharStream input) {
         this(input, new RecognizerSharedState());
     }
     public WhileLexer(CharStream input, RecognizerSharedState state) {
         super(input,state);
     }
     public String getGrammarFileName() { return "WhileLexer.g"; }
 
     public Token nextToken() {
         while (true) {
             if ( input.LA(1)==CharStream.EOF ) {
                 Token eof = new CommonToken((CharStream)input,Token.EOF,
                                             Token.DEFAULT_CHANNEL,
                                             input.index(),input.index());
                 eof.setLine(getLine());
                 eof.setCharPositionInLine(getCharPositionInLine());
                 return eof;
             }
             state.token = null;
     	state.channel = Token.DEFAULT_CHANNEL;
             state.tokenStartCharIndex = input.index();
             state.tokenStartCharPositionInLine = input.getCharPositionInLine();
             state.tokenStartLine = input.getLine();
     	state.text = null;
             try {
                 int m = input.mark();
                 state.backtracking=1; 
                 state.failed=false;
                 mTokens();
                 state.backtracking=0;
                 if ( state.failed ) {
                     input.rewind(m);
                     input.consume(); 
                 }
                 else {
                     emit();
                     return state.token;
                 }
             }
             catch (RecognitionException re) {
                 // shouldn't happen in backtracking mode, but...
                 reportError(re);
                 recover(re);
             }
         }
     }
 
     public void memoize(IntStream input,
     		int ruleIndex,
     		int ruleStartIndex)
     {
     if ( state.backtracking>1 ) super.memoize(input, ruleIndex, ruleStartIndex);
     }
 
     public boolean alreadyParsedRule(IntStream input, int ruleIndex) {
     if ( state.backtracking>1 ) return super.alreadyParsedRule(input, ruleIndex);
     return false;
     }
     // $ANTLR start "PROGRAM"
     public final void mPROGRAM() throws RecognitionException {
         try {
             int _type = PROGRAM;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:25:9: ( 'program' )
             // WhileLexer.g:25:11: 'program'
             {
             match("program"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "PROGRAM"
 
     // $ANTLR start "INPUT"
     public final void mINPUT() throws RecognitionException {
         try {
             int _type = INPUT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:26:7: ( 'read' )
             // WhileLexer.g:26:9: 'read'
             {
             match("read"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "INPUT"
 
     // $ANTLR start "OUTPUT"
     public final void mOUTPUT() throws RecognitionException {
         try {
             int _type = OUTPUT;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:27:8: ( 'write' )
             // WhileLexer.g:27:10: 'write'
             {
             match("write"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "OUTPUT"
 
     // $ANTLR start "LINEBREAK"
     public final void mLINEBREAK() throws RecognitionException {
         try {
             int _type = LINEBREAK;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:28:11: ( '\\\\' )
             // WhileLexer.g:28:13: '\\\\'
             {
             match('\\'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "LINEBREAK"
 
     // $ANTLR start "WS"
     public final void mWS() throws RecognitionException {
         try {
             int _type = WS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:29:4: ( ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' | '\\\\\\r\\n' )+ )
             // WhileLexer.g:29:7: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' | '\\\\\\r\\n' )+
             {
             // WhileLexer.g:29:7: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' | '\\\\\\r\\n' )+
             int cnt1=0;
             loop1:
             do {
                 int alt1=7;
                 switch ( input.LA(1) ) {
                 case ' ':
                     {
                     alt1=1;
                     }
                     break;
                 case '\r':
                     {
                     alt1=2;
                     }
                     break;
                 case '\t':
                     {
                     alt1=3;
                     }
                     break;
                 case '\f':
                     {
                     alt1=4;
                     }
                     break;
                 case '\n':
                     {
                     alt1=5;
                     }
                     break;
                 case '\\':
                     {
                     alt1=6;
                     }
                     break;
 
                 }
 
                 switch (alt1) {
             	case 1 :
             	    // WhileLexer.g:29:8: ' '
             	    {
             	    match(' '); if (state.failed) return ;
 
             	    }
             	    break;
             	case 2 :
             	    // WhileLexer.g:29:12: '\\r'
             	    {
             	    match('\r'); if (state.failed) return ;
 
             	    }
             	    break;
             	case 3 :
             	    // WhileLexer.g:29:17: '\\t'
             	    {
             	    match('\t'); if (state.failed) return ;
 
             	    }
             	    break;
             	case 4 :
             	    // WhileLexer.g:29:22: '\\u000C'
             	    {
             	    match('\f'); if (state.failed) return ;
 
             	    }
             	    break;
             	case 5 :
             	    // WhileLexer.g:29:31: '\\n'
             	    {
             	    match('\n'); if (state.failed) return ;
 
             	    }
             	    break;
             	case 6 :
             	    // WhileLexer.g:29:36: '\\\\\\r\\n'
             	    {
             	    match("\\\r\n"); if (state.failed) return ;
 
 
 
             	    }
             	    break;
 
             	default :
             	    if ( cnt1 >= 1 ) break loop1;
             	    if (state.backtracking>0) {state.failed=true; return ;}
                         EarlyExitException eee =
                             new EarlyExitException(1, input);
                         throw eee;
                 }
                 cnt1++;
             } while (true);
 
 
             if ( state.backtracking==1 ) { _channel=HIDDEN; }
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "WS"
 
     // $ANTLR start "FOR"
     public final void mFOR() throws RecognitionException {
         try {
             int _type = FOR;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:31:5: ( 'for' )
             // WhileLexer.g:31:7: 'for'
             {
             match("for"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "FOR"
 
     // $ANTLR start "DO"
     public final void mDO() throws RecognitionException {
         try {
             int _type = DO;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:32:4: ( 'do' )
             // WhileLexer.g:32:6: 'do'
             {
             match("do"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "DO"
 
     // $ANTLR start "OD"
     public final void mOD() throws RecognitionException {
         try {
             int _type = OD;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:33:4: ( 'od' )
             // WhileLexer.g:33:6: 'od'
             {
             match("od"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "OD"
 
     // $ANTLR start "WHILE"
     public final void mWHILE() throws RecognitionException {
         try {
             int _type = WHILE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:34:7: ( 'while' )
             // WhileLexer.g:34:9: 'while'
             {
             match("while"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "WHILE"
 
     // $ANTLR start "NIL"
     public final void mNIL() throws RecognitionException {
         try {
             int _type = NIL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:35:5: ( 'nil' )
             // WhileLexer.g:35:7: 'nil'
             {
             match("nil"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "NIL"
 
     // $ANTLR start "COMMA"
     public final void mCOMMA() throws RecognitionException {
         try {
             int _type = COMMA;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:36:7: ( ',' )
             // WhileLexer.g:36:9: ','
             {
             match(','); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "COMMA"
 
     // $ANTLR start "TL"
     public final void mTL() throws RecognitionException {
         try {
             int _type = TL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:37:4: ( 'tl' )
             // WhileLexer.g:37:6: 'tl'
             {
             match("tl"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "TL"
 
     // $ANTLR start "CONS"
     public final void mCONS() throws RecognitionException {
         try {
             int _type = CONS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:38:6: ( 'cons' )
             // WhileLexer.g:38:8: 'cons'
             {
             match("cons"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "CONS"
 
     // $ANTLR start "IF"
     public final void mIF() throws RecognitionException {
         try {
             int _type = IF;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:39:4: ( 'if' )
             // WhileLexer.g:39:6: 'if'
             {
             match("if"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "IF"
 
     // $ANTLR start "FI"
     public final void mFI() throws RecognitionException {
         try {
             int _type = FI;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:40:4: ( 'fi' )
             // WhileLexer.g:40:6: 'fi'
             {
             match("fi"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "FI"
 
     // $ANTLR start "CALL"
     public final void mCALL() throws RecognitionException {
         try {
             int _type = CALL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:41:6: ( 'call' )
             // WhileLexer.g:41:8: 'call'
             {
             match("call"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "CALL"
 
     // $ANTLR start "THEN"
     public final void mTHEN() throws RecognitionException {
         try {
             int _type = THEN;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:42:6: ( 'then' )
             // WhileLexer.g:42:8: 'then'
             {
             match("then"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "THEN"
 
     // $ANTLR start "ELSE"
     public final void mELSE() throws RecognitionException {
         try {
             int _type = ELSE;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:43:6: ( 'else' )
             // WhileLexer.g:43:8: 'else'
             {
             match("else"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "ELSE"
 
     // $ANTLR start "SKIP"
     public final void mSKIP() throws RecognitionException {
         try {
             int _type = SKIP;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:44:6: ( 'skip' )
             // WhileLexer.g:44:8: 'skip'
             {
             match("skip"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "SKIP"
 
     // $ANTLR start "NOP"
     public final void mNOP() throws RecognitionException {
         try {
             int _type = NOP;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:45:5: ( 'nop' )
             // WhileLexer.g:45:7: 'nop'
             {
             match("nop"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "NOP"
 
     // $ANTLR start "HD"
     public final void mHD() throws RecognitionException {
         try {
             int _type = HD;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:46:4: ( 'hd' )
             // WhileLexer.g:46:7: 'hd'
             {
             match("hd"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "HD"
 
     // $ANTLR start "ID"
     public final void mID() throws RecognitionException {
         try {
             int _type = ID;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:47:4: ( ( 'a' .. 'z' )+ )
             // WhileLexer.g:47:6: ( 'a' .. 'z' )+
             {
             // WhileLexer.g:47:6: ( 'a' .. 'z' )+
             int cnt2=0;
             loop2:
             do {
                 int alt2=2;
                 int LA2_0 = input.LA(1);
 
                 if ( ((LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
                     alt2=1;
                 }
 
 
                 switch (alt2) {
             	case 1 :
             	    // WhileLexer.g:
             	    {
             	    if ( (input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
             	        input.consume();
             	        state.failed=false;
             	    }
             	    else {
             	        if (state.backtracking>0) {state.failed=true; return ;}
             	        MismatchedSetException mse = new MismatchedSetException(null,input);
             	        recover(mse);
             	        throw mse;
             	    }
 
 
             	    }
             	    break;
 
             	default :
             	    if ( cnt2 >= 1 ) break loop2;
             	    if (state.backtracking>0) {state.failed=true; return ;}
                         EarlyExitException eee =
                             new EarlyExitException(2, input);
                         throw eee;
                 }
                 cnt2++;
             } while (true);
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "ID"
 
     // $ANTLR start "AFF"
     public final void mAFF() throws RecognitionException {
         try {
             int _type = AFF;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:48:5: ( ':=' )
             // WhileLexer.g:48:7: ':='
             {
             match(":="); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "AFF"
 
     // $ANTLR start "DP"
     public final void mDP() throws RecognitionException {
         try {
             int _type = DP;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:49:4: ( ':' )
             // WhileLexer.g:49:6: ':'
             {
             match(':'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "DP"
 
     // $ANTLR start "VAR"
     public final void mVAR() throws RecognitionException {
         try {
             int _type = VAR;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:50:5: ( 'A' .. 'Z' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )* )
             // WhileLexer.g:50:7: 'A' .. 'Z' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )*
             {
             matchRange('A','Z'); if (state.failed) return ;
 
             // WhileLexer.g:50:15: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' )*
             loop3:
             do {
                 int alt3=2;
                 int LA3_0 = input.LA(1);
 
                 if ( ((LA3_0 >= '0' && LA3_0 <= '9')||(LA3_0 >= 'A' && LA3_0 <= 'Z')||(LA3_0 >= 'a' && LA3_0 <= 'z')) ) {
                     alt3=1;
                 }
 
 
                 switch (alt3) {
             	case 1 :
             	    // WhileLexer.g:
             	    {
             	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
             	        input.consume();
             	        state.failed=false;
             	    }
             	    else {
             	        if (state.backtracking>0) {state.failed=true; return ;}
             	        MismatchedSetException mse = new MismatchedSetException(null,input);
             	        recover(mse);
             	        throw mse;
             	    }
 
 
             	    }
             	    break;
 
             	default :
             	    break loop3;
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
     // $ANTLR end "VAR"
 
     // $ANTLR start "PR"
     public final void mPR() throws RecognitionException {
         try {
             int _type = PR;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:51:4: ( '%' )
             // WhileLexer.g:51:6: '%'
             {
             match('%'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "PR"
 
     // $ANTLR start "NUM"
     public final void mNUM() throws RecognitionException {
         try {
             int _type = NUM;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:52:5: ( '0' .. '9' )
             // WhileLexer.g:
             {
             if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                 input.consume();
                 state.failed=false;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 recover(mse);
                 throw mse;
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
 
     // $ANTLR start "PO"
     public final void mPO() throws RecognitionException {
         try {
             int _type = PO;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:53:4: ( '(' )
             // WhileLexer.g:53:6: '('
             {
             match('('); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "PO"
 
     // $ANTLR start "PF"
     public final void mPF() throws RecognitionException {
         try {
             int _type = PF;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:54:4: ( ')' )
             // WhileLexer.g:54:6: ')'
             {
             match(')'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "PF"
 
     // $ANTLR start "PLUS"
     public final void mPLUS() throws RecognitionException {
         try {
             int _type = PLUS;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:55:6: ( '+' )
             // WhileLexer.g:55:8: '+'
             {
             match('+'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "PLUS"
 
     // $ANTLR start "MIN"
     public final void mMIN() throws RecognitionException {
         try {
             int _type = MIN;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:56:5: ( '-' )
             // WhileLexer.g:56:7: '-'
             {
             match('-'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "MIN"
 
     // $ANTLR start "MUL"
     public final void mMUL() throws RecognitionException {
         try {
             int _type = MUL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:57:5: ( '*' )
             // WhileLexer.g:57:7: '*'
             {
             match('*'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "MUL"
 
     // $ANTLR start "PV"
     public final void mPV() throws RecognitionException {
         try {
             int _type = PV;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:58:4: ( ';' )
             // WhileLexer.g:58:6: ';'
             {
             match(';'); if (state.failed) return ;
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "PV"
 
     // $ANTLR start "ISEQUAL"
     public final void mISEQUAL() throws RecognitionException {
         try {
             int _type = ISEQUAL;
             int _channel = DEFAULT_TOKEN_CHANNEL;
             // WhileLexer.g:59:9: ( '=?' )
             // WhileLexer.g:59:11: '=?'
             {
             match("=?"); if (state.failed) return ;
 
 
 
             }
 
             state.type = _type;
             state.channel = _channel;
         }
         finally {
         	// do for sure before leaving
         }
     }
     // $ANTLR end "ISEQUAL"
 
     public void mTokens() throws RecognitionException {
         // WhileLexer.g:1:39: ( PROGRAM | INPUT | OUTPUT | LINEBREAK | WS | FOR | DO | OD | WHILE | NIL | COMMA | TL | CONS | IF | FI | CALL | THEN | ELSE | SKIP | NOP | HD | ID | AFF | DP | VAR | PR | NUM | PO | PF | PLUS | MIN | MUL | PV | ISEQUAL )
         int alt4=34;
         switch ( input.LA(1) ) {
         case 'p':
             {
             int LA4_1 = input.LA(2);
 
             if ( (synpred1_WhileLexer()) ) {
                 alt4=1;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 1, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'r':
             {
             int LA4_4 = input.LA(2);
 
             if ( (synpred2_WhileLexer()) ) {
                 alt4=2;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 4, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'w':
             {
             int LA4_6 = input.LA(2);
 
             if ( (synpred3_WhileLexer()) ) {
                 alt4=3;
             }
             else if ( (synpred9_WhileLexer()) ) {
                 alt4=9;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 6, input);
 
                 throw nvae;
 
             }
             }
             break;
         case '\\':
             {
             int LA4_9 = input.LA(2);
 
             if ( (synpred4_WhileLexer()) ) {
                 alt4=4;
             }
             else if ( (synpred5_WhileLexer()) ) {
                 alt4=5;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 9, input);
 
                 throw nvae;
 
             }
             }
             break;
         case '\t':
         case '\n':
         case '\f':
         case '\r':
         case ' ':
             {
             alt4=5;
             }
             break;
         case 'f':
             {
             int LA4_13 = input.LA(2);
 
             if ( (synpred6_WhileLexer()) ) {
                 alt4=6;
             }
             else if ( (synpred15_WhileLexer()) ) {
                 alt4=15;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 13, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'd':
             {
             int LA4_16 = input.LA(2);
 
             if ( (synpred7_WhileLexer()) ) {
                 alt4=7;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 16, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'o':
             {
             int LA4_18 = input.LA(2);
 
             if ( (synpred8_WhileLexer()) ) {
                 alt4=8;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 18, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'n':
             {
             int LA4_20 = input.LA(2);
 
             if ( (synpred10_WhileLexer()) ) {
                 alt4=10;
             }
             else if ( (synpred20_WhileLexer()) ) {
                 alt4=20;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 20, input);
 
                 throw nvae;
 
             }
             }
             break;
         case ',':
             {
             alt4=11;
             }
             break;
         case 't':
             {
             int LA4_24 = input.LA(2);
 
             if ( (synpred12_WhileLexer()) ) {
                 alt4=12;
             }
             else if ( (synpred17_WhileLexer()) ) {
                 alt4=17;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 24, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'c':
             {
             int LA4_27 = input.LA(2);
 
             if ( (synpred13_WhileLexer()) ) {
                 alt4=13;
             }
             else if ( (synpred16_WhileLexer()) ) {
                 alt4=16;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 27, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'i':
             {
             int LA4_30 = input.LA(2);
 
             if ( (synpred14_WhileLexer()) ) {
                 alt4=14;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 30, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'e':
             {
             int LA4_32 = input.LA(2);
 
             if ( (synpred18_WhileLexer()) ) {
                 alt4=18;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 32, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 's':
             {
             int LA4_34 = input.LA(2);
 
             if ( (synpred19_WhileLexer()) ) {
                 alt4=19;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 34, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'h':
             {
             int LA4_36 = input.LA(2);
 
             if ( (synpred21_WhileLexer()) ) {
                 alt4=21;
             }
             else if ( (synpred22_WhileLexer()) ) {
                 alt4=22;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 36, input);
 
                 throw nvae;
 
             }
             }
             break;
         case 'a':
         case 'b':
         case 'g':
         case 'j':
         case 'k':
         case 'l':
         case 'm':
         case 'q':
         case 'u':
         case 'v':
         case 'x':
         case 'y':
         case 'z':
             {
             alt4=22;
             }
             break;
         case ':':
             {
             int LA4_39 = input.LA(2);
 
             if ( (synpred23_WhileLexer()) ) {
                 alt4=23;
             }
             else if ( (synpred24_WhileLexer()) ) {
                 alt4=24;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 39, input);
 
                 throw nvae;
 
             }
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
             alt4=25;
             }
             break;
         case '%':
             {
             alt4=26;
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
             alt4=27;
             }
             break;
         case '(':
             {
             alt4=28;
             }
             break;
         case ')':
             {
             alt4=29;
             }
             break;
         case '+':
             {
             alt4=30;
             }
             break;
         case '-':
             {
             alt4=31;
             }
             break;
         case '*':
             {
             alt4=32;
             }
             break;
         case ';':
             {
             alt4=33;
             }
             break;
         case '=':
             {
             alt4=34;
             }
             break;
         default:
             if (state.backtracking>0) {state.failed=true; return ;}
             NoViableAltException nvae =
                 new NoViableAltException("", 4, 0, input);
 
             throw nvae;
 
         }
 
         switch (alt4) {
             case 1 :
                 // WhileLexer.g:1:41: PROGRAM
                 {
                 mPROGRAM(); if (state.failed) return ;
 
 
                 }
                 break;
             case 2 :
                 // WhileLexer.g:1:49: INPUT
                 {
                 mINPUT(); if (state.failed) return ;
 
 
                 }
                 break;
             case 3 :
                 // WhileLexer.g:1:55: OUTPUT
                 {
                 mOUTPUT(); if (state.failed) return ;
 
 
                 }
                 break;
             case 4 :
                 // WhileLexer.g:1:62: LINEBREAK
                 {
                 mLINEBREAK(); if (state.failed) return ;
 
 
                 }
                 break;
             case 5 :
                 // WhileLexer.g:1:72: WS
                 {
                 mWS(); if (state.failed) return ;
 
 
                 }
                 break;
             case 6 :
                 // WhileLexer.g:1:75: FOR
                 {
                 mFOR(); if (state.failed) return ;
 
 
                 }
                 break;
             case 7 :
                 // WhileLexer.g:1:79: DO
                 {
                 mDO(); if (state.failed) return ;
 
 
                 }
                 break;
             case 8 :
                 // WhileLexer.g:1:82: OD
                 {
                 mOD(); if (state.failed) return ;
 
 
                 }
                 break;
             case 9 :
                 // WhileLexer.g:1:85: WHILE
                 {
                 mWHILE(); if (state.failed) return ;
 
 
                 }
                 break;
             case 10 :
                 // WhileLexer.g:1:91: NIL
                 {
                 mNIL(); if (state.failed) return ;
 
 
                 }
                 break;
             case 11 :
                 // WhileLexer.g:1:95: COMMA
                 {
                 mCOMMA(); if (state.failed) return ;
 
 
                 }
                 break;
             case 12 :
                 // WhileLexer.g:1:101: TL
                 {
                 mTL(); if (state.failed) return ;
 
 
                 }
                 break;
             case 13 :
                 // WhileLexer.g:1:104: CONS
                 {
                 mCONS(); if (state.failed) return ;
 
 
                 }
                 break;
             case 14 :
                 // WhileLexer.g:1:109: IF
                 {
                 mIF(); if (state.failed) return ;
 
 
                 }
                 break;
             case 15 :
                 // WhileLexer.g:1:112: FI
                 {
                 mFI(); if (state.failed) return ;
 
 
                 }
                 break;
             case 16 :
                 // WhileLexer.g:1:115: CALL
                 {
                 mCALL(); if (state.failed) return ;
 
 
                 }
                 break;
             case 17 :
                 // WhileLexer.g:1:120: THEN
                 {
                 mTHEN(); if (state.failed) return ;
 
 
                 }
                 break;
             case 18 :
                 // WhileLexer.g:1:125: ELSE
                 {
                 mELSE(); if (state.failed) return ;
 
 
                 }
                 break;
             case 19 :
                 // WhileLexer.g:1:130: SKIP
                 {
                 mSKIP(); if (state.failed) return ;
 
 
                 }
                 break;
             case 20 :
                 // WhileLexer.g:1:135: NOP
                 {
                 mNOP(); if (state.failed) return ;
 
 
                 }
                 break;
             case 21 :
                 // WhileLexer.g:1:139: HD
                 {
                 mHD(); if (state.failed) return ;
 
 
                 }
                 break;
             case 22 :
                 // WhileLexer.g:1:142: ID
                 {
                 mID(); if (state.failed) return ;
 
 
                 }
                 break;
             case 23 :
                 // WhileLexer.g:1:145: AFF
                 {
                 mAFF(); if (state.failed) return ;
 
 
                 }
                 break;
             case 24 :
                 // WhileLexer.g:1:149: DP
                 {
                 mDP(); if (state.failed) return ;
 
 
                 }
                 break;
             case 25 :
                 // WhileLexer.g:1:152: VAR
                 {
                 mVAR(); if (state.failed) return ;
 
 
                 }
                 break;
             case 26 :
                 // WhileLexer.g:1:156: PR
                 {
                 mPR(); if (state.failed) return ;
 
 
                 }
                 break;
             case 27 :
                 // WhileLexer.g:1:159: NUM
                 {
                 mNUM(); if (state.failed) return ;
 
 
                 }
                 break;
             case 28 :
                 // WhileLexer.g:1:163: PO
                 {
                 mPO(); if (state.failed) return ;
 
 
                 }
                 break;
             case 29 :
                 // WhileLexer.g:1:166: PF
                 {
                 mPF(); if (state.failed) return ;
 
 
                 }
                 break;
             case 30 :
                 // WhileLexer.g:1:169: PLUS
                 {
                 mPLUS(); if (state.failed) return ;
 
 
                 }
                 break;
             case 31 :
                 // WhileLexer.g:1:174: MIN
                 {
                 mMIN(); if (state.failed) return ;
 
 
                 }
                 break;
             case 32 :
                 // WhileLexer.g:1:178: MUL
                 {
                 mMUL(); if (state.failed) return ;
 
 
                 }
                 break;
             case 33 :
                 // WhileLexer.g:1:182: PV
                 {
                 mPV(); if (state.failed) return ;
 
 
                 }
                 break;
             case 34 :
                 // WhileLexer.g:1:185: ISEQUAL
                 {
                 mISEQUAL(); if (state.failed) return ;
 
 
                 }
                 break;
 
         }
 
     }
 
     // $ANTLR start synpred1_WhileLexer
     public final void synpred1_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:41: ( PROGRAM )
         // WhileLexer.g:1:41: PROGRAM
         {
         mPROGRAM(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred1_WhileLexer
 
     // $ANTLR start synpred2_WhileLexer
     public final void synpred2_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:49: ( INPUT )
         // WhileLexer.g:1:49: INPUT
         {
         mINPUT(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred2_WhileLexer
 
     // $ANTLR start synpred3_WhileLexer
     public final void synpred3_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:55: ( OUTPUT )
         // WhileLexer.g:1:55: OUTPUT
         {
         mOUTPUT(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred3_WhileLexer
 
     // $ANTLR start synpred4_WhileLexer
     public final void synpred4_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:62: ( LINEBREAK )
         // WhileLexer.g:
         {
         if ( input.LA(1)=='\\' ) {
             input.consume();
             state.failed=false;
         }
         else {
             if (state.backtracking>0) {state.failed=true; return ;}
             MismatchedSetException mse = new MismatchedSetException(null,input);
             recover(mse);
             throw mse;
         }
 
 
         }
 
     }
     // $ANTLR end synpred4_WhileLexer
 
     // $ANTLR start synpred5_WhileLexer
     public final void synpred5_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:72: ( WS )
         // WhileLexer.g:1:72: WS
         {
         mWS(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred5_WhileLexer
 
     // $ANTLR start synpred6_WhileLexer
     public final void synpred6_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:75: ( FOR )
         // WhileLexer.g:1:75: FOR
         {
         mFOR(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred6_WhileLexer
 
     // $ANTLR start synpred7_WhileLexer
     public final void synpred7_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:79: ( DO )
         // WhileLexer.g:1:79: DO
         {
         mDO(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred7_WhileLexer
 
     // $ANTLR start synpred8_WhileLexer
     public final void synpred8_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:82: ( OD )
         // WhileLexer.g:1:82: OD
         {
         mOD(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred8_WhileLexer
 
     // $ANTLR start synpred9_WhileLexer
     public final void synpred9_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:85: ( WHILE )
         // WhileLexer.g:1:85: WHILE
         {
         mWHILE(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred9_WhileLexer
 
     // $ANTLR start synpred10_WhileLexer
     public final void synpred10_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:91: ( NIL )
         // WhileLexer.g:1:91: NIL
         {
         mNIL(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred10_WhileLexer
 
     // $ANTLR start synpred12_WhileLexer
     public final void synpred12_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:101: ( TL )
         // WhileLexer.g:1:101: TL
         {
         mTL(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred12_WhileLexer
 
     // $ANTLR start synpred13_WhileLexer
     public final void synpred13_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:104: ( CONS )
         // WhileLexer.g:1:104: CONS
         {
         mCONS(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred13_WhileLexer
 
     // $ANTLR start synpred14_WhileLexer
     public final void synpred14_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:109: ( IF )
         // WhileLexer.g:1:109: IF
         {
         mIF(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred14_WhileLexer
 
     // $ANTLR start synpred15_WhileLexer
     public final void synpred15_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:112: ( FI )
         // WhileLexer.g:1:112: FI
         {
         mFI(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred15_WhileLexer
 
     // $ANTLR start synpred16_WhileLexer
     public final void synpred16_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:115: ( CALL )
         // WhileLexer.g:1:115: CALL
         {
         mCALL(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred16_WhileLexer
 
     // $ANTLR start synpred17_WhileLexer
     public final void synpred17_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:120: ( THEN )
         // WhileLexer.g:1:120: THEN
         {
         mTHEN(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred17_WhileLexer
 
     // $ANTLR start synpred18_WhileLexer
     public final void synpred18_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:125: ( ELSE )
         // WhileLexer.g:1:125: ELSE
         {
         mELSE(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred18_WhileLexer
 
     // $ANTLR start synpred19_WhileLexer
     public final void synpred19_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:130: ( SKIP )
         // WhileLexer.g:1:130: SKIP
         {
         mSKIP(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred19_WhileLexer
 
     // $ANTLR start synpred20_WhileLexer
     public final void synpred20_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:135: ( NOP )
         // WhileLexer.g:1:135: NOP
         {
         mNOP(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred20_WhileLexer
 
     // $ANTLR start synpred21_WhileLexer
     public final void synpred21_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:139: ( HD )
         // WhileLexer.g:1:139: HD
         {
         mHD(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred21_WhileLexer
 
     // $ANTLR start synpred22_WhileLexer
     public final void synpred22_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:142: ( ID )
         // WhileLexer.g:1:142: ID
         {
         mID(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred22_WhileLexer
 
     // $ANTLR start synpred23_WhileLexer
     public final void synpred23_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:145: ( AFF )
         // WhileLexer.g:1:145: AFF
         {
         mAFF(); if (state.failed) return ;
 
 
         }
 
     }
     // $ANTLR end synpred23_WhileLexer
 
     // $ANTLR start synpred24_WhileLexer
     public final void synpred24_WhileLexer_fragment() throws RecognitionException {
         // WhileLexer.g:1:149: ( DP )
         // WhileLexer.g:
         {
         if ( input.LA(1)==':' ) {
             input.consume();
             state.failed=false;
         }
         else {
             if (state.backtracking>0) {state.failed=true; return ;}
             MismatchedSetException mse = new MismatchedSetException(null,input);
             recover(mse);
             throw mse;
         }
 
 
         }
 
     }
     // $ANTLR end synpred24_WhileLexer
 
     public final boolean synpred2_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred2_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred8_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred8_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred10_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred10_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred24_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred24_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred12_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred12_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred1_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred1_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred16_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred16_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred14_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred14_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred21_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred21_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred5_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred5_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred9_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred9_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred17_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred17_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred23_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred23_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred20_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred20_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred22_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred22_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred3_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred3_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred4_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred4_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred19_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred19_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred18_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred18_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred13_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred13_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred15_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred15_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred7_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred7_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred6_WhileLexer() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred6_WhileLexer_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
 
 
  
 
 }
