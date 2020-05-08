 package pong.control;
 
 import org.jbox2d.callbacks.ContactImpulse;
 import org.jbox2d.callbacks.ContactListener;
 import org.jbox2d.collision.Manifold;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.contacts.Contact;
 
 import pong.model.*;
 
 public class HitDetection implements ContactListener {
 
 	GameEngine ge;
 	
 	public HitDetection(GameEngine ge) {
 		this.ge = ge;
 	}
 	
 	@Override
 	public void beginContact(Contact arg0) {
 		GameItem item1 = (GameItem)arg0.getFixtureA().getBody().getUserData();
 		GameItem item2 = (GameItem)arg0.getFixtureB().getBody().getUserData();
 
 		//Has a ball hit a wall?
 		if((item1 instanceof Ball && item2 instanceof Wall) || (item2 instanceof Ball && item1 instanceof Wall) ){
 			ballHitWall(item1, item2);	
 		}
 		//Has a ball hit a paddle?
 		if((item1 instanceof Paddle && item2 instanceof Ball) || (item2 instanceof Ball && item1 instanceof Paddle) ){
 			ballHitPaddle(item1, item2);	
 		}
 
 		
 	}
 
 	private void ballHitPaddle(GameItem item1, GameItem item2) {
 		// Play sound effect when a ball hits a paddle
 		ge.playSound("blip.wav");
 	}
 
 	private void ballHitWall(GameItem item1, GameItem item2){
 		Ball ball;
 		Wall wall;
 		if(item1 instanceof Ball){
 			ball = (Ball)item1;
 			wall = (Wall)item2;
 		}else{
 			ball = (Ball)item2;
 			wall = (Wall)item1;
 		}
 		Player player;
 		player = ge.getPlayer1();
 		if(player.getGoals().contains(wall)){
 			//Player 1's goal has been hit
 			ge.ballOut(player);
 		}
 		player = ge.getPlayer2();
 		if(player.getGoals().contains(wall)){
 			//Player 2's goal has been hit
 			ge.ballOut(player);
 		}
 		else if( !(player.getGoals().contains(wall)) && !(ge.getPlayer1().getGoals().contains(wall)) ){
 			// Regular wall has been hit, play wallsound
			ge.playSound("wallsound2.wav");
 		}
 	}
 	
 	
 	@Override
 	public void endContact(Contact arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void postSolve(Contact arg0, ContactImpulse arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void preSolve(Contact arg0, Manifold arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
