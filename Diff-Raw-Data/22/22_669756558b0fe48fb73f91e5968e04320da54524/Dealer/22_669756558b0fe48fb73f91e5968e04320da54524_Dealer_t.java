 package NewBlackjack.src;
 
 import java.util.Vector;
 
 public class Dealer extends Player {
 
 	public Dealer() {
 		super("House");
 	}
 
 	public void playHand(Deck deck) {
 		boolean bust = false;
 
 		while (!bust) {
 			System.out.println(this.getName() + "'s hand:");
 			this.getHand().printHand();
 
 			while (!bust) {
 				if (this.getHand().getPoints() > 21) {
 					System.out.println("HOUSE BUST!");
 					bust = true;
 				}
				else if (((this.getHand().getPoints() >= 17) && !this.getHand().hasAce()) ||
						((this.getHand().getPoints() > 17) && this.getHand().hasAce()))
 					bust = true;
				else if ((this.getHand().getRawPoints() > this.getHand().getPoints()) && (this.getHand().getPoints() >= 17))
						bust = true;
 				else {
 					Card draw;
 					this.getHand().addCard(draw = deck.getNextCard());
 					System.out.println("The house drew " + draw.toString());
 				}
 			}
 		}
 	}
 
 	public void deal(Vector<Player> players, Deck deck, Dealer deal) {
 		for (Player p : players) {
 			p.addHand(new Hand(deck.getNextCard(), deck.getNextCard()));
 		}
 
 		deal.addHand(new Hand(deck.getNextCard(), deck.getNextCard()));
 	}
 }
