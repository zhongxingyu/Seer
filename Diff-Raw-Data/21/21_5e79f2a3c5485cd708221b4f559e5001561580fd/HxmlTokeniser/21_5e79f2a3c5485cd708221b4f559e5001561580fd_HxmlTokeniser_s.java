 package com.pellcorp.xml.parser;
 
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.io.Reader;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A StringTokenizer like, XML/HTML parser.
  * 
  * <P>
  * I did make use of some Aelfred ideas, especially the nice way that the
  * parsing was carried out, but generally apart from some method naming and
  * small pieces of code/logic I utilised, this parser is different, more based
  * on my original HtmlStreamTokenizer, which is implemented along the lines of
  * java.util.StringTokenizer.
  * </P>
  * 
  * <BR>
  * 
  * <PRE>
  * <b>Note:</b> Tag, Entity, ProcessingInstruction and Function names must be of the following form:
  * 		First char:	('_'|':'|[a-zA-Z])
  * 		The rest:	('_'|'.'|[a-zA-Z0-9])
  * </PRE>
  * 
  * <BR>
  * 
  * <P>
  * At this point even though this parser is written to correctly recognise and
  * return COMMENT, CDATA and PI the default is to parse them, but then to put
  * them into the dataBuffer as normal text. This is controlled by three private
  * boolean variables. (ignoreComments, ignoreCData, ignorePI)
  * </P>
  * 
  * <DL>
  * <DT>Recognises</DT>
  * <DD>&lt;Start Tag&gt;</DD>
  * <DD>&lt;Empty Tag/&gt;</DD>
  * <DD>&lt;/End Tag&gt;</DD>
  * </DL>
  * 
  * <p>
  * If you define tags for the parser to look for, it will not find any &entity;
  * or $function(...) tags inside the tags. The same goes if you specify that the
  * parser should look for Program Instructions or CDATA sections. Nothing will
  * be be returned from inside the instructions. <BR>
  * <BR>
  * Copyright (C)2001 Jason Pell. <BR>
  * 
  * <PRE>
  * 	This program is free software; you can redistribute it and/or
  * 	modify it under the terms of the GNU General Public License
  * 	as published by the Free Software Foundation; either version 2
  * 	of the License, or (at your option) any later version.
  * 	<BR>
  * 	This program is distributed in the hope that it will be useful,
  * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
  * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * 	GNU General Public License for more details.
  * 	<BR>
  * 	You should have received a copy of the GNU General Public License
  * 	along with this program; if not, write to the Free Software
  * 	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  * 	<BR>
  * 	Email: 	jasonpell@hotmail.com
  * 	Url:	http://www.geocities.com/SiliconValley/Haven/9778
  * </PRE>
  * 
  * @version 0.11 Updated parseFunction to include ( and ) even if function name
  *          is valid. Also added methods to set ignoreComments, ignoreCDAta,
  *          ignorePI. Added a new constructor to set these variables as well.
  *          06/08/2001
  * @version 0.10 Improvements to functions. Functions are now defined as
  *          $name(args) A function can also be $(args), but only if a function
  *          name of "" has been included in the constructor or reset. Also added
  *          parseArgument, so that arguments within functions would be returned
  *          from the getArguments() method as an array of strings, with any
  *          delimiters, such as " or ' removed. Also improved CDATA and PI so
  *          that if they are not being parsed, then tags/functions/entities
  *          inside them can be parsed.
  * @version 0.01 Initial version
  * @author Jason Pell
  */
 
 public class HxmlTokeniser {
 	/**
 	 * Html/XML start tag.
 	 */
 	public static final int START_TAG = 1;
 
 	/**
 	 * HTML/XML End tag.
 	 */
 	public static final int END_TAG = 2;
 
 	/**
 	 * XML Empty tag.
 	 */
 	public static final int EMPTY_TAG = 3;
 
 	/**
 	 * HTML/XML &amp;entity;
 	 */
 	public static final int ENTITY = 4;
 
 	/**
 	 * Specific to my purpose, is a special new token $function(...)
 	 */
 	public static final int FUNCTION = 5;
 
 	/**
 	 * XML CDATA (Character Data)
 	 */
 	public static final int CDATA = 6;
 
 	/**
 	 * XML Processing Instruction &lt;?application ... ?&gt;
 	 */
 	public static final int PI = 7;
 
 	/**
 	 * Comment
 	 */
 	public static final int COMMENT = 8;
 
 	/**
 	 * @param tags
 	 *            Specify any tag names that are recognised.
 	 * @param entities
 	 *            Specify any entity names that are recognised.
 	 * @param functions
 	 *            Specify any function names that are to be recognised.
 	 * 
 	 *            tags, entities and functions are all case insensitive.
 	 */
 	public HxmlTokeniser(Reader reader) {
 		dataBuffer = new StringBuilder();
 		
 		// Only supports 10 character readback. Need to increase if
 		// we have any big patterns to look for in parseUntil.
 		this.reader = new LineNumberReader(reader);
 	}
 
 	/**
 	 * Return type of last token found with nextToken.
 	 */
 	public int getTokenType() {
 		return tokenType;
 	}
 
 	/**
 	 * Debug method.
 	 */
 	public String getTypeAsString() {
 		switch (getTokenType()) {
 		case START_TAG:
 			return "START_TAG";
 		case END_TAG:
 			return "END_TAG";
 		case EMPTY_TAG:
 			return "EMPTY_TAG";
 		case CDATA:
 			return "CDATA";
 		case PI:
 			return "PI";
 		case COMMENT:
 			return "COMMENT";
 		default:
 			return "Not Defined";
 		}
 	}
 
 	/**
 	 * Return name of last token found with nextToken.
 	 * 
 	 * If tokenType == START_TAG, END_TAG or EMPTY_TAG, the tokenName, will be
 	 * the tag name. If tokenType == ENTITY, the tokenName will be the actual
 	 * entity. If tokenType == FUNCTION, the tokenName will be the function name
 	 * (minus arguments) If tokenType == PI, the tokenName is the Application.
 	 * Otherwise it will be null.
 	 */
 	public String getTokenName() {
 		return tokenName;
 	}
 
 	/**
 	 * Will return attributes if getTokenType()==START_TAG or EMPTY_TAG,
 	 * otherwise return null. Even if START_TAG or EMPTY_TAG, the attribute may
 	 * not be found, in which case this method will return null anyway.
 	 */
 	public String getAttribute(String name) {
 		if (htAttributes != null && tokenType == START_TAG
 				|| tokenType == EMPTY_TAG) {
 			return (String) htAttributes.get(name);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Will return attributes if getTokenType()==START_TAG or EMPTY_TAG,
 	 * otherwise return null. The Enumeration may be empty.
 	 */
 	public Collection<String> getAttributes() {
 		if (htAttributes != null && tokenType == START_TAG
 				|| tokenType == EMPTY_TAG) {
 			return htAttributes.keySet();
 		} else {
 			return Collections.EMPTY_LIST;
 		}
 	}
 
 	/**
 	 * If a parse exception occurs.
 	 * 
 	 * @exception IOException
 	 *                Bubbles up from unread(...), read(...) or readInt(...).
 	 */
 	public boolean nextToken() throws IOException {
 		// reset all variables for nextToken.
 		reset();
 
 		// Go with a int at the top level, so we can test for -1.
 		int i;
 		char c;
 		while (true) {
 			i = readInt();
 
 			// Store end of text, in case this is it...
 			endOfTextIndex = getIndex();
 
 			switch (i) {
 			case '<':
 				append('<');
 
 				c = read();
 				switch (c) {
 				case '!': // Comment or CDATA...
 					append('!');
 					c = read();
 					switch (c) {
 					case '-':
 						require('-');
 						append("--");
 						parseComment();
 						break;
 					case '[':
 						require("CDATA[");
 						append("[CDATA[");
 						parseCData();
 						break;
 					default:
 						// Unread the last character.
 						unread(c);
 						// Nothing, continue as before.
 						endOfTextIndex = -1;
 					}
 					break; // case '!'
 				case '?': // Processing Instructions
 					append('?');
 					parsePI();
 					break;
 				case '/': // End Tag.
 					append('/');
 					parseEndTag();
 					break;
 				default: // Normal Element.
 					unread(c);
 					parseStartTag();
 					break;
 				}
 				break; // case '<'
 
 			case -1: // End Of File
 				return false;
 
 			default: // Normal text.
 				append((char) i);
 				// Set it back now.
 				endOfTextIndex = -1;
 
 			} // End of outer switch.
 
 			// If !=-1, this indicates that we have parsed a token correctly.
 			if (endOfTextIndex != -1) {
 				return true;
 			}
 		}
 	}
 
 	/**
 	 * Get the text of the last token encountered.
 	 * 
 	 * May not return the complete contents of START_TAG, EMPTY_TAG, END_TAG,
 	 * ENTITY or FUNCTION. Really only designed for use by COMMENT, CDATA and
 	 * PI.
 	 */
 	public String getTokenContent() {
 		if (endOfTextIndex != -1 && dataBuffer.length() > 0) {
 			return getString(endOfTextIndex, dataBuffer.length());
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Will return null, if no text available.
 	 */
 	public String getText() {
 		if (endOfTextIndex != -1 && dataBuffer.length() > 0) {
 			return getString(0, endOfTextIndex);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the current line number. The line number returned from the
 	 * reader, is actually increased by 1 (one) before return, to take into
 	 * account the first line, which is not counted until its end.
 	 */
 	public int getLineNumber() {
 		if (reader != null) {
 			return reader.getLineNumber() + 1;
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Ignore CDATA
 	 */
 	public boolean isIgnoreCData() {
 		return ignoreCData;
 	}
 
 	/**
 	 * Ignore Programming Instructions
 	 */
 	public boolean isIgnorePI() {
 		return ignorePI;
 	}
 
 	/**
 	 * Ignore comments
 	 */
 	public boolean isIgnoreComments() {
 		return ignoreComments;
 	}
 
 	/**
 	 * Set ignoreComments indicator
 	 */
 	public void ignoreComments(boolean b) {
 		this.ignoreComments = b;
 	}
 
 	/**
 	 * Set ignoreCData indicator
 	 */
 	public void ignoreCData(boolean b) {
 		this.ignoreCData = b;
 	}
 
 	/**
 	 * Set ignorePI indicator
 	 */
 	public void ignorePI(boolean b) {
 		this.ignorePI = b;
 	}
 
 	private void unread(int c) throws IOException {
 		if (pos == 0) {
 			throw new IOException("Pushback buffer overflow");
 		}
 		buf[--pos] = (char) c;
 	}
 
 	/**
 	 * Unread from c length characters back into buffer.
 	 */
 	private void unread(char cbuf[], int len) throws IOException {
 		// Because the position is subtracted when a character
 		// is added to buffer
 		if (len > pos) {
 			throw new IOException("Pushback buffer overflow");
 		}
 		pos -= len;
 		System.arraycopy(cbuf, 0, buf, pos, len);
 	}
 
 	private char read() throws IOException {
 		return (char) readInt();
 	}
 
 	private int readInt() throws IOException {
 		if (pos < buf.length) {
 			return buf[pos++];
 		} else {
 			return reader.read();
 		}
 	}
 
 	private void append(char c) {
 		dataBuffer.append(c);
 	}
 
 	private void append(String s) {
 		dataBuffer.append(s);
 	}
 
 	/**
 	 * Skip whitespace characters in input.
 	 */
 	private void skipWhitespace() throws IOException {
 		char c;
 		while (Character.isWhitespace((c = read()))) {
 			// do nothing
 		}
 		unread(c);
 	}
 
 	/**
 	 * Parse any whitespace and add to the buffer.
 	 */
 	private void parseWhitespace() throws IOException {
 		char c;
 		while (Character.isWhitespace((c = read()))) {
 			append(c);
 		}
 		unread(c);
 	}
 
 	/**
 	 * The next character in the input, must be c.
 	 */
 	private void require(char required) throws IOException {
 		char c = read();
 		if (c != required) {
 			unread(c);
 			throw new IOException("Line:" + getLineNumber() + "; Character '"
 					+ required + "' not found: " + c);
 		}
 	}
 
 	/**
 	 * Calls require(char) for each character in String s.
 	 */
 	private void require(String s) throws IOException {
 		for (int i = 0; i < s.length(); i++) {
 			require(s.charAt(i));
 		}
 	}
 
 	/**
 	 * Returns the index of the last character added to the dataBuffer, or 0 if
 	 * not characters are currently stored in the dataBuffer.
 	 */
 	private int getIndex() {
 		if (dataBuffer.length() > 0) {
 			return dataBuffer.length();
 		} else {
 			return 0;
 		}
 	}
 
 	/**
 	 * Returns a substring from the dataBuffer, starting at startIndex and
 	 * length characters long. An IndexOutOfBoundsException may be thrown if the
 	 * parameters are invalid.
 	 */
 	private String getString(int startIndex, int stopIndex) {
 		char text[] = new char[stopIndex - startIndex];
 		dataBuffer.getChars(startIndex, stopIndex, text, 0);
 		return new String(text);
 	}
 
 	/**
 	 * Should unread the next character after the name ends, so that the calling
 	 * process can continue correctly.
 	 * 
 	 * Assumes whitespace has already been skipped.
 	 * 
 	 * @see #isLegalNameChar(char)
 	 */
 	private String getNmToken() throws IOException {
 		// Store start of name.
 		int indexOfStart = getIndex();
 
 		char c;
 		c = read();
 
 		// If not valid first character.
 		if (c != '*' && c != '_' && c != ':' && !Character.isLetter(c)) {
 			throw new IOException("Line:" + getLineNumber()
 					+ "; Invalid initial name char: " + c);
 		}
 		append(c);
 
 		// Now keep going while isLetterOrDigit.
 		while (isLegalNameChar((c = read()))) {
 			append(c);
 		}
 		unread(c);
 
 		// Now return the name.
 		return getString(indexOfStart, getIndex());
 	}

 	/**
 	 * Is legal name character. This is only for characters after the initial
 	 * one, because the initial character cannot be a number, whereas this
 	 * method will return true for a number.
 	 * 
 	 * <b>Note:</b> Tag, Entity, ProcessingInstruction and Function names must
 	 * be of the following form: First char: ('_'|':'|[a-zA-Z]) The rest:
 	 * ('_'|[a-zA-Z0-9])
 	 */
 	private boolean isLegalNameChar(char c) {
		return (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '*');
 	}
 
 	/**
 	 * At least one character of whitespace.
 	 */
 	private void requireWhitespace() throws IOException {
 		char c = read();
 		if (Character.isWhitespace(c)) {
 			skipWhitespace();
 		} else {
 			throw new IOException("Line:" + getLineNumber()
 					+ "; Whitespace required: " + c);
 		}
 	}
 
 	/**
 	 * Assumes the <!-- has already been parsed.
 	 */
 	private void parseComment() throws IOException {
 		// Ignores anything, even if a -> is found, it will not be treated like
 		// a comment,
 		// even though technically it should be.
 		parseUntil("-->");
 		// So we can get at the whole comment string.
 		append("-->");
 
 		if (!ignoreComments) {
 			// Could change this if need be.
 			tokenName = null;
 			tokenType = COMMENT;
 		} else {
 			endOfTextIndex = -1;
 		}
 
 	}
 
 	/**
 	 * Parses XML <![CDATA[ unparsed data ]]> Assumes the <![CDATA[ has already
 	 * been parsed.
 	 */
 	private void parseCData() throws IOException {
 		if (!ignoreCData) {
 			parseUntil("]]>");
 			append("]]>");
 
 			// Could change this if need be.
 			tokenName = null;
 			tokenType = CDATA;
 		} else {
 			endOfTextIndex = -1;
 		}
 	}
 
 	/**
 	 * Parse Processing instructions. Assumes the <? has already been parsed.
 	 */
 	private void parsePI() throws IOException {
 		String name = getNmToken();
 		if (!ignorePI) {
 			// Does not matter what name is returned, Processing instruction
 			// should be skipped anyway.
 			if (!tryRead("?>")) {
 				requireWhitespace();
 				// Append one character of whitespace, so we can get the
 				// contents of PI if required.
 				append(' ');
 				parseUntil("?>");
 				append("?>");
 			}
 
 			tokenName = name;
 			tokenType = PI;
 		} else {
 			endOfTextIndex = -1;
 		}
 	}
 
 	/**
 	 * This parses a START_TAG or EMPTY_TAG. Assumes the < has already been
 	 * parsed.
 	 */
 	private void parseStartTag() throws IOException {
 		String name = getNmToken();
 		skipWhitespace();
 		char c = read();
 		while (c != '/' && c != '>') {
 			unread(c);
 			parseAttribute();
 			skipWhitespace();
 			c = read();
 		}
 
 		// Now we are at the end of the element.
 		switch (c) {
 		case '>':
 			tokenName = name;
 			tokenType = START_TAG;
 			break;
 		case '/':
 			require('>');
 			tokenName = name;
 			tokenType = EMPTY_TAG;
 			break;
 		}
 	}
 
 	/**
 	 * Assumes that the </ have already been found.
 	 */
 	private void parseEndTag() throws IOException {
 		String name = getNmToken();
 
 		// Check that it is legal.
 		skipWhitespace();
 		// In case some person wanted to put a datasource in the end tag as
 		// well.
 		parseUntil('>');
 		// require('>');
 		tokenName = name;
 		tokenType = END_TAG;
 	}
 
 	/**
 	 * Assume we should be at the start of the next name=value.
 	 * 
 	 * If the attribute does not start with a &quot; character, then there
 	 * cannot be any whitespace.
 	 */
 	private void parseAttribute() throws IOException {
 		// Get the attribute name.
 		String name = getNmToken();
 		skipWhitespace();
 		require('=');
 		skipWhitespace();
 
 		// Get Delimiter.
 		char delim = read();
 
 		if (delim != '\"' && delim != '\'') {
 			unread(delim);
 			delim = '\0';
 		}
 
 		int indexOfStart = getIndex();
 		char c = read();
 		while (true) {
 			if (delim == '\0') {
 				if (c == '/' || c == '>' || Character.isWhitespace(c)) {
 					unread(c);
 					break;
 				}
 			} else {
 				// Ensure previous was not an escape character.
 				if (getString(getIndex() - 1, getIndex()).charAt(0) != '\\'
 						&& c == delim) {
 					break;
 				} else if (c == '\n' || c == '\r') {
 					throw new IOException("Line:" + getLineNumber()
 							+ "; Newline in attribute value.");
 				} else if (c == '/' || c == '>') {
 					throw new IOException("Line:" + getLineNumber()
 							+ "; Attribute value was not closed correctly: "
 							+ c);
 				}
 			}
 
 			append(c);
 
 			// Read next character.
 			c = read();
 		}
 
 		String value = getString(indexOfStart, getIndex());
 		htAttributes.put(name, value);
 	}
 
 	/**
 	 * We know that we have read in the required character if no exception is
 	 * thrown indicating end of stream.
 	 */
 	private void parseUntil(char chr) throws IOException {
 		char c;
 		while ((c = read()) != chr) {
 			append(c);
 		}
 	}
 
 	/**
 	 * Attempts to read the next character, and if it is not the one expected,
 	 * the character read is pushed back.
 	 */
 	private boolean tryRead(char chr) throws IOException {
 		char c = read();
 		if (c == chr) {
 			return true;
 		} else {
 			unread(c);
 			return false;
 		}
 	}
 
 	/**
 	 * Attempt to read the next s.length() characters, if they match s, then
 	 * return true, otherwise push characters back and return false.
 	 */
 	private boolean tryRead(String s) throws IOException {
 		char c[] = new char[s.length()];
 		for (int i = 0; i < s.length(); i++) {
 			c[i] = read();
 			if (c[i] != s.charAt(i)) {
 				if (i == 0) {
 					unread(c[0]);
 				} else {
 					unread(c, i);
 				}
 				return false;
 			}
 		}
 		// Finished for loop, so must have been successful.
 		return true;
 	}
 
 	/**
 	 * Keep reading (and appending) characters until the pattern is found.
 	 */
 	private void parseUntil(String pattern) throws IOException {
 		// Will only ever have to unread one less than the pattern.
 		char arr[] = new char[pattern.length() - 1];
 
 		// So when we break out of the for loop it continues while.
 		retry: while (true) {
 			// Get first character.
 			parseUntil(pattern.charAt(0));
 			for (int i = 1; i < pattern.length(); i++) {
 				arr[i - 1] = read();
 				if (pattern.charAt(i) != arr[i - 1]) {
 					// Unread any extra characters.
 					if (i == 1) {
 						unread(arr[i - 1]);
 					} else {
 						unread(arr, i - 1);
 					}
 
 					// Retry while loop.
 					continue retry;
 				}
 			}
 			// Finished for loop, so break out completely, as we found what we
 			// were looking for.
 			break;
 		}
 	}
 
 	/**
 	 * Reset the dataBuffer to zero length. Presumably after the getText()
 	 * method was called.
 	 */
 	private void reset() {
 		dataBuffer.setLength(0);
 		endOfTextIndex = -1;
 		if (htAttributes != null) {
 			htAttributes.clear();
 		}
 		tokenType = -1;
 		tokenName = null;
 	}
 
 	/**
 	 * Search for name in names array.
 	 */
 	private boolean isFound(String name, String[] names, boolean ignoreCase) {
 		for (int i = 0; i < names.length; i++) {
 			if (ignoreCase) {
 				if (name.equalsIgnoreCase(names[i])) {
 					return true;
 				}
 			} else {
 				if (name.equals(names[i])) {
 					return true;
 				}
 			}
 		}
 		// Nothing found.
 		return false;
 	}
 
 	/**
 	 * Instead of using pushbackbuffer, lets use this instead.
 	 */
 	private char[] buf = new char[10];
 	private int pos = buf.length;
 
 	/**
 	 * Should the parser treat comments as normal text?
 	 */
 	private boolean ignoreComments = false;
 
 	/**
 	 * Should the parser should treat xml CDATA blocks as normal text?
 	 */
 	private boolean ignoreCData = false;
 
 	/**
 	 * Should the parser should treat xml Processing Instructions as normal
 	 * text?
 	 */
 	private boolean ignorePI = false;
 
 	/**
 	 * Store the name of the Tag/Entity/Function here.
 	 */
 	private String tokenName = null;
 
 	/**
 	*/
 	private int tokenType = -1;
 
 	/**
 	 * Stores any attributes for the last element encountered with nextToken, as
 	 * long as the type is START_TAG or EMPTY_TAG.
 	 */
 	private final Map<String, String> htAttributes = new HashMap<String, String>();
 
 	/**
 	 * Stores the reader we want to read from.
 	 */
 	private LineNumberReader reader = null;
 
 	/**
 	 * Stores the data accessed while searching for the current tag.
 	 */
 	private StringBuilder dataBuffer = null;
 
 	/**
 	 * When the start of a Tag/Entity/Function is encountered, this is set, so
 	 * we only return the text before...
 	 */
 	private int endOfTextIndex = -1;
 }
