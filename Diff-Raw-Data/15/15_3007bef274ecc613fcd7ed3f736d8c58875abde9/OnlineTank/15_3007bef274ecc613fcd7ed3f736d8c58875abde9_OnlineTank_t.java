 package networking;
 
 import baseGame.derp;
 import blueMaggot.Game;
import blueMaggot.GameState;
 import inputhandler.InputHandler;
 import level.BasicLevel;
 import entity.FloatingPoint;
 import entity.Shell;
 import entity.Tank;
 
 public class OnlineTank extends Tank {
 
 
 	public OnlineTank(FloatingPoint point, int playerNumber, InputHandler input, BasicLevel level,String[] msg) {
 		super(point, playerNumber, input, level);
 		if(msg !=null)
 		setPlayerNumber(Integer.parseInt(msg[7]));
 		
 		// TODO Auto-generated constructor stub
 	}
 	public OnlineTank(double x, double y, int playerNumber, InputHandler input, BasicLevel level,String[] msg) {
 		super(x,y,playerNumber,input,level);
 		if(msg !=null)
 			setPlayerNumber(Integer.parseInt(msg[7]));
 	
 	}
 
 	@Override
 	public void player1Input(){
 	
 	}
 	
 	@Override
 	public void tick(double dt){
 		if(shouldTick() && getPlayerNumber() == 2){
 			
 			super.tick(dt);
 			return;
 		}
 			
 		
 		if(!shouldTick() && getPlayerNumber() == 1){
			if (x > GameState.getInstance().width + 100 || x < -100 || y > GameState.getInstance().height + 100 || y < -1000)
				remove();
 			this.dt = dt;
 			gravitate();
 			player2Input();
 			move(dt);
 			if (chargingCannon)
 				cannonCharge += 0.015 * dt;
 			if (jetPackFuel + 5 > 100)
 				jetPackFuel = 100;
 			else
 				jetPackFuel += 2 * dt;
 			
 			handleTerrainIntersection();
 			
 			applyFriction();
 			tickWeapons(dt);
 			
			if (this.y < -500)
				remove();
			
 		}
 	}
 	
 
 	@Override
 	public String getObject(){
 		
 		String msg = super.getObject();
 		if(shouldTick() && derp.playerNumber == 2 && playerNumber == 1){
 			setSouldTick(false);
 			setShouldBeSent(false);
 		}
 		return msg;
 		
 	}
 	
 }
