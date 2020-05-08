 package game.screen;
 
 import game.Game;
 import game.Map;
 import game.utils.FileSaver;
 import game.utils.SpriteSheet;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 
 public class ScreenMainMenu extends Screen {
 
 	Graphics g;
 	private int lastWidth = 0; 
 	private int lastHeight = 0;
 
 	public ScreenMainMenu(int width, int height, SpriteSheet sheet) {
 		super(width, height, sheet);
 		addButton("Singleplayer", new Rectangle(290, 116, 232, 25));
 		addButton("Wave mode", new Rectangle(290, 148, 232, 25));
 		addButton("Map editor", new Rectangle(290, 180, 232, 25));
 		addButton("Options", new Rectangle(290, 212, 232, 25));
 		addButton("Exit", new Rectangle(290, 244, 232, 25));
 	}
 	
 
 	@Override
 	public void render(Graphics g) {
 		this.g = g;
 		this.drawBackgroundScreen();
 		// this.drawAnimatedBackground();
 		//game.getFontRenderer().drawCenteredString(Game.TITLE, 36, 3);
 		g.drawImage(game.logo, (game.getWidth()/2)-164, 36, 328, 66, game);
 		ScreenTools.drawButton((game.getWidth()/2)-116, 116, 232, 25, "Singleplayer", g, game,
 				new Color(255, 255, 255, 155), Color.white);
 		ScreenTools.drawButton((game.getWidth()/2)-116, 148, 232, 25, "TEST BUTTON", g, game,
 				new Color(255, 255, 255, 155), Color.white);
 		ScreenTools.drawButton((game.getWidth()/2)-116, 180, 232, 25, "Map Editor", g, game,
 				new Color(255, 255, 255, 155), Color.white);
 		ScreenTools.drawButton((game.getWidth()/2)-116, 212, 232, 25, "Options", g, game,
 				new Color(255, 255, 255, 155), Color.white);
 		ScreenTools.drawButton((game.getWidth()/2)-116, 244, 232, 25, "Quit", g, game, new Color(
 				255, 255, 255, 155), Color.white);
 
 		game.getFontRenderer().drawString(
 				String.format("%s version %s", Game.TITLE, Game.VERSION), 0,
 				game.getHeight()-9, 1);
 
 		if(game.startError != null)
			ScreenTools.drawButton(100, 300, 600, 200, "Substrate experianced the following\nerror whilst starting:\n "+game.startError+"\n\nThe game may not work correctly!", g, game, new Color(155,0,0), new Color(255,255,255,255));
 	}
 
 	@Override
 	public void tick() {
 		if(game.getWidth() != lastWidth || game.getHeight() != lastHeight)
 		{
 			lastWidth = game.getWidth();
 			lastHeight = game.getHeight();
 			clearButtons();
 			
 			addButton("Singleplayer", new Rectangle((game.getWidth()/2)-116, 116, 232, 25));
 			addButton("Wave mode", new Rectangle((game.getWidth()/2)-116, 148, 232, 25));
 			addButton("Map editor", new Rectangle((game.getWidth()/2)-116, 180, 232, 25));
 			addButton("Options", new Rectangle((game.getWidth()/2)-116, 212, 232, 25));
 			addButton("Exit", new Rectangle((game.getWidth()/2)-116, 244, 232, 25));
 		}
 	}
 
 	@Override
 	public void postAction(String action) {
 		switch (action) {
 		case "Singleplayer":
 			game.setScreen(new ScreenIntro(w, h, sheet));
 			break;
 		case "Wave mode":
 			game.setScreen(new ScreenDeath(w, h, sheet));
 			break;
 		case "Map editor":
 			game.setScreen(new ScreenMapEditor(w, h, sheet));
 			break;
 		case "Options":
 			game.setScreen(new ScreenOptions(w, h, sheet, game.SETTINGS));
 			break;
 		case "Exit":
 			game.shutdown();
 			break;
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 	}
 
 }
