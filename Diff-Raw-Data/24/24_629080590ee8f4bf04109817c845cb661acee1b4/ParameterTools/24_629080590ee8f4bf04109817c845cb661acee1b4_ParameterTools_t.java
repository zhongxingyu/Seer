 package de.zib.gndms.kit.config;
 
 import org.jetbrains.annotations.NotNull;
 
 import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
 
 
 /**
      * Helper class for parsing string parameters and rendering string-string-maps
  *
  * Supported format (assumes greedy matching):
  *
  * <pre>
  * (params) = (entry) [';' (entry)]* [';']
  * (entry) = (ws-char)* (printable-non-ws-char)+[{':'|'='} (value)]
  * (value) = (ignored-ws-char)* {(trimmed-value) | (exact-value)} (ignored-ws-char)*
  * (trimmed-value) = (pintable-chars-taken-as-value)*
  * (exact-value) = '\'' (pintable-chars-taken-as-value)* '\''
  *               | '"' (pintable-chars-taken-as-value)* '"'
  * </pre>
  *
  * If a key is not provided with a value, it is set to the string "true" unless
  * the key name starts with "!" in which case the key (without the "!") is set to "false".
  * Alternatively, a leading "-" may be used to dennote false and "+" to denote true. As a
  * consequence of this shortcut for boolean values, no real key name may start with "+", "-",
  * or "!".
  *
  * Escaping is supported in (printable-chars-taken-as-value) via backspace.
  */
 public final class ParameterTools {
     public static final int EXPECTED_MAX_KEY_LENGTH = 32;
     public static final int EXPECTED_MAX_VAL_LENGTH = 64;
 
     /**
      * Escapes the following characters from a given String {@code [ ] ,  : ; ' \ "}
      * @param sParam the String wich shall be escaped
      * @return the escpaded String
      */
 	@SuppressWarnings({ "HardcodedFileSeparator" })
 	public static String escape(final String sParam) {
 		final StringBuilder builder = new StringBuilder(sParam.length() + (sParam.length() / 8));
 		for (int i = 0; i < sParam.length(); i++) {
 			final char cur = sParam.charAt(i);
 			switch (cur) {
 				case '\\':
 			    case '[':
 				case ']':
 			    case ',':
 				case ';':
 			    case '\'':
 				case '"':
 				case ':':
 					builder.append('\\');
 				default:
 					builder.append(cur);
 			}
 		}
 		return builder.toString();
 	}
 
 
     private enum ParseMode
         { KEY_WHITE, KEY, VALUE_BEGIN, TRIMMED_VALUE, EXACT_VALUE, EXACT_VALUE_DONE }
 
     public ParameterTools() { throw new UnsupportedOperationException("Don't"); }
 
 
     /**
      * Parses the parameters of a String according to the description given above ({@link ParameterTools description}) and puts the keys with their corresponding values onto the map
      * @param targetMap The Map receiving all the keys with their values
      * @param params the String holding all the keys and their values in the Syntax described above
      * @param keyPattern  The pattern a valid key must match. Can be {@code null}, but still the keyname must not start with "+", "-", "!" if it's not a boolean value
      * @throws ParameterParseException if the String is not valid according to the Syntax, or the key does not match the pattern 
      */
     @SuppressWarnings({ "HardcodedFileSeparator" })
     public static void parseParameters(final @NotNull Map<String, String> targetMap,
                                        final @NotNull String params,
                                        final Pattern keyPattern)
             throws ParameterParseException {
         String currentKey = "";
         StringBuilder builder = new StringBuilder(Math.max(EXPECTED_MAX_KEY_LENGTH,
                                                            EXPECTED_MAX_VAL_LENGTH));
         ParseMode mode = ParseMode.KEY_WHITE;
         char[] chars = params.toCharArray();
         char currentQuote = '\'';
 
         for (int i = 0; i < chars.length; i++) {
             char cur = chars[i];
             if (!Character.isDefined(cur))
                 throw new ParameterParseException("Invalid character in ", params, i);
             switch (mode) {
                 case KEY_WHITE:
                     if (Character.isWhitespace(cur))
                         break;
                     else {
                         builder.delete(0, builder.length());
                         mode = ParseMode.KEY;
                     }
                 case KEY:
                     switch (cur) {
                         case '=':
                         case ':':
                             currentKey = makeKey(builder, keyPattern, i);
                             mode = ParseMode.VALUE_BEGIN;
                             break;
                         case ';':
                             makeBoolEntry(targetMap, builder, keyPattern, i);
                             mode = ParseMode.KEY_WHITE;
                             break;
                         default:
                             if (Character.isWhitespace(cur))
                                 throw new ParameterParseException
                                         ("Whitespace between key name and ':'", builder, i);
                             builder.append(cur);
                     }
                     break;
                 case VALUE_BEGIN:
                     switch (cur) {
                         case '\'':
                             currentQuote = '\'';
                             mode = ParseMode.EXACT_VALUE;
                             break;
                         case '"':
                             currentQuote = '"';
                             mode = ParseMode.EXACT_VALUE;
                             break;
                         default:
                             if (Character.isWhitespace(cur))
                                 break;
                             else {
                                 builder.append(cur);
                                 mode = ParseMode.TRIMMED_VALUE;
                             }
                     }
                     break;
                 case TRIMMED_VALUE:
                     switch (cur) {
                         case ';':
                             makeEntry(targetMap, currentKey, builder, i, true);
                             mode = ParseMode.KEY_WHITE;
                             break;
                         case '\\':
                             cur = escape(builder, chars, i);
                             i++;
                         default:
                             builder.append(cur);
                     }
                     break;
                 case EXACT_VALUE:
                     switch (cur) {
                         case '"':
                             if (currentQuote == '"')
                                 mode = ParseMode.EXACT_VALUE_DONE;
                             else
                                 builder.append(cur);
                             break;
                         case '\'':
                             if (currentQuote == '\'')
                                 mode = ParseMode.EXACT_VALUE_DONE;
                             else
                                 builder.append(cur);
                             break;
                         case '\\':
                             cur = escape(builder, chars, i);
                             i++;
                         default:
                             builder.append(cur);
                     }
                     break;
                 case EXACT_VALUE_DONE:
                     boolean finished = i + 1 == chars.length;
                     if (! Character.isWhitespace(cur)) {
                         finished = true;
                         // Must look at this char again in next iteration unless ';'
                         if (cur != ';')
                             i--;
                     }
                     if (finished) {
                         makeEntry(targetMap, currentKey, builder, i, false);
                         mode = ParseMode.KEY_WHITE;
                     }
                     break;
                 default:
                     throw new IllegalStateException("Unknown or unhandled ParseMode");
             }
         }
         if (mode == ParseMode.TRIMMED_VALUE
                 || mode == ParseMode.VALUE_BEGIN || mode == ParseMode.EXACT_VALUE_DONE) {
                 makeEntry(targetMap, currentKey, builder, chars.length, true);
                 return;
         }
         if (mode == ParseMode.KEY) {
             if (builder.length() == 0)
                 return;
             makeBoolEntry(targetMap, builder, keyPattern, params.length());
             return;
         }
         if (mode == ParseMode.KEY_WHITE)
             return;
         else
             throw new ParameterParseException
                     ("Incomplete entry encountered before end of string",
                      params, params.length());
     }
 
 
     /**
      * Associates a boolean value as String to a character-key (according to {@link ParameterTools description}) and stores it on the map.
      * If the first character is an encoding for its value, it  will be deleted.
      * @see de.zib.gndms.kit.config.ParameterTools#putBoolKey(java.util.Map, java.util.regex.Pattern, String, String, int)  
      */
     private static void makeBoolEntry(
             final Map<String, String> targetMap, final StringBuilder builderParam,
             final Pattern keyPattern, final int index)
             throws ParameterParseException {
         String boolKey = builderParam.toString();
         builderParam.delete(0, builderParam.length());
 
         if (boolKey.length() == 0)
             throw new ParameterParseException("Key name of size zero", index);
 
         switch (boolKey.charAt(0)) {
 
             case '!':
             case '-':
                 putBoolKey(targetMap, keyPattern, boolKey.substring(1), "false", index);
                 break;
             case '+':
                 putBoolKey(targetMap, keyPattern, boolKey.substring(1), "true", index);
                 break;
             default:
                 putBoolKey(targetMap, keyPattern, boolKey, "true", index);
         }
     }
 
 
     
     /**
      * Puts the key {@code boolKeyParam} with the value {@code val} on {@code targetMap} if either
      * the key matches a specific pattern or {@code keyPattern=null}.
      * @param targetMap the Map where the key and the associated value will be stored at
      * @param keyPattern the pattern a key must match
      * @param boolKeyParam the key with which the value is associated
      * @param val the value to be associated with the key. Should be "false" or "true"
      * @param index can be used to trace the current character position of the original String, when this method is invoked
      * @throws ParameterParseException if the key does not match the pattern 
      */
     private static void putBoolKey(
             final Map<String, String> targetMap, final Pattern keyPattern,
             final String boolKeyParam, final String val, final int index)
             throws ParameterParseException {
         if (keyPattern == null || keyPattern.matcher(boolKeyParam).matches())
             targetMap.put(boolKeyParam, val);
         else
             throw new ParameterParseException("Invalid key  name", index);
     }
 
 
     /**
      *
      * @param builderParam
      * @param charsParam
      * @param index
      * @return
      * @throws ParameterParseException
      */
     private static char escape(
             final StringBuilder builderParam, final char[] charsParam, final int index)
             throws ParameterParseException {
         if (index + 1 == charsParam.length)
             throw new ParameterParseException
                     ("Unfinished escape in value", builderParam, index);
         else
             return charsParam[index + 1];
     }
 
     /**
      * Returns a {@code String} out of the {@code StringBuilder} if it is a valid key, matching the pattern
      * @param builderParam the characters which shall be converted into a key-String
      * @param keyPattern a Pattern the characters must match to be a valid key
      * @param index can be used to trace the current character position of the original String, when this method is invoked
      * @return   Returns a {@code String} out of the {@code StringBuilder} if it is a valid key, matching the pattern
      * @throws ParameterParseException if the key name is invalid, e.g. starts with {@code ! + -} or does not match {@code keypattern}
      */
     private static String makeKey(final StringBuilder builderParam,
                                   final Pattern keyPattern, final int index)
             throws ParameterParseException {
         final String currentKey;
         currentKey = builderParam.toString();
         if (currentKey.length() == 0)
             throw new ParameterParseException("Key name of size zero", index);
         builderParam.delete(0, builderParam.length());
         final char firstChar = currentKey.charAt(0);
         if (firstChar == '!' || firstChar == '+' || firstChar == '-' ||
                 keyPattern != null && ! keyPattern.matcher(currentKey).matches())
                 throw new ParameterParseException("Invalid key name", index);
 
 
         return currentKey;
     }
 
     /**
      * Appends all keys with their corresponding values from the {@code Map} to a {@code StringBuilder} matching the pattern.
      * Strings containing whitespaces or {@code ' : ; =} are not allowed.
      * <br>
      * Syntax:
      * <pre>
      * key: 'value' [; key: 'value' ]*  
      * </pre>
      * @param builder the resulting String will be appended to {@code builder}
      * @param map contains the keys and their corresponding values
      * @param keyPattern the pattern a valid key must match. If {@code null} all Strings containing legal characters are accepted
      * @param addNewlines if true, a new line follows after every semicolon
      */
     @SuppressWarnings({ "HardcodedLineSeparator" })
     public static void append(final @NotNull StringBuilder builder,
                               final @NotNull Map<String, String> map,
                               final Pattern keyPattern,
                               final boolean addNewlines)
     {
         boolean first = true;
 
         for (Map.Entry<String,String> entry : map.entrySet()) {
             String key = entry.getKey();
             if (keyPattern == null || keyPattern.matcher(key).matches()) {
                 if (first)
                     first = false;
                 else
                     { builder.append(';'); builder.append(addNewlines ? '\n' : ' '); }
                 String val = entry.getValue();
                 for (char c : key.toCharArray()) {
                     if (Character.isWhitespace(c))
                         throw new IllegalArgumentException("Whitespace in key name");
                     if (c == ':' || c == '\'' || c == ';' || c == '=')
                         throw new IllegalArgumentException("Reserved char in key name");
                 }
                 builder.append(key);
                 builder.append(": '");
                final String escaped = val.replaceAll("'", "\\'");
                 builder.append(escaped);
                 builder.append('\'');
             }
         }
     }
 
 
     /**
      * Invokes {@link ParameterTools#append(StringBuilder, Map, Pattern, boolean)}
      * with {@code addNewlines=false} so that no new line follows after a semicolon
      */
     public static void append(
             final @NotNull StringBuilder builder,
             final @NotNull Map<String, String> map,
             final Pattern keyPattern) {
         append(builder, map, keyPattern, false);
     }
 
     /** 
      * @return the String generated by {@link ParameterTools#append(StringBuilder, java.util.Map, java.util.regex.Pattern, boolean)} 
      */
     public static @NotNull String asString(final @NotNull Map<String, String> map,
         final Pattern keyPattern, final boolean addNewlines) {
         StringBuilder resultBuilder =
                 new StringBuilder(map.size() *
                         (EXPECTED_MAX_KEY_LENGTH + EXPECTED_MAX_VAL_LENGTH));
         append(resultBuilder, map, keyPattern, addNewlines);
         return resultBuilder.toString();
     }
 
     /**
      * Invokes {@link ParameterTools#asString(Map, Pattern, boolean)}
      * with {@code addNewlines=false} so that no new line follows after a semicolon
      */
     @SuppressWarnings({ "StaticMethodOnlyUsedInOneClass" })
     public static @NotNull String asString(
             final @NotNull Map<String, String> map, final Pattern keyPattern) {
         return asString(map, keyPattern, false);
     }
 
     /**
      * Puts the specified key with his corresponding value onto map
      *
      * @param targetMap the Map where the key and the associated value will be stored at
      * @param currentKeyParam the key with which the value is associated 
      * @param valueBuilderParam the value to be associated with the key
      * @param index can be used to trace the current character position of the original String, when this method is invoked
      * @param trim if true String's trim()-Method will be invoked on {@code valueBuilderParam} before saving in {@code targetMap}
      * @throws ParameterParseException if key {@code currentKeyParam} duplicate 
      */
     private static void makeEntry(
             final @NotNull Map<String, String> targetMap,
             final @NotNull String currentKeyParam,
             final @NotNull StringBuilder valueBuilderParam,
             final int index, final boolean trim)
             throws ParameterParseException {
         String value = trim ? valueBuilderParam.toString().trim() : valueBuilderParam.toString();
         if (targetMap.containsKey(currentKeyParam))
             throw new ParameterParseException
                     ("Duplicate key ", currentKeyParam, index);
         targetMap.put(currentKeyParam, value);
     }
 
     /**
      *
      */
     public static final class ParameterParseException extends Exception {
         private static final long serialVersionUID = 2568606838649522059L;
 
 
         public ParameterParseException() {
             super();
         }
 
 
         public ParameterParseException(final String message) {
             super(message);
         }
 
 
         public ParameterParseException(final String message, final int index) {
             super(message + "(at pos. " + Integer.toString(index) + ')');
         }
 
         public ParameterParseException(final String message, final String token,
                                        final int index) {
             this(message + " '" + token + "' ", index);
         }
 
         public ParameterParseException(
             final String messageParam, final Object builderParam,
             final int iParam) {
         this (messageParam, builderParam.toString(), iParam);
         }
     }
 
 
 
         /*
         Sollte bei [A\\,C] nicht A,C als ein Eintrag herauskommen ?
          */
         @SuppressWarnings({ "HardcodedFileSeparator" })
         public static List<String> parseStringArray(final String optStrParam) {
             final List<String> entries = new LinkedList<String>();
             boolean nested = false;
             boolean escape = false;
             boolean leading = false;
             StringBuilder builder = new StringBuilder(EXPECTED_MAX_VAL_LENGTH);
             for (int i = 0; i < optStrParam.length(); i++) {
                 final char c = optStrParam.charAt(i);
                 switch (c) {
                     case '\\':
                         if (! escape) escape = true;
                         break;
                     case ',':
                         if (!escape && nested) {
                             entries.add(builder.toString());
                             builder.setLength(0);
                             leading = true;
                             break;
                         }
                     case '[':
                         if (!escape && !nested) {
                             builder.setLength(0);
                             nested = true;
                             leading = true;
                             break;
                         }
                     case ']': {
                         if (!escape && nested) {
                             nested = false;
                             leading = false;
                             entries.add(builder.toString());
                             builder.setLength(0);
                             break;
                         }
                     }
                     default:
                         final boolean isWhite = Character.isWhitespace(c);
                         if (escape) {
                             builder.append(c);
                             if (! isWhite) leading = false;
                         }
                         else {
                             if (isWhite) {
                                 if (leading) break;
                             }
                             else
                                 leading = false;
                             builder.append(c);
                         }
                 }
             }
             return entries;
         }
 
 
 
 
     
 
 
 
 }
