 import static org.lwjgl.opengl.GL11.*;
  
 import org.lwjgl.opengl.*;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.*;
  
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
  
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 import de.matthiasmann.twl.utils.PNGDecoder;
  
 public class Game{
 	private ArrayList<Entity> entities = new ArrayList<Entity>();
 	private ArrayList<Entity> removeList = new ArrayList<Entity>();
     public Map map;
     private MyTank player;
     static private int camera_x,camera_y,camera_w,camera_h;
     static final int WORLD_W,WORLD_H;
     private float gunRotation = 0;
     float bodyAng = 0;
     private int maxSpeed = 20,speed = 2;
     private boolean keyControlRelease;
     private static long timerTicksPerSecond = Sys.getTimerResolution();
     private long lastLoopTime = getTime();
     private long lastFpsTime;
     private int fps;
     
     protected long firingInterval = 100;	// ms
 	protected long lastFire;
 	private int bulletIndex = 0;
 	private Bullet[]	bullets;
     
     static{
     	//initial world size
         WORLD_W = 1920;
         WORLD_H = 1200;
     }
    
     public Game(){
     	//initialed value of camera
     	camera_x = 0;
         camera_y = 0;
         camera_w = 640;
         camera_h = 480;
         
         try {
 			Display.setIcon(new ByteBuffer[] {
 			        loadIcon(getClass().getResource("gameIcon.png"))  // "bin/gameIcon.png" size 32x32
 			    });
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
         //Mouse.setGrabbed(true);
 
         //initial display
         try {
             Display.setDisplayMode(new DisplayMode(camera_w, camera_h));
             Display.create();
         } catch (LWJGLException e) {
         	e.printStackTrace();
         }
         entities.clear();
         
         // set game
         map = new Map();
         player = new MyTank(this);
         player.setPositionToMap(2,3);
         Brick[] brick = new Brick[11];
         for(int i = 0;i < brick.length;i++){
         	brick[i] = new Brick(this);
         	brick[i].setPositionToMap(i+3, 3);
         	entities.add(brick[i]);
         }
         entities.add(player);
        bullets = new Bullet[20];
 		for (int i = 0; i < bullets.length; i++) {
 			bullets[i] = new Bullet(this);
 		}
                
         //initialization opengl code
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
         //glOrtho(0 ,640 ,480 ,0 ,-1 , 1);
         glOrtho(camera_x ,640+camera_x ,480+camera_y ,camera_y ,-1 , 1);
         glMatrixMode(GL_MODELVIEW);
         glEnable(GL_TEXTURE_2D);
         
         glEnable(GL_BLEND);
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
         while(!Display.isCloseRequested()){
         	//clear screen
             glClear(GL_COLOR_BUFFER_BIT);
             
             long delta = getTime() - lastLoopTime;
     		lastLoopTime = getTime();
     		lastFpsTime += delta;
     		fps++;
 
     		// update our FPS
     		if (lastFpsTime >= 1000) {
     			Display.setTitle("Awesome Tank (FPS: " + fps + ")");
     			lastFpsTime = 0;
     			fps = 0;
     		}
            
             /*sky.bind();
             glBegin(GL_QUADS);
             	glTexCoord2f(0,0);
             	glVertex2f(0 ,0);//upper left
             	glTexCoord2f(1,0);
             	glVertex2f(WORLD_W ,0);//upper right
             	glTexCoord2f(1,1);
             	glVertex2f(WORLD_W ,WORLD_H);//bottom right
             	glTexCoord2f(0,1);
             	glVertex2f(0 ,WORLD_H);//bottom left
             glEnd();*/
             
             input();
             for ( Entity entity : entities ) {
             	if(entity instanceof MyTank)
             		player.move(delta, bodyAng);
             	else
             		entity.move(delta);
             	if(entity instanceof Bullet){
 					if(map.blocked((int)entity.x/map.TILE_SIZE, (int)entity.y/map.TILE_SIZE)){
 						((Bullet) entity).reinitialize((float)(player.x-Math.cos(0.0174532925*gunRotation)*player.width/1.5), (float)(player.y-Math.sin(0.0174532925*gunRotation)*player.height/1.5),((Bullet)entity).getMoveSpeed(), ((Bullet)entity).getMoveSpeed());
 						removeList.add(entity);
 					}
 				}
 			}
             
             // check collisions and move
     		for (int p = 0; p < entities.size(); p++) {
     			for (int s = p + 1; s < entities.size(); s++) {
     				Entity me = entities.get(p);
     				Entity him = entities.get(s);
     				if (me.collidesWith(him)) {
     					me.collidedWith(him);
     					him.collidedWith(me);
     					if(me.getClass() == MyTank.class){
     						((MyTank)me).moveBack();
     					}
     				}
     			}
     		}
             
             //set camera
             setCamera();
             glMatrixMode(GL_PROJECTION);
             glLoadIdentity();
             glOrtho(camera_x ,640+camera_x ,480+camera_y ,camera_y ,-1 , 1);
             
     		entities.removeAll(removeList);
     		removeList.clear();
     		
             //draw
             map.draw();
             for ( Entity entity : entities ) {
             	if(entity instanceof MyTank)
             		player.draw();
             	else
             		entity.draw();
 			}
             
             Display.update();
             Display.sync(60);
         }
         Display.destroy();
     }
    
     private void input() {
     	boolean KEY_W = Keyboard.isKeyDown(Keyboard.KEY_W);
     	boolean KEY_S = Keyboard.isKeyDown(Keyboard.KEY_S);
     	boolean KEY_D = Keyboard.isKeyDown(Keyboard.KEY_D);
     	boolean KEY_A = Keyboard.isKeyDown(Keyboard.KEY_A);
     	
         if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
         	Display.destroy();
             System.exit(0);
         }
         
         /*if(keyControlRelease) {
         	//Display.setTitle("ss");
         	if(speed > 0)
         		speed -= 5;
         }*/
         
         /*while (Keyboard.next()) {
             if (Keyboard.getEventKeyState()) {
                 if (Keyboard.getEventKey() == Keyboard.KEY_W) {
                 	//########################################
                 	if(speed < maxSpeed)
                     	speed += 5;
                     box.move(0, -speed);
                     keyControlRelease = false; 
                 	Display.setTitle("w pres");
                 	player.move(0, -5);
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_S) {
                 	keyControlRelease = false;
                 	player.move(0, 5);
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_D) {
                 	keyControlRelease = false;
                 	player.move(5, 0);
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                 	keyControlRelease = false;
                 	player.move(-5, 0);
                 }
             }
             else {
                 if (Keyboard.getEventKey() == Keyboard.KEY_W) {
                 	Display.setTitle("w release");
                 	keyControlRelease = true;
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_S) {
                 	keyControlRelease = true;
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_D) {
                 	keyControlRelease = true;
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                 	keyControlRelease = true;
                 }
             }
         }*/
         
         player.setDX(0);
         player.setDY(0);
         if(KEY_W && !KEY_S){
         	bodyAng = 90;
         	if(KEY_A)
         		bodyAng = 45;
         	if(KEY_D)
         		bodyAng = 135;
         	player.setDY(-speed);
         }
         if(KEY_S && !KEY_W){
         	bodyAng = 270;
         	if(KEY_A)
         		bodyAng = 315;
         	if(KEY_D)
         		bodyAng = 225;
         	player.setDY(speed);
         }
         if(KEY_D && !KEY_A){
         	bodyAng = 180;
         	if(KEY_W)
         		bodyAng = 135;
         	if(KEY_S)
         		bodyAng = 225;
         	player.setDX(speed);
         }
         if(KEY_A && !KEY_D){
         	bodyAng = 0;
         	if(KEY_W)
         		bodyAng = 45;
         	if(KEY_S)
         		bodyAng = 315;
         	player.setDX(-speed);
         }
         
         if(Mouse.isButtonDown(0)){
         	//player.Fire();
         	if (System.currentTimeMillis() - lastFire < firingInterval) {
     			return;
     		}
     		lastFire = System.currentTimeMillis();
     		Bullet bullet = bullets[bulletIndex ++ % bullets.length];
     		bulletIndex %= bullets.length;
     		bullet.reinitialize((float)(player.x-Math.cos(0.0174532925*gunRotation)*player.width/1.5), (float)(player.y-Math.sin(0.0174532925*gunRotation)*player.height/1.5),(float)-Math.cos(0.0174532925*gunRotation)*bullet.dx, (float)-Math.sin(0.0174532925*gunRotation)*bullet.dy);
     		entities.add(bullet);
     		//soundManager.playEffect(SOUND_SHOT);
         }
 	}
     
     private void setCamera(){
     	// mouse position (0,0) at center screen
     	float mouseX = (Mouse.getX() - camera_w/2)*0.5f;
     	float mouseY = (Mouse.getY() - camera_h/2)*0.5f;
     	
     	String str = String.valueOf(gunRotation);
     	
     	// Mouse.getX(),Mouse.getY() position (0,0) at bottom left
     	//Display.setTitle(str);
     	//gunRotation = 57.2957795f*(float)Math.atan2(camera_h/2 - Mouse.getY(),Mouse.getX() - camera_w/2);
     	gunRotation = 57.2957795f*(float)Math.atan2(camera_h/2 - Mouse.getY(),Mouse.getX() - camera_w/2);
     	gunRotation += 180;
     	player.setGunAngle(gunRotation);
 
     	camera_x = (int)mouseX + (int)(player.get_centerX() - camera_w/2);
     	camera_y = -(int)mouseY + (int)(player.get_centerY() - camera_h/2);
     }
     
     public void addEntity(Entity entity) {
     	entities.add(entity);
 	}
     
     public void removeEntity(Entity entity) {
 		removeList.add(entity);
 	}
     
     public static long getTime() {
 		return (Sys.getTime() * 1000) / timerTicksPerSecond;
 	}
 
 	private ByteBuffer loadIcon(URL url) throws IOException {
         InputStream is = url.openStream();
         try {
             PNGDecoder decoder = new PNGDecoder(is);
             ByteBuffer bb = ByteBuffer.allocateDirect(decoder.getWidth()*decoder.getHeight()*4);
             decoder.decode(bb, decoder.getWidth()*4, PNGDecoder.Format.RGBA);
             bb.flip();
             return bb;
         } finally {
             is.close();
         }
     }
    
     public static void main(String[] args) {
             new Game();
     }
 }
