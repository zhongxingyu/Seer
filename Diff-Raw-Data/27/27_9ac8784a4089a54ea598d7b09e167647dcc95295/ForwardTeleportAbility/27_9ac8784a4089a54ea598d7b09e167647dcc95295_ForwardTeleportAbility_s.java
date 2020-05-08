 package entities.players.abilities;
 
 import entities.players.Player;
 
 public class ForwardTeleportAbility extends PlayerAbility {
 	
     public void use(Player p){
 		if (p.getDirection() == 1){
			p.moveX(5f);
 		} else {
			p.moveX(-5f);
 		}
 	}
 
 	@Override
 	public void stop_sounds() {
 		// TODO Auto-generated method stub
 		
 	}
 }
