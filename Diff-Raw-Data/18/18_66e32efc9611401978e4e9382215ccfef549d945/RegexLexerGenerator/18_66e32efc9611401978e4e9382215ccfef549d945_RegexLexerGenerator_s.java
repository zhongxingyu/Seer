 /**
  * 
  */
 package compiler.lex;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import compiler.Context;
 import compiler.Symbol;
 import compiler.Utils;
 import compiler.automata.Characters;
 import compiler.automata.DfaSimulator;
 import compiler.automata.FiniteAutomaton;
 import compiler.automata.Simulator;
 import compiler.automata.State;
 import compiler.lex.LexerGenerator.AbstractLexerGenerator;
 
 /**
  * @author Michael
  * 
  */
 public class RegexLexerGenerator extends AbstractLexerGenerator {
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * compiler.lex.LexerGenerator.AbstractLexerGenerator#generateImpl(compiler
 	 * .Context, java.util.LinkedHashSet, java.util.Map)
 	 */
 	@Override
 	protected Result generateImpl(final Context context,
 			LinkedHashSet<LexerAction> allActions,
 			Map<String, LinkedHashMap<String, LexerAction>> groupedActions) {
 		final Map<String, FiniteAutomaton<LexerAction, Character>> automata = new HashMap<String, FiniteAutomaton<LexerAction, Character>>(
 				groupedActions.size());
 
 		// for each state, build an automaton
 		for (String lexerState : groupedActions.keySet()) {
 			Collection<LexerAction> lexerStateActions = groupedActions.get(
 					lexerState).values();
 			FiniteAutomaton.Builder<LexerAction, Character> builder = FiniteAutomaton
 					.builder(Characters.setOperations());
 			State<LexerAction> startState = builder.newState();
 			List<State<LexerAction>> regexStartStates = new ArrayList<State<LexerAction>>(
 					lexerStateActions.size());
 
 			// construct an NFA for each regex
 			for (LexerAction lexerAction : lexerStateActions) {
 				Symbol regexParseTree = Regex.canonicalize(Regex.parse(
 						lexerAction.pattern()).parseTree());
 				regexStartStates.add(Regex.buildNfaFor(builder, lexerAction,
 						regexParseTree));
 			}
 
 			// combine the regex NFA's into a single NFA
 			for (State<LexerAction> regexStartState : regexStartStates) {
 				builder.createEdge(startState, regexStartState);
 			}
 
 			// create a DFA from the resulting NFA
 			FiniteAutomaton<LexerAction, Character> dfa = builder
 					.toFiniteAutomaton().toDfa(allActions);
 			automata.put(lexerState, dfa);
 		}
 
 		final Lexer lexer = new Lexer() {
 
 			@Override
 			public boolean isCompiled() {
 				return false;
 			}
 
 			@Override
 			public Iterator<Symbol> lex(Reader reader) {
 				final LineNumberAndPositionBufferedReader markableReader = new LineNumberAndPositionBufferedReader(
 						reader);
 				// the mark always marks the "beginning" of the stream. That is,
 				// the place
 				// where we last matched
 				markableReader.mark();
 
 				// simulators in use prior to the current simulator
 				final Deque<Simulator<LexerAction, Character>> simulatorStack = new ArrayDeque<Simulator<LexerAction, Character>>();
 				simulatorStack.push(new DfaSimulator<LexerAction, Character>(
 						automata.get(DEFAULT_STATE)));
 				
 				return new Iterator<Symbol>() {
 					private LexerAction lastMatchEndAction = null;
 					private int lastMatchOffset;
 					private boolean sentEOF = false;
 
 					@Override
 					public boolean hasNext() {
 						return !this.sentEOF;
 					}
 
 					@Override
 					public Symbol next() {
 						Symbol token = null;
 
 						// loop until we find a token to return or send eof
 						do {
 							// read the next character
 							int c = markableReader.uncheckedRead();
 							
 							// if there are no more chars to send, send eof
 							if (c == -1) {
 								if (!this.hasNext()) {
 									throw new NoSuchElementException();
 								}
 
 								// if we have a last match, roll back and match
 								// that
 								if (this.lastMatchEndAction != null) {
 									token = this.performMatch();
 								}
 								// if we have no match but we've read characters since the last
 								// mark, then we must have started accepting a symbol and then encountered EOF
 								// e. g. we saw f, o, EOF and started matching "for". In that case, the trailing characters
 								// need to be sent as unrecognized symbols. Note that we know it's always safe to check
 								// the mark offset here because we always call mark() at the beginning or after any match
 								else if (markableReader.offsetFromMark() > 0) {
 									token = this.performMatch();
 								}
								// send EOF since we're really done
								else {
 									token = context.eofType().createSymbol("",
 											markableReader.lineNumber(),
 											markableReader.position());
 									this.sentEOF = true; // causes hasNext() to return false
 									try {
 										markableReader.close();
 									} catch (IOException ex) {
 										Utils.err(ex);
 									}
 								}
 
 								return token;
 							}
 
 							// simulate the input
 							switch (this.currentSimulator().consume((char) c)) {
 							case Reject:
 								// do nothing
 								break;
 							case Accept:
 								this.lastMatchEndAction = this
 										.currentSimulator().currentValue();
 								this.lastMatchOffset = markableReader
 										.offsetFromMark();
 								break;
 							case Error:
 								// attempt to match
 								token = this.performMatch();
 								break;
 							}
 
 						} while (token == null);
 
 						return token;
 					}
 
 					private Symbol performMatch() {
 						Symbol match;
 
 						// roll back to the end of the last match
 						markableReader.reset();
 
 						// read the first character after the mark
 						int firstMatchChar = markableReader.uncheckedRead(), line = markableReader
 								.lineNumber(), position = markableReader
 								.position();
 						Utils.check(firstMatchChar != -1); // sanity check
 
 						// if we have a last match accept it
 						if (this.lastMatchEndAction != null) {
 							// re-read the matched string
 							char[] matchedChars = new char[this.lastMatchOffset];
 							matchedChars[0] = (char) firstMatchChar;
 							for (int i = 1; i < matchedChars.length; i++) {
 								matchedChars[i] = (char) markableReader
 										.uncheckedRead();
 							}
 
 							// possibly create a symbol
 							match = this.lastMatchEndAction.symbolType() != null ? this.lastMatchEndAction
 									.symbolType().createSymbol(
 											String.valueOf(matchedChars), line,
 											position) : null;
 
 							// update the current simulator
 							switch (this.lastMatchEndAction.actionType()) {
 							case Swap:
 								simulatorStack.pop();
 								// fall through
 							case Enter:
 								simulatorStack
 										.push(new DfaSimulator<LexerAction, Character>(
 												automata.get(this.lastMatchEndAction
 														.endState())));
 								break;
 							case Leave:
 								simulatorStack.pop();
 								// fall through
 							default:
 								// whenever reusing an old simulator, be sure to
 								// reset it!
 								this.currentSimulator().reset();
 								break;
 							}
 
 							this.lastMatchEndAction = null;
 						}
 						// otherwise, match unrecognized
 						else {
 							match = context.unrecognizedType().createSymbol(
 									String.valueOf((char) firstMatchChar),
 									line, position);
 							this.currentSimulator().reset();
 						}
 
 						// mark after the last match
 						markableReader.mark();
 
 						return match;
 					}
 
 					private Simulator<LexerAction, Character> currentSimulator() {
 						return simulatorStack.peekFirst();
 					}
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException("remove");
 					}
 				};
 			}
 		};
 
 		return new LexerGenerator.Result() {
 
 			@Override
 			public List<String> warnings() {
 				return Collections.emptyList();
 			}
 
 			@Override
 			public Lexer lexer() {
 				return lexer;
 			}
 
 			@Override
 			public List<String> errors() {
 				return Collections.emptyList();
 			}
 		};
 	}
 }
