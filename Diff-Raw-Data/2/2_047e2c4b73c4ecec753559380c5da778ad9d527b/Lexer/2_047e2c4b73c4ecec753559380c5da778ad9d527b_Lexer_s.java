 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 //
 // This class lexes a file into tokens.
 
 package me.pavlina.alco.lex;
 
 import java.io.*;
 import java.util.ArrayList;
 import me.pavlina.alco.compiler.ErrorAnnotator;
 import me.pavlina.alco.compiler.errors.*;
 
 public class Lexer implements ErrorAnnotator
 {
     ArrayList<String> lines;
     ArrayList<Token> tokens;
     StringCollector collector;
     File file;
     int col;
     int line;
     
     /**
      * Initialise the lexer. This does not lex, but it reads in the entire file,
      * so the fill will be closed after it finishes. */
     public Lexer (File file) throws java.io.IOException
     {
         String line;
         this.file = file;
         this.line = 0;
         this.col = 0;
         lines = new ArrayList<String> ();
         tokens = new ArrayList<Token> ();
         collector = new StringCollector ();
 
         // Open the file and read each line into 'lines'
         BufferedReader reader = new BufferedReader (new FileReader (file));
         while ((line = reader.readLine ()) != null) {
             lines.add (line);
         }
         reader.close ();
     }
 
     /**
      * Get the number of tokens. */
     public int length () {
         return tokens.size ();
     }
 
     /**
      * Get a token. */
     public Token get (int i) {
         return tokens.get (i);
     }
 
     /**
      * Set (rewrite) a token. USE CAREFULLY. */
     public void set (int i, Token e) {
         tokens.set (i, e);
     }
 
     /**
      * Generate tokens */
     public void lex () throws CError {
         for (line = 0; line < lines.size (); ++line) {
             for (col = 0; col < lines.get (line).length (); ++col) {
                 switch (lines.get (line).charAt (col)) {
                 case ' ':
                 case 0x0009:
                 case 0x000a:
                 case 0x000b:
                 case 0x000c:
                 case 0x000d:
                     // Whitespace
                     continue;
                 case '0':
                 case '1':
                 case '2':
                 case '3':
                 case '4':
                 case '5':
                 case '6':
                 case '7':
                 case '8':
                 case '9':
                     consumeNumber ();
                     break;
                 case '@':
                 case '_':
                 case 'A':
                 case 'B':
                 case 'C':
                 case 'D':
                 case 'E':
                 case 'F':
                 case 'G':
                 case 'H':
                 case 'I':
                 case 'J':
                 case 'K':
                 case 'L':
                 case 'M':
                 case 'N':
                 case 'O':
                 case 'P':
                 case 'Q':
                 case 'R':
                 case 'S':
                 case 'T':
                 case 'U':
                 case 'V':
                 case 'W':
                 case 'X':
                 case 'Y':
                 case 'Z':
                 case 'a':
                 case 'b':
                 case 'c':
                 case 'd':
                 case 'e':
                 case 'f':
                 case 'g':
                 case 'h':
                 case 'i':
                 case 'j':
                 case 'k':
                 case 'l':
                 case 'm':
                 case 'n':
                 case 'o':
                 case 'p':
                 case 'q':
                 case 'r':
                 case 's':
                 case 't':
                 case 'u':
                 case 'v':
                 case 'w':
                 case 'x':
                 case 'y':
                 case 'z':
                     consumeWord ();
                     break;
                 case '$':
                     consumeExtrastandard ();
                     break;
                 case '/':
                     consumeOperOrComment ();
                     break;
                 case '+':
                 case '-':
                 case '~':
                 case '*':
                 case '%':
                 case '<':
                 case '>':
                 case '&':
                 case '^':
                 case '|':
                 case '!':
                 case '=':
                 case '(':
                 case ')':
                 case '[':
                 case ']':
                 case '{':
                 case '}':
                 case ',':
                 case ';':
                 case ':':
                 case '.':
                     consumeOper ();
                     break;
                 default:
                     throw new UnexpectedChar
                         (lines.get (line).charAt (col), line, col, this);
                 }
             }
         }
     }
 
     private void consumeNumber () throws CError {
         boolean breakFor = false;
         int radix = 100;
         boolean radixAllowed = false;
         boolean dotAllowed = true;
         boolean expAllowed = true;
         int firstCol = col;
         int type = Token.INT;
         int expCol = -2; // Must be at most -2
         for (; col < lines.get (line).length (); ++col) {
             char ch = lines.get (line).charAt (col);
             switch (ch) {
             case ' ':
             case 0x0009:
             case 0x000a:
             case 0x000b:
             case 0x000c:
             case 0x000d:
             case '/':
             case '~':
             case '*':
             case '%':
             case '<':
             case '>':
             case '&':
             case '^':
             case '|':
             case '!':
             case '=':
             case '(':
             case ')':
             case '[':
             case ']':
             case '{':
             case '}':
             case ',':
                 // Stop character. Move col back and break out of the loop
                 --col;
                 breakFor = true;
                 break;
             case '+':
             case '-':
                 // Stop character or exponent sign.
                 if (col == expCol + 1) {
                     collector.append (ch);
                 } else {
                     --col;
                     breakFor = true;
                 }
                 break;
             case '0':
                 if (col == firstCol) {
                     // Starting a number with 0 allows a radix to be specified.
                     radixAllowed = true;
                 }
                 collector.append (ch);
                 break;
             case '1':
                 collector.append (ch);
                 break;
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
                 // These characters are allowed in octal and above
                 if (radix < 8) {
                     throw new UnexpectedChar
                         (ch, line, col, this,
                          "in base " + radix + " number");
                 }
                 collector.append (ch);
                 break;
             case '8':
             case '9':
                 // These characters are allowed in decimal and above
                 if (radix < 10) {
                     throw new UnexpectedChar
                         (ch, line, col, this,
                          "in base " + radix + " number");
                 }
                 collector.append (ch);
                 break;
             case 'A':
             case 'C':
             case 'F':
             case 'a':
             case 'c':
             case 'f':
                 // These characters are allowed in hexadecimal
                 if (radix < 16) {
                     throw new UnexpectedChar
                         (ch, line, col, this,
                          "in base " + radix + " number");
                 }
                 collector.append (ch);
                 break;
             case 'B':
             case 'b':
                 // This is a hex 0xB or a binary radix specifier
                 if (col == firstCol + 1 && radixAllowed) {
                     expAllowed = false;
                     dotAllowed = false;
                     radix = 2;
                 } else if (radix < 16)
                     throw new UnexpectedChar
                         (ch, line, col, this,
                          "in base " + radix + " number");
                 collector.append (ch);
                 break;
             case 'D':
             case 'd':
                 // This is a hex 0xD or a decimal radix specifier
                 if (col == firstCol + 1 && radixAllowed) {
                     expAllowed = false;
                     dotAllowed = false;
                     radix = 10;
                 } else if (radix < 16)
                     throw new UnexpectedChar
                         (ch, line, col, this,
                          "in base " + radix + " number");
                 collector.append (ch);
                 break;
             case 'O':
             case 'o':
                 if (col == firstCol + 1 && radixAllowed) {
                     expAllowed = false;
                     dotAllowed = false;
                     radix = 8;
                 } else
                     throw new UnexpectedChar (ch, line, col, this);
                 collector.append (ch);
                 break;
             case 'X':
             case 'x':
                 if (col == firstCol + 1 && radixAllowed) {
                     expAllowed = false;
                     dotAllowed = false;
                     radix = 16;
                 } else
                     throw new UnexpectedChar (ch, line, col, this);
                 collector.append (ch);
                 break;
             case 'E':
             case 'e':
                 // This is an exponent specifier or a hex 0xE
                 if (expAllowed) {
                     expAllowed = false;
                     dotAllowed = false;
                     type = Token.REAL;
                     expCol = col;
                 } else if (radix < 16)
                     throw new UnexpectedChar
                         (ch, line, col, this,
                          "in base " + radix + " number");
                 collector.append (ch);
                 break;
             case '.':
                 if (!dotAllowed)
                     throw new UnexpectedChar (ch, line, col, this);
                 type = Token.REAL;
                 dotAllowed = false;
                 collector.append (ch);
                 break;
             default:
                 --col;
                 breakFor = true;
                 break;
             }
             if (breakFor) break;
         }
         String val = collector.toString ();
         collector.clear ();
         tokens.add (new Token (type, val, line, col, this));
     }
 
     private void consumeWord () throws CError {
         boolean breakFor = false;
         boolean hasAt = false;
         int firstCol = col;
         for (; col < lines.get (line).length (); ++col) {
             char ch = lines.get (line).charAt (col);
             switch (ch) {
             case ' ':
             case 0x0009:
             case 0x000a:
             case 0x000b:
             case 0x000c:
             case 0x000d:
             case '/':
             case '~':
             case '*':
             case '%':
             case '<':
             case '>':
             case '&':
             case '^':
             case '|':
             case '!':
             case '=':
             case '(':
             case ')':
             case '[':
             case ']':
             case '{':
             case '}':
             case ',':
             case '+':
             case '-':
                 // Stop character. Move col back and break out of the loop
                 --col;
                 breakFor = true;
                 break;
             case '@':
                 // Only valid as the first character
                 if (col != firstCol)
                     throw new UnexpectedChar (ch, line, col, this);
                 hasAt = true;
                 collector.append (ch);
                 break;
             case '0':
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
             case '8':
             case '9':
                 // Not valid as the first character. Don't worry about the
                 // hasAt == false case, because if the first character was a digit
                 // this wouldn't be picked up as a word anyway
                 if (hasAt) {
                     if (col == firstCol + 1)
                         throw new UnexpectedChar (ch, line, col, this);
                 }
                 collector.append (ch);
                 break;
             case '_':
             case 'A':
             case 'B':
             case 'C':
             case 'D':
             case 'E':
             case 'F':
             case 'G':
             case 'H':
             case 'I':
             case 'J':
             case 'K':
             case 'L':
             case 'M':
             case 'N':
             case 'O':
             case 'P':
             case 'Q':
             case 'R':
             case 'S':
             case 'T':
             case 'U':
             case 'V':
             case 'W':
             case 'X':
             case 'Y':
             case 'Z':
             case 'a':
             case 'b':
             case 'c':
             case 'd':
             case 'e':
             case 'f':
             case 'g':
             case 'h':
             case 'i':
             case 'j':
             case 'k':
             case 'l':
             case 'm':
             case 'n':
             case 'o':
             case 'p':
             case 'q':
             case 'r':
             case 's':
             case 't':
             case 'u':
             case 'v':
             case 'w':
             case 'x':
             case 'y':
             case 'z':
                 collector.append (ch);
                 break;
             default:
                 --col;
                 breakFor = true;
                 break;
             }
             if (breakFor) break;
         }
         String val = collector.toString ();
         collector.clear ();
         tokens.add (new Token (Token.WORD, val, line, firstCol, this));
     }
 
     private void consumeExtrastandard () throws CError {
         boolean breakFor = false;
         boolean hasAt = false;
         int firstCol = col;
         for (; col < lines.get (line).length (); ++col) {
             // The first two characters must be $$
             char ch = lines.get (line).charAt (col);
             if (col == firstCol || col == (firstCol + 1)) {
                 if (ch != '$')
                     throw new UnexpectedChar
                         (ch, line, col, this,
                          "extrastandard identifier must start with $$");
                 collector.append (ch);
                 continue;
             }
             switch (ch) {
             case ' ':
             case 0x0009:
             case 0x000a:
             case 0x000b:
             case 0x000c:
             case 0x000d:
             case '/':
             case '~':
             case '*':
             case '%':
             case '<':
             case '>':
             case '&':
             case '^':
             case '|':
             case '!':
             case '=':
             case '(':
             case ')':
             case '[':
             case ']':
             case '{':
             case '}':
             case ',':
             case '+':
             case '-':
                 // Stop character. Move col back and break out of the loop
                 --col;
                 breakFor = true;
                 break;
             case '0':
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
             case '8':
             case '9':
                 // Not valid as the first character after $$
                 if (col == firstCol + 2)
                     throw new UnexpectedChar (ch, line, col, this);
                 collector.append (ch);
                 break;
             case '_':
             case 'A':
             case 'B':
             case 'C':
             case 'D':
             case 'E':
             case 'F':
             case 'G':
             case 'H':
             case 'I':
             case 'J':
             case 'K':
             case 'L':
             case 'M':
             case 'N':
             case 'O':
             case 'P':
             case 'Q':
             case 'R':
             case 'S':
             case 'T':
             case 'U':
             case 'V':
             case 'W':
             case 'X':
             case 'Y':
             case 'Z':
             case 'a':
             case 'b':
             case 'c':
             case 'd':
             case 'e':
             case 'f':
             case 'g':
             case 'h':
             case 'i':
             case 'j':
             case 'k':
             case 'l':
             case 'm':
             case 'n':
             case 'o':
             case 'p':
             case 'q':
             case 'r':
             case 's':
             case 't':
             case 'u':
             case 'v':
             case 'w':
             case 'x':
             case 'y':
             case 'z':
                 collector.append (ch);
                 break;
             default:
                 --col;
                 breakFor = true;
                 break;
             }
             if (breakFor) break;
         }
         String val = collector.toString ();
         collector.clear ();
         tokens.add (new Token (Token.EXTRA, val, line, firstCol, this));
     }
 
     private void consumeOperOrComment () throws CError {
         if (lines.get (line).regionMatches (col, "//", 0, 2)) {
             // Line comment - just skip the rest of the line
             ++line;
             col = 0;
         } else if (lines.get (line).regionMatches (col, "/*", 0, 2)) {
             // Block comment
             col += 2;
             consumeBlockComment ();
         } else
             consumeOper ();
     }
 
     private void consumeBlockComment () throws CError {
         // Run to the ending */
         // Recurse for nesting
         // If we hit the end of the file, complain
         for (; line < lines.size (); ++line) {
             // Initialise col=0 after this loop so we can start at a nonzero col
             for (; col < lines.get (line).length (); ++col) {
                 if (lines.get (line).regionMatches (col, "/*", 0, 2)) {
                     // Nested comment
                     col += 2;
                     consumeBlockComment ();
                 } else if (lines.get (line).regionMatches (col, "*/", 0, 2)) {
                     col += 2;
                     return;
                 }
             }
             col = 0;
         }
         throw new UnexpectedEOF
             ("*/", lines.size () - 1,
              lines.get (lines.size () - 1).length () - 1, 0, 0, this);
     }
 
     private void consumeOper () throws CError {
         char ch1 = lines.get (line).charAt (col);
         char ch2;
         char ch3;
         String oper = null;
         if (col + 1 < lines.get (line).length ())
             ch2 = lines.get (line).charAt (col + 1);
         else
             ch2 = 0;
         if (col + 2 < lines.get (line).length ())
             ch3 = lines.get (line).charAt (col + 2);
         else
             ch3 = 0;
 
         switch (ch1) {
         case '/':
             switch (ch2) {
             case '=': oper = "/="; break;
             default:  oper = "/";
             } break;
         case '+':
             switch (ch2) {
             case '+': oper = "++"; break;
             case '=': oper = "+="; break;
             default:  oper = "+";
             } break;
         case '-':
             switch (ch2) {
             case '-': oper = "--"; break;
             case '=': oper = "-="; break;
            default:  oper = "=";
             } break;
         case '~':
             oper = "~"; break;
         case '*':
             switch (ch2) {
             case '=': oper = "*="; break;
             default:  oper = "*";
             } break;
         case '%':
             switch (ch2) {
             case '%':
                 switch (ch3) {
                 case '=': oper = "%%="; break;
                 default:  oper = "%%";
                 } break;
             case '=': oper = "%="; break;
             default:  oper = "%";
             } break;
         case '<':
             switch (ch2) {
             case '<':
                 switch (ch3) {
                 case '=': oper = "<<="; break;
                 default:  oper = "<<"; break;
                 } break;
             case '=': oper = "<="; break;
             default:  oper = "<";
             } break;
         case '>':
             switch (ch2) {
             case '>':
                 switch (ch3) {
                 case '=': oper = ">>="; break;
                 default:  oper = ">>"; break;
                 } break;
             case '=': oper = "<="; break;
             default:  oper = "<";
             } break;
         case '&':
             switch (ch2) {
             case '&': oper = "&&"; break;
             case '=': oper = "&="; break;
             default:  oper = "&";
             } break;
         case '^':
             switch (ch2) {
             case '=': oper = "^="; break;
             default:  oper = "^";
             } break;
         case '|':
             switch (ch2) {
             case '|': oper = "||"; break;
             case '=': oper = "|="; break;
             default:  oper = "|";
             } break;
         case '!':
             switch (ch2) {
             case '=': oper = "!="; break;
             default:  oper = "!"; break;
             } break;
         case '=':
             switch (ch2) {
             case '=': oper = "=="; break;
             default:  oper = "=";
             } break;
         case '(':
             oper = "("; break;
         case ')':
             oper = ")"; break;
         case '[':
             oper = "["; break;
         case ']':
             oper = "]"; break;
         case '{':
             oper = "{"; break;
         case '}':
             oper = "}"; break;
         case ',':
             oper = ","; break;
         case ':':
             oper = ":"; break;
         case ';':
             oper = ";"; break;
         case '.':
             if (ch2 == '.' && ch3 == '.') {
                 oper = "..."; break;
             } else {
                 oper = "."; break;
             }
         }
 
         if (oper == null)
             throw new UnexpectedChar (ch1, line, col, this, " in operator");
         tokens.add (new Token (Token.OPER, oper, line, col, this));
         col += oper.length () - 1;
     }
 
     public void annotate (int line, int col, int start, int stop,
                           PrintStream out)
     {
         String L = lines.get (line);
         out.println (L);
         for (int i = 0; i < L.length (); ++i) {
             if (i == col)
                 out.print ("^");
             else if (i >= start && i < stop && L.charAt (i) != '\t')
                 out.print ("~");
             else if (L.charAt (i) == '\t')
                 out.print ("\t");
             else
                 out.print (" ");
         }
         out.print ('\n');
     }
 
     public String filename ()
     {
         return file.getName ();
     }
 }
