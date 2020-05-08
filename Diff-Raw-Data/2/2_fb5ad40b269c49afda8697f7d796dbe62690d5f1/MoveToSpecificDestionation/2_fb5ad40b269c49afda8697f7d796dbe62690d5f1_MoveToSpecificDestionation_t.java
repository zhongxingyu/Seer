 import java.util.Collections;
 import java.util.List;
 
 
 public class MoveToSpecificDestionation extends Behavior {
 	
 	public MoveToSpecificDestionation(Ant ant) {
 		super(ant);
 	}
 
 	@Override
 	public BehaviorDecision move() {
 		List<Aim> movement = MyBot.ants.getDirections(owner.getPosition(), owner.getDestination());
 		Collections.shuffle(movement);
 		
		return new BehaviorDecision(owner.getDestination(), "Moving to specific destination at " + owner.getDestination(), 15);
 	}
 
 }
