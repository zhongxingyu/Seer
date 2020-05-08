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
 
 package org.ojim.logic.rules;
 
 import org.ojim.iface.Rules;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.Street;
 
 public class GameRules {
 
 	public static final int MAX_DOUBLES_ALLOWED = 3;
 	private GameState state;
 	private Rules rules;
 	
 	public GameRules(GameState state, Rules rules) {
 		this.state = state;
 		this.rules = rules;
 	}
 	
 	public boolean isFieldUpgradable(Player player, Field field, int levelChange) {
 		if(!canPlayerModifyStreet(player, field)) {
 			return false;
 		}
 
 		Street street = (Street) field;
 		
 		//When Upgrading, check for Money
 		if(levelChange > 0) {
 			if(player.getBalance() < levelChange * street.getFieldGroup().getHousePrice()) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isFieldMortgageable(Player player, BuyableField buyField, boolean mortgage) {
 	
 		//The Player can't spend more money than he has
 		if(player.getBalance() < buyField.getMortgagePrice() && mortgage) {
 			return false;
 		}
 		
 		//When the Street already has the needed state, then we are done
 		if((buyField.isMortgaged() ^ mortgage)) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public int getGoMoney(){
 		return this.rules.goMoney;
 	}
 	
 	public boolean getDoubleMoneyOnGo() {
 		return this.rules.doubleMoneyOnGo;
 	}
 	
 	
 	/**
 	 * The Start Time for an Auction, between a Auction started and the first call
 	 * @return
 	 */
 	public int getActionStartTime() {
 		return 20;
 	}
 	
 	/**
 	 * The Time between two calls or ticks in an Auction
 	 * @return
 	 */
 	public int getActionTickTime() {
 		return 5;
 	}
 	
 	public boolean isFieldMortgageable(Player player, Field field) {
 		if(!canPlayerModifyBuyableField(player, field)) {
 			return false;
 		}
 		
 		return this.isFieldMortgageable(player, field, !((BuyableField) field).isMortgaged());
 	}
 	
 	public boolean isFieldMortgageable(Player player, Field field, boolean mortgage) {
 		if(!canPlayerModifyBuyableField(player, field)) {
 			return false;
 		}
 		
 		return this.isFieldMortgageable(player, (BuyableField) field, mortgage);		
 	}
 	
 	public boolean isPlayerOnTurn(Player player) {
		return player != this.state.getActivePlayer();
 	}
 	
 	private boolean canPlayerModifyStreet(Player player, Field street) {
 		if (!canPlayerModifyBuyableField(player, street)) {
 			return false;
 		}
 		
 		//Is the Field at position a street?
 		if(!(street instanceof Street)) {
 			return false;
 		}
 		
 		return true;
 	}
 
 	private boolean canPlayerModifyBuyableField(Player player, Field field) {
 		//Is the player on turn?
 		if(player != this.state.getActivePlayer()) {
 			return false;
 		}
 		
 		//Is the Field at position a buyable field?
 		if(!(field instanceof BuyableField)) {
 			return false;
 		}
 		
 		//Is the player on turn owner of the street?
 		if(this.state.getActivePlayer() != ((BuyableField) field).getOwner()) {
 			return false;
 		}
 		
 		return true;
 	}
 
 	public boolean isPlayerInPrison(Player player) {
 		if(player.getJail() != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean canPlayerGetOutOfJail(Player player, boolean usesGetOutOfJailCard) {
 		if(usesGetOutOfJailCard) {
 			if(player.getNumberOfGetOutOfJailCards() > 0) {
 				return true;
 			}
 		} else {
 			if(player.getBalance() >= player.getJail().getMoneyToPay()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isRollRequiredByActivePlayer() {
 		if(this.state.getActivePlayerNeedsToRoll()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Gets the Starting Bid for an Auction, normally this is 0
 	 * @param objective The Field that is Auctioned
 	 * @return The Minimum Value of the Auctioned Field
 	 */
 	public int getAuctionStartBid(BuyableField objective) {
 		return 0;
 	}
 }
