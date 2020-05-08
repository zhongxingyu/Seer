 package tests;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import com.example.model.ArrayGameModel;
 import com.example.model.Boat;
 import com.example.model.Direction;
 import com.example.model.ModelStage;
 import com.example.model.ModelTurn;
 import com.example.model.Position;
 
 public class ModelTest extends TestCase {
 
 	public void testShouldBePlayer1sTurnAndPlaceBoatStageWhenStartingNewGame() throws Throwable {
         ArrayGameModel model = new ArrayGameModel();
         assertEquals(ModelTurn.PLAYER1, model.getTurn());
         assertEquals(ModelStage.PLACE_BOATS, model.getStage());   
     }
 	
 	public void testAllBoatsShouldBeCreatedInTheStart() throws Throwable {
 		ArrayGameModel model = new ArrayGameModel();
 	}
 	
 	public void testThereShouldBeNoBoatsPlacedInTheStart() throws Throwable {
 		ArrayGameModel model = new ArrayGameModel();
 		for (Boat boat : model.getBoats()) {
 			assertSame(null, boat.getPosition());
 		}
 	}
 	
 	// TODO This test is not finished.
 	public void testPlacingABoatShouldPlaceABoatInTheModel() throws Throwable {
         ArrayGameModel model = new ArrayGameModel();
         Position pos = new Position(5, 'e');
        model.gameboardPressed(pos, Direction.RIGHT);
     }
 }
