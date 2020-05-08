 package slug.soc.game.gameObjects.tasks;
 
 import org.newdawn.slick.util.pathfinding.Path;
 
 import slug.soc.game.Pathfinder;
 import slug.soc.game.gameObjects.GameObject;
 import slug.soc.game.gameObjects.GameObjectPerson;
 import slug.soc.game.gameObjects.MovementOrder;
 import slug.soc.game.gameObjects.MovementOrderCoordinate;
 import slug.soc.game.gameState.GameModeState;
 
 public class HaveChildTask extends AbstractTask {
 
 	private MovementOrder path;
 	private boolean readyToChild;
 
 	public HaveChildTask(GameObject owner, GameObject target) {
 		super(owner, target);
 		Pathfinder finder = new Pathfinder(10000,false);
 		Path tmpPath = finder.findPath(owner.getX(), owner.getY(), target.getX(), target.getY());
 		if(tmpPath != null){
 			path = new MovementOrder(tmpPath.getLength());
 			for(int i = 0; i < tmpPath.getLength(); i++){
 				if(i != 0){
 					path.getLastCoord().setNextCoord(
 							new MovementOrderCoordinate(tmpPath.getStep(i).getX() - tmpPath.getStep(i - 1).getX()
 									,tmpPath.getStep(i).getY() -  tmpPath.getStep(i - 1).getY()
 									, path.getLastCoord()));
 				}
 				else{
 					path.getLastCoord().setNextCoord(
 							new MovementOrderCoordinate(tmpPath.getStep(i).getX() - owner.getX()
 									,tmpPath.getStep(i).getY() - owner.getY()
 									, path.getLastCoord()));
 				}
 			}
 		}else{
 			isCompleted = true;
 		}
 	}
 
 	@Override
 	public void act() {
 		if(readyToChild){
 			if(!((GameObjectPerson) owner).isFemale() && !((GameObjectPerson) target).isFemale()){
 				((GameObjectPerson) owner).addRumor("It is rumored he prefers the company of men.");
 				GameModeState.getInstance().addNotification(" Rumors are spreading that " + owner.getName() + " likes the male interest.");
 			}
 			else{
 				GameObjectPerson child = ((GameObjectPerson) target).haveChild((GameObjectPerson) target, (GameObjectPerson)owner);
 				if(child != null){
 					GameModeState.getInstance().addObjectToQueue(child);
 					GameModeState.getInstance().addNotification(owner.getName() + " and " + target.getName() + " have had a child.");
 				}}
 			isCompleted = true;
 		}
 		else{
 			owner.giveOrders(path);
 			if(path.getLastCoord().getNextCoord() == null){
 				readyToChild = true;
 			}
 			else{
 				owner.giveOrders(path);
 			}
 		}
 	}
 
 	@Override
 	public String getDesc() {
 		return "having a child with " + target.getName();
 	}
 
 }
