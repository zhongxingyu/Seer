 package org.fiz;
 
 import java.util.*;
 
 /**
  * The Template class substitutes values from a Dataset into a template
  * string to produce a new string through a process called "expansion".
  * The goal for templates is to provide a simple mechanism for handling
  * the most common kinds of substitution; more complex forms, such as
  * iteration or recursion, are left to Java code.  Each character from
  * the template is copied to the output string except for the following
  * patterns, which cause substitutions:
  * @<i>name</i>             Copy the contents of the dataset element named
  *                          <i>name</i> to the output.  <i>name</i>
  *                          consists of all the standard Unicode identifier
  *                          characters following the {@code @}.  It is an
  *                          error if the name doesn't exist in the dataset.
  * @(<i>name</i>)           Copy the contents of the data value named
  *                          <i>name</i> to the output.  <i>name</i>
  *                          consists of all the characters following the "("
  *                          up to the next ")".  {@code @} can be used
  *                          between the parentheses to perform substitutions
  *                          on the name; for example, {@code @(@foo)} finds
  *                          dataset element {@code foo}, uses its value as the
  *                          name of another dataset element, and substitutes
  *                          the value of that element into the output.  It is
  *                          an error if the name doesn't exist in the dataset.
  * @<i>name</i>?{...}       Default: if <i>name</i> exists in the dataset,
  *                          copy its contents to the output and skip over
  *                          {@code ?{...}}.  Otherwise, performed template
  *                          expansion on the the text between the braces.
  * @<i>name</i>?{<i>t1</i>|<i>t2</i>}    Choice: if <i>name</i> exists in the
  *                          dataset perform template expansion on <i>t1</i>.
  *                          Otherwise perform template expansion on <i>t2</i>.
  * {{...}}                  Conditional substitution: normally the information
  *                          between the braces is processed just like the rest
  *                          of the template, except that the braces are not
  *                          copied to the output.  However, if there is a
  *                          data reference for which the name doesn't exist
  *                          then the information between the braces skipped:
  *                          nothing is copied to the output.  Furthermore, if
  *                          there are space characters next to either of the
  *                          curly braces then one of the spaces may be
  *                          removed to avoid reduncant spaces (enclose the
  *                          space in {{}} to keep this from happening.
  * @@                       Append "@" to the output.
  * @{                       Append "{" to the output.
  * @}                       Append "}" to the output.
  * @*                       Any other occurrence of "@" besides those
  *                          described above is illegal and results in an error.
  * When a dataset element is substituted into a template, special characters
  * in the value will be escaped according to a SpecialChars enum supplied
  * during expansion.  For example, if HTML encoding has been specified,
  * {@code <} characters will be translated to the entity reference
  * {@code &lt;}.  Translation occurs only for values coming from the
  * dataset, not for characters in the template itself;  you should ensure
  * that template characters already obey the output encoding rules.
  * Translation can be disabled for dataset values by specifying {@code NONE}
  * as the encoding.
  *
  * Some forms of the {@code expand} method allow you to provide data values
  * using one or more Objects instead of a Dataset.  In this case, you use
  * numerical specifiers such as {@code @3} to refer to the values:
  * {@code @1} refers to the string value of the first object, {@code @2}
  * refers to the string value of the second object, and so on.  In some
  * cases you can provide both a dataset and Objects, in which case a
  * specifier such as {@code @2} refers to an object, if there is one
  * corresponding to that index, otherwise it is looked up in the dataset.
  */
 
 public class Template {
     /**
      * MissingValueError is thrown when a dataset element required by a
      * template does not exist.
      */
     public static class MissingValueError extends Error {
         /**
          * Constructs a MissingValueError with a given message.
          * @param message          Detailed information about the problem
          */
         public MissingValueError(String message) {
             super(message);
         }
     }
 
     /**
      * SyntaxError is thrown when there is an incorrect construct in a
      * template, such as an {@code @} followed by an unrecognized character.
      */
     public static class SyntaxError extends Error {
         /**
          * Constructs a SyntaxError with a given message.
          * @param message          Detailed information about the problem
          */
         public SyntaxError(String message) {
             super(message);
         }
     }
 
     /**
      * Instances of this enum indicate how to escape special characters
      * in data values incorporated into template output.
      */
     public enum SpecialChars {
         /**
          * The output will be used in HTML, so replace special HTML
          * characters such as {@code <} with HTML entities such as
          * {@code &lt;}.
          */
         HTML,
 
         /**
          * The output will be used as Javascript code; assume that all
          * substituted data values will be used in Javascript strings,
          * so use backslashes to quote special characters.
          */
         JAVASCRIPT,
 
         /**
          * The output will be used as part of a URL, so use {@code %xx}
          * encoding for any characters that aren't permitted in URLs.
          */
         URL,
 
         /** Don't perform any transformations on the data values. */
         NONE}
 
     // The following class is used internally to pass information between
     // methods.  Among other things, it provides a vehicle for returning
     // additional results from a method.
     protected static class ParseInfo {
         CharSequence template;     // The template being processed.
         int templateEnd;           // Stop processing template characters
                                    // after processing the character just
                                    // before this one;  used to selectively
                                    // process portions of the template.
         Dataset data;              // Source of data for expansions.
         StringBuilder out;         // Output information is appended here.
         SpecialChars quoting;      // How to transform special characters
                                    // in substituted values from {@code data}.
                                    // Null means we are doing SQL template
                                    // expansion.
         Object[] indexedData;      // Used in some forms of {@code expand}:
                                    // provides additional data referenced with
                                    // numerical specifiers such as {@code @1}.
         ArrayList<String> sqlParameters;
                                    // Non-null means we are expanding an SQL
                                    // template, so variables don't get
                                    // substituted into the output string: this
                                    // object (provided by the caller) collects
                                    // the values of all of the variables,
                                    // which are then added to the SQL statement
                                    // by the caller.
         SpecialChars indexedQuoting;
                                    // How to transform special characters when
                                    // substituting values from
                                    // {@code indexedData}.  Null means we are
                                    // doing SQL template expansion.
         SpecialChars currentQuoting;
                                    // Set by {@code findValue}: either
                                    // {@code quoting} or {@code indexedQuoting}.
         boolean ignoreErrors;      // True means we are processing information
                                    // between curly braces, so don't get upset
                                    // if data values can't be found.
         boolean missingData;       // True means that a data value couldn't
                                    // be found but ignoreErrors was true.
         boolean skip;              // True means we are parsing part of the
                                    // template without actually expanding it
                                    // (e.g. in a @foo?{...|...} substitution)
                                    // so don't bother looking up or copying
                                    // data values.  Skip other copying also,
                                    // if it's convenient; anything copied to
                                    // {@code out} will be discarded.
         int end;                   // Modified by methods such as
                                    // expandName to hold the index of the
                                    // character just after the last one
                                    // processed by the method.  For example,
                                    // if the method processed "@(name)"
                                    // then this value will give the index
                                    // of the next character after the ")".
         int lastDeletedSpace;      // Used in collapsing space characters
                                    // around braces to handle cumulative
                                    // situations like "<{@a} {@b} {@c}>":
                                    // if all three sets in braces drop out,
                                    // we want to eliminate exactly 2 spaces.
                                    // This field holds the index into the
                                    // template of the last space character
                                    // we collapsed out, or -1 if none.
     }
 
     // No constructor: this class only has a static methods.
     private Template() {}
 
     /**
      * Substitute data into a template string, appending the result to a
      * StringBuilder.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param out                  The expanded template is appended here.
      * @param quoting              Determines whether (and how) special
      *                             characters in data values are quoted.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void expand(CharSequence template, Dataset data,
             StringBuilder out, SpecialChars quoting)
             throws MissingValueError, SyntaxError {
         ParseInfo info = new ParseInfo();
         info.template = template;
         info.data = data;
         info.out = out;
         info.quoting = quoting;
         info.ignoreErrors = false;
         info.missingData = false;
         info.lastDeletedSpace = -1;
         expandRange(info, 0, template.length());
     }
 
     /**
      * Substitute data into a template string and return the expanded result.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param quoting              Determines whether (and how) special
      *                             characters in data values are quoted.
      * @return                     The result produced by expanding the
      *                             template.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static String expand(CharSequence template, Dataset data,
             SpecialChars quoting)
             throws MissingValueError, SyntaxError {
         StringBuilder out = new StringBuilder(template.length() + 50);
         expand(template, data, out, quoting);
         return out.toString();
     }
 
     /**
      * Substitute data into a template string, appending the result to a
      * StringBuilder using HTML encoding.
      * @param template             Contains text to be copied to
      *                             {@code out} plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param out                  The expanded template is appended here.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void expand(CharSequence template, Dataset data,
             StringBuilder out) throws MissingValueError, SyntaxError {
         expand(template, data, out, SpecialChars.HTML);
     }
 
     /**
      * Substitute data into a template string and return the expanded result;
      * use HTML quoting.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @return                     The result produced by expanding the
      *                             template.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static String expand(CharSequence template, Dataset data)
             throws MissingValueError, SyntaxError {
         StringBuilder out = new StringBuilder(template.length() + 50);
         expand(template, data, out, SpecialChars.HTML);
         return out.toString();
     }
 
     /**
      * Substitute data into a template string using values from both a
      * dataset and a collection of objects, appending to a StringBuilder.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param out                  The expanded template is appended here.
      * @param quoting              Determines how to handle special characters
      *                             in values retrieved from {@code data}.
      * @param indexedQuoting       Determines how to handle special characters
      *                             in values retrieved from
      *                             {@code indexValues}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void expand(CharSequence template, Dataset data,
             StringBuilder out, SpecialChars quoting,
             SpecialChars indexedQuoting, Object ... indexedData)
             throws MissingValueError, SyntaxError {
         ParseInfo info = new ParseInfo();
         info.template = template;
         info.data = data;
         info.out = out;
         info.quoting = quoting;
         info.indexedData = indexedData;
         info.sqlParameters = null;
         info.indexedQuoting = indexedQuoting;
         info.ignoreErrors = false;
         info.missingData = false;
         info.lastDeletedSpace = -1;
         expandRange(info, 0, template.length());
     }
 
     /**
      * Substitute data into a template string using values from both a
      * dataset and a collection of objects and return the expanded result.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param quoting              Determines how to handle special characters
      *                             in values retrieved from {@code data}.
      * @param indexedQuoting       Determines how to handle special characters
      *                             in values retrieved from
      *                             {@code indexValues}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @return                     The result produced by expanding the
      *                             template.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static String expand(CharSequence template, Dataset data,
             SpecialChars quoting, SpecialChars indexedQuoting,
             Object ... indexedData) throws MissingValueError, SyntaxError {
         StringBuilder out = new StringBuilder(template.length() + 50);
         expand(template, data, out, quoting, indexedQuoting, indexedData);
         return out.toString();
     }
 
     /**
      * Substitute data into a template string using values from both a
      * dataset and a collection of objects, with HTML encoding everywhere,
      * and append the result to a StringBuilder.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param out                  The expanded template is appended here.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void expand(CharSequence template, Dataset data,
             StringBuilder out, Object ... indexedData)
             throws MissingValueError, SyntaxError {
         expand(template, data, out, Template.SpecialChars.HTML,
                 Template.SpecialChars.HTML, indexedData);
     }
 
     /**
      * Substitute data into a template string using values from both a
      * dataset and a collection of objects, with HTML encoding everywhere,
      * and return the expanded result.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @return                     The result produced by expanding the
      *                             template.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static String expand(CharSequence template, Dataset data,
             Object ... indexedData) throws MissingValueError, SyntaxError {
         StringBuilder out = new StringBuilder(template.length() + 50);
         expand(template, data, out, Template.SpecialChars.HTML,
                 Template.SpecialChars.HTML, indexedData);
         return out.toString();
     }
 
     /**
      * Substitute data into a template string using values from a collection
      * of objects (no dataset) and append the result to a StringBuilder.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param out                  The expanded template is appended here.
      * @param quoting              Determines how to handle special characters
      *                             in substituted data values.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void expand(CharSequence template, StringBuilder out,
             SpecialChars quoting, Object ... indexedData)
             throws MissingValueError, SyntaxError {
         expand(template, null, out, null, quoting, indexedData);
     }
 
     /**
      * Substitute data into a template string using values from a collection
      * of objects (no dataset) and return the result.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param quoting              Determines how to handle special characters
      *                             in substituted data values.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @return                     The result produced by expanding the
      *                             template.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static String expand(CharSequence template, SpecialChars quoting,
             Object ... indexedData) throws MissingValueError, SyntaxError {
         StringBuilder out = new StringBuilder(template.length() + 50);
         expand(template, null, out, null, quoting, indexedData);
         return out.toString();
     }
 
     /**
      * Substitute data into a template string using values from a collection
      * of objects (no dataset) with HTML encoding and append the result to
      * a StringBuilder.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param out                  The expanded template is appended here.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static void expand(CharSequence template, StringBuilder out,
             Object ... indexedData) throws MissingValueError, SyntaxError {
         expand(template, null, out, null, Template.SpecialChars.HTML,
                 indexedData);
     }
 
     /**
      * Substitute data into a template string using values from a collection
      * of objects (no dataset) with HTML encoding and return the result.
      * @param template             Contains text plus substitution
      *                             specifiers such as {@code @foo}.
      * @param indexedData          One or more objects, whose values can be
      *                             referred to in the template with
      *                             numerical specifiers such as {@code @1}.
      *                             Null values may be supplied to indicate
      *                             "no object with this index".
      * @return                     The result produced by expanding the
      *                             template.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static String expand(CharSequence template, Object ... indexedData)
             throws MissingValueError, SyntaxError {
         StringBuilder out = new StringBuilder(template.length() + 50);
         expand(template, null, out, null, Template.SpecialChars.HTML,
                 indexedData);
         return out.toString();
     }
 
     /**
      * Substitute data into a template SQL query and return the expanded
      * result.  Because of the way variables are handled in SQL queries,
      * variable values are not substituted directly into the query.  Instead,
      * each substitution causes a "?" character to appear in the output query.
      * The variable values are collected in a separate ArrayList for the
      * caller, which will then invoke a JDBC method to attach them to the
      * SQL statement for the query.
      * @param template             Contains an SQL query that may contain
      *                             substitution specifiers such as
      *                             {@code @foo}.
      * @param data                 Provides data to be substituted into the
      *                             template.
      * @return                     The result produced by expanding the
      *                             template.
      * @throws MissingValueError   A required data value couldn't be found.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     public static String expandSql(CharSequence template, Dataset data,
             ArrayList<String> sqlParameters)
             throws MissingValueError, SyntaxError {
         ParseInfo info = new ParseInfo();
         info.template = template;
         info.data = data;
         info.out = new StringBuilder(template.length() + 50);
         info.quoting = null;
         info.sqlParameters = sqlParameters;
         info.indexedQuoting = null;
         info.ignoreErrors = false;
         info.missingData = false;
         info.lastDeletedSpace = -1;
         expandRange(info, 0, template.length());
         return info.out.toString();
     }
 
     /**
      * Given a range of characters in a template, expand the characters
      * in the range.
      * @param info                 Contains information about the template
      *                             being expanded.
      * @param start                Index of the first character to expand.
      * @param end                  Index of the character just after the last
      *                             one to expand.
      * @throws MissingValueError   Thrown if a data value couldn't be found
      *                             and info.ignoreErrors is false.
      * @throws SyntaxError         The template contains an illegal construct.
      */
     protected static void expandRange(ParseInfo info, int start, int end)
             throws MissingValueError, SyntaxError {
         CharSequence template = info.template;
         int oldTemplateEnd = info.templateEnd;
         info.templateEnd = end;
 
         for (int i = start; i < info.templateEnd; ) {
             char c = template.charAt(i);
             if (c == '@') {
                 expandAtSign(info, i+1);
                 i = info.end;
             } else if ((c == '{') && ((i+1) < info.templateEnd)
                     && (template.charAt(i+1) == '{')) {
                 expandBraces(info, i+2);
                 i = info.end;
             } else {
                 info.out.append(c);
                 i++;
             }
         }
         info.templateEnd = oldTemplateEnd;
     }
 
     /**
      * This method is invoked to process the part of a template immediately
      * following an "@".
      * @param info                 Contains information about the template
      *                             being expanded.  This method modifies
      *                             info.out, info.missingInfo, and info.end.
      *                             Info.end is set to the index of the first
      *                             character following the @-specifier (e.g.
      *                             for {@code @foo+bar} info.end will refer
      *                             to the {@code +} and for {@code@{abc}
      *                             info.end will refer to the "a".
      * @param start                Index of the character immediately after
      *                             the {@code @}.
      * @throws MissingValueError   Thrown if a data value couldn't be found
      *                             and info.ignoreErrors is false.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     protected static void expandAtSign(ParseInfo info, int start)
             throws MissingValueError {
         if (start >= info.templateEnd) {
             throw new SyntaxError("dangling \"@\" in template \"" +
                     info.template + "\"");
         }
         char c = info.template.charAt(start);
         if (Character.isUnicodeIdentifierStart(c) || Character.isDigit(c)) {
             info.end = StringUtil.identifierEnd(info.template, start);
             String name = info.template.subSequence(start,
                     info.end).toString();
             if ((info.end < info.templateEnd)
                     && (info.template.charAt(info.end) == '?'))  {
                 expandChoice(info, name, info.end+1);
             } else {
                 appendValue(info, name);
             }
         } else if (c == '(') {
             expandParenName(info, start+1);
         } else if ((c == '@') || (c == '{') || (c == '}')) {
             info.out.append(c);
             info.end = start+1;
         } else {
             throw new SyntaxError("invalid sequence \"@" + c
                     + "\" in template \"" + info.template + "\"");
         }
     }
 
     /**
      * This method is invoked to expand substitutions  that start with
      * {@code @name?}.
      * @param info                 Overall information about the template
      *                             expansion.  This method appends text to
      *                             info.out and sets info.end to the index
      *                             of the first character after the end of
      *                             this substitution.  Info.missingInfo may
      *                             also get set.
      * @param name                 The name of the variable (everything
      *                             between the "@" and the "?").
      * @param start                Index in info.template of the character
      *                             just after the "?".
      * @throws MissingValueError   Thrown if a data value couldn't be found
      *                             and info.ignoreErrors is false.
      * @throws SyntaxError         The template contains an illegal construct.
      */
     protected static void expandChoice(ParseInfo info, String name,
             int start) throws MissingValueError, SyntaxError {
         CharSequence template = info.template;
         if ((start >= info.templateEnd) || (template.charAt(start) != '{')) {
             throw new SyntaxError("missing \"{\" after \"?\" " +
                     "in template \"" + template + "\"");
         }
         int first = start + 1;
         int second = skipTo(info, start, '|', '}');
         if (second >= info.templateEnd) {
             throw new SyntaxError("incomplete @...?{...} substitution " +
                     "in template \"" + template + "\"");
         }
         if (template.charAt(second) == '}') {
             // This substitution has the form "@name?{template}".
             if (findValue(info, name, false) != null) {
                 appendValue(info, name);
             } else {
                 expandRange(info, first, second);
             }
             info.end = second+1;
         } else {
             // This substitution has the form "@name?{template1|template2}".
             int third = skipTo(info, start, '}', '}');
             if (third >= info.templateEnd) {
                 throw new SyntaxError("incomplete @...?{...} substitution " +
                         "in template \"" + template + "\"");
             }
             if (findValue(info, name, false) != null) {
                 expandRange(info, first, second);
             } else {
                 expandRange(info, second+1, third);
             }
             info.end = third+1;
         }
     }
 
     /**
      * This method is invoked to expand parenthesized names, such as
      * @(@first+@second).
      * @param info                 Contains information about the template
      *                             being expanded.  This method modifies
      *                             info.out, info.missingInfo, and info.end.
      *                             Info.end will be set to the index of
      *                             the character just after the closing
      *                             parenthesis.
      * @param start                Index of the character immediately after
      *                             the "@(".
      * @throws MissingValueError   Thrown if a data value couldn't be found
      *                             and info.ignoreErrors is false.
      * @throws SyntaxError         The template contains an illegal construct
      *                             such as {@code @+}.
      */
     protected static void expandParenName(ParseInfo info, int start)
             throws MissingValueError, SyntaxError {
         // Note: we use info.out to collect the name of the dataset value,
         // then erase this from info.out before appending the value.
         int oldLength = info.out.length();
 
         // While we are collecting the name, we don't want to do any
         // special-character handling.  To accomplish this, temporarily
         // change the encoding (then change it back before processing the
         // final data value).
         SpecialChars oldQuoting = info.quoting;
         info.quoting = SpecialChars.NONE;
 
         CharSequence template = info.template;
         for (int i = start; i < info.templateEnd; ) {
             char c = template.charAt(i);
             if (c == '@') {
                 expandAtSign(info, i+1);
                 i = info.end;
             } else if (c == ')') {
                 info.end = i+1;
                 String name = info.out.substring(oldLength);
                 info.out.setLength(oldLength);
                 info.quoting = oldQuoting;
                 appendValue(info, name);
                 return;
             } else {
                 info.out.append(c);
                 i++;
             }
         }
         throw new SyntaxError("missing \")\" for \"@(\" in template \""
                 + info.template + "\"");
     }
 
     /**
      * Given the name of a dataset element, this method appends the value
      * of that element to the output string.
      * @param info                 Contains information about the template
      *                             being expanded.  This method modifies
      *                             info.out and info.missingInfo.
      * @param name                 Name of the desired dataset element.
      * @throws MissingValueError   Thrown if the data value couldn't be found
      *                             and info.ignoreErrors is false.
      */
     protected static void appendValue(ParseInfo info, String name)
             throws MissingValueError {
         String value;
         if (info.skip) {
             return;
         }
         if (info.ignoreErrors) {
             value = findValue(info, name, false);
             if (value == null) {
                 info.missingData = true;
                 return;
             }
         } else {
             value = findValue(info, name, true);
         }
         if (info.currentQuoting == SpecialChars.HTML) {
             Html.escapeHtmlChars(value, info.out);
         } else if (info.currentQuoting == SpecialChars.JAVASCRIPT) {
             Html.escapeStringChars(value, info.out);
         } else if (info.currentQuoting == SpecialChars.URL) {
             Html.escapeUrlChars(value, info.out);
         } else if (info.currentQuoting == SpecialChars.NONE) {
             info.out.append(value);
         } else if (info.sqlParameters != null) {
             // The template being expanded is an SQL query.  Substitute
             // "?" into the query, and save the variable value so that
             // the caller can add it to the SQL statement.
             info.sqlParameters.add(value);
             info.out.append("?");
         } else {
             throw new InternalError("unknown quoting value in " +
                     "Template.appendValue");
         }
     }
 
     /**
      * This method is invoked to expand a portion of a template that
      * lies between double curly braces.
      * @param info                 Contains information about the template
      *                             being expanded.  This method modifies
      *                             info.out, info.missingInfo, and info.end.
      *                             Info.end will be set to the index of
      *                             the character just after the closing
      *                             braces.
      * @param start                Index of the character immediately after
      *                             the "{{".
      * @throws SyntaxError         The template is illegal, e.g. it doesn't
      *                             contain closing braces.
      */
     protected static void expandBraces(ParseInfo info, int start)
             throws SyntaxError {
         info.ignoreErrors = true;
         info.missingData = false;
         int oldEnd = info.out.length();
         int oldSqlParametersSize = (info.sqlParameters != null) ?
                 info.sqlParameters.size() : 0;
         CharSequence template = info.template;
 
         for (int i = start; i < info.templateEnd; ) {
             char c = template.charAt(i);
             if (c == '@') {
                 expandAtSign(info, i+1);
                 i = info.end;
             } else if ((c == '}') && ((i+1) < info.templateEnd)
                     && (template.charAt(i+1) == '}')) {
                 info.end = i+2;
                 if (info.missingData) {
                     // Some of the required data was not available, so
                     // undo all of the output we have generated.  Furthermore,
                     // if omitting the material in braces results in redundant
                     // spaces, then eliminate the space character on one side
                     // of the braces.
 
                     char next;
                     int indexBeforeBraces = start-3;
 
                     // If there is a space before the "{{" that hasn't already
                     // been deleted and the character after the "}}" is
                     // either a space, a close-delimiter such as ">", or the
                     // end of the template, delete the space before the "{{".
                     if (info.end < info.templateEnd) {
                         next = template.charAt(info.end);
                     } else {
                         // The "}}" is at the end of the template, so pretend
                         // the character after the "}}" is a close-delimiter.
                         next = '>';
                     }
                     if ((info.lastDeletedSpace < indexBeforeBraces)
                             && (template.charAt(indexBeforeBraces) == ' ')
                             && ((next == ' ') || (next == ']')
                             || (next == '>') || (next == '}')
                             || (next == ')') || (next == '\"')
                             || (next == '\''))) {
                         info.lastDeletedSpace = indexBeforeBraces;
                         oldEnd--;
                     } else {
                         // If there is a space after the "}}" and the
                         // character before the "{{" is an open-delimiter
                         // such as "<" or the beginning of the string, then
                         // skip over the trailing space.
                         char prev;
                         if (start >= 3) {
                             prev = template.charAt(indexBeforeBraces);
                         } else {
                             // The "{{" is at the beginning of the template,
                             // so pretend the previous character is an
                             // open-delimiter.
                             prev = '<';
                         }
                         if ((next == ' ') && ((prev == '[')
                                 || (prev == '<') || (prev == '{')
                                 || (prev == '(') || (prev == '\"')
                                 || (prev == '\''))) {
                             info.lastDeletedSpace = info.end;
                             info.end++;
                         }
                     }
                     info.out.setLength(oldEnd);
                     if (info.sqlParameters != null) {
                         for (int size = info.sqlParameters.size();
                                 size > oldSqlParametersSize; ) {
                             size--;
                             info.sqlParameters.remove(size);
                         }
                     }
                 }
                 info.ignoreErrors = false;
                 return;
             } else {
                 info.out.append(c);
                 i++;
             }
         }
         throw new SyntaxError("unmatched \"{{\" in template \""
                 + info.template + "\"");
     }
 
     /**
      * This method is used to skip over parts of the template; it is
      * used to parse parts of the template that may or may not actually
      * be expanded.  Given a starting position, it scans forward to find
      * the next unquoted character at this level that matches either of two
      * input characters (characters that are part of a {@code @} or
      * {@code {{...}} structure are skipped}.  This method does not
      * reference any data values or copy any information to the output.
      * @param info                 Information about the template
      *                             expansion.
      * @param start                First character to check for punctuation.
      * @param c1                   Desired character: stop when this
      *                             character is found.
      * @param c2                   Another desired character: stop when this
      *                             character is found.
      * @return                     The index of the first unquoted character
      *                             equal to c1 or c2, or info.templateEnd
      *                             if no such character is found.
      */
     protected static int skipTo(ParseInfo info, int start, char c1,
             char c2) {
         CharSequence template = info.template;
         int i;
 
         // This method invokes the same procedures that do actual
         // expansions, except that (a) we set the "skip" flag to avoid
         // some operations, and (b) we record some of the state of the
         // expansion and restore it the end to cancel any other side
         // effects.
         boolean oldSkip = info.skip;
         info.skip = true;
         int oldLength = info.out.length();
 
         for (i = start; i < info.templateEnd; ) {
             char c = template.charAt(i);
             if (c == '@') {
                 expandAtSign(info, i+1);
                 i = info.end;
             } else if ((c == '{') && ((i+1) < info.templateEnd)
                     && (template.charAt(i+1) == '{')) {
                 expandBraces(info, i+2);
                 i = info.end;
             } else if ((c == c1) || (c == c2)) {
                 break;
             } else {
                 i++;
             }
         }
 
         // Discard anything that was appended to the output during this
         // method and restore the other state that we saved.
         info.out.setLength(oldLength);
         info.skip = oldSkip;
         return i;
     }
 
     /**
      * Given the name of a value to be substituted, find the value.  In
      * addition to returning the value, this method sets info.currentQuoting
      * to indicate the appropriate form of special character handling to use
      * for this value.
      * @param info                 Information about the template
      *                             expansion.
      * @param name                 Name of the desired value.
      * @param required             True means throw an error if the value
      *                             can't be found
      * @return                     The value corresponding to {@code name}.
      * @throws MissingValueError   The desired value could not be found
      *                             and {@code requaired} was true.
      */
     protected static String findValue(ParseInfo info, String name,
             boolean required) throws MissingValueError {
 
         // If there is indexed data in this expansion and the name is an
         // integer, use an indexed value if there is one corresponding to
         // the name.
         if (info.indexedData != null) {
             try {
                 int index = Integer.parseInt(name);
                 if ((index >= 1) && (index <= info.indexedData.length)) {
                     info.currentQuoting = info.indexedQuoting;
                     Object value = info.indexedData[index-1];
                     if (value != null) {
                         return value.toString();
                     }
                 }
             }
             catch (NumberFormatException e) {
                 // The name isn't a number, so move on and try the dataset.
             }
         }
 
         String result = null;
         if (info.data != null) {
             info.currentQuoting = info.quoting;
             result = info.data.check(name);
         }
         if ((result != null) || !required) {
             return result;
         }
         throw new MissingValueError("missing value \"" + name + "\" " +
                 "in template \"" + info.template + "\"");
     }
 }
