 package miniJava.SyntacticAnalyzer;
 
 import miniJava.SyntacticAnalyzer.SourceFile;
 import miniJava.SyntacticAnalyzer.Token;
 
 // maybe I'll use ErrorReporter
 
 public final class Scanner {
 
     private SourceFile sourceFile;
 
     private char currentChar;
     private StringBuffer currentSpelling;
     private boolean currentlyScanningToken;
 
     public Scanner(SourceFile source) {
         sourceFile = source;
         currentChar = sourceFile.getSource();
     }
 
     // takeIt appends the current character to the current token, and gets
     // the next character from the source program.
     private void takeIt() {
         if(currentlyScanningToken) {
             currentSpelling.append(currentChar);
         }
         currentChar = sourceFile.getSource();
     }
 
     private int scanToken() {
 
         switch(currentChar) {
         case 'a':  case 'b':  case 'c':  case 'd':  case 'e':
         case 'f':  case 'g':  case 'h':  case 'i':  case 'j':
         case 'k':  case 'l':  case 'm':  case 'n':  case 'o':
         case 'p':  case 'q':  case 'r':  case 's':  case 't':
         case 'u':  case 'v':  case 'w':  case 'x':  case 'y':
         case 'z':
         case 'A':  case 'B':  case 'C':  case 'D':  case 'E':
         case 'F':  case 'G':  case 'H':  case 'I':  case 'J':
         case 'K':  case 'L':  case 'M':  case 'N':  case 'O':
         case 'P':  case 'Q':  case 'R':  case 'S':  case 'T':
         case 'U':  case 'V':  case 'W':  case 'X':  case 'Y':
         case 'Z':
             takeIt();
             while(Character.isLetter(currentChar) || 
                 Character.isDigit(currentChar) || currentChar == '_')
                 takeIt();
             return Token.IDENTIFIER;
 
         case '0':  case '1':  case '2':  case '3':  case '4':
         case '5':  case '6':  case '7':  case '8':  case '9':
             takeIt();
             while(Character.isDigit(currentChar))
                 takeIt();
             return Token.INTLITERAL;
 
         case '>':
             takeIt();
             if(currentChar == '=') {
                 takeIt();
                 return Token.GEQUAL;
             }
             return Token.GREATER;
 
         case '<':
             takeIt();
             if(currentChar == '=') {
                 takeIt();
                 return Token.LEQUAL;
             }
             return Token.LESS;
 
         case '=':
             takeIt();
             if(currentChar == '=') {
                 takeIt();
                 return Token.EQUAL;
             }
             return Token.ASSIGN;
 
         case '&':
             takeIt();
             if(currentChar != '&') {
                 System.exit(4);
             }
             takeIt();
             return Token.AND;
 
         case '|':
             takeIt();
             if(currentChar != '|')
                 System.exit(4);
             takeIt();
             return Token.OR;
 
         case '!':
             takeIt();
             if(currentChar == '=') {
                 takeIt();
                 return Token.NOTEQUAL;
             }
             return Token.NOT;
 
         case '+':
             takeIt();
             return Token.PLUS;
 
         case '-':
             takeIt();
             return Token.MINUS;
 
         case '*':
             takeIt();
             return Token.TIMES;
 
         case '/':
             takeIt();
             return Token.DIV;
 
         case '.':
             takeIt();
             return Token.DOT;
 
         case ',':
             takeIt();
             return Token.COMMA;
 
         case ';':
             takeIt();
             return Token.SEMICOLON;
 
         case '(':
             takeIt();
             return Token.LPAREN;
 
         case ')':
             takeIt();
             return Token.RPAREN;
 
         case '[':
             takeIt();
             return Token.LBRACKET;
 
         case ']':
             takeIt();
             return Token.RBRACKET;
 
         case '{':
             takeIt();
             return Token.LCURLY;
 
         case '}':
             takeIt();
             return Token.RCURLY;
 
         case SourceFile.eot:
             return Token.EOT;
 
         default:
             takeIt();
             return Token.ERROR;
 
         }
     }
 
     // scanSeparator skips a single separator.
    private void scanSeparator() {
         switch(currentChar) {
         case '/':
             takeIt();
             if(currentChar == '/')
                 singleLineComment();
             else if(currentChar == '*')
                 multiLineComment();
             break;
 
         case ' ': case '\n': case '\r': case '\t':
             takeIt();
             break;
         }
     }
 
     private void singleLineComment() {
         while(currentChar != '\n'
                 && currentChar != '\r'
                 && currentChar != SourceFile.eot)
             takeIt();
 
         takeIt();
     }
 
     private void multiLineComment() {
         currentChar = sourceFile.getSource();
         char nextChar = sourceFile.getSource();
 
         while(currentChar != '*' || nextChar != '/') {
             if(currentChar == SourceFile.eot) {
                 System.exit(4);
             }
             currentChar = nextChar;
             nextChar = sourceFile.getSource();
         }
         currentChar = sourceFile.getSource();
     }
 
     public Token scan() {
         Token tok;
         SourcePosition pos;
         int kind;
 
         currentlyScanningToken = false;
         while(currentChar == '/'
                 || currentChar == ' '
                 || currentChar == '\n'
                 || currentChar == '\r'
                || currentChar == '\t')
            scanSeparator();
 
         currentlyScanningToken = true;
         currentSpelling = new StringBuffer("");
         pos = new SourcePosition();
         pos.start = sourceFile.getCurrentLine();
 
         kind = scanToken();
 
         pos.finish = sourceFile.getCurrentLine();
         tok = new Token(kind, currentSpelling.toString(), pos);
 
         return tok;
     }
 }
