 package final_project.tests;
 
 import static org.junit.Assert.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import final_project.model.*;
 import final_project.model.store.*;
 
 /**
  * @author Josh
  *
  */
 public class DERoundTest {
 
 	DERound round1;
 	DERound round2;
 	DERound round3;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		ArrayList<Integer> seeding1 = new ArrayList<Integer>();
 		seeding1.add(80);
 		seeding1.add(70);
 		seeding1.add(50);
 		seeding1.add(90);
 		seeding1.add(40);
 		seeding1.add(39);
 		seeding1.add(60);
 		seeding1.add(20);
 		round1 = new DERound(null, null, seeding1, 0);
 
 		ArrayList<Integer> seeding2 = new ArrayList<Integer>();
 		seeding2.add(10);
 		seeding2.add(15);
 		seeding2.add(17);
 		seeding2.add(42);
 		seeding2.add(73);
 		round2 = new DERound(null, null, seeding2, 0);
 
 		ArrayList<Integer> seeding3 = new ArrayList<Integer>(seeding1);
 		seeding3.add(29);
 		seeding3.add(69);
 		seeding3.add(43);
 		round3 = new DERound(null, null, seeding3, 0);
 	}
 
 
 	@After
 	public void tearDown() {
 		ArrayList<Integer> seeding1 = new ArrayList<Integer>();
 		seeding1.add(80);
 		seeding1.add(70);
 		seeding1.add(50);
 		seeding1.add(90);
 		seeding1.add(40);
 		seeding1.add(39);
 		seeding1.add(60);
 		seeding1.add(20);
 		round1 = new DERound(null, null, seeding1, 0);
 
 		ArrayList<Integer> seeding2 = new ArrayList<Integer>();
 		seeding2.add(10);
 		seeding2.add(15);
 		seeding2.add(17);
 		seeding2.add(42);
 		seeding2.add(73);
 		round2 = new DERound(null, null, seeding2, 0);
 
 		ArrayList<Integer> seeding3 = new ArrayList<Integer>(seeding1);
 		seeding3.add(29);
 		seeding3.add(69);
 		seeding3.add(43);
 		round3 = new DERound(null, null, seeding3, 0);
 	}
 
 	/**
 	 * Test method for {@link final_project.model.DERound#setupRound()}.
 	 */
 	@Test
 	public void testSetupRound() {
 		fail("Not yet implemented");
 	}
 
 	/**
 	 * Test method for {@link final_project.model.DERound#makeCut()}.
 	 */
 	@Test
 	public void testMakeCut() {
 		round1.setCut(0);
 		round1.makeCut();
 		assertEquals(8, round1.getSeeding().size());
 		tearDown();
 
 		round1.setCut(15);
 		round1.makeCut();
 		assertEquals(7, round1.getSeeding().size());
 		tearDown();
 		
 		round1.setCut(20);
 		round1.makeCut();
 		assertEquals(7, round1.getSeeding().size());
 		tearDown();
 		
 		round1.setCut(25);
 		round1.makeCut();
 		assertEquals(6, round1.getSeeding().size());
 		tearDown();
 		
 		round1.setCut(30);
 		round1.makeCut();
 		assertEquals(6, round1.getSeeding().size());
 		tearDown();
 		
 		round2.setCut(15);
 		round2.makeCut();
 		assertEquals(5, round2.getSeeding().size());
 		tearDown();
 		
 		round2.setCut(20);
 		round2.makeCut();
 		assertEquals(4, round2.getSeeding().size());
 		tearDown();
 		
 		round2.setCut(25);
 		round2.makeCut();
 		assertEquals(4, round2.getSeeding().size());
 		tearDown();
 		
 		round2.setCut(30);
 		round2.makeCut();
 		assertEquals(4, round2.getSeeding().size());
 		tearDown();
 		
 		round3.setCut(15);
 		round3.makeCut();
 		assertEquals(10, round3.getSeeding().size());
 		tearDown();
 		
 		round3.setCut(20);
 		round3.makeCut();
 		assertEquals(9, round3.getSeeding().size());
 		tearDown();
 		
 		round3.setCut(25);
 		round3.makeCut();
 		assertEquals(9, round3.getSeeding().size());
 		tearDown();
 		
 		round3.setCut(30);
 		round3.makeCut();
 		assertEquals(8, round3.getSeeding().size());
 	}
 
 	/**
 	 * Test method for {@link final_project.model.DERound#populateBracket()}.
 	 */
 	@Test
 	public void testPopulateBracket() {
 		round1.calcBracketSize();
 		assertEquals(round1.getMatches().length, 7);
 		round2.calcBracketSize();
 		assertEquals(round2.getMatches().length, 7);
 		round3.calcBracketSize();
 		assertEquals(round3.getMatches().length, 15);
 
 		round1.populateBracket();
 		round2.populateBracket();
 		round3.populateBracket();
 		Result[] round1Matches = round1.getMatches();
 		Result[] round2Matches = round2.getMatches();
 		Result[] round3Matches = round3.getMatches();

 		System.out.println(Arrays.deepToString(round2Matches));
 		
 		assertNull(round1Matches[0]);
 		assertNull(round1Matches[1]);
 		assertNull(round1Matches[2]);
 		// These players could be in the opposite order, so it might fail because of that and have to be switched
 		assertEquals(round1Matches[3].getPlayer1(), 80);
 		assertEquals(round1Matches[3].getPlayer2(), 20);
 		assertEquals(round1Matches[4].getPlayer1(), 90);
 		assertEquals(round1Matches[4].getPlayer2(), 40);
 		assertEquals(round1Matches[5].getPlayer1(), 50);
 		assertEquals(round1Matches[5].getPlayer2(), 39);
 		assertEquals(round1Matches[6].getPlayer1(), 70);
 		assertEquals(round1Matches[6].getPlayer2(), 60);
 
 		assertNull(round2Matches[0]);
 		assertNull(round2Matches[1]);
 		// These players could be in the opposite order, so it might fail because of that and have to be switched
 		assertEquals(round2Matches[1].getPlayer1(), 10);
 		assertEquals(round2Matches[1].getPlayer2(), -1);
 		assertEquals(round2Matches[2].getPlayer1(), 17);
 		assertEquals(round2Matches[2].getPlayer2(), 15);
 		assertEquals(round2Matches[3].getPlayer1(), 10);
 		assertEquals(round2Matches[3].getPlayer2(), -1);
 		assertEquals(round2Matches[4].getPlayer1(), 42);
 		assertEquals(round2Matches[4].getPlayer2(), 73);
 		assertEquals(round2Matches[5].getPlayer1(), 17);
 		assertEquals(round2Matches[5].getPlayer2(), -1);
 		assertEquals(round2Matches[6].getPlayer1(), 15);
 		assertEquals(round2Matches[6].getPlayer2(), -1);
 
 		assertNull(round3Matches[0]);
 		assertNull(round3Matches[1]);
 		assertNull(round3Matches[2]);
 		// These players could be in the opposite order, so it might fail because of that and have to be switched
 		assertEquals(round3Matches[3].getPlayer1(), 80);
 		assertEquals(round3Matches[3].getPlayer2(), -1);
 		assertEquals(round3Matches[4].getPlayer1(), 40);
 		assertEquals(round3Matches[4].getPlayer2(), 90);
 		assertEquals(round3Matches[5].getPlayer1(), 50);
 		assertEquals(round3Matches[5].getPlayer2(), -1);
 		assertEquals(round3Matches[6].getPlayer1(), 70);
 		assertEquals(round3Matches[6].getPlayer2(), -1);
 		assertEquals(round3Matches[7].getPlayer1(), 80);
 		assertEquals(round3Matches[7].getPlayer2(), -1);
 		assertEquals(round3Matches[8].getPlayer1(), 29);
 		assertEquals(round3Matches[8].getPlayer2(), 20);
 		assertEquals(round3Matches[9].getPlayer1(), 40);
 		assertEquals(round3Matches[9].getPlayer2(), -1);
 		assertEquals(round3Matches[10].getPlayer1(), 90);
 		assertEquals(round3Matches[10].getPlayer2(), -1);
 		assertEquals(round3Matches[11].getPlayer1(), 50);
 		assertEquals(round3Matches[11].getPlayer2(), -1);
 		assertEquals(round3Matches[12].getPlayer1(), 43);
 		assertEquals(round3Matches[12].getPlayer2(), 30);
 		assertEquals(round3Matches[13].getPlayer1(), 60);
 		assertEquals(round3Matches[13].getPlayer2(), 69);
 		assertEquals(round3Matches[14].getPlayer1(), 70);
 		assertEquals(round3Matches[14].getPlayer2(), -1);
 	}
 }
