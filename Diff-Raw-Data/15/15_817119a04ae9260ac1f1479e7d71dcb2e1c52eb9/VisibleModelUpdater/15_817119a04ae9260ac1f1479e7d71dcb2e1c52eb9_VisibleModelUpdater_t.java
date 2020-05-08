 package de.nordakademie.nakjava.server.internal;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;
 
 import de.nordakademie.nakjava.gamelogic.cards.impl.Target;
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.PlayerState;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 import de.nordakademie.nakjava.server.internal.model.IdentityTransformator;
 import de.nordakademie.nakjava.server.internal.model.LeaveOutVisibleCheck;
 import de.nordakademie.nakjava.server.internal.model.Model;
 import de.nordakademie.nakjava.server.internal.model.Transformator;
 import de.nordakademie.nakjava.server.internal.model.VisibleField;
 import de.nordakademie.nakjava.server.internal.model.VisibleField.TargetInState;
 import de.nordakademie.nakjava.server.shared.serial.PlayerModel;
 
 public class VisibleModelUpdater {
 
 	// Suppresses default constructor for noninstantiability
 	private VisibleModelUpdater() {
 		throw new AssertionError();
 	}
 
 	private static <V> void updatePlayerModel(Player player, long sessionId) {
 		// TODO ErrorHandling: how are we doing it?
 		PlayerModel currentPlayerTransferModel = player.getState().getModel();
 
 		Session session = Sessions.getInstance().getSession(sessionId);
 		PlayerState self = session.getPlayerStateForPlayer(player);
 		PlayerState opponent = session.getPlayerStateForPlayer(session
 				.getOneOtherPlayer(player));
 		Model global = session.getModel();
 
 		flatScan(global, currentPlayerTransferModel, self.getState());
 		deepScan(self, currentPlayerTransferModel, Target.SELF, self.getState());
 		deepScan(opponent, currentPlayerTransferModel, Target.OPPONENT, self
 				.getState());
 
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
 
 		// Map<Target, String> targetToName = new HashMap<>();
 		// targetToName.put(Target.SELF, player.getName());
 		// switch (self.getState()) {
 		// case LOGIN:
 		// targetToName.put(Target.SELF, ((LoginSpecificModel) self
 		// .getStateSpecificModel()).getCurrentPartOfName());
 		// break;
 		// case CONFIGUREGAME:
 		// ConfigureGameSpecificModel configSpecificModel =
 		// (ConfigureGameSpecificModel) self
 		// .getStateSpecificModel();
 		// String deckName;
 		// if (configSpecificModel.getChosenDeck() == null) {
 		// deckName = null;
 		// } else {
 		// deckName = "StandardDeck";
 		//
 		// for (Entry<String, Set<String>> entry : player.getSavedDecks()
 		// .entrySet()) {
 		// if (entry.getValue() == configSpecificModel.getChosenDeck()) {
 		// deckName = entry.getKey();
 		// }
 		// }
 		// }
 		// Map<String, WinStrategyInformation> strategyDescription = new
 		// HashMap<>();
 		// for (String strategyName : WinStrategies.getInstance()
 		// .getStrategies()) {
 		// strategyDescription.put(strategyName, WinStrategies
 		// .getInstance().getStrategyInformationForName(
 		// strategyName));
 		// }
 		// currentPlayerTransferModel
 		// .setStateSpecificInfos(new ConfigurationSpecificInformation(
 		// strategyDescription, configSpecificModel
 		// .getWinStrategy(), deckName));
 		// break;
 		// case EDITDECK:
 		// EditDeckSpecificModel editDeckSpecificModel = (EditDeckSpecificModel)
 		// self
 		// .getStateSpecificModel();
 		// Map<CardInformation, Boolean> chosenCards = new HashMap<>();
 		// for (Entry<String, Boolean> card : editDeckSpecificModel
 		// .getChosenCards().entrySet()) {
 		// for (Entry<String, CardInformation> cardInfo : CardLibrary
 		// .get().getCardInformation().entrySet()) {
 		// if (cardInfo.getKey() == card.getKey()) {
 		// chosenCards.put(cardInfo.getValue(), card.getValue());
 		// }
 		// }
 		// }
 		// currentPlayerTransferModel
 		// .setStateSpecificInfos(new EditDeckSpecificInformation(
 		// chosenCards, editDeckSpecificModel
 		// .getCurrentPartOfDeckName()));
 		// break;
 		// case PLAYCARDSTATE:
 		// break;
 		// }
 		//
 		// Map<Target, State> targetToState = new HashMap<>();
 		// targetToState.put(Target.SELF, self.getState());
 		// Player otherPlayer = session.getOneOtherPlayer(player);
 		// if (otherPlayer != null) {
 		// if (opponent.getState() == State.LOGIN) {
 		// targetToName.put(Target.OPPONENT,
 		// ((LoginSpecificModel) opponent.getStateSpecificModel())
 		// .getCurrentPartOfName());
 		// } else {
 		// targetToName.put(Target.OPPONENT, otherPlayer.getName());
 		// }
 		// targetToState.put(Target.OPPONENT, opponent.getState());
 		// }
 		// currentPlayerTransferModel.setTargetToState(targetToState);
 		// currentPlayerTransferModel.setTargetToName(targetToName);
 	}
 
 	/**
 	 * Scans a passed object for fields, which are annotated with
 	 * {@link VisibleField}. If so, values of these fields are extracted and put
 	 * in the {@link PlayerModel}. Fields which are not explicitly excluded (
 	 * {@link LeaveOutVisibleCheck}) and which are not complex types as arrays
 	 * or lists will also be scanned.
 	 * 
 	 * @param obj
 	 *            Object which fields to scan (also private!)
 	 * @param transfer
 	 *            the values will be added to this transfer object
 	 * @param target
 	 *            only annotations with this target will be considered
 	 * @param state
 	 *            only annotation with this state will be considered
 	 */
 	private static void deepScan(Object obj, PlayerModel transfer,
 			Target target, State state) {
 		if (obj == null) {
 			return;
 		}
 		for (Field field : obj.getClass().getDeclaredFields()) {
 			if (field.isAnnotationPresent(VisibleField.class)) {
 
 				VisibleField annotation = field
 						.getAnnotation(VisibleField.class);
 
 				for (TargetInState tis : annotation.targets()) {
 					if (tis.target() == target
 							&& Arrays.asList(tis.states()).contains(state)) {
 						transfer.putGenericTransferObject(generateName(field,
 								obj, target), extractObject(field, obj,
 								annotation));
 					}
 				}
 
 			} else if (!field.isAnnotationPresent(LeaveOutVisibleCheck.class)) {
 				if (!field.getType().isArray()
 						&& !Collection.class.isAssignableFrom(field.getType())
						&& !Map.class.isAssignableFrom(field.getType())
						&& !isPrimitiveType(field.getType())) {
 					field.setAccessible(true);
 					try {
 						deepScan(field.get(obj), transfer, target, state);
 					} catch (IllegalArgumentException | IllegalAccessException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
	private static boolean isPrimitiveType(Class<?> clazz) {

		final String[] primitiveTypes = new String[] { "String", "Integer",
				"int", "Float", "float", "Double", "double", "Long", "long",
				"Character", "char", "Short", "short", "Boolean", "boolean",
				"Byte", "byte" };

		return Arrays.asList(primitiveTypes).contains(clazz.getSimpleName());

	}

 	/**
 	 * Scans the object for fields, which are annotated with
 	 * {@link VisibleField} annotations. Only {@link Target}.SELF should be used
 	 * in that case. Only the first target is considered.
 	 * 
 	 * @param obj
 	 *            object to flat scan for annotated fields
 	 * @param transfer
 	 *            the results of this fields will be added here
 	 * @param state
 	 *            this state must be in the annotation to consider this field
 	 */
 	private static void flatScan(Object obj, PlayerModel transfer, State state) {
 
 		for (Field field : obj.getClass().getDeclaredFields()) {
 
 			if (field.isAnnotationPresent(VisibleField.class)) {
 				VisibleField annotation = field
 						.getAnnotation(VisibleField.class);
 
 				for (TargetInState tis : annotation.targets()) {
 					if (Arrays.asList(tis.states()).contains(state)) {
 						transfer.putGenericTransferObject(generateName(field,
 								obj, tis.target()), extractObject(field, obj,
 								annotation));
 					}
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * Extracts the value from the annotated field. If a {@link Transformator}
 	 * class is mentioned in the annotation this class is instantiated and
 	 * transform() is invoked. Due to Generic Issues, this needs to be done via
 	 * reflection.
 	 * 
 	 * @param field
 	 * @param from
 	 * @param annotation
 	 * @return
 	 */
 	private static Object extractObject(Field field, Object from,
 			VisibleField annotation) {
 		field.setAccessible(true);
 		try {
 			Object value = field.get(from);
 			if (annotation.transformer() != IdentityTransformator.class) {
 				value = Transformator.class
 						.getMethod("transform", Object.class).invoke(
 								annotation.transformer().newInstance(), value);
 			}
 
 			return value;
 
 		} catch (IllegalArgumentException e) {
 			// TODO
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	private static String generateName(Field field, Object from, Target target) {
 		return from.getClass().getName() + "." + field.getName() + "." + target;
 	}
 
 	public static void update(long sessionId) {
 		Session session = Sessions.getInstance().getSession(sessionId);
 		for (Player player : session.getSetOfPlayers()) {
 			updatePlayerModel(player, sessionId);
 
 		}
 	}
 }
