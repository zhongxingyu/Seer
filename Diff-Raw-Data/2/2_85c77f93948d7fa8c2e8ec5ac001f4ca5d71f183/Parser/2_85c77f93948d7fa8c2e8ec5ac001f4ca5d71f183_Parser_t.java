 package org.parser;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.List;
 
 import org.parser.Operator.Assoc;
 import org.suite.Context;
 import org.suite.Singleton;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Str;
 import org.suite.node.Tree;
 import org.util.IoUtil;
 import org.util.LogUtil;
 import org.util.ParserUtil;
 import org.util.Util;
 
 public class Parser {
 
 	private Context localContext;
 
 	private Operator operators[];
 
 	public static final String closeGroupComment = "=-";
 	public static final String openGroupComment = "-=";
 	public static final String closeLineComment = "\n";
 	public static final String openLineComment = "--";
 
 	private static final List<Character> whitespaces = Arrays.asList('\t', '\r', '\n');
 
 	public Parser(Operator operators[]) {
 		this(Singleton.get().getGrandContext(), operators);
 	}
 
 	public Parser(Context context, Operator operators[]) {
 		localContext = context;
 		this.operators = operators;
 	}
 
 	public Node parseClassPathFile(String fn) throws IOException {
 		return parse(getClass().getClassLoader().getResourceAsStream(fn));
 	}
 
 	public Node parse(InputStream is) throws IOException {
 		return parse(IoUtil.readStream(is));
 	}
 
 	public Node parse(String s) {
 		s = removeComments(s);
 		s = convertIndents(s);
 		s = convertWhitespaces(s);
 		return parseWithoutComments(s);
 	}
 
 	/**
 	 * Parse without comments.
 	 */
 	private Node parseWithoutComments(String s) {
 		return parseWithoutComments(s, 0);
 	}
 
 	private Node parseWithoutComments(String s, int fromOp) {
 		s = s.trim();
 		return !s.isEmpty() ? parseRawString(s, fromOp) : Atom.NIL;
 	}
 
 	private Node parseRawString(String s, int fromOp) {
 		int end = s.length();
 		char first = s.charAt(0), last = s.charAt(end - 1);
 
 		for (int i = fromOp; i < operators.length; i++) {
 			Operator operator = operators[i];
 			String lr[] = ParserUtil.search(s, operator);
 
 			if (operator == TermOp.BRACES)
 				if (lr != null && last == '}')
 					lr[1] = Util.substr(lr[1], 0, -1);
 				else
 					continue;
 
 			if (lr != null) {
 				boolean isLeftAssoc = operator.getAssoc() == Assoc.LEFT;
 				int li = fromOp + (isLeftAssoc ? 0 : 1);
 				int ri = fromOp + (isLeftAssoc ? 1 : 0);
 				return Tree.create(operator, parseWithoutComments(lr[0], li), parseWithoutComments(lr[1], ri));
 			}
 		}
 
 		if (first == '(' && last == ')' || first == '[' && last == ']')
 			return parseWithoutComments(Util.substr(s, 1, -1));
 		if (first == '`' && last == '`')
 			return parseRawString(" " + Util.substr(s, 1, -1) + " ", 0);
 
 		try {
 			return Int.create(Integer.parseInt(s));
		} catch (Exception ignored) {
 		}
 
 		if (first == '"' && last == '"')
 			return new Str(unescape(Util.substr(s, 1, -1), "\""));
 
 		if (first == '\'' && last == '\'')
 			s = unescape(Util.substr(s, 1, -1), "'");
 
 		// Shows warning if the atom has mismatched quotes or brackets
 		int quote = 0, depth = 0;
 		for (char c : s.toCharArray()) {
 			quote = ParserUtil.getQuoteChange(quote, c);
 			if (quote == 0)
 				depth = ParserUtil.checkDepth(depth, c);
 		}
 
 		if (quote != 0 || depth != 0)
 			LogUtil.info("Suspicious input when parsing " + s);
 
 		return Atom.create(localContext, s);
 	}
 
 	/**
 	 * Turns indent patterns into parentheses, to provide Python-like parsing.
 	 */
 	private String convertIndents(String s) {
 		StringBuilder sb = new StringBuilder();
 		int lastIndent = 0;
 
 		s = "\n" + s + "\n";
 
 		while (!s.isEmpty()) {
 			String line;
 			String lr[];
 
 			lr = ParserUtil.search(s, "\n", Assoc.RIGHT, false);
 			if (lr != null) {
 				line = lr[0];
 				s = lr[1];
 			} else {
 				line = s;
 				s = "";
 			}
 
 			int length = line.length();
 			int indent = 0;
 			while (indent < length && line.charAt(indent) == '\t')
 				indent++;
 
 			line = line.substring(indent).trim();
 
 			// Converts :: notation, "if:: a" becomes "if (a)"
 			lr = ParserUtil.search(line, "::", Assoc.RIGHT);
 			line = lr != null ? lr[0] + " (" + lr[1] + ")" : line;
 			length = line.length();
 
 			if (length != 0) { // Ignore empty lines
 				int startPos = 0, endPos = length;
 
 				// Find operators at beginning and end of line
 				for (Operator operator : operators) {
 					String name = operator.getName().trim();
 
 					if (!name.isEmpty()) {
 						if (line.startsWith(name + " "))
 							startPos = Math.max(startPos, 1 + name.length());
 						if (line.equals(name))
 							startPos = Math.max(startPos, name.length());
 						if (line.endsWith(name))
 							endPos = Math.min(endPos, length - name.length());
 					}
 				}
 
 				if (startPos > endPos) // When a line has only one operator
 					startPos = 0;
 
 				// Insert parentheses by line indentation
 				String decoratedLine = "";
 				while (lastIndent > indent) {
 					decoratedLine += ") ";
 					lastIndent--;
 				}
 				decoratedLine += line.substring(0, startPos);
 				while (lastIndent < indent) {
 					decoratedLine += " (";
 					lastIndent++;
 				}
 				decoratedLine += line.substring(startPos, endPos);
 				decoratedLine += line.substring(endPos);
 
 				sb.append(decoratedLine + "\n");
 			}
 		}
 
 		return sb.toString();
 	}
 
 	private boolean isWhitespaces(String s) {
 		boolean result = true;
 		for (char c : s.toCharArray())
 			result &= whitespaces.contains(c);
 		return result;
 	}
 
 	private String convertWhitespaces(String s) {
 		for (char whitespace : whitespaces)
 			s = replace(s, "" + whitespace, " ");
 		return s;
 	}
 
 	private String replace(String s, String from, String to) {
 		while (true) {
 			int pos = ParserUtil.search(s, 0, from);
 
 			if (pos != -1)
 				s = s.substring(0, pos) + to + s.substring(pos + from.length());
 			else
 				return s;
 		}
 	}
 
 	private String removeComments(String s) {
 		s = removeComments(s, openGroupComment, closeGroupComment);
 		s = removeComments(s, openLineComment, closeLineComment);
 		return s;
 	}
 
 	private String removeComments(String s, String open, String close) {
 		int closeLength = !isWhitespaces(close) ? close.length() : 0;
 
 		while (true) {
 			int pos1 = ParserUtil.search(s, 0, open);
 			if (pos1 == -1)
 				return s;
 			int pos2 = ParserUtil.search(s, pos1 + open.length(), close);
 			if (pos2 == -1)
 				return s;
 			s = s.substring(0, pos1) + s.substring(pos2 + closeLength);
 		}
 	}
 
 	private static String unescape(String s, String quote) {
 		s = s.replace(quote + quote, quote);
 
 		try {
 			int pos = 0;
 			while ((pos = s.indexOf('%', pos)) != -1) {
 				int pos1 = pos + 1;
 
 				if (pos1 < s.length() && s.charAt(pos1) != '%') {
 					String hex = s.substring(pos1, pos + 3);
 					char c = (char) Integer.parseInt(hex, 16);
 					s = s.substring(0, pos) + c + s.substring(pos + 3);
 				} else
 					s = s.substring(0, pos) + s.substring(pos1);
 
 				pos++;
 			}
 		} catch (Exception ex) {
 			// StringIndexOutOfBoundsException, NumberFormatException
 			LogUtil.error(ex);
 		}
 
 		return s;
 	}
 
 }
