 package manager;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import cards.*;
 import player.*;
 
 public class GameManager {
 	
 	private List<Player> players;
 	private Pile deck;
 	private Pile communityCards;
 	
 	
 	private int pot;
 	private int bet;
 	
 	public GameManager(){
 		players = new ArrayList<Player>();
 		deck = Deck.fullDeck();
 	}
 	
 	public void addPlayer(Player p){
 		players.add(p);
 	}
 	
 	public void playGames(int hands) {
 		for(int i = 0; i < hands; i++) {
 			playHand(i % players.size());
 		}
 	}
 	
 	private void playHand(int button){
 	
 		List<Player> activePlayers = new ArrayList<Player>();
 		Collections.copy(activePlayers,players);
 		
 		bet = 0;
 		int raises = 0;
 		boolean raise = false;
 		dealHoleCards();
 		
 		
 		// Preflop	
 		int index = button;
 		
 		// Small blind
		index = (index + 1) % (activePlayers.size() -1); 
 		activePlayers.get(index).updateStack(-1);
 		activePlayers.get(index).updateAmountWagered(1);
 		updatePot(1);
 		
 		// Big blind
 		index = (index + 1) % activePlayers.size(); 
 		activePlayers.get(index).updateStack(-2);
 		activePlayers.get(index).updateAmountWagered(2);
 		updatePot(2);
 		
 		//Cycle through each remaining active player.
 		while(true) {
 			Player player = players.get(index);
 			Action action = player.act(Round.PREFLOP,communityCards, bet, raises, pot);
 			switch(action.getType()) {
 			case FOLD: 
 				player.setPreFlopAction(Action.fold());
 				activePlayers.remove(player);
 				break;
 			case CALL:
 				player.setPreFlopAction(Action.call());
 				player.updateStack(player.getAmountWagered()-bet);
 				player.updateAmountWagered(bet- player.getAmountWagered());
 				updatePot(bet);
 				break;
 			case RAISE:
 				player.setPreFlopAction(Action.raise(3*bet));
 				player.updateAmountWagered(3*bet);
 				player.updateStack(-3*bet);
 				updatePot(3*bet);
 				raises++;
 				break;
 			case BET:
 				activePlayers.get(index).updateStack(-bet);
 				player.setPreFlopAction(Action.bet(bet));
 				player.updateAmountWagered(bet);
 				updatePot(bet);
 				bet = action.getBet();
 				break;	
 			}
 			for(Player p : activePlayers) {
 				int callCount = 0;
 				//Check if the last player action was to call.
 				if(p.getPreFlopActions().get(p.getFlopActions().size() - 1).getType() == Action.Type.CALL) {
 					callCount++;
 				}
 				//If all but one player has called it's time to see a flop.
 				if(callCount == activePlayers.size() - 1) {
 					break;
 				}
 			}
 			index = (index + 1) % activePlayers.size();
 			
 			//All but one have folded and the hand ends.
 			if(activePlayers.size() == 1) {
 				activePlayers.get(index).updateStack(pot);
 				break;
 			}
 		}
 		
 		communityCards.clear();
 	}
 	
 	private void dealHoleCards() {
 		for(Player player : players) {
 			player.addCard(deck.pop());
 			player.addCard(deck.pop());
 		}
 		
 	}
 	
 	private void updatePot(int delta) {
 		this.pot += delta;
 		
 	}
 }
