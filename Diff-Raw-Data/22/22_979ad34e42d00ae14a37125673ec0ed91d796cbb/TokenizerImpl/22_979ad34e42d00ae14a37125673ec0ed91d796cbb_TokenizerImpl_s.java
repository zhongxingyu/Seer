 /*
  * Created on Apr 18, 2005
  */
 package org.soulspace.template.tokenizer.impl;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.MatchResult;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.PatternCompiler;
 import org.apache.oro.text.regex.PatternMatcher;
 import org.apache.oro.text.regex.PatternMatcherInput;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 import org.soulspace.template.exception.UnknownTokenException;
 import org.soulspace.template.tokenizer.IToken;
 import org.soulspace.template.tokenizer.ITokenList;
 import org.soulspace.template.tokenizer.TokenType;
 import org.soulspace.template.tokenizer.Tokenizer;
 import org.soulspace.template.util.RegExHelper;
 
 public class TokenizerImpl implements Tokenizer {
 
 	// pattern for section splitting
   private final static String REGEX_1 =
 		"(?:<\\?(!--.*?--)(?:\\?>))" // comments
     + "|(?:<?\\?(.*?)(?:\\?>))" // code
     + "|(.*?(?=(?:<\\?|\r\n|\n|$)))" // template text
   	;
 
   
   // pattern for token splitting of code
   private final static String REGEX_2 = 
     "(" + "(?:xml.*)" // XML Declaration
       + "|(?:if)" // IF
       + "|(?:else)" // ELSE
       + "|(?:foreach)" // FOREACH
       + "|(?:while)" // WHILE
       + "|(?:break)" // BREAK
       + "|(?:continue)" // CONTINUE
       + "|(?:string|numeric|list|map|method)" // DECLARATION
       + "|(?:(?:begin)|(?:\\{))" // BLOCK_BEGIN
       + "|(?:(?:end)|(?:\\}))" // BLOCK_END
       + "|(?:\\'((?:\\\\'|[^']))*\\')" // STRING_CONST
       + "|(?:(?:\\+|\\-)?\\d+(?:\\.\\d+){0,1})" // NUMBER_CONST
       + "|(?:\\()" // PAREN_LEFT
       + "|(?:\\))" // PAREN_RIGHT
       + "|(?:\\[)" // BRACKET_LEFT
       + "|(?:\\])" // BRACKET_RIGHT
       + "|(?:<-)"  // FOREACH ASSIGNMENT
       + "|(?:(?:>=)|(?:>)|(?:<=)|(?:<)|(?:==)|(?:!=))" // COMPARISON OPERATORS
       + "|(?:(?:-)|(?:\\+)|(?:\\*)|(?:\\/\\/)|(?:\\/)|(?:\\%))" // ARITHMETIC OPERATORS
       + "|(?:(?:\\&\\&)|(?:\\|\\|)|(?:\\!))" // BOOLEAN OPERATORS
       + "|(?:\\|)" // FILTER
       + "|(?:(?:eq)|(?:ne))" // STRING OPERATORS
       + "|(?:\\:)" // DEREFERENCE
       + "|(?:\\.)" // TYPE METHOD
       + "|(?:\\=)" // ASSIGN
       + "|(?:\\,)" // SEQUENCE
       + "|(?:\\w+)" // IDENTIFIER
            + ")";
 
   private static Pattern pattern1;
   private static Pattern pattern2;
   
   public TokenizerImpl() {
     super();
   }
 
   public ITokenList createTokenList() {
   	return new TokenList();
   }
   
   /**
    * Scan a template and split it into a list of tokens.
    * 
    * @param template
    *          Template to parse.
    * @return token list
    * @throws UnknownTokenException
    */
   public ITokenList tokenize(String template) {
     ITokenList tokenList = new TokenList();
     return tokenize(tokenList, template);
   }
 
   public ITokenList tokenize(ITokenList tokenList, String template) {
     PatternCompiler compiler = new Perl5Compiler();
     PatternMatcher matcher = new Perl5Matcher();
     PatternMatcherInput input;
     MatchResult result;
     String comment = null;
     String code = null;
     String text = null;
 
     if(pattern1 == null) {
       try {
         pattern1 = compiler.compile(REGEX_1, Perl5Compiler.SINGLELINE_MASK);
       } catch (MalformedPatternException e) {
         throw new UnknownTokenException("Bad pattern: " + e.getMessage());
       }
     }
       
     input = new PatternMatcherInput(template);
     while (matcher.contains(input, pattern1)) {
       result = matcher.getMatch();
       comment = result.group(1);
       code = result.group(2);
       text = result.group(3);
 
       // comment, just count lines
       if(comment != null) {
        tokenList.incCurrentLines(comment.split("(\r\n|\n)").length - 1);
       }
       
       if (code != null) {
      	String[] codeLines = code.split("(\r\n|\n)");
       	boolean incLine = false;
       	for(String codeLine : codeLines) {
         	if(incLine) {
         		// count line
         		tokenList.incCurrentLine();
         	} else {
         		incLine = true;
         	}
         	tokenizeCodeInput(tokenList, codeLine);      		
       	}
       }
       if (text != null) {
       	// count line
      	if(text.matches("(?s)^(\r\n|\n)(.)*$")) {
       		tokenList.incCurrentLine();      		
       	}
      	String[] textLines = text.split("(\r\n|\n)");
      	for(String textLine : textLines) {
        	tokenizeText(tokenList, textLine);      		
       	}
       }
     }
 
     return tokenList;  	
   }
   
   /**
    * Tokenize a piece of code
    * 
    * @param code
    * @throws UnknownTokenException
    */
   void tokenizeCodeInput(ITokenList tokenList, String code) throws UnknownTokenException {
     PatternCompiler compiler = new Perl5Compiler();
     PatternMatcher matcher = new Perl5Matcher();
     PatternMatcherInput input;
     MatchResult result;
 
 //		System.out.println("CODE Line " + tokenList.getCurrentLine() + ": '" + code +"'");
 
     if(pattern2 == null) {
       try {
         pattern2 = compiler.compile(REGEX_2);
       } catch (MalformedPatternException e) {
         System.out.println("Bad pattern: " + REGEX_2 + ".");
         System.out.println(e.getMessage());
         e.printStackTrace();
         return;
       }
     }
     
     input = new PatternMatcherInput(code);
 
     while (matcher.contains(input, pattern2)) {
       result = matcher.getMatch();
 
       String match = result.group(1);
       if (match != null && !match.equalsIgnoreCase("")) {
         tokenizeCode(tokenList, match);
       }
     }
   }
 
   /**
    * Create a new TEXT token and add it to the Tokenlist
    * 
    * @param html
    */
   void tokenizeText(ITokenList tokenList, String text) {
 //		System.out.println("TEXT Line " + tokenList.getCurrentLine() + ": '" + text + "'");
   	IToken token = tokenList.lookUpLastToken();
     if (token != null && token.getType().equals(TokenType.TEXT)) {
     	// append text to last token
       token.setData(token.getData() + text);
     } else if (token != null && !token.getType().equals(TokenType.TEXT)
         && !token.getType().equals(TokenType.IDENTIFIER)) {
       if (RegExHelper.match(text, "^(\\n|\\r\\n)(\\w*)$") != null) {
         // filter blank lines
        tokenList.addToken(TokenType.TEXT, "");
       } else {
       	// create new token
         tokenList.addToken(TokenType.TEXT, text);
       }
     } else {
     	// create new token
       tokenList.addToken(TokenType.TEXT, text);
     }
   }
 
   /**
    * Create a new code token and add it to the Tokenlist
    * 
    * @param code
    * @throws UnknownTokenException
    */
   void tokenizeCode(ITokenList tokenList, String code) throws UnknownTokenException {
     MatchResult result;
 
     if ((result = RegExHelper.match(code, "^(xml.*)$")) != null) {
       tokenList.addToken(TokenType.TEXT, "<?" + result.group(0) + "?>");
     } else if ((result = RegExHelper.match(code, "if")) != null) {
       tokenList.addToken(TokenType.IF);
     } else if ((result = RegExHelper.match(code, "else")) != null) {
       tokenList.addToken(TokenType.ELSE);
     } else if ((result = RegExHelper.match(code, "foreach")) != null) {
       tokenList.addToken(TokenType.FOREACH);
     } else if ((result = RegExHelper.match(code, "while")) != null) {
       tokenList.addToken(TokenType.WHILE);
     } else if ((result = RegExHelper.match(code, "break")) != null) {
       tokenList.addToken(TokenType.BREAK);
     } else if ((result = RegExHelper.match(code, "continue")) != null) {
       tokenList.addToken(TokenType.CONTINUE);
     } else if ((result = RegExHelper.match(code,
         "(string|numeric|list|map|method)")) != null) {
       tokenList.addToken(TokenType.DECLARATION, result.group(0));
     } else if ((result = RegExHelper.match(code, "(begin|\\{)")) != null) {
       tokenList.addToken(TokenType.BLOCK_BEGIN);
     } else if ((result = RegExHelper.match(code, "(end|\\})")) != null) {
       tokenList.addToken(TokenType.BLOCK_END);
     } else if ((result = RegExHelper.match(code, "\\'((?:\\\\'|[^'])*)\\'")) != null) {
     	// replace "\'" with "'"
       tokenList.addToken(TokenType.STRING_CONST, result.group(1).replace("\\'", "'"));
     } else if ((result = RegExHelper.match(code, "((?:\\+|\\-)?\\d+(?:\\.\\d+){0,1})")) != null) {
       tokenList.addToken(TokenType.NUMBER_CONST, result.group(0));
 
       // Operators
     } else if ((result = RegExHelper.match(code, "\\(")) != null) {
       tokenList.addToken(TokenType.PAREN_LEFT);
     } else if ((result = RegExHelper.match(code, "\\)")) != null) {
       tokenList.addToken(TokenType.PAREN_RIGHT);
     } else if ((result = RegExHelper.match(code, "\\[")) != null) {
       tokenList.addToken(TokenType.BRACKET_LEFT);
     } else if ((result = RegExHelper.match(code, "\\]")) != null) {
       tokenList.addToken(TokenType.BRACKET_RIGHT);
     } else if ((result = RegExHelper.match(code, "<-")) != null) {
       tokenList.addToken(TokenType.FOREACH_ASSIGN);
     } else if ((result = RegExHelper.match(code, "<=")) != null) {
       tokenList.addToken(TokenType.LESS_EQUAL);
     } else if ((result = RegExHelper.match(code, ">=")) != null) {
       tokenList.addToken(TokenType.GREATER_EQUAL);
     } else if ((result = RegExHelper.match(code, "<")) != null) {
       tokenList.addToken(TokenType.LESS);
     } else if ((result = RegExHelper.match(code, ">")) != null) {
       tokenList.addToken(TokenType.GREATER);
     } else if ((result = RegExHelper.match(code, "==")) != null) {
       tokenList.addToken(TokenType.EQUAL);
     } else if ((result = RegExHelper.match(code, "!=")) != null) {
       tokenList.addToken(TokenType.NOT_EQUAL);
     } else if ((result = RegExHelper.match(code, "eq")) != null) {
       tokenList.addToken(TokenType.STRING_EQUAL);
     } else if ((result = RegExHelper.match(code, "ne")) != null) {
       tokenList.addToken(TokenType.STRING_NOT_EQUAL);
     } else if ((result = RegExHelper.match(code, "\\+")) != null) {
       tokenList.addToken(TokenType.PLUS);
     } else if ((result = RegExHelper.match(code, "-")) != null) {
       tokenList.addToken(TokenType.MINUS);
     } else if ((result = RegExHelper.match(code, "\\*")) != null) {
       tokenList.addToken(TokenType.MULT);
     } else if ((result = RegExHelper.match(code, "\\/\\/")) != null) {
       tokenList.addToken(TokenType.IDIV);
     } else if ((result = RegExHelper.match(code, "\\/")) != null) {
       tokenList.addToken(TokenType.DIV);
     } else if ((result = RegExHelper.match(code, "\\%")) != null) {
       tokenList.addToken(TokenType.MODULO);
     } else if ((result = RegExHelper.match(code, "\\&\\&")) != null) {
       tokenList.addToken(TokenType.LOGICAL_AND);
     } else if ((result = RegExHelper.match(code, "\\|\\|")) != null) {
       tokenList.addToken(TokenType.LOGICAL_OR);
     } else if ((result = RegExHelper.match(code, "!")) != null) {
       tokenList.addToken(TokenType.LOGICAL_NOT);
     } else if ((result = RegExHelper.match(code, "\\|")) != null) {
       tokenList.addToken(TokenType.FILTER);
     } else if ((result = RegExHelper.match(code, ":")) != null) {
       tokenList.addToken(TokenType.DEREFERENCE);
     } else if ((result = RegExHelper.match(code, "\\.")) != null) {
       tokenList.addToken(TokenType.TYPE_METHOD_CALL);
     } else if ((result = RegExHelper.match(code, "=")) != null) {
       tokenList.addToken(TokenType.ASSIGN);
     } else if ((result = RegExHelper.match(code, "\\,")) != null) {
       tokenList.addToken(TokenType.SEPERATOR);
     } else if ((result = RegExHelper.match(code, "\\w+(?:\\:\\w+)*")) != null) {
       // Identifier
       tokenList.addToken(TokenType.IDENTIFIER, result.group(0));
     } else {
       // Unknown Token
       UnknownTokenException ex = new UnknownTokenException("Unknown Token '"
           + code + "'!");
       throw ex;
     }
   }
 }
