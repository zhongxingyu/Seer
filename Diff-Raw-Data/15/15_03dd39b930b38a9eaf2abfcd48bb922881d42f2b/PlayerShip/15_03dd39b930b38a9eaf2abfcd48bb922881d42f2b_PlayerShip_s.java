 package org.soupware.slicktest;
 
 import java.util.ArrayList;
<<<<<<< HEAD
=======
 import java.util.List;
>>>>>>> eae0a53d92327dc2cf0dcf23854c7ebe71811abf
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 public class PlayerShip {
 	
 	int health = 5;
 	
 	float x = 400;
 	float y = 300;
 	float scale = 1.0f;
 	float fireRate = 50;
 	boolean isWhite = true;
 	
 	Image ship = null;
 	Color shipColor = new Color(255,255,255);
 	
 	Bubble bubble = null;
 	boolean isBubbled = false;
 	
 	List<Bullets> bullets = new ArrayList<Bullets>();
 	Rectangle hb = null;
 	
 	boolean isPressed = false;
 	ArrayList<Integer> pressed = new ArrayList<Integer>();
 	
 	
 	public PlayerShip() throws SlickException{
 		ship = new Image("data/img/shipwhite.png");
 		bullets.add(new Bullets(x,y));
 		hb = new Rectangle(x+6, y, 25, 10);
 	}
 	
 	public void move(Input input, int key, int delta) throws SlickException{
 		
 		// HITBOX
 		if(isWhite){
 			hb = new Rectangle(x+6, y, 25, 10);
 		}
 		else{
 			hb = new Rectangle(x+6, y, 25, 10);
 		}
 		
 		
 		// MOVEMENT
 		if(isKeyDown(input, key, Input.KEY_A) || isKeyDown(input, key, Input.KEY_LEFT)){
         	x -= 0.3*delta;
         }
  
         if(isKeyDown(input, key, Input.KEY_D) || isKeyDown(input, key, Input.KEY_RIGHT)){
         	x += 0.3*delta;
         }
  
         if(isKeyDown(input, key, Input.KEY_W) || isKeyDown(input, key, Input.KEY_UP)){
         	y -= 0.3*delta;
         }
  
         if(isKeyDown(input, key, Input.KEY_S) || isKeyDown(input, key, Input.KEY_DOWN)){
         	y += 0.15*delta;
         }
       
         
         //TRANSFORM
         if((isKeyPressed(input, key, Input.KEY_LCONTROL) || input.isKeyPressed(Input.KEY_RCONTROL))){
         	if(isWhite){
         		ship = new Image("data/img/shipblack.png");
         		isWhite = false;
         	}
         	else{
         		ship = new Image("data/img/shipwhite.png");
         		isWhite = true;
         	}
         }
 		
         
         // FIRING
         for(int i = 0; i<bullets.size();i++){
         	bullets.get(i).fire(input,delta,x,y,isWhite,true);
         }
         if(isWhite){
         	shipColor = new Color(255,255,255);
         }
         else{
         	shipColor = new Color(0,0,0);
         }
 	}
 	
<<<<<<< HEAD
 	public boolean isKeyDown(Input i, int key, int code) {
 		return (i.isKeyDown(code) || key == code);
 	}
 	
 	public boolean isKeyPressed(Input i, int key, int code) {
 		if (i.isKeyPressed(code)) {
 			return true;
 		} else if (key == code) {
 			if (pressed.indexOf(key) == -1) {
 				pressed.add(key);
 				return true;
 			} else {
 				if (pressed.indexOf(-key) != -1) {
 					pressed.remove(pressed.indexOf(key));
 				}
 				return false;
 			}
 		} else {
 			return false;
 		}
=======
 	public void bubbled(){
 		bubble = new Bubble(x,y);
 		isBubbled = true;
>>>>>>> eae0a53d92327dc2cf0dcf23854c7ebe71811abf
 	}
 	
 	public void render(Graphics g) throws SlickException{
 		for(int i = 0; i<bullets.size();i++){
 			bullets.get(i).render(g,shipColor);
 		}
 		ship.draw(x, y, scale);
 		
 		if(isBubbled){
 			bubble.render(g,x,y);
 		}
 		
 	//	if(isWhite){
 	//		g.drawRect(x+6, y, 20, 25);
 	//	}
 	//	else{
 	//		g.drawRect(blackx+6, blacky, 20, 25);
 	//	}
 	}
 }
 
