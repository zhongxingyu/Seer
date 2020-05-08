 package states;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class TownState extends AreaState {
 
 	public TownState(int stateID) {
 		super(stateID);
 	}
 
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.init(container, game);
 		bgImage = new TiledMap("Assets/Transition 1/Map.tmx");
		players[0].init(0);
		players[1].init(1);
 		
 		battleStops = new int[3];
 		battleStops[0] = 1000;
 		battleStops[1] = 2000;
 		battleStops[2] = 3000;
 	}
 }
