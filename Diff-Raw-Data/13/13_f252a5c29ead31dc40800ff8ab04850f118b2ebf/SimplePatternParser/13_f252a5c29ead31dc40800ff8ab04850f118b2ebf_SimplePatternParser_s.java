 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.text;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Pattern;
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Simple pattern parser.
  *
  * <h3>Format</h3>
  *
  * <p>A simple pattern is a text string that may contain letters, digits,
  * underscores, hyphens, dots and the wildcard characters <code>'*'</code>
  * (asterisk) and <code>'?'</code> (question mark).
  *
  * <p>The location of an asterisk indicates any number of characters is
  * allowed. The location of a question mark indicates exactly one character is
  * expected.
  *
  * <p>To allow matching of simple patterns, a simple pattern is first compiled
  * into a Perl 5 regular expression. Every asterisk is converted to
  * <code>".*"</code>, while every question mark is converted to
  * <code>"."</code>.
  *
  * <h3>Examples</h3>
  *
  * <p>Examples of conversions from a simple pattern to a Perl 4 regular
  * expression:
  *
  * <table>
  *    <tr><th>Simple pattern</th><th>Perl 5 regex equivalent</th></tr>
  *    <tr><td></td>              <td></td>                     </tr>
  *    <tr><td>*</td>             <td>.*</td>                   </tr>
  *    <tr><td>?</td>             <td>.</td>                    </tr>
  *    <tr><td>_Get*</td>         <td>_Get.*</td>               </tr>
  *    <tr><td>_Get*i?n</td>      <td>_Get.*i.n</td>            </tr>
  *    <tr><td>*on</td>           <td>.*on</td>                 </tr>
  * </table>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  * @author Peter Troon (<a href="mailto:peter.troon@nl.wanadoo.com">peter.troon@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public class SimplePatternParser extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>SimplePatternParser</code> object.
     */
    public SimplePatternParser() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Converts the specified simple pattern to a Perl 5 regular expression.
     *
     * @param simplePattern
     *    the simple pattern, cannot be <code>null</code>.
     *
     * @return
     *    the Perl 5 regular expression, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>simplePattern == null</code>.
     *
     * @throws ParseException
     *    if provided simplePattern is invalid or could not be parsed.
     */
    public Perl5Pattern parseSimplePattern(String simplePattern)
    throws IllegalArgumentException, ParseException {
 
       MandatoryArgumentChecker.check("simplePattern", simplePattern);
 
       simplePattern = convertToPerl5RegularExpression(simplePattern);
 
       Perl5Pattern perl5pattern = null;
       Perl5Compiler perl5compiler = new Perl5Compiler();
 
       boolean parseError = false;
       try {
          Pattern pattern = perl5compiler.compile(simplePattern);
          if (pattern instanceof Perl5Pattern) {
             perl5pattern = (Perl5Pattern) pattern;
          } else {
             parseError = true;
          }
       } catch (
          MalformedPatternException mpe) {
          parseError = true;
       }
 
       if (parseError) {
          throw new ParseException("An error occurred while parsing the pattern '" + simplePattern + "'.");
       }
 
       return perl5pattern;
    }
 
    /**
     * Converts the pattern to a Perl 5 Regular Expression. This means that
     * every asterisk is replaced by a dot and an asterisk, every question mark
     * is replaced by a dot, an accent circunflex is prepended to the pattern
     * and a dollar sign is appended to the pattern.
     *
     * @param pattern
     *    the pattern to be converted, may not be <code>null</code>.
     *
     * @return
     *    the converted pattern, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>pattern == null</code>.
     *
     * @throws ParseException
     *    if provided simplePattern is invalid or could not be parsed.
     */
    private String convertToPerl5RegularExpression(String pattern)
    throws NullPointerException, ParseException {
 
       char[] contents = pattern.toCharArray();
       int size = contents.length;
       FastStringBuffer buffer = new FastStringBuffer(size * 2);
       char prevChar = (char) 0;
 
       for (int i= 0; i < size; i++) {
          char currChar = contents[i];
 
          if (currChar >= 'a' && currChar <= 'z') {
             buffer.append(currChar);
          } else if (currChar >= 'A' && currChar <= 'Z') {
             buffer.append(currChar);
          } else if (currChar >= '0' && currChar <= '9') {
             buffer.append(currChar);
          } else if (currChar == '_') {
             buffer.append(currChar);
          } else if (currChar == '-') {
             buffer.append(currChar);
          } else if (currChar == '.') {
             buffer.append("\\.");
          } else if ((currChar == '*' || currChar == '?') && (prevChar == '*' || prevChar == '?')) {
            throw new ParseException("The pattern \"" + pattern + "\" is invalid. It is not allowed to have two wildcard characters next to each other.");
          } else if (currChar == '*') {
             buffer.append(".*");
          } else if (currChar == '?') {
             buffer.append('.');
          } else if (currChar == ',') {
             buffer.append('|');
          } else {
             throw new ParseException("The pattern \"" + pattern + "\" is invalid. The character '" + currChar + "' is not allowed.");
          }
 
          prevChar = currChar;
       }
 
       return buffer.toString();
    }
 }
