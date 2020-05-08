// $ANTLR 3.4 C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g 2012-01-31 22:39:31
 
 	package parser;
 	import ast.*;
 	import java.util.LinkedList;
 
     import static misc.WhileLanguageParserUtils.*;
     import misc.Pair;
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class WhileLanguageParser extends Parser {
     public static final String[] tokenNames = new String[] {
         "<invalid>", "<EOR>", "<DOWN>", "<UP>", "BOOL_LITERAL", "COMMENT", "IDENT", "INT_LITERAL", "QUANTIFIER", "WS", "'!'", "'!='", "'%'", "'&'", "'('", "')'", "'*'", "'+'", "','", "'-'", "'/'", "';'", "'<'", "'<='", "'='", "'=='", "'>'", "'>='", "'['", "']'", "'array'", "'assert'", "'assume'", "'axiom'", "'bool'", "'else'", "'ensure'", "'if'", "'int'", "'invariant'", "'main'", "'return'", "'while'", "'{'", "'|'", "'}'"
     };
 
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
 
     // delegates
     public Parser[] getDelegates() {
         return new Parser[] {};
     }
 
     // delegators
 
 
     public WhileLanguageParser(TokenStream input) {
         this(input, new RecognizerSharedState());
     }
     public WhileLanguageParser(TokenStream input, RecognizerSharedState state) {
         super(input, state);
     }
 
     public String[] getTokenNames() { return WhileLanguageParser.tokenNames; }
     public String getGrammarFileName() { return "C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g"; }
 
 
         private LinkedList<String> reporter;
         private boolean error = false;
 
         public void setErrorReporter(LinkedList<String> reporter) {
             this.reporter = reporter;
         }
 
         @Override
         public void emitErrorMessage(String msg) {
             reporter.add(msg);
         }
 
         @Override
         protected Object recoverFromMismatchedToken(IntStream input,
                                                     int ttype,
                                                     BitSet follow)
                                                     throws RecognitionException {
             error = true;
             return super.recoverFromMismatchedToken(input, ttype, follow);
         }
 
 
 
     // $ANTLR start "program"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:53:1: program returns [Program p] : (a= axiom_statement )* (f= function_declaration )* main ;
     public final Program program() throws RecognitionException {
         Program p = null;
 
 
         WhileLanguageParser.axiom_statement_return a =null;
 
         WhileLanguageParser.function_declaration_return f =null;
 
         WhileLanguageParser.main_return main1 =null;
 
 
         LinkedList<Axiom> axiom = new LinkedList<Axiom>();
         	       LinkedList<Function> function = new LinkedList<Function>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:56:9: ( (a= axiom_statement )* (f= function_declaration )* main )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:56:11: (a= axiom_statement )* (f= function_declaration )* main
             {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:56:11: (a= axiom_statement )*
             loop1:
             do {
                 int alt1=2;
                 int LA1_0 = input.LA(1);
 
                 if ( (LA1_0==33) ) {
                     alt1=1;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:56:12: a= axiom_statement
             	    {
             	    pushFollow(FOLLOW_axiom_statement_in_program61);
             	    a=axiom_statement();
 
             	    state._fsp--;
 
 
             	    if (!error) axiom.addAll((a!=null?a.result:null));
 
             	    }
             	    break;
 
             	default :
             	    break loop1;
                 }
             } while (true);
 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:57:13: (f= function_declaration )*
             loop2:
             do {
                 int alt2=2;
                 int LA2_0 = input.LA(1);
 
                 if ( (LA2_0==34||LA2_0==38) ) {
                     alt2=1;
                 }
 
 
                 switch (alt2) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:57:14: f= function_declaration
             	    {
             	    pushFollow(FOLLOW_function_declaration_in_program82);
             	    f=function_declaration();
 
             	    state._fsp--;
 
 
             	    if (!error) function.add((f!=null?f.ast:null));
 
             	    }
             	    break;
 
             	default :
             	    break loop2;
                 }
             } while (true);
 
 
             pushFollow(FOLLOW_main_in_program88);
             main1=main();
 
             state._fsp--;
 
 
             p = error ? null : new Program(new Position(),
                             function.toArray(new Function[function.size()]), (main1!=null?main1.ast:null),
                     		axiom.toArray(new Axiom[axiom.size()]));
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return p;
     }
     // $ANTLR end "program"
 
 
 
     // $ANTLR start "single_expression"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:63:1: single_expression returns [ Expression ast ] : expression ;
     public final Expression single_expression() throws RecognitionException {
         Expression ast = null;
 
 
         WhileLanguageParser.expression_return expression2 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:64:2: ( expression )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:64:4: expression
             {
             pushFollow(FOLLOW_expression_in_single_expression121);
             expression2=expression();
 
             state._fsp--;
 
 
             ast = error ? null : (expression2!=null?expression2.ast:null);
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return ast;
     }
     // $ANTLR end "single_expression"
 
 
     public static class function_declaration_return extends ParserRuleReturnScope {
         public Function ast;
     };
 
 
     // $ANTLR start "function_declaration"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:67:1: function_declaration returns [ Function ast ] : type IDENT '(' (p= parameter_list )? ')' function_body ;
     public final WhileLanguageParser.function_declaration_return function_declaration() throws RecognitionException {
         WhileLanguageParser.function_declaration_return retval = new WhileLanguageParser.function_declaration_return();
         retval.start = input.LT(1);
 
 
         Token IDENT3=null;
         LinkedList<FunctionParameter> p =null;
 
         Type type4 =null;
 
         WhileLanguageParser.function_body_return function_body5 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:68:9: ( type IDENT '(' (p= parameter_list )? ')' function_body )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:68:11: type IDENT '(' (p= parameter_list )? ')' function_body
             {
             pushFollow(FOLLOW_type_in_function_declaration145);
             type4=type();
 
             state._fsp--;
 
 
             IDENT3=(Token)match(input,IDENT,FOLLOW_IDENT_in_function_declaration147); 
 
             match(input,14,FOLLOW_14_in_function_declaration149); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:68:27: (p= parameter_list )?
             int alt3=2;
             int LA3_0 = input.LA(1);
 
             if ( (LA3_0==34||LA3_0==38) ) {
                 alt3=1;
             }
             switch (alt3) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:68:27: p= parameter_list
                     {
                     pushFollow(FOLLOW_parameter_list_in_function_declaration153);
                     p=parameter_list();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             match(input,15,FOLLOW_15_in_function_declaration156); 
 
             pushFollow(FOLLOW_function_body_in_function_declaration158);
             function_body5=function_body();
 
             state._fsp--;
 
 
             if ("main".equals((IDENT3!=null?IDENT3.getText():null))) throw new TreeGeneratorException("\"main\" must only be declared once.");
                          if ("length".equals((IDENT3!=null?IDENT3.getText():null))) throw new TreeGeneratorException("\"length\" is a reserved name.");
                     	LinkedList<FunctionParameter> params = error ? null :
                     		p != null ? p : new LinkedList<FunctionParameter>();
                     	 retval.ast = error ? null :
                             new Function(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), type4, (IDENT3!=null?IDENT3.getText():null),
                     		params.toArray(new FunctionParameter[params.size()]),
                     		(function_body5!=null?function_body5.ast:null), (function_body5!=null?function_body5.pre:null).toArray(new Assumption[(function_body5!=null?function_body5.pre:null).size()]),
                     		(function_body5!=null?function_body5.post:null).toArray(new Ensure[(function_body5!=null?function_body5.post:null).size()]));
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "function_declaration"
 
 
     public static class main_return extends ParserRuleReturnScope {
         public Function ast;
     };
 
 
     // $ANTLR start "main"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:80:1: main returns [ Function ast ] : 'main' '(' ( parameter_list )? ')' main_body ;
     public final WhileLanguageParser.main_return main() throws RecognitionException {
         WhileLanguageParser.main_return retval = new WhileLanguageParser.main_return();
         retval.start = input.LT(1);
 
 
         LinkedList<FunctionParameter> parameter_list6 =null;
 
         WhileLanguageParser.main_body_return main_body7 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:81:9: ( 'main' '(' ( parameter_list )? ')' main_body )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:81:11: 'main' '(' ( parameter_list )? ')' main_body
             {
             match(input,40,FOLLOW_40_in_main198); 
 
             match(input,14,FOLLOW_14_in_main200); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:81:22: ( parameter_list )?
             int alt4=2;
             int LA4_0 = input.LA(1);
 
             if ( (LA4_0==34||LA4_0==38) ) {
                 alt4=1;
             }
             switch (alt4) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:81:22: parameter_list
                     {
                     pushFollow(FOLLOW_parameter_list_in_main202);
                     parameter_list6=parameter_list();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             match(input,15,FOLLOW_15_in_main205); 
 
             pushFollow(FOLLOW_main_body_in_main207);
             main_body7=main_body();
 
             state._fsp--;
 
 
             LinkedList<FunctionParameter> params = error ? null :
                     		parameter_list6 != null ? parameter_list6 : new LinkedList<FunctionParameter>();
                     	retval.ast = error ? null : new Function(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), null, "main",
                     		params.toArray(new FunctionParameter[params.size()]),
                     		(main_body7!=null?main_body7.ast:null), (main_body7!=null?main_body7.pre:null).toArray(new Assumption[(main_body7!=null?main_body7.pre:null).size()]),
                     		(main_body7!=null?main_body7.post:null).toArray(new Ensure[(main_body7!=null?main_body7.post:null).size()]));
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "main"
 
 
 
     // $ANTLR start "parameter_list"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:90:1: parameter_list returns [ LinkedList<FunctionParameter> params ] : p1= parameter ( ',' p2= parameter )* ;
     public final LinkedList<FunctionParameter> parameter_list() throws RecognitionException {
         LinkedList<FunctionParameter> params = null;
 
 
         FunctionParameter p1 =null;
 
         FunctionParameter p2 =null;
 
 
         params = new LinkedList<FunctionParameter>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:92:9: (p1= parameter ( ',' p2= parameter )* )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:92:11: p1= parameter ( ',' p2= parameter )*
             {
             pushFollow(FOLLOW_parameter_in_parameter_list262);
             p1=parameter();
 
             state._fsp--;
 
 
             if (!error) params.add(p1);
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:92:60: ( ',' p2= parameter )*
             loop5:
             do {
                 int alt5=2;
                 int LA5_0 = input.LA(1);
 
                 if ( (LA5_0==18) ) {
                     alt5=1;
                 }
 
 
                 switch (alt5) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:92:62: ',' p2= parameter
             	    {
             	    match(input,18,FOLLOW_18_in_parameter_list268); 
 
             	    pushFollow(FOLLOW_parameter_in_parameter_list272);
             	    p2=parameter();
 
             	    state._fsp--;
 
 
             	    if (!error) params.add(p2);
 
             	    }
             	    break;
 
             	default :
             	    break loop5;
                 }
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
         return params;
     }
     // $ANTLR end "parameter_list"
 
 
 
     // $ANTLR start "parameter"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:95:1: parameter returns [ FunctionParameter ast ] : type IDENT ;
     public final FunctionParameter parameter() throws RecognitionException {
         FunctionParameter ast = null;
 
 
         Token IDENT8=null;
         Type type9 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:96:9: ( type IDENT )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:96:11: type IDENT
             {
             pushFollow(FOLLOW_type_in_parameter306);
             type9=type();
 
             state._fsp--;
 
 
             IDENT8=(Token)match(input,IDENT,FOLLOW_IDENT_in_parameter308); 
 
             ast = error ? null : new FunctionParameter((IDENT8!=null?IDENT8.getText():null), type9);
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return ast;
     }
     // $ANTLR end "parameter"
 
 
     public static class function_body_return extends ParserRuleReturnScope {
         public StatementBlock ast;
         public LinkedList<Assumption> pre;
         public LinkedList<Ensure> post;
     };
 
 
     // $ANTLR start "function_body"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:99:1: function_body returns [ StatementBlock ast, LinkedList<Assumption> pre, LinkedList<Ensure> post ] : (a= assume_statement )? '{' ( statement )* r= return_statement '}' (e= ensure_statement )? ;
     public final WhileLanguageParser.function_body_return function_body() throws RecognitionException {
         WhileLanguageParser.function_body_return retval = new WhileLanguageParser.function_body_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.assume_statement_return a =null;
 
         WhileLanguageParser.return_statement_return r =null;
 
         WhileLanguageParser.ensure_statement_return e =null;
 
         WhileLanguageParser.statement_return statement10 =null;
 
 
         LinkedList<Statement> s = new LinkedList<Statement>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:101:9: ( (a= assume_statement )? '{' ( statement )* r= return_statement '}' (e= ensure_statement )? )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:101:11: (a= assume_statement )? '{' ( statement )* r= return_statement '}' (e= ensure_statement )?
             {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:101:12: (a= assume_statement )?
             int alt6=2;
             int LA6_0 = input.LA(1);
 
             if ( (LA6_0==32) ) {
                 alt6=1;
             }
             switch (alt6) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:101:12: a= assume_statement
                     {
                     pushFollow(FOLLOW_assume_statement_in_function_body347);
                     a=assume_statement();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             retval.pre = error ? null : (a!=null?a.result:null) != null ? (a!=null?a.result:null) : new LinkedList<Assumption>();
 
             match(input,43,FOLLOW_43_in_function_body362); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:102:15: ( statement )*
             loop7:
             do {
                 int alt7=2;
                 int LA7_0 = input.LA(1);
 
                 if ( (LA7_0==IDENT||LA7_0==31||LA7_0==34||(LA7_0 >= 37 && LA7_0 <= 38)||LA7_0==42) ) {
                     alt7=1;
                 }
 
 
                 switch (alt7) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:102:17: statement
             	    {
             	    pushFollow(FOLLOW_statement_in_function_body366);
             	    statement10=statement();
 
             	    state._fsp--;
 
 
             	    if (!error) { s.addAll(possibleDivByZero((statement10!=null?statement10.divisors:null))); s.addAll(possibleNotPositive((statement10!=null?statement10.positive:null)));
             	                s.addAll(possibleArrayOutOfBounds((statement10!=null?statement10.arrayIndices:null))); s.add((statement10!=null?statement10.ast:null)); }
 
             	    }
             	    break;
 
             	default :
             	    break loop7;
                 }
             } while (true);
 
 
             pushFollow(FOLLOW_return_statement_in_function_body387);
             r=return_statement();
 
             state._fsp--;
 
 
             s.addAll(possibleArrayOutOfBounds(r.arrayIndices)); s.add(r.ast);
 
             match(input,45,FOLLOW_45_in_function_body391); 
 
             retval.ast = error ?null : new StatementBlock(s.toArray(new Statement[s.size()]), new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()));
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:106:12: (e= ensure_statement )?
             int alt8=2;
             int LA8_0 = input.LA(1);
 
             if ( (LA8_0==36) ) {
                 alt8=1;
             }
             switch (alt8) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:106:12: e= ensure_statement
                     {
                     pushFollow(FOLLOW_ensure_statement_in_function_body421);
                     e=ensure_statement();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             retval.post = error ? null : (e!=null?e.result:null) != null ? (e!=null?e.result:null) : new LinkedList<Ensure>();
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "function_body"
 
 
     public static class main_body_return extends ParserRuleReturnScope {
         public StatementBlock ast;
         public LinkedList<Assumption> pre;
         public LinkedList<Ensure> post;
     };
 
 
     // $ANTLR start "main_body"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:110:1: main_body returns [ StatementBlock ast, LinkedList<Assumption> pre, LinkedList<Ensure> post ] : (a= assume_statement )? '{' ( statement )* '}' (e= ensure_statement )? ;
     public final WhileLanguageParser.main_body_return main_body() throws RecognitionException {
         WhileLanguageParser.main_body_return retval = new WhileLanguageParser.main_body_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.assume_statement_return a =null;
 
         WhileLanguageParser.ensure_statement_return e =null;
 
         WhileLanguageParser.statement_return statement11 =null;
 
 
         LinkedList<Statement> s = new LinkedList<Statement>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:112:9: ( (a= assume_statement )? '{' ( statement )* '}' (e= ensure_statement )? )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:112:11: (a= assume_statement )? '{' ( statement )* '}' (e= ensure_statement )?
             {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:112:12: (a= assume_statement )?
             int alt9=2;
             int LA9_0 = input.LA(1);
 
             if ( (LA9_0==32) ) {
                 alt9=1;
             }
             switch (alt9) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:112:12: a= assume_statement
                     {
                     pushFollow(FOLLOW_assume_statement_in_main_body470);
                     a=assume_statement();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             retval.pre = error ? null : (a!=null?a.result:null) != null ? (a!=null?a.result:null) : new LinkedList<Assumption>();
 
             match(input,43,FOLLOW_43_in_main_body485); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:113:15: ( statement )*
             loop10:
             do {
                 int alt10=2;
                 int LA10_0 = input.LA(1);
 
                 if ( (LA10_0==IDENT||LA10_0==31||LA10_0==34||(LA10_0 >= 37 && LA10_0 <= 38)||LA10_0==42) ) {
                     alt10=1;
                 }
 
 
                 switch (alt10) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:113:17: statement
             	    {
             	    pushFollow(FOLLOW_statement_in_main_body489);
             	    statement11=statement();
 
             	    state._fsp--;
 
 
             	    if (!error) { s.addAll(possibleDivByZero((statement11!=null?statement11.divisors:null))); s.addAll(possibleNotPositive((statement11!=null?statement11.positive:null)));
             	                s.addAll(possibleArrayOutOfBounds((statement11!=null?statement11.arrayIndices:null))); s.add((statement11!=null?statement11.ast:null)); }
 
             	    }
             	    break;
 
             	default :
             	    break loop10;
                 }
             } while (true);
 
 
             match(input,45,FOLLOW_45_in_main_body496); 
 
             retval.ast = error ?null : new StatementBlock(s.toArray(new Statement[s.size()]), new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()));
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:116:12: (e= ensure_statement )?
             int alt11=2;
             int LA11_0 = input.LA(1);
 
             if ( (LA11_0==36) ) {
                 alt11=1;
             }
             switch (alt11) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:116:12: e= ensure_statement
                     {
                     pushFollow(FOLLOW_ensure_statement_in_main_body526);
                     e=ensure_statement();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             retval.post = error ? null : (e!=null?e.result:null) != null ? (e!=null?e.result:null) : new LinkedList<Ensure>();
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "main_body"
 
 
     public static class if_body_return extends ParserRuleReturnScope {
         public StatementBlock ast;
     };
 
 
     // $ANTLR start "if_body"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:119:1: if_body returns [ StatementBlock ast ] : '{' ( statement )* '}' ;
     public final WhileLanguageParser.if_body_return if_body() throws RecognitionException {
         WhileLanguageParser.if_body_return retval = new WhileLanguageParser.if_body_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.statement_return statement12 =null;
 
 
         LinkedList<Statement> s = new LinkedList<Statement>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:121:9: ( '{' ( statement )* '}' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:121:11: '{' ( statement )* '}'
             {
             match(input,43,FOLLOW_43_in_if_body564); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:121:15: ( statement )*
             loop12:
             do {
                 int alt12=2;
                 int LA12_0 = input.LA(1);
 
                 if ( (LA12_0==IDENT||LA12_0==31||LA12_0==34||(LA12_0 >= 37 && LA12_0 <= 38)||LA12_0==42) ) {
                     alt12=1;
                 }
 
 
                 switch (alt12) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:121:17: statement
             	    {
             	    pushFollow(FOLLOW_statement_in_if_body568);
             	    statement12=statement();
 
             	    state._fsp--;
 
 
             	    if (!error) {s.addAll(possibleDivByZero((statement12!=null?statement12.divisors:null)));
             	                s.addAll(possibleArrayOutOfBounds((statement12!=null?statement12.arrayIndices:null))); s.addAll(possibleNotPositive((statement12!=null?statement12.positive:null)));
             	                s.add((statement12!=null?statement12.ast:null));}
 
             	    }
             	    break;
 
             	default :
             	    break loop12;
                 }
             } while (true);
 
 
             match(input,45,FOLLOW_45_in_if_body575); 
 
             retval.ast = error ? null : new StatementBlock(s.toArray(new Statement[s.size()]), new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()));
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "if_body"
 
 
     public static class loop_body_return extends ParserRuleReturnScope {
         public StatementBlock ast;
         public LinkedList<Invariant> pre;
         public LinkedList<Ensure> post;
     };
 
 
     // $ANTLR start "loop_body"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:127:1: loop_body returns [ StatementBlock ast, LinkedList<Invariant> pre, LinkedList<Ensure> post ] : (i= invariant_statement )? '{' ( statement )* '}' (e= ensure_statement )? ;
     public final WhileLanguageParser.loop_body_return loop_body() throws RecognitionException {
         WhileLanguageParser.loop_body_return retval = new WhileLanguageParser.loop_body_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.invariant_statement_return i =null;
 
         WhileLanguageParser.ensure_statement_return e =null;
 
         WhileLanguageParser.statement_return statement13 =null;
 
 
         LinkedList<Statement> s = new LinkedList<Statement>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:129:9: ( (i= invariant_statement )? '{' ( statement )* '}' (e= ensure_statement )? )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:129:11: (i= invariant_statement )? '{' ( statement )* '}' (e= ensure_statement )?
             {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:129:12: (i= invariant_statement )?
             int alt13=2;
             int LA13_0 = input.LA(1);
 
             if ( (LA13_0==39) ) {
                 alt13=1;
             }
             switch (alt13) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:129:12: i= invariant_statement
                     {
                     pushFollow(FOLLOW_invariant_statement_in_loop_body626);
                     i=invariant_statement();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             retval.pre = error ? null : (i!=null?i.result:null) != null ? (i!=null?i.result:null) : new LinkedList<Invariant>();
 
             match(input,43,FOLLOW_43_in_loop_body641); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:130:15: ( statement )*
             loop14:
             do {
                 int alt14=2;
                 int LA14_0 = input.LA(1);
 
                 if ( (LA14_0==IDENT||LA14_0==31||LA14_0==34||(LA14_0 >= 37 && LA14_0 <= 38)||LA14_0==42) ) {
                     alt14=1;
                 }
 
 
                 switch (alt14) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:130:17: statement
             	    {
             	    pushFollow(FOLLOW_statement_in_loop_body645);
             	    statement13=statement();
 
             	    state._fsp--;
 
 
             	    if (!error) {s.addAll(possibleDivByZero((statement13!=null?statement13.divisors:null)));
             	                s.addAll(possibleArrayOutOfBounds((statement13!=null?statement13.arrayIndices:null))); s.addAll(possibleNotPositive((statement13!=null?statement13.positive:null)));
             	                s.add((statement13!=null?statement13.ast:null));}
 
             	    }
             	    break;
 
             	default :
             	    break loop14;
                 }
             } while (true);
 
 
             match(input,45,FOLLOW_45_in_loop_body652); 
 
             retval.ast = error ? null : new StatementBlock(s.toArray(new Statement[s.size()]),
                           new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()));
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:135:12: (e= ensure_statement )?
             int alt15=2;
             int LA15_0 = input.LA(1);
 
             if ( (LA15_0==36) ) {
                 alt15=1;
             }
             switch (alt15) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:135:12: e= ensure_statement
                     {
                     pushFollow(FOLLOW_ensure_statement_in_loop_body682);
                     e=ensure_statement();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             retval.post = error ? null : (e!=null?e.result:null) != null ? (e!=null?e.result:null) : new LinkedList<Ensure>();
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "loop_body"
 
 
     public static class statement_return extends ParserRuleReturnScope {
         public Statement ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
         public LinkedList<Expression> positive;
     };
 
 
     // $ANTLR start "statement"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:138:1: statement returns [ Statement ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices,\r\n\t\t\t\tLinkedList<Expression> positive ] : (e1= assert_statement |e2= variable_declaration |e3= array_declaration |e4= assignment |e5= if_statement |e6= while_statement );
     public final WhileLanguageParser.statement_return statement() throws RecognitionException {
         WhileLanguageParser.statement_return retval = new WhileLanguageParser.statement_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.assert_statement_return e1 =null;
 
         WhileLanguageParser.variable_declaration_return e2 =null;
 
         WhileLanguageParser.array_declaration_return e3 =null;
 
         WhileLanguageParser.assignment_return e4 =null;
 
         WhileLanguageParser.if_statement_return e5 =null;
 
         WhileLanguageParser.while_statement_return e6 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:140:9: (e1= assert_statement |e2= variable_declaration |e3= array_declaration |e4= assignment |e5= if_statement |e6= while_statement )
             int alt16=6;
             alt16 = dfa16.predict(input);
             switch (alt16) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:140:11: e1= assert_statement
                     {
                     pushFollow(FOLLOW_assert_statement_in_statement716);
                     e1=assert_statement();
 
                     state._fsp--;
 
 
                     if (!error) {retval.ast = (e1!=null?e1.ast:null); retval.divisors = (e1!=null?e1.divisors:null); retval.arrayIndices = (e1!=null?e1.arrayIndices:null);}
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:141:11: e2= variable_declaration
                     {
                     pushFollow(FOLLOW_variable_declaration_in_statement732);
                     e2=variable_declaration();
 
                     state._fsp--;
 
 
                     if (!error) {retval.ast = (e2!=null?e2.ast:null); retval.divisors = (e2!=null?e2.divisors:null); retval.arrayIndices = (e2!=null?e2.arrayIndices:null);}
 
                     }
                     break;
                 case 3 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:142:11: e3= array_declaration
                     {
                     pushFollow(FOLLOW_array_declaration_in_statement748);
                     e3=array_declaration();
 
                     state._fsp--;
 
 
                     if (!error) {retval.ast = (e3!=null?e3.ast:null); retval.divisors = (e3!=null?e3.divisors:null); retval.arrayIndices = (e3!=null?e3.arrayIndices:null); retval.positive = (e3!=null?e3.positive:null);}
 
                     }
                     break;
                 case 4 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:143:11: e4= assignment
                     {
                     pushFollow(FOLLOW_assignment_in_statement764);
                     e4=assignment();
 
                     state._fsp--;
 
 
                     if (!error) {retval.ast = (e4!=null?e4.ast:null); retval.divisors = (e4!=null?e4.divisors:null); retval.arrayIndices = (e4!=null?e4.arrayIndices:null);}
 
                     }
                     break;
                 case 5 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:144:11: e5= if_statement
                     {
                     pushFollow(FOLLOW_if_statement_in_statement780);
                     e5=if_statement();
 
                     state._fsp--;
 
 
                     if (!error) {retval.ast = (e5!=null?e5.ast:null); retval.divisors = (e5!=null?e5.divisors:null); retval.arrayIndices = (e5!=null?e5.arrayIndices:null);}
 
                     }
                     break;
                 case 6 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:145:11: e6= while_statement
                     {
                     pushFollow(FOLLOW_while_statement_in_statement796);
                     e6=while_statement();
 
                     state._fsp--;
 
 
                     if (!error) {retval.ast = (e6!=null?e6.ast:null); retval.divisors = (e6!=null?e6.divisors:null); retval.arrayIndices = (e6!=null?e6.arrayIndices:null);}
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "statement"
 
 
     public static class invariant_statement_return extends ParserRuleReturnScope {
         public LinkedList<Invariant> result;
     };
 
 
     // $ANTLR start "invariant_statement"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:148:1: invariant_statement returns [ LinkedList<Invariant> result ] : ( 'invariant' e1= quantified_expression ';' | 'invariant' '{' (e2= quantified_expression ';' )+ '}' );
     public final WhileLanguageParser.invariant_statement_return invariant_statement() throws RecognitionException {
         WhileLanguageParser.invariant_statement_return retval = new WhileLanguageParser.invariant_statement_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.quantified_expression_return e1 =null;
 
         WhileLanguageParser.quantified_expression_return e2 =null;
 
 
         retval.result = new LinkedList<Invariant>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:150:9: ( 'invariant' e1= quantified_expression ';' | 'invariant' '{' (e2= quantified_expression ';' )+ '}' )
             int alt18=2;
             int LA18_0 = input.LA(1);
 
             if ( (LA18_0==39) ) {
                 int LA18_1 = input.LA(2);
 
                 if ( (LA18_1==43) ) {
                     alt18=2;
                 }
                 else if ( (LA18_1==BOOL_LITERAL||(LA18_1 >= IDENT && LA18_1 <= QUANTIFIER)||LA18_1==10||LA18_1==14||LA18_1==17||LA18_1==19) ) {
                     alt18=1;
                 }
                 else {
                     NoViableAltException nvae =
                         new NoViableAltException("", 18, 1, input);
 
                     throw nvae;
 
                 }
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 18, 0, input);
 
                 throw nvae;
 
             }
             switch (alt18) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:150:11: 'invariant' e1= quantified_expression ';'
                     {
                     match(input,39,FOLLOW_39_in_invariant_statement833); 
 
                     pushFollow(FOLLOW_quantified_expression_in_invariant_statement837);
                     e1=quantified_expression();
 
                     state._fsp--;
 
 
                     match(input,21,FOLLOW_21_in_invariant_statement839); 
 
                     if (!error) {retval.result.addAll(possibleDivByZeroI((e1!=null?e1.divisors:null)));
                                 retval.result.addAll(possibleArrayOutOfBoundsI((e1!=null?e1.arrayIndices:null)));
                                 retval.result.add(new Invariant(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e1!=null?e1.ast:null)));}
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:153:11: 'invariant' '{' (e2= quantified_expression ';' )+ '}'
                     {
                     match(input,39,FOLLOW_39_in_invariant_statement853); 
 
                     match(input,43,FOLLOW_43_in_invariant_statement855); 
 
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:153:27: (e2= quantified_expression ';' )+
                     int cnt17=0;
                     loop17:
                     do {
                         int alt17=2;
                         int LA17_0 = input.LA(1);
 
                         if ( (LA17_0==BOOL_LITERAL||(LA17_0 >= IDENT && LA17_0 <= QUANTIFIER)||LA17_0==10||LA17_0==14||LA17_0==17||LA17_0==19) ) {
                             alt17=1;
                         }
 
 
                         switch (alt17) {
                     	case 1 :
                     	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:153:28: e2= quantified_expression ';'
                     	    {
                     	    pushFollow(FOLLOW_quantified_expression_in_invariant_statement860);
                     	    e2=quantified_expression();
 
                     	    state._fsp--;
 
 
                     	    match(input,21,FOLLOW_21_in_invariant_statement862); 
 
                     	    if (!error) {retval.result.addAll(possibleDivByZeroI((e2!=null?e2.divisors:null)));
                     	                retval.result.addAll(possibleArrayOutOfBoundsI((e2!=null?e2.arrayIndices:null)));
                     	                retval.result.add(new Invariant(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e2!=null?e2.ast:null)));}
 
                     	    }
                     	    break;
 
                     	default :
                     	    if ( cnt17 >= 1 ) break loop17;
                                 EarlyExitException eee =
                                     new EarlyExitException(17, input);
                                 throw eee;
                         }
                         cnt17++;
                     } while (true);
 
 
                     match(input,45,FOLLOW_45_in_invariant_statement869); 
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "invariant_statement"
 
 
     public static class axiom_statement_return extends ParserRuleReturnScope {
         public LinkedList<Axiom> result;
     };
 
 
     // $ANTLR start "axiom_statement"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:158:1: axiom_statement returns [ LinkedList<Axiom> result ] : 'axiom' e1= quantified_expression ';' ;
     public final WhileLanguageParser.axiom_statement_return axiom_statement() throws RecognitionException {
         WhileLanguageParser.axiom_statement_return retval = new WhileLanguageParser.axiom_statement_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.quantified_expression_return e1 =null;
 
 
         retval.result = new LinkedList<Axiom>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:160:9: ( 'axiom' e1= quantified_expression ';' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:160:11: 'axiom' e1= quantified_expression ';'
             {
             match(input,33,FOLLOW_33_in_axiom_statement907); 
 
             pushFollow(FOLLOW_quantified_expression_in_axiom_statement911);
             e1=quantified_expression();
 
             state._fsp--;
 
 
             match(input,21,FOLLOW_21_in_axiom_statement913); 
 
             if (!error) {retval.result.addAll(possibleDivByZeroAx((e1!=null?e1.divisors:null)));
                         retval.result.addAll(possibleArrayOutOfBoundsAx((e1!=null?e1.arrayIndices:null)));
                         retval.result.add(new Axiom(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e1!=null?e1.ast:null)));}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "axiom_statement"
 
 
     public static class assert_statement_return extends ParserRuleReturnScope {
         public Assertion ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "assert_statement"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:165:1: assert_statement returns [ Assertion ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : 'assert' e1= quantified_expression ';' ;
     public final WhileLanguageParser.assert_statement_return assert_statement() throws RecognitionException {
         WhileLanguageParser.assert_statement_return retval = new WhileLanguageParser.assert_statement_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.quantified_expression_return e1 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:166:9: ( 'assert' e1= quantified_expression ';' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:166:11: 'assert' e1= quantified_expression ';'
             {
             match(input,31,FOLLOW_31_in_assert_statement944); 
 
             pushFollow(FOLLOW_quantified_expression_in_assert_statement948);
             e1=quantified_expression();
 
             state._fsp--;
 
 
             match(input,21,FOLLOW_21_in_assert_statement950); 
 
             if (!error) {retval.ast = new Assertion(
                                 new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e1!=null?e1.ast:null));
                             retval.divisors = (e1!=null?e1.divisors:null);
                             retval.arrayIndices = (e1!=null?e1.arrayIndices:null);}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "assert_statement"
 
 
     public static class assume_statement_return extends ParserRuleReturnScope {
         public LinkedList<Assumption> result;
     };
 
 
     // $ANTLR start "assume_statement"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:172:1: assume_statement returns [ LinkedList<Assumption> result ] : ( 'assume' e1= quantified_expression ';' | 'assume' '{' (e2= quantified_expression ';' )+ '}' );
     public final WhileLanguageParser.assume_statement_return assume_statement() throws RecognitionException {
         WhileLanguageParser.assume_statement_return retval = new WhileLanguageParser.assume_statement_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.quantified_expression_return e1 =null;
 
         WhileLanguageParser.quantified_expression_return e2 =null;
 
 
         retval.result = new LinkedList<Assumption>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:174:9: ( 'assume' e1= quantified_expression ';' | 'assume' '{' (e2= quantified_expression ';' )+ '}' )
             int alt20=2;
             int LA20_0 = input.LA(1);
 
             if ( (LA20_0==32) ) {
                 int LA20_1 = input.LA(2);
 
                 if ( (LA20_1==43) ) {
                     alt20=2;
                 }
                 else if ( (LA20_1==BOOL_LITERAL||(LA20_1 >= IDENT && LA20_1 <= QUANTIFIER)||LA20_1==10||LA20_1==14||LA20_1==17||LA20_1==19) ) {
                     alt20=1;
                 }
                 else {
                     NoViableAltException nvae =
                         new NoViableAltException("", 20, 1, input);
 
                     throw nvae;
 
                 }
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 20, 0, input);
 
                 throw nvae;
 
             }
             switch (alt20) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:174:11: 'assume' e1= quantified_expression ';'
                     {
                     match(input,32,FOLLOW_32_in_assume_statement987); 
 
                     pushFollow(FOLLOW_quantified_expression_in_assume_statement991);
                     e1=quantified_expression();
 
                     state._fsp--;
 
 
                     match(input,21,FOLLOW_21_in_assume_statement993); 
 
                     if (!error) {retval.result.addAll(possibleDivByZeroA((e1!=null?e1.divisors:null)));
                                 retval.result.addAll(possibleArrayOutOfBoundsA((e1!=null?e1.arrayIndices:null)));
                                 retval.result.add(new Assumption(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e1!=null?e1.ast:null)));}
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:177:11: 'assume' '{' (e2= quantified_expression ';' )+ '}'
                     {
                     match(input,32,FOLLOW_32_in_assume_statement1007); 
 
                     match(input,43,FOLLOW_43_in_assume_statement1009); 
 
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:177:24: (e2= quantified_expression ';' )+
                     int cnt19=0;
                     loop19:
                     do {
                         int alt19=2;
                         int LA19_0 = input.LA(1);
 
                         if ( (LA19_0==BOOL_LITERAL||(LA19_0 >= IDENT && LA19_0 <= QUANTIFIER)||LA19_0==10||LA19_0==14||LA19_0==17||LA19_0==19) ) {
                             alt19=1;
                         }
 
 
                         switch (alt19) {
                     	case 1 :
                     	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:177:25: e2= quantified_expression ';'
                     	    {
                     	    pushFollow(FOLLOW_quantified_expression_in_assume_statement1014);
                     	    e2=quantified_expression();
 
                     	    state._fsp--;
 
 
                     	    match(input,21,FOLLOW_21_in_assume_statement1016); 
 
                     	    if (!error) {retval.result.addAll(possibleDivByZeroA((e2!=null?e2.divisors:null)));
                     	                retval.result.addAll(possibleArrayOutOfBoundsA((e2!=null?e2.arrayIndices:null)));
                     	                retval.result.add(new Assumption(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e2!=null?e2.ast:null)));}
 
                     	    }
                     	    break;
 
                     	default :
                     	    if ( cnt19 >= 1 ) break loop19;
                                 EarlyExitException eee =
                                     new EarlyExitException(19, input);
                                 throw eee;
                         }
                         cnt19++;
                     } while (true);
 
 
                     match(input,45,FOLLOW_45_in_assume_statement1023); 
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "assume_statement"
 
 
     public static class ensure_statement_return extends ParserRuleReturnScope {
         public LinkedList<Ensure> result;
     };
 
 
     // $ANTLR start "ensure_statement"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:182:1: ensure_statement returns [ LinkedList<Ensure> result ] : ( 'ensure' e1= quantified_expression ';' | 'ensure' '{' (e2= quantified_expression ';' )+ '}' );
     public final WhileLanguageParser.ensure_statement_return ensure_statement() throws RecognitionException {
         WhileLanguageParser.ensure_statement_return retval = new WhileLanguageParser.ensure_statement_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.quantified_expression_return e1 =null;
 
         WhileLanguageParser.quantified_expression_return e2 =null;
 
 
         retval.result = new LinkedList<Ensure>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:184:9: ( 'ensure' e1= quantified_expression ';' | 'ensure' '{' (e2= quantified_expression ';' )+ '}' )
             int alt22=2;
             int LA22_0 = input.LA(1);
 
             if ( (LA22_0==36) ) {
                 int LA22_1 = input.LA(2);
 
                 if ( (LA22_1==43) ) {
                     alt22=2;
                 }
                 else if ( (LA22_1==BOOL_LITERAL||(LA22_1 >= IDENT && LA22_1 <= QUANTIFIER)||LA22_1==10||LA22_1==14||LA22_1==17||LA22_1==19) ) {
                     alt22=1;
                 }
                 else {
                     NoViableAltException nvae =
                         new NoViableAltException("", 22, 1, input);
 
                     throw nvae;
 
                 }
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 22, 0, input);
 
                 throw nvae;
 
             }
             switch (alt22) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:184:11: 'ensure' e1= quantified_expression ';'
                     {
                     match(input,36,FOLLOW_36_in_ensure_statement1058); 
 
                     pushFollow(FOLLOW_quantified_expression_in_ensure_statement1062);
                     e1=quantified_expression();
 
                     state._fsp--;
 
 
                     match(input,21,FOLLOW_21_in_ensure_statement1064); 
 
                     if (!error) {retval.result.addAll(possibleDivByZeroE((e1!=null?e1.divisors:null)));
                                 retval.result.addAll(possibleArrayOutOfBoundsE((e1!=null?e1.arrayIndices:null)));
                                 retval.result.add(new Ensure(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e1!=null?e1.ast:null)));}
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:187:11: 'ensure' '{' (e2= quantified_expression ';' )+ '}'
                     {
                     match(input,36,FOLLOW_36_in_ensure_statement1078); 
 
                     match(input,43,FOLLOW_43_in_ensure_statement1080); 
 
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:187:24: (e2= quantified_expression ';' )+
                     int cnt21=0;
                     loop21:
                     do {
                         int alt21=2;
                         int LA21_0 = input.LA(1);
 
                         if ( (LA21_0==BOOL_LITERAL||(LA21_0 >= IDENT && LA21_0 <= QUANTIFIER)||LA21_0==10||LA21_0==14||LA21_0==17||LA21_0==19) ) {
                             alt21=1;
                         }
 
 
                         switch (alt21) {
                     	case 1 :
                     	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:187:25: e2= quantified_expression ';'
                     	    {
                     	    pushFollow(FOLLOW_quantified_expression_in_ensure_statement1085);
                     	    e2=quantified_expression();
 
                     	    state._fsp--;
 
 
                     	    match(input,21,FOLLOW_21_in_ensure_statement1087); 
 
                     	    if (!error) {retval.result.addAll(possibleDivByZeroE((e2!=null?e2.divisors:null)));
                     	                retval.result.addAll(possibleArrayOutOfBoundsE((e2!=null?e2.arrayIndices:null)));
                     	                retval.result.add(new Ensure(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e2!=null?e2.ast:null)));}
 
                     	    }
                     	    break;
 
                     	default :
                     	    if ( cnt21 >= 1 ) break loop21;
                                 EarlyExitException eee =
                                     new EarlyExitException(21, input);
                                 throw eee;
                         }
                         cnt21++;
                     } while (true);
 
 
                     match(input,45,FOLLOW_45_in_ensure_statement1094); 
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "ensure_statement"
 
 
     public static class assignment_return extends ParserRuleReturnScope {
         public Assignment ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "assignment"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:192:1: assignment returns [ Assignment ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : ( IDENT '=' expression ';' | IDENT ( '[' e1= expression ']' )+ '=' e2= expression ';' );
     public final WhileLanguageParser.assignment_return assignment() throws RecognitionException {
         WhileLanguageParser.assignment_return retval = new WhileLanguageParser.assignment_return();
         retval.start = input.LT(1);
 
 
         Token IDENT15=null;
         Token IDENT16=null;
         WhileLanguageParser.expression_return e1 =null;
 
         WhileLanguageParser.expression_return e2 =null;
 
         WhileLanguageParser.expression_return expression14 =null;
 
 
         retval.divisors = new LinkedList<Expression>();  LinkedList<Expression> currentIndices = new LinkedList<Expression>();
         	retval.arrayIndices = new LinkedList<Pair<ArrayRead, Expression>>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:195:9: ( IDENT '=' expression ';' | IDENT ( '[' e1= expression ']' )+ '=' e2= expression ';' )
             int alt24=2;
             int LA24_0 = input.LA(1);
 
             if ( (LA24_0==IDENT) ) {
                 int LA24_1 = input.LA(2);
 
                 if ( (LA24_1==24) ) {
                     alt24=1;
                 }
                 else if ( (LA24_1==28) ) {
                     alt24=2;
                 }
                 else {
                     NoViableAltException nvae =
                         new NoViableAltException("", 24, 1, input);
 
                     throw nvae;
 
                 }
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 24, 0, input);
 
                 throw nvae;
 
             }
             switch (alt24) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:195:11: IDENT '=' expression ';'
                     {
                     IDENT15=(Token)match(input,IDENT,FOLLOW_IDENT_in_assignment1132); 
 
                     match(input,24,FOLLOW_24_in_assignment1134); 
 
                     pushFollow(FOLLOW_expression_in_assignment1136);
                     expression14=expression();
 
                     state._fsp--;
 
 
                     match(input,21,FOLLOW_21_in_assignment1138); 
 
                     if (!error) {retval.ast = new Assignment(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
                                     (expression14!=null?expression14.ast:null), new Identifier((IDENT15!=null?IDENT15.getText():null)));
                                 retval.divisors = (expression14!=null?expression14.divisors:null); retval.arrayIndices = (expression14!=null?expression14.arrayIndices:null);}
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:198:11: IDENT ( '[' e1= expression ']' )+ '=' e2= expression ';'
                     {
                     IDENT16=(Token)match(input,IDENT,FOLLOW_IDENT_in_assignment1152); 
 
                     LinkedList<Expression> idx = new LinkedList<Expression>();
 
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:199:10: ( '[' e1= expression ']' )+
                     int cnt23=0;
                     loop23:
                     do {
                         int alt23=2;
                         int LA23_0 = input.LA(1);
 
                         if ( (LA23_0==28) ) {
                             alt23=1;
                         }
 
 
                         switch (alt23) {
                     	case 1 :
                     	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:199:12: '[' e1= expression ']'
                     	    {
                     	    match(input,28,FOLLOW_28_in_assignment1167); 
 
                     	    pushFollow(FOLLOW_expression_in_assignment1171);
                     	    e1=expression();
 
                     	    state._fsp--;
 
 
                     	    if (!error) {idx.add((e1!=null?e1.ast:null));
                     	            	retval.arrayIndices.add(new Pair<ArrayRead, Expression>(new ArrayRead(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
                     	            		new Identifier((IDENT16!=null?IDENT16.getText():null)), currentIndices.toArray(new Expression[currentIndices.size()])), (e1!=null?e1.ast:null)));
                     	                    currentIndices.add((e1!=null?e1.ast:null)); retval.divisors.addAll((e1!=null?e1.divisors:null)); retval.arrayIndices.addAll((e1!=null?e1.arrayIndices:null));}
 
                     	    match(input,29,FOLLOW_29_in_assignment1185); 
 
                     	    }
                     	    break;
 
                     	default :
                     	    if ( cnt23 >= 1 ) break loop23;
                                 EarlyExitException eee =
                                     new EarlyExitException(23, input);
                                 throw eee;
                         }
                         cnt23++;
                     } while (true);
 
 
                     match(input,24,FOLLOW_24_in_assignment1190); 
 
                     pushFollow(FOLLOW_expression_in_assignment1194);
                     e2=expression();
 
                     state._fsp--;
 
 
                     match(input,21,FOLLOW_21_in_assignment1196); 
 
                     if (!error) {retval.ast = new ArrayAssignment(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
                                                (e2!=null?e2.ast:null), new Identifier((IDENT16!=null?IDENT16.getText():null)),
                             				   idx.toArray(new Expression[idx.size()]));
                                 retval.divisors.addAll((e2!=null?e2.divisors:null));
                                 retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "assignment"
 
 
     public static class variable_declaration_return extends ParserRuleReturnScope {
         public VariableDeclaration ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "variable_declaration"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:211:1: variable_declaration returns [ VariableDeclaration ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : type IDENT ( '=' expression )? ';' ;
     public final WhileLanguageParser.variable_declaration_return variable_declaration() throws RecognitionException {
         WhileLanguageParser.variable_declaration_return retval = new WhileLanguageParser.variable_declaration_return();
         retval.start = input.LT(1);
 
 
         Token IDENT18=null;
         WhileLanguageParser.expression_return expression17 =null;
 
         Type type19 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:212:9: ( type IDENT ( '=' expression )? ';' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:212:11: type IDENT ( '=' expression )? ';'
             {
             pushFollow(FOLLOW_type_in_variable_declaration1239);
             type19=type();
 
             state._fsp--;
 
 
             IDENT18=(Token)match(input,IDENT,FOLLOW_IDENT_in_variable_declaration1241); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:212:22: ( '=' expression )?
             int alt25=2;
             int LA25_0 = input.LA(1);
 
             if ( (LA25_0==24) ) {
                 alt25=1;
             }
             switch (alt25) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:212:24: '=' expression
                     {
                     match(input,24,FOLLOW_24_in_variable_declaration1245); 
 
                     pushFollow(FOLLOW_expression_in_variable_declaration1247);
                     expression17=expression();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             match(input,21,FOLLOW_21_in_variable_declaration1252); 
 
             if (!error) {
                     	Expression init = (expression17!=null?expression17.ast:null) != null ? (expression17!=null?expression17.ast:null) : null;
                     	retval.ast = new VariableDeclaration(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (IDENT18!=null?IDENT18.getText():null), init, type19);
                         retval.divisors = (expression17!=null?expression17.ast:null) != null ? (expression17!=null?expression17.divisors:null) : new LinkedList<Expression>();
                         retval.arrayIndices = (expression17!=null?expression17.ast:null) != null ? (expression17!=null?expression17.arrayIndices:null) : null;}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "variable_declaration"
 
 
     public static class array_declaration_return extends ParserRuleReturnScope {
         public ArrayDeclaration ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
         public LinkedList<Expression> positive;
     };
 
 
     // $ANTLR start "array_declaration"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:219:1: array_declaration returns [ ArrayDeclaration ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices,\r\n\t\t\t\tLinkedList<Expression> positive ] : type IDENT '=' array_init ';' ;
     public final WhileLanguageParser.array_declaration_return array_declaration() throws RecognitionException {
         WhileLanguageParser.array_declaration_return retval = new WhileLanguageParser.array_declaration_return();
         retval.start = input.LT(1);
 
 
         Token IDENT21=null;
         WhileLanguageParser.array_init_return array_init20 =null;
 
         Type type22 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:221:9: ( type IDENT '=' array_init ';' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:221:11: type IDENT '=' array_init ';'
             {
             pushFollow(FOLLOW_type_in_array_declaration1283);
             type22=type();
 
             state._fsp--;
 
 
             IDENT21=(Token)match(input,IDENT,FOLLOW_IDENT_in_array_declaration1285); 
 
             match(input,24,FOLLOW_24_in_array_declaration1287); 
 
             pushFollow(FOLLOW_array_init_in_array_declaration1289);
             array_init20=array_init();
 
             state._fsp--;
 
 
             match(input,21,FOLLOW_21_in_array_declaration1291); 
 
             if (!error) {
                     	Expression[] dim = (array_init20!=null?array_init20.dim:null).toArray(new Expression[(array_init20!=null?array_init20.dim:null).size()]);
                     	retval.ast = new ArrayDeclaration(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (IDENT21!=null?IDENT21.getText():null), type22, dim);
                         retval.divisors = (array_init20!=null?array_init20.divisors:null);retval.arrayIndices = (array_init20!=null?array_init20.arrayIndices:null); retval.positive = (array_init20!=null?array_init20.dim:null);}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "array_declaration"
 
 
     public static class array_init_return extends ParserRuleReturnScope {
         public LinkedList<Expression> dim;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "array_init"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:227:1: array_init returns [ LinkedList<Expression> dim, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : 'array' ( '[' expression ']' )+ ;
     public final WhileLanguageParser.array_init_return array_init() throws RecognitionException {
         WhileLanguageParser.array_init_return retval = new WhileLanguageParser.array_init_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.expression_return expression23 =null;
 
 
        retval.divisors = new LinkedList<Expression>();retval.dim = new LinkedList<Expression>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:229:2: ( 'array' ( '[' expression ']' )+ )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:229:4: 'array' ( '[' expression ']' )+
             {
             match(input,30,FOLLOW_30_in_array_init1324); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:229:12: ( '[' expression ']' )+
             int cnt26=0;
             loop26:
             do {
                 int alt26=2;
                 int LA26_0 = input.LA(1);
 
                 if ( (LA26_0==28) ) {
                     alt26=1;
                 }
 
 
                 switch (alt26) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:229:14: '[' expression ']'
             	    {
             	    match(input,28,FOLLOW_28_in_array_init1328); 
 
             	    pushFollow(FOLLOW_expression_in_array_init1330);
             	    expression23=expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.dim.add((expression23!=null?expression23.ast:null)); retval.divisors.addAll((expression23!=null?expression23.divisors:null));
             	            retval.arrayIndices.addAll((expression23!=null?expression23.arrayIndices:null));}
 
             	    match(input,29,FOLLOW_29_in_array_init1334); 
 
             	    }
             	    break;
 
             	default :
             	    if ( cnt26 >= 1 ) break loop26;
                         EarlyExitException eee =
                             new EarlyExitException(26, input);
                         throw eee;
                 }
                 cnt26++;
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "array_init"
 
 
     public static class if_statement_return extends ParserRuleReturnScope {
         public Conditional ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "if_statement"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:233:1: if_statement returns [ Conditional ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : 'if' '(' expression ')' b1= if_body ( 'else' b2= if_body )? ;
     public final WhileLanguageParser.if_statement_return if_statement() throws RecognitionException {
         WhileLanguageParser.if_statement_return retval = new WhileLanguageParser.if_statement_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.if_body_return b1 =null;
 
         WhileLanguageParser.if_body_return b2 =null;
 
         WhileLanguageParser.expression_return expression24 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:234:9: ( 'if' '(' expression ')' b1= if_body ( 'else' b2= if_body )? )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:234:11: 'if' '(' expression ')' b1= if_body ( 'else' b2= if_body )?
             {
             match(input,37,FOLLOW_37_in_if_statement1359); 
 
             match(input,14,FOLLOW_14_in_if_statement1361); 
 
             pushFollow(FOLLOW_expression_in_if_statement1363);
             expression24=expression();
 
             state._fsp--;
 
 
             match(input,15,FOLLOW_15_in_if_statement1365); 
 
             pushFollow(FOLLOW_if_body_in_if_statement1369);
             b1=if_body();
 
             state._fsp--;
 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:234:46: ( 'else' b2= if_body )?
             int alt27=2;
             int LA27_0 = input.LA(1);
 
             if ( (LA27_0==35) ) {
                 alt27=1;
             }
             switch (alt27) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:234:48: 'else' b2= if_body
                     {
                     match(input,35,FOLLOW_35_in_if_statement1373); 
 
                     pushFollow(FOLLOW_if_body_in_if_statement1377);
                     b2=if_body();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             if (!error) {
                     	retval.ast = new Conditional(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (expression24!=null?expression24.ast:null), (b1!=null?b1.ast:null), (b2!=null?b2.ast:null));
                         retval.divisors = (expression24!=null?expression24.divisors:null);retval.arrayIndices = (expression24!=null?expression24.arrayIndices:null);}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "if_statement"
 
 
     public static class while_statement_return extends ParserRuleReturnScope {
         public Loop ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "while_statement"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:239:1: while_statement returns [ Loop ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : 'while' '(' expression ')' loop_body ;
     public final WhileLanguageParser.while_statement_return while_statement() throws RecognitionException {
         WhileLanguageParser.while_statement_return retval = new WhileLanguageParser.while_statement_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.loop_body_return loop_body25 =null;
 
         WhileLanguageParser.expression_return expression26 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:240:9: ( 'while' '(' expression ')' loop_body )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:240:11: 'while' '(' expression ')' loop_body
             {
             match(input,42,FOLLOW_42_in_while_statement1411); 
 
             match(input,14,FOLLOW_14_in_while_statement1413); 
 
             pushFollow(FOLLOW_expression_in_while_statement1415);
             expression26=expression();
 
             state._fsp--;
 
 
             match(input,15,FOLLOW_15_in_while_statement1417); 
 
             pushFollow(FOLLOW_loop_body_in_while_statement1419);
             loop_body25=loop_body();
 
             state._fsp--;
 
 
             if (!error) {
             		Invariant[] i = new Invariant[0];
             		i = (loop_body25!=null?loop_body25.pre:null) != null ? (loop_body25!=null?loop_body25.pre:null).toArray(new Invariant[(loop_body25!=null?loop_body25.pre:null).size()]) : null;
             		Ensure[] e = new Ensure[0];
             		e = (loop_body25!=null?loop_body25.post:null) != null ? (loop_body25!=null?loop_body25.post:null).toArray(new Ensure[(loop_body25!=null?loop_body25.post:null).size()]) : null;
                    	retval.ast = new Loop(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (expression26!=null?expression26.ast:null), (loop_body25!=null?loop_body25.ast:null), i, e);
                     retval.divisors = (expression26!=null?expression26.divisors:null);retval.arrayIndices = (expression26!=null?expression26.arrayIndices:null);}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "while_statement"
 
 
     public static class return_statement_return extends ParserRuleReturnScope {
         public ReturnStatement ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "return_statement"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:249:1: return_statement returns [ ReturnStatement ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : 'return' expression ';' ;
     public final WhileLanguageParser.return_statement_return return_statement() throws RecognitionException {
         WhileLanguageParser.return_statement_return retval = new WhileLanguageParser.return_statement_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.expression_return expression27 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:250:2: ( 'return' expression ';' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:250:4: 'return' expression ';'
             {
             match(input,41,FOLLOW_41_in_return_statement1443); 
 
             pushFollow(FOLLOW_expression_in_return_statement1445);
             expression27=expression();
 
             state._fsp--;
 
 
             match(input,21,FOLLOW_21_in_return_statement1447); 
 
             if (!error) {
                     retval.ast = new ReturnStatement(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (expression27!=null?expression27.ast:null));
                     retval.divisors = (expression27!=null?expression27.divisors:null);retval.arrayIndices = (expression27!=null?expression27.arrayIndices:null);}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "return_statement"
 
 
     public static class quantified_expression_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "quantified_expression"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:255:1: quantified_expression returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : ( QUANTIFIER IDENT '(' ( range )? ')' e= quantified_expression | expression );
     public final WhileLanguageParser.quantified_expression_return quantified_expression() throws RecognitionException {
         WhileLanguageParser.quantified_expression_return retval = new WhileLanguageParser.quantified_expression_return();
         retval.start = input.LT(1);
 
 
         Token QUANTIFIER29=null;
         Token IDENT30=null;
         WhileLanguageParser.quantified_expression_return e =null;
 
         WhileLanguageParser.range_return range28 =null;
 
         WhileLanguageParser.expression_return expression31 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:256:9: ( QUANTIFIER IDENT '(' ( range )? ')' e= quantified_expression | expression )
             int alt29=2;
             int LA29_0 = input.LA(1);
 
             if ( (LA29_0==QUANTIFIER) ) {
                 alt29=1;
             }
             else if ( (LA29_0==BOOL_LITERAL||(LA29_0 >= IDENT && LA29_0 <= INT_LITERAL)||LA29_0==10||LA29_0==14||LA29_0==17||LA29_0==19) ) {
                 alt29=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 29, 0, input);
 
                 throw nvae;
 
             }
             switch (alt29) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:256:11: QUANTIFIER IDENT '(' ( range )? ')' e= quantified_expression
                     {
                     QUANTIFIER29=(Token)match(input,QUANTIFIER,FOLLOW_QUANTIFIER_in_quantified_expression1471); 
 
                     IDENT30=(Token)match(input,IDENT,FOLLOW_IDENT_in_quantified_expression1473); 
 
                     match(input,14,FOLLOW_14_in_quantified_expression1475); 
 
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:256:32: ( range )?
                     int alt28=2;
                     int LA28_0 = input.LA(1);
 
                     if ( (LA28_0==BOOL_LITERAL||(LA28_0 >= IDENT && LA28_0 <= INT_LITERAL)||LA28_0==10||LA28_0==14||LA28_0==17||LA28_0==19) ) {
                         alt28=1;
                     }
                     switch (alt28) {
                         case 1 :
                             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:256:32: range
                             {
                             pushFollow(FOLLOW_range_in_quantified_expression1477);
                             range28=range();
 
                             state._fsp--;
 
 
                             }
                             break;
 
                     }
 
 
                     match(input,15,FOLLOW_15_in_quantified_expression1480); 
 
                     pushFollow(FOLLOW_quantified_expression_in_quantified_expression1484);
                     e=quantified_expression();
 
                     state._fsp--;
 
 
                     if (!error) {
                             	Range r = null;
                             	retval.divisors = (e!=null?e.divisors:null);
                                 retval.arrayIndices = (e!=null?e.arrayIndices:null);
                             	if ((range28!=null?range28.e1:null) != null) {
                             		r = new Range((range28!=null?range28.e1:null), (range28!=null?range28.e2:null));
                             		retval.divisors.addAll((range28!=null?range28.divisors:null));
                                     retval.arrayIndices.addAll((range28!=null?range28.arrayIndices:null));
                             	}
                             	if ("forall".equals((QUANTIFIER29!=null?QUANTIFIER29.getText():null)))
                             			retval.ast = new ForAllQuantifier(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), r, new Identifier((IDENT30!=null?IDENT30.getText():null)),
                             				(e!=null?e.ast:null));
                                 else retval.ast = new ExistsQuantifier(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), r, new Identifier((IDENT30!=null?IDENT30.getText():null)),
                             				(e!=null?e.ast:null));
                             	}
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:271:11: expression
                     {
                     pushFollow(FOLLOW_expression_in_quantified_expression1498);
                     expression31=expression();
 
                     state._fsp--;
 
 
                     if (!error) {
                                 retval.divisors = (expression31!=null?expression31.divisors:null);
                             	retval.ast = (expression31!=null?expression31.ast:null);
                                 retval.arrayIndices = (expression31!=null?expression31.arrayIndices:null);
                             }
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "quantified_expression"
 
 
     public static class range_return extends ParserRuleReturnScope {
         public Expression e1;
         public Expression e2;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "range"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:278:1: range returns [ Expression e1, Expression e2, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : l= expression ',' u= expression ;
     public final WhileLanguageParser.range_return range() throws RecognitionException {
         WhileLanguageParser.range_return retval = new WhileLanguageParser.range_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.expression_return l =null;
 
         WhileLanguageParser.expression_return u =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:279:9: (l= expression ',' u= expression )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:279:11: l= expression ',' u= expression
             {
             pushFollow(FOLLOW_expression_in_range1531);
             l=expression();
 
             state._fsp--;
 
 
             match(input,18,FOLLOW_18_in_range1533); 
 
             pushFollow(FOLLOW_expression_in_range1537);
             u=expression();
 
             state._fsp--;
 
 
             if (!error) {
                     	retval.e1 =l.ast; retval.e2 =u.ast;
                     	retval.divisors = l.divisors;
                     	retval.divisors.addAll(u.divisors);
                         retval.arrayIndices = (l!=null?l.arrayIndices:null);
                         retval.arrayIndices.addAll((u!=null?u.arrayIndices:null));}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "range"
 
 
     public static class expression_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "expression"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:287:1: expression returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : e1= rel_expression ( '==' e2= rel_expression | '!=' e2= rel_expression )* ;
     public final WhileLanguageParser.expression_return expression() throws RecognitionException {
         WhileLanguageParser.expression_return retval = new WhileLanguageParser.expression_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.rel_expression_return e1 =null;
 
         WhileLanguageParser.rel_expression_return e2 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:288:9: (e1= rel_expression ( '==' e2= rel_expression | '!=' e2= rel_expression )* )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:288:11: e1= rel_expression ( '==' e2= rel_expression | '!=' e2= rel_expression )*
             {
             pushFollow(FOLLOW_rel_expression_in_expression1570);
             e1=rel_expression();
 
             state._fsp--;
 
 
             if (!error) {retval.ast = e1.ast; retval.divisors = (e1!=null?e1.divisors:null); retval.arrayIndices = (e1!=null?e1.arrayIndices:null);}
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:288:120: ( '==' e2= rel_expression | '!=' e2= rel_expression )*
             loop30:
             do {
                 int alt30=3;
                 int LA30_0 = input.LA(1);
 
                 if ( (LA30_0==25) ) {
                     alt30=1;
                 }
                 else if ( (LA30_0==11) ) {
                     alt30=2;
                 }
 
 
                 switch (alt30) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:289:12: '==' e2= rel_expression
             	    {
             	    match(input,25,FOLLOW_25_in_expression1587); 
 
             	    pushFollow(FOLLOW_rel_expression_in_expression1591);
             	    e2=rel_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new LogicalExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new Equal());
             	            	  	retval.divisors.addAll(e2.divisors);retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
             	case 2 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:292:12: '!=' e2= rel_expression
             	    {
             	    match(input,11,FOLLOW_11_in_expression1606); 
 
             	    pushFollow(FOLLOW_rel_expression_in_expression1610);
             	    e2=rel_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new LogicalExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new NotEqual());
             	            		retval.divisors.addAll(e2.divisors);retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
 
             	default :
             	    break loop30;
                 }
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "expression"
 
 
     public static class rel_expression_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "rel_expression"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:299:1: rel_expression returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : e1= add_expression ( '<' e2= add_expression | '<=' e2= add_expression | '>' e2= add_expression | '>=' e2= add_expression )* ;
     public final WhileLanguageParser.rel_expression_return rel_expression() throws RecognitionException {
         WhileLanguageParser.rel_expression_return retval = new WhileLanguageParser.rel_expression_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.add_expression_return e1 =null;
 
         WhileLanguageParser.add_expression_return e2 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:300:9: (e1= add_expression ( '<' e2= add_expression | '<=' e2= add_expression | '>' e2= add_expression | '>=' e2= add_expression )* )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:300:11: e1= add_expression ( '<' e2= add_expression | '<=' e2= add_expression | '>' e2= add_expression | '>=' e2= add_expression )*
             {
             pushFollow(FOLLOW_add_expression_in_rel_expression1655);
             e1=add_expression();
 
             state._fsp--;
 
 
             if (!error) {retval.ast = e1.ast; retval.divisors = (e1!=null?e1.divisors:null); retval.arrayIndices = (e1!=null?e1.arrayIndices:null);}
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:300:120: ( '<' e2= add_expression | '<=' e2= add_expression | '>' e2= add_expression | '>=' e2= add_expression )*
             loop31:
             do {
                 int alt31=5;
                 switch ( input.LA(1) ) {
                 case 22:
                     {
                     alt31=1;
                     }
                     break;
                 case 23:
                     {
                     alt31=2;
                     }
                     break;
                 case 26:
                     {
                     alt31=3;
                     }
                     break;
                 case 27:
                     {
                     alt31=4;
                     }
                     break;
 
                 }
 
                 switch (alt31) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:301:12: '<' e2= add_expression
             	    {
             	    match(input,22,FOLLOW_22_in_rel_expression1672); 
 
             	    pushFollow(FOLLOW_add_expression_in_rel_expression1677);
             	    e2=add_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new LogicalExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new Less());
             	            	  	retval.divisors.addAll(e2.divisors);retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
             	case 2 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:304:12: '<=' e2= add_expression
             	    {
             	    match(input,23,FOLLOW_23_in_rel_expression1692); 
 
             	    pushFollow(FOLLOW_add_expression_in_rel_expression1696);
             	    e2=add_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new LogicalExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new LessEqual());
             	            		retval.divisors.addAll(e2.divisors);retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
             	case 3 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:307:12: '>' e2= add_expression
             	    {
             	    match(input,26,FOLLOW_26_in_rel_expression1711); 
 
             	    pushFollow(FOLLOW_add_expression_in_rel_expression1716);
             	    e2=add_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new LogicalExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new Greater());
             	            		retval.divisors.addAll(e2.divisors);retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
             	case 4 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:310:12: '>=' e2= add_expression
             	    {
             	    match(input,27,FOLLOW_27_in_rel_expression1731); 
 
             	    pushFollow(FOLLOW_add_expression_in_rel_expression1735);
             	    e2=add_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new LogicalExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new GreaterEqual());
             	            		retval.divisors.addAll(e2.divisors);retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
 
             	default :
             	    break loop31;
                 }
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "rel_expression"
 
 
     public static class add_expression_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "add_expression"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:316:1: add_expression returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : e1= mul_expression ( '|' e2= mul_expression | '+' e2= mul_expression | '-' e2= mul_expression )* ;
     public final WhileLanguageParser.add_expression_return add_expression() throws RecognitionException {
         WhileLanguageParser.add_expression_return retval = new WhileLanguageParser.add_expression_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.mul_expression_return e1 =null;
 
         WhileLanguageParser.mul_expression_return e2 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:317:9: (e1= mul_expression ( '|' e2= mul_expression | '+' e2= mul_expression | '-' e2= mul_expression )* )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:317:11: e1= mul_expression ( '|' e2= mul_expression | '+' e2= mul_expression | '-' e2= mul_expression )*
             {
             pushFollow(FOLLOW_mul_expression_in_add_expression1779);
             e1=mul_expression();
 
             state._fsp--;
 
 
             if (!error) {retval.ast = e1.ast; retval.divisors = (e1!=null?e1.divisors:null); retval.arrayIndices = (e1!=null?e1.arrayIndices:null);}
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:317:120: ( '|' e2= mul_expression | '+' e2= mul_expression | '-' e2= mul_expression )*
             loop32:
             do {
                 int alt32=4;
                 switch ( input.LA(1) ) {
                 case 44:
                     {
                     alt32=1;
                     }
                     break;
                 case 17:
                     {
                     alt32=2;
                     }
                     break;
                 case 19:
                     {
                     alt32=3;
                     }
                     break;
 
                 }
 
                 switch (alt32) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:318:12: '|' e2= mul_expression
             	    {
             	    match(input,44,FOLLOW_44_in_add_expression1796); 
 
             	    pushFollow(FOLLOW_mul_expression_in_add_expression1800);
             	    e2=mul_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new LogicalExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new Disjunction());
             	            	  	retval.divisors.addAll(e2.divisors);retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
             	case 2 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:321:12: '+' e2= mul_expression
             	    {
             	    match(input,17,FOLLOW_17_in_add_expression1815); 
 
             	    pushFollow(FOLLOW_mul_expression_in_add_expression1819);
             	    e2=mul_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new ArithmeticExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new Addition());
             	            		retval.divisors.addAll(e2.divisors);retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
             	case 3 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:324:12: '-' e2= mul_expression
             	    {
             	    match(input,19,FOLLOW_19_in_add_expression1834); 
 
             	    pushFollow(FOLLOW_mul_expression_in_add_expression1838);
             	    e2=mul_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new ArithmeticExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new Subtraction());
             	            		retval.divisors.addAll(e2.divisors);retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
 
             	default :
             	    break loop32;
                 }
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "add_expression"
 
 
     public static class mul_expression_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "mul_expression"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:330:1: mul_expression returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : e1= unary_expression ( '&' e2= unary_expression | '*' e2= unary_expression | '/' e2= unary_expression | '%' e2= unary_expression )* ;
     public final WhileLanguageParser.mul_expression_return mul_expression() throws RecognitionException {
         WhileLanguageParser.mul_expression_return retval = new WhileLanguageParser.mul_expression_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.unary_expression_return e1 =null;
 
         WhileLanguageParser.unary_expression_return e2 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:331:9: (e1= unary_expression ( '&' e2= unary_expression | '*' e2= unary_expression | '/' e2= unary_expression | '%' e2= unary_expression )* )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:331:11: e1= unary_expression ( '&' e2= unary_expression | '*' e2= unary_expression | '/' e2= unary_expression | '%' e2= unary_expression )*
             {
             pushFollow(FOLLOW_unary_expression_in_mul_expression1882);
             e1=unary_expression();
 
             state._fsp--;
 
 
             if (!error) {retval.ast = e1.ast; retval.divisors = (e1!=null?e1.divisors:null); retval.arrayIndices = (e1!=null?e1.arrayIndices:null);}
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:331:122: ( '&' e2= unary_expression | '*' e2= unary_expression | '/' e2= unary_expression | '%' e2= unary_expression )*
             loop33:
             do {
                 int alt33=5;
                 switch ( input.LA(1) ) {
                 case 13:
                     {
                     alt33=1;
                     }
                     break;
                 case 16:
                     {
                     alt33=2;
                     }
                     break;
                 case 20:
                     {
                     alt33=3;
                     }
                     break;
                 case 12:
                     {
                     alt33=4;
                     }
                     break;
 
                 }
 
                 switch (alt33) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:332:12: '&' e2= unary_expression
             	    {
             	    match(input,13,FOLLOW_13_in_mul_expression1899); 
 
             	    pushFollow(FOLLOW_unary_expression_in_mul_expression1903);
             	    e2=unary_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new LogicalExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new Conjunction());
             	            	  	retval.divisors.addAll(e2.divisors);
             	                    retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
             	case 2 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:336:12: '*' e2= unary_expression
             	    {
             	    match(input,16,FOLLOW_16_in_mul_expression1918); 
 
             	    pushFollow(FOLLOW_unary_expression_in_mul_expression1922);
             	    e2=unary_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new ArithmeticExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new Multiplication());
             	            		retval.divisors.addAll(e2.divisors);
             	                    retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
             	case 3 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:340:12: '/' e2= unary_expression
             	    {
             	    match(input,20,FOLLOW_20_in_mul_expression1937); 
 
             	    pushFollow(FOLLOW_unary_expression_in_mul_expression1941);
             	    e2=unary_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.ast = new ArithmeticExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	                        retval.ast, (e2!=null?e2.ast:null), new Division());
             	            		retval.divisors.addAll(e2.divisors);
             	            		retval.divisors.add((e2!=null?e2.ast:null));
             	                    retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
             	case 4 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:345:12: '%' e2= unary_expression
             	    {
             	    match(input,12,FOLLOW_12_in_mul_expression1956); 
 
             	    pushFollow(FOLLOW_unary_expression_in_mul_expression1960);
             	    e2=unary_expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {
             	            		retval.ast = new ArithmeticExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), retval.ast, (e2!=null?e2.ast:null), new Modulo());
             	            		retval.divisors.addAll(e2.divisors);
             	            		retval.divisors.add((e2!=null?e2.ast:null));
             	                    retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
 
             	default :
             	    break loop33;
                 }
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "mul_expression"
 
 
     public static class unary_expression_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "unary_expression"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:353:1: unary_expression returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : ( '!' e= parenthesized_expression | '-' e= parenthesized_expression | ( '+' )? e= parenthesized_expression );
     public final WhileLanguageParser.unary_expression_return unary_expression() throws RecognitionException {
         WhileLanguageParser.unary_expression_return retval = new WhileLanguageParser.unary_expression_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.parenthesized_expression_return e =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:354:9: ( '!' e= parenthesized_expression | '-' e= parenthesized_expression | ( '+' )? e= parenthesized_expression )
             int alt35=3;
             switch ( input.LA(1) ) {
             case 10:
                 {
                 alt35=1;
                 }
                 break;
             case 19:
                 {
                 alt35=2;
                 }
                 break;
             case BOOL_LITERAL:
             case IDENT:
             case INT_LITERAL:
             case 14:
             case 17:
                 {
                 alt35=3;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 35, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt35) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:354:11: '!' e= parenthesized_expression
                     {
                     match(input,10,FOLLOW_10_in_unary_expression2002); 
 
                     pushFollow(FOLLOW_parenthesized_expression_in_unary_expression2007);
                     e=parenthesized_expression();
 
                     state._fsp--;
 
 
                     if (!error) {
                             	retval.ast = new LogicalExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e!=null?e.ast:null), null, new Negation());
                             	retval.divisors = e.divisors; retval.arrayIndices = (e!=null?e.arrayIndices:null);}
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:357:11: '-' e= parenthesized_expression
                     {
                     match(input,19,FOLLOW_19_in_unary_expression2021); 
 
                     pushFollow(FOLLOW_parenthesized_expression_in_unary_expression2026);
                     e=parenthesized_expression();
 
                     state._fsp--;
 
 
                     if (!error) {
                             	retval.ast = new ArithmeticExpression(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()), (e!=null?e.ast:null), null, new UnaryMinus());
                             	retval.divisors = e.divisors; retval.arrayIndices = (e!=null?e.arrayIndices:null);}
 
                     }
                     break;
                 case 3 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:360:11: ( '+' )? e= parenthesized_expression
                     {
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:360:11: ( '+' )?
                     int alt34=2;
                     int LA34_0 = input.LA(1);
 
                     if ( (LA34_0==17) ) {
                         alt34=1;
                     }
                     switch (alt34) {
                         case 1 :
                             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:360:11: '+'
                             {
                             match(input,17,FOLLOW_17_in_unary_expression2040); 
 
                             }
                             break;
 
                     }
 
 
                     pushFollow(FOLLOW_parenthesized_expression_in_unary_expression2045);
                     e=parenthesized_expression();
 
                     state._fsp--;
 
 
                     if (!error) {
                             	retval.ast = (e!=null?e.ast:null);
                             	retval.divisors = e.divisors;
                                 retval.arrayIndices = (e!=null?e.arrayIndices:null);}
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "unary_expression"
 
 
     public static class parenthesized_expression_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "parenthesized_expression"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:366:1: parenthesized_expression returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : ( '(' expression ')' | function_call | array_read | IDENT | literal_expression );
     public final WhileLanguageParser.parenthesized_expression_return parenthesized_expression() throws RecognitionException {
         WhileLanguageParser.parenthesized_expression_return retval = new WhileLanguageParser.parenthesized_expression_return();
         retval.start = input.LT(1);
 
 
         Token IDENT35=null;
         WhileLanguageParser.expression_return expression32 =null;
 
         WhileLanguageParser.function_call_return function_call33 =null;
 
         WhileLanguageParser.array_read_return array_read34 =null;
 
         WhileLanguageParser.literal_expression_return literal_expression36 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:367:9: ( '(' expression ')' | function_call | array_read | IDENT | literal_expression )
             int alt36=5;
             switch ( input.LA(1) ) {
             case 14:
                 {
                 alt36=1;
                 }
                 break;
             case IDENT:
                 {
                 switch ( input.LA(2) ) {
                 case 14:
                     {
                     alt36=2;
                     }
                     break;
                 case 28:
                     {
                     alt36=3;
                     }
                     break;
                 case EOF:
                 case 11:
                 case 12:
                 case 13:
                 case 15:
                 case 16:
                 case 17:
                 case 18:
                 case 19:
                 case 20:
                 case 21:
                 case 22:
                 case 23:
                 case 25:
                 case 26:
                 case 27:
                 case 29:
                 case 44:
                     {
                     alt36=4;
                     }
                     break;
                 default:
                     NoViableAltException nvae =
                         new NoViableAltException("", 36, 2, input);
 
                     throw nvae;
 
                 }
 
                 }
                 break;
             case BOOL_LITERAL:
             case INT_LITERAL:
                 {
                 alt36=5;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 36, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt36) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:367:11: '(' expression ')'
                     {
                     match(input,14,FOLLOW_14_in_parenthesized_expression2076); 
 
                     pushFollow(FOLLOW_expression_in_parenthesized_expression2078);
                     expression32=expression();
 
                     state._fsp--;
 
 
                     match(input,15,FOLLOW_15_in_parenthesized_expression2080); 
 
                     if (!error) {retval.ast = (expression32!=null?expression32.ast:null); retval.divisors = (expression32!=null?expression32.divisors:null);
                                     retval.arrayIndices = (expression32!=null?expression32.arrayIndices:null);}
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:369:11: function_call
                     {
                     pushFollow(FOLLOW_function_call_in_parenthesized_expression2094);
                     function_call33=function_call();
 
                     state._fsp--;
 
 
                     if (!error) {retval.ast = (function_call33!=null?function_call33.ast:null); retval.divisors = (function_call33!=null?function_call33.divisors:null);
                                                 retval.arrayIndices = (function_call33!=null?function_call33.arrayIndices:null);}
 
                     }
                     break;
                 case 3 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:371:11: array_read
                     {
                     pushFollow(FOLLOW_array_read_in_parenthesized_expression2108);
                     array_read34=array_read();
 
                     state._fsp--;
 
 
                     if (!error) {retval.ast = (array_read34!=null?array_read34.ast:null); retval.divisors = (array_read34!=null?array_read34.divisors:null);
                                             retval.arrayIndices = (array_read34!=null?array_read34.arrayIndices:null);}
 
                     }
                     break;
                 case 4 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:373:11: IDENT
                     {
                     IDENT35=(Token)match(input,IDENT,FOLLOW_IDENT_in_parenthesized_expression2122); 
 
                     if (!error) {retval.ast = new VariableRead(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
                                 new Identifier((IDENT35!=null?IDENT35.getText():null))); retval.divisors = new LinkedList<Expression>();
                                 retval.arrayIndices = new LinkedList<Pair<ArrayRead, Expression>>();}
 
                     }
                     break;
                 case 5 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:376:11: literal_expression
                     {
                     pushFollow(FOLLOW_literal_expression_in_parenthesized_expression2136);
                     literal_expression36=literal_expression();
 
                     state._fsp--;
 
 
                     if (!error) {retval.ast = (literal_expression36!=null?literal_expression36.ast:null); retval.divisors = (literal_expression36!=null?literal_expression36.divisors:null);
                                 retval.arrayIndices = (literal_expression36!=null?literal_expression36.arrayIndices:null);}
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "parenthesized_expression"
 
 
     public static class function_call_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "function_call"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:380:1: function_call returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : IDENT '(' ( arglist )? ')' ;
     public final WhileLanguageParser.function_call_return function_call() throws RecognitionException {
         WhileLanguageParser.function_call_return retval = new WhileLanguageParser.function_call_return();
         retval.start = input.LT(1);
 
 
         Token IDENT38=null;
         WhileLanguageParser.arglist_return arglist37 =null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:381:9: ( IDENT '(' ( arglist )? ')' )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:381:11: IDENT '(' ( arglist )? ')'
             {
             IDENT38=(Token)match(input,IDENT,FOLLOW_IDENT_in_function_call2167); 
 
             match(input,14,FOLLOW_14_in_function_call2169); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:381:21: ( arglist )?
             int alt37=2;
             int LA37_0 = input.LA(1);
 
             if ( (LA37_0==BOOL_LITERAL||(LA37_0 >= IDENT && LA37_0 <= INT_LITERAL)||LA37_0==10||LA37_0==14||LA37_0==17||LA37_0==19) ) {
                 alt37=1;
             }
             switch (alt37) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:381:21: arglist
                     {
                     pushFollow(FOLLOW_arglist_in_function_call2171);
                     arglist37=arglist();
 
                     state._fsp--;
 
 
                     }
                     break;
 
             }
 
 
             match(input,15,FOLLOW_15_in_function_call2174); 
 
             if (!error) {
                     	Expression[] params = new Expression[0];
                     	if ((arglist37!=null?arglist37.params:null) != null) params = (arglist37!=null?arglist37.params:null).toArray(new Expression[(arglist37!=null?arglist37.params:null).size()]);
                     	retval.ast = new FunctionCall(new Identifier((IDENT38!=null?IDENT38.getText():null)), params , new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()));
                     	retval.divisors = (arglist37!=null?arglist37.divisors:null);
                         retval.arrayIndices = (arglist37!=null?arglist37.arrayIndices:null);}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "function_call"
 
 
     public static class arglist_return extends ParserRuleReturnScope {
         public LinkedList<Expression> params;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "arglist"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:389:1: arglist returns [ LinkedList<Expression> params, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : e1= expression ( ',' e2= expression )* ;
     public final WhileLanguageParser.arglist_return arglist() throws RecognitionException {
         WhileLanguageParser.arglist_return retval = new WhileLanguageParser.arglist_return();
         retval.start = input.LT(1);
 
 
         WhileLanguageParser.expression_return e1 =null;
 
         WhileLanguageParser.expression_return e2 =null;
 
 
         retval.params = new LinkedList<Expression>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:391:9: (e1= expression ( ',' e2= expression )* )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:391:11: e1= expression ( ',' e2= expression )*
             {
             pushFollow(FOLLOW_expression_in_arglist2213);
             e1=expression();
 
             state._fsp--;
 
 
             if (!error) {retval.params.add((Expression) e1.ast); retval.divisors = (e1!=null?e1.divisors:null); retval.arrayIndices = (e1!=null?e1.arrayIndices:null);}
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:392:10: ( ',' e2= expression )*
             loop38:
             do {
                 int alt38=2;
                 int LA38_0 = input.LA(1);
 
                 if ( (LA38_0==18) ) {
                     alt38=1;
                 }
 
 
                 switch (alt38) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:392:12: ',' e2= expression
             	    {
             	    match(input,18,FOLLOW_18_in_arglist2228); 
 
             	    pushFollow(FOLLOW_expression_in_arglist2232);
             	    e2=expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {retval.params.add((Expression) (e2!=null?e2.ast:null)); retval.divisors.addAll((e2!=null?e2.divisors:null));
             	                                    retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));}
 
             	    }
             	    break;
 
             	default :
             	    break loop38;
                 }
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "arglist"
 
 
     public static class array_read_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "array_read"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:396:1: array_read returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : IDENT '[' e1= expression ']' ( '[' e2= expression ']' )* ;
     public final WhileLanguageParser.array_read_return array_read() throws RecognitionException {
         WhileLanguageParser.array_read_return retval = new WhileLanguageParser.array_read_return();
         retval.start = input.LT(1);
 
 
         Token IDENT39=null;
         WhileLanguageParser.expression_return e1 =null;
 
         WhileLanguageParser.expression_return e2 =null;
 
 
         LinkedList<Expression> l = new LinkedList<Expression>(); LinkedList<Expression> currentIndices = new LinkedList<Expression>();
         	retval.arrayIndices = new LinkedList<Pair<ArrayRead, Expression>>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:399:9: ( IDENT '[' e1= expression ']' ( '[' e2= expression ']' )* )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:399:11: IDENT '[' e1= expression ']' ( '[' e2= expression ']' )*
             {
             IDENT39=(Token)match(input,IDENT,FOLLOW_IDENT_in_array_read2272); 
 
             match(input,28,FOLLOW_28_in_array_read2274); 
 
             pushFollow(FOLLOW_expression_in_array_read2278);
             e1=expression();
 
             state._fsp--;
 
 
             if (!error) {l.add((e1!=null?e1.ast:null));
                     	retval.arrayIndices.add(new Pair<ArrayRead, Expression>(new ArrayRead(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
                     		new Identifier((IDENT39!=null?IDENT39.getText():null)), currentIndices.toArray(new Expression[currentIndices.size()])), (e1!=null?e1.ast:null)));
                             currentIndices.add((e1!=null?e1.ast:null)); retval.arrayIndices.addAll((e1!=null?e1.arrayIndices:null));
                     		retval.divisors = (e1!=null?e1.divisors:null);}
 
             match(input,29,FOLLOW_29_in_array_read2282); 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:404:10: ( '[' e2= expression ']' )*
             loop39:
             do {
                 int alt39=2;
                 int LA39_0 = input.LA(1);
 
                 if ( (LA39_0==28) ) {
                     alt39=1;
                 }
 
 
                 switch (alt39) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:404:12: '[' e2= expression ']'
             	    {
             	    match(input,28,FOLLOW_28_in_array_read2295); 
 
             	    pushFollow(FOLLOW_expression_in_array_read2299);
             	    e2=expression();
 
             	    state._fsp--;
 
 
             	    if (!error) {l.add((e2!=null?e2.ast:null));
             	            	retval.arrayIndices.add(new Pair<ArrayRead, Expression>(new ArrayRead(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
             	            		new Identifier((IDENT39!=null?IDENT39.getText():null)), currentIndices.toArray(new Expression[currentIndices.size()])), (e2!=null?e2.ast:null)));
             	                    currentIndices.add((e2!=null?e2.ast:null)); retval.arrayIndices.addAll((e2!=null?e2.arrayIndices:null));
             	            		retval.divisors.addAll((e2!=null?e2.divisors:null));}
 
             	    match(input,29,FOLLOW_29_in_array_read2303); 
 
             	    }
             	    break;
 
             	default :
             	    break loop39;
                 }
             } while (true);
 
 
             if (!error) {retval.ast = new ArrayRead(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
                         new Identifier((IDENT39!=null?IDENT39.getText():null)), l.toArray(new Expression[l.size()]));}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "array_read"
 
 
     public static class literal_expression_return extends ParserRuleReturnScope {
         public Expression ast;
         public LinkedList<Expression> divisors;
         public LinkedList<Pair<ArrayRead, Expression>> arrayIndices;
     };
 
 
     // $ANTLR start "literal_expression"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:413:1: literal_expression returns [ Expression ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices ] : ( INT_LITERAL | BOOL_LITERAL );
     public final WhileLanguageParser.literal_expression_return literal_expression() throws RecognitionException {
         WhileLanguageParser.literal_expression_return retval = new WhileLanguageParser.literal_expression_return();
         retval.start = input.LT(1);
 
 
         Token INT_LITERAL40=null;
         Token BOOL_LITERAL41=null;
 
         retval.divisors = new LinkedList<Expression>(); retval.arrayIndices = new LinkedList<Pair<ArrayRead, Expression>>();
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:415:9: ( INT_LITERAL | BOOL_LITERAL )
             int alt40=2;
             int LA40_0 = input.LA(1);
 
             if ( (LA40_0==INT_LITERAL) ) {
                 alt40=1;
             }
             else if ( (LA40_0==BOOL_LITERAL) ) {
                 alt40=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 40, 0, input);
 
                 throw nvae;
 
             }
             switch (alt40) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:415:11: INT_LITERAL
                     {
                     INT_LITERAL40=(Token)match(input,INT_LITERAL,FOLLOW_INT_LITERAL_in_literal_expression2354); 
 
                     if (!error) {retval.ast = new NumericLiteral(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
                                 (INT_LITERAL40!=null?INT_LITERAL40.getText():null));}
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:417:11: BOOL_LITERAL
                     {
                     BOOL_LITERAL41=(Token)match(input,BOOL_LITERAL,FOLLOW_BOOL_LITERAL_in_literal_expression2368); 
 
                     if (!error) {retval.ast = new BooleanLiteral(new Position(((Token)retval.start).getLine(), ((Token)retval.start).getCharPositionInLine()),
                                 (BOOL_LITERAL41!=null?BOOL_LITERAL41.getText():null));}
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "literal_expression"
 
 
 
     // $ANTLR start "type"
     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:421:1: type returns [ Type ast ] : ( 'int' | 'bool' ) ( '[' ']' )* ;
     public final Type type() throws RecognitionException {
         Type ast = null;
 
 
         try {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:422:9: ( ( 'int' | 'bool' ) ( '[' ']' )* )
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:422:11: ( 'int' | 'bool' ) ( '[' ']' )*
             {
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:422:11: ( 'int' | 'bool' )
             int alt41=2;
             int LA41_0 = input.LA(1);
 
             if ( (LA41_0==38) ) {
                 alt41=1;
             }
             else if ( (LA41_0==34) ) {
                 alt41=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 41, 0, input);
 
                 throw nvae;
 
             }
             switch (alt41) {
                 case 1 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:422:12: 'int'
                     {
                     match(input,38,FOLLOW_38_in_type2400); 
 
                     ast = error ? null : new IntegerType();
 
                     }
                     break;
                 case 2 :
                     // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:423:11: 'bool'
                     {
                     match(input,34,FOLLOW_34_in_type2414); 
 
                     ast = error ? null : new BooleanType();
 
                     }
                     break;
 
             }
 
 
             // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:423:61: ( '[' ']' )*
             loop42:
             do {
                 int alt42=2;
                 int LA42_0 = input.LA(1);
 
                 if ( (LA42_0==28) ) {
                     alt42=1;
                 }
 
 
                 switch (alt42) {
             	case 1 :
             	    // C:\\Users\\simon\\Studium\\PSE\\src\\grammar\\WhileLanguage.g:423:63: '[' ']'
             	    {
             	    match(input,28,FOLLOW_28_in_type2420); 
 
             	    match(input,29,FOLLOW_29_in_type2422); 
 
             	    ast = error ? null : new ArrayType(ast);
 
             	    }
             	    break;
 
             	default :
             	    break loop42;
                 }
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
         return ast;
     }
     // $ANTLR end "type"
 
     // Delegated rules
 
 
     protected DFA16 dfa16 = new DFA16(this);
     static final String DFA16_eotS =
         "\15\uffff";
     static final String DFA16_eofS =
         "\15\uffff";
     static final String DFA16_minS =
         "\1\6\1\uffff\2\6\3\uffff\1\35\1\25\1\6\1\4\2\uffff";
     static final String DFA16_maxS =
         "\1\52\1\uffff\2\34\3\uffff\1\35\1\30\1\34\1\36\2\uffff";
     static final String DFA16_acceptS =
         "\1\uffff\1\1\2\uffff\1\4\1\5\1\6\4\uffff\1\2\1\3";
     static final String DFA16_specialS =
         "\15\uffff}>";
     static final String[] DFA16_transitionS = {
             "\1\4\30\uffff\1\1\2\uffff\1\3\2\uffff\1\5\1\2\3\uffff\1\6",
             "",
             "\1\10\25\uffff\1\7",
             "\1\10\25\uffff\1\7",
             "",
             "",
             "",
             "\1\11",
             "\1\13\2\uffff\1\12",
             "\1\10\25\uffff\1\7",
             "\1\13\1\uffff\2\13\2\uffff\1\13\3\uffff\1\13\2\uffff\1\13\1"+
             "\uffff\1\13\12\uffff\1\14",
             "",
             ""
     };
 
     static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
     static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
     static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
     static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
     static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
     static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
     static final short[][] DFA16_transition;
 
     static {
         int numStates = DFA16_transitionS.length;
         DFA16_transition = new short[numStates][];
         for (int i=0; i<numStates; i++) {
             DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
         }
     }
 
     class DFA16 extends DFA {
 
         public DFA16(BaseRecognizer recognizer) {
             this.recognizer = recognizer;
             this.decisionNumber = 16;
             this.eot = DFA16_eot;
             this.eof = DFA16_eof;
             this.min = DFA16_min;
             this.max = DFA16_max;
             this.accept = DFA16_accept;
             this.special = DFA16_special;
             this.transition = DFA16_transition;
         }
         public String getDescription() {
             return "138:1: statement returns [ Statement ast, LinkedList<Expression> divisors, LinkedList<Pair<ArrayRead, Expression>> arrayIndices,\r\n\t\t\t\tLinkedList<Expression> positive ] : (e1= assert_statement |e2= variable_declaration |e3= array_declaration |e4= assignment |e5= if_statement |e6= while_statement );";
         }
     }
  
 
     public static final BitSet FOLLOW_axiom_statement_in_program61 = new BitSet(new long[]{0x0000014600000000L});
     public static final BitSet FOLLOW_function_declaration_in_program82 = new BitSet(new long[]{0x0000014400000000L});
     public static final BitSet FOLLOW_main_in_program88 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_expression_in_single_expression121 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_type_in_function_declaration145 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_IDENT_in_function_declaration147 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_function_declaration149 = new BitSet(new long[]{0x0000004400008000L});
     public static final BitSet FOLLOW_parameter_list_in_function_declaration153 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_function_declaration156 = new BitSet(new long[]{0x0000080100000000L});
     public static final BitSet FOLLOW_function_body_in_function_declaration158 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_40_in_main198 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_main200 = new BitSet(new long[]{0x0000004400008000L});
     public static final BitSet FOLLOW_parameter_list_in_main202 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_main205 = new BitSet(new long[]{0x0000080100000000L});
     public static final BitSet FOLLOW_main_body_in_main207 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_parameter_in_parameter_list262 = new BitSet(new long[]{0x0000000000040002L});
     public static final BitSet FOLLOW_18_in_parameter_list268 = new BitSet(new long[]{0x0000004400000000L});
     public static final BitSet FOLLOW_parameter_in_parameter_list272 = new BitSet(new long[]{0x0000000000040002L});
     public static final BitSet FOLLOW_type_in_parameter306 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_IDENT_in_parameter308 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_assume_statement_in_function_body347 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_43_in_function_body362 = new BitSet(new long[]{0x0000066480000040L});
     public static final BitSet FOLLOW_statement_in_function_body366 = new BitSet(new long[]{0x0000066480000040L});
     public static final BitSet FOLLOW_return_statement_in_function_body387 = new BitSet(new long[]{0x0000200000000000L});
     public static final BitSet FOLLOW_45_in_function_body391 = new BitSet(new long[]{0x0000001000000002L});
     public static final BitSet FOLLOW_ensure_statement_in_function_body421 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_assume_statement_in_main_body470 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_43_in_main_body485 = new BitSet(new long[]{0x0000246480000040L});
     public static final BitSet FOLLOW_statement_in_main_body489 = new BitSet(new long[]{0x0000246480000040L});
     public static final BitSet FOLLOW_45_in_main_body496 = new BitSet(new long[]{0x0000001000000002L});
     public static final BitSet FOLLOW_ensure_statement_in_main_body526 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_43_in_if_body564 = new BitSet(new long[]{0x0000246480000040L});
     public static final BitSet FOLLOW_statement_in_if_body568 = new BitSet(new long[]{0x0000246480000040L});
     public static final BitSet FOLLOW_45_in_if_body575 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_invariant_statement_in_loop_body626 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_43_in_loop_body641 = new BitSet(new long[]{0x0000246480000040L});
     public static final BitSet FOLLOW_statement_in_loop_body645 = new BitSet(new long[]{0x0000246480000040L});
     public static final BitSet FOLLOW_45_in_loop_body652 = new BitSet(new long[]{0x0000001000000002L});
     public static final BitSet FOLLOW_ensure_statement_in_loop_body682 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_assert_statement_in_statement716 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_variable_declaration_in_statement732 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_array_declaration_in_statement748 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_assignment_in_statement764 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_if_statement_in_statement780 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_while_statement_in_statement796 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_39_in_invariant_statement833 = new BitSet(new long[]{0x00000000000A45D0L});
     public static final BitSet FOLLOW_quantified_expression_in_invariant_statement837 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_invariant_statement839 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_39_in_invariant_statement853 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_43_in_invariant_statement855 = new BitSet(new long[]{0x00000000000A45D0L});
     public static final BitSet FOLLOW_quantified_expression_in_invariant_statement860 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_invariant_statement862 = new BitSet(new long[]{0x00002000000A45D0L});
     public static final BitSet FOLLOW_45_in_invariant_statement869 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_33_in_axiom_statement907 = new BitSet(new long[]{0x00000000000A45D0L});
     public static final BitSet FOLLOW_quantified_expression_in_axiom_statement911 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_axiom_statement913 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_31_in_assert_statement944 = new BitSet(new long[]{0x00000000000A45D0L});
     public static final BitSet FOLLOW_quantified_expression_in_assert_statement948 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_assert_statement950 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_32_in_assume_statement987 = new BitSet(new long[]{0x00000000000A45D0L});
     public static final BitSet FOLLOW_quantified_expression_in_assume_statement991 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_assume_statement993 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_32_in_assume_statement1007 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_43_in_assume_statement1009 = new BitSet(new long[]{0x00000000000A45D0L});
     public static final BitSet FOLLOW_quantified_expression_in_assume_statement1014 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_assume_statement1016 = new BitSet(new long[]{0x00002000000A45D0L});
     public static final BitSet FOLLOW_45_in_assume_statement1023 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_36_in_ensure_statement1058 = new BitSet(new long[]{0x00000000000A45D0L});
     public static final BitSet FOLLOW_quantified_expression_in_ensure_statement1062 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_ensure_statement1064 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_36_in_ensure_statement1078 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_43_in_ensure_statement1080 = new BitSet(new long[]{0x00000000000A45D0L});
     public static final BitSet FOLLOW_quantified_expression_in_ensure_statement1085 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_ensure_statement1087 = new BitSet(new long[]{0x00002000000A45D0L});
     public static final BitSet FOLLOW_45_in_ensure_statement1094 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_IDENT_in_assignment1132 = new BitSet(new long[]{0x0000000001000000L});
     public static final BitSet FOLLOW_24_in_assignment1134 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_assignment1136 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_assignment1138 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_IDENT_in_assignment1152 = new BitSet(new long[]{0x0000000010000000L});
     public static final BitSet FOLLOW_28_in_assignment1167 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_assignment1171 = new BitSet(new long[]{0x0000000020000000L});
     public static final BitSet FOLLOW_29_in_assignment1185 = new BitSet(new long[]{0x0000000011000000L});
     public static final BitSet FOLLOW_24_in_assignment1190 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_assignment1194 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_assignment1196 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_type_in_variable_declaration1239 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_IDENT_in_variable_declaration1241 = new BitSet(new long[]{0x0000000001200000L});
     public static final BitSet FOLLOW_24_in_variable_declaration1245 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_variable_declaration1247 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_variable_declaration1252 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_type_in_array_declaration1283 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_IDENT_in_array_declaration1285 = new BitSet(new long[]{0x0000000001000000L});
     public static final BitSet FOLLOW_24_in_array_declaration1287 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_array_init_in_array_declaration1289 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_array_declaration1291 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_30_in_array_init1324 = new BitSet(new long[]{0x0000000010000000L});
     public static final BitSet FOLLOW_28_in_array_init1328 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_array_init1330 = new BitSet(new long[]{0x0000000020000000L});
     public static final BitSet FOLLOW_29_in_array_init1334 = new BitSet(new long[]{0x0000000010000002L});
     public static final BitSet FOLLOW_37_in_if_statement1359 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_if_statement1361 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_if_statement1363 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_if_statement1365 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_if_body_in_if_statement1369 = new BitSet(new long[]{0x0000000800000002L});
     public static final BitSet FOLLOW_35_in_if_statement1373 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_if_body_in_if_statement1377 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_42_in_while_statement1411 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_while_statement1413 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_while_statement1415 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_while_statement1417 = new BitSet(new long[]{0x0000088000000000L});
     public static final BitSet FOLLOW_loop_body_in_while_statement1419 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_41_in_return_statement1443 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_return_statement1445 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_21_in_return_statement1447 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_QUANTIFIER_in_quantified_expression1471 = new BitSet(new long[]{0x0000000000000040L});
     public static final BitSet FOLLOW_IDENT_in_quantified_expression1473 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_quantified_expression1475 = new BitSet(new long[]{0x00000000000AC4D0L});
     public static final BitSet FOLLOW_range_in_quantified_expression1477 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_quantified_expression1480 = new BitSet(new long[]{0x00000000000A45D0L});
     public static final BitSet FOLLOW_quantified_expression_in_quantified_expression1484 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_expression_in_quantified_expression1498 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_expression_in_range1531 = new BitSet(new long[]{0x0000000000040000L});
     public static final BitSet FOLLOW_18_in_range1533 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_range1537 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_rel_expression_in_expression1570 = new BitSet(new long[]{0x0000000002000802L});
     public static final BitSet FOLLOW_25_in_expression1587 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_rel_expression_in_expression1591 = new BitSet(new long[]{0x0000000002000802L});
     public static final BitSet FOLLOW_11_in_expression1606 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_rel_expression_in_expression1610 = new BitSet(new long[]{0x0000000002000802L});
     public static final BitSet FOLLOW_add_expression_in_rel_expression1655 = new BitSet(new long[]{0x000000000CC00002L});
     public static final BitSet FOLLOW_22_in_rel_expression1672 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_add_expression_in_rel_expression1677 = new BitSet(new long[]{0x000000000CC00002L});
     public static final BitSet FOLLOW_23_in_rel_expression1692 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_add_expression_in_rel_expression1696 = new BitSet(new long[]{0x000000000CC00002L});
     public static final BitSet FOLLOW_26_in_rel_expression1711 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_add_expression_in_rel_expression1716 = new BitSet(new long[]{0x000000000CC00002L});
     public static final BitSet FOLLOW_27_in_rel_expression1731 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_add_expression_in_rel_expression1735 = new BitSet(new long[]{0x000000000CC00002L});
     public static final BitSet FOLLOW_mul_expression_in_add_expression1779 = new BitSet(new long[]{0x00001000000A0002L});
     public static final BitSet FOLLOW_44_in_add_expression1796 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_mul_expression_in_add_expression1800 = new BitSet(new long[]{0x00001000000A0002L});
     public static final BitSet FOLLOW_17_in_add_expression1815 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_mul_expression_in_add_expression1819 = new BitSet(new long[]{0x00001000000A0002L});
     public static final BitSet FOLLOW_19_in_add_expression1834 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_mul_expression_in_add_expression1838 = new BitSet(new long[]{0x00001000000A0002L});
     public static final BitSet FOLLOW_unary_expression_in_mul_expression1882 = new BitSet(new long[]{0x0000000000113002L});
     public static final BitSet FOLLOW_13_in_mul_expression1899 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_unary_expression_in_mul_expression1903 = new BitSet(new long[]{0x0000000000113002L});
     public static final BitSet FOLLOW_16_in_mul_expression1918 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_unary_expression_in_mul_expression1922 = new BitSet(new long[]{0x0000000000113002L});
     public static final BitSet FOLLOW_20_in_mul_expression1937 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_unary_expression_in_mul_expression1941 = new BitSet(new long[]{0x0000000000113002L});
     public static final BitSet FOLLOW_12_in_mul_expression1956 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_unary_expression_in_mul_expression1960 = new BitSet(new long[]{0x0000000000113002L});
     public static final BitSet FOLLOW_10_in_unary_expression2002 = new BitSet(new long[]{0x00000000000040D0L});
     public static final BitSet FOLLOW_parenthesized_expression_in_unary_expression2007 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_19_in_unary_expression2021 = new BitSet(new long[]{0x00000000000040D0L});
     public static final BitSet FOLLOW_parenthesized_expression_in_unary_expression2026 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_17_in_unary_expression2040 = new BitSet(new long[]{0x00000000000040D0L});
     public static final BitSet FOLLOW_parenthesized_expression_in_unary_expression2045 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_14_in_parenthesized_expression2076 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_parenthesized_expression2078 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_parenthesized_expression2080 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_function_call_in_parenthesized_expression2094 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_array_read_in_parenthesized_expression2108 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_IDENT_in_parenthesized_expression2122 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_literal_expression_in_parenthesized_expression2136 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_IDENT_in_function_call2167 = new BitSet(new long[]{0x0000000000004000L});
     public static final BitSet FOLLOW_14_in_function_call2169 = new BitSet(new long[]{0x00000000000AC4D0L});
     public static final BitSet FOLLOW_arglist_in_function_call2171 = new BitSet(new long[]{0x0000000000008000L});
     public static final BitSet FOLLOW_15_in_function_call2174 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_expression_in_arglist2213 = new BitSet(new long[]{0x0000000000040002L});
     public static final BitSet FOLLOW_18_in_arglist2228 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_arglist2232 = new BitSet(new long[]{0x0000000000040002L});
     public static final BitSet FOLLOW_IDENT_in_array_read2272 = new BitSet(new long[]{0x0000000010000000L});
     public static final BitSet FOLLOW_28_in_array_read2274 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_array_read2278 = new BitSet(new long[]{0x0000000020000000L});
     public static final BitSet FOLLOW_29_in_array_read2282 = new BitSet(new long[]{0x0000000010000002L});
     public static final BitSet FOLLOW_28_in_array_read2295 = new BitSet(new long[]{0x00000000000A44D0L});
     public static final BitSet FOLLOW_expression_in_array_read2299 = new BitSet(new long[]{0x0000000020000000L});
     public static final BitSet FOLLOW_29_in_array_read2303 = new BitSet(new long[]{0x0000000010000002L});
     public static final BitSet FOLLOW_INT_LITERAL_in_literal_expression2354 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_BOOL_LITERAL_in_literal_expression2368 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_38_in_type2400 = new BitSet(new long[]{0x0000000010000002L});
     public static final BitSet FOLLOW_34_in_type2414 = new BitSet(new long[]{0x0000000010000002L});
     public static final BitSet FOLLOW_28_in_type2420 = new BitSet(new long[]{0x0000000020000000L});
     public static final BitSet FOLLOW_29_in_type2422 = new BitSet(new long[]{0x0000000010000002L});
 
 }
