 package strategy;
 
 import util.Info;
 
 public class HadesStrategy extends Strategy {
 
 	int[] cardsOnTable;
 	
 	int playerPosition;//0-8
 	int dealerPosition;//0-8
 	
 	int turn;
 	
 	int playerCardOne;
 	int playerCardTwo;
 	
 	int numberOfPlayers;//1-9
 	
 	//String playerName = "BobbyDrop";
 	
 	@Override
 	public void update(Info info) {
 		//Example on info extraction from object.
 		cardsOnTable = info.getCardsOnTable();
 	}
 
 	@Override
 	public int whatToDo() {
 		if(numberOfCardsOnTable() == 0){
 			if(turn == 1){
 				cheatSheetStartStrategy();
 			}
 			// TODO when it is not the first round of betting
 		}else{
 			// TODO Post-Flop strategy
 		}
 		return 0;
 	}
 	
 	private void cheatSheetStartStrategy(){
 		if(!handIsGrouped()){
 			//fold
 		} else {
 			if(someoneHasBet()){
 				//if you are not the blinds
 				if(!playerIsBlinds()){
 					if(handInGroupA() || handInGroupB()){
 						//re raise
 					} else {
 						//fold
 					}
 				} else {
 					//need to add the bit for if you are the blinds
 				}
 			}
 			if(getPositionStrength() == 3){
 				//raise
 			} else if (getPositionStrength() == 2){
 				if(handInGroupA() ||
 				   handInGroupB() ||
 				   handInGroupC() ||
 				   handInGroupD()){
 					//raise
 				}
 			} else if (getPositionStrength() == 1){
 				if(handInGroupA() ||
 				   handInGroupB() ||
 				   handInGroupC()){
 					//raise
 				}
 			} else {
 				//panic
 			}
 		}
 	}
 	
 	private boolean playerIsBlinds() {
 		//needs to 
 		// TODO return if the player is blinds.
 		return false;
 	}
 
 	//i should consider rewriting this ugly ass function. Use a new array where 0 is always dealer.
 	//the logic on position seems dodgey (not the code, but the strategy logic) with less than 9 players. kind of worrying.
 	private int getPositionStrength(){
 		//this function needs finishing, it is fairly simple but needs more information to calculate its result.
 		//dealer is always latest and strongest position.
 		//blinds are weird.
 		//they are considered the weakest and worst position, but before the flop the big blind acts last
 		//but stats show that the big blind is the position that looses the most money.
 		//so for now this will return them as weakest 0.
 		//looking at the table counter clockwise.
 		//
 		//if there are more than 5 players, next player is late
 		//the remaining players are divided into middle and early. more early positions.
 		int strengthNumber = 4;
 		
 		
 		//THIS ONLY WORKS FOR MORE THAN 5 PLAYERS CURRENTLY.
 		//NOT GOOD
 		if(numberOfPlayers > 5){
 			//late
 			if(dealerPosition == playerPosition){
 				strengthNumber = 3;
 			} else{
 				if(dealerPosition!=0){
 					if(dealerPosition-1 == playerPosition){
 						strengthNumber = 3;
 					}
 				} else {
 					if(numberOfPlayers-1 == playerPosition){
 						strengthNumber = 3;
 					}
 				}
 			}
 			
 			//blinds
 			if(dealerPosition+1 == playerPosition){
 				strengthNumber = 0;
 			} else if(dealerPosition+2 == playerPosition){
 				strengthNumber = 0;
 			} else if(dealerPosition == numberOfPlayers){
 				if(0 == playerPosition){
 					strengthNumber = 0;
 				} else if(1 == playerPosition){
 					strengthNumber = 0;
 				}
 			} else if(dealerPosition+1 == numberOfPlayers){
 				if(numberOfPlayers == playerPosition){
 					strengthNumber = 0;
 				} else if(0 == playerPosition){
 					strengthNumber = 0;
 				}
 			}
 			
 			//early
 			int leftToSort = numberOfPlayers-3;
 			int earlysInt = leftToSort/2;
 			
 			for (int i = 0; i < earlysInt; i = i + 1) {
 				int positionToCheck = dealerPosition + 3 + i;
 				
 				//dunno if this will work or not, how reassuring
 				if(positionToCheck>numberOfPlayers){
 					positionToCheck = positionToCheck - numberOfPlayers - 1;
 				}
 				
 				if(positionToCheck == playerPosition){
 					strengthNumber = 1;
 				}
 			} 
 			
 			//middle
 			if(strengthNumber == 4){
 				strengthNumber = 2;
 			}
 			
 		}
 		
 		//strengthNumber either 1,2,3. early,middle,late position. Higher is better.
 		//maybe use zero for blinds
 		return strengthNumber;
 	}
 	
 
 	private boolean playerHandMatches(char checkValueOne,char checkValueTwo, boolean onlySuited){
 		//just uses the players card into this method, makes things look more tidy.
 		return handMatches(playerCardOne,playerCardTwo,checkValueOne,checkValueTwo, onlySuited);
 	}
 	
 	private boolean handMatches(int cardOneNum,int cardTwoNum,char checkValueOne,char checkValueTwo, boolean onlySuited){
 		char cardOneVal =  intToCard(cardOneNum).charAt(1);
 		char cardTwoVal =  intToCard(cardTwoNum).charAt(1);
 		
 		if(onlySuited){
 			char cardOneSuit =  intToCard(cardOneNum).charAt(0);
 			char cardTwoSuit =  intToCard(cardTwoNum).charAt(0);
 			
 			//comparing chars
 			if(cardOneSuit != cardTwoSuit){
 				return false;
 			}
 		}
 		
 		//comparing chars
 		//check if cards are equal to the provided chars. Both way round.
 		if(cardOneVal == checkValueOne && cardTwoVal == checkValueTwo){
 			return true;
 		} else if(cardOneVal == checkValueTwo && cardTwoVal == checkValueOne){
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private String intToCard(int intToChange) {
 		//just uses siteEmulators intocard. 
 		//seperate for ease of use and intocard might move later.
		return util.SiteEmulator.intToCard(intToChange);
 	}
 
 	private boolean handIsGrouped(){
 		//returns false if hand is not in any groups.
 		
 		if(handInGroupA() ||
 		   handInGroupB() ||
 		   handInGroupC() ||
 		   handInGroupD() ||
 		   handInGroupE()){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean handInGroupA(){
 		//should return true if hand is in group A.
 		if(
 			playerHandMatches('A','A',false) ||
 			playerHandMatches('K','K',false) ||
 			playerHandMatches('A','K',true)
 		){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean handInGroupB(){
 		//should return true if hand is in group B.
 		if(
 			playerHandMatches('Q','Q',false) ||
 			playerHandMatches('A','K',false) ||
 			playerHandMatches('J','J',false) ||
 			playerHandMatches('T','T',false)
 		){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean handInGroupC(){
 		//should return true if hand is in group C.
 		if(
 			playerHandMatches('A','Q',true) ||
 			playerHandMatches('9','9',false) ||
 			playerHandMatches('8','8',false) ||
 			playerHandMatches('A','J',true)
 		){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean handInGroupD(){
 		//should return true if hand is in group D.
 		if(
 			playerHandMatches('7','7',false) ||
 			playerHandMatches('K','Q',true) ||
 			playerHandMatches('6','6',false) ||
 			playerHandMatches('A','T',true) ||
 			playerHandMatches('5','5',false) ||
 			playerHandMatches('A','J',false)
 		){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean handInGroupE(){
 		//should return true if hand is in group E.
 		if(
 			playerHandMatches('K','Q',false) ||
 			playerHandMatches('4','4',false) ||
 			playerHandMatches('K','J',true) ||
 			playerHandMatches('3','3',false) ||
 			playerHandMatches('2','2',false) ||
 			playerHandMatches('A','T',false) ||
 			playerHandMatches('Q','J',true)
 		){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean someoneHasBet(){
 		// TODO should return true if someone has be before you.
 		return false;
 	}
 	
 	private static void trace(String PrintMeOut){
 		//using trace because its fucking awesome.
 		//similar to as3 (which im used to)
 		//comment one line to remove all traces.
 		//could change the out to a file to act as a log.
 		//yeah i know, im sick.
 		System.out.println(PrintMeOut);
 	}
 	
 	//Example of private method.
 	private int numberOfCardsOnTable(){
 		return cardsOnTable.length;
 	}
 
 }
