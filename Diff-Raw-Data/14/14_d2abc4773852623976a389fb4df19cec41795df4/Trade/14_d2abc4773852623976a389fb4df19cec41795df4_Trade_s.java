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
 
 package org.ojim.logic.state;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ojim.logic.Logic;
 import org.ojim.logic.ServerLogic;
 import org.ojim.logic.accounting.Bank;
 import org.ojim.logic.rules.GameRules;
 import org.ojim.logic.state.fields.BuyableField;
 
 /**
  * Saves everything related to a trade.
  * 
  * @author Philip Caroli, Fabian Neundorf
  */
 public class Trade {
 	
 	private boolean tradeWithBank;
 	private final ServerPlayer acting;
 	private final ServerPlayer partner;
 	private int tradeState;
 	private Bank bank;
 	private GameRules rules;
 	
 	private int offeredCash, requiredCash;
 	private int offeredNumberOfGetOutOfJailCards, requiredNumberOfGetOutOfJailCards; //xZise: Jay :D
 	private List<BuyableField> offeredEstates;
 	private List<BuyableField> requiredEstates;
 
 	public Trade(ServerPlayer acting, ServerPlayer partner) {
 		this.acting = acting;
 		this.partner = partner;
 		
 		this.tradeState = 0;
 		this.offeredCash = 0;
 		this.requiredCash = 0;
 		this.offeredNumberOfGetOutOfJailCards = 0;
 		this.requiredNumberOfGetOutOfJailCards = 0;
 		this.offeredEstates	= new ArrayList<BuyableField>();
 		this.requiredEstates = new ArrayList<BuyableField>();
 	}
 	
 	public Trade(ServerPlayer acting, Bank bank, GameRules rules) {
 		this.acting = acting;
 		this.rules = rules;
 		this.bank = bank;
 		this.partner = null;
 		this.tradeWithBank = true;
 	}
 
 	public int getTradeState() {
 		return this.tradeState;
 	}
 	
 	public void setTradeState(int state) {
 		this.tradeState = state;
 	}
 	
 	/**
 	 * @return the acting
 	 */
 	public ServerPlayer getActing() {
 		return this.acting;
 	}
 
 	/**
 	 * @return the partner
 	 */
 	public ServerPlayer getPartner() {
 		return this.partner;
 	}
 
 	/**
 	 * @return the offered cash
 	 */
 	public int getOfferedCash() {
 		return this.offeredCash;
 	}
 	
 	/**
 	 * @return the required cash
 	 */
 	public int getRequiredCash() {
 		int cash = this.getOfferedCash();
 		for(BuyableField field : this.offeredEstates) {
 			cash += rules.getFieldValueForBank(field);
 		}
 		cash += this.offeredNumberOfGetOutOfJailCards * 500;
 		return cash;
 	}
 
 	/**
 	 * @return the required number of GetOutOfJailCards
 	 */
 	public int getRequiredNumberOfGetOutOfJailCards() {
 		return this.requiredNumberOfGetOutOfJailCards;
 	}
 	
 	/**
 	 * @return the offered number of GetOutOfJailCards
 	 */
 	public int getOfferedNumberOfGetOutOfJailCards() {
 		return this.offeredNumberOfGetOutOfJailCards;
 	}
 
 	public void setOfferedCash(int amount) {
 		this.offeredCash = amount;
 	}
 	
 	public void setRequiredCash(int amount) {
 		this.requiredCash = amount;
 	}
 
 	/**
 	 * Sets the number of "get out of jail cards" in the trade.
 	 * @param amount The number of cards. Set it to a negative value to receive the cards. 
 	 */	
 	public void setOfferedNumberOfGetOutOfJailCards(int count) {
 		this.offeredNumberOfGetOutOfJailCards = count;
 	}
 	
 	/**
 	 * Sets the number of "get out of jail cards" in the trade.
 	 * @param amount The number of cards. Set it to a negative value to receive the cards. 
 	 */	
 	public void setRequiredNumberOfGetOutOfJailCards(int count) {
 		this.requiredNumberOfGetOutOfJailCards = count;
 	}
 	
 	/**
 	 * Adds a estate to the selling list.
 	 * @param estate The selling estate.
 	 * @return True if the estate wasn't on the list before. Otherwise false.
 	 */
 	public boolean addOfferedEstate(BuyableField estate) {
 		if(estate.getOwner() == acting) {
 			return this.offeredEstates.add(estate);
 		}
 		return false;
 	}
 	
 	/**
 	 * Adds a estate to the buying list.
 	 * @param estate The buying estate.
 	 * @return True if the estate wasn't on the list before. Otherwise false.
 	 */
 	public boolean addRequiredEstate(BuyableField estate) {
 		if(!tradeWithBank && estate.getOwner() == partner) {
 			return this.requiredEstates.add(estate);
 		}
 		return false;
 	}
 	
 	public List<BuyableField> getOfferedEstates() {
 		return this.offeredEstates;
 	}
 	
 	public List<BuyableField> getRequiredEstates() {
 		return this.requiredEstates;
 	}
 	
 	public boolean removeOfferedEstate(BuyableField estate) {
 		return this.offeredEstates.remove(estate);
 	}
 	
 	public boolean removeRequiredEstate(BuyableField estate) {
 		return this.requiredEstates.remove(estate);
 	}
 
 	public void executeTrade(ServerLogic logic) {
 		//Change the Estates
 		for(BuyableField field : this.requiredEstates) {
 			logic.changeFieldOwner(partner, acting, field);
 		}
 		for(BuyableField field : this.offeredEstates) {
 			logic.changeFieldOwner(acting, acting, field);
 		}
 		
 		//TODO Do the Exchange of GetOutOfJailCards
 		
 		
 		//Change the Cash
 		if(tradeWithBank) {
 			logic.exchangeMoney(acting, bank, getOfferedCash() - getRequiredCash());
 		}
 		logic.exchangeMoney(acting, partner, getOfferedCash() - getRequiredCash());
 	}
 }
