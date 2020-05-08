 package src.edu.ucsc.gameAI.conditions;
 
 import edu.ucsc.gameAI.ICondition;
 import pacman.game.Constants.GHOST;
 import pacman.game.Game;
 
 public class GhostEaten implements ICondition {
         
     private Game _game;
     private GHOST _ghost;
     
        public GhostEaten(Game game, GHOST ghost)
        {
         _game = game;
         _ghost = ghost;
        }
         
        public boolean test() 
        {
                return _game.wasGhostEaten(_ghost);
        }
 }
