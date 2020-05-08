 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package enterprise.sessions;
 
 import enterprise.persistence.Player;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;

 /**
  *
  * @author Cyril Cecchinel
  */
 @Stateless
 public class GameSession implements GameSessionRemote{
     @javax.persistence.PersistenceContext(unitName="GameDB")
     private EntityManager em ;
     
     @Override
     public void addPlayer(Player p) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public int getPlayerScore() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public int getOpponentScore() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean playerWin() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean opponentWin() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void play(int choice) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
 }
