 import edu.ncsu.csc216.solitaire.model.Deck;
 
 
 public class Test {
 	public static void main(String[] args) {
		int[] testDeck = {19, 7, 5, 6, 2, 15, 28, 11, 4, 8, 9, 13, 24, 27, 16, 25, 23, 14, 10, 12, 20, 3, 21, 1, 18, 17, 26, 22};
 		Deck d = new Deck(testDeck);
		for(int i = 0; i < 1000; i++) {
 			d.getKeystreamValue();
 		}
 	}
 }
