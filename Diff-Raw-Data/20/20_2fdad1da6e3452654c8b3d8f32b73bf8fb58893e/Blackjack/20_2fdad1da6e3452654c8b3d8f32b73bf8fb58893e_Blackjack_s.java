 package com.philihp.bj;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class Blackjack {
 	
 	/**
 	 * Number of decks in a shoe
 	 */
 	public static int SHOE_SIZE = 6;
 	
 	/**
 	 * Dealer Hits or Stands on a soft 17
 	 */
 	public static Response SOFT17 = Response.S;
 	
 	/**
 	 * Blackjack Payout
 	 */
 	public static float BLACKJACK_PAYOUT = 3f/2f;
 	
 	/**
 	 * Can Double-Down after Split?
 	 */
 	public static boolean DOUBLE_AFTER_SPLIT = true;
 	
 	/**
 	 * Amount of penetration into the shoe. 0.75 would mean to play 75% of the shoe.
 	 */
 	public static float CUT_CARD_PENETRATION = 0.66667f;
 	
 	public static int MIN_BET = 10;
 	
 	/**
 	 * House limit on number of resplits (max this many hands)
 	 */
 	public static int LIMIT_ON_RESPLITS = 4;
 
 	public static Random random;
 
 	public static void main(String[] args) {
 
 		random = new Random();
 		int seconds = 0;
 		long startTime = System.nanoTime();
 		if (args.length == 1) {
 			seconds = Integer.parseInt(args[0]);
 			System.out.println("Running for "+seconds+" seconds...");
 		}
 
 		Player player = new ZeroMemoryPlayer();
 		Player dealer = new DealerPlayer();
 		long money = 0; // 1,000,000
 		long handsPlayed = 0;
 		
 		for(;;) {
 			Deck deck = new Deck(random, SHOE_SIZE, player);
 			player.resetCount(SHOE_SIZE);
 			while ((float)deck.size() / deck.getInitialSize() > CUT_CARD_PENETRATION) {
 				handsPlayed++;
 	
 				List<Hand> playerHands = new ArrayList<Hand>(1);
 				playerHands.add(new Hand(player.bet(), deck.draw(), deck.draw(), false));
 				Hand dealerHand = new Hand(deck.draw(), deck.draw());
 				money -= playerHands.get(0).getBet();
 				
 				if(dealerHand.getValue() == 21) {
 					//dealer has blackjack. do not play out hands, just leave money on
 					//table and start over.
 					continue;
 				}
 				
 				money += playoutPlayer(player, deck, playerHands, dealerHand);
 	
 				playoutDealer(deck, dealer, dealerHand);
 	
 				for(Hand playerHand : playerHands) {
 					money += payout(playerHand, dealerHand);
 				}
 			}
 			
 			if((System.nanoTime()-startTime) / 1000000000f > seconds) break;
 		}
 		
 		System.out.println("Hands Played:    "+handsPlayed);
 		System.out.println("Money Delta:     "+money);
 		System.out.println("Min-Bet:         "+MIN_BET);
 		System.out.println("House Edge %:    "+(-100*(double)money / (handsPlayed * MIN_BET)));
 		System.out.println("...in "+((float)(System.nanoTime()-startTime)/1000000000f)+" seconds");
 	}
 
 	private static float playoutPlayer(Player player, Deck deck,
 			List<Hand> playerHands, Hand dealerHand) {
 		int i = 0;
 		float money = 0;
 		do {
 			Hand playerHand = playerHands.get(i);
 			
 			for(;;) {
 				Response response = player.prompt(playerHand, dealerHand, playerHands.size() < LIMIT_ON_RESPLITS);
 				if(response == Response.RH){
 					if(playerHand.size() == 2 && playerHand.isSplit() == false) {
 						playerHand.surrender();
 						break;
 					}
 					else response = Response.H;
 				}
 				if(response == Response.DH){
 					if(playerHand.canDoubleDown()) {
 						int bet = playerHand.getBet();
 						money -= bet;
 						playerHand.addBet(bet);
 						playerHand.add(deck.draw());
 						break;
 					}
 					else response = Response.H;
 				}
 				if(response == Response.H) {
 					playerHand.add(deck.draw());
 					if (playerHand.getValue() > 21) // bust!
 						break;
 					else
 						continue;
 				}
 				if(response == Response.DS) {
 					if(playerHand.canDoubleDown()) {
 						int bet = playerHand.getBet();
 						money -= bet;
 						playerHand.addBet(bet);
 						response = Response.S;
 					}
 					else response = Response.S;
 				}
 				if(response == Response.S) {
 					break;
 				}
 				if(response == Response.P) {
 					money -= playerHand.getBet();
 					Hand left = new Hand(playerHand.getBet(), playerHand.get(0), deck.draw(), true);
 					Hand right = new Hand(playerHand.getBet(), playerHand.get(1), deck.draw(), true);
 					playerHands.set(i, left);
 					playerHands.add(right);
 					playerHand = left;
 					continue;
 				}
 			}
 		}
 		while(++i < playerHands.size());
 		return money;
 	}
 
 	private static void playoutDealer(Deck deck, Player dealer, Hand dealerHand) {
		dealDealer: for (;;) {
			switch (dealer.prompt(null, dealerHand, false)) {
			case H:
				dealerHand.add(deck.draw());
				break;
			case S:
				break dealDealer;
			default:
				throw new RuntimeException("Dealer should never do anything but Hit or Stay. " + dealerHand);
			}

			if (dealerHand.getValue() > 21) // bust!
				break dealDealer;
 		}
 	}
 
 	private static int payout(Hand playerHand,
 			Hand dealerHand) {
 		if(playerHand.isSurrendered()) {
 			return playerHand.getBet() / 2;
 		}
 		else if(playerHand.isBlackjack() && dealerHand.isBlackjack()) {
 			//System.out.println("--- \tPush, both blackjacks");
 			return playerHand.getBet();
 		}
 		else if(playerHand.isBlackjack() && dealerHand.isBlackjack() == false) {
 			//System.out.println("Win \tPlayer BJ "+playerHand);
 			return (int)(playerHand.getBet()*(1+BLACKJACK_PAYOUT));
 		}
 		else if(playerHand.isBlackjack() == false && dealerHand.isBlackjack()) {
 			//System.out.println("Lose \tDealer BJ "+dealerHand);
 			return 0;
 		}
 		else if (playerHand.getValue() > 21) {
 			//System.out.println("Lose \tPlayer Bust "+playerHand.getValue());
 			return 0;
 		}
 		else if (dealerHand.getValue() > 21) {
 			//System.out.println("Win \tDealer Bust "+dealerHand.getValue());
 			return playerHand.getBet()*2;
 		}
 		else if(playerHand.getValue() > dealerHand.getValue()) {
 			//System.out.println("Win \tplayer="+playerHand.getValue()+", dealer="+dealerHand.getValue());
 			return playerHand.getBet()*2;
 		}
 		else if(playerHand.getValue() < dealerHand.getValue()) {
 			//System.out.println("Lose \tplayer="+playerHand.getValue()+", dealer="+dealerHand.getValue());
 			return 0;
 		}
 		else if (playerHand.getValue() == dealerHand.getValue()) {
 			//System.out.println("--- \tPush @ " + dealerHand.getValue());
 			return playerHand.getBet();
 		}
 		else throw new RuntimeException("Player: "+playerHand + ", Dealer: "+dealerHand);
 	}
 
 
 }
