 package org.soupware.slicktest;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class PlayerShip {
 	float whitex = 400;
 	float whitey = 300;
 	float blackx = -400;
 	float blacky = -300;
 	float scale = 1.0f;
 	
 	boolean isWhite = true;
 	
 	Image swhite = null;
 	Image sblack = null;
 	
 	Color shipColor = new Color(255,255,255);
 	
 	public Bullets bullets = null;
 	
 	public PlayerShip() throws SlickException{
 		swhite = new Image("data/img/shipwhite.png");
 		sblack = new Image("data/img/shipblack.png");
 		
 		bullets = new Bullets(whitex,whitey);
 	}
 	
 	public void move(Input input, int delta){
 		//bullets = new Bullets();
 		
 		// MOVEMENT
 		
 		if(input.isKeyDown(Input.KEY_A) || input.isKeyDown(Input.KEY_LEFT))
         {
         	if(isWhite){
        		whitex -= 1.0*delta;
         	}
         	else{
        		blackx -= 1.0*delta;
         	}
         }
  
         if(input.isKeyDown(Input.KEY_D) || input.isKeyDown(Input.KEY_RIGHT))
         {
         	if(isWhite){
         		whitex += 0.3*delta;
         	}
         	else{
         		blackx += 0.3*delta;
         	}
         }
  
         if(input.isKeyDown(Input.KEY_W) || input.isKeyDown(Input.KEY_UP))
         {
         	if(isWhite){
         		whitey -= 0.3*delta;
         	}
         	else{
         		blacky -= 0.3*delta;
         	}
         }
  
         if(input.isKeyDown(Input.KEY_S) || input.isKeyDown(Input.KEY_DOWN))
         {
         	if(isWhite){
        		whitey += 0.1*delta;
         	}
         	else{
        		blacky += 0.1*delta;
         	}
         }
       
         
         //TRANSFORM
         
         if((input.isKeyPressed(Input.KEY_LCONTROL) || input.isKeyPressed(Input.KEY_RCONTROL))){
         	if(isWhite){
         		blackx = whitex;
         		blacky = whitey;
         		whitex = -400;
         		whitey = -300;
         		isWhite = false;
         	}
         	else{
         		whitex = blackx;
         		whitey = blacky;
         		blackx = -400;
         		blacky = -300;
         		isWhite = true;
         	}
         }
 		
         
         // FIRING
         if(isWhite){
         	shipColor = new Color(255,255,255);
         	bullets.mightfire(input,delta,whitex,whitey);
         }
         else{
         	shipColor = new Color(0,0,0);
         	bullets.mightfire(input,delta,blackx,blacky);
         }
 	}
 	
 	public void render(Graphics g) throws SlickException{
 		bullets.render(g,shipColor);
 		swhite.draw(whitex, whitey, scale);
 		sblack.draw(blackx, blacky, scale);
 	}
 }
 
