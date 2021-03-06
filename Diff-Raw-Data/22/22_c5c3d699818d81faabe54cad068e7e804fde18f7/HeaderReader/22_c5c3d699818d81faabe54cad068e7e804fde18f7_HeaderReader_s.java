 /**
  * Copyright 2005-2010 Noelios Technologies.
  * 
  * The contents of this file are subject to the terms of one of the following
  * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
  * "Licenses"). You can select the license that you prefer but you may not use
  * this file except in compliance with one of these Licenses.
  * 
  * You can obtain a copy of the LGPL 3.0 license at
  * http://www.opensource.org/licenses/lgpl-3.0.html
  * 
  * You can obtain a copy of the LGPL 2.1 license at
  * http://www.opensource.org/licenses/lgpl-2.1.php
  * 
  * You can obtain a copy of the CDDL 1.0 license at
  * http://www.opensource.org/licenses/cddl1.php
  * 
  * You can obtain a copy of the EPL 1.0 license at
  * http://www.opensource.org/licenses/eclipse-1.0.php
  * 
  * See the Licenses for the specific language governing permissions and
  * limitations under the Licenses.
  * 
  * Alternatively, you can obtain a royalty free commercial license with less
  * limitations, transferable or non-transferable, directly at
  * http://www.noelios.com/products/restlet-engine
  * 
  * Restlet is a registered trademark of Noelios Technologies.
  */
 
 package org.restlet.engine.http.header;
 
 import static org.restlet.engine.http.header.HeaderUtils.isComma;
 import static org.restlet.engine.http.header.HeaderUtils.isDoubleQuote;
 import static org.restlet.engine.http.header.HeaderUtils.isLinearWhiteSpace;
 import static org.restlet.engine.http.header.HeaderUtils.isQuoteCharacter;
 import static org.restlet.engine.http.header.HeaderUtils.isQuotedText;
 import static org.restlet.engine.http.header.HeaderUtils.isSemiColon;
 import static org.restlet.engine.http.header.HeaderUtils.isTokenChar;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 
 import org.restlet.Context;
 import org.restlet.data.Encoding;
 import org.restlet.data.Parameter;
 
 /**
  * HTTP-style header reader.
  * 
  * @param <V>
  *            The header value target type. There can be multiple values for a
  *            single header.
  * @author Jerome Louvel
  */
 public class HeaderReader<V> {
     /** The header to read. */
     private final String header;
 
     /** The current read index (or -1 if not reading anymore). */
     private volatile int index;
 
     /**
      * Constructor.
      * 
      * @param header
      *            The header to read.
      */
     public HeaderReader(String header) {
         this.header = header;
         this.index = ((header == null) || (header.length() == 0)) ? -1 : 0;
     }
 
     /**
      * Adds values to the given list.
      * 
      * @param values
      *            The list of values to update.
      */
     public void addValues(Collection<V> values) {
         try {
             // Skip leading spaces
             skipSpaces();
 
             // Read the first value
             V nextValue = readValue();
 
             while (canAdd(nextValue, values)) {
                 // Add the value to the list
                 values.add(nextValue);
 
                 // Attempt to skip the value separator
                 if (skipValueSeparator()) {
                     // Read the next value
                     nextValue = readValue();
                 }
             }
         } catch (IOException ioe) {
             Context.getCurrentLogger().log(Level.INFO,
                     "Unable to read a header", ioe);
         }
     }
 
     /**
      * Indicates if the value can be added the the list. Useful to prevent the
      * addition of {@link Encoding#IDENTITY} constants for example. By default
      * it returns true for non null values.
      * 
      * @param value
      *            The value to add.
      * 
      * @param values
      *            The target collection.
      * @return True if the value can be added.
      */
     protected boolean canAdd(V value, Collection<V> values) {
         return (value != null) && !values.contains(value);
     }
 
     /**
      * Creates a new parameter with a null value. Can be overridden.
      * 
      * @param name
      *            The parameter name.
      * @return The new parameter.
      */
     protected final Parameter createParameter(String name) {
         return createParameter(name, null);
     }
 
     /**
      * Creates a new parameter. Can be overridden.
      * 
      * @param name
      *            The parameter name.
      * @param value
      *            The parameter value or null.
      * @return The new parameter.
      */
     protected Parameter createParameter(String name, String value) {
         return new Parameter(name, value);
     }
 
     /**
      * Reads the next character.
      * 
      * @return The next character.
      */
     public int peek() {
         int result = -1;
 
         if (this.index != -1) {
             result = this.header.charAt(this.index);
         }
 
         return result;
     }
 
     /**
      * Reads the next character.
      * 
      * @return The next character.
      */
     public int read() {
         int result = -1;
 
         if (this.index >= 0) {
             result = this.header.charAt(this.index++);
 
             if (this.index >= this.header.length()) {
                 this.index = -1;
             }
         }
 
         return result;
     }
 
     /**
      * Reads the next digits.
      * 
      * @return The next digits.
      */
     public String readDigits() {
         StringBuilder sb = new StringBuilder();
         int next = read();
 
         while (isTokenChar(next)) {
             sb.append((char) next);
             next = read();
         }
 
         // Unread the last character (separator or end marker)
         unread();
 
         return sb.toString();
     }
 
     /**
      * Reads the next pair as a parameter.
      * 
      * @return The next pair as a parameter.
      * @throws IOException
      */
     public Parameter readParameter() throws IOException {
         Parameter result = null;
         String name = readToken();
         int nextChar = read();
 
         if (name.length() > 0) {
             if (nextChar == '=') {
                 // The parameter has a value
                 result = createParameter(name, readParameterValue());
             } else {
                 // The parameter has not value
                 unread();
                 result = createParameter(name);
             }
         } else {
             throw new IOException(
                     "Parameter or extension has no name. Please check your value");
         }
 
         return result;
     }
 
     /**
      * Reads a parameter value which is either a token or a quoted string.
      * 
      * @return A parameter value.
      * @throws IOException
      */
     public String readParameterValue() throws IOException {
         String result = null;
 
         // Discard any leading space
         skipSpaces();
 
         // Detect if quoted string or token available
         int nextChar = peek();
 
         if (isDoubleQuote(nextChar)) {
             result = readQuotedString();
         } else if (isTokenChar(nextChar)) {
             result = readToken();
         }
 
         return result;
     }
 
     /**
     * Reads the next quoted string.
      * 
      * @return The next quoted string.
      * @throws IOException
      */
     public String readQuotedString() throws IOException {
         String result = null;
         int nextChar = read();
 
         // First character must be a double quote
         if (isDoubleQuote(nextChar)) {
             StringBuilder buffer = new StringBuilder();
 
             while (result == null) {
                 nextChar = read();
 
                 if (isQuotedText(nextChar)) {
                     buffer.append((char) nextChar);
                 } else if (isQuoteCharacter(nextChar)) {
                     // Start of a quoted pair (escape sequence)
                     buffer.append((char) read());
                 } else if (isDoubleQuote(nextChar)) {
                     // End of quoted string
                     result = buffer.toString();
                 } else if (nextChar == -1) {
                     throw new IOException(
                             "Unexpected end of quoted string. Please check your value");
                 } else {
                     throw new IOException(
                             "Invalid character \""
                                     + nextChar
                                     + "\" detected in quoted string. Please check your value");
                 }
             }
         } else {
             throw new IOException(
                     "A quoted string must start with a double quote");
         }
 
         return result;
     }
 
     /**
      * Read the next header value of a multi-value header. It skips leading and
      * trailing spaces.
      * 
      * @see <a
      *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2">HTTP
      *      parsing rule</a>
      * 
      * @return The next header value or null.
      */
     public String readRawValue() {
         // Skip leading spaces
         skipSpaces();
 
         // Read value until end or comma
         StringBuilder sb = null;
         int next = read();
 
         while ((next != -1) && !isComma(next)) {
             if (sb == null) {
                 sb = new StringBuilder();
             }
 
             sb.append((char) next);
             next = read();
         }
 
         // Remove trailing spaces
         if (sb != null) {
             for (int i = sb.length() - 1; (i >= 0)
                     && isLinearWhiteSpace(sb.charAt(i)); i--) {
                 sb.deleteCharAt(i);
             }
         }
 
         // Unread the separator
         if (isComma(next)) {
             unread();
         }
 
         return (sb == null) ? null : sb.toString();
     }
 
     /**
      * Reads the next token.
      * 
      * @return The next token.
      */
     public String readToken() {
         StringBuilder sb = new StringBuilder();
         int next = read();
 
         while (isTokenChar(next)) {
             sb.append((char) next);
             next = read();
         }
 
         // Unread the last character (separator or end marker)
         unread();
 
         return sb.toString();
     }
 
     /**
      * Read the next value. There can be multiple values for a single header.
      * 
      * @return The next value.
      */
     public V readValue() throws IOException {
         return null;
     }
 
     /**
      * Returns a new list with all values added.
      * 
      * @return A new list with all values added.
      */
     public List<V> readValues() {
         List<V> result = new CopyOnWriteArrayList<V>();
         addValues(result);
         return result;
     }
 
     /**
      * Skips the next spaces.
      */
     public void skipSpaces() {
         while (isLinearWhiteSpace(read())) {
             // Ignore
         }
 
         // Restore the first non space character found
         unread();
     }
 
     /**
      * Skips the next value separator (comma) including leading and trailing
      * spaces.
      * 
      * @return True if a separator was effectively skipped.
      */
     public boolean skipValueSeparator() {
         boolean result = false;
 
         // Skip leading spaces
         skipSpaces();
 
         // Check if next character is a value separator
         if (isComma(read())) {
             result = true;
 
             // Skip trailing spaces
             skipSpaces();
         } else {
             // Probably reached the end of the header
             unread();
         }
 
         return result;
     }
 
     /**
      * Unreads the last character.
      */
     public void unread() {
         this.index--;
     }
 
     /**
      * Skips the next parameter separator (semi-colon) including leading and
      * trailing spaces.
      * 
      * @return True if a separator was effectively skipped.
      */
     public boolean skipParameterSeparator() {
         boolean result = false;
 
         // Skip leading spaces
         skipSpaces();
 
         // Check if next character is a parameter separator
         if (isSemiColon(read())) {
             result = true;
 
             // Skip trailing spaces
             skipSpaces();
         } else {
             // Probably reached the end of the header
             unread();
         }
 
         return result;
     }
 
 }
