 package jp.gr.java_conf.konkonlab.game_of_life.models.test;
 
 import jp.gr.java_conf.konkonlab.game_of_life.models.Cell;
 import junit.framework.TestCase;
 
 public class CellTest extends TestCase {
 
 	Cell deadCell;
 	Cell aliveCell;
 	
 	@Override
 	protected void setUp() throws Exception {
 		deadCell = Cell.createDeadCell();
 		aliveCell = Cell.createAliveCell();
 		super.setUp();
 	}
 	
 	public void testInitAlive() throws Exception {
 		assertTrue(aliveCell.isAlive());
 	}
 
 	public void testInitDead() throws Exception {
 		assertFalse(deadCell.isAlive());
 	}
 	
 	public void testGroupArive() throws Exception {
 		aliveCell.setGroup(1);
 		assertEquals(1, aliveCell.getGroup());
 	}
 	
 	public void testGroupDead() throws Exception {
 		deadCell.setGroup(1); // ignored
 		assertEquals(-1, deadCell.getGroup());
 	}
 	
 	public void testCreateNextGenerationStillDead() throws Exception {
 		Cell nextGenCell = deadCell.createNextGeneration(2);
 		assertFalse(nextGenCell.isAlive());
 	}
 	
 	public void testCreateNextGenerationBirth() throws Exception {
 		Cell nextGenCell = deadCell.createNextGeneration(3);
 		assertTrue(nextGenCell.isAlive());
 	}
 	
 	public void testCreateNextGenerationExist() throws Exception {
 		Cell nextGenCell = aliveCell.createNextGeneration(3);
 		assertTrue(nextGenCell.isAlive());
 	}
 
 	public void testCreateNextGenerationTooFew() throws Exception {
 		Cell nextGenCell = aliveCell.createNextGeneration(1);
 		assertFalse(nextGenCell.isAlive());
 	}
 
 	public void testCreateNextGenerationTooMany() throws Exception {
 		Cell nextGenCell = aliveCell.createNextGeneration(4);
 		assertFalse(nextGenCell.isAlive());
 	}
 	
 	public void test0YearsOld() throws Exception {
 		assertEquals(0, aliveCell.getAge() );
 	}
 	
 	public void test1YearsOld() throws Exception {
 		Cell oneYearsOldCell = aliveCell.createNextGeneration(3);
 		assertEquals(1, oneYearsOldCell.getAge() );
 	}
 	
 	public void test2YearsOld() throws Exception {
 		Cell twoYearsOldCell = aliveCell.createNextGeneration(3).createNextGeneration(2);
 		assertEquals(2, twoYearsOldCell.getAge() );
 	}
 	
 	public void testCalcGroupBirth() throws Exception {
 		aliveCell.decideGroup(3);
 		assertEquals(0, aliveCell.getGroup());
 	}
 
 	public void testCalcGroupNormal() throws Exception {
 		Cell oneYearsOldCell = aliveCell.createNextGeneration(3);
 		oneYearsOldCell.decideGroup(3);
 		assertEquals(1, oneYearsOldCell.getGroup());
 	}
 
 	public void testCalcGroupAdalt() throws Exception {
 		Cell fiveYearsOldCell = aliveCell;
 		for(int i = 0; i < 5; i++) {
 			fiveYearsOldCell = fiveYearsOldCell.createNextGeneration(3);
 		}
 		fiveYearsOldCell.decideGroup(3);
 		assertEquals(2, fiveYearsOldCell.getGroup());
 	}
 
 	public void testCalcGroupDying() throws Exception {
 		Cell dyingCell = aliveCell.createNextGeneration(3);
 		dyingCell.decideGroup(0);
 		assertEquals(3, dyingCell.getGroup());
 	}
 
 }
