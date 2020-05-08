 package tomato.screen;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import tomato.Camera;
 import tomato.Game;
 import tomato.GameObject;
 import tomato.Input;
 import tomato.entity.CooldownObserver;
 import tomato.entity.LifeObserver;
 import tomato.entity.PhysicsEntity;
 import tomato.gfx.Art;
 import tomato.level.Level;
 import tomato.level.LevelFactory;
 import tomato.screen.layer.Background;
 import tomato.screen.layer.ColorLayer;
 import tomato.screen.layer.DescriptionLayer;
 import tomato.screen.layer.FadeOutLayer;
 import tomato.screen.layer.PauseLayer;
 import tomato.screen.layer.PlayerStatsLayer;
 
 public class GameScreen extends Screen implements LifeObserver {
 	private Camera cam;
 	private Level level;
 	private int life;
 	private double time = 0D;
 	private ArrayList<GameObject> gameObjects;
 	private PlayerStatsLayer statsLayer;
 	private FadeOutLayer fadeOutLayer;
 	private PauseLayer pauseLayer;
 	private boolean pausedByFocus, paused;
 
 	public GameScreen() {
 		statsLayer = new PlayerStatsLayer();
 		pausedByFocus = false;
 		paused = false;
 		pauseLayer = new PauseLayer();
 		level = LevelFactory.getLevelById(LevelFactory.level1);
 		cam = Camera.getInstance().init(Game.GAME_WIDTH, Game.GAME_HEIGHT);
 		level.init();
 
 		PhysicsEntity player = level.getPlayer();
 		player.addObserver((CooldownObserver) statsLayer);
 		player.addObserver(this);
 
 		gameObjects = new ArrayList<GameObject>();
 		gameObjects.add(new ColorLayer(new Color(0x7AA1FF)));
 		fillBackgounds(Art.level_bg2, 0.5, 0.1);
 		fillBackgounds(Art.level_bg1, 0.8, 0.1);
 		fadeOutLayer = new FadeOutLayer(30, new Color(0xFF0B0F));
 		gameObjects.add(new DescriptionLayer(Art.level1_desc));
 		gameObjects.add(level);
 		gameObjects.add(statsLayer);
 		gameObjects.add(fadeOutLayer);
 		gameObjects.add(pauseLayer);
 		Game.getGameInstance().addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				if (!paused) {
 					pauseGame();
 
 					pausedByFocus = true;
 				}
 
 			}
 
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				if (paused && pausedByFocus) {
 					resumeGame();
 					pausedByFocus = false;
 				}
 
 			}
 		});
 	}
 
 	public void fillBackgounds(BufferedImage img, double factorX, double factorY) {
 		int width = img.getWidth();
 
 		for (int i = 0; i * width < level.getWidth(); i++) {
 			gameObjects.add(new Background(img, i * width, factorX, factorY));
 		}
 	}
 
 	@Override
 	public void render(Graphics g) {
 		super.render(g);
 		g.setColor(new Color(104, 136, 247, 255));
 
 		for (int i = 0; i < gameObjects.size(); i++) {
 			gameObjects.get(i).render(g, cam);
 		}
 
 		if (Game.DEBUG) {
 			g.setColor(Color.DARK_GRAY);
 			for (int j = 0; j < Game.GAME_HEIGHT; j++) {
 				g.drawLine(0, j * 15 - cam.y, Game.GAME_WIDTH, j * 15 - cam.y);
 			}
 			for (int i = 0; i < level.getWidth() / 15; i++) {
 				g.drawLine(i * 15 - cam.x, 0, i * 15 - cam.x, Game.GAME_HEIGHT);
 			}
 		}
 		if (life == 0) {
 			Art.drawString("Press ESC to continue...", g,
 					100 + (int) (Math.cos(time * 2) * 50),
 					(Game.GAME_HEIGHT / 2 - 10)
 							+ (int) (Math.sin(time * 2) * 50));
 		}
 	}
 
 	@Override
 	public void tick(Input input, double delta) {
 		super.tick(input, delta);
 
 		if (input.buttons[Input.ESCAPE]) {
 			setScreen(new TitleScreen());
 			input.releaseAllKeys();
 		}
 		if (input.buttons[Input.PAUSE] && !input.oldButtons[Input.PAUSE]) {
 			togglePause();
 		}
 		if (!paused) {
 			for (int i = 0; i < gameObjects.size(); i++) {
 				gameObjects.get(i).tick(input, delta);
 			}
 		}
 		if (life == 0) {
 			time += delta / Game.factor;
 			if (!fadeOutLayer.isFadingOut()) {
 				fadeOutLayer.startFadeOut();
 			}
 			if (time >= 40D) {
 				setScreen(new TitleScreen());
 				input.releaseAllKeys();
 			}
 		}
 
 	}
 
 	@Override
 	public void updateLife(int life) {
 		this.life = life;
 		statsLayer.updateLife(life);
 	}
 
 	public void pauseGame() {
 		paused = true;
 		pauseLayer.show(true);
 	}
 
 	public void resumeGame() {
 		paused = false;
 		pauseLayer.show(false);
 	}
 
 	public void togglePause() {
 		paused = !paused;
 		pauseLayer.show(paused);
 	}
 
 }
