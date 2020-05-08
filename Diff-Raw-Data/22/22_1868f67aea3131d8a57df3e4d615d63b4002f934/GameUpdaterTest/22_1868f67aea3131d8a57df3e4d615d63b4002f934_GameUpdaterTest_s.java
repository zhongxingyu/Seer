 package bombgame.controller.gamehandler.impl;
 
 import bombgame.controller.ai.impl.ManAI;
 import bombgame.controller.player.impl.Player;
 import bombgame.entities.impl.Bomb;
 import bombgame.entities.impl.Explosion;
 import bombgame.entities.impl.Man;
 import junit.framework.TestCase;
 
 public final class GameUpdaterTest extends TestCase {
 	
 	GameUpdater gu;
 	GameHandler gh;
 
 	public void setUp() {
		gh = new GameHandler(new Player(new Man(0,0)));
 		gu = new GameUpdater(gh);
 	}
 	
 	public void testGetHandler() {
 		assertEquals(gu.getHandler(), gh);
 	}
 	
 	public void testPlaceBombs() {
 		Man m = new Man(0, 0);
 		m.setPlaceBomb(true);
 		gh.addObject(new Man(1, 1));
 		gh.addObject(m);
 		gu.placeBombs();
 		assertEquals(gh.getBombs().size(), 1);
 	}
 	
 	public void testUpdateBombs() {
 		Bomb b = new Bomb(1, 1);
 		gh.addObject(b);
 		
 		while(b.getTimer() > 0) {
 			gu.updateBombs();
 		}
 		gu.updateBombs();
 		
 		assertEquals(gh.getBombs().size(), 0);
 		assertEquals(gh.getExplosionList().size(), 1);
 	}
 	
 	public void testUpdateExplosion() {
 		Explosion exp = new Explosion(1, 1);
 		gh.addObject(exp);
 		
 		while(exp.getTimer() > 0) {
 			gu.updateExplosion();
 		}
 		
 		assertEquals(gh.getExplosionList().size(), 0);
 	}
 	
 	public void testUpdateMen() {
 		Man man = new Man(0, 0);
 		gh.addObject(man);
 		
 		gu.updateMen();
 		// Check moveMan -> no dir so do nothing
 		// is checked in TestcalcTest
 		
 		Explosion expl = new Explosion(0, 0);
 		gh.addObject(expl);
 		gu.updateMen();
 		assertEquals(gh.getMen().size(), 0);
 	}
 	
 	public void testCheckHit() {
 		Man man = new Man(0, 0);
 		assertFalse(gu.checkHit(man));
 		
 		Explosion expl = new Explosion(0, 0);
 		gh.addObject(expl);
 		assertTrue(gu.checkHit(man));
 	}
 	
 	public void testUpdateAIs() {
 			gh.addAI(new ManAI(new Man(9,9),gh));
 			gu.updateAIs();
 	}
 	
 }
