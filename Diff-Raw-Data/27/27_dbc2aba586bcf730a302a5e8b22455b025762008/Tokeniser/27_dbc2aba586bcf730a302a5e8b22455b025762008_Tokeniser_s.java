 package bruno.lang.grammar;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 
 import bruno.lang.grammar.Grammar.Rule;
 
 public final class Tokeniser {
 
 	private final Grammar grammar;
 
 	public Tokeniser(Grammar grammar) {
 		super();
 		this.grammar = grammar;
 	}
 
 	public Tokens tokenise(String start, byte[] input) {
 		Rule r = grammar.rule(start.intern());
 		Tokens tokens = new Tokens(8000);
 		try {
 			int t = tokenise(r, ByteBuffer.wrap(input), 0, Rule.ANY_WHITESPACE, tokens);
 		} catch (Exception e) {
 			System.err.println(tokens);
 		}
 		if (tokens.end() != input.length) {
 			System.err.println("Failed to parse:");
			System.out.println(new String(input, tokens.end(), 60));
 		}
 		//TODO verify and visualize errors
 		return tokens;
 	}
 	
 	private static int tokenise(Rule rule, ByteBuffer input, int position, Rule separator, Tokens tokens) {
 		switch (rule.type) {
 		case LITERAL:
 			return character(rule, input, position);
 		case TERMINAL:
 			return terminal(rule, input, position); 
 		case ITERATION:
 			return iteration(rule, input, position, separator, tokens);
 		case SEQUENCE:
 			return sequence(rule, input, position, separator, tokens);
 		case SELECTION:
 			return selection(rule, input, position, separator, tokens);
 		case CAPTURE:
 			return capture(rule, input, position, separator, tokens);
 		default:
 			throw new IllegalArgumentException("`"+rule+"` has no proper type: "+rule.type);
 		}
 	}
 
 	private static int character(Rule rule, ByteBuffer input, int position) {
 		if (position >= input.limit())
 			return -1;
 		final byte[] l = rule.literal;
 		for (int i = 0; i < l.length; i++) {
 			if (input.get(position) != l[i])
 				return -1;
 		}
 		return position+l.length;
 	}
 
 	private static int capture(Rule rule, ByteBuffer input, int position,
 			Rule separator, Tokens tokens) {
 		tokens.push(rule, position);
 		int end = tokenise(rule.elements[0], input, position, separator, tokens);
 		if (end > position) {
 			tokens.done(end);
 		} else {
 			tokens.pop();
 		}
 		return end;
 	}
 
 	private static int selection(Rule rule, ByteBuffer input, int position,
 			Rule separator, Tokens tokens) {
 		for (Rule r : rule.elements) {
 			int end = tokenise(r, input, position, separator, tokens);
 			if (end >= 0) {
 				return end;
 			}
 		}
 		return -1;
 	}
 
 	private static int sequence(Rule rule, ByteBuffer input, int position,
 			Rule separator, Tokens tokens) {
 		int end = position;
 		for (int i = 0; i < rule.elements.length; i++) {
 			end = tokenise(rule.separation, input, end, Rule.EMPTY_STRING, tokens);
 			Rule r = rule.elements[i];
 			int endPosition = tokenise(r, input, end, separator, tokens);
 			if (endPosition == -1) {
 				return -1;
 			}
 			end = endPosition;
 		}
 		return end;
 	}
 
 	private static int iteration(Rule rule, ByteBuffer input, int position,
 			Rule separator, Tokens tokens) {
 		int end = position;
 		int c = 0;
 		while (c < rule.occur.max) {
 			int endPosition = tokenise(rule.elements[0], input, end, separator, tokens);
 			if (endPosition == -1) {
 				if (c < rule.occur.min) {
 					return -1;
 				}
 				return end;
 			} else {
 				end = endPosition;
 				if (rule.separation != null) {
 					end = tokenise(rule.separation, input, end, Rule.EMPTY_STRING, tokens);
 				}
 				c++;
 			}
 		}
 		return end;
 	}
 
 	private static int terminal(Rule rule, ByteBuffer input, int position) {
 		if (position >= input.limit())
 			return -1;
 		final int l = rule.terminal.matching(input, position);
 		return l == 0 ? -1 : position + l;
 	}
 	
 	public static Tokens tokenise(String filename) throws IOException {
 		Tokeniser t = new Tokeniser(BNF.GRAMMAR);
 		return t.tokenise("grammar", readFile(filename));
 	}
 
 	static byte[] readFile(String path) throws IOException {
 		return Files.readAllBytes(Paths.get(path));
 	}
 
 }
