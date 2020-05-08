 /*  Copyright (C) 2010 - 2011  Fabian Neundorf, Philip Caroli,
  *  Maximilian Madlung,	Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.client.ai.valuation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.ojim.client.SimpleClient;
 import org.ojim.client.ai.commands.AcceptCommand;
 import org.ojim.client.ai.commands.AuctionBidCommand;
 import org.ojim.client.ai.commands.BuildHouseCommand;
 import org.ojim.client.ai.commands.Command;
 import org.ojim.client.ai.commands.DeclineCommand;
 import org.ojim.client.ai.commands.EndTurnCommand;
 import org.ojim.client.ai.commands.NullCommand;
 import org.ojim.client.ai.commands.OutOfPrisonCommand;
 import org.ojim.client.ai.commands.SellCommand;
 import org.ojim.client.ai.commands.ToggleMortgageCommand;
 import org.ojim.client.ai.commands.TradeCommand;
 import org.ojim.log.OJIMLogger;
 import org.ojim.logic.Logic;
 import org.ojim.logic.state.Auction;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.Jail;
 import org.ojim.logic.state.fields.Street;
 
 import edu.kit.iti.pse.iface.IServer;
 
 /**
  * Valuator - returns the best command
  * 
  * @author Jeremias Mechler
  * 
  */
 public class Valuator extends SimpleClient {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1145966856856523668L;
 	private double[] weights;
 	private ValuationFunction[] valuationFunctions;
 	private Logic logic;
 	private int playerID;
 	private IServer server;
 	private Logger logger;
 	private int auctionBid;
 	private int auctionSteps = 11;
	private int currentStep = 2;
 	private boolean endTurn = false;
 	private boolean auctionEndTurn = false;
 	private int[] trade;
 	private int count = 0;
 	private int[] toSell;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param logic
 	 *            reference to logic
 	 * @param server
 	 *            reference to server
 	 * @param playerID
 	 *            The player's ID
 	 */
 	public Valuator(Logic logic, IServer server, int playerID) {
 		super(logic, playerID, server);
 		assert (logic != null);
 		assert (server != null);
 		this.logic = logic;
 		this.server = server;
 		this.playerID = playerID;
 		weights = new double[ValuationFunction.COUNT];
 		for (int i = 0; i < weights.length; i++) {
 			weights[i] = 1;
 		}
 		weights[0] = 100000;
 		weights[2] = 50000;
 		this.logger = OJIMLogger.getLogger(this.getClass().toString());
 
 		valuationFunctions = new ValuationFunction[6];
 		valuationFunctions[0] = CapitalValuator.getInstance();
 		valuationFunctions[1] = PropertyValuator.getInstance();
 		valuationFunctions[2] = PrisonValuator.getInstance();
 		valuationFunctions[3] = MortgageValuator.getInstance();
 		valuationFunctions[4] = PropertyGroupValuator.getInstance();
 		valuationFunctions[5] = BuildingOnPropertyValuator.getInstance();
 
 		for (int i = 0; i < ValuationFunction.COUNT; i++) {
 			assert (valuationFunctions[i] != null);
 		}
 
 		ValuationParameters.init(logic);
 		trade = new int[40];
 	}
 
 	/**
 	 * Returns the best command
 	 * 
 	 * @param position
 	 *            current position
 	 * @return command
 	 */
 	public Command returnBestCommand(int position) {
 		if (position >= 40) {
 			throw new IllegalArgumentException("Invalid position: " + position);
 		}
 
 		PriorityQueue<Command> queue = new PriorityQueue<Command>();
 		Field field = getGameState().getFieldAt(Math.abs(position));
 		initFunctions();
 
 		// OJIMLogger.changeGlobalLevel(Level.WARNING);
 		// OJIMLogger.changeLogLevel(logger, Level.FINE);
 
 		// Feld potenziell kaufbar
 		if (!endTurn && !auctionEndTurn) {
 			// System.out.println("turn");
 
 			queue.add(buyFields(field));
 			// Feld potentiell bebaubar
 			queue.add(upgradeStreet());
 			queue.add(tradeStreet(logic.getGameState().getFieldAt(position)));
 			queue.add(getOutOfJail());
 			// reicht das?
 
 			if (queue.peek() instanceof NullCommand) {
 				endTurn = true;
 			}
 			return queue.peek();
 		}
 
 		if (endTurn) {
 			endTurn = false;
 			return new EndTurnCommand(logic, server, playerID);
 		}
 		if (auctionEndTurn) {
 			auctionEndTurn = false;
 			return new NullCommand(logic, server, playerID);
 		}
 		assert (false);
 		return new NullCommand(logic, server, playerID);
 	}
 
 	/**
 	 * Acting on a trade offer
 	 * 
 	 * @return Command
 	 */
 	public Command actOnTradeOffer() {
 		initFunctions();
 		// System.out.println("Trade!");
 		assert (this.getTradeState() == TradeState.WAITING_PROPOSED);
 		boolean restricted = false;
 		if (getRequiredEstates() != null) {
 			for (BuyableField field : getRequiredEstates()) {
 				if (field.isMortgaged()) {
 					// TODO necessary?
 					assert (false);
 					restricted = true;
 				}
 				if (field instanceof Street && ((Street) field).getBuiltLevel() > 0) {
 					// TODO necessary?
 					assert (false);
 					restricted = true;
 				}
 			}
 
 		}
 		if (!restricted) {
 			double value = getOfferedCash();
 			double minus = tradeValuateRequestedEstates();
 			value += tradeValuateJailCards();
 			value += tradeValuateOfferedEstates();
 			minus -= getRequiredCash();
 			minus += valuationFunctions[0].returnValuation(this.getRequiredCash());
 			// missing: out of jail cards!
 			if (value + minus > 0) {
 				assert (false);
 				return new AcceptCommand(logic, server, playerID);
 			}
 		}
 		return new DeclineCommand(logic, server, playerID);
 	}
 
 	/**
 	 * Pays back mortgages
 	 * 
 	 * @return command
 	 */
 	public Command paybackMortgages() {
 		initFunctions();
 		PriorityQueue<Command> queue = new PriorityQueue<Command>();
 		Command command = new NullCommand(logic, server, playerID);
 		command.setValuation(0);
 		queue.add(command);
 		for (BuyableField field : getMe().getFields()) {
 			if (field.isMortgaged()) {
 				field.setSelected(true);
 				double valuation = getResults(field.getPosition(), field.getMortgagePrice());
 				System.out.println(valuation);
 				field.setSelected(false);
 				if (valuation >= 0) {
 					command = new ToggleMortgageCommand(logic, server, playerID, field);
 					command.setValuation(valuation);
 					queue.add(command);
 				}
 			}
 		}
 		return queue.peek();
 	}
 
 	/**
 	 * Acts on auction
 	 * 
 	 * @return Command
 	 */
 	public Command actOnAuction() {
 		initFunctions();
 		Auction auction = this.getGameState().getAuction();
 		assert (auction.getState() != AuctionState.NOT_RUNNING);
 		int realValue = (int) getResults(auction.objective.getPosition(), 0);
 		int bidder;
 		if (auction.getHighestBidder() == null) {
 			bidder = -1;
 		} else {
 			bidder = auction.getHighestBidder().getId();
 		}
 		logger.log(Level.FINE, "state = " + auction.getState().value + " bidder = " + bidder + " highesBid = "
 				+ auction.getHighestBid() + " value = " + realValue);
 
 		if (auction.getState() != AuctionState.THIRD && auction.getHighestBidder() != getMe()
 				&& auction.getHighestBid() < realValue && currentStep < auctionSteps) {
 			logger.log(Level.FINE, "Highest bid = " + auction.getHighestBid());
 			if (auction.getHighestBid() < realValue) {
 				logger.log(Level.FINE, "Valuation " + realValue);
				double factor = Math.log(currentStep++) / Math.log(auctionSteps);
 				auctionBid = (int) (factor * realValue);
 				logger.log(Level.FINE, "Bidding " + auctionBid);
 
 				if (getResults(auction.objective.getPosition(), auctionBid) > 0) {
 					return new AuctionBidCommand(logic, server, playerID, auctionBid);
 				} else {
 					auctionEndTurn = true;
 					return new NullCommand(logic, server, playerID);
 				}
 			}
 		}
 		if (auction.getState() == AuctionState.THIRD) {
			currentStep = 2;
 		}
 		if (auction.getHighestBidder() != getMe()) {
 			auctionEndTurn = true;
 		}
 		return new NullCommand(logic, server, playerID);
 	}
 
 	private double getResults(int position, int amount) {
 		assert (position <= 40 && position >= 0);
 		assert (amount >= 0);
 		double result = weights[0] * valuationFunctions[0].returnValuation(amount);
 		for (int i = 1; i < valuationFunctions.length; i++) {
 			result += weights[i] * valuationFunctions[i].returnValuation(position);
 		}
 		logger.log(Level.INFO, "gesamt = " + result);
 		return result;
 	}
 
 	private double tradeValuateJailCards() {
 		int offeredCards = getNumberOfOfferedGetOutOfJailCards();
 		int difference = ValuationParameters.desiredNumberOfOutOfOjailCards
 				- this.getMe().getNumberOfGetOutOfJailCards();
 		if (difference > 0) {
 			if (offeredCards >= difference) {
 				return ((Jail) getGameState().getFieldAt(10)).getMoneyToPay() * difference;
 			} else {
 				return ((Jail) getGameState().getFieldAt(10)).getMoneyToPay() * offeredCards;
 			}
 		} else {
 			return 0;
 		}
 	}
 
 	private double tradeValuateEstates(BuyableField[] estates) {
 		assert (estates != null);
 		double result = 0;
 		for (BuyableField estate : estates) {
 			result += getResults(estate.getPosition(), 0);
 		}
 		return result;
 	}
 
 	private double tradeValuateRequestedEstates() {
 		return (-1) * tradeValuateEstates(getRequiredEstates());
 
 	}
 
 	private double tradeValuateOfferedEstates() {
 		return tradeValuateEstates(getOfferedEstate());
 	}
 
 	private void getPropertiesToSell(int requiredCash, boolean mortgage) {
 		int cash = 0;
 		ArrayList<BuyableField> list = new ArrayList<BuyableField>();
 		PriorityQueue<BuyableField> queue = getMe().getQueue();
 		// TODO better condition?
 		while (cash < requiredCash && !queue.isEmpty()) {
 			BuyableField temp = queue.poll();
 			cash += temp.getPrice() / 2;
 			list.add(temp);
 			revaluate(queue);
 		}
 		int[] result = new int[list.size()];
 		int i = 0;
 		for (BuyableField field : list) {
 			result[i++] = field.getPosition();
 		}
 		toSell = result;
 	}
 
 	// beim Hinzufügen alle neu validieren?
 
 	private void revaluate(PriorityQueue<BuyableField> queue) {
 		BuyableField[] fields = new BuyableField[queue.size()];
 		int i = 0;
 		while (!queue.isEmpty()) {
 			BuyableField field = queue.poll();
 			field.setValuation(getResults(field.getPosition(), 0));
 			fields[i++] = field;
 		}
 		for (BuyableField field : fields) {
 			queue.add(field);
 		}
 		assert (queue.size() == fields.length);
 	}
 
 	private void sell(int requiredCash) {
 		int initialCash = getLogic().getGameState().getActivePlayer().getBalance();
 		int newCash = 0;
 		// Property[] toBeSold = new Property[list.size()];
 		// toBeSold = list.toArray(new Property[0]);
 		// Iterator-Zugriffsfehler?
 		for (BuyableField field : this.getMe().getQueue()) {
 			if (newCash < requiredCash) {
 				int faceValue = field.getPrice();
 				new SellCommand(logic, server, playerID, field.getPosition(), (int) Math.max(faceValue,
 						field.getValuation()), faceValue / 2).execute();
 				newCash = logic.getGameState().getActivePlayer().getBalance() - initialCash;
 				assert (newCash != initialCash);
 				// property = null; // field.setSelected(true);
 			} else {
 				field.setSelected(false);
 			}
 		}
 		revaluate(getMe().getQueue());
 	}
 
 	private int getPrice(int position) {
 		assert (position < 40 && position >= 0);
 		Field field = logic.getGameState().getFieldAt(position);
 		assert (field instanceof BuyableField);
 		return ((BuyableField) field).getPrice();
 	}
 
 	private Command tradeStreet(Field blaField) {
 		count++;
 		Command result = new NullCommand(logic, server, playerID);
 		result.setValuation(0);
 		assert (blaField != null);
 		if (trade[blaField.getPosition()] != count - 1 && blaField instanceof BuyableField
				&& ((BuyableField) blaField).getOwner() != null && ((BuyableField) blaField).getOwner() != getMe()) {
 			trade[blaField.getPosition()] = count;
 			PriorityQueue<BuyableField> queue = new PriorityQueue<BuyableField>();
 			double streetValue = getResults(blaField.getPosition(), 0);
 			// get possible trade street
 			Field[] myFields = getMe().getFields();
 			Field[] hisFields = ((BuyableField) blaField).getOwner().getFields();
 			HashSet<Integer> colorList = new HashSet<Integer>();
 			for (Field field : hisFields) {
 				if (field instanceof BuyableField) {
 					int color = (((BuyableField) field).getFieldGroup().getColor());
 					if (color != blaField.getFieldGroup().getColor()) {
 						colorList.add(color);
 					}
 				}
 			}
 
 			for (Field field : myFields) {
 				if (field instanceof BuyableField) {
 					int color = (((BuyableField) field).getFieldGroup().getColor());
 					if (colorList.contains(color)) {
 						queue.add((BuyableField) field);
 					}
 				}
 			}
 			revaluate(queue);
 			if (!queue.isEmpty()) {
 				// System.out.println("Val = " + queue.peek().getValuation());
 				result = new TradeCommand(logic, playerID, server, queue.peek(), (BuyableField) blaField);
 				result.setValuation(1000 * streetValue);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Acts on negative money
 	 * 
 	 * @return Command
 	 */
 	public Command negative() {
 		if (getMe().getBalance() > 0) {
 			throw new IllegalStateException("Positive cash!");
 		}
 		logger.log(Level.FINE, "Negative cash!");
 		int balance = Math.abs(getMe().getBalance());
 		assert (balance > 0);
 		BuyableField[] result = mortgageBuildings(balance);
 		boolean success = false;
 		if (result.length > 0) {
 			success = true;
 			return new ToggleMortgageCommand(logic, server, playerID, result[0]);
 		} else {
 			LinkedList<BuyableField> list = new LinkedList<BuyableField>();
 			result = mortgageBuildings(0);
 			if (result.length != 0) {
 				int sum = 0;
 				for (int i = 0; i < result.length; i++) {
 					if (sum < balance) {
 						list.add(result[i]);
 						sum += result[i].getMortgagePrice();
 					}
 				}
 				if (sum >= balance) {
 					success = true;
 					return new ToggleMortgageCommand(logic, server, playerID, list.toArray(new BuyableField[0]));
 				}
 			}
 		}
 		return new NullCommand(logic, server, playerID);
 	}
 
 	private BuyableField[] mortgageBuildings(int money) {
 		LinkedList<BuyableField> list = new LinkedList<BuyableField>();
 
 		for (BuyableField field : getMe().getFields()) {
 			if (getGameRules().isFieldMortgageable(getMe(), field) && field.getMortgagePrice() >= Math.abs(money)) {
 				list.add(field);
 			}
 		}
 
 		// assert(getMe().getFields().length > 0);
 
 		/**
 		 * Compares the price of two fields
 		 */
 		final class MoneyComparator implements Comparator<BuyableField> {
 			@Override
 			public int compare(BuyableField o1, BuyableField o2) {
 				return o1.getPrice() - o2.getPrice();
 			}
 		}
 
 		BuyableField[] array = list.toArray(new BuyableField[0]);
 		Arrays.sort(array, new MoneyComparator());
 		return array;
 	}
 
 	private Command buyFields(Field field) {
 		assert (field != null);
 		if (field instanceof BuyableField && !endTurn) {
 			logger.log(Level.FINE, "BuyableField!");
 			Player owner = ((BuyableField) field).getOwner();
 			double realValue = getResults(field.getPosition(), 0);
 			// Feld gehört mir (noch) nicht
 			if (owner != getMe()) {
 				int price = ((BuyableField) field).getPrice();
 				double valuation = getResults(field.getPosition(), price);
 				// double realValue = getResults(position, 0);
 				// Feld gehört der Bank
 				if (owner == null) {
 					if (valuation > 0) {
 						logger.log(Level.FINE, "Granted");
 						assert (logic != null);
 						assert (server != null);
 						logger.log(Level.FINE, "Accept");
 						return new AcceptCommand(logic, server, playerID);
 						// nicht ausreichend, ab wann verkaufen oder handeln?
 					} else if (realValue > 0 && false) {
 						// preis?
 						// getPropertiesToSell();
 						// sell();
 
 						// return new DeclineCommand(logic, server, playerID);
 					} else {
 						// Ablehnen
 						logger.log(Level.FINE, "Decline");
 						endTurn = true;
 						return new DeclineCommand(logic, server, playerID);
 					}
 				} else {
 					// Trade!
 					logger.log(Level.FINE, "Soon trade");
 					// return new NullCommand(logic, server, playerID);
 				}
 			}
 
 		}
 		Command result = new NullCommand(logic, server, playerID);
 		result.setValuation(0);
 		return result;
 	}
 
 	private Command upgradeStreet() {
 		PriorityQueue<Street> upgradeableStreets = new PriorityQueue<Street>();
 		for (int i = 0; i < 40; i++) {
 			Field field = getGameState().getFieldAt(i);
 			if (field instanceof Street) {
 				Street street = (Street) field;
 				// assert(getGameRules().isFieldUpgradable(getMe(), field, street.getBuiltLevel() + 1));
 
 				if (getGameRules().isFieldUpgradable(getMe(), field, street.getBuiltLevel() + 1)) {
 					street.setSelected(true);
 					street.setValuation(getResults(street.getPosition(),
 							server.getEstateHousePrice(street.getPosition())));
 					street.setSelected(false);
 					upgradeableStreets.add((Street) field);
 				}
 			}
 		}
 		if (upgradeableStreets.size() > 0 && upgradeableStreets.peek().getValuation() > 0) {
 			Command result = new BuildHouseCommand(logic, server, playerID, upgradeableStreets.peek());
 			result.setValuation(upgradeableStreets.peek().getValuation());
 			return result;
 		} else {
 			Command result = new NullCommand(logic, server, playerID);
 			result.setValuation(0);
 			return result;
 		}
 	}
 
 	private Command getOutOfJail() {
 		assert (getMe() == logic.getGameState().getActivePlayer());
 		Jail jail = getMe().getJail();
 		if (jail != null) {
 			assert (false);
 			double valuation = getResults(jail.getPosition(), jail.getMoneyToPay());
 			if (valuation > 0) {
 				Command result = new OutOfPrisonCommand(logic, server, playerID);
 				result.setValuation(valuation);
 				return result;
 			}
 		}
 		Command result = new EndTurnCommand(logic, server, playerID);
 		result.setValuation(0);
 		return result;
 	}
 
 	private void initFunctions() {
 		for (ValuationFunction function : valuationFunctions) {
 			assert (function != null);
 			function.setParameters(logic);
 			function.setServer(server);
 		}
 	}
 
 }
