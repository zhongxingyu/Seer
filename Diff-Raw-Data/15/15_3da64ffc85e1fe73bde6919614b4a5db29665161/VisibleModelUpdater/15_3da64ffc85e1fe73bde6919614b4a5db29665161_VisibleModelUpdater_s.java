 package de.nordakademie.nakjava.server.internal;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import de.nordakademie.nakjava.gamelogic.cards.impl.CardLibrary;
 import de.nordakademie.nakjava.gamelogic.cards.impl.Target;
import de.nordakademie.nakjava.gamelogic.shared.WinStrategyInformation;
 import de.nordakademie.nakjava.gamelogic.shared.cards.CardInformation;
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.PlayerState;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.winstrategies.WinStrategies;
 import de.nordakademie.nakjava.server.internal.model.ConfigureGameSpecificModel;
 import de.nordakademie.nakjava.server.internal.model.EditDeckSpecificModel;
 import de.nordakademie.nakjava.server.internal.model.LoginSpecificModel;
 import de.nordakademie.nakjava.server.shared.serial.PlayerModel;
 import de.nordakademie.nakjava.server.shared.serial.stateSpecificInfos.ConfigurationSpecificInformation;
 import de.nordakademie.nakjava.server.shared.serial.stateSpecificInfos.EditDeckSpecificInformation;
 
 public class VisibleModelUpdater {
 
 	// Suppresses default constructor for noninstantiability
 	private VisibleModelUpdater() {
 		throw new AssertionError();
 	}
 
 	private static <V> void updatePlayerModel(Player player, long sessionId) {
 		// TODO ErrorHandling: how are we doing it?
 		PlayerModel playerModel = player.getState().getModel();
 		Session session = Sessions.getInstance().getSession(sessionId);
 		PlayerState self = session.getPlayerStateForPlayer(player);
 		PlayerState opponent = session.getPlayerStateForPlayer(session
 				.getOneOtherPlayer(player));
 		// TODO create something like "ActionRules"
 		// TODO insert StateSpecificInfos
 		// TODO setting of dirtyBit via "ActionRules"
 		/*
 		 * playerModel.setArtifacts(self.getArtifacts()); List<CardInformation>
 		 * cards = new ArrayList<>(); for (String cardName :
 		 * self.getCards().getCardsOnHand()) { CardInformation cardInfo =
 		 * CardLibrary.get().getCardInformation() .get(cardName); if (cardInfo
 		 * != null) { cards.add(cardInfo); } else { // throw new
 		 * Exception("Card not found in CardLibrary"); } }
 		 * playerModel.setCardHand(cards);
 		 */
 
 		Map<Target, String> targetToName = new HashMap<>();
 		targetToName.put(Target.SELF, player.getName());
 		switch (self.getState()) {
 		case LOGIN:
 			targetToName.put(Target.SELF, ((LoginSpecificModel) self
 					.getStateSpecificModel()).getCurrentPartOfName());
 			break;
 		case CONFIGUREGAME:
 			ConfigureGameSpecificModel configSpecificModel = (ConfigureGameSpecificModel) self
 					.getStateSpecificModel();
 			String deckName;
 			if (configSpecificModel.getChosenDeck() == null) {
 				deckName = null;
 			} else {
 				deckName = "StandardDeck";
 
 				for (Entry<String, Set<String>> entry : player.getSavedDecks()
 						.entrySet()) {
 					if (entry.getValue() == configSpecificModel.getChosenDeck()) {
 						deckName = entry.getKey();
 					}
 				}
 			}
 			Map<String, WinStrategyInformation> strategyDescription = new HashMap<>();
 			for (String strategyName : WinStrategies.getInstance()
 					.getStrategies()) {
				strategyDescription.put(strategyName, null);
 			}
 			playerModel
 					.setStateSpecificInfos(new ConfigurationSpecificInformation(
 							strategyDescription, configSpecificModel
 									.getWinStrategy(), deckName));
 			break;
 		case EDITDECK:
 			EditDeckSpecificModel editDeckSpecificModel = (EditDeckSpecificModel) self
 					.getStateSpecificModel();
 			Map<CardInformation, Boolean> chosenCards = new HashMap<>();
 			for (Entry<String, Boolean> card : editDeckSpecificModel
 					.getChosenCards().entrySet()) {
 				for (Entry<String, CardInformation> cardInfo : CardLibrary
 						.get().getCardInformation().entrySet()) {
 					if (cardInfo.getKey() == card.getKey()) {
 						chosenCards.put(cardInfo.getValue(), card.getValue());
 					}
 				}
 			}
 			playerModel.setStateSpecificInfos(new EditDeckSpecificInformation(
 					chosenCards, editDeckSpecificModel
 							.getCurrentPartOfDeckName()));
 			break;
 		case PLAYCARDSTATE:
 			break;
 		}
 
 		Map<Target, State> targetToState = new HashMap<>();
 		targetToState.put(Target.SELF, self.getState());
 		Player otherPlayer = session.getOneOtherPlayer(player);
 		if (otherPlayer != null) {
 			if (opponent.getState() == State.LOGIN) {
 				targetToName.put(Target.OPPONENT,
 						((LoginSpecificModel) opponent.getStateSpecificModel())
 								.getCurrentPartOfName());
 			} else {
 				targetToName.put(Target.OPPONENT, otherPlayer.getName());
 			}
 			targetToState.put(Target.OPPONENT, opponent.getState());
 		}
 		playerModel.setTargetToState(targetToState);
 		playerModel.setTargetToName(targetToName);
 	}
 
 	public static void update(long sessionId) {
 		Session session = Sessions.getInstance().getSession(sessionId);
 		for (Player player : session.getSetOfPlayers()) {
 			updatePlayerModel(player, sessionId);
 
 		}
 	}
 }
