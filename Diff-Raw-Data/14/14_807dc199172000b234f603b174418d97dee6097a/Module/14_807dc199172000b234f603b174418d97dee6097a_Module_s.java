 package edu.acmX.exhibit.brickbreaker;
 
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import processing.core.PImage;
 
 import edu.mines.acmX.exhibit.input_services.events.EventManager;
 import edu.mines.acmX.exhibit.input_services.events.EventType;
 import edu.mines.acmX.exhibit.input_services.hardware.BadFunctionalityRequestException;
 import edu.mines.acmX.exhibit.input_services.hardware.DeviceConnectionException;
 import edu.mines.acmX.exhibit.input_services.hardware.HardwareManager;
 import edu.mines.acmX.exhibit.input_services.hardware.HardwareManagerManifestException;
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.HandTrackerInterface;
 import edu.mines.acmX.exhibit.module_management.modules.ProcessingModule;
 
 public class Module extends ProcessingModule {
 
 	public static int BACKGROUND_COLOR;
 	public static float PADDLE_START_X;
 	public static float PADDLE_START_Y;
 	public static float PADDLE_WIDTH;
 	public static float PADDLE_HEIGHT;
 	public static float PROJECTILE_START_X;
 	public static float PROJECTILE_START_Y;
 	public static float PROJECTILE_LENGTH;
 	public static float BRICK_WIDTH;
 	public static float BRICK_HEIGHT;
 	public static float BRICK_VERTICAL_SPACING;
 	public static float BRICK_HORIZONTAL_SPACING;
 	public static Rectangle2D BRICK_BOUNDS;
 	public static float LIVES_DISP_X;
 	public static float LIVES_DISP_Y;
 	public static float LIVES_SPACING;
 	public static final int START_LIVES = 3;
 	public static final int BRICK_POINT = 10;
 	
 	private List<List<Brick>> bricksList;
 	private Paddle paddle;
 	private Projectile projectile;
 	private int lives;
 	private boolean lost;
 	private int aliveBricks;
 	private int endTime;
 	private VirtualRectClick end;
 	private VirtualRectClick playAgain;
 	
 	private static HardwareManager hardwareManager;
 	private static EventManager eventManager;
 	private HandTrackerInterface driver;
 	private MyHandReceiver receiver;
 	
 	private float handX;
 	private float handY;
 	
 	public static final String CURSOR_FILENAME = "hand_cursor.png";
 	private PImage cursor_image;
 	
 	private int points;
 	
 	public void setup() {
 		generateConstants();
 		size(width, height);
 		BACKGROUND_COLOR = color(0, 0, 139);
 		paddle = new Paddle(this, PADDLE_START_X, PADDLE_START_Y , PADDLE_WIDTH, PADDLE_HEIGHT);
 		handX = PADDLE_START_X;
 		projectile = spawnProjectile();
 		lives = START_LIVES;
 		lost = false;
 		bricksList = populateBricks();
 		end = new VirtualRectClick(1000, 3 * width / 5, 3 * height/ 5, width / 5, height /5);
 		playAgain = new VirtualRectClick(1000, width / 5, 3 * height / 5, width / 5, height / 5);
 		noCursor();
 		cursor_image = loadImage(CURSOR_FILENAME, "edu.acmX.exhibit.brickbreaker");
 		cursor_image.resize(32, 32);
 		points = 0;
 		registerTracking();
 	}
 	
 	public void draw() {
 		update();
 		background(BACKGROUND_COLOR);
 		paddle.draw();
 		projectile.draw();
 		drawBricks();
 		drawLives();
 		drawScore();
 		if (lives < 0) {
 			drawGameOver();
 		}
 
 	}
 	
 	public void update() {
 		// update hands 
 		driver.updateDriver();
 		if (receiver.whichHand() != -1) {			
 			handX = receiver.getX() * (2 + (width / 640));
 			handY = receiver.getY() * (2 + (height / 480));
 			handX -= 300;
 			handY -= 300;
 			// TODO find a better place for this function vvvv
 			if (projectile.getVelocityX() == 0 && projectile.getVelocityY() == 0) {
 				projectile.startMoving(); 
 			}
 		}
 		else if (receiver.whichHand() == -1 && projectile.getVelocityX() != 0 && projectile.getVelocityY() != 0) {
 			projectile.stopMoving();
 		}
 		paddle.update();
 		projectile.update();
 		checkCollisions();
 		checkForDeadProjectiles();
 
 		if (aliveBricks <= 0) {
 			++lives;
 			projectile = spawnProjectile();
 			bricksList = populateBricks();
 		}
 		if (lives < 0) {
 			//cursor();
 			end.update((int) handX, (int) handY, millis());
 			playAgain.update((int) handX, (int) handY, millis());
 			if(end.durationCompleted(millis())) {
 				exit();
 			}
 			else if(playAgain.durationCompleted(millis())) {
 				noCursor();
 				lives = START_LIVES;
 				bricksList = populateBricks();
 				projectile = spawnProjectile();
 				paddle.setX(PADDLE_START_X);
 				points = 0;
 			}
 		}
 	}
 	
 	// checks all collisions against the projectile
 	public void checkCollisions() {
 		// check collision with paddle
 		Rectangle2D intersect = projectile.getRect().createIntersection(paddle.getRect());
 		if (!intersect.isEmpty()) {
 			if (intersect.getWidth() < intersect.getHeight()) {
 				projectile.reverseXDirection();
 			}
 			else if (intersect.getWidth() > intersect.getHeight()) {
 				projectile.reverseYDirection();
 			}
 		}
 		// check collisions with all bricks
 		for(List<Brick> list : bricksList) {
 			for(Brick brick : list) {
 				Rectangle2D brickIntersect = projectile.getRect().createIntersection(brick.getRect());
 				if(!brickIntersect.isEmpty() && !brick.isDead()) {
 					if (brickIntersect.getWidth() < brickIntersect.getHeight()) {
 						projectile.reverseXDirection();
 					}
 					else if (brickIntersect.getWidth() > brickIntersect.getHeight()) {
 						projectile.reverseYDirection();
 					}
 					brick.kill();
 					--aliveBricks;
 					points += BRICK_POINT;
 				}
 			}
 		}
 	}
 	
 	public void checkForDeadProjectiles() {
 		if (projectile.isDead()) {
 			projectile = spawnProjectile();
 			--lives;
 		}
 	}
 	
 	public Projectile spawnProjectile() {
 		return new Projectile(this, PROJECTILE_START_X, PROJECTILE_START_Y, PROJECTILE_LENGTH);
 	}
 	
 	public void generateConstants() {
 		PADDLE_START_X =  width / 2;
 		PADDLE_START_Y = height - (height / 20);
 		PADDLE_WIDTH = width / 8;
 		PADDLE_HEIGHT = height / 25;
 		PROJECTILE_START_X =  width / 30;
 		PROJECTILE_START_Y = height / 2;
 		PROJECTILE_LENGTH = width / 40;
 		BRICK_WIDTH = width / 20;
 		BRICK_HEIGHT = height / 40;
 		BRICK_VERTICAL_SPACING = height / 40;
 		BRICK_HORIZONTAL_SPACING = width / 60;
 		BRICK_BOUNDS = new Rectangle2D.Float(width / 30, height / 30, width - width / 30, height / 3);
 		LIVES_DISP_X = 0;
 		LIVES_DISP_Y = 0;
 		LIVES_SPACING = height / 60;
 	}
 	
 	public List<List<Brick>> populateBricks() {
 		aliveBricks = 0;
 		ArrayList<List<Brick>> listOfLists = new ArrayList<List<Brick>>(); 
 		int x = (int) BRICK_BOUNDS.getMinX();
 		int y = (int) BRICK_BOUNDS.getMinY();
 		while(y < BRICK_BOUNDS.getHeight() + BRICK_BOUNDS.getMinY() - BRICK_HEIGHT) {
 			x = (int) BRICK_BOUNDS.getMinX();
 			ArrayList<Brick> row = new ArrayList<Brick>(); 
 			int color = color((int) random(256), (int) random(256), (int) random(256));
 			while(x < BRICK_BOUNDS.getWidth() + BRICK_BOUNDS.getMinX()- BRICK_WIDTH) {
 				row.add(new Brick(this, x, y, BRICK_WIDTH, BRICK_HEIGHT, color));
 				x += BRICK_WIDTH + BRICK_HORIZONTAL_SPACING;
 			}
 			aliveBricks += row.size();
 			listOfLists.add(row);
 			y += BRICK_HEIGHT + BRICK_VERTICAL_SPACING;
 		}
 		return listOfLists;
 	}
 	
 	public void drawBricks() {
 		for(List<Brick> list : bricksList) {
 			for(Brick brick : list) {
 				if(!brick.isDead()) {
 					brick.draw();
 				}
 			}
 		}
 	}
 	
 	public void drawLives() {
 		fill(Projectile.COLOR);
 		for(int i = 0; i < lives; i++) {
 			rect(LIVES_DISP_X, LIVES_DISP_Y + (i * (LIVES_SPACING + (projectile.getLength() / 2))), projectile.getLength() / 2, projectile.getLength() / 2);
 		}
 	}
 	
 	public void drawGameOver() {
 			fill(projectile.getColor());
 			rect(0, 0, width, height);
 			fill(255, 69, 0);
 			textSize(width / 8);
 			//rectMode(CENTER);
 			text("GAME OVER", width / 10, height / 3);
 			stroke(0);
 			strokeWeight(4);
 			fill(255, 0, 0);
 			rect(end.getX(), end.getY(), end.getWidth(), end.getHeight(), end.getWidth() / 6, end.getHeight() / 6);
 			fill(50, 205, 50);
 			rect(playAgain.getX(), playAgain.getY(), playAgain.getWidth(), playAgain.getHeight(), playAgain.getWidth() / 6, playAgain.getHeight() / 6);
 			noStroke();
 			image(cursor_image, handX, handY);
 
 	}
 	
 	public void drawScore() {
 		fill(255, 215, 0);
 		textSize(32);
 		text("" + points, 19 * width / 20, height / 20);
 	}
 	
 	public void registerTracking() {
 		// hardware stuff
 		try {
 			hardwareManager = HardwareManager.getInstance();
 		} catch (HardwareManagerManifestException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DeviceConnectionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ArrayList<String> drivers;
 		try {
 			drivers = (ArrayList) hardwareManager.getDevices("handtracking");
 			driver = (HandTrackerInterface) hardwareManager.inflateDriver(
 					drivers.get(0), "handtracking");
 
 		} catch (BadFunctionalityRequestException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		eventManager = EventManager.getInstance();
 		receiver = new MyHandReceiver();
 		eventManager.registerReceiver(EventType.HAND_CREATED, receiver);
 		eventManager.registerReceiver(EventType.HAND_UPDATED, receiver);
 		eventManager.registerReceiver(EventType.HAND_DESTROYED, receiver);
 	}
 	
 	public float getHandX() {
 		return handX;
 	}
 	
 	public float getHandY() {
 		return handY;
 	}
 }
