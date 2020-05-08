 package com.turbonips.troglodytes.states;
 
 import java.awt.Font;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.MouseListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.turbonips.troglodytes.ResourceFactory;
 import com.turbonips.troglodytes.ResourceManager;
 
 public class MenuState extends BaseGameState implements MouseListener {
 	public static final int ID = 1;
 	
 	private UnicodeFont titleFont;
 	private UnicodeFont buttonFont;
 	private String title = "Troglodytes";
 	
 	private TiledMap map;
 	private int mapX = 0, mapY = 0;
 	
 	private int mapVelX = (int)(Math.random()*-2) - 1; 
 	private int mapVelY = (int)(Math.random()*-2) - 1;
 	private StateBasedGame game;
 	
 	enum State {NONE,
 				NEW_GAME,
 				OPTIONS,
 				EXIT};
 				
 	State selectedState = State.NONE;
 	private String newGameSt = "New Game";
 	private int newGameX, newGameY;
 	private String optionsSt = "Options";
 	private int optionsX, optionsY;
 	private String exitSt = "Exit";
 	private int exitX, exitY;
 
 	@Override
 	public void init(GameContainer container, final StateBasedGame game)
 			throws SlickException {
 		super.init(container, game);
 		ResourceManager manager = ResourceManager.getInstance();
 		ResourceFactory factory = ResourceFactory.getInstance();
 		ArrayList<String> resourceIds = factory.getResourceIds("tiledmap");
 		String randomMap = resourceIds.get((int)(Math.random() * resourceIds.size()));
 		map = (TiledMap)manager.getResource(randomMap).getObject();
 
 		// load a default java font
 		String fontName = "Palatino";
 		//String fontName = "Lucida Sans Unicode";
 		titleFont = new UnicodeFont (new Font(fontName, Font.BOLD, 70));
 		titleFont.getEffects().add(new ColorEffect(java.awt.Color.white));
 		buttonFont = new UnicodeFont (new Font(fontName, Font.BOLD, 30));
 		buttonFont.getEffects().add(new ColorEffect(java.awt.Color.white));
 		this.game = game;
 		
 		newGameX = container.getWidth()/2-(buttonFont.getWidth(newGameSt)/2);
 		newGameY = container.getHeight()/4+titleFont.getHeight(title)+35;
 		optionsX = container.getWidth()/2-(buttonFont.getWidth(optionsSt)/2);
 		optionsY = container.getHeight()/4+titleFont.getHeight(title)+35+buttonFont.getHeight(newGameSt)+10;
 		exitX = container.getWidth()/2-(buttonFont.getWidth(exitSt)/2);
		exitY = container.getHeight()/4+titleFont.getHeight(title)+35+buttonFont.getHeight(newGameSt)+10+buttonFont.getHeight(optionsSt)+10;
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		Color WHITE = new Color(255,255,255);
 		Color ORANGE = new Color(255,127,0);
 		
 		map.render(mapX, mapY, 0);
 		map.render(mapX, mapY, 1);
 		map.render(mapX, mapY, 2);
 		
 		if (map.getTileWidth()*map.getWidth() < Math.abs(mapX)+container.getWidth() ||
 			mapX > 0) {
 			mapVelX *= -1;
 		}
 		if (map.getTileHeight()*map.getHeight() < Math.abs(mapY)+container.getHeight() ||
 			mapY > 0) {
 			mapVelY *= -1;
 		}
 		mapX += mapVelX;
 		mapY += mapVelY;
 				
 		// Draw the Title
 		titleFont.drawString(container.getWidth()/2-(titleFont.getWidth(title)/2), 
 				container.getHeight()/4, 
 				title);
 		
 		
 		// Draw the new game button
 		if (selectedState == State.NEW_GAME) {
 			buttonFont.drawString(newGameX,newGameY, newGameSt, ORANGE);
 		} else {
 			buttonFont.drawString(newGameX,newGameY, newGameSt, WHITE);
 		}
 		
 		// Draw the options button
 		if (selectedState == State.OPTIONS) {
 			buttonFont.drawString(optionsX, optionsY, optionsSt, ORANGE);
 		} else {
 			buttonFont.drawString(optionsX, optionsY, optionsSt, WHITE);
 		}
 		
 		// Draw the exit button
 		if (selectedState == State.EXIT) {
 			buttonFont.drawString(exitX, exitY, exitSt, ORANGE);
 		} else {
 			buttonFont.drawString(exitX, exitY, exitSt, WHITE);
 		}
 		
 		titleFont.loadGlyphs();
 		buttonFont.loadGlyphs();
 		
 	}
 	
 	@Override
 	public void mouseClicked(int button, int x, int y, int clickCount) {
 		// New game clicked
 		if (x > newGameX && x < newGameX + buttonFont.getWidth(newGameSt) &&
 			y > newGameY && y < newGameY + buttonFont.getHeight(newGameSt)) {
 			game.enterState(PlayingState.ID);
 		}
 		
 		// Options clicked
 		if (x > optionsX && x < optionsX + buttonFont.getWidth(optionsSt) &&
 			y > optionsY && y < optionsY + buttonFont.getHeight(optionsSt)) {
 			game.enterState(OptionState.ID);
 		}
 		
 		// Exit clicked
 		if (x > exitX && x < exitX + buttonFont.getWidth(exitSt) &&
 			y > exitY && y < exitY + buttonFont.getHeight(exitSt)) {
 			System.exit(1);
 		}
 		
 		selectedState = State.NONE;
 	}
 	
 	@Override
 	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
 		selectedState = State.NONE;
 		
 		// New game selected
 		if (newx > newGameX && newx < newGameX + buttonFont.getWidth(newGameSt) &&
 			newy > newGameY && newy < newGameY + buttonFont.getHeight(newGameSt)) {
 			selectedState = State.NEW_GAME;
 		}
 		
 		// Options selected
 		if (newx > optionsX && newx < optionsX + buttonFont.getWidth(optionsSt) &&
 			newy > optionsY && newy < optionsY + buttonFont.getHeight(optionsSt)) {
 			selectedState = State.OPTIONS;
 		}
 		
 		// Exit selected
 		if (newx > exitX && newx < exitX + buttonFont.getWidth(exitSt) &&
 			newy > exitY && newy < exitY + buttonFont.getHeight(exitSt)) {
 			selectedState = State.EXIT;
 		}
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 
 }
