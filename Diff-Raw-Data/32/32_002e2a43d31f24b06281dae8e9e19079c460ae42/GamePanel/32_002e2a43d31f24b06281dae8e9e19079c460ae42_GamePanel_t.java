 package view;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import controller.GameController;
 
 import model.GameModel;
 import model.geometrical.CollisionBox;
 import model.items.weapons.Projectile;
 import model.sprites.Player;
 import model.sprites.Sprite;
 
 /**
  * 
  * 
  * @author 
  *
  */
 public class GamePanel extends JPanel implements PropertyChangeListener, MouseMotionListener {
 
 	private static final long serialVersionUID = 1L;
 	private GameModel model;
 	private GameController controller;
 	private long tick = 0;
 	private TileView[][] tiles;
 	private List<ObjectRenderer<?>> objects;
 	private Camera camera;
 	private final int SLEEP = 1000 / 60;
 	
 	/**
 	 * Creates a new panel with the specified model and controller.
 	 * @param model the model to display.
 	 * @param controller the controller to use.
 	 */
 	public GamePanel(GameModel model, GameController controller) {
 		super();
 		this.model = model;
 		this.controller = controller;
 		this.addMouseMotionListener(this);
 		this.camera = new Camera(40);
 		this.initObjectList();
 		this.initTileList();
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				while(true) {
 					repaint();
 					tick++;
 					try{
 						Thread.sleep(SLEEP);
 					}catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 		t.start();
 	}
 	
 	/*
 	 * Creates the list which holds all the sprite views, and loads it with the 
 	 * sprites already in the world.
 	 */
 	private void initObjectList() {
 		objects = new ArrayList<ObjectRenderer<?>>();
 		for(Sprite s : this.model.getWorld().getSprites()) {
 			if(s instanceof Player) {
 				objects.add(new PlayerView((Player)s));
 			}else{
 				objects.add(new SpriteView(s));
 			}
 		}
 		for(Projectile p : this.model.getWorld().getProjectiles()) {
 			objects.add(new ProjectileView(p));
 		}
 	}
 	
 	/*
 	 * Creates the list which holds all the renderers of the tiles.
 	 */
 	private void initTileList() {
 		tiles = new TileView[model.getWorld().getTiles().length][model.getWorld().getTiles()[0].length];
 		for(int i = 0; i < model.getWorld().getTiles().length; i++) {
 			for(int j = 0; j < model.getWorld().getTiles()[i].length; j++) {
 				tiles[i][j] = new TileView(model.getWorld().getTiles()[i][j]);
 			}
 		}
 	}
 
 	/**
 	 * Draws everything.
 	 */
 	@Override
 	public void paintComponent(Graphics g) {	
 		//super.paintComponent(g);
 						
 		//Draws all the static world objects.
 		for(int i = 0; i < model.getWorld().getTiles().length; i++) {
 			for(int j = 0; j < model.getWorld().getTiles()[i].length; j++) {
 				tiles[i][j].render(g, camera.getOffset(), camera.getScale());
 			}
 		}
 		
 		//Sets the player to the center of the screen.
 		camera.setToCenter(model.getPlayer().getCenter(), getSize());
 		//Draws all the dynamic items.
 		for(ObjectRenderer<?> or : objects) {
 			or.render(g, camera.getOffset(), camera.getScale());
 		}
 		//data:
 		g.setColor(new Color(255, 255, 255, 150));
 		g.fillRect(0, 0, 600, 200);
 		g.setColor(Color.BLACK);
 		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
 		g.drawString("World size: " + model.getWorld().getTiles().length + "x" + model.getWorld().getTiles()[0].length, 10, 20);
 		g.drawString("Number of updates since start (ctr): " + controller.getNbrOfUpdates() 
 				+ ", average: " + controller.getNbrOfUpdates()/(int)(controller.getMsSinceStart()/1000) + "/s", 10, 40);
 		g.drawString("Number of updates since start (view): " + tick 
 				+ ", average: " + tick/(int)(controller.getMsSinceStart()/1000) + "/s", 10, 60);
		g.drawString("Number of projectiles in model: " + model.getWorld().getProjectiles().size(), 10, 80);
 		g.drawString("Number of characters/sprites: " + model.getWorld().getSprites().size(), 10, 100);
 		g.drawString("Time: " + (int)(controller.getMsSinceStart()/1000) + " s", 10, 120);
 		g.drawString("Number of ObjectRenderers: " + this.objects.size(), 10, 140);
 		//TODO ta bort det mest ver....................
 	}
 	
 	/**
 	 * Draws the collision box.
 	 * @param g the graphics instance to use when drawing.
 	 * @param scale the scale to draw at.
 	 * @param box the collision box to draw.
 	 * @param color the colour to draw in.
 	 * @param renderPosition specify if the position of each line should be marked.
 	 * @param colourPosition the colour of the position mark.
 	 */
 	public static void renderCollisionBox(Graphics g, model.geometrical.Position pos, int scale, CollisionBox box, Color colour, 
 			boolean renderPosition, Color colourPosition) {
 		g.setColor(colour);
 		if(box != null) {
 			for(java.awt.geom.Rectangle2D r : box.getRectangles()) {
 				g.fillRect((int)(r.getX() * scale + pos.getX()), (int)(r.getY() * scale  + pos.getY()), 
 						(int)(r.getWidth() * scale), (int)(r.getHeight() * scale));
 			}
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if(e.getPropertyName().equals(GameModel.ADDED_SPRITE)) {
 			System.out.println("Added sprite caught by GamePanel");
 			if(e.getNewValue() instanceof Player) {
 				this.objects.add(new PlayerView((Player)e.getNewValue()));
 			}else{
 				this.objects.add(new SpriteView((Sprite)e.getNewValue()));
 			}
 		}else if(e.getPropertyName().equals(GameModel.REMOVED_SPRITE) ||
 				e.getPropertyName().equals(GameModel.REMOVED_PROJECTILE)) {
 			System.out.println("Removed sprite caught by GamePanel");
 			for(ObjectRenderer<?> or : this.objects) {
 				if(or.getObject() == e.getOldValue()) {
 					objects.remove(or);
 					break;
 				}
 			}
 		}else if(e.getPropertyName().equals(GameModel.ADDED_PROJECTILE)) {
 			System.out.println("Added projectile caught by GamePanel");
 			this.objects.add(new ProjectileView((Projectile)e.getNewValue()));
 		}
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		this.controller.handleMouseAt((float)(e.getX()-camera.getX())/camera.getScale(), 
 				(float)(e.getY()-camera.getY())/camera.getScale());
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		this.controller.handleMouseAt((float)(e.getX()-camera.getX())/camera.getScale(), 
 				(float)(e.getY()-camera.getY())/camera.getScale());
 	}
 }
