 package napoleon.model.role;
 
 import java.util.List;
 
 import napoleon.model.card.Card;
 import napoleon.model.card.CardSet;
 import napoleon.model.player.Player;
 import napoleon.model.rule.GameContext;
 
 
 public class Dealer {
 
 	private static final int Rounds = 12;
 	private static final int RestOfCardsAfterServe = 5;
 	private List<Card> cards;
 	private final GameContext gameContext;
 
 	public Dealer(GameContext gameContext) {
 		this.gameContext = gameContext;
 		cards = CardSet.New().AllCards();
 	}
 
 	public void Serve() {
 		Player[] players = gameContext.getPlayers();
 		for (int i = 0; i < Rounds; i++) {
 			players[0].takeCard(getRandomCard());
 			players[1].takeCard(getRandomCard());
 			players[2].takeCard(getRandomCard());
 			players[3].takeCard(getRandomCard());
 		}
		gameContext.addClosedCardsOnTable(cards);
 	}
 
 	private Card getRandomCard() {
 		Card card = cards.get((int)(Math.random() * cards.size()));
 		cards.remove(card);
 		return card;
 	}
 
 	public Integer cardCount() {
 		return cards.size();
 	}
 
 	public static Dealer New(GameContext gameContext) {
 		return new Dealer(gameContext);
 	}
 
 	public boolean hasServed() {
 		return cards.size() == RestOfCardsAfterServe;
 	}
 }
