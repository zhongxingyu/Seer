 package lv.k2611a.network.req;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import lv.k2611a.service.connection.ConnectionState;
import lv.k2611a.service.game.GameService;
 import lv.k2611a.service.game.UserActionService;
 
 public abstract class AbstractGameStateChanger implements GameStateChanger, Request {
 
     @Autowired
     private UserActionService userActionService;
 
     @Autowired
     private ConnectionState connectionState;
 
    @Autowired
    private GameService gameService;

     protected int playerId;
 
     public int getPlayerId() {
         return playerId;
     }
 
     public void setPlayerId(int playerId) {
         this.playerId = playerId;
     }
 
     @Override
     public void process() {
         playerId = connectionState.getPlayerId();
        if (gameService.hasLost(playerId)) {
            return;
        }
         userActionService.registerAction(this);
     }
 }
