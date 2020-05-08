 package model;
 
 import java.util.ArrayList;
 
 public class CautiousStrategy implements Strategy{
 
 	public Pawn getNextMove(int currentRoll, ArrayList<Pawn> moveablePawns, Field[] gameBoard) {
 		for(Pawn pawn: moveablePawns){
 			int startPos = pawn.getPosition();
 			Player passedHomeOwner = null;
 			for(int i=1;i<6;i++){
 				startPos = startPos-1 < 0 ? 40 : startPos-1;
 				if(gameBoard[startPos] instanceof StartTile){
 					passedHomeOwner = gameBoard[startPos].getForkOwner();
 				}
 				if(!gameBoard[startPos].getOccupant().equals(null) && !gameBoard[startPos].getOccupant().equals(passedHomeOwner)){
 					if(!gameBoard[startPos].getOccupant().equals(pawn.getOwner())){
 						return pawn;
 					}
 				}else{
 					continue;
 				}
 			}
 		}
		return null;
 	}
 	
 	public String toString(){
 		return "Cautious";
 	}
 
 }
