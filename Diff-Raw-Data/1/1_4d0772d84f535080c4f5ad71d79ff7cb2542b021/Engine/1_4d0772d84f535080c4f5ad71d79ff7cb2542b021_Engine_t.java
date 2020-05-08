 package fr.spirotron.planeshooter;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.image.BufferStrategy;
 import java.util.Iterator;
 
 import fr.spirotron.planeshooter.entities.AnimationEntityManager;
 import fr.spirotron.planeshooter.entities.Entity;
 import fr.spirotron.planeshooter.entities.EntityFactory;
 import fr.spirotron.planeshooter.entities.EntityFactory.EntityType;
 import fr.spirotron.planeshooter.entities.PlayerShotEntityManager;
 import fr.spirotron.planeshooter.entities.UserEntityManager;
 import fr.spirotron.planeshooter.utils.Bounds;
 
 
 public class Engine implements Runnable {
 	private static final Color BACKGROUND_COLOR = new Color(0x024994);
 	private static final int DISPLAY_BUFFER_COUNT = 2;
 	private static final int TICK = 10;
 	
 	private Dimension screenDimension;
 	private Canvas canvas;
 	private BufferStrategy bufferStrategy;
 	
 	private Entity player1Entity;
 	
 	
 	private boolean running;
 	
 	private UserEntityManager player1Manager;
 	private PlayerShotEntityManager player1ShotManager;
 	private AnimationEntityManager animationManager;
 	
 	private EntityFactory entityFactory;
 	
 	public Canvas createCanvas(Dimension screenDimension) {
 		System.out.println("Creating canvas...");
 		this.screenDimension = screenDimension;
 		
 		canvas = new Canvas();
 		canvas.setSize(screenDimension);
		canvas.setBackground(BACKGROUND_COLOR);
 		
 		player1Manager = new UserEntityManager(canvas);
 		player1ShotManager = new PlayerShotEntityManager(screenDimension);
 		animationManager = new AnimationEntityManager();
 		
 		return canvas;
 	}
 	
 	public void init() throws Exception {
 		System.out.println("Initializing engine...");
 		canvas.createBufferStrategy(DISPLAY_BUFFER_COUNT);
 		bufferStrategy = canvas.getBufferStrategy();
 		
 		entityFactory = new EntityFactory();
 		
 		player1Entity = entityFactory.activateEntity(EntityType.PLAYER1, player1Manager, animationManager);
 		player1ShotManager.setPlayerEntity(player1Entity);
 		
 		fillBackground();
 		start();
 	}
 
 	private void fillBackground() {
 		Graphics2D gfx = (Graphics2D)bufferStrategy.getDrawGraphics();
 		
 		gfx.setColor(BACKGROUND_COLOR);
 		gfx.fillRect(0, 0, screenDimension.width, screenDimension.height);
 		
 		gfx.dispose();
 		bufferStrategy.show();
 		Toolkit.getDefaultToolkit().sync();
 	}
 	
 	private void eraseEntities(Graphics2D gfx) {
 		for (Iterator<Entity> it=entityFactory.getActivatedEntities(); it.hasNext(); ) {
 			Entity entityToErase = it.next();
 			Dimension dim = entityToErase.getDimension();
 			Bounds bounds = entityToErase.getBounds();
 			
 			gfx.setColor(BACKGROUND_COLOR);
 			gfx.fillRect(bounds.left, bounds.top, dim.width, dim.height);
 		}
 	}
 	
 	private void updateStates() {
 		for (Iterator<Entity> it=entityFactory.getActivatedEntities(); it.hasNext(); ) {
 			Entity entity = it.next();
 			entity.update();
 			
 			switch (entity.getType()) {
 				case PLAYER1:
 					if (UserEntityManager.isFiring(player1Entity)) {
 						createShot(player1ShotManager);
 						UserEntityManager.stopFiring(player1Entity);
 					}
 					break;
 					
 				default:
 					break;
 			}
 		}
 	}
 	
 	private void releaseDeadEntities() {
 		for (Iterator<Entity> it=entityFactory.getActivatedEntities(); it.hasNext(); ) {
 			Entity entity = it.next();
 			
 			if (entity.isDead())
 				entityFactory.deactivate(entity.getId());
 		}
 	}
 	
 	private void drawEntities(Graphics2D gfx) {
 		for (Iterator<Entity> it=entityFactory.getActivatedEntities(); it.hasNext(); ) {
 			Entity entity = it.next();
 			
 			animationManager.update(entity);
 			entity.draw(gfx);
 		}
 	}
 	
 	private long update() {
 		long startTime = System.currentTimeMillis();
 		Graphics2D gfx = (Graphics2D)bufferStrategy.getDrawGraphics();
 		
 		eraseEntities(gfx);
 		updateStates();
 		
 		releaseDeadEntities();
 		drawEntities(gfx);
 		
 		gfx.dispose();
 		bufferStrategy.show();
 		Toolkit.getDefaultToolkit().sync();
 		
 		long totalTime = System.currentTimeMillis() - startTime;
 		System.out.println("Update done in "+totalTime+" ms.");
 		
 		return totalTime;
 	}
 	
 	private void createShot(PlayerShotEntityManager shotManager) {
 		entityFactory.activateEntity(EntityType.PLAYER1_SHOT, shotManager, animationManager);
 	}
 
 	private void start() {
 		new Thread(this, "gamethread").start();
 	}
 	
 	public void stop() {
 		running = false;
 	}
 	
 	public void run() {
 		running = true;
 		
 		while (running) {
 			long updateTime = update();
 			
 			if (updateTime < TICK) {
 				try {
 					Thread.sleep(TICK-updateTime);
 				} catch (InterruptedException e) {
 					throw new Error(e);
 				}
 			}
 		}
 	}
 }
