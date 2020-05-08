 public class PrioritizationTest {
 	static TokenPrioritizer prioritizer = TokenPrioritizerFactory.getInstance();
 
 	public static void main(String[] args) {
 		// Some dummy token IDs
 		String firstTokenID = "dummy-token-ID-1";
 		String secondTokenID = "dummy-token-ID-2";
		String thirdTokenID = "dummy-token-ID-2";
 
 		// Create a couple of test Tokens
 		Token firstToken = new Token(firstTokenID, 3);
 		Token secondToken = new Token(secondTokenID, 1);
 		Token thirdToken = new Token(thirdTokenID, 1);
 
 		// Add the tokens to the prioritizer
 		prioritizer.addToken(firstToken);
 		prioritizer.addToken(secondToken);
 
 		// Confirm that we get the correct token back from the prioritizer
 		Token returnedToken = prioritizer.nextToken();
 		assertTrue(returnedToken.getTokenID().equals(secondTokenID));
 	}
 
 
 	static void assertTrue(boolean assertion) {
 		if (!assertion) {
 			throw new RuntimeException("Assertion Failed");
 		}
 	}
 }
