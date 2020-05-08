 /*
  * Java parser based on the BNF definition parser.
  * Copyright (C) 2013  Zuben El Acribi
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package bnf;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * This is a Java parser based on BNF definition parser which uses
  * Java BNF definitions provided in java.bnf. These definitions can
  * be found at http://docs.oracle.com/javase/specs/jls/se7/jls7.pdf.
  * 
  * @author Zuben El Acribi
  */
 public class JavaParser extends Parser {
 
 	/**
 	 * Constructs a Java Parser.
 	 * @throws ParserInitializationException if there is a syntax error
 	 *   in the BNF definition or an IOException occurs.
 	 */
 	public JavaParser() throws ParserInitializationException {
 		super();
 	}
 	
 	@Override
 	public void initialize() throws ParserInitializationException {
 		try {
 			BufferedReader inp = new BufferedReader(new FileReader("java.bnf"));
 			StringBuffer buff = new StringBuffer();
 			int colon = -1;
 			while (true) {
 				String line = inp.readLine();
 				if (line == null) {
 					break;
 				}
 				line = line.trim();
 				if (line.length() == 0) {
 					continue;
 				}
 				int newColon = line.indexOf(':');
 				boolean idBeforeColon = newColon >= 0;
 				for (int i = 0; i < newColon; i++) {
 					if (!Character.isJavaIdentifierPart(line.charAt(i))) {
 						idBeforeColon = false;
 						break;
 					}
 				}
 				if (idBeforeColon) {
 					if (buff.length() > 0) {
 						putDefinition(buff.toString().trim(), colon);
 						buff.delete(0, buff.length());
 					}
 					colon = newColon;
 				}
 				buff.append(line);
 				buff.append('\n');
 			}
 			if (buff.length() > 0) {
 				putDefinition(buff.toString(), colon);
 			}
 			inp.close();
 		} catch (IOException ex) {
 			throw new ParserInitializationException(ex);
 		} catch (ParseException ex) {
 			throw new ParserInitializationException(ex);
 		}
 
 		// Enter fictive definitions for StringLiteral, IntegerLiteral, CharacterLiteral, FloatingPointLiteral
 		// in order an exception not to be thrown by checkForMissingDefinitions().
 		// These definitions will be processed separately by the JavParser.
 		definitions.put("StringLiteral", null);
 		definitions.put("IntegerLiteral", null);
 		definitions.put("CharacterLiteral", null);
 		definitions.put("FloatingPointLiteral", null);
 	}
 	
 	public ParseTree parse(String path) throws ParseException {
 		return parse(new File(path));
 	}
 	
 	public ParseTree parse(File f) throws ParseException {
 		return parse("CompilationUnit", f);
 	}
 
 	private void putDefinition(String s, int colon) throws ParseException {
 		String def = s.substring(0, colon);
 		Tree t = BnfDefParser.parse(s.substring(colon + 1).trim());
 		t.parent = new Tree(def, 0, def.length(), null);
 		definitions.put(def, t);
 	}
 
 	@Override
 	protected Tree extension(Tree t, String s, int begin, int end) throws ParseException {
		// Clear whitespace.
		while (begin < end && Character.isWhitespace(s.charAt(begin))) {
			begin++;
		}
 		if (begin >= end) {
 			return null;
 		}
 		if (t.node.equals("StringLiteral")) {
 			return string(t, s, begin, end);
 		} else if (t.node.equals("IntegerLiteral")) {
 			return number(t, s, begin, end);
 		} else if (t.node.equals("CharacterLiteral")) {
 			return string(t, s, begin, end);
 		} else if (t.node.equals("FloatingPointLiteral")) {
 			return number(t, s, begin, end);
 		} else {
 			return null; // Not an extension of this parser.
 		}
 	}
 
 	private Tree string(Tree t, String s, int begin, int end) throws ParseException {
 		char quote = s.charAt(begin);
 		if (t.node.equals("CharacterLiteral") && quote != '\'') {
 			return null;
 		}
 		if (t.node.equals("StringLiteral") && quote != '"') {
 			return null;
 		}
 		for (int i = begin + 1; i < end; i++) {
 			char ch = s.charAt(i);
 			if (ch == '\\') {
 				i++;
 			} else if (ch == '\n') {
 				throw new ParseException("String constant exceeds line at " + pos(i));
 			} else if (ch == quote) {
 				return new Tree(s, begin, i + 1, t);
 			}
 		}
 		return null;
 	}
 
 	private Tree number(Tree t, String s, int begin, int end) throws ParseException {
 		boolean digits = false;
 		int pos = begin;
 		if (pos < end && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) {
 			pos++;
 		}
 		boolean period = false;
 		if (t.node.equals("FloatingPointLiteral") && pos < end && s.charAt(pos) == '.') {
 			pos++;
 			period = true;
 		}
 		boolean hex = false;
 		if (t.node.equals("IntegerLiteral") && pos < end - 1 && s.substring(pos, pos + 2).toLowerCase().equals("0x")) {
 			hex = true;
 			pos += 2;
 		}
 		while (pos < end && (Character.isDigit(s.charAt(pos)) || (hex && ((s.charAt(pos) >= 'a' && s.charAt(pos) <= 'f') || (s.charAt(pos) >= 'A' && s.charAt(pos) <= 'F'))))) {
 			pos++;
 			digits = true;
 		}
 		if (!period && digits && t.node.equals("FloatingPointLiteral") && pos < end && s.charAt(pos) == '.') {
 			pos++;
 			while (pos < end && Character.isDigit(s.charAt(pos))) {
 				pos++;
 			}
 		}
 		if (digits && t.node.equals("IntegerLiteral") && pos < end && Character.toLowerCase(s.charAt(pos)) == 'l') {
 			pos++;
 		}
 		if (digits && t.node.equals("FloatingPointLiteral") && pos < end && Character.toLowerCase(s.charAt(pos)) == 'e') {
 			if (pos >= end) {
 				return null;
 			}
 			pos++;
 			if (s.charAt(pos) == '+' || s.charAt(pos) == '-') {
 				pos++;
 			}
 			if (pos >= end) {
 				return null;
 			}
 			digits = false;
 			while (pos < end && Character.isDigit(s.charAt(pos))) {
 				pos++;
 				digits = true;
 			}
 		}
 		if (digits && t.node.equals("FloatingPointLiteral") && pos < end && (Character.toLowerCase(s.charAt(pos)) == 'f' || Character.toLowerCase(s.charAt(pos)) == 'd')) {
 			pos++;
 		}
 		if (digits) {
 			if (pos < end && Character.isLetter(s.charAt(pos))) {
 				return null;
 			}
 			return new Tree(s, begin, pos, t);
 		} else {
 			return null;
 		}
 	}
 	
 	Set<String> keywords;
 
 	@Override
 	protected boolean keyword(String s) {
 		if (keywords == null) {
 			keywords = new HashSet<String>();
 			for (String def : definitions.keySet()) {
 				browseTreeForKeywords(definitions.get(def));
 			}
 		}
 		return keywords.contains(s);
 	}
 
 	private void browseTreeForKeywords(Tree t) {
 		if (t == null) {
 			return;
 		}
 		if (t.type == NodeType.token) {
 			if (Character.isLetter(t.node.charAt(1))) {
 				keywords.add(t.node.substring(1, t.node.length() - 1));
 			}
 		}
 		for (Tree b : t.branches) {
 			browseTreeForKeywords(b);
 		}
 	}
 
 	@Override
 	protected int skipWhiteSpace(String s, int begin, int end) throws ParseException {
 		int b = begin;
 		while (true) {
 			b = super.skipWhiteSpace(s, b, end);
 			if (b < end - 2 && s.startsWith("/*", b)) {
 				int c = s.indexOf("*/", b + 1);
 				if (c < 0) {
 					throw new RuntimeException("Comment not closed in " + pos(b));
 				}
 				b = c + 2;
 			} else if (b < end - 2 && s.startsWith("//", b)) {
 				int c = s.indexOf("\n", b + 2);
 				if (c < 0) {
 					c = s.length();
 				}
 				b = c + 1;
 			}
 			if (b == begin) {
 				return begin;
 			}
 			begin = b;
 		}
 	}
 
 }
