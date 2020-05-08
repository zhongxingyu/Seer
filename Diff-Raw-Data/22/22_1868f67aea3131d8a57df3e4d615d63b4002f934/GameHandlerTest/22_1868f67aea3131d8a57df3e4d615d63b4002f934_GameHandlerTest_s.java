 package bombgame.controller.gamehandler.impl;
 
 import bombgame.controller.ai.impl.ManAI;
 import bombgame.controller.player.impl.Player;
 import bombgame.entities.impl.Bomb;
 import bombgame.entities.impl.Explosion;
 import bombgame.entities.impl.GameObject;
 import bombgame.entities.impl.Man;
 import bombgame.entities.impl.Wall;
 import junit.framework.TestCase;
 
 public final class GameHandlerTest extends TestCase {
 
 	GameHandler gh1;
 	GameHandler gh2;
 	GameHandler gh3;
 	GameHandler gh4;
 	GameHandler gh5;
 	
 	public void setUp() {
 		gh1 = new GameHandler(new GameObject[10][10]);
 		gh2 = new GameHandler(new GameObject[2][3]);
		gh3 = new GameHandler(2, 3, new Player(new Man(0,0)));
		gh4 = new GameHandler(new Player(new Man(0,0)));
 		gh5 = new GameHandler(new GameObject[3][3]);
 	}
 	
 	public void testAddObject() {
 		GameObject o1 = new Wall(3,4);
 		gh1.addObject(o1);
 		assertSame(o1, gh1.getField()[3][4]);
 		
 		GameObject o2 = new Man(2,7);
 		gh1.addObject(o2);
 		assertSame(o2, gh1.getField()[2][7]);
 		
 		GameObject o3 = new Bomb(1,8);
 		gh1.addObject(o3);
 		assertSame(o3, gh1.getField()[1][8]);
 		
 		
 		GameObject o4 = new Man(3,4);
 		gh1.addObject(o4);
 		assertSame(o4, gh1.getField()[4][4]);
 		
 		gh1.addObject(new Wall(9,9));
 		
 		GameObject o5 = new Man(9,9);
 		gh1.addObject(o5);
 		assertSame(o5, gh1.getField()[8][9]);
 		
 		gh1.addObject(new Wall(9,5));
 		gh1.addObject(new Wall(8,5));
 		
 		GameObject o6 = new Man(9,5);
 		gh1.addObject(o6);
 		assertSame(o6, gh1.getField()[9][6]);
 		
 		gh1.addObject(new Wall(0,9));
 		gh1.addObject(new Wall(1,9));
 		
 		GameObject o7 = new Man(0,9);
 		gh1.addObject(o7);
 		assertSame(o7, gh1.getField()[0][8]);
 		//###########################
 		
 		for(int i = 0; i<3; i++) {
 			for(int j = 0; j<3; j++) {
 				gh5.addObject(new Wall(i,j));
 			}
 		}
 		GameObject o9 = new Man(1,1);
 		gh5.addObject(o9);
 		assertSame(o9, gh5.getField()[1][1]);
 		//because (3/4) already used by o1, don't add 
 		//GameObject o4 = new Explosion(3,4);
 		//gh1.addObject(o4);
 		//assertNotSame(o4, gh1.getField()[3][4]);
 		
 		GameObject o27 = new Explosion(2,4);
 		gh1.addObject(o27);
 		assertSame(o27, gh1.getField()[2][4]);
 		
 	}
 	
 	public void testRemoveObject() {
 		GameObject o1 = new Wall(3,4);
 		gh1.addObject(o1);
 		gh1.removeObject(o1);
 		assertNull(gh1.getField()[3][4]);
 		
 		GameObject o2 = new Man(6,3);
 		gh1.addObject(o2);
 		gh1.removeObject(o2);
 		assertNull(gh1.getField()[6][3]);
 		
 		GameObject o3 = new Bomb(2,7);
 		gh1.addObject(o3);
 		gh1.removeObject(o3);
 		assertNull(gh1.getField()[2][7]);
 		
 		GameObject o4 = new Explosion(1,5);
 
 		gh1.addObject(o4);
 		gh1.removeObject(o4);
 		assertNull(gh1.getField()[1][5]);
 		
 		gh1.addObject(new Wall(2,2));
 		gh1.removeObject(new Wall(2,2));
 		assertNotNull(gh1.getField()[2][2]);
 	}
 	
 	// addExplosionList
 	
 	// removeExplositionList
 	
 	// SpawnMan
 	
 	// SetField
 	
 	public void testSetPlayer() {
 		gh1.setPlayer(new Player(new Man(1,1)));
 		assertNotNull(gh1.getPlayer());
 	}
 	
 	public void testAddAI() {
 		gh1.addAI(new ManAI(new Man(2,2), gh1));
 		assertEquals(gh1.getAIs().size(), 1);
 	}
 	
 	public void testGetField() {
 		assertNotNull(gh1.getField());
 	}
 	
 	public void testGetMen() {
 		assertNotNull(gh1.getMen());
 	}
 	
 	public void testGetBombs() {
 		assertNotNull(gh1.getBombs());
 	}
 	
 	// Get player
 	
 	public void testGetExplosionList() {
 		assertNotNull(gh1.getExplosionList());
 	}
 	
 	// Get AIs
 	
 	public void testGetUpdater() {
 		assertNotNull(gh1.getUpdater());
 	}
 	
 	public void testGetCalculator() {
 		assertNotNull(gh1.getCalculator());
 	}
 	
 	public void testGetExplosion() {
 		Explosion exp = new Explosion(3,4);
 		assertNull(gh1.getExplosion(exp));
 		gh1.addObject(exp);
 		assertNotNull(gh1.getExplosion(exp));
 		exp = new Explosion(2,1);
 		assertNull(gh1.getExplosion(exp));
 	}
 	
 	// Update all
 	public void testUpdateAll() {	
 		// Add Men (Player not needed, direction is made manual)
 		Man m1 = new Man(6, 6);
 		m1.setDirection(Man.RIGHT); // Moves right
 		Man m2 = new Man(5, 5);
 		m2.setPlaceBomb(true); // Places bomb
 		Man m3 = new Man(0, 0); // Is destroyed
 		gh1.addObject(m1);
 		gh1.addObject(m2);
 		gh1.addObject(m3);
 		
 		// Add Explosions
 		gh1.addObject(new Explosion(0, 0));
 		
 		gh1.updateAll();
 		assertEquals(gh1.getMen().size(), 2); // destroyed
 		assertEquals(gh1.getField()[7][6], m1); // position
 		assertEquals(gh1.getBombs().size(), 1); // bomb
 	}
 	
 	public void testToString() {
 		
 	}
 }
