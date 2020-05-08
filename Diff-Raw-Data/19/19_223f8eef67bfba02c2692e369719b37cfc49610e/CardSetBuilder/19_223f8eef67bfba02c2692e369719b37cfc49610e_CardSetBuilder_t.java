 package org.fazio.dominion.builders;
 
 import org.fazio.dominion.entities.Card;
 import org.fazio.dominion.entities.CardSet;
 
import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Michael Fazio <michael.fazio@kohls.com>
  * @since 10/31/12 11:10 AM
  */
 public class CardSetBuilder {
 
 	String name;
 	Card baneCard;
	List<Card> kingdomCards = new ArrayList<Card>();
	CardSet.StartingCardSet startingCardSet = CardSet.StartingCardSet.Standard;
 	boolean platinumColony;
 
 	public CardSetBuilder setName(final String name) {
 		this.name = name;
 		return this;
 	}
 
 	public CardSetBuilder setBaneCard(final Card baneCard) {
 		this.baneCard = baneCard;
 		return this;
 	}
 
 	public CardSetBuilder addKingdomCard(final Card kingdomCard) {
 		this.kingdomCards.add(kingdomCard);
 		return this;
 	}
 
 	public CardSetBuilder addKingdomCards(final List<Card> kingdomCards) {
 		this.kingdomCards.addAll(kingdomCards);
 		return this;
 	}
 
 	public CardSetBuilder setStartingCardSet(final CardSet.StartingCardSet startingCardSet) {
 		this.startingCardSet = startingCardSet;
 		return this;
 	}
 
 	public CardSetBuilder setPlatinumColony(final boolean platinumColony) {
 		this.platinumColony = platinumColony;
 		return this;
 	}
 
 	public CardSet build() {
 		return new CardSet(this.name, this.kingdomCards, this.baneCard, this.startingCardSet, this.platinumColony);
 	}
 }
