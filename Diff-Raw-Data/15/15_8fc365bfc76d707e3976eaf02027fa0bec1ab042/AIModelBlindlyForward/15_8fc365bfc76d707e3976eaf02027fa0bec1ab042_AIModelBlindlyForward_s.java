 package aiModels;
 
 import gameObjects.Board;
 import gameObjects.GameObject;
 import gameObjects.GameObjectCreature;
 import gameObjects.GameObjectCreature.CreatureAlliance;
 
 import java.util.ArrayList;
 
 import primary.ApplicationModel;
 import primary.Constants;
 import primary.Point;
 import actions.ActionMove;
 
 public class AIModelBlindlyForward extends AIModel {
 	private Point myLocation;
 	private CreatureAlliance myAlliance;
 	private GameObjectCreature myTempReference; //only used so that the move system can function properly
 	GameObject myTarget;
 
 	public AIModelBlindlyForward() {
 		myTarget = null;
 	}
 	
 	@Override
 	public void assignToCreature(GameObjectCreature newSelf) {
		myLocation = newSelf.myLocation;
		myAlliance = newSelf.myAlliance;
 		newSelf.myAIModel = this;
 		myTempReference = newSelf;
 	}
 	
 	@Override
 	public ActionMove planNextMove() {
 		if (myTarget == null) {
 			
 			if (myAlliance == CreatureAlliance.PLAYER) {
 				Board myBoard = ApplicationModel.getInstance().myBoard;
 				if (myBoard.myTokens.isEmpty()) 
 					return null;
 				
 				for (int i = 0; i < myBoard.myTokens.size(); i++) {
 					if (visibleSquares[myBoard.myTokens.get(i).myLocation.y][myBoard.myTokens.get(i).myLocation.x]) {
 						myTarget = myBoard.myTokens.get(i);
 						break;
 					}
 				}
 			}
 			else { //this is a ghost
 					myTarget = ApplicationModel.getInstance().myPlayer;
 			}
 		}
 
 
 		ArrayList<PolicyMove> bestMoveList;
 		
 		if ((myTarget == null) || (myTarget != null && !visibleSquares[myTarget.myLocation.y][myTarget.myLocation.x])) { 
 			// No target visible, Randomly move
 			bestMoveList = getRandomMoveList();
 		}
 		else
 			bestMoveList = populateBestMoveDeterministicList(myLocation.x, myLocation.y, myTarget.myLocation.x, myTarget.myLocation.y);
 		PolicyMove moveTarget = determineBestMove(bestMoveList);
 		
 		if (moveTarget == null)
 			return null;
 		
 		Point targetP = Constants.outcomeOfMove(moveTarget, myLocation);
 		
 		if (targetP == null)
 			return null;
 		
 		return new ActionMove(targetP, myTempReference, moveTarget);
 	}
 	
 	@Override
 	public void clearTarget(GameObject oldTarget) {
 		if (myTarget == null)
 			return;
 		
 		if (oldTarget == myTarget) {
 			myTarget = null;
 		}
 	}
 		
 	@Override
 	public String describeActionPlan() {
 		String retval = "Blind Direct Move - Moves directly to target but has no concept of self or blocking objects";
 		
 		if (myTarget != null) 
 			retval += "  Target: " + myTarget.name + " at (" + myTarget.myLocation.x + ", " + myTarget.myLocation.y + ")";
 		
 		return retval;
 	}}
