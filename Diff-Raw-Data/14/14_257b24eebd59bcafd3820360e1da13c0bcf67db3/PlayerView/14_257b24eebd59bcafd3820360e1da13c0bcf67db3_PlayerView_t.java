 package com.blackjack.player;
 
 import com.blackjack.cards.Card;
 import com.blackjack.cards.Hand;
 import com.blackjack.gamecontrollers.Drill;
 import com.blackjack.gamecontrollers.DrillController;
 
 public class PlayerView {
 
 	private IPlayerPanel playerPanel;
 	private DrillController drillCtr;
 	private Hand dealerHand = new Hand();
 	private Hand playerHand = new Hand();
 	private Card dealerShowCard;
 
 	private PlayerView() {
 		super();
 		drillCtr = DrillController.createDrillController(this);
 	}
 
 	private void setPlayerPanel(IPlayerPanel playerPanel) {
 		this.playerPanel = playerPanel;
 	}
 
 	public static PlayerView createPlayerView(IPlayerPanel playerPanel) {
 		PlayerView playerView = new PlayerView();
 		playerView.setPlayerPanel(playerPanel);
 		return playerView;
 	}
 
 	public void startPlay() {
 		drillCtr.startPlay();
 	}
 
 	public void playerMove(Play play) {
 		// play the next move
 		boolean result = drillCtr.checkPlay(play, dealerShowCard, playerHand);
 		showResult(result);
 	}
 
 	public void showResult(boolean result) {
 		 System.out.println(result);
 	}
 
 	public void showCards() {
 		if (!playerHand.isAPair())
 			playerPanel.disableButton(Play.SPLIT);
 		playerPanel.showCards(playerHand, dealerHand);
 	}
 
 	public void setdrillCtr(DrillController drillCtr) {
 		this.drillCtr = drillCtr;
 	}
 
 	public void buttonPressed(Play buttonAction) {
 		drillCtr.doAction(buttonAction);
 	}
 
 	public void disableButton(Play play) {
 		playerPanel.disableButton(play);
 
 	}
 
 	public void enableButton(Play play) {
 		playerPanel.enableButton(play);
 
 	}
 
 	public void disableAllButtons() {
 		playerPanel.disableAllButtons();
 
 	}
 
 	public void enableAllButtons() {
 		playerPanel.enableAllButtons();
 
 	}
 
 	public void givePlayerACard(Card playerCard) {
 		playerHand.add(playerCard);
 		
 	}
 
 	public void giveDealerACard(Card dealerCard) {
 		dealerHand.add(dealerCard);
 		
 	}
 
 	public Card getDealerCard() {
 		return dealerHand.getHand().get(0);
 	}
 	
 	public Hand getPlayerHand() {
 		return playerHand;
 	}
 
 	public void emptyHands() {
 		dealerHand = new Hand();
 		playerHand = new Hand();
 	}
 
 	public void clearCards() {
 		emptyHands();
 		playerPanel.clearCards();
 	}
 
 	public void drillChange(Drill drillCommand) {
 		drillCtr.drillChange(drillCommand);
 		
 	}
 }
