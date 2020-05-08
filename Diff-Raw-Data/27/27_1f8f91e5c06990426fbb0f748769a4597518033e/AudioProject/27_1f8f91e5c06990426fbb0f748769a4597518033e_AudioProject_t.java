 package audioProject;
 
 import static java.lang.Math.abs;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.io.FileNotFoundException;
 import java.util.Random;
 
 import javax.swing.JOptionPane;
 
 import maryb.player.Player;
 import toritools.additionaltypes.ColorCycler;
 import toritools.additionaltypes.ColorUtils;
 import toritools.entity.Entity;
 import toritools.entity.Level;
 import toritools.entrypoint.Binary;
 import toritools.math.Vector2;
 import toritools.scripting.ScriptUtils;
 import audioProject.controller.WaveController;
 import audioProject.entities.BadShipFactory;
 import audioProject.entities.PlayerShip;
 import audioProject.entities.ScrollingBackground;
 
 /**
  * Template for our possible audio project.
  * 
  * @author toriscope
  * 
  */
 public class AudioProject extends Binary {
 	
 	public static Player soundPlayer = new Player();
 	public static WaveController controller;
 	
 	public static Random random;
 	
 	/**
 	 * To make it easier to change things.
 	 */
 	public static Color 
 			barsColor = new Color(0, 250, 0),
 			barsLighterColor = new Color(0, 250, 0),
 			barsDarkerColor = new Color(245, 153, 255),
 			shipColor = Color.black,
 			enemyColor = Color.RED,
 			bgColor = null;
    
 	public static float getFloat() {
 		return random.nextFloat();
 	}
 	
 	public static int bars = 100;
 	
 	public static void main(String[] args) {
 		new AudioProject();
 	}
 
 	public AudioProject() {
		super(new Vector2(800, 600), 60, "Audio Technical Project 1");
 	}
 
 	@Override
 	protected boolean render(Graphics rootCanvas, Level level) {
 		try {
 			((Graphics2D) rootCanvas).setStroke(new BasicStroke(4));
 			rootCanvas.setColor(bgColor);
 			rootCanvas.fillRect(-1, -1, (int) VIEWPORT.x + 2, (int) VIEWPORT.y + 2);
 			for (int i = level.getLayers().size() - 1; i >= 0; i--) {
 				for (Entity e : level.getLayers().get(i)) {
 					if (e.isVisible())
 						e.draw(rootCanvas, Vector2.ZERO);
 				}
 			}
 			rootCanvas.setColor(Color.BLACK);
 			rootCanvas.drawString("Entities: " + level.getNonSolids().size(), 10, 20);
 			rootCanvas.drawString("Feel: " + controller.getFeel(), 10 , 40);
 			
 		} catch (final Exception uhoh) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	protected void initialize() {
 		String songName = JOptionPane.showInputDialog("Name of song? (unicorn/goo)");
 		if (songName == null) {
 			JOptionPane.showMessageDialog(null, "YOU ARE MAXIMUM LAME");
 			System.exit(1);
 		}
 		soundPlayer.setCurrentVolume(1f);
 		soundPlayer.setSourceLocation("audioProject/" + songName + ".mp3");
 		try {
 			controller = new WaveController(songName, bars);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		soundPlayer.play();
 		moments = entities = 0;
 		random = new Random(0);
 	}
 	
 	long moments, entities;
 	
 	ColorCycler enemyColorCycler = new ColorCycler(230, 255, 0, 0, 0, 0);
 	ColorCycler bgColorCycler = new ColorCycler(220, 255, 220, 255, 220, 255);
 
 	@Override
 	protected void globalLogic(Level level) {
 		
 		controller.setTime44100((long) (soundPlayer.getCurrentPosition()* 0.001));
 		
 		ScrollingBackground bg = (ScrollingBackground) level.getEntityWithId("bg");
 		PlayerShip player = (PlayerShip) level.getEntityWithId("player");
 		
 		bg.setFocus(player.getPos(), .5f);
 		bg.setSpeed(2 * controller.getFeel());
 		
 		if (ScriptUtils.getKeyHolder().isPressed(KeyEvent.VK_ESCAPE)) {
 			System.exit(0);
 		}
 		
		if (getFloat() < .019 * abs(controller.getFeel())) {
 			level.spawnEntity(BadShipFactory.makeDefaultEnemy(VIEWPORT));
 		}
 		
 		//bgColor = ColorUtils.blend(Color.BLACK, new Color(0, 64, 13), controller.getFeel());
 		//barsColor = ColorUtils.blend(Color.GREEN, new Color(0, 64, 13), 1 - controller.getFeel());
 		
 		bgColor = ColorUtils.blend(Color.BLUE, Color.CYAN, controller.getFeel());
 		Color c = ColorUtils.blend(barsDarkerColor, barsLighterColor, controller.getFeel());
 		barsColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 220);
 	}
 
 	@Override
 	protected void setupCurrentLevel(Level levelBeingLoaded) {
 		levelBeingLoaded.spawnEntity(new PlayerShip());
 		levelBeingLoaded.spawnEntity(new ScrollingBackground(VIEWPORT, 1, bars, 2.3f, .614f, 100, 100, .05f * controller.getAverageFeel()));
 		addLevelBounds(levelBeingLoaded);
 	}
 	
 	private void addLevelBounds(final Level levelBeingLoaded) {
 		Entity l, r, u , d;
 
 		l = new Entity();
 		l.setSolid(true);
 		l.setPos(new Vector2(-20, 0));
 		l.setDim(new Vector2(20, VIEWPORT.y));
 		
 		r = new Entity();
 		r.setSolid(true);
 		r.setPos(new Vector2(VIEWPORT.x, 0));
 		r.setDim(new Vector2(VIEWPORT.x, VIEWPORT.y));
 		
 		u = new Entity();
 		u.setSolid(true);
 		u.setPos(new Vector2(0, -20));
 		u.setDim(new Vector2(VIEWPORT.x, 20));
 		
 		d = new Entity();
 		d.setSolid(true);
 		d.setPos(new Vector2(0, VIEWPORT.y));
 		d.setDim(new Vector2(VIEWPORT.x, 20));
 		
 		levelBeingLoaded.spawnEntity(l);
 		levelBeingLoaded.spawnEntity(r);
 		levelBeingLoaded.spawnEntity(u);
 		levelBeingLoaded.spawnEntity(d);
 	}
 
 	@Override
 	protected Level getStartingLevel() {
 		Level level = new Level();
 		level.setDim(VIEWPORT);
 		return level;
 	}
 
 }
