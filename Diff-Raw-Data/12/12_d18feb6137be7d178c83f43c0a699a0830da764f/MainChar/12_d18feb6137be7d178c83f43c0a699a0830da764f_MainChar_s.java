 package entities;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 import bullets.NormalBullet;
 
 public class MainChar {
 	
 	final float MAXSPEED = 0.3f; //Maximum speed
	final float ACCELERATION = 0.008f; //Acceleration rate
 	public final int BULLETDELAY = 500; //Bullet spawn delay
 	final float BULLETSPEED = 1f; //Bullet speed
 	public Image charsprite; //Character sprite
 	float scaledAccel; //Scaled acceleration
 	public int bulletdelta = 0; //Bullet spawn timer
 	public int hp; //Health
 	float speedx = 0;
 	float speedy = 0;
 	public float x,y; //Coordinates
 	
 	public MainChar(Image mainchar, int x, int y) throws SlickException{
 		//x,y are spawn coordinates
 		charsprite = mainchar;
 		this.x = x;
 		this.y = y;
 		hp = 20;
 	}
 	
 	public void draw(){
 		charsprite.draw(x,y);
 	}
 	
 	public void move(Input input, int delta, int max_x, int max_y){
 		//Lag jump fix sometimes causes problems; temporarily disabled
 		//This seems to help with lag jumps
 		/*
 		dcounter++;
 		if(dcounter/1000000 > 0){
 			deltanum = 0;
 			dcounter = 0;
 		}
 		if(deltanum < 10000){
 			delta *= deltanum;
 			delta += delta;
 			deltanum++;
 			delta /= deltanum;
 		}
 		*/
 		
		scaledAccel = ACCELERATION;
 		
 		//Key input adds acceleration value to speed
 		if(input.isKeyDown(Input.KEY_A) && input.isKeyDown(Input.KEY_D)){
 			slowDown();
 		}
 		else if(input.isKeyDown(Input.KEY_W) && input.isKeyDown(Input.KEY_S)){
 			slowDown();
 		}
 		else if(input.isKeyDown(Input.KEY_A) && input.isKeyDown(Input.KEY_W)){
 			if(speedx >= -MAXSPEED)
 				speedx -= scaledAccel/Math.sqrt(2);
 			if(speedy >= -MAXSPEED)
 				speedy -= scaledAccel/Math.sqrt(2);
 		}
 		else if(input.isKeyDown(Input.KEY_D) && input.isKeyDown(Input.KEY_W)){
 			if(speedx <= MAXSPEED)
 				speedx += scaledAccel/Math.sqrt(2);
 			if(speedy >= -MAXSPEED)
 				speedy -= scaledAccel/Math.sqrt(2);
 		}
 		else if(input.isKeyDown(Input.KEY_A) && input.isKeyDown(Input.KEY_S)){
 			if(speedx >= -MAXSPEED)
 				speedx -= scaledAccel/Math.sqrt(2);
 			if(speedy <= MAXSPEED)
 				speedy += scaledAccel/Math.sqrt(2);
 		}
 		else if(input.isKeyDown(Input.KEY_D) && input.isKeyDown(Input.KEY_S)){
 			if(speedx <= MAXSPEED)
 				speedx += scaledAccel/Math.sqrt(2);
 			if(speedy <= MAXSPEED)
 				speedy += scaledAccel/Math.sqrt(2);
 		}
 		else if(input.isKeyDown(Input.KEY_A)){
 			if(speedx >= -MAXSPEED)
 				speedx -= scaledAccel;
 			if(speedy > 0){
 				if(speedy - scaledAccel/2 <= 0)
 					speedy = 0;
 				else
 					speedy -= scaledAccel/2;
 			}
 			if(speedy < 0){
 				if(speedy + scaledAccel/2 >= 0)
 					speedy = 0;
 				else
 					speedy += scaledAccel/2;
 			}
 		}
 		else if(input.isKeyDown(Input.KEY_D)){
 			if(speedx <= MAXSPEED)
 				speedx += scaledAccel;
 			if(speedy > 0){
 				if(speedy - scaledAccel/2 <= 0)
 					speedy = 0;
 				else
 					speedy -= scaledAccel/2;
 			}
 			if(speedy < 0){
 				if(speedy + scaledAccel/2 >= 0)
 					speedy = 0;
 				else
 					speedy += scaledAccel/2;
 			}
 		}
 		else if(input.isKeyDown(Input.KEY_W)){
 			if(speedy >= -MAXSPEED)
 				speedy -= scaledAccel;
 			if(speedx > 0){
 				if(speedx - scaledAccel/2 <= 0)
 					speedx = 0;
 				else
 					speedx -= scaledAccel/2;
 			}
 			if(speedx < 0){
 				if(speedx + scaledAccel/2 >= 0)
 					speedx = 0;
 				else
 					speedx += scaledAccel/2;
 			}
 		}
 		else if(input.isKeyDown(Input.KEY_S)){
 			if(speedy <= MAXSPEED)
 				speedy += scaledAccel;
 			if(speedx > 0){
 				if(speedx - scaledAccel/2 <= 0)
 					speedx = 0;
 				else
 					speedx -= scaledAccel/2;
 			}
 			if(speedx < 0){
 				if(speedx + scaledAccel/2 >= 0)
 					speedx = 0;
 				else
 					speedx += scaledAccel/2;
 			}
 		}
 		else{
 			slowDown();
 		}
 		
 		//Position is updated according to speed and checked if outside level
 		if(x + speedx*delta >= 0 && x+speedx*delta <= max_x - charsprite.getWidth())
 			x += speedx*delta;
 		if(y + speedy*delta >= 0 && y+speedy*delta <= max_y - charsprite.getHeight())
 			y += speedy*delta;
 	}
 	
 	//Deceleration method
 	public void slowDown(){
 		if(speedx > 0){
 			if(speedx - scaledAccel/2 <= 0)
 				speedx = 0;
 			else
 				speedx -= scaledAccel/2;
 		}
 		if(speedx < 0){
 			if(speedx + scaledAccel/2 >= 0)
 				speedx = 0;
 			else
 				speedx += scaledAccel/2;
 		}
 		if(speedy > 0){
 			if(speedy - scaledAccel/2 <= 0)
 				speedy = 0;
 			else
 				speedy -= scaledAccel/2;
 		}
 		if(speedy < 0){
 			if(speedy + scaledAccel/2 >= 0)
 				speedy = 0;
 			else
 				speedy += scaledAccel/2;
 		}
 	}
 	
 	//Rotates the sprite to point towards the mouse
 	public void rotate(Input input){
 		float dx = x + charsprite.getWidth()/2 - input.getMouseX();
 		float dy = y + charsprite.getHeight()/2 - input.getMouseY();
 		float arctan;
 		if(dy > 0 && dx >= 0 || dy < 0 && dx >= 0)
 			arctan = (float)Math.toDegrees(Math.atan(dy/dx)) + 180;
 		else if(dy < 0 && dx < 0 || dy > 0 && dx < 0)
 			arctan = (float)Math.toDegrees(Math.atan(dy/dx));
 		else if (x > 0)
 			arctan = 90;
 		else if (x < 0)
 			arctan = -90;
 		else
 			arctan = 0;
 		charsprite.rotate(arctan - charsprite.getRotation()+90);
 	}
 	
 	public boolean shoot(Input input) throws SlickException{
 		return input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON);
 	}
 	
 	public Bullet spawnBullet(String bulletsprite) throws SlickException{
 		Bullet b = new NormalBullet("normal", "none",x + charsprite.getWidth()/2,y + charsprite.getHeight()/2,BULLETSPEED,charsprite.getRotation()-90, true);
 		return b;
 	}
 }
