 package chalmers.TDA367.B17.tanks;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.model.AbstractTank;
 import chalmers.TDA367.B17.model.Player;
 import chalmers.TDA367.B17.network.Network.Pck7_TankID;
 import chalmers.TDA367.B17.states.ServerState;
 
 public class TankFactory {
 
 	private TankFactory() {}
 
 	/**
 	 * Get a tank based on a player's tank type.
 	 * @param player The new owner of the tank
 	 * @return A tank based on a player's type
 	 */
 	public static AbstractTank getTank(Player player){
 		AbstractTank tank;
 		if(player.getTankType().equals("default")){
			tank = new DefaultTank(GameController.getInstance().generateID(), new Vector2f(0,-1), player, player.getPlayerColor());
 		}else{
			tank =  new DefaultTank(GameController.getInstance().generateID(), new Vector2f(0,-1), player, player.getPlayerColor());
 		}
 		player.setTank(tank);
     	
     	if(player.getConnection() != null){
     		Pck7_TankID tankPck = new Pck7_TankID();
     		tankPck.tankID = player.getTank().getId();
     		player.getConnection().sendTCP(tankPck);
     		ServerState.getInstance().addToClientQueue(tankPck, player.getConnection());
     	}
     	
     	return tank;
 	}
 }
