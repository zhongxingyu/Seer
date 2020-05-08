 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.helper;
 
 import util.misc.Strings;
 
 /**
  * The <code>Match</code> class is used to test if a string satisfies a
  * set word pattern matching rules.
  * <p>
  * Examples:<br>
  * <pre>
  * Match m = new Match("one two three");
  * m.match("three one two"); // returns true
  * m.match("three two one go"); // returns true
  * m.allowExtraWords = false;
  * m.match("three two one go"): // returns false
  * m.match("three two honey"); // returns true
  * m.allowExtraChars = false;
  * m.match("three two honey"); // returns false
  * m.allowAnyWordArder = false;
  * m.match("three one two"); // returns false
  *
  * m.setPattern("one$two three$four"); // '$' means 'or'
  * m.match("one two"); // returns false
  * m.match("two three"); // returns true
  *
  * m.setPattern("&oto&s#s"); // '&' means any string, '#' means any char
  * m.match("photosynthesis"); // returns true
  * m.match("fotosynthesis"); // returns true
  * m.match("fotosinthesis"); // returns true
  * m.match("fotosinfisys"); // returns true
  * m.match("photasynthesis"); // returns false
  * </pre>
  * (Note: This is Chris Denham's code. I just copied it in here. --sam)
  */
 public class Match
 {
   /**
    * Constructs a pattern matching object, and calls setPattern(pattern) .
   * @param pattern 
    * @see #setPattern
    */
   public Match(String pattern)
   {
     setPattern(pattern);
   }
 
   /**
    * This field controls whether or not extra characters in a word are to be
    * permitted. For example, if the pattern is "abc" and this field is
    * set to a value of <code>true</code>, this will successfully match with strings
    * such as "abcde" or "abacab". If this field is set to a value of <code>false</code>
    * , only the string "abc" will match the pattern "abc".<p>
    * This field has a default value of <code>true</code>.
    */
   public boolean allowExtraChars = true;
 
   /**
    * This field controls whether or not words in the match string must appear in
    * the same order as the words in the pattern. For example, if the pattern
    * is "Mary had a little lamb" and this field is set to a value of <code>true</code>,
    * this will successfully match with strings such as "Mary lamb had a little".<p>
    * This field has a default value of <code>true</code>.
    */
   public boolean allowAnyWordOrder = true;
 
   /**
    * This field controls whether or not words that are not in the pattern
    * are permitted match string. For example, if the pattern
    * is "banana" and this field is set to a value of <code>true</code>,
    * this will successfully match with strings such as "Eat a banana today".
    * <p>
    * This field has a default value of <code>true</code>.
    */
   public boolean allowExtraWords = true;
 
   /**
    * An array of strings whose elements contain the individual words in the
    * pattern string.
    */
   private String patternWords[];
 
   /**
    * Sets the pattern that we want to test strings against.
    * The pattern is a set of one or more words (each word separated by a space).
    * To get a match, all of the words in the pattern must match with a word
    * in the string you are matching against. Extra control of the matching
    * process can be achieved using the '&', '#' and '$' characters. If you want
    * to use these characters literally, precede them with the backslash character.
    * <br>
    * '&' means match any string. <br>
    * '#' means match any character. <br>
    * '$' match with the preceding OR the next word. <br>
    * See the examples above for details of usage.
   * @param pattern 
    */
   public void setPattern(String pattern)
   {
      patternWords = breakIntoTokens(pattern, ' ');
   }
 
   /**
    * This function is only provided for compatibility with OpenWin oMatch class.
    * The preferred method for using this class would be to create
    * an instance of the Match class and control its mode through the
    * object's fields.
   * @param response 
   * @param pattern 
   * @return whether the response matches the pattern.
    */
   static public boolean match(String response, String pattern)
   {
     if (pattern.charAt(0) != '%')
     {
       System.err.println("Old style Match pattern must start with %<mode>");
       return false;
     }
     String parts[] = breakIntoTokens(pattern, '%');
     for (int i = 0; i < parts.length; i++)
     {
       int mode = parts[i].charAt(0) - '0';
       Match match = new Match(parts[i].substring(1));
       match.allowExtraChars = (mode & 1) == 0;
       match.allowAnyWordOrder = (mode & 2) == 0;
       match.allowExtraWords = (mode & 4) == 0;
       if (match.match(response)) return true;
     }
     return false;
   }
 
   /**
    * Tests whether the specified string satisfies the pattern matching rules.
   * @param response 
    * @return if the response matches the pattern, this method returns a
    * value of <code>true</code>, otherwise it returns <code>false</code>.
    */
   public boolean match(String response)
   {
     String words[] = breakIntoTokens(response, ' ');
     if (!allowExtraWords && words.length != patternWords.length) return false;
 
     int correct = 0;
     int iw = 0;
     for (int ip = 0; ip < patternWords.length; ip++)
     {
       if (allowAnyWordOrder) iw = 0;
       while (iw < words.length)
       {
         boolean matched = matchWordEx(words[iw++], patternWords[ip]);
         if (matched) { correct++; break; }
       }
     }
     return correct == patternWords.length;
   }
 
   /**
    * Calls matchWord for each or-ed word in chars.
    * ("or-ed" combinations are delimited by the '$' character)
    */
   private boolean matchWordEx(String word, String chars)
   {
     String parts[] = breakIntoTokens(chars, '$');
     for (int i = 0; i < parts.length; i++)
     {
       String pattern = substituteWildCards(parts[i]);
       if (matchWord(word, pattern)) return true;
     }
     return false;
   }
 
   /**
    * Checks if the specified word contains all the characters in 'chars'
    * in the same order.
    */
   private boolean matchWord(String word, String chars)
   {
     int index = 0;
     for (int i = 0; i < chars.length(); i++)
     {
       char c = chars.charAt(i);
       if (c != hash.charAt(0)) // code for '#' matches any char
       {
         if (c == ampersand.charAt(0)) // code for '&' matches any string
         {
           if (i == chars.length() - 1) return true; // '&' at end
           index = findNextIndex(word, index, chars.substring(i + 1));
           if (index == -1) return false;
         } else if (allowExtraChars) {
           index = word.indexOf(c, index);
           if (index == -1) return false;
           index++;
         } else {
           if (index >= word.length() || c != word.charAt(index)) return false;
           index++;
         }
       } else index++;
     }
     return allowExtraChars ? true : (index == word.length());
   }
 
   /**
    * For a pattern word containing the '&' wildcard, we need to look-ahead to
    * find something that matches the part of the word that follows the '&'
    * but not including any text following any subsequent '&' in the pattern word
    */
   private int findNextIndex(String word, int index, String chars)
   {
     if (chars.charAt(0) == '&') return index; // "&&" is the same as "&"
     int endMatch = chars.indexOf('&');
     if (endMatch != -1) chars = chars.substring(0, endMatch);
     for (int i = index; i < word.length(); i++)
     {
         if (matchWord(word.substring(i), chars)) return i;
     }
     return index;
   }
 
   /**
    * Encodes wildcards '#' and '&' as "unlikey" unicode chracters.
    * (also de-escapes any escaped wildcard characters).
    * This code relys on these unicode characters not being present in the
    * the pattern string; a pretty safe assumption because the typical usage
    * will only use characters in the ASCII range i.e. 0x00 to 0x7F
    */
   private static final String tempC = "\uBAD1";
   private static final String hash = "\uBAD2";
   private static final String ampersand = "\uBAD3";
   private String substituteWildCards(String chars)
   {
     chars = Strings.replace(chars, "\\#", tempC);
     chars = Strings.replace(chars, "#", hash);
     chars = Strings.replace(chars, tempC, "#");
 
     chars = Strings.replace(chars, "\\&", tempC);
     chars = Strings.replace(chars, "&", ampersand);
     chars = Strings.replace(chars, tempC, "&");
 
     return chars;
   }
 
   /**
    * This is a variant of Util.breakIntoTokens except this version preserves
    * any escaped delimiter characters by replacing them with a "hopefully"
    * extremely unlikely unicode character, then reverting these characters
    * in the tokenized strings.
    */
   static private String[] breakIntoTokens(String text, char delimiter)
   {
     String sDelimiter = "" + delimiter;
     if (delimiter == '$') sDelimiter = "\\$"; // needs escaping for regex
     
     text = Strings.replace(text, "\\" + delimiter, tempC);
     String tokens[] = text.split(sDelimiter);
     for (int i = 0; i < tokens.length; i++)
     {
       tokens[i] = Strings.replace(tokens[i], tempC, sDelimiter);
     }
     return tokens;
   }
 
 }
