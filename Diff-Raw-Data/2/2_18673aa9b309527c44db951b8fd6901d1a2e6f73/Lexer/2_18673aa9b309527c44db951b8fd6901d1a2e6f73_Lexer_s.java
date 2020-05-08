 package xhl.core;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import xhl.core.Token.TokenType;
 import xhl.core.elements.Position;
 
 import com.google.common.collect.ImmutableMap;
 
 import static xhl.core.Token.TokenType.*;
 
 import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
 
 /**
  * Lexical analyzer for XHL
  *
  * @author Sergej Chodarev
  */
 class Lexer {
     // Regular expressions and maps for tokens
     private static final Pattern numberRx = Pattern.compile("-?\\d+(\\.\\d*)?");
     private static final Pattern operatorRx = Pattern.compile("[-+*/_=<>?!:&|]+");
     private static final Pattern symbolRx = Pattern.compile("[a-zA-Z]\\w*");
     private static final ImmutableMap<Character, TokenType> simpleTokens =
             new ImmutableMap.Builder<Character, TokenType>()
                     .put('(', PAR_OPEN)
                     .put(')', PAR_CLOSE)
                     .put('[', BRACKET_OPEN)
                     .put(']', BRACKET_CLOSE)
                     .put('{', BRACE_OPEN)
                     .put('}', BRACE_CLOSE)
                     .put(',', COMMA)
                     .put('.', DOT)
                     .build();
     private static final String OPEN = "([{";
     private static final String CLOSE = "}])";
 
     // Input stream
     private final BufferedReader input;
     // Currently processed line
     private String line;
     // Cursor position
     private int lineN = 0;
     private int columnN = 0;
 
     // Lexer state
     private final Stack<Integer> indent = new Stack<Integer>();
     private int braceLevel = 0;
 
     // List of tokens
     private final List<Token> tokens = newArrayListWithExpectedSize(120);
     private final ListIterator<Token> tokensIterator;
 
     /**
      * Initialize lexical analyzer and analyze text in input stream.
      *
      * @param input
      *            Input stream.
      * @throws IOException
      */
     public Lexer(Reader input) throws IOException {
         this.input = new BufferedReader(input);
         indent.push(0);
         readTokens();
         tokensIterator = tokens.listIterator();
     }
 
     /**
      * Get next token.
      *
      * @return Next token or <code>null</code> if end of file was reached.
      */
     public Token nextToken() {
         if (tokensIterator.hasNext())
             return tokensIterator.next();
         else
             return null;
     }
 
     /** Get next token without removing it from the list. */
     public Token checkNextToken() {
         if (tokensIterator.hasNext())
             return tokens.get(tokensIterator.nextIndex());
         else
             return null;
     }
 
     /**
      * Read all tokens from input stream.
      */
     private void readTokens() throws IOException {
         line = input.readLine();
         while (line != null) {
             lineN++;
             columnN = 0;
 
             // Read indentation
             int indentation = readIndentation();
             // Skip empty line
             if (endOfLine()) {
                 line = input.readLine();
                 continue;
             }
            // Add INDEND or DEDENT tokens
             if (braceLevel == 0)
                 processIndentation(indentation);
             // Read tokens on the line
             while (!endOfLine()) {
                 tokens.add(readToken());
                 skipSpace();
             }
             // End of line
             if (braceLevel == 0)
                 tokens.add(new Token(LINEEND, getPosition()));
             // Next line
             line = input.readLine();
         }
         // DEDENT on the ond of file
         while (indent.peek() > 0) {
             indent.pop();
             tokens.add(new Token(DEDENT, getPosition()));
         }
     }
 
     /**
      * Generate INDENT or DEDENT tokens for indentation change.
      *
      * @param indentation
      *            Level of indentation.
      */
     private void processIndentation(int indentation) {
         if (indentation > indent.peek()) {
             indent.push(indentation);
             tokens.add(new Token(INDENT, getPosition()));
         } else if (indentation < indent.peek()) {
             while (indentation < indent.peek()) {
                 indent.pop();
                 tokens.add(new Token(DEDENT, getPosition()));
             }
             // TODO indentation error if levels does not match
         }
     }
 
     /**
      * Is and of line reached?
      *
      * Comment also ends line.
      */
     private boolean endOfLine() {
         return (columnN == line.length() || line.charAt(columnN) == '#' || line
                 .charAt(columnN) == ';'); // ; only for backwards compatibility
     }
 
     /**
      * Read spaces and count the amount of indentation.
      *
      * @return indentation level.
      */
     private int readIndentation() {
         char c;
         int indentation = 0;
         while (!endOfLine()) {
             c = line.charAt(columnN);
             switch (c) {
             case ' ':
                 indentation++;
                 break;
             case '\t':
                 indentation += 8;
                 indentation -= indentation % 8;
                 break;
             default:
                 return indentation;
             }
             columnN++;
         }
         return indentation;
     }
 
     private void skipSpace() {
         while (!endOfLine() && (" \t".indexOf(line.charAt(columnN)) != -1))
             columnN++;
     }
 
     /**
      * Try to match regular expression at the current cursor position.
      *
      * @param rx
      *            Regular expression
      * @return Matched text or <code>null</code> if regular expression did not
      *         match.
      */
     private String matchRx(Pattern rx) {
         Matcher m = rx.matcher(line.substring(columnN));
         if (m.lookingAt()) {
             columnN += m.end();
             return m.group();
         } else
             return null;
     }
 
     /**
      * Read one token at current position.
      */
     private Token readToken() {
         if (line.charAt(columnN) == '"')
             return readString();
 
         char ch = line.charAt(columnN);
         // Punctuation
         if (simpleTokens.containsKey(ch)) {
             TokenType type = simpleTokens.get(ch);
             if (OPEN.indexOf(ch) != -1)
                 braceLevel++;
             else if (CLOSE.indexOf(ch) != -1)
                 braceLevel--;
             columnN++;
             return new Token(type, getPosition());
         }
 
         String text = matchRx(numberRx);
         if (text != null)
             return new Token(NUMBER, Double.valueOf(text), getPosition());
 
         text = matchRx(operatorRx);
         if (text != null)
             return new Token(OPERATOR, text, getPosition());
 
         text = matchRx(symbolRx);
         if (text != null) {
             if (text.equals("true"))
                 return new Token(TRUE, getPosition());
             if (text.equals("false"))
                 return new Token(FALSE, getPosition());
             if (text.equals("none"))
                 return new Token(NONE, getPosition());
             else
                 return new Token(SYMBOL, text, getPosition());
         }
         return null;
     }
 
     /** Read string token. */
     private Token readString() {
         StringBuilder sb = new StringBuilder();
         char ch;
         columnN++; // "
         while (columnN < line.length()) {
             ch = line.charAt(columnN);
             if (ch == '"')
                 break;
             sb.append(ch);
             columnN++;
         }
         // TODO: Error on unclosed string
         columnN++; // "
         return new Token(STRING, sb.toString(), getPosition());
     }
 
     private Position getPosition() {
         return new Position("input", lineN, columnN); // FIXME filename
     }
 
 }
