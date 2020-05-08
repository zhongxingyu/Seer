 /**
  * 
  */
 package compiler.lex;
 
 import java.io.*;
 import java.util.*;
 
 import compiler.*;
 
 /**
  * A simple lexer generator which handles only single-character strings and the
  * empty string (the empty string is interpreted as matching any character)
  * 
  * @author Michael
  */
 public class CharLexerGenerator extends LexerGenerator.AbstractLexerGenerator {
 
 	@Override
 	protected Result generateImpl(final Context context,
 			LinkedHashSet<LexerAction> allActions,
 			final Map<String, LinkedHashMap<String, LexerAction>> groupedActions) {
 		for (LexerAction la : allActions)
 			Utils.check(la.pattern().length() <= 1,
 					String.format("Pattern \"%s\" is too long!", la.pattern()));
 
 		final Lexer lexer = new Lexer() {
 
 			@Override
 			public boolean isCompiled() {
 				return false;
 			}
 
 			@Override
 			public Iterator<Symbol> lex(Reader reader) {
 				final LineNumberAndPositionBufferedReader bufferedReader = new LineNumberAndPositionBufferedReader(
 						reader);
 
 				return new Iterator<Symbol>() {
 					private boolean sentEOF = false;
 					private Deque<String> stateStack = new ArrayDeque<String>(
 							LexerAction.DEFAULT_SET);
 					private LinkedHashMap<String, LexerAction> stateActions = groupedActions
 							.get(Lexer.DEFAULT_STATE);
 
 					@Override
 					public boolean hasNext() {
 						return !this.sentEOF;
 					}
 
 					@Override
 					public Symbol next() {
 						SymbolType tokenType = null;
 						Symbol token = null;
 
 						// loop until we find a token to return or send eof
 						do {
 							// read the next character
 							int c = bufferedReader.uncheckedRead();
 
 							// if there are no more chars to send, send eof
 							if (c == -1) {
 								if (!this.hasNext())
 									throw new NoSuchElementException();								
 								this.sentEOF = true;
 								return context.eofType().createSymbol("",
 										bufferedReader.lineNumber(),
 										bufferedReader.position());
 							}
 
 							String text = String.valueOf((char) c);
 							LexerAction action = this.stateActions.get(text);
 							if (action == null)
 								action = this.stateActions.get("");
 
 							if (action == null) {
 								tokenType = context.unrecognizedType();
 							} else {
 								tokenType = action.symbolType();
 								switch (action.actionType()) {
 								case Swap:
 									this.stateStack.pop();
 									// fall through
 								case Enter:
 									this.stateStack.push(action.endState());
 									this.stateActions = groupedActions
 											.get(action.endState());
 									break;
 								case Leave:
 									this.stateStack.pop();
 									this.stateActions = groupedActions
 											.get(this.stateStack.peekFirst());
 									break;
 								case None:
 									break;
 								}
 							}
 
 							// if we have a token type, create a token
 							if (tokenType != null)
 								token = tokenType.createSymbol(text,
 										bufferedReader.lineNumber(),
 										bufferedReader.position());
 						} while (token == null);
 
 						return token;
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
