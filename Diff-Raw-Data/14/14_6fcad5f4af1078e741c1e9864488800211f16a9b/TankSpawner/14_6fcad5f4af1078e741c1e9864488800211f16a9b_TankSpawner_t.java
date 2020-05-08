 package chalmers.TDA367.B17.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.spawnpoints.TankSpawnPoint;
 
 public class TankSpawner{
 	
 	private List<TankSpawnPoint> tankSpawnPoints;
 	private List<Player> playerList;
 	
 	public TankSpawner(){
 		tankSpawnPoints = new ArrayList<TankSpawnPoint>();
 		playerList = new ArrayList<Player>();
 	}
 	
 	public void update(int delta){
 		for(int i = 0; i < playerList.size(); i++){
 			Player p = playerList.get(i);
 			p.setRespawnTimer(p.getRespawnTimer() - delta);
 			if(p.getRespawnTimer() <= 0){
 				GameController controller = GameController.getInstance();
 				//Bad place to run this
 				Iterator<Entry<Integer, Entity>> iterator = controller.getWorld().getEntities().entrySet().iterator();
 				while(iterator.hasNext()){
 					Map.Entry<Integer, Entity> entry = (Entry<Integer, Entity>) iterator.next();
 					Entity entity = entry.getValue();
 					if(entity instanceof TankSpawnPoint)
 						controller.getWorld().checkCollisionsFor(entity);
 				}
 				getTankSpawnPoint(p);
 				playerList.remove(i);
 			}
 		}
 	}
 	
 	public void addTankSpawnPoint(TankSpawnPoint tankSpawnPoint){
 		tankSpawnPoints.add(tankSpawnPoint);
 	}
 	
 	public void addPlayer(Player player){
 		playerList.add(player);
 	}
 	
 	public void getTankSpawnPoint(Player player){
		if(!(GameController.getInstance().getGameConditions().getPlayers().size() > tankSpawnPoints.size())){
			while(true){
				int index = (int)(Math.random()*(GameController.getInstance().getGameConditions().getPlayers().size()));
				TankSpawnPoint tmp = tankSpawnPoints.get(index);
				if(tmp.isSpawnable()){
					tmp.spawnTank(player);
					break;
				}
 			}
 		}
 	}
 }
