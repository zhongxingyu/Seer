 package Source;
 
 import java.util.ArrayList;
 
 /**
  * TableWalker is initialized with a DFATable. calling walkTable with an input
  * char will cause TableWalker to update its state based on its table and return
  * a token auto-magically at the correct time.
  * 
  * @author Sam
  * 
  */
 
 public class TableWalker {
 	/*
 	 * dfa is the dfaTable (a hashmap which contains mappings between
 	 * (currentstate, transitioncharacter) and resulting state.
 	 * lastKnownValidToken is pretty self-explanatory. We use it to hold onto
 	 * the last thing we know is a valid token and is returnable once we reach
 	 * something we can't add to the current token. currentToken is where we
 	 * hold the current set of characters we're dealing with. We add to it with
 	 * every character from the file and we take things out of it when we need
 	 * to pull out a valid token. startState is the very first state of the DFA.
 	 * currentState is the state we're currently in. returnList is a list that
 	 * holds the tokens we may need to return upon this character. It's a list
 	 * for a reason that's easier to show in example than in text: Say our
 	 * language accepts only "abd", "a", and "be". We feed the machine abe. When
 	 * it sees a, it knows it can stop there, but it's going to continue looking
 	 * for abd because it wants to get the longest token possible. It hits e,
 	 * then realizes abd won't work. It then needs to go ahead and return both
 	 * "a" and "be" on the same turn. So the list lets us do that.
 	 */
 	private DFATable dfa;
 	private StringBuffer lastKnownValidToken;
 	private StringBuffer currentToken = new StringBuffer();
 	private State startState;// I need a way to get this from the dfa table.
 	private State currentState;
 	private ArrayList<Token> returnList = new ArrayList<Token>();
 	private String lastKnownValidTokenType;
 
 	public TableWalker(DFATable dfaTable, State start) {
 		this.dfa = dfaTable;
 		startState = start;
 		currentState = startState;
 	}
 
 	public DFATable getDfa() {
 		return dfa;
 	}
 
 	/**
 	 * walkTable will take in a character c, check table position and return a
 	 * token when state is sanguine
 	 * 
 	 * @return An array of valid tokens, or "null" if it's not yet ready to
 	 *         return anything.
 	 */
 	public ArrayList<Token> walkTable(char c) {
 		/*
 		 * Ignore whitespace!
 		 */
 		boolean whiteSpace = false;
 		if((c==' ') || (c=='\t')||(c=='\n')||(c=='\r')){
 			//return null;
 			whiteSpace = true;
 		}
 		
 		/*
 		 * Start by clearing out the return list, we don't want any entries from
 		 * previous runs sticking around.
 		 */
 		returnList = new ArrayList<Token>();
 		/*
 		 * If the current state is final, save it as a last known valid state we
 		 * could go back to.
 		 */
 		if (currentState.isFinal) {
 			lastKnownValidToken = new StringBuffer(currentToken);
 			lastKnownValidTokenType = currentState.tokenName;
 		} else {
 			lastKnownValidToken = new StringBuffer();
 		}
 		/*
 		 * Let's add the new character to the buffer of characters we're
 		 * considering.
 		 */
 		if(whiteSpace==false){
 			currentToken.append(c);
 		}
 
 		/*
 		 * We move to the next state based on the character we just took in,
 		 * using the dfa hashmap.
 		 */
 		currentState = dfa.get(new StateCharacter(currentState, c));
 		/*
 		 * If the new state is "null", it's a dead state, so we need to do the
 		 * following: 1. Hand back the last known valid token 2. Remove that
 		 * valid token from the buffer of characters we're currently looking at
 		 * 3. If there are any characters left over, re-process them
 		 */
 		if (currentState == null) {
 
 			/*
 			 * We're going to remove our valid token from the current buffer and
 			 * save the result inside newCurrentToken. We'll re-evaluate it
 			 * later.
 			 */
 			int numCharactersUndealtWith = currentToken.length()
 					- lastKnownValidToken.length();
 			StringBuffer newCurrentToken = new StringBuffer();
 			/*
 			 * If we never had a valid token this time round, we know that the
 			 * first character of the current token can't be any good. So we'll
 			 * discard it and reevaluate the rest. Example: If only the strings
 			 * "abcd" and "bc" are good, and we see "abc", we never had a valid
 			 * token while progressing through abc, so we'll discard a and
 			 * re-evalute the bc part.
 			 */
 			if (lastKnownValidToken.length() == 0) {
 				for (int i = 1; i < numCharactersUndealtWith; i++) {
 					newCurrentToken.append(currentToken.charAt(currentToken
 							.length() - numCharactersUndealtWith + i));
 				}
 			}
 			/*
 			 * If we DID have a valid token this time round, we'll discard the
 			 * characters which were part of the valid token, and reevaluate the
 			 * rest.
 			 */
 			else {
 				for (int i = 0; i < numCharactersUndealtWith; i++) {
 					newCurrentToken.append(currentToken.charAt(currentToken
 							.length() - numCharactersUndealtWith + i));
 				}
 			}
 
 			/*
 			 * If we had a last known valid token, let's hold it inside the
 			 * actual Token class and add it to our returnList.
 			 */
 			if (lastKnownValidToken.length() != 0) {
 				Token newToken = new Token(lastKnownValidTokenType, lastKnownValidToken);
 				returnList.add(newToken);// How do we know what type the token
 											// is?
 			}
 			/*
 			 * If we didn't have a valid token previously, we need to get rid of
 			 * the first character (since it can't possibly be good) and
 			 * reevaluate the rest from the beginning.
 			 */
 			else {
				System.out.println("SHIT SHIT SHIT: '"+currentToken+"'");
 				throw new IllegalArgumentException(currentToken.charAt(0) + " could not be recognized as part of a valid token.");
 			}
 
 			/*
 			 * We'll go ahead and reevaluate whatever characters we had left
 			 * over
 			 */
 			reEvaluate(newCurrentToken);
 			/*
 			 * And finally, return our returnList.
 			 */
 			
 			if(c==(char)65535){
 				if(currentToken.length() != 1){
 					throw new IllegalArgumentException(currentToken.toString() + " could not be recognized as part of a valid token.");
 				}
 			}
 			return returnList;
 		} else {
 			/*
 			 * Since the current state isn't null, we could potentially go on to
 			 * recognize a bigger token. So we just return null for now.
 			 */
 			if(c==(char)65535){
 				if(currentToken.length() != 0){
 					throw new IllegalArgumentException(currentToken.toString() + " could not be recognized as part of a valid token.");
 				}
 			}
 			return null;
 		}
 	}
 
 	private void reEvaluate(StringBuffer newCurrentToken) {
 
 		/*
 		 * We're going to begin again at the start state. We'll clear out our
 		 * lastKnownValidToken, since that's already been added to the
 		 * returnList, and we'll make our current buffer equal to the passed- in
 		 * buffer that holds characters we need to re-evaluate.
 		 */
 		currentState = startState;
 		lastKnownValidToken = new StringBuffer();
 		currentToken = newCurrentToken;
 		newCurrentToken = new StringBuffer();
 
 		/*
 		 * We're going to go through every character in our buffer, trying to
 		 * move as far as we can through the dfa.
 		 */
 		for (int i = 0; i < currentToken.length(); i++) {
 			if(currentToken.charAt(i) == (char)65535){
 				return;
 			}
 			currentState = dfa.get(new StateCharacter(currentState,
 					currentToken.charAt(i)));
 
 			/*
 			 * If we've gone as far as we can....
 			 */
 			if (currentState == null) {
 
 				/*
 				 * Figure out how many characters we aren't able to use.
 				 */
 				int numCharactersUndealtWith = currentToken.length()
 						- lastKnownValidToken.length();
 				/*
 				 * If we never had a valid token this time round, we know that
 				 * the first character of the current token can't be any good.
 				 * So we'll discard it and reevaluate the rest. Example: If only
 				 * the strings "abcd" and "bc" are good, and we see "abc", we
 				 * never had a valid token while progressing through abc, so
 				 * we'll discard a and re-evalute the bc part.
 				 */
 				if (lastKnownValidToken.length() == 0) {
 					for (int j = 1; j < numCharactersUndealtWith; j++) {
 						newCurrentToken.append(currentToken.charAt(currentToken
 								.length() - numCharactersUndealtWith + j));
 					}
 				}
 				/*
 				 * If we DID have a valid token this time round, we'll discard
 				 * the characters which were part of the valid token, and
 				 * reevaluate the rest.
 				 */
 				else {
 					for (int k = 0; k < numCharactersUndealtWith; k++) {
 						newCurrentToken.append(currentToken.charAt(currentToken
 								.length() - numCharactersUndealtWith + k));
 					}
 				}
 
 				/*
 				 * If we actually had a previous valid token, add it to the
 				 * return list, and reevaluate the characters that weren't part
 				 * of the valid token.
 				 */
 				if (lastKnownValidToken.length() != 0) {
 					returnList.add(new Token(lastKnownValidTokenType, lastKnownValidToken));
 					reEvaluate(newCurrentToken);
 				}
 				/*
 				 * If we didn't have a previously valid token, we need to throw
 				 * an error, and reevaluate everything without the first
 				 * character, which can't possibly be good.
 				 */
 				else {
 					throw new IllegalArgumentException(currentToken.charAt(0) + " could not be recognized as part of a valid token.");
 				}
 			}
 			/*
 			 * If we can still continue (i.e. we didn't reach a 'null' state),
 			 * check to see if we're in a final state. If we are, save it as the
 			 * last known valid token.
 			 */
 			else {
 				if (currentState.isFinal) {
 					lastKnownValidToken = currentToken;
 				}
 			}
 		}
 	}
 }
