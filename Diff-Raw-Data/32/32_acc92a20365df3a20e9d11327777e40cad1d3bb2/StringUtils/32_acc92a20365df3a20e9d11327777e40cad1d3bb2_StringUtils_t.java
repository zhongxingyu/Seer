 /*
                         ACM-SL Commons
 
     Copyright (C) 2002-2003  Jose San Leandro Armendriz
                              jsanleandro@yahoo.es
                              chousz@yahoo.com
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jsr000@terra.es
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabaas
                     Boadilla del monte
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendriz
  *
  * Description: Provides some useful methods when working with Strings.
  *
  * Last modified by: $Author$ at $Date$
  *
  * File version: $Revision$
  *
  * Project version: $Name$
  *                  ("Name" means no concrete version has been checked out)
  *
  * $Id$
  *
  */
 package org.acmsl.commons.utils;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.commons.patterns.Utils;
 import org.acmsl.commons.regexpplugin.Compiler;
 import org.acmsl.commons.regexpplugin.Helper;
 import org.acmsl.commons.regexpplugin.MalformedPatternException;
 import org.acmsl.commons.regexpplugin.Matcher;
 import org.acmsl.commons.regexpplugin.MatchResult;
 import org.acmsl.commons.regexpplugin.Pattern;
 import org.acmsl.commons.regexpplugin.RegexpEngineNotFoundException;
 import org.acmsl.commons.regexpplugin.RegexpManager;
 import org.acmsl.commons.utils.StringValidator;
 import org.acmsl.commons.version.Version;
 import org.acmsl.commons.version.VersionFactory;
 
 /*
  * Importing some JDK classes.
  */
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.StringTokenizer;
 
 /*
  * Importing commons-logging classes.
  */
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Provides some useful methods when working with Strings.
  * @author <a href="mailto:jsanleandro@yahoo.es"
            >Jose San Leandro Armendriz</a>
  * @stereotype tested
  * @testcase unittests.org.acmsl.commons.utils.TestStringUtils
  * @version $Revision$
  */
 public abstract class StringUtils
     implements  Utils
 {
     /**
      * Singleton implemented as a weak reference.
      */
     private static WeakReference singleton;
 
     /**
      * Compiler instance.
      */
     private static Compiler m__Compiler;
 
     /**
      * The subpackage pattern.
      */
     private static Pattern m__SubPackagePattern;
 
     /**
      * The package pattern.
      */
     private static Pattern m__PackagePattern;
 
     /**
      * The justify pattern.
      */
     private static Pattern m__JustifyPattern;
 
     /**
      * The uncapitalize pattern.
      */
     private static Pattern m__UnCapitalizePattern;
 
     /**
      * Specifies a new weak reference.
      * @param utils the utils instance to use.
      */
     protected static void setReference(final StringUtils utils)
     {
         singleton = new WeakReference(utils);
     }
 
     /**
      * Retrieves the weak reference.
      * @return such reference.
      */
     protected static WeakReference getReference()
     {
         return singleton;
     }
 
     /**
      * Retrieves a StringUtils instance.
      * @return such instance.
      */
     public static StringUtils getInstance()
     {
         StringUtils result = null;
 
         WeakReference reference = getReference();
 
         if  (reference != null) 
         {
             result = (StringUtils) reference.get();
         }
 
         if  (result == null) 
         {
             result = new StringUtils() {};
         }
 
         Compiler t_Compiler = result.getCompiler();
 
         if  (t_Compiler == null)
         {
             try 
             {
                 t_Compiler = createCompiler();
 
                 if  (t_Compiler != null)
                 {
                     result.immutableSetCompiler(t_Compiler);
 
                     try 
                     {
                         result.immutableSetSubPackagePattern(
                             t_Compiler.compile("(.*)\\.(.*)"));
                     }
                     catch  (final MalformedPatternException exception)
                     {
                         /*
                          * This should never happen. It's a compile-time
                          * error not detected by the compiler, but it's
                          * nothing dynamic. So, if it fails, fix it once
                          * and forget.
                          */
                         LogFactory.getLog(StringUtils.class).error(
                             "Invalid sub-package pattern", exception);
 
                         result = null;
                     }
 
                     if  (result != null)
                     {
                         try
                         {
                             result.immutableSetPackagePattern(
                                 t_Compiler.compile("(.*?)\\.(.*)"));
                         }
                         catch  (final MalformedPatternException exception)
                         {
                             /*
                              * This should never happen. It's a compile-time
                              * error not detected by the compiler, but it's
                              * nothing dynamic. So, if it fails, fix it once
                              * and forget.
                              */
                             LogFactory.getLog(StringUtils.class).error(
                                 "Invalid package pattern", exception);
 
                             result = null;
                         }
                     }
 
                     if  (result != null)
                     {
                         try
                         {
                             result.immutableSetJustifyPattern(
                                 t_Compiler.compile("(.*?)\\s+(.*)"));
                         }
                         catch  (final MalformedPatternException exception)
                         {
                             /*
                              * This should never happen. It's a compile-time
                              * error not detected by the compiler, but it's
                              * nothing dynamic. So, if it fails, fix it once
                              * and forget.
                              */
                             LogFactory.getLog(StringUtils.class).error(
                                 "Invalid justify pattern", exception);
 
                             result = null;
                         }
                     }
 
                     if  (result != null)
                     {
                         try
                         {
                             boolean t_bCaseSensitive = t_Compiler.isCaseSensitive();
                             t_Compiler.setCaseSensitive(true);
                             result.immutableSetUnCapitalizePattern(
                                 t_Compiler.compile("\\s*([^A-Z\\s]*)\\s*([A-Z]?)?(.*)"));
                             t_Compiler.setCaseSensitive(t_bCaseSensitive);
                         }
                         catch  (final MalformedPatternException exception)
                         {
                             /*
                              * This should never happen. It's a compile-time
                              * error not detected by the compiler, but it's
                              * nothing dynamic. So, if it fails, fix it once
                              * and forget.
                              */
                             LogFactory.getLog(StringUtils.class).error(
                                 "Invalid un-capitalize pattern", exception);
 
                             result = null;
                         }
                     }
                 }
                 else 
                 {
                     LogFactory.getLog(StringUtils.class).error(
                         "compiler unavailable");
                 }
 
                 if  (result != null)
                 {
                     setReference(result);
                 }
             }
             catch  (final RegexpEngineNotFoundException exception)
             {
                 LogFactory.getLog(StringUtils.class).error(
                     "no regexp engine found", exception);
 
                 result = null;
             }
             catch  (final Throwable throwable)
             {
                 LogFactory.getLog(StringUtils.class).fatal(
                     "Unknown error", throwable);
             }
         }
         
         return result;
     }
 
     /**
      * Protected constructor to avoid accidental instantiation.
      */
     protected StringUtils()  {};
 
     /**
      * Specifies the regexp compiler.
      * @param compiler the compiler.
      */
     private void immutableSetCompiler(final Compiler compiler)
     {
         m__Compiler = compiler;
     }
 
     /**
      * Specifies the regexp compiler.
      * @param compiler the compiler.
      */
     protected void setCompiler(final Compiler compiler)
     {
         immutableSetCompiler(compiler);
     }
 
     /**
      * Retrieves the regexp compiler.
      * @return such compiler.
      */
     protected Compiler getCompiler()
     {
         return m__Compiler;
     }
 
     /**
      * Specifies the subpackage pattern.
      * @param pattern the pattern.
      */
     private void immutableSetSubPackagePattern(final Pattern pattern)
     {
         m__SubPackagePattern = pattern;
     }
 
     /**
      * Specifies the subpackage pattern.
      * @param pattern the pattern.
      */
     protected void setSubPackagePattern(final Pattern pattern)
     {
         immutableSetSubPackagePattern(pattern);
     }
 
     /**
      * Retrieves the subpackage pattern.
      * @return such pattern.
      */
     public Pattern getSubPackagePattern()
     {
         return m__SubPackagePattern;
     }
 
     /**
      * Specifies the package pattern.
      * @param pattern the pattern.
      */
     private void immutableSetPackagePattern(final Pattern pattern)
     {
         m__PackagePattern = pattern;
     }
 
     /**
      * Specifies the package pattern.
      * @param pattern the pattern.
      */
     protected void setPackagePattern(final Pattern pattern)
     {
         immutableSetPackagePattern(pattern);
     }
 
     /**
      * Retrieves the package pattern.
      * @return such pattern.
      */
     public Pattern getPackagePattern()
     {
         return m__PackagePattern;
     }
 
     /**
      * Specifies the justify pattern.
      * @param pattern the pattern.
      */
     private void immutableSetJustifyPattern(final Pattern pattern)
     {
         m__JustifyPattern = pattern;
     }
 
     /**
      * Specifies the justify pattern.
      * @param pattern the pattern.
      */
     protected void setJustifyPattern(final Pattern pattern)
     {
         immutableSetJustifyPattern(pattern);
     }
 
     /**
      * Retrieves the justify pattern.
      * @return such pattern.
      */
     public Pattern getJustifyPattern()
     {
         return m__JustifyPattern;
     }
 
     /**
      * Specifies the uncapitalize pattern.
      * @param pattern the pattern.
      */
     private void immutableSetUnCapitalizePattern(final Pattern pattern)
     {
         m__UnCapitalizePattern = pattern;
     }
 
     /**
      * Specifies the uncapitalize pattern.
      * @param pattern the pattern.
      */
     protected void setUnCapitalizePattern(final Pattern pattern)
     {
         immutableSetUnCapitalizePattern(pattern);
     }
 
     /**
      * Retrieves the uncapitalize pattern.
      * @return such pattern.
      */
     public Pattern getUnCapitalizePattern()
     {
         return m__UnCapitalizePattern;
     }
 
     /**
      * Creates the compiler.
      * @return the regexp compiler.
      */
     protected static synchronized Compiler createCompiler()
         throws  RegexpEngineNotFoundException
     {
         return RegexpManager.createCompiler();
 
     }
 
     /**
      * Removes all duplicates of specified char in given text.
      * @param text the text to parse.
      * @param lookfor the char to remove duplicates.
      * @return the updated text.
      */
     public String removeDuplicate(
         final String original, final char lookfor)
     {
         return
             removeDuplicate(original, lookfor, StringValidator.getInstance());
     }
 
     /**
      * Removes all duplicates of specified char in given text.
      * @param text the text to parse.
      * @param lookfor the char to remove duplicates.
      * @param stringValidator the StringValidator instance.
      * @return the updated text.
      * @precondition stringValidator != null
      */
     protected String removeDuplicate(
         final String original,
         final char lookfor,
         final StringValidator stringValidator)
     {
         String result = original;
 
         if  (!stringValidator.isEmpty(original))
         {
             String t_strLookFor = new String(new char[]{ lookfor });
 
             StringTokenizer t_strTokenizer =
                 new StringTokenizer(original, t_strLookFor, false);
 
             if  (t_strTokenizer.hasMoreTokens())
             {
                 result = "";
             }
 
             while  (t_strTokenizer.hasMoreTokens())
             {
                 result += t_strTokenizer.nextToken();
 
                 if  (t_strTokenizer.hasMoreTokens())
                 {
                     result += t_strLookFor;
                 }
             }
         }
         
         return result;
     }
 
     /**
      * Replaces a sequence with another inside a text content.
      * @param content the text content to update.
      * @param original the text to replace.
      * @param replacement the replacement.
      * @return the updated version of the original text.
      */
     public String replace(
         final String content,
         final String original,
         final String replacement)
     {
         return
             replace(
                 content,
                 original,
                 replacement,
                 StringValidator.getInstance(),
                 getCompiler(),
                 RegexpManager.createMatcher());
     }
 
     /**
      * Replaces a sequence with another inside a text content.
      * @param content the text content to update.
      * @param original the text to replace.
      * @param replacement the replacement.
      * @param stringValidator the StringValdiator instance.
      * @param compiler the compiler.
      * @parma matcher the matcher.
      * @return the updated version of the original text.
      * @precondition stringValidator != null
      * @precondition compiler != null
      * @precondition matcher != null
      */
     protected String replace(
         final String content,
         final String original,
         final String replacement,
         final StringValidator stringValidator,
         final Compiler compiler,
         final Matcher matcher)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         try
         {
             MatchResult t_MatchResult = null;
 
             Pattern t_Pattern =
                 compiler.compile(
                     "(.*?)" + original + "(.*)");
 
             String t_strTextToProcess = content;
 
             if  (t_Pattern != null)
             {
                 while   (   (!stringValidator.isEmpty(t_strTextToProcess))
                          && (matcher.contains(content, t_Pattern)))
                 {
                     t_MatchResult = matcher.getMatch();
 
                     t_sbResult.append(t_MatchResult.group(1) + replacement);
 
                     // the rest is parsed next.
                     t_strTextToProcess = t_MatchResult.group(2);
                 }
             }
 
             if  (!stringValidator.isEmpty(t_strTextToProcess))
             {
                 t_sbResult.append(t_strTextToProcess);
             }
         }
         catch (final MalformedPatternException exception)
         {
             LogFactory.getLog(getClass()).warn(
                 "Malformed pattern (possibly a quote symbol conflict)",
                 exception);
         }
         catch  (final RegexpEngineNotFoundException exception)
         {
             /*
              * This exception is thrown only if no regexp library is available
              * at runtime. Not only this one, but any method provided by this
              * class that use regexps will not work.
              */
             LogFactory.getLog(getClass()).error(
                 "Cannot find any regexp engine", exception);
         }
 
         return t_sbResult.toString();
     }
 
     /**
      * Puts a quote before and after given string.
      * @param text the content to quote.
      * @return the quoted version of such content.
      */
     public String quote(final String text)
     {
         return quote(text, '\"');
     }
 
     /**
      * Puts a quote before and after given string.
      * @param text the content to quote.
      * @param quote the quote.
      * @return the quoted version of such content.
      */
     public String quote(final String text, final char quote)
     {
         return quote(text, quote, StringValidator.getInstance());
     }
 
     /**
      * Puts a quote before and after given string.
      * @param text the content to quote.
      * @param quote the quote.
      * @param stringValidator the StringValidator instance.
      * @return the quoted version of such content.
      * @precondition stringValidator != null
      */
     public String quote(
         final String text,
         final char quote,
         final StringValidator stringValidator)
     {
         String result = text;
 
         boolean t_bProcessed = true;
 
         if  (!stringValidator.isEmpty(text))
         {
             if  (text.charAt(0) != quote)
             {
                 if  (text.charAt(0) != quote)
                 {
                     result = quote + text + quote;
                 }
                 else
                 {
                     if  (text.trim().length() > 0)
                     {
                         result =
                               quote
                             + text.substring(1, text.length() - 1)
                             + quote;
                     }
                     else
                     {
                         t_bProcessed = false;
                     }
                 }
             }
             else
             {
                 result = text;
             }
         }
 
         if  (!t_bProcessed)
         {
             result = "" + quote + quote;
         }
 
         return result;
     }
 
     /**
      * Removes all quotes at the beginning and ending of given string.
      * @param text the content to unquote.
      * @return the unquoted version of such content.
      */
     public String unquote(final String text)
     {
         return unquote(text, '\'');
     }
 
     /**
      * Removes all quotes at the beginning and ending of given string.
      * @param text the content to unquote.
      * @param quote the quote.
      * @return the unquoted version of such content.
      */
     public String unquote(final String text, final char quote)
     {
         return unquote(text, quote, StringValidator.getInstance());
     }
 
     /**
      * Removes all quotes at the beginning and ending of given string.
      * @param text the content to unquote.
      * @param quote the quote.
      * @param stringValidator the StringValidator instance.
      * @return the unquoted version of such content.
      * @precondition stringValidator != null
      */
     public String unquote(
         final String text,
         final char quote,
         final StringValidator stringValidator)
     {
         String result = "";
 
         if  (!stringValidator.isEmpty(text))
         {
             if  (text.charAt(0) != quote)
             {
                 if  (text.charAt(0) != quote)
                 {
                     result = text;
                 }
                 else
                 {
                     if  (text.trim().length() > 0)
                     {
                         result =
                             text.substring(1, text.length() - 1);
                     }
                 }
             }
             else
             {
                 result =
                     text.substring(1, text.length() - 1);
             }
         }
 
         return result;
     }
 
     /**
      * Normalizes given value, whose words are defined by a concrete char
      * separator. In this situation, <i>normalization</i> means
      * <i>capitalization</i> and <i>removal</i> of all non-alphanumeric
      * characters.
      * @param value the value to normalize.
      * @param separator the word separator.
      * @return the normalized version.
      */
     public String normalize(final String value, final char separator)
     {
         String result = value;
 
         if  (result != null) 
         {
             try
             {
                 String t_strValue = capitalize(value, separator);
 
                 Helper t_Helper = RegexpManager.createHelper();
 
                 result = t_Helper.replaceAll(t_strValue, "\\W", "");
             }
             catch (final MalformedPatternException exception)
             {
                 LogFactory.getLog(getClass()).fatal(
                     "Malformed pattern",
                     exception);
             }
             catch (final RegexpEngineNotFoundException exception)
             {
                 LogFactory.getLog(getClass()).fatal(
                     "Cannot find any regexp engine.",
                     exception);
             }
         }
         
         return result;
     }
 
     /**
      * Capitalizes the words contained in given string, using a concrete char
      * separator. For instance,
      * <code>capitalize("asd-efg", '-').equals("AsdEfg")</code>.
      * @param text the text to process.
      * @param separator the word separator.
      * @return the capitalized string.
      */
     public String capitalize(final String text, final char separator)
     {
         StringBuffer t_sbResult = null;
 
         if  (text != null) 
         {
             t_sbResult = new StringBuffer(text.length());
 
             boolean t_bToUpper = true;
 
             for  (int t_iIndex = 0; t_iIndex < text.length(); t_iIndex++)
             {
                 if (text.charAt(t_iIndex) == separator)
                 {
                     t_bToUpper = true;
                     continue;
                 }
 
                 if (t_bToUpper)
                 {
                     t_sbResult.append(
                         Character.toUpperCase(text.charAt(t_iIndex)));
 
                     t_bToUpper = false;
                     continue;
                 }
 
                 t_sbResult.append(text.charAt(t_iIndex));
             }
         }
 
         return ((t_sbResult == null) ? null : t_sbResult.toString());
     }
 
     /**
      * Normalizes the words contained in given string, using a concrete char
      * separator. The first letter is always in lower case.
      * @param text the text to process.
      * @param separator the word separator.
      * @return the processed string.
      */
     public String toJavaMethod(final String text, final char separator)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         String t_strText = normalize(text, separator);
 
         if  (t_strText != null)
         {
             if  (t_strText.length() > 0) 
             {
                 t_sbResult.append(t_strText.substring(0,1).toLowerCase());
                 t_sbResult.append(t_strText.substring(1));
             }
             else 
             {
                 t_sbResult.append(t_strText.toLowerCase());
             }
         }
 
         return t_sbResult.toString();
     }
 
     /**
      * Capitalizes the contents, using given separator, and word list.
      * This is the same as, replacing all words from the list in the
      * input to its capitalized versions, and finally
      * applying <code>toJavaMethod(text, separator)</code> to the result.
      * @param text the text.
      * @param separator the separator.
      * @param words the predefined words.
      * @return the token collection.
      */
     public String toJavaMethod(
         final String text, final char separator, final String[] words)
     {
         String result = text;
 
         try
         {
             if  (   (text      != null)
                  && (words     != null))
             {
                 Helper t_Helper = RegexpManager.createHelper();
 
                 for  (int t_iIndex = 0; t_iIndex < words.length; t_iIndex++) 
                 {
                     result =
                         t_Helper.replaceAll(
                             result,
                             words[t_iIndex],
                             capitalize(words[t_iIndex], separator));
                 }
             }
 
             result = toJavaMethod(result, separator);
         }
         catch  (final MalformedPatternException exception)
         {
             /*
              * This exception is about pattern mismatch. In my opinion,
              * it's an error that should be detected at compile time,
              * but regexp API design cannot provide this functionality,
              * since all patterns are defined with strings, and therefore
              * they escape all compiler checks.
              */
             LogFactory.getLog(StringUtils.class).warn(
                 "Malformed static patterns are fatal coding errors.",
                 exception);
         }
         catch  (final RegexpEngineNotFoundException exception)
         {
             /*
              * This exception is thrown only if no regexp library is available
              * at runtime. Not only this one, but any method provided by this
              * class that use regexps will not work.
              */
             LogFactory.getLog(StringUtils.class).fatal(
                 "Cannot find any regexp engine.",
                 exception);
         }
         
         return result;
     }
 
     /**
      * Retrieves all tokens found in given string, using given separator.
      * @param text the text.
      * @param separator the separator.
      * @return the token collection.
      */
     public Collection tokenize(final String text, final String separator)
     {
         return
             tokenize(
                 text,
                 separator,
                 StringValidator.getInstance(),
                 getCompiler());
     }
 
     /**
      * Retrieves all tokens found in given string, using given separator.
      * @param text the text.
      * @param separator the separator.
      * @param stringValidator the StringValidator instance.
      * @param compiler the compiler.
      * @return the token collection.
      * @precondition stringValidator != null
      * @precondition compiler != null
      */
     protected Collection tokenize(
         final String text,
         final String separator,
         final StringValidator stringValidator,
         final Compiler compiler)
     {
         ArrayList result = new ArrayList();
 
         String t_strText = text;
 
         try
         {
             if  (   (t_strText      != null)
                  && (separator != null))
             {
                 Matcher t_Matcher = RegexpManager.createMatcher();
 
                 MatchResult t_MatchResult = null;
 
                 if  (   (compiler != null)
                      && (t_Matcher  != null))
                 {
                     Pattern t_Pattern =
                         compiler.compile(
                             "(.*?)" + separator + "(.*)");
 
                     while  (   (!stringValidator.isEmpty(t_strText))
                             && (t_Matcher.contains(t_strText, t_Pattern)))
                     {
                         t_MatchResult = t_Matcher.getMatch();
 
                         result.add(t_MatchResult.group(1));
 
                         // the rest is parsed next.
                         t_strText = t_MatchResult.group(2);
                     }
                 }
                 else 
                 {
                     LogFactory.getLog(getClass()).error(
                           "regexp compiler (" + compiler + ") "
                         + "or matcher (" + t_Matcher + ")  unavailable");
                 }
             }
 
             if  (!stringValidator.isEmpty(t_strText))
             {
                 result.add(t_strText);
             }
         }
         catch  (final MalformedPatternException exception)
         {
             LogFactory.getLog(getClass()).error(
                 "Malformed pattern (possibly due to quote symbol conflict)",
                 exception);
         }
         catch  (final RegexpEngineNotFoundException exception)
         {
             /*
              * This exception is thrown only if no regexp library is available
              * at runtime. Not only this one, but any method provided by this
              * class that use regexps will not work.
              */
             LogFactory.getLog(getClass()).error(
                 "Cannot find any regexp engine.", exception);
         }
         
         return result;
     }
 
     /**
      * Extracts the most specific package name from given text.
      * @param text the text.
      * @return the leaf package name.
      */
     public String extractPackageName(final String text)
     {
         return capitalize(extractPackageGroup(text, 2), '_');
     }
 
     /**
      * Extracts the class name from given fully-qualified class identifier.
      * @param text the text.
      * @return the class name.
      */
     public String extractClassName(final String text)
     {
         return extractPackageGroup(text, 1);
     }
 
     /**
      * Extracts a concrete group in sub-package regexp from given text.
      * @param text the text.
      * @param group the group.
      * @return the selected group.
      * @precondition ((group == 1) || (group == 2))
      */
     protected String extractPackageGroup(
         final String text, final int group)
     {
         String result = text;
 
         try
         {
             if  (result != null)
             {
                 Matcher t_Matcher = RegexpManager.createMatcher();
 
                 MatchResult t_MatchResult = null;
 
                 Pattern t_SubPackagePattern = getSubPackagePattern();
 
                 if  (   (result != null)
                      && (result.trim().length() > 0)
                      && (t_SubPackagePattern != null)
                      && (t_Matcher.contains(
                              result, t_SubPackagePattern)))
                 {
                     t_MatchResult = t_Matcher.getMatch();
 
                     // the rest is parsed next.
                     result = t_MatchResult.group(group);
                 }
             }
         }
         catch  (final RegexpEngineNotFoundException exception)
         {
             /*
              * This exception is thrown only if no regexp library is available
              * at runtime. Not only this one, but any method provided by this
              * class that use regexps will not work.
              */
             LogFactory.getLog(getClass()).fatal(
                 "Cannot find any regexp engine.",
                 exception);
         }
         
         return result;
     }
 
     /**
      * Translates given package name to a relative file path.
      * @param packageName the pacjage name.
      * @return such path
      */
     public String packageToFilePath(final String packageName)
     {
         return packageToFilePath(packageName, StringValidator.getInstance());
     }
 
     /**
      * Translates given package name to a relative file path.
      * @param packageName the pacjage name.
      * @param stringValidator the StringValidator instance.
      * @return such path.
      * @precondition stringValidator != null
      */
     protected String packageToFilePath(
         final String packageName, final StringValidator stringValidator)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         try
         {
             if  (packageName != null)
             {
                 String t_strPackageName = packageName;
 
                 Matcher t_Matcher = RegexpManager.createMatcher();
 
                 MatchResult t_MatchResult = null;
 
                 boolean t_bMatched = false;
 
                 Pattern t_PackagePattern = getPackagePattern();
 
                 while  (   (!stringValidator.isEmpty(t_strPackageName))
                         && (t_PackagePattern != null)
                         && (t_Matcher.contains(
                                t_strPackageName, t_PackagePattern)))
                 {
                     t_MatchResult = t_Matcher.getMatch();
 
                     String t_strSubPattern = t_MatchResult.group(1);
 
                     if  (   (!stringValidator.isEmpty(t_strSubPattern))
                          && (!".".equals(t_strSubPattern)))
                     {
                         if  (t_bMatched)
                         {
                             t_sbResult.append(File.separator);
                         }
                         
                         t_sbResult.append(t_strSubPattern);
 
                         t_bMatched = true;
                     }
 
                     // the rest is parsed next.
                     t_strPackageName = t_MatchResult.group(2);
                 }
 
                 if  (   (!stringValidator.isEmpty(t_strPackageName))
                      && (!".".equals(t_strPackageName)))
                 {
                     if  (t_bMatched)
                     {
                         t_sbResult.append(File.separator);
                     }
 
                     t_sbResult.append(t_strPackageName);
                 }
             }
         }
         catch  (final RegexpEngineNotFoundException exception)
         {
             /*
              * This exception is thrown only if no regexp library is available
              * at runtime. Not only this one, but any method provided by this
              * class that use regexps will not work.
              */
             LogFactory.getLog(getClass()).fatal(
                 "Cannot find any regexp engine.", exception);
         }
 
         return t_sbResult.toString();
     }
 
     /**
      * Justifies given text to avoid exceeding specified margin,
      * if possible.
      * @param text the text to justify.
      * @param linePrefix the prefix to add to all justified lines.
      * @param margin the margin.
      * @return the justified text.
      */
     public String justify(final String text, final int margin)
     {
         return justify(text, "", margin);
     }
 
     /**
      * Justifies given text to avoid exceeding specified margin,
      * if possible.
      * @param text the text to justify.
      * @param linePrefix the prefix to add to all justified lines.
      * @param margin the margin.
      * @return the justified text.
      */
     public String justify(
         final String text, final String linePrefix, final int margin)
     {
         return
             justify(text, linePrefix, margin, StringValidator.getInstance());
     }
 
     /**
      * Justifies given text to avoid exceeding specified margin,
      * if possible.
      * @param text the text to justify.
      * @param linePrefix the prefix to add to all justified lines.
      * @param margin the margin.
      * @param stringValidator the StringValidator instance.
      * @return the justified text.
      * @precondition stringValidator != null
      */
     protected String justify(
         final String text,
         final String linePrefix,
         final int margin,
         final StringValidator stringValidator)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         String t_strText = text;
 
         try
         {
             StringValidator t_StringValidator =
                 StringValidator.getInstance();
 
             String t_strLinePrefix = linePrefix;
 
             if  (t_strText != null)
             {
                 if  (t_strLinePrefix == null)
                 {
                     t_strLinePrefix = "";
                 }
 
                 Matcher t_Matcher = RegexpManager.createMatcher();
 
                 MatchResult t_MatchResult = null;
 
                 Pattern t_JustifyPattern = getJustifyPattern();
 
                 String t_strCurrentLine = "";
 
                 String t_strCurrentWord = "";
 
                 while  (   (!stringValidator.isEmpty(text))
                         && (t_JustifyPattern != null)
                         && (t_Matcher.contains(
                                t_strText, t_JustifyPattern)))
                 {
                     t_MatchResult = t_Matcher.getMatch();
 
                     if  (t_MatchResult != null)
                     {
                         t_strCurrentWord = t_MatchResult.group(1);
 
                         if  (t_strCurrentWord != null)
                         {
                             if  (  t_strCurrentLine.length()
                                  + t_strCurrentWord.length()
                                  + 1
                                  > margin)
                             {
                                 t_sbResult.append(t_strCurrentLine);
 
                                 if  (t_strCurrentLine.length() > 0)
                                 {
                                     t_sbResult.append("\n");
                                 }
 
                                 t_strCurrentLine = t_strLinePrefix;
                             }
                             else 
                             {
                                 if  (t_strCurrentLine.length() > 0)
                                 {
                                     t_strCurrentLine += " ";
                                 }
                             }
 
                             t_strCurrentLine += t_strCurrentWord;
                         }
 
                         // the rest is parsed next.
                         t_strText = t_MatchResult.group(2);
                     }
                 }
 
                 t_strCurrentWord = t_strText;
 
                 t_sbResult.append(t_strCurrentLine);
 
                 if  (  t_strCurrentLine.length()
                      + t_strCurrentWord.length()
                      + 1
                      > margin)
                 {
                     if  (t_strCurrentLine.length() > 0)
                     {
                         t_sbResult.append("\n");
 
                         t_sbResult.append(t_strLinePrefix);
                     }
 
                     if  (t_strCurrentWord.length() > margin)
                     {
                         t_sbResult.append(t_strCurrentWord);
                         t_sbResult.append("\n");
                         t_sbResult.append(t_strLinePrefix);
                     }
                 }
                 else 
                 {
                     t_sbResult.append(" ");
                 }
 
                 t_sbResult.append(t_strCurrentWord);
             }
         }
         catch  (final RegexpEngineNotFoundException exception)
         {
             /*
              * This exception is thrown only if no regexp library is available
              * at runtime. Not only this one, but any method provided by this
              * class that use regexps will not work.
              */
             LogFactory.getLog(getClass()).fatal(
                 "Cannot find any regexp engine.", exception);
         }
 
         return t_sbResult.toString();
     }
 
     /**
      * Uncapitalizes the begining of given text.
      * @param input such input.
      * @return the processed input.
      * @precondition input != null
      * @precondition input.length() > 1
      */
     public String unCapitalizeStart(final String input)
     {
         return input.substring(0, 1).toLowerCase() + input.substring(1);
     }
 
     /**
      * Uncapitalizes given text, i.e. "thisTest"
      * to "this<code>[separator]</code>test".
      * @param input the input to process.
      * @param separator the separator.
      * @return the processed input.
      * @precondition input != null
      * @precondition separator != null
      */
     public String unCapitalize(final String input, final String separator)
     {
         return
             unCapitalize(
                 input,
                 separator,
                 StringValidator.getInstance(),
                 getUnCapitalizePattern(),
                 RegexpManager.createMatcher());
     }
 
     /**
      * Uncapitalizes given text, i.e. "thisTest"
      * to "this<code>[separator]</code>test".
      * @param input the input to process.
      * @param separator the separator.
      * @param stringValidator the StringValidator instance.
      * @param pattern the regexp pattern to use.
      * @param matcher the matcher instance to use.
      * @return the processed input.
      * @precondition input != null
      * @precondition separator != null
      * @precondition stringValidator != null
      * @precondition pattern != null
      * @precondition matcher != null
      */
     protected String unCapitalize(
         final String input,
         final String separator,
         final StringValidator stringValidator,
         final Pattern pattern,
         final Matcher matcher)
     {
         StringBuffer t_sbResult = new StringBuffer();
 
         try
         {
             String t_strTextToProcess = input;
 
             String t_strCurrentWord = "";
 
             MatchResult t_MatchResult = null;
 
             while  (   (!stringValidator.isEmpty(t_strTextToProcess))
                     && (matcher.contains(t_strTextToProcess, pattern)))
             {
                 t_MatchResult = matcher.getMatch();
 
                 if  (t_MatchResult != null)
                 {
                     t_strCurrentWord = t_MatchResult.group(1);
 
                     if  (!stringValidator.isEmpty(t_strCurrentWord))
                     {
                         t_sbResult.append(t_strCurrentWord);
                     }
 
                     // the rest is parsed next.
                     t_strTextToProcess = t_MatchResult.group(3);
 
                     if  (!stringValidator.isEmpty(t_strTextToProcess))
                     {
                         t_sbResult.append(separator);
                     }
 
                     String t_strUpperCaseLetter = t_MatchResult.group(2);
 
                     if  (!stringValidator.isEmpty(t_strUpperCaseLetter))
                     {
                         t_sbResult.append(t_strUpperCaseLetter.toLowerCase());
                     }
                 }
             }
 
             if  (!stringValidator.isEmpty(t_strTextToProcess))
             {
                 t_sbResult.append(t_strTextToProcess);
             }
         }
         catch  (final RegexpEngineNotFoundException exception)
         {
             /*
              * This exception is thrown only if no regexp library is available
              * at runtime. Not only this one, but any method provided by this
              * class that use regexps will not work.
              */
             LogFactory.getLog(getClass()).fatal(
                 "Cannot find any regexp engine.", exception);
         }
 
         return t_sbResult.toString();
     }
 
     /**
      * Adds given prefix and suffix to each line of given text.
      * <code>
      * applyToEachLine(" line 1   \n    and line 2", "||{0}{1}//").equals(
      *     "||line 1//\n||   and line 2//\n")
      * </code>
      * 0 - indentation spaces
      * 1 - trimmed text
      * @param text the text.
      * @param format the format.
      * @return the processed text.
      * @precondition text != null
      * @precondition format != null
      */
     public String applyToEachLine(
         final String text, final String format)
     {
         StringBuffer result = new StringBuffer();
 
         StringTokenizer t_StringTokenizer =
             new StringTokenizer(text, "\n", false);
 
         int t_iInitialIndent = retrieveMinimumIndentInAllLines(text);
 
         StringBuffer t_sbInitialIndent = new StringBuffer();
 
         for (int t_iIndex = 0; t_iIndex < t_iInitialIndent; t_iIndex++)
         {
             t_sbInitialIndent.append(" ");
         }
 
         String t_strInitialIndent = t_sbInitialIndent.toString();
 
         boolean t_bFirstLine = true;
 
         String t_strCurrentLine = null;
         String t_strTrimmedCurrentLine = null;
 
         MessageFormat t_Formatter = new MessageFormat(format);
 
         String t_strCurrentIndent = "";
 
         while  (t_StringTokenizer.hasMoreTokens())
         {
             t_strCurrentLine = t_StringTokenizer.nextToken();
 
             t_strTrimmedCurrentLine = t_strCurrentLine.trim();
 
             if  (!t_bFirstLine)
             {
                 t_iInitialIndent =
                     t_strCurrentLine.indexOf(t_strTrimmedCurrentLine);
 
                 if  (t_iInitialIndent > t_strInitialIndent.length())
                 {
                     t_strCurrentIndent =
                           t_strCurrentLine.substring(
                               t_strInitialIndent.length(), t_iInitialIndent);
                 }
                else
                {
                    t_strCurrentIndent = "";
                }
             }
 
             result.append(
                 t_Formatter.format(
                     new Object[]
                     {
                         t_strCurrentIndent,
                         t_strTrimmedCurrentLine
                     }));
 
             result.append("\n");
 
             if  (t_bFirstLine)
             {
                 t_bFirstLine = false;
             }
         }
 
         return result.toString();
     }
 
     /**
      * Finds out the minimum indent of all lines in given text.
      * <code>
      * retrieveMinimumIndentInAllLines(" line 1   \n    and line 2", "||" , "//") == 1
      * </code>
      * <code>
      * retrieveMinimumIndentInAllLines("    line 1   \n  .  and line 2", "||" , "//") == 2
      * </code>
      * @param text the text.
      * @return such information.
      * @precondition text != null
      */
     public int retrieveMinimumIndentInAllLines(final String text)
     {
         int result = -1;
 
         StringTokenizer t_StringTokenizer =
             new StringTokenizer(text, "\n", false);
 
         int t_iInitialIndent = -1;
 
         String t_strCurrentLine = null;
         String t_strTrimmedCurrentLine = null;
 
         while  (t_StringTokenizer.hasMoreTokens())
         {
             t_strCurrentLine = t_StringTokenizer.nextToken();
 
             t_strTrimmedCurrentLine = t_strCurrentLine.trim();
 
             t_iInitialIndent =
                 t_strCurrentLine.indexOf(t_strTrimmedCurrentLine);
 
             if  (   (   (t_iInitialIndent > 0)
                      && (t_iInitialIndent < result))
                  || (result == -1))
             {
                 result = t_iInitialIndent;
             }
         }
 
         if  (result == -1)
         {
             result = 0;
         }
 
         return result;
     }
 }
