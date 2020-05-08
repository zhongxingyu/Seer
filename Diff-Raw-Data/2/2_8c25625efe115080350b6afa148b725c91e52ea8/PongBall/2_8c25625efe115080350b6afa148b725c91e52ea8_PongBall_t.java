 //draft of pong ball entity, proof of concept
 
 //created piogre 6/16
 
 //draft version notes:
 //constant x velocity (10) back and forth, xPos range 0 to 100
 //y velocity changes, yPos ranges 0 to 50
 
 import java.util.Random;
 
public class PongBall implements PhysObj{
 	
 	private int posX;
 	private int posY;
 	private float velY;
 	private int velX;
 	
 	public PongBall(){
 		
 		posX = 50;
 		posY = 25;
 		
 		Random gen = new Random();
 		
 		if(gen.nextBoolean()){
 			
 			velX = 10;
 			
 		}else{
 			
 			velX = -10;
 			
 		}//end if
 		
 		velY = gen.nextFloat()- 0.5f * 20; //value between -10 and 10, can't start with larger y than x but can get bigger later
 		
 	}//end random velX and velY constructor
 	
 	public PongBall(boolean isRight, float yVelocity){
 		
 		posX = 50;
 		posY = 25;
 		
 		Random gen = new Random();
 		
 		if(isRight){
 			
 			velX = 10;
 			
 		}else{
 			
 			velX = -10;
 			
 		}//end if
 		
 		velY = yVelocity;
 		
 	}//end constructor which takes a y velocity and a left-or-right for x, for dev use only
 	
 }//end PongBall
