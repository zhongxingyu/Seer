 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 
 public class BattleshipWeek3Tests {
 
 	@Test
 	public void testIsGridEmpty() {
 		Grid g = new Grid(10,10);
 		Assert.assertTrue(g.isEmpty());
 	}
 	
 	@Test
 	public void testIsGameBoardEmpty(){
 		GameBoard gb = new GameBoard(10,10,5);
 		Assert.asserTrue(gb.isEmpty());
 	}
 
 	@Test
 	public void testThatGameBoardIsNotEmptyAfterAddingShips(){
 		GameBoard gb = new GameBoard(10,10,2);
 		ArrayList<Ship> ships = new ArrayList<Ship>();
 		ships.add(new Ship(3,3,3,true));
 		ships.add(new Ship(4,6,2,true);
 		gb.checkAndPlaceShips(ships);
 		Assert.assertFalse(gb.isEmpty);
 	}
 	
 	@Test
 	public void testThatMakesSureShipsCanBeAddedUsingMultipleCallsToCheckAndPlaceShips(){
 		GameBoard gb = new GameBoard(10,10,2);
 		ArrayList<Ship> ships = new ArrayList<Ship>();
 		ships.add(new Ship(3,3,3,true));
 		Assert.assertTrue(gb.checkAndPlaceShips(ships));
 		ships.removeAll(ships);
 		ships.add(new Ship(4,6,2,true));
 		Assert.assertTrue(gb.checkAndPlaceShips(ships));
 	}
 	
 	@Test
 	public void testThatMakesSureMoreShipsThanAllowedCanNotBePlaced(){
 		GameBoard gb = new GameBoard(10,10,2);
 		ArrayList<Ship> ships = new ArrayList<Ship>();
 		ships.add(new Ship(3,3,3,true));
 		ships.add(new Ship(4,6,2,true));
 		Assert.assertTrue(gb.checkAndPlaceShips(ships));
 		ships.removeAll(ships);
 		ships.add(new Ship(1,1,5,false));
 		Assert.assertFalse(gb.checkAndPlaceShips(ships));
 	}
 	
 	@Test
 	public void testThatDefaultConstructorForGameBoardInitializesCorrectly(){
 		GameBoard gb = new GameBoard();
 		Assert.assertEquals(10, gb.getWidth());
 		Assert.assertEquals(10,gb.getHeight());
 		Assert.assertEquals(5, gb.getNumberOfShips());
 	}
 	
 	@Test
 	public void testThatAIConstructsProperly(){
 		GameBoard gb = new GameBoard();
 		AI ai = new AI(gb);
 		Assert.assertNotNull(ai);
 	}
 	
 	@Test
 	public void testThatAIPlacesShips(){
 		GameBoard gb = new GameBoard();
 		AI ai = new AI(gb);
 		ai.placeShips();
 		Boolean result = false;
 		
 		for(int r = 0; r < gb.getHeight(); r++){
 			for(int c = 0; c < gb.getWidth(); c++){
 				result = result || !(gb.getTopGrid()[r][c] instanceof ShipCell); 
 			}
 		}
 		
 		Assert.assertTrue(result);
 	}
 	
 	@Test
 	public void testThatAIShoots(){
 		GameBoard gb = new GameBoard();
 		ArrayList<Ship> ships = new ArrayList<Ship>();
 		ships.add(new Ship(3,3,3,true));
 		ships.add(new Ship(4,6,2,true));
 		ships.add(new Ship(1,1,5,false));
 		gb.checkAndPlaceShips(ships);
 		AI ai = new AI(gb);
 		ai.placeShips();
 		Boolean result = false;
 		
 		for(int r = 0; r < gb.getHeight(); r++){
 			for(int c = 0; c < gb.getWidth(); c++){
 				result = result || (gb.getBottomGrid()[r][c] instanceof Hit) || (gb.getBottomGrid()[r][c] instanceof Miss); 
 			}
 		}
 		
 		Assert.assertTrue(result);
 	}
 	
 	Locale englishLocale = new Locale("en", "US");
 	Locale germanLocale = new Locale("de", "DE");
 	ResourceBundle englishBundle = GameStarter.setupBundle(englishLocale);
 	ResourceBundle germanBundle = GameStarter.setupBundle(germanLocale);
 	
 	@Test
 	public void testEnglishTitles() {
 		assertEquals("Start Screen", englishBundle.getString("startScreen"));
 		assertEquals("Setup Screen ", englishBundle.getString("setupScreen"));
 		assertEquals("Board Screen", englishBundle.getString("boardScreen"));
 		assertEquals("BATTLESHIP", englishBundle.getString("battleship"));
 	}
 	
 	@Test
 	public void testEnglishButtonText() {
 		assertEquals("Start", englishBundle.getString("start"));
 		assertEquals("Back", englishBundle.getString("back"));
 		assertEquals("Next", englishBundle.getString("next"));
 		assertEquals("Play", englishBundle.getString("play"));
 		// Added after initial commit
 		assertEquals("OK", englishBundle.getString("ok"));
 	}
 	
 	@Test
 	public void testEnglishLabelText() {
 		assertEquals("Enter your board size: (Width, Height) ", englishBundle.getString("enterBoardSize"));
 		assertEquals("Enter the number of ships: ", englishBundle.getString("enterNumShips"));
 		assertEquals("Length of Ship ", englishBundle.getString("shipLength"));
 	}
 	
	@Test
 	public void testEnglishMenuText() {
 		assertEquals("Edit", englishBundle.getString("edit"));
 		assertEquals("Change Language", englishBundle.getString("changeLanguage"));
 		assertEquals("English", englishBundle.getString("english"));
 		assertEquals("Change the application's language to English", englishBundle.getString("changeToEnglish"));
 		assertEquals("German", englishBundle.getString("german"));
 		assertEquals("Change the application's language to German", englishBundle.getString("changeToGerman"));
 		assertEquals("Help", englishBundle.getString("help"));
 		assertEquals("About", englishBundle.getString("about"));
 		// Added after commit
 		assertEquals("How to play", englishBundle.getString("howToPlay"));
 		assertEquals("Goal", englishBundle.getString("goal"));
 		assertEquals("Setup", englishBundle.getString("setup"));
 		assertEquals("Play", englishBundle.getString("play"));
 		assertEquals("Victory", englishBundle.getString("victory"));
 	}
 	
 	@Test
 	public void testEnglishPopupText() {
 		assertEquals("<b>Goal:</b> To sink all of your opponent's ships by correctly guessing their location.<br>", englishBundle.getString("aboutGoal"));
 		assertEquals("<b>Setup:</b> Each player has a board with two girds and a set number of ships each with a specified size. The top grid is used to track your shots at the opponent's ships. The bottom grid is used to place your ships vertically or horizontally (not diagonally) across grid spaces, and cannot hang over the grid. Ships can touch each other, but can't both be on the same space. The bottom grid is also used to track the opponent's shots at your ships.<br>", englishBundle.getString("aboutSetup"));
 		assertEquals("<b>Play:</b> Players take turns firing a shot to attack enemy ships. On your turn, left-click on a space to shoot there. If there is no ship there, it will be a 'miss' and marked white. If there is a ship, it will be a 'hit' and marked red. The markings are the same when your opponent shoots at your ships. When every space of a ship is marked red, the ship is sunk.<br>", englishBundle.getString("aboutPlay"));
 		assertEquals("<b>Victory:</b> The first player to sink all of the other player's ships wins.", englishBundle.getString("aboutVictory"));
 		assertEquals("This Battleship application was created by Chris Hoorn, Cody Plungis, and Tiffany Pohl.", englishBundle.getString("applicationInfo"));
 	}
 	
 	@Test
 	public void testGermanTitles() {
 		assertEquals("Stellen Sie Schirm an", germanBundle.getString("startScreen"));
 		assertEquals("Einstellungs-Schirm ", germanBundle.getString("setupScreen"));
 		assertEquals("Brett-Schirm", germanBundle.getString("boardScreen"));
 		assertEquals("LINIENSCHIFF", germanBundle.getString("battleship"));
 	}
 	
 	@Test
 	public void testGermanButtonText() {
 		assertEquals("Anfang", germanBundle.getString("start"));
 		assertEquals("Rckseite", germanBundle.getString("back"));
 		assertEquals("Zunchst", germanBundle.getString("next"));
 		assertEquals("Spiel", germanBundle.getString("play"));
 	}
 	
 	@Test
 	public void testGermanLabelText() {
 		assertEquals("Tragen Sie Ihre Brettgre ein: (Breite, Hhe) ", germanBundle.getString("enterBoardSize"));
 		assertEquals("Geben Sie die Zahl Schiffen ein: ", germanBundle.getString("enterNumShips"));
 		assertEquals("Lnge von Schiff ", germanBundle.getString("shipLength"));
 	}
 	
 	@Test
 	public void testGermanMenuText() {
 		assertEquals("Redigieren Sie", germanBundle.getString("edit"));
 		assertEquals("ndern Sie Sprache", germanBundle.getString("changeLanguage"));
 		assertEquals("Englisch", germanBundle.getString("english"));
 		assertEquals("ndern Sie die Sprache der Anwendung zu Englisch", germanBundle.getString("changeToEnglish"));
 		assertEquals("Deutsch", germanBundle.getString("german"));
 		assertEquals("ndern Sie die Sprache der Anwendung zum Deutschen", germanBundle.getString("changeToGerman"));
 		assertEquals("Hilfe", germanBundle.getString("help"));
 		assertEquals("ber", germanBundle.getString("about"));
 		// Added after commit
 		assertEquals("Wie man spielt", germanBundle.getString("howToPlay"));
 		assertEquals("Ziel", germanBundle.getString("goal"));
 		assertEquals("Einstellung", germanBundle.getString("setup"));
 		assertEquals("Spiel", englishBundle.getString("play"));
 		assertEquals("Sieg", germanBundle.getString("victory"));
 	}
 	
 	@Test
 	public void testGermanPopupText() {
 		assertEquals("<b>Ziel:</b> Zu alle Schiffe des Konkurrenten sinken, durch ihre Position richtig schtzen.<br>", germanBundle.getString("aboutGoal"));
 		assertEquals("<b>Einstellung:</b> Jeder Spieler hat ein Brett mit zwei Sticheleien, eine Satzanzahl von Schiffen jede mit einer spezifizierten Gre. Das Spitzenrasterfeld wird verwendet, um Ihre Schsse an den Schiffen des Konkurrenten aufzuspren. Das untere Rasterfeld wird verwendet, um Ihre Schiffe (nicht diagonal) ber Rasterfeldrume vertikal oder horizontal zu setzen und kann nicht ber dem Rasterfeld hngen. Schiffe knnen sich berhren, aber knnen nicht beide auf dem gleichen Raum sein. Das untere Rasterfeld wird auch verwendet, um die Schsse des Konkurrenten an Ihren Schiffen aufzuspren.<br>", germanBundle.getString("aboutSetup"));
 		assertEquals("<b>Spiel:</b> Die Spieler nehmen Umdrehungen einen Schu abfeuernd zu den feindlichen Schiffen des Angriffs. Auf Ihrer Umdrehung link-klicken Sie auf einem Raum, um dort zu schieen. Wenn es kein Schiff dort gibt, ist es ein Verlust und ein signifikantes Wei. Wenn es ein Schiff gibt, ist es ein Schlag und ein signifikantes Rot. Die Markierungen sind die selben wenn Ihre Konkurrenteneintragfden an Ihren Schiffen. Wenn jeder Raum eines Schiffs signifikantes Rot ist, wird das Schiff gesunken.<br>", germanBundle.getString("aboutPlay"));
 		assertEquals("<b>Sieg:</b> Der erste Spieler, zum aller Schiffe des anderen Spielers zu sinken gewinnt.", germanBundle.getString("aboutVictory"));
 		assertEquals("Diese Linienschiffanwendung wurde durch Chris Hoorn, Cody Plungis und Tiffany Pohl verursacht.", germanBundle.getString("applicationInfo"));
 	}
 	
 	/**
 	 * Added after original commit of Week 3 test cases
 	 */
 	@Test
 	public void testThatBundleUpdatesCorrectly() {
 		ResourceBundle currentBundle = GameStarter.setupBundle(englishLocale);
 		assertEquals(englishBundle, currentBundle);
 		GameStarter.updateBundle(germanBundle);
 		assertEquals(GameStarter.bundle, germanBundle);
 	}
 }
