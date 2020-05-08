 package test.usecase;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import exception.InvalidDimensionException;
 import exception.InvalidFileException;
 import exception.InvalidNbPlayersException;
 import game.CaptureTheFlag;
 import game.Game;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import org.junit.Test;
 
 import controller.GameController;
 
 /**
  * @author Maxim
  *
  */
 public class ChooseCaptureTheFlagModeTest {
 	
 	private Game game;
 	private GameController gc = new GameController();
 
 	@Test
 	public void testChooseValidNbOfPlayers() throws FileNotFoundException, InvalidFileException, InvalidDimensionException {	
 		try {
			gc.startNewCTFFromFile(new File("src/test/usecase/validfile_bigCTFfield.grid"),9);
 		} catch (InvalidNbPlayersException e) {
 			fail("the number of players is valid");
 		}
 		assertEquals(10, game.getGrid().getDimension().getX());
 		assertEquals(10, game.getGrid().getDimension().getY());
 		assertTrue(game.getMode() instanceof CaptureTheFlag);
 	}
 	
 	
 	@Test
 	public void testChooseInvalidNbOfPlayers() {
 		//more players than positions on the grid
 		try {
 			gc.startNewCTFFromFile(new File("src/test/usecase/validfile_bigCTFfield.grid"),10);
 			fail("cannot happen");
 		} catch (FileNotFoundException | InvalidFileException
 				| InvalidDimensionException e) {
 			fail("cannot happen");
 		} catch (InvalidNbPlayersException e) {
 			//InvalidNbPlayersException thrown
 		}
 	}
 
 }
