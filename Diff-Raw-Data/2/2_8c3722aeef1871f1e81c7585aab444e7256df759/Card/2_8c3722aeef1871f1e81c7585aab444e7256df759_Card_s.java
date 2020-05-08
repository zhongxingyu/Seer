 import java.awt.Image;
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.swing.ImageIcon;
 
 /**
  * Abstract class for the card class
  * 
  * @author harrissa
  * 
  */
 public abstract class Card {
 
 	public int cost;
 	public String imagePath;
 	public int value;
 	public boolean defense;
 	public ArrayList<CardColor> cardColor = new ArrayList<CardColor>();
 	public ArrayList<Integer> effects = new ArrayList<Integer>();
 	public CardType cardType;
 	public String name;
 	public int[] input = new int[3];
 	public int amount = 1;
 	public ArrayList<Object> objList;
 	
 
 	
 	
 	public abstract ArrayList<Choice> getChoice(Game g);
 	
 	public abstract void use(ArrayList<Choice> choices);
 	
 	public abstract Card newCard();
 	
 	
 	/**
 	 * A method that will get a list of opponents
 	 * 
 	 * @param g
 	 * @return
 	 */
 	public ArrayList<String> getOpponents(Game g) {
 		objList = new ArrayList<Object>();
 		Player p = g.getCurrentPlayer();
 		ArrayList<Player> oppObj = g.players;
 		ArrayList<String> oppStrings = new ArrayList<String>();
 		int i = 1;
 		for (Player o : oppObj) {
 			if (!p.equals(o)) {
 				objList.add(o);
 				String name = "Player " + i;
 				oppStrings.add(name);
 			}
 			i++;
 		}
 		return oppStrings;
 	}
 
 	/**
 	 * A method that will return a list of gems in the players gempile
 	 * 
 	 * @param g
 	 * @return
 	 */
 	public ArrayList<String> getGempile(Game g) {
 		objList = new ArrayList<Object>();
 		Player p = g.getCurrentPlayer();
 		int[] gempile = p.gemPile;
 		ArrayList<String> gemStrings = new ArrayList<String>();
 		int whichGem = 1;
 		for (int gems : gempile) {
			if(gems>0){
 				this.objList.add(whichGem-1);
 				gemStrings.add(Integer.toString(whichGem) + " Gem");
 			}
 			whichGem++;
 		}
 		return gemStrings;
 	}
 	/**
 	 * A method that will return a list of cards in the player's hand
 	 * @param g
 	 * @return
 	 */
 	public ArrayList<String> getHand(Game g) {
 		Player p = g.getCurrentPlayer();
 		ArrayList<Card> h = p.hand;
 		ArrayList<String> handStrings = new ArrayList<String>();
 		for (Card card : h) {
 			handStrings.add(card.name);
 		}
 		return handStrings;
 	}
 
 	/**
 	 * A method that will return a list of cards in the player's bag
 	 * @param g
 	 * @return
 	 */
 	public ArrayList<String> getBag(Game g) {
 		Player p = g.getCurrentPlayer();
 		ArrayList<Card> b = p.bag;
 		ArrayList<String> bagStrings = new ArrayList<String>();
 		for (Card card : b) {
 			bagStrings.add(card.name);
 		}
 		return bagStrings;
 	}
 
 	public void discard(Player p) {
 		p.hand.remove(this);
 		p.discard.add(this);
 	}
 
 	/**
 	 * Method that trashed the chosen card from the players hand
 	 * 
 	 * @param p
 	 */
 	public void trashHand(Player p, int card) {
 		p.hand.remove(card);
 	}
 
 	/**
 	 * Effect that allows player p to crash n gems in their gem pile
 	 * 
 	 * @param p
 	 * @param n
 	 */
 	public void crash(Player crasher, Player crashee, int... gems) {
 		for (int i : gems) {
 			crasher.gemPile[i] = crasher.gemPile[i] - 1;
 			crashee.gemPile[0] = crashee.gemPile[0] + i + 1;
 		}
 	}
 
 	/**
 	 * Effect that allows player p to combine two gems into one gem in their gem
 	 * pile.
 	 * 
 	 * @param p
 	 */
 	public void combine(Player p, int... gems) {
 		int v = gems[0] + gems[1] + 1;
 		if (v < 3) {
 			p.gemPile[gems[0]] = p.gemPile[gems[0]] - 1;
 			p.gemPile[gems[1]] = p.gemPile[gems[1]] - 1;
 			p.gemPile[v] = p.gemPile[v] + 1;
 		}
 	}
 
 	/**
 	 * Effect that allows player p to draw n cards from his bag
 	 * 
 	 * @param p
 	 * @param n
 	 */
 	public void draw(Player p, int n) {
 		p.drawFromBag(n);
 	}
 
 	/**
 	 * Effect that allows player p to put a trap token on a stack in the bank,
 	 * then trash this chip. Each token give a wound to each player who buys
 	 * from that stack.
 	 * 
 	 * @param p
 	 */
 	// public void trap(Player p) {
 	//
 	// }
 
 	/**
 	 * Effect that allows player p to lock a card in his hand
 	 * 
 	 * @param p
 	 */
 	public void lock(Player p, int... cards) {
 		Arrays.sort(cards);
 		for (int counter = cards.length - 1; counter >= 0; counter--) {
 			p.lockedCards.add(p.hand.get(cards[counter]));
 			p.hand.remove(cards[counter]);
 		}
 	}
 
 	/**
 	 * Ongoing: Lock a card each turn, discard when you buy a purplecard
 	 * 
 	 * @param p
 	 */
 	// public void ongoingLock(Player p) {
 	//
 	// }
 
 	/**
 	 * Risky Move: Put a gem from your hand into your gem pile. If you do, gain
 	 * a gem of 1 higher value
 	 * 
 	 * @param p
 	 */
 	// public void risky(Player p, int gem) {
 	// // int v = p.hand.get(gem).value;
 	// // p.hand.remove(gem);
 	// // p.discard.add()
 	// }
 
 	public ArrayList<CardColor> colorList(CardColor... col) {
 		ArrayList<CardColor> colors = new ArrayList<CardColor>();
 		for (CardColor c : col) {
 			colors.add(c);
 		}
 		return colors;
 	}
 
 	public void setAmount(int num) {
 		this.amount = num;
 	}
 }
