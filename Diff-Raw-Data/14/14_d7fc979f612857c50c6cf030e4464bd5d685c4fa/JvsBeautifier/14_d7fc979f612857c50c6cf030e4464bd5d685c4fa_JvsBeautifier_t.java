 /*******************************************************************************
 * Thierry.Vieville@sophia.inria.fr, Copyright (C) 2009.  All rights reserved. *
 *******************************************************************************/
 
 package org.javascool.core;
 
 /** Normalise la mise en page d'un source Jvs. */
 public class JvsBeautifier {
   /** Reformate un morceau de code Jvs.
    * <p>- C'est un mécanisme restritif un peu fragile destiné au sous-langage Jvs de Java.</p>
    * @param text Le source à reformate.
    * @return Le source reformaté.
    */
   public static String run(String text) {
     char f[] = text.trim().replace((char) 160, ' ').toCharArray();
     String g = "", ln = "\n";
     int par = 0;
     for(int i = 0, j; i < f.length;) {
       // Escapes /* comments
       if((f[i] == '/') && (i < f.length - 1) && (f[i + 1] == '*')) {
         g += f[i++];
         while(i < f.length && !(f[i - 1] == '*' && f[i] == '/'))
           g += f[i++];
         if(i < f.length)
           g += f[i++] + ln;
         // Escapes // comments
       } else if((f[i] == '/') && (i < f.length - 1) && (f[i + 1] == '/')) {
         while(i < f.length && f[i] != '\n')
           g += f[i++];
         g += ln;
         i++;
         // Escapes " chars
       } else if(f[i] == '"') {
         g += f[i++];
         while(i < f.length && (f[i - 1] == '\\' || f[i] != '"') && f[i] != '\n')
           g += f[i++];
         if(i < f.length)
           g += f[i++];
         // Escapes @ pragma
       } else if(f[i] == '@') {
         while(i < f.length && f[i] != '\n')
           g += f[i++];
         g += ln;
         i++;
         // Normalizes spaces
       } else if(Character.isWhitespace(f[i])) {
         g += ' ';
         i++;
         while(i < f.length && Character.isWhitespace(f[i]))
           i++;
       } else {
         char c0 = g.length() == 0 ? ' ' : g.charAt(g.length() - 1);
         // Counts (parenthesies)
         if(f[i] == '(')
           par++;
         if(f[i] == ')')
           par--;
         // Normalize spaces around operators
         if(isOperator(f[i])) {
           if(!(Character.isWhitespace(c0) || isOperator(c0)))
             g += ' ';
           g += f[i];
           if((i < f.length - 1) && !(Character.isWhitespace(f[i + 1]) || isOperator(f[i + 1])))
             g += ' ';
         } else if(f[i] == '.') {
          if(g.length() > 0 && Character.isWhitespace(c0))
             g = g.substring(0, g.length() - 1);
           g += f[i];
           while(i < f.length - 1 && Character.isWhitespace(f[i + 1]))
             i++;
           // Normalize spaces around punctuation
         } else if((f[i] == ',') || (f[i] == ';') || (f[i] == ')')) {
          if(g.length() > 0 && Character.isWhitespace(c0))
             g = g.substring(0, g.length() - 1);
           g += f[i];
           if((par > 0) && (f[i] != ')'))
             if((i < f.length - 1) && !Character.isWhitespace(f[i + 1]))
               g += ' ';
           if((f[i] == ')') && (i < f.length - 1) && (f[i + 1] == '{'))
             g += ' ';
         } else if(f[i] == '(') {
          if(g.length() > 0 && Character.isWhitespace(c0) && (g.length() > 1) && Character.isLetterOrDigit(g.charAt(g.length() - 2)))
             g = g.substring(0, g.length() - 1);
           g += f[i];
           while(i < f.length - 1 && Character.isWhitespace(f[i + 1]))
             i++;
         } else if(f[i] == '}') {
           for(int n = 0; n < 3; n++)
            if(g.length() > 0 && Character.isWhitespace(g.charAt(g.length() - 1)))
               g = g.substring(0, g.length() - 1);
           g += f[i];
         } else
           g += f[i];
         // Reformats {blocks}
         if((f[i] == '{') || (f[i] == '}') || ((f[i] == ';') && (par == 0))) {
           if(f[i] == '{')
             ln += "   ";
          if(ln.length() >= 3 && f[i] == '}')
             ln = ln.substring(0, ln.length() - 3);
           g += ln;
           if(ln.length() == 1)
             g += "\n";
           i++;
           while(i < f.length && Character.isWhitespace(f[i]))
             i++;
         } else
           i++;
       }
     }
     return "\n" + g.
            replaceAll("\\}\\s*else\\s*(\\{|if)", "} else $1").
            replaceAll("(while|if|for|return)\\s*([^a-z_0-9_])", "$1 $2");
   }
   private static boolean isOperator(char c) {
     switch(c) {
     case '+':
     case '-':
     case '*':
     case '/':
     case '%':
     case '&':
     case '|':
     case '^':
     case '=':
     case '!':
     case '<':
     case '>':
     case ':':
       return true;
     default:
       return false;
     }
   }
 }
