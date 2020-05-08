 package org.dyndns.pamelloes.SpoutCasino.games;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.inventory.ItemStack;
 import org.dyndns.pamelloes.SpoutCasino.SpoutCasino;
 import org.dyndns.pamelloes.SpoutCasino.cards.Card;
 import org.dyndns.pamelloes.SpoutCasino.cards.Deck;
 import org.dyndns.pamelloes.SpoutCasino.gui.WaitingGui;
 import org.dyndns.pamelloes.SpoutCasino.gui.holdem.HoldemGui;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class HoldemController implements GameController {
 	private List<SpoutPlayer> requests = new ArrayList<SpoutPlayer>();
 	
 	private SpoutPlayer[] players = new SpoutPlayer[4];
 	private int[] bets = new int[4];
 	private Map<SpoutPlayer, Card[]> hands = new HashMap<SpoutPlayer, Card[]>();
 	private Map<SpoutPlayer, HoldemGui> guis = new HashMap<SpoutPlayer, HoldemGui>();
 	private List<Card> community = new ArrayList<Card>();
 	
 	private Deck deck = new Deck();
 	
 	private boolean inprogress = false;
 	private int dealer = -1, turn = -1;
 	private int pot = 0, call = 0;
 	private boolean round = false;
	private static final int TURN_LENGTH = 300;
 	private static final int GAME_BREAK = 600;
 	private int timeremaining = GAME_BREAK;
 
 	public HoldemController() {
 		deck.makeDeck(false);
 	}
 	
 	public Deck getDeck() {
 		return deck;
 	}
 	
 	public void onTick() {
 		acceptRequests();
 		if(!inprogress) {
 			if(timeremaining % 10 == 0) setTimeRemaining(timeremaining/10);
 			if(timeremaining == 0) {
 				if(getPlayerCount() >= 2) startGame();
 				else timeremaining = GAME_BREAK;
 			} else timeremaining--;
 		} else {
 			if(timeremaining % 10 == 0) setTimeRemaining(timeremaining/10);
 			if(timeremaining <= 0) {
 				if(timeremaining == 0) if(fold(turn)) {
 					for(int i = 0; i < 4; i++) if(players[i] != null) endGame(i);
 					return;
 				}
 				if(turn == dealer) {
 					if(round) {
 						if(community.size() == 0)  flop();
 						else if (community.size() == 3) turn();
 						else if (community.size() == 4) river();
 						else if (community.size() == 5) {
 							int[] winners = findWinner();
 							endGame(winners);
 							return;
 						}
 						round = false;
 					} else {
 						round = true;
 					}
 				}
 				nextTurn();
				if(round && bets[turn] == call) timeremaining = -1;
				else if(round) guis.get(players[turn]).setCall(call - bets[turn]);
 			} else timeremaining--;
 		}
 	}
 
 	public void handleEntryRequest(final SpoutPlayer player) {
 		requests.add(player);
 		player.getMainScreen().attachPopupScreen(new WaitingGui(player, new Runnable() {
 			public void run() {
 				requests.remove(player);
 			}
 		}));
 	}
 	
 	public void handlePlayerExit(SpoutPlayer player) {
 		hands.remove(player);
 		guis.remove(player);
 		int id = -1;
 		for(int i = 0; i < 4; i++) if(players[i] == player) {
 			players[i] = null;
 			id = i;
 		}
 		if(id == -1) return;
 		for(int i = 0; i < 4; i++) if(players[i] !=null) guis.get(players[i]).setState(id, 0);
 		if(getActivePlayerCount() == 1) {
 			for(int i = 0; i < 4; i++) if(players[i] != null) endGame(i);
 			return;
 		}
 		if(id == dealer && inprogress) nextDealer();
 		if(id == turn && inprogress) nextTurn();
 	}
 
 	public void handleForcedTermination() {
 		if(getActivePlayerCount() == 0) return;
 		int amount = pot/getActivePlayerCount();
 		for(SpoutPlayer p : players) if(p!=null && guis.containsKey(p)) {
 			giveChips(p, amount);
 			p.getMainScreen().closePopup();
 		}
 	}
 	
 	public void bet(int amount) {
 		setPot(pot+amount);
		bets[turn] = amount;
 	}
 	
 	public void advanceTurn() {
 		timeremaining = -1;
 	}
 	
 	public void fold() {
 		timeremaining = 0;
 	}
 	
 	public void setCall(int call) {
 		this.call = call;
		for(int i = 0; i < 4; i++) if(players[i]!=null) guis.get(players[i]).setCall(call);
 	}
 	
 	private void flop() {
 		deck.dealTop();//burn
 		Card a = deck.dealTop();
 		Card b = deck.dealTop();
 		Card c = deck.dealTop();
 		community.add(a);
 		community.add(b);
 		community.add(c);
 		for(int i = 0; i < 4; i++) if(players[i] != null) guis.get(players[i]).flop(a, b, c);
 		setCall(0);
 	}
 	
 	private void turn() {
 		deck.dealTop();//burn
 		Card a = deck.dealTop();
 		community.add(a);
 		for(int i = 0; i < 4; i++) if(players[i] != null) guis.get(players[i]).turn(a);
 		setCall(0);
 	}
 	
 	private void river() {
 		deck.dealTop();//burn
 		Card a = deck.dealTop();
 		community.add(a);
 		for(int i = 0; i < 4; i++) if(players[i] != null) guis.get(players[i]).turn(a);
 		setCall(0);
 	}
 	
 	private void endGame(int... ids) {
 		for(int id : ids) giveChips(players[id], (int) (((double)pot)/((double)ids.length)));
 		for(int i = 0; i < 4; i++) if(players[i]!=null) {
 			guis.get(players[i]).clearCommunity();
 			for(int j = 0; j < 4; j++) if(players[j]!=null) guis.get(players[i]).setState(j, 1);
 		}
 		inprogress = false;
 		setTurn(-1);
 		setDealer(-1);
 		timeremaining = GAME_BREAK;
 		setPot(0);
 		hands.clear();
 		community.clear();
 	}
 	
 	private void acceptRequests() {
 		Iterator<SpoutPlayer> request = requests.iterator();
 		for(int i = 0; i < 4 && request.hasNext(); i++) if(players[i]==null) {
 			enterGame(i,request.next());
 			request.remove();
 			break;
 		}
 	}
 	
 	private void enterGame(int id, SpoutPlayer player) {
 		players[id] = player;
 		WaitingGui.waiting.remove(player);
 		player.getMainScreen().closePopup();
 		HoldemGui gui = new HoldemGui(player,this,id);
 		gui.setState(id, 1);
 		for(int i = 0; i < 4; i++) {
 			if(i == id) continue;
 			if(players[i] == null) {
 				gui.setState(i, 0);
 			} else if(hands.containsKey(players[i])){
 				gui.setState(i, 2);
 				guis.get(players[i]).setState(id, 1);
 			} else {
 				gui.setState(i, 1);
 				guis.get(players[i]).setState(id, 1);
 			}
 		}
 		gui.setDealer(inprogress ? dealer : -1);
 		gui.setTurn(inprogress ? turn: -1, !round);
 		gui.setPot(pot);
 		gui.setTimeRemaining(timeremaining, inprogress);
 		if(community.size() > 2) gui.flop(community.get(0), community.get(1), community.get(2));
 		if(community.size() > 3) gui.turn(community.get(3));
 		if(community.size() > 4) gui.river(community.get(4));
 		guis.put(player, gui);
 		player.getMainScreen().attachPopupScreen(gui);
 	}
 	
 	private void startGame() {
 		inprogress = true;
 		round = false;
 		deck.makeDeck(false);
 		deck.shuffle();
 		for(int i = 0; i < 4; i++) if(players[i] != null) startGame(i);
 		setCall(0);
 		nextDealer();
 		turn = dealer;
 		nextTurn();
 	}
 	
 	private void startGame(int id) {
 		SpoutPlayer player = players[id];
 		Card[] hand = new Card[]{deck.dealTop(),deck.dealTop()};
 		hands.put(player, hand);
 		guis.get(player).showCards(hand[0], hand[1]);
 		for(int i = 0; i < 4; i++) {
 			if(players[i] == null) continue;
 			guis.get(players[i]).setState(id, 2);
 		}
 	}
 	
 	private void nextDealer() {
 		while(true) {
 			dealer = (dealer + 1) % 4;
 			if(players[dealer] == null || !hands.containsKey(players[dealer])) continue;
 			setDealer(dealer);
 			return;
 		}
 	}
 	
 	private void setDealer(int dealer) {
 		for(int i = 0; i < 4; i++) if(players[i] != null) guis.get(players[i]).setDealer(dealer);
 	}
 	
 	private void nextTurn() {
 		while(true) {
 			turn = (turn + 1) % 4;
 			if(players[turn] == null || !hands.containsKey(players[turn])) continue;
 			setTurn(turn);
 			timeremaining = TURN_LENGTH;
 			return;
 		}
 	}
 	
 	private void setTurn(int turn) {
 		for(int i = 0; i < 4; i++) if(players[i] != null) guis.get(players[i]).setTurn(turn, !round);
 	}
 	
 	private void setTimeRemaining(int time) {
 		for(int i = 0; i < 4; i++) if(players[i]!=null) guis.get(players[i]).setTimeRemaining(time, inprogress);
 	}
 	
 	private void setPot(int pot) {
 		this.pot = pot;
 		for(int i = 0; i < 4; i++) if(players[i]!=null) guis.get(players[i]).setPot(pot);
 	}
 	
 	private boolean fold(int player) {
 		for(int i = 0; i < 4; i++) if(players[i]!=null) guis.get(players[i]).setState(player, 3);
 		hands.remove(players[player]);
 		return getActivePlayerCount() == 1;
 	}
 	
 	private int getPlayerCount() {
 		int in = 0;
 		for(SpoutPlayer p : players) if(p!=null) in++;
 		return in;
 	}
 	
 	private int getActivePlayerCount() {
 		int in = 0;
 		for(SpoutPlayer p : players) if(p!=null && hands.containsKey(p)) in++;
 		return in;
 	}
 	
 	private int[] findWinner() {
 		Map<SpoutPlayer, Card[]> besthands = new HashMap<SpoutPlayer, Card[]>();
		for(int i = 0; i < 4; i++) if(players[i] != null) {
 			Card[] total = new Card[7];
 			System.arraycopy(hands.get(players[i]), 0, total, 0, 2);
 			System.arraycopy(community.toArray(), 0, total, 2, 5);
 			besthands.put(players[i], Poker.getBestHand(total));
 		}
 		List<Entry<SpoutPlayer, Card[]>> best = null;
 		for(Entry<SpoutPlayer, Card[]> entry : besthands.entrySet()) {
 			if(best == null) {
 				best = new ArrayList<Entry<SpoutPlayer, Card[]>>();
 				best.add(entry);
 				continue;
 			}
 			Card[] btemp = Poker.compareHands(best.get(0).getValue(), entry.getValue());
 			if(btemp!=null) {
 				if(entry.getValue().equals(btemp)) {
 					best.clear();
 					best.add(entry);
 				}
 			} else {
 				Card[] h1 = hands.get(best.get(0).getKey());
 				Card[] h2 = hands.get(entry.getKey());
 				boolean p1 = Poker.pair(h1);
 				boolean p2 = Poker.pair(h2);
 				if(p1 && p2) {
 					Card[] res = Poker.comparePair(h1,h2);
 					if(h2.equals(res)) {
 						best.clear();
 						best.add(entry);
 					}
 				} else if(p2 && !p1) {
 					best.clear();
 					best.add(entry);
 				} else if (!p1 && !p2){
 					Card[] res = Poker.comparePair(h1,h2);
 					if(h2.equals(res)) {
 						best.clear();
 						best.add(entry);
 					} else if(res == null) {
 						best.add(entry);
 					}
 				}
 			}
 		}
 		int[] results = new int[best.size()];
 		for(int i = 0; i < results.length; i++) for(int j = 0; j < 4; j++) if(best.get(i).getKey().equals(players[j])) {
 			results[i] = j;
 			break;
 		}
 		return results;
 	}
 	
 	public void giveChips(SpoutPlayer p, int amount) {
 		if(p==null || amount == 0) return;
 		int left = amount;
 		
 		int chips = 0;
 		while(left >= SpoutCasino.diamondrate)  {
 			left -= SpoutCasino.diamondrate;
 			chips++;
 			if(chips == 64) {
 				chips = 0;
 				p.getInventory().addItem(new SpoutItemStack(SpoutCasino.diamondchip,64));
 			}
 		}
 		p.getInventory().addItem(new SpoutItemStack(SpoutCasino.diamondchip,chips));
 		
 		chips = 0;
 		while(left >= SpoutCasino.goldrate)  {
 			left -= SpoutCasino.goldrate;
 			chips++;
 			if(chips == 64) {
 				chips = 0;
 				p.getInventory().addItem(new SpoutItemStack(SpoutCasino.goldchip,64));
 			}
 		}
 		p.getInventory().addItem(new SpoutItemStack(SpoutCasino.goldchip,chips));
 		
 		chips = 0;
 		while(left > 0) {
 			left--;
 			chips++;
 			if(chips == 64) {
 				chips = 0;
 				p.getInventory().addItem(new SpoutItemStack(SpoutCasino.ironchip,64));
 			}
 		}
 		p.getInventory().addItem(new SpoutItemStack(SpoutCasino.ironchip,chips));
 	}
 	
 	public void takeChips(SpoutPlayer p, int amnt) {
 		if(amnt > countChips(p)) throw new RuntimeException("Tried to take " + amnt + " chips from " + p.getName() + " but they only have " + countChips(p) + " chips!");
 		int chips = amnt;
 		ItemStack[] inv = p.getInventory().getContents();
 		for(ItemStack i : inv) {
 			if(i == null) continue;
 			SpoutItemStack sp = new SpoutItemStack(i);
 			if(sp.getMaterial().equals(SpoutCasino.ironchip)) {
 				if(i.getAmount() > chips)  {
 					i.setAmount(i.getAmount() - chips);
 					p.getInventory().setContents(inv);
 					return;
 				} else {
 					chips -= i.getAmount();
 					i.setAmount(0);
 				}
 			}
 			if(sp.getMaterial().equals(SpoutCasino.goldchip)) {
 				while(chips >= SpoutCasino.goldrate)  {
 					chips -= SpoutCasino.goldrate;
 					i.setAmount(i.getAmount() - 1);
 					if(i.getAmount() == 0) break;
 				}
 			}
 			if(sp.getMaterial().equals(SpoutCasino.diamondchip)) {
 				while(chips >= SpoutCasino.diamondrate)  {
 					chips -= SpoutCasino.diamondrate;
 					i.setAmount(i.getAmount() - 1);
 					if(i.getAmount() == 0) break;
 				}
 			}
 		}
 		p.getInventory().setContents(inv);
 		if(chips == 0) return;
 		for(ItemStack i : inv) {
 			if(i == null) continue;
 			SpoutItemStack sp = new SpoutItemStack(i);
 			if(sp.getMaterial().equals(SpoutCasino.ironchip)) { } //shouldn't be any left now.
 			if(sp.getMaterial().equals(SpoutCasino.goldchip)) {
 				if(i.getAmount() * SpoutCasino.goldrate > chips) {
 					int amount = i.getAmount() * SpoutCasino.goldrate;
 					i.setAmount(0);
 					p.getInventory().setContents(inv);
 					amount -= chips;
 					giveChips(p, amount);
 					return;
 				}
 			}
 			if(sp.getMaterial().equals(SpoutCasino.diamondchip)) {
 				if(i.getAmount() * SpoutCasino.diamondrate > chips) {
 					int amount = i.getAmount() * SpoutCasino.diamondrate;
 					i.setAmount(0);
 					p.getInventory().setContents(inv);
 					amount -= chips;
 					giveChips(p, amount);
 					return;
 				}
 			}
 		}
 		throw new RuntimeException("Couldn't take " + amnt + " chips from " + p.getName() + "!!");
 	}
 	
 	public int countChips(SpoutPlayer p) {
 		int chips = 0;
 		ItemStack[] inv = p.getInventory().getContents();
 		for(ItemStack i : inv) {
 			if(i == null) continue;
 			SpoutItemStack sp = new SpoutItemStack(i);
 			if(sp.getMaterial().equals(SpoutCasino.ironchip)) chips += i.getAmount();
 			if(sp.getMaterial().equals(SpoutCasino.goldchip)) chips += i.getAmount() * SpoutCasino.goldrate;
 			if(sp.getMaterial().equals(SpoutCasino.diamondchip)) chips += i.getAmount() * SpoutCasino.diamondrate;
 		}
 		return chips;
 	}
 }
