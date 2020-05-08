 package controller;
 
 import javax.swing.Timer;
 
 import model.GameModel;
 import model.GameState;
 import model.GameStateFactory;
import model.elements.Invader;
 import view.MainView;
 
 import command.CommandFactory;
 import command.CommandListener;
 
 public class GameController extends AbstractController {
 
 	private Timer timer;
 
 	public GameController(MainView gw, GameModel gm) {
 		super(gw, gm);
 
 		gm.setActiveGameState(GameStateFactory.createLevelOne()); // TODO this is BAD
 
 	}
 	
 	private Timer getTimer() {
 		if (this.timer == null) {
 			this.timer = new Timer(500, new CommandListener(CommandFactory.createUpdateGameStateCommand(gm.getActiveGameState())));
 		}
 		
 		return timer;
 	}
 
 	public void setGameLoopEnabled(boolean enabled) {
 		if (enabled) {
 			this.getTimer().start();
 		} else {
 			this.getTimer().stop();
 		}
 	}
 
 	/**
 	 * @param gameState
 	 *            The {@link GameState} whose values are to be stepped forward
 	 *            in time.
 	 */
 	public void updateGameState(GameState gameState) {
 		// TODO implement
 		long currentTime = System.currentTimeMillis();
 		long timeDelta = currentTime - gameState.getLastUpdateTime();
 		
		for (Invader inv : gameState.getInvaders()) {
			//inv.getPosition().x
		}
		
 		System.out.println(timeDelta);
 		
 		gameState.setLastUpdateTime(currentTime);
 	}
 
 }
