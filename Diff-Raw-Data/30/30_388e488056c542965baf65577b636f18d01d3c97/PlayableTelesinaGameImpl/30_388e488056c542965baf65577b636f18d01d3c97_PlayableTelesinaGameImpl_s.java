 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.juhokall.telesina.io;
 
 import com.google.inject.Inject;
 import com.juhokall.telesina.game.TelesinaGameImpl;
 import com.juhokall.telesina.game.action.TelesinaGameAction;
 import com.juhokall.telesina.model.Player;
 import com.juhokall.telesina.model.Situation;
 import com.juhokall.telesina.model.TelesinaHand;
 import com.juhokall.telesina.model.core.Telesina;
 import com.juhokall.telesina.rules.TelesinaHandRater;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 /**
  *
  * @author juho
  */
 public class PlayableTelesinaGameImpl extends TelesinaGameImpl implements PlayableTelesinaGame {
 
 	private Random random;
 	private Situation situation;
 	private int street;
 	private Set deck;
 	private TelesinaHandRater handRater;
 
 	public PlayableTelesinaGameImpl(TelesinaGameAction action) {
 		super(action);
 		random = new Random();
 	}
 
 	@Inject
 	public PlayableTelesinaGameImpl(TelesinaHandRater handRater, TelesinaGameAction action) {
 		super(action);
 		this.handRater = handRater;
 	}
 
 	@Override
 	public void setNewGame(int numberOfPlayers) {
 		super.setNewGame(numberOfPlayers);
 		situation = super.getSituation();
 		random = new Random();
 		deck = super.getDeck();
 	}
 
 	@Override
	public void dealNextStreet() {
 		int card;
 		if (street < Telesina.NORMAL_STREET_COUNT) {
 			for (int playerId : situation.getPlayerIds()) {
 					card = dealCard();
 					super.dealCardForPlayer(card, playerId);
 			}
 		} else if (street == Telesina.NORMAL_STREET_COUNT) {
 			card = random.nextInt(deck.size());
 			for (int playerId : situation.getPlayerIds()) {
 				super.dealCardForPlayer(card, playerId);
 			}
 		} else {
 		}
 		setActivePlayer(situation);
 		situation.setPlayersLeft(situation.getPlayerCount());
 		street++;
 	}
 
 	@Override
 	public int getActivePlayer() {
 		return situation.getActivePlayerId();
 	}
 
 	private void setActivePlayer(Situation situation) {
 		int strongestHand = 0;
 		for (int playerId : situation.getPlayerIds()){
 			TelesinaHand hand = situation.getPlayer(playerId).getHand();
 			int handStrength = handRater.getTelesinaHandValue(hand);
 			if (handStrength > strongestHand) {
 				strongestHand = handStrength;
 				situation.setActivePlayer(playerId);
 			}
 		}
 	}
 
 	private int dealCard() {
 		int card;
 		do {
 			card = random.nextInt(Telesina.DECK_LENGTH);
 		} while (!deck.contains(card));
 		deck.remove(card);
 		return card;
 	}
 
 	@Override
 	public int dealHoleCard(int playerNumber) {
 		return dealCard();	
 	}
 
 	@Override
 	public int[] getPlayersCards(int playerNumber) {
 		return getPlayersHand(playerNumber).getCards();
 	}
 	@Override
 	public TelesinaHand getPlayersHand(int playerNumber) {
 		Player player = situation.getPlayer(playerNumber);
 		TelesinaHand hand = player.getHand();
 		return hand;	
 	}
 }
