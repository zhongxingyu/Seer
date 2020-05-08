// $ANTLR 3.4 /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g 2012-02-23 20:23:17
 
     package verifier.smtlib.z3;
 
     import java.util.LinkedList;
     import java.util.HashMap;
     import misc.Pair;
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class z3Parser extends Parser {
     public static final String[] tokenNames = new String[] {
         "<invalid>", "<EOR>", "<DOWN>", "<UP>", "BOOL", "IDENT", "INT", "TYPE", "WS", "'!'", "'('", "'(Array'", "'(define-fun'", "'(model'", "')'", "'='", "'_'", "'and'", "'as-array'", "'ite'", "'sat'", "'unknown'", "'unsat'"
     };
 
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
     public String getGrammarFileName() { return "/Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g"; }
 
 
 
     // $ANTLR start "start"
     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:15:1: start returns [LinkedList<Pair<Boolean,String>> list] : (pair= block )+ ;
     public final LinkedList<Pair<Boolean,String>> start() throws RecognitionException {
         LinkedList<Pair<Boolean,String>> list = null;
 
 
         Pair<Boolean, String> pair =null;
 
 
         list = new LinkedList<Pair<Boolean,String>>();
         try {
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:16:2: ( (pair= block )+ )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:16:3: (pair= block )+
             {
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:16:3: (pair= block )+
             int cnt1=0;
             loop1:
             do {
                 int alt1=2;
                 int LA1_0 = input.LA(1);
 
                 if ( ((LA1_0 >= 20 && LA1_0 <= 22)) ) {
                     alt1=1;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:16:4: pair= block
             	    {
             	    pushFollow(FOLLOW_block_in_start37);
             	    pair=block();
 
             	    state._fsp--;
 
 
             	    list.add(pair);
 
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
         return list;
     }
     // $ANTLR end "start"
 
 
 
     // $ANTLR start "block"
     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:19:1: block returns [Pair<Boolean, String> result] : ( 'unsat' ( . )* | 'sat' example= model | 'unknown' ( . )* );
     public final Pair<Boolean, String> block() throws RecognitionException {
         Pair<Boolean, String> result = null;
 
 
         String example =null;
 
 
         try {
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:20:2: ( 'unsat' ( . )* | 'sat' example= model | 'unknown' ( . )* )
             int alt4=3;
             switch ( input.LA(1) ) {
             case 22:
                 {
                 alt4=1;
                 }
                 break;
             case 20:
                 {
                 alt4=2;
                 }
                 break;
             case 21:
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
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:20:3: 'unsat' ( . )*
                     {
                     match(input,22,FOLLOW_22_in_block53); 
 
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:21:5: ( . )*
                     loop2:
                     do {
                         int alt2=2;
                         switch ( input.LA(1) ) {
                         case EOF:
                             {
                             alt2=2;
                             }
                             break;
                         case 22:
                             {
                             alt2=2;
                             }
                             break;
                         case 20:
                             {
                             alt2=2;
                             }
                             break;
                         case 21:
                             {
                             alt2=2;
                             }
                             break;
                         case BOOL:
                         case IDENT:
                         case INT:
                         case TYPE:
                         case WS:
                         case 9:
                         case 10:
                         case 11:
                         case 12:
                         case 13:
                         case 14:
                         case 15:
                         case 16:
                         case 17:
                         case 18:
                         case 19:
                             {
                             alt2=1;
                             }
                             break;
 
                         }
 
                         switch (alt2) {
                     	case 1 :
                     	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:21:5: .
                     	    {
                     	    matchAny(input); 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop2;
                         }
                     } while (true);
 
 
                     result = new Pair(true,"");
 
                     }
                     break;
                 case 2 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:22:5: 'sat' example= model
                     {
                     match(input,20,FOLLOW_20_in_block68); 
 
                     pushFollow(FOLLOW_model_in_block78);
                     example=model();
 
                     state._fsp--;
 
 
                     result = new Pair(false, example);
 
                     }
                     break;
                 case 3 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:24:4: 'unknown' ( . )*
                     {
                     match(input,21,FOLLOW_21_in_block85); 
 
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:25:3: ( . )*
                     loop3:
                     do {
                         int alt3=2;
                         switch ( input.LA(1) ) {
                         case EOF:
                             {
                             alt3=2;
                             }
                             break;
                         case 22:
                             {
                             alt3=2;
                             }
                             break;
                         case 20:
                             {
                             alt3=2;
                             }
                             break;
                         case 21:
                             {
                             alt3=2;
                             }
                             break;
                         case BOOL:
                         case IDENT:
                         case INT:
                         case TYPE:
                         case WS:
                         case 9:
                         case 10:
                         case 11:
                         case 12:
                         case 13:
                         case 14:
                         case 15:
                         case 16:
                         case 17:
                         case 18:
                         case 19:
                             {
                             alt3=1;
                             }
                             break;
 
                         }
 
                         switch (alt3) {
                     	case 1 :
                     	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:25:3: .
                     	    {
                     	    matchAny(input); 
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop3;
                         }
                     } while (true);
 
 
                     result = new Pair(true,"unknown");
 
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
         return result;
     }
     // $ANTLR end "block"
 
 
 
     // $ANTLR start "model"
     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:28:1: model returns [String example] : '(model' ( '(define-fun' id= IDENT '(' ( IDENT '!' TYPE )* ')' TYPE val= value ')' )* ( '(define-fun' id= IDENT '(' ')' '(Array' ( TYPE )+ TYPE ')' '(' '_' 'as-array' id2= ( IDENT '!' INT ) ')' ')' )* ( '(define-fun' id3= ( IDENT '!' INT ) '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE ( '(' ass= ite[(String)m.get($id3.text)] ')' )? (v= value )? ')' )* ')' ;
     public final String model() throws RecognitionException {
         String example = null;
 
 
         Token id=null;
         Token id2=null;
         Token id3=null;
         String val =null;
 
         String ass =null;
 
         String v =null;
 
 
         example = "";
         try {
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:29:2: ( '(model' ( '(define-fun' id= IDENT '(' ( IDENT '!' TYPE )* ')' TYPE val= value ')' )* ( '(define-fun' id= IDENT '(' ')' '(Array' ( TYPE )+ TYPE ')' '(' '_' 'as-array' id2= ( IDENT '!' INT ) ')' ')' )* ( '(define-fun' id3= ( IDENT '!' INT ) '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE ( '(' ass= ite[(String)m.get($id3.text)] ')' )? (v= value )? ')' )* ')' )
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:29:4: '(model' ( '(define-fun' id= IDENT '(' ( IDENT '!' TYPE )* ')' TYPE val= value ')' )* ( '(define-fun' id= IDENT '(' ')' '(Array' ( TYPE )+ TYPE ')' '(' '_' 'as-array' id2= ( IDENT '!' INT ) ')' ')' )* ( '(define-fun' id3= ( IDENT '!' INT ) '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE ( '(' ass= ite[(String)m.get($id3.text)] ')' )? (v= value )? ')' )* ')'
             {
             match(input,13,FOLLOW_13_in_model111); 
 
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:30:3: ( '(define-fun' id= IDENT '(' ( IDENT '!' TYPE )* ')' TYPE val= value ')' )*
             loop6:
             do {
                 int alt6=2;
                 int LA6_0 = input.LA(1);
 
                 if ( (LA6_0==12) ) {
                     int LA6_1 = input.LA(2);
 
                     if ( (LA6_1==IDENT) ) {
                         int LA6_3 = input.LA(3);
 
                         if ( (LA6_3==10) ) {
                             int LA6_4 = input.LA(4);
 
                             if ( (LA6_4==14) ) {
                                 int LA6_5 = input.LA(5);
 
                                 if ( (LA6_5==TYPE) ) {
                                     alt6=1;
                                 }
 
 
                             }
                             else if ( (LA6_4==IDENT) ) {
                                 alt6=1;
                             }
 
 
                         }
 
 
                     }
 
 
                 }
 
 
                 switch (alt6) {
             	case 1 :
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:30:4: '(define-fun' id= IDENT '(' ( IDENT '!' TYPE )* ')' TYPE val= value ')'
             	    {
             	    match(input,12,FOLLOW_12_in_model116); 
 
             	    id=(Token)match(input,IDENT,FOLLOW_IDENT_in_model122); 
 
             	    match(input,10,FOLLOW_10_in_model125); 
 
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:30:33: ( IDENT '!' TYPE )*
             	    loop5:
             	    do {
             	        int alt5=2;
             	        int LA5_0 = input.LA(1);
 
             	        if ( (LA5_0==IDENT) ) {
             	            alt5=1;
             	        }
 
 
             	        switch (alt5) {
             	    	case 1 :
             	    	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:30:34: IDENT '!' TYPE
             	    	    {
             	    	    match(input,IDENT,FOLLOW_IDENT_in_model127); 
 
             	    	    match(input,9,FOLLOW_9_in_model129); 
 
             	    	    match(input,TYPE,FOLLOW_TYPE_in_model132); 
 
             	    	    }
             	    	    break;
 
             	    	default :
             	    	    break loop5;
             	        }
             	    } while (true);
 
 
             	    match(input,14,FOLLOW_14_in_model135); 
 
             	    match(input,TYPE,FOLLOW_TYPE_in_model137); 
 
             	    pushFollow(FOLLOW_value_in_model143);
             	    val=value();
 
             	    state._fsp--;
 
 
             	    match(input,14,FOLLOW_14_in_model145); 
 
             	    example += (id!=null?id.getText():null) + "=" + val + "\n";
 
             	    }
             	    break;
 
             	default :
             	    break loop6;
                 }
             } while (true);
 
 
             HashMap m = new HashMap();
 
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:33:3: ( '(define-fun' id= IDENT '(' ')' '(Array' ( TYPE )+ TYPE ')' '(' '_' 'as-array' id2= ( IDENT '!' INT ) ')' ')' )*
             loop8:
             do {
                 int alt8=2;
                 int LA8_0 = input.LA(1);
 
                 if ( (LA8_0==12) ) {
                     int LA8_1 = input.LA(2);
 
                     if ( (LA8_1==IDENT) ) {
                         int LA8_3 = input.LA(3);
 
                         if ( (LA8_3==10) ) {
                             alt8=1;
                         }
 
 
                     }
 
 
                 }
 
 
                 switch (alt8) {
             	case 1 :
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:33:4: '(define-fun' id= IDENT '(' ')' '(Array' ( TYPE )+ TYPE ')' '(' '_' 'as-array' id2= ( IDENT '!' INT ) ')' ')'
             	    {
             	    match(input,12,FOLLOW_12_in_model170); 
 
             	    id=(Token)match(input,IDENT,FOLLOW_IDENT_in_model177); 
 
             	    match(input,10,FOLLOW_10_in_model179); 
 
             	    match(input,14,FOLLOW_14_in_model181); 
 
             	    example = (id!=null?id.getText():null);
 
             	    match(input,11,FOLLOW_11_in_model195); 
 
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:34:19: ( TYPE )+
             	    int cnt7=0;
             	    loop7:
             	    do {
             	        int alt7=2;
             	        int LA7_0 = input.LA(1);
 
             	        if ( (LA7_0==TYPE) ) {
             	            int LA7_1 = input.LA(2);
 
             	            if ( (LA7_1==TYPE) ) {
             	                alt7=1;
             	            }
 
 
             	        }
 
 
             	        switch (alt7) {
             	    	case 1 :
             	    	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:34:20: TYPE
             	    	    {
             	    	    match(input,TYPE,FOLLOW_TYPE_in_model197); 
 
             	    	    example += "[ ]";
 
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
 
 
             	    match(input,TYPE,FOLLOW_TYPE_in_model203); 
 
             	    match(input,14,FOLLOW_14_in_model206); 
 
             	    match(input,10,FOLLOW_10_in_model210); 
 
             	    match(input,16,FOLLOW_16_in_model212); 
 
             	    match(input,18,FOLLOW_18_in_model214); 
 
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:35:29: ( IDENT '!' INT )
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:35:30: IDENT '!' INT
             	    {
             	    match(input,IDENT,FOLLOW_IDENT_in_model222); 
 
             	    match(input,9,FOLLOW_9_in_model224); 
 
             	    match(input,INT,FOLLOW_INT_in_model226); 
 
             	    }
 
 
             	    match(input,14,FOLLOW_14_in_model228); 
 
             	    match(input,14,FOLLOW_14_in_model229); 
 
             	    m.put((id2!=null?id2.getText():null),(id!=null?id.getText():null));
 
             	    example += "\n";
 
             	    }
             	    break;
 
             	default :
             	    break loop8;
                 }
             } while (true);
 
 
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:36:3: ( '(define-fun' id3= ( IDENT '!' INT ) '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE ( '(' ass= ite[(String)m.get($id3.text)] ')' )? (v= value )? ')' )*
             loop12:
             do {
                 int alt12=2;
                 int LA12_0 = input.LA(1);
 
                 if ( (LA12_0==12) ) {
                     alt12=1;
                 }
 
 
                 switch (alt12) {
             	case 1 :
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:36:4: '(define-fun' id3= ( IDENT '!' INT ) '(' ( '(' IDENT '!' INT TYPE ')' )+ ')' TYPE ( '(' ass= ite[(String)m.get($id3.text)] ')' )? (v= value )? ')'
             	    {
             	    match(input,12,FOLLOW_12_in_model239); 
 
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:36:24: ( IDENT '!' INT )
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:36:25: IDENT '!' INT
             	    {
             	    match(input,IDENT,FOLLOW_IDENT_in_model246); 
 
             	    match(input,9,FOLLOW_9_in_model248); 
 
             	    match(input,INT,FOLLOW_INT_in_model250); 
 
             	    }
 
 
             	    match(input,10,FOLLOW_10_in_model253); 
 
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:36:43: ( '(' IDENT '!' INT TYPE ')' )+
             	    int cnt9=0;
             	    loop9:
             	    do {
             	        int alt9=2;
             	        int LA9_0 = input.LA(1);
 
             	        if ( (LA9_0==10) ) {
             	            alt9=1;
             	        }
 
 
             	        switch (alt9) {
             	    	case 1 :
             	    	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:36:44: '(' IDENT '!' INT TYPE ')'
             	    	    {
             	    	    match(input,10,FOLLOW_10_in_model255); 
 
             	    	    match(input,IDENT,FOLLOW_IDENT_in_model256); 
 
             	    	    match(input,9,FOLLOW_9_in_model258); 
 
             	    	    match(input,INT,FOLLOW_INT_in_model260); 
 
             	    	    match(input,TYPE,FOLLOW_TYPE_in_model262); 
 
             	    	    match(input,14,FOLLOW_14_in_model263); 
 
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
 
 
             	    match(input,14,FOLLOW_14_in_model266); 
 
             	    match(input,TYPE,FOLLOW_TYPE_in_model268); 
 
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:37:8: ( '(' ass= ite[(String)m.get($id3.text)] ')' )?
             	    int alt10=2;
             	    int LA10_0 = input.LA(1);
 
             	    if ( (LA10_0==10) ) {
             	        alt10=1;
             	    }
             	    switch (alt10) {
             	        case 1 :
             	            // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:37:10: '(' ass= ite[(String)m.get($id3.text)] ')'
             	            {
             	            match(input,10,FOLLOW_10_in_model279); 
 
             	            pushFollow(FOLLOW_ite_in_model284);
             	            ass=ite((String)m.get((id3!=null?id3.getText():null)));
 
             	            state._fsp--;
 
 
             	            example += ass;
 
             	            match(input,14,FOLLOW_14_in_model288); 
 
             	            }
             	            break;
 
             	    }
 
 
             	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:37:84: (v= value )?
             	    int alt11=2;
             	    int LA11_0 = input.LA(1);
 
             	    if ( (LA11_0==BOOL||LA11_0==INT) ) {
             	        alt11=1;
             	    }
             	    switch (alt11) {
             	        case 1 :
             	            // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:37:85: v= value
             	            {
             	            pushFollow(FOLLOW_value_in_model296);
             	            v=value();
 
             	            state._fsp--;
 
 
            	            example +=(String)m.get((id3!=null?id3.getText():null)) + "=" + v;
 
             	            }
             	            break;
 
             	    }
 
 
             	    match(input,14,FOLLOW_14_in_model300); 
 
             	    }
             	    break;
 
             	default :
             	    break loop12;
                 }
             } while (true);
 
 
             match(input,14,FOLLOW_14_in_model306); 
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return example;
     }
     // $ANTLR end "model"
 
 
 
     // $ANTLR start "ite"
     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:41:1: ite[String id] returns [String assignment] : ( 'ite' '(' '=' IDENT '!' INT i= INT ')' val= value ( value | '(' as= ite[id] ')' ) | 'ite' '(' 'and' ( '(' '=' IDENT '!' INT i= INT ')' )+ ')' val= value ( value | '(' as= ite[id] ')' ) );
     public final String ite(String id) throws RecognitionException {
         String assignment = null;
 
 
         Token i=null;
         String val =null;
 
         String as =null;
 
 
         assignment = id;
         try {
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:42:2: ( 'ite' '(' '=' IDENT '!' INT i= INT ')' val= value ( value | '(' as= ite[id] ')' ) | 'ite' '(' 'and' ( '(' '=' IDENT '!' INT i= INT ')' )+ ')' val= value ( value | '(' as= ite[id] ')' ) )
             int alt16=2;
             int LA16_0 = input.LA(1);
 
             if ( (LA16_0==19) ) {
                 int LA16_1 = input.LA(2);
 
                 if ( (LA16_1==10) ) {
                     int LA16_2 = input.LA(3);
 
                     if ( (LA16_2==15) ) {
                         alt16=1;
                     }
                     else if ( (LA16_2==17) ) {
                         alt16=2;
                     }
                     else {
                         NoViableAltException nvae =
                             new NoViableAltException("", 16, 2, input);
 
                         throw nvae;
 
                     }
                 }
                 else {
                     NoViableAltException nvae =
                         new NoViableAltException("", 16, 1, input);
 
                     throw nvae;
 
                 }
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 16, 0, input);
 
                 throw nvae;
 
             }
             switch (alt16) {
                 case 1 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:42:4: 'ite' '(' '=' IDENT '!' INT i= INT ')' val= value ( value | '(' as= ite[id] ')' )
                     {
                     match(input,19,FOLLOW_19_in_ite326); 
 
                     match(input,10,FOLLOW_10_in_ite327); 
 
                     match(input,15,FOLLOW_15_in_ite328); 
 
                     match(input,IDENT,FOLLOW_IDENT_in_ite330); 
 
                     match(input,9,FOLLOW_9_in_ite332); 
 
                     match(input,INT,FOLLOW_INT_in_ite334); 
 
                     i=(Token)match(input,INT,FOLLOW_INT_in_ite340); 
 
                     match(input,14,FOLLOW_14_in_ite341); 
 
                     pushFollow(FOLLOW_value_in_ite348);
                     val=value();
 
                     state._fsp--;
 
 
                     assignment = "[" + (i!=null?i.getText():null) + "]" + "=" + val + "\n";
 
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:44:9: ( value | '(' as= ite[id] ')' )
                     int alt13=2;
                     int LA13_0 = input.LA(1);
 
                     if ( (LA13_0==BOOL||LA13_0==INT) ) {
                         alt13=1;
                     }
                     else if ( (LA13_0==10) ) {
                         alt13=2;
                     }
                     else {
                         NoViableAltException nvae =
                             new NoViableAltException("", 13, 0, input);
 
                         throw nvae;
 
                     }
                     switch (alt13) {
                         case 1 :
                             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:44:10: value
                             {
                             pushFollow(FOLLOW_value_in_ite369);
                             value();
 
                             state._fsp--;
 
 
                             }
                             break;
                         case 2 :
                             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:44:18: '(' as= ite[id] ')'
                             {
                             match(input,10,FOLLOW_10_in_ite373); 
 
                             pushFollow(FOLLOW_ite_in_ite376);
                             as=ite(id);
 
                             state._fsp--;
 
 
                             match(input,14,FOLLOW_14_in_ite378); 
 
                             assignment += as;
 
                             }
                             break;
 
                     }
 
 
                     }
                     break;
                 case 2 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:45:4: 'ite' '(' 'and' ( '(' '=' IDENT '!' INT i= INT ')' )+ ')' val= value ( value | '(' as= ite[id] ')' )
                     {
                     match(input,19,FOLLOW_19_in_ite385); 
 
                     match(input,10,FOLLOW_10_in_ite386); 
 
                     match(input,17,FOLLOW_17_in_ite387); 
 
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:45:17: ( '(' '=' IDENT '!' INT i= INT ')' )+
                     int cnt14=0;
                     loop14:
                     do {
                         int alt14=2;
                         int LA14_0 = input.LA(1);
 
                         if ( (LA14_0==10) ) {
                             alt14=1;
                         }
 
 
                         switch (alt14) {
                     	case 1 :
                     	    // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:45:18: '(' '=' IDENT '!' INT i= INT ')'
                     	    {
                     	    match(input,10,FOLLOW_10_in_ite389); 
 
                     	    match(input,15,FOLLOW_15_in_ite390); 
 
                     	    match(input,IDENT,FOLLOW_IDENT_in_ite392); 
 
                     	    match(input,9,FOLLOW_9_in_ite394); 
 
                     	    match(input,INT,FOLLOW_INT_in_ite396); 
 
                     	    i=(Token)match(input,INT,FOLLOW_INT_in_ite402); 
 
                     	    match(input,14,FOLLOW_14_in_ite403); 
 
                     	    assignment += "["+(i!=null?i.getText():null)+"]";
 
                     	    }
                     	    break;
 
                     	default :
                     	    if ( cnt14 >= 1 ) break loop14;
                                 EarlyExitException eee =
                                     new EarlyExitException(14, input);
                                 throw eee;
                         }
                         cnt14++;
                     } while (true);
 
 
                     match(input,14,FOLLOW_14_in_ite407); 
 
                     pushFollow(FOLLOW_value_in_ite419);
                     val=value();
 
                     state._fsp--;
 
 
                     assignment += "=" + val + "\n";
 
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:46:62: ( value | '(' as= ite[id] ')' )
                     int alt15=2;
                     int LA15_0 = input.LA(1);
 
                     if ( (LA15_0==BOOL||LA15_0==INT) ) {
                         alt15=1;
                     }
                     else if ( (LA15_0==10) ) {
                         alt15=2;
                     }
                     else {
                         NoViableAltException nvae =
                             new NoViableAltException("", 15, 0, input);
 
                         throw nvae;
 
                     }
                     switch (alt15) {
                         case 1 :
                             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:46:63: value
                             {
                             pushFollow(FOLLOW_value_in_ite423);
                             value();
 
                             state._fsp--;
 
 
                             }
                             break;
                         case 2 :
                             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:46:71: '(' as= ite[id] ')'
                             {
                             match(input,10,FOLLOW_10_in_ite427); 
 
                             pushFollow(FOLLOW_ite_in_ite430);
                             as=ite(id);
 
                             state._fsp--;
 
 
                             match(input,14,FOLLOW_14_in_ite432); 
 
                             assignment += as;
 
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
         return assignment;
     }
     // $ANTLR end "ite"
 
 
 
     // $ANTLR start "value"
     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:49:1: value returns [String content] : ( INT | BOOL );
     public final String value() throws RecognitionException {
         String content = null;
 
 
         Token INT1=null;
         Token BOOL2=null;
 
         try {
             // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:50:2: ( INT | BOOL )
             int alt17=2;
             int LA17_0 = input.LA(1);
 
             if ( (LA17_0==INT) ) {
                 alt17=1;
             }
             else if ( (LA17_0==BOOL) ) {
                 alt17=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 17, 0, input);
 
                 throw nvae;
 
             }
             switch (alt17) {
                 case 1 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:50:4: INT
                     {
                     INT1=(Token)match(input,INT,FOLLOW_INT_in_value449); 
 
                     content =(INT1!=null?INT1.getText():null);
 
                     }
                     break;
                 case 2 :
                     // /Users/DavAdi/Documents/Adrian/PSE/PSE2011/src/grammar/z3.g:51:4: BOOL
                     {
                     BOOL2=(Token)match(input,BOOL,FOLLOW_BOOL_in_value456); 
 
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
 
 
  
 
     public static final BitSet FOLLOW_block_in_start37 = new BitSet(new long[]{0x0000000000700002L});
     public static final BitSet FOLLOW_22_in_block53 = new BitSet(new long[]{0x00000000007FFFF2L});
     public static final BitSet FOLLOW_20_in_block68 = new BitSet(new long[]{0x0000000000002000L});
     public static final BitSet FOLLOW_model_in_block78 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_21_in_block85 = new BitSet(new long[]{0x00000000007FFFF2L});
     public static final BitSet FOLLOW_13_in_model111 = new BitSet(new long[]{0x0000000000005000L});
     public static final BitSet FOLLOW_12_in_model116 = new BitSet(new long[]{0x0000000000000020L});
     public static final BitSet FOLLOW_IDENT_in_model122 = new BitSet(new long[]{0x0000000000000400L});
     public static final BitSet FOLLOW_10_in_model125 = new BitSet(new long[]{0x0000000000004020L});
     public static final BitSet FOLLOW_IDENT_in_model127 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_9_in_model129 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_TYPE_in_model132 = new BitSet(new long[]{0x0000000000004020L});
     public static final BitSet FOLLOW_14_in_model135 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_TYPE_in_model137 = new BitSet(new long[]{0x0000000000000050L});
     public static final BitSet FOLLOW_value_in_model143 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model145 = new BitSet(new long[]{0x0000000000005000L});
     public static final BitSet FOLLOW_12_in_model170 = new BitSet(new long[]{0x0000000000000020L});
     public static final BitSet FOLLOW_IDENT_in_model177 = new BitSet(new long[]{0x0000000000000400L});
     public static final BitSet FOLLOW_10_in_model179 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model181 = new BitSet(new long[]{0x0000000000000800L});
     public static final BitSet FOLLOW_11_in_model195 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_TYPE_in_model197 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_TYPE_in_model203 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model206 = new BitSet(new long[]{0x0000000000000400L});
     public static final BitSet FOLLOW_10_in_model210 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_16_in_model212 = new BitSet(new long[]{0x0000000000040000L});
     public static final BitSet FOLLOW_18_in_model214 = new BitSet(new long[]{0x0000000000000020L});
     public static final BitSet FOLLOW_IDENT_in_model222 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_9_in_model224 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_INT_in_model226 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model228 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model229 = new BitSet(new long[]{0x0000000000005000L});
     public static final BitSet FOLLOW_12_in_model239 = new BitSet(new long[]{0x0000000000000020L});
     public static final BitSet FOLLOW_IDENT_in_model246 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_9_in_model248 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_INT_in_model250 = new BitSet(new long[]{0x0000000000000400L});
     public static final BitSet FOLLOW_10_in_model253 = new BitSet(new long[]{0x0000000000000400L});
     public static final BitSet FOLLOW_10_in_model255 = new BitSet(new long[]{0x0000000000000020L});
     public static final BitSet FOLLOW_IDENT_in_model256 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_9_in_model258 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_INT_in_model260 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_TYPE_in_model262 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model263 = new BitSet(new long[]{0x0000000000004400L});
     public static final BitSet FOLLOW_14_in_model266 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_TYPE_in_model268 = new BitSet(new long[]{0x0000000000004450L});
     public static final BitSet FOLLOW_10_in_model279 = new BitSet(new long[]{0x0000000000080000L});
     public static final BitSet FOLLOW_ite_in_model284 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model288 = new BitSet(new long[]{0x0000000000004050L});
     public static final BitSet FOLLOW_value_in_model296 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_model300 = new BitSet(new long[]{0x0000000000005000L});
     public static final BitSet FOLLOW_14_in_model306 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_19_in_ite326 = new BitSet(new long[]{0x0000000000000400L});
     public static final BitSet FOLLOW_10_in_ite327 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_ite328 = new BitSet(new long[]{0x0000000000000020L});
     public static final BitSet FOLLOW_IDENT_in_ite330 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_9_in_ite332 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_INT_in_ite334 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_INT_in_ite340 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_ite341 = new BitSet(new long[]{0x0000000000000050L});
     public static final BitSet FOLLOW_value_in_ite348 = new BitSet(new long[]{0x0000000000000450L});
     public static final BitSet FOLLOW_value_in_ite369 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_10_in_ite373 = new BitSet(new long[]{0x0000000000080000L});
     public static final BitSet FOLLOW_ite_in_ite376 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_ite378 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_19_in_ite385 = new BitSet(new long[]{0x0000000000000400L});
     public static final BitSet FOLLOW_10_in_ite386 = new BitSet(new long[]{0x0000000000020000L});
     public static final BitSet FOLLOW_17_in_ite387 = new BitSet(new long[]{0x0000000000000400L});
     public static final BitSet FOLLOW_10_in_ite389 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_ite390 = new BitSet(new long[]{0x0000000000000020L});
     public static final BitSet FOLLOW_IDENT_in_ite392 = new BitSet(new long[]{0x0000000000000200L});
     public static final BitSet FOLLOW_9_in_ite394 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_INT_in_ite396 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_INT_in_ite402 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_ite403 = new BitSet(new long[]{0x0000000000004400L});
     public static final BitSet FOLLOW_14_in_ite407 = new BitSet(new long[]{0x0000000000000050L});
     public static final BitSet FOLLOW_value_in_ite419 = new BitSet(new long[]{0x0000000000000450L});
     public static final BitSet FOLLOW_value_in_ite423 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_10_in_ite427 = new BitSet(new long[]{0x0000000000080000L});
     public static final BitSet FOLLOW_ite_in_ite430 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_ite432 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_INT_in_value449 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_BOOL_in_value456 = new BitSet(new long[]{0x0000000000000002L});
 
 }
