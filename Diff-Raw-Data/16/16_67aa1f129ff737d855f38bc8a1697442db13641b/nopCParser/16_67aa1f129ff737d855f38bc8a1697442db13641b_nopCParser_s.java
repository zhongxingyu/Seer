// $ANTLR 3.4 nopC.g 2012-05-12 16:20:06
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.TreeMap;
 import src.*;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
  import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 @SuppressWarnings({"all", "warnings", "unchecked"})
 public class nopCParser extends Parser {
     public static final String[] tokenNames = new String[] {
         "<invalid>", "<EOR>", "<DOWN>", "<UP>", "COMMENT", "LETTER", "LINE_COMMENT", "NAME", "WERT", "WS", "'!='", "'%'", "'%='", "'&&'", "'&'", "'&='", "'('", "')'", "'*'", "'*='", "'+'", "'++'", "'+='", "','", "'-'", "'--'", "'-='", "'/'", "'/='", "':'", "';'", "'<'", "'<<'", "'<<='", "'<='", "'='", "'=='", "'>'", "'>='", "'>>'", "'>>='", "'?'", "'^'", "'^='", "'bool'", "'break'", "'continue'", "'else'", "'for'", "'goto'", "'if'", "'int'", "'return'", "'void'", "'while'", "'{'", "'|'", "'|='", "'||'", "'}'"
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
     public static final int T__46=46;
     public static final int T__47=47;
     public static final int T__48=48;
     public static final int T__49=49;
     public static final int T__50=50;
     public static final int T__51=51;
     public static final int T__52=52;
     public static final int T__53=53;
     public static final int T__54=54;
     public static final int T__55=55;
     public static final int T__56=56;
     public static final int T__57=57;
     public static final int T__58=58;
     public static final int T__59=59;
     public static final int COMMENT=4;
     public static final int LETTER=5;
     public static final int LINE_COMMENT=6;
     public static final int NAME=7;
     public static final int WERT=8;
     public static final int WS=9;
 
     // delegates
     public Parser[] getDelegates() {
         return new Parser[] {};
     }
 
     // delegators
 
 
     public nopCParser(TokenStream input) {
         this(input, new RecognizerSharedState());
     }
     public nopCParser(TokenStream input, RecognizerSharedState state) {
         super(input, state);
         this.state.ruleMemo = new HashMap[106+1];
          
 
     }
 
     public String[] getTokenNames() { return nopCParser.tokenNames; }
     public String getGrammarFileName() { return "nopC.g"; }
 
 
         TreeMap<String, FunctionDefinition> functionTable = new TreeMap<String, FunctionDefinition>();
         HashMap<String, String> varTable = new HashMap<String, String>();
     		GenericStatement hackStore1;
     		
     		FileWriter fstream;
     	  BufferedWriter out;
     		String version = "0.0000000000000001";
 
 
 
         private String getDateTime() {
             DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
             Date date = new Date();
             return dateFormat.format(date);
         }
 
     		void startWritingASM () {
     			try {
     			fstream = new FileWriter("asm.txt");
     			out = new BufferedWriter(fstream);
     			writeInit();	// sets pc to start label
     			 } catch (Exception e) {
     				System.err.println("Error: " + e.getMessage());
     			}
     		}
     		
     		void stopWritingASM () {
     			try {
     			out.close();
     			 } catch (Exception e) {
     				System.err.println("Error: " + e.getMessage());
     			}
     		}
     	
     		void writeASM (String str) {
     			try{
     		  	out.write(str);
     		  }catch (Exception e){
     		  	System.err.println("Error: " + e.getMessage());
     		  }
     		}
     		
     		void writeSetRegToMemory(String reg, String varname, HashMap<String, String> scope) {
     			writeASM("SET [" + scope.get(varname) + "], " + reg + "\n" );
     		}
     		
     		void writeSetImmidiateToReg(String reg, String immidiate) {
     			writeASM("SET " + reg + ", " + immidiate + "\n" );
     		}
     		
     		void writeSetVarToReg(String reg, String varname, HashMap<String, String> scope) {
     			writeASM("SET " + reg + ", [" + scope.get(varname) + "]\n" );
     		}
     		
     		
     		
     		
     		void writeInit () {
     			writeASM("\n;COMPILED BY NOPC VERSION " + version + " ON " + getDateTime() +  " \n");
     			writeASM(";NOPC WAS WRITTEN BY STEPHAN WYPLER AND ALEXANDER RUST\n");
     			writeASM(";SEE https://github.com/stylpen/nop FOR MORE INFORMATION\n\n");
     			writeASM(";BEGIN ASM\n");			
     			writeASM("SET PC, START \n");
     		}
     				
     		void writeDSEG () {
     			System.out.println("Writing DSEG");
     			writeASM("; BEGIN DSEG\n");
     			
     			for (String label : varTable.keySet()) {
     				writeASM(":" + label + " dat " +  String.format("%04x", Integer.parseInt(varTable.get(label))) + "\n");				
     			}
     			writeASM(":START JSR " + functionTable.get("main").getLabel() + "\n");				// Writes jump to main function so we can set pc to START at the beginning wihtout knowing where the main function will be
     		}
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
     		
 
 
 
     // $ANTLR start "cFile"
     // nopC.g:141:1: cFile returns [GenericStatement ret] : ( globalFunctionOrStatement[cFile] )+ ;
     public final GenericStatement cFile() throws RecognitionException {
         GenericStatement ret = null;
 
         int cFile_StartIndex = input.index();
 
 
         HashMap<String, String> scope = new HashMap<String, String>();
         GenericStatement cFile = new GenericStatement(scope, functionTable, varTable);
         startWritingASM();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 1) ) { return ret; }
 
             // nopC.g:147:2: ( ( globalFunctionOrStatement[cFile] )+ )
             // nopC.g:148:2: ( globalFunctionOrStatement[cFile] )+
             {
             // nopC.g:148:2: ( globalFunctionOrStatement[cFile] )+
             int cnt1=0;
             loop1:
             do {
                 int alt1=2;
                 int LA1_0 = input.LA(1);
 
                 if ( (LA1_0==44||LA1_0==51||LA1_0==53) ) {
                     alt1=1;
                 }
 
 
                 switch (alt1) {
             	case 1 :
             	    // nopC.g:148:2: globalFunctionOrStatement[cFile]
             	    {
             	    pushFollow(FOLLOW_globalFunctionOrStatement_in_cFile71);
             	    globalFunctionOrStatement(cFile);
 
             	    state._fsp--;
             	    if (state.failed) return ret;
 
             	    }
             	    break;
 
             	default :
             	    if ( cnt1 >= 1 ) break loop1;
             	    if (state.backtracking>0) {state.failed=true; return ret;}
                         EarlyExitException eee =
                             new EarlyExitException(1, input);
                         throw eee;
                 }
                 cnt1++;
             } while (true);
 
 
             if ( state.backtracking==0 ) {writeDSEG(); stopWritingASM();}
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 1, cFile_StartIndex); }
 
         }
         return ret;
     }
     // $ANTLR end "cFile"
 
 
 
     // $ANTLR start "globalFunctionOrStatement"
     // nopC.g:153:1: globalFunctionOrStatement[GenericStatement parent] returns [GenericStatement ret] : ( ( typeSpecifier NAME ( '=' | ';' | ',' ) )=> globalVariableDeclaration[parent] | ( typeSpecifier NAME '(' )=> functionDefinition[parent] );
     public final GenericStatement globalFunctionOrStatement(GenericStatement parent) throws RecognitionException {
         GenericStatement ret = null;
 
         int globalFunctionOrStatement_StartIndex = input.index();
 
         nopCParser.globalVariableDeclaration_return globalVariableDeclaration1 =null;
 
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return ret; }
 
             // nopC.g:154:2: ( ( typeSpecifier NAME ( '=' | ';' | ',' ) )=> globalVariableDeclaration[parent] | ( typeSpecifier NAME '(' )=> functionDefinition[parent] )
             int alt2=2;
             int LA2_0 = input.LA(1);
 
             if ( (LA2_0==44||LA2_0==51||LA2_0==53) ) {
                 int LA2_1 = input.LA(2);
 
                 if ( (LA2_1==NAME) ) {
                     int LA2_2 = input.LA(3);
 
                     if ( (synpred4_nopC()) ) {
                         alt2=1;
                     }
                     else if ( (synpred5_nopC()) ) {
                         alt2=2;
                     }
                     else {
                         if (state.backtracking>0) {state.failed=true; return ret;}
                         NoViableAltException nvae =
                             new NoViableAltException("", 2, 2, input);
 
                         throw nvae;
 
                     }
                 }
                 else {
                     if (state.backtracking>0) {state.failed=true; return ret;}
                     NoViableAltException nvae =
                         new NoViableAltException("", 2, 1, input);
 
                     throw nvae;
 
                 }
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ret;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 2, 0, input);
 
                 throw nvae;
 
             }
             switch (alt2) {
                 case 1 :
                     // nopC.g:155:2: ( typeSpecifier NAME ( '=' | ';' | ',' ) )=> globalVariableDeclaration[parent]
                     {
                     pushFollow(FOLLOW_globalVariableDeclaration_in_globalFunctionOrStatement114);
                     globalVariableDeclaration1=globalVariableDeclaration(parent);
 
                     state._fsp--;
                     if (state.failed) return ret;
 
                     if ( state.backtracking==0 ) {ret = (globalVariableDeclaration1!=null?globalVariableDeclaration1.ret:null);}
 
                     }
                     break;
                 case 2 :
                     // nopC.g:156:4: ( typeSpecifier NAME '(' )=> functionDefinition[parent]
                     {
                     pushFollow(FOLLOW_functionDefinition_in_globalFunctionOrStatement133);
                     functionDefinition(parent);
 
                     state._fsp--;
                     if (state.failed) return ret;
 
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
             if ( state.backtracking>0 ) { memoize(input, 2, globalFunctionOrStatement_StartIndex); }
 
         }
         return ret;
     }
     // $ANTLR end "globalFunctionOrStatement"
 
 
     public static class globalVariableDeclaration_return extends ParserRuleReturnScope {
         public GenericStatement ret;
     };
 
 
     // $ANTLR start "globalVariableDeclaration"
     // nopC.g:159:1: globalVariableDeclaration[GenericStatement parent] returns [GenericStatement ret] : typeSpecifier globalVariableDeclarationList[parent] ';' ;
     public final nopCParser.globalVariableDeclaration_return globalVariableDeclaration(GenericStatement parent) throws RecognitionException {
         nopCParser.globalVariableDeclaration_return retval = new nopCParser.globalVariableDeclaration_return();
         retval.start = input.LT(1);
 
         int globalVariableDeclaration_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return retval; }
 
             // nopC.g:160:2: ( typeSpecifier globalVariableDeclarationList[parent] ';' )
             // nopC.g:162:2: typeSpecifier globalVariableDeclarationList[parent] ';'
             {
             pushFollow(FOLLOW_typeSpecifier_in_globalVariableDeclaration154);
             typeSpecifier();
 
             state._fsp--;
             if (state.failed) return retval;
 
             pushFollow(FOLLOW_globalVariableDeclarationList_in_globalVariableDeclaration156);
             globalVariableDeclarationList(parent);
 
             state._fsp--;
             if (state.failed) return retval;
 
             match(input,30,FOLLOW_30_in_globalVariableDeclaration159); if (state.failed) return retval;
 
             if ( state.backtracking==0 ) {System.out.println(input.toString(retval.start,input.LT(-1)));}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 3, globalVariableDeclaration_StartIndex); }
 
         }
         return retval;
     }
     // $ANTLR end "globalVariableDeclaration"
 
 
 
     // $ANTLR start "globalVariableDeclarationList"
     // nopC.g:170:1: globalVariableDeclarationList[GenericStatement parent] returns [GenericStatement ret] : (n1= NAME ( '=' w1= WERT )? ) ( ',' (n2= NAME ( '=' w2= WERT )? ) )* ;
     public final GenericStatement globalVariableDeclarationList(GenericStatement parent) throws RecognitionException {
         GenericStatement ret = null;
 
         int globalVariableDeclarationList_StartIndex = input.index();
 
         Token n1=null;
         Token w1=null;
         Token n2=null;
         Token w2=null;
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return ret; }
 
             // nopC.g:171:2: ( (n1= NAME ( '=' w1= WERT )? ) ( ',' (n2= NAME ( '=' w2= WERT )? ) )* )
             // nopC.g:172:2: (n1= NAME ( '=' w1= WERT )? ) ( ',' (n2= NAME ( '=' w2= WERT )? ) )*
             {
             // nopC.g:172:2: (n1= NAME ( '=' w1= WERT )? )
             // nopC.g:172:3: n1= NAME ( '=' w1= WERT )?
             {
             n1=(Token)match(input,NAME,FOLLOW_NAME_in_globalVariableDeclarationList189); if (state.failed) return ret;
 
             // nopC.g:172:12: ( '=' w1= WERT )?
             int alt3=2;
             int LA3_0 = input.LA(1);
 
             if ( (LA3_0==35) ) {
                 alt3=1;
             }
             switch (alt3) {
                 case 1 :
                     // nopC.g:172:13: '=' w1= WERT
                     {
                     match(input,35,FOLLOW_35_in_globalVariableDeclarationList191); if (state.failed) return ret;
 
                     w1=(Token)match(input,WERT,FOLLOW_WERT_in_globalVariableDeclarationList197); if (state.failed) return ret;
 
                     }
                     break;
 
             }
 
 
             }
 
 
             if ( state.backtracking==0 ) {parent.addVarToScope((n1!=null?n1.getText():null), (w1!=null?w1.getText():null));}
 
             // nopC.g:172:73: ( ',' (n2= NAME ( '=' w2= WERT )? ) )*
             loop5:
             do {
                 int alt5=2;
                 int LA5_0 = input.LA(1);
 
                 if ( (LA5_0==23) ) {
                     alt5=1;
                 }
 
 
                 switch (alt5) {
             	case 1 :
             	    // nopC.g:172:74: ',' (n2= NAME ( '=' w2= WERT )? )
             	    {
             	    match(input,23,FOLLOW_23_in_globalVariableDeclarationList204); if (state.failed) return ret;
 
             	    // nopC.g:172:78: (n2= NAME ( '=' w2= WERT )? )
             	    // nopC.g:172:79: n2= NAME ( '=' w2= WERT )?
             	    {
             	    n2=(Token)match(input,NAME,FOLLOW_NAME_in_globalVariableDeclarationList211); if (state.failed) return ret;
 
             	    // nopC.g:172:88: ( '=' w2= WERT )?
             	    int alt4=2;
             	    int LA4_0 = input.LA(1);
 
             	    if ( (LA4_0==35) ) {
             	        alt4=1;
             	    }
             	    switch (alt4) {
             	        case 1 :
             	            // nopC.g:172:89: '=' w2= WERT
             	            {
             	            match(input,35,FOLLOW_35_in_globalVariableDeclarationList213); if (state.failed) return ret;
 
             	            w2=(Token)match(input,WERT,FOLLOW_WERT_in_globalVariableDeclarationList219); if (state.failed) return ret;
 
             	            }
             	            break;
 
             	    }
 
 
             	    }
 
 
             	    if ( state.backtracking==0 ) {parent.addVarToScope((n2!=null?n2.getText():null), (w2!=null?w2.getText():null));}
 
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
             if ( state.backtracking>0 ) { memoize(input, 4, globalVariableDeclarationList_StartIndex); }
 
         }
         return ret;
     }
     // $ANTLR end "globalVariableDeclarationList"
 
 
     public static class variableDeclaration_return extends ParserRuleReturnScope {
         public GenericStatement ret;
     };
 
 
     // $ANTLR start "variableDeclaration"
     // nopC.g:175:1: variableDeclaration[GenericStatement parent] returns [GenericStatement ret] : typeSpecifier variableDeclarationList[parent] ';' ;
     public final nopCParser.variableDeclaration_return variableDeclaration(GenericStatement parent) throws RecognitionException {
         nopCParser.variableDeclaration_return retval = new nopCParser.variableDeclaration_return();
         retval.start = input.LT(1);
 
         int variableDeclaration_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return retval; }
 
             // nopC.g:176:2: ( typeSpecifier variableDeclarationList[parent] ';' )
             // nopC.g:177:2: typeSpecifier variableDeclarationList[parent] ';'
             {
             pushFollow(FOLLOW_typeSpecifier_in_variableDeclaration245);
             typeSpecifier();
 
             state._fsp--;
             if (state.failed) return retval;
 
             pushFollow(FOLLOW_variableDeclarationList_in_variableDeclaration247);
             variableDeclarationList(parent);
 
             state._fsp--;
             if (state.failed) return retval;
 
             match(input,30,FOLLOW_30_in_variableDeclaration250); if (state.failed) return retval;
 
             if ( state.backtracking==0 ) {System.out.println(input.toString(retval.start,input.LT(-1)));}
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 5, variableDeclaration_StartIndex); }
 
         }
         return retval;
     }
     // $ANTLR end "variableDeclaration"
 
 
 
     // $ANTLR start "variableDeclarationList"
     // nopC.g:180:1: variableDeclarationList[GenericStatement parent] returns [GenericStatement ret] : (n1= NAME ( '=' expression[parent] )? ) ( ',' (n2= NAME ( '=' expression[parent] )? ) )* ;
     public final GenericStatement variableDeclarationList(GenericStatement parent) throws RecognitionException {
         GenericStatement ret = null;
 
         int variableDeclarationList_StartIndex = input.index();
 
         Token n1=null;
         Token n2=null;
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return ret; }
 
             // nopC.g:181:2: ( (n1= NAME ( '=' expression[parent] )? ) ( ',' (n2= NAME ( '=' expression[parent] )? ) )* )
             // nopC.g:183:2: (n1= NAME ( '=' expression[parent] )? ) ( ',' (n2= NAME ( '=' expression[parent] )? ) )*
             {
             // nopC.g:183:2: (n1= NAME ( '=' expression[parent] )? )
             // nopC.g:183:3: n1= NAME ( '=' expression[parent] )?
             {
             n1=(Token)match(input,NAME,FOLLOW_NAME_in_variableDeclarationList278); if (state.failed) return ret;
 
             // nopC.g:183:13: ( '=' expression[parent] )?
             int alt6=2;
             int LA6_0 = input.LA(1);
 
             if ( (LA6_0==35) ) {
                 alt6=1;
             }
             switch (alt6) {
                 case 1 :
                     // nopC.g:183:14: '=' expression[parent]
                     {
                     match(input,35,FOLLOW_35_in_variableDeclarationList281); if (state.failed) return ret;
 
                     pushFollow(FOLLOW_expression_in_variableDeclarationList283);
                     expression(parent);
 
                     state._fsp--;
                     if (state.failed) return ret;
 
                     }
                     break;
 
             }
 
 
             }
 
 
             if ( state.backtracking==0 ) {parent.addVarToScope((n1!=null?n1.getText():null), null); writeSetRegToMemory("X", (n1!=null?n1.getText():null), parent.getScope());     }
 
             // nopC.g:183:139: ( ',' (n2= NAME ( '=' expression[parent] )? ) )*
             loop8:
             do {
                 int alt8=2;
                 int LA8_0 = input.LA(1);
 
                 if ( (LA8_0==23) ) {
                     alt8=1;
                 }
 
 
                 switch (alt8) {
             	case 1 :
             	    // nopC.g:183:140: ',' (n2= NAME ( '=' expression[parent] )? )
             	    {
             	    match(input,23,FOLLOW_23_in_variableDeclarationList291); if (state.failed) return ret;
 
             	    // nopC.g:183:144: (n2= NAME ( '=' expression[parent] )? )
             	    // nopC.g:183:145: n2= NAME ( '=' expression[parent] )?
             	    {
             	    n2=(Token)match(input,NAME,FOLLOW_NAME_in_variableDeclarationList298); if (state.failed) return ret;
 
             	    // nopC.g:183:155: ( '=' expression[parent] )?
             	    int alt7=2;
             	    int LA7_0 = input.LA(1);
 
             	    if ( (LA7_0==35) ) {
             	        alt7=1;
             	    }
             	    switch (alt7) {
             	        case 1 :
             	            // nopC.g:183:156: '=' expression[parent]
             	            {
             	            match(input,35,FOLLOW_35_in_variableDeclarationList301); if (state.failed) return ret;
 
             	            pushFollow(FOLLOW_expression_in_variableDeclarationList303);
             	            expression(parent);
 
             	            state._fsp--;
             	            if (state.failed) return ret;
 
             	            }
             	            break;
 
             	    }
 
 
             	    }
 
 
             	    if ( state.backtracking==0 ) {parent.addVarToScope((n2!=null?n2.getText():null), null);}
 
             	    }
             	    break;
 
             	default :
             	    break loop8;
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
             if ( state.backtracking>0 ) { memoize(input, 6, variableDeclarationList_StartIndex); }
 
         }
         return ret;
     }
     // $ANTLR end "variableDeclarationList"
 
 
     public static class functionDefinition_return extends ParserRuleReturnScope {
         public GenericStatement ret;
     };
 
 
     // $ANTLR start "functionDefinition"
     // nopC.g:187:1: functionDefinition[GenericStatement parent] returns [GenericStatement ret] : typeSpecifier NAME '(' parameterList[functionDefinition] ')' '{' ( statement[functionDefinition] )* '}' ;
     public final nopCParser.functionDefinition_return functionDefinition(GenericStatement parent) throws RecognitionException {
         nopCParser.functionDefinition_return retval = new nopCParser.functionDefinition_return();
         retval.start = input.LT(1);
 
         int functionDefinition_StartIndex = input.index();
 
         Token NAME2=null;
 
 
         FunctionDefinition functionDefinition = new FunctionDefinition(parent.getScope(), functionTable, varTable);
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return retval; }
 
             // nopC.g:191:2: ( typeSpecifier NAME '(' parameterList[functionDefinition] ')' '{' ( statement[functionDefinition] )* '}' )
             // nopC.g:192:3: typeSpecifier NAME '(' parameterList[functionDefinition] ')' '{' ( statement[functionDefinition] )* '}'
             {
             pushFollow(FOLLOW_typeSpecifier_in_functionDefinition336);
             typeSpecifier();
 
             state._fsp--;
             if (state.failed) return retval;
 
             NAME2=(Token)match(input,NAME,FOLLOW_NAME_in_functionDefinition338); if (state.failed) return retval;
 
             if ( state.backtracking==0 ) {functionDefinition.addFun((NAME2!=null?NAME2.getText():null)); writeASM(functionDefinition.getLabel() + ": \n"); }
 
             match(input,16,FOLLOW_16_in_functionDefinition342); if (state.failed) return retval;
 
             pushFollow(FOLLOW_parameterList_in_functionDefinition344);
             parameterList(functionDefinition);
 
             state._fsp--;
             if (state.failed) return retval;
 
             match(input,17,FOLLOW_17_in_functionDefinition347); if (state.failed) return retval;
 
             match(input,55,FOLLOW_55_in_functionDefinition349); if (state.failed) return retval;
 
             // nopC.g:192:160: ( statement[functionDefinition] )*
             loop9:
             do {
                 int alt9=2;
                 int LA9_0 = input.LA(1);
 
                 if ( ((LA9_0 >= NAME && LA9_0 <= WERT)||LA9_0==16||LA9_0==21||LA9_0==25||LA9_0==30||(LA9_0 >= 44 && LA9_0 <= 46)||(LA9_0 >= 48 && LA9_0 <= 54)) ) {
                     alt9=1;
                 }
 
 
                 switch (alt9) {
             	case 1 :
             	    // nopC.g:192:160: statement[functionDefinition]
             	    {
             	    pushFollow(FOLLOW_statement_in_functionDefinition351);
             	    statement(functionDefinition);
 
             	    state._fsp--;
             	    if (state.failed) return retval;
 
             	    }
             	    break;
 
             	default :
             	    break loop9;
                 }
             } while (true);
 
 
             match(input,59,FOLLOW_59_in_functionDefinition355); if (state.failed) return retval;
 
             if ( state.backtracking==0 ) {System.out.println(input.toString(retval.start,input.LT(-1))); }
 
             }
 
             retval.stop = input.LT(-1);
 
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 7, functionDefinition_StartIndex); }
 
         }
         return retval;
     }
     // $ANTLR end "functionDefinition"
 
 
 
     // $ANTLR start "parameterList"
     // nopC.g:196:1: parameterList[FunctionDefinition parent] : ( typeSpecifier (n1= NAME ) ( ',' typeSpecifier (n2= NAME ) )* )? ;
     public final void parameterList(FunctionDefinition parent) throws RecognitionException {
         int parameterList_StartIndex = input.index();
 
         Token n1=null;
         Token n2=null;
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return ; }
 
             // nopC.g:197:2: ( ( typeSpecifier (n1= NAME ) ( ',' typeSpecifier (n2= NAME ) )* )? )
             // nopC.g:198:3: ( typeSpecifier (n1= NAME ) ( ',' typeSpecifier (n2= NAME ) )* )?
             {
             // nopC.g:198:3: ( typeSpecifier (n1= NAME ) ( ',' typeSpecifier (n2= NAME ) )* )?
             int alt11=2;
             int LA11_0 = input.LA(1);
 
             if ( (LA11_0==44||LA11_0==51||LA11_0==53) ) {
                 alt11=1;
             }
             switch (alt11) {
                 case 1 :
                     // nopC.g:198:4: typeSpecifier (n1= NAME ) ( ',' typeSpecifier (n2= NAME ) )*
                     {
                     pushFollow(FOLLOW_typeSpecifier_in_parameterList375);
                     typeSpecifier();
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     // nopC.g:198:18: (n1= NAME )
                     // nopC.g:198:19: n1= NAME
                     {
                     n1=(Token)match(input,NAME,FOLLOW_NAME_in_parameterList382); if (state.failed) return ;
 
                     }
 
 
                     if ( state.backtracking==0 ) {parent.addParam((n1!=null?n1.getText():null));}
 
                     // nopC.g:198:58: ( ',' typeSpecifier (n2= NAME ) )*
                     loop10:
                     do {
                         int alt10=2;
                         int LA10_0 = input.LA(1);
 
                         if ( (LA10_0==23) ) {
                             alt10=1;
                         }
 
 
                         switch (alt10) {
                     	case 1 :
                     	    // nopC.g:198:59: ',' typeSpecifier (n2= NAME )
                     	    {
                     	    match(input,23,FOLLOW_23_in_parameterList387); if (state.failed) return ;
 
                     	    pushFollow(FOLLOW_typeSpecifier_in_parameterList389);
                     	    typeSpecifier();
 
                     	    state._fsp--;
                     	    if (state.failed) return ;
 
                     	    // nopC.g:198:77: (n2= NAME )
                     	    // nopC.g:198:78: n2= NAME
                     	    {
                     	    n2=(Token)match(input,NAME,FOLLOW_NAME_in_parameterList396); if (state.failed) return ;
 
                     	    }
 
 
                     	    if ( state.backtracking==0 ) {parent.addParam((n2!=null?n2.getText():null));}
 
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
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 8, parameterList_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "parameterList"
 
 
 
     // $ANTLR start "codeBlock"
     // nopC.g:201:1: codeBlock[GenericStatement parent] : ( '{' ( statement[parent] )* '}' | statement[parent] );
     public final void codeBlock(GenericStatement parent) throws RecognitionException {
         int codeBlock_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return ; }
 
             // nopC.g:202:2: ( '{' ( statement[parent] )* '}' | statement[parent] )
             int alt13=2;
             int LA13_0 = input.LA(1);
 
             if ( (LA13_0==55) ) {
                 alt13=1;
             }
             else if ( ((LA13_0 >= NAME && LA13_0 <= WERT)||LA13_0==16||LA13_0==21||LA13_0==25||LA13_0==30||(LA13_0 >= 44 && LA13_0 <= 46)||(LA13_0 >= 48 && LA13_0 <= 54)) ) {
                 alt13=2;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 13, 0, input);
 
                 throw nvae;
 
             }
             switch (alt13) {
                 case 1 :
                     // nopC.g:203:2: '{' ( statement[parent] )* '}'
                     {
                     match(input,55,FOLLOW_55_in_codeBlock417); if (state.failed) return ;
 
                     // nopC.g:203:5: ( statement[parent] )*
                     loop12:
                     do {
                         int alt12=2;
                         int LA12_0 = input.LA(1);
 
                         if ( ((LA12_0 >= NAME && LA12_0 <= WERT)||LA12_0==16||LA12_0==21||LA12_0==25||LA12_0==30||(LA12_0 >= 44 && LA12_0 <= 46)||(LA12_0 >= 48 && LA12_0 <= 54)) ) {
                             alt12=1;
                         }
 
 
                         switch (alt12) {
                     	case 1 :
                     	    // nopC.g:203:5: statement[parent]
                     	    {
                     	    pushFollow(FOLLOW_statement_in_codeBlock418);
                     	    statement(parent);
 
                     	    state._fsp--;
                     	    if (state.failed) return ;
 
                     	    }
                     	    break;
 
                     	default :
                     	    break loop12;
                         }
                     } while (true);
 
 
                     match(input,59,FOLLOW_59_in_codeBlock421); if (state.failed) return ;
 
                     }
                     break;
                 case 2 :
                     // nopC.g:204:4: statement[parent]
                     {
                     pushFollow(FOLLOW_statement_in_codeBlock426);
                     statement(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
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
             if ( state.backtracking>0 ) { memoize(input, 9, codeBlock_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "codeBlock"
 
 
 
     // $ANTLR start "statement"
     // nopC.g:208:1: statement[GenericStatement parent] : ( ( functionCall[null] ';' ) | ( assignment[null] ';' ) | variableDeclaration[parent] | selection_statement[parent] | iteration_statement[parent] | jump_statement[parent] | expression_statement[parent] );
     public final void statement(GenericStatement parent) throws RecognitionException {
         int statement_StartIndex = input.index();
 
 
         	hackStore1 = parent; // We cannot pass parent to functionCall or assignment directly, as there is a synpred rule involved during backtracking which does not take parameters. Therefore we store it in a global variable to access the variable through this later
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return ; }
 
             // nopC.g:212:2: ( ( functionCall[null] ';' ) | ( assignment[null] ';' ) | variableDeclaration[parent] | selection_statement[parent] | iteration_statement[parent] | jump_statement[parent] | expression_statement[parent] )
             int alt14=7;
             switch ( input.LA(1) ) {
             case NAME:
                 {
                 switch ( input.LA(2) ) {
                 case 16:
                     {
                     int LA14_15 = input.LA(3);
 
                     if ( (synpred17_nopC()) ) {
                         alt14=1;
                     }
                     else if ( (true) ) {
                         alt14=7;
                     }
                     else {
                         if (state.backtracking>0) {state.failed=true; return ;}
                         NoViableAltException nvae =
                             new NoViableAltException("", 14, 15, input);
 
                         throw nvae;
 
                     }
                     }
                     break;
                 case 12:
                 case 15:
                 case 19:
                 case 22:
                 case 26:
                 case 28:
                 case 33:
                 case 35:
                 case 40:
                 case 43:
                 case 57:
                     {
                     alt14=2;
                     }
                     break;
                 case 10:
                 case 11:
                 case 13:
                 case 14:
                 case 18:
                 case 20:
                 case 21:
                 case 24:
                 case 25:
                 case 27:
                 case 30:
                 case 31:
                 case 32:
                 case 34:
                 case 36:
                 case 37:
                 case 38:
                 case 39:
                 case 41:
                 case 42:
                 case 56:
                 case 58:
                     {
                     alt14=7;
                     }
                     break;
                 default:
                     if (state.backtracking>0) {state.failed=true; return ;}
                     NoViableAltException nvae =
                         new NoViableAltException("", 14, 1, input);
 
                     throw nvae;
 
                 }
 
                 }
                 break;
             case 44:
             case 51:
             case 53:
                 {
                 alt14=3;
                 }
                 break;
             case 50:
                 {
                 alt14=4;
                 }
                 break;
             case 48:
             case 54:
                 {
                 alt14=5;
                 }
                 break;
             case 45:
             case 46:
             case 49:
             case 52:
                 {
                 alt14=6;
                 }
                 break;
             case WERT:
             case 16:
             case 21:
             case 25:
             case 30:
                 {
                 alt14=7;
                 }
                 break;
             default:
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 14, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt14) {
                 case 1 :
                     // nopC.g:213:5: ( functionCall[null] ';' )
                     {
                     // nopC.g:213:5: ( functionCall[null] ';' )
                     // nopC.g:213:7: functionCall[null] ';'
                     {
                     pushFollow(FOLLOW_functionCall_in_statement453);
                     functionCall(null);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,30,FOLLOW_30_in_statement456); if (state.failed) return ;
 
                     }
 
 
                     }
                     break;
                 case 2 :
                     // nopC.g:214:5: ( assignment[null] ';' )
                     {
                     // nopC.g:214:5: ( assignment[null] ';' )
                     // nopC.g:214:7: assignment[null] ';'
                     {
                     pushFollow(FOLLOW_assignment_in_statement465);
                     assignment(null);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,30,FOLLOW_30_in_statement468); if (state.failed) return ;
 
                     }
 
 
                     }
                     break;
                 case 3 :
                     // nopC.g:215:5: variableDeclaration[parent]
                     {
                     pushFollow(FOLLOW_variableDeclaration_in_statement476);
                     variableDeclaration(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 4 :
                     // nopC.g:216:5: selection_statement[parent]
                     {
                     pushFollow(FOLLOW_selection_statement_in_statement483);
                     selection_statement(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 5 :
                     // nopC.g:217:6: iteration_statement[parent]
                     {
                     pushFollow(FOLLOW_iteration_statement_in_statement491);
                     iteration_statement(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 6 :
                     // nopC.g:218:6: jump_statement[parent]
                     {
                     pushFollow(FOLLOW_jump_statement_in_statement499);
                     jump_statement(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 7 :
                     // nopC.g:219:6: expression_statement[parent]
                     {
                     pushFollow(FOLLOW_expression_statement_in_statement507);
                     expression_statement(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
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
             if ( state.backtracking>0 ) { memoize(input, 10, statement_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "statement"
 
 
 
     // $ANTLR start "selection_statement"
     // nopC.g:223:1: selection_statement[GenericStatement parent] : 'if' '(' expression[parent] ')' codeBlock[selection_statement_if] ( options {k=1; backtrack=false; } : 'else' codeBlock[selection_statement_else] )? ;
     public final void selection_statement(GenericStatement parent) throws RecognitionException {
         int selection_statement_StartIndex = input.index();
 
 
         ScopedStatement selection_statement_if = new ScopedStatement(parent.getScope(), functionTable, varTable);
         ScopedStatement selection_statement_else = new ScopedStatement(parent.getScope(), functionTable, varTable);
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return ; }
 
             // nopC.g:228:2: ( 'if' '(' expression[parent] ')' codeBlock[selection_statement_if] ( options {k=1; backtrack=false; } : 'else' codeBlock[selection_statement_else] )? )
             // nopC.g:228:4: 'if' '(' expression[parent] ')' codeBlock[selection_statement_if] ( options {k=1; backtrack=false; } : 'else' codeBlock[selection_statement_else] )?
             {
             match(input,50,FOLLOW_50_in_selection_statement528); if (state.failed) return ;
 
             match(input,16,FOLLOW_16_in_selection_statement530); if (state.failed) return ;
 
             pushFollow(FOLLOW_expression_in_selection_statement532);
             expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             match(input,17,FOLLOW_17_in_selection_statement535); if (state.failed) return ;
 
             pushFollow(FOLLOW_codeBlock_in_selection_statement537);
             codeBlock(selection_statement_if);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:228:70: ( options {k=1; backtrack=false; } : 'else' codeBlock[selection_statement_else] )?
             int alt15=2;
             int LA15_0 = input.LA(1);
 
             if ( (LA15_0==47) ) {
                 int LA15_1 = input.LA(2);
 
                 if ( (true) ) {
                     alt15=1;
                 }
             }
             switch (alt15) {
                 case 1 :
                     // nopC.g:228:103: 'else' codeBlock[selection_statement_else]
                     {
                     match(input,47,FOLLOW_47_in_selection_statement553); if (state.failed) return ;
 
                     pushFollow(FOLLOW_codeBlock_in_selection_statement555);
                     codeBlock(selection_statement_else);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
 
             }
 
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 11, selection_statement_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "selection_statement"
 
 
 
     // $ANTLR start "iteration_statement"
     // nopC.g:231:1: iteration_statement[GenericStatement parent] : ( 'while' '(' expression[parent] ')' codeBlock[iteration_statement] | 'for' '(' expression_statement[parent] expression_statement[parent] ( expression[parent] )? ')' codeBlock[iteration_statement] );
     public final void iteration_statement(GenericStatement parent) throws RecognitionException {
         int iteration_statement_StartIndex = input.index();
 
 
         ScopedStatement iteration_statement = new ScopedStatement(parent.getScope(), functionTable, varTable);
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return ; }
 
             // nopC.g:235:2: ( 'while' '(' expression[parent] ')' codeBlock[iteration_statement] | 'for' '(' expression_statement[parent] expression_statement[parent] ( expression[parent] )? ')' codeBlock[iteration_statement] )
             int alt17=2;
             int LA17_0 = input.LA(1);
 
             if ( (LA17_0==54) ) {
                 alt17=1;
             }
             else if ( (LA17_0==48) ) {
                 alt17=2;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 17, 0, input);
 
                 throw nvae;
 
             }
             switch (alt17) {
                 case 1 :
                     // nopC.g:235:4: 'while' '(' expression[parent] ')' codeBlock[iteration_statement]
                     {
                     match(input,54,FOLLOW_54_in_iteration_statement575); if (state.failed) return ;
 
                     match(input,16,FOLLOW_16_in_iteration_statement577); if (state.failed) return ;
 
                     pushFollow(FOLLOW_expression_in_iteration_statement579);
                     expression(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,17,FOLLOW_17_in_iteration_statement582); if (state.failed) return ;
 
                     pushFollow(FOLLOW_codeBlock_in_iteration_statement584);
                     codeBlock(iteration_statement);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 2 :
                     // nopC.g:236:4: 'for' '(' expression_statement[parent] expression_statement[parent] ( expression[parent] )? ')' codeBlock[iteration_statement]
                     {
                     match(input,48,FOLLOW_48_in_iteration_statement590); if (state.failed) return ;
 
                     match(input,16,FOLLOW_16_in_iteration_statement592); if (state.failed) return ;
 
                     pushFollow(FOLLOW_expression_statement_in_iteration_statement594);
                     expression_statement(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     pushFollow(FOLLOW_expression_statement_in_iteration_statement597);
                     expression_statement(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     // nopC.g:236:72: ( expression[parent] )?
                     int alt16=2;
                     int LA16_0 = input.LA(1);
 
                     if ( ((LA16_0 >= NAME && LA16_0 <= WERT)||LA16_0==16||LA16_0==21||LA16_0==25) ) {
                         alt16=1;
                     }
                     switch (alt16) {
                         case 1 :
                             // nopC.g:236:72: expression[parent]
                             {
                             pushFollow(FOLLOW_expression_in_iteration_statement600);
                             expression(parent);
 
                             state._fsp--;
                             if (state.failed) return ;
 
                             }
                             break;
 
                     }
 
 
                     match(input,17,FOLLOW_17_in_iteration_statement604); if (state.failed) return ;
 
                     pushFollow(FOLLOW_codeBlock_in_iteration_statement606);
                     codeBlock(iteration_statement);
 
                     state._fsp--;
                     if (state.failed) return ;
 
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
             if ( state.backtracking>0 ) { memoize(input, 12, iteration_statement_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "iteration_statement"
 
 
 
     // $ANTLR start "expression_statement"
     // nopC.g:239:1: expression_statement[GenericStatement parent] : ( ';' | expression[parent] ';' );
     public final void expression_statement(GenericStatement parent) throws RecognitionException {
         int expression_statement_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return ; }
 
             // nopC.g:240:2: ( ';' | expression[parent] ';' )
             int alt18=2;
             int LA18_0 = input.LA(1);
 
             if ( (LA18_0==30) ) {
                 alt18=1;
             }
             else if ( ((LA18_0 >= NAME && LA18_0 <= WERT)||LA18_0==16||LA18_0==21||LA18_0==25) ) {
                 alt18=2;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 18, 0, input);
 
                 throw nvae;
 
             }
             switch (alt18) {
                 case 1 :
                     // nopC.g:240:4: ';'
                     {
                     match(input,30,FOLLOW_30_in_expression_statement621); if (state.failed) return ;
 
                     }
                     break;
                 case 2 :
                     // nopC.g:241:4: expression[parent] ';'
                     {
                     pushFollow(FOLLOW_expression_in_expression_statement626);
                     expression(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,30,FOLLOW_30_in_expression_statement629); if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {System.out.println("DORT");	}
 
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
             if ( state.backtracking>0 ) { memoize(input, 13, expression_statement_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "expression_statement"
 
 
 
     // $ANTLR start "jump_statement"
     // nopC.g:245:1: jump_statement[GenericStatement parent] : ( 'goto' NAME ';' | 'continue' ';' | 'break' ';' | 'return' ';' | 'return' expression[parent] ';' );
     public final void jump_statement(GenericStatement parent) throws RecognitionException {
         int jump_statement_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return ; }
 
             // nopC.g:246:2: ( 'goto' NAME ';' | 'continue' ';' | 'break' ';' | 'return' ';' | 'return' expression[parent] ';' )
             int alt19=5;
             switch ( input.LA(1) ) {
             case 49:
                 {
                 alt19=1;
                 }
                 break;
             case 46:
                 {
                 alt19=2;
                 }
                 break;
             case 45:
                 {
                 alt19=3;
                 }
                 break;
             case 52:
                 {
                 int LA19_4 = input.LA(2);
 
                 if ( (LA19_4==30) ) {
                     alt19=4;
                 }
                 else if ( ((LA19_4 >= NAME && LA19_4 <= WERT)||LA19_4==16||LA19_4==21||LA19_4==25) ) {
                     alt19=5;
                 }
                 else {
                     if (state.backtracking>0) {state.failed=true; return ;}
                     NoViableAltException nvae =
                         new NoViableAltException("", 19, 4, input);
 
                     throw nvae;
 
                 }
                 }
                 break;
             default:
                 if (state.backtracking>0) {state.failed=true; return ;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 19, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt19) {
                 case 1 :
                     // nopC.g:246:4: 'goto' NAME ';'
                     {
                     match(input,49,FOLLOW_49_in_jump_statement646); if (state.failed) return ;
 
                     match(input,NAME,FOLLOW_NAME_in_jump_statement648); if (state.failed) return ;
 
                     match(input,30,FOLLOW_30_in_jump_statement650); if (state.failed) return ;
 
                     }
                     break;
                 case 2 :
                     // nopC.g:247:4: 'continue' ';'
                     {
                     match(input,46,FOLLOW_46_in_jump_statement655); if (state.failed) return ;
 
                     match(input,30,FOLLOW_30_in_jump_statement657); if (state.failed) return ;
 
                     }
                     break;
                 case 3 :
                     // nopC.g:248:4: 'break' ';'
                     {
                     match(input,45,FOLLOW_45_in_jump_statement662); if (state.failed) return ;
 
                     match(input,30,FOLLOW_30_in_jump_statement664); if (state.failed) return ;
 
                     }
                     break;
                 case 4 :
                     // nopC.g:249:4: 'return' ';'
                     {
                     match(input,52,FOLLOW_52_in_jump_statement669); if (state.failed) return ;
 
                     match(input,30,FOLLOW_30_in_jump_statement671); if (state.failed) return ;
 
                     }
                     break;
                 case 5 :
                     // nopC.g:250:4: 'return' expression[parent] ';'
                     {
                     match(input,52,FOLLOW_52_in_jump_statement676); if (state.failed) return ;
 
                     pushFollow(FOLLOW_expression_in_jump_statement678);
                     expression(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,30,FOLLOW_30_in_jump_statement681); if (state.failed) return ;
 
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
             if ( state.backtracking>0 ) { memoize(input, 14, jump_statement_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "jump_statement"
 
 
 
     // $ANTLR start "functionCall"
     // nopC.g:253:1: functionCall[GenericStatement parent] : NAME '(' functionCallArgumentList[p, fun] ')' ;
     public final void functionCall(GenericStatement parent) throws RecognitionException {
         int functionCall_StartIndex = input.index();
 
         Token NAME3=null;
 
 
         	GenericStatement p = parent; 
         	
         	if (p == null) {
         		 p = hackStore1;
         	}
         	
         	FunctionDefinition fun = null;
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return ; }
 
             // nopC.g:263:2: ( NAME '(' functionCallArgumentList[p, fun] ')' )
             // nopC.g:264:2: NAME '(' functionCallArgumentList[p, fun] ')'
             {
             NAME3=(Token)match(input,NAME,FOLLOW_NAME_in_functionCall701); if (state.failed) return ;
 
             match(input,16,FOLLOW_16_in_functionCall703); if (state.failed) return ;
 
             if ( state.backtracking==0 ) {fun = functionTable.get((NAME3!=null?NAME3.getText():null));}
 
             pushFollow(FOLLOW_functionCallArgumentList_in_functionCall707);
             functionCallArgumentList(p, fun);
 
             state._fsp--;
             if (state.failed) return ;
 
             match(input,17,FOLLOW_17_in_functionCall710); if (state.failed) return ;
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 15, functionCall_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "functionCall"
 
 
 
     // $ANTLR start "functionCallArgumentList"
     // nopC.g:267:1: functionCallArgumentList[GenericStatement parent, FunctionDefinition fun] : ( NAME | functionCall[parent] | WERT )? ( ',' ( NAME | functionCall[parent] | WERT ) )* ;
     public final void functionCallArgumentList(GenericStatement parent, FunctionDefinition fun) throws RecognitionException {
         int functionCallArgumentList_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return ; }
 
             // nopC.g:268:2: ( ( NAME | functionCall[parent] | WERT )? ( ',' ( NAME | functionCall[parent] | WERT ) )* )
             // nopC.g:269:5: ( NAME | functionCall[parent] | WERT )? ( ',' ( NAME | functionCall[parent] | WERT ) )*
             {
             // nopC.g:269:5: ( NAME | functionCall[parent] | WERT )?
             int alt20=4;
             int LA20_0 = input.LA(1);
 
             if ( (LA20_0==NAME) ) {
                 int LA20_1 = input.LA(2);
 
                 if ( (LA20_1==16) ) {
                     alt20=2;
                 }
                 else if ( (LA20_1==17||LA20_1==23) ) {
                     alt20=1;
                 }
             }
             else if ( (LA20_0==WERT) ) {
                 alt20=3;
             }
             switch (alt20) {
                 case 1 :
                     // nopC.g:269:6: NAME
                     {
                     match(input,NAME,FOLLOW_NAME_in_functionCallArgumentList729); if (state.failed) return ;
 
                     }
                     break;
                 case 2 :
                     // nopC.g:269:13: functionCall[parent]
                     {
                     pushFollow(FOLLOW_functionCall_in_functionCallArgumentList733);
                     functionCall(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 3 :
                     // nopC.g:269:36: WERT
                     {
                     match(input,WERT,FOLLOW_WERT_in_functionCallArgumentList738); if (state.failed) return ;
 
                     }
                     break;
 
             }
 
 
             // nopC.g:269:43: ( ',' ( NAME | functionCall[parent] | WERT ) )*
             loop22:
             do {
                 int alt22=2;
                 int LA22_0 = input.LA(1);
 
                 if ( (LA22_0==23) ) {
                     alt22=1;
                 }
 
 
                 switch (alt22) {
             	case 1 :
             	    // nopC.g:269:44: ',' ( NAME | functionCall[parent] | WERT )
             	    {
             	    match(input,23,FOLLOW_23_in_functionCallArgumentList743); if (state.failed) return ;
 
             	    // nopC.g:269:48: ( NAME | functionCall[parent] | WERT )
             	    int alt21=3;
             	    int LA21_0 = input.LA(1);
 
             	    if ( (LA21_0==NAME) ) {
             	        int LA21_1 = input.LA(2);
 
             	        if ( (LA21_1==16) ) {
             	            alt21=2;
             	        }
             	        else if ( (LA21_1==17||LA21_1==23) ) {
             	            alt21=1;
             	        }
             	        else {
             	            if (state.backtracking>0) {state.failed=true; return ;}
             	            NoViableAltException nvae =
             	                new NoViableAltException("", 21, 1, input);
 
             	            throw nvae;
 
             	        }
             	    }
             	    else if ( (LA21_0==WERT) ) {
             	        alt21=3;
             	    }
             	    else {
             	        if (state.backtracking>0) {state.failed=true; return ;}
             	        NoViableAltException nvae =
             	            new NoViableAltException("", 21, 0, input);
 
             	        throw nvae;
 
             	    }
             	    switch (alt21) {
             	        case 1 :
             	            // nopC.g:269:49: NAME
             	            {
             	            match(input,NAME,FOLLOW_NAME_in_functionCallArgumentList746); if (state.failed) return ;
 
             	            }
             	            break;
             	        case 2 :
             	            // nopC.g:269:56: functionCall[parent]
             	            {
             	            pushFollow(FOLLOW_functionCall_in_functionCallArgumentList750);
             	            functionCall(parent);
 
             	            state._fsp--;
             	            if (state.failed) return ;
 
             	            }
             	            break;
             	        case 3 :
             	            // nopC.g:269:79: WERT
             	            {
             	            match(input,WERT,FOLLOW_WERT_in_functionCallArgumentList755); if (state.failed) return ;
 
             	            }
             	            break;
 
             	    }
 
 
             	    }
             	    break;
 
             	default :
             	    break loop22;
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
             if ( state.backtracking>0 ) { memoize(input, 16, functionCallArgumentList_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "functionCallArgumentList"
 
 
 
     // $ANTLR start "assignment"
     // nopC.g:272:1: assignment[GenericStatement parent] : NAME assignmentOperator expression[p] ;
     public final void assignment(GenericStatement parent) throws RecognitionException {
         int assignment_StartIndex = input.index();
 
         Token NAME4=null;
 
 
         	GenericStatement p = parent; 
         	
         	if (p == null) {
         		 p = hackStore1;
         	}
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return ; }
 
             // nopC.g:280:2: ( NAME assignmentOperator expression[p] )
             // nopC.g:281:3: NAME assignmentOperator expression[p]
             {
             NAME4=(Token)match(input,NAME,FOLLOW_NAME_in_assignment777); if (state.failed) return ;
 
             if ( state.backtracking==0 ) {System.out.println("HIER: " + (NAME4!=null?NAME4.getText():null));}
 
             pushFollow(FOLLOW_assignmentOperator_in_assignment781);
             assignmentOperator();
 
             state._fsp--;
             if (state.failed) return ;
 
             pushFollow(FOLLOW_expression_in_assignment783);
             expression(p);
 
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
             if ( state.backtracking>0 ) { memoize(input, 17, assignment_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "assignment"
 
 
 
     // $ANTLR start "assignmentOperator"
     // nopC.g:287:1: assignmentOperator : ( '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|=' );
     public final void assignmentOperator() throws RecognitionException {
         int assignmentOperator_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return ; }
 
             // nopC.g:288:2: ( '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|=' )
             // nopC.g:
             {
             if ( input.LA(1)==12||input.LA(1)==15||input.LA(1)==19||input.LA(1)==22||input.LA(1)==26||input.LA(1)==28||input.LA(1)==33||input.LA(1)==35||input.LA(1)==40||input.LA(1)==43||input.LA(1)==57 ) {
                 input.consume();
                 state.errorRecovery=false;
                 state.failed=false;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 throw mse;
             }
 
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 18, assignmentOperator_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "assignmentOperator"
 
 
 
     // $ANTLR start "expression"
     // nopC.g:301:2: expression[GenericStatement parent] : logical_or_expression[parent] ( '?' expression[parent] ':' expression[parent] )? ;
     public final void expression(GenericStatement parent) throws RecognitionException {
         int expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return ; }
 
             // nopC.g:302:3: ( logical_or_expression[parent] ( '?' expression[parent] ':' expression[parent] )? )
             // nopC.g:302:5: logical_or_expression[parent] ( '?' expression[parent] ':' expression[parent] )?
             {
             pushFollow(FOLLOW_logical_or_expression_in_expression864);
             logical_or_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:303:4: ( '?' expression[parent] ':' expression[parent] )?
             int alt23=2;
             int LA23_0 = input.LA(1);
 
             if ( (LA23_0==41) ) {
                 alt23=1;
             }
             switch (alt23) {
                 case 1 :
                     // nopC.g:304:5: '?' expression[parent] ':' expression[parent]
                     {
                     match(input,41,FOLLOW_41_in_expression877); if (state.failed) return ;
 
                     pushFollow(FOLLOW_expression_in_expression884);
                     expression(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     match(input,29,FOLLOW_29_in_expression892); if (state.failed) return ;
 
                     pushFollow(FOLLOW_expression_in_expression899);
                     expression(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
 
             }
 
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 19, expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "expression"
 
 
 
     // $ANTLR start "logical_or_expression"
     // nopC.g:311:2: logical_or_expression[GenericStatement parent] : logical_and_expression[parent] ( '||' logical_and_expression[parent] )* ;
     public final void logical_or_expression(GenericStatement parent) throws RecognitionException {
         int logical_or_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return ; }
 
             // nopC.g:312:3: ( logical_and_expression[parent] ( '||' logical_and_expression[parent] )* )
             // nopC.g:312:5: logical_and_expression[parent] ( '||' logical_and_expression[parent] )*
             {
             pushFollow(FOLLOW_logical_and_expression_in_logical_or_expression921);
             logical_and_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:313:4: ( '||' logical_and_expression[parent] )*
             loop24:
             do {
                 int alt24=2;
                 int LA24_0 = input.LA(1);
 
                 if ( (LA24_0==58) ) {
                     alt24=1;
                 }
 
 
                 switch (alt24) {
             	case 1 :
             	    // nopC.g:314:5: '||' logical_and_expression[parent]
             	    {
             	    match(input,58,FOLLOW_58_in_logical_or_expression934); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_logical_and_expression_in_logical_or_expression938);
             	    logical_and_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \n OR X, Y\nIFG X, 0\n   SET X, 1\n");}
 
             	    }
             	    break;
 
             	default :
             	    break loop24;
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
             if ( state.backtracking>0 ) { memoize(input, 20, logical_or_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "logical_or_expression"
 
 
 
     // $ANTLR start "logical_and_expression"
     // nopC.g:318:2: logical_and_expression[GenericStatement parent] : inclusive_or_expression[parent] ( '&&' inclusive_or_expression[parent] )* ;
     public final void logical_and_expression(GenericStatement parent) throws RecognitionException {
         int logical_and_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return ; }
 
             // nopC.g:319:3: ( inclusive_or_expression[parent] ( '&&' inclusive_or_expression[parent] )* )
             // nopC.g:319:5: inclusive_or_expression[parent] ( '&&' inclusive_or_expression[parent] )*
             {
             pushFollow(FOLLOW_inclusive_or_expression_in_logical_and_expression962);
             inclusive_or_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:320:4: ( '&&' inclusive_or_expression[parent] )*
             loop25:
             do {
                 int alt25=2;
                 int LA25_0 = input.LA(1);
 
                 if ( (LA25_0==13) ) {
                     alt25=1;
                 }
 
 
                 switch (alt25) {
             	case 1 :
             	    // nopC.g:321:5: '&&' inclusive_or_expression[parent]
             	    {
             	    match(input,13,FOLLOW_13_in_logical_and_expression975); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_inclusive_or_expression_in_logical_and_expression979);
             	    inclusive_or_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nIFG Y, 0\n   SET Y, FFFF\n AND X, Y\nIFG X, 0\n   SET X, 1\n");}
 
             	    }
             	    break;
 
             	default :
             	    break loop25;
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
             if ( state.backtracking>0 ) { memoize(input, 21, logical_and_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "logical_and_expression"
 
 
 
     // $ANTLR start "inclusive_or_expression"
     // nopC.g:325:2: inclusive_or_expression[GenericStatement parent] : exclusive_or_expression[parent] ( '|' exclusive_or_expression[parent] )* ;
     public final void inclusive_or_expression(GenericStatement parent) throws RecognitionException {
         int inclusive_or_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return ; }
 
             // nopC.g:326:3: ( exclusive_or_expression[parent] ( '|' exclusive_or_expression[parent] )* )
             // nopC.g:326:5: exclusive_or_expression[parent] ( '|' exclusive_or_expression[parent] )*
             {
             pushFollow(FOLLOW_exclusive_or_expression_in_inclusive_or_expression1003);
             exclusive_or_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:327:4: ( '|' exclusive_or_expression[parent] )*
             loop26:
             do {
                 int alt26=2;
                 int LA26_0 = input.LA(1);
 
                 if ( (LA26_0==56) ) {
                     alt26=1;
                 }
 
 
                 switch (alt26) {
             	case 1 :
             	    // nopC.g:328:5: '|' exclusive_or_expression[parent]
             	    {
             	    match(input,56,FOLLOW_56_in_inclusive_or_expression1016); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_exclusive_or_expression_in_inclusive_or_expression1020);
             	    exclusive_or_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nOR X, Y\n");}
 
             	    }
             	    break;
 
             	default :
             	    break loop26;
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
             if ( state.backtracking>0 ) { memoize(input, 22, inclusive_or_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "inclusive_or_expression"
 
 
 
     // $ANTLR start "exclusive_or_expression"
     // nopC.g:332:2: exclusive_or_expression[GenericStatement parent] : and_expression[parent] ( '^' and_expression[parent] )* ;
     public final void exclusive_or_expression(GenericStatement parent) throws RecognitionException {
         int exclusive_or_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return ; }
 
             // nopC.g:333:3: ( and_expression[parent] ( '^' and_expression[parent] )* )
             // nopC.g:333:5: and_expression[parent] ( '^' and_expression[parent] )*
             {
             pushFollow(FOLLOW_and_expression_in_exclusive_or_expression1044);
             and_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:334:3: ( '^' and_expression[parent] )*
             loop27:
             do {
                 int alt27=2;
                 int LA27_0 = input.LA(1);
 
                 if ( (LA27_0==42) ) {
                     alt27=1;
                 }
 
 
                 switch (alt27) {
             	case 1 :
             	    // nopC.g:335:4: '^' and_expression[parent]
             	    {
             	    match(input,42,FOLLOW_42_in_exclusive_or_expression1055); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_and_expression_in_exclusive_or_expression1059);
             	    and_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nXOR X, Y\n");}
 
             	    }
             	    break;
 
             	default :
             	    break loop27;
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
             if ( state.backtracking>0 ) { memoize(input, 23, exclusive_or_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "exclusive_or_expression"
 
 
 
     // $ANTLR start "and_expression"
     // nopC.g:339:2: and_expression[GenericStatement parent] : equality_expression[parent] ( '&' equality_expression[parent] )* ;
     public final void and_expression(GenericStatement parent) throws RecognitionException {
         int and_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return ; }
 
             // nopC.g:340:3: ( equality_expression[parent] ( '&' equality_expression[parent] )* )
             // nopC.g:340:5: equality_expression[parent] ( '&' equality_expression[parent] )*
             {
             pushFollow(FOLLOW_equality_expression_in_and_expression1082);
             equality_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:341:3: ( '&' equality_expression[parent] )*
             loop28:
             do {
                 int alt28=2;
                 int LA28_0 = input.LA(1);
 
                 if ( (LA28_0==14) ) {
                     alt28=1;
                 }
 
 
                 switch (alt28) {
             	case 1 :
             	    // nopC.g:342:4: '&' equality_expression[parent]
             	    {
             	    match(input,14,FOLLOW_14_in_and_expression1093); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_equality_expression_in_and_expression1097);
             	    equality_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nAND X, Y\n");}
 
             	    }
             	    break;
 
             	default :
             	    break loop28;
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
             if ( state.backtracking>0 ) { memoize(input, 24, and_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "and_expression"
 
 
 
     // $ANTLR start "equality_expression"
     // nopC.g:346:2: equality_expression[GenericStatement parent] : relational_expression[parent] ( '==' relational_expression[parent] | '!=' relational_expression[parent] )* ;
     public final void equality_expression(GenericStatement parent) throws RecognitionException {
         int equality_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return ; }
 
             // nopC.g:347:3: ( relational_expression[parent] ( '==' relational_expression[parent] | '!=' relational_expression[parent] )* )
             // nopC.g:347:5: relational_expression[parent] ( '==' relational_expression[parent] | '!=' relational_expression[parent] )*
             {
             pushFollow(FOLLOW_relational_expression_in_equality_expression1123);
             relational_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:348:4: ( '==' relational_expression[parent] | '!=' relational_expression[parent] )*
             loop29:
             do {
                 int alt29=3;
                 int LA29_0 = input.LA(1);
 
                 if ( (LA29_0==36) ) {
                     alt29=1;
                 }
                 else if ( (LA29_0==10) ) {
                     alt29=2;
                 }
 
 
                 switch (alt29) {
             	case 1 :
             	    // nopC.g:349:5: '==' relational_expression[parent]
             	    {
             	    match(input,36,FOLLOW_36_in_equality_expression1136); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_relational_expression_in_equality_expression1140);
             	    relational_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET B, 0\nSET Y, POP \nIFE	 X, Y\n   SET B, 1\nSET X, B\n");}
 
             	    }
             	    break;
             	case 2 :
             	    // nopC.g:350:6: '!=' relational_expression[parent]
             	    {
             	    match(input,10,FOLLOW_10_in_equality_expression1150); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_relational_expression_in_equality_expression1154);
             	    relational_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET B, 0\nSET Y, POP \nIFN  X, Y\n   SET B, 1\nSET X, B\n");}
 
             	    }
             	    break;
 
             	default :
             	    break loop29;
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
             if ( state.backtracking>0 ) { memoize(input, 25, equality_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "equality_expression"
 
 
 
     // $ANTLR start "relational_expression"
     // nopC.g:355:2: relational_expression[GenericStatement parent] : shift_expression[parent] ( '<' shift_expression[parent] | '>' shift_expression[parent] | '<=' shift_expression[parent] | '>=' shift_expression[parent] )* ;
     public final void relational_expression(GenericStatement parent) throws RecognitionException {
         int relational_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return ; }
 
             // nopC.g:356:3: ( shift_expression[parent] ( '<' shift_expression[parent] | '>' shift_expression[parent] | '<=' shift_expression[parent] | '>=' shift_expression[parent] )* )
             // nopC.g:356:5: shift_expression[parent] ( '<' shift_expression[parent] | '>' shift_expression[parent] | '<=' shift_expression[parent] | '>=' shift_expression[parent] )*
             {
             pushFollow(FOLLOW_shift_expression_in_relational_expression1183);
             shift_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:357:4: ( '<' shift_expression[parent] | '>' shift_expression[parent] | '<=' shift_expression[parent] | '>=' shift_expression[parent] )*
             loop30:
             do {
                 int alt30=5;
                 switch ( input.LA(1) ) {
                 case 31:
                     {
                     alt30=1;
                     }
                     break;
                 case 37:
                     {
                     alt30=2;
                     }
                     break;
                 case 34:
                     {
                     alt30=3;
                     }
                     break;
                 case 38:
                     {
                     alt30=4;
                     }
                     break;
 
                 }
 
                 switch (alt30) {
             	case 1 :
             	    // nopC.g:358:7: '<' shift_expression[parent]
             	    {
             	    match(input,31,FOLLOW_31_in_relational_expression1198); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_shift_expression_in_relational_expression1202);
             	    shift_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET B, 0\nSET Y, POP \nIFG X, Y\n   SET B, 1\nSET X, B\n");}
 
             	    }
             	    break;
             	case 2 :
             	    // nopC.g:359:7: '>' shift_expression[parent]
             	    {
             	    match(input,37,FOLLOW_37_in_relational_expression1215); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_shift_expression_in_relational_expression1219);
             	    shift_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET B, 0\nSET Y, POP \nIFG Y, X\n   SET B, 1\nSET X, B\n");}
 
             	    }
             	    break;
             	case 3 :
             	    // nopC.g:360:7: '<=' shift_expression[parent]
             	    {
             	    match(input,34,FOLLOW_34_in_relational_expression1232); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_shift_expression_in_relational_expression1236);
             	    shift_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET B, 0\nSET Y, POP \nIFG X, Y\n   SET B, 1\nIFE X, Y\n    SET B, 1\nSET X, B\n" );}
 
             	    }
             	    break;
             	case 4 :
             	    // nopC.g:361:7: '>=' shift_expression[parent]
             	    {
             	    match(input,38,FOLLOW_38_in_relational_expression1249); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_shift_expression_in_relational_expression1253);
             	    shift_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET B, 0\nSET Y, POP \nIFG Y, X\n   SET B, 1\nIFE X, Y\n    SET B, 1\nSET X, B\n" );}
 
             	    }
             	    break;
 
             	default :
             	    break loop30;
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
             if ( state.backtracking>0 ) { memoize(input, 26, relational_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "relational_expression"
 
 
 
     // $ANTLR start "shift_expression"
     // nopC.g:365:2: shift_expression[GenericStatement parent] : additive_expression[parent] ( '<<' additive_expression[parent] | '>>' additive_expression[parent] )* ;
     public final void shift_expression(GenericStatement parent) throws RecognitionException {
         int shift_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return ; }
 
             // nopC.g:366:3: ( additive_expression[parent] ( '<<' additive_expression[parent] | '>>' additive_expression[parent] )* )
             // nopC.g:366:5: additive_expression[parent] ( '<<' additive_expression[parent] | '>>' additive_expression[parent] )*
             {
             pushFollow(FOLLOW_additive_expression_in_shift_expression1283);
             additive_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             // nopC.g:367:3: ( '<<' additive_expression[parent] | '>>' additive_expression[parent] )*
             loop31:
             do {
                 int alt31=3;
                 int LA31_0 = input.LA(1);
 
                 if ( (LA31_0==32) ) {
                     alt31=1;
                 }
                 else if ( (LA31_0==39) ) {
                     alt31=2;
                 }
 
 
                 switch (alt31) {
             	case 1 :
             	    // nopC.g:368:5: '<<' additive_expression[parent]
             	    {
             	    match(input,32,FOLLOW_32_in_shift_expression1295); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_additive_expression_in_shift_expression1299);
             	    additive_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nSHL Y, X\n SET X, Y\n");}
 
             	    }
             	    break;
             	case 2 :
             	    // nopC.g:369:6: '>>' additive_expression[parent]
             	    {
             	    match(input,39,FOLLOW_39_in_shift_expression1309); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_additive_expression_in_shift_expression1313);
             	    additive_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nSHR Y, X\n SET X, Y\n");}
 
             	    }
             	    break;
 
             	default :
             	    break loop31;
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
             if ( state.backtracking>0 ) { memoize(input, 27, shift_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "shift_expression"
 
 
 
     // $ANTLR start "additive_expression"
     // nopC.g:373:2: additive_expression[GenericStatement parent] : ( multiplicative_expression[parent] ) ( '+' multiplicative_expression[parent] | '-' multiplicative_expression[parent] )* ;
     public final void additive_expression(GenericStatement parent) throws RecognitionException {
         int additive_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return ; }
 
             // nopC.g:374:3: ( ( multiplicative_expression[parent] ) ( '+' multiplicative_expression[parent] | '-' multiplicative_expression[parent] )* )
             // nopC.g:374:5: ( multiplicative_expression[parent] ) ( '+' multiplicative_expression[parent] | '-' multiplicative_expression[parent] )*
             {
             // nopC.g:374:5: ( multiplicative_expression[parent] )
             // nopC.g:374:6: multiplicative_expression[parent]
             {
             pushFollow(FOLLOW_multiplicative_expression_in_additive_expression1340);
             multiplicative_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             }
 
 
             // nopC.g:375:4: ( '+' multiplicative_expression[parent] | '-' multiplicative_expression[parent] )*
             loop32:
             do {
                 int alt32=3;
                 int LA32_0 = input.LA(1);
 
                 if ( (LA32_0==20) ) {
                     alt32=1;
                 }
                 else if ( (LA32_0==24) ) {
                     alt32=2;
                 }
 
 
                 switch (alt32) {
             	case 1 :
             	    // nopC.g:376:6: '+' multiplicative_expression[parent]
             	    {
             	    match(input,20,FOLLOW_20_in_additive_expression1355); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_multiplicative_expression_in_additive_expression1359);
             	    multiplicative_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nADD X, Y\n");}
 
             	    }
             	    break;
             	case 2 :
             	    // nopC.g:377:7: '-' multiplicative_expression[parent]
             	    {
             	    match(input,24,FOLLOW_24_in_additive_expression1371); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_multiplicative_expression_in_additive_expression1375);
             	    multiplicative_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nSUB Y, X\n SET X, Y \n");}
 
             	    }
             	    break;
 
             	default :
             	    break loop32;
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
             if ( state.backtracking>0 ) { memoize(input, 28, additive_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "additive_expression"
 
 
 
     // $ANTLR start "multiplicative_expression"
     // nopC.g:381:2: multiplicative_expression[GenericStatement parent] : ( unary_expression[parent] ) ( '*' unary_expression[parent] | '/' unary_expression[parent] | '%' unary_expression[parent] )* ;
     public final void multiplicative_expression(GenericStatement parent) throws RecognitionException {
         int multiplicative_expression_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return ; }
 
             // nopC.g:382:3: ( ( unary_expression[parent] ) ( '*' unary_expression[parent] | '/' unary_expression[parent] | '%' unary_expression[parent] )* )
             // nopC.g:382:5: ( unary_expression[parent] ) ( '*' unary_expression[parent] | '/' unary_expression[parent] | '%' unary_expression[parent] )*
             {
             // nopC.g:382:5: ( unary_expression[parent] )
             // nopC.g:382:6: unary_expression[parent]
             {
             pushFollow(FOLLOW_unary_expression_in_multiplicative_expression1401);
             unary_expression(parent);
 
             state._fsp--;
             if (state.failed) return ;
 
             }
 
 
             // nopC.g:383:5: ( '*' unary_expression[parent] | '/' unary_expression[parent] | '%' unary_expression[parent] )*
             loop33:
             do {
                 int alt33=4;
                 switch ( input.LA(1) ) {
                 case 18:
                     {
                     alt33=1;
                     }
                     break;
                 case 27:
                     {
                     alt33=2;
                     }
                     break;
                 case 11:
                     {
                     alt33=3;
                     }
                     break;
 
                 }
 
                 switch (alt33) {
             	case 1 :
             	    // nopC.g:384:6: '*' unary_expression[parent]
             	    {
             	    match(input,18,FOLLOW_18_in_multiplicative_expression1417); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_unary_expression_in_multiplicative_expression1421);
             	    unary_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nMUL X, Y\n");}
 
             	    }
             	    break;
             	case 2 :
             	    // nopC.g:385:7: '/' unary_expression[parent]
             	    {
             	    match(input,27,FOLLOW_27_in_multiplicative_expression1433); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_unary_expression_in_multiplicative_expression1437);
             	    unary_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nDIV Y, X\n SET X, Y \n");}
 
             	    }
             	    break;
             	case 3 :
             	    // nopC.g:386:7: '%' unary_expression[parent]
             	    {
             	    match(input,11,FOLLOW_11_in_multiplicative_expression1449); if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET PUSH, X\n");}
 
             	    pushFollow(FOLLOW_unary_expression_in_multiplicative_expression1453);
             	    unary_expression(parent);
 
             	    state._fsp--;
             	    if (state.failed) return ;
 
             	    if ( state.backtracking==0 ) {writeASM("SET Y, POP \nMOD Y, X\n SET X, Y \n");}
 
             	    }
             	    break;
 
             	default :
             	    break loop33;
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
             if ( state.backtracking>0 ) { memoize(input, 29, multiplicative_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "multiplicative_expression"
 
 
 
     // $ANTLR start "unary_expression"
     // nopC.g:393:1: unary_expression[GenericStatement parent] : (p= postfix_expression[parent] | '++' unary_expression[parent] | '--' unary_expression[parent] );
     public final void unary_expression(GenericStatement parent) throws RecognitionException {
         int unary_expression_StartIndex = input.index();
 
         String p =null;
 
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return ; }
 
             // nopC.g:394:2: (p= postfix_expression[parent] | '++' unary_expression[parent] | '--' unary_expression[parent] )
             int alt34=3;
             switch ( input.LA(1) ) {
             case NAME:
             case WERT:
             case 16:
                 {
                 alt34=1;
                 }
                 break;
             case 21:
                 {
                 alt34=2;
                 }
                 break;
             case 25:
                 {
                 alt34=3;
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
                     // nopC.g:394:4: p= postfix_expression[parent]
                     {
                     pushFollow(FOLLOW_postfix_expression_in_unary_expression1487);
                     p=postfix_expression(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     }
                     break;
                 case 2 :
                     // nopC.g:395:4: '++' unary_expression[parent]
                     {
                     match(input,21,FOLLOW_21_in_unary_expression1494); if (state.failed) return ;
 
                     pushFollow(FOLLOW_unary_expression_in_unary_expression1496);
                     unary_expression(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {writeASM("ADD X, 1\n"); if (p != null) {writeSetRegToMemory("X", p, parent.getScope()); }}
 
                     }
                     break;
                 case 3 :
                     // nopC.g:396:4: '--' unary_expression[parent]
                     {
                     match(input,25,FOLLOW_25_in_unary_expression1503); if (state.failed) return ;
 
                     pushFollow(FOLLOW_unary_expression_in_unary_expression1505);
                     unary_expression(parent);
 
                     state._fsp--;
                     if (state.failed) return ;
 
                     if ( state.backtracking==0 ) {writeASM("SUB X, 1\n"); if (p != null) {writeSetRegToMemory("X", p, parent.getScope()); }}
 
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
             if ( state.backtracking>0 ) { memoize(input, 30, unary_expression_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "unary_expression"
 
 
 
     // $ANTLR start "postfix_expression"
     // nopC.g:399:1: postfix_expression[GenericStatement parent] returns [String varname] : p= primary_expression[parent] ( '++' | '--' )* ;
     public final String postfix_expression(GenericStatement parent) throws RecognitionException {
         String varname = null;
 
         int postfix_expression_StartIndex = input.index();
 
         String p =null;
 
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return varname; }
 
             // nopC.g:400:2: (p= primary_expression[parent] ( '++' | '--' )* )
             // nopC.g:400:6: p= primary_expression[parent] ( '++' | '--' )*
             {
             pushFollow(FOLLOW_primary_expression_in_postfix_expression1529);
             p=primary_expression(parent);
 
             state._fsp--;
             if (state.failed) return varname;
 
             // nopC.g:401:9: ( '++' | '--' )*
             loop35:
             do {
                 int alt35=3;
                 int LA35_0 = input.LA(1);
 
                 if ( (LA35_0==21) ) {
                     alt35=1;
                 }
                 else if ( (LA35_0==25) ) {
                     alt35=2;
                 }
 
 
                 switch (alt35) {
             	case 1 :
             	    // nopC.g:401:11: '++'
             	    {
             	    match(input,21,FOLLOW_21_in_postfix_expression1542); if (state.failed) return varname;
 
             	    if ( state.backtracking==0 ) {writeASM("ADD X, 1\n"); if (p != null) {writeSetRegToMemory("X", p, parent.getScope()); }}
 
             	    }
             	    break;
             	case 2 :
             	    // nopC.g:402:11: '--'
             	    {
             	    match(input,25,FOLLOW_25_in_postfix_expression1556); if (state.failed) return varname;
 
             	    if ( state.backtracking==0 ) {writeASM("SUB X, 1\n"); if (p != null) {writeSetRegToMemory("X", p, parent.getScope()); }}
 
             	    }
             	    break;
 
             	default :
             	    break loop35;
                 }
             } while (true);
 
 
             if ( state.backtracking==0 ) {varname = p;}
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 31, postfix_expression_StartIndex); }
 
         }
         return varname;
     }
     // $ANTLR end "postfix_expression"
 
 
 
     // $ANTLR start "primary_expression"
     // nopC.g:407:1: primary_expression[GenericStatement parent] returns [String varname] : ( NAME | '(' expression[parent] ')' | functionCall[parent] | WERT );
     public final String primary_expression(GenericStatement parent) throws RecognitionException {
         String varname = null;
 
         int primary_expression_StartIndex = input.index();
 
         Token NAME5=null;
         Token WERT6=null;
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return varname; }
 
             // nopC.g:408:2: ( NAME | '(' expression[parent] ')' | functionCall[parent] | WERT )
             int alt36=4;
             switch ( input.LA(1) ) {
             case NAME:
                 {
                 int LA36_1 = input.LA(2);
 
                 if ( (LA36_1==16) ) {
                     alt36=3;
                 }
                 else if ( (LA36_1==EOF||(LA36_1 >= 10 && LA36_1 <= 11)||(LA36_1 >= 13 && LA36_1 <= 14)||(LA36_1 >= 17 && LA36_1 <= 18)||(LA36_1 >= 20 && LA36_1 <= 21)||(LA36_1 >= 23 && LA36_1 <= 25)||LA36_1==27||(LA36_1 >= 29 && LA36_1 <= 32)||LA36_1==34||(LA36_1 >= 36 && LA36_1 <= 39)||(LA36_1 >= 41 && LA36_1 <= 42)||LA36_1==56||LA36_1==58) ) {
                     alt36=1;
                 }
                 else {
                     if (state.backtracking>0) {state.failed=true; return varname;}
                     NoViableAltException nvae =
                         new NoViableAltException("", 36, 1, input);
 
                     throw nvae;
 
                 }
                 }
                 break;
             case 16:
                 {
                 alt36=2;
                 }
                 break;
             case WERT:
                 {
                 alt36=4;
                 }
                 break;
             default:
                 if (state.backtracking>0) {state.failed=true; return varname;}
                 NoViableAltException nvae =
                     new NoViableAltException("", 36, 0, input);
 
                 throw nvae;
 
             }
 
             switch (alt36) {
                 case 1 :
                     // nopC.g:408:4: NAME
                     {
                     NAME5=(Token)match(input,NAME,FOLLOW_NAME_in_primary_expression1590); if (state.failed) return varname;
 
                     if ( state.backtracking==0 ) {writeSetVarToReg("X", (NAME5!=null?NAME5.getText():null), parent.getScope()); varname = (NAME5!=null?NAME5.getText():null);}
 
                     }
                     break;
                 case 2 :
                     // nopC.g:409:4: '(' expression[parent] ')'
                     {
                     match(input,16,FOLLOW_16_in_primary_expression1597); if (state.failed) return varname;
 
                     pushFollow(FOLLOW_expression_in_primary_expression1599);
                     expression(parent);
 
                     state._fsp--;
                     if (state.failed) return varname;
 
                     match(input,17,FOLLOW_17_in_primary_expression1602); if (state.failed) return varname;
 
                     }
                     break;
                 case 3 :
                     // nopC.g:410:4: functionCall[parent]
                     {
                     pushFollow(FOLLOW_functionCall_in_primary_expression1608);
                     functionCall(parent);
 
                     state._fsp--;
                     if (state.failed) return varname;
 
                     }
                     break;
                 case 4 :
                     // nopC.g:411:4: WERT
                     {
                     WERT6=(Token)match(input,WERT,FOLLOW_WERT_in_primary_expression1614); if (state.failed) return varname;
 
                     if ( state.backtracking==0 ) {writeSetImmidiateToReg("X", (WERT6!=null?WERT6.getText():null));}
 
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
             if ( state.backtracking>0 ) { memoize(input, 32, primary_expression_StartIndex); }
 
         }
         return varname;
     }
     // $ANTLR end "primary_expression"
 
 
 
     // $ANTLR start "typeSpecifier"
     // nopC.g:434:1: typeSpecifier : ( 'int' | 'void' | 'bool' );
     public final void typeSpecifier() throws RecognitionException {
         int typeSpecifier_StartIndex = input.index();
 
         try {
             if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return ; }
 
             // nopC.g:435:2: ( 'int' | 'void' | 'bool' )
             // nopC.g:
             {
             if ( input.LA(1)==44||input.LA(1)==51||input.LA(1)==53 ) {
                 input.consume();
                 state.errorRecovery=false;
                 state.failed=false;
             }
             else {
                 if (state.backtracking>0) {state.failed=true; return ;}
                 MismatchedSetException mse = new MismatchedSetException(null,input);
                 throw mse;
             }
 
 
             }
 
         }
         catch (RecognitionException re) {
             reportError(re);
             recover(input,re);
         }
 
         finally {
         	// do for sure before leaving
             if ( state.backtracking>0 ) { memoize(input, 33, typeSpecifier_StartIndex); }
 
         }
         return ;
     }
     // $ANTLR end "typeSpecifier"
 
     // $ANTLR start synpred4_nopC
     public final void synpred4_nopC_fragment() throws RecognitionException {
         // nopC.g:155:2: ( typeSpecifier NAME ( '=' | ';' | ',' ) )
         // nopC.g:155:3: typeSpecifier NAME ( '=' | ';' | ',' )
         {
         pushFollow(FOLLOW_typeSpecifier_in_synpred4_nopC95);
         typeSpecifier();
 
         state._fsp--;
         if (state.failed) return ;
 
         match(input,NAME,FOLLOW_NAME_in_synpred4_nopC97); if (state.failed) return ;
 
         if ( input.LA(1)==23||input.LA(1)==30||input.LA(1)==35 ) {
             input.consume();
             state.errorRecovery=false;
             state.failed=false;
         }
         else {
             if (state.backtracking>0) {state.failed=true; return ;}
             MismatchedSetException mse = new MismatchedSetException(null,input);
             throw mse;
         }
 
 
         }
 
     }
     // $ANTLR end synpred4_nopC
 
     // $ANTLR start synpred5_nopC
     public final void synpred5_nopC_fragment() throws RecognitionException {
         // nopC.g:156:4: ( typeSpecifier NAME '(' )
         // nopC.g:156:5: typeSpecifier NAME '('
         {
         pushFollow(FOLLOW_typeSpecifier_in_synpred5_nopC123);
         typeSpecifier();
 
         state._fsp--;
         if (state.failed) return ;
 
         match(input,NAME,FOLLOW_NAME_in_synpred5_nopC125); if (state.failed) return ;
 
         match(input,16,FOLLOW_16_in_synpred5_nopC128); if (state.failed) return ;
 
         }
 
     }
     // $ANTLR end synpred5_nopC
 
     // $ANTLR start synpred17_nopC
     public final void synpred17_nopC_fragment() throws RecognitionException {
         // nopC.g:213:5: ( ( functionCall[null] ';' ) )
         // nopC.g:213:5: ( functionCall[null] ';' )
         {
         // nopC.g:213:5: ( functionCall[null] ';' )
         // nopC.g:213:7: functionCall[null] ';'
         {
         pushFollow(FOLLOW_functionCall_in_synpred17_nopC453);
         functionCall(null);
 
         state._fsp--;
         if (state.failed) return ;
 
         match(input,30,FOLLOW_30_in_synpred17_nopC456); if (state.failed) return ;
 
         }
 
 
         }
 
     }
     // $ANTLR end synpred17_nopC
 
     // Delegated rules
 
     public final boolean synpred4_nopC() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred4_nopC_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred5_nopC() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred5_nopC_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
     public final boolean synpred17_nopC() {
         state.backtracking++;
         int start = input.mark();
         try {
             synpred17_nopC_fragment(); // can never throw exception
         } catch (RecognitionException re) {
             System.err.println("impossible: "+re);
         }
         boolean success = !state.failed;
         input.rewind(start);
         state.backtracking--;
         state.failed=false;
         return success;
     }
 
 
  
 
     public static final BitSet FOLLOW_globalFunctionOrStatement_in_cFile71 = new BitSet(new long[]{0x0028100000000002L});
     public static final BitSet FOLLOW_globalVariableDeclaration_in_globalFunctionOrStatement114 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_functionDefinition_in_globalFunctionOrStatement133 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_typeSpecifier_in_globalVariableDeclaration154 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_globalVariableDeclarationList_in_globalVariableDeclaration156 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_globalVariableDeclaration159 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_NAME_in_globalVariableDeclarationList189 = new BitSet(new long[]{0x0000000800800002L});
     public static final BitSet FOLLOW_35_in_globalVariableDeclarationList191 = new BitSet(new long[]{0x0000000000000100L});
     public static final BitSet FOLLOW_WERT_in_globalVariableDeclarationList197 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_23_in_globalVariableDeclarationList204 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_NAME_in_globalVariableDeclarationList211 = new BitSet(new long[]{0x0000000800800002L});
     public static final BitSet FOLLOW_35_in_globalVariableDeclarationList213 = new BitSet(new long[]{0x0000000000000100L});
     public static final BitSet FOLLOW_WERT_in_globalVariableDeclarationList219 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_typeSpecifier_in_variableDeclaration245 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_variableDeclarationList_in_variableDeclaration247 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_variableDeclaration250 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_NAME_in_variableDeclarationList278 = new BitSet(new long[]{0x0000000800800002L});
     public static final BitSet FOLLOW_35_in_variableDeclarationList281 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_expression_in_variableDeclarationList283 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_23_in_variableDeclarationList291 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_NAME_in_variableDeclarationList298 = new BitSet(new long[]{0x0000000800800002L});
     public static final BitSet FOLLOW_35_in_variableDeclarationList301 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_expression_in_variableDeclarationList303 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_typeSpecifier_in_functionDefinition336 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_NAME_in_functionDefinition338 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_16_in_functionDefinition342 = new BitSet(new long[]{0x0028100000020000L});
     public static final BitSet FOLLOW_parameterList_in_functionDefinition344 = new BitSet(new long[]{0x0000000000020000L});
     public static final BitSet FOLLOW_17_in_functionDefinition347 = new BitSet(new long[]{0x0080000000000000L});
     public static final BitSet FOLLOW_55_in_functionDefinition349 = new BitSet(new long[]{0x087F700042210180L});
     public static final BitSet FOLLOW_statement_in_functionDefinition351 = new BitSet(new long[]{0x087F700042210180L});
     public static final BitSet FOLLOW_59_in_functionDefinition355 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_typeSpecifier_in_parameterList375 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_NAME_in_parameterList382 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_23_in_parameterList387 = new BitSet(new long[]{0x0028100000000000L});
     public static final BitSet FOLLOW_typeSpecifier_in_parameterList389 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_NAME_in_parameterList396 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_55_in_codeBlock417 = new BitSet(new long[]{0x087F700042210180L});
     public static final BitSet FOLLOW_statement_in_codeBlock418 = new BitSet(new long[]{0x087F700042210180L});
     public static final BitSet FOLLOW_59_in_codeBlock421 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_statement_in_codeBlock426 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_functionCall_in_statement453 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_statement456 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_assignment_in_statement465 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_statement468 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_variableDeclaration_in_statement476 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_selection_statement_in_statement483 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_iteration_statement_in_statement491 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_jump_statement_in_statement499 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_expression_statement_in_statement507 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_50_in_selection_statement528 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_16_in_selection_statement530 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_expression_in_selection_statement532 = new BitSet(new long[]{0x0000000000020000L});
     public static final BitSet FOLLOW_17_in_selection_statement535 = new BitSet(new long[]{0x00FF700042210180L});
     public static final BitSet FOLLOW_codeBlock_in_selection_statement537 = new BitSet(new long[]{0x0000800000000002L});
     public static final BitSet FOLLOW_47_in_selection_statement553 = new BitSet(new long[]{0x00FF700042210180L});
     public static final BitSet FOLLOW_codeBlock_in_selection_statement555 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_54_in_iteration_statement575 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_16_in_iteration_statement577 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_expression_in_iteration_statement579 = new BitSet(new long[]{0x0000000000020000L});
     public static final BitSet FOLLOW_17_in_iteration_statement582 = new BitSet(new long[]{0x00FF700042210180L});
     public static final BitSet FOLLOW_codeBlock_in_iteration_statement584 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_48_in_iteration_statement590 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_16_in_iteration_statement592 = new BitSet(new long[]{0x0000000042210180L});
     public static final BitSet FOLLOW_expression_statement_in_iteration_statement594 = new BitSet(new long[]{0x0000000042210180L});
     public static final BitSet FOLLOW_expression_statement_in_iteration_statement597 = new BitSet(new long[]{0x0000000002230180L});
     public static final BitSet FOLLOW_expression_in_iteration_statement600 = new BitSet(new long[]{0x0000000000020000L});
     public static final BitSet FOLLOW_17_in_iteration_statement604 = new BitSet(new long[]{0x00FF700042210180L});
     public static final BitSet FOLLOW_codeBlock_in_iteration_statement606 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_30_in_expression_statement621 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_expression_in_expression_statement626 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_expression_statement629 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_49_in_jump_statement646 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_NAME_in_jump_statement648 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_jump_statement650 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_46_in_jump_statement655 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_jump_statement657 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_45_in_jump_statement662 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_jump_statement664 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_52_in_jump_statement669 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_jump_statement671 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_52_in_jump_statement676 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_expression_in_jump_statement678 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_jump_statement681 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_NAME_in_functionCall701 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_16_in_functionCall703 = new BitSet(new long[]{0x0000000000820180L});
     public static final BitSet FOLLOW_functionCallArgumentList_in_functionCall707 = new BitSet(new long[]{0x0000000000020000L});
     public static final BitSet FOLLOW_17_in_functionCall710 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_NAME_in_functionCallArgumentList729 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_functionCall_in_functionCallArgumentList733 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_WERT_in_functionCallArgumentList738 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_23_in_functionCallArgumentList743 = new BitSet(new long[]{0x0000000000000180L});
     public static final BitSet FOLLOW_NAME_in_functionCallArgumentList746 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_functionCall_in_functionCallArgumentList750 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_WERT_in_functionCallArgumentList755 = new BitSet(new long[]{0x0000000000800002L});
     public static final BitSet FOLLOW_NAME_in_assignment777 = new BitSet(new long[]{0x0200090A14489000L});
     public static final BitSet FOLLOW_assignmentOperator_in_assignment781 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_expression_in_assignment783 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_logical_or_expression_in_expression864 = new BitSet(new long[]{0x0000020000000002L});
     public static final BitSet FOLLOW_41_in_expression877 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_expression_in_expression884 = new BitSet(new long[]{0x0000000020000000L});
     public static final BitSet FOLLOW_29_in_expression892 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_expression_in_expression899 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_logical_and_expression_in_logical_or_expression921 = new BitSet(new long[]{0x0400000000000002L});
     public static final BitSet FOLLOW_58_in_logical_or_expression934 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_logical_and_expression_in_logical_or_expression938 = new BitSet(new long[]{0x0400000000000002L});
     public static final BitSet FOLLOW_inclusive_or_expression_in_logical_and_expression962 = new BitSet(new long[]{0x0000000000002002L});
     public static final BitSet FOLLOW_13_in_logical_and_expression975 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_inclusive_or_expression_in_logical_and_expression979 = new BitSet(new long[]{0x0000000000002002L});
     public static final BitSet FOLLOW_exclusive_or_expression_in_inclusive_or_expression1003 = new BitSet(new long[]{0x0100000000000002L});
     public static final BitSet FOLLOW_56_in_inclusive_or_expression1016 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_exclusive_or_expression_in_inclusive_or_expression1020 = new BitSet(new long[]{0x0100000000000002L});
     public static final BitSet FOLLOW_and_expression_in_exclusive_or_expression1044 = new BitSet(new long[]{0x0000040000000002L});
     public static final BitSet FOLLOW_42_in_exclusive_or_expression1055 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_and_expression_in_exclusive_or_expression1059 = new BitSet(new long[]{0x0000040000000002L});
     public static final BitSet FOLLOW_equality_expression_in_and_expression1082 = new BitSet(new long[]{0x0000000000004002L});
     public static final BitSet FOLLOW_14_in_and_expression1093 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_equality_expression_in_and_expression1097 = new BitSet(new long[]{0x0000000000004002L});
     public static final BitSet FOLLOW_relational_expression_in_equality_expression1123 = new BitSet(new long[]{0x0000001000000402L});
     public static final BitSet FOLLOW_36_in_equality_expression1136 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_relational_expression_in_equality_expression1140 = new BitSet(new long[]{0x0000001000000402L});
     public static final BitSet FOLLOW_10_in_equality_expression1150 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_relational_expression_in_equality_expression1154 = new BitSet(new long[]{0x0000001000000402L});
     public static final BitSet FOLLOW_shift_expression_in_relational_expression1183 = new BitSet(new long[]{0x0000006480000002L});
     public static final BitSet FOLLOW_31_in_relational_expression1198 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_shift_expression_in_relational_expression1202 = new BitSet(new long[]{0x0000006480000002L});
     public static final BitSet FOLLOW_37_in_relational_expression1215 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_shift_expression_in_relational_expression1219 = new BitSet(new long[]{0x0000006480000002L});
     public static final BitSet FOLLOW_34_in_relational_expression1232 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_shift_expression_in_relational_expression1236 = new BitSet(new long[]{0x0000006480000002L});
     public static final BitSet FOLLOW_38_in_relational_expression1249 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_shift_expression_in_relational_expression1253 = new BitSet(new long[]{0x0000006480000002L});
     public static final BitSet FOLLOW_additive_expression_in_shift_expression1283 = new BitSet(new long[]{0x0000008100000002L});
     public static final BitSet FOLLOW_32_in_shift_expression1295 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_additive_expression_in_shift_expression1299 = new BitSet(new long[]{0x0000008100000002L});
     public static final BitSet FOLLOW_39_in_shift_expression1309 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_additive_expression_in_shift_expression1313 = new BitSet(new long[]{0x0000008100000002L});
     public static final BitSet FOLLOW_multiplicative_expression_in_additive_expression1340 = new BitSet(new long[]{0x0000000001100002L});
     public static final BitSet FOLLOW_20_in_additive_expression1355 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_multiplicative_expression_in_additive_expression1359 = new BitSet(new long[]{0x0000000001100002L});
     public static final BitSet FOLLOW_24_in_additive_expression1371 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_multiplicative_expression_in_additive_expression1375 = new BitSet(new long[]{0x0000000001100002L});
     public static final BitSet FOLLOW_unary_expression_in_multiplicative_expression1401 = new BitSet(new long[]{0x0000000008040802L});
     public static final BitSet FOLLOW_18_in_multiplicative_expression1417 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_unary_expression_in_multiplicative_expression1421 = new BitSet(new long[]{0x0000000008040802L});
     public static final BitSet FOLLOW_27_in_multiplicative_expression1433 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_unary_expression_in_multiplicative_expression1437 = new BitSet(new long[]{0x0000000008040802L});
     public static final BitSet FOLLOW_11_in_multiplicative_expression1449 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_unary_expression_in_multiplicative_expression1453 = new BitSet(new long[]{0x0000000008040802L});
     public static final BitSet FOLLOW_postfix_expression_in_unary_expression1487 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_21_in_unary_expression1494 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_unary_expression_in_unary_expression1496 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_25_in_unary_expression1503 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_unary_expression_in_unary_expression1505 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_primary_expression_in_postfix_expression1529 = new BitSet(new long[]{0x0000000002200002L});
     public static final BitSet FOLLOW_21_in_postfix_expression1542 = new BitSet(new long[]{0x0000000002200002L});
     public static final BitSet FOLLOW_25_in_postfix_expression1556 = new BitSet(new long[]{0x0000000002200002L});
     public static final BitSet FOLLOW_NAME_in_primary_expression1590 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_16_in_primary_expression1597 = new BitSet(new long[]{0x0000000002210180L});
     public static final BitSet FOLLOW_expression_in_primary_expression1599 = new BitSet(new long[]{0x0000000000020000L});
     public static final BitSet FOLLOW_17_in_primary_expression1602 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_functionCall_in_primary_expression1608 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_WERT_in_primary_expression1614 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_typeSpecifier_in_synpred4_nopC95 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_NAME_in_synpred4_nopC97 = new BitSet(new long[]{0x0000000840800000L});
     public static final BitSet FOLLOW_set_in_synpred4_nopC99 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_typeSpecifier_in_synpred5_nopC123 = new BitSet(new long[]{0x0000000000000080L});
     public static final BitSet FOLLOW_NAME_in_synpred5_nopC125 = new BitSet(new long[]{0x0000000000010000L});
     public static final BitSet FOLLOW_16_in_synpred5_nopC128 = new BitSet(new long[]{0x0000000000000002L});
     public static final BitSet FOLLOW_functionCall_in_synpred17_nopC453 = new BitSet(new long[]{0x0000000040000000L});
     public static final BitSet FOLLOW_30_in_synpred17_nopC456 = new BitSet(new long[]{0x0000000000000002L});
 
 }
