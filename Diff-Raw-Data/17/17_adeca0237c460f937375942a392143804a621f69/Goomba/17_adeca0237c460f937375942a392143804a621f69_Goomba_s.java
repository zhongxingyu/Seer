 package demo;
 
 import java.util.List;
 import com.golden.gamedev.GameObject;
 import core.characters.GameElement;
 import core.characters.NPC;
 import core.characters.ai.DeadState;
 import core.characters.ai.State;
 
 
 /**
  * @author Eric Mercer (JacenLakiir)
  */
 @SuppressWarnings("serial")
 public class Goomba extends NPC
 {
 
     public Goomba (GameObject game)
     {
         super(game);
     }
     
     public Goomba (GameObject game, List<State> possibleStates)
     {
         super(game, possibleStates);
     }
     
     @Override
     public void afterHitFromTopBy (GameElement e) 
     {
         super.afterHitFromTopBy(e);
     }
     
     @Override
     public void afterHitFromRightBy (GameElement e)
     {
         super.afterHitFromRightBy(e);
     }
     
     @Override
     public void afterHitFromLeftBy (GameElement e)
     {
         super.afterHitFromLeftBy(e);
     }
     
     public void afterHitFromTopBy (Mario e) {
         setCurrentState(new DeadState(this));
     }
     
     public void afterHitFromRightBy (Koopa k)
     {
        handleKoopaSideCollision(k);
        setDirection(-1);
     }
     
     public void afterHitFromLeftBy (Koopa k)
     {
        handleKoopaSideCollision(k);
        setDirection(1);
     }
     
    private void handleKoopaSideCollision (Koopa k)
     {
         if (k.isInShellState() && k.getShellSpeed() != 0)
             setActive(false);
        setDirection(1);
     }
     
 }
