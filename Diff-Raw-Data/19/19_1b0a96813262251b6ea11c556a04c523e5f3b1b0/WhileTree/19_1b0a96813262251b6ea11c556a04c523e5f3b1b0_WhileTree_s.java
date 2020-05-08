 package analyze;
// $ANTLR 3.4 WhileTree.g 2013-01-11 08:24:27
 
 import antlr.StringUtils;
 import java.io.*;
 import antlr.collections.AST;
 import java.lang.StringBuilder;
 
 
 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class WhileTree extends TreeParser {
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
     public TreeParser[] getDelegates() {
         return new TreeParser[] {};
     }
 
     // delegators
 
 
     public WhileTree(TreeNodeStream input) {
         this(input, new RecognizerSharedState());
     }
     public WhileTree(TreeNodeStream input, RecognizerSharedState state) {
         super(input, state);
     }
 
     public String[] getTokenNames() { return WhileTree.tokenNames; }
     public String getGrammarFileName() { return "WhileTree.g"; }
 
 
     	/** Default inset */
     	int inset = 4;
     	
     	/** While struct inset */
     	int whileInset = 4;
     	
     	/** If struct inset */
     	int ifInset = 4;
     	
     	/**	For struct inset */
     	int forInset = 4;
     	
     	/** Current inset */
     	int currentInset = 0;
     	
     	/** Default line length */
     	int lineBreakLength = 80;
     	
     	/** Current length of the line */
     	int currentLineLength = 0;
     	
     	/** Maximum tabs allowed per on a line */
     	int maxTabsAllowed = 0;
     	
     	/** Contain the formated code */
     	StringBuilder sb = new StringBuilder();
     	
     	/** String used as default tabulation */
     	String tab = " ";
 
     	/**
     	 * Append formated code to the StringBuilder
     	 * @param s The String to be added
     	 */
     	protected void print(String s){
     		currentLineLength += s.length();
     		if(currentLineLength > lineBreakLength && s.length() <= lineBreakLength){
     			sb.append(" \\");
     			newLine();
     			indent();
     			print(s.startsWith(" ")?s.substring(1):s);
     		}
     		else
     			sb.append(s);
     	}
 
     	/**
     	 * Print the correct indentation for the current line
     	*/
     	protected void indent(){
     		int i = currentInset;
     		if(i > maxTabsAllowed)
     			i = maxTabsAllowed;
     		while(i!=0){
     			print(tab);
     			i--;
     		}
     	}
     	
     	/**
     	 * Give the whole formated code
     	 * @return A String with the code
     	 */
     	public String getFormatedCode(){
     		return sb.toString();
     	}
 
     	/**
     	 * Display a new line
     	 */
     	protected void newLine(){
     		currentLineLength = 0;
     		sb.append("\r\n");
     	}
     	
     	/**
     	 * Define the inset for the next lines
     	 * @param inset The new inset
     	 */
     	private void setInset(int inset){
     		currentInset = inset;
     	}
     	
     	/**
     	 * Define the default indents
     	 * @param def Default indentation
     	 * @param whileIns While struct indentation
     	 * @param ifIns If struct indentation
     	 * @param forIns For struct indentation
     	 * @param page Maximum line length
     	 */
     	public void setIndent(int def, int whileIns, int ifIns, int forIns, int page){
     		inset = def;
     		whileInset = whileIns;
     		ifInset = ifIns;
     		forInset = forIns;
     		lineBreakLength = page;
     		maxTabsAllowed = (page / tab.length())-2;
     	}
     	
 
 
 
     // $ANTLR start "file"
     // WhileTree.g:119:1: file : prog ( prog )* ;
     public final void file() throws RecognitionException {
         try {
             // WhileTree.g:119:6: ( prog ( prog )* )
             // WhileTree.g:119:8: prog ( prog )*
             {
             pushFollow(FOLLOW_prog_in_file54);
             prog();
 
             state._fsp--;
             if (state.failed) return ;
 
             // WhileTree.g:119:13: ( prog )*
             loop1:
             do {
                 int alt1=2;
                 int LA1_0 = input.LA(1);
 
                 if ( (LA1_0==ID) ) {
                     alt1=1;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // WhileTree.g:119:13: prog
             	    {
             	    pushFollow(FOLLOW_prog_in_file56);
             	    prog();
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    }
             	    break;
 
             	default :
             	    break loop1;
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
         return ;
     }
     // $ANTLR end "file"
 
 
 
     // $ANTLR start "prog"
     // WhileTree.g:122:1: prog : id= ID definition= def ;
     public final void prog() throws RecognitionException {
         CommonTree id=null;
 
         try {
             // WhileTree.g:122:6: (id= ID definition= def )
             // WhileTree.g:122:8: id= ID definition= def
             {
             id=(CommonTree)match(input,ID,FOLLOW_ID_in_prog69); if (state.failed) return ;
 
             if ( state.backtracking==0 ) {
             			setInset(0);
             			String name = id.getText();
             			print("program "+name+":");
             			newLine();
             		}
 
             pushFollow(FOLLOW_def_in_prog81);
             def();
 
             state._fsp--;
             if (state.failed) return ;
 
             if ( state.backtracking==0 ) {
             			newLine();
             		}
 
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
     // $ANTLR end "prog"
 
 
 
     // $ANTLR start "def"
     // WhileTree.g:136:1: def : vars PR commands PR vars ;
     public final void def() throws RecognitionException {
         try {
             // WhileTree.g:136:5: ( vars PR commands PR vars )
             // WhileTree.g:136:7: vars PR commands PR vars
             {
             if ( state.backtracking==0 ) {
             			print("read ");
             		}
 
             pushFollow(FOLLOW_vars_in_def99);
             vars();
 
             state._fsp--;
             if (state.failed) return ;
 
             match(input,PR,FOLLOW_PR_in_def101); if (state.failed) return ;
 
             if ( state.backtracking==0 ) {
             			newLine();
             			print("%");
             			newLine();
             			setInset(inset);
             		}
 
             pushFollow(FOLLOW_commands_in_def109);
             commands();
 
             state._fsp--;
             if (state.failed) return ;
 
             match(input,PR,FOLLOW_PR_in_def111); if (state.failed) return ;
 
             if ( state.backtracking==0 ) {
             			setInset(0);
             			newLine();
             			print("%");
             			newLine();
             			print("write ");
             		}
 
             pushFollow(FOLLOW_vars_in_def119);
             vars();
 
             state._fsp--;
             if (state.failed) return ;
 
             if ( state.backtracking==0 ) {
             			newLine();
             		}
 
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
     // $ANTLR end "def"
 
 
 
     // $ANTLR start "vars"
     // WhileTree.g:160:1: vars : var1= VAR (var2= VAR )* ;
     public final void vars() throws RecognitionException {
         CommonTree var1=null;
         CommonTree var2=null;
 
         try {
             // WhileTree.g:160:6: (var1= VAR (var2= VAR )* )
             // WhileTree.g:160:9: var1= VAR (var2= VAR )*
             {
             var1=(CommonTree)match(input,VAR,FOLLOW_VAR_in_vars136); if (state.failed) return ;
 
             if ( state.backtracking==0 ) {
             			print(var1.getText());
             		}
 
             // WhileTree.g:164:3: (var2= VAR )*
             loop2:
             do {
                 int alt2=2;
                 int LA2_0 = input.LA(1);
 
                 if ( (LA2_0==VAR) ) {
                     alt2=1;
                 }
 
 
                 switch (alt2) {
             	case 1 :
             	    // WhileTree.g:164:4: var2= VAR
             	    {
             	    var2=(CommonTree)match(input,VAR,FOLLOW_VAR_in_vars148); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {
             	    			print(", "+var2.getText());
             	    		}
 
             	    }
             	    break;
 
             	default :
             	    break loop2;
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
         return ;
     }
     // $ANTLR end "vars"
 
 
 
     // $ANTLR start "commands"
     // WhileTree.g:171:1: commands : command ( PV ( command )? )* ;
     public final void commands() throws RecognitionException {
         try {
             // WhileTree.g:171:10: ( command ( PV ( command )? )* )
             // WhileTree.g:171:13: command ( PV ( command )? )*
             {
             if ( state.backtracking==0 ) {
             			indent();
             		}
 
             pushFollow(FOLLOW_command_in_commands172);
             command();
 
             state._fsp--;
             if (state.failed) return ;
 
             // WhileTree.g:174:11: ( PV ( command )? )*
             loop4:
             do {
                 int alt4=2;
                 int LA4_0 = input.LA(1);
 
                 if ( (LA4_0==PV) ) {
                     alt4=1;
                 }
 
 
                 switch (alt4) {
             	case 1 :
             	    // WhileTree.g:174:12: PV ( command )?
             	    {
             	    match(input,PV,FOLLOW_PV_in_commands175); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {
             	    			print(" ;");
             	    			newLine();
             	    			indent();
             	    		}
 
             	    // WhileTree.g:180:3: ( command )?
             	    int alt3=2;
             	    int LA3_0 = input.LA(1);
 
             	    if ( (LA3_0==FOR||LA3_0==IF||LA3_0==NOP||(LA3_0 >= VAR && LA3_0 <= WHILE)) ) {
             	        alt3=1;
             	    }
             	    switch (alt3) {
             	        case 1 :
             	            // WhileTree.g:180:3: command
             	            {
             	            pushFollow(FOLLOW_command_in_commands183);
             	            command();
 
             	            state._fsp--;
             	            if (state.failed) return ;
 
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
     // $ANTLR end "commands"
 
 
 
     // $ANTLR start "command"
     // WhileTree.g:183:1: command : (nop= NOP | vars AFF exprs | WHILE expr DO commands OD | IF expr THEN commands ( ELSE commands )? FI | FOR expr DO commands OD );
     public final void command() throws RecognitionException {
         CommonTree nop=null;
 
         try {
             // WhileTree.g:183:9: (nop= NOP | vars AFF exprs | WHILE expr DO commands OD | IF expr THEN commands ( ELSE commands )? FI | FOR expr DO commands OD )
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
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 6, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt6) {
                 case 1 :
                     // WhileTree.g:183:11: nop= NOP
                     {
                     nop=(CommonTree)match(input,NOP,FOLLOW_NOP_in_command198); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(nop.getText());
                     		}
 
                     }
                     break;
                 case 2 :
                     // WhileTree.g:188:4: vars AFF exprs
                     {
                     pushFollow(FOLLOW_vars_in_command210);
                     vars();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,AFF,FOLLOW_AFF_in_command212); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(" := ");
                     		}
 
                     pushFollow(FOLLOW_exprs_in_command220);
                     exprs();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 3 :
                     // WhileTree.g:194:4: WHILE expr DO commands OD
                     {
                     match(input,WHILE,FOLLOW_WHILE_in_command229); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print("while ");
                     		}
 
                     pushFollow(FOLLOW_expr_in_command237);
                     expr();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,DO,FOLLOW_DO_in_command239); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(" do");
                     			newLine();
                     			setInset(currentInset+whileInset);
                     		}
 
                     pushFollow(FOLLOW_commands_in_command247);
                     commands();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,OD,FOLLOW_OD_in_command249); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			setInset(currentInset-whileInset);
                     			newLine();
                     			indent();
                     			print("od");
                     		}
 
                     }
                     break;
                 case 4 :
                     // WhileTree.g:212:4: IF expr THEN commands ( ELSE commands )? FI
                     {
                     match(input,IF,FOLLOW_IF_in_command261); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print("if ");
                     		}
 
                     pushFollow(FOLLOW_expr_in_command269);
                     expr();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,THEN,FOLLOW_THEN_in_command271); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(" then");
                     			newLine();
                     			setInset(currentInset+ifInset);
                     		}
 
                     pushFollow(FOLLOW_commands_in_command279);
                     commands();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			setInset(currentInset-ifInset);
                     			newLine();
                     		}
 
                     // WhileTree.g:227:3: ( ELSE commands )?
                     int alt5=2;
                     int LA5_0 = input.LA(1);
 
                     if ( (LA5_0==ELSE) ) {
                         alt5=1;
                     }
                     switch (alt5) {
                         case 1 :
                             // WhileTree.g:227:4: ELSE commands
                             {
                             match(input,ELSE,FOLLOW_ELSE_in_command288); if (state.failed) return ;
 
                             if ( state.backtracking==0 ) {	indent();
                             			print("else");
                             			newLine();
                             			setInset(currentInset+ifInset);
                             		}
 
                             pushFollow(FOLLOW_commands_in_command296);
                             commands();
 
                             state._fsp--;
                             if (state.failed) return ;
 
                             if ( state.backtracking==0 ) {
                             			setInset(currentInset-ifInset);
                             			newLine();	
                             		}
 
                             }
                             break;
 
                     }
 
 
                     match(input,FI,FOLLOW_FI_in_command307); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			indent();
                     			print("fi");
                     		}
 
                     }
                     break;
                 case 5 :
                     // WhileTree.g:244:4: FOR expr DO commands OD
                     {
                     match(input,FOR,FOLLOW_FOR_in_command319); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print("for ");
                     		}
 
                     pushFollow(FOLLOW_expr_in_command327);
                     expr();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,DO,FOLLOW_DO_in_command329); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(" do");
                     			newLine();
                     			setInset(currentInset+forInset);
                     		}
 
                     pushFollow(FOLLOW_commands_in_command337);
                     commands();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,OD,FOLLOW_OD_in_command339); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			setInset(currentInset-forInset);
                     			newLine();
                     			indent();
                     			print("od");
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
     // $ANTLR end "command"
 
 
 
     // $ANTLR start "exprs"
     // WhileTree.g:264:1: exprs : expr ( ( COMMA expr ) | expraff )* ;
     public final void exprs() throws RecognitionException {
         try {
             // WhileTree.g:264:7: ( expr ( ( COMMA expr ) | expraff )* )
             // WhileTree.g:264:9: expr ( ( COMMA expr ) | expraff )*
             {
             pushFollow(FOLLOW_expr_in_exprs354);
             expr();
 
             state._fsp--;
             if (state.failed) return ;
 
             if ( state.backtracking==0 ) {
 
             		}
 
             // WhileTree.g:268:3: ( ( COMMA expr ) | expraff )*
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
             	    // WhileTree.g:268:4: ( COMMA expr )
             	    {
             	    // WhileTree.g:268:4: ( COMMA expr )
             	    // WhileTree.g:268:5: COMMA expr
             	    {
             	    match(input,COMMA,FOLLOW_COMMA_in_exprs364); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {
             	    			print(", ");
             	    		}
 
             	    pushFollow(FOLLOW_expr_in_exprs373);
             	    expr();
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    }
 
 
             	    }
             	    break;
             	case 2 :
             	    // WhileTree.g:272:11: expraff
             	    {
             	    pushFollow(FOLLOW_expraff_in_exprs378);
             	    expraff();
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    }
             	    break;
 
             	default :
             	    break loop7;
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
         return ;
     }
     // $ANTLR end "exprs"
 
 
 
     // $ANTLR start "expr"
     // WhileTree.g:276:1: expr : (nil= NIL |var= VAR |id= ID | PO par PF );
     public final void expr() throws RecognitionException {
         CommonTree nil=null;
         CommonTree var=null;
         CommonTree id=null;
 
         try {
             // WhileTree.g:276:6: (nil= NIL |var= VAR |id= ID | PO par PF )
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
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 8, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt8) {
                 case 1 :
                     // WhileTree.g:277:3: nil= NIL
                     {
                     nil=(CommonTree)match(input,NIL,FOLLOW_NIL_in_expr396); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print("nil");
                     		}
 
                     }
                     break;
                 case 2 :
                     // WhileTree.g:282:4: var= VAR
                     {
                     var=(CommonTree)match(input,VAR,FOLLOW_VAR_in_expr410); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(var.getText());
                     		}
 
                     }
                     break;
                 case 3 :
                     // WhileTree.g:287:4: id= ID
                     {
                     id=(CommonTree)match(input,ID,FOLLOW_ID_in_expr424); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(id.getText());
                     		}
 
                     }
                     break;
                 case 4 :
                     // WhileTree.g:292:4: PO par PF
                     {
                     match(input,PO,FOLLOW_PO_in_expr436); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print("(");
                     		}
 
                     pushFollow(FOLLOW_par_in_expr444);
                     par();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,PF,FOLLOW_PF_in_expr446); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(")");
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
     // $ANTLR end "expr"
 
 
 
     // $ANTLR start "par"
     // WhileTree.g:304:1: par : ( CONS expr expr | HD expr | TL expr | CALL id= ID expr ( expr )* |id= ID expr ( expr )* );
     public final void par() throws RecognitionException {
         CommonTree id=null;
 
         try {
             // WhileTree.g:304:5: ( CONS expr expr | HD expr | TL expr | CALL id= ID expr ( expr )* |id= ID expr ( expr )* )
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
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 11, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt11) {
                 case 1 :
                     // WhileTree.g:304:7: CONS expr expr
                     {
                     match(input,CONS,FOLLOW_CONS_in_par464); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print("cons ");
                     		}
 
                     pushFollow(FOLLOW_expr_in_par472);
                     expr();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(" ");
                     		}
 
                     pushFollow(FOLLOW_expr_in_par480);
                     expr();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 2 :
                     // WhileTree.g:315:4: HD expr
                     {
                     match(input,HD,FOLLOW_HD_in_par490); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print("hd ");
                     		}
 
                     pushFollow(FOLLOW_expr_in_par498);
                     expr();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 3 :
                     // WhileTree.g:322:4: TL expr
                     {
                     match(input,TL,FOLLOW_TL_in_par507); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print("tl ");
                     		}
 
                     pushFollow(FOLLOW_expr_in_par515);
                     expr();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 4 :
                     // WhileTree.g:328:4: CALL id= ID expr ( expr )*
                     {
                     match(input,CALL,FOLLOW_CALL_in_par523); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print("call ");
                     		}
 
                     id=(CommonTree)match(input,ID,FOLLOW_ID_in_par533); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(id.getText()+" ");
                     		}
 
                     pushFollow(FOLLOW_expr_in_par541);
                     expr();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     // WhileTree.g:336:7: ( expr )*
                     loop9:
                     do {
                         int alt9=2;
                         int LA9_0 = input.LA(1);
 
                         if ( (LA9_0==ID||LA9_0==NIL||LA9_0==PO||LA9_0==VAR) ) {
                             alt9=1;
                         }
 
 
                         switch (alt9) {
                     	case 1 :
                     	    // WhileTree.g:337:3: expr
                     	    {
                     	    if ( state.backtracking==0 ) {
                     	    			print(" ");
                     	    		}
 
                     	    pushFollow(FOLLOW_expr_in_par550);
                     	    expr();
 
                     	    state._fsp--;
                     	    if (state.failed) return ;
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop9;
                         }
                     } while (true);
 
 
                     }
                     break;
                 case 5 :
                     // WhileTree.g:342:4: id= ID expr ( expr )*
                     {
                     id=(CommonTree)match(input,ID,FOLLOW_ID_in_par562); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {
                     			print(id.getText()+" ");
                     		}
 
                     pushFollow(FOLLOW_expr_in_par570);
                     expr();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     // WhileTree.g:346:7: ( expr )*
                     loop10:
                     do {
                         int alt10=2;
                         int LA10_0 = input.LA(1);
 
                         if ( (LA10_0==ID||LA10_0==NIL||LA10_0==PO||LA10_0==VAR) ) {
                             alt10=1;
                         }
 
 
                         switch (alt10) {
                     	case 1 :
                     	    // WhileTree.g:347:3: expr
                     	    {
                     	    if ( state.backtracking==0 ) {
                     	    			print(" ");
                     	    		}
 
                     	    pushFollow(FOLLOW_expr_in_par579);
                     	    expr();
 
                     	    state._fsp--;
                     	    if (state.failed) return ;
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop10;
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
     // $ANTLR end "par"
 
 
 
     // $ANTLR start "expraff"
     // WhileTree.g:354:1: expraff : ISEQUAL expr ;
     public final void expraff() throws RecognitionException {
         try {
             // WhileTree.g:354:9: ( ISEQUAL expr )
             // WhileTree.g:354:11: ISEQUAL expr
             {
             match(input,ISEQUAL,FOLLOW_ISEQUAL_in_expraff592); if (state.failed) return ;
 
             if ( state.backtracking==0 ) {
             			print(" =? ");
             		}
 
             pushFollow(FOLLOW_expr_in_expraff600);
             expr();
 
             state._fsp--;
             if (state.failed) return ;
 
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
     // $ANTLR end "expraff"
 
     // Delegated rules
 
 
  
 
     public static final BitSet FOLLOW_prog_in_file54 = new BitSet(new long[]{0x0000000004000002L});
     public static final BitSet FOLLOW_prog_in_file56 = new BitSet(new long[]{0x0000000004000002L});
     public static final BitSet FOLLOW_ID_in_prog69 = new BitSet(new long[]{0x0010000000000000L});
     public static final BitSet FOLLOW_def_in_prog81 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_vars_in_def99 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_PR_in_def101 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_def109 = new BitSet(new long[]{0x0000080000000000L});
     public static final BitSet FOLLOW_PR_in_def111 = new BitSet(new long[]{0x0010000000000000L});
     public static final BitSet FOLLOW_vars_in_def119 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_VAR_in_vars136 = new BitSet(new long[]{0x0010000000000002L});
     public static final BitSet FOLLOW_VAR_in_vars148 = new BitSet(new long[]{0x0010000000000002L});
     public static final BitSet FOLLOW_command_in_commands172 = new BitSet(new long[]{0x0000400000000002L});
     public static final BitSet FOLLOW_PV_in_commands175 = new BitSet(new long[]{0x0030401009000002L});
     public static final BitSet FOLLOW_command_in_commands183 = new BitSet(new long[]{0x0000400000000002L});
     public static final BitSet FOLLOW_NOP_in_command198 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_vars_in_command210 = new BitSet(new long[]{0x0000000000000010L});
     public static final BitSet FOLLOW_AFF_in_command212 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_exprs_in_command220 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_WHILE_in_command229 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_command237 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_DO_in_command239 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_command247 = new BitSet(new long[]{0x0000004000000000L});
     public static final BitSet FOLLOW_OD_in_command249 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_IF_in_command261 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_command269 = new BitSet(new long[]{0x0004000000000000L});
     public static final BitSet FOLLOW_THEN_in_command271 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_command279 = new BitSet(new long[]{0x0000000000240000L});
     public static final BitSet FOLLOW_ELSE_in_command288 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_command296 = new BitSet(new long[]{0x0000000000200000L});
     public static final BitSet FOLLOW_FI_in_command307 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_FOR_in_command319 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_command327 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_DO_in_command329 = new BitSet(new long[]{0x0030001009000000L});
     public static final BitSet FOLLOW_commands_in_command337 = new BitSet(new long[]{0x0000004000000000L});
     public static final BitSet FOLLOW_OD_in_command339 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_expr_in_exprs354 = new BitSet(new long[]{0x0000000040000102L});
     public static final BitSet FOLLOW_COMMA_in_exprs364 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_exprs373 = new BitSet(new long[]{0x0000000040000102L});
     public static final BitSet FOLLOW_expraff_in_exprs378 = new BitSet(new long[]{0x0000000040000102L});
     public static final BitSet FOLLOW_NIL_in_expr396 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_VAR_in_expr410 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_ID_in_expr424 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_PO_in_expr436 = new BitSet(new long[]{0x0008000006001080L});
     public static final BitSet FOLLOW_par_in_expr444 = new BitSet(new long[]{0x0000010000000000L});
     public static final BitSet FOLLOW_PF_in_expr446 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_CONS_in_par464 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par472 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par480 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_HD_in_par490 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par498 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_TL_in_par507 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par515 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_CALL_in_par523 = new BitSet(new long[]{0x0000000004000000L});
     public static final BitSet FOLLOW_ID_in_par533 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par541 = new BitSet(new long[]{0x0010040404000002L});
     public static final BitSet FOLLOW_expr_in_par550 = new BitSet(new long[]{0x0010040404000002L});
     public static final BitSet FOLLOW_ID_in_par562 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_par570 = new BitSet(new long[]{0x0010040404000002L});
     public static final BitSet FOLLOW_expr_in_par579 = new BitSet(new long[]{0x0010040404000002L});
     public static final BitSet FOLLOW_ISEQUAL_in_expraff592 = new BitSet(new long[]{0x0010040404000000L});
     public static final BitSet FOLLOW_expr_in_expraff600 = new BitSet(new long[]{0x0000000000000002L});
 
 }
