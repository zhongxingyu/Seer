 package me.bevilacqua.game.entity;
 
 import me.bevilacqua.game.InputHandler;
 import me.bevilacqua.game.gfx.Screen;
 import me.bevilacqua.game.gfx.Sprite;
 
 public class Player extends Mob {
 	
 	private InputHandler input;
 	
 	
 	public Player(InputHandler input) { //Mouse Later
 		this.input = input;
 	}
 	
 	public Player(int x , int y , InputHandler input) { 
 		this.x = x;
 		this.y = y;
 		this.input = input;
 	}
 
 	public void tick() {	
 		int xa  = 0 , ya = 0;
 		if(input.up) ya--;
 		if(input.down) ya++;
 		if(input.left) xa--;
 		if(input.right) xa++;
 		
 		if(xa != 0 || ya != 0) move(xa , ya);
 	}
 	
 	public void render(Screen screen ) {
		screen.renderPlayer(x , y - 15 ,Sprite.DefaultPlayer0); //TODO: find better way to render player because there are going to be multiple types of the player
		screen.renderPlayer(x, y, Sprite.DefaultPlayer1);
 	}
 
 	@Override
 	public void render() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
