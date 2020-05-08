 /**
  * 
  */
 package compiler.test;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import compiler.Context;
 import compiler.Symbol;
 import compiler.SymbolType;
 import compiler.Utils;
 import compiler.automata.Characters;
 import compiler.automata.Edge;
 import compiler.automata.FiniteAutomaton;
 import compiler.automata.State;
 import compiler.lex.CharLexerGenerator;
 import compiler.lex.Lexer;
 import compiler.lex.LexerAction;
 import compiler.lex.LineNumberAndPositionBufferedReader;
 import compiler.lex.Regex;
 import compiler.lex.RegexLexerGenerator;
 
 /**
  * @author Michael
  * 
  */
 public class LexTests {
 	public static void charLexerGeneratorTest() {
 		Context c = new Context();
 		SymbolType a = c.getTerminalSymbolType("X"), b = c
 				.getTerminalSymbolType("Y"), any = c
 				.getTerminalSymbolType("ANY_CHAR");
 
 		LinkedHashSet<LexerAction> actions = new LinkedHashSet<LexerAction>();
 		actions.add(LexerAction.lexToken("a", a));
 		actions.add(LexerAction.lexToken("b", b));
 		actions.add(LexerAction.enter(Utils.set(Lexer.DEFAULT_STATE, "inside"),
 				"(", null, "inside"));
 		actions.add(LexerAction.leave(Utils.set("inside"), ")", null));
 		actions.add(LexerAction.lexToken(
 				Utils.set(Lexer.DEFAULT_STATE, "inside"), "", any));
 
 		Lexer lexer = new CharLexerGenerator().generate(c, actions).lexer();
 
 		String text = "abbaXXXbabaX";
 		Reader reader = new StringReader(text);
 		Iterator<Symbol> it = lexer.lex(reader);
 		int i = 0;
 		while (it.hasNext()) {
 			Symbol s = it.next();
 			if (s.type() == c.eofType())
 				Utils.check(i == text.length());
 			else if (s.type() == a)
 				Utils.check(text.charAt(i) == 'a');
 			else if (s.type() == b)
 				Utils.check(text.charAt(i) == 'b');
 			else
 				Utils.check(s.type() == any);
 			i++;
 		}
 		lexerLineNumberAndPositionTest(lexer, text, c.eofType());
 
 		text = "abX(abab)b(())bbbXa(X)";
 		reader = new StringReader(text);
 		List<Symbol> tokens = Utils.toList(lexer.lex(reader));
 		Utils.check(tokens.size() == text.replaceAll("\\(", "")
 				.replaceAll("\\)", "").length() + 1);
 		SymbolType[] types = { a, b, any, any, any, any, any, b, b, b, b, any,
 				a, any, c.eofType() };
 		for (i = 0; i < types.length; i++)
 			Utils.check(tokens.get(i).type().equals(types[i]), i + ": "
 					+ tokens.get(i).type() + " != " + types[i]);
 		lexerLineNumberAndPositionTest(lexer, text, c.eofType());
 	}
 
 	private static void lexerLineNumberAndPositionTest(Lexer lexer,
 			String text, SymbolType eofType) {
 		String[] lines = Utils.split(text, "\n");
 		// add the \n's back
 		for (int i = 0; i < lines.length; i++) {
 			if (i < lines.length - 1
 					|| (!text.isEmpty() && text.charAt(text.length() - 1) == '\n')) {
 				lines[i] += '\n';
 			}
 		}
 
 		List<Symbol> tokens = Utils.toList(lexer.lex(new StringReader(text)));
 		for (Symbol token : tokens) {
 			if (!token.type().equals(eofType)) {
 				int absoluteStartPosition = getAbsolutePosition(lines,
 						token.line(), token.position()), absoluteEndPosition = getAbsolutePosition(
 						lines, token.endLine(), token.endPosition());
 				String tokenText = text.substring(absoluteStartPosition,
 						absoluteEndPosition + 1);
 				Utils.check(token.text().equals(tokenText));
 			}
 		}
 	}
 
 	private static int getAbsolutePosition(String[] lines, int line,
 			int position) {
 		int absolutePosition = 0;
 		for (int i = 1; i < line; i++) {
 			absolutePosition += lines[i - 1].length();
 		}
 		absolutePosition += position - 1;
 
 		return absolutePosition;
 	}
 
 	public static void basicRegexTest() {
 		Symbol parseTree = Regex.canonicalize(Regex.parse("a").parseTree());
 		Utils.check(parseTree.children().get(0).children().get(0).type()
 				.equals(Regex.CHAR));
 
 		String regex = "[\\-\\+]?[0-9]*\\.?[0-9]+([eE][\\-\\+]?[0-9]+)?";
 		parseTree = Regex.canonicalize(Regex.parse(regex).parseTree());
 		Utils.check(parseTree.type().equals(Regex.REGEX_LIST));
 		Utils.check(parseTree.children().size() == 5);
 		Utils.check(parseTree.children().get(0).children().get(0).children()
 				.get(1).type().equals(Regex.SET_LIST));
 
 		regex = "\\\\.";
 		parseTree = Regex.canonicalize(Regex.parse(regex).parseTree());
 		Utils.check(parseTree.type().equals(Regex.REGEX_LIST));
 		Utils.check(parseTree.children().get(0).children().get(0).type()
 				.equals(Regex.ESCAPED));
 		Utils.check(parseTree.children().get(1).children().get(0).type()
 				.equals(Regex.WILDCARD));
 	}
 
 	public static void regexNfaTest() {
 		FiniteAutomaton<SymbolType, Character> auto;
 
 		auto = createSimpleNfa("a");
 		checkNfa(auto, 3, 2, 1);
 
 		auto = createSimpleNfa("");
 		checkNfa(auto, 3, 2, 2);
 
 		auto = createSimpleNfa("a|b");
 		checkNfa(auto, 6, 6, 4);
 
 		auto = createSimpleNfa("ab");
 		checkNfa(auto, 4, 3, 1);
 
 		auto = createSimpleNfa("a*");
 		checkNfa(auto, 4, 4, 3);
 
 		auto = createSimpleNfa("a+");
 		checkNfa(auto, 5, 5, 3);
 
 		auto = createSimpleNfa("a?");
 		checkNfa(auto, 6, 6, 5);
 
 		auto = createSimpleNfa("[abc]");
 		checkNfa(auto, 4, 5, 2);
 
 		auto = createSimpleNfa("[a-z]");
 		checkNfa(auto, 4, 3, 2);
 
 		auto = createSimpleNfa("[^a]");
 		checkNfa(auto, 4, 4, 2);
 		
 		auto = createSimpleNfa("[^a\\?]");
 		checkNfa(auto, 4, 5, 2);
 
 		// should create an nfa with an unreachable state
 		auto = createSimpleNfa("[]");
 		checkNfa(auto, 4, 2, 2);
 
 		auto = createSimpleNfa("\\n");
 		checkNfa(auto, 3, 2, 1);
 	}
 
 	public static void regexEscapeTest() {
 		String[] inputs = new String[] { "abc", "a*b", "*?[]ab()\\n\n", "^^" }, outputs = new String[] {
 				"abc", "a\\*b", "\\*\\?\\[\\]ab\\(\\)\\\\n\n", "\\^\\^" };
 		for (int i = 0; i < inputs.length; i++) {
 			String escaped = Regex.escape(inputs[i]);
 			Utils.check(outputs[i].equals(escaped), escaped + " != "
 					+ outputs[i]);
 		}
 	}
 
 	private static FiniteAutomaton<SymbolType, Character> createSimpleNfa(
 			String regex) {
 		Symbol parseTree = Regex.canonicalize(Regex.parse(regex).parseTree());
 		FiniteAutomaton.Builder<SymbolType, Character> builder = FiniteAutomaton
 				.<SymbolType, Character> builder(Characters.setOperations());
 
 		Regex.buildNfaFor(builder,
 				new Context().getTerminalSymbolType("TOKEN"), parseTree);
 		return builder.toFiniteAutomaton();
 	}
 
 	private static void checkNfa(
 			FiniteAutomaton<SymbolType, Character> automaton,
 			int expectedStateCount, int expectedEdgeCount,
 			int expectedEpsilonEdgeCount) {
 		Set<State<SymbolType>> stateCollection = automaton.states();
 		Set<Edge<SymbolType, Character>> edgeCollection = automaton.edges();
 
 		Utils.check(stateCollection.size() == expectedStateCount,
 				String.format("Bad state count! (expected: %s, found %s)", expectedStateCount, stateCollection.size()));
 		Utils.check(edgeCollection.size() == expectedEdgeCount,
 				String.format("Bad edge count! (expected: %s, found %s)", expectedEdgeCount, edgeCollection.size()));
 
 		int epsilonEdgeCount = 0;
 		for (Edge<SymbolType, Character> edge : edgeCollection) {
 			if (edge.transitionOnSet() == null) {
 				epsilonEdgeCount++;
 			}
 		}
 		Utils.check(epsilonEdgeCount == expectedEpsilonEdgeCount,
 				"Bad epsilon edge count!");
 
 		int acceptCount = 0;
 		for (State<SymbolType> state : stateCollection) {
 			if (state.value() != null) {
 				acceptCount++;
 			}
 		}
 		Utils.check(acceptCount == 1, "Bad accept count!");
 	}
 
 	public static void regexLexerGeneratorTest() {
 		Context c = new Context();
 		SymbolType iff = c.getTerminalSymbolType("IF"), id = c
 				.getTerminalSymbolType("ID"), num = c
 				.getTerminalSymbolType("INT"), real = c
 				.getTerminalSymbolType("REAL"), mangled = c.getTerminalSymbolType("MANGLED"),
 				commentText = c
 				.getTerminalSymbolType("COMMENT"), constant = c
 				.getTerminalSymbolType("CONSTANT"), ur = c.unrecognizedType(), eof = c
 				.eofType();
 		String commentState = "COMMENT_STATE";
 
 		LinkedHashSet<LexerAction> actions = new LinkedHashSet<LexerAction>();
 
 		// basic symbols
 		actions.add(LexerAction.lexToken("if", iff));
 		actions.add(LexerAction.lexToken("[a-z][a-z0-9]*", id));
 		actions.add(LexerAction.lexToken("[0-9]+", num));
 		actions.add(LexerAction.lexToken("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)",
 				real));
 		actions.add(LexerAction.lexToken("[A-Z][^a-z]*[A-Z]", constant));
 		actions.add(LexerAction.lexToken("__[^\\\\\"]__", mangled));
 
 		// skip whitespace
 		actions.add(LexerAction.skip(LexerAction.DEFAULT_SET, "[ \r\n\t]"));
 
 		// nested comment support
 		actions.add(LexerAction.enter(
 				Utils.set(Lexer.DEFAULT_STATE, commentState), "/\\*", null,
 				commentState));
 		actions.add(LexerAction.leave(Collections.singleton(commentState),
 				"\\*/", null));
 		// pick up comment text (possibly in chunks) without mistakenly looking
 		// past "*/"
 		actions.add(LexerAction.lexToken(Collections.singleton(commentState),
 				"([a-zA-Z0-9\n ]+)|.", commentText));
 
 		Lexer lexer = new RegexLexerGenerator().generate(c, actions).lexer();
 
 		checkLexer(lexer, "", new SymbolType[] { eof });
 		checkLexer(lexer, "iif123 iff 123",
 				new SymbolType[] { id, id, num, eof });
 		checkLexer(lexer, "if ia0 1 a1.5", new SymbolType[] { iff, id, num, id,
 				real, eof });
 		checkLexer(lexer, "if/*aa<bb*/aa<bb", new SymbolType[] { iff,
 				commentText, commentText, commentText, id, ur, id, eof });
 		checkLexer(lexer, "if 2000.2000a0\nif 5 10", new SymbolType[] { iff,
 				real, id, iff, num, num, eof });
 		checkLexer(lexer, "1/*2/*//*/**/*/**/if*/if", new SymbolType[] { num,
 				commentText, commentText, commentText, commentText, iff, eof });
 		checkLexer(lexer, "AA", constant, eof);
 		checkLexer(lexer, "AAaAA", constant, id, constant, eof);
 		checkLexer(lexer, "ZZ!@#$H", constant, eof);
 		checkLexer(lexer, "ZZ/*THIS IS NOT ACTUALLY A COMMENT*/HH", constant, eof);
 		checkLexer(lexer, "MMMM/*bUT THIS IS A COMMENT SINCE IT STARTED WITH A LOWER CASE LETTER*/H!H", constant, commentText, constant, eof);
 		checkLexer(lexer, "__a__", mangled, eof);
 		checkLexer(lexer, "__\\__", ur, ur, ur, ur, ur, eof);
 		// MA 4/30/13: these 2 tests make sure that skip works properly when a skipped match is at the end of the string
 		checkLexer(lexer, " \t", eof);
 		checkLexer(lexer, "\r2\n", num, eof);
 		checkLexer(lexer, "/**/", eof);
 		checkLexer(lexer, "/**/if/**/", iff, eof);
 	}
 
 	private static void checkLexer(Lexer lexer, String input,
 			SymbolType... outputTypes) {
 		// simple test
 		lexerLineNumberAndPositionTest(lexer, input,
 				outputTypes[outputTypes.length - 1]);
 
 		List<Symbol> output = Utils.toList(lexer.lex(new StringReader(input)));
 
 		// check types
 		Utils.check(output.size() == outputTypes.length, "Bad output length!");
 		for (int i = 0; i < output.size(); i++) {
 			Utils.check(output.get(i).type().equals(outputTypes[i]),
 					"Bad output type at " + i);
 		}
 
 		// check texts
 		for (Symbol s : output) {
 			Utils.check(input.contains(s.text()));
 		}
 	}
 
 	public static void readerTest() throws IOException {
 		LineNumberAndPositionBufferedReader r = new LineNumberAndPositionBufferedReader(
 				new StringReader("a\n\nbcd"));
 		List<Object> result = new ArrayList<Object>();
 		result.add(r.position()); // 0
 		result.add(r.lineNumber()); // 0
 		result.add((char) r.uncheckedRead()); // a
 		r.mark();
 		result.add((char) r.uncheckedRead()); // \n
 		result.add(r.lineNumber()); // 1
 		result.add(r.position()); // 2
 		result.add((char) r.uncheckedRead()); // \n
 		result.add((char) r.uncheckedRead()); // b
 		r.reset();
 		result.add(r.lineNumber()); // 1
 		result.add(r.position()); // 1
 		char[] buf = new char[4];
 		r.read(buf);
 		result.add(Arrays.hashCode(buf));
 		result.add(r.offsetFromMark()); // 4
 		r.mark();
 		result.add(r.offsetFromMark()); // 0
 		r.mark();
 		r.mark();
 		r.reset();
 		result.add((char) r.uncheckedRead()); // d
 		result.add(r.uncheckedRead()); // -1
 		r.reset();
 		result.add(r.lineNumber()); // 3
 		result.add(r.position()); // 2
 		result.add((char) r.uncheckedRead()); // d
 		result.add(r.position()); // 3
 		result.add(r.uncheckedRead()); // -1
 		result.add(r.position()); // 3
 		result.add(r.lineNumber()); // 3
 		result.add(r.offsetFromMark()); // 1
 		result.add(r.uncheckedRead()); // -1
 
 		Object[] expected = new Object[] { 0, 0, 'a', '\n', 1, 2, '\n', 'b', 1,
 				1, Arrays.hashCode(new char[] { '\n', '\n', 'b', 'c' }), 4, 0,
 				'd', -1, 3, 2, 'd', 3, -1, 3, 3, 1, -1 };
 		Utils.check(Arrays.asList(expected).equals(result));
		r.close();
 	}
 
 	public static void main(String[] args) {
 		try {
 			readerTest();
 		} catch (IOException ex) {
 			Utils.err(ex);
 		}
 
 		charLexerGeneratorTest();
 
 		basicRegexTest();
 
 		regexNfaTest();
 
 		regexEscapeTest();
 
 		regexLexerGeneratorTest();
 
 		System.out.println("All lex tests passed!");
 	}
 }
