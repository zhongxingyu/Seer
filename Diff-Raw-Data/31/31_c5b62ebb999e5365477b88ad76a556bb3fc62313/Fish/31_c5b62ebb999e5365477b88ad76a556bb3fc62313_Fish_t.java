 package com.badlogic.androidgames.fishboy;
 
 import java.util.Random;
 
 import android.graphics.Rect;
 
 import com.badlogic.androidgames.framework.Game;
 import com.badlogic.androidgames.framework.math.Vector2;
 
 public class Fish {
 	public Vector2 position;
 	public boolean isDead;
 	public Rect bounds;
 	private Random rand;
 	
 	public Fish(Vector2 position, Game game) {
 		this.position = position;
 		this.isDead = false;
 		this.rand = new Random();
 		this.bounds = new Rect();
 	}
 
 	public void update(float deltaTime){
		this.position.add(new Vector2(0, -25.0f * deltaTime));
		this.bounds = new Rect((int) this.position.x, Assets.fish.getHeight(), (int) this.position.y, Assets.fish.getWidth());
 		
 		if (this.position.y < 200){
 			this.isDead = true;
 		}
 		
 		if (isDead){
 			Assets.dead.play(1);
 		}	
 	}
 
 	public void reboot(Game game) {
 		// TODO Auto-generated method stub
 		this.position = new Vector2( 
 				rand.nextInt(game.getGraphics().getWidth() - Assets.fish.getWidth() + 1) + Assets.fish.getWidth(),
 	            game.getGraphics().getHeight()
 	    );
		this.isDead = false;
 	}
 
 	public boolean hit(Rect playerBounds) {
 		if (this.bounds.intersect(playerBounds)){
 			return true;
 		}
 		return false;
 	}
 	
 	
 	
 }
