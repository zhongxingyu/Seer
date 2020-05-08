 package de.htwg.wzzrd.model;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import resources.TestDataProvider;
 import de.htwg.wzzrd.model.LightTableInterface.GameIsFinished;
 import de.htwg.wzzrd.model.LightTableInterface.PlayerAlreadyLaidCard;
 import de.htwg.wzzrd.model.LightTableInterface.RoundAlreadyFinished;
 import de.htwg.wzzrd.model.LightTableInterface.RoundNotFinished;
 import de.htwg.wzzrd.model.LightTableInterface.SubRoundAlreadyFinished;
 import de.htwg.wzzrd.model.LightTableInterface.SubRoundNotFinished;
 import de.htwg.wzzrd.model.tableComponent.impl.Card;
 import de.htwg.wzzrd.model.tableComponent.impl.Player;
 
 /**
  * Contains tests which probably could be avoided.
  * 
  * @author Michael
  */
 public class LightTableTest extends TestCase {
     private LightTableInterface lt;
     private Player[] p = TestDataProvider.genTwoPlayerArray();
 
     @Rule
     ExpectedException e = ExpectedException.none();
 
     @Override
     @Before
     public void setUp() throws GameIsFinished, RoundNotFinished {
         lt = new LightTable();
         lt.initTable(p);
     }
 
     @Test
     public void test_startGame() throws GameIsFinished, RoundNotFinished, SubRoundAlreadyFinished, PlayerAlreadyLaidCard, SubRoundNotFinished, RoundAlreadyFinished {
         assertEquals(31, lt.getRoundsLeft());
         assertTrue(lt.isRoundFinished());
         assertTrue(lt.isSubRoundFinished());
 
         assertEquals(0, lt.getRoundNumber());
         lt.startRound(0);
         assertFalse(lt.isSubRoundFinished());
         assertFalse(lt.isRoundFinished());
         assertEquals(30, lt.getRoundsLeft());
         lt.addCard(p[0], new Card(0, 2));
         lt.addCard(p[1], new Card(0, 1));
         assertTrue(lt.isSubRoundFinished());
         assertTrue(lt.isRoundFinished());
         assertEquals(30, lt.getRoundsLeft());
         assertEquals(p[0], lt.getSubRoundWinner());
         assertEquals(1, lt.getTrickCount(p[0]));
         assertEquals(0, lt.getTrickCount(p[1]));
         assertEquals(1, lt.getRoundNumber());
 
         try {
             lt.getTrickCount(new Player("Haha"));
             fail();
         } catch (IllegalArgumentException e) {}
 
         lt.startRound(1);
         assertEquals(2, lt.getRoundNumber());
         assertEquals(29, lt.getRoundsLeft());
         assertFalse(lt.isRoundFinished());
         assertFalse(lt.isSubRoundFinished());
         lt.addCard(p[0], new Card(0, 13));
         lt.addCard(p[1], new Card(1, 1));
         assertTrue(lt.isSubRoundFinished());
         assertEquals(p[1], lt.getSubRoundWinner());
         lt.startSubRound();
         assertFalse(lt.isRoundFinished());
         assertFalse(lt.isSubRoundFinished());
         try {
             lt.getSubRoundWinner();
             fail();
         } catch (SubRoundNotFinished e) {}
         lt.addCard(p[0], new Card(0, 13));
         lt.addCard(p[1], new Card(1, 1));
         assertTrue(lt.isSubRoundFinished());
         assertEquals(p[1], lt.getSubRoundWinner());
         assertEquals(new Card(1, 1), lt.getSubRoundWinnerCard());
         assertTrue(lt.isRoundFinished());
         assertEquals(2, lt.getRoundNumber());
 
         try {
             lt.startSubRound();
             fail();
         } catch (RoundAlreadyFinished e) {}
     }
 
     @Test
     public void test_illegal_combination() {
         LightTable t = new LightTable();
         try {
             t.initTable(new Player[] { new Player("p1"), new Player("p2"), new Player("p3"), new Player("p4"), new Player("p5"), new Player("p6"), new Player("p7") });
             fail();
         } catch (Exception e) {}
     }
 
     @Test
     public void test_addCard_PlayerAlreadyLaidCard() throws SubRoundAlreadyFinished, PlayerAlreadyLaidCard, SubRoundNotFinished, GameIsFinished, RoundNotFinished {
         assertTrue(lt.isRoundFinished());
         assertTrue(lt.isSubRoundFinished());
         lt.startRound(0);
         try {
             lt.addCard(p[0], new Card(0, 2));
             lt.addCard(p[0], new Card(0, 1));
             fail();
         } catch (PlayerAlreadyLaidCard e) {}
         lt.addCard(p[1], new Card(0, 1));
         assertEquals(p[0], lt.getSubRoundWinner());
     }
 
     @Test
     public void test_addCard_SubRoundAlreadyFinished() throws PlayerAlreadyLaidCard, SubRoundNotFinished, GameIsFinished, RoundNotFinished {
         try {
             lt.addCard(p[0], new Card(0, 3));
             fail();
         } catch (SubRoundAlreadyFinished e) {}
         try {
             lt.startRound(0);
             lt.addCard(p[0], new Card(0, 3));
             lt.addCard(p[1], new Card(0, 3));
             lt.addCard(p[0], new Card(0, 3));
             fail();
         } catch (SubRoundAlreadyFinished e) {}
     }
 
     @Test
     public void test_getTrickCount() throws SubRoundAlreadyFinished, PlayerAlreadyLaidCard, SubRoundNotFinished, RoundAlreadyFinished, GameIsFinished, RoundNotFinished {
         lt.startRound(0);
         lt.addCard(p[0], new Card(0, 0));
         try {
             lt.startRound(1);
             fail();
         } catch (RoundNotFinished e) {}
         lt.addCard(p[1], new Card(0, 13));
         lt.startRound(1);
         lt.addCard(p[0], new Card(0, 0));
         lt.addCard(p[1], new Card(0, 13));
         lt.startSubRound();
         lt.addCard(p[0], new Card(0, 0));
         lt.addCard(p[1], new Card(0, 13));
         lt.startRound(1);
     }
 
     @Test
     public void test_trick_mechanics() throws SubRoundAlreadyFinished, PlayerAlreadyLaidCard, SubRoundNotFinished, RoundAlreadyFinished, GameIsFinished, RoundNotFinished {
         lt.startRound(0);
         lt.addCard(p[0], new Card(3, 13));
         lt.addCard(p[1], new Card(0, 1));
         assertEquals(p[1], lt.getSubRoundWinner());
     }
 
     @Test
    public void test_trick_mechanics2() throws SubRoundAlreadyFinished, PlayerAlreadyLaidCard, SubRoundNotFinished, RoundAlreadyFinished, GameIsFinished, RoundNotFinished {
        lt.startRound(3);
        lt.addCard(p[1], new Card(2, 4));
        lt.addCard(p[0], new Card(3, 0));
        assertEquals(p[1], lt.getSubRoundWinner());
    }

    @Test
     public void test_game_end() throws SubRoundAlreadyFinished, PlayerAlreadyLaidCard, SubRoundNotFinished, RoundAlreadyFinished, GameIsFinished, RoundNotFinished {
         assertEquals(31, lt.getRoundsLeft());
         for (int i = 1; i <= 30; i++) {
             playRound(i);
             assertEquals(31 - i, lt.getRoundsLeft());
         }
         try {
             playRound(31);
             fail();
         } catch (GameIsFinished e) {}
     }
 
     private void playRound(int roundnumber) throws SubRoundAlreadyFinished, PlayerAlreadyLaidCard, SubRoundNotFinished, RoundAlreadyFinished, GameIsFinished, RoundNotFinished {
         lt.startRound(0);
         lt.addCard(p[0], new Card(0, 3));
         lt.addCard(p[1], new Card(0, 3));
         for (int i = 1; i < roundnumber; i++) {
             lt.startSubRound();
             lt.addCard(p[0], new Card(0, 3));
             lt.addCard(p[1], new Card(0, 3));
         }
     }
 }
