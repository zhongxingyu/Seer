 package herbstJennrichLehmannRitter.engine.model.impl;
 
 import herbstJennrichLehmannRitter.engine.enums.CardType;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Deck;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 public class DeckImpl implements Deck {
 	
 	private HandDeckImpl handDeck = new HandDeckImpl();
 	private CemeteryDeckImpl cemeteryDeck = new CemeteryDeckImpl();
 	private DeckStackImpl deckStack = new DeckStackImpl();
 	
 	private class HandDeckImpl {
 		
 		private Collection<Card> handDeck = new ArrayList<Card>();
 		
 		public Collection<Card> getCards() {
 			return Collections.unmodifiableCollection(this.handDeck);
 		}
 		
 		public void discardCard(Card card) {
 			this.handDeck.remove(card);
 			DeckImpl.this.cemeteryDeck.addCard(card);
 		}
 
 		public void discardAllCards() {
 			DeckImpl.this.cemeteryDeck.addCards(this.handDeck);
 			this.handDeck.clear();
 		}
 		
 		public void discardAllCardsByType(CardType cardType) {
 			Collection<Card> cardToRemove = new ArrayList<Card>();
 			
 			for (Card card : this.handDeck) {
 				if (card.getCardType() == cardType) {
 					cardToRemove.add(card);
 				}
 			}
 			
 			for (Card card : cardToRemove) {
 				discardCard(card);
 			}
 		}
 		
 		public boolean pickCard() {
 			if (this.handDeck.size() < 6) {
 				this.handDeck.add(DeckImpl.this.deckStack.pickCard());
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		public boolean pickCards(int numberOfCards) {
 			if (this.handDeck.size() < 6) {
 				for (int i = 0; i < numberOfCards; i++) {
 					if (this.handDeck.size() >= 6) {
 						break;
 					}
 					this.handDeck.add(DeckImpl.this.deckStack.pickCard());
 				}
 				return true;
 			} else {
 				return false;
 			}
 		}
 		
 		public boolean pickCardFromDeckStackOrCemeteryDeckWithCostAbout(int cost) {
 			if( this.handDeck.size() < 6) {
 				List<Card> cards = new ArrayList<Card>();
 				Card card = null;
 					
 				cards.addAll(DeckImpl.this.cemeteryDeck.getAllCards());
 				cards.addAll(DeckImpl.this.deckStack.getAllCards());
 				Collections.shuffle(cards);
 					
 				for (Card cardIteration : cards) {
 					if (cardIteration.getCostBrick() > cost 
 							|| cardIteration.getCostCrystal() > cost 
 							|| cardIteration.getCostMonsters() > cost) {
 						card = cardIteration;
 						break;
 					}
 				}
 					
 				if( card != null ) {
 					if (DeckImpl.this.cemeteryDeck.getAllCards().contains(card)) {
 						DeckImpl.this.cemeteryDeck.removeCard(card);
 					} else {
 						DeckImpl.this.deckStack.removeCard(card);
 					}
 					this.handDeck.add(card);
 					return true;
 				} else {
 					return false;
 				}
 			} else {
 				return false;
 			}
 		}
 
 		public boolean pickNumberOfCardsWithType(int numberOfCards, CardType cardType) {
 			if (this.handDeck.size() < 6) {
 				List<Card> cards = new ArrayList<Card>();
 				Card card;
 				
 				do {
 					card = DeckImpl.this.deckStack.pickCard();
 					if (card.getCardType() == cardType) {
 						cards.add(card);
 					}
 				} while (cards.size() < numberOfCards);
 				
 				Collections.shuffle(cards);
 				
 				for (int i = 0; i < numberOfCards; i++) {
 					if (this.handDeck.size() >= 6) {
 						break;
 					}
 					this.handDeck.add(cards.get(i));
 				}
 				return true;
 			} else {
 				return false;
 			}
 		}
 		
 		public void exchangeCardsWithHandDeck(HandDeckImpl handDeck) {
 			Collection<Card> tmp = this.handDeck;
 			this.handDeck = handDeck.handDeck;
 			handDeck.handDeck = tmp;
 		}
 	}
 	
 	private class CemeteryDeckImpl {
 
 		private Collection<Card> cemeteryDeck = new ArrayList<Card>();
 		
 		public void addCard(Card card) {
 			this.cemeteryDeck.add(card);
 		}
 		
 		public void addCards(Collection<Card> cards) {
 			this.cemeteryDeck.addAll(cards);
 		}
 		
 		public void removeCard(Card card) {
 			this.cemeteryDeck.remove(card);
 		}
 		
 		public Collection<Card> getAllCards() {
 			return this.cemeteryDeck;
 		}
 		
 		 public void clear() {
 			 this.cemeteryDeck.clear();
 		 }
 	}
 	
 	private class DeckStackImpl {
 
 		private List<Card> deckStack = new ArrayList<Card>();
 		
 		public void shuffle() {
 			Collections.shuffle(this.deckStack);
 		}
 		
 		public Card pickCard() {
 			if (this.deckStack.isEmpty()) {
 				refill();
 			}
 			
 			Card result = this.deckStack.get(0);
 			this.deckStack.remove(0);
 			return result;
 		}
 		
 		public void removeCard(Card card) {
 			this.deckStack.remove(card);
 		}
 		
 		private void refill() {
 			this.deckStack.addAll(DeckImpl.this.cemeteryDeck.getAllCards());
 			DeckImpl.this.cemeteryDeck.clear();
 		}
 		
 		public Collection<Card> getAllCards() {
 			return this.deckStack;
 		}
 	}
 	
 	public DeckImpl(Collection<Card> cards) {
 		this.deckStack.deckStack.addAll(cards);
 		this.deckStack.shuffle();
 		this.handDeck.pickCards(6);
 	}
 
 	@Override
 	public Collection<Card> getAllCards() {
 		return Collections.unmodifiableCollection(this.handDeck.getCards());
 	}
 
 	@Override
 	public void discardCard(Card card) {
 		this.handDeck.discardCard(card);
 	}
 
 	@Override
 	public void discardAllCards() {
 		this.handDeck.discardAllCards();
 	}
 
 	@Override
 	public void discardAllCardsByType(CardType cardType) {
 		this.handDeck.discardAllCardsByType(cardType);
 	}
 
 	@Override
 	public boolean pickCard() {
 		return this.handDeck.pickCard();
 	}
 
 	@Override
 	public boolean pickCards(int numberOfCards) {
 		return this.handDeck.pickCards(numberOfCards);
 	}
 
 	@Override
 	public boolean pickNumberOfCardsWithType(int numberOfCards,	CardType cardType) {
 		return this.handDeck.pickNumberOfCardsWithType(numberOfCards, cardType);
 	}
 
 	@Override
 	public boolean pickCardFromDeckStackOrCemeteryDeckWithCostAbout(int cost) {
 		return this.handDeck.pickCardFromDeckStackOrCemeteryDeckWithCostAbout(cost);
 	}
 
 	@Override
 	public void exchangeCardsWithHandDeck(Deck deck) {
 		if (deck instanceof DeckImpl) {
 			this.handDeck.exchangeCardsWithHandDeck(((DeckImpl)deck).handDeck);
 		}
 	}
 
 	@Override
 	public void shuffle() {
 		this.deckStack.shuffle();
 	}
	
	@Override
	public int getHandDeckSize() {
		return this.handDeck.handDeck.size();
	}
 }
