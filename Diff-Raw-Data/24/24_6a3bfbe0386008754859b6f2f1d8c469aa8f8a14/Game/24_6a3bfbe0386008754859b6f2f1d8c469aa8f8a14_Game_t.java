 package android.topdown.game;
 
 import android.content.Context;
 import android.gameengine.icadroids.engine.GameEngine;
 import android.gameengine.icadroids.input.OnScreenButtons;
 import android.gameengine.icadroids.renderer.GameView;
 import android.gameengine.icadroids.renderer.Viewport;
 import android.gameengine.icadroids.sound.GameSound;
 import android.graphics.Color;
 import android.view.Display;
 import android.view.WindowManager;
 
 public class Game extends GameEngine {
 
 	private Player player;
 	private Level level;
 	private Infobar info;
 	private Viewport port;
 	Zombie z;
 
 	public static final int PISTOLSOUND = 0;
 	public static final int SHOTGUNSOUND = 1;
 	public static final int GUNCLICKSOUND = 2;
 
 	public Game() {
 		super();
 		level = new Level("map");
 		player = new Player();
 		info = new Infobar(player);
 		addPlayer(player, 0, 0);
 		addGameObject(info);
 		z = new Zombie(100, 1, 1, player);
 		z.rotate(180);
 		addGameObject(z, 26 * 64, 5 * 64);
 		Viewport.useViewport = true;
 	}
 
 	@Override
 	public void initialize() {
 		super.initialize();
 
 		info.isVisible = true;
 
 		player.setPosition(super.getScreenWidth() / 2 - player.getSprite().getFrameWidth() / 2, super.getScreenHeight() / 2 - player.getSprite().getFrameHeight() / 2);
 		player.isVisible = true;
 
 		OnScreenButtons.use = true;
 		OnScreenButtons.feedback = true;
 		OnScreenButtons.opacity = 100;
 
 		GameView.BACKGROUND_COLOR = Color.BLACK;
 
 		level.initialize();
 
 		setTileMap(level.getGameTiles());
 
 		port = Viewport.getInstance();
 		port.setBounds(0, 0, 64 * 64, 64 * 64);
 		port.setPlayer(player);
 		port.setPlayerPositionTolerance(0.0, 0.0);
 		port.setPlayerPositionOnScreen(Viewport.PLAYER_VCENTER | Viewport.PLAYER_HCENTER);
 		Display display = ((WindowManager) GameEngine.getAppContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		info.setScreenSize(display.getHeight(), display.getWidth());
 
 		GameSound.addSound(PISTOLSOUND, "pistol");
 		GameSound.addSound(SHOTGUNSOUND, "shotgun");
 		GameSound.addSound(GUNCLICKSOUND, "gunclick");
 	}
 
 	@Override
 	public void update() {
 		super.update();
 
 		// if (OnScreenButtons.start)
 		// if (OnScreenButtons.select)
 		// player.setPosition(super.getScreenWidth() / 2 -
 		// player.getSprite().getFrameWidth() / 2, super.getScreenHeight() / 2 -
 		// player.getSprite().getFrameHeight() / 2);
 		if (OnScreenButtons.start)
 			z.rotate(7.5f);
 		if (OnScreenButtons.select)
 			z.rotate(-7.5f);
 		
 		level.update();
 		
		info.setPort(port.getZoomFactor(), port.getViewportX(), port.getViewportY());
 		info.setPosition(port.getViewportX() + 200, port.getViewportY() + 200);
 	}
 
 }
