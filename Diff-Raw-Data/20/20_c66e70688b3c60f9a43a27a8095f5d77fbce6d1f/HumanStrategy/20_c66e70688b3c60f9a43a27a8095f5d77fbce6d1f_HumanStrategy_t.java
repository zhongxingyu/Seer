 package org.jdominion.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdominion.Card;
 import org.jdominion.CardList;
 import org.jdominion.Game;
 import org.jdominion.Hand;
 import org.jdominion.IPlayer;
 import org.jdominion.Player;
 import org.jdominion.Supply;
 import org.jdominion.Turn;
 import org.jdominion.aiStrategies.IStrategy;
 import org.jdominion.decisions.ChooseCardFromSupply;
 import org.jdominion.decisions.ChooseCardsFromHand;
 import org.jdominion.decisions.ChooseFromRevealedCards;
 import org.jdominion.decisions.Decision;
 import org.jdominion.decisions.MultipleChoice;
 import org.jdominion.decisions.YesNoDecision;
 import org.jdominion.decisions.multipleChoice.Choice;
 import org.jdominion.effects.CardEffect;
 import org.jdominion.event.EventManager;
 
 public class HumanStrategy implements IStrategy {
 
 	private MainWindow mainWindow = null;
 	private Player player;
 
 	public HumanStrategy() {
 	}
 
 	public HumanStrategy(MainWindow mainWindow) {
 		this.mainWindow = mainWindow;
 	}
 
 	@Override
 	public void gameStarted(Game game) {
 		if (mainWindow == null) {
 			initializeMainWindow(game);
 		}
 
 	}
 
 	public void initializeMainWindow(Game game) {
 		List<IPlayer> players = new ArrayList<IPlayer>();
 		for (Player player : game.getPlayers()) {
 			players.add(player);
 		}
 
 		mainWindow = new MainWindow(new GuiInformationSource(game, player.getHand(), players, game.getSupply()));
 		mainWindow.setVisible(true);
 
 		GUIEventHandler observer = new GUIEventHandler(mainWindow);
 		observer.registerForEvents(EventManager.getInstance());
 	}
 
 	@Override
 	public String getName() {
 		return "Human";
 	}
 
 	@Override
 	public List<Class<? extends Card>> getNeededCards() {
 		return new ArrayList<Class<? extends Card>>();
 	}
 
 	@Override
 	public boolean canWorkWithSupply(Supply supply) {
 		return true;
 	}
 
 	@Override
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	@Override
 	public void decide(Decision<?> decision, CardEffect effect, Hand hand, Turn currentTurn, Supply supply) {
 		// TODO Auto-generated method stub
 	}
 
 	public void decide(ChooseCardsFromHand decision, CardEffect effect, Hand hand, Turn currentTurn, Supply supply) {
 		CardList choosenCards;
 		do {
 			choosenCards = new CardList();
 			for (int i = 0; i < decision.getMaximumNumberOfCards(); i++) {
 				Card choosenCard = mainWindow.chooseCardFromHand(decision.getUserMessage(), i >= decision.getMinimumNumberOfCards() || decision.isCancelable());
 				if (choosenCard == null) {
 					if (choosenCards.isEmpty()) {
 						decision.setCanceled(true);
 						return;
 					} else {
 						break;
 					}
 				}
 				choosenCards.add(choosenCard);
 			}
 		} while (!decision.isValidAnswer(choosenCards));
 		decision.setAnswer(choosenCards);
 	}
 
 	public void decide(ChooseCardFromSupply decision, CardEffect effect, Hand hand, Turn currentTurn, Supply supply) {
 		Class<? extends Card> choosenCard;
		do {
 			choosenCard = mainWindow.chooseCardFromSupply(decision.getUserMessage(), decision.getAvailableCards(), decision.isCancelable());
			if (choosenCard == null && decision.isCancelable()) {
 				decision.setCanceled(true);
				return;
 			}
		} while (!decision.isValidAnswer(choosenCard));

		decision.setAnswer(choosenCard);
 	}
 
 	public void decide(YesNoDecision decision, CardEffect effect, Hand hand, Turn currentTurn, Supply supply) {
 		boolean answer = mainWindow.askUser(decision.getUserMessage());
 		decision.setAnswer(answer);
 	}
 
 	public void decide(ChooseFromRevealedCards decision, CardEffect effect, Hand hand, Turn currentTurn, Supply supply) {
 		decision.chooseDefaultAnswer(hand, currentTurn, supply);
 		mainWindow.chooseFromRevealedCards(decision);
 		decision.setAnswer(decision.getRevealedCards());
 	}
 
 	public void decide(MultipleChoice<Choice> decision, CardEffect effect, Hand hand, Turn currentTurn, Supply supply) {
 
 		List<Choice> answer = mainWindow.multipleChoice(decision.getUserMessage(), decision.isCancelable(), decision.getMinAnswers(), decision.getMaxAnswers(),
 				decision.getChoices());
 		decision.setAnswer(answer);
 	}
 
 }
