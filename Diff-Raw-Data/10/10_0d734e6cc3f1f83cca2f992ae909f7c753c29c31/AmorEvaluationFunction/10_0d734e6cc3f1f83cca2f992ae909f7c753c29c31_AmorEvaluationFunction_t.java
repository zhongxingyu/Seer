 package de.schelklingen2008.reversi.ai.evaluation;
 
 import de.schelklingen2008.reversi.model.GameModel;
 import de.schelklingen2008.reversi.model.Player;
 
 public class AmorEvaluationFunction implements EvaluationFunction
 {
 
     public int evaluatePosition(GameModel gameModel, Player player)
     {
         int x = 0;
         x = 3;
 
         return 0;
     }
 
     private int spielSteinBewertung(GameModel gameModel, Player player)
     {
         int[][] spielfeldbewertung = { { 1600, 100, 800, 800, 800, 800, 100, 1600 },
 
         { 100, 50, 200, 200, 200, 200, 50, 100 }, { 800, 200, 200, 200, 200, 200, 200, 800 },
                 { 800, 200, 200, 200, 200, 200, 200, 800 }, { 800, 200, 200, 200, 200, 200, 200, 800 },
                 { 800, 200, 200, 200, 200, 200, 200, 800 }, { 100, 50, 200, 200, 200, 200, 50, 100 },
                 { 1600, 100, 800, 800, 800, 800, 100, 1600 }, };
 
         int playerSteinBewertung = 0;
         if (gameModel.getPlayer(1, 1) == player)
         {
             spielfeldbewertung[1][2] = 800;
             spielfeldbewertung[2][2] = 800;
             spielfeldbewertung[2][1] = 800;
         }
 
         for (int i = 0; i < 8; i++)
         {
             for (int j = 0; j < 8; j++)
             {
                 if (gameModel.getBoard()[i][j] == player)
                 {
                     playerSteinBewertung = playerSteinBewertung + spielfeldbewertung[i][j];
                 }
             }
 
         }
         return playerSteinBewertung;
     }
 }
