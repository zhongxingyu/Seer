 /**
  * 
  */
 package compiler.fsa;
 
 import compiler.Token;
 import compiler.TokenType;
 import compiler.io.MPFile;
 
 /**
  * 
  *
  */
 public class CommentFSA implements FSA {
 
     @Override
     public Token getToken(MPFile file) {
         Token t = null;
         int state = 1;
         int startingLine = file.getLineIndex();
         int startingCol = file.getCurrentLine().getColumnIndex();
         String lexeme = "";
         char current = 0;
 
         while (state != 3) {
 
             /*
              * if the file does not have any more characters but it is in an
              * accept state exit FSA, in case the accept state can optionally
              * accept more characters
              */
             if (!file.hasNextChar(true) && state == 3) {
                 state = 3;
             } else {
 
                 switch (state) {
                 case 1:
                     current = file.getNextCharacter(true);
                     if (current == '{') // always true
                     {
                         state = 2;
                     }
                     break;
                 case 2:
                     if (file.hasNextChar(true)) {
                         current = file.getCurrentCharacter();
                         if (current == '}') {
                             file.getNextCharacter(true);
                             state = 3;
                         } else if (current == '{') {
                            System.out.println("WARNING: { found inside comment, double check comments. Found at line "
                                    + file.getLineIndex() + " column " + file.getColumnIndex());
                             lexeme += file.getNextCharacter(true);
                         } else {
                             lexeme += file.getNextCharacter(true);
                         }
                     } else // no characters left in file
                     {
                         return new Token(TokenType.MP_RUN_COMMENT, lexeme,
                                 startingLine, startingCol);
                     }
                     break;
                 }
             }
 
         }
         t = new Token(TokenType.MP_COMMENT, lexeme, startingLine, startingCol);
         return t;
     }
 
 }
