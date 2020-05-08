 package com.atlassian.sal.core.pluginsettings;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 
 /**
  * Escape utility methods which only escapes VERTICAL_TAB and NEW_LINE.
  *
  * The escape logic is custom made here instead of using StringEscapeUtils in apache common since
  * we don't want to interfere with other special characters that we don't use in {@link AbstractStringPluginSettings}.
  *
  * For example, in that case if the data read from the backing store is "this is \\me", we're not sure whether
  * it's escaped or non-escaped.
  *
  * @since 2.2.0
  */
 class EscapeUtils
 {
     // Not for instantiation.
     private EscapeUtils()
     {
     }
 
     protected static final char VERTICAL_TAB = '\f';
     protected static final char NEW_LINE = '\n';
 
     /**
      * This only escapes VERTICAL_TAB and NEW_LINE.
      */
     protected static String escape(String str)
     {
         if (str == null)
         {
             return null;
         }
 
         StringWriter writer = new StringWriter(str.length() * 2);
         try
         {
             escape(writer, str);
         }
         catch (IOException e)
         {
             throw new RuntimeException("exception while writing to StringWriter (should be impossible in this context)", e);
         }
 
         return writer.toString();
     }
 
     private static void escape(Writer out, String str) throws IOException
     {
         int len = str.length();
         for (int i = 0; i < len; i++)
         {
             char ch = str.charAt(i);
 
             if (ch == VERTICAL_TAB)
             {
                 out.write('\\');
                 out.write('f');
             }
             else if (ch == NEW_LINE)
             {
                 out.write('\\');
                 out.write('n');
             }
             else
             {
                out.write(ch);
             }
         }
     }
 
     public static String unescape(String str)
     {
         if (str == null)
         {
             return null;
         }
         try
         {
             StringWriter writer = new StringWriter(str.length());
             unescape(writer, str);
             return writer.toString();
         }
         catch (IOException e)
         {
             throw new RuntimeException("exception while writing to StringWriter (should be impossible in this context)", e);
         }
      }
 
     private static void unescape(Writer out, String str) throws IOException
     {
         int len = str.length();
         boolean hadSlash = false;
 
         for (int i = 0; i < len; i++) {
             char ch = str.charAt(i);
             if (hadSlash) {
                 switch (ch) {
                     case 'f':
                         out.write('\f');
                         break;
                     case 'n':
                         out.write('\n');
                         break;
                     default :
                         out.write(ch);
                         break;
                 }
                 // the slash is taken care of.
                 hadSlash = false;
                 continue;
             } else if (ch == '\\') {
                 hadSlash = true;
                 continue;
             }
             out.write(ch);
         }
         // the the slash is the last character, then just give it out.
         if (hadSlash) {
             out.write('\\');
         }
     }
 }
