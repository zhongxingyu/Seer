 package ru.spbau.opeykin.drunkard.game.objects;
 
 import static ru.spbau.opeykin.drunkard.game.Interaction.InteractionResult;
 
 import ru.spbau.opeykin.drunkard.game.Position;
 
 
 public class Policeman extends RouteGoingGameObject {
 
     private final Position returnPosition;
 	
 	private boolean complete = false;
 
 
     public Policeman(Position position, Position target, Position returnPosition) {
 		super(position, target);
         this.returnPosition = returnPosition;
 	}
 
 
 	@Override
 	public InteractionResult affect(AffectableGameObject gameObject) {
 		return gameObject.getAffected(this);		
 	}
 	
 	@Override
     protected void updateRoute() {
 		if (complete) {
 			route = getRoute(returnPosition);
 		} else {
 			route = getRoute(target);
 		}
 	}
 
 
 	@Override
 	public void doTurn() {
         goRoute();
 	}
 
 	@Override
     InteractionResult getAffected(Drunkard drunkard) {
 		if (!complete && drunkard.getPosition() == target) {
 			complete = true;
 			return InteractionResult.REPLACE_HOST;
 		}
 		updateRoute();
 		return InteractionResult.KEEP_BOTH;
 	}
 
     @Override
     InteractionResult getAffected(PoliceDepartment policeDepartment) {
         return InteractionResult.RELEASE_VISITOR;
     }
 
     @Override
 	public char getSymbol() {
 		return '!';
 	}
 }
