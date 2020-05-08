 package server;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import server.exceptions.InvalidTerritoryStateException;
 import server.exceptions.NotEnoughPlayersException;
 import server.remoteexceptions.ServerFullException;
 import valueobjects.BonusCard;
 import valueobjects.BonusCardStack;
 import valueobjects.Player;
 import valueobjects.PlayerCollection;
 import valueobjects.Territory;
 
 import commons.Action;
 import commons.ClientMethods;
 import commons.GameMethods;
 import commons.actions.AttackAction;
 import commons.actions.DefendAction;
 import commons.actions.GameStartedAction;
 import commons.actions.NextPlayerAction;
 import commons.actions.PhaseAction;
 import commons.actions.PlayerJoinedAction;
 import commons.actions.TerritoryUnitsChangedAction;
 
 import cui.IO;
 import de.root1.simon.Registry;
 import de.root1.simon.Simon;
 import de.root1.simon.annotation.SimonRemote;
 import de.root1.simon.exceptions.NameBindingException;
 import de.root1.simon.exceptions.SimonRemoteException;
 
 /**
  * The game class manages a complete game of Risk
  * 
  * @author Jannes, Hendrik
  * 
  */
 @SimonRemote
 public class GameMethodsImpl implements GameMethods, Serializable {
 
 	private static final long serialVersionUID = -3491803188267650698L;
 
 	// die gewürfelten Ergebnisse müssen auf dem Server lokal gespeichert werden
 	ArrayList<Integer> attackDice;
 	ArrayList<Integer> defendDice;
 
 	// das angreifende land und angegeriffende Land muss temporär gespeichert werden
 	Territory attackingTerritory;
 	Territory defendTerritory;
 
 	private boolean started = false; 
 	private PlayerCollection players = new PlayerCollection();
 	private List<ClientMethods> clients = new ArrayList<ClientMethods>();
 
 	private TerritoryManager territoryManager = new TerritoryManager();
 	private BonusCardStack bonusCardManager = new BonusCardStack();
 	private BonusTracker bonusTracker = new BonusTracker();
 
 	/**
 	 * The current player
 	 */
 	private Player currentPlayer;
 
 	/**
 	 * The current phase of a player's turn
 	 */
 	private Phase currentPhase = Phase.START;
 
 	/**
 	 * Phases of a player's turn
 	 */
 	public static enum Phase {
 		START, TURNINCARDS, PLACEMENT, ATTACK1, ATTACK2, ATTACK3, MOVEMENT
 	};
 
 	public GameMethodsImpl(String name, int port) throws UnknownHostException,
 	IOException, NameBindingException {
 		Registry registry = Simon.createRegistry(port);
 		registry.bind(name, this);
 	}
 
 	// START OF observable
 
 
 	//	private void setChanged() {
 	//		changed = true;
 	//	}
 	//	
 	//	private void clearChanged() {
 	//		changed = false;
 	//	}
 
 	private void notifyPlayers(Action arg) {
 		//		if (!changed) {
 		//			return;
 		//		}
 		// Notify all observers
 		for (ClientMethods client:clients) {
 			client.update(this, arg);
 		}
 		//		clearChanged();
 	}
 
 	// END OF observable
 
 	/**
 	 * Adds a player to the game and consequently to the list of observers
 	 * @return 
 	 */
 	public Player addPlayer(String name, ClientMethods client) throws ServerFullException {
 		if (started) {
 			// Game is already in progress
 			throw new ServerFullException();
 		} else if (players.size() >= 6) {
 			// Too many players
 			throw new ServerFullException();
 		}
 
 		// Add the client
 		if (client == null) {
 			throw new NullPointerException();
 		}
 		clients.add(client);
 
 		Player player = new Player(name);
 
 		players.add(player);
 
 		// Output a success message
 		IO.write("Client connected.");
 
 		notifyPlayers(new PlayerJoinedAction(player));
 
 		return player;
 	}
 
 	/**
 	 * Deletes a player from the list of observers
 	 */
 	public void deletePlayer(ClientMethods client) {
 		clients.remove(client);
 	}
 
 	/**
 	 * Starts this instance of Risk
 	 */
 	public void start() throws NotEnoughPlayersException {
 		int playerCount = players.size();
 
 		// Check if there are enough players
 		if (playerCount < 3) {
 			// TODO: implement logic for 2 players
 			throw new NotEnoughPlayersException(playerCount);
 		}
 
 		// Start units for every player
 		int startUnits;
 		// Get the total amount of start units per player
 		if (playerCount > 3) {
 			startUnits = 30;
 		} else if (playerCount == 3) {
 			startUnits = 35;
 		} else {
 			// TODO implement logic for 2 player games
 			startUnits = 36;
 		}
 
 		// Set the start units for each player
 		for (Player player : players) {
 			player.addSupplies(startUnits);
 		}
 
 		placeStartUnitsRandomly();
 
 		// Set the game status to started
 		started = true;
 		notifyPlayers(new GameStartedAction());
 
 		// switch the pahse to turning
 		//		currentPhase = Phase.TURNINCARDS;
 
 		// Set the first phase
 		nextPhase();
 	}
 
 	/**
 	 * @return all Players
 	 */
 	public PlayerCollection getPlayers() {
 		return players;
 	}
 
 	public TerritoryManager getTerritoryManager() {
 		return territoryManager;
 	}
 
 	/**
 	 * Returns whether the game is over
 	 * 
 	 * @return True, if somebody has won the game
 	 */
 	public boolean isOver() {
 		// TODO Distinguish between world domination/missions
 		return players.size() == 1;
 	}
 
 	/**
 	 * Returns the winner of the game, if there is one.<br>
 	 * If the game isn't finished yet, <code>null</code> will be returned.
 	 * 
 	 * @return Winner of the game
 	 */
 	public Player getWinner() {
 		// Return the last man standing
 		Player winner = null;
 		if (isOver()) {
 			winner = players.getNextPlayer();
 		}
 		return winner;
 	}
 
 	/**
 	 * TODO doc
 	 * 
 	 * @return Player
 	 */
 	public Player getActivePlayer() {
 		return currentPlayer;
 	}
 
 	/**
 	 * TODO doc
 	 */
 	private void nextPlayer() {
 		// Advance to the next player
 		currentPlayer = players.getNextPlayer();
 		IO.write("Next player: "+ currentPlayer.getName() + " (" + currentPlayer.getSupplies() + ")");
 		notifyPlayers(new NextPlayerAction(currentPlayer));
 	}
 
 	/**
 	 * Returns the current phase.
 	 * @return
 	 */
 	public Phase getPhase() {
 		return currentPhase;
 	}
 
 	/**
 	 * Prepares and returns the next action in the sequence. This method can
 	 * change the active player, so always use this method before getting the
 	 * active player.<br>
 	 * <br>
 	 * The sequence of actions in a turn:
 	 * <ol>
 	 * <li>TURNINCARDS</li>
 	 * <li>PLACEMENT</li>
 	 * <li>ATTACK</li>
 	 * <li>MOVEMENT</li>
 	 * </ol>
 	 * 
 	 * @return Action The next action/phase
 	 */
 	public void nextPhase() {
 		Phase cp = getPhase();
 		notifyPlayers(new PhaseAction(currentPlayer, cp));
 		// Which action comes afterwards the current one?
 		switch (currentPhase) {
 			// The first action is at the end of this switch block
 			case TURNINCARDS:
 				// Placing the supply units is next
 				// überprüfen der karten und supply hnzufügen
 				// currentphase = PLACEMENT
 				preparePlacementAction();
 				break;
 
 			case PLACEMENT:
 				// Attacking other players is next
 				prepareAttack1Action();
 				break;
 			case ATTACK1:
 				// here: choose the Teritory(Button)
 				prepareAttack2Action();
 				break;
 			case ATTACK2:
 				// here: choose territory to attaxk (button)
 				prepareAttack3Action();
 				break;
 			case ATTACK3:
 				// here: the Amount of attacking units and calculate the fight
 				//TODO REMOVE THIS
 //				prepareMovementAction();
 				break;
 			case MOVEMENT:
 				// TODO Only if the player conquered at least one territory
 				currentPlayer.addBonusCard(bonusCardManager.retrieveCard());
 				// End of a player's turn. Start a new one.
 			default:
 				// Start
 				nextPlayer();
 				// Turning in cards is next
 				prepareTurnInAction();
 				break;
 		}
 	}
 
 	/**
 	 * Calculates supply for the current player. This is only called once
 	 * in every turn.<br>
 	 * <br>
 	 * This doesn't include unit supplies for cards that the player may turn in
 	 * later.
 	 */
 	private void calculateSupplies() {
 		// Base unit amount for occupied territories
 		int supplies = currentPlayer.getTerritoryCount() / 3;
 		// At least 3
 		if (supplies < 3) {
 			supplies = 3;
 		}
 
 		// TODO Extra supplies for conquered continents
 		//		for (Continent continent : activePlayer.getContinents()) {
 		//			supplies += continent.getBonusSupplies();
 		//		}
 
 		// Add the supplies
 		currentPlayer.addSupplies(supplies);
 	}
 
 	/**
 	 * TODO doc
 	 */
 	private void prepareTurnInAction() {
 		// Can the player turn in cards?
 		if (currentPlayer.canTurnInCards()) {
 			currentPhase = Phase.TURNINCARDS;
 		} else {
 			// If the player can't turn in cards, skip to the next step
 			preparePlacementAction();
 			currentPhase = Phase.PLACEMENT;
 		}
 	}
 
 	/**
 	 * TODO doc
 	 */
 	private void preparePlacementAction() {
 		calculateSupplies();
 		currentPhase = Phase.PLACEMENT;
 	}
 
 	/**
 	 * TODO doc
 	 */
 	private void prepareAttack1Action() {
 		/*
 		 * Figure out which territories of the current player could be used for
 		 * an attack
 		 * Must be owned by the player
 		 * Must have at least 2 units
 		 */
 		currentPhase = Phase.ATTACK1;
 	}
 	/**
 	 * TODO doc
 	 */
 	private void prepareAttack2Action() {
 		/*
 		 * Figure out which territories of the current player could be used for
 		 * an attack
 		 * Must be owned by the player
 		 * Must have at least 2 units
 		 */
 		currentPhase = Phase.ATTACK2;
 	}
 	/**
 	 * TODO doc
 	 */
 	private void prepareAttack3Action() {
 		/*
 		 * Figure out which territories of the current player could be used for
 		 * an attack
 		 * Must be owned by the player
 		 * Must have at least 2 units
 		 */
 		currentPhase = Phase.ATTACK3;
 	}
 
 	/**
 	 * TODO doc
 	 */
 	private void prepareMovementAction() {
 		/*
 		 * Figure out which territories have units which are eligible to be
 		 * moved
 		 * The territory's units must not have participated in a battle
 		 * The territory needs at least 2 units
 		 */
 		currentPhase = Phase.MOVEMENT;
 	}
 
 	/**
 	 * TODO doc
 	 */
 	public void placeStartUnitsRandomly() {
 		Player currentPlayer;
 		for (Territory territory : territoryManager.getRandomTerritoryList()) {
 			// Cycle through all players
 			currentPlayer = players.getNextPlayer();
 
 			// Place one unit on the territory
 			try {
 				territoryManager.changeTerritoryOwner(currentPlayer, territory,
 						1);
 			} catch (InvalidTerritoryStateException e) {
 				e.printStackTrace();
 			}
 
 			// Remove the placed units from the player's supply
 			currentPlayer.subtractSupplies(1);
 		}
 
 		// Place the remaining units randomly
 		while (!players.supplyAllocated()) {
 			// Cycle through all players
 			currentPlayer = players.getNextPlayer();
 
 			// Add one unit to a random territory
 			currentPlayer.getRandomTerritory().addUnits(1);
 			// Remove it from the player's supply
 			currentPlayer.subtractSupplies(1);
 		}
 
 		// Reset the current player to player 1
 		players.resetActivePlayer();
 	}
 
 	/**
 	 * TODO doc
 	 */
 	public void redeemBonusCards(List<BonusCard> cards) {
 		// TODO make this a real exception
 		assert currentPlayer.getBonusCards().containsAll(cards);
 		assert cards.size() == 3;
 		// TODO Check if the card triple is valid
 		// falls Bonuskarten drei stück verfügbar
 		// wenn genau drei karten verfügbar und alle drei gleich
 		if(currentPlayer.getBonusCards().size() == 3 && 
 				currentPlayer.getBonusCards().containsAll(cards)) {
 			// Redeem the cards
 			currentPlayer.removeBonusCards(cards);
 			currentPlayer.addSupplies(bonusTracker.getNextBonus());
 		}else if(currentPlayer.getBonusCards().size() >= 3) {
 
 		}
 	}
 
 	@Override
 	public void save() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void load() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Mission getMyMission(Player player) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<BonusCard> getMyBonusCards(Player player) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public HashMap<String, Territory> getTerritories() {
 		return territoryManager.getTerritoryMap();
 	}
 
 	@Override
 	public List<Territory> getMyTerritories(Player player) {
 		// TODO Auto-generated method stub
 		return player.getTerritories();
 	}
 
 	@Override
 	public List<Territory> getMyTerritoriesForAttacking(Player player) {
 		ArrayList<Territory> territories = player.getTerritories();
 		ArrayList<Territory> attackingTerritories = new ArrayList<Territory>();
 
 		for(int i = 1; i < territories.size(); i++) {
 			CopyOnWriteArrayList<Territory> neighbors = territories.get(i).getNeighbors();
 			for(int j = 1; j < neighbors.size() ;j++){
 				if(!neighbors.get(j).getOwner().equals(player) && territories.get(i).getUnits() > 1){
 					if(!attackingTerritories.contains(neighbors.get(j))) {
 						attackingTerritories.add(territories.get(i));
 					}
 				}
 			}
 		}
 		return attackingTerritories;
 	}
 
 	@Override
 	public List<Territory> getMyTerritoriesForMoving(Player player) {
 		ArrayList<Territory> territories = player.getTerritories();
 		ArrayList<Territory> moveTerritories = new ArrayList<Territory>();
 
 		for(int i = 1; i <= territories.size(); i++) {
 			CopyOnWriteArrayList<Territory> neighbors = territories.get(i).getNeighbors();
 			for(int j = 1; j <= neighbors.size() ;j++){
 				if(neighbors.get(j).getOwner().equals(player) && territories.get(i).getUnits() > 1){
 					if(!moveTerritories.contains(neighbors.get(j))) {
 						moveTerritories.add(territories.get(i));
 					}
 				}
 			}
 		}
 		return moveTerritories;
 	}
 
 	@Override
 	public CopyOnWriteArrayList<Territory> getOpposingNeighborsOf(Territory territory) {
 		CopyOnWriteArrayList<Territory> opposingNeighbors = territory.getNeighbors();
 		for(Territory territory2 : opposingNeighbors) {
 			if(territory2.getOwner().equals(territory.getOwner())){
 				opposingNeighbors.remove(territory2);
 			}
 		}
 		return opposingNeighbors;
 	}
 
 	@Override
 	public CopyOnWriteArrayList<Territory> getSimilarNeighborsOf(Territory territory) {
 		CopyOnWriteArrayList<Territory> similarNeighbors = territory.getNeighbors();
 
 		for(Territory territory2 : similarNeighbors) {
 			if(!territory2.getOwner().equals(territory.getOwner())){
 				similarNeighbors.remove(territory2);
 			}
 		}
 		return similarNeighbors;
 	}
 
 	@Override
 	public void placeUnits(String territory, int amount) {
 		Territory t = territoryManager.getTerritoryMap().get(territory);
 
 		Player owner = t.getOwner();
 
 		if(owner.getSupplies() > 0){
 			t.addUnits(amount);
 			owner.subtractSupplies(amount);
 
 			// Send a notification to all clients
 			notifyPlayers(new TerritoryUnitsChangedAction(t, t.getUnits()));
 		}
 
 		if(owner.getSupplies() == 0){
 			nextPhase();
 		}
 	}
 
 	@Override
 	public void attack(Territory attackingTerritory,
 			Territory attackedTerritory, int amount) {
 		// Angreifer(amount) das nicht mehr als 3 und nicht weniger als 1 sein
 		
 		attackDice = getDice(amount);
 		this.attackingTerritory = attackingTerritory;
 		this.defendTerritory = attackedTerritory;
 		notifyPlayers(new AttackAction(attackingTerritory, attackedTerritory, amount));
 	}
 
 	public void defend(Territory defendTerritory, int amount) {
 		// Verteidiger(amount) darf nicht mehr als 2 und nicht weniger als 1 sein
 		defendDice = getDice(amount);
 		//notifyPlayers(new DefendAction(defendTerritory, amount));
 		// nun wird der Kampf bzw. die zwei würfel verglichen "Kampf" findet statt
 		calculateDice(attackDice, defendDice);
 	}
 	
 	// diese Methode ist Pseudo mäßig programmiert
 	public void calculateDice(ArrayList<Integer> attackDice, ArrayList<Integer> defendDice) {
 		// TODO vergleichen und auswerten der Würfel
 		// TODO irgendein lustiger algorithmus zum Vergleich der da unten steht ;-)
 		int defendLoseUnits = 0;
 		int attackLoseUnits = 0;
 
 		for(int i = 0; i <= defendDice.size()-1; i++) {
 			if(defendDice.get(i) > attackDice.get(i)) {
 				System.out.println("Defensive: "+ defendDice.get(i) +" schlägt Offensive: "+ attackDice.get(i));
 				attackLoseUnits++;
 			}else if(defendDice.get(i) == attackDice.get(i)) {
 				System.out.println("Defensive: "+ defendDice.get(i) +" schlägt Offensive: "+ attackDice.get(i) +"GLEICHEIT");
 				attackLoseUnits++;
 			}else if(defendDice.get(i) < attackDice.get(i)) {
 				System.out.println("Offensive: "+ attackDice.get(i) +" schlägt Defensive: "+ defendDice.get(i));
 				defendLoseUnits++;
 			}
 		}
 		// TODO und danach setzen der Values auf den jeweiligen Territories
 		attackingTerritory.setUnits(attackingTerritory.getUnits() - attackLoseUnits);
 		defendTerritory.setUnits(defendTerritory.getUnits() - defendLoseUnits);
 		// TODO Es muss noch überprüft werden ob das Land übernommen wurde oder nicht
 
 		// TODO ein notifiy rauswerfen mit den geänderten values
 		// TODO koennte so aussehen: notifyPlayers(territory, dasjeweiligeterritory.getUnits()
 		notifyPlayers(new TerritoryUnitsChangedAction(attackingTerritory, attackingTerritory.getUnits()));
 		notifyPlayers(new TerritoryUnitsChangedAction(defendTerritory, defendTerritory.getUnits()));
 	}
 
 	public ArrayList<Integer> getDice(int amount) {
 		ArrayList<Integer> dice = new ArrayList<Integer>();
 
 		for(int i = 0; i <= amount; i++) {
 			dice.add(i, (int) ((Math.random()) * 6 + 1));
 		}
 
 		//sortieren der würfel TODO Absteigend oder Aufsteigend ? Inhalt muss man noch auslesen
 		Collections.sort(dice);
 		// Nach dem return muss der attaker die Würfel mit dem des verteidiger vergelichen
 		return dice;
 	}
 
 	@Override
 	public void move(Territory source, Territory target, int amount)
 	throws SimonRemoteException {
 		source.setUnits(source.getUnits() - amount);
 		target.setUnits(target.getUnits() + amount);
 		// Es müssen noch die Clients Notified werden
 		notifyPlayers(new TerritoryUnitsChangedAction(source, source.getUnits()));
 		notifyPlayers(new TerritoryUnitsChangedAction(target, target.getUnits()));
 	}
 
 	// setzt die Pahse zurück auf Attack wegen wieder angriff für die GUI
 	public void resetAttack() {
 		currentPhase = Phase.ATTACK1;
		notifyPlayers(new PhaseAction(getActivePlayer(),Phase.ATTACK1));
 	}
 }
