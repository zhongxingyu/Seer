 package uw.cse403.minion.test;
 
 import uw.cse403.minion.Ability;
 import uw.cse403.minion.Skill;
 import junit.framework.TestCase;
 
 /**
  * Whitebox test of backend skill object
  * 
  * @author loki
  *
  */
 public class SkillTest extends TestCase {
 
 	/**
 	 * Test two parameter constructor for fatal failure only
 	 */
 	public void testSkillConstructor() {
 		Skill acro = new Skill(-1, 1);
 		Skill climb = new Skill(-1, 4);
 		
 		assertTrue(acro != null);
 		assertTrue(climb != null);
 	}
 
 	/**
 	 * Test three parameter constructor for fatal failure only
 	 */
 	public void testSkillConstructorTitle() {
		Skill acro = new Skill(-1, "Sing", 26);
		Skill climb = new Skill(-1, "Brewer", 27);
 		
 		assertTrue(acro != null);
 		assertTrue(climb != null);
 	}
 
 	/**
 	 * Test get Name
 	 * Depends on two parameter constructor
 	 */
 	public void testGetName() {
 		//Ensure equally spelled/mispelled throughout test
 		String acroName = "Acrobatics";
 		String climbName = "Climb";
 		
 		Skill acro = new Skill(-1, 1);
 		Skill climb = new Skill(-1, 4);
 	
 		assertEquals(acroName, acro.getName());
 		assertEquals(climbName, climb.getName());
 	}
 
 	/**
 	 * Test get rank
 	 * Depends on constructor
 	 */
 	public void testGetRank() {
 		Skill acro = new Skill(-1, 1);
 		assertEquals(0, acro.getRank());
 	}
 	
 	/**
 	 * Test add to skill rank
 	 * Depends on constructor and getRank
 	 */
 	public void testAddToRank() {
 		Skill acro = new Skill(-1, 1);
 		assertEquals(0, acro.getRank());
 		
 		acro.addToRank(1);
 		assertEquals(1, acro.getRank());
 
 		acro.addToRank(3);
 		assertEquals(4, acro.getRank());
 	}
 
 	/**
 	 * Test get modifiers to skill
 	 * Depends on constructor
 	 */
 	public void testGetModifier() {
 		Skill acro = new Skill(-1, 1);
 		assertEquals(0, acro.getModifier("racial"));
 	}
 
 	/**
 	 * Test add a modifier to skill
 	 * Depends on constructor and getModifier
 	 */
 	public void testAddModifier() {
 		Skill acro = new Skill(-1, 1);
 		assertEquals(0, acro.getModifier("racial"));
 	
 		acro.addModifier("racial", 8);
 		assertEquals(8, acro.getModifier("racial"));
 		
 		acro.addModifier("magic", 2);
 		assertEquals(8, acro.getModifier("racial"));
 		assertEquals(2, acro.getModifier("magic"));
 	}
 	
 	/**
 	 * Test remove a modifier to a skill
 	 * Depends on constructor, getModifier and addModifier
 	 */
 	public void testRemoveModifier() {
 		Skill acro = new Skill(-1, 1);
 		assertEquals(0, acro.getModifier("racial"));
 		acro.removeModifier("racial");
 		assertEquals(0, acro.getModifier("racial"));
 	
 		acro.addModifier("racial", 8);
 		assertEquals(8, acro.getModifier("racial"));
 		
 		acro.removeModifier("racial");
 		assertEquals(0, acro.getModifier("racial"));
 	}
 
 	/**
 	 * Test get total skill bonus
 	 * Depends on constructor, addModifier and Ability class
 	 */
 	public void testGetBonus() {
 		Skill acro = new Skill(-1, 1);
 		Skill climb = new Skill(-1, 4);
 		
 		acro.addToRank(4);
 
 		Ability dex = new Ability(-1, 1);
 		dex.setBase(12);
 		Ability str = new Ability(-1, 0);
 		str.setBase(8);
 		
 		assertEquals(8, acro.getBonus(dex));
 		assertEquals(-1, climb.getBonus(str));
 		
 		acro.addModifier("racial", 4);
 		acro.addModifier("flaw", -8);
 		assertEquals(4, acro.getBonus(dex));
 	}
 
 	public void testWriteToDB() {
 		//TODO: Test Database
 	}
 
 }
