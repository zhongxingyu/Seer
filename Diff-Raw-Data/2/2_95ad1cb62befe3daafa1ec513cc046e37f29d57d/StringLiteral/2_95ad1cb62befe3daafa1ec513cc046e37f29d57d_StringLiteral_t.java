 package veeju.forms;
 
 import java.util.regex.*;
 
 /**
  * String literal to be self-evaluated.
  *
  * <pre>{@code
  * <string-literal>         ::= [ <string-literal-prefix> ]
  *                              <string-literal-body>
  * <string-literal-prefix>  ::= <alpha>
  * <string-literal-body>    ::= '"' { <string-item> } '"'
  *                            | "'" { <string-item> } "'"
  *                            | '"""' { <string-item> } '"""'
  *                            | "'''" { <string-item> } "'''"
  * <string-item>            ::= <string-character> | <string-escape-sequence>
  * <string-character>       ::= <any source character except "\" or the quote>
  * <string-escape-sequence> ::= "\" <any character>
  * }</pre>
  */
 public final class StringLiteral extends Form {
     /**
      * The regular expression pattern that matches to string literals.
      */
     public static final Pattern PATTERN = Pattern.compile(
         // 1) prefix:
         "( [A-Za-z] )? " +
         // 2) double_quote_long_string:
         "(?: [\"]\"\" ( (?: [^\\\\] | \\\\. )*? ) [\"]\"\"" +
         // 3) single_quote_long_string:
         "|   ''' ( (?: [^\\\\] | \\\\. )*? )  '''" +
         // 4) double_quote_short_string:
         "|   \" ( (?: [^\"\\\\] | \\\\. )* ) \"" +
         // 5) single_quote_short_string:
         "|   ' ( (?: [^'\\\\] | \\\\. )* ) '" +
         ")",
         Pattern.COMMENTS
     );
 
     /**
      * The regular expression pattern that matches to escaping sequences.
      */
     public static final Pattern ESCAPE_PATTERN = Pattern.compile(
         "\\\\(?:(x[A-Fa-f0-9]{2})" + // 1) x
         "|(u[A-Fa-f0-9]{4})" + // 2) u
        "|(U[A-Fa-f0-9]{8})" + // 3) U
         "|([^Uux]))" // 4) rest
     );
 
     /**
      * The list of escaping metacharacters without leading slash.
      *
      * @see #ESCAPE_MEANINGS
      */
     public static final char[] ESCAPE_METACHARS = {'\\', '\'', '"', 'a', 'b',
                                                    'f', 'n', 'r', 't', 'v'};
 
     /**
      * The list of escaping matachracters' meanings.
      *
      * @see #ESCAPE_METACHARS
      */
     public static final char[] ESCAPE_MEANINGS = {'\\', '\'', '"', '\u0007',
                                                   '\b', '\f', '\n', '\r',
                                                   '\t', '\u000b'};
 
     /**
      * A string value the string literal represents.
      *
      * @see #getString()
      */
     protected final String string;
 
     /**
      * The prefix character.
      *
      * The null character ({@code '\0'}) means no prefix.
      *
      * @see #getPrefix()
      */
     protected final char prefix;
 
     /**
      * Creates a {@code StringLiteral} instance.
      *
      * @param string a body of the string literal.
      */
     public StringLiteral(final String string) {
         this.string = string;
         this.prefix = '\0';
     }
 
     /**
      * Creates a {@code StringLiteral} instance with a prefix character.
      *
      * @param string a body of the string literal.
      * @param prefix a prefix character of the string literal.
      */
     public StringLiteral(final String string, final char prefix) {
         if ((prefix < 'a' || prefix > 'z') && (prefix < 'A' || prefix > 'Z')) {
             throw new IllegalArgumentException("invalid prefix character: " +
                                                Character.toString(prefix));
         }
         this.string = string;
         this.prefix = prefix;
     }
 
     /**
      * The method for getting its body.
      *
      * @return the body of the string literal.
      * @see #string
      */
     public String getString() {
         return string;
     }
 
     /**
      * The method for getting its prefix character.
      *
      * It returns {@code null} when there is no prefix character for the
      * string literal.
      *
      * @return the prefix character of the string literal.
      *         {@code null} means the string literal has no prefix character.
      * @see #prefix
      */
     public Character getPrefix() {
         if (prefix == '\0') return null;
         return prefix;
     }
 
     public String generateCodeString() {
         final StringBuilder buf = new StringBuilder(string.length() + 2);
         if (prefix != '\0') {
             buf.append(prefix);
         }
         boolean usingSq = string.contains("\"") && !string.contains("'");
         for (char c : ESCAPE_MEANINGS) {
             if (!usingSq) break;
             else if (c == '"') continue;
             usingSq = usingSq && string.indexOf(c) < 0;
         }
         for (int i = 0, strlen = string.length(); i < strlen; ++i) {
             if (!usingSq) break;
             char c = string.charAt(i);
             usingSq = usingSq && '\u0020' <= c && c < '\u007f';
         }
         final char wrap = usingSq ? '\'' : '"', nwrap = usingSq ? '"' : '\'';
         buf.append(wrap);
         for (int i = 0, strlen = string.length(); i < strlen; ++i) {
             char c = string.charAt(i);
             boolean found = false;
             if (c != nwrap) {
                 for (short j = 0; j < ESCAPE_MEANINGS.length; ++j) {
                     char m = ESCAPE_MEANINGS[j];
                     if (m != c) continue;
                     buf.append('\\');
                     buf.append(ESCAPE_METACHARS[j]);
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 int code = string.codePointAt(i);
                 if (c > '\uffff') {
                     buf.append(String.format("\\U%08x", code));
                 } else if (c > '\u00ff') {
                     buf.append(String.format("\\u%04x", code));
                 } else if (c < '\u0020' || c >= '\u007f') {
                     buf.append(String.format("\\x%02x", code));
                 } else {
                     buf.append(c);
                 }
             }
         }
         buf.append(wrap);
         return buf.toString();
     }
 }
 
