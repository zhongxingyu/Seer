 package ch.fhnw.cbip.compiler.scanner;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.fhnw.cbip.compiler.error.LexicalError;
 import ch.fhnw.cbip.compiler.scanner.enums.ModeAttribute;
 import ch.fhnw.cbip.compiler.scanner.enums.OperatorAttribute;
 import ch.fhnw.cbip.compiler.scanner.enums.TypeAttribute;
 import ch.fhnw.cbip.compiler.scanner.state.InitialState;
 import ch.fhnw.cbip.compiler.scanner.state.LetterState;
 import ch.fhnw.cbip.compiler.scanner.token.Ident;
 import ch.fhnw.cbip.compiler.scanner.token.Keyword;
 import ch.fhnw.cbip.compiler.scanner.token.Literal;
 import ch.fhnw.cbip.compiler.scanner.token.Mode;
 import ch.fhnw.cbip.compiler.scanner.token.Operator;
 import ch.fhnw.cbip.compiler.scanner.token.Symbol;
 import ch.fhnw.cbip.compiler.scanner.token.Type;
 
 public class ScannerTest {
     private Scanner scanner;
 
     @Before
     public void setUp() throws Exception {
         scanner = new Scanner();
     }
 
     @Test
     public void testSetState() {
         IScannerState initial = new InitialState();
         scanner.setState(initial, true);
 
         assertEquals(initial, scanner.getState());
 
         IScannerState letter = new LetterState();
         scanner.setState(letter, true);
 
         assertEquals(letter, scanner.getState());
     }
 
     @Test
     public void testScanDivide() throws LexicalError {
         ITokenList list = null;
         try {
             list = scanner.scan(getCode("divide.iml"));
         } catch (IOException e) {
             fail(e.getMessage());
         }
         if (list != null) {
 
         } else {
             fail("list is null");
         }
         ITokenList expectedList = new TokenList();
         Keyword.Program programm = new Keyword.Program();
         
         programm.setLine(1);
         expectedList.add(programm);
         Ident progIdent = new Ident("intDiv");
         progIdent.setLine(1);
         expectedList.add(progIdent);
         
         Keyword.Global global = new Keyword.Global();
         global.setLine(2);
         expectedList.add(global);
         
         Keyword.Proc proc = new Keyword.Proc();
         proc.setLine(3);
         expectedList.add(proc);
         Ident procIdent = new Ident("divide");
         procIdent.setLine(3);
         expectedList.add(procIdent);
             Symbol.LParen lparenProc = new Symbol.LParen();
             lparenProc.setLine(3);
             expectedList.add(lparenProc);
             
             Mode.FlowMode fmIn = new Mode.FlowMode(ModeAttribute.IN);
             fmIn.setLine(3);
             expectedList.add(fmIn);
             
             Mode.MechMode mmCp = new Mode.MechMode(ModeAttribute.COPY);
             mmCp.setLine(3);
             expectedList.add(mmCp);
             
             Ident mIdent3 = new Ident("m");
             mIdent3.setLine(3);
             expectedList.add(mIdent3);
             
             Symbol.Colon colProc = new Symbol.Colon();
             colProc.setLine(3);
             expectedList.add(colProc);
             
             Type intProc = new Type(TypeAttribute.INT32);
             intProc.setLine(3);
             expectedList.add(intProc);
             
             Symbol.Comma commaProc = new Symbol.Comma();
             commaProc.setLine(3);
             expectedList.add(commaProc);
             
             expectedList.add(fmIn);
             expectedList.add(mmCp);
             Ident nIdent3 = new Ident("n");
             nIdent3.setLine(3);
             expectedList.add(nIdent3);
             expectedList.add(colProc);
             expectedList.add(intProc);
             expectedList.add(commaProc);
             
             Mode.FlowMode fmIOut = new Mode.FlowMode(ModeAttribute.OUT);
             fmIOut.setLine(3);
             expectedList.add(fmIOut);
             Mode.MechMode mmRef = new Mode.MechMode(ModeAttribute.REF);
             mmRef.setLine(3);
             expectedList.add(mmRef);
             Ident qIdent3 = new Ident("q");
             qIdent3.setLine(3);
             expectedList.add(qIdent3);
             expectedList.add(colProc);
             expectedList.add(intProc);
             expectedList.add(commaProc);
             
             expectedList.add(fmIOut);
             expectedList.add(mmRef);
             Ident rIdent3 = new Ident("r");
             rIdent3.setLine(3);
             expectedList.add(rIdent3);
             expectedList.add(colProc);
             expectedList.add(intProc);
             
             Symbol.RParen rparenProc = new Symbol.RParen();
             rparenProc.setLine(3);
             expectedList.add(rparenProc);
         
         Symbol.LBrace lbrProc = new Symbol.LBrace();
         lbrProc.setLine(4);
         expectedList.add(lbrProc);
         
         Ident qIdent5 = new Ident("q");
         qIdent5.setLine(5);
         expectedList.add(qIdent5);
         Keyword.Init init5 = new Keyword.Init();
         init5.setLine(5);
         expectedList.add(init5);
         Symbol.Becomes bec5 = new Symbol.Becomes();
         bec5.setLine(5);
         expectedList.add(bec5);
         Literal lit5 = new Literal(0);
         lit5.setLine(5);
         expectedList.add(lit5);
         Symbol.Semicolon sc5 = new Symbol.Semicolon();
         sc5.setLine(5);
         expectedList.add(sc5);
         
         Ident rIdent6 = new Ident("r");
         rIdent6.setLine(6);
         expectedList.add(rIdent6);
         Keyword.Init init6 = new Keyword.Init();
         init6.setLine(6);
         expectedList.add(init6);
         Symbol.Becomes bec6 = new Symbol.Becomes();
         bec6.setLine(6);
         expectedList.add(bec6);
         Ident mIdent6 = new Ident("m");
         mIdent6.setLine(6);
         expectedList.add(mIdent6);
         Symbol.Semicolon sc6 = new Symbol.Semicolon();
         sc6.setLine(6);
         expectedList.add(sc6);
         
         Keyword.While while7 = new Keyword.While();
         while7.setLine(7);
         expectedList.add(while7);
         Symbol.LParen lparen7 = new Symbol.LParen();
         lparen7.setLine(7);
         expectedList.add(lparen7);
         Ident rIdent7 = new Ident("r");
         rIdent7.setLine(7);
         expectedList.add(rIdent7);
         Operator.RelOpr ge7 = new Operator.RelOpr(OperatorAttribute.GE);
         ge7.setLine(7);
         expectedList.add(ge7);
         Ident nIdent7 = new Ident("n");
         nIdent7.setLine(7);
         expectedList.add(nIdent7);
         Symbol.RParen rparen7 = new Symbol.RParen();
         rparen7.setLine(7);
         expectedList.add(rparen7);
         Symbol.LBrace lbr7 = new Symbol.LBrace();
         lbr7.setLine(7);
         expectedList.add(lbr7);
         
         Ident qIdent8 = new Ident("q");
         qIdent8.setLine(8);
         expectedList.add(qIdent8);
         Symbol.Becomes bec8 = new Symbol.Becomes();
         bec8.setLine(8);
         expectedList.add(bec8);
         expectedList.add(qIdent8);
         Operator.AddOpr plus8 = new Operator.AddOpr(OperatorAttribute.PLUS);
         plus8.setLine(8);
         expectedList.add(plus8);
         Literal lit8 = new Literal(1);
         lit8.setLine(8);
         expectedList.add(lit8);
         Symbol.Semicolon sc8 = new Symbol.Semicolon();
         sc8.setLine(8);
         expectedList.add(sc8);
         
         Ident rIdent9 = new Ident("r");
         rIdent9.setLine(9);
         expectedList.add(rIdent9);
         Symbol.Becomes bec9 = new Symbol.Becomes();
         bec9.setLine(9);
         expectedList.add(bec9);
         expectedList.add(rIdent9);
         Operator.AddOpr plus9 = new Operator.AddOpr(OperatorAttribute.MINUS);
         plus9.setLine(9);
         expectedList.add(plus9);
         Ident nIdent9 = new Ident("n");
         nIdent9.setLine(9);
         expectedList.add(nIdent9);
 
         Symbol.RBrace rbr10 = new Symbol.RBrace();
         rbr10.setLine(10);
         expectedList.add(rbr10);
         
         Symbol.RBrace rbr11 = new Symbol.RBrace();
         rbr11.setLine(11);
         expectedList.add(rbr11);
         Symbol.Semicolon sc11 = new Symbol.Semicolon();
         sc11.setLine(11);
         expectedList.add(sc11);
         
         Mode.ChangeMode var13 = new Mode.ChangeMode(ModeAttribute.VAR);
         var13.setLine(13);
         expectedList.add(var13);
         Ident mIdent13 = new Ident("m");
         mIdent13.setLine(13);
         expectedList.add(mIdent13);
         Symbol.Colon col13 = new Symbol.Colon();
         col13.setLine(13);
         expectedList.add(col13);
         Type int13 = new Type(TypeAttribute.INT32);
         int13.setLine(13);
         expectedList.add(int13);
         Symbol.Semicolon sc13 = new Symbol.Semicolon();
         sc13.setLine(13);
         expectedList.add(sc13);
 
         Mode.ChangeMode var14 = new Mode.ChangeMode(ModeAttribute.VAR);
         var14.setLine(14);
         expectedList.add(var14);
         Ident nIdent14 = new Ident("n");
         nIdent14.setLine(14);
         expectedList.add(nIdent14);
         Symbol.Colon col14 = new Symbol.Colon();
         col14.setLine(14);
         expectedList.add(col14);
         Type int14 = new Type(TypeAttribute.INT32);
         int14.setLine(14);
         expectedList.add(int14);
         Symbol.Semicolon sc14 = new Symbol.Semicolon();
         sc14.setLine(14);
         expectedList.add(sc14);
 
         Mode.ChangeMode var15 = new Mode.ChangeMode(ModeAttribute.VAR);
         var15.setLine(15);
         expectedList.add(var15);
         Ident qIdent15 = new Ident("q");
         qIdent15.setLine(15);
         expectedList.add(qIdent15);
         Symbol.Colon col15 = new Symbol.Colon();
         col15.setLine(15);
         expectedList.add(col15);
         Type int15 = new Type(TypeAttribute.INT32);
         int15.setLine(15);
         expectedList.add(int15);
         Symbol.Semicolon sc15 = new Symbol.Semicolon();
         sc15.setLine(15);
         expectedList.add(sc15);
 
         Mode.ChangeMode var16 = new Mode.ChangeMode(ModeAttribute.VAR);
         var16.setLine(16);
         expectedList.add(var16);
         Ident rIdent16 = new Ident("r");
         rIdent16.setLine(16);
         expectedList.add(rIdent16);
         Symbol.Colon col16 = new Symbol.Colon();
         col16.setLine(16);
         expectedList.add(col16);
         Type int16 = new Type(TypeAttribute.INT32);
         int16.setLine(16);
         expectedList.add(int16);
         
         Symbol.LBrace lbr17 = new Symbol.LBrace();
         lbr17.setLine(17);
         expectedList.add(lbr17);
         
         Symbol.QuestMark qm18 = new Symbol.QuestMark();
         qm18.setLine(18);
         expectedList.add(qm18);
         Ident mIdent18 = new Ident("m");
         mIdent18.setLine(18);
         expectedList.add(mIdent18);
         Keyword.Init init18 = new Keyword.Init();
         init18.setLine(18);
         expectedList.add(init18);
         Symbol.Semicolon sc18 = new Symbol.Semicolon();
         sc18.setLine(18);
         expectedList.add(sc18);
         
         Symbol.QuestMark qm19 = new Symbol.QuestMark();
         qm19.setLine(19);
         expectedList.add(qm19);
         Ident nIdent19 = new Ident("n");
         nIdent19.setLine(19);
         expectedList.add(nIdent19);
         Keyword.Init init19 = new Keyword.Init();
         init19.setLine(19);
         expectedList.add(init19);
         Symbol.Semicolon sc19 = new Symbol.Semicolon();
         sc19.setLine(19);
         expectedList.add(sc19);
         
         Keyword.Call call20 = new Keyword.Call();
         call20.setLine(20);
         expectedList.add(call20);
 
         Ident divIdent20 = new Ident("divide");
         divIdent20.setLine(20);
         expectedList.add(divIdent20);
 
         Symbol.LParen lparen20 = new Symbol.LParen();
         lparen20.setLine(20);
         expectedList.add(lparen20);
 
         Ident mIdent20 = new Ident("m");
         mIdent20.setLine(20);
         expectedList.add(mIdent20);
 
         Symbol.Comma comma20 = new Symbol.Comma();
         comma20.setLine(20);
         expectedList.add(comma20);
 
         Ident nIdent20 = new Ident("n");
         nIdent20.setLine(20);
         expectedList.add(nIdent20);
 
         expectedList.add(comma20);
 
         Ident qIdent20 = new Ident("q");
         qIdent20.setLine(20);
         expectedList.add(qIdent20);
 
         Keyword.Init init20 = new Keyword.Init();
         init20.setLine(20);
         expectedList.add(init20);
 
         expectedList.add(comma20);
 
         Ident rIdent20 = new Ident("r");
         rIdent20.setLine(20);
         expectedList.add(rIdent20);
         expectedList.add(init20);
 
         Symbol.RParen rparen20 = new Symbol.RParen();
         rparen20.setLine(20);
         expectedList.add(rparen20);
 
         Symbol.Semicolon sc20 = new Symbol.Semicolon();
         sc20.setLine(20);
         expectedList.add(sc20);        
         
         Symbol.ExclaMark qm21 = new Symbol.ExclaMark();
         qm21.setLine(21);
         expectedList.add(qm21);
         Ident qIdent21 = new Ident("q");
         qIdent21.setLine(21);
         expectedList.add(qIdent21);
         Symbol.Semicolon sc21 = new Symbol.Semicolon();
         sc21.setLine(21);
         expectedList.add(sc21);
         
         Symbol.ExclaMark qm22 = new Symbol.ExclaMark();
         qm22.setLine(22);
         expectedList.add(qm22);
         Ident rIdent22 = new Ident("r");
         rIdent22.setLine(22);
         expectedList.add(rIdent22);        
 
         Symbol.RBrace rbr23 = new Symbol.RBrace();
         rbr23.setLine(23);
         expectedList.add(rbr23);
 
         Keyword.Sentinel sentinel = new Keyword.Sentinel();
        sentinel.setLine(23);
         expectedList.add(sentinel);
 
         assertEquals(expectedList, list);
     }
 
     private BufferedReader getCode(String name) throws IOException {
         InputStream stream = this.getClass().getResourceAsStream(name);
         InputStreamReader isr = new InputStreamReader(stream);
         return new BufferedReader(isr);
     }
 
 }
