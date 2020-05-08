 /**
  *
  */
 package ca.uwaterloo.joos.scanner;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Greg Wang
  *
  */
 public class DFA {
 
 //	private static final Logger logger = Main.getLogger(DFA.class);
 
 	enum DfaStruct {
 		BOF,
 		Alphabets,
 		States,
 		StartingState,
 		AcceptingStates,
 		Transactions,
 		Transformations,
 		EOF
 	}
 
 	static private Map<String, String> symbolRegexTable = null;
 	static private String regexForKeyword(String keyword) {
 		if(DFA.symbolRegexTable == null){
 			DFA.symbolRegexTable = new HashMap<String, String>();
 			DFA.symbolRegexTable.put("any", ".");			// Anything
 			DFA.symbolRegexTable.put("space", "\\s");		// Whitespace, [' ', '\t', '\n', '\r']
			DFA.symbolRegexTable.put("eol", "[\\n\\r]");		// End of line
 			DFA.symbolRegexTable.put("alpha", "[a-zA-Z]");	// Alphabets
 			DFA.symbolRegexTable.put("digit", "[0-9]");		// Digits
			DFA.symbolRegexTable.put("punc", "[~!@#$%^&*()_+{}|:\"<>?`-=[]\\\\;',./]");		// Punctuation
			DFA.symbolRegexTable.put("escape", "[btnfr\"'\\\\]");
			DFA.symbolRegexTable.put("octal", "[0-7]");
			DFA.symbolRegexTable.put("not-octal", "[^0-7]");
			DFA.symbolRegexTable.put("0-3", "[0-3]");
 		}
 
 		return DFA.symbolRegexTable.get(keyword);
 	}
 
 	private class RegexTransaction {
 		private String regex;
 		private String next;
 
 		/**
 		 * Return the next state if input matches the regex, otherwise return null
 		 * @param inpub
 		 */
 		public String nextStateForInput(String input) {
 			return input.matches(this.regex) ? this.next : null;
 		}
 
 		public RegexTransaction(String regex, String next){
 			this.regex = regex;
 			this.next = next;
 		}
 
 	}
 
 	@SuppressWarnings("serial")
 	public class UnkownInputKeywordException extends Exception {
 		public UnkownInputKeywordException(String string) {
 			super(string);
 		}
 	}
 
 	private Set<String> alphabets;
 	private Set<String> states;
 	private String startingState;
 	private Set<String> acceptingStates;
 	private Map<String, Map<String, String>> transactions;
 	private Map<String, List<RegexTransaction>> regexTransactions;
 	private Map<String, String> tokenKindTransformations;
 
 	/**
 	 * Construct a DFA logic representation from a DFA file
 	 *
 	 * @param dfaFile
 	 * @throws IOException Fail to read the file
 	 * @throws ArrayIndexOutOfBoundsException DFA file in wrong format
 	 * @throws NumberFormatException DFA file in wrong format
 	 * @throws UnkownInputKeywordException Reach unknown keyword as an transaction rule input
 	 */
 	public DFA(File dfaFile) throws IOException, ArrayIndexOutOfBoundsException, NumberFormatException, UnkownInputKeywordException {
 		this.alphabets = new HashSet<String>();
 		this.states = new HashSet<String>();
 		this.acceptingStates = new HashSet<String>();
 		this.transactions = new HashMap<String, Map<String, String>>();
 		this.regexTransactions = new HashMap<String, List<RegexTransaction>>();
 		this.tokenKindTransformations = new HashMap<String, String>();
 
 		parseDfaFile(dfaFile);
 	}
 
 	public String getStartingState() {
 		return startingState;
 	}
 
 	public String nextStateFor(String state, String input) {
 		String next = null;
 
 		// First, try find next state from normal transactions
 		Map<String, String> inputMapNext = this.transactions.get(state);
 		if(inputMapNext != null) {
 			next = inputMapNext.get(input);
 		}
 		if(next != null) return next;
 
 		// Second, try find applicable regex transaction rules
 		List<RegexTransaction> regexTransactions = this.regexTransactions.get(state);
 		if(regexTransactions == null) return null;
 
 		// If there is any, try find next state from regex transactions
 		for(RegexTransaction regexTrans: regexTransactions) {
 			next = regexTrans.nextStateForInput(input);
 			if(next != null) return next;
 		}
 
 		// If nothing found, no match
 		return null;
 	}
 
 	public boolean isAcceptingState(String state) {
 		return this.acceptingStates.contains(state);
 	}
 
 	public String getTokenKindTransformation(String lexeme) {
 		return this.tokenKindTransformations.get(lexeme);
 	}
 
 	public void addTokenKindTransformation(String lexeme, String kind) {
 		this.tokenKindTransformations.put(lexeme, kind);
 	}
 
 	private void parseDfaFile(File dfaFile) throws IOException, ArrayIndexOutOfBoundsException, NumberFormatException, UnkownInputKeywordException {
 		FileInputStream inputSteam = new FileInputStream(dfaFile);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(inputSteam));
 
 		int remainLines = 0;
 		DfaStruct stage = DfaStruct.BOF;
 		while(reader.ready() && stage != DfaStruct.EOF) {
 			String line = reader.readLine();
 			String[] split = line.split("\\s");
 
 			// If initializing or no more line to read
 			if(remainLines == 0) {
 				stage = DfaStruct.values()[stage.ordinal() + 1];
 				if(stage == DfaStruct.StartingState) this.startingState = split[0];
 				else remainLines = Integer.parseInt(split[0]);
 				continue;
 			}
 			remainLines--;
 
 			// Parse the line
 			switch(stage) {
 			case Alphabets:
 				this.alphabets.add(split[0]);
 				break;
 			case States:
 				this.states.add(split[0]);
 				break;
 			case AcceptingStates:
 				this.acceptingStates.add(split[0]);
 				break;
 			case Transactions:
 				this.parseTransaction(split[0], split[1], split[2]);
 				break;
 			case Transformations:
 				this.addTokenKindTransformation(split[0], split[1]);
 				break;
 			case BOF:
 			case StartingState:
 			case EOF:
 			default:
 				break;
 			}
 		}
 
 		reader.close();
 	}
 
 	private void parseTransaction(String state, String input, String next) throws UnkownInputKeywordException {
 		if(input.length() > 1) {
 			List<RegexTransaction> trans = this.regexTransactions.get(state);
 			if(trans == null){
 				trans = new ArrayList<RegexTransaction>();
 				this.regexTransactions.put(state, trans);
 			}
 			String regex = DFA.regexForKeyword(input);
 			if(regex != null) {
 				trans.add(new RegexTransaction(regex, next));
 			}
 			else {
 				throw new UnkownInputKeywordException("Reach unknown DFA transaction input keyword");
 			}
 		}
 		else {
 			Map<String, String> inputMapNext = this.transactions.get(state);
 			if(inputMapNext == null) {
 				inputMapNext = new HashMap<String, String>();
 				this.transactions.put(state, inputMapNext);
 			}
 			inputMapNext.put(input, next);
 		}
 	}
 }
 
