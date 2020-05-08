 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only
  *    (as Java .class files or a .jar file containing the .class files) and only
  *    as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software
  *    development kit, other library, or development tool without written consent of
  *    Netspective Corporation. Any modified form of The Software is bound by
  *    these same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: SyntaxHighlight.java,v 1.2 2002-09-17 02:08:52 shahid.shah Exp $
  */
 
 package com.netspective.sparx.xaf.html;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.Ostermiller.Syntax.Lexer.Lexer;
 import com.Ostermiller.Syntax.Lexer.Token;
 
 public class SyntaxHighlight
 {
     private static Map lexers = new HashMap();
 
     static
     {
         register(
                com.Ostermiller.Syntax.Lexer.HTMLLexer1.class,
                 new String[]{
                     "htm",
                     "html",
                     "xml",
                     "jsp",
                     "xsl",
                 }
         );
         register(
                 com.Ostermiller.Syntax.Lexer.JavaLexer.class,
                 new String[]{
                     "jav",
                     "java",
                 }
         );
         register(
                 com.Ostermiller.Syntax.Lexer.JavaScriptLexer.class,
                 new String[]{
                     "js",
                 }
         );
         register(
                 com.Ostermiller.Syntax.Lexer.SQLLexer.class,
                 new String[]{
                     "sql",
                 }
         );
         register(
                 com.Ostermiller.Syntax.Lexer.CLexer.class,
                 new String[]{
                     "c",
                     "h",
                     "cc",
                     "cpp",
                     "cxx",
                     "c++",
                     "hpp",
                     "hxx",
                     "hh",
                 }
         );
         register(
                 com.Ostermiller.Syntax.Lexer.PropertiesLexer.class,
                 new String[]{
                     "props",
                     "properties",
                 }
         );
         register(
                 com.Ostermiller.Syntax.Lexer.LatexLexer.class,
                 new String[]{
                     "tex",
                     "sty",
                     "cls",
                     "dtx",
                     "ins",
                     "latex",
                 }
         );
         register(
                 com.Ostermiller.Syntax.Lexer.PlainLexer.class,
                 new String[]{
                     "txt",
                     "text",
                 }
         );
     }
 
     /**
      * Register a lexer to handle the given mime types and fileExtensions.
      * <p>
      * If a document has a type "text/plain" it will first match a registered
      * mime type of "text/plain" and then a registered mime type of "text".
      *
      * @param lexer String representing the fully qualified java name of the lexer.
      * @param mimeTypes array of mime types that the lexer can handle.
      * @param fileExtensions array of fileExtensions the lexer can handle. (case insensitive)
      */
     public static void register(Class lexer, String[] fileExtensions)
     {
         for (int i = 0; i < fileExtensions.length; i++)
         {
             lexers.put(fileExtensions[i].toLowerCase(), lexer);
         }
     }
 
     /**
      * Open a span if needed for the given style.
      *
      * @param description style description.
      * @param out place to write output.
      */
     private static void openSpan(String description, Writer out) throws IOException
     {
         out.write("<span class=" + description + ">");
     }
 
     /**
      * Close a span if needed for the given style.
      *
      * @param description style description.
      * @param out place to write output.
      */
     private static void closeSpan(String description, Writer out) throws IOException
     {
         out.write("</span>");
     }
 
     /**
      * Write a highlighted document html fragment.
      *
      * @param lexer Lexer from which to get input.
      * @param out place to write output.
      * @throws IOException if an I/O error occurs.
      */
     public static void emitHtml(Lexer lexer, Writer out) throws IOException
     {
         String currentDescription = null;
         Token token;
         out.write("<pre>");
         while ((token = lexer.getNextToken()) != null)
         {
             // optimization implemented here:
             // ignored white space can be put in the same span as the stuff
             // around it.  This saves space because spans don't have to be
             // opened and closed.
             if ((token.isWhiteSpace()) || (currentDescription != null && token.getDescription().equals(currentDescription)))
             {
                 writeEscapedHTML(token.getContents(), out);
             }
             else
             {
                 if (currentDescription != null) closeSpan(currentDescription, out);
                 currentDescription = token.getDescription();
                 openSpan(currentDescription, out);
                 writeEscapedHTML(token.getContents(), out);
             }
         }
         if (currentDescription != null) closeSpan(currentDescription, out);
         out.write("</pre>");
     }
 
     /**
      * Write a highlighted document html fragment.
      *
      * @param fileName the fileName to get the source from
      * @param out place to write output.
      * @returns true if this file type can be handled
      * @throws IOException if an I/O error occurs.
      */
     public static boolean emitHtml(String type, Reader in, Writer out) throws IOException
     {
         Class lexerClass = (Class) lexers.get(type);
         if (lexerClass != null)
         {
             try
             {
                 Constructor cons = lexerClass.getDeclaredConstructor(new Class[]{Class.forName("java.io.Reader")});
                 Lexer lexer = (Lexer) cons.newInstance(new Object[]{in});
                 emitHtml(lexer, out);
             }
             catch (Exception e)
             {
                 throw new IOException(e.getMessage());
             }
             return true;
         }
         else
             return false;
     }
 
     /**
      * Write a highlighted document html fragment.
      *
      * @param fileName the fileName to get the source from
      * @param out place to write output.
      * @returns true if this file type can be handled
      * @throws IOException if an I/O error occurs.
      */
     public static boolean emitHtml(File file, Writer out) throws IOException
     {
         String fileName = file.getAbsolutePath();
         if (file.exists())
         {
             String extn = fileName.substring(fileName.lastIndexOf('.') + 1);
             return emitHtml(extn, new FileReader(file), out);
         }
         else
             throw new IOException("File not found: " + fileName);
     }
 
     /**
      * Write a highlighted document html fragment.
      *
      * @param fileName the fileName to get the source from
      * @param out place to write output.
      * @returns true if this file type can be handled
      * @throws IOException if an I/O error occurs.
      */
     public static boolean emitHtml(String fileName, Writer out) throws IOException
     {
         return emitHtml(new File(fileName), out);
     }
 
     /**
      * Write the string after escaping characters that would hinder
      * it from rendering in html.
      *
      * @param text The string to be escaped and written
      * @param out output gets written here
      */
     private static void writeEscapedHTML(String text, Writer out) throws IOException
     {
         boolean lastSpace = false;
         for (int i = 0; i < text.length(); i++)
         {
             char ch = text.charAt(i);
             switch (ch)
             {
                 case '<':
                     {
                         out.write("&lt;");
                         lastSpace = false;
                         break;
                     }
                 case '>':
                     {
                         out.write("&gt;");
                         lastSpace = false;
                         break;
                     }
                 case '&':
                     {
                         out.write("&amp;");
                         lastSpace = false;
                         break;
                     }
                 case '"':
                     {
                         out.write("&quot;");
                         lastSpace = false;
                         break;
                     }
                 default:
                     {
                         out.write(ch);
                         lastSpace = false;
                         break;
                     }
             }
         }
     }
 }
