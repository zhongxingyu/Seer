 package game;
 
 import game.enumeration.Suit;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * Cette classe sert à représenter la main d'un joueur. Elle contient
  * donc une collection de carte.
  * @author Frédérik Paradis
  * @see Card
  * @see Deck
  * @see Suit
  */
 public class Hand {
 	
 	/**
 	 * Le nombre maximum de carte dans la main.
 	 */
 	final static public int MAX_CARDS = 10;
 	
 	/**
 	 * La collection de cartes de la main. L'attribut est « transient » parce que,
 	 * lors de la sérialisation, on ne veut pas que les cartes soient sérialisées
 	 * avec la main.
 	 */
 	private transient ArrayList<Card> hand = new ArrayList<Card>(MAX_CARDS);
 	
 	/**
 	 * Le nombre de carte de la main. Cet attribut existe parce que, lors de la 
 	 * sérialisation, nous n'avons plus les cartes et nous avons donc besoin de cet
 	 * attribut pour avoir le nombre de carte. 
 	 */
 	private int numberOfCard = 0;
 	
 	/**
 	 * L'atoût de la partie de la main.
 	 */
 	private Suit suit = null;
 	
 	/**
 	 * Le constructeur crée la main à partir d'un paquet de carte.
 	 * @param deck Le paquet de carte d'où les cartes de la main vont
 	 *             être prises.
 	 * @throws Exception
 	 * @see Deck
 	 */
 	public Hand(Deck deck) throws Exception {
 		for(int i = 0; i < MAX_CARDS; ++i) {
 			if(deck.isEmpty()) {
 				throw new Exception("The deck is empty.");
 			}
 			else {
 				this.hand.add(deck.takeCard());
 				++this.numberOfCard;
 			}
 		}
 	}
 	
 	/**
 	 * Cette méthode retourne l'atoût de la partie de la main.
 	 * @return Retourne l'atoût de la partie de la main si elle a été
 	 *         initialisée; sinon null.
 	 */
 	public Suit getGameSuit() {
 		return this.suit;
 	}
 	
 	/**
 	 * Cette méthode sert modifier l'atoût de la partie de la main.
 	 * L'atoût ne peut pas être « noir » ou « couleur ».
 	 * @param suit L'atoût de la partie
 	 * @return Retourne vrai si l'atoût est valide; faux sinon.
 	 */
 	public boolean setGameSuit(Suit suit) {
 		if(this.suit ==  Suit.BLACK || this.suit == Suit.COLOR)
 			return false;
 		
 		this.suit = suit;
 		return true;
 	}
 	
 	/**
 	 * Cette méthode retourne le tableau des cartes de la main.
 	 * @return Retourne le tableau des cartes de la main.
 	 */
 	public Card[] getCards() {
 		if(this.hand == null) {
 			return null;
 		} else {
 			Card cards[] = new Card[ this.hand.size() ];
 			this.hand.toArray( cards );
 			return cards;
 			//return (Card[])this.hand.toArray();
 		}
 	}
 	
 	/**
 	 * Cette méthode permet de changer les cartes de la main.
 	 * @param cards La collection de carte
 	 */
 	public void setCards(ArrayList<Card> cards) {
 		this.hand = (ArrayList<Card>) cards.clone();
 		this.numberOfCard = this.hand.size();
 	}
 	
 	/**
 	 * Cette méthode permet de jouer une carte d'une main.
 	 * @param card La carte à jouer
 	 * @return Retourne vrai si la carte a été trouvé et enlevé de la main;
 	 *         sinon faux.
 	 */
 	public boolean playCard(Card card) {
 		if(this.hand != null && this.hand.contains(card)) {
 			this.hand.remove(card);
 			--this.numberOfCard;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Cette méthode sert à savoir les cartes jouables selon l'atoût
 	 * du tour.
 	 * @param turn L'atoût du tour.
 	 * @return Retourne la collection de cartes jouables.
 	 */
 	public ArrayList<Card> getPlayableCard(Suit turn) {
		if(this.suit !=  null && !this.suit.equals(Suit.NONE) && this.hand != null && turn != null) {
 			ArrayList<Card> ret = new ArrayList<Card>();
 			Iterator<Card> itr = this.hand.iterator(); 
 			Card add = null;
 			
 			while(itr.hasNext()) {
 				add = itr.next();
 				if(add.getSuit().equals(turn)) {
 					ret.add(add);
 				}
 			}
 			
 			if(ret.size() != 0) {
 				/*try {
 					add = new Card(Suit.COLOR, CardValue.JOKER);
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				if(this.hand.contains(add)) {
 					ret.add(add);
 				}
 				
 				try {
 					add = new Card(Suit.BLACK, CardValue.JOKER);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if(this.hand.contains(add)) {
 					ret.add(add);
 				}*/
 				
 				return ret;
 			}
 			
 			
 			
 			/*//Si on n'a pas de carte avec l'atout du tour...
 			itr = this.hand.iterator(); 
 			while(itr.hasNext()) {
 				add = itr.next();
 				if(add.getSuit().equals(this.suit)) {
 					ret.add(add);
 				}
 			}
 			
 			if(this.suit == Suit.SPADES) {
 				try {
 					add = new Card(Suit.CLUBS, CardValue.JACK);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if(this.hand.contains(add)) {
 					ret.add(add);
 				}
 			}
 			else if(this.suit == Suit.CLUBS) {
 				try {
 					add = new Card(Suit.SPADES, CardValue.JACK);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if(this.hand.contains(add)) {
 					ret.add(add);
 				}
 			}
 			else if(this.suit == Suit.DIAMONDS) {
 				try {
 					add = new Card(Suit.HEARTS, CardValue.JACK);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if(this.hand.contains(add)) {
 					ret.add(add);
 				}
 			}
 			else if(this.suit == Suit.HEARTS) {
 				try {
 					add = new Card(Suit.DIAMONDS, CardValue.JACK);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if(this.hand.contains(add)) {
 					ret.add(add);
 				}
 			}
 			
 			if(ret.size() != 0) {
 				try {
 					add = new Card(Suit.COLOR, CardValue.JOKER);
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				if(this.hand.contains(add)) {
 					ret.add(add);
 				}
 				
 				try {
 					add = new Card(Suit.BLACK, CardValue.JOKER);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if(this.hand.contains(add)) {
 					ret.add(add);
 				}
 				
 				return ret;
 			}*/
 		}
 		
 		return this.hand;
 	}
 	
 	/**
 	 * Cette méthode retourne le nombre de carte dans la main.
 	 * @return Retourne le nombre de carte dans la main.
 	 */
 	public int getNumberOfCard() {
 		return this.numberOfCard;
 	}
 	
 	public String toString() {
 		
 		String hand = "";
 	
 		for( Card card : this.getCards() ) {
 			hand += card.toString() + "\n";
 		}
 		
 		return hand;
 		
 	}
 	
 	public boolean equals(Object obj) {
 		if(obj instanceof Hand) {
 			Hand hand = (Hand)obj;
 			if(hand.hand.size() == this.hand.size() &&
 					hand.numberOfCard == this.numberOfCard &&
 					hand.suit == this.suit) {
 				for(int i = 0; i < this.hand.size(); ++i) {
 					if(!hand.hand.get(i).equals(this.hand.get(i))) {
 						return false;
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 }
