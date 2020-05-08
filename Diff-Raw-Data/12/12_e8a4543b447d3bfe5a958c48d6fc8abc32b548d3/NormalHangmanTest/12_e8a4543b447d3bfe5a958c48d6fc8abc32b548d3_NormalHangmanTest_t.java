 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class NormalHangmanTest {
 	
 	private NormalHangMan hm;
 	private final String WORD = "SPONGEBOB";
 
 	@Before
 	public void setUp() throws Exception {
 		hm = new NormalHangMan(WORD, 8, new ArrayList<Character>());
 	}
 	
 	@Test
 	public void testInitialValues() {
 		// call the constructor and see that the initial values are correct
 		assertEquals(WORD, hm.getSecretWord());
 		assertEquals(8, hm.numGuessesRemaining());
 		assertEquals(7, hm.numLettersRemaining()); // because the word has 7 distinct letters
 		assertEquals("_ _ _ _ _ _ _ _ _ ", hm.displayGameState());
 		assertEquals(hm.lettersGuessed().isEmpty(), true);
 		assertFalse(hm.gameOver());
 	}
 
 	@Test
 	public void testCorrectGuess1() {
 		// make a correct guess and see if everything is updated
 		boolean correct = hm.makeGuess('S');
 		assertTrue(correct);
 
 		assertEquals(8, hm.numGuessesRemaining());
 		assertEquals(6, hm.numLettersRemaining());
 		assertEquals("S _ _ _ _ _ _ _ _ ", hm.displayGameState());
 		assertEquals('S', hm.lettersGuessed().get(0), 'S');
 		assertFalse(hm.gameOver());
 	}
 	
 	@Test
 	public void testCorrectGuess2() {
 		// make a correct guess and see if everything is updated
 		boolean correct = hm.makeGuess('O');
 		assertTrue(correct);
 
 		assertEquals(8, hm.numGuessesRemaining());
 		assertEquals(6, hm.numLettersRemaining());
 		assertEquals("_ _ O _ _ _ _ O _ ", hm.displayGameState());
		assertEquals(hm.lettersGuessed().get(0).charValue(), 'O');
 		assertFalse(hm.gameOver());
 	}
 
 	@Test
 	public void testTwoCorrectGuesses() {
 		// make two correct guesses and see if everything is updated
 		boolean correct = hm.makeGuess('S');
 		assertTrue(correct);
 		correct = hm.makeGuess('P');
 		assertTrue(correct);
 
 		assertEquals(8, hm.numGuessesRemaining());
 		assertEquals(5, hm.numLettersRemaining());
 		assertEquals("S P _ _ _ _ _ _ _ ", hm.displayGameState());
 		assertEquals(hm.lettersGuessed().get(0).charValue(), 'S');
 		assertEquals(hm.lettersGuessed().get(1).charValue(), 'P');
 		assertFalse(hm.gameOver());
 	}
 	
 	@Test
 	public void testIncorrectGuess() {
 		// make an incorrect guess and see if everything is updated
 		boolean correct = hm.makeGuess('K');
 		assertFalse(correct);
 
 		assertEquals(7, hm.numGuessesRemaining());
 		assertEquals(7, hm.numLettersRemaining());
 		assertEquals("_ _ _ _ _ _ _ _ _ ", hm.displayGameState());
		assertEquals(hm.lettersGuessed().get(0).charValue(), 'K');
 		assertFalse(hm.gameOver());
 	}
 	
 	@Test
 	public void testTwoIncorrectGuesses() {
 		// make two incorrect guesses and see if everything is updated
 		boolean correct = hm.makeGuess('K');
 		assertFalse(correct);
 		correct = hm.makeGuess('T');
 		assertFalse(correct);
 
 		assertEquals(6, hm.numGuessesRemaining());
 		assertEquals(7, hm.numLettersRemaining());
 		assertEquals("_ _ _ _ _ _ _ _ _ ", hm.displayGameState());
 		assertEquals(hm.lettersGuessed().get(0).charValue(), 'K');
 		assertEquals(hm.lettersGuessed().get(1).charValue(), 'T');
 		assertFalse(hm.gameOver());
 	}
 	
 	@Test
 	public void testCorrectAndIncorrectGuesses() {
 		// make correct and incorrect guesses and see if everything is updated
 		boolean correct = hm.makeGuess('S');
 		assertTrue(correct);
 		correct = hm.makeGuess('T');
 		assertFalse(correct);
 		correct = hm.makeGuess('P');
 		assertTrue(correct);
 		correct = hm.makeGuess('K');
 		assertFalse(correct);
 
 		assertEquals(6, hm.numGuessesRemaining());
 		assertEquals(5, hm.numLettersRemaining());
 		assertEquals("S P _ _ _ _ _ _ _ ", hm.displayGameState());
 		assertEquals(hm.lettersGuessed().get(0).charValue(), 'S');
 		assertEquals(hm.lettersGuessed().get(1).charValue(), 'T');
 		assertEquals(hm.lettersGuessed().get(2).charValue(), 'P');
 		assertEquals(hm.lettersGuessed().get(3).charValue(), 'K');
 		assertFalse(hm.gameOver());
 	}
 	
 	@Test
 	public void testIllegalCharGuess() {
 		boolean correct = hm.makeGuess('?');
 		assertFalse(correct);
 		assertEquals(8, hm.numGuessesRemaining());
 		assertEquals("_ _ _ _ _ _ _ _ _ ", hm.displayGameState());
 		assertEquals(hm.lettersGuessed().isEmpty(), true);
 		assertFalse(hm.gameOver());
 	}
 
 	@Test
 	public void testMultipleCharGuess() {
 		boolean correct = hm.makeGuess('S');
 		assertTrue(correct);
 		correct = hm.makeGuess('S');
 		assertFalse(correct);
 		
 		assertEquals(8, hm.numGuessesRemaining());
 		assertEquals("S _ _ _ _ _ _ _ _ ", hm.displayGameState());
 		assertEquals(hm.lettersGuessed().get(0).charValue(), 'S');
 		assertFalse(hm.gameOver());
 	}
 
 	
 	@Test
 	public void testWin() {
 		// correctly guess the word and see if the game ends
 		hm.makeGuess('S');
 		hm.makeGuess('P');
 		hm.makeGuess('O');
 		hm.makeGuess('N');
 		hm.makeGuess('G');
 		hm.makeGuess('E');
 		hm.makeGuess('B');
 
 		assertEquals("S P O N G E B O B ", hm.displayGameState());
 		assertTrue(hm.gameOver());
 		assertTrue(hm.isWin());
 	}
 
 	@Test
 	public void testLoss() {
 		// use up all guesses and see if game ends
 		hm.makeGuess('A');
 		hm.makeGuess('C');
 		hm.makeGuess('D');
 		hm.makeGuess('F');
 		hm.makeGuess('H');
 		hm.makeGuess('I');
 		hm.makeGuess('J');
 		hm.makeGuess('K');
 
 		assertTrue(hm.gameOver());
 		assertFalse(hm.isWin());
 	}
 }
