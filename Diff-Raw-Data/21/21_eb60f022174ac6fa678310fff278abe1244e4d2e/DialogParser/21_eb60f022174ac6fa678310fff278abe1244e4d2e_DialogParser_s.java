// $ANTLR 3.4 Dialog.g 2013-01-26 17:22:47
 
 package net.citizensnpcs.adventures.dialog;
 import java.util.Collection;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import com.google.common.collect.Maps;
 import com.google.common.base.Predicates;
 import net.citizensnpcs.adventures.dialog.*;
 import net.citizensnpcs.adventures.dialog.statements.*;
 import net.citizensnpcs.adventures.dialog.evaluators.*;
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class DialogParser extends Parser {
     public static final String[] tokenNames = new String[] {
         "<invalid>", "<EOR>", "<DOWN>", "<UP>", "BOOLEAN_LITERAL", "DIGIT", "IDENT", "INTEGER", "LETTER", "LINE_COMMENT", "ML_COMMENT", "NEWLINE", "NUMBER", "QUERY_STRING", "STRING_LITERAL", "WS", "'!='", "'%'", "'('", "')'", "'*'", "'**'", "'+'", "','", "'-'", "'-p'", "'/'", "';'", "'<'", "'<='", "'='", "'>'", "'>='", "'criteria'", "'d'", "'events='", "'h'", "'log'", "'m'", "'ms'", "'ns'", "'remember'", "'response'", "'rule'", "'s'", "'then'", "'us'", "'{'", "'}'"
     };
 
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
 
     // delegates
     public Parser[] getDelegates() {
         return new Parser[] {};
     }
 
     // delegators
 
 
     public DialogParser(TokenStream input) {
         this(input, new RecognizerSharedState());
     }
     public DialogParser(TokenStream input, RecognizerSharedState state) {
         super(input, state);
     }
 
     public String[] getTokenNames() { return DialogParser.tokenNames; }
     public String getGrammarFileName() { return "Dialog.g"; }
 
 
     /*  @Override
         public void reportError(RecognitionException e) {
             throw e;
         }
     */
     private VariableSource variableSource;
 
     public void setVariableSource(VariableSource source) {
         this.variableSource = source;
     }
 
 
 
     // $ANTLR start "program"
     // Dialog.g:50:1: program[DialogEngine.ParseContext context] : ( rule[context] | response[context] )* ;
     public final void program(DialogEngine.ParseContext context) throws RecognitionException {
         DialogParser.rule_return rule1 =null;
 
         Response response2 =null;
 
 
         try {
             // Dialog.g:50:45: ( ( rule[context] | response[context] )* )
             // Dialog.g:51:5: ( rule[context] | response[context] )*
             {
             // Dialog.g:51:5: ( rule[context] | response[context] )*
             loop1:
             do {
                 int alt1=3;
                 int LA1_0 = input.LA(1);
 
                 if ( (LA1_0==43) ) {
                     alt1=1;
                 }
                 else if ( (LA1_0==42) ) {
                     alt1=2;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // Dialog.g:52:9: rule[context]
             	    {
             	    pushFollow(FOLLOW_rule_in_program81);
             	    rule1=rule(context);
 
             	    state._fsp--;
 
 
             	     context.ruleLoaded((rule1!=null?rule1.eventNames:null), (rule1!=null?rule1.rule:null)); 
 
             	    }
             	    break;
             	case 2 :
             	    // Dialog.g:53:11: response[context]
             	    {
             	    pushFollow(FOLLOW_response_in_program96);
             	    response2=response(context);
 
             	    state._fsp--;
 
 
             	     context.responseLoaded(response2); 
 
             	    }
             	    break;
 
             	default :
             	    break loop1;
                 }
             } while (true);
 
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return ;
     }
     // $ANTLR end "program"
 
 
 
     // $ANTLR start "name"
     // Dialog.g:56:1: name[DialogEngine.ParseContext context] returns [String name] : (i1= IDENT |i2= IDENT '/' i3= IDENT ) ;
     public final String name(DialogEngine.ParseContext context) throws RecognitionException {
         String name = null;
 
 
         Token i1=null;
         Token i2=null;
         Token i3=null;
 
         try {
             // Dialog.g:56:64: ( (i1= IDENT |i2= IDENT '/' i3= IDENT ) )
             // Dialog.g:57:5: (i1= IDENT |i2= IDENT '/' i3= IDENT )
             {
             // Dialog.g:57:5: (i1= IDENT |i2= IDENT '/' i3= IDENT )
             int alt2=2;
             int LA2_0 = input.LA(1);
 
             if ( (LA2_0==IDENT) ) {
                 int LA2_1 = input.LA(2);
 
                 if ( (LA2_1==26) ) {
                     alt2=2;
                 }
                 else if ( (LA2_1==27||LA2_1==45||LA2_1==47) ) {
                     alt2=1;
                 }
                 else {
                     NoViableAltException nvae =
                         new NoViableAltException("", 2, 1, input);
 
                     throw nvae;
 
                 }
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 2, 0, input);
 
                 throw nvae;
 
             }
             switch (alt2) {
                 case 1 :
                     // Dialog.g:58:9: i1= IDENT
                     {
                     i1=(Token)match(input,IDENT,FOLLOW_IDENT_in_name136); 
 
                      name = context.disambiguateName((i1!=null?i1.getText():null)); 
 
                     }
                     break;
                 case 2 :
                     // Dialog.g:59:11: i2= IDENT '/' i3= IDENT
                     {
                     i2=(Token)match(input,IDENT,FOLLOW_IDENT_in_name153); 
 
                     match(input,26,FOLLOW_26_in_name155); 
 
                     i3=(Token)match(input,IDENT,FOLLOW_IDENT_in_name159); 
 
                      name = (i2!=null?i2.getText():null) + '/' + (i3!=null?i3.getText():null); 
 
                     }
                     break;
 
             }
 
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return name;
     }
     // $ANTLR end "name"
 
 
 
     // $ANTLR start "response"
     // Dialog.g:63:1: response[DialogEngine.ParseContext context] returns [Response response] : 'response' name[context] '{' ( response_statement[context, builder] ';' )* '}' ;
     public final Response response(DialogEngine.ParseContext context) throws RecognitionException {
         Response response = null;
 
 
         String name3 =null;
 
 
         try {
             // Dialog.g:63:74: ( 'response' name[context] '{' ( response_statement[context, builder] ';' )* '}' )
             // Dialog.g:64:5: 'response' name[context] '{' ( response_statement[context, builder] ';' )* '}'
             {
              Response.Builder builder = Response.builder(); 
 
             match(input,42,FOLLOW_42_in_response197); 
 
             pushFollow(FOLLOW_name_in_response199);
             name3=name(context);
 
             state._fsp--;
 
 
              builder.name(name3); 
 
             match(input,47,FOLLOW_47_in_response204); 
 
             // Dialog.g:65:64: ( response_statement[context, builder] ';' )*
             loop3:
             do {
                 int alt3=2;
                 int LA3_0 = input.LA(1);
 
                 if ( (LA3_0==IDENT||LA3_0==37||LA3_0==41) ) {
                     alt3=1;
                 }
 
 
                 switch (alt3) {
             	case 1 :
             	    // Dialog.g:65:65: response_statement[context, builder] ';'
             	    {
             	    pushFollow(FOLLOW_response_statement_in_response207);
             	    response_statement(context, builder);
 
             	    state._fsp--;
 
 
             	    match(input,27,FOLLOW_27_in_response210); 
 
             	    }
             	    break;
 
             	default :
             	    break loop3;
                 }
             } while (true);
 
 
             match(input,48,FOLLOW_48_in_response214); 
 
              response = builder.build(); 
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return response;
     }
     // $ANTLR end "response"
 
 
 
     // $ANTLR start "response_statement"
     // Dialog.g:69:1: response_statement[DialogEngine.ParseContext context, Response.Builder builder] : ( 'log' expression | remember_statement | generic_statement[context] );
     public final void response_statement(DialogEngine.ParseContext context, Response.Builder builder) throws RecognitionException {
         Evaluator expression4 =null;
 
         QueryRunnable remember_statement5 =null;
 
         QueryRunnable generic_statement6 =null;
 
 
         try {
             // Dialog.g:69:82: ( 'log' expression | remember_statement | generic_statement[context] )
             int alt4=3;
             switch ( input.LA(1) ) {
             case 37:
                 {
                 alt4=1;
                 }
                 break;
             case 41:
                 {
                 alt4=2;
                 }
                 break;
             case IDENT:
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
                     // Dialog.g:70:5: 'log' expression
                     {
                     match(input,37,FOLLOW_37_in_response_statement239); 
 
                     pushFollow(FOLLOW_expression_in_response_statement241);
                     expression4=expression();
 
                     state._fsp--;
 
 
                      builder.statement(Log.logging(expression4)); 
 
                     }
                     break;
                 case 2 :
                     // Dialog.g:71:7: remember_statement
                     {
                     pushFollow(FOLLOW_remember_statement_in_response_statement251);
                     remember_statement5=remember_statement();
 
                     state._fsp--;
 
 
                      builder.statement(remember_statement5); 
 
                     }
                     break;
                 case 3 :
                     // Dialog.g:72:7: generic_statement[context]
                     {
                     pushFollow(FOLLOW_generic_statement_in_response_statement261);
                     generic_statement6=generic_statement(context);
 
                     state._fsp--;
 
 
                      builder.statement(generic_statement6); 
 
                     }
                     break;
 
             }
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return ;
     }
     // $ANTLR end "response_statement"
 
 
     public static class rule_return extends ParserRuleReturnScope {
         public Collection<String> eventNames;
         public Rule rule;
     };
 
 
     // $ANTLR start "rule"
     // Dialog.g:75:1: rule[DialogEngine.ParseContext context] returns [Collection<String> eventNames, Rule rule] : 'rule' name[context] '{' criteria ';' ( rule_statement[context, builder] ';' )* '}' ;
     public final DialogParser.rule_return rule(DialogEngine.ParseContext context) throws RecognitionException {
         DialogParser.rule_return retval = new DialogParser.rule_return();
         retval.start = input.LT(1);
 
 
         String name7 =null;
 
         Collection<String> criteria8 =null;
 
 
         try {
             // Dialog.g:75:92: ( 'rule' name[context] '{' criteria ';' ( rule_statement[context, builder] ';' )* '}' )
             // Dialog.g:76:5: 'rule' name[context] '{' criteria ';' ( rule_statement[context, builder] ';' )* '}'
             {
              Rule.Builder builder = Rule.builder(); 
 
             match(input,43,FOLLOW_43_in_rule292); 
 
             pushFollow(FOLLOW_name_in_rule294);
             name7=name(context);
 
             state._fsp--;
 
 
              builder.name(name7); 
 
             match(input,47,FOLLOW_47_in_rule299); 
 
             pushFollow(FOLLOW_criteria_in_rule301);
             criteria8=criteria();
 
             state._fsp--;
 
 
              retval.eventNames = criteria8; 
 
             match(input,27,FOLLOW_27_in_rule305); 
 
             // Dialog.g:77:113: ( rule_statement[context, builder] ';' )*
             loop5:
             do {
                 int alt5=2;
                 int LA5_0 = input.LA(1);
 
                 if ( (LA5_0==IDENT||(LA5_0 >= 41 && LA5_0 <= 42)) ) {
                     alt5=1;
                 }
 
 
                 switch (alt5) {
             	case 1 :
             	    // Dialog.g:77:114: rule_statement[context, builder] ';'
             	    {
             	    pushFollow(FOLLOW_rule_statement_in_rule308);
             	    rule_statement(context, builder);
 
             	    state._fsp--;
 
 
             	    match(input,27,FOLLOW_27_in_rule311); 
 
             	    }
             	    break;
 
             	default :
             	    break loop5;
                 }
             } while (true);
 
 
             match(input,48,FOLLOW_48_in_rule315); 
 
              retval.rule = builder.build(); 
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "rule"
 
 
 
     // $ANTLR start "rule_statement"
     // Dialog.g:81:1: rule_statement[DialogEngine.ParseContext context, Rule.Builder builder] : ( ( 'response' n= name[context] ( 'then' (target= IDENT |target= NUMBER ) event= IDENT )? ) | remember_statement | generic_statement[context] );
     public final void rule_statement(DialogEngine.ParseContext context, Rule.Builder builder) throws RecognitionException {
         Token target=null;
         Token event=null;
         String n =null;
 
         QueryRunnable remember_statement9 =null;
 
         QueryRunnable generic_statement10 =null;
 
 
         try {
             // Dialog.g:81:74: ( ( 'response' n= name[context] ( 'then' (target= IDENT |target= NUMBER ) event= IDENT )? ) | remember_statement | generic_statement[context] )
             int alt8=3;
             switch ( input.LA(1) ) {
             case 42:
                 {
                 alt8=1;
                 }
                 break;
             case 41:
                 {
                 alt8=2;
                 }
                 break;
             case IDENT:
                 {
                 alt8=3;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 8, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt8) {
                 case 1 :
                     // Dialog.g:82:5: ( 'response' n= name[context] ( 'then' (target= IDENT |target= NUMBER ) event= IDENT )? )
                     {
                     // Dialog.g:82:5: ( 'response' n= name[context] ( 'then' (target= IDENT |target= NUMBER ) event= IDENT )? )
                     // Dialog.g:82:6: 'response' n= name[context] ( 'then' (target= IDENT |target= NUMBER ) event= IDENT )?
                     {
                     match(input,42,FOLLOW_42_in_rule_statement345); 
 
                     pushFollow(FOLLOW_name_in_rule_statement349);
                     n=name(context);
 
                     state._fsp--;
 
 
                      CallResponse.Builder responseBuilder = CallResponse.builder(n); 
 
                     // Dialog.g:83:9: ( 'then' (target= IDENT |target= NUMBER ) event= IDENT )?
                     int alt7=2;
                     int LA7_0 = input.LA(1);
 
                     if ( (LA7_0==45) ) {
                         alt7=1;
                     }
                     switch (alt7) {
                         case 1 :
                             // Dialog.g:85:13: 'then' (target= IDENT |target= NUMBER ) event= IDENT
                             {
                             match(input,45,FOLLOW_45_in_rule_statement390); 
 
                             // Dialog.g:85:20: (target= IDENT |target= NUMBER )
                             int alt6=2;
                             int LA6_0 = input.LA(1);
 
                             if ( (LA6_0==IDENT) ) {
                                 alt6=1;
                             }
                             else if ( (LA6_0==NUMBER) ) {
                                 alt6=2;
                             }
                             else {
                                 NoViableAltException nvae =
                                     new NoViableAltException("", 6, 0, input);
 
                                 throw nvae;
 
                             }
                             switch (alt6) {
                                 case 1 :
                                     // Dialog.g:85:21: target= IDENT
                                     {
                                     target=(Token)match(input,IDENT,FOLLOW_IDENT_in_rule_statement395); 
 
                                     }
                                     break;
                                 case 2 :
                                     // Dialog.g:85:36: target= NUMBER
                                     {
                                     target=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_rule_statement401); 
 
                                     }
                                     break;
 
                             }
 
 
                             event=(Token)match(input,IDENT,FOLLOW_IDENT_in_rule_statement406); 
 
                              responseBuilder.callback(new CallEventCallback((target!=null?target.getText():null), (event!=null?event.getText():null))); 
 
                             }
                             break;
 
                     }
 
 
                      builder.statement(responseBuilder.build()); 
 
                     }
 
 
                     }
                     break;
                 case 2 :
                     // Dialog.g:89:7: remember_statement
                     {
                     pushFollow(FOLLOW_remember_statement_in_rule_statement444);
                     remember_statement9=remember_statement();
 
                     state._fsp--;
 
 
                      builder.statement(remember_statement9); 
 
                     }
                     break;
                 case 3 :
                     // Dialog.g:90:7: generic_statement[context]
                     {
                     pushFollow(FOLLOW_generic_statement_in_rule_statement454);
                     generic_statement10=generic_statement(context);
 
                     state._fsp--;
 
 
                      builder.statement(generic_statement10); 
 
                     }
                     break;
 
             }
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return ;
     }
     // $ANTLR end "rule_statement"
 
 
 
     // $ANTLR start "generic_statement"
     // Dialog.g:93:1: generic_statement[DialogEngine.ParseContext context] returns [QueryRunnable statement] :i1= IDENT (i2= IDENT '=' expression )* {...}?;
     public final QueryRunnable generic_statement(DialogEngine.ParseContext context) throws RecognitionException {
         QueryRunnable statement = null;
 
 
         Token i1=null;
         Token i2=null;
         Evaluator expression11 =null;
 
 
         try {
             // Dialog.g:93:89: (i1= IDENT (i2= IDENT '=' expression )* {...}?)
             // Dialog.g:94:5: i1= IDENT (i2= IDENT '=' expression )* {...}?
             {
              String name; Map<String,Evaluator> vars = Maps.newHashMap(); 
 
             i1=(Token)match(input,IDENT,FOLLOW_IDENT_in_generic_statement488); 
 
              name = (i1!=null?i1.getText():null); 
 
             // Dialog.g:95:35: (i2= IDENT '=' expression )*
             loop9:
             do {
                 int alt9=2;
                 int LA9_0 = input.LA(1);
 
                 if ( (LA9_0==IDENT) ) {
                     alt9=1;
                 }
 
 
                 switch (alt9) {
             	case 1 :
             	    // Dialog.g:95:36: i2= IDENT '=' expression
             	    {
             	    i2=(Token)match(input,IDENT,FOLLOW_IDENT_in_generic_statement495); 
 
             	    match(input,30,FOLLOW_30_in_generic_statement497); 
 
             	    pushFollow(FOLLOW_expression_in_generic_statement499);
             	    expression11=expression();
 
             	    state._fsp--;
 
 
             	     vars.put((i2!=null?i2.getText():null), expression11); 
 
             	    }
             	    break;
 
             	default :
             	    break loop9;
                 }
             } while (true);
 
 
              statement = context.buildStatement(name, vars); 
 
             if ( !(( statement != null )) ) {
                 throw new FailedPredicateException(input, "generic_statement", " $statement != null ");
             }
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return statement;
     }
     // $ANTLR end "generic_statement"
 
 
 
     // $ANTLR start "remember_statement"
     // Dialog.g:98:1: remember_statement returns [QueryRunnable statement] : 'remember' remember_assignment[builder] ( ',' remember_assignment[builder] )* ;
     public final QueryRunnable remember_statement() throws RecognitionException {
         QueryRunnable statement = null;
 
 
         try {
             // Dialog.g:98:54: ( 'remember' remember_assignment[builder] ( ',' remember_assignment[builder] )* )
             // Dialog.g:99:5: 'remember' remember_assignment[builder] ( ',' remember_assignment[builder] )*
             {
              Remember.Builder builder = Remember.builder(); 
 
             match(input,41,FOLLOW_41_in_remember_statement538); 
 
             pushFollow(FOLLOW_remember_assignment_in_remember_statement540);
             remember_assignment(builder);
 
             state._fsp--;
 
 
             // Dialog.g:100:45: ( ',' remember_assignment[builder] )*
             loop10:
             do {
                 int alt10=2;
                 int LA10_0 = input.LA(1);
 
                 if ( (LA10_0==23) ) {
                     alt10=1;
                 }
 
 
                 switch (alt10) {
             	case 1 :
             	    // Dialog.g:100:46: ',' remember_assignment[builder]
             	    {
             	    match(input,23,FOLLOW_23_in_remember_statement544); 
 
             	    pushFollow(FOLLOW_remember_assignment_in_remember_statement546);
             	    remember_assignment(builder);
 
             	    state._fsp--;
 
 
             	    }
             	    break;
 
             	default :
             	    break loop10;
                 }
             } while (true);
 
 
              statement = builder.build(); 
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return statement;
     }
     // $ANTLR end "remember_statement"
 
 
 
     // $ANTLR start "remember_assignment"
     // Dialog.g:104:1: remember_assignment[Remember.Builder builder] : QUERY_STRING '=' expression (i1= INTEGER i2= time_unit )? ( '-p' )? ;
     public final void remember_assignment(Remember.Builder builder) throws RecognitionException {
         Token i1=null;
         Token QUERY_STRING12=null;
         TimeUnit i2 =null;
 
         Evaluator expression13 =null;
 
 
         try {
             // Dialog.g:104:48: ( QUERY_STRING '=' expression (i1= INTEGER i2= time_unit )? ( '-p' )? )
             // Dialog.g:105:5: QUERY_STRING '=' expression (i1= INTEGER i2= time_unit )? ( '-p' )?
             {
              boolean isPersistent = false; long expiration = Long.MAX_VALUE; TimeUnit unit = TimeUnit.DAYS; 
 
             QUERY_STRING12=(Token)match(input,QUERY_STRING,FOLLOW_QUERY_STRING_in_remember_assignment580); 
 
             match(input,30,FOLLOW_30_in_remember_assignment582); 
 
             pushFollow(FOLLOW_expression_in_remember_assignment584);
             expression13=expression();
 
             state._fsp--;
 
 
             // Dialog.g:107:9: (i1= INTEGER i2= time_unit )?
             int alt11=2;
             int LA11_0 = input.LA(1);
 
             if ( (LA11_0==INTEGER) ) {
                 alt11=1;
             }
             switch (alt11) {
                 case 1 :
                     // Dialog.g:108:13: i1= INTEGER i2= time_unit
                     {
                     i1=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_remember_assignment611); 
 
                     pushFollow(FOLLOW_time_unit_in_remember_assignment615);
                     i2=time_unit();
 
                     state._fsp--;
 
 
                      expiration = Long.parseLong((i1!=null?i1.getText():null)); unit = i2; 
 
                     }
                     break;
 
             }
 
 
             // Dialog.g:110:9: ( '-p' )?
             int alt12=2;
             int LA12_0 = input.LA(1);
 
             if ( (LA12_0==25) ) {
                 alt12=1;
             }
             switch (alt12) {
                 case 1 :
                     // Dialog.g:111:13: '-p'
                     {
                     match(input,25,FOLLOW_25_in_remember_assignment653); 
 
                      isPersistent = true; 
 
                     }
                     break;
 
             }
 
 
              builder.remember((QUERY_STRING12!=null?QUERY_STRING12.getText():null), expression13, ExpirationTime.expiringAt(expiration, unit, isPersistent)); 
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return ;
     }
     // $ANTLR end "remember_assignment"
 
 
 
     // $ANTLR start "criteria"
     // Dialog.g:116:1: criteria returns [Collection<String> eventNames] : 'criteria' ( 'events=' e1= IDENT ( ',' e2= IDENT )* ) (i1= IDENT '=' op1= expression |i2= IDENT '>' op2= expression |i3= IDENT '<' op3= expression |i4= IDENT '<=' op4= expression |i5= IDENT '>=' op5= expression |i6= IDENT '!=' op6= expression |i7= IDENT )* ;
     public final Collection<String> criteria() throws RecognitionException {
         Collection<String> eventNames = null;
 
 
         Token e1=null;
         Token e2=null;
         Token i1=null;
         Token i2=null;
         Token i3=null;
         Token i4=null;
         Token i5=null;
         Token i6=null;
         Token i7=null;
         Evaluator op1 =null;
 
         Evaluator op2 =null;
 
         Evaluator op3 =null;
 
         Evaluator op4 =null;
 
         Evaluator op5 =null;
 
         Evaluator op6 =null;
 
 
         try {
             // Dialog.g:116:49: ( 'criteria' ( 'events=' e1= IDENT ( ',' e2= IDENT )* ) (i1= IDENT '=' op1= expression |i2= IDENT '>' op2= expression |i3= IDENT '<' op3= expression |i4= IDENT '<=' op4= expression |i5= IDENT '>=' op5= expression |i6= IDENT '!=' op6= expression |i7= IDENT )* )
             // Dialog.g:117:5: 'criteria' ( 'events=' e1= IDENT ( ',' e2= IDENT )* ) (i1= IDENT '=' op1= expression |i2= IDENT '>' op2= expression |i3= IDENT '<' op3= expression |i4= IDENT '<=' op4= expression |i5= IDENT '>=' op5= expression |i6= IDENT '!=' op6= expression |i7= IDENT )*
             {
              eventNames = new ArrayList<String>(); 
 
             match(input,33,FOLLOW_33_in_criteria708); 
 
             // Dialog.g:118:16: ( 'events=' e1= IDENT ( ',' e2= IDENT )* )
             // Dialog.g:119:21: 'events=' e1= IDENT ( ',' e2= IDENT )*
             {
             match(input,35,FOLLOW_35_in_criteria732); 
 
             e1=(Token)match(input,IDENT,FOLLOW_IDENT_in_criteria736); 
 
              eventNames.add((e1!=null?e1.getText():null)); 
 
             // Dialog.g:119:72: ( ',' e2= IDENT )*
             loop13:
             do {
                 int alt13=2;
                 int LA13_0 = input.LA(1);
 
                 if ( (LA13_0==23) ) {
                     alt13=1;
                 }
 
 
                 switch (alt13) {
             	case 1 :
             	    // Dialog.g:119:73: ',' e2= IDENT
             	    {
             	    match(input,23,FOLLOW_23_in_criteria742); 
 
             	    e2=(Token)match(input,IDENT,FOLLOW_IDENT_in_criteria746); 
 
             	     eventNames.add((e2!=null?e2.getText():null)); 
 
             	    }
             	    break;
 
             	default :
             	    break loop13;
                 }
             } while (true);
 
 
             }
 
 
             // Dialog.g:121:5: (i1= IDENT '=' op1= expression |i2= IDENT '>' op2= expression |i3= IDENT '<' op3= expression |i4= IDENT '<=' op4= expression |i5= IDENT '>=' op5= expression |i6= IDENT '!=' op6= expression |i7= IDENT )*
             loop14:
             do {
                 int alt14=8;
                 int LA14_0 = input.LA(1);
 
                 if ( (LA14_0==IDENT) ) {
                     switch ( input.LA(2) ) {
                     case 30:
                         {
                         alt14=1;
                         }
                         break;
                     case 31:
                         {
                         alt14=2;
                         }
                         break;
                     case 28:
                         {
                         alt14=3;
                         }
                         break;
                     case 29:
                         {
                         alt14=4;
                         }
                         break;
                     case 32:
                         {
                         alt14=5;
                         }
                         break;
                     case 16:
                         {
                         alt14=6;
                         }
                         break;
                     case IDENT:
                     case 27:
                         {
                         alt14=7;
                         }
                         break;
 
                     }
 
                 }
 
 
                 switch (alt14) {
             	case 1 :
             	    // Dialog.g:122:9: i1= IDENT '=' op1= expression
             	    {
             	    i1=(Token)match(input,IDENT,FOLLOW_IDENT_in_criteria786); 
 
             	    match(input,30,FOLLOW_30_in_criteria788); 
 
             	    pushFollow(FOLLOW_expression_in_criteria792);
             	    op1=expression();
 
             	    state._fsp--;
 
 
             	     NumberQueryPredicate.equalTo((i1!=null?i1.getText():null), op1); 
 
             	    }
             	    break;
             	case 2 :
             	    // Dialog.g:123:11: i2= IDENT '>' op2= expression
             	    {
             	    i2=(Token)match(input,IDENT,FOLLOW_IDENT_in_criteria808); 
 
             	    match(input,31,FOLLOW_31_in_criteria810); 
 
             	    pushFollow(FOLLOW_expression_in_criteria814);
             	    op2=expression();
 
             	    state._fsp--;
 
 
             	     NumberQueryPredicate.greaterThan((i2!=null?i2.getText():null), op2); 
 
             	    }
             	    break;
             	case 3 :
             	    // Dialog.g:124:11: i3= IDENT '<' op3= expression
             	    {
             	    i3=(Token)match(input,IDENT,FOLLOW_IDENT_in_criteria830); 
 
             	    match(input,28,FOLLOW_28_in_criteria832); 
 
             	    pushFollow(FOLLOW_expression_in_criteria836);
             	    op3=expression();
 
             	    state._fsp--;
 
 
             	     NumberQueryPredicate.lessThan((i3!=null?i3.getText():null), op3); 
 
             	    }
             	    break;
             	case 4 :
             	    // Dialog.g:125:11: i4= IDENT '<=' op4= expression
             	    {
             	    i4=(Token)match(input,IDENT,FOLLOW_IDENT_in_criteria852); 
 
             	    match(input,29,FOLLOW_29_in_criteria854); 
 
             	    pushFollow(FOLLOW_expression_in_criteria858);
             	    op4=expression();
 
             	    state._fsp--;
 
 
             	     NumberQueryPredicate.lessThanOrEqual((i4!=null?i4.getText():null), op4); 
 
             	    }
             	    break;
             	case 5 :
             	    // Dialog.g:126:11: i5= IDENT '>=' op5= expression
             	    {
             	    i5=(Token)match(input,IDENT,FOLLOW_IDENT_in_criteria874); 
 
             	    match(input,32,FOLLOW_32_in_criteria876); 
 
             	    pushFollow(FOLLOW_expression_in_criteria880);
             	    op5=expression();
 
             	    state._fsp--;
 
 
             	     NumberQueryPredicate.greaterThanOrEqual((i5!=null?i5.getText():null), op5); 
 
             	    }
             	    break;
             	case 6 :
             	    // Dialog.g:127:11: i6= IDENT '!=' op6= expression
             	    {
             	    i6=(Token)match(input,IDENT,FOLLOW_IDENT_in_criteria896); 
 
             	    match(input,16,FOLLOW_16_in_criteria898); 
 
             	    pushFollow(FOLLOW_expression_in_criteria902);
             	    op6=expression();
 
             	    state._fsp--;
 
 
             	     NumberQueryPredicate.not((i6!=null?i6.getText():null), op6);
 
             	    }
             	    break;
             	case 7 :
             	    // Dialog.g:128:11: i7= IDENT
             	    {
             	    i7=(Token)match(input,IDENT,FOLLOW_IDENT_in_criteria918); 
 
             	     NumberQueryPredicate.of((i7!=null?i7.getText():null), Predicates.<Number>alwaysTrue()); 
 
             	    }
             	    break;
 
             	default :
             	    break loop14;
                 }
             } while (true);
 
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return eventNames;
     }
     // $ANTLR end "criteria"
 
 
 
     // $ANTLR start "expression"
     // Dialog.g:131:1: expression returns [Evaluator value] : op1= mult ( '+' op2= mult | '-' op2= mult )* ;
     public final Evaluator expression() throws RecognitionException {
         Evaluator value = null;
 
 
         Evaluator op1 =null;
 
         Evaluator op2 =null;
 
 
         try {
             // Dialog.g:131:38: (op1= mult ( '+' op2= mult | '-' op2= mult )* )
             // Dialog.g:132:5: op1= mult ( '+' op2= mult | '-' op2= mult )*
             {
             pushFollow(FOLLOW_mult_in_expression949);
             op1=mult();
 
             state._fsp--;
 
 
              value = op1; 
 
             // Dialog.g:133:5: ( '+' op2= mult | '-' op2= mult )*
             loop15:
             do {
                 int alt15=3;
                 int LA15_0 = input.LA(1);
 
                 if ( (LA15_0==22) ) {
                     alt15=1;
                 }
                 else if ( (LA15_0==24) ) {
                     alt15=2;
                 }
 
 
                 switch (alt15) {
             	case 1 :
             	    // Dialog.g:134:5: '+' op2= mult
             	    {
             	    match(input,22,FOLLOW_22_in_expression963); 
 
             	    pushFollow(FOLLOW_mult_in_expression967);
             	    op2=mult();
 
             	    state._fsp--;
 
 
             	     value = PlusEvaluator.create(value, op2); 
 
             	    }
             	    break;
             	case 2 :
             	    // Dialog.g:135:8: '-' op2= mult
             	    {
             	    match(input,24,FOLLOW_24_in_expression978); 
 
             	    pushFollow(FOLLOW_mult_in_expression982);
             	    op2=mult();
 
             	    state._fsp--;
 
 
             	     value = MinusEvaluator.create(value, op2); 
 
             	    }
             	    break;
 
             	default :
             	    break loop15;
                 }
             } while (true);
 
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return value;
     }
     // $ANTLR end "expression"
 
 
 
     // $ANTLR start "unary"
     // Dialog.g:138:1: unary returns [Evaluator value] : ( '+' | '-' )* term ;
     public final Evaluator unary() throws RecognitionException {
         Evaluator value = null;
 
 
         Evaluator term14 =null;
 
 
         try {
             // Dialog.g:138:33: ( ( '+' | '-' )* term )
             // Dialog.g:139:5: ( '+' | '-' )* term
             {
              boolean positive = true; 
 
             // Dialog.g:140:5: ( '+' | '-' )*
             loop16:
             do {
                 int alt16=3;
                 int LA16_0 = input.LA(1);
 
                 if ( (LA16_0==22) ) {
                     alt16=1;
                 }
                 else if ( (LA16_0==24) ) {
                     alt16=2;
                 }
 
 
                 switch (alt16) {
             	case 1 :
             	    // Dialog.g:140:6: '+'
             	    {
             	    match(input,22,FOLLOW_22_in_unary1014); 
 
             	    }
             	    break;
             	case 2 :
             	    // Dialog.g:140:12: '-'
             	    {
             	    match(input,24,FOLLOW_24_in_unary1018); 
 
             	     positive = !positive; 
 
             	    }
             	    break;
 
             	default :
             	    break loop16;
                 }
             } while (true);
 
 
             pushFollow(FOLLOW_term_in_unary1028);
             term14=term();
 
             state._fsp--;
 
 
 
                      value = term14;
                      if (!positive)
                      	value = NegationEvaluator.create(value);
                      
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return value;
     }
     // $ANTLR end "unary"
 
 
 
     // $ANTLR start "mult"
     // Dialog.g:147:1: mult returns [Evaluator value] : op1= unary ( '*' op2= unary | '/' op2= unary | '%' op2= unary | '**' op2= unary )* ;
     public final Evaluator mult() throws RecognitionException {
         Evaluator value = null;
 
 
         Evaluator op1 =null;
 
         Evaluator op2 =null;
 
 
         try {
             // Dialog.g:147:32: (op1= unary ( '*' op2= unary | '/' op2= unary | '%' op2= unary | '**' op2= unary )* )
             // Dialog.g:148:5: op1= unary ( '*' op2= unary | '/' op2= unary | '%' op2= unary | '**' op2= unary )*
             {
             pushFollow(FOLLOW_unary_in_mult1048);
             op1=unary();
 
             state._fsp--;
 
 
              value = op1; 
 
             // Dialog.g:149:5: ( '*' op2= unary | '/' op2= unary | '%' op2= unary | '**' op2= unary )*
             loop17:
             do {
                 int alt17=5;
                 switch ( input.LA(1) ) {
                 case 20:
                     {
                     alt17=1;
                     }
                     break;
                 case 26:
                     {
                     alt17=2;
                     }
                     break;
                 case 17:
                     {
                     alt17=3;
                     }
                     break;
                 case 21:
                     {
                     alt17=4;
                     }
                     break;
 
                 }
 
                 switch (alt17) {
             	case 1 :
             	    // Dialog.g:150:9: '*' op2= unary
             	    {
             	    match(input,20,FOLLOW_20_in_mult1066); 
 
             	    pushFollow(FOLLOW_unary_in_mult1070);
             	    op2=unary();
 
             	    state._fsp--;
 
 
             	     value = MultiplyEvaluator.create(value, op2); 
 
             	    }
             	    break;
             	case 2 :
             	    // Dialog.g:151:11: '/' op2= unary
             	    {
             	    match(input,26,FOLLOW_26_in_mult1084); 
 
             	    pushFollow(FOLLOW_unary_in_mult1088);
             	    op2=unary();
 
             	    state._fsp--;
 
 
             	     value = DivideEvaluator.create(value, op2); 
 
             	    }
             	    break;
             	case 3 :
             	    // Dialog.g:152:11: '%' op2= unary
             	    {
             	    match(input,17,FOLLOW_17_in_mult1102); 
 
             	    pushFollow(FOLLOW_unary_in_mult1106);
             	    op2=unary();
 
             	    state._fsp--;
 
 
             	     value = ModEvaluator.create(value, op2); 
 
             	    }
             	    break;
             	case 4 :
             	    // Dialog.g:153:11: '**' op2= unary
             	    {
             	    match(input,21,FOLLOW_21_in_mult1120); 
 
             	    pushFollow(FOLLOW_unary_in_mult1124);
             	    op2=unary();
 
             	    state._fsp--;
 
 
             	     value = PowerEvaluator.create(value, op2); 
 
             	    }
             	    break;
 
             	default :
             	    break loop17;
                 }
             } while (true);
 
 
             }
 
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return value;
     }
     // $ANTLR end "mult"
 
 
 
     // $ANTLR start "term"
     // Dialog.g:156:1: term returns [Evaluator value] : ( '(' expression ')' | BOOLEAN_LITERAL | NUMBER | STRING_LITERAL | QUERY_STRING );
     public final Evaluator term() throws RecognitionException {
         Evaluator value = null;
 
 
         Token BOOLEAN_LITERAL16=null;
         Token NUMBER17=null;
         Token STRING_LITERAL18=null;
         Token QUERY_STRING19=null;
         Evaluator expression15 =null;
 
 
         try {
             // Dialog.g:156:32: ( '(' expression ')' | BOOLEAN_LITERAL | NUMBER | STRING_LITERAL | QUERY_STRING )
             int alt18=5;
             switch ( input.LA(1) ) {
             case 18:
                 {
                 alt18=1;
                 }
                 break;
             case BOOLEAN_LITERAL:
                 {
                 alt18=2;
                 }
                 break;
             case NUMBER:
                 {
                 alt18=3;
                 }
                 break;
             case STRING_LITERAL:
                 {
                 alt18=4;
                 }
                 break;
             case QUERY_STRING:
                 {
                 alt18=5;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 18, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt18) {
                 case 1 :
                     // Dialog.g:157:5: '(' expression ')'
                     {
                     match(input,18,FOLLOW_18_in_term1149); 
 
                     pushFollow(FOLLOW_expression_in_term1151);
                     expression15=expression();
 
                     state._fsp--;
 
 
                     match(input,19,FOLLOW_19_in_term1153); 
 
                      value = expression15; 
 
                     }
                     break;
                 case 2 :
                     // Dialog.g:158:7: BOOLEAN_LITERAL
                     {
                     BOOLEAN_LITERAL16=(Token)match(input,BOOLEAN_LITERAL,FOLLOW_BOOLEAN_LITERAL_in_term1163); 
 
                      value = BooleanEvaluator.create((BOOLEAN_LITERAL16!=null?BOOLEAN_LITERAL16.getText():null)); 
 
                     }
                     break;
                 case 3 :
                     // Dialog.g:159:7: NUMBER
                     {
                     NUMBER17=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_term1173); 
 
                      value = NumberEvaluator.create((NUMBER17!=null?NUMBER17.getText():null)); 
 
                     }
                     break;
                 case 4 :
                     // Dialog.g:160:7: STRING_LITERAL
                     {
                     STRING_LITERAL18=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_term1183); 
 
                      value = StringEvaluator.create((STRING_LITERAL18!=null?STRING_LITERAL18.getText():null)); 
 
                     }
                     break;
                 case 5 :
                     // Dialog.g:161:7: QUERY_STRING
                     {
                     QUERY_STRING19=(Token)match(input,QUERY_STRING,FOLLOW_QUERY_STRING_in_term1193); 
 
                      value = VariableEvaluator.create(variableSource, (QUERY_STRING19!=null?QUERY_STRING19.getText():null)); 
 
                     }
                     break;
 
             }
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return value;
     }
     // $ANTLR end "term"
 
 
 
     // $ANTLR start "time_unit"
     // Dialog.g:163:1: time_unit returns [TimeUnit unit] : ( 'ns' | 'us' | 'ms' | 's' | 'm' | 'h' | 'd' );
     public final TimeUnit time_unit() throws RecognitionException {
         TimeUnit unit = null;
 
 
         try {
             // Dialog.g:163:35: ( 'ns' | 'us' | 'ms' | 's' | 'm' | 'h' | 'd' )
             int alt19=7;
             switch ( input.LA(1) ) {
             case 40:
                 {
                 alt19=1;
                 }
                 break;
             case 46:
                 {
                 alt19=2;
                 }
                 break;
             case 39:
                 {
                 alt19=3;
                 }
                 break;
             case 44:
                 {
                 alt19=4;
                 }
                 break;
             case 38:
                 {
                 alt19=5;
                 }
                 break;
             case 36:
                 {
                 alt19=6;
                 }
                 break;
             case 34:
                 {
                 alt19=7;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 19, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt19) {
                 case 1 :
                     // Dialog.g:164:5: 'ns'
                     {
                     match(input,40,FOLLOW_40_in_time_unit1212); 
 
                      unit = TimeUnit.NANOSECONDS; 
 
                     }
                     break;
                 case 2 :
                     // Dialog.g:165:7: 'us'
                     {
                     match(input,46,FOLLOW_46_in_time_unit1222); 
 
                      unit = TimeUnit.MICROSECONDS; 
 
                     }
                     break;
                 case 3 :
                     // Dialog.g:166:7: 'ms'
                     {
                     match(input,39,FOLLOW_39_in_time_unit1232); 
 
                      unit = TimeUnit.MILLISECONDS; 
 
                     }
                     break;
                 case 4 :
                     // Dialog.g:167:7: 's'
                     {
                     match(input,44,FOLLOW_44_in_time_unit1242); 
 
                      unit = TimeUnit.SECONDS; 
 
                     }
                     break;
                 case 5 :
                     // Dialog.g:168:7: 'm'
                     {
                     match(input,38,FOLLOW_38_in_time_unit1252); 
 
                      unit = TimeUnit.MINUTES; 
 
                     }
                     break;
                 case 6 :
                     // Dialog.g:169:7: 'h'
                     {
                     match(input,36,FOLLOW_36_in_time_unit1262); 
 
                      unit = TimeUnit.HOURS; 
 
                     }
                     break;
                 case 7 :
                     // Dialog.g:170:7: 'd'
                     {
                     match(input,34,FOLLOW_34_in_time_unit1272); 
 
                      unit = TimeUnit.DAYS; 
 
                     }
                     break;
 
             }
         }
 
         catch (RecognitionException e) {
             throw e;
         }
 
         finally {
         	// do for sure before leaving
         }
         return unit;
     }
     // $ANTLR end "time_unit"
 
     // Delegated rules
 
 
  
 
     public static final BitSet FOLLOW_rule_in_program81 = new BitSet(new long[]{0x00000C0000000002L});
     public static final BitSet FOLLOW_response_in_program96 = new BitSet(new long[]{0x00000C0000000002L});
     public static final BitSet FOLLOW_IDENT_in_name136 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_IDENT_in_name153 = new BitSet(new long[]{0x0000000004000000L});
     public static final BitSet FOLLOW_26_in_name155 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_IDENT_in_name159 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_42_in_response197 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_name_in_response199 = new BitSet(new long[]{0x0000800000000000L});
     public static final BitSet FOLLOW_47_in_response204 = new BitSet(new long[]{0x0001022000000040L});
     public static final BitSet FOLLOW_response_statement_in_response207 = new BitSet(new long[]{0x0000000008000000L});
     public static final BitSet FOLLOW_27_in_response210 = new BitSet(new long[]{0x0001022000000040L});
     public static final BitSet FOLLOW_48_in_response214 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_37_in_response_statement239 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_response_statement241 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_remember_statement_in_response_statement251 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_generic_statement_in_response_statement261 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_43_in_rule292 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_name_in_rule294 = new BitSet(new long[]{0x0000800000000000L});
     public static final BitSet FOLLOW_47_in_rule299 = new BitSet(new long[]{0x0000000200000000L});
     public static final BitSet FOLLOW_criteria_in_rule301 = new BitSet(new long[]{0x0000000008000000L});
     public static final BitSet FOLLOW_27_in_rule305 = new BitSet(new long[]{0x0001060000000040L});
     public static final BitSet FOLLOW_rule_statement_in_rule308 = new BitSet(new long[]{0x0000000008000000L});
     public static final BitSet FOLLOW_27_in_rule311 = new BitSet(new long[]{0x0001060000000040L});
     public static final BitSet FOLLOW_48_in_rule315 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_42_in_rule_statement345 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_name_in_rule_statement349 = new BitSet(new long[]{0x0000200000000002L});
     public static final BitSet FOLLOW_45_in_rule_statement390 = new BitSet(new long[]{0x0000000000001040L});
     public static final BitSet FOLLOW_IDENT_in_rule_statement395 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_NUMBER_in_rule_statement401 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_IDENT_in_rule_statement406 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_remember_statement_in_rule_statement444 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_generic_statement_in_rule_statement454 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_IDENT_in_generic_statement488 = new BitSet(new long[]{0x0000000000000042L});
     public static final BitSet FOLLOW_IDENT_in_generic_statement495 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_generic_statement497 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_generic_statement499 = new BitSet(new long[]{0x0000000000000042L});
     public static final BitSet FOLLOW_41_in_remember_statement538 = new BitSet(new long[]{0x0000000000002000L});
     public static final BitSet FOLLOW_remember_assignment_in_remember_statement540 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_23_in_remember_statement544 = new BitSet(new long[]{0x0000000000002000L});
     public static final BitSet FOLLOW_remember_assignment_in_remember_statement546 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_QUERY_STRING_in_remember_assignment580 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_remember_assignment582 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_remember_assignment584 = new BitSet(new long[]{0x0000000002000082L});
     public static final BitSet FOLLOW_INTEGER_in_remember_assignment611 = new BitSet(new long[]{0x000051D400000000L});
     public static final BitSet FOLLOW_time_unit_in_remember_assignment615 = new BitSet(new long[]{0x0000000002000002L});
     public static final BitSet FOLLOW_25_in_remember_assignment653 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_33_in_criteria708 = new BitSet(new long[]{0x0000000800000000L});
     public static final BitSet FOLLOW_35_in_criteria732 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_IDENT_in_criteria736 = new BitSet(new long[]{0x0000000000800042L});
     public static final BitSet FOLLOW_23_in_criteria742 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_IDENT_in_criteria746 = new BitSet(new long[]{0x0000000000800042L});
     public static final BitSet FOLLOW_IDENT_in_criteria786 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_criteria788 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_criteria792 = new BitSet(new long[]{0x0000000000000042L});
     public static final BitSet FOLLOW_IDENT_in_criteria808 = new BitSet(new long[]{0x0000000080000000L});
     public static final BitSet FOLLOW_31_in_criteria810 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_criteria814 = new BitSet(new long[]{0x0000000000000042L});
     public static final BitSet FOLLOW_IDENT_in_criteria830 = new BitSet(new long[]{0x0000000010000000L});
     public static final BitSet FOLLOW_28_in_criteria832 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_criteria836 = new BitSet(new long[]{0x0000000000000042L});
     public static final BitSet FOLLOW_IDENT_in_criteria852 = new BitSet(new long[]{0x0000000020000000L});
     public static final BitSet FOLLOW_29_in_criteria854 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_criteria858 = new BitSet(new long[]{0x0000000000000042L});
     public static final BitSet FOLLOW_IDENT_in_criteria874 = new BitSet(new long[]{0x0000000100000000L});
     public static final BitSet FOLLOW_32_in_criteria876 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_criteria880 = new BitSet(new long[]{0x0000000000000042L});
     public static final BitSet FOLLOW_IDENT_in_criteria896 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_16_in_criteria898 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_criteria902 = new BitSet(new long[]{0x0000000000000042L});
     public static final BitSet FOLLOW_IDENT_in_criteria918 = new BitSet(new long[]{0x0000000000000042L});
     public static final BitSet FOLLOW_mult_in_expression949 = new BitSet(new long[]{0x0000000001400002L});
     public static final BitSet FOLLOW_22_in_expression963 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_mult_in_expression967 = new BitSet(new long[]{0x0000000001400002L});
     public static final BitSet FOLLOW_24_in_expression978 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_mult_in_expression982 = new BitSet(new long[]{0x0000000001400002L});
     public static final BitSet FOLLOW_22_in_unary1014 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_24_in_unary1018 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_term_in_unary1028 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_unary_in_mult1048 = new BitSet(new long[]{0x0000000004320002L});
     public static final BitSet FOLLOW_20_in_mult1066 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_unary_in_mult1070 = new BitSet(new long[]{0x0000000004320002L});
     public static final BitSet FOLLOW_26_in_mult1084 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_unary_in_mult1088 = new BitSet(new long[]{0x0000000004320002L});
     public static final BitSet FOLLOW_17_in_mult1102 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_unary_in_mult1106 = new BitSet(new long[]{0x0000000004320002L});
     public static final BitSet FOLLOW_21_in_mult1120 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_unary_in_mult1124 = new BitSet(new long[]{0x0000000004320002L});
     public static final BitSet FOLLOW_18_in_term1149 = new BitSet(new long[]{0x0000000001447010L});
     public static final BitSet FOLLOW_expression_in_term1151 = new BitSet(new long[]{0x0000000000080000L});
     public static final BitSet FOLLOW_19_in_term1153 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_BOOLEAN_LITERAL_in_term1163 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_NUMBER_in_term1173 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_STRING_LITERAL_in_term1183 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_QUERY_STRING_in_term1193 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_40_in_time_unit1212 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_46_in_time_unit1222 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_39_in_time_unit1232 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_44_in_time_unit1242 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_38_in_time_unit1252 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_36_in_time_unit1262 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_34_in_time_unit1272 = new BitSet(new long[]{0x0000000000000002L});
 
 }
