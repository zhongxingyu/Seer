 /**
  * 
  */
 package compiler.lex;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import compiler.Context;
 import compiler.Symbol;
 import compiler.SymbolType;
 import compiler.Utils;
 import compiler.automata.Characters;
 import compiler.automata.FiniteAutomaton;
 import compiler.automata.SetOperations;
 import compiler.automata.State;
 import compiler.canonicalize.Canonicalize;
 import compiler.parse.Associativity;
 import compiler.parse.Grammar;
 import compiler.parse.LALRGenerator;
 import compiler.parse.Parser;
 import compiler.parse.Precedence;
 import compiler.parse.Precedence.ProductionPrecedence;
 import compiler.parse.Production;
 
 /**
  * @author Michael
  * 
  */
 public class Regex {
 	private static final Context context = new Context();
 
 	public static final SymbolType KLEENE_CLOSURE = context
 			.getTerminalSymbolType("*"), OR = context
 			.getTerminalSymbolType("|"), ESCAPE = context
 			.getTerminalSymbolType("\\"), LPAREN = context
 			.getTerminalSymbolType("("), RPAREN = context
 			.getTerminalSymbolType(")"), ZERO_OR_ONE = context
 			.getTerminalSymbolType("?"), ONE_PLUS = context
 			.getTerminalSymbolType("+"), RANGE_OPERATOR = context
 			.getTerminalSymbolType("-"), SET_INVERSE_OPERATOR = context
 			.getTerminalSymbolType("^"), LBRACKET = context
 			.getTerminalSymbolType("["), RBRACKET = context
 			.getTerminalSymbolType("]"), WILDCARD = context
 			.getTerminalSymbolType("."), CHAR = context
 			.getTerminalSymbolType("CHAR");
 
 	public static final SymbolType REGEX = context
 			.getNonTerminalSymbolType("REGEX"), REGEX_LIST = context
 			.getNonTerminalSymbolType("REGEX_LIST"), ESCAPED = context
 			.getNonTerminalSymbolType("ESCAPED"), RANGE = context
 			.getNonTerminalSymbolType("RANGE"), SET = context
 			.getNonTerminalSymbolType("SET"), SET_LIST = context
 			.getNonTerminalSymbolType("SET_LIST");
 
 	public static final Grammar GRAMMAR;
 	private static final Lexer lexer;
 	private static final Parser parser;
 
 	static {
 		LinkedHashSet<Production> productions = new LinkedHashSet<Production>();
 
 		for (SymbolType terminalType : Utils.set(KLEENE_CLOSURE, OR, ESCAPE,
 				LPAREN, RPAREN, ZERO_OR_ONE, ONE_PLUS, RANGE_OPERATOR, SET_INVERSE_OPERATOR,
 				LBRACKET, RBRACKET, WILDCARD, CHAR)) {
 			productions.add(new Production(ESCAPED, ESCAPE, terminalType));
 		}
 
 		productions.add(new Production(RANGE, CHAR, RANGE_OPERATOR, CHAR));
 
 		productions.addAll(Production.makeList(SET_LIST, SET, null, Context.ListOption.AllowEmpty));
 
 		productions.add(new Production(SET, RANGE));
 		productions.add(new Production(SET, ESCAPED));
 		productions.add(new Production(SET, CHAR));
 
 		productions.addAll(Production.makeList(REGEX_LIST, REGEX, null, Context.ListOption.AllowEmpty));
 
 		productions.add(new Production(REGEX, ESCAPED));
 		productions.add(new Production(REGEX, LPAREN, REGEX_LIST, RPAREN));
 		productions.add(new Production(REGEX, REGEX, OR, REGEX));
 		productions.add(new Production(REGEX, REGEX, KLEENE_CLOSURE));
 		productions.add(new Production(REGEX, REGEX, ZERO_OR_ONE));
 		productions.add(new Production(REGEX, REGEX, ONE_PLUS));
 		productions.add(new Production(REGEX, LBRACKET, SET_LIST, RBRACKET));
 		productions.add(new Production(REGEX, LBRACKET, SET_INVERSE_OPERATOR, SET_LIST, RBRACKET));
 		productions.add(new Production(REGEX, WILDCARD));
 		productions.add(new Production(REGEX, CHAR));
 
 		LinkedHashMap<Set<SymbolType>, Associativity> precedence = new LinkedHashMap<Set<SymbolType>, Associativity>();
 		precedence.put(Utils.set(KLEENE_CLOSURE, ZERO_OR_ONE, ONE_PLUS),
 				Associativity.NonAssociative);
 		precedence.put(Utils.set(OR), Associativity.Left);
 
 		GRAMMAR = new Grammar(context, "Regex", REGEX_LIST, productions,
 				Precedence.createFunction(precedence,
 						ProductionPrecedence.LeftmostTerminal,
 						Collections.<Production, SymbolType> emptyMap()));
 
 		LinkedHashSet<LexerAction> actions = new LinkedHashSet<LexerAction>();
 		for (SymbolType terminalType : GRAMMAR.terminalSymbolTypes())
 			if (terminalType.equals(CHAR))
 				actions.add(LexerAction.lexToken("", CHAR));
 			else if (!terminalType.equals(context.eofType()))
 				actions.add(LexerAction.lexToken(terminalType.name(),
 						terminalType));
 		lexer = new CharLexerGenerator().generate(context, actions).lexer();
 
 		parser = new LALRGenerator().generate(GRAMMAR).parser();
 	}
 
 	public static Lexer lexer() {
 		return lexer;
 	}
 
 	public static Parser parser() {
 		return parser;
 	}
 
 	public static Parser.Result parse(String regex) {
 		return parser().parse(lexer().lex(new StringReader(regex)));
 	}
 
 	public static Symbol canonicalize(Symbol regexParseTree) {
 		Utils.check(regexParseTree.type().equals(REGEX_LIST));
 
 		Map<SymbolType, SymbolType> listTypes = new HashMap<SymbolType, SymbolType>();
 		listTypes.put(REGEX_LIST, null);
 		listTypes.put(SET_LIST, null);
 
 		return Canonicalize.flattenLists(regexParseTree, listTypes, false);
 	}
 
 	/**
 	 * Builds an NFA for the given accept value and regex, and returns the start
 	 * state (if the builder already has states, then this will not be the same
 	 * as the eventual automata's start state).
 	 */
 	public static <T> State<T> buildNfaFor(
 			FiniteAutomaton.Builder<T, Character> builder, T acceptValue,
 			Symbol regexSymbol) {
 		State<T> startState = builder.newState();
 		State<T> acceptState = builder.newState(acceptValue);
 		State<T> headState = buildNfa(regexSymbol, startState, builder);
 		builder.createEdge(headState, acceptState);
 
 		return startState;
 	}
 
 	/**
 	 * Creates an NFA with a tail leading from the provided start state to a
 	 * head state, which is returned.
 	 */
 	private static <T> State<T> buildNfa(Symbol regexSymbol,
 			State<T> startState, FiniteAutomaton.Builder<T, Character> builder) {
 		// build the list by creating intermediate start and end states and
 		// filling the gaps
 		if (regexSymbol.type().equals(REGEX_LIST)) {
 			// for strict compliance with MCI, an empty list goes to
 			// start - epsilon -> head
 			if (regexSymbol.children().isEmpty()) {
 				State<T> headState = builder.newState();
 				builder.createEdge(startState, headState);
 				return headState;
 			}
 
 			// start - R1 - R2 ...
 			State<T> listHead = startState;
 			for (Symbol child : regexSymbol.children()) {
 				State<T> newListHead = buildNfa(child, listHead, builder);
 				listHead = newListHead;
 			}
 
 			return listHead;
 		}
 		// For a regex, we look at it's type and construct the appropriate DFA.
 		if (regexSymbol.type().equals(REGEX)) {
 			SymbolType type;
 			switch (regexSymbol.children().size()) {
 			case 1:
 				type = regexSymbol.children().get(0).type();
 				if (type.equals(ESCAPED)) {
 					char equivalentValue = getChar(regexSymbol.children()
 							.get(0));
 
 					// equivalent to 'equivalentCharacter'
 					return buildNfa(
 							REGEX.createSymbol(CHAR.createSymbol(
 									String.valueOf(equivalentValue), 1, 1)),
 							startState, builder);
 				}
 				if (type.equals(CHAR)) {
 					// start - ch -> head
 					State<T> headState = builder.newState();
 					builder.createEdge(startState, Collections
 							.singleton(getChar(regexSymbol.children().get(0))),
 							headState);
 					return headState;
 				}
 				if (type.equals(WILDCARD)) {
 					// start - any -> head
 					State<T> headState = builder.newState();
 					builder.createEdge(startState, Characters.allCharacters(),
 							headState);
 					return headState;
 				}
 				throw Utils.err("Should never get here!");
 			case 2:
 				type = regexSymbol.children().get(1).type();
 				if (type.equals(ZERO_OR_ONE)) {
 					// same as R | ()
 					return buildNfa(REGEX.createSymbol(regexSymbol.children()
 							.get(0), OR.createSymbol("FAKE", 1, 1), REGEX
 							.createSymbol(LPAREN.createSymbol("FAKE", 1, 1),
 									REGEX_LIST.createSymbol(),
 									RPAREN.createSymbol("FAKE", 1, 1))),
 							startState, builder);
 				}
 				if (type.equals(ONE_PLUS)) {
 					// same as RR*
 					Symbol innerRegex = regexSymbol.children().get(0);
 					return buildNfa(
 							REGEX_LIST.createSymbol(innerRegex, REGEX
 									.createSymbol(innerRegex, KLEENE_CLOSURE
 											.createSymbol("FAKE", 1, 1))),
 							startState, builder);
 				}
 				if (type.equals(KLEENE_CLOSURE)) {
 					/*
 					 * start - epsilon -> head AND head - R - epsilon -> head
 					 */
 					State<T> headState = builder.newState();
 					State<T> innerHeadState = buildNfa(regexSymbol.children()
 							.get(0), headState, builder);
 					builder.createEdge(startState, headState);
 					builder.createEdge(innerHeadState, headState);
 					return headState;
 				}
 				throw Utils.err("Should never get here!");
 			case 3:
 				type = regexSymbol.children().get(1).type();
 				if (type.equals(OR)) {
 					/*
 					 * start - epsilon -> tail AND ( tail - R1 - epsilon -> head
 					 * OR tail - R2 - epsilon -> head )
 					 */
 					State<T> tailState = builder.newState();
 					State<T> headState = builder.newState();
 					State<T> innerHeadState1 = buildNfa(regexSymbol.children()
 							.get(0), tailState, builder);
 					State<T> innerHeadState2 = buildNfa(regexSymbol.children()
 							.get(2), tailState, builder);
 					builder.createEdge(startState, tailState);
 					builder.createEdge(innerHeadState1, headState);
 					builder.createEdge(innerHeadState2, headState);
 					return headState;
 				}
 				if (type.equals(REGEX_LIST)) {
 					// recurse on the list
 					return buildNfa(regexSymbol.children().get(1), startState,
 							builder);
 				}
 				if (type.equals(SET_LIST)) {
 					/*
 					 * start - epsilon -> tail AND ( tail - S1 -> head OR tail -
 					 * S2 -> head ... )
 					 */
 					State<T> tailState = builder.newState();
 					State<T> headState = builder.newState();
 					for (Symbol setChild : regexSymbol.children().get(1)
 							.children()) {
 						Symbol setSymbol = setChild.children().get(0);
 						Collection<Character> charSet;
 						if (setSymbol.type().equals(CHAR)) {
 							// tail - ch -> head
 							charSet = Collections.singleton(getChar(setSymbol));
 						} else if (setSymbol.type().equals(ESCAPED)) {
 							// tail - \ch -> head
 							charSet = Collections.singleton(getChar(setSymbol));
 						} else if (setSymbol.type().equals(RANGE)) {
 							// start - [chars] -> end
 							char min = getChar(setSymbol.children().get(0)), max = getChar(setSymbol
 									.children().get(2));
 							charSet = Characters.range(min, max);
 						} else {
 							throw Utils.err("Should never get here!");
 						}
 						builder.createEdge(tailState, charSet, headState);
 					}
 					builder.createEdge(startState, tailState);
 					return headState;
 				}
 				throw Utils.err("Should never get here!");
 			case 4:
 				type = regexSymbol.children().get(2).type();
 				if (type.equals(SET_LIST)) {
 					Utils.check(regexSymbol.children().get(1).type().equals(SET_INVERSE_OPERATOR));
 					// an inverted set is just a set with the contents inverted
 					return buildNfa(REGEX.createSymbol(regexSymbol.children().get(0), invertSetList(regexSymbol.children().get(2)), regexSymbol.children().get(3)), startState, builder);
 				}
 				throw Utils.err("Should never get here!");
 			default:
 				throw Utils.err("Should never get here!");
 			}
 		}
 		throw Utils.err("Should never get here!");
 	}
 
 	private static Symbol invertSetList(Symbol symbol) {
 		Utils.check(symbol.type().equals(SET_LIST));
 		
 		// if the list is empty, this is equivalent to a single range from MIN_CHAR to MAX_CHAR
 		// because the inverse of the empty set is the universe
 		if (symbol.children().isEmpty()) {
 			return SET_LIST.createSymbol(makeRange(Character.MIN_VALUE, Character.MAX_VALUE));
 		}
 		
 		// (1) collect all characters and ranges
 		List<Collection<Character>> charSets = new ArrayList<Collection<Character>>();
		for (Symbol child : symbol.children().get(0).children()) { // go two levels deep because child is a SET
 			if (child.type().equals(RANGE)) {
 				charSets.add(Characters.range(getChar(child.children().get(0)), getChar(child.children().get(2))));
 			} else {
 				charSets.add(Collections.singleton(getChar(child)));
 			}
 		}
 		
 		// (2) create non-overlapping ranges (we know they're ranges or singletons due to the nature of the possible sets
 		// we passed in)
 		final SetOperations<Character> setOps = Characters.setOperations();
 		Set<Collection<Character>> nonOverlappingRanges = setOps.partitionedUnion(charSets);
 		
 		// (3) sort the ranges
 		List<Collection<Character>> sortedRanges = new ArrayList<Collection<Character>>(nonOverlappingRanges);
 		Collections.sort(sortedRanges, new Comparator<Collection<Character>>() {
 			@Override
 			public int compare(Collection<Character> a,
 					Collection<Character> b) {
 				char minA = setOps.min(a),
 					minB = setOps.min(b);
 				return minA < minB ? -1 : (minA > minB ? 1 : 0);
 			}			
 		});
 		
 		// (4) the inverse set is composed of the gaps between the ranges and at the ends
 		List<Symbol> gaps = new ArrayList<Symbol>(sortedRanges.size() + 1);
 		char min = setOps.min(sortedRanges.get(0)), 
 			max = setOps.max(Utils.last(sortedRanges));
 		if (Character.MIN_VALUE < min) {
 			gaps.add(makeRange(Character.MIN_VALUE, (char)(min - 1)));
 		}
		for (int i = 0; i < gaps.size() - 1; i++) {
 			// note that we don't have to check max(i) < min(i + 1) here because we know the ranges don't overlap :)
 			gaps.add(makeRange((char)(setOps.max(sortedRanges.get(i)) + 1), (char)(setOps.min(sortedRanges.get(i + 1)) - 1)));
 		}
 		if (Character.MAX_VALUE > max) {
 			gaps.add(makeRange((char)(max + 1), Character.MAX_VALUE));
 		}
 		
 		return SET_LIST.createSymbol(gaps);
 	}
 	
 	private static Symbol makeRange(char min, char max) {
 		if (min == max) {
 			return SET.createSymbol(CHAR.createSymbol(String.valueOf(min), 1, 1));
 		}
 		
 		Symbol start = CHAR.createSymbol(String.valueOf(min), 1, 1),
 			end = CHAR.createSymbol(String.valueOf(max), 1, 1);
 		return SET.createSymbol(RANGE.createSymbol(start, RANGE_OPERATOR.createSymbol("FAKE", 1, 1), end));
 	}
 
 	private static char getChar(Symbol singleCharSymbol) {
 		if (singleCharSymbol.type().equals(ESCAPED)) {
 			char escapedChar = getChar(singleCharSymbol.children().get(1)), equivalentValue;
 			switch (escapedChar) {
 			case 'n': // newline
 				equivalentValue = '\n';
 				break;
 			case 't':
 				equivalentValue = '\t';
 				break;
 			case 'r':
 				equivalentValue = '\r';
 				break;
 			default:
 				equivalentValue = escapedChar;
 				break;
 			}
 
 			return equivalentValue;
 		}
 
 		// sanity check, until we support more complex character types
 		Utils.check(singleCharSymbol.text().length() == 1, singleCharSymbol.text());
 
 		return singleCharSymbol.text().charAt(0);
 	}
 	
 	/**
 	 * Returns a pattern that matches the literal pattern
 	 */
 	public static String escape(String pattern) {
 		Iterator<Symbol> symbols = lexer().lex(new StringReader(pattern));
 		StringBuilder sb = new StringBuilder();
 		for (Symbol next = symbols.next(); !next.type().equals(context.eofType()); next = symbols.next()) {
 			if (!next.type().equals(CHAR)) {
 				sb.append(ESCAPE.name());
 			}
 			sb.append(next.text());
 		}
 		
 		return sb.toString();
 	}
 }
