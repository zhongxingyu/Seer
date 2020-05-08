 package states;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.MusicListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tests.MusicListenerTest;
 import org.newdawn.slick.tiled.TiledMap;
 
 import core.MainGame;
 
 import weapons.*;
 
 import dudes.GoblinArcher;
 import dudes.Knight;
 import dudes.Monster;
 
 public class TownState extends AreaState {
 
 	public TownState(int stateID) {
 		super(stateID);
 	}
 
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.init(container, game);
 		loop = new Music("Assets/Sound/Loops/TownState.wav");
 		bgImage = new TiledMap("Assets/World/Map2.tmx");
		areaLength = 180;
 
 		// note: players are initialized ONLY in the first area
 		players[0].init(0);
 		players[1].init(1);
 		
 //		SpiderWeb sw1 = new SpiderWeb(new float[] {container.getWidth()-200f, container.getHeight()-200f});
 //		SpiderWeb sw2 = new SpiderWeb(new float[] {container.getWidth()-300f, container.getHeight()-150f});
 //
 //		obstacles.add(sw1);
 //		obstacles.add(sw2);
 	
 		ArrayList<Monster> group_1 = new ArrayList<Monster>();
 		Knight g1_knight1 = new Knight(container.getWidth(),
 				container.getHeight() - 80, 0, container);
 		Knight g1_knight2 = new Knight(container.getWidth(),
 				container.getHeight() - 160, 0, container);
 		/*Knight g1_knight3 = new Knight(container.getWidth(),
 				container.getHeight() - 240, 0);*/
 		g1_knight1.init();
 		g1_knight2.init();
 		//g1_knight3.init();
 		group_1.add(g1_knight1);
 		group_1.add(g1_knight2);
 		//group_1.add(g1_knight3);
 
 		ArrayList<Monster> group_2 = new ArrayList<Monster>();
 		Knight g2_knight1 = new Knight(container.getWidth(),
 				container.getHeight() - 80, 0, container);
 		Knight g2_knight2 = new Knight(container.getWidth(),
 				container.getHeight() - 160, 0, container);
 		Knight g2_knight3 = new Knight(container.getWidth(),
 				container.getHeight() - 240, 0, container);
 		g2_knight1.init();
 		g2_knight2.init();
 		g2_knight3.init();
 		group_2.add(g2_knight1);
 		group_2.add(g2_knight2);
 		group_2.add(g2_knight3);
 
 		ArrayList<Monster> group_3 = new ArrayList<Monster>();
 		Knight g3_knight1 = new Knight(container.getWidth(),
 				container.getHeight() - 80, 0, container);
 		Knight g3_knight2 = new Knight(container.getWidth(),
 				container.getHeight() - 160, 0, container);
 		Knight g3_knight3 = new Knight(0,
 				container.getHeight() - 240, 0, container);
 		Knight g3_knight4 = new Knight(0,
 				container.getHeight() - 80, 0, container);
 		g3_knight1.init();
 		g3_knight2.init();
 		g3_knight3.init();
 		g3_knight4.init();
 		group_3.add(g3_knight1);
 		group_3.add(g3_knight2);
 		group_3.add(g3_knight3);
 		group_3.add(g3_knight4);
 		
 		monsters.add(group_1);
 		monsters.add(group_2);
 		monsters.add(group_3);
 
 		battleStops = new int[3];
 		battleStops[0] = 700;
 		battleStops[1] = 1300;
 		battleStops[2] = 1900;
 
 	}
 	
 	@Override
 	public int getID(){
 		return 2;
 	}
 }
