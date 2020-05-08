 package fi.ringofsnake.gamestates;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import fi.ringofsnake.main.Main;
 import fi.ringofsnake.entities.ScrollingBackGround;
 import fi.ringofsnake.entities.SnakeMap;
 
 import fi.ringofsnake.entities.SquirrelMob;
 
 import fi.ringofsnake.io.ResourceManager;
 import fi.ringofsnake.entities.Player;
 
 public class PlayGameState extends BasicGameState {
 
 	private int stateID = -1;
 	private Player player;
 	private SquirrelMob squirrels;
 
 	private SnakeMap currentMap = null;
 
 	private float[] offset = { 0.0f, 0.0f };
 
 	private ScrollingBackGround scrollingBackGround;
 	
 	private Music gamePlayMusic;
 	
 	public PlayGameState(int stateID) {
 		this.stateID = stateID;		
 	}
 
 	public int getID() {
 		return stateID;
 	}
 
 	private float tunnelHorizontalOffset = 0;	
 	private float tunnelSpeed = 0.5f;
 	
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		
 		currentMap = new SnakeMap(3000, 3);
 		player = new Player(container);
 
 		gamePlayMusic = ResourceManager.fetchMusic("GAMEPLAY_BG_MUSIC");
 
 		scrollingBackGround = new ScrollingBackGround(0.5f);
 		squirrels = new SquirrelMob();
 	}
 
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		
 		super.enter(container, game);
 		gamePlayMusic.loop();	
 	}
 	
 	@Override
 	public void leave(GameContainer container, StateBasedGame game)
 			throws SlickException {
 
 		super.leave(container, game);
 		gamePlayMusic.stop();
 		squirrels.stop();
 	}
 	
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		// TODO read map
 
 		scrollingBackGround.render(container, g);
 		
 		//This moves the map position relative to cat
 		g.translate( (int)-(player.position.x + tunnelHorizontalOffset), 
 						  -container.getHeight()/2 - 180 );
 		
 		currentMap.render(container, g);		
 		g.resetTransform();	
 		
 		
 		squirrels.render(container, g);		
 		
 		// just for now
 		player.render(container, g);
 
 
 		//Draw scores and other things here. Bitch.
 		
 		drawDebugLines( container, g );
 	}
 
 	private void drawDebugLines( GameContainer cont, Graphics g ) {
 		
 		g.setColor(Color.red);
 		
 		for( int y = 50; y<cont.getHeight();y += 50) {
 			g.drawString( String.valueOf(y), 5, y);
 			g.drawLine(20, y, 40, y);
 		}
 	}
 	
 	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)		throws SlickException {
 		squirrels.update(container, delta);
 		//player.update(container, delta);
 		Input input = container.getInput();
 		player.update(container, delta);
 
 		if (input.isKeyPressed(Input.KEY_PAUSE)	|| input.isKeyPressed(Input.KEY_ESCAPE)) {
 			game.enterState(Main.MAINMENU_GAME_STATE);
 		}
 
 		int mod = currentMap.getTile(0, 0).getImage().getHeight(); // assume
 																	// rect
 																	// tiles
 		// FIXME bg scrolling, do this with camera?
 		float step = (float) Math.sin((double) (System.currentTimeMillis()) / 1000.0);
 		offset[0] = ((offset[0] + step) % mod);
 		offset[1] = ((offset[1] + step) % mod);
 				
 		scrollingBackGround.update(container, delta);
 		
 		tunnelHorizontalOffset += tunnelSpeed * delta;
 	}
 }
