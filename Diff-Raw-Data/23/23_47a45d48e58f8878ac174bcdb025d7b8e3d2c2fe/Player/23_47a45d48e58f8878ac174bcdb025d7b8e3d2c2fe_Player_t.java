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
 
 import org.ojim.logic.accounting.IMoneyPartner;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Jail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Player implements IMoneyPartner {
 
 	//TODO: Add NullPlayer behaviour
 	public static final Player NullPlayer = new Player();
 	
 	/**
 	 * Name to identify the player
 	 */
 	private String name;
 
 	/** The position of the player on the GameBoard */
 	private int position;
 
 	/** Amount of cash the player possesses */
 	private int balance;
 
 	/** The ID of the player */
 	private int id;
 
 	/** The color that symbolizes the Player */
 	private int color;
 	
 	/**
 	 * Number of Rounds the Player has to stay in Jail
 	 */
 	private int roundsInJail;
 
 	/** Contains all fields which this player owns. */
 	private List<BuyableField> fields;
 	
 	public Player() {
 		this.fields = new ArrayList<BuyableField>();
 	}
 	
 	public int getRoundsInJail() {
 		return this.roundsInJail;
 	}
 	
 	/**
 	 * Is the Player ready to start the Game
 	 */
 	private boolean isReady;
 
 	/**
 	 * NULL if Player is not in jail
 	 */
 	private Jail jail;
 	
 	private boolean isBankrupt;
 
 	private int numberOfGetOutOfJailCards;
 
 	/**
 	 * Creates a new player object.
 	 * 
 	 * @param name
 	 *            The name of the player.
 	 * @param position
 	 *            The position on the board.
 	 * @param balance
 	 *            The starting balance.
 	 * @param id
 	 *            The unique player ID.
 	 * @param color
 	 *            The color of the player.
 	 */
 	public Player(String name, int position, int balance, int id, int color) {
 		this.fields = new ArrayList<BuyableField>();
 		
 		this.name = name;
 		this.position = position;
 		this.balance = balance;
 		this.id = id;
 		this.color = color;
 		this.isReady = false;
 		this.jail = null;
 		this.isBankrupt = false;
 	}
 
 	/**
 	 * Transfers the amount of money to the player.
 	 * 
 	 * @param amount
 	 *            the amount of money.
 	 */
 	public void transferMoney(int amount) {
 		this.balance += amount;
 	}
 	
 	public boolean getIsBankrupt() {
 		return this.isBankrupt;
 	}
 	
 	public void setBankrupt() {
 		this.isBankrupt = true;
 	}
 	
 	public void waitInJail() {
 		this.roundsInJail--;
 		if(this.roundsInJail < 1) {
 			this.jail = null;
 		}
 	}
 
 	public void sendToJail(Jail jail) {
 		this.jail = jail;
 		this.roundsInJail = jail.getRoundsToWait();
 	}
 	
 	public int getSignedPosition() {
 		if(this.jail == null) {
 			return this.position;
 		} else {
 			return -this.position;
 		}
 	}
 	
 	public int getColor() {
 		return this.color;
 	}
 
 	public void setIsReady(boolean ready) {
 		this.isReady = ready;
 	}
 
 	public boolean getIsReady() {
 		return this.isReady;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public int getPosition() {
 		return this.position;
 	}
 
 	public int getId() {
 		return this.id;
 	}
 
 	public int getBalance() {
 		return this.balance;
 	}
 
 	public BuyableField[] getFields() {
 		return this.fields.toArray(new BuyableField[0]);
 	}
 
 	/* FIELD STACK */
 	
 	public void addField(BuyableField field) {
 		if (!this.fields.contains(field)) {
 			this.fields.add(field);
 			if (!field.getOwner().equals(this)) {
 				field.buy(this);
 			}
 		}
 	}
 
 	public void removeField(BuyableField field) {
 		if (this.fields.remove(field)) {
 			if (field.getOwner().equals(this)) {
 				field.buy(null);
 			}
 		}
 	}
 
 	public Jail getJail() {
 		return this.jail;
 	}
 
 	public void setPosition(int position) {
 		this.position = position;
 	}
 
 	public void setNumberOfGetOutOfJailCards(int numberOfGetOutOfJailCards) {
 		this.numberOfGetOutOfJailCards = numberOfGetOutOfJailCards;
 	}
 	
 	public int getNumberOfGetOutOfJailCards() {
 		return this.numberOfGetOutOfJailCards;
 	}
 	
 }
