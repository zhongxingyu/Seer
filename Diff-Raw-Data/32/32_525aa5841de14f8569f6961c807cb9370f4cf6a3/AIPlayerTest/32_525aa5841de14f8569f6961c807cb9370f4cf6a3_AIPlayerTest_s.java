 package dominoes.players;
 
 import dominoes.Bone;
 import dominoes.BoneYard;
 import dominoes.DominoUI;
 import dominoes.Dominoes;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 
 /**
  * User: Sam Wright
  * Date: 13/03/2013
  * Time: 10:52
  */
 @RunWith(MockitoJUnitRunner.class)
 public class AIPlayerTest {
     @Mock
     private DominoUI ui;
     private DominoPlayer player1, player2;
 
     private DominoPlayer createAIPlayer() {
         return new AIPlayer();
 //        return new ExampleCleverPlayer();
     }
 
     @Before
     public void setUp() throws Exception {
         player1 = createAIPlayer();
         player2 = createAIPlayer();
         player1.setName("First Player");
         player2.setName("Second Player");
     }
 
     @Test
     public void testBoneyard() throws Exception {
         BoneYard boneYard = new BoneYard(6);
 
         assertEquals(28, boneYard.size());
 
         Set<Bone> allBones = new HashSet<Bone>();
         for (int i = 0; i < 28; ++i)
             allBones.add(boneYard.draw());
 
         assertEquals(28, allBones.size());
 
     }
 
     @Test
     public void testGame() throws Exception {
         player1.setPoints(0);
         player2.setPoints(0);
 
         System.out.println("Playing game...");
         Dominoes game = new Dominoes(ui, player1, player2, 100, 6);
         game.play();
 
         System.out.println("Player 1 scored " + player1.getPoints());
         System.out.println("Player 2 scored " + player2.getPoints());
 
         assertTrue((player1.getPoints() >= 100 && player2.getPoints() < 100)
                 || (player2.getPoints() >= 100 && player1.getPoints() < 100));
     }
 
     @Test
     public void testMultipleGames() throws Exception {
        for (int i = 0; i < 1000; ++i)
             testGame();
 
     }
 
 
     public static void main(String[] args) {
         DominoPlayer probabilisticAI = new AIPlayer();
         DominoPlayer dumbAI = new ExampleCleverPlayer(false);
 
         DominoUI ui = mock(DominoUI.class);
 
         probabilisticAI.setName("Probabilistic AI");
         dumbAI.setName("Dumb AI");
 
         int probWins = 0, dumbWins = 0, probPoints = 0, dumbPoints = 0;
 
         System.out.println("Starting game...");
 
         for (int i = 0; i < 100; ++i) {
             probabilisticAI.setPoints(0);
             dumbAI.setPoints(0);
 
             DominoPlayer winner = new Dominoes(ui, probabilisticAI, dumbAI, 100, 6).play();
 
             if (winner == probabilisticAI)
                 probWins += 1;
             else
                 dumbWins += 1;
 
             probPoints += probabilisticAI.getPoints();
             dumbPoints += dumbAI.getPoints();
 
             System.out.format("AI won %d points (%.1f%%) and %d games (%.1f%%)%n", probPoints, probPoints * 100. / (probPoints + dumbPoints)
                                                                            , probWins, probWins * 100. / (probWins + dumbWins));
         }
     }
 }
