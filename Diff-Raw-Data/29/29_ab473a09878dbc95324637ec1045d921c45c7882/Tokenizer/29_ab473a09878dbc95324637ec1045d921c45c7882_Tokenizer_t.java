 package rocnikovaprace;
 
 import java.util.ArrayList;
 
 public class Tokenizer {
 
    public static ArrayList<Token> tokenize(String s, String tab, boolean multiline, TokenType lastType) {
       if (tab.equals("\t")) {
          s = s.replace("\t", "    ");
       }
       ArrayList<Token> tokens = new ArrayList<Token>();
       for (int i = 0; i < s.length(); i++) {
          StringBuilder sb = new StringBuilder();
          Token token = new Token();
          if (s.equals("")) {
             token.string = "\n";
             token.type = TokenType.EOL;
             break;
          } else if (multiline) {
             while (i != s.length() - 1) {
                if (lastType.equals(TokenType.MULTICOMMENT)
                        && s.charAt(i) == '*' && s.charAt(i + 1) == '/') {
                   sb.append("*");
                   i++;
                   break;
 
                } else if (lastType.equals(TokenType.STRING)
                        && s.charAt(i) == '"' || s.charAt(i) == '\'') {
                   break;
                }
                sb.append(s.charAt(i));
                i++;
             }
             sb.append(s.charAt(i));
             token.string = sb.toString();
             token.type = lastType;
             multiline = false;
          } else {
             switch (s.charAt(i)) {
                case '"':
                case '\'':
                   char c = s.charAt(i);
                   sb.append(c);
                   i++;
                  if (s.charAt(i) == c) { //Empty string?
                      sb.append(c);
                  } else { // Else go to the end, ignoring escaped quotes
                     while (i != s.length() - 1 && (s.charAt(i) == '\\' || s.charAt(i + 1) != c)) {
                        sb.append(s.charAt(i));
                        i++;
                     }
                     sb.append(s.charAt(i)).append(c);
                     i++;
                   }
                   token.string = sb.toString();
                   token.type = TokenType.STRING;
                   break;
 
                case '}':
                case '{':
                case ';':
                   token.string = Character.toString(s.charAt(i));
                   token.type = TokenType.EOL;
                   break;
 
                case '(':
                case ')':
                   token.string = Character.toString(s.charAt(i));
                   token.type = TokenType.BRACKET;
                   break;
 
                case ' ':
                   break;
 
                case '.':
                   token.string = ".";
                   token.type = TokenType.DOT;
                   break;

                case '<':
                  if (Character.toString(s.charAt(i + 1)).matches("[A-Z]")) {
                      sb.append("<").append(s.charAt(i + 1));
                      i += 2;
                     while (i < s.length() - 1 && s.charAt(i) != '>') {
                         sb.append(s.charAt(i));
                         i++;
                      }
                      sb.append(">");
                      token.string = sb.toString();
                      token.type = TokenType.CLASS;
                      break;
                   }

                case '/':
                   if (i != s.length() - 1 && s.charAt(i + 1) == '/') {
                      token.string = s.substring(i);
                      token.type = TokenType.COMMENT;
                      i = s.length() - 1;
                      break;
 
                   } else if (i != s.length() - 1 && s.charAt(i + 1) == '*') {
                      sb.append("/*");
                      i += 2;
                      while (i < s.length() - 1) {
                         if (s.charAt(i) == '*' && s.charAt(i + 1) == '/') {
                            sb.append("*/");
                            i++;
                            break;
                         }
                         sb.append(s.charAt(i));
                         i++;
                      }
                      if (!sb.toString().endsWith("*/")) {
                         sb.append(s.charAt(i));
                      }
                      token.string = sb.toString();
                      token.type = TokenType.MULTICOMMENT;
                      break;
                   }
 
                default:
                   if (Character.isDigit(s.charAt(i))) {
                      while (i != s.length() - 1 && (Character.isDigit(s.charAt(i + 1))
                              || Character.isLetter(s.charAt(i + 1)) || s.charAt(i + 1) == '.')) {
                         sb.append(s.charAt(i));
                         i++;
                      }
                      sb.append(s.charAt(i));
                      token.string = sb.toString();
                      token.type = TokenType.CONSTANT;
 
                   } else {
                      // Works atm, but it's really ugly and needs some optimizing.
                      char secondChar = (i != s.length() - 1) ? s.charAt(i + 1) : 'x'; // Just a char that's not an operator(fugly, I know)
                      int operatorLength = Data.getOperatorLength(s.charAt(i), secondChar);
                      if (operatorLength == 2) {
                         i++;
                      }
 
                      switch (operatorLength) {
                         case 0:
                            break;
                         case 1:
                         case 2:
                            token.type = TokenType.OPERATOR;
                            token.string = (operatorLength == 2) ? Character.toString(s.charAt(i - 1)) + Character.toString(s.charAt(i)) : Character.toString(s.charAt(i));
                      }
                      if (operatorLength == 1 || operatorLength == 2) {
                         break;
                      }
 
                      while (i != s.length() - 1 && s.charAt(i) != ' ') {
 
                         if (Data.isOperator(s.charAt(i + 1))
                                 || s.charAt(i + 1) == '\"'
                                 || s.charAt(i + 1) == '}'
                                 || s.charAt(i + 1) == '{'
                                 || s.charAt(i + 1) == ';'
                                 || s.charAt(i + 1) == '.') {
                            break;
                         }
 
                         sb.append(s.charAt(i));
                         i++;
                      }
                      if (s.charAt(i) != ' ') {
                         sb.append(s.charAt(i));
                      }
                      token.string = sb.toString();
                      token.type = (Data.isKeyword(token.string)) ? TokenType.KEYWORD : TokenType.CODE;
                   }
             }
          }
          if (token.string != null) {
             tokens.add(token);
          }
       }
       return tokens;
    }
 }
