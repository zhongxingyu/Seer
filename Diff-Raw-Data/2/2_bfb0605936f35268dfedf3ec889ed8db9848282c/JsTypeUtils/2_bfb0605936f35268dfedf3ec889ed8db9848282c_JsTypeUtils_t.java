 /*
  * Copyright (C) 2012 Klaus Reimer <k@ailis.de>
  * See LICENSE.md for licensing information.
  */
 
 package de.ailis.jasdoc.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * JavaScript type utility methods.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public final class JsTypeUtils
 {
     /**
      * Private constructor to prevent instantiation.
      */
     private JsTypeUtils()
     {
         // Empty
     }
 
     /**
      * Splits an expression by the specified separator but keeps track of
      * brackets so separator characters inside of a sub type are ignored. The
      * splitted parts are also trimmed.
      *
      * @param expression
      *            The expression to split.
      * @param separator
      *            The separator character.
      * @return The splitted expression parts.
      */
     public static String[] split(final String expression, final char separator)
     {
         final List<String> parts = new ArrayList<String>();
         int start = 0;
         int level = 0;
         final int len = expression.length();
         for (int i = 0; i < len; i += 1)
         {
             final char c = expression.charAt(i);
             if (c == separator)
             {
                 if (level == 0)
                 {
                     parts.add(expression.substring(start, i).trim());
                     start = i + 1;
                 }
             }
             if (c == '{' || c == '<' || c == '[' || c == '(')
                 level += 1;
             else if (c == '}' || c == '>' || c == ']' || c == ')')
                 level -= 1;
         }
         if (start <= len) parts.add(expression.substring(start, len).trim());
         return parts.toArray(new String[parts.size()]);
     }
 
     /**
      * Searches the end index for a specific bracket.
      *
      * @param e
      *            The type expression to search in.
      * @param start
      *            The start index pointing at the opening bracket.
      * @return The end index.
      */
     public static int findEnd(final String e, final int start)
     {
         if (e.length() == 0) return -1;
         final char startChar = e.charAt(start);
         final int len = e.length();
         int level = 0;
         char endChar;
 
         switch (startChar)
         {
             case '(':
                 endChar = ')';
                 break;
             case '<':
                 endChar = '>';
                 break;
             case '{':
                 endChar = '}';
                 break;
             case '[':
                endChar = ']';
                 break;
             default:
                 return len - 1;
         }
         for (int i = start + 1; i < len; i += 1)
         {
             final char c = e.charAt(i);
             if (c == endChar)
             {
                 if (level == 0) return i;
                 level -= 1;
             }
             else if (c == startChar)
             {
                 level += 1;
             }
         }
         return len - 1;
     }
 
     /**
      * Splits the specified string into a type and the rest of the string. If
      * string is "{Object.<string, number>} test" for example then the returned
      * array contains two elements. The first element is the type expression and
      * the second element is the rest of the string ("test" in this case)
      *
      * @param s
      *            The string to split.
      * @return The string splitted into a type expression and the rest of the
      *         string. It always returns an array with two elements. All two can
      *         be empty.
      */
     public static String[] splitByType(final String s)
     {
         final String all = s.trim();
         final int pos = findEnd(all, 0);
         final String typeExpr = all.substring(0, pos + 1);
         final String rest = all.substring(pos + 1).trim();
         return new String[] { typeExpr, rest };
     }
 
     /**
      * Splits the specified string into a type, a name and the rest of the
      * string. If string is "{Object.<string, number>} test Some doc" for
      * example then the returned array contains three elements. The first
      * element is the type expression, the second element is name ("test") and
      * the rest of the string ("Some doc").
      *
      * @param s
      *            The string to split.
      * @return The string splitted into a type expression, a name and the rest
      *         of the string. It always returns an array with three elements.
      *         All three can be empty.
      */
     public static String[] splitByTypeAndName(final String s)
     {
         final String[] parts = splitByType(s);
         final String[] parts2 = parts[1].split("\\s+", 2);
         return new String[] { parts[0], parts2[0],
             parts2.length == 2 ? parts2[1] : "" };
     }
 }
