 /*
  * Copyright 2011 Jeroen Meetsma
  *
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.util.misc;
 
 import org.ijsberg.iglu.util.io.StreamSupport;
import org.junit.Test;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeMap;
 
 /**
  * Helper class containing string manipulation functions.
  *
  */
 public abstract class StringSupport {
 	/**
 	 * replaces the first occurrence of needle in haystack with newNeedle
 	 *
 	 * @param haystack input string
 	 * @param needle   string to place
 	 * @param index	position to place
 	 */
 	public static String insert(String haystack, String needle, int index) {
 		StringBuffer val = new StringBuffer(haystack);
 		val.insert(index, needle);
 		return val.toString();
 	}
 
 
 	public static String replaceFirst(String haystack, String needle, String newNeedle) {
 		StringBuffer val = new StringBuffer(haystack);
 		replaceFirst(val, needle, newNeedle);
 		return val.toString();
 	}
 
 
 	/**
 	 * replaces the first occurance of needle in haystack with newNeedle
 	 *
 	 * @param haystack  input string
 	 * @param needle	string to replace
 	 * @param newNeedle replacement
 	 */
 	public static void replaceFirst(StringBuffer haystack, String needle, String newNeedle) {
 		int idx = haystack.indexOf(needle);
 		if (idx != -1) {
 			haystack.replace(idx, idx + needle.length(), newNeedle);
 		}
 	}
 
 
 	public static String replaceLast(String haystack, String needle, String newNeedle) {
 		StringBuffer val = new StringBuffer(haystack);
 		replaceLast(val, needle, newNeedle);
 		return val.toString();
 	}
 
 	/**
 	 * replaces the last occurance of needle in haystack with newNeedle
 	 *
 	 * @param haystack  input string
 	 * @param needle	string to replace
 	 * @param newNeedle replacement
 	 */
 	public static void replaceLast(StringBuffer haystack, String needle, String newNeedle) {
 		int idx = haystack.lastIndexOf(needle);
 		if (idx != -1) {
 			haystack.replace(idx, idx + needle.length(), newNeedle);
 		}
 	}
 
 	/**
 	 * replaces all occurances of needle in haystack with newNeedle
 	 * the input itself is not modified
 	 *
 	 * @param haystack  input string
 	 * @param needle	string to replace
 	 * @param newNeedle replacement
 	 * @see StringSupport#replaceAll(StringBuffer, String, String) use StringBuffer input
 	 *      to do a series of invocations in a row with minimal performance impact
 	 */
 	public static String replaceAll(String haystack, String needle, String newNeedle) {
 		StringBuffer val = new StringBuffer(haystack);
 		replaceAll(val, needle, newNeedle);
 		return val.toString();
 	}
 
 	public static String replaceAll(String haystack, String needle, String newNeedle, int interval) {
 		StringBuffer val = new StringBuffer(haystack);
 		replaceAll(val, needle, newNeedle, interval);
 		return val.toString();
 	}
 
 
 	/**
 	 * Replaces a series of possible occurrences by a series of substitutes.
 	 *
 	 * @param haystack
 	 * @param needle
 	 * @param newNeedle
 	 * @return
 	 */
 	public static String replaceAll(String haystack, String[] needle, String newNeedle[]) {
 		if (needle.length != newNeedle.length) {
 			throw new IllegalArgumentException("length of original and replace values do not match (" + needle.length + " != " + newNeedle.length + " )");
 		}
 		StringBuffer buf = new StringBuffer(haystack);
 		for (int i = 0; i < needle.length; i++) {
 			replaceAll(buf, needle[i], newNeedle[i]);
 		}
 		return buf.toString();
 	}
 
 	/**
 	 * replaces all occurances of needle in haystack with newNeedle
 	 * the input itself is not modified
 	 *
 	 * @param haystack  input string
 	 * @param needle	string to replace
 	 * @param newNeedle replacement
 	 */
 	public static void replaceAll(StringBuffer haystack, String needle, String newNeedle) {
 /*		if(needle == null || "".equals(needle))
 		{
 			throw new IllegalArgumentException("string to replace may not be empty");
 		}
 		int idx = haystack.indexOf(needle);
 		int needleLength = needle.length();
 		int newNeedleLength = newNeedle.length();
 		while (idx != -1)
 		{
 			haystack.replace(idx, idx + needleLength, newNeedle);
 			idx = haystack.indexOf(needle, idx + newNeedleLength);
 		}*/
 		replaceAll(haystack, needle, newNeedle, 0);
 	}
 
 	//interval minimum size of chunks after which replacement must take place
 	//default is 0, which means that all found characters must be replaced
 	public static void replaceAll(StringBuffer haystack, String needle, String newNeedle, int interval) {
 		if (needle == null || "".equals(needle)) {
 			throw new IllegalArgumentException("string to replace may not be empty");
 		}
 		int idx = haystack.indexOf(needle);
 		int nextIdx = -1;
 		int processedChunkSize = idx;
 		int needleLength = needle.length();
 		int newNeedleLength = newNeedle.length();
 		while (idx != -1/* && idx < haystack.length()*/) {
 			if (processedChunkSize >= interval) {
 				haystack.replace(idx, idx + needleLength, newNeedle);
 				nextIdx = haystack.indexOf(needle, idx + newNeedleLength);
 				processedChunkSize = nextIdx - idx;//length of replacement is not included
 				idx = nextIdx;
 			}
 			else {
 				nextIdx = haystack.indexOf(needle, idx + newNeedleLength);
 				processedChunkSize += nextIdx - idx;
 				idx = nextIdx;
 				if (newNeedleLength == 0) {
 					return;
 				}
 			}
 		}
 	}
 
 
 	public static int count(String haystack, String needle) {
 		if (needle == null || "".equals(needle)) {
 			throw new IllegalArgumentException("string to count not be empty");
 		}
 		int idx = haystack.indexOf(needle);
 		int count = 0;
 		while (idx != -1) {
 			idx = haystack.indexOf(needle, idx + 1);
 			count++;
 		}
 		return count;
 	}
 
 	/**
 	 */
 	public static String condenseWhitespace(String haystack) {
 		StringBuffer val = new StringBuffer(haystack);
 		condenseWhitespace(val);
 		return val.toString();
 	}
 
 	/**
 	 */
 	public static void condenseWhitespace(StringBuffer haystack) {
 		//TODO leave trailing whitespace
 		//TODO convert all whitespace to spaces
 		int size = haystack.length();
 //		int nrRemoved = 0;
 		boolean prevIsWhiteSpace = false;
 		for (int i = 0; i < size; i++) {
 			char c = haystack.charAt(i);
 			if (c == 10 || c == 13) {
 				c = ' ';
 				haystack.replace(i, i + 1, " ");
 			}
 			if (Character.isWhitespace(c)) {
 				if (prevIsWhiteSpace) {
 					haystack.delete(i, i + 1);
 					size--;
 					i--;
 //					nrRemoved++;
 				}
 				prevIsWhiteSpace = true;
 			}
 			else {
 				prevIsWhiteSpace = false;
 			}
 		}
 	}
 
 	/**
 	 * removes all occurances of needle in haystack
 	 *
 	 * @param haystack input string
 	 * @param needle   string to remove
 	 */
 	public static String removeAll(String haystack, String needle) {
 		return replaceAll(haystack, needle, "");
 	}
 
 
 	/**
 	 * removes all occurances of needle in haystack
 	 *
 	 * @param haystack input string
 	 * @param needle   string to remove
 	 */
 	public static void removeAll(StringBuffer haystack, String needle) {
 		replaceAll(haystack, needle, "");
 	}
 
 	/**
 	 * Finds a specific value in a resource bundle
 	 * The format of the bundle should be:
 	 * [RESOURCE=VALUE];*
 	 *
 	 * @param bundle resource string in the format described above
 	 * @param resource the resource (key value) to search for
 	 * @param caseSensitive indicatie if key values are case sensitive
 	 * @return the value of the resource
 	 */
 /*
 	public static String findResourceValue(String bundle, String resource, boolean caseSensitive)
 	{
 		String bun = bundle;
 		if (!caseSensitive)
 		{
 			bun = bun.toUpperCase();
 			resource = resource.toUpperCase();
 		}
 
 
 		StringTokenizer tokens = new StringTokenizer(bun, ";");
 		while (tokens.hasMoreTokens())
 		{
 			String tok = tokens.nextToken();
 			if (tok.indexOf(resource + "=") == 0)
 			{
 				return tok.substring(resource.length() + 1);
 			}
 		}
 		return null;
 	}
 */
 
 	/**
 	 * Finds a specific value in a resource bundle
 	 * using case insensitive search
 	 */
 /*
 	public static String findResourceValue(String bundle, String resource)
 	{
 		return resourceValue(bundle, resource, false);
 	}
 */
 
 	/**
 	 * Escapes quotes, double quotes, ecape characters and end-of-line characters in strings.
 	 *
 	 * @param in input string
 	 * @return escaped string
 	 */
 	public static String esc(String in) {
 		if (in == null) {
 			return null;
 		}
 		StringBuffer val = new StringBuffer(in);
 		esc(val);
 		return val.toString();
 	}
 
 	/**
 	 * Escapes quotes, double quotes, ecape characters and end-of-line characters in strings
 	 *
 	 * @param in input string
 	 * @return escaped string
 	 */
 	public static void esc(StringBuffer in) {
 		if (in == null) {
 			return;
 		}
 		replaceAll(in, "\\", "\\\\");
 		replaceAll(in, "'", "\\'");
 		replaceAll(in, "\"", "\\\"");
 		replaceAll(in, "\n", "\\n");
 		replaceAll(in, "\r", "\\r");
 	}
 
 
 	/**
 	 * tells whether all characters in a String are digits
 	 *
 	 * @param in String to evaluate
 	 */
 	public static boolean isNumeric(String in) {
 		char c = 0;
 		for (int i = in.length(); i > 0; i--) {
 			c = in.charAt(i - 1);
 			if (!Character.isDigit(c)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * tells whether all characters in a String are letters
 	 *
 	 * @param in String to evaluate
 	 */
 	public static boolean isAlpha(String in) {
 		char c = 0;
 		for (int i = in.length(); i > 0; i--) {
 			c = in.charAt(i - 1);
 			if (!Character.isLetter(c)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * tells whether all characters in a String are digits or letters
 	 *
 	 * @param in String to evaluate
 	 */
 	public static boolean isAlphaNumeric(String in) {
 		char c = 0;
 		for (int i = in.length(); i > 0; i--) {
 			c = in.charAt(i - 1);
 			if (!Character.isLetterOrDigit(c)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	/**
 	 * tells whether all characters in a String are letters or digits or part of a given String
 	 *
 	 * @param in	String to evaluate
 	 * @param chars characters which are allowed in the given String
 	 */
 	public static boolean isAlphaNumericOrContainsOnlyCharacters(String in, String chars) {
 		char c = 0;
 		for (int i = 0; i < in.length(); i++) {
 			c = in.charAt(i);
 			if (Character.isLetterOrDigit(c) == (chars.indexOf(c) != -1)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	/**
 	 * tells whether one or more characters in a String are part of a given String
 	 *
 	 * @param in	String to evaluate
 	 * @param chars characters which are to be tested in the given String
 	 */
 	public static boolean containsCharacters(String in, String chars) {
 		for (int i = 0; i < chars.length(); i++) {
 			if (in.indexOf(chars.charAt(i)) != -1) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 
 	/**
 	 * tells whether all characters in a String are part of a given String
 	 *
 	 * @param in	String to evaluate
 	 * @param chars characters which are to be tested in the given String
 	 */
 	public static boolean containsOnlyCharacters(String in, String chars) {
 		for (int i = 0; i < in.length(); i++) {
 			if (chars.indexOf(in.charAt(i)) == -1) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	/**
 	 * keep reading until the InputStream is exhausted
 	 *
 	 * @param input
 	 * @return a resulting String
 	 */
 	public static String absorbInputStream(InputStream input) throws IOException {
 		byte[] bytes = StreamSupport.absorbInputStream(input);
 		return new String(bytes);
 	}
 
 
 	/**
 	 * keep reading until the InputStream is exhausted
 	 *
 	 * @param input
 	 * @return a resulting String
 	 */
 	public static String absorbInputStream(InputStream input, String encoding) throws IOException {
 		byte[] bytes = StreamSupport.absorbInputStream(input);
 		return new String(bytes, encoding);
 	}
 
 	/**
 	 * Writes the contents of a string to an output stream.
 	 *
 	 * @param s
 	 * @param output
 	 * @throws IOException
 	 */
 	public static void writeToOutputStream(String s, OutputStream output) throws IOException {
 		writeToOutputStream(s, output, null);
 	}
 
 	/**
 	 * Writes the contents of a string to an output stream.
 	 *
 	 * @param s
 	 * @param output
 	 * @param encoding
 	 * @throws IOException
 	 */
 	public static void writeToOutputStream(String s, OutputStream output, String encoding) throws IOException {
 		BufferedReader reader = new BufferedReader(new StringReader(s));
 		PrintStream writer;
 		if (encoding != null) {
 			writer = new PrintStream(output, true, encoding);
 		}
 		else {
 			writer = new PrintStream(output, true);
 		}
 		String line;
 		while ((line = reader.readLine()) != null) {
 			writer.println(line);
 		}
 	}
 
 	/**
 	 * reads all words in a text, words in this case are a series of characters seperated by SPACE, ',', '.' or ':'
 	 *
 	 * @param input
 	 * @return a collection of uniquely identified words in order of appearance
 	 */
 	public static List<String> split(String input) {
 		return split(input, " ,.:;/\\", false);
 	}
 
 
 	/**
 	 * reads all words in a text, words in this case are a series of characters seperated by SPACE, ',', '.' or ':'
 	 *
 	 * @param input
 	 * @param punctuationChars
 	 * @return a collection of uniquely identified words
 	 */
 	public static List<String> split(String input, String punctuationChars) {
 		return split(input, punctuationChars, false);
 	}
 
 
 	/**
 	 * reads all words in a text and converts them to lower case
 	 *
 	 * @param input
 	 * @param punctuationChars characters that can not belong to words and are therefore separators
 	 * @param sort			 indicates if result must be sorted alphabetically
 	 * @return a collection of uniquely identified words
 	 */
 	public static List<String> split(String input, String punctuationChars, boolean sort) {
 		return split(input, punctuationChars, sort, false);
 	}
 
 	/**
 	 * reads all words in a text and converts them to lower case
 	 *
 	 * @param input
 	 * @param punctuationChars characters that can not belong to words and are therefore separators
 	 * @return a collection of uniquely identified words
 	 */
 	public static List<String> split(String input, String punctuationChars, String quoteChars) {
 		return split(input, punctuationChars, quoteChars, false, false, false);
 	}
 
 	/**
 	 * reads all words in a text
 	 *
 	 * @param input
 	 * @param punctuationChars   characters that can not belong to words and are therefore separators
 	 * @param sort			   whether to sort the result alphabetically
 	 * @param convertToLowerCase whether to convert all found words to lowercase
 	 * @return a collection of uniquely identified words
 	 */
 	public static List<String> split(String input, String punctuationChars, boolean sort, boolean convertToLowerCase) {
 		return split(input, punctuationChars, sort, convertToLowerCase, false);
 	}
 
 
 	/**
 	 * reads all words in a text
 	 *
 	 * @param input
 	 * @param punctuationChars   characters that can not belong to words and are therefore separators
 	 * @param sort			   whether to sort the result alphabetically. (If the result is sorted, setting the distinct flag to false has no effect)
 	 * @param convertToLowerCase whether to convert all found words to lowercase
 	 * @param distinct		   true if a certain word may occur only once in the resulting collection
 	 * @return a collection of extracted words
 	 */
 	public static List<String> split(String input, String punctuationChars, boolean sort, boolean convertToLowerCase, boolean distinct) {
 		return split(input, punctuationChars, "\"", sort, convertToLowerCase, distinct);
 	}
 
 	//TODO get rid of flags (move to spec)
 	//TODO distinguish delimiters and bracketdelimiters
 	//TODO get rid of this code altogether
 
 	
 	/*	
 	@Test
 	public void testSplit() {
 		List<CodePiece> result = SourceFile.split("hello this contains a=20l a long", new RegExTokenDef("[0-9]+l", NumericConstant.class), 13);
 		System.out.println(result);
 	}
 
 	
 	public static List<CodePiece> split(String val, RegExTokenDef tokenType, int lineNr) {
 //TODO @see FileSupport.countOccurrences
 		List<CodePiece> retval = new ArrayList<CodePiece>();
 		
 		Pattern p = Pattern.compile(tokenType.getKey());
 		Matcher matcher = p.matcher(val);
 		
 		int position = 0;
 
 		while (matcher.find()) {
 			String undefCode = val.substring(position, matcher.start());
 			if(undefCode.length() > 0) {
 				retval.add(new UndefinedCode(undefCode, lineNr));
 			}
 			position = matcher.end();
 			retval.add(CodePiece.createCodePiece(tokenType.codePieceType, val.substring(matcher.start(), position), lineNr));
 		}
 		String undefCode = val.substring(position);
 		if(undefCode.length() > 0) {
 			retval.add(new UndefinedCode(undefCode, lineNr));
 		}			
 		  
 		/*if (matcher.matches()) {
 //			retval.add(CodePiece.createCodePiece(tokenType.codePieceType, val.substring(matcher.start(), matcher.end()), lineNr));
 			retval.add(CodePiece.createCodePiece(tokenType.codePieceType, val.substring(matcher.start(), matcher.end()), lineNr));
 		}* /
 		return retval;
 	}
 */
 
 	/**
 	 * reads all words in a text
 	 *
 	 * @param input
 	 * @param punctuationChars   characters that can not belong to words and are therefore separators
 	 * @param quoteSymbols	   used as list of characters used to group strings
 	 * @param sort			   whether to sort the result alphabetically. (If the result is sorted, setting the distinct flag to false has no effect)
 	 * @param convertToLowerCase whether to convert all found words to lower case
 	 * @param distinct		   true if a certain word may occur only once in the resulting collection
 	 * @return a collection of extracted words
 	 */
 	public static List<String> split(String input, String punctuationChars, String quoteSymbols, boolean sort, boolean convertToLowerCase, boolean distinct) {
 		if (input == null) {
 			return new ArrayList<String>(0);
 		}
 		TreeMap<String, Object> storage = new TreeMap<String, Object>();
 		ArrayList<String> unsortedStorage = new ArrayList<String>();
 
 		StringBuffer word = new StringBuffer();
 		boolean readingWord = false;
 		boolean insideQuotes = false;
 		char insideQuote = '\0';
 		for (int i = 0; i < input.length(); i++) {
 			if (readingWord) {
 				if (punctuationChars.indexOf(input.charAt(i)) != -1 && !insideQuotes) {
 					//check forbidden word list first
 					//or maybe make arrangements in Index
 					//e.g.: disable Index with too many (%) references
 
 					String foundWord = word.toString()/*.trim()*/;
 					if (convertToLowerCase) {
 						foundWord = foundWord.toLowerCase();
 					}
 					if (!sort) {
 						if (!distinct || !storage.containsKey(foundWord)) {
 							unsortedStorage.add(foundWord);
 						}
 					}
 					storage.put(foundWord, new Object());
 					readingWord = false;
 				}
 				else {
 					if (quoteSymbols != null && quoteSymbols.indexOf(input.charAt(i)) != -1) {
 						insideQuote = input.charAt(i);
 						insideQuotes = !insideQuotes;
 					}
 					else {
 						word.append(input.charAt(i));
 					}
 				}
 			}
 			else {
 				if (punctuationChars.indexOf(input.charAt(i)) == -1) {
 					word = new StringBuffer();
 					if (quoteSymbols != null && quoteSymbols.indexOf(input.charAt(i)) != -1) {
 						insideQuote = input.charAt(i);
 						insideQuotes = !insideQuotes;
 					}
 					else {
 						word.append(input.charAt(i));
 					}
 					readingWord = true;
 				}
 			}
 		}
 		if (readingWord) {
 			String foundWord = word.toString()/*.trim()*/;
 			if (convertToLowerCase) {
 				foundWord = foundWord.toLowerCase();
 			}
 			if (!sort) {
 				if (!distinct || !storage.containsKey(foundWord)) {
 					unsortedStorage.add(foundWord);
 				}
 			}
 			storage.put(foundWord, new Object());
 		}
 		if (sort) {
 			return new ArrayList<String>(storage.keySet());
 		}
 		else {
 			return unsortedStorage;
 		}
 	}
 
 
 	/**
 	 * reads all strings in a text that are in between certain tags such as '[' and ']'
 	 *
 	 * @param input
 	 * @param startTag
 	 * @param endTag
 	 * @param sort whether to sort the result alphabetically
 	 * @return a collection of uniquely identified words
 	 */
 	public static Set extractStringsInbetweenTagsFromText(String input, char startTag, char endTag, boolean sort) {
 		
 		TreeMap<String, Object> storage = new TreeMap<String, Object>();
 		HashSet<String> unsortedStorage = new HashSet<String>();
 
 		StringBuffer word = new StringBuffer();
 		boolean readingWord = false;
 		LOOP:
 		for (int i = 0; i < input.length(); i++) {
 			if (readingWord) {
 				if (input.charAt(i) == endTag) {
 					//check forbidden word list first
 					//or maybe make arrangements in Index
 					//e.g.: disable Index with too many (%) references
 
 
 					String foundWord = word.toString();
 					if (!sort) {
 						if (!storage.containsKey(foundWord)) {
 							unsortedStorage.add(foundWord);
 						}
 					}
 
 
 					storage.put(foundWord, new Object());
 					readingWord = false;
 					continue LOOP;
 				}
 				else {
 					word.append(input.charAt(i));
 				}
 			}
 			else {
 				if (input.charAt(i) == startTag) {
 					word = new StringBuffer();
 					readingWord = true;
 				}
 				else {
 					continue LOOP;
 				}
 			}
 		}
 		if (readingWord) {
 			String foundWord = word.toString()/*.toLowerCase()*/;
 			if (!sort) {
 				if (!storage.containsKey(foundWord)) {
 					unsortedStorage.add(foundWord);
 				}
 			}
 			storage.put(foundWord, new Object());
 		}
 		if (sort) {
 			return storage.keySet();
 		}
 		else {
 			return unsortedStorage;
 		}
 	}
 
 	/**
 	 * Retrieves stack trace from throwable
 	 *
 	 * @param t
 	 * @return
 	 */
 	public static String getStackTrace(Throwable t) {
 		StringWriter sw = new StringWriter();
 		t.printStackTrace(new PrintWriter(sw));
 		return sw.toString();
 	}
 
 	/**
 	 * Retrieves stack trace from throwable.
 	 *
 	 * @param t
 	 * @param depth
 	 * @return
 	 */
 	public static String getStackTrace(Throwable t, int depth) {
 		return getStackTrace(t, depth, null);
 	}
 
 	/**
 	 * Retrieves stack trace from throwable.
 	 *
 	 * @param t
 	 * @param depth
 	 * @param prefix
 	 * @return
 	 */
 	public static String getStackTrace(Throwable t, int depth, String prefix) {
 		StringBuffer retval = new StringBuffer();
 		int nrWritten = 0;
 		retval.append(t.toString());
 		StackTraceElement[] elements = t.getStackTrace();
 		for (int i = 0; nrWritten < depth && i < elements.length; i++) {
 			String line = elements[i].toString();
 			if (prefix == null || line.startsWith(prefix)) {
 				retval.append("\n" + line);
 				nrWritten++;
 			}
 		}
 		return retval.toString();
 	}
 
 	/**
 	 * Trims strings that contain too many characters and adds trailing characters
 	 * to indicate that the original string has been trimmed.
 	 *
 	 * @param input
 	 * @param maxNrofChars maximum allowed length of input
 	 * @param end		  trailing characters when trimmed
 	 * @return original string or trimmed version
 	 */
 	public static String trim(String input, int maxNrofChars, String end) {
 		if (end == null) {
 			end = "";
 		}
 		int margin = end.length();
 		if (input != null && input.length() > maxNrofChars + margin) {
 			input = input.substring(0, maxNrofChars) + end;
 		}
 		return input;
 	}
 
 
 
 	/**
 	 * Creates a character array and initializes it with a default value.
 	 *
 	 * @param size
 	 * @param defaultVal
 	 * @return
 	 */
 	public static char[] createCharArray(int size, char defaultVal) {
 		char[] retval = new char[size];
 		if (size > 0) {
 			if (size < 500) {
 				for (int i = 0; i < size; i++) {
 					retval[i] = defaultVal;
 				}
 			}
 			else {
 				initializelLargeCharArray(retval, defaultVal);
 			}
 		}
 		return retval;
 	}
 
 	/**
 	 * @param retval
 	 * @param defaultVal
 	 * @return
 	 */
 	static char[] initializelLargeCharArray(char[] retval, char defaultVal) {
 		int size = retval.length;
 		retval[0] = defaultVal;
 		int i = 1;
 		int half = (size / 2) + 1;
 		while (i < half) {
 			System.arraycopy(retval, 0, retval, i, i);
 			i *= 2;
 		}
 		System.arraycopy(retval, 0, retval, i, size - i);
 		return retval;
 	}
 
 
 
 }
