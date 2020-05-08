 /**
  * Copyright 2010 55 Minutes (http://www.55minutes.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package fiftyfive.util;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Truncates strings using a configurable set of rules. For a reasonable
  * default ruleset, use the empty constructor:
  * <pre class="example">
  * new TruncateHelper().truncate("My really long string that needs to be cut down to size.", 50);
  * // Result: "My really long string that needs to be cut down…"</pre>
  * 
  * @see #truncate
  */
public class TruncateHelper implements java.io.Serializable
 {
     private String _suffix = "…";
     private boolean _trimFirst = true;
     private boolean _compressWhiteSpace = true;
     private int _breakWordLongerThan = 10;
     private Pattern _wordPattern = Pattern.compile("[^\\s\\-–—]");
     private Pattern _wordDelimeterPattern = Pattern.compile("[\\s\\-–—]");
     
     /**
      * Constructs a TruncateHelper with a reasonable set of defaults.
      * <pre class="example">
      * suffix = "…"
      * trimFirst = true
      * compressWhiteSpace = true
      * breakWordLongerThan = 10
      * wordPattern = [^\s\-–—]
      * wordDelimeterPattern = [\s\-–—]</pre>
      */
     public TruncateHelper()
     {
         super();
     }
     
     /**
      * Intelligently shortens a string to the specified maximum number of
      * characters. The shortened string will be no longer than the maximum,
      * including the ellipsis. Examples:
      * <pre class="example">
      * truncate("  Already short enough, when trimmed.  ", 35);
      * // "Already short enough, when trimmed."
      * 
      * truncate("My really long string that needs to be cut down to size.", 50);
      * // "My really long string that needs to be cut down…"
      * 
      * truncate("Myreallylongstringthatneedstobecutdowntosize.", 30);
      * // "Myreallylongstringthatneedsto…"
      * 
      * truncate("If we encounter dashes—like this—don't include them.", 35);
      * // "If we encounter dashes—like this…"
      * 
      * truncate(null);
      * // null</pre>
      * <p>
      * In detail, the algorithm is as follows:
      * <ol>
      * <li>If {@link #setTrimFirst trimFirst} is {@code true}, trim the leading
      *     and trailing spaces from the string. (Default is {@code true}).</li>
      * <li>If {@link #setCompressWhiteSpace compressWhiteSpace} is {@code true},
      *     remove extra white spaces from the string. (Default is
      *     {@code true}).</li>
      * <li>If the trimmed string is now less than or equal to the desired
      *     maximum length, we're done. Return the trimmed string.</li>
      * <li>Otherwise, shorten the string while leaving whole words intact
      *     (see {@link #setBreakWordLongerThan breakWordLongerThan}) and
      *     append a {@link #setSuffix suffix} at the end. (Default is "…").</li>
      * </ol>
      * 
      * @return {@code null} if the supplied string is {@code null}, the
      *         trimmed string if it does not exceed the desired maximum length,
      *         or a truncated version of the string that is less than or
      *         equal to the desired maximum length.
      */
     public String truncate(String string, int maxLength)
     {
         if(null == string) return null;
         
         if(_trimFirst)
         {
             string = string.trim();
         }
         
         if(_compressWhiteSpace)
         {
             string = string.replaceAll("\\s{2,}", " ");
         }
         
         if(string.length() <= maxLength)
         {
             return string;
         }
         
         int maxCapture = maxLength - 1 - _suffix.length();
         int minCapture = maxCapture - _breakWordLongerThan;
         if(minCapture < 0 || minCapture >= maxCapture)
         {
             minCapture = 0;
         }
         
         Pattern patt = Pattern.compile(String.format(
             "(?s)^(.{%d,%d}%s)%s.*",
             minCapture,
             maxCapture,
             _wordPattern,
             _wordDelimeterPattern
         ));
         Matcher match = patt.matcher(string);
         if(match.matches())
         {
             return string.substring(0, match.end(1)) + _suffix;
         }
         return string.substring(0, maxLength - _suffix.length()) + _suffix;
     }
     
     // Properties
     
     public String getSuffix()
     {
         return _suffix;
     }
 
     /**
      * Sets the suffix that will be appended to the truncated string, if in
      * fact the string needs to be shortened. The default is "…" (the
      * ellipsis character).
      */
     public TruncateHelper setSuffix(String suffix)
     {
         // Treat null as empty string
         if(null == suffix) suffix = "";
         
         this._suffix = suffix;
         return this;
     }
     
     public boolean getTrimFirst()
     {
         return _trimFirst;
     }
 
     /**
      * Sets whether leading and trailing white space should be removed from
      * the string before its length is tested and it is truncated.
      * The default is {@code true}.
      */
     public TruncateHelper setTrimFirst(boolean trimFirst)
     {
         this._trimFirst = trimFirst;
         return this;
     }
     
     public boolean getCompressWhiteSpace()
     {
         return _compressWhiteSpace;
     }
 
     /**
      * Sets whether extra white space characters are reduced to a single
      * space character before the string's length is tested and the string
      * is truncated. For example, two space characters between sentences
      * would be reduced to one space. This is usually desired when targeting
      * the web, since extra white space is ignored by default during HTML
      * rendering. The default is {@code true}.
      */
     public TruncateHelper setCompressWhiteSpace(boolean compressWhiteSpace)
     {
         this._compressWhiteSpace = compressWhiteSpace;
         return this;
     }
     
     public int getBreakWordLongerThan()
     {
         return _breakWordLongerThan;
     }
 
     /**
      * Sets the maximum word length that is required to remain intact. The
      * truncation algorithm will do its best to leave words intact and only
      * put the ellipsis (or other suffix) at the end of a word. However if the
      * string contains a very long sequence of characters with no spaces, it
      * may be preferable to show as much of the sequence as possible, rather
      * than omitting that word entirely. For example:
      * <pre class="example">
      * truncate("A string containing the word Antidisestablishmentarianism.", 50)</pre>
      * <p>
      * will produce:
      * <pre class="example">
      * A string containing the word Antidisestablishment…</pre>
      * <p>
      * because "Antidisestablishmentarianism" is longer that the
      * {@code breakWordLongerThan} limit. If this limit is set to
      * {@code -1} (no limit), the result would be:
      * <pre class="example">
      * A string containing the word…</pre>
      * <p>
      * because the algorithm will not break apart the word. Note that as a
      * result the truncated string is much shorter than the desired maximum
      * 50 characters.
      * <p>
      * The default {@code breakWordLongerThan} limit is 10 characters.
      */
     public TruncateHelper setBreakWordLongerThan(int breakWordLongerThan)
     {
         this._breakWordLongerThan = breakWordLongerThan;
         return this;
     }
     
     public Pattern getWordPattern()
     {
         return _wordPattern;
     }
 
     /**
      * Sets the regular expression that matches word characters. The default
      * is any non-space character except hyphen, en dash and em dash:
      * {@code [^\s\-–—]}.
      */
     public TruncateHelper setWordPattern(Pattern wordPattern)
     {
         Assert.notNull(wordPattern);
         this._wordPattern = wordPattern;
         return this;
     }
     
     public Pattern getWordDelimeterPattern()
     {
         return _wordDelimeterPattern;
     }
 
     /**
      * Sets the regular expression that matches non-word characters (i.e. the
      * delimiters between words). The default is the inverse of the default
      * {@code wordPattern}: {@code [\s\-–—]}.
      */
     public TruncateHelper setWordDelimeterPattern(Pattern wordDelimeterPattern)
     {
         Assert.notNull(wordDelimeterPattern);
         this._wordDelimeterPattern = wordDelimeterPattern;
         return this;
     }
 }
