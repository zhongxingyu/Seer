 package org.softwarehelps.learncs.SIMPLESQL;
 
 import java.util.*;
 import java.awt.*;
 
 public class U {
      public static int atoi (String s) {
           try {
                if (s.startsWith("+"))
                     s = s.substring(1);
                return Integer.valueOf(s).intValue();
           }
           catch (NumberFormatException nfe) {
                return 0;
           }
      }
 
      public static long atol (String s) {
           try {
                if (s.startsWith("+"))
                     s = s.substring(1);
                return Long.valueOf(s).longValue();
           }
           catch (NumberFormatException nfe) {
                return 0;
           }
      }
 
      public static double atod (String s) {
           try {
                return Double.valueOf(s).doubleValue();
           }
           catch (NumberFormatException nfe) {
                return 0;
           }
      }
 
      public static boolean isInt (String s) {
           try {
                if (s.startsWith("+"))
                     s = s.substring(1);
                int n = Integer.valueOf(s).intValue();
                return true;
           }
           catch (NumberFormatException nfe) {
                return false;
           }
      }
 
 
      public static String[] copy (String array[]) {
           String[] newone = new String[array.length];
           for (int i=0; i<array.length; i++)
                newone[i] = new String(array[i]);
           return newone;
      }
 
      public static String[] tokenize(String s) {
           StringTokenizer st = new StringTokenizer(s);
           String[] tokens = new String[st.countTokens()];
           int i=0;
           while (st.hasMoreTokens()) {
                String token = st.nextToken();
                tokens[i++] = token;
           }
           return tokens;
      }
 
      public static String[] tokenize(String s, String delim) {
           StringTokenizer st = new StringTokenizer(s, delim);
           String[] tokens = new String[st.countTokens()];
           int i=0;
           while (st.hasMoreTokens()) {
                String token = st.nextToken();
                tokens[i++] = token;
           }
           return tokens;
      }
 
      public static int find (String s, String[] tokens) {
           for (int i=0; i<tokens.length; i++) 
                if (tokens[i] != null && s.equals(tokens[i]))
                     return i;
           return -1;
      }
 
      public static String[] shift (String[] tokens, int howmany) {
           int numleft = tokens.length - howmany;
           if (numleft == 0)
                return null;
           String[] newtokens = new String[numleft];
           for (int i=0; i<numleft; i++) 
                newtokens[i] = tokens[howmany+i];
           return newtokens;
      }
 
      public static String detokenize(String[] tokens) {
           String s = "";
           for (int i=0; i<tokens.length; i++)
                if (tokens[i] != null)
                     s += tokens[i] + " ";
           return s;
      }
 
      public static boolean equals (String[] list1, String[] list2) {
           if (list1.length != list2.length)
                return false;
           for (int i=0; i<list1.length; i++)
                if (list1[i] != null && list2[i] != null)
                     if (list1[i].equals(list2[i]))
                          return false;
           return true;
      }
 
      public static void sleep (long milliseconds) {
           try {
                Thread.sleep(milliseconds);
           } catch (InterruptedException ie) {}
      }
 
      public static int power(int n, int power) {
           if (power < 0)
                return 0;
           int result = 1;
           for (int i=0; i<power; i++)
                result *= n;
           return result;
      }
 
      public static String convert (int n, int base) {
           String s = "";
           if (n == 0)
                return "0";
           while (n > 0) {
                int rem = n % base;
                n = n / base;
                s = Character.forDigit(rem, base) + s;
           }
           return s.toUpperCase();
      }
 
      public static boolean isint (String s) {
           char ch = s.charAt(0);
           int starting = 0;
           if (ch == '+' || ch == '-') {
                starting = 1;
                if (s.length() == 1)
                     return false;
           }
           for (int i=0; i<s.length(); i++) {
                ch = s.charAt(i);
                if (!Character.isDigit(ch))
                     return false;
           }
           return true;
      }
 
      public static String dec2bin (int n) {
           int sign = 1;
           if (n < 0) {
                sign = -1;
                n = -n;
           }
           String s = "";
           if (n == 0)
                return "0000000000000000";
           while (n > 0) {
                int rem = n % 2;
                n = n / 2;
                if (rem == 1)
                     s = "1" + s;
                else
                     s = "0" + s;
           }
           if (sign == 1)
                return s;
           else {
                return twoscomplement(s);
 }
      }
 
      public static int bin2dec (String s) {
           int sign = 1;
           if (s.length() >= 16 && s.charAt(0) == '1') {
                sign = -1;
                s = twoscomplement(s);
           }
           int n = 0;
           int len = s.length();
           for (int i=0; i<len; i++) {
                char ch = s.charAt(i);
                n *= 2;
                if (ch == '1')
                     n += 1;
           }
           return sign * n;
      }
 
      public static String twoscomplement (String s) {
           s = padout(s, '0', 16);
           String news = "";
           for (int i=0; i<16; i++) {
                char ch = s.charAt(i);
                if (ch == '0')
                     news += "1";
                else
                     news += "0";
           }
           String result = "";
           int carry = 1;
           for (int i=15; i>=0; i--) {
                char ch = news.charAt(i);
                if (carry == 1) {
                     if (ch == '1') 
                          result = "0" + result;
                     else {
                          result = "1" + result;
                          carry = 0;
                     }
                }
                else {
                     result = ch + result;
                }
           }
           return result;
      }
 
      public static String padout (String s, char padChar, int max) {
           while (s.length() < max)
                s = padChar + s;
           return s;
      }
 
      public static String squish (String s, char remch) {
           String news = "";
           for (int i=0; i<s.length(); i++) {
                char ch = s.charAt(i);
                if (ch != remch)
                    news += ch;
           }
           return news;
      }
 
      public static Color translateColor (String s) {
           if (s.equals("white"))  return Color.white;
           if (s.equals("red"))    return Color.red;
           if (s.equals("yellow")) return Color.yellow;
           if (s.equals("blue"))   return Color.blue;
           if (s.equals("cyan"))   return Color.cyan;
           if (s.equals("green"))  return Color.green;
           if (s.equals("gray"))   return Color.lightGray;
           return Color.black;
      }
 
      public static String getField (String s, int fieldnum) {
           return getField (s, fieldnum, ' ');
      }
 
      public static String getField (String s, int fieldnum, char separator) {
           StringTokenizer st = new StringTokenizer(s, separator+"");
           for (int i=0; i<=fieldnum; i++) {
                if (!st.hasMoreTokens())
                     return null;
                String tok = st.nextToken();
                if (i == fieldnum)
                     return tok;
           }
           return null;
      }
 
      public static void main (String[] args) {
           String s = "abc/xyz/def/jdkfjdkfd";
           System.out.println ("field 0 = "+getField(s,0,'/'));
           System.out.println ("field 1 = "+getField(s,1,'/'));
           System.out.println ("field 2 = "+getField(s,2,'/'));
           System.out.println ("field 3 = "+getField(s,3,'/'));
      }
 
      // The following picks apart the big string "s" which presumably
      // has newlines separating the lines, and it returns the nth line.
 
      public static String getLine (String s, int n) {
           int newlines = 0;
           String rets = "";
 
           for (int i=0; i<s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '\n') {
                     newlines++;
                     if (newlines > n)
                          return rets;
                }
                else {
                     if (newlines == n)
                          rets += ch;
                }
           }
           return rets;
      }
 
      // The following picks apart the big string "s" which presumably
      // has newlines separating the lines, and it counts how many newlines
      // it found, returning 1+ that value as the number of lines.
 
      public static int countLines (String s) {
           int newlines = 0;
 
           for (int i=0; i<s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '\n') 
                     newlines++;
           }
           return newlines+1;
      }
 
      public static String[] add (String s, String[] tokens) {
           String[] rets = new String[tokens.length+1];
           rets[0] = s;
           for (int i=0; i<tokens.length; i++)
                rets[i+1] = tokens[i];
           return rets;
      }
 
      public static String[] add (String[] tokens, String s) {
           String[] rets = new String[tokens.length+1];
           for (int i=0; i<tokens.length; i++)
                rets[i] = tokens[i];
           rets[tokens.length] = s;
           return rets;
      }
 
      public static boolean contains (String[] tokens, String s) {
           for (int i=0; i<tokens.length; i++)
                if (s.equals(tokens[i]))
                     return true;
           return false;
      }
 
      public static String removeChar (String s, char ch) {
           String rets = "";
           for (int i=0; i<s.length(); i++) {
                char c = s.charAt(i);
                if (ch != c)
                     rets += c;
           }
           return rets;
      }
 
      public static String replaceChar (String s, char ch1, char ch2) {
           String rets = "";
           for (int i=0; i<s.length(); i++) {
                char c = s.charAt(i);
                if (c == ch1)
                     rets += ch2;
                else
                     rets += c;
           }
           return rets;
      }
 
      public static String surroundWith (String s, char ch1, char ch2) {
           String rets = "";
           for (int i=0; i<s.length(); i++) {
                char c = s.charAt(i);
                if (c == ch1)
                     rets += ch2 + "" + ch1 + "" + ch2;
                else
                     rets += c;
           }
           return rets;
      }
 
      public static String replacePattern(String target, String pattern,
                                          String replacement) {
           String rets = "";
           while (true) {
                int n = target.indexOf(pattern);
                if (n == -1) 
                     return rets + target;
                rets = rets + target.substring(0,n) + replacement;
                target = target.substring(n+pattern.length());
           }
      }
 
      public static String[] getParts (String[] tokens, int start, int limit) {
           if (limit - start <= 0)
                return null;
           String[] rets = new String[limit-start];
           int k = 0;
           for (int i=start; i<limit; i++)
                rets[k++] = tokens[i];
           return rets;
      }
 
      public static void cleanCommas (String[] tokens) {
           for (int i=0; i<tokens.length; i++)
                if (tokens[i] != null && tokens[i].endsWith(","))
                     tokens[i] = tokens[i].substring(0,tokens[i].length()-1);
      }
 
      public static String print (String[] tokens) {
           String rets = "";
           for (int i=0; i<tokens.length; i++)
                rets = rets + " "+tokens[i];
           return rets.substring(1);
      }
 }
