 package se.chalmers.tda367.std.tests;
 
import static org.junit.Assert.*;

 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import se.chalmers.tda367.std.core.DynamicLoader;
 import se.chalmers.tda367.std.core.Player;
 import se.chalmers.tda367.std.core.Wave;
 import se.chalmers.tda367.std.core.WaveItem;
 import se.chalmers.tda367.std.core.exported.BasicEnemy;
 import se.chalmers.tda367.std.core.factories.GameBoardFactory;
 import se.chalmers.tda367.std.utilities.SpriteCreator;
 import se.chalmers.tda367.std.core.*;
 
 /**
  * Unit test for {@link se.chalmers.tda367.std.core.WaveController}.
  * Remember to change WaveController to public to be able to test it
  * @author Emil Johansson
  * @date   27 mar 2012
  */
 //TODO more extensive test
 public class TestWaveController {
 	// To test the wave-controller, change access level from package-private to public.
 
 	private Wave createBasicWave(int n){
 		ConcurrentLinkedQueue<WaveItem> q = new ConcurrentLinkedQueue<WaveItem>();	
 		for(int i = 0; i<n; i++){
 			q.add(new WaveItem(DynamicLoader.createInstance(BasicEnemy.class), i+1));
 		}
 		return new Wave(q);
 	}
 	private GameBoardFactory gameBoardFactory = new GameBoardFactory();
 	private Player player = new Player();
 	private WaveController wCont;
 	private IGameBoard gameBoard;
 
 	@Before
 	public void before(){
 		SpriteCreator.setNativeSpriteClass(NativeDummySprite.class);
 		gameBoard = gameBoardFactory.create(1);
 		wCont = new WaveController(gameBoard, player); 
 	}
 	@Test
 	public void testStartWave() throws Exception {
 		Wave w = createBasicWave(3);
 		wCont.startWave(w);
 	}
 }
