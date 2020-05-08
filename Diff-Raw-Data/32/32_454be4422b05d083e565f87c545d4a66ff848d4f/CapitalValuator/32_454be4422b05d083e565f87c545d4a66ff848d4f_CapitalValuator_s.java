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
 
 import java.util.logging.Level;
 
 import org.ojim.client.ai.commands.SellCommand;
 import org.ojim.log.OJIMLogger;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Field;
 
 /**
  * 
  * Die Klasse CapitalValuator bewertet, ob sich der KI-Client leisten kann, Geld auszugeben
  * 
  * @author Jeremias Mechler
  */
 public final class CapitalValuator extends ValuationFunction {
 
 	protected CapitalValuator() {
 	}
 
 	private static boolean hackDisable = true;
 
 	/**
 	 * This is a singleton object!
 	 * 
 	 * @return An instance
 	 */
 	public static CapitalValuator getInstance() {
 		return ValuationFunction.getInstance(false, CapitalValuator.class);
 	}
 
 	/**
 	 * Sometimes we have to specify an amount, for example if we want to buy something
 	 * 
 	 * @param amount
 	 *            Amount of money
 	 * @return Valuation 0 if granted, -1 if denied
 	 */
 	@Override
 	public double returnValuation(int amount) {
 
 		// Da die Klasse ein Singleton ist, muss der aktive Spieler bei jedem
 		// Aufruf bestimmt werden
 		getLogger();
 //		OJIMLogger.changeLogLevel(logger, Level.FINE);
 		Player currentPlayer = this.getGameState().getActivePlayer();
 		logger.log(Level.FINE, "Current cash = " + currentPlayer.getBalance() + " Price = " + amount);
 		// Die Gesamtgeldsumme aller Gegenspieler
 		int sum = 0;
 		// Der Geldbetrag des Spielers mit dem meisten Geld
 		int max = 0;
 		int count = 0;
 		Player[] players = this.getGameState().getPlayers();
 		for (Player player : players) {
 			if (player == currentPlayer) {
 				continue;
 			}
 			sum += player.getBalance();
 			count++;
 			if (player.getBalance() > max) {
 				max = player.getBalance();
 			}
 		}
 		assert(server != null);
		double required = ValuationParameters.baseCash + 0.01 * (max + (sum / count)) + 0.05 * (40 - server.getNumberOfHousesLeft());
 
 
 		// Nach dem Papier
 //		double required = ValuationParameters.baseCash
 //		 + ValuationParameters.averageCashPercentage
 //		 * (((double) sum / (double) (players.length - 1)) + sum)
 //				+ ValuationParameters.maxCashPercentage * max;
 		logger.log(Level.FINE, "Required = " + required);
 
 		if (currentPlayer.getBalance() - amount >= required) {
 			logger.log(Level.FINE, "Granted");
 			return 0;
 		} else {
 			logger.log(Level.FINE, "Denied");
 			// HACK: Sell!
 			if (!hackDisable) {
 				if (currentPlayer.getBalance() < 1000) {
 					int field = -1;
 					for (int i = 0; i < 40; i++) {
 						if (field == -1) {
 							Field bla = getGameState().getFieldAt(i);
 							if (bla instanceof BuyableField) {
 								if (((BuyableField) bla).getOwner().getId() == currentPlayer.getId()) {
 									field = i;
 								}
 							}
 						}
 					}
 					if (field != -1) {
 						// assert (false);
 						new SellCommand(getLogic(), getServer(), getGameState().getActivePlayer().getId(), field, 2, 1)
 								.execute();
 						hackDisable = true;
 					}
 				}
 			}
 			return -1;
 
 		}
 	}
 }
