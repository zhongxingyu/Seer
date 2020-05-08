 /*
  * This file is in the public domain.
  *
  * Derived from http://blogs.itemis.de/stundzig/archives/726
  */
 package test;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.Token;
 import org.eclipse.xtext.GrammarUtil;
 import org.eclipse.xtext.ParserRule;
 import org.eclipse.xtext.junit4.AbstractXtextTests;
 import org.eclipse.xtext.parser.IParseResult;
 import org.eclipse.xtext.parser.antlr.ITokenDefProvider;
 import org.eclipse.xtext.parser.antlr.Lexer;
 import org.eclipse.xtext.parser.antlr.XtextTokenStream;
 
 import com.github.usrprops_xtext.parser.antlr.UsrpropsDslParser;
 import com.google.inject.Inject;
 
 public class LexerAndParserTest extends AbstractXtextTests {
 
     @Inject
     private Lexer lexer;
 
     @Inject
     private ITokenDefProvider tokenDefProvider;
 
     @Inject
     private UsrpropsDslParser parser;
 
     private boolean debugTokenStream = false;
 
     public boolean isDebugTokenStream() {
         return debugTokenStream;
     }
 
     public void setDebugTokenStream(boolean debugTokenStream) {
         this.debugTokenStream = debugTokenStream;
     }
 
     /**
      * return the list of tokens created by the lexer from the given input
      */
     public List<Token> getTokens(String input) {
         CharStream stream = new ANTLRStringStream(input);
         lexer.setCharStream(stream);
         XtextTokenStream tokenStream = new XtextTokenStream(lexer, tokenDefProvider);
         @SuppressWarnings("unchecked")
         List<Token> tokens = tokenStream.getTokens();
         return tokens;
     }
 
     /**
      * return the name of the terminal rule for a given token
      */
     public String getTokenType(Token token) {
         return tokenDefProvider.getTokenDefMap().get(token.getType());
     }
 
     /**
     * @param input
     *            tokenize the input and then debug the tokens
     */
    public void debugTokens(String input) {
        List<Token> tokens = getTokens(input);
        debugTokens(tokens);
    }

    /**
      * @param tokens
      *            the tokens to debug to System.out
      */
     public void debugTokens(List<Token> tokens) {
         for (int i = 0; i < tokens.size(); i++) {
             Token token = tokens.get(i);
            System.out.println("Token type=" + getTokenType(token) + " typeId=" + token.getType() + " text=" + token.getText());
         }
     }
 
     /**
      * check whether an input is chopped into a list of expected token types
      */
     public void checkTokenisation(String input, String... expectedTokenTypes) {
         List<Token> tokens = getTokens(input);
         List<String> actualTokenTypes = new ArrayList<String>(tokens.size());
         for (int i = 0; i < tokens.size(); i++) {
             Token token = tokens.get(i);
             actualTokenTypes.add(getTokenType(token));
             if (debugTokenStream) {
                 debugTokens(tokens);
             }
         }
         assertEquals(input, Arrays.asList(expectedTokenTypes), actualTokenTypes);
     }
 
     /**
      * check that an input is not tokenised using a particular terminal rule
      */
     public void failTokenisation(String input, String unExpectedTokenType) {
         List<Token> tokens = getTokens(input);
         assertEquals(input, 1, tokens.size());
         Token token = tokens.get(0);
         assertNotSame(input, unExpectedTokenType, getTokenType(token));
     }
 
     /**
      * return the parse result for an input given a specific entry rule of the
      * grammar
      */
     public IParseResult getParseResult(String input, String entryRule) {
         ParserRule rule = (ParserRule) GrammarUtil.findRuleForName(getGrammarAccess().getGrammar(), entryRule);
         return parser.parse(rule, new StringReader(input));
     }
 
     /**
      * check that the input can be successfully parsed given a specific entry
      * rule of the grammar
      */
     public void checkParsing(String input, String entryRule) {
         IParseResult la = getParseResult(input, entryRule);
         assertFalse(input, la.hasSyntaxErrors());
     }
 
     /**
      * check that the input cannot be successfully parsed given a specific entry
      * rule of the grammar
      */
     public void failParsing(String input, String entryRule) {
         IParseResult la = getParseResult(input, entryRule);
         assertFalse(input, la.hasSyntaxErrors());
     }
 
     /**
      *
      * @param ruleName
      *            name of the terminal rule
      * @return The name of terminal rule will be capitalised and "RULE_" will be
      *         appended to the front of the ruleName
      */
     public static String rule(String ruleName) {
         return new StringBuilder("RULE_").append(ruleName.toUpperCase()).toString();
     }
 
     /**
      * Return the rule name for a keyword, which is the keyword enclosed in
      * single quotes
      *
      * @param keyword
      *            the keyword
      * @return the rule name for the keyword
      */
     public static String keyword(String keyword) {
         return new StringBuilder("'").append(keyword).append("'").toString();
     }
 
     /**
      * check that input is treated as a keyword by the grammar
      */
     public void checkKeyword(String input) {
         checkTokenisation(input, keyword(input));
     }
 
     /**
      * check that input is not treated as a keyword by the grammar
      */
     public void failKeyword(String keyword) {
         List<Token> tokens = getTokens(keyword);
         assertEquals(keyword, 1, tokens.size());
         String type = getTokenType(tokens.get(0));
         assertFalse(keyword, type.charAt(0) == '\'');
     }
 }
