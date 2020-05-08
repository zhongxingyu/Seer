 package de.htwg.monopoly.controller.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import de.htwg.monopoly.controller.IController;
 import de.htwg.monopoly.entities.Bank;
 import de.htwg.monopoly.entities.Dice;
 import de.htwg.monopoly.entities.IFieldObject;
 import de.htwg.monopoly.entities.Player;
 import de.htwg.monopoly.entities.Playfield;
 import de.htwg.monopoly.entities.Street;
 import de.htwg.monopoly.observer.impl.Observable;
 import de.htwg.monopoly.util.IMonopolyUtil;
 
 public class Controller extends Observable implements IController {
 	private PlayerController players;
 	private Playfield field;
 	private Player currentPlayer;
 	private IFieldObject currentField;
 
 	private StringBuilder message;
 	private int lastChooseOption;
 
 	/* internationalization */
 	private ResourceBundle bundle = ResourceBundle.getBundle("Messages",
 			Locale.GERMAN);
 
 	public Controller() {
 		this.players = new PlayerController();
 		this.field = new Playfield();
 		this.message = new StringBuilder();
 	}
 
 	@Override
 	public boolean setNumberofPlayer() {
 		return players.readNumberOfPlayer();
 	}
 
 	@Override
 	public boolean setNameofPlayer(int i) {
 		return players.readNameOfPlayer(i);
 	}
 
 	@Override
 	public void initGame(int fieldSize) {
 		this.field.initialize(fieldSize);
 	}
 
 	@Override
 	public void startNewGame() {
 		// TODO ZufallsSpieler auswhlen
 		this.currentPlayer = players.getNextPlayer();
 		notifyObservers(0);
 	}
 
 	@Override
 	public void startTurn() {
 		// this currentPlayer players.currentPlayer
 		if (currentPlayer.isInPrison()) {
 			currentPlayer.incrementPrisonRound();
 			message.append(bundle.getString("contr_bsys") + "\n");
 		} else {
 			turn();
 		}
 		// berprfen auf was frn feldobjek
 		// dementsprechend notify
 		notifyObservers(1);
 		// notifyObservers
 	}
 
 	private void turn() {
 		rollDice();
 		/* move player -> max number to dice is fieldSize */
 		field.movePlayer(currentPlayer,
 				(Dice.getResultDice() % field.getfieldSize() + 1));
 		/* set current field */
 		this.currentField = field.getCurrentField(currentPlayer);
 		/* add information on whith field is current user */
 		message.append(field.appendInfo(currentField, currentPlayer));
 
 	}
 
 	@Override
 	public void rollDice() {
 		/* throw dice */
 		Dice.throwDice();
 	}
 
 	@Override
 	public void endTurn() {
 		this.message.delete(0, this.message.length());
 		this.currentPlayer = players.getNextPlayer();
 	}
 
 	@Override
 	public void exitGame() {
 		// TODO
 	}
 
 	@Override
 	public boolean buyStreet() {
 		/* get current street */
 		Street currentStreet = (Street) field.getCurrentField(currentPlayer);
 		/* check if enough money */
 		if (currentStreet.getPriceForStreet() < currentPlayer.getBudget()) {
 			/* decrement money of current player */
 			currentPlayer.setBudget(currentPlayer.getBudget()
 					- currentStreet.getPriceForStreet());
 			currentStreet.setOwner(currentPlayer);
 			/* add street to ownership of current player */
 			currentPlayer.addOwnership(currentStreet);
 			return true;
 		}
 		/* TODO: not enough money!! -> end of game? */
 		return false;
 
 	}
 
 	@Override
 	public void checkFieldType() {
 		field.getCurrentField(currentPlayer);
 		// pseudo
 		/*
 		 * switsch fieldobject case street case ereignis case gemeinschaft case
 		 * los case frei parken case gehe ins gefngnis case zu besuch im
 		 * gefngnis case bahnhof case elektrowerk case wasserwerk case
 		 * steuerfeld notifyObserver
 		 */
 	}
 
 	@Override
 	public void payRent() {
 		Bank.payRent(currentPlayer, field.getCurrentField(currentPlayer));
 		notifyObservers();
 
 	}
 
 	@Override
 	public void receiveGoMoney() {
 		Bank.receiveMoney(currentPlayer, IMonopolyUtil.LOS_MONEY);
 		notifyObservers();
 	}
 
 	public PlayerController getPlayers() {
 		return players;
 	}
 
 	public Playfield getField() {
 		return field;
 	}
 
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 
 	/* for junit test */
 	public void setCurrentField(IFieldObject currentField) {
 		this.currentField = currentField;
 	}
 
 	@Override
 	public List<String> getOptions(int chooseOption) {
 
 		/* TODO INfos selber suchen und zusammenbauen */
 
 		List<String> options = new ArrayList<String>();
 
 		switch (chooseOption) {
 		case 1:
 			/* add options if user in prison */
 			options.addAll(getOptionPrison());
 			/* add option to dice */
 			options.add("(d) " + bundle.getString("dice"));
 			break;
 		case 2:
 			/* add options if user on a street object */
			options.add(getOptionOnStreet());
 			// NO BREAK
 		case IMonopolyUtil.OPTION_FINISH:
 			options.add("(b) " + bundle.getString("contr_finish"));
 		default:
 			break;
 		}
 		/*
 		 * set information witch option is choose, to check correct user input
 		 * in function isCorrectOption
 		 */
 		this.lastChooseOption = chooseOption;
 		/* add option to quit the game (without loosing) */
 		options.add("(x) " + bundle.getString("contr_quit"));
 		/* return a list of options (strings) */
 		return options;
 	}
 
	private String getOptionOnStreet() {
 		/* if current field a steet */
 		if (currentField.getType() == 's') {
 			Street s = (Street) currentField;
 			/* check if street have a owner */
 			if (s.getOwner() == null) {
 				/* if not -> add option to buy street */
				return ("(y) " + bundle.getString("contr_buy"));
 			}
 		}
		return null;
 	}
 
 	private List<String> getOptionPrison() {
 		List<String> options = new ArrayList<String>();
 		/* check if current player in prison */
 		if (currentPlayer.isInPrison()) {
 			/* add option to leave prison */
 			options.add("(f) " + bundle.getString("contr_free") + " ("
 					+ IMonopolyUtil.FREIKAUFEN + ")");
 			options.add("(3) " + bundle.getString("contr_threeDice"));
 			// TODO check if contains free park card
 		}
 		/* returns a list with options */
 		return options;
 
 	}
 
 	/**
 	 * function to check if input of user correct
 	 */
 	public boolean isCorrectOption(String chooseOption) {
 		/* get the last options for current user */
 		List<String> options = new ArrayList<String>();
 		options = getOptions(this.lastChooseOption);
 		/* create string for search */
 		String strChooseOptions = "(" + chooseOption + ")";
 		/* check if choosen option containing in option list for user */
 		for (String tmp : options) {
 			if (tmp.contains(strChooseOptions)) {
 				/* correct option */
 				return true;
 			}
 		}
 		/* not correct options */
 		return false;
 	}
 
 	/**
 	 * return a String with information about the current turn
 	 */
 	public String getMessage() {
 		return this.message.toString();
 	}
 
 }
