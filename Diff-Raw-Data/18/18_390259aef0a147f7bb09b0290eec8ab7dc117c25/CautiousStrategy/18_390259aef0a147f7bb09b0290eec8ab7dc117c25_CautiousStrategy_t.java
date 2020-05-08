 package model;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class CautiousStrategy implements Strategy{
 
 	public Pawn getNextMove(int currentRoll, ArrayList<Pawn> moveablePawns, Field[] gameBoard) {
 		for(Pawn pawn: moveablePawns){
 			int startPos = pawn.getPosition();
 			Player passedHomeOwner = null;
 			for(int i=1;i<6;i++){
				startPos = startPos-1 < 0 ? 39 : startPos-1;
				if(gameBoard[startPos%40] instanceof StartTile){
					passedHomeOwner = gameBoard[startPos%40].getForkOwner();
 				}
				if(gameBoard[startPos%40].getOccupant() != null && gameBoard[startPos%40].getOccupant() != (passedHomeOwner)){
					if(gameBoard[startPos%40].getOccupant() == pawn.getOwner()){
 						return pawn;
 					}
 				}else{
 					continue;
 				}
 			}
 		}
 		return moveRandom(currentRoll, moveablePawns, gameBoard);
 	}
 	
 	public Pawn moveRandom(int currentRoll, ArrayList<Pawn> moveablePawns, Field[] gameBoard){
 		Random rand = new Random();
 		if(moveablePawns.isEmpty()){
 			return null;
 		}
 		int random = rand.nextInt(moveablePawns.size());
 		return moveablePawns.get(random);
 	}
 	
 	public String toString(){
 		return "Cautious";
 	}
 
 }
