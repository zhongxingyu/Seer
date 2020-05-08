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
 
 package org.ojim.logic.state.fields;
 
 import java.util.Map;
 
 import org.jdom.DataConversionException;
 import org.jdom.Element;
 import org.ojim.logic.ServerLogic;
 import org.ojim.logic.accounting.Bank;
 import org.ojim.logic.actions.ActionPayFieldRent;
 import org.ojim.logic.state.Player;
 
 /**
  * A fields, that is able to buy.
  * 
  * @author Fabian Neundorf
  */
 public abstract class BuyableField extends Field {
 
 	private int price;
 	private Player owner;
 	
 	private boolean mortgaged;
 	private int mortgagePrice;
 
 	public BuyableField(String name, int position, int price) {
 		super(name, position);
 		this.price = price;
 		this.mortgagePrice = this.price / 2;
 	}
 	
 	public BuyableField(String name, int position, int price, ServerLogic logic) {
 		this(name, position, price);
 		this.setExecuteActions(new ActionPayFieldRent(logic, this));
 	}
 	
 	public BuyableField(Element element, ServerLogic logic, Map<Integer, FieldGroup> groups) throws DataConversionException {
 		super(element, groups);
 		this.price = Integer.parseInt(element.getChild("price").getText());
 		this.mortgagePrice = this.price / 2;
 		this.setExecuteActions(new ActionPayFieldRent(logic, this));
 	}
 	
 	public void buy(Player newOwner) {
 		// Remove previous owner
 		if (this.owner != null) {
 			this.owner.removeField(this);
 		}
 		this.owner = newOwner;
 		if (this.owner != null) {
 			this.owner.addField(this);
 		}
 	}
 
 	public abstract int getRent();
 
 	public int getPrice() {
 		return this.price;
 	}
 
 	public boolean isMortgaged() {
 		return this.mortgaged;
 	}
 
 	public int getMortgagePrice() {
 		return this.mortgagePrice;
 	}
 
 	public void setMortgaged(boolean mortgaged) {
 		this.mortgaged = mortgaged;
 	}
 
 	/**
 	 * Pays the rent from the player to the owner.
 	 * 
 	 * @param player
 	 *            the player who got to the field.
 	 */
 	public void payRent(Player player) {
		if (this.owner != null && !this.owner.equals(player)) {
 			Bank.exchangeMoney(player, this.owner, this.getRent());
 		}
 	}
 	
 	/**
 	 * Returns the owner of the user.
 	 * @return the owner of the user. If this field is buyable it returns <code>null</code>.
 	 */
 	public Player getOwner() {
 		return this.owner;
 	}
 }
