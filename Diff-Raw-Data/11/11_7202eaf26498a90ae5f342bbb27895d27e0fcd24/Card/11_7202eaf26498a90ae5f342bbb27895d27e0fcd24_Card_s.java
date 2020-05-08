 package se.chalmers.dat255.risk.model;
 
 /**
  * A class for representing a card in the game.
  * A card either has a type of unit and the id of a province in the form of a string
  * or has the type joker and the id joker. A joker is equal to any other card 
  * according to the overwritten equals method.
  * 
 * @author Emma Håkansson and Christoffer Matsson
  * @since 2013-09-16
  *
  */
 public class Card {
 	
 	private String provinceName;
 	private Card.CardType type;
 	
 	/**
 	 * Constructor for creating a card with the given type and string
 	 * or the string "Joker" if the given type is JOKER.
 	 * @param type, the type of the card.
 	 * @param name, the name of the province belonging to the card being created.
 	 */
 	public Card(Card.CardType type, String name){
 		if(type == CardType.JOKER){
 			provinceName = "Joker";
 		}else{
 			provinceName = name;
 		}
 		this.type = type;	
 	}
 	
 	/**
 	 * Method for accessing the name(id) of the province belonging to a card.
 	 * @return the name of the province.
 	 */
 	public String getName(){
 		return provinceName;
 	}
 	
 	/**
 	 * Method for accessing the type of a card. 
 	 * @return
 	 */
 	public Card.CardType getType(){
 		return this.type;
 	}
 	
 	/**
 	 * Compares two Cards.
 	 * Returns true if cards are of the same type 
 	 * and when comparing with a card of the type JOKER.
 	 */
 	@Override
 	public boolean equals(Object o){
 		if(o == null){
 			return false;
 		}
 		if(o instanceof Card){
 			Card tmp = (Card) o;
 			if(this.type == CardType.JOKER || tmp.type == CardType.JOKER){
 				return true;
 			}
 			return this.type == tmp.type;
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return super.hashCode();
 	}
 	
 	/**
 	 * Enum for representing the different types of cards which will exist in the deck.
 	 */
 	public static enum CardType {INFANTRY, CAVALRY, ARTILLERY, JOKER}
 }
