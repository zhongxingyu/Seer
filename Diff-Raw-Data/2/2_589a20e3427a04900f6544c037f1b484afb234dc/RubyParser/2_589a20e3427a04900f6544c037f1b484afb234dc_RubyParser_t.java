 /*
  * Author: C.Williams
  *
  *  Copyright (c) 2004 RubyPeople. 
  *
  *  This file is part of the Ruby Development Tools (RDT) plugin for eclipse.
  *  You can get copy of the GPL along with further information about RubyPeople 
  *  and third party software bundled with RDT in the file 
  *  org.rubypeople.rdt.core_0.4.0/RDT.license or otherwise at 
  *  http://www.rubypeople.org/RDT.license.
  *
  *  RDT is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  RDT is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with RDT; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.rubypeople.rdt.internal.core.parser;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class RubyParser {
 
 	private static RubyParserStack stack = new RubyParserStack();
 	private static RubyScript script;
 	private static final char[] VARIABLE_END_CHARS = { ' ', '.', '[', '(', ')', ']', ',', '}', '{'};
 	private static boolean inDocs;
 
 	private static final String START_OF_STATEMENT_REGEX = "^(;\\s+)?\\s*";
 	private static final Pattern WHILE_PATTERN = Pattern.compile(START_OF_STATEMENT_REGEX + "while ");
 	private static final Pattern UNLESS_PATTERN = Pattern.compile(START_OF_STATEMENT_REGEX + "unless ");
 	private static final Pattern UNTIL_PATTERN = Pattern.compile(START_OF_STATEMENT_REGEX + "until ");
 	private static final Pattern CASE_PATTERN = Pattern.compile(START_OF_STATEMENT_REGEX + "case ");
 	private static final Pattern IF_PATTERN = Pattern.compile(START_OF_STATEMENT_REGEX + "if ");
 	private static final Pattern END_PATTERN = Pattern.compile("^\\s*(.+\\s+)?end(\\..+)?(\\s+.+)?$");
 	private static final Pattern DO_PATTERN = Pattern.compile(".+\\s+do\\s*$");
 
 	/**
 	 * @return
 	 */
 	public static RubyScript parse(String string) throws ParseException {
 		script = new RubyScript();
 		stack.push(script);
 		BufferedReader reader = new BufferedReader(new StringReader(string));
 		String curLine = null;
 		int lineNum = 0;
 		int offset = 0;
 		try {
 			while ((curLine = reader.readLine()) != null) {
 				String myLine = curLine;
 				if (inDocs) {
 					myLine = findMultiLineDocEnd(curLine);
 				}
 				if (inDocs) {
 					offset = 0;
 					lineNum++;
 					continue;
 				}
 
 				myLine = removeAfterPoundSymbol(myLine);
 				findMultiLineDocBeginning(myLine);
 				if (inDocs) {
 					offset = 0;
 					lineNum++;
 					continue;
 				}
 				findBegin(myLine, lineNum);
 				findIf(myLine, lineNum);
 				findCase(myLine, lineNum);
 				findUnless(myLine, lineNum);
 				findUntil(myLine, lineNum);
 				findWhile(myLine, lineNum);
 				findFor(myLine, lineNum);
 				findDo(myLine, lineNum);
 				findRequires(myLine, lineNum);
 				findModule(myLine, lineNum);
 				findClass(myLine, lineNum);
 				findMethod(myLine, lineNum);
 				findClassVariable(myLine, lineNum);
 				findInstanceVariable(myLine, lineNum);
 				findGlobal(myLine, lineNum);
 				findPrivateModifier(myLine, lineNum);
 				findAttributeReaderModifier(myLine, lineNum);
 				findAttributeWriterModifier(myLine, lineNum);
 				findAttributeAccessor(myLine, lineNum);
 				findEnd(myLine, lineNum);
 				offset = 0;
 				lineNum++;
 			}
 		} catch (IOException e) {
 			throw new ParseException(e);
 		}
 		script.setEnd(lineNum, offset);
 
 		stack.clear();
 		return script;
 	}
 
 	/**
 	 * @param myLine
 	 * @param lineNum
 	 */
 	private static void findDo(String myLine, int lineNum) {
 		Matcher doMatcher = DO_PATTERN.matcher(myLine);
 		if (doMatcher.find()) {
 			pushMultiLineElement(new RubyDo(lineNum, myLine.indexOf("do")));
 		}
 	}
 
 	/**
 	 * Wraps the RubyParserStack's pushAndLink method with a try/throws clause
 	 * to log any StackEmptyExceptions
 	 * 
 	 * @param element
 	 *            the RubyElement to be pushed onto the stack and added to the
 	 *            last open element
 	 */
 	private static void pushMultiLineElement(RubyElement element) {
 		try {
 			stack.pushAndLink(element);
 		} catch (StackEmptyException e) {
 			log(e.getMessage());
 		}
 	}
 
 	/**
 	 * @param curLine
 	 */
 	private static String findMultiLineDocEnd(String curLine) {
 		final String token = "=end";
 		if (curLine.indexOf(token) == -1) return curLine;
 		inDocs = false;
 		return curLine.substring(endIndexOf(curLine, token));
 	}
 
 	/**
 	 * @param myLine
 	 */
 	private static void findMultiLineDocBeginning(String myLine) {
 		if (myLine.indexOf("=begin") != -1) {
 			inDocs = true;
 		}
 	}
 
 	/**
 	 * @param myLine
 	 */
 	private static void findAttributeAccessor(String myLine, int lineNum) {
 		findAccessModifier(myLine, RubyElement.PUBLIC, "attr_accessor ", "@", lineNum);
 	}
 
 	/**
 	 * @param myLine
 	 */
 	private static void findAttributeWriterModifier(String myLine, int lineNum) {
 		findAccessModifier(myLine, RubyElement.WRITE, "attr_writer ", "@", lineNum);
 	}
 
 	/**
 	 * @param myLine
 	 */
 	private static void findAttributeReaderModifier(String myLine, int lineNum) {
 		findAccessModifier(myLine, RubyElement.READ, "attr_reader ", "@", lineNum);
 	}
 
 	/**
 	 * @param myLine
 	 */
 	private static void findAccessModifier(String myLine, String accessRightsToGrant, String accessModifierTag, String symbolPrefix, int lineNum) {
 		if (myLine.indexOf(accessModifierTag) == -1) return;
 
 		List tokens = getSymbols(myLine, accessModifierTag);
 		RubyElement parent = stack.findParentClassOrModule();
 		if (parent == null) return;
 
 		for (Iterator iter = tokens.iterator(); iter.hasNext();) {
 			String elementName = (String) iter.next();
 			RubyElement element = parent.getElement(symbolPrefix + elementName);
 			if (element != null) {
 				element.setAccess(accessRightsToGrant);
 				continue;
 			}
			script.addParseError(new ParseError("Attempting to set access rights " + accessRightsToGrant + " to an unknown element " + elementName, lineNum, myLine.indexOf(elementName), myLine.indexOf(elementName) + elementName.length()));
 		}
 	}
 
 	/**
 	 * @param myLine
 	 */
 	private static void findPrivateModifier(String myLine, int lineNum) {
 		findAccessModifier(myLine, RubyElement.PRIVATE, "private ", "", lineNum);
 	}
 
 	/**
 	 * Returns a list of symbol names given string of the format: "prefix
 	 * :symbol, :symbolTwo"
 	 * 
 	 * @param myLine
 	 * @return
 	 */
 	private static List getSymbols(String myLine, String prefix) {
 		String copy = myLine.substring(endIndexOf(myLine, prefix));
 		StringTokenizer tokenizer = new StringTokenizer(copy, ", ");
 		List list = new ArrayList();
 		while (tokenizer.hasMoreTokens()) {
 			list.add(tokenizer.nextToken().substring(1));
 		}
 		return list;
 	}
 
 	/**
 	 * Returns the index of the last char of <code>token</code> in <code>myLine</code>.
 	 * Returns -1 if there are no occurences. (myLine.indexOf(token) +
 	 * token.length())
 	 * 
 	 * @param myLine
 	 * @param token
 	 * @return
 	 */
 	private static int endIndexOf(String myLine, String token) {
 		if (myLine.indexOf(token) == -1) return -1;
 		return myLine.indexOf(token) + token.length();
 	}
 
 	/**
 	 * @param lineNum
 	 * @param myLine
 	 */
 	private static void findBegin(String myLine, int lineNum) {
 		int beginIndex = myLine.indexOf("begin");
 		if (beginIndex == -1) return;
 		pushMultiLineElement(new RubyBegin(lineNum, beginIndex));
 	}
 
 	/**
 	 * @param lineNum
 	 * @param myLine
 	 */
 	private static void findFor(String myLine, int lineNum) {
 		int forIndex = myLine.indexOf("for ");
 		if ((forIndex != -1) && (!inQuotes(forIndex, myLine))) {
 			pushMultiLineElement(new RubyFor(lineNum, forIndex));
 		}
 	}
 
 	/**
 	 * @param lineNum
 	 * @param myLine
 	 */
 	private static void findWhile(String myLine, int lineNum) {
 		Matcher whileMatcher = WHILE_PATTERN.matcher(myLine);
 		if (whileMatcher.find()) {
 			try {
 				if (!(stack.peek() instanceof RubyCase)) {
 					pushMultiLineElement(new RubyWhile(lineNum, whileMatcher.end() - 6));
 				}
 			} catch (StackEmptyException e) {
 				log(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * @param curLine
 	 * @return
 	 */
 	private static String removeAfterPoundSymbol(String curLine) {
 		int poundStart = curLine.indexOf("#");
 		if (poundStart == -1) return curLine;
 		if (inQuotes(poundStart, curLine) || inRegex(poundStart, curLine)) { return curLine; }
 		return curLine.substring(0, poundStart);
 	}
 
 	/**
 	 * Returns true if the index position in curLine is inside quotes
 	 * 
 	 * @param index
 	 *            The index position to check
 	 * @param curLine
 	 *            The String to check @returntrue if the index position in
 	 *            curLine is inside quotes
 	 */
 	private static boolean inQuotes(int index, String curLine) {
 		List openQuotes = new ArrayList();
 		for (int curPosition = 0; curPosition < index; curPosition++) {
 			char c = curLine.charAt(curPosition);
 			if (isQuoteChar(c)) {
 				Character newChar = new Character(c);
 				if (!openQuotes.isEmpty()) {
 					Character open = (Character) openQuotes.get(openQuotes.size() - 1);
 					if (newChar.equals(open)) {
 						openQuotes.remove(openQuotes.size() - 1);
 						continue;
 					}
 				}
 				openQuotes.add(newChar);
 			}
 		}
 		return !openQuotes.isEmpty() || inPercentString('q', index, curLine) || inPercentString('Q', index, curLine);
 	}
 
 	/**
 	 * @param index
 	 * @param curLine
 	 * @return
 	 */
 	static boolean inPercentString(char type, int index, String curLine) {
 		int end = endIndexOf(curLine, "%" + type);
 		if (end == -1) return false;
 		if (end > index) return false;
 		char c = curLine.charAt(end);
 		if (isOpenBracket(c)) c = getMatchingBracket(c);
 		for (int i = end + 1; i < index; i++) {
 			if (curLine.charAt(i) == c && curLine.charAt(i-1) != '\\') { return false; }
 		}
 		return true;
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findUntil(String curLine, int lineNum) {
 		Matcher untilMatcher = UNTIL_PATTERN.matcher(curLine);
 		if (untilMatcher.find()) {
 			pushMultiLineElement(new RubyUntil(lineNum, untilMatcher.end() - 6));
 		}
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findCase(String curLine, int lineNum) {
 		Matcher caseMatcher = CASE_PATTERN.matcher(curLine);
 		if (caseMatcher.find()) {
 			pushMultiLineElement(new RubyCase(lineNum, caseMatcher.end() - 5));
 		}
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findUnless(String curLine, int lineNum) {
 		Matcher unlessMatcher = UNLESS_PATTERN.matcher(curLine);
 		if (unlessMatcher.find()) {
 			pushMultiLineElement(new RubyUnless(lineNum, unlessMatcher.end() - 7));
 		}
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findIf(String curLine, int lineNum) {
 		Matcher ifMatcher = IF_PATTERN.matcher(curLine);
 		if (ifMatcher.find()) {
 			pushMultiLineElement(new RubyIf(lineNum, ifMatcher.end() - 3));
 		}
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findGlobal(String curLine, int lineNum) {
 		int globalIndex = curLine.indexOf("$");
 		if (globalIndex == -1) return;
 		if ((inQuotes(globalIndex, curLine) || inRegex(globalIndex, curLine)) && !isSubstituted(globalIndex, curLine)) return;
 		String name = getToken("$", VARIABLE_END_CHARS, curLine);
 		script.addElement(new RubyGlobal("$" + name, lineNum, globalIndex));
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findClassVariable(String curLine, int lineNum) {
 		int instanceIndex = curLine.indexOf("@@");
 		if (instanceIndex == -1) return;
 		if ((inQuotes(instanceIndex, curLine) || inRegex(instanceIndex, curLine)) && !isSubstituted(instanceIndex, curLine)) return;
 		String name = getToken("@@", VARIABLE_END_CHARS, curLine);
 		addVariable(new RubyClassVariable("@@" + name, lineNum, instanceIndex));
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findInstanceVariable(String curLine, int lineNum) {
 		int instanceIndex = curLine.indexOf("@");
 		if (instanceIndex == -1) return;
 		if (curLine.indexOf("@@") != -1) return;
 		if ((inQuotes(instanceIndex, curLine) || inRegex(instanceIndex, curLine)) && !isSubstituted(instanceIndex, curLine)) return;
 		String name = getToken("@", VARIABLE_END_CHARS, curLine);
 		addVariable(new RubyInstanceVariable("@" + name, lineNum, instanceIndex));
 	}
 
 	/**
 	 * @param variable
 	 */
 	private static void addVariable(RubyElement variable) {
 		RubyElement element = stack.findParentClassOrModule();
 		if (element == null) {
 			element = script;
 		}
 		element.addElement(variable);
 		log("Added variable to open element: " + element);
 	}
 
 	/**
 	 * @param instanceIndex
 	 * @param curLine
 	 */
 	private static boolean isSubstituted(int instanceIndex, String curLine) {
 		return ((curLine.charAt(instanceIndex - 1) == '#') || (curLine.substring(instanceIndex - 2, instanceIndex).equals("#{")));
 	}
 
 	/**
 	 * Returns true if the given index in the String curLine is inside a
 	 * regular expression
 	 * 
 	 * @param index
 	 *            the int position to check
 	 * @param curLine
 	 *            the String to check within
 	 * @return
 	 */
 	private static boolean inRegex(int index, String curLine) {
 
 		boolean insideregex = false;
 		for (int curPosition = 0; curPosition < index; curPosition++) {
 			if (curLine.charAt(curPosition) == '/') {
 				insideregex = !insideregex;
 			}
 		}
 		return insideregex || inPercentRegex(index, curLine);
 	}
 
 	/**
 	 * @param index
 	 * @param curLine
 	 * @return
 	 */
 	private static boolean inPercentRegex(int index, String curLine) {
 		return inPercentString('r', index, curLine);
 	}
 
 	/**
 	 * Returns the matching bracket for a given character. If it is not a
 	 * bracket, returns the newline character
 	 * 
 	 * @param c
 	 * @return
 	 */
 	static char getMatchingBracket(char c) {
 		switch (c) {
 		case '(':
 			return ')';
 		case '{':
 			return '}';
 		case '[':
 			return ']';
 		default:
 			return '\n';
 		}
 	}
 
 	/**
 	 * @param c
 	 * @return
 	 */
 	static boolean isOpenBracket(char c) {
 		return contains(new char[] { '(', '{', '['}, c);
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findEnd(String curLine, int lineNum) {
 		Matcher match = END_PATTERN.matcher(curLine);
 		if (match.find()) {
 			log("Found end: " + curLine);
 			try {
 				stack.closeLastOpenElement(lineNum, curLine.indexOf("end"));
 			} catch (StackEmptyException e) {
 				log(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findRequires(String curLine, int lineNum) {
 		final String token = "require ";
 		int start = endIndexOf(curLine, token);
 		if (start == -1) return;
 		String leftOver = curLine.substring(start);
 		for (int i = 0; i < leftOver.length(); i++) {
 			char c = leftOver.charAt(i);
 			if (!isQuoteChar(c)) continue;
 			String name = getToken(token + c, new char[] { c}, curLine);
 			RubyRequires requires = new RubyRequires(name, lineNum, start + 1);
 			if (!script.contains(requires)) {
 				script.addRequires(requires);
 			} else {
 				script.addParseError(new ParseError("Duplicate require statement unnecessary.", lineNum, requires.getStart().getOffset(), requires.getEnd().getOffset()));
 			}
 			return;
 		}
 	}
 
 	/**
 	 * Returns true if the given char c is ' or "
 	 * 
 	 * @param c
 	 *            character to test
 	 * @return boolean indicating if character is a quote
 	 */
 	private static boolean isQuoteChar(char c) {
 		return (c == '\'') || (c == '"');
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findClass(String curLine, int lineNum) {
 		char[] tokens = { ' ', ';'};
 		int location = findElement("class ", tokens, curLine);
 		if (location == -1) return;
 		String name = getToken("class ", tokens, curLine);
 		if (Character.isLowerCase(name.charAt(0))) script.addParseError(new ParseError("Class names should begin with an uppercase letter.", lineNum, location, location + name.length()));
 		pushMultiLineElement(new RubyClass(name, lineNum, location));
 	}
 
 	/**
 	 * Returns the startIndex of the element (given a tokenIdentifier prefix). -1
 	 * if the tokenIdentifier is not found, or the element has an empty name
 	 * 
 	 * @param tokenIdentifierThe
 	 *            prefix which denotes a particular token i.e "require "
 	 * @param tokens
 	 *            a character array containg characters which can mark the end
 	 *            of the element name
 	 * @param curLine
 	 *            The String we'll be checking for the element
 	 * @return
 	 */
 	private static int findElement(String tokenIdentifier, char[] tokens, String curLine) {
 		int start = endIndexOf(curLine, tokenIdentifier);
 		if (start == -1) return -1;
 		if (getToken(tokenIdentifier, tokens, curLine).length() == 0) return -1;
 		log("Found start of element: " + curLine);
 		return start;
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findModule(String curLine, int lineNum) {
 		char[] tokens = { ' ', ';'};
 		int start = findElement("module ", tokens, curLine);
 		if (start == -1) return;
 		String name = getToken("module ", tokens, curLine);
 		if (Character.isLowerCase(name.charAt(0))) script.addParseError(new ParseError("Module names should begin with an uppercase letter.", lineNum, start, start + name.length()));
 		pushMultiLineElement(new RubyModule(name, lineNum, start));
 	}
 
 	/**
 	 * @param curLine
 	 * @param lineNum
 	 */
 	private static void findMethod(String curLine, int lineNum) {
 		int start = endIndexOf(curLine, "def ");
 		if (start == -1) return;
 		String name = getMethodName(curLine);
 		pushMultiLineElement(new RubyMethod(name, lineNum, start));
 	}
 
 	/**
 	 * @param curLine
 	 * @return
 	 */
 	private static String getMethodName(String curLine) {
 		char[] tokens = { '(', ' '};
 		return getToken("def ", tokens, curLine);
 	}
 
 	/**
 	 * Given a string prefix, this method returns the next token (name) of the
 	 * line delimited by any character within the delimiters array. If the next
 	 * token is not ended by a member of the delimiters array, then the rest of
 	 * the string is returned.
 	 * 
 	 * @param prefix
 	 * @param delimiters
 	 * @param line
 	 * @return
 	 */
 	private static String getToken(String prefix, char[] delimiters, String line) {
 		int endOfPrefix = endIndexOf(line, prefix);
 		for (int i = endOfPrefix; i < line.length(); i++) {
 			if (contains(delimiters, line.charAt(i))) { return line.substring(endOfPrefix, i); }
 		}
 		return line.substring(endOfPrefix);
 	}
 
 	/**
 	 * Given a character array of characters and a character, the method
 	 * determines if the given character is contained in the array
 	 * 
 	 * @param chars
 	 * @param c
 	 * @return a boolean to indicate if this array contains the character
 	 */
 	private static boolean contains(char[] chars, char c) {
 		for (int i = 0; i < chars.length; i++) {
 			if (chars[i] == c) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @param string
 	 */
 	private static void log(String string) {
 		System.out.println(string);
 		//RubyPlugin.log(new Exception(string));
 	}
 }
