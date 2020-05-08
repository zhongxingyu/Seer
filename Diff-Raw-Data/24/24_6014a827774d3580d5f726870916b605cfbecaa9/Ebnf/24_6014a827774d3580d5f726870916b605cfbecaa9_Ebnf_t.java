 package suite.parser;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import suite.node.io.Operator.Assoc;
 import suite.util.FunUtil;
 import suite.util.FunUtil.Fun;
 import suite.util.FunUtil.Source;
 import suite.util.LogUtil;
 import suite.util.Pair;
 import suite.util.ParseUtil;
 import suite.util.To;
 import suite.util.Util;
 
 /**
  * Parser for Backus-Naur form grammars.
  * 
  * @author ywsing
  */
 public class Ebnf {
 
 	private String rootGrammarName;
 	private Map<String, Grammar> grammars = new HashMap<>();
 
 	private static final boolean trace = false;
 
 	private interface Grammar {
 	}
 
 	private abstract class WrappingGrammar implements Grammar {
 		private Grammar grammar;
 
 		private WrappingGrammar(Grammar grammar) {
 			this.grammar = grammar;
 		}
 	}
 
 	private class OrGrammar implements Grammar {
 		private List<Grammar> grammars;
 
 		public OrGrammar(List<Grammar> grammars) {
 			this.grammars = grammars;
 		}
 	}
 
 	private class JoinGrammar implements Grammar {
 		private List<Grammar> grammars;
 
 		public JoinGrammar(List<Grammar> grammars) {
 			this.grammars = grammars;
 		}
 	}
 
 	private class OptionalGrammar extends WrappingGrammar {
 		private OptionalGrammar(Grammar grammar) {
 			super(grammar);
 		}
 	}
 
 	private class RepeatGrammar extends WrappingGrammar {
 		private RepeatGrammar(Grammar grammar) {
 			super(grammar);
 		}
 	}
 
 	private class NamedGrammar implements Grammar {
 		private String name;
 
 		private NamedGrammar(String name) {
 			this.name = name;
 		}
 	}
 
 	private class TokenGrammar implements Grammar {
 		private String token;
 
 		private TokenGrammar(String token) {
 			this.token = token;
 		}
 	}
 
 	public class Node {
 		private String name;
 		private int start, end;
 		private List<Node> nodes = new ArrayList<>();
 
 		public Node(String name, int start) {
 			this(name, start, 0);
 		}
 
 		public Node(String name, int start, int end) {
 			this.name = name;
 			this.start = start;
 			this.end = end;
 		}
 
 		public String toString() {
 			return name + "@" + start + "-" + end + nodes;
 		}
 
 		public String toPrettyPrint() {
 			StringBuilder sb = new StringBuilder();
 			prettyPrint("", sb);
 			return sb.toString();
 		}
 
 		private void prettyPrint(String indent, StringBuilder sb) {
 			String indent1 = indent + "  ";
 			sb.append(indent + name + "@" + start + "-" + end + "\n");
 			for (Node node : nodes)
 				node.prettyPrint(indent1, sb);
 		}
 
 		public String getEntity() {
 			return name;
 		}
 
 		public int getEnd() {
 			return start;
 		}
 	}
 
 	public Ebnf(Reader reader) throws IOException {
 		BufferedReader br = new BufferedReader(reader);
 		String line;
 		Pair<String, String> lr;
 
 		while ((line = br.readLine()) != null)
 			if (!line.isEmpty() && line.charAt(0) != '#' && (lr = Util.split2(line, " ::= ")) != null) {
 				grammars.put(lr.t0, parseGrammar(lr.t1));
 
 				if (rootGrammarName == null)
 					rootGrammarName = lr.t0;
 			}
 	}
 
 	private Grammar parseGrammar(String s) {
 		Grammar grammar;
 		List<String> list;
 		s = s.trim();
 
 		if ((list = ParseUtil.searchn(s, " | ", Assoc.RIGHT)).size() > 1)
 			grammar = new OrGrammar(parseGrammars(list));
 		else if ((list = ParseUtil.searchn(s, " ", Assoc.RIGHT)).size() > 1)
 			grammar = new JoinGrammar(parseGrammars(list));
 		else if (s.endsWith("*"))
 			grammar = new RepeatGrammar(parseGrammar(Util.substr(s, 0, -1)));
 		else if (s.endsWith("?"))
 			grammar = new OptionalGrammar(parseGrammar(Util.substr(s, 0, -1)));
 		else if (s.startsWith("(") && s.endsWith(")"))
 			grammar = parseGrammar(Util.substr(s, 1, -1));
 		else if (s.startsWith("\"") && s.endsWith("\""))
 			grammar = new TokenGrammar(Util.substr(s, 1, -1));
 		else
 			grammar = new NamedGrammar(s);
 
 		return grammar;
 	}
 
 	private List<Grammar> parseGrammars(List<String> list) {
 		return To.list(FunUtil.iter(FunUtil.map(new Fun<String, Grammar>() {
 			public Grammar apply(String s) {
 				return parseGrammar(s);
 			}
 		}, FunUtil.asSource(list))));
 	}
 
 	public Node parse(String s) {
 		return parse(s, 0, rootGrammarName);
 	}
 
 	public Node parse(String s, int end, String name) {
 		return new Parse(s).parse(end, parseGrammar(name));
 	}
 
 	private class Parse {
 		private String in;
 		private int length;
 		private int errorPosition = 0;
 		private String errorName;
 
 		private class State {
 			private State previous;
 			private int pos;
 
 			private String name;
 			private int depth;
 
 			private State(State previous, int pos) {
				this(previous, pos, previous.name, previous.depth);
 			}
 
 			private State(State previous, int pos, String name, int depth) {
 				this.previous = previous;
 				this.pos = pos;
 				this.name = name;
 				this.depth = depth;
 			}
 		}
 
 		private final Source<State> noResult = FunUtil.nullSource();
 
 		private Parse(String in) {
 			this.in = in;
 			length = in.length();
 		}
 
 		private Node parse(int pos, Grammar grammar) {
 			State initialState = new State(null, pos, null, 1);
 			Source<State> source = parse(initialState, grammar);
 			State state;
 
 			while ((state = source.source()) != null)
 				if (state.pos == length) {
 					List<State> states = new LinkedList<>();
 
 					while (state != null) {
 						states.add(0, state);
 						state = state.previous;
 					}
 
 					Node root = new Node(null, 0);
 
 					Deque<Node> stack = new ArrayDeque<>();
 					stack.push(root);
 
 					for (State state_ : states) {
 						while (state_.depth < stack.size())
 							stack.pop().end = state_.pos;
 
 						Node node = new Node(state_.name, state_.pos);
 
 						if (state_.name != null)
 							stack.peek().nodes.add(node);
 
 						while (state_.depth > stack.size())
 							stack.push(node);
 					}
 
 					return root.nodes.get(0);
 				}
 
 			throw new RuntimeException("Syntax error for entity " + errorName + " at " + findPosition(errorPosition));
 		}
 
 		private Source<State> parse(State state, Grammar grammar) {
 			int pos = expectWhitespaces(state.pos);
 
 			if (trace)
 				LogUtil.info("parse(" + grammar + "): " + in.substring(pos));
 
 			State state1 = new State(state, pos);
 			Source<State> states;
 
 			if (grammar instanceof NamedGrammar) {
 				NamedGrammar namedGrammar = (NamedGrammar) grammar;
 				final String name = namedGrammar.name;
 				final int depth = state1.depth;
 				state1 = deepen(state1, name);
 
 				if (name.equals("<EOF>"))
 					states = state1.pos == length ? FunUtil.asSource(state1) : noResult;
 				else if (name.equals("<CHARACTER_LITERAL>"))
 					states = parseExpect(state1, expectCharLiteral(pos));
 				else if (name.equals("<FLOATING_POINT_LITERAL>"))
 					states = parseExpect(state1, expectFloatLiteral(pos));
 				else if (name.equals("<IDENTIFIER>"))
 					states = parseExpect(state1, expectIdentifier(pos));
 				else if (name.equals("<INTEGER_LITERAL>"))
 					states = parseExpect(state1, expectIntegerLiteral(pos));
 				else if (name.equals("<STRING_LITERAL>"))
 					states = parseExpect(state1, expectStringLiteral(pos));
 				else
 					states = parse(state1, grammars.get(name));
 
 				states = FunUtil.map(new Fun<State, State>() {
 					public State apply(State state) {
 						return undeepen(state, depth);
 					}
 				}, states);
 			} else if (grammar instanceof OrGrammar)
 				states = parseOr(state1, (OrGrammar) grammar);
 			else if (grammar instanceof JoinGrammar)
 				states = parseJoin(state1, (JoinGrammar) grammar);
 			else if (grammar instanceof OptionalGrammar)
 				states = FunUtil.cons(state1, parse(state1, child(grammar)));
 			else if (grammar instanceof RepeatGrammar)
 				states = parseRepeat(state1, child(grammar));
 			else if (grammar instanceof TokenGrammar)
 				states = parseExpect(state1, expectString(pos, ((TokenGrammar) grammar).token));
 			else
 				states = noResult;
 
			if (states == noResult && state1.name != null && pos >= errorPosition) {
 				errorPosition = pos;
				errorName = state1.name;
 			}
 
 			return states;
 		}
 
 		private Source<State> parseRepeat(final State state, final Grammar grammar) {
 			return new Source<State>() {
 				private State state_ = state;
 				private Deque<Source<State>> sources = new ArrayDeque<>();
 
 				public State source() {
 					State state0 = state_;
 					if (state0 != null) {
 						sources.push(parse(state0, grammar));
 
 						while (!sources.isEmpty() && (state_ = sources.peek().source()) == null)
 							sources.pop();
 					}
 					return state0;
 				}
 			};
 		}
 
 		private Source<State> parseOr(final State state, final OrGrammar grammar) {
 			return FunUtil.concat(FunUtil.map(new Fun<Grammar, Source<State>>() {
 				public Source<State> apply(Grammar childGrammar) {
 					return parse(state, childGrammar);
 				}
 			}, FunUtil.asSource(grammar.grammars)));
 		}
 
 		private Source<State> parseJoin(State state, final JoinGrammar grammar) {
 			Source<State> source = FunUtil.asSource(state);
 
 			for (final Grammar childGrammar : grammar.grammars)
 				source = FunUtil.concat(FunUtil.map(new Fun<State, Source<State>>() {
 					public Source<State> apply(State state) {
 						return parse(state, childGrammar);
 					}
 				}, source));
 
 			return source;
 		}
 
 		private Grammar child(Grammar grammar) {
 			return ((WrappingGrammar) grammar).grammar;
 		}
 
 		private Source<State> parseExpect(State state, int end) {
 			return state.pos < end ? FunUtil.asSource(new State(state, end)) : noResult;
 		}
 
 		private State deepen(State state, String name) {
 			return new State(state, state.pos, name, state.depth + 1);
 		}
 
 		private State undeepen(State state, int depth) {
 			return new State(state, state.pos, null, depth);
 		}
 
 		private int expectCharLiteral(int start) {
 			int pos = start, end;
 			if (pos < length && in.charAt(pos) == '\'') {
 				pos++;
 				if (pos < length && in.charAt(pos) == '\\')
 					pos++;
 				if (pos < length)
 					pos++;
 				if (pos < length && in.charAt(pos) == '\'') {
 					pos++;
 					end = pos;
 				} else
 					end = start;
 			} else
 				end = start;
 			return end;
 		}
 
 		private int expectFloatLiteral(int start) {
 			int pos = start;
 			pos = expectIntegerLiteral(pos);
 			if (pos < length && in.charAt(pos) == '.') {
 				pos++;
 				pos = expectIntegerLiteral(pos);
 			}
 			if (pos < length && "fd".indexOf(in.charAt(pos)) >= 0)
 				pos++;
 			return pos;
 		}
 
 		private int expectIdentifier(int start) {
 			int pos = start;
 			if (pos < length && Character.isJavaIdentifierStart(in.charAt(pos))) {
 				pos++;
 				while (pos < length && Character.isJavaIdentifierPart(in.charAt(pos)))
 					pos++;
 			}
 			return pos;
 		}
 
 		private int expectIntegerLiteral(int start) {
 			int pos = start;
 			while (pos < length && Character.isDigit(in.charAt(pos)))
 				pos++;
 			if (pos < length && in.charAt(pos) == 'l')
 				pos++;
 			return pos;
 		}
 
 		private int expectStringLiteral(int start) {
 			int pos = start, end;
 			if (pos < length && in.charAt(pos) == '"') {
 				pos++;
 				char c;
 
 				while (pos < length && (c = in.charAt(pos)) != '"') {
 					pos++;
 					if (pos < length && c == '\\')
 						pos++;
 				}
 
 				if (pos < length && in.charAt(pos) == '"') {
 					pos++;
 					end = pos;
 				} else
 					end = start;
 			} else
 				end = start;
 			return end;
 		}
 
 		private int expectString(int end, String s) {
 			return end + (in.startsWith(s, end) ? s.length() : 0);
 		}
 
 		private int expectWhitespaces(int start) {
 			int pos = start, pos1;
 			while ((pos1 = expectWhitespace(pos)) > pos)
 				pos = pos1;
 			return pos;
 		}
 
 		private int expectWhitespace(int pos) {
 			while (pos < length && Character.isWhitespace(in.charAt(pos)))
 				pos++;
 			pos = expectComment(pos, "/*", "*/");
 			pos = expectComment(pos, "//", "\n");
 			return pos;
 		}
 
 		private int expectComment(int start, String sm, String em) {
 			int sl = sm.length(), el = em.length();
 			int pos = start, end;
 			if (pos < length && Util.equals(Util.substr(in, pos, pos + sl), sm)) {
 				pos += 2;
 				while (pos < length && !Util.equals(Util.substr(in, pos, pos + el), em))
 					pos++;
 				if (pos < length && Util.equals(Util.substr(in, pos, pos + el), em)) {
 					pos += 2;
 					end = pos;
 				} else
 					end = start;
 			} else
 				end = start;
 			return end;
 		}
 
 		private Pair<Integer, Integer> findPosition(int position) {
 			int row = 1, col = 1;
 
 			for (int i = 0; i < position; i++) {
 				col++;
 
 				if (in.charAt(i) == 10) {
 					row++;
 					col = 1;
 				}
 			}
 
 			Pair<Integer, Integer> pos = Pair.create(row, col);
 			return pos;
 		}
 	}
 
 }
