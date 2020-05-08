// $ANTLR 3.4 ../src/grammar/z3.g 2012-01-17 23:52:03
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class z3Parser extends Parser {
     public static final String[] tokenNames = new String[] {
         "<invalid>", "<EOR>", "<DOWN>", "<UP>", "BOOL", "CHAR", "ESC_SEQ", "HEX_DIGIT", "IDENT", "INT", "OCTAL_ESC", "TYPE", "UNICODE_ESC", "WS", "'!'", "'('", "'()'", "'(='", "'(Array'", "'(_as-array'", "'(and'", "'(define-fun'", "'(model'", "')'", "'))'", "'Int'", "'define-fun'", "'sat'", "'unknown'", "'unsat'"
     };
 
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
     public Parser[] getDelegates() {
         return new Parser[] {};
     }
 
     // delegators
 
 
     public z3Parser(TokenStream input) {
         this(input, new RecognizerSharedState());
     }
     public z3Parser(TokenStream input, RecognizerSharedState state) {
         super(input, state);
     }
 
     public String[] getTokenNames() { return z3Parser.tokenNames; }
     public String getGrammarFileName() { return "../src/grammar/z3.g"; }
 
 
 
     // $ANTLR start "start"
     // ../src/grammar/z3.g:3:1: start : ( block )+ ;
     public final void start() throws RecognitionException {
         try {
             // ../src/grammar/z3.g:3:7: ( ( block )+ )
             // ../src/grammar/z3.g:3:9: ( block )+
             {
             // ../src/grammar/z3.g:3:9: ( block )+
             int cnt1=0;
             loop1:
             do {
                 int alt1=2;
                 int LA1_0 = input.LA(1);
 
                 if ( ((LA1_0 >= 27 && LA1_0 <= 29)) ) {
                     alt1=1;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // ../src/grammar/z3.g:3:9: block
             	    {
             	    pushFollow(FOLLOW_block_in_start10);
             	    block();
 
             	    state._fsp--;
 
 
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
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return ;
     }
     // $ANTLR end "start"
 
 
 
     // $ANTLR start "block"
     // ../src/grammar/z3.g:6:1: block : ( 'unsat' ( . )* | 'sat' model | 'unknown' ( . )* );
     public final void block() throws RecognitionException {
         try {
             // ../src/grammar/z3.g:6:7: ( 'unsat' ( . )* | 'sat' model | 'unknown' ( . )* )
             int alt4=3;
             switch ( input.LA(1) ) {
             case 29:
                 {
                 alt4=1;
                 }
                 break;
             case 27:
                 {
                 alt4=2;
                 }
                 break;
             case 28:
                 {
                 alt4=3;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 4, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt4) {
                 case 1 :
                     // ../src/grammar/z3.g:6:9: 'unsat' ( . )*
                     {
                     match(input,29,FOLLOW_29_in_block21); 
 
                     // ../src/grammar/z3.g:7:5: ( . )*
                     loop2:
                     do {
                         int alt2=2;
                         switch ( input.LA(1) ) {
                         case EOF:
                             {
                             alt2=2;
                             }
                             break;
                         case 29:
                             {
                             alt2=2;
                             }
                             break;
                         case 27:
                             {
                             alt2=2;
                             }
                             break;
                         case 28:
                             {
                             alt2=2;
                             }
                             break;
                         case BOOL:
                         case CHAR:
                         case ESC_SEQ:
                         case HEX_DIGIT:
                         case IDENT:
                         case INT:
                         case OCTAL_ESC:
                         case TYPE:
                         case UNICODE_ESC:
                         case WS:
                         case 14:
                         case 15:
                         case 16:
                         case 17:
                         case 18:
                         case 19:
                         case 20:
                         case 21:
                         case 22:
                         case 23:
                         case 24:
                         case 25:
                         case 26:
                             {
                             alt2=1;
                             }
                             break;
 
                         }
 
                         switch (alt2) {
                     	case 1 :
                     	    // ../src/grammar/z3.g:7:5: .
                     	    {
                     	    matchAny(input); 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop2;
                         }
                     } while (true);
 
 
                     }
                     break;
                 case 2 :
                     // ../src/grammar/z3.g:8:5: 'sat' model
                     {
                     match(input,27,FOLLOW_27_in_block34); 
 
                     pushFollow(FOLLOW_model_in_block40);
                     model();
 
                     state._fsp--;
 
 
                     }
                     break;
                 case 3 :
                     // ../src/grammar/z3.g:10:4: 'unknown' ( . )*
                     {
                     match(input,28,FOLLOW_28_in_block45); 
 
                     // ../src/grammar/z3.g:11:3: ( . )*
                     loop3:
                     do {
                         int alt3=2;
                         switch ( input.LA(1) ) {
                         case EOF:
                             {
                             alt3=2;
                             }
                             break;
                         case 29:
                             {
                             alt3=2;
                             }
                             break;
                         case 27:
                             {
                             alt3=2;
                             }
                             break;
                         case 28:
                             {
                             alt3=2;
                             }
                             break;
                         case BOOL:
                         case CHAR:
                         case ESC_SEQ:
                         case HEX_DIGIT:
                         case IDENT:
                         case INT:
                         case OCTAL_ESC:
                         case TYPE:
                         case UNICODE_ESC:
                         case WS:
                         case 14:
                         case 15:
                         case 16:
                         case 17:
                         case 18:
                         case 19:
                         case 20:
                         case 21:
                         case 22:
                         case 23:
                         case 24:
                         case 25:
                         case 26:
                             {
                             alt3=1;
                             }
                             break;
 
                         }
 
                         switch (alt3) {
                     	case 1 :
                     	    // ../src/grammar/z3.g:11:3: .
                     	    {
                     	    matchAny(input); 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop3;
                         }
                     } while (true);
 
 
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
         return ;
     }
     // $ANTLR end "block"
 
 
 
     // $ANTLR start "model"
     // ../src/grammar/z3.g:14:1: model : '(model' ( '(define-fun' IDENT '(' ( IDENT TYPE )* ')' TYPE value ')' )* ( 'define-fun' IDENT '()' '(Array' ( 'Int' )+ TYPE ')' '(_as-array' IDENT '!' INT ')' )* ( '(define-fun' IDENT '!' INT '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE '(' ite '))' )* ')' ;
     public final void model() throws RecognitionException {
         try {
             // ../src/grammar/z3.g:15:2: ( '(model' ( '(define-fun' IDENT '(' ( IDENT TYPE )* ')' TYPE value ')' )* ( 'define-fun' IDENT '()' '(Array' ( 'Int' )+ TYPE ')' '(_as-array' IDENT '!' INT ')' )* ( '(define-fun' IDENT '!' INT '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE '(' ite '))' )* ')' )
             // ../src/grammar/z3.g:15:4: '(model' ( '(define-fun' IDENT '(' ( IDENT TYPE )* ')' TYPE value ')' )* ( 'define-fun' IDENT '()' '(Array' ( 'Int' )+ TYPE ')' '(_as-array' IDENT '!' INT ')' )* ( '(define-fun' IDENT '!' INT '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE '(' ite '))' )* ')'
             {
             match(input,22,FOLLOW_22_in_model61); 
 
             // ../src/grammar/z3.g:16:3: ( '(define-fun' IDENT '(' ( IDENT TYPE )* ')' TYPE value ')' )*
             loop6:
             do {
                 int alt6=2;
                 int LA6_0 = input.LA(1);
 
                 if ( (LA6_0==21) ) {
                     int LA6_2 = input.LA(2);
 
                     if ( (LA6_2==IDENT) ) {
                         int LA6_3 = input.LA(3);
 
                         if ( (LA6_3==15) ) {
                             alt6=1;
                         }
 
 
                     }
 
 
                 }
 
 
                 switch (alt6) {
             	case 1 :
             	    // ../src/grammar/z3.g:16:4: '(define-fun' IDENT '(' ( IDENT TYPE )* ')' TYPE value ')'
             	    {
             	    match(input,21,FOLLOW_21_in_model66); 
 
             	    match(input,IDENT,FOLLOW_IDENT_in_model68); 
 
             	    match(input,15,FOLLOW_15_in_model71); 
 
             	    // ../src/grammar/z3.g:16:28: ( IDENT TYPE )*
             	    loop5:
             	    do {
             	        int alt5=2;
             	        int LA5_0 = input.LA(1);
 
             	        if ( (LA5_0==IDENT) ) {
             	            alt5=1;
             	        }
 
 
             	        switch (alt5) {
             	    	case 1 :
             	    	    // ../src/grammar/z3.g:16:29: IDENT TYPE
             	    	    {
             	    	    match(input,IDENT,FOLLOW_IDENT_in_model73); 
 
             	    	    match(input,TYPE,FOLLOW_TYPE_in_model76); 
 
             	    	    }
             	    	    break;
 
             	    	default :
             	    	    break loop5;
             	        }
             	    } while (true);
 
 
             	    match(input,23,FOLLOW_23_in_model79); 
 
             	    match(input,TYPE,FOLLOW_TYPE_in_model81); 
 
             	    pushFollow(FOLLOW_value_in_model83);
             	    value();
 
             	    state._fsp--;
 
 
             	    match(input,23,FOLLOW_23_in_model85); 
 
             	    }
             	    break;
 
             	default :
             	    break loop6;
                 }
             } while (true);
 
 
             // ../src/grammar/z3.g:17:3: ( 'define-fun' IDENT '()' '(Array' ( 'Int' )+ TYPE ')' '(_as-array' IDENT '!' INT ')' )*
             loop8:
             do {
                 int alt8=2;
                 int LA8_0 = input.LA(1);
 
                 if ( (LA8_0==26) ) {
                     alt8=1;
                 }
 
 
                 switch (alt8) {
             	case 1 :
             	    // ../src/grammar/z3.g:17:4: 'define-fun' IDENT '()' '(Array' ( 'Int' )+ TYPE ')' '(_as-array' IDENT '!' INT ')'
             	    {
             	    match(input,26,FOLLOW_26_in_model92); 
 
             	    match(input,IDENT,FOLLOW_IDENT_in_model94); 
 
             	    match(input,16,FOLLOW_16_in_model96); 
 
             	    match(input,18,FOLLOW_18_in_model98); 
 
             	    // ../src/grammar/z3.g:17:36: ( 'Int' )+
             	    int cnt7=0;
             	    loop7:
             	    do {
             	        int alt7=2;
             	        int LA7_0 = input.LA(1);
 
             	        if ( (LA7_0==25) ) {
             	            alt7=1;
             	        }
 
 
             	        switch (alt7) {
             	    	case 1 :
             	    	    // ../src/grammar/z3.g:17:37: 'Int'
             	    	    {
             	    	    match(input,25,FOLLOW_25_in_model100); 
 
             	    	    }
             	    	    break;
 
             	    	default :
             	    	    if ( cnt7 >= 1 ) break loop7;
             	                EarlyExitException eee =
             	                    new EarlyExitException(7, input);
             	                throw eee;
             	        }
             	        cnt7++;
             	    } while (true);
 
 
             	    match(input,TYPE,FOLLOW_TYPE_in_model104); 
 
             	    match(input,23,FOLLOW_23_in_model107); 
 
             	    match(input,19,FOLLOW_19_in_model111); 
 
             	    match(input,IDENT,FOLLOW_IDENT_in_model113); 
 
             	    match(input,14,FOLLOW_14_in_model115); 
 
             	    match(input,INT,FOLLOW_INT_in_model117); 
 
             	    match(input,23,FOLLOW_23_in_model118); 
 
             	    }
             	    break;
 
             	default :
             	    break loop8;
                 }
             } while (true);
 
 
             // ../src/grammar/z3.g:19:3: ( '(define-fun' IDENT '!' INT '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE '(' ite '))' )*
             loop10:
             do {
                 int alt10=2;
                 int LA10_0 = input.LA(1);
 
                 if ( (LA10_0==21) ) {
                     alt10=1;
                 }
 
 
                 switch (alt10) {
             	case 1 :
             	    // ../src/grammar/z3.g:19:4: '(define-fun' IDENT '!' INT '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE '(' ite '))'
             	    {
             	    match(input,21,FOLLOW_21_in_model125); 
 
             	    match(input,IDENT,FOLLOW_IDENT_in_model127); 
 
             	    match(input,14,FOLLOW_14_in_model129); 
 
             	    match(input,INT,FOLLOW_INT_in_model131); 
 
             	    match(input,15,FOLLOW_15_in_model133); 
 
             	    // ../src/grammar/z3.g:19:35: ( '(' IDENT '!' INT TYPE ')' )+
             	    int cnt9=0;
             	    loop9:
             	    do {
             	        int alt9=2;
             	        int LA9_0 = input.LA(1);
 
             	        if ( (LA9_0==15) ) {
             	            alt9=1;
             	        }
 
 
             	        switch (alt9) {
             	    	case 1 :
             	    	    // ../src/grammar/z3.g:19:36: '(' IDENT '!' INT TYPE ')'
             	    	    {
             	    	    match(input,15,FOLLOW_15_in_model135); 
 
             	    	    match(input,IDENT,FOLLOW_IDENT_in_model136); 
 
             	    	    match(input,14,FOLLOW_14_in_model138); 
 
             	    	    match(input,INT,FOLLOW_INT_in_model140); 
 
             	    	    match(input,TYPE,FOLLOW_TYPE_in_model142); 
 
             	    	    match(input,23,FOLLOW_23_in_model143); 
 
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
 
 
             	    match(input,23,FOLLOW_23_in_model146); 
 
             	    match(input,TYPE,FOLLOW_TYPE_in_model148); 
 
             	    match(input,15,FOLLOW_15_in_model151); 
 
             	    pushFollow(FOLLOW_ite_in_model152);
             	    ite();
 
             	    state._fsp--;
 
 
             	    match(input,24,FOLLOW_24_in_model153); 
 
             	    }
             	    break;
 
             	default :
             	    break loop10;
                 }
             } while (true);
 
 
             match(input,23,FOLLOW_23_in_model159); 
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return ;
     }
     // $ANTLR end "model"
 
 
 
     // $ANTLR start "ite"
     // ../src/grammar/z3.g:23:1: ite : ( '(=' IDENT '!' INT ')' value ( value | ite ) | '(and' ( '(=' IDENT '!' INT ')' )+ ')' value ( value | ite ) );
     public final void ite() throws RecognitionException {
         try {
             // ../src/grammar/z3.g:23:5: ( '(=' IDENT '!' INT ')' value ( value | ite ) | '(and' ( '(=' IDENT '!' INT ')' )+ ')' value ( value | ite ) )
             int alt14=2;
             int LA14_0 = input.LA(1);
 
             if ( (LA14_0==17) ) {
                 alt14=1;
             }
             else if ( (LA14_0==20) ) {
                 alt14=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 14, 0, input);
 
                 throw nvae;
 
             }
             switch (alt14) {
                 case 1 :
                     // ../src/grammar/z3.g:23:7: '(=' IDENT '!' INT ')' value ( value | ite )
                     {
                     match(input,17,FOLLOW_17_in_ite169); 
 
                     match(input,IDENT,FOLLOW_IDENT_in_ite171); 
 
                     match(input,14,FOLLOW_14_in_ite173); 
 
                     match(input,INT,FOLLOW_INT_in_ite175); 
 
                     match(input,23,FOLLOW_23_in_ite176); 
 
                     pushFollow(FOLLOW_value_in_ite179);
                     value();
 
                     state._fsp--;
 
 
                     // ../src/grammar/z3.g:23:36: ( value | ite )
                     int alt11=2;
                     int LA11_0 = input.LA(1);
 
                     if ( (LA11_0==BOOL||LA11_0==INT) ) {
                         alt11=1;
                     }
                     else if ( (LA11_0==17||LA11_0==20) ) {
                         alt11=2;
                     }
                     else {
                         NoViableAltException nvae =
                             new NoViableAltException("", 11, 0, input);
 
                         throw nvae;
 
                     }
                     switch (alt11) {
                         case 1 :
                             // ../src/grammar/z3.g:23:37: value
                             {
                             pushFollow(FOLLOW_value_in_ite182);
                             value();
 
                             state._fsp--;
 
 
                             }
                             break;
                         case 2 :
                             // ../src/grammar/z3.g:23:45: ite
                             {
                             pushFollow(FOLLOW_ite_in_ite186);
                             ite();
 
                             state._fsp--;
 
 
                             }
                             break;
 
                     }
 
 
                     }
                     break;
                 case 2 :
                     // ../src/grammar/z3.g:24:4: '(and' ( '(=' IDENT '!' INT ')' )+ ')' value ( value | ite )
                     {
                     match(input,20,FOLLOW_20_in_ite192); 
 
                     // ../src/grammar/z3.g:24:10: ( '(=' IDENT '!' INT ')' )+
                     int cnt12=0;
                     loop12:
                     do {
                         int alt12=2;
                         int LA12_0 = input.LA(1);
 
                         if ( (LA12_0==17) ) {
                             alt12=1;
                         }
 
 
                         switch (alt12) {
                     	case 1 :
                     	    // ../src/grammar/z3.g:24:11: '(=' IDENT '!' INT ')'
                     	    {
                     	    match(input,17,FOLLOW_17_in_ite194); 
 
                     	    match(input,IDENT,FOLLOW_IDENT_in_ite196); 
 
                     	    match(input,14,FOLLOW_14_in_ite198); 
 
                     	    match(input,INT,FOLLOW_INT_in_ite200); 
 
                     	    match(input,23,FOLLOW_23_in_ite201); 
 
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
 
 
                     match(input,23,FOLLOW_23_in_ite204); 
 
                     pushFollow(FOLLOW_value_in_ite206);
                     value();
 
                     state._fsp--;
 
 
                     // ../src/grammar/z3.g:24:44: ( value | ite )
                     int alt13=2;
                     int LA13_0 = input.LA(1);
 
                     if ( (LA13_0==BOOL||LA13_0==INT) ) {
                         alt13=1;
                     }
                     else if ( (LA13_0==17||LA13_0==20) ) {
                         alt13=2;
                     }
                     else {
                         NoViableAltException nvae =
                             new NoViableAltException("", 13, 0, input);
 
                         throw nvae;
 
                     }
                     switch (alt13) {
                         case 1 :
                             // ../src/grammar/z3.g:24:45: value
                             {
                             pushFollow(FOLLOW_value_in_ite209);
                             value();
 
                             state._fsp--;
 
 
                             }
                             break;
                         case 2 :
                             // ../src/grammar/z3.g:24:53: ite
                             {
                             pushFollow(FOLLOW_ite_in_ite213);
                             ite();
 
                             state._fsp--;
 
 
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
         return ;
     }
     // $ANTLR end "ite"
 
 
 
     // $ANTLR start "value"
     // ../src/grammar/z3.g:27:1: value returns [String content] : ( INT | BOOL );
     public final String value() throws RecognitionException {
         String content = null;
 
 
         Token INT1=null;
         Token BOOL2=null;
 
         try {
             // ../src/grammar/z3.g:28:2: ( INT | BOOL )
             int alt15=2;
             int LA15_0 = input.LA(1);
 
             if ( (LA15_0==INT) ) {
                 alt15=1;
             }
             else if ( (LA15_0==BOOL) ) {
                 alt15=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 15, 0, input);
 
                 throw nvae;
 
             }
             switch (alt15) {
                 case 1 :
                     // ../src/grammar/z3.g:28:4: INT
                     {
                     INT1=(Token)match(input,INT,FOLLOW_INT_in_value229); 
 
                     content =(INT1!=null?INT1.getText():null);
 
                     }
                     break;
                 case 2 :
                     // ../src/grammar/z3.g:29:4: BOOL
                     {
                     BOOL2=(Token)match(input,BOOL,FOLLOW_BOOL_in_value236); 
 
                     content = (BOOL2!=null?BOOL2.getText():null);
 
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
         return content;
     }
     // $ANTLR end "value"
 
     // Delegated rules
 
 
  
 
     public static final BitSet FOLLOW_block_in_start10 = new BitSet(new long[]{0x0000000038000002L});
     public static final BitSet FOLLOW_29_in_block21 = new BitSet(new long[]{0x000000003FFFFFF2L});
     public static final BitSet FOLLOW_27_in_block34 = new BitSet(new long[]{0x0000000000400000L});
     public static final BitSet FOLLOW_model_in_block40 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_28_in_block45 = new BitSet(new long[]{0x000000003FFFFFF2L});
     public static final BitSet FOLLOW_22_in_model61 = new BitSet(new long[]{0x0000000004A00000L});
     public static final BitSet FOLLOW_21_in_model66 = new BitSet(new long[]{0x0000000000000100L});
     public static final BitSet FOLLOW_IDENT_in_model68 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_model71 = new BitSet(new long[]{0x0000000000800100L});
     public static final BitSet FOLLOW_IDENT_in_model73 = new BitSet(new long[]{0x0000000000000800L});
     public static final BitSet FOLLOW_TYPE_in_model76 = new BitSet(new long[]{0x0000000000800100L});
     public static final BitSet FOLLOW_23_in_model79 = new BitSet(new long[]{0x0000000000000800L});
     public static final BitSet FOLLOW_TYPE_in_model81 = new BitSet(new long[]{0x0000000000000210L});
     public static final BitSet FOLLOW_value_in_model83 = new BitSet(new long[]{0x0000000000800000L});
     public static final BitSet FOLLOW_23_in_model85 = new BitSet(new long[]{0x0000000004A00000L});
     public static final BitSet FOLLOW_26_in_model92 = new BitSet(new long[]{0x0000000000000100L});
     public static final BitSet FOLLOW_IDENT_in_model94 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_16_in_model96 = new BitSet(new long[]{0x0000000000040000L});
     public static final BitSet FOLLOW_18_in_model98 = new BitSet(new long[]{0x0000000002000000L});
     public static final BitSet FOLLOW_25_in_model100 = new BitSet(new long[]{0x0000000002000800L});
     public static final BitSet FOLLOW_TYPE_in_model104 = new BitSet(new long[]{0x0000000000800000L});
     public static final BitSet FOLLOW_23_in_model107 = new BitSet(new long[]{0x0000000000080000L});
     public static final BitSet FOLLOW_19_in_model111 = new BitSet(new long[]{0x0000000000000100L});
     public static final BitSet FOLLOW_IDENT_in_model113 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model115 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_INT_in_model117 = new BitSet(new long[]{0x0000000000800000L});
     public static final BitSet FOLLOW_23_in_model118 = new BitSet(new long[]{0x0000000004A00000L});
     public static final BitSet FOLLOW_21_in_model125 = new BitSet(new long[]{0x0000000000000100L});
     public static final BitSet FOLLOW_IDENT_in_model127 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model129 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_INT_in_model131 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_model133 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_model135 = new BitSet(new long[]{0x0000000000000100L});
     public static final BitSet FOLLOW_IDENT_in_model136 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model138 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_INT_in_model140 = new BitSet(new long[]{0x0000000000000800L});
     public static final BitSet FOLLOW_TYPE_in_model142 = new BitSet(new long[]{0x0000000000800000L});
     public static final BitSet FOLLOW_23_in_model143 = new BitSet(new long[]{0x0000000000808000L});
     public static final BitSet FOLLOW_23_in_model146 = new BitSet(new long[]{0x0000000000000800L});
     public static final BitSet FOLLOW_TYPE_in_model148 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_model151 = new BitSet(new long[]{0x0000000000120000L});
     public static final BitSet FOLLOW_ite_in_model152 = new BitSet(new long[]{0x0000000001000000L});
     public static final BitSet FOLLOW_24_in_model153 = new BitSet(new long[]{0x0000000000A00000L});
     public static final BitSet FOLLOW_23_in_model159 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_17_in_ite169 = new BitSet(new long[]{0x0000000000000100L});
     public static final BitSet FOLLOW_IDENT_in_ite171 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_ite173 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_INT_in_ite175 = new BitSet(new long[]{0x0000000000800000L});
     public static final BitSet FOLLOW_23_in_ite176 = new BitSet(new long[]{0x0000000000000210L});
     public static final BitSet FOLLOW_value_in_ite179 = new BitSet(new long[]{0x0000000000120210L});
     public static final BitSet FOLLOW_value_in_ite182 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_ite_in_ite186 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_20_in_ite192 = new BitSet(new long[]{0x0000000000020000L});
     public static final BitSet FOLLOW_17_in_ite194 = new BitSet(new long[]{0x0000000000000100L});
     public static final BitSet FOLLOW_IDENT_in_ite196 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_ite198 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_INT_in_ite200 = new BitSet(new long[]{0x0000000000800000L});
     public static final BitSet FOLLOW_23_in_ite201 = new BitSet(new long[]{0x0000000000820000L});
     public static final BitSet FOLLOW_23_in_ite204 = new BitSet(new long[]{0x0000000000000210L});
     public static final BitSet FOLLOW_value_in_ite206 = new BitSet(new long[]{0x0000000000120210L});
     public static final BitSet FOLLOW_value_in_ite209 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_ite_in_ite213 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_INT_in_value229 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_BOOL_in_value236 = new BitSet(new long[]{0x0000000000000002L});
 
 }
