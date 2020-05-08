 package ical.parser;
 
 import org.antlr.v4.runtime.ANTLRInputStream;
 import org.antlr.v4.runtime.Token;
 import org.junit.Test;
 
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 
 public class ICalendarLexerTest {
 
     /**
      * Helper method to get exactly 1 token from `source`. It will fail when
      * there are less, or more than a single token in `source`.
      *
      * @param source
      *         the input source to tokenize.
      *
      * @return a token created from `source`.
      */
     private Token getToken(String source) {
 
         List<? extends Token> tokens = getTokens(source);
 
         if (tokens.size() != 1) {
             fail("expected exactly 1 token for input: '" + source + "', encountered: " + tokens);
         }
 
         return tokens.get(0);
     }
 
     /**
      * Creates a list of tokens given some input `source`.
      *
      * @param source
      *         the input to tokenize.
      *
      * @return a list of tokens given some input `source`.
      */
     private List<? extends Token> getTokens(String source) {
 
         ICalendarLexer lexer = new ICalendarLexer(new ANTLRInputStream(source));
         return lexer.getAllTokens();
     }
 
     @Test
     public void LINE_FOLD_Test() {
         assertThat(getTokens("\r\n ").size(), is(0));
         assertThat(getTokens("\n\t").size(), is(0));
     }
 
     @Test
     public void WSP_Test() {
         assertThat(getToken(" ").getType(), is(ICalendarLexer.WSP));
         assertThat(getToken("\t").getType(), is(ICalendarLexer.WSP));
     }
 
     @Test
     public void EscapedChar_Test() {
         assertThat(getToken("\\n").getType(), is(ICalendarLexer.ESCAPED_CHAR));
         assertThat(getToken("\\\\").getType(), is(ICalendarLexer.ESCAPED_CHAR));
         assertThat(getToken("\\,").getType(), is(ICalendarLexer.ESCAPED_CHAR));
         assertThat(getToken("\\;").getType(), is(ICalendarLexer.ESCAPED_CHAR));
     }
 
     @Test
     public void CRLF_Test() {
         assertThat(getToken("\r\n").getType(), is(ICalendarLexer.CRLF));
         assertThat(getToken("\n").getType(), is(ICalendarLexer.CRLF));
     }
 
     @Test
     public void X21_Test() {
         assertThat(getToken("!").getType(), is(ICalendarLexer.EXCLAMATION));
     }
 
     @Test
     public void X22_Test() {
         assertThat(getToken("\"").getType(), is(ICalendarLexer.DQUOTE));
     }
 
     @Test
     public void X23_Test() {
         assertThat(getToken("#").getType(), is(ICalendarLexer.HASH));
     }
 
     @Test
     public void X24_Test() {
         assertThat(getToken("$").getType(), is(ICalendarLexer.DOLLAR));
     }
 
     @Test
     public void X25_Test() {
         assertThat(getToken("%").getType(), is(ICalendarLexer.X25));
     }
 
     @Test
     public void X26_Test() {
         assertThat(getToken("&").getType(), is(ICalendarLexer.AMP));
     }
 
     @Test
     public void X27_Test() {
         assertThat(getToken("'").getType(), is(ICalendarLexer.X27));
     }
 
     @Test
     public void X28_Test() {
         assertThat(getToken("(").getType(), is(ICalendarLexer.X28));
     }
 
     @Test
     public void X29_Test() {
         assertThat(getToken(")").getType(), is(ICalendarLexer.X29));
     }
 
     @Test
     public void X2A_Test() {
         assertThat(getToken("*").getType(), is(ICalendarLexer.X2A));
     }
 
     @Test
     public void X2B_Test() {
         assertThat(getToken("+").getType(), is(ICalendarLexer.PLUS));
     }
 
     @Test
     public void X2C_Test() {
         assertThat(getToken(",").getType(), is(ICalendarLexer.COMMA));
     }
 
     @Test
     public void X2D_Test() {
         assertThat(getToken("-").getType(), is(ICalendarLexer.MINUS));
     }
 
     @Test
     public void X2E_Test() {
         assertThat(getToken(".").getType(), is(ICalendarLexer.DOT));
     }
 
     @Test
     public void X2F_Test() {
         assertThat(getToken("/").getType(), is(ICalendarLexer.FSLASH));
     }
 
     @Test
     public void X30_Test() {
         assertThat(getToken("0").getType(), is(ICalendarLexer.D0));
     }
 
     @Test
     public void X31_Test() {
         assertThat(getToken("1").getType(), is(ICalendarLexer.D1));
     }
 
     @Test
     public void X32_Test() {
         assertThat(getToken("2").getType(), is(ICalendarLexer.D2));
     }
 
     @Test
     public void X33_Test() {
         assertThat(getToken("3").getType(), is(ICalendarLexer.D3));
     }
 
     @Test
     public void X34_Test() {
         assertThat(getToken("4").getType(), is(ICalendarLexer.D4));
     }
 
     @Test
     public void X35_Test() {
         assertThat(getToken("5").getType(), is(ICalendarLexer.D5));
     }
 
     @Test
     public void X36_Test() {
         assertThat(getToken("6").getType(), is(ICalendarLexer.D6));
     }
 
     @Test
     public void X37_Test() {
         assertThat(getToken("7").getType(), is(ICalendarLexer.D7));
     }
 
     @Test
     public void X38_Test() {
         assertThat(getToken("8").getType(), is(ICalendarLexer.D8));
     }
 
     @Test
     public void X39_Test() {
         assertThat(getToken("9").getType(), is(ICalendarLexer.D9));
     }
 
     @Test
     public void X3A_Test() {
         assertThat(getToken(":").getType(), is(ICalendarLexer.COL));
     }
 
     @Test
     public void X3B_Test() {
         assertThat(getToken(";").getType(), is(ICalendarLexer.SCOL));
     }
 
     @Test
     public void X3C_Test() {
         assertThat(getToken("<").getType(), is(ICalendarLexer.X3C));
     }
 
     @Test
     public void X3D_Test() {
         assertThat(getToken("=").getType(), is(ICalendarLexer.ASSIGN));
     }
 
     @Test
     public void X3E_Test() {
         assertThat(getToken(">").getType(), is(ICalendarLexer.X3E));
     }
 
     @Test
     public void X3F_Test() {
         assertThat(getToken("?").getType(), is(ICalendarLexer.X3F));
     }
 
     @Test
     public void X40_Test() {
         assertThat(getToken("@").getType(), is(ICalendarLexer.X40));
     }
 
     @Test
     public void X5B_Test() {
         assertThat(getToken("[").getType(), is(ICalendarLexer.X5B));
     }
 
     @Test
     public void X5C_Test() {
         assertThat(getToken("\\").getType(), is(ICalendarLexer.BSLASH));
     }
 
     @Test
     public void X5D_Test() {
         assertThat(getToken("]").getType(), is(ICalendarLexer.X5D));
     }
 
     @Test
     public void X5E_Test() {
         assertThat(getToken("^").getType(), is(ICalendarLexer.CARET));
     }
 
     @Test
     public void X5F_Test() {
         assertThat(getToken("_").getType(), is(ICalendarLexer.USCORE));
     }
 
     @Test
     public void X60_Test() {
         assertThat(getToken("`").getType(), is(ICalendarLexer.X60));
     }
 
     @Test
     public void X7B_Test() {
         assertThat(getToken("{").getType(), is(ICalendarLexer.X7B));
     }
 
     @Test
     public void X7C_Test() {
         assertThat(getToken("|").getType(), is(ICalendarLexer.X7C));
     }
 
     @Test
     public void X7D_Test() {
         assertThat(getToken("}").getType(), is(ICalendarLexer.X7D));
     }
 
     @Test
     public void X7E_Test() {
         assertThat(getToken("~").getType(), is(ICalendarLexer.X7E));
     }
 
     @Test
     public void A_Test() {
         assertThat(getToken("A").getType(), is(ICalendarLexer.A));
     }
 
     @Test
     public void B_Test() {
         assertThat(getToken("B").getType(), is(ICalendarLexer.B));
     }
 
     @Test
     public void C_Test() {
         assertThat(getToken("C").getType(), is(ICalendarLexer.C));
     }
 
     @Test
     public void D_Test() {
         assertThat(getToken("D").getType(), is(ICalendarLexer.D));
     }
 
     @Test
     public void E_Test() {
         assertThat(getToken("E").getType(), is(ICalendarLexer.E));
     }
 
     @Test
     public void F_Test() {
         assertThat(getToken("F").getType(), is(ICalendarLexer.F));
     }
 
     @Test
     public void G_Test() {
         assertThat(getToken("G").getType(), is(ICalendarLexer.G));
     }
 
     @Test
     public void H_Test() {
         assertThat(getToken("H").getType(), is(ICalendarLexer.H));
     }
 
     @Test
     public void I_Test() {
         assertThat(getToken("I").getType(), is(ICalendarLexer.I));
     }
 
     @Test
     public void J_Test() {
         assertThat(getToken("J").getType(), is(ICalendarLexer.J));
     }
 
     @Test
     public void K_Test() {
         assertThat(getToken("K").getType(), is(ICalendarLexer.K));
     }
 
     @Test
     public void L_Test() {
         assertThat(getToken("L").getType(), is(ICalendarLexer.L));
     }
 
     @Test
     public void M_Test() {
         assertThat(getToken("M").getType(), is(ICalendarLexer.M));
     }
 
     @Test
     public void N_Test() {
         assertThat(getToken("N").getType(), is(ICalendarLexer.N));
     }
 
     @Test
     public void O_Test() {
         assertThat(getToken("O").getType(), is(ICalendarLexer.O));
     }
 
     @Test
     public void P_Test() {
         assertThat(getToken("P").getType(), is(ICalendarLexer.P));
     }
 
     @Test
     public void Q_Test() {
         assertThat(getToken("Q").getType(), is(ICalendarLexer.Q));
     }
 
     @Test
     public void R_Test() {
         assertThat(getToken("R").getType(), is(ICalendarLexer.R));
     }
 
     @Test
     public void S_Test() {
         assertThat(getToken("S").getType(), is(ICalendarLexer.S));
     }
 
     @Test
     public void T_Test() {
         assertThat(getToken("T").getType(), is(ICalendarLexer.T));
     }
 
     @Test
     public void U_Test() {
         assertThat(getToken("U").getType(), is(ICalendarLexer.U));
     }
 
     @Test
     public void V_Test() {
         assertThat(getToken("V").getType(), is(ICalendarLexer.V));
     }
 
     @Test
     public void W_Test() {
         assertThat(getToken("W").getType(), is(ICalendarLexer.W));
     }
 
     @Test
     public void X_Test() {
         assertThat(getToken("X").getType(), is(ICalendarLexer.X));
     }
 
     @Test
     public void Y_Test() {
         assertThat(getToken("Y").getType(), is(ICalendarLexer.Y));
     }
 
     @Test
     public void Z_Test() {
         assertThat(getToken("Z").getType(), is(ICalendarLexer.Z));
     }
 
     @Test
     public void NON_US_ASCII_Test() {
         for (int ch = 129; ch <= 1024; ch++) {
             assertThat(getToken(String.valueOf((char) ch)).getType(), is(ICalendarLexer.NON_US_ASCII));
         }
     }
 }
