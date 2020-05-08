 import edu.ncsu.csc216.solitaire.model.Deck;
 
 
 public class Test {
 	public static void main(String[] args) {
		int[] testDeck = {13, 15, 4, 27, 9, 1, 18, 12, 2, 6, 22, 14, 20, 8, 23, 7, 3, 24, 10, 17, 16, 21, 11, 25, 5, 26, 19, 28};
 		Deck d = new Deck(testDeck);
		for(int i = 0; i < 100; i++) {
 			d.getKeystreamValue();
 		}
 	}
 }
