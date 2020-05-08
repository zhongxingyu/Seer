 import java.util.Random;
 import java.util.ArrayList;
 
 public class LandOffice{
 /*
 The land office buys and sells property. If a player wishes to sell a property 
 during their turn, they may enter the land office and offer a property to sell. 
 If a mule is on the property when sold, it is lost. The selling price is 400 + random(0-200). 
 You may also buy property if any is available from the land office. The buying price is 300 + round * random(0-100).
 */
 
 private Random rand = new Random();
 private int propertyPrice;
 private int buyingPrice;
 private int low = 0;
 private int shigh = 200;
 private int bhigh = 100;
 private int sellingRand = rand.nextInt(shigh-low);
 private int buyingRand = rand.nextInt(bhigh-low);
 private int playerValue;
 
 	public LandOffice(int getRoundNumber, int currPlayer){
 		if(getRoundNumber <= 2){
 			this.propertyPrice = 0;
 		}
 		if(getRoundNumber > 2){
 			this.propertyPrice  = 300 + (getRoundNumber * buyingRand);
 		}
 	}
 
 public void buyProperty(ArrayList<Player> players, int currPlayer, Map map){
 	playerValue = (int)players.get(currPlayer).getMoney();
 	if((playerValue - propertyPrice) >= 0){
 		players.get(currPlayer).setMoney(playerValue - propertyPrice);
		players.get(currPlayer).addTile();
 	}
 	else{
 		System.out.println("Player does not have sufficient funds.");
 	}
 	
 }
 
 /**
  * Subtracts land cost from player's money
  * @param players ArrayList contaning all players
  * @param currPlayer The current player
  */
 public void sellingProperty(ArrayList<Player> players, int currPlayer){
 	playerValue = (int)players.get(currPlayer).getMoney();
 	players.get(currPlayer).setMoney(playerValue + buyingPrice);
 }
 
 /**
  * Adds land cost to player's money
  * @param players ArrayList contaning all players
  * @param currPlayer The current player
  */
 public void setBuyingPrice(int buyingPrice) {
     this.buyingPrice = buyingPrice;
 }
 
 /**
  * Setter method for the property price
  * @param propertyPrice The new property price
  */
 public void setPropertyPrice(int propertyPrice) {
     this.propertyPrice = propertyPrice;
 }
 
 /**
  * Getter method for the property price
  * @param propertyPrice Property price
  */
 public int getPropertyPrice() {
     return this.propertyPrice;
 }
 
 
}
