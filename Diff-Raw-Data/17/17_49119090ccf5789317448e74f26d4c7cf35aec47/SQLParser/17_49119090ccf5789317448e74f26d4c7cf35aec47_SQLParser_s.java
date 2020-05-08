// $ANTLR 3.4 /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g 2012-02-13 21:30:22
 
   package org.simplesql.parser;
   
   import org.simplesql.parser.tree.*;
   
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.antlr.runtime.tree.*;
 
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class SQLParser extends Parser {
     public static final String[] tokenNames = new String[] {
         "<invalid>", "<EOR>", "<DOWN>", "<UP>", "BOOL", "BY", "DOUBLE", "EXPRESSION", "FROM", "FUNC", "GROUP", "IDENT", "INTEGER", "LIMIT", "LOGICAL", "LOGICAL_TOKEN", "MINUS", "MULT_TOKEN", "NEGATION_TOKEN", "NUMBER", "ORDER", "PLUS", "REAL_NUMBER", "RELATION", "RELATION_TOKEN", "SELECT", "STRING", "STRING_LITERAL", "UNARY", "VARIABLE", "WHERE", "WS", "'('", "')'", "'*'", "'+'", "','", "'-'", "'/'", "';'", "'mod'"
     };
 
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
     public Parser[] getDelegates() {
         return new Parser[] {};
     }
 
     // delegators
 
 
     public SQLParser(TokenStream input) {
         this(input, new RecognizerSharedState());
     }
     public SQLParser(TokenStream input, RecognizerSharedState state) {
         super(input, state);
     }
 
 protected TreeAdaptor adaptor = new CommonTreeAdaptor();
 
 public void setTreeAdaptor(TreeAdaptor adaptor) {
     this.adaptor = adaptor;
 }
 public TreeAdaptor getTreeAdaptor() {
     return adaptor;
 }
     public String[] getTokenNames() { return SQLParser.tokenNames; }
     public String getGrammarFileName() { return "/home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g"; }
 
 
       
       public SELECT select;
       public static final java.util.Set<String> variables = new java.util.HashSet<String>();
 
 
     public static class statement_return extends ParserRuleReturnScope {
         public SELECT ret = new SELECT(variables);;
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "statement"
     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:46:1: statement returns [SELECT ret = new SELECT(variables);] : SELECT (se1= expression ) ( ',' (se1= expression ) )* FROM IDENT ( ( WHERE w1= logical ) | ( 'GROUP' 'BY' (gpe1= expression ) ( ',' (gpe1= expression ) )* ) | ( 'ORDER' 'BY' (ope1= expression ) ( ',' (ope1= expression ) )* ) | ( 'LIMIT' l= INTEGER ) )* ( ';' )* ;
     public final SQLParser.statement_return statement() throws RecognitionException {
         SQLParser.statement_return retval = new SQLParser.statement_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token l=null;
         Token SELECT1=null;
         Token char_literal2=null;
         Token FROM3=null;
         Token IDENT4=null;
         Token WHERE5=null;
         Token string_literal6=null;
         Token string_literal7=null;
         Token char_literal8=null;
         Token string_literal9=null;
         Token string_literal10=null;
         Token char_literal11=null;
         Token string_literal12=null;
         Token char_literal13=null;
         SQLParser.expression_return se1 =null;
 
         SQLParser.logical_return w1 =null;
 
         SQLParser.expression_return gpe1 =null;
 
         SQLParser.expression_return ope1 =null;
 
 
         Object l_tree=null;
         Object SELECT1_tree=null;
         Object char_literal2_tree=null;
         Object FROM3_tree=null;
         Object IDENT4_tree=null;
         Object WHERE5_tree=null;
         Object string_literal6_tree=null;
         Object string_literal7_tree=null;
         Object char_literal8_tree=null;
         Object string_literal9_tree=null;
         Object string_literal10_tree=null;
         Object char_literal11_tree=null;
         Object string_literal12_tree=null;
         Object char_literal13_tree=null;
 
         try {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:47:12: ( SELECT (se1= expression ) ( ',' (se1= expression ) )* FROM IDENT ( ( WHERE w1= logical ) | ( 'GROUP' 'BY' (gpe1= expression ) ( ',' (gpe1= expression ) )* ) | ( 'ORDER' 'BY' (ope1= expression ) ( ',' (ope1= expression ) )* ) | ( 'LIMIT' l= INTEGER ) )* ( ';' )* )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:47:14: SELECT (se1= expression ) ( ',' (se1= expression ) )* FROM IDENT ( ( WHERE w1= logical ) | ( 'GROUP' 'BY' (gpe1= expression ) ( ',' (gpe1= expression ) )* ) | ( 'ORDER' 'BY' (ope1= expression ) ( ',' (ope1= expression ) )* ) | ( 'LIMIT' l= INTEGER ) )* ( ';' )*
             {
             root_0 = (Object)adaptor.nil();
 
 
             SELECT1=(Token)match(input,SELECT,FOLLOW_SELECT_in_statement151); 
             SELECT1_tree = 
             (Object)adaptor.create(SELECT1)
             ;
             adaptor.addChild(root_0, SELECT1_tree);
 
 
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:47:21: (se1= expression )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:47:22: se1= expression
             {
             pushFollow(FOLLOW_expression_in_statement156);
             se1=expression();
 
             state._fsp--;
 
             adaptor.addChild(root_0, se1.getTree());
 
              select = retval.ret;  retval.ret.select((se1!=null?se1.expr:null));
 
             }
 
 
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:48:21: ( ',' (se1= expression ) )*
             loop1:
             do {
                 int alt1=2;
                 int LA1_0 = input.LA(1);
 
                 if ( (LA1_0==36) ) {
                     alt1=1;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:48:22: ',' (se1= expression )
             	    {
             	    char_literal2=(Token)match(input,36,FOLLOW_36_in_statement183); 
             	    char_literal2_tree = 
             	    (Object)adaptor.create(char_literal2)
             	    ;
             	    adaptor.addChild(root_0, char_literal2_tree);
 
 
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:48:26: (se1= expression )
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:48:27: se1= expression
             	    {
             	    pushFollow(FOLLOW_expression_in_statement188);
             	    se1=expression();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, se1.getTree());
 
             	    retval.ret.select((se1!=null?se1.expr:null));
 
             	    }
 
 
             	    }
             	    break;
 
             	default :
             	    break loop1;
                 }
             } while (true);
 
 
             FROM3=(Token)match(input,FROM,FOLLOW_FROM_in_statement195); 
             FROM3_tree = 
             (Object)adaptor.create(FROM3)
             ;
             adaptor.addChild(root_0, FROM3_tree);
 
 
             IDENT4=(Token)match(input,IDENT,FOLLOW_IDENT_in_statement197); 
             IDENT4_tree = 
             (Object)adaptor.create(IDENT4)
             ;
             adaptor.addChild(root_0, IDENT4_tree);
 
 
             retval.ret.table((IDENT4!=null?IDENT4.getText():null));
 
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:49:13: ( ( WHERE w1= logical ) | ( 'GROUP' 'BY' (gpe1= expression ) ( ',' (gpe1= expression ) )* ) | ( 'ORDER' 'BY' (ope1= expression ) ( ',' (ope1= expression ) )* ) | ( 'LIMIT' l= INTEGER ) )*
             loop4:
             do {
                 int alt4=5;
                 switch ( input.LA(1) ) {
                 case WHERE:
                     {
                     alt4=1;
                     }
                     break;
                 case GROUP:
                     {
                     alt4=2;
                     }
                     break;
                 case ORDER:
                     {
                     alt4=3;
                     }
                     break;
                 case LIMIT:
                     {
                     alt4=4;
                     }
                     break;
 
                 }
 
                 switch (alt4) {
             	case 1 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:50:13: ( WHERE w1= logical )
             	    {
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:50:13: ( WHERE w1= logical )
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:50:14: WHERE w1= logical
             	    {
             	    WHERE5=(Token)match(input,WHERE,FOLLOW_WHERE_in_statement229); 
             	    WHERE5_tree = 
             	    (Object)adaptor.create(WHERE5)
             	    ;
             	    adaptor.addChild(root_0, WHERE5_tree);
 
 
             	    pushFollow(FOLLOW_logical_in_statement233);
             	    w1=logical();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, w1.getTree());
 
             	    retval.ret.where((w1!=null?w1.ret:null));
 
             	    }
 
 
             	    }
             	    break;
             	case 2 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:52:13: ( 'GROUP' 'BY' (gpe1= expression ) ( ',' (gpe1= expression ) )* )
             	    {
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:52:13: ( 'GROUP' 'BY' (gpe1= expression ) ( ',' (gpe1= expression ) )* )
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:52:14: 'GROUP' 'BY' (gpe1= expression ) ( ',' (gpe1= expression ) )*
             	    {
             	    string_literal6=(Token)match(input,GROUP,FOLLOW_GROUP_in_statement275); 
             	    string_literal6_tree = 
             	    (Object)adaptor.create(string_literal6)
             	    ;
             	    adaptor.addChild(root_0, string_literal6_tree);
 
 
             	    string_literal7=(Token)match(input,BY,FOLLOW_BY_in_statement277); 
             	    string_literal7_tree = 
             	    (Object)adaptor.create(string_literal7)
             	    ;
             	    adaptor.addChild(root_0, string_literal7_tree);
 
 
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:52:27: (gpe1= expression )
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:52:28: gpe1= expression
             	    {
             	    pushFollow(FOLLOW_expression_in_statement282);
             	    gpe1=expression();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, gpe1.getTree());
 
             	    retval.ret.groupBy((gpe1!=null?gpe1.expr:null));
 
             	    }
 
 
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:53:22: ( ',' (gpe1= expression ) )*
             	    loop2:
             	    do {
             	        int alt2=2;
             	        int LA2_0 = input.LA(1);
 
             	        if ( (LA2_0==36) ) {
             	            alt2=1;
             	        }
 
 
             	        switch (alt2) {
             	    	case 1 :
             	    	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:53:23: ',' (gpe1= expression )
             	    	    {
             	    	    char_literal8=(Token)match(input,36,FOLLOW_36_in_statement310); 
             	    	    char_literal8_tree = 
             	    	    (Object)adaptor.create(char_literal8)
             	    	    ;
             	    	    adaptor.addChild(root_0, char_literal8_tree);
 
 
             	    	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:53:27: (gpe1= expression )
             	    	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:53:28: gpe1= expression
             	    	    {
             	    	    pushFollow(FOLLOW_expression_in_statement315);
             	    	    gpe1=expression();
 
             	    	    state._fsp--;
 
             	    	    adaptor.addChild(root_0, gpe1.getTree());
 
             	    	    retval.ret.groupBy((gpe1!=null?gpe1.expr:null));
 
             	    	    }
 
 
             	    	    }
             	    	    break;
 
             	    	default :
             	    	    break loop2;
             	        }
             	    } while (true);
 
 
             	    }
 
 
             	    }
             	    break;
             	case 3 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:55:13: ( 'ORDER' 'BY' (ope1= expression ) ( ',' (ope1= expression ) )* )
             	    {
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:55:13: ( 'ORDER' 'BY' (ope1= expression ) ( ',' (ope1= expression ) )* )
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:55:14: 'ORDER' 'BY' (ope1= expression ) ( ',' (ope1= expression ) )*
             	    {
             	    string_literal9=(Token)match(input,ORDER,FOLLOW_ORDER_in_statement359); 
             	    string_literal9_tree = 
             	    (Object)adaptor.create(string_literal9)
             	    ;
             	    adaptor.addChild(root_0, string_literal9_tree);
 
 
             	    string_literal10=(Token)match(input,BY,FOLLOW_BY_in_statement361); 
             	    string_literal10_tree = 
             	    (Object)adaptor.create(string_literal10)
             	    ;
             	    adaptor.addChild(root_0, string_literal10_tree);
 
 
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:55:27: (ope1= expression )
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:55:28: ope1= expression
             	    {
             	    pushFollow(FOLLOW_expression_in_statement366);
             	    ope1=expression();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, ope1.getTree());
 
             	    retval.ret.orderBy((ope1!=null?ope1.expr:null));
 
             	    }
 
 
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:56:22: ( ',' (ope1= expression ) )*
             	    loop3:
             	    do {
             	        int alt3=2;
             	        int LA3_0 = input.LA(1);
 
             	        if ( (LA3_0==36) ) {
             	            alt3=1;
             	        }
 
 
             	        switch (alt3) {
             	    	case 1 :
             	    	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:56:23: ',' (ope1= expression )
             	    	    {
             	    	    char_literal11=(Token)match(input,36,FOLLOW_36_in_statement394); 
             	    	    char_literal11_tree = 
             	    	    (Object)adaptor.create(char_literal11)
             	    	    ;
             	    	    adaptor.addChild(root_0, char_literal11_tree);
 
 
             	    	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:56:27: (ope1= expression )
             	    	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:56:28: ope1= expression
             	    	    {
             	    	    pushFollow(FOLLOW_expression_in_statement399);
             	    	    ope1=expression();
 
             	    	    state._fsp--;
 
             	    	    adaptor.addChild(root_0, ope1.getTree());
 
             	    	    retval.ret.orderBy((ope1!=null?ope1.expr:null));
 
             	    	    }
 
 
             	    	    }
             	    	    break;
 
             	    	default :
             	    	    break loop3;
             	        }
             	    } while (true);
 
 
             	    }
 
 
             	    }
             	    break;
             	case 4 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:58:13: ( 'LIMIT' l= INTEGER )
             	    {
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:58:13: ( 'LIMIT' l= INTEGER )
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:58:14: 'LIMIT' l= INTEGER
             	    {
             	    string_literal12=(Token)match(input,LIMIT,FOLLOW_LIMIT_in_statement443); 
             	    string_literal12_tree = 
             	    (Object)adaptor.create(string_literal12)
             	    ;
             	    adaptor.addChild(root_0, string_literal12_tree);
 
 
             	    l=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_statement447); 
             	    l_tree = 
             	    (Object)adaptor.create(l)
             	    ;
             	    adaptor.addChild(root_0, l_tree);
 
 
             	    retval.ret.limit((l!=null?l.getText():null));
 
             	    }
 
 
             	    }
             	    break;
 
             	default :
             	    break loop4;
                 }
             } while (true);
 
 
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:60:13: ( ';' )*
             loop5:
             do {
                 int alt5=2;
                 int LA5_0 = input.LA(1);
 
                 if ( (LA5_0==39) ) {
                     alt5=1;
                 }
 
 
                 switch (alt5) {
             	case 1 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:60:13: ';'
             	    {
             	    char_literal13=(Token)match(input,39,FOLLOW_39_in_statement479); 
             	    char_literal13_tree = 
             	    (Object)adaptor.create(char_literal13)
             	    ;
             	    adaptor.addChild(root_0, char_literal13_tree);
 
 
             	    }
             	    break;
 
             	default :
             	    break loop5;
                 }
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
             retval.tree = (Object)adaptor.rulePostProcessing(root_0);
             adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
     	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
 
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "statement"
 
 
     public static class unary_return extends ParserRuleReturnScope {
         public UNARY unary = new UNARY();
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "unary"
     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:70:1: unary returns [UNARY unary = new UNARY()] : (v1= funct |v2= term );
     public final SQLParser.unary_return unary() throws RecognitionException {
         SQLParser.unary_return retval = new SQLParser.unary_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         SQLParser.funct_return v1 =null;
 
         SQLParser.term_return v2 =null;
 
 
 
         try {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:71:2: (v1= funct |v2= term )
             int alt6=2;
             int LA6_0 = input.LA(1);
 
             if ( (LA6_0==IDENT) ) {
                 int LA6_1 = input.LA(2);
 
                 if ( (LA6_1==32) ) {
                     alt6=1;
                 }
                 else if ( (LA6_1==EOF||LA6_1==FROM||LA6_1==GROUP||(LA6_1 >= LIMIT && LA6_1 <= LOGICAL)||LA6_1==ORDER||LA6_1==RELATION||LA6_1==WHERE||(LA6_1 >= 33 && LA6_1 <= 40)) ) {
                     alt6=2;
                 }
                 else {
                     NoViableAltException nvae =
                         new NoViableAltException("", 6, 1, input);
 
                     throw nvae;
 
                 }
             }
             else if ( (LA6_0==BOOL||LA6_0==DOUBLE||LA6_0==INTEGER||LA6_0==STRING_LITERAL||LA6_0==32) ) {
                 alt6=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 6, 0, input);
 
                 throw nvae;
 
             }
             switch (alt6) {
                 case 1 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:72:7: v1= funct
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     pushFollow(FOLLOW_funct_in_unary510);
                     v1=funct();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, v1.getTree());
 
                     retval.unary.term((v1!=null?v1.f:null));
 
                     }
                     break;
                 case 2 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:73:9: v2= term
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     pushFollow(FOLLOW_term_in_unary524);
                     v2=term();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, v2.getTree());
 
                     retval.unary.term((v2!=null?v2.term:null));
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
             retval.tree = (Object)adaptor.rulePostProcessing(root_0);
             adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
     	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
 
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "unary"
 
 
     public static class mult_return extends ParserRuleReturnScope {
         public MULT ret = new MULT();
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "mult"
     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:78:1: mult returns [MULT ret = new MULT()] : u1= unary ( (t= '*' |t= '/' |t= 'mod' ) u2= unary )* ;
     public final SQLParser.mult_return mult() throws RecognitionException {
         SQLParser.mult_return retval = new SQLParser.mult_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token t=null;
         SQLParser.unary_return u1 =null;
 
         SQLParser.unary_return u2 =null;
 
 
         Object t_tree=null;
 
         try {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:79:4: (u1= unary ( (t= '*' |t= '/' |t= 'mod' ) u2= unary )* )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:80:5: u1= unary ( (t= '*' |t= '/' |t= 'mod' ) u2= unary )*
             {
             root_0 = (Object)adaptor.nil();
 
 
             pushFollow(FOLLOW_unary_in_mult562);
             u1=unary();
 
             state._fsp--;
 
             adaptor.addChild(root_0, u1.getTree());
 
             retval.ret.unary((u1!=null?u1.unary:null));
 
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:81:5: ( (t= '*' |t= '/' |t= 'mod' ) u2= unary )*
             loop8:
             do {
                 int alt8=2;
                 int LA8_0 = input.LA(1);
 
                 if ( (LA8_0==34||LA8_0==38||LA8_0==40) ) {
                     alt8=1;
                 }
 
 
                 switch (alt8) {
             	case 1 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:81:6: (t= '*' |t= '/' |t= 'mod' ) u2= unary
             	    {
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:81:6: (t= '*' |t= '/' |t= 'mod' )
             	    int alt7=3;
             	    switch ( input.LA(1) ) {
             	    case 34:
             	        {
             	        alt7=1;
             	        }
             	        break;
             	    case 38:
             	        {
             	        alt7=2;
             	        }
             	        break;
             	    case 40:
             	        {
             	        alt7=3;
             	        }
             	        break;
             	    default:
             	        NoViableAltException nvae =
             	            new NoViableAltException("", 7, 0, input);
 
             	        throw nvae;
 
             	    }
 
             	    switch (alt7) {
             	        case 1 :
             	            // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:81:7: t= '*'
             	            {
             	            t=(Token)match(input,34,FOLLOW_34_in_mult574); 
             	            t_tree = 
             	            (Object)adaptor.create(t)
             	            ;
             	            adaptor.addChild(root_0, t_tree);
 
 
             	            retval.ret.mult();
 
             	            }
             	            break;
             	        case 2 :
             	            // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:81:30: t= '/'
             	            {
             	            t=(Token)match(input,38,FOLLOW_38_in_mult582); 
             	            t_tree = 
             	            (Object)adaptor.create(t)
             	            ;
             	            adaptor.addChild(root_0, t_tree);
 
 
             	            retval.ret.divide();
 
             	            }
             	            break;
             	        case 3 :
             	            // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:82:7: t= 'mod'
             	            {
             	            t=(Token)match(input,40,FOLLOW_40_in_mult594); 
             	            t_tree = 
             	            (Object)adaptor.create(t)
             	            ;
             	            adaptor.addChild(root_0, t_tree);
 
 
             	            retval.ret.mod();
 
             	            }
             	            break;
 
             	    }
 
 
             	    pushFollow(FOLLOW_unary_in_mult606);
             	    u2=unary();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, u2.getTree());
 
             	    retval.ret.unary((u2!=null?u2.unary:null));
 
             	    }
             	    break;
 
             	default :
             	    break loop8;
                 }
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
             retval.tree = (Object)adaptor.rulePostProcessing(root_0);
             adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
     	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
 
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "mult"
 
 
     public static class expression_return extends ParserRuleReturnScope {
         public EXPRESSION expr = new EXPRESSION();
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "expression"
     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:88:1: expression returns [EXPRESSION expr = new EXPRESSION()] : em1= mult ( (t+= '-' |t+= '+' ) em2= mult )* ;
     public final SQLParser.expression_return expression() throws RecognitionException {
         SQLParser.expression_return retval = new SQLParser.expression_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token t=null;
         List list_t=null;
         SQLParser.mult_return em1 =null;
 
         SQLParser.mult_return em2 =null;
 
 
         Object t_tree=null;
 
         try {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:89:4: (em1= mult ( (t+= '-' |t+= '+' ) em2= mult )* )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:89:6: em1= mult ( (t+= '-' |t+= '+' ) em2= mult )*
             {
             root_0 = (Object)adaptor.nil();
 
 
             pushFollow(FOLLOW_mult_in_expression637);
             em1=mult();
 
             state._fsp--;
 
             adaptor.addChild(root_0, em1.getTree());
 
             retval.expr.mult((em1!=null?em1.ret:null));
 
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:90:28: ( (t+= '-' |t+= '+' ) em2= mult )*
             loop10:
             do {
                 int alt10=2;
                 int LA10_0 = input.LA(1);
 
                 if ( (LA10_0==35||LA10_0==37) ) {
                     alt10=1;
                 }
 
 
                 switch (alt10) {
             	case 1 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:90:29: (t+= '-' |t+= '+' ) em2= mult
             	    {
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:90:29: (t+= '-' |t+= '+' )
             	    int alt9=2;
             	    int LA9_0 = input.LA(1);
 
             	    if ( (LA9_0==37) ) {
             	        alt9=1;
             	    }
             	    else if ( (LA9_0==35) ) {
             	        alt9=2;
             	    }
             	    else {
             	        NoViableAltException nvae =
             	            new NoViableAltException("", 9, 0, input);
 
             	        throw nvae;
 
             	    }
             	    switch (alt9) {
             	        case 1 :
             	            // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:90:30: t+= '-'
             	            {
             	            t=(Token)match(input,37,FOLLOW_37_in_expression648); 
             	            t_tree = 
             	            (Object)adaptor.create(t)
             	            ;
             	            adaptor.addChild(root_0, t_tree);
 
             	            if (list_t==null) list_t=new ArrayList();
             	            list_t.add(t);
 
 
             	            retval.expr.minus();
 
             	            }
             	            break;
             	        case 2 :
             	            // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:90:55: t+= '+'
             	            {
             	            t=(Token)match(input,35,FOLLOW_35_in_expression655); 
             	            t_tree = 
             	            (Object)adaptor.create(t)
             	            ;
             	            adaptor.addChild(root_0, t_tree);
 
             	            if (list_t==null) list_t=new ArrayList();
             	            list_t.add(t);
 
 
             	            retval.expr.plus();
 
             	            }
             	            break;
 
             	    }
 
 
             	    pushFollow(FOLLOW_mult_in_expression662);
             	    em2=mult();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, em2.getTree());
 
             	    retval.expr.mult((em2!=null?em2.ret:null));
 
             	    }
             	    break;
 
             	default :
             	    break loop10;
                 }
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
             retval.tree = (Object)adaptor.rulePostProcessing(root_0);
             adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
     	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
 
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "expression"
 
 
     public static class relation_return extends ParserRuleReturnScope {
         public RELATION ret;
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "relation"
     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:96:1: relation returns [RELATION ret] : e1= expression RELATION e2= expression ;
     public final SQLParser.relation_return relation() throws RecognitionException {
         SQLParser.relation_return retval = new SQLParser.relation_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token RELATION14=null;
         SQLParser.expression_return e1 =null;
 
         SQLParser.expression_return e2 =null;
 
 
         Object RELATION14_tree=null;
 
         try {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:97:9: (e1= expression RELATION e2= expression )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:98:11: e1= expression RELATION e2= expression
             {
             root_0 = (Object)adaptor.nil();
 
 
             pushFollow(FOLLOW_expression_in_relation717);
             e1=expression();
 
             state._fsp--;
 
             adaptor.addChild(root_0, e1.getTree());
 
             RELATION14=(Token)match(input,RELATION,FOLLOW_RELATION_in_relation719); 
             RELATION14_tree = 
             (Object)adaptor.create(RELATION14)
             ;
             adaptor.addChild(root_0, RELATION14_tree);
 
 
             pushFollow(FOLLOW_expression_in_relation723);
             e2=expression();
 
             state._fsp--;
 
             adaptor.addChild(root_0, e2.getTree());
 
             retval.ret = new RELATION((e1!=null?e1.expr:null), (RELATION14!=null?RELATION14.getText():null), (e2!=null?e2.expr:null));
 
             }
 
             retval.stop = input.LT(-1);
 
 
             retval.tree = (Object)adaptor.rulePostProcessing(root_0);
             adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
     	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
 
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "relation"
 
 
     public static class logical_return extends ParserRuleReturnScope {
         public LOGICAL ret = new LOGICAL();
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "logical"
     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:102:1: logical returns [LOGICAL ret = new LOGICAL()] : r1= relation (lotoken= LOGICAL r2= relation )* ;
     public final SQLParser.logical_return logical() throws RecognitionException {
         SQLParser.logical_return retval = new SQLParser.logical_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token lotoken=null;
         SQLParser.relation_return r1 =null;
 
         SQLParser.relation_return r2 =null;
 
 
         Object lotoken_tree=null;
 
         try {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:103:9: (r1= relation (lotoken= LOGICAL r2= relation )* )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:103:12: r1= relation (lotoken= LOGICAL r2= relation )*
             {
             root_0 = (Object)adaptor.nil();
 
 
             pushFollow(FOLLOW_relation_in_logical766);
             r1=relation();
 
             state._fsp--;
 
             adaptor.addChild(root_0, r1.getTree());
 
             retval.ret.relation((r1!=null?r1.ret:null)); select.rangeGroups.addRange((r1!=null?r1.ret:null).getRange());
 
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:104:11: (lotoken= LOGICAL r2= relation )*
             loop11:
             do {
                 int alt11=2;
                 int LA11_0 = input.LA(1);
 
                 if ( (LA11_0==LOGICAL) ) {
                     alt11=1;
                 }
 
 
                 switch (alt11) {
             	case 1 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:104:12: lotoken= LOGICAL r2= relation
             	    {
             	    lotoken=(Token)match(input,LOGICAL,FOLLOW_LOGICAL_in_logical784); 
             	    lotoken_tree = 
             	    (Object)adaptor.create(lotoken)
             	    ;
             	    adaptor.addChild(root_0, lotoken_tree);
 
 
 
             	                  retval.ret.logical((lotoken!=null?lotoken.getText():null));
             	                  org.simplesql.parser.tree.LOGICAL.OP lop = org.simplesql.parser.tree.LOGICAL.OP.parse((lotoken!=null?lotoken.getText():null));
             	                  if(lop == org.simplesql.parser.tree.LOGICAL.OP.JAVA_OR || lop == org.simplesql.parser.tree.LOGICAL.OP.OR)
             	                     select.rangeGroups.nextGroup();
             	                  
             	                
 
             	    pushFollow(FOLLOW_relation_in_logical803);
             	    r2=relation();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, r2.getTree());
 
             	    retval.ret.relation((r2!=null?r2.ret:null)); select.rangeGroups.addRange((r2!=null?r2.ret:null).getRange());
 
             	    }
             	    break;
 
             	default :
             	    break loop11;
                 }
             } while (true);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
             retval.tree = (Object)adaptor.rulePostProcessing(root_0);
             adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
     	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
 
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "logical"
 
 
     public static class funct_return extends ParserRuleReturnScope {
         public FUNCTION f = new FUNCTION();
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "funct"
     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:116:1: funct returns [FUNCTION f = new FUNCTION()] : IDENT '(' fe1= expression ( ',' fe2= expression )* ')' ;
     public final SQLParser.funct_return funct() throws RecognitionException {
         SQLParser.funct_return retval = new SQLParser.funct_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token IDENT15=null;
         Token char_literal16=null;
         Token char_literal17=null;
         Token char_literal18=null;
         SQLParser.expression_return fe1 =null;
 
         SQLParser.expression_return fe2 =null;
 
 
         Object IDENT15_tree=null;
         Object char_literal16_tree=null;
         Object char_literal17_tree=null;
         Object char_literal18_tree=null;
 
         try {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:117:6: ( IDENT '(' fe1= expression ( ',' fe2= expression )* ')' )
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:117:8: IDENT '(' fe1= expression ( ',' fe2= expression )* ')'
             {
             root_0 = (Object)adaptor.nil();
 
 
             IDENT15=(Token)match(input,IDENT,FOLLOW_IDENT_in_funct850); 
             IDENT15_tree = 
             (Object)adaptor.create(IDENT15)
             ;
             adaptor.addChild(root_0, IDENT15_tree);
 
 
             retval.f.name((IDENT15!=null?IDENT15.getText():null));
 
             char_literal16=(Token)match(input,32,FOLLOW_32_in_funct853); 
             char_literal16_tree = 
             (Object)adaptor.create(char_literal16)
             ;
             adaptor.addChild(root_0, char_literal16_tree);
 
 
             pushFollow(FOLLOW_expression_in_funct857);
             fe1=expression();
 
             state._fsp--;
 
             adaptor.addChild(root_0, fe1.getTree());
 
             retval.f.expression((fe1!=null?fe1.expr:null));
 
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:117:84: ( ',' fe2= expression )*
             loop12:
             do {
                 int alt12=2;
                 int LA12_0 = input.LA(1);
 
                 if ( (LA12_0==36) ) {
                     alt12=1;
                 }
 
 
                 switch (alt12) {
             	case 1 :
             	    // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:117:85: ',' fe2= expression
             	    {
             	    char_literal17=(Token)match(input,36,FOLLOW_36_in_funct862); 
             	    char_literal17_tree = 
             	    (Object)adaptor.create(char_literal17)
             	    ;
             	    adaptor.addChild(root_0, char_literal17_tree);
 
 
             	    pushFollow(FOLLOW_expression_in_funct866);
             	    fe2=expression();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, fe2.getTree());
 
             	    retval.f.expression((fe2!=null?fe2.expr:null));
 
             	    }
             	    break;
 
             	default :
             	    break loop12;
                 }
             } while (true);
 
 
             char_literal18=(Token)match(input,33,FOLLOW_33_in_funct872); 
             char_literal18_tree = 
             (Object)adaptor.create(char_literal18)
             ;
             adaptor.addChild(root_0, char_literal18_tree);
 
 
             }
 
             retval.stop = input.LT(-1);
 
 
             retval.tree = (Object)adaptor.rulePostProcessing(root_0);
             adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
     	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
 
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "funct"
 
 
     public static class term_return extends ParserRuleReturnScope {
         public TERM term;
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "term"
     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:122:1: term returns [TERM term] : ( (v= IDENT -> ^( VARIABLE $v) |v= INTEGER -> ^( NUMBER $v) |v= BOOL -> ^( BOOL $v) |v= DOUBLE -> ^( REAL_NUMBER $v) | '(' r= expression ')' -> ^( EXPRESSION $r) ) |v= STRING_LITERAL -> ^( STRING $v) );
     public final SQLParser.term_return term() throws RecognitionException {
         SQLParser.term_return retval = new SQLParser.term_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token v=null;
         Token char_literal19=null;
         Token char_literal20=null;
         SQLParser.expression_return r =null;
 
 
         Object v_tree=null;
         Object char_literal19_tree=null;
         Object char_literal20_tree=null;
         RewriteRuleTokenStream stream_INTEGER=new RewriteRuleTokenStream(adaptor,"token INTEGER");
         RewriteRuleTokenStream stream_DOUBLE=new RewriteRuleTokenStream(adaptor,"token DOUBLE");
         RewriteRuleTokenStream stream_IDENT=new RewriteRuleTokenStream(adaptor,"token IDENT");
         RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
         RewriteRuleTokenStream stream_32=new RewriteRuleTokenStream(adaptor,"token 32");
         RewriteRuleTokenStream stream_33=new RewriteRuleTokenStream(adaptor,"token 33");
         RewriteRuleTokenStream stream_BOOL=new RewriteRuleTokenStream(adaptor,"token BOOL");
         RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
         try {
             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:122:25: ( (v= IDENT -> ^( VARIABLE $v) |v= INTEGER -> ^( NUMBER $v) |v= BOOL -> ^( BOOL $v) |v= DOUBLE -> ^( REAL_NUMBER $v) | '(' r= expression ')' -> ^( EXPRESSION $r) ) |v= STRING_LITERAL -> ^( STRING $v) )
             int alt14=2;
             int LA14_0 = input.LA(1);
 
             if ( (LA14_0==BOOL||LA14_0==DOUBLE||(LA14_0 >= IDENT && LA14_0 <= INTEGER)||LA14_0==32) ) {
                 alt14=1;
             }
             else if ( (LA14_0==STRING_LITERAL) ) {
                 alt14=2;
             }
             else {
                 NoViableAltException nvae =
                     new NoViableAltException("", 14, 0, input);
 
                 throw nvae;
 
             }
             switch (alt14) {
                 case 1 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:123:7: (v= IDENT -> ^( VARIABLE $v) |v= INTEGER -> ^( NUMBER $v) |v= BOOL -> ^( BOOL $v) |v= DOUBLE -> ^( REAL_NUMBER $v) | '(' r= expression ')' -> ^( EXPRESSION $r) )
                     {
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:123:7: (v= IDENT -> ^( VARIABLE $v) |v= INTEGER -> ^( NUMBER $v) |v= BOOL -> ^( BOOL $v) |v= DOUBLE -> ^( REAL_NUMBER $v) | '(' r= expression ')' -> ^( EXPRESSION $r) )
                     int alt13=5;
                     switch ( input.LA(1) ) {
                     case IDENT:
                         {
                         alt13=1;
                         }
                         break;
                     case INTEGER:
                         {
                         alt13=2;
                         }
                         break;
                     case BOOL:
                         {
                         alt13=3;
                         }
                         break;
                     case DOUBLE:
                         {
                         alt13=4;
                         }
                         break;
                     case 32:
                         {
                         alt13=5;
                         }
                         break;
                     default:
                         NoViableAltException nvae =
                             new NoViableAltException("", 13, 0, input);
 
                         throw nvae;
 
                     }
 
                     switch (alt13) {
                         case 1 :
                             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:123:9: v= IDENT
                             {
                             v=(Token)match(input,IDENT,FOLLOW_IDENT_in_term899);  
                             stream_IDENT.add(v);
 
 
                             variables.add((v!=null?v.getText():null).trim());
 
                             // AST REWRITE
                             // elements: v
                             // token labels: v
                             // rule labels: retval
                             // token list labels: 
                             // rule list labels: 
                             // wildcard labels: 
                             retval.tree = root_0;
                             RewriteRuleTokenStream stream_v=new RewriteRuleTokenStream(adaptor,"token v",v);
                             RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
 
                             root_0 = (Object)adaptor.nil();
                             // 124:11: -> ^( VARIABLE $v)
                             {
                                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:124:14: ^( VARIABLE $v)
                                 {
                                 Object root_1 = (Object)adaptor.nil();
                                 root_1 = (Object)adaptor.becomeRoot(
                                 (Object)adaptor.create(VARIABLE, "VARIABLE")
                                 , root_1);
 
                                 adaptor.addChild(root_1, stream_v.nextNode());
 
                                 adaptor.addChild(root_0, root_1);
                                 }
 
                                 adaptor.addChild(root_0, retval.term = new VARIABLE((v!=null?v.getText():null)));
 
                             }
 
 
                             retval.tree = root_0;
 
                             }
                             break;
                         case 2 :
                             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:125:9: v= INTEGER
                             {
                             v=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_term940);  
                             stream_INTEGER.add(v);
 
 
                             // AST REWRITE
                             // elements: v
                             // token labels: v
                             // rule labels: retval
                             // token list labels: 
                             // rule list labels: 
                             // wildcard labels: 
                             retval.tree = root_0;
                             RewriteRuleTokenStream stream_v=new RewriteRuleTokenStream(adaptor,"token v",v);
                             RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
 
                             root_0 = (Object)adaptor.nil();
                             // 125:19: -> ^( NUMBER $v)
                             {
                                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:125:22: ^( NUMBER $v)
                                 {
                                 Object root_1 = (Object)adaptor.nil();
                                 root_1 = (Object)adaptor.becomeRoot(
                                 (Object)adaptor.create(NUMBER, "NUMBER")
                                 , root_1);
 
                                 adaptor.addChild(root_1, stream_v.nextNode());
 
                                 adaptor.addChild(root_0, root_1);
                                 }
 
                                 adaptor.addChild(root_0,  retval.term = new INTEGER((v!=null?v.getText():null)));
 
                             }
 
 
                             retval.tree = root_0;
 
                             }
                             break;
                         case 3 :
                             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:126:9: v= BOOL
                             {
                             v=(Token)match(input,BOOL,FOLLOW_BOOL_in_term963);  
                             stream_BOOL.add(v);
 
 
                             // AST REWRITE
                             // elements: BOOL, v
                             // token labels: v
                             // rule labels: retval
                             // token list labels: 
                             // rule list labels: 
                             // wildcard labels: 
                             retval.tree = root_0;
                             RewriteRuleTokenStream stream_v=new RewriteRuleTokenStream(adaptor,"token v",v);
                             RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
 
                             root_0 = (Object)adaptor.nil();
                             // 126:19: -> ^( BOOL $v)
                             {
                                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:126:22: ^( BOOL $v)
                                 {
                                 Object root_1 = (Object)adaptor.nil();
                                 root_1 = (Object)adaptor.becomeRoot(
                                 stream_BOOL.nextNode()
                                 , root_1);
 
                                 adaptor.addChild(root_1, stream_v.nextNode());
 
                                 adaptor.addChild(root_0, root_1);
                                 }
 
                                 adaptor.addChild(root_0,  retval.term = new BOOLEAN((v!=null?v.getText():null)) );
 
                             }
 
 
                             retval.tree = root_0;
 
                             }
                             break;
                         case 4 :
                             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:127:9: v= DOUBLE
                             {
                             v=(Token)match(input,DOUBLE,FOLLOW_DOUBLE_in_term989);  
                             stream_DOUBLE.add(v);
 
 
                             // AST REWRITE
                             // elements: v
                             // token labels: v
                             // rule labels: retval
                             // token list labels: 
                             // rule list labels: 
                             // wildcard labels: 
                             retval.tree = root_0;
                             RewriteRuleTokenStream stream_v=new RewriteRuleTokenStream(adaptor,"token v",v);
                             RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
 
                             root_0 = (Object)adaptor.nil();
                             // 127:18: -> ^( REAL_NUMBER $v)
                             {
                                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:127:21: ^( REAL_NUMBER $v)
                                 {
                                 Object root_1 = (Object)adaptor.nil();
                                 root_1 = (Object)adaptor.becomeRoot(
                                 (Object)adaptor.create(REAL_NUMBER, "REAL_NUMBER")
                                 , root_1);
 
                                 adaptor.addChild(root_1, stream_v.nextNode());
 
                                 adaptor.addChild(root_0, root_1);
                                 }
 
                                 adaptor.addChild(root_0, retval.term = new DOUBLE((v!=null?v.getText():null)));
 
                             }
 
 
                             retval.tree = root_0;
 
                             }
                             break;
                         case 5 :
                             // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:128:9: '(' r= expression ')'
                             {
                             char_literal19=(Token)match(input,32,FOLLOW_32_in_term1011);  
                             stream_32.add(char_literal19);
 
 
                             pushFollow(FOLLOW_expression_in_term1015);
                             r=expression();
 
                             state._fsp--;
 
                             stream_expression.add(r.getTree());
 
                             char_literal20=(Token)match(input,33,FOLLOW_33_in_term1017);  
                             stream_33.add(char_literal20);
 
 
                             // AST REWRITE
                             // elements: r
                             // token labels: 
                             // rule labels: retval, r
                             // token list labels: 
                             // rule list labels: 
                             // wildcard labels: 
                             retval.tree = root_0;
                             RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                             RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
 
                             root_0 = (Object)adaptor.nil();
                             // 128:30: -> ^( EXPRESSION $r)
                             {
                                 // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:128:33: ^( EXPRESSION $r)
                                 {
                                 Object root_1 = (Object)adaptor.nil();
                                 root_1 = (Object)adaptor.becomeRoot(
                                 (Object)adaptor.create(EXPRESSION, "EXPRESSION")
                                 , root_1);
 
                                 adaptor.addChild(root_1, stream_r.nextTree());
 
                                 adaptor.addChild(root_0, root_1);
                                 }
 
                             }
 
 
                             retval.tree = root_0;
 
                             }
                             break;
 
                     }
 
 
                     }
                     break;
                 case 2 :
                     // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:131:9: v= STRING_LITERAL
                     {
                     v=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_term1057);  
                     stream_STRING_LITERAL.add(v);
 
 
                     // AST REWRITE
                     // elements: v
                     // token labels: v
                     // rule labels: retval
                     // token list labels: 
                     // rule list labels: 
                     // wildcard labels: 
                     retval.tree = root_0;
                     RewriteRuleTokenStream stream_v=new RewriteRuleTokenStream(adaptor,"token v",v);
                     RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
 
                     root_0 = (Object)adaptor.nil();
                     // 131:26: -> ^( STRING $v)
                     {
                         // /home/gvanvuuren/checkouts/SimpleSQL/parser/src/main/antlr3/org/simplesql/parser/SQL.g:131:29: ^( STRING $v)
                         {
                         Object root_1 = (Object)adaptor.nil();
                         root_1 = (Object)adaptor.becomeRoot(
                         (Object)adaptor.create(STRING, "STRING")
                         , root_1);
 
                         adaptor.addChild(root_1, stream_v.nextNode());
 
                         adaptor.addChild(root_0, root_1);
                         }
 
                         adaptor.addChild(root_0, retval.term = new STRING((v!=null?v.getText():null)));
 
                     }
 
 
                     retval.tree = root_0;
 
                     }
                     break;
 
             }
             retval.stop = input.LT(-1);
 
 
             retval.tree = (Object)adaptor.rulePostProcessing(root_0);
             adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
     	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
 
         }
 
         finally {
         	// do for sure before leaving
         }
         return retval;
     }
     // $ANTLR end "term"
 
     // Delegated rules
 
 
  
 
     public static final BitSet FOLLOW_SELECT_in_statement151 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_statement156 = new BitSet(new long[]{0x0000001000000100L});
     public static final BitSet FOLLOW_36_in_statement183 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_statement188 = new BitSet(new long[]{0x0000001000000100L});
     public static final BitSet FOLLOW_FROM_in_statement195 = new BitSet(new long[]{0x0000000000000800L});
     public static final BitSet FOLLOW_IDENT_in_statement197 = new BitSet(new long[]{0x0000008040102402L});
     public static final BitSet FOLLOW_WHERE_in_statement229 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_logical_in_statement233 = new BitSet(new long[]{0x0000008040102402L});
     public static final BitSet FOLLOW_GROUP_in_statement275 = new BitSet(new long[]{0x0000000000000020L});
     public static final BitSet FOLLOW_BY_in_statement277 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_statement282 = new BitSet(new long[]{0x0000009040102402L});
     public static final BitSet FOLLOW_36_in_statement310 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_statement315 = new BitSet(new long[]{0x0000009040102402L});
     public static final BitSet FOLLOW_ORDER_in_statement359 = new BitSet(new long[]{0x0000000000000020L});
     public static final BitSet FOLLOW_BY_in_statement361 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_statement366 = new BitSet(new long[]{0x0000009040102402L});
     public static final BitSet FOLLOW_36_in_statement394 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_statement399 = new BitSet(new long[]{0x0000009040102402L});
     public static final BitSet FOLLOW_LIMIT_in_statement443 = new BitSet(new long[]{0x0000000000001000L});
     public static final BitSet FOLLOW_INTEGER_in_statement447 = new BitSet(new long[]{0x0000008040102402L});
     public static final BitSet FOLLOW_39_in_statement479 = new BitSet(new long[]{0x0000008000000002L});
     public static final BitSet FOLLOW_funct_in_unary510 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_term_in_unary524 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_unary_in_mult562 = new BitSet(new long[]{0x0000014400000002L});
     public static final BitSet FOLLOW_34_in_mult574 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_38_in_mult582 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_40_in_mult594 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_unary_in_mult606 = new BitSet(new long[]{0x0000014400000002L});
     public static final BitSet FOLLOW_mult_in_expression637 = new BitSet(new long[]{0x0000002800000002L});
     public static final BitSet FOLLOW_37_in_expression648 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_35_in_expression655 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_mult_in_expression662 = new BitSet(new long[]{0x0000002800000002L});
     public static final BitSet FOLLOW_expression_in_relation717 = new BitSet(new long[]{0x0000000000800000L});
     public static final BitSet FOLLOW_RELATION_in_relation719 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_relation723 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_relation_in_logical766 = new BitSet(new long[]{0x0000000000004002L});
     public static final BitSet FOLLOW_LOGICAL_in_logical784 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_relation_in_logical803 = new BitSet(new long[]{0x0000000000004002L});
     public static final BitSet FOLLOW_IDENT_in_funct850 = new BitSet(new long[]{0x0000000100000000L});
     public static final BitSet FOLLOW_32_in_funct853 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_funct857 = new BitSet(new long[]{0x0000001200000000L});
     public static final BitSet FOLLOW_36_in_funct862 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_funct866 = new BitSet(new long[]{0x0000001200000000L});
     public static final BitSet FOLLOW_33_in_funct872 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_IDENT_in_term899 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_INTEGER_in_term940 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_BOOL_in_term963 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_DOUBLE_in_term989 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_32_in_term1011 = new BitSet(new long[]{0x0000000108001850L});
     public static final BitSet FOLLOW_expression_in_term1015 = new BitSet(new long[]{0x0000000200000000L});
     public static final BitSet FOLLOW_33_in_term1017 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_STRING_LITERAL_in_term1057 = new BitSet(new long[]{0x0000000000000002L});
 
 }
