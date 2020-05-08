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
 import java.util.LinkedList;
 import java.util.PriorityQueue;
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
 import org.ojim.log.OJIMLogger;
 import org.ojim.logic.Logic;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.Jail;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.logic.state.fields.StreetFieldGroup;
 
 import edu.kit.iti.pse.iface.IServer;
 
 /**
  * Valuator - returns the best command
  * 
  * @author Jeremias Mechler
  * 
  */
 public class Valuator extends SimpleClient {
 
 	private double[] weights;
 	private ValuationFunction[] valuationFunctions;
 	private Logic logic;
 	private int playerID;
 	private IServer server;
 	private Logger logger;
 	private int auctionBid;
 	// private PriorityQueue<Property> properties;
 	private int auctionSteps = 11;
 	private int currentStep = 2;
 	// private LinkedList<Property> list;
 	private int[] toSell;
 	private boolean endTurn = false;
 	
 
 	/**
 	 * Constructor
 	 * 
 	 * @param state
 	 *            reference to state
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
 	}
 
 	/**
 	 * Returns the best command
 	 * 
 	 * @param position
 	 *            current position
 	 * @return command
 	 */
 	public Command returnBestCommand(int position) {
 		assert(this.getNumberOfGetOutOfJailCards(playerID) == 0);
 		assert (position >= 0);
 		Field field = getGameState().getFieldAt(Math.abs(position));
 		for (ValuationFunction function : valuationFunctions) {
 			assert (function != null);
 			function.setParameters(logic);
 			function.setServer(server);
 		}
 
 		// OJIMLogger.changeGlobalLevel(Level.WARNING);
 		// OJIMLogger.changeLogLevel(logger, Level.FINE);
 
 		// Feld potenziell kaufbar
 		if (field instanceof BuyableField && !endTurn) {
 			logger.log(Level.FINE, "BuyableField!");
 			Player owner = ((BuyableField) field).getOwner();
 			double realValue = getResults(position, 0);
 			// Feld gehört mir (noch) nicht
 			if (owner != getMe()) {
 				int price = ((BuyableField) field).getPrice();
 				double valuation = getResults(position, price);
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
 //					return new NullCommand(logic, server, playerID);
 				}
 			}
 
 			// Feld potentiell bebaubar
 			if (field instanceof Street) {
 				Street street = (Street) field;
 				// Feld bebaubar
 //				if (allOfGroupOwned(street)) {
 //					assert(false);
 //				}
 				if (allOfGroupOwned(street) && this.getNumberOfHousesLeft() > 0) {
 					logger.log(Level.FINE, "Owning everything in group " + street.getColorGroup());
 					// assert(false);
 					double valuation = getResults(position, this.getEstateHousePrice(street.getPosition()));
 					// houses, hotels, real price?
 					// if (valuation > 0) {
 					if (true) {
 						// int oldLevel = street.getNumberOfHouse();
 						System.out.println(this.getEstateHouses(street.getPosition()));
 						System.out.println(this.getNumberOfHousesLeft());
 
 						// construct(street);
 						// this.
 						// street.upgrade(street.getNumberOfHouse() + 1);
 						// assert (street.getNumberOfHouse() == oldLevel + 1);
 						if (this.getEstateHouses(street.getPosition()) == 1) {
 							assert (false);
 							logger.log(Level.FINE, "All houses owned on " + street.getPosition());
 						}
 //						assert(false);
 						return new BuildHouseCommand(logic, server, playerID, street);
 						// assert(false);
 					} else {
 						// assert(false);
 					}
 				}
 			}
 		}
 
 		// reicht das?
 		else if (field instanceof Jail) {
 			double valuation = getResults(position, 0);
 			// To be tested!
 			if (valuation > 0 && position == -10) {
 				return new OutOfPrisonCommand(logic, server, playerID);
 			} else {
 				// null command?
 				System.out.println(valuation);
 				// assert (false);
 				endTurn = false;
 				return new EndTurnCommand(logic, server, playerID);
 			}
 		}
 		endTurn = false;
 		return new EndTurnCommand(logic, server, playerID);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Command actOnTradeOffer() {
 		System.out.println("Trade!");
 		assert (getTradeState() == 1);
 		boolean restricted = false;
 		if (getRequiredEstates() != null) {
 			for (int position : getRequiredEstates()) {
 				BuyableField field = (BuyableField) getGameState().getFieldAt(position);
 				if (field.isMortgaged()) {
 					restricted = true;
 				}
 				if (field instanceof Street && ((Street) field).getBuiltLevel() > 0) {
 					restricted = false;
 				}
 			}
 		}
 		if (!restricted) {
 			double value = getOfferedCash();
 			double minus = tradeValuateRequestedEstates();
 			value += tradeValuateJailCards();
 			value += tradeValuateOfferedEstates();
 			minus += valuationFunctions[0].returnValuation(this.getRequiredCash());
 			// missing: out of jail cards!
 			if (value + minus > 0) {
 				return new AcceptCommand(logic, server, playerID);
 			}
 		}
 		return new DeclineCommand(logic, server, playerID);
 	}
 
 	public Command actOnAuction() {
 		int auctionState = getAuctionState();
 		assert (auctionState >= 0);
 		int realValue = (int) getResults(getAuctionedEstate(), 0);
 		getAuctionState();
 		getBidder();
 		getHighestBid();
 		logger.log(Level.FINE, "state = " + getAuctionState() + " bidder = " + getBidder() + " highesBid = " + getHighestBid() + " value = " + realValue);
 
 		if ((auctionState = getAuctionState()) < 3 && getBidder() != playerID && getHighestBid() < realValue
 				&& currentStep < auctionSteps) {
 			logger.log(Level.FINE, "Highest bid = " + getHighestBid());
 			if (getHighestBid() < realValue) {
 				logger.log(Level.FINE, "Valuation " + realValue);
 				double factor = Math.log(currentStep++)/Math.log(auctionSteps);
 				auctionBid = (int)(factor * realValue);
 				logger.log(Level.FINE, "Bidding " + auctionBid);
 
 				if (getResults(getAuctionedEstate(), auctionBid) > 0) {
 					System.out.println("THere!");
 //					assert(false);
 					return new AuctionBidCommand(logic, server, playerID, auctionBid);
 				} else {
 					assert (false);
 				}
 			}
 		}
 		if (getAuctionState() == 3) {
 			currentStep = 2;
 		}
 		return new NullCommand(logic, server, playerID);
 	}
 
 	private double getResults(int position, int amount) {
 		double result = weights[0] * valuationFunctions[0].returnValuation(amount);
 		for (int i = 1; i < valuationFunctions.length; i++) {
 			result += weights[i] * valuationFunctions[i].returnValuation(position);
 		}
 		return result;
 	}
 
 	private double tradeValuateJailCards() {
 		int offeredCards = getNumberOfOfferedGetOutOfJailCards();
 		int difference = ValuationParameters.desiredNumberOfOutOfOjailCards - getNumberOfGetOutOfJailCards(playerID);
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
 
 	private double tradeValuateEstates(int[] estates) {
 		double result = 0;
 		for (int estate : estates) {
 			result += getResults(estate, 0);
 		}
 		return result;
 	}
 
 	private double tradeValuateRequestedEstates() {
 		return (-1) * tradeValuateEstates(getRequiredEstates());
 	}
 
 	private double tradeValuateOfferedEstates() {
 		return tradeValuateEstates(getOfferedEstates());
 	}
 
 	private boolean decideWhetherToSell(int buyPosition, int sellPosition) {
 		return (getResults(buyPosition, 0) > getResults(sellPosition, 0));
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
 		Field field = logic.getGameState().getFieldAt(position);
 		assert (field instanceof BuyableField);
 		return ((BuyableField) field).getPrice();
 	}
 
 	private boolean allOfGroupOwned(Street street) {
 		StreetFieldGroup group = street.getFieldGroup();
 		if (group.getFields().length > 1) {
 			int count = 0;
 			for (Field field : group.getFields()) {
 				if (((BuyableField) field).getOwner() == getMe()) {
 					count++;
 				}
 			}
 			return (count == group.getFields().length);
 		} else {
 			System.out.println(street.getFieldGroup().getName());
 			return false;
 		}
 	}
 	
 //	private boolean
 	
 }
