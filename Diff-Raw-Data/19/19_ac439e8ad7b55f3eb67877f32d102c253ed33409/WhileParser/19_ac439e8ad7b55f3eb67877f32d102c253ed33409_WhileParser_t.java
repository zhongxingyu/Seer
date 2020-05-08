 package analyze;
// $ANTLR 3.4 WhileParser.g 2012-11-23 08:10:09
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.antlr.runtime.tree.*;
 
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class WhileParser extends Parser {
     public static final String[] tokenNames = new String[] {
         "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AFF", "AFFECTATION", "ALTCONDITION", "CALL", "COMMA", "COMMANDE", "COMMANDES", "CONDITION", "CONS", "CONTENU", "DEBUTCOMMANDES", "DEFINITION", "DO", "DP", "ELSE", "ENTREE", "EXPRESSION", "FI", "FINCOMMANDES", "FINCONDITION", "FOR", "HD", "ID", "IF", "INITIALISATION", "INPUT", "ISEQUAL", "LINEBREAK", "MIN", "MUL", "NIL", "NOM", "NOP", "NUM", "OD", "OUTPUT", "PF", "PLUS", "PO", "PR", "PROGRAM", "PROGRAMME", "PV", "SEPCOMMANDES", "SKIP", "SORTIE", "THEN", "TL", "VAR", "WHILE", "WS"
     };
 
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
     public Parser[] getDelegates() {
         return new Parser[] {};
     }
 
     // delegators
 
 
     public WhileParser(TokenStream input) {
         this(input, new RecognizerSharedState());
     }
     public WhileParser(TokenStream input, RecognizerSharedState state) {
         super(input, state);
     }
 
 protected TreeAdaptor adaptor = new CommonTreeAdaptor();
 
 public void setTreeAdaptor(TreeAdaptor adaptor) {
     this.adaptor = adaptor;
 }
 public TreeAdaptor getTreeAdaptor() {
     return adaptor;
 }
     public String[] getTokenNames() { return WhileParser.tokenNames; }
     public String getGrammarFileName() { return "WhileParser.g"; }
 
 
     	/*public void reportError(RecognitionException e) {
     		if(e instanceof MissingTokenException){
     			System.err.print("Error at line "+e.line+", column "+e.charPositionInLine+": '"+e.token.getText()+"' was not expected, ");
     			MissingTokenException mte = (MissingTokenException)e;
     			System.err.println("Token "+getTokenNames()[mte.expecting]+" wanted");
     		}
     		else if(e instanceof NoViableAltException){
     			System.err.println("Ah ouais, mais nan");
     		}
     		else {super.reportError(e);}
     	}*/
 
 
     public static class file_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "file"
     // WhileParser.g:24:1: file : ( ( prog )+ ) ;
     public final WhileParser.file_return file() throws RecognitionException {
         WhileParser.file_return retval = new WhileParser.file_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         WhileParser.prog_return prog1 =null;
 
 
 
         try {
             // WhileParser.g:24:6: ( ( ( prog )+ ) )
             // WhileParser.g:24:8: ( ( prog )+ )
             {
             root_0 = (Object)adaptor.nil();
 
 
             // WhileParser.g:24:8: ( ( prog )+ )
             // WhileParser.g:24:9: ( prog )+
             {
             // WhileParser.g:24:9: ( prog )+
             int cnt1=0;
             loop1:
             do {
                 int alt1=2;
                 int LA1_0 = input.LA(1);
 
                 if ( (LA1_0==PROGRAM) ) {
                     alt1=1;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // WhileParser.g:24:9: prog
             	    {
             	    pushFollow(FOLLOW_prog_in_file51);
             	    prog1=prog();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, prog1.getTree());
 
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
     // $ANTLR end "file"
 
 
     public static class prog_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "prog"
     // WhileParser.g:27:1: prog : ( PROGRAM ID DP def ) -> ID def ;
     public final WhileParser.prog_return prog() throws RecognitionException {
         WhileParser.prog_return retval = new WhileParser.prog_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token PROGRAM2=null;
         Token ID3=null;
         Token DP4=null;
         WhileParser.def_return def5 =null;
 
 
         Object PROGRAM2_tree=null;
         Object ID3_tree=null;
         Object DP4_tree=null;
         RewriteRuleTokenStream stream_PROGRAM=new RewriteRuleTokenStream(adaptor,"token PROGRAM");
         RewriteRuleTokenStream stream_DP=new RewriteRuleTokenStream(adaptor,"token DP");
         RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
         RewriteRuleSubtreeStream stream_def=new RewriteRuleSubtreeStream(adaptor,"rule def");
         try {
             // WhileParser.g:27:6: ( ( PROGRAM ID DP def ) -> ID def )
             // WhileParser.g:27:8: ( PROGRAM ID DP def )
             {
             // WhileParser.g:27:8: ( PROGRAM ID DP def )
             // WhileParser.g:27:9: PROGRAM ID DP def
             {
             PROGRAM2=(Token)match(input,PROGRAM,FOLLOW_PROGRAM_in_prog65);  
             stream_PROGRAM.add(PROGRAM2);
 
 
             ID3=(Token)match(input,ID,FOLLOW_ID_in_prog67);  
             stream_ID.add(ID3);
 
 
             DP4=(Token)match(input,DP,FOLLOW_DP_in_prog69);  
             stream_DP.add(DP4);
 
 
             pushFollow(FOLLOW_def_in_prog71);
             def5=def();
 
             state._fsp--;
 
             stream_def.add(def5.getTree());
 
             }
 
 
             // AST REWRITE
             // elements: def, ID
             // token labels: 
             // rule labels: retval
             // token list labels: 
             // rule list labels: 
             // wildcard labels: 
             retval.tree = root_0;
             RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
 
             root_0 = (Object)adaptor.nil();
             // 28:3: -> ID def
             {
                 adaptor.addChild(root_0, 
                 stream_ID.nextNode()
                 );
 
                 adaptor.addChild(root_0, stream_def.nextTree());
 
             }
 
 
             retval.tree = root_0;
 
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
     // $ANTLR end "prog"
 
 
     public static class def_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "def"
     // WhileParser.g:31:1: def : INPUT vars PR commands PR OUTPUT vars -> vars PR commands PR vars ;
     public final WhileParser.def_return def() throws RecognitionException {
         WhileParser.def_return retval = new WhileParser.def_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token INPUT6=null;
         Token PR8=null;
         Token PR10=null;
         Token OUTPUT11=null;
         WhileParser.vars_return vars7 =null;
 
         WhileParser.commands_return commands9 =null;
 
         WhileParser.vars_return vars12 =null;
 
 
         Object INPUT6_tree=null;
         Object PR8_tree=null;
         Object PR10_tree=null;
         Object OUTPUT11_tree=null;
         RewriteRuleTokenStream stream_INPUT=new RewriteRuleTokenStream(adaptor,"token INPUT");
         RewriteRuleTokenStream stream_PR=new RewriteRuleTokenStream(adaptor,"token PR");
         RewriteRuleTokenStream stream_OUTPUT=new RewriteRuleTokenStream(adaptor,"token OUTPUT");
         RewriteRuleSubtreeStream stream_vars=new RewriteRuleSubtreeStream(adaptor,"rule vars");
         RewriteRuleSubtreeStream stream_commands=new RewriteRuleSubtreeStream(adaptor,"rule commands");
         try {
             // WhileParser.g:31:5: ( INPUT vars PR commands PR OUTPUT vars -> vars PR commands PR vars )
             // WhileParser.g:31:7: INPUT vars PR commands PR OUTPUT vars
             {
             INPUT6=(Token)match(input,INPUT,FOLLOW_INPUT_in_def90);  
             stream_INPUT.add(INPUT6);
 
 
             pushFollow(FOLLOW_vars_in_def92);
             vars7=vars();
 
             state._fsp--;
 
             stream_vars.add(vars7.getTree());
 
             PR8=(Token)match(input,PR,FOLLOW_PR_in_def94);  
             stream_PR.add(PR8);
 
 
             pushFollow(FOLLOW_commands_in_def96);
             commands9=commands();
 
             state._fsp--;
 
             stream_commands.add(commands9.getTree());
 
             PR10=(Token)match(input,PR,FOLLOW_PR_in_def98);  
             stream_PR.add(PR10);
 
 
             OUTPUT11=(Token)match(input,OUTPUT,FOLLOW_OUTPUT_in_def100);  
             stream_OUTPUT.add(OUTPUT11);
 
 
             pushFollow(FOLLOW_vars_in_def102);
             vars12=vars();
 
             state._fsp--;
 
             stream_vars.add(vars12.getTree());
 
             // AST REWRITE
            // elements: vars, PR, PR, vars, commands
             // token labels: 
             // rule labels: retval
             // token list labels: 
             // rule list labels: 
             // wildcard labels: 
             retval.tree = root_0;
             RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
 
             root_0 = (Object)adaptor.nil();
             // 32:3: -> vars PR commands PR vars
             {
                 adaptor.addChild(root_0, stream_vars.nextTree());
 
                 adaptor.addChild(root_0, 
                 stream_PR.nextNode()
                 );
 
                 adaptor.addChild(root_0, stream_commands.nextTree());
 
                 adaptor.addChild(root_0, 
                 stream_PR.nextNode()
                 );
 
                 adaptor.addChild(root_0, stream_vars.nextTree());
 
             }
 
 
             retval.tree = root_0;
 
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
     // $ANTLR end "def"
 
 
     public static class vars_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "vars"
     // WhileParser.g:35:1: vars : VAR ( COMMA VAR )* -> VAR ( VAR )* ;
     public final WhileParser.vars_return vars() throws RecognitionException {
         WhileParser.vars_return retval = new WhileParser.vars_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token VAR13=null;
         Token COMMA14=null;
         Token VAR15=null;
 
         Object VAR13_tree=null;
         Object COMMA14_tree=null;
         Object VAR15_tree=null;
         RewriteRuleTokenStream stream_VAR=new RewriteRuleTokenStream(adaptor,"token VAR");
         RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
 
         try {
             // WhileParser.g:35:6: ( VAR ( COMMA VAR )* -> VAR ( VAR )* )
             // WhileParser.g:35:9: VAR ( COMMA VAR )*
             {
             VAR13=(Token)match(input,VAR,FOLLOW_VAR_in_vars127);  
             stream_VAR.add(VAR13);
 
 
             // WhileParser.g:35:13: ( COMMA VAR )*
             loop2:
             do {
                 int alt2=2;
                 int LA2_0 = input.LA(1);
 
                 if ( (LA2_0==COMMA) ) {
                     alt2=1;
                 }
 
 
                 switch (alt2) {
             	case 1 :
             	    // WhileParser.g:35:14: COMMA VAR
             	    {
             	    COMMA14=(Token)match(input,COMMA,FOLLOW_COMMA_in_vars130);  
             	    stream_COMMA.add(COMMA14);
 
 
             	    VAR15=(Token)match(input,VAR,FOLLOW_VAR_in_vars132);  
             	    stream_VAR.add(VAR15);
 
 
             	    }
             	    break;
 
             	default :
             	    break loop2;
                 }
             } while (true);
 
 
             // AST REWRITE
             // elements: VAR, VAR
             // token labels: 
             // rule labels: retval
             // token list labels: 
             // rule list labels: 
             // wildcard labels: 
             retval.tree = root_0;
             RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
 
             root_0 = (Object)adaptor.nil();
             // 36:3: -> VAR ( VAR )*
             {
                 adaptor.addChild(root_0, 
                 stream_VAR.nextNode()
                 );
 
                 // WhileParser.g:36:10: ( VAR )*
                 while ( stream_VAR.hasNext() ) {
                     adaptor.addChild(root_0, 
                     stream_VAR.nextNode()
                     );
 
                 }
                 stream_VAR.reset();
 
             }
 
 
             retval.tree = root_0;
 
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
     // $ANTLR end "vars"
 
 
     public static class commands_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "commands"
     // WhileParser.g:39:1: commands : command ( PV ( command )? )* ;
     public final WhileParser.commands_return commands() throws RecognitionException {
         WhileParser.commands_return retval = new WhileParser.commands_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token PV17=null;
         WhileParser.command_return command16 =null;
 
         WhileParser.command_return command18 =null;
 
 
         Object PV17_tree=null;
 
         try {
             // WhileParser.g:39:10: ( command ( PV ( command )? )* )
             // WhileParser.g:39:12: command ( PV ( command )? )*
             {
             root_0 = (Object)adaptor.nil();
 
 
             pushFollow(FOLLOW_command_in_commands155);
             command16=command();
 
             state._fsp--;
 
             adaptor.addChild(root_0, command16.getTree());
 
             // WhileParser.g:39:20: ( PV ( command )? )*
             loop4:
             do {
                 int alt4=2;
                 int LA4_0 = input.LA(1);
 
                 if ( (LA4_0==PV) ) {
                     alt4=1;
                 }
 
 
                 switch (alt4) {
             	case 1 :
             	    // WhileParser.g:39:21: PV ( command )?
             	    {
             	    PV17=(Token)match(input,PV,FOLLOW_PV_in_commands158); 
             	    PV17_tree = 
             	    (Object)adaptor.create(PV17)
             	    ;
             	    adaptor.addChild(root_0, PV17_tree);
 
 
             	    // WhileParser.g:39:24: ( command )?
             	    int alt3=2;
             	    int LA3_0 = input.LA(1);
 
             	    if ( (LA3_0==FOR||LA3_0==IF||LA3_0==NOP||(LA3_0 >= VAR && LA3_0 <= WHILE)) ) {
             	        alt3=1;
             	    }
             	    switch (alt3) {
             	        case 1 :
             	            // WhileParser.g:39:24: command
             	            {
             	            pushFollow(FOLLOW_command_in_commands160);
             	            command18=command();
 
             	            state._fsp--;
 
             	            adaptor.addChild(root_0, command18.getTree());
 
             	            }
             	            break;
 
             	    }
 
 
             	    }
             	    break;
 
             	default :
             	    break loop4;
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
     // $ANTLR end "commands"
 
 
     public static class command_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "command"
     // WhileParser.g:42:1: command : ( NOP | vars AFF exprs | WHILE expr DO commands OD | IF expr THEN commands ( ELSE commands )? FI | FOR expr DO commands OD );
     public final WhileParser.command_return command() throws RecognitionException {
         WhileParser.command_return retval = new WhileParser.command_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token NOP19=null;
         Token AFF21=null;
         Token WHILE23=null;
         Token DO25=null;
         Token OD27=null;
         Token IF28=null;
         Token THEN30=null;
         Token ELSE32=null;
         Token FI34=null;
         Token FOR35=null;
         Token DO37=null;
         Token OD39=null;
         WhileParser.vars_return vars20 =null;
 
         WhileParser.exprs_return exprs22 =null;
 
         WhileParser.expr_return expr24 =null;
 
         WhileParser.commands_return commands26 =null;
 
         WhileParser.expr_return expr29 =null;
 
         WhileParser.commands_return commands31 =null;
 
         WhileParser.commands_return commands33 =null;
 
         WhileParser.expr_return expr36 =null;
 
         WhileParser.commands_return commands38 =null;
 
 
         Object NOP19_tree=null;
         Object AFF21_tree=null;
         Object WHILE23_tree=null;
         Object DO25_tree=null;
         Object OD27_tree=null;
         Object IF28_tree=null;
         Object THEN30_tree=null;
         Object ELSE32_tree=null;
         Object FI34_tree=null;
         Object FOR35_tree=null;
         Object DO37_tree=null;
         Object OD39_tree=null;
 
         try {
             // WhileParser.g:42:9: ( NOP | vars AFF exprs | WHILE expr DO commands OD | IF expr THEN commands ( ELSE commands )? FI | FOR expr DO commands OD )
             int alt6=5;
             switch ( input.LA(1) ) {
             case NOP:
                 {
                 alt6=1;
                 }
                 break;
             case VAR:
                 {
                 alt6=2;
                 }
                 break;
             case WHILE:
                 {
                 alt6=3;
                 }
                 break;
             case IF:
                 {
                 alt6=4;
                 }
                 break;
             case FOR:
                 {
                 alt6=5;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 6, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt6) {
                 case 1 :
                     // WhileParser.g:42:11: NOP
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     NOP19=(Token)match(input,NOP,FOLLOW_NOP_in_command173); 
                     NOP19_tree = 
                     (Object)adaptor.create(NOP19)
                     ;
                     adaptor.addChild(root_0, NOP19_tree);
 
 
                     }
                     break;
                 case 2 :
                     // WhileParser.g:43:4: vars AFF exprs
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     pushFollow(FOLLOW_vars_in_command178);
                     vars20=vars();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, vars20.getTree());
 
                     AFF21=(Token)match(input,AFF,FOLLOW_AFF_in_command180); 
                     AFF21_tree = 
                     (Object)adaptor.create(AFF21)
                     ;
                     adaptor.addChild(root_0, AFF21_tree);
 
 
                     pushFollow(FOLLOW_exprs_in_command182);
                     exprs22=exprs();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, exprs22.getTree());
 
                     }
                     break;
                 case 3 :
                     // WhileParser.g:44:4: WHILE expr DO commands OD
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     WHILE23=(Token)match(input,WHILE,FOLLOW_WHILE_in_command187); 
                     WHILE23_tree = 
                     (Object)adaptor.create(WHILE23)
                     ;
                     adaptor.addChild(root_0, WHILE23_tree);
 
 
                     pushFollow(FOLLOW_expr_in_command189);
                     expr24=expr();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, expr24.getTree());
 
                     DO25=(Token)match(input,DO,FOLLOW_DO_in_command191); 
                     DO25_tree = 
                     (Object)adaptor.create(DO25)
                     ;
                     adaptor.addChild(root_0, DO25_tree);
 
 
                     pushFollow(FOLLOW_commands_in_command193);
                     commands26=commands();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, commands26.getTree());
 
                     OD27=(Token)match(input,OD,FOLLOW_OD_in_command195); 
                     OD27_tree = 
                     (Object)adaptor.create(OD27)
                     ;
                     adaptor.addChild(root_0, OD27_tree);
 
 
                     }
                     break;
                 case 4 :
                     // WhileParser.g:45:4: IF expr THEN commands ( ELSE commands )? FI
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     IF28=(Token)match(input,IF,FOLLOW_IF_in_command200); 
                     IF28_tree = 
                     (Object)adaptor.create(IF28)
                     ;
                     adaptor.addChild(root_0, IF28_tree);
 
 
                     pushFollow(FOLLOW_expr_in_command202);
                     expr29=expr();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, expr29.getTree());
 
                     THEN30=(Token)match(input,THEN,FOLLOW_THEN_in_command204); 
                     THEN30_tree = 
                     (Object)adaptor.create(THEN30)
                     ;
                     adaptor.addChild(root_0, THEN30_tree);
 
 
                     pushFollow(FOLLOW_commands_in_command206);
                     commands31=commands();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, commands31.getTree());
 
                     // WhileParser.g:45:26: ( ELSE commands )?
                     int alt5=2;
                     int LA5_0 = input.LA(1);
 
                     if ( (LA5_0==ELSE) ) {
                         alt5=1;
                     }
                     switch (alt5) {
                         case 1 :
                             // WhileParser.g:45:27: ELSE commands
                             {
                             ELSE32=(Token)match(input,ELSE,FOLLOW_ELSE_in_command209); 
                             ELSE32_tree = 
                             (Object)adaptor.create(ELSE32)
                             ;
                             adaptor.addChild(root_0, ELSE32_tree);
 
 
                             pushFollow(FOLLOW_commands_in_command211);
                             commands33=commands();
 
                             state._fsp--;
 
                             adaptor.addChild(root_0, commands33.getTree());
 
                             }
                             break;
 
                     }
 
 
                     FI34=(Token)match(input,FI,FOLLOW_FI_in_command215); 
                     FI34_tree = 
                     (Object)adaptor.create(FI34)
                     ;
                     adaptor.addChild(root_0, FI34_tree);
 
 
                     }
                     break;
                 case 5 :
                     // WhileParser.g:46:4: FOR expr DO commands OD
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     FOR35=(Token)match(input,FOR,FOLLOW_FOR_in_command220); 
                     FOR35_tree = 
                     (Object)adaptor.create(FOR35)
                     ;
                     adaptor.addChild(root_0, FOR35_tree);
 
 
                     pushFollow(FOLLOW_expr_in_command222);
                     expr36=expr();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, expr36.getTree());
 
                     DO37=(Token)match(input,DO,FOLLOW_DO_in_command224); 
                     DO37_tree = 
                     (Object)adaptor.create(DO37)
                     ;
                     adaptor.addChild(root_0, DO37_tree);
 
 
                     pushFollow(FOLLOW_commands_in_command226);
                     commands38=commands();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, commands38.getTree());
 
                     OD39=(Token)match(input,OD,FOLLOW_OD_in_command228); 
                     OD39_tree = 
                     (Object)adaptor.create(OD39)
                     ;
                     adaptor.addChild(root_0, OD39_tree);
 
 
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
     // $ANTLR end "command"
 
 
     public static class exprs_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "exprs"
     // WhileParser.g:49:1: exprs : expr ( ( COMMA expr ) | expraff )* ;
     public final WhileParser.exprs_return exprs() throws RecognitionException {
         WhileParser.exprs_return retval = new WhileParser.exprs_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token COMMA41=null;
         WhileParser.expr_return expr40 =null;
 
         WhileParser.expr_return expr42 =null;
 
         WhileParser.expraff_return expraff43 =null;
 
 
         Object COMMA41_tree=null;
 
         try {
             // WhileParser.g:49:8: ( expr ( ( COMMA expr ) | expraff )* )
             // WhileParser.g:49:10: expr ( ( COMMA expr ) | expraff )*
             {
             root_0 = (Object)adaptor.nil();
 
 
             pushFollow(FOLLOW_expr_in_exprs240);
             expr40=expr();
 
             state._fsp--;
 
             adaptor.addChild(root_0, expr40.getTree());
 
             // WhileParser.g:49:15: ( ( COMMA expr ) | expraff )*
             loop7:
             do {
                 int alt7=3;
                 int LA7_0 = input.LA(1);
 
                 if ( (LA7_0==COMMA) ) {
                     alt7=1;
                 }
                 else if ( (LA7_0==ISEQUAL) ) {
                     alt7=2;
                 }
 
 
                 switch (alt7) {
             	case 1 :
             	    // WhileParser.g:49:16: ( COMMA expr )
             	    {
             	    // WhileParser.g:49:16: ( COMMA expr )
             	    // WhileParser.g:49:17: COMMA expr
             	    {
             	    COMMA41=(Token)match(input,COMMA,FOLLOW_COMMA_in_exprs244); 
             	    COMMA41_tree = 
             	    (Object)adaptor.create(COMMA41)
             	    ;
             	    adaptor.addChild(root_0, COMMA41_tree);
 
 
             	    pushFollow(FOLLOW_expr_in_exprs246);
             	    expr42=expr();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, expr42.getTree());
 
             	    }
 
 
             	    }
             	    break;
             	case 2 :
             	    // WhileParser.g:49:31: expraff
             	    {
             	    pushFollow(FOLLOW_expraff_in_exprs251);
             	    expraff43=expraff();
 
             	    state._fsp--;
 
             	    adaptor.addChild(root_0, expraff43.getTree());
 
             	    }
             	    break;
 
             	default :
             	    break loop7;
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
     // $ANTLR end "exprs"
 
 
     public static class expr_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "expr"
     // WhileParser.g:52:1: expr : ( NIL | VAR | ID | PO par PF );
     public final WhileParser.expr_return expr() throws RecognitionException {
         WhileParser.expr_return retval = new WhileParser.expr_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token NIL44=null;
         Token VAR45=null;
         Token ID46=null;
         Token PO47=null;
         Token PF49=null;
         WhileParser.par_return par48 =null;
 
 
         Object NIL44_tree=null;
         Object VAR45_tree=null;
         Object ID46_tree=null;
         Object PO47_tree=null;
         Object PF49_tree=null;
 
         try {
             // WhileParser.g:52:6: ( NIL | VAR | ID | PO par PF )
             int alt8=4;
             switch ( input.LA(1) ) {
             case NIL:
                 {
                 alt8=1;
                 }
                 break;
             case VAR:
                 {
                 alt8=2;
                 }
                 break;
             case ID:
                 {
                 alt8=3;
                 }
                 break;
             case PO:
                 {
                 alt8=4;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 8, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt8) {
                 case 1 :
                     // WhileParser.g:52:8: NIL
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     NIL44=(Token)match(input,NIL,FOLLOW_NIL_in_expr263); 
                     NIL44_tree = 
                     (Object)adaptor.create(NIL44)
                     ;
                     adaptor.addChild(root_0, NIL44_tree);
 
 
                     }
                     break;
                 case 2 :
                     // WhileParser.g:53:4: VAR
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     VAR45=(Token)match(input,VAR,FOLLOW_VAR_in_expr268); 
                     VAR45_tree = 
                     (Object)adaptor.create(VAR45)
                     ;
                     adaptor.addChild(root_0, VAR45_tree);
 
 
                     }
                     break;
                 case 3 :
                     // WhileParser.g:54:4: ID
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     ID46=(Token)match(input,ID,FOLLOW_ID_in_expr273); 
                     ID46_tree = 
                     (Object)adaptor.create(ID46)
                     ;
                     adaptor.addChild(root_0, ID46_tree);
 
 
                     }
                     break;
                 case 4 :
                     // WhileParser.g:55:4: PO par PF
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     PO47=(Token)match(input,PO,FOLLOW_PO_in_expr279); 
                     PO47_tree = 
                     (Object)adaptor.create(PO47)
                     ;
                     adaptor.addChild(root_0, PO47_tree);
 
 
                     pushFollow(FOLLOW_par_in_expr281);
                     par48=par();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, par48.getTree());
 
                     PF49=(Token)match(input,PF,FOLLOW_PF_in_expr283); 
                     PF49_tree = 
                     (Object)adaptor.create(PF49)
                     ;
                     adaptor.addChild(root_0, PF49_tree);
 
 
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
     // $ANTLR end "expr"
 
 
     public static class par_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "par"
     // WhileParser.g:58:1: par : ( CONS expr expr | HD expr | TL expr | CALL ID expr ( expr )* | ID expr ( expr )* );
     public final WhileParser.par_return par() throws RecognitionException {
         WhileParser.par_return retval = new WhileParser.par_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token CONS50=null;
         Token HD53=null;
         Token TL55=null;
         Token CALL57=null;
         Token ID58=null;
         Token ID61=null;
         WhileParser.expr_return expr51 =null;
 
         WhileParser.expr_return expr52 =null;
 
         WhileParser.expr_return expr54 =null;
 
         WhileParser.expr_return expr56 =null;
 
         WhileParser.expr_return expr59 =null;
 
         WhileParser.expr_return expr60 =null;
 
         WhileParser.expr_return expr62 =null;
 
         WhileParser.expr_return expr63 =null;
 
 
         Object CONS50_tree=null;
         Object HD53_tree=null;
         Object TL55_tree=null;
         Object CALL57_tree=null;
         Object ID58_tree=null;
         Object ID61_tree=null;
 
         try {
             // WhileParser.g:58:5: ( CONS expr expr | HD expr | TL expr | CALL ID expr ( expr )* | ID expr ( expr )* )
             int alt11=5;
             switch ( input.LA(1) ) {
             case CONS:
                 {
                 alt11=1;
                 }
                 break;
             case HD:
                 {
                 alt11=2;
                 }
                 break;
             case TL:
                 {
                 alt11=3;
                 }
                 break;
             case CALL:
                 {
                 alt11=4;
                 }
                 break;
             case ID:
                 {
                 alt11=5;
                 }
                 break;
             default:
                 NoViableAltException nvae =
                     new NoViableAltException("", 11, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt11) {
                 case 1 :
                     // WhileParser.g:58:7: CONS expr expr
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     CONS50=(Token)match(input,CONS,FOLLOW_CONS_in_par293); 
                     CONS50_tree = 
                     (Object)adaptor.create(CONS50)
                     ;
                     adaptor.addChild(root_0, CONS50_tree);
 
 
                     pushFollow(FOLLOW_expr_in_par295);
                     expr51=expr();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, expr51.getTree());
 
                     pushFollow(FOLLOW_expr_in_par297);
                     expr52=expr();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, expr52.getTree());
 
                     }
                     break;
                 case 2 :
                     // WhileParser.g:59:4: HD expr
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     HD53=(Token)match(input,HD,FOLLOW_HD_in_par302); 
                     HD53_tree = 
                     (Object)adaptor.create(HD53)
                     ;
                     adaptor.addChild(root_0, HD53_tree);
 
 
                     pushFollow(FOLLOW_expr_in_par304);
                     expr54=expr();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, expr54.getTree());
 
                     }
                     break;
                 case 3 :
                     // WhileParser.g:60:4: TL expr
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     TL55=(Token)match(input,TL,FOLLOW_TL_in_par309); 
                     TL55_tree = 
                     (Object)adaptor.create(TL55)
                     ;
                     adaptor.addChild(root_0, TL55_tree);
 
 
                     pushFollow(FOLLOW_expr_in_par311);
                     expr56=expr();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, expr56.getTree());
 
                     }
                     break;
                 case 4 :
                     // WhileParser.g:61:4: CALL ID expr ( expr )*
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     CALL57=(Token)match(input,CALL,FOLLOW_CALL_in_par316); 
                     CALL57_tree = 
                     (Object)adaptor.create(CALL57)
                     ;
                     adaptor.addChild(root_0, CALL57_tree);
 
 
                     ID58=(Token)match(input,ID,FOLLOW_ID_in_par318); 
                     ID58_tree = 
                     (Object)adaptor.create(ID58)
                     ;
                     adaptor.addChild(root_0, ID58_tree);
 
 
                     pushFollow(FOLLOW_expr_in_par320);
                     expr59=expr();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, expr59.getTree());
 
                     // WhileParser.g:61:17: ( expr )*
                     loop9:
                     do {
                         int alt9=2;
                         int LA9_0 = input.LA(1);
 
                         if ( (LA9_0==ID||LA9_0==NIL||LA9_0==PO||LA9_0==VAR) ) {
                             alt9=1;
                         }
 
 
                         switch (alt9) {
                     	case 1 :
                     	    // WhileParser.g:61:18: expr
                     	    {
                     	    pushFollow(FOLLOW_expr_in_par323);
                     	    expr60=expr();
 
                     	    state._fsp--;
 
                     	    adaptor.addChild(root_0, expr60.getTree());
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop9;
                         }
                     } while (true);
 
 
                     }
                     break;
                 case 5 :
                     // WhileParser.g:62:4: ID expr ( expr )*
                     {
                     root_0 = (Object)adaptor.nil();
 
 
                     ID61=(Token)match(input,ID,FOLLOW_ID_in_par330); 
                     ID61_tree = 
                     (Object)adaptor.create(ID61)
                     ;
                     adaptor.addChild(root_0, ID61_tree);
 
 
                     pushFollow(FOLLOW_expr_in_par332);
                     expr62=expr();
 
                     state._fsp--;
 
                     adaptor.addChild(root_0, expr62.getTree());
 
                     // WhileParser.g:62:12: ( expr )*
                     loop10:
                     do {
                         int alt10=2;
                         int LA10_0 = input.LA(1);
 
                         if ( (LA10_0==ID||LA10_0==NIL||LA10_0==PO||LA10_0==VAR) ) {
                             alt10=1;
                         }
 
 
                         switch (alt10) {
                     	case 1 :
                     	    // WhileParser.g:62:13: expr
                     	    {
                     	    pushFollow(FOLLOW_expr_in_par335);
                     	    expr63=expr();
 
                     	    state._fsp--;
 
                     	    adaptor.addChild(root_0, expr63.getTree());
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop10;
                         }
                     } while (true);
 
 
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
     // $ANTLR end "par"
 
 
     public static class expraff_return extends ParserRuleReturnScope {
         Object tree;
         public Object getTree() { return tree; }
     };
 
 
     // $ANTLR start "expraff"
     // WhileParser.g:65:1: expraff : ISEQUAL expr ;
     public final WhileParser.expraff_return expraff() throws RecognitionException {
         WhileParser.expraff_return retval = new WhileParser.expraff_return();
         retval.start = input.LT(1);
 
 
         Object root_0 = null;
 
         Token ISEQUAL64=null;
         WhileParser.expr_return expr65 =null;
 
 
         Object ISEQUAL64_tree=null;
 
         try {
             // WhileParser.g:65:10: ( ISEQUAL expr )
             // WhileParser.g:65:12: ISEQUAL expr
             {
             root_0 = (Object)adaptor.nil();
 
 
             ISEQUAL64=(Token)match(input,ISEQUAL,FOLLOW_ISEQUAL_in_expraff349); 
             ISEQUAL64_tree = 
             (Object)adaptor.create(ISEQUAL64)
             ;
             adaptor.addChild(root_0, ISEQUAL64_tree);
 
 
             pushFollow(FOLLOW_expr_in_expraff351);
             expr65=expr();
 
             state._fsp--;
 
             adaptor.addChild(root_0, expr65.getTree());
 
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
     // $ANTLR end "expraff"
 
     // Delegated rules
 
 
  
 
     public static final BitSet FOLLOW_prog_in_file51 = new BitSet(new long[]{0x0000100000000002L});
     public static final BitSet FOLLOW_PROGRAM_in_prog65 = new BitSet(new long[]{0x0000000004000000L});
     public static final BitSet FOLLOW_ID_in_prog67 = new BitSet(new long[]{0x0000000000020000L});
     public static final BitSet FOLLOW_DP_in_prog69 = new BitSet(new long[]{0x0000000020000000L});
     public static final BitSet FOLLOW_def_in_prog71 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_INPUT_in_def90 = new BitSet(new long[]{0x0010000000000000L});
     public static final BitSet FOLLOW_vars_in_def92 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_PR_in_def94 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_def96 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_PR_in_def98 = new BitSet(new long[]{0x0000008000000000L});
     public static final BitSet FOLLOW_OUTPUT_in_def100 = new BitSet(new long[]{0x0010000000000000L});
     public static final BitSet FOLLOW_vars_in_def102 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_VAR_in_vars127 = new BitSet(new long[]{0x0000000000000102L});
     public static final BitSet FOLLOW_COMMA_in_vars130 = new BitSet(new long[]{0x0010000000000000L});
     public static final BitSet FOLLOW_VAR_in_vars132 = new BitSet(new long[]{0x0000000000000102L});
     public static final BitSet FOLLOW_command_in_commands155 = new BitSet(new long[]{0x0000400000000002L});
     public static final BitSet FOLLOW_PV_in_commands158 = new BitSet(new long[]{0x0030401009000002L});
     public static final BitSet FOLLOW_command_in_commands160 = new BitSet(new long[]{0x0000400000000002L});
     public static final BitSet FOLLOW_NOP_in_command173 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_vars_in_command178 = new BitSet(new long[]{0x0000000000000010L});
     public static final BitSet FOLLOW_AFF_in_command180 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_exprs_in_command182 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_WHILE_in_command187 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_command189 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_DO_in_command191 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_command193 = new BitSet(new long[]{0x0000004000000000L});
     public static final BitSet FOLLOW_OD_in_command195 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_IF_in_command200 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_command202 = new BitSet(new long[]{0x0004000000000000L});
     public static final BitSet FOLLOW_THEN_in_command204 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_command206 = new BitSet(new long[]{0x0000000000240000L});
     public static final BitSet FOLLOW_ELSE_in_command209 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_command211 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_FI_in_command215 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_FOR_in_command220 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_command222 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_DO_in_command224 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_command226 = new BitSet(new long[]{0x0000004000000000L});
     public static final BitSet FOLLOW_OD_in_command228 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_expr_in_exprs240 = new BitSet(new long[]{0x0000000040000102L});
     public static final BitSet FOLLOW_COMMA_in_exprs244 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_exprs246 = new BitSet(new long[]{0x0000000040000102L});
     public static final BitSet FOLLOW_expraff_in_exprs251 = new BitSet(new long[]{0x0000000040000102L});
     public static final BitSet FOLLOW_NIL_in_expr263 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_VAR_in_expr268 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_ID_in_expr273 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_PO_in_expr279 = new BitSet(new long[]{0x0008000006001080L});
     public static final BitSet FOLLOW_par_in_expr281 = new BitSet(new long[]{0x0000010000000000L});
     public static final BitSet FOLLOW_PF_in_expr283 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_CONS_in_par293 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par295 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par297 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_HD_in_par302 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par304 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_TL_in_par309 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par311 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_CALL_in_par316 = new BitSet(new long[]{0x0000000004000000L});
     public static final BitSet FOLLOW_ID_in_par318 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par320 = new BitSet(new long[]{0x0010040404000002L});
     public static final BitSet FOLLOW_expr_in_par323 = new BitSet(new long[]{0x0010040404000002L});
     public static final BitSet FOLLOW_ID_in_par330 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par332 = new BitSet(new long[]{0x0010040404000002L});
     public static final BitSet FOLLOW_expr_in_par335 = new BitSet(new long[]{0x0010040404000002L});
     public static final BitSet FOLLOW_ISEQUAL_in_expraff349 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_expraff351 = new BitSet(new long[]{0x0000000000000002L});
 
 }
