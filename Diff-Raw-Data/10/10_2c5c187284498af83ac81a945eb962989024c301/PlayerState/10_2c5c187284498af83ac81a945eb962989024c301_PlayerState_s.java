 package de.nordakademie.nakjava.gamelogic.shared.playerstate;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import de.nordakademie.nakjava.gamelogic.cards.impl.Target;
 import de.nordakademie.nakjava.gamelogic.stateMachineEvenNewer.states.State;
 import de.nordakademie.nakjava.server.internal.model.LeaveOutVisibleCheck;
 import de.nordakademie.nakjava.server.internal.model.LoginSpecificModel;
 import de.nordakademie.nakjava.server.internal.model.SimulationModels;
 import de.nordakademie.nakjava.server.internal.model.StateSpecificModel;
 import de.nordakademie.nakjava.server.internal.model.VisibleField;
 import de.nordakademie.nakjava.server.internal.model.VisibleField.TargetInState;
 import de.nordakademie.nakjava.server.persistence.Deck;
 import de.nordakademie.nakjava.server.persistence.DeckPersister;
 
 public class PlayerState implements Serializable {
 
 	@VisibleField(targets = {
 			@TargetInState(target = Target.SELF, states = { State.LOGIN,
 					State.CONFIGUREGAME, State.READYTOSTARTSTATE,
 					State.PLAYCARDSTATE, State.ADJUSTCARDHANDSTATE, State.STOP,
 					State.EDITDECK, State.ENDOFGAMESTATE,
 					State.OTHERPLAYERLEFTGAME, State.SIMULATIONSTATE }),
 			@TargetInState(target = Target.OPPONENT, states = { State.LOGIN,
 					State.EDITDECK, State.CONFIGUREGAME,
 					State.READYTOSTARTSTATE, State.PLAYCARDSTATE,
 					State.ADJUSTCARDHANDSTATE, State.STOP,
 					State.ENDOFGAMESTATE, State.SIMULATIONSTATE }) })
 	private State state;
 	@VisibleField(targets = { @TargetInState(states = { State.CONFIGUREGAME,
 			State.READYTOSTARTSTATE, State.EDITDECK, State.PLAYCARDSTATE,
 			State.ADJUSTCARDHANDSTATE, State.STOP, State.SIMULATIONSTATE }, target = Target.OPPONENT) })
 	private String name = "";
 	@LeaveOutVisibleCheck
 	private List<Deck> savedDecks;
 	private StateSpecificModel stateSpecificModel;
 	private SimulationModels simulationModels;
 
 	public PlayerState() {
 
 		state = State.LOGIN;
 		stateSpecificModel = new LoginSpecificModel();
 		savedDecks = new ArrayList<>();
 	}
 
 	public State getState() {
 		return state;
 	}
 
 	public void setState(State state) {
 		this.state = state;
 	}
 
 	public StateSpecificModel getStateSpecificModel() {
 		return stateSpecificModel;
 	}
 
 	public void setStateSpecificModel(StateSpecificModel stateSpecificModel) {
 		this.stateSpecificModel = stateSpecificModel;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<Deck> getSavedDecks() {
 		return savedDecks;
 	}
 
 	public void setSavedDecks(List<Deck> savedDecks) {
 		this.savedDecks = savedDecks;
 	}
 
 	public SimulationModels getSimulationModels() {
 		return simulationModels;
 	}
 
 	public void setSimulationModels(SimulationModels simulationModels) {
 		this.simulationModels = simulationModels;
 	}
 
 	public Deck addDeck(String name, Set<String> cards) {
 		Deck deck = new Deck(name, cards);
 		Deck deckWithToBeAddedName = getDeckWithName(name);
 		if (deckWithToBeAddedName != null) {
 			savedDecks.remove(deckWithToBeAddedName);
			savedDecks.add(deck);
 		}
 		DeckPersister.saveDecks(this);
 		return deck;
 	}
 
 	public void removeDeck(Deck deck) {
 		savedDecks.remove(deck);
 		DeckPersister.saveDecks(this);
 	}
 
 	public Deck getDeckWithName(String name) {
 		for (Deck deck : savedDecks) {
 			if (deck.getName().equals(name)) {
 				return deck;
 			}
 		}
 		return null;
 	}
 }
