 package com.wickedspiral.jacss.lexer;
 
 /**
  * @author wasche
  * @since 2011.08.04
  */
 public enum Token
 {
     IDENTIFIER,
     NUMBER,
     STRING,
     COMMENT,
     WHITESPACE,
     OP,
     HASH,
     PERCENT,
     LPAREN,
     RPAREN,
     EQUALS,
     GT,
     AT,
     COLON,
     SEMICOLON,
     LBRACE,
     RBRACE,
     LBRACKET,
     RBRACKET,
     PLUS,
     STAR,
     PERIOD,
     COMMA,
     TILDAE,
     PIPE,
     BACKSLASH,
     DOLLAR,
     CARROT,
    BANG,
    QUESTION
     ;
 
     public static Token getByCharacter(char c)
     {
         switch (c)
         {
             case '#':
                 return HASH;
             case '%':
                 return PERCENT;
             case '(':
                 return LPAREN;
             case ')':
                 return RPAREN;
             case '=':
                 return EQUALS;
             case '>':
                 return GT;
             case '@':
                 return AT;
             case ':':
                 return COLON;
             case ';':
                 return SEMICOLON;
             case '{':
                 return LBRACE;
             case '}':
                 return RBRACE;
             case '[':
                 return LBRACKET;
             case ']':
                 return RBRACKET;
             case '+':
                 return PLUS;
             case '*':
                 return STAR;
             case '.':
                 return PERIOD;
             case ',':
                 return COMMA;
             case '~':
                 return TILDAE;
             case '|':
                 return PIPE;
             case '\\':
                 return BACKSLASH;
             case '$':
                 return DOLLAR;
             case '^':
                 return CARROT;
             case '!':
                 return BANG;
            case '?':
                return QUESTION;
             case ' ':
                 return WHITESPACE;
             default:
                 System.err.println("Couldn't find token for character: '" + c + "'");
                 return null;
         }
     }
 }
