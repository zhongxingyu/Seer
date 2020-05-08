 package com.teamcoffee.game.models;
 
 import com.badlogic.gdx.math.Vector2;
 import com.teamcoffee.game.views.Audio;
 import com.teamcoffee.game.views.Level;
 
 public class Fire extends Entity{
 	
 	public Fire(Vector2 position, float width, float height){
 		super(position, width, height, "data/flames_0.png", 4, 3);
 	}
 
 	@Override
 	public void onCollide(int direction, MoveableEntity a, Level level) {
//		float locationX = this.getPosition().x;
//		float locationY = this.getPosition().y + this.getHeight();
//		level.addEntity(new Block(new Vector2(locationX, locationY),50,50));
 		Audio.death();
 		level.respawnPlayer();
		
 	}
 
 
 }
