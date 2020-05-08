 package characters.actions;
 
 import characters.GameCharacter;
 import map.Map;
 import utility.*;
 
 public final class MovementGameAction extends AbstractGameAction {
 	
 	private final Position position;
 
 	public MovementGameAction(GameCharacter subject, Direction there) {
 		super(subject);
 		position = DirectionProcessor.applyDirectionToPosition(subject.position, there);
 		actionPointsLeft = Map.getPassageCost(position);
 	}
 	
 	public void execute() {
		Map.moveGameCharacter(subject, position);
 	}
 
 }
