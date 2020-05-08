 /**
  * The contents of this file are subject to the Open Software License
  * Version 3.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.opensource.org/licenses/osl-3.0.txt
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  */
 package org.mulgara.query.filter;
 
 import java.util.regex.Pattern;
 import static java.util.regex.Pattern.*;
 
 import org.mulgara.query.QueryException;
 import org.mulgara.query.filter.value.ValueLiteral;
 
 
 /**
  * The regular expression test for values.
  * TODO: Move this on to Xalan Regex functions as these are fully compliant with SPARQL,
  * while the Java ones are not.
  *
  * @created Mar 8, 2008
  * @author Paul Gearon
  * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
  */
 public class RegexFn extends BinaryTestFilter {
 
   /** Generated Serialization ID for RMI */
   private static final long serialVersionUID = 6785353529347360357L;
 
   /** a cache of the last pattern */ 
   private Pattern pattern = null;
 
   /** The expression that resolves flags */
   private ValueLiteral flagExpression = null;
 
   /** a cache of the last flag string */
   private String flagsStr = null;
 
   /** a cache of the last flags */
   private int flags = 0;
 
   /**
    * Creates an equality test operation with default flags
    * @param lhs The first term to compare
    * @param rhs The second term to compare
    */
   public RegexFn(ValueLiteral str, ValueLiteral patternStr) {
     super(str, patternStr);
   }
 
   /**
    * Creates an equality test operation with default flags
    * @param lhs The first term to compare
    * @param rhs The second term to compare
    */
   public RegexFn(ValueLiteral str, ValueLiteral patternStr, ValueLiteral flagExpression) {
     super(str, patternStr);
     this.flagExpression = flagExpression;
    if (flagExpression != null) flagExpression.setContextOwner(this);
   }
 
   /** @see org.mulgara.query.filter.BinaryTestFilter#testCmp() */
   boolean testCmp() throws QueryException {
     return pattern().matcher(str()).matches();
   }
 
   /**
    * Gets the string to be matched in this regular expression.
    * @return The string to be matched against.
    * @throws QueryException If the expression for the string cannot be resolved.
    */
   private String str() throws QueryException {
     if (!lhs.isLiteral() || !((ValueLiteral)lhs).isSimple()) throw new QueryException("Type Error: Invalid type in regular expression. Need string, got: " + lhs.getClass().getSimpleName());
     return ((ValueLiteral)lhs).getLexical();
   }
 
   /**
    * Gets the Pattern to use for the current variable bindings. This will calculate a new pattern
    * and flags if either change for the current variable bindings.
    * @return A Pattern for regex matching, using the existing pattern if there was no update.
    * @throws QueryException If the pattern string or flags string cannot be resolved.
    */
   private Pattern pattern() throws QueryException {
     if (!rhs.isLiteral() || !((ValueLiteral)rhs).isSimple()) throw new QueryException("Type Error: Invalid pattern type in regular expression. Need string, got: " + rhs.getClass().getSimpleName());
     String patternStr = ((ValueLiteral)rhs).getLexical();
     int oldFlags = flags;
     // note that the call to flags has a side-effect
     if (oldFlags != flags() || pattern == null || !patternStr.equals(pattern.pattern())) {
       pattern = Pattern.compile(patternStr, flags);
     }
     return pattern;
   }
 
   /** Characters used for regex flags */
   private static final String optionChars = "smix";
   /** Regex flags that correspond to the optionChars */
   private static final int[] optionFlags = new int[] { DOTALL, MULTILINE, CASE_INSENSITIVE, COMMENTS };
 
   /**
    * Gets the flags to use for this regex call. This will calculate new flags is the expression
    * the flags come from is updated.
    * @return An int with the flags for the current binding. Returns 0 if no flags are to be used.
    * @throws QueryException The expression the flags are built on cannot be resolved.
    */
   private int flags() throws QueryException {
     if (flagExpression == null) return 0;
     if (!flagExpression.isLiteral() || !((ValueLiteral)flagExpression).isSimple()) throw new QueryException("Type Error: Invalid flags in regular expression. Need string, got: " + rhs.getClass().getSimpleName());
     String currentFlagStr = flagExpression.getLexical();
     if (flagsStr == null || !flagsStr.equals(currentFlagStr)) {
       flagsStr = currentFlagStr;
       // calculate the new flags
       flags = 0;
       for (int i = 0; i < optionChars.length(); i++) {
         if (flagsStr.indexOf(optionChars.charAt(i)) != -1) flags |= optionFlags[i];
       }
     }
     return flags;
   }
   
 }
