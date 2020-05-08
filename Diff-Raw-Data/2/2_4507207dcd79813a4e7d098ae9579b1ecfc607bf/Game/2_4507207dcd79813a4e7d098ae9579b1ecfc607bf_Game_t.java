 package main;
 
 import objects.GameObject;
 import objects.Player;
 
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Transform;
 
 public class Game extends BasicGame{
	private final Color background = new Color(130,130,130);
 	private final float speed = 4f;
 	private final float jumpSpeed = 10f;
 	private final float friction = 0.88f;
 //	private final float gravity = 0.001f;
 	private final float gravity = 0.5f;
 	private final float maxSpeed = 20;
 	
 	int tick = 0;
 	int lastTick = 0;
 	int lastJump = 0;
 	private float xGravity;
 	private float yGravity;
 	
 	public boolean change = false;
 	private GameObject[] objects;
 	private Player p;
 	public Game() {
 		super("Gravimax");
 	}
 	
 	public void run() throws SlickException{
 
 		AppGameContainer app = new AppGameContainer(new Game());
 		app.setTargetFrameRate(60);
 		app.setDisplayMode(800,800, false);
 		app.start();
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		System.out.println("Initializing");
 		
 		p = new Player(new Rectangle(400,600,50,50));
 		
 		xGravity = 0f;
 		yGravity = gravity;
 		
 		objects = new GameObject[20];
 		objects[0] = new GameObject(new Rectangle(50,750,700,50),Color.blue,true);
 		objects[1] = new GameObject(new Rectangle(0,50,50,700),Color.red,true);
 		objects[2] = new GameObject(new Rectangle(50,0,700,50),Color.green,true);
 		objects[3] = new GameObject(new Rectangle(750,50,50,700),Color.yellow,true);
 		objects[4] = p;
 		objects[5] = new GameObject(new Rectangle(500,600,50,50), Color.black);
 		objects[6] = new GameObject(new Rectangle(150,560,50,90),Color.black);
 		
 	}
 	
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		g.setBackground(background);
 		
 		// Draw Borders
 		for (GameObject b : objects){
 			if(b != null){
 				b.draw(g);
 			}
 		}
 		
 		p.draw(g);
 		
 		g.drawLine(195, 0, 195, 800);
 		g.drawLine(265, 0, 265, 800);
 		g.drawLine(285, 0, 285, 800);
 		g.drawLine(240, 0, 240, 800);
 	}
 	
 	
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException {
 		tick++;
 		// Print debug output
 		if(gc.getInput().isKeyDown(Input.KEY_ESCAPE) && (tick - lastTick > 30 || lastTick == 0)){
 			System.out.println(p.getX()+"/"+p.getY());
 			lastTick = tick;
 		}
 		
 		// Handle movement
 		if(gc.getInput().isKeyDown(Input.KEY_A)){
 			if(yGravity != 0){
 				p.setSpeedX(-speed * Math.signum(yGravity));
 			}
 			if(xGravity != 0){
 				p.setSpeedY(speed * Math.signum(xGravity));
 			}
 		} else if(gc.getInput().isKeyDown(Input.KEY_D)){
 			if(yGravity != 0){
 				p.setSpeedX(speed * Math.signum(yGravity));
 			}
 			if(xGravity != 0){
 				p.setSpeedY(-speed * Math.signum(xGravity));
 			}
 		}
 		if(gc.getInput().isKeyDown(Input.KEY_SPACE) && !p.isJumping() && (tick - lastJump >= 20 || lastJump == 0)){
 			if(yGravity > 0){
 				p.setSpeedY(-jumpSpeed);
 			} else if(yGravity < 0){
 				p.setSpeedY(jumpSpeed);
 			}
 			if(xGravity > 0){
 				p.setSpeedX(-jumpSpeed);
 			} else if(xGravity < 0){
 				p.setSpeedX(jumpSpeed);
 			}
 			p.setJumping(true);
 			lastJump = tick;
 		}
 		
 		
 		
 		// Move player
 		p.setX(p.getX() + p.getSpeedX());
 		p.setY(p.getY() + p.getSpeedY());
 		
 		
 		// Handle all collisions
 		GameObject collisionObject = null;
 		GameObject collisionObject2 = null;
 		int dir = 1;
 		int dir2 = 1;
 		
 		// Loop through all objects and check for collision with player
 		for(GameObject obj : objects){
 			if(obj != null && obj != p){
 				if(p.getSpeedX() < 0 && p.collideLeft(obj) && Math.abs(p.getX() - (obj.getX() + obj.shape.getWidth() * 1)) < 50){ // Player collided with an object to the left
 					System.out.println("pX: " + p.getX());
 					System.out.println("obj width: "+ obj.shape.getWidth());
 					System.out.println("obj X: "+ obj.getX());
 					p.setSpeedX(0);
 					if(xGravity < 0){
 						System.out.println("Fixed jumping");
 						p.setJumping(false);
 					}
 					if(obj.isColored()){
 						rotateGravity(gc, 1);
 						collisionObject2 = obj;
 						dir2 = -1;
 					} else {
 						yGravity = 0;
 						xGravity = -gravity;
 						collisionObject = obj;
 						dir = 1;
 					}
 				} else if(p.getSpeedX() > 0 && p.collideRight(obj) && Math.abs(p.getX() - (obj.getX() + obj.shape.getWidth() * -1)) < 50){ // Player collided with something to the right
 					p.setSpeedX(0);
 					if(xGravity > 0){
 						p.setJumping(false);
 					}
 					if(obj.isColored()){
 						rotateGravity(gc, 3);
 						collisionObject2 = obj;
 						dir2 = -1;
 					} else {
 						yGravity = 0;
 						xGravity = gravity;
 						collisionObject = obj;
 						dir = -1;
 					}
 //					System.out.println("Collided right");
 				} 
 				
 				if(p.getSpeedY() > 0 && p.collideDown(obj) && Math.abs(p.getY() - (obj.getY() + (obj.shape.getHeight()/2 + p.shape.getWidth()/2) * -1)) < 20){ // Player collided with an object below
 					collisionObject2 = obj;
 					dir2 = -1;
 					p.setSpeedY(0);
 					if(yGravity > 0){
 						p.setJumping(false);
 					}
 					xGravity = 0;
 					yGravity = gravity;
 //					System.out.println("Collided down");
 				} else if(p.getSpeedY() < 0 && p.collideUp(obj) && Math.abs(p.getY() - (obj.getY() + (obj.shape.getHeight()/2 + p.shape.getHeight() / 2) * 1)) < 20){ // Player collided with an object above
 					p.setSpeedY(0);
 					if(yGravity < 0){
 						p.setJumping(false);
 					}
 					collisionObject2 = obj;
 					if(obj.isColored()){
 						System.out.println("Collided upwards");
 						rotateGravity(gc, 2);
 						
 						dir2 = -1;
 					} else {
 						xGravity = 0;
 						yGravity = -gravity;
 						
 						
 					}
 //					System.out.println("Collided up");
 				}
 					
 			}
 		}
 
 		// Correct the collision so it doesn't go through solid objects but only do it if the distance is not too long (to prevent collision with several sides of one object)
 		if(collisionObject != null){
 			System.out.println("Object X: " + collisionObject.getX());
 			System.out.println("Width: " + collisionObject.shape.getWidth());
 			System.out.println("Dir: " + dir);
 			System.out.println("playerX : " + p.getX() + "/" + p.getY());
 			p.setX(collisionObject.getX() + (collisionObject.shape.getWidth()/2 + p.shape.getWidth() / 2)* dir);
 			System.out.println("Corrected playerX to: " + p.getX() + "/" + p.getY());
 		}
 		if(collisionObject2 != null){
 //			System.out.println("Object Y: " + collisionObject2.getY());
 //			System.out.println("Height: " + collisionObject2.shape.getHeight());
 //			System.out.println("Dir2: " + dir2);
 			p.setY(collisionObject2.getY() + (collisionObject2.shape.getHeight()/2 + p.shape.getHeight() / 2) * dir2);
 //			System.out.println("Corrected playerY to: " + p.getX() + "/" + p.getY());
 		}
 		
 		// Handle friction and gravity
 		if(xGravity == 0){
 			p.setSpeedX(p.getSpeedX() * friction);
 		}
 		if(yGravity == 0) {
 			p.setSpeedY(p.getSpeedY() * friction);
 		}
 		p.setSpeedX(Math.max(-maxSpeed, Math.min(maxSpeed,p.getSpeedX() + xGravity)));
 		p.setSpeedY(Math.max(-maxSpeed, Math.min(maxSpeed,p.getSpeedY() + yGravity)));
 //		System.out.println(p.getSpeedY());
 	}
 	
 	public void rotateGravity(GameContainer gc, float side){
 		xGravity = 0;
 		yGravity = gravity;
 		for(GameObject obj : objects){
 			if(obj != null){
 				obj.setSpeedX(0);
 				obj.setSpeedY(0);
 				if(side == 2){
 					obj.setX(gc.getWidth() - obj.getX());
 					obj.setY(gc.getHeight() - obj.getY());
 					obj.shape = obj.shape.transform(Transform.createRotateTransform((float) (Math.PI)));
 				} else {
 					// Magic formula for rotating entire screen
 					float tempX = obj.getX();
 					float tempY = obj.getY();
 				
 					obj.setX(tempY * (-side + 2) + gc.getWidth() * (1/2f*side - 1/2f));
 					obj.setY(tempX * (side - 2) + gc.getHeight() * (-1/2f*side + 3/2f));
 					obj.shape = obj.shape.transform(Transform.createRotateTransform((float) (Math.PI/2)));
 					if(obj.equals(p)){
 						System.out.println("Yes, this is player");
 						System.out.println(obj.shape.getX() + "/" + obj.shape.getY());
 					}
 					System.out.println("Converted from: " + tempX + "/" + tempY + " to: " + obj.getX() + "/" + obj.getY());
 				}
 			}
 		}
 	}
 }
