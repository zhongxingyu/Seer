 /**
  * 
  */
 package compiler.lex;
 
 import java.util.*;
 
 import compiler.*;
 
 /**
  * @author Michael
  * 
  */
 public class LexerAction {
 	public enum ActionType {
 		None, Enter, Leave, Swap,
 	}
 
 	public static final Set<String> DEFAULT_SET = Collections
 			.singleton(Lexer.DEFAULT_STATE);
 
 	private final Set<String> validStates;
 	private final String pattern, endState;
 	private final SymbolType symbolType;
 	private final ActionType actionType;
 
 	private LexerAction(Set<String> validStates, String pattern,
 			SymbolType symbolType, ActionType actionType, String endState) {
 		Utils.check(pattern != null, "Pattern cannot be null!");
 		Utils.check(!validStates.isEmpty(),
 				"Action must be valid in at least one state!");
 		Utils.check(!validStates.contains(null), "Valid states cannot be null");
 		Utils.check(symbolType == null || symbolType.isTerminal(),
 				"Non-terminal symbol types cannot be lexed!");
 
 		this.validStates = validStates;
 		this.pattern = pattern;
 		this.symbolType = symbolType;
 		this.actionType = actionType;
 		this.endState = endState;
 	}
 
 	public Set<String> validStates() {
 		return this.validStates;
 	}
 
 	public String pattern() {
 		return this.pattern;
 	}
 
 	public SymbolType symbolType() {
 		return this.symbolType;
 	}
 
 	public ActionType actionType() {
 		return this.actionType;
 	}
 
 	public String endState() {
 		return this.endState;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 
 		LexerAction that = Utils.cast(obj, LexerAction.class);
 		return that != null && that.pattern.equals(this.pattern)
 				&& that.validStates.equals(this.validStates)
 				&& Utils.equals(that.symbolType, this.symbolType)
 				&& that.actionType.equals(this.actionType)
 				&& Utils.equals(that.endState, this.endState);
 	}
 
 	@Override
 	public int hashCode() {
 		return this.pattern.hashCode() ^ this.validStates.hashCode()
 				^ Utils.hashCode(this.symbolType) ^ this.actionType.hashCode()
 				^ Utils.hashCode(this.endState);
 	}
 
 	@Override
 	public String toString() {
 		String s = "in states " + this.validStates + " on pattern \""
 				+ this.pattern + "\"";
 
 		boolean doLex = this.symbolType != null, doAction = this.actionType != ActionType.None;
 
 		if (doLex)
 			s += " lex " + this.symbolType.name();
 		if (doAction) {
 			s += doLex ? " and " : " ";
 			switch (this.actionType) {
 			case Enter:
 				s += "enter state " + this.endState;
				break;
 			case Leave:
 				s += "leave current state";
				break;
 			case Swap:
 				s += "switch current state to " + this.endState;
				break;
			case None:
				break;
 			}
 		}
 
 		return s;
 	}
 
 	/**
 	 * Lexes a token in the default state
 	 */
 	public static LexerAction lexToken(String pattern, SymbolType symbolType) {
 		return new LexerAction(DEFAULT_SET, pattern, symbolType,
 				ActionType.None, null);
 	}
 
 	/**
 	 * Skips over the input
 	 */
 	public static LexerAction skip(Set<String> validStates, String pattern) {
 		return lexToken(validStates, pattern, null);
 	}
 
 	/**
 	 * Lexes a token in the specified state
 	 */
 	public static LexerAction lexToken(String validState, String pattern,
 			SymbolType symbolType) {
 		return new LexerAction(Collections.singleton(validState), pattern,
 				symbolType, ActionType.None, null);
 	}
 
 	/**
 	 * Lexes a token in any one of the specified set of states
 	 */
 	public static LexerAction lexToken(Set<String> validStates, String pattern,
 			SymbolType symbolType) {
 		return new LexerAction(Utils.immutableCopy(validStates), pattern,
 				symbolType, ActionType.None, null);
 	}
 
 	/**
 	 * Enters (pushes) the specified state
 	 */
 	public static LexerAction enter(Set<String> validStates, String pattern,
 			SymbolType symbolType, String endState) {
 		Utils.check(endState != null, "Cannot enter a null state!");
 
 		return new LexerAction(Utils.immutableCopy(validStates), pattern,
 				symbolType, ActionType.Enter, endState);
 	}
 
 	/**
 	 * Leaves (pops) the current state
 	 */
 	public static LexerAction leave(Set<String> validStates, String pattern,
 			SymbolType symbolType) {
 		return new LexerAction(Utils.immutableCopy(validStates), pattern,
 				symbolType, ActionType.Leave, null);
 	}
 
 	/**
 	 * Swaps the current state for the specified state
 	 */
 	public static LexerAction swap(Set<String> validStates, String pattern,
 			SymbolType symbolType, String endState) {
 		Utils.check(endState != null, "Cannot enter a null state!");
 
 		return new LexerAction(Utils.immutableCopy(validStates), pattern,
 				symbolType, ActionType.Swap, endState);
 	}
 }
