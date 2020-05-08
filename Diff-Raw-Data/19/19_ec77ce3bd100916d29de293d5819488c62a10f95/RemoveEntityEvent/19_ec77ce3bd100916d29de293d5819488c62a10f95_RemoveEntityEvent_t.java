 package game.event;
 
 import java.util.List;
 
 import game.Game;
 import game.entity.Entity;
 
 public class RemoveEntityEvent extends AbstractEvent{
 
 	private final long target_id;
	private boolean activated = false;
 	
 	public RemoveEntityEvent(long _target_id){
 		target_id = _target_id;
 	}
 	@Override
 	protected boolean applyEvent(long _timeStamp, List<Entity> _trigger) {
		if(!activated){
			Game.getInstance().removeEntity(target_id);
			activated = true;
			return true;
		}
		return false;
 	}
 }
