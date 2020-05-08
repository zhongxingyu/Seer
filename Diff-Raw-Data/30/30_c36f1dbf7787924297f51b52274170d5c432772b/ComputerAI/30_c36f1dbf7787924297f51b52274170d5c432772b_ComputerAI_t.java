 package pong.control;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import static pong.model.Const.*;
 import pong.model.Ball;
 import pong.model.Player;
 
 public class ComputerAI {
 
 	private int difficulty = DEFAULT_AI_DIFFICULTY;
 
 	public ComputerAI(int difficulty){
 		this.difficulty = difficulty;
 	}
 
 	public void MoveAI(Player player2, Ball mainBall){
 		Body paddleBody = player2.getPaddle().getBody();
 		Body ballBody = mainBall.getBody();
 
 		Vec2 ballvector = ballBody.getPosition();
 		Vec2 paddlevector = paddleBody.getPosition();
 
 //		System.out.println("Ball y: " + ballvector.y);
 //		System.out.println("Paddle y: " + paddlevector.y);
 		if( ballvector.y > 0){
 			
 			// NUDGE THE BALL IF IT IS CLOSE TO THE PADDLE --- THIS DOES NOT WORK YET
			if( ballvector.y < paddlevector.y && ballvector.y > 28 ){
				paddleBody.setLinearVelocity(new Vec2( 0f , -300f));
 //				paddleBody.applyForce(new Vec2(0, -30), new Vec2(0,0) );
 			}
 
			else if( paddlevector.x == ballvector.x ){
 				// SET FORCE TO ZERO - PADDLEPOSITION IS GOOD
 				paddleBody.setLinearVelocity(new Vec2( 0f , 0f));
 			}
 			else if( paddlevector.x > ballvector.x ){
 				paddleBody.setLinearVelocity(new Vec2( -DEFAULT_AI_SPEED*difficulty , 0f));
 			}
 			else if( paddlevector.x < ballvector.x ){
 				paddleBody.setLinearVelocity(new Vec2( DEFAULT_AI_SPEED*difficulty , 0f));
 			}
 		} // END IF BALL IS NOT ON AI's SIDE
 		else
 		{
 			// DONT MOVE PADDLE WHEN THE BALL IS ON THE OTHER PLAYERS SIDE OF THE GAMEAREA
 			paddleBody.setLinearVelocity(new Vec2( 0f , 0f));
 		}
 	}
 
 }
