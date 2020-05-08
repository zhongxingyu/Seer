 package nd.regex;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static nd.regex.Token.Type;
 
 /**
  *
  */
 final class ParserImpl implements Parser {
 
     private final Lexer lexer;
     private final List<Token> tokens = new ArrayList<Token>();
     private Token current;
     private int currentIndex = -1;
 
     ParserImpl(Lexer lexer) {
         this.lexer = lexer;
     }
 
 
     @Override
     public AST parse() {
         init();
         AST root = new SequenceNode(new Token(Type.SEQUENCE, ""), true);
         while (current.type() != Type.EOF) {
             parse(root, current);
         }
         return root;
     }
 
     private void parse(AST currentRoot, Token token) {
         switch (token.type()) {
             case ZERO_OR_ONE:
             case ZERO_OR_MORE:
             case ONE_OR_MORE:
             case LEFT_CURLY_BRACKET:
                 parseQuantifier(currentRoot);
                 break;
             case LEFT_BRACKET:
             case LEFT_BRACKET_CARET:
                 parseCharacterClass(currentRoot);
                 break;
             case OR:
                 parseAlternative(currentRoot, false);
                 break;
             case LEFT_PAREN:
                 parseGroup(currentRoot);
                 break;
             case CLASS_ANY_CHARACTER:
             case CLASS_DIGIT:
             case CLASS_NON_DIGIT:
             case CLASS_WHITESPACE:
             case CLASS_NON_WHITESPACE:
             case CLASS_WORD_CHARACTER:
             case CLASS_NON_WORD_CHARACTER:
                 parsePredefinedCharacterClass(currentRoot);
                 break;
             case CHARACTER:
                 parseCharacter(currentRoot);
                 break;
             default:
                 throw new UnexpectedTokenException(token);
         }
     }
 
     private void parseCharacter(AST currentRoot) {
         currentRoot.addChild(new CharacterNode(current));
         match(Type.CHARACTER);
     }
 
     private void parseQuantifier(AST currentRoot) {
         if (currentRoot.children().isEmpty()) {
             throw new UnexpectedTokenException(current);
         }
         AST term = currentRoot.removeLastChild();
         AST quantifier;
         if (isPredefinedQuantifier(current.type())) {
             quantifier = parsePredefinedQuantifier(term);
         } else if (isUserDefinedQuantifier(current.type())) {
             quantifier = parseUserDefinedQuantifier(term);
         } else {
             throw new UnexpectedTokenException(current);
         }
         currentRoot.addChild(quantifier);
     }
 
     private boolean isPredefinedQuantifier(Type t) {
         return t == Type.ZERO_OR_ONE || t == Type.ZERO_OR_MORE || t == Type.ONE_OR_MORE;
     }
 
     private boolean isUserDefinedQuantifier(Type t) {
         return t == Type.LEFT_CURLY_BRACKET;
     }
 
     private QuantifierNode parsePredefinedQuantifier(AST term) {
         Token token = current;
         match(current.type());
         switch (token.type()) {
             case ZERO_OR_MORE: return new UnboundedQuantifierNode(term, token, 0);
             case ONE_OR_MORE : return new UnboundedQuantifierNode(term, token, 1);
             case ZERO_OR_ONE : return new BoundedQuantifierNode(term, token, 0, 1);
             default: throw new UnexpectedTokenException(token);
         }
     }
 
     private QuantifierNode parseUserDefinedQuantifier(AST term) {
         QuantifierNode quantifier;
         Token quantifierToken = current;
         match(Type.LEFT_CURLY_BRACKET);
         StringBuilder low = new StringBuilder();
         StringBuilder high = new StringBuilder();
         StringBuilder tmp = low;
         boolean oneBound = true;
         while (current.type() != Type.RIGHT_CURLY_BRACKET) {
             if (!Character.isDigit(current.text().charAt(0)) && !current.text().equals(","))
                 throw new UnexpectedTokenException(current);
             if (current.text().equals(",")) {
                 tmp = high;
                 oneBound = false;
                 match(Type.CHARACTER);
             } else {
                 tmp.append(current.text());
                 match(Type.CHARACTER);
             }
         }
         if (oneBound) {
             quantifier = new BoundedQuantifierNode(term, quantifierToken,
                     Integer.valueOf(low.toString()), Integer.valueOf(low.toString()));
         } else {
             if (high.length() > 0) {
                 quantifier = new BoundedQuantifierNode(term, quantifierToken,
                         Integer.valueOf(low.toString()), Integer.valueOf(high.toString()));
             } else {
                 quantifier = new UnboundedQuantifierNode(term, quantifierToken, Integer.valueOf(low.toString()));
             }
         }
         match(Type.RIGHT_CURLY_BRACKET);
         return quantifier;
     }
 
     private void parsePredefinedCharacterClass(AST currentRoot) {
         Token token = current;
         match(current.type());
         switch (token.type()) {
             case CLASS_ANY_CHARACTER:      currentRoot.addChild(ANY_CHARACTER_CLASS_NODE); break;
             case CLASS_DIGIT:              currentRoot.addChild(ANY_DIGIT_CLASS_NODE); break;
             case CLASS_NON_DIGIT:          currentRoot.addChild(ANY_NON_DIGIT_CLASS_NODE);break;
             case CLASS_WHITESPACE:         currentRoot.addChild(ANY_WHITESPACE_CLASS_NODE);break;
             case CLASS_NON_WHITESPACE:     currentRoot.addChild(ANY_NON_WHITESPACE_CLASS_NODE);break;
             case CLASS_WORD_CHARACTER:     currentRoot.addChild(ANY_WORD_CHARACTER_CLASS_NODE);break;
             case CLASS_NON_WORD_CHARACTER: currentRoot.addChild(ANY_NON_WORD_CHARACTER_CLASS_NODE);break;
             default: throw new UnexpectedTokenException(token);
         }
     }
 
     private void parseAlternative(AST currentRoot, boolean inGroup) {
         match(Type.OR);
         List<AST> firstAlternative = new ArrayList<AST>();
         while (!currentRoot.children().isEmpty()) {
             firstAlternative.add(currentRoot.removeLastChild());
         }
         Collections.reverse(firstAlternative);
         AST alternative = new AlternativeNode(firstAlternative);
         while (current.type() != Type.EOF) {
             if (inGroup && current.type() == Type.RIGHT_PAREN) break;
             parse(alternative, current);
         }
         currentRoot.addChild(alternative);
     }
 
     private void parseGroup(AST currentRoot) {
         AST group = new SequenceNode(current);
         match(Type.LEFT_PAREN);
         while (current.type() != Type.RIGHT_PAREN) {
             switch (current.type()) {
                 case OR:
                     parseAlternative(group, true);
                     break;
                 case LEFT_PAREN:
                     parseGroup(group);
                     break;
                 default:
                     parse(group, current);
                     break;
             }
         }
         match(Type.RIGHT_PAREN);
         currentRoot.addChild(group);
     }
 
     private void parseCharacterClass(AST currentRoot) {
         AST charClass = new CharacterClassNode(current);
         match(current.type());
         while (true) {
             switch (current.type()) {
                 case RIGHT_BRACKET:
                     if (lookBehind(1).type() == Type.LEFT_BRACKET || lookBehind(1).type() == Type.LEFT_BRACKET_CARET) {
                         charClass.addChild(new CharacterNode(new Token(Type.CHARACTER, "]")));
                         match(Type.RIGHT_BRACKET);
                     } else {
                         currentRoot.addChild(charClass);
                         match(Type.RIGHT_BRACKET);
                         return;
                     }
                     break;
                 case LEFT_BRACKET:
                 case LEFT_BRACKET_CARET:
                     parseCharacterClass(charClass);
                     break;
                 case CHARACTER:
                     if (lookahead(1).text().equals("-") && lookahead(2).type() == Type.CHARACTER) {
                         parseCharacterClassInterval(charClass);
                     } else {
                         parseCharacter(charClass);
                     }
                     break;
                 case EOF:
                     throw new UnexpectedTokenException(current);
                 default:
                     charClass.addChild(new CharacterNode(new Token(Type.CHARACTER, String.valueOf(current.text()))));
                     match(current.type());
             }
         }
     }
 
     private void parseCharacterClassInterval(AST currentRoot) {
         CharacterNode lowBound = new CharacterNode(current);
         match(Type.CHARACTER);
         match(Type.CHARACTER);
         CharacterNode highBound = new CharacterNode(current);
         match(Type.CHARACTER);
         CharacterClassIntervalNode interval = new CharacterClassIntervalNode(lowBound, highBound);
         currentRoot.addChild(interval);
     }
 
     private void init() {
         readTokens();
         consume();
     }
 
     private void readTokens() {
         while (lexer.hasNext()) {
             tokens.add(lexer.nextToken());
         }
     }
 
     private void consume() {
         currentIndex++;
         if (currentIndex >= tokens.size()) {
             current = new Token(Type.EOF, "EOF");
         } else {
             current = tokens.get(currentIndex);
         }
     }
 
     private void match(Token.Type type) {
         if (current.type().equals(type)) {
             consume();
         } else {
             throw new UnexpectedTokenException(current);
         }
     }
 
     private Token lookahead(int i) {
         if (currentIndex + i >= tokens.size()) {
             return new Token(Type.EOF, "EOF");
         } else {
             return tokens.get(currentIndex + i);
         }
     }
 
     private Token lookBehind(int i) {
         if (currentIndex - i < 0) {
             return new Token(Type.EOF, "EOF");
         } else {
             return tokens.get(currentIndex - i);
         }
     }
 
 
     //nodes for predefined character classes:
     private static final CharacterClassNode ANY_CHARACTER_CLASS_NODE = new CharacterClassNode(new Token(Type.CLASS_ANY_CHARACTER, "."));
     private static final CharacterClassNode ANY_DIGIT_CLASS_NODE = new CharacterClassNode(new Token(Type.LEFT_BRACKET, "["));
     private static final CharacterClassNode ANY_NON_DIGIT_CLASS_NODE = new CharacterClassNode(new Token(Type.LEFT_BRACKET_CARET, "[^"));
     private static final CharacterClassNode ANY_WHITESPACE_CLASS_NODE = new CharacterClassNode(new Token(Type.LEFT_BRACKET, "["));
     private static final CharacterClassNode ANY_NON_WHITESPACE_CLASS_NODE = new CharacterClassNode(new Token(Type.LEFT_BRACKET_CARET, "[^"));
     private static final CharacterClassNode ANY_WORD_CHARACTER_CLASS_NODE = new CharacterClassNode(new Token(Type.LEFT_BRACKET, "["));
     private static final CharacterClassNode ANY_NON_WORD_CHARACTER_CLASS_NODE = new CharacterClassNode(new Token(Type.LEFT_BRACKET_CARET, "[^"));
 
     static {
         CharacterClassIntervalNode digitsInterval = new CharacterClassIntervalNode(
                 new CharacterNode(new Token(Type.CHARACTER, "0")),
                 new CharacterNode(new Token(Type.CHARACTER, "9")));
         ANY_DIGIT_CLASS_NODE.addChild(digitsInterval);
         ANY_NON_DIGIT_CLASS_NODE.addChild(digitsInterval);
 
         List<CharacterNode> whitespaces = new ArrayList<CharacterNode>();
         whitespaces.add(new CharacterNode(new Token(Type.CHARACTER, " ")));
         whitespaces.add(new CharacterNode(new Token(Type.CHARACTER, "\t")));
         whitespaces.add(new CharacterNode(new Token(Type.CHARACTER, "\n")));
         whitespaces.add(new CharacterNode(new Token(Type.CHARACTER, "\f")));
         whitespaces.add(new CharacterNode(new Token(Type.CHARACTER, "\r")));
         for (CharacterNode ws : whitespaces) {
             ANY_WHITESPACE_CLASS_NODE.addChild(ws);
             ANY_NON_WHITESPACE_CLASS_NODE.addChild(ws);
         }
 
         CharacterClassIntervalNode lowerCaseInterval = new CharacterClassIntervalNode(
                 new CharacterNode(new Token(Type.CHARACTER, "a")),
                 new CharacterNode(new Token(Type.CHARACTER, "z")));
         CharacterClassIntervalNode upperCaseInterval = new CharacterClassIntervalNode(
                 new CharacterNode(new Token(Type.CHARACTER, "a")),
                 new CharacterNode(new Token(Type.CHARACTER, "z")));
         ANY_WORD_CHARACTER_CLASS_NODE.addChild(lowerCaseInterval);
         ANY_WORD_CHARACTER_CLASS_NODE.addChild(upperCaseInterval);
         ANY_WORD_CHARACTER_CLASS_NODE.addChild(digitsInterval);
         ANY_WORD_CHARACTER_CLASS_NODE.addChild(new CharacterNode(new Token(Type.CHARACTER, "_")));
         ANY_NON_WORD_CHARACTER_CLASS_NODE.addChild(lowerCaseInterval);
         ANY_NON_WORD_CHARACTER_CLASS_NODE.addChild(upperCaseInterval);
         ANY_NON_WORD_CHARACTER_CLASS_NODE.addChild(digitsInterval);
         ANY_NON_WORD_CHARACTER_CLASS_NODE.addChild(new CharacterNode(new Token(Type.CHARACTER, "_")));
     }
 
     private static class ParserException extends Error {
         public ParserException(String message) {
             super(message);
         }
     }
 
     private static class UnexpectedTokenException extends ParserException {
         public UnexpectedTokenException(Token t) {
             super(String.format("Unexpected token %s", t));
         }
     }
 }
