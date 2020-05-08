 package com.google.gson.stream;
 
 /*
  * Copyright (C) 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import com.google.gson.JsonParseException;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.Closeable;
 
 public class JsonReaderEx implements Closeable {
   /** The only non-execute prefix this parser permits */
   private static final char[] NON_EXECUTE_PREFIX = ")]}'\n".toCharArray();
 
   private static final String TRUE = "true";
   private static final String FALSE = "false";
 
   private final CharSequence in;
  //private final StringCreator stringCreator;
   private final int start;
 
   /** True to accept non-spec compliant JSON */
   private boolean lenient = false;
 
  private int position;
   private final int limit;
 
   /*
    * The nesting stack. Using a manual array rather than an ArrayList saves 20%.
    */
   private JsonScope[] stack = new JsonScope[32];
   private int stackSize = 0;
 
   /**
    * The type of the next token to be returned by {@link #peek} and {@link
    * #advance}. If null, peek() will assign a value.
    */
   private JsonToken token;
 
   /** The text of the next name. */
   private String name;
 
   /*
    * For the next literal value, we may have the text value, or the position
    * and length in the buffer.
    */
   private String value;
   private int valuePosition;
   private int valueLength;
 
   /** True if we're currently handling a skipValue() call. */
   private boolean skipping = false;
 
   private StringBuilder builder;
 
   /**
    * Creates a new instance that reads a JSON-encoded stream from {@code in}.
    */
   public JsonReaderEx(@NotNull CharSequence in) {
     this(in, 0);
   }
 
   public JsonReaderEx(@NotNull CharSequence in, int start) {
    this(in, start, null);
  }

  //new StringCreator() {
  //      @Override
  //      public String createString(int start, int end) {
  //        return in.subSequence(start, end).toString();
  //      }
  //    }

  public JsonReaderEx(@NotNull CharSequence in, int start, StringCreator stringCreator) {
     this.in = in;
    //this.stringCreator = stringCreator;
     this.start = start;
    position = start;
     limit = in.length();
     push(JsonScope.EMPTY_DOCUMENT);
   }
 
  public interface StringCreator {
    String createString(int start, int end);
  }

   public JsonReaderEx subReader() {
     int p;
     if (token == null && stack[stackSize - 1] == JsonScope.DANGLING_NAME) {
       moveToObjectValue();
       p = position;
       nextValue();
     }
     else {
       p = position;
     }
     // we keep our initial start position - our current JsonReader may be also sub reader (so, current position may be not absolute)
     return new JsonReaderEx(in, start + p);
   }
 
   @SuppressWarnings("UnusedDeclaration")
   public final void setLenient(boolean lenient) {
     this.lenient = lenient;
   }
 
   @SuppressWarnings("UnusedDeclaration")
   public final boolean isLenient() {
     return lenient;
   }
 
   /**
    * Consumes the next token from the JSON stream and asserts that it is the
    * beginning of a new array.
    */
   public void beginArray() {
     expect(JsonToken.BEGIN_ARRAY);
   }
 
   /**
    * Consumes the next token from the JSON stream and asserts that it is the
    * end of the current array.
    */
   public void endArray() {
     expect(JsonToken.END_ARRAY);
   }
 
   /**
    * Consumes the next token from the JSON stream and asserts that it is the
    * beginning of a new object.
    */
   public void beginObject() {
     expect(JsonToken.BEGIN_OBJECT);
   }
 
   /**
    * Consumes the next token from the JSON stream and asserts that it is the
    * end of the current object.
    */
   public void endObject() {
     expect(JsonToken.END_OBJECT);
   }
 
   /**
    * Consumes {@code expected}.
    */
   private void expect(JsonToken expected) {
     peek();
     if (token != expected) {
       throw createParseError("Expected " + expected + " but was " + peek());
     }
     advance();
   }
 
   /**
    * Returns true if the current array or object has another element.
    */
   public boolean hasNext() {
     peek();
     return token != JsonToken.END_OBJECT && token != JsonToken.END_ARRAY;
   }
 
   /**
    * Returns the type of the next token without consuming it.
    */
   public JsonToken peek() {
     if (token != null) {
       return token;
     }
 
     switch (stack[stackSize - 1]) {
     case EMPTY_DOCUMENT:
       if (lenient) {
         consumeNonExecutePrefix();
       }
       stack[stackSize - 1] = JsonScope.NONEMPTY_DOCUMENT;
       JsonToken firstToken = nextValue();
       if (!lenient && token != JsonToken.BEGIN_ARRAY && token != JsonToken.BEGIN_OBJECT) {
         throw createParseError("Expected JSON document to start with '[' or '{' but was " + token);
       }
       return firstToken;
     case EMPTY_ARRAY:
       return nextInArray(true);
     case NONEMPTY_ARRAY:
       return nextInArray(false);
     case EMPTY_OBJECT:
       return nextInObject(true);
     case DANGLING_NAME:
       moveToObjectValue();
       return nextValue();
     case NONEMPTY_OBJECT:
       return nextInObject(false);
     case NONEMPTY_DOCUMENT:
       int c = nextNonWhitespace(false);
       if (c == -1) {
         return JsonToken.END_DOCUMENT;
       }
       position--;
       if (!lenient) {
         throw createParseError("Expected EOF");
       }
       return nextValue();
     case CLOSED:
       throw new IllegalStateException("JsonReaderEx is closed");
     default:
       throw new AssertionError();
     }
   }
 
   /**
    * Consumes the non-execute prefix if it exists.
    */
   private void consumeNonExecutePrefix() {
     // fast forward through the leading whitespace
     nextNonWhitespace(true);
     position--;
 
     if (position + NON_EXECUTE_PREFIX.length > limit) {
       return;
     }
 
     for (int i = 0; i < NON_EXECUTE_PREFIX.length; i++) {
       if (in.charAt(position + i) != NON_EXECUTE_PREFIX[i]) {
         return; // not a security token!
       }
     }
 
     // we consumed a security token!
     position += NON_EXECUTE_PREFIX.length;
   }
 
   /**
    * Advances the cursor in the JSON stream to the next token.
    */
   private JsonToken advance() {
     peek();
 
     JsonToken result = token;
     token = null;
     value = null;
     name = null;
     return result;
   }
 
   /**
    * Returns the next token, a {@link JsonToken#NAME property name}, and consumes it
    */
   public String nextName() {
     peek();
     if (token != JsonToken.NAME) {
       throw new IllegalStateException("Expected a name but was " + peek()
           + " at line " + getLineNumber() + " column " + getColumnNumber());
     }
     String result = name;
     advance();
     return result;
   }
 
   /**
    * Returns the {@link JsonToken#STRING string} value of the next token,
    * consuming it. If the next token is a number, this method will return its
    * string form.
    *
    * @throws IllegalStateException if the next token is not a string or if
    *     this reader is closed.
    */
   public String nextString() {
     peek();
     if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
       throw new IllegalStateException("Expected a string but was " + peek()
           + " at line " + getLineNumber() + " column " + getColumnNumber());
     }
 
     String result = value;
     advance();
     return result;
   }
 
   /**
    * Returns the {@link JsonToken#BOOLEAN boolean} value of the next token,
    * consuming it.
    *
    * @throws IllegalStateException if the next token is not a boolean or if
    *     this reader is closed.
    */
   public boolean nextBoolean() {
     peek();
     if (token != JsonToken.BOOLEAN) {
         throw createParseError("Expected a boolean but was " + token);
     }
 
     boolean result = (value == TRUE);
     advance();
     return result;
   }
 
   /**
    * Consumes the next token from the JSON stream and asserts that it is a
    * literal null.
    *
    * @throws IllegalStateException if the next token is not null or if this
    *     reader is closed.
    */
   public void nextNull() {
     peek();
     if (token != JsonToken.NULL) {
       throw new IllegalStateException("Expected null but was " + token
           + " at line " + getLineNumber() + " column " + getColumnNumber());
     }
 
     advance();
   }
 
   /**
    * Returns the {@link JsonToken#NUMBER double} value of the next token,
    * consuming it. If the next token is a string, this method will attempt to
    * parse it as a double using {@link Double#parseDouble(String)}.
    *
    * @throws IllegalStateException if the next token is not a literal value.
    * @throws NumberFormatException if the next literal value cannot be parsed
    *     as a double, or is non-finite.
    */
   public double nextDouble() {
     peek();
     if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
       throw new IllegalStateException("Expected a double but was " + token
           + " at line " + getLineNumber() + " column " + getColumnNumber());
     }
 
     double result = Double.parseDouble(value);
     if ((result >= 1.0d && value.length() > 0 && value.charAt(0) == '0')) {
       throw createParseError("JSON forbids octal prefixes: " + value);
     }
     if (!lenient && (Double.isNaN(result) || Double.isInfinite(result))) {
       throw createParseError("JSON forbids NaN and infinities: " + value);
     }
 
     advance();
     return result;
   }
 
   /**
    * Returns the {@link JsonToken#NUMBER long} value of the next token,
    * consuming it. If the next token is a string, this method will attempt to
    * parse it as a long. If the next token's numeric value cannot be exactly
    * represented by a Java {@code long}, this method throws.
    *
    * @throws IllegalStateException if the next token is not a literal value.
    * @throws NumberFormatException if the next literal value cannot be parsed
    *     as a number, or exactly represented as a long.
    */
   public long nextLong() {
     peek();
     if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
       throw new IllegalStateException("Expected a long but was " + token
           + " at line " + getLineNumber() + " column " + getColumnNumber());
     }
 
     long result;
     try {
       result = Long.parseLong(value);
     } catch (NumberFormatException ignored) {
       double asDouble = Double.parseDouble(value); // don't catch this NumberFormatException
       result = (long) asDouble;
       if (result != asDouble) {
         throw new NumberFormatException("Expected a long but was " + value
             + " at line " + getLineNumber() + " column " + getColumnNumber());
       }
     }
 
     if (result >= 1L && value.startsWith("0")) {
       throw createParseError("JSON forbids octal prefixes: " + value);
     }
 
     advance();
     return result;
   }
 
   /**
    * Returns the {@link JsonToken#NUMBER int} value of the next token,
    * consuming it. If the next token is a string, this method will attempt to
    * parse it as an int. If the next token's numeric value cannot be exactly
    * represented by a Java {@code int}, this method throws.
    *
    * @throws IllegalStateException if the next token is not a literal value.
    * @throws NumberFormatException if the next literal value cannot be parsed
    *     as a number, or exactly represented as an int.
    */
   public int nextInt() {
     peek();
     if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
       throw new IllegalStateException("Expected an int but was " + token
           + " at line " + getLineNumber() + " column " + getColumnNumber());
     }
 
     int result;
     try {
       result = Integer.parseInt(value);
     } catch (NumberFormatException ignored) {
       double asDouble = Double.parseDouble(value); // don't catch this NumberFormatException
       result = (int) asDouble;
       if (result != asDouble) {
         throw new NumberFormatException("Expected an int but was " + value
             + " at line " + getLineNumber() + " column " + getColumnNumber());
       }
     }
 
     if (result >= 1L && value.startsWith("0")) {
       throw createParseError("JSON forbids octal prefixes: " + value);
     }
 
     advance();
     return result;
   }
 
   /**
    * Closes this JSON reader and the underlying {@link java.io.Reader}.
    */
   public void close() {
     value = null;
     token = null;
     stack[0] = JsonScope.CLOSED;
     stackSize = 1;
   }
 
   /**
    * Skips the next value recursively. If it is an object or array, all nested
    * elements are skipped. This method is intended for use when the JSON token
    * stream contains unrecognized or unhandled values.
    */
   public void skipValue() {
     skipping = true;
     try {
       int count = 0;
       do {
         JsonToken token = advance();
         if (token == JsonToken.BEGIN_ARRAY || token == JsonToken.BEGIN_OBJECT) {
           count++;
         }
         else if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
           count--;
         }
       }
       while (count != 0);
     }
     finally {
       skipping = false;
     }
   }
 
   private void push(JsonScope newTop) {
     if (stackSize == stack.length) {
       JsonScope[] newStack = new JsonScope[stackSize * 2];
       System.arraycopy(stack, 0, newStack, 0, stackSize);
       stack = newStack;
     }
     stack[stackSize++] = newTop;
   }
 
   @SuppressWarnings("fallthrough")
   private JsonToken nextInArray(boolean firstElement) {
     if (firstElement) {
       stack[stackSize - 1] = JsonScope.NONEMPTY_ARRAY;
     }
     else {
       // Look for a comma before each element after the first element
       switch (nextNonWhitespace(true)) {
         case ']':
           stackSize--;
           return token = JsonToken.END_ARRAY;
         case ';':
           checkLenient(); // fall-through
         case ',':
           break;
         default:
           throw createParseError("Unterminated array");
       }
     }
 
     switch (nextNonWhitespace(true)) {
       case ']':
         if (firstElement) {
           stackSize--;
           return token = JsonToken.END_ARRAY;
         }
         // fall-through to handle ",]"
       case ';':
       case ',':
       /* In lenient mode, a 0-length literal means 'null' */
         checkLenient();
         position--;
         value = "null";
         return token = JsonToken.NULL;
       default:
         position--;
         return nextValue();
     }
   }
 
   @SuppressWarnings("fallthrough")
   private JsonToken nextInObject(boolean firstElement) {
     /*
      * Read delimiters. Either a comma/semicolon separating this and the
      * previous name-value pair, or a close brace to denote the end of the
      * object.
      */
     if (firstElement) {
       /* Peek to see if this is the empty object. */
       switch (nextNonWhitespace(true)) {
       case '}':
         stackSize--;
         return token = JsonToken.END_OBJECT;
       default:
         position--;
       }
     } else {
       switch (nextNonWhitespace(true)) {
       case '}':
         stackSize--;
         return token = JsonToken.END_OBJECT;
       case ';':
       case ',':
         break;
       default:
         throw createParseError("Unterminated object");
       }
     }
 
     /* Read the name. */
     int quote = nextNonWhitespace(true);
     switch (quote) {
       case '\'':
         checkLenient(); // fall-through
       case '"':
         name = nextString((char)quote);
         break;
       default:
         checkLenient();
         position--;
         name = nextLiteral(false);
         if (name.length() == 0) {
           throw createParseError("Expected name");
         }
     }
 
     stack[stackSize - 1] = JsonScope.DANGLING_NAME;
     return token = JsonToken.NAME;
   }
 
   private void moveToObjectValue() {
     // Read the name/value separator. Usually a colon ':'.
     // In lenient mode we also accept an equals sign '=', or an arrow "=>"
     switch (nextNonWhitespace(true)) {
       case ':':
         break;
       case '=':
         checkLenient();
         if (position < limit && in.charAt(position) == '>') {
           position++;
         }
         break;
       default:
         throw new JsonParseException("Expected ':'");
     }
 
     stack[stackSize - 1] = JsonScope.NONEMPTY_OBJECT;
   }
 
   @SuppressWarnings("fallthrough")
   private JsonToken nextValue() {
     int c = nextNonWhitespace(true);
     switch (c) {
       case '{':
         push(JsonScope.EMPTY_OBJECT);
         return token = JsonToken.BEGIN_OBJECT;
 
       case '[':
         push(JsonScope.EMPTY_ARRAY);
         return token = JsonToken.BEGIN_ARRAY;
 
       case '\'':
         checkLenient(); // fall-through
       case '"':
         value = nextString((char)c);
         return token = JsonToken.STRING;
 
       default:
         position--;
         return readLiteral();
     }
   }
 
   private int getLineNumber() {
     int result = 1;
     for (int i = 0; i < position; i++) {
       if (in.charAt(i) == '\n') {
         result++;
       }
     }
     return result;
   }
 
   private int getColumnNumber() {
     int result = 1;
     for (int i = 0; i < position; i++) {
       if (in.charAt(i) == '\n') {
         result = 1;
       }
       else {
         result++;
       }
     }
     return result;
   }
 
   /**
    * Returns the next character in the stream that is neither whitespace nor a
    * part of a comment. When this returns, the returned character is always at
    * {@code buffer[pos-1]}; this means the caller can always push back the
    * returned character by decrementing {@code position}.
    */
   private int nextNonWhitespace(boolean throwOnEof) {
     /*
      * This code uses ugly local variables 'p' and 'l' representing the 'pos'
      * and 'limit' fields respectively. Using locals rather than fields saves
      * a few field reads for each whitespace character in a pretty-printed
      * document, resulting in a 5% speedup. We need to flush 'p' to its field
      * before any (potentially indirect) call to fillBuffer() and reread both
      * 'p' and 'l' after any (potentially indirect) call to the same method.
      */
     CharSequence in = this.in;
     int p = position;
     int l = limit;
     while (true) {
       if (p == l) {
         position = p;
         break;
       }
 
       char c = in.charAt(p++);
       switch (c) {
         case '\t':
         case ' ':
         case '\n':
         case '\r':
           continue;
 
         case '/':
           position = p;
           if (p == l) {
             position--; // push back '/' so it's still in the buffer when this method returns
             boolean charsLoaded = (position + 3) < limit;
             position++; // consume the '/' again
             if (!charsLoaded) {
               return c;
             }
           }
 
           checkLenient();
           char peek = in.charAt(position);
           switch (peek) {
             case '*':
               // skip a /* c-style comment */
               position++;
               if (!skipTo("*/")) {
                 throw new JsonParseException("Unterminated comment");
               }
               p = position + 2;
               l = limit;
               continue;
 
             case '/':
               // skip a // end-of-line comment
               position++;
               skipToEndOfLine();
               p = position;
               l = limit;
               continue;
 
             default:
               return c;
           }
 
         case '#':
           position = p;
         /*
          * Skip a # hash end-of-line comment. The JSON RFC doesn't
          * specify this behaviour, but it's required to parse
          * existing documents. See http://b/2571423.
          */
           checkLenient();
           skipToEndOfLine();
           p = position;
           l = limit;
           continue;
 
         default:
           position = p;
           return c;
       }
     }
     if (throwOnEof) {
       throw new JsonParseException("End of input at line " + getLineNumber() + " column " + getColumnNumber());
     }
     else {
       return -1;
     }
   }
 
   private void checkLenient() {
     if (!lenient) {
       throw new JsonParseException("Use JsonReaderEx.setLenient(true) to accept malformed JSON");
     }
   }
 
   /**
    * Advances the position until after the next newline character. If the line
    * is terminated by "\r\n", the '\n' must be consumed as whitespace by the
    * caller.
    */
   private void skipToEndOfLine() {
     while (position < limit) {
       char c = in.charAt(position++);
       if (c == '\n' || c == '\r') {
         break;
       }
     }
   }
 
   private boolean skipTo(String toFind) {
     outer:
     for (; position + toFind.length() <= limit; position++) {
       for (int c = 0; c < toFind.length(); c++) {
         if (in.charAt(position + c) != toFind.charAt(c)) {
           continue outer;
         }
       }
       return true;
     }
     return false;
   }
 
   /**
    * Returns the string up to but not including {@code quote}, unescaping any
    * character escape sequences encountered along the way. The opening quote
    * should have already been read. This consumes the closing quote, but does
    * not include it in the returned string.
    *
    * @param quote either ' or ".
    * @throws NumberFormatException if any unicode escape sequences are
    *     malformed.
    */
   private String nextString(char quote) {
     // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
     CharSequence in = this.in;
     StringBuilder builder = null;
     int p = position;
     int l = limit;
     // the index of the first character not yet appended to the builder
     int start = p;
     while (p < l) {
       char c = in.charAt(p++);
       if (c == quote) {
         position = p;
         if (skipping) {
           return "skipped!";
         }
         else if (builder == null) {
           return in.subSequence(start, p - 1).toString();
         }
         else {
           builder.append(in, start, p - 1);
           String result = builder.toString();
           builder.setLength(0);
           return result;
         }
       }
       else if (c == '\\') {
         position = p;
         if (builder == null) {
           if (this.builder == null) {
             this.builder = new StringBuilder();
           }
           builder = this.builder;
         }
         builder.append(in, start, p - 1);
         builder.append(readEscapeCharacter());
         p = position;
         l = limit;
         start = p;
       }
     }
     throw createParseError("Unterminated string");
   }
 
   /**
    * Reads the value up to but not including any delimiter characters. This
    * does not consume the delimiter character.
    *
    * @param assignOffsetsOnly true for this method to only set the valuePos
    *     and valueLength fields and return a null result. This only works if
    *     the literal is short; a string is returned otherwise.
    */
   @SuppressWarnings("fallthrough")
   private String nextLiteral(boolean assignOffsetsOnly) {
     valuePosition = -1;
     int i = position;
     findNonLiteralCharacter:
     for (; i < limit; i++) {
       switch (in.charAt(i)) {
         case '/':
         case '\\':
         case ';':
         case '#':
         case '=':
           checkLenient(); // fall-through
         case '{':
         case '}':
         case '[':
         case ']':
         case ':':
         case ',':
         case ' ':
         case '\t':
         case '\f':
         case '\r':
         case '\n':
           break findNonLiteralCharacter;
       }
     }
 
     String result;
     if (assignOffsetsOnly) {
       valuePosition = position;
       result = null;
     }
     else if (skipping) {
       result = "skipped!";
     }
     else {
       result = in.subSequence(position, i).toString();
     }
     valueLength = i - position;
     position += valueLength;
     //noinspection ConstantConditions
     return result;
   }
 
   @Override
   public String toString() {
     return getClass().getSimpleName() + " at line " + getLineNumber() + " column " + getColumnNumber();
   }
 
   /**
    * Unescapes the character identified by the character or characters that
    * immediately follow a backslash. The backslash '\' should have already
    * been read. This supports both unicode escapes "u000A" and two-character
    * escapes "\n".
    *
    * @throws NumberFormatException if any unicode escape sequences are
    *     malformed.
    */
   private char readEscapeCharacter() {
     if (position == limit) {
       throw createParseError("Unterminated escape sequence");
     }
 
     char escaped = in.charAt(position++);
     switch (escaped) {
       case 'u':
         if (position + 4 > limit) {
           throw createParseError("Unterminated escape sequence");
         }
         // Equivalent to Integer.parseInt(stringPool.get(buffer, pos, 4), 16);
         char result = 0;
         for (int i = position, end = i + 4; i < end; i++) {
           char c = in.charAt(i);
           result <<= 4;
           if (c >= '0' && c <= '9') {
             result += (c - '0');
           }
           else if (c >= 'a' && c <= 'f') {
             result += (c - 'a' + 10);
           }
           else if (c >= 'A' && c <= 'F') {
             result += (c - 'A' + 10);
           }
           else {
             throw new NumberFormatException("\\u" + in.subSequence(position, position + 4));
           }
         }
         position += 4;
         return result;
 
       case 't':
         return '\t';
 
       case 'b':
         return '\b';
 
       case 'n':
         return '\n';
 
       case 'r':
         return '\r';
 
       case 'f':
         return '\f';
 
       case '\'':
       case '"':
       case '\\':
       default:
         return escaped;
     }
   }
 
   /**
    * Reads a null, boolean, numeric or unquoted string literal value.
    */
   private JsonToken readLiteral() {
     value = nextLiteral(true);
     if (valueLength == 0) {
       throw createParseError("Expected literal value");
     }
     token = decodeLiteral();
     if (token == JsonToken.STRING) {
       checkLenient();
     }
     return token;
   }
 
   /**
    * Assigns {@code nextToken} based on the value of {@code nextValue}.
    */
   private JsonToken decodeLiteral() {
     if (valuePosition == -1) {
       // it was too long to fit in the buffer so it can only be a string
       return JsonToken.STRING;
     }
     else if (valueLength == 4
              && ('n' == in.charAt(valuePosition) || 'N' == in.charAt(valuePosition))
              && ('u' == in.charAt(valuePosition + 1) || 'U' == in.charAt(valuePosition + 1))
              && ('l' == in.charAt(valuePosition + 2) || 'L' == in.charAt(valuePosition + 2))
              && ('l' == in.charAt(valuePosition + 3) || 'L' == in.charAt(valuePosition + 3))) {
       value = "null";
       return JsonToken.NULL;
     }
     else if (valueLength == 4
              && ('t' == in.charAt(valuePosition) || 'T' == in.charAt(valuePosition))
              && ('r' == in.charAt(valuePosition + 1) || 'R' == in.charAt(valuePosition + 1))
              && ('u' == in.charAt(valuePosition + 2) || 'U' == in.charAt(valuePosition + 2))
              && ('e' == in.charAt(valuePosition + 3) || 'E' == in.charAt(valuePosition + 3))) {
       value = TRUE;
       return JsonToken.BOOLEAN;
     }
     else if (valueLength == 5
              && ('f' == in.charAt(valuePosition) || 'F' == in.charAt(valuePosition))
              && ('a' == in.charAt(valuePosition + 1) || 'A' == in.charAt(valuePosition + 1))
              && ('l' == in.charAt(valuePosition + 2) || 'L' == in.charAt(valuePosition + 2))
              && ('s' == in.charAt(valuePosition + 3) || 'S' == in.charAt(valuePosition + 3))
              && ('e' == in.charAt(valuePosition + 4) || 'E' == in.charAt(valuePosition + 4))) {
       value = FALSE;
       return JsonToken.BOOLEAN;
     }
     else {
       value = in.subSequence(valuePosition, valuePosition + valueLength).toString();
       return decodeNumber(in, valuePosition, valueLength);
     }
   }
 
   /**
    * Determine whether the characters is a JSON number. Numbers are of the
    * form -12.34e+56. Fractional and exponential parts are optional. Leading
    * zeroes are not allowed in the value or exponential part, but are allowed
    * in the fraction.
    */
   private static JsonToken decodeNumber(CharSequence chars, int offset, int length) {
     int i = offset;
     char c = chars.charAt(i);
     if (c == '-') {
       c = chars.charAt(++i);
     }
 
     if (c == '0') {
       c = chars.charAt(++i);
     }
     else if (c >= '1' && c <= '9') {
       c = chars.charAt(++i);
       while (c >= '0' && c <= '9') {
         c = chars.charAt(++i);
       }
     }
     else {
       return JsonToken.STRING;
     }
 
     if (c == '.') {
       c = chars.charAt(++i);
       while (c >= '0' && c <= '9') {
         c = chars.charAt(++i);
       }
     }
 
     if (c == 'e' || c == 'E') {
       c = chars.charAt(++i);
       if (c == '+' || c == '-') {
         c = chars.charAt(++i);
       }
       if (c >= '0' && c <= '9') {
         c = chars.charAt(++i);
         while (c >= '0' && c <= '9') {
           c = chars.charAt(++i);
         }
       }
       else {
         return JsonToken.STRING;
       }
     }
 
     if (i == offset + length) {
       return JsonToken.NUMBER;
     }
     else {
       return JsonToken.STRING;
     }
   }
 
   /**
    * Throws a new IO exception with the given message and a context snippet
    * with this reader's content.
    */
   private JsonParseException createParseError(String message) {
     throw new JsonParseException(message + " at line " + getLineNumber() + " column " + getColumnNumber());
   }
 }
