 package de.schelklingen2008.reversi.ai.strategy;
 
 import org.apache.tools.ant.taskdefs.Move;
 
 import com.threerings.ezgame.Game;
 
 import de.schelklingen2008.reversi.model.GameModel;
 import de.schelklingen2008.reversi.model.Piece;
 import de.schelklingen2008.reversi.model.Player;
 
 public class AmorStrategy implements ReversiStrategy
 {
 
     Player player;
 
     public int getCount()
     {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public Piece move(GameModel gameModel)
     {
         player = gameModel.getTurnHolder();
 
         return null;
     }
 
     public Move minimax(GameModel gameModel)
     {
         int best = Integer.MAX_VALUE;
         Move bestMove = null;
         int value;
         Player hilf = gameModel.getTurnHolder();
         for (int i = 0; i < gameModel.getLegalMoves(hilf).length; i++)
         {
 
             for (int j = 0; j < gameModel.getLegalMoves(hilf)[0].length; j++)
             {
                 Game clone = game.copy();
                 value = mmVal(clone.move(move), 5);
                 if (value > best)
                 {
                     best = value;
                     bestMove = move;
 
                 }
             }
 
             return bestMove;
 
         }
 
     }
 
     public int mmVal(GameModel gameModel, int depth)
     {
         Player hilf = gameModel.getTurnHolder();
         if (depth == 0) return amorEvaluationFunction.evaluatePosition(gameModel, hilf);
         int best;
         int value;
         if (gameModel.isTurnHolder(player))
             best = Integer.MIN_VALUE;
         else
             best = Integer.MAX_VALUE;
 
         for (int i = 0; i < gameModel.getLegalMoves(hilf).length; i++)
         {
 
             for (int j = 0; j < gameModel.getLegalMoves(hilf)[0].length; j++)
             {
                 Game clone = (Game) gameModel.clone();
                 value = mmVal(clone.move(move), depth - 1);
             }
         }
         if (gameModel.isTurnHolder(player))
         {
             if (value > best)
                 best = value;
             else if (value < best) best = value;
         }
         return best;
 
     }
 }
