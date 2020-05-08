 import static org.junit.Assert.*;
 
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import org.junit.Test;
 
 
 public class BattleshipWeek4Tests {
 
 	//The ship lengths will be displayed on the placeShipScreen
 	//NOT COMPLETED
 	
 	//The user can place ships in order of lengths given
 	//COMPLETE
 	
 	//The user can click on a cell to add a ship
 	//COMPLETE
 	
 	//The ships appear on the grid when added
 	//COMPLETE
 	
 	//A right click places a ship horizontally
 	//IGNORED
 	
 	//A left click places a ship vertically
 	//IGNORED
 	
 	//When play is clicked a list of the ships is created and passed to checkAndPlaceShips
 	//COMPLETE
 	
 	//If the ships are incorrectly placed, then it tells the user to try again from the beginning
 	//COMPLETE
 	
 	//If placed correctly, then the boardscreen appears
 	//COMPLETE
 	
 	//The users placed ships appear on the bottom screen
 	//COMPLETE
 	
 	//AI ships do not appear on the top screen,but exist on the gameboard
 	//COMPLETE
 	
 	//A message is displayed on screen telling which turn it is
 	//NOT COMPLETE
 	
 	//On their turn the user can click on a cell to shoot once
 	//COMPLETE
 	
 	//Shooting a cell calls shoot on the top grid
 	//COMPLETE
 	
 	//Cells are re-displayed after every shot
 	//COMPLETE
 	
 	//The AI shoots slowly enough to be realistic
 	//NOT COMPLETE
 	
 	//After the AI shoots, it is the player's turn again
 	//COMPLETE
 	
 	//When a shot results in an empty grid, a winning screen is displayed
 	//COMPLETE
 	
 	//The user may end or go back to StartScreen
 	//COMPLETE
 	
 	//A message is displayed when a ship is sunk
 	//COMPLETE
 	
 	//The player may go again upon sinking a ship
 	//COMPLETE
 	
 			
 	Locale englishLocale = new Locale("en", "US");
 	Locale germanLocale = new Locale("de", "DE");
 	ResourceBundle englishBundle = GameStarter.setupBundle(englishLocale);
 	ResourceBundle germanBundle = GameStarter.setupBundle(germanLocale);
 	
 	@Test
 	public void testNewGermanText() {
 		assertEquals("Ende-Bildschirm", germanBundle.getString("endScreen"));
		assertEquals("Spielen Wieder", germanBundle.getString("playAgain"));
		assertEquals("Verlassen", germanBundle.getString("endGame"));
		assertEquals("Sie sank ein Schiff!\nSchieBen wieder", germanBundle.getString("sunkShipText"));
 		assertEquals("Sie sank ein Schiff", germanBundle.getString("sunkShipTitle"));
 		assertEquals("Sie schossen dort bereits", germanBundle.getString("overkillTitle"));
 		assertEquals("SchieBen woanders", germanBundle.getString("overkillText"));
 		assertEquals("Platzierungsfe hler", germanBundle.getString("placeErrorTitle"));
 		assertEquals("Falsche platzierung\nVersuchen Sie es erneut", germanBundle.getString("placeErrorText"));
		assertEquals("Glckwnsche!\nSie sind siegreich!", germanBundle.getString("winMessage"));
		assertEquals("Sie wurden besiegt!\nMehr glck beim nchsten mal!", germanBundle.getString("lossMessage"));
 	}
 	
 	@Test
 	public void testNewEnglishText() {
 		assertEquals("End Screen", englishBundle.getString("endScreen"));
 		assertEquals("Play Again", englishBundle.getString("playAgain"));
 		assertEquals("Exit", englishBundle.getString("endGame"));
		assertEquals("You sunk a ship!\nShoot Again!", englishBundle.getString("sunkShipText"));
 		assertEquals("You sunk a ship!", englishBundle.getString("sunkShipTitle"));
 		assertEquals("You already shot there", englishBundle.getString("overkillTitle"));
 		assertEquals("Shoot somewhere else", englishBundle.getString("overkillText"));
 		assertEquals("Placement Error", englishBundle.getString("placeErrorTitle"));
		assertEquals("Improper Placement\nTry again", englishBundle.getString("placeErrorText"));
		assertEquals("Congratulations!\nYou are victorious!", englishBundle.getString("winMessage"));
		assertEquals("You were defeated!\nBetter luck next time!", englishBundle.getString("lossMessage"));
 	}
 	
 	
 }
