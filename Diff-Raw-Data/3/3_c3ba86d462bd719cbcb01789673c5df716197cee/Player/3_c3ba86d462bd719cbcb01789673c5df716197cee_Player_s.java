 /**
  * Class to represent a player.
  */
 package cs283.catan;
 import java.io.Serializable;
 import java.util.*;
 
 public class Player implements Serializable
 {
 	/**
      * 
      */
     private static final long serialVersionUID = -687219382118580014L;
     
     /**
      * List of resource cards owned by the player
      * Changed on 4/19 to be an int array
      * 
      * BRICK
      * LUMBER
      * ORE
      * WHEAT
      * WOOL
      * in that order
      */
     
     private int[] resCards = new int[5];
     
     /**
      * List of development cards
      */
 	public List<DevelopmentCard> devCards = new LinkedList<DevelopmentCard>();
 	
 	/**
 	 * Number of victory points by category
 	 */
 	private int permanentPoints;
 	private int longestRoadPoints;
 	private int largestArmyPoints;
 	
 	/**
 	 * Number of knights played
 	 */
 	private int numKnightsPlayed;
 	
 	/*
 	 * Boolean values for trade points
 	 */
 	
 	public boolean has3To1Port = false;
 	public boolean has2WoolPort = false;
 	public boolean has2WheatPort = false;
 	public boolean has2OrePort = false;
 	public boolean has2LumberPort = false;
 	public boolean has2BrickPort = false;
 	
 	
 	/*
 	 * Boolean value for robberMode
 	 */
 	
 	public boolean robberMode = false;
 	public boolean stealMode = false;
 	public int roadBuilderMode = 2;
 	public int yearOfPlentyMode = 0;
 	public int settlementPlacementMode = 2;
 	
 	/**
 	 * Color index used when drawing the board in the GUI.
 	 */
 	private int colorIndex;
 	
 	public String username;
 	
 	
 	
 	public Player(String username, int colorIndex)
 	{
 		this.username = username;
 		this.colorIndex = colorIndex;
 		this.numKnightsPlayed = 0;
 		this.resCards[0] = 0;
 		this.resCards[1] = 0;
 		this.resCards[2] = 0;
 		this.resCards[3] = 0;
 		this.resCards[4] = 0;
 		this.permanentPoints = this.longestRoadPoints = 
 		                       this.largestArmyPoints = 0;
 	}
 	
 	/**
 	 * Returns the total number of victory points.
 	 * @return the total number of victory points.
 	 */
 	public int getVictoryPoints() {
 	    return this.permanentPoints + this.longestRoadPoints 
 	           + this.largestArmyPoints;
 	}
 	
 	/**
 	 * Increment the number of permanent victory points (settlements or
 	 * victory point dev cards)
 	 */
 	public void incrementPermanentPoints() {
 	    this.permanentPoints++;
 	}
 	
 	/**
 	 * Indicates whether or not the player has the longest road and sets the
 	 * victory points appropriately
 	 * @param hasLongestRoad
 	 */
 	public void setLongestRoad(boolean hasLongestRoad) {
 	    longestRoadPoints = hasLongestRoad ? 2 : 0;
 	}
 	
 	/**
 	 * Indicates whether or not the player has the largest army and sets the
 	 * victory points appropriately
 	 */
 	public void setLargestArmy(boolean hasLargestArmy) {
 	    largestArmyPoints = hasLargestArmy ? 2 : 0;
 	}
 	
 	public String getUsername()
 	{
 		return this.username;
 	}
 	
 	/**
 	 * Returns the color index of the player used by the GUI.
 	 * @return the color index.
 	 */
 	public int getColorIndex() {
 	    return colorIndex;
 	}
 	
 	public int getNumCards(String type)
 	{
 		
 		if (type.equals("ORE"))
 		{
 			return resCards[ResourceCard.ORE.getIndex()];
 		}else if (type.equals("WOOL"))
 		{
 			return resCards[ResourceCard.WOOL.getIndex()];
 		}else if (type.equals("WHEAT"))
 		{
 			return resCards[ResourceCard.WHEAT.getIndex()];
 		}else if (type.equals("LUMBER"))
 		{
 			return resCards[ResourceCard.LUMBER.getIndex()];
 		}else if (type.equals("BRICK"))
 		{
 			return resCards[ResourceCard.BRICK.getIndex()];
 		}else
 		{
 			return -1;
 		}
 		
 		
 	}
 	
 	public int getNumCards()
 	{
 		int total = 0;
 		for (int i = 0; i < 5; i++)
 		{
 			total += resCards[i];
 		}
 		return total;
 	}
 	
 	public boolean addCards(String type, int number)
 	{
 		if (type.equals("ORE"))
 		{
 			resCards[ResourceCard.ORE.getIndex()] = 
 			                     resCards[ResourceCard.ORE.getIndex()] + number;
 		}else if (type.equals("WOOL"))
 		{
 			resCards[ResourceCard.WOOL.getIndex()] = 
 			                    resCards[ResourceCard.WOOL.getIndex()] + number;
 		}else if (type.equals("WHEAT"))
 		{
 			resCards[ResourceCard.WHEAT.getIndex()] = 
 			                   resCards[ResourceCard.WHEAT.getIndex()] + number;
 		}else if (type.equals("LUMBER"))
 		{
 			resCards[ResourceCard.LUMBER.getIndex()] = 
 			                  resCards[ResourceCard.LUMBER.getIndex()] + number;	
 		}else if (type.equals("BRICK"))
 		{
 			resCards[ResourceCard.BRICK.getIndex()] = 
 			                   resCards[ResourceCard.BRICK.getIndex()] + number;
 		}else
 		{
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean removeCards(String type, int number)
 	{
 		if (type.equals("ORE"))
 		{
 			if (resCards[ResourceCard.ORE.getIndex()] <= number)
 			{
 				resCards[ResourceCard.ORE.getIndex()] = 
 				                 resCards[ResourceCard.ORE.getIndex()] - number;
 				return true;
 			}
 		}else if (type.equals("WOOL"))
 		{
 			if (resCards[ResourceCard.WOOL.getIndex()] <= number)
 			{
 				resCards[ResourceCard.WOOL.getIndex()] = 
 				                resCards[ResourceCard.WOOL.getIndex()] - number;
 				return true;
 			}
 		}else if (type.equals("WHEAT"))
 		{
 			if (resCards[ResourceCard.WHEAT.getIndex()] <= number)
 			{
 				resCards[ResourceCard.WHEAT.getIndex()] = 
 				               resCards[ResourceCard.WHEAT.getIndex()] - number;
 				return true;
 			}
 		}else if (type.equals("LUMBER"))
 		{
 			if (resCards[ResourceCard.LUMBER.getIndex()] <= number)
 			{
 				resCards[ResourceCard.LUMBER.getIndex()] = 
 				              resCards[ResourceCard.LUMBER.getIndex()] - number;
 				return true;
 			}
 		}else if (type.equals("BRICK"))
 		{
 			if (resCards[ResourceCard.BRICK.getIndex()] <= number)
 			{
 				resCards[ResourceCard.BRICK.getIndex()] = 
 				               resCards[ResourceCard.BRICK.getIndex()] - number;
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Adds an array of cards. Returns whether or not the add was successful,
 	 * which is based on whether or not the array is size 5 and has all nonzero
 	 * elements.
 	 * @param cards
 	 * @return whether or not the array was valid.
 	 */
 	public boolean addArrayOfCards(int cards[]) {
 	    boolean isArrayAdded = true;
 	    
 	    if (cards != null && cards.length == 5) {
             // Make sure all values are nonzero
             for (int card : cards) {
                 if (card < 0) {
                     isArrayAdded = false;
                     break;
                 }
             }
 	    } else {
 	        isArrayAdded = false;
 	    }
 	    
         // Add the cards
         if (isArrayAdded) {
             for (int i = 0; i < cards.length; i++) {
                 resCards[i] += cards[i];
             }
         }
         
         return isArrayAdded;
 	}
 	/**
 	 * Returns the number of knights played.
 	 * @return the number of knights played.
 	 */
 	public int getNumKnightsPlayed() {
 	    return numKnightsPlayed;
 	}
 	
 	/*private void addResCard(ResourceCard.CardType type)
 	 *{
 	 *
 	}*/
 	
 	public void addDevCard(DevelopmentCard card) {
 	    devCards.add(card);
 	    
 	    if (card.getDevCardType() == 
 	        DevelopmentCard.DevCardType.VICTORY_POINTS) {
 	        
 	        incrementPermanentPoints();
 	    }
 	}
 	
 	/**
 	 * Returns the list of development cards.
 	 * @return the list of development cards.
 	 */
 	public List<DevelopmentCard> getDevelopmentCards() {
 	    return this.devCards;
 	}
 	
 	/**
 	 * Removes one wheat, one wool, one brick, and one lumber from the player's
 	 * hand.
 	 */
 	public void doSettlementPurchase() {
 	    removeCards("WOOL", 1);
 	    removeCards("LUMBER", 1);
 	    removeCards("BRICK", 1);
 	    removeCards("WHEAT", 1);
 	}
 	
 	/**
 	 * Removes one brick and one lumber from the player's hand.
 	 */
 	public void doRoadPurchase() {
 		removeCards("LUMBER", 1);
 		removeCards("BRICK", 1);
 	}
 	
 	/**
 	 * Removes three ore and two wheat from the player's hand.
 	 */
 	public void doCityPurchase() {
 		removeCards("ORE", 3);
 		removeCards("WHEAT", 2);
 	}
 	
 	/**
 	 * Removes one ore, one wheat, and one wool from the player's hand.
 	 */
 	public void doDevCardPurchase() {
 		removeCards("WOOL", 1);
 		removeCards("ORE", 1);
 		removeCards("WHEAT", 1);
 	}
 	
 	
 	/**
 	 * Plays a knight if one is available.
 	 * @return whether or not a knight was successfully played.
 	 */
 	public boolean playKnight() {
 	    boolean isKnightPlayed = false;
 	    
 	    // Find a knight card and play it
 	    for (DevelopmentCard devCard : devCards) {
 	        if (devCard.getDevCardType() == 
 	            DevelopmentCard.DevCardType.KNIGHT) {
 	            
 	            devCards.remove(devCard);
 	            isKnightPlayed = true;
 	            
 	            numKnightsPlayed++;
 	            
 	            break;
 	        }
 	    }
 	    
 	    return isKnightPlayed;
 	}
 	
 	
 	/**
 	 * Overrides the toString method, so that when a Player is converted to
 	 * a String, the name of the player is returned.
 	 */
 	@Override
 	public String toString() {
 	    return username;
 	}
 }
