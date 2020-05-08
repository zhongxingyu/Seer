 package projectrts.model.core;
 
 /**
  * The main model class of the RTS Game
  * The class handles the world and they players in the game
  * @author Bjrn Persson Mattson, Modified by Filip Brynfors
  */
 public class GameModel implements IGame {
 	private World world = new World(P.INSTANCE.getWorldHeight(), P.INSTANCE.getWorldWidth());
 	Player humanPlayer = new Player();
 	Player aiPlayer = new Player();
 	
 	
	
 	@Override
 	public void update(float tpf) {
 		humanPlayer.update(tpf);
 		aiPlayer.update(tpf);
 	}
 
 	@Override
 	public IPlayer getPlayer() {
 		return humanPlayer;
 	}
 
 	@Override
 	public ITile[][] getTileMap() {
 		return world.getTileMap();
 	}
 }
